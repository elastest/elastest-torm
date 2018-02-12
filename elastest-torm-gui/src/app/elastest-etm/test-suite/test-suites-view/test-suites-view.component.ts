import { Component, OnInit, Input } from '@angular/core';
import { TestSuiteModel } from '../test-suite-model';
import { Router } from '@angular/router';
import { TJobExecModel } from '../../tjob-exec/tjobExec-model';
import { TestCaseModel } from '../../test-case/test-case-model';
import { TJobExecService } from '../../tjob-exec/tjobExec.service';
import { FileModel } from '../../files-manager/file-model';
import { ConfigurationService } from '../../../config/configuration-service.service';

@Component({
  selector: 'etm-test-suites-view',
  templateUrl: './test-suites-view.component.html',
  styleUrls: ['./test-suites-view.component.scss'],
})
export class TestSuitesViewComponent implements OnInit {
  @Input() testSuites: TestSuiteModel[];
  @Input() tJobExec: TJobExecModel;

  filesUrlPrefix: string;

  testCaseColumns: any[] = [
    { name: 'result', label: 'Result' },
    { name: 'name', label: 'Name' },
    { name: 'time', label: 'Time' },
    { name: 'failureMessage', label: 'Failure Msg' },
    { name: 'failureType', label: 'Failure Type' },
    { name: 'failureErrorLine', label: 'Failure Error Line' },
    { name: 'failureDetail', label: 'Failure Detail' },
    { name: 'files', label: 'Files' },
    { name: 'logAnalyzer', label: 'View In Log Analyzer' },
  ];

  constructor(
    private router: Router,
    private tJobExecService: TJobExecService,
    private configurationService: ConfigurationService,
  ) {
    this.filesUrlPrefix = configurationService.configModel.host;
  }

  ngOnInit() {
    if (this.tJobExec) {
      this.getExecutionFiles();
    }
  }

  viewInLogAnalyzer(testCase: TestCaseModel): void {
    if (this.tJobExec) {
      this.router.navigate(['/loganalyzer'], {
        queryParams: { tjob: this.tJobExec.tJob.id, exec: this.tJobExec.id, testCase: testCase.name },
      });
    }
  }

  getExecutionFiles(): void {
    this.tJobExecService.getTJobExecutionFiles(this.tJobExec.tJob.id, this.tJobExec.id).subscribe(
      (tJobsExecFiles: FileModel[]) => {
        for (let testSuite of this.testSuites) {
          for (let testCase of testSuite.testCases) {
            tJobsExecFiles = this.setTestCaseFiles(tJobsExecFiles, testCase);
          }
        }
      },
      (error) => console.log(error),
    );
  }

  setTestCaseFiles(tJobExecFiles: FileModel[], testCase: TestCaseModel): FileModel[] {
    let newExecFiles: FileModel[] = [];
    for (let file of tJobExecFiles) {
      if (file.name.startsWith(testCase.name + '_')) {
        testCase.files.push(file);
      } else {
        newExecFiles.push(file);
      }
    }
    return newExecFiles;
  }
}
