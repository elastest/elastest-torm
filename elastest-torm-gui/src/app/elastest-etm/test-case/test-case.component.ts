import { TitlesService } from '../../shared/services/titles.service';
import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { TestCaseService } from './test-case.service';
import { TestCaseModel } from './test-case-model';
import { ElastestLogAnalyzerComponent } from '../../elastest-log-analyzer/elastest-log-analyzer.component';
import { TJobExecService } from '../tjob-exec/tjobExec.service';
import { FileModel } from '../files-manager/file-model';
import { ConfigurationService } from '../../config/configuration-service.service';
import { MonitoringService } from '../../shared/services/monitoring.service';

@Component({
  selector: 'etm-test-case',
  templateUrl: './test-case.component.html',
  styleUrls: ['./test-case.component.scss'],
})
export class TestCaseComponent implements OnInit {
  public params;
  public testCase: TestCaseModel;
  public selectedTab: number;
  private filesUrlPrefix: string;

  @ViewChild('miniLogAnalyzer') private miniLogAnalyzer: ElastestLogAnalyzerComponent;

  constructor(
    private testCaseService: TestCaseService,
    private monitoringService: MonitoringService,
    public route: ActivatedRoute,
    private titlesService: TitlesService,
    private router: Router,
    private tJobExecService: TJobExecService,
    private configurationService: ConfigurationService,
  ) {
    this.filesUrlPrefix = configurationService.configModel.host.replace('4200', '8091');
  }

  ngOnInit(): void {
    this.titlesService.setPathName(this.router.routerState.snapshot.url);
    this.getTestCase();
  }

  getTestCase(): void {
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params.subscribe((params: Params) => {
        this.params = params;
        this.testCaseService.getTestCaseById(this.params.testCaseId).subscribe((testCase: TestCaseModel) => {
          this.testCase = testCase;
          this.testCase['result'] = this.testCase.getResultIcon();
          this.getExecutionFiles();
        });
      });
    }
  }

  getExecutionFiles(): void {
    this.tJobExecService.getTJobExecutionFiles(this.params.tJobId, this.params.tJobExecId).subscribe(
      (tJobsExecFiles: FileModel[]) => {
        let i: number = 0;
        tJobsExecFiles.forEach((file: FileModel) => {
          if (this.isMP4(file)) {
            file['tabRef'] = this.miniLogAnalyzer.componentsTree.treeModel.nodes.length + 2 + i; // components and Logs + Files tabs
            i++;
          }
        });
        this.testCase.setTestCaseFiles(tJobsExecFiles);
      },
      (error) => console.log(error),
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

  goToTab(num: number): void {
    this.selectedTab = num;
  }
}
