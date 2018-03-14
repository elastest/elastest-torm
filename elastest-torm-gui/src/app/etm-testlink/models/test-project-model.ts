export class TestProjectModel {
    id: number;
    name: string;
    prefix: string;
    notes: string;
    enableRequirements: boolean;
    enableTestPriority: boolean;
    enableAutomation: boolean;
    enableInventory: boolean;
    active: boolean;
    public: boolean;

    constructor() {
        this.id = 0;
    }


    public getRouteString(): string {
        return this.name;
    }
}