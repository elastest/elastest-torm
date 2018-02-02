import { AbstractTJobModel } from './abstract-tjob-model';

export class AbstractTJobExecModel {
  id: number;
  tJob: AbstractTJobModel;
  monitoringIndex: string;

  constructor() {
    this.id = 0;
    this.monitoringIndex = '';
  }

  getTJobIndex(): string {
    let testIndex: string = this.monitoringIndex.split(',')[0];
    return testIndex;
  }

  getCurrentMonitoringIndex(component: string) {
    let index: string = this.getTJobIndex();
    if (component === 'sut') {
      index = this.getSutIndex();
    }
    return index;
  }

  getSutIndex(): string {
    let sutIndex: string = '';
    if (this.tJob.hasSut()) {
      sutIndex = this.monitoringIndex.split(',')[1];
      if (!sutIndex) {
        sutIndex = this.getTJobIndex();
      }
    } else {
      sutIndex = this.getTJobIndex();
    }
    return sutIndex;
  }
}
