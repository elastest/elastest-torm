import { minDate, maxDate } from '../../elastest-log-manager/utils/Utils';
import { TJobService } from '../../elastest-etm/tjob/tjob.service';
import { ProjectService } from '../../elastest-etm/project/project.service';
import { TJobExecModel } from '../../elastest-etm/tjob-exec/tjobExec-model';
import { Router } from '@angular/router';
import { TJobExecService } from '../../elastest-etm/tjob-exec/tjobExec.service';
import { Component, Inject, OnInit, Optional } from '@angular/core';
import { MD_DIALOG_DATA, MdDialogRef } from '@angular/material';
import { dateToInputLiteral } from '../utils/Utils';
import { ExternalService } from '../../elastest-etm/external/external.service';
import { ExternalTJobExecModel } from '../../elastest-etm/external/external-tjob-execution/external-tjob-execution-model';
import { AbstractTJobExecModel } from '../../elastest-etm/models/abstract-tjob-exec-model';
import { AbstractProjectModel } from '../../elastest-etm/models/abstract-project-model';
import { AbstractTJobModel } from '../../elastest-etm/models/abstract-tjob-model';
import { TJobModel } from '../../elastest-etm/tjob/tjob-model';
import { ProjectModel } from '../../elastest-etm/project/project-model';
import { ExternalProjectModel } from '../../elastest-etm/external/external-project/external-project-model';
import { ExternalTJobModel } from '../../elastest-etm/external/external-tjob/external-tjob-model';

@Component({
  selector: 'get-index-modal',
  templateUrl: './get-index-modal.component.html',
  styleUrls: ['./get-index-modal.component.scss'],
})
export class GetIndexModalComponent implements OnInit {
  public abstractProjects: AbstractProjectModel[] = [];
  public selectedProject: AbstractProjectModel;

  public abstractTJobs: AbstractTJobModel[] = [];
  public selectedTJob: AbstractTJobModel;

  public abstractTJobExecs: AbstractTJobExecModel[] = [];
  public tJobExecsObj: {
    tJobExec: TJobExecModel;
    selected: boolean;
  };

  internalSelected: boolean = true;
  externalSelected: boolean = false;

  public selectedAbstractTJobExecs: Map<number, AbstractTJobExecModel> = new Map<number, AbstractTJobExecModel>();

  public selectAll: boolean = false;

  public loadingExecs: boolean = false;

  // TJob Exec Data
  tJobExecColumns: any[] = [
    { name: 'select', label: 'Select' },
    { name: 'id', label: 'Id' },
    { name: 'startDate', label: 'Start Date' },
    { name: 'endDate', label: 'End Date' },
    { name: 'result', label: 'Result' },
  ];

  constructor(
    private projectService: ProjectService,
    private tJobService: TJobService,
    private tJobExecService: TJobExecService,
    private externalService: ExternalService,
    private dialogRef: MdDialogRef<GetIndexModalComponent>,
    @Optional()
    @Inject(MD_DIALOG_DATA)
    public fromExec: any,
  ) {
    this.init();
    if (fromExec) {
      if (fromExec.type === 'normal') {
        this.loadByGivenExec();
      } else if (fromExec.type === 'external') {
        this.loadByGivenExternalExec();
      }
    } else {
      this.loadProjects();
    }
  }

  ngOnInit() {}

  init(): void {
    this.abstractProjects = [];
    this.selectedProject = undefined;

    this.abstractTJobs = [];
    this.selectedTJob = undefined;

    this.abstractTJobExecs = [];

    this.selectedAbstractTJobExecs = new Map<number, AbstractTJobExecModel>();
    this.selectAll = false;
    this.loadingExecs = false;
  }

  changeType(checked: 'internal' | 'external'): void {
    if (checked === 'internal') {
      this.internalSelected = true;
      this.externalSelected = false;
    } else {
      this.internalSelected = false;
      this.externalSelected = true;
    }
    this.init();
    this.loadProjects();
  }

