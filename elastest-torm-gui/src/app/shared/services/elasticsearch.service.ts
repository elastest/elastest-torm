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
    console.log('Task Service created.');
    this.rowData = [];
    this.esUrl = this.configurationService.configModel.hostElasticsearch;
  }

  returnMessages(data: any) {
    let messagesList: string[] = [];
    if (data.hits) {
      for (let logEntry of data.hits.hits) {
        if (logEntry._source['message'] !== undefined) {
          messagesList.push(logEntry._source['message']);
        }
      }
    }
    return messagesList;
  }

  internalSearch(url: string, query: any, maxResults: number, append: boolean = false) {
    console.log('URL:', url, 'Query:', query);

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

  getFromGivenMessage(index: string, fromMessage: string, type: string) {
    let url = this.esUrl + index;

    let _logs = new Subject<string[]>();
    let logs = _logs.asObservable();

    this.searchMessage(url, fromMessage, type) //To Do: recursive search (if message is not in first 10000)
      .subscribe(
      (data) => {
        let dataReceived: number = data.hits.hits.length;
        if (dataReceived > 0) {
          let sortId: number = data.hits.hits[0].sort[0];

          let newQuery: any = {
            search_after: [sortId],
            sort: [
              { '@timestamp': 'desc' }
            ],
            query: {
              term: { _type: type }
            },
          }

          this.searchLogsByType(index, type, newQuery).subscribe(
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
      );

    return logs;
  }

  searchMessage(url: string, fromMessage: string, type: string) {
    let searchUrl: string = url + '/_search';
    let theQuery = {
      sort: [
        { '@timestamp': 'asc' }
      ],
      query: {
        bool: {
          must: {
            match_phrase: { message: fromMessage }
          },
          filter: {
            term: { _type: type }
          }
        }
      }
    };

    return this.internalSearch(searchUrl, theQuery, 10000);
  }


  /**
   * Search all logs recursively by type
   * @param url 
   * @param type 
   * @param theQuery optional
   */
  searchLogsByType(index: string, type: string, theQuery?: any) {
    let size: number = 1000;
    let url = this.esUrl + index;
    let searchUrl: string = url + '/_search';

    if (theQuery === undefined || theQuery === null) {
      theQuery = {
        sort: [
          { '@timestamp': 'asc' }
        ],
        query: {
          term: {
            _type: type
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

          let messages: string[] = this.returnMessages(data);
          this.searchLogsByType(index, type, theQuery).subscribe(
            (result) => {
              _logs.next(messages.concat(result));
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



}