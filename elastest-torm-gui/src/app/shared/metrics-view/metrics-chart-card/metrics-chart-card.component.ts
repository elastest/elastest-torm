import { MetricsFieldModel } from './models/metrics-field-model';
import { Observable } from 'rxjs/Rx';
import { ESRabComplexMetricsModel } from './models/es-rab-complex-metrics-model';
import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { MetricsViewComponent } from '../metrics-view.component';
import { ButtonModel } from '../../button-component/button.model';

@Component({
  selector: 'metrics-chart-card',
  templateUrl: './metrics-chart-card.component.html',
  styleUrls: ['./metrics-chart-card.component.scss'],
})
export class MetricsChartCardComponent implements OnInit {
  @Input()
  public model: ESRabComplexMetricsModel;

  @Input()
  public fieldsList?: MetricsFieldModel[];

  @Input()
  public showConfig: boolean;

  @Input()
  public remove: Function;

  @Input()
  public customButtons: ButtonModel[] = [];

  @ViewChild('metricsView')
  metricsView: MetricsViewComponent;

  constructor() {}

  ngOnInit(): void {}

  getTimelineSubscription(): Observable<any> {
    return this.metricsView.getTimelineSubscription();
  }

  getHoverSubscription(): Observable<any> {
    return this.metricsView.getHoverSubscription();
  }

  getLeaveSubscription(): Observable<any> {
    return this.metricsView.getLeaveSubscription();
  }

  updateDomain(domain): void {
    this.metricsView.updateDomain(domain);
  }

  hoverCharts(item): void {
    this.metricsView.hoverCharts(item);
  }

  leaveCharts(): void {
    this.metricsView.leaveCharts();
  }
}
