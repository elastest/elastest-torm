import { MetricsDataModel } from './metrics-data-model';
export class SingleMetricModel implements MetricsDataModel{
  name: any;
  value: any;
  timestamp: any;

  constructor() {
    this.name = '';
    this.value = '';
    this.timestamp = '';
  }
}
