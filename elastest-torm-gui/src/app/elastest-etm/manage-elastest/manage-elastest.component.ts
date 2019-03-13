import { Component, OnInit } from '@angular/core';
import { TitlesService } from '../../shared/services/titles.service';
import { ConfigurationService } from '../../config/configuration-service.service';

@Component({
  selector: 'etm-manage-elastest',
  templateUrl: './manage-elastest.component.html',
  styleUrls: ['./manage-elastest.component.scss'],
})
export class ManageElastestComponent implements OnInit {
  public selectedTab: number;

  constructor(private titlesService: TitlesService, public configService: ConfigurationService) {}

  ngOnInit(): void {
    this.titlesService.setHeadTitle('Manage Elastest');
  }
}
