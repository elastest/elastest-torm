import { DefaultESFieldModel, components } from '../../defaultESData-model';
import { LogFieldModel } from './log-field-model';
import { ESRabLogModel } from './es-rab-log-model';


export class AllLogsTypesModel {
    logsList: LogFieldModel[];

    constructor() {
        this.logsList = [];
        for (let component of components) {
            this.createFieldsListByComponent(component);
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

    createFieldsListByComponent(component: string) {
        let logField: LogFieldModel = new LogFieldModel(component);
        logField.activated = true;
        this.logsList.push(logField);
    }

}