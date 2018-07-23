import { SutModel } from '../sut/sut-model';

export class SutExecModel {
    id: number;
    deplotStatus: number;
    url: string;
    sut: SutModel;
    parameters: any[];

    constructor() {
        this.id = 0;
        this.deplotStatus = 0;
        this.url = '';
        this.sut = undefined;
        this.parameters = [];
    }
}
