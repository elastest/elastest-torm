import { Component, OnInit, ViewChild, Input } from '@angular/core';
import { ExternalDataModel } from '../../../models/external-data-model';
import { IExternalExecution } from '../../../models/external-execution-interface';
import { ExternalService } from '../../../external.service';
import { ExternalTJobModel } from '../../../external-tjob/external-tjob-model';
import { Observable } from 'rxjs/Observable';
import { ServiceType } from '../../../external-project/external-project-model';
import { BuildModel } from '../../../../../etm-testlink/models/build-model';
import { TestLinkService } from '../../../../../etm-testlink/testlink.service';
import { TLTestCaseModel } from '../../../../../etm-testlink/models/test-case-model';
import { ExternalTestCaseModel } from '../../../external-test-case/external-test-case-model';
import { ExternalTJobExecModel } from '../../external-tjob-execution-model';
import { window } from 'rxjs/operator/window';
import { ExternalTestExecutionModel } from '../../../external-test-execution/external-test-execution-model';
import { IExternalExecutionSaveModel } from '../../../models/external-execution-save.model';
import { TestCaseExecutionModel } from '../../../../../etm-testlink/models/test-case-execution-model';

@Component({
  selector: 'etm-case-execution-view',
  templateUrl: './case-execution-view.component.html',
  styleUrls: ['./case-execution-view.component.scss'],
})
export class CaseExecutionViewComponent implements OnInit, IExternalExecution {
  @ViewChild('executionForm') executionForm: IExternalExecution;

  @Input() exTJob: ExternalTJobModel;
  @Input() exTJobExec: ExternalTJobExecModel;

  serviceType: ServiceType;
  externalTestCases: ExternalTestCaseModel[] = [];
  currentExternalTestExecution: ExternalTestExecutionModel;

  data: any;
  tJobExecUrl: string;

  // TestLink
  showTestLinkExec: boolean = false;
  testLinkBuilds: BuildModel[] = [];
  testLinkSelectedBuild: BuildModel;
  disableTLNextBtn: boolean = false;
  execFinished: boolean = false;

  constructor(private externalService: ExternalService, private testLinkService: TestLinkService) {}
  ngOnInit() {
    this.serviceType = this.exTJob.getServiceType();
    this.initExecutionView();
  }

  initExecutionView(): void {
    switch (this.serviceType) {
      case 'TESTLINK':
        this.setTJobExecutionUrl('Test Plan Execution:');
        this.initTestLinkData();
        break;
      default:
        break;
    }
  }

  setTJobExecutionUrl(label: string): void {
    if (this.exTJobExec) {
      this.tJobExecUrl =
        document.location.origin +
        '/#/external/projects/' +
        this.exTJob.exProject.id +
        '/tjob/' +
        this.exTJob.id +
        '/exec/' +
        this.exTJobExec.id;
      this.tJobExecUrl = ' <p><strong>' + label + ' </strong><a href="' + this.tJobExecUrl + '">' + this.tJobExecUrl + '</a></p>';
    }
  }

  getCurrentTestExecutionUrl(label: string): string {
    let testExecUrl: string =
      document.location.origin +
      '/#/external/projects/' +
      this.exTJob.exProject.id +
      '/tjob/' +
      this.exTJob.id +
      '/case/' +
      this.currentExternalTestExecution.exTestCase.id +
      '/exec/' +
      this.currentExternalTestExecution.id;
    testExecUrl = ' <p><strong>' + label + ' </strong><a href="' + testExecUrl + '">' + testExecUrl + '</a></p>';
    return testExecUrl;
  }

  /**********************/
  /* **** TestLink **** */
  /**********************/

  initTestLinkData(): void {
    this.testLinkService.getPlanBuildsById(this.exTJob.externalId).subscribe(
      (builds: BuildModel[]) => {
        this.testLinkBuilds = builds;
      },
      (error) => console.log(error),
    );
  }

  selectTestLinkBuild(): void {
    this.showTestLinkExec = true;
    this.externalTestCases = this.exTJob.exTestCases;
    if (this.testLinkSelectedBuild) {
      this.testLinkService.getBuildById(this.testLinkSelectedBuild.id).subscribe(
        (build: BuildModel) => {
          this.data = {
            build: build,
          };
          // Load First TCase
          this.loadNextTestLinkCase();
        },
        (error) => console.log(error),
      );
    }
  }

