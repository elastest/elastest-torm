export class EimConfigModel {
  id: number;
  user: string;
  privateKey: string;
  ip: string;
  agentId: number;
  logstashIp: string;
  logstashTcpHost: string;
  logstashTcpPort: string;
  logstashBeatsHost: string;
  logstashBeatsPort: string;

  logstashBindedTcpHost: string;
  logstashBindedTcpPort: string;
  logstashBindedBeatsHost: string;
  logstashBindedBeatsPort: string;

  logstashHttpPort: string;
  logstashHttpApiUrl: string;

  constructor(eimConfigJson: any = undefined) {
    if (!eimConfigJson) {
      this.id = 0;
      this.privateKey = '';
      this.ip = '';
      this.agentId = undefined;
      this.logstashIp = '';

      this.logstashTcpHost = '';
      this.logstashTcpPort = '';
      this.logstashBeatsHost = '';
      this.logstashBeatsPort = '';

      this.logstashBindedTcpHost = '';
      this.logstashBindedTcpPort = '';
      this.logstashBindedBeatsHost = '';
      this.logstashBindedBeatsPort = '';

      this.logstashHttpPort = '';
      this.logstashHttpApiUrl = '';
    } else {
      this.id = eimConfigJson.id;
      this.user = eimConfigJson.user;
      this.privateKey = eimConfigJson.privateKey;
      this.ip = eimConfigJson.ip;
      this.agentId = eimConfigJson.agentId;
      this.logstashIp = eimConfigJson.logstashIp;

      this.logstashTcpHost = eimConfigJson.logstashTcpHost;
      this.logstashTcpPort = eimConfigJson.logstashTcpPort;
      this.logstashBeatsHost = eimConfigJson.logstashBeatsHost;
      this.logstashBeatsPort = eimConfigJson.logstashBeatsPort;

      this.logstashBindedTcpHost = eimConfigJson.logstashBindedTcpHost;
      this.logstashBindedTcpPort = eimConfigJson.logstashBindedTcpPort;
      this.logstashBindedBeatsHost = eimConfigJson.logstashBindedBeatsHost;
      this.logstashBindedBeatsPort = eimConfigJson.logstashBindedBeatsPort;

      this.logstashHttpPort = eimConfigJson.logstashHttpPort;
      this.logstashHttpApiUrl = eimConfigJson.logstashHttpApiUrl;
    }
  }
}
