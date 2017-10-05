export class TestEngineModel {
    name: string;
    started: boolean;
    ready: boolean;
    url: string;

    constructor() {
        this.name = '';
        this.started = false;
        this.ready = false;
        this.url = '';
    }
}