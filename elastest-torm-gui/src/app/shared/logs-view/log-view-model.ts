import { LoadPreviousModel } from '../load-previous-view/load-previous-model';

export interface LogViewModel extends LoadPreviousModel {
  name: string;
  traces: any[];
  filteredTraces: any[];
  prevTraces: any[];
  prevLoaded: boolean;
  hidePrevBtn: boolean;
  etType: string;
  monitoringIndex: string;

  cloneModel(): LogViewModel;

  getAllLogs(): void;
  loadPrevious(): void;

  isInfoTrace(trace: any): boolean;
  isErrorTrace(trace: any): boolean;
  isWarningTrace(trace: any): boolean;
  isErrorOrWarningTrace(trace: any): boolean;

  getInfos(): any[];
  getErrors(): any[];
  getWarnings(): any[];
  getErrorsAndWarnings(): any[];
}
