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
    let result: defaultResult = 'FAIL';
    if (this.isSuccess()) {
      result = 'SUCCESS';
    }
    icon = getResultIconByString(result);

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

  setTestCaseFiles(tJobExecFiles: FileModel[]): FileModel[] {
    let newExecFiles: FileModel[] = [];
    for (let file of tJobExecFiles) {
      if (file.name.startsWith(this.name + '_')) {
        this.files.push(file);
      } else {
        newExecFiles.push(file);
      }
    }
    return newExecFiles;
  }
}
