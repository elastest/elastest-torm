import { TLTestCaseModel } from '../../models/test-case-model';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { TitlesService } from '../../../shared/services/titles.service';
import { Component, OnInit } from '@angular/core';
import { TestLinkService } from '../../testlink.service';
import { MatDialog } from '@angular/material';
import { TestCaseExecutionModel } from '../../models/test-case-execution-model';

@Component({
  selector: 'testlink-test-case-execs',
  templateUrl: './test-case-execs.component.html',
  styleUrls: ['./test-case-execs.component.scss'],
})
export class TestCaseExecsComponent implements OnInit {
  testCase: TLTestCaseModel;
  buildId: number;
  execs: TestCaseExecutionModel[] = [];
  showSpinner: boolean = true;
  testProjectId: number;

  // Exec Data
  execColumns: any[] = [
    { name: 'id', label: 'Id', width: 80 },
    { name: 'status', label: 'Status' },
    { name: 'testerId', label: 'testerId', width: 95 },
    { name: 'executionTimeStamp', label: 'Date', width: 215 },
    { name: 'testPlanId', label: 'Plan Id', width: 80 },
    { name: 'testCaseVersionId', label: 'Test Case Version Id', width: 150 },
    { name: 'testCaseVersionNumber', label: 'Test Case Version', width: 150 },
    { name: 'executionType', label: 'Execution Type' },
    { name: 'notes', label: 'Notes' },

    // { name: 'options', label: 'Options' },
  ];

  constructor(
    private titlesService: TitlesService,
    private testLinkService: TestLinkService,
    private route: ActivatedRoute,
    private router: Router,
    public dialog: MatDialog,
  ) {}

  ngOnInit(): void {
    this.titlesService.setHeadTitle('Test Case Execs');
    this.testCase = new TLTestCaseModel();
    this.loadCase();
  }

  loadCase(): void {
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params
        .switchMap((params: Params) => {
          this.testProjectId = params['projectId'];
          this.buildId = params['buildId'];
          return this.testLinkService.getTestCaseById(params['caseId']);
        })
        .subscribe((testCase: TLTestCaseModel) => {
          this.testCase = testCase;
          this.titlesService.setPathName(this.router.routerState.snapshot.url);

          this.loadExecs();
        });
    }
  }

  loadExecs(): void {
    this.testLinkService.getBuildCaseExecs(this.testCase.id, this.buildId).subscribe(
      (execs: TestCaseExecutionModel[]) => {
        this.execs = execs.reverse(); // To sort
        this.showSpinner = false;
      },
      (error) => console.log(error),
    );
  }
}
