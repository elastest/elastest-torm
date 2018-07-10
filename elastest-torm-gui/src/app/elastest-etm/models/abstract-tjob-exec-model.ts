import { AbstractTJobModel } from './abstract-tjob-model';
import { getResultIconByString } from '../../shared/utils';

export class AbstractTJobExecModel {
  id: number;
  tJob: AbstractTJobModel;
  monitoringIndex: string;
  result: string;
  resultMsg: string;
  startDate: Date;
  endDate: Date;

  constructor() {
    this.id = 0;
    this.monitoringIndex = '';
    this.result = '';
    this.resultMsg = '';
    this.startDate = undefined;
    this.endDate = undefined;
  }

  getTJobIndex(): string {
    let testIndex: string = this.monitoringIndex.split(',')[0];
    return testIndex;
  }

  getCurrentMonitoringIndex(component: string): string {
    let index: string = this.getTJobIndex();
    if (component === 'sut' || component.startsWith('sut_')) {
      index = this.getSutIndex();
    }
    return index;
  }

  public getAbstractTJobExecClass(): string {
    return 'AbstractTJobExecModel';
  }

  getSutIndex(): string {
    let sutIndex: string = '';
    if (this.tJob && this.tJob.hasSut()) {
      sutIndex = this.monitoringIndex.split(',')[1];
      if (!sutIndex) {
        sutIndex = this.getTJobIndex();
      }
    } else {
      sutIndex = this.getTJobIndex();
    }
    return sutIndex;
  }

  finished(): boolean {
    return (
      this.result === 'SUCCESS' ||
      this.result === 'FAIL' ||
      this.result === 'ERROR' ||
      this.result === 'STOPPED' ||
      this.result === 'FAILED'
    );
  }

  notExecuted(): boolean {
    return this.result === 'NOT_EXECUTED';
  }

  starting(): boolean {
    return this.result === 'IN PROGRESS' || this.result === 'STARTING TSS' || this.result === 'WAITING TSS';
  }

  stopped(): boolean {
    return this.result === 'STOPPED';
  }

  public getResultIcon(): any {
    let icon: any = {
      name: '',
      color: '',
      result: this.result,
    };
    if (this.finished() || this.notExecuted()) {
      icon = getResultIconByString(this.result);
    }
    return icon;
  }

  public getSplittedComposedMonitoringIndex(): string[] {
    return this.monitoringIndex.split(',');
  }
}
