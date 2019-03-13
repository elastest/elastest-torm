import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { ConfigurationService } from '../../../config/configuration-service.service';
import { Observable } from 'rxjs';

@Injectable()
export class ElasticsearchApiService {
  constructor(private http: HttpClient, private configurationService: ConfigurationService) {}

  public getAllIndices(): Observable<string[]> {
    let url: string = this.configurationService.configModel.hostApi + '/elasticsearch/index';
    return this.http.get(url).map((data: string[]) => data);
  }

  public getIndicesByHealth(health: ESHealthStatus): Observable<string[]> {
    let url: string = this.configurationService.configModel.hostApi + '/elasticsearch/index/byhealth/' + health;
    return this.http.get(url).map((data: string[]) => data);
  }

  public getRedIndices(): Observable<string[]> {
    return this.getIndicesByHealth('red');
  }

  public deleteIndicesByHealth(health: ESHealthStatus): Observable<boolean> {
    let url: string = this.configurationService.configModel.hostApi + '/elasticsearch/index/byhealth/' + health;
    return this.http.delete(url, { observe: 'response' }).map((data: HttpResponse<boolean>) => {
      return data.body;
    });
  }

  public deleteRedIndices(): Observable<boolean> {
    return this.deleteIndicesByHealth('red');
  }
}

export type ESHealthStatus = 'red' | 'yellow' | 'green';
