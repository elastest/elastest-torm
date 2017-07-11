import { AfterViewInit, Component, ElementRef, ViewChild, OnDestroy } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Subscription } from 'rxjs/Rx';

import { ElastestEusComponent } from '../../elastest-eus/elastest-eus.component';
import { RabESLogModel } from '../../shared/logs-view/models/rab-es-log-model';
import { ETRESMetricsModel } from '../../shared/metrics-view/models/et-res-metrics-model';
import { ElasticSearchService } from '../../shared/services/elasticsearch.service';
import { StompWSManager } from '../stomp-ws-manager.service';
import { TJobExecModel } from '../tjob-exec/tjobExec-model';
import { TJobExecService } from '../tjob-exec/tjobExec.service';
import { TJobService } from '../tjob/tjob.service';

@Component({
  selector: 'etm-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class DashboardComponent implements AfterViewInit, OnDestroy {

  // Chart
  cpuData: ETRESMetricsModel;
  memoryData: ETRESMetricsModel;

  tJobId: number;
  withSut: boolean = false;

  sutLogView: RabESLogModel;
  testLogView: RabESLogModel;

  testLogsSubscription: Subscription;
  sutLogsSubscription: Subscription;
  testMetricsSubscription: Subscription;
  sutMetricsSubscription: Subscription;

  tJobExecId: number;
  tJobExec: TJobExecModel;
  
  constructor(private _titleService: Title,
    private tJobService: TJobService,
    private tJobExecService: TJobExecService,
    private stompWSManager: StompWSManager,
    private route: ActivatedRoute, private router: Router,
    private elasticsearchService: ElasticSearchService,
  ) {
    this.initLogsView();
    this.initMetricsView();
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
    this.testLogsSubscription = this.stompWSManager.testLogs$
      .subscribe((data) => this.testLogView.traces.push(data));

    this.sutLogsSubscription = this.stompWSManager.sutLogs$
      .subscribe((data) => this.sutLogView.traces.push(data));

    this.testMetricsSubscription = this.stompWSManager.testMetrics$
      .subscribe((data) => this.updateData(data, 0));

    this.sutMetricsSubscription = this.stompWSManager.sutMetrics$
      .subscribe((data) => this.updateData(data, 1));

    this.tJobExec = new TJobExecModel();
    this.loadTJobExec();

    this._titleService.setTitle('ElasTest ETM');
  }

  loadTJobExec() {
    this.tJobExecService.getTJobExecutionByTJobId(this.tJobId, this.tJobExecId)
      .subscribe((tJobExec: TJobExecModel) => {
        this.tJobExec = tJobExec;
        this.withSut = this.tJobExec.tJob.hasSut();

        this.testLogView.logUrl = this.tJobExec.logs;
        this.sutLogView.logUrl = this.tJobExec.logs;

        if (!this.withSut) {
          this.sutLogView.traces = ['TJob Without Sut. There aren\'t logs'];
        }
   
        console.log('Suscribe to TJob execution.');
        this.tJobExecService.createAndSubscribeToTopic(this.tJobExec);
      });
  }

  initLogsView() {
    this.sutLogView = new RabESLogModel(this.elasticsearchService);
    this.testLogView = new RabESLogModel(this.elasticsearchService);

    this.testLogView.name = 'Test Logs';
    this.sutLogView.name = 'Sut Logs';

    this.testLogView.logType = 'testlogs';
    this.sutLogView.logType = 'sutlogs';
  }

  initMetricsView() {
    this.cpuData = new ETRESMetricsModel();
    this.memoryData = new ETRESMetricsModel();

    this.cpuData.name = 'CPU Usage';
    this.memoryData.name = 'Memory Usage';

    this.cpuData.yAxisLabel = 'Usage %';
    this.memoryData.yAxisLabel = this.cpuData.yAxisLabel;

    this.cpuData.type = 'cpu';
    this.memoryData.type = 'memory';
  }

  updateData(data: any, position: number) {
    this.cpuData.updateData(data, position);
    this.memoryData.updateData(data, position);
  }

  ngOnDestroy() {
    this.stompWSManager.ususcribeWSDestination();
  }
}
