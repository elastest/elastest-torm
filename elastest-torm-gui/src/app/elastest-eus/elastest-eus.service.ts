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
  // private sessionPath: string = 'wd/hub/session';

  private sessionPath: string = 'session';

  constructor(private http: Http, private configurationService: ConfigurationService) {}

  public startSession(browser: string, version: string): Observable<any> {
    let url: string = this.eusUrl + this.sessionPath;
    let versionValue: string = version;
    if (!versionValue) {
      versionValue = '';
    }
    let browserName: string = browser;
    if (browser === 'opera') {
      browserName = 'operablink';
    }
    let capabilities = { browserName: browserName, version: versionValue, platform: 'ANY', live: true };
    let data: any = { desiredCapabilities: capabilities, capabilities: capabilities };
    return this.http.post(url, data).map((response: Response) => {
      if (response.json().value && response.json().value.sessionId) {
        return response.json().value.sessionId;
      }
      return response.json().sessionId;
    });
  }

  public getVncUrl(sessionId: string): Observable<string> {
    let url: string = this.eusUrl + this.sessionPath + '/' + sessionId + '/vnc';
    return this.http.get(url).map((response: Response) => {
      return response.text();
    });
  }

  public getVncUrlSplitted(sessionId: string): Observable<CompleteUrlObj> {
    let _obs: Subject<any> = new Subject<any>();
    let obs: Observable<any> = _obs.asObservable();
    this.getVncUrl(sessionId).subscribe(
      (url: string) => {
        let spplitedUrl: CompleteUrlObj = getUrlObj(url);
        _obs.next(spplitedUrl);
      },
      (error) => _obs.error(error),
    );

    return obs;
  }

  public getRecording(sessionId: string, hubContainerName: string): Observable<Response> {
    let url: string = this.eusUrl + this.sessionPath + '/' + sessionId + '/recording/' + hubContainerName;
    return this.http.get(url);
  }

  public getSessionRecordings(sessionId: string, hubContainerName: string): Observable<Response> {
    let url: string = this.eusUrl + this.sessionPath + '/' + sessionId + '/recording/' + hubContainerName;
    return this.http.get(url);
  }

  public deleteRecording(sessionId: string, hubContainerName: string): Observable<Response> {
    let url: string = this.eusUrl + this.sessionPath + '/' + sessionId + '/recording/' + hubContainerName;
    return this.http.delete(url);
  }

  public stopSession(sessionId: string): Observable<Response> {
    let url: string = this.eusUrl + this.sessionPath + '/' + sessionId;
    return this.http.delete(url);
  }

  public getStatus(): Observable<Response> {
    let url: string = this.eusUrl + 'status';
    console.log('GET ' + url + ' (to find out browser list in EUS)');
    return this.http.get(url);
  }

  public setEusUrl(eusUrl: string): void {
    this.eusUrl = eusUrl;
  }

  public setEusHost(eusHost: string): void {
    this.hostName = eusHost;
  }
}
