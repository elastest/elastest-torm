import { TitlesService } from '../../../shared/services/titles.service';
import { TJobExecModel } from '../../tjob-exec/tjobExec-model';
import { TJobExecService } from '../../tjob-exec/tjobExec.service';
import { TJobService } from '../tjob.service';
import { Component, OnInit, ViewContainerRef, Input } from '@angular/core';
import {
  TdDialogService,
  IConfirmConfig,
  TdDataTableSortingOrder,
  ITdDataTableSortChangeEvent,
  TdDataTableService,
  ITdDataTableColumn,
} from '@covalent/core';
import { MatDialog } from '@angular/material';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { TJobModel } from '../tjob-model';
import { ProjectModel } from '../../project/project-model';
import { ProjectService } from '../../project/project.service';
import { RunTJobModalComponent } from '../run-tjob-modal/run-tjob-modal.component';
import { ETModelsTransformServices } from '../../../shared/services/et-models-transform.service';

@Component({
  selector: 'etm-tjobs-manager',
  templateUrl: './tjobs-manager.component.html',
  styleUrls: ['./tjobs-manager.component.scss'],
})
export class TJobsManagerComponent implements OnInit {
  @Input()
  projectId: string;

  @Input()
  project: ProjectModel;

  tJobs: TJobModel[] = [];
  showSpinner: boolean = true;

  deletingInProgress: boolean = false;
  duplicateInProgress: boolean = false;

  // TJob Data
  tjobColumns: ITdDataTableColumn[] = [
    { name: 'id', label: 'Id', width: 80 },
    { name: 'result', label: 'Result', width: 74, sortable: false },
    { name: 'name', label: 'Name' },
    { name: 'imageName', label: 'Image Name' },
    { name: 'lastExecutionDate', label: 'Last Execution' },
    { name: 'sut', label: 'Sut', width: 80 },
    { name: 'multi', label: 'Multi Axis', width: 100 },
    { name: 'options', label: 'Options', sortable: false },
  ];

  sortBy: string = 'id';
  sortOrder: TdDataTableSortingOrder = TdDataTableSortingOrder.Ascending;

  constructor(
    private titlesService: TitlesService,
    private tJobService: TJobService,
    private tJobExecService: TJobExecService,
    private _dialogService: TdDialogService,
    private _viewContainerRef: ViewContainerRef,
    public dialog: MatDialog,
    private router: Router,
    private route: ActivatedRoute,
    private projectService: ProjectService,
    private eTModelsTransformServices: ETModelsTransformServices,
    private dataTableService: TdDataTableService,
  ) {}

  ngOnInit(): void {
    this.init();
  }

  init(forceLoadProject: boolean = false): void {
    // If child
    if (this.project) {
      this.initDataFromProject(forceLoadProject);
    } else if (this.projectId) {
      this.loadProjectAndTJobs(this.projectId);
    } else if (this.route.params !== null || this.route.params !== undefined) {
      // If routing
      this.route.params.subscribe((params: Params) => {
        if (params['projectId']) {
          this.loadProjectAndTJobs(params['projectId']);
        } else {
          // Get all TJobs
          this.loadAllTJobs();
        }
      });
    } else {
      this.loadAllTJobs();
    }
  }

  loadProjectAndTJobs(projectId: string | number): void {
    this.projectService.getProject(projectId, 'medium').subscribe((project: ProjectModel) => {
      this.project = project;
      this.initDataFromProject();
      this.duplicateInProgress = false;
    });
  }

  initDataFromProject(forceLoadProject: boolean = false): void {
    if (forceLoadProject) {
      this.loadProjectAndTJobs(this.project.id);
    }
    if (this.project) {
      this.tJobs = this.project.tjobs;
      this.showSpinner = false;
      this.setLastTJobExecForEachTJob();
    }
  }

  loadAllTJobs(): void {
    this.tJobService.getTJobs().subscribe(
      (tJobs: TJobModel[]) => {
        this.tJobs = tJobs;
        this.duplicateInProgress = false;
      },
      (error: Error) => {
        this.duplicateInProgress = false;
        console.log(error);
      },
    );
  }

