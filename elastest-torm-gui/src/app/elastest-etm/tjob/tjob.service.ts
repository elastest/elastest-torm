import { ETModelsTransformServices } from '../../shared/services/et-models-transform.service';
import { EsmServiceModel } from '../../elastest-esm/esm-service.model';
import { DashboardConfigModel } from './dashboard-config-model';
import { ParameterModel } from '../parameter/parameter-model';
import { ConfigurationService } from '../../config/configuration-service.service';
import { SutModel } from '../sut/sut-model';
import { TJobExecModel } from '../tjob-exec/tjobExec-model';
import { TJobExecService } from '../tjob-exec/tjobExec.service';
import { TJobModel } from './tjob-model';
import { Http } from '@angular/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import 'rxjs/Rx';

@Injectable()
export class TJobService {
  constructor(
    private http: Http, private configurationService: ConfigurationService,
    private eTModelsTransformServices: ETModelsTransformServices,
  ) { }

  public getTJobs() {
    let url: string = this.configurationService.configModel.hostApi + '/tjob';
    return this.http.get(url)
      .map((response) => this.eTModelsTransformServices.jsonToTJobsList(response.json()));
  }

  public getTJob(id: string) {
    let url: string = this.configurationService.configModel.hostApi + '/tjob/' + id;
    return this.http.get(url)
      .map(
      (response) => {
        let data: any = response.json();
        if (data !== undefined && data !== null) {
          return this.eTModelsTransformServices.jsonToTJobModel(data);
        } else {
          throw new Error('Empty response. TJob not exist or you don\'t have permissions to access it');
        }
      });
  }

  public createTJob(tJob: TJobModel, action: string) {
    if (!tJob.hasSut()) {
      tJob.sut = undefined;
    }
    tJob.generateExecDashboardConfig();
    tJob.esmServicesString = JSON.stringify(tJob.esmServices);
    let url: string = this.configurationService.configModel.hostApi + '/tjob';
    if (action === 'new') {
      return this.http.post(url, tJob)
        .map((response) => response.json());
    } else {
      return this.http.put(url, tJob)
        .map((response) => response.json());
    }

  }

  public modifyTJob(tJob: TJobModel) {
    return this.createTJob(tJob, 'edit');
  }

  public deleteTJob(tJob: TJobModel) {
    let url: string = this.configurationService.configModel.hostApi + '/tjob/' + tJob.id;
    return this.http.delete(url)
      .map((response) => response.json());
  }

  /***** Others *****/

  public getTJobExecsList(tJob: TJobModel): TJobExecModel[] {
    return this.eTModelsTransformServices.jsonToTJobExecsList(tJob.tjobExecs);
  }

}
