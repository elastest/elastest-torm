import { ProjectModel } from './project-model';
import { Http, RequestOptions } from '@angular/http';
import { Injectable } from '@angular/core';
import { ETM_API } from '../../../config/api.config';

@Injectable()
export class ProjectService {

  constructor(private http: Http) { }


  public getProject(id: string) {

    let url = ETM_API + '/project/' + id;
    return this.http.get(url)
      .map(response => response.json());
  }

  public getProjects() {
    let url = ETM_API + '/project';
    return this.http.get(url)
      .map(response => this.transformDataToDataTable(response.json()));
  }

  transformDataToDataTable(projects: any[]) {
    let projectsDataToTable: ProjectModel[] = [];
    /*let projectDataToTable: ProjectModel;*/
    for (let project of projects) {
      console.log(project.name);
      /*projectDataToTable = new ProjectModel();
      projectDataToTable.id = project.id;
      projectDataToTable.name = project.name;*/
      projectsDataToTable.push(this.transformToProjectmodel(project));
    }
    return projectsDataToTable;
  }

  transformToProjectmodel(project: any) {
    let projectDataToTable: ProjectModel;

    projectDataToTable = new ProjectModel();
    projectDataToTable.id = project.id;
    projectDataToTable.name = project.name;

    return projectDataToTable;
  }

  public createProject(project: ProjectModel) {
    let url = ETM_API + '/project';
    return this.http.post(url, project)
      .map(response => response.json());
  }

  public deleteProject(project: ProjectModel) {
    let url = ETM_API + '/project/'+ project.id;
    return this.http.delete(url)
      .map(response => response.json());
  }
}
