import { ExternalService } from '../external/external.service';
import { EtmLogsGroupComponent } from './etm-logs-group/etm-logs-group.component';
import { MonitoringConfigurationComponent } from './monitoring-configuration/monitoring-configuration.component';
import { EtmChartGroupComponent } from './etm-chart-group/etm-chart-group.component';
import { Observable, Subject } from 'rxjs/Rx';
import { TJobService } from '../tjob/tjob.service';

import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { MdDialog, MdDialogRef } from '@angular/material';
import { AbstractTJobModel } from '../models/abstract-tjob-model';
import { AbstractTJobExecModel } from '../models/abstract-tjob-exec-model';
import { TJobModel } from '../tjob/tjob-model';
import { ExternalTJobModel } from '../external/external-tjob/external-tjob-model';
import { MonitoringService } from '../../shared/services/monitoring.service';
import { TJobExecModel } from '../tjob-exec/tjobExec-model';

@Component({
  selector: 'etm-monitoring-view',
  templateUrl: './etm-monitoring-view.component.html',
  styleUrls: ['./etm-monitoring-view.component.scss'],
})
export class EtmMonitoringViewComponent implements OnInit {
  @ViewChild('metricsGroup')
  metricsGroup: EtmChartGroupComponent;
  @ViewChild('logsGroup')
  logsGroup: EtmLogsGroupComponent;

  @Input()
  public live: boolean;

  @Input()
  public showConfigBtn: boolean;

  tJob: AbstractTJobModel;
  tJobExec: AbstractTJobExecModel;

  component: string = '';
  stream: string = '';
  metricName: string = '';

  constructor(
    private monitoringService: MonitoringService,
    private externalService: ExternalService,
    private tJobService: TJobService,
    public dialog: MdDialog,
  ) {}

  ngOnInit() {}

  initView(tJob: AbstractTJobModel, tJobExec: AbstractTJobExecModel): void {
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
    this.addMoreSubscribe().subscribe(
      (obj: any) => {
        let added: boolean = this.addMoreFromObj(obj);
        if (showPopup) {
          if (added) {
            this.monitoringService.popupService.openSnackBar('Added succesfully!', 'OK');
          } else {
            this.monitoringService.popupService.openSnackBar('Already exist', 'OK');
          }
        }
        if (withSave) {
          this.saveMonitoringConfig(showPopup);
        }
      },
      (error: Error) => console.log(error),
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
      let monitoringIndex: string = this.tJobExec.monitoringIndex;
      // If Multi Parent
      if (this.tJobExec instanceof TJobExecModel && this.tJobExec.isParent()) {
        monitoringIndex = this.tJobExec.getChildsMonitoringIndices();
      }

      this.monitoringService.searchAllDynamic(monitoringIndex, this.stream, this.component, this.metricName).subscribe(
        (obj: any) => {
          _addMoreSubject.next(obj);
        },
        (error: Error) => _addMoreSubject.error('Could not load more: ' + error),
      );
    } else {
      _addMoreSubject.error('Could not load more. EtmMonitoringViewComponent has not been init yet');
    }

    return addMoreObs;
  }

  isInit(): boolean {
    return this.tJobExec !== undefined;
  }

  saveMonitoringConfig(showPopup: boolean = true): void {
    switch (this.tJob.getAbstractTJobClass()) {
      case 'TJobModel':
        let tJobModel: TJobModel = this.tJob as TJobModel;
        this.tJobService.modifyTJob(tJobModel).subscribe(
          (data) => {
            if (showPopup) {
              this.monitoringService.popupService.openSnackBar('Monitoring configuration saved into TJob', 'OK');
            }
          },
          (error: Error) => console.log(error),
        );
        break;
      case 'ExternalTJobModel':
        let externalTJobModel: ExternalTJobModel = this.tJob as ExternalTJobModel;
        this.externalService.modifyExternalTJob(externalTJobModel).subscribe(
          (data) => {
            if (showPopup) {
              this.monitoringService.popupService.openSnackBar('Monitoring configuration saved into TJob', 'OK');
            }
          },
          (error: Error) => console.log(error),
        );
        break;

      default:
        // Abstract
        break;
    }
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
    dialogRef.afterClosed().subscribe((data: any) => {
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
        this.monitoringService.popupService.openSnackBar(msg);
      }
    });
  }

  updateLogsFromList(logsList: any[], withSave: boolean): void {
    for (let log of logsList) {
      if (log.activated) {
        this.updateLog(log, withSave);
      } else {
        // Remove
        this.removeLogCard(log);
        if (withSave) {
          this.saveMonitoringConfig(false);
        }
      }
    }
  }

  public updateLog(log: any, withSave: boolean, showPopup: boolean = false): void {
    this.component = log.component;
    this.stream = log.stream;
    this.metricName = '';
    this.addMore(withSave, showPopup);
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
        this.updateMetric(metric, withSave);
      } else {
        // Remove
        this.removeMetricCard(metric);
        if (withSave) {
          this.saveMonitoringConfig(false);
        }
      }
    }
  }

  updateMetric(metric: any, withSave: boolean, showPopup: boolean = false): void {
    this.component = metric.component;
    this.stream = metric.stream;
    this.metricName = metric.metricName;
    this.addMore(withSave, showPopup);
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
