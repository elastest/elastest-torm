import { Observable } from 'rxjs/Rx';
import { LineChartComponent } from '@swimlane/ngx-charts/release';
import { ComboChartComponent } from './combo-chart/combo-chart.component';
import { ESRabComplexMetricsModel } from './models/es-rab-complex-metrics-model';
import { ComplexMetricsModel } from './models/complex-metrics-model';
import { Component, Input, OnInit, ViewChild } from '@angular/core';

@Component({
    selector: 'complex-metrics-view',
    templateUrl: './complex-metrics-view.component.html',
    styleUrls: ['./complex-metrics-view.component.scss']
})
export class ComplexMetricsViewComponent implements OnInit {
    @Input()
    public model: ESRabComplexMetricsModel;
    @Input()
    public showConfig: boolean;

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
