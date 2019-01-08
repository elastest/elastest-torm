import { EtmLogsGroupComponent } from '../etm-logs-group/etm-logs-group.component';
import {
  MetricbeatType,
  metricFieldGroupList,
  MetricFieldGroupModel,
  getMetricBeatFieldGroupList,
  isMetricFieldGroup,
} from '../../../shared/metrics-view/metrics-chart-card/models/all-metrics-fields-model';
import { TreeComponent } from 'angular-tree-component/dist/components/tree.component';
import { TJobExecModel } from '../../tjob-exec/tjobExec-model';
import { AgTreeCheckModel, TreeCheckElementModel } from '../../../shared/ag-tree-model';
import { Component, Inject, OnInit, Optional, ViewChild } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material';
import { EtmChartGroupComponent } from '../etm-chart-group/etm-chart-group.component';
import { defaultStreamMap } from '../../../shared/defaultESData-model';
import { MonitoringService } from '../../../shared/services/monitoring.service';

@Component({
  selector: 'monitoring-configuration',
  templateUrl: './monitoring-configuration.component.html',
  styleUrls: ['./monitoring-configuration.component.scss'],
})
export class MonitoringConfigurationComponent implements OnInit {
  @ViewChild('logsTree') logsTreeComponent: TreeComponent;
  @ViewChild('metricsTree') metricsTreeComponent: TreeComponent;

  logTree: AgTreeCheckModel;
  metricTree: AgTreeCheckModel;

  tJobExec: TJobExecModel;
  logCards: EtmLogsGroupComponent;
  metricCards: EtmChartGroupComponent;

  loadingLogs: boolean;
  loadingMetrics: boolean;

  noLogs: boolean = false;
  noMetrics: boolean = false;

  metricbeatFieldGroupList: MetricFieldGroupModel[];

  constructor(
    private dialogRef: MatDialogRef<MonitoringConfigurationComponent>,
    private monitoringService: MonitoringService,
    @Optional()
    @Inject(MAT_DIALOG_DATA)
    public inputObj: any,
  ) {
    this.tJobExec = inputObj.exec;
    this.logCards = inputObj.logCards;
    this.metricCards = inputObj.metricCards;
    this.metricbeatFieldGroupList = getMetricBeatFieldGroupList();
  }

  ngOnInit() {
    this.loadTrees();
  }

  loadTrees(): void {
    this.loadingLogs = true;
    this.loadingMetrics = true;

    this.noLogs = false;
    this.noMetrics = false;

    this.logTree = new AgTreeCheckModel();
    this.metricTree = new AgTreeCheckModel();

    this.loadLogsTree();
    this.loadMetricsTree();
  }

  loadLogsTree(): void {
    this.monitoringService.getLogsTree(this.tJobExec).subscribe(
      (logTree: any[]) => {
        this.logTree.setByObjArray(logTree);
        if (this.logTree.tree.length === 0) {
          this.noLogs = true;
          this.loadingLogs = false;
        } else {
          // If exist card, init checks
          for (let logCard of this.logCards.logsList) {
            for (let componentStream of this.logTree.tree) {
              if (logCard.component === componentStream.name) {
                for (let stream of componentStream.children) {
                  if (logCard.stream === stream.name) {
                    stream.checked = true;
                  }
                }
                break;
              }
            }
          }
          this.loadingLogs = false;

          this.logsTreeComponent.treeModel.update();
          this.logsTreeComponent.treeModel.expandAll();
          this.logTree.updateCheckboxes(this.logsTreeComponent.treeModel.roots);
        }
      },
      (error) => {
        this.loadingLogs = false;
        this.noLogs = true;
      },
    );
  }

  cleanMetricTree(metricTree: any[]): any[] {
    // Delete metricbeat non supported by ET (ej: system_core...)
    let componentPos: number = 0;

    for (let componentStreamType of metricTree) {
      if (componentStreamType.children) {
        let streamPos: number = 0;
        for (let streamType of componentStreamType.children) {
          if (streamType.children) {
            if (streamType.name !== defaultStreamMap.composed_metrics && streamType !== defaultStreamMap.atomic_metric) {
              let newTypeList: any[] = [];
              for (let etType of streamType.children) {
                let typeName: string = etType.name;
                let deleted: boolean = false;
                for (let metricbeatType in MetricbeatType) {
                  if (isNaN(parseInt(metricbeatType))) {
                    if (
                      typeName.startsWith(metricbeatType + '_') &&
                      !isMetricFieldGroup(etType.name, this.metricbeatFieldGroupList)
                    ) {
                      deleted = true;
                      break;
                    }
                  }
                }
                if (!deleted) {
                  newTypeList.push(etType);
                }
              }
              metricTree[componentPos].children[streamPos].children = newTypeList;
            }
          }
          streamPos++;
        }
      }
      componentPos++;
    }
    return metricTree;
  }

