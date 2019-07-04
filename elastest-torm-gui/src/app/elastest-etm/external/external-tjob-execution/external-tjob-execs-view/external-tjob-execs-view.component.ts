import { Component, OnInit, Input, ViewContainerRef } from '@angular/core';
import { ExternalService } from '../../external.service';
import { ExternalTJobExecModel } from '../external-tjob-execution-model';
import { MatDialogRef, MatDialog } from '@angular/material';
import { SelectBuildModalComponent } from '../../../../etm-testlink/test-plan/select-build-modal/select-build-modal.component';
import { IConfirmConfig, TdDialogService, ITdDataTableSelectAllEvent, ITdDataTableSelectEvent } from '@covalent/core';
import { Subject, Observable } from 'rxjs';

@Component({
  selector: 'etm-external-tjob-execs-view',
  templateUrl: './external-tjob-execs-view.component.html',
  styleUrls: ['./external-tjob-execs-view.component.scss'],
})
export class ExternalTjobExecsViewComponent implements OnInit {
  @Input() exTJobId: number | string;
  exTJobExecs: ExternalTJobExecModel[] = [];
  deletingInProgress: boolean = false;

  selectedExecsIds: number[] = [];
  execIdsWithErrorOnDelete: number[] = [];

  execsColumns: any[] = [
    { name: 'id', label: 'Id', width: 80 },
    { name: 'result', label: 'Result', width: 74 },
    { name: 'startDate', label: 'Start Date' },
    { name: 'endDate', label: 'End Date' },
    { name: 'exTJob.id', label: 'External TJob Id' },
    { name: 'options', label: 'Options' },
  ];

  constructor(
    private externalService: ExternalService,
    private _dialogService: TdDialogService,
    private _viewContainerRef: ViewContainerRef,
    public dialog: MatDialog,
  ) {}

  ngOnInit(): void {
    this.loadExternalTJobExecs();
  }

  loadExternalTJobExecs(): void {
    if (this.exTJobId !== undefined && this.exTJobId !== null) {
      this.externalService.getExternalTJobExecsByExternalTJobId(this.exTJobId).subscribe(
        (exTJobExecs: ExternalTJobExecModel[]) => {
          this.exTJobExecs = exTJobExecs.reverse();
        },
        (error: Error) => console.log(error),
      );
    } else {
      this.externalService.getAllExternalTJobExecs().subscribe(
        (exTJobExecs: ExternalTJobExecModel[]) => {
          this.exTJobExecs = exTJobExecs.reverse();
        },
        (error: Error) => console.log(error),
      );
    }
  }

  resumeExTJobExec(exTJobExec: ExternalTJobExecModel): void {
    if (exTJobExec && exTJobExec.executionConfigObj) {
      let savedConfig: any = exTJobExec.executionConfigObj;
      savedConfig.exTJobExecId = exTJobExec.id;
      let dialogRef: MatDialogRef<SelectBuildModalComponent> = this.dialog.open(SelectBuildModalComponent, {
        data: {
          savedConfig: savedConfig,
        },
        minWidth: '35%',
      });
    }
  }

  deleteExternalTJobExec(exTJobExec: ExternalTJobExecModel): void {
    if (exTJobExec) {
      let iConfirmConfig: IConfirmConfig = {
        message: 'External TJob Execution ' + exTJobExec.id + ' will be deleted, do you want to continue?',
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
            this.externalService.deleteExternalTJobExecById(exTJobExec.id).subscribe(
              (response: any) => {
                this.deletingInProgress = false;
                this.loadExternalTJobExecs();
              },
              (error: Error) => {
                this.deletingInProgress = false;
                console.log(error);
              },
            );
          }
        });
    }
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
            this.removeMultipleExecsRecursively([...this.selectedExecsIds]).subscribe(
              (end: boolean) => {
                if (this.execIdsWithErrorOnDelete.length > 0) {
                  let errorMsg: string = 'Error on delete executions with ids: ' + this.execIdsWithErrorOnDelete;
                  this.externalService.popupService.openSnackBar(errorMsg);
                } else {
                  this.externalService.popupService.openSnackBar('Executions ' + this.selectedExecsIds + ' has been removed!');
                }

                this.deletingInProgress = false;
                this.loadExternalTJobExecs();
                this.execIdsWithErrorOnDelete = [];
                this.selectedExecsIds = [];
              },
              (error: Error) => {
                console.log(error);
                this.deletingInProgress = false;
                this.loadExternalTJobExecs();
                this.execIdsWithErrorOnDelete = [];
                this.selectedExecsIds = [];
              },
            );
          }
        });
    }
  }

  removeMultipleExecsRecursively(selectedExecsIds: number[], _obs: Subject<any> = new Subject<any>()): Observable<boolean> {
    let obs: Observable<any> = _obs.asObservable();

    if (selectedExecsIds.length > 0) {
      let execId: number = selectedExecsIds.shift();
      this.externalService.deleteExternalTJobExecById(execId).subscribe(
        (execs: ExternalTJobExecModel) => {
          this.removeMultipleExecsRecursively(selectedExecsIds, _obs);
        },
        (error: Error) => {
          console.log(error);
          this.execIdsWithErrorOnDelete.push(execId);
          this.removeMultipleExecsRecursively(selectedExecsIds, _obs);
        },
      );
    } else {
      _obs.next(true);
    }

    return obs;
  }

  switchExecsSelectionByData(exec: ExternalTJobExecModel, selected: boolean): void {
    if (selected) {
      this.selectedExecsIds.push(exec.id);
    } else {
      const index: number = this.selectedExecsIds.indexOf(exec.id, 0);
      if (index > -1) {
        this.selectedExecsIds.splice(index, 1);
      }
    }
  }

  switchExecSelection(event: ITdDataTableSelectEvent): void {
    if (event && event.row) {
      let exec: ExternalTJobExecModel = event.row;
      this.switchExecsSelectionByData(exec, event.selected);
    }
  }

  switchAllExecsSelection(event: ITdDataTableSelectAllEvent): void {
    if (event && event.rows) {
      for (let exec of event.rows) {
        this.switchExecsSelectionByData(exec, event.selected);
      }
    }
  }
}
