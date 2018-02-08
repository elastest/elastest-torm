import { TitlesService } from '../shared/services/titles.service';
import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { MdDialogRef, MdDialog, MdDialogConfig } from '@angular/material';
import { ElastestEusDialog } from './elastest-eus.dialog';
import { ElastestEusDialogService } from './elastest-eus.dialog.service';
import { EusService } from './elastest-eus.service';
import { EusTestModel } from './elastest-eus-test-model';
import { ConfigurationService } from '../config/configuration-service.service';


@Component({
  selector: 'app-elastest-eus',
  templateUrl: './elastest-eus.component.html',
  styleUrls: ['./elastest-eus.component.scss'],
})
export class ElastestEusComponent implements OnInit, OnDestroy {
  componentTitle: string = "ElasTest User Emulator Service (EUS)";
  sessionId: string = "";

  browser: string = "";
  vncUrl: string = "";
  creationTime: string = "";
  websocket: WebSocket;

  selectedBrowser: string;
  selectedVersion: object = {};

  browserVersions: object;
  browserVersionsKeys: object;

  testColumns: any[] = [
    { name: 'id', label: 'Session id' },
    { name: 'browser', label: 'Browser' },
    { name: 'version', label: 'Version' },
    { name: 'creationTime', label: 'Creation Time' },
    { name: 'url', label: 'Actions' }
  ];

  testData: EusTestModel[] = [];

  recordings: EusTestModel[] = [];

  @Input()
  eusUrl: string = "http://localhost:8040/eus/v1/";

  @Input()
  eusHost: string = "localhost";

  @Input()
  eusPort: number = 8040;

  @Input()
  standalone: boolean = true;

  @Input()
  isNested: boolean = false;

  @Output()
  onInitComponent = new EventEmitter<string>();

  constructor(private titlesService: TitlesService, private eusService: EusService,
    private eusDialog: ElastestEusDialogService, private configurationService: ConfigurationService) { }

  ngOnInit() {
    if (!this.isNested) {
      this.titlesService.setHeadAndTopTitle('Web Browsers');
    }
    if (this.configurationService.configModel.eusServiceUrl && this.standalone) {
      console.log("Uses data from backend.");
      this.eusService.setEusUrl(this.configurationService.configModel.eusServiceUrl);
      this.eusService.setEusHost(this.configurationService.configModel.eusHost);
      this.eusPort = +this.configurationService.configModel.eusPort;
      this.eusHost = this.configurationService.configModel.eusHost;
    } else {
      console.log("Uses default or passed arguments.");
      this.eusService.setEusUrl(this.eusUrl);
      this.eusService.setEusHost(this.eusHost);
    }

    this.eusService.getStatus().subscribe(
      ok => {
        this.browserVersions = ok.json().browsers;
        this.browserVersionsKeys = Object.keys(this.browserVersions);
      },
      error => console.error(error)
    );

    if (!this.websocket) {
      if (this.configurationService.configModel.eusServiceUrl && this.standalone) {
        this.websocket = new WebSocket(this.configurationService.configModel.eusWebSocketUrl);
      } else {
        this.websocket = new WebSocket("ws://" + this.eusHost + ":" + this.eusPort + "/eus/v1/eus-ws");
      }

      this.websocket.onopen = () => this.websocket.send("getSessions");
      this.websocket.onopen = () => this.websocket.send("getRecordings");

      this.websocket.onmessage = (message) => {
        let json = JSON.parse(message.data);

        if (json.newSession) {
          let testModel: EusTestModel = new EusTestModel();
          testModel.id = json.newSession.id;
          testModel.browser = json.newSession.browser;
          testModel.version = json.newSession.version;
          testModel.creationTime = json.newSession.creationTime;
          testModel.url = json.newSession.url;
          this.testData.push(testModel);
          this.testData = Array.from(this.testData);
        }
        else if (json.recordedSession) {
          let testModel: EusTestModel = new EusTestModel();
          testModel.id = json.recordedSession.id;
          testModel.browser = json.recordedSession.browser;
          testModel.version = json.recordedSession.version;
          testModel.creationTime = json.recordedSession.creationTime;
          this.recordings.push(testModel);
          this.recordings = Array.from(this.recordings);
        }
        else if (json.removeSession) {
          let entry: EusTestModel;
          let newTestData: EusTestModel[] = [];
          for (entry of this.testData) {
            if (entry.id !== json.removeSession.id) {
              newTestData.push(entry);
            }
          }
          this.testData = Array.from(newTestData);
        }
      };
    }
  }

