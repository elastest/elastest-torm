export class BuildModel {
    id: number;
    testPlanId: number;
    name: string;
    notes: string;

    constructor() {
        this.id = 0;
    }


    public getRouteString(): string {
        return 'TestLink ' + ' / Build ' + this.name;
    }
}
