import { SutModel } from './sut-model';
import { defaultStreamMap } from '../../shared/defaultESData-model';

export class EimMonitoringConfigModel {
  id: number;
  exec: string;
  component: string;
  dockerized: boolean;
  beats: EimBeatsMap;

  constructor(exec: string = '', component: string = '', dockerized: boolean = false) {
    this.id = 0;
    this.exec = exec; // Sets on save in Backend (SutService)
    this.component = component;
    this.dockerized = dockerized;
    this.beats = new EimBeatsMap();
  }
}

export class EimBeatsMap {
  packetbeat: EimBeatConfigModel;
  filebeat: EimBeatConfigModel;
  metricbeat: EimBeatConfigModel;

  constructor() {
    this.packetbeat = new EimBeatConfigModel('packetbeat', 'et_packetbeat');
    this.filebeat = new EimBeatConfigModel('filebeat', defaultStreamMap.log, ['/var/lib/docker/containers/']);
    this.metricbeat = new EimBeatConfigModel('metricbeat', 'et_metricbeat', ['/var/run/docker.sock']);
  }
}

export class EimBeatConfigModel {
  id: number;
  name: string;
  stream: string;
  paths: string[];
  dockerized: string[];

  constructor(name: string = '', stream: string = '', dockerizedList: string[] = undefined) {
    this.id = 0;
    this.name = name;
    this.stream = stream;
    this.paths = [];
    this.dockerized = dockerizedList;
  }
}
