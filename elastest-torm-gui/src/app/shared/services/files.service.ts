import { Injectable } from '@angular/core';
@Injectable()
export class FilesService {
  defaultName: string = 'elastest_download';
  defaultExtension: string = 'json';

  downloadObjectAsJson(obj: object, name: string = this.defaultName): void {
    this.downloadJsonStringAsJson(JSON.stringify(obj), name);
  }

  downloadJsonStringAsJson(jsonString: string, name: string = this.defaultName): void {
    let objAsBlob: Blob = new Blob([jsonString], { type: 'text/json;charset=utf-8;' });
    let url: string = window.URL.createObjectURL(objAsBlob);
    this.downloadFileFromGeneratedURL(url, name);
  }

  downloadStringAsTextFile(stringObj: string, name: string = this.defaultName, extension: string = 'txt'): void {
    let objAsBlob: Blob = new Blob([stringObj], { type: 'text/plain;charset=utf-8;' });
    let url: string = window.URL.createObjectURL(objAsBlob);
    this.downloadFileFromGeneratedURL(url, name, extension);
  }

  downloadFileFromGeneratedURL(url: string, name: string = this.defaultName, extension: string = this.defaultExtension): void {
    let a: any = document.createElement('a');
    document.body.appendChild(a);
    a.setAttribute('style', 'display: none');
    a.href = url;
    a.download =
      (name && name !== '' ? name : this.defaultName) +
      (extension && extension !== '' ? '.' + extension : '.' + this.defaultExtension);
    a.click();
    window.URL.revokeObjectURL(url);
    a.remove(); // remove the tmp element
  }
}
