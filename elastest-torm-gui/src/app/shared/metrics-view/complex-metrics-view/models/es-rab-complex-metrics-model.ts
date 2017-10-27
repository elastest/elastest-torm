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
    activatedFieldsList: boolean[]
    metricsIndex: string;
    componentType: string;
    stream: string;

    leftChartAllData: LineChartMetricModel[];
    rightChartOneAllData: LineChartMetricModel[];
    rightChartTwoAllData: LineChartMetricModel[];

    constructor(elastestESService: ElastestESService) {
        super();
        this.elastestESService = elastestESService;
        this.allMetricsFields = new AllMetricsFields(); // Object with a list of all metrics
        this.initActivatedFieldsList();
        this.metricsIndex = '';
        this.componentType = '';
        this.stream = '';

        this.showXAxisLabel = false;
        this.xAxisLabel = 'Time';

        this.yAxisLabelLeft = 'Bytes';
        this.yAxisLabelRightOne = 'Usage %';
        this.yAxisLabelRightTwo = 'Amount / sec';

        this.yLeftAxisTickFormatting = this.normalFormat;
        this.yRightOneAxisTickFormatting = this.percentFormat;
        this.yRightTwoAxisTickFormatting = this.normalFormat;

        this.leftChartAllData = []
        this.rightChartOneAllData = []
        this.rightChartTwoAllData = []

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
            this.elastestESService.searchAllMetrics(this.metricsIndex, metric)
                .subscribe(
                (data) => {
                    switch (metric.unit) {
                        case 'percent':
                            this.rightChartOneAllData = this.rightChartOneAllData.concat(data);
                            if (metric.activated) {
                                this.rightChartOne = this.rightChartOne.concat(data);
                            }
                            break;
                        case 'bytes':
                            this.leftChartAllData = this.leftChartAllData.concat(data);
                            if (metric.activated) {
                                this.leftChart = this.leftChart.concat(data);
                            }
                            break;
                        case 'amount/sec':
                            this.rightChartTwoAllData = this.rightChartTwoAllData.concat(data);
                            if (metric.activated) {
                                this.rightChartTwo = this.rightChartTwo.concat(data);
                            }
                            break;
                        default:
                            break;
                    }
                }
                );
        }
    }

    updateData(trace: any) {
        let positionsList: number[] = this.allMetricsFields.getPositionsList(trace.type, trace.component_type);
        for (let position of positionsList) {
            let metric: MetricsFieldModel = this.allMetricsFields.fieldsList[position];
            let parsedData: SingleMetricModel = this.elastestESService.convertToMetricTrace(trace, metric);
            if (parsedData !== undefined) {
                this.addData(metric, [parsedData]);
            }
        }
    }

    addData(metric: MetricsFieldModel, newData: SingleMetricModel[]) {
        switch (metric.unit) {
            case 'percent':
                this.rightChartOneAllData = this.addDataToGivenList(this.rightChartOneAllData, metric, newData);
                this.rightChartOne = this.filterDataByGivenList(this.rightChartOneAllData);
                break;
            case 'bytes':
                this.leftChartAllData = this.addDataToGivenList(this.leftChartAllData, metric, newData);
                this.leftChart = this.filterDataByGivenList(this.leftChartAllData);
                break;
            case 'amount/sec':
                this.rightChartTwoAllData = this.addDataToGivenList(this.rightChartTwoAllData, metric, newData);
                this.rightChartTwo = this.filterDataByGivenList(this.rightChartTwoAllData);
                break;
            default:
                break;
        }
    }

    addSimpleMetricTraces(data: LineChartMetricModel[]) {
        this.leftChart = this.leftChart.concat(data);
    }

    addDataToSimpleMetric(metric: MetricsFieldModel, newData: SingleMetricModel[]) {
        this.leftChart = this.addDataToGivenList(this.leftChart, metric, newData);
    }

    addDataToGivenList(list: LineChartMetricModel[], metric: MetricsFieldModel, newData: SingleMetricModel[]) {
        let lineChartPosition: number = this.getPosition(list, metric.name);
        if (lineChartPosition === undefined) {
            let newLineChart: LineChartMetricModel = new LineChartMetricModel();
            newLineChart.name = metric.name;
            list.push(newLineChart);
            lineChartPosition = list.length - 1;
        }
        list[lineChartPosition].series = list[lineChartPosition].series.concat(newData);
        return [...list];
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

    clearData() {
        this.rightChartOne = [...[]];
        this.leftChart = [...[]];
        this.rightChartTwo = [...[]];
    }

    filterData() {
        this.initActivatedFieldsList();

        this.rightChartOne = [...this.filterDataByGivenList(this.rightChartOneAllData)];
        this.leftChart = [...this.filterDataByGivenList(this.leftChartAllData)];
        this.rightChartTwo = [...this.filterDataByGivenList(this.rightChartTwoAllData)];
    }

    filterDataByGivenList(allList: LineChartMetricModel[]) {

        let list: LineChartMetricModel[] = [];
        let position: number;
        for (let metric of allList) {
            position = this.allMetricsFields.getPositionByName(metric.name);
            if (this.activatedFieldsList[position]) {
                list.push(metric);
            }
        }
        return [...list];
    }

    activateAll() {
        for (let metric of this.allMetricsFields.fieldsList) {
            metric.activated = true;
        }
    }

    deactivateAll() {
        for (let metric of this.allMetricsFields.fieldsList) {
            metric.activated = false;
        }
    }


    activateAllMatchesByNameList(nameList: string[]) {
        this.deactivateAll();
        for (let name of nameList) {
            this.activateByName(name);
        }
        this.initActivatedFieldsList();
    }

    activateByName(name: string) {
        for (let metric of this.allMetricsFields.fieldsList) {
            if (metric.name === name) {
                metric.activated = true;
                break;
            }
        }
    }

    activateAllMatchesByNameSuffix(suffix: string) {
        let position: number = 0;
        for (let metric of this.allMetricsFields.fieldsList) {
            metric.activated = metric.name.endsWith(suffix);
            // To avoid this.initActivatedFieldsList() for
            this.activatedFieldsList[position] = metric.name.endsWith(suffix);
            position++;
        }
    }

    initActivatedFieldsList() {
        this.activatedFieldsList = [];
        this.allMetricsFields.fieldsList.map(
            (data) => this.activatedFieldsList.push(data.activated)
        );
    }
}
