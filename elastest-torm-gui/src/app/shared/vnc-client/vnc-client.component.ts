import { VncUI } from './ui';
import { AfterViewInit, Component, ElementRef, HostListener, Input, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Observable } from 'rxjs/Observable';

@Component({
  selector: 'vnc-client',
  templateUrl: './vnc-client.component.html',
  styleUrls: ['./vnc-client.component.scss'],
})
export class VncClientComponent implements AfterViewInit, OnInit, OnDestroy {
  @ViewChild('canvas') vncCanvas: ElementRef;

  @Input() public host: string;
  @Input() public port: any;
  @Input() public password: string;
  @Input() public autoconnect: boolean = false;
  @Input() public viewOnly: boolean = false;
  @Input() public showStatus: boolean = true;
  @Input() public showConnectionBtns: boolean = true;

  htmlEl: HTMLHtmlElement = document.getElementsByTagName('html')[0];

  public canvas: HTMLCanvasElement;
  public vncUi: VncUI;

  statusInfo: string;
  statusColor: string = '#000000';
  statusIcon: string = 'fiber_manual_record';

  canvasFocused: boolean = false;
  canvasMouseDown: boolean = false;

  constructor(private elementRef: ElementRef) {
    this.switchFocus = this.switchFocus.bind(this);
    this.canvas = <HTMLCanvasElement>document.getElementById('vnc_canvas');
  }

  ngOnInit() {
    this.manageKeyboard();
  }

  ngAfterViewInit(): void {
    this.initVnc();
  }

  ngOnDestroy(): void {
    this.disconnect();
  }

  initVnc(): void {
    if (this.host && this.port) {
      this.vncUi = new VncUI(this.host, this.port, this.autoconnect, this.viewOnly, this.password);
      this.vncUi.init();
      this.suscribeToStatus();
      this.preventFocus();
    }
  }

  connect(): void {
    this.vncUi.connect();
  }

  disconnect(): void {
    this.vncUi.disconnect();
  }

  reconnect(): void {
    if (!this.vncUi.connected) {
      this.connect();
    } else {
      this.disconnect();
      this.connect();
    }
  }

  preventFocus(): void {
    // Capturing Keyboard disabled by default
    this.canvasFocused = false;
    this.switchFocus();
  }

  manageKeyboard(): void {
    const canvasNE: any = this.vncCanvas.nativeElement;

    const mouseOutObs: Observable<any> = Observable.fromEvent(canvasNE, 'mouseout');
    const mouseOverObs: Observable<any> = Observable.fromEvent(canvasNE, 'mouseover');
    const mouseDownObs: Observable<any> = Observable.fromEvent(canvasNE, 'mousedown');
    const mouseUpObs: Observable<any> = Observable.fromEvent(canvasNE, 'mouseup');

    mouseOutObs.subscribe(
      (mouseEvent) => {
        if (!this.canvasFocused || !this.canvasMouseDown) {
          this.switchHTMLClickEventListener(true);
          mouseEvent.stopPropagation();
          mouseEvent.preventDefault();
          this.canvasFocused = false;
        }
      },
      (error) => console.log(error),
    );
    mouseOverObs.subscribe(
      (mouseEvent) => {
        this.switchHTMLClickEventListener(false);
        mouseEvent.stopPropagation();
        mouseEvent.preventDefault();
        this.canvasFocused = true;
      },
      (error) => console.log(error),
    );

    mouseDownObs.subscribe(
      (mouseEvent) => {
        this.canvasMouseDown = true;
        mouseEvent.stopPropagation();
        mouseEvent.preventDefault();
        this.switchFocus();
      },
      (error) => console.log(error),
    );
    mouseUpObs.subscribe(
      (mouseEvent) => {
        this.canvasMouseDown = false;
        mouseEvent.stopPropagation();
        mouseEvent.preventDefault();
      },
      (error) => console.log(error),
    );
    this.switchHTMLClickEventListener(true);
  }

  switchHTMLClickEventListener(add: boolean = true): void {
    this.htmlEl.removeEventListener('click', this.switchFocus, false);
    if (add) {
      this.htmlEl.addEventListener('click', this.switchFocus, false);
    } else {
      this.htmlEl.removeEventListener('click', this.switchFocus, false);
    }
  }

  switchFocus(): void {
    if (this.vncUi) {
      if (this.canvasFocused) {
        this.vncUi.rfb.get_keyboard().set_focused(true);
      } else {
        this.vncUi.rfb.get_keyboard().set_focused(false);
      }
    }
  }

  suscribeToStatus(): void {
    this.vncUi.statusObs.subscribe((status) => {
      this.statusInfo = status;
      if (status.startsWith('Error')) {
        this.statusColor = '#cc200f';
        this.statusIcon = 'error';
      } else {
        this.statusIcon = 'fiber_manual_record';
        if (status.startsWith('Connected')) {
          this.statusColor = '#7fac16';
        } else if (status.startsWith('Disconnected')) {
          this.statusColor = '#cc200f';
        } else if (status.startsWith('Connecting') || status.startsWith('Disconnecting')) {
          this.statusColor = '#ffac2f';
        } else if (status.startsWith('Warning')) {
          this.statusColor = '#ffac2f';
          this.statusIcon = 'warning';
        }
      }
    });
  }

  errorStatus(): boolean {
    return this.statusInfo && this.statusInfo.startsWith('Error');
  }
}
