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
  public autoconnect: boolean;

  public canvas: HTMLCanvasElement;

  public path;
  public resizeTimeout;
  public desktopName;

  public vncUi: VncUI;

  constructor(private elementRef: ElementRef) {
    this.canvas = <HTMLCanvasElement>document.getElementById('vnc_canvas');
  }

  ngOnInit() {
    // this.createScript('./assets/vnc-resources/vendor/promise.js');
    // this.createScript('./assets/vnc-resources/vendor/browser-es-module-loader/dist/browser-es-module-loader.js');
  }

  ngAfterViewInit(): void {
    this.vncUi = new VncUI(this.host, this.port, this.autoconnect, this.password);
    this.vncUi.init();
  }

  ngOnDestroy() {
    this.disconnect();
  }

  connect() {
    this.vncUi.connect();
  }

  disconnect() {
    this.vncUi.disconnect();
  }

  createScript(path: string, type: string = 'text/javascript') {
    // document.write('<script type="' + type + '" src="' + path + '"></script>');
    const s = document.createElement('script');
    s.type = 'text/javascript';

    s.src = path;
    this.elementRef.nativeElement.appendChild(s);
  }
}
