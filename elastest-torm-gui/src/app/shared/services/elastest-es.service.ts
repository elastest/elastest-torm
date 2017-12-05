import { ESAggsModel } from '../elasticsearch-model/elasticsearch-model';
import { AllMetricsFields } from '../metrics-view/complex-metrics-view/models/all-metrics-fields-model';
import { MetricsFieldModel } from '../metrics-view/complex-metrics-view/models/metrics-field-model';
import { SingleMetricModel } from '../metrics-view/models/single-metric-model';
import { ESRabLogModel } from '../logs-view/models/es-rab-log-model';
import { MetricsDataType } from '../metrics-view/models/et-res-metrics-model';
import { LineChartMetricModel } from '../metrics-view/models/linechart-metric-model';
import { ElasticSearchService } from './elasticsearch.service';

import { componentFactoryName } from '@angular/compiler';
import { Injectable } from '@angular/core';
import { PopupService } from './popup.service';
import { Observable, Subject } from 'rxjs/Rx';

@Injectable()
export class ElastestESService {
    allMetricsFields: AllMetricsFields = new AllMetricsFields(); // Object with a list of all metrics
    esUrl: string;
    constructor(
        private elasticsearchService: ElasticSearchService,
        public popupService: PopupService,
    ) {
        this.esUrl = this.elasticsearchService.esUrl;
    }

    search(url: string, searchBody: any) {
        return this.elasticsearchService.internalSearch(url, searchBody);
    }

    getTermsByTypeAndComponent(type: string, component: string) {
        let terms: any[];
        terms = [
            { 'term': { _type: type } },
            { 'term': { component: component } },
        ];
        return terms;
    }

    getTermsByStreamAndComponent(stream: string, component: string) {
        let terms: any[];
        terms = [
            { 'term': { stream: stream } },
            { 'term': { component: component } },
        ];
        return terms;
    }


    getTermsByMetricsField(metricsField: MetricsFieldModel) {
        let terms: any[] = [{ 'term': { _type: metricsField.type } }];
        if (metricsField.component !== undefined && metricsField.component !== '') {
            terms.push(
                { 'term': { component: metricsField.component } },
            );
        }
        return terms;
    }

    searchAllLogs(index: string, stream: string, component: string, theQuery?: any) {
        let _logs: Subject<string[]> = new Subject<string[]>();
        let logs: Observable<string[]> = _logs.asObservable();

        let terms: any[] = this.getTermsByStreamAndComponent(stream, component);
        this.elasticsearchService.searchAllByTerm(index, terms, theQuery).subscribe(
            (data) => {
                _logs.next(this.convertToLogTraces(data));
            }
        );

        return logs;
    }

