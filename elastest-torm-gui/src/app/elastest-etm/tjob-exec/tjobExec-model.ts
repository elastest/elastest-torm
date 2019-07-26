import { MultiConfigModel } from '../../shared/multi-config-view/multi-config-view.component';
import { SutExecModel } from '../sut-exec/sutExec-model';
import { TJobModel } from '../tjob/tjob-model';
import { AbstractTJobExecModel } from '../models/abstract-tjob-exec-model';
import { TestSuiteModel } from '../test-suite/test-suite-model';
import { removeDuplicates } from '../../shared/utils';

export class TJobExecModel extends AbstractTJobExecModel {
  id: number;
  duration: number;
  error: string;
  sutExec: SutExecModel;
  monitoringIndex: string;
  tJob: TJobModel;
  testSuites: TestSuiteModel[];
  parameters: any[];
  externalUrls: any;
  type: TJobExecTypeEnum;
  execParent: TJobExecModel;
  execChilds: TJobExecModel[];
  multiConfigurations: MultiConfigModel[];

  constructor(tJobExec: TJobExecModel = undefined) {
    super(tJobExec);
    if (tJobExec !== undefined) {
      this.id = tJobExec.id;
      this.duration = tJobExec.duration;
      this.error = tJobExec.error;
      this.sutExec = tJobExec.sutExec;
      this.monitoringIndex = tJobExec.monitoringIndex;
      this.tJob = tJobExec.tJob;
      this.testSuites = tJobExec.testSuites;
      this.parameters = tJobExec.parameters;
      this.externalUrls = tJobExec.externalUrls;
      this.type = tJobExec.type;
      this.execParent = tJobExec.execParent;
      this.execChilds = tJobExec.execChilds;
      this.multiConfigurations = tJobExec.multiConfigurations;
    } else {
      this.id = 0;
      this.duration = 0;
      this.error = undefined;
      this.sutExec = undefined;
      this.monitoringIndex = '';
      this.tJob = undefined;
      this.testSuites = [];
      this.parameters = [];
      this.externalUrls = undefined;
      this.type = 'SIMPLE';
      this.execParent = undefined;
      this.execChilds = [];
      this.multiConfigurations = [];
    }
  }

  public hasSutExec(): boolean {
    return this.sutExec !== undefined && this.sutExec !== null && this.sutExec.id !== 0;
  }

  public getRouteString(): string {
    return 'Execution ' + this.id;
  }

  public getAbstractTJobExecClass(): string {
    return 'TJobExecModel';
  }

  public getExternalUrl(): string {
    if (this.externalUrls !== undefined) {
      let url: string = this.externalUrls['jenkins-build-url'];
      return url !== undefined && url !== null && url !== '' ? url : undefined;
    } else {
      return undefined;
    }
  }

  isSimple(): boolean {
    let isSimple: boolean = this.type === 'SIMPLE';
    if (this.tJob) {
      isSimple = isSimple && this.tJob.multi;
    }
    return isSimple;
  }

  isChild(): boolean {
    let isChild: boolean = this.type === 'CHILD';

    if (this.tJob) {
      isChild = isChild && this.tJob.multi;
    }

    return isChild;
  }

  isParent(): boolean {
    let isParent: boolean = this.type === 'PARENT';

    if (this.tJob) {
      isParent = isParent && this.tJob.multi;
    }

    return isParent;
  }

  isMultiConfig(): boolean {
    return this.isChild() || this.isParent();
  }

  hasChilds(): boolean {
    return this.execChilds !== undefined && this.execChilds !== null && this.execChilds.length > 0;
  }

  // All monitoring indices separated by comma
  getChildsMonitoringIndices(): string {
    let monitoringIndices: string = '';
    if (this.isParent() && this.execChilds !== undefined) {
      let counter: number = 0;
      for (let child of this.execChilds) {
        if (counter === 0 && child.monitoringIndex !== undefined && child.monitoringIndex !== '') {
          monitoringIndices = child.monitoringIndex;
          counter++;
        } else {
          monitoringIndices +=
            child.monitoringIndex !== undefined && child.monitoringIndex !== '' ? ',' + child.monitoringIndex : '';
          counter++;
        }
      }
    }
    return monitoringIndices;
  }

  // List of monitoring indices by exec (['325, s3_e54', '326, s3_e54', '327, s3_e54'])
  getChildsMonitoringIndicesList(): string[] {
    let monitoringIndices: string[] = [];
    if (this.isParent() && this.execChilds !== undefined) {
      for (let child of this.execChilds) {
        if (child.monitoringIndex !== undefined && child.monitoringIndex !== '') {
          monitoringIndices.push(child.monitoringIndex);
        }
      }
    }
    return monitoringIndices;
  }

  hasMonitoringMarks(): boolean {
    let hasMarks: boolean = super.hasMonitoringMarks();

    if (this.isParent() && this.execChilds) {
      for (let child of this.execChilds) {
        hasMarks = hasMarks || child.hasMonitoringMarks();
      }
    }
    return hasMarks;
  }

  getMonitoringMarkIds(): string[] {
    if (!this.hasMonitoringMarks()) {
      return [];
    }

    let monitoringMarkIds: string[] = super.getMonitoringMarkIds();
    if (this.isParent() && this.execChilds) {
      for (let child of this.execChilds) {
        monitoringMarkIds = monitoringMarkIds.concat(child.getMonitoringMarkIds());
      }
    }
    monitoringMarkIds = removeDuplicates(monitoringMarkIds);
    return monitoringMarkIds;
  }

  getIdAsString(): string {
    return '' + this.id;
  }

  hasTestSuites(): boolean {
    return this.testSuites !== undefined && this.testSuites !== null && this.testSuites.length > 0;
  }
}

export type TJobExecTypeEnum = 'SIMPLE' | 'PARENT' | 'CHILD' | '';
