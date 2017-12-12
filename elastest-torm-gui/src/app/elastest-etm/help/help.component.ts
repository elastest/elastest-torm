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

  constructor(private titlesService: TitlesService, private configurationService: ConfigurationService) { }

  ngOnInit() {
    this.titlesService.setHeadAndTopTitle('Help');
    this.configurationService.getHelpInfo()
    .subscribe(
      (helpInfo) => {
        this.versionsInfo = helpInfo.versionsInfo;
        this.elastestModulesNames = Object.keys(this.versionsInfo);
      },
    );
  }

}
