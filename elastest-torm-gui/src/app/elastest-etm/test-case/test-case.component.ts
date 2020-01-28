import { TitlesService } from '../../shared/services/titles.service';
import { Component, OnInit, ViewChild, Input } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { TestCaseService } from './test-case.service';
import { TestCaseModel } from './test-case-model';
import { ElastestLogAnalyzerComponent } from '../../elastest-log-analyzer/elastest-log-analyzer.component';
import { TJobExecService } from '../tjob-exec/tjobExec.service';
import { FileModel } from '../files-manager/file-model';
import { EtmMonitoringViewComponent } from '../etm-monitoring-view/etm-monitoring-view.component';
import { TJobExecModel } from '../tjob-exec/tjobExec-model';
import { StartFinishTestCaseTraces } from '../../elastest-log-analyzer/log-analyzer.service';
import { ITdDataTableColumn } from '@covalent/core';
import { TestSuiteModel } from '../test-suite/test-suite-model';
import { FilesService } from '../../shared/services/files.service';

@Component({
  selector: 'etm-test-case',
  templateUrl: './test-case.component.html',
  styleUrls: ['./test-case.component.scss'],
})
export class TestCaseComponent implements OnInit {
  @ViewChild('miniLogAnalyzer', { static: false })
  private miniLogAnalyzer: ElastestLogAnalyzerComponent;

  @ViewChild('logsAndMetrics', { static: true })
  logsAndMetrics: EtmMonitoringViewComponent;

  showLogsAndMetrics: boolean = false;

  tJobId: number;
  tJobExecId: number;
  testCaseId: number;

  public selectedTab: number;

  @Input()
  tJobExec: TJobExecModel;
  @Input()
  testCase: TestCaseModel;
  @Input()
  testSuite: TestSuiteModel;

  nested: boolean = false;

  videoFiles: FileModel[] = [];
  eusSessionsNames: string[] = [];
  eusSessionsGroupsMap: Map<string, FileModel[]> = new Map();
  eusSessionsGroupsVmafMap: Map<string, string> = new Map();

  // SuT Data
  filesColumns: ITdDataTableColumn[] = [
    { name: 'name', label: 'Name' },
    { name: 'extension', label: 'extension', width: { min: 86, max: 135 } },
    { name: 'folderName', label: 'Category', width: { min: 86, max: 190 } },
    {
      name: 'resourceType',
      label: 'Resource Type',
      width: { min: 110, max: 266 },
    },
    { name: 'options', label: 'Options', width: { min: 84, max: 120 } },
  ];

  constructor(
    private testCaseService: TestCaseService,
    public route: ActivatedRoute,
    private titlesService: TitlesService,
    private router: Router,
    private tJobExecService: TJobExecService,
    private filesService: FilesService,
  ) {}

  ngOnInit(): void {
    // Nested
    if (this.tJobExec && this.testCase) {
      this.nested = true;
      this.tJobId = this.tJobExec.tJob.id;
      this.tJobExecId = this.tJobExec.id;
      this.testCaseId = this.testCase.id;
      this.testCase = this.testCase;
      this.initAfterLoadTestCase();
    } else {
      this.titlesService.setPathName(this.router.routerState.snapshot.url);
      // Complete Section
      if (this.route.params !== null || this.route.params !== undefined) {
        this.route.params.subscribe((params: Params) => {
          if (params) {
            this.tJobId = params.tJobId;
            this.tJobExecId = params.tJobExecId;
            this.testCaseId = params.testCaseId;
            this.loadTestCase();
          }
        });
      }
    }
  }

  loadTestCase(): void {
    this.testCaseService.getTestCaseById(this.testCaseId).subscribe((testCase: TestCaseModel) => {
      this.testCase = testCase;
      this.initAfterLoadTestCase();
    });
  }

  initAfterLoadTestCase(): void {
    this.testCase.result = this.testCase.getResultIcon();
    this.getExecutionFiles();
    if (this.logsAndMetrics) {
      if (this.nested) {
        this.initAfterLoadTJobExec();
      } else {
        this.tJobExecService.getTJobExecutionByTJobId(this.tJobId, this.tJobExecId).subscribe(
          (tJobExec: TJobExecModel) => {
            this.tJobExec = tJobExec;
            this.initAfterLoadTJobExec();
          },
          (error: Error) => console.error(error),
        );
      }
    }
  }

