import { TJobExecModel } from '../elastest-etm/tjob-exec/tjobExec-model';
import { EsmServiceInstanceModel } from './esm-service-instance.model';
import { EsmServiceModel } from './esm-service.model';
import { ConfigurationService } from '../config/configuration-service.service';
import { Injectable } from '@angular/core';
import { Http, Response, URLSearchParams } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';
import { Subject } from 'rxjs/Subject';
import { PullingObjectModel } from '../shared/pulling-obj.model';

export type tssParentType = 'normal' | 'tjobexec' | 'external';

@Injectable()
export class EsmService {
  constructor(private http: Http, private configurationService: ConfigurationService) {}

  getSupportServices(): Observable<EsmServiceModel[]> {
    let url: string = this.configurationService.configModel.hostApi + '/esm/services';
    return this.http.get(url).map((response) => this.transformIntoEsmServiceModel(response));
  }

  provisionServiceInstance(serviceId: string) {
    let url: string = this.configurationService.configModel.hostApi + '/esm/services/' + serviceId + '/prov';
    return this.http.post(url, null).map((response) => response['_body']);
  }

  deprovisionServiceInstance(serviceInstanceId: string) {
    let url: string = this.configurationService.configModel.hostApi + '/esm/services/instances/' + serviceInstanceId;
    return this.http.delete(url, null).map((response) => console.log(JSON.stringify(response)));
  }

  deprovisionTJobExecServiceInstance(serviceInstanceId: string, tJobExecId: string | number) {
    let url: string =
      this.configurationService.configModel.hostApi + '/esm/services/instances/' + serviceInstanceId + '/tjobexec/' + tJobExecId;
    return this.http.delete(url, null).map((response) => console.log(JSON.stringify(response)));
  }

  deprovisionExternalTJobExecServiceInstance(serviceInstanceId: string, externalTJobExecId: string | number) {
    let url: string =
      this.configurationService.configModel.hostApi +
      '/esm/services/instances/' +
      serviceInstanceId +
      '/external/tjobexec/' +
      externalTJobExecId;
    return this.http.delete(url, null).map((response) => console.log(JSON.stringify(response)));
  }

  getSupportServicesInstances(): Observable<EsmServiceInstanceModel[]> {
    let url: string = this.configurationService.configModel.hostApi + '/esm/services/instances';
    return this.http.get(url).map((response) => this.transformIntoSupportServiceInstanceList(response));
  }

  getSupportServicesInstancesByTJobExec(tJobExec: TJobExecModel): Observable<EsmServiceInstanceModel[]> {
    let url: string = this.configurationService.configModel.hostApi + '/esm/services/instances/tJobExec/' + tJobExec.id;
    return this.http.get(url).map((response) => this.transformIntoSupportServiceInstanceList(response));
  }

  getSupportServiceInstance(id: string): Observable<EsmServiceInstanceModel> {
    let url: string = this.configurationService.configModel.hostApi + '/esm/services/instances/' + id;
    return this.http.get(url).map((response) => new EsmServiceInstanceModel(response.json()));
  }

  getSupportServiceInstanceByType(id: string, type: tssParentType = 'normal'): Observable<EsmServiceInstanceModel> {
    if (type === 'external') {
      return this.getExternalTJobExecSupportServiceInstance(id);
    } else if (type === 'tjobexec') {
      return this.getTJobExecSupportServiceInstance(id);
    } else {
      return this.getSupportServiceInstance(id);
    }
  }

  getTJobExecSupportServiceInstance(id: string): Observable<EsmServiceInstanceModel> {
    let url: string = this.configurationService.configModel.hostApi + '/esm/services/instances/' + id + '/tjobexec';
    return this.http.get(url).map((response) => new EsmServiceInstanceModel(response.json()));
  }

  getExternalTJobExecSupportServiceInstance(id: string): Observable<EsmServiceInstanceModel> {
    let url: string = this.configurationService.configModel.hostApi + '/esm/services/instances/' + id + '/external/tjobexec';
    return this.http.get(url).map((response) => new EsmServiceInstanceModel(response.json()));
  }

  transformIntoEsmServiceModel(response: Response): EsmServiceModel[] {
    let res = response.json();
    let retrivedServices: EsmServiceModel[] = [];

    for (let service of res) {
      let config: string = service.config;
      if (service.name === 'EUS') {
        config = '{ "webRtcStats": { "type": "boolean", "label": "Gather WebRTC Statistics", "default": false } }';
      }

      let esmService: EsmServiceModel = new EsmServiceModel(service.id, service.name, false, config);
      retrivedServices.push(esmService);
    }
    return retrivedServices;
  }

  transformIntoSupportServiceInstanceList(response: Response): EsmServiceInstanceModel[] {
    let res = response.json();
    let retrivedServicesInstance: EsmServiceInstanceModel[] = [];

    for (let serviceInstance of res) {
      retrivedServicesInstance.push(new EsmServiceInstanceModel(serviceInstance));
    }
    return retrivedServicesInstance;
  }

  waitForTssInstanceUp(
    esmServicesInstanceId: string,
    timer: Observable<number>,
    subscription: Subscription,
    type: tssParentType = 'normal',
  ): PullingObjectModel {
    let _obs: Subject<EsmServiceInstanceModel> = new Subject<EsmServiceInstanceModel>();
    let obs: Observable<EsmServiceInstanceModel> = _obs.asObservable();

    timer = Observable.interval(2000);
    if (subscription === null || subscription === undefined) {
      subscription = timer.subscribe(() => {
        this.getSupportServiceInstanceByType(esmServicesInstanceId, type).subscribe(
          (esmServicesInstance: EsmServiceInstanceModel) => {
            if (esmServicesInstance.serviceReady) {
              if (subscription !== undefined) {
                subscription.unsubscribe();
                subscription = undefined;
                _obs.next(esmServicesInstance);
              }
            }
          },
          (error) => console.log(error),
        );
      });
    }
    let responseObj: PullingObjectModel = new PullingObjectModel();
    responseObj.observable = obs;
    responseObj.subscription = subscription;
    return responseObj;
  }
}
