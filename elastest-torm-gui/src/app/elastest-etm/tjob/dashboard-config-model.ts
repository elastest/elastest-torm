import { MetricsFieldModel } from '../../shared/metrics-view/complex-metrics-view/models/metrics-field-model';
import { AllMetricsFields } from '../../shared/metrics-view/complex-metrics-view/models/all-metrics-fields-model';
export class DashboardConfigModel {
    showComplexMetrics: boolean;
    allMetricsFields: AllMetricsFields;

    constructor(jsonString: string = undefined) {
        this.showComplexMetrics = true;
        this.initAllMetricsFields();
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
                let metricModel: MetricsFieldModel = new MetricsFieldModel(metric.type, metric.subtype, metric.unit, metric.componentType);
                metricModel.activated = metric.activated;
                let position: number = this.allMetricsFields.getPositionByName(metricModel.name);
                this.allMetricsFields.fieldsList[position] = metricModel;
            }
        }
    }

    changeShowComplexMetricsActive($event) {
        this.showComplexMetrics = $event.checked;
    }

}