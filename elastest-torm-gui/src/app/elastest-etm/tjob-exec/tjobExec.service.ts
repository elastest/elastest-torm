import { PopupService } from '../../shared/services/popup.service';
import { ETModelsTransformServices } from '../../shared/services/et-models-transform.service';
import { EsmServiceModel } from '../../elastest-esm/esm-service.model';
import { DashboardConfigModel } from '../tjob/dashboard-config-model';
import { ConfigurationService } from '../../config/configuration-service.service';
import { SutExecModel } from '../sut-exec/sutExec-model';
import { SutModel } from '../sut/sut-model';
import { TJobModel } from '../tjob/tjob-model';
import { TJobExecModel } from './tjobExec-model';
import { Http, Response } from '@angular/http';
import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs/Rx';
import 'rxjs/Rx';
import { TestCaseModel } from '../test-case/test-case-model';
import { TestSuiteModel } from '../test-suite/test-suite-model';
import { LogAnalyzerService, StartFinishTestCaseTraces } from '../../elastest-log-analyzer/log-analyzer.service';
import { sleep } from '../../shared/utils';
import { ESRangeModel } from '../../shared/elasticsearch-model/es-query-model';
import { MetricTraces } from '../../shared/services/elastest-es.service';

@Injectable()
export class TJobExecService {
  constructor(
    private http: Http,
    private configurationService: ConfigurationService,
    private eTModelsTransformServices: ETModelsTransformServices,
    public popupService: PopupService,
    public logAnalyzerService: LogAnalyzerService,
  ) {}

  //  TJobExecution functions
  public runTJob(tJobId: number, parameters?: any[], sutParams?: any[]): Observable<TJobExecModel> {
    let url: string = this.configurationService.configModel.hostApi + '/tjob/' + tJobId + '/exec';
    let body: any = {};
    if (parameters) {
      body['tJobParams'] = parameters;
    }

    if (sutParams) {
      body['sutParams'] = sutParams;
    }

    return this.http.post(url, body).map((response: Response) => {
      let data: any = response.json();
      if (response.status !== 200) {
        let msg: string = 'Sut instrumented by Elastest is still activating beats. Wait and try again';
        this.popupService.openSnackBar(response.status + ' Code: ' + msg, 'OK', 5000);
        throw new Error(msg);
      } else {
        if (data !== undefined && data !== null) {
          return this.eTModelsTransformServices.jsonToTJobExecModel(response.json());
        } else {
          throw new Error('Empty response.');
        }
      }
    });
  }

  // Get all Executions of a TJob
  public getTJobsExecutions(tJob: TJobModel): Observable<TJobExecModel[]> {
    let url: string = this.configurationService.configModel.hostApi + '/tjob/' + tJob.id + '/exec';
    return this.http.get(url).map((response) => this.eTModelsTransformServices.jsonToTJobExecsList(response.json()));
  }

  public getTJobExecutionFiles(tJobId: number, tJobExecId: number) {
    let url: string = this.configurationService.configModel.hostApi + '/tjob/' + tJobId + '/exec/' + tJobExecId + '/files';
    return this.http.get(url).map((response) => response.json());
  }

  /*public getTJobExecutionFiles(tJobExec: TJobExecModel){
    let url: string = this.configurationService.configModel.hostApi + '/tjob/' + tJobExec.tJob.id + '/exec/' + tJobExec.id + '/files';
    return this.http.get(url)
    .map((response) => console.log(response.json()));
  }*/

  // Get a specific TJob Execution
  public getTJobExecution(tJob: TJobModel, idTJobExecution: number): Observable<TJobExecModel> {
    return this.getTJobExecutionByTJobId(tJob.id, idTJobExecution);
  }

  public getAllTJobExecs(): Observable<TJobExecModel[]> {
    let url: string = this.configurationService.configModel.hostApi + '/tjob/execs';
    return this.http.get(url).map((response) => {
      let data: any = response.json();
      if (data !== undefined && data !== null) {
        return this.eTModelsTransformServices.jsonToTJobExecsList(data);
      } else {
        throw new Error("Empty response. There are not TJobExecutions or you don't have permissions to access them");
      }
    });
  }

