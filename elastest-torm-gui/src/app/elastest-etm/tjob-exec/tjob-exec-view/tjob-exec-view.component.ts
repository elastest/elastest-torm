import { Component, OnInit } from '@angular/core';
import { Params, ActivatedRoute } from '@angular/router';
import { TJobExecModel } from '../tjobExec-model';
import { TJobExecService } from '../tjobExec.service';
import { Subscription, Observable, interval } from 'rxjs';

@Component({
  selector: 'etm-tjob-exec-view',
  templateUrl: './tjob-exec-view.component.html',
  styleUrls: ['./tjob-exec-view.component.scss'],
})
export class TjobExecViewComponent implements OnInit {
  tJobId: number;
  tJobExecId: number;
  tJobExec: TJobExecModel;

  finished: boolean = true;

  checkResultSubscription: Subscription;

  constructor(private tJobExecService: TJobExecService, private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.tJobId = undefined;
    this.tJobExecId = undefined;
    this.tJobExec = undefined;
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params.subscribe((params: Params) => {
        this.tJobId = params.tJobId;
        this.tJobExecId = params.tJobExecId;
        this.tJobExec = new TJobExecModel();
        this.loadTJobExec();
      });
    }
  }

  ngOnDestroy(): void {
    this.unsubscribeCheckResult();
  }

  unsubscribeCheckResult(): void {
    if (this.checkResultSubscription !== undefined) {
      this.checkResultSubscription.unsubscribe();
      this.checkResultSubscription = undefined;
    }
  }

  checkResultStatus(): void {
    let timer: Observable<number> = interval(1800);
    if (this.checkResultSubscription === null || this.checkResultSubscription === undefined) {
      this.checkResultSubscription = timer.subscribe(() => {
        this.tJobExecService.getResultStatus(this.tJobId, this.tJobExec).subscribe(
          (data) => {
            if (data.result !== this.tJobExec.result) {
              this.tJobExec.result = data.result;
            }
            this.tJobExec.resultMsg = data.msg;
            if (this.tJobExec.finished()) {
              this.unsubscribeCheckResult();
              this.tJobExecService
                .getTJobExecutionByTJobId(this.tJobId, this.tJobExecId)
                .subscribe((finishedTJobExec: TJobExecModel) => {
                  this.tJobExec = finishedTJobExec;
                  this.finished = this.tJobExec.finished();
                });
            }
          },
          (error: Error) => console.log(error),
        );
      });
    }
  }

  loadTJobExec(): void {
    this.tJobExecService.getTJobExecutionByTJobId(this.tJobId, this.tJobExecId).subscribe(
      (tJobExec: TJobExecModel) => {
        this.tJobExec = tJobExec;
        this.finished = this.tJobExec.finished();
        this.checkResultStatus();
      },
      (error: Error) => console.log(error),
    );
  }
}
