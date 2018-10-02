import { VersionInfo } from './version-info.model';
import { ContainerPortModel } from './container-port.model';

export class CoreServiceModel {
  name: string;
  versionInfo: VersionInfo;
  imageName: string;
  imageDate: Date;
  containerNames: string[];
  ports: ContainerPortModel[];
  status: string;
  networks: string[];

  constructor() {}

  isPlatformImage(): boolean {
    return this.name === 'platform';
  }

  getStatusIcon(): any {
    let icon: any = {
      name: 'fiber_manual_record',
      color: '#666666',
      status: this.status,
    };

    if (this.status) {
      let lowerCaseStatus: string = this.status.toLowerCase();
      if (lowerCaseStatus.startsWith('up')) {
        icon.color = '#669a13';
      } else if (lowerCaseStatus.startsWith('created')) {
        icon.color = '#ffac2f';
      } else {
        icon.color = '#c82a0e';
      }
    }
    return icon;
  }
}