  newTJob(): void {
    this.router.navigate(['/projects', this.project.id, 'tjob', 'new']);
  }

  runTJob(tJob: TJobModel, project: ProjectModel): void {
    if (tJob.hasParameters()) {
      tJob.project = project;
      this.dialog.open(RunTJobModalComponent, {
        data: tJob.cloneTJob(),
        height: '85%',
        width: '65%',
      });
    } else {
      this.tJobExecService.runTJob(tJob.id, undefined, undefined, tJob.multiConfigurations).subscribe(
        (tjobExecution: TJobExecModel) => {
          this.router.navigate(['/projects', this.project.id, 'tjob', tJob.id, 'tjob-exec', tjobExecution.id]);
        },
        (error: Error) => console.error('Error:' + error),
      );
    }
  }

  editTJob(tJob: TJobModel): void {
    if (tJob.external && tJob.getExternalEditPage()) {
      window.open(tJob.getExternalEditPage());
    } else {
      this.router.navigate(['/projects', this.project.id, 'tjob', 'edit', tJob.id]);
    }
  }

  deleteTJob(tJob: TJobModel): void {
    let iConfirmConfig: IConfirmConfig = {
      message: 'TJob ' + tJob.id + ' will be deleted with all TJob Executions, do you want to continue?',
      disableClose: false,
      viewContainerRef: this._viewContainerRef,
      title: 'Confirm',
      cancelButton: 'Cancel',
      acceptButton: 'Yes, delete',
    };
    this._dialogService
      .openConfirm(iConfirmConfig)
      .afterClosed()
      .subscribe((accept: boolean) => {
        if (accept) {
          this.deletingInProgress = true;
          this.tJobService.deleteTJob(tJob).subscribe(
            (tJob) => {
              this.deletingInProgress = false;
              this.init(true);
            },
            (error: Error) => {
              this.deletingInProgress = false;
              console.log(error);
            },
          );
        }
      });
  }

  viewTJob(tJob: TJobModel): void {
    this.router.navigate(['/projects', this.project.id, 'tjob', tJob.id]);
  }

  setLastTJobExecForEachTJob(): void {
    let lastExecution: TJobExecModel = new TJobExecModel();
    for (let tjob of this.tJobs) {
      this.tJobExecService.getLastNTJobExecutions(tjob.id, 1, true).subscribe((lastExecs: TJobExecModel[]) => {
        let lastExec: TJobExecModel = lastExecs ? lastExecs[0] : undefined;
        if (lastExec !== undefined) {
          lastExecution = this.eTModelsTransformServices.jsonToTJobExecModel(lastExec, true);
          tjob['lastExecutionDate'] = lastExecution.endDate ? lastExecution.endDate : lastExecution.startDate;
          tjob['result'] = lastExecution.getResultIcon();
        } else {
          lastExecution = new TJobExecModel();
          lastExecution.result = 'NOT_EXECUTED';
          tjob['lastExecutionDate'] = undefined;
          tjob['result'] = lastExecution.getResultIcon();
        }
      });
    }
  }

  duplicateTJob(tJob: TJobModel): void {
    this.duplicateInProgress = true;
    if (!tJob.project) {
      let project: ProjectModel = new ProjectModel();
      project.id = this.project.id;
      tJob.project = project;
    }
    this.tJobService.duplicateTJob(tJob).subscribe(
      (tJob: any) => {
        this.init(true);
        this.duplicateInProgress = false;
      },
      (error: Error) => {
        console.log(error);
        this.duplicateInProgress = false;
      },
    );
  }

  sort(sortEvent: ITdDataTableSortChangeEvent): void {
    this.sortBy = sortEvent.name;
    this.sortOrder = sortEvent.order;
    this.tJobs = this.dataTableService.sortData(this.tJobs, this.sortBy, this.sortOrder);
  }
}
