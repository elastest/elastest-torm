import { TestEngineModel } from './test-engine-model';
import { ConfigurationService } from '../config/configuration-service.service';
import { Http, RequestOptions } from '@angular/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';

@Injectable()
export class TestEnginesService {
    mainUrl: string = this.configurationService.configModel.hostApi + '/engines/';

    constructor(private http: Http, private configurationService: ConfigurationService,
    ) { }

    createTestEnginesList(testEnginesNames: any[]): TestEngineModel[] {
        let testEngines: TestEngineModel[] = [];
        for (let name of testEnginesNames) {
            let testEngine: TestEngineModel = new TestEngineModel();
            testEngine.name = name;
            testEngines.push(testEngine);
        }
        return testEngines;
    }

    getTestEngines() {
        return this.http.get(this.mainUrl)
            .map((response) => this.createTestEnginesList(response.json()));
    }

    startTestEngine(testEngineModel: TestEngineModel) {
        return this.http.post(this.mainUrl, testEngineModel.name)
            .map((response) => response['_body']);
    }

    isStarted(testEngineModel: TestEngineModel) {
        let url: string = this.mainUrl + testEngineModel.name + '/started';
        return this.http.get(url)
            .map((response) => (response.json()));
    }

    isWorking(testEngineModel: TestEngineModel) {
        let url: string = this.mainUrl + testEngineModel.name + '/working';
        return this.http.get(url)
            .map((response) => (response.json()));
    }
    
    stopTestEngine(testEngineModel: TestEngineModel) {
        let url: string = this.mainUrl + testEngineModel.name;
        return this.http.delete(url)
            .map((response) => response);
    }

    getUrl(engineName: string) {
        let url: string = this.mainUrl + engineName + '/url';
        return this.http.get(url)
            .map((response) => response['_body']);
    }
}