import { AfterViewInit, Component, ElementRef, ViewChild } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { TdDigitsPipe, TdLoadingService } from '@covalent/core';
import { Subscription } from 'rxjs/Rx';

import { AlertsService, ItemsService, ProductsService, UsersService } from '../../../services';
import { StompWSManager } from '../stomp-ws-manager.service';
import { TJobExecService } from '../tjob-exec/tjobExec.service';
import { TJobService } from '../tjob/tjob.service';
import { cpuData, memoryData } from './data';



@Component({
  selector: 'etm-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
  viewProviders: [ItemsService, UsersService, ProductsService, AlertsService],
})
export class DashboardComponent implements AfterViewInit {
  @ViewChild('scrollMeTest') private myScrollContainerTest: ElementRef;
  @ViewChild('scrollMeSut') private myScrollContainerSut: ElementRef;

  items: Object[];
  users: Object[];
  products: Object[];
  alerts: Object[];

  // Chart
  cpuData: any = [];
  memoryData: any = [];

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
  testTraces: string[] = [];
  sutTraces: string[] = [];
  testMetricsSubscription: Subscription;
  sutMetricsSubscription: Subscription;


  constructor(private _titleService: Title,
    private _itemsService: ItemsService,
    private _usersService: UsersService,
    private _alertsService: AlertsService,
    private _productsService: ProductsService,
    private _loadingService: TdLoadingService,
    private tJobService: TJobService,
    private tJobExecService: TJobExecService,
    private stompWSManager: StompWSManager) {
    this.cpuData = cpuData;
    this.memoryData = memoryData;
  }

  ngOnInit() {
    this.testTraces = this.stompWSManager.testTraces;
    if (this.testTraces.length > 0) {
      this.testTraces.splice(0, this.stompWSManager.testTraces.length);
    }
    this.scrollToBottomTest();

    this.sutTraces = this.stompWSManager.sutTraces;
    if (this.sutTraces.length > 0) {
      this.sutTraces.splice(0, this.stompWSManager.sutTraces.length);
    }
    this.scrollToBottomSut();

    this.testMetricsSubscription = this.stompWSManager.testMetrics$
      .subscribe(data => this.updateData(data, true));

    this.sutMetricsSubscription = this.stompWSManager.sutMetrics$
      .subscribe(data => this.updateData(data, false));
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
      this.cpuData[0].series.push(parsedData);
    } else {
      this.cpuData[1].series.push(parsedData);
    }
    this.cpuData = [...this.cpuData];
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
      this.memoryData[0].series.push(parsedData);
    } else {
      this.memoryData[1].series.push(parsedData);
    }
    this.memoryData = [...this.memoryData];
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
    this.scrollToBottomTest();
    this.scrollToBottomSut();
  }

  scrollToBottomTest(): void {
    try {
      this.myScrollContainerTest.nativeElement.scrollTop = this.myScrollContainerTest.nativeElement.scrollHeight;
    } catch (err) {
      console.log('[Error]:' + err.toString());
    }
  }

  scrollToBottomSut(): void {
    try {
      this.myScrollContainerSut.nativeElement.scrollTop = this.myScrollContainerSut.nativeElement.scrollHeight;
    } catch (err) {
      console.log('[Error]:' + err.toString());
    }
  }

  public runTJob() {

    this.tJobExecService.runTJob(this.tJobId)
      .subscribe(
      (tjobExecution) => {
        console.log('TJobExecutionId:' + tjobExecution.id);
        this.createAndSubscribe(tjobExecution);
      },
      (error) => console.error('Error:' + error),
    );
  }

  public createAndSubscribe(tjobExecution: any) {
    this.stompWSManager.subscribeWSDestinationTestLog('q-' + tjobExecution.id + '-test-log');
    this.stompWSManager.subscribeWSDestinationTestMetrics('q-' + tjobExecution.id + '-test-metrics');
    if (this.withSut) {
      this.stompWSManager.subscribeWSDestinationSutLog('q-' + tjobExecution.id + '-sut-log');
      this.stompWSManager.subscribeWSDestinationSutMetrics('q-' + tjobExecution.id + '-sut-metrics');
    } else {
      this.sutTraces.push('TJob without Sut');
    }
  }



  ngAfterViewInit(): void {
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

  // ngx transform using covalent digits pipe
  axisDigits(val: any): any {
    return new TdDigitsPipe().transform(val);
  }
}
