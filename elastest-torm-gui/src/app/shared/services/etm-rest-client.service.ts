import { Injectable } from '@angular/core';
import { ConfigurationService } from '../../config/configuration-service.service';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class EtmRestClientService {
  isExternal: boolean = false;
  constructor(private http: HttpClient, private configurationService: ConfigurationService) {}

  doGet(urlToGet: string): Observable<any> {
    let jsonStr: string = JSON.stringify({});
    return this.call(urlToGet, jsonStr, 'get');
  }

  doDelete(urlToDelete: string): Observable<any> {
    let jsonStr: string = JSON.stringify({});
    return this.call(urlToDelete, jsonStr, 'delete');
  }

  doPost(urlToPost: string, body: any): Observable<any> {
    let jsonStr: string = JSON.stringify(body);
    return this.call(urlToPost, jsonStr, 'post');
  }

  doPut(urlToPut: string, body: any): Observable<any> {
    let jsonStr: string = JSON.stringify(body);
    return this.call(urlToPut, jsonStr, 'put');
  }

  private call(urlToCall: string, jsonStr: string, method: 'get' | 'post' | 'delete' | 'put'): Observable<any> {
    let url: string = this.configurationService.configModel.hostApi + '/rest/' + method + '/' + urlToCall;
    return this.http.post(url, jsonStr);
  }
}
