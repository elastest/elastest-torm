import { ETModelsTransformServices } from '../../shared/services/et-models-transform.service';
import { SutModel } from './sut-model';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import { ConfigurationService } from '../../config/configuration-service.service';
import 'rxjs/Rx';
import { ExternalElasticsearch } from './external-elasticsearch.model';

@Injectable()
export class SutService {
  constructor(
    private http: HttpClient,
    private configurationService: ConfigurationService,
    private eTModelsTransformServices: ETModelsTransformServices,
  ) {}

  public getSuts(): Observable<SutModel[]> {
    let url: string = this.configurationService.configModel.hostApi + '/sut';
    return this.http.get(url).map((data: any[]) => this.eTModelsTransformServices.jsonToSutsList(data));
  }

  public getSut(id: number): Observable<SutModel> {
    let url: string = this.configurationService.configModel.hostApi + '/sut/' + id;
    return this.http.get(url).map((data: any) => {
      if (data !== undefined && data !== null) {
        return this.eTModelsTransformServices.jsonToSutModel(data);
      } else {
        throw new Error("Empty response. SuT not exist or you don't have permissions to access it");
      }
    });
  }

  public createSut(sut: SutModel): Observable<SutModel> {
    let url: string = this.configurationService.configModel.hostApi + '/sut';
    return this.http
      .post(url, sut, { observe: 'response' })
      .map((response: HttpResponse<any>) => this.eTModelsTransformServices.jsonToSutModel(response.body));
  }

  public modifySut(): void {
    // TODO
  }

  public deleteSut(sut: SutModel): Observable<any> {
    let url: string = this.configurationService.configModel.hostApi + '/sut/' + sut.id;
    return this.http.delete(url);
  }

  public getLogstashInfo(): any {
    return this.configurationService.getLogstashInfo();
  }

  public duplicateSut(sut: SutModel): Observable<SutModel> {
    let url: string = this.configurationService.configModel.hostApi + '/sut/' + sut.id + '/duplicate';
    return this.http
      .post(url, sut, { observe: 'response' })
      .map((response: HttpResponse<any>) => this.eTModelsTransformServices.jsonToSutModel(response.body));
  }

  public checkExternalElasticsearchConnection(extES: ExternalElasticsearch): Observable<boolean> {
    let url: string = this.configurationService.configModel.hostApi + '/sut/externalelasticsearch/connection';
    return this.http.post(url, extES, { observe: 'response' }).map((data: HttpResponse<boolean>) => {
      return data.body;
    });
  }
}
