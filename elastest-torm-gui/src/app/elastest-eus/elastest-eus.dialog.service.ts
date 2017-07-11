import {Observable} from 'rxjs/Rx';
import {ElastestEusDialog} from './elastest-eus.dialog';
import {MdDialogRef, MdDialog, MdDialogConfig} from '@angular/material';
import {Injectable} from '@angular/core';

@Injectable()
export class ElastestEusDialogService {

  constructor(private dialog: MdDialog) {}

  public open(title: string, message: string, closeButton: boolean, iframeUrl: string): Observable<boolean> {

    let dialogRef: MdDialogRef<ElastestEusDialog>;

    dialogRef = this.dialog.open(ElastestEusDialog, {
      disableClose: closeButton
    });

    dialogRef.componentInstance.title = title;
    dialogRef.componentInstance.message = message;
    dialogRef.componentInstance.iframeUrl = iframeUrl;
    dialogRef.componentInstance.closeButton = closeButton;

    return dialogRef.afterClosed();
  }
}
