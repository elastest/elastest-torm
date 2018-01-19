import { TestCaseStepModel } from './test-case-step-model';

export class TestCaseModel {
    id: number;
    name: string;
    testSuiteId: number;
    testProjectId: number;
    authorLogin: string;
    summary: string;
    steps: TestCaseStepModel[];
    preconditions: string;
    testImportance: TestImportanceValue;
    executionType: ExecTypeValue;
    executionOrder: number;
    order: number;
    internalId: number;
    fullExternalId: string;
    checkDuplicatedName: boolean;
    actionOnDuplicatedName: ActionOnDuplicateValue;
    versionId: number;
    version: number;
    parentId: number;
    customFields: CustomFieldModel[];
    executionStatus: ExecStatusValue;
    platform: PlatformModel;
    featureId: number;

    constructor() {
        this.id = 0;
        this.testImportance = 'MEDIUM';
        this.executionStatus = 'NOT_RUN';
        this.executionType = 'MANUAL';
        this.actionOnDuplicatedName = 'BLOCK';
        this.authorLogin = 'admin'; // TODO get author
        this.summary = ' ';
        this.preconditions = ' ';
    }

    public getRouteString(): string {
        return 'TestLink ' + ' / TestCase ' + this.name;
    }

    getImportanceValues(): TestImportanceValue[] {
        return ['LOW', 'MEDIUM', 'HIGH'];
    }


    getExecStatusValues(): ExecStatusValue[] {
        return ['NOT_RUN', 'PASSED', 'FAILED', 'BLOCKED'];
    }


    getExecTypeValues(): ExecTypeValue[] {
        return ['AUTOMATED', 'MANUAL'];
    }

    getActionOnDuplicatedNameValues(): ActionOnDuplicateValue[] {
        return ['BLOCK', 'GENERATE_NEW', 'CREATE_NEW_VERSION'];
    }

}

export type ActionOnDuplicateValue = 'BLOCK' | 'GENERATE_NEW' | 'CREATE_NEW_VERSION' | 'block' | 'generate_new' | 'create_new_version';
export type ExecTypeValue = 'MANUAL' | 'AUTOMATED' | 1 | 2; // 1 Manual, 2 Automated
export type TestImportanceValue = 'LOW' | 'MEDIUM' | 'HIGH' | 1 | 2 | 3; // LOW(1), MEDIUM(2), HIGH(3)
export type ExecStatusValue = 'NOT_RUN' | 'PASSED' | 'FAILED' | 'BLOCKED' | 'n' | 'p' | 'f' | 'b'; // NOT_RUN('n'), PASSED('p'), FAILED('f'), BLOCKED('b')

export class CustomFieldModel {
    id: number;
    name: string;
    label: string;
    type: number;
    possibleValues: string;
    defaultValue: string;
    validRegexp: string;
    lengthMin: number;
    lengthMax: number;
    showOnDesign: boolean;
    enableOnDesign: boolean;
    showOnExecution: boolean;
    enableOnExecution: boolean;
    showOnTestPlanDesign: boolean;
    enableOnTestPlanDesign: boolean;
    displayOrder: number; // ?
    location: number; // ?
    value: string;

    constructor() {
        this.id = 0;
    }
}

export class PlatformModel {
    id: number;
    name: string;
    notes: string;

    constructor() {
        this.id = 0;
    }
}