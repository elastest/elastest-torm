import { StompWSManager } from '../stomp-ws-manager.service';
import { SutModel } from '../sut/sut-model';
import { SutExecModel } from './sutExec-model';
import { Http } from '@angular/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import { ConfigurationService } from '../../config/configuration-service.service';
import 'rxjs/Rx';

@Injectable()
export class SutExecService {
    constructor(private http: Http, private stompWSManager: StompWSManager, private configurationService: ConfigurationService) { }



    public deploySut(sutId: number) {
        let url = this.configurationService.configModel.hostApi + '/sut/' + sutId + '/exec';
        return this.http.post(url, {})
            .map((response) => response.json());
    }

    public getSutsExecutions(sut: SutModel) {
        let url = this.configurationService.configModel.hostApi + '/sut/' + sut.id + '/exec';
        return this.http.get(url)
            .map((response) => this.transformSutExecDataToDataTable(response.json()));
    }

    transformSutExecDataToDataTable(sutExecs: any[]) {
        let sutExecsDataToTable: SutExecModel[] = [];
        for (let sutExec of sutExecs) {
            sutExecsDataToTable.push(this.transformToSutExecmodel(sutExec));
        }
        return sutExecsDataToTable;
    }


    transformToSutExecmodel(sutExec: any) {
        let sutExecsDataToTable: SutExecModel;

        sutExecsDataToTable = new SutExecModel();
        sutExecsDataToTable.id = sutExec.id;
        sutExecsDataToTable.deplotStatus = sutExec.deplotStatus;
        sutExecsDataToTable.url = sutExec.url;
        sutExecsDataToTable.sut = sutExec.sut;

        return sutExecsDataToTable;
    }

    public getSutExecution(sut: SutModel, idSutExecution: number) {
        let url = this.configurationService.configModel.hostApi + '/sut/' + sut.id + '/exec/' + idSutExecution;
        return this.http.get(url)
            .map(
            (response) => {
                let data: any = response.json();
                if (data !== undefined && data !== null) {
                    return this.transformToSutExecmodel(data);
                }
                else {
                    throw new Error('Empty response. SuT Execution not exist or you don\'t have permissions to access it');
                }
            });
    }

    public deleteSutExecution(sut: SutModel, sutExecution: SutExecModel) {
        let url = this.configurationService.configModel.hostApi + '/sut/' + sut.id + '/exec/' + sutExecution.id;
        return this.http.delete(url)
            .map((response) => response.json());
    }

    private subscribeQueues(sutExec: any) {

    }


}
