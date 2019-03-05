import { ExternalTestCaseModel } from '../external-test-case/external-test-case-model';
import { AbstractTJobExecModel } from '../../models/abstract-tjob-exec-model';
import { ExternalTJobExecModel } from '../external-tjob-execution/external-tjob-execution-model';
import { ServiceType } from '../external-project/external-project-model';
import { TestCaseExecutionModel } from '../../../etm-testlink/models/test-case-execution-model';
import { getResultIconByString } from '../../../shared/utils';
import { ExternalTJobModel } from '../external-tjob/external-tjob-model';

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

  finished(): boolean {
    return (
      this.result === 'SUCCESS' ||
      this.result === 'FAIL' ||
      this.result === 'ERROR' ||
      this.result === 'STOPPED' ||
      this.result === 'FAILED' ||
      this.result === 'PASSED' ||
      this.result === 'BLOCKED'
    );
  }

  notExecuted(): boolean {
    return this.result === 'NOT_EXECUTED';
  }

  stopped(): boolean {
    return this.result === 'STOPPED';
  }

  public getResultIcon(): any {
    let icon: any = {
      name: '',
      color: '',
    };
    if (this.finished() || this.notExecuted()) {
      icon = getResultIconByString(this.result);
    }
    return icon;
  }

  public setFieldsByExternalObjAndService(externalObj: any, service: ServiceType): void {
    switch (service) {
      case 'TESTLINK':
        this.setTestLinkFields(externalObj);
        break;
      default:
        break;
    }
  }

  public setTestLinkFields(tlExec: TestCaseExecutionModel): void {
    let fieldsObj: object = {
      build: { id: tlExec.buildId },
      plan: { id: tlExec.testPlanId },
    };
    this.fields = JSON.stringify(fieldsObj);
  }

  public getRouteString(specificExTJob?: ExternalTJobModel): string {
    return this.exTestCase.getRouteString(specificExTJob) + ' / Execution ' + this.id;
  }

  getServiceType(): ServiceType {
    let type: ServiceType;
    if (this.exTestCase !== undefined) {
      type = this.exTestCase.getServiceType();
    }
    return type;
  }
}
