import { ConfigurationService } from '../../config/configuration-service.service';
import { FileModel } from './file-model';
import { TJobExecService } from '../tjob-exec/tjobExec.service';
import { Component, Input, OnInit } from '@angular/core';
import { Observable, Subscription } from 'rxjs/Rx';
import { TdDataTableService, TdDataTableSortingOrder, ITdDataTableSortChangeEvent, IPageChangeEvent } from '@covalent/core';
import { ExternalService } from '../external/external.service';
import { TJobExecModel } from '../tjob-exec/tjobExec-model';
import { ExternalTJobExecModel } from '../external/external-tjob-execution/external-tjob-execution-model';
import { ElastestEusDialog } from '../../elastest-eus/elastest-eus.dialog';
import { MdDialogRef } from '@angular/material';
import { ElastestEusDialogService } from '../../elastest-eus/elastest-eus.dialog.service';

@Component({
  selector: 'etm-files-manager',
  templateUrl: './files-manager.component.html',
  styleUrls: ['./files-manager.component.scss'],
})
export class FilesManagerComponent implements OnInit {
  @Input() tJobId: number;
  @Input() tJobExecId: number;
  @Input() tJobExecFinish: boolean = false;
  @Input() external: boolean = false;

  filesColumns: any[] = [
    { name: 'name', label: 'Name' },
    { name: 'serviceName', label: 'Service' },
    { name: 'options', label: 'Options' },
  ];

  executionFiles: FileModel[] = [];
  filteredExecutionFiles: FileModel[] = [];

  sortBy: string = 'serviceName';
  sortOrder: TdDataTableSortingOrder = TdDataTableSortingOrder.Ascending;

  timer: Observable<number>;
  subscription: Subscription;

  filesUrlPrefix: string;

  loading: boolean = true;

  constructor(
    private _dataTableService: TdDataTableService,
    private tJobExecService: TJobExecService,
    private externalService: ExternalService,
    private configurationService: ConfigurationService,
    private eusDialog: ElastestEusDialogService,
  ) {
    this.filesUrlPrefix = configurationService.configModel.host;
  }

  ngOnInit() {
    this.waitForExecutionFiles();
  }

  ngOnDestroy() {
    this.loading = false;
    this.endSubscription();
  }

  waitForExecutionFiles(): void {
    this.timer = Observable.interval(3500);
    if (this.subscription === undefined) {
      this.loading = true;
      console.log('Start polling for check tssInstance status');
      this.subscription = this.timer.subscribe(() => {
        this.loadExecutionFiles();
      });
    }
  }

  loadExecutionFiles(): void {
    if (!this.external) {
      this.tJobExecService.getTJobExecutionByTJobId(this.tJobId, this.tJobExecId).subscribe((tJobExecution: TJobExecModel) => {
        if (tJobExecution.finished() || tJobExecution.notExecuted()) {
          this.endSubscription();
        }
        this.tJobExecService.getTJobExecutionFiles(this.tJobId, this.tJobExecId).subscribe((tJobsExecFiles: any) => {
          this.prepareDataTable(tJobsExecFiles);
        });
      });
    } else {
      this.externalService.getExternalTJobExecById(this.tJobExecId).subscribe((exTJobExec: ExternalTJobExecModel) => {
        if (exTJobExec.finished() || exTJobExec.notExecuted()) {
          this.endSubscription();
        }
        this.externalService.getExternalTJobExecutionFiles(this.tJobExecId).subscribe((tJobsExecFiles: any) => {
          this.prepareDataTable(tJobsExecFiles);
        });
      });
    }
  }

  prepareDataTable(servicesInstances: FileModel[]): void {
    this.executionFiles = servicesInstances;
    this.filterFiles();
    this.loading = false;
  }

  endSubscription(): void {
    if (this.subscription !== undefined) {
      console.log('Stop polling to retrive files');
      this.subscription.unsubscribe();
      this.subscription = undefined;
    }
  }

  viewSession(url: string, title: string = 'Recorded Video'): void {
    let dialog: MdDialogRef<ElastestEusDialog> = this.eusDialog.getDialog(true);
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
}
