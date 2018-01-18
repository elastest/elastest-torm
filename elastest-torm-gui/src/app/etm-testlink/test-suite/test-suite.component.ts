import { TestCaseModel } from '../models/test-case-model';
import { TestSuiteModel } from '../models/test-suite-model';
import { TdDialogService } from '@covalent/core/dialogs/services/dialog.service';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { TitlesService } from '../../shared/services/titles.service';
import { Component, OnInit, ViewContainerRef } from '@angular/core';
import { TestProjectModel } from '../models/test-project-model';
import { TestLinkService } from '../testlink.service';
import { MdDialog } from '@angular/material';

@Component({
  selector: 'app-test-suite',
  templateUrl: './test-suite.component.html',
  styleUrls: ['./test-suite.component.scss']
})
export class TestSuiteComponent implements OnInit {

  testSuite: TestSuiteModel;
  testCases: TestCaseModel[] = [];

  // TestCase Data
  testCaseColumns: any[] = [
    { name: 'id', label: 'Id' },
    { name: 'testCaseVersionId', label: 'Version ID' },
    { name: 'actions', label: 'Actions' },
    { name: 'expectedResults', label: 'Expected Results' },
    { name: 'active', label: 'Active' },
    { name: 'executionType', label: 'Execution Type' },

    // { name: 'options', label: 'Options' },
  ];

  constructor(private titlesService: TitlesService, private testLinkService: TestLinkService,
    private route: ActivatedRoute, private router: Router,
    private _dialogService: TdDialogService, private _viewContainerRef: ViewContainerRef,
    public dialog: MdDialog
  ) { }

  ngOnInit() {
    this.titlesService.setHeadTitle('Test Suite');
    this.testSuite = new TestSuiteModel();
    this.loadSuite();
  }

  loadSuite(): void {
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params.switchMap((params: Params) => this.testLinkService.getTestSuiteById(params['suiteId'], params['projectId']))
        .subscribe((suite: TestSuiteModel) => {
          this.testSuite = suite;
          this.titlesService.setTopTitle(this.testSuite.getRouteString());

          this.loadTestCases();
        });
    }
  }

  loadTestCases(): void {
    this.testLinkService.getSuiteTestCases(this.testSuite)
      .subscribe(
      (testCases: TestCaseModel[]) => {
        this.testCases = testCases;
      },
      (error) => console.log(error),
    );
  }

}
