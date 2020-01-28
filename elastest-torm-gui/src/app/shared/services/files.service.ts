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

  isVideo(name: string): boolean {
    if (name) {
      let lowercasedName: string = name.toLowerCase();
      return (
        lowercasedName.includes('.mp4') ||
        lowercasedName.includes('.webm') ||
        lowercasedName.includes('.avi') ||
        lowercasedName.includes('.y4m')
      );
    } else {
      return false;
    }
  }

  isVideoByFileModel(file: FileModel): boolean {
    return file && this.isVideo(file.name);
  }

  isImage(name: string): boolean {
    if (name) {
      let lowercasedName: string = name.toLowerCase();
      return (
        lowercasedName.includes('.jpg') ||
        lowercasedName.includes('.jpeg') ||
        lowercasedName.includes('.png') ||
        lowercasedName.includes('.raw') ||
        lowercasedName.includes('.dng') ||
        lowercasedName.includes('.svg') ||
        lowercasedName.includes('.tiff') ||
        lowercasedName.includes('.gif')
      );
    } else {
      return false;
    }
  }

  isImageByFileModel(file: FileModel): boolean {
    return file && this.isImage(file.name);
  }

  isAudio(name: string): boolean {
    if (name) {
      let lowercasedName: string = name.toLowerCase();
      return (
        lowercasedName.includes('.wav') ||
        lowercasedName.includes('.mp3') ||
        lowercasedName.includes('.midi') ||
        lowercasedName.includes('.wma') ||
        lowercasedName.includes('.aac') ||
        lowercasedName.includes('.aiff') ||
        lowercasedName.includes('.ogg')
      );
    } else {
      return false;
    }
  }

  isAudioByFileModel(file: FileModel): boolean {
    return file && this.isAudio(file.name);
  }

  isCsv(name: string): boolean {
    if (name) {
      let lowercasedName: string = name.toLowerCase();
      return lowercasedName.includes('.csv');
    } else {
      return false;
    }
  }

  isCsvByFileModel(file: FileModel): boolean {
    return file && this.isImage(file.name);
  }

  isExcel(name: string): boolean {
    if (name) {
      let lowercasedName: string = name.toLowerCase();
      return (
        lowercasedName.includes('.xls') ||
        lowercasedName.includes('.xlt') ||
        lowercasedName.includes('.xlsx')
      );
    } else {
      return false;
    }
  }

  isExcelByFileModel(file: FileModel): boolean {
    return file && this.isExcel(file.name);
  }

  isPdf(name: string): boolean {
    if (name) {
      let lowercasedName: string = name.toLowerCase();
      return lowercasedName.includes('.pdf');
    } else {
      return false;
    }
  }

  isPdfByFileModel(file: FileModel): boolean {
    return file && this.isPdf(file.name);
  }

  getIcon(name: string): any {
    let icon: any = {
      name: 'file-alt',
      color: '#ffab2f',
    };

    if (this.isVideo(name)) {
      icon.name = 'file-video';
      return icon;
    }

    if (this.isAudio(name)) {
      icon.name = 'file-audio';
      return icon;
    }

    if (this.isImage(name)) {
      icon.name = 'file-image';
      return icon;
    }

    if (this.isPdf(name)) {
      icon.name = 'file-pdf';
      icon.color = '#ee3124';
      return icon;
    }

    if (this.isExcel(name)) {
      icon.name = 'file-excel';
      icon.color = '#287233';
      return icon;
    }

    if (this.isCsv(name)) {
      icon.name = 'file-csv';
      icon.color = '#287233';
      return icon;
    }

    icon.color = '#666666';
    return icon;
  }

  getIconByFileModel(file: FileModel): any {
    return this.getIcon(file ? file.name : '');
  }
}
