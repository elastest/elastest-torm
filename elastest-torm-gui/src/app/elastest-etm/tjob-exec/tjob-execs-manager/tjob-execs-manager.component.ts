import { Observable, Subscription } from 'rxjs/Rx';
import { Router } from '@angular/router';
import { TdDialogService, IConfirmConfig } from '@covalent/core';

import { TJobExecModel } from '../tjobExec-model';
import { TJobExecService } from '../tjobExec.service';
import { TitlesService } from '../../../shared/services/titles.service';
import { Component, OnInit, ViewContainerRef, Input, OnDestroy, HostListener } from '@angular/core';
import { MatDialog } from '@angular/material';
import { interval } from 'rxjs';
import { first } from 'rxjs/operators';

@Component({
  selector: 'etm-tjob-execs-manager',
  templateUrl: './tjob-execs-manager.component.html',
  styleUrls: ['./tjob-execs-manager.component.scss'],
})
export class TJobExecsManagerComponent implements OnInit, OnDestroy {
  @Input()
  isNested: boolean = false;

  // First load
  tJobExecsFinished: TJobExecModel[];
  // Finished recently
  lastTJobExecsFinished: TJobExecModel[];
  // All merged
  allTJobExecsFinished: TJobExecModel[];
  loadingAllPrevFinished: boolean = false;
  allFinishedPrevLoaded: boolean = false;

  firstInitializationOfFinished: boolean = true;

  tJobExecsRunning: TJobExecModel[];
  firstInitializationOfRunning: boolean = true;

  deletingInProgress: boolean = false;

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

  loadTJobExecs(firstLoadOrForce: boolean = false): void {
    /* *** Running *** */
    this.loadRunningTJobExecs(firstLoadOrForce);

    /* *** Finished *** */
    this.loadFinishedTJobExecs(firstLoadOrForce);
  }

  loadRunningTJobExecs(firstLoadOrForce: boolean = false): void {
    if (this.reloadRunning || firstLoadOrForce) {
      this.tJobExecService.getAllRunningTJobsExecutions(true).subscribe(
        (runningTJobExecs: TJobExecModel[]) => {
          runningTJobExecs = runningTJobExecs.reverse(); // To sort Descending
          this.tJobExecsRunning = runningTJobExecs;
          this.firstInitializationOfRunning = false;
        },
        (error: Error) => {
          console.log(error);
        },
      );
    }
  }

  loadFinishedTJobExecs(firstLoadOrForce: boolean = false): void {
    if (this.reloadFinished || firstLoadOrForce) {
      if (!this.tJobExecsFinished || this.tJobExecsFinished.length === 0) {
        this.tJobExecService.getLastNFinishedOrNotExecutedTJobsExecutions(15, true).subscribe(
          (finishedTJobExecs: TJobExecModel[]) => {
            this.tJobExecsFinished = finishedTJobExecs;
            this.loadLastFinishedTJobExecs();
            this.firstInitializationOfFinished = false;
          },
          (error: Error) => {
            console.log(error);
          },
        );
      } else {
        this.loadLastFinishedTJobExecs();
        this.firstInitializationOfFinished = false;
      }
    }
  }

  loadLastFinishedTJobExecs(): void {
    if (this.tJobExecsFinished.length > 0) {
      this.tJobExecService
        .getAllFinishedOrNotExecutedTJobsExecutionsSinceId(this.tJobExecsFinished[0].id, 'greater', true)
        .subscribe(
          (lastFinishedTJobExecs: TJobExecModel[]) => {
            this.lastTJobExecsFinished = lastFinishedTJobExecs;
            this.generateAllFinishedTJobExecs();
          },
          (error: Error) => console.log(error),
        );
    }
  }

  generateAllFinishedTJobExecs(): void {
    if (this.lastTJobExecsFinished.length > 0) {
      this.allTJobExecsFinished = this.lastTJobExecsFinished.concat(this.tJobExecsFinished);
    } else {
      this.allTJobExecsFinished = this.tJobExecsFinished;
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

              // Reload all from last load execution
              if (this.allTJobExecsFinished && this.allTJobExecsFinished.length > 0) {
                let firstExecId: number = this.allTJobExecsFinished[this.allTJobExecsFinished.length - 1].id;
                if (firstExecId > 1) {
                  firstExecId -= 1;
                }
                this.tJobExecService.getAllFinishedOrNotExecutedTJobsExecutionsSinceId(firstExecId, 'greater', true).subscribe(
                  (finishedTJobExecs: TJobExecModel[]) => {
                    this.lastTJobExecsFinished = [];
                    finishedTJobExecs = finishedTJobExecs.reverse();
                    this.tJobExecsFinished = finishedTJobExecs;
                    this.generateAllFinishedTJobExecs();
                  },
                  (error: Error) => console.log(error),
                );
              }
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

  loadMorePreviousFinished(): void {
    if (this.tJobExecsFinished && this.tJobExecsFinished.length > 0) {
      this.loadingAllPrevFinished = true;
      let firstExecId: number = this.tJobExecsFinished[this.tJobExecsFinished.length - 1].id;
      this.tJobExecService.getFinishedOrNotExecutedTJobsExecutionsByRangeAndSinceId(firstExecId, 0, 12, 'less', true).subscribe(
        (prev: TJobExecModel[]) => {
          if (prev.length > 0) {
            this.tJobExecsFinished = this.tJobExecsFinished.concat(prev);
            this.generateAllFinishedTJobExecs();
          } else {
            this.allFinishedPrevLoaded = true;
          }
          this.loadingAllPrevFinished = false;
        },
        (error: Error) => {
          console.log(error);
          this.loadingAllPrevFinished = false;
        },
      );
    }
  }
}
