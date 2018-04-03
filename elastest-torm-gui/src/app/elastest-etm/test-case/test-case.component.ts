import { TitlesService } from '../../shared/services/titles.service';
import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { TestCaseService } from './test-case.service';
import { TestCaseModel } from './test-case-model';
import { ElastestESService } from '../../shared/services/elastest-es.service';
import { TreeNode } from 'angular-tree-component/dist/defs/api';
import { ElastestLogAnalyzerComponent } from '../../elastest-log-analyzer/elastest-log-analyzer.component';
import { TJobExecService } from '../tjob-exec/tjobExec.service';
import { FileModel } from '../files-manager/file-model';

@Component({
  selector: 'etm-test-case',
  templateUrl: './test-case.component.html',
  styleUrls: ['./test-case.component.scss'],
})
export class TestCaseComponent implements OnInit {
  public params;
  public testCase: TestCaseModel;

  @ViewChild('miniLogAnalyzer') private miniLogAnalyzer: ElastestLogAnalyzerComponent;

  constructor(
    private testCaseService: TestCaseService,
    private elastestESService: ElastestESService,
    public route: ActivatedRoute,
    private titlesService: TitlesService,
    private router: Router,
    private tJobExecService: TJobExecService,
  ) {}

  ngOnInit() {
    this.titlesService.setPathName(this.router.routerState.snapshot.url);
    this.getTestCase();
  }

  getTestCase(): void {
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params.subscribe((params: Params) => {
        this.params = params;
        this.testCaseService.getTestCaseById(this.params.testCaseId).subscribe((testCase: TestCaseModel) => {
          this.testCase = testCase;
          this.getExecutionFiles();
          this.testCase['result'] = this.testCase.getResultIcon();
        });
      });
    }
  }

  getExecutionFiles(): void {
    this.tJobExecService.getTJobExecutionFiles(this.params.tJobId, this.params.tJobExecId).subscribe(
      (tJobsExecFiles: FileModel[]) => {
        this.testCase.setTestCaseFiles(tJobsExecFiles);
      },
      (error) => console.log(error),
    );
  }

  isMP4(file: FileModel): boolean {
    return file && file.name.endsWith('mp4');
  }
}
