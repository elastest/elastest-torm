import { TestCaseModel } from '../test-case/test-case-model';
import { TJobExecModel } from '../tjob-exec/tjobExec-model';

export class TestSuiteModel {
  id: number;
  name: string;
  timeElapsed: number;
  errors: number;
  failures: number;
  skipped: number;
  flakes: number;
  numTests: number;
  testCases: TestCaseModel[];
  tJobExec: TJobExecModel;

  constructor() {
    this.id = 0;
    this.name = '';
    this.tJobExec = undefined;
  }

  public getResultIcon(): any {
    let icon: any = {
      name: '',
      color: '',
    };

    if (this.errors !== undefined && this.errors !== null && this.failures !== undefined && this.failures !== null) {
      if (this.errors === 0 && this.failures === 0) {
        icon.name = 'check_circle';
        icon.color = '#669a13';
      } else {
        icon.name = 'error';
        icon.color = '#c82a0e';
      }
    }

    return icon;
  }
}
