import { Component, Inject, OnInit, Optional } from '@angular/core';
import { MD_DIALOG_DATA, MdDialogRef } from '@angular/material';
import { TestCaseModel, ExecStatusValue } from '../../models/test-case-model';
import { TestCaseExecutionModel } from '../../models/test-case-execution-model';
import { BuildModel } from '../../models/build-model';
import { TestLinkService } from '../../testlink.service';

@Component({
  selector: 'testlink-execute-case-modal',
  templateUrl: './execute-case-modal.component.html',
  styleUrls: ['./execute-case-modal.component.scss']
})
export class ExecuteCaseModalComponent implements OnInit {

  // TestCaseSteps Data
  testCaseStepsColumns: any[] = [
    { name: 'id', label: 'Id' },
    // { name: 'testCaseVersionId', label: 'Version Id' },
    // { name: 'number', label: 'Number' },
    { name: 'actions', label: 'Actions' },
    { name: 'expectedResults', label: 'Expected Results' },
    // { name: 'active', label: 'Active' },
    { name: 'executionType', label: 'Exec Type' },
  ];

  testCase: TestCaseModel;
  build: BuildModel;

  tcExec: TestCaseExecutionModel;

  constructor(
    private dialogRef: MdDialogRef<ExecuteCaseModalComponent>,
    private testLinkService: TestLinkService,
    @Optional() @Inject(MD_DIALOG_DATA) public data: any,
  ) {
    this.tcExec = new TestCaseExecutionModel();
    this.testCase = data.testCase;
    this.build = data.build;
  }

  ngOnInit() {
    this.tcExec.testCaseVersionId = this.testCase.versionId;
    this.tcExec.testCaseVersionNumber = this.testCase.version;
    this.tcExec.executionType = this.testCase.executionType;
    this.tcExec.testPlanId = this.build.testPlanId;
    this.tcExec.buildId = this.build.id;
  }

  saveExecution(): void {
    this.testLinkService.saveExecution(this.tcExec, this.testCase.id)
      .subscribe(
      (data) => {
        let response: any = { saved: true };
        this.dialogRef.close(response);
      },
      (error) => console.log(error)
      );
  }

}
