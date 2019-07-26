import { Component, OnInit, Input } from '@angular/core';
import { TestSuiteModel } from './test-suite-model';
import { TJobExecModel } from '../tjob-exec/tjobExec-model';
import { TitlesService } from '../../shared/services/titles.service';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { TestSuiteService } from './test-suite.service';

@Component({
  selector: 'etm-test-suite',
  templateUrl: './test-suite.component.html',
  styleUrls: ['./test-suite.component.scss'],
})
export class TestSuiteComponent implements OnInit {
  @Input()
  testSuite: TestSuiteModel;

  @Input()
  tJobExec: TJobExecModel;

  tJobExecId: number;
  testSuiteId: number;

  nested: boolean = false;

  selectedTestCaseTab: number;

  constructor(
    public route: ActivatedRoute,
    private titlesService: TitlesService,
    private router: Router,
    private testSuiteService: TestSuiteService,
  ) {}

  ngOnInit(): void {
    this.titlesService.setPathName(this.router.routerState.snapshot.url);

    // Nested
    if (this.tJobExec && this.testSuite) {
      this.nested = true;
      this.tJobExecId = this.tJobExec.id;
      this.testSuiteId = this.testSuite.id;
    } else {
      // Complete Section
      if (this.route.params !== null || this.route.params !== undefined) {
        this.route.params.subscribe((params: Params) => {
          if (params) {
            this.tJobExecId = params.tJobExecId;
            this.testSuiteId = params.testCaseId;
            this.loadTestSuite();
          }
        });
      }
    }
  }

  loadTestSuite(): void {
    this.testSuiteService.getTestSuiteById(this.testSuiteId).subscribe((testSuite: TestSuiteModel) => {
      this.testSuite = testSuite;
    });
  }
}
