import { LoadPreviousModel } from '../load-previous-view/load-previous-model';

export interface LogViewModel extends LoadPreviousModel {
    name: string;
    traces: any[];
    filteredTraces: any[];
    prevTraces: any[];
    prevLoaded: boolean;
    hidePrevBtn: boolean;
    etType: string;
    monitoringIndex: string;
    getAllLogs();
    loadPrevious();
}
