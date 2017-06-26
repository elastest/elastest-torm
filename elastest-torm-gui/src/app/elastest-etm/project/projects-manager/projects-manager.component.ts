import { Router } from '@angular/router';
import { ProjectModel } from '../project-model';
import { ProjectService } from '../project.service';
import { Title } from '@angular/platform-browser';
import {
    IPageChangeEvent,
    ITdDataTableRowClickEvent,
    ITdDataTableSortChangeEvent,
    TdDataTableService,
    TdDataTableSortingOrder
} from '@covalent/core';
import { AfterViewInit, Component, OnInit } from '@angular/core';

@Component({
  selector: 'etm-projects-manager',
  templateUrl: './projects-manager.component.html',
  styleUrls: ['./projects-manager.component.scss']
})
export class ProjectsManagerComponent implements OnInit, AfterViewInit {

  columns: any[] = [
    { name: 'id', label: 'Id' }, 
    { name: 'name',  label: 'Project' },
    /*{ name: 'edit', label: 'Edit' },    
    { name: 'run', label: 'Run'},    
    { name: 'delete', label: 'Delete'},*/
  ];

  data: ProjectModel[] = [];

  filteredData: any[] = [];
  filteredTotal: number = 0;
  searchTerm: string = '';
  fromRow: number = 1;
  currentPage: number = 1;
  pageSize: number = 5;
  sortBy: string = 'name';
  sortOrder: TdDataTableSortingOrder = TdDataTableSortingOrder.Descending;


  constructor(private _titleService: Title,
              private _dataTableService: TdDataTableService, private projectService: ProjectService, private router: Router) { }

  ngOnInit() {
    this.projectService.getProjects()
    .subscribe(
      projects => this.prepareDataTable(projects),
    );
  }

  prepareDataTable(projects: ProjectModel[]){
    console.log("Retrived Projects:"+projects);
    for(let pro of projects){
      console.log(pro.name);
    }
    this.data = projects;
    this.filteredData = this.data;
    this.filteredTotal = this.data.length;
    this.filter();
  }

  ngAfterViewInit(): void {
    this._titleService.setTitle( 'Product Stats' );
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
    let newData: any[] = this.data;
    newData = this._dataTableService.filterData(newData, this.searchTerm, true);
    this.filteredTotal = newData.length;
    newData = this._dataTableService.sortData(newData, this.sortBy, this.sortOrder);
    newData = this._dataTableService.pageData(newData, this.fromRow, this.currentPage * this.pageSize);
    this.filteredData = newData;
  }

  rowClick(clickEvent: ITdDataTableRowClickEvent){
    //alert("CLICK");
    this.router.navigate(['/projects-management/edit', clickEvent.row.id]);

  }

}
