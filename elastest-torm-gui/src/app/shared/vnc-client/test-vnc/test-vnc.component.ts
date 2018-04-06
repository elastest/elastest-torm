import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';

@Component({
  selector: 'test-vnc',
  templateUrl: './test-vnc.component.html',
  styleUrls: ['./test-vnc.component.scss'],
})
export class TestVncComponent implements OnInit {
  host: string = 'localhost';
  port: any = '6080';
  password: string = undefined;
  autoconnect: boolean = true;
  viewOnly: boolean = false;
  showStatus: boolean = true;
  showConnectionBtns: boolean = true;
  ready: boolean = false;

  constructor(public router: Router) {}

  ngOnInit() {
    let params: any = this.router.parseUrl(this.router.url).queryParams;
    if (params.host !== undefined) {
      this.host = params.host;
    }
    if (params.port !== undefined) {
      this.port = params.port;
    }
    if (params.password !== undefined) {
      this.password = params.password;
    }
    if (params.autoconnect !== undefined) {
      this.autoconnect = params.autoconnect;
    }
    if (params.viewOnly !== undefined) {
      this.viewOnly = params.viewOnly;
    }
    if (params.showStatus !== undefined) {
      this.showStatus = params.showStatus;
    }
    if (params.showConnectionBtns !== undefined) {
      this.showConnectionBtns = params.showConnectionBtns;
    }
    this.ready = true;
  }
}
