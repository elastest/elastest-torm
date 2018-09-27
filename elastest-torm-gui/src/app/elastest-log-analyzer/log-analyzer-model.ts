import { Subscription } from 'rxjs/Rx';
import { AgTreeCheckModel } from '../shared/ag-tree-model';
import { LogAnalyzerConfigModel } from './log-analyzer-config-model';
export class LogAnalyzerModel {
  // Basic
  selectedIndices: string[];
  fromDate: Date;
  toDate: Date;
  maxResults: number;
  tail: boolean;
  tailSubscription: Subscription;
  pauseTail: boolean;
  usingTail: boolean;
  messageFilter: string;

  selectedRow: number;

  // Filters
  componentsStreams: AgTreeCheckModel;
  levels: AgTreeCheckModel;

  // Grid Config
  laConfig: LogAnalyzerConfigModel;

  constructor() {
    this.selectedIndices = ['*'];
    this.fromDate = this.getDefaultFromDate();
    this.toDate = this.getDefaultToDate();
    this.maxResults = 2400;
    this.tail = false;
    this.tailSubscription = undefined;
    this.pauseTail = false;
    this.usingTail = false;
    this.messageFilter = '';

    this.componentsStreams = new AgTreeCheckModel();
    this.levels = new AgTreeCheckModel();
    this.messageFilter = '';
    this.selectedRow = undefined;

    this.laConfig = new LogAnalyzerConfigModel();
  }

  public getDefaultFromDate(): Date {
    return new Date(new Date().valueOf() - 24 * 60 * 60 * 1000);
  }

  public getDefaultToDate(): Date {
    return new Date(new Date().valueOf() + 2 * 60 * 60 * 1000);
  }

  public selectedIndicesToString(): string {
    return this.selectedIndices.join(',');
  }

  public setComponentsStreams(componentsStreams: any[]): void {
    this.componentsStreams = new AgTreeCheckModel();
    this.componentsStreams.setByObjArray(componentsStreams);
    this.componentsStreams.setCheckedToAll(true);
  }

  public setLevels(levels: any[]): void {
    this.levels = new AgTreeCheckModel();
    this.levels.setByObjArray(levels);
    this.levels.setCheckedToAll(false);
  }

  public hasSelectedRow(): boolean {
    return this.selectedRow !== undefined;
  }

  public switchPauseTail(pause: boolean): void {
    this.pauseTail = pause;
  }

  public stopTail(): void {
    if (this.tailSubscription) {
      this.tailSubscription.unsubscribe();
    }
    this.tailSubscription = undefined;
  }
}
