import {Observable} from 'rxjs/Rx';
import {ElastestEusDialog} from './elastest-eus.dialog';
import {MdDialogRef, MdDialog, MdDialogConfig} from '@angular/material';
import {Injectable} from '@angular/core';

@Injectable()
export class ElastestEusDialogService {

  constructor(private dialog: MdDialog) {}

  public getDialog(close: boolean): MdDialogRef<ElastestEusDialog> {
    let dialogRef: MdDialogRef<ElastestEusDialog> = this.dialog.open(ElastestEusDialog, {
      disableClose: close
    });

    return dialogRef;
  }

  public popUpMessage(title: string, message: string): Observable<boolean> {
    let dialogRef: MdDialogRef<ElastestEusDialog> =  this.dialog.open(ElastestEusDialog);
    dialogRef.componentInstance.title = title;
    dialogRef.componentInstance.message = message;

    return dialogRef.afterClosed();
  }
}
