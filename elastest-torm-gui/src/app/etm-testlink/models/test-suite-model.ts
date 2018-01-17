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

    }
}

export class ActionOnDuplicateModel {
    value: 'block' | 'generate_new' | 'create_new_version';

    constructor() {
        this.value = 'block';
    }
}