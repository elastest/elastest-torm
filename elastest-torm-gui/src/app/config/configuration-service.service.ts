import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { ConfigModel } from './config-model';
import { ETM_API } from '../../config/api.config';

@Injectable()
export class ConfigurationService {

  public configModel: ConfigModel;
  
  constructor( ) {    
   }

  load(){
    let host: string = window.location.host;
    this.configModel = { 
      'host': "http://" + host, 
      'hostApi': "http://"+ host + "/api",
      'hostElasticsearch': "http://" + environment.hostElasticSearch + "/",
      'hostWsServer': "ws://" + host
    };
  }

}
