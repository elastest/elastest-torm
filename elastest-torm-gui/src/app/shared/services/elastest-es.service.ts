import { ESLogModel } from '../logs-view/models/elasticsearch-log-model';
import { ElasticSearchService } from './elasticsearch.service';

import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';

@Injectable()
export class ElastestESService {
    constructor(
        private elasticsearchService: ElasticSearchService,
    ) { }

    searchLogsByType(index: string, type: string, theQuery?: any) {
        if (theQuery !== undefined) {
            return this.elasticsearchService.searchLogsByType(index, type, theQuery);
        }
        else {
            return this.elasticsearchService.searchLogsByType(index, type);
        }
    }

    getPrevLogsFromMessage(index: string, fromMessage: string, type: string) {
        if (fromMessage !== undefined && fromMessage !== null) {
            return this.elasticsearchService.getPrevFromGivenMessage(index, fromMessage, type);
        }
        else {
            return Observable.throw(new Error('There isn\'t reference log messages yet to load previous'));
        }
    }

    loadPreviousTrace(fromMessage: string, type: string, componentType: string) {

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