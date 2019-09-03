import { components, defaultStreamMap } from '../../../shared/defaultESData-model';
import { ESRabComplexMetricsModel } from '../../../shared/metrics-view/metrics-chart-card/models/es-rab-complex-metrics-model';
import { MetricsChartCardComponent } from '../../../shared/metrics-view/metrics-chart-card/metrics-chart-card.component';
import { ElastestRabbitmqService } from '../../../shared/services/elastest-rabbitmq.service';
import { SingleMetricModel } from '../../../shared/metrics-view/models/single-metric-model';
import { MetricsFieldModel } from '../../../shared/metrics-view/metrics-chart-card/models/metrics-field-model';
import { Subject } from 'rxjs/Rx';
import {
  Component,
  Input,
  OnInit,
  Output,
  QueryList,
  ViewChildren,
  EventEmitter,
  AfterViewInit,
  AfterViewChecked,
} from '@angular/core';
import { Subscription } from 'rxjs/Rx';
import { AbstractTJobModel } from '../../models/abstract-tjob-model';
import { AbstractTJobExecModel } from '../../models/abstract-tjob-exec-model';
import { ExternalTJobExecModel } from '../../external/external-tjob-execution/external-tjob-execution-model';
import { TJobExecModel } from '../../tjob-exec/tjobExec-model';
import { MonitoringService } from '../../../shared/services/monitoring.service';
import { ButtonModel } from '../../../shared/button-component/button.model';
import { allArrayPairCombinations } from '../../../shared/utils';
import { Units } from '../../../shared/metrics-view/metrics-chart-card/models/all-metrics-fields-model';

@Component({
  selector: 'etm-chart-group',
  templateUrl: './etm-chart-group.component.html',
  styleUrls: ['./etm-chart-group.component.scss'],
})
export class EtmChartGroupComponent implements OnInit, AfterViewInit, AfterViewChecked {
  @ViewChildren(MetricsChartCardComponent)
  metricsChartCardComponents: QueryList<MetricsChartCardComponent>;

  @Input()
  public live: boolean;
  @Input()
  tJob: AbstractTJobModel;
  @Input()
  tJobExec: AbstractTJobExecModel;

  firstTimeInitialized: boolean = false;

  subscriptions: Map<string, Subscription> = new Map();

  // Metrics Chart
  allInOneMetrics: ESRabComplexMetricsModel;
  chartsList: ESRabComplexMetricsModel[] = [];
  combinedPairChartsList: ESRabComplexMetricsModel[] = [];
  groupedMetricsList: ESRabComplexMetricsModel[][] = [];

  customButtons: ButtonModel[] = [];

  loaded: boolean = false;

  // TimeLine Observable
  @Output()
  timelineObs = new EventEmitter<any>();

  @Output()
  hoverObs = new EventEmitter<any>();

  @Output()
  leaveObs = new EventEmitter<any>();

  chartsEventsSubscriptionsObs: Subscription[] = [];

  metricsPairsCardsNames: string[] = [];

  startDate: Date;
  endDate: Date;

  constructor(private monitoringService: MonitoringService, private elastestRabbitmqService: ElastestRabbitmqService) {}

  ngOnInit(): void {}

  ngAfterViewInit(): void {
    this.metricsChartCardComponents.changes.subscribe((data) => this.subscribeAllToEvents());
    if (this.live) {
      this.initObservables();
    }
  }

  ngAfterViewChecked(): void {
    if (!this.loaded) {
      this.subscribeAllToEvents();
    }
  }

  initObservables(): void {
    // Get default Rabbit queues
    let subjectMap: Map<string, Subject<string>> = this.elastestRabbitmqService.subjectMap;
    subjectMap.forEach((obs: Subject<string>, key: string) => {
      let subjectData: any = this.elastestRabbitmqService.getDataFromSubjectName(key);
      if (subjectData.streamType === 'composed_metrics' || subjectData.streamType === 'atomic_metric') {
        obs.subscribe((data) => this.updateDefaultChartsData(data));
      }
    });
  }

