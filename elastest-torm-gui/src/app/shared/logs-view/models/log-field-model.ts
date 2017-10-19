import { DefaultESFieldModel } from '../../defaultESData-model';
export class LogFieldModel extends DefaultESFieldModel {
    constructor(componentType: string, infoId?: string) {
        super(componentType, 'log', infoId);
    }

    changeActive($event) {
        this.activated = $event.checked;
    }
}