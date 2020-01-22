import { PullingObjectModel } from '../../shared/pulling-obj.model';
import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { ConfigurationService } from '../../config/configuration-service.service';
import { PopupService } from '../../shared/services/popup.service';
import { ETExternalModelsTransformService } from './et-external-models-transform.service';
import { Observable } from 'rxjs/Observable';
import { ExternalProjectModel } from './external-project/external-project-model';
import { ExternalTJobModel } from './external-tjob/external-tjob-model';
import { ExternalTJobExecModel, ExternalTJobExecFinishedModel } from './external-tjob-execution/external-tjob-execution-model';
import { ExternalTestCaseModel } from './external-test-case/external-test-case-model';
import { ExternalTestExecutionModel } from './external-test-execution/external-test-execution-model';
import { Subscription } from 'rxjs/Subscription';
import { Subject } from 'rxjs/Subject';
import { interval } from 'rxjs';
import { ETModelsTransformServices } from '../../shared/services/et-models-transform.service';
import { FileModel } from '../files-manager/file-model';

@Injectable()
export class ExternalService {
  hostApi: string;

  constructor(
    private http: HttpClient,
    private configurationService: ConfigurationService,
    public eTExternalModelsTransformService: ETExternalModelsTransformService,
    public eTModelsTransformServices: ETModelsTransformServices,
    public popupService: PopupService,
  ) {
    this.hostApi = this.configurationService.configModel.hostApi;
  }

  public dropAllExternalData(externalSystemId: string): Observable<boolean> {
    let url: string = this.hostApi + '/external/drop/' + externalSystemId;
    return this.http.delete(url, { observe: 'response' }).map((data: HttpResponse<boolean>) => {
      return data.body;
    });
  }

  /*************************/
  /******** Projects *******/
  /*************************/

  public getAllExternalProjects(): Observable<ExternalProjectModel[]> {
    let url: string = this.hostApi + '/external/project';
    return this.http.get(url).map((data: any[]) => this.eTExternalModelsTransformService.jsonToExternalProjectsList(data));
  }

  public getExternalProjectByType(project: ExternalProjectModel): Observable<ExternalProjectModel> {
    let url: string = this.hostApi + '/external/project/type/' + project.type;
    return this.http.get(url).map((data: any) => this.eTExternalModelsTransformService.jsonToExternalProjectModel(data));
  }

  public getExternalProjectById(projectId: number | string): Observable<ExternalProjectModel> {
    let url: string = this.hostApi + '/external/project/' + projectId;
    return this.http.get(url).map((data: any) => this.eTExternalModelsTransformService.jsonToExternalProjectModel(data));
  }

  // public createProject(project: ExternalProjectModel): Observable<ExternalProjectModel> {
  //     let url: string = this.configurationService.configModel.hostApi + '/external/project';
  //     return this.http.post(url, project, { observe: 'response' })
  //         .map((response: HttpResponse<any>) => response.body);
  // }

  /************************/
  /********* TJobs ********/
  /************************/

  public getAllExternalTJobs(): Observable<ExternalTJobModel[]> {
    let url: string = this.hostApi + '/external/extjob';
    return this.http.get(url).map((data: any[]) => this.eTExternalModelsTransformService.jsonToExternalTJobsList(data));
  }

  public getExternalTJobById(tJobId: number): Observable<ExternalTJobModel> {
    let url: string = this.hostApi + '/external/extjob/' + tJobId;
    return this.http.get(url).map((data: any) => this.eTExternalModelsTransformService.jsonToExternalTJobModel(data));
  }

  public modifyExternalTJob(tJob: ExternalTJobModel): Observable<ExternalTJobModel> {
    if (!tJob.hasSut()) {
      tJob.sut = undefined;
    }

    tJob.generateExecDashboardConfig();
    tJob.esmServicesString = JSON.stringify(tJob.esmServices);
    tJob.exTJobExecs = undefined;
    let url: string = this.configurationService.configModel.hostApi + '/external/extjob';
    return this.http.put(url, tJob).map((data: any) => this.eTExternalModelsTransformService.jsonToExternalTJobModel(data));
  }

