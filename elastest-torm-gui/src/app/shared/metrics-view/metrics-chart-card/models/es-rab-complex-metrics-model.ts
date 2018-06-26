import { SingleMetricModel } from '../../models/single-metric-model';
import { LineChartMetricModel } from '../../models/linechart-metric-model';
import { MetricsFieldModel } from './metrics-field-model';
import { AllMetricsFields } from './all-metrics-fields-model';
import { ColorSchemeModel } from '../../models/color-scheme-model';
import { ComplexMetricsModel } from './complex-metrics-model';
import { defaultStreamMap } from '../../../defaultESData-model';
import { MonitoringService } from '../../../services/monitoring.service';

export class ESRabComplexMetricsModel extends ComplexMetricsModel {
  monitoringService: MonitoringService;

  allMetricsFields: AllMetricsFields;
  activatedFieldsList: boolean[];
  monitoringIndex: string;
  component: string;
  stream: string;

  leftChartAllData: LineChartMetricModel[];
  rightChartOneAllData: LineChartMetricModel[];
  rightChartTwoAllData: LineChartMetricModel[];

  constructor(monitoringService: MonitoringService, ignoreComponent: string = '') {
    super();
    this.monitoringService = monitoringService;

    this.allMetricsFields = new AllMetricsFields(true, ignoreComponent); // Object with a list of all metrics
    this.initActivatedFieldsList();
    this.monitoringIndex = '';
    this.component = '';
    this.stream = '';

    this.showXAxisLabel = false;
    this.xAxisLabel = 'Time';

    this.yAxisLabelLeft = 'Bytes';
    this.yAxisLabelRightOne = 'Usage %';
    this.yAxisLabelRightTwo = 'Amount / sec';

    this.yLeftAxisTickFormatting = this.normalFormat;
    this.yRightOneAxisTickFormatting = this.percentFormat;
    this.yRightTwoAxisTickFormatting = this.normalFormat;

    this.leftChartAllData = [];
    this.rightChartOneAllData = [];
    this.rightChartTwoAllData = [];

    this.scheme = this.getDefaultChartScheme();
  }

  getDefaultChartScheme(): ColorSchemeModel {
    let defaultChartScheme: ColorSchemeModel = new ColorSchemeModel();
    defaultChartScheme.name = 'defaultChartScheme';
    defaultChartScheme.selectable = true;
    defaultChartScheme.group = 'time';
    defaultChartScheme.domain = [
      '#01579b',
      '#7aa3e5',
      '#cc7f20',
      '#00bfa5',
      '#a29e0e',
      '#1dbf00',
      '#7e00bf',
      '#bf003e',
      '#ffac2f',
      '#666666',
      '#276279',
      '#68822a',
      '#a8385d',
      '#c77c4f',
      '#865c84',
      '#a8c314',
      '#c31444',
      '#14c38a',
      '#de56a8',
      '#ff0000',
    ];
    return defaultChartScheme;
  }

  getAllMetrics(): void {
    for (let metric of this.allMetricsFields.fieldsList) {
      this.monitoringService.getAllMetrics(this.monitoringIndex, metric).subscribe((data: LineChartMetricModel[]) => {
        switch (metric.unit) {
          case 'percent':
            this.rightChartOneAllData = this.rightChartOneAllData.concat(data);
            if (metric.activated) {
              this.rightChartOne = this.rightChartOne.concat(data);
            }
            break;
          case 'bytes':
            this.leftChartAllData = this.leftChartAllData.concat(data);
            if (metric.activated) {
              this.leftChart = this.leftChart.concat(data);
            }
            break;
          case 'amount/sec':
            this.rightChartTwoAllData = this.rightChartTwoAllData.concat(data);
            if (metric.activated) {
              this.rightChartTwo = this.rightChartTwo.concat(data);
            }
            break;
          default:
            break;
        }
      });
    }
  }

  updateData(trace: any): void {
    let positionsList: number[] = this.allMetricsFields.getPositionsList(trace['et_type'], trace.component, trace.stream);
    for (let position of positionsList) {
      let metric: MetricsFieldModel = this.allMetricsFields.fieldsList[position];
      let parsedData: SingleMetricModel = this.monitoringService.convertToMetricTrace(trace, metric);
      if (parsedData !== undefined) {
        this.addData(metric, [parsedData]);
      }
    }
  }

