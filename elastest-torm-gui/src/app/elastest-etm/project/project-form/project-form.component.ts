import { TitlesService } from '../../../shared/services/titles.service';
import { ActivatedRoute, Params } from '@angular/router';
import { ProjectService } from '../project.service';
import { ProjectModel } from '../project-model';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-project-form',
  templateUrl: './project-form.component.html',
  styleUrls: ['./project-form.component.scss']
})
export class ProjectFormComponent implements OnInit {

  project: ProjectModel;
  editMode: boolean = false;

  constructor(
    private titlesService: TitlesService,
    private projectService: ProjectService, private route: ActivatedRoute,
  ) { }

  ngOnInit() {
    this.titlesService.setHeadTitle('Edit Project');
    this.project = new ProjectModel();
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params.switchMap((params: Params) => this.projectService.getProject(params['projectId']))
        .subscribe((project: ProjectModel) => {
          this.project = project;
          this.titlesService.setTopTitle(this.project.getRouteString());
        });
    }

  }

  goBack(): void {
    window.history.back();
  }

  save() {
    this.projectService.createProject(this.project)
      .subscribe(
      project => this.postSave(project),
      error => console.log(error)
      );

  }

  postSave(project: any) {
    this.project = project;
    window.history.back();
  }

}
