import { PullingObjectModel } from '../../shared/pulling-obj.model';
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
import { Subscription } from 'rxjs/Subscription';
import { Subject } from 'rxjs/Subject';
import { SutModel } from '../sut/sut-model';

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

  public getExternalProjectById(projectId: number | string): Observable<ExternalProjectModel> {
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
    tJob.exTJobExecs = undefined;
    let url: string = this.configurationService.configModel.hostApi + '/external/extjob';
    return this.http
      .put(url, tJob)
      .map((response: Response) => this.eTExternalModelsTransformService.jsonToExternalTJobModel(response.json()));
  }

  public createExternalTJob(tJob: ExternalTJobModel): Observable<ExternalTJobModel> {
    if (!tJob.hasSut()) {
      tJob.sut = undefined;
    }

    tJob.generateExecDashboardConfig();
    let url: string = this.configurationService.configModel.hostApi + '/external/extjob';
    return this.http
      .post(url, tJob)
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

  public getExternalTJobExecsByExternalTJobId(tJobId: string | number): Observable<ExternalTJobExecModel[]> {
    let url: string = this.hostApi + '/external/extjob/' + tJobId + '/tjobexec';
    return this.http
      .get(url)
      .map((response: Response) => this.eTExternalModelsTransformService.jsonToExternalTJobExecsList(response.json()));
  }

  public getExternalTJobExecById(tJobExecId: number | string): Observable<ExternalTJobExecModel> {
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

  public createExternalTJobExecutionByExTJobId(exTJobId: number | string): Observable<ExternalTJobExecModel> {
    let url: string = this.configurationService.configModel.hostApi + '/external/extjob/' + exTJobId + '/tjobexec';
    return this.http
      .post(url, {})
      .map((response: Response) => this.eTExternalModelsTransformService.jsonToExternalTJobExecModel(response.json()));
  }

  public modifyExternalTJobExec(tJobExec: ExternalTJobExecModel): Observable<ExternalTJobExecModel> {
    let url: string = this.configurationService.configModel.hostApi + '/external/tjobexec';
    return this.http
      .put(url, tJobExec)
      .map((response: Response) => this.eTExternalModelsTransformService.jsonToExternalTJobExecModel(response.json()));
  }

  public getExternalTJobExecutionFiles(tJobExecId: number): Observable<any> {
    let url: string = this.configurationService.configModel.hostApi + '/external/tjobexec/' + tJobExecId + '/files';
    return this.http.get(url).map((response) => response.json());
  }

  public getExternalTestExecsByExternalTJobExecId(exTJobExecId: string | number): Observable<ExternalTestExecutionModel[]> {
    let url: string = this.hostApi + '/external/tjobexec/' + exTJobExecId + '/testexec';
    return this.http
      .get(url)
      .map((response: Response) => this.eTExternalModelsTransformService.jsonToExternalTestExecsList(response.json()));
  }

  public checkTJobExecFinished(
    tJobExecId: string | number,
    timer: Observable<number>,
    subscription: Subscription,
  ): PullingObjectModel {
    let _obs: Subject<boolean> = new Subject<boolean>();
    let obs: Observable<boolean> = _obs.asObservable();

    timer = Observable.interval(2000);
    if (subscription === null || subscription === undefined) {
      subscription = timer.subscribe(() => {
        this.getExternalTJobExecById(tJobExecId).subscribe(
          (exec: ExternalTJobExecModel) => {
            if (exec.finished()) {
              if (subscription !== undefined) {
                subscription.unsubscribe();
                subscription = undefined;
                _obs.next(true);
              }
            }
          },
          (error) => console.log(error),
        );
      });
    }
    let responseObj: PullingObjectModel = new PullingObjectModel();
    responseObj.observable = obs;
    responseObj.subscription = subscription;
    return responseObj;
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

  public getExternalTestExecById(execId: number): Observable<ExternalTestExecutionModel> {
    let url: string = this.hostApi + '/external/testexec/' + execId;
    return this.http
      .get(url)
      .map((response: Response) => this.eTExternalModelsTransformService.jsonToExternalTestExecutionModel(response.json()));
  }

  public createExternalTestExecution(exec: ExternalTestExecutionModel): Observable<ExternalTestExecutionModel> {
    let url: string = this.configurationService.configModel.hostApi + '/external/testexec';
    return this.http
      .post(url, exec)
      .map((response: Response) => this.eTExternalModelsTransformService.jsonToExternalTestExecutionModel(response.json()));
  }

  public setExternalTJobExecToTestExecutionByExecutionId(
    execId: number | string,
    exTJobExecId: number | string,
  ): Observable<ExternalTestExecutionModel> {
    let url: string = this.hostApi + '/external/testexec/' + execId + '/tjobexec/' + exTJobExecId;
    return this.http
      .get(url)
      .map((response: Response) => this.eTExternalModelsTransformService.jsonToExternalTestExecutionModel(response.json()));
  }
}
