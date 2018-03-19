import { SutModel } from './sut-model';

export class EimMonitoringConfigModel {
  id: number;
  exec: string;
  component: string;
  beats: EimBeatsMap;

  constructor(exec: string = '', component: string = '', stream: string = '') {
    this.id = 0;
    this.exec = exec; // Sets on save in Backend (SutService)
    this.component = component;
    this.beats = new EimBeatsMap(stream);
  }
}

export class EimBeatsMap {
  packetbeat: EimBeatConfigModel;
  filebeat: EimBeatConfigModel;
  topbeat: EimBeatConfigModel;

  constructor(stream: string = '') {
    this.packetbeat = new EimBeatConfigModel('packetbeat', stream);
    this.filebeat = new EimBeatConfigModel('filebeat', stream);
    this.topbeat = new EimBeatConfigModel('topbeat', stream);
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
