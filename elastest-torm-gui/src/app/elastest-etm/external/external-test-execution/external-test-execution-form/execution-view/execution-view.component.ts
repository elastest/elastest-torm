import { Component, OnInit, Input, ViewChild } from '@angular/core';
import { IExternalExecution } from '../../../models/external-execution-interface';
import { Observable } from 'rxjs/Observable';
import { Subject } from 'rxjs/Subject';
import { TestLinkService } from '../../../../../etm-testlink/testlink.service';
import { ExternalDataModel } from '../../../models/external-data-model';
import { ServiceType } from '../../../models/external-project-model';
import { TestCaseModel } from '../../../../../etm-testlink/models/test-case-model';
import { BuildModel } from '../../../../../etm-testlink/models/build-model';

@Component({
  selector: 'etm-execution-view',
  templateUrl: './execution-view.component.html',
  styleUrls: ['./execution-view.component.scss']
})
export class ExecutionViewComponent implements OnInit, IExternalExecution {

  @Input() model: ExternalDataModel;

  @ViewChild('executionForm') executionForm: IExternalExecution;

  data: any;

  service: ServiceType;

  constructor(
    private testLinkService: TestLinkService,
  ) { }

  ngOnInit() {
    this.service = this.model.serviceType;
    this.initServiceData();
  }

  initServiceData(): void {
    switch (this.service) {
      case 'TESTLINK':
        this.initTestLinkData();
        break;

      default:
        break;
    }
  }

  initTestLinkData(): void {
    this.testLinkService.getTestCaseById(this.model.data.testCaseId)
      .subscribe(
      (testCase: TestCaseModel) => {
        this.testLinkService.getBuildById(this.model.data.buildId)
          .subscribe(
          (build: BuildModel) => {
            this.data = {
              testCase: testCase,
              build: build,
            };
          },
          (error) => console.log(error),
        );
      },
      (error) => console.log(error),

    );
  }

  saveExecution(): Observable<boolean> {
    return this.executionForm.saveExecution();
  }

}
