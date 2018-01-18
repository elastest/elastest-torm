import { ActivatedRoute, Params } from '@angular/router';
import { TitlesService } from '../../../shared/services/titles.service';
import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { TestSuiteModel } from '../../models/test-suite-model';
import { TestLinkService } from '../../testlink.service';
import { TestProjectModel } from '../../models/test-project-model';

@Component({
  selector: 'app-test-suite-form',
  templateUrl: './test-suite-form.component.html',
  styleUrls: ['./test-suite-form.component.scss']
})
export class TestSuiteFormComponent implements OnInit {
  @ViewChild('suiteNameInput') suiteNameInput: ElementRef;

  testSuite: TestSuiteModel;
  testProject: TestProjectModel;
  currentPath: string = '';

  constructor(
    private titlesService: TitlesService,
    private testlinkService: TestLinkService, private route: ActivatedRoute,
  ) { }

  ngOnInit() {
    this.titlesService.setHeadTitle('Edit Test Suite');
    this.testSuite = new TestSuiteModel();
    this.currentPath = this.route.snapshot.url[0].path;
    if (this.route.params !== null || this.route.params !== undefined) {
      if (this.currentPath === 'edit') {
        // this.route.params.switchMap((params: Params) => this.testlinkService.getSuiteById(params['suiteId']))
        //   .subscribe((suite: TestSuiteModel) => {
        //     this.testSuite = suite;
        //     this.titlesService.setTopTitle(this.testSuite.getRouteString());
        //   });
      } else if (this.currentPath === 'new') {
        this.loadTestProject();
      }

    } else {
      window.history.back();
    }
  }

  ngAfterViewInit() {
    this.suiteNameInput.nativeElement.focus();
  }

  loadTestProject(): void {
    this.route.params.switchMap((params: Params) => this.testlinkService.getProjectById(params['projectId']))
      .subscribe(
      (project: TestProjectModel) => {
        this.testProject = project;
        this.testSuite.testProjectId = this.testProject.id;
      },
      (error) => console.log(error)
      );
  }

  goBack(): void {
    window.history.back();
  }

  save(): void {
    this.testlinkService.createTestSuite(this.testSuite)
      .subscribe(
      (suite) => this.postSave(suite),
      (error) => this.testlinkService.popupService.openSnackBar('Error: Suite with name ' + this.testSuite.name + ' already exist'),
    );

  }

  postSave(suite: any): void {
    this.testSuite = suite;
    window.history.back();
  }
}
