import { TitlesService } from '../shared/services/titles.service';
import { Component, EventEmitter, Input, OnDestroy, OnInit, Output, HostListener } from '@angular/core';
import { MatDialogRef } from '@angular/material';
import { ElastestEusDialog } from './elastest-eus.dialog';
import { ElastestEusDialogService } from './elastest-eus.dialog.service';
import { EusService } from './elastest-eus.service';
import { EusTestModel } from './elastest-eus-test-model';
import { ConfigurationService } from '../config/configuration-service.service';
import { AbstractTJobExecModel } from '../elastest-etm/models/abstract-tjob-exec-model';
import { TJobExecModel } from '../elastest-etm/tjob-exec/tjobExec-model';
import { ExternalTJobExecModel } from '../elastest-etm/external/external-tjob-execution/external-tjob-execution-model';
import { TdDataTableSortingOrder, TdDataTableService, ITdDataTableSortChangeEvent } from '@covalent/core';
import { isString } from '../shared/utils';

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
    { name: 'version', label: 'Version', sortable: false },
    { name: 'creationTime', label: 'Creation Time' },
    { name: 'actions', label: 'Actions', sortable: false },
  ];

  dateFields: string[] = ['creationTime'];
  sortBy: string = 'creationTime';
  sortOrder: TdDataTableSortingOrder = TdDataTableSortingOrder.Descending;

  activeBrowsersColumns: any[] = this.recordingColumns.concat([
    { name: 'status', label: 'Status' },
    { name: 'statusMsg', label: 'Info' },
  ]);

  activeBrowsers: EusTestModel[] = [];
  activeBrowsersMap: Map<string, number> = new Map<string, number>();
  recordings: EusTestModel[] = [];
  liveSession: EusTestModel;

  liveDialog: MatDialogRef<ElastestEusDialog>;

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

  @Input()
  abstractTJobExec: AbstractTJobExecModel;

  @Output()
  onInitComponent: EventEmitter<string> = new EventEmitter<string>();

  constructor(
    private titlesService: TitlesService,
    private eusService: EusService,
    private eusDialog: ElastestEusDialogService,
    private configurationService: ConfigurationService,
    private dataTableService: TdDataTableService,
  ) {}

  ngOnInit(): void {
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
      (data: any) => {
        this.initBrowsersByGiven(data.browsers);
        this.loading = false;
      },
      (error: Error) => console.error('Error getting EUS status: ' + error),
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

  ngOnDestroy(): void {
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
          // this.websocket.send('getSessions');
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
            if (entry && entry.id !== json.removeSession.id) {
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
      testModel.elastestExecutionData = jsonSession.elastestExecutionData;
    }
    return testModel;
  }

  addOrUpdateActiveBrowsers(sessionJson: any): void {
    let testModel: EusTestModel = this.getEusTestModelFromSessionJson(sessionJson);
    // If is a browser of execution
    if (this.abstractTJobExec && testModel.elastestExecutionData) {
      let isExecutionData: boolean = false;
      if (this.abstractTJobExec instanceof TJobExecModel && this.abstractTJobExec.tJob) {
        isExecutionData =
          testModel.elastestExecutionData['type'] === 'tJob' &&
          testModel.elastestExecutionData['tJobId'] === this.abstractTJobExec.tJob.id &&
          testModel.elastestExecutionData['tJobExecId'] === this.abstractTJobExec.id;
      } else if (this.abstractTJobExec instanceof ExternalTJobExecModel && this.abstractTJobExec.exTJob) {
        isExecutionData =
          testModel.elastestExecutionData['type'] === 'externalTJob' &&
          testModel.elastestExecutionData['tJobId'] === this.abstractTJobExec.exTJob.id &&
          testModel.elastestExecutionData['tJobExecId'] === this.abstractTJobExec.id;
      }
      if (isExecutionData) {
        this.addOrUpdateActiveBrowsersByTestModel(testModel);
      }
    } else {
      // normal
      this.addOrUpdateActiveBrowsersByTestModel(testModel);
    }
  }

  addOrUpdateActiveBrowsersByTestModel(testModel: EusTestModel): void {
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
      setTimeout(() => {
        console.log('Trying to reconnect to EUS WS');
        this.startWebSocket();
      }, 5000);
    }
  }

  sortRecordingsByDate(): void {
    try {
      this.recordings = Array.from(
        this.recordings.sort((recording1: EusTestModel, recording2: EusTestModel) => {
          let direction: number = 0;
          if (this.sortOrder === TdDataTableSortingOrder.Descending) {
            direction = 1;
          } else if (this.sortOrder === TdDataTableSortingOrder.Ascending) {
            direction = -1;
          }

          let time1: Date;
          let time2: Date;

          if (isString(recording1[this.sortBy])) {
            time1 = this.getDateFromEusCreationTime(recording1[this.sortBy]);
            time2 = this.getDateFromEusCreationTime(recording2[this.sortBy]);
          } else {
            time1 = recording1[this.sortBy];
            time2 = recording2[this.sortBy];
          }

          if (time1.getTime() < time2.getTime()) {
            return direction;
          } else if (time1.getTime() > time2.getTime()) {
            return -direction;
          } else {
            return direction;
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

      // Month is zero-based
      let fullYearObj: any = {
        day: fullYear.split('-')[0],
        month: Number(fullYear.split('-')[1]) - 1,
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
    let dialog: MatDialogRef<ElastestEusDialog> = this.eusDialog.getDialog(true);
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

  getRecordingUrl(data: string): string {
    return 'http://' + this.eusHost + ':' + this.eusPort + data;
  }

  opneRecordingInNewTab(testModel: EusTestModel): void {
    this.eusService.getRecording(testModel.id).subscribe(
      (data: string) => {
        window.open(this.getRecordingUrl(data));
      },
      (error: Error) => console.error(error),
    );
  }

  viewRecording(testModel: EusTestModel): void {
    this.eusService.getRecording(testModel.id).subscribe(
      (data: string) => {
        let videoUrl: string = this.getRecordingUrl(data);
        console.log('Video URL: ' + videoUrl);
        this.viewSession(videoUrl, testModel, ' - recorded test', 'video');
      },
      (error: Error) => console.error(error),
    );
  }

  deleteRecording(testModel: EusTestModel): void {
    this.eusService.deleteRecording(testModel.id).subscribe(
      (data: any) => {
        let entry: EusTestModel;
        let newTestData: EusTestModel[] = [];
        for (entry of this.recordings) {
          if (entry.id !== testModel.id) {
            newTestData.push(entry);
          }
        }
        this.recordings = Array.from(newTestData);
      },
      (error: Error) => console.error(error),
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
        (data: any) => {
          this.stopSession();
        },
        (error: Error) => {
          console.error(error);
          this.liveDialog = undefined;
        },
      );

      let extraCapabilities: any = { elastestTimeout: 0 };
      this.eusService.startSession(this.selectedBrowser, this.selectedVersion[this.selectedBrowser], extraCapabilities).subscribe(
        (eusTestModel: EusTestModel) => {
          this.sessionId = eusTestModel.id;
          this.eusService.getVncUrl(this.sessionId).subscribe(
            (url: string) => {
              this.liveDialog.componentInstance.loading = false;
              this.liveDialog.componentInstance.iframeUrl = url;
            },
            (error: Error) => console.error(error),
          );
        },
        (error: Error) => console.error(error),
      );
    } else {
      this.eusDialog.popUpMessage('Browser not selected', 'You need to chose one browsers to start a session').subscribe();
    }
  }

  stopSession(sessionId: string = this.sessionId): void {
    this.eusService.stopSession(sessionId).subscribe(
      (data: any) => {
        this.vncUrl = null;
        this.liveSession = undefined;
        this.liveDialog = undefined;
      },
      (error: Error) => console.error(error),
    );
  }

  selectBrowser(browser: string): void {
    this.selectedBrowser = browser;
    Object.keys(this.selectedVersion).forEach((key: string) => {
      if (key !== browser) {
        this.selectedVersion[key] = '';
      }
    });
  }

  clearVersion(): void {
    Object.keys(this.selectedVersion).forEach((key: string) => (this.selectedVersion[key] = ''));
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
      (data: any) => {
        this.initBrowsersByGiven(data);
        this.loading = false;
      },
      (error: Error) => {
        console.log(error);
        this.loading = false;
      },
    );
  }

  sortRecordings(sortEvent?: ITdDataTableSortChangeEvent): void {
    this.sortBy = sortEvent ? sortEvent.name : this.sortBy;
    this.sortOrder = sortEvent ? sortEvent.order : this.sortOrder;
    if (this.dateFields.indexOf(this.sortBy) > -1) {
      this.sortRecordingsByDate();
    } else {
      this.recordings = this.dataTableService.sortData(this.recordings, this.sortBy, this.sortOrder);
    }
  }
}
