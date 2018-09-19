import { DockerServiceStatus } from '../shared/docker-service-status.model';

export class EtPluginModel extends DockerServiceStatus {
  name: string;
  url: string;
  imagesList: string[];
  user: string;
  pass: string;

  constructor() {
    super();
    this.name = '';
    this.url = '';
    this.imagesList = [];
    this.user = '';
    this.pass = '';
  }
}
