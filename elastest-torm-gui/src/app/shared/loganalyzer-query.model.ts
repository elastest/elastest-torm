import { TreeCheckElementModel } from './ag-tree-model';

export class LogAnalyzerQueryModel {
  indices: string[];
  componentsStreams: TreeCheckElementModel[];
  levels: string[];
  size: number;
  rangeLT: Date | string;
  rangeGT: Date | string;
  rangeLTE: Date | string;
  rangeGTE: Date | string;
  matchMessage: string;
  searchAfterTrace: object;

  // Others
  filterPathList: string[];

  constructor() {
    this.indices = [];
    this.componentsStreams = [];
    this.levels = [];

    this.filterPathList = [];
  }
}
