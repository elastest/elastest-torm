import { ExternalProjectModel } from '../external-project/external-project-model';
import { ExternalTJobExecModel } from '../external-tjob-execution/external-tjob-execution-model';
import { ExternalTestCaseModel } from '../external-test-case/external-test-case-model';
import { SutModel } from '../../sut/sut-model';

export class ExternalTJobModel {
    id: number;
    name: string;
    externalId: string;
    externalSystemId: string;

    project: ExternalProjectModel;
    externalTJobExecs: ExternalTJobExecModel[];
    externalTestCases: ExternalTestCaseModel[];
    sut: SutModel;

    constructor() {
        this.id = 0;
        this.name = '';

        this.project = undefined;
        this.externalTJobExecs = [];
        this.externalTestCases = [];
        this.sut = undefined;
    }
}