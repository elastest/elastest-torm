import { Injectable } from '@angular/core';
import { FileModel } from '../../elastest-etm/files-manager/file-model';
import { ConfigurationService } from '../../config/configuration-service.service';
import { ElastestEusDialogService } from '../../elastest-eus/elastest-eus.dialog.service';
import { ElastestEusDialog } from '../../elastest-eus/elastest-eus.dialog';
import { MatDialogRef } from '@angular/material';
@Injectable()
export class FilesService {
  defaultName: string = 'elastest_download';
  defaultExtension: string = 'json';
  filesUrlPrefix: string;

  constructor(
    private configurationService: ConfigurationService,
    private eusDialog: ElastestEusDialogService,
  ) {
    this.filesUrlPrefix = configurationService.configModel.proxyHost;
  }

  downloadObjectAsJson(obj: object, name: string = this.defaultName): void {
    this.downloadJsonStringAsJson(JSON.stringify(obj), name);
  }

  downloadJsonStringAsJson(
    jsonString: string,
    name: string = this.defaultName,
  ): void {
    let objAsBlob: Blob = new Blob([jsonString], {
      type: 'text/json;charset=utf-8;',
    });
    let url: string = window.URL.createObjectURL(objAsBlob);
    this.downloadFileFromGeneratedURL(url, name);
  }

  downloadStringAsTextFile(
    stringObj: string,
    name: string = this.defaultName,
    extension: string = 'txt',
  ): void {
    let objAsBlob: Blob = new Blob([stringObj], {
      type: 'text/plain;charset=utf-8;',
    });
    let url: string = window.URL.createObjectURL(objAsBlob);
    this.downloadFileFromGeneratedURL(url, name, extension);
  }

  downloadFileFromGeneratedURL(
    url: string,
    name: string = this.defaultName,
    extension: string = this.defaultExtension,
  ): void {
    let a: any = document.createElement('a');
    document.body.appendChild(a);
    a.setAttribute('style', 'display: none');
    a.href = url;
    a.download =
      (name && name !== '' ? name : this.defaultName) +
      (extension && extension !== ''
        ? '.' + extension
        : '.' + this.defaultExtension);
    a.click();
    window.URL.revokeObjectURL(url);
    a.remove(); // remove the tmp element
  }

  isVideo(name: string): boolean {
    return (
      name.includes('.mp4') ||
      name.includes('.webm') ||
      name.includes('.avi') ||
      name.includes('.y4m')
    );
  }

  isVideoByFileModel(file: FileModel): boolean {
    return file && this.isVideo(file.name);
  }

  getFileUrl(file: FileModel): string {
    return this.filesUrlPrefix + file.encodedUrl;
  }

  openVideoInDialog(file: FileModel, title: string = 'Recorded Video'): void {
    let url: string = this.getFileUrl(file);
    let dialog: MatDialogRef<ElastestEusDialog> = this.eusDialog.getDialog(
      true,
    );
    dialog.componentInstance.title = title;
    dialog.componentInstance.iframeUrl = url;
    dialog.componentInstance.sessionType = 'video';
    dialog.componentInstance.closeButton = true;
  }

  openFileInNewTab(file: FileModel): void {
    window.open(this.getFileUrl(file));
  }
}
