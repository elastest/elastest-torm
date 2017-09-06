import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'test-vnc',
  templateUrl: './test-vnc.component.html',
  styleUrls: ['./test-vnc.component.scss']
})
export class TestVncComponent implements OnInit {

  host: string = 'localhost';
  port: any = '6080';
  password: string = undefined;
  autoconnect: boolean = true;
  viewOnly: boolean = false;

  constructor() { }

  ngOnInit() {
  }

}
