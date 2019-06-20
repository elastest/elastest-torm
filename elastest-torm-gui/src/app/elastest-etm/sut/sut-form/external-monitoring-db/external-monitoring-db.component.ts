import { Component, OnInit, Input, ViewChild } from '@angular/core';
import { ExternalMonitoringDBForLogs } from '../../../external-monitoring-db/external-monitoring-db-for-logs.model';
import { ExternalMonitoringDBForMetrics } from '../../../external-monitoring-db/external-monitoring-db-for-metrics.model';
import { SutModel } from '../../sut-model';
import { ExternalElasticsearch } from '../../../external-monitoring-db/external-elasticsearch.model';
import { ParameterModel } from '../../../parameter/parameter-model';
import { ExternalElasticsearchConfigurationComponent } from './external-elasticsearch-configuration/external-elasticsearch-configuration.component';
import { ExternalPrometheus } from '../../../external-monitoring-db/external-prometheus.model';
import { ExternalPrometheusConfigurationComponent } from './external-prometheus-configuration/external-prometheus-configuration.component';
import { MatSelectChange } from '@angular/material';

@Component({
  selector: 'etm-external-monitoring-db',
  templateUrl: './external-monitoring-db.component.html',
  styleUrls: ['./external-monitoring-db.component.scss'],
})
export class ExternalMonitoringDbComponent implements OnInit {
  @ViewChild('externalElasticsearchConfigurationComponent')
  externalElasticsearchConfigurationComponent: ExternalElasticsearchConfigurationComponent;

  @ViewChild('externalPrometheusConfigurationComponent')
  externalPrometheusConfigurationComponent: ExternalPrometheusConfigurationComponent;

  @Input()
  sut: SutModel;

  // Logs
  externalElasticsearchForLogs: ExternalElasticsearch;

  // Metrics
  externalPrometheusForMetrics: ExternalPrometheus;
  externalElasticsearchForMetrics: ExternalElasticsearch;

  constructor() {}

  ngOnInit(): void {
    if (!this.sut.externalMonitoringDBForLogs) {
      this.sut.externalMonitoringDBForLogs = new ExternalMonitoringDBForLogs();
    }

    if (!this.sut.externalMonitoringDBForMetrics) {
      this.sut.externalMonitoringDBForMetrics = new ExternalMonitoringDBForMetrics();
    }

    this.externalElasticsearchForLogs = this.sut.externalMonitoringDBForLogs.getExternalElasticsearch();
    this.externalPrometheusForMetrics = this.sut.externalMonitoringDBForMetrics.getExternalPrometheus();
  }

  getParameters(): ParameterModel[] {
    return this.sut && this.sut.parameters ? this.sut.parameters : undefined;
  }

  setParameters(parameters: ParameterModel[]): void {
    if (this.sut && parameters) {
      this.sut.parameters = [...parameters];
    }
  }

  isValidForm(): boolean {
    let valid: boolean = true;
    if (this.externalElasticsearchConfigurationComponent) {
      valid = valid && this.externalElasticsearchConfigurationComponent.isValidForm();
    }

    if (this.externalPrometheusConfigurationComponent) {
      valid = valid && this.externalPrometheusConfigurationComponent.isValidForm();
    }

    return valid;
  }

  changeLogsDBType(event: MatSelectChange): void {
    if (event && event.value) {
      if (!this.sut.externalMonitoringDBForLogs) {
        this.sut.externalMonitoringDBForLogs = new ExternalMonitoringDBForLogs();
      }

      this.sut.externalMonitoringDBForLogs.initFromType(event.value);

      if (event.value === 'ELASTICSEARCH' && !this.externalElasticsearchForLogs) {
        this.externalElasticsearchForLogs = this.sut.externalMonitoringDBForLogs.getExternalElasticsearch();
      }
    }
  }

  changeMetricsDBType(event: MatSelectChange): void {
    if (event && event.value) {
      if (!this.sut.externalMonitoringDBForMetrics) {
        this.sut.externalMonitoringDBForMetrics = new ExternalMonitoringDBForMetrics();
      }

      this.sut.externalMonitoringDBForMetrics.initFromType(event.value);

      if (event.value === 'PROMETHEUS' && !this.externalPrometheusForMetrics) {
        this.externalPrometheusForMetrics = this.sut.externalMonitoringDBForMetrics.getExternalPrometheus();
      } else if (event.value === 'ELASTICSEARCH' && !this.externalElasticsearchForMetrics) {
        this.externalElasticsearchForMetrics = this.sut.externalMonitoringDBForMetrics.getExternalElasticsearch();
      }
    }
  }
}
