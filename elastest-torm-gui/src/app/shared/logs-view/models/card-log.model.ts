import { LogViewModel } from '../log-view-model';

export class CardLogModel implements LogViewModel {
  name: string;
  traces: any[];
  filteredTraces: any[];
  prevTraces: any[];
  prevLoaded: boolean;
  hidePrevBtn: boolean;
  etType: string;
  monitoringIndex: string;

  startDate: Date;
  endDate: Date;

  previousFunctionObj: { function: Function };

  constructor() {
    this.name = '';
    this.prevTraces = [];
    this.traces = [];
    this.filteredTraces = [];
    this.prevLoaded = false;
    this.hidePrevBtn = false;
    this.etType = '';
    this.monitoringIndex = '';
  }

  loadPrevious(): void {
    this.previousFunctionObj.function();
    this.prevLoaded = true;
  }
  getAllLogs(): void {}
}
