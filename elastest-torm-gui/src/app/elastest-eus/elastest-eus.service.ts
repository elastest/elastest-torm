import { Injectable } from '@angular/core';
import { Http, Response } from '@angular/http';
import { ConfigurationService } from '../config/configuration-service.service';
import { getUrlObj, CompleteUrlObj } from '../shared/utils';
import { Observable } from 'rxjs/Observable';
import { Subject } from 'rxjs/Subject';

@Injectable()
export class EusService {

  private eusUrl: string = this.configurationService.configModel.eusServiceUrl;
  private hostName: string = this.configurationService.configModel.hostName;

  constructor(private http: Http, private configurationService: ConfigurationService) { }

  public startSession(browser: string, version: string): Observable<any> {
    let url: string = this.eusUrl + 'session';
    let versionValue: string = version;
    if (!versionValue) {
      versionValue = '';
    }

    let data: any = { 'desiredCapabilities': { 'browserName': browser, 'version': versionValue, 'platform': 'ANY', 'live': true } };
    return this.http.post(url, data)
      .map((response: Response) => response.json().sessionId);
  }

  public getVncUrl(sessionId: string): Observable<string> {
    let url: string = this.eusUrl + 'session/' + sessionId + '/vnc';
    return this.http.get(url)
      .map(
      (response: Response) => {
        return response.text();
      });
  }

  public getVncUrlSplitted(sessionId: string): Observable<CompleteUrlObj> {
    let _obs: Subject<any> = new Subject<any>();
    let obs: Observable<any> = _obs.asObservable();
    this.getVncUrl(sessionId)
      .subscribe(
      (url: string) => {
        let spplitedUrl: CompleteUrlObj = getUrlObj(url);
        _obs.next(spplitedUrl);
      },
      (error) => _obs.error(error),
    );

    return obs;
  }

  public getRecording(sessionId: string): Observable<Response> {
    let url: string = this.eusUrl + 'session/' + sessionId + '/recording';
    return this.http.get(url);
  }

  public deleteRecording(sessionId: string): Observable<Response> {
    let url: string = this.eusUrl + 'session/' + sessionId + '/recording';
    return this.http.delete(url);
  }

  public stopSession(sessionId: string): Observable<Response> {
    let url: string = this.eusUrl + 'session/' + sessionId;
    return this.http.delete(url);
  }

  public setEusUrl(eusUrl: string): void {
    this.eusUrl = eusUrl;
  }

  public setEusHost(eusHost: string): void {
    this.hostName = eusHost;
  }


}
