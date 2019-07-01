import { ExecutionFormComponent } from '../../execution/execution-form/execution-form.component';
import { Component, OnInit, ViewChild, HostListener, OnDestroy } from '@angular/core';
import { ExternalTJobModel } from '../../../elastest-etm/external/external-tjob/external-tjob-model';
import {
  ExternalTJobExecModel,
  ExternalTJobExecFinishedModel,
} from '../../../elastest-etm/external/external-tjob-execution/external-tjob-execution-model';
import { ExternalService } from '../../../elastest-etm/external/external.service';
import { Router, Params, ActivatedRoute } from '@angular/router';
import { TestLinkService } from '../../testlink.service';
import { BrowserVersionModel } from '../../../elastest-eus/elastest-eus.service';
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
import { TitlesService } from '../../../shared/services/titles.service';
import { sleep } from '../../../shared/utils';
import { ElastestRabbitmqService } from '../../../shared/services/elastest-rabbitmq.service';
import { HttpErrorResponse } from '@angular/common/http';
import { EtmRestClientService } from '../../../shared/services/etm-rest-client.service';
import { EusBowserSyncModel } from '../../../elastest-eus/elastest-eus-browser-sync.model';
import { BrowserCardComponentComponent } from '../../../elastest-eus/browser-card-component/browser-card-component.component';
import { CrossbrowserComponentComponent } from '../../../elastest-eus/crossbrowser-component/crossbrowser-component.component';

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
  @ViewChild('singleBrowserCard')
  singleBrowserCard: BrowserCardComponentComponent;
  @ViewChild('crossbrowser')
  crossbrowser: CrossbrowserComponentComponent;

  // For development only! RETURN TO FALSE ON COMMIT
  activateGUIDevelopmentMode: boolean = false;

  params: Params;

  resume: boolean = false;
  exTJobExecId: string;

  testPlan: TestPlanModel;
  selectedBuild: BuildModel;

  testCases: TLTestCaseModel[] = [];

  exTJob: ExternalTJobModel;
  exTJobExec: ExternalTJobExecModel;
  currentExternalTestCase: ExternalTestCaseModel;
  currentExternalTestExecution: ExternalTestExecutionModel;

  exTJobExecFinish: boolean = false;
  execFinishedTimer: Observable<number>;
  execFinishedSubscription: Subscription;

  alreadyLeave: boolean = false;

  showStopAndPauseBtns: boolean = false;

  executionCardMsg: string = 'Loading...';
  executionCardSubMsg: string = '';

  // EUS
  eusTimer: Observable<number>;
  eusSubscription: Subscription;
  eusInstanceId: string;

  browserAndEusDeprovided: boolean = false;

  browserName: string = 'chrome';
  browserVersion: string = 'latest';
  browserList: BrowserVersionModel[];
  crossbrowserEnabled: boolean = false;

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

  // TSS
  serviceInstances: EsmServiceInstanceModel[] = [];
  instancesNumber: number;
  checkTSSInstancesSubscription: Subscription;

  startScan: boolean = true;

  constructor(
    private externalService: ExternalService,
    public router: Router,
    private route: ActivatedRoute,
    private esmService: EsmService,
    private testLinkService: TestLinkService,
    private titlesService: TitlesService,
    private elastestRabbitmqService: ElastestRabbitmqService,
    private etmRestClientService: EtmRestClientService,
  ) {}

  ngOnInit(): void {
    this.titlesService.setPathName(this.router.routerState.snapshot.url);
    if (!this.activateGUIDevelopmentMode) {
      let queryParams: any = this.router.parseUrl(this.router.url).queryParams;
      if (queryParams) {
        // Single browser
        if (queryParams.browserName) {
          this.browserName = queryParams.browserName;
          if (queryParams.browserVersion) {
            this.browserVersion = queryParams.browserVersion;
          }
        }

        // Cross browser
        if (queryParams.browserList) {
          let browserVersionPairList: string[] = queryParams.browserList.split(',');
          this.browserList = [];
          for (let browserVersionPair of browserVersionPairList) {
            let newBrowser: BrowserVersionModel = new BrowserVersionModel(browserVersionPair);
            this.browserList.push(newBrowser);
          }
          this.crossbrowserEnabled = true;
        }

        if (queryParams.extraHosts) {
          this.extraHosts = queryParams.extraHosts;
        }

        if (queryParams.platform) {
          this.platformId = queryParams.platform;
        }

        if (queryParams.fromSaved) {
          this.resume = queryParams.fromSaved;
        }

        if (queryParams.exTJobExecId) {
          this.exTJobExecId = queryParams.exTJobExecId;
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
    if (this.exTJobExec.finished() || this.exTJobExec.paused()) {
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
        this.initExternalTJobExecution();
      },
      (error: Error) => console.log(error),
    );
  }

  initExternalTJobExecution(): void {
    this.exTJobExec = new ExternalTJobExecModel();

    let initExtTJobExecMethod: Observable<ExternalTJobExecModel>;

    // Resume paused Execution
    if (this.resume) {
      initExtTJobExecMethod = this.externalService.resumeExternalTJobExecution(this.exTJobExecId);
    } else {
      // Create new Execution
      initExtTJobExecMethod = this.externalService.createExternalTJobExecutionByExTJobId(this.exTJob.id);
    }

    initExtTJobExecMethod.subscribe((exTJobExec: ExternalTJobExecModel) => {
      this.exTJobExec = exTJobExec;

      let executionConfig: any = {
        testProjectId: this.params['projectId'],
        buildId: this.selectedBuild.id,
        planId: this.testPlan.id,
        platformId: this.platformId,
        extraHosts: this.extraHosts,
        browserName: this.browserName,
        browserVersion: this.browserVersion,
      };

      this.exTJobExec.executionConfigObj = executionConfig;
      this.exTJobExec.executionConfig = JSON.stringify(executionConfig);

      // +1 because EUS
      this.checkFinished();
      this.instancesNumber = this.exTJobExec.exTJob.esmServicesChecked + 1;
      if (this.instancesNumber > 1) {
        this.updateBrowsersCardMsg('Waiting for Test Support Services');
      }

      this.getSupportServicesInstances().subscribe(
        (ok: boolean) => {
          this.logsAndMetrics.initView(this.exTJob, this.exTJobExec);
          this.setLogsAndMetricsToBrowsers();
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

  initEusDataAndStartWS(eusIp: string, eusPort: string | number, eusUrl: string): void {
    if (this.crossbrowserEnabled && this.crossbrowser) {
      this.crossbrowser.initEusData(eusIp, eusPort, eusUrl);
      // Websocket is started for each browser in crossbrowser component
    } else if (this.singleBrowserCard) {
      this.singleBrowserCard.initEusData(eusIp, eusPort, eusUrl);
      this.singleBrowserCard.startWebSocket();
    }
  }

  waitForEus(exTJobExec: ExternalTJobExecModel): void {
    this.executionCardMsg = 'Please wait while the browser is loading. This may take a while.';
    this.updateBrowsersCardMsg('Waiting for EUS...');
    if (exTJobExec.envVars && exTJobExec.envVars['EUS_INSTANCE_ID']) {
      this.eusInstanceId = exTJobExec.envVars['EUS_INSTANCE_ID'];

      if (exTJobExec.envVars['ET_EUS_API']) {
        // If EUS is shared (started on init)
        this.initEusDataAndStartWS(
          exTJobExec.envVars['ET_EUS_HOST'],
          exTJobExec.envVars['ET_EUS_PORT'],
          exTJobExec.envVars['ET_EUS_API'],
        );

        this.waitBeforeLoadBrowsers();
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
            this.initEusDataAndStartWS(eus.ip, eus.port + '', eus.apiUrl);
            this.waitBeforeLoadBrowsers();
          },
          (error: Error) => console.log(error),
        );
      }
    }
  }

  waitBeforeLoadBrowsers(withWait: boolean = true): void {
    if (this.exTJob.withSut()) {
      this.updateBrowsersCardMsg('The session will start once the SuT is ready');
      this.executionCardMsg = 'Waiting for SuT...';
      if (!this.exTJobExec.executing()) {
        sleep(2000).then(() => {
          this.waitBeforeLoadBrowsers(withWait);
        });
      } else {
        this.initSutUrl();
        this.startBrowsers();
      }
    } else {
      this.startBrowsers();
    }
  }

  startBrowsers(): void {
    this.updateBrowsersCardMsg('Waiting for Browser(s)...');
    this.executionCardMsg = 'Just a little more...';
    let extraCapabilities: any = { manualRecording: true, elastestTimeout: 0 };
    extraCapabilities = this.addTssCapabilities(extraCapabilities);

    // CrossBrowser
    if (this.crossbrowserEnabled) {
      this.startCrossbrowser(extraCapabilities);
    } else {
      // Single browser
      this.startSingleBrowser(extraCapabilities);
    }
  }

  startSingleBrowser(extraCapabilities: any): void {
    this.singleBrowserCard
      .startSession(this.browserName, this.browserVersion, extraCapabilities, false, this.extraHosts)
      .subscribe(
        (sessionId: any) => {
          this.exTJobExec.envVars['BROWSER_SESSION_ID'] = sessionId;
          this.openSutUrl();
          this.initAfterStartBrowsers();
        },
        (errorResponse: HttpErrorResponse | Error) => {
          let errorMsg: any;
          if (errorResponse instanceof HttpErrorResponse) {
            errorMsg = errorResponse.error.stacktraceMessage;
          } else {
            errorMsg = errorResponse.stack;
          }
          this.forceEnd(true, 'Error on start browser session', 'ERROR', errorMsg ? errorMsg : '');
        },
      );
  }

  startCrossbrowser(extraCapabilities: any): void {
    this.crossbrowser.startCrossbrowser(extraCapabilities, this.browserList, this.sutUrl, false, this.extraHosts).subscribe(
      (browserSync: EusBowserSyncModel) => {
        this.exTJobExec.envVars['CROSSBROWSER_SESSION_ID'] = browserSync.identifier;
        this.initAfterStartBrowsers();
      },
      (errorResponse: HttpErrorResponse | Error) => {
        let errorMsg: any;
        if (errorResponse instanceof HttpErrorResponse) {
          errorMsg = errorResponse.error.stacktraceMessage;
        } else {
          errorMsg = errorResponse.stack;
        }
        this.forceEnd(true, 'Error on start crossbrowser session', 'ERROR', errorMsg ? errorMsg : '');
      },
    );
  }

  initAfterStartBrowsers(): void {
    this.showStopAndPauseBtns = true;
    let browserLog: any = this.exTJobExec.getBrowserLogObj();
    if (browserLog) {
      this.logsAndMetrics.addMoreFromObj(browserLog);
    }
    this.loadTestCases();
  }

  addTssCapabilities(extraCapabilities: any): any {
    if (this.serviceInstances) {
      for (let instance of this.serviceInstances) {
        if (instance.serviceName.toLowerCase() === 'ess') {
          let httpproxyapiKey: string = 'httpproxyapi';
          if (!instance.urls || !instance.urls.get(httpproxyapiKey)) {
            this.forceEnd(true, 'Error on initialize browser(s)', 'ERROR', 'ESS proxy api url is not available');
          } else {
            let proxyUrl: string = instance.urls.get(httpproxyapiKey).internal;
            extraCapabilities['proxy'] = { httpProxy: proxyUrl, proxyType: 'MANUAL' };
            if (extraCapabilities['firstMatch'] === undefined || extraCapabilities['firstMatch'] === null) {
              extraCapabilities['firstMatch'] = [{ proxy: { httpProxy: proxyUrl, proxyType: 'manual' } }];
            } else {
              extraCapabilities['firstMatch'][0]['proxy'] = { httpProxy: proxyUrl, proxyType: 'manual' };
            }

            let essApiUrl: string = instance.urls.get('api').internal;
            this.startEssScan(essApiUrl);
          }
        }
      }
    }
    return extraCapabilities;
  }

  startEssScan(essApiUrl: string): void {
    if (!this.startScan) {
      sleep(5000).then(() => {
        this.startEssScan(essApiUrl);
      });
    } else {
      this.etmRestClientService.doPost(essApiUrl + '/start/', { sites: [this.sutUrl] }).subscribe(
        (response: any) => {
          console.log('ESS start response:', response);

          // Checking the status of the scan
          if (response && response.status === 'starting-ess') {
            this.checkEssScanStatus(essApiUrl);
          }
        },
        (error: Error) => console.log(error),
      );
    }
  }

  checkEssScanStatus(essApiUrl: string): void {
    this.etmRestClientService.doGet(essApiUrl + '/status/').subscribe(
      (response: any) => {
        console.log('ESS status response', response);
        if (response && response.status === 'not-yet') {
          sleep(5000).then(() => {
            this.checkEssScanStatus(essApiUrl);
          });
        }
      },
      (error: Error) => {
        console.error(error);
      },
    );
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
      if (this.singleBrowserCard && !this.crossbrowser) {
        this.singleBrowserCard.navigateToUrl(this.sutUrl).subscribe();
      }
    }
  }

  loadTestCases(): void {
    this.testLinkService
      .getPlanTestCasesByIdAndPlatformIdAndBuildId(this.testPlan.id, this.selectedBuild.id, this.platformId)
      .subscribe((testCases: TLTestCaseModel[]) => {
        this.testCases = testCases;

        if (this.testCases) {
          this.totalCases = this.testCases.length;
        }

        if (this.resume) {
          let lastExecutedTCaseId: string = this.exTJobExec.lastExecutedTCaseId;
          let lastExecutedTCasePosition: number = 0;

          if (lastExecutedTCaseId && this.testCases) {
            for (let testCase of this.testCases) {
              if (testCase.id + '' === lastExecutedTCaseId) {
                break;
              }
              lastExecutedTCasePosition++;
            }
          }

          this.testCases = this.testCases.slice(lastExecutedTCasePosition + 1);
        }

        this.startExecution();
      });
  }

  startExecution(): void {
    if (this.testCases && this.testCases.length > 0) {
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

  stopBrowsersAndDeprovideEus(): void {
    if (this.singleBrowserCard !== undefined || this.crossbrowser !== undefined) {
      this.updateBrowsersCardMsg('Shutting down Browser(s)...');

      let stopMethod: Observable<any>;
      if (this.crossbrowserEnabled && this.crossbrowser) {
        stopMethod = this.crossbrowser.stopCrossbrowser();
      } else if (this.singleBrowserCard) {
        stopMethod = this.singleBrowserCard.stopBrowser();
      } else {
        this.deprovisionEUS();
        return;
      }

      stopMethod.subscribe(
        (ok) => {
          this.deprovisionEUS();
        },
        (error: Error) => {
          this.deprovisionEUS();
        },
      );
    } else {
      this.deprovisionEUS();
    }
  }

  deprovisionEUS(): void {
    if (this.eusInstanceId && this.exTJobExec) {
      this.updateBrowsersCardMsg('Shutting down EUS...');
      this.esmService.deprovisionExternalTJobExecServiceInstance(this.eusInstanceId, this.exTJobExec.id).subscribe(
        () => {
          this.updateBrowsersCardMsg('FINISHED');
          this.browserAndEusDeprovided = true;
          this.showFiles = true;
        },
        (error: Error) => console.log(error),
      );
    }
    this.unsubscribeEus();
  }

  updateBrowsersCardMsg(msg: string): void {
    if (this.crossbrowserEnabled && this.crossbrowser) {
      this.crossbrowser.updateMsg(msg);
    } else if (this.singleBrowserCard) {
      this.singleBrowserCard.updateMsg(msg);
    }
  }

  stopWebsocket(): void {
    if (this.crossbrowserEnabled && this.crossbrowser) {
      this.crossbrowser.stopWebsocket();
    } else if (this.singleBrowserCard) {
      this.singleBrowserCard.stopWebsocket();
    }
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

  end(fromError: boolean = false, fromPause: boolean = false): void {
    this.stopWebsocket();
    if (!fromError && !fromPause) {
      this.executionCardMsg = 'The execution has been finished!';
      this.executionCardSubMsg = 'The associated files will be shown when browser(s) and eus have stopped';
    }
    this.showStopAndPauseBtns = false;
    this.unsubscribeExecFinished();
    this.stopBrowsersAndDeprovideEus();
  }

  forceEnd(
    fromError: boolean = false,
    execCardMsg: string = 'The execution has been stopped!',
    result: string = 'STOPPED',
    resultMsg: string = 'Stopping...',
  ): void {
    this.stopping = true;
    this.executionCardMsg = execCardMsg;
    this.showStopAndPauseBtns = false;
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
    // Set last Executed TCase Id for pause
    if (this.currentExternalTestCase !== undefined && this.currentExternalTestCase !== null) {
      this.exTJobExec.lastExecutedTCaseId = this.currentExternalTestCase.externalId;
    }

    let nextTLCase: TLTestCaseModel = this.testCases.shift();
    if (nextTLCase !== undefined) {
      // Load External Test Case
      this.testLinkService.getExternalTestCaseByTestCaseId(nextTLCase.id).subscribe(
        (exTestCase: ExternalTestCaseModel) => {
          this.currentExternalTestCase = exTestCase;
          let currentVideoNamePrefix: string = nextTLCase.name.split(' ').join('-') + '_';

          // Start recording
          let startRecordingMethod: Observable<any>;
          if (this.crossbrowserEnabled && this.crossbrowser) {
            startRecordingMethod = this.crossbrowser.startRecording(currentVideoNamePrefix);
          } else if (this.singleBrowserCard) {
            startRecordingMethod = this.singleBrowserCard.startRecording(currentVideoNamePrefix);
          }

          startRecordingMethod.subscribe(
            (ok) => {
              this.initCurrentExternalTestExecution(exTestCase);

              this.externalService.createExternalTestExecution(this.currentExternalTestExecution).subscribe(
                (savedExTestExec: ExternalTestExecutionModel) => {
                  this.currentExternalTestExecution = savedExTestExec;
                  this.startTestLinkTestCaseExecution(nextTLCase);
                },
                (error: Error) => {
                  if (this.resume) {
                    // Is Test Execution paused and tmp saved
                    this.externalService
                      .getExternalTestExecByExternalIdAndSystemId(
                        this.currentExternalTestExecution.externalId,
                        this.currentExternalTestExecution.externalSystemId,
                      )
                      .subscribe((savedExTestExec: ExternalTestExecutionModel) => {
                        this.currentExternalTestExecution = savedExTestExec;
                        this.currentExternalTestExecution.startDate = new Date();

                        this.startTestLinkTestCaseExecution(nextTLCase);
                      });
                  } else {
                    console.log(error);
                    this.forceEnd();
                  }
                },
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

              let stopRecordingMethod: Observable<any>;
              if (this.crossbrowserEnabled && this.crossbrowser) {
                stopRecordingMethod = this.crossbrowser.stopRecording();
              } else if (this.singleBrowserCard) {
                stopRecordingMethod = this.singleBrowserCard.stopRecording();
              }

              stopRecordingMethod.subscribe(
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
    // this.startScan = true;

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

  pauseExecution(): void {
    this.showStopAndPauseBtns = false;
    this.stopping = true;
    this.exTJobExec.result = 'PAUSED';
    this.exTJobExec.resultMsg = 'Pausing...';
    this.executionCardMsg = 'Pausing...';
    this.singleBrowserCard.stopRecording().subscribe(
      (ok: any) => {
        let deleteRecordingMethod: Observable<any>;
        if (this.crossbrowserEnabled && this.crossbrowser) {
          deleteRecordingMethod = this.crossbrowser.deleteRecording();
        } else if (this.singleBrowserCard) {
          deleteRecordingMethod = this.singleBrowserCard.deleteRecording();
        }

        deleteRecordingMethod.subscribe((ok: any) => {
          this.end(false, true);
          this.disableTLNextBtn = true;
          this.execFinished = true;
          this.externalService.getExternalTestExecsByExternalTJobExecId(this.exTJobExec.id).subscribe(
            (exTestExecs: ExternalTestExecutionModel[]) => {
              this.exTJobExec.exTestExecs = exTestExecs;

              this.exTJobExec.endDate = new Date();
              this.exTJobExec.exTestExecs = []; // TODO fix No _valueDeserializer assigned
              this.exTJobExec.exTJob.exTestCases = []; // TODO fix No _valueDeserializer assigned

              this.externalService.modifyExternalTJobExec(this.exTJobExec).subscribe();
              this.executionCardMsg = 'The execution has been paused!';
              this.exTJobExec.resultMsg = 'Paused';
              this.externalService.modifyExternalTJobExec(this.exTJobExec).subscribe();
              this.executionCardSubMsg = '';
            },
            (error: Error) => {
              console.log(error);
              this.externalService.modifyExternalTJobExec(this.exTJobExec).subscribe();
              this.executionCardMsg = 'The execution has been paused!';
              this.exTJobExec.resultMsg = 'Paused';
              this.externalService.modifyExternalTJobExec(this.exTJobExec).subscribe();
              this.executionCardSubMsg = '';
            },
          );
        });
      },
      (error: Error) => {
        console.log(error);
        this.savingAndLoadingTCase = false;
      },
    );

    // Save last executed tc id
    // Set paused status
    // Stop browser, eus and execution (status paused)
  }

  getSupportServicesInstances(): Observable<boolean> {
    let _obs: Subject<boolean> = new Subject<boolean>();
    let obs: Observable<boolean> = _obs.asObservable();

    let timer: Observable<number> = interval(2200);
    if (this.checkTSSInstancesSubscription === null || this.checkTSSInstancesSubscription === undefined) {
      this.checkTSSInstancesSubscription = timer.subscribe(() => {
        this.esmService.getSupportServicesInstancesByExternalTJobExec(this.exTJobExec).subscribe(
          (serviceInstances: EsmServiceInstanceModel[]) => {
            if (serviceInstances.length === this.instancesNumber || this.exTJobExec.finished() || this.exTJobExec.paused()) {
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

  viewEndedTJobExec(): void {
    this.router.navigate(['/external/projects/', this.exTJob.exProject.id, 'tjob', this.exTJob.id, 'exec', this.exTJobExec.id]);
  }

  resizeBrowsers($event): void {
    if (this.crossbrowserEnabled && this.crossbrowser) {
      this.crossbrowser.resizeBrowsers($event);
    } else if (this.singleBrowserCard) {
      this.singleBrowserCard.resizeBrowsers($event);
    }
  }

  showBrowserMsgSpinner(): boolean {
    return (
      this.exTJobExec &&
      (!this.exTJobExec.finished() || (this.exTJobExec.finished() && !this.browserAndEusDeprovided)) &&
      (!this.exTJobExec.paused() || (this.exTJobExec.paused() && !this.browserAndEusDeprovided))
    );
  }

  setLogsAndMetricsToBrowsers(): void {
    if (this.crossbrowserEnabled && this.crossbrowser) {
      this.crossbrowser.setLogsAndMetrics(this.logsAndMetrics);
    } else if (this.singleBrowserCard) {
      this.singleBrowserCard.setLogsAndMetrics(this.logsAndMetrics);
    }
  }

  showExecution(): boolean {
    let showExecution: boolean = this.exTJob && this.exTJobExec && !this.exTJobExec.finished() && this.exTJobExec.executing();
    if (this.crossbrowserEnabled) {
      showExecution = showExecution && this.crossbrowser !== undefined;
    } else {
      showExecution = showExecution && this.singleBrowserCard && !this.singleBrowserCard.browserAndEusLoading;
    }
    return showExecution;
  }
}