    getPrevLogsFromTrace(index: string, traces: any[], stream: string, component: string) {
        let _logs: Subject<string[]> = new Subject<string[]>();
        let logs = _logs.asObservable();

        if (traces.length > 0) {
            let trace: any = traces[0];
            let terms: any[] = this.getTermsByStreamAndComponent(stream, component);
            this.elasticsearchService.getPrevFromTimestamp(index, trace.timestamp, terms).subscribe(
                (data) => {
                    _logs.next(this.convertToLogTraces(data));
                    if (data.length > 0) {
                        this.popupService.openSnackBar('Previous traces has been loaded', 'OK');
                    } else {
                        this.popupService.openSnackBar('There aren\'t previous traces to load', 'OK');
                    }
                }
            );
        } else {
            _logs.error('There isn\'t reference traces yet to load previous')
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


    getLastLogTraces(index: string, stream: string, component: string, size: number = 10) {
        let _logs: Subject<string[]> = new Subject<string[]>();
        let logs = _logs.asObservable();

        let terms: any[] = this.getTermsByStreamAndComponent(stream, component);

        this.elasticsearchService.getLast(index, terms, size).subscribe(
            (data) => {
                _logs.next(this.convertToLogTraces(data));
            }
        );
        return logs;
    }



    // Metrics

    searchAllMetrics(index: string, metricsField: MetricsFieldModel, theQuery?: any) {
        let _metrics: Subject<LineChartMetricModel[]> = new Subject<LineChartMetricModel[]>();
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
        let _metrics: Subject<LineChartMetricModel[]> = new Subject<LineChartMetricModel[]>();
        let metrics = _metrics.asObservable();

        if (trace !== undefined) {

            let terms: any[] = this.getTermsByMetricsField(metricsField);
            this.elasticsearchService.getPrevFromTimestamp(index, trace.timestamp, terms).subscribe(
                (data) => {
                    _metrics.next(this.convertToMetricTraces(data, metricsField));
                }
            );
        } else {
            _metrics.next([]);
            this.popupService.openSnackBar('There isn\'t reference traces yet to load previous', 'OK');
        }

        return metrics;
    }

    convertToMetricTraces(data: any[], metricsField: MetricsFieldModel) {
        let tracesList: LineChartMetricModel[];
        if (metricsField.component === undefined || metricsField.component === '') {
            tracesList = this.getInitMetricsData();

            let position: number = undefined;
            let parsedMetric: any;
            for (let metricTrace of data) {
                parsedMetric = this.convertToMetricTrace(metricTrace._source, metricsField);
                position = this.getMetricPosition(metricTrace._source.component);
                if (position !== undefined && parsedMetric !== undefined) {
                    tracesList[position].series.push(parsedMetric);
                }
            }
        } else {
            tracesList = this.getInitMetricsDataComplex(metricsField);
            let parsedMetric: any;
            let position: number = undefined;
            for (let metricTrace of data) {
                parsedMetric = this.convertToMetricTrace(metricTrace._source, metricsField);
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
        } else if (trace.type === metricsField.type) {
            parsedData = this.convertToGenericMetricsData(trace, metricsField);
        }
        return parsedData;
    }

    convertToGenericMetricsData(trace: any, metricsField: MetricsFieldModel) {
        let parsedData: SingleMetricModel = undefined;
        if (trace['@timestamp'] !== '0001-01-01T00:00:00.000Z' && trace[trace.type]) {
            parsedData = this.getBasicSingleMetric(trace);
            if (metricsField.streamType === 'atomic_metric') {
                parsedData.value = trace[trace.type];
            } else {
                parsedData.value = trace[trace.type][metricsField.subtype];
            }
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
                    // parsedData = this.convertToGenericMetricsData(trace, metricsField);
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

    getMetricPosition(component: string) {
        let position: number = undefined;

        component = component.toLowerCase();
        component = component.charAt(0).toUpperCase() + component.slice(1);

        if (MetricsDataType[component] !== undefined) {
            position = MetricsDataType[component];
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



    getLastMetricTraces(index: string, metricsField: MetricsFieldModel, size: number = 10) {
        let _metrics: Subject<LineChartMetricModel[]> = new Subject<LineChartMetricModel[]>();
        let metrics = _metrics.asObservable();

        let terms: any[] = this.getTermsByMetricsField(metricsField);

        this.elasticsearchService.getLast(index, terms, size).subscribe(
            (data) => {
                _metrics.next(this.convertToMetricTraces(data, metricsField));
            }
        );
        return metrics;
    }



    initTestLog(log: ESRabLogModel) {
        log.name = 'Test Logs';
        log.type = 'testlogs';
        log.stream = 'custom_log';
        log.component = 'test';
    }

    initSutLog(log: ESRabLogModel) {
        log.name = 'SuT Logs';
        log.type = 'sutlogs';
        log.stream = 'custom_log';
        log.component = 'sut';
    }


    // Dynamic
    getDynamicTerms(stream: string, component: string) {
        let terms: any[];
        terms = [
            { 'term': { stream: stream } },
            { 'term': { component: component } },
        ];
        return terms;
    }

    searchAllDynamic(index: string, stream: string, component: string, metricName?: string, theQuery?: any) {
        let _obs: Subject<any> = new Subject<any>();
        let obs = _obs.asObservable();

        let terms: any[] = this.getDynamicTerms(stream, component);
        let filters: string[] = this.getBasicFilterFields().concat(
            ['message', 'units', 'unit']
        );
        if (metricName && metricName !== '') {
            filters.push(metricName);
            terms.push(
                { 'term': { type: metricName.split('.')[0] } },
            );
        }

        this.elasticsearchService.searchAllByTerm(index, terms, theQuery, filters)
            .subscribe(
            (data: any[]) => {
                if (data.length > 0) {
                    let convertedData: any;
                    let firstElement: any = data[0];
                    let firstSource: any = firstElement._source;
                    let streamType: string = '';
                    let type: string = firstSource.type;
                    let obj: any = {
                        streamType: streamType,
                        type: type,
                        data: convertedData,
                        component: component,
                        stream: stream,
                        logIndex: index,
                    };

                    if (this.isLogTrace(firstElement)) {
                        this.addDynamicLog(_obs, obj, data);
                    } else if (this.isMetricsTrace(firstElement)) {
                        this.addDynamicComposedMetrics(_obs, obj, data);
                    } else if (this.isAtomicMetricTrace(firstElement)) {
                        this.addDynamicAtomicMetric(_obs, obj, data);
                    } else {
                        this.popupService.openSnackBar('Cannot add the traces obtained with the parameters provided', 'OK');
                    }
                } else {
                    this.popupService.openSnackBar('Nothing found', 'OK');
                }
            });

        return obs;
    }

    addDynamicLog(_obs: Subject<any>, obj: any, data: any[]) {
        let logTraces: string[] = this.convertToLogTraces(data);

        obj.data = logTraces;
        obj.streamType = 'log';
        _obs.next(obj);
    }

    addDynamicComposedMetrics(_obs: Subject<any>, obj: any, data: any[]) {
        let firstElement: any = data[0];
        let firstSource: any = firstElement._source;
        let metricObj: any = firstSource[firstSource.type];
        for (let metricName in metricObj) {
            this.addDynamicMetric(_obs, obj, data, firstSource, metricName, 'composed_metrics');
        }
    }

    addDynamicAtomicMetric(_obs: Subject<any>, obj: any, data: any[]) {
        let firstElement: any = data[0];
        let firstSource: any = firstElement._source;
        let metricName: string = firstSource.type;
        this.addDynamicMetric(_obs, obj, data, firstSource, metricName, 'atomic_metric');
    }

    addDynamicMetric(_obs: Subject<any>, obj: any, data: any[], firstSource: any, metricName: string, streamType: string) {
        let unit: string;
        if (firstSource.units && firstSource.units[metricName]) {
            unit = firstSource.units[metricName];
        } else if (firstSource.unit) {
            unit = firstSource.unit;
        } else {
            unit = this.allMetricsFields.getDefaultUnitBySubtype(metricName);
        }

        let metricsField: MetricsFieldModel
            = new MetricsFieldModel(firstSource.type, metricName, unit, obj.component, obj.stream, streamType, true);
        let metricsTraces: LineChartMetricModel[] = this.convertToMetricTraces(data, metricsField);

        obj.data = metricsTraces;
        obj.streamType = streamType;
        obj['metricFieldModel'] = metricsField;

        obj.unit = unit;
        if (metricsTraces[0].series.length > 0) { // If chart is not empty, add it
            _obs.next(obj);
        }
    }

    isLogTrace(trace: any) {
        return trace._source['stream_type'] !== undefined && trace._source['stream_type'] !== null && trace._source['stream_type'] === 'log';
    }

    isMetricsTrace(trace: any) {
        return trace._source['stream_type'] !== undefined && trace._source['stream_type'] !== null && trace._source['stream_type'] === 'composed_metrics';
    }

    isAtomicMetricTrace(trace: any) {
        return trace._source['stream_type'] !== undefined && trace._source['stream_type'] !== null && trace._source['stream_type'] === 'atomic_metric';
    }

    getBasicFilterFields(streamType?): string[] {
        let filters: string[] = ['@timestamp', 'type', 'component', 'stream', 'stream_type', 'exec'];

        if (streamType && streamType === 'log') {
            filters.push('message', 'level');
        }

        return filters;
    }

    // TODO Refactor:

    getAggTreeList(agg: any, fields: string[]): any[] {
        let aggTreeList: any[] = [];
        if (fields.length > 0) {
            let field: string = fields[0] + 's';
            if (agg[field] && agg[field].buckets) {
                let buckets: any[] = agg[field].buckets;
                for (let bucket of buckets) {
                    let aggObj: any = {};
                    aggObj.name = bucket.key;
                    aggObj.children = this.getAggTreeList(bucket, fields.slice(1));
                    aggTreeList.push(aggObj);
                }
            }
        }
        return aggTreeList;
    }


    getAggTreeOfIndex(index: string, fieldsList: string[], query?: any): Observable<any[]> {
        let _aggTreeSub: Subject<any[]> = new Subject<any[]>();
        let aggTreeObs: Observable<any[]> = _aggTreeSub.asObservable();

        let url: string = this.esUrl + index + '/_search?ignore_unavailable';
        let aggsModel: ESAggsModel = new ESAggsModel();
        aggsModel.initNestedByFieldsList(fieldsList);

        let aggsObj: any = aggsModel.convertToESFormat();
        aggsObj.size = 0;
        if (query) {
            aggsObj.query = query;
        }

        this.elasticsearchService.internalSearch(url, aggsObj)
            .subscribe(
            (data: any) => {
                if (data.aggregations) {
                    let aggTreeList: any[] = this.getAggTreeList(data.aggregations, fieldsList);
                    _aggTreeSub.next(aggTreeList);
                } else {
                    _aggTreeSub.next([]);
                }

            },
            (error) => console.log(error),
        );

        return aggTreeObs;
    }
}