import { DefaultESFieldModel } from '../../defaultESData-model';
export class LogFieldModel extends DefaultESFieldModel {
    constructor(component: string, stream?: string) {
        super(component, 'log', stream);
    }

    changeActive($event) {
        this.activated = $event.checked;
    }
}