  public createExternalTJob(tJob: ExternalTJobModel): Observable<ExternalTJobModel> {
    if (!tJob.hasSut()) {
      tJob.sut = undefined;
    }

    tJob.generateExecDashboardConfig();
    let url: string = this.configurationService.configModel.hostApi + '/external/extjob';
    return this.http
      .post(url, tJob, { observe: 'response' })
      .map((response: HttpResponse<any>) => this.eTExternalModelsTransformService.jsonToExternalTJobModel(response.body));
  }

  /*************************/
  /******* TJob Execs ******/
  /*************************/

  public getAllExternalTJobExecs(): Observable<ExternalTJobExecModel[]> {
    let url: string = this.hostApi + '/external/tjobexec';
    return this.http.get(url).map((data: any[]) => this.eTExternalModelsTransformService.jsonToExternalTJobExecsList(data));
  }

  public getExternalTJobExecsByExternalTJobId(tJobId: string | number): Observable<ExternalTJobExecModel[]> {
    let url: string = this.hostApi + '/external/extjob/' + tJobId + '/tjobexec';
    return this.http.get(url).map((data: any[]) => this.eTExternalModelsTransformService.jsonToExternalTJobExecsList(data));
  }

  public getExternalTJobExecById(tJobExecId: number | string): Observable<ExternalTJobExecModel> {
    let url: string = this.hostApi + '/external/tjobexec/' + tJobExecId;
    return this.http.get(url).map((data: any) => this.eTExternalModelsTransformService.jsonToExternalTJobExecModel(data));
  }

  public deleteExternalTJobExecById(tJobExecId: number | string): Observable<any> {
    let url: string = this.hostApi + '/external/tjobexec/' + tJobExecId;
    return this.http.delete(url);
  }

  public createExternalTJobExecution(exec: ExternalTJobExecModel): Observable<ExternalTJobExecModel> {
    let url: string = this.configurationService.configModel.hostApi + '/external/tjobexec';
    return this.http
      .post(url, exec, { observe: 'response' })
      .map((response: HttpResponse<any>) => this.eTExternalModelsTransformService.jsonToExternalTJobExecModel(response.body));
  }

  public createExternalTJobExecutionByExTJobId(
    exTJobId: number | string,
    extTJobExec?: ExternalTJobExecModel,
  ): Observable<ExternalTJobExecModel> {
    let url: string = this.configurationService.configModel.hostApi + '/external/extjob/' + exTJobId + '/tjobexec';
    return this.http
      .post(url, extTJobExec, { observe: 'response' })
      .map((response: HttpResponse<any>) => this.eTExternalModelsTransformService.jsonToExternalTJobExecModel(response.body));
  }

  public resumeExternalTJobExecution(exTJobExecId: number | string): Observable<ExternalTJobExecModel> {
    let url: string = this.configurationService.configModel.hostApi + '/external/tjobexec/resume/' + exTJobExecId;
    return this.http.get(url).map((data: any) => this.eTExternalModelsTransformService.jsonToExternalTJobExecModel(data));
  }

  public modifyExternalTJobExec(exec: ExternalTJobExecModel): Observable<ExternalTJobExecModel> {
    let url: string = this.configurationService.configModel.hostApi + '/external/tjobexec';
    return this.http.put(url, exec).map((data: any) => this.eTExternalModelsTransformService.jsonToExternalTJobExecModel(data));
  }

  public getExternalTJobExecutionFiles(tJobExecId: number): Observable<FileModel[]> {
    let url: string = this.configurationService.configModel.hostApi + '/external/tjobexec/' + tJobExecId + '/files';
    return this.http.get(url).map((data: any[]) => this.eTModelsTransformServices.jsonToFilesList(data));
  }

