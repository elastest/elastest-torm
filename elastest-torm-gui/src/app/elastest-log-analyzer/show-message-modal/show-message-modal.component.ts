import { Component, Inject, OnInit, Optional } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material';

@Component({
  selector: 'show-message-modal',
  templateUrl: './show-message-modal.component.html',
  styleUrls: ['./show-message-modal.component.scss']
})
export class ShowMessageModalComponent implements OnInit {
  row: any;
  columns: string[];
  rawData: any;
  constructor(
    private dialogRef: MatDialogRef<ShowMessageModalComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: any,
  ) {
    this.row = data.row;
    this.columns = data.columns;

    if(this.row.rawData !== undefined && this.row.rawData !== null){
      this.rawData = JSON.parse(this.row.rawData);
    }
  }

  ngOnInit() {
  }

}
