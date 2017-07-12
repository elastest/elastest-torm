import { ElastestESService } from '../../services/elastest-es.service';
import { LogViewModel } from '../log-view-model';

export class ESLogModel implements LogViewModel {
    elastestESService: ElastestESService;

    name: string;
    prevTraces: string[];
    traces: string[];
    prevLoaded: boolean;
    hidePrevBtn: boolean;
    type: string;
    componentType: string;
    logIndex: string;

    constructor(elastestESService: ElastestESService) {
        this.name = '';
        this.prevTraces = [];
        this.traces = [];
        this.prevLoaded = false;
        this.hidePrevBtn = false;
        this.type = '';
        this.componentType = '';
        this.logIndex = '';

        this.elastestESService = elastestESService;
    }

    getAllLogsByType() {
        this.elastestESService.searchAllLogs(this.logIndex, this.type, this.componentType)
            .subscribe(
            (data) => {
                this.traces = data;
            }
            );
    }

    loadPrevious() {
        return this.elastestESService.getPrevLogsFromMessage(this.logIndex, this.traces[0], this.type, this.componentType);
    }
}
