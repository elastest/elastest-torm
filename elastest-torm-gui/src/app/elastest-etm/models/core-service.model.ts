import { VersionInfo } from './version-info.model';
import { ContainerPortModel } from './container-port.model';
import { getErrorColor, getWarnColor, getInfoColor } from '../../shared/utils';

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
        icon.color = getInfoColor();
      } else if (lowerCaseStatus.startsWith('created')) {
        icon.color = getWarnColor();
      } else {
        icon.color = getErrorColor();
      }
    }
    return icon;
  }

  isStatusError(): boolean {
    let error: boolean = false;
    if (!this.status) {
      error = true;
    } else {
      let lowerCaseStatus: string = this.status.toLowerCase();
      error = !lowerCaseStatus.startsWith('up') && !lowerCaseStatus.startsWith('created');
    }

    return error;
  }
}
