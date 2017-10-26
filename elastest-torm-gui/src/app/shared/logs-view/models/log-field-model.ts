import { DefaultESFieldModel } from '../../defaultESData-model';
export class LogFieldModel extends DefaultESFieldModel {
    constructor(componentType: string, stream?: string) {
        super(componentType, 'log', stream);
    }

    changeActive($event) {
        this.activated = $event.checked;
    }
}