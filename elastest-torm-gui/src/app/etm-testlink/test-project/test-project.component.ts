import { TestPlanModel } from '../models/test-plan-model';
import { TdDialogService } from '@covalent/core/dialogs/services/dialog.service';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { TitlesService } from '../../shared/services/titles.service';
import { Component, OnInit, ViewContainerRef } from '@angular/core';
import { TestProjectModel } from '../models/test-project-model';
import { TestLinkService } from '../testlink.service';
import { MdDialog } from '@angular/material';
import { TestSuiteModel } from '../models/test-suite-model';

@Component({
  selector: 'testlink-test-project',
  templateUrl: './test-project.component.html',
  styleUrls: ['./test-project.component.scss']
})
export class TestProjectComponent implements OnInit {
  testProject: TestProjectModel;
  testSuites: TestSuiteModel[] = [];
  testPlans: TestPlanModel[] = [];


  // Test Suite Data
  suiteColumns: any[] = [
    { name: 'id', label: 'Id' },
    { name: 'name', label: 'Name' },
    { name: 'testProjectId', label: 'Test Project ID' },
    { name: 'details', label: 'Details' },
    { name: 'parentId', label: 'Parent ID' },
    { name: 'order', label: 'Order' },
    { name: 'checkDuplicatedName', label: 'Check Duplicated Name' },
    { name: 'actionOnDuplicatedName', label: 'On Duplicated Name' },
    { name: 'options', label: 'Options' },
  ];

  constructor(
    private titlesService: TitlesService, private testLinkService: TestLinkService,
    private route: ActivatedRoute, private router: Router,
    private _dialogService: TdDialogService, private _viewContainerRef: ViewContainerRef,

    public dialog: MdDialog,
  ) { }

  ngOnInit() {
    this.titlesService.setHeadTitle('Project');
    this.testProject = new TestProjectModel();
    this.loadProject();
  }

  loadProject(): void {
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params.switchMap((params: Params) => this.testLinkService.getProjectById(params['projectId']))
        .subscribe((project: TestProjectModel) => {
          this.testProject = project;
          this.titlesService.setTopTitle(this.testProject.getRouteString());
          this.loadTestSuites();
          this.loadTestPlans();
        });
    }
  }

  loadTestSuites(): void {
    this.testLinkService.getProjecTestSuites(this.testProject)
      .subscribe(
      (suites: TestSuiteModel[]) => {
        this.testSuites = suites;
      },
      (error) => console.log(error),
    );
  }

  loadTestPlans(): void {

  }

}
