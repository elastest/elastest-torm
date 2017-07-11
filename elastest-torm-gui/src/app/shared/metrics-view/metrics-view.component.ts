import { MetricsViewModel } from './metrics-view-model';

import { Component, Input, OnInit, ViewChild } from '@angular/core';

@Component({
  selector: 'metrics-view',
  templateUrl: './metrics-view.component.html',
  styleUrls: ['./metrics-view.component.scss']
})
export class MetricsViewComponent implements OnInit {
  @Input()
  public model: MetricsViewModel;

  constructor() { }

  ngOnInit() {
  }

}
