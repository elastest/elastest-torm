import { ExecutionFormComponent } from '../../execution/execution-form/execution-form.component';
import { Component, OnInit, ViewChild, HostListener } from '@angular/core';
import { ExternalTJobModel } from '../../../elastest-etm/external/external-tjob/external-tjob-model';
import { ExternalTJobExecModel } from '../../../elastest-etm/external/external-tjob-execution/external-tjob-execution-model';
import { ExternalService } from '../../../elastest-etm/external/external.service';
import { Router, Params, ActivatedRoute } from '@angular/router';
import { TestLinkService } from '../../testlink.service';
import { EusService } from '../../../elastest-eus/elastest-eus.service';
import { ExternalTestCaseModel } from '../../../elastest-etm/external/external-test-case/external-test-case-model';
import { ExternalTestExecutionModel } from '../../../elastest-etm/external/external-test-execution/external-test-execution-model';
import { CompleteUrlObj } from '../../../shared/utils';
import { BuildModel } from '../../models/build-model';
import { IExternalExecutionSaveModel } from '../../../elastest-etm/external/models/external-execution-save.model';
import { TestCaseExecutionModel } from '../../models/test-case-execution-model';
import { TLTestCaseModel } from '../../models/test-case-model';
import { Observable, Subscription } from 'rxjs';
import { CaseExecutionViewComponent } from '../../../elastest-etm/external/external-tjob-execution/external-tjob-execution-new/case-execution-view/case-execution-view.component';
import { EtmMonitoringViewComponent } from '../../../elastest-etm/etm-monitoring-view/etm-monitoring-view.component';
import { PullingObjectModel } from '../../../shared/pulling-obj.model';
import { EsmServiceInstanceModel } from '../../../elastest-esm/esm-service-instance.model';
import { EsmService } from '../../../elastest-esm/esm-service.service';
import { TestPlanModel } from '../../models/test-plan-model';
import { EusTestModel } from '../../../elastest-eus/elastest-eus-test-model';
import { Response } from '@angular/http';
import { TitlesService } from '../../../shared/services/titles.service';

@Component({
  selector: 'testlink-test-plan-execution',
  templateUrl: './test-plan-execution.component.html',
  styleUrls: ['./test-plan-execution.component.scss'],
})
export class TestPlanExecutionComponent implements OnInit {
  @ViewChild('logsAndMetrics') logsAndMetrics: EtmMonitoringViewComponent;
  @ViewChild('tlExecutionForm') tlExecutionForm: ExecutionFormComponent;

  params: Params;

  testPlan: TestPlanModel;
  selectedBuild: BuildModel;

  exTJob: ExternalTJobModel;
  exTJobExec: ExternalTJobExecModel;
  externalTestCases: ExternalTestCaseModel[] = [];
  currentExternalTestExecution: ExternalTestExecutionModel;

  exTJobExecFinish: boolean = false;
  execFinishedTimer: Observable<number>;
  execFinishedSubscription: Subscription;

  alreadyLeave: boolean = false;

  showStopBtn: boolean = false;

  executionCardMsg: string = 'Loading...';
  executionCardSubMsg: string = '';

  // EUS
  eusTimer: Observable<number>;
  eusSubscription: Subscription;
  eusInstanceId: string;
  eusUrl: string;

  browserCardMsg: string = 'Loading...';
  browserAndEusLoading: boolean = true;
  browserAndEusDeprovided: boolean = false;

  // Browser
  sessionId: string;
  hubContainerName: string;
  vncBrowserUrl: string;
  autoconnect: boolean = true;
  viewOnly: boolean = false;

  // Files
  showFiles: boolean = false;

  // Others
  data: any;
  tJobExecUrl: string;

  disableTLNextBtn: boolean = false;
  execFinished: boolean = false;

