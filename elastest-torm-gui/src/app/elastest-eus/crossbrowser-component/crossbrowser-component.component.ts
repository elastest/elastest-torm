import { Component, OnInit, Input, ViewChildren, QueryList, OnDestroy, ViewChild, HostListener } from '@angular/core';
import { EusService, BrowserVersionModel } from '../elastest-eus.service';
import { EusBowserSyncModel } from '../elastest-eus-browser-sync.model';
import { Subject, Observable, Subscription } from 'rxjs';
import { EtmMonitoringViewComponent } from '../../elastest-etm/etm-monitoring-view/etm-monitoring-view.component';
import { EusSessionInfoModel } from '../elastest-eus-sessioninfo.model';
import { BrowserCardComponentComponent } from '../browser-card-component/browser-card-component.component';
import { sleep, getErrorColor, getWarnColor } from '../../shared/utils';
import { EusTestModel } from '../elastest-eus-test-model';
import { ActivatedRoute, Params } from '@angular/router';
import { SelfAdjustableCardComponent } from '../../shared/ng-self-adjustable-components/self-adjustable-card/self-adjustable-card.component';

@Component({
  selector: 'etm-eus-crossbrowser-component',
  templateUrl: './crossbrowser-component.component.html',
  styleUrls: ['./crossbrowser-component.component.scss'],
})
export class CrossbrowserComponentComponent implements OnInit, OnDestroy {
  @ViewChildren('singleBrowser') browserCards: QueryList<BrowserCardComponentComponent>;
  @ViewChild('mainCard')
  mainCard: SelfAdjustableCardComponent;

  @Input()
  withBrowserSync: boolean;

  @Input()
  logsAndMetrics: EtmMonitoringViewComponent = undefined;

  @Input()
  browserSyncIdentifier: string;

  @Input()
  showSpinner: Function = this.showSpinnerDefault;

  @Input()
  fullscreenMode: boolean = false;

  isNested: boolean = true;

  browserCardMsg: string = 'Loading...';
  stoppingOrStopped: boolean = false;

  eusIp: string;
  eusPort: string | number;
  eusUrl: string;

  browserSync: EusBowserSyncModel;
  extraCapabilities: any;
  browserList: BrowserVersionModel[];
  sutUrl: string;
  live?: boolean;
  extraHosts?: string[];

  groupedSessions: EusSessionInfoModel[][] = [];

  logErrors: number = 0;
  logWarnings: number = 0;

  errorColor: string = getErrorColor();
  warnColor: string = getWarnColor();

  mouseKeyboardEvents: Subject<MouseEvent> = new Subject<MouseEvent>();
  mouseKeyboardEventsObs: Observable<MouseEvent>;
  mouseKeyboardEventsSubscription: Subscription;

  urlToNavigate: string = '';

  constructor(private eusService: EusService, private route: ActivatedRoute) {}

  ngOnInit(): void {
    if (this.browserSyncIdentifier) {
      this.eusService.getCrossbrowserSession(this.browserSyncIdentifier).subscribe(
        (browserSync: EusBowserSyncModel) => {
          let eusUrl: string = this.eusService.getEusUrl();
          let eusURLObj: URL = new URL(eusUrl);

          this.initEusData(eusURLObj.hostname, eusURLObj.port, eusUrl);
          this.initByBrowserSync(browserSync);
        },
        (error: Error) => {
          console.error(error);
        },
      );
    } else {
      if (this.route.params !== null || this.route.params !== undefined) {
        this.route.params.subscribe((params: Params) => {
          // Component page
          if (params.crossbrowserId) {
            this.browserSyncIdentifier = params.crossbrowserId;
            this.isNested = false;
            this.ngOnInit();
          }
        });
      }
    }
  }

  ngOnDestroy(): void {
    this.clear();
  }

  @HostListener('window:beforeunload')
  beforeunloadHandler(): void {
    // On window closed leave session
    this.clear();
  }

  clear(): void {
    // Only remove if comes from it's own page
    if (this.browserSyncIdentifier) {
      this.unsubscribeToMouseEvents();
    }
  }

  initEusData(eusIp: string, eusPort: string | number, eusUrl: string): void {
    this.eusIp = eusIp;
    this.eusPort = eusPort;
    this.eusUrl = eusUrl;
    this.eusService.setEusUrl(this.eusUrl);
  }

  createGroupedLogsList(): void {
    let defaultGroupNum: number = 2;
    this.groupedSessions = this.createGroupedArray(this.browserSync.sessions, defaultGroupNum);
  }

