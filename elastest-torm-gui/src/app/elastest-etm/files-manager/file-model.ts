export class FileModel {
  name: string;
  extension: string;
  url: string;
  encodedUrl: string;
  folderName: string;
  resourceType: TJobExecTypeEnum;
  serviceName: string;

  isEusBrowserVideo(): boolean {
    return this.resourceType === 'EUS_BROWSER_RECORDING';
  }

  isEusFile(): boolean {
    return (
      this.isEusBrowserVideo() ||
      this.resourceType === 'EUS' ||
      this.resourceType === 'EUS_SESSION_INFO' ||
      this.resourceType === 'EUS_QOE_RESULT'
    );
  }

  isEusMetadataFile(): boolean {
    return this.resourceType === 'EUS_SESSION_INFO' || this.name.endsWith('.eus') || this.extension === 'eus';
  }

}

export type TJobExecTypeEnum =
  | 'DEFAULT'
  | 'EUS'
  | 'EUS_BROWSER_RECORDING'
  | 'EUS_SESSION_INFO'
  | 'EUS_QOE_RESULT'
  | '';
