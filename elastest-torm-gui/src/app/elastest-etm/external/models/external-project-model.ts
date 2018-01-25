import { ExternalIdModel } from './external-id-model';

export type ServiceType = 'TESTLINK';
export class ExternalProjectModel {
    id: ExternalIdModel;
    name: string;
    type: ServiceType;

    constructor() {
        this.id = new ExternalIdModel();
        this.name = '';
        this.type = undefined;
    }

}