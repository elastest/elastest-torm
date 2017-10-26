import { ElastestESService } from '../../shared/services/elastest-es.service';
import { TJobExecModel } from '../tjob-exec/tjobExec-model';
import { TJobModel } from '../tjob/tjob-model';
import { EtmLogsGroupComponent } from '../../shared/logs-view/etm-logs-group/etm-logs-group.component';
import {
  EtmComplexMetricsGroupComponent,
} from '../../shared/metrics-view/complex-metrics-view/etm-complex-metrics-group/etm-complex-metrics-group.component';
import { Component, Input, OnInit, ViewChild } from '@angular/core';

@Component({
  selector: 'etm-logs-metrics-view',
  templateUrl: './etm-logs-metrics-view.component.html',
  styleUrls: ['./etm-logs-metrics-view.component.scss']
})
export class EtmLogsMetricsViewComponent implements OnInit {
  @ViewChild('metricsGroup') metricsGroup: EtmComplexMetricsGroupComponent;
  @ViewChild('logsGroup') logsGroup: EtmLogsGroupComponent;

  @Input()
  public live: boolean;

  tJob: TJobModel;
  tJobExec: TJobExecModel;

  componentType: string = '';
  stream: string = '';
  metricName: string = '';

  constructor(
    private elastestESService: ElastestESService,
  ) { }

  ngOnInit() {
  }

  initView(tJob: TJobModel, tJobExec: TJobExecModel) {
    this.tJob = tJob;
    this.tJobExec = tJobExec;

    //Load logs
    this.logsGroup.initLogsView(tJob, tJobExec);

    //Load metrics
    this.metricsGroup.initMetricsView(tJob, tJobExec);
  }

  timelineEvent($event) {
    if (!$event.unselect) {
      this.logsGroup.selectTimeRange($event.domain);
    } else {
      this.logsGroup.unselectTimeRange();
    }
  }

  hoverEvent(time) {
    this.logsGroup.selectTracesByTime(time);
  }

  leaveEvent() {
    this.logsGroup.unselectTraces();
  }

  addMore() {
    if (this.isInit()) {
      this.elastestESService.searchAllDynamic(this.tJobExec.logIndex, this.stream, this.componentType, this.metricName)
        .subscribe(
        (obj: any) => {
          if (obj.traceType === 'log') {
            this.logsGroup.addMoreLogs(obj);
          } else if (obj.traceType === 'composed_metrics') {
            this.metricsGroup.addMoreMetrics(obj);
          } else if (obj.traceType === 'atomic_metric') {
            this.metricsGroup.addMoreMetrics(obj);
          }

          this.componentType = '';
          this.stream = '';
          this.metricName = '';
        },
        (error) => console.log(error),
      );
    }
  }

  isInit() {
    return this.tJobExec !== undefined;
  }
}
