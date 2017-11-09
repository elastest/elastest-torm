import { TitlesService } from '../../../shared/services/titles-service';
import { SutModel } from '../../sut/sut-model';
import { TJobExecModel } from '../../tjob-exec/tjobExec-model';
import { TdDialogService } from '@covalent/core/dialogs/services/dialog.service';
import { SutService } from '../../sut/sut.service';
import { TJobExecService } from '../../tjob-exec/tjobExec.service';
import { TJobService } from '../../tjob/tjob.service';
import { IConfirmConfig } from '@covalent/core';
import { TJobModel } from '../../tjob/tjob-model';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { ProjectService } from '../project.service';
import { ProjectModel } from '../project-model';
import { Component, OnInit, ViewContainerRef } from '@angular/core';
import { MdDialog } from '@angular/material';
import { RunTJobModalComponent } from '../../tjob/run-tjob-modal/run-tjob-modal.component';

@Component({
  selector: 'app-project-manager',
  templateUrl: './project-manager.component.html',
  styleUrls: ['./project-manager.component.scss']
})
export class ProjectManagerComponent implements OnInit {
  project: ProjectModel;

  // SuT Data
  sutColumns: any[] = [
    { name: 'id', label: 'Id' },
    { name: 'name', label: 'Name' },
    { name: 'specification', label: 'Specification' },
    { name: 'sutType', label: 'SuT Type' },
    { name: 'description', label: 'Description' },
    { name: 'options', label: 'Options' },
  ];

  // TJob Data
  tjobColumns: any[] = [
    { name: 'id', label: 'Id' },
    { name: 'name', label: 'Name' },
    { name: 'imageName', label: 'Image Name' },
    { name: 'sut', label: 'Sut' },
    { name: 'options', label: 'Options' },
  ];

  constructor(
    private titlesService: TitlesService, private projectService: ProjectService,
    private route: ActivatedRoute, private router: Router,
    private tJobService: TJobService, private tJobExecService: TJobExecService, private sutService: SutService,
    private _dialogService: TdDialogService, private _viewContainerRef: ViewContainerRef,

    public dialog: MdDialog,
  ) { }

  ngOnInit() {
    this.titlesService.setHeadTitle('Project');
    this.project = new ProjectModel();
    this.loadProject();
  }

  loadProject() {
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params.switchMap((params: Params) => this.projectService.getProject(params['projectId']))
        .subscribe((project: ProjectModel) => {
          this.project = project;
          this.titlesService.setTopTitle(this.project.getRouteString());
        });
    }
  }


  /****** TJobs functions ******/

  newTJob() {
    this.router.navigate(['/projects', this.project.id, 'tjob', 'new']);
  }

  runTJob(tJob: TJobModel, project: ProjectModel) {
    if (tJob.parameters.length > 0) {
      tJob.project = project;
      let dialogRef = this.dialog.open(RunTJobModalComponent, {
        data: tJob.cloneTJob(),
      });
    } else {
      this.tJobExecService.runTJob(tJob.id, tJob.parameters)
        .subscribe(
        (tjobExecution: TJobExecModel) => {
          this.router.navigate(['/projects', this.project.id, 'tjob', tJob.id, 'tjob-exec', tjobExecution.id, 'dashboard']);
        },
        (error) => console.error('Error:' + error),
      );
    }
  }
  editTJob(tJob: TJobModel) {
    this.router.navigate(['/projects', this.project.id, 'tjob', 'edit', tJob.id]);
  }
  deleteTJob(tJob: TJobModel) {
    let iConfirmConfig: IConfirmConfig = {
      message: 'TJob ' + tJob.id + ' will be deleted with all TJob Executions, do you want to continue?',
      disableClose: false,
      viewContainerRef: this._viewContainerRef,
      title: 'Confirm',
      cancelButton: 'Cancel',
      acceptButton: 'Yes, delete',
    };
    this._dialogService.openConfirm(iConfirmConfig).afterClosed().subscribe((accept: boolean) => {
      if (accept) {
        this.tJobService.deleteTJob(tJob).subscribe(
          (tJob) => this.loadProject(),
          (error) => console.log(error)
        );
      }
    });
  }

  viewTJob(tJob: TJobModel) {
    this.router.navigate(['/projects', this.project.id, 'tjob', tJob.id]);
  }


  /****** Suts functions ******/

  newSut() {
    this.router.navigate(['/projects', this.project.id, 'sut', 'new']);
  }


  editSut(sut: SutModel) {
    this.router.navigate(['/projects', this.project.id, 'sut', 'edit', sut.id]);
  }
  deleteSut(sut: SutModel) {
    let iConfirmConfig: IConfirmConfig = {
      message: 'Sut ' + sut.id
      + ' will be deleted with all SuT Executions, do you want to continue? (SuT only will be deleted if hasn\'t associated TJobs)',
      disableClose: false,
      viewContainerRef: this._viewContainerRef,
      title: 'Confirm',
      cancelButton: 'Cancel',
      acceptButton: 'Yes, delete',
    };
    this._dialogService.openConfirm(iConfirmConfig).afterClosed().subscribe((accept: boolean) => {
      if (accept) {
        this.sutService.deleteSut(sut).subscribe(
          (sut) => this.loadProject(),
          (error) => console.log(error)
        );
      }
    });
  }

  viewSut(sut: SutModel) {
    this.router.navigate(['/projects', this.project.id, 'sut', sut.id]);
  }

}
