import { DefaultESFieldModel, components } from '../../../defaultESData-model';
import { MetricsFieldModel } from './metrics-field-model';

// Aux Classes
export class MetricFieldGroupModel {
    type: string;
    subtypes: SubtypesObjectModel[];

    constructor(type: string, subtypes: SubtypesObjectModel[]) {
        this.type = type;
        this.subtypes = subtypes.slice(0);
    }
}

export type Units = 'percent' | 'bytes' | 'amount/sec';

export class SubtypesObjectModel {
    subtype: string;
    unit: Units | string;

    constructor(subtype: string, unit: Units) {
        this.subtype = subtype;
        this.unit = unit;
    }
}


// Subtypes
export let cpuSubtypes: SubtypesObjectModel[] = [
    new SubtypesObjectModel('totalUsage', 'percent'),
];
export let memorySubtypes: SubtypesObjectModel[] = [
    new SubtypesObjectModel('usage', 'percent'),
    new SubtypesObjectModel('maxUsage', 'bytes'),
    // new SubtypesObjectModel('limit', 'bytes'),
];
export let blkioSubtypes: SubtypesObjectModel[] = [
    new SubtypesObjectModel('read_ps', 'bytes'),
    new SubtypesObjectModel('write_ps', 'bytes'),
    new SubtypesObjectModel('total_ps', 'bytes'),
];
export let netSubtypes: SubtypesObjectModel[] = [ // rx -> received; tx -> transmited
    new SubtypesObjectModel('rxBytes_ps', 'amount/sec'),
    new SubtypesObjectModel('rxErrors_ps', 'amount/sec'),
    new SubtypesObjectModel('rxPackets_ps', 'amount/sec'),
    new SubtypesObjectModel('txBytes_ps', 'amount/sec'),
    new SubtypesObjectModel('txErrors_ps', 'amount/sec'),
    new SubtypesObjectModel('txPackets_ps', 'amount/sec'),
];

export let metricFieldGroupList: MetricFieldGroupModel[] = [
    new MetricFieldGroupModel('cpu', cpuSubtypes),
    new MetricFieldGroupModel('memory', memorySubtypes),
    new MetricFieldGroupModel('blkio', blkioSubtypes),
    new MetricFieldGroupModel('net', netSubtypes),
];

// Main classes
export class AllMetricsFields {
    fieldsList: MetricsFieldModel[];

    constructor(withComponent: boolean = true) {
        this.fieldsList = [];
        if (withComponent) {
            for (let component of components) {
                this.fieldsList = this.fieldsList.concat(this.createFieldsListByComponent(component));
            }
        } else {
            this.fieldsList = this.getFieldListWithoutComponent();
        }
    }

    createFieldsListByComponent(component: string) {
        let list: MetricsFieldModel[] = [];
        for (let metricFieldGroup of metricFieldGroupList) { // Foreach type for this component
            list = list.concat(this.createFieldsListBySublist(metricFieldGroup, component));
        }
        return list;
    }

    createFieldsListBySublist(metricFieldGroup: MetricFieldGroupModel, component: string) {
        let list: MetricsFieldModel[] = [];

        for (let subtype of metricFieldGroup.subtypes) { // Foreach subtype of this type and this component
            let newField: MetricsFieldModel = new MetricsFieldModel(metricFieldGroup.type, subtype.subtype, subtype.unit, component);
            if (newField.type === 'cpu' && newField.subtype === 'totalUsage') { // Hardcoded
                newField.activated = true;
            }
            list.push(newField);
        }
        return list;
    }

    getFieldListWithoutComponent() {
        return this.createFieldsListByComponent('');
    }

    getPositionsList(type: string, component: string, stream?: string) {
        let namePrefix: string = component + '_' + type;
        if (stream) {
            namePrefix = component + '_' + stream + '_' + type;
        }
        let positionsList: number[] = [];
        let counter: number = 0;
        for (let metric of this.fieldsList) {
            if (metric.name.startsWith(namePrefix)) {
                positionsList.push(counter);
            }
            counter++;
        }
        return positionsList;
    }

    getPositionByName(name: string) {
        let position: number;
        let counter: number = 0;
        for (let metric of this.fieldsList) {
            if (metric.name === name) {
                position = counter;
                break;
            }
            counter++;
        }
        return position;
    }

    getDefaultUnitBySubtype(subtypeName: string) {
        for (let type of metricFieldGroupList) {
            for (let subtype of type.subtypes) {
                if (subtypeName === subtype.subtype) {
                    return subtype.unit;
                }
            }
        }
        return '';
    }
}