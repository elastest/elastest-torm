import { Http } from '@angular/http';
import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { ConfigModel } from './config-model';
import { ETM_API } from '../../config/api.config';
import { Observable } from 'rxjs/Rx';

@Injectable()
export class ConfigurationService {
  public configModel: ConfigModel;
  protocol: string;
  host: string;
  hostApi: string;

  constructor(private http: Http) {
    console.log('Starting configuration.');
    this.protocol = window.location.protocol;
    this.host = window.location.host;
    console.log('protocol: ' + this.protocol);
    this.hostApi = (this.protocol === 'https:' ? 'https://' : 'http://') + this.host + '/api';
    console.log('hostapi: ' + this.hostApi);
  }

  load() {
    return new Promise((resolve, reject) => {
      this.getServicesInfo().subscribe((servicesInfo) => {
        let eusUrl: URL =
          servicesInfo.elasTestExecMode === 'normal' && servicesInfo.eusSSInstance !== null
            ? new URL(servicesInfo.eusSSInstance.urls.api)
            : null;
        this.configModel = {
          hostName: window.location.hostname,
          host: (this.protocol === 'https:' ? 'https://' : 'http://') + this.host,
          hostApi: this.hostApi,
          hostElasticsearch: servicesInfo.elasticSearchUrl + '/',
          hostEIM: (this.protocol === 'https:' ? 'https://' : 'http://') + environment.hostEIM + '/',
          hostWsServer: (this.protocol === 'https:' ? 'wss://' : 'ws://') + this.host + servicesInfo.rabbitPath,
          eusHost: eusUrl !== null ? eusUrl.hostname : null,
          eusPort: eusUrl !== null ? eusUrl.port : null,
          eusServiceUrlNoPath: (this.protocol === 'https:' ? 'https://' : 'http://') + environment.eus,
          eusServiceUrl:
            servicesInfo.elasTestExecMode === 'normal' && servicesInfo.eusSSInstance !== null
              ? this.protocol === 'https:'
                ? String(servicesInfo.eusSSInstance.urls.api).replace('http://', 'https://')
                : servicesInfo.eusSSInstance.urls.api
              : null,
          eusWebSocketUrl:
            servicesInfo.elasTestExecMode === 'normal' && servicesInfo.eusSSInstance !== null
              ? this.protocol === 'https:'
                ? String(servicesInfo.eusSSInstance.urls.eusWSapi).replace('ws://', 'wss://')
                : servicesInfo.eusSSInstance.urls.eusWSapi
              : null,
          elasTestExecMode: servicesInfo.elasTestExecMode,
          testLinkStarted: servicesInfo.testLinkStarted,
          httpsSecure: this.protocol === 'https' ? true : false,
          empGrafanaUrl: servicesInfo.empGrafanaUrl,
          edmCommandUrl: servicesInfo.edmCommandUrl,
        };

        resolve();
        console.log('The configuration is completed.');
      });
    });
  }

  public getServicesInfo(): Observable<any> {
    let url: string = this.hostApi + '/context/services/info';
    return this.http.get(url).map((response) => response.json());
  }

  public getElasticsearchApi(): Observable<any> {
    let url: string = this.hostApi + '/context/elasticsearch/api';
    return this.http.get(url).map((response) => response['_body']);
  }

  public getWSHost(): Observable<any> {
    let url: string = this.hostApi + '/context/ws-host';
    return this.http.get(url).map((response) => response['_body']);
  }

  public getLogstashIp(): Observable<any> {
    let hostApi: string = this.configModel.hostApi;
    let url: string = hostApi + '/context/logstash/ip';
    return this.http.get(url).map((response) => response['_body']);
  }

  public getLogstashInfo(): Observable<any> {
    let hostApi: string = this.configModel.hostApi;
    let url: string = hostApi + '/context/logstash/info';
    return this.http.get(url).map((response) => response.json());
  }

  public getHelpInfo(): Observable<any> {
    let url: string = this.configModel.hostApi + '/context/help/info';
    return this.http.get(url).map((response) => response.json());
  }
}
