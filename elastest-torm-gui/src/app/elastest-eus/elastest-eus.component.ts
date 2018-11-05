import { TitlesService } from '../shared/services/titles.service';
import { Component, EventEmitter, Input, OnDestroy, OnInit, Output, HostListener } from '@angular/core';
import { MdDialogRef } from '@angular/material';
import { ElastestEusDialog } from './elastest-eus.dialog';
import { ElastestEusDialogService } from './elastest-eus.dialog.service';
import { EusService } from './elastest-eus.service';
import { EusTestModel } from './elastest-eus-test-model';
import { ConfigurationService } from '../config/configuration-service.service';
import { Response } from '@angular/http';

@Component({
  selector: 'app-elastest-eus',
  templateUrl: './elastest-eus.component.html',
  styleUrls: ['./elastest-eus.component.scss'],
})
export class ElastestEusComponent implements OnInit, OnDestroy {
  componentTitle: string = 'ElasTest User Emulator Service (EUS)';
  sessionId: string = '';

  browser: string = '';
  vncUrl: string = '';
  creationTime: string = '';
  websocket: WebSocket;

  selectedBrowser: string;
  selectedVersion: object = {};

  browserVersions: object;
  browserNamesList: string[];

  loading: boolean = true;

  manuallyClosed: boolean = false;

  recordingColumns: any[] = [
    { name: 'id', label: 'Session id' },
    { name: 'browser', label: 'Browser' },
    { name: 'version', label: 'Version' },
    { name: 'creationTime', label: 'Creation Time' },
    { name: 'actions', label: 'Actions' },
  ];

  activeBrowsersColumns: any[] = this.recordingColumns.concat([
    { name: 'status', label: 'Status' },
    { name: 'statusMsg', label: 'Info' },
  ]);

  activeBrowsers: EusTestModel[] = [];
  activeBrowsersMap: Map<string, number> = new Map<string, number>();
  recordings: EusTestModel[] = [];
  liveSession: EusTestModel;

  liveDialog: MdDialogRef<ElastestEusDialog>;

  @Input()
  eusUrl: string = 'http://localhost:8040/eus/v1/';

  @Input()
  eusHost: string = 'localhost';

  @Input()
  eusPort: number = 8040;

  // If standalone, is live
  @Input()
  standalone: boolean = true;

  @Input()
  isNested: boolean = false;

  @Output()
  onInitComponent: EventEmitter<string> = new EventEmitter<string>();

  constructor(
    private titlesService: TitlesService,
    private eusService: EusService,
    private eusDialog: ElastestEusDialogService,
    private configurationService: ConfigurationService,
  ) {}

  ngOnInit() {
    if (!this.isNested) {
      this.titlesService.setHeadTitle('Web Browsers');
    }

    if (!this.isNested && this.configurationService.configModel.eusServiceUrl && this.standalone) {
      console.log('Uses data from backend.');
      this.eusService.setEusUrl(this.configurationService.configModel.eusServiceUrl);
      this.eusService.setEusHost(this.configurationService.configModel.eusHost);
      this.eusPort = +this.configurationService.configModel.eusPort;
      this.eusHost = this.configurationService.configModel.eusHost;
      this.eusUrl = this.configurationService.configModel.eusServiceUrl;
    } else {
      console.log('Uses default or passed arguments.', this.eusHost, this.eusUrl);
      this.eusService.setEusUrl(this.eusUrl);
      this.eusService.setEusHost(this.eusHost);
    }

    this.eusService.getStatus().subscribe(
      (ok: Response) => {
        this.initBrowsersByGiven(ok.json().browsers);
        this.loading = false;
      },
      (error) => console.error('Error getting EUS status: ' + error),
    );

    this.startWebSocket();
  }

  initBrowsersByGiven(obj: object): void {
    this.browserVersions = obj;
    this.browserNamesList = Object.keys(this.browserVersions);
    if (this.browserNamesList.length > 0) {
      this.selectBrowser(this.browserNamesList[0]);
    }
  }

  ngOnDestroy() {
    this.end();
  }

  @HostListener('window:beforeunload')
  beforeunloadHandler() {
    this.end();
  }

  end(): void {
    if (this.websocket) {
      this.manuallyClosed = true;
      this.websocket.close();
    }
    if (this.sessionId !== null && this.sessionId !== undefined && this.sessionId !== '') {
      this.stopSession();
    }
  }