  getIgnoreComponent(): string {
    return this.tJob && this.tJob.hasSut() ? '' : 'sut'; // if is without sut, ignore sut metrics
  }

  initModels(tJob: AbstractTJobModel, tJobExec: AbstractTJobExecModel): void {
    this.tJob = tJob;
    this.tJobExec = tJobExec;

    if (this.tJobExec.hasMonitoringMarks()) {
      // One button for each mark id
      for (let markId of this.tJobExec.getMonitoringMarkIds()) {
        let markButtonModel: ButtonModel = new ButtonModel();
        markButtonModel.name = '"' + markId + '" Mark View';
        markButtonModel.color = 'accent';
        markButtonModel.hideIcon = true;
        markButtonModel.buttonType = 'raised-button';
        markButtonModel.clickMethodTooltip = 'Switch to "' + markId + '" Mark View';
        markButtonModel.clickMethod = () => {
          for (let button of this.customButtons) {
            button.disabled = false;
          }
          // Show Mark View and disable button (disables for all cards)
          markButtonModel.disabled = this.showMarkView(markId);
        };
        this.customButtons.push(markButtonModel);
      }

      // Time view btn
      if (this.customButtons.length > 0) {
        let timeButtonModel: ButtonModel = new ButtonModel();
        timeButtonModel.name = 'Time View';
        timeButtonModel.color = 'accent';
        timeButtonModel.hideIcon = true;
        timeButtonModel.buttonType = 'raised-button';
        timeButtonModel.disabled = true;
        timeButtonModel.clickMethodTooltip = 'Return to Time View';
        timeButtonModel.clickMethod = () => {
          for (let button of this.customButtons) {
            button.disabled = false;
          }
          timeButtonModel.disabled = this.showMarkView();
        };
        this.customButtons.push(timeButtonModel);
      }
    }
  }

  // When metric card is already activated
  initMetricsView(
    tJob: AbstractTJobModel,
    tJobExec: AbstractTJobExecModel,
    activeView?: string,
    customStartDate?: Date,
    customEndDate?: Date,
  ): void {
    tJobExec.activeView = activeView;

    this.startDate = customStartDate ? customStartDate : tJobExec.startDate;
    this.endDate = customEndDate ? customEndDate : tJobExec.endDate;

    this.allInOneMetrics = undefined;
    this.chartsList = [];
    this.groupedMetricsList = [];

    if (!this.firstTimeInitialized) {
      this.initModels(tJob, tJobExec);
    }

    let monitoringIndex: string = this.tJobExec.monitoringIndex;
    let showAIO: boolean = this.tJob.execDashboardConfigModel.showAllInOne;
    let passTJobExec: boolean = false;
    let ignoreDefaultDockbeatMetrics: boolean = false;

    // Multi parent TJobExec
    if (tJobExec instanceof TJobExecModel && tJobExec.isParent()) {
      monitoringIndex = tJobExec.getChildsMonitoringIndices();
      showAIO = false;
      passTJobExec = true;
      ignoreDefaultDockbeatMetrics = true;
    }

    if (showAIO) {
      this.initAIO();
    }

    for (let metric of this.tJob.execDashboardConfigModel.allMetricsFields.fieldsList) {
      if (metric.activated) {
        let individualMetrics: ESRabComplexMetricsModel = this.initializeBasicAttrByMetric(metric);
        individualMetrics.metric = metric;
        individualMetrics.monitoringIndex = monitoringIndex;
        individualMetrics.startDate = this.startDate;
        individualMetrics.endDate = this.endDate;
        if (metric.component === '') {
          // If no component, is a default metric (dockbeat whit more than 1 component)
          if (ignoreDefaultDockbeatMetrics) {
            metric.activated = false;
          } else {
            individualMetrics.activateAllMatchesByNameSuffix(metric.name);
            if (!this.live) {
              individualMetrics.getAllMetrics();
            } else {
              this.createSubjectAndSubscribe(individualMetrics.component, metric.stream, metric.streamType);
            }

            this.addChartToList(individualMetrics);
          }
        } else {
          // Else, is a custom metric
          let pos: number = this.initCustomMetric(metric, individualMetrics);
          if (!this.live && pos >= 0) {
            let metricName: string = metric.streamType === 'atomic_metric' ? metric.etType : metric.etType + '.' + metric.subtype;
            this.monitoringService
              .searchAllDynamic(
                individualMetrics.monitoringIndex,
                metric.stream,
                metric.component,
                metricName,
                passTJobExec ? tJobExec : undefined,
                this.startDate,
                this.endDate,
                true,
                true,
                'metric',
              )
              .subscribe(
                (obj: any) => {
                  if (!metric.unit && obj.unit) {
                    metric.unit = obj.unit;
                    this.chartsList[pos].setUnits(obj.unit);
                  }
                  this.chartsList[pos].addSimpleMetricTraces(obj.data);
                },
                (error: Error) => console.log(error),
              );
          }
        }
      }
    }
    this.createGroupedMetricList();
    this.firstTimeInitialized = true;

    if (this.tJob.execDashboardConfigModel.combineMetricsInPairs) {
      this.initMetricsPairs();
    }
  }

