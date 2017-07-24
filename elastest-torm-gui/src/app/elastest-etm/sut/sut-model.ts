import { ProjectModel } from '../project/project-model';

export class SutModel {
    id: number;
    name: string;
    specification: string;
    imageName: string;
    description: string;
    project: ProjectModel;

    constructor() {
        this.id = 0;
        this.name = '';
        this.specification = '';
        this.imageName = '';
        this.description = '';
        this.project = undefined;
    }
}
