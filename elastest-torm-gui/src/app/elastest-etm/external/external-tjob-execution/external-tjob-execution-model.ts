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

  public getAbstractTJobExecClass(): string {
    return 'ExternalTJobExecModel';
  }

  getSutIndex(): string {
    let sutIndex: string = '';
    if (this.exTJob && this.exTJob.hasSut()) {
      sutIndex = this.monitoringIndex.split(',')[1];
      if (!sutIndex) {
        sutIndex = this.getTJobIndex();
      }
    } else {
      sutIndex = this.getTJobIndex();
    }
    return sutIndex;
  }

  getCurrentMonitoringIndex(component: string): string {
    let index: string = this.getTJobIndex();
    if (component === 'sut') {
      index = this.getSutIndex();
    }
    return index;
  }

  getBrowserLogObj(): any {
    let browserLog: any;
    if (this.monitoringIndex && this.envVars['BROWSER_SESSION_ID']) {
      browserLog = {
        monitoringIndex: this.monitoringIndex,
        component: 'tss_eus_browser_' + this.envVars['BROWSER_SESSION_ID'],
        stream: 'console',
        streamType: 'log',
        type: 'dynamic',
      };
    }
    return browserLog;
  }
}
