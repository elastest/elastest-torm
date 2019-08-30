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
  allInOneMetricsActivated: boolean = false;

  tJobExec: TJobExecModel;
  logCards: EtmLogsGroupComponent;
  metricCards: EtmChartGroupComponent;

  loadingLogs: boolean;
  loadingMetrics: boolean;

  noLogs: boolean = false;
  noMetrics: boolean = false;
  combineMetrics: boolean = false;
  metricbeatFieldGroupList: MetricFieldGroupModel[];

  hideLogs: boolean = false;
  hideMetrics: boolean = false;

  logsToCompare: any[] = [];

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
    this.combineMetrics = inputObj.combineMetricsInPairs;
    this.metricbeatFieldGroupList = getMetricBeatFieldGroupList();
    this.hideLogs = inputObj.hideLogs !== undefined ? inputObj.hideLogs : false;
    this.hideMetrics = inputObj.hideMetrics !== undefined ? inputObj.hideMetrics : false;

    this.allInOneMetricsActivated = this.metricCards.isAllInOneMetricsCardShowing();
  }

  ngOnInit(): void {
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
    if (!this.hideLogs && this.logCards) {
      this.monitoringService.getLogsTree(this.tJobExec).subscribe(
        (logTree: any[]) => {
          this.logTree.setByObjArray(logTree);
          if (this.logTree.tree.length === 0) {
            this.noLogs = true;
            this.loadingLogs = false;
          } else {
            // If exist card in logsList , init checks
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

            // If exist card in logsComparisonMap , init checks
            for (let key of Array.from(this.logCards.getLogComparatorKeys())) {
              for (let logComparison of this.logCards.getLogComparatorModel().get(key)) {
                for (let componentStream of this.logTree.tree) {
                  if (logComparison.component === componentStream.name) {
                    for (let stream of componentStream.children) {
                      if (logComparison.stream === stream.name) {
                        stream.checked = true;
                      }
                    }
                    break;
                  }
                }
              }
            }

            this.loadingLogs = false;

            this.logsTreeComponent.treeModel.update();
            this.logsTreeComponent.treeModel.expandAll();
            this.logTree.updateCheckboxes(this.logsTreeComponent.treeModel.roots);
          }
        },
        (error: Error) => {
          this.loadingLogs = false;
          this.noLogs = true;
        },
      );
    }
  }

  cleanMetricTree(metricTree: any[]): any[] {
    // Delete metricbeat non supported by ET (ej: system_core...)
    let componentPos: number = 0;

    // Component -> Stream -> EtType (And after 1.5.0 version) -> StreamType
    for (let componentStreamEtType of metricTree) {
      if (componentStreamEtType.children) {
        let streamPos: number = 0;
        for (let streamEtType of componentStreamEtType.children) {
          if (streamEtType.children) {
            if (streamEtType.name !== defaultStreamMap.composed_metrics && streamEtType !== defaultStreamMap.atomic_metric) {
              let newTypeList: any[] = [];
              for (let etType of streamEtType.children) {
                let ettypeName: string = etType.name;
                let deleted: boolean = false;
                for (let metricbeatType in MetricbeatType) {
                  if (isNaN(parseInt(metricbeatType))) {
                    if (
                      ettypeName.startsWith(metricbeatType + '_') &&
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
      (error: Error) => {
        this.loadingMetrics = false;
        this.noMetrics = true;
      },
    );
  }

  loadSubtypesAndInitMetricTree(): void {
    // Component -> Stream -> EtType (And after 1.5.0 version) -> StreamType
    for (let componentStreamEtType of this.metricTree.tree) {
      let component: string = componentStreamEtType.name;
      for (let streamEtType of componentStreamEtType.children) {
        let stream: string = streamEtType.name;
        for (let ettypeStreamTypeTree of streamEtType.children) {
          let etType: string = ettypeStreamTypeTree.name;

          // If has streamType (since after 1.5.0v)
          // Add streamType to additionalData field and remove from children
          // Because children is used after to add subtypes
          let streamTypesList: string[] = [];
          if (ettypeStreamTypeTree.children.length > 0) {
            for (let streamTypeTree of ettypeStreamTypeTree.children) {
              if (streamTypeTree.name) {
                streamTypesList.push(streamTypeTree.name);
              }
            }
            ettypeStreamTypeTree.additionalData['streamTypes'] = streamTypesList;
            ettypeStreamTypeTree.children = [];
          }

          let currentMetricFieldGroupList: MetricFieldGroupModel[] = [];
          if (isMetricFieldGroup(etType, this.metricbeatFieldGroupList)) {
            // If is Metricbeat etType
            currentMetricFieldGroupList = this.metricbeatFieldGroupList;
          } else if (isMetricFieldGroup(etType, metricFieldGroupList)) {
            // If it's Dockbeat etType
            currentMetricFieldGroupList = metricFieldGroupList;
          } else {
            if (streamTypesList.indexOf('composed_metrics') > -1) {
              // TODO add as subtype
            }
          }

          // Add subtype manually for default metrics (dockbeat and metricbeat)
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

                ettypeStreamTypeTree.children.push(subtypeTree);
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
        if (this.tJobExec instanceof TJobExecModel && this.tJobExec.isParent()) {
          this.logsToCompare.push(log);
        } else {
          logsList.push(log);
        }
      }
    }
    return logsList;
  }

  getMetricsList(): any {
    let metricsList: any[] = [];
    // Component -> Stream -> EtType (And after 1.5.0 version) -> StreamType
    for (let componentStreamEtTypeSubtype of this.metricTree.tree) {
      let component: string = componentStreamEtTypeSubtype.name;
      for (let streamEtTypeSubtype of componentStreamEtTypeSubtype.children) {
        let stream: string = streamEtTypeSubtype.name;
        for (let ettypeSubtype of streamEtTypeSubtype.children) {
          let etType: string = ettypeSubtype.name;
          let streamTypes: string[];
          if (
            ettypeSubtype.additionalData &&
            ettypeSubtype.additionalData['streamTypes'] &&
            ettypeSubtype.additionalData['streamTypes']
          ) {
            streamTypes = ettypeSubtype.additionalData['streamTypes'];
          }

          let metricsToAdd: any[] = [];
          if (streamTypes && streamTypes.length > 0) {
            for (let streamType of streamTypes) {
              metricsToAdd = this.getMetrics(
                component,
                stream,
                etType,
                ettypeSubtype.checked,
                ettypeSubtype.children,
                streamType,
              );
            }
          } else {
            metricsToAdd = this.getMetrics(component, stream, etType, ettypeSubtype.checked, ettypeSubtype.children);
          }

          if (metricsToAdd) {
            metricsList = metricsList.concat(metricsToAdd);
          }
        }
      }
    }
    return metricsList;
  }

  getMetrics(
    component: string,
    stream: string,
    etType: string,
    checked: boolean,
    subtypes: TreeCheckElementModel[] = [],
    streamType?: string,
  ): any[] {
    let metrics: any[] = [];
    // Can be multiple subtipes (metricbeat, for example has: system_cpu[total, user...])
    if (subtypes && subtypes.length > 0) {
      for (let subtype of subtypes) {
        if (subtype && subtype.checked) {
          let metric: any = {
            component: component,
            stream: stream,
            etType: etType,
            metricName: etType + '.' + subtype.name,
            subtype: subtype.name,
            activated: subtype.checked,
            streamType: streamType,
          };
          metrics.push(metric);
        }
      }
    } else {
      let metric: any = {
        component: component,
        stream: stream,
        etType: etType,
        metricName: etType,
        activated: checked,
        streamType: streamType,
      };
      metrics.push(metric);
    }

    return metrics;
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
      logsToCompare: this.logsToCompare,
      withSave: withSave,
      allInOneMetricsActivated: this.allInOneMetricsActivated,
      combineMetricsPairs: this.combineMetrics,
    };

    this.dialogRef.close(response);
  }
}
