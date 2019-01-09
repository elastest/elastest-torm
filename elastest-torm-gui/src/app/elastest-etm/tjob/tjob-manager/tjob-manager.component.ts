import { TitlesService } from '../../../shared/services/titles.service';
import { RunTJobModalComponent } from '../run-tjob-modal/run-tjob-modal.component';
import { SutModel } from '../../sut/sut-model';
import { TJobExecModel } from '../../tjob-exec/tjobExec-model';
import { TJobExecService } from '../../tjob-exec/tjobExec.service';
import { TJobModel } from '../tjob-model';
import { TJobService } from '../tjob.service';

import { Component, OnInit, ViewContainerRef } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { IConfirmConfig, TdDialogService } from '@covalent/core';
import { MatDialog, MatDialogRef } from '@angular/material';

@Component({
  selector: 'app-tjob-manager',
  templateUrl: './tjob-manager.component.html',
  styleUrls: ['./tjob-manager.component.scss'],
})
export class TjobManagerComponent implements OnInit {
  tJob: TJobModel;
  editMode: boolean = false;

  sutEmpty: SutModel = new SutModel();
  deletingInProgress: boolean = false;

  // TJob Exec Data
  tJobExecColumns: any[] = [
    { name: 'id', label: 'Id' },
    { name: 'result', label: 'Result' },
    { name: 'duration', label: 'Duration(sec)' },
    { name: 'startDate', label: 'Start Date' },
    { name: 'endDate', label: 'End Date' },
    { name: 'lastExecutionDate', label: 'Last Execution' },
    { name: 'sutExecution', label: 'Sut Execution' },
    { name: 'options', label: 'Options' },
  ];
  tJobExecData: TJobExecModel[] = [];
  showSpinner: boolean = true;

  constructor(
    private titlesService: TitlesService,
    private tJobService: TJobService,
    private tJobExecService: TJobExecService,
    private route: ActivatedRoute,
    private router: Router,
    private _dialogService: TdDialogService,
    private _viewContainerRef: ViewContainerRef,
    public dialog: MatDialog,
  ) {}

  ngOnInit() {
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
              this.sortTJobsExec(); // Id desc
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
            (error) => {
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
            (error) => {
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
}
