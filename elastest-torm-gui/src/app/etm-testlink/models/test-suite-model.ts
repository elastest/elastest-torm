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

    public getRouteString(): string {
        return 'TestLink ' + ' / TestSuite ' + this.name;
    }
}

export class ActionOnDuplicateModel {
    value: 'BLOCK' | 'GENERATE_NEW' | 'CREATE_NEW_VERSION';

    constructor() {
        this.value = 'BLOCK';
    }

    setValue(value: string): void {
        if (value === 'BLOCK' || value === 'GENERATE_NEW' || value === 'CREATE_NEW_VERSION') {
            this.value = value;
        }
    }
}