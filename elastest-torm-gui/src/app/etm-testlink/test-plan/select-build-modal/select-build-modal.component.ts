import { Component, OnInit, Inject } from '@angular/core';
import { Router } from '@angular/router';
import { TestPlanModel } from '../../models/test-plan-model';
import { BuildModel } from '../../models/build-model';
import { MD_DIALOG_DATA } from '@angular/material';

@Component({
  selector: 'app-select-build-modal',
  templateUrl: './select-build-modal.component.html',
  styleUrls: ['./select-build-modal.component.scss'],
})
export class SelectBuildModalComponent implements OnInit {
  selectedBuild: BuildModel;
  testPlan: TestPlanModel;
  builds: BuildModel[];
  testProjectId: string | number;

  ready: boolean = false;

  constructor(private router: Router, @Inject(MD_DIALOG_DATA) public data: any) {
    this.testPlan = data.testPlan;
    this.builds = data.builds;
    this.testProjectId = data.testProjectId;
    if (this.testPlan && this.builds && this.testProjectId) {
      this.ready = true;
    }
  }

  ngOnInit() {}

  runTestPlan(): void {
    this.router.navigate([
      '/testlink/projects',
      this.testProjectId,
      'plans',
      this.testPlan.id,
      'builds',
      this.selectedBuild.id,
      'exec',
      'new',
    ]);
  }
}
