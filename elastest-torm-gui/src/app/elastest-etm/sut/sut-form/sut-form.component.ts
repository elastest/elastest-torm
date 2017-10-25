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

  managedChecked: boolean = true;
  repoNameChecked: boolean = false;
  deployedChecked: boolean = false;

  specText: string = 'SuT Specification';
  deployedSpecText: string = 'SuT IP';
  managedSpecText: string = 'Docker Image';


  constructor(private sutService: SutService, private route: ActivatedRoute,
    private projectService: ProjectService,
  ) { }

  ngOnInit() {
    this.sut = new SutModel();
    this.currentPath = this.route.snapshot.url[0].path;
    if (this.route.params !== null || this.route.params !== undefined) {
      if (this.currentPath === 'edit') {
        this.route.params.switchMap((params: Params) => this.sutService.getSut(params['sutId']))
          .subscribe((sut: SutModel) => {
            this.sut = sut;
            this.managedChecked = sut.sutType === 'MANAGED';
            this.repoNameChecked = sut.sutType === 'REPOSITORY';
            this.deployedChecked = sut.sutType === 'DEPLOYED';
          });
      } else if (this.currentPath === 'new') {
        this.route.params.switchMap((params: Params) => this.projectService.getProject(params['projectId']))
          .subscribe(
          (project: ProjectModel) => {
            this.sut = new SutModel();
            this.sut.project = project;
            this.sut.sutType = 'MANAGED';
          },
        );
      }
    }
  }
  sutBy(selected: string) {
    // Reset
    this.managedChecked = false;
    this.repoNameChecked = false;
    this.deployedChecked = false;

    if (selected === 'managedSut') {
      this.sut.sutType = 'MANAGED';
      this.managedChecked = true;
    } else {
      if (selected === 'deployedSut') {
        this.sut.sutType = 'DEPLOYED';
        this.deployedChecked = true;
      } else {
        this.sut.sutType = 'REPOSITORY';
        this.repoNameChecked = true;
      }
    }
  }

  goBack(): void {
    window.history.back();
  }

  preSave() {
    if (this.sut.sutType === 'DEPLOYED') {
      this.sutService.getLogstashInfo().subscribe(
        (data) => {
          this.sut.eimConfig.ip = this.sut.specification;
          this.sut.eimConfig.logstashIp = data.logstashIp;
          this.sut.eimConfig.logstashBeatsPort = data.logstashBeatsPort;
          this.sut.eimConfig.logstashHttpPort = data.logstashHttpPort;

          this.save();
        },
        (error) => console.log(error),
      );
    } else {
      this.sut.eimConfig = undefined;
      this.save();
    }
  }

  save() {
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

  changeUseEIM($event) {
    this.sut.instrumentalize = $event.checked;
  }
}
