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

        let componentTypePrefix: string = '';
        if (componentType !== undefined && componentType !== '') {
            componentTypePrefix = componentType + '_';
        }

        this.name = componentTypePrefix + type + '_' + subtype;

        this.activated = false;
    }

    changeActive($event) {
        this.activated = $event.checked;
    }
}