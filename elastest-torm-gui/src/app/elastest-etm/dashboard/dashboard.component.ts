import { AfterViewInit, Component, ElementRef, ViewChild, OnDestroy } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { TdDigitsPipe, TdLoadingService } from '@covalent/core';
import { Subscription } from 'rxjs/Rx';

import { AlertsService, ItemsService, ProductsService, UsersService } from '../../../services';
import { ElasticSearchService } from '../../elastest-log-manager/services/elasticsearch.service';
import { StompWSManager } from '../stomp-ws-manager.service';
import { TJobExecModel } from '../tjob-exec/tjobExec-model';
import { TJobExecService } from '../tjob-exec/tjobExec.service';
import { TJobService } from '../tjob/tjob.service';
import { MetricsDataModel } from './metrics-data-model';
import { MdSnackBar } from '@angular/material';
import { LogViewModel } from '../../shared/logs-view/log-view-model';

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

  sutLogView: LogViewModel = new LogViewModel();
  testLogView: LogViewModel = new LogViewModel();

  testMetricsSubscription: Subscription;
  sutMetricsSubscription: Subscription;

  tJobExecId: number;
  tJobExec: TJobExecModel;

  fromTJobPage: boolean = false;

  constructor(private _titleService: Title,
    private _itemsService: ItemsService,
    private _usersService: UsersService,
    private _alertsService: AlertsService,
    private _productsService: ProductsService,
    private _loadingService: TdLoadingService,
    private tJobService: TJobService,
    private tJobExecService: TJobExecService,
    private stompWSManager: StompWSManager,
    private elasticService: ElasticSearchService,
    private route: ActivatedRoute, private router: Router,
    private snackBar: MdSnackBar) {
    this.testLogView.name = 'Test Logs';
    this.sutLogView.name = 'Sut Logs';

    this.testLogView.traces = this.stompWSManager.testTraces;
    this.sutLogView.traces = this.stompWSManager.sutTraces;

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

    if (this.testLogView.traces.length > 0) {
      this.testLogView.traces.splice(0, this.stompWSManager.testTraces.length);
    }

    if (this.sutLogView.traces.length > 0) {
      this.sutLogView.traces.splice(0, this.stompWSManager.sutTraces.length);
    }
    this.testMetricsSubscription = this.stompWSManager.testMetrics$
      .subscribe(data => this.updateData(data, true));

    this.sutMetricsSubscription = this.stompWSManager.sutMetrics$
      .subscribe(data => this.updateData(data, false));

    this.tJobExec = new TJobExecModel();
    this.loadTJobExec();

    this._titleService.setTitle('ElasTest ETM');
    this._loadingService.register('items.load');
    this._itemsService.query().subscribe((items: Object[]) => {
      this.items = items;
      setTimeout(() => {
        this._loadingService.resolve('items.load');
      }, 750);
    }, (error: Error) => {
      this._itemsService.staticQuery().subscribe((items: Object[]) => {
        this.items = items;
        setTimeout(() => {
          this._loadingService.resolve('items.load');
        }, 750);
      });
    });
    this._loadingService.register('alerts.load');
    this._alertsService.query().subscribe((alerts: Object[]) => {
      this.alerts = alerts;
      setTimeout(() => {
        this._loadingService.resolve('alerts.load');
      }, 750);
    });
    this._loadingService.register('products.load');
    this._productsService.query().subscribe((products: Object[]) => {
      this.products = products;
      setTimeout(() => {
        this._loadingService.resolve('products.load');
      }, 750);
    });
    this._loadingService.register('favorites.load');
    this._productsService.query().subscribe((products: Object[]) => {
      this.products = products;
      setTimeout(() => {
        this._loadingService.resolve('favorites.load');
      }, 750);
    });
    this._loadingService.register('users.load');
    this._usersService.query().subscribe((users: Object[]) => {
      this.users = users;
      setTimeout(() => {
        this._loadingService.resolve('users.load');
      }, 750);
    }, (error: Error) => {
      this._usersService.staticQuery().subscribe((users: Object[]) => {
        this.users = users;
        setTimeout(() => {
          this._loadingService.resolve('users.load');
        }, 750);
      });
    });
  }

  loadTJobExec() {    
    this.tJobExecService.getTJobExecutionByTJobId(this.tJobId, this.tJobExecId)
      .subscribe((tJobExec: TJobExecModel) => {
        this.tJobExec = tJobExec;
        this.withSut = this.tJobExec.tJob.hasSut();
        if (this.fromTJobPage) {
          console.log('Suscribe to TJob execution.')
          this.createAndSubscribeToTopic(this.tJobExecId);
        } else {
        this.createAndSubscribe(this.tJobExec);
        }
      });  
  }

  verifySut() {
    this.withSut = !this.withSut;
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

  ngAfterViewChecked() {
  }

  

  public createAndSubscribe(tjobExecution: any) {
    this.stompWSManager.subscribeToQueDestination('q-' + tjobExecution.id + '-test-log', this.stompWSManager.testLogResponse);
    this.stompWSManager.subscribeToQueDestination('q-' + tjobExecution.id + '-test-metrics', this.stompWSManager.testMetricsResponse);
    if (this.withSut) {
      this.stompWSManager.subscribeToQueDestination('q-' + tjobExecution.id + '-sut-log', this.stompWSManager.sutLogResponse);
      this.stompWSManager.subscribeToQueDestination('q-' + tjobExecution.id + '-sut-metrics', this.stompWSManager.sutMetricsResponse);
    } else {
      this.sutLogView.traces.push('TJob without Sut');
    }
  }

  public createAndSubscribeToTopic(tjobExecution: any) {
    this.stompWSManager.subscribeToTopicDestination('test.' + tjobExecution + '.log', this.stompWSManager.testLogResponse);
    this.stompWSManager.subscribeToTopicDestination('test.' + tjobExecution + '.metrics', this.stompWSManager.testMetricsResponse);
    if (this.withSut) {
      this.stompWSManager.subscribeToTopicDestination('sut.' + tjobExecution + '.log', this.stompWSManager.sutLogResponse);
      this.stompWSManager.subscribeToTopicDestination('sut.' + tjobExecution + '.metrics', this.stompWSManager.sutMetricsResponse);
    } else {
      this.sutLogView.traces.push('TJob without Sut');
    }
  }

  // ngx transform using covalent digits pipe
  axisDigits(val: any): any {
    return new TdDigitsPipe().transform(val);
  }

  public loadPrevTestLogs() {
    if (this.testLogView.traces[0] !== undefined && this.testLogView.traces[0] !== null) {
      this.elasticService.getFromGivenTestLog(this.tJobExec.logs, this.testLogView.traces[0])
        .subscribe(
        (messages) => {
          this.testLogView.prevTraces = messages;
          this.testLogView.prevTracesLoaded = true;
        },
        (error) => console.log(error),
      );
    }
  }

  public loadPrevSutLogs() {
    if (this.sutLogView.traces[0] !== undefined && this.sutLogView.traces[0] !== null) {
      this.elasticService.getFromGivenSutLog(this.tJobExec.logs, this.sutLogView.traces[0])
        .subscribe(
        (messages) => {
          this.sutLogView.prevTraces = messages;
          this.sutLogView.prevTracesLoaded = true;
        },
        (error) => console.log(error),
      );
    }
    else {
      // this.openSnackBar("Test", null);
    }
  }

  openSnackBar(message: string, action: string) {
    this.snackBar.open(message, action, {
      duration: 2000,
    });
  }

  ngOnDestroy() {
    this.stompWSManager.ususcribeWSDestination('');
  }
}
