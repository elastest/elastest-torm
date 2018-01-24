import { PopupService } from '../shared/services/popup.service';
import { TestCaseModel } from './models/test-case-model';
import { TestSuiteModel } from './models/test-suite-model';
import { TestPlanModel } from './models/test-plan-model';
import { Injectable } from '@angular/core';
import { ETTestlinkModelsTransformService } from '../shared/services/et-testlink-models-transform.service';
import { Observable } from 'rxjs/Rx';
import { ConfigurationService } from '../config/configuration-service.service';
import { Http, Response } from '@angular/http';
import { TestProjectModel } from './models/test-project-model';
import { BuildModel } from './models/build-model';
import { TestCaseExecutionModel } from './models/test-case-execution-model';

@Injectable()
export class TestLinkService {

    hostApi: string;

    constructor(
        private http: Http, private configurationService: ConfigurationService,
        public eTTestlinkModelsTransformService: ETTestlinkModelsTransformService,
        public popupService: PopupService,
    ) {
        this.hostApi = this.configurationService.configModel.hostApi;
    }

    /***********************/
    /******** Others *******/
    /***********************/

    public getTestlinkUrl(): Observable<String> {
        let url: string = this.hostApi + '/testlink/url';
        return this.http.get(url)
            .map((response: Response) => response['_body']);
    }

    public syncTestlink(): Observable<Boolean> {
        let url: string = this.hostApi + '/testlink/sync';
        return this.http.get(url)
            .map((response: Response) => response['_body']);
    }

    /*************************/
    /******** Projects *******/
    /*************************/

    public getAllTestProjects(): Observable<TestProjectModel[]> {
        let url: string = this.hostApi + '/testlink/project';
        return this.http.get(url)
            .map((response: Response) => this.eTTestlinkModelsTransformService.jsonToTestProjectsList(response.json()));
    }

    public getProjectByName(project: TestProjectModel): Observable<TestProjectModel> {
        let url: string = this.hostApi + '/testlink/project/name/' + project.name;
        return this.http.get(url)
            .map((response: Response) => this.eTTestlinkModelsTransformService.jsonToTestProjectModel(response.json()));
    }

    public getProjectById(projectId: number): Observable<TestProjectModel> {
        let url: string = this.hostApi + '/testlink/project/' + projectId;
        return this.http.get(url)
            .map((response: Response) => this.eTTestlinkModelsTransformService.jsonToTestProjectModel(response.json()));
    }

    public createProject(project: TestProjectModel): Observable<TestProjectModel> {
        let url: string = this.configurationService.configModel.hostApi + '/testlink/project';
        return this.http.post(url, project)
            .map((response: Response) => response.json());
    }

    /***********************/
    /******** Suites *******/
    /***********************/

    public getProjecTestSuites(project: TestProjectModel): Observable<TestSuiteModel[]> {
        let url: string = this.hostApi + '/testlink/project/' + project.id + '/suite';
        return this.http.get(url)
            .map((response: Response) => this.eTTestlinkModelsTransformService.jsonToTestSuiteList(response.json()));
    }

    public getTestSuite(suite: TestSuiteModel): Observable<TestSuiteModel> {
        return this.getTestSuiteById(suite.id, suite.testProjectId);
    }

    public getTestSuiteById(suiteId: number, projectId: number): Observable<TestSuiteModel> {
        let url: string = this.hostApi + '/testlink/project/' + projectId + '/suite/' + suiteId;
        return this.http.get(url)
            .map((response: Response) => this.eTTestlinkModelsTransformService.jsonToTestSuiteModel(response.json()));
    }

    public createTestSuite(suite: TestSuiteModel): Observable<TestSuiteModel> {
        let url: string = this.hostApi + '/testlink/project/' + suite.testProjectId + '/suite';
        return this.http.post(url, suite)
            .map((response: Response) => response.json());
    }

    /*************************/
    /********* Cases *********/
    /*************************/

    public getSuiteTestCases(suite: TestSuiteModel): Observable<TestCaseModel[]> {
        let url: string = this.hostApi + '/testlink/project/' + suite.testProjectId + '/suite/' + suite.id + '/case';
        return this.http.get(url)
            .map((response: Response) => this.eTTestlinkModelsTransformService.jsonToTestCasesList(response.json()));
    }

    public getTestCase(testCase: TestCaseModel): Observable<TestCaseModel> {
        return this.getTestCaseById(testCase.id);
    }

    public getTestCaseById(testCaseId: number): Observable<TestCaseModel> {
        let url: string = this.hostApi + '/testlink/project/suite/case/' + testCaseId;
        return this.http.get(url)
            .map((response: Response) => this.eTTestlinkModelsTransformService.jsonToTestCaseModel(response.json()));
    }

    public createTestCase(testCase: TestCaseModel): Observable<TestCaseModel> {
        let url: string = this.hostApi + '/testlink/project/' + testCase.testProjectId + '/suite/' + testCase.testSuiteId + '/case';
        return this.http.post(url, testCase)
            .map((response: Response) => response.json());
    }

