import { TitlesService } from '../../../shared/services/titles.service';
import { Router } from '@angular/router';
import { ProjectModel } from '../project-model';
import { ProjectService } from '../project.service';
import { Meta, Title } from '@angular/platform-browser';
import {
  IPageChangeEvent,
  ITdDataTableRowClickEvent,
  ITdDataTableSortChangeEvent,
  TdDataTableService,
  TdDataTableSortingOrder,
  TdDialogService,
  IConfirmConfig,
} from '@covalent/core';
import { AfterViewInit, Component, Input, OnInit, ViewContainerRef } from '@angular/core';
import { MdDialog } from '@angular/material';

@Component({
  selector: 'etm-projects-manager',
  templateUrl: './projects-manager.component.html',
  styleUrls: ['./projects-manager.component.scss']
})
export class ProjectsManagerComponent implements OnInit, AfterViewInit {

  @Input()
  isNested: boolean = false;
  tableStyle: string = "without_scroll_table";

  // Project data
  projectColumns: any[] = [
    { name: 'id', label: 'Id' },
    { name: 'name', label: 'Project' },
    { name: 'options', label: 'Options' },
  ];

  projectData: ProjectModel[] = [];
  showSpinner: boolean = true;

  sortBy: string = 'name';
  sortOrder: TdDataTableSortingOrder = TdDataTableSortingOrder.Ascending;

  projectChildsActived: boolean = false;
  projectSelected: ProjectModel = undefined;

  deletingInProgress: boolean = false;

  constructor(
    private titlesService: TitlesService,
    private _dataTableService: TdDataTableService, private projectService: ProjectService, private router: Router,
    private _dialogService: TdDialogService, private _viewContainerRef: ViewContainerRef,
    public dialog: MdDialog,
  ) { }

  ngOnInit() {
    if (!this.isNested) {
      this.titlesService.setHeadTitle('Projects');
      this.tableStyle = "without_scroll_table";
    } else {
      this.tableStyle = "scroll_table";
    }

    this.loadProjects();
  }

  loadProjects() {
    this.projectService.getProjects()
      .subscribe(
      (projects) => {
        this.projectData = projects;
        this.showSpinner = false;
      },
    );
  }

  ngAfterViewInit(): void {
  }

  editProject(project: ProjectModel) {
    this.router.navigate(['/projects/edit', project.id]);
  }

  deleteProject(project: ProjectModel) {
    let iConfirmConfig: IConfirmConfig = {
      message: 'Project ' + project.id + ':' + project.name + ' will be deleted, do you want to continue?',
      disableClose: false, // defaults to false
      viewContainerRef: this._viewContainerRef,
      title: 'Confirm',
      cancelButton: 'Cancel',
      acceptButton: 'Yes, delete',
    };
    this._dialogService.openConfirm(iConfirmConfig).afterClosed().subscribe((accept: boolean) => {
      if (accept) {
        this.deletingInProgress = true;
        this.projectService.deleteProject(project).subscribe(
          (project) => {
            this.loadProjects();
            this.deletingInProgress = false;
          },
          (error) => {
            this.deletingInProgress = false;
            console.log(error)
          }
        );
      }
    });
  }

  viewProject(project: ProjectModel) {
    this.router.navigate(['/projects', project.id]);
  }
}
