import { CustomFieldModel, TLTestCaseModel, PlatformModel } from '../../etm-testlink/models/test-case-model';
import { Injectable } from '@angular/core';
import { TestProjectModel } from '../../etm-testlink/models/test-project-model';
import { TLTestSuiteModel } from '../../etm-testlink/models/test-suite-model';
import { TestPlanModel } from '../../etm-testlink/models/test-plan-model';
import { BuildModel } from '../../etm-testlink/models/build-model';
import { TestCaseStepModel } from '../../etm-testlink/models/test-case-step-model';
import { TestCaseExecutionModel } from '../../etm-testlink/models/test-case-execution-model';
@Injectable()
export class ETTestlinkModelsTransformService {
  constructor() {}

  /**************************/
  /******** Projects ********/
  /**************************/

  jsonToTestProjectsList(projects: any[]): TestProjectModel[] {
    let projectsList: TestProjectModel[] = [];
    for (let project of projects) {
      projectsList.push(this.jsonToTestProjectModel(project));
    }
    return projectsList;
  }

  jsonToTestProjectModel(project: any): TestProjectModel {
    let newProject: TestProjectModel;
    newProject = new TestProjectModel();
    newProject.id = project.id;
    newProject.name = project.name;
    newProject.prefix = project.prefix;
    newProject.notes = project.notes;
    newProject.enableRequirements = project.enableRequirements;
    newProject.enableTestPriority = project.enableTestPriority;
    newProject.enableAutomation = project.enableAutomation;
    newProject.enableInventory = project.enableInventory;
    newProject.active = project.active;
    newProject.public = project.public;

    return newProject;
  }

  /**************************/
  /********* Suites *********/
  /**************************/

  jsonToTestSuiteList(suites: any[]): TLTestSuiteModel[] {
    let suitesList: TLTestSuiteModel[] = [];
    for (let suite of suites) {
      suitesList.push(this.jsonToTestSuiteModel(suite));
    }
    return suitesList;
  }

  jsonToTestSuiteModel(suite: any): TLTestSuiteModel {
    let newSuite: TLTestSuiteModel;
    newSuite = new TLTestSuiteModel();
    newSuite.id = suite.id;
    newSuite.testProjectId = suite.testProjectId;
    newSuite.name = suite.name;
    newSuite.details = suite.details;
    newSuite.parentId = suite.parentId;
    newSuite.order = suite.order;
    newSuite.checkDuplicatedName = suite.checkDuplicatedName;

    newSuite.actionOnDuplicatedName = suite.actionOnDuplicatedName;

    return newSuite;
  }

  /*************************/
  /********* Cases *********/
  /*************************/

  jsonToTestCasesList(cases: any[]): TLTestCaseModel[] {
    let casesList: TLTestCaseModel[] = [];
    for (let testCase of cases) {
      casesList.push(this.jsonToTestCaseModel(testCase));
    }
    return casesList;
  }

  jsonToTestCaseModel(testCase: any): TLTestCaseModel {
    let newCase: TLTestCaseModel;
    if (testCase !== undefined && testCase !== null) {
      newCase = new TLTestCaseModel();
      newCase.id = testCase.id;
      newCase.testProjectId = testCase.testProjectId;
      newCase.name = testCase.name;
      newCase.testSuiteId = testCase.testSuiteId;
      newCase.authorLogin = testCase.authorLogin;
      newCase.summary = testCase.summary;
      newCase.steps = this.jsonToTestStepsList(testCase.steps);
      newCase.preconditions = testCase.preconditions;

      newCase.testImportance = testCase.testImportance;

      newCase.executionType = testCase.executionType;

      newCase.executionOrder = testCase.executionOrder;
      newCase.order = testCase.order;
      newCase.internalId = testCase.internalId;
      newCase.fullExternalId = testCase.fullExternalId;
      newCase.checkDuplicatedName = testCase.checkDuplicatedName;

      newCase.actionOnDuplicatedName = testCase.actionOnDuplicatedName;

      newCase.versionId = testCase.versionId;
      newCase.version = testCase.version;
      newCase.parentId = testCase.parentId;

      newCase.customFields = [];
      if (testCase.customFields !== null && testCase.customField !== undefined) {
        for (let customField of testCase.customFields) {
          newCase.customFields.push(this.jsonToCustomField(customField));
        }
      }

      newCase.executionStatus = testCase.executionStatus;

      newCase.platform = this.jsonToPlatform(testCase.platform);
      newCase.featureId = testCase.featureId;
      newCase.testCaseStatus = testCase.testCaseStatus;
    }
    return newCase;
  }

