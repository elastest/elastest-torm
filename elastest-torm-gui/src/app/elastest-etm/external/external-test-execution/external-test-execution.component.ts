import { Component, OnInit } from '@angular/core';
import { TitlesService } from '../../../shared/services/titles.service';
import { ExternalService } from '../external.service';
import { ActivatedRoute, Router, Params } from '@angular/router';
import { ExternalTestExecutionModel } from './external-test-execution-model';
import { ExternalTestCaseModel } from '../external-test-case/external-test-case-model';
import { ServiceType } from '../external-project/external-project-model';

@Component({
  selector: 'etm-external-test-execution',
  templateUrl: './external-test-execution.component.html',
  styleUrls: ['./external-test-execution.component.scss'],
})
export class ExternalTestExecutionComponent implements OnInit {
  exTestExecId: number;
  exTestExec: ExternalTestExecutionModel;
  exTestCase: ExternalTestCaseModel;

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
        this.titlesService.setTopTitle(exTestExec.getRouteString());
        this.exTestCase = this.exTestExec.exTestCase;
        this.serviceType = this.exTestExec.getServiceType();
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
