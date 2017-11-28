import { AgTreeCheckModel } from '../../../shared/ag-tree-model';
import { Component, OnInit } from '@angular/core';
import { MD_DIALOG_DATA, MdDialogRef } from '@angular/material';

@Component({
  selector: 'app-monitoring-configuration',
  templateUrl: './monitoring-configuration.component.html',
  styleUrls: ['./monitoring-configuration.component.scss']
})
export class MonitoringConfigurationComponent implements OnInit {

  nodes: AgTreeCheckModel = new AgTreeCheckModel();
  nodesArray: any[] = [
    {
      name: 'sut',
      children: [
        { name: 'default_log', checked: true },
        {
          name: 'et_dockbeat',
          children: [
            { name: 'cpu.totalUsage', checked: true, }
          ],
          checked: true,
        },
        { name: 'default_log', checked: true },
      ],
      checked: true,
    },
    {
      name: 'test',
      children: [
        { name: 'default_log', checked: true },
        {
          name: 'et_dockbeat',
          children: [
            { name: 'cpu.totalUsage', checked: true, }
          ],
          checked: true,
        }
      ],
      checked: true,
    },
    {
      name: 'browser',
      children: [
        { name: 'console', checked: true },
      ],
      checked: true,
    }
  ];

  constructor(
    private dialogRef: MdDialogRef<MonitoringConfigurationComponent>,
  ) { }

  ngOnInit() {
    this.nodes = new AgTreeCheckModel();
    this.nodes.setByObjArray(this.nodesArray);
  }

}
