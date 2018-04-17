import { ExternalTJobModel } from '../external-tjob/external-tjob-model';
import { ExternalTestExecutionModel } from '../external-test-execution/external-test-execution-model';
import { ServiceType } from '../external-project/external-project-model';
import { FileModel } from '../../files-manager/file-model';

export class ExternalTestCaseModel {
  id: number;
  name: string;
  fields: any;
  externalId: string;
  externalSystemId: string;
  exTJob: ExternalTJobModel;
  exTestExecs: ExternalTestExecutionModel[];

  constructor() {
    this.id = 0;
    this.name = '';
    this.fields = undefined;
    this.exTJob = undefined;
    this.exTestExecs = [];
  }

  public getRouteString(): string {
    return this.exTJob.getRouteString() + ' / Case ' + this.id;
  }

  getServiceType(): ServiceType {
    let type: ServiceType;
    if (this.exTJob !== undefined) {
      type = this.exTJob.getServiceType();
    }
    return type;
  }

  setTestCaseFiles(tJobExecFiles: FileModel[]): FileModel[] {
    let execFilesFiltered: FileModel[] = [];
    for (let file of tJobExecFiles) {
      if (file.name.startsWith(this.name.split(' ').join('-') + '_')) {
        execFilesFiltered.push(file);
      }
    }
    return execFilesFiltered;
  }
}
