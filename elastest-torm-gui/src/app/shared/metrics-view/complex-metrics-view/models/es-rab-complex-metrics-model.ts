import { AllMetricsFields } from './all-metrics-fields-model';
import { ColorSchemeModel } from '../../models/color-scheme-model';
import { ElastestESService } from '../../../services/elastest-es.service';
import { ComplexMetricsModel } from './complex-metrics-model';

export class ESRabComplexMetricsModel extends ComplexMetricsModel {
    elastestESService: ElastestESService;
    allMetricsFields: AllMetricsFields;
    metricsIndex: string;

    constructor(elastestESService: ElastestESService) {
        super();
        this.elastestESService = elastestESService;
        this.allMetricsFields = new AllMetricsFields(); // Object with a list of all metrics
        this.metricsIndex = '';

        this.showXAxisLabel = false;
        this.xAxisLabel = 'Time';

        this.yAxisLabelLeft = 'Bytes';
        this.yAxisLabelRightOne = 'Usage %';
        this.yAxisLabelRightTwo = 'Amount/sec';

        this.yLeftAxisTickFormatting = this.normalFormat;
        this.yRightOneAxisTickFormatting = this.percentFormat;
        this.yRightTwoAxisTickFormatting = this.normalFormat;


        this.scheme = this.getDefaultChartScheme();
    }

    getDefaultChartScheme() {
        let defaultChartScheme: ColorSchemeModel = new ColorSchemeModel();
        defaultChartScheme.name = 'defaultChartScheme';
        defaultChartScheme.selectable = true;
        defaultChartScheme.group = 'time';
        defaultChartScheme.domain = [
            '#01579b', '#7aa3e5', '#cc7f20', '#00bfa5', '#a29e0e',
            '#1dbf00', '#7e00bf', '#bf003e', '#ffac2f', '#666666',
            '#276279', '#68822a', '#a8385d', '#c77c4f', '#865c84',
            '#a8c314', '#c31444', '#14c38a', '#de56a8', '#ff0000'
        ];
        return defaultChartScheme;
    }

    getAllMetrics() {
        for (let metric of this.allMetricsFields.fieldsList) {
            if (metric.activated) {
                this.elastestESService.searchAllMetrics(this.metricsIndex, metric)
                    .subscribe(
                    (data) => {
                        switch (metric.unit) {
                            case 'percent':
                                this.rightChartOne = [...this.rightChartOne.concat(data)];
                                break;
                            case 'bytes':
                                this.leftChart = [...this.leftChart.concat(data)];
                                break;
                            case 'amount/sec':
                                this.rightChartTwo = [...this.rightChartTwo.concat(data)];
                                break;
                            default:
                                break;
                        }
                    }
                    );
            }
        }
    }



    loadPrevious() { }
}