  constructor(
    private externalService: ExternalService,
    public router: Router,
    private route: ActivatedRoute,
    private esmService: EsmService,
    private eusService: EusService,
    private testLinkService: TestLinkService,
    private titlesService: TitlesService,
  ) {
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params.subscribe((params: Params) => {
        this.params = params;
        this.loadPlanAndBuild();
      });
    }
  }

  ngOnInit() {
    this.titlesService.setPathName(this.router.routerState.snapshot.url);
  }

  ngOnDestroy(): void {
    this.alreadyLeave = true;
    this.end();
  }

  @HostListener('window:beforeunload')
  beforeunloadHandler() {
    // On window closed leave session
    this.end();
  }

  loadPlanAndBuild(): void {
    this.testLinkService.getTestPlanById(this.params['planId']).subscribe(
      (plan: TestPlanModel) => {
        this.testPlan = plan;
        this.loadBuild();
      },
      (error: Error) => console.log(error, 'params:', this.params),
    );
  }

  loadBuild(): void {
    this.testLinkService.getBuildById(this.params['buildId']).subscribe(
      (build: BuildModel) => {
        this.selectedBuild = build;
        this.loadExternalTJob();
      },
      (error) => console.log(error),
    );
  }

  /**********************/
  /* **** External **** */
  /**********************/

  loadExternalTJob(): void {
    this.testLinkService.getExternalTJobByTestPlanId(this.testPlan.id).subscribe(
      (exTJob: ExternalTJobModel) => {
        this.exTJob = exTJob;
        this.createTJobExecution();
      },
      (error) => console.log(error),
    );
  }

  createTJobExecution(): void {
    this.exTJobExec = new ExternalTJobExecModel();
    this.externalService.createExternalTJobExecutionByExTJobId(this.exTJob.id).subscribe((exTJobExec: ExternalTJobExecModel) => {
      this.exTJobExec = exTJobExec;
      this.logsAndMetrics.initView(this.exTJob, this.exTJobExec);
      this.checkFinished();
      this.waitForEus(exTJobExec);
    });
  }

  checkFinished(): void {
    let responseObj: PullingObjectModel = this.externalService.checkTJobExecFinished(
      this.exTJobExec.id,
      this.execFinishedTimer,
      this.execFinishedSubscription,
    );
    this.execFinishedSubscription = responseObj.subscription;

    responseObj.observable.subscribe((finished: boolean) => {
      if (finished) {
        // this.end();
        if (!this.alreadyLeave) {
          // On Navigate to ended exec ngondestroy will deprovision EUS and browser
          this.viewEndedTJobExec();
        }
      }
    });
  }

  waitForEus(exTJobExec: ExternalTJobExecModel): void {
    this.executionCardMsg = 'Please wait while the browser is loading. This may take a while.';
    this.browserCardMsg = 'Waiting for EUS...';
    if (exTJobExec.envVars && exTJobExec.envVars['EUS_INSTANCE_ID']) {
      this.eusInstanceId = exTJobExec.envVars['EUS_INSTANCE_ID'];

      if (exTJobExec.envVars['ET_EUS_API']) {
        // If EUS is shared (started on init)
        this.eusUrl = exTJobExec.envVars['ET_EUS_API'];
        this.eusService.setEusUrl(this.eusUrl);
        this.loadChromeBrowser();
      } else {
        let responseObj: PullingObjectModel = this.esmService.waitForTssInstanceUp(
          this.eusInstanceId,
          this.eusTimer,
          this.eusSubscription,
          'external',
        );
        this.eusSubscription = responseObj.subscription;

        responseObj.observable.subscribe(
          (eus: EsmServiceInstanceModel) => {
            this.eusUrl = eus.apiUrl;
            this.eusService.setEusUrl(this.eusUrl);
            this.loadChromeBrowser();
          },
          (error) => console.log(error),
        );
      }
    }
  }

  loadChromeBrowser(): void {
    this.browserCardMsg = 'Waiting for Browser...';
    this.executionCardMsg = 'Just a little more...';
    let extraCapabilities: any = { manualRecording: true };
    this.eusService.startSession('chrome', 'latest', extraCapabilities).subscribe(
      (eusTestModel: EusTestModel) => {
        this.sessionId = eusTestModel.id;
        this.hubContainerName = eusTestModel.hubContainerName;
        this.showStopBtn = true;
        this.exTJobExec.envVars['BROWSER_SESSION_ID'] = this.sessionId;
        let browserLog: any = this.exTJobExec.getBrowserLogObj();
        if (browserLog) {
          this.logsAndMetrics.addMoreFromObj(browserLog);
        }
        this.eusService.getVncUrl(this.sessionId).subscribe(
          (vncUrl: string) => {
            this.vncBrowserUrl = vncUrl;
            this.browserAndEusLoading = false;
            this.startExecution();
          },
          (error) => console.error(error),
        );
      },
      (error) => console.log(error),
    );
  }

  startExecution(): void {
    this.externalTestCases = this.exTJob.exTestCases;
    this.setTJobExecutionUrl('Test Plan Execution:');
    this.data = {
      build: this.selectedBuild,
    };
    // Load First TCase
    this.loadNextTestLinkCase();
  }

  deprovideBrowserAndEus(): void {
    if (this.sessionId !== undefined) {
      this.browserCardMsg = 'Shutting down Browser...';
      this.vncBrowserUrl = undefined;
      this.eusService.stopSession(this.sessionId).subscribe(
        (ok) => {
          this.sessionId = undefined;
          this.deprovisionEUS();
        },
        (error) => {
          console.error(error);
          this.deprovisionEUS();
        },
      );
    } else {
      this.deprovisionEUS();
    }
  }

  deprovisionEUS(): void {
    if (this.eusInstanceId && this.exTJobExec) {
      this.browserCardMsg = 'Shutting down EUS...';
      this.esmService.deprovisionExternalTJobExecServiceInstance(this.eusInstanceId, this.exTJobExec.id).subscribe(
        () => {
          this.browserCardMsg = 'FINISHED';
          this.browserAndEusDeprovided = true;
          this.showFiles = true;
        },
        (error) => console.log(error),
      );
    }
    this.unsubscribeEus();
  }

  unsubscribeEus(): void {
    if (this.eusSubscription) {
      this.eusSubscription.unsubscribe();
      this.eusSubscription = undefined;
    }
  }

  unsubscribeExecFinished(): void {
    if (this.execFinishedSubscription) {
      this.execFinishedSubscription.unsubscribe();
      this.execFinishedSubscription = undefined;
    }
  }

  end(): void {
    this.executionCardMsg = 'The execution has been finished!';
    this.executionCardSubMsg = 'The associated files will be shown when browser and eus have stopped';
    this.showStopBtn = false;
    this.unsubscribeExecFinished();
    this.deprovideBrowserAndEus();
  }

  forceEnd(): void {
    this.executionCardMsg = 'The execution has been stopped!';
    this.showStopBtn = false;
    this.exTJobExec.result = 'STOPPED';
    this.exTJobExec.endDate = new Date();
    this.externalService.modifyExternalTJobExec(this.exTJobExec);
    this.end();
  }

  setTJobExecutionUrl(label: string): void {
    if (this.exTJobExec) {
      this.tJobExecUrl =
        document.location.origin +
        '/#/external/project/' +
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
      '/#/external/project/' +
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

  loadNextTestLinkCase(): void {
    let nextCase: ExternalTestCaseModel = this.externalTestCases.shift();
    if (nextCase !== undefined) {
      let videoName: string = nextCase.name.split(' ').join('-') + '_' + this.sessionId;
      this.eusService.startRecording(this.sessionId, this.hubContainerName, videoName).subscribe(
        (ok) => {
          this.initCurrentExternalTestExecution(nextCase);
          this.externalService.createExternalTestExecution(this.currentExternalTestExecution).subscribe(
            (savedExTestExec: ExternalTestExecutionModel) => {
              this.currentExternalTestExecution = savedExTestExec;
              this.startTestLinkTestCaseExecution(nextCase.externalId);
            },
            (error: Error) => console.log(error),
          );
        },
        (error: Error) => console.log(error),
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
        if (testCase !== undefined && testCase !== null) {
          // New object to detect on changes
          this.data = {
            testCase: testCase,
            build: build,
            additionalNotes: additionalNotes,
          };
        } else {
          this.saveTLCaseExecution();
        }
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
              this.eusService.stopRecording(this.sessionId, this.hubContainerName).subscribe(
                (ok: Response) => {
                  this.loadNextTestLinkCase();
                },
                (error: Error) => console.log(error),
              );
            },
            (error) => console.log(error),
          );
        }
      },
      (error) => console.log(error),
    );
  }

  finishTJobExecution(): void {
    this.executionCardMsg = 'The execution has been finished!';
    this.executionCardSubMsg = 'The associated files will be shown when browser and eus have stopped';
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
    if (this.tlExecutionForm) {
      return this.tlExecutionForm.saveExecution();
    } else {
      this.finishTJobExecution();
      throw new Error('Error: TestCase View not loaded');
    }
  }

  viewEndedTJobExec(): void {
    this.router.navigate(['/external/projects/', this.exTJob.exProject.id, 'tjob', this.exTJob.id, 'exec', this.exTJobExec.id]);
  }
}
