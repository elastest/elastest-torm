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
      this.fieldFilters = [];
      this.useESIndicesByExecution = false;
    } else {
      this.indices = externalElasticsearchJson.indices;
      this.fieldFilters = externalElasticsearchJson.fieldFilters ? externalElasticsearchJson.fieldFilters : [];
      this.useESIndicesByExecution =
        externalElasticsearchJson.useESIndicesByExecution !== undefined &&
        externalElasticsearchJson.useESIndicesByExecution !== null
          ? externalElasticsearchJson.useESIndicesByExecution
          : false;
    }
  }

  initByGiven(externalES: ExternalElasticsearch): void {
    super.initByGiven(externalES);
    this.indices = externalES.indices;
    this.streamFields = externalES.streamFields ? externalES.streamFields : '';
    this.fieldFilters = externalES.fieldFilters ? externalES.fieldFilters : [];
    this.useESIndicesByExecution =
      externalES.useESIndicesByExecution !== undefined && externalES.useESIndicesByExecution !== null
        ? externalES.useESIndicesByExecution
        : false;
  }

  getLogIndicesParamName(): string {
    return 'EXT_ELASTICSEARCH_LOGS_INDICES';
  }
}
