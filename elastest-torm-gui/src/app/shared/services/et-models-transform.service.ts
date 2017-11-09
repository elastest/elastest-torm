import { EimConfigModel } from '../../elastest-etm/sut/eim-config-model';
import { EsmServiceModel } from '../../elastest-esm/esm-service.model';
import { DashboardConfigModel } from '../../elastest-etm/tjob/dashboard-config-model';
import { ProjectModel } from '../../elastest-etm/project/project-model';
import { SutExecModel } from '../../elastest-etm/sut-exec/sutExec-model';
import { SutModel } from '../../elastest-etm/sut/sut-model';
import { TJobExecModel } from '../../elastest-etm/tjob-exec/tjobExec-model';
import { TJobModel } from '../../elastest-etm/tjob/tjob-model';
import { Injectable } from '@angular/core';
@Injectable()
export class ETModelsTransformServices {

    constructor() { }

    /***** Project *****/
    jsonToProjectsList(projects: any[]): ProjectModel[] {
        let projectsList: ProjectModel[] = [];
        for (let project of projects) {
            projectsList.push(this.jsonToProjectModel(project));
        }
        return projectsList;
    }

    /**
     * @param project 
     * @param onlyProject : not load TJobs. Cyclic references
     */
    jsonToProjectModel(project: any, onlyProject: boolean = false): ProjectModel {
        let newProject: ProjectModel;
        newProject = new ProjectModel();
        newProject.id = project.id;
        newProject.name = project.name;
        newProject.suts = this.jsonToSutsList(project.suts);
        if (!onlyProject) {
            newProject.tjobs = this.jsonToTJobsList(project.tjobs, true);
        }
        return newProject;
    }

    /***** TJob *****/

    jsonToTJobsList(tjobs: any[], fromProject: boolean = false): TJobModel[] {
        let tJobsList: TJobModel[] = [];
        for (let tjob of tjobs) {
            tJobsList.push(this.jsonToTJobModel(tjob, fromProject));
        }
        return tJobsList;
    }

    /**
     * @param tjob 
     * @param fromProject Cyclic references
     */
    jsonToTJobModel(tjob: any, fromProject: boolean = false, fromExec: boolean = false): TJobModel {
        let newTJob: TJobModel;

        newTJob = new TJobModel();
        newTJob.id = tjob.id;
        newTJob.name = tjob.name;
        newTJob.imageName = tjob.imageName;
        if (tjob.sut !== undefined && tjob.sut !== null) {
            newTJob.sut = this.jsonToSutModel(tjob.sut);
        } else {
            newTJob.sut = new SutModel();
        }

        if (!fromProject) {
            newTJob.project = this.jsonToProjectModel(tjob.project, true);
        } else {
            newTJob.project = tjob.project;
        }

        newTJob.tjobExecs = tjob.tjobExecs;
        newTJob.parameters = tjob.parameters;
        newTJob.commands = tjob.commands;
        newTJob.resultsPath = tjob.resultsPath;
        newTJob.execDashboardConfig = tjob.execDashboardConfig;
        newTJob.execDashboardConfigModel = new DashboardConfigModel(tjob.execDashboardConfig);
        if (tjob.esmServicesString !== undefined && tjob.esmServicesString !== null) {
            for (let service of JSON.parse(tjob.esmServicesString)) {
                newTJob.esmServices.push(new EsmServiceModel(service.id, service.name,
                    service.selected));
                if (service.selected) {
                    newTJob.esmServicesChecked++;
                }
            }
        }

        return newTJob;
    }


    /***** TJobExec *****/
    public jsonToTJobExecsList(tjobExecs: any[]): TJobExecModel[] {
        let tJobExecsList: TJobExecModel[] = [];
        for (let tjobExec of tjobExecs) {
            tJobExecsList.push(this.jsonToTJobExecModel(tjobExec));
        }
        return tJobExecsList;
    }


    public jsonToTJobExecModel(tjobExec: any): TJobExecModel {
        let newTJobExec: TJobExecModel;
        newTJobExec = new TJobExecModel();

        newTJobExec.id = tjobExec.id;
        newTJobExec.duration = tjobExec.duration;
        newTJobExec.error = tjobExec.error;
        newTJobExec.result = tjobExec.result;
        if (tjobExec.sutExecution !== undefined && tjobExec.sutExecution !== null) {
            newTJobExec.sutExec = this.jsonToSutExecModel(tjobExec.sutExecution);
        } else {
            newTJobExec.sutExec = new SutExecModel();
        }
        newTJobExec.logIndex = tjobExec.logIndex;

        if (tjobExec.tJob !== undefined && tjobExec.tJob !== null) {
            newTJobExec.tJob = this.jsonToTJobModel(tjobExec.tJob);
        } else {
            newTJobExec.tJob = new TJobModel();
        }
        newTJobExec.testSuite = tjobExec.testSuite;
        newTJobExec.parameters = tjobExec.parameters;
        newTJobExec.resultMsg = tjobExec.resultMsg;

        return newTJobExec;
    }


    /***** Sut *****/
    jsonToSutsList(suts: any[]): SutModel[] {
        let sutsList: SutModel[] = [];
        for (let sut of suts) {
            sutsList.push(this.jsonToSutModel(sut));
        }
        return sutsList;
    }

    jsonToSutModel(sut: any): SutModel {
        let newSut: SutModel;

        newSut = new SutModel();
        newSut.id = sut.id;
        newSut.name = sut.name;
        newSut.specification = sut.specification;
        newSut.sutType = sut.sutType;
        newSut.description = sut.description;
        newSut.project = sut.project;
        newSut.eimConfig = new EimConfigModel(sut.eimConfig);
        newSut.instrumentalize = sut.instrumentalize;
        if (sut.instrumentalize === undefined || sut.instrumentalize === null) {
            sut.instrumentalize = false;
        }
        newSut.currentSutExec = sut.currentSutExec;
        newSut.instrumentedBy = sut.instrumentedBy;
        newSut.port = sut.port;

        return newSut;
    }

    /***** SutExec *****/
    jsonToSutExecsList(sutExecs: any[]) {
        let sutExecsList: SutExecModel[] = [];
        for (let sutExec of sutExecs) {
            sutExecsList.push(this.jsonToSutExecModel(sutExec));
        }
        return sutExecsList;
    }


    jsonToSutExecModel(sutExec: any) {
        let newSutExec: SutExecModel;

        newSutExec = new SutExecModel();
        newSutExec.id = sutExec.id;
        newSutExec.deplotStatus = sutExec.deplotStatus;
        newSutExec.url = sutExec.url;
        newSutExec.sut = sutExec.sut;

        return newSutExec;
    }

}