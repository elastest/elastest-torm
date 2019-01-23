import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import 'rxjs/Rx';
import { ConfigurationService } from '../../config/configuration-service.service';
import { ETModelsTransformServices } from '../../shared/services/et-models-transform.service';
import { TestSuiteModel } from '../test-suite/test-suite-model';
import { TestCaseModel } from './test-case-model';

@Injectable()
export class TestCaseService {
  constructor(
    private http: HttpClient,
    private configurationService: ConfigurationService,
    private eTModelsTransformServices: ETModelsTransformServices,
  ) {}

  public getTestCasesByTestSuiteIdTJobExecIdAndTJobId(
    testSuiteId: number,
    tJobExecId: number,
    tJobId: number,
  ): Observable<TestCaseModel[]> {
    let url: string =
      this.configurationService.configModel.hostApi +
      '/tjob/' +
      tJobId +
      '/exec/' +
      tJobExecId +
      '/testsuite/' +
      testSuiteId +
      '/testcase';
    return this.http.get(url).map((data: any[]) => this.eTModelsTransformServices.jsonToTestCasesList(data));
  }

  public getTestCasesByTestSuite(testSuite: TestSuiteModel): Observable<TestCaseModel[]> {
    return this.getTestCasesByTestSuiteIdTJobExecIdAndTJobId(testSuite.id, testSuite.tJobExec.id, testSuite.tJobExec.tJob.id);
  }

  public getTestCaseById(testCaseId: number): Observable<TestCaseModel> {
    let url: string = this.configurationService.configModel.hostApi + '/tjob/exec/testsuite/testcase/' + testCaseId;
    return this.http.get(url).map((data: any) => this.eTModelsTransformServices.jsonToTestCaseModel(data));
  }

  public getTestCase(testCase: TestCaseModel): Observable<TestCaseModel> {
    return this.getTestCaseById(testCase.id);
  }
}
