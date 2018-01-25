import { Component, OnInit, Input, ViewChild } from '@angular/core';
import { ExternalDataModel } from '../../models/external-data-model';
import { IExternalExecution } from '../../models/external-execution-interface';
import { ExternalService } from '../../external.service';
import { Router } from '@angular/router';

@Component({
  selector: 'etm-external-test-execution-form',
  templateUrl: './external-test-execution-form.component.html',
  styleUrls: ['./external-test-execution-form.component.scss']
})
export class ExternalTestExecutionFormComponent implements OnInit {

  model: ExternalDataModel;
  ready: boolean = false;

  @ViewChild('externalExecution')
  externalExecution: IExternalExecution;

  constructor(
    private externalService: ExternalService, public router: Router,
  ) {
    let params: any = router.parseUrl(router.url).queryParams;
    this.model = new ExternalDataModel();
    this.model.serviceType = params.serviceType;
    this.model.data = params;
    this.ready = true;
  }

  ngOnInit() {

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
}
