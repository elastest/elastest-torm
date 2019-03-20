import { EtPluginModel } from './et-plugin-model';
import { ConfigurationService } from '../config/configuration-service.service';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import { HttpClient, HttpResponse } from '@angular/common/http';

@Injectable()
export class EtPluginsService {
  mainUrl: string = this.configurationService.configModel.hostApi + '/etplugins/';

  constructor(private http: HttpClient, private configurationService: ConfigurationService) {}

  public transformRawEtPluginsList(etPluginsRaw: any[]): EtPluginModel[] {
    let etPlugins: EtPluginModel[] = [];
    for (let rawEngine of etPluginsRaw) {
      etPlugins.push(this.transformRawEtPlugin(rawEngine));
    }
    return etPlugins;
  }

  public transformRawEtPlugin(rawEngine: any): EtPluginModel {
    let etPlugin: EtPluginModel = new EtPluginModel();
    etPlugin.name = rawEngine.name;
    etPlugin.url = rawEngine.url;
    etPlugin.imagesList = rawEngine.imagesList;
    etPlugin.status = rawEngine.status;
    etPlugin.statusMsg = rawEngine.statusMsg;
    etPlugin.user = rawEngine.user;
    etPlugin.pass = rawEngine.pass;
    etPlugin.displayName = rawEngine.displayName;
    etPlugin.fileName = rawEngine.fileName;
    return etPlugin;
  }

  /* *** API *** */
  getEtPlugins(): Observable<EtPluginModel[]> {
    return this.http.get(this.mainUrl).map((data: any[]) => this.transformRawEtPluginsList(data));
  }

  getEtPlugin(name: string): Observable<EtPluginModel> {
    let url: string = this.mainUrl + name;
    return this.http.get(url).map((data: any) => this.transformRawEtPlugin(data));
  }

  startEtPlugin(etPluginModel: EtPluginModel): Observable<EtPluginModel> {
    let url: string = this.mainUrl + etPluginModel.name + '/start';
    return this.http
      .post(url, undefined, { observe: 'response' })
      .map((response: HttpResponse<any>) => this.transformRawEtPlugin(response.body));
  }

  startEtPluginAsync(etPluginModel: EtPluginModel): Observable<EtPluginModel> {
    let url: string = this.mainUrl + etPluginModel.name + '/start/async';
    return this.http
      .post(url, undefined, { observe: 'response' })
      .map((response: HttpResponse<any>) => this.transformRawEtPlugin(response.body));
  }

  stopEtPlugin(etPluginModel: EtPluginModel): Observable<EtPluginModel> {
    let url: string = this.mainUrl + etPluginModel.name;
    return this.http.delete(url).map((data: any) => this.transformRawEtPlugin(data));
  }

  isStarted(etPluginModel: EtPluginModel): Observable<boolean> {
    let url: string = this.mainUrl + etPluginModel.name + '/started';
    return this.http.get(url).map((data: boolean) => data);
  }

  isWorking(etPluginModel: EtPluginModel): Observable<boolean> {
    let url: string = this.mainUrl + etPluginModel.name + '/working';
    return this.http.get(url).map((data: boolean) => data);
  }

  getUrl(name: string): Observable<string> {
    let url: string = this.mainUrl + name + '/url';
    return this.http.get(url, { responseType: 'text' }).map((data: string) => data);
  }

  /* **************************************************************** */
  /* *********************** SPECIFIC METHODS *********************** */
  /* **************************************************************** */
  getUniqueEtPlugins(): Observable<EtPluginModel[]> {
    return this.http.get(this.mainUrl + 'unique').map((data: any[]) => this.transformRawEtPluginsList(data));
  }

  getUniqueEtPlugin(name: string): Observable<EtPluginModel> {
    let url: string = this.mainUrl + 'unique/' + name;
    return this.http.get(url).map((data: any[]) => this.transformRawEtPlugin(data));
  }
}