  loadNextTestLinkCase(): void {
    let nextCase: ExternalTestCaseModel = this.externalTestCases.shift();
    if (nextCase !== undefined) {
      this.initCurrentExternalTestExecution(nextCase);
      this.externalService.createExternalTestExecution(this.currentExternalTestExecution).subscribe(
        (savedExTestExec: ExternalTestExecutionModel) => {
          this.currentExternalTestExecution = savedExTestExec;
          this.startTestLinkTestCaseExecution(nextCase.externalId);
        },
        (error) => console.log(error),
      );
    } else {
      this.finishTJobExecution();
    }
  }

  initCurrentExternalTestExecution(currentCase: ExternalTestCaseModel): void {
    this.currentExternalTestExecution = new ExternalTestExecutionModel();
    this.currentExternalTestExecution.exTestCase = currentCase;
    this.currentExternalTestExecution.exTJobExec = new ExternalTJobExecModel();
    this.currentExternalTestExecution.exTJobExec.id = this.exTJobExec.id;

    this.currentExternalTestExecution.startDate = new Date();
    this.currentExternalTestExecution.externalSystemId = currentCase.externalSystemId;
    this.currentExternalTestExecution.externalId = 'tmp-exId-' + this.exTJobExec.id + '-' + currentCase.id;
    this.currentExternalTestExecution.monitoringIndex = this.exTJobExec.monitoringIndex;
  }

  startTestLinkTestCaseExecution(testCaseId: string): void {
    let build: BuildModel = this.data.build;
    let additionalNotes: string = this.getCurrentTestExecutionUrl('Test Case Execution:');
    additionalNotes += this.tJobExecUrl;
    this.testLinkService.getBuildTestCaseById(build.id, testCaseId).subscribe(
      (testCase: TLTestCaseModel) => {
        // New object to detect on changes
        this.data = {
          testCase: testCase,
          build: build,
          additionalNotes: additionalNotes,
        };
      },
      (error) => console.log(error),
    );
  }

  saveTLCaseExecution(): void {
    this.saveExecution().subscribe(
      (savedObj: IExternalExecutionSaveModel) => {
        let tlExec: TestCaseExecutionModel = savedObj.response;
        if (tlExec.id !== undefined) {
          this.currentExternalTestExecution.externalId = tlExec.id.toString();
          this.currentExternalTestExecution.endDate = new Date();
          this.currentExternalTestExecution.result = tlExec.status;
          this.currentExternalTestExecution.setFieldsByExternalObjAndService(tlExec, 'TESTLINK');

          this.externalService.modifyExternalTestExecution(this.currentExternalTestExecution).subscribe(
            (savedExTestExec: ExternalTestExecutionModel) => {
              this.externalService.popupService.openSnackBar('TestCase Execution has been saved successfully');
              this.loadNextTestLinkCase();
            },
            (error) => console.log(error),
          );
        }
      },
      (error) => console.log(error),
    );
  }

  finishTJobExecution(): void {
    this.disableTLNextBtn = true;
    this.execFinished = true;
    this.externalService.getExternalTestExecsByExternalTJobExecId(this.exTJobExec.id).subscribe(
      (exTestExecs: ExternalTestExecutionModel[]) => {
        this.exTJobExec.exTestExecs = exTestExecs;
        this.exTJobExec.updateResultByTestExecsResults();
        this.externalService.popupService.openSnackBar('There is no more Test Cases to Execute');
        this.exTJobExec.endDate = new Date();
        this.exTJobExec.exTestExecs = []; // TODO fix No _valueDeserializer assigned
        this.exTJobExec.exTJob.exTestCases = []; // TODO fix No _valueDeserializer assigned

        this.externalService.modifyExternalTJobExec(this.exTJobExec).subscribe();
      },
      (error) => {
        this.exTJobExec.result = 'SUCCESS';
        this.externalService.popupService.openSnackBar('There is no more Test Cases to Execute');
        this.externalService.modifyExternalTJobExec(this.exTJobExec).subscribe();
      },
    );
  }

  saveExecution(): Observable<IExternalExecutionSaveModel> {
    if (this.executionForm) {
      return this.executionForm.saveExecution();
    } else {
      this.finishTJobExecution();
      throw new Error('Error: TestCase View not loaded');
    }
  }

  forceStop(): Observable<ExternalTJobExecModel> {
    this.exTJobExec.result = 'STOPPED';
    return this.externalService.modifyExternalTJobExec(this.exTJobExec);
  }
}
