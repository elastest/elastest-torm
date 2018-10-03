import { MetricsFieldModel } from '../metrics-chart-card/models/metrics-field-model';
import { ColorSchemeModel } from './color-scheme-model';
import { MetricsModel } from './metrics-model';
import { LineChartMetricModel } from './linechart-metric-model';
import { MonitoringService } from '../../services/monitoring.service';

export enum MetricsDataType {
  Test,
  Sut,
}

export class ETRESMetricsModel extends MetricsModel {
  // ElasTest RabbitMq ElasticSearch Metrics Model
  monitoringService: MonitoringService;

  name: string;
  colorScheme: ColorSchemeModel;
  gradient: boolean;

  showXAxis: boolean;
  showYAxis: boolean;
  showLegend: boolean;

  showXAxisLabel: boolean;
  xAxisLabel: string;

  showYAxisLabel: boolean;
  yAxisLabel: string;

  autoScale: boolean;

  timeline: boolean;
  data: LineChartMetricModel[];

  metricsField: MetricsFieldModel;

  monitoringIndex: string;
  component: string;

  prevTraces: string[];
  prevLoaded: boolean;
  hidePrevBtn: boolean;

  constructor(monitoringService: MonitoringService, metricsField: MetricsFieldModel) {
    super();
    this.monitoringService = monitoringService;

    this.metricsField = metricsField;
    this.etType = this.metricsField.etType;

    this.showXAxis = true;
    this.showYAxis = true;
    this.gradient = false;
    this.showLegend = true;
    this.showXAxisLabel = true;
    this.xAxisLabel = '';
    this.showYAxisLabel = true;
    this.yAxisLabel = '';
    this.timeline = true;
    this.autoScale = true;

    this.colorScheme = new ColorSchemeModel();
    this.colorScheme.domain = ['#ffac2f', '#666666'];

    this.component = '';
    this.monitoringIndex = '';

    this.prevTraces = [];
    this.prevLoaded = false;
    this.hidePrevBtn = false;

    // Data
    this.data = this.monitoringService.getInitMetricsData();

    this.initMetricsTypeData();
  }

  initMetricsTypeData(): void {
    this.name = this.metricsField.etType + ' ' + this.metricsField.subtype;
    switch (this.metricsField.unit) {
      case 'percent':
        this.yAxisLabel = this.metricsField.subtype + ' %';
        break;
      case 'bytes':
        this.yAxisLabel = 'Bytes';
        break;
      case 'amount/sec':
        this.yAxisLabel = 'Amount / sec';
        break;
      default:
        this.name = '';
    }
  }

  getAllMetrics(): void {
    this.monitoringService.getAllMetrics(this.monitoringIndex, this.metricsField).subscribe((data) => {
      this.data = data;
      this.data = [...this.data];
    });
  }

  loadPrevious(): void {
    let compareTrace: any = this.getOldTrace();
    this.monitoringService.getPrevMetricsFromTrace(this.monitoringIndex, compareTrace, this.metricsField).subscribe(
      (data) => {
        if (data.length > 0) {
          if (data[MetricsDataType.Test].series.length > 0) {
            this.data[MetricsDataType.Test].series.unshift.apply(
              this.data[MetricsDataType.Test].series,
              data[MetricsDataType.Test].series,
            );
            this.prevLoaded = true;
          }
          if (data[MetricsDataType.Sut].series.length > 0) {
            this.data[MetricsDataType.Sut].series.unshift.apply(
              this.data[MetricsDataType.Sut].series,
              data[MetricsDataType.Sut].series,
            );
            this.prevLoaded = true;
          }
          this.data = [...this.data];
          this.monitoringService.popupService.openSnackBar('Previous traces has been loaded', 'OK');
        } else {
          this.monitoringService.popupService.openSnackBar("There aren't previous traces to load", 'OK');
        }
      },
      (error: Error) => {
        // this.monitoringService.popupService.openSnackBar("Error on load previous traces", 'OK');
      },
    );
  }

  getOldTrace(): any {
    let oldTrace: any = undefined;
    for (let singleLineChart of this.data) {
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

  updateData(trace: any): void {
    let position: number = this.monitoringService.getMetricPosition(trace.component);

    if (position !== undefined) {
      let parsedData: any = this.monitoringService.convertToMetricTrace(trace, this.metricsField);
      if (parsedData !== undefined) {
        this.data[position].series.push(parsedData);
        this.data = [...this.data];
      }
    }
  }
}
