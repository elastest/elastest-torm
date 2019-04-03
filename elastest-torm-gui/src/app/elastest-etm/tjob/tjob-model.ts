import { MultiConfigModel } from '../../shared/multi-config-view/multi-config-view.component';
import { EsmServiceModel } from '../../elastest-esm/esm-service.model';
import { DashboardConfigModel } from './dashboard-config-model';
import { ProjectModel } from '../project/project-model';
import { SutModel } from '../sut/sut-model';
import { TJobExecModel } from '../tjob-exec/tjobExec-model';
import { AbstractTJobModel } from '../models/abstract-tjob-model';

export class TJobModel extends AbstractTJobModel {
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
  external: boolean;
  externalUrls: any;
  multi: boolean;
  multiConfigurations: MultiConfigModel[];
  maxExecutions: number;

  constructor(tJob?: TJobModel) {
    let defaultMaxExecutions: number = 30;
    super(tJob);
    if (tJob === undefined) {
      this.imageName = '';
      this.sut = undefined;
      this.project = undefined;
      this.tjobExecs = [];
      this.parameters = [];
      this.commands = '';
      this.resultsPath = '';
      this.esmServicesString = '';
      this.esmServices = [];
      this.esmServicesChecked = 0;
      this.external = false;
      this.externalUrls = undefined;
      this.multi = false;
      this.multiConfigurations = [];
      this.maxExecutions = defaultMaxExecutions;
    } else {
      this.imageName = tJob.imageName;
      this.sut = tJob.sut;
      this.project = tJob.project;
      this.tjobExecs = tJob.tjobExecs;
      this.parameters = tJob.parameters;
      this.commands = tJob.commands;
      this.resultsPath = tJob.resultsPath;
      this.esmServicesString = tJob.esmServicesString;
      this.esmServices = tJob.esmServices;
      this.esmServicesChecked = tJob.esmServicesChecked;
      this.external = tJob.external;
      this.externalUrls = tJob.externalUrls;
      this.multi = tJob.multi;
      this.multiConfigurations = tJob.multiConfigurations;
      this.maxExecutions =
        tJob.maxExecutions !== undefined && tJob.maxExecutions !== null ? tJob.maxExecutions : defaultMaxExecutions;
    }
  }

  public cloneTJob(): TJobModel {
    let tJob: TJobModel = Object.assign(new TJobModel(), this, {
      parameters: this.parameters ? [...this.parameters] : [],
      tjobExecs: this.tjobExecs ? [...this.tjobExecs] : [],
      esmServices: this.esmServices ? [...this.esmServices] : [],
      multiConfigurations: this.multiConfigurations ? [...this.multiConfigurations] : [],
    });
    return tJob;
  }

  public withCommands(): boolean {
    return this.commands !== undefined && this.commands !== null && this.commands !== '';
  }

  public arrayCommands(): string[] {
    let commandsArray: string[] = this.commands.split(';').filter((x: string) => x); // ignore empty strings
    return commandsArray;
  }

  public getRouteString(): string {
    return this.name;
  }

  hasParameters(): boolean {
    return this.parameters.length > 0 || this.sut.parameters.length > 0;
  }

  public hasMultiConfiguration(): boolean {
    return (
      this.multi &&
      this.multiConfigurations !== undefined &&
      this.multiConfigurations !== null &&
      this.multiConfigurations.length > 0
    );
  }

  public getAbstractTJobClass(): string {
    return 'TJobModel';
  }

  public getExternalEditPage(): string {
    if (this.external && this.externalUrls['jenkins-Job']) {
      return this.externalUrls['jenkins-Job'];
    } else {
      return undefined;
    }
  }
}
