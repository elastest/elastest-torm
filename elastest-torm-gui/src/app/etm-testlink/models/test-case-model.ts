import { ActionOnDuplicateModel } from './test-suite-model';

export class TestCaseModel {
    id: number;
    name: string;
    testSuiteId: number;
    testProjectId: number;
    authorLogin: string;
    summary: string;
    steps: TestCaseStepModel[];
    preconditions: string;
    testImportance: TestImportanceModel;
    executionType: ExecutionTypeModel;
    executionOrder: number;
    order: number;
    internalId: number;
    fullExternalId: string;
    checkDuplicatedName: boolean;
    actionOnDuplicatedName: ActionOnDuplicateModel;
    versionId: number;
    version: number;
    parentId: number;
    customFields: CustomFieldModel[];
    executionStatus: ExecutionStatusModel;
    platform: PlatformModel;
    featureId: number;

    constructor() {
        this.id = 0;
    }
}

export class TestCaseStepModel {
    id: number;
    testCaseVersionId: number;
    number: number;
    actions: string;
    expectedResults: string;
    active: boolean;
    executionType: ExecutionTypeModel;

    constructor() {
        this.id = 0;
        this.executionType = new ExecutionTypeModel();
    }

}

export class ExecutionTypeModel {
    value: 1 | 2; // 1 Manual, 2 Automated

    constructor() {
        this.value = 1;
    }

    setValue(value: number): void {
        if (value === 1 || value === 2) {
            this.value = value;
        }
    }
}

export class TestImportanceModel {
    value: 1 | 2 | 3; // LOW(1), MEDIUM(2), HIGH(3)

    constructor() {
        this.value = 2;
    }

    setValue(value: number): void {
        if (value === 1 || value === 2 || value === 3) {
            this.value = value;
        }
    }
}

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

export class ExecutionStatusModel {
    value: 'n' | 'p' | 'f' | 'b'; // NOT_RUN('n'), PASSED('p'), FAILED('f'), BLOCKED('b')
    constructor() {
        this.value = 'n';
    }

    setValue(value: string): void {
        if (value === 'n' || value === 'p' || value === 'f' || value === 'b') {
            this.value = value;
        }
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