  createGroupedArray(arr: EusSessionInfoModel[], chunkSize: number): EusSessionInfoModel[][] {
    let groups: EusSessionInfoModel[][] = [];
    let i: number = 0;
    for (i = 0; i < arr.length; i += chunkSize) {
      groups.push(arr.slice(i, i + chunkSize));
    }
    return groups;
  }

  async initByBrowserSync(browserSync: EusBowserSyncModel): Promise<void> {
    this.browserSync = browserSync;
    this.createGroupedLogsList();
    await this.initBrowserCards();
    this.hideCardContentBackground();
  }

  startCrossbrowser(
    extraCapabilities: any,
    browserList: BrowserVersionModel[],
    sutUrl: string,
    live?: boolean,
    extraHosts?: string[],
    acceptInsecure: boolean = false,
  ): Observable<EusBowserSyncModel> {
    let _obs: Subject<any> = new Subject<any>();
    let obs: Observable<any> = _obs.asObservable();

    this.browserList = browserList;
    this.extraCapabilities = extraCapabilities;
    this.sutUrl = sutUrl;
    this.urlToNavigate = sutUrl;
    this.live = live;
    this.extraHosts = extraHosts;

    // Async/await to wait for initialization
    this.eusService
      .startCrossbrowserSession(
        browserList,
        sutUrl,
        extraCapabilities,
        live,
        extraHosts,
        acceptInsecure,
        this.withBrowserSync,
        this.fullscreenMode,
      )
      .subscribe(
        async (browserSync: EusBowserSyncModel) => {
          await this.initByBrowserSync(browserSync);
          _obs.next(browserSync);
        },
        (error: Error) => {
          _obs.error(error);
        },
      );
    return obs;
  }

  async initBrowserCards(): Promise<any> {
    if (this.browserCards.toArray().length !== this.browserSync.sessions.length) {
      await sleep(2000).then(async () => {
        await this.initBrowserCards();
      });
    } else {
      let position: number = 0;
      for (let browserCard of this.browserCards.toArray()) {
        let session: EusSessionInfoModel = this.browserSync.sessions[position];

        browserCard.initEusData(this.eusIp, this.eusPort, this.eusUrl);
        browserCard.startWebSocket();

        browserCard.initBrowserCreationInfo(session.browser, session.version, this.extraCapabilities, this.live, this.extraHosts);

        let eusTestModel: EusTestModel = new EusTestModel();
        eusTestModel.initFromEusSessionInfoModel(session);
        browserCard.initBrowserInstanceInfo(eusTestModel);
        browserCard.getAndInitVncUrl();
        // Navigation is not necessary because is loaded in backend
        position++;
      }
      this.subscribeToMouseEvents();
    }
  }

  stopCrossbrowser(): Observable<any> {
    let _obs: Subject<any> = new Subject<any>();
    let obs: Observable<any> = _obs.asObservable();
    this.stoppingOrStopped = true;
    if (this.browserSync) {
      this.eusService.stopCrossbrowserSession(this.browserSync.identifier).subscribe(
        (ok: any) => {
          _obs.next(ok);
        },
        (error: Error) => _obs.error(error),
      );
    } else {
      _obs.error('There is not any browserSync object to stop');
    }
    return obs;
  }

  startRecording(videoNamePrefix: string): Observable<any> {
    let _obs: Subject<any> = new Subject<any>();
    let obs: Observable<any> = _obs.asObservable();
    if (this.browserCards.toArray().length !== this.browserSync.sessions.length) {
      sleep(2000).then(() => {
        return this.startRecording(videoNamePrefix);
      });
    } else {
      this.startBrowserListRecording(videoNamePrefix, [...this.browserCards.toArray()], _obs);
    }
    return obs;
  }

  startBrowserListRecording(videoNamePrefix: string, browsers: BrowserCardComponentComponent[], _obs: Subject<any>): void {
    if (browsers && browsers.length > 0) {
      let browserCard: BrowserCardComponentComponent = browsers.shift();
      browserCard.startRecording(videoNamePrefix).subscribe(
        (ok: any) => {
          this.startBrowserListRecording(videoNamePrefix, browsers, _obs);
        },
        (error: Error) => {
          console.log(error);
        },
      );
    } else {
      _obs.next(true);
    }
  }

  stopRecording(): Observable<any> {
    let _obs: Subject<any> = new Subject<any>();
    let obs: Observable<any> = _obs.asObservable();
    if (this.browserCards.toArray().length !== this.browserSync.sessions.length) {
      sleep(2000).then(() => {
        return this.stopRecording();
      });
    } else {
      this.stopBrowserListRecording([...this.browserCards.toArray()], _obs);
    }
    return obs;
  }

