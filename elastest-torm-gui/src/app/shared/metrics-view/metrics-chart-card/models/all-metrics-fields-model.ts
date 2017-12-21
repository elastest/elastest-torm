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

    getSubtypesList(): string[] {
        let subtypesList: string[] = [];
        for (let subtype of this.subtypes) {
            subtypesList.push(subtype.subtype);
        }
        return subtypesList;
    }
}

export type Units = 'percent' | 'bytes' | 'amount/sec';

export class SubtypesObjectModel {
    subtype: string;
    unit: Units | string;

    constructor(subtype: string, unit: Units | string) {
        this.subtype = subtype;
        this.unit = unit;
    }
}

/********************/
/***** Dockbeat *****/
/********************/

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

/**********************/
/***** Metricbeat *****/
/**********************/
export let metricbeatCpuSubtypes: SubtypesObjectModel[] = [
    new SubtypesObjectModel('system', 'percent'),
    new SubtypesObjectModel('user', 'percent'),
    new SubtypesObjectModel('total', 'percent'),
    new SubtypesObjectModel('steal', 'percent'),
    new SubtypesObjectModel('softirq', 'percent'),
    new SubtypesObjectModel('nice', 'percent'),
    new SubtypesObjectModel('irq', 'percent'),
    new SubtypesObjectModel('iowait', 'percent'),
    new SubtypesObjectModel('idle', 'percent'),

];

export let metricbeatMemorySubtypes: SubtypesObjectModel[] = [
    new SubtypesObjectModel('used_bytes', 'bytes'),
    new SubtypesObjectModel('used_pct', 'percent'),
];


export function getMetricbeatNetworkSubtypes(): SubtypesObjectModel[] {
    let subtypeList: string[] = ['in', 'out'];
    let netSubtypes: SubtypesObjectModel[] = [];
    for (let subtype of subtypeList) {
        netSubtypes.push(new SubtypesObjectModel(subtype + '_bytes', 'bytes'));
        netSubtypes.push(new SubtypesObjectModel(subtype + '_dropped', 'amount'));
        netSubtypes.push(new SubtypesObjectModel(subtype + '_packets', 'amount'));
        netSubtypes.push(new SubtypesObjectModel(subtype + '_errors', 'amount'));
    }
    return netSubtypes;
}


export enum MetricbeatType {
    system,
}

export function getMetricBeatFieldGroupList(): MetricFieldGroupModel[] {
    let list: MetricFieldGroupModel[] = [];
    for (let type in MetricbeatType) {
        if (isNaN(parseInt(type))) { // enums returns position and value
            list.push(new MetricFieldGroupModel(type + '_cpu', metricbeatCpuSubtypes));
            list.push(new MetricFieldGroupModel(type + '_memory', metricbeatMemorySubtypes));
            // list.push(new MetricFieldGroupModel(type + '_network', getMetricbeatNetworkSubtypes())); //Disabled (traces with same millisecond)
        }
    }
    return list;
}

export function getMetricbeatFieldGroupIfItsMetricbeatType(type: string): MetricFieldGroupModel[] {
    let metricBeatFieldGroupList: MetricFieldGroupModel[] = getMetricBeatFieldGroupList();
    return isMetricFieldGroup(type, metricBeatFieldGroupList) ? metricBeatFieldGroupList : undefined;
}


export function isMetricFieldGroup(type: string, givenMetricFieldGroupList: MetricFieldGroupModel[]): boolean {
    for (let metricFieldGroup of givenMetricFieldGroupList) {
        if (type === metricFieldGroup.type) {
            return true;
        }
    }
    return false;
}

/************************/
/***** Main classes *****/
/************************/
export class AllMetricsFields {
    fieldsList: MetricsFieldModel[]; // Do not make push never!! use addMetricsFieldToList

    constructor(withComponent: boolean = true, ignoreComponent: string = '') {
        this.fieldsList = [];
        if (withComponent) {
            for (let component of components) {
                if (component !== ignoreComponent) {
                    this.fieldsList = this.fieldsList.concat(this.createFieldsListByComponent(component));
                }
            }
        } else {
            this.fieldsList = this.getFieldListWithoutComponent();
        }
    }

