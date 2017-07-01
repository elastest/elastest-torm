import { ConfigurationService } from '../../config/configuration-service.service';
import { StompWSManager } from '../stomp-ws-manager.service';
import { TJobExecModel } from '../tjob-exec/tjobExec-model';
import { TJobModel } from './tjob-model';
import { SutModel } from '../sut/sut-model';
import { Http } from '@angular/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import 'rxjs/Rx';

@Injectable()
export class TJobService {
  constructor(private http: Http, private stompWSManager: StompWSManager, private configurationService: ConfigurationService) { }

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
    tjobsDataToTable.sut = tjob.sut;
    if (tjobsDataToTable.sut === undefined || tjobsDataToTable.sut === null || ((tjobsDataToTable.sut !== undefined && tjobsDataToTable.sut !== null) && tjobsDataToTable.sut.id === 0)) {
      tjobsDataToTable.sut = new SutModel();
    }
    tjobsDataToTable.project = tjob.project;
    tjobsDataToTable.tjobExecs = tjob.tjobExecs;

    return tjobsDataToTable;
  }

  public getTJob(id: string) {
    let url = this.configurationService.configModel.hostApi + '/tjob/' + id;
    return this.http.get(url)
      .map(response => this.transformToTjobmodel(response.json()));
  }

  public createTJob(tjob: TJobModel) {
    if (tjob.sut !== undefined && tjob.sut.id === 0) {
      tjob.sut = undefined;
    }

    let url = this.configurationService.configModel.hostApi + '/tjob';
    return this.http.post(url, tjob)
      .map((response) => response.json());
  }

  public modifyTJob() {

  }

  public deleteTJob(tJob: TJobModel) {
    let url = this.configurationService.configModel.hostApi + '/tjob/' + tJob.id;
    return this.http.delete(url)
      .map((response) => response.json());
  }
}
