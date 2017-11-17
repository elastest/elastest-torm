import { TitlesService } from '../../../shared/services/titles.service';
import { EtmLogsMetricsViewComponent } from '../../etm-logs-metrics-view/etm-logs-metrics-view.component';

import { ESRabLogModel } from '../../../shared/logs-view/models/es-rab-log-model';
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
  @ViewChild('logsAndMetrics') logsAndMetrics: EtmLogsMetricsViewComponent;


  tJobId: number;
  tJobExecId: number;
  tJobExec: TJobExecModel;
  tJob: TJobModel;

  statusIcon: any = {
    name: '',
    color: '',
  };

    // TJob Data
    testCaseColumns: any[] = [
      { name: 'id', label: 'Id' },
      { name: 'name', label: 'Name' },
      { name: 'time', label: 'Time' },
      { name: 'failureMessage', label: 'Failure Msg' },
      { name: 'failureType', label: 'Failure Type' },
      { name: 'failureErrorLine', label: 'Failure Error Line' },
      { name: 'failureDetail', label: 'Failure Detail' },
    ];

  constructor(
    private titlesService: TitlesService,
    private tJobExecService: TJobExecService, private tJobService: TJobService,
    private elastestESService: ElastestESService,
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
    this.titlesService.setHeadTitle('TJob Execution');
    this.tJobExec = new TJobExecModel();
    this.loadTJobExec();
  }

  loadTJobExec() {
    this.tJobExecService.getTJobExecutionByTJobId(this.tJobId, this.tJobExecId)
      .subscribe((tJobExec: TJobExecModel) => {
        this.tJobExec = tJobExec;

        this.statusIcon = this.tJobExec.getResultIcon();
        this.titlesService.setTopTitle(tJobExec.getRouteString());

        this.tJobService.getTJob(this.tJobId.toString())
          .subscribe(
          (tJob: TJobModel) => {
            this.tJob = tJob;
            if (!this.tJobExec.finished()) {
              this.router.navigate(
                ['/projects', tJob.project.id, 'tjob', this.tJobId, 'tjob-exec', this.tJobExecId, 'dashboard'],
                { queryParams: { fromTJobManager: true } });
            } else {
              this.logsAndMetrics.initView(this.tJob, this.tJobExec);
            }
          },
          (error) => console.log(error),
        );
      });
  }

  viewTJob() {
    this.router.navigate(
      ['/projects', this.tJob.project.id, 'tjob', this.tJobId]
    );
  }
}
