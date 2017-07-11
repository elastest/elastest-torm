import { ESLogModel } from '../logs-view/models/elasticsearch-log-model';
import { ElasticSearchService } from './elasticsearch.service';

import { Injectable } from '@angular/core';

@Injectable()
export class ElastestESService {
    constructor(
        private elasticsearchService: ElasticSearchService
    ) { }

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