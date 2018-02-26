import { ConfigurationService } from '../../../config/configuration-service.service';
import { TitlesService } from '../../../shared/services/titles.service';
import { ProjectModel } from '../../project/project-model';
import { ProjectService } from '../../project/project.service';
import { SutModel } from '../sut-model';
import { SutService } from '../sut.service';

import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';
import { AfterViewInit } from '@angular/core/src/metadata/lifecycle_hooks';
import { ExternalService } from '../../external/external.service';
import { ExternalProjectModel } from '../../external/external-project/external-project-model';

@Component({
  selector: 'etm-sut-form',
  templateUrl: './sut-form.component.html',
  styleUrls: ['./sut-form.component.scss'],
})
export class SutFormComponent implements OnInit, AfterViewInit {
  @ViewChild('sutNameInput') sutNameInput: ElementRef;

  sut: SutModel;
  sutExecIndex: string = '';
  editMode: boolean = false;
  currentPath: string = '';

  // Sut Type
  managedChecked: boolean = true;
  repoNameChecked: boolean = false;
  deployedChecked: boolean = false;

  // Instrumented By
  withoutInsCheck: boolean = true;
  elastestInsCheck: boolean = false;
  adminInsCheck: boolean = false;

  // Managed Docker Type
  commands: boolean = true;
  dockerImage: boolean = false;
  dockerCompose: boolean = false;

  // Commands Option
  optionDefault: boolean = true;
  optionInNewContainer: boolean = false;
  optionInDockerCompose: boolean = false;

  commandsContainerHelpHead: string = 'SuT is started inside the command container';
  dockerImageHelpHead: string = this.commandsContainerHelpHead + ' into a new docker container';
  dockerComposeHelpHead: string = this.commandsContainerHelpHead + ' into a new docker compose containers';
  currentCommandsModeHelpHead: string = this.commandsContainerHelpHead;

  commandsContainerHelpDesc: string = 'Launch your SuT in the "Commands" text area above.';
  dockerImageHelpDesc: string = 'You need to use a docker image with docker installed into, such "elastest/test-etm-alpinedockerjava".' +
    ' The only mandatory requirement is that your "Commands" must end with "docker run --name $ET_SUT_CONTAINER_NAME ..." command.' +
    ' Use Example: "docker run --rm --name $ET_SUT_CONTAINER_NAME myimage"';
  dockerComposeHelpDesc: string = 'You need to use a docker image with docker installed into, such "elastest/test-etm-alpinedockerjava".' +
    ' The only mandatory requirement is that your "Commands" must end with "docker-compose ... -p $ET_SUT_CONTAINER_NAME up" command.' +
    ' Use Example: "docker-compose -f docker-compose.yml -p $ET_SUT_CONTAINER_NAME up"';

  currentCommandsModeHelpDesc: string = this.commandsContainerHelpDesc;

  // Others
  specText: string = 'SuT Specification';
  deployedSpecText: string = 'SuT IP';
  managedSpecText: string = 'Docker Image';
  managedCommandsSpecText: string = 'Commands Container Image';

  instrumentalized: boolean = false;

  elasTestExecMode: string;

  constructor(
    private titlesService: TitlesService,
    private sutService: SutService,
    private route: ActivatedRoute,
    private projectService: ProjectService,
    private externalService: ExternalService,
    private configurationService: ConfigurationService,
  ) {}

  ngOnInit() {
    this.titlesService.setHeadTitle('Edit SuT');
    this.elasTestExecMode = this.configurationService.configModel.elasTestExecMode;
    this.sut = new SutModel();
    if (this.route.params !== null || this.route.params !== undefined) {
      this.currentPath = this.route.snapshot.url[0].path;
      if (this.currentPath === 'edit') {
        this.route.params.switchMap((params: Params) => this.sutService.getSut(params['sutId'])).subscribe((sut: SutModel) => {
          this.sut = sut;
          this.titlesService.setTopTitle(this.sut.getRouteString());
          this.initSutType();
          this.initInstrumentedBy();
          this.initInstrumentalized();
          this.initCommandOptions();
          this.initManagedType();

          this.sutExecIndex = this.sut.getSutESIndex();
        });
      } else if (this.currentPath === 'new') {
        if (this.route.params !== null || this.route.params !== undefined) {
          // If routing
          this.route.params.subscribe((params: Params) => {
            if (params['projectId']) {
              this.loadFromProject(params['projectId']);
            } else if (params['exProjectId']) {
              this.loadFromExternalProject(params['exProjectId']);
            }
          });
        }
      }
    }
  }

