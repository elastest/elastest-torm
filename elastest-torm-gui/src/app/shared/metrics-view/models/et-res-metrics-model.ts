import { TdDigitsPipe } from '@covalent/core/common/pipes/digits/digits.pipe';
import { single } from 'rxjs/operator/single';
import { ColorSchemeModel } from './color-scheme-model';
import { MetricsModel } from './metrics-model';
import { SingleMetricModel } from './single-metric-model';
import { ElastestESService } from '../../services/elastest-es.service';

export type MetricsType =
    'cpu'
    | 'memory';

export enum MetricsDataType {
    Test,
    Sut,
}

export class ETRESMetricsModel extends MetricsModel { //ElasTest RabbitMq ElasticSearch Metrics Model
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


    constructor(elastestESService: ElastestESService, type: MetricsType) {
        super();
        this.elastestESService = elastestESService;

        this.type = type;

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

        this.componentType = '';
        this.metricsIndex = '';

        this.prevTraces = [];
        this.prevLoaded = false;
        this.hidePrevBtn = false;

        //Data
        this.data = this.elastestESService.getInitMetricsData();

        this.initMetricsTypeData();
    }

    initMetricsTypeData() {
        switch (this.type) {
            case 'cpu':
                this.name = 'CPU Usage';
                this.yAxisLabel = 'Usage %';
                break;
            case 'memory':
                this.name = 'Memory Usage';
                this.yAxisLabel = 'Usage %';
                break;
            default:
                this.name = '';
        }
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
        let compareTrace: any = this.getOldTrace();
        if (compareTrace !== undefined) {
            this.elastestESService.getPrevMetricsFromTrace(this.metricsIndex, compareTrace, this.type)
                .subscribe(
                (data) => {
                    if (data[MetricsDataType.Test].series.length > 0) {
                        this.data[MetricsDataType.Test].series.unshift.apply(
                            this.data[MetricsDataType.Test].series,
                            data[MetricsDataType.Test].series
                        );
                        this.prevLoaded = true;
                    }
                    if (data[MetricsDataType.Sut].series.length > 0) {
                        this.data[MetricsDataType.Sut].series.unshift.apply(
                            this.data[MetricsDataType.Sut].series,
                            data[MetricsDataType.Sut].series
                        );
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

    getOldTrace() {
        let oldTrace: any = undefined;
        for (let singleMetric of this.data) {
            if (oldTrace === undefined && singleMetric.series.length > 0) {
                oldTrace = singleMetric.series[0];
            }
            if (singleMetric.series[0].name < oldTrace.name) {
                oldTrace = singleMetric.series[0];
            }
        }
        return oldTrace;
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