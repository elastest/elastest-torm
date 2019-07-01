import { EusSessionInfoModel } from './elastest-eus-sessioninfo.model';

export class EusBowserSyncModel {
  // ServiceName is a enum in EUS backend
  serviceName: string;
  // Usually container name
  identifier: string;
  sessions: EusSessionInfoModel[];
  guiUrl: string;
  appUrl: string;
  constructor(json: any) {
    if (json) {
      this.identifier = json.identifier;
      this.serviceName = json.serviceName;
      this.guiUrl = json.guiUrl;
      this.appUrl = json.appUrl;
      this.sessions = [];
      if (json.sessions) {
        for (let session of json.sessions) {
          this.sessions.push(new EusSessionInfoModel(session));
        }
      }
    } else {
      this.sessions = [];
    }
  }
}
