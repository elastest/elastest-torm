import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse, HttpHeaders } from '@angular/common/http';

import { ConfigurationService } from '../config/configuration-service.service';
import { getUrlObj, CompleteUrlObj, isString } from '../shared/utils';
import { Observable } from 'rxjs/Observable';
import { Subject } from 'rxjs/Subject';
import { EusTestModel } from './elastest-eus-test-model';
import { EusBowserSyncModel } from './elastest-eus-browser-sync.model';

@Injectable()
export class EusService {
  /* default something like http://172.18.0.1:8091/eus/v1/,
   but can be set with 'http://172.18.0.1:8091/eus/v1/execution/{key}/' like in test-plan-execution of testlink */
  private eusUrl: string = this.configurationService.configModel.eusServiceUrl;
  private hostName: string = this.configurationService.configModel.hostName;
  // private sessionPath: string = 'wd/hub/session';

  // Session path default 'session'
  private sessionPath: string = 'session';

  constructor(private http: HttpClient, private configurationService: ConfigurationService) {}

  public startSession(
    browser: string,
    version: string,
    extraCapabilities?: any,
    live: boolean = true,
    extraHosts: string[] = [],
  ): Observable<EusTestModel> {
    let url: string = this.eusUrl + this.sessionPath;

    let data: any = this.getSessionBody(browser, version, extraCapabilities, live, extraHosts);
    return this.http.post(url, data).map((json: any) => {
      let testModel: EusTestModel = new EusTestModel(json);
      return testModel;
    });
  }

  public getVncUrl(sessionId: string): Observable<string> {
    let url: string = this.eusUrl + this.sessionPath + '/' + sessionId + '/vnc';
    return this.http.get(url, { responseType: 'text' });
  }

  public getVncUrlSplitted(sessionId: string): Observable<CompleteUrlObj> {
    let _obs: Subject<any> = new Subject<any>();
    let obs: Observable<any> = _obs.asObservable();
    this.getVncUrl(sessionId).subscribe(
      (url: string) => {
        let spplitedUrl: CompleteUrlObj = getUrlObj(url);
        _obs.next(spplitedUrl);
      },
      (error: Error) => _obs.error(error),
    );

    return obs;
  }

  public startRecording(sessionId: string, hubContainerName: string, videoName: string = sessionId): Observable<any> {
    let url: string =
      this.eusUrl + this.sessionPath + '/' + encodeURIComponent(sessionId) + '/recording/' + hubContainerName + '/start';

    return this.http.post(url, videoName, { observe: 'response' }).map((response: HttpResponse<any>) => {
      if (response) {
        return response;
      } else {
        return undefined;
      }
    });
  }

  public stopRecording(sessionId: string, hubContainerName: string): Observable<any> {
    let url: string =
      this.eusUrl + this.sessionPath + '/' + encodeURIComponent(sessionId) + '/recording/' + hubContainerName + '/stop';
    return this.http.delete(url);
  }

  public getRecording(sessionIdOrFileName: string): Observable<string> {
    let url: string = this.eusUrl + this.sessionPath + '/' + encodeURIComponent(sessionIdOrFileName) + '/recording';
    return this.http.get(url, { responseType: 'text' });
  }

  public deleteRecording(sessionIdOrFileName: string): Observable<any> {
    let url: string = this.eusUrl + this.sessionPath + '/' + encodeURIComponent(sessionIdOrFileName) + '/recording';
    return this.http.delete(url);
  }

  public stopSession(sessionId: string): Observable<any> {
    let url: string = this.eusUrl + this.sessionPath + '/' + encodeURIComponent(sessionId);
    return this.http.delete(url);
  }

  public getStatus(): Observable<any> {
    let url: string = this.eusUrl + 'status';
    console.log('GET ' + url + ' (to find out browser list in EUS)');
    return this.http.get(url);
  }

  public getBrowsers(): Observable<any> {
    let url: string = this.eusUrl + 'browsers';
    return this.http.get(url);
  }

  public getCachedBrowsers(): Observable<any> {
    let url: string = this.eusUrl + 'browsers/cached';
    return this.http.get(url);
  }

  public navigateToUrl(sessionId: string, urlToOpen: string): Observable<any> {
    let url: string = this.eusUrl + this.sessionPath + '/' + sessionId + '/url';
    return this.http.post(url, { url: urlToOpen }, { observe: 'response' }).map((response: HttpResponse<any>) => {
      if (response) {
        return response;
      } else {
        return undefined;
      }
    });
  }

  public getFileFromSession(sessionId: string, path: string, isDirectory: boolean = false): Observable<object> {
    let url: string =
      this.eusUrl + 'browserfile/' + this.sessionPath + '/' + sessionId + '/' + path + '?isDirectory=' + isDirectory;
    return this.http.get(url, { responseType: 'blob' as 'json' });
  }

  public uploadFileToSession(sessionId: string, file: File): Observable<string> {
    let url: string = this.eusUrl + 'browserfile/' + this.sessionPath + '/' + sessionId;

    let formData: FormData = new FormData();
    formData.append('file', file);

    // Returns text/plain
    return this.http.post(url, formData, { responseType: 'text' });
  }

  public startCrossbrowserSession(
    browsers: BrowserVersionModel[],
    sutUrl: string,
    extraCapabilities?: any,
    live: boolean = true,
    extraHosts: string[] = [],
  ): Observable<EusBowserSyncModel> {
    let url: string = this.eusUrl + 'crossbrowser';

    let data: any = this.getCrossbrowserSessionBody(browsers, sutUrl, extraCapabilities, live, extraHosts);
    return this.http.post(url, data).map((json: any) => {
      let model: EusBowserSyncModel = new EusBowserSyncModel(json);
      return model;
    });
  }

  public stopCrossbrowserSession(id: string): Observable<any> {
    let url: string = this.eusUrl + 'crossbrowser/' + id;
    return this.http.delete(url);
  }

  public getCrossbrowserSession(id: string): Observable<any> {
    let url: string = this.eusUrl + 'crossbrowser/' + id;
    return this.http.get(url).map((json: any) => {
      let model: EusBowserSyncModel = new EusBowserSyncModel(json);
      return model;
    });
  }

  /* ********************************* */
  /* ********* Other methods ********* */
  /* ********************************* */

  public getSessionBody(
    browser: string,
    version: string,
    extraCapabilities?: any,
    live: boolean = true,
    extraHosts: string[] = [],
  ): any {
    let versionValue: string = version;
    if (!versionValue) {
      versionValue = '';
    }
    let browserName: string = browser;
    if (browser === 'opera') {
      browserName = 'operablink';
    }
    if (typeof extraHosts === 'string') {
      extraHosts = [extraHosts];
    }
    let capabilities: any = {
      browserName: browserName,
      version: versionValue,
      platform: 'ANY',
      live: live,
      extraHosts: extraHosts,
    };
    if (extraCapabilities) {
      capabilities = { ...capabilities, ...extraCapabilities };
    }
    return { desiredCapabilities: capabilities, capabilities: capabilities };
  }

  public getCrossbrowserSessionBody(
    browsers: BrowserVersionModel[],
    sutUrl: string,
    extraCapabilities?: any,
    live: boolean = true,
    extraHosts: string[] = [],
  ): any {
    let data: any = this.getSessionBody(undefined, undefined, extraCapabilities, live, extraHosts);
    data['sutUrl'] = sutUrl;
    let sessionsCapabilities: any[] = [];

    for (let browser of browsers) {
      let currentData: any = this.getSessionBody(browser.browser, browser.version, extraCapabilities, live, extraHosts);
      sessionsCapabilities.push(currentData);
    }
    data['sessionsCapabilities'] = sessionsCapabilities;

    return data;
  }

  public downloadFileFromSession(sessionId: string, path: string, isDirectory: boolean = false): Observable<boolean> {
    let _obs: Subject<boolean> = new Subject<boolean>();
    let obs: Observable<boolean> = _obs.asObservable();
    this.getFileFromSession(sessionId, path, isDirectory).subscribe(
      (response: any) => {
        let dataType: any = response.type;
        let binaryData: any[] = [];
        binaryData.push(response);
        let downloadLink: any = document.createElement('a');
        downloadLink.href = window.URL.createObjectURL(new Blob(binaryData, { type: dataType }));
        let splittedPath: string[] = path.split('/');
        let filename: string = splittedPath[splittedPath.length - 1];
        if (filename) {
          downloadLink.setAttribute('download', filename);
        }
        document.body.appendChild(downloadLink);
        downloadLink.click();
        _obs.next(true);
      },
      (error: Error) => {
        console.log(error);
        _obs.next(false);
      },
    );
    return obs;
  }

  public uploadFilesToSession(sessionId: string, files: FileList): Observable<object> {
    let _obs: Subject<object> = new Subject<object>();
    let obs: Observable<object> = _obs.asObservable();

    let filesArray: File[] = Array.from(files);
    let responsesArray: any[] = [];
    let errorsArray: any[] = [];

    filesArray.forEach((file: File) => {
      this.uploadFileToSession(sessionId, file).subscribe(
        (response: any) => {
          responsesArray.push(response);
          if (responsesArray.concat(errorsArray).length === filesArray.length) {
            let responseObj: object = {
              responses: responsesArray,
              errors: errorsArray,
            };
            _obs.next(responseObj);
          }
        },
        (error: Error) => {
          errorsArray.push(error);
          if (responsesArray.concat(errorsArray).length === filesArray.length) {
            let responseObj: object = {
              responses: responsesArray,
              errors: errorsArray,
            };
            _obs.next(responseObj);
          }
        },
      );
    });
    return obs;
  }

  public setEusUrl(eusUrl: string): void {
    this.eusUrl = eusUrl;
  }

  public getEusUrl(): string {
    return this.eusUrl;
  }

  public setEusHost(eusHost: string): void {
    this.hostName = eusHost;
  }

  public getEusWsByHostAndPort(host: string, port: string | number): string {
    return 'ws://' + host + ':' + port + '/eus/v1/eus-ws';
  }
}

export class BrowserVersionModel {
  browser: string;
  version: string;

  constructor(jsonOrPairStr: string | { browser: string; version: string }) {
    if (jsonOrPairStr) {
      if (typeof jsonOrPairStr === 'string' || jsonOrPairStr instanceof String) {
        // Pair browser_version
        let splittedPair: string[] = jsonOrPairStr.split('_');
        this.browser = splittedPair[0];
        this.version = splittedPair[1];
      } else {
        // obj
        this.browser = jsonOrPairStr.browser;
        this.version = jsonOrPairStr.version;
      }
    }
  }
}
