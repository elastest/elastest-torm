import { SingleMetricModel } from './single-metric-model';
export class LineChartMetricModel {
  name: string;
  series: SingleMetricModel[];

  constructor() {
    this.name = '';
    this.series = [];
  }
}