    addMetricsFieldToList(
        metricsFieldModel: MetricsFieldModel, component: string, stream: string, streamType?: string, activated: boolean = false
    ): number {
        let alreadySaved: boolean = false;
        let position: number = 0;
        for (let metricsField of this.fieldsList) {
            if (metricsField.name === metricsFieldModel.name) {
                alreadySaved = true;
                metricsField.activated = activated;
                return position;
            }
            position++;
        }
        if (!alreadySaved) {
            let subtypeObj: SubtypesObjectModel = new SubtypesObjectModel(metricsFieldModel.subtype, metricsFieldModel.unit);
            this.addFieldToFieldList(this.fieldsList, metricsFieldModel.type, subtypeObj, component, stream, streamType, activated);
            return this.fieldsList.length - 1;
        }
    }

    createFieldsListByComponent(component: string): MetricsFieldModel[] {
        let list: MetricsFieldModel[] = [];
        for (let metricFieldGroup of metricFieldGroupList) { // Foreach type for this component
            list = list.concat(this.createFieldsListBySublist(metricFieldGroup, component));
        }
        return list;
    }

    createFieldsListBySublist(
        metricFieldGroup: MetricFieldGroupModel, component: string, stream?: string, streamType?: string, activated: boolean = false
    ): MetricsFieldModel[] {
        let list: MetricsFieldModel[] = [];

        for (let subtype of metricFieldGroup.subtypes) { // Foreach subtype of this type and this component
            list = [...this.addFieldToFieldList(list, metricFieldGroup.type, subtype, component, stream, streamType, activated)];
        }
        return list;
    }

    addFieldToFieldList(
        list: MetricsFieldModel[], type: string, subtype: SubtypesObjectModel,
        component: string, stream?: string, streamType?: string, activated: boolean = false
    ): MetricsFieldModel[] {
        let newField: MetricsFieldModel = new MetricsFieldModel(
            type, subtype.subtype, subtype.unit, component, stream, streamType, activated
        );
        list.push(newField);
        return list;
    }

    disableMetricField(name: string, component: string, stream: string): void {
        // this.addFieldToFieldList(this.fieldsList, name, component, stream, false);
    }

    disableMetricFieldByTitleName(name: string): void {
        name = name.replace(/^\s/, ''); // repair if starts with white space
        name = name.replace(/\s/g, '_');
        for (let metricsField of this.fieldsList) {
            if (metricsField.name === name) {
                metricsField.activated = false;
                break;
            }
        }
    }

    getFieldListWithoutComponent(): MetricsFieldModel[] {
        return this.createFieldsListByComponent('');
    }

    getPositionsList(type: string, component: string, stream?: string): number[] {
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

    getPositionByName(name: string): number {
        let position: number;
        let counter: number = 0;
        for (let metric of this.fieldsList) {
            if (metric.name === name) {
                position = counter;
                break;
            }
            counter++;
        }
        if (position === undefined) { // If no position, return new position
            position = this.fieldsList.length;
        }
        return position;
    }

    getDefaultUnitBySubtype(subtypeName: string): Units | string {
        for (let type of metricFieldGroupList) {
            for (let subtype of type.subtypes) {
                if (subtypeName === subtype.subtype) {
                    return subtype.unit;
                }
            }
        }
        return '';
    }



    getDefaultUnitByTypeAndSubtype(typeName: string, subtypeName: string): Units | string {
        let currentMetricFieldGroupList: MetricFieldGroupModel[] = getMetricbeatFieldGroupIfItsMetricbeatType(typeName);
        if (currentMetricFieldGroupList === undefined) { // If is not Metricbeat type, it's dockbeat
            currentMetricFieldGroupList = metricFieldGroupList;
        }
        for (let type of currentMetricFieldGroupList) {
            for (let subtype of type.subtypes) {
                if (subtypeName === subtype.subtype) {
                    return subtype.unit;
                }
            }
        }
        return '';
    }
}