import { ActivatedRoute, Params } from '@angular/router';
import { TitlesService } from '../../../shared/services/titles.service';
import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { TestProjectModel } from '../../models/test-project-model';
import { TestLinkService } from '../../testlink.service';

@Component({
  selector: 'testlink-test-project-form',
  templateUrl: './test-project-form.component.html',
  styleUrls: ['./test-project-form.component.scss']
})
export class TestProjectFormComponent implements OnInit {

  @ViewChild('projectNameInput') projectNameInput: ElementRef;

  testProject: TestProjectModel;
  currentPath: string = '';

  constructor(
    private titlesService: TitlesService,
    private testlinkService: TestLinkService, private route: ActivatedRoute,
  ) { }

  ngOnInit() {
    this.titlesService.setHeadTitle('Edit Test Project');
    this.testProject = new TestProjectModel();
    this.currentPath = this.route.snapshot.url[0].path;
    if (this.route.params !== null || this.route.params !== undefined) {
      if (this.currentPath === 'edit') {
        this.route.params.switchMap((params: Params) => this.testlinkService.getProjectById(params['projectId']))
          .subscribe((project: TestProjectModel) => {
            this.testProject = project;
            this.titlesService.setTopTitle(this.testProject.getRouteString());
          });
      }
    }
  }

  ngAfterViewInit() {
    this.projectNameInput.nativeElement.focus();
  }

  goBack(): void {
    window.history.back();
  }

  save(): void {
    this.testlinkService.createProject(this.testProject)
      .subscribe(
      (project) => this.postSave(project),
      (error) => this.testlinkService.popupService.openSnackBar('Error: Project with name ' + this.testProject.name + ' already exist'),
    );

  }

  postSave(project: any): void {
    this.testProject = project;
    window.history.back();
  }

}
