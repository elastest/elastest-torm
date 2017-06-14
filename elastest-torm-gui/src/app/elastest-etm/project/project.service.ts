import { ProjectModel } from './project-model';
import { Http } from '@angular/http';
import { Injectable } from '@angular/core';
import { ETM_API } from '../../../config/api.config';

@Injectable()
export class ProjectService {

  constructor(private http: Http) { }

 
  public getProject(tJobId: number){

    let url = ETM_API + '/project/{'+tJobId+'}';
    return this.http.get(url)
      .map( response => response.json());
  }
 
  public getProjects(){

    let url = ETM_API + '/project';
    return this.http.get(url)
      .map( response => this.transformDataToDataTable(response.json()));
  }

  transformDataToDataTable(projects: any){
    let projectsDataToTable: ProjectModel[] = []; 
    let projectDataToTable: ProjectModel;
    for(let project of projects){
      console.log(project.name);
      projectDataToTable = new ProjectModel();
      projectDataToTable.id = project.id;
      projectDataToTable.name = project.name;
      projectsDataToTable.push(projectDataToTable);
    }
    return projectsDataToTable;
  }

  public createProject(project: ProjectModel){
    let url = ETM_API + '/project' ;
    return this.http.post(url, project)
      .map( response => response.json())
  }
}
