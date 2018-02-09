import { TestCaseStepModel } from './test-case-step-model';

export class TLTestCaseModel {
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
    testCaseStatus: TestCaseStatusValue;

    constructor() {
        this.id = 0;
        this.testImportance = 'MEDIUM';
        this.executionStatus = 'NOT_RUN';
        this.executionType = 'MANUAL';
        this.actionOnDuplicatedName = 'BLOCK';
        this.testCaseStatus = 'DRAFT';
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

    getExecStatusValuesToExec(): ExecStatusValue[] {
        let status: ExecStatusValue[] = this.getExecStatusValues();
        status.shift();
        return status;
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

    getTestCaseStatusValues(): TestCaseStatusValue[] {
        return ['FINAL', 'FUTURE', 'OBSOLETE', 'REWORK', 'REVIEW_IN_PROGRESS', 'READY_FOR_REVIEW', 'DRAFT'];
    }
}

export type ActionOnDuplicateValue = 'BLOCK' | 'GENERATE_NEW' | 'CREATE_NEW_VERSION' | 'block' | 'generate_new' | 'create_new_version';
export type ExecTypeValue = 'MANUAL' | 'AUTOMATED' | 1 | 2; // 1 Manual, 2 Automated
export type TestImportanceValue = 'LOW' | 'MEDIUM' | 'HIGH' | 1 | 2 | 3; // LOW(1), MEDIUM(2), HIGH(3)
export type ExecStatusValue = 'NOT_RUN' | 'PASSED' | 'FAILED' | 'BLOCKED'
    | 'n' | 'p' | 'f' | 'b'; // NOT_RUN('n'), PASSED('p'), FAILED('f'), BLOCKED('b')
export type TestCaseStatusValue = 'FINAL' | 'FUTURE' | 'OBSOLETE' | 'REWORK' | 'REVIEW_IN_PROGRESS' | 'READY_FOR_REVIEW' | 'DRAFT'
    | 7 | 6 | 5 | 4 | 3 | 2 | 1; // FINAL(7), FUTURE(6), OBSOLETE(5), REWORK(4), REVIEW_IN_PROGRESS(3), READY_FOR_REVIEW(2), DRAFT(1);

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