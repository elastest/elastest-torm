import { ExternalTestCaseModel } from '../external-test-case/external-test-case-model';
import { AbstractTJobExecModel } from '../../models/abstract-tjob-exec-model';
import { ExternalTJobExecModel } from '../external-tjob-execution/external-tjob-execution-model';

export class ExternalTestExecutionModel {
  id: number;
  monitoringIndex: string;
  fields: any;
  result: string;
  externalId: string;
  externalSystemId: string;
  exTestCase: ExternalTestCaseModel;
  exTJobExec: ExternalTJobExecModel;
  startDate: Date;
  endDate: Date;

  constructor() {
    this.id = 0;
    this.monitoringIndex = '';
    this.fields = undefined;
    this.exTestCase = undefined;
    this.exTJobExec = undefined;
    this.startDate = undefined;
    this.endDate = undefined;
  }
}
