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
      let responseUrl = response.text();

      if (this.hostName !== 'localhost' && this.hostName !== '127.0.0.1') {
        const token1 = "//";
        const token2 = ":";
        const token3 = "host=";
        const token4 = "&port";
        let index1 = responseUrl.indexOf(token1) + token1.length;
        let index2 = responseUrl.indexOf(token2, responseUrl.indexOf(token2) + 1);
        let index3 = responseUrl.indexOf(token3) + token3.length;
        let index4 = responseUrl.indexOf(token4);

        let changedUrl = responseUrl.substring(0, index1) +
          this.hostName + responseUrl.substring(index2, index3) +
          this.hostName + responseUrl.substring(index4);

        responseUrl = changedUrl;
      }
      return responseUrl;
    });
  }

  public deleteVnc(sessionId: string) {
    let url = this.eusUrl + "session/" + sessionId + "/vnc";
    return this.http.delete(url);
  }

  public stopSession(sessionId: string) {
    let url = this.eusUrl + "session/" + sessionId;
    return this.http.delete(url);
  }


}
