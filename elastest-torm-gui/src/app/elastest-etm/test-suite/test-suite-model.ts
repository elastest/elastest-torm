import { TestCaseModel } from '../test-case/test-case-model';
import { TJobExecModel } from '../tjob-exec/tjobExec-model';
import { defaultResult, getResultIconByString } from '../../shared/utils';

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
      let result: defaultResult = 'FAIL';

      if (this.errors === 0 && this.failures === 0) {
        result = 'SUCCESS';
      }

      icon = getResultIconByString(result);
    }

    return icon;
  }
}
