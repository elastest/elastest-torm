import { TJobModel } from '../tjob/tjob-model';
import { AfterViewInit, Component, ElementRef, ViewChild, OnDestroy } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Subscription } from 'rxjs/Rx';

import { ElastestEusComponent } from '../../elastest-eus/elastest-eus.component';
import { RabESLogModel } from '../../shared/logs-view/models/rab-es-log-model';
import { ETRESMetricsModel } from '../../shared/metrics-view/models/et-res-metrics-model';
import { ElastestESService } from '../../shared/services/elastest-es.service';
import { ElastestRabbitmqService } from '../../shared/services/elastest-rabbitmq.service';
import { TJobExecModel } from '../tjob-exec/tjobExec-model';
import { TJobExecService } from '../tjob-exec/tjobExec.service';
import { TJobService } from '../tjob/tjob.service';
import {
  ESRabComplexMetricsModel,
} from '../../shared/metrics-view/complex-metrics-view/models/es-rab-complex-metrics-model';

@Component({
  selector: 'etm-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class DashboardComponent implements AfterViewInit, OnDestroy {

  tJobId: number;
  withSut: boolean = false;

  tJobExecId: number;
  tJobExec: TJobExecModel;

  // Logs
  sutLogView: RabESLogModel;
  testLogView: RabESLogModel;

  // Metrics Chart
  allInOneMetrics: ESRabComplexMetricsModel;
  metricsList: ETRESMetricsModel[] = [];
  groupedMetricsList: ETRESMetricsModel[][] = [];


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
    this.initLogsView();
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
      .subscribe((data) => this.testLogView.traces.push(data));

    this.sutLogsSubscription = this.elastestRabbitmqService.sutLogs$
      .subscribe((data) => this.sutLogView.traces.push(data));

    this.testMetricsSubscription = this.elastestRabbitmqService.testMetrics$
      .subscribe((data) => this.updateData(data));

    this.sutMetricsSubscription = this.elastestRabbitmqService.sutMetrics$
      .subscribe((data) => this.updateData(data));

    this.tJobExec = new TJobExecModel();
    this.loadTJobExec();

    this._titleService.setTitle('ElasTest ETM');
  }

  loadTJobExec() {
    this.tJobExecService.getTJobExecutionByTJobId(this.tJobId, this.tJobExecId)
      .subscribe((tJobExec: TJobExecModel) => {
        this.tJobExec = tJobExec;
        this.withSut = this.tJobExec.tJob.hasSut();

        // Init logs index
        this.testLogView.logIndex = this.tJobExec.logIndex;
        this.sutLogView.logIndex = this.tJobExec.logIndex;

        this.initMetricsView(this.tJobExec.tJob);

        if (!this.withSut) {
          this.sutLogView.traces = [
            {
              'message': 'TJob Without Sut. There aren\'t logs'
            }
          ];
        }

        console.log('Suscribe to TJob execution.');
        this.elastestRabbitmqService.createAndSubscribeToTopic(this.tJobExec);
      });
  }

  initLogsView() {
    this.sutLogView = new RabESLogModel(this.elastestESService);
    this.testLogView = new RabESLogModel(this.elastestESService);

    this.elastestESService.initTestLog(this.testLogView);
    this.elastestESService.initSutLog(this.sutLogView);
  }

  initMetricsView(tJob: TJobModel) {
    console.log('aglia',tJob)
    if (tJob.execDashboardConfigModel.showComplexMetrics) {
      this.allInOneMetrics = new ESRabComplexMetricsModel(this.elastestESService);
      this.allInOneMetrics.name = 'Metrics';
      this.allInOneMetrics.hidePrevBtn = false;
      this.allInOneMetrics.metricsIndex = this.tJobExec.logIndex;
    }
    for (let metric of tJob.execDashboardConfigModel.allMetricsFields.fieldsList) {
      if (metric.activated) {
        let etRESMetrics: ETRESMetricsModel = new ETRESMetricsModel(this.elastestESService, metric);
        etRESMetrics.hidePrevBtn = false;
        etRESMetrics.metricsIndex = this.tJobExec.logIndex;

        this.metricsList.push(etRESMetrics);
      }
    }
    this.groupedMetricsList = this.createGroupedArray(this.metricsList, 2);
  }

  updateData(data: any) {
    for (let group of this.groupedMetricsList) {
      for (let metric of group) {
        metric.updateData(data);
      }
    }

    this.allInOneMetrics.updateData(data);
  }

  ngOnDestroy() {
    this.elastestRabbitmqService.unsubscribeWSDestination();
  }

  createGroupedArray(arr, chunkSize) {
    let groups = [], i;
    for (i = 0; i < arr.length; i += chunkSize) {
      groups.push(arr.slice(i, i + chunkSize));
    }
    return groups;
  }
}
