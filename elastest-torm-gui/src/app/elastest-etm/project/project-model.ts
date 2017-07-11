import { SutModel } from '../sut/sut-model';
import { TJobModel } from '../tjob/tjob-model';

export class ProjectModel {
    id: number;
    name: string;
    suts: SutModel[];
    tjobs: TJobModel[];
    constructor(){
        this.id = 0;
        this.name = '';
        this.suts = [];
        this.tjobs = [];
    }
}
