import { Injectable } from '@angular/core';
@Injectable()
export class FilesService {
  downloadObjectAsJson(obj: object): void {
    this.downloadJsonStringAsJson(JSON.stringify(obj));
  }

  downloadJsonStringAsJson(jsonString: string): void {
    let objAsBlob: Blob = new Blob([jsonString], { type: 'text/json;charset=utf-8;' });
    let url: string = window.URL.createObjectURL(objAsBlob);
    let a: any = document.createElement('a');
    document.body.appendChild(a);
    a.setAttribute('style', 'display: none');
    a.href = url;
    a.download = 'execution.json';
    a.click();
    window.URL.revokeObjectURL(url);
    a.remove(); // remove the tmp element
  }
}
