import { Injectable } from '@angular/core';
import { Http, Response } from '@angular/http';
import { ConfigurationService } from '../../config/configuration-service.service';
import { PopupService } from '../../shared/services/popup.service';
import { ETExternalModelsTransformService } from './et-external-models-transform.service';
import { Observable } from 'rxjs/Observable';
import { ExternalProjectModel } from './external-project/external-project-model';
import { ExternalTJobModel } from './external-tjob/external-tjob-model';
import { ExternalTJobExecModel } from './external-tjob-execution/external-tjob-execution-model';
import { ExternalTestCaseModel } from './external-test-case/external-test-case-model';
import { ExternalTestExecutionModel } from './external-test-execution/external-test-execution-model';

@Injectable()
export class ExternalService {
  hostApi: string;

  constructor(
    private http: Http,
    private configurationService: ConfigurationService,
    public eTExternalModelsTransformService: ETExternalModelsTransformService,
    public popupService: PopupService,
  ) {
    this.hostApi = this.configurationService.configModel.hostApi;
  }

  /*************************/
  /******** Projects *******/
  /*************************/

  public getAllExternalProjects(): Observable<ExternalProjectModel[]> {
    let url: string = this.hostApi + '/external/project';
    return this.http
      .get(url)
      .map((response: Response) => this.eTExternalModelsTransformService.jsonToExternalProjectsList(response.json()));
  }

  public getExternalProjectByType(project: ExternalProjectModel): Observable<ExternalProjectModel> {
    let url: string = this.hostApi + '/external/project/type/' + project.type;
    return this.http
      .get(url)
      .map((response: Response) => this.eTExternalModelsTransformService.jsonToExternalProjectModel(response.json()));
  }

  public getExternalProjectById(projectId: number): Observable<ExternalProjectModel> {
    let url: string = this.hostApi + '/external/project/' + projectId;
    return this.http
      .get(url)
      .map((response: Response) => this.eTExternalModelsTransformService.jsonToExternalProjectModel(response.json()));
  }

  // public createProject(project: ExternalProjectModel): Observable<ExternalProjectModel> {
  //     let url: string = this.configurationService.configModel.hostApi + '/external/project';
  //     return this.http.post(url, project)
  //         .map((response: Response) => response.json());
  // }

  /************************/
  /********* TJobs ********/
  /************************/

  public getAllExternalTJobs(): Observable<ExternalTJobModel[]> {
    let url: string = this.hostApi + '/external/extjob';
    return this.http
      .get(url)
      .map((response: Response) => this.eTExternalModelsTransformService.jsonToExternalTJobsList(response.json()));
  }

  public getExternalTJobById(tJobId: number): Observable<ExternalTJobModel> {
    let url: string = this.hostApi + '/external/extjob/' + tJobId;
    return this.http
      .get(url)
      .map((response: Response) => this.eTExternalModelsTransformService.jsonToExternalTJobModel(response.json()));
  }

  public modifyExternalTJob(tJob: ExternalTJobModel): Observable<ExternalTJobModel> {
    if (!tJob.hasSut()) {
      tJob.sut = undefined;
    }
    tJob.generateExecDashboardConfig();
    let url: string = this.configurationService.configModel.hostApi + '/external/extjob';
    return this.http
      .put(url, tJob)
      .map((response: Response) => this.eTExternalModelsTransformService.jsonToExternalTJobModel(response.json()));
  }

  /*************************/
  /******* TJob Execs ******/
  /*************************/

  public getAllExternalTJobExecs(): Observable<ExternalTJobExecModel[]> {
    let url: string = this.hostApi + '/external/tjobexec';
    return this.http
      .get(url)
      .map((response: Response) => this.eTExternalModelsTransformService.jsonToExternalTJobExecsList(response.json()));
  }

  public getExternalTJobExecById(tJobExecId: number): Observable<ExternalTJobExecModel> {
    let url: string = this.hostApi + '/external/tjobexec/' + tJobExecId;
    return this.http
      .get(url)
      .map((response: Response) => this.eTExternalModelsTransformService.jsonToExternalTJobExecModel(response.json()));
  }

  public createExternalTJobExecution(exec: ExternalTJobExecModel): Observable<ExternalTJobExecModel> {
    let url: string = this.configurationService.configModel.hostApi + '/external/tjobexec';
    return this.http
      .post(url, exec)
      .map((response: Response) => this.eTExternalModelsTransformService.jsonToExternalTJobExecModel(response.json()));
  }

  /************************/
  /********* Cases ********/
  /************************/

  public getAllExternalTestCases(): Observable<ExternalTestCaseModel[]> {
    let url: string = this.hostApi + '/external/testcase';
    return this.http
      .get(url)
      .map((response: Response) => this.eTExternalModelsTransformService.jsonToExternalTestCasesList(response.json()));
  }

  public getExternalTestCaseById(caseId: number): Observable<ExternalTestCaseModel> {
    let url: string = this.hostApi + '/external/testcase/' + caseId;
    return this.http
      .get(url)
      .map((response: Response) => this.eTExternalModelsTransformService.jsonToExternalTestCaseModel(response.json()));
  }

  /************************/
  /********* Execs ********/
  /************************/

  public getAllExternalTestExecs(): Observable<ExternalTestExecutionModel[]> {
    let url: string = this.hostApi + '/external/testexec';
    return this.http
      .get(url)
      .map((response: Response) => this.eTExternalModelsTransformService.jsonToExternalTestExecsList(response.json()));
  }

  public getExternalTestExecById(caseId: number): Observable<ExternalTestExecutionModel> {
    let url: string = this.hostApi + '/external/testexec/' + caseId;
    return this.http
      .get(url)
      .map((response: Response) => this.eTExternalModelsTransformService.jsonToExternalTestExecutionModel(response.json()));
  }
}
