import { Component, OnInit, Input } from '@angular/core';
import { ExternalPrometheus } from '../external-prometheus.model';
import { FormGroup, Validators, FormControl } from '@angular/forms';
import { ExternalMonitoringDBService } from '../../sut/sut-form/external-monitoring-db/external-monitoring-db.service';

@Component({
  selector: 'etm-external-prometheus-configuration',
  templateUrl: './external-prometheus-configuration.component.html',
  styleUrls: ['./external-prometheus-configuration.component.scss'],
})
export class ExternalPrometheusConfigurationComponent implements OnInit {
  @Input()
  externalPrometheus: ExternalPrometheus;

  @Input()
  monitoringType: 'logs' | 'metrics';

  ready: boolean = false;

  connectedStatus: string = '';
  connectedStatusColor: string = '';
  connectedStatusIcon: string = '';
  checkingConnection: boolean = false;

  filterFieldsLabel: string = 'You can filter by fields and values';
  filterFieldsSubLabel: string =
    'ElasTest will save only traces that contain the specified values of the specified fields.' +
    ' If more than one field is specified, the combinatorics are performed using AND. Values for field are made by OR. Fields must be root fields';

  public prometheusFormGroup: FormGroup = new FormGroup({
    prometheusProtocol: new FormControl('', [Validators.required]),
    prometheusIp: new FormControl('', [Validators.required]),
    prometheusPort: new FormControl('', [Validators.required]),
    traceNameField: new FormControl('', [Validators.required]),
  });

  constructor(private externalMonitoringDBService: ExternalMonitoringDBService) {}

  ngOnInit(): void {
    if (
      this.externalPrometheus &&
      this.externalPrometheus.id !== undefined &&
      this.externalPrometheus.id !== null &&
      this.externalPrometheus.id > 0
    ) {
      this.externalMonitoringDBService
        .getExternalPrometheusById(this.externalPrometheus.id)
        .subscribe((externalPrometheus: ExternalPrometheus) => {
          this.externalPrometheus.initByGiven(externalPrometheus);
          this.ready = true;
        });
    } else {
      this.ready = true;
    }
  }

  checkExternalPrometheusConnection(): void {
    this.checkingConnection = true;
    this.externalMonitoringDBService.checkExternalPrometheusConnection(this.externalPrometheus).subscribe(
      (connected: boolean) => {
        if (connected) {
          this.connectedStatus = 'Connected';
          this.connectedStatusColor = '#7fac16';
          this.connectedStatusIcon = 'fiber_manual_record';
        } else {
          this.connectedStatus = 'Error';
          this.connectedStatusColor = '#cc200f';
          this.connectedStatusIcon = 'error';
        }
        this.checkingConnection = false;
      },
      (error: Error) => {
        console.log(error);
        this.checkingConnection = false;
      },
    );
  }

  isValidForm(): boolean {
    return this.prometheusFormGroup.valid;
  }
}
