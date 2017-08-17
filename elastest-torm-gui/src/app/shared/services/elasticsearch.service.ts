import { ConfigModel } from '../../config/config-model';
import { ConfigurationService } from '../../config/configuration-service.service';

import { Injectable } from '@angular/core';
import { Http, Request, RequestMethod, RequestOptions, Response } from '@angular/http';
import { Subject } from 'rxjs/Rx';
import 'rxjs/Rx';

@Injectable()
export class ElasticSearchService {

  public rowData: any[] = [];
  _scroll_id: string;
  noMore: boolean = false;

  esUrl: string;

  constructor(public http: Http, private configurationService: ConfigurationService) {
    this.rowData = [];
    this.esUrl = this.configurationService.configModel.hostElasticsearch;
  }

  internalSearch(url: string, query: any, maxResults: number, append: boolean = false) {
    // console.log('URL:', url, 'Query:', query);

    let requestOptions = new RequestOptions({
      method: RequestMethod.Post,
      url,
      body: JSON.stringify(query)
    });

    return this.http.request(new Request(requestOptions))
      .map((res: Response) => {
        return res.json();
      }, (err: Response) => {
        console.error('Error:', err);
      });
  }

  getIndexTraceCount(url: string) {
    return this.getInfo(url + '_count');
  }

  getInfo(url: string) {
    let requestOptions = new RequestOptions({
      method: RequestMethod.Get,
      url
    });

    return this.http.request(new Request(requestOptions))
      .map((res: Response) => {
        return res.json();
      }, (err: Response) => {
        console.error('Error:', err);
      });
  }

  getIndices(url: string) {
    let requestOptions = new RequestOptions({
      method: RequestMethod.Get,
      url
    });

    return this.http.request(new Request(requestOptions))
      .map((res: Response) => {
        return res.json();
      }, (err: Response) => {
        console.error('Error:', err);
      });
  }


  /**
   * Search and return all hits recursively by terms and/or given query
   * @param index 
   * @param terms 
   * @param theQuery optional
   */
  searchAllByTerm(index: string, terms: any[], theQuery?: any) {
    let size: number = 1000;
    let url = this.esUrl + index;
    let searchUrl: string = url + '/_search';

    if (theQuery === undefined || theQuery === null) {
      theQuery = {
        sort: [
          { '@timestamp': 'asc' }
        ],
        query: {
          bool: {
            must: terms
          }
        },
        size: size,
      };
    }
    else {
      theQuery['size'] = size;
    }

    let _logs = new Subject<string[]>();
    let logs = _logs.asObservable();

    this.internalSearch(searchUrl, theQuery, size)
      .subscribe(
      (data) => {
        let dataReceived: number = data.hits.hits.length;
        if (dataReceived > 0) {
          let lastReceivedPos: number = dataReceived - 1;
          let sortId: number = data.hits.hits[lastReceivedPos].sort[0];
          theQuery['search_after'] = [sortId];

          this.searchAllByTerm(index, terms, theQuery).subscribe(
            (result) => {
              _logs.next(data.hits.hits.concat(result));
            },
            (error) => console.error(error)
          );
        }
        else {
          _logs.next([]);
        }
      });

    return logs;
  }

  /**
   * Search and return all messages recursively by terms and/or given query
   * @param index 
   * @param terms 
   * @param theQuery optional
   */
  searchAllMessagesByTerm(index: string, terms: any[], theQuery?: any) {
    let _logs = new Subject<string[]>();
    let logs = _logs.asObservable();
    this.searchAllByTerm(index, terms, theQuery)
      .subscribe(
      (data) => {
        let messages: string[] = this.returnMessages(data);
        _logs.next(messages);
      }
      );
    return logs;
  }

  returnMessages(data: any) {
    let messagesList: string[] = [];
    for (let logEntry of data) {
      if (logEntry._source['message'] !== undefined) {
        messagesList.push(logEntry._source['message']);
      }
    }
    return messagesList;
  }

  inverseSearch(index: string, terms: any[], data: any, _logs: Subject<string[]>) {
    let dataReceived: number = data.hits.hits.length;
    if (dataReceived > 0) {
      let sortId: number = data.hits.hits[0].sort[0];

      let newQuery: any = {
        search_after: [sortId],
        sort: [
          { '@timestamp': 'desc' }
        ],
        query: {
          bool: {
            must: terms
          }
        },
      }

      this.searchAllByTerm(index, terms, newQuery).subscribe(
        (messages) => {
          let orderedMessages: string[] = messages.reverse();
          _logs.next(orderedMessages);
        }
      );
    }
    else {
      _logs.next([]);
    }
  }


