import { DockerServiceStatus } from '../shared/docker-service-status.model';

export class EusTestModel extends DockerServiceStatus {
  id: string;
  browser: string;
  version: string;
  creationTime: string;
  url: string;
  hubContainerName: string;
}
