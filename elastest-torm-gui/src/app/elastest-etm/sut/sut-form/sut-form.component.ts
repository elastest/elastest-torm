import { ProjectModel } from '../../project/project-model';
import { ProjectService } from '../../project/project.service';
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
  currentPath: string = '';

  imageNameChecked: boolean = true;
  repoNameChecked: boolean = false;

  constructor(private sutService: SutService, private route: ActivatedRoute,
    private projectService: ProjectService) { }

  ngOnInit() {
    this.sut = new SutModel();
    this.currentPath = this.route.snapshot.url[0].path;
    if (this.route.params !== null || this.route.params !== undefined) {
      if (this.currentPath === 'edit') {
        this.route.params.switchMap((params: Params) => this.sutService.getSut(params['sutId']))
          .subscribe((sut: SutModel) => {
            this.sut = sut;
            this.imageNameChecked = sut.imageName !== '' && sut.specification === '';
            this.repoNameChecked = sut.imageName === '' && sut.specification !== '';
          });
      }
      else if (this.currentPath === 'new') {
        this.route.params.switchMap((params: Params) => this.projectService.getProject(params['projectId']))
          .subscribe(
          (project: ProjectModel) => {
            this.sut = new SutModel();
            this.sut.project = project;
          },
        );
      }
    }
  }
  sutBy(selected: string) {
    if (selected === 'imageName') {
      this.sut.specification = '';
      this.imageNameChecked = true;
      this.repoNameChecked = false;
    }
    else {
      this.sut.imageName = '';
      this.imageNameChecked = false;
      this.repoNameChecked = true;
    }
  }

  goBack(): void {
    window.history.back();
  }

  save() {
    console.log(this.sut);
    this.sutService.createSut(this.sut)
      .subscribe(
      (sut) => this.postSave(sut),
      (error) => console.log(error)
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
