import { ComboChartComponent } from './combo-chart/combo-chart.component';
import { ViewChild } from '@angular/core/src/metadata/di';
import { ESRabComplexMetricsModel } from './models/es-rab-complex-metrics-model';
import { ComplexMetricsModel } from './models/complex-metrics-model';
import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'complex-metrics-view',
  templateUrl: './complex-metrics-view.component.html',
  styleUrls: ['./complex-metrics-view.component.scss']
})
export class ComplexMetricsViewComponent implements OnInit {
  @Input()
  public model: ESRabComplexMetricsModel;
  // @ViewChild
  // comboChart: ComboChartComponent;

  constructor() { }

  ngOnInit() {
  }


}
