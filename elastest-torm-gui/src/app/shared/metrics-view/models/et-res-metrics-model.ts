import { TdDigitsPipe } from '@covalent/core/common/pipes/digits/digits.pipe';
import { ColorSchemeModel } from './color-scheme-model';
import { MetricsDataModel } from './metrics-data-model';
import { SingleMetricModel } from './single-metric-model';
import { ElastestESService } from '../../services/elastest-es.service';

export class ETRESMetricsModel extends MetricsDataModel { //ElasTest RabbitMq ElasticSearch Metrics Model
    elastestESService: ElastestESService;

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

    metricsIndex: string;
    componentType: string;

    prevTraces: string[];
    prevLoaded: boolean;
    hidePrevBtn: boolean;


    constructor(elastestESService: ElastestESService) {
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
        this.componentType = '';
        this.metricsIndex = '';

        this.prevTraces = [];
        this.prevLoaded = false;
        this.hidePrevBtn = false;

        this.elastestESService = elastestESService;
    }

    getAllMetrics() {
        this.elastestESService.searchAllMetrics(this.metricsIndex, this.type)
            .subscribe(
            (data) => {
                this.data = data;
                this.data = [...this.data];
            }
            );
    }

    loadPrevious() {
        let compareTrace: any[] = [];
        if (this.data[1].series.length > 0) {
            compareTrace = this.data[1].series;
        }
        else if (this.data[0].series.length > 0) {
            compareTrace = this.data[0].series;
        }
        if (compareTrace.length > 0) {
            this.elastestESService.getPrevMetricsFromTrace(this.metricsIndex, compareTrace[0], this.type)
                .subscribe(
                (data) => {
                    if (data[0].series.length > 0) {
                        this.data[0].series.unshift.apply(this.data[0].series, data[0].series);
                        this.prevLoaded = true;
                    }
                    if (data[1].series.length > 0) {
                        this.data[1].series.unshift.apply(this.data[1].series, data[1].series);
                        this.prevLoaded = true;
                    }
                    this.data = [...this.data];
                },
            );
        }
        else {
            this.elastestESService.openSnackBar('There isn\'t reference traces yet to load previous', 'OK');
        }
    }

    updateData(trace: any) {
        let position: number = this.elastestESService.getMetricPosition(trace.component_type);

        if (position !== undefined) {
            let parsedData: any = this.elastestESService.convertToMetricTrace(trace, this.type);
            if (parsedData !== undefined) {
                this.data[position].series.push(parsedData);
                this.data = [...this.data];
            }
        }
    }
}