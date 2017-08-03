import { ComplexMetricsModel } from './models/complex-metrics-model';
import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'complex-metrics-view',
  templateUrl: './complex-metrics-view.component.html',
  styleUrls: ['./complex-metrics-view.component.scss']
})
export class ComplexMetricsViewComponent implements OnInit {
  @Input()
  public model: ComplexMetricsModel;
  constructor() { }

  ngOnInit() {
  }


}
