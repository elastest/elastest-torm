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
  exTJobs: ExternalTJobModel[];
  exTestExecs: ExternalTestExecutionModel[];

  constructor() {
    this.id = 0;
    this.name = '';
    this.fields = undefined;
    this.exTJobs = [];
    this.exTestExecs = [];
  }

  public getRouteString(specificExTJob?: ExternalTJobModel): string {
    if (specificExTJob === undefined) {
      if (this.getFirstExTJob() !== undefined) {
        specificExTJob = this.getFirstExTJob();
      }
    }

    if (specificExTJob !== undefined) {
      return specificExTJob.getRouteString() + ' / Case ' + this.id;
    }
    return undefined;
  }

  getServiceType(): ServiceType {
    let type: ServiceType;
    if (this.getFirstExTJob() !== undefined) {
      type = this.getFirstExTJob().getServiceType();
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

  getFirstExTJob(): ExternalTJobModel {
    if (this.exTJobs !== undefined && this.exTJobs.length > 0) {
      return this.exTJobs[0];
    }
    return undefined;
  }
}
