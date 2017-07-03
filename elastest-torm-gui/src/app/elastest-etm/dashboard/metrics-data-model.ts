class SingleMetricModel {
  name: string;
  series: any[];

  constructor() {
    this.name = '';
    this.series = [];
  }
}

export class MetricsDataModel {
  data: SingleMetricModel[];
  constructor() {
    let test: SingleMetricModel = new SingleMetricModel();
    test.name = 'Test';
    let sut: SingleMetricModel = new SingleMetricModel();
    sut.name = 'Sut';
    this.data = [];
    this.data.push(test);
    this.data.push(sut);
  }
}