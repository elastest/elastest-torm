import { DockerServiceStatus } from '../shared/docker-service-status.model';

export class EtPluginModel extends DockerServiceStatus {
  name: string;
  url: string;
  imagesList: string[];

  constructor() {
    super();
    this.name = '';
    this.url = '';
    this.imagesList = [];
  }
}
