import { TdDigitsPipe, TdLoadingService } from '@covalent/core';
import { MetricsViewModel } from '../metrics-view-model';
import { ColorSchemeModel } from './color-scheme-model';
import { LineChartMetricModel } from './linechart-metric-model';

export class MetricsModel implements MetricsViewModel {
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
  type: string;

  prevTraces: string[];
  prevLoaded: boolean;
  hidePrevBtn: boolean;

  constructor() {
    this.name = '';

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
    this.data = [];
    this.type = '';

    this.prevTraces = [];
    this.prevLoaded = false;
    this.hidePrevBtn = false;
  }

  axisDigits(val: any): any {
    return new TdDigitsPipe().transform(val);
  }

  loadPrevious() { }
}