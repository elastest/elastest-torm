import { TreeComponent } from 'angular-tree-component/dist/components/tree.component';
import { TJobExecModel } from '../../tjob-exec/tjobExec-model';
import { ElastestESService } from '../../../shared/services/elastest-es.service';
import { ESBoolQueryModel, ESTermModel } from '../../../shared/elasticsearch-model/es-query-model';
import { AgTreeCheckModel } from '../../../shared/ag-tree-model';
import { Component, Inject, OnInit, Optional, ViewChild } from '@angular/core';
import { MD_DIALOG_DATA, MdDialogRef } from '@angular/material';

@Component({
  selector: 'app-monitoring-configuration',
  templateUrl: './monitoring-configuration.component.html',
  styleUrls: ['./monitoring-configuration.component.scss']
})
export class MonitoringConfigurationComponent implements OnInit {
  @ViewChild('logsTree') logsTree: TreeComponent;
  // @ViewChild('MetricsTree') MetricsTree: TreeComponent;
  componentsStreams: AgTreeCheckModel;


  constructor(
    private dialogRef: MdDialogRef<MonitoringConfigurationComponent>,
    private elastestESService: ElastestESService,
    @Optional() @Inject(MD_DIALOG_DATA) public tJobExec: TJobExecModel,

  ) {
    this.componentsStreams = new AgTreeCheckModel();

  }

  ngOnInit() {
    this.loadLogsTree();
    // this.loadMetricsTree();
  }

  loadLogsTree(): void {
    let componentStreamQuery: ESBoolQueryModel = new ESBoolQueryModel();
    let streamTypeTerm: ESTermModel = new ESTermModel();
    streamTypeTerm.name = 'stream_type';
    streamTypeTerm.value = 'log';
    componentStreamQuery.bool.must.termList.push(streamTypeTerm);

    this.elastestESService.getIndexComponentStreamList(
      this.tJobExec.logIndex, componentStreamQuery.convertToESFormat()
    ).subscribe(
      (componentsStreams: any[]) => {
        this.componentsStreams.setByObjArray(componentsStreams);

        // Init checks
        for (let logCard of this.tJobExec.tJob.execDashboardConfigModel.allLogsTypes.logsList) {
          for (let componentStream of this.componentsStreams.tree) {
            if (logCard.component === componentStream.name) {
              for (let stream of componentStream.children) {
                if (logCard.stream === stream.name) {
                  stream.checked = logCard.activated;
                }
              }
            }
          }
        }

        this.logsTree.treeModel.update();
      }
      );
  }



  applyAndSave(): void {
    //
    this.apply();
  }

  apply(): void {

    let response: any = {};
    this.dialogRef.close(response);
  }

}
