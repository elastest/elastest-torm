import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { ConfigModel } from './config-model';
import { ETM_API } from '../../config/api.config';

@Injectable()
export class ConfigurationService {

  public configModel: ConfigModel;

  constructor() {
  }

  load() {
    let host: string = window.location.host;
    this.configModel = {
      'hostName': window.location.hostname,
      'host': "http://" + host,
      'hostApi': "http://" + host + "/api",
      'hostElasticsearch': "http://" + environment.hostElasticSearch + "/",
      'hostWsServer': "ws://" + host,
      'eusServiceUrlNoPath': "http://" + environment.eus,
      'eusServiceUrl': "http://" + environment.eus + "/eus/v1/",
      'eusWebSocketUrl': "ws://" + environment.eus + "/eus/v1/eus-ws"
    };
  }

}
