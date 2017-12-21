import { MetricsFieldModel } from './models/metrics-field-model';
import { Observable } from 'rxjs/Rx';
import { LineChartComponent } from '@swimlane/ngx-charts/release';
import { ComboChartComponent } from './combo-chart/combo-chart.component';
import { ESRabComplexMetricsModel } from './models/es-rab-complex-metrics-model';
import { ComplexMetricsModel } from './models/complex-metrics-model';
import { Component, Input, OnInit, ViewChild } from '@angular/core';

@Component({
    selector: 'metrics-chart-card',
    templateUrl: './metrics-chart-card.component.html',
    styleUrls: ['./metrics-chart-card.component.scss']
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
  
    @ViewChild('comboChart')
    comboChart: ComboChartComponent;

    constructor() { }

    ngOnInit() {
    }

    getTimelineSubscription(): Observable<any> {
        return this.comboChart.timelineObs;
    }

    getHoverSubscription(): Observable<any> {
        return this.comboChart.hoverObs;
    }

    getLeaveSubscription(): Observable<any> {
        return this.comboChart.leaveObs;
    }

    updateDomain(domain) {
        this.comboChart.updateDomainAux(domain);
    }

    hoverCharts(item) {
        this.comboChart.tooltipObj.mouseMove('', true, item.value);
    }

    leaveCharts() {
        this.comboChart.hideCirclesAux();
    }
}
