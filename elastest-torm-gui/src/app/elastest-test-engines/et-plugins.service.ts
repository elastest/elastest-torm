import { EtPluginModel } from './et-plugin-model';
import { ConfigurationService } from '../config/configuration-service.service';
import { Http, Response } from '@angular/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';

@Injectable()
export class EtPluginsService {
  mainUrl: string = this.configurationService.configModel.hostApi + '/etplugins/';

  constructor(private http: Http, private configurationService: ConfigurationService) {}

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
    return testEngine;
  }

  /* *** API *** */
  getEtPlugins(): Observable<EtPluginModel[]> {
    return this.http.get(this.mainUrl).map((response: Response) => this.transformRawTestEnginesList(response.json()));
  }

  getEtPlugin(name: string): Observable<EtPluginModel> {
    let url: string = this.mainUrl + name;
    return this.http.get(url).map((response: Response) => this.transformRawTestEngine(response.json()));
  }

  startEtPlugin(etPluginModel: EtPluginModel): Observable<EtPluginModel> {
    let url: string = this.mainUrl + etPluginModel.name + '/start';
    return this.http.post(url, undefined).map((response: Response) => this.transformRawTestEngine(response.json()));
  }

  startEtPluginAsync(etPluginModel: EtPluginModel): Observable<EtPluginModel> {
    let url: string = this.mainUrl + etPluginModel.name + '/start/async';
    return this.http.post(url, undefined).map((response: Response) => this.transformRawTestEngine(response.json()));
  }

  stopEtPlugin(etPluginModel: EtPluginModel): Observable<EtPluginModel> {
    let url: string = this.mainUrl + etPluginModel.name;
    return this.http.delete(url).map((response: Response) => this.transformRawTestEngine(response.json()));
  }

  isStarted(etPluginModel: EtPluginModel): Observable<boolean> {
    let url: string = this.mainUrl + etPluginModel.name + '/started';
    return this.http.get(url).map((response: Response) => response.json());
  }

  isWorking(etPluginModel: EtPluginModel) {
    let url: string = this.mainUrl + etPluginModel.name + '/working';
    return this.http.get(url).map((response: Response) => response.json());
  }

  getUrl(name: string) {
    let url: string = this.mainUrl + name + '/url';
    return this.http.get(url).map((response: Response) => response['_body']);
  }

  /* **************************************************************** */
  /* *********************** SPECIFIC METHODS *********************** */
  /* **************************************************************** */
  getUniqueEtPlugins(): Observable<EtPluginModel[]> {
    return this.http.get(this.mainUrl + 'unique').map((response: Response) => this.transformRawTestEnginesList(response.json()));
  }

  getUniqueEtPlugin(name: string): Observable<EtPluginModel> {
    let url: string = this.mainUrl + 'unique/' + name;
    return this.http.get(url).map((response: Response) => this.transformRawTestEngine(response.json()));
  }
}
