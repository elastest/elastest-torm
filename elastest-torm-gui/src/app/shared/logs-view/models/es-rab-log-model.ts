import { ElastestESService } from '../../services/elastest-es.service';
import { LogViewModel } from '../log-view-model';

export class ESRabLogModel implements LogViewModel {
    elastestESService: ElastestESService;

    name: string;
    traces: any[];
    filteredTraces: any[];
    prevTraces: any[];
    prevLoaded: boolean;
    hidePrevBtn: boolean;
    type: string;
    componentType: string;
    logIndex: string;

    constructor(elastestESService: ElastestESService, ) {
        this.name = '';
        this.prevTraces = [];
        this.traces = [];
        this.filteredTraces = [];
        this.prevLoaded = false;
        this.hidePrevBtn = false;
        this.type = '';
        this.componentType = '';
        this.logIndex = '';

        this.elastestESService = elastestESService;
    }

    getAllLogs() {
        this.elastestESService.searchAllLogs(this.logIndex, this.type, this.componentType)
            .subscribe(
            (data) => {
                this.traces = data;
            }
            );
    }

    loadPrevious() {
        this.elastestESService.getPrevLogsFromTrace(this.logIndex, this.traces, this.type, this.componentType)
            .subscribe(
            (data) => {
                if (data.length > 0) {
                    this.prevTraces = data;
                    this.prevLoaded = true;
                }
            },
        );
    }

    selectTimeRange(domain) {
        let counter: number = 0;
        for (let trace of this.traces) {
            if (trace.timestamp >= domain[0] && trace.timestamp <= domain[1]) {
                this.filteredTraces.push(trace);
                counter++;
            }
        }

        if (counter = 0) {
            this.filteredTraces.push({ 'message': '' })
        }
    }

    clearFilter() {
        this.filteredTraces = [];
    }
}
