import { Component, OnInit, ViewChild } from '@angular/core';
import { OnDestroy } from '@angular/core/src/metadata/lifecycle_hooks';
import { ExternalDataModel } from '../../models/external-data-model';
import { EusService } from '../../../../elastest-eus/elastest-eus.service';
import { ExternalService } from '../../external.service';
import { CompleteUrlObj } from '../../../../shared/utils';
import { IExternalExecution } from '../../models/external-execution-interface';
import { ExternalTJobModel } from '../../external-tjob/external-tjob-model';
import { ActivatedRoute, Params, Router } from '@angular/router';


@Component({
  selector: 'etm-external-tjob-execution-new',
  templateUrl: './external-tjob-execution-new.component.html',
  styleUrls: ['./external-tjob-execution-new.component.scss'],
})
export class ExternalTjobExecutionNewComponent implements OnInit, OnDestroy {
  @ViewChild('externalExecution') externalExecution: IExternalExecution;

  externalTJob: ExternalTJobModel;
  model: ExternalDataModel;
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
    this.model = new ExternalDataModel();
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params.subscribe((params: Params) => {
        this.loadExternalTJob(params['tJobId']);
      });
    }
  }

  ngOnInit() {}

  loadExternalTJob(id: number): void {
    this.externalService.getExternalTJobById(id).subscribe(
      (externalTJob: ExternalTJobModel) => {
        this.externalTJob = externalTJob;
        this.model.serviceType = this.externalTJob.getServiceType();

        // this.model.data = params;
        // this.ready = true;

        // this.loadChromeBrowser();
      },
      (error) => console.log(error),
    );
  }

  saveExecution(): void {
    this.externalExecution.saveExecution().subscribe(
      (saved: boolean) => {
        this.externalService.popupService.openSnackBar('Execution has been saved successfully');

        // Do something

        window.history.back();
      },
      (error) => console.log(error),
    );
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
    if (this.sessionId !== undefined) {
      this.eusService.stopSession(this.sessionId).subscribe((ok) => {}, (error) => console.error(error));
    }
  }
}