  ngOnDestroy() {
    if (this.websocket) {
      this.websocket.close();
    }
  }

  viewSession(url: string, testModel: EusTestModel, titleSuffix: string) {
    let dialog: MdDialogRef<ElastestEusDialog> = this.eusDialog.getDialog(true);
    let title: string = this.capitalize(testModel.browser) + " " + testModel.version;
    title += titleSuffix;
    dialog.componentInstance.title = title;
    dialog.componentInstance.iframeUrl = url;
    dialog.componentInstance.closeButton = true;
  }

  getLiveUrl(url: string) {
    window.open(url);
  }

  getRecordingUrl(testModel: EusTestModel) {
    this.eusService.getRecording(testModel.id).subscribe(
      ok => {
        window.open("http://" + this.eusHost + ":" + this.eusPort + ok.text());
      },
      error => console.error(error)
    );
  }

  viewRecording(testModel: EusTestModel) {
    this.eusService.getRecording(testModel.id).subscribe(
      ok => {
        let videoUrl = "http://" + this.eusHost + ":" + this.eusPort + ok.text();
        console.log("Video URL: " + videoUrl);
        this.viewSession(videoUrl, testModel, " - recorded test");
      },
      error => console.error(error)
    );
  }

  deleteRecording(testModel: EusTestModel) {
    this.eusService.deleteRecording(testModel.id).subscribe(
      ok => {
        let entry: EusTestModel;
        let newTestData: EusTestModel[] = [];
        for (entry of this.recordings) {
          if (entry.id !== testModel.id) {
            newTestData.push(entry);
          }
        }
        this.recordings = Array.from(newTestData);
      },
      error => console.error(error)
    );
  }

  startSession() {
    if (this.selectedBrowser) {
      let dialog: MdDialogRef<ElastestEusDialog> = this.eusDialog.getDialog(true);
      let message: string = this.capitalize(this.selectedBrowser);

      if (this.selectedVersion[this.selectedBrowser]) {
        message += " " + this.selectedVersion[this.selectedBrowser];
      }
      message += " - live session";
      dialog.componentInstance.title = message;
      dialog.componentInstance.message = "";
      dialog.componentInstance.loading = true;
      dialog.componentInstance.closeButton = true;

      dialog.afterClosed().subscribe(
        ok => this.stopSession(),
        error => console.error(error)
      );

      this.eusService.startSession(this.selectedBrowser, this.selectedVersion[this.selectedBrowser]).subscribe(
        id => {
          this.sessionId = id;
          this.eusService.getVncUrl(this.sessionId).subscribe(
            url => {
              dialog.componentInstance.loading = false;
              dialog.componentInstance.iframeUrl = url;
            },
            error => console.error(error)
          );
        },
        error => console.error(error)
      );

    }
    else {
      this.eusDialog.popUpMessage('Browser not selected', 'You need to chose one browsers to start a session')
        .subscribe();
    }
  }

  stopSession() {
    this.eusService.stopSession(this.sessionId).subscribe(
      ok => this.vncUrl = null,
      error => console.error(error)
    );
  }

  selectBrowser(browser: string) {
    this.selectedBrowser = browser;
    Object.keys(this.selectedVersion).forEach(key => {
        if (key != browser) {
          this.selectedVersion[key] = '';
        }
      }
    );
  }

  clearVersion() {
    Object.keys(this.selectedVersion).forEach(key => this.selectedVersion[key] = '');
  }

  capitalize(value: any) {
    if (value) {
      return value.charAt(0).toUpperCase() + value.slice(1);
    }
    return value;
  }

}
