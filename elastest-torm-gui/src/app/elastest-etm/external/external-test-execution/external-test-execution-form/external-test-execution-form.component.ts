import { Component, OnInit, Input, ViewChild, OnDestroy } from '@angular/core';
import { ExternalDataModel } from '../../models/external-data-model';
import { IExternalExecution } from '../../models/external-execution-interface';
import { ExternalService } from '../../external.service';
import { Router } from '@angular/router';
import { EusService } from '../../../../elastest-eus/elastest-eus.service';
import { CompleteUrlObj } from '../../../../shared/utils';
import { IExternalExecutionSaveModel } from '../../models/external-execution-save.model';
import { EusTestModel } from '../../../../elastest-eus/elastest-eus-test-model';

@Component({
  selector: 'etm-external-test-execution-form',
  templateUrl: './external-test-execution-form.component.html',
  styleUrls: ['./external-test-execution-form.component.scss'],
})
export class ExternalTestExecutionFormComponent implements OnInit, OnDestroy {
  @ViewChild('externalExecution', { static: false }) externalExecution: IExternalExecution;

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

  constructor(private externalService: ExternalService, public router: Router, private eusService: EusService) {
    let params: any = router.parseUrl(router.url).queryParams;
    this.model = new ExternalDataModel();
    this.model.serviceType = params.serviceType;
    this.model.data = params;
    this.ready = true;

    this.loadChromeBrowser();
  }

  ngOnInit() {}

  saveExecution(): void {
    this.externalExecution.saveExecution().subscribe(
      (savedObj: IExternalExecutionSaveModel) => {
        this.externalService.popupService.openSnackBar('Execution has been saved successfully');

        // Do something

        window.history.back();
      },
      (error) => console.log(error),
    );
  }

  loadChromeBrowser(): void {
    this.eusService.startSession('chrome', '62').subscribe(
      (eusTestModel: EusTestModel) => {
        this.sessionId = eusTestModel.id;
        this.eusService.getVncUrlSplitted(eusTestModel.id).subscribe(
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
