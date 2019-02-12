import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import 'rxjs/add/operator/map';
import 'rxjs/Rx';
import { CountFormat } from '../model/count-format.model';
import { Execution } from '../model/execution.model';
import { Project } from '../model/project.model';
import { Observable } from 'rxjs/Observable';

@Injectable()
export class ElasticsearchService {
  baseAPIUrl = 'http://localhost:8443/';
  baseAPIExecutionsUrl = this.baseAPIUrl + 'api/executions';
  baseAPILogsUrl = this.baseAPIUrl + 'api/logs';
  baseAPIDiffMatchPatchUrl = this.baseAPIUrl + 'api/diff';
  baseAPIProjectsUrl = this.baseAPIUrl + 'api/projects';
  baseELASTICSEARCHUrl = 'http://localhost:9200/';

  constructor(private http: HttpClient) {}

  getCountOfProjects() {
    return this.http
      .get<CountFormat>(this.baseELASTICSEARCHUrl + 'projects/_count')
      .map((response) => response.count, (error) => error);
  }

  async getExecutionByIdAsync(id: string) {
    try {
      const response = await this.http.get(this.baseAPIExecutionsUrl + '?id=' + id).toPromise();
      return response as Execution;
    } catch (error) {
      console.log(error);
    }
  }

  async getExecutionsByProjectAsync(project: string) {
    try {
      const response = await this.http.get(this.baseAPIExecutionsUrl + '?project=' + project).toPromise();
      return response as Execution[];
    } catch (error) {
      console.log(error);
    }
  }

  deleteExecutionById(id: string) {
    return this.http.delete(this.baseAPIExecutionsUrl + '/' + id).map((response) => response, (error) => error);
  }

  async getLogsByLoggerAsync(logger: string, project: string, test: string, method?: string) {
    try {
      let composedUrl = this.baseAPILogsUrl + '?logger=' + logger + '&project=' + project + '&test=' + test;
      if (method !== undefined) {
        composedUrl += '&method=' + method;
      }
      const response = await this.http.get(composedUrl).toPromise();
      return response as string[];
    } catch (error) {
      console.log(error);
    }
  }

  async getLogsByTestAsync(test: number, project: string, classes: boolean, maven?: boolean) {
    try {
      let composedUrl = this.baseAPILogsUrl + '/' + test + '?project=' + project + '&classes=' + classes;
      (composedUrl += '&maven=' + maven) && maven;
      const response = await this.http.get(composedUrl).toPromise();
      return response as string[];
    } catch (error) {
      console.log(error);
    }
  }

  getProjectsAll() {
    return this.http
      .get(this.baseAPIProjectsUrl)
      .map((response) => response as Project[])
      .catch((error) => Observable.throw('No projects available. You must create the first project to see it.'));
  }

  getProjectByName(name: string) {
    return this.http
      .get(this.baseAPIProjectsUrl + '/' + name)
      .map((response) => response as Project, (error) => 'No project found with the given name.');
  }

  deleteProjectById(id: number) {
    return this.http.delete(this.baseAPIProjectsUrl + '/' + id).map((response) => response, (error) => error);
  }

  async postFile(files: File[], project: string) {
    try {
      const body = new FormData();
      for (let i = 0; i < files.length; i++) {
        body.append('files', files[i]);
      }
      const headers = new HttpHeaders();
      headers.append('Content-Type', 'application/pdf');
      const composedUrl = this.baseAPIProjectsUrl + '/' + project;
      const response = await this.http.post(composedUrl, body, { headers: headers }).toPromise();
      return response;
    } catch (error) {
      console.log(error);
    }
  }

  async downloadResource(url): Promise<Blob> {
    const file = await this.http.get<Blob>(url, { responseType: 'blob' as 'json' }).toPromise();
    return file;
  }

  async postDiff(text1: string, text2: string) {
    try {
      const body = { text1: text1, text2: text2 };
      const headers = new HttpHeaders();
      headers.append('Content-Type', 'text/plain');
      const response = await this.http
        .post(this.baseAPIDiffMatchPatchUrl, JSON.stringify(body), {
          headers: headers,
          responseType: 'text',
        })
        .toPromise();
      return response;
    } catch (error) {
      console.log(error);
    }
  }
}
