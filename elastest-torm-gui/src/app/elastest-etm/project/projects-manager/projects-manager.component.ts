import { Router } from '@angular/router';
import { SutModel } from '../../sut/sut-model';
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

  filteredData: any[] = [];
  filteredTotal: number = 0;
  searchTerm: string = '';
  fromRow: number = 1;
  currentPage: number = 1;
  pageSize: number = 5;
  sortBy: string = 'name';
  sortOrder: TdDataTableSortingOrder = TdDataTableSortingOrder.Ascending;

  projectChildsActived: boolean = false;
  projectSelected: string = '';

  // SuT Data
  sutColumns: any[] = [
    { name: 'id', label: 'Id' },
    { name: 'name', label: 'Name' },
    { name: 'specification', label: 'GitHub App Repo' },
    { name: 'description', label: 'Description' },
    { name: 'options', label: 'Options' },
  ];

  sutData: SutModel[] = [];


  constructor(private _titleService: Title,
    private _dataTableService: TdDataTableService, private projectService: ProjectService, private router: Router,
    private _dialogService: TdDialogService, private _viewContainerRef: ViewContainerRef) { }

  ngOnInit() {
    this.loadProjects();
  }

  loadProjects() {
    this.projectService.getProjects()
      .subscribe(
      (projects) => this.prepareDataTable(projects),
    );
  }

  prepareDataTable(projects: ProjectModel[]) {
    console.log("Retrived Projects:" + projects);
    for (let pro of projects) {
      console.log(pro.name);
    }
    this.projectData = projects;
    this.filteredData = this.projectData;
    this.filteredTotal = this.projectData.length;
    this.filter();
  }

  ngAfterViewInit(): void {
    this._titleService.setTitle('Product Stats');
    this.filter();
  }

  sort(sortEvent: ITdDataTableSortChangeEvent): void {
    this.sortBy = sortEvent.name;
    this.sortOrder = sortEvent.order;
    this.filter();
  }

  search(searchTerm: string): void {
    this.searchTerm = searchTerm;
    this.filter();
  }

  page(pagingEvent: IPageChangeEvent): void {
    this.fromRow = pagingEvent.fromRow;
    this.currentPage = pagingEvent.page;
    this.pageSize = pagingEvent.pageSize;
    this.filter();
  }

  filter(): void {
    let newData: any[] = this.projectData;
    newData = this._dataTableService.filterData(newData, this.searchTerm, true);
    this.filteredTotal = newData.length;
    newData = this._dataTableService.sortData(newData, this.sortBy, this.sortOrder);
    newData = this._dataTableService.pageData(newData, this.fromRow, this.currentPage * this.pageSize);
    this.filteredData = newData;
  }

  editProject(project: ProjectModel) {
    console.log(project);
    this.router.navigate(['/projects-management/edit', project.id]);
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


  showProjectChilds(event: ITdDataTableRowClickEvent) {
    this.projectChildsActived = true;
    this.projectSelected = event.row.name;
  }
}