  addData(metric: MetricsFieldModel, newData: SingleMetricModel[]): void {
    switch (metric.unit) {
      case 'percent':
        this.rightChartOneAllData = this.addDataToGivenList(this.rightChartOneAllData, metric, newData);
        this.rightChartOne = this.filterDataByGivenList(this.rightChartOneAllData);
        break;
      case 'bytes':
        this.leftChartAllData = this.addDataToGivenList(this.leftChartAllData, metric, newData);
        this.leftChart = this.filterDataByGivenList(this.leftChartAllData);
        break;
      case 'amount/sec':
        this.rightChartTwoAllData = this.addDataToGivenList(this.rightChartTwoAllData, metric, newData);
        this.rightChartTwo = this.filterDataByGivenList(this.rightChartTwoAllData);
        break;
      default:
        break;
    }
  }

  addSimpleMetricTraces(data: LineChartMetricModel[]): void {
    // Used for Custom metrics, which are single metrics
    if (!data) {
      data = [];
    }
    if (this.leftChart.length === 0) {
      this.leftChart = this.leftChart.concat(data);
    } else {
      this.leftChart[0].series = this.leftChart[0].series.concat(data[0].series);
      this.leftChart = [...this.leftChart];
    }
  }

  initLineChartByGivenListName(listName: 'left' | 'rightOne' | 'rightTwo', name: string): void {
    let newLineChart: LineChartMetricModel = new LineChartMetricModel();
    newLineChart.name = name;
    switch (listName) {
      case 'left':
        this.leftChart.push(newLineChart);
        break;

      case 'rightOne':
        this.rightChartOne.push(newLineChart);
        break;

      case 'rightTwo':
        this.rightChartTwo.push(newLineChart);
        break;
      default:
        break;
    }
  }

  initLineChartByGivenList(list: LineChartMetricModel[], name: string): LineChartMetricModel[] {
    let newLineChart: LineChartMetricModel = new LineChartMetricModel();
    newLineChart.name = name;
    list.push(newLineChart);
    return list;
  }

  // Simple Metric (Not default metric)

  initSimpleMetricLineChart(name: string) {
    this.initLineChartByGivenListName('left', name);
  }

  addDataToSimpleMetric(metric: MetricsFieldModel, newData: SingleMetricModel[]): void {
    this.leftChart = this.addDataToGivenList(this.leftChart, metric, newData);
  }

  addDataToGivenList(
    list: LineChartMetricModel[],
    metric: MetricsFieldModel,
    newData: SingleMetricModel[],
  ): LineChartMetricModel[] {
    let lineChartPosition: number = this.getPosition(list, metric.name);
    if (lineChartPosition === undefined) {
      list = this.initLineChartByGivenList(list, metric.name);
      lineChartPosition = list.length - 1;
    }
    list[lineChartPosition].series = list[lineChartPosition].series.concat(newData);
    return [...list];
  }

  getPosition(list: LineChartMetricModel[], name: string): number {
    let position: number;
    let counter: number = 0;
    for (let line of list) {
      if (line.name === name) {
        position = counter;
        break;
      }
      counter++;
    }
    return position;
  }

  // END Simple Metric (Not default metric)

  loadPrevious(): void {
    let compareTrace: any = this.getOldTrace();
    if (this.isDefault()) {
      // Default chart
      let position: number = 0;
      for (let metric of this.allMetricsFields.fieldsList) {
        this.monitoringService
          .getPrevMetricsFromTrace(this.monitoringIndex, compareTrace, metric)
          .subscribe((data: LineChartMetricModel[]) => {
            if (data.length > 0) {
              this.addData(metric, data[0].series);
              this.prevLoaded = true;
              this.monitoringService.popupService.openSnackBar('Previous traces has been loaded', 'OK');
            } else {
              this.monitoringService.popupService.openSnackBar("There aren't previous traces to load", 'OK');
            }
          });
        position++;
      }
    } else {
      // Custom chart
      let individualChart: MetricsFieldModel = this.allMetricsFields.fieldsList.find(
        (currentIndividualChart: MetricsFieldModel) => currentIndividualChart.name === this.name.split(' ').join('_'),
      );
      if (individualChart) {
        this.monitoringService
          .getPrevMetricsFromTrace(this.monitoringIndex, compareTrace, individualChart)
          .subscribe((data: LineChartMetricModel[]) => {
            if (data.length > 0) {
              this.addSimpleMetricTraces(data);
              this.prevLoaded = true;
              this.monitoringService.popupService.openSnackBar('Previous traces has been loaded', 'OK');
            } else {
              this.monitoringService.popupService.openSnackBar("There aren't previous traces to load", 'OK');
            }
          });
      }
    }
  }

