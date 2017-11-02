import { EimConfigModel } from './eim-config-model';
import { SutModel } from './sut-model';
import { SutExecModel } from '../sut-exec/sutExec-model';
import { Http } from '@angular/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import { ConfigurationService } from '../../config/configuration-service.service';
import 'rxjs/Rx';

@Injectable()
export class SutService {
  constructor(private http: Http, private configurationService: ConfigurationService) { }

  public getSuts() {
    let url = this.configurationService.configModel.hostApi + '/sut';
    return this.http.get(url)
      .map((response) => this.transformSutDataToDataTable(response.json()));
  }

  transformSutDataToDataTable(suts: any[]) {
    let sutsDataToTable: SutModel[] = [];
    for (let sut of suts) {
      sutsDataToTable.push(this.transformToSutmodel(sut));
    }
    return sutsDataToTable;
  }

  transformToSutmodel(sut: any) {
    let sutsDataToTable: SutModel;

    sutsDataToTable = new SutModel();
    sutsDataToTable.id = sut.id;
    sutsDataToTable.name = sut.name;
    sutsDataToTable.specification = sut.specification;
    sutsDataToTable.sutType = sut.sutType;
    sutsDataToTable.description = sut.description;
    sutsDataToTable.project = sut.project;
    sutsDataToTable.eimConfig = new EimConfigModel(sut.eimConfig);
    sutsDataToTable.instrumentalize = sut.instrumentalize;
    if (sut.instrumentalize === undefined || sut.instrumentalize === null) {
      sut.instrumentalize = false;
    }
    sutsDataToTable.currentSutExec = sut.currentSutExec;
    sutsDataToTable.instrumentedBy = sut.instrumentedBy;
    sutsDataToTable.port = sut.port;

    return sutsDataToTable;
  }

  public getSut(id: number) {
    let url: string = this.configurationService.configModel.hostApi + '/sut/' + id;
    return this.http.get(url)
      .map(
      (response) => {
        let data: any = response.json();
        if (data !== undefined && data !== null) {
          return this.transformToSutmodel(data);
        } else {
          throw new Error('Empty response. SuT not exist or you don\'t have permissions to access it');
        }
      });
  }

  public createSut(sut: SutModel) {
    let url: string = this.configurationService.configModel.hostApi + '/sut';
    if (sut.eimConfig) {
      sut.eimConfig.validatePrivateKey();
    }
    return this.http.post(url, sut)
      .map((response) => response.json());
  }


  public modifySut() { }

  public deleteSut(sut: SutModel) {
    let url: string = this.configurationService.configModel.hostApi + '/sut/' + sut.id;
    return this.http.delete(url)
      .map((response) => response.json());
  }

  public getLogstashInfo() {
    return this.configurationService.getLogstashInfo();
  }
}
