import { TJobExecModel } from '../../tjob-exec/tjobExec-model';
import { Router } from '@angular/router';
import { TJobExecService } from '../../tjob-exec/tjobExec.service';
import { TJobService } from '../tjob.service';
import { TJobModel } from '../tjob-model';
import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material';
import { ParameterModel } from '../../parameter/parameter-model';

@Component({
  selector: 'run-tjob-modal',
  templateUrl: './run-tjob-modal.component.html',
  styleUrls: ['./run-tjob-modal.component.scss'],
})
export class RunTJobModalComponent implements OnInit {
  initializing: boolean = true;

  initialTJobParameters: ParameterModel[];
  initialSutParameters: ParameterModel[];

  constructor(
    private tJobService: TJobService,
    private tJobExecService: TJobExecService,
    private router: Router,
    @Inject(MAT_DIALOG_DATA) public tJob: TJobModel,
  ) {}

  ngOnInit(): void {
    this.tJobService.getTJob(this.tJob.id + '').subscribe(
      (tJob: TJobModel) => {
        this.tJob = tJob;
        this.initialTJobParameters = this.tJob ? [...this.tJob.parameters] : [];
        this.initialSutParameters = this.hasSut() ? [...this.tJob.sut.parameters] : [];
        this.initializing = false;
      },
      (error: Error) => console.log(error),
    );
  }

  runTJob(): void {
    this.tJobExecService
      .runTJob(
        this.tJob.id,
        this.tJob.parameters,
        this.hasSut() ? this.tJob.sut.parameters : undefined,
        this.tJob.multiConfigurations,
      )
      .subscribe(
        (tjobExecution: TJobExecModel) => {
          this.router.navigate(['/projects', this.tJob.project.id, 'tjob', this.tJob.id, 'tjob-exec', tjobExecution.id]);
        },
        (error: Error) => console.error('Error:' + error),
      );
  }

  hasSut(): boolean {
    return this.tJob.sut !== undefined && this.tJob.sut !== null && this.tJob.sut.id !== 0;
  }
}
