import { BuildModel } from '../models/build-model';
import { TestPlanModel } from '../models/test-plan-model';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { TitlesService } from '../../shared/services/titles.service';
import { Component, OnInit } from '@angular/core';
import { TestLinkService } from '../testlink.service';
import { MatDialog, MatDialogRef } from '@angular/material';
import { ExternalTJobModel } from '../../elastest-etm/external/external-tjob/external-tjob-model';
import { TLTestCaseModel } from '../models/test-case-model';
import { SelectBuildModalComponent } from './select-build-modal/select-build-modal.component';
import { ITdDataTableSortChangeEvent, TdDataTableSortingOrder, TdDataTableService } from '@covalent/core';

@Component({
  selector: 'testlink-test-plan',
  templateUrl: './test-plan.component.html',
  styleUrls: ['./test-plan.component.scss'],
})
export class TestPlanComponent implements OnInit {
  testPlan: TestPlanModel;
  builds: BuildModel[] = [];
  showSpinnerBuilds: boolean = true;
  testPlanCases: TLTestCaseModel[] = [];
  showSpinnerCases: boolean = true;
  testProjectId: number;

  exTJob: ExternalTJobModel;

  // Build Data
  buildColumns: any[] = [
    { name: 'id', label: 'Id', width: 80 },
    { name: 'name', label: 'Name' },
    { name: 'testPlanId', label: 'Test Plan ID' },
    { name: 'notes', label: 'Notes' },

    // { name: 'options', label: 'Options' },
  ];

  // TestCase Data
  testCaseColumns: any[] = [
    { name: 'id', label: 'Id', width: 80 },
    { name: 'name', label: 'Name' },
    { name: 'testCaseStatus', label: 'Status' },
    { name: 'summary', label: 'Summary' },
    { name: 'preconditions', label: 'Preconditions' },
    { name: 'executionType', label: 'Exec Type' },
    { name: 'fullExternalId', label: 'External ID' },
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
    this.titlesService.setHeadTitle('Test Plan');
    this.testPlan = new TestPlanModel();
    this.loadPlan();
  }

  loadPlan(): void {
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params
        .switchMap((params: Params) => {
          this.testProjectId = params['projectId'];
          return this.testLinkService.getTestPlanById(params['planId']);
        })
        .subscribe((plan: TestPlanModel) => {
          this.testPlan = plan;
          this.titlesService.setPathName(this.router.routerState.snapshot.url);

          this.loadBuilds();
          this.loadTestCases();
          this.loadExternalTJob();
        });
    }
  }

  loadBuilds(): void {
    this.testLinkService.getPlanBuilds(this.testPlan).subscribe(
      (builds: BuildModel[]) => {
        this.builds = builds;
        this.showSpinnerBuilds = false;
      },
      (error: Error) => console.log(error),
    );
  }

  loadTestCases(): void {
    this.testLinkService.getPlanTestCases(this.testPlan).subscribe(
      (testCases: TLTestCaseModel[]) => {
        this.testPlanCases = testCases;
        this.showSpinnerCases = false;
      },
      (error: Error) => console.log(error),
    );
  }

  loadExternalTJob(): void {
    this.testLinkService.getExternalTJobByTestPlanId(this.testPlan.id).subscribe(
      (tJob: ExternalTJobModel) => {
        this.exTJob = tJob;
      },
      (error: Error) => console.log(error),
    );
  }

  runTestPlan(): void {
    let dialogRef: MatDialogRef<SelectBuildModalComponent> = this.dialog.open(SelectBuildModalComponent, {
      data: {
        testPlan: this.testPlan,
        builds: this.builds,
        testProjectId: this.testProjectId,
      },
      minWidth: '20%',
    });
  }

  sortBuilds(sortEvent: ITdDataTableSortChangeEvent): void {
    this.sortBy = sortEvent.name;
    this.sortOrder = sortEvent.order;
    this.builds = this.dataTableService.sortData(this.builds, this.sortBy, this.sortOrder);
  }

  sortCases(sortEvent: ITdDataTableSortChangeEvent): void {
    this.sortBy = sortEvent.name;
    this.sortOrder = sortEvent.order;
    this.testPlanCases = this.dataTableService.sortData(this.testPlanCases, this.sortBy, this.sortOrder);
  }
}
