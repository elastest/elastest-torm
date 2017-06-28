import { ProjectModel } from '../project/project-model';
import { SutModel } from '../sut/sut-model';

export class TJobModel {
    id: number;
    name: string;
    imageName: string;
    sut: SutModel;

    constructor() {
        this.id = 0;
        this.name = '';
        this.imageName = '';
        this.sut = undefined;
    }
}
