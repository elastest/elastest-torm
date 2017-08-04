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
    unit: Units;

    constructor(subtype: string, unit: Units) {
        this.subtype = subtype;
        this.unit = unit;
    }
}


export let componentTypes: string[] = [
    'test',
    'sut',
];

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

// Main class
export class AllMetricsFields {
    fieldsList: MetricsFieldModel[];

    constructor() {
        this.fieldsList = [];
        for (let componentType of componentTypes) {
            this.fieldsList = this.fieldsList.concat(this.createFieldsListByComponent(componentType));
        }
    }

    createFieldsListByComponent(componentType: string) {
        let list: MetricsFieldModel[] = [];
        for (let metricFieldGroup of metricFieldGroupList) { // Foreach type for this componentType
            list = list.concat(this.createFieldsListBySublist(metricFieldGroup, componentType));
        }
        return list;
    }

    createFieldsListBySublist(metricFieldGroup: MetricFieldGroupModel, componentType: string) {
        let list: MetricsFieldModel[] = [];

        for (let subtype of metricFieldGroup.subtypes) { // Foreach subtype of this type and this componentType
            list.push(new MetricsFieldModel(metricFieldGroup.type, subtype.subtype, subtype.unit, componentType));
        }
        return list;
    }

    getPositionsList(type: string, componentType: string) {
        let namePrefix: string = componentType + '_' + type;
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

}