  jsonToCustomField(customField: any): CustomFieldModel {
    let newCustomField: CustomFieldModel;
    if (customField !== undefined && customField !== null) {
      newCustomField = new CustomFieldModel();
      newCustomField.id = customField.id;
      newCustomField.name = customField.name;
      newCustomField.label = customField.label;
      newCustomField.type = customField.type;
      newCustomField.possibleValues = customField.possibleValues;
      newCustomField.defaultValue = customField.defaultValue;
      newCustomField.validRegexp = customField.validRegexp;
      newCustomField.lengthMin = customField.lengthMin;
      newCustomField.lengthMax = customField.lengthMax;
      newCustomField.showOnDesign = customField.showOnDesign;
      newCustomField.enableOnDesign = customField.enableOnDesign;
      newCustomField.showOnExecution = customField.showOnExecution;
      newCustomField.enableOnExecution = customField.enableOnExecution;
      newCustomField.showOnTestPlanDesign = customField.showOnTestPlanDesign;
      newCustomField.enableOnTestPlanDesign = customField.enableOnTestPlanDesign;
      newCustomField.displayOrder = customField.displayOrder;
      newCustomField.location = customField.location;
      newCustomField.value = customField.value;
    }
    return newCustomField;
  }

  jsonToTestStepsList(steps: any[]): TestCaseStepModel[] {
    let stepsList: TestCaseStepModel[] = [];
    for (let step of steps) {
      if (step !== undefined && step !== null) {
        stepsList.push(this.jsonToTestCaseStepModel(step));
      }
    }
    return stepsList;
  }

  jsonToTestCaseStepModel(step: any): TestCaseStepModel {
    let newStep: TestCaseStepModel;
    if (step !== undefined && step !== null) {
      newStep = new TestCaseStepModel();
      newStep.id = step.id;
      newStep.testCaseVersionId = step.testCaseVersionId;
      newStep.number = step.number;
      newStep.actions = step.actions;
      newStep.expectedResults = step.expectedResults;
      newStep.active = step.active;

      newStep.executionType = step.executionType;
    }
    return newStep;
  }

  jsonToPlatform(platform: any): PlatformModel {
    let newPlatform: PlatformModel;
    if (platform !== undefined && platform !== null) {
      newPlatform = new PlatformModel();
      newPlatform.id = platform.id;
      newPlatform.name = platform.name;
      newPlatform.notes = platform.notes;
    }
    return newPlatform;
  }

  /*************************/
  /********* Plans *********/
  /*************************/

  jsonToTestPlanList(plans: any[]): TestPlanModel[] {
    let plansList: TestPlanModel[] = [];
    for (let plan of plans) {
      plansList.push(this.jsonToTestPlanModel(plan));
    }
    return plansList;
  }

  jsonToTestPlanModel(plan: any): TestPlanModel {
    let newSuite: TestPlanModel;
    newSuite = new TestPlanModel();
    newSuite.id = plan.id;
    newSuite.name = plan.name;
    newSuite.projectName = plan.projectName;
    newSuite.notes = plan.notes;
    newSuite.active = plan.active;
    newSuite.public = plan.public;
    return newSuite;
  }

  /**************************/
  /********* Builds *********/
  /**************************/

  jsonToBuildList(builds: any[]): BuildModel[] {
    let buiildsList: BuildModel[] = [];
    for (let build of builds) {
      buiildsList.push(this.jsonToBuildModel(build));
    }
    return buiildsList;
  }

  jsonToBuildModel(build: any): BuildModel {
    let newPlan: BuildModel;
    newPlan = new BuildModel();
    newPlan.id = build.id;
    newPlan.testPlanId = build.testPlanId;
    newPlan.name = build.name;
    newPlan.notes = build.notes;

    return newPlan;
  }

  /***************************/
  /********** Execs **********/
  /***************************/

  jsonToExecList(execs: any[]): TestCaseExecutionModel[] {
    let execsList: TestCaseExecutionModel[] = [];
    for (let exec of execs) {
      execsList.push(this.jsonToExecModel(exec));
    }
    return execsList;
  }

  jsonToExecModel(exec: any): TestCaseExecutionModel {
    let newExec: TestCaseExecutionModel;
    newExec = new TestCaseExecutionModel();
    newExec.id = exec.id;
    newExec.buildId = exec.buildId;
    newExec.testerId = exec.testerId;
    newExec.executionTimeStamp = exec.executionTimeStamp;
    newExec.status = exec.status;
    newExec.testPlanId = exec.testPlanId;
    newExec.testCaseVersionId = exec.testCaseVersionId;
    newExec.testCaseVersionNumber = exec.testCaseVersionNumber;

    newExec.executionType = exec.executionType;
    newExec.notes = exec.notes;

    return newExec;
  }
}
