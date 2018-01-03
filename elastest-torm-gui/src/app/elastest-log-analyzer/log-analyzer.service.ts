import { Observable } from 'rxjs/Rx';
import { Http, Response } from '@angular/http';
import { ETModelsTransformServices } from '../shared/services/et-models-transform.service';
import { ConfigurationService } from '../config/configuration-service.service';
import { Injectable } from '@angular/core';
import { LogAnalyzerConfigModel } from './log-analyzer-config-model';

@Injectable()
export class LogAnalyzerService {
    constructor(
        private http: Http, private configurationService: ConfigurationService,
        private eTModelsTransformServices: ETModelsTransformServices,
    ) { }

    public getLogAnalyzerConfig(): Observable<LogAnalyzerConfigModel> {
        let url: string = this.configurationService.configModel.hostApi + '/loganalyzerconfig/';
        return this.http.get(url)
            .map(
            (response: Response) => {
                let errorOccur: boolean = true;
                if (response !== undefined && response !== null) {
                    if (response['_body']) {
                        let data: any = response.json();
                        if (data !== undefined && data !== null) {
                            errorOccur = false;
                            return this.eTModelsTransformServices.jsonToLogAnalyzerConfigModel(data);
                        }
                    } else {
                        errorOccur = false;
                        return undefined;
                    }
                }
                if (errorOccur) {
                    throw new Error('Empty response. LogAnalyzerConfig not exist or you don\'t have permissions to access it');
                }
            });
    }

    public saveLogAnalyzerConfig(logAnalyzerConfigModel: LogAnalyzerConfigModel): Observable<LogAnalyzerConfigModel> {
        let url: string = this.configurationService.configModel.hostApi + '/loganalyzerconfig';
        logAnalyzerConfigModel.generatColumnsConfigJson();
        return this.http.post(url, logAnalyzerConfigModel)
            .map((response: Response) => {
                let data: any = response.json();
                if (data !== undefined && data !== null) {
                    return this.eTModelsTransformServices.jsonToLogAnalyzerConfigModel(data);
                } else {
                    throw new Error('Empty response. TJob not exist or you don\'t have permissions to access it');
                }
            });
    }
}