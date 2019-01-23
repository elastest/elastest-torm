import { CaseExecutionViewComponent } from './case-execution-view/case-execution-view.component';
import { Component, OnInit, ViewChild, HostListener } from '@angular/core';
import { OnDestroy } from '@angular/core/src/metadata/lifecycle_hooks';
import { ExternalDataModel } from '../../models/external-data-model';
import { EusService } from '../../../../elastest-eus/elastest-eus.service';
import { ExternalService } from '../../external.service';
import { CompleteUrlObj } from '../../../../shared/utils';
import { IExternalExecution } from '../../models/external-execution-interface';
import { ExternalTJobModel } from '../../external-tjob/external-tjob-model';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { ServiceType } from '../../external-project/external-project-model';
import { ExternalTJobExecModel, ExternalTJobExecFinishedModel } from '../external-tjob-execution-model';
import { EsmService } from '../../../../elastest-esm/esm-service.service';
import { Subscription } from 'rxjs/Subscription';
import { Observable } from 'rxjs/Observable';
import { EsmServiceInstanceModel } from '../../../../elastest-esm/esm-service-instance.model';
import { EtmMonitoringViewComponent } from '../../../etm-monitoring-view/etm-monitoring-view.component';
import { PullingObjectModel } from '../../../../shared/pulling-obj.model';
import { EusTestModel } from '../../../../elastest-eus/elastest-eus-test-model';

@Component({
  selector: 'etm-external-tjob-execution-new',
  templateUrl: './external-tjob-execution-new.component.html',
  styleUrls: ['./external-tjob-execution-new.component.scss'],
})
export class ExternalTjobExecutionNewComponent implements OnInit, OnDestroy {
  @ViewChild('logsAndMetrics')
  logsAndMetrics: EtmMonitoringViewComponent;
  @ViewChild('etmCaseExecutionView')
  etmCaseExecutionView: CaseExecutionViewComponent;

  exTJob: ExternalTJobModel;
  exTJobExec: ExternalTJobExecModel;
  exTJobExecFinish: boolean = false;
  execFinishedTimer: Observable<number>;
  execFinishedSubscription: Subscription;

  showStopBtn: boolean = false;

  // EUS
  eusTimer: Observable<number>;
  eusSubscription: Subscription;
  eusInstanceId: string;
  eusUrl: string;

  browserLoadingMsg: string = 'Loading...';
  // Browser
  sessionId: string;

  vncBrowserUrl: string;
  vncHost: string;
  vncPort: string;
  vncPassword: string;
  autoconnect: boolean = true;
  viewOnly: boolean = false;

  // Files
  showFiles: boolean = false;

  constructor(
    private externalService: ExternalService,
    public router: Router,
    private eusService: EusService,
    private esmService: EsmService,
    private route: ActivatedRoute,
  ) {
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params.subscribe((params: Params) => {
        this.loadExternalTJob(params['tJobId']);
      });
    }
  }

  ngOnInit(): void {}

  loadExternalTJob(id: number): void {
    this.externalService.getExternalTJobById(id).subscribe(
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

    responseObj.observable.subscribe((externalExecFinished: ExternalTJobExecFinishedModel) => {
      this.exTJobExec.result = externalExecFinished.exec.result;
      this.exTJobExec.resultMsg = externalExecFinished.exec.resultMsg;
      if (externalExecFinished.finished) {
        this.end();
      }
    });
  }

  waitForEus(exTJobExec: ExternalTJobExecModel): void {
    this.browserLoadingMsg = 'Waiting for EUS...';
    if (exTJobExec.envVars && exTJobExec.envVars['EUS_INSTANCE_ID']) {
      this.eusInstanceId = exTJobExec.envVars['EUS_INSTANCE_ID'];
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
        (error: Error) => console.log(error),
      );
    }
  }

  loadChromeBrowser(): void {
    this.browserLoadingMsg = 'Waiting for Browser...';
    this.eusService.startSession('chrome', '62').subscribe(
      (eusTestModel: EusTestModel) => {
        this.sessionId = eusTestModel.id;
        this.showStopBtn = true;
        this.exTJobExec.envVars['BROWSER_SESSION_ID'] = this.sessionId;
        let browserLog: any = this.exTJobExec.getBrowserLogObj();
        if (browserLog) {
          this.logsAndMetrics.addMoreFromObj(browserLog);
        }
        this.eusService.getVncUrlSplitted(this.sessionId).subscribe(
          (urlObj: CompleteUrlObj) => {
            this.vncHost = urlObj.queryParams.host;
            this.vncPort = urlObj.queryParams.port;
            this.vncPassword = urlObj.queryParams.password;
            this.vncBrowserUrl = urlObj.href;
          },
          (error: Error) => console.error(error),
        );
      },
      (error: Error) => console.log(error),
    );
  }

  ngOnDestroy(): void {
    this.end();
  }

  @HostListener('window:beforeunload')
  beforeunloadHandler() {
    // On window closed leave session
    this.end();
  }

  deprovideBrowserAndEus(): void {
    if (this.sessionId !== undefined) {
      this.browserLoadingMsg = 'Shutting down Browser...';
      this.vncBrowserUrl = undefined;
      this.eusService.stopSession(this.sessionId).subscribe(
        (ok: any) => {
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
      this.browserLoadingMsg = 'Shutting down EUS...';
      this.esmService.deprovisionExternalTJobExecServiceInstance(this.eusInstanceId, this.exTJobExec.id).subscribe(
        () => {
          this.browserLoadingMsg = 'FINISHED';
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

  end(): void {
    this.showStopBtn = false;
    this.deprovideBrowserAndEus();
    this.unsubscribeExecFinished();
  }

  forceEnd(): void {
    this.showStopBtn = false;
    this.exTJobExec.result = 'STOPPED';
    this.exTJobExec.endDate = new Date();
    this.externalService.modifyExternalTJobExec(this.exTJobExec);
    this.end();
  }
}
