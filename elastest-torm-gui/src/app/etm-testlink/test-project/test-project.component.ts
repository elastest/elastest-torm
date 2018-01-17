import { TdDialogService } from '@covalent/core/dialogs/services/dialog.service';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { TitlesService } from '../../shared/services/titles.service';
import { Component, OnInit, ViewContainerRef } from '@angular/core';
import { TestProjectModel } from '../models/test-project-model';
import { TestLinkService } from '../testlink.service';
import { MdDialog } from '@angular/material';

@Component({
  selector: 'testlink-test-project',
  templateUrl: './test-project.component.html',
  styleUrls: ['./test-project.component.scss']
})
export class TestProjectComponent implements OnInit {
  testProject: TestProjectModel;

  constructor(
    private titlesService: TitlesService, private testLinkService: TestLinkService,
    private route: ActivatedRoute, private router: Router,
    private _dialogService: TdDialogService, private _viewContainerRef: ViewContainerRef,

    public dialog: MdDialog,
  ) { }

  ngOnInit() {
    this.titlesService.setHeadTitle('Project');
    this.testProject = new TestProjectModel();
    this.loadProject();
  }

  loadProject() {
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params.switchMap((params: Params) => this.testLinkService.getProjectById(params['projectId']))
        .subscribe((project: TestProjectModel) => {
          this.testProject = project;
          this.titlesService.setTopTitle(this.testProject.getRouteString());
        });
    }
  }


}
