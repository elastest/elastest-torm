import { TestSuiteModel } from '../test-suite/test-suite-model';

export class TestCaseModel {
  id: number;
  name: string;
  time: number;
  failureMessage: string;
  failureType: string;
  failureErrorLine: string;
  failureDetail: string;
  testSuite: TestSuiteModel;

  constructor() {
    this.id = 0;
    this.name = '';
    this.testSuite = undefined;
  }
}
