import { SutModel } from '../../sut/sut-model';
import { TJobExecModel } from '../../tjob-exec/tjobExec-model';
import { TJobExecService } from '../../tjob-exec/tjobExec.service';
import { TJobModel } from '../tjob-model';
import { TJobService } from '../tjob.service';

import { Component, OnInit, ViewContainerRef } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { IConfirmConfig } from '@covalent/core';
import { TdDialogService } from '@covalent/core/dialogs/services/dialog.service';

@Component({
  selector: 'app-tjob-manager',
  templateUrl: './tjob-manager.component.html',
  styleUrls: ['./tjob-manager.component.scss']
})
export class TjobManagerComponent implements OnInit {
  tJob: TJobModel;
  editMode: boolean = false;

  sutEmpty: SutModel = new SutModel();

  tjobColumns: any[] = [
    { name: 'id', label: 'Id' },
    { name: 'name', label: 'Name' },
    { name: 'imageName', label: 'Image Name' },
    { name: 'sut.id', label: 'Sut' },
  ];

  // TJob Exec Data
  tJobExecColumns: any[] = [
    { name: 'id', label: 'Id' },
    { name: 'result', label: 'Result' },
    { name: 'duration', label: 'Duration' },
    { name: 'sutExecution', label: 'Sut Execution' },
    { name: 'error', label: 'Error' },
    { name: 'logs', label: 'Logs Url' },
    { name: 'options', label: 'Options' },
  ];
  tJobExecData: TJobExecModel[] = [];

  constructor(private tJobService: TJobService, private tJobExecService: TJobExecService,
    private route: ActivatedRoute, private router: Router,
    private _dialogService: TdDialogService, private _viewContainerRef: ViewContainerRef, ) { }

  ngOnInit() {
    this.tJob = new TJobModel();
    this.reloadTJob();
  }

  reloadTJob() {
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params.switchMap((params: Params) => this.tJobService.getTJob(params['tJobId']))
        .subscribe((tJob: TJobModel) => {
          this.tJob = tJob;
          if (this.tJob.sut.id === 0) {
            this.tJob.sut = this.sutEmpty;
          }
          this.tJobExecData = this.tJobExecService.transformTJobExecDataToDataTable(tJob.tjobExecs);
          this.sortTJobsExec(); //Id desc
        });
    }
  }

  sortTJobsExec() {
    this.tJobExecData = this.tJobExecData.reverse();
  }

  deleteTJobExec(tJobExec: TJobExecModel) {
    let iConfirmConfig: IConfirmConfig = {
      message: 'TJob Execution ' + tJobExec.id + ' will be deleted, do you want to continue?',
      disableClose: false,
      viewContainerRef: this._viewContainerRef,
      title: 'Confirm',
      cancelButton: 'Cancel',
      acceptButton: 'Yes, delete',
    };
    this._dialogService.openConfirm(iConfirmConfig).afterClosed().subscribe((accept: boolean) => {
      if (accept) {
        this.tJobExecService.deleteTJobExecution(this.tJob, tJobExec).subscribe(
          (tJob) => this.reloadTJob(),
          (error) => console.log(error)
        );
      }
    });
  }

  viewTJobExec(tJobExec: TJobExecModel) {
    this.router.navigate(['/projects', this.tJob.project.id, 'tjob', this.tJob.id, 'tjob-exec', tJobExec.id]);
  }

}