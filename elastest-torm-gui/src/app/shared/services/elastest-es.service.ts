import { ESLogModel } from '../logs-view/models/elasticsearch-log-model';
import { ElasticSearchService } from './elasticsearch.service';

import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';

@Injectable()
export class ElastestESService {
    constructor(
        private elasticsearchService: ElasticSearchService,
    ) { }

    getTermsByTypeAndComponentType(type: string, componentType: string) {
        return [
            { 'term': { _type: type } },
            { 'term': { component_type: componentType } },
        ];
    }

    searchAllLogs(index: string, type: string, componentType: string, theQuery?: any) {
        let terms: any[] = this.getTermsByTypeAndComponentType(type, componentType);
        if (theQuery !== undefined) {
            return this.elasticsearchService.searchAllByTerm(index, terms, theQuery);
        }
        else {
            return this.elasticsearchService.searchAllByTerm(index, terms);
        }
    }

    getPrevLogsFromMessage(index: string, fromMessage: string, type: string, componentType: string) {
        let terms: any[] = this.getTermsByTypeAndComponentType(type, componentType);
        if (fromMessage !== undefined && fromMessage !== null) {
            return this.elasticsearchService.getPrevFromGivenMessage(index, fromMessage, terms);
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