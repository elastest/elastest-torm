import { TestSuiteModel } from '../test-suite/test-suite-model';
import { FileModel } from '../files-manager/file-model';
import { getResultIconByString, defaultResult } from '../../shared/utils';

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
    let result: defaultResult = this.getResult();
    icon = getResultIconByString(result);

    return icon;
  }

  public getResult(): defaultResult {
    let result: defaultResult = 'FAIL';
    if (this.isSuccess()) {
      result = 'SUCCESS';
    } else if (this.isSkipped()) {
      result = 'SKIPPED';
    }

    return result;
  }

  public isSuccess(): boolean {
    if (this.isFailed() || this.isSkipped()) {
      return false;
    }
    return true;
  }

  public isFailed(): boolean {
    if (!this.isSkipped()) {
      if (this.failureDetail !== undefined && this.failureDetail !== null) {
        return true;
      }

      if (this.failureType !== undefined && this.failureType !== null) {
        return true;
      }

      if (this.failureErrorLine !== undefined && this.failureErrorLine !== null) {
        return true;
      }

      if (this.failureMessage !== undefined && this.failureMessage !== null) {
        return true;
      }
    }
    return false;
  }

  public isSkipped(): boolean {
    let skipped: boolean = false;

    if (
      this.failureType !== undefined &&
      this.failureType !== null &&
      this.failureType === 'skipped' &&
      this.failureMessage !== undefined &&
      this.failureMessage !== null &&
      this.failureMessage === 'skipped'
    ) {
      skipped = true;
    }

    return skipped;
  }
  setTestCaseFiles(tJobExecFiles: FileModel[]): FileModel[] {
    let newExecFiles: FileModel[] = [];
    for (let file of tJobExecFiles) {
      // If testCase.name has spaces, replace with -
      if (file.name.startsWith(this.name.replace(/\s+/g, '-') + '_')) {
        this.files.push(file);
      } else {
        newExecFiles.push(file);
      }
    }
    return newExecFiles;
  }
}
