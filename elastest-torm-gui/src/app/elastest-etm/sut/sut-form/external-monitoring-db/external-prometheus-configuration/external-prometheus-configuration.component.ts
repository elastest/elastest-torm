import { Component, OnInit, Input } from '@angular/core';
import { ExternalPrometheus } from '../../../../external-monitoring-db/external-prometheus.model';
import { FormGroup, Validators, FormControl } from '@angular/forms';
import { SutService } from '../../../sut.service';
import { ExternalMonitoringDBService } from '../external-monitoring-db.service';

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

  externalPrometheusConnectedStatus: string = '';
  externalPrometheusConnectedStatusColor: string = '';
  externalPrometheusConnectedStatusIcon: string = '';
  externalPrometheusCheckingConnection: boolean = false;

  public prometheusFormGroup: FormGroup = new FormGroup({
    prometheusProtocol: new FormControl('', [Validators.required]),
    prometheusIp: new FormControl('', [Validators.required]),
    prometheusPort: new FormControl('', [Validators.required]),
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
    this.externalPrometheusCheckingConnection = true;
    this.externalMonitoringDBService.checkExternalPrometheusConnection(this.externalPrometheus).subscribe(
      (connected: boolean) => {
        if (connected) {
          this.externalPrometheusConnectedStatus = 'Connected';
          this.externalPrometheusConnectedStatusColor = '#7fac16';
          this.externalPrometheusConnectedStatusIcon = 'fiber_manual_record';
        } else {
          this.externalPrometheusConnectedStatus = 'Error';
          this.externalPrometheusConnectedStatusColor = '#cc200f';
          this.externalPrometheusConnectedStatusIcon = 'error';
        }
        this.externalPrometheusCheckingConnection = false;
      },
      (error: Error) => {
        console.log(error);
        this.externalPrometheusCheckingConnection = false;
      },
    );
  }

  isValidForm(): boolean {
    return this.prometheusFormGroup.valid;
  }
}
