import { ConfigurationService } from '../../../config/configuration-service.service';
import { TitlesService } from '../../../shared/services/titles.service';
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

  dockerCompose: boolean = false;

  specText: string = 'SuT Specification';
  deployedSpecText: string = 'SuT IP';
  managedSpecText: string = 'Docker Image';

  instrumentalized: boolean = false;

  elasTestExecMode: string;

  constructor(
    private titlesService: TitlesService,
    private sutService: SutService, private route: ActivatedRoute,
    private projectService: ProjectService,
    private configurationService: ConfigurationService,
  ) { }

  ngOnInit() {
    this.titlesService.setHeadTitle('Edit SuT');
    this.elasTestExecMode = this.configurationService.configModel.elasTestExecMode;
    this.sut = new SutModel();
    this.currentPath = this.route.snapshot.url[0].path;
    if (this.route.params !== null || this.route.params !== undefined) {
      if (this.currentPath === 'edit') {
        this.route.params.switchMap((params: Params) => this.sutService.getSut(params['sutId']))
          .subscribe((sut: SutModel) => {
            this.sut = sut;
            this.titlesService.setTopTitle(this.sut.getRouteString());
            this.initSutType();
            this.initInstrumentedBy();
            this.initInstrumentalized();
            this.dockerCompose = this.sut.isByDockerCompose();
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
            this.sut.managedDockerType = 'IMAGE';
          },
        );
      }
    }
  }

  initSutType(): void {
    this.managedChecked = this.sut.sutType === 'MANAGED';
    this.repoNameChecked = this.sut.sutType === 'REPOSITORY';
    this.deployedChecked = this.sut.sutType === 'DEPLOYED';
  }

  initInstrumentedBy(): void {
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
        this.sut.mainService = '';
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

          this.save(exit);
        },
        (error) => console.log(error),
      );
    } else {
      this.sut.eimConfig = undefined;
      this.save(exit);
    }
  }

  save(exit: boolean = true) {
    this.sutService.createSut(this.sut)
      .subscribe(
      (sut) => this.postSave(sut, exit),
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

  showGetInfoBtn() {
    return (
      !this.sut.eimConfig.logstashIp && !this.sut.eimConfig.logstashBeatsPort && !this.sut.eimConfig.logstashHttpPort && !this.sut.currentSutExec
    );
  }

  managedDockerTypeBy(compose: boolean): void {
    this.dockerCompose = compose;
    this.sut.managedDockerType = compose ? 'COMPOSE' : 'IMAGE';
  }
}
