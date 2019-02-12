import { Component, OnInit, Input } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ITdDataTableColumn } from '@covalent/core';
import { Log } from '../model/log.model';
import { ElasticsearchService } from '../service/elasticsearch.service';
import { TableService } from '../service/table.service';
import { ClassC } from '../model/classc.model';
import { TestC } from '../model/testc.model';
import { Execution } from '../model/execution.model';

@Component({
  selector: 'elastest-logcomparator-report-comparison',
  templateUrl: './report-comparison.component.html',
  styleUrls: ['./report-comparison.component.scss'],
})
export class ReportComparisonComponent implements OnInit {
  @Input() diff: string;

  classesL: any[];
  classesLc: any[];
  comparatorText: string = '';
  comparedText: string = '';
  comparisonInProgress: boolean = false;
  comparisonButtonsClasses: string[] = ['primary', 'primary', 'primary'];
  comparisonMode: number;
  deleteInProgress: boolean;
  execDeleting: string;
  execsData: ITdDataTableColumn[] = [
    { name: 'id', label: 'Id', width: 60 },
    { name: 'start_date', label: 'Start date', width: 240 },
    { name: 'status', label: 'Status', width: 200 },
  ];
  execsRow: any[] = [];
  execution: Execution;
  loadingData: boolean;
  project: string;
  showExecSelection: boolean;
  showSelectionMessage: boolean = false;
  selected: any[] = [];
  singleSelected: Execution;
  status: string = 'BUILD FAILURE';
  ready: boolean;
  resultData: any[] = [];
  test: string;
  viewButtonsClasses: string[] = ['accent', 'primary', 'primary', 'primary'];
  viewMode: number;

  constructor(
    private activatedRoute: ActivatedRoute,
    private tableService: TableService,
    private elasticsearchService: ElasticsearchService,
    private router: Router,
  ) {
    this.comparisonInProgress = false;
    this.loadingData = true;
    this.showExecSelection = false;
  }

  async ngOnInit(): Promise<any> {
    // this.test = this.activatedRoute.snapshot.parent.params['exec'];
    // this.execution = await this.elasticsearchService.getExecutionByIdAsync(this.test);
    // this.project = this.activatedRoute.snapshot.parent.parent.params['project'];
    this.classesL = [];
    this.classesLc = [];
    this.viewMode = 0;
    // this.reloadTabContent();
    // const result: Execution = await this.elasticsearchService.getExecutionByIdAsync(this.test);
    // this.status = result.status;

    this.resultData = [];
    this.comparatorText = '';
    this.comparatorText = this.generateOutput(this.classesL);
    this.comparedText = '';
    this.comparedText = this.generateOutput(this.classesLc);
    this.resultData[0] = {
      logs: await this.readDiffer(),
    };
    this.comparisonInProgress = true;

    this.loadingData = false;
  }

  async reloadTabContent(): Promise<any> {
    const response = await this.elasticsearchService.getExecutionsByProjectAsync(this.project);
    this.execsRow = [];
    for (let i: number = 0; i < response.length; i++) {
      let icon: any;
      let classi: any;
      if (response[i].status.indexOf('SUCCESS') !== -1) {
        icon = 'check_circle';
        classi = 'tc-green-700';
      } else {
        icon = 'error';
        classi = 'tc-red-700';
      }
      if (this.test !== response[i].id + '') {
        this.execsRow.push({
          id: response[i].id,
          start_date: response[i].start_date,
          status: {
            icon: icon,
            class: classi,
            status: response[i].status,
          },
          time_elapsed: response[i].time_elapsed,
        });
      } else {
        this.selected[0] = this.execsRow[this.execsRow.length - 1];
      }
    }
  }

  async updateComparisonMode(mode: number): Promise<any> {
    this.comparisonMode = mode;
    this.loadingData = true;
    if (this.selected[0] === undefined) {
      this.showSelectionMessage = true;
      this.showExecSelection = true;
      this.loadingData = false;
    } else {
      switch (this.viewMode) {
        case 0:
          await this.generateRawComparison();
          break;
        case 1:
          await this.generateMethodsComparison();
          break;
        case 2:
          await this.generateMethodsComparison();
          break;
        case 3:
        default:
          await this.generateRawComparison();
          break;
      }
      this.resetComparisonButtonsClasses();
    }
  }

