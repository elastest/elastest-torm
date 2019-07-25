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

@Component({
  selector: 'etm-test-case',
  templateUrl: './test-case.component.html',
  styleUrls: ['./test-case.component.scss'],
})
export class TestCaseComponent implements OnInit {
  private filesUrlPrefix: string;

  @ViewChild('logsAndMetrics')
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

  nested: boolean = false;

  mp4Files: FileModel[] = [];

  // SuT Data
  filesColumns: ITdDataTableColumn[] = [
    { name: 'name', label: 'Name' },
    { name: 'options', label: 'Options', sortable: false, width: { min: 46, max: 130 } },
  ];

  @ViewChild('miniLogAnalyzer') private miniLogAnalyzer: ElastestLogAnalyzerComponent;

  constructor(
    private testCaseService: TestCaseService,
    public route: ActivatedRoute,
    private titlesService: TitlesService,
    private router: Router,
    private tJobExecService: TJobExecService,
    private configurationService: ConfigurationService,
  ) {
    this.filesUrlPrefix = this.configurationService.configModel.host.replace('4200', '8091');
  }

  ngOnInit(): void {
    this.titlesService.setPathName(this.router.routerState.snapshot.url);

    // Nested
    if (this.tJobExec && this.testCase) {
      this.nested = true;
      this.tJobId = this.tJobExec.tJob.id;
      this.tJobExecId = this.tJobExec.id;
      this.testCaseId = this.testCase.id;
      this.loadTestCase();
    } else {
      // Complete Section
      if (this.route.params !== null || this.route.params !== undefined) {
        this.route.params.subscribe((params: Params) => {
          params = params;
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
      this.testCase['result'] = this.testCase.getResultIcon();
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
    });
  }

  initAfterLoadTJobExec(): void {
    this.tJobExecService.logAnalyzerService
      .searchTestCaseStartAndFinishTraces(
        this.testCase.name,
        [this.tJobExec.monitoringIndex],
        this.tJobExec.startDate,
        this.tJobExec.endDate,
      )
      .subscribe((startFinishObj: StartFinishTestCaseTraces) => {
        if (startFinishObj) {
          let startDate: Date = startFinishObj.startDate;
          let endDate: Date = startFinishObj.finishDate;
          if (startDate && endDate) {
            this.logsAndMetrics.initView(this.tJobExec.tJob, this.tJobExec, startDate, endDate);
            this.showLogsAndMetrics = true;
          }
        }
      });
  }

  getExecutionFiles(): void {
    this.tJobExecService.getTJobExecutionFiles(this.tJobId, this.tJobExecId).subscribe(
      (tJobsExecFiles: FileModel[]) => {
        let i: number = 0;
        tJobsExecFiles.forEach((file: FileModel) => {
          if (this.isMP4(file)) {
            file['order'] = i;
            i++;
          }
        });
        this.testCase.setTestCaseFiles(tJobsExecFiles);
        this.mp4Files = this.getMP4Files();
      },
      (error: Error) => console.log(error),
    );
  }

  getMP4Files(): FileModel[] {
    let mp4Files: FileModel[] = [];
    mp4Files = this.testCase.files.filter((file: FileModel) => this.isMP4(file));
    return mp4Files;
  }

  isMP4(file: FileModel): boolean {
    return file && file.name.endsWith('mp4');
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
    // video position, ALL logs + Files tabs);
    this.goToTab(file['order'] + 2);
  }

  viewInLogAnalyzer(): void {
    if (this.testCase) {
      this.router.navigate(
        [
          '/projects',
          this.tJobExec.tJob.project.id,
          'tjob',
          this.tJobId,
          'tjob-exec',
          this.tJobExecId,
          'testSuite',
          this.testCase.testSuite.id,
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
