import { TestCaseModel } from './models/test-case-model';
import { TestSuiteModel } from './models/test-suite-model';
import { TestPlanModel } from './models/test-plan-model';
import { Injectable } from '@angular/core';
import { ETTestlinkModelsTransformService } from '../shared/services/et-testlink-models-transform.service';
import { Observable } from 'rxjs/Rx';
import { ConfigurationService } from '../config/configuration-service.service';
import { Http } from '@angular/http';
import { TestProjectModel } from './models/test-project-model';
@Injectable()
export class TestLinkService {

    hostApi: string;

    constructor(
        private http: Http, private configurationService: ConfigurationService,
        private eTTestlinkModelsTransformService: ETTestlinkModelsTransformService,
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
        let url: string = this.hostApi + '/testlink/project/' + project.name;
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
        let url: string = this.hostApi + '/testlink/project/' + suite.testProjectId + '/suite/' + suite.id;
        return this.http.get(url)
            .map((response) => this.eTTestlinkModelsTransformService.jsonToTestSuiteModel(response.json()));
    }

    public createTestSuite(suite: TestSuiteModel): Observable<TestSuiteModel> {
        let url: string = this.hostApi + '/testlink/project/' + suite.testProjectId + '/suite';
        return this.http.post(url, suite)
            .map((response) => response.json());
    }

    /************************/
    /********* Cases ********/
    /************************/

    public getSuiteTestCases(suite: TestSuiteModel): Observable<TestCaseModel[]> {
        let url: string = this.hostApi + '/testlink/project/' + suite.testProjectId + '/suite/' + suite.id + '/case';
        return this.http.get(url)
            .map((response) => this.eTTestlinkModelsTransformService.jsonToTestCasesList(response.json()));
    }

    public getTestCase(testCase: TestCaseModel): Observable<TestCaseModel> {
        let url: string = this.hostApi + '/testlink/project/' + testCase.testProjectId + '/suite/' + testCase.testSuiteId + '/case/' + testCase.id;
        return this.http.get(url)
            .map((response) => this.eTTestlinkModelsTransformService.jsonToTestCaseModel(response.json()));
    }

    public createTestCase(testCase: TestCaseModel): Observable<TestCaseModel> {
        let url: string = this.hostApi + '/testlink/project/' + testCase.testProjectId + '/suite/' + testCase.testSuiteId + '/case';
        return this.http.post(url, testCase)
            .map((response) => response.json());
    }

}