import { Component, OnInit, ViewChild } from '@angular/core';
import { TitlesService } from '../../../shared/services/titles.service';
import { ExternalService } from '../external.service';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { ExternalTJobModel } from '../external-tjob/external-tjob-model';
import { ExternalTJobExecModel } from './external-tjob-execution-model';
import { EtmMonitoringViewComponent } from '../../etm-monitoring-view/etm-monitoring-view.component';
import { ExternalTestExecutionModel } from '../external-test-execution/external-test-execution-model';
import { MonitoringService } from '../../../shared/services/monitoring.service';

@Component({
  selector: 'etm-external-tjob-execution',
  templateUrl: './external-tjob-execution.component.html',
  styleUrls: ['./external-tjob-execution.component.scss'],
})
export class ExternalTjobExecutionComponent implements OnInit {
  @ViewChild('logsAndMetrics', { static: true }) logsAndMetrics: EtmMonitoringViewComponent;

  exTJobId: number;
  exTJobExecId: number;
  exTJob: ExternalTJobModel;
  exTJobExec: ExternalTJobExecModel;
  exTestExecs: ExternalTestExecutionModel[];

  constructor(
    private titlesService: TitlesService,
    private externalService: ExternalService,
    private monitoringService: MonitoringService,
    private route: ActivatedRoute,
    private router: Router,
  ) {
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params.subscribe((params: Params) => {
        this.exTJobId = params.tJobId;
        this.exTJobExecId = params.execId;
      });
    }
  }

  ngOnInit(): void {
    this.exTJobExec = new ExternalTJobExecModel();
    this.loadExternalTJobExec();
  }

  loadExternalTJobExec(): void {
    this.externalService.getExternalTJobExecById(this.exTJobExecId).subscribe((exTJobExec: ExternalTJobExecModel) => {
      this.exTJobExec = exTJobExec;

      // this.statusIcon = this.exTJobExec.getResultIcon();
      this.titlesService.setHeadTitle('Plan Execution ' + this.exTJobExec.id);
      this.titlesService.setPathName(this.router.routerState.snapshot.url);

      this.externalService.getExternalTestExecsByExternalTJobExecId(this.exTJobExec.id).subscribe(
        (exTestExecs: ExternalTestExecutionModel[]) => {
          this.exTestExecs = exTestExecs;
        },
        (error: Error) => console.log(error),
      );

      this.exTJob = this.exTJobExec.exTJob;
      this.logsAndMetrics.initView(this.exTJob, this.exTJobExec);
      let browserLog: any = this.exTJobExec.getBrowserLogObj();
      if (browserLog) {
        this.logsAndMetrics.updateLog(browserLog, false);
      }
    });
  }

  viewInLogAnalyzer(): void {
    this.router.navigate(['/loganalyzer'], {
      queryParams: { exTJob: this.exTJob.id, exTJobExec: this.exTJobExec.id },
    });
  }
}
