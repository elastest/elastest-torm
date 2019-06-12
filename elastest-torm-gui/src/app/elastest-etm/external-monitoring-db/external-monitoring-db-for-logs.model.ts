import { ExternalMonitoringDB } from './external-monitoring-db.model';
import { ExternalElasticsearch } from './external-elasticsearch.model';

export class ExternalMonitoringDBForLogs {
  private externalMonitoringDB: ExternalMonitoringDB;

  id: number;
  type: ExternalMonitoringDBForLogsType;

  constructor(externalMonitoringDBForLogs: any = undefined) {
    if (!externalMonitoringDBForLogs) {
      // Hardcoded because has only 1 type
      this.type = 'ELASTICSEARCH';
      this.externalMonitoringDB = new ExternalElasticsearch();
    } else {
      this.id = externalMonitoringDBForLogs.id;
      this.type = externalMonitoringDBForLogs.type;

      if (this.type) {
        if (this.type === 'ELASTICSEARCH') {
          this.externalMonitoringDB = new ExternalElasticsearch(externalMonitoringDBForLogs.externalMonitoringDB);
        }
      }
    }
  }

  isUsingExternalElasticsearchForLogs(): boolean {
    return this.type === 'ELASTICSEARCH';
  }

  getExternalElasticsearch(): ExternalElasticsearch {
    if (this.externalMonitoringDB instanceof ExternalElasticsearch) {
      return this.externalMonitoringDB;
    } else {
      return undefined;
    }
  }
}

export type ExternalMonitoringDBForLogsType = 'ELASTICSEARCH' | '';
