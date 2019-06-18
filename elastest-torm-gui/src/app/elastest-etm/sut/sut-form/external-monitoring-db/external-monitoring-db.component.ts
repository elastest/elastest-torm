import { Component, OnInit, Input } from '@angular/core';
import { ExternalMonitoringDBForLogs } from '../../../external-monitoring-db/external-monitoring-db-for-logs.model';
import { ExternalMonitoringDBForMetrics } from '../../../external-monitoring-db/external-monitoring-db-for-metrics.model';
import { SutModel } from '../../sut-model';
import { ExternalElasticsearch } from '../../../external-monitoring-db/external-elasticsearch.model';
import { ParameterModel } from '../../../parameter/parameter-model';

@Component({
  selector: 'etm-external-monitoring-db',
  templateUrl: './external-monitoring-db.component.html',
  styleUrls: ['./external-monitoring-db.component.scss'],
})
export class ExternalMonitoringDbComponent implements OnInit {
  @Input()
  sut: SutModel;

  externalElasticsearch: ExternalElasticsearch;

  constructor() {}

  ngOnInit(): void {
    if (!this.sut.externalMonitoringDBForLogs) {
      this.sut.externalMonitoringDBForLogs = new ExternalMonitoringDBForLogs();
    }

    if (!this.sut.externalMonitoringDBForMetrics) {
      this.sut.externalMonitoringDBForMetrics = new ExternalMonitoringDBForMetrics();
    }

    this.externalElasticsearch = this.sut.externalMonitoringDBForLogs.getExternalElasticsearch();
  }

  getParameters(): ParameterModel[] {
    return this.sut && this.sut.parameters ? this.sut.parameters : undefined;
  }

  setParameters(parameters: ParameterModel[]): void {
    if (this.sut && parameters) {
      this.sut.parameters = [...parameters];
    }
  }
}
