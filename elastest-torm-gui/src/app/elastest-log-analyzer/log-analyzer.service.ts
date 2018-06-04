import { ElastestESService } from '../shared/services/elastest-es.service';
import { ESMatchModel, ESRangeModel, ESTermModel } from '../shared/elasticsearch-model/es-query-model';
import { ESSearchModel } from '../shared/elasticsearch-model/elasticsearch-model';
import { Observable } from 'rxjs/Rx';
import { Http, Response } from '@angular/http';
import { ETModelsTransformServices } from '../shared/services/et-models-transform.service';
import { ConfigurationService } from '../config/configuration-service.service';
import { Injectable } from '@angular/core';
import { LogAnalyzerConfigModel } from './log-analyzer-config-model';
import { TJobExecModel } from '../elastest-etm/tjob-exec/tjobExec-model';

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
    public elastestESService: ElastestESService,
    private eTModelsTransformServices: ETModelsTransformServices,
  ) {}

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
    let esSearchModel: ESSearchModel = new ESSearchModel();

    // Add term stream_type === 'log'
    esSearchModel.body.boolQuery.bool.must.termList.push(this.streamTypeTerm);
    esSearchModel.body.sort.sortMap.set('@timestamp', 'asc');
    esSearchModel.body.sort.sortMap.set('_uid', 'asc'); // Sort by _id too to prevent traces of the same millisecond being disordered
    return esSearchModel;
  }

  setRangeToEsSearchModelByGiven(
    esSearchModel: ESSearchModel,
    from: Date | string,
    to: Date | string,
    includedFrom: boolean = true,
    includedTo: boolean = true,
  ): ESSearchModel {
    esSearchModel.body.boolQuery.bool.must.range = new ESRangeModel();
    esSearchModel.body.boolQuery.bool.must.range.field = '@timestamp';

    if (includedFrom) {
      esSearchModel.body.boolQuery.bool.must.range.gte = from;
    } else {
      esSearchModel.body.boolQuery.bool.must.range.gt = from;
    }

    if (includedTo) {
      esSearchModel.body.boolQuery.bool.must.range.lte = to;
    } else {
      esSearchModel.body.boolQuery.bool.must.range.lt = to;
    }

    return esSearchModel;
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

  searchTJobExecTraceByGivenMsg(msg: string, tJobExec: TJobExecModel, maxResults: number = this.maxResults): Observable<any> {
    return this.searchTraceByGivenMsg(msg, [tJobExec.monitoringIndex], tJobExec.startDate, tJobExec.endDate);
  }

  searchTraceByGivenMsg(
    msg: string,
    indices: string[],
    from: Date,
    to: Date,
    maxResults: number = this.maxResults,
  ): Observable<any> {
    let esSearchModel: ESSearchModel = this.initAndGetESModel();

    esSearchModel.indices = indices;
    esSearchModel.filterPathList = this.filters;
    esSearchModel.body.size = this.maxResults;

    esSearchModel = this.setRangeToEsSearchModelByGiven(esSearchModel, from, to);

    this.setMatchByGivenEsSearchModel(msg, esSearchModel);

    let searchUrl: string = esSearchModel.getSearchUrl(this.elastestESService.esUrl);
    let searchBody: object = esSearchModel.getSearchBody();

    return this.elastestESService.search(searchUrl, searchBody);
  }
}
