import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { ConfigurationService } from '../../../config/configuration-service.service';
import { Observable } from 'rxjs';

@Injectable()
export class ManageClustersService {
  constructor(private http: HttpClient, private configurationService: ConfigurationService) {}

  public uploadClusterTarFile(file: File): Observable<string> {
    let url: string = this.configurationService.configModel.hostApi + '/epm/cluster/file';

    let formData: FormData = new FormData();
    formData.append('file', file);

    // Returns text/plain
    return this.http.post(url, formData, { responseType: 'text' });
  }

  public uploadNodeTarFile(file: File): Observable<string> {
    let url: string = this.configurationService.configModel.hostApi + '/epm/node/file';

    let formData: FormData = new FormData();
    formData.append('file', file);

    // Returns text/plain
    return this.http.post(url, formData, { responseType: 'text' });
  }

  /* ******************************************* */
  /* ***************** Cluster ***************** */
  /* ******************************************* */

  public createCluster(): Observable<any> {
    let url: string = this.configurationService.configModel.hostApi + '/epm/cluster';
    return this.http.post(url, { observe: 'response' }).map((response: HttpResponse<any>) => response.body);
  }

  public deleteCluster(): Observable<any> {
    let url: string = this.configurationService.configModel.hostApi + '/epm/cluster';
    return this.http.delete(url, { observe: 'response' }).map((response: HttpResponse<any>) => response.body);
  }

  public getAllClusters(): Observable<any[]> {
    let url: string = this.configurationService.configModel.hostApi + '/epm/cluster';
    return this.http.get(url).map((data: any[]) => data);
  }

  public getCluster(clusterId: string): Observable<any> {
    let url: string = this.configurationService.configModel.hostApi + '/epm/cluster/' + clusterId;
    return this.http.get(url).map((data: any) => data);
  }

  /* ***************************************** */
  /* ***************** Nodes ***************** */
  /* ***************************************** */

  public createNode(clusterId: string): Observable<any> {
    let url: string = this.configurationService.configModel.hostApi + '/epm/cluster/' + clusterId + '/node';
    return this.http.post(url, { observe: 'response' }).map((response: HttpResponse<any>) => response.body);
  }

  public deleteNode(clusterId: string, nodeId: string): Observable<any> {
    let url: string = this.configurationService.configModel.hostApi + '/epm/cluster/' + clusterId + '/node/' + nodeId;
    return this.http.delete(url, { observe: 'response' }).map((response: HttpResponse<any>) => response.body);
  }

  public getAllNodes(clusterId: string): Observable<any[]> {
    let url: string = this.configurationService.configModel.hostApi + '/epm/cluster/' + clusterId + '/node';
    return this.http.get(url).map((data: any[]) => data);
  }

  public getNode(clusterId: string, nodeId: string): Observable<any> {
    let url: string = this.configurationService.configModel.hostApi + '/epm/cluster/' + clusterId + '/node/' + nodeId;
    return this.http.get(url).map((data: any) => data);
  }
}
