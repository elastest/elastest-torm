import { TitlesService } from '../../../shared/services/titles.service';
import { EsmService } from '../../../elastest-esm/esm-service.service';
import { EsmServiceModel } from '../../../elastest-esm/esm-service.model';
import { ProjectModel } from '../../project/project-model';
import { ProjectService } from '../../project/project.service';
import { SutModel } from '../../sut/sut-model';
import { TJobModel } from '../tjob-model';
import { TJobService } from '../tjob.service';

import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';
import { AfterViewInit } from '@angular/core/src/metadata/lifecycle_hooks';
import { SupportServiceConfigModel } from '../../../elastest-esm/support-service.model';

@Component({
  selector: 'etm-tjob-form',
  templateUrl: './tjob-form.component.html',
  styleUrls: ['./tjob-form.component.scss'],
})
export class TJobFormComponent implements OnInit, AfterViewInit {
  @ViewChild('tjobNameInput') tjobNameInput: ElementRef;

  tJob: TJobModel;
  editMode: boolean = false;

  sutEmpty: SutModel = new SutModel();
  currentSut: string = 'None';
  useImageCommand: boolean = false;
  elastestEsmServices: string[];
  esmServicesCatalog: EsmServiceModel[];
  action: string;

  constructor(
    private titlesService: TitlesService,
    private tJobService: TJobService,
    private route: ActivatedRoute,
    private projectService: ProjectService,
    private esmService: EsmService,
  ) {
    this.esmServicesCatalog = [];
  }

  ngOnInit() {
    this.titlesService.setHeadTitle('Edit TJob');
    this.init();
  }

  ngAfterViewInit() {
    this.tjobNameInput.nativeElement.focus();
  }

  init(): void {
    this.tJob = new TJobModel();
    this.action = this.route.snapshot.url[0].path;
    if (this.route.params !== null || this.route.params !== undefined) {
      this.esmService.getSupportServices().subscribe((response) => {
        this.esmServicesCatalog = response;
        if (this.action === 'edit') {
          this.editMode = true;
          this.route.params
            .switchMap((params: Params) => this.tJobService.getTJob(params['tJobId']))
            .subscribe((tJob: TJobModel) => {
              this.tJob = tJob;
              this.titlesService.setTopTitle(this.tJob.getRouteString());
              this.currentSut = tJob.sut.id > 0 ? tJob.sut.name : 'None';
              this.useImageCommand = !this.tJob.withCommands();
              for (let tJobEsmService of this.tJob.esmServices) {
                for (let esmServiceToSelect of this.esmServicesCatalog) {
                  if (tJobEsmService.selected && tJobEsmService.id === esmServiceToSelect.id) {
                    esmServiceToSelect.selected = true;
                    for (let singleTJobEsmConfigKey of tJobEsmService.getConfigKeys()) {
                      let singleTJobEsmConfig: SupportServiceConfigModel = tJobEsmService.config[singleTJobEsmConfigKey];
                      for (let singleEsmConfigKey of esmServiceToSelect.getConfigKeys()) {
                        let singleEsmConfig: SupportServiceConfigModel = esmServiceToSelect.config[singleEsmConfigKey];
                        if (singleTJobEsmConfig.value !== undefined && singleTJobEsmConfig.value !== null) {
                          singleEsmConfig.value = singleTJobEsmConfig.value;
                        }
                      }
                    }
                  }
                }
              }
            });
        } else if (this.action === 'new') {
          this.route.params
            .switchMap((params: Params) => this.projectService.getProject(params['projectId'], true))
            .subscribe((project: ProjectModel) => {
              this.tJob = new TJobModel();
              this.tJob.project = project;
            });
        }
      });
    }
  }

  goBack(): void {
    window.history.back();
  }

  save(): void {
    if (this.useImageCommand) {
      this.tJob.commands = '';
    }
    this.tJob.esmServices = this.esmServicesCatalog;
    console.log('Services ' + JSON.stringify(this.tJob.esmServices));

    this.tJobService.createTJob(this.tJob, this.action).subscribe((tJob) => this.postSave(tJob), (error) => console.log(error));
  }

  postSave(tJob: any): void {
    this.tJob = tJob;
    window.history.back();
  }

  cancel(): void {
    window.history.back();
  }

  logChecked(log: any): boolean {
    if (this.hideSut(log)) {
      return false;
    } else {
      return log.activated;
    }
  }

  hideSut(log: any): boolean {
    return (
      log.name.startsWith('sut') &&
      (this.tJob.sut === this.sutEmpty || this.tJob.sut === undefined || (this.action !== 'new' && !this.tJob.hasSut()))
    );
  }
}
