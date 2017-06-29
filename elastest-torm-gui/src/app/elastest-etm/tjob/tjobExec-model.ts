import { ProjectModel } from '../project/project-model';
import { SutModel } from '../sut/sut-model';
import { SutExecModel } from '../sut/sutExec-model';
import { TJobModel } from './tjob-model';

export class TJobExecModel {
    id: number;
    duration: number;
    error: string;
    result: number;
    sutExec: SutExecModel;
    tJob: TJobModel;
    // toJobExec: TOJobExecModel:

    constructor() {
        this.id = 0;
        this.duration = 0;
        this.error = undefined;
        this.result = 0;
        this.sutExec = undefined;
        this.tJob = undefined;
    }
}
