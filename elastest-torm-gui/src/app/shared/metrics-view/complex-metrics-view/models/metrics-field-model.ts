import { DefaultESFieldModel } from '../../../defaultESData-model';
import { Units } from './all-metrics-fields-model';


export class MetricsFieldModel extends DefaultESFieldModel {
    type: string;
    subtype: string;
    unit: Units | string;

    constructor(type: string, subtype: string, unit: Units  | string, componentType: string, infoId?: string, traceType?: string) {
        if(!traceType){
            traceType = 'metrics';
        }
        super(componentType, traceType, infoId);
        this.type = type;
        this.subtype = subtype;
        this.unit = unit;

        let namePrefix: string = this.name.split('metrics')[0]; // remove trace_type added at the end on super constructor
        namePrefix = this.name.split('beats_metrics')[0]; // remove default infoId if added at the end on super constructor
        this.name = namePrefix + type + '_' + subtype;

        this.activated = false;
    }
}