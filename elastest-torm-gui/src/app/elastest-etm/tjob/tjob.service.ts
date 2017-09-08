import { EsmServiceModel } from '../../elastest-esm/esm-service.model';
import { DashboardConfigModel } from './dashboard-config-model';
import { ParameterModel } from '../parameter/parameter-model';
import { ConfigurationService } from '../../config/configuration-service.service';
import { SutModel } from '../sut/sut-model';
import { SutService } from '../sut/sut.service';
import { TJobExecModel } from '../tjob-exec/tjobExec-model';
import { TJobExecService } from '../tjob-exec/tjobExec.service';
import { TJobModel } from './tjob-model';
import { Http } from '@angular/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import 'rxjs/Rx';

@Injectable()
export class TJobService {
  constructor(private http: Http, private configurationService: ConfigurationService,
    private sutService: SutService) { }

  public getTJobs() {
    let url = this.configurationService.configModel.hostApi + '/tjob';
    return this.http.get(url)
      .map((response) => this.transformTJobDataToDataTable(response.json()));
  }

  transformTJobDataToDataTable(tjobs: any[]) {
    let tjobsDataToTable: TJobModel[] = [];
    for (let tjob of tjobs) {
      tjobsDataToTable.push(this.transformToTjobmodel(tjob));
    }
    return tjobsDataToTable;
  }

  transformToTjobmodel(tjob: any) {
    let tjobsDataToTable: TJobModel;

    tjobsDataToTable = new TJobModel();
    tjobsDataToTable.id = tjob.id;
    tjobsDataToTable.name = tjob.name;
    tjobsDataToTable.imageName = tjob.imageName;
    if (tjob.sut !== undefined && tjob.sut !== null) {
      tjobsDataToTable.sut = this.sutService.transformToSutmodel(tjob.sut);
    } else {
      tjobsDataToTable.sut = new SutModel();
    }
    tjobsDataToTable.project = tjob.project;
    tjobsDataToTable.tjobExecs = tjob.tjobExecs;
    tjobsDataToTable.parameters = tjob.parameters;
    tjobsDataToTable.commands = tjob.commands;
    tjobsDataToTable.resultsPath = tjob.resultsPath;
    tjobsDataToTable.execDashboardConfig = tjob.execDashboardConfig;
    tjobsDataToTable.execDashboardConfigModel = new DashboardConfigModel(tjob.execDashboardConfig);
    if (tjob.esmServicesString !== undefined && tjob.esmServicesString !== null){      
      for(let service of JSON.parse(tjob.esmServicesString)){
        tjobsDataToTable.esmServices.push( new EsmServiceModel(service.id, service.name,
          service.selected));
      }      
    } 

    return tjobsDataToTable;
  }

  public getTJob(id: string) {
    let url = this.configurationService.configModel.hostApi + '/tjob/' + id;
    return this.http.get(url)
      .map(
      (response) => {
        let data: any = response.json();
        if (data !== undefined && data !== null) {
          return this.transformToTjobmodel(data);
        } else {
          throw new Error('Empty response. TJob not exist or you don\'t have permissions to access it');
        }
      });
  }

  public createTJob(tjob: TJobModel) {
    if (!tjob.hasSut()) {
      tjob.sut = undefined;
    }
    tjob.generateExecDashboardConfig();
    tjob.esmServicesString = JSON.stringify(tjob.esmServices);
    console.log("Services " + JSON.stringify(tjob.esmServicesString));
    let url = this.configurationService.configModel.hostApi + '/tjob';
    return this.http.put(url, tjob)
      .map((response) => response.json());
  }

  public modifyTJob() { }

  public deleteTJob(tJob: TJobModel) {
    let url = this.configurationService.configModel.hostApi + '/tjob/' + tJob.id;
    return this.http.delete(url)
      .map((response) => response.json());
  }
}
