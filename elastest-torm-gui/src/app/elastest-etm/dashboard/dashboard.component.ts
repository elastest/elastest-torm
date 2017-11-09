import { TitlesService } from '../../shared/services/titles.service';
import { EsmServiceInstanceModel } from '../../elastest-esm/esm-service-instance.model';
import { EsmService } from '../../elastest-esm/esm-service.service';
import { EtmLogsMetricsViewComponent } from '../etm-logs-metrics-view/etm-logs-metrics-view.component';
import { TJobModel } from '../tjob/tjob-model';
import { AfterViewInit, Component, ElementRef, ViewChild, OnDestroy } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Subscription } from 'rxjs/Rx';

import { ElastestEusComponent } from '../../elastest-eus/elastest-eus.component';
import { ESRabLogModel } from '../../shared/logs-view/models/es-rab-log-model';
import { ETRESMetricsModel } from '../../shared/metrics-view/models/et-res-metrics-model';
import { ElastestESService } from '../../shared/services/elastest-es.service';
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
  @ViewChild('logsAndMetrics') logsAndMetrics: EtmLogsMetricsViewComponent;

  tJobId: number;
  tJob: TJobModel;
  withSut: boolean = false;

  tJobExecId: number;
  tJobExec: TJobExecModel;

  serviceInstances: EsmServiceInstanceModel[] = [];
  instancesNumber: number;

  statusMessage: string = '';

  constructor(private titlesService: TitlesService,
    private tJobService: TJobService,
    private tJobExecService: TJobExecService,
    private elastestRabbitmqService: ElastestRabbitmqService,
    private route: ActivatedRoute, private router: Router,
    private elastestESService: ElastestESService,
    private esmService: EsmService,
  ) {
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params.subscribe(
        (params: Params) => {
          this.tJobId = params.tJobId;
          this.tJobExecId = params.tJobExecId;
        }
      );
    }

  }

  ngOnInit() {
    this.titlesService.setHeadTitle('TJob Execution');
  }

  ngAfterViewInit(): void {
    this.loadTJobExec();
  }

  loadTJobExec() {
    this.tJobExecService.getTJobExecutionByTJobId(this.tJobId, this.tJobExecId)
      .subscribe((tJobExec: TJobExecModel) => {
        this.tJobExec = tJobExec;
        this.titlesService.setTopTitle(tJobExec.getRouteString());
        this.withSut = this.tJobExec.tJob.hasSut();

        this.tJobService.getTJob(this.tJobExec.tJob.id.toString())
          .subscribe(
          (tJob: TJobModel) => {
            this.tJob = tJob;
            if (this.tJobExec.finished()) {
              this.router.navigate(
                ['/projects', tJob.project.id, 'tjob', this.tJobId, 'tjob-exec', this.tJobExecId]);
            } else {
              this.checkResultStatus();
              this.instancesNumber = this.tJobExec.tJob.esmServicesChecked;
              if (tJobExec) {
                setTimeout(() => {
                  this.getSupportServicesInstances();
                }, 0);
              }
              this.logsAndMetrics.initView(tJob, this.tJobExec);
              if (!this.tJobExec.starting()) { // If it's already started, get last trace(s)
                this.logsAndMetrics.loadLastTraces();
              }
              this.elastestRabbitmqService.subscribeToDefaultTopics(this.tJobExec);
            }
          });
      });
  }

  getSupportServicesInstances() {
    this.esmService.getSupportServicesInstancesByTJobExec(this.tJobExec)
      .subscribe((serviceInstances: EsmServiceInstanceModel[]) => {
        if (serviceInstances.length === this.instancesNumber || this.tJobExec.finished()) {
          this.serviceInstances = [...serviceInstances];
        } else {
          setTimeout(() => {
            this.getSupportServicesInstances();
          }, 2000);
        }
      });
  }

  ngOnDestroy() {
    this.elastestRabbitmqService.unsubscribeWSDestination();
  }

  checkResultStatus() {
    this.tJobExecService.getResultStatus(this.tJob, this.tJobExec).subscribe(
      (data) => {
        this.tJobExec.result = data.result;
        this.tJobExec.resultMsg = data.msg;
        if (this.tJobExec.finished()) {
          console.log('TJob Execution Finished');
        } else {
          setTimeout(() => {
            this.checkResultStatus();
          }, 2000);
        }
      }
    )
  }
}

