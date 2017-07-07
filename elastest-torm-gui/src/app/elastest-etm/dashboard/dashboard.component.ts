import { AfterViewInit, Component, ElementRef, ViewChild, OnDestroy } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { TdDigitsPipe, TdLoadingService } from '@covalent/core';
import { Subscription } from 'rxjs/Rx';

import { AlertsService, ItemsService, ProductsService, UsersService } from '../../../services';
import { ElastestEusComponent } from '../../elastest-eus/elastest-eus.component';
import { ElasticSearchService } from '../../shared/services/elasticsearch.service';
import { StompWSManager } from '../stomp-ws-manager.service';
import { TJobExecModel } from '../tjob-exec/tjobExec-model';
import { TJobExecService } from '../tjob-exec/tjobExec.service';
import { TJobService } from '../tjob/tjob.service';
import { MetricsDataModel } from './metrics-data-model';
import { RabESLogModel } from '../../shared/logs-view/models/rab-es-log-model';

@Component({
  selector: 'etm-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
  viewProviders: [ItemsService, UsersService, ProductsService, AlertsService],
})
export class DashboardComponent implements AfterViewInit, OnDestroy {

  items: Object[];
  users: Object[];
  products: Object[];
  alerts: Object[];

  // Chart
  cpuData: MetricsDataModel = new MetricsDataModel();
  memoryData: MetricsDataModel = new MetricsDataModel();

  view: any[] = [700, 400];

  // options
  showXAxis: boolean = true;
  showYAxis: boolean = true;
  gradient: boolean = false;
  showLegend: boolean = false;
  showXAxisLabel: boolean = true;
  xAxisLabel: string = '';
  showYAxisLabel: boolean = true;
  yAxisLabel: string = 'Usage %';
  timeline: boolean = false;


  colorScheme: any = {
    domain: ['#ffac2f', '#666666', '#2196F3', '#81D4FA', '#FF9800'],
  };

  // line, area
  autoScale: boolean = true;

  tJobId: number;
  withSut: boolean = false;

  sutLogView: RabESLogModel;
  testLogView: RabESLogModel;

  testMetricsSubscription: Subscription;
  sutMetricsSubscription: Subscription;

  tJobExecId: number;
  tJobExec: TJobExecModel;

  fromTJobPage: boolean = false;

  constructor(private _titleService: Title,
    private tJobService: TJobService,
    private tJobExecService: TJobExecService,
    private stompWSManager: StompWSManager,
    private route: ActivatedRoute, private router: Router,
    private elasticsearchService: ElasticSearchService,
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

    this.route.queryParams.subscribe(
      params => {
        this.fromTJobPage = params['fromTJobManager'];
      }
    );
  }

  ngOnInit() {
  }

  ngAfterViewInit(): void {
    this.testLogView.traces.splice(0, this.stompWSManager.testTraces.length);
    this.sutLogView.traces.splice(0, this.stompWSManager.sutTraces.length);

    this.testMetricsSubscription = this.stompWSManager.testMetrics$
      .subscribe((data) => this.updateData(data, true));

    this.sutMetricsSubscription = this.stompWSManager.sutMetrics$
      .subscribe((data) => this.updateData(data, false));

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

        if (this.fromTJobPage) {
          console.log('Suscribe to TJob execution.')
          this.tJobExecService.createAndSubscribeToTopic(this.tJobExec);
        } else {
          this.tJobExecService.createAndSubscribe(this.tJobExec);
        }
      });
  }

  initLogsView() {
    this.sutLogView = new RabESLogModel(this.elasticsearchService);
    this.testLogView = new RabESLogModel(this.elasticsearchService);

    this.testLogView.name = 'Test Logs';
    this.sutLogView.name = 'Sut Logs';

    this.testLogView.traces = this.stompWSManager.testTraces;
    this.sutLogView.traces = this.stompWSManager.sutTraces;

    this.testLogView.logType = 'testlogs';
    this.sutLogView.logType = 'sutlogs';
  }

  updateData(data: any, test: boolean) {
    if (data.type === 'cpu') {
      this.updateCpuData(data, test);
    } else if (data.type === 'memory') {
      this.updateMemoryData(data, test);
    }
  }

  updateCpuData(data: any, test: boolean) {
    let parsedData: any = this.parseCpuData(data);
    if (test) {
      this.cpuData.data[0].series.push(parsedData);
    } else {
      this.cpuData.data[1].series.push(parsedData);
    }
    this.cpuData.data = [...this.cpuData.data];
  }

  parseCpuData(data: any) {
    let parsedData: any = {
      'value': data.cpu.totalUsage,
      'name': new Date('' + data['@timestamp']),
    };
    return parsedData;
  }

  updateMemoryData(data: any, test: boolean) {
    let parsedData: any = this.parseMemoryData(data);

    if (test) {
      this.memoryData.data[0].series.push(parsedData);
    } else {
      this.memoryData.data[1].series.push(parsedData);
    }
    this.memoryData.data = [...this.memoryData.data];
  }

  parseMemoryData(data: any) {
    let perMemoryUsage = data.memory.usage * 100 / data.memory.limit;
    let parsedData: any = {
      'value': perMemoryUsage,
      'name': new Date('' + data['@timestamp']),
    };
    return parsedData;
  }

  // ngx transform using covalent digits pipe
  axisDigits(val: any): any {
    return new TdDigitsPipe().transform(val);
  }

  ngOnDestroy() {
    this.stompWSManager.ususcribeWSDestination('');
  }
}
