import { ActivatedRoute, Params, Router } from '@angular/router';
import { TitlesService } from '../../shared/services/titles.service';
import { Component, OnInit, Input } from '@angular/core';
import { TestLinkService } from '../testlink.service';
import { MatDialog } from '@angular/material';
import { TestCaseExecutionModel } from '../models/test-case-execution-model';

@Component({
  selector: 'testlink-test-execution',
  templateUrl: './execution.component.html',
  styleUrls: ['./execution.component.scss'],
})
export class ExecutionComponent implements OnInit {
  @Input() caseId: number;
  @Input() execId: number;

  testExec: TestCaseExecutionModel;

  constructor(
    private titlesService: TitlesService,
    private testLinkService: TestLinkService,
    private route: ActivatedRoute,
    private router: Router,
    public dialog: MatDialog,
  ) {}
  ngOnInit() {
    this.titlesService.setHeadTitle('Test Case');
    this.testExec = new TestCaseExecutionModel();
    this.init();
  }

  init(): void {
    if (this.caseId !== undefined && this.execId !== undefined) {
      this.loadExecution();
    } else if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params.subscribe((params: Params) => {
        this.caseId = params['caseId'];
        this.execId = params['execId'];
        this.loadExecution();
      });
    }
  }

  loadExecution(): void {
    this.testLinkService.getTestExecById(this.caseId, this.execId).subscribe((testExec: TestCaseExecutionModel) => {
      this.testExec = testExec;
      this.titlesService.setPathName(this.router.routerState.snapshot.url);
    });
  }
}
