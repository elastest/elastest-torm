import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import {MdDialogRef, MdDialog, MdDialogConfig} from '@angular/material';
import {ElastestEusDialog} from './elastest-eus.dialog';
import {ElastestEusDialogService} from './elastest-eus.dialog.service';
import {EusService} from './elastest-eus.service';
import {EusTestModel} from './elastest-eus-test-model';
import {ConfigurationService} from '../config/configuration-service.service';


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
  browsers = [
    'chrome',
    'firefox'
  ];

  selectedVersion: string;
  browserVersions = {
    'chrome': [
      { value: '59', viewValue: '59' },
      { value: '58', viewValue: '58' },
      { value: '57', viewValue: '57' }
    ],
    'firefox': [
      { value: '54', viewValue: '54' },
      { value: '53', viewValue: '53' },
      { value: '52', viewValue: '52' }
    ]
  };


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
  eusUrl: string;

  @Input()
  eusHost: string;

  @Input()
  eusPort: number;

  @Output()
  onInitComponent = new EventEmitter<string>();

  constructor(private eusService: EusService, private eusDialog: ElastestEusDialogService, private configurationService: ConfigurationService) { }

  ngOnInit() {
    if (this.eusUrl){
      this.eusService.setEusUrl(this.eusUrl);
    }

    if (this.eusHost){
      this.eusService.setEusHost(this.eusHost);
    }
    if (!this.websocket) {      
      if(this.eusHost && this.eusPort){
        this.websocket = new WebSocket("ws://" + this.eusHost + ":50316" + "/eus/v1/eus-ws");        
      }else{
        this.websocket = new WebSocket(this.configurationService.configModel.eusWebSocketUrl);
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

  viewRecording(testModel: EusTestModel) {
    this.eusService.getRecording(testModel.id).subscribe(
      ok => {
        this.viewSession(this.configurationService.configModel.eusServiceUrlNoPath + ok.text(), testModel, " - recorded test");
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
      if (this.selectedVersion) {
        message += " " + this.selectedVersion;
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

      this.eusService.startSession(this.selectedBrowser, this.selectedVersion).subscribe(
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

  selectBrowser(browserValue) {
    this.selectedBrowser = browserValue;
  }

  clearVersion() {
    this.selectedVersion = "";
  }

  capitalize(value: any) {
    if (value) {
      return value.charAt(0).toUpperCase() + value.slice(1);
    }
    return value;
  }

}
