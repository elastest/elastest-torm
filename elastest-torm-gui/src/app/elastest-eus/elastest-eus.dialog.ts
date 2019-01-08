import { MatDialogRef } from '@angular/material';
import { Component } from '@angular/core';
import { EusTestModel } from './elastest-eus-test-model';

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
  public testModel: EusTestModel;

  constructor(public dialogRef: MatDialogRef<ElastestEusDialog>) {}
}
