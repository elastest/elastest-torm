import { Injectable } from '@angular/core';
import { ExternalProjectModel } from './external-project/external-project-model'
import { ExternalTJobModel } from './external-tjob/external-tjob-model';
import { ExternalTestCaseModel } from './external-test-case/external-test-case-model';
import { ExternalTJobExecModel } from './external-tjob-execution/external-tjob-execution-model';
import { ETModelsTransformServices } from '../../shared/services/et-models-transform.service';
import { SutModel } from '../sut/sut-model';
import { ExternalTestExecutionModel } from './external-test-execution/external-test-execution-model';

@Injectable()
export class ETExternalModelsTransformService {

    constructor(
        private eTModelsTransformService: ETModelsTransformServices,
    ) { }

    /**************************/
    /******** Projects ********/
    /**************************/

    jsonToExternalProjectsList(projects: any[]): ExternalProjectModel[] {
        let projectsList: ExternalProjectModel[] = [];
        for (let project of projects) {
            projectsList.push(this.jsonToExternalProjectModel(project));
        }
        return projectsList;
    }


    jsonToExternalProjectModel(project: any, withoutTJobs: boolean = false, withoutSuts: boolean = false): ExternalProjectModel {
        let newProject: ExternalProjectModel;
        newProject = new ExternalProjectModel();
        newProject.id = project.id;
        newProject.name = project.name;
        newProject.type = project.type;
        newProject.externalId = project.externalId;
        newProject.externalSystemId = project.externalSystemId;
        if (!withoutSuts) {
            newProject.suts = this.eTModelsTransformService.jsonToSutsList(project.suts, true);
        }
        if (!withoutTJobs) {
            newProject.externalTJobs = this.jsonToExternalTJobsList(project.externalTJobs, true);
        }

        return newProject;
    }


    /*************************/
    /********* TJobs *********/
    /*************************/


    jsonToExternalTJobsList(tjobs: any[], fromProject: boolean = false): ExternalTJobModel[] {
        let tjobsList: ExternalTJobModel[] = [];
        for (let tjob of tjobs) {
            tjobsList.push(this.jsonToExternalTJobModel(tjob));
        }
        return tjobsList;
    }


    jsonToExternalTJobModel(tjob: any, fromProject: boolean = false): ExternalTJobModel {
        let newTJob: ExternalTJobModel;
        newTJob = new ExternalTJobModel();
        newTJob.id = tjob.id;
        newTJob.name = tjob.name;
        newTJob.externalId = tjob.externalId;
        newTJob.externalSystemId = tjob.externalSystemId;
        newTJob.externalTestCases = tjob.externalTestCases;
        newTJob.externalTJobExecs = tjob.externalTJobExecs;
        if (!fromProject) {
            newTJob.project = this.jsonToExternalProjectModel(tjob.project, true);
        } else {
            newTJob.project = tjob.project;
        }

        if (tjob.sut !== undefined && tjob.sut !== null) {
            newTJob.sut = this.eTModelsTransformService.jsonToSutModel(tjob.sut, true); // Hardcoded fromProject (like fromTJob)
        } else {
            newTJob.sut = new SutModel();
        }

        return newTJob;
    }

    /*****************************/
    /********* TJobExecs *********/
    /*****************************/


    jsonToExternalTJobExecsList(tjobexecs: any[]): ExternalTJobExecModel[] {
        let tjobexecsList: ExternalTJobExecModel[] = [];
        for (let tjobexec of tjobexecs) {
            tjobexecsList.push(this.jsonToExternalTJobExecModel(tjobexec));
        }
        return tjobexecsList;
    }


    jsonToExternalTJobExecModel(tjobexec: any): ExternalTJobExecModel {
        let newTJobExec: ExternalTJobExecModel;
        newTJobExec = new ExternalTJobExecModel();
        newTJobExec.id = tjobexec.id;
        newTJobExec.esIndex = tjobexec.esIndex;

        if (tjobexec.tJob !== undefined && tjobexec.tJob !== null) {
            newTJobExec.externalTJob = this.jsonToExternalTJobModel(tjobexec.externalTJob);
        } else {
            newTJobExec.externalTJob = new ExternalTJobModel();
        }

        return newTJobExec;
    }


    /*************************/
    /********* Cases *********/
    /*************************/

    jsonToExternalTestCasesList(cases: any[]): ExternalTestCaseModel[] {
        let casesList: ExternalTestCaseModel[] = [];
        for (let testCase of cases) {
            casesList.push(this.jsonToExternalTestCaseModel(testCase));
        }
        return casesList;
    }

    jsonToExternalTestCaseModel(testCase: any): ExternalTestCaseModel {
        let newCase: ExternalTestCaseModel;
        newCase = new ExternalTestCaseModel();
        newCase.id = testCase.id;
        newCase.name = testCase.name;
        newCase.externalId = testCase.externalId;
        newCase.externalSystemId = testCase.externalSystemId;

        newCase.fields = testCase.fields;
        newCase.externalTestExecs = testCase.externalTestExecs;
        if (testCase.tJob !== undefined && testCase.tJob !== null) {
            newCase.externalTJob = this.jsonToExternalTJobModel(testCase.externalTJob);
        } else {
            newCase.externalTJob = new ExternalTJobModel();
        }
        return newCase;
    }


    /*****************************/
    /********* TestExecs *********/
    /*****************************/

    jsonToExternalTestExecsList(testExecs: any[]): ExternalTestExecutionModel[] {
        let testExecsList: ExternalTestExecutionModel[] = [];
        for (let testExec of testExecs) {
            testExecsList.push(this.jsonToExternalTestExecutionModel(testExec));
        }
        return testExecsList;
    }


    jsonToExternalTestExecutionModel(testExec: any): ExternalTestExecutionModel {
        let newTestExec: ExternalTestExecutionModel;
        newTestExec = new ExternalTestExecutionModel();
        newTestExec.id = testExec.id;
        newTestExec.esIndex = testExec.esIndex;
        newTestExec.fields = testExec.fields;
        newTestExec.result = testExec.result;
        newTestExec.externalId = testExec.externalId;
        newTestExec.externalSystemId = testExec.externalSystemId;


        if (testExec.tJob !== undefined && testExec.tJob !== null) {
            newTestExec.exTestCase = this.jsonToExternalTestCaseModel(testExec.exTestCase);
        } else {
            newTestExec.exTestCase = new ExternalTestCaseModel();
        }

        return newTestExec;
    }
}