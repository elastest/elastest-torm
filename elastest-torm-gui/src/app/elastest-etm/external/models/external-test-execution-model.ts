import { ExternalIdModel } from './external-id-model';
import { ExternalTestCaseModel } from './external-test-case-model';

export class ExternalTestExecutionModel {
    id: ExternalIdModel;
    fields: any;
    result: string;
    exTestCase: ExternalTestCaseModel;

    constructor() {
        this.id = new ExternalIdModel();
        this.fields = undefined;
        this.exTestCase = undefined;
    }
}