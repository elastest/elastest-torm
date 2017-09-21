import { EsmServiceInstanceModel } from './esm-service-instance.model';
import { EsmServiceModel } from './esm-service.model';
import { ConfigurationService } from '../config/configuration-service.service';
import { Injectable } from '@angular/core';
import { Http, Response, URLSearchParams } from '@angular/http';

@Injectable()
export class EsmService {

  constructor(private http: Http, private configurationService: ConfigurationService) { }

  getSupportServices(){
    let url = this.configurationService.configModel.hostApi + '/esm/services';
    return this.http.get(url)
      .map((response) =>  this.transformIntoEsmServiceModel(response)
    );
  }

  provisionServiceInstance(serviceId: string){
    let url = this.configurationService.configModel.hostApi + '/esm/services/' + serviceId + '/prov';    
    return this.http.post(url, null)
      .map((response) =>  console.log(JSON.stringify(response))
    );
  }

  deprovisionServiceInstance(serviceInstanceId: string){
    let url = this.configurationService.configModel.hostApi + '/esm/services/instances/' + serviceInstanceId;    
    return this.http.delete(url, null)
      .map((response) =>  console.log(JSON.stringify(response))
    );
  }

  getSupportServicesInstances(){
    let url = this.configurationService.configModel.hostApi + '/esm/services/instances';
    return this.http.get(url)
      .map((response) =>  this.transformIntoSupportServiceInstanceList(response)
    );
  }

  getSupportServiceInstance(id: string){
    let url = this.configurationService.configModel.hostApi + '/esm/services/instances/' + id;
    return this.http.get(url)
      .map((response) => this.transformIntoSupportServiceInstance(response.json())
    );
  }

  transformIntoEsmServiceModel(response: Response ){
    let res =  response.json();
    let retrivedServices: EsmServiceModel[] = [];

    for(let service of res){
      retrivedServices.push(new EsmServiceModel(service.id, service.name, false));
    }
    return retrivedServices;    
  }

  transformIntoSupportServiceInstanceList(response: Response ){    
    let res =  response.json();
    let retrivedServicesInstance: EsmServiceInstanceModel[] = [];

    for(let serviceInstance of res){      
        retrivedServicesInstance.push(this.transformIntoSupportServiceInstance(serviceInstance));      
    }
    return retrivedServicesInstance;    
  }
  
  transformIntoSupportServiceInstance(serviceInstance: any ){
    console.log("Service instance " + JSON.stringify(serviceInstance));
    return new  EsmServiceInstanceModel(serviceInstance);
  }
}
