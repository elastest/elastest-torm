import { TitlesService } from '../../../shared/services/titles.service';
import { SutModel } from '../../sut/sut-model';
import {
  TdDialogService,
  TdDataTableSortingOrder,
  TdDataTableService,
  ITdDataTableSortChangeEvent,
  ITdDataTableColumn,
  ITdDataTableSelectEvent,
  ITdDataTableSelectAllEvent,
} from '@covalent/core';
import { SutService } from '../../sut/sut.service';
import { IConfirmConfig } from '@covalent/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Component, OnInit, ViewContainerRef, Input } from '@angular/core';
import { MatDialog } from '@angular/material';
import { ProjectService } from '../../project/project.service';
import { ProjectModel } from '../../project/project-model';
import { ExternalProjectModel } from '../../external/external-project/external-project-model';
import { ExternalService } from '../../external/external.service';
import { Observable, Subject } from 'rxjs';

@Component({
  selector: 'etm-suts-manager',
  templateUrl: './suts-manager.component.html',
  styleUrls: ['./suts-manager.component.scss'],
})
export class SutsManagerComponent implements OnInit {
  @Input()
  projectId: string;
  @Input()
  exProjectId: string;

  @Input()
  project: ProjectModel;

  @Input()
  exProject: ExternalProjectModel;
  parentType: string;

  suts: SutModel[] = [];
  showSpinner: boolean = true;

  deletingInProgress: boolean = false;
  duplicateInProgress: boolean = false;

  selectedSutsIds: number[] = [];
  sutIdsWithErrorOnDelete: number[] = [];

  // SuT Data
  sutColumns: ITdDataTableColumn[] = [
    { name: 'id', label: 'Id', width: 80 },
    { name: 'name', label: 'Name' },
    { name: 'specification', label: 'Specification', sortable: false },
    { name: 'sutType', label: 'SuT Type', width: { min: 74, max: 113 } },
    { name: 'description', label: 'Description', sortable: false },
    { name: 'options', label: 'Options', sortable: false, width: { min: 46, max: 130 } },
  ];

  sortBy: string = 'id';
  sortOrder: TdDataTableSortingOrder = TdDataTableSortingOrder.Ascending;

  constructor(
    private titlesService: TitlesService,
    private projectService: ProjectService,
    private route: ActivatedRoute,
    private router: Router,
    private externalService: ExternalService,
    private sutService: SutService,
    private _dialogService: TdDialogService,
    private _viewContainerRef: ViewContainerRef,
    public dialog: MatDialog,
    private dataTableService: TdDataTableService,
  ) {}

  ngOnInit(): void {
    this.init();
  }

  init(forceLoadProject: boolean = false): void {
    // If child
    if (this.project || this.exProject) {
      if (this.project) {
        this.initDataFromProject(forceLoadProject);
      } else if (this.exProject) {
        this.initDataFromExternalProject(forceLoadProject);
      }
    } else if (this.projectId || this.exProjectId) {
      if (this.projectId) {
        this.loadProjectAndSuts(this.projectId);
      } else if (this.exProjectId) {
        this.loadExternalProjectAndSuts(this.exProjectId);
      }
    } else if (this.route.params !== null || this.route.params !== undefined) {
      // If routing
      this.route.params.subscribe((params: Params) => {
        if (params['projectId']) {
          this.loadProjectAndSuts(params['projectId']);
        } else if (params['exProjectId']) {
          this.loadExternalProjectAndSuts(params['exProjectId']);
        } else {
          // Get all suts
          this.loadAllSuts();
        }
      });
    } else {
      this.loadAllSuts();
    }
  }

  loadProjectAndSuts(projectId: string | number): void {
    this.projectService.getProject(projectId, 'medium').subscribe((project: ProjectModel) => {
      this.project = project;
      this.initDataFromProject();
      this.duplicateInProgress = false;
    });
  }

  initDataFromProject(forceLoadProject: boolean = false): void {
    if (forceLoadProject) {
      this.loadProjectAndSuts(this.project.id);
    }
    this.parentType = 'ProjectModel';
    if (this.project) {
      this.suts = [...this.project.suts];
      this.showSpinner = false;
    }
  }

  loadExternalProjectAndSuts(exProjectId: string | number): void {
    this.externalService.getExternalProjectById(exProjectId).subscribe((exProject: ExternalProjectModel) => {
      this.exProject = exProject;
      this.initDataFromExternalProject();
      this.duplicateInProgress = false;
    });
  }

  initDataFromExternalProject(forceLoadProject: boolean = false): void {
    if (forceLoadProject) {
      this.loadExternalProjectAndSuts(this.exProject.id);
    }
    this.parentType = 'ExternalProjectModel';
    if (this.exProject) {
      this.suts = [...this.exProject.suts];
      this.showSpinner = false;
    }
  }

  loadAllSuts(): void {
    this.sutService.getSuts().subscribe(
      (suts: SutModel[]) => {
        this.suts = [...suts];
        this.duplicateInProgress = false;
      },
      (error: Error) => {
        this.duplicateInProgress = false;
        console.log(error);
      },
    );
  }

  newSut(): void {
    if (this.parentType === 'ExternalProjectModel') {
      this.router.navigate(['/external/projects', this.exProject.id, 'sut', 'new']);
    } else if (this.parentType === 'ProjectModel') {
      this.router.navigate(['/projects', this.project.id, 'sut', 'new']);
    }
  }

