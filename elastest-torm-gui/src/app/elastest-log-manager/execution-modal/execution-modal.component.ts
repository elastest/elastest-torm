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

@Component({
  selector: 'app-execution-modal',
  templateUrl: './execution-modal.component.html',
  styleUrls: ['./execution-modal.component.scss']
})
export class ExecutionModalComponent implements OnInit {

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
    private dialogRef: MdDialogRef<ExecutionModalComponent>,
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
      (execs: TJobExecModel[]) => this.tJobExecs = execs.reverse(),
    );
  }

  saveIndices(): void {
    let selectedIndices: string = '';
    let separator: string = '';

    this.selectedTJobExecs.forEach((tJobExec: TJobExecModel, key: number) => {
      selectedIndices = selectedIndices + separator + tJobExec.monitoringIndex;
      separator = ',';
    });

    this.dialogRef.close({ selectedIndices: selectedIndices });
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
}
