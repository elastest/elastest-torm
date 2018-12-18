import { DashboardConfigModel } from '../tjob/dashboard-config-model';
import { AbstractProjectModel } from './abstract-project-model';
import { SutModel } from '../sut/sut-model';

export class AbstractTJobModel {
  id: number;
  name: string;
  sut: SutModel;
  project: AbstractProjectModel;
  execDashboardConfig: string;
  execDashboardConfigModel: DashboardConfigModel;

  constructor(absTJob?: AbstractTJobModel) {
    if (absTJob === undefined) {
      this.id = 0;
      this.name = '';
      this.execDashboardConfig = '';
      this.execDashboardConfigModel = new DashboardConfigModel();
    } else {
      this.id = absTJob.id;
      this.name = absTJob.name;
      this.execDashboardConfig = absTJob.execDashboardConfig;
      this.execDashboardConfigModel = absTJob.execDashboardConfigModel;
    }
  }

  public generateExecDashboardConfig(): void {
    this.execDashboardConfig = JSON.stringify(this.execDashboardConfigModel);
  }

  public hasSut(): boolean {
    return this.sut !== undefined && this.sut !== null && this.sut.id !== 0;
  }

  public getAbstractTJobClass(): string {
    return 'AbstractTJobModel';
  }
}
