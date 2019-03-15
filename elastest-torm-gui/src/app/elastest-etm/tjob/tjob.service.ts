import { ETModelsTransformServices } from '../../shared/services/et-models-transform.service';
import { ConfigurationService } from '../../config/configuration-service.service';
import { TJobExecModel } from '../tjob-exec/tjobExec-model';
import { TJobModel } from './tjob-model';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import 'rxjs/Rx';
import { Observable } from 'rxjs/Rx';
import { backendViewType } from '../types/backend-view.type';

@Injectable()
export class TJobService {
  constructor(
    private http: HttpClient,
    private configurationService: ConfigurationService,
    private eTModelsTransformServices: ETModelsTransformServices,
  ) {}

  public getTJobs(viewType: backendViewType = 'complete'): Observable<TJobModel[]> {
    let url: string = this.configurationService.configModel.hostApi + '/tjob';
    // + '?viewType=' + viewType;
    return this.http.get(url).map((data: any[]) => this.eTModelsTransformServices.jsonToTJobsList(data));
  }

  public getTJob(id: string, viewType: backendViewType = 'complete'): Observable<TJobModel> {
    let url: string = this.configurationService.configModel.hostApi + '/tjob/' + id + '?viewType=' + viewType;
    return this.http.get(url).map((data: any) => {
      if (data !== undefined && data !== null) {
        return this.eTModelsTransformServices.jsonToTJobModel(data);
      } else {
        throw new Error("Empty response. TJob not exist or you don't have permissions to access it");
      }
    });
  }

  public createTJob(tJob: TJobModel, action: string = 'new'): Observable<any> {
    if (!tJob.hasSut()) {
      tJob.sut = undefined;
    }
    tJob.generateExecDashboardConfig();
    tJob.esmServicesString = JSON.stringify(tJob.esmServices);
    let url: string = this.configurationService.configModel.hostApi + '/tjob';
    if (action === 'new') {
      return this.http.post(url, tJob, { observe: 'response' }).map((response: HttpResponse<any>) => response.body);
    } else {
      return this.http.put(url, tJob, { observe: 'response' }).map((response: HttpResponse<any>) => response.body);
    }
  }

  public modifyTJob(tJob: TJobModel): Observable<any> {
    return this.createTJob(tJob, 'edit');
  }

  public duplicateTJob(tJob: TJobModel): Observable<any> {
    let newTJob: TJobModel = new TJobModel(tJob);
    newTJob.id = 0;
    newTJob.tjobExecs = [];
    return this.createTJob(newTJob);
  }

  public deleteTJob(tJob: TJobModel): Observable<any> {
    let url: string = this.configurationService.configModel.hostApi + '/tjob/' + tJob.id;
    return this.http.delete(url);
  }

  /***** Others *****/

  public getTJobExecsList(tJob: TJobModel): TJobExecModel[] {
    return this.eTModelsTransformServices.jsonToTJobExecsList(tJob.tjobExecs);
  }
}
