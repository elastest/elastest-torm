import { ETModelsTransformServices } from '../../shared/services/et-models-transform.service';
import { ProjectModel } from './project-model';
import { Http, RequestOptions } from '@angular/http';
import { Injectable } from '@angular/core';
import { ConfigurationService } from '../../config/configuration-service.service';
import { Observable } from 'rxjs';

@Injectable()
export class ProjectService {
  constructor(
    private http: Http,
    private configurationService: ConfigurationService,
    private eTModelsTransformServices: ETModelsTransformServices,
  ) {}

  public getProject(id: string, onlyProject: boolean = false) {
    let url: string = this.configurationService.configModel.hostApi + '/project/' + id;
    return this.http.get(url).map((response) => {
      let data: any = response.json();
      if (data !== undefined && data !== null) {
        return this.eTModelsTransformServices.jsonToProjectModel(data, onlyProject);
      } else {
        throw new Error("Empty response. Project not exist or you don't have permissions to access it");
      }
    });
  }

  public getProjects(): Observable<ProjectModel[]> {
    let url: string = this.configurationService.configModel.hostApi + '/project';
    return this.http.get(url).map((response) => this.eTModelsTransformServices.jsonToProjectsList(response.json()));
  }

  public createProject(project: ProjectModel) {
    let url: string = this.configurationService.configModel.hostApi + '/project';
    return this.http.post(url, this.convertProjectToBackProject(project)).map((response) => response.json());
  }

  public convertProjectToBackProject(project: ProjectModel) {
    return { id: project.id, name: project.name };
  }

  public deleteProject(project: ProjectModel) {
    let url: string = this.configurationService.configModel.hostApi + '/project/' + project.id;
    return this.http.delete(url).map((response) => response.json());
  }

  public restoreDemoProjects(): Observable<boolean> {
    let url: string = this.configurationService.configModel.hostApi + '/project/restore';
    return this.http.get(url).map((response) => response.json());
  }
}
