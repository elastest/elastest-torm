import { ExternalTJobModel } from '../external-tjob/external-tjob-model';

export class ExternalTJobExecModel {
    id: number;
    esIndex: string;
    externalTJob: ExternalTJobModel;

    constructor() {
        this.id = 0;
        this.esIndex = '';
        this.externalTJob = undefined;
    }
}