  // Prev by message
  /**
   * Search and return all previous hits from given message and by terms
   * @param index 
   * @param terms 
   * @param theQuery optional
   */
  getPrevFromGivenMessage(index: string, fromMessage: string, terms: any[]) {
    let url = this.esUrl + index;

    let _logs = new Subject<string[]>();
    let logs = _logs.asObservable();

    this.searchMessage(url, fromMessage, terms) //To Do: recursive search (if message is not in first 10000)
      .subscribe(
      (data) => {
        this.inverseSearch(index, terms, data, _logs);
      }
      );

    return logs;
  }

  searchMessage(url: string, fromMessage: string, terms: any[]) {
    let must: any[] = [
      {
        match_phrase: { message: fromMessage }
      }
    ];

    must = must.concat(terms);

    let searchUrl: string = url + '/_search';
    let theQuery = {
      sort: [
        { '@timestamp': 'asc' }
      ],
      query: {
        bool: {
          must: must
        }
      }
    };

    return this.internalSearch(searchUrl, theQuery, 10000);
  }


  /**
   * Search and return all previous messages from given message and by terms
   * @param index 
   * @param terms 
   * @param theQuery optional
   */
  getPrevMessagesFromGivenMessage(index: string, fromMessage: string, terms: any[]) {
    let _logs = new Subject<string[]>();
    let logs = _logs.asObservable();
    this.getPrevFromGivenMessage(index, fromMessage, terms)
      .subscribe(
      (data) => {
        let messages: string[] = this.returnMessages(data);
        _logs.next(messages);
      }
      );
    return logs;
  }


  // Prev by ID

  /**
   * Search and return all previous hits from given id and by terms
   * @param index 
   * @param terms 
   * @param theQuery optional
   */
  getPrevFromId(index: string, id: string, terms: any[]) {
    let url = this.esUrl + index;

    let _logs = new Subject<string[]>();
    let logs = _logs.asObservable();

    this.searchById(url, id, terms) //To Do: recursive search (if id is not in first 10000)
      .subscribe(
      (data) => {
        this.inverseSearch(index, terms, data, _logs);
      }
      );

    return logs;
  }

  searchById(url: string, id: string, terms: any[]) {
    let must: any[] = [
      { 'term': { _id: id } }
    ];

    must = must.concat(terms);

    let searchUrl: string = url + '/_search';
    let theQuery = {
      sort: [
        { '@timestamp': 'asc' }
      ],
      query: {
        bool: {
          must: must
        }
      }
    };

    return this.internalSearch(searchUrl, theQuery, 10000);
  }

  /**
   * Search and return all previous messages from given id and by terms
   * @param index 
   * @param terms 
   * @param theQuery optional
   */
  getPrevMessagesFromGivenId(index: string, id: string, terms: any[]) {
    let _logs = new Subject<string[]>();
    let logs = _logs.asObservable();
    this.getPrevFromId(index, id, terms)
      .subscribe(
      (data) => {
        let messages: string[] = this.returnMessages(data);
        _logs.next(messages);
      }
      );
    return logs;
  }




  // Prev by timestamp

  /**
   * Search and return all previous hits from given timestamp and by terms
   * @param index 
   * @param terms 
   * @param theQuery optional
   */
  getPrevFromTimestamp(index: string, timestamp: string, terms: any[]) {
    let url = this.esUrl + index;

    let _logs = new Subject<string[]>();
    let logs = _logs.asObservable();

    this.searchByTimestamp(url, timestamp, terms) //To Do: recursive search (if timestamp is not in first 10000)
      .subscribe(
      (data) => {
        this.inverseSearch(index, terms, data, _logs);
      }
      );

    return logs;
  }

  searchByTimestamp(url: string, timestamp: string, terms: any[]) {
    let must: any[] = [
      { 'term': { '@timestamp': timestamp } }
    ];

    must = must.concat(terms);

    let searchUrl: string = url + '/_search';
    let theQuery = {
      sort: [
        { '@timestamp': 'asc' }
      ],
      query: {
        bool: {
          must: must
        }
      }
    };

    return this.internalSearch(searchUrl, theQuery, 10000);
  }

  /**
   * Search and return all previous messages from given timestamp and by terms
   * @param index 
   * @param terms 
   * @param theQuery optional
   */
  getPrevMessagesFromTimestamp(index: string, timestamp: string, terms: any[]) {
    let _logs = new Subject<string[]>();
    let logs = _logs.asObservable();
    this.getPrevFromTimestamp(index, timestamp, terms)
      .subscribe(
      (data) => {
        let messages: string[] = this.returnMessages(data);
        _logs.next(messages);
      }
      );
    return logs;
  }
}