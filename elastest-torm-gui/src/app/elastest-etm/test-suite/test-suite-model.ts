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

  constructor(testSuite: TestSuiteModel = undefined) {
    if (testSuite !== undefined) {
      this.id = testSuite.id;
      this.name = testSuite.name;
      this.timeElapsed = testSuite.timeElapsed;
      this.errors = testSuite.errors;
      this.failures = testSuite.failures;
      this.skipped = testSuite.skipped;
      this.flakes = testSuite.flakes;
      this.numTests = testSuite.numTests;
      this.testCases = testSuite.testCases;
      this.tJobExec = testSuite.tJobExec;
    } else {
      this.id = 0;
      this.name = '';
      this.tJobExec = undefined;
    }
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
        if (this.skipped === this.numTests) {
          result = 'SKIPPED';
        }
      }

      icon = getResultIconByString(result);
    }

    return icon;
  }

  public isSkipped(): boolean {
    return this.skipped !== undefined && this.skipped > 0;
  }

  public isFailed(): boolean {
    return !this.isSkipped() && this.errors !== undefined && this.errors > 0;
  }

  public isSuccess(): boolean {
    if (this.isFailed() || this.isSkipped()) {
      return false;
    }
    return true;
  }
}
