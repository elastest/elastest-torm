import { ActivatedRoute, Router } from '@angular/router';
import { EsmService } from '../../esm-service.service';
import { EsmServiceModel } from '../../esm-service.model';
import { TdDataTableService, TdDataTableSortingOrder, ITdDataTableSortChangeEvent, IPageChangeEvent } from '@covalent/core';
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
  selectedService: string;


  constructor(private _dataTableService: TdDataTableService, private esmService: EsmService,
    private route: ActivatedRoute, private router: Router) { }

  ngOnInit() {
    this.loadServiceInstances();
    this.esmService.getSupportServices()
      .subscribe(
      (esmServices) => {
      this.supportServices = esmServices; console.log(JSON.stringify(esmServices));
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

  loadServiceInstances() {
    this.esmService.getSupportServicesInstances()
      .subscribe(
      (esmServicesInstances) => {
        this.prepareDataTable(esmServicesInstances);
      }
      );
  }

  provisionServiceInstance() {
    this.esmService.provisionServiceInstance(this.selectedService)
      .subscribe(
      (esmServices) => {
        console.log(esmServices); this.loadServiceInstances();
      }
      );
  }

  deprovisionService(serviceInstance: EsmServiceInstanceModel) {
    this.esmService.deprovisionServiceInstance(serviceInstance.id)
      .subscribe(
      (esmServices) => {
        console.log(esmServices); this.loadServiceInstances();
      }
      );
  }

  goToServiceGui(serviceInstance: EsmServiceInstanceModel) {
    console.log("Navigate to service gui:" + serviceInstance.uiUrl);
    this.router.navigate(['/support-services/service-gui'], { queryParams: { page: serviceInstance.uiUrl } });

  }

  goToServiceDetail(serviceInstance: EsmServiceInstanceModel) {
    this.router.navigate(['/support-services/service-detail', serviceInstance.id]);
  }

  prepareDataTable(servicesInstances: EsmServiceInstanceModel[]) {
    this.instancesData = servicesInstances;
    this.filteredData = this.instancesData;
    this.filteredTotal = this.instancesData.length;
    this.filter();
  }
}