  viewSut(sut: SutModel): void {
    if (this.parentType === 'ExternalProjectModel') {
      this.router.navigate(['/external/project', this.exProject.id, 'sut', sut.id]);
    } else if (this.parentType === 'ProjectModel') {
      this.router.navigate(['/projects', this.project.id, 'sut', sut.id]);
    }
  }

  editSut(sut: SutModel): void {
    if (this.parentType === 'ExternalProjectModel') {
      this.router.navigate(['/external/projects', this.exProject.id, 'sut', 'edit', sut.id]);
    } else if (this.parentType === 'ProjectModel') {
      this.router.navigate(['/projects', this.project.id, 'sut', 'edit', sut.id]);
    }
  }

  deleteSut(sut: SutModel): void {
    let iConfirmConfig: IConfirmConfig = {
      message:
        'Sut ' +
        sut.id +
        " will be deleted with all SuT Executions, do you want to continue? (SuT only will be deleted if hasn't associated TJobs)",
      disableClose: false,
      viewContainerRef: this._viewContainerRef,
      title: 'Confirm',
      cancelButton: 'Cancel',
      acceptButton: 'Yes, delete',
    };
    this._dialogService
      .openConfirm(iConfirmConfig)
      .afterClosed()
      .subscribe((accept: boolean) => {
        if (accept) {
          this.deletingInProgress = true;
          this.sutService.deleteSut(sut).subscribe(
            (sut) => {
              this.deletingInProgress = false;
              this.init(true);
            },
            (error: Error) => {
              this.deletingInProgress = false;
              console.log(error);
            },
          );
        }
      });
  }

  duplicateSut(sut: SutModel): void {
    this.duplicateInProgress = true;
    this.sutService.duplicateSut(sut).subscribe(
      (sut: SutModel) => {
        this.init(true);
        this.duplicateInProgress = false;
      },
      (error: Error) => {
        console.log(error);
        this.duplicateInProgress = false;
      },
    );
  }

  sort(sortEvent: ITdDataTableSortChangeEvent): void {
    this.sortBy = sortEvent.name;
    this.sortOrder = sortEvent.order;
    this.suts = [...this.dataTableService.sortData(this.suts, this.sortBy, this.sortOrder)];
  }

  removeSelectedSuts(): void {
    if (this.selectedSutsIds.length > 0) {
      let iConfirmConfig: IConfirmConfig = {
        message: 'Selected Suts will be deleted, do you want to continue?',
        disableClose: false, // defaults to false
        viewContainerRef: this._viewContainerRef,
        title: 'Confirm',
        cancelButton: 'Cancel',
        acceptButton: 'Yes, delete',
      };
      this._dialogService
        .openConfirm(iConfirmConfig)
        .afterClosed()
        .subscribe((accept: boolean) => {
          if (accept) {
            this.deletingInProgress = true;

            this.sutIdsWithErrorOnDelete = [];
            this.removeMultipleSutsRecursively([...this.selectedSutsIds]).subscribe(
              (end: boolean) => {
                if (this.sutIdsWithErrorOnDelete.length > 0) {
                  let errorMsg: string = 'Error on delete suts with ids: ' + this.sutIdsWithErrorOnDelete;
                  this.externalService.popupService.openSnackBar(errorMsg);
                } else {
                  this.externalService.popupService.openSnackBar('Suts ' + this.selectedSutsIds + ' has been removed!');
                }

                this.deletingInProgress = false;
                this.init(true);
                this.sutIdsWithErrorOnDelete = [];
                this.selectedSutsIds = [];
              },
              (error: Error) => {
                console.log(error);
                this.deletingInProgress = false;
                this.init();
                this.sutIdsWithErrorOnDelete = [];
                this.selectedSutsIds = [];
              },
            );
          }
        });
    }
  }

  switchSutsSelectionByData(sut: SutModel, selected: boolean): void {
    if (selected) {
      this.selectedSutsIds.push(sut.id);
    } else {
      const index: number = this.selectedSutsIds.indexOf(sut.id, 0);
      if (index > -1) {
        this.selectedSutsIds.splice(index, 1);
      }
    }
  }

  switchSutSelection(event: ITdDataTableSelectEvent): void {
    if (event && event.row) {
      let sut: SutModel = event.row;
      this.switchSutsSelectionByData(sut, event.selected);
    }
  }

  switchAllSutsSelection(event: ITdDataTableSelectAllEvent): void {
    if (event && event.rows) {
      for (let sut of event.rows) {
        this.switchSutsSelectionByData(sut, event.selected);
      }
    }
  }

  removeMultipleSutsRecursively(selectedSutsIds: number[], _obs: Subject<any> = new Subject<any>()): Observable<boolean> {
    let obs: Observable<any> = _obs.asObservable();

    if (selectedSutsIds.length > 0) {
      let sutId: number = selectedSutsIds.shift();
      this.sutService.deleteSutById(sutId).subscribe(
        (sut: SutModel) => {
          this.removeMultipleSutsRecursively(selectedSutsIds, _obs);
        },
        (error: Error) => {
          console.log(error);
          this.sutIdsWithErrorOnDelete.push(sutId);
          this.removeMultipleSutsRecursively(selectedSutsIds, _obs);
        },
      );
    } else {
      _obs.next(true);
    }

    return obs;
  }
}
