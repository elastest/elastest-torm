import { Component, OnInit, Input, ViewContainerRef } from '@angular/core';
import { ExternalService } from '../../external.service';
import { ExternalTJobExecModel } from '../external-tjob-execution-model';
import { MatDialogRef, MatDialog } from '@angular/material';
import { SelectBuildModalComponent } from '../../../../etm-testlink/test-plan/select-build-modal/select-build-modal.component';
import { IConfirmConfig, TdDialogService } from '@covalent/core';

@Component({
  selector: 'etm-external-tjob-execs-view',
  templateUrl: './external-tjob-execs-view.component.html',
  styleUrls: ['./external-tjob-execs-view.component.scss'],
})
export class ExternalTjobExecsViewComponent implements OnInit {
  @Input() exTJobId: number | string;
  exTJobExecs: ExternalTJobExecModel[] = [];
  deletingInProgress: boolean = false;

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
}
