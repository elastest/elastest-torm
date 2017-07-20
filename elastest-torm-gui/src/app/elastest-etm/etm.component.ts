import { TdLayoutManageListComponent } from '@covalent/core/layout/layout-manage-list/layout-manage-list.component';
import { ElastestRabbitmqService } from '../shared/services/elastest-rabbitmq.service';
import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';

import { Title } from '@angular/platform-browser';

import { TdMediaService } from '@covalent/core';

@Component({
  selector: 'etm-etm',
  templateUrl: './etm.component.html',
  styleUrls: ['./etm.component.scss'],
})
export class EtmComponent implements AfterViewInit, OnInit {
  @ViewChild('manageList') manageList: TdLayoutManageListComponent;

  title: string;
  openedMenu: boolean = true;

  constructor(private _titleService: Title,
    public media: TdMediaService, private elastestRabbitmqService: ElastestRabbitmqService) { }

  ngOnInit() {
    this.elastestRabbitmqService.configWSConnection();
    this.elastestRabbitmqService.startWsConnection();
  }

  ngAfterViewInit(): void {
    // broadcast to all listener observables when loading the page
    this.media.broadcast();

    this._titleService.setTitle('Test Management');
    this.title = this._titleService.getTitle();
  }

  openMainSidenav() {
    this.manageList.toggle();
    // this.openedMenu = !this.openedMenu;
  }
}
