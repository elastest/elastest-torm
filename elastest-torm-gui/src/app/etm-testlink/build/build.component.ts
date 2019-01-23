import { BuildModel } from '../models/build-model';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { TitlesService } from '../../shared/services/titles.service';
import { Component, OnInit } from '@angular/core';
import { TestLinkService } from '../testlink.service';
import { TLTestCaseModel } from '../models/test-case-model';
import { MatDialog, MatDialogRef } from '@angular/material';
import { ExecuteCaseModalComponent } from './execute-case-modal/execute-case-modal.component';
import { ServiceType } from '../../elastest-etm/external/external-project/external-project-model';

@Component({
  selector: 'testlink-build',
  templateUrl: './build.component.html',
  styleUrls: ['./build.component.scss'],
})
export class BuildComponent implements OnInit {
  build: BuildModel;
  testCases: TLTestCaseModel[] = [];
  testProjectId: number;

  loadingCases: boolean = true;

  // TestCase Data
  testCasesColumns: any[] = [
    { name: 'id', label: 'Id', width: 80 },
    { name: 'name', label: 'Name' },
    { name: 'testCaseStatus', label: 'Status' },
    // { name: 'testSuiteId', label: 'Suite ID' },
    // { name: 'testProjectId', label: 'Project ID' },
    // { name: 'authorLogin', label: 'Author Login' },
    { name: 'summary', label: 'Summary' },
    { name: 'preconditions', label: 'Preconditions' },
    // { name: 'testImportance', label: 'Importance' },
    { name: 'executionType', label: 'Exec Type' },
    { name: 'executionOrder', label: 'Exec Order' },
    { name: 'order', label: 'Order' },
    // { name: 'internalId', label: 'Internal ID' },
    { name: 'fullExternalId', label: 'External ID' },
    // { name: 'checkDuplicatedName', label: 'Check Duplicated Name' },
    // { name: 'actionOnDuplicatedName', label: 'Action On Duplicated Name' },
    { name: 'versionId', label: 'Version ID' },
    { name: 'version', label: 'Version' },
    // { name: 'parentId', label: 'Parent ID' },
    { name: 'executionStatus', label: 'Execution Status' },
    { name: 'platform.id', label: 'Platform' },
    { name: 'featureId', label: 'Feature Id' },
    // { name: 'customFields', label: 'Custom Fields' },
    // steps: TestCaseStepModel[];

    { name: 'options', label: 'Options' },
  ];

  constructor(
    private titlesService: TitlesService,
    private testLinkService: TestLinkService,
    private route: ActivatedRoute,
    private router: Router,
    public dialog: MatDialog,
  ) {}

  ngOnInit() {
    this.titlesService.setHeadTitle('Build');
    this.build = new BuildModel();
    this.loadBuild();
  }

  loadBuild(): void {
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params
        .switchMap((params: Params) => {
          this.testProjectId = params['projectId'];
          return this.testLinkService.getBuildById(params['buildId']);
        })
        .subscribe((build: BuildModel) => {
          this.build = build;
          this.titlesService.setPathName(this.router.routerState.snapshot.url);

          this.loadTestCases();
        });
    }
  }

  loadTestCases(): void {
    this.testLinkService.getBuildTestCases(this.build).subscribe(
      (testCases: TLTestCaseModel[]) => {
        this.testCases = testCases;
        this.loadingCases = false;
      },
      (error) => console.log(error),
    );
  }

  execTestCase(testCase: TLTestCaseModel): void {
    this.openSelectExecutions(testCase);
  }

  execTestCaseWithElastest(testCase: TLTestCaseModel): void {
    let type: ServiceType = 'TESTLINK';
    this.router.navigate(['/external/execute'], {
      queryParams: { serviceType: type, testCaseId: testCase.id, buildId: this.build.id },
    });
  }

  public openSelectExecutions(testCase: TLTestCaseModel): void {
    let data: any = {
      testCase: testCase,
      build: this.build,
    };
    let dialogRef: MatDialogRef<ExecuteCaseModalComponent> = this.dialog.open(ExecuteCaseModalComponent, {
      data: data,
      height: '80%',
      width: '90%',
    });
    dialogRef.afterClosed().subscribe((data: any) => {
      if (data) {
        // Ok Pressed
        if (data.saved) {
          this.loadingCases = true;
          this.testLinkService.popupService.openSnackBar('Execution has been saved successfully');
          this.loadBuild();
        }
      } else {
      }
    });
  }
}
