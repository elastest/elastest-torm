import { LogFieldModel } from '../../shared/logs-view/models/log-field-model';
import { ESRabLogModel } from '../../shared/logs-view/models/es-rab-log-model';
import { AllLogsTypesModel } from '../../shared/logs-view/models/all-logs-types';
import { MetricsFieldModel } from '../../shared/metrics-view/complex-metrics-view/models/metrics-field-model';
import { AllMetricsFields } from '../../shared/metrics-view/complex-metrics-view/models/all-metrics-fields-model';
export class DashboardConfigModel {
    showComplexMetrics: boolean;
    allMetricsFields: AllMetricsFields;
    allLogsTypes: AllLogsTypesModel;

    constructor(jsonString: string = undefined) {
        this.showComplexMetrics = true;
        this.initAllMetricsFields();
        this.allLogsTypes = new AllLogsTypesModel();
        if (jsonString !== undefined && jsonString !== null) {
            this.initFromJson(jsonString);
        }
    }

    public initAllMetricsFields() {
        this.allMetricsFields = new AllMetricsFields(false);
    }

    initFromJson(jsonString: string) {
        if (jsonString !== '') {
            let json: any = JSON.parse(jsonString);
            this.showComplexMetrics = json.showComplexMetrics;
            for (let metric of json.allMetricsFields.fieldsList) {
                if (metric.stream === undefined) {
                    metric.stream = '';
                }
                let metricModel: MetricsFieldModel = new MetricsFieldModel(metric.type, metric.subtype, metric.unit, metric.componentType, metric.stream);
                metricModel.activated = metric.activated;
                let position: number = this.allMetricsFields.getPositionByName(metricModel.name);
                this.allMetricsFields.fieldsList[position] = metricModel;
            }

            for (let log of json.allLogsTypes.logsList) {
                if (log.stream === undefined) {
                    log.stream = '';
                }
                let logModel: LogFieldModel = new LogFieldModel(log.componentType, log.stream);
                logModel.activated = log.activated;
                let position: number = this.allLogsTypes.getPositionByName(log.name);
                this.allLogsTypes.logsList[position] = logModel;
            }
        }
    }

    changeShowComplexMetricsActive($event) {
        this.showComplexMetrics = $event.checked;
    }

}