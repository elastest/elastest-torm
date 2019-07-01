export class EusSessionInfoModel {
  sessionId: string;
  hubUrl: string;
  hubContainerName: string;
  creationTime: string;
  browser: string;
  version: string;
  liveSession: boolean;
  timeout: number;
  testName: string;
  manualRecording: boolean;
  folderPath: string;
  elastestExecutionData: any;
  capabilities: any;

  constructor(json: any) {
    if (json) {
      this.sessionId = json.sessionId;
      this.hubUrl = json.hubUrl;
      this.hubContainerName = json.hubContainerName;
      this.creationTime = json.creationTime;
      this.browser = json.browser;
      this.version = json.version;
      this.liveSession = json.liveSession;
      this.timeout = json.timeout;
      this.testName = json.testName;
      this.manualRecording = json.manualRecording;
      this.folderPath = json.folderPath;
      this.elastestExecutionData = json.elastestExecutionData;
      this.capabilities = json.capabilities;
    } else {
    }
  }
}
