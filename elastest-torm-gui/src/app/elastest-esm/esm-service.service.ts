import { ConfigurationService } from '../config/configuration-service.service';
import { Injectable } from '@angular/core';
import { Http } from '@angular/http';

@Injectable()
export class EsmService {

  constructor(private http: Http, private configurationService: ConfigurationService) { }

  getElastestESMServices(){
    let url = this.configurationService.configModel.hostApi + '/esm/services';
    return this.http.get(url)
      .map((response) =>  response.json()
    );
  }

}
