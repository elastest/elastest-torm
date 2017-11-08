import { Router } from '@angular/router';
import { ProjectModel } from '../project-model';
import { ProjectService } from '../project.service';
import { Title } from '@angular/platform-browser';
import {
  IPageChangeEvent,
  ITdDataTableRowClickEvent,
  ITdDataTableSortChangeEvent,
  TdDataTableService,
  TdDataTableSortingOrder,
  TdDialogService,
  IConfirmConfig,
} from '@covalent/core';
import { AfterViewInit, Component, OnInit, ViewContainerRef } from '@angular/core';
import { MdDialog } from '@angular/material';

@Component({
  selector: 'etm-projects-manager',
  templateUrl: './projects-manager.component.html',
  styleUrls: ['./projects-manager.component.scss']
})
export class ProjectsManagerComponent implements OnInit, AfterViewInit {

  // Project data
  projectColumns: any[] = [
    { name: 'id', label: 'Id' },
    { name: 'name', label: 'Project' },
    { name: 'options', label: 'Options' },
  ];

  projectData: ProjectModel[] = [];

  sortBy: string = 'name';
  sortOrder: TdDataTableSortingOrder = TdDataTableSortingOrder.Ascending;

  projectChildsActived: boolean = false;
  projectSelected: ProjectModel = undefined;

  constructor(private _titleService: Title,
    private _dataTableService: TdDataTableService, private projectService: ProjectService, private router: Router,
    private _dialogService: TdDialogService, private _viewContainerRef: ViewContainerRef,
    public dialog: MdDialog) { }

  ngOnInit() {
    this.loadProjects();
  }

  loadProjects() {
    this.projectService.getProjects()
      .subscribe(
      (projects) => {
        this.projectData = projects;
      },
    );
  }

  ngAfterViewInit(): void {
    this._titleService.setTitle('Product Stats');
  }

  editProject(project: ProjectModel) {
    this.router.navigate(['/projects/edit', project.id]);
  }

  deleteProject(project: ProjectModel) {
    let iConfirmConfig: IConfirmConfig = {
      message: 'Project ' + project.id + ' will be deleted, do you want to continue?',
      disableClose: false, // defaults to false
      viewContainerRef: this._viewContainerRef,
      title: 'Confirm',
      cancelButton: 'Cancel',
      acceptButton: 'Yes, delete',
    };
    this._dialogService.openConfirm(iConfirmConfig).afterClosed().subscribe((accept: boolean) => {
      if (accept) {
        this.projectService.deleteProject(project).subscribe(
          (project) => this.loadProjects(),
          (error) => console.log(error)
        );
      }
    });
  }


  viewProject(project: ProjectModel) {
    this.router.navigate(['/projects', project.id]);
  }
}
