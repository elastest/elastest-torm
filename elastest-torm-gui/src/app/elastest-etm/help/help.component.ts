import { TitlesService } from '../../shared/services/titles.service';
import { ConfigurationService } from '../../config/configuration-service.service';
import { Component, OnInit } from '@angular/core';
import { version } from 'punycode';

@Component({
  selector: 'etm-help',
  templateUrl: './help.component.html',
  styleUrls: ['./help.component.scss']
})
export class HelpComponent implements OnInit {

  versionsInfo: any[];
  elastestModulesNames: string[];
  versionsInfoDataTable: any[] = [];

  etCurrentVersion: string;

  // SuT Data
  versionsInfoColumns: any[] = [
    { name: 'imageName', label: 'Image Name' },
    { name: 'date', label: 'Date' },
    { name: 'commitId', label: 'Commit Id' },
    { name: 'version', label: 'Version' },
  ];

  constructor(private titlesService: TitlesService, private configurationService: ConfigurationService) { }

  ngOnInit() {
    this.titlesService.setHeadAndTopTitle('Help');
    this.configurationService.getHelpInfo()
      .subscribe(
      (helpInfo) => {
        this.versionsInfo = helpInfo.versionsInfo;
        this.elastestModulesNames = Object.keys(this.versionsInfo);
        this.initDataTable();
      },
    );
  }

  initDataTable(): void {
    if (this.versionsInfo && this.elastestModulesNames) {
      for (let versionInfo of this.elastestModulesNames) {
        let obj: any = {
          imageName: versionInfo,
          date: this.versionsInfo[versionInfo].date,
          commitId: this.versionsInfo[versionInfo].commitId,
          version: this.versionsInfo[versionInfo].name,
        };
        this.versionsInfoDataTable.push(obj);

        if (versionInfo.startsWith('elastest/platform')) {
          this.etCurrentVersion = versionInfo.split(':')[1];
          if (this.etCurrentVersion === undefined || this.etCurrentVersion === 'latest') {
            this.etCurrentVersion = 'Latest Stable';
          }
        }
      }
      this.versionsInfoDataTable = [...this.versionsInfoDataTable];
    }
  }

}
