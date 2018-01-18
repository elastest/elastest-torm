import { ActivatedRoute, Params } from '@angular/router';
import { TitlesService } from '../../../shared/services/titles.service';
import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { TestPlanModel } from '../../models/test-plan-model';
import { TestLinkService } from '../../testlink.service';
import { TestProjectModel } from '../../models/test-project-model';

@Component({
  selector: 'app-test-plan-form',
  templateUrl: './test-plan-form.component.html',
  styleUrls: ['./test-plan-form.component.scss']
})
export class TestPlanFormComponent implements OnInit {


  @ViewChild('planNameInput') planNameInput: ElementRef;

  testPlan: TestPlanModel;
  testProjects: TestProjectModel[] = [];
  currentPath: string = '';

  constructor(
    private titlesService: TitlesService,
    private testlinkService: TestLinkService, private route: ActivatedRoute,
  ) { }

  ngOnInit() {
    this.titlesService.setHeadTitle('Edit Test Plan');
    this.testPlan = new TestPlanModel();
    this.currentPath = this.route.snapshot.url[0].path;
    this.loadTestProjects();
    if (this.route.params !== null || this.route.params !== undefined) {
      if (this.currentPath === 'edit') {
        // this.route.params.switchMap((params: Params) => this.testlinkService.getPlanById(params['planId']))
        //   .subscribe((plan: TestPlanModel) => {
        //     this.testPlan = plan;
        //     this.titlesService.setTopTitle(this.testPlan.getRouteString());
        //   });
      }
    }
  }

  ngAfterViewInit() {
    this.planNameInput.nativeElement.focus();
  }

  loadTestProjects(): void {
    this.testlinkService.getAllTestProjects()
      .subscribe(
      (projects: TestProjectModel[]) => {
        this.testProjects = projects;
      },
      (error) => console.log(error)
      );
  }

  goBack(): void {
    window.history.back();
  }

  save(): void {
    this.testlinkService.createTestPlan(this.testPlan)
      .subscribe(
      (plan) => this.postSave(plan),
      (error) => this.testlinkService.popupService.openSnackBar('Error: Plan with name ' + this.testPlan.name + ' already exist'),
    );

  }

  postSave(plan: any): void {
    this.testPlan = plan;
    window.history.back();
  }
}
