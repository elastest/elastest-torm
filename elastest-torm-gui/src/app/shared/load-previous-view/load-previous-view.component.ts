import { LoadPreviousModel } from './load-previous-model';
import { Component, Input, OnInit } from '@angular/core';
import { MdSnackBar } from '@angular/material';

@Component({
  selector: 'load-previous-view',
  templateUrl: './load-previous-view.component.html',
  styleUrls: ['./load-previous-view.component.scss']
})
export class LoadPreviousViewComponent implements OnInit {
  @Input()
  public model: LoadPreviousModel;

  constructor(private snackBar: MdSnackBar, ) { }

  ngOnInit() {
  }

  loadPrevious() {
    this.model.loadPrevious()
      .subscribe(
      (messages) => {
        this.model.prevTraces = messages;
        this.model.prevLoaded = true;
        if (messages.length > 0) {
          this.openSnackBar('Previous traces has been loaded', 'OK');
        }
        else {
          this.openSnackBar('There aren\'t previous traces to load', 'OK');
        }
      },
      (error) => this.openSnackBar('There isn\'t reference traces yet to load previous', 'OK'),
    );
  }

  openSnackBar(message: string, action: string) {
    this.snackBar.open(message, action, {
      duration: 3500,
    });
  }

}
