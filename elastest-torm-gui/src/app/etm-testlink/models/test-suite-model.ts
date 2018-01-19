import { ActionOnDuplicateValue } from "./test-case-model";

export class TestSuiteModel {
    id: number;
    testProjectId: number;
    name: string;
    details: string;
    parentId: number;
    order: number;
    checkDuplicatedName: boolean;
    actionOnDuplicatedName: ActionOnDuplicateValue;

    constructor() {
        this.id = 0;
    }

    public getRouteString(): string {
        return 'TestLink ' + ' / TestSuite ' + this.name;
    }
}

