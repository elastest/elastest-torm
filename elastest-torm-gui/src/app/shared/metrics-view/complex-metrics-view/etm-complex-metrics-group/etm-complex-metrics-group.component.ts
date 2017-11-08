import { MetricsFieldModel } from '../models/metrics-field-model';
import { TJobService } from '../../../../elastest-etm/tjob/tjob.service';
import { SingleMetricModel } from '../../models/single-metric-model';
import { ElastestRabbitmqService } from '../../../services/elastest-rabbitmq.service';
import { Subject, Observable } from 'rxjs/Rx';
import { ComplexMetricsViewComponent } from '../complex-metrics-view.component';
import { TJobModel } from '../../../../elastest-etm/tjob/tjob-model';
import { ElastestESService } from '../../../services/elastest-es.service';
import { ESRabComplexMetricsModel } from '../models/es-rab-complex-metrics-model';
import { TJobExecModel } from '../../../../elastest-etm/tjob-exec/tjobExec-model';
import { Component, Input, OnInit, Output, QueryList, ViewChildren, EventEmitter } from '@angular/core';
import { Subscription } from 'rxjs/Rx';
import { components, defaultStreamMap } from '../../../defaultESData-model';

@Component({
  selector: 'etm-complex-metrics-group',
  templateUrl: './etm-complex-metrics-group.component.html',
  styleUrls: ['./etm-complex-metrics-group.component.scss']
})
export class EtmComplexMetricsGroupComponent implements OnInit {
  @ViewChildren(ComplexMetricsViewComponent) complexMetricsViewComponents: QueryList<ComplexMetricsViewComponent>;

