/*
 * (C) Copyright 2016 Kurento (http://kurento.org/)
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import { Injectable } from '@angular/core';
import { Http, Request, RequestMethod, RequestOptions, Response } from '@angular/http';
import { BehaviorSubject } from 'rxjs/Rx';
import 'rxjs/Rx';

@Injectable()
export class ElasticSearchService {

  public rowData: any[] = [];
  _scroll_id: string;
  noMore: boolean = false;

  constructor(public http: Http) {
    console.log('Task Service created.');
    this.rowData = [];
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

  /**
   * returns an observable. When all data has been received (recursively), obeservable calls next method to send data
   * @param url 
   * @param type 
   * @param from 
   */
  searchLogsByType(url: string, type: string, from?: number, theQuery?: any) {
    let size: number = 1000;
    if (from === undefined || from === null) {
      from = 0;
    }

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
        from: from
      };
    }
    else {
      theQuery['size'] = size;
      theQuery['from'] = from;
    }

    let _logs = new BehaviorSubject<string[]>([]);
    let logs = _logs.asObservable();

    this.internalSearch(searchUrl, theQuery, size)
      .subscribe(
      (data) => {
        let dataReceived: number = data.hits.hits.length;
        let messages: string[] = this.returnMessages(data);

        if (data.hits.total - (from + dataReceived) > 0) {
          this.searchLogsByType(url, type, from + size).subscribe(
            (result) => {
              _logs.next(messages.concat(result));
            },
            (error) => console.log(error)
          );
        }
        else {
          _logs.next(messages);
        }
      });

    return logs;
  }

  searchTestLogs(url: string) {
    return this.searchLogsByType(url, 'testlogs');
  }

  searchSutLogs(url: string) {
    return this.searchLogsByType(url, 'sutlogs');
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

  getFromGivenLog(url: string, fromMessage: string, type: string) {
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


    let _logs = new BehaviorSubject<string[]>([]);
    let logs = _logs.asObservable();

    this.internalSearch(searchUrl, theQuery, 10000)
      .subscribe(
      (data) => {
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

        this.searchLogsByType(url, type, 0, newQuery).subscribe(
          (messages) => {
            let orderedMessages: string[] = messages.reverse();
            _logs.next(orderedMessages);
          }
        );
      });

    return logs;
  }

  getFromGivenTestLog(url: string, fromMessage: string) {
    return this.getFromGivenLog(url, fromMessage, 'testlogs');
  }

  getFromGivenSutLog(url: string, fromMessage: string) {
    return this.getFromGivenLog(url, fromMessage, 'sutlogs');
  }

  getFromSutLog(url: string) {

  }
}