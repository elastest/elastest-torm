export class EsmServiceInstanceModel {
    id: string = "";
    serviceName: string = "";
    uiUrl: string = "" ;
    apiUrl: string = "";
    ip: string = "";
    port: number = 0;
    subServices: EsmServiceInstanceModel[] = []; 
    endpointsOtherData: any;
    
    constructor(serviceInstance: any){
        if (serviceInstance){
            this.id = serviceInstance.instanceId;
            this.serviceName = serviceInstance.endpointName;
            this.uiUrl = serviceInstance.urls.gui != undefined ? serviceInstance.urls.gui : "";
            this.ip = serviceInstance.urls.api != undefined ? serviceInstance.urls.api : "";
            this.port = serviceInstance.serviceIp, serviceInstance.servicePort;
            this.endpointsOtherData = serviceInstance.endpointsData;

            for (let subService of serviceInstance.subServices){
                this.subServices.push(new EsmServiceInstanceModel(subService));
            }
        }        
    }
}
