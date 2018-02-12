import { TestSuiteModel } from '../test-suite/test-suite-model';
import { FileModel } from '../files-manager/file-model';

export class TestCaseModel {
  id: number;
  name: string;
  time: number;
  failureMessage: string;
  failureType: string;
  failureErrorLine: string;
  failureDetail: string;
  testSuite: TestSuiteModel;

  // Only GUI Attr
  files: FileModel[];

  constructor() {
    this.id = 0;
    this.name = '';
    this.testSuite = undefined;

    this.files = [];
  }

  public getResultIcon(): any {
    let icon: any = {
      name: '',
      color: '',
    };

    if (this.isSuccess()) {
      icon.name = 'check_circle';
      icon.color = '#669a13';
    } else {
      icon.name = 'error';
      icon.color = '#c82a0e';
    }

    return icon;
  }

  public isSuccess(): boolean {
    let isSuccess: boolean = true;

    if (this.failureDetail !== undefined && this.failureDetail !== null) {
      return false;
    }

    if (this.failureType !== undefined && this.failureType !== null) {
      return false;
    }

    if (this.failureErrorLine !== undefined && this.failureErrorLine !== null) {
      return false;
    }

    if (this.failureMessage !== undefined && this.failureMessage !== null) {
      return false;
    }

    return isSuccess;
  }
}
