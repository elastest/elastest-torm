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
  urls: Map<String, SSIUrl>;
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

      this.urls = new Map();

      if (serviceInstance.urls) {
        this.urlsKeys = Object.keys(serviceInstance.urls);
        for (let key of this.urlsKeys) {
          let ssiUrl: SSIUrl = new SSIUrl(serviceInstance.urls[key]);
          this.urls.set(key, ssiUrl);
        }
      }

      this.uiUrl = this.getUrlIfExistsByKey('gui') !== undefined ? this.getUrlIfExistsByKey('gui') : '';
      this.apiUrl = this.getUrlIfExistsByKey('api') !== undefined ? this.getUrlIfExistsByKey('api') : '';

      for (let subService of serviceInstance.subServices) {
        this.subServices.push(new EsmServiceInstanceModel(subService));
      }

      this.status = serviceInstance.status;
      this.statusMsg = serviceInstance.statusMsg;
    }
  }

  getUrlIfExistsByKey(urlKey: string): string {
    let url: string = undefined;
    if (this.urls && this.urls.has(urlKey)) {
      let urlObj: SSIUrl = this.urls.get(urlKey);
      // If external, return external, else return internal
      if (urlObj) {
        url = urlObj.external && urlObj.external !== '' ? urlObj.external : urlObj.internal;
      }
    }
    return url;
  }
}

export class SSIUrl {
  internal: string;
  external: string;

  constructor(ssiUrl?: any) {
    if (ssiUrl) {
      this.internal = ssiUrl.internal;
      this.external = ssiUrl.external;
    }
  }
}
