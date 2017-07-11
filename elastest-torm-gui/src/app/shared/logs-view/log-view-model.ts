import { LoadPreviousModel } from '../load-previous-view/load-previous-model';

export interface LogViewModel extends LoadPreviousModel {
    name: string;
    traces: string[];
    prevTraces: string[];
    prevLoaded: boolean;
    hidePrevBtn: boolean;
    type: string;
    logIndex: string;
    getAllLogsByType();
    loadPrevious();
}
