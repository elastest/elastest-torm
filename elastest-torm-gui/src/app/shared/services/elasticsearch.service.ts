import { ConfigModel } from '../../config/config-model';
import { ConfigurationService } from '../../config/configuration-service.service';

import { Injectable } from '@angular/core';
import { Http, Request, RequestMethod, RequestOptions, Response } from '@angular/http';
import { Subject, Observable } from 'rxjs/Rx';
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

  getSearch(url: string): Observable<any> {
    let requestOptions: RequestOptions = new RequestOptions({
      method: RequestMethod.Get,
      url,
    });

    return this.http.request(new Request(requestOptions)).map(
      (res: Response) => {
        return res.json();
      },
      (err: Response) => {
        console.error('Error:', err);
      },
    );
  }

  internalSearch(url: string, query: any): Observable<any> {
    // console.log('URL:', url, 'Query:', query);

    let requestOptions: RequestOptions = new RequestOptions({
      method: RequestMethod.Post,
      url,
      body: JSON.stringify(query),
    });

    return this.http.request(new Request(requestOptions)).map(
      (res: Response) => {
        return res.json();
      },
      (err: Response) => {
        console.error('Error:', err);
      },
    );
  }

  getIndexTraceCount(url: string): Observable<any> {
    return this.getInfo(url + '_count');
  }

  getInfo(url: string): Observable<any> {
    let requestOptions: RequestOptions = new RequestOptions({
      method: RequestMethod.Get,
      url,
    });

    return this.http.request(new Request(requestOptions)).map(
      (res: Response) => {
        return res.json();
      },
      (err: Response) => {
        console.error('Error:', err);
      },
    );
  }

  getIndices(): Observable<any> {
    let url: string = this.esUrl + '_aliases';
    return this.getSearch(url).map(
      (res: Response) => {
        return Object.keys(res);
      },
      (err: Response) => {
        console.error('Error:', err);
      },
    );
  }

  /**
   * Search and return all hits recursively by terms and/or given query
   * @param index
   * @param terms
   * @param theQuery optional
   */
  searchAllByTerm(index: string, terms: any[], theQuery?: any, filterPath?: string[]): Observable<string[]> {
    let size: number = 1000;
    let url: string = this.esUrl + index;
    let searchUrl: string = url + '/_search';

    searchUrl = this.addFilterToSearchUrl(searchUrl, filterPath);

    if (theQuery === undefined || theQuery === null) {
      theQuery = this.getDefaultQuery(terms);
    }
    theQuery['size'] = size;
    let _logs: Subject<string[]> = new Subject<string[]>();
    let logs: Observable<string[]> = _logs.asObservable();

    this.internalSearch(searchUrl, theQuery).subscribe((data) => {
      if (data.hits && data.hits.hits) {
        let dataReceived: number = data.hits.hits.length;
        if (dataReceived > 0) {
          let lastReceivedPos: number = dataReceived - 1;
          let sortIdList: any[] = data.hits.hits[lastReceivedPos].sort;
          theQuery['search_after'] = sortIdList;

          this.searchAllByTerm(index, terms, theQuery, filterPath).subscribe(
            (result) => {
              _logs.next(data.hits.hits.concat(result));
            },
            (error) => console.error(error),
          );
        } else {
          _logs.next([]);
        }
      } else {
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
  searchAllMessagesByTerm(index: string, terms: any[], theQuery?: any): Observable<string[]> {
    let _logs: Subject<string[]> = new Subject<string[]>();
    let logs: Observable<string[]> = _logs.asObservable();
    this.searchAllByTerm(index, terms, theQuery).subscribe((data) => {
      let messages: string[] = this.returnMessages(data);
      _logs.next(messages);
    });
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

  inverseSearch(index: string, terms: any[], data: any, _logs: Subject<string[]>): void {
    let dataReceived: number = data.hits.hits.length;
    if (dataReceived > 0) {
      let sortIdList: any[] = data.hits.hits[0].sort;

      let newQuery: any = {
        search_after: sortIdList,
        sort: [{ '@timestamp': 'desc' }, { _uid: 'desc' }],
        query: {
          bool: {
            must: terms,
          },
        },
      };

      this.searchAllByTerm(index, terms, newQuery).subscribe((messages) => {
        let orderedMessages: string[] = messages.reverse();
        _logs.next(orderedMessages);
      });
    } else {
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
  getPrevFromGivenMessage(index: string, fromMessage: string, terms: any[]): Observable<string[]> {
    let url: string = this.esUrl + index;

    let _logs: Subject<string[]> = new Subject<string[]>();
    let logs: Observable<string[]> = _logs.asObservable();

    this.searchMessage(url, fromMessage, terms) //To Do: recursive search (if message is not in first 10000)
      .subscribe((data) => {
        this.inverseSearch(index, terms, data, _logs);
      });

    return logs;
  }

  getLast(index: string, terms: any[], size: number = 1000, theQuery?: any): Observable<any[]> {
    if (theQuery === undefined || theQuery === null) {
      theQuery = {
        sort: [{ '@timestamp': 'desc' }, { _uid: 'desc' }],
        query: {
          bool: {
            must: terms,
          },
        },
      };
    }
    theQuery['size'] = size;

    let url: string = this.esUrl + index;
    let searchUrl: string = url + '/_search';

    searchUrl = this.addFilterToSearchUrl(searchUrl);
    return this.internalSearch(searchUrl, theQuery).map(
      (data) => {
        let dataArray: any[] = data.hits.hits;
        return dataArray.reverse();
      },
      (error) => console.log(error),
    );
  }

  searchMessage(url: string, fromMessage: string, terms: any[]): Observable<any> {
    let must: any[] = [
      {
        match_phrase: { message: fromMessage },
      },
    ];

    must = must.concat(terms);

    let searchUrl: string = url + '/_search';
    let theQuery: any = this.getDefaultQuery(must);
    theQuery['size'] = 10000;
    return this.internalSearch(searchUrl, theQuery);
  }

  /**
   * Search and return all previous messages from given message and by terms
   * @param index
   * @param terms
   * @param theQuery optional
   */
  getPrevMessagesFromGivenMessage(index: string, fromMessage: string, terms: any[]): Observable<string[]> {
    let _logs: Subject<string[]> = new Subject<string[]>();
    let logs: Observable<string[]> = _logs.asObservable();
    this.getPrevFromGivenMessage(index, fromMessage, terms).subscribe((data) => {
      let messages: string[] = this.returnMessages(data);
      _logs.next(messages);
    });
    return logs;
  }

  // Prev by ID

  /**
   * Search and return all previous hits from given id and by terms
   * @param index
   * @param terms
   * @param theQuery optional
   */
  getPrevFromId(index: string, id: string, terms: any[]): Observable<string[]> {
    let url: string = this.esUrl + index;

    let _logs: Subject<string[]> = new Subject<string[]>();
    let logs: Observable<string[]> = _logs.asObservable();

    this.searchById(url, id, terms) // ToDo: recursive search (if id is not in first 10000)
      .subscribe((data) => {
        this.inverseSearch(index, terms, data, _logs);
      });

    return logs;
  }

  searchById(url: string, id: string, terms: any[]) {
    let must: any[] = [{ term: { _id: id } }];

    must = must.concat(terms);

    let searchUrl: string = url + '/_search';
    let theQuery: any = this.getDefaultQuery(must);

    theQuery['size'] = 10000;
    return this.internalSearch(searchUrl, theQuery);
  }

  /**
   * Search and return all previous messages from given id and by terms
   * @param index
   * @param terms
   * @param theQuery optional
   */
  getPrevMessagesFromGivenId(index: string, id: string, terms: any[]): Observable<string[]> {
    let _logs: Subject<string[]> = new Subject<string[]>();
    let logs: Observable<string[]> = _logs.asObservable();
    this.getPrevFromId(index, id, terms).subscribe((data) => {
      let messages: string[] = this.returnMessages(data);
      _logs.next(messages);
    });
    return logs;
  }

  // Prev by timestamp

  /**
   * Search and return all previous hits from given timestamp and by terms
   * @param index
   * @param terms
   * @param theQuery optional
   */
  getPrevFromTimestamp(index: string, timestamp: string, terms: any[]): Observable<string[]> {
    let url: string = this.esUrl + index;

    let _logs: Subject<string[]> = new Subject<string[]>();
    let logs: Observable<string[]> = _logs.asObservable();

    this.searchByTimestamp(url, timestamp, terms) //To Do: recursive search (if timestamp is not in first 10000)
      .subscribe((data) => {
        this.inverseSearch(index, terms, data, _logs);
      });

    return logs;
  }

  searchByTimestamp(url: string, timestamp: string, terms: any[]): Observable<any> {
    let must: any[] = [{ term: { '@timestamp': timestamp } }];

    must = must.concat(terms);

    let searchUrl: string = url + '/_search';
    let theQuery: any = this.getDefaultQuery(must);

    theQuery['size'] = 10000;
    return this.internalSearch(searchUrl, theQuery);
  }

  /**
   * Search and return all previous messages from given timestamp and by terms
   * @param index
   * @param terms
   * @param theQuery optional
   */
  getPrevMessagesFromTimestamp(index: string, timestamp: string, terms: any[]): Observable<string[]> {
    let _logs: Subject<string[]> = new Subject<string[]>();
    let logs: Observable<string[]> = _logs.asObservable();
    this.getPrevFromTimestamp(index, timestamp, terms).subscribe((data) => {
      let messages: string[] = this.returnMessages(data);
      _logs.next(messages);
    });
    return logs;
  }

  getDefaultQuery(must: any): object {
    let theQuery: object = {
      sort: [{ '@timestamp': 'asc' }, { _uid: 'asc' }],
      query: {
        bool: {
          must: must,
        },
      },
    };

    return theQuery;
  }

  addFilterToSearchUrl(searchUrl: string, filterPath?: string[]): string {
    searchUrl += '?ignore_unavailable'; // For multiple index (ignore if not exist)
    if (filterPath && filterPath.length > 0) {
      let filterPathPrefix: string = 'filter_path=';
      let filterPathSource: string = 'hits.hits._source.';
      let filterPathSort: string = 'hits.hits.sort,';

      searchUrl += '&' + filterPathPrefix + filterPathSort;
      let counter: number = 0;
      for (let filter of filterPath) {
        searchUrl += filterPathSource + filter;
        if (counter < filterPath.length - 1) {
          searchUrl += ',';
        }
        counter++;
      }
    }
    return searchUrl;
  }
}
