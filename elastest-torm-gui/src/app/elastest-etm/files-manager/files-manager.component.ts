import { ConfigurationService } from '../../config/configuration-service.service';
import { FileModel } from './file-model';
import { TJobExecService } from '../tjob-exec/tjobExec.service';
import { Component, Input, OnInit, OnDestroy } from '@angular/core';
import { Observable, Subscription } from 'rxjs/Rx';
import { TdDataTableService, TdDataTableSortingOrder } from '@covalent/core';
import { ExternalService } from '../external/external.service';
import { TJobExecModel } from '../tjob-exec/tjobExec-model';
import { ExternalTJobExecModel } from '../external/external-tjob-execution/external-tjob-execution-model';
import { ElastestEusDialog } from '../../elastest-eus/elastest-eus.dialog';
import { MatDialogRef } from '@angular/material';
import { ElastestEusDialogService } from '../../elastest-eus/elastest-eus.dialog.service';
import { AbstractTJobExecModel } from '../models/abstract-tjob-exec-model';
import { interval } from 'rxjs';
import { AbstractTJobModel } from '../models/abstract-tjob-model';

@Component({
  selector: 'etm-files-manager',
  templateUrl: './files-manager.component.html',
  styleUrls: ['./files-manager.component.scss'],
})
export class FilesManagerComponent implements OnInit, OnDestroy {
  @Input() tJobId: number;
  @Input() tJobExecId: number;

  @Input() tJob: AbstractTJobModel;
  @Input() tJobExec: AbstractTJobExecModel;

  @Input() tJobExecFinish: boolean = false;
  @Input() external: boolean = false;

  filesColumns: any[] = [
    { name: 'name', label: 'Name' },
    { name: 'folderName', label: 'Category' },
    { name: 'options', label: 'Options' },
  ];

  executionFiles: FileModel[] = [];
  filteredExecutionFiles: FileModel[] = [];

  sortBy: string = 'folderName';
  sortOrder: TdDataTableSortingOrder = TdDataTableSortingOrder.Ascending;

  timer: Observable<number>;
  subscription: Subscription;

  filesUrlPrefix: string;

  loading: boolean = true;
  finished: boolean = false;

  constructor(
    private _dataTableService: TdDataTableService,
    private tJobExecService: TJobExecService,
    private externalService: ExternalService,
    private configurationService: ConfigurationService,
    private eusDialog: ElastestEusDialogService,
  ) {
    this.filesUrlPrefix = configurationService.configModel.proxyHost;
  }

  ngOnInit(): void {
    if (!this.tJob && !this.tJobExec) {
      if (!this.external) {
        this.tJobExecService.getTJobExecutionByTJobId(this.tJobId, this.tJobExecId).subscribe((tJobExecution: TJobExecModel) => {
          this.manageIfFinished(tJobExecution);
          this.tJob = tJobExecution.tJob;
          this.tJobExec = tJobExecution;
          this.loadExecutionFiles();
        });
      } else {
        this.externalService.getExternalTJobExecById(this.tJobExecId).subscribe((exTJobExec: ExternalTJobExecModel) => {
          this.manageIfFinished(exTJobExec);
          this.tJob = exTJobExec.exTJob;
          this.tJobExec = exTJobExec;
          this.loadExecutionFiles();
        });
      }
    } else {
      this.manageIfFinished(this.tJobExec);
      this.loadExecutionFiles();
    }
  }

  ngOnDestroy(): void {
    this.loading = false;
    this.finished = true;
    this.endSubscription();
  }

  waitForExecutionFiles(): void {
    this.timer = interval(3500);
    if (this.subscription === undefined) {
      this.loading = true;
      console.log('Start polling for check tssInstance status');
      this.subscription = this.timer.subscribe(() => {
        this.loadExecutionFiles();
      });
    }
  }

  loadExecutionFiles(): void {
    // Is internal
    let getAbstractTJobExecutionFiles: Observable<any>;
    // Is external
    if (this.external) {
      getAbstractTJobExecutionFiles = this.externalService.getExternalTJobExecutionFiles(this.tJobExec.id);
    } else {
      // Is internal
      getAbstractTJobExecutionFiles = this.tJobExecService.getTJobExecutionFiles(this.tJob.id, this.tJobExec.id);
    }

    // Get files
    getAbstractTJobExecutionFiles.subscribe((tJobsExecFiles: any) => {
      this.prepareDataTable(tJobsExecFiles);
    });
  }

  manageIfFinished(exec: AbstractTJobExecModel): void {
    if (exec.finished() || exec.notExecuted()) {
      this.endSubscription();
      this.finished = true;
    }
  }

  prepareDataTable(files: FileModel[]): void {
    this.executionFiles = files;
    this.filterFiles();
    this.loading = false;
    if (!this.finished) {
      this.waitForExecutionFiles();
    }
  }

  endSubscription(): void {
    if (this.subscription !== undefined) {
      console.log('Stop polling to retrive files');
      this.subscription.unsubscribe();
      this.subscription = undefined;
    }
  }

  viewSession(file: FileModel, title: string = 'Recorded Video'): void {
    let url: string = this.filesUrlPrefix + file.encodedUrl;
    let dialog: MatDialogRef<ElastestEusDialog> = this.eusDialog.getDialog(true);
    dialog.componentInstance.title = title;
    dialog.componentInstance.iframeUrl = url;
    dialog.componentInstance.sessionType = 'video';
    dialog.componentInstance.closeButton = true;
  }

  isEusMetadata(name: string): boolean {
    return name.endsWith('.eus');
  }

  filterFiles(): void {
    if (this.executionFiles && this.executionFiles.length >= 0) {
      for (let file of this.executionFiles) {
        if (!this.isEusMetadata(file.name)) {
          this.filteredExecutionFiles.push(file);
        }
      }
    }
  }

  showNoFilesMessage(): boolean {
    return this.filteredExecutionFiles && this.filteredExecutionFiles.length === 0;
  }

  isVideo(name: string): boolean {
    return name.includes('.mp4') || name.includes('.webm') || name.includes('.avi');
  }
}
