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

  constructor() {
    this.indices = [];
    this.selectedTerms = [];
  }
}
