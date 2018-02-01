import { ExternalTJobModel } from '../external-tjob/external-tjob-model';

export class ExternalTJobExecModel {
  id: number;
  esIndex: string;
  exTJob: ExternalTJobModel;
  envVars: any;

  constructor() {
    this.id = 0;
    this.esIndex = '';
    this.exTJob = undefined;
    this.envVars = {};
  }

  public getRouteString(): string {
    return this.exTJob.getRouteString() + ' / Execution ' + this.id;
  }
}
