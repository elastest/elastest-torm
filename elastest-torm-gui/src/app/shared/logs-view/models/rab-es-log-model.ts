import { Observable } from 'rxjs/Rx';

import { ElasticSearchService } from '../../services/elasticsearch.service';
import { ESLogModel } from './elasticsearch-log-model';

export class RabESLogModel extends ESLogModel {

    constructor(elasticsearchService: ElasticSearchService) {
        super(elasticsearchService);
    }

    getAllLogsByType() {
        this.elasticsearchService.searchLogsByType(this.logUrl, this.logType)
            .subscribe(
            (data) => {
                this.traces = data;
            }
            );
    }

    loadPreviousLogs() {
        if (this.traces[0] !== undefined && this.traces[0] !== null) {
            return this.elasticsearchService.getFromGivenLog(this.logUrl, this.traces[0], this.logType);
        }
        else{
            return Observable.throw(new Error('There isn\'t reference log messages yet to load previous'));
        }
    }
}
