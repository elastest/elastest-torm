import { StompWSManager } from './stomp-ws-manager.service';
import { AfterViewInit, Component, OnInit } from '@angular/core';

import { Title } from '@angular/platform-browser';

import { TdMediaService } from '@covalent/core';

@Component({
  selector: 'etm-etm',
  templateUrl: './etm.component.html',
  styleUrls: ['./etm.component.scss'],
})
export class EtmComponent implements AfterViewInit, OnInit {

  title: string;
  constructor(private _titleService: Title,
              public media: TdMediaService, private stompWSManager: StompWSManager) { }

  ngOnInit(){
    this.stompWSManager.configWSConnection('/logs');
    this.stompWSManager.startWsConnection();
  }

  ngAfterViewInit(): void {
    // broadcast to all listener observables when loading the page
    this.media.broadcast();

    this._titleService.setTitle( 'Product Dashboard' );
    this.title = this._titleService.getTitle();
  }
}
