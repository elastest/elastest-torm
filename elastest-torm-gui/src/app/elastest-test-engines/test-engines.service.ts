import { TestEngineModel } from './test-engine-model';
import { ConfigurationService } from '../config/configuration-service.service';
import { Http, Response } from '@angular/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';

@Injectable()
export class TestEnginesService {
  mainUrl: string = this.configurationService.configModel.hostApi + '/engines/';

  constructor(private http: Http, private configurationService: ConfigurationService) {}

  transformRawTestEnginesList(testEnginesRaw: any[]): TestEngineModel[] {
    let testEngines: TestEngineModel[] = [];
    for (let rawEngine of testEnginesRaw) {
      testEngines.push(this.transformRawTestEngine(rawEngine));
    }
    return testEngines;
  }

  transformRawTestEngine(rawEngine: any): TestEngineModel {
    let testEngine: TestEngineModel = new TestEngineModel();
    testEngine.name = rawEngine.name;
    testEngine.url = rawEngine.url;
    testEngine.imagesList = rawEngine.imagesList;
    testEngine.status = rawEngine.status;
    testEngine.statusMsg = rawEngine.statusMsg;
    return testEngine;
  }

  /* *** API *** */
  getTestEngines(): Observable<TestEngineModel[]> {
    return this.http.get(this.mainUrl).map((response: Response) => this.transformRawTestEnginesList(response.json()));
  }

  getEngine(name: string): Observable<TestEngineModel> {
    let url: string = this.mainUrl + name;
    return this.http.get(url).map((response: Response) => this.transformRawTestEngine(response.json()));
  }

  startTestEngine(testEngineModel: TestEngineModel): Observable<TestEngineModel> {
    let url: string = this.mainUrl + testEngineModel.name + '/start';
    return this.http.post(url, undefined).map((response: Response) => this.transformRawTestEngine(response.json()));
  }

  startTestEngineAsync(testEngineModel: TestEngineModel): Observable<TestEngineModel> {
    let url: string = this.mainUrl + testEngineModel.name + '/start/async';
    return this.http.post(url, undefined).map((response: Response) => this.transformRawTestEngine(response.json()));
  }

  stopTestEngine(testEngineModel: TestEngineModel): Observable<TestEngineModel> {
    let url: string = this.mainUrl + testEngineModel.name;
    return this.http.delete(url).map((response: Response) => this.transformRawTestEngine(response.json()));
  }

  isStarted(testEngineModel: TestEngineModel): Observable<boolean> {
    let url: string = this.mainUrl + testEngineModel.name + '/started';
    return this.http.get(url).map((response: Response) => response.json());
  }

  isWorking(testEngineModel: TestEngineModel) {
    let url: string = this.mainUrl + testEngineModel.name + '/working';
    return this.http.get(url).map((response: Response) => response.json());
  }

  getUrl(name: string) {
    let url: string = this.mainUrl + name + '/url';
    return this.http.get(url).map((response: Response) => response['_body']);
  }
}
