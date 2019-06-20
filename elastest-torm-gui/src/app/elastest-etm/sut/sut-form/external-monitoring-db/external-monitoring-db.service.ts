import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import { ConfigurationService } from '../../../../config/configuration-service.service';
import { ETModelsTransformServices } from '../../../../shared/services/et-models-transform.service';
import { ExternalElasticsearch } from '../../../external-monitoring-db/external-elasticsearch.model';
import { ExternalPrometheus } from '../../../external-monitoring-db/external-prometheus.model';

@Injectable()
export class ExternalMonitoringDBService {
  constructor(
    private http: HttpClient,
    private configurationService: ConfigurationService,
    private eTModelsTransformServices: ETModelsTransformServices,
  ) {}

  public getExternalElasticsearchById(id: number): Observable<ExternalElasticsearch> {
    let url: string = this.configurationService.configModel.hostApi + '/externalmonitoringdb/elasticsearch/' + id;
    return this.http.get(url).map((data: any) => {
      if (data !== undefined && data !== null) {
        return new ExternalElasticsearch(data);
      } else {
        throw new Error("Empty response. ExternalElasticsearch not exist or you don't have permissions to access it");
      }
    });
  }

  public getExternalPrometheusById(id: number): Observable<ExternalPrometheus> {
    let url: string = this.configurationService.configModel.hostApi + '/externalmonitoringdb/prometheus/' + id;
    return this.http.get(url).map((data: any) => {
      if (data !== undefined && data !== null) {
        return new ExternalPrometheus(data);
      } else {
        throw new Error("Empty response. ExternalPrometheus not exist or you don't have permissions to access it");
      }
    });
  }

  public checkExternalElasticsearchConnection(extES: ExternalElasticsearch): Observable<boolean> {
    let url: string = this.configurationService.configModel.hostApi + '/externalmonitoringdb/elasticsearch/connection';
    return this.http.post(url, extES, { observe: 'response' }).map((data: HttpResponse<boolean>) => {
      return data.body;
    });
  }

  public checkExternalPrometheusConnection(externalPrometheus: ExternalPrometheus): Observable<boolean> {
    let url: string = this.configurationService.configModel.hostApi + '/externalmonitoringdb/prometheus/connection';
    return this.http.post(url, externalPrometheus, { observe: 'response' }).map((data: HttpResponse<boolean>) => {
      return data.body;
    });
  }
}
