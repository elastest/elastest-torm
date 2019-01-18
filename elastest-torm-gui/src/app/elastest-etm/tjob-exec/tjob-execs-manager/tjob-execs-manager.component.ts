import { Observable, Subscription } from 'rxjs/Rx';
import { Router } from '@angular/router';
import { TdDialogService, IConfirmConfig } from '@covalent/core';

import { TJobExecModel } from '../tjobExec-model';
import { TJobExecService } from '../tjobExec.service';
import { TitlesService } from '../../../shared/services/titles.service';
import { Component, OnInit, ViewContainerRef, Input, OnDestroy, HostListener } from '@angular/core';
import { MatDialog } from '@angular/material';
import { interval } from 'rxjs';

@Component({
  selector: 'etm-tjob-execs-manager',
  templateUrl: './tjob-execs-manager.component.html',
  styleUrls: ['./tjob-execs-manager.component.scss'],
})
export class TJobExecsManagerComponent implements OnInit, OnDestroy {
  @Input()
  isNested: boolean = false;

  tJobExecsFinished: TJobExecModel[] = [];
  tJobExecsRunning: TJobExecModel[] = [];

  deletingInProgress: boolean = false;

  loadingRunning: boolean = true;
  loadingFinished: boolean = true;

  loadAllFinished: boolean = false;

  reloadSubscription: Subscription;
  reloadRunning: boolean = true;
  reloadFinished: boolean = true;

  // TJob Exec Data
  tJobExecColumns: any[] = [
    { name: 'id', label: 'Id', width: 80 },
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
    public dialog: MatDialog,
  ) {}

  ngOnInit(): void {
    if (!this.isNested) {
      this.titlesService.setHeadTitle('All TJob Executions');
    }

    this.loadTJobExecs(true);

    let timer: Observable<number> = interval(5000);
    if (this.reloadSubscription === null || this.reloadSubscription === undefined) {
      this.reloadSubscription = timer.subscribe(() => {
        this.loadTJobExecs();
      });
    }
  }

  ngOnDestroy(): void {
    this.unsubscribeReload();
  }

  @HostListener('window:beforeunload')
  beforeunloadHandler() {
    // On window closed leave session
    this.unsubscribeReload();
  }

  unsubscribeReload(): void {
    if (this.reloadSubscription) {
      this.reloadSubscription.unsubscribe();
      this.reloadSubscription = undefined;
    }
  }

  loadTJobExecs(firstLoad: boolean = false): void {
    if (this.reloadRunning || firstLoad) {
      this.loadingRunning = true;
      this.tJobExecService.getAllRunningTJobExecutionsWithoutChilds().subscribe(
        (runningTJobExecs: TJobExecModel[]) => {
          this.tJobExecsRunning = [];
          runningTJobExecs = runningTJobExecs.reverse(); // To sort Descending
          this.tJobExecsRunning = runningTJobExecs;
          this.loadingRunning = false;
        },
        (error: Error) => {
          this.loadingRunning = false;
          console.log(error);
        },
      );
    }

    if (this.reloadFinished || firstLoad) {
      this.loadingFinished = true;
      this.loadFinishedTJobExecs().subscribe(
        (finishedTJobExecs: TJobExecModel[]) => {
          this.tJobExecsFinished = [];
          if (this.loadAllFinished) {
            finishedTJobExecs = finishedTJobExecs.reverse();
          }
          this.tJobExecsFinished = finishedTJobExecs;
          this.loadingFinished = false;
        },
        (error: Error) => {
          this.loadingFinished = false;
          console.log(error);
        },
      );
    }
  }

  loadFinishedTJobExecs(): Observable<TJobExecModel[]> {
    if (this.loadAllFinished) {
      return this.tJobExecService.getAllFinishedOrNotExecutedTJobExecutions();
    } else {
      // Default
      return this.tJobExecService.getLastNFinishedOrNotExecutedTJobExecutionsWithoutChilds(15);
    }
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
          this.tJobExecService.deleteTJobExecution(tJobExec.tJob, tJobExec).subscribe(
            (exec: TJobExecModel) => {
              this.deletingInProgress = false;
              this.tJobExecService.popupService.openSnackBar(
                'TJob Execution Nº' + tJobExec.id + ' has been removed successfully!',
              );
              this.loadTJobExecs();
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
