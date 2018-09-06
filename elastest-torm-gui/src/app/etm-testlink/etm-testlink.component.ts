import { TdDialogService } from '@covalent/core/dialogs/services/dialog.service';
import { Router } from '@angular/router';
import { TdDataTableService } from '@covalent/core';
import { TitlesService } from '../shared/services/titles.service';
import { TestProjectModel } from './models/test-project-model';
import { Component, Input, OnInit, ViewContainerRef, HostListener, OnDestroy } from '@angular/core';
import { TestLinkService } from './testlink.service';
import { MdDialog } from '@angular/material';
import { EtPluginsService } from '../elastest-test-engines/et-plugins.service';
import { EtPluginModel } from '../elastest-test-engines/et-plugin-model';
import { Subscription, Observable } from 'rxjs';

@Component({
  selector: 'etm-testlink',
  templateUrl: './etm-testlink.component.html',
  styleUrls: ['./etm-testlink.component.scss'],
})
export class EtmTestlinkComponent implements OnInit, OnDestroy {
  @Input()
  isNested: boolean = false;
  isStarted: boolean = false;
  startingInProcess: boolean = true;
  testLinkUrl: string;

  disableBtns: boolean = false;

  timer: Observable<number>;
  subscription: Subscription;

  // Project data
  projectColumns: any[] = [
    { name: 'id', label: 'Id' },
    { name: 'name', label: 'Project' },
    { name: 'prefix', label: 'Prefix' },
    { name: 'notes', label: 'Notes' },
    { name: 'enableRequirements', label: 'Enable Requirements' },
    { name: 'enableTestPriority', label: 'Enable Test Priority' },
    { name: 'enableAutomation', label: 'Enable Automation' },
    { name: 'enableInventory', label: 'Enable Inventory' },
    { name: 'active', label: 'Active' },
    { name: 'public', label: 'Public' },
    // { name: 'options', label: 'Options' },
  ];

  projectsList: TestProjectModel[] = [];
  showSpinner: boolean = true;

  testLinkModel: EtPluginModel;

  constructor(
    private titlesService: TitlesService,
    private testlinkService: TestLinkService,
    public dialog: MdDialog,
    private etPluginsService: EtPluginsService,
  ) {}

  ngOnInit() {
    if (!this.isNested) {
      this.titlesService.setHeadTitle('TestLink');
    }

    this.etPluginsService.getUniqueEtPlugin('testlink').subscribe(
      (testLinkModel: EtPluginModel) => {
        this.testLinkModel = testLinkModel;
        this.initIfStarted();
        if (testLinkModel.isNotInitialized()) {
          this.startingInProcess = false;
        } else {
          this.waitForReady();
        }
      },
      (error: Error) => console.log(error),
    );
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  @HostListener('window:beforeunload')
  beforeunloadHandler() {
    // On window closed leave session
    this.unsubscribe();
  }

  unsubscribe(): void {
    if (this.subscription !== undefined) {
      this.subscription.unsubscribe();
      this.subscription = undefined;
    }
  }

  waitForReady(): void {
    this.timer = Observable.interval(1800);
    if (
      this.testLinkModel.isCreated() &&
      !this.testLinkModel.isReady() &&
      (this.subscription === null || this.subscription === undefined)
    ) {
      this.subscription = this.timer.subscribe(() => {
        this.etPluginsService.getEtPlugin(this.testLinkModel.name).subscribe((etPlugin: EtPluginModel) => {
          this.testLinkModel = etPlugin;
          if (etPlugin.isReady()) {
            this.initIfStarted();
            this.subscription.unsubscribe();
            this.subscription = undefined;
          }
        });
      });
    }
  }

  initIfStarted(): void {
    this.etPluginsService.isStarted(this.testLinkModel).subscribe(
      (started: boolean) => {
        this.isStarted = started;
        if (started) {
          this.startingInProcess = false;
          this.loadData();
        }
      },
      (error: Error) => console.log(error),
    );
  }

  startTestLink(): void {
    this.startingInProcess = true;
    this.testlinkService.startTestLink().subscribe(
      (testLinkModel: EtPluginModel) => {
        this.testLinkModel = testLinkModel;
        this.waitForReady();
      },
      (error: Error) => {
        console.log(error);
        this.startingInProcess = false;
      },
    );
  }

  loadData(): void {
    this.loadTestLinkUrl();
    this.loadProjects();
  }

  loadTestLinkUrl(): void {
    this.testlinkService.getTestlinkUrl().subscribe((url: string) => {
      this.testLinkUrl = url;
    });
  }

  loadProjects(): void {
    this.testlinkService.getAllTestProjects().subscribe((projects: TestProjectModel[]) => {
      this.projectsList = projects;
      this.showSpinner = false;
    });
  }

  syncTestLink(): void {
    this.disableBtns = true;
    this.testlinkService.syncTestlink().subscribe(
      (sync: boolean) => {
        this.loadProjects();
        this.testlinkService.popupService.openSnackBar('Successfully synchronized with Elastest!');
        this.disableBtns = false;
      },
      (error: Error) => {
        this.disableBtns = false;
        console.log(error);
      },
    );
  }
}
