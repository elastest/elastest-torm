import { ExternalMonitoringDB } from './external-monitoring-db.model';

export class ExternalPrometheus extends ExternalMonitoringDB {
  // CONSTANT
  monitoringType: string = 'PROMETHEUS';

  id: number;
  protocol: 'http' | 'https' | ''; // On add new, add too in getProtocolsList
  ip: string;
  port: string;
  path: string;
  user: string;
  pass: string;

  constructor(externalPrometheusJson: any = undefined) {
    super(externalPrometheusJson);
    if (!externalPrometheusJson) {
    } else {
    }
  }
}
