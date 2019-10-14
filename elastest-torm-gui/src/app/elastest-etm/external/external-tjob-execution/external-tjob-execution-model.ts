import { AbstractTJobExecModel } from '../../models/abstract-tjob-exec-model';
import { ExternalTJobModel } from '../external-tjob/external-tjob-model';
import { ExternalTestExecutionModel } from '../external-test-execution/external-test-execution-model';

export class ExternalTJobExecModel extends AbstractTJobExecModel {
  id: number;
  monitoringIndex: string;
  exTJob: ExternalTJobModel;
  envVars: any;
  exTestExecs: ExternalTestExecutionModel[];
  lastExecutedTCaseId: string;
  executionConfig: string;
  executionConfigObj: {};

  constructor() {
    super();
    this.id = 0;
    this.monitoringIndex = '';
    this.exTJob = undefined;
    this.envVars = {};
    this.exTestExecs = [];
    this.lastExecutedTCaseId = undefined;
    this.executionConfig = '{}';
    this.executionConfigObj = {};
  }

  public getRouteString(): string {
    return 'TJobExecution ' + this.id;
  }

  public getAbstractTJobExecClass(): string {
    return 'ExternalTJobExecModel';
  }

  getSutIndex(): string {
    let sutIndex: string = '';
    if (this.exTJob && this.exTJob.hasSut()) {
      sutIndex = this.getMonitoringIndexAsList()[1];
      if (!sutIndex) {
        sutIndex = this.getTJobExecIndex();
      }
    } else {
      sutIndex = this.getTJobExecIndex();
    }
    return sutIndex;
  }

  getCurrentMonitoringIndex(component: string): string {
    let index: string = this.getTJobExecIndex();
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
        component: 'tss_eus_browser_externalTJob_' + this.id + '_0',
        stream: 'console',
        streamType: 'log',
        type: 'dynamic',
      };
    }
    return browserLog;
  }

  updateResultByTestExecsResults(): void {
    let result: string = 'SUCCESS';
    for (let exec of this.exTestExecs) {
      result = result === 'SUCCESS' && exec.result === 'PASSED' ? 'SUCCESS' : 'FAIL';
    }
    this.result = result;
  }
}

export class ExternalTJobExecFinishedModel {
  finished: boolean;
  exec: ExternalTJobExecModel;

  constructor(finished: boolean, exec: ExternalTJobExecModel) {
    this.finished = finished;
    this.exec = exec;
  }
}
