import { TJobModel } from '../../tjob/tjob-model';
import { TJobService } from '../../tjob/tjob.service';
import { TJobExecModel } from '../tjobExec-model';
import { TJobExecService } from '../tjobExec.service';

import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';

@Component({
  selector: 'app-tjob-exec-manager',
  templateUrl: './tjob-exec-manager.component.html',
  styleUrls: ['./tjob-exec-manager.component.scss']
})
export class TjobExecManagerComponent implements OnInit {
  tJobId: number;
  tJobExecId: number;
  tJobExec: TJobExecModel;

  constructor(private tJobExecService: TJobExecService, private tJobService: TJobService,
    private route: ActivatedRoute, private router: Router,
  ) {
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params.subscribe(
        (params: Params) => {
          this.tJobId = params.tJobId;
          this.tJobExecId = params.tJobExecId;
        }
      )
    }
  }

  ngOnInit() {
    this.tJobExec = new TJobExecModel();
    this.loadTJobExec();
  }

  loadTJobExec() {
    this.tJobExecService.getTJobExecutionByTJobId(this.tJobId, this.tJobExecId)
      .subscribe((tJobExec: TJobExecModel) => {
        this.tJobExec = tJobExec;
        if (this.tJobExec.result === 'IN PROGRESS') {
          this.tJobService.getTJob(this.tJobId.toString())
            .subscribe(
            (tJob: TJobModel) => this.router.navigate(['/projects-management/tjob-management', this.tJobId, 'tjobExec-management', this.tJobExecId, 'dashboard', (tJob.sut !== undefined && tJob.sut.id !== 0) ])
            )
        }
        // this.tJobExecData = tJob.tjobExecs;
      });
  }

}
