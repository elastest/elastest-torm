import { LogFieldModel } from './log-field-model';
import { ESRabLogModel } from './es-rab-log-model';

export let componentTypes: string[] = [
    'test',
    'sut',
];

export let infoIds: string[] = [
    'default_log',
    'beats_metrics',
];

export class AllLogsTypesModel {
    logsList: LogFieldModel[];

    constructor() {
        this.logsList = [];
        for (let componentType of componentTypes) {
            this.createFieldsListByComponent(componentType);
        }
    }

    getPositionByName(name: string) {
        let position: number;
        let counter: number = 0;
        for (let log of this.logsList) {
            if (log.name === name) {
                position = counter;
                break;
            }
            counter++;
        }
        return position;
    }

    createFieldsListByComponent(componentType: string) {
        let logField: LogFieldModel = new LogFieldModel(componentType);
        logField.activated = true;
        this.logsList.push(logField);
    }

}