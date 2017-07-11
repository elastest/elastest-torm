import { TdDigitsPipe, TdLoadingService } from '@covalent/core';
import { MetricsViewModel } from '../metrics-view-model';
import { ColorSchemeModel } from './color-scheme-model';
import { SingleMetricModel } from './single-metric-model';

export class MetricsDataModel implements MetricsViewModel {
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

  constructor() {
    this.name = '';

    this.showXAxis = true;
    this.showYAxis = true;
    this.gradient = false;
    this.showLegend = false;
    this.showXAxisLabel = true;
    this.xAxisLabel = '';
    this.showYAxisLabel = true;
    this.yAxisLabel = '';
    this.timeline = false;
    this.autoScale = true;

    this.colorScheme = new ColorSchemeModel();
    this.data = [];
    this.type = '';
  }

  axisDigits(val: any): any {
    return new TdDigitsPipe().transform(val);
  }

  updateData(data: any, position: number) {
    let parsedData: any = this.parseData(data);

    if (parsedData !== undefined) {
      this.data[position].series.push(parsedData);
      this.data = [...this.data];
    }
  }

  parseData(data: any) {
    let parsedData: any = undefined;
    if (data.type === 'cpu' && this.type === 'cpu') {
      parsedData = this.parseCpuData(data);

    } else if (data.type === 'memory' && this.type === 'memory') {
      parsedData = this.parseMemoryData(data);
    }
    return parsedData;
  }

  parseCpuData(data: any) {
    console.log('parsecpuData', data);
    let parsedData: any = {
      'value': data.cpu.totalUsage,
      'name': new Date('' + data['@timestamp']),
    };
    return parsedData;
  }


  parseMemoryData(data: any) {
    let perMemoryUsage = data.memory.usage * 100 / data.memory.limit;
    let parsedData: any = {
      'value': perMemoryUsage,
      'name': new Date('' + data['@timestamp']),
    };
    return parsedData;
  }
}