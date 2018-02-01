import { ExternalProjectModel, ServiceType } from '../external-project/external-project-model';
import { ExternalTJobExecModel } from '../external-tjob-execution/external-tjob-execution-model';
import { ExternalTestCaseModel } from '../external-test-case/external-test-case-model';
import { SutModel } from '../../sut/sut-model';

export class ExternalTJobModel {
  id: number;
  name: string;
  externalId: string;
  externalSystemId: string;

  exProject: ExternalProjectModel;
  exTJobExecs: ExternalTJobExecModel[];
  exTestCases: ExternalTestCaseModel[];
  sut: SutModel;

  constructor() {
    this.id = 0;
    this.name = '';

    this.exProject = undefined;
    this.exTJobExecs = [];
    this.exTestCases = [];
    this.sut = undefined;
  }

  getServiceType(): ServiceType {
    let type: ServiceType;
    if (this.exProject !== undefined) {
      type = this.exProject.type;
    }
    return type;
  }

  public getRouteString(): string {
    return this.exProject.getRouteString() + ' / TJob ' + this.id;
  }
}
