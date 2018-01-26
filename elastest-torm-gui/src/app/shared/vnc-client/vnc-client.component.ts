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
  @Input()
  public showStatus: boolean = true;

  public canvas: HTMLCanvasElement;
  public vncUi: VncUI;

  statusInfo: string;
  statusColor: string = '#000000';
  statusIcon: string = 'fiber_manual_record';

  constructor(private elementRef: ElementRef) {
    this.canvas = <HTMLCanvasElement>document.getElementById('vnc_canvas');
  }

  ngOnInit() { }

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
    }
  }

  connect(): void {
    this.vncUi.connect();
  }

  disconnect(): void {
    this.vncUi.disconnect();
  }

  suscribeToStatus(): void {
    this.vncUi.statusObs.subscribe(
      (status) => {
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
}
