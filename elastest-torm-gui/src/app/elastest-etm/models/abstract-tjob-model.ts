import { DashboardConfigModel } from '../tjob/dashboard-config-model';

export class AbstractTJobModel {
  id: number;
  name: string;
  //sut
  //project
  execDashboardConfig: string;
  execDashboardConfigModel: DashboardConfigModel;

  constructor() {
    this.id = 0;
    this.name = '';
    this.execDashboardConfig = '';
    this.execDashboardConfigModel = new DashboardConfigModel();
  }

  public generateExecDashboardConfig(): void {
    this.execDashboardConfig = JSON.stringify(this.execDashboardConfigModel);
  }

  //   public hasSut(): boolean {
  //     return this.sut !== undefined && this.sut !== null && this.sut.id !== 0;
  //   }
}
