import { MetricsFieldModel } from '../metrics-view/complex-metrics-view/models/metrics-field-model';
import { SingleMetricModel } from '../metrics-view/models/single-metric-model';
import { ESLogModel } from '../logs-view/models/elasticsearch-log-model';
import { MetricsDataType } from '../metrics-view/models/et-res-metrics-model';
import { LineChartMetricModel } from '../metrics-view/models/linechart-metric-model';
import { ElasticSearchService } from './elasticsearch.service';

import { componentFactoryName } from '@angular/compiler';
import { Injectable } from '@angular/core';
import { PopupService } from './popup.service';
import { Observable, Subject } from 'rxjs/Rx';

@Injectable()
export class ElastestESService {
    constructor(
        private elasticsearchService: ElasticSearchService,
        public popupService: PopupService,
    ) { }

    getTermsByTypeAndComponentType(type: string, componentType: string) {
        let terms: any[];
        terms = [
            { 'term': { _type: type } },
            { 'term': { component_type: componentType } },
        ];
        return terms;
    }


    getTermsByMetricsField(metricsField: MetricsFieldModel) {
        let terms: any[] = [{ 'term': { _type: metricsField.type } }];
        if (metricsField.componentType !== undefined && metricsField.componentType !== '') {
            terms.push(
                { 'term': { component_type: metricsField.componentType } },
            );
        }
        return terms;
    }

    searchAllLogs(index: string, type: string, componentType: string, theQuery?: any) {
        let _logs = new Subject<string[]>();
        let logs = _logs.asObservable();

        let terms: any[] = this.getTermsByTypeAndComponentType(type, componentType);
        this.elasticsearchService.searchAllByTerm(index, terms, theQuery).subscribe(
            (data) => {
                _logs.next(this.convertToLogTraces(data));
            }
        );

        return logs;
    }

    getPrevLogsFromTrace(index: string, traces: any[], type: string, componentType: string) {
        let _logs = new Subject<string[]>();
        let logs = _logs.asObservable();

        if (traces.length > 0) {
            let trace: any = traces[0];
            let terms: any[] = this.getTermsByTypeAndComponentType(type, componentType);
            this.elasticsearchService.getPrevFromTimestamp(index, trace.timestamp, terms).subscribe(
                (data) => {
                    _logs.next(this.convertToLogTraces(data));
                    if (data.length > 0) {
                        this.popupService.openSnackBar('Previous traces has been loaded', 'OK');
                    }
                    else {
                        this.popupService.openSnackBar('There aren\'t previous traces to load', 'OK');
                    }
                }
            );
        }
        else {
            _logs.next([]);
            this.popupService.openSnackBar('There isn\'t reference traces yet to load previous', 'OK');
        }
        return logs;
    }

    convertToLogTraces(data: any[]) {
        let tracesList: any[] = [];
        for (let logEntry of data) {
            if (logEntry._source['message'] !== undefined) {
                tracesList.push(
                    {
                        'timestamp': logEntry._source['@timestamp'],
                        'message': logEntry._source['message']
                    }
                );
            }
        }
        return tracesList;
    }



    // Metrics

    searchAllMetrics(index: string, metricsField: MetricsFieldModel, theQuery?: any) {
        let _metrics = new Subject<LineChartMetricModel[]>();
        let metrics = _metrics.asObservable();
        let terms: any[] = this.getTermsByMetricsField(metricsField);
        this.elasticsearchService.searchAllByTerm(index, terms, theQuery).subscribe(
            (data) => {
                _metrics.next(this.convertToMetricTraces(data, metricsField));
            }
        );

        return metrics;
    }

    getPrevMetricsFromTrace(index: string, trace: any, metricsField: MetricsFieldModel) {
        let _metrics = new Subject<LineChartMetricModel[]>();
        let metrics = _metrics.asObservable();

        if (trace !== undefined) {

            let terms: any[] = this.getTermsByMetricsField(metricsField);
            this.elasticsearchService.getPrevFromTimestamp(index, trace.timestamp, terms).subscribe(
                (data) => {
                    _metrics.next(this.convertToMetricTraces(data, metricsField));
                }
            );
        }
        else {
            _metrics.next([]);
            this.popupService.openSnackBar('There isn\'t reference traces yet to load previous', 'OK');
        }

        return metrics;
    }


    convertToMetricTraces(data: any[], metricsField: MetricsFieldModel) {
        let tracesList: LineChartMetricModel[];
        if (metricsField.componentType === undefined || metricsField.componentType === '') {
            tracesList = this.getInitMetricsData();

            let position: number = undefined;
            let parsedMetric: any;
            for (let logEntry of data) {
                parsedMetric = this.convertToMetricTrace(logEntry._source, metricsField);
                position = this.getMetricPosition(logEntry._source.component_type);
                if (position !== undefined && parsedMetric !== undefined) {
                    tracesList[position].series.push(parsedMetric);
                }
            }
        } else {
            tracesList = this.getInitMetricsDataComplex(metricsField);
            let parsedMetric: any;
            let position: number = undefined;
            for (let logEntry of data) {
                parsedMetric = this.convertToMetricTrace(logEntry._source, metricsField);
                position = 0;
                if (position !== undefined && parsedMetric !== undefined) {
                    tracesList[position].series.push(parsedMetric);
                }
            }
        }

        return tracesList;
    }