  startWebSocket(): void {
    if (!this.websocket) {
      if (!this.isNested && this.configurationService.configModel.eusServiceUrl && this.standalone) {
        this.websocket = new WebSocket(this.configurationService.configModel.eusWebSocketUrl);
      } else {
        this.websocket = new WebSocket(this.eusService.getEusWsByHostAndPort(this.eusHost, this.eusPort));
      }

      this.websocket.onopen = () => {
        if (this.standalone) {
          this.websocket.send('getLiveSessions');
        } else {
          this.websocket.send('getSessions');
        }
        this.websocket.send('getRecordings');
      };

      this.websocket.onclose = () => this.reconnect();

      this.websocket.onmessage = (message) => {
        let json: any = JSON.parse(message.data);

        // If new normal session and not standalone (tJob)
        if (json.newSession && !this.standalone) {
          this.addOrUpdateActiveBrowsers(json.newSession);
        } else if (json.newLiveSession && this.standalone) {
          // If live session and standalone
          this.addOrUpdateActiveBrowsers(json.newLiveSession);
          // new live session
          this.liveSession = this.getEusTestModelFromSessionJson(json.newLiveSession);
          if (
            this.liveDialog !== undefined &&
            this.liveDialog !== null &&
            this.liveDialog.componentInstance !== undefined &&
            this.liveDialog.componentInstance !== null
          ) {
            this.liveDialog.componentInstance.testModel = this.liveSession;
          }
        } else if (json.recordedSession) {
          let testModel: EusTestModel = this.getEusTestModelFromSessionJson(json.recordedSession);
          if (testModel.live === undefined || testModel.live === null || testModel.live) {
            this.recordings.push(testModel);
            this.sortRecordings();
          }
        } else if (json.removeSession) {
          let entry: EusTestModel;
          let newTestData: EusTestModel[] = [];
          for (entry of this.activeBrowsers) {
            if (entry.id !== json.removeSession.id) {
              newTestData.push(entry);
            }
          }
          this.activeBrowsers = Array.from(newTestData);
        }
      };
    }
  }

  getEusTestModelFromSessionJson(jsonSession: any): EusTestModel {
    let testModel: EusTestModel = new EusTestModel();
    if (jsonSession !== undefined && jsonSession !== null) {
      testModel.id = jsonSession.id;
      testModel.browser = jsonSession.browser;
      testModel.version = jsonSession.version;
      testModel.creationTime = jsonSession.creationTime;
      testModel.url = jsonSession.url;
      testModel.hubContainerName = jsonSession.hubContainerName;
      testModel.status = jsonSession.status;
      testModel.statusMsg = jsonSession.statusMsg;
      testModel.live = jsonSession.live;
    }
    return testModel;
  }

  addOrUpdateActiveBrowsers(sessionJson: any): void {
    let testModel: EusTestModel = this.getEusTestModelFromSessionJson(sessionJson);
    let position: number;
    if (this.activeBrowsersMap.has(testModel.hubContainerName)) {
      position = this.activeBrowsersMap.get(testModel.hubContainerName);
      this.activeBrowsers[position] = testModel;
    } else {
      position = this.activeBrowsers.push(testModel) - 1;
    }
    this.activeBrowsers = Array.from(this.activeBrowsers);

    this.activeBrowsersMap.set(testModel.hubContainerName, position);
  }

  reconnect(): void {
    if (!this.manuallyClosed) {
      // try to reconnect websocket in 5 seconds
      setTimeout(function() {
        console.log('Trying to reconnect to EUS WS');
        this.startWebSocket();
      }, 5000);
    }
  }

  sortRecordings(): void {
    try {
      this.recordings = Array.from(
        this.recordings.sort((recording1: EusTestModel, recording2: EusTestModel) => {
          let time1: Date = this.getDateFromEusCreationTime(recording1.creationTime);
          let time2: Date = this.getDateFromEusCreationTime(recording2.creationTime);

          if (time1 > time2) {
            return -1;
          } else if (time1 < time2) {
            return 1;
          } else {
            return 0;
          }
        }),
      );
    } catch (e) {
      console.log(e);
      this.recordings = Array.from(this.recordings);
    }
  }

  getDateFromEusCreationTime(time: string): Date {
    // time format:  03-09-2018 01:46:49 CEST
    if (time === undefined) {
      return undefined;
    }
    try {
      let date: Date = new Date();

      let fullYear: string = time.split(' ')[0];

      let fullYearObj: any = {
        day: fullYear.split('-')[0],
        month: fullYear.split('-')[1],
        year: fullYear.split('-')[2],
      };

      let fullTime: string = time.split(' ')[1];
      let fullTimeObj: any = {
        hour: fullTime.split(':')[0],
        min: fullTime.split(':')[1],
        sec: fullTime.split(':')[2],
      };

      date.setFullYear(fullYearObj.year, fullYearObj.month, fullYearObj.day);

      date.setHours(fullTimeObj.hour, fullTimeObj.min, fullTimeObj.sec);
      return date;
    } catch (e) {
      console.log(e);
    }
  }

