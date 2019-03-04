import { PopupService } from '../shared/services/popup.service';
import { TLTestCaseModel } from './models/test-case-model';
import { TLTestSuiteModel } from './models/test-suite-model';
import { TestPlanModel } from './models/test-plan-model';
import { Injectable } from '@angular/core';
import { ETTestlinkModelsTransformService } from '../shared/services/et-testlink-models-transform.service';
import { Observable } from 'rxjs/Rx';
import { ConfigurationService } from '../config/configuration-service.service';
import { TestProjectModel } from './models/test-project-model';
import { BuildModel } from './models/build-model';
import { TestCaseExecutionModel } from './models/test-case-execution-model';
import { ExternalProjectModel } from '../elastest-etm/external/external-project/external-project-model';
import { ETExternalModelsTransformService } from '../elastest-etm/external/et-external-models-transform.service';
import { ExternalTJobModel } from '../elastest-etm/external/external-tjob/external-tjob-model';
import { ExternalTestCaseModel } from '../elastest-etm/external/external-test-case/external-test-case-model';
import { ExternalTestExecutionModel } from '../elastest-etm/external/external-test-execution/external-test-execution-model';
import { EtPluginsService } from '../elastest-test-engines/et-plugins.service';
import { EtPluginModel } from '../elastest-test-engines/et-plugin-model';
import { HttpClient, HttpResponse } from '@angular/common/http';

@Injectable()
export class TestLinkService {
  hostApi: string;

  constructor(
    private http: HttpClient,
    private configurationService: ConfigurationService,
    public eTTestlinkModelsTransformService: ETTestlinkModelsTransformService,
    public eTExternalModelsTransformService: ETExternalModelsTransformService,
    public popupService: PopupService,
    public etPluginsService: EtPluginsService,
  ) {
    this.hostApi = this.configurationService.configModel.hostApi;
  }

  /***********************/
  /******** Others *******/
  /***********************/

  public isStarted(): Observable<boolean> {
    let url: string = this.hostApi + '/testlink/started';
    return this.http.get(url).map((data: boolean) => data);
  }

  public isReady(): Observable<boolean> {
    let url: string = this.hostApi + '/testlink/ready';
    return this.http.get(url).map((data: boolean) => data);
  }

  public startTestLink(): Observable<EtPluginModel> {
    let url: string = this.hostApi + '/testlink/start';
    return this.http
      .post(url, undefined, { observe: 'response' })
      .map((response: HttpResponse<any>) => this.etPluginsService.transformRawTestEngine(response.body));
  }

  public getTestlinkUrl(): Observable<string> {
    let url: string = this.hostApi + '/testlink/url';
    return this.http.get(url).map((data: string) => data);
  }

  public syncTestlink(): Observable<boolean> {
    let url: string = this.hostApi + '/testlink/sync';
    return this.http.get(url).map((data: boolean) => data);
  }

  public dropAllExternalTLData(): Observable<boolean> {
    let url: string = this.hostApi + '/testlink/drop';
    return this.http.delete(url, { observe: 'response' }).map((data: HttpResponse<boolean>) => {
      return data.body;
    });
  }

  /*************************/
  /******** Projects *******/
  /*************************/

  public getAllTestProjects(): Observable<TestProjectModel[]> {
    let url: string = this.hostApi + '/testlink/project';
    return this.http.get(url).map((data: any[]) => this.eTTestlinkModelsTransformService.jsonToTestProjectsList(data).reverse());
  }

  public getProjectByName(project: TestProjectModel): Observable<TestProjectModel> {
    let url: string = this.hostApi + '/testlink/project/name/' + project.name;
    return this.http.get(url).map((data: any) => this.eTTestlinkModelsTransformService.jsonToTestProjectModel(data));
  }

  public getProjectById(projectId: number | string): Observable<TestProjectModel> {
    let url: string = this.hostApi + '/testlink/project/' + projectId;
    return this.http.get(url).map((data: any) => this.eTTestlinkModelsTransformService.jsonToTestProjectModel(data));
  }

  public createProject(project: TestProjectModel): Observable<TestProjectModel> {
    let url: string = this.configurationService.configModel.hostApi + '/testlink/project';
    return this.http.post(url, project, { observe: 'response' }).map((response: HttpResponse<any>) => response.body);
  }

  /***********************/
  /******** Suites *******/
  /***********************/

  public getProjecTestSuites(project: TestProjectModel): Observable<TLTestSuiteModel[]> {
    let url: string = this.hostApi + '/testlink/project/' + project.id + '/suite';
    return this.http.get(url).map((data: any[]) => this.eTTestlinkModelsTransformService.jsonToTestSuiteList(data));
  }

  public getTestSuite(suite: TLTestSuiteModel): Observable<TLTestSuiteModel> {
    return this.getTestSuiteById(suite.id, suite.testProjectId);
  }