  initAfterLoadTJobExec(): void {
    // TODO Temporally disabled (not working: logsView and metricsView are undefined)
    // if (this.testCase.startDate && this.testCase.endDate) {
    //   this.initMonitoring(this.testCase.startDate, this.testCase.endDate);
    // } else {
    this.tJobExecService.logAnalyzerService
      .searchTestCaseStartAndFinishTraces(
        this.testCase.name,
        [this.tJobExec.monitoringIndex],
        this.tJobExec.startDate,
        this.tJobExec.endDate,
        this.testSuite ? this.testSuite.name : undefined,
      )
      .subscribe((startFinishObj: StartFinishTestCaseTraces) => {
        if (startFinishObj) {
          let startDate: Date = startFinishObj.startDate;
          let endDate: Date = startFinishObj.finishDate;
          this.initMonitoring(startDate, endDate);
        }
      });
    // }
  }

  initMonitoring(startDate: Date, endDate: Date): void {
    if (startDate && endDate) {
      this.logsAndMetrics.initView(this.tJobExec.tJob, this.tJobExec, startDate, endDate);
      this.showLogsAndMetrics = true;
    }
  }

  getExecutionFiles(): void {
    this.tJobExecService.getTJobExecutionFiles(this.tJobId, this.tJobExecId).subscribe(
      (tJobsExecFiles: FileModel[]) => {
        this.videoFiles = [];
        let i: number = 0;
        this.testCase.setTestCaseFiles(tJobsExecFiles);

        // Get video files and eus sessions names
        this.testCase.files.forEach((file: FileModel) => {
          if (this.filesService.isVideoByFileModel(file)) {
            file['order'] = i;
            i++;
            this.videoFiles.push(file);
          }
          if (file && file.isEusMetadataFile() && file.name) {
            let filenameOnly: string = file.name.split('.')[0];
            this.eusSessionsNames.push(filenameOnly);
            this.eusSessionsGroupsMap.set(filenameOnly, []);
          }
        });

        // Group Files by session
        this.testCase.files.forEach((file: FileModel) => {
          for (let sessionName of this.eusSessionsNames) {
            if (file && file.name.startsWith(sessionName)) {
              this.eusSessionsGroupsMap.get(sessionName).push(file);

              // Vmaf
              if (this.filesService.isVmafAverageByFileModel(file)) {
                this.filesService.getFileContent(this.filesService.getFileUrl(file)).subscribe(
                  (data: any) => {
                    this.eusSessionsGroupsVmafMap.set(sessionName, data);
                  },
                  (error: Error) => console.error(error),
                );
              }
              break;
            }
          }
        });
      },
      (error: Error) => console.log(error),
    );
  }

  goToTab(num: number): void {
    this.selectedTab = num;
  }

  goToVideoTab(file: FileModel): void {
    // video position, LogAnalyzer + Files + Details tabs);
    let previousTabs: number = 3;

    if (this.caseHasBrowserFiles()) {
      // Browser Videos Tab
      previousTabs += 1;
    }

    this.goToTab(file['order'] + previousTabs);
  }

  viewInLogAnalyzer(): void {
    if (this.testCase && this.testSuite) {
      this.router.navigate(
        [
          '/projects',
          this.tJobExec.tJob.project.id,
          'tjob',
          this.tJobId,
          'tjob-exec',
          this.tJobExecId,
          'testSuite',
          this.testSuite.id,
          'testCase',
          this.testCase.id,
          'loganalyzer',
        ],
        {
          queryParams: {
            tjob: this.tJobId,
            exec: this.tJobExecId,
            testCase: this.testCase.name,
          },
        },
      );
    }
  }

  caseHasFiles(): boolean {
    return this.testCase && this.testCase.files && this.testCase.files.length > 0;
  }

  caseHasBrowserFiles(): boolean {
    return this.eusSessionsNames.length > 0;
  }
}