  async updateViewMode(comp: number, mode: number): Promise<any> {
    this.viewMode = mode;
    this.loadingData = true;
    this.resetViewButtonsClasses();
    switch (this.viewMode) {
      case 0:
        await this.viewRaw(comp, true);
        break;
      case 1:
        await this.viewByMethods(comp);
        break;
      case 2:
        await this.viewByMethods(comp);
        break;
      case 3:
      default:
        await this.viewRaw(comp, false);
        break;
    }
    if (this.comparisonInProgress) {
      this.updateComparisonMode(this.comparisonMode);
    } else {
      this.loadingData = false;
    }
  }

  disableComparison(): void {
    this.comparisonInProgress = false;
    this.comparisonMode = 0;
    this.selected[0] = undefined;
    this.resetComparisonButtonsClasses();
  }

  goBack(): void {
    this.router.navigate(['/projects', this.project]);
  }

  selectEvent(event: any): void {
    this.selected[0] = event.row;
    if (this.comparisonInProgress) {
      this.updateComparisonMode(this.comparisonMode);
    }
    if (this.showSelectionMessage) {
      this.showSelectionMessage = false;
      this.updateComparisonMode(this.comparisonMode);
      this.showExecSelection = false;
    }
  }

  private generateOutput(logs: Log[]): string {
    let result: string = '';
    let comparatorDate: Date = new Date();
    if (!logs || logs[0] === undefined) {
      return result;
    }
    for (let i: number = 0; i < logs.length; i++) {
      logs[i].timestamp.length > 2 ? (logs[i].timestamp = logs[i].timestamp.substring(0, 23)) : (logs[i].timestamp = '');
      logs[i].thread.length > 2
        ? logs[i].thread.indexOf('[') === -1 && (logs[i].thread = ' [' + logs[i].thread + '] ')
        : (logs[i].thread = '');
      logs[i].level.length > 2 ? (logs[i].level = logs[i].level) : (logs[i].level = '');
      logs[i].logger.length > 2 ? (logs[i].logger = logs[i].logger) : (logs[i].logger = '');
    }
    if (this.comparisonMode + '' === '2') {
      comparatorDate = new Date(this.findValidTimestamp(logs));
    }
    for (let i: number = 0; i < logs.length; i++) {
      this.comparisonMode + '' === '1' && (logs[i].timestamp = '');
      if (this.comparisonMode + '' === '2' && logs[i].timestamp.length > 2) {
        logs[i].timestamp = (new Date(logs[i].timestamp).valueOf() - comparatorDate.valueOf()).toString();
      }
      result += logs[i].timestamp + logs[i].thread + logs[i].level + ' ' + logs[i].logger + '' + ' ' + logs[i].message + '\r\n';
    }
    return result;
  }