  @Input()
  public live: boolean;
  @Input()
  tJob: TJobModel;
  @Input()
  tJobExec: TJobExecModel;

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
    let subjectMap: Map<string, Subject<string>> = this.elastestRabbitmqService.subjectMap;
    subjectMap.forEach((obs: Subject<string>, key: string) => {
      let subjectData: any = this.elastestRabbitmqService.getDataFromSubjectName(key);
      if (subjectData.streamType === 'composed_metrics' || subjectData.streamType === 'atomic_metric') {
        obs.subscribe((data) => this.updateMetricsData(data));
      }
    });
  }

  getIgnoreComponent(): string {
    return this.tJob.hasSut() ? '' : 'sut'; // if is without sut, ignore sut metrics
  }

  initAIO() {
    let ignoreComponent: string = this.getIgnoreComponent();
    this.allInOneMetrics = new ESRabComplexMetricsModel(this.elastestESService, ignoreComponent);
    this.allInOneMetrics.name = 'All Metrics';
    this.allInOneMetrics.hidePrevBtn = !this.live;
    this.allInOneMetrics.metricsIndex = this.tJobExec.logIndex;
    let defaultMetricName: string = 'test' + '_' + 'et_dockbeat' + '_' + 'cpu_totalUsage'; // Activate Test cpu usage as default in AIO
    this.allInOneMetrics.activateAndApplyByName(defaultMetricName);
    if (!this.live) {
      this.allInOneMetrics.getAllMetrics();
    }
  }

  initMetricsView(tJob: TJobModel, tJobExec: TJobExecModel) {
    this.tJob = tJob;
    this.tJobExec = tJobExec;

    if (this.tJob.execDashboardConfigModel.showComplexMetrics) {
      this.initAIO();
    }

    for (let metric of this.tJob.execDashboardConfigModel.allMetricsFields.fieldsList) {
      if (metric.activated) {
        let individualMetrics: ESRabComplexMetricsModel = this.initializeBasicAttrByMetric(metric);
        individualMetrics.metricsIndex = this.tJobExec.logIndex;
        if (metric.component === '') { // If no component, is a default metric
          individualMetrics.activateAllMatchesByNameSuffix(metric.name);
          if (!this.live) {
            individualMetrics.getAllMetrics();
          }
          this.metricsList.push(individualMetrics);
        } else { // Else, is a custom metric
          let pos: number = this.initCustomMetric(metric, individualMetrics);
          if (!this.live && pos >= 0) {
            let metricName: string = metric.streamType === 'atomic_metric' ? metric.type : metric.type + '.' + metric.subtype;
            this.elastestESService.searchAllDynamic(individualMetrics.metricsIndex, metric.stream, metric.component, metricName)
              .subscribe(
              (obj) => this.metricsList[pos].addSimpleMetricTraces(obj.data),
              (error) => console.log(error),
            );
          }
        }
      }
    }
    this.createGroupedMetricList();
  }

  initCustomMetric(metric: MetricsFieldModel, individualMetrics: ESRabComplexMetricsModel): number {
    if (metric.unit) {
      individualMetrics.yAxisLabelLeft = metric.unit;
    }

    this.metricsList.push(individualMetrics);
    this.createGroupedMetricList();

    individualMetrics.allMetricsFields.fieldsList.push(metric);
    this.tJob.execDashboardConfigModel.allMetricsFields.addMetricsFieldToList(
      metric, individualMetrics.component, individualMetrics.stream, metric.streamType, metric.activated
    );

    let pos: number = this.metricsList.length - 1;

    if (this.live) {
      this.elastestRabbitmqService.createSubject(metric.streamType, individualMetrics.component, metric.stream);
      let index: string = this.tJobExec.getCurrentESIndex(individualMetrics.component);

      this.elastestRabbitmqService.createAndSubscribeToTopic(index, metric.streamType, individualMetrics.component, metric.stream)
        .subscribe(
        (data) => {
          let parsedData: SingleMetricModel = this.elastestESService.convertToMetricTrace(data, metric);
          if (this.metricsList[pos]) {
            this.metricsList[pos].addDataToSimpleMetric(metric, [parsedData]);
          }
        },
        (error) => console.log(error)
        );
    }
    return pos;
  }

  addMoreMetrics(obj: any) {
    let metric: MetricsFieldModel = obj.metricFieldModel;
    let individualMetrics: ESRabComplexMetricsModel = this.initializeBasicAttrByMetric(metric);

    if (!this.alreadyExist(individualMetrics.name)) {
      individualMetrics.addSimpleMetricTraces(obj.data);
      individualMetrics.metricsIndex = this.tJobExec.logIndex;
      this.initCustomMetric(metric, individualMetrics);
      this.elastestESService.popupService.openSnackBar('Metric added', 'OK');

    } else {
      this.elastestESService.popupService.openSnackBar('Already exist', 'OK');
    }
  }

  initializeBasicAttrByMetric(metric: any): ESRabComplexMetricsModel {
    let ignoreComponent: string = this.getIgnoreComponent();
    let individualMetrics: ESRabComplexMetricsModel = new ESRabComplexMetricsModel(this.elastestESService, ignoreComponent);
    individualMetrics.name = this.createName(metric.component, metric.stream, metric.type, metric.subtype);
    individualMetrics.component = metric.component;
    individualMetrics.stream = metric.stream;
    individualMetrics.hidePrevBtn = !this.live;
    return individualMetrics;
  }

  createName(component: string, stream: string, type: string, subtype: string): string {
    return component + ' ' + stream + ' ' + type + ' ' + subtype;
  }

  alreadyExist(name: string): boolean {
    for (let metric of this.metricsList) {
      if (metric.name === name) {
        return true;
      }
    }
    return false;
  }

  createGroupedMetricList() {
    let defaultGroupNum: number = 2;
    this.groupedMetricsList = this.createGroupedArray(this.metricsList, defaultGroupNum);
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
        if (metric.isDefault()) {
          metric.updateData(data);
        }
      }
    }

    if (this.allInOneMetrics) {
      this.allInOneMetrics.updateData(data);
    }
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

    let component: string = this.metricsList[pos].component;
    let stream: string = this.metricsList[pos].stream;
    let name: string = this.metricsList[pos].name;

    // If is live and is the last metric card, unsubscribe
    if (this.live && lastMetric && !this.allInOneMetrics) {
      this.unsubscribe(component, stream);
    }
    this.metricsList.splice(pos, 1);
    this.createGroupedMetricList();

    this.tJob.execDashboardConfigModel.allMetricsFields.disableMetricFieldByTitleName(name);
  }

  removeAndUnsubscribeAIO() {
    if (this.live && this.metricsList.length === 0) {
      this.unsubscribe(this.allInOneMetrics.component, this.allInOneMetrics.stream);
    }
    this.allInOneMetrics = undefined;
  }

  unsubscribe(component: string, stream: string) {
    let streamType: string = 'composed_metrics';

    if (!stream || stream === '') {
      stream = defaultStreamMap.metrics;
    }

    if (!component || component === '') {
      for (component of components) {
        let index: string = this.tJobExec.getCurrentESIndex(component);
        this.elastestRabbitmqService.unsuscribeFromTopic(index, streamType, component, stream);
      }
    } else {
      let index: string = this.tJobExec.getCurrentESIndex(component);
      this.elastestRabbitmqService.unsuscribeFromTopic(index, streamType, component, stream);
    }
  }

  loadLastTraces(){
    for (let chart of this.metricsList) {
      chart.loadLastTraces();
    }
  }
}
