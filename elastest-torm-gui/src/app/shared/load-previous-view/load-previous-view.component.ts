import { LoadPreviousModel } from './load-previous-model';
import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'load-previous-view',
  templateUrl: './load-previous-view.component.html',
  styleUrls: ['./load-previous-view.component.scss']
})
export class LoadPreviousViewComponent implements OnInit {
  @Input()
  public model: LoadPreviousModel;

  constructor() { }

  ngOnInit() {
  }

  loadPrevious() {
    this.model.loadPrevious();
  }



}
