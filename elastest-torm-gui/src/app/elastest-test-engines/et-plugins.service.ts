import { EtPluginModel } from './et-plugin-model';
import { ConfigurationService } from '../config/configuration-service.service';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import { HttpClient, HttpResponse } from '@angular/common/http';

@Injectable()
export class EtPluginsService {
  mainUrl: string = this.configurationService.configModel.hostApi + '/etplugins/';

  constructor(private http: HttpClient, private configurationService: ConfigurationService) {}

  public transformRawTestEnginesList(testEnginesRaw: any[]): EtPluginModel[] {
    let testEngines: EtPluginModel[] = [];
    for (let rawEngine of testEnginesRaw) {
      testEngines.push(this.transformRawTestEngine(rawEngine));
    }
    return testEngines;
  }

  public transformRawTestEngine(rawEngine: any): EtPluginModel {
    let testEngine: EtPluginModel = new EtPluginModel();
    testEngine.name = rawEngine.name;
    testEngine.url = rawEngine.url;
    testEngine.imagesList = rawEngine.imagesList;
    testEngine.status = rawEngine.status;
    testEngine.statusMsg = rawEngine.statusMsg;
    testEngine.user = rawEngine.user;
    testEngine.pass = rawEngine.pass;
    testEngine.displayName = rawEngine.displayName;
    return testEngine;
  }

  /* *** API *** */
  getEtPlugins(): Observable<EtPluginModel[]> {
    return this.http.get(this.mainUrl).map((data: any[]) => this.transformRawTestEnginesList(data));
  }

  getEtPlugin(name: string): Observable<EtPluginModel> {
    let url: string = this.mainUrl + name;
    return this.http.get(url).map((data: any) => this.transformRawTestEngine(data));
  }

  startEtPlugin(etPluginModel: EtPluginModel): Observable<EtPluginModel> {
    let url: string = this.mainUrl + etPluginModel.name + '/start';
    return this.http.post(url, undefined).map((response: HttpResponse<any>) => this.transformRawTestEngine(response.body));
  }

  startEtPluginAsync(etPluginModel: EtPluginModel): Observable<EtPluginModel> {
    let url: string = this.mainUrl + etPluginModel.name + '/start/async';
    return this.http.post(url, undefined).map((response: HttpResponse<any>) => this.transformRawTestEngine(response.body));
  }

  stopEtPlugin(etPluginModel: EtPluginModel): Observable<EtPluginModel> {
    let url: string = this.mainUrl + etPluginModel.name;
    return this.http.delete(url).map((data: any) => this.transformRawTestEngine(data));
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
    return this.http.get(url).map((data: string) => data);
  }

  /* **************************************************************** */
  /* *********************** SPECIFIC METHODS *********************** */
  /* **************************************************************** */
  getUniqueEtPlugins(): Observable<EtPluginModel[]> {
    return this.http.get(this.mainUrl + 'unique').map((data: any[]) => this.transformRawTestEnginesList(data));
  }

  getUniqueEtPlugin(name: string): Observable<EtPluginModel> {
    let url: string = this.mainUrl + 'unique/' + name;
    return this.http.get(url).map((data: any[]) => this.transformRawTestEngine(data));
  }
}
