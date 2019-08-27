import { TitlesService } from '../../../shared/services/titles.service';
import { Router } from '@angular/router';
import { ProjectModel } from '../project-model';
import { ProjectService } from '../project.service';
import {
  TdDataTableSortingOrder,
  TdDialogService,
  IConfirmConfig,
  ITdDataTableSelectEvent,
  ITdDataTableSelectAllEvent,
  ITdDataTableColumn,
  TdDataTableService,
  ITdDataTableSortChangeEvent,
} from '@covalent/core';
import { AfterViewInit, Component, Input, OnInit, ViewContainerRef } from '@angular/core';
import { MatDialog } from '@angular/material';
import { PopupService } from '../../../shared/services/popup.service';
import { Observable, Subject } from 'rxjs';

@Component({
  selector: 'etm-projects-manager',
  templateUrl: './projects-manager.component.html',
  styleUrls: ['./projects-manager.component.scss'],
})
export class ProjectsManagerComponent implements OnInit, AfterViewInit {
  @Input()
  isNested: boolean = false;
  tableStyle: string = 'useMaxHeight';

  // Project data
  projectColumns: ITdDataTableColumn[] = [
    { name: 'id', label: 'Id', width: 80 },
    { name: 'name', label: 'Project' },
    { name: 'nTJobs', label: 'Nº TJobs', width: 87 },
    { name: 'nSuts', label: 'Nº Suts', width: 87 },
    { name: 'options', label: 'Options', sortable: false },
  ];

  sortBy: string = 'id';
  sortOrder: TdDataTableSortingOrder = TdDataTableSortingOrder.Ascending;

  projectData: ProjectModel[] = [];
  loading: boolean = true;

  projectChildsActived: boolean = false;

  public deletingInProgress: boolean = false;

  selectedProjectsIds: number[] = [];
  projectIdsWithErrorOnDelete: number[] = [];

  constructor(
    private titlesService: TitlesService,
    private projectService: ProjectService,
    private router: Router,
    private _dialogService: TdDialogService,
    private _viewContainerRef: ViewContainerRef,
    public dialog: MatDialog,
    private popupService: PopupService,
    private dataTableService: TdDataTableService,
  ) {}

  ngOnInit(): void {
    if (!this.isNested) {
      this.titlesService.setHeadTitle('Projects');
      this.tableStyle = 'useMaxHeight';
    } else {
      this.tableStyle = 'scroll_table';
    }

    this.loadProjects();
  }

  loadProjects(): void {
    this.loading = true;
    this.projectService.getProjects('medium').subscribe((projects: ProjectModel[]) => {
      this.projectData = projects;
      this.loading = false;
    });
  }

  ngAfterViewInit(): void {}

  editProject(project: ProjectModel): void {
    this.router.navigate(['/projects/edit', project.id]);
  }

  deleteProject(project: ProjectModel): void {
    let iConfirmConfig: IConfirmConfig = {
      message: 'Project ' + project.id + ':' + project.name + ' will be deleted, do you want to continue?',
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
          this.projectService.deleteProject(project).subscribe(
            (project: ProjectModel) => {
              this.loadProjects();
              this.deletingInProgress = false;
            },
            (error: Error) => {
              this.deletingInProgress = false;
              console.log(error);
            },
          );
        }
      });
  }

  viewProject(project: ProjectModel): void {
    this.router.navigate(['/projects', project.id]);
  }

  restoreDemoProjects(): void {
    this.projectService.restoreDemoProjects().subscribe(
      (restored: boolean) => {
        if (restored) {
          this.loadProjects();
        }
      },
      (error: Error) => console.log(error),
    );
  }

  switchProjectsSelectionByData(project: ProjectModel, selected: boolean): void {
    if (selected) {
      this.selectedProjectsIds.push(project.id);
    } else {
      const index: number = this.selectedProjectsIds.indexOf(project.id, 0);
      if (index > -1) {
        this.selectedProjectsIds.splice(index, 1);
      }
    }
  }

  switchProjectSelection(event: ITdDataTableSelectEvent): void {
    if (event && event.row) {
      let project: ProjectModel = event.row;
      this.switchProjectsSelectionByData(project, event.selected);
    }
  }

  switchAllProjectsSelection(event: ITdDataTableSelectAllEvent): void {
    if (event && event.rows) {
      for (let project of event.rows) {
        this.switchProjectsSelectionByData(project, event.selected);
      }
    }
  }

  removeSelectedProjects(): void {
    if (this.selectedProjectsIds.length > 0) {
      let iConfirmConfig: IConfirmConfig = {
        message: 'Selected Projects will be deleted, do you want to continue?',
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

            this.projectIdsWithErrorOnDelete = [];
            this.removeMultipleProjectsRecursively([...this.selectedProjectsIds]).subscribe(
              (end: boolean) => {
                if (this.projectIdsWithErrorOnDelete.length > 0) {
                  let errorMsg: string = 'Error on delete projects with ids: ' + this.projectIdsWithErrorOnDelete;
                  this.popupService.openSnackBar(errorMsg);
                } else {
                  this.popupService.openSnackBar('Projects ' + this.selectedProjectsIds + ' has been removed!');
                }

                this.deletingInProgress = false;
                this.loadProjects();
                this.projectIdsWithErrorOnDelete = [];
                this.selectedProjectsIds = [];
              },
              (error: Error) => {
                console.log(error);
                this.deletingInProgress = false;
                this.loadProjects();
                this.projectIdsWithErrorOnDelete = [];
                this.selectedProjectsIds = [];
              },
            );
          }
        });
    }
  }

  removeMultipleProjectsRecursively(selectedProjectsIds: number[], _obs: Subject<any> = new Subject<any>()): Observable<boolean> {
    let obs: Observable<any> = _obs.asObservable();

    if (selectedProjectsIds.length > 0) {
      let projectId: number = selectedProjectsIds.shift();

      this.projectService.deleteProjectById(projectId).subscribe(
        (project: ProjectModel) => {
          this.removeMultipleProjectsRecursively(selectedProjectsIds, _obs);
        },
        (error: Error) => {
          console.log(error);
          this.projectIdsWithErrorOnDelete.push(projectId);
          this.removeMultipleProjectsRecursively(selectedProjectsIds, _obs);
        },
      );
    } else {
      _obs.next(true);
    }

    return obs;
  }

  sort(sortEvent: ITdDataTableSortChangeEvent): void {
    this.sortBy = sortEvent.name;
    this.sortOrder = sortEvent.order;
    this.projectData = this.dataTableService.sortData(this.projectData, this.sortBy, this.sortOrder);
  }
}
