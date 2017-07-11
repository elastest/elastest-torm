import { TdDigitsPipe, TdLoadingService } from '@covalent/core';
import { ColorSchemeModel } from './models/color-scheme-model';
import { SingleMetricModel } from './models/single-metric-model';

export interface MetricsViewModel {
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
  data: SingleMetricModel[];
  type: string;

  // ngx transform using covalent digits pipe
  axisDigits(val: any);
  updateData(data: any, position: number);
  parseData(data: any);
  parseCpuData(data: any);
  parseMemoryData(data: any);
}