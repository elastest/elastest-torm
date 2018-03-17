export let components: string[] = ['test', 'sut'];

// Default stream values for each streamType
export let defaultStreamMap: any = {
  log: 'default_log',
  composed_metrics: 'et_dockbeat',
  atomic_metric: 'et_dockbeat',
};

export class DefaultESFieldModel {
  component: string;
  stream: string;
  streamType: string;
  name: string;
  activated: boolean;

  constructor(component: string, streamType: string, stream?: string) {
    this.component = component;
    this.stream = stream;
    this.streamType = streamType;

    let componentPrefix: string = '';
    if (component !== undefined && component !== '') {
      componentPrefix = component + '_';
    }

    let streamPrefix: string = '';
    if (stream === undefined || stream === '') {
      stream = defaultStreamMap[streamType];
      this.stream = stream;
    }
    streamPrefix = stream + '_';

    this.name = componentPrefix + streamPrefix + streamType;
    this.activated = false;
  }

  changeActive($event) {
    this.activated = $event.checked;
  }
}
