import { DefaultESFieldModel, defaultStreamMap } from '../../defaultESData-model';
import { ElastestRabbitmqService } from '../../services/elastest-rabbitmq.service';
import { TJobExecModel } from '../../../elastest-etm/tjob-exec/tjobExec-model';
import { TJobModel } from '../../../elastest-etm/tjob/tjob-model';
import { ElastestESService } from '../../services/elastest-es.service';
import { ESRabLogModel } from '../models/es-rab-log-model';
import { LogsViewComponent } from '../logs-view.component';
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

  logsList: ESRabLogModel[] = [];
  groupedLogsList: ESRabLogModel[][] = [];

  loaded: boolean = false;

  testLogsSubscription: Subscription;
  sutLogsSubscription: Subscription;

  selectedTraces: number[][] = [];

  tJob: TJobModel;
  tJobExec: TJobExecModel;

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

  initObservables() {
    // Get default Rabbit queues 
    let subjectMap: Map<string, Subject<string>> = this.elastestRabbitmqService.subjectMap;
    subjectMap.forEach((obs: Subject<string>, key: string) => {
      let subjectData: any = this.elastestRabbitmqService.getDataFromSubjectName(key);
      if (subjectData.traceType === 'log') {
        obs.subscribe((data) => this.updateLogsData(data, subjectData.componentType));
      }
    });
  }

  initLogsView(tJob: TJobModel, tJobExec: TJobExecModel) {
    this.tJob = tJob;
    this.tJobExec = tJobExec;

    for (let log of tJob.execDashboardConfigModel.allLogsTypes.logsList) {
      if (log.activated) {
        let individualLogs: ESRabLogModel = new ESRabLogModel(this.elastestESService);
        individualLogs.name = this.capitalize(log.componentType) + ' Logs';
        individualLogs.type = log.componentType + 'logs';
        individualLogs.componentType = log.componentType;
        if (log.stream === undefined || log.stream === null || log.stream === '') {
          log.stream = defaultStreamMap.log;
        }
        individualLogs.stream = log.stream;
        individualLogs.hidePrevBtn = !this.live;
        individualLogs.logIndex = tJobExec.logIndex;
        if (!this.live) {
          individualLogs.getAllLogs();
        }
        this.logsList.push(individualLogs);
      }
    }
    this.createGroupedLogsList();
  }

  addMoreLogs(obj: any) {
    let individualLogs: ESRabLogModel = new ESRabLogModel(this.elastestESService);
    individualLogs.name = this.capitalize(obj.componentType) + ' ' + this.capitalize(obj.stream) + ' Logs';
    individualLogs.type = obj.type;
    individualLogs.componentType = obj.componentType;
    individualLogs.stream = obj.stream;
    individualLogs.hidePrevBtn = !this.live;
    individualLogs.logIndex = obj.logIndex;
    individualLogs.traces = obj.data;
    if (!this.alreadyExist(individualLogs.name)) {
      this.logsList.push(individualLogs);
      this.createGroupedLogsList();
      this.elastestESService.popupService.openSnackBar('Log added', 'OK');
      if (this.live) {
        this.elastestRabbitmqService.createSubject(obj.traceType, individualLogs.componentType, obj.stream);
        this.elastestRabbitmqService.createAndSubscribeToTopic(this.tJobExec, obj.traceType, individualLogs.componentType, obj.stream)
          .subscribe(
          (data) => this.updateLogsData(data, individualLogs.componentType, individualLogs.stream)
          );
      }
    } else {
      this.elastestESService.popupService.openSnackBar('Already exist', 'OK');
    }
  }

  alreadyExist(name: string) {
    for (let log of this.logsList) {
      if (log.name === name) {
        return true;
      }
    }
    return false;
  }

  createGroupedLogsList() {
    let defaultGroupNum: number = 2;
    this.groupedLogsList = this.createGroupedArray(this.logsList, defaultGroupNum);
  }

  createGroupedArray(arr, chunkSize) {
    let groups = [], i;
    for (i = 0; i < arr.length; i += chunkSize) {
      groups.push(arr.slice(i, i + chunkSize));
    }
    return groups;
  }

  capitalize(value: any) {
    if (value) {
      return value.charAt(0).toUpperCase() + value.slice(1);
    }
    return value;
  }

  updateLogsData(data: any, componentType: string, stream: string = defaultStreamMap.log) {
    let found: boolean = false;
    for (let group of this.groupedLogsList) {
      for (let log of group) {
        if (log.componentType === componentType && log.stream === stream) {
          log.traces.push(data);
          found = true;
          break;
        }
      }
      if (found) { break; }
    }
  }

  selectTimeRange(domain) {
    for (let group of this.groupedLogsList) {
      for (let log of group) {
        log.selectTimeRange(domain);
      }
    }
  }

  unselectTimeRange() {
    for (let group of this.groupedLogsList) {
      for (let log of group) {
        log.unselectTimeRange();
      }
    }
  }

  selectTracesByTime(time) {
    this.unselectTraces();
    let logPos: number = 0;
    for (let group of this.groupedLogsList) {
      for (let log of group) {
        this.selectTrace(logPos, log.getTracePositionByTime(time));
        logPos++;
      }
    }
  }

  selectTrace(logPos: number, tracePos: number) {
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

  unselectTraces() {
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

  removeAndUnsubscribe(pos: number) {
    // If is live, unsubscribe
    if (this.live) {
      let traceType: string = 'log';
      let componentType: string = this.logsList[pos].componentType;
      let stream: string = this.logsList[pos].stream;

      this.elastestRabbitmqService.unsuscribeFromTopic(this.tJobExec, traceType, componentType, stream);
    }
    this.logsList.splice(pos, 1);
    this.createGroupedLogsList();
  }

}
