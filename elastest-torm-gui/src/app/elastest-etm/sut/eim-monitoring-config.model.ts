import { SutModel } from './sut-model';

export class EimMonitoringConfigModel {
  id: number;
  exec: string;
  component: string;
  sut: SutModel;
  beats: Map<string, EimBeatConfigModel>;

  constructor(exec: string = '', component: string = '', stream: string = '') {
    this.id = 0;
    this.exec = exec; // Sets on save in Backend (SutService)
    this.component = component;
    this.sut = undefined;

    this.beats = new Map();
    this.beats.set('packetbeat', new EimBeatConfigModel('packetbeat', stream));
    this.beats.set('filebeat', new EimBeatConfigModel('filebeat', stream));
    this.beats.set('topbeat', new EimBeatConfigModel('topbeat', stream));
  }
}

export class EimBeatConfigModel {
  id: number;
  name: string;
  stream: string;
  paths: string[];
  eimMonitoringConfig: EimMonitoringConfigModel;

  constructor(name: string = '', stream: string = '') {
    this.id = 0;
    this.name = name;
    this.stream = stream;
    this.paths = [];
    this.eimMonitoringConfig = undefined;
  }
}