    /***********************/
    /******** Plans ********/
    /***********************/

    public getProjectTestPlans(project: TestProjectModel): Observable<TestPlanModel[]> {
        let url: string = this.hostApi + '/testlink/project/' + project.id + '/plan';
        return this.http.get(url)
            .map((response: Response) => this.eTTestlinkModelsTransformService.jsonToTestPlanList(response.json()));
    }

    public getTestPlan(plan: TestPlanModel): Observable<TestPlanModel> {
        return this.getTestPlanByName(plan.name, plan.projectName);
    }

    public getTestPlanByName(planName: string, projectName: string): Observable<TestPlanModel> {
        let url: string = this.hostApi + '/testlink/project/' + projectName + '/plan/name/' + planName;
        return this.http.get(url)
            .map((response: Response) => this.eTTestlinkModelsTransformService.jsonToTestPlanModel(response.json()));
    }

    public getTestPlanById(planId: number): Observable<TestPlanModel> {
        let url: string = this.hostApi + '/testlink/project/plan/' + planId;
        return this.http.get(url)
            .map((response: Response) => this.eTTestlinkModelsTransformService.jsonToTestPlanModel(response.json()));
    }

    public createTestPlan(plan: TestPlanModel): Observable<TestCaseModel> {
        let url: string = this.hostApi + '/testlink/project/' + plan.projectName + '/plan';
        return this.http.post(url, plan)
            .map((response: Response) => response.json());
    }


    /************************/
    /******** Builds ********/
    /************************/

    public getPlanBuilds(plan: TestPlanModel): Observable<BuildModel[]> {
        let url: string = this.hostApi + '/testlink/project/' + plan.projectName + '/plan/' + plan.id + '/build';
        return this.http.get(url)
            .map((response: Response) => this.eTTestlinkModelsTransformService.jsonToBuildList(response.json()));
    }

    public getLatestPlanBuild(plan: TestPlanModel): Observable<BuildModel> {
        let url: string = this.hostApi + '/testlink/project/' + plan.projectName + '/plan/' + plan.id + '/build/latestF';
        return this.http.get(url)
            .map((response: Response) => this.eTTestlinkModelsTransformService.jsonToBuildModel(response.json()));
    }

    public getBuildById(buildId: number): Observable<BuildModel> {
        let url: string = this.hostApi + '/testlink/project/plan/build/' + buildId;
        return this.http.get(url)
            .map((response: Response) => this.eTTestlinkModelsTransformService.jsonToBuildModel(response.json()));
    }

    public createBuild(build: BuildModel): Observable<TestCaseModel> {
        let url: string = this.hostApi + '/testlink/project/' + 'dummyprojectId' + '/plan/' + build.testPlanId + '/build';
        return this.http.post(url, build)
            .map((response: Response) => response.json());
    }

    public getBuildTestCases(build: BuildModel): Observable<TestCaseModel[]> {
        let url: string = this.hostApi + '/testlink/project/plan/build/' + build.id + '/case';
        return this.http.get(url)
            .map((response: Response) => this.eTTestlinkModelsTransformService.jsonToTestCasesList(response.json()));
    }


    /***********************/
    /******** Execs ********/
    /***********************/

    public saveExecution(execution: TestCaseExecutionModel, testCaseId: number) {
        let url: string = this.hostApi + '/testlink/project/plan/build/case/' + testCaseId + '/exec';
        return this.http.post(url, execution)
            .map((response: Response) => response.json());
    }

    public getAllExecs(): Observable<TestCaseExecutionModel[]> {
        let url: string = this.hostApi + '/testlink/execs';
        return this.http.get(url)
            .map((response: Response) => this.eTTestlinkModelsTransformService.jsonToExecList(response.json()));
    }

    public getTestCaseExecs(testCaseId: number): Observable<TestCaseExecutionModel[]> {
        let url: string = this.hostApi + '/testlink/project/suite/case/' + testCaseId + '/execs';
        return this.http.get(url)
            .map((response: Response) => this.eTTestlinkModelsTransformService.jsonToExecList(response.json()));
    }

    public getPlanCaseExecs(testCaseId: number, testPlanId: number): Observable<TestCaseExecutionModel[]> {
        let url: string = this.hostApi + '/testlink/project/plan/' + testPlanId + '/build/case/' + testCaseId + '/execs';
        return this.http.get(url)
            .map((response: Response) => this.eTTestlinkModelsTransformService.jsonToExecList(response.json()));
    }


    public getBuildCaseExecs(testCaseId: number, buildId: number): Observable<TestCaseExecutionModel[]> {
        let url: string = this.hostApi + '/testlink/project/plan/build/' + buildId + '/case/' + testCaseId + '/execs';
        return this.http.get(url)
            .map((response: Response) => this.eTTestlinkModelsTransformService.jsonToExecList(response.json()));
    }
}