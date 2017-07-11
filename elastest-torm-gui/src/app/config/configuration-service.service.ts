import { Injectable } from '@angular/core';
import { ConfigModel } from './config-model';
import { ETM_API } from '../../config/api.config';

@Injectable()
export class ConfigurationService {

  public configModel: ConfigModel;
  
  constructor( ) {    
   }

  load(){
    this.configModel = { 
      'host': "http://" + window.location.host, 
      'hostApi': "http://"+ window.location.host + "/api",
      'hostElasticsearch': 'http://localhost:9200/'
    };
  }

}