  public getExternalTestExecsByExternalTJobExecId(exTJobExecId: string | number): Observable<ExternalTestExecutionModel[]> {
    let url: string = this.hostApi + '/external/tjobexec/' + exTJobExecId + '/testexec';
    return this.http.get(url).map((data: any[]) => this.eTExternalModelsTransformService.jsonToExternalTestExecsList(data));
  }

  public checkTJobExecFinished(
    tJobExecId: string | number,
    timer: Observable<number>,
    subscription: Subscription,
  ): PullingObjectModel {
    let _obs: Subject<ExternalTJobExecFinishedModel> = new Subject<{
      finished: boolean;
      exec: ExternalTJobExecModel;
    }>();
    let obs: Observable<ExternalTJobExecFinishedModel> = _obs.asObservable();

    timer = interval(2000);
    if (subscription === null || subscription === undefined) {
      subscription = timer.subscribe(() => {
        this.getExternalTJobExecById(tJobExecId).subscribe(
          (exec: ExternalTJobExecModel) => {
            if (exec.finished()) {
              if (subscription !== undefined) {
                subscription.unsubscribe();
                subscription = undefined;
                _obs.next(new ExternalTJobExecFinishedModel(true, exec));
              } else {
                _obs.next(new ExternalTJobExecFinishedModel(false, exec));
              }
            } else {
              _obs.next(new ExternalTJobExecFinishedModel(false, exec));
            }
          },
          (error: Error) => console.log(error),
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
    return this.http.get(url).map((data: any[]) => this.eTExternalModelsTransformService.jsonToExternalTestCasesList(data));
  }

  public getExternalTestCaseById(caseId: number): Observable<ExternalTestCaseModel> {
    let url: string = this.hostApi + '/external/testcase/' + caseId;
    return this.http.get(url).map((data: any) => this.eTExternalModelsTransformService.jsonToExternalTestCaseModel(data));
  }

  /************************/
  /********* Execs ********/
  /************************/

  public getAllExternalTestExecs(): Observable<ExternalTestExecutionModel[]> {
    let url: string = this.hostApi + '/external/testexec';
    return this.http.get(url).map((data: any[]) => this.eTExternalModelsTransformService.jsonToExternalTestExecsList(data));
  }

  public getExternalTestExecById(execId: number): Observable<ExternalTestExecutionModel> {
    let url: string = this.hostApi + '/external/testexec/' + execId;
    return this.http.get(url).map((data: any) => this.eTExternalModelsTransformService.jsonToExternalTestExecutionModel(data));
  }

  public getExternalTestExecByExternalIdAndSystemId(
    externalId: string,
    externalSystemId: string,
  ): Observable<ExternalTestExecutionModel> {
    let url: string = this.hostApi + '/external/testexec/byexternal/' + externalSystemId + '/' + externalId;
    return this.http.get(url).map((data: any) => this.eTExternalModelsTransformService.jsonToExternalTestExecutionModel(data));
  }

  public createExternalTestExecution(exec: ExternalTestExecutionModel): Observable<ExternalTestExecutionModel> {
    let url: string = this.configurationService.configModel.hostApi + '/external/testexec';
    return this.http
      .post(url, exec, { observe: 'response' })
      .map((response: HttpResponse<any>) =>
        this.eTExternalModelsTransformService.jsonToExternalTestExecutionModel(response.body),
      );
  }

  public modifyExternalTestExecution(exec: ExternalTestExecutionModel): Observable<ExternalTestExecutionModel> {
    let url: string = this.configurationService.configModel.hostApi + '/external/testexec';
    return this.http
      .put(url, exec)
      .map((data: any) => this.eTExternalModelsTransformService.jsonToExternalTestExecutionModel(data));
  }

  public setExternalTJobExecToTestExecutionByExecutionId(
    execId: number | string,
    exTJobExecId: number | string,
  ): Observable<ExternalTestExecutionModel> {
    let url: string = this.hostApi + '/external/testexec/' + execId + '/tjobexec/' + exTJobExecId;
    return this.http.get(url).map((data: any) => this.eTExternalModelsTransformService.jsonToExternalTestExecutionModel(data));
  }
}
