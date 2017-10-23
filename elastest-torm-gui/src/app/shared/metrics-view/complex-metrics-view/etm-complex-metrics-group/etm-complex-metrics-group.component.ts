import { ElastestRabbitmqService } from '../../../services/elastest-rabbitmq.service';
import { Subject, Observable } from 'rxjs/Rx';
import { ComplexMetricsViewComponent } from '../complex-metrics-view.component';
import { TJobModel } from '../../../../elastest-etm/tjob/tjob-model';
import { ElastestESService } from '../../../services/elastest-es.service';
import { ESRabComplexMetricsModel } from '../models/es-rab-complex-metrics-model';
import { TJobExecModel } from '../../../../elastest-etm/tjob-exec/tjobExec-model';
import { Component, Input, OnInit, Output, QueryList, ViewChildren, EventEmitter } from '@angular/core';
import { Subscription } from 'rxjs/Rx';
import { componentTypes, defaultInfoIdMap } from '../../../defaultESData-model';

@Component({
  selector: 'etm-complex-metrics-group',
  templateUrl: './etm-complex-metrics-group.component.html',
  styleUrls: ['./etm-complex-metrics-group.component.scss']
})
export class EtmComplexMetricsGroupComponent implements OnInit {
  @ViewChildren(ComplexMetricsViewComponent) complexMetricsViewComponents: QueryList<ComplexMetricsViewComponent>;

  @Input()
  public live: boolean;

  // Metrics Chart
  allInOneMetrics: ESRabComplexMetricsModel;
  metricsList: ESRabComplexMetricsModel[] = [];
  groupedMetricsList: ESRabComplexMetricsModel[][] = [];

  loaded: boolean = false;

  // TimeLine Observable
  @Output()
  timelineObs = new EventEmitter<any>();

  @Output()
  hoverObs = new EventEmitter<any>();

  @Output()
  leaveObs = new EventEmitter<any>();

  tJob: TJobModel;
  tJobExec: TJobExecModel;

  constructor(
    private elastestESService: ElastestESService,
    private elastestRabbitmqService: ElastestRabbitmqService,
  ) { }

  ngOnInit() {
  }

  ngAfterViewInit(): void {
    if (this.live) {
      this.initObservables();
    }
  }

  initObservables() {
    // Get default Rabbit queues 
    let obsMap: Map<string, Observable<string>> = this.elastestRabbitmqService.observableMap;
    obsMap.forEach((obs: Observable<string>, key: string) => {
      let obsData: any = this.elastestRabbitmqService.getDataFromObsName(key);
      if (obsData.traceType === 'metrics') {
        obs.subscribe((data) => this.updateMetricsData(data));
      }
    });
  }


  initMetricsView(tJob: TJobModel, tJobExec: TJobExecModel) {
    this.tJob = tJob;
    this.tJobExec = tJobExec;

    if (tJob.execDashboardConfigModel.showComplexMetrics) {
      this.allInOneMetrics = new ESRabComplexMetricsModel(this.elastestESService);
      this.allInOneMetrics.name = 'All Metrics';
      this.allInOneMetrics.hidePrevBtn = !this.live;
      this.allInOneMetrics.metricsIndex = tJobExec.logIndex;
      if (!this.live) {
        this.allInOneMetrics.getAllMetrics();
      }
    }
    for (let metric of tJob.execDashboardConfigModel.allMetricsFields.fieldsList) {
      if (metric.activated) {
        let individualMetrics: ESRabComplexMetricsModel = new ESRabComplexMetricsModel(this.elastestESService);
        individualMetrics.name = metric.type + ' ' + metric.subtype;
        individualMetrics.componentType = metric.componentType;

        individualMetrics.activateAllMatchesByNameSuffix(metric.name);
        individualMetrics.hidePrevBtn = !this.live;
        individualMetrics.metricsIndex = tJobExec.logIndex;
        if (!this.live) {
          individualMetrics.getAllMetrics();
        }

        this.metricsList.push(individualMetrics);
      }
    }
    this.groupedMetricsList = this.createGroupedArray(this.metricsList, 2);
  }

  createGroupedArray(arr, chunkSize) {
    let groups = [], i;
    for (i = 0; i < arr.length; i += chunkSize) {
      groups.push(arr.slice(i, i + chunkSize));
    }
    return groups;
  }

  updateMetricsData(data: any) {
    for (let group of this.groupedMetricsList) {
      for (let metric of group) {
        metric.updateData(data);
      }
    }

    this.allInOneMetrics.updateData(data);
  }

  ngAfterViewChecked() {
    if (!this.loaded) {
      this.subscribeToEvents();
    }
  }

  subscribeToEvents() {
    this.loaded = this.complexMetricsViewComponents.toArray() && this.complexMetricsViewComponents.toArray().length > 0;
    if (this.loaded) {
      this.complexMetricsViewComponents.forEach(
        (element) => {
          element.getTimelineSubscription().subscribe(
            (data) => {
              this.updateTimeline(data);
              this.timelineObs.next(data);
            }
          );

          element.getHoverSubscription().subscribe(
            (data) => {
              this.hoverCharts(data);
              this.hoverObs.next(data.value);
            }
          )

          element.getLeaveSubscription().subscribe(
            (data) => {
              this.leaveCharts();
              this.leaveObs.next();
            }
          )
        }
      );
    }
  }

  updateTimeline(domain) {
    this.complexMetricsViewComponents.forEach(
      (element) => {
        element.updateDomain(domain);
      }
    );
  }

  hoverCharts(item) {
    this.complexMetricsViewComponents.forEach(
      (element) => {
        element.hoverCharts(item);
      }
    );
  }

  leaveCharts() {
    this.complexMetricsViewComponents.forEach(
      (element) => {
        element.leaveCharts();
      }
    );
  }

  removeAndUnsubscribe(pos: number) {
    let lastMetric: boolean = false;
    if (this.metricsList.length === 1) {
      lastMetric = true;
    }

    // If is live and is the last metric card, unsubscribe
    if (this.live && lastMetric && !this.allInOneMetrics) {
      let componentType: string = this.metricsList[pos].componentType;
      let infoId: string = this.metricsList[pos].infoId;
      this.unsubscribe(componentType, infoId);
    }
    this.metricsList.splice(pos, 1);
    this.groupedMetricsList = this.createGroupedArray(this.metricsList, 2);
  }

  removeAndUnsubscribeAIO() {
    if (this.live && this.metricsList.length === 0) {
      this.unsubscribe(this.allInOneMetrics.componentType, this.allInOneMetrics.infoId);
    }
    this.allInOneMetrics = undefined;
  }

  unsubscribe(componentType: string, infoId: string) {
    let traceType: string = 'metrics';

    if (!infoId || infoId === '') {
      infoId = defaultInfoIdMap.metrics;
    }

    if (!componentType || componentType === '') {
      for (componentType of componentTypes) {
        this.elastestRabbitmqService.unsuscribeFromTopic(this.tJobExec, traceType, componentType, infoId);
      }
    } else {
      this.elastestRabbitmqService.unsuscribeFromTopic(this.tJobExec, traceType, componentType, infoId);
    }
  }

}