  viewSession(url: string, testModel: EusTestModel, titleSuffix: string, sessionType: 'live' | 'video' = 'live'): void {
    let dialog: MdDialogRef<ElastestEusDialog> = this.eusDialog.getDialog(true);
    let title: string = this.capitalize(testModel.browser) + ' ' + testModel.version;
    title += titleSuffix;
    dialog.componentInstance.title = title;
    dialog.componentInstance.iframeUrl = url;
    dialog.componentInstance.sessionType = sessionType;
    dialog.componentInstance.closeButton = true;
  }

  getLiveUrl(url: string): void {
    window.open(url);
  }

  getRecordingUrl(testModel: EusTestModel): void {
    this.eusService.getRecording(testModel.id).subscribe(
      (ok: Response) => {
        window.open('http://' + this.eusHost + ':' + this.eusPort + ok.text());
      },
      (error) => console.error(error),
    );
  }

  viewRecording(testModel: EusTestModel): void {
    this.eusService.getRecording(testModel.id).subscribe(
      (ok: Response) => {
        let videoUrl: string = 'http://' + this.eusHost + ':' + this.eusPort + ok.text();
        console.log('Video URL: ' + videoUrl);
        this.viewSession(videoUrl, testModel, ' - recorded test', 'video');
      },
      (error) => console.error(error),
    );
  }

  deleteRecording(testModel: EusTestModel): void {
    this.eusService.deleteRecording(testModel.id).subscribe(
      (ok: Response) => {
        let entry: EusTestModel;
        let newTestData: EusTestModel[] = [];
        for (entry of this.recordings) {
          if (entry.id !== testModel.id) {
            newTestData.push(entry);
          }
        }
        this.recordings = Array.from(newTestData);
      },
      (error) => console.error(error),
    );
  }

  startSession(): void {
    if (this.selectedBrowser) {
      this.liveDialog = this.eusDialog.getDialog(true);
      let message: string = this.capitalize(this.selectedBrowser);

      if (this.selectedVersion[this.selectedBrowser]) {
        message += ' ' + this.selectedVersion[this.selectedBrowser];
      }
      message += ' - live session';
      this.liveDialog.componentInstance.title = message;
      this.liveDialog.componentInstance.message = '';
      this.liveDialog.componentInstance.loading = true;
      this.liveDialog.componentInstance.closeButton = true;
      this.liveDialog.componentInstance.testModel = this.liveSession;

      this.liveDialog.afterClosed().subscribe(
        (ok: Response) => {
          this.stopSession();
        },
        (error) => {
          console.error(error);
          this.liveDialog = undefined;
        },
      );

      this.eusService.startSession(this.selectedBrowser, this.selectedVersion[this.selectedBrowser]).subscribe(
        (eusTestModel: EusTestModel) => {
          this.sessionId = eusTestModel.id;
          this.eusService.getVncUrl(this.sessionId).subscribe(
            (url) => {
              this.liveDialog.componentInstance.loading = false;
              this.liveDialog.componentInstance.iframeUrl = url;
            },
            (error) => console.error(error),
          );
        },
        (error) => console.error(error),
      );
    } else {
      this.eusDialog.popUpMessage('Browser not selected', 'You need to chose one browsers to start a session').subscribe();
    }
  }

  stopSession(): void {
    this.eusService.stopSession(this.sessionId).subscribe(
      (ok: Response) => {
        this.vncUrl = null;
        this.liveSession = undefined;
        this.liveDialog = undefined;
      },
      (error) => console.error(error),
    );
  }

  selectBrowser(browser: string): void {
    this.selectedBrowser = browser;
    Object.keys(this.selectedVersion).forEach((key) => {
      if (key !== browser) {
        this.selectedVersion[key] = '';
      }
    });
  }

  clearVersion(): void {
    Object.keys(this.selectedVersion).forEach((key) => (this.selectedVersion[key] = ''));
  }

  capitalize(value: any): any {
    if (value) {
      return value.charAt(0).toUpperCase() + value.slice(1);
    }
    return value;
  }

  refreshBrowsers(): void {
    this.loading = true;

    this.eusService.getBrowsers().subscribe(
      (ok: Response) => {
        this.initBrowsersByGiven(ok.json());
        this.loading = false;
      },
      (error: Error) => {
        console.log(error);
        this.loading = false;
      },
    );
  }
}
