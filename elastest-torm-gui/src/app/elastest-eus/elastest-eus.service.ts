import { Injectable } from '@angular/core';
import { Http, Response } from '@angular/http';
import { ConfigurationService } from '../config/configuration-service.service';
import { getUrlObj, CompleteUrlObj } from '../shared/utils';
import { Observable } from 'rxjs/Observable';
import { Subject } from 'rxjs/Subject';
import { EusTestModel } from './elastest-eus-test-model';

@Injectable()
export class EusService {
  private eusUrl: string = this.configurationService.configModel.eusServiceUrl;
  private hostName: string = this.configurationService.configModel.hostName;
  // private sessionPath: string = 'wd/hub/session';

  private sessionPath: string = 'session';

  constructor(private http: Http, private configurationService: ConfigurationService) {}

  public startSession(browser: string, version: string, extraCapabilities?: any, live: boolean = true): Observable<EusTestModel> {
    let url: string = this.eusUrl + this.sessionPath;
    let versionValue: string = version;
    if (!versionValue) {
      versionValue = '';
    }
    let browserName: string = browser;
    if (browser === 'opera') {
      browserName = 'operablink';
    }
    let capabilities: any = { browserName: browserName, version: versionValue, platform: 'ANY', live: live };
    if (extraCapabilities) {
      capabilities = { ...capabilities, ...extraCapabilities };
    }
    let data: any = { desiredCapabilities: capabilities, capabilities: capabilities };
    return this.http.post(url, data).map((response: Response) => {
      let json: any = response.json();

      let testModel: EusTestModel = new EusTestModel();
      testModel.id = json.sessionId;
      testModel.hubContainerName = json.hubContainerName;

      if (json.value) {
        if (json.value.sessionId) {
          testModel.id = json.value.sessionId;
        }

        testModel.browser = json.value.browserName;
        testModel.version = json.value.version;
        testModel.creationTime = json.value.creationTime;
        testModel.url = json.value.url;
        testModel.status = json.value.status;
        testModel.statusMsg = json.value.statusMsg;
      }

      return testModel;
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

  public startRecording(sessionId: string, hubContainerName: string, videoName: string = sessionId): Observable<any> {
    let url: string = this.eusUrl + this.sessionPath + '/' + sessionId + '/recording/' + hubContainerName + '/start';

    return this.http.post(url, videoName).map((response: Response) => {
      if (response) {
        return response;
      } else {
        return undefined;
      }
    });
  }

  public stopRecording(sessionId: string, hubContainerName: string): Observable<Response> {
    let url: string = this.eusUrl + this.sessionPath + '/' + sessionId + '/recording/' + hubContainerName + '/stop';

    return this.http.delete(url);
  }

  public getRecording(sessionId: string): Observable<Response> {
    let url: string = this.eusUrl + this.sessionPath + '/' + sessionId + '/recording';
    return this.http.get(url);
  }

  public deleteRecording(sessionId: string): Observable<Response> {
    let url: string = this.eusUrl + this.sessionPath + '/' + sessionId + '/recording';
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

  public navigateToUrl(sessionId: string, urlToOpen: string): Observable<any> {
    let url: string = this.eusUrl + this.sessionPath + '/' + sessionId + '/url';
    return this.http.post(url, { url: urlToOpen }).map((response: Response) => {
      if (response) {
        return response;
      } else {
        return undefined;
      }
    });
  }

  public setEusUrl(eusUrl: string): void {
    this.eusUrl = eusUrl;
  }

  public setEusHost(eusHost: string): void {
    this.hostName = eusHost;
  }

  public getEusWsByHostAndPort(host: string, port: string | number): string {
    return 'ws://' + host + ':' + port + '/eus/v1/eus-ws';
  }
}
