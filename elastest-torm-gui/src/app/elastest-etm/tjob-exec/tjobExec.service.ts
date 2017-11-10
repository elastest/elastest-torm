import { ETModelsTransformServices } from '../../shared/services/et-models-transform.service';
import { EsmServiceModel } from '../../elastest-esm/esm-service.model';
import { DashboardConfigModel } from '../tjob/dashboard-config-model';
import { ConfigurationService } from '../../config/configuration-service.service';
import { SutExecModel } from '../sut-exec/sutExec-model';
import { SutModel } from '../sut/sut-model';
import { TJobModel } from '../tjob/tjob-model';
import { TJobExecModel } from './tjobExec-model';
import { Http } from '@angular/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import 'rxjs/Rx';

@Injectable()
export class TJobExecService {
  constructor(
    private http: Http, private configurationService: ConfigurationService,
    private eTModelsTransformServices: ETModelsTransformServices
  ) { }

  //  TJobExecution functions
  public runTJob(tJobId: number, parameters: any[]) {
    let url: string = this.configurationService.configModel.hostApi + '/tjob/' + tJobId + '/exec';
    return this.http.post(url, parameters)
      .map((response) => response.json());
  }

  public getTJobsExecutions(tJob: TJobModel) {
    let url: string = this.configurationService.configModel.hostApi + '/tjob/' + tJob.id + '/exec';
    return this.http.get(url)
      .map((response) => this.eTModelsTransformServices.jsonToTJobExecsList(response.json()));
  }

  public getTJobExecutionFiles(tJobId: number, tJobExecId: number) {
    let url: string = this.configurationService.configModel.hostApi + '/tjob/' + tJobId + '/exec/' + tJobExecId + '/files';
    return this.http.get(url)
      .map((response) => response.json());
  }

  /*public getTJobExecutionFiles(tJobExec: TJobExecModel){
    let url: string = this.configurationService.configModel.hostApi + '/tjob/' + tJobExec.tJob.id + '/exec/' + tJobExec.id + '/files';
    return this.http.get(url)
    .map((response) => console.log(response.json()));
  }*/


  public getTJobExecution(tJob: TJobModel, idTJobExecution: number) {
    return this.getTJobExecutionByTJobId(tJob.id, idTJobExecution);
  }

  public getTJobExecutionByTJobId(tJobId: number, idTJobExecution: number) {
    let url: string = this.configurationService.configModel.hostApi + '/tjob/' + tJobId + '/exec/' + idTJobExecution;
    return this.http.get(url)
      .map(
      (response) => {
        let data: any = response.json();
        if (data !== undefined && data !== null) {
          return this.eTModelsTransformServices.jsonToTJobExecModel(data);
        } else {
          throw new Error('Empty response. TJob Execution not exist or you don\'t have permissions to access it');
        }
      });
  }

  public stopTJobExecution(tJob: TJobModel, tJobExecution: TJobExecModel) {
    let url: string = this.configurationService.configModel.hostApi + '/tjob/' + tJob.id + '/exec/' + tJobExecution.id + '/stop';
    return this.http.delete(url)
      .map((response) => {
        let data: any = response.json();
        if (data !== undefined && data !== null) {
          return this.eTModelsTransformServices.jsonToTJobExecModel(data);
        } else {
          throw new Error('Empty response. TJob Execution not exist or you don\'t have permissions to access it');
        }
      });
  }

  public deleteTJobExecution(tJob: TJobModel, tJobExecution: TJobExecModel) {
    let url: string = this.configurationService.configModel.hostApi + '/tjob/' + tJob.id + '/exec/' + tJobExecution.id;
    return this.http.delete(url)
      .map((response) => response.json());
  }

  public getResultStatus(tJob: TJobModel, tJobExecution: TJobExecModel) {
    let url: string = this.configurationService.configModel.hostApi + '/tjob/' + tJob.id + '/exec/' + tJobExecution.id + '/result';
    return this.http.get(url)
      .map((response) => response.json());
  }
}
