import { Component, OnInit, Input } from '@angular/core';
import { TJobExecModel } from '../tjobExec-model';
import { ParameterModel } from '../../parameter/parameter-model';
import { TestCaseModel } from '../../test-case/test-case-model';
import { getResultIconByString } from '../../../shared/utils';

@Component({
  selector: 'etm-parent-tjob-exec-report-view',
  templateUrl: './parent-tjob-exec-report-view.component.html',
  styleUrls: ['./parent-tjob-exec-report-view.component.scss'],
})
export class ParentTjobExecReportViewComponent implements OnInit {
  @Input()
  model: TJobExecModel;

  // test * config
  uniqueConfigRows: any[] = [];
  // config1 * config2
  twoConfigRows: any[] = [];
  // test * config1|config2
  config1Config2Rows: any[] = [];
  // test * config2|config1
  config2Config1Rows: any[] = [];

  uniqueConfigColumns: any[] = [{ name: 'caseName', label: 'Test Name' }];
  twoConfigColumns: any[] = [];

  config1Config2Columns: any[] = [{ name: 'caseName', label: 'Test Name' }];
  config2Config1Columns: any[] = [{ name: 'caseName', label: 'Test Name' }];

  invertAxis: boolean = false;

  constructor() {}

  ngOnInit() {
    try {
      this.loadData();
    } catch (e) {
      console.log(e);
    }
  }

  loadData(): void {
    if (
      this.model &&
      this.model.execChilds &&
      this.model.execChilds.length > 0 &&
      this.model.execChilds[0].testSuites &&
      this.model.execChilds[0].testSuites.length > 0 &&
      this.model.execChilds[0].testSuites[0].testCases &&
      this.model.execChilds[0].testSuites[0].testCases.length > 0
    ) {
      if (this.model.multiConfigurations.length === 1) {
        this.loadUniqueConfigRows();
      } else if (this.model.multiConfigurations.length === 2) {
        this.loadTwoConfigRows();
        this.loadConfig1Config2Rows();
        this.loadConfig2Config1Rows();
      }
    }
  }

  loadUniqueConfigRows(): void {
    let configName: string = this.model.multiConfigurations[0].name;

    let suitePos: number = 0;
    let casePos: number = 0;
    for (let testSuite of this.model.execChilds[0].testSuites) {
      for (let testCase of testSuite.testCases) {
        let row: any = {
          caseName: testSuite.name + ' - ' + testCase.name,
        };
        for (let child of this.model.execChilds) {
          let param: ParameterModel = this.getParameterByName(configName, child.parameters);
          let colName: string = param.value;
          let colValue: string = child.testSuites[suitePos].testCases[casePos].getResult();
          row[colName] = colValue;
          if (casePos === 0) {
            this.uniqueConfigColumns[0].label = 'Test Name / ' + param.name;
            // Add column only first
            this.uniqueConfigColumns.push({ name: colName, label: colName });
          }
        }
        this.uniqueConfigRows.push(row);
        casePos++;
      }
      suitePos++;
    }
  }

  loadTwoConfigRows(): void {
    let config1Name: string = this.model.multiConfigurations[0].name;
    let config2Name: string = this.model.multiConfigurations[1].name;

    let addedColumns: string[] = [];

    let auxMap: Map<string, number> = new Map<string, number>();
    for (let child of this.model.execChilds) {
      let param1: ParameterModel = this.getParameterByName(config1Name, child.parameters);
      let param2: ParameterModel = this.getParameterByName(config2Name, child.parameters);

      if (this.twoConfigColumns.length === 0) {
        this.twoConfigColumns.push({ name: 'col1', label: param1.name + ' / ' + param2.name });
      }

      let row: any;
      let rowName: string = param1.value;

      let colName: string = param2.value;
      let colValue: string = child.result;
      let position: number = 0;

      if (auxMap.get(rowName) !== undefined) {
        position = auxMap.get(rowName);
        row = this.twoConfigRows[position];
      } else {
        row = {
          col1: rowName,
        };
        position = this.twoConfigRows.length;
        auxMap.set(rowName, position);
      }

      row[colName] = colValue;

      if (addedColumns.indexOf(colName) === -1) {
        this.twoConfigColumns.push({ name: colName, label: colName });
        addedColumns.push(colName);
      }

      this.twoConfigRows[position] = row;
      this.twoConfigRows = [...this.twoConfigRows];
    }
  }

