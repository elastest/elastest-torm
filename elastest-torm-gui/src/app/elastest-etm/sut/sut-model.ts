import { EimConfigModel } from './eim-config-model';
import { ProjectModel } from '../project/project-model';

export class SutModel {
    id: number;
    name: string;
    specification: string;
    sutType: string;
    description: string;
    project: ProjectModel;
    eimConfig: EimConfigModel;
    instrumentalize: boolean;
    currentSutExec: string;

    constructor() {
        this.id = 0;
        this.name = '';
        this.specification = '';
        this.sutType = '';
        this.description = '';
        this.project = undefined;
        this.eimConfig = new EimConfigModel();
        this.instrumentalize = false;
        this.currentSutExec = undefined;
    }
}
