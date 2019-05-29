import { TestPlanModel } from '../models/test-plan-model';
import { TdDataTableSortingOrder, TdDataTableService, ITdDataTableSortChangeEvent } from '@covalent/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { TitlesService } from '../../shared/services/titles.service';
import { Component, OnInit } from '@angular/core';
import { TestProjectModel } from '../models/test-project-model';
import { TestLinkService } from '../testlink.service';
import { MatDialog, MatDialogRef } from '@angular/material';
import { TLTestSuiteModel } from '../models/test-suite-model';
import { ExternalProjectModel } from '../../elastest-etm/external/external-project/external-project-model';
import { ExternalTJobModel } from '../../elastest-etm/external/external-tjob/external-tjob-model';
import { SelectBuildModalComponent } from '../test-plan/select-build-modal/select-build-modal.component';

@Component({
  selector: 'testlink-test-project',
  templateUrl: './test-project.component.html',
  styleUrls: ['./test-project.component.scss'],
})
export class TestProjectComponent implements OnInit {
  testProject: TestProjectModel;
  testSuites: TLTestSuiteModel[] = [];
  showSpinnerSuites: boolean = true;
  testPlans: TestPlanModel[] = [];
  showSpinnerPlans: boolean = true;

  exProject: ExternalProjectModel;
  loadingExProject: boolean = true;

  // Test Suite Data
  suiteColumns: any[] = [
    { name: 'id', label: 'Id', width: 80 },
    { name: 'name', label: 'Name' },
    { name: 'details', label: 'Details' },
    { name: 'parentId', label: 'Parent ID' },
    { name: 'order', label: 'Order' },
  ];

  // Test Plan Data
  planColumns: any[] = [
    { name: 'id', label: 'Id', width: 80 },
    { name: 'name', label: 'Name' },
    { name: 'sut', label: 'Sut', width: 80 },
    { name: 'notes', label: 'Notes' },
    { name: 'active', label: 'Active' },
    { name: 'public', label: 'Public' },
    { name: 'options', label: 'Options', sortable: false },
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
    this.titlesService.setHeadTitle('Test Project');
    this.testProject = new TestProjectModel();
    this.loadProject();
  }

  loadProject(): void {
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params
        .switchMap((params: Params) => this.testLinkService.getProjectById(params['projectId']))
        .subscribe((project: TestProjectModel) => {
          this.testProject = project;
          this.titlesService.setPathName(this.router.routerState.snapshot.url);
          this.loadTestSuites();
          this.loadTestPlans();
          this.loadExternalProject();
        });
    }
  }

  loadTestSuites(): void {
    this.testLinkService.getProjecTestSuites(this.testProject).subscribe(
      (suites: TLTestSuiteModel[]) => {
        this.testSuites = suites;
        this.showSpinnerSuites = false;
      },
      (error: Error) => console.log(error),
    );
  }

  loadTestPlans(): void {
    this.testLinkService.getProjectTestPlans(this.testProject).subscribe(
      (plans: TestPlanModel[]) => {
        this.testPlans = plans;
        this.showSpinnerPlans = false;
      },
      (error: Error) => console.log(error),
    );
  }

  loadExternalProject(): void {
    this.testLinkService.getExternalProjectByTestProjectId(this.testProject.id).subscribe(
      (exProject: ExternalProjectModel) => {
        this.exProject = exProject;
        this.loadingExProject = false;
      },
      (error: Error) => {
        console.log(error);
        this.loadingExProject = false;
      },
    );
  }

  /* Test Plans */
  runTestPlan(testPlan: TestPlanModel): void {
    let dialogRef: MatDialogRef<SelectBuildModalComponent> = this.dialog.open(SelectBuildModalComponent, {
      data: {
        testPlan: testPlan,
        testProjectId: this.testProject.id,
      },
      minWidth: '20%',
    });
  }

  editTestPlan(testPlan: TestPlanModel): void {
    if (this.exProject) {
      this.testLinkService.getExternalTJobByTestPlanId(testPlan.id).subscribe(
        (exTJob: ExternalTJobModel) => {
          this.router.navigate(['/external/projects', this.exProject.id, 'tjob', 'edit', exTJob.id]);
        },
        (error: Error) => console.log(error),
      );
    }
  }

  sortSuites(sortEvent: ITdDataTableSortChangeEvent): void {
    this.sortBy = sortEvent.name;
    this.sortOrder = sortEvent.order;
    this.testSuites = this.dataTableService.sortData(this.testSuites, this.sortBy, this.sortOrder);
  }

  sortPlans(sortEvent: ITdDataTableSortChangeEvent): void {
    this.sortBy = sortEvent.name;
    this.sortOrder = sortEvent.order;
    this.testPlans = this.dataTableService.sortData(this.testPlans, this.sortBy, this.sortOrder);
  }

  getSut(testPlan: TestPlanModel): string {
    let sut: string = 'None';
    if (this.exProject && this.exProject.exTJobs) {
      for (let exTJob of this.exProject.exTJobs) {
        if (exTJob && exTJob.externalId === testPlan.id + '') {
          if (exTJob.sut && exTJob.sut.id > 0) {
            return exTJob.sut.id + '';
          }
          break;
        }
      }
    }
    return sut;
  }
}
