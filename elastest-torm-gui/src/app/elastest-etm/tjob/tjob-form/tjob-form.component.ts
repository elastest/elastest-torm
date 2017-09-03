import { EsmService } from '../../../elastest-esm/esm-service.service';
import { EsmServiceModel } from '../../../elastest-esm/esm-service.model';
import { ProjectModel } from '../../project/project-model';
import { ProjectService } from '../../project/project.service';
import { SutModel } from '../../sut/sut-model';
import { TJobModel } from '../tjob-model';
import { TJobService } from '../tjob.service';

import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';

@Component({
  selector: 'etm-tjob-form',
  templateUrl: './tjob-form.component.html',
  styleUrls: ['./tjob-form.component.scss']
})
export class TJobFormComponent implements OnInit {

  tJob: TJobModel;
  editMode: boolean = false;

  sutEmpty: SutModel = new SutModel();
  currentSut: string = 'None';
  withCommands: boolean = false;
  
  elastestEsmServices: string[];


  constructor(private tJobService: TJobService, private route: ActivatedRoute,
    private projectService: ProjectService, private esmService: EsmService) { }

  ngOnInit() {
    this.tJob = new TJobModel();
    let currentPath: string = this.route.snapshot.url[0].path;
    if (this.route.params !== null || this.route.params !== undefined) {
      if (currentPath === 'edit') {
        this.editMode = true;
        this.route.params.switchMap((params: Params) => this.tJobService.getTJob(params['tJobId']))
          .subscribe((tJob: TJobModel) => {
            this.tJob = tJob;
            this.currentSut = tJob.sut.id > 0 ? tJob.sut.id.toString() : 'None';
            this.withCommands = this.tJob.withCommands();            
          });
      }
      else if (currentPath === 'new') {
        this.route.params.switchMap((params: Params) => this.projectService.getProject(params['projectId']))
          .subscribe(
          (project: ProjectModel) => {
            this.tJob = new TJobModel();
            this.tJob.project = project;
            this.loadEsmServices();
          },
        );
      }
    }
  }

  loadEsmServices() {
    this.esmService.getElastestESMServices()
      .subscribe((response) => {
        this.elastestEsmServices = response;
        let serviceId = 0;
        for (let serviceName of this.elastestEsmServices) {
          this.tJob.esmServicesCatalogArray.push(new EsmServiceModel( serviceId, serviceName, false ));
          serviceId++;
        }
      });
  }

  goBack(): void {
    window.history.back();
  }

  save() {
    if (!this.withCommands) {
      this.tJob.commands = '';
    }

    this.tJobService.createTJob(this.tJob)
      .subscribe(
      tJob => this.postSave(tJob),
      error => console.log(error)
      );

  }

  postSave(tJob: any) {
    this.tJob = tJob;
    window.history.back();
  }

  cancel() {
    window.history.back();
  }
}
