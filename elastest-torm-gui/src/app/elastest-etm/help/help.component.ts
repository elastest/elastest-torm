import { TitlesService } from '../../shared/services/titles.service';
import { ConfigurationService } from '../../config/configuration-service.service';
import { Component, OnInit } from '@angular/core';

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
    this.titlesService.setHeadTitle('Help');
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
      for (let fullImageName of this.elastestModulesNames) {
        let obj: any = {
          imageName: fullImageName.split(':')[0],
          date: this.versionsInfo[fullImageName].date,
          commitId: this.versionsInfo[fullImageName].commitId,
          version: fullImageName.includes('elastest/platform') && !fullImageName.includes(':dev') ? this.versionsInfo[fullImageName].name : (fullImageName.split(':')[1] ? fullImageName.split(':')[1] : 'latest') ,
        };
        this.versionsInfoDataTable.push(obj);
        
        if (fullImageName.startsWith('elastest/platform')) {
          this.etCurrentVersion = this.versionsInfo[fullImageName].name;
          if (this.etCurrentVersion === undefined || this.etCurrentVersion === 'latest') {
            this.etCurrentVersion = 'Latest Stable';
          }
        }
      }
      this.versionsInfoDataTable = [...this.versionsInfoDataTable];
    }
  }

}
