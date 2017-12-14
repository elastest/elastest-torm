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
    instrumentedBy: string;
    port: string;
    managedDockerType: string;
    mainService: string;
    parameters: any[];

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
        this.instrumentedBy = '';
        this.port = undefined;
        this.managedDockerType = '';
        this.mainService = '';
        this.parameters = [];
    }

    public getRouteString(): string {
        return this.project.getRouteString() + ' / SuT / ' + this.name;
    }

    public isManaged(): boolean {
        return this.sutType === 'MANAGED';
    }

    public isDeployed(): boolean {
        return this.sutType === 'DEPLOYED';
    }

    public isByDockerCompose(): boolean {
        return this.isManaged && this.managedDockerType === 'COMPOSE';
    }

    public getSutESIndex(): string {
        return 's' + this.id + '_e' + this.currentSutExec;
    }
}
