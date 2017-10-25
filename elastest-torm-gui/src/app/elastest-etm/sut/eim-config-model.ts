export class EimConfigModel {
    id: number;
    user: string;
    privateKey: string;
    ip: string;
    agentId: number;
    logstashIp: string;
    logstashBeatsPort: string;
    logstashHttpPort: string;

    constructor(eimConfigJson: any = undefined) {
        if (!eimConfigJson) {
            this.id = 0;
            this.privateKey = '';
            this.ip = '';
            this.agentId = undefined;
            this.logstashIp = '';
            this.logstashBeatsPort = '';
            this.logstashHttpPort = '';
        } else {
            this.id = eimConfigJson.id;
            this.user = eimConfigJson.user;
            this.privateKey = eimConfigJson.privateKey;
            this.ip = eimConfigJson.ip;
            this.agentId = eimConfigJson.agentId;
            this.logstashIp = eimConfigJson.logstashIp;
            this.logstashBeatsPort = eimConfigJson.logstashBeatsPort;
            this.logstashHttpPort = eimConfigJson.logstashHttpPort;
        }
    }
}