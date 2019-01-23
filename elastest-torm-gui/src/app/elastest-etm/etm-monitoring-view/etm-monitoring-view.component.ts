import { ExternalService } from '../external/external.service';
import { EtmLogsGroupComponent } from './etm-logs-group/etm-logs-group.component';
import { MonitoringConfigurationComponent } from './monitoring-configuration/monitoring-configuration.component';
import { EtmChartGroupComponent } from './etm-chart-group/etm-chart-group.component';
import { Observable, Subject } from 'rxjs/Rx';
import { TJobService } from '../tjob/tjob.service';

import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material';
import { AbstractTJobModel } from '../models/abstract-tjob-model';
import { AbstractTJobExecModel } from '../models/abstract-tjob-exec-model';
import { TJobModel } from '../tjob/tjob-model';
import { ExternalTJobModel } from '../external/external-tjob/external-tjob-model';
import { MonitoringService } from '../../shared/services/monitoring.service';
import { TJobExecModel } from '../tjob-exec/tjobExec-model';
import { LogAnalyzerService } from '../../elastest-log-analyzer/log-analyzer.service';
import { MonitorMarkModel } from './monitor-mark.model';
import { sleep, asyncSleep } from '../../shared/utils';

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
  isInitialized: boolean = false;
  isMultiParentTJobExec: boolean = false;

  component: string = '';
  stream: string = '';
  metricName: string = '';
  etMonitorMarkPrefix: string = '##elastest-monitor-mark: ';
  lockForMarkPrefix: boolean = false;

  constructor(
    private monitoringService: MonitoringService,
    private externalService: ExternalService,
    private logAnalyzerService: LogAnalyzerService,
    private tJobService: TJobService,
    public dialog: MatDialog,
  ) {}

  ngOnInit() {}

  initView(tJob: AbstractTJobModel, tJobExec: AbstractTJobExecModel): void {
    this.tJob = tJob;
    this.tJobExec = tJobExec;

    this.isMultiParentTJobExec =
      tJobExec !== undefined &&
      tJobExec.finished() &&
      tJobExec instanceof TJobExecModel &&
      tJobExec.isParent() &&
      tJobExec.execChilds.length > 0;

    // Init Monitor Marks first if there are
    this.initMonitorMarks();

    // If !Multi Parent init directly. Else, init after monitor marks loaded
    if (!this.isMultiParentTJobExec) {
      this.initLogAndMetricViews();
    }
  }

  initLogAndMetricViews(parentTJobExec: AbstractTJobExecModel = this.tJobExec): void {
    this.tJobExec = parentTJobExec;

    if (!this.isInitialized) {
      // Load logs
      this.logsGroup.initLogsView(this.tJob, this.tJobExec);

      // Load metrics
      this.metricsGroup.initMetricsView(this.tJob, this.tJobExec);

      this.isInitialized = true;
    }
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

  // Adds new monitoring card
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

  // Gets data of the new monitoring card to be added
  addMoreSubscribe(): Observable<any> {
    let _addMoreSubject: Subject<any> = new Subject<any>();
    let addMoreObs: Observable<any> = _addMoreSubject.asObservable();

    if (this.isInit()) {
      let monitoringIndex: string = this.tJobExec.monitoringIndex;

      let searchAllObs: Observable<any>;
      // If Multi Parent
      if (this.tJobExec instanceof TJobExecModel && this.tJobExec.isParent()) {
        monitoringIndex = this.tJobExec.getChildsMonitoringIndices();
        searchAllObs = this.monitoringService.searchAllDynamic(
          monitoringIndex,
          this.stream,
          this.component,
          this.metricName,
          this.tJobExec,
        );
      } else {
        searchAllObs = this.monitoringService.searchAllDynamic(monitoringIndex, this.stream, this.component, this.metricName);
      }

      searchAllObs.subscribe(
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

  // Adds data of the new monitoring card to be added
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
  isInit(): boolean {
    return this.tJobExec !== undefined;
  }

  // Persists monitoring config into tJob
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
    let dialogRef: MatDialogRef<MonitoringConfigurationComponent> = this.dialog.open(MonitoringConfigurationComponent, {
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

  /* ********************** */
  /* *** Monitor Marks ***  */
  /* ********************** */

  waitForUnlock(functionsToExec: Function[], parentTJobExec: AbstractTJobExecModel): void {
    // sleep
    sleep(500)
      .then(() => {
        if (this.lockForMarkPrefix) {
          this.waitForUnlock(functionsToExec, parentTJobExec);
        } else {
          if (functionsToExec.length > 0) {
            functionsToExec.shift()();
            this.waitForUnlock(functionsToExec, parentTJobExec);
          } else {
            this.initLogAndMetricViews(parentTJobExec);
          }
        }
      })
      .catch((e) => {
        this.waitForUnlock(functionsToExec, parentTJobExec);
      });
  }

  initMonitorMarks(tJobExec: AbstractTJobExecModel = this.tJobExec): AbstractTJobExecModel {
    if (tJobExec !== undefined && tJobExec.finished()) {
      if (tJobExec instanceof TJobExecModel && tJobExec.isParent() && tJobExec.execChilds.length > 0) {
        let childPos: number = 0;
        let functionsToExec: Function[] = [];
        for (let child of tJobExec.execChilds) {
          let currentChildPos: number = childPos;
          functionsToExec.push(() => {
            tJobExec.execChilds[currentChildPos] = this.initMonitorMarks(child) as TJobExecModel;
          });

          childPos++;
        }
        this.waitForUnlock(functionsToExec, tJobExec);
      } else {
        this.lockForMarkPrefix = true;
        this.logAnalyzerService
          .searchTraceByGivenMsg(
            this.etMonitorMarkPrefix,
            tJobExec.getMonitoringIndexAsList(),
            tJobExec.startDate,
            tJobExec.endDate,
          )
          .subscribe(
            (monitorMarkTraces: any[]) => {
              for (let markTrace of monitorMarkTraces) {
                let msg: string = markTrace.message;
                let timestamp: string = markTrace['@timestamp'];
                let markModel: MonitorMarkModel = new MonitorMarkModel();
                markModel.initByGivenMsg(msg, timestamp);

                if (!markModel.isEmpty()) {
                  // Use addMonitoringMark instead of push
                  tJobExec.addMonitoringMark(markModel);
                }
              }
              this.lockForMarkPrefix = false;
            },
            (error: Error) => {
              console.log(error);
              this.lockForMarkPrefix = false;
            },
          );
      }

      if (tJobExec instanceof TJobExecModel && tJobExec.isChild()) {
        // Do nothing
      } else {
        this.tJobExec = tJobExec;
      }
      return tJobExec;
    }
  }
}
