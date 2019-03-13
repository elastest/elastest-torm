import { CardLogModel } from '../../shared/logs-view/models/card-log.model';
import { Observable, Subscription } from 'rxjs/Rx';
import { TitlesService } from '../../shared/services/titles.service';
import { ConfigurationService } from '../../config/configuration-service.service';
import { Component, HostListener, OnInit, OnDestroy } from '@angular/core';
import { CoreServiceModel } from '../models/core-service.model';
import { VersionInfo } from '../models/version-info.model';
import { PopupService } from '../../shared/services/popup.service';
import { interval } from 'rxjs';

@Component({
  selector: 'etm-help',
  templateUrl: './help.component.html',
  styleUrls: ['./help.component.scss'],
})
export class HelpComponent implements OnInit, OnDestroy {
  coreServices: CoreServiceModel[] = [];
  etCurrentVersion: string;

  autorefreshEnabled: boolean = true;
  lastRefresh: Date;
  coreServicesSubscription: Subscription;
  loadingCoreServices: boolean = false;

  coreServiceLogs: CardLogModel;
  loadingLogs: boolean = false;
  numberOfLogs: number = 100;
  subscribeToNextLogs: boolean = false;

  // SuT Data
  coreServiceColumns: any[] = [
    { name: 'status', label: 'Status' },
    { name: 'name', label: 'Service Name' },
    { name: 'imageName', label: 'Image Name' },
    { name: 'imageDate', label: 'Image Date' },
    { name: 'versionInfo.tag', label: 'Version' },
    { name: 'versionInfo.commitDate', label: 'Commit Date' },
    { name: 'versionInfo.commitId', label: 'Commit Id', width: 340 },
    { name: 'networks', label: 'Networks' },
    { name: 'containerNames', label: 'Container Names' },
    { name: 'options', label: 'Options', width: 90 },
  ];

  constructor(
    private titlesService: TitlesService,
    private configurationService: ConfigurationService,
    private popupService: PopupService,
  ) {}

  ngOnInit(): void {
    this.titlesService.setHeadTitle('Help');
    this.init();
    this.startCoreServicesSubscription();
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
    this.coreServicesSubscription.unsubscribe();
    this.subscribeToNextLogs = false;
  }

  init(): void {
    this.loadingCoreServices = true;
    this.configurationService.getCoreServicesInfo().subscribe(
      (coreServices: CoreServiceModel[]) => {
        this.coreServices = coreServices;
        this.initCurrentETVersion();
        this.lastRefresh = new Date();
        this.loadingCoreServices = false;
      },
      (error: Error) => {
        console.log(error);
        this.loadingCoreServices = false;
      },
    );
  }

  initCurrentETVersion(): void {
    for (let coreService of this.coreServices) {
      if (coreService.isPlatformImage()) {
        this.etCurrentVersion = coreService.versionInfo.tag;
        if (this.etCurrentVersion === undefined || this.etCurrentVersion === 'latest') {
          this.etCurrentVersion = 'Latest Stable';
        }
      }
    }
  }

  switchAutorefresh(enableAutorefresh: boolean): void {
    this.autorefreshEnabled = enableAutorefresh;
  }

  startCoreServicesSubscription(): void {
    let timer: Observable<number> = interval(6000);
    this.coreServicesSubscription = timer.subscribe(() => {
      if (this.autorefreshEnabled) {
        this.init();
        this.loadNextCoreServiceLogs();
      }
    });
  }

  loadCoreServiceLogs(coreServiceName: string): void {
    this.loadingLogs = true;
    this.coreServiceLogs = new CardLogModel();
    this.coreServiceLogs.previousFunctionObj = { function: this.loadPreviousLogs.bind(this) };
    // this.coreServiceLogs.hidePrevBtn = true;
    this.coreServiceLogs.name = coreServiceName;
    this.configurationService.getSomeCoreServiceLogs(coreServiceName, this.numberOfLogs, false).subscribe(
      (logs: string) => {
        this.coreServiceLogs.traces = this.configurationService.logsWithTimestampToLogViewTraces(logs);
        this.loadingLogs = false;
        this.subscribeToNextLogs = true;
      },
      (error: Error) => {
        this.popupService.openSnackBar('Error on get ' + coreServiceName + ' logs');
        console.log(error);
        this.loadingLogs = false;
        this.subscribeToNextLogs = false;
      },
    );
  }

  loadNextCoreServiceLogs(): void {
    if (this.subscribeToNextLogs && this.coreServiceLogs !== undefined && this.coreServiceLogs.name !== undefined) {
      if (this.coreServiceLogs.traces && this.coreServiceLogs.traces.length > 0) {
        let last: number = this.coreServiceLogs.traces.length - 1;
        let lastDateString: string = this.coreServiceLogs.traces[last].timestamp;
        if (lastDateString) {
          let lastDate: Date = new Date(lastDateString);
          // Time in seconds
          let since: number = Math.trunc(lastDate.getTime() / 1000);
          this.configurationService.getCoreServiceLogsSince(this.coreServiceLogs.name, since, false).subscribe(
            (logs: string) => {
              let logsAsTraces: any[] = this.configurationService.logsWithTimestampToLogViewTraces(logs);
              logsAsTraces = this.discardDuplicatedTraces(logsAsTraces, lastDate, this.coreServiceLogs.traces[last].message);
              this.coreServiceLogs.traces = this.coreServiceLogs.traces.concat(logsAsTraces);
            },
            (error: Error) => {
              this.popupService.openSnackBar('Error on get next ' + this.coreServiceLogs.name + ' logs');
              console.log(error);
            },
          );
        }
      }
    }
  }

  discardDuplicatedTraces(traces: any[], lastDate: Date, lastMessage: string): any[] {
    let firstDateString: string = traces[0].timestamp;
    let newTraces: any[] = traces;
    if (firstDateString) {
      let firstDate: Date = new Date(firstDateString);
      if (firstDate.getTime() < lastDate.getTime()) {
        newTraces = this.discardDuplicatedTraces(traces.slice(1), lastDate, lastMessage);
      } else if (firstDate.getTime() === lastDate.getTime()) {
        if (traces[0].message === lastMessage) {
          newTraces = traces.slice(1);
        } else {
          newTraces = this.discardDuplicatedTraces(traces.slice(1), lastDate, lastMessage);
        }
      }
    }
    return newTraces;
  }

  loadPreviousLogs(): void {
    this.loadingLogs = true;
    this.subscribeToNextLogs = false;

    this.configurationService.getAllCoreServiceLogs(this.coreServiceLogs.name, false).subscribe(
      (logs: string) => {
        this.coreServiceLogs.traces = this.configurationService.logsWithTimestampToLogViewTraces(logs);
        this.loadingLogs = false;
        this.subscribeToNextLogs = true;
      },
      (error: Error) => {
        this.popupService.openSnackBar('Error on get ' + this.coreServiceLogs.name + ' logs');
        console.log(error);
        this.loadingLogs = false;
        this.subscribeToNextLogs = false;
      },
    );
  }

  removeLogCard(): void {
    this.coreServiceLogs = undefined;
    this.loadingLogs = false;
  }
}
