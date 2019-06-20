import { EimConfigModel } from '../../elastest-etm/sut/eim-config-model';
import { EsmServiceModel, TssManifest } from '../../elastest-esm/esm-service.model';
import { DashboardConfigModel } from '../../elastest-etm/tjob/dashboard-config-model';
import { ProjectModel } from '../../elastest-etm/project/project-model';
import { SutExecModel } from '../../elastest-etm/sut-exec/sutExec-model';
import { SutModel } from '../../elastest-etm/sut/sut-model';
import { TJobExecModel } from '../../elastest-etm/tjob-exec/tjobExec-model';
import { TJobModel } from '../../elastest-etm/tjob/tjob-model';
import { Injectable } from '@angular/core';
import { LogAnalyzerConfigModel } from '../../elastest-log-analyzer/log-analyzer-config-model';
import { ExternalProjectModel } from '../../elastest-etm/external/external-project/external-project-model';
import { TestSuiteModel } from '../../elastest-etm/test-suite/test-suite-model';
import { TestCaseModel } from '../../elastest-etm/test-case/test-case-model';
import { EimMonitoringConfigModel, EimBeatConfigModel } from '../../elastest-etm/sut/eim-monitoring-config.model';
import { ExternalElasticsearch } from '../../elastest-etm/external-monitoring-db/external-elasticsearch.model';
import { ExternalPrometheus } from '../../elastest-etm/external-monitoring-db/external-prometheus.model';

