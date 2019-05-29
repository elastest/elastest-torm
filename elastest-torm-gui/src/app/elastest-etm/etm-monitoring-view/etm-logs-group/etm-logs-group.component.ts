import { LogsViewComponent } from '../../../shared/logs-view/logs-view.component';
import { ESRabLogModel } from '../../../shared/logs-view/models/es-rab-log-model';
import { ElastestRabbitmqService } from '../../../shared/services/elastest-rabbitmq.service';
import { LogFieldModel } from '../../../shared/logs-view/models/log-field-model';
import { components, defaultStreamMap } from '../../../shared/defaultESData-model';
import { Component, Input, OnInit, QueryList, ViewChildren } from '@angular/core';
import { Subscription } from 'rxjs/Rx';
import { AbstractTJobModel } from '../../models/abstract-tjob-model';
import { AbstractTJobExecModel } from '../../models/abstract-tjob-exec-model';
import { ExternalTJobExecModel } from '../../external/external-tjob-execution/external-tjob-execution-model';
import { TJobExecModel } from '../../tjob-exec/tjobExec-model';
import { MonitoringService } from '../../../shared/services/monitoring.service';
import { TJobModel } from '../../tjob/tjob-model';
import { LogComparisonModel } from '../../../elastest-log-comparator/model/log-comparison.model';
import { allArrayPairCombinations, isStringIntoArray } from '../../../shared/utils';

@Component({
  selector: 'etm-logs-group',
  templateUrl: './etm-logs-group.component.html',
  styleUrls: ['./etm-logs-group.component.scss'],
})
export class EtmLogsGroupComponent implements OnInit {
  @ViewChildren(LogsViewComponent)
  logsViewComponents: QueryList<LogsViewComponent>;

  @Input()
  public live: boolean;
  @Input()
  tJob: AbstractTJobModel;
  @Input()
  tJobExec: AbstractTJobExecModel;

  logsList: ESRabLogModel[] = [];
  groupedLogsList: ESRabLogModel[][] = [];

  logsComparisonMap: Map<string, LogComparisonModel[]> = new Map<string, LogComparisonModel[]>();
  logsComparisonKeys: string[] = [];
  aioKey: string = 'AIO';

  subscriptions: Map<string, Subscription> = new Map();

  loaded: boolean = false;

  selectedTraces: number[][] = [];

  constructor(private monitoringService: MonitoringService, private elastestRabbitmqService: ElastestRabbitmqService) {}

  ngOnInit(): void {}

  // When a log card is already activated
  initLogsView(tJob: AbstractTJobModel, tJobExec: AbstractTJobExecModel): void {
    this.tJob = tJob;
    this.tJobExec = tJobExec;

    for (let log of this.tJob.execDashboardConfigModel.allLogsTypes.logsList) {
      if (log.activated) {
        if (log.stream === undefined || log.stream === null || log.stream === '') {
          log.stream = defaultStreamMap.log;
        }

        if (tJobExec instanceof TJobExecModel && tJobExec.isParent()) {
          this.addMoreLogsComparisons(tJobExec, log.name, log.stream, log.component);
        } else {
          let individualLogs: ESRabLogModel = new ESRabLogModel(this.monitoringService);
          individualLogs.name = this.capitalize(log.component) + ' Logs';
          individualLogs.etType = log.component + 'logs';
          individualLogs.component = log.component;
          individualLogs.stream = log.stream;
          individualLogs.hidePrevBtn = !this.live;
          individualLogs.monitoringIndex = this.tJobExec.monitoringIndex;
          individualLogs.startDate = this.tJobExec.startDate;
          individualLogs.endDate = this.tJobExec.endDate;

          if (!this.live) {
            individualLogs.getAllLogs(this.tJobExec.startDate, this.tJobExec.endDate);
          } else {
            if (log.component !== 'sut') {
              this.createSubjectAndSubscribe(individualLogs.component, log.stream, log.streamType);
            } else if (tJob.hasSut() || (tJob instanceof TJobModel && tJob.external && !tJob.hasSut())) {
              this.createSubjectAndSubscribe(individualLogs.component, log.stream, log.streamType);
            }
          }
          this.logsList.push(individualLogs);
        }
      }
    }
    this.createGroupedLogsList();
  }

  // Added manually
  addMoreLogs(obj: any): boolean {
    let individualLogs: ESRabLogModel = new ESRabLogModel(this.monitoringService);
    individualLogs.name = this.capitalize(obj.component) + ' ' + this.capitalize(obj.stream) + ' Logs';
    individualLogs.etType = obj.etType;
    individualLogs.component = obj.component;
    individualLogs.stream = obj.stream;
    individualLogs.hidePrevBtn = !this.live;
    individualLogs.monitoringIndex = obj.monitoringIndex;
    individualLogs.traces = obj.data ? obj.data : [];
    if (!this.alreadyExist(individualLogs)) {
      this.logsList.push(individualLogs);
      this.createGroupedLogsList();

      if (this.live) {
        this.createSubjectAndSubscribe(individualLogs.component, obj.stream, obj.streamType);
      }
      return true;
    } else {
      return false;
    }
  }