  public getLastNTJobExecutions(n: number): Observable<TJobExecModel[]> {
    let url: string = this.configurationService.configModel.hostApi + '/tjob/execs/last/' + n;
    return this.http.get(url).map((response) => {
      let data: any = response.json();
      if (data !== undefined && data !== null) {
        return this.eTModelsTransformServices.jsonToTJobExecsList(data);
      } else {
        throw new Error("Empty response. There are not TJobExecutions or you don't have permissions to access them");
      }
    });
  }

  public getAllRunningTJobExecutions(): Observable<TJobExecModel[]> {
    let url: string = this.configurationService.configModel.hostApi + '/tjob/execs/running';
    return this.http.get(url).map((response) => {
      let data: any = response.json();
      if (data !== undefined && data !== null) {
        return this.eTModelsTransformServices.jsonToTJobExecsList(data);
      } else {
        throw new Error("Empty response. There are not TJobExecutions or you don't have permissions to access them");
      }
    });
  }

  public getLastNRunningTJobExecutions(n: number): Observable<TJobExecModel[]> {
    let url: string = this.configurationService.configModel.hostApi + '/tjob/execs/running/last/' + n;
    return this.http.get(url).map((response) => {
      let data: any = response.json();
      if (data !== undefined && data !== null) {
        return this.eTModelsTransformServices.jsonToTJobExecsList(data);
      } else {
        throw new Error("Empty response. There are not TJobExecutions or you don't have permissions to access them");
      }
    });
  }

  public getAllFinishedOrNotExecutedTJobExecutions(): Observable<TJobExecModel[]> {
    let url: string = this.configurationService.configModel.hostApi + '/tjob/execs/finished';
    return this.http.get(url).map((response) => {
      let data: any = response.json();
      if (data !== undefined && data !== null) {
        return this.eTModelsTransformServices.jsonToTJobExecsList(data);
      } else {
        throw new Error("Empty response. There are not TJobExecutions or you don't have permissions to access them");
      }
    });
  }

  public getLastNFinishedOrNotExecutedTJobExecutions(n: number): Observable<TJobExecModel[]> {
    let url: string = this.configurationService.configModel.hostApi + '/tjob/execs/finished/last/' + n;
    return this.http.get(url).map((response) => {
      let data: any = response.json();
      if (data !== undefined && data !== null) {
        return this.eTModelsTransformServices.jsonToTJobExecsList(data);
      } else {
        throw new Error("Empty response. There are not TJobExecutions or you don't have permissions to access them");
      }
    });
  }

  public getTJobExecutionByTJobId(tJobId: number, idTJobExecution: number): Observable<TJobExecModel> {
    let url: string = this.configurationService.configModel.hostApi + '/tjob/' + tJobId + '/exec/' + idTJobExecution;
    return this.http.get(url).map((response) => {
      let data: any = response.json();
      if (data !== undefined && data !== null) {
        return this.eTModelsTransformServices.jsonToTJobExecModel(data);
      } else {
        throw new Error("Empty response. TJob Execution not exist or you don't have permissions to access it");
      }
    });
  }

  public stopTJobExecution(tJob: TJobModel, tJobExecution: TJobExecModel): Observable<TJobExecModel> {
    let url: string = this.configurationService.configModel.hostApi + '/tjob/' + tJob.id + '/exec/' + tJobExecution.id + '/stop';
    return this.http.delete(url).map((response) => {
      let data: any = response.json();
      if (data !== undefined && data !== null) {
        return this.eTModelsTransformServices.jsonToTJobExecModel(data);
      } else {
        throw new Error("Empty response. TJob Execution not exist or you don't have permissions to access it");
      }
    });
  }

  public deleteTJobExecution(tJob: TJobModel, tJobExecution: TJobExecModel): Observable<any> {
    let url: string = this.configurationService.configModel.hostApi + '/tjob/' + tJob.id + '/exec/' + tJobExecution.id;
    return this.http.delete(url).map((response) => response.json());
  }

  public getResultStatus(tJob: TJobModel, tJobExecution: TJobExecModel): Observable<any> {
    let url: string =
      this.configurationService.configModel.hostApi + '/tjob/' + tJob.id + '/exec/' + tJobExecution.id + '/result';
    return this.http.get(url).map((response) => response.json());
  }

