import { ExecutionFormComponent } from '../../execution/execution-form/execution-form.component';
import { Component, OnInit, ViewChild, HostListener, OnDestroy, ViewContainerRef } from '@angular/core';
import { ExternalTJobModel } from '../../../elastest-etm/external/external-tjob/external-tjob-model';
import {
  ExternalTJobExecModel,
  ExternalTJobExecFinishedModel,
} from '../../../elastest-etm/external/external-tjob-execution/external-tjob-execution-model';
import { ExternalService } from '../../../elastest-etm/external/external.service';
import { Router, Params, ActivatedRoute } from '@angular/router';
import { TestLinkService } from '../../testlink.service';
import { EusService } from '../../../elastest-eus/elastest-eus.service';
import { ExternalTestCaseModel } from '../../../elastest-etm/external/external-test-case/external-test-case-model';
import { ExternalTestExecutionModel } from '../../../elastest-etm/external/external-test-execution/external-test-execution-model';
import { BuildModel } from '../../models/build-model';
import { IExternalExecutionSaveModel } from '../../../elastest-etm/external/models/external-execution-save.model';
import { TestCaseExecutionModel } from '../../models/test-case-execution-model';
import { TLTestCaseModel } from '../../models/test-case-model';
import { Observable, Subscription, interval, Subject } from 'rxjs';
import { EtmMonitoringViewComponent } from '../../../elastest-etm/etm-monitoring-view/etm-monitoring-view.component';
import { PullingObjectModel } from '../../../shared/pulling-obj.model';
import { EsmServiceInstanceModel } from '../../../elastest-esm/esm-service-instance.model';
import { EsmService } from '../../../elastest-esm/esm-service.service';
import { TestPlanModel } from '../../models/test-plan-model';
import { EusTestModel } from '../../../elastest-eus/elastest-eus-test-model';
import { TitlesService } from '../../../shared/services/titles.service';
import { sleep, getWarnColor, getErrorColor } from '../../../shared/utils';
import { ElastestRabbitmqService } from '../../../shared/services/elastest-rabbitmq.service';
import { HttpErrorResponse } from '@angular/common/http';
import { TdDialogService } from '@covalent/core';
import { EtmRestClientService } from '../../../shared/services/etm-rest-client.service';

@Component({
  selector: 'testlink-test-plan-execution',
  templateUrl: './test-plan-execution.component.html',
  styleUrls: ['./test-plan-execution.component.scss'],
})
export class TestPlanExecutionComponent implements OnInit, OnDestroy {
  @ViewChild('logsAndMetrics')
  logsAndMetrics: EtmMonitoringViewComponent;
  @ViewChild('tlExecutionForm')
  tlExecutionForm: ExecutionFormComponent;

  params: Params;

  testPlan: TestPlanModel;
  selectedBuild: BuildModel;

  testCases: TLTestCaseModel[] = [];

  exTJob: ExternalTJobModel;
  exTJobExec: ExternalTJobExecModel;
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
  manuallyClosed: boolean = false;

  browserCardMsg: string = 'Loading...';
  browserAndEusLoading: boolean = true;
  browserAndEusDeprovided: boolean = false;

  websocket: WebSocket;
  eusTestModel: EusTestModel = new EusTestModel();

  // Browser
  sessionId: string;
  hubContainerName: string;
  vncBrowserUrl: string;
  autoconnect: boolean = true;
  viewOnly: boolean = false;

  browserName: string = 'chrome';
  browserVersion: string = 'latest';

  platformId: number = 0;

  extraHosts: string[] = [];

  // Files
  showFiles: boolean = false;

  // Others
  data: any;
  tJobExecUrl: string;

  savingAndLoadingTCase: boolean = false;

  disableTLNextBtn: boolean = false;
  execFinished: boolean = false;

  stopping: boolean = false;

  sutUrl: string = '';

  totalCases: number = 0;

  browserFilesToUpload: File | FileList;
  downloadFilePath: string = '';

  // TSS
  serviceInstances: EsmServiceInstanceModel[] = [];
  instancesNumber: number;
  checkTSSInstancesSubscription: Subscription;

