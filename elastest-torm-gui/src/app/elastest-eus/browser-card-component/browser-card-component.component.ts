import { Component, OnInit, ViewContainerRef, Input, ViewChild } from '@angular/core';
import { PopupService } from '../../shared/services/popup.service';
import { EusService } from '../elastest-eus.service';
import { TdDialogService } from '@covalent/core';
import { VncClientComponent } from '../../shared/vnc-client/vnc-client.component';
import { EtmMonitoringViewComponent } from '../../elastest-etm/etm-monitoring-view/etm-monitoring-view.component';
import { EusTestModel } from '../elastest-eus-test-model';
import { getErrorColor, getWarnColor } from '../../shared/utils';
import { HttpErrorResponse } from '@angular/common/http';
import { Subject, Observable } from 'rxjs';

@Component({
  selector: 'etm-eus-browser-card-component',
  templateUrl: './browser-card-component.component.html',
  styleUrls: ['./browser-card-component.component.scss'],
})
export class BrowserCardComponentComponent implements OnInit {
  @ViewChild('browserVnc')
  browserVnc: VncClientComponent;
  @Input()
  logsAndMetrics: EtmMonitoringViewComponent = undefined;
  @Input()
  showSpinner: Function;
  @Input()
  public mouseKeyboardEvents: Subject<MouseEvent>;

  // EUS info
  eusIp: string;
  eusPort: string | number;
  eusUrl: string;

  websocket: WebSocket;

  // Browser creation info
  browserName: string = 'chrome';
  browserVersion: string = 'latest';
  extraCapabilities: any;
  live: boolean;
  extraHosts: string[];

  browserAndEusLoading: boolean = true;

  // Browser instance info
  sessionId: string;
  hubContainerName: string;
  vncBrowserUrl: string;
  autoconnect: boolean = true;
  viewOnly: boolean = false;
  currentVideoName: string;
  eusTestModel: EusTestModel;

  browserCardMsg: string = 'Loading...';

  browserFilesToUpload: File | FileList;
  downloadFilePath: string = '';

  logErrors: number = 0;
  logWarnings: number = 0;

  errorColor: string = getErrorColor();
  warnColor: string = getWarnColor();

  manuallyClosed: boolean = false;

  constructor(
    private popupService: PopupService,
    private eusService: EusService,
    private _dialogService: TdDialogService,
    private _viewContainerRef: ViewContainerRef,
  ) {}

  ngOnInit(): void {}

  initByGiven(): void {}

  initEusData(eusIp: string, eusPort: string | number, eusUrl: string): void {
    this.eusIp = eusIp;
    this.eusPort = eusPort;
    this.eusUrl = eusUrl;
    this.eusService.setEusUrl(this.eusUrl);
  }

  initBrowserCreationInfo(browser: string, version: string, extraCapabilities: any, live: boolean, extraHosts: string[]): void {
    this.browserName = browser;
    this.browserVersion = version;
    this.extraCapabilities = extraCapabilities;
    this.live = live;
    this.extraHosts = extraHosts;
  }

  initBrowserInstanceInfo(eusTestModel: EusTestModel): void {
    this.eusTestModel = eusTestModel;
    this.sessionId = eusTestModel.id;
    this.hubContainerName = eusTestModel.hubContainerName;
  }

  getAndInitVncUrl(_obs?: Subject<any>): void {
    this.eusService.getVncUrl(this.sessionId).subscribe(
      (vncUrl: string) => {
        this.vncBrowserUrl = vncUrl;
        this.browserAndEusLoading = false;
        if (_obs) {
          _obs.next(this.sessionId);
        }
      },
      (error: Error) => {
        console.error(error);
        if (_obs) {
          _obs.error(error);
        }
      },
    );
  }

  startSession(
    browser: string,
    version: string,
    extraCapabilities?: any,
    live: boolean = true,
    extraHosts: string[] = [],
    acceptInsecure: boolean = false,
  ): Observable<any> {
    let _obs: Subject<any> = new Subject<any>();
    let obs: Observable<any> = _obs.asObservable();

    this.initBrowserCreationInfo(browser, version, extraCapabilities, live, extraHosts);

    this.eusService.startSession(browser, version, extraCapabilities, live, extraHosts, acceptInsecure).subscribe(
      (eusTestModel: EusTestModel) => {
        this.initBrowserInstanceInfo(eusTestModel);
        this.getAndInitVncUrl(_obs);
      },
      (errorResponse: HttpErrorResponse) => {
        let error: any = errorResponse.error;
        this.updateMsg('Error');
        _obs.error(error);
      },
    );
    return obs;
  }

