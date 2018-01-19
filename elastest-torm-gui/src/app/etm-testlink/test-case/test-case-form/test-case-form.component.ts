import { ActivatedRoute, Params } from '@angular/router';
import { TitlesService } from '../../../shared/services/titles.service';
import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { TestCaseModel } from '../../models/test-case-model';
import { TestLinkService } from '../../testlink.service';
import { TestProjectModel } from '../../models/test-project-model';
import { TestSuiteModel } from '../../models/test-suite-model';

@Component({
  selector: 'testlink-test-case-form',
  templateUrl: './test-case-form.component.html',
  styleUrls: ['./test-case-form.component.scss']
})
export class TestCaseFormComponent implements OnInit {
  @ViewChild('caseNameInput') caseNameInput: ElementRef;

  testCase: TestCaseModel;
  testProject: TestProjectModel;
  testSuite: TestSuiteModel;
  currentPath: string = '';

  constructor(
    private titlesService: TitlesService,
    private testlinkService: TestLinkService, private route: ActivatedRoute,
  ) { }

  ngOnInit() {
    this.titlesService.setHeadTitle('Edit Test Case');
    this.testCase = new TestCaseModel();

    this.currentPath = this.route.snapshot.url[0].path;
    if (this.route.params !== null || this.route.params !== undefined) {
      if (this.currentPath === 'edit') {
        // this.route.params.switchMap((params: Params) => this.testlinkService.getCaseById(params['caseId']))
        //   .subscribe((case: TestCaseModel) => {
        //     this.testCase = case;
        //     this.titlesService.setTopTitle(this.testCase.getRouteString());
        //   });
      } else if (this.currentPath === 'new') {
        this.loadTestProject();
      }

    } else {
      window.history.back();
    }
  }

  ngAfterViewInit() {
    this.caseNameInput.nativeElement.focus();
  }

  loadTestProject(): void {
    this.route.params.switchMap((params: Params) => this.testlinkService.getProjectById(params['projectId']))
      .subscribe(
      (project: TestProjectModel) => {
        this.testProject = project;
        this.testCase.testProjectId = this.testProject.id;
        this.loadTestSuite();
      },
      (error) => console.log(error)
      );
  }

  loadTestSuite(): void {
    this.route.params.switchMap((params: Params) => this.testlinkService.getTestSuiteById(params['suiteId'], this.testProject.id))
      .subscribe(
      (suite: TestSuiteModel) => {
        this.testSuite = suite;
        this.testCase.testSuiteId = this.testSuite.id;
      },
      (error) => console.log(error)
      );
  }

  goBack(): void {
    window.history.back();
  }

  save(): void {
    this.testlinkService.createTestCase(this.testCase)
      .subscribe(
      (testCase: TestCaseModel) => this.postSave(testCase),
      (error) => this.testlinkService.popupService.openSnackBar('Error: Case with name ' + this.testCase.name + ' already exist'),
    );

  }

  postSave(testCase: TestCaseModel): void {
    console.log(testCase)
    this.testCase = this.testlinkService.eTTestlinkModelsTransformService.jsonToTestCaseModel(testCase);
    console.log(this.testCase)
    window.history.back();
  }

}