  getOldTrace(): any {
    let combinedList: LineChartMetricModel[] = this.rightChartOne.concat(this.leftChart).concat(this.rightChartTwo);
    let oldTrace: any = undefined;
    for (let singleLineChart of combinedList) {
      if (singleLineChart.series.length > 0) {
        if (oldTrace === undefined) {
          oldTrace = singleLineChart.series[0];
        }
        if (singleLineChart.series[0].name < oldTrace.name) {
          oldTrace = singleLineChart.series[0];
        }
      }
    }
    return oldTrace;
  }

  clearData(): void {
    this.rightChartOne = [...[]];
    this.leftChart = [...[]];
    this.rightChartTwo = [...[]];
  }

  filterData(): void {
    this.initActivatedFieldsList();

    this.rightChartOne = [...this.filterDataByGivenList(this.rightChartOneAllData)];
    this.leftChart = [...this.filterDataByGivenList(this.leftChartAllData)];
    this.rightChartTwo = [...this.filterDataByGivenList(this.rightChartTwoAllData)];
  }

  filterDataByGivenList(allList: LineChartMetricModel[]): LineChartMetricModel[] {
    let list: LineChartMetricModel[] = [];
    let position: number;
    for (let metric of allList) {
      position = this.allMetricsFields.getPositionByName(metric.name);
      if (this.activatedFieldsList[position]) {
        list.push(metric);
      }
    }
    return [...list];
  }

  activateAll(): void {
    for (let metric of this.allMetricsFields.fieldsList) {
      metric.activated = true;
    }
  }

  deactivateAll(): void {
    for (let metric of this.allMetricsFields.fieldsList) {
      metric.activated = false;
    }
  }

  activateAllMatchesByStreamTypeAndSubtype(stream: string, etType: string, subtype?: string): void {
    this.deactivateAll();
    for (let metric of this.allMetricsFields.fieldsList) {
      if (!subtype) {
        subtype = metric.subtype;
      }
      if (metric.etType === etType && metric.stream === stream && metric.subtype === subtype) {
        metric.activated = true;
        break;
      }
    }
    this.initActivatedFieldsList();
  }

  activateAllMatchesByNameList(nameList: string[]): void {
    this.deactivateAll();
    for (let name of nameList) {
      this.activateByName(name);
    }
    this.initActivatedFieldsList();
  }

  activateAndApplyByName(name: string): void {
    this.deactivateAll();
    this.activateByName(name);
    this.initActivatedFieldsList();
  }

  activateByName(name: string): void {
    for (let metric of this.allMetricsFields.fieldsList) {
      if (metric.name === name) {
        metric.activated = true;
        break;
      }
    }
  }

  activateAllMatchesByNameSuffix(suffix: string): void {
    let position: number = 0;
    for (let metric of this.allMetricsFields.fieldsList) {
      metric.activated = metric.name.endsWith(suffix);
      // To avoid this.initActivatedFieldsList() for
      this.activatedFieldsList[position] = metric.name.endsWith(suffix);
      position++;
    }
  }

  initActivatedFieldsList(): void {
    this.activatedFieldsList = [];
    this.allMetricsFields.fieldsList.map((data: MetricsFieldModel) => this.activatedFieldsList.push(data.activated));
  }

  isDefault(): boolean {
    let isDefault: boolean = false;

    if (
      this.component === '' &&
      (this.stream === defaultStreamMap.composed_metrics || this.stream === defaultStreamMap.atomic_metric)
    ) {
      isDefault = true; // Is normal chart with 1 or more components
    }
    if (this.component === '' && this.stream === '') {
      isDefault = true; // Is All-In-One (loadPrevious)
    }
    return isDefault;
  }

  loadLastTraces(size: number = 10): void {
    for (let metric of this.allMetricsFields.fieldsList) {
      // this.monitoringService.getLastMetricTraces(this.monitoringIndex, metric, size);
    } // TODO
  }
}