  public getTestSuiteById(suiteId: number | string, projectId: number | string): Observable<TLTestSuiteModel> {
    let url: string = this.hostApi + '/testlink/project/' + projectId + '/suite/' + suiteId;
    return this.http.get(url).map((data: any[]) => this.eTTestlinkModelsTransformService.jsonToTestSuiteModel(data));
  }

  public createTestSuite(suite: TLTestSuiteModel): Observable<TLTestSuiteModel> {
    let url: string = this.hostApi + '/testlink/project/' + suite.testProjectId + '/suite';
    return this.http.post(url, suite, { observe: 'response' }).map((response: HttpResponse<any>) => response.body);
  }

  /*************************/
  /********* Cases *********/
  /*************************/

  public getSuiteTestCases(suite: TLTestSuiteModel): Observable<TLTestCaseModel[]> {
    let url: string = this.hostApi + '/testlink/project/' + suite.testProjectId + '/suite/' + suite.id + '/case';
    return this.http.get(url).map((data: any[]) => this.eTTestlinkModelsTransformService.jsonToTestCasesList(data));
  }

  public getTestCase(testCase: TLTestCaseModel): Observable<TLTestCaseModel> {
    return this.getTestCaseById(testCase.id);
  }

  public getTestCaseById(testCaseId: number | string): Observable<TLTestCaseModel> {
    let url: string = this.hostApi + '/testlink/project/suite/case/' + testCaseId;
    return this.http.get(url).map((data: any) => this.eTTestlinkModelsTransformService.jsonToTestCaseModel(data));
  }

  public createTestCase(testCase: TLTestCaseModel): Observable<TLTestCaseModel> {
    let url: string = this.hostApi + '/testlink/project/' + testCase.testProjectId + '/suite/' + testCase.testSuiteId + '/case';
    return this.http.post(url, testCase, { observe: 'response' }).map((response: HttpResponse<any>) => response.body);
  }

  public getBuildTestCase(build: BuildModel, testCase: TLTestCaseModel): Observable<TLTestCaseModel> {
    return this.getBuildTestCaseById(build.id, testCase.id);
  }

  public getBuildTestCaseById(buildId: string | number, testCaseId: string | number): Observable<TLTestCaseModel> {
    let url: string = this.hostApi + '/testlink/project/plan/build/' + buildId + '/case/' + testCaseId;
    return this.http.get(url).map((data: any) => {
      if (data) {
        return this.eTTestlinkModelsTransformService.jsonToTestCaseModel(data);
      } else {
        return undefined;
      }
    });
  }

  /***********************/
  /******** Plans ********/
  /***********************/

  public getProjectTestPlans(project: TestProjectModel): Observable<TestPlanModel[]> {
    let url: string = this.hostApi + '/testlink/project/' + project.id + '/plan';
    return this.http.get(url).map((data: any[]) => this.eTTestlinkModelsTransformService.jsonToTestPlanList(data));
  }

  public getTestPlan(plan: TestPlanModel): Observable<TestPlanModel> {
    return this.getTestPlanByName(plan.name, plan.projectName);
  }

  public getTestPlanByName(planName: string, projectName: string): Observable<TestPlanModel> {
    let url: string = this.hostApi + '/testlink/project/' + projectName + '/plan/name/' + planName;
    return this.http.get(url).map((data: any) => this.eTTestlinkModelsTransformService.jsonToTestPlanModel(data));
  }

  public getTestPlanById(planId: number | string): Observable<TestPlanModel> {
    let url: string = this.hostApi + '/testlink/project/plan/' + planId;
    return this.http.get(url).map((data: any) => this.eTTestlinkModelsTransformService.jsonToTestPlanModel(data));
  }

  public createTestPlan(plan: TestPlanModel): Observable<TLTestCaseModel> {
    let url: string = this.hostApi + '/testlink/project/plan';
    return this.http.post(url, plan, { observe: 'response' }).map((response: HttpResponse<any>) => response.body);
  }

  /************************/
  /******** Builds ********/
  /************************/

  public getPlanBuildsById(planId: number | string): Observable<BuildModel[]> {
    let url: string = this.hostApi + '/testlink/project/plan/' + planId + '/build';
    return this.http.get(url).map((data: any[]) => this.eTTestlinkModelsTransformService.jsonToBuildList(data));
  }

  public getPlanBuildById(planId: number | string, buildId: number | string): Observable<BuildModel> {
    let url: string = this.hostApi + '/testlink/project/plan/' + planId + '/build/' + buildId;
    return this.http.get(url).map((data: any) => this.eTTestlinkModelsTransformService.jsonToBuildModel(data));
  }

  public getPlanBuilds(plan: TestPlanModel): Observable<BuildModel[]> {
    return this.getPlanBuildsById(plan.id);
  }

  public getPlanTestCasesById(planId: number | string): Observable<TLTestCaseModel[]> {
    let url: string = this.hostApi + '/testlink/project/plan/' + planId + '/case';
    return this.http.get(url).map((data: any[]) => this.eTTestlinkModelsTransformService.jsonToTestCasesList(data));
  }

