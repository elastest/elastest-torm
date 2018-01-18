export class TestSuiteModel {
    id: number;
    testProjectId: number;
    name: string;
    details: string;
    parentId: number;
    order: number;
    checkDuplicatedName: boolean;
    actionOnDuplicatedName: ActionOnDuplicateModel;

    constructor() {
        this.id = 0;
    }
}

export class ActionOnDuplicateModel {
    value: 'block' | 'generate_new' | 'create_new_version';

    constructor() {
        this.value = 'block';
    }

    setValue(value: string): void {
        if (value === 'block' || value === 'generate_new' || value === 'create_new_version') {
            this.value = value;
        }
    }
}