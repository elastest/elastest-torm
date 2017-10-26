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

  withoutInsCheck: boolean = true;
  elastestInsCheck: boolean = false;
  adminInsCheck: boolean = false;

  specText: string = 'SuT Specification';
  deployedSpecText: string = 'SuT IP';
  managedSpecText: string = 'Docker Image';

  instrumentalized: boolean = false;

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
            this.initSutType();
            this.initInstrumentalizedBy();
            this.initInstrumentalized();
          });
      } else if (this.currentPath === 'new') {
        this.route.params.switchMap((params: Params) => this.projectService.getProject(params['projectId']))
          .subscribe(
          (project: ProjectModel) => {
            this.sut = new SutModel();
            this.sut.project = project;
            this.sut.sutType = 'MANAGED';
            this.sut.instrumentedBy = 'WITHOUT';
            this.initInstrumentalized();
          },
        );
      }
    }
  }

  initSutType() {
    this.managedChecked = this.sut.sutType === 'MANAGED';
    this.repoNameChecked = this.sut.sutType === 'REPOSITORY';
    this.deployedChecked = this.sut.sutType === 'DEPLOYED';
  }

  initInstrumentalizedBy() {
    this.withoutInsCheck = this.sut.instrumentedBy === 'WITHOUT';
    this.elastestInsCheck = this.sut.instrumentedBy === 'ELASTEST';
    this.adminInsCheck = this.sut.instrumentedBy === 'ADMIN';
  }

  initInstrumentalized() {
    this.instrumentalized = this.sut.instrumentalize;
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

  preSave(exit: boolean = true) {
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

  save(exit: boolean = true) {
    this.sutService.createSut(this.sut)
      .subscribe(
      (sut) => this.postSave(sut),
      (error) => console.log(error)
      );
  }

  postSave(sut: any, exit: boolean = true) {
    this.sut = sut;
    if (exit) {
      window.history.back();
    }
  }

  cancel() {
    window.history.back();
  }

  instrumentalize($event) {
    this.sut.instrumentalize = $event.checked;
  }

  deinstrumentalize($event) {
    this.sut.instrumentalize = !$event.checked;
  }

  deployedType(selected: string) {
    // Reset
    this.withoutInsCheck = false;
    this.elastestInsCheck = false;
    this.adminInsCheck = false;

    if (selected === 'withoutIns') {
      this.sut.instrumentedBy = 'WITHOUT';
      this.withoutInsCheck = true;
    } else {
      if (selected === 'elastestIns') {
        this.sut.instrumentedBy = 'ELASTEST';
        this.elastestInsCheck = true;
      } else {
        this.sut.instrumentedBy = 'ADMIN';
        this.adminInsCheck = true;
      }
    }
  }

}
