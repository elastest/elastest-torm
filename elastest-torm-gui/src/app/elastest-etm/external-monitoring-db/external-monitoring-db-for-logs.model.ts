import { ExternalMonitoringDB } from './external-monitoring-db.model';
import { ExternalElasticsearch } from './external-elasticsearch.model';

const TYPE: any = {
  ELASTICSEARCH: 'ELASTICSEARCH',
  NONE: 'NONE',
};

export class ExternalMonitoringDBForLogs {
  private externalMonitoringDB: ExternalMonitoringDB;

  id: number;
  type: ExternalMonitoringDBForLogsType;

  constructor(externalMonitoringDBForLogs: any = undefined) {
    if (!externalMonitoringDBForLogs) {
      this.initFromType('NONE');
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

  initFromType(type: ExternalMonitoringDBForLogsType): void {
    if (type) {
      if (type === 'ELASTICSEARCH') {
        this.type = type;
        this.externalMonitoringDB = new ExternalElasticsearch();
      } else if (type === 'NONE') {
        this.type = type;
        this.externalMonitoringDB = undefined;
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

  getTypes(): ExternalMonitoringDBForLogsType[] {
    return Object.keys(TYPE);
  }
}

export type ExternalMonitoringDBForLogsType = keyof typeof TYPE;