  createSubjectAndSubscribe(component: string, stream: string, streamType: string): void {
    let index: string = this.getAbstractTJobExecIndex(component);
    if (index) {
      let key: string = this.elastestRabbitmqService.getDestinationFromData(index, streamType, component, stream);
      this.elastestRabbitmqService.createSubject(streamType, component, stream);
      let subscription: Subscription = this.elastestRabbitmqService
        .createAndSubscribeToTopic(index, streamType, component, stream)
        .subscribe((data) => this.updateLogsData(data, component, stream));

      this.subscriptions.set(key, subscription);
    }
  }

  alreadyExist(newLog: ESRabLogModel): boolean {
    for (let log of this.logsList) {
      if (log.component === newLog.component && log.stream === newLog.stream) {
        return true;
      }
    }
    return false;
  }

  createGroupedLogsList(): void {
    let defaultGroupNum: number = 2;
    this.groupedLogsList = this.createGroupedArray(this.logsList, defaultGroupNum);
  }

  createGroupedArray(arr: ESRabLogModel[], chunkSize: number): ESRabLogModel[][] {
    let groups: ESRabLogModel[][] = [];
    let i: number = 0;
    for (i = 0; i < arr.length; i += chunkSize) {
      groups.push(arr.slice(i, i + chunkSize));
    }
    return groups;
  }

  capitalize(value: any): any {
    if (value) {
      return value.charAt(0).toUpperCase() + value.slice(1);
    }
    return value;
  }

  updateLogsData(data: any, component: string, stream: string = defaultStreamMap.log): void {
    let found: boolean = false;
    for (let group of this.groupedLogsList) {
      for (let log of group) {
        if (log.component === component && log.stream === stream) {
          if (log.traces.length >= log.maxsize) {
            log.traces.splice(0, 1);
          }
          log.traces.push(data);
          found = true;
          break;
        }
      }
      if (found) {
        break;
      }
    }
  }

  selectTimeRange(domain): void {
    for (let group of this.groupedLogsList) {
      for (let log of group) {
        log.selectTimeRange(domain);
      }
    }
  }

  unselectTimeRange(): void {
    for (let group of this.groupedLogsList) {
      for (let log of group) {
        log.unselectTimeRange();
      }
    }
  }

  selectTracesByTime(time): void {
    this.unselectTraces();
    let logPos: number = 0;
    for (let group of this.groupedLogsList) {
      for (let log of group) {
        this.selectTrace(logPos, log.getTracePositionByTime(time));
        logPos++;
      }
    }
  }

  selectTrace(logPos: number, tracePos: number): void {
    if (tracePos >= 0) {
      if (this.logsViewComponents.toArray() && this.logsViewComponents.toArray().length > 0) {
        let logsView: LogsViewComponent = this.logsViewComponents.toArray()[logPos];
        logsView.scrollToElement(tracePos);
        let tracesList: NodeListOf<HTMLLIElement> = logsView.getElementsList();
        tracesList[tracePos].style.backgroundColor = '#ffac2f';
        this.selectedTraces.push([logPos, tracePos]);
      }
    }
  }

  unselectTraces(): void {
    if (this.selectedTraces.length > 0 && this.logsViewComponents.toArray() && this.logsViewComponents.toArray().length > 0) {
      let logsViewList: LogsViewComponent[] = this.logsViewComponents.toArray();

      for (let selected of this.selectedTraces) {
        let tracesList: NodeListOf<HTMLLIElement> = logsViewList[selected[0]].getElementsList();
        if (tracesList[selected[1]] !== undefined) {
          tracesList[selected[1]].style.removeProperty('background-color');
        }
      }
    }
    this.selectedTraces = [];
  }

  removeAndUnsubscribe(pos: number): void {
    let component: string = this.logsList[pos].component;
    let stream: string = this.logsList[pos].stream;
    let name: string = this.logsList[pos].name;
    let streamType: string = 'log';

    // If is live, unsubscribe
    if (this.live) {
      let index: string = this.getAbstractTJobExecIndex(component);

      this.elastestRabbitmqService.unsuscribeFromTopic(index, streamType, component, stream);

      let key: string = this.elastestRabbitmqService.getDestinationFromData(index, streamType, component, stream);
      if (this.subscriptions.has(key)) {
        this.subscriptions.get(key).unsubscribe();
        this.subscriptions.delete(key);
      }
    }
    this.logsList.splice(pos, 1);
    this.createGroupedLogsList();
  }

