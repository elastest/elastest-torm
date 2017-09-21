import { ActivatedRoute, Params } from '@angular/router';
import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'esm-service-gui',
  templateUrl: './service-gui.component.html',
  styleUrls: ['./service-gui.component.scss']
})
export class ServiceGuiComponent implements OnInit {

  @Input()
  serviceUrl: string;

  constructor(private activatedRoute: ActivatedRoute) { }

  ngOnInit() {    
    console.log(this.serviceUrl);   
  }

}
