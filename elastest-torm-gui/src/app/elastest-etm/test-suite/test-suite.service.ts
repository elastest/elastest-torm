import { Http } from '@angular/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import 'rxjs/Rx';
import { TJobExecModel } from '../tjob-exec/tjobExec-model';
import { ConfigurationService } from '../../config/configuration-service.service';
import { ETModelsTransformServices } from '../../shared/services/et-models-transform.service';
import { TestSuiteModel } from './test-suite-model';

@Injectable()
export class TestSuiteService {
  constructor(
    private http: Http,
    private configurationService: ConfigurationService,
    private eTModelsTransformServices: ETModelsTransformServices,
  ) {}

  public getTestSuitesByTJobExec(tJobExec: TJobExecModel): Observable<TestSuiteModel[]> {
    return this.getTestSuitesByTJobExecIdAndTJobId(tJobExec.id, tJobExec.tJob.id);
  }

  public getTestSuitesByTJobExecIdAndTJobId(tJobExecId: number, tJobId: number): Observable<TestSuiteModel[]> {
    let url: string = this.configurationService.configModel.hostApi + '/tjob/' + tJobId + '/exec/' + tJobExecId + '/testsuite';
    return this.http.get(url).map((response) => this.eTModelsTransformServices.jsonToTestSuitesList(response.json()));
  }
}