  loadConfig1Config2Rows(): void {
    let config1Name: string = this.model.multiConfigurations[0].name;
    let config2Name: string = this.model.multiConfigurations[1].name;

    let suitePos: number = 0;
    let casePos: number = 0;
    for (let testSuite of this.model.execChilds[0].testSuites) {
      for (let testCase of testSuite.testCases) {
        let config1Map: Map<string, Map<string, string>> = new Map();
        for (let config1Value of this.model.multiConfigurations[0].configValues) {
          let config2Map: Map<string, string> = new Map();
          config1Map.set(config1Value, config2Map);
        }

        for (let child of this.model.execChilds) {
          let param1: ParameterModel = this.getParameterByName(config1Name, child.parameters);
          let param2: ParameterModel = this.getParameterByName(config2Name, child.parameters);

          let result: string = child.testSuites[suitePos].testCases[casePos].getResult();
          config1Map.get(param1.value).set(param2.value, result);
        }

        let caseName: string = testSuite.name + ' - ' + testCase.name;
        let row: any = {
          caseName: caseName,
        };

        config1Map.forEach((config2Map: Map<string, string>, key1: string) => {
          config2Map.forEach((value: string, key2: string) => {
            let colName: string = config1Name + '= ' + key1 + ' \n ' + config2Name + '= ' + key2;
            row[colName] = value;
            if (casePos === 0) {
              // Add column only first time
              this.config1Config2Columns.push({ name: colName, label: colName });
            }
          });
        });

        this.config1Config2Rows.push(row);
        casePos++;
      }
      suitePos++;
    }
  }

  loadConfig2Config1Rows(): void {
    let config1Name: string = this.model.multiConfigurations[0].name;
    let config2Name: string = this.model.multiConfigurations[1].name;

    let suitePos: number = 0;
    let casePos: number = 0;
    for (let testSuite of this.model.execChilds[0].testSuites) {
      for (let testCase of testSuite.testCases) {
        let config2Map: Map<string, Map<string, string>> = new Map();
        for (let config2Value of this.model.multiConfigurations[1].configValues) {
          let config1Map: Map<string, string> = new Map();
          config2Map.set(config2Value, config1Map);
        }

        for (let child of this.model.execChilds) {
          let param1: ParameterModel = this.getParameterByName(config1Name, child.parameters);
          let param2: ParameterModel = this.getParameterByName(config2Name, child.parameters);

          let result: string = child.testSuites[suitePos].testCases[casePos].getResult();
          config2Map.get(param2.value).set(param1.value, result);
        }

        let caseName: string = testSuite.name + ' - ' + testCase.name;
        let row: any = {
          caseName: caseName,
        };

        config2Map.forEach((config1Map: Map<string, string>, key2: string) => {
          config1Map.forEach((value: string, key1: string) => {
            let colName: string = config2Name + '= ' + key2 + ' \n ' + config1Name + '= ' + key1;
            row[colName] = value;
            if (casePos === 0) {
              // Add column only first time
              this.config2Config1Columns.push({ name: colName, label: colName });
            }
          });
        });

        this.config2Config1Rows.push(row);
        casePos++;
      }
      suitePos++;
    }
  }

  getParameterByName(name: string, params: ParameterModel[]): ParameterModel {
    for (let param of params) {
      if (param.name === name) {
        return param;
      }
    }
    return;
  }

  getResultIconByString(result: string): any {
    return getResultIconByString(result);
  }

  switchInvertAxis(): void {
    this.invertAxis = !this.invertAxis;
  }
}
