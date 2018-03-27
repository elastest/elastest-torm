import { TitlesService } from '../../shared/services/titles.service';
import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { TestCaseService } from './test-case.service';
import { TestCaseModel } from './test-case-model';
import { ESBoolQueryModel, ESTermModel } from '../../shared/elasticsearch-model/es-query-model';
import { ElastestESService } from '../../shared/services/elastest-es.service';
import { LogAnalyzerModel } from '../../elastest-log-analyzer/log-analyzer-model';
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
  public logAnalyzer: LogAnalyzerModel;
  public componentsStreams: any[];
  public streamTypeTerm: ESTermModel;

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
    this.logAnalyzer = new LogAnalyzerModel();
    this.streamTypeTerm = new ESTermModel();
    this.initStreamTypeTerm();
    this.getComponents();
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
        this.testCase.setTestCaseFiles(tJobsExecFiles, 'mp4');
      },
      (error) => console.log(error),
    );
  }

  getComponents(): void {
    let componentStreamQuery: ESBoolQueryModel = new ESBoolQueryModel();
    componentStreamQuery.bool.must.termList.push(this.streamTypeTerm);
    let fieldsList: string[] = ['component', 'stream'];
    this.elastestESService
      .getAggTreeOfIndex(this.logAnalyzer.selectedIndicesToString(), fieldsList, componentStreamQuery.convertToESFormat())
      .subscribe((componentsStreams: any[]) => {
        this.componentsStreams = componentsStreams;
      });
  }

  private initStreamTypeTerm(): void {
    this.streamTypeTerm.name = 'stream_type';
    this.streamTypeTerm.value = 'log';
  }
}
