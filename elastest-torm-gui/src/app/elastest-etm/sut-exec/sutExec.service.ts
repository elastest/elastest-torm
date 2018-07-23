import { ETModelsTransformServices } from '../../shared/services/et-models-transform.service';
import { SutModel } from '../sut/sut-model';
import { SutExecModel } from './sutExec-model';
import { Http } from '@angular/http';
import { Injectable } from '@angular/core';
import { ConfigurationService } from '../../config/configuration-service.service';
import 'rxjs/Rx';

@Injectable()
export class SutExecService {
    constructor(
        private http: Http, private configurationService: ConfigurationService, private eTModelsTransformServices: ETModelsTransformServices
    ) { }

    public deploySut(sutId: number) {
        let url: string = this.configurationService.configModel.hostApi + '/sut/' + sutId + '/exec';
        return this.http.post(url, {})
            .map((response) => response.json());
    }

    public getSutsExecutions(sut: SutModel) {
        let url: string = this.configurationService.configModel.hostApi + '/sut/' + sut.id + '/exec';
        return this.http.get(url)
            .map((response) => this.eTModelsTransformServices.jsonToSutExecsList(response.json()));
    }

    public getSutExecution(sut: SutModel, idSutExecution: number) {
        let url: string = this.configurationService.configModel.hostApi + '/sut/' + sut.id + '/exec/' + idSutExecution;
        return this.http.get(url)
            .map(
            (response) => {
                let data: any = response.json();
                if (data !== undefined && data !== null) {
                    return this.eTModelsTransformServices.jsonToSutExecModel(data);
                } else {
                    throw new Error('Empty response. SuT Execution not exist or you don\'t have permissions to access it');
                }
            });
    }

    public deleteSutExecution(sut: SutModel, sutExecution: SutExecModel) {
        let url: string = this.configurationService.configModel.hostApi + '/sut/' + sut.id + '/exec/' + sutExecution.id;
        return this.http.delete(url)
            .map((response) => response.json());
    }
}
