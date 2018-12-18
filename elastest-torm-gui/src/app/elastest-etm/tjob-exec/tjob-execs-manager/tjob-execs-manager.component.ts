import { Observable } from 'rxjs/Rx';
import { ActivatedRoute, Router } from '@angular/router';
import { TdDialogService } from '@covalent/core/dialogs/services/dialog.service';
import { IConfirmConfig } from '@covalent/core';
import { TJobExecModel } from '../tjobExec-model';
import { TJobExecService } from '../tjobExec.service';
import { TitlesService } from '../../../shared/services/titles.service';
import { Component, OnInit, ViewContainerRef, Input } from '@angular/core';
import { MdDialog } from '@angular/material';

@Component({
  selector: 'etm-tjob-execs-manager',
  templateUrl: './tjob-execs-manager.component.html',
  styleUrls: ['./tjob-execs-manager.component.scss'],
})
export class TJobExecsManagerComponent implements OnInit {
  @Input()
  isNested: boolean = false;

  tJobExecsFinished: TJobExecModel[] = [];
  tJobExecsRunning: TJobExecModel[] = [];

  defaultRefreshText: string = 'Refresh';
  refreshText: string = this.defaultRefreshText;
  deletingInProgress: boolean = false;

  loading: boolean = true;
  loadAllFinished: boolean = false;

  // TJob Exec Data
  tJobExecColumns: any[] = [
    { name: 'id', label: 'Id' },
    { name: 'tJob.name', label: 'TJob' },
    { name: 'result', label: 'Result' },
    { name: 'duration', label: 'Duration(sec)' },
    { name: 'startDate', label: 'Start Date' },
    { name: 'endDate', label: 'End Date' },
    { name: 'options', label: 'Options' },
  ];

  constructor(
    private titlesService: TitlesService,
    private tJobExecService: TJobExecService,
    private router: Router,
    private _dialogService: TdDialogService,
    private _viewContainerRef: ViewContainerRef,
    public dialog: MdDialog,
  ) {}

  ngOnInit() {
    if (!this.isNested) {
      this.titlesService.setHeadTitle('All TJob Executions');
    }

    this.loadTJobExecs();
  }

  loadTJobExecs(refreshText: string = ''): void {
    this.loading = true;
    this.refreshText = refreshText;

    this.tJobExecService.getAllRunningTJobExecutionsWithoutChilds().subscribe(
      (runningTJobExecs: TJobExecModel[]) => {
        this.tJobExecsFinished = [];
        this.tJobExecsRunning = [];

        this.loadFinishedTJobExecs().subscribe(
          (finishedTJobExecs: TJobExecModel[]) => {
            runningTJobExecs = runningTJobExecs.reverse(); // To sort Descending
            if (this.loadAllFinished) {
              finishedTJobExecs = finishedTJobExecs.reverse();
            }

            this.tJobExecsFinished = finishedTJobExecs;
            this.tJobExecsRunning = runningTJobExecs;

            this.refreshText = this.defaultRefreshText;
            this.loading = false;
          },
          (error: Error) => {
            this.loading = false;
            console.log(error);
          },
        );
      },
      (error: Error) => {
        this.loading = false;
        console.log(error);
      },
    );
  }

  loadFinishedTJobExecs(): Observable<TJobExecModel[]> {
    if (this.loadAllFinished) {
      return this.tJobExecService.getAllFinishedOrNotExecutedTJobExecutions();
    } else {
      // Default
      return this.tJobExecService.getLastNFinishedOrNotExecutedTJobExecutionsWithoutChilds(15);
    }
  }

  deleteTJobExec(tJobExec: TJobExecModel) {
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
          this.tJobExecService.deleteTJobExecution(tJobExec.tJob, tJobExec).subscribe(
            (exec: TJobExecModel) => {
              this.deletingInProgress = false;
              this.tJobExecService.popupService.openSnackBar(
                'TJob Execution Nº' + tJobExec.id + ' has been removed successfully!',
              );
              this.loadTJobExecs();
            },
            (error) => {
              this.deletingInProgress = true;
              this.tJobExecService.popupService.openSnackBar('TJob Execution could not be deleted');
            },
          );
        }
      });
  }

  viewTJobExec(tJobExec: TJobExecModel) {
    this.router.navigate(['/projects', tJobExec.tJob.project.id, 'tjob', tJobExec.tJob.id, 'tjob-exec', tJobExec.id]);
  }

  viewInLogAnalyzer(tJobExec: TJobExecModel): void {
    this.router.navigate(['/loganalyzer'], { queryParams: { tjob: tJobExec.tJob.id, exec: tJobExec.id } });
  }

  showAllFinished(): void {
    this.loadAllFinished = true;
    this.loadTJobExecs();
  }
}
