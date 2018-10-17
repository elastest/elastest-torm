import { DefaultESFieldModel } from '../../../defaultESData-model';
import { Units } from './all-metrics-fields-model';

export class MetricsFieldModel extends DefaultESFieldModel {
  etType: string;
  subtype: string;
  unit: Units | string;

  constructor(
    etType: string,
    subtype: string,
    unit: Units | string,
    component: string,
    stream?: string,
    streamType?: string,
    activated: boolean = false,
    exec?: string,
  ) {
    if (streamType === undefined) {
      streamType = 'composed_metrics';
    }
    super(component, streamType, stream, exec);
    this.etType = etType;
    this.subtype = subtype;
    this.unit = unit;

    let namePrefix: string = this.name.split(streamType)[0]; // remove stream_type added at the end on super constructor
    // namePrefix = namePrefix.split('et_dockbeat')[0]; // remove default stream if added at the end on super constructor
    this.name = namePrefix + etType + '_' + subtype;
    this.activated = activated;
  }

  componentIsEmpty(): boolean {
    return this.component === undefined || this.component === '';
  }
}
