import { EsmService } from '../../esm-service.service';
import { EsmServiceModel } from '../../esm-service.model';
import { TdDataTableService, TdDataTableSortingOrder, ITdDataTableSortChangeEvent,IPageChangeEvent} from '@covalent/core';
import { EsmServiceInstanceModel } from '../../esm-service-instance.model';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'esm-instance-manager',
  templateUrl: './instances-manager.component.html',
  styleUrls: ['./instances-manager.component.scss']
})
export class InstancesManagerComponent implements OnInit {
   
   serviceColumns: any[] = [
    { name: 'id', label: 'Id' },
    { name: 'serviceName', label: 'Service' },
    { name: 'options', label: 'Options' },
  ];

  instancesData: EsmServiceInstanceModel[] = [];

  filteredData: any[] = [];
  filteredTotal: number = 0;
  searchTerm: string = '';
  fromRow: number = 1;
  currentPage: number = 1;
  pageSize: number = 5;
  sortBy: string = 'serviceName';
  sortOrder: TdDataTableSortingOrder = TdDataTableSortingOrder.Ascending;

  supportServices: EsmServiceModel[] = [];
  selectedService: EsmServiceModel;


  constructor(private _dataTableService: TdDataTableService, private esmService: EsmService) { }

  ngOnInit() {
    this.loadServiceInstances();
    this.esmService.getElastestESMServices()
    .subscribe(
      (esmServices) => { this.supportServices = esmServices
      }
    );
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
    let newData: any[] = this.instancesData;
    newData = this._dataTableService.filterData(newData, this.searchTerm, true);
    this.filteredTotal = newData.length;
    newData = this._dataTableService.sortData(newData, this.sortBy, this.sortOrder);
    newData = this._dataTableService.pageData(newData, this.fromRow, this.currentPage * this.pageSize);
    this.filteredData = newData;
  }

  loadServiceInstances(){}

  provisionServiceInstance(){}

  goToService(serviceInstance: EsmServiceInstanceModel){}

  deprovisionService(serviceInstance: EsmServiceInstanceModel){

  }

}
