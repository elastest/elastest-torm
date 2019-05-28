import { ExternalProjectModel, ServiceType } from '../external-project/external-project-model';
import { ExternalTJobExecModel } from '../external-tjob-execution/external-tjob-execution-model';
import { ExternalTestCaseModel } from '../external-test-case/external-test-case-model';
import { SutModel } from '../../sut/sut-model';
import { AbstractTJobModel } from '../../models/abstract-tjob-model';
import { DashboardConfigModel } from '../../tjob/dashboard-config-model';
import { EsmServiceModel } from '../../../elastest-esm/esm-service.model';

export class ExternalTJobModel extends AbstractTJobModel {
  id: number;
  name: string;
  externalId: string;
  externalSystemId: string;

  exProject: ExternalProjectModel;
  exTJobExecs: ExternalTJobExecModel[];
  exTestCases: ExternalTestCaseModel[];
  sut: SutModel;
  esmServicesString: string;
  esmServices: EsmServiceModel[];
  esmServicesChecked: number;

  execDashboardConfig: string;
  execDashboardConfigModel: DashboardConfigModel;

  constructor() {
    super();
    this.id = 0;
    this.name = '';

    this.exProject = undefined;
    this.exTJobExecs = [];
    this.exTestCases = [];
    this.sut = undefined;

    this.esmServicesString = '';
    this.esmServices = [];
    this.esmServicesChecked = 0;

    this.execDashboardConfigModel = new DashboardConfigModel(undefined, false, false, false);
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

  public getAbstractTJobClass(): string {
    return 'ExternalTJobModel';
  }

  public withSut(): boolean {
    return this.sut !== undefined && this.sut.id > 0;
  }
}
