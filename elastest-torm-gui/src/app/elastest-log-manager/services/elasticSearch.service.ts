/*
 * (C) Copyright 2016 Kurento (http://kurento.org/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import {
  Http,
  Response,
  RequestOptions,
  RequestMethod,
  Request
} from '@angular/http';
import {Injectable} from '@angular/core';
import 'rxjs/Rx';

@Injectable()
export class ElasticSearchService {

  public rowData:any[] = [];
  _scroll_id:string;
  noMore:boolean = false;

  constructor(public http:Http) {
    console.log('Task Service created.');
    this.rowData = [];
  }

  internalSearch(url:string, query:any, maxResults:number, append:boolean = false) {
    console.log('URL:', url, 'Query:', query);

    let requestOptions = new RequestOptions({
      method: RequestMethod.Post,
      url,
      body: JSON.stringify(query)
    });

    return this.http.request(new Request(requestOptions))
      .map((res:Response) => {
        return res.json();
      }, (err:Response) => {
        console.error('Error:', err);
      });
  }

  getIndices(url:string) {

    let requestOptions = new RequestOptions({
      method: RequestMethod.Get,
      url
    });

    return this.http.request(new Request(requestOptions))
      .map((res:Response) => {
        return res.json();
      }, (err:Response) => {
        console.error('Error:', err);
      });
  }
}
/**
 * Created by rbenitez on 19/4/16.
 */
