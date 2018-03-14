import { TLTestCaseModel } from '../../models/test-case-model';
import { TdDialogService } from '@covalent/core/dialogs/services/dialog.service';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { TitlesService } from '../../../shared/services/titles.service';
import { Component, OnInit, ViewContainerRef } from '@angular/core';
import { TestLinkService } from '../../testlink.service';
import { MdDialog } from '@angular/material';
import { TestCaseExecutionModel } from '../../models/test-case-execution-model';



@Component({
  selector: 'testlink-test-case-execs',
  templateUrl: './test-case-execs.component.html',
  styleUrls: ['./test-case-execs.component.scss']
})
export class TestCaseExecsComponent implements OnInit {
  testCase: TLTestCaseModel;
  buildId: number;
  execs: TestCaseExecutionModel[] = [];
  testProjectId: number;

  // Exec Data
  execColumns: any[] = [
    { name: 'id', label: 'Id' },
    { name: 'status', label: 'Status' },
    { name: 'testerId', label: 'testerId' },
    { name: 'executionTimeStamp', label: 'Date' },
    { name: 'testPlanId', label: 'Plan Id' },
    { name: 'testCaseVersionId', label: 'Test Case Version Id' },
    { name: 'testCaseVersionNumber', label: 'Test Case Version' },
    { name: 'executionType', label: 'Execution Type' },
    { name: 'notes', label: 'Notes' },

    // { name: 'options', label: 'Options' },
  ];

  constructor(private titlesService: TitlesService, private testLinkService: TestLinkService,
    private route: ActivatedRoute, private router: Router,
    private _dialogService: TdDialogService, private _viewContainerRef: ViewContainerRef,
    public dialog: MdDialog
  ) { }

  ngOnInit() {
    this.titlesService.setHeadTitle('Test Case Execs');
    this.testCase = new TLTestCaseModel();
    this.loadCase();
  }

  loadCase(): void {
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params.switchMap(
        (params: Params) => {
          this.testProjectId = params['projectId'];
          this.buildId = params['buildId'];
          return this.testLinkService.getTestCaseById(params['caseId']);
        }
      )
        .subscribe((testCase: TLTestCaseModel) => {
          this.testCase = testCase;
          this.titlesService.setPathName(this.router.routerState.snapshot.url);

          this.loadExecs();
        });
    }
  }

  loadExecs(): void {
    this.testLinkService.getBuildCaseExecs(this.testCase.id, this.buildId)
      .subscribe(
      (execs: TestCaseExecutionModel[]) => {
        this.execs = execs.reverse(); // To sort
      },
      (error) => console.log(error),
    );
  }

}
