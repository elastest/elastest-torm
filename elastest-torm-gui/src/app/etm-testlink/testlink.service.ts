import { PopupService } from '../shared/services/popup.service';
import { TestCaseModel } from './models/test-case-model';
import { TestSuiteModel } from './models/test-suite-model';
import { TestPlanModel } from './models/test-plan-model';
import { Injectable } from '@angular/core';
import { ETTestlinkModelsTransformService } from '../shared/services/et-testlink-models-transform.service';
import { Observable } from 'rxjs/Rx';
import { ConfigurationService } from '../config/configuration-service.service';
import { Http } from '@angular/http';
import { TestProjectModel } from './models/test-project-model';
import { BuildModel } from './models/build-model';
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

    /*************************/
    /******** Projects *******/
    /*************************/

    public getAllTestProjects(): Observable<TestProjectModel[]> {
        let url: string = this.hostApi + '/testlink/project';
        return this.http.get(url)
            .map((response) => this.eTTestlinkModelsTransformService.jsonToTestProjectsList(response.json()));
    }

    public getProjectByName(project: TestProjectModel): Observable<TestProjectModel> {
        let url: string = this.hostApi + '/testlink/project/name/' + project.name;
        return this.http.get(url)
            .map((response) => this.eTTestlinkModelsTransformService.jsonToTestProjectModel(response.json()));
    }

    public getProjectById(projectId: number): Observable<TestProjectModel> {
        let url: string = this.hostApi + '/testlink/project/' + projectId;
        return this.http.get(url)
            .map((response) => this.eTTestlinkModelsTransformService.jsonToTestProjectModel(response.json()));
    }

    public createProject(project: TestProjectModel): Observable<TestProjectModel> {
        let url: string = this.configurationService.configModel.hostApi + '/testlink/project';
        return this.http.post(url, project)
            .map((response) => response.json());
    }

    /***********************/
    /******** Suites *******/
    /***********************/

    public getProjecTestSuites(project: TestProjectModel): Observable<TestSuiteModel[]> {
        let url: string = this.hostApi + '/testlink/project/' + project.id + '/suite';
        return this.http.get(url)
            .map((response) => this.eTTestlinkModelsTransformService.jsonToTestSuiteList(response.json()));
    }

    public getTestSuite(suite: TestSuiteModel): Observable<TestSuiteModel> {
        return this.getTestSuiteById(suite.id, suite.testProjectId);
    }

    public getTestSuiteById(suiteId: number, projectId: number): Observable<TestSuiteModel> {
        let url: string = this.hostApi + '/testlink/project/' + projectId + '/suite/' + suiteId;
        return this.http.get(url)
            .map((response) => this.eTTestlinkModelsTransformService.jsonToTestSuiteModel(response.json()));
    }

    public createTestSuite(suite: TestSuiteModel): Observable<TestSuiteModel> {
        let url: string = this.hostApi + '/testlink/project/' + suite.testProjectId + '/suite';
        return this.http.post(url, suite)
            .map((response) => response.json());
    }

    /*************************/
    /********* Cases *********/
    /*************************/

    public getSuiteTestCases(suite: TestSuiteModel): Observable<TestCaseModel[]> {
        let url: string = this.hostApi + '/testlink/project/' + suite.testProjectId + '/suite/' + suite.id + '/case';
        return this.http.get(url)
            .map((response) => this.eTTestlinkModelsTransformService.jsonToTestCasesList(response.json()));
    }

    public getTestCase(testCase: TestCaseModel): Observable<TestCaseModel> {
        return this.getTestCaseById(testCase.id, testCase.testProjectId, testCase.testSuiteId);
    }

    public getTestCaseById(testCaseId: number, testProjectId: number, testSuiteId: number): Observable<TestCaseModel> {
        let url: string = this.hostApi + '/testlink/project/' + testProjectId + '/suite/' + testSuiteId + '/case/' + testCaseId;
        return this.http.get(url)
            .map((response) => this.eTTestlinkModelsTransformService.jsonToTestCaseModel(response.json()));
    }

    public createTestCase(testCase: TestCaseModel): Observable<TestCaseModel> {       
        let url: string = this.hostApi + '/testlink/project/' + testCase.testProjectId + '/suite/' + testCase.testSuiteId + '/case';
        return this.http.post(url, testCase)
            .map((response) => response.json());
    }

    /***********************/
    /******** Plans ********/
    /***********************/

    public getProjectTestPlans(project: TestProjectModel): Observable<TestPlanModel[]> {
        let url: string = this.hostApi + '/testlink/project/' + project.id + '/plan';
        return this.http.get(url)
            .map((response) => this.eTTestlinkModelsTransformService.jsonToTestPlanList(response.json()));
    }

    public getTestPlan(plan: TestPlanModel): Observable<TestPlanModel> {
        return this.getTestPlanByName(plan.name, plan.projectName);
    }

    public getTestPlanByName(planName: string, projectName: string): Observable<TestPlanModel> {
        let url: string = this.hostApi + '/testlink/project/' + projectName + '/plan/name/' + planName;
        return this.http.get(url)
            .map((response) => this.eTTestlinkModelsTransformService.jsonToTestPlanModel(response.json()));
    }

    public getTestPlanById(planId: number, projectId: number = 0): Observable<TestPlanModel> {
        let url: string = this.hostApi + '/testlink/project/' + 0 + '/plan/' + planId;
        return this.http.get(url)
            .map((response) => this.eTTestlinkModelsTransformService.jsonToTestPlanModel(response.json()));
    }

    public createTestPlan(plan: TestPlanModel): Observable<TestCaseModel> {
        let url: string = this.hostApi + '/testlink/project/' + plan.projectName + '/plan';
        return this.http.post(url, plan)
            .map((response) => response.json());
    }


    /************************/
    /******** Builds ********/
    /************************/

    public getPlanBuilds(plan: TestPlanModel): Observable<BuildModel[]> {
        let url: string = this.hostApi + '/testlink/project/' + plan.projectName + '/plan/' + plan.id + '/build';
        return this.http.get(url)
            .map((response) => this.eTTestlinkModelsTransformService.jsonToBuildList(response.json()));
    }

    public getLatestPlanBuild(plan: TestPlanModel): Observable<BuildModel> {
        let url: string = this.hostApi + '/testlink/project/' + plan.projectName + '/plan/' + plan.id + '/build/latestF';
        return this.http.get(url)
            .map((response) => this.eTTestlinkModelsTransformService.jsonToBuildModel(response.json()));
    }

    public createBuild(build: BuildModel): Observable<TestCaseModel> {
        let url: string = this.hostApi + '/testlink/project/' + 'dummyprojectId' + '/plan/' + build.testPlanId + '/build';
        return this.http.post(url, build)
            .map((response) => response.json());
    }
}