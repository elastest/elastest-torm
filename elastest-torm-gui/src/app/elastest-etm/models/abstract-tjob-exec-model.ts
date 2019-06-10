import { AbstractTJobModel } from './abstract-tjob-model';
import { getResultIconByString } from '../../shared/utils';
import { MonitorMarkModel } from '../etm-monitoring-view/monitor-mark.model';

export class AbstractTJobExecModel {
  id: number;
  tJob: AbstractTJobModel;
  monitoringIndex: string;
  result: string;
  resultMsg: string;
  startDate: Date;
  endDate: Date;
  monitoringStorageType: '' | 'elasticsearch' | 'mysql';
  monitoringMarks: MonitorMarkModel[];
  monitoringMarksIds: string[] = [];
  activeView: string;

  constructor(exec: AbstractTJobExecModel = undefined) {
    if (exec) {
      this.id = exec.id;
      this.monitoringIndex = exec.monitoringIndex;
      this.result = exec.result;
      this.resultMsg = exec.resultMsg;
      this.startDate = exec.startDate;
      this.endDate = exec.endDate;
      this.monitoringMarks = exec.monitoringMarks ? exec.monitoringMarks : [];
      this.activeView = exec.activeView;
    } else {
      this.id = 0;
      this.monitoringIndex = '';
      this.result = '';
      this.resultMsg = '';
      this.startDate = undefined;
      this.endDate = undefined;
      this.monitoringMarks = [];
      this.activeView = undefined;
    }
  }

  getMonitoringIndexAsList(): string[] {
    let monitoringIndexList: string[] = [];
    if (this.monitoringIndex) {
      monitoringIndexList = this.monitoringIndex.split(',');
    }
    return monitoringIndexList;
  }

  getTJobIndex(): string {
    let testIndex: string = this.getMonitoringIndexAsList()[0];
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

  resultError(): boolean {
    return this.result === 'ERROR';
  }

  notExecuted(): boolean {
    return this.result === 'NOT_EXECUTED';
  }

  finishedOrNotExecuted(): boolean {
    return this.finished() || this.notExecuted();
  }

  starting(): boolean {
    return this.result === 'IN PROGRESS' || this.result === 'STARTING TSS' || this.result === 'WAITING TSS';
  }

  executing(): boolean {
    return this.result === 'EXECUTING_TEST' || this.result === 'EXECUTING TEST';
  }

  stopped(): boolean {
    return this.result === 'STOPPED';
  }

  paused(): boolean {
    return this.result === 'PAUSED';
  }

  public getResultIcon(): any {
    let icon: any = {
      name: '',
      color: '',
      result: this.result,
    };
    if (this.finished() || this.notExecuted() || this.paused()) {
      icon = getResultIconByString(this.result);
    }
    return icon;
  }

  public getSplittedComposedMonitoringIndex(): string[] {
    return this.monitoringIndex.split(',');
  }

  hasMonitoringMarks(): boolean {
    return this.monitoringMarks && this.monitoringMarks.length > 0;
  }

  getMonitoringMarkIds(): string[] {
    return this.monitoringMarksIds;
  }

  addMonitoringMark(mark: MonitorMarkModel): void {
    this.monitoringMarks.push(mark);
    if (this.monitoringMarksIds.indexOf(mark.id) === -1) {
      this.monitoringMarksIds.push(mark.id);
    }
  }

  getMonitoringMarksById(id: string): MonitorMarkModel[] {
    let marks: MonitorMarkModel[] = [];
    for (let mark of this.monitoringMarks) {
      if (mark.id === id) {
        marks.push(mark);
      }
    }
    return marks;
  }
}
