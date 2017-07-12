import { ESLogModel } from '../logs-view/models/elasticsearch-log-model';
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
        if (trace !== undefined && trace !== null) {
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
        else {
            this.openSnackBar('There isn\'t reference traces yet to load previous', 'OK');
            return Observable.throw(new Error('There isn\'t reference log messages yet to load previous'));
        }
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