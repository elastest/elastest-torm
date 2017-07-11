import { ElasticSearchService } from './elasticsearch.service';

import { Injectable } from '@angular/core';

@Injectable()
export class ElastestESService {
    constructor(private elasticsearchService: ElasticSearchService) {

    }
}