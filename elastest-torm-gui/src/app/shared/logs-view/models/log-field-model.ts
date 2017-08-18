export class LogFieldModel {
    componentType: string;
    name: string;
    activated: boolean;

    constructor(componentType: string) {
        this.componentType = componentType;

        let componentTypePrefix: string = '';
        if (componentType !== undefined && componentType !== '') {
            componentTypePrefix = componentType + '_';
        }

        this.name = componentTypePrefix + 'log';
        this.activated = false;
    }

    changeActive($event) {
        this.activated = $event.checked;
    }
}