  addChartToList(individualMetrics: ESRabComplexMetricsModel, isCombinedPair: boolean = false): number {
    if (isCombinedPair) {
      return this.combinedPairChartsList.push(individualMetrics) - 1;
    } else {
      // returns position
      let pos: number = this.chartsList.push(individualMetrics) - 1;
      this.initMetricsPairs();
      return pos;
    }
  }

  initAIO(): void {
    let ignoreComponent: string = this.getIgnoreComponent();
    this.allInOneMetrics = new ESRabComplexMetricsModel(this.monitoringService, ignoreComponent);
    this.allInOneMetrics.name = 'All Default Metrics';
    this.allInOneMetrics.hidePrevBtn = !this.live;
    this.allInOneMetrics.monitoringIndex = this.tJobExec.monitoringIndex;
    let defaultMetricName: string = 'test' + '_' + 'et_dockbeat' + '_' + 'cpu_totalUsage'; // Activate Test cpu usage as default in AIO
    this.allInOneMetrics.activateAndApplyByName(defaultMetricName);
    if (!this.live) {
      this.allInOneMetrics.getAllMetrics(false);
    }
  }

  initCustomMetric(metric: MetricsFieldModel, individualMetrics: ESRabComplexMetricsModel): number {
    if (metric.unit) {
      individualMetrics.setUnits(metric.unit);
    }
    let pos: number = this.addChartToList(individualMetrics);
    this.createGroupedMetricList();

    individualMetrics.allMetricsFields.addMetricsFieldToList(
      metric,
      individualMetrics.component,
      individualMetrics.stream,
      metric.streamType,
      metric.activated,
    );

    if (this.live) {
      this.elastestRabbitmqService.createSubject(metric.streamType, individualMetrics.component, metric.stream);
      let index: string = this.getAbstractTJobExecIndex(individualMetrics.component);

      this.elastestRabbitmqService
        .createAndSubscribeToTopic(index, metric.streamType, individualMetrics.component, metric.stream)
        .subscribe(
          (data: any) => {
            if (data['et_type'] === metric.etType && data.component === metric.component) {
              let parsedData: SingleMetricModel = this.monitoringService.convertToMetricTrace(data, metric);
              if (parsedData === undefined) {
                console.error('Undefined data received, not added to ' + metric.name);
              } else {
                if (this.chartsList[pos]) {
                  this.chartsList[pos].addDataToSimpleMetric(metric, [parsedData]);
                }
              }
            }
          },
          (error: Error) => console.log(error),
        );
    }
    return pos;
  }

