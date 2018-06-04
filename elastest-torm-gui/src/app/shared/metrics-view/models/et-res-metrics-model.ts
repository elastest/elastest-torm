import { MetricsFieldModel } from '../metrics-chart-card/models/metrics-field-model';
import { TdDigitsPipe } from '@covalent/core/common/pipes/digits/digits.pipe';
import { single } from 'rxjs/operator/single';
import { ColorSchemeModel } from './color-scheme-model';
import { MetricsModel } from './metrics-model';
import { LineChartMetricModel } from './linechart-metric-model';
import { ElastestESService } from '../../services/elastest-es.service';

export enum MetricsDataType {
    Test,
    Sut,
}

export class ETRESMetricsModel extends MetricsModel { // ElasTest RabbitMq ElasticSearch Metrics Model
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
    data: LineChartMetricModel[];

    metricsField: MetricsFieldModel;

    monitoringIndex: string;
    component: string;

    prevTraces: string[];
    prevLoaded: boolean;
    hidePrevBtn: boolean;

    constructor(elastestESService: ElastestESService, metricsField: MetricsFieldModel,
    ) {
        super();
        this.elastestESService = elastestESService;

        this.metricsField = metricsField;
        this.etType = this.metricsField.etType;

        this.showXAxis = true;
        this.showYAxis = true;
        this.gradient = false;
        this.showLegend = true;
        this.showXAxisLabel = true;
        this.xAxisLabel = '';
        this.showYAxisLabel = true;
        this.yAxisLabel = '';
        this.timeline = true;
        this.autoScale = true;

        this.colorScheme = new ColorSchemeModel();
        this.colorScheme.domain = ['#ffac2f', '#666666'];

        this.component = '';
        this.monitoringIndex = '';

        this.prevTraces = [];
        this.prevLoaded = false;
        this.hidePrevBtn = false;

        // Data
        this.data = this.elastestESService.getInitMetricsData();

        this.initMetricsTypeData();
    }

    initMetricsTypeData() {
        this.name = this.metricsField.etType + ' ' + this.metricsField.subtype;
        switch (this.metricsField.unit) {
            case 'percent':
                this.yAxisLabel = this.metricsField.subtype + ' %';
                break;
            case 'bytes':
                this.yAxisLabel = 'Bytes';
                break;
            case 'amount/sec':
                this.yAxisLabel = 'Amount / sec';
                break;
            default:
                this.name = '';
        }
    }

    getAllMetrics() {
        this.elastestESService.searchAllMetrics(this.monitoringIndex, this.metricsField)
            .subscribe(
            (data) => {
                this.data = data;
                this.data = [...this.data];
            }
            );
    }

    loadPrevious() {
        let compareTrace: any = this.getOldTrace();
        this.elastestESService.getPrevMetricsFromTrace(this.monitoringIndex, compareTrace, this.metricsField)
            .subscribe(
            (data) => {
                if (data.length > 0) {
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
                    this.elastestESService.popupService.openSnackBar('Previous traces has been loaded', 'OK');
                } else {
                    this.elastestESService.popupService.openSnackBar('There aren\'t previous traces to load', 'OK');
                }
            },
        );
    }

    getOldTrace() {
        let oldTrace: any = undefined;
        for (let singleLineChart of this.data) {
            if (singleLineChart.series.length > 0) {
                if (oldTrace === undefined) {
                    oldTrace = singleLineChart.series[0];
                }
                if (singleLineChart.series[0].name < oldTrace.name) {
                    oldTrace = singleLineChart.series[0];
                }
            }
        }
        return oldTrace;
    }

    updateData(trace: any) {
        let position: number = this.elastestESService.getMetricPosition(trace.component);

        if (position !== undefined) {
            let parsedData: any = this.elastestESService.convertToMetricTrace(trace, this.metricsField);
            if (parsedData !== undefined) {
                this.data[position].series.push(parsedData);
                this.data = [...this.data];
            }
        }
    }
}