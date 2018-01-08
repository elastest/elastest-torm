import { EimConfigModel } from '../../elastest-etm/sut/eim-config-model';
import { EsmServiceModel } from '../../elastest-esm/esm-service.model';
import { DashboardConfigModel } from '../../elastest-etm/tjob/dashboard-config-model';
import { ProjectModel } from '../../elastest-etm/project/project-model';
import { SutExecModel } from '../../elastest-etm/sut-exec/sutExec-model';
import { SutModel } from '../../elastest-etm/sut/sut-model';
import { TJobExecModel } from '../../elastest-etm/tjob-exec/tjobExec-model';
import { TJobModel } from '../../elastest-etm/tjob/tjob-model';
import { Injectable } from '@angular/core';
import { LogAnalyzerConfigModel } from '../../elastest-log-analyzer/log-analyzer-config-model';
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
     * Cyclic references:
     * @param withoutTJobs
     * * @param withoutSuts
     */
    jsonToProjectModel(project: any, withoutTJobs: boolean = false, withoutSuts: boolean = false): ProjectModel {
        let newProject: ProjectModel;
        newProject = new ProjectModel();
        newProject.id = project.id;
        newProject.name = project.name;
        if (!withoutSuts) {
            newProject.suts = this.jsonToSutsList(project.suts, true);
        }
        if (!withoutTJobs) {
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
    jsonToTJobModel(tjob: any, fromProject: boolean = false): TJobModel {
        let newTJob: TJobModel;

        newTJob = new TJobModel();
        newTJob.id = tjob.id;
        newTJob.name = tjob.name;
        newTJob.imageName = tjob.imageName;
        if (tjob.sut !== undefined && tjob.sut !== null) {
            newTJob.sut = this.jsonToSutModel(tjob.sut, true); // Hardcoded fromProject (like fromTJob)
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
        newTJobExec.startDate = new Date(tjobExec.startDate);
        if (tjobExec.endDate !== undefined && tjobExec.endDate !== null) {
            newTJobExec.endDate = new Date(tjobExec.endDate);
        }

        return newTJobExec;
    }

    /***** Sut *****/
    jsonToSutsList(suts: any[], fromProject: boolean = false): SutModel[] {
        let sutsList: SutModel[] = [];
        for (let sut of suts) {
            sutsList.push(this.jsonToSutModel(sut, fromProject));
        }
        return sutsList;
    }

    jsonToSutModel(sut: any, fromProject: boolean = false): SutModel {
        let newSut: SutModel;

        newSut = new SutModel();
        newSut.id = sut.id;
        newSut.name = sut.name;
        newSut.specification = sut.specification;
        newSut.sutType = sut.sutType;
        newSut.description = sut.description;
        if (!fromProject) {
            newSut.project = this.jsonToProjectModel(sut.project, true, true);
        } else {
            newSut.project = sut.project;
        }
        newSut.eimConfig = new EimConfigModel(sut.eimConfig);
        newSut.instrumentalize = sut.instrumentalize;
        if (sut.instrumentalize === undefined || sut.instrumentalize === null) {
            sut.instrumentalize = false;
        }
        newSut.currentSutExec = sut.currentSutExec;
        newSut.instrumentedBy = sut.instrumentedBy;
        newSut.port = sut.port;
        newSut.managedDockerType = sut.managedDockerType;
        newSut.mainService = sut.mainService;
        newSut.parameters = sut.parameters;
        newSut.commands = sut.commands;

        return newSut;
    }

    /***** SutExec *****/
    jsonToSutExecsList(sutExecs: any[]): SutExecModel[] {
        let sutExecsList: SutExecModel[] = [];
        for (let sutExec of sutExecs) {
            sutExecsList.push(this.jsonToSutExecModel(sutExec));
        }
        return sutExecsList;
    }

    jsonToSutExecModel(sutExec: any): SutExecModel {
        let newSutExec: SutExecModel;

        newSutExec = new SutExecModel();
        newSutExec.id = sutExec.id;
        newSutExec.deplotStatus = sutExec.deplotStatus;
        newSutExec.url = sutExec.url;
        newSutExec.sut = sutExec.sut;
        newSutExec.parameters = sutExec.parameters;

        return newSutExec;
    }

    /**** LogAnalyzerConfig ****/

    jsonToLogAnalyzerConfigModel(logAnalyzerConfig: any): LogAnalyzerConfigModel {
        let logAnalyzerConfigModel: LogAnalyzerConfigModel = new LogAnalyzerConfigModel();
        if (logAnalyzerConfig !== undefined && logAnalyzerConfig !== null) {
            logAnalyzerConfigModel.id = logAnalyzerConfig.id;
            logAnalyzerConfigModel.columnsConfig = logAnalyzerConfig.columnsConfig;
            logAnalyzerConfigModel.columnsState = JSON.parse(logAnalyzerConfigModel.columnsConfig)
        }

        return logAnalyzerConfigModel;
    }
}