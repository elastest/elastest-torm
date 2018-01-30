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
import { ExternalTJobExecModel } from '../external-tjob-execution-model';

@Component({
  selector: 'etm-external-tjob-execution-new',
  templateUrl: './external-tjob-execution-new.component.html',
  styleUrls: ['./external-tjob-execution-new.component.scss'],
})
export class ExternalTjobExecutionNewComponent implements OnInit, OnDestroy {
  exTJob: ExternalTJobModel;
  exTJobExec: ExternalTJobExecModel;
  ready: boolean = false;

  // Browser
  sessionId: string;

  vncBrowserUrl: string;
  vncHost: string;
  vncPort: string;
  vncPassword: string;
  autoconnect: boolean = true;
  viewOnly: boolean = false;

  constructor(
    private externalService: ExternalService,
    public router: Router,
    private eusService: EusService,
    private route: ActivatedRoute,
  ) {
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params.subscribe((params: Params) => {
        this.loadExternalTJob(params['tJobId']);
      });
    }
  }

  ngOnInit() {}

  loadExternalTJob(id: number): void {
    this.externalService.getExternalTJobById(id).subscribe(
      (exTJob: ExternalTJobModel) => {
        this.exTJob = exTJob;
        this.createTJobExecution();
      },
      (error) => console.log(error),
    );
  }

  createTJobExecution(): void {
    this.exTJobExec = new ExternalTJobExecModel();
    this.exTJobExec.exTJob = new ExternalTJobModel();
    this.exTJobExec.exTJob.id = this.exTJob.id;
    this.externalService
      .createExternalTJobExecution(this.exTJobExec)
      .subscribe((exTJobExec: ExternalTJobExecModel) => {
        this.exTJobExec = exTJobExec;
        this.ready = true;
        // this.loadChromeBrowser();
      });
  }

  loadChromeBrowser(): void {
    this.eusService.startSession('chrome', '62').subscribe(
      (sessionId: string) => {
        this.sessionId = sessionId;
        this.eusService.getVncUrlSplitted(sessionId).subscribe(
          (urlObj: CompleteUrlObj) => {
            this.vncHost = urlObj.queryParams.host;
            this.vncPort = urlObj.queryParams.port;
            this.vncPassword = urlObj.queryParams.password;
            this.vncBrowserUrl = urlObj.href;
          },
          (error) => console.error(error),
        );
      },
      (error) => console.log(error),
    );
  }

  ngOnDestroy(): void {
    this.removeBrowser();
  }

  @HostListener('window:beforeunload')
  beforeunloadHandler() {
    // On window closed leave session
    this.removeBrowser();
  }

  removeBrowser(): void {
    if (this.sessionId !== undefined) {
      this.eusService
        .stopSession(this.sessionId)
        .subscribe((ok) => {}, (error) => console.error(error));
    }
  }
}