  loadFromProject(projectId: string): void {
    this.projectService.getProject(projectId).subscribe((project: ProjectModel) => {
      this.sut = new SutModel();
      this.sut.project = project;
      this.initCommonSutFields();
    });
  }

  loadFromExternalProject(exProjectId: string): void {
    this.externalService.getExternalProjectById(exProjectId).subscribe((exProject: ExternalProjectModel) => {
      this.sut = new SutModel();
      this.sut.exProject = exProject;
      this.initCommonSutFields();
    });
  }

  initCommonSutFields(): void {
    this.sut.sutType = 'MANAGED';
    this.sut.instrumentedBy = 'WITHOUT';
    this.sut.managedDockerType = 'COMMANDS';
    this.sut.commandsOption = 'DEFAULT';
    this.initInstrumentalized();
  }

  ngAfterViewInit() {
    this.sutNameInput.nativeElement.focus();
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

  initInstrumentalized(): void {
    this.instrumentalized = this.sut.instrumentalize;
  }

  initManagedType(): void {
    this.commands = this.sut.isByCommands();
    this.dockerImage = this.sut.isByDockerImage();
    this.dockerCompose = this.sut.isByDockerCompose();
  }

  initCommandOptions(): void {
    this.changeCommandsOption(this.sut.commandsOption);
  }

  sutBy(selected: string): void {
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

  instrumentalize($event): void {
    this.sut.instrumentalize = $event.checked;
  }

  deinstrumentalize($event): void {
    this.sut.instrumentalize = !$event.checked;
  }

  deployedType(selected: string): void {
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

  showGetInfoBtn(): boolean {
    return (
      !this.sut.eimConfig.logstashIp &&
      !this.sut.eimConfig.logstashBeatsPort &&
      !this.sut.eimConfig.logstashHttpPort &&
      !this.sut.currentSutExec
    );
  }

  managedDockerTypeBy(mode: string): void {
    this.commands = false;
    this.dockerImage = false;
    this.dockerCompose = false;
    switch (mode) {
      case 'commands':
        this.commands = true;
        this.sut.managedDockerType = 'COMMANDS';
        break;
      case 'image':
        this.dockerImage = true;
        this.sut.managedDockerType = 'IMAGE';
        break;
      case 'compose':
        this.dockerCompose = true;
        this.sut.managedDockerType = 'COMPOSE';
        break;
      default:
    }
  }

  changeCommandsOption(option: string): void {
    this.optionDefault = false;
    this.optionInNewContainer = false;
    this.optionInDockerCompose = false;
    switch (option) {
      case 'IN_NEW_CONTAINER':
      case 'container':
        this.optionInNewContainer = true;
        this.sut.commandsOption = 'IN_NEW_CONTAINER';
        this.currentCommandsModeHelpHead = this.dockerImageHelpHead;
        this.currentCommandsModeHelpDesc = this.dockerImageHelpDesc;
        break;
      case 'IN_DOCKER_COMPOSE':
      case 'compose':
        this.optionInDockerCompose = true;
        this.sut.commandsOption = 'IN_DOCKER_COMPOSE';
        this.currentCommandsModeHelpHead = this.dockerComposeHelpHead;
        this.currentCommandsModeHelpDesc = this.dockerComposeHelpDesc;
        break;
      case 'DEFAULT':
      case 'default':
      default:
        this.optionDefault = true;
        this.sut.commandsOption = 'DEFAULT';
        this.currentCommandsModeHelpHead = this.commandsContainerHelpHead;
        this.currentCommandsModeHelpDesc = this.commandsContainerHelpDesc;

        break;
    }
  }

  goBack(): void {
    window.history.back();
  }

  preSave(exit: boolean = true): void {
    if (this.sut.sutType === 'DEPLOYED') {
      this.sutService.getLogstashInfo().subscribe(
        (data: any) => {
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

  save(exit: boolean = true): void {
    if (!this.sut.isByCommands() || !this.managedChecked) {
      this.sut.commands = '';
    }
    if (!this.sut.isByDockerCompose() && !this.sut.isByCommandsInDockerCompose()) {
      this.sut.mainService = '';
    }
    this.sutService.createSut(this.sut).subscribe(
      (sut: SutModel) => this.postSave(sut, exit),
      (error) => {
        this.externalService.popupService.openSnackBar('An error has occurred');
        console.log(error);
      },
    );
  }

  postSave(sut: SutModel, exit: boolean = true): void {
    this.sut = sut;
    this.sutExecIndex = this.sut.getSutESIndex();

    if (exit) {
      window.history.back();
    }
  }

  cancel(): void {
    window.history.back();
  }
}
