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
    resultMsg: string;

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
        this.resultMsg = '';
    }

    public hasSutExec(): boolean {
        return (this.sutExec !== undefined && this.sutExec !== null && this.sutExec.id !== 0);
    }

    getTJobIndex(): string {
        let testIndex: string = this.logIndex.split(',')[0];
        return testIndex;
    }

    getSutIndex(): string {
        let sutIndex: string = '';
        if (this.tJob.hasSut()) {
            sutIndex = this.logIndex.split(',')[1];
            if (!sutIndex) {
                sutIndex = this.getTJobIndex();
            }
        }
        return sutIndex;
    }

    getCurrentESIndex(component: string) {
        let index: string = this.getTJobIndex();
        if (component === 'sut') {
            index = this.getSutIndex();
        }
        return index;
    }

    finished(): boolean {
        return this.result === 'FINISHED' || this.result === 'FAILURE';
    }

    starting(): boolean {
        return this.result === 'IN PROGRESS' || this.result === 'STARTING TSS' || this.result === 'WAITING TSS';
    }


    public getRouteString(): string {
        return this.tJob.getRouteString() + ' / Exec / ' + this.id;
    }
}
