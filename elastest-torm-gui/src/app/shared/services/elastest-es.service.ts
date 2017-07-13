import { ESLogModel } from '../logs-view/models/elasticsearch-log-model';
import { SingleMetricModel } from '../metrics-view/models/single-metric-model';
import { ElasticSearchService } from './elasticsearch.service';

import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs/Rx';
import { MdSnackBar } from '@angular/material';

@Injectable()
export class ElastestESService {
    constructor(
        private elasticsearchService: ElasticSearchService,
        private snackBar: MdSnackBar,
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

    getPrevLogsFromTrace(index: string, trace: any, type: string, componentType: string) {
        let _logs = new Subject<string[]>();
        let logs = _logs.asObservable();

        let terms: any[] = this.getTermsByTypeAndComponentType(type, componentType);
        this.elasticsearchService.getPrevFromTimestamp(index, trace.timestamp, terms).subscribe(
            (data) => {
                _logs.next(this.convertToLogTraces(data));
                if (data.length > 0) {
                    this.openSnackBar('Previous traces has been loaded', 'OK');
                }
                else {
                    this.openSnackBar('There aren\'t previous traces to load', 'OK');
                }
            }
        );
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
        let _metrics = new Subject<SingleMetricModel[]>();
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
        let _metrics = new Subject<SingleMetricModel[]>();
        let metrics = _metrics.asObservable();

        let terms: any[] = [{ 'term': { _type: type } }];
        this.elasticsearchService.getPrevFromTimestamp(index, trace.timestamp, terms).subscribe(
            (data) => {
                _metrics.next(this.convertToMetricTraces(data, type));
                if (data.length > 0) {
                    this.openSnackBar('Previous traces has been loaded', 'OK');
                }
                else {
                    this.openSnackBar('There aren\'t previous traces to load', 'OK');
                }
            }
        );

        return metrics;
    }


    convertToMetricTraces(data: any[], type: string) {
        let test: SingleMetricModel = new SingleMetricModel();
        test.name = 'Test';
        let sut: SingleMetricModel = new SingleMetricModel();
        sut.name = 'Sut';

        let tracesList: SingleMetricModel[] = [];
        tracesList.push(test);
        tracesList.push(sut);

        let position: number = undefined;
        let parsedMetric: any;
        for (let logEntry of data) {
            parsedMetric = this.convertToMetricTrace(logEntry._source, type);
            position = this.getMetricPosition(logEntry._source.component_type);
            if (position !== undefined) {
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
        let parsedData: any = {
            'value': trace.cpu.totalUsage,
            'name': new Date('' + trace['@timestamp']),
            'timestamp': trace['@timestamp'],
        };
        return parsedData;
    }


    convertToMemoryData(trace: any) {
        let perMemoryUsage = trace.memory.usage * 100 / trace.memory.limit;
        let parsedData: any = {
            'value': perMemoryUsage,
            'name': new Date('' + trace['@timestamp']),
            'timestamp': trace['@timestamp'],
        };
        return parsedData;
    }

    getMetricPosition(componentType: string) {
        let position: number = undefined;
        if (componentType === 'test') {
            position = 0;
        }
        else if (componentType === 'sut') {
            position = 1;
        }

        return position;
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

    openSnackBar(message: string, action: string) {
        this.snackBar.open(message, action, {
            duration: 3500,
        });
    }
}