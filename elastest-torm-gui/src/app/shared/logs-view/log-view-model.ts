export interface LogViewModel {
    name: string;
    prevTraces: string[];
    traces: string[];
    prevTracesLoaded: boolean;
    hidePrevBtn: boolean;
    logType: string;
    logIndex: string;
    getAllLogsByType();
    loadPreviousLogs();
}
