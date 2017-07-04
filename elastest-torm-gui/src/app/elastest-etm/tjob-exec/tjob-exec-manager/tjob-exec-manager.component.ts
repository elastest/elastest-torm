import { ElasticSearchService } from '../../../elastest-log-manager/services/elasticsearch.service';
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

  testTraces: string[] = ['Loading Logs...'];
  sutTraces: string[] = ['Loading Logs...'];

  constructor(private tJobExecService: TJobExecService, private tJobService: TJobService, private elasticService: ElasticSearchService,
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
        this.tJobService.getTJob(this.tJobId.toString())
          .subscribe(
          (tJob: TJobModel) => {
            if (this.tJobExec.result === 'IN PROGRESS') {
              this.router.navigate(
                ['/projects/tjob', this.tJobId, 'tjob-exec', this.tJobExecId, 'dashboard']
              )

            }
            else {
              //Load logs
              this.elasticService.searchTestLogs('http://' + tJobExec.logs)
                .subscribe(
                (data) => {
                  this.testTraces = data;
                }
                );

              if (tJob.hasSut()) {
                this.elasticService.searchSutLogs('http://' + tJobExec.logs)
                  .subscribe(
                  (data) => {
                    this.sutTraces = data;
                  }
                  );
              }
              else {
                this.sutTraces = ['TJob Without Sut. There aren\'t logs'];
              }
            }
          },
          (error) => console.log(error),
        );
      });
  }

}
