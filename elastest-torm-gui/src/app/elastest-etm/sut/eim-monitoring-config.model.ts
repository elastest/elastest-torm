import { SutModel } from './sut-model';
import { defaultStreamMap } from '../../shared/defaultESData-model';

export class EimMonitoringConfigModel {
  id: number;
  exec: string;
  component: string;
  beats: EimBeatsMap;

  constructor(exec: string = '', component: string = '') {
    this.id = 0;
    this.exec = exec; // Sets on save in Backend (SutService)
    this.component = component;
    this.beats = new EimBeatsMap();
  }
}

export class EimBeatsMap {
  packetbeat: EimBeatConfigModel;
  filebeat: EimBeatConfigModel;
  metricbeat: EimBeatConfigModel;

  constructor() {
    this.packetbeat = new EimBeatConfigModel('packetbeat', 'et_packetbeat');
    this.filebeat = new EimBeatConfigModel('filebeat', defaultStreamMap.log);
    this.metricbeat = new EimBeatConfigModel('metricbeat', 'et_metricbeat');
  }
}

export class EimBeatConfigModel {
  id: number;
  name: string;
  stream: string;
  paths: string[];

  constructor(name: string = '', stream: string = '') {
    this.id = 0;
    this.name = name;
    this.stream = stream;
    this.paths = [];
  }
}
