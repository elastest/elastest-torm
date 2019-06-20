export abstract class ExternalMonitoringDB {
  id: number;
  ip: string;
  port: string;
  path: string;
  protocol: 'http' | 'https' | ''; // On add new, add too in getProtocolsList
  user: string;
  pass: string;

  constructor(externalMonitoringDBJson: any = undefined) {
    if (!externalMonitoringDBJson) {
      this.id = 0;
      this.protocol = 'http';
      this.ip = '';
      this.port = '';
      this.path = '';
      this.user = '';
      this.pass = '';
    } else {
      this.id = externalMonitoringDBJson.id;
      this.protocol = externalMonitoringDBJson.protocol;
      this.ip = externalMonitoringDBJson.ip;
      this.port = externalMonitoringDBJson.port;
      this.path = externalMonitoringDBJson.path;
      this.user = externalMonitoringDBJson.user;
      this.pass = externalMonitoringDBJson.pass;
    }
  }

  initByGiven(externalDB: ExternalMonitoringDB): void {
    this.id = externalDB.id;
    this.protocol = externalDB.protocol;
    this.ip = externalDB.ip;
    this.port = externalDB.port;
    this.path = externalDB.path;
    this.user = externalDB.user;
    this.pass = externalDB.pass;
  }

  public getProtocolsList(): string[] {
    return ['http', 'https'];
  }
}
