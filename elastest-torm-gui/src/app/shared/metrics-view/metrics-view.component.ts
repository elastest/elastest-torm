import { MetricsFieldModel } from './metrics-chart-card/models/metrics-field-model';
import { Observable } from 'rxjs/Rx';
import { ComboChartComponent } from './metrics-chart-card/combo-chart/combo-chart.component';
import { ESRabComplexMetricsModel } from './metrics-chart-card/models/es-rab-complex-metrics-model';
import { Component, Input, OnInit, ViewChild } from '@angular/core';

@Component({
  selector: 'metrics-view',
  templateUrl: './metrics-view.component.html',
  styleUrls: ['./metrics-view.component.scss'],
})
export class MetricsViewComponent implements OnInit {
  @Input() public model: ESRabComplexMetricsModel;

  @Input() public fieldsList?: MetricsFieldModel[];

  @Input() public showConfig: boolean;

  @Input() public remove: Function;

  @ViewChild('comboChart', { static: true }) comboChart: ComboChartComponent;

  constructor() {}

  ngOnInit(): void {}

  getTimelineSubscription(): Observable<any> {
    return this.comboChart.timelineObs;
  }

  getHoverSubscription(): Observable<any> {
    return this.comboChart.hoverObs;
  }

  getLeaveSubscription(): Observable<any> {
    return this.comboChart.leaveObs;
  }

  updateDomain(domain): void {
    this.comboChart.updateDomainAux(domain);
  }

  hoverCharts(item): void {
    this.comboChart.tooltipObj.mouseMove('', true, item.value);
  }

  leaveCharts(): void {
    this.comboChart.hideCirclesAux();
  }
}
