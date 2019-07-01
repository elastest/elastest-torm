import { DockerServiceStatus } from '../shared/docker-service-status.model';
import { EusSessionInfoModel } from './elastest-eus-sessioninfo.model';

export class EusTestModel extends DockerServiceStatus {
  id: string;
  browser: string;
  version: string;
  creationTime: string;
  url: string;
  hubContainerName: string;
  live: boolean;
  elastestExecutionData: any;

  constructor(json?: any) {
    super();

    if (json) {
      this.id = json.sessionId;
      this.hubContainerName = json.hubContainerName;

      if (json.value) {
        if (json.value.sessionId) {
          this.id = json.value.sessionId;
        }

        this.browser = json.value.browserName;
        this.version = json.value.version;
        this.creationTime = json.value.creationTime;
        this.url = json.value.url;
        this.status = json.value.status;
        this.statusMsg = json.value.statusMsg;
      }
    }
  }

  initFromEusSessionInfoModel(model: EusSessionInfoModel): void {
    if (model) {
      this.id = model.sessionId;
      this.hubContainerName = model.hubContainerName;

      if (model.sessionId) {
        this.id = model.sessionId;
      }

      this.browser = model.browser;
      this.version = model.version;
      this.creationTime = model.creationTime;
      this.url = model.hubUrl;
    }
  }
}
