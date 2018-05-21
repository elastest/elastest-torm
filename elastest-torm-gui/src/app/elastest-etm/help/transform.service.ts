import { CoreServiceModel } from '../models/core-service.model';
import { VersionInfo } from '../models/version-info.model';
import { ContainerPortModel } from '../models/container-port.model';
import { Injectable } from '@angular/core';

@Injectable()
export class TransformService {
  constructor() {}
  /* *** CoreServices *** */
  jsonToCoreServicesList(coreServices: any[]): CoreServiceModel[] {
    let coreServicesList: CoreServiceModel[] = [];
    for (let coreService of coreServices) {
      coreServicesList.push(this.jsonToCoreServiceModel(coreService));
    }
    return coreServicesList;
  }

  jsonToCoreServiceModel(coreService: any): CoreServiceModel {
    let newCoreService: CoreServiceModel;
    newCoreService = new CoreServiceModel();
    if (coreService !== undefined && coreService !== null) {
      newCoreService.name = coreService.name;
      newCoreService.versionInfo = this.jsonToVersionInfo(coreService.versionInfo);
      newCoreService.imageName = coreService.imageName;
      newCoreService.containerNames = coreService.containerNames;
      newCoreService.ports = this.jsonToPortsList(coreService.ports);
      newCoreService.status = coreService.status;
      newCoreService.networks = coreService.networks;
    }
    return newCoreService;
  }

  jsonToVersionInfo(versionInfo: any): VersionInfo {
    let newVersionInfo: VersionInfo;
    newVersionInfo = new VersionInfo();
    if (versionInfo !== undefined && versionInfo !== null) {
      newVersionInfo.commitId = versionInfo.commitId;
      if (versionInfo.date !== undefined && versionInfo.date !== 'unspecified') {
        newVersionInfo.date = new Date(versionInfo.date);
      }
      newVersionInfo.tag = versionInfo.tag;
    }
    return newVersionInfo;
  }

  jsonToPortsList(ports: any[]): ContainerPortModel[] {
    let portsList: ContainerPortModel[] = [];
    for (let port of ports) {
      portsList.push(this.jsonToContainerPortModel(port));
    }
    return portsList;
  }

  jsonToContainerPortModel(port: any): ContainerPortModel {
    let newPort: ContainerPortModel;
    newPort = new ContainerPortModel();
    if (port !== undefined && port !== null) {
      newPort.ip = port.IP;
      newPort.privatePort = port.PrivatePort;
      newPort.publicPort = port.PublicPort;
      newPort.type = port.Type;
    }
    return newPort;
  }
}
