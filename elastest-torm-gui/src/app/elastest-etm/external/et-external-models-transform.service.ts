import { Injectable } from '@angular/core';
import { ExternalProjectModel } from './external-project/external-project-model';
import { ExternalTJobModel } from './external-tjob/external-tjob-model';
import { ExternalTestCaseModel } from './external-test-case/external-test-case-model';
import { ExternalTJobExecModel } from './external-tjob-execution/external-tjob-execution-model';
import { ETModelsTransformServices } from '../../shared/services/et-models-transform.service';
import { SutModel } from '../sut/sut-model';
import { ExternalTestExecutionModel } from './external-test-execution/external-test-execution-model';
import { DashboardConfigModel } from '../tjob/dashboard-config-model';
import { TssManifest, EsmServiceModel } from '../../elastest-esm/esm-service.model';

@Injectable()
export class ETExternalModelsTransformService {
  constructor(private eTModelsTransformService: ETModelsTransformServices) {}

  /**************************/
  /******** Projects ********/
  /**************************/

  jsonToExternalProjectsList(projects: any[]): ExternalProjectModel[] {
    let projectsList: ExternalProjectModel[] = [];
    if (projects && projects.length > 0) {
      for (let project of projects) {
        projectsList.push(this.jsonToExternalProjectModel(project));
      }
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
      newProject.exTJobs = this.jsonToExternalTJobsList(project.exTJobs, true);
    }

    return newProject;
  }

  /*************************/
  /********* TJobs *********/
  /*************************/

  jsonToExternalTJobsList(
    tjobs: any[],
    fromProject: boolean = false,
    withoutTestCases: boolean = false,
    withoutTJobExecs: boolean = false,
  ): ExternalTJobModel[] {
    let tjobsList: ExternalTJobModel[] = [];
    if (tjobs && tjobs.length > 0) {
      for (let tjob of tjobs) {
        tjobsList.push(this.jsonToExternalTJobModel(tjob, fromProject, withoutTestCases, withoutTJobExecs));
      }
    }
    return tjobsList;
  }

  jsonToExternalTJobModel(
    tjob: any,
    fromProject: boolean = false,
    withoutTestCases: boolean = false,
    withoutTJobExecs: boolean = false,
  ): ExternalTJobModel {
    let newTJob: ExternalTJobModel;
    newTJob = new ExternalTJobModel();
    newTJob.id = tjob.id;
    newTJob.name = tjob.name;
    newTJob.externalId = tjob.externalId;
    newTJob.externalSystemId = tjob.externalSystemId;
    if (!withoutTestCases) {
      newTJob.exTestCases = tjob.exTestCases;
    }
    if (!withoutTJobExecs) {
      newTJob.exTJobExecs = tjob.exTJobExecs;
    }
    if (!fromProject) {
      newTJob.exProject = this.jsonToExternalProjectModel(tjob.exProject, true);
    } else {
      newTJob.exProject = tjob.exProject;
    }

    if (tjob.sut !== undefined && tjob.sut !== null) {
      newTJob.sut = this.eTModelsTransformService.jsonToSutModel(tjob.sut, true); // Hardcoded fromProject (like fromTJob)
    } else {
      newTJob.sut = new SutModel();
    }
    newTJob.execDashboardConfig = tjob.execDashboardConfig;
    newTJob.execDashboardConfigModel = new DashboardConfigModel(tjob.execDashboardConfig, false, false, false);

    if (tjob.esmServicesString !== undefined && tjob.esmServicesString !== null && tjob.esmServicesString !== '') {
      newTJob.esmServicesString = tjob.esmServicesString;
      for (let service of JSON.parse(tjob.esmServicesString)) {
        let manifest: any = service.manifest;
        if (manifest === undefined || manifest === null) {
          manifest = { config: undefined };
        }
        service.manifest = manifest;
        let tssManifest: TssManifest = new TssManifest();

        if (service.manifest) {
          tssManifest.initFromJson(service.manifest);
        }

        newTJob.esmServices.push(new EsmServiceModel(service.id, service.name, service.selected, tssManifest));
        if (service.selected) {
          newTJob.esmServicesChecked++;
        }
      }
    }

    return newTJob;
  }

  /*****************************/
  /********* TJobExecs *********/
  /*****************************/

  jsonToExternalTJobExecsList(tjobexecs: any[], fromTJob: boolean = false): ExternalTJobExecModel[] {
    let tjobexecsList: ExternalTJobExecModel[] = [];
    if (tjobexecs && tjobexecs.length > 0) {
      for (let tjobexec of tjobexecs) {
        tjobexecsList.push(this.jsonToExternalTJobExecModel(tjobexec, fromTJob));
      }
    }
    return tjobexecsList;
  }

