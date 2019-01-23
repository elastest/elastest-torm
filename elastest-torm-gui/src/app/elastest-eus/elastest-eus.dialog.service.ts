import { Observable } from 'rxjs/Rx';
import { ElastestEusDialog } from './elastest-eus.dialog';
import { MatDialogRef, MatDialog, MatDialogConfig } from '@angular/material';
import { Injectable } from '@angular/core';

@Injectable()
export class ElastestEusDialogService {
  constructor(private dialog: MatDialog) {}

  public getDialog(close: boolean): MatDialogRef<ElastestEusDialog> {
    let dialogRef: MatDialogRef<ElastestEusDialog> = this.dialog.open(ElastestEusDialog, {
      disableClose: close,
    });

    return dialogRef;
  }

  public popUpMessage(title: string, message: string): Observable<boolean> {
    let dialogRef: MatDialogRef<ElastestEusDialog> = this.dialog.open(ElastestEusDialog);
    dialogRef.componentInstance.title = title;
    dialogRef.componentInstance.message = message;

    return dialogRef.afterClosed();
  }
}