  // For development only! RETURN TO FALSE ON COMMIT
  activateGUIDevelopmentMode: boolean = false;

  logErrors: number = 0;
  logWarnings: number = 0;

  errorColor: string = getErrorColor();
  warnColor: string = getWarnColor();

  constructor(
    private externalService: ExternalService,
    public router: Router,
    private route: ActivatedRoute,
    private esmService: EsmService,
    private eusService: EusService,
    private testLinkService: TestLinkService,
    private titlesService: TitlesService,
    private elastestRabbitmqService: ElastestRabbitmqService,
    private _dialogService: TdDialogService,
    private _viewContainerRef: ViewContainerRef,
    private etmRestClientService: EtmRestClientService,
  ) {}

  ngOnInit(): void {
    this.titlesService.setPathName(this.router.routerState.snapshot.url);
    if (!this.activateGUIDevelopmentMode) {
      let queryParams: any = this.router.parseUrl(this.router.url).queryParams;
      if (queryParams) {
        if (queryParams.browserName) {
          this.browserName = queryParams.browserName;
          if (queryParams.browserVersion) {
            this.browserVersion = queryParams.browserVersion;
          }
        }

        if (queryParams.extraHosts) {
          this.extraHosts = queryParams.extraHosts;
        }

        if (queryParams.platform) {
          this.platformId = queryParams.platform;
        }
      }

      if (this.route.params !== null || this.route.params !== undefined) {
        this.route.params.subscribe(
          (params: Params) => {
            this.params = params;
            this.loadPlanAndBuild();
          },
          (error: Error) => console.error(error),
        );
      }
    }
  }

  ngOnDestroy(): void {
    this.alreadyLeave = true;
    this.clear();
  }

  @HostListener('window:beforeunload')
  beforeunloadHandler(): void {
    // On window closed leave session
    this.clear();
  }

