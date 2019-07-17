import { Component, OnInit, Inject, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { TestPlanModel } from '../../models/test-plan-model';
import { BuildModel } from '../../models/build-model';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material';
import { TestLinkService } from '../../testlink.service';
import { TLTestCaseModel, PlatformModel } from '../../models/test-case-model';
import { EusService } from '../../../elastest-eus/elastest-eus.service';
import { ConfigurationService } from '../../../config/configuration-service.service';
import { StringListViewDescription } from '../../../shared/string-list-view/string-list-view.component';

@Component({
  selector: 'select-build-modal',
  templateUrl: './select-build-modal.component.html',
  styleUrls: ['./select-build-modal.component.scss'],
})
export class SelectBuildModalComponent implements OnInit {
  selectedBuild: BuildModel;
  testPlan: TestPlanModel;
  builds: BuildModel[];

  platforms: PlatformModel[] = [];
  selectedPlatform: PlatformModel;

  testProjectId: string | number;

  // Loaded to check if there are no test cases (not run)
  testPlanCases: TLTestCaseModel[];

  availableBrowserVersions: object;
  availableBrowserNamesList: string[];
  loadingBrowsers: boolean = false;

  // Single browser
  selectedBrowser: string;
  selectedVersion: object = {};

  // Crossbrowser
  crossbrowserEnabled: boolean = false;
  propagateEvents: boolean = true;
  selectedCrossbrowsers: string[] = [];

  ready: boolean = false;
  fail: boolean = false;

  extraHosts: string[] = [];
  extraHostsDescription: StringListViewDescription = new StringListViewDescription(
    'Add Extra Host to the browser container /etc/hosts',
    'Syntax => DNS:IP',
  );

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

    if (this.data.savedConfig) {
      this.initFromSavedConfig();
    } else {
      this.init();
    }
  }

  ngOnInit(): void {}

  init(): void {
    this.testProjectId = this.data.testProjectId;
    this.testPlan = this.data.testPlan;
    this.loadBrowsers();
    this.loadPlatforms();
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

  initFromSavedConfig(): void {
    this.runTestPlan(true);
  }

  isBasicDataLoaded(): boolean {
    return (
      this.testPlan &&
      this.builds !== undefined &&
      this.builds !== null &&
      this.testProjectId !== undefined &&
      this.testProjectId !== null
    );
  }

  loadPlatforms(): void {
    if (this.testPlan) {
      this.testLinkService.getTestPlanPlatforms(this.testPlan.id).subscribe(
        (platforms: PlatformModel[]) => {
          let nonePlatform: PlatformModel = new PlatformModel();
          if (platforms && platforms.length > 0) {
            this.platforms = platforms;
            this.selectedPlatform = this.platforms[0];
          } else {
            nonePlatform.id = 0;
            nonePlatform.name = 'NONE';
            this.platforms.push(nonePlatform);
            this.selectedPlatform = this.platforms[0];
          }
        },
        (error: Error) => console.log(error),
      );
    }
  }

  setReady(): void {
    if (this.isBasicDataLoaded()) {
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

  runTestPlan(fromSavedConfig: boolean = false): void {
    if (!fromSavedConfig) {
      let queryParams: any = {
        extraHosts: this.extraHosts,
        platform: this.selectedPlatform.id,
      };

      if (this.crossbrowserEnabled) {
        queryParams.browserList = this.selectedCrossbrowsers.join(',');
        queryParams.propagateEvents = this.propagateEvents;
      } else {
        queryParams.browserName = this.selectedBrowser;
        queryParams.browserVersion = this.selectedVersion[this.selectedBrowser];
      }

      this.router.navigate(
        ['/testlink/projects', this.testProjectId, 'plans', this.testPlan.id, 'builds', this.selectedBuild.id, 'exec', 'new'],
        { queryParams: queryParams },
      );
    } else {
      let savedConfig: any = this.data.savedConfig;

      let queryParams: any = {
        extraHosts: savedConfig.extraHosts,
        platform: savedConfig.platformId,
        fromSaved: true,
        exTJobExecId: savedConfig.exTJobExecId,
      };

      if (this.crossbrowserEnabled) {
        queryParams.browserList = savedConfig.browserList;
        queryParams.propagateEvents = savedConfig.propagateEvents;
      } else {
        queryParams.browserName = savedConfig.browserName;
        queryParams.browserVersion = savedConfig.browserVersion;
      }

      this.router.navigate(
        [
          '/testlink/projects',
          savedConfig.testProjectId,
          'plans',
          savedConfig.planId,
          'builds',
          savedConfig.buildId,
          'exec',
          'new',
        ],
        { queryParams: queryParams },
      );
      this.dialogRef.close();
    }
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
    this.availableBrowserVersions = obj;
    this.availableBrowserNamesList = Object.keys(this.availableBrowserVersions);
    if (this.availableBrowserNamesList.length > 0) {
      this.selectBrowser(this.availableBrowserNamesList[0]);
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
      this.availableBrowserNamesList &&
      (this.availableBrowserNamesList.length === 0 ||
        (this.availableBrowserNamesList.length === 1 &&
          this.availableBrowserVersions[this.availableBrowserNamesList[0]].length === 1))
    );
  }

  clearVersion(): void {
    Object.keys(this.selectedVersion).forEach((key: string) => (this.selectedVersion[key] = ''));
  }

  addBrowser(): void {
    if (this.selectedBrowser) {
      let version: string = 'latest';

      if (this.selectedVersion && this.selectedVersion[this.selectedBrowser]) {
        version = this.selectedVersion[this.selectedBrowser];
      }

      let newBrowserVersionPair: string = this.selectedBrowser + '_' + version;
      this.selectedCrossbrowsers.push(newBrowserVersionPair);
    }
  }
}
