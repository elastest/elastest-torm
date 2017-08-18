import { EtmLogsGroupComponent } from '../../shared/logs-view/etm-logs-group/etm-logs-group.component';
import {
  EtmComplexMetricsGroupComponent,
} from '../../shared/metrics-view/complex-metrics-view/etm-complex-metrics-group/etm-complex-metrics-group.component';
import { TJobModel } from '../tjob/tjob-model';
import { AfterViewInit, Component, ElementRef, ViewChild, OnDestroy } from '@angular/core';
import { Title } from '@angular/platform-browser';
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
  @ViewChild('metricsGroup') metricsGroup: EtmComplexMetricsGroupComponent;
  @ViewChild('logsGroup') logsGroup: EtmLogsGroupComponent;

  tJobId: number;
  withSut: boolean = false;

  tJobExecId: number;
  tJobExec: TJobExecModel;

  testLogsSubscription: Subscription;
  sutLogsSubscription: Subscription;
  testMetricsSubscription: Subscription;
  sutMetricsSubscription: Subscription;

  constructor(private _titleService: Title,
    private tJobService: TJobService,
    private tJobExecService: TJobExecService,
    private elastestRabbitmqService: ElastestRabbitmqService,
    private route: ActivatedRoute, private router: Router,
    private elastestESService: ElastestESService,
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
  }

  ngAfterViewInit(): void {
    this.testLogsSubscription = this.elastestRabbitmqService.testLogs$
      .subscribe((data) => this.updateLogsData(data, 'test'));
    this.sutLogsSubscription = this.elastestRabbitmqService.sutLogs$
      .subscribe((data) => this.updateLogsData(data, 'sut'));

    this.testMetricsSubscription = this.elastestRabbitmqService.testMetrics$
      .subscribe((data) => this.updateMetricsData(data));

    this.sutMetricsSubscription = this.elastestRabbitmqService.sutMetrics$
      .subscribe((data) => this.updateMetricsData(data));

    this.tJobExec = new TJobExecModel();
    this.loadTJobExec();

    this._titleService.setTitle('ElasTest ETM');
  }

  loadTJobExec() {
    this.tJobExecService.getTJobExecutionByTJobId(this.tJobId, this.tJobExecId)
      .subscribe((tJobExec: TJobExecModel) => {
        this.tJobExec = tJobExec;
        this.withSut = this.tJobExec.tJob.hasSut();

        this.metricsGroup.initMetricsView(this.tJobExec.tJob, this.tJobExec);
        this.logsGroup.initLogsView(this.tJobExec.tJob, this.tJobExec);

        console.log('Suscribe to TJob execution.');
        this.elastestRabbitmqService.createAndSubscribeToTopic(this.tJobExec);
      });
  }


  updateMetricsData(data: any) {
    this.metricsGroup.updateMetricsData(data);
  }

  updateLogsData(data: any, componentType: string) {
    this.logsGroup.updateLogsData(data, componentType);
  }

  ngOnDestroy() {
    this.elastestRabbitmqService.unsubscribeWSDestination();
  }
}
