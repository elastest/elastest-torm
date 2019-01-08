import { ETModelsTransformServices } from '../../shared/services/et-models-transform.service';
import { SutModel } from '../sut/sut-model';
import { SutExecModel } from './sutExec-model';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ConfigurationService } from '../../config/configuration-service.service';
import 'rxjs/Rx';
import { Observable } from 'rxjs/Rx';

@Injectable()
export class SutExecService {
  constructor(
    private http: HttpClient,
    private configurationService: ConfigurationService,
    private eTModelsTransformServices: ETModelsTransformServices,
  ) {}

  public deploySut(sutId: number): Observable<any> {
    let url: string = this.configurationService.configModel.hostApi + '/sut/' + sutId + '/exec';
    return this.http.post(url, {}).map((response: HttpResponse<any>) => response.body);
  }

  public getSutsExecutions(sut: SutModel): Observable<SutExecModel[]> {
    let url: string = this.configurationService.configModel.hostApi + '/sut/' + sut.id + '/exec';
    return this.http.get(url).map((data: any[]) => this.eTModelsTransformServices.jsonToSutExecsList(data));
  }

  public getSutExecution(sut: SutModel, idSutExecution: number): Observable<SutExecModel> {
    let url: string = this.configurationService.configModel.hostApi + '/sut/' + sut.id + '/exec/' + idSutExecution;
    return this.http.get(url).map((data: any) => {
      if (data !== undefined && data !== null) {
        return this.eTModelsTransformServices.jsonToSutExecModel(data);
      } else {
        throw new Error("Empty response. SuT Execution not exist or you don't have permissions to access it");
      }
    });
  }

  public deleteSutExecution(sut: SutModel, sutExecution: SutExecModel) {
    let url: string = this.configurationService.configModel.hostApi + '/sut/' + sut.id + '/exec/' + sutExecution.id;
    return this.http.delete(url);
  }
}
