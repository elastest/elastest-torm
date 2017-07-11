import { ConfigurationService } from '../../config/configuration-service.service';
import { StompWSManager } from '../stomp-ws-manager.service';
import { SutExecModel } from '../sut-exec/sutExec-model';
import { SutExecService } from '../sut-exec/sutExec.service';
import { SutModel } from '../sut/sut-model';
import { SutService } from '../sut/sut.service';
import { TJobModel } from '../tjob/tjob-model';
import { TJobExecModel } from './tjobExec-model';
import { Http } from '@angular/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import 'rxjs/Rx';

@Injectable()
export class TJobExecService {
  constructor(private http: Http, private stompWSManager: StompWSManager, private configurationService: ConfigurationService,
    private sutExecService: SutExecService, private sutService: SutService) { }

  //  TJobExecution functions
  public runTJob(tJobId: number) {
    let url = this.configurationService.configModel.hostApi + '/tjob/' + tJobId + '/exec';
    return this.http.post(url, {})
      .map((response) => response.json());
  }

  public getTJobsExecutions(tJob: TJobModel) {
    let url = this.configurationService.configModel.hostApi + '/tjob/' + tJob.id + '/exec';
    return this.http.get(url)
      .map((response) => this.transformTJobExecDataToDataTable(response.json()));
  }

  public transformTJobExecDataToDataTable(tjobExecs: any[]) {
    let tjobExecsDataToTable: TJobExecModel[] = [];
    for (let tjobExec of tjobExecs) {
      tjobExecsDataToTable.push(this.transformToTjobExecmodel(tjobExec));
    }
    return tjobExecsDataToTable;
  }


  public transformToTjobExecmodel(tjobExec: any) {
    let tjobExecsDataToTable: TJobExecModel;
    tjobExecsDataToTable = new TJobExecModel();

    tjobExecsDataToTable.id = tjobExec.id;
    tjobExecsDataToTable.duration = tjobExec.duration;
    tjobExecsDataToTable.error = tjobExec.error;
    tjobExecsDataToTable.result = tjobExec.result;
    if (tjobExec.sutExecution !== undefined && tjobExec.sutExecution !== null) {
      tjobExecsDataToTable.sutExec = this.sutExecService.transformToSutExecmodel(tjobExec.sutExecution);
    }
    else {
      tjobExecsDataToTable.sutExec = new SutExecModel();
    }
    tjobExecsDataToTable.logIndex = tjobExec.logIndex;

    if (tjobExec.tJob !== undefined && tjobExec.tJob !== null) {
      tjobExecsDataToTable.tJob = this.transformToTjobmodelForTJobExec(tjobExec.tJob);
    }
    else {
      tjobExecsDataToTable.tJob = new TJobModel();
    }

    return tjobExecsDataToTable;
  }

  public getTJobExecution(tJob: TJobModel, idTJobExecution: number) {
    return this.getTJobExecutionByTJobId(tJob.id, idTJobExecution);
  }

  public getTJobExecutionByTJobId(tJobId: number, idTJobExecution: number) {
    let url = this.configurationService.configModel.hostApi + '/tjob/' + tJobId + '/exec/' + idTJobExecution;
    return this.http.get(url)
      .map(
      (response) => {
        let data: any = response.json();
        if (data !== undefined && data !== null) {
          return this.transformToTjobExecmodel(data);
        }
        else {
          throw new Error('Empty response. TJob Execution not exist or you don\'t have permissions to access it');
        }
      });
  }

  public deleteTJobExecution(tJob: TJobModel, tJobExecution: TJobExecModel) {
    let url = this.configurationService.configModel.hostApi + '/tjob/' + tJob.id + '/exec/' + tJobExecution.id;
    return this.http.delete(url)
      .map((response) => response.json());
  }

  transformToTjobmodelForTJobExec(tjob: any) { // Not convert tjob exec list 
    let tjobsDataToTable: TJobModel;

    tjobsDataToTable = new TJobModel();
    tjobsDataToTable.id = tjob.id;
    tjobsDataToTable.name = tjob.name;
    tjobsDataToTable.imageName = tjob.imageName;
    if (tjob.sut !== undefined && tjob.sut !== null) {
      tjobsDataToTable.sut = this.sutService.transformToSutmodel(tjob.sut);
    }
    else {
      tjobsDataToTable.sut = new SutModel();
    }
    tjobsDataToTable.project = tjob.project;
    tjobsDataToTable.tjobExecs = tjob.tjobExecs;

    return tjobsDataToTable;
  }


  // Others
  public createAndSubscribe(tjobExecution: TJobExecModel) {
    let withSut: boolean = tjobExecution.tJob.hasSut();

    this.stompWSManager.subscribeToQueDestination('q-' + tjobExecution.id + '-test-log', this.stompWSManager.testLogResponse);
    this.stompWSManager.subscribeToQueDestination('q-' + tjobExecution.id + '-test-metrics', this.stompWSManager.testMetricsResponse);
    if (withSut) {
      this.stompWSManager.subscribeToQueDestination('q-' + tjobExecution.id + '-sut-log', this.stompWSManager.sutLogResponse);
      this.stompWSManager.subscribeToQueDestination('q-' + tjobExecution.id + '-sut-metrics', this.stompWSManager.sutMetricsResponse);
    } else {
      this.stompWSManager.sutTraces.push('TJob without Sut');
    }
  }

  public createAndSubscribeToTopic(tjobExecution: TJobExecModel) {
    let withSut: boolean = tjobExecution.tJob.hasSut();

    this.stompWSManager.subscribeToTopicDestination('test.' + tjobExecution.id + '.log', this.stompWSManager.testLogResponse);
    this.stompWSManager.subscribeToTopicDestination('test.' + tjobExecution.id + '.metrics', this.stompWSManager.testMetricsResponse);
    if (withSut) {
      this.stompWSManager.subscribeToTopicDestination('sut.' + tjobExecution.id + '.log', this.stompWSManager.sutLogResponse);
      this.stompWSManager.subscribeToTopicDestination('sut.' + tjobExecution.id + '.metrics', this.stompWSManager.sutMetricsResponse);
    } else {
      this.stompWSManager.sutTraces.push('TJob without Sut');
    }
  }
}
