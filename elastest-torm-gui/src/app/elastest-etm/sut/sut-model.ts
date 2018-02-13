import { EimConfigModel } from './eim-config-model';
import { ProjectModel } from '../project/project-model';
import { ExternalProjectModel } from '../external/external-project/external-project-model';

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
  commands: string;
  sutInNewContainer: boolean = false;

  exProject: ExternalProjectModel;

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
    this.commands = '';
    this.sutInNewContainer = false;

    this.exProject = undefined;
  }

  public getRouteString(): string {
    let routeStr: string = ' / SuT / ' + this.name;
    if (this.project) {
      routeStr = this.project.getRouteString() + routeStr;
    } else if (this.exProject) {
      routeStr = this.exProject.getRouteString() + routeStr;
    }
    return routeStr;
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

  public withCommands() {
    return this.commands !== undefined && this.commands !== null && this.commands !== '';
  }

  public arrayCommands(): string[] {
    let commandsArray: string[] = this.commands.split(';').filter((x: string) => x); // ignore empty strings
    return commandsArray;
  }

  changeSutInNewContainerValue($event): void {
    this.sutInNewContainer = $event.checked;
  }
}
