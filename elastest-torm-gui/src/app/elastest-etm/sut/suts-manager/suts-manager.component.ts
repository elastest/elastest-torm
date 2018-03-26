import { TitlesService } from '../../../shared/services/titles.service';
import { SutModel } from '../../sut/sut-model';
import { TdDialogService } from '@covalent/core/dialogs/services/dialog.service';
import { SutService } from '../../sut/sut.service';
import { TJobExecService } from '../../tjob-exec/tjobExec.service';
import { TJobService } from '../../tjob/tjob.service';
import { IConfirmConfig } from '@covalent/core';
import { TJobModel } from '../../tjob/tjob-model';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Component, OnInit, ViewContainerRef, Input } from '@angular/core';
import { MdDialog } from '@angular/material';
import { RunTJobModalComponent } from '../../tjob/run-tjob-modal/run-tjob-modal.component';
import { ProjectService } from '../../project/project.service';
import { ProjectModel } from '../../project/project-model';
import { ExternalProjectModel } from '../../external/external-project/external-project-model';
import { ExternalService } from '../../external/external.service';

@Component({
  selector: 'etm-suts-manager',
  templateUrl: './suts-manager.component.html',
  styleUrls: ['./suts-manager.component.scss'],
})
export class SutsManagerComponent implements OnInit {
  @Input() projectId: string;
  @Input() exProjectId: string;

  project: ProjectModel;
  exProject: ExternalProjectModel;
  parentType: string;

  suts: SutModel[] = [];
  showSpinner: boolean = true;

  deletingInProgress: boolean = false;

  // SuT Data
  sutColumns: any[] = [
    { name: 'id', label: 'Id' },
    { name: 'name', label: 'Name' },
    { name: 'specification', label: 'Specification' },
    { name: 'sutType', label: 'SuT Type' },
    { name: 'description', label: 'Description' },
    { name: 'options', label: 'Options' },
  ];

  constructor(
    private titlesService: TitlesService,
    private projectService: ProjectService,
    private route: ActivatedRoute,
    private router: Router,
    private externalService: ExternalService,
    private sutService: SutService,
    private _dialogService: TdDialogService,
    private _viewContainerRef: ViewContainerRef,
    public dialog: MdDialog,
  ) {}

  ngOnInit() {
    this.init();
  }

  init(): void {
    // If child
    if (this.projectId || this.exProjectId) {
      if (this.projectId) {
        this.loadProjectAndSuts(this.projectId);
      } else if (this.exProjectId) {
        this.loadExternalProjectAndSuts(this.exProjectId);
      }
    } else if (this.route.params !== null || this.route.params !== undefined) {
      // If routing
      this.route.params.subscribe((params: Params) => {
        if (params['projectId']) {
          this.loadProjectAndSuts(params['projectId']);
        } else if (params['exProjectId']) {
          this.loadExternalProjectAndSuts(params['exProjectId']);
        } else {
          // Get all suts
          this.loadAllSuts();
        }
      });
    } else {
      this.loadAllSuts();
    }
  }

  loadProjectAndSuts(projectId: string): void {
    this.projectService.getProject(projectId).subscribe((project: ProjectModel) => {
      this.project = project;
      this.parentType = 'ProjectModel';
      if (project) {
        this.suts = this.project.suts;
        this.showSpinner = false;
      }
    });
  }

  loadExternalProjectAndSuts(exProjectId: string | number): void {
    this.externalService.getExternalProjectById(exProjectId).subscribe((exProject: ExternalProjectModel) => {
      this.exProject = exProject;
      this.parentType = 'ExternalProjectModel';
      if (exProject) {
        this.suts = this.exProject.suts;
        this.showSpinner = false;
      }
    });
  }

  loadAllSuts(): void {
    this.sutService.getSuts().subscribe(
      (suts: SutModel[]) => {
        this.suts = suts;
      },
      (error) => console.log(error),
    );
  }

  newSut(): void {
    if (this.parentType === 'ExternalProjectModel') {
      this.router.navigate(['/external/project', this.exProject.id, 'sut', 'new']);
    } else if (this.parentType === 'ProjectModel') {
      this.router.navigate(['/projects', this.project.id, 'sut', 'new']);
    }
  }

  viewSut(sut: SutModel): void {
    if (this.parentType === 'ExternalProjectModel') {
      this.router.navigate(['/external/project', this.exProject.id, 'sut', sut.id]);
    } else if (this.parentType === 'ProjectModel') {
      this.router.navigate(['/projects', this.project.id, 'sut', sut.id]);
    }
  }

  editSut(sut: SutModel): void {
    if (this.parentType === 'ExternalProjectModel') {
      this.router.navigate(['/external/project', this.exProject.id, 'sut', 'edit', sut.id]);
    } else if (this.parentType === 'ProjectModel') {
      this.router.navigate(['/projects', this.project.id, 'sut', 'edit', sut.id]);
    }
  }

  deleteSut(sut: SutModel): void {
    let iConfirmConfig: IConfirmConfig = {
      message:
        'Sut ' +
        sut.id +
        " will be deleted with all SuT Executions, do you want to continue? (SuT only will be deleted if hasn't associated TJobs)",
      disableClose: false,
      viewContainerRef: this._viewContainerRef,
      title: 'Confirm',
      cancelButton: 'Cancel',
      acceptButton: 'Yes, delete',
    };
    this._dialogService
      .openConfirm(iConfirmConfig)
      .afterClosed()
      .subscribe((accept: boolean) => {
        if (accept) {
          this.deletingInProgress = true;
          this.sutService.deleteSut(sut).subscribe(
            (sut) => {
              this.deletingInProgress = false;
              this.init();
            },
            (error) => {
              this.deletingInProgress = false;
              console.log(error);
            },
          );
        }
      });
  }
}
