import { StompWSManager } from '../stomp-ws-manager.service';
import { TJobModel } from './tjob-model';
import { ETM_API } from '../../../config/api.config';
import { Http } from '@angular/http';
import { Injectable } from '@angular/core';
import 'rxjs/Rx';

@Injectable()
export class TJobService {
  constructor(private http: Http, private stompWSManager: StompWSManager) { }

  public getTJobs() {
    let url = ETM_API + '/tjob';
    return this.http.get(url)
      .map((response) => this.transformDataToDataTable(response.json()));
  }

  transformDataToDataTable(tjobs: any[]) {
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

    return tjobsDataToTable;
  }

  public getTJob() {

  }

  public createTJob(tjob: TJobModel) {
    let url = ETM_API + '/tjob';
    return this.http.post(url, tjob)
      .map((response) => response.json());
  }

  public modifyTJob() {

  }

  public deleteTJob() {

  }

  public runTJob(tJobId: number) {
    let url = ETM_API + '/tjob/' + tJobId + '/exec';
    return this.http.post(url, {})
      .map((response) => response.json());
  }

  public getTJobsExecutions() {

  }

  public getTJobExecution(idTJobExecution: number) {

  }

  public deleteTJobExecution(idTJobExecution: number) {

  }

  private subscribeQueues(tjobExec: any) {

  }


}