  jsonToExternalTJobExecModel(tjobexec: any, fromTJob: boolean = false): ExternalTJobExecModel {
    let newTJobExec: ExternalTJobExecModel;
    newTJobExec = new ExternalTJobExecModel();
    newTJobExec.id = tjobexec.id;
    newTJobExec.monitoringIndex = tjobexec.monitoringIndex;
    newTJobExec.envVars = tjobexec.envVars;
    newTJobExec.result = tjobexec.result;
    newTJobExec.resultMsg = tjobexec.resultMsg;

    if (tjobexec.exTJob !== undefined && tjobexec.exTJob !== null) {
      if (!fromTJob) {
        newTJobExec.exTJob = this.jsonToExternalTJobModel(tjobexec.exTJob, false, false, true);
      } else {
        newTJobExec.exTJob = tjobexec.exTJob;
      }
    } else {
      if (!fromTJob) {
        newTJobExec.exTJob = new ExternalTJobModel();
      } else {
        newTJobExec.exTJob = undefined;
      }
    }

    newTJobExec.exTestExecs = tjobexec.exTestExecs;

    if (tjobexec.startDate !== undefined && tjobexec.startDate !== null) {
      newTJobExec.startDate = new Date(tjobexec.startDate);
    }
    if (tjobexec.endDate !== undefined && tjobexec.endDate !== null) {
      newTJobExec.endDate = new Date(tjobexec.endDate);
    }

    newTJobExec.monitoringStorageType = tjobexec.monitoringStorageType;

    newTJobExec.lastExecutedTCaseId = tjobexec.lastExecutedTCaseId;
    newTJobExec.executionConfig = tjobexec.executionConfig;
    newTJobExec.executionConfigObj = newTJobExec.executionConfig ? JSON.parse(newTJobExec.executionConfig) : {};

    return newTJobExec;
  }

  /*************************/
  /********* Cases *********/
  /*************************/

  jsonToExternalTestCasesList(cases: any[], fromTJob: boolean = false): ExternalTestCaseModel[] {
    let casesList: ExternalTestCaseModel[] = [];
    if (cases && cases.length > 0) {
      for (let testCase of cases) {
        casesList.push(this.jsonToExternalTestCaseModel(testCase, fromTJob));
      }
    }
    return casesList;
  }

  jsonToExternalTestCaseModel(
    testCase: any,
    fromTJob: boolean = false,
    withoutTestExecs: boolean = false,
  ): ExternalTestCaseModel {
    let newCase: ExternalTestCaseModel;
    newCase = new ExternalTestCaseModel();
    newCase.id = testCase.id;
    newCase.name = testCase.name;
    newCase.externalId = testCase.externalId;
    newCase.externalSystemId = testCase.externalSystemId;

    newCase.fields = testCase.fields;
    if (!withoutTestExecs) {
      newCase.exTestExecs = testCase.exTestExecs;
    }
    if (testCase.exTJobs !== undefined && testCase.exTJobs !== null) {
      if (!fromTJob) {
        newCase.exTJobs = this.jsonToExternalTJobsList(testCase.exTJobs, false, true, false);
      } else {
        newCase.exTJobs = testCase.exTJobs;
      }
    } else {
      if (!fromTJob) {
        newCase.exTJobs = [];
      } else {
        newCase.exTJobs = [];
      }
    }

    return newCase;
  }

  /*****************************/
  /********* TestExecs *********/
  /*****************************/

  jsonToExternalTestExecsList(testExecs: any[], fromTestCase: boolean = false): ExternalTestExecutionModel[] {
    let testExecsList: ExternalTestExecutionModel[] = [];
    if (testExecs && testExecs.length > 0) {
      for (let testExec of testExecs) {
        testExecsList.push(this.jsonToExternalTestExecutionModel(testExec, fromTestCase));
      }
    }
    return testExecsList;
  }

  jsonToExternalTestExecutionModel(testExec: any, fromTestCase: boolean = false): ExternalTestExecutionModel {
    let newTestExec: ExternalTestExecutionModel;
    newTestExec = new ExternalTestExecutionModel();
    newTestExec.id = testExec.id;
    newTestExec.monitoringIndex = testExec.monitoringIndex;
    newTestExec.fields = testExec.fields;
    newTestExec.result = testExec.result;
    newTestExec.externalId = testExec.externalId;
    newTestExec.externalSystemId = testExec.externalSystemId;

    if (testExec.exTestCase !== undefined && testExec.exTestCase !== null) {
      if (!fromTestCase) {
        newTestExec.exTestCase = this.jsonToExternalTestCaseModel(testExec.exTestCase, false, true);
      } else {
        newTestExec.exTestCase = testExec.exTestCase;
      }
    } else {
      if (!fromTestCase) {
        newTestExec.exTestCase = new ExternalTestCaseModel();
      } else {
        newTestExec.exTestCase = undefined;
      }
    }

    newTestExec.exTJobExec = testExec.exTJobExec;

    if (testExec.startDate !== undefined && testExec.startDate !== null) {
      newTestExec.startDate = new Date(testExec.startDate);
    }
    if (testExec.endDate !== undefined && testExec.endDate !== null) {
      newTestExec.endDate = new Date(testExec.endDate);
    }

    return newTestExec;
  }
}
