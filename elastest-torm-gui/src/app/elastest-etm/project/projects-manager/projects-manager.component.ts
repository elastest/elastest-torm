import { TitlesService } from '../../../shared/services/titles.service';
import { Router } from '@angular/router';
import { ProjectModel } from '../project-model';
import { ProjectService } from '../project.service';
import { TdDataTableSortingOrder, TdDialogService, IConfirmConfig } from '@covalent/core';
import { AfterViewInit, Component, Input, OnInit, ViewContainerRef } from '@angular/core';
import { MatDialog } from '@angular/material';

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
  projectColumns: any[] = [
    { name: 'id', label: 'Id', width: 80 },
    { name: 'name', label: 'Project' },
    { name: 'options', label: 'Options' },
  ];

  projectData: ProjectModel[] = [];
  loading: boolean = true;

  sortBy: string = 'name';
  sortOrder: TdDataTableSortingOrder = TdDataTableSortingOrder.Ascending;

  projectChildsActived: boolean = false;
  projectSelected: ProjectModel = undefined;

  public deletingInProgress: boolean = false;

  constructor(
    private titlesService: TitlesService,
    private projectService: ProjectService,
    private router: Router,
    private _dialogService: TdDialogService,
    private _viewContainerRef: ViewContainerRef,
    public dialog: MatDialog,
  ) {}

  ngOnInit() {
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
    this.projectService.getProjects().subscribe((projects: ProjectModel[]) => {
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
            (error) => {
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
}
