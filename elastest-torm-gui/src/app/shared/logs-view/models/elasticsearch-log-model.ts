import { ElasticSearchService } from '../../services/elasticsearch.service';
import { LogViewModel } from '../log-view-model';

export class ESLogModel implements LogViewModel {
    elasticsearchService: ElasticSearchService;

    name: string;
    prevTraces: string[];
    traces: string[];
    prevLoaded: boolean;
    hidePrevBtn: boolean;
    type: string;
    componentType: string;
    logIndex: string;

    constructor(elasticsearchService: ElasticSearchService) {
        this.name = '';
        this.prevTraces = [];
        this.traces = [];
        this.prevLoaded = false;
        this.hidePrevBtn = false;
        this.type = '';
        this.componentType = '';
        this.logIndex = '';

        this.elasticsearchService = elasticsearchService;
    }

    getAllLogsByType() {
        this.elasticsearchService.searchLogsByType(this.logIndex, this.type)
            .subscribe(
            (data) => {
                this.traces = data;
            }
            );
    }

    loadPrevious() { }
}