  stopBrowserListRecording(browsers: BrowserCardComponentComponent[], _obs: Subject<any>): void {
    if (browsers && browsers.length > 0) {
      let browserCard: BrowserCardComponentComponent = browsers.shift();
      browserCard.stopRecording().subscribe(
        (ok: any) => {
          this.stopBrowserListRecording(browsers, _obs);
        },
        (error: Error) => {
          console.log(error);
        },
      );
    } else {
      _obs.next(true);
    }
  }

  deleteRecording(): Observable<any> {
    let _obs: Subject<any> = new Subject<any>();
    let obs: Observable<any> = _obs.asObservable();
    if (this.browserCards.toArray().length !== this.browserSync.sessions.length) {
      sleep(2000).then(() => {
        return this.deleteRecording();
      });
    } else {
      this.deleteBrowserListRecording([...this.browserCards.toArray()], _obs);
    }
    return obs;
  }

  deleteBrowserListRecording(browsers: BrowserCardComponentComponent[], _obs: Subject<any>): void {
    if (browsers && browsers.length > 0) {
      let browserCard: BrowserCardComponentComponent = browsers.shift();
      browserCard.deleteRecording().subscribe(
        (ok: any) => {
          this.deleteBrowserListRecording(browsers, _obs);
        },
        (error: Error) => {
          console.log(error);
        },
      );
    } else {
      _obs.next(true);
    }
  }

  setLogsAndMetrics(logsAndMetrics: EtmMonitoringViewComponent): void {
    this.logsAndMetrics = logsAndMetrics;
  }

  updateMsg(msg: string): void {
    this.browserCardMsg = msg;
    if (this.browserCards && this.browserSync && this.browserSync.sessions) {
      for (let browserCard of this.browserCards.toArray()) {
        browserCard.updateMsg(msg);
      }
    }
  }

  stopWebsocket(): void {
    if (this.browserCards && this.browserSync && this.browserSync.sessions) {
      if (this.browserCards.toArray().length !== this.browserSync.sessions.length) {
        sleep(2000).then(() => {
          this.stopWebsocket();
        });
      } else {
        for (let browserCard of this.browserCards.toArray()) {
          browserCard.stopWebsocket();
        }
      }
    }
  }

  resizeBrowsers($event): void {
    if (this.browserCards && this.browserSync && this.browserSync.sessions) {
      if (this.browserCards.toArray().length !== this.browserSync.sessions.length) {
        sleep(2000).then(() => {
          this.resizeBrowsers($event);
        });
      } else {
        for (let browserCard of this.browserCards.toArray()) {
          browserCard.resizeBrowser($event);
        }
      }
    }
  }

  openInNewTab(): void {
    if (this.browserSync && this.browserSync.identifier) {
      let url: string = '/#/eus/crossbrowser/' + this.browserSync.identifier;
      window.open(url);
    }
  }

  getLogsErrors(): number {
    this.logErrors = this.logsAndMetrics.getLogsErrors();
    return this.logErrors;
  }

  getLogsWarnings(): number {
    this.logWarnings = this.logsAndMetrics.getLogsWarnings();
    return this.logWarnings;
  }

  showSpinnerDefault(): boolean {
    return false;
  }

  hideCardContentBackground(): boolean {
    let hide: boolean = this.groupedSessions.length > 0 && !this.stoppingOrStopped;
    if (this.mainCard) {
      this.mainCard.setNoContentBackground(hide);
    }
    return hide;
  }

  subscribeToMouseEvents(): void {
    this.mouseKeyboardEventsObs = this.mouseKeyboardEvents.asObservable();
    this.mouseKeyboardEventsSubscription = this.mouseKeyboardEventsObs.subscribe((mouseEvent: MouseEvent) => {
      if (this.browserCards && this.browserCards.toArray().length > 0) {
        for (let browserCard of this.browserCards.toArray()) {
          browserCard.emitMouseEvent(mouseEvent);
        }
      }
    });
  }
  unsubscribeToMouseEvents(): void {
    if (this.mouseKeyboardEventsSubscription) {
      this.mouseKeyboardEventsSubscription.unsubscribe();
      this.mouseKeyboardEventsSubscription = undefined;
    }
  }

  navigateToUrl(urlToOpen: string): void {
    if (this.browserCards && this.browserCards.toArray().length > 0) {
      for (let browserCard of this.browserCards.toArray()) {
        browserCard.navigateToUrl(urlToOpen).subscribe();
      }
    }
  }

  dispatchEvent(e: Event): void {
    if (this.browserCards) {
      for (let browserCard of this.browserCards.toArray()) {
        browserCard.dispatchEvent(e);
      }
    }
  }
}
