import { ElasticSearchService } from '../../../shared/services/elasticsearch.service';
import { LogViewModel } from '../../../shared/logs-view/log-view-model';
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

  sutLogView: LogViewModel = new LogViewModel();
  testLogView: LogViewModel = new LogViewModel();

  constructor(private tJobExecService: TJobExecService, private tJobService: TJobService, private elasticService: ElasticSearchService,
    private route: ActivatedRoute, private router: Router, ) {
    this.initLogsView();
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

        this.testLogView.logUrl = this.tJobExec.logs;
        this.sutLogView.logUrl = this.tJobExec.logs;

        this.tJobService.getTJob(this.tJobId.toString())
          .subscribe(
          (tJob: TJobModel) => {
            if (this.tJobExec.result === 'IN PROGRESS') {
              this.router.navigate(
                ['/projects', tJob.project.id, 'tjob', this.tJobId, 'tjob-exec', this.tJobExecId, 'dashboard'],
                { queryParams: { fromTJobManager: true } });
            }
            else {
              //Load logs
              this.elasticService.searchTestLogs(tJobExec.logs)
                .subscribe(
                (data) => {
                  this.testLogView.traces = data;
                }
                );

              if (tJob.hasSut()) {
                this.elasticService.searchSutLogs(tJobExec.logs)
                  .subscribe(
                  (data) => {
                    this.sutLogView.traces = data;
                  }
                  );
              }
              else {
                this.sutLogView.traces = ['TJob Without Sut. There aren\'t logs'];
              }
            }
          },
          (error) => console.log(error),
        );
      });
  }

  initLogsView() {
    this.testLogView.name = 'Test Logs';
    this.sutLogView.name = 'Sut Logs';
    this.testLogView.hidePrevBtn = true;
    this.sutLogView.hidePrevBtn = true;

    this.testLogView.traces = ['Loading Logs...'];
    this.sutLogView.traces = ['Loading Logs...'];

    this.testLogView.logType = 'testlogs';
    this.sutLogView.logType = 'sutlogs';
  }

}