  loadMetricsTree(): void {
    this.monitoringService.getMetricsTree(this.tJobExec).subscribe(
      (metricTree: any[]) => {
        metricTree = this.cleanMetricTree(metricTree);
        this.metricTree.setByObjArray(metricTree);
        if (this.metricTree.tree.length === 0) {
          this.noMetrics = true;
          this.loadingMetrics = false;
        } else {
          this.loadSubtypesAndInitMetricTree();

          this.loadingMetrics = false;
          this.metricsTreeComponent.treeModel.update();
          this.metricsTreeComponent.treeModel.expandAll();
          this.metricTree.updateCheckboxes(this.metricsTreeComponent.treeModel.roots);
        }
      },
      (error) => {
        this.loadingMetrics = false;
        this.noMetrics = true;
      },
    );
  }

  loadSubtypesAndInitMetricTree(): void {
    for (let componentStreamType of this.metricTree.tree) {
      let component: string = componentStreamType.name;
      for (let streamType of componentStreamType.children) {
        let stream: string = streamType.name;
        for (let typeTree of streamType.children) {
          let etType: string = typeTree.name;
          let currentMetricFieldGroupList: MetricFieldGroupModel[] = [];
          if (isMetricFieldGroup(etType, this.metricbeatFieldGroupList)) {
            // If is Metricbeat etType
            currentMetricFieldGroupList = this.metricbeatFieldGroupList;
          } else if (isMetricFieldGroup(etType, metricFieldGroupList)) {
            // If it's Dockbeat etType
            currentMetricFieldGroupList = metricFieldGroupList;
          }
          for (let metricFieldGroup of currentMetricFieldGroupList) {
            if (metricFieldGroup.etType === etType) {
              for (let subtype of metricFieldGroup.subtypes) {
                let subtypeTree: TreeCheckElementModel = new TreeCheckElementModel();
                subtypeTree.name = subtype.subtype;

                // If exist card, init checks
                let metricCardName: string = this.metricCards.createName(component, stream, etType, subtypeTree.name);
                if (this.metricCards.alreadyExist(metricCardName)) {
                  subtypeTree.checked = true;
                }

                typeTree.children.push(subtypeTree);
              }
              break;
            }
          }
        }
      }
    }
  }

  getLogsList(): any[] {
    let logsList: any[] = [];
    for (let componentStream of this.logTree.tree) {
      for (let stream of componentStream.children) {
        let log: any = {
          component: componentStream.name,
          stream: stream.name,
          activated: stream.checked,
        };
        logsList.push(log);
      }
    }
    return logsList;
  }

  getMetricsList(): any {
    let metricsList: any[] = [];
    for (let componentStreamTypeSubtype of this.metricTree.tree) {
      let component: string = componentStreamTypeSubtype.name;
      for (let streamTypeSubtype of componentStreamTypeSubtype.children) {
        let stream: string = streamTypeSubtype.name;
        for (let typeSubtype of streamTypeSubtype.children) {
          let etType: string = typeSubtype.name;
          if (typeSubtype.children.length > 0) {
            for (let subtype of typeSubtype.children) {
              let metric: any = {
                component: component,
                stream: stream,
                metricName: etType + '.' + subtype.name,
                activated: subtype.checked,
              };
              metricsList.push(metric);
            }
          } else {
            let metric: any = {
              component: component,
              stream: stream,
              metricName: etType,
              activated: typeSubtype.checked,
            };
            metricsList.push(metric);
          }
        }
      }
    }
    return metricsList;
  }

  applyAndSave(): void {
    this.applyConfig(true);
  }

  applyConfig(withSave: boolean = false): void {
    let logsList: any[] = this.getLogsList();
    let metricsList: any[] = this.getMetricsList();
    let response: any = {
      logsList: logsList,
      metricsList: metricsList,
      withSave: withSave,
    };
    this.dialogRef.close(response);
  }
}
