export class MonitoringQueryModel {
  indices: string[];
  component: string;
  componentService: string;
  etType: string;
  timestamp: Date;
  stream: string;
  streamType: string;
  containerName: string;
  selectedTerms: string[];
  message: string;
  timeRange: {
    lt: Date;
    gt: Date;
    lte: Date;
    gte: Date;
  };

  constructor() {
    this.indices = [];
    this.selectedTerms = [];
  }

  initTimeRange(): void {
    this.timeRange = {
      lt: undefined,
      gt: undefined,
      lte: undefined,
      gte: undefined,
    };
  }

  setTimeRange(from: Date = undefined, to: Date = undefined, includedFrom: boolean = true, includedTo: boolean = true): void {
    if (from !== undefined) {
      if (this.timeRange === undefined || this.timeRange === null) {
        this.initTimeRange();
      }

      if (includedFrom) {
        this.timeRange.gte = from;
      } else {
        this.timeRange.gt = from;
      }
    }

    if (to !== undefined) {
      if (this.timeRange === undefined || this.timeRange === null) {
        this.initTimeRange();
      }

      if (includedTo) {
        this.timeRange.lte = to;
      } else {
        this.timeRange.lt = to;
      }
    }
  }
}
