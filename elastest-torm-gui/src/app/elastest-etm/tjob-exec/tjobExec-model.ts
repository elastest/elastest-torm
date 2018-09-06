import { SutExecModel } from '../sut-exec/sutExec-model';
import { TJobModel } from '../tjob/tjob-model';
import { AbstractTJobExecModel } from '../models/abstract-tjob-exec-model';
import { TestSuiteModel } from '../test-suite/test-suite-model';

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

  constructor() {
    super();
    this.id = 0;
    this.duration = 0;
    this.error = undefined;
    this.sutExec = undefined;
    this.monitoringIndex = '';
    this.tJob = undefined;
    this.testSuites = [];
    this.parameters = [];
    this.externalUrls = undefined;
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
}
