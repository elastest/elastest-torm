import { TJobExecModel } from '../elastest-etm/tjob-exec/tjobExec-model';
import { EsmServiceInstanceModel } from './esm-service-instance.model';
import { EsmServiceModel, TssManifest } from './esm-service.model';
import { ConfigurationService } from '../config/configuration-service.service';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';
import { Subject } from 'rxjs/Subject';
import { PullingObjectModel } from '../shared/pulling-obj.model';
import { HttpClient, HttpResponse } from '@angular/common/http';

export type tssParentType = 'normal' | 'tjobexec' | 'external';

@Injectable()
export class EsmService {
  constructor(private http: HttpClient, private configurationService: ConfigurationService) {}

  getSupportServices(): Observable<EsmServiceModel[]> {
    let url: string = this.configurationService.configModel.hostApi + '/esm/services';
    return this.http.get(url).map((data: any[]) => this.transformIntoEsmServiceModel(data));
  }

  // TODO api call to /prov without tJobExec (backend)
  provisionServiceInstance(serviceId: string): Observable<any> {
    let url: string = this.configurationService.configModel.hostApi + '/esm/services/' + serviceId + '/prov';
    return this.http.post(url, undefined, { responseType: 'text' });
  }

  deprovisionServiceInstance(serviceInstanceId: string): Observable<any> {
    let url: string = this.configurationService.configModel.hostApi + '/esm/services/instances/' + serviceInstanceId;
    return this.http.delete(url).map((data: any) => console.log(data));
  }

  deprovisionTJobExecServiceInstance(serviceInstanceId: string, tJobExecId: string | number): Observable<any> {
    let url: string =
      this.configurationService.configModel.hostApi + '/esm/services/instances/' + serviceInstanceId + '/tjobexec/' + tJobExecId;
    return this.http.delete(url).map((data: any) => console.log(data));
  }

  deprovisionExternalTJobExecServiceInstance(serviceInstanceId: string, externalTJobExecId: string | number): Observable<any> {
    let url: string =
      this.configurationService.configModel.hostApi +
      '/esm/services/instances/' +
      serviceInstanceId +
      '/external/tjobexec/' +
      externalTJobExecId;
    return this.http.delete(url).map((data: any) => console.log(data));
  }

  getSupportServicesInstances(): Observable<EsmServiceInstanceModel[]> {
    let url: string = this.configurationService.configModel.hostApi + '/esm/services/instances';
    return this.http.get(url).map((data: any[]) => this.transformIntoSupportServiceInstanceList(data));
  }

  getSupportServicesInstancesByTJobExec(tJobExec: TJobExecModel): Observable<EsmServiceInstanceModel[]> {
    let url: string = this.configurationService.configModel.hostApi + '/esm/services/instances/tJobExec/' + tJobExec.id;
    return this.http.get(url).map((data: any[]) => this.transformIntoSupportServiceInstanceList(data));
  }

  getSupportServiceInstance(id: string): Observable<EsmServiceInstanceModel> {
    let url: string = this.configurationService.configModel.hostApi + '/esm/services/instances/' + id;
    return this.http.get(url).map((data: any) => new EsmServiceInstanceModel(data));
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
    return this.http.get(url).map((data: any) => new EsmServiceInstanceModel(data));
  }

  getExternalTJobExecSupportServiceInstance(id: string): Observable<EsmServiceInstanceModel> {
    let url: string = this.configurationService.configModel.hostApi + '/esm/services/instances/' + id + '/external/tjobexec';
    return this.http.get(url).map((data: any) => new EsmServiceInstanceModel(data));
  }

  transformIntoEsmServiceModel(data: any): EsmServiceModel[] {
    let retrivedServices: EsmServiceModel[] = [];

    for (let service of data) {
      let tssManifest: TssManifest;
      let manifest: any = service.manifest;

      if (service.name === 'EUS') {
        if (manifest === undefined || manifest === null) {
          manifest = { config: undefined };
        }
        // TODO remove hardcoded
        manifest.config = '{ "webRtcStats": { "type": "boolean", "label": "Gather WebRTC Statistics", "default": false } }';
        service.manifest = manifest;
        if (service.manifest) {
          tssManifest = new TssManifest();
          tssManifest.initFromJson(service.manifest);
        }
      }
      let esmService: EsmServiceModel = new EsmServiceModel(service.id, service.name, false, tssManifest);
      retrivedServices.push(esmService);
    }
    return retrivedServices;
  }

  transformIntoSupportServiceInstanceList(data: any[]): EsmServiceInstanceModel[] {
    let retrivedServicesInstance: EsmServiceInstanceModel[] = [];

    for (let serviceInstance of data) {
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
