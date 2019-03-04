import { TLTestCaseModel } from '../models/test-case-model';
import { TLTestSuiteModel } from '../models/test-suite-model';
import { TdDataTableSortingOrder, TdDataTableService, ITdDataTableSortChangeEvent } from '@covalent/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { TitlesService } from '../../shared/services/titles.service';
import { Component, OnInit } from '@angular/core';
import { TestLinkService } from '../testlink.service';
import { MatDialog } from '@angular/material';

@Component({
  selector: 'testlink-test-suite',
  templateUrl: './test-suite.component.html',
  styleUrls: ['./test-suite.component.scss'],
})
export class TLTestSuiteComponent implements OnInit {
  testSuite: TLTestSuiteModel;
  testCases: TLTestCaseModel[] = [];
  projectId: number;

  // TestCase Data
  testCaseColumns: any[] = [
    { name: 'id', label: 'Id', width: 80 },
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
    { name: 'order', label: 'Order', width: 77 },
    // { name: 'internalId', label: 'Internal ID' },
    { name: 'fullExternalId', label: 'External ID', width: 105 },
    // { name: 'checkDuplicatedName', label: 'Check Duplicated Name' },
    // { name: 'actionOnDuplicatedName', label: 'Action On Duplicated Name' },
    { name: 'versionId', label: 'Version ID', width: 106 },
    { name: 'version', label: 'Version', width: 88 },
    { name: 'parentId', label: 'Parent ID', width: 98 },
    // { name: 'executionStatus', label: 'Execution Status' },
    { name: 'platform', label: 'Platform' },
    { name: 'featureId', label: 'Feature Id' },
    // { name: 'customFields', label: 'Custom Fields' },
    // steps: TestCaseStepModel[];

    // { name: 'options', label: 'Options' },
  ];

  sortBy: string = 'id';
  sortOrder: TdDataTableSortingOrder = TdDataTableSortingOrder.Ascending;

  constructor(
    private titlesService: TitlesService,
    private testLinkService: TestLinkService,
    private route: ActivatedRoute,
    private router: Router,
    public dialog: MatDialog,
    private dataTableService: TdDataTableService,
  ) {}

  ngOnInit(): void {
    this.titlesService.setHeadTitle('Test Suite');
    this.testSuite = new TLTestSuiteModel();
    this.loadSuite();
  }

  loadSuite(): void {
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params
        .switchMap((params: Params) => {
          this.projectId = params['projectId'];
          return this.testLinkService.getTestSuiteById(params['suiteId'], params['projectId']);
        })
        .subscribe((suite: TLTestSuiteModel) => {
          this.testSuite = suite;
          this.titlesService.setPathName(this.router.routerState.snapshot.url);
          this.loadTestCases();
        });
    }
  }

  loadTestCases(): void {
    this.testLinkService.getSuiteTestCases(this.testSuite).subscribe(
      (testCases: TLTestCaseModel[]) => {
        this.testCases = testCases;
      },
      (error: Error) => console.log(error),
    );
  }

  sort(sortEvent: ITdDataTableSortChangeEvent): void {
    this.sortBy = sortEvent.name;
    this.sortOrder = sortEvent.order;
    this.testCases = this.dataTableService.sortData(this.testCases, this.sortBy, this.sortOrder);
  }
}