  clear(): void {
    if (this.exTJobExec.finished()) {
      this.end();
    } else {
      this.forceEnd();
    }
    this.elastestRabbitmqService.unsubscribeWSDestination();
    this.unsubscribeCheckTssInstances();
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
      (error: Error) => console.log(error),
    );
  }

  /************************************/
  /* *********** External *********** */
  /************************************/

  loadExternalTJob(): void {
    this.testLinkService.getExternalTJobByTestPlanId(this.testPlan.id).subscribe(
      (exTJob: ExternalTJobModel) => {
        this.exTJob = exTJob;
        this.createTJobExecution();
      },
      (error: Error) => console.log(error),
    );
  }

  createTJobExecution(): void {
    this.exTJobExec = new ExternalTJobExecModel();
    this.externalService.createExternalTJobExecutionByExTJobId(this.exTJob.id).subscribe((exTJobExec: ExternalTJobExecModel) => {
      this.exTJobExec = exTJobExec;
      // +1 because EUS
      this.checkFinished();
      this.instancesNumber = this.exTJobExec.exTJob.esmServicesChecked + 1;
      if (this.instancesNumber > 1) {
        this.browserCardMsg = 'Waiting for Test Support Services';
      }

      this.getSupportServicesInstances().subscribe(
        (ok: boolean) => {
          this.logsAndMetrics.initView(this.exTJob, this.exTJobExec);
          this.waitForEus(exTJobExec);
        },
        (error: Error) => console.log(error),
      );
    });
  }

  checkFinished(): void {
    let responseObj: PullingObjectModel = this.externalService.checkTJobExecFinished(
      this.exTJobExec.id,
      this.execFinishedTimer,
      this.execFinishedSubscription,
    );
    this.execFinishedSubscription = responseObj.subscription;

    responseObj.observable.subscribe((externalExecFinished: ExternalTJobExecFinishedModel) => {
      if (!this.stopping) {
        this.exTJobExec.result = externalExecFinished.exec.result;
        this.exTJobExec.resultMsg = externalExecFinished.exec.resultMsg;
        this.exTJobExec.envVars = externalExecFinished.exec.envVars;
      }
      if (externalExecFinished.finished) {
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

        this.startWebSocket(
          this.eusService.getEusWsByHostAndPort(exTJobExec.envVars['ET_EUS_HOST'], exTJobExec.envVars['ET_EUS_PORT']),
        );

        this.initLoadBrowser();
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
            this.startWebSocket(this.eusService.getEusWsByHostAndPort(eus.ip, eus.port));
            this.initLoadBrowser();
          },
          (error: Error) => console.log(error),
        );
      }
    }
  }

  initLoadBrowser(withWait: boolean = true): void {
    if (this.exTJob.withSut()) {
      this.browserCardMsg = 'The session will start once the SuT is ready';
      this.executionCardMsg = 'Waiting for SuT...';
      if (!this.exTJobExec.executing()) {
        sleep(2000).then(() => {
          this.initLoadBrowser(withWait);
        });
      } else {
        this.initSutUrl();
        this.loadBrowser();
      }
    } else {
      this.loadBrowser();
    }
  }

  loadBrowser(): void {
    this.browserCardMsg = 'Waiting for Browser...';
    this.executionCardMsg = 'Just a little more...';
    let extraCapabilities: any = { manualRecording: true, elastestTimeout: 0 };

    extraCapabilities = this.addTssCapabilities(extraCapabilities);
    this.eusService.startSession(this.browserName, this.browserVersion, extraCapabilities, false, this.extraHosts).subscribe(
      (eusTestModel: EusTestModel) => {
        this.eusTestModel = eusTestModel;
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
            this.openSutUrl();
            this.vncBrowserUrl = vncUrl;
            this.browserAndEusLoading = false;
            this.loadTestCases();
          },
          (error: Error) => console.error(error),
        );
      },
      (errorResponse: HttpErrorResponse) => {
        let error: any = errorResponse.error;
        this.forceEnd(true, 'Error on start browser session', 'ERROR', error ? error.stacktraceMessage : '');
        this.eusTestModel.statusMsg = 'Error';
      },
    );
  }

  addTssCapabilities(extraCapabilities: any): any {
    if (this.serviceInstances) {
      for (let instance of this.serviceInstances) {
        if (instance.serviceName.toLowerCase() === 'ess') {
          let httpproxyapiKey: string = 'httpproxyapi';
          if (!instance.urls || !instance.urls.get(httpproxyapiKey)) {
            this.forceEnd(true, 'Error on initialize browser', 'ERROR', 'ESS proxy api url is not available');
          } else {
            let proxyUrl: string = instance.urls.get(httpproxyapiKey).internal;

            if (extraCapabilities['chromeOptions'] === undefined || extraCapabilities['chromeOptions'] === null) {
              extraCapabilities['chromeOptions'] = {};
            }

            if (extraCapabilities['chromeOptions']['args'] === undefined || extraCapabilities['chromeOptions']['args'] === null) {
              extraCapabilities['chromeOptions']['args'] = [];
            }

            extraCapabilities['chromeOptions']['args'].push('--proxy-server=' + proxyUrl);
            let essApiUrl: string = instance.urls.get('api').internal;

            this.etmRestClientService.doPost(essApiUrl + '/start/', { sites: [this.sutUrl] }).subscribe(
              (response: any) => {
                console.log('ESS response:', response);
              },
              (error: Error) => console.log(error),
            );
          }
        }
      }
    }
    return extraCapabilities;
  }

  initSutUrl(): void {
    if (this.exTJob.withSut()) {
      if (this.exTJobExec.envVars['ET_SUT_URL'] !== undefined) {
        this.sutUrl = this.exTJobExec.envVars['ET_SUT_URL'];
      }
    }
  }

  openSutUrl(): void {
    if (this.exTJob.withSut() && this.sutUrl !== '') {
      this.eusService.navigateToUrl(this.sessionId, this.sutUrl).subscribe();
    }
  }

  loadTestCases(): void {
    this.testLinkService
      .getPlanTestCasesByIdAndPlatformIdAndBuildId(this.testPlan.id, this.selectedBuild.id, this.platformId)
      .subscribe((testCases: TLTestCaseModel[]) => {
        this.testCases = testCases;

        this.startExecution();
      });
  }

  startExecution(): void {
    if (this.testCases && this.testCases.length > 0) {
      this.totalCases = this.testCases.length;

      this.setTJobExecutionUrl('Test Plan Execution:');
      this.data = {
        build: this.selectedBuild,
        platform: this.platformId,
      };
      // Load First TCase
      this.loadNextTestLinkCase();
    } else {
      this.forceEnd(true, 'Error on start execution', 'ERROR', 'There are not test cases to execute');
    }
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
        (error: Error) => {
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
        (error: Error) => console.log(error),
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

  end(fromError: boolean = false): void {
    if (this.websocket) {
      this.manuallyClosed = true;
      this.websocket.close();
    }
    if (!fromError) {
      this.executionCardMsg = 'The execution has been finished!';
      this.executionCardSubMsg = 'The associated files will be shown when browser and eus have stopped';
    }
    this.showStopBtn = false;
    this.unsubscribeExecFinished();
    this.deprovideBrowserAndEus();
  }

  forceEnd(
    fromError: boolean = false,
    execCardMsg: string = 'The execution has been stopped!',
    result: string = 'STOPPED',
    resultMsg: string = 'Stopping...',
  ): void {
    this.stopping = true;
    this.executionCardMsg = execCardMsg;
    this.showStopBtn = false;
    this.exTJobExec.result = result;
    this.exTJobExec.resultMsg = resultMsg;
    this.exTJobExec.endDate = new Date();
    this.externalService.modifyExternalTJobExec(this.exTJobExec).subscribe();
    this.end(fromError);
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

  /************************************/
  /* *********** TestLink *********** */
  /************************************/

  loadNextTestLinkCase(): void {
    let nextTLCase: TLTestCaseModel = this.testCases.shift();
    if (nextTLCase !== undefined) {
      // Load External Test Case
      this.testLinkService.getExternalTestCaseByTestCaseId(nextTLCase.id).subscribe(
        (exTestCase: ExternalTestCaseModel) => {
          let videoName: string = nextTLCase.name.split(' ').join('-') + '_' + this.sessionId;
          // Start recording
          this.eusService.startRecording(this.sessionId, this.hubContainerName, videoName).subscribe(
            (ok) => {
              this.initCurrentExternalTestExecution(exTestCase);
              this.externalService.createExternalTestExecution(this.currentExternalTestExecution).subscribe(
                (savedExTestExec: ExternalTestExecutionModel) => {
                  this.currentExternalTestExecution = savedExTestExec;
                  this.startTestLinkTestCaseExecution(nextTLCase);
                },
                (error: Error) => console.log(error),
              );
            },
            (error: Error) => console.log(error),
          );
        },
        (error: Error) => {
          console.log(error);
          this.forceEnd(true, 'Error on load test case ' + nextTLCase.id, 'ERROR', error ? error.message : '');
        },
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

  startTestLinkTestCaseExecution(testCase: TLTestCaseModel): void {
    let build: BuildModel = this.data.build;
    let additionalNotes: string = this.getCurrentTestExecutionUrl('Test Case Execution:');
    additionalNotes += this.tJobExecUrl;

    if (testCase !== undefined && testCase !== null) {
      // New object to detect on changes
      this.data = {
        testCase: testCase,
        build: build,
        platform: this.platformId,
        additionalNotes: additionalNotes,
      };
      this.savingAndLoadingTCase = false;
    } else {
      this.saveTLCaseExecution();
    }
  }

  saveTLCaseExecution(): void {
    this.savingAndLoadingTCase = true;
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
                (ok: any) => {
                  this.loadNextTestLinkCase();
                },
                (error: Error) => {
                  console.log(error);
                  this.savingAndLoadingTCase = false;
                },
              );
            },
            (error: Error) => {
              console.log(error);
              this.savingAndLoadingTCase = false;
            },
          );
        }
      },
      (error: Error) => {
        console.log(error);
        this.savingAndLoadingTCase = false;
      },
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
      (error: Error) => {
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

  startWebSocket(wsUrl: string): void {
    if (!this.websocket && wsUrl !== undefined) {
      this.websocket = new WebSocket(wsUrl);

      this.websocket.onopen = () => {
        this.websocket.send('getSessions');
        this.websocket.send('getRecordings');
      };

      this.websocket.onclose = () => this.reconnect(wsUrl);

      this.websocket.onmessage = (message: any) => {
        let json: any = JSON.parse(message.data);
        if (json.newSession) {
          if (this.eusTestModel !== undefined) {
            this.eusTestModel.status = json.newSession.status;
            this.eusTestModel.statusMsg = json.newSession.statusMsg;
          }
        } else if (json.removeSession) {
        }
      };
    }
  }

  reconnect(wsUrl: string): void {
    if (!this.manuallyClosed) {
      // try to reconnect websocket in 5 seconds
      setTimeout(() => {
        console.log('Trying to reconnect to EUS WS');
        this.startWebSocket(wsUrl);
      }, 5000);
    }
  }

  onUploadBrowserFile(files: FileList | File): void {
    if (files instanceof FileList) {
      this.eusService.uploadFilesToSession(this.sessionId, files).subscribe(
        (responseObj: object) => {
          if (!responseObj || responseObj['errors'].length > 0) {
            this.externalService.popupService.openSnackBar('An error has occurred in uploading some files');
            for (let error of responseObj['errors']) {
              console.log(error);
            }
          } else {
            this.externalService.popupService.openSnackBar('All files has been uploaded succesfully');
          }
        },
        (error: Error) => {
          console.log(error);
          this.externalService.popupService.openSnackBar('An error has occurred in uploading files');
        },
      );
    } else if (files instanceof File) {
      this.eusService.uploadFileToSession(this.sessionId, files).subscribe(
        (response: any) => {
          this.externalService.popupService.openSnackBar('The file has been uploaded succesfully');
        },
        (error: Error) => {
          console.log(error);
          this.externalService.popupService.openSnackBar('An error has occurred in uploading file');
        },
      );
    }
  }

  downloadFile(): void {
    this._dialogService
      .openPrompt({
        message: 'Pelase, insert the complete path to the file in the Browser context',
        disableClose: true,
        viewContainerRef: this._viewContainerRef,
        title: 'Download a file',
        value: '',
        cancelButton: 'Cancel',
        acceptButton: 'Download',
        width: '400px',
      })
      .afterClosed()
      .subscribe((path: string) => {
        if (path) {
          this.eusService.downloadFileFromSession(this.sessionId, path).subscribe(
            (ok: boolean) => {
              if (!ok) {
                this.externalService.popupService.openSnackBar('Error on get file');
              }
            },
            (error: Error) => {
              console.log(error);
              this.externalService.popupService.openSnackBar('Error on get file');
            },
          );
        }
      });
  }

  getSupportServicesInstances(): Observable<boolean> {
    let _obs: Subject<boolean> = new Subject<boolean>();
    let obs: Observable<boolean> = _obs.asObservable();

    let timer: Observable<number> = interval(2200);
    if (this.checkTSSInstancesSubscription === null || this.checkTSSInstancesSubscription === undefined) {
      this.checkTSSInstancesSubscription = timer.subscribe(() => {
        this.esmService.getSupportServicesInstancesByExternalTJobExec(this.exTJobExec).subscribe(
          (serviceInstances: EsmServiceInstanceModel[]) => {
            if (serviceInstances.length === this.instancesNumber || this.exTJobExec.finished()) {
              this.unsubscribeCheckTssInstances();
              this.serviceInstances = [...serviceInstances];
              _obs.next(true);
            }
          },
          (error: Error) => {
            console.log(error);
            if (this.instancesNumber === 1) {
              _obs.next(true);
            }
          },
        );
      });
    }
    return obs;
  }

  unsubscribeCheckTssInstances(): void {
    if (this.checkTSSInstancesSubscription !== undefined) {
      this.checkTSSInstancesSubscription.unsubscribe();
      this.checkTSSInstancesSubscription = undefined;
    }
  }

  getLogsErrors(): number {
    this.logErrors = this.logsAndMetrics.getLogsErrors();
    return this.logErrors;
  }

  getLogsWarnings(): number {
    this.logWarnings = this.logsAndMetrics.getLogsWarnings();
    return this.logWarnings;
  }
}
