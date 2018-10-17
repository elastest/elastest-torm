import { Component, Input, OnInit } from '@angular/core';
import { MultiConfigModel } from '../../../shared/multi-config-view/multi-config-view.component';
import { getResultIconByString, isString } from '../../../shared/utils';
import { ParameterModel } from '../../parameter/parameter-model';
import { TJobExecModel } from '../tjobExec-model';

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
  uniqueConfigExecTableRows: any[] = [];
  uniqueConfigColumns: any[] = [{ name: 'caseName', label: 'Test Name' }];
  uniqueConfigExecTableColumns: any[] = [];

  /* ***************** */
  /* *** 2 configs *** */
  /* ***************** */

  // config1 * config2
  twoConfigExecTableRows: any[] = [];
  twoConfigExecTableColumns: any[] = [];
  // test * config1|config2
  config1Config2Rows: any[] = [];
  config1Config2Columns: any[] = [{ name: 'caseName', label: 'Test Name' }];
  // test * config2|config1
  config2Config1Rows: any[] = [];
  config2Config1Columns: any[] = [{ name: 'caseName', label: 'Test Name' }];

  /* *************************** */
  /* *** more than 2 configs *** */
  /* *************************** */
  configNRows: any[] = [];
  configNColumns: any[] = [{ name: 'caseName', label: 'Test Name' }];

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
        this.loadUniqueConfigExecTableRows();
      } else if (this.model.multiConfigurations.length === 2) {
        this.loadtwoConfigExecTableRows();
        this.loadConfig1Config2Rows();
        this.loadConfig2Config1Rows();
      } else if (this.model.multiConfigurations.length > 2) {
        this.loadNConfigRows();
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

  loadUniqueConfigExecTableRows(): void {
    let configName: string = this.model.multiConfigurations[0].name;
    let columnAdded: boolean = false;
    for (let child of this.model.execChilds) {
      let param: ParameterModel = this.getParameterByName(configName, child.parameters);

      if (this.uniqueConfigExecTableColumns.length === 0) {
        this.uniqueConfigExecTableColumns.push({ name: 'col1', label: param.name });
      }
      let row: any = {
        col1: param.value,
      };

      let colName: string = 'Result';
      let colValue: string = child.result;
      row[colName] = colValue;
      if (!columnAdded) {
        // Add column only first
        this.uniqueConfigExecTableColumns.push({ name: colName, label: colName });
        columnAdded = true;
      }
      this.uniqueConfigExecTableRows.push(row);
    }
  }

  loadtwoConfigExecTableRows(): void {
    let config1Name: string = this.model.multiConfigurations[0].name;
    let config2Name: string = this.model.multiConfigurations[1].name;

    let addedColumns: string[] = [];

    let auxMap: Map<string, number> = new Map<string, number>();
    for (let child of this.model.execChilds) {
      let param1: ParameterModel = this.getParameterByName(config1Name, child.parameters);
      let param2: ParameterModel = this.getParameterByName(config2Name, child.parameters);

      if (this.twoConfigExecTableColumns.length === 0) {
        this.twoConfigExecTableColumns.push({ name: 'col1', label: '' });
      }

      let row: any;
      let rowName: string = param1.name + '= ' + param1.value;

      let colName: string = param2.name + '= ' + param2.value;
      let colValue: string = child.result;
      let position: number = 0;

      if (auxMap.get(rowName) !== undefined) {
        position = auxMap.get(rowName);
        row = this.twoConfigExecTableRows[position];
      } else {
        row = {
          col1: rowName,
        };
        position = this.twoConfigExecTableRows.length;
        auxMap.set(rowName, position);
      }

      row[colName] = colValue;

      if (addedColumns.indexOf(colName) === -1) {
        this.twoConfigExecTableColumns.push({ name: colName, label: colName });
        addedColumns.push(colName);
      }

      this.twoConfigExecTableRows[position] = row;
      this.twoConfigExecTableRows = [...this.twoConfigExecTableRows];
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

  initConfigMap(configArray: MultiConfigModel[]): Map<string, any> {
    if (configArray.length === 0) {
      return undefined;
    }

    let configMap: Map<string, any> = new Map();
    let currentConfig: MultiConfigModel = configArray.shift();

    if (configArray.length > 0) {
      for (let configValue of currentConfig.configValues) {
        let childConfigMap: Map<string, string> = this.initConfigMap([...configArray]);
        configMap.set(configValue, childConfigMap);
      }
    } else {
      // last
      for (let configValue of currentConfig.configValues) {
        configMap.set(configValue, undefined);
      }
    }
    return configMap;
  }

  updateConfigMap(configMap: Map<string, any>, paramsArray: ParameterModel[], value: string): Map<string, any> {
    if (paramsArray.length === 0) {
      return configMap;
    }
    let currentParam: ParameterModel = paramsArray.shift();

    while (paramsArray.length > 0 && !currentParam.multiConfig) {
      currentParam = paramsArray.shift();
    }

    if (paramsArray.length > 0) {
      let childConfigMap: Map<string, string> = this.updateConfigMap(configMap.get(currentParam.value), [...paramsArray], value);
      configMap.set(currentParam.value, childConfigMap);
    } else if (currentParam !== undefined) {
      // last
      configMap.set(currentParam.value, value);
    }
    return configMap;
  }

  getRowByMap(
    configMap: Map<string, any>,
    addColumn: boolean,
    row: any,
    columnName: string = '',
    configPosition: number = 0,
  ): any {
    if (configMap === undefined || configMap.size === 0) {
      return row;
    }

    configMap.forEach((value: any, key: string) => {
      let currentColumnName: string = columnName;
      if (currentColumnName === '') {
        currentColumnName = '';
      } else {
        currentColumnName += ' \n ';
      }

      currentColumnName += this.model.multiConfigurations[configPosition].name + '= ' + key;

      if (value instanceof Map) {
        return this.getRowByMap(value, addColumn, row, currentColumnName, configPosition + 1);
      } else if (isString(value)) {
        row[currentColumnName] = value;
        if (addColumn) {
          // Add column only first time
          this.configNColumns.push({ name: currentColumnName, label: currentColumnName });
        }
      } else {
        return row;
      }
    });
    return row;
  }

  loadNConfigRows(): void {
    let suitePos: number = 0;
    let casePos: number = 0;
    for (let testSuite of this.model.execChilds[0].testSuites) {
      for (let testCase of testSuite.testCases) {
        let configArray: MultiConfigModel[] = [...this.model.multiConfigurations];
        let configMap: Map<string, any> = this.initConfigMap(configArray);

        for (let child of this.model.execChilds) {
          let result: string = child.testSuites[suitePos].testCases[casePos].getResult();
          this.updateConfigMap(configMap, [...child.parameters], result);
        }

        let caseName: string = testSuite.name + ' - ' + testCase.name;
        let row: any = {
          caseName: caseName,
        };

        row = this.getRowByMap(configMap, casePos === 0, row);
        this.configNRows.push(row);
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
