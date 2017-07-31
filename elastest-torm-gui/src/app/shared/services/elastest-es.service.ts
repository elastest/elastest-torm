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
        private popupService: PopupService,
    ) { }

    getTermsByTypeAndComponentType(type: string, componentType: string) {
        return [
            { 'term': { _type: type } },
            { 'term': { component_type: componentType } },
        ];
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



    //Metrics

    searchAllMetrics(index: string, type: string, theQuery?: any) {
        let _metrics = new Subject<LineChartMetricModel[]>();
        let metrics = _metrics.asObservable();

        let terms: any[] = [{ 'term': { _type: type } }];
        this.elasticsearchService.searchAllByTerm(index, terms, theQuery).subscribe(
            (data) => {
                _metrics.next(this.convertToMetricTraces(data, type));
            }
        );

        return metrics;
    }

    getPrevMetricsFromTrace(index: string, trace: any, type: string) {
        let _metrics = new Subject<LineChartMetricModel[]>();
        let metrics = _metrics.asObservable();

        if (trace !== undefined) {
            let terms: any[] = [{ 'term': { _type: type } }];
            this.elasticsearchService.getPrevFromTimestamp(index, trace.timestamp, terms).subscribe(
                (data) => {
                    _metrics.next(this.convertToMetricTraces(data, type));
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
            _metrics.next([]);
            this.popupService.openSnackBar('There isn\'t reference traces yet to load previous', 'OK');
        }

        return metrics;
    }


    convertToMetricTraces(data: any[], type: string) {
        let tracesList: LineChartMetricModel[] = this.getInitMetricsData();

        let position: number = undefined;
        let parsedMetric: any;
        for (let logEntry of data) {
            parsedMetric = this.convertToMetricTrace(logEntry._source, type);
            position = this.getMetricPosition(logEntry._source.component_type);
            if (position !== undefined && parsedMetric !== undefined) {
                tracesList[position].series.push(parsedMetric);
            }
        }

        return tracesList;
    }

    convertToMetricTrace(trace: any, type: string) {
        let parsedData: any = undefined;
        if (trace.type === 'cpu' && type === 'cpu') {
            parsedData = this.convertToCpuData(trace);

        } else if (trace.type === 'memory' && type === 'memory') {
            parsedData = this.convertToMemoryData(trace);
        }
        return parsedData;
    }

    convertToCpuData(trace: any) {
        let parsedData: SingleMetricModel = undefined;
        if (trace.cpu.totalUsage !== 0 && trace['@timestamp'] !== '0001-01-01T00:00:00.000Z') {
            parsedData.value = trace.cpu.totalUsage * 100;
            parsedData.name = new Date('' + trace['@timestamp']);
            parsedData.timestamp = trace['@timestamp'];
        }
        return parsedData;
    }


    convertToMemoryData(trace: any) {
        let perMemoryUsage = trace.memory.usage * 100 / trace.memory.limit;
        let parsedData: SingleMetricModel = undefined;
        parsedData.value = perMemoryUsage;
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