import { TdDigitsPipe } from '@covalent/core/common/pipes/digits/digits.pipe';
import { ColorSchemeModel } from './color-scheme-model';
import { MetricsDataModel } from './metrics-data-model';
import { SingleMetricModel } from './single-metric-model';


export class ETRESMetricsModel extends MetricsDataModel { //ElasTest RabbitMq ElasticSearch Metrics Model
    name: string;
    colorScheme: ColorSchemeModel;
    gradient: boolean;

    showXAxis: boolean;
    showYAxis: boolean;
    showLegend: boolean;

    showXAxisLabel: boolean;
    xAxisLabel: string;

    showYAxisLabel: boolean;
    yAxisLabel: string;

    autoScale: boolean;

    timeline: boolean;
    data: SingleMetricModel[];
    type: string;

    constructor() {
        super();
        this.name = '';

        this.showXAxis = true;
        this.showYAxis = true;
        this.gradient = false;
        this.showLegend = false;
        this.showXAxisLabel = true;
        this.xAxisLabel = '';
        this.showYAxisLabel = true;
        this.yAxisLabel = '';
        this.timeline = false;
        this.autoScale = true;

        this.colorScheme = new ColorSchemeModel();
        this.colorScheme.domain = ['#ffac2f', '#666666'];

        //Data
        let test: SingleMetricModel = new SingleMetricModel();
        test.name = 'Test';
        let sut: SingleMetricModel = new SingleMetricModel();
        sut.name = 'Sut';

        this.data = [];
        this.data.push(test);
        this.data.push(sut);

        this.type = '';
    }
}