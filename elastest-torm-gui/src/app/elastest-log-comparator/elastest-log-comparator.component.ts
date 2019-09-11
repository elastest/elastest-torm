import { Component, OnInit, Input } from '@angular/core';
import { LogComparisonModel } from './model/log-comparison.model';
import { isStringIntoArray, allArrayPairCombinations } from '../shared/utils';
import { LogFieldModel } from '../shared/logs-view/models/log-field-model';
import { TJobExecModel } from '../elastest-etm/tjob-exec/tjobExec-model';
import { defaultStreamMap } from '../shared/defaultESData-model';

@Component({
  selector: 'elastest-log-comparator',
  templateUrl: './elastest-log-comparator.component.html',
  styleUrls: ['./elastest-log-comparator.component.scss'],
})
export class ElastestLogComparatorComponent implements OnInit {
  @Input() public live: boolean;
  @Input() public remove: Function;
  @Input() public hide: boolean = false;

  model: Map<string, LogComparisonModel[]> = new Map<string, LogComparisonModel[]>();
  keys: string[] = [];

  aioKey: string = 'AIO';

  selectedLogTab: number;
  selectedLogComparisionTab: number;

  constructor() {}

  ngOnInit(): void {}

  goToLogTab(num: number): void {
    this.selectedLogTab = num;
  }

  goToLogComparisonTab(num: number): void {
    this.selectedLogComparisionTab = num;
  }

  generateAIOLogsComparisonTab(): void {
    if (this.model) {
      // Has only 1 (AIO comparison tab) or two (1 normal and 1 AIO)
      if (this.model.size > 0 && this.model.size < 3 && this.model.has(this.aioKey)) {
        // 1 or 2 and has AIO
        if (this.model.size > 0) {
          this.model.delete(this.aioKey);
          this.keys = Array.from(this.model.keys());
        }
      } else {
        if (this.model.size > 1) {
          if (this.model.has(this.aioKey)) {
            this.model.delete(this.aioKey);
            this.keys = Array.from(this.model.keys());
          }

          let componentsList: string[] = [];

          for (let key of this.keys) {
            let currentLogComparisonList: LogComparisonModel[] = this.model.get(key);
            if (currentLogComparisonList.length > 0) {
              let currentComponent: string = currentLogComparisonList[0].component;

              if (!isStringIntoArray(currentComponent, componentsList)) {
                componentsList.push(currentComponent);
              }
            }
          }

          let firstLogComparisonList: LogComparisonModel[] = Array.from(this.model.values())[0];
          for (let logComparison of firstLogComparisonList) {
            let newLogComparison: LogComparisonModel = new LogComparisonModel();
            newLogComparison.name = logComparison.pair.join(' | ');
            newLogComparison.stream = logComparison.stream;
            newLogComparison.startDate = logComparison.startDate;
            newLogComparison.endDate = logComparison.endDate;
            newLogComparison.pair = logComparison.pair;
            newLogComparison.components = componentsList;
            this.addMoreLogsComparison(this.aioKey, newLogComparison);
          }
        } // Else is empty
      }
    }
  }

  // Comparison
  addMoreLogsComparison(logName: string, logComparison: LogComparisonModel): boolean {
    if (!this.alreadyExistComparison(logName, logComparison)) {
      if (!this.model.has(logName)) {
        // model.put

        this.model = new Map(this.model.set(logName, []));
        this.keys = Array.from(this.model.keys());
      }

      this.model.get(logName).push(logComparison);
      return true;
    } else {
      return false;
    }
  }

  addMoreLogsComparisons(
    monitoringIndicesList: string[],
    startDate: Date,
    endDate: Date,
    logName: string,
    stream: string,
    component: string,
  ): boolean {
    let added: boolean = false;

    if (monitoringIndicesList.length > 1) {
      let pairCombinations: string[][] = allArrayPairCombinations(monitoringIndicesList);

      for (let pair of pairCombinations) {
        let logComparison: LogComparisonModel = new LogComparisonModel();
        logComparison.name = pair.join(' | ');
        logComparison.component = component;
        logComparison.stream = stream;
        logComparison.startDate = startDate;
        logComparison.endDate = endDate;
        logComparison.pair = pair;

        added = this.addMoreLogsComparison(logName, logComparison) || added;
      }
    }

    if (added) {
      this.generateAIOLogsComparisonTab();
    }
    return added;
  }

  addMoreLogsComparisonsByTJobExecsList(tJobExecs: TJobExecModel[], allLogs: LogFieldModel[]): void {
    if (tJobExecs && allLogs) {
      let monitoringIndicesList: string[] = [];
      let startDate: Date;
      let endDate: Date;

      for (let tJobExec of tJobExecs) {
        // Monitoring index
        if (tJobExec.monitoringIndex !== undefined && tJobExec.monitoringIndex !== '') {
          monitoringIndicesList.push(tJobExec.monitoringIndex);
        }

        // StartDate
        if (!startDate) {
          startDate = tJobExec.startDate;
        } else {
          startDate = tJobExec.startDate.getTime() < startDate.getTime() ? tJobExec.startDate : startDate;
        }
        // EndDate
        if (!endDate) {
          endDate = tJobExec.endDate;
        } else {
          if (!tJobExec.endDate) {
            tJobExec.endDate = new Date();
          }
          endDate = tJobExec.endDate.getTime() > endDate.getTime() ? tJobExec.endDate : endDate;
        }
      }

      // For each logs (test_default_log, sut..., tss_eus....)
      for (let log of allLogs) {
        if (log.stream === undefined || log.stream === null || log.stream === '') {
          log.stream = defaultStreamMap.log;
        }

        this.hide = false;
        this.addMoreLogsComparisons(monitoringIndicesList, startDate, endDate, log.name, log.stream, log.component);
      }
    }
  }

  alreadyExistComparison(logName: string, comparison: LogComparisonModel): boolean {
    if (this.model.has(logName) && this.model.get(logName) !== undefined) {
      for (let log of this.model.get(logName)) {
        if (
          log.component === comparison.component &&
          log.stream === comparison.stream &&
          log.startDate === comparison.startDate &&
          log.endDate === comparison.endDate &&
          log.isSamePair(comparison.pair)
        ) {
          return true;
        }
      }
    }
    return false;
  }

  removeLogComparatorTab(logField: LogFieldModel): void {
    // Remove entire key-values pair
    if (logField && this.model && this.model.has(logField.name)) {
      this.model.delete(logField.name);
      this.keys = Array.from(this.model.keys());
      this.generateAIOLogsComparisonTab();
    }
  }

  cleanMap(): void {
    this.model = new Map();
    this.keys = [];
  }
}
