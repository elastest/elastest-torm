import { ExternalTJobModel } from '../external-tjob/external-tjob-model';

export class ExternalTJobExecModel {
  id: number;
  monitoringIndex: string;
  exTJob: ExternalTJobModel;
  envVars: any;

  constructor() {
    this.id = 0;
    this.monitoringIndex = '';
    this.exTJob = undefined;
    this.envVars = {};
  }

  public getRouteString(): string {
    return this.exTJob.getRouteString() + ' / Execution ' + this.id;
  }
}