  public getPlanTestCases(plan: TestPlanModel): Observable<TLTestCaseModel[]> {
    return this.getPlanTestCasesById(plan.id);
  }

  public getLatestPlanBuild(plan: TestPlanModel): Observable<BuildModel> {
    let url: string = this.hostApi + '/testlink/project/' + plan.projectName + '/plan/' + plan.id + '/build/latestF';
    return this.http.get(url).map((data: any) => this.eTTestlinkModelsTransformService.jsonToBuildModel(data));
  }

  public getBuildById(buildId: number | string): Observable<BuildModel> {
    let url: string = this.hostApi + '/testlink/project/plan/build/' + buildId;
    return this.http.get(url).map((data: any) => this.eTTestlinkModelsTransformService.jsonToBuildModel(data));
  }

  public createBuild(build: BuildModel): Observable<TLTestCaseModel> {
    let url: string = this.hostApi + '/testlink/project/plan/build';
    return this.http.post(url, build, { observe: 'response' }).map((response: HttpResponse<any>) => response.body);
  }

  public getBuildTestCases(build: BuildModel): Observable<TLTestCaseModel[]> {
    let url: string = this.hostApi + '/testlink/project/plan/build/' + build.id + '/case';
    return this.http.get(url).map((data: any[]) => this.eTTestlinkModelsTransformService.jsonToTestCasesList(data));
  }

  /***********************/
  /******** Execs ********/
  /***********************/

  public saveExecution(execution: TestCaseExecutionModel, testCaseId: number | string): Observable<TestCaseExecutionModel> {
    let url: string = this.hostApi + '/testlink/project/plan/build/case/' + testCaseId + '/exec';
    return this.http
      .post(url, execution, { observe: 'response' })
      .map((response: HttpResponse<any>) => this.eTTestlinkModelsTransformService.jsonToExecModel(response.body));
  }

  public getAllExecs(): Observable<TestCaseExecutionModel[]> {
    let url: string = this.hostApi + '/testlink/execs';
    return this.http.get(url).map((data: any[]) => this.eTTestlinkModelsTransformService.jsonToExecList(data));
  }

  public getTestCaseExecs(testCaseId: number | string): Observable<TestCaseExecutionModel[]> {
    let url: string = this.hostApi + '/testlink/project/suite/case/' + testCaseId + '/execs';
    return this.http.get(url).map((data: any[]) => this.eTTestlinkModelsTransformService.jsonToExecList(data));
  }

  public getTestExecById(testCaseId: number | string, testExecId: number | string): Observable<TestCaseExecutionModel> {
    let url: string = this.hostApi + '/testlink/project/suite/case/' + testCaseId + '/exec/' + testExecId;
    return this.http.get(url).map((data: any) => this.eTTestlinkModelsTransformService.jsonToExecModel(data));
  }

  public getPlanCaseExecs(testCaseId: number | string, testPlanId: number | string): Observable<TestCaseExecutionModel[]> {
    let url: string = this.hostApi + '/testlink/project/plan/' + testPlanId + '/build/case/' + testCaseId + '/execs';
    return this.http.get(url).map((data: any[]) => this.eTTestlinkModelsTransformService.jsonToExecList(data));
  }

  public getBuildCaseExecs(testCaseId: number | string, buildId: number | string): Observable<TestCaseExecutionModel[]> {
    let url: string = this.hostApi + '/testlink/project/plan/build/' + buildId + '/case/' + testCaseId + '/execs';
    return this.http.get(url).map((data: any[]) => this.eTTestlinkModelsTransformService.jsonToExecList(data));
  }

  /*************************/
  /******** External *******/
  /*************************/

  public getExternalProjectByTestProjectId(projectId: number | string): Observable<ExternalProjectModel> {
    let url: string = this.hostApi + '/testlink/external/project/' + projectId;
    return this.http.get(url).map((data: any) => this.eTExternalModelsTransformService.jsonToExternalProjectModel(data));
  }

  public getExternalTJobByTestPlanId(planId: number | string): Observable<ExternalTJobModel> {
    let url: string = this.hostApi + '/testlink/external/tjob/' + planId;
    return this.http.get(url).map((data: any) => this.eTExternalModelsTransformService.jsonToExternalTJobModel(data));
  }

  public getExternalTestCaseByTestCaseId(caseId: number | string): Observable<ExternalTestCaseModel> {
    let url: string = this.hostApi + '/testlink/external/testcase/' + caseId;
    return this.http.get(url).map((data: any) => this.eTExternalModelsTransformService.jsonToExternalTestCaseModel(data));
  }

  public getExternalTestExecutionByExecutionId(execId: number | string): Observable<ExternalTestExecutionModel> {
    let url: string = this.hostApi + '/testlink/external/testexec/' + execId;
    return this.http.get(url).map((data: any) => this.eTExternalModelsTransformService.jsonToExternalTestExecutionModel(data));
  }
}
