import { SingleMetricModel } from '../../models/single-metric-model';
import { LineChartMetricModel } from '../../models/linechart-metric-model';
import { MetricsFieldModel } from './metrics-field-model';
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
            '#a8c314', '#c31444', '#14c38a', '#de56a8', '#ff0000',
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
                                this.rightChartOne = this.rightChartOne.concat(data);
                                break;
                            case 'bytes':
                                this.leftChart = this.leftChart.concat(data);
                                break;
                            case 'amount/sec':
                                this.rightChartTwo = this.rightChartTwo.concat(data);
                                break;
                            default:
                                break;
                        }
                    }
                    );
            }
        }
    }

    updateData(trace: any) {
        let positionsList: number[] = this.allMetricsFields.getPositionsList(trace.type, trace.component_type);
        for (let position of positionsList) {
            let metric: MetricsFieldModel = this.allMetricsFields.fieldsList[position];
            if (metric.activated) {
                let parsedData: SingleMetricModel = this.elastestESService.convertToMetricTrace(trace, metric);
                if (parsedData !== undefined) {
                    this.addData(metric, [parsedData]);
                }
            }
        }
    }

    addData(metric: MetricsFieldModel, newData: SingleMetricModel[]) {
        let lineChartPosition: number;
        switch (metric.unit) {
            case 'percent':
                lineChartPosition = this.getPosition(this.rightChartOne, metric.name);
                if (lineChartPosition === undefined) {
                    let newLineChart: LineChartMetricModel = new LineChartMetricModel();
                    newLineChart.name = metric.name;
                    this.rightChartOne.push(newLineChart);
                    lineChartPosition = this.rightChartOne.length - 1;
                }
                this.rightChartOne[lineChartPosition].series = this.rightChartOne[lineChartPosition].series.concat(newData);
                this.rightChartOne = [...this.rightChartOne];
                break;
            case 'bytes':
                lineChartPosition = this.getPosition(this.leftChart, metric.name);
                if (lineChartPosition === undefined) {
                    let newLineChart: LineChartMetricModel = new LineChartMetricModel();
                    newLineChart.name = metric.name;
                    this.leftChart.push(newLineChart);
                    lineChartPosition = this.leftChart.length - 1;
                }
                this.leftChart[lineChartPosition].series = this.leftChart[lineChartPosition].series.concat(newData);
                this.leftChart = [...this.leftChart];
                break;
            case 'amount/sec':
                lineChartPosition = this.getPosition(this.rightChartTwo, metric.name);
                if (lineChartPosition === undefined) {
                    let newLineChart: LineChartMetricModel = new LineChartMetricModel();
                    newLineChart.name = metric.name;
                    this.rightChartTwo.push(newLineChart);
                    lineChartPosition = this.rightChartTwo.length - 1;
                }
                this.rightChartTwo[lineChartPosition].series = this.rightChartTwo[lineChartPosition].series.concat(newData);
                this.rightChartTwo = [...this.rightChartTwo];
                break;
            default:
                break;
        }
    }

    getPosition(list: LineChartMetricModel[], name: string) {
        let position: number;
        let counter: number = 0;
        for (let line of list) {
            if (line.name === name) {
                position = counter;
                break;
            }
            counter++;
        }
        return position;
    }


    loadPrevious() {
        let compareTrace: any = this.getOldTrace();
        let position: number = 0;

        for (let metric of this.allMetricsFields.fieldsList) {
            if (metric.activated) {
                this.elastestESService.getPrevMetricsFromTrace(this.metricsIndex, compareTrace, metric)
                    .subscribe(
                    (data) => {
                        if (data.length > 0) {
                            this.addData(metric, data[0].series);
                            this.prevLoaded = true;
                            this.elastestESService.popupService.openSnackBar('Previous traces has been loaded', 'OK');
                        } else {
                            this.elastestESService.popupService.openSnackBar('There aren\'t previous traces to load', 'OK');
                        }
                    },
                );
            }
            position++;
        }
    }

    getOldTrace() {
        let combinedList: LineChartMetricModel[] = this.rightChartOne.concat(this.leftChart).concat(this.rightChartTwo);
        let oldTrace: any = undefined;
        for (let singleLineChart of combinedList) {
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
}
