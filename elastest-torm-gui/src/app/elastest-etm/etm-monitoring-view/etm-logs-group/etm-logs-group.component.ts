import { LogsViewComponent } from '../../../shared/logs-view/logs-view.component';
import { ESRabLogModel } from '../../../shared/logs-view/models/es-rab-log-model';
import { ElastestESService } from '../../../shared/services/elastest-es.service';
import { ElastestRabbitmqService } from '../../../shared/services/elastest-rabbitmq.service';
import { LogFieldModel } from '../../../shared/logs-view/models/log-field-model';
import { TJobService } from '../../tjob/tjob.service';
import { components, defaultStreamMap } from '../../../shared/defaultESData-model';
import { TJobExecModel } from '../../../elastest-etm/tjob-exec/tjobExec-model';
import { TJobModel } from '../../tjob/tjob-model';
import { Component, Input, OnInit, QueryList, ViewChildren } from '@angular/core';
import { Observable, Subject, Subscription } from 'rxjs/Rx';

@Component({
  selector: 'etm-logs-group',
  templateUrl: './etm-logs-group.component.html',
  styleUrls: ['./etm-logs-group.component.scss']
})
export class EtmLogsGroupComponent implements OnInit {
  @ViewChildren(LogsViewComponent) logsViewComponents: QueryList<LogsViewComponent>;

  @Input()
  public live: boolean;
  @Input()
  tJob: TJobModel;
  @Input()
  tJobExec: TJobExecModel;

  logsList: ESRabLogModel[] = [];
  groupedLogsList: ESRabLogModel[][] = [];

  loaded: boolean = false;

  testLogsSubscription: Subscription;
  sutLogsSubscription: Subscription;

  selectedTraces: number[][] = [];

  constructor(
    private elastestESService: ElastestESService,
    private elastestRabbitmqService: ElastestRabbitmqService,
  ) { }

  ngOnInit() {
  }

  ngAfterViewInit(): void {
    if (this.live) {
      this.initObservables();
    }
  }

  initObservables(): void {
    // Get default Rabbit queues 
    let subjectMap: Map<string, Subject<string>> = this.elastestRabbitmqService.subjectMap;
    subjectMap.forEach((obs: Subject<string>, key: string) => {
      let subjectData: any = this.elastestRabbitmqService.getDataFromSubjectName(key);
      if (subjectData.streamType === 'log') {
        obs.subscribe((data) => this.updateLogsData(data, subjectData.component));
      }
    });
  }

  initLogsView(tJob: TJobModel, tJobExec: TJobExecModel): void {
    this.tJob = tJob;
    this.tJobExec = tJobExec;

    for (let log of this.tJob.execDashboardConfigModel.allLogsTypes.logsList) {
      if (log.activated) {
        let individualLogs: ESRabLogModel = new ESRabLogModel(this.elastestESService);
        individualLogs.name = this.capitalize(log.component) + ' Logs';
        individualLogs.type = log.component + 'logs';
        individualLogs.component = log.component;
        if (log.stream === undefined || log.stream === null || log.stream === '') {
          log.stream = defaultStreamMap.log;
        }
        individualLogs.stream = log.stream;
        individualLogs.hidePrevBtn = !this.live;
        individualLogs.monitoringIndex = this.tJobExec.monitoringIndex;
        if (!this.live) {
          individualLogs.getAllLogs();
        } else if (!this.isDefault(individualLogs)) {
          this.createSubjectAndSubscribe(individualLogs.component, log.stream, log.streamType);
        }
        this.logsList.push(individualLogs);
      }
    }
    this.createGroupedLogsList();
  }

  addMoreLogs(obj: any): boolean {
    let individualLogs: ESRabLogModel = new ESRabLogModel(this.elastestESService);
    individualLogs.name = this.capitalize(obj.component) + ' ' + this.capitalize(obj.stream) + ' Logs';
    individualLogs.type = obj.type;
    individualLogs.component = obj.component;
    individualLogs.stream = obj.stream;
    individualLogs.hidePrevBtn = !this.live;
    individualLogs.monitoringIndex = obj.monitoringIndex;
    individualLogs.traces = obj.data;
    if (!this.alreadyExist(individualLogs)) {
      this.logsList.push(individualLogs);
      this.createGroupedLogsList();
      let logField: LogFieldModel = new LogFieldModel(individualLogs.component, individualLogs.stream);
      this.tJob.execDashboardConfigModel.allLogsTypes.addLogFieldToList(logField.name, logField.component, logField.stream, true);
      if (this.live) {
        this.createSubjectAndSubscribe(individualLogs.component, obj.stream, obj.streamType);
      }
      return true;
    } else {
      return false;
    }
  }

  createSubjectAndSubscribe(component: string, stream: string, streamType: string): void {
    this.elastestRabbitmqService.createSubject(streamType, component, stream);
    let index: string = this.tJobExec.getCurrentESIndex(component);
    this.elastestRabbitmqService.createAndSubscribeToTopic(index, streamType, component, stream)
      .subscribe(
      (data) => this.updateLogsData(data, component, stream)
      );
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

  createGroupedArray(arr, chunkSize): any {
    let groups = [], i;
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
      if (found) { break; }
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
    // If is live, unsubscribe
    if (this.live) {
      let streamType: string = 'log';
      let index: string = this.tJobExec.getCurrentESIndex(component);

      this.elastestRabbitmqService.unsuscribeFromTopic(index, streamType, component, stream);
    }
    this.logsList.splice(pos, 1);
    this.createGroupedLogsList();
    let logField: LogFieldModel = new LogFieldModel(component, stream);
    this.tJob.execDashboardConfigModel.allLogsTypes.disableLogField(logField.name, logField.component, logField.stream);
  }

  isDefault(log: ESRabLogModel): boolean {
    return components.indexOf(log.component) > -1 && log.stream === defaultStreamMap.log;
  }

  loadLastTraces(): void {
    for (let log of this.logsList) {
      log.loadLastTraces();
    }
  }
}
