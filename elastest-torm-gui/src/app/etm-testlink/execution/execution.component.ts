import { TdDialogService } from '@covalent/core/dialogs/services/dialog.service';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { TitlesService } from '../../shared/services/titles.service';
import { Component, OnInit, ViewContainerRef } from '@angular/core';
import { TestProjectModel } from '../models/test-project-model';
import { TestLinkService } from '../testlink.service';
import { MdDialog } from '@angular/material';
import { TestCaseExecutionModel } from '../models/test-case-execution-model';

@Component({
  selector: 'testlink-test-execution',
  templateUrl: './execution.component.html',
  styleUrls: ['./execution.component.scss'],
})
export class ExecutionComponent implements OnInit {
  testExec: TestCaseExecutionModel;

  constructor(
    private titlesService: TitlesService,
    private testLinkService: TestLinkService,
    private route: ActivatedRoute,
    private router: Router,
    private _dialogService: TdDialogService,
    private _viewContainerRef: ViewContainerRef,
    public dialog: MdDialog,
  ) {}
  ngOnInit() {
    this.titlesService.setHeadTitle('Test Case');
    this.testExec = new TestCaseExecutionModel();
    this.loadCase();
  }

  loadCase(): void {
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params
        .switchMap((params: Params) => this.testLinkService.getTestExecById(params['caseId'], params['execId']))
        .subscribe((testExec: TestCaseExecutionModel) => {
          this.testExec = testExec;
          this.titlesService.setTopTitle(this.testExec.getRouteString());
        });
    }
  }
}
