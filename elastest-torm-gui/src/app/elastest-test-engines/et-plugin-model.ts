import { DockerServiceStatus } from '../shared/docker-service-status.model';

export class EtPluginModel extends DockerServiceStatus {
  name: string;
  url: string;
  imagesList: string[];
  user: string;
  pass: string;
  displayName: string;
  fileName: string;
  showCredentialsInEtmViewOnlyMode: boolean;

  constructor() {
    super();
    this.name = '';
    this.url = '';
    this.imagesList = [];
    this.user = '';
    this.pass = '';
    this.displayName = '';
    this.fileName = '';
    this.showCredentialsInEtmViewOnlyMode = true;
  }
}
