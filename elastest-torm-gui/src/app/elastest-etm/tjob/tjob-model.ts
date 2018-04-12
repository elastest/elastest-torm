import { EsmServiceModel } from '../../elastest-esm/esm-service.model';
import { selectContext } from 'webdriver-js-extender/built/lib/command_definitions';
import { DashboardConfigModel } from './dashboard-config-model';
import { AllMetricsFields } from '../../shared/metrics-view/metrics-chart-card/models/all-metrics-fields-model';
import { ParameterModel } from '../parameter/parameter-model';
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

  constructor() {
    super();
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
    this.external = false;
  }

  public cloneTJob(): TJobModel {
    let tJob: TJobModel = Object.assign({}, this, {
      parameters: [...this.parameters],
      tjobExecs: [...this.tjobExecs],
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

  public changeServiceSelection($event, i: number): void {
    console.log('Service id:' + i);
    this.esmServices[i].selected = $event.checked;
  }

  public getRouteString(): string {
    return this.name;
  }

  hasParameters(): boolean {
    return this.parameters.length > 0 || this.sut.parameters.length > 0;
  }

  public getAbstractTJobClass(): string {
    return 'TJobModel';
  }

  public getLastExecution(): TJobExecModel {
    if (this.tjobExecs.length > 0) {
      return this.tjobExecs[this.tjobExecs.length - 1];
    }
    return undefined;
  }
}
