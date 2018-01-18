export class TestPlanModel {
    id: number;
    name: string;
    projectName: string;
    notes: string;
    active: string;
    public: string;

    constructor() {
        this.id = 0;
    }


    public getRouteString(): string {
        return 'TestLink ' + ' / TestSuite ' + this.name;
    }
}