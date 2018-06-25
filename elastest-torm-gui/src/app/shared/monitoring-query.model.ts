export class MonitoringQueryModel {
  indices: string[];
  component: string;
  componentService: string;
  etType: string;
  timestamp: Date;
  stream: string;
  streamType: string;
  containerName: string;

  constructor() {
    this.indices = [];
  }
}