    convertToMetricTrace(trace: any, metricsField: MetricsFieldModel) {
        let parsedData: any = undefined;
        if (trace.type === 'cpu' && metricsField.type === 'cpu') {
            parsedData = this.convertToCpuData(trace, metricsField);
        } else if (trace.type === 'memory' && metricsField.type === 'memory') {
            parsedData = this.convertToMemoryData(trace, metricsField);
        } else if (trace.type === 'blkio' && metricsField.type === 'blkio') {
            parsedData = this.convertToDiskData(trace, metricsField);
        } else if (trace.type === 'net' && metricsField.type === 'net') {
            parsedData = this.convertToNetData(trace, metricsField);
        }
        return parsedData;
    }

    convertToCpuData(trace: any, metricsField: MetricsFieldModel) {
        let parsedData: SingleMetricModel = undefined;
        if (trace['@timestamp'] !== '0001-01-01T00:00:00.000Z') {
            switch (metricsField.subtype) {
                case ('totalUsage'):

                    parsedData = this.getBasicSingleMetric(trace);
                    parsedData.value = trace.cpu.totalUsage * 100;
                    break;
                default:
                    break;
            }
        }
        return parsedData;
    }

    convertToMemoryData(trace: any, metricsField: MetricsFieldModel) {
        let parsedData: SingleMetricModel = undefined;
        if (trace['@timestamp'] !== '0001-01-01T00:00:00.000Z') {
            switch (metricsField.subtype) {
                case ('usage'):
                    let perMemoryUsage: number = trace.memory.usage * 100 / trace.memory.limit;
                    parsedData = this.getBasicSingleMetric(trace);
                    parsedData.value = perMemoryUsage;
                    break;
                case ('limit'):
                    parsedData = this.getBasicSingleMetric(trace);
                    parsedData.value = trace.memory.limit;
                    break;
                case ('maxUsage'):
                    parsedData = this.getBasicSingleMetric(trace);
                    parsedData.value = trace.memory.maxUsage;
                    break;
                default:
                    break;
            }
        }
        return parsedData;
    }

    convertToDiskData(trace: any, metricsField: MetricsFieldModel) {
        let parsedData: SingleMetricModel = undefined;
        if (trace['@timestamp'] !== '0001-01-01T00:00:00.000Z') {
            switch (metricsField.subtype) {
                case ('read_ps'):
                    parsedData = this.getBasicSingleMetric(trace);
                    parsedData.value = trace.blkio.read_ps;
                    break;
                case ('write_ps'):
                    parsedData = this.getBasicSingleMetric(trace);
                    parsedData.value = trace.blkio.write_ps;
                    break;
                case ('total_ps'):
                    parsedData = this.getBasicSingleMetric(trace);
                    parsedData.value = trace.blkio.total_ps;
                    break;
                default:
                    break;
            }
        }
        return parsedData;
    }
    convertToNetData(trace: any, metricsField: MetricsFieldModel) {
        let parsedData: SingleMetricModel = undefined;
        if (trace['@timestamp'] !== '0001-01-01T00:00:00.000Z') {
            switch (metricsField.subtype) {
                case ('rxBytes_ps'):
                    parsedData = this.getBasicSingleMetric(trace);
                    parsedData.value = trace.net.rxBytes_ps;
                    break;
                case ('rxErrors_ps'):
                    parsedData = this.getBasicSingleMetric(trace);
                    parsedData.value = trace.net.rxErrors_ps;
                    break;
                case ('rxPackets_ps'):
                    parsedData = this.getBasicSingleMetric(trace);
                    parsedData.value = trace.net.rxPackets_ps;
                    break;
                case ('txBytes_ps'):
                    parsedData = this.getBasicSingleMetric(trace);
                    parsedData.value = trace.net.txBytes_ps;
                    break;
                case ('txErrors_ps'):
                    parsedData = this.getBasicSingleMetric(trace);
                    parsedData.value = trace.net.txErrors_ps;
                    break;
                case ('txPackets_ps'):
                    parsedData = this.getBasicSingleMetric(trace);
                    parsedData.value = trace.net.txPackets_ps;
                    break;
                default:
                    break;
            }
        }
        return parsedData;
    }

    getBasicSingleMetric(trace: any) {
        let parsedData: SingleMetricModel = new SingleMetricModel();
        parsedData.name = new Date('' + trace['@timestamp']);
        parsedData.timestamp = trace['@timestamp'];
        return parsedData;
    }

    getMetricPosition(componentType: string) {
        let position: number = undefined;

        componentType = componentType.toLowerCase();
        componentType = componentType.charAt(0).toUpperCase() + componentType.slice(1);

        if (MetricsDataType[componentType] !== undefined) {
            position = MetricsDataType[componentType];
        }
        return position;
    }

    getInitMetricsData() {
        let tracesList: LineChartMetricModel[] = [];

        for (let type in MetricsDataType) {
            if (isNaN(parseInt(type))) {
                tracesList[MetricsDataType[type]] = new LineChartMetricModel();
                tracesList[MetricsDataType[type]].name = type;
            }
        }
        return tracesList;
    }

    getInitMetricsDataComplex(metricsField: MetricsFieldModel) {
        let tracesList: LineChartMetricModel[] = [];
        let trace: LineChartMetricModel = new LineChartMetricModel();
        trace.name = metricsField.name;
        tracesList.push(trace);
        return tracesList;
    }

    initTestLog(log: ESLogModel) {
        log.name = 'Test Logs';
        log.type = 'testlogs';
        log.componentType = 'test';
    }

    initSutLog(log: ESLogModel) {
        log.name = 'SuT Logs';
        log.type = 'sutlogs';
        log.componentType = 'sut';
    }
}