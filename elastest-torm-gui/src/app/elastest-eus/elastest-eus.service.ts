import {Injectable} from '@angular/core';
import {Http, Response} from '@angular/http';

@Injectable()
export class EusService {

  private eusUrl: string = 'http://localhost:8080/eus/v1/';

  constructor(private http: Http) {}

  public startSession(browser: string, version: string) {
    let url = this.eusUrl + "session";
    let versionValue = version;
    if (!versionValue) {
      versionValue = "";
    }

    let data = {'desiredCapabilities': {'browserName': browser, 'version': versionValue, 'platform': 'ANY', 'live': true}};
    return this.http.post(url, data).map(response => response.json().sessionId);
  }

  public getVncUrl(sessionId: string) {
    let url = this.eusUrl + "session/" + sessionId + "/vnc";
    return this.http.get(url).map(response => response.text());
  }

  public stopSession(sessionId: string) {
    let url = this.eusUrl + "session/" + sessionId;
    return this.http.delete(url);
  }


}
