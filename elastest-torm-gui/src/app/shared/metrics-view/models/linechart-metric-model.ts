import { MetricsDataModel } from './metrics-data-model';
import { SingleMetricModel } from './single-metric-model';
export class LineChartMetricModel implements MetricsDataModel {
  name: string;
  series: SingleMetricModel[];

  constructor(name: string = '') {
    this.name = name;
    this.series = [];
  }
}
