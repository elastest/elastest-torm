import { MultiConfigModel } from '../../shared/multi-config-view/multi-config-view.component';
import { ExternalMonitoringDB } from './external-monitoring-db.model';

export class ExternalElasticsearch extends ExternalMonitoringDB {
  // CONSTANT
  monitoringType: string = 'ELASTICSEARCH';

  indices: string;
  streamFields: string;
  fieldFilters: MultiConfigModel[];
  useESIndicesByExecution: boolean = false;

  constructor(externalElasticsearchJson: any = undefined) {
    super(externalElasticsearchJson);
    if (!externalElasticsearchJson) {
      this.indices = '';
      this.streamFields = '';
      this.fieldFilters = [];
      this.useESIndicesByExecution = false;
    } else {
      this.indices = externalElasticsearchJson.indices;
      this.streamFields = externalElasticsearchJson.streamFields ? externalElasticsearchJson.streamFields : '';
      this.fieldFilters = externalElasticsearchJson.fieldFilters ? externalElasticsearchJson.fieldFilters : [];
      this.useESIndicesByExecution =
        externalElasticsearchJson.useESIndicesByExecution !== undefined &&
        externalElasticsearchJson.useESIndicesByExecution !== null
          ? externalElasticsearchJson.useESIndicesByExecution
          : false;
    }
  }

  getLogIndicesParamName(): string {
    return 'EXT_ELASTICSEARCH_LOGS_INDICES';
  }
}
