import { TitlesService } from '../../../shared/services/titles.service';
import { TJobExecModel } from '../../tjob-exec/tjobExec-model';
import { TJobExecService } from '../../tjob-exec/tjobExec.service';
import { TJobService } from '../tjob.service';
import { Component, OnInit, ViewContainerRef, Input } from '@angular/core';
import { TdDialogService, IConfirmConfig } from '@covalent/core';
import { MdDialog } from '@angular/material';
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

  project: ProjectModel;
  tJobs: TJobModel[] = [];
  showSpinner: boolean = true;

  deletingInProgress: boolean = false;
  duplicateInProgress: boolean = false;

  // TJob Data
  tjobColumns: any[] = [
    { name: 'id', label: 'Id' },
    { name: 'name', label: 'Name' },
    { name: 'imageName', label: 'Image Name' },
    { name: 'lastExecutionDate', label: 'Last Execution' },
    { name: 'result', label: 'Result' },
    { name: 'sut', label: 'Sut' },
    { name: 'multi', label: 'Multi Axis' },
    { name: 'options', label: 'Options' },
  ];

  constructor(
    private titlesService: TitlesService,
    private tJobService: TJobService,
    private tJobExecService: TJobExecService,
    private _dialogService: TdDialogService,
    private _viewContainerRef: ViewContainerRef,
    public dialog: MdDialog,
    private router: Router,
    private route: ActivatedRoute,
    private projectService: ProjectService,
    private eTModelsTransformServices: ETModelsTransformServices,
  ) {}

  ngOnInit() {
    this.init();
  }

  init(): void {
    // If child
    if (this.projectId) {
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

  loadProjectAndTJobs(projectId: string): void {
    this.projectService.getProject(projectId).subscribe((project: ProjectModel) => {
      this.project = project;
      if (project) {
        this.tJobs = this.project.tjobs;
        this.showSpinner = false;
        this.addLastTJob();
      }
      this.duplicateInProgress = false;
    });
  }

  loadAllTJobs(): void {
    this.tJobService.getTJobs().subscribe(
      (tJobs: TJobModel[]) => {
        this.tJobs = tJobs;
        this.duplicateInProgress = false;
      },
      (error) => {
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
        (error) => console.error('Error:' + error),
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
              this.init();
            },
            (error) => {
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

  addLastTJob(): void {
    let lastExecution: TJobExecModel = new TJobExecModel();
    for (let tjob of this.tJobs) {
      if (tjob.getLastExecution() !== undefined) {
        lastExecution = this.eTModelsTransformServices.jsonToTJobExecModel(tjob.getLastExecution(), true);
        tjob['lastExecutionDate'] = lastExecution.endDate ? lastExecution.endDate : lastExecution.startDate;
        tjob['result'] = lastExecution.getResultIcon();
      } else {
        lastExecution = new TJobExecModel();
        lastExecution.result = 'NOT_EXECUTED';
        tjob['lastExecutionDate'] = undefined;
        tjob['result'] = lastExecution.getResultIcon();
      }
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
        this.init();
      },
      (error: Error) => {
        console.log(error);
        this.duplicateInProgress = false;
      },
    );
  }
}