  // Added manually
  addMoreMetrics(obj: any): boolean {
    let added: boolean = false;
    for (let metric of obj.metricFieldModels) {
      let individualMetrics: ESRabComplexMetricsModel = this.initializeBasicAttrByMetric(metric);
      individualMetrics.metric = metric;
      if (!this.alreadyExist(individualMetrics.name)) {
        individualMetrics.addSimpleMetricTraces(obj.data);
        individualMetrics.monitoringIndex = this.tJobExec.monitoringIndex;
        this.initCustomMetric(metric, individualMetrics);

        added = added || true;
      } else {
        added = added || false;
      }
    }

    return added;
  }

  initializeBasicAttrByMetric(metric: MetricsFieldModel): ESRabComplexMetricsModel {
    let ignoreComponent: string = this.getIgnoreComponent();
    let individualMetrics: ESRabComplexMetricsModel = new ESRabComplexMetricsModel(this.monitoringService, ignoreComponent);
    individualMetrics.metric = metric;
    individualMetrics.name = this.createName(metric.component, metric.stream, metric.etType, metric.subtype);
    individualMetrics.component = metric.component;
    individualMetrics.stream = metric.stream;
    individualMetrics.hidePrevBtn = !this.live;
    return individualMetrics;
  }

  initMetricsPairs(): void {
    this.combinedPairChartsList = [];
    let metricsPairsList: MetricsFieldModel[][] = allArrayPairCombinations(this.getChartsListMetricFieldModels());
    if (metricsPairsList) {
      for (let metricPair of metricsPairsList) {
        this.addMoreMetricsFromMetricPair(metricPair);
      }
    }
  }

  // Added manually
  addMoreMetricsFromMetricPair(metricsPairForCombo: MetricsFieldModel[]): boolean {
    if (metricsPairForCombo) {
      let ignoreComponent: string = this.getIgnoreComponent();
      let comboPairChart: ESRabComplexMetricsModel = new ESRabComplexMetricsModel(this.monitoringService, ignoreComponent);
      comboPairChart.isCombinedPair = true;
      comboPairChart.startDate = this.startDate;
      comboPairChart.endDate = this.endDate;

      let firstMetric: MetricsFieldModel = metricsPairForCombo[0];
      let secondMetric: MetricsFieldModel = metricsPairForCombo[1];

      if (firstMetric && secondMetric) {
        let firstMetricName: string = this.createName(
          firstMetric.component,
          firstMetric.stream,
          firstMetric.etType,
          firstMetric.subtype,
        );
        let secondMetricName: string = this.createName(
          secondMetric.component,
          secondMetric.stream,
          secondMetric.etType,
          secondMetric.subtype,
        );

        comboPairChart.name = firstMetricName + ' / ' + secondMetricName;
        comboPairChart.hidePrevBtn = !this.live;

        let monitoringIndex: string = this.tJobExec.monitoringIndex;

        // If Multi Parent
        if (this.tJobExec instanceof TJobExecModel && this.tJobExec.isParent()) {
          monitoringIndex = this.tJobExec.getChildsMonitoringIndices();
        }

        comboPairChart.monitoringIndex = monitoringIndex;

        // Check first if has unit
        let firstMetricUnit: Units | string = this.monitoringService.getMetricUnitByTrace(
          firstMetric,
          firstMetric.subtype,
          firstMetric.etType,
        );

        let secondMetricUnit: Units | string = this.monitoringService.getMetricUnitByTrace(
          secondMetric,
          secondMetric.subtype,
          secondMetric.etType,
        );

        if (firstMetricUnit && secondMetricUnit) {
          this.initMetricPairChart(firstMetric, secondMetric, firstMetricUnit, secondMetricUnit, comboPairChart);
        } else {
          // Get data of first metric
          this.monitoringService.getMetricUnit(monitoringIndex, firstMetric).subscribe(
            (firstMetricUnit2: Units | string) => {
              // Get data of second metric
              this.monitoringService.getMetricUnit(monitoringIndex, secondMetric).subscribe(
                (secondMetricUnit2: Units | string) => {
                  this.initMetricPairChart(firstMetric, secondMetric, firstMetricUnit2, secondMetricUnit2, comboPairChart);
                },
                (error: Error) => console.error('Could not load more metric pair (second metric): ' + error),
              );
            },
            (error: Error) => console.error('Could not load more metric pair (first metric): ' + error),
          );
        }
      }
    }
    return true;
  }

