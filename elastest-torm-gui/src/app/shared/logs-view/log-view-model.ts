export interface LogViewModel {
    name: string;
    prevTraces: string[];
    traces: string[];
    prevTracesLoaded: boolean;
    hidePrevBtn: boolean;
    logType: string;
    logUrl: string;
    getAllLogsByType();
    loadPreviousLogs();
}