  private async generateMethodsComparison(): Promise<any> {
    this.comparisonInProgress = false;
    const comparisonDictionary: { [name: string]: ClassC } = {};
    await this.updateViewMode(0, this.viewMode);
    await this.updateViewMode(1, this.viewMode);
    for (let i: number = 0; i < this.classesL.length; i++) {
      if (comparisonDictionary[this.classesL[i].name] === undefined) {
        const methods = [];
        for (let j: number = 0; j < this.classesL[i].methods.length; j++) {
          methods.push({
            name: this.classesL[i].methods[j].name,
            comparator: this.generateOutput(this.classesL[i].methods[j].logs),
            compared: '',
          });
        }
        comparisonDictionary[this.classesL[i].name] = {
          name: this.classesL[i].name,
          tests: methods,
        };
      }
    }
    for (let i: number = 0; i < this.classesLc.length; i++) {
      if (comparisonDictionary[this.classesLc[i].name] !== undefined) {
        const targetClass = comparisonDictionary[this.classesLc[i].name];
        for (let j: number = 0; j < this.classesLc[i].methods.length; j++) {
          const position = this.containsTest(targetClass.tests, this.classesLc[i].methods[j]);
          if (position !== -1) {
            targetClass.tests[position].compared = this.generateOutput(this.classesLc[i].methods[j].logs);
          } else {
            targetClass.tests.push({
              name: this.classesLc[i].methods[j].name,
              comparator: '',
              compared: this.generateOutput(this.classesLc[i].methods[j].logs),
            });
          }
        }
        comparisonDictionary[this.classesLc[i].name] = targetClass;
      } else {
        const methodsC = [];
        for (let j: number = 0; j < this.classesLc[i].methods.length; j++) {
          methodsC.push({
            methodsC: this.classesLc[i].methods[j].name,
            comparator: '',
            compared: this.generateOutput(this.classesLc[i].methods[j].logs),
          });
        }
        comparisonDictionary[this.classesLc[i].name] = {
          name: this.classesLc[i].name,
          tests: methodsC,
        };
      }
    }
    if (this.viewMode === 2) {
      const map = await this.cleanDictionary(comparisonDictionary);
      this.resultData = [];
      for (const classC in map) {
        if (map.hasOwnProperty(classC)) {
          const value = map[classC];
          const methodsData = [];
          this.comparatorText = '';
          this.comparedText = '';
          for (let i: number = 0; i < value.tests.length; i++) {
            this.comparatorText = value.tests[i].comparator;
            this.comparedText = value.tests[i].compared;
            methodsData.push({
              name: value.tests[i].name,
              logs: await this.readDiffer(),
            });
          }
          this.resultData.push({
            name: value.name,
            methods: methodsData,
          });
        }
      }
    } else {
      this.resultData = [];
      for (const classC in comparisonDictionary) {
        if (comparisonDictionary.hasOwnProperty(classC)) {
          const value = comparisonDictionary[classC];
          const methodsData = [];
          this.comparatorText = '';
          this.comparedText = '';
          for (let i: number = 0; i < value.tests.length; i++) {
            this.comparatorText = value.tests[i].comparator;
            this.comparedText = value.tests[i].compared;
            methodsData.push({
              name: value.tests[i].name,
              logs: await this.readDiffer(),
            });
          }
          this.resultData.push({
            name: value.name,
            methods: methodsData,
          });
        }
      }
    }
    this.comparisonInProgress = true;
  }

  private async cleanDictionary(dictionary: any): Promise<any> {
    const execution = await this.elasticsearchService.getExecutionByIdAsync(this.test);
    const map: { [name: string]: ClassC } = {};
    for (const classC in dictionary) {
      if (dictionary.hasOwnProperty(classC)) {
        const value = dictionary[classC];
        for (let i: number = 0; i < value.tests.length; i++) {
          if (this.containsError(value.tests[i].name, execution.testcases)) {
            if (!map.hasOwnProperty(classC)) {
              map[classC] = {
                name: classC,
                tests: [],
              };
            }
            map[classC].tests.push({
              name: value.tests[i].name,
              comparator: value.tests[i].comparator,
              compared: value.tests[i].compared,
            });
          }
        }
      }
    }
    return map;
  }

  private containsError(value: string, testcases: any[]): boolean {
    for (let i: number = 0; i < testcases.length; i++) {
      if (testcases[i].failureDetail !== null && testcases[i].name.indexOf(value) !== -1) {
        return true;
      }
    }
    return false;
  }

  private async generateRawComparison(): Promise<any> {
    this.comparisonInProgress = false;
    await this.updateViewMode(0, this.viewMode);
    await this.updateViewMode(1, this.viewMode);
    this.resultData = [];
    this.comparatorText = '';
    this.comparatorText = this.generateOutput(this.classesL);
    this.comparedText = '';
    this.comparedText = this.generateOutput(this.classesLc);
    this.resultData[0] = {
      logs: await this.readDiffer(),
    };
    this.comparisonInProgress = true;
  }

  private async readDiffer(): Promise<any> {
    const response: string = this.diff
      ? this.diff
      : await this.elasticsearchService.postDiff(this.comparatorText, this.comparedText);
    return this.tableService.generateTable(response);
  }

