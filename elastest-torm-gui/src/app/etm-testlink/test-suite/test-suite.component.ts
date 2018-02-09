import { TLTestCaseModel } from '../models/test-case-model';
import { TLTestSuiteModel } from '../models/test-suite-model';
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
export class TLTestSuiteComponent implements OnInit {

  testSuite: TLTestSuiteModel;
  testCases: TLTestCaseModel[] = [];
  projectId: number;

  // TestCase Data
  testCaseColumns: any[] = [
    { name: 'id', label: 'Id' },
    { name: 'name', label: 'Name' },
    { name: 'testCaseStatus', label: 'Status' },
    // { name: 'testSuiteId', label: 'Suite ID' },
    // { name: 'testProjectId', label: 'Project ID' },
    { name: 'authorLogin', label: 'Author Login' },
    { name: 'summary', label: 'Summary' },
    { name: 'preconditions', label: 'Preconditions' },
    { name: 'testImportance', label: 'Importance' },
    { name: 'executionType', label: 'Exec Type' },
    { name: 'executionOrder', label: 'Exec Order' },
    { name: 'order', label: 'Order' },
    // { name: 'internalId', label: 'Internal ID' },
    { name: 'fullExternalId', label: 'External ID' },
    // { name: 'checkDuplicatedName', label: 'Check Duplicated Name' },
    // { name: 'actionOnDuplicatedName', label: 'Action On Duplicated Name' },
    { name: 'versionId', label: 'Version ID' },
    { name: 'version', label: 'Version' },
    { name: 'parentId', label: 'Parent ID' },
    // { name: 'executionStatus', label: 'Execution Status' },
    { name: 'platform', label: 'Platform' },
    { name: 'featureId', label: 'Feature Id' },
    // { name: 'customFields', label: 'Custom Fields' },
    // steps: TestCaseStepModel[];


    // { name: 'options', label: 'Options' },
  ];

  constructor(private titlesService: TitlesService, private testLinkService: TestLinkService,
    private route: ActivatedRoute, private router: Router,
    private _dialogService: TdDialogService, private _viewContainerRef: ViewContainerRef,
    public dialog: MdDialog
  ) { }

  ngOnInit() {
    this.titlesService.setHeadTitle('Test Suite');
    this.testSuite = new TLTestSuiteModel();
    this.loadSuite();
  }

  loadSuite(): void {
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params.switchMap((params: Params) => {
        this.projectId = params['projectId'];
        return this.testLinkService.getTestSuiteById(params['suiteId'], params['projectId'])
      }).subscribe((suite: TLTestSuiteModel) => {
        this.testSuite = suite;
        this.titlesService.setTopTitle(this.testSuite.getRouteString());
        this.loadTestCases();
      });
    }
  }

  loadTestCases(): void {
    this.testLinkService.getSuiteTestCases(this.testSuite)
      .subscribe(
      (testCases: TLTestCaseModel[]) => {
        this.testCases = testCases;
      },
      (error) => console.log(error),
    );
  }

}