  initMetricPairChart(
    firstMetric: MetricsFieldModel,
    secondMetric: MetricsFieldModel,
    firstMetricUnit: Units | string,
    secondMetricUnit: Units | string,
    comboPairChart: ESRabComplexMetricsModel,
  ): void {
    firstMetric.unit = firstMetricUnit;
    secondMetric.unit = secondMetricUnit;

    comboPairChart.allMetricsFields.addMetricsFieldToList(
      firstMetric,
      comboPairChart.component,
      comboPairChart.stream,
      firstMetric.streamType,
      firstMetric.activated,
    );

    comboPairChart.allMetricsFields.addMetricsFieldToList(
      secondMetric,
      comboPairChart.component,
      comboPairChart.stream,
      secondMetric.streamType,
      secondMetric.activated,
    );

    if (firstMetricUnit === secondMetricUnit) {
      comboPairChart.setUnits(firstMetricUnit);
    } else {
      comboPairChart.setUnits(firstMetricUnit, secondMetricUnit);
    }

    comboPairChart.getAllMetrics();

    let pos: number = this.addChartToList(comboPairChart, true);
    this.metricsPairsCardsNames.push(comboPairChart.name);
    this.createGroupedMetricList();

    if (this.live) {
      this.subscribeToMetric(firstMetric, 'left', pos);

      if (firstMetricUnit === secondMetricUnit) {
        this.subscribeToMetric(secondMetric, 'left', pos);
      } else {
        this.subscribeToMetric(secondMetric, 'rightOne', pos);
      }
    }
  }

  subscribeToMetric(metric: MetricsFieldModel, listName: 'left' | 'rightOne' | 'rightTwo', pos: number): void {
    this.elastestRabbitmqService.createSubject(metric.streamType, metric.component, metric.stream);
    let index: string = this.getAbstractTJobExecIndex(metric.component);

    this.elastestRabbitmqService.createAndSubscribeToTopic(index, metric.streamType, metric.component, metric.stream).subscribe(
      (data: any) => {
        if (data['et_type'] === metric.etType && data.component === metric.component) {
          let parsedData: SingleMetricModel = this.monitoringService.convertToMetricTrace(data, metric);
          if (parsedData === undefined) {
            console.error('Undefined data received, not added to ' + metric.name);
          } else {
            if (this.chartsList[pos]) {
              this.chartsList[pos].addDataToSimpleMetricByGivenList(listName, metric, [parsedData]);
            }
          }
        }
      },
      (error: Error) => console.log(error),
    );
  }

  createSubjectAndSubscribe(component: string, stream: string, streamType: string): void {
    let index: string = this.getAbstractTJobExecIndex(component);
    if (index) {
      // Default chart
      if (!component || component === '') {
        // Subscribe to all component topics
        for (component of components) {
          let key: string = this.elastestRabbitmqService.getDestinationFromData(index, streamType, component, stream);
          this.elastestRabbitmqService.createSubject(streamType, component, stream);
          let subscription: Subscription = this.elastestRabbitmqService
            .createAndSubscribeToTopic(index, streamType, component, stream)
            .subscribe((data: any) => this.updateDefaultChartsData(data));

          this.subscriptions.set(key, subscription);
        }
      } else {
        // TODO but no necessary because only is used by default charts
      }
    }
  }

  createName(component: string, stream: string, etType: string, subtype: string): string {
    return this.createNameWithoutSubtype(component, stream, etType) + ' ' + subtype;
  }

  createNameWithoutSubtype(component: string, stream: string, etType: string): string {
    return component + ' ' + stream + ' ' + etType;
  }

