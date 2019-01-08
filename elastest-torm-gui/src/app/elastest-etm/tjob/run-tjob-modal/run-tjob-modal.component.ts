import { TJobExecModel } from '../../tjob-exec/tjobExec-model';
import { Router } from '@angular/router';
import { TJobExecService } from '../../tjob-exec/tjobExec.service';
import { TJobService } from '../tjob.service';
import { TJobModel } from '../tjob-model';
import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material';

@Component({
  selector: 'run-tjob-modal',
  templateUrl: './run-tjob-modal.component.html',
  styleUrls: ['./run-tjob-modal.component.scss'],
})
export class RunTJobModalComponent implements OnInit {
  initializing: boolean = true;
  constructor(
    private tJobService: TJobService,
    private tJobExecService: TJobExecService,
    private router: Router,
    @Inject(MAT_DIALOG_DATA) public tJob: TJobModel,
  ) {}

  ngOnInit() {
    this.tJobService.getTJob(this.tJob.id + '').subscribe(
      (tJob: TJobModel) => {
        this.tJob = tJob;
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
        (error) => console.error('Error:' + error),
      );
  }

  hasSut(): boolean {
    return this.tJob.sut !== undefined && this.tJob.sut !== null && this.tJob.sut.id !== 0;
  }
}
