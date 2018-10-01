import { Component, OnInit } from '@angular/core';
import { TitlesService } from '../../shared/services/titles.service';

@Component({
  selector: 'etm-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class DashboardComponent implements OnInit {
  constructor(private titlesService: TitlesService) {}

  ngOnInit() {
    this.titlesService.setHeadTitle('Dashboard');
  }
}
