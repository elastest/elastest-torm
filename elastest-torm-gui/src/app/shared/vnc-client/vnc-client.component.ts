import { VncUI } from './ui';
import { Component, ElementRef, Input, OnDestroy, OnInit, ViewChild, EventEmitter } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { fromEvent, Subject } from 'rxjs';
import { getUrlObj, randomNum } from '../utils';

@Component({
  selector: 'vnc-client',
  templateUrl: './vnc-client.component.html',
  styleUrls: ['./vnc-client.component.scss'],
})
export class VncClientComponent implements OnInit, OnDestroy {
  @ViewChild('canvas') vncCanvas: ElementRef;
  @ViewChild('vncScreen') vncScreen: ElementRef;

  @Input() public host: string;
  @Input() public port: any;
  @Input() public password: string;
  @Input() public autoconnect: boolean = false;
  @Input() public viewOnly: boolean = false;
  @Input() public showStatus: boolean = true;
  @Input() public showConnectionBtns: boolean = true;
  @Input() public resize: 'scale' | 'downscale' | 'remote' = 'scale';
  @Input() public vncUrl: string;
  @Input() public bgColor: string = '#666666';
  @Input() public mouseKeyboardEvents: Subject<MouseEvent>;

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
  }

  ngOnInit(): void {
    this.canvas = this.vncCanvas.nativeElement as HTMLCanvasElement;
    this.manageKeyboard();

    if (this.host && this.port) {
      this.initVnc();
    } else {
      this.getInfoFromVncUrl();
    }
  }

  ngOnDestroy(): void {
    this.disconnect();
  }

  initVnc(): void {
    if (this.host && this.port) {
      this.vncUi = new VncUI(
        this.host,
        this.port,
        this.autoconnect,
        this.viewOnly,
        this.password,
        this.resize,
        this.canvas,
        this.vncScreen.nativeElement,
      );
      this.vncUi.init();
      this.suscribeToStatus();
      this.preventFocus();
    }
  }

  getInfoFromVncUrl(): void {
    if (this.vncUrl !== undefined) {
      let vncUrlObj: any = getUrlObj(this.vncUrl);
      let vncQueryParams: any = vncUrlObj.queryParams;
      this.host = vncUrlObj.hostname;
      this.port = vncUrlObj.port;
      this.password = vncQueryParams.password;
      this.autoconnect = vncQueryParams.autoconnect !== undefined ? vncQueryParams.autoconnect : this.autoconnect;
      this.viewOnly = vncQueryParams.viewOnly !== undefined ? vncQueryParams.viewOnly : this.viewOnly;
      this.showStatus = vncQueryParams.showStatus !== undefined ? vncQueryParams.showStatus : this.showStatus;
      this.showConnectionBtns =
        vncQueryParams.showConnectionBtns !== undefined ? vncQueryParams.showConnectionBtns : this.showConnectionBtns;
      this.resize = vncQueryParams.resize !== undefined ? vncQueryParams.resize : this.resize;

      this.initVnc();
    }
  }

  connect(): void {
    if (this.vncUi !== undefined && this.canvas) {
      this.vncUi.connect();
    }
  }

  disconnect(): void {
    if (this.vncUi !== undefined) {
      this.vncUi.disconnect();
    }
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

    const mouseOutObs: Observable<any> = fromEvent(canvasNE, 'mouseout');
    const mouseOverObs: Observable<any> = fromEvent(canvasNE, 'mouseover');
    const mouseDownObs: Observable<any> = fromEvent(canvasNE, 'mousedown');
    const mouseUpObs: Observable<any> = fromEvent(canvasNE, 'mouseup');

    mouseOutObs.subscribe(
      (mouseEvent: MouseEvent) => {
        if (!this.canvasFocused || !this.canvasMouseDown) {
          this.switchHTMLClickEventListener(true);
          this.stopEventPropagationAndDefault(mouseEvent);
          this.canvasFocused = false;
        }
        if (this.mouseKeyboardEvents && !mouseEvent.metaKey) {
          this.mouseKeyboardEvents.next(mouseEvent);
        }
      },
      (error: Error) => console.log(error),
    );
    mouseOverObs.subscribe(
      (mouseEvent: MouseEvent) => {
        this.switchHTMLClickEventListener(false);
        this.stopEventPropagationAndDefault(mouseEvent);
        this.canvasFocused = true;
        if (this.mouseKeyboardEvents && !mouseEvent.metaKey) {
          this.mouseKeyboardEvents.next(mouseEvent);
        }
      },
      (error: Error) => console.log(error),
    );

    mouseDownObs.subscribe(
      (mouseEvent: MouseEvent) => {
        this.canvasMouseDown = true;
        this.stopEventPropagationAndDefault(mouseEvent);
        this.switchFocus();
        if (this.mouseKeyboardEvents && !mouseEvent.metaKey) {
          this.mouseKeyboardEvents.next(mouseEvent);
        }
      },
      (error: Error) => console.log(error),
    );
    mouseUpObs.subscribe(
      (mouseEvent: MouseEvent) => {
        this.canvasMouseDown = false;
        this.stopEventPropagationAndDefault(mouseEvent);
        if (this.mouseKeyboardEvents && !mouseEvent.metaKey) {
          this.mouseKeyboardEvents.next(mouseEvent);
        }
      },
      (error: Error) => console.log(error),
    );
    this.switchHTMLClickEventListener(true);
  }

  stopEventPropagationAndDefault(mouseEvent: MouseEvent): void {
    if (mouseEvent) {
      mouseEvent.stopPropagation();
      mouseEvent.preventDefault();
    }
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
    if (this.vncUi && this.vncUi.rfb) {
      if (this.canvasFocused) {
        this.vncUi.rfb.get_keyboard().set_focused(true);
      } else {
        this.vncUi.rfb.get_keyboard().set_focused(false);
      }
    }
  }

  emitMouseEvent(mouseEvent: MouseEvent): void {
    // Calculate position
    let x: number = this.canvas.getBoundingClientRect().left + mouseEvent.offsetX;
    let y: number = this.canvas.getBoundingClientRect().top + mouseEvent.offsetY;

    let newMouseEvent: MouseEvent;
    let mouseEventInit: any = {};
    for (let key of Object.keys(mouseEvent)) {
      mouseEventInit[key] = mouseEvent[key];
    }
    mouseEventInit.x = x;
    mouseEventInit.clientX = x;

    mouseEventInit.y = y;
    mouseEventInit.clientY = y;

    // HACK to know if event is propagated
    mouseEventInit.metaKey = true;

    switch (event.type) {
      case 'mouseout':
        newMouseEvent = new MouseEvent('mouseout', mouseEventInit);
        break;
      case 'mouseover':
        newMouseEvent = new MouseEvent('mouseover', mouseEventInit);
        break;
      case 'mousedown':
        newMouseEvent = new MouseEvent('mousedown', mouseEventInit);
        break;
      case 'mouseup':
        newMouseEvent = new MouseEvent('mouseup', mouseEventInit);
        break;
      default:
        return;
    }

    this.canvas.dispatchEvent(newMouseEvent);
  }

  suscribeToStatus(): void {
    this.vncUi.statusObs.subscribe((status: string) => {
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

  dispatchEvent(e: Event): void {
    this.canvas.dispatchEvent(e);
  }
}
