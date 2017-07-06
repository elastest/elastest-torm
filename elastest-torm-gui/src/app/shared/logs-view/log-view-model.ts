export class LogViewModel {
    name: string;
    prevTraces: string[];
    traces: string[];
    prevTracesLoaded: boolean;
    hidePrevBtn: boolean;
    logType: string;
    logUrl: string;

    constructor() {
        this.name = '';
        this.prevTraces = [];
        this.traces = [];
        this.prevTracesLoaded = false;
        this.hidePrevBtn = false;
        this.logType = '';
        this.logUrl = '';
    }
}
