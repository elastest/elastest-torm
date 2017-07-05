export class LogViewModel {
    name: string;
    prevTraces: string[];
    traces: string[];
    prevTracesLoaded: boolean;
    hidePrevBtn: boolean;

    constructor() {
        this.name = '';
        this.prevTraces = [];
        this.traces = [];
        this.prevTracesLoaded = false;
        this.hidePrevBtn = false;
    }
}
