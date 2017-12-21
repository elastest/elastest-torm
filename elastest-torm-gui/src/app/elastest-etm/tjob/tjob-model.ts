import { EsmServiceModel } from '../../elastest-esm/esm-service.model';
import { selectContext } from 'webdriver-js-extender/built/lib/command_definitions';
import { DashboardConfigModel } from './dashboard-config-model';
import { AllMetricsFields } from '../../shared/metrics-view/metrics-chart-card/models/all-metrics-fields-model';
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
    commands: string;
    resultsPath: string;
    execDashboardConfig: string;
    execDashboardConfigModel: DashboardConfigModel;
    esmServicesString: string;
    esmServices: EsmServiceModel[];
    esmServicesChecked: number;

    constructor() {
        this.id = 0;
        this.name = '';
        this.imageName = '';
        this.sut = undefined;
        this.project = undefined;
        this.tjobExecs = [];
        this.parameters = [];
        this.commands = '';
        this.resultsPath = '';
        this.execDashboardConfig = '';
        this.execDashboardConfigModel = new DashboardConfigModel();
        this.esmServicesString = '';
        this.esmServices = [];
        this.esmServicesChecked = 0;
    }

    public generateExecDashboardConfig(): void {
        this.execDashboardConfig = JSON.stringify(this.execDashboardConfigModel);
    }

    public hasSut(): boolean {
        return (this.sut !== undefined && this.sut !== null && this.sut.id !== 0);
    }

    public cloneTJob(): TJobModel {
        let tJob: TJobModel = Object.assign({}, this, {
            parameters: [...this.parameters],
            tjobExecs: [...this.tjobExecs],
        },
        );
        return tJob;
    }

    public withCommands() {
        return this.commands !== undefined && this.commands !== null && this.commands !== '';
    }

    public arrayCommands(): string[] {
        let commandsArray: string[] = this.commands.split(';').filter((x: string) => x); // ignore empty strings
        return commandsArray;
    }

    public changeServiceSelection($event, i: number): void {
        console.log('Service id:' + i);
        this.esmServices[i].selected = $event.checked;
    }

    public getRouteString(): string {
        return this.project.getRouteString() + ' / ' + this.name;
    }

    hasParameters(): boolean {
        return this.parameters.length > 0 || this.sut.parameters.length > 0;
    }
}
