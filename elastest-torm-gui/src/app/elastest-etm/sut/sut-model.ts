import { ParameterModel } from '../parameter/parameter-model';
import { EimConfigModel } from './eim-config-model';
import { ProjectModel } from '../project/project-model';
import { ExternalProjectModel } from '../external/external-project/external-project-model';
import { EimMonitoringConfigModel } from './eim-monitoring-config.model';
import { ExternalElasticsearch } from './external-elasticsearch.model';

export class SutModel {
  id: number;
  name: string;
  specification: string;
  sutType: 'MANAGED' | 'DEPLOYED' | 'REPOSITORY' | '';
  description: string;
  project: ProjectModel;
  eimConfig: EimConfigModel;
  eimMonitoringConfig: EimMonitoringConfigModel;
  // Indicates if you want to instrumentalize the sut
  instrumentalize: boolean;
  // Indicates if the Sut is instrumentalized
  instrumentalized: boolean;
  currentSutExec: string;
  instrumentedBy: 'WITHOUT' | 'ELASTEST' | 'ADMIN' | 'EXTERNAL_ELASTICSEARCH' | '';
  protocol: 'http' | 'https' | ''; // On add new, add too in getProtocolsList
  port: string;
  path: string;
  managedDockerType: 'IMAGE' | 'COMPOSE' | 'COMMANDS' | '';
  mainService: string;
  parameters: ParameterModel[];
  commands: string;
  commandsOption: 'DEFAULT' | 'IN_NEW_CONTAINER' | 'IN_DOCKER_COMPOSE' | '';

  exProject: ExternalProjectModel;

  externalElasticsearch: ExternalElasticsearch;

  constructor(sut?: SutModel) {
    if (sut === undefined) {
      this.id = 0;
      this.name = '';
      this.specification = '';
      this.sutType = '';
      this.description = '';
      this.project = undefined;
      this.eimConfig = new EimConfigModel();
      this.eimMonitoringConfig = new EimMonitoringConfigModel('', 'sut', false);
      this.instrumentalize = false;
      this.instrumentalized = false;
      this.currentSutExec = undefined;
      this.instrumentedBy = '';
      this.protocol = 'http';
      this.port = undefined;
      this.path = undefined;
      this.managedDockerType = '';
      this.mainService = '';
      this.parameters = [];
      this.commands = '';
      this.commandsOption = '';

      this.exProject = undefined;
      this.externalElasticsearch = new ExternalElasticsearch();
    } else {
      this.id = sut.id;
      this.name = sut.name;
      this.specification = sut.specification;
      this.sutType = sut.sutType;
      this.description = sut.description;
      this.project = sut.project;
      this.eimConfig = sut.eimConfig;
      this.eimMonitoringConfig = sut.eimMonitoringConfig;
      this.instrumentalize = sut.instrumentalize;
      this.instrumentalized = sut.instrumentalized;
      this.currentSutExec = sut.currentSutExec;
      this.instrumentedBy = sut.instrumentedBy;
      this.protocol = sut.protocol;
      this.port = sut.port;
      this.path = sut.path;
      this.managedDockerType = sut.managedDockerType;
      this.mainService = sut.mainService;
      this.parameters = sut.parameters;
      this.commands = sut.commands;
      this.commandsOption = sut.commandsOption;

      this.exProject = sut.exProject;
      this.externalElasticsearch = sut.externalElasticsearch;
    }
  }

  public getProtocolsList(): string[] {
    return ['http', 'https'];
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

  public isInstrumentedByElastest(): boolean {
    return this.isDeployed() && this.instrumentedBy === 'ELASTEST';
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

  public isInstrumentedByElastestAndHasBeatsConfig(): boolean {
    return (
      this.isInstrumentedByElastest() &&
      this.eimMonitoringConfig !== undefined &&
      this.eimMonitoringConfig !== null &&
      this.eimMonitoringConfig.beats !== undefined &&
      this.eimMonitoringConfig.beats !== null
    );
  }

  public isInstrumentedByElastestAndHasDockerizedBeatsConfig(): boolean {
    return (
      this.isInstrumentedByElastestAndHasBeatsConfig() &&
      ((this.eimMonitoringConfig.beats.metricbeat !== undefined &&
        this.eimMonitoringConfig.beats.metricbeat.dockerized !== undefined &&
        this.eimMonitoringConfig.beats.metricbeat.dockerized.length > 0) ||
        (this.eimMonitoringConfig.beats.filebeat !== undefined &&
          this.eimMonitoringConfig.beats.filebeat.dockerized !== undefined &&
          this.eimMonitoringConfig.beats.filebeat.dockerized.length > 0))
    );
  }
}
