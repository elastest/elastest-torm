export class LogComparisonModel {
  name: string;
  component: string;
  stream: string;
  pair: string[];

  startDate: Date;
  endDate: Date;

  constructor() {
    this.name = '';

    this.component = '';
    this.stream = '';
    this.pair = [];
    this.startDate = undefined;
    this.endDate = undefined;
  }
}

export type comparisonMode = 'complete' | 'notimestamp' | 'timediff';
