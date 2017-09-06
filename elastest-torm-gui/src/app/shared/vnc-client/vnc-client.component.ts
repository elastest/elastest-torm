import { VncUI } from './ui';
import { AfterViewInit, Component, ElementRef, HostListener, Input, OnDestroy, OnInit, ViewChild } from '@angular/core';

@Component({
  selector: 'vnc-client',
  templateUrl: './vnc-client.component.html',
  styleUrls: ['./vnc-client.component.scss']
})
export class VncClientComponent implements AfterViewInit, OnInit, OnDestroy {
  @Input()
  public host: string;
  @Input()
  public port: any;
  @Input()
  public password: string;
  @Input()
  public autoconnect: boolean = false;
  @Input()
  public viewOnly: boolean = false;

  public canvas: HTMLCanvasElement;

  public path;
  public resizeTimeout;
  public desktopName;

  public vncUi: VncUI;

  constructor(private elementRef: ElementRef) {
    this.canvas = <HTMLCanvasElement>document.getElementById('vnc_canvas');
  }

  ngOnInit() { }

  ngAfterViewInit(): void {
    this.initVnc();
  }

  ngOnDestroy() {
    this.disconnect();
  }

  initVnc() {
    if (this.host && this.port) {
      this.vncUi = new VncUI(this.host, this.port, this.autoconnect, this.viewOnly, this.password);
      this.vncUi.init();
    }
  }

  connect() {
    this.vncUi.connect();
  }

  disconnect() {
    this.vncUi.disconnect();
  }
}
