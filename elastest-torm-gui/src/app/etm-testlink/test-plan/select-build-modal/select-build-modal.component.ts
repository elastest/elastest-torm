import { Component, OnInit, Inject, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { TestPlanModel } from '../../models/test-plan-model';
import { BuildModel } from '../../models/build-model';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material';
import { TestLinkService } from '../../testlink.service';
import { TLTestCaseModel } from '../../models/test-case-model';
import { EusService } from '../../../elastest-eus/elastest-eus.service';
import { ConfigurationService } from '../../../config/configuration-service.service';

@Component({
  selector: 'select-build-modal',
  templateUrl: './select-build-modal.component.html',
  styleUrls: ['./select-build-modal.component.scss'],
})
export class SelectBuildModalComponent implements OnInit {
  selectedBuild: BuildModel;
  testPlan: TestPlanModel;
  builds: BuildModel[];
  testProjectId: string | number;
  testPlanCases: TLTestCaseModel[];

  selectedBrowser: string;
  selectedVersion: object = {};

  browserVersions: object;
  browserNamesList: string[];
  loadingBrowsers: boolean = false;

  ready: boolean = false;
  fail: boolean = false;

  extraHosts: string[] = [];

  constructor(
    private router: Router,
    private testLinkService: TestLinkService,
    private eusService: EusService,
    @Inject(MAT_DIALOG_DATA) public data: any,
    public dialogRef: MatDialogRef<SelectBuildModalComponent>,
    private configurationService: ConfigurationService,
  ) {
    this.eusService.setEusUrl(this.configurationService.configModel.eusServiceUrl);
    this.eusService.setEusHost(this.configurationService.configModel.eusHost);

    this.init();
  }

  ngOnInit(): void {}

  init(): void {
    this.testProjectId = this.data.testProjectId;
    this.testPlan = this.data.testPlan;
    this.loadBrowsers();
    if (this.data.builds) {
      this.builds = this.data.builds;
      this.setReady();
    } else {
      this.testLinkService.getPlanBuilds(this.testPlan).subscribe(
        (builds: BuildModel[]) => {
          this.builds = builds;
          this.setReady();
        },
        (error: Error) => console.log(error),
      );
    }
  }

  setReady(): void {
    if (this.testPlan && this.builds !== undefined && this.builds !== null && this.testProjectId) {
      this.testLinkService.getPlanTestCases(this.testPlan).subscribe(
        (testCases: TLTestCaseModel[]) => {
          this.testPlanCases = testCases;
          this.ready = true;

          // Select first automatically
          if (this.builds && this.builds.length > 0) {
            this.selectedBuild = this.builds[0];
            // If there is only one build, execute directly
            if (
              this.builds.length === 1 &&
              this.selectedBuild &&
              this.selectedBuild.id &&
              this.thereIsOnlyOneBrowser() &&
              this.selectedBrowser &&
              this.selectedVersion &&
              this.selectedVersion[this.selectedBrowser]
            ) {
              this.runTestPlan();
              this.dialogRef.close();
            }
          }
        },
        (error: Error) => {
          console.log(error);
          this.fail = true;
        },
      );
    } else {
      this.fail = true;
    }
  }

  runTestPlan(): void {
    this.router.navigate(
      ['/testlink/projects', this.testProjectId, 'plans', this.testPlan.id, 'builds', this.selectedBuild.id, 'exec', 'new'],
      {
        queryParams: {
          browserName: this.selectedBrowser,
          browserVersion: this.selectedVersion[this.selectedBrowser],
          extraHosts: this.extraHosts,
        },
      },
    );
  }

  /* *** Browsers *** */

  loadBrowsers(): void {
    this.loadingBrowsers = true;

    this.eusService.getBrowsers().subscribe(
      (data: any) => {
        this.initBrowsersByGiven(data);
        this.loadingBrowsers = false;
      },
      (error: Error) => {
        console.log(error);
        this.loadingBrowsers = false;
      },
    );
  }

  initBrowsersByGiven(obj: object): void {
    this.browserVersions = obj;
    this.browserNamesList = Object.keys(this.browserVersions);
    if (this.browserNamesList.length > 0) {
      this.selectBrowser(this.browserNamesList[0]);
    }
  }

  selectBrowser(browser: string): void {
    this.selectedBrowser = browser;
    Object.keys(this.selectedVersion).forEach((key: string) => {
      if (key !== browser) {
        this.selectedVersion[key] = '';
      }
    });
  }

  thereIsOnlyOneBrowser(): boolean {
    return (
      this.browserNamesList &&
      (this.browserNamesList.length === 0 ||
        (this.browserNamesList.length === 1 && this.browserVersions[this.browserNamesList[0]].length === 1))
    );
  }

  clearVersion(): void {
    Object.keys(this.selectedVersion).forEach((key: string) => (this.selectedVersion[key] = ''));
  }
}
