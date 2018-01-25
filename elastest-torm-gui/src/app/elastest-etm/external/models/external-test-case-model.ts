import { ExternalIdModel } from './external-id-model';
import { ExternalProjectModel } from './external-project-model';

export class ExternalTestCaseModel {
    id: ExternalIdModel;
    name: string;
    fields: any;
    exProject: ExternalProjectModel;

    constructor() {
        this.id = new ExternalIdModel();
        this.name = '';
        this.fields = undefined;
        this.exProject = undefined;
    }
}