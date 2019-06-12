import { ExternalMonitoringDB } from './external-monitoring-db.model';
import { ExternalElasticsearch } from './external-elasticsearch.model';
import { ExternalPrometheus } from './external-prometheus.model';

export class ExternalMonitoringDBForMetrics {
  id: number;
  type: ExternalMonitoringDBForMetricsType;
  externalMonitoringDB: ExternalMonitoringDB;

  constructor(externalMonitoringDBForMetrics: any = undefined) {
    if (!externalMonitoringDBForMetrics) {
      this.type = 'PROMETHEUS';
      this.externalMonitoringDB = new ExternalPrometheus();
    } else {
      this.id = externalMonitoringDBForMetrics.id;
      this.type = externalMonitoringDBForMetrics.type;

      if (this.type) {
        if (this.type === 'ELASTICSEARCH') {
          this.externalMonitoringDB = new ExternalElasticsearch(externalMonitoringDBForMetrics.externalMonitoringDB);
        } else if (this.type === 'PROMETHEUS') {
          this.externalMonitoringDB = new ExternalPrometheus(externalMonitoringDBForMetrics.externalMonitoringDB);
        }
      }
    }
  }

  isUsingExternalElasticsearchForMetrics(): boolean {
    return this.type === 'ELASTICSEARCH';
  }

  isUsingExternalPrometheusForMetrics(): boolean {
    return this.type === 'PROMETHEUS';
  }
}

export type ExternalMonitoringDBForMetricsType = 'ELASTICSEARCH' | 'PROMETHEUS' | '';
