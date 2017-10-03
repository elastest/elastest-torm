export class EimConfigModel {
    id: number;
    instrumentalized: boolean;
    user: string;
    privateKey: string;
    ip: string;
    agentId: number;

    constructor(eimConfigJson: any = undefined) {
        if (!eimConfigJson) {
            this.id = 0;
            this.instrumentalized = false;
            this.privateKey = '';
            this.ip = '';
            this.agentId = undefined;
        } else {
            this.id = eimConfigJson.id;
            this.instrumentalized = eimConfigJson.instrumentalized;
            this.user = eimConfigJson.user;
            this.privateKey = eimConfigJson.privateKey;
            this.ip = eimConfigJson.ip;
            this.agentId = eimConfigJson.agentId;
        }
    }
}