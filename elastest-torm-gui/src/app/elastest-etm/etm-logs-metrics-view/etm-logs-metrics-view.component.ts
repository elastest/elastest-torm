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

  constructor() { }

  ngOnInit() {
  }

  initView(tJob: TJobModel, tJobExec: TJobExecModel) {
    //Load logs
    this.logsGroup.initLogsView(tJob, tJobExec);

    //Load metrics
    this.metricsGroup.initMetricsView(tJob, tJobExec);
  }

  timelineEvent($event) {
    console.log($event)
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
}
