import { TitlesService } from '../../../shared/services/titles.service';
import { RunTJobModalComponent } from '../run-tjob-modal/run-tjob-modal.component';
import { SutModel } from '../../sut/sut-model';
import { TJobExecModel } from '../../tjob-exec/tjobExec-model';
import { TJobExecService } from '../../tjob-exec/tjobExec.service';
import { TJobModel } from '../tjob-model';
import { TJobService } from '../tjob.service';

import { Component, OnInit, ViewContainerRef, ViewChild } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import {
  IConfirmConfig,
  TdDialogService,
  ITdDataTableSelectEvent,
  ITdDataTableSelectAllEvent,
  ITdDataTableSortChangeEvent,
  ITdDataTableColumn,
  TdDataTableService,
  TdDataTableSortingOrder,
  TdPagingBarComponent,
  IPageChangeEvent,
} from '@covalent/core';
import { MatDialog, MatDialogRef } from '@angular/material';
import { Observable, Subject } from 'rxjs';

@Component({
  selector: 'etm-tjob-manager',
  templateUrl: './tjob-manager.component.html',
  styleUrls: ['./tjob-manager.component.scss'],
})
export class TjobManagerComponent implements OnInit {
  @ViewChild(TdPagingBarComponent) execsPaging: TdPagingBarComponent;

  tJob: TJobModel;
  editMode: boolean = false;

  sutEmpty: SutModel = new SutModel();
  deletingInProgress: boolean = false;

  tJobExecData: TJobExecModel[] = [];
  showSpinner: boolean = true;

  selectedExecsIds: number[] = [];
  execIdsWithErrorOnDelete: number[] = [];

  // TJob Exec Data
  tJobExecColumns: ITdDataTableColumn[] = [
    { name: 'id', label: 'Id', width: 80 },
    { name: 'result', label: 'Result' },
    { name: 'lastExecutionDate', label: 'Last Execution' },
    { name: 'startDate', label: 'Start Date' },
    { name: 'endDate', label: 'End Date' },
    { name: 'duration', label: 'Duration(sec)' },
    { name: 'sutExecution', label: 'Sut Execution', width: 120 },
    { name: 'monitoringStorageType', label: 'Mon. Storage', width: 123 },
    { name: 'options', label: 'Options', sortable: false },
  ];

  // Sort
  sortBy: string = 'id';
  sortOrder: TdDataTableSortingOrder = TdDataTableSortingOrder.Ascending;

  // Pagination
  fromExecsRow: number = 1;
  currentExecsPage: number = 1;
  execsPageSize: number = 10;
  execsFilteredTotal: number;
  execsFilteredData: TJobExecModel[] = [];

  constructor(
    private titlesService: TitlesService,
    private tJobService: TJobService,
    private tJobExecService: TJobExecService,
    private route: ActivatedRoute,
    private router: Router,
    private _dialogService: TdDialogService,
    private _viewContainerRef: ViewContainerRef,
    public dialog: MatDialog,
    private dataTableService: TdDataTableService,
  ) {}

  ngOnInit(): void {
    this.tJob = new TJobModel();
    this.reloadTJob();
  }

