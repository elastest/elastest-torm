import { ConfigurationService } from '../../config/configuration-service.service';
import { FileModel } from './file-model';
import { TJobExecService } from '../tjob-exec/tjobExec.service';
import { Component, Input, OnInit } from '@angular/core';
import { Observable, Subscription } from 'rxjs/Rx';
import { TdDataTableService, TdDataTableSortingOrder, ITdDataTableSortChangeEvent, IPageChangeEvent } from '@covalent/core';

@Component({
  selector: 'etm-files-manager',
  templateUrl: './files-manager.component.html',
  styleUrls: ['./files-manager.component.scss']
})
export class FilesManagerComponent implements OnInit {

  @Input()
  tJobId: number;
  @Input()
  tJobExecId: number;
  @Input()
  tJobExecFinish: boolean;


  filesColumns: any[] = [
    { name: 'name', label: 'name' },
    { name: 'serviceName', label: 'Service' },
    { name: 'options', label: 'Options' },
  ];

  executionFiles: FileModel[] = [];

  filteredData: any[] = [];
  filteredTotal: number = 0;
  searchTerm: string = '';
  fromRow: number = 1;
  currentPage: number = 1;
  pageSize: number = 5;
  sortBy: string = 'serviceName';
  sortOrder: TdDataTableSortingOrder = TdDataTableSortingOrder.Ascending;

  timer: Observable<number>;
  subscription: Subscription;

  filesUrlPrefix: string;

  constructor(private _dataTableService: TdDataTableService, private tJobExecService: TJobExecService,
    private configurationService: ConfigurationService) {
    this.filesUrlPrefix = configurationService.configModel.host;
  }

  ngOnInit() {
    this.loadExecutionFiles();
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
    let newData: any[] = this.executionFiles;
    newData = this._dataTableService.filterData(newData, this.searchTerm, true);
    this.filteredTotal = newData.length;
    newData = this._dataTableService.sortData(newData, this.sortBy, this.sortOrder);
    newData = this._dataTableService.pageData(newData, this.fromRow, this.currentPage * this.pageSize);
    this.filteredData = newData;
  }

  loadExecutionFiles() {
    this.timer = Observable.interval(3500);
    if (this.subscription === undefined) {
      console.log('Start polling for check tssInstance status');
      this.subscription = this.timer
        .subscribe(() => {
          this.tJobExecService.getTJobExecutionByTJobId(this.tJobId, this.tJobExecId)
            .subscribe(
            (tJobExecution) => {
              if (tJobExecution.finished()) {
                console.log('Stop polling to retrive files');
                if (this.subscription !== undefined) {
                  this.subscription.unsubscribe();
                  this.subscription = undefined;
                }
              }
              this.tJobExecService.getTJobExecutionFiles(this.tJobId, this.tJobExecId)
                .subscribe((tJobsExecFiles) => {
                  this.prepareDataTable(tJobsExecFiles)
                });
            });
        });
    }
  }

  prepareDataTable(servicesInstances: FileModel[]) {
    this.executionFiles = servicesInstances;
    this.filteredData = this.executionFiles;
    this.filteredTotal = this.executionFiles.length;
    this.filter();
  }

  ngOnDestroy() {
    if (this.subscription !== undefined) {
      this.subscription.unsubscribe();
      this.subscription = undefined;
    }
  }

}
