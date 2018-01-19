import { ActionOnDuplicateModel } from './test-suite-model';
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

    public getRouteString(): string {
        return 'TestLink ' + ' / TestCase ' + this.name;
    }
}

export class ExecutionTypeModel {
    value: 'MANUAL' | 'AUTOMATED'; // 1 Manual, 2 Automated

    constructor() {
        this.value = 'MANUAL';
    }

    setValue(value: string): void {
        if (value === 'MANUAL' || value === 'MANUAL') {
            this.value = value;
        }
    }
}

export class TestImportanceModel {
    value: 'LOW' | 'MEDIUM' | 'HIGH'; // LOW(1), MEDIUM(2), HIGH(3)

    constructor() {
        this.value = 'MEDIUM';
    }

    setValue(value: string): void {
        if (value === 'LOW' || value === 'MEDIUM' || value === 'HIGH') {
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
    value: 'NOT_RUN' | 'PASSED' | 'FAILED' | 'BLOCKED'; // NOT_RUN('n'), PASSED('p'), FAILED('f'), BLOCKED('b')
    constructor() {
        this.value = 'NOT_RUN';
    }

    setValue(value: string): void {
        if (value === 'NOT_RUN' || value === 'PASSED' || value === 'FAILED' || value === 'BLOCKED') {
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