  reloadTJob(): void {
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params
        .switchMap((params: Params) => this.tJobService.getTJob(params['tJobId']))
        .subscribe((tJob: TJobModel) => {
          this.tJob = tJob;
          this.titlesService.setHeadTitle('TJob ' + this.tJob.name);
          this.titlesService.setPathName(this.router.routerState.snapshot.url);
          if (this.tJob.sut.id === 0) {
            this.tJob.sut = this.sutEmpty;
          }
          this.tJobExecService.getTJobsExecutionsWithoutChilds(tJob).subscribe(
            (tJobExecs: TJobExecModel[]) => {
              this.tJobExecData = tJobExecs;
              this.tJobExecData.forEach((tJobExec: TJobExecModel) => {
                tJobExec['lastExecutionDate'] = tJobExec.endDate ? tJobExec.endDate : tJobExec.startDate;
              });
              this.showSpinner = false;
              // this.sortTJobsExec(); // Id desc
              this.filterExecs();
            },
            (error: Error) => console.log(error),
          );
        });
    }
  }

  sortTJobsExec(): void {
    this.tJobExecData = this.tJobExecData.reverse();
  }

  deleteTJobExec(tJobExec: TJobExecModel): void {
    let iConfirmConfig: IConfirmConfig = {
      message: 'TJob Execution ' + tJobExec.id + ' will be deleted, do you want to continue?',
      disableClose: false,
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
          this.tJobExecService.deleteTJobExecution(this.tJob, tJobExec).subscribe(
            (exec) => {
              this.deletingInProgress = false;
              this.tJobExecService.popupService.openSnackBar(
                'TJob Execution Nº' + tJobExec.id + ' has been removed successfully!',
              );
              this.reloadTJob();
            },
            (error: Error) => {
              this.deletingInProgress = true;
              this.tJobExecService.popupService.openSnackBar('TJob Execution could not be deleted');
            },
          );
        }
      });
  }

  viewTJobExec(tJobExec: TJobExecModel): void {
    this.router.navigate(['/projects', this.tJob.project.id, 'tjob', this.tJob.id, 'tjob-exec', tJobExec.id]);
  }

  runTJob(): void {
    if (this.tJob.hasParameters()) {
      let dialogRef: MatDialogRef<RunTJobModalComponent> = this.dialog.open(RunTJobModalComponent, {
        data: this.tJob.cloneTJob(),
        height: '85%',
        width: '65%',
      });
    } else {
      this.tJobExecService.runTJob(this.tJob.id, undefined, undefined, this.tJob.multiConfigurations).subscribe(
        (tjobExecution: TJobExecModel) => {
          this.router.navigate(['/projects', this.tJob.project.id, 'tjob', this.tJob.id, 'tjob-exec', tjobExecution.id]);
        },
        (error: Error) => console.error('Error:' + error),
      );
    }
  }

  editSut(): void {
    this.router.navigate(['/projects', this.tJob.project.id, 'sut', 'edit', this.tJob.sut.id]);
  }

  editTJob(): void {
    if (this.tJob.external && this.tJob.getExternalEditPage()) {
      window.open(this.tJob.getExternalEditPage());
    } else {
      this.router.navigate(['/projects', this.tJob.project.id, 'tjob', 'edit', this.tJob.id]);
    }
  }

  deleteTJob(): void {
    let iConfirmConfig: IConfirmConfig = {
      message: 'TJob ' + this.tJob.id + ' will be deleted with all TJob Executions, do you want to continue?',
      disableClose: false,
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
          this.tJobService.deleteTJob(this.tJob).subscribe(
            (tJob) => {
              this.deletingInProgress = true;
              this.router.navigate(['/projects']);
            },
            (error: Error) => {
              this.deletingInProgress = true;
              console.log(error);
            },
          );
        }
      });
  }

  viewInLogAnalyzer(tJobExec: TJobExecModel): void {
    this.router.navigate(['/loganalyzer'], { queryParams: { tjob: this.tJob.id, exec: tJobExec.id } });
  }

  switchExecSelectionByData(tJobExec: TJobExecModel, selected: boolean): void {
    if (selected) {
      this.selectedExecsIds.push(tJobExec.id);
    } else {
      const index: number = this.selectedExecsIds.indexOf(tJobExec.id, 0);
      if (index > -1) {
        this.selectedExecsIds.splice(index, 1);
      }
    }
  }

  switchExecSelection(event: ITdDataTableSelectEvent): void {
    if (event && event.row) {
      let tJobExec: TJobExecModel = event.row;
      this.switchExecSelectionByData(tJobExec, event.selected);
    }
  }

  switchAllExecsSelection(event: ITdDataTableSelectAllEvent): void {
    if (event && event.rows) {
      for (let tJobExec of event.rows) {
        this.switchExecSelectionByData(tJobExec, event.selected);
      }
    }
  }

  compareExecutions(): void {
    this.router.navigate(['/projects', this.tJob.project.id, 'tjob', this.tJob.id, 'comparator'], {
      queryParams: { execs: this.selectedExecsIds.join(',') },
    });
  }

  removeSelectedExecs(): void {
    if (this.selectedExecsIds.length > 0) {
      let iConfirmConfig: IConfirmConfig = {
        message: 'Selected Executions will be deleted, do you want to continue?',
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

            this.execIdsWithErrorOnDelete = [];
            this.removeMultipleExecutionsRecursively([...this.selectedExecsIds]).subscribe(
              (end: boolean) => {
                if (this.execIdsWithErrorOnDelete.length > 0) {
                  let errorMsg: string = 'Error on delete execs with ids: ' + this.execIdsWithErrorOnDelete;
                  this.tJobExecService.popupService.openSnackBar(errorMsg);
                } else {
                  this.tJobExecService.popupService.openSnackBar('Executions ' + this.selectedExecsIds + ' has been removed!');
                }

                this.deletingInProgress = false;
                this.reloadTJob();
                this.execIdsWithErrorOnDelete = [];
                this.selectedExecsIds = [];
              },
              (error: Error) => {
                console.log(error);
                this.deletingInProgress = false;
                this.reloadTJob();
                this.execIdsWithErrorOnDelete = [];
                this.selectedExecsIds = [];
              },
            );
          }
        });
    }
  }

  removeMultipleExecutionsRecursively(selectedExecsIds: number[], _obs: Subject<any> = new Subject<any>()): Observable<boolean> {
    let obs: Observable<any> = _obs.asObservable();

    if (selectedExecsIds.length > 0) {
      let execId: number = selectedExecsIds.shift();

      this.tJobExecService.deleteTJobExecutionById(this.tJob, execId).subscribe(
        (tJobExec: TJobExecModel) => {
          this.removeMultipleExecutionsRecursively(selectedExecsIds, _obs);
        },
        (error: Error) => {
          console.log(error);
          this.execIdsWithErrorOnDelete.push(execId);
          this.removeMultipleExecutionsRecursively(selectedExecsIds, _obs);
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
    this.tJobExecData = this.dataTableService.sortData(this.tJobExecData, this.sortBy, this.sortOrder);
    this.filterExecs();
  }

  execsPage(pagingEvent: IPageChangeEvent): void {
    this.fromExecsRow = pagingEvent.fromRow;
    this.currentExecsPage = pagingEvent.page;
    this.execsPageSize = pagingEvent.pageSize;
    this.filterExecs();
  }

  async filterExecs(): Promise<void> {
    let newData: any[] = this.tJobExecData;
    // let excludedColumns: string[] = await this.tJobExecColumns
    //   .filter((column: ITdDataTableColumn) => {
    //     return (
    //       (column.filter === undefined && column.hidden === true) || (column.filter !== undefined && column.filter === false)
    //     );
    //   })
    //   .map((column: ITdDataTableColumn) => {
    //     return column.name;
    //   });
    // newData = await this.dataTableService.filterData(newData, this.searchTerm, true, excludedColumns);
    this.execsFilteredTotal = newData.length;
    newData = await this.dataTableService.sortData(newData, this.sortBy, this.sortOrder);
    newData = await this.dataTableService.pageData(newData, this.fromExecsRow, this.currentExecsPage * this.execsPageSize);
    this.execsFilteredData = newData;
  }
}
