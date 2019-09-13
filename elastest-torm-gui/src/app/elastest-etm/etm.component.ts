import { ConfigurationService } from '../config/configuration-service.service';
import { TitlesService } from '../shared/services/titles.service';
import { ElastestRabbitmqService } from '../shared/services/elastest-rabbitmq.service';
import { AfterViewInit, Component, OnInit, ViewChild, ChangeDetectorRef } from '@angular/core';
import { TdMediaService, TdLayoutManageListComponent } from '@covalent/core';
import { Observable } from 'rxjs';

@Component({
  selector: 'etm-etm',
  templateUrl: './etm.component.html',
  styleUrls: ['./etm.component.scss'],
})
export class EtmComponent implements AfterViewInit, OnInit {
  @ViewChild('manageList', { static: true })
  manageList: TdLayoutManageListComponent;

  openedMenu: boolean = true;
  enableRefresh: boolean = false;

  gtLgWidth: string = '188px';
  gtSmWidth: string = '170px';
  gtXsWidth: string = '28%';
  otherWidth: string = '45%';
  onlyIconsWidth: string = '49px';

  constructor(
    private titlesService: TitlesService,
    public media: TdMediaService,
    private elastestRabbitmqService: ElastestRabbitmqService,
    private cdr: ChangeDetectorRef,
    public configService: ConfigurationService,
  ) {}

  ngOnInit(): void {
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

  switchMainSidenavWidthIcon(width: string): void {
    if (this.manageList.sidenavWidth === this.onlyIconsWidth) {
      this.manageList.sidenavWidth = width;
    } else {
      this.manageList.sidenavWidth = this.onlyIconsWidth;
    }
  }

  openMainSidenav(): void {
    if (this.media.query('gt-lg')) {
      this.switchMainSidenavWidthIcon(this.gtLgWidth);
    } else if (this.media.query('gt-md')) {
      this.switchMainSidenavWidthIcon(this.gtSmWidth);
    } else if (this.media.query('gt-sm')) {
      this.switchMainSidenavWidthIcon(this.gtSmWidth);
    } else if (this.media.query('gt-xs')) {
      this.manageList.sidenavWidth = this.gtXsWidth;
      this.manageList.toggle();
    } else {
      this.manageList.sidenavWidth = this.otherWidth;
      this.manageList.toggle();
    }
  }

  isMediaSizeByGiven(size: 'gt-lg' | 'gt-md' | 'gt-sm' | 'gt-xs'): Observable<boolean> {
    return this.media.registerQuery(size);
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
