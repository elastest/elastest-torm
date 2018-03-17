import { EimConfigModel } from './eim-config-model';
import { ProjectModel } from '../project/project-model';
import { ExternalProjectModel } from '../external/external-project/external-project-model';
import { EimMonitoringConfigModel } from './eim-monitoring-config.model';
import { defaultStreamMap } from '../../shared/defaultESData-model';

export class SutModel {
  id: number;
  name: string;
  specification: string;
  sutType: 'MANAGED' | 'DEPLOYED' | 'REPOSITORY' | '';
  description: string;
  project: ProjectModel;
  eimConfig: EimConfigModel;
  eimMonitoringConfig: EimMonitoringConfigModel;
  instrumentalize: boolean;
  currentSutExec: string;
  instrumentedBy: string;
  port: string;
  managedDockerType: 'IMAGE' | 'COMPOSE' | 'COMMANDS' | '';
  mainService: string;
  parameters: any[];
  commands: string;
  commandsOption: 'DEFAULT' | 'IN_NEW_CONTAINER' | 'IN_DOCKER_COMPOSE' | '';

  exProject: ExternalProjectModel;

  constructor() {
    this.id = 0;
    this.name = '';
    this.specification = '';
    this.sutType = '';
    this.description = '';
    this.project = undefined;
    this.eimConfig = new EimConfigModel();
    this.eimMonitoringConfig = new EimMonitoringConfigModel('', 'sut', defaultStreamMap.log);
    this.instrumentalize = false;
    this.currentSutExec = undefined;
    this.instrumentedBy = '';
    this.port = undefined;
    this.managedDockerType = '';
    this.mainService = '';
    this.parameters = [];
    this.commands = '';
    this.commandsOption = '';

    this.exProject = undefined;
  }

  public getRouteString(): string {
    return this.name;
  }

  public isManaged(): boolean {
    return this.sutType === 'MANAGED';
  }

  public isDeployed(): boolean {
    return this.sutType === 'DEPLOYED';
  }

  public isByDockerCompose(): boolean {
    return this.isManaged() && this.managedDockerType === 'COMPOSE';
  }

  public isByDockerImage(): boolean {
    return this.isManaged() && this.managedDockerType === 'IMAGE';
  }

  public isByCommands(): boolean {
    return this.isManaged() && this.managedDockerType === 'COMMANDS';
  }

  public isByCommandsDefault(): boolean {
    return this.isByCommands() && this.commandsOption === 'DEFAULT';
  }

  public isByCommandsInNewContainer(): boolean {
    return this.isByCommands() && this.commandsOption === 'IN_NEW_CONTAINER';
  }

  public isByCommandsInDockerCompose(): boolean {
    return this.isByCommands() && this.commandsOption === 'IN_DOCKER_COMPOSE';
  }

  public getSutESIndex(): string {
    return 's' + this.id + '_e' + this.currentSutExec;
  }

  public withCommands(): boolean {
    return this.commands !== undefined && this.commands !== null && this.commands !== '';
  }

  public arrayCommands(): string[] {
    let commandsArray: string[] = this.commands.split(';').filter((x: string) => x); // ignore empty strings
    return commandsArray;
  }

  public mainServiceIsNotEmpty(): boolean {
    return this.mainService !== undefined && this.mainService !== null && this.mainService !== '';
  }
}
