import { TestEngineModel } from './test-engine-model';
import { ConfigurationService } from '../config/configuration-service.service';
import { Http, RequestOptions } from '@angular/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';

@Injectable()
export class TestEnginesService {
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
        let url: string = this.configurationService.configModel.hostApi + '/engines/';
        return this.http.get(url)
            .map((response) => this.createTestEnginesList(response.json()));
    }

    startTestEngine(testEngineModel: TestEngineModel) {
        let url: string = this.configurationService.configModel.hostApi + '/engines/';
        return this.http.post(url, testEngineModel.name)
            .map((response) => response);
    }
} 