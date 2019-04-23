import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse, HttpHeaders } from '@angular/common/http';

import { ConfigurationService } from '../config/configuration-service.service';
import { getUrlObj, CompleteUrlObj } from '../shared/utils';
import { Observable } from 'rxjs/Observable';
import { Subject } from 'rxjs/Subject';
import { EusTestModel } from './elastest-eus-test-model';

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
    let data: any = { desiredCapabilities: capabilities, capabilities: capabilities };
    return this.http.post(url, data).map((json: any) => {
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

  public getRecording(sessionId: string): Observable<string> {
    let url: string = this.eusUrl + this.sessionPath + '/' + encodeURIComponent(sessionId) + '/recording';
    return this.http.get(url, { responseType: 'text' });
  }

  public deleteRecording(sessionId: string): Observable<any> {
    let url: string = this.eusUrl + this.sessionPath + '/' + encodeURIComponent(sessionId) + '/recording';
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

  public setEusHost(eusHost: string): void {
    this.hostName = eusHost;
  }

  public getEusWsByHostAndPort(host: string, port: string | number): string {
    return 'ws://' + host + ':' + port + '/eus/v1/eus-ws';
  }
}
