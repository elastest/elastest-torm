import { StompWSManager } from '../stomp-ws-manager.service';
import { SutModel } from './sut-model';
import { SutExecModel } from './sutExec-model';
import { Http } from '@angular/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import { ConfigurationService } from '../../config/configuration-service.service';
import 'rxjs/Rx';

@Injectable()
export class SutService {
  constructor(private http: Http, private stompWSManager: StompWSManager, private configurationService: ConfigurationService) { }

  public getSuts() {
    let url = this.configurationService.configModel.hostApi + '/sut';
    return this.http.get(url)
      .map((response) => this.transformSutDataToDataTable(response.json()));
  }

  transformSutDataToDataTable(suts: any[]) {
    let sutsDataToTable: SutModel[] = [];
    for (let sut of suts) {
      sutsDataToTable.push(this.transformToSutmodel(sut));
    }
    return sutsDataToTable;
  }

  transformToSutmodel(sut: any) {
    let sutsDataToTable: SutModel;

    sutsDataToTable = new SutModel();
    sutsDataToTable.id = sut.id;
    sutsDataToTable.name = sut.name;
    sutsDataToTable.specification = sut.specification;
    sutsDataToTable.description = sut.description;
    sutsDataToTable.project = sut.project;

    return sutsDataToTable;
  }

  public getSut(id: number) {
    let url = this.configurationService.configModel.hostApi + '/sut/' + id;
    return this.http.get(url)
      .map(response => this.transformToSutmodel(response.json()));
  }

  public createSut(sut: SutModel) {
    let url = this.configurationService.configModel.hostApi + '/sut';
    return this.http.post(url, sut)
      .map((response) => response.json());
  }

  public modifySut() {

  }

  public deleteSut(sut: SutModel) {
    let url = this.configurationService.configModel.hostApi + '/sut/' + sut.id;
    return this.http.delete(url)
      .map((response) => response.json());
  }

  //  SutExecution functions

  public deploySut(sutId: number) {
    let url = this.configurationService.configModel.hostApi + '/sut/' + sutId + '/exec';
    return this.http.post(url, {})
      .map((response) => response.json());
  }

  public getSutsExecutions(sut: SutModel) {
    let url = this.configurationService.configModel.hostApi + '/sut/' + sut.id + '/exec';
    return this.http.get(url)
      .map((response) => this.transformSutExecDataToDataTable(response.json()));
  }

  transformSutExecDataToDataTable(sutExecs: any[]) {
    let sutExecsDataToTable: SutExecModel[] = [];
    for (let sutExec of sutExecs) {
      sutExecsDataToTable.push(this.transformToSutExecmodel(sutExec));
    }
    return sutExecsDataToTable;
  }


  transformToSutExecmodel(sutExec: any) {
    let sutExecsDataToTable: SutExecModel;

    sutExecsDataToTable = new SutExecModel();
    sutExecsDataToTable.id = sutExec.id;
    sutExecsDataToTable.deplotStatus = sutExec.deplotStatus;
    sutExecsDataToTable.url = sutExec.url;
    sutExecsDataToTable.sut = sutExec.sut;

    return sutExecsDataToTable;
  }

  public getSutExecution(sut: SutModel, idSutExecution: number) {
    let url = this.configurationService.configModel.hostApi + '/sut/' + sut.id + '/exec/' + idSutExecution;
    return this.http.get(url)
      .map(response => this.transformToSutExecmodel(response.json()));
  }

  public deleteSutExecution(sut: SutModel, sutExecution: SutExecModel) {
    let url = this.configurationService.configModel.hostApi + '/sut/' + sut.id + '/exec/' + sutExecution.id;
    console.log("url: " + url);
    return this.http.delete(url)
      .map((response) => response.json());
  }

  private subscribeQueues(sutExec: any) {

  }


}
