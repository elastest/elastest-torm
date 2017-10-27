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
  esmServicesCatalog: EsmServiceModel[];
  action: string;

  constructor(private tJobService: TJobService, private route: ActivatedRoute,
    private projectService: ProjectService, private esmService: EsmService) {
    this.esmServicesCatalog = [];
  }

  ngOnInit() {
    this.tJob = new TJobModel();
    this.action = this.route.snapshot.url[0].path;
    if (this.route.params !== null || this.route.params !== undefined) {
      this.esmService.getSupportServices()
        .subscribe((response) => {
          this.esmServicesCatalog = response;

          if (this.action === 'edit') {
            this.editMode = true;
            this.route.params.switchMap((params: Params) => this.tJobService.getTJob(params['tJobId']))
              .subscribe((tJob: TJobModel) => {
                this.tJob = tJob;
                this.currentSut = tJob.sut.id > 0 ? tJob.sut.id.toString() : 'None';
                this.withCommands = this.tJob.withCommands();
                for (let esmService of this.tJob.esmServices) {
                  for (let esmServiceToSelect of this.esmServicesCatalog) {
                    if (esmService.selected && esmService.id === esmServiceToSelect.id) {
                      esmServiceToSelect.selected = true;
                    }
                  }
                }
              });
          } else if (this.action === 'new') {
            this.route.params.switchMap((params: Params) => this.projectService.getProject(params['projectId'], true))
              .subscribe(
              (project: ProjectModel) => {
                this.tJob = new TJobModel();
                this.tJob.project = project;
              },
            );
          }
        });
    }
  }

  goBack(): void {
    window.history.back();
  }

  save() {
    if (!this.withCommands) {
      this.tJob.commands = '';
    }

    this.tJob.esmServices = this.esmServicesCatalog;
    console.log("Services " + JSON.stringify(this.tJob.esmServices));

    this.tJobService.createTJob(this.tJob, this.action)
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
