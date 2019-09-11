import { FilesService } from '../../../shared/services/files.service';
import { TdDialogService } from '@covalent/core';
import { IConfirmConfig } from '@covalent/core';
import { TitlesService } from '../../../shared/services/titles.service';
import { EtmMonitoringViewComponent } from '../../etm-monitoring-view/etm-monitoring-view.component';

import { TJobModel } from '../../tjob/tjob-model';
import { TJobService } from '../../tjob/tjob.service';
import { TJobExecModel } from '../tjobExec-model';
import { TJobExecService } from '../tjobExec.service';

import { Component, OnInit, ViewChild, ViewContainerRef, Input } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { MatDialog } from '@angular/material';
import { MetricTraces, LogTraces, MonitoringService } from '../../../shared/services/monitoring.service';
import { ParameterModel } from '../../parameter/parameter-model';
import { sleep } from '../../../shared/utils';
import { ElastestLogComparatorComponent } from '../../../elastest-log-comparator/elastest-log-comparator.component';

@Component({
  selector: 'etm-finished-tjob-exec-manager',
  templateUrl: './finished-tjob-exec-manager.component.html',
  styleUrls: ['./finished-tjob-exec-manager.component.scss'],
})
export class FinishedTjobExecManagerComponent implements OnInit {
  @ViewChild('logsAndMetrics', { static: true })
  logsAndMetrics: EtmMonitoringViewComponent;
  showLogsAndMetrics: boolean = false;

  @ViewChild('logComparator', { static: false })
  logComparator: ElastestLogComparatorComponent;

  @Input()
  tJobExec: TJobExecModel;

  tJobId: number;
  tJobExecId: number;
  tJob: TJobModel;
  downloading: boolean = false;
  deletingInProgress: boolean = false;

  tJobExecMultiConfigs: ParameterModel[] = [];
  tJobExecParameters: ParameterModel[] = [];

  globalInfoExpansionPanelOpened: boolean = false;
  logsAndMetricsAlreadyInitialized: boolean = false;

  lastTJobExecSuccess: TJobExecModel;

  stopSleep: boolean = false;

  statusIcon: any = {
    name: '',
    color: '',
  };

  // TJob Data
  testCaseColumns: any[] = [
    { name: 'id', label: 'Id', width: 80 },
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
    private monitoringService: MonitoringService,
    private route: ActivatedRoute,
    private router: Router,
    private _dialogService: TdDialogService,
    private _viewContainerRef: ViewContainerRef,
    private filesService: FilesService,
    public dialog: MatDialog,
  ) {}

