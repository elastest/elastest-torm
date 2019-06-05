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

  cloneModel(): LogViewModel {
    let model: CardLogModel = new CardLogModel();
    model.name = this.name;
    model.prevTraces = [...this.prevTraces];
    model.traces = [...this.traces];
    model.filteredTraces = [...this.filteredTraces];
    model.prevLoaded = this.prevLoaded;
    model.hidePrevBtn = this.hidePrevBtn;
    model.etType = this.etType;
    model.monitoringIndex = this.monitoringIndex;
    model.startDate = this.startDate;
    model.endDate = this.endDate;
    return model;
  }

  loadPrevious(): void {
    this.previousFunctionObj.function();
    this.prevLoaded = true;
  }
  getAllLogs(): void {}

  isInfoTrace(trace: any): boolean {
    if (trace && trace.level) {
      let lowerCasedLevel: string = trace.level.toLowerCase();
      return lowerCasedLevel === 'info';
    }
  }

  isErrorTrace(trace: any): boolean {
    if (trace && trace.level) {
      let lowerCasedLevel: string = trace.level.toLowerCase();
      return lowerCasedLevel === 'error' || lowerCasedLevel === 'err' || lowerCasedLevel === 'severe';
    }
    return false;
  }

  isWarningTrace(trace: any): boolean {
    if (trace && trace.level) {
      let lowerCasedLevel: string = trace.level.toLowerCase();
      return lowerCasedLevel === 'warn' || lowerCasedLevel === 'warning';
    }
    return false;
  }

  isErrorOrWarningTrace(trace: any): boolean {
    return this.isErrorTrace(trace) || this.isWarningTrace(trace);
  }

  getTracesByCondition(condition: Function): any[] {
    let tracesMatched: any[] = [];
    if (this.prevTraces) {
      for (let trace of this.prevTraces) {
        if (condition(trace)) {
          tracesMatched.push(trace);
        }
      }
    }

    if (this.traces) {
      for (let trace of this.traces) {
        if (condition(trace)) {
          tracesMatched.push(trace);
        }
      }
    }

    return tracesMatched;
  }

  getInfos(): any[] {
    return this.getTracesByCondition(this.isInfoTrace);
  }

  getErrors(): any[] {
    return this.getTracesByCondition(this.isErrorTrace);
  }

  getWarnings(): any[] {
    return this.getTracesByCondition(this.isWarningTrace);
  }

  getErrorsAndWarnings(): any[] {
    return this.getTracesByCondition(this.isErrorOrWarningTrace.bind(this));
  }
}
