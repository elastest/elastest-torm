import { ProjectModel } from '../project/project-model';

export class SutModel {
    id: number;
    name: string;
    specification: string;
    description: string;

    constructor() {
        this.id = 0;
        this.name = '';
        this.specification = '';
        this.description = '';
    }
}
