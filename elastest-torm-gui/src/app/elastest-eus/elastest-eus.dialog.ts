import { MdDialogRef } from '@angular/material';
import { Component } from '@angular/core';

@Component({
  selector: 'eus-dialog',
  templateUrl: './elastest-eus.dialog.html',
})
export class ElastestEusDialog {
  public title: string;
  public message: string;
  public iframeUrl: string;
  public loading: boolean = false;
  public sessionType: 'live' | 'video' = 'live';
  public closeButton: boolean = false;

  constructor(public dialogRef: MdDialogRef<ElastestEusDialog>) {}
}
