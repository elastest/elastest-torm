import { ETModelsTransformServices } from '../../shared/services/et-models-transform.service';
import { ConfigurationService } from '../../config/configuration-service.service';
import { TJobExecModel } from '../tjob-exec/tjobExec-model';
import { TJobModel } from './tjob-model';
import { Http } from '@angular/http';
import { Injectable } from '@angular/core';
import 'rxjs/Rx';
import { Observable } from 'rxjs/Rx';

@Injectable()
export class TJobService {
  constructor(
    private http: Http,
    private configurationService: ConfigurationService,
    private eTModelsTransformServices: ETModelsTransformServices,
  ) {}

  public getTJobs(): Observable<TJobModel[]> {
    let url: string = this.configurationService.configModel.hostApi + '/tjob';
    return this.http.get(url).map((response) => this.eTModelsTransformServices.jsonToTJobsList(response.json()));
  }

  public getTJob(id: string): Observable<TJobModel> {
    let url: string = this.configurationService.configModel.hostApi + '/tjob/' + id;
    return this.http.get(url).map((response) => {
      let data: any = response.json();
      if (data !== undefined && data !== null) {
        return this.eTModelsTransformServices.jsonToTJobModel(data);
      } else {
        throw new Error("Empty response. TJob not exist or you don't have permissions to access it");
      }
    });
  }

  public createTJob(tJob: TJobModel, action: string): Observable<any> {
    if (!tJob.hasSut()) {
      tJob.sut = undefined;
    }
    tJob.generateExecDashboardConfig();
    tJob.esmServicesString = JSON.stringify(tJob.esmServices);
    let url: string = this.configurationService.configModel.hostApi + '/tjob';
    if (action === 'new') {
      return this.http.post(url, tJob).map((response) => response.json());
    } else {
      return this.http.put(url, tJob).map((response) => response.json());
    }
  }

  public modifyTJob(tJob: TJobModel): Observable<any> {
    return this.createTJob(tJob, 'edit');
  }

  public deleteTJob(tJob: TJobModel): Observable<any> {
    let url: string = this.configurationService.configModel.hostApi + '/tjob/' + tJob.id;
    return this.http.delete(url).map((response) => response.json());
  }

  /***** Others *****/

  public getTJobExecsList(tJob: TJobModel): TJobExecModel[] {
    return this.eTModelsTransformServices.jsonToTJobExecsList(tJob.tjobExecs);
  }
}
