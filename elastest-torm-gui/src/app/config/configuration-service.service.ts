import { Http } from '@angular/http';
import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { ConfigModel } from './config-model';
import { ETM_API } from '../../config/api.config';
import { Observable } from 'rxjs/Rx';

@Injectable()
export class ConfigurationService {

  public configModel: ConfigModel;

  constructor(private http: Http, ) {
  }

  load() {
    let host: string = window.location.host;
    let hostApi: string = 'http://' + host + '/api';
    this.configModel = {
      'hostName': window.location.hostname,
      'host': 'http://' + host,
      'hostApi': hostApi,
      'hostElasticsearch': 'http://' + environment.hostElasticSearch + '/',
      'hostEIM': 'http://' + environment.hostEIM + '/',
      'hostWsServer': 'ws://' + host + '/rabbitmq',
      'eusServiceUrlNoPath': 'http://' + environment.eus,
      'eusServiceUrl': 'http://' + environment.eus + '/eus/v1/',
      'eusWebSocketUrl': 'ws://' + environment.eus + '/eus/v1/eus-ws',
    };
    this.getElasticsearchApi(hostApi)
      .subscribe(
      (data) => {
        let slash: string = '/';
        if (data.slice(-1) === slash) {
          slash = '';
        }
        this.configModel.hostElasticsearch = data + slash;
      },
    );

   /* this.getWSHost(hostApi)
    .subscribe(
    (data) => {
      let slash: string = '/';
      if (data.slice(-1) === slash) {
        slash = '';
      }
      this.configModel.hostWsServer = 'ws://' + data + slash;
    },
  );*/

  }

  public getElasticsearchApi(hostApi: string) {
    let url: string = hostApi + '/context/elasticsearch/api';
    return this.http.get(url)
      .map((response) => response['_body']);
  }

  public getWSHost(hostApi: string) {
    let url: string = hostApi + '/context/ws-host';
    return this.http.get(url)
      .map((response) => response['_body']);
  }

  public getLogstashIp() {
    let hostApi: string = this.configModel.hostApi;
    let url: string = hostApi + '/context/logstash/ip';
    return this.http.get(url)
      .map((response) => response['_body']);
  }


  public getLogstashInfo() {
    let hostApi: string = this.configModel.hostApi;
    let url: string = hostApi + '/context/logstash/info';
    return this.http.get(url)
      .map((response) => response.json());
  }

}