  private async viewByMethods(mode: number): Promise<any> {
    this.ready = false;
    mode === 0 ? (this.classesL = []) : (this.classesLc = []);
    const loggers = await this.elasticsearchService.getLogsByTestAsync(
      mode === 0 ? this.test : this.selected[0].id,
      this.project,
      true,
      false,
    );
    for (let i: number = 0; i < loggers.length; i++) {
      if (loggers[i].split(' ').length === 2) {
        const logger = loggers[i].split(' ')[1];
        const partialLogger = logger.split('.')[logger.split('.').length - 1];
        const methods = await this.elasticsearchService.getLogsByLoggerAsync(
          partialLogger,
          this.project,
          mode === 0 ? this.test : this.selected[0].id,
          undefined,
        );
        const methodsData = [];
        for (let j: number = 0; j < methods.length; j++) {
          if (methods[j] !== '') {
            const cleanMethod = methods[j].replace('(', '').replace(')', '');
            methodsData.push({
              name: methods[j],
              logs: await this.elasticsearchService.getLogsByLoggerAsync(
                partialLogger,
                this.project,
                mode === 0 ? this.test : this.selected[0].id,
                cleanMethod,
              ),
            });
          }
        }
        mode === 0
          ? this.classesL.push({ name: loggers[i].split(' ')[1], methods: methodsData })
          : this.classesLc.push({ name: loggers[i].split(' ')[1], methods: methodsData });
      }
    }
    if (this.viewMode === 2 && !this.comparisonInProgress) {
      const execution = await this.elasticsearchService.getExecutionByIdAsync(this.test);
      const classAux = [];
      for (let i: number = 0; i < this.classesL.length; i++) {
        const failMethods = [];
        for (let j: number = 0; j < this.classesL[i].methods.length; j++) {
          if (this.containsError(this.classesL[i].methods[j].name, execution.testcases)) {
            failMethods.push(this.classesL[i].methods[j]);
          }
        }
        if (failMethods.length > 0) {
          classAux.push({
            name: this.classesL[i].name,
            methods: failMethods,
          });
        }
      }
      this.classesL = classAux;
    }
    this.ready = true;
  }

  private async viewRaw(mode: number, maven: boolean): Promise<any> {
    this.ready = false;
    mode === 0 ? (this.classesL = []) : (this.classesLc = []);
    const logs: string[] = await this.elasticsearchService.getLogsByTestAsync(
      mode === 0 ? this.test : this.selected[0].id,
      this.project,
      false,
      maven,
    );
    if (logs) {
      for (let i: number = 0; i < logs.length; i++) {
        mode === 0 ? this.classesL.push(logs[i]) : this.classesLc.push(logs[i]);
      }
      this.ready = true;
    }
  }

  private containsTest(tests: TestC[], test: TestC): number {
    for (let i: number = 0; i < tests.length; i++) {
      if (tests[i].name === test.name) {
        return i;
      }
    }
    return -1;
  }

  private findValidTimestamp(logs: Log[]): string {
    for (let i: number = 0; i < logs.length; i++) {
      if (logs[i].timestamp.length > 2) {
        return logs[i].timestamp;
      }
    }
    return '';
  }

  private index(testcases: string[], method: string): boolean {
    for (let i: number = 0; i < testcases.length; i++) {
      const elements = testcases[i].split(',');
      if (elements[0] === method && elements[1] === 'true') {
        return true;
      }
    }
    return false;
  }

  private resetComparisonButtonsClasses() {
    for (let i: number = 0; i < this.comparisonButtonsClasses.length; i++) {
      this.comparisonButtonsClasses[i] = 'primary';
    }
    if (this.comparisonInProgress) {
      this.comparisonButtonsClasses[this.comparisonMode] = 'accent';
    }
    this.loadingData = false;
  }

  private resetViewButtonsClasses() {
    for (let i: number = 0; i < this.viewButtonsClasses.length; i++) {
      this.viewButtonsClasses[i] = 'primary';
    }
    this.viewButtonsClasses[this.viewMode] = 'accent';
  }
}
