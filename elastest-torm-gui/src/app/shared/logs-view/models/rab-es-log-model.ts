import { Observable } from 'rxjs/Rx';

import { ElasticSearchService } from '../../services/elasticsearch.service';
import { ESLogModel } from './elasticsearch-log-model';

export class RabESLogModel extends ESLogModel {

    constructor(elasticsearchService: ElasticSearchService) {
        super(elasticsearchService);
    }

    getAllLogsByType() {
        super.getAllLogsByType();
    }

    loadPrevious() {
        if (this.traces[0] !== undefined && this.traces[0] !== null) {
            return this.elasticsearchService.getFromGivenMessage(this.logIndex, this.traces[0], this.type);
        }
        else {
            return Observable.throw(new Error('There isn\'t reference log messages yet to load previous'));
        }
    }
}
