import {Injectable} from '@angular/core';
import {Http, Response} from '@angular/http';
import {ConfigurationService} from '../config/configuration-service.service';

@Injectable()
export class EusService {

  private eusUrl: string = this.configurationService.configModel.eusServiceUrl;
  private hostName: string = this.configurationService.configModel.hostName;

  constructor(private http: Http, private configurationService: ConfigurationService) { }

  public startSession(browser: string, version: string) {
    let url = this.eusUrl + "session";
    let versionValue = version;
    if (!versionValue) {
      versionValue = "";
    }

    let data = { 'desiredCapabilities': { 'browserName': browser, 'version': versionValue, 'platform': 'ANY', 'live': true } };
    return this.http.post(url, data).map(response => response.json().sessionId);
  }

  public getVncUrl(sessionId: string) {
    let url = this.eusUrl + "session/" + sessionId + "/vnc";
    return this.http.get(url).map(response => {
      return response.text();
    });
  }

  public getRecording(sessionId: string) {
    let url = this.eusUrl + "session/" + sessionId + "/recording";
    return this.http.get(url);
  }

  public deleteRecording(sessionId: string) {
    let url = this.eusUrl + "session/" + sessionId + "/recording";
    return this.http.delete(url);
  }

  public stopSession(sessionId: string) {
    let url = this.eusUrl + "session/" + sessionId;
    return this.http.delete(url);
  }

  public setEusUrl(eusUrl: string){
    this.eusUrl = eusUrl;
  }

  public setEusHost(eusHost: string){
    this.hostName = eusHost;
  }


}