  alreadyExist(name: string): boolean {
    for (let metric of this.chartsList) {
      if (metric.name === name) {
        return true;
      }
    }
    return false;
  }

  createGroupedMetricList(): void {
    let defaultGroupNum: number = 3;
    this.groupedMetricsList = this.createGroupedArray(this.chartsList.concat(this.combinedPairChartsList), defaultGroupNum);
  }

  createGroupedArray(arr: any[], chunkSize: number): any[] {
    let groups: any[] = [];
    let i: number;
    for (i = 0; i < arr.length; i += chunkSize) {
      groups.push(arr.slice(i, i + chunkSize));
    }
    return groups;
  }

  updateDefaultChartsData(data: any): void {
    for (let group of this.groupedMetricsList) {
      for (let metric of group) {
        if (metric.isDefault()) {
          // Only update default metrics
          metric.updateData(data);
        }
      }
    }

    if (this.allInOneMetrics) {
      this.allInOneMetrics.updateData(data);
    }
  }

  subscribeAllToEvents(): void {
    if (!this.live) {
      // If not is live, subscribe to events
      this.unsubscribeAllEvents();
      this.loaded =
        this.metricsChartCardComponents.toArray() && this.metricsChartCardComponents.toArray().length === this.chartsList.length;
      if (this.loaded) {
        this.metricsChartCardComponents.forEach((element: MetricsChartCardComponent) => {
          this.subscribeToEvents(element);
        });
      }
    }
  }

  unsubscribeAllEvents(): void {
    for (let subscription of this.chartsEventsSubscriptionsObs) {
      subscription.unsubscribe();
    }
    this.chartsEventsSubscriptionsObs = [];
  }

  subscribeToEvents(element: MetricsChartCardComponent): void {
    let eventSubscription: Subscription;
    eventSubscription = element.getTimelineSubscription().subscribe((data: any) => {
      this.updateTimeline(data);
      this.timelineObs.next(data);
    });

    this.chartsEventsSubscriptionsObs.push(eventSubscription);

    eventSubscription = element.getHoverSubscription().subscribe((data: any) => {
      this.hoverCharts(data);
      this.hoverObs.next(data.value);
    });

    this.chartsEventsSubscriptionsObs.push(eventSubscription);

    eventSubscription = element.getLeaveSubscription().subscribe((data: any) => {
      this.leaveCharts();
      this.leaveObs.next();
    });

    this.chartsEventsSubscriptionsObs.push(eventSubscription);
  }

  updateTimeline(domain: any): void {
    this.metricsChartCardComponents.forEach((element: any) => {
      element.updateDomain(domain);
    });
  }

  hoverCharts(item: any): void {
    this.metricsChartCardComponents.forEach((element: any) => {
      element.hoverCharts(item);
    });
  }

  leaveCharts(): void {
    this.metricsChartCardComponents.forEach((element: any) => {
      element.leaveCharts();
    });
  }

  removeAndUnsubscribeByListAndPos(pos: number, list: ESRabComplexMetricsModel[] = this.chartsList): void {
    let lastMetric: boolean = false;
    if (list.length === 1) {
      lastMetric = true;
    }

    if (list[pos]) {
      let component: string = list[pos].component;
      let stream: string = list[pos].stream;
      let name: string = list[pos].name;

      // If is live and is the last metric card, unsubscribe
      if (this.live && lastMetric && !this.allInOneMetrics) {
        this.unsubscribe(component, stream);
      }
      list.splice(pos, 1);
      this.createGroupedMetricList();
    }
  }

  removeAndUnsubscribeByNameAndList(name: string, list: ESRabComplexMetricsModel[] = this.chartsList): void {
    let pos: number = 0;
    for (let metricCard of list) {
      if (metricCard.name === name) {
        this.removeAndUnsubscribeByListAndPos(pos, list);
      }
      pos++;
    }
  }

