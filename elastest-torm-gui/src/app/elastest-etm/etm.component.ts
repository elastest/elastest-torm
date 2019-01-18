import { ConfigurationService } from '../config/configuration-service.service';
import { TitlesService } from '../shared/services/titles.service';
import { ElastestRabbitmqService } from '../shared/services/elastest-rabbitmq.service';
import { AfterViewInit, Component, OnInit, ViewChild, ChangeDetectorRef } from '@angular/core';
import { TdMediaService, TdLayoutManageListComponent } from '@covalent/core';

@Component({
  selector: 'etm-etm',
  templateUrl: './etm.component.html',
  styleUrls: ['./etm.component.scss'],
})
export class EtmComponent implements AfterViewInit, OnInit {
  @ViewChild('manageList')
  manageList: TdLayoutManageListComponent;

  openedMenu: boolean = true;
  enableRefresh: boolean = false;

  constructor(
    private titlesService: TitlesService,
    public media: TdMediaService,
    private elastestRabbitmqService: ElastestRabbitmqService,
    private cdr: ChangeDetectorRef,
    private configService: ConfigurationService,
  ) {}

  ngOnInit() {
    if (this.titlesService.getTitle().getTitle() === 'Dashboard') {
      this.enableRefresh = true;
    }
    this.titlesService.setHeadTitle('ElasTest');
    this.elastestRabbitmqService.configWSConnection();
    this.elastestRabbitmqService.startWsConnection();
  }

  ngAfterViewInit(): void {
    // broadcast to all listener observables when loading the page
    this.media.broadcast();
    this.cdr.detectChanges();
  }

  openMainSidenav(): void {
    this.manageList.toggle();
    // this.openedMenu = !this.openedMenu;
  }

  capitalize(value: any): any {
    if (value) {
      return value.charAt(0).toUpperCase() + value.slice(1);
    }
    return value;
  }

  toggleFullscreen(): void {
    let documentVar: any = document as any;
    let documentElement: any = document.documentElement as any;
    let documentBody: any = document.body as any;

    if (
      documentVar.fullscreenElement || // alternative standard method
      documentVar.mozFullScreenElement || // currently working methods
      documentVar.webkitFullscreenElement ||
      documentVar.msFullscreenElement
    ) {
      if (documentVar.exitFullscreen) {
        documentVar.exitFullscreen();
      } else if (documentVar.mozCancelFullScreen) {
        documentVar.mozCancelFullScreen();
      } else if (documentVar.webkitExitFullscreen) {
        documentVar.webkitExitFullscreen();
      } else if (documentVar.msExitFullscreen) {
        documentVar.msExitFullscreen();
      }
    } else {
      let element: any = Element as any;
      if (documentElement.requestFullscreen) {
        documentElement.requestFullscreen();
      } else if (documentElement.mozRequestFullScreen) {
        documentElement.mozRequestFullScreen();
      } else if (documentElement.webkitRequestFullscreen) {
        documentElement.webkitRequestFullscreen(element.ALLOW_KEYBOARD_INPUT);
      } else if (documentBody.msRequestFullscreen) {
        documentBody.msRequestFullscreen();
      }
    }
  }
  setTitles(): void {}
}
