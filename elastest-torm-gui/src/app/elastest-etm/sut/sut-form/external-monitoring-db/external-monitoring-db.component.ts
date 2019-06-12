import { Component, OnInit, Input } from '@angular/core';
import { ExternalMonitoringDBForLogs } from '../../../external-monitoring-db/external-monitoring-db-for-logs.model';
import { ExternalMonitoringDBForMetrics } from '../../../external-monitoring-db/external-monitoring-db-for-metrics.model';
import { ParameterModel } from '../../../parameter/parameter-model';
import { SutService } from '../../sut.service';
import { SutModel } from '../../sut-model';
import { ExternalElasticsearch } from '../../../external-monitoring-db/external-elasticsearch.model';

@Component({
  selector: 'etm-external-monitoring-db',
  templateUrl: './external-monitoring-db.component.html',
  styleUrls: ['./external-monitoring-db.component.scss'],
})
export class ExternalMonitoringDbComponent implements OnInit {
  @Input()
  sut: SutModel;

  // External Elasticsearch
  esIndicesParamName: string = 'EXT_ELASTICSEARCH_INDICES';

  extESConnectedStatus: string = '';
  extESConnectedStatusColor: string = '';
  extESConnectedStatusIcon: string = '';
  extESCheckingConnection: boolean = false;
  extESFilterFieldsLabel: string = 'You can filter by fields and values';
  extESFilterFieldsSubLabel: string =
    'ElasTest will save only traces that contain the specified values of the specified fields.' +
    ' If more than one field is specified, the combinatorics are performed using AND. Values for field are made by OR. Fields must be root fields';

  constructor(private sutService: SutService) {}

  ngOnInit(): void {
    if (!this.sut.externalMonitoringDBForLogs) {
      this.sut.externalMonitoringDBForLogs = new ExternalMonitoringDBForLogs();
    }

    if (!this.sut.externalMonitoringDBForMetrics) {
      this.sut.externalMonitoringDBForMetrics = new ExternalMonitoringDBForMetrics();
    }
  }

  switchUseESIndicesByExecution($event): void {
    this.sut.externalMonitoringDBForLogs.getExternalElasticsearch().useESIndicesByExecution = $event.checked;

    if (
      this.sut.parameters === undefined ||
      this.sut.parameters === null ||
      this.sut.externalMonitoringDBForLogs.getExternalElasticsearch() === undefined ||
      this.sut.externalMonitoringDBForLogs.getExternalElasticsearch() === null
    ) {
      return;
    }

    let index: number = 0;
    for (let param of this.sut.parameters) {
      if (param.name === this.esIndicesParamName) {
        this.sut.parameters.splice(index, 1);
        this.sut.parameters = [...this.sut.parameters];

        break;
      }
      index += 1;
    }

    if (this.sut.externalMonitoringDBForLogs.getExternalElasticsearch().useESIndicesByExecution) {
      let indiceParam: ParameterModel = new ParameterModel();
      indiceParam.name = this.esIndicesParamName;
      indiceParam.value = this.sut.externalMonitoringDBForLogs.getExternalElasticsearch().indices;
      this.sut.parameters.push(indiceParam);
    }
  }

  checkExternalESConnection(): void {
    this.extESCheckingConnection = true;
    this.sutService
      .checkExternalElasticsearchConnection(this.sut.externalMonitoringDBForLogs.getExternalElasticsearch())
      .subscribe(
        (connected: boolean) => {
          if (connected) {
            this.extESConnectedStatus = 'Connected';
            this.extESConnectedStatusColor = '#7fac16';
            this.extESConnectedStatusIcon = 'fiber_manual_record';
          } else {
            this.extESConnectedStatus = 'Error';
            this.extESConnectedStatusColor = '#cc200f';
            this.extESConnectedStatusIcon = 'error';
          }
          this.extESCheckingConnection = false;
        },
        (error: Error) => {
          console.log(error);
          this.extESCheckingConnection = false;
        },
      );
  }
}
