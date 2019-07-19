import { DefaultESFieldModel } from '../../../defaultESData-model';
import { Units } from './all-metrics-fields-model';
import { TJobExecModel } from '../../../../elastest-etm/tjob-exec/tjobExec-model';

export class MetricsFieldModel extends DefaultESFieldModel {
  // like cpu
  etType: string;
  // like totalUsage (default)
  subtype: string;
  unit: Units | string;
  // like totalUsage (default)
  metricName: string;

  constructor(
    etType: string,
    subtype: string,
    unit: Units | string,
    component: string,
    stream?: string,
    streamType?: string,
    activated: boolean = false,
    tJobExec?: TJobExecModel,
    metricName?: string,
  ) {
    if (streamType === undefined) {
      streamType = 'composed_metrics';
    }
    super(component, streamType, stream, tJobExec);
    this.etType = etType;
    this.subtype = subtype;
    this.unit = unit;

    let namePrefix: string = this.name.split(streamType)[0]; // remove stream_type added at the end on super constructor
    // namePrefix = namePrefix.split('et_dockbeat')[0]; // remove default stream if added at the end on super constructor

    let namePrefixSeparator: string = '';

    // Multi TJobExec Child
    if (this.hasChildTJobExecWithMultiConfigParams()) {
      namePrefixSeparator = ' ';
    }

    this.name = namePrefix + namePrefixSeparator + etType + '_' + subtype;

    this.activated = activated;
    this.metricName = metricName;
  }

  componentIsEmpty(): boolean {
    return this.component === undefined || this.component === '';
  }
}
