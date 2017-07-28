import { ProjectModel } from '../project/project-model';
import { SutModel } from '../sut/sut-model';
import { SutExecModel } from '../sut-exec/sutExec-model';
import { TJobModel } from '../tjob/tjob-model';

export class TJobExecModel {
    id: number;
    duration: number;
    error: string;
    result: string;
    sutExec: SutExecModel;
    logIndex: string;
    tJob: TJobModel;
    testSuite: any;
    parameters: any[];
    // toJobExec: TOJobExecModel:

    constructor() {
        this.id = 0;
        this.duration = 0;
        this.error = undefined;
        this.result = '';
        this.sutExec = undefined;
        this.logIndex = '';
        this.tJob = undefined;
        this.testSuite = undefined;
        this.parameters = [];
    }

    public hasSutExec(): boolean {
        return (this.sutExec !== undefined && this.sutExec !== null && this.sutExec.id !== 0);
    }
}
