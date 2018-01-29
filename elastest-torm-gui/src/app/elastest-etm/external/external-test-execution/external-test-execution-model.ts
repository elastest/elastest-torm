import { ExternalTestCaseModel } from '../external-test-case/external-test-case-model';

export class ExternalTestExecutionModel {
    id: number;
    esIndex: string;
    fields: any;
    result: string;
    externalId: string;
    externalSystemId: string;
    exTestCase: ExternalTestCaseModel;

    constructor() {
        this.id = 0;
        this.esIndex = '';
        this.fields = undefined;
        this.exTestCase = undefined;
    }
}