import { Units } from './all-metrics-fields-model';


export class MetricsFieldModel {
    type: string;
    subtype: string;
    unit: Units;
    componentType: string;
    name: string;
    activated: boolean;

    constructor(type: string, subtype: string, unit: Units, componentType: string) {
        this.type = type;
        this.subtype = subtype;
        this.unit = unit;
        this.componentType = componentType;

        this.name = componentType + '_' + type + '_' + subtype;
        this.activated = true;
    }

    changeActive($event) {
        this.activated = $event.checked;
    }
}