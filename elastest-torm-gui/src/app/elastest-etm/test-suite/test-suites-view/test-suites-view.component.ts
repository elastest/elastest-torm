import { Component, OnInit, Input } from '@angular/core';
import { TestSuiteModel } from '../test-suite-model';
import { Router } from '@angular/router';
import { TJobExecModel } from '../../tjob-exec/tjobExec-model';
import { TestCaseModel } from '../../test-case/test-case-model';
import { TJobExecService } from '../../tjob-exec/tjobExec.service';
import { FileModel } from '../../files-manager/file-model';
import { ConfigurationService } from '../../../config/configuration-service.service';
import { MatDialogRef } from '@angular/material';
import { ElastestEusDialog } from '../../../elastest-eus/elastest-eus.dialog';
import { ElastestEusDialogService } from '../../../elastest-eus/elastest-eus.dialog.service';
import { ITdDataTableColumn } from '@covalent/core';

@Component({
  selector: 'etm-test-suites-view',
  templateUrl: './test-suites-view.component.html',
  styleUrls: ['./test-suites-view.component.scss'],
})
export class TestSuitesViewComponent implements OnInit {
  @Input()
  testSuites: TestSuiteModel[];
  @Input()
  tJobExec: TJobExecModel;

  filesUrlPrefix: string;
  selectedTestCaseTab: number;

  constructor(
    private router: Router,
    private tJobExecService: TJobExecService,
    private configurationService: ConfigurationService,
    private eusDialog: ElastestEusDialogService,
  ) {
    this.filesUrlPrefix = this.configurationService.configModel.proxyHost;
  }

  ngOnInit(): void {}

  viewTestCaseDetails(suite: TestSuiteModel, testCase: TestCaseModel): void {
    if (this.tJobExec) {
      this.router.navigate([
        '/projects',
        this.tJobExec.tJob.project.id,
        'tjob',
        this.tJobExec.tJob.id,
        'tjob-exec',
        this.tJobExec.id,
        'testSuite',
        suite.id,
        'testCase',
        testCase.id,
      ]);
    }
  }

  goToTestCaseTab(num: number): void {
    this.selectedTestCaseTab = num;
  }
}
