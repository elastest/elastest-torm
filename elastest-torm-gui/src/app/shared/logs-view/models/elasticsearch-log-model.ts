import { ElasticSearchService } from '../../services/elasticsearch.service';
import { LogViewModel } from '../log-view-model';

export class ESLogModel implements LogViewModel {
    elasticsearchService: ElasticSearchService;

    name: string;
    prevTraces: string[];
    traces: string[];
    prevTracesLoaded: boolean;
    hidePrevBtn: boolean;
    logType: string;
    logUrl: string;

    constructor(elasticsearchService: ElasticSearchService) {
        this.name = '';
        this.prevTraces = [];
        this.traces = [];
        this.prevTracesLoaded = false;
        this.hidePrevBtn = false;
        this.logType = '';
        this.logUrl = '';

        this.elasticsearchService = elasticsearchService;
    }

    getAllLogsByType() {
        this.elasticsearchService.searchLogsByType(this.logUrl, this.logType)
            .subscribe(
            (data) => {
                this.traces = data;
            }
            );
    }

    loadPreviousLogs() { }
}
