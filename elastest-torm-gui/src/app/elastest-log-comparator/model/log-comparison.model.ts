export class LogComparisonModel {
  name: string;
  component: string;
  components: string[];
  stream: string;
  pair: string[];

  startDate: Date;
  endDate: Date;

  constructor() {
    this.name = '';

    this.component = '';
    this.components = [];
    this.stream = '';
    this.pair = [];
    this.startDate = undefined;
    this.endDate = undefined;
  }

  isSamePair(pair: string[]): boolean {
    let same: boolean = false;
    if (pair && this.pair && pair.length === 2 && this.pair.length === 2) {
      same = pair[0] === this.pair[0] && pair[1] === this.pair[1];
    }

    return same;
  }
}

export type comparisonMode = 'complete' | 'notimestamp' | 'timediff';
export type viewMode = 'complete' | 'testslogs' | 'failedtests';
