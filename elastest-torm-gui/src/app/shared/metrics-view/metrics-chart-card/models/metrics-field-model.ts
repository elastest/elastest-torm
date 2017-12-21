import { DefaultESFieldModel } from '../../../defaultESData-model';
import { Units } from './all-metrics-fields-model';

export class MetricsFieldModel extends DefaultESFieldModel {
    type: string;
    subtype: string;
    unit: Units | string;

    constructor(type: string, subtype: string, unit: Units | string,
        component: string, stream?: string, streamType?: string, activated: boolean = false
    ) {
        if (streamType === undefined) {
            streamType = 'composed_metrics';
        }
        super(component, streamType, stream);
        this.type = type;
        this.subtype = subtype;
        this.unit = unit;

        let namePrefix: string = this.name.split(streamType)[0]; // remove stream_type added at the end on super constructor
        // namePrefix = namePrefix.split('et_dockbeat')[0]; // remove default stream if added at the end on super constructor
        this.name = namePrefix + type + '_' + subtype;
        this.activated = activated;
    }
}