import { Component, OnInit, ViewChildren } from '@angular/core';
import { TitlesService } from '../../shared/services/titles.service';
import { ProjectsManagerComponent } from '../project/projects-manager/projects-manager.component';

@Component({
  selector: 'etm-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class DashboardComponent implements OnInit {
  @ViewChildren(ProjectsManagerComponent)
  projectsManager: ProjectsManagerComponent;

  constructor(private titlesService: TitlesService) {}

  ngOnInit() {
    this.titlesService.setHeadTitle('Dashboard');
  }
}
