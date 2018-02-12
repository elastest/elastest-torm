import { Component, OnInit, Input } from '@angular/core';
import { TestSuiteModel } from '../test-suite-model';
import { Router } from '@angular/router';
import { TJobExecModel } from '../../tjob-exec/tjobExec-model';
import { TestCaseModel } from '../../test-case/test-case-model';

@Component({
  selector: 'etm-test-suites-view',
  templateUrl: './test-suites-view.component.html',
  styleUrls: ['./test-suites-view.component.scss'],
})
export class TestSuitesViewComponent implements OnInit {
  @Input() testSuites: TestSuiteModel[];
  @Input() tJobExec: TJobExecModel;

  testCaseColumns: any[] = [
    { name: 'id', label: 'Id' },
    { name: 'name', label: 'Name' },
    { name: 'time', label: 'Time' },
    { name: 'failureMessage', label: 'Failure Msg' },
    { name: 'failureType', label: 'Failure Type' },
    { name: 'failureErrorLine', label: 'Failure Error Line' },
    { name: 'failureDetail', label: 'Failure Detail' },
    { name: 'logAnalyzer', label: 'View In Log Analyzer' },
  ];

  constructor(private router: Router) {}

  ngOnInit() {}

  viewInLogAnalyzer(testCase: TestCaseModel): void {
    if (this.tJobExec) {
      this.router.navigate(['/loganalyzer'], {
        queryParams: { tjob: this.tJobExec.tJob.id, exec: this.tJobExec.id, testCase: testCase.name },
      });
    }
  }
}
