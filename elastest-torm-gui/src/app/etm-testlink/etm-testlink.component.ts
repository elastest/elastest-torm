import { TitlesService } from '../shared/services/titles.service';
import { TestProjectModel } from './models/test-project-model';
import { Component, Input, OnInit, ViewContainerRef, HostListener, OnDestroy } from '@angular/core';
import { TestLinkService } from './testlink.service';
import { MatDialog, MatDialogRef } from '@angular/material';
import { EtPluginsService } from '../elastest-test-engines/et-plugins.service';
import { EtPluginModel } from '../elastest-test-engines/et-plugin-model';
import { Subscription, Observable, interval } from 'rxjs';
import { CredentialsDialogComponent } from '../shared/credentials-dialog/credentials-dialog.component';

@Component({
  selector: 'etm-testlink',
  templateUrl: './etm-testlink.component.html',
  styleUrls: ['./etm-testlink.component.scss'],
})
export class EtmTestlinkComponent implements OnInit, OnDestroy {
  @Input()
  isNested: boolean = false;
  isRunning: boolean = false;
  startingInProcess: boolean = true;
  testLinkUrl: string;

  disableBtns: boolean = false;

  timer: Observable<number>;
  subscription: Subscription;
  startedFirstTime: boolean = false;

  // Project data
  projectColumns: any[] = [
    { name: 'id', label: 'Id', width: 80 },
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
    public dialog: MatDialog,
    private etPluginsService: EtPluginsService,
  ) {}

  ngOnInit(): void {
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
  beforeunloadHandler(): void {
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
    this.timer = interval(1800);
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
          }
        });
      });
    }
  }

  initIfStarted(): void {
    this.testlinkService.isReady().subscribe(
      (ready: boolean) => {
        this.isRunning = ready;
        if (ready) {
          this.unsubscribe();
          this.startingInProcess = false;
          this.loadData();
        }
      },
      (error: Error) => console.log(error),
    );
  }

  startTestLink(): void {
    this.startingInProcess = true;
    this.startedFirstTime = true;
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
    if (this.startedFirstTime) {
      this.syncTestLink();
    } else {
      this.loadProjects();
    }
  }

  loadTestLinkUrl(): void {
    this.etPluginsService.getUrl(this.testLinkModel.name).subscribe((url: string) => {
      this.testLinkUrl = url;
    });
  }

  loadProjects(): void {
    this.testlinkService.getAllTestProjects().subscribe((projects: TestProjectModel[]) => {
      this.projectsList = [...projects];
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

  openDialog(): void {
    let dialogRef: MatDialogRef<CredentialsDialogComponent> = this.dialog.open(CredentialsDialogComponent, {
      height: '30%',
      width: '40%',
      data: this.testLinkModel,
    });
    dialogRef.afterClosed().subscribe((result) => {
      console.log(`Dialog closed: ${result}`);
    });
  }
}