  getAbstractTJobExecIndex(component: string): string {
    let index: string;
    switch (this.tJobExec.getAbstractTJobExecClass()) {
      case 'ExternalTJobExecModel':
        let externalTJobExec: ExternalTJobExecModel = this.tJobExec as ExternalTJobExecModel;
        index = externalTJobExec.getCurrentMonitoringIndex(component);
        break;
      case 'TJobExecModel':
        let tJobExec: TJobExecModel = this.tJobExec as TJobExecModel;
        index = tJobExec.getCurrentMonitoringIndex(component);
        break;
      default:
        // Abstract
        break;
    }
    return index;
  }

  isDefault(log: ESRabLogModel): boolean {
    return components.indexOf(log.component) > -1 && log.stream === defaultStreamMap.log;
  }

  loadLastTraces(): void {
    for (let log of this.logsList) {
      log.loadLastTraces();
    }
  }

  // Comparison

  addMoreLogsComparison(logName: string, logComparison: LogComparisonModel): boolean {
    if (!this.alreadyExistComparison(logName, logComparison)) {
      if (!this.logsComparisonMap.has(logName)) {
        this.logsComparisonMap.set(logName, []);
        this.logsComparisonKeys = Array.from(this.logsComparisonMap.keys());
      }

      this.logsComparisonMap.get(logName).push(logComparison);
      return true;
    } else {
      return false;
    }
  }

  addMoreLogsComparisons(tJobExec: AbstractTJobExecModel, logName: string, stream: string, component: string): boolean {
    let added: boolean = false;
    if (tJobExec instanceof TJobExecModel && tJobExec.isParent()) {
      let monitoringIndicesList: string[] = tJobExec.getChildsMonitoringIndicesList();
      if (monitoringIndicesList.length > 1) {
        let pairCombinations: string[][] = allArrayPairCombinations(monitoringIndicesList);

        for (let pair of pairCombinations) {
          let logComparison: LogComparisonModel = new LogComparisonModel();
          logComparison.name = pair.join(' | ');
          logComparison.component = component;
          logComparison.stream = stream;
          logComparison.startDate = tJobExec.startDate;
          logComparison.endDate = tJobExec.endDate;
          logComparison.pair = pair;

          added = this.addMoreLogsComparison(logName, logComparison) || added;
        }
      }
    }
    if (added) {
      this.generateAIOLogsComparisonTab();
    }
    return added;
  }

  alreadyExistComparison(logName: string, comparison: LogComparisonModel): boolean {
    if (this.logsComparisonMap.has(logName) && this.logsComparisonMap.get(logName) !== undefined) {
      for (let log of this.logsComparisonMap.get(logName)) {
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
    if (logField && this.logsComparisonMap && this.logsComparisonMap.has(logField.name)) {
      this.logsComparisonMap.delete(logField.name);
      this.logsComparisonKeys = Array.from(this.logsComparisonMap.keys());
      this.generateAIOLogsComparisonTab();
    }
  }

  removeLogComparatorCard(): void {
    this.logsComparisonMap = new Map();
    this.logsComparisonKeys = [];
  }

  generateAIOLogsComparisonTab(): void {
    if (this.logsComparisonMap) {
      // Has only 1 (AIO comparison tab) or two (1 normal and 1 AIO)
      if (this.logsComparisonMap.size > 0 && this.logsComparisonMap.size < 3 && this.logsComparisonMap.has(this.aioKey)) {
        // 1 or 2 and has AIO
        if (this.logsComparisonMap.size > 0) {
          this.logsComparisonMap.delete(this.aioKey);
          this.logsComparisonKeys = Array.from(this.logsComparisonMap.keys());
        }
      } else {
        if (this.logsComparisonMap.size > 1) {
          if (this.logsComparisonMap.has(this.aioKey)) {
            this.logsComparisonMap.delete(this.aioKey);
            this.logsComparisonKeys = Array.from(this.logsComparisonMap.keys());
          }

          let componentsList: string[] = [];

          for (let key of this.logsComparisonKeys) {
            let currentLogComparisonList: LogComparisonModel[] = this.logsComparisonMap.get(key);
            if (currentLogComparisonList.length > 0) {
              let currentComponent: string = currentLogComparisonList[0].component;

              if (!isStringIntoArray(currentComponent, componentsList)) {
                componentsList.push(currentComponent);
              }
            }
          }

          let firstLogComparisonList: LogComparisonModel[] = Array.from(this.logsComparisonMap.values())[0];
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

  getErrors(): number {
    let errors: number = 0;
    for (let log of this.logsList) {
      errors += log.getErrors().length;
    }

    return errors;
  }

  getWarnings(): number {
    let warns: number = 0;
    for (let log of this.logsList) {
      warns += log.getWarnings().length;
    }

    return warns;
  }
}
