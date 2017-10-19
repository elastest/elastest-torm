export let componentTypes: string[] = [
    'test',
    'sut',
];

// Default infoId values for each traceType
export let defaultInfoIdMap: any = {
    log: 'default_log',
    metrics: 'beats_metrics',
}

export class DefaultESFieldModel {
    componentType: string;
    infoId: string;
    traceType: string;
    name: string;
    activated: boolean;

    constructor(componentType: string, traceType: string, infoId?: string) {
        this.componentType = componentType;
        this.infoId = infoId;
        this.traceType = traceType;

        let componentTypePrefix: string = '';
        if (componentType !== undefined && componentType !== '') {
            componentTypePrefix = componentType + '_';
        }

        let infoIdPrefix: string = '';
        if (infoId === undefined || infoId === '') {
            infoId = defaultInfoIdMap[traceType];
        }
        infoIdPrefix = infoId + '_';


        this.name = componentTypePrefix + infoIdPrefix + traceType;
        this.activated = false;
    }

    changeActive($event) {
        this.activated = $event.checked;
    }
}
