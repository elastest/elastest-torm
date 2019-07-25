import { Component, OnInit, Input } from '@angular/core';
import { TestSuiteModel } from '../test-suite-model';
import { Router } from '@angular/router';
import { TJobExecModel } from '../../tjob-exec/tjobExec-model';
import { TestCaseModel } from '../../test-case/test-case-model';
import { ConfigurationService } from '../../../config/configuration-service.service';

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

  constructor(private router: Router, private configurationService: ConfigurationService) {
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