  /* ************* */
  /* *** Utils *** */
  /* ************* */

  loadTestSuitesInfoToDownload(tJobExec: TJobExecModel, testSuites: TestSuiteModel[]): Observable<boolean> {
    let _suites: Subject<boolean> = new Subject<boolean>();
    let suitesObs: Observable<boolean> = _suites.asObservable();
    this.loadTestSuitesInfoToDownloadByGiven(tJobExec, testSuites, _suites, false);
    return suitesObs;
  }

  loadTestSuitesInfoToDownloadByGiven(
    tJobExec: TJobExecModel,
    testSuites: TestSuiteModel[],
    _suites: Subject<boolean>,
    someTestSuiteWithDate: boolean,
  ): void {
    if (testSuites.length > 0) {
      let suite: TestSuiteModel = testSuites.shift();
      this.loadTestCasesInfoToDownload(tJobExec, [...suite.testCases]).subscribe(
        (someTestCaseWithDate: boolean) => {
          someTestSuiteWithDate = someTestSuiteWithDate || someTestCaseWithDate;

          this.loadTestSuitesInfoToDownloadByGiven(tJobExec, testSuites, _suites, someTestSuiteWithDate);
        },
        (error: Error) => {
          _suites.error(error);
        },
      );
    } else {
      sleep(1000).then(() => {
        // because sometimes, next is fired before subscription
        _suites.next(someTestSuiteWithDate);
      });
    }
  }

  loadTestCasesInfoToDownload(tJobExec: TJobExecModel, testCases: TestCaseModel[]): Observable<boolean> {
    let _cases: Subject<boolean> = new Subject<boolean>();
    let casesObs: Observable<boolean> = _cases.asObservable();
    this.loadTestCasesInfoToDownloadByGiven(tJobExec, testCases, _cases, false);
    return casesObs;
  }

  loadTestCasesInfoToDownloadByGiven(
    tJobExec: TJobExecModel,
    testCases: TestCaseModel[],
    _cases: Subject<boolean>,
    someTestCaseWithDate: boolean,
  ): void {
    if (testCases.length > 0) {
      let tCase: TestCaseModel = testCases.shift();

      // Obtain start/finish traces first
      this.logAnalyzerService
        .searchTestCaseStartAndFinishTraces(tCase.name, [tJobExec.monitoringIndex], tJobExec.startDate, tJobExec.endDate)
        .subscribe(
          (startFinishObj: StartFinishTestCaseTraces) => {
            let _logs: Subject<any[]> = new Subject<any[]>();
            let logsObs: Observable<any[]> = _logs.asObservable();

            // Logs
            this.logAnalyzerService.searchTestCaseLogsByGivenStartFinishTraces(
              tCase.name,
              [tJobExec.monitoringIndex],
              startFinishObj,
              _logs,
            );

            logsObs.subscribe(
              (logs: any[]) => {
                someTestCaseWithDate = true;
                tCase['logs'] = logs;
                let timeRange: ESRangeModel = this.logAnalyzerService.elastestESService.getRangeByGiven(
                  startFinishObj.startDate,
                  startFinishObj.finishDate,
                );

                // Metrics
                this.logAnalyzerService.elastestESService.getAllTJobExecMetrics(tJobExec, timeRange).subscribe(
                  (metricsTraces: MetricTraces[]) => {
                    tCase['metrics'] = metricsTraces;

                    this.loadTestCasesInfoToDownloadByGiven(tJobExec, testCases, _cases, someTestCaseWithDate);
                  },
                  (error: Error) => {
                    this.loadTestCasesInfoToDownloadByGiven(tJobExec, testCases, _cases, someTestCaseWithDate);
                  },
                );
              },
              (error: Error) => {
                this.loadTestCasesInfoToDownloadByGiven(tJobExec, testCases, _cases, someTestCaseWithDate);
              },
            );
          },
          (error: Error) => {
            _cases.error(error);
          },
        );
    } else {
      _cases.next(someTestCaseWithDate);
    }
  }
}
