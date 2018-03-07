import { ExternalTestCaseModel } from '../external-test-case/external-test-case-model';
import { AbstractTJobExecModel } from '../../models/abstract-tjob-exec-model';
import { ExternalTJobExecModel } from '../external-tjob-execution/external-tjob-execution-model';

export class ExternalTestExecutionModel {
  id: number;
  monitoringIndex: string;
  fields: any;
  result: string;
  externalId: string;
  externalSystemId: string;
  exTestCase: ExternalTestCaseModel;
  exTJobExec: ExternalTJobExecModel;
  startDate: Date;
  endDate: Date;

  constructor() {
    this.id = 0;
    this.monitoringIndex = '';
    this.fields = undefined;
    this.exTestCase = undefined;
    this.exTJobExec = undefined;
    this.startDate = undefined;
    this.endDate = undefined;
  }

  finished(): boolean {
    return (
      this.result === 'SUCCESS' ||
      this.result === 'FAIL' ||
      this.result === 'ERROR' ||
      this.result === 'STOPPED' ||
      this.result === 'FAILED' ||
      this.result === 'PASSED' ||
      this.result === 'BLOCKED'
    );
  }

  notExecuted(): boolean {
    return this.result === 'NOT_EXECUTED';
  }

  stopped(): boolean {
    return this.result === 'STOPPED';
  }

  public getResultIcon(): any {
    let icon: any = {
      name: '',
      color: '',
    };
    if (this.finished() || this.notExecuted()) {
      switch (this.result) {
        case 'SUCCESS':
        case 'PASSED':
          icon.name = 'check_circle';
          icon.color = '#669a13';
          break;
        case 'FAIL':
        case 'FAILED':
          icon.name = 'error';
          icon.color = '#c82a0e';
          break;
        case 'STOPPED':
        case 'NOT_EXECUTED':
          icon.name = 'indeterminate_check_box';
          icon.color = '#c82a0e';
          break;
        case 'ERROR':
          icon.name = 'do_not_disturb';
          icon.color = '#c82a0e';
          break;
        case 'BLOCKED':
          icon.name = 'warning';
          icon.color = '#ffac2f';
          break;
        default:
          break;
      }
    }
    return icon;
  }
}
