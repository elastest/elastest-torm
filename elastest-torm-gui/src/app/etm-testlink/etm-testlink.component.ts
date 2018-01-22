import { TdDialogService } from '@covalent/core/dialogs/services/dialog.service';
import { Router } from '@angular/router';
import { TdDataTableService } from '@covalent/core';
import { TitlesService } from '../shared/services/titles.service';
import { TestProjectModel } from './models/test-project-model';
import { Component, Input, OnInit, ViewContainerRef } from '@angular/core';
import { TestLinkService } from './testlink.service';
import { MdDialog } from '@angular/material';

@Component({
  selector: 'etm-testlink',
  templateUrl: './etm-testlink.component.html',
  styleUrls: ['./etm-testlink.component.scss']
})
export class EtmTestlinkComponent implements OnInit {
  @Input()
  isNested: boolean = false;
  testLinkUrl: string;

  // Project data
  projectColumns: any[] = [
    { name: 'id', label: 'Id' },
    { name: 'name', label: 'Project' },
    { name: 'prefix', label: 'Prefix' },
    { name: 'notes', label: 'Notes' },
    { name: 'enableRequirements', label: 'Enable Requirements' },
    { name: 'enableTestPriority', label: 'Enable Test Priority' },
    { name: 'enableAutomation', label: 'Enable Automation' },
    { name: 'enableInventory', label: 'Enable Inventory' },
    { name: 'active', label: 'Active' },
    { name: 'public', label: 'Public' },
    // { name: 'options', label: 'Options' },
  ];

  projectsList: TestProjectModel[] = [];


  constructor(
    private titlesService: TitlesService,
    private _dataTableService: TdDataTableService, private testlinkService: TestLinkService, private router: Router,
    private _dialogService: TdDialogService, private _viewContainerRef: ViewContainerRef,
    public dialog: MdDialog,
  ) { }

  ngOnInit() {
    if (!this.isNested) {
      this.titlesService.setHeadAndTopTitle('Testlink Projects');
    }
    this.loadTestLinkUrl();
    this.loadProjects();
  }

  loadTestLinkUrl(): void {
    this.testlinkService.getTestlinkUrl()
      .subscribe(
      (url: string) => {
        this.testLinkUrl = url;
      },
    );
  }

  loadProjects(): void {
    this.testlinkService.getAllTestProjects()
      .subscribe(
      (projects: TestProjectModel[]) => {
        this.projectsList = projects;
      },
    );
  }
}
