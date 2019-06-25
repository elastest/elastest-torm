import { ExternalMonitoringDB } from './external-monitoring-db.model';
import { MultiConfigModel } from '../../shared/multi-config-view/multi-config-view.component';

export class ExternalPrometheus extends ExternalMonitoringDB {
  // CONSTANT
  monitoringType: string = 'PROMETHEUS';

  fieldFilters: MultiConfigModel[];

  constructor(externalPrometheusJson: any = undefined) {
    super(externalPrometheusJson);
    if (!externalPrometheusJson) {
      this.fieldFilters = [];
      // Default
      this.traceNameField = '__name__';
    } else {
      this.fieldFilters = externalPrometheusJson.fieldFilters ? externalPrometheusJson.fieldFilters : [];
      this.traceNameField = externalPrometheusJson.traceNameField ? externalPrometheusJson.traceNameField : '__name__';
    }
  }

  initByGiven(externalPrometheus: ExternalPrometheus): void {
    super.initByGiven(externalPrometheus);
    this.fieldFilters = externalPrometheus.fieldFilters ? externalPrometheus.fieldFilters : [];
    this.traceNameField = externalPrometheus.traceNameField ? externalPrometheus.traceNameField : '__name__';
  }
}
