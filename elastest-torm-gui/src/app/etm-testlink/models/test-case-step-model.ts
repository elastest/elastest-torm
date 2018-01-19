import { ExecTypeValue } from './test-case-model';

export class TestCaseStepModel {
    id: number;
    testCaseVersionId: number;
    number: number;
    actions: string;
    expectedResults: string;
    active: boolean;
    executionType: ExecTypeValue;

    constructor() {
        this.id = 0;
        this.executionType = 'MANUAL';
    }

}