  stopBrowser(): Observable<any> {
    let _obs: Subject<any> = new Subject<any>();
    let obs: Observable<any> = _obs.asObservable();
    this.vncBrowserUrl = undefined;

    this.eusService.stopSession(this.sessionId).subscribe(
      (ok) => {
        this.sessionId = undefined;
        _obs.next(ok);
      },
      (error: Error) => {
        _obs.error(error);
      },
    );
    return obs;
  }

  startRecording(videoNamePrefix: string = this.sessionId): Observable<any> {
    this.currentVideoName = videoNamePrefix + this.sessionId;
    return this.eusService.startRecording(this.sessionId, this.hubContainerName, this.currentVideoName);
  }

  stopRecording(): Observable<any> {
    let _obs: Subject<any> = new Subject<any>();
    let obs: Observable<any> = _obs.asObservable();
    this.eusService.stopRecording(this.sessionId, this.hubContainerName).subscribe(
      (ok: any) => {
        _obs.next(ok);
      },
      (error: Error) => {
        _obs.error(error);
      },
    );
    return obs;
  }

  deleteRecording(): Observable<any> {
    return this.eusService.deleteRecording(this.currentVideoName);
  }

  navigateToUrl(urlToOpen: string): Observable<any> {
    return this.eusService.navigateToUrl(this.sessionId, urlToOpen);
  }

  startWebSocket(): void {
    let wsUrl: string = this.eusService.getEusWsByHostAndPort(this.eusIp, this.eusPort);
    if (!this.websocket && wsUrl !== undefined) {
      this.websocket = new WebSocket(wsUrl);

      this.websocket.onopen = () => {
        this.websocket.send('getSessions');
        this.websocket.send('getRecordings');
      };

      this.websocket.onclose = () => this.reconnect();

      this.websocket.onmessage = (message: any) => {
        let json: any = JSON.parse(message.data);
        if (json.newSession) {
          if (this.eusTestModel !== undefined) {
            this.eusTestModel.status = json.newSession.status;
            this.eusTestModel.statusMsg = json.newSession.statusMsg;
          }
        } else if (json.removeSession) {
        }
      };
    }
  }

  stopWebsocket(): void {
    if (this.websocket) {
      this.manuallyClosed = true;
      this.websocket.close();
    }
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

  onUploadBrowserFile(files: FileList | File): void {
    if (files instanceof FileList) {
      this.eusService.uploadFilesToSession(this.sessionId, files).subscribe(
        (responseObj: object) => {
          if (!responseObj || responseObj['errors'].length > 0) {
            this.popupService.openSnackBar('An error has occurred in uploading some files');
            for (let error of responseObj['errors']) {
              console.log(error);
            }
          } else {
            this.popupService.openSnackBar('All files has been uploaded succesfully');
          }
        },
        (error: Error) => {
          console.log(error);
          this.popupService.openSnackBar('An error has occurred in uploading files');
        },
      );
    } else if (files instanceof File) {
      this.eusService.uploadFileToSession(this.sessionId, files).subscribe(
        (response: any) => {
          this.popupService.openSnackBar('The file has been uploaded succesfully');
        },
        (error: Error) => {
          console.log(error);
          this.popupService.openSnackBar('An error has occurred in uploading file');
        },
      );
    }
  }

  downloadFile(): void {
    this._dialogService
      .openPrompt({
        message: 'Pelase, insert the complete path to the file in the Browser context',
        disableClose: true,
        viewContainerRef: this._viewContainerRef,
        title: 'Download a file',
        value: '',
        cancelButton: 'Cancel',
        acceptButton: 'Download',
        width: '400px',
      })
      .afterClosed()
      .subscribe((path: string) => {
        if (path) {
          this.eusService.downloadFileFromSession(this.sessionId, path).subscribe(
            (ok: boolean) => {
              if (!ok) {
                this.popupService.openSnackBar('Error on get file');
              }
            },
            (error: Error) => {
              console.log(error);
              this.popupService.openSnackBar('Error on get file');
            },
          );
        }
      });
  }

  setLogsAndMetrics(logsAndMetrics: EtmMonitoringViewComponent): void {
    this.logsAndMetrics = logsAndMetrics;
  }

  getLogsErrors(): number {
    this.logErrors = this.logsAndMetrics.getLogsErrors();
    return this.logErrors;
  }

  getLogsWarnings(): number {
    this.logWarnings = this.logsAndMetrics.getLogsWarnings();
    return this.logWarnings;
  }

  updateMsg(msg: string): void {
    this.browserCardMsg = msg;
  }

  resizeBrowser($event): void {
    if (this.browserVnc && this.browserVnc.vncUi) {
      this.browserVnc.vncUi.onResize();
    }
  }

  emitMouseEvent(event: MouseEvent): void {
    if (this.browserVnc && this.browserVnc.vncUi) {
      this.browserVnc.emitMouseEvent(event);
    }
  }

  dispatchEvent(e: Event): void {
    this.browserVnc.dispatchEvent(e);
  }
}
