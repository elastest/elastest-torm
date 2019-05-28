import { ExternalTJobModel } from '../external-tjob-model';
import { TitlesService } from '../../../../shared/services/titles.service';
import { ExternalService } from '../../external.service';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';
import { SutModel } from '../../../sut/sut-model';
import { EsmService } from '../../../../elastest-esm/esm-service.service';
import { EsmServiceModel } from '../../../../elastest-esm/esm-service.model';
import { SupportServiceConfigModel } from '../../../../elastest-esm/support-service.model';

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

  esmServicesCatalog: EsmServiceModel[] = [];
  esmServicesAvailableInExternal: string[] = ['ESS'];

  constructor(
    private externalService: ExternalService,
    private titlesService: TitlesService,
    private route: ActivatedRoute,
    private esmService: EsmService,
  ) {}

  ngOnInit(): void {
    this.exTJob = new ExternalTJobModel();
    if (this.route.params !== null || this.route.params !== undefined) {
      // If routing
      this.currentPath = this.route.snapshot.url[0].path;
      this.route.params.subscribe((params: Params) => {
        this.esmService.getSupportServices().subscribe((response: EsmServiceModel[]) => {
          if (response) {
            for (let tss of response) {
              if (this.esmServicesAvailableInExternal.indexOf(tss.name) > -1) {
                this.esmServicesCatalog.push(tss);
              }
            }
          }

          if (this.currentPath === 'edit') {
            if (params['tJobId']) {
              this.externalService.getExternalTJobById(params['tJobId']).subscribe(
                (exTJob: ExternalTJobModel) => {
                  this.exTJob = exTJob;
                  this.currentSut = exTJob.sut.id > 0 ? exTJob.sut.name : 'None';

                  for (let tJobEsmService of this.exTJob.esmServices) {
                    for (let esmServiceToSelect of this.esmServicesCatalog) {
                      if (tJobEsmService.selected && tJobEsmService.id === esmServiceToSelect.id) {
                        esmServiceToSelect.selected = true;
                        for (let singleTJobEsmConfigKey of tJobEsmService.getConfigKeys()) {
                          let singleTJobEsmConfig: SupportServiceConfigModel =
                            tJobEsmService.manifest.config[singleTJobEsmConfigKey];
                          for (let singleEsmConfigKey of esmServiceToSelect.getConfigKeys()) {
                            let singleEsmConfig: SupportServiceConfigModel =
                              esmServiceToSelect.manifest.config[singleEsmConfigKey];
                            if (singleTJobEsmConfig.value !== undefined && singleTJobEsmConfig.value !== null) {
                              singleEsmConfig.value = singleTJobEsmConfig.value;
                            }
                          }
                        }
                      }
                    }
                  }
                },
                (error: Error) => console.log(error),
              );
            }
          }
        });
      });
    }
  }

  save(): void {
    this.exTJob.esmServices = this.esmServicesCatalog;

    this.externalService
      .modifyExternalTJob(this.exTJob)
      .subscribe((exTJob: ExternalTJobModel) => this.postSave(exTJob), (error: Error) => console.log(error));
  }
  postSave(exTJob: any): void {
    this.exTJob = exTJob;
    window.history.back();
  }

  cancel(): void {
    window.history.back();
  }
}
