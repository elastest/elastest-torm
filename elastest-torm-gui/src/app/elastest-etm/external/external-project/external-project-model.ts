import { ExternalTJobModel } from '../external-tjob/external-tjob-model';
import { SutModel } from '../../sut/sut-model';
import { AbstractProjectModel } from '../../models/abstract-project-model';

export type ServiceType = 'TESTLINK';
export class ExternalProjectModel extends AbstractProjectModel {
  id: number;
  name: string;
  type: ServiceType;
  externalId: string;
  externalSystemId: string;

  exTJobs: ExternalTJobModel[];
  suts: SutModel[];

  constructor() {
    super();
    this.id = 0;
    this.name = '';
    this.type = undefined;
    this.externalId = undefined;
    this.externalSystemId = undefined;

    this.exTJobs = [];
    this.suts = [];
  }

  public getRouteString(): string {
    return 'External / Project ' + this.id;
  }
}
