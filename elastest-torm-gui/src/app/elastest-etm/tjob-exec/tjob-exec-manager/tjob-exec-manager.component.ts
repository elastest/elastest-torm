import { Observable, Subject } from 'rxjs/Rx';
import { TdDialogService } from '@covalent/core/dialogs/services/dialog.service';
import { IConfirmConfig } from '@covalent/core';
import { TitlesService } from '../../../shared/services/titles.service';
import { EtmMonitoringViewComponent } from '../../etm-monitoring-view/etm-monitoring-view.component';

import { ESRabLogModel } from '../../../shared/logs-view/models/es-rab-log-model';
import { ETRESMetricsModel } from '../../../shared/metrics-view/models/et-res-metrics-model';
import { ElastestESService, LogTraces } from '../../../shared/services/elastest-es.service';
import { TJobModel } from '../../tjob/tjob-model';
import { TJobService } from '../../tjob/tjob.service';
import { TJobExecModel } from '../tjobExec-model';
import { TJobExecService } from '../tjobExec.service';

import { Component, OnInit, ViewChild, ViewContainerRef } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { MdDialog } from '@angular/material';
import { SafeUrl } from '@angular/platform-browser';
import { Http } from '@angular/http';
import { timestamp } from 'rxjs/operators';

@Component({
  selector: 'app-tjob-exec-manager',
  templateUrl: './tjob-exec-manager.component.html',
  styleUrls: ['./tjob-exec-manager.component.scss'],
})
export class TjobExecManagerComponent implements OnInit {
  @ViewChild('logsAndMetrics') logsAndMetrics: EtmMonitoringViewComponent;

  tJobId: number;
  tJobExecId: number;
  tJobExec: TJobExecModel;
  tJob: TJobModel;

  statusIcon: any = {
    name: '',
    color: '',
  };

  // TJob Data
  testCaseColumns: any[] = [
    { name: 'id', label: 'Id' },
    { name: 'name', label: 'Name' },
    { name: 'time', label: 'Time (s)' },
    { name: 'failureMessage', label: 'Failure Msg' },
    { name: 'failureType', label: 'Failure Type' },
    { name: 'failureErrorLine', label: 'Failure Error Line' },
    { name: 'failureDetail', label: 'Failure Detail' },
  ];

  constructor(
    private titlesService: TitlesService,
    private tJobExecService: TJobExecService,
    private tJobService: TJobService,
    private elastestESService: ElastestESService,
    private route: ActivatedRoute,
    private router: Router,
    private _dialogService: TdDialogService,
    private _viewContainerRef: ViewContainerRef,
    public dialog: MdDialog,
  ) {
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params.subscribe((params: Params) => {
        this.tJobId = params.tJobId;
        this.tJobExecId = params.tJobExecId;
      });
    }
  }

  ngOnInit() {
    this.tJobExec = new TJobExecModel();
    this.loadTJobExec();
  }

  loadTJobExec(): void {
    this.tJobExecService.getTJobExecutionByTJobId(this.tJobId, this.tJobExecId).subscribe((tJobExec: TJobExecModel) => {
      this.tJobExec = tJobExec;

      this.statusIcon = this.tJobExec.getResultIcon();
      this.titlesService.setHeadTitle('Execution ' + this.tJobExec.id);
      this.titlesService.setPathName(this.router.routerState.snapshot.url);

      this.tJobService.getTJob(this.tJobId.toString()).subscribe(
        (tJob: TJobModel) => {
          this.tJob = tJob;
          if (!this.tJobExec.finished()) {
            this.router.navigate(['/projects', tJob.project.id, 'tjob', this.tJobId, 'tjob-exec', this.tJobExecId, 'dashboard'], {
              queryParams: { fromTJobManager: true },
            });
          } else {
            this.logsAndMetrics.initView(this.tJob, this.tJobExec);
          }
        },
        (error) => console.log(error),
      );
    });
  }

  viewTJob(): void {
    this.router.navigate(['/projects', this.tJob.project.id, 'tjob', this.tJobId]);
  }

  viewInLogAnalyzer(): void {
    this.router.navigate(['projects', this.tJob.project.id, 'tjob', this.tJob.id, 'tjob-exec', this.tJobExec.id, 'loganalyzer'], {
      queryParams: { tjob: this.tJob.id, exec: this.tJobExec.id },
    });
  }

  deleteTJobExec(): void {
    let iConfirmConfig: IConfirmConfig = {
      message: 'TJob Execution ' + this.tJobExec.id + ' will be deleted, do you want to continue?',
      disableClose: false,
      viewContainerRef: this._viewContainerRef,
      title: 'Confirm',
      cancelButton: 'Cancel',
      acceptButton: 'Yes, delete',
    };
    this._dialogService
      .openConfirm(iConfirmConfig)
      .afterClosed()
      .subscribe((accept: boolean) => {
        if (accept) {
          this.tJobExecService.deleteTJobExecution(this.tJob, this.tJobExec).subscribe(
            (exec) => {
              this.tJobExecService.popupService.openSnackBar(
                'TJob Execution Nº' + this.tJobExec.id + ' has been removed successfully!',
              );
              this.viewTJob();
            },
            (error) => {
              this.tJobExecService.popupService.openSnackBar('TJob Execution could not be deleted');
            },
          );
        }
      });
  }

  downloadAsJson(): void {
    let jsonObj: object = {
      tJobExec: this.tJobExec,
    };

    this.loadAllLogs().subscribe((logsTraces: LogTraces[]) => {
      jsonObj['logs'] = logsTraces;
      let objAsBlob: Blob = new Blob([JSON.stringify(jsonObj)], { type: 'text/json;charset=utf-8;' });

      let url: string = window.URL.createObjectURL(objAsBlob);
      let a: any = document.createElement('a');
      document.body.appendChild(a);
      a.setAttribute('style', 'display: none');
      a.href = url;
      a.download = 'execution.json';
      a.click();
      window.URL.revokeObjectURL(url);
      a.remove(); // remove the element
    });
  }

  loadAllLogs(): Observable<LogTraces[]> {
    let _logs: Subject<LogTraces[]> = new Subject<LogTraces[]>();
    let logsObs: Observable<LogTraces[]> = _logs.asObservable();
    let logs: LogTraces[] = [];
    this.elastestESService.getLogsTree(this.tJobExec).subscribe((logsComponentStreams: any[]) => {
      let allLogs: ESRabLogModel[] = [];
      for (let componentStream of logsComponentStreams) {
        for (let stream of componentStream.children) {
          let currentLog: ESRabLogModel = new ESRabLogModel(this.elastestESService);
          currentLog.component = componentStream.name;
          currentLog.stream = stream.name;
          currentLog.monitoringIndex = this.tJobExec.monitoringIndex;
          allLogs.push(currentLog);
        }
      }
      this.loadAllLogsByGiven(allLogs, _logs, logs);
    });
    return logsObs;
  }

  loadAllLogsByGiven(logsObjList: ESRabLogModel[], _logs: Subject<LogTraces[]>, logs: LogTraces[]): void {
    if (logsObjList.length > 0) {
      let currentLog: ESRabLogModel = logsObjList.shift();
      currentLog.getAllLogsSubscription().subscribe((data: any[]) => {
        let logTraces: LogTraces = new LogTraces();
        logTraces.name = currentLog.component + '-' + currentLog.stream;
        logTraces.traces = data;
        logs.push(logTraces);
        this.loadAllLogsByGiven(logsObjList, _logs, logs);
      });
    } else {
      _logs.next(logs);
    }
  }
}