  loadProjects(): void {
    if (this.internalSelected) {
      this.projectService
        .getProjects()
        .subscribe((projectsList: ProjectModel[]) => (this.abstractProjects = projectsList), (error) => this.dialogRef.close());
    } else {
      this.externalService
        .getAllExternalProjects()
        .subscribe(
          (projectsList: ExternalProjectModel[]) => (this.abstractProjects = projectsList),
          (error) => this.dialogRef.close(),
        );
    }
  }

  loadTJobs(project: AbstractProjectModel): void {
    if (project instanceof ProjectModel) {
      this.abstractTJobs = project.tjobs;
    } else if (project instanceof ExternalProjectModel) {
      this.abstractTJobs = project.exTJobs;
    }
  }

  loadTJobExecs(abstractTJob: AbstractTJobModel): void {
    this.loadingExecs = true;
    if (this.internalSelected) {
      if (abstractTJob instanceof TJobModel) {
        this.tJobExecService.getTJobsExecutions(abstractTJob).subscribe((execs: TJobExecModel[]) => {
          this.abstractTJobExecs = execs.reverse();
          this.loadingExecs = false;
        }, (error: Error) => (this.loadingExecs = false));
      }
    } else {
      if (abstractTJob instanceof ExternalTJobModel) {
        this.externalService.getExternalTJobExecsByExternalTJobId(abstractTJob.id).subscribe((execs: ExternalTJobExecModel[]) => {
          this.abstractTJobExecs = execs.reverse();
          this.loadingExecs = false;
        }, (error: Error) => (this.loadingExecs = false));
      }
    }
  }

  initDefaultSelection(): void {
    if (this.abstractTJobExecs.length > 0) {
      this.selectedTJob = this.abstractTJobExecs[0].tJob;
      this.selectedProject = this.selectedTJob.project;
      this.checkAbstractTJobExec(true, this.abstractTJobExecs[0]);
    }
  }

  selectAllExecs(checked: boolean): void {
    this.selectAll = checked;
    for (let exec of this.abstractTJobExecs) {
      this.checkAbstractTJobExec(checked, exec);
    }
  }

  checkAbstractTJobExec(checked: boolean, abstractTJobExec: AbstractTJobExecModel): void {
    let key: number = abstractTJobExec.id;

    if (checked && !this.selectedAbstractTJobExecs.has(key)) {
      this.selectedAbstractTJobExecs.set(key, abstractTJobExec);
    } else {
      this.selectedAbstractTJobExecs.delete(key);
    }
  }

  loadByGivenExec(): void {
    this.changeType('internal');

    this.tJobExecService.getTJobExecutionByTJobId(this.fromExec.tJob, this.fromExec.exec).subscribe(
      (tJobExec: TJobExecModel) => {
        this.checkAbstractTJobExec(true, tJobExec);
        this.saveIndices();
      },
      (error: Error) => this.loadProjects(),
    );
  }

  loadByGivenExternalExec(): void {
    this.changeType('external');
    this.externalService.getExternalTJobExecById(this.fromExec.exTJobExec).subscribe(
      (exTJobExec: ExternalTJobExecModel) => {
        this.checkAbstractTJobExec(true, exTJobExec);
        this.saveIndices();
      },
      (error: Error) => this.loadProjects(),
    );
  }

  // On press OK
  saveIndices(): void {
    let selectedIndices: string[] = [];
    let fromDate: Date = new Date();
    let auxFromDate: Date = fromDate;

    let toDate: Date = new Date();
    let auxToDate: Date = undefined;

    this.selectedAbstractTJobExecs.forEach((currentExec: AbstractTJobExecModel, key: number) => {
      selectedIndices.push(currentExec.monitoringIndex);
      auxFromDate = minDate(auxFromDate, currentExec.startDate);
      auxToDate = maxDate(auxToDate, currentExec.endDate);
    });

    auxFromDate === fromDate ? (fromDate = undefined) : (fromDate = auxFromDate);

    auxToDate === toDate ? (toDate = undefined) : (toDate = auxToDate);

    let response: any = { selectedIndices: selectedIndices };
    response.fromDate = fromDate;
    response.toDate = toDate;
    this.dialogRef.close(response);
  }
}
