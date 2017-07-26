import { ParameterModel } from '../parameter/parameter-model';
import { ProjectModel } from '../project/project-model';
import { SutModel } from '../sut/sut-model';
import { TJobExecModel } from '../tjob-exec/tjobExec-model';

export class TJobModel {
    id: number;
    name: string;
    imageName: string;
    sut: SutModel;
    project: ProjectModel;
    tjobExecs: TJobExecModel[];
    parameters: any[];

    constructor() {
        this.id = 0;
        this.name = '';
        this.imageName = '';
        this.sut = undefined;
        this.project = undefined;
        this.tjobExecs = [];
        this.parameters = [];
    }

    public hasSut(): boolean {
        return (this.sut !== undefined && this.sut !== null && this.sut.id !== 0);
    }
}
