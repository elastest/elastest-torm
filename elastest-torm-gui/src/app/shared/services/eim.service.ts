import { EimConfigModel } from '../../elastest-etm/sut/eim-config-model';
import { ConfigModel } from '../../config/config-model';
import { ConfigurationService } from '../../config/configuration-service.service';

import { Injectable } from '@angular/core';
import { Http, Request, RequestMethod, RequestOptions, Response } from '@angular/http';
import { Subject } from 'rxjs/Rx';
import 'rxjs/Rx';

@Injectable()
export class EIMService {

  eimUrl: string;

  constructor(public http: Http, private configurationService: ConfigurationService) {
    this.eimUrl = this.configurationService.configModel.hostEIM;
  }

  public registerAgent(eimConfig: EimConfigModel) {
    let url: string = this.eimUrl + 'eim/api/agent';
    return this.http.post(url, eimConfig)
      .map((response) => response.json());
  }
}