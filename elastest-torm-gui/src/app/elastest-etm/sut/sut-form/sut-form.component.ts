import { SutModel } from '../sut-model';
import { SutService } from '../sut.service';

import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';

@Component({
  selector: 'etm-sut-form',
  templateUrl: './sut-form.component.html',
  styleUrls: ['./sut-form.component.scss']
})
export class SutFormComponent implements OnInit {

  sut: SutModel;
  editMode: boolean = false;

  constructor(private sutService: SutService, private route: ActivatedRoute, ) { }

  ngOnInit() {
    this.sut = new SutModel();
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params.switchMap((params: Params) => this.sutService.getSut(params['sutId']))
        .subscribe((sut: SutModel) => {
          this.sut = sut;
        });
    }

  }

  goBack(): void {
    window.history.back();
  }

  save() {
    console.log(this.sut);
    this.sutService.createSut(this.sut)
      .subscribe(
      sut => this.postSave(sut),
      error => console.log(error)
      );

  }

  postSave(sut: any) {
    this.sut = sut;
    window.history.back();
  }

  cancel() {
    window.history.back();
  }
}