import { ParameterModel } from '../../elastest-etm/parameter/parameter-model';
import { MultiConfigModel } from '../multi-config-view/multi-config-view.component';
import { ExternalMonitoringDBForLogs } from '../../elastest-etm/external-monitoring-db/external-monitoring-db-for-logs.model';
import { ExternalMonitoringDBForMetrics } from '../../elastest-etm/external-monitoring-db/external-monitoring-db-for-metrics.model';
@Injectable()
export class ETModelsTransformServices {
  constructor() {}

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
    if (project) {
      newProject = new ProjectModel();
      newProject.id = project.id;
      newProject.name = project.name;
      if (!withoutSuts) {
        newProject.suts = this.jsonToSutsList(project.suts, true);
      }
      if (!withoutTJobs) {
        newProject.tjobs = this.jsonToTJobsList(project.tjobs, true);
      }
    }
    return newProject;
  }

  /***** TJob *****/

  jsonToTJobsList(tjobs: any[], fromProject: boolean = false): TJobModel[] {
    let tJobsList: TJobModel[] = [];
    if (tjobs) {
      for (let tjob of tjobs) {
        tJobsList.push(this.jsonToTJobModel(tjob, fromProject));
      }
    }
    return tJobsList;
  }

  /**
   * @param tjob
   * @param fromProject Cyclic references
   */
  jsonToTJobModel(tjob: any, fromProject: boolean = false): TJobModel {
    let newTJob: TJobModel;
    if (tjob) {
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
      if (tjob.esmServicesString !== undefined && tjob.esmServicesString !== null && tjob.esmServicesString !== '') {
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
      newTJob.external = tjob.external;
      newTJob.externalUrls = tjob.externalUrls;
      newTJob.multi = tjob.multi ? tjob.multi : false;

      newTJob.multiConfigurations = this.jsonToMultiConfigsList(tjob.multiConfigurations);
      newTJob.maxExecutions = tjob.maxExecutions !== undefined && tjob.maxExecutions !== null ? tjob.maxExecutions : 0;
    }
    return newTJob;
  }

  /***** TJobExec *****/
  public jsonToTJobExecsList(tjobExecs: any[], withoutTestSuites: boolean = false): TJobExecModel[] {
    let tJobExecsList: TJobExecModel[] = [];
    if (tjobExecs) {
      for (let tjobExec of tjobExecs) {
        tJobExecsList.push(this.jsonToTJobExecModel(tjobExec, withoutTestSuites));
      }
    }
    return tJobExecsList;
  }

  public jsonToTJobExecModel(tjobExec: any, withoutTestSuites: boolean = false): TJobExecModel {
    let newTJobExec: TJobExecModel;
    if (tjobExec) {
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
      newTJobExec.monitoringIndex = tjobExec.monitoringIndex;

      if (tjobExec.tJob !== undefined && tjobExec.tJob !== null) {
        newTJobExec.tJob = this.jsonToTJobModel(tjobExec.tJob);
      } else {
        newTJobExec.tJob = new TJobModel();
      }

      if (!withoutTestSuites) {
        newTJobExec.testSuites = this.jsonToTestSuitesList(tjobExec.testSuites, true);
      }

      newTJobExec.parameters = tjobExec.parameters;
      newTJobExec.resultMsg = tjobExec.resultMsg;
      newTJobExec.startDate = new Date(tjobExec.startDate);
      if (tjobExec.endDate !== undefined && tjobExec.endDate !== null) {
        newTJobExec.endDate = new Date(tjobExec.endDate);
      }

      if (newTJobExec.startDate && newTJobExec.endDate) {
        try {
          newTJobExec.duration = (newTJobExec.endDate.getTime() - newTJobExec.startDate.getTime()) / 1000; // secs
        } catch (e) {}
      }

      newTJobExec.externalUrls = tjobExec.externalUrls;
      newTJobExec.monitoringStorageType = tjobExec.monitoringStorageType;

      newTJobExec.type = tjobExec.type ? tjobExec.type : 'SIMPLE';
      newTJobExec.execParent = tjobExec.execParent ? this.jsonToTJobExecModel(tjobExec.execParent) : undefined;
      newTJobExec.execChilds = tjobExec.execChilds ? this.jsonToTJobExecsList(tjobExec.execChilds) : [];
      newTJobExec.multiConfigurations = this.jsonToMultiConfigsList(tjobExec.multiConfigurations);
    }
    return newTJobExec;
  }

  /***** TestSuite *****/
  public jsonToTestSuitesList(testSuites: any[], fromTJobExec: boolean = false): TestSuiteModel[] {
    let tJobExecsList: TestSuiteModel[] = [];
    if (testSuites) {
      for (let testSuite of testSuites) {
        tJobExecsList.push(this.jsonToTestSuiteModel(testSuite, false, fromTJobExec));
      }
    }
    return tJobExecsList;
  }

  public jsonToTestSuiteModel(testSuite: any, withoutTestCases: boolean = false, fromTJobExec: boolean = false): TestSuiteModel {
    let newTestSuite: TestSuiteModel;
    if (testSuite) {
      newTestSuite = new TestSuiteModel();

      newTestSuite.id = testSuite.id;
      newTestSuite.name = testSuite.name;
      newTestSuite.timeElapsed = testSuite.timeElapsed;
      newTestSuite.errors = testSuite.errors;
      newTestSuite.failures = testSuite.failures;
      newTestSuite.skipped = testSuite.skipped;
      newTestSuite.flakes = testSuite.flakes;
      newTestSuite.numTests = testSuite.numTests;

      if (!withoutTestCases) {
        newTestSuite.testCases = this.jsonToTestCasesList(testSuite.testCases, true);
      }

      if (testSuite.tJobExec !== undefined && testSuite.tJobExec !== null) {
        if (!fromTJobExec) {
          newTestSuite.tJobExec = this.jsonToTJobExecModel(testSuite.tJobExec, true);
        } else {
          newTestSuite.tJobExec = testSuite.tJobExec;
        }
      } else {
        if (!fromTJobExec) {
          newTestSuite.tJobExec = new TJobExecModel();
        } else {
          newTestSuite.tJobExec = undefined;
        }
      }
    }
    return newTestSuite;
  }

  /***** TestCase *****/
  public jsonToTestCasesList(testCases: any[], fromTestSuite: boolean = false): TestCaseModel[] {
    let testCasesList: TestCaseModel[] = [];
    if (testCases) {
      for (let testCase of testCases) {
        testCasesList.push(this.jsonToTestCaseModel(testCase, fromTestSuite));
      }
    }
    return testCasesList;
  }

  public jsonToTestCaseModel(testCase: any, fromTestSuite: boolean = false): TestCaseModel {
    let newTestCase: TestCaseModel;
    if (testCase) {
      newTestCase = new TestCaseModel();

      newTestCase.id = testCase.id;
      newTestCase.name = testCase.name;
      newTestCase.time = testCase.time;
      newTestCase.failureMessage = testCase.failureMessage;
      newTestCase.failureType = testCase.failureType;
      newTestCase.failureErrorLine = testCase.failureErrorLine;
      newTestCase.failureDetail = testCase.failureDetail;

      if (!fromTestSuite) {
        newTestCase.testSuite = this.jsonToTestSuiteModel(testCase.testSuite, true);
      } else {
        newTestCase.testSuite = testCase.testSuite;
      }
    }

    return newTestCase;
  }

  /***** Sut *****/
  jsonToSutsList(suts: any[], fromProject: boolean = false): SutModel[] {
    let sutsList: SutModel[] = [];
    if (suts) {
      for (let sut of suts) {
        sutsList.push(this.jsonToSutModel(sut, fromProject));
      }
    }
    return sutsList;
  }

  jsonToSutModel(sut: any, fromProject: boolean = false): SutModel {
    let newSut: SutModel;

    if (sut) {
      newSut = new SutModel();
      newSut.id = sut.id;
      newSut.name = sut.name;
      newSut.specification = sut.specification;
      newSut.sutType = sut.sutType;
      newSut.description = sut.description;
      if (!fromProject && sut.project) {
        newSut.project = this.jsonToProjectModel(sut.project, true, true);
      } else {
        newSut.project = sut.project;
      }
      newSut.eimConfig = new EimConfigModel(sut.eimConfig);
      newSut.instrumentalize = sut.instrumentalize;
      if (sut.instrumentalize === undefined || sut.instrumentalize === null) {
        sut.instrumentalize = false;
      }

      newSut.instrumentalized = sut.instrumentalized;
      if (sut.instrumentalized === undefined || sut.instrumentalized === null) {
        sut.instrumentalized = false;
      }

      newSut.currentSutExec = sut.currentSutExec;
      newSut.instrumentedBy = sut.instrumentedBy;

      newSut.externalMonitoringDBForLogs = new ExternalMonitoringDBForLogs(sut.externalMonitoringDBForLogs);
      newSut.externalMonitoringDBForMetrics = new ExternalMonitoringDBForMetrics(sut.externalMonitoringDBForMetrics);

      newSut.protocol = sut.protocol !== undefined && sut.protocol !== null ? sut.protocol : 'http';
      newSut.port = sut.port;
      newSut.path = sut.path;
      newSut.mainService = sut.mainService;
      newSut.parameters = sut.parameters;
      newSut.commands = sut.commands;
      // TMP if commands set managed with commands
      newSut.managedDockerType = newSut.withCommands() ? 'COMMANDS' : sut.managedDockerType;

      if (!fromProject && sut.exProject) {
        newSut.exProject = new ExternalProjectModel();
        newSut.exProject.initFromJson(sut.exProject);
      } else {
        newSut.exProject = sut.exProject;
      }

      newSut.externalMonitoringDBForLogs = new ExternalMonitoringDBForLogs(sut.externalMonitoringDBForLogs);
      newSut.externalMonitoringDBForMetrics = new ExternalMonitoringDBForMetrics(sut.externalMonitoringDBForMetrics);

      newSut.commandsOption = sut.commandsOption;

      newSut.eimMonitoringConfig = this.jsonToEimMonitoringConfigModel(sut.eimMonitoringConfig);
    }
    return newSut;
  }

  /***** SutExec *****/
  jsonToSutExecsList(sutExecs: any[]): SutExecModel[] {
    let sutExecsList: SutExecModel[] = [];
    if (sutExecs) {
      for (let sutExec of sutExecs) {
        sutExecsList.push(this.jsonToSutExecModel(sutExec));
      }
    }
    return sutExecsList;
  }

  jsonToSutExecModel(sutExec: any): SutExecModel {
    let newSutExec: SutExecModel;
    if (sutExec) {
      newSutExec = new SutExecModel();
      newSutExec.id = sutExec.id;
      newSutExec.deplotStatus = sutExec.deplotStatus;
      newSutExec.url = sutExec.url;
      newSutExec.sut = sutExec.sut;
      newSutExec.parameters = sutExec.parameters;
    }

    return newSutExec;
  }

  /**** EimMonitoringConfig ****/

  jsonToEimMonitoringConfigModel(eimMonitoringConfig: any): EimMonitoringConfigModel {
    let newEimMonitoringConfigModel: EimMonitoringConfigModel = new EimMonitoringConfigModel();
    if (eimMonitoringConfig !== undefined && eimMonitoringConfig !== null) {
      newEimMonitoringConfigModel.id = eimMonitoringConfig.id;
      newEimMonitoringConfigModel.exec = eimMonitoringConfig.exec;
      newEimMonitoringConfigModel.component = eimMonitoringConfig.component;
      newEimMonitoringConfigModel.dockerized = eimMonitoringConfig.dockerized;

      if (eimMonitoringConfig.beats !== undefined) {
        for (let key of Object.keys(eimMonitoringConfig.beats)) {
          let currentBeat: EimBeatConfigModel = eimMonitoringConfig.beats[key];
          if (currentBeat !== undefined && newEimMonitoringConfigModel.beats[key] !== undefined) {
            newEimMonitoringConfigModel.beats[key].id = currentBeat.id;
            newEimMonitoringConfigModel.beats[key].name = currentBeat.name;
            newEimMonitoringConfigModel.beats[key].paths = currentBeat.paths;
            newEimMonitoringConfigModel.beats[key].dockerized = currentBeat.dockerized;
            newEimMonitoringConfigModel.beats[key].stream = currentBeat.stream;
          }
        }
      }
    }

    return newEimMonitoringConfigModel;
  }

  /**** LogAnalyzerConfig ****/

  jsonToLogAnalyzerConfigModel(logAnalyzerConfig: any): LogAnalyzerConfigModel {
    let logAnalyzerConfigModel: LogAnalyzerConfigModel = new LogAnalyzerConfigModel();
    if (logAnalyzerConfig !== undefined && logAnalyzerConfig !== null) {
      logAnalyzerConfigModel.id = logAnalyzerConfig.id;
      logAnalyzerConfigModel.columnsConfig = logAnalyzerConfig.columnsConfig;
      logAnalyzerConfigModel.columnsState = JSON.parse(logAnalyzerConfigModel.columnsConfig);
    }

    return logAnalyzerConfigModel;
  }

  /* *** Parameters *** */

  jsonToParametersList(parameters: any[]): ParameterModel[] {
    let parametersList: ParameterModel[] = [];

    for (let parameter of parameters) {
      parametersList.push(this.jsonToParameterModel(parameter));
    }
    return parametersList;
  }

  jsonToParameterModel(param: any): ParameterModel {
    return new ParameterModel(param);
  }

  /* *** MultiConfig *** */

  jsonToMultiConfigsList(multiConfigs: any[]): MultiConfigModel[] {
    let multiConfigsList: MultiConfigModel[] = [];

    if (multiConfigs !== undefined && multiConfigs !== null) {
      for (let multiConfig of multiConfigs) {
        multiConfigsList.push(this.jsonToMultiConfigModel(multiConfig));
      }
    }
    return multiConfigsList;
  }

  jsonToMultiConfigModel(multiConfig: any): MultiConfigModel {
    return new MultiConfigModel(multiConfig);
  }
}
