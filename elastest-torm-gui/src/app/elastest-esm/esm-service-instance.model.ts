import { DockerServiceStatus } from '../shared/docker-service-status.model';
export class EsmServiceInstanceModel extends DockerServiceStatus {
  id: string = '';
  endpointName: string = '';
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
  fullyInitialized: boolean = false;
  tssEnvVariables: any;
  tssEnvVariablesKeys: any[] = [];

  constructor(serviceInstance: any) {
    super();
    if (serviceInstance) {
      this.id = serviceInstance.instanceId;
      this.serviceName = serviceInstance.serviceName;
      this.endpointName = serviceInstance.endpointName;
      this.uiUrl = serviceInstance.urls.gui !== undefined ? serviceInstance.urls.gui : '';
      this.apiUrl = serviceInstance.urls.api !== undefined ? serviceInstance.urls.api : '';
      this.ip = serviceInstance.serviceIp !== undefined ? serviceInstance.serviceIp : '';
      this.port = serviceInstance.servicePort;
      this.fullyInitialized = serviceInstance.fullyInitialized;
      this.tssEnvVariables = serviceInstance.parameters;
      this.tssEnvVariablesKeys = Object.keys(serviceInstance.parameters);
      this.endpointsOtherData = serviceInstance.endpointsData;
      this.endpointsDataKeys = Object.keys(serviceInstance.endpointsData);
      for (let endpointDataKey of this.endpointsDataKeys) {
        this.endpointsNodeDataKeys.push(Object.keys(serviceInstance.endpointsData[endpointDataKey]));
      }
      this.urlsKeys = Object.keys(serviceInstance.urls);
      this.urls = serviceInstance.urls;
      for (let subService of serviceInstance.subServices) {
        this.subServices.push(new EsmServiceInstanceModel(subService));
      }

      this.status = serviceInstance.status;
      this.statusMsg = serviceInstance.statusMsg;
    }
  }
}
