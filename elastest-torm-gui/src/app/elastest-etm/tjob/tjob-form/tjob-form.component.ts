import { SutModel } from '../../sut/sut-model';
import { TJobModel } from '../tjob-model';
import { TJobService } from '../tjob.service';

import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';

@Component({
  selector: 'etm-tjob-form',
  templateUrl: './tjob-form.component.html',
  styleUrls: ['./tjob-form.component.scss']
})
export class TJobFormComponent implements OnInit {

  tJob: TJobModel;
  editMode: boolean = false;

  sutEmpty: SutModel = new SutModel();


  constructor(private tJobService: TJobService, private route: ActivatedRoute, ) { }

  ngOnInit() {
    this.tJob = new TJobModel();
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params.switchMap((params: Params) => this.tJobService.getTJob(params['tJobId']))
        .subscribe((tJob: TJobModel) => {
          this.tJob = tJob;
          if (this.tJob.sut.id === 0) {
            this.tJob.sut = this.sutEmpty;
          }
        });
    }

  }

  goBack(): void {
    window.history.back();
  }

  save() {
    this.tJobService.createTJob(this.tJob)
      .subscribe(
      tJob => this.postSave(tJob),
      error => console.log(error)
      );

  }

  postSave(tJob: any) {
    this.tJob = tJob;
    window.history.back();
  }

  cancel() {
    window.history.back();
  }
}
