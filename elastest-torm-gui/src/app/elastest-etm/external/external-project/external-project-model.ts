import { ExternalTJobModel } from '../external-tjob/external-tjob-model';
import { SutModel } from '../../sut/sut-model';

export type ServiceType = 'TESTLINK';
export class ExternalProjectModel {
    id: number;
    name: string;
    type: ServiceType;
    externalId: string;
    externalSystemId: string;

    exTJobs: ExternalTJobModel[];
    suts: SutModel[];

    constructor() {
        this.id = 0;
        this.name = '';
        this.type = undefined;
        this.externalId = undefined;
        this.externalSystemId = undefined;

        this.exTJobs = [];
        this.suts = [];

    }

}