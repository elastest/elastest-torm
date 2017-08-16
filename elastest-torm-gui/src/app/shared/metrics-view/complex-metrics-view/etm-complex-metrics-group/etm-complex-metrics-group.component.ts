import { TJobModel } from '../../../../elastest-etm/tjob/tjob-model';
import { ElastestESService } from '../../../services/elastest-es.service';
import { ETRESMetricsModel } from '../../models/et-res-metrics-model';
import { ESRabComplexMetricsModel } from '../models/es-rab-complex-metrics-model';
import { TJobExecModel } from '../../../../elastest-etm/tjob-exec/tjobExec-model';
import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'etm-complex-metrics-group',
  templateUrl: './etm-complex-metrics-group.component.html',
  styleUrls: ['./etm-complex-metrics-group.component.scss']
})
export class EtmComplexMetricsGroupComponent implements OnInit {
  @Input()
  public live: boolean;

  // Metrics Chart
  allInOneMetrics: ESRabComplexMetricsModel;
  metricsList: ETRESMetricsModel[] = [];
  groupedMetricsList: ETRESMetricsModel[][] = [];

  constructor(private elastestESService: ElastestESService) { }

  ngOnInit() {
  }

  initMetricsView(tJob: TJobModel, tJobExec: TJobExecModel) {
    if (tJob.execDashboardConfigModel.showComplexMetrics) {
      this.allInOneMetrics = new ESRabComplexMetricsModel(this.elastestESService);
      this.allInOneMetrics.name = 'Metrics';
      this.allInOneMetrics.hidePrevBtn = !this.live;
      this.allInOneMetrics.metricsIndex = tJobExec.logIndex;
      if (!this.live) {
        this.allInOneMetrics.getAllMetrics();
      }
    }
    for (let metric of tJob.execDashboardConfigModel.allMetricsFields.fieldsList) {
      if (metric.activated) {
        let etRESMetrics: ETRESMetricsModel = new ETRESMetricsModel(this.elastestESService, metric);
        etRESMetrics.hidePrevBtn = !this.live;
        etRESMetrics.metricsIndex = tJobExec.logIndex;
        if (!this.live) {
          etRESMetrics.getAllMetrics();
        }
        this.metricsList.push(etRESMetrics);
      }
    }
    this.groupedMetricsList = this.createGroupedArray(this.metricsList, 2);
  }

  createGroupedArray(arr, chunkSize) {
    let groups = [], i;
    for (i = 0; i < arr.length; i += chunkSize) {
      groups.push(arr.slice(i, i + chunkSize));
    }
    return groups;
  }

  updateData(data: any) {
    for (let group of this.groupedMetricsList) {
      for (let metric of group) {
        metric.updateData(data);
      }
    }

    this.allInOneMetrics.updateData(data);
  }
}