  removeAndUnsubscribe(metric: ESRabComplexMetricsModel): void {
    if (metric) {
      this.removeAndUnsubscribeByNameAndList(metric.name, metric.isCombinedPair ? this.combinedPairChartsList : this.chartsList);
    }
  }

  removeAndUnsubscribeByName(name: string, isCombinedPair: boolean = false): void {
    this.removeAndUnsubscribeByNameAndList(name, isCombinedPair ? this.combinedPairChartsList : this.chartsList);
  }

  removeAndUnsubscribeByComponentAndStreamAndList(
    component: string,
    stream: string,
    list: ESRabComplexMetricsModel[] = this.chartsList,
  ): void {
    let position: number = 0;
    for (let metricCard of list) {
      if (metricCard.component === component && metricCard.stream === stream) {
        this.removeAndUnsubscribeByListAndPos(position, list);
        break;
      }
      position++;
    }
  }

  removeAndUnsubscribeByComponentAndStream(component: string, stream: string, isCombinedPair: boolean = false): void {
    this.removeAndUnsubscribeByComponentAndStreamAndList(
      component,
      stream,
      isCombinedPair ? this.combinedPairChartsList : this.chartsList,
    );
  }

  removeAndUnsubscribeAIO(): void {
    if (this.live && this.chartsList.length === 0 && this.allInOneMetrics) {
      this.unsubscribe(this.allInOneMetrics.component, this.allInOneMetrics.stream);
    }
    this.allInOneMetrics = undefined;
  }

  removeAllMetricsPairs(): void {
    for (let name of this.metricsPairsCardsNames) {
      this.removeAndUnsubscribeByName(name, true);
    }
    this.metricsPairsCardsNames = [];
  }

  isAllInOneMetricsCardShowing(): boolean {
    return this.allInOneMetrics !== undefined && this.allInOneMetrics !== null;
  }

  unsubscribe(component: string, stream: string): void {
    let streamType: string = 'composed_metrics';

    if (!stream || stream === '') {
      stream = defaultStreamMap.metrics;
    }

    if (!component || component === '') {
      // If default chart, unsubscribe to all component topics
      for (component of components) {
        let index: string = this.getAbstractTJobExecIndex(component);
        this.elastestRabbitmqService.unsuscribeFromTopic(index, streamType, component, stream);

        let key: string = this.elastestRabbitmqService.getDestinationFromData(index, streamType, component, stream);
        if (this.subscriptions.has(key)) {
          this.subscriptions.get(key).unsubscribe();
          this.subscriptions.delete(key);
        }
      }
    } else {
      let index: string = this.getAbstractTJobExecIndex(component);
      this.elastestRabbitmqService.unsuscribeFromTopic(index, streamType, component, stream);
    }
  }

  loadLastTraces(): void {
    for (let chart of this.chartsList) {
      chart.loadLastTraces();
    }
  }

  getAbstractTJobExecIndex(component: string): string {
    let index: string;
    switch (this.tJobExec.getAbstractTJobExecClass()) {
      case 'ExternalTJobExecModel':
        let externalTJobExec: ExternalTJobExecModel = this.tJobExec as ExternalTJobExecModel;
        index = externalTJobExec.getCurrentMonitoringIndex(component);
        break;
      case 'TJobExecModel':
        let tJobExec: TJobExecModel = this.tJobExec as TJobExecModel;
        index = tJobExec.getCurrentMonitoringIndex(component);
        break;
      default:
        // Abstract
        break;
    }
    return index;
  }

  showMarkView(markId: string = undefined): boolean {
    // if markId = undefined, normal view (time)
    this.initMetricsView(this.tJob, this.tJobExec, markId);
    return true;
  }

  getChartsListMetricFieldModels(): MetricsFieldModel[] {
    let list: MetricsFieldModel[] = [];
    if (this.chartsList) {
      for (let chart of this.chartsList) {
        if (chart.metric) {
          list.push(chart.metric);
        }
      }
    }
    return list;
  }
}
