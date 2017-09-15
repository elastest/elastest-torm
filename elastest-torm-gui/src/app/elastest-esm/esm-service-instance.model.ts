export class EsmServiceInstanceModel {
    id: string;
    serviceName: string;
    uiUrl: string;
    ip: string;
    port: number;

    constructor(id: string, serviceName: string, uiUrl: string, ip: string, port: number){
        this.id = id;
        this.serviceName = serviceName;
        this.uiUrl = uiUrl;
        this.ip = ip;
        this.port = port;
    }    
}
