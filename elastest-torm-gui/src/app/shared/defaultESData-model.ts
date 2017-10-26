export let componentTypes: string[] = [
    'test',
    'sut',
];

// Default stream values for each traceType
export let defaultStreamMap: any = {
    log: 'default_log',
    metrics: 'beats_metrics',
}

export class DefaultESFieldModel {
    componentType: string;
    stream: string;
    traceType: string;
    name: string;
    activated: boolean;

    constructor(componentType: string, traceType: string, stream?: string) {
        this.componentType = componentType;
        this.stream = stream;
        this.traceType = traceType;

        let componentTypePrefix: string = '';
        if (componentType !== undefined && componentType !== '') {
            componentTypePrefix = componentType + '_';
        }

        let streamPrefix: string = '';
        if (stream === undefined || stream === '') {
            stream = defaultStreamMap[traceType];
            this.stream = stream;
        }
        streamPrefix = stream + '_';


        this.name = componentTypePrefix + streamPrefix + traceType;
        this.activated = false;
    }

    changeActive($event) {
        this.activated = $event.checked;
    }
}
