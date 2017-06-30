import { ProjectModel } from './project-model';
import { Http, RequestOptions } from '@angular/http';
import { Injectable } from '@angular/core';
import { ConfigurationService } from '../../config/configuration-service.service';

@Injectable()
export class ProjectService {

  constructor(private http: Http, private confgurationService: ConfigurationService) { }

  transformDataToDataTable(projects: any[]) {
    let projectsDataToTable: ProjectModel[] = [];
    for (let project of projects) {
      projectsDataToTable.push(this.transformToProjectmodel(project));
    }
    return projectsDataToTable;
  }

  transformToProjectmodel(project: any) {
    let projectDataToTable: ProjectModel;

    projectDataToTable = new ProjectModel();
    projectDataToTable.id = project.id;
    projectDataToTable.name = project.name;
    projectDataToTable.suts = project.suts;
    projectDataToTable.tjobs = project.tjobs;
    return projectDataToTable;
  }

  public getProject(id: string) {
    let url = this.confgurationService.configModel.hostApi + '/project/' + id;
    return this.http.get(url)
      .map(response => this.transformToProjectmodel(response.json()));
  }

  public getProjects() {
    let url = this.confgurationService.configModel.hostApi + '/project';
    return this.http.get(url)
      .map(response => this.transformDataToDataTable(response.json()));
  }

  public createProject(project: ProjectModel) {
    let url = this.confgurationService.configModel.hostApi + '/project';
    return this.http.post(url, this.convertProjectToBackProject(project))
      .map(response => response.json());
  }

  public convertProjectToBackProject(project: ProjectModel){
    return {"id":project.id, "name": project.name};
  }

  public deleteProject(project: ProjectModel) {
    let url = this.confgurationService.configModel.hostApi + '/project/' + project.id;
    return this.http.delete(url)
      .map(response => response.json());
  }
}
