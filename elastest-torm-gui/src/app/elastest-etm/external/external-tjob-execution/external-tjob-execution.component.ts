import { Component, OnInit, ViewChild, ViewContainerRef } from '@angular/core';
import { TitlesService } from '../../../shared/services/titles.service';
import { ExternalService } from '../external.service';
import { ElastestESService } from '../../../shared/services/elastest-es.service';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { TdDialogService } from '@covalent/core';
import { MdDialog } from '@angular/material';
import { ExternalTJobModel } from '../external-tjob/external-tjob-model';
import { ExternalTJobExecModel } from './external-tjob-execution-model';
import { EtmMonitoringViewComponent } from '../../etm-monitoring-view/etm-monitoring-view.component';

@Component({
  selector: 'etm-external-tjob-execution',
  templateUrl: './external-tjob-execution.component.html',
  styleUrls: ['./external-tjob-execution.component.scss'],
})
export class ExternalTjobExecutionComponent implements OnInit {
  @ViewChild('logsAndMetrics') logsAndMetrics: EtmMonitoringViewComponent;

  exTJobId: number;
  exTJobExecId: number;
  exTJob: ExternalTJobModel;
  exTJobExec: ExternalTJobExecModel;

  statusIcon: any = {
    name: '',
    color: '',
  };

  constructor(
    private titlesService: TitlesService,
    private externalService: ExternalService,
    private elastestESService: ElastestESService,
    private route: ActivatedRoute,
    private router: Router,
    private _dialogService: TdDialogService,
    private _viewContainerRef: ViewContainerRef,
    public dialog: MdDialog,
  ) {
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params.subscribe((params: Params) => {
        this.exTJobId = params.tJobId;
        this.exTJobExecId = params.execId;
      });
    }
  }

  ngOnInit() {
    this.titlesService.setHeadTitle('TJob Execution');
    this.exTJobExec = new ExternalTJobExecModel();
    this.loadExternalTJobExec();
  }

  loadExternalTJobExec(): void {
    this.externalService
      .getExternalTJobExecById(this.exTJobExecId)
      .subscribe((exTJobExec: ExternalTJobExecModel) => {
        this.exTJobExec = exTJobExec;

        // this.statusIcon = this.exTJobExec.getResultIcon();
        this.titlesService.setTopTitle(exTJobExec.getRouteString());

        this.exTJob = this.exTJobExec.exTJob;
        this.logsAndMetrics.initView(this.exTJob, this.exTJobExec);
      });
  }

  viewInLogAnalyzer(): void {
    this.router.navigate(['/loganalyzer'], {
      queryParams: { tjob: this.exTJob.id, exec: this.exTJobExec.id },
    });
  }
}
