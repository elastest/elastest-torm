import { FileModel } from './file-model';
import { TJobExecService } from '../tjob-exec/tjobExec.service';
import { Component, Input, OnInit, OnDestroy } from '@angular/core';
import { Observable, Subscription } from 'rxjs/Rx';
import { TdDataTableService, TdDataTableSortingOrder } from '@covalent/core';
import { ExternalService } from '../external/external.service';
import { TJobExecModel } from '../tjob-exec/tjobExec-model';
import { ExternalTJobExecModel } from '../external/external-tjob-execution/external-tjob-execution-model';
import { AbstractTJobExecModel } from '../models/abstract-tjob-exec-model';
import { interval } from 'rxjs';
import { AbstractTJobModel } from '../models/abstract-tjob-model';
import { FilesService } from '../../shared/services/files.service';

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

  eusSessionsNames: string[] = [];

  filesColumns: any[] = [
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

  executionFiles: FileModel[] = [];
  filteredExecutionFiles: FileModel[] = [];

  sortBy: string = 'folderName';
  sortOrder: TdDataTableSortingOrder = TdDataTableSortingOrder.Ascending;

  timer: Observable<number>;
  subscription: Subscription;

  loading: boolean = true;
  finished: boolean = false;

  constructor(
    private _dataTableService: TdDataTableService,
    private tJobExecService: TJobExecService,
    private externalService: ExternalService,
    private filesService: FilesService,
  ) {}

  ngOnInit(): void {
    if (!this.tJob && !this.tJobExec) {
      if (!this.external) {
        this.tJobExecService
          .getTJobExecutionByTJobId(this.tJobId, this.tJobExecId)
          .subscribe((tJobExecution: TJobExecModel) => {
            this.manageIfFinished(tJobExecution);
            this.tJob = tJobExecution.tJob;
            this.tJobExec = tJobExecution;
            this.loadExecutionFiles();
          });
      } else {
        this.externalService
          .getExternalTJobExecById(this.tJobExecId)
          .subscribe((exTJobExec: ExternalTJobExecModel) => {
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
      console.log(
        'Start polling for get execution ' + this.tJobExecId + ' files',
      );
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
      getAbstractTJobExecutionFiles = this.externalService.getExternalTJobExecutionFiles(
        this.tJobExec.id,
      );
    } else {
      // Is internal
      getAbstractTJobExecutionFiles = this.tJobExecService.getTJobExecutionFiles(
        this.tJob.id,
        this.tJobExec.id,
      );
    }

    // Get files
    getAbstractTJobExecutionFiles.subscribe((execFiles: FileModel[]) => {
      if (execFiles) {
        for (let execFile of execFiles) {
          if (execFile && execFile.isEusMetadataFile() && execFile.name) {
            this.eusSessionsNames.push(execFile.name.split('.')[0]);
          }
        }
      }

      this.prepareDataTable(execFiles);
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

  filterFiles(): void {
    if (this.executionFiles && this.executionFiles.length >= 0) {
      for (let file of this.executionFiles) {
        if (!file.isEusMetadataFile()) {
          this.filteredExecutionFiles.push(file);
        }
      }
    }
  }

  showNoFilesMessage(): boolean {
    return (
      this.filteredExecutionFiles && this.filteredExecutionFiles.length === 0
    );
  }
}
