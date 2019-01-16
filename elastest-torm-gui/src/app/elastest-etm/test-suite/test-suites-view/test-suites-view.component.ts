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

  testCaseColumns: any[] = [
    { name: 'result', label: 'Result', width: 68 },
    { name: 'logAnalyzer', label: 'Log', width: 60 },
    { name: 'name', label: 'Name' },
    { name: 'files', label: 'Files' },
    { name: 'time', label: 'Time (s)' },
    { name: 'failureMessage', label: 'Failure Msg' },
    { name: 'failureType', label: 'Failure Type' },
    { name: 'failureErrorLine', label: 'Error Line', width: 96 },
    { name: 'failureDetail', label: 'Failure Detail' },
  ];

  constructor(
    private router: Router,
    private tJobExecService: TJobExecService,
    private configurationService: ConfigurationService,
    private eusDialog: ElastestEusDialogService,
  ) {
    this.filesUrlPrefix = configurationService.configModel.proxyHost;
  }

  ngOnInit() {
    if (this.tJobExec) {
      this.getExecutionFiles();
    }
  }

  viewInLogAnalyzer(suite: TestSuiteModel, testCase: TestCaseModel): void {
    if (this.tJobExec && suite) {
      this.router.navigate(
        [
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
          'loganalyzer',
        ],
        {
          queryParams: { tjob: this.tJobExec.tJob.id, exec: this.tJobExec.id, testCase: testCase.name },
        },
      );
    }
  }
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

  getExecutionFiles(): void {
    this.tJobExecService.getTJobExecutionFiles(this.tJobExec.tJob.id, this.tJobExec.id).subscribe(
      (tJobsExecFiles: FileModel[]) => {
        for (let testSuite of this.testSuites) {
          for (let testCase of testSuite.testCases) {
            tJobsExecFiles = testCase.setTestCaseFiles(tJobsExecFiles);
          }
        }
      },
      (error) => console.log(error),
    );
  }

  viewSession(url: string, title: string = 'Recorded Video'): void {
    let dialog: MatDialogRef<ElastestEusDialog> = this.eusDialog.getDialog(true);
    dialog.componentInstance.title = title;
    dialog.componentInstance.iframeUrl = url;
    dialog.componentInstance.sessionType = 'video';
    dialog.componentInstance.closeButton = true;
  }
}
