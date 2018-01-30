import { BuildModel } from '../models/build-model';
import { TestPlanModel } from '../models/test-plan-model';
import { TdDialogService } from '@covalent/core/dialogs/services/dialog.service';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { TitlesService } from '../../shared/services/titles.service';
import { Component, OnInit, ViewContainerRef } from '@angular/core';
import { TestLinkService } from '../testlink.service';
import { MdDialog } from '@angular/material';
import { ExternalTJobModel } from '../../elastest-etm/external/external-tjob/external-tjob-model';


@Component({
  selector: 'testlink-test-plan',
  templateUrl: './test-plan.component.html',
  styleUrls: ['./test-plan.component.scss']
})
export class TestPlanComponent implements OnInit {
  testPlan: TestPlanModel;
  builds: BuildModel[] = [];
  testProjectId: number;

  externalTJob: ExternalTJobModel;

  // Build Data
  buildColumns: any[] = [
    { name: 'id', label: 'Id' },
    { name: 'name', label: 'Name' },
    { name: 'testPlanId', label: 'Test Plan ID' },
    { name: 'notes', label: 'Notes' },

    // { name: 'options', label: 'Options' },
  ];

  constructor(private titlesService: TitlesService, private testLinkService: TestLinkService,
    private route: ActivatedRoute, private router: Router,
    private _dialogService: TdDialogService, private _viewContainerRef: ViewContainerRef,
    public dialog: MdDialog
  ) { }

  ngOnInit() {
    this.titlesService.setHeadTitle('Test Plan');
    this.testPlan = new TestPlanModel();
    this.loadPlan();
  }

  loadPlan(): void {
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params.switchMap(
        (params: Params) => {
          this.testProjectId = params['projectId'];
          return this.testLinkService.getTestPlanById(params['planId']);
        }
      )
        .subscribe((plan: TestPlanModel) => {
          this.testPlan = plan;
          this.titlesService.setTopTitle(this.testPlan.getRouteString());

          this.loadBuilds();
          this.loadExternalTJob();
        });
    }
  }

  loadBuilds(): void {
    this.testLinkService.getPlanBuilds(this.testPlan)
      .subscribe(
      (builds: BuildModel[]) => {
        this.builds = builds;
      },
      (error) => console.log(error),
    );
  }

  loadExternalTJob(): void {
    this.testLinkService.getExternalTJobByTestPlanId(this.testPlan.id)
      .subscribe(
      (tJob: ExternalTJobModel) => {
        this.externalTJob = tJob;
      },
      (error) => console.log(error),
    );
  }
}
