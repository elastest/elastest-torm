import { Component, OnInit, Input } from '@angular/core';
import { ExternalElasticsearch } from '../../../../external-monitoring-db/external-elasticsearch.model';
import { ParameterModel } from '../../../../parameter/parameter-model';
import { SutService } from '../../../sut.service';

@Component({
  selector: 'etm-external-elasticsearch-configuration',
  templateUrl: './external-elasticsearch-configuration.component.html',
  styleUrls: ['./external-elasticsearch-configuration.component.scss'],
})
export class ExternalElasticsearchConfigurationComponent implements OnInit {
  @Input()
  externalES: ExternalElasticsearch;

  @Input()
  monitoringType: 'logs' | 'metrics';

  @Input()
  getParameters: Function;

  @Input()
  setParameters: Function;

  // External Elasticsearch

  extESConnectedStatus: string = '';
  extESConnectedStatusColor: string = '';
  extESConnectedStatusIcon: string = '';
  extESCheckingConnection: boolean = false;
  extESFilterFieldsLabel: string = 'You can filter by fields and values';
  extESFilterFieldsSubLabel: string =
    'ElasTest will save only traces that contain the specified values of the specified fields.' +
    ' If more than one field is specified, the combinatorics are performed using AND. Values for field are made by OR. Fields must be root fields';

  constructor(private sutService: SutService) {}

  ngOnInit(): void {}

  switchUseESIndicesByExecution($event): void {
    this.externalES.useESIndicesByExecution = $event.checked;

    let parameters: ParameterModel[] = this.getParameters();

    if (parameters === undefined || parameters === null || this.externalES === undefined || this.externalES === null) {
      return;
    }

    let index: number = 0;
    for (let param of parameters) {
      if (param.name === this.externalES.getLogIndicesParamName()) {
        parameters.splice(index, 1);
        this.setParameters(parameters);
        parameters = this.getParameters();
        break;
      }
      index += 1;
    }

    if (this.externalES.useESIndicesByExecution) {
      let indiceParam: ParameterModel = new ParameterModel();
      indiceParam.name = this.externalES.getLogIndicesParamName();
      indiceParam.value = this.externalES.indices;
      parameters.push(indiceParam);
      this.setParameters(parameters);
    }
  }

  checkExternalESConnection(): void {
    this.extESCheckingConnection = true;
    this.sutService.checkExternalElasticsearchConnection(this.externalES).subscribe(
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
