import { LogFieldModel } from '../../shared/logs-view/models/log-field-model';
import { ESRabLogModel } from '../../shared/logs-view/models/es-rab-log-model';
import { AllLogsTypesModel } from '../../shared/logs-view/models/all-logs-types';
import { MetricsFieldModel } from '../../shared/metrics-view/metrics-chart-card/models/metrics-field-model';
import { AllMetricsFields } from '../../shared/metrics-view/metrics-chart-card/models/all-metrics-fields-model';
export class DashboardConfigModel {
  showAllInOne: boolean;
  allMetricsFields: AllMetricsFields;
  allLogsTypes: AllLogsTypesModel;

  constructor(
    jsonString: string = undefined,
    activateAllLogs: boolean = true,
    activateAllMetrics: boolean = false,
    showAllInOne: boolean = true,
  ) {
    this.showAllInOne = showAllInOne;
    this.allMetricsFields = new AllMetricsFields(false, '', activateAllMetrics);
    this.allLogsTypes = new AllLogsTypesModel('', activateAllLogs);
    if (jsonString !== undefined && jsonString !== null) {
      this.initFromJson(jsonString);
    }
  }

  initFromJson(jsonString: string): void {
    if (jsonString !== '') {
      let json: any = JSON.parse(jsonString);
      this.showAllInOne = json.showAllInOne;
      for (let metric of json.allMetricsFields.fieldsList) {
        if (metric.stream === undefined) {
          metric.stream = '';
        }

        if (metric.streamType === undefined) {
          metric.streamType = 'composed_metrics';
        }

        let metricModel: MetricsFieldModel = new MetricsFieldModel(
          metric.etType,
          metric.subtype,
          metric.unit,
          metric.component,
          metric.stream,
          metric.streamType,
        );

        metricModel.activated = metric.activated;
        let position: number = this.allMetricsFields.getPositionByName(metricModel.name);
        this.allMetricsFields.fieldsList[position] = metricModel;
      }

      for (let log of json.allLogsTypes.logsList) {
        if (log.stream === undefined) {
          log.stream = '';
        }
        let logModel: LogFieldModel = new LogFieldModel(log.component, log.stream);
        logModel.activated = log.activated;
        let position: number = this.allLogsTypes.getPositionByName(log.name);
        this.allLogsTypes.logsList[position] = logModel;
      }
    }
  }

  changeShowAllInOneActive($event): void {
    this.showAllInOne = $event.checked;
  }
}
