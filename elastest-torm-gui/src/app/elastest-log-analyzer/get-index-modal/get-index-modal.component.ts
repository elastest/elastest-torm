import { minDate } from '../../elastest-log-manager/utils/Utils';
import { TJobService } from '../../elastest-etm/tjob/tjob.service';
import { ProjectService } from '../../elastest-etm/project/project.service';
import { TJobExecModel } from '../../elastest-etm/tjob-exec/tjobExec-model';
import { ProjectModel } from '../../elastest-etm/project/project-model';
import { ElasticSearchService } from '../../shared/services/elasticsearch.service';
import { Router } from '@angular/router';
import { TJobExecService } from '../../elastest-etm/tjob-exec/tjobExec.service';
import { TJobModel } from '../../elastest-etm/tjob/tjob-model';
import { Component, Inject, OnInit } from '@angular/core';
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

  // TJob Exec Data
  tJobExecColumns: any[] = [
    { name: 'select', label: 'Select' },
    { name: 'id', label: 'Id' },
    { name: 'startDate', label: 'Start Date' },
    { name: 'result', label: 'Result' },
  ];

  constructor(
    // @Inject(MD_DIALOG_DATA) public tJob: TJobModel,
    private projectService: ProjectService, private tJobService: TJobService, private tJobExecService: TJobExecService,
    private router: Router, public elasticSearchService: ElasticSearchService,
    private dialogRef: MdDialogRef<GetIndexModalComponent>,
  ) {
    this.loadProjects();
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
    this.tJobExecService.getTJobsExecutions(tJob).subscribe(
      (execs: TJobExecModel[]) => {
        this.tJobExecs = execs.reverse();
      },
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

  // On press OK
  saveIndices(): void {
    let selectedIndices: string[] = [];
    let fromDate: Date = new Date();
    let auxDate: Date = fromDate;
    this.selectedTJobExecs.forEach((tJobExec: TJobExecModel, key: number) => {
      selectedIndices.push(tJobExec.logIndex);
      auxDate = minDate(auxDate, tJobExec.startDate);
    });

    auxDate === fromDate ? fromDate = undefined : fromDate = auxDate;

    let response: any = { selectedIndices: selectedIndices };
    response.fromDate = fromDate;
    this.dialogRef.close(response);
  }
}
