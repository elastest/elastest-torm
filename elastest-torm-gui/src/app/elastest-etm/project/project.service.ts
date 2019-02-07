import { ETModelsTransformServices } from '../../shared/services/et-models-transform.service';
import { ProjectModel } from './project-model';
import { Injectable } from '@angular/core';
import { ConfigurationService } from '../../config/configuration-service.service';
import { Observable } from 'rxjs';
import { HttpClient, HttpResponse } from '@angular/common/http';

@Injectable()
export class ProjectService {
  constructor(
    private http: HttpClient,
    private configurationService: ConfigurationService,
    private eTModelsTransformServices: ETModelsTransformServices,
  ) {}

  public getProject(id: string, onlyProject: boolean = false): Observable<ProjectModel> {
    let url: string = this.configurationService.configModel.hostApi + '/project/' + id;
    return this.http.get(url).map((data: any) => {
      if (data !== undefined && data !== null) {
        return this.eTModelsTransformServices.jsonToProjectModel(data, onlyProject);
      } else {
        throw new Error("Empty response. Project not exist or you don't have permissions to access it");
      }
    });
  }

  public getProjects(): Observable<ProjectModel[]> {
    let url: string = this.configurationService.configModel.hostApi + '/project';
    return this.http.get(url).map((data: any) => this.eTModelsTransformServices.jsonToProjectsList(data));
  }

  public createProject(project: ProjectModel): Observable<any> {
    let url: string = this.configurationService.configModel.hostApi + '/project';
    return this.http
      .post(url, this.convertProjectToBackProject(project), { observe: 'response' })
      .map((response: HttpResponse<any>) => response.body);
  }

  public convertProjectToBackProject(project: ProjectModel): object {
    return { id: project.id, name: project.name };
  }

  public deleteProject(project: ProjectModel): Observable<any> {
    return this.deleteProjectById(project.id);
  }

  public deleteProjectById(projectId: string | number): Observable<any> {
    let url: string = this.configurationService.configModel.hostApi + '/project/' + projectId;
    return this.http.delete(url).map((data: any) => data);
  }

  public restoreDemoProjects(): Observable<boolean> {
    let url: string = this.configurationService.configModel.hostApi + '/project/restore';
    return this.http.get(url).map((data: any) => data);
  }
}
