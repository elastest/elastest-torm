export class EsmServiceInstanceModel {
    id: string = '';
    serviceName: string = '';
    uiUrl: string = '';
    apiUrl: string = '';
    ip: string = '';
    port: number = 0;
    urlsKeys: any[] = [];
    endpointsDataKeys: any[] = [];
    endpointsNodeDataKeys: any[] = [];
    urls: any;
    subServices: EsmServiceInstanceModel[] = [];
    endpointsOtherData: any;

    constructor(serviceInstance: any) {
        if (serviceInstance) {
            this.id = serviceInstance.instanceId;
            this.serviceName = serviceInstance.endpointName;
            this.uiUrl = serviceInstance.urls.gui !== undefined ? serviceInstance.urls.gui : '';
            this.apiUrl = serviceInstance.urls.api !== undefined ? serviceInstance.urls.api : '';
            this.ip = serviceInstance.serviceIp !== undefined ? serviceInstance.serviceIp : '';
            this.port = serviceInstance.servicePort;
            this.endpointsOtherData = serviceInstance.endpointsData;
            this.endpointsDataKeys = Object.keys(serviceInstance.endpointsData);
            for(let endpointDataKey of this.endpointsDataKeys){
                this.endpointsNodeDataKeys.push(Object.keys(serviceInstance.endpointsData[endpointDataKey]));
            }
            //this.endpointsDataNodeKeys = Object.keys(serviceInstance.endpointsData);
            this.urlsKeys = Object.keys(serviceInstance.urls);
            console.log("UrlKeys: " + this.urlsKeys);
            this.urls = serviceInstance.urls;

            for (let subService of serviceInstance.subServices) {
                this.subServices.push(new EsmServiceInstanceModel(subService));
            }
        }
    }
}
