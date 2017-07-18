import { PopupService } from '../../services/popup.service';
import { ElastestESService } from '../../services/elastest-es.service';
import { LogViewModel } from '../log-view-model';

export class ESLogModel implements LogViewModel {
    elastestESService: ElastestESService;

    name: string;
    traces: any[];
    prevTraces: any[];
    prevLoaded: boolean;
    hidePrevBtn: boolean;
    type: string;
    componentType: string;
    logIndex: string;

    constructor(elastestESService: ElastestESService,
        private popupService: PopupService,
    ) {
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

    getAllLogs() {
        this.elastestESService.searchAllLogs(this.logIndex, this.type, this.componentType)
            .subscribe(
            (data) => {
                this.traces = data;
            }
            );
    }

    loadPrevious() {
        if (this.traces.length > 0) {
            this.elastestESService.getPrevLogsFromTrace(this.logIndex, this.traces[0], this.type, this.componentType)
                .subscribe(
                (data) => {
                    this.prevTraces = data;
                    this.prevLoaded = true;
                },
            );
        }
        else {
            this.popupService.openSnackBar('There isn\'t reference traces yet to load previous', 'OK');
        }
    }
}
