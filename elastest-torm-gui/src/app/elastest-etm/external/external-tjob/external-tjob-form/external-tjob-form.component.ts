import { ExternalTJobModel } from '../external-tjob-model';
import { TitlesService } from '../../../../shared/services/titles.service';
import { ExternalService } from '../../external.service';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';
import { SutModel } from '../../../sut/sut-model';

@Component({
  selector: 'etm-external-tjob-form',
  templateUrl: './external-tjob-form.component.html',
  styleUrls: ['./external-tjob-form.component.scss'],
})
export class ExternalTjobFormComponent implements OnInit {
  exTJob: ExternalTJobModel;
  currentPath: string = '';
  currentSut: string = 'None';
  sutEmpty: SutModel = new SutModel();

  constructor(private externalService: ExternalService, private titlesService: TitlesService, private route: ActivatedRoute) {}

  ngOnInit() {
    this.exTJob = new ExternalTJobModel();
    if (this.route.params !== null || this.route.params !== undefined) {
      // If routing
      this.currentPath = this.route.snapshot.url[0].path;
      this.route.params.subscribe((params: Params) => {
        if (this.currentPath === 'edit') {
          if (params['tJobId']) {
            this.externalService.getExternalTJobById(params['tJobId']).subscribe(
              (exTJob: ExternalTJobModel) => {
                this.exTJob = exTJob;
                this.currentSut = exTJob.sut.id > 0 ? exTJob.sut.name : 'None';
              },
              (error) => console.log(error),
            );
          }
        }
      });
    }
  }

  save(): void {
    this.externalService
      .modifyExternalTJob(this.exTJob)
      .subscribe((exTJob: ExternalTJobModel) => this.postSave(exTJob), (error) => console.log(error));
  }
  postSave(exTJob: any): void {
    this.exTJob = exTJob;
    window.history.back();
  }
}
