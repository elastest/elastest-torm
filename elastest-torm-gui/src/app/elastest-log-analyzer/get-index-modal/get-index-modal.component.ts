import { minDate, maxDate } from '../../elastest-log-manager/utils/Utils';
import { TJobService } from '../../elastest-etm/tjob/tjob.service';
import { ProjectService } from '../../elastest-etm/project/project.service';
import { TJobExecModel } from '../../elastest-etm/tjob-exec/tjobExec-model';
import { ProjectModel } from '../../elastest-etm/project/project-model';
import { Router } from '@angular/router';
import { TJobExecService } from '../../elastest-etm/tjob-exec/tjobExec.service';
import { TJobModel } from '../../elastest-etm/tjob/tjob-model';
import { Component, Inject, OnInit, Optional } from '@angular/core';
import { MD_DIALOG_DATA, MdDialogRef } from '@angular/material';
import { dateToInputLiteral } from '../utils/Utils';

@Component({
  selector: 'get-index-modal',
  templateUrl: './get-index-modal.component.html',
  styleUrls: ['./get-index-modal.component.scss']
})

export class GetIndexModalComponent implements OnInit {
  public projects: ProjectModel[] = [];
  public selectedProject: ProjectModel;

  public tJobs: TJobModel[] = [];
  public selectedTJob: TJobModel;

  public tJobExecs: TJobExecModel[] = [];
  public tJobExecsObj: {
    tJobExec: TJobExecModel,
    selected: boolean,
  }
  public selectedTJobExecs: Map<number, TJobExecModel> = new Map<number, TJobExecModel>();

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
    private projectService: ProjectService, private tJobService: TJobService, private tJobExecService: TJobExecService,
    private dialogRef: MdDialogRef<GetIndexModalComponent>,
    @Optional() @Inject(MD_DIALOG_DATA) public fromExec: any,
  ) {
    if (fromExec) {
      this.loadByGivenExec();
    } else {
      this.loadProjects();
    }
  }

  ngOnInit() { }

  loadProjects(): void {
    this.projectService.getProjects()
      .subscribe(
      (projectsList: ProjectModel[]) => this.projects = projectsList,
      (error) => this.dialogRef.close(),
    );
  }

  loadTJobs(project: ProjectModel): void {
    this.tJobs = project.tjobs;
  }

  loadTJobExecs(tJob: TJobModel): void {
    this.loadingExecs = true;
    this.tJobExecService.getTJobsExecutions(tJob).subscribe(
      (execs: TJobExecModel[]) => {
        this.tJobExecs = execs.reverse();
        this.loadingExecs = false;
      },
      (error: Error) => this.loadingExecs = false,
    );
  }

  initDefaultSelection(): void {
    if (this.tJobExecs.length > 0) {
      this.selectedTJob = this.tJobExecs[0].tJob;
      this.selectedProject = this.selectedTJob.project;
      this.checkTJobExec(true, this.tJobExecs[0]);
    }
  }

  selectAllExecs(checked: boolean): void {
    this.selectAll = checked;
    for (let exec of this.tJobExecs) {
      this.checkTJobExec(checked, exec);
    }
  }

  checkTJobExec(checked: boolean, tJobExec: TJobExecModel): void {
    let key: number = tJobExec.id;

    if (checked && !this.selectedTJobExecs.has(key)) {
      this.selectedTJobExecs.set(key, tJobExec);
    } else {
      this.selectedTJobExecs.delete(key);
    }

  }

  loadByGivenExec(): void {
    this.tJobExecService.getTJobExecutionByTJobId(this.fromExec.tJob, this.fromExec.exec)
      .subscribe(
      (tJobExec: TJobExecModel) => {
        this.checkTJobExec(true, tJobExec);
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

    this.selectedTJobExecs.forEach((tJobExec: TJobExecModel, key: number) => {
      selectedIndices.push(tJobExec.monitoringIndex);
      auxFromDate = minDate(auxFromDate, tJobExec.startDate);
      auxToDate = maxDate(auxToDate, tJobExec.endDate);
    });

    auxFromDate === fromDate ? fromDate = undefined : fromDate = auxFromDate;

    auxToDate === toDate ? toDate = undefined : toDate = auxToDate;

    let response: any = { selectedIndices: selectedIndices };
    response.fromDate = fromDate;
    response.toDate = toDate;
    this.dialogRef.close(response);
  }
}
