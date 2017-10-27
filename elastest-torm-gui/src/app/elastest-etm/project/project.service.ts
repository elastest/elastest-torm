import { ProjectModel } from './project-model';
import { Http, RequestOptions } from '@angular/http';
import { Injectable } from '@angular/core';
import { ConfigurationService } from '../../config/configuration-service.service';
import { TJobService } from '../tjob/tjob.service';
import { SutService } from '../sut/sut.service';

@Injectable()
export class ProjectService {

  constructor(private http: Http, private confgurationService: ConfigurationService,
    private tJobService: TJobService, private sutService: SutService) { }

  transformDataToDataTable(projects: any[]) {
    let projectsDataToTable: ProjectModel[] = [];
    for (let project of projects) {
      projectsDataToTable.push(this.transformToProjectmodel(project));
    }
    return projectsDataToTable;
  }

  transformToProjectmodel(project: any, onlyProject: boolean = false) {
    let projectDataToTable: ProjectModel;

    projectDataToTable = new ProjectModel();
    projectDataToTable.id = project.id;
    projectDataToTable.name = project.name;
    if (!onlyProject) {
      projectDataToTable.suts = this.sutService.transformSutDataToDataTable(project.suts);
      projectDataToTable.tjobs = this.tJobService.transformTJobDataToDataTable(project.tjobs);
    }
    return projectDataToTable;
  }

  public getProject(id: string, onlyProject: boolean = false) {
    let url = this.confgurationService.configModel.hostApi + '/project/' + id;
    return this.http.get(url)
      .map(
      (response) => {
        let data: any = response.json();
        if (data !== undefined && data !== null) {
          return this.transformToProjectmodel(data, onlyProject);
        }
        else {
          throw new Error('Empty response. Project not exist or you don\'t have permissions to access it');
        }
      });
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

  public convertProjectToBackProject(project: ProjectModel) {
    return { "id": project.id, "name": project.name };
  }

  public deleteProject(project: ProjectModel) {
    let url = this.confgurationService.configModel.hostApi + '/project/' + project.id;
    return this.http.delete(url)
      .map(response => response.json());
  }
}
