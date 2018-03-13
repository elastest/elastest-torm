import { TestPlanModel } from '../models/test-plan-model';
import { TdDialogService } from '@covalent/core/dialogs/services/dialog.service';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { TitlesService } from '../../shared/services/titles.service';
import { Component, OnInit, ViewContainerRef } from '@angular/core';
import { TestProjectModel } from '../models/test-project-model';
import { TestLinkService } from '../testlink.service';
import { MdDialog } from '@angular/material';
import { TLTestSuiteModel } from '../models/test-suite-model';
import { ExternalProjectModel } from '../../elastest-etm/external/external-project/external-project-model';
import { ExternalTJobModel } from '../../elastest-etm/external/external-tjob/external-tjob-model';

@Component({
  selector: 'testlink-test-project',
  templateUrl: './test-project.component.html',
  styleUrls: ['./test-project.component.scss'],
})
export class TestProjectComponent implements OnInit {
  testProject: TestProjectModel;
  testSuites: TLTestSuiteModel[] = [];
  testPlans: TestPlanModel[] = [];

  exProject: ExternalProjectModel;

  // Test Suite Data
  suiteColumns: any[] = [
    { name: 'id', label: 'Id' },
    { name: 'name', label: 'Name' },
    { name: 'details', label: 'Details' },
    { name: 'parentId', label: 'Parent ID' },
    { name: 'order', label: 'Order' },
  ];

  // Test Plan Data
  planColumns: any[] = [
    { name: 'id', label: 'Id' },
    { name: 'name', label: 'Name' },
    { name: 'notes', label: 'Notes' },
    { name: 'active', label: 'Active' },
    { name: 'public', label: 'Public' },
  ];

  constructor(
    private titlesService: TitlesService,
    private testLinkService: TestLinkService,
    private route: ActivatedRoute,
    private router: Router,
    private _dialogService: TdDialogService,
    private _viewContainerRef: ViewContainerRef,
    public dialog: MdDialog,
  ) {}

  ngOnInit() {
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
          this.titlesService.setTopTitle(this.testProject.getRouteString());
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
      },
      (error) => console.log(error),
    );
  }

  loadTestPlans(): void {
    this.testLinkService.getProjectTestPlans(this.testProject).subscribe(
      (plans: TestPlanModel[]) => {
        this.testPlans = plans;
      },
      (error) => console.log(error),
    );
  }

  loadExternalProject(): void {
    this.testLinkService.getExternalProjectByTestProjectId(this.testProject.id).subscribe(
      (exProject: ExternalProjectModel) => {
        this.exProject = exProject;
      },
      (error) => console.log(error),
    );
  }

  /* Test Plans */
  runTestPlan(row: TestPlanModel): void {
    this.testLinkService.getExternalTJobByTestPlanId(row.id).subscribe(
      (exTJob: ExternalTJobModel) => {
        this.router.navigate(['/external/project/', exTJob.exProject.id, 'tjob', exTJob.id, 'exec', 'new']);
      },
      (error) => console.log(error),
    );
  }
}
