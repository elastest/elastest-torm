import { AbstractTJobExecModel } from '../../models/abstract-tjob-exec-model';
import { ExternalTJobModel } from '../external-tjob/external-tjob-model';

export class ExternalTJobExecModel extends AbstractTJobExecModel {
  id: number;
  monitoringIndex: string;
  exTJob: ExternalTJobModel;
  envVars: any;

  constructor() {
    super();
    this.id = 0;
    this.monitoringIndex = '';
    this.exTJob = undefined;
    this.envVars = {};
  }

  public getRouteString(): string {
    return this.exTJob.getRouteString() + ' / Execution ' + this.id;
  }
}
