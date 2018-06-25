import { Observable } from 'rxjs/Rx';
import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
import { ConfigurationService } from '../../config/configuration-service.service';
import { MonitoringQueryModel } from '../monitoring-query.model';
@Injectable()
export class MonitoringService {
  etmApiUrl: string;
  constructor(public http: Http, private configurationService: ConfigurationService) {
    this.etmApiUrl = this.configurationService.configModel.hostApi;
  }

  public searchLogs(query: MonitoringQueryModel): Observable<any> {
    let url: string = this.etmApiUrl + '/monitoring/log';
    return this.http.post(url, query).map((response) => response.json());
  }

  public searchPreviousLogs(query: MonitoringQueryModel): Observable<any> {
    let url: string = this.etmApiUrl + '/monitoring/log/previous';
    return this.http.post(url, query).map((response) => response.json());
  }

  public searchLastLogs(query: MonitoringQueryModel, size: number): Observable<any> {
    let url: string = this.etmApiUrl + '/monitoring/log/last/' + size;
    return this.http.post(url, query).map((response) => response.json());
  }
}
