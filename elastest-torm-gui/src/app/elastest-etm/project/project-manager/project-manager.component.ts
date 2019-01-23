import { TitlesService } from '../../../shared/services/titles.service';
import { TdDialogService } from '@covalent/core';
import { IConfirmConfig } from '@covalent/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { ProjectService } from '../project.service';
import { ProjectModel } from '../project-model';
import { Component, OnInit, ViewContainerRef } from '@angular/core';
import { MatDialog } from '@angular/material';

@Component({
  selector: 'etm-project-manager',
  templateUrl: './project-manager.component.html',
  styleUrls: ['./project-manager.component.scss'],
})
export class ProjectManagerComponent implements OnInit {
  project: ProjectModel;
  projectId: string;
  deletingInProgress: boolean = false;

  constructor(
    private titlesService: TitlesService,
    private projectService: ProjectService,
    private route: ActivatedRoute,
    private router: Router,
    private _dialogService: TdDialogService,
    private _viewContainerRef: ViewContainerRef,
    public dialog: MatDialog,
  ) {}

  ngOnInit() {
    this.project = new ProjectModel();
    this.loadProject();
  }

  loadProject(): void {
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params
        .switchMap((params: Params) => this.projectService.getProject(params['projectId']))
        .subscribe((project: ProjectModel) => {
          this.project = project;
          this.projectId = project.id.toString();
          this.titlesService.setHeadTitle(this.project.name);
          this.titlesService.setPathName(this.router.routerState.snapshot.url);
        });
    }
  }

  editProject(): void {
    this.router.navigate(['/projects/edit', this.project.id]);
  }

  deleteProject(): void {
    let iConfirmConfig: IConfirmConfig = {
      message: 'Project ' + this.project.id + ':' + this.project.name + ' will be deleted, do you want to continue?',
      disableClose: false, // defaults to false
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
          this.projectService.deleteProject(this.project).subscribe(
            (project) => {
              this.deletingInProgress = false;
              this.router.navigate(['/projects']);
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
