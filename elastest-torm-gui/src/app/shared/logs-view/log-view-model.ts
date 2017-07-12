import { LoadPreviousModel } from '../load-previous-view/load-previous-model';

export interface LogViewModel extends LoadPreviousModel {
    name: string;
    traces: any[];
    prevTraces: any[];
    prevLoaded: boolean;
    hidePrevBtn: boolean;
    type: string;
    logIndex: string;
    getAllLogs();
    loadPrevious();
}
