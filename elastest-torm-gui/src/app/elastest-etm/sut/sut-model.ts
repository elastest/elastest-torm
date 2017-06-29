import { ProjectModel } from '../project/project-model';

export class SutModel {
    id: number;
    name: string;
    specification: string;
    description: string;
    project: ProjectModel;

    constructor() {
        this.id = 0;
        this.name = '';
        this.specification = '';
        this.description = '';
        this.project = undefined;
    }
}
