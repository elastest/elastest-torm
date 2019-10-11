import { DefaultESFieldModel, components } from '../../../defaultESData-model';
import { MetricsFieldModel } from './metrics-field-model';

// Aux Classes
export class MetricFieldGroupModel {
  etType: string;
  subtypes: SubtypesObjectModel[];

  constructor(etType: string, subtypes: SubtypesObjectModel[]) {
    this.etType = etType;
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
export let cpuSubtypes: SubtypesObjectModel[] = [new SubtypesObjectModel('totalUsage', 'percent')];
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
export let netSubtypes: SubtypesObjectModel[] = [
  // rx -> received; tx -> transmited
  new SubtypesObjectModel('rxBytes_ps', 'amount/sec'),
  new SubtypesObjectModel('rxErrors_ps', 'amount/sec'),
  new SubtypesObjectModel('rxPackets_ps', 'amount/sec'),
  new SubtypesObjectModel('txBytes_ps', 'amount/sec'),
  new SubtypesObjectModel('txErrors_ps', 'amount/sec'),
  new SubtypesObjectModel('txPackets_ps', 'amount/sec'),
  new SubtypesObjectModel('txDropped_ps', 'amount/sec'),
  new SubtypesObjectModel('rxDropped_ps', 'amount/sec'),
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
  new SubtypesObjectModel('system_pct', 'percent'),
  new SubtypesObjectModel('user_pct', 'percent'),
  new SubtypesObjectModel('total_pct', 'percent'),
  new SubtypesObjectModel('steal_pct', 'percent'),
  new SubtypesObjectModel('softirq_pct', 'percent'),
  new SubtypesObjectModel('nice_pct', 'percent'),
  new SubtypesObjectModel('irq_pct', 'percent'),
  new SubtypesObjectModel('iowait_pct', 'percent'),
  new SubtypesObjectModel('idle_pct', 'percent'),
  new SubtypesObjectModel('cores', 'amount'),
];

export let metricbeatCpuForDockerSubtypes: SubtypesObjectModel[] = [
  new SubtypesObjectModel('system', 'percent'),
  new SubtypesObjectModel('total', 'percent'),
  new SubtypesObjectModel('user', 'percent'),
  new SubtypesObjectModel('kernel', 'percent'),
];

export let metricbeatMemorySubtypes: SubtypesObjectModel[] = [
  new SubtypesObjectModel('used_bytes', 'bytes'),
  new SubtypesObjectModel('used_pct', 'percent'),
  new SubtypesObjectModel('total', 'bytes'),
  new SubtypesObjectModel('free', 'bytes'),
];

export let metricbeatMemoryForDockerSubtypes: SubtypesObjectModel[] = [
  new SubtypesObjectModel('usage_max', 'bytes'),
  new SubtypesObjectModel('usage_pct', 'percent'),
  new SubtypesObjectModel('total', 'bytes'),
];

export function getMetricbeatNetworkSubtypes(forDocker: boolean = false): SubtypesObjectModel[] {
  let subtypeList: string[] = ['in', 'out'];
  if (forDocker) {
    subtypeList.push('inbound', 'outbound');
  }
  let networkSubtypes: SubtypesObjectModel[] = [];

  for (let subtype of subtypeList) {
    networkSubtypes.push(new SubtypesObjectModel(subtype + '_bytes', 'bytes'));
    networkSubtypes.push(new SubtypesObjectModel(subtype + '_dropped', 'amount'));
    networkSubtypes.push(new SubtypesObjectModel(subtype + '_packets', 'amount'));
    networkSubtypes.push(new SubtypesObjectModel(subtype + '_errors', 'amount'));
  }
  return networkSubtypes;
}

export function getMetricbeatDiskIOSubtypes(forDocker: boolean = false): SubtypesObjectModel[] {
  let subtypeList: string[] = ['write', 'read'];
  let diskioSubtypes: SubtypesObjectModel[] = [];
  if (forDocker) {
    subtypeList.push('summary');

    for (let subtype of subtypeList) {
      diskioSubtypes.push(new SubtypesObjectModel(subtype + '_bytes', 'bytes'));
      diskioSubtypes.push(new SubtypesObjectModel(subtype + '_ops', 'amount'));
      diskioSubtypes.push(new SubtypesObjectModel(subtype + '_rate', 'amount/sec'));
    }
  } else {
    for (let subtype of subtypeList) {
      diskioSubtypes.push(new SubtypesObjectModel(subtype + '_bytes', 'bytes'));
      diskioSubtypes.push(new SubtypesObjectModel(subtype + '_count', 'amount'));
      diskioSubtypes.push(new SubtypesObjectModel(subtype + '_time', 'millis'));
    }
  }
  return diskioSubtypes;
}

export enum MetricbeatType {
  system,
  docker,
}

export function getMetricBeatFieldGroupList(): MetricFieldGroupModel[] {
  let list: MetricFieldGroupModel[] = [];
  for (let etType in MetricbeatType) {
    if (isNaN(parseInt(etType))) {
      // enums returns position and value
      if (etType === 'docker') {
        list.push(new MetricFieldGroupModel(etType + '_cpu', metricbeatCpuForDockerSubtypes));
        list.push(new MetricFieldGroupModel(etType + '_memory', metricbeatMemoryForDockerSubtypes));
        list.push(new MetricFieldGroupModel(etType + '_network', getMetricbeatNetworkSubtypes(true)));
        list.push(new MetricFieldGroupModel(etType + '_diskio', getMetricbeatDiskIOSubtypes(true)));
      } else {
        list.push(new MetricFieldGroupModel(etType + '_cpu', metricbeatCpuSubtypes));
        list.push(new MetricFieldGroupModel(etType + '_memory', metricbeatMemorySubtypes));
        list.push(new MetricFieldGroupModel(etType + '_network', getMetricbeatNetworkSubtypes(false)));
        list.push(new MetricFieldGroupModel(etType + '_diskio', getMetricbeatDiskIOSubtypes(false)));
      }
    }
  }
  return list;
}

export function getMetricbeatFieldGroupIfItsMetricbeatType(etType: string): MetricFieldGroupModel[] {
  let metricBeatFieldGroupList: MetricFieldGroupModel[] = getMetricBeatFieldGroupList();
  return isMetricFieldGroup(etType, metricBeatFieldGroupList) ? metricBeatFieldGroupList : undefined;
}

export function isMetricFieldGroup(etType: string, givenMetricFieldGroupList: MetricFieldGroupModel[]): boolean {
  for (let metricFieldGroup of givenMetricFieldGroupList) {
    if (etType === metricFieldGroup.etType) {
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

  constructor(withComponent: boolean = true, ignoreComponent: string = '', activate: boolean = false) {
    this.fieldsList = [];
    if (withComponent) {
      for (let component of components) {
        if (component !== ignoreComponent) {
          this.fieldsList = this.fieldsList.concat(this.createFieldsListByComponent(component, activate));
        }
      }
    } else {
      this.fieldsList = this.getFieldListWithoutComponent();
    }
  }

  addMetricsFieldToList(
    metricsFieldModel: MetricsFieldModel,
    component: string,
    stream: string,
    streamType?: string,
    activated: boolean = false,
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
      this.addFieldToFieldList(this.fieldsList, metricsFieldModel.etType, subtypeObj, component, stream, streamType, activated);
      return this.fieldsList.length - 1;
    }
  }

  createFieldsListByComponent(component: string, activate: boolean = false): MetricsFieldModel[] {
    let list: MetricsFieldModel[] = [];
    for (let metricFieldGroup of metricFieldGroupList) {
      // Foreach etType for this component
      list = list.concat(this.createFieldsListBySublist(metricFieldGroup, component, undefined, undefined, activate));
    }
    return list;
  }

  createFieldsListBySublist(
    metricFieldGroup: MetricFieldGroupModel,
    component: string,
    stream?: string,
    streamType?: string,
    activated: boolean = false,
  ): MetricsFieldModel[] {
    let list: MetricsFieldModel[] = [];

    for (let subtype of metricFieldGroup.subtypes) {
      // Foreach subtype of this etType and this component
      list = [...this.addFieldToFieldList(list, metricFieldGroup.etType, subtype, component, stream, streamType, activated)];
    }
    return list;
  }

  addFieldToFieldList(
    list: MetricsFieldModel[],
    etType: string,
    subtype: SubtypesObjectModel,
    component: string,
    stream?: string,
    streamType?: string,
    activated: boolean = false,
  ): MetricsFieldModel[] {
    let newField: MetricsFieldModel = new MetricsFieldModel(
      etType,
      subtype.subtype,
      subtype.unit,
      component,
      stream,
      streamType,
      activated,
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

  getPositionsList(etType: string, component: string, stream?: string): number[] {
    let namePrefix: string = component + '_' + etType;
    if (stream) {
      namePrefix = component + '_' + stream + '_' + etType;
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
    if (position === undefined) {
      // If no position, return new position
      position = this.fieldsList.length;
    }
    return position;
  }

  getDefaultUnitBySubtype(subtypeName: string): Units | string {
    for (let etType of metricFieldGroupList) {
      for (let subtype of etType.subtypes) {
        if (subtypeName === subtype.subtype) {
          return subtype.unit;
        }
      }
    }
    return '';
  }

  getDefaultUnitByTypeAndSubtype(typeName: string, subtypeName: string): Units | string {
    let currentMetricFieldGroupList: MetricFieldGroupModel[] = getMetricbeatFieldGroupIfItsMetricbeatType(typeName);
    if (currentMetricFieldGroupList === undefined) {
      // If is not Metricbeat etType, it's dockbeat
      currentMetricFieldGroupList = metricFieldGroupList;
    }
    for (let etType of currentMetricFieldGroupList) {
      for (let subtype of etType.subtypes) {
        if (subtypeName === subtype.subtype) {
          return subtype.unit;
        }
      }
    }
    return '';
  }
}
