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

  constructor(private projectService: ProjectService) { }

  ngOnInit() {
    this.project = new ProjectModel();
  }

  goBack(): void {
    window.history.back();
  }

  save(){
    this.projectService.createProject(this.project)
    .subscribe(
      project => this.postSave(project),
      error => console.log("Error creating project.")
    );

  }

  postSave(project: any){
    this.project = project;
    window.history.back();
  }

}
