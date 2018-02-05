import { DefaultESFieldModel, components } from '../../defaultESData-model';
import { LogFieldModel } from './log-field-model';
import { ESRabLogModel } from './es-rab-log-model';

export class AllLogsTypesModel {
  logsList: LogFieldModel[];

  constructor(ignoreComponent: string = '', activated: boolean = true) {
    this.logsList = [];
    for (let component of components) {
      if (component !== ignoreComponent) {
        this.createFieldsListByComponent(component, activated);
      }
    }
  }

  getPositionByName(name: string): number {
    let position: number;
    let counter: number = 0;
    for (let log of this.logsList) {
      if (log.name === name) {
        position = counter;
        break;
      }
      counter++;
    }
    if (position === undefined) {
      // If no position, return new position
      position = this.logsList.length;
    }
    return position;
  }

  addLogFieldToList(name: string, component: string, stream: string, activated: boolean = false): void {
    let alreadySaved: boolean = false;
    for (let logField of this.logsList) {
      if (logField.name === name) {
        alreadySaved = true;
        logField.activated = activated;
        break;
      }
    }
    if (!alreadySaved) {
      this.createFieldsListByComponentAndStream(component, stream, activated);
    }
  }

  createFieldsListByComponent(component: string, activated: boolean = true): void {
    this.createFieldsListByComponentAndStream(component, undefined, activated);
  }

  createFieldsListByComponentAndStream(component: string, stream?: string, activated: boolean = true): void {
    let logField: LogFieldModel = this.getNewLogField(component, stream, activated);
    this.logsList.push(logField);
  }

  getNewLogField(component: string, stream?: string, activated: boolean = true): LogFieldModel {
    let logField: LogFieldModel = new LogFieldModel(component, stream);
    logField.activated = activated;
    return logField;
  }

  disableLogField(name: string, component: string, stream: string): void {
    this.addLogFieldToList(name, component, stream, false);
  }
}
