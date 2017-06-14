import { Component, AfterViewInit } from '@angular/core';

import { Title } from '@angular/platform-browser';

import { TdLoadingService, TdDigitsPipe } from '@covalent/core';

import { ItemsService, UsersService, ProductsService, AlertsService } from '../../../services';

import { multi } from './data';

import { TJobService } from '../tjob/tjob.service';
import { StompWSManager } from '../stomp-ws-manager.service';


@Component({
  selector: 'etm-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
  viewProviders: [ItemsService, UsersService, ProductsService, AlertsService],
})
export class DashboardComponent implements AfterViewInit {

  items: Object[];
  users: Object[];
  products: Object[];
  alerts: Object[];

  // Chart
  single: any[];
  multi: any[];
  cpuData: any = [
    {
      'name': 'Test',
      'series': [
      ],
    },
    {
      'name': 'Sut',
      'series': [
      ],
    },
  ];

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

  colorScheme: any = {
    domain: ['#1565C0', '#EF6C00', '#2196F3', '#81D4FA', '#FF9800'],
  };

  // line, area
  autoScale: boolean = true;



  constructor(private _titleService: Title,
    private _itemsService: ItemsService,
    private _usersService: UsersService,
    private _alertsService: AlertsService,
    private _productsService: ProductsService,
    private _loadingService: TdLoadingService,
    private tJobService: TJobService,
    private stompWSManager: StompWSManager) {
    this.stompWSManager.testCpuDataUpdated.subscribe((data: any) => this.updateCpuData(data, true));
    this.stompWSManager.sutCpuDataUpdated.subscribe((data: any) => this.updateCpuData(data, false));
  }

  updateCpuData(data: any, test: boolean) {
    if (data.type === 'cpu') {
      let parsedData: any = {
        'value': data.cpu.totalUsage,
        'name': new Date('' + data['@timestamp']),
      }
      if (test) {
        this.cpuData[0].series.push(parsedData);
      }
      else {
        this.cpuData[1].series.push(parsedData);
      }
      this.cpuData = [...this.cpuData];
    }
  }


  tJobId: number;
  withSut: boolean = false;

  verifySut() {
    this.withSut = !this.withSut;
  }

  public runTJob() {

    this.tJobService.runTJob(this.tJobId)
      .subscribe(
      tjobExecution => {
        console.log('TJobExecutionId:' + tjobExecution.id);
        this.createAndSubscribe(tjobExecution);
      },
      error => console.error("Error:" + error)
      );
  }

  public createAndSubscribe(tjobExecution: any) {
    this.stompWSManager.subscribeWSDestinationTest('q-' + tjobExecution.id + '-test-metrics');
    if (this.withSut) {
      this.stompWSManager.subscribeWSDestinationSut('q-' + tjobExecution.id + '-sut-metrics');
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
