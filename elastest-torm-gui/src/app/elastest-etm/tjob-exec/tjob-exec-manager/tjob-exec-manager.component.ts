import {
  EtmComplexMetricsGroupComponent,
} from '../../../shared/metrics-view/complex-metrics-view/etm-complex-metrics-group/etm-complex-metrics-group.component';
import {
  ESRabComplexMetricsModel,
} from '../../../shared/metrics-view/complex-metrics-view/models/es-rab-complex-metrics-model';
import { ESLogModel } from '../../../shared/logs-view/models/elasticsearch-log-model';
import { ETRESMetricsModel } from '../../../shared/metrics-view/models/et-res-metrics-model';
import { ElastestESService } from '../../../shared/services/elastest-es.service';
import { TJobModel } from '../../tjob/tjob-model';
import { TJobService } from '../../tjob/tjob.service';
import { TJobExecModel } from '../tjobExec-model';
import { TJobExecService } from '../tjobExec.service';

import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';

@Component({
  selector: 'app-tjob-exec-manager',
  templateUrl: './tjob-exec-manager.component.html',
  styleUrls: ['./tjob-exec-manager.component.scss']
})
export class TjobExecManagerComponent implements OnInit {
  @ViewChild('metricsGroup') metricsGroup: EtmComplexMetricsGroupComponent;

  tJobId: number;
  tJobExecId: number;
  tJobExec: TJobExecModel;
  tJob: TJobModel;

  // Logs
  sutLogView: ESLogModel = new ESLogModel(this.elastestESService);
  testLogView: ESLogModel = new ESLogModel(this.elastestESService);

  constructor(private tJobExecService: TJobExecService, private tJobService: TJobService,
    private elastestESService: ElastestESService,
    private route: ActivatedRoute, private router: Router,
  ) {
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

        // Init logs index
        this.testLogView.logIndex = this.tJobExec.logIndex;
        this.sutLogView.logIndex = this.tJobExec.logIndex;

        this.tJobService.getTJob(this.tJobId.toString())
          .subscribe(
          (tJob: TJobModel) => {
            this.tJob = tJob;
            if (this.tJobExec.result === 'IN PROGRESS') {
              this.router.navigate(
                ['/projects', tJob.project.id, 'tjob', this.tJobId, 'tjob-exec', this.tJobExecId, 'dashboard'],
                { queryParams: { fromTJobManager: true } });
            }
            else {
              //Load logs
              this.testLogView.getAllLogs();

              if (tJob.hasSut()) {
                this.sutLogView.getAllLogs();
              }
              else {
                this.sutLogView.traces = [
                  {
                    'message': 'TJob Without Sut. There aren\'t logs'
                  }
                ];
              }

              //Load metrics
              this.metricsGroup.initMetricsView(tJob, tJobExec);

            }
          },
          (error) => console.log(error),
        );
      });
  }

  initLogsView() {
    this.elastestESService.initTestLog(this.testLogView);
    this.elastestESService.initSutLog(this.sutLogView);

    this.testLogView.hidePrevBtn = true;
    this.sutLogView.hidePrevBtn = true;

    this.testLogView.traces = ['Loading Logs...'];
    this.sutLogView.traces = ['Loading Logs...'];
  }
}
