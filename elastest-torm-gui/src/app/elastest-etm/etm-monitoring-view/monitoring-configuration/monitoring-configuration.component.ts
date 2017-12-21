import {
  MetricbeatType,
  metricFieldGroupList,
  MetricFieldGroupModel,
  getMetricbeatFieldGroupIfItsMetricbeatType,
  getMetricBeatFieldGroupList,
  isMetricFieldGroup,
} from '../../../shared/metrics-view/metrics-chart-card/models/all-metrics-fields-model';
import { EtmLogsGroupComponent } from '../../../shared/logs-view/etm-logs-group/etm-logs-group.component';
import { TreeComponent } from 'angular-tree-component/dist/components/tree.component';
import { TJobExecModel } from '../../tjob-exec/tjobExec-model';
import { ElastestESService } from '../../../shared/services/elastest-es.service';
import { ESBoolQueryModel, ESTermModel } from '../../../shared/elasticsearch-model/es-query-model';
import { AgTreeCheckModel, TreeCheckElementModel } from '../../../shared/ag-tree-model';
import { Component, Inject, OnInit, Optional, ViewChild } from '@angular/core';
import { MD_DIALOG_DATA, MdDialogRef } from '@angular/material';
import { EtmComplexMetricsGroupComponent } from '../../../shared/metrics-view/metrics-chart-card/etm-complex-metrics-group/etm-complex-metrics-group.component';
import { defaultStreamMap } from '../../../shared/defaultESData-model';

@Component({
  selector: 'app-monitoring-configuration',
  templateUrl: './monitoring-configuration.component.html',
  styleUrls: ['./monitoring-configuration.component.scss']
})
export class MonitoringConfigurationComponent implements OnInit {
  @ViewChild('logsTree') logsTreeComponent: TreeComponent;
  @ViewChild('metricsTree') metricsTreeComponent: TreeComponent;

  logTree: AgTreeCheckModel;
  metricTree: AgTreeCheckModel;

  tJobExec: TJobExecModel;
  logCards: EtmLogsGroupComponent;
  metricCards: EtmComplexMetricsGroupComponent;

  loadingLogs: boolean;
  loadingMetrics: boolean;

  noLogs: boolean = false;
  noMetrics: boolean = false;

  metricbeatFieldGroupList: MetricFieldGroupModel[];

  constructor(
    private dialogRef: MdDialogRef<MonitoringConfigurationComponent>,
    private elastestESService: ElastestESService,
    @Optional() @Inject(MD_DIALOG_DATA) public inputObj: any,

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
    let componentStreamQuery: ESBoolQueryModel = new ESBoolQueryModel();
    let streamTypeTerm: ESTermModel = new ESTermModel();
    streamTypeTerm.name = 'stream_type';
    streamTypeTerm.value = 'log';
    componentStreamQuery.bool.must.termList.push(streamTypeTerm);

    let fieldsList: string[] = ['component', 'stream'];

    this.elastestESService.getAggTreeOfIndex(
      this.tJobExec.logIndex, fieldsList, componentStreamQuery.convertToESFormat()
    ).subscribe(
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
      }
      );
  }

  cleanMetricTree(metricTree: any[]): any[] { // Delete metricbeat non supported by ET (ej: system_core...)
    let componentPos: number = 0;

    for (let componentStreamType of metricTree) {
      if (componentStreamType.children) {
        let streamPos: number = 0;
        for (let streamType of componentStreamType.children) {
          if (streamType.children) {
            if (streamType.name !== defaultStreamMap.composed_metrics && streamType !== defaultStreamMap.atomic_metric) {
              let newTypeList: any[] = [];
              for (let type of streamType.children) {
                let typeName: string = type.name;
                let deleted: boolean = false;
                for (let metricbeatType in MetricbeatType) {
                  if (isNaN(parseInt(metricbeatType))) {
                    if (typeName.startsWith(metricbeatType + '_') && !isMetricFieldGroup(type.name, this.metricbeatFieldGroupList)) {
                      deleted = true;
                      break;
                    }
                  }
                }
                if (!deleted) {
                  newTypeList.push(type);
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
    let componentStreamTypeQuery: ESBoolQueryModel = new ESBoolQueryModel();
    let notStreamTypeTerm: ESTermModel = new ESTermModel();
    notStreamTypeTerm.name = 'stream_type';
    notStreamTypeTerm.value = 'log'; // Must NOT
    componentStreamTypeQuery.bool.mustNot.termList.push(notStreamTypeTerm);

    let notContainerMetric: ESTermModel = new ESTermModel();
    notContainerMetric.name = 'type';
    notContainerMetric.value = 'container';
    componentStreamTypeQuery.bool.mustNot.termList.push(notContainerMetric);

    let fieldsList: string[] = ['component', 'stream', 'type'];
    this.elastestESService.getAggTreeOfIndex(
      this.tJobExec.logIndex, fieldsList, componentStreamTypeQuery.convertToESFormat()
    ).subscribe(
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
      }

      );
  }

  loadSubtypesAndInitMetricTree(): void {
    for (let componentStreamType of this.metricTree.tree) {
      let component: string = componentStreamType.name;
      for (let streamType of componentStreamType.children) {
        let stream: string = streamType.name;
        for (let typeTree of streamType.children) {
          let type: string = typeTree.name;
          let currentMetricFieldGroupList: MetricFieldGroupModel[] = [];
          if (isMetricFieldGroup(type, this.metricbeatFieldGroupList)) { // If is Metricbeat type
            currentMetricFieldGroupList = this.metricbeatFieldGroupList;
          } else if (isMetricFieldGroup(type, metricFieldGroupList)) { // If it's Dockbeat type
            currentMetricFieldGroupList = metricFieldGroupList;
          }
          for (let metricFieldGroup of currentMetricFieldGroupList) {
            if (metricFieldGroup.type === type) {
              for (let subtype of metricFieldGroup.subtypes) {
                let subtypeTree: TreeCheckElementModel = new TreeCheckElementModel();
                subtypeTree.name = subtype.subtype;

                // If exist card, init checks
                let metricCardName: string = this.metricCards.createName(component, stream, type, subtypeTree.name);
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
          let type: string = typeSubtype.name;
          if (typeSubtype.children.length > 0) {
            for (let subtype of typeSubtype.children) {
              let metric: any = {
                component: component,
                stream: stream,
                metricName: type + '.' + subtype.name,
                activated: subtype.checked,
              };
              metricsList.push(metric);
            }
          } else {
            let metric: any = {
              component: component,
              stream: stream,
              metricName: type,
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