  ngOnInit(): void {
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params.subscribe((params: Params) => {
        this.tJobId = params.tJobId;
        this.tJobExecId = params.tJobExecId;
        this.setTitle();

        if (this.tJobExec !== undefined && this.tJobExec !== null) {
          this.initAfterTJobExecLoaded();
        } else {
          this.tJobExec = new TJobExecModel();
          this.loadTJobExec();
        }
      });
    }
  }

  setTitle(): void {
    let title: string = 'Execution';

    if (this.tJobExec) {
      if (this.tJobExec.isChild()) {
        title = 'Configuration Execution';
      } else if (this.tJobExec.isParent()) {
        title = 'Multi-Config Execution';
      }
    }
    title += this.tJobExecId !== undefined ? ' ' + this.tJobExecId : '';
    this.titlesService.setHeadTitle(title);
  }

  loadTJobExec(): void {
    this.tJobExecService.getTJobExecutionByTJobId(this.tJobId, this.tJobExecId).subscribe((tJobExec: TJobExecModel) => {
      this.tJobExec = tJobExec;
      this.initAfterTJobExecLoaded();
    });
  }

  initAfterTJobExecLoaded(): void {
    this.statusIcon = this.tJobExec.getResultIcon();
    this.setTitle();
    this.initElasTestMonitoringMarks();
    this.titlesService.setPathName(this.router.routerState.snapshot.url);
    if (this.tJobExec.parameters) {
      for (let param of this.tJobExec.parameters) {
        let parameter: ParameterModel = new ParameterModel(param);
        if (param.multiConfig) {
          this.tJobExecMultiConfigs.push(parameter);
        } else {
          this.tJobExecParameters.push(parameter);
        }
      }
    }
    this.tJobService.getTJob(this.tJobId.toString()).subscribe(
      (tJob: TJobModel) => {
        this.tJob = tJob;
        if (this.tJobExec.finished()) {
          this.checkIfFailedAndInit();
          if (this.tJobExec.isChild()) {
            this.initLogsAndMetrics();
            this.tJobExecService.getChildTJobExecParent(this.tJobExec.id).subscribe(
              (parent: TJobExecModel) => {
                this.tJobExec.execParent = parent;
              },
              (error: Error) => console.log(error),
            );
          }
        }
      },
      (error: Error) => console.log(error),
    );
  }

  checkIfFailedAndInit(): void {
    if (this.tJobExec.result && this.tJobExec.isFailed()) {
      this.tJobExecService.getLastSuccessTJobExecution(this.tJob.id, false).subscribe(
        (lastTJobExecSuccess: TJobExecModel) => {
          if (lastTJobExecSuccess) {
            this.lastTJobExecSuccess = lastTJobExecSuccess;
            this.initLogComparator();
          }
        },
        (error: Error) => {
          console.error(error);
        },
      );
    }
  }

  initLogComparator(): void {
    let execsToCompare: TJobExecModel[] = [this.tJobExec, this.lastTJobExecSuccess];

    // retry
    if (this.logComparator) {
      this.logComparator.addMoreLogsComparisonsByTJobExecsList(
        execsToCompare,
        this.tJob.execDashboardConfigModel.allLogsTypes.logsList,
      );
    }
  }

  initLogsAndMetrics(): void {
    if (this.logsAndMetrics) {
      this.logsAndMetrics.initView(this.tJob, this.tJobExec);
      this.showLogsAndMetrics = true;
      this.logsAndMetricsAlreadyInitialized = true;
    }
  }

  initElasTestMonitoringMarks(): void {
    if (this.tJobExec.isParent()) {
      // TODO
    }
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
          this.deletingInProgress = true;
          this.tJobExecService.deleteTJobExecution(this.tJob, this.tJobExec).subscribe(
            (exec) => {
              this.deletingInProgress = false;
              this.tJobExecService.popupService.openSnackBar(
                'TJob Execution Nº' + this.tJobExec.id + ' has been removed successfully!',
              );
              this.viewTJob();
            },
            (error: Error) => {
              this.deletingInProgress = false;
              this.tJobExecService.popupService.openSnackBar('TJob Execution could not be deleted');
            },
          );
        }
      });
  }

  downloadAsJson(): void {
    this.downloading = true;
    let jsonObj: object = {
      tJobExec: this.tJobExec,
    };

    try {
      this.tJobExecService.loadTestSuitesInfoToDownload(this.tJobExec, [...this.tJobExec.testSuites]).subscribe(
        (someTestSuiteWithDate: boolean) => {
          if (someTestSuiteWithDate) {
            this.downloading = false;
            jsonObj['tJobExec'] = this.tJobExec;
            this.filesService.downloadObjectAsJson(jsonObj, this.getFileName());
          } else {
            this.downloadAsJsonWithoutTestCases();
          }
        },
        (error: Error) => {
          console.log('Error: ' + error, 'Trying to download without testcases');
          this.downloadAsJsonWithoutTestCases();
        },
      );
    } catch (e) {
      this.downloading = false;
      this.monitoringService.popupService.openSnackBar('Error: the execution could not be downloaded as json');
    }
  }

  downloadAsJsonWithoutTestCases(): void {
    let jsonObj: object = {
      tJobExec: this.tJobExec,
    };

    this.monitoringService.getAllTJobExecLogs(this.tJobExec).subscribe(
      (logsTraces: LogTraces[]) => {
        jsonObj['logs'] = logsTraces;

        // Todo metrics and disable btn while processing
        this.monitoringService.getAllTJobExecMetrics(this.tJobExec).subscribe((metricsTraces: MetricTraces[]) => {
          jsonObj['metrics'] = metricsTraces;

          // Create tmp url and link element for download
          this.filesService.downloadObjectAsJson(jsonObj, this.getFileName());
          this.downloading = false;
        });
      },
      (error: Error) => {
        this.downloading = false;
        this.monitoringService.popupService.openSnackBar('Error: the execution could not be downloaded as json');
      },
    );
  }

  getFileName(): string {
    let namePrefix: string = this.tJob && this.tJob.name && this.tJob.name !== '' ? 'TJob_' + this.tJob.name : '';
    let nameSuffix: string = this.tJobExec && this.tJobExec.id && this.tJobExec.id !== 0 ? 'execution_' + this.tJobExec.id : '';
    let name: string = (namePrefix !== '' ? namePrefix + '-' : '') + (nameSuffix !== '' ? nameSuffix : '');

    return name !== '' ? name : 'execution';
  }

  openExternalUrl(): void {
    let url: string = this.tJobExec.getExternalUrl();
    if (url !== undefined) {
      window.open(url);
    }
  }

  viewParent(): void {
    if (this.tJobExec && this.tJobExec.isChild() && this.tJobExec.execParent) {
      this.router.navigate(['/projects', this.tJob.project.id, 'tjob', this.tJob.id, 'tjob-exec', this.tJobExec.execParent.id]);
    }
  }

  expandGlobalInfoExpansionPanel(): void {
    this.globalInfoExpansionPanelOpened = true;

    if (!this.logsAndMetricsAlreadyInitialized) {
      // sleep
      sleep(1000)
        .then(() => {
          if (!this.logsAndMetrics) {
            this.expandGlobalInfoExpansionPanel();
          } else {
            this.initLogsAndMetrics();
          }
        })
        .catch((e) => {
          this.expandGlobalInfoExpansionPanel();
        });
    }
  }
}
