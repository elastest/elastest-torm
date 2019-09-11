import { Component, Inject, OnInit, Optional, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material';
import { TLTestCaseModel, ExecStatusValue } from '../../models/test-case-model';
import { TestCaseExecutionModel } from '../../models/test-case-execution-model';
import { BuildModel } from '../../models/build-model';
import { TestLinkService } from '../../testlink.service';

@Component({
  selector: 'testlink-execute-case-modal',
  templateUrl: './execute-case-modal.component.html',
  styleUrls: ['./execute-case-modal.component.scss'],
})
export class ExecuteCaseModalComponent implements OnInit, AfterViewChecked {
  @ViewChild('notes', { static: true }) notes: ElementRef;
  alreadyFocused: boolean = false;

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

  testCase: TLTestCaseModel;
  build: BuildModel;
  platformId: number = 0;

  tcExec: TestCaseExecutionModel;

  constructor(
    private dialogRef: MatDialogRef<ExecuteCaseModalComponent>,
    private testLinkService: TestLinkService,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: any,
  ) {
    this.tcExec = new TestCaseExecutionModel();
    this.testCase = data.testCase;
    this.build = data.build;
    this.platformId = this.data.platformId ? this.data.platformId : 0;
  }

  ngOnInit(): void {
    this.tcExec.testCaseVersionId = this.testCase.versionId;
    this.tcExec.testCaseVersionNumber = this.testCase.version;
    this.tcExec.executionType = this.testCase.executionType;
    this.tcExec.testPlanId = this.build.testPlanId;
    this.tcExec.buildId = this.build.id;
  }

  ngAfterViewChecked(): void {
    if (!this.alreadyFocused) {
      this.notes.nativeElement.focus();
      this.alreadyFocused = true;
    }
  }

  saveExecution(): void {
    this.testLinkService.saveExecution(this.tcExec, this.testCase.id, this.platformId).subscribe(
      (data) => {
        let response: any = { saved: true };
        this.dialogRef.close(response);
      },
      (error: Error) => console.log(error),
    );
  }
}
