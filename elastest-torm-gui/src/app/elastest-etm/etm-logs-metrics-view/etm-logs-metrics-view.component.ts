import { Observable, Subject } from 'rxjs/Rx';
import { MonitoringConfigurationComponent } from './monitoring-configuration/monitoring-configuration.component';
import { TJobService } from '../tjob/tjob.service';
import { ElastestESService } from '../../shared/services/elastest-es.service';
import { TJobExecModel } from '../tjob-exec/tjobExec-model';
import { TJobModel } from '../tjob/tjob-model';
import { EtmLogsGroupComponent } from '../../shared/logs-view/etm-logs-group/etm-logs-group.component';
import {
  EtmComplexMetricsGroupComponent,
} from '../../shared/metrics-view/complex-metrics-view/etm-complex-metrics-group/etm-complex-metrics-group.component';
import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { MdDialog, MdDialogRef } from '@angular/material';

@Component({
  selector: 'etm-logs-metrics-view',
  templateUrl: './etm-logs-metrics-view.component.html',
  styleUrls: ['./etm-logs-metrics-view.component.scss']
})
export class EtmLogsMetricsViewComponent implements OnInit {
  @ViewChild('metricsGroup') metricsGroup: EtmComplexMetricsGroupComponent;
  @ViewChild('logsGroup') logsGroup: EtmLogsGroupComponent;

  @Input()
  public live: boolean;

  tJob: TJobModel;
  tJobExec: TJobExecModel;

  component: string = '';
  stream: string = '';
  metricName: string = '';

  constructor(
    private elastestESService: ElastestESService,
    private tJobService: TJobService,
    public dialog: MdDialog,
  ) { }

  ngOnInit() {
  }

  initView(tJob: TJobModel, tJobExec: TJobExecModel): void {
    this.tJob = tJob;
    this.tJobExec = tJobExec;

    // Load logs
    this.logsGroup.initLogsView(tJob, tJobExec);

    // Load metrics
    this.metricsGroup.initMetricsView(tJob, tJobExec);
  }

  timelineEvent($event): void {
    if (!$event.unselect) {
      this.logsGroup.selectTimeRange($event.domain);
    } else {
      this.logsGroup.unselectTimeRange();
    }
  }

  hoverEvent(time): void {
    this.logsGroup.selectTracesByTime(time);
  }

  leaveEvent(): void {
    this.logsGroup.unselectTraces();
  }

  addMore(withSave: boolean = false): void {
    this.addMoreSubscribe()
      .subscribe(
      (obj: any) => {
        this.addMoreFromObj(obj);
        if (withSave) {
          this.saveMonitoringConfig();
        }
      },
      (error) => console.log(error),
    );
  }

  addMoreFromObj(obj: any): void {
    if (obj.streamType === 'log') {
      this.logsGroup.addMoreLogs(obj);
    } else if (obj.streamType === 'composed_metrics') {
      this.metricsGroup.addMoreMetrics(obj);
    } else if (obj.streamType === 'atomic_metric') {
      this.metricsGroup.addMoreMetrics(obj);
    }

    this.component = '';
    this.stream = '';
    this.metricName = '';
  }

  addMoreSubscribe(): Observable<any> {
    let _addMoreSubject: Subject<any> = new Subject<any>();
    let addMoreObs: Observable<any> = _addMoreSubject.asObservable();

    if (this.isInit()) {
      this.elastestESService.searchAllDynamic(this.tJobExec.logIndex, this.stream, this.component, this.metricName)
        .subscribe(
        (obj: any) => {
          _addMoreSubject.next(obj);
        },
        (error) => _addMoreSubject.error('Could not load more')
        ,
      );
    } else {
      _addMoreSubject.error('Could not load more. EtmLogsMetricsView has not been init yet')
    }

    return addMoreObs;
  }

  isInit(): boolean {
    return this.tJobExec !== undefined;
  }

  saveMonitoringConfig(): void {
    this.tJobService.modifyTJob(this.tJob).subscribe(
      (data) => this.elastestESService.popupService.openSnackBar('Monitoring configuration saved into TJob', 'OK'),
      (error) => console.log(error)
    );
  }

  loadLastTraces(): void {
    this.logsGroup.loadLastTraces();
    this.metricsGroup.loadLastTraces();
  }

  public openMonitoringConfig(): void {
    let dialogRef: MdDialogRef<MonitoringConfigurationComponent> = this.dialog.open(MonitoringConfigurationComponent, {
      data: { exec: this.tJobExec, logCards: this.logsGroup, metricCards: this.metricsGroup },
      height: '80%',
      width: '90%',
    });
    dialogRef.afterClosed()
      .subscribe(
      (data: any) => {
        if (data) {
          let withSave: boolean = false;
          if (data.withSave) {
            withSave = data.withSave;
          }
          if (data.logsList) {
            this.updateLogsFromList(data.logsList, withSave);
          }
          if (data.metricsList) {
            this.updateMetricsFromList(data.metricsList, withSave);
          }
        }
      },
    );
  }

  updateLogsFromList(logsList: any[], withSave: boolean): void {
    for (let log of logsList) {
      if (log.activated) {
        this.component = log.component;
        this.stream = log.stream;
        this.metricName = '';
        this.addMore(withSave);
      } else { // Remove
        this.removeLogCard(log, withSave);
      }
    }
  }

  removeLogCard(log: any, withSave: boolean = false): void {
    let position: number = 0;
    for (let logCard of this.logsGroup.logsList) {
      if (logCard.component === log.component && logCard.stream === log.stream) {
        this.logsGroup.removeAndUnsubscribe(position);
        break;
      }
      position++;
    }
    if (withSave) {
      this.saveMonitoringConfig();
    }
  }
  updateMetricsFromList(metricsList: any[], withSave: boolean): void {
    for (let metric of metricsList) {
      if (metric.activated) {
        this.component = metric.component;
        this.stream = metric.stream;
        this.metricName = metric.metricName;
        this.addMore(withSave);
      } else { // Remove
        this.removeMetricCard(metric, withSave);
      }
    }
  }

  removeMetricCard(metric: any, withSave: boolean = false): void {
    let position: number = 0;
    for (let metricCard of this.metricsGroup.metricsList) {
      if (metricCard.component === metric.component && metricCard.stream === metric.stream) {
        this.metricsGroup.removeAndUnsubscribe(position);
        break;
      }
      position++;
    }
    if (withSave) {
      this.saveMonitoringConfig();
    }
  }
}
