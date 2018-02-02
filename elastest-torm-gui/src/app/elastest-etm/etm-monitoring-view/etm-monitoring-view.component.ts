import { EtmLogsGroupComponent } from './etm-logs-group/etm-logs-group.component';
import { MonitoringConfigurationComponent } from './monitoring-configuration/monitoring-configuration.component';
import { EtmChartGroupComponent } from './etm-chart-group/etm-chart-group.component';
import { Observable, Subject } from 'rxjs/Rx';
import { TJobService } from '../tjob/tjob.service';
import { ElastestESService } from '../../shared/services/elastest-es.service';
import { TJobExecModel } from '../tjob-exec/tjobExec-model';
import { TJobModel } from '../tjob/tjob-model';

import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { MdDialog, MdDialogRef } from '@angular/material';

@Component({
  selector: 'etm-monitoring-view',
  templateUrl: './etm-monitoring-view.component.html',
  styleUrls: ['./etm-monitoring-view.component.scss']
})
export class EtmMonitoringViewComponent implements OnInit {
  @ViewChild('metricsGroup') metricsGroup: EtmChartGroupComponent;
  @ViewChild('logsGroup') logsGroup: EtmLogsGroupComponent;

  @Input()
  public live: boolean;

  @Input()
  public showConfigBtn: boolean;

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

  addMore(withSave: boolean = false, showPopup: boolean = true): void {
    this.addMoreSubscribe()
      .subscribe(
      (obj: any) => {
        let added: boolean = this.addMoreFromObj(obj);
        if (showPopup) {
          if (added) {
            this.elastestESService.popupService.openSnackBar('Added succesfully!', 'OK');
          } else {
            this.elastestESService.popupService.openSnackBar('Already exist', 'OK');
          }
        }
        if (withSave) {
          this.saveMonitoringConfig(showPopup);
        }
      },
      (error) => console.log(error),
    );
  }

  addMoreFromObj(obj: any): boolean {
    let added: boolean = false;
    if (obj.streamType === 'log') {
      added = this.logsGroup.addMoreLogs(obj);
    } else if (obj.streamType === 'composed_metrics' || obj.streamType === 'atomic_metric') {
      added = this.metricsGroup.addMoreMetrics(obj);
    }

    this.component = '';
    this.stream = '';
    this.metricName = '';

    return added;
  }

  addMoreSubscribe(): Observable<any> {
    let _addMoreSubject: Subject<any> = new Subject<any>();
    let addMoreObs: Observable<any> = _addMoreSubject.asObservable();

    if (this.isInit()) {
      this.elastestESService.searchAllDynamic(this.tJobExec.monitoringIndex, this.stream, this.component, this.metricName)
        .subscribe(
        (obj: any) => {
          _addMoreSubject.next(obj);
        },
        (error) => _addMoreSubject.error('Could not load more')
        ,
      );
    } else {
      _addMoreSubject.error('Could not load more. EtmMonitoringViewComponent has not been init yet')
    }

    return addMoreObs;
  }

  isInit(): boolean {
    return this.tJobExec !== undefined;
  }

  saveMonitoringConfig(showPopup: boolean = true): void {
    this.tJobService.modifyTJob(this.tJob).subscribe(
      (data) => {
        if (showPopup) {
          this.elastestESService.popupService.openSnackBar('Monitoring configuration saved into TJob', 'OK');
        }
      },
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
          let msg: string = 'Monitoring changes has been applied';
          if (data.withSave) {
            withSave = data.withSave;
            msg += ' and saved';
          }
          if (data.logsList) {
            this.updateLogsFromList(data.logsList, withSave);
          }
          if (data.metricsList) {
            this.updateMetricsFromList(data.metricsList, withSave);
          }
          this.elastestESService.popupService.openSnackBar(msg);
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
        this.addMore(withSave, false);
      } else { // Remove
        this.removeLogCard(log);
        if (withSave) {
          this.saveMonitoringConfig(false);
        }
      }
    }
  }

  removeLogCard(log: any): void {
    let position: number = 0;
    for (let logCard of this.logsGroup.logsList) {
      if (logCard.component === log.component && logCard.stream === log.stream) {
        this.logsGroup.removeAndUnsubscribe(position);
        break;
      }
      position++;
    }
  }
  updateMetricsFromList(metricsList: any[], withSave: boolean): void {
    for (let metric of metricsList) {
      if (metric.activated) {
        this.component = metric.component;
        this.stream = metric.stream;
        this.metricName = metric.metricName;
        this.addMore(withSave, false);
      } else { // Remove
        this.removeMetricCard(metric);
        if (withSave) {
          this.saveMonitoringConfig(false);
        }
      }
    }
  }

  removeMetricCard(metric: any): void {
    let position: number = 0;
    for (let metricCard of this.metricsGroup.metricsList) {
      if (metricCard.component === metric.component && metricCard.stream === metric.stream) {
        this.metricsGroup.removeAndUnsubscribe(position);
        break;
      }
      position++;
    }
  }
}
