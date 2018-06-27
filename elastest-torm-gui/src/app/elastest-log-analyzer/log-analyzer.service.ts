import { ESMatchModel, ESTermModel } from '../shared/elasticsearch-model/es-query-model';
import { ESSearchModel } from '../shared/elasticsearch-model/elasticsearch-model';
import { Observable, Subject } from 'rxjs/Rx';
import { Http, Response } from '@angular/http';
import { ETModelsTransformServices } from '../shared/services/et-models-transform.service';
import { ConfigurationService } from '../config/configuration-service.service';
import { Injectable } from '@angular/core';
import { LogAnalyzerConfigModel } from './log-analyzer-config-model';
import { MonitoringService } from '../shared/services/monitoring.service';
import { LogAnalyzerQueryModel } from '../shared/loganalyzer-query.model';

@Injectable()
export class LogAnalyzerService {
  public startTestCasePrefix: string = '##### Start test: ';
  public endTestCasePrefix: string = '##### Finish test: ';

  public streamType: string = 'log';
  public streamTypeTerm: ESTermModel = new ESTermModel();

  public filters: string[] = ['@timestamp', 'message', 'level', 'et_type', 'component', 'stream', 'stream_type', 'exec'];

  maxResults: number = 10000;

  constructor(
    private http: Http,
    private configurationService: ConfigurationService,
    public monitoringService: MonitoringService,
    private eTModelsTransformServices: ETModelsTransformServices,
  ) {
    this.initStreamTypeTerm();
  }

  initStreamTypeTerm(): void {
    this.streamTypeTerm.name = 'stream_type';
    this.streamTypeTerm.value = this.streamType;
  }

  public getLogAnalyzerConfig(): Observable<LogAnalyzerConfigModel> {
    let url: string = this.configurationService.configModel.hostApi + '/loganalyzerconfig/';
    return this.http.get(url).map((response: Response) => {
      let errorOccur: boolean = true;
      if (response !== undefined && response !== null) {
        if (response['_body']) {
          let data: any = response.json();
          if (data !== undefined && data !== null) {
            errorOccur = false;
            return this.eTModelsTransformServices.jsonToLogAnalyzerConfigModel(data);
          }
        } else {
          errorOccur = false;
          return undefined;
        }
      }
      if (errorOccur) {
        throw new Error("Empty response. LogAnalyzerConfig not exist or you don't have permissions to access it");
      }
    });
  }

  public saveLogAnalyzerConfig(logAnalyzerConfigModel: LogAnalyzerConfigModel): Observable<LogAnalyzerConfigModel> {
    let url: string = this.configurationService.configModel.hostApi + '/loganalyzerconfig';
    logAnalyzerConfigModel.generatColumnsConfigJson();
    return this.http.post(url, logAnalyzerConfigModel).map((response: Response) => {
      let data: any = response.json();
      if (data !== undefined && data !== null) {
        return this.eTModelsTransformServices.jsonToLogAnalyzerConfigModel(data);
      } else {
        throw new Error("Empty response. TJob not exist or you don't have permissions to access it");
      }
    });
  }

  public initAndGetESModel(): ESSearchModel {
    //TODO remove
    let esSearchModel: ESSearchModel = new ESSearchModel();

    // Add term stream_type === 'log'
    esSearchModel.body.boolQuery.bool.must.termList.push(this.streamTypeTerm);
    esSearchModel.body.sort.sortMap.set('@timestamp', 'asc');
    esSearchModel.body.sort.sortMap.set('_uid', 'asc'); // Sort by _id too to prevent traces of the same millisecond being disordered
    return esSearchModel;
  }

  setRangeToEsSearchModelByGiven(
    //TODO remove
    esSearchModel: ESSearchModel,
    from: Date | string,
    to: Date | string,
    includedFrom: boolean = true,
    includedTo: boolean = true,
  ): ESSearchModel {
    esSearchModel.body.boolQuery.bool.must.range = this.monitoringService.getRangeByGiven(from, to, includedFrom, includedTo);

    return esSearchModel;
  }

  setTimeRangeToLogAnalyzerQueryModel(
    logAnalyzerQueryModel: LogAnalyzerQueryModel,
    from: Date | string,
    to: Date | string,
    includedFrom: boolean = true,
    includedTo: boolean = true,
  ): LogAnalyzerQueryModel {
    if (includedFrom) {
      logAnalyzerQueryModel.rangeGTE = from;
    } else {
      logAnalyzerQueryModel.rangeGT = from;
    }

    if (includedTo) {
      logAnalyzerQueryModel.rangeLTE = to;
    } else {
      logAnalyzerQueryModel.rangeLT = to;
    }

    return logAnalyzerQueryModel;
  }

  setMatchByGivenEsSearchModel(msg: string = '', esSearchModel: ESSearchModel): ESSearchModel {
    /* Message field by default */
    if (msg !== '') {
      let messageMatch: ESMatchModel = new ESMatchModel();
      messageMatch.field = 'message';
      messageMatch.query = '*' + msg + '*';
      messageMatch.type = 'phrase_prefix';
      esSearchModel.body.boolQuery.bool.must.matchList.push(messageMatch);
    }
    return esSearchModel;
  }

  setMatchByGivenLogAnalyzerQueryModel(msg: string = '', logAnalyzerQueryModel: LogAnalyzerQueryModel): LogAnalyzerQueryModel {
    /* Message field by default */
    if (msg !== '') {
      logAnalyzerQueryModel.matchMessage = '*' + msg + '*';
    }
    return logAnalyzerQueryModel;
  }

