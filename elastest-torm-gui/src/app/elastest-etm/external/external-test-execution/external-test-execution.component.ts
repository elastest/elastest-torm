import { Component, OnInit, ViewChild } from '@angular/core';
import { TitlesService } from '../../../shared/services/titles.service';
import { ExternalService } from '../external.service';
import { ActivatedRoute, Router, Params } from '@angular/router';
import { ExternalTestExecutionModel } from './external-test-execution-model';
import { ExternalTestCaseModel } from '../external-test-case/external-test-case-model';
import { ServiceType } from '../external-project/external-project-model';
import { ExternalTJobExecModel } from '../external-tjob-execution/external-tjob-execution-model';
import { ElastestLogAnalyzerComponent } from '../../../elastest-log-analyzer/elastest-log-analyzer.component';
import { FileModel } from '../../files-manager/file-model';
import { ConfigurationService } from '../../../config/configuration-service.service';
import { FilesService } from '../../../shared/services/files.service';

@Component({
  selector: 'etm-external-test-execution',
  templateUrl: './external-test-execution.component.html',
  styleUrls: ['./external-test-execution.component.scss'],
})
export class ExternalTestExecutionComponent implements OnInit {
  @ViewChild('miniLogAnalyzer', { static: false }) public miniLogAnalyzer: ElastestLogAnalyzerComponent;

  exTestExecId: number;
  exTestExec: ExternalTestExecutionModel;
  exTestCase: ExternalTestCaseModel;
  files: FileModel[] = [];
  filesUrlPrefix: string;
  selectedTab: number = 0;

  exTJobId: number;
  exTJobExec: ExternalTJobExecModel;

  serviceType: ServiceType;

  constructor(
    private titlesService: TitlesService,
    private externalService: ExternalService,
    private route: ActivatedRoute,
    private router: Router,
    private configurationService: ConfigurationService,
    private filesService: FilesService,
  ) {
    this.filesUrlPrefix = configurationService.configModel.host.replace('4200', '8091');
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params.subscribe((params: Params) => {
        this.exTestExecId = params.execId;
        this.exTJobId = params.tJobId;
      });
    }
  }

  ngOnInit(): void {
    this.titlesService.setHeadTitle('External Test Execution');
    this.titlesService.setPathName(this.router.routerState.snapshot.url);
    this.exTestExec = new ExternalTestExecutionModel();
    this.loadExternalTestExec();
  }

  loadExternalTestExec(): void {
    this.externalService.getExternalTestExecById(this.exTestExecId).subscribe(
      (exTestExec: ExternalTestExecutionModel) => {
        this.exTestExec = exTestExec;
        this.exTestCase = this.exTestExec.exTestCase;
        this.serviceType = this.exTestExec.getServiceType();

        this.exTJobExec = this.externalService.eTExternalModelsTransformService.jsonToExternalTJobExecModel(
          this.exTestExec.exTJobExec,
        );
        this.getExecutionFiles();
      },
      (error: Error) => console.log(error),
    );
  }

  getExecutionFiles(): void {
    this.externalService.getExternalTJobExecutionFiles(this.exTestExec.exTJobExec.id).subscribe(
      (tJobsExecFiles: any) => {
        let i: number = 0;
        tJobsExecFiles.forEach((file: FileModel) => {
          if (this.filesService.isVideoByFileModel(file)) {
            file['tabRef'] = this.miniLogAnalyzer.componentsTree.treeModel.nodes.length + 2 + i; // components and Logs + Files tabs
          }
        });
        this.files = this.exTestCase.setTestCaseFiles(tJobsExecFiles);
      },
      (error: Error) => console.log(error),
    );
  }

  getVideoFiles(): FileModel[] {
    let videoFiles: FileModel[] = [];
    videoFiles = this.files.filter((file: FileModel) => this.filesService.isVideoByFileModel(file));
    return videoFiles;
  }

  goToTab(num: number): void {
    this.selectedTab = num;
  }

  viewInLogAnalyzer(): void {
    this.router.navigate(['/loganalyzer'], {
      queryParams: {
        exTJob: this.exTJobId,
        exTJobExec: this.exTestExec.exTJobExec.id,
        exTestCase: this.exTestCase.name,
        exTestExec: this.exTestExec.id,
      },
    });
  }
}
