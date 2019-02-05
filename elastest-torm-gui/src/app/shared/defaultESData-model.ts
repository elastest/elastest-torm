import { TJobExecModel } from '../elastest-etm/tjob-exec/tjobExec-model';
import { ParameterModel } from '../elastest-etm/parameter/parameter-model';

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
  tJobExec: TJobExecModel;
  activeView: string;

  constructor(component: string, streamType: string, stream?: string, tJobExec?: TJobExecModel) {
    this.component = component;
    this.stream = stream;
    this.streamType = streamType;
    this.activated = false;
    if (tJobExec) {
      this.tJobExec = new TJobExecModel(tJobExec);
      if (tJobExec.activeView) {
        this.activeView = tJobExec.activeView;
      }
    }

    /* *** Line Name In Legend *** */
    if (this.hasChildTJobExecWithMultiConfigParams()) {
      // If is Multi child, set configurations as name
      this.name = '';
      for (let param of tJobExec.parameters) {
        if (param.multiConfig) {
          if (this.name !== '') {
            this.name += ' | ';
          }

          this.name += param.name + '= ' + param.value;
        }
      }
    } else {
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

      let execPrefix: string = '';
      // if (exec !== undefined && exec !== '') {
      //   execPrefix = exec + '_';
      //   this.exec = exec;
      // }

      this.name = componentPrefix + execPrefix + streamPrefix + streamType;
    }
  }

  changeActive($event): void {
    this.activated = $event.checked;
  }

  hasChildTJobExec(): boolean {
    return this.tJobExec !== undefined && this.tJobExec !== null && this.tJobExec.isChild();
  }

  hasChildTJobExecWithMultiConfigParams(): boolean {
    return this.hasChildTJobExec() && this.tJobExec.parameters !== undefined && this.tJobExec.parameters.length > 0;
  }
}
