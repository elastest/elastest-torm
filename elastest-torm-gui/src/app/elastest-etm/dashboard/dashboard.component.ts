import { PopupService } from '../../shared/services/popup.service';
import { ConfigurationService } from '../../config/configuration-service.service';
import { TitlesService } from '../../shared/services/titles.service';
import { EsmServiceInstanceModel } from '../../elastest-esm/esm-service-instance.model';
import { EsmService } from '../../elastest-esm/esm-service.service';
import { EtmMonitoringViewComponent } from '../etm-monitoring-view/etm-monitoring-view.component';
import { TJobModel } from '../tjob/tjob-model';
import { AfterViewInit, Component, ViewChild, OnDestroy } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Subscription, Observable } from 'rxjs/Rx';

import { ElastestRabbitmqService } from '../../shared/services/elastest-rabbitmq.service';
import { TJobExecModel } from '../tjob-exec/tjobExec-model';
import { TJobExecService } from '../tjob-exec/tjobExec.service';
import { TJobService } from '../tjob/tjob.service';

@Component({
  selector: 'etm-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class DashboardComponent implements AfterViewInit, OnDestroy {
  @ViewChild('logsAndMetrics') logsAndMetrics: EtmMonitoringViewComponent;

  elastestMode: string;

  tJobId: number;
  tJob: TJobModel;
  withSut: boolean = false;

  tJobExecId: number;
  tJobExec: TJobExecModel;

  serviceInstances: EsmServiceInstanceModel[] = [];
  instancesNumber: number;

  statusMessage: string = '';
  statusIcon: any = {
    name: '',
    color: '',
  };

  disableStopBtn: boolean = false;

  checkResultSubscription: Subscription;
  checkTSSInstancesSubscription: Subscription;

  constructor(
    private titlesService: TitlesService,
    private tJobService: TJobService,
    private tJobExecService: TJobExecService,
    private elastestRabbitmqService: ElastestRabbitmqService,
    private route: ActivatedRoute,
    private router: Router,
    private popupService: PopupService,
    private esmService: EsmService,
    private configurationService: ConfigurationService,
  ) {
    this.elastestMode = this.configurationService.configModel.elasTestExecMode;
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params.subscribe((params: Params) => {
        this.tJobId = params.tJobId;
        this.tJobExecId = params.tJobExecId;
      });
    }
  }

  ngOnInit() {
    this.titlesService.setHeadTitle('TJob Execution');
  }

  ngAfterViewInit(): void {
    this.loadTJobExec();
  }

  ngOnDestroy() {
    this.elastestRabbitmqService.unsubscribeWSDestination();
    this.unsubscribeCheckResult();
    this.unsubscribeCheckTssInstances();
  }

  loadTJobExec(): void {
    this.tJobExecService.getTJobExecutionByTJobId(this.tJobId, this.tJobExecId).subscribe((tJobExec: TJobExecModel) => {
      this.tJobExec = tJobExec;
      this.titlesService.setPathName(this.router.routerState.snapshot.url);
      this.withSut = this.tJobExec.tJob.hasSut();

      this.tJobService.getTJob(this.tJobExec.tJob.id.toString()).subscribe((tJob: TJobModel) => {
        this.tJob = tJob;
        if (this.tJobExec.finished()) {
          this.navigateToResultPage();
        } else {
          this.checkResultStatus();
          this.instancesNumber = this.tJobExec.tJob.esmServicesChecked;
          if (tJobExec) {
            this.getSupportServicesInstances();
          }
          this.logsAndMetrics.initView(tJob, this.tJobExec);
          if (!this.tJobExec.starting()) {
            // If it's already started, get last trace(s)
            this.logsAndMetrics.loadLastTraces();
          }
          this.elastestRabbitmqService.subscribeToDefaultTopics(this.tJobExec);
        }
      });
    });
  }

  navigateToResultPage(): void {
    this.router.navigate(['/projects', this.tJob.project.id, 'tjob', this.tJobId, 'tjob-exec', this.tJobExecId]);
  }

  getSupportServicesInstances(): void {
    let timer: Observable<number> = Observable.interval(2200);
    if (this.checkTSSInstancesSubscription === null || this.checkTSSInstancesSubscription === undefined) {
      this.checkTSSInstancesSubscription = timer.subscribe(() => {
        this.esmService.getSupportServicesInstancesByTJobExec(this.tJobExec).subscribe(
          (serviceInstances: EsmServiceInstanceModel[]) => {
            if (serviceInstances.length === this.instancesNumber || this.tJobExec.finished()) {
              this.unsubscribeCheckTssInstances();
              this.serviceInstances = [...serviceInstances];
            }
          },
          (error) => console.log(error),
        );
      });
    }
  }

  unsubscribeCheckTssInstances(): void {
    if (this.checkTSSInstancesSubscription !== undefined) {
      this.checkTSSInstancesSubscription.unsubscribe();
      this.checkTSSInstancesSubscription = undefined;
    }
  }

  checkResultStatus(): void {
    let timer: Observable<number> = Observable.interval(1800);
    if (this.checkResultSubscription === null || this.checkResultSubscription === undefined) {
      this.checkResultSubscription = timer.subscribe(() => {
        this.tJobExecService.getResultStatus(this.tJob, this.tJobExec).subscribe(
          (data) => {
            this.tJobExec.result = data.result;
            this.tJobExec.resultMsg = data.msg;
            if (this.tJobExec.finished()) {
              this.unsubscribeCheckResult();
              this.tJobExecService
                .getTJobExecutionByTJobId(this.tJobId, this.tJobExecId)
                .subscribe((finishedTJobExec: TJobExecModel) => {
                  this.tJobExec = finishedTJobExec;
                  this.statusIcon = this.tJobExec.getResultIcon();
                });
              this.popupService.openSnackBar('TJob Execution Finished with status ' + this.tJobExec.result);
            }
          },
          (error) => console.log(error),
        );
      });
    }
  }

  unsubscribeCheckResult(): void {
    if (this.checkResultSubscription !== undefined) {
      this.checkResultSubscription.unsubscribe();
      this.checkResultSubscription = undefined;
    }
  }

  viewTJob(): void {
    this.router.navigate(['/projects', this.tJob.project.id, 'tjob', this.tJobId]);
  }

  stopExec(): void {
    this.disableStopBtn = true;
    this.tJobExecService.stopTJobExecution(this.tJob, this.tJobExec).subscribe((tJobExec: TJobExecModel) => {
      this.tJobExec = tJobExec;
      let msg: string = 'The execution has been stopped';
      if (!this.tJobExec.stopped()) {
        msg = 'The execution has finished before stopping it';
      }
      this.popupService.openSnackBar(msg);
    }, (error) => (this.disableStopBtn = false));
  }
}
