import { Observable, Subscription } from 'rxjs/Rx';
import { TitlesService } from '../../shared/services/titles.service';
import { ConfigurationService } from '../../config/configuration-service.service';
import { Component, OnInit } from '@angular/core';
import { CoreServiceModel } from '../models/core-service.model';
import { VersionInfo } from '../models/version-info.model';

@Component({
  selector: 'etm-help',
  templateUrl: './help.component.html',
  styleUrls: ['./help.component.scss'],
})
export class HelpComponent implements OnInit {
  coreServices: CoreServiceModel[] = [];
  etCurrentVersion: string;

  autorefreshEnabled: boolean = true;
  lastRefresh: Date;
  coreServicesSubscription: Subscription;

  // SuT Data
  coreServiceColumns: any[] = [
    { name: 'status', label: 'Status' },
    { name: 'name', label: 'Service Name' },
    { name: 'imageName', label: 'Image Name' },
    { name: 'versionInfo.tag', label: 'Version' },
    { name: 'versionInfo.date', label: 'Date' },
    { name: 'containerNames', label: 'Container Names' },
    { name: 'networks', label: 'Networks' },
    { name: 'versionInfo.commitId', label: 'Commit Id' },
  ];

  constructor(private titlesService: TitlesService, private configurationService: ConfigurationService) {}

  ngOnInit() {
    this.titlesService.setHeadTitle('Help');
    this.init();
    this.startCoreServicesSubscription();
  }

  init(): void {
    this.configurationService.getCoreServicesInfo().subscribe((coreServices: CoreServiceModel[]) => {
      this.coreServices = coreServices;
      this.initCurrentETVersion();
      this.lastRefresh = new Date();
    });
  }

  initCurrentETVersion(): void {
    for (let coreService of this.coreServices) {
      if (coreService.isPlatformImage()) {
        this.etCurrentVersion = coreService.versionInfo.tag;
        if (this.etCurrentVersion === undefined || this.etCurrentVersion === 'latest') {
          this.etCurrentVersion = 'Latest Stable';
        }
      }
    }
  }

  switchAutorefresh(enableAutorefresh: boolean): void {
    this.autorefreshEnabled = enableAutorefresh;
  }

  startCoreServicesSubscription(): void {
    let timer: Observable<number>;

    timer = Observable.interval(8000);
    this.coreServicesSubscription = timer.subscribe(() => {
      if (this.autorefreshEnabled) {
        this.init();
      }
    });
  }
}
