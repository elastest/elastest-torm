import { Component, OnInit, ViewChild } from '@angular/core';
import { TitlesService } from '../../../shared/services/titles.service';
import { ExternalService } from '../external.service';
import { ActivatedRoute, Router, Params } from '@angular/router';
import { ExternalTestExecutionModel } from './external-test-execution-model';
import { ExternalTestCaseModel } from '../external-test-case/external-test-case-model';
import { ServiceType } from '../external-project/external-project-model';
import { EtmMonitoringViewComponent } from '../../etm-monitoring-view/etm-monitoring-view.component';
import { ExternalTJobModel } from '../external-tjob/external-tjob-model';
import { ExternalTJobExecModel } from '../external-tjob-execution/external-tjob-execution-model';

@Component({
  selector: 'etm-external-test-execution',
  templateUrl: './external-test-execution.component.html',
  styleUrls: ['./external-test-execution.component.scss'],
})
export class ExternalTestExecutionComponent implements OnInit {
  @ViewChild('logsAndMetrics') logsAndMetrics: EtmMonitoringViewComponent;

  exTestExecId: number;
  exTestExec: ExternalTestExecutionModel;
  exTestCase: ExternalTestCaseModel;

  exTJob: ExternalTJobModel;
  exTJobExec: ExternalTJobExecModel;

  serviceType: ServiceType;

  constructor(
    private titlesService: TitlesService,
    private externalService: ExternalService,
    private route: ActivatedRoute,
    private router: Router,
  ) {
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params.subscribe((params: Params) => {
        this.exTestExecId = params.execId;
      });
    }
  }

  ngOnInit() {
    this.titlesService.setHeadTitle('External Test Execution');
    this.exTestExec = new ExternalTestExecutionModel();
    this.loadExternalTestExec();
  }

  loadExternalTestExec(): void {
    this.externalService.getExternalTestExecById(this.exTestExecId).subscribe(
      (exTestExec: ExternalTestExecutionModel) => {
        this.exTestExec = exTestExec;
        this.exTestCase = this.exTestExec.exTestCase;
        this.serviceType = this.exTestExec.getServiceType();

        this.exTJob = this.exTestCase.exTJob;
        this.exTJobExec = this.externalService.eTExternalModelsTransformService.jsonToExternalTJobExecModel(
          this.exTestExec.exTJobExec,
        );

        this.logsAndMetrics.initView(this.exTJob, this.exTJobExec);
        let browserLog: any = this.exTJobExec.getBrowserLogObj();
        if (browserLog) {
          this.logsAndMetrics.updateLog(browserLog, false);
        }
      },
      (error) => console.log(error),
    );
  }

  viewInLogAnalyzer(): void {
    this.router.navigate(['/loganalyzer'], {
      queryParams: {
        exTJob: this.exTestCase.exTJob.id,
        exTJobExec: this.exTestExec.exTJobExec.id,
        exTestCase: this.exTestCase.name,
        exTestExec: this.exTestExec.id,
      },
    });
  }
}