  searchTraceByGivenMsg(
    msg: string,
    indices: string[],
    from: Date,
    to: Date,
    maxResults: number = this.maxResults,
  ): Observable<any> {
    let logAnalyzerQueryModel: LogAnalyzerQueryModel = new LogAnalyzerQueryModel();

    logAnalyzerQueryModel.indices = indices;
    logAnalyzerQueryModel.filterPathList = this.filters;
    logAnalyzerQueryModel.size = this.maxResults;

    logAnalyzerQueryModel = this.setTimeRangeToLogAnalyzerQueryModel(logAnalyzerQueryModel, from, to);
    this.setMatchByGivenLogAnalyzerQueryModel(msg, logAnalyzerQueryModel);

    return this.monitoringService.searchLogAnalyzerQuery(logAnalyzerQueryModel);
  }

  searchTestCaseStartTrace(
    caseName: string,
    indices: string[],
    from: Date,
    to: Date,
    maxResults: number = this.maxResults,
  ): Observable<any> {
    let startMsg: string = this.startTestCasePrefix + caseName;

    return this.searchTraceByGivenMsg(startMsg, indices, from, to, maxResults);
  }

  searchTestCaseFinishTrace(
    caseName: string,
    indices: string[],
    from: Date,
    to: Date,
    maxResults: number = this.maxResults,
  ): Observable<any> {
    let finishMsg: string = this.endTestCasePrefix + caseName;

    return this.searchTraceByGivenMsg(finishMsg, indices, from, to, maxResults);
  }

  searchTestCaseStartAndFinishTraces(
    caseName: string,
    indices: string[],
    from: Date,
    to: Date,
    maxResults: number = this.maxResults,
  ): Observable<StartFinishTestCaseTraces> {
    let _startFinish: Subject<StartFinishTestCaseTraces> = new Subject<StartFinishTestCaseTraces>();
    let startFinishObs: Observable<StartFinishTestCaseTraces> = _startFinish.asObservable();

    let startFinishObj: StartFinishTestCaseTraces = new StartFinishTestCaseTraces();

    this.searchTestCaseStartTrace(caseName, indices, from, to).subscribe(
      (startData: any[]) => {
        if (startData.length > 0) {
          let startRow: any = startData[0];
          let startDate: Date = new Date(startRow['@timestamp']);

          this.searchTestCaseFinishTrace(caseName, indices, from, to).subscribe((finishData: any[]) => {
            if (finishData.length > 0) {
              let finishRow: any = finishData[0];
              let finishDate: Date = new Date(finishRow['@timestamp']);

              startFinishObj.startRow = startRow;
              startFinishObj.startDate = startDate;

              startFinishObj.finishRow = finishRow;
              startFinishObj.finishDate = finishDate;

              _startFinish.next(startFinishObj);
            } else {
              _startFinish.error(
                'An error occurred while trying to obtain the start/finish traces of the test case ' +
                  caseName +
                  ' : finish trace not found',
              );
            }
          });
        } else {
          _startFinish.error(
            'An error occurred while trying to obtain the start/finish traces of the test case ' +
              caseName +
              ' : start trace not found',
          );
        }
      },
      (error: Error) => {
        _startFinish.error(
          'An error occurred while trying to obtain the start/finish traces of the test case ' + caseName + ' : ' + error,
        );
      },
    );

    return startFinishObs;
  }

  searchTestCaseLogsByGivenStartFinishTraces(
    caseName: string,
    indices: string[],
    startFinishObj: StartFinishTestCaseTraces,
    _logs: Subject<any[]>,
    maxResults: number = this.maxResults,
  ): void {
    // Obtain start/finish traces first
    let logAnalyzerQueryModel: LogAnalyzerQueryModel = new LogAnalyzerQueryModel();

    logAnalyzerQueryModel.indices = indices;
    logAnalyzerQueryModel.filterPathList = this.filters;
    logAnalyzerQueryModel.size = this.maxResults;

    logAnalyzerQueryModel.searchAfterTrace = startFinishObj.startRow;

    this.setTimeRangeToLogAnalyzerQueryModel(logAnalyzerQueryModel, startFinishObj.startDate, startFinishObj.finishDate);

    // Load Logs
    this.monitoringService.searchLogAnalyzerQuery(logAnalyzerQueryModel).subscribe(
      (logs: any[]) => {
        let finishRowFullMsg: string = startFinishObj.finishRow.message;
        let finishObj: any = logs.find((x: any) => x.message === finishRowFullMsg);
        if (finishObj) {
          let finishIndex: number = logs.indexOf(finishObj);
          logs.splice(finishIndex);
        }

        let procesedLogs: any = this.monitoringService.getLogsObjFromRawSource(logs);
        _logs.next(procesedLogs);
      },
      (error) => {
        _logs.error('an error has occurred while searching for test case ' + caseName + ' logs: ' + error);
      },
    );
  }

  searchTestCaseLogs(
    caseName: string,
    indices: string[],
    from: Date,
    to: Date,
    maxResults: number = this.maxResults,
  ): Observable<any[]> {
    let _logs: Subject<any[]> = new Subject<any[]>();
    let logsObs: Observable<any[]> = _logs.asObservable();
    // Obtain start/finish traces first
    this.searchTestCaseStartAndFinishTraces(caseName, indices, from, to).subscribe(
      (startFinishObj: StartFinishTestCaseTraces) => {
        this.searchTestCaseLogsByGivenStartFinishTraces(caseName, indices, startFinishObj, _logs, maxResults);
      },
    );
    return logsObs;
  }
}

export class StartFinishTestCaseTraces {
  startRow: any;
  startDate: Date;
  finishRow: any;
  finishDate: Date;

  constructor() {}
}
