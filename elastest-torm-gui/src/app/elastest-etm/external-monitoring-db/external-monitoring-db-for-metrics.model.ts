import { ExternalMonitoringDB } from './external-monitoring-db.model';
import { ExternalElasticsearch } from './external-elasticsearch.model';
import { ExternalPrometheus } from './external-prometheus.model';

const TYPE: any = {
  ELASTICSEARCH: 'ELASTICSEARCH',
  PROMETHEUS: 'PROMETHEUS',
  NONE: 'NONE',
};

export class ExternalMonitoringDBForMetrics {
  id: number;
  type: ExternalMonitoringDBForMetricsType;
  externalMonitoringDB: ExternalMonitoringDB;

  constructor(externalMonitoringDBForMetrics: any = undefined) {
    if (!externalMonitoringDBForMetrics) {
      this.initFromType('NONE');
    } else {
      this.id = externalMonitoringDBForMetrics.id;
      this.type = externalMonitoringDBForMetrics.type;

      if (this.type) {
        if (this.type === 'ELASTICSEARCH') {
          this.externalMonitoringDB = new ExternalElasticsearch(externalMonitoringDBForMetrics.externalMonitoringDB);
        } else if (this.type === 'PROMETHEUS') {
          this.externalMonitoringDB = new ExternalPrometheus(externalMonitoringDBForMetrics.externalMonitoringDB);
        } else if (this.type === 'NONE') {
          this.externalMonitoringDB = undefined;
        }
      }
    }
  }

  initFromType(type: ExternalMonitoringDBForMetricsType): void {
    if (type) {
      if (type === 'ELASTICSEARCH') {
        this.type = type;
        this.externalMonitoringDB = new ExternalElasticsearch();
      } else if (type === 'PROMETHEUS') {
        this.type = type;
        this.externalMonitoringDB = new ExternalPrometheus();
      } else if (type === 'NONE') {
        this.type = type;
        this.externalMonitoringDB = undefined;
      }
    }
  }

  isUsingExternalElasticsearchForMetrics(): boolean {
    return this.type === 'ELASTICSEARCH';
  }

  isUsingExternalPrometheusForMetrics(): boolean {
    return this.type === 'PROMETHEUS';
  }

  isUsingExternalDBForMetrics(): boolean {
    return this.type !== 'NONE';
  }

  getTypes(): ExternalMonitoringDBForMetricsType[] {
    return Object.keys(TYPE);
  }

  getExternalElasticsearch(): ExternalElasticsearch {
    if (this.externalMonitoringDB instanceof ExternalElasticsearch) {
      return this.externalMonitoringDB;
    } else {
      return undefined;
    }
  }

  getExternalPrometheus(): ExternalPrometheus {
    if (this.externalMonitoringDB instanceof ExternalPrometheus) {
      return this.externalMonitoringDB;
    } else {
      return undefined;
    }
  }
}

export type ExternalMonitoringDBForMetricsType = keyof typeof TYPE;
