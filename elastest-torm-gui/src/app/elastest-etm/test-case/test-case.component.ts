import { TitlesService } from '../../shared/services/titles.service';
import { Component, OnInit, ViewChild, Input } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { TestCaseService } from './test-case.service';
import { TestCaseModel } from './test-case-model';
import { ElastestLogAnalyzerComponent } from '../../elastest-log-analyzer/elastest-log-analyzer.component';
import { TJobExecService } from '../tjob-exec/tjobExec.service';
import { FileModel } from '../files-manager/file-model';
import { ConfigurationService } from '../../config/configuration-service.service';
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
  private filesUrlPrefix: string;

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

  mp4Files: FileModel[] = [];

  // SuT Data
  filesColumns: ITdDataTableColumn[] = [
    { name: 'name', label: 'Name' },
    { name: 'options', label: 'Options', sortable: false, width: { min: 46, max: 130 } },
  ];

  @ViewChild('miniLogAnalyzer', { static: false }) private miniLogAnalyzer: ElastestLogAnalyzerComponent;

  constructor(
    private testCaseService: TestCaseService,
    public route: ActivatedRoute,
    private titlesService: TitlesService,
    private router: Router,
    private tJobExecService: TJobExecService,
    private configurationService: ConfigurationService,
    private filesService: FilesService,
  ) {
    this.filesUrlPrefix = this.configurationService.configModel.host.replace('4200', '8091');
  }

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
        this.mp4Files = [];
        let i: number = 0;
        tJobsExecFiles.forEach((file: FileModel) => {
          if (this.filesService.isVideoByFileModel(file) && file.name.startsWith(this.testCase.name)) {
            file['order'] = i;
            i++;
            this.mp4Files.push(file);
          }
        });
        this.testCase.setTestCaseFiles(tJobsExecFiles);
      },
      (error: Error) => console.log(error),
    );
  }

  getVideoFileUrl(file: FileModel): string {
    return this.filesUrlPrefix + file.encodedUrl;
  }

  openUrlInNewTab(file: FileModel): void {
    window.open(this.getVideoFileUrl(file));
  }

  goToTab(num: number): void {
    this.selectedTab = num;
  }

  goToVideoTab(file: FileModel): void {
    // video position, LogAnalyzer + Files + Details tabs);
    this.goToTab(file['order'] + 3);
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
          queryParams: { tjob: this.tJobId, exec: this.tJobExecId, testCase: this.testCase.name },
        },
      );
    }
  }
}
