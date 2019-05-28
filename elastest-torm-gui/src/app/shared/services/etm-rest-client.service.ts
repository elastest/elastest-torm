import { Injectable } from '@angular/core';
import { ConfigurationService } from '../../config/configuration-service.service';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class EtmRestClientService {
  isExternal: boolean = false;
  constructor(private http: HttpClient, private configurationService: ConfigurationService) {}

  doGet(urlToGet: string): Observable<any> {
    let url: string = this.configurationService.configModel.hostApi + '/rest/get/' + urlToGet;
    return this.http.get(url);
  }

  doDelete(urlToDelete: string): Observable<any> {
    let url: string = this.configurationService.configModel.hostApi + '/rest/delete/' + urlToDelete;
    return this.http.delete(url);
  }

  doPost(urlToPost: string, body: any): Observable<any> {
    let url: string = this.configurationService.configModel.hostApi + '/rest/post/' + urlToPost;
    let jsonStr: string = JSON.stringify(body);
    return this.http.post(url, jsonStr);
  }

  doPut(urlToPut: string, body: any): Observable<any> {
    let url: string = this.configurationService.configModel.hostApi + '/rest/get/' + urlToPut;
    let jsonStr: string = JSON.stringify(body);
    return this.http.put(url, jsonStr);
  }
}
