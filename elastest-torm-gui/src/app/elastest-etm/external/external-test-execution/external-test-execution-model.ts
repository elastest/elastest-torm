import { ExternalTestCaseModel } from '../external-test-case/external-test-case-model';
import { AbstractTJobExecModel } from '../../models/abstract-tjob-exec-model';

export class ExternalTestExecutionModel extends AbstractTJobExecModel {
  id: number;
  monitoringIndex: string;
  fields: any;
  result: string;
  externalId: string;
  externalSystemId: string;
  exTestCase: ExternalTestCaseModel;

  constructor() {
    super();
    this.id = 0;
    this.monitoringIndex = '';
    this.fields = undefined;
    this.exTestCase = undefined;
  }
}
