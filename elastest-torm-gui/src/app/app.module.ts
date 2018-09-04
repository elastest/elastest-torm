import { DatePipe } from '@angular/common';
import { MonitoringService } from './shared/services/monitoring.service';
import { FilesService } from './shared/services/files.service';
import { TransformService } from './elastest-etm/help/transform.service';
import { TestLinkService } from './etm-testlink/testlink.service';
import { ExternalService } from './elastest-etm/external/external.service';
import { LogAnalyzerService } from './elastest-log-analyzer/log-analyzer.service';
import { MonitoringConfigurationComponent } from './elastest-etm/etm-monitoring-view/monitoring-configuration/monitoring-configuration.component';
import { EtmMonitoringViewComponent } from './elastest-etm/etm-monitoring-view/etm-monitoring-view.component';
import { ETModelsTransformServices } from './shared/services/et-models-transform.service';
import { ETTestlinkModelsTransformService } from './shared/services/et-testlink-models-transform.service';
import { TitlesService } from './shared/services/titles.service';
import { TestEnginesService } from './elastest-test-engines/test-engines.service';
import { EsmService } from './elastest-esm/esm-service.service';
import { TdLayoutManageListComponent } from '@covalent/core/layout/layout-manage-list/layout-manage-list.component';
import { CovalentExpansionPanelModule, CovalentMessageModule } from '@covalent/core';
import { APP_INITIALIZER, NgModule, Type } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BrowserModule, Title } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { CovalentHttpModule } from '@covalent/http';
import { CovalentHighlightModule } from '@covalent/highlight';
import { CovalentMarkdownModule } from '@covalent/markdown';

import { AppComponent } from './app.component';
import { RequestInterceptor } from '../config/interceptors/request.interceptor';

import { routedComponents, AppRoutingModule, appRoutes } from './app-routing.module';

import { BreadcrumbService } from './shared/breadcrumb/breadcrumb.service';
import { SharedModule } from './shared/shared.module';
import { StompService } from './shared/services/stomp.service';
import { StompWSManager } from './shared/services/stomp-ws-manager.service';
import { TJobService } from './elastest-etm/tjob/tjob.service';
import { TJobExecService } from './elastest-etm/tjob-exec/tjobExec.service';
import { SutService } from './elastest-etm/sut/sut.service';
import { SutExecService } from './elastest-etm/sut-exec/sutExec.service';
import { ProjectService } from './elastest-etm/project/project.service';
import { TestSuiteService } from './elastest-etm/test-suite/test-suite.service';
import { TestCaseService } from './elastest-etm/test-case/test-case.service';
import {
  MdDatepickerModule,
  MdNativeDateModule,
  MdRadioModule,
  MdButtonToggleModule,
  MdDialogModule,
  MdSidenavModule,
  MdProgressSpinnerModule,
} from '@angular/material';
import { ConfigurationService } from './config/configuration-service.service';
import { configServiceFactory } from './config/configServiceFactory';
import { TjobManagerComponent } from './elastest-etm/tjob/tjob-manager/tjob-manager.component';
import { SutManagerComponent } from './elastest-etm/sut/sut-manager/sut-manager.component';
import { TjobExecManagerComponent } from './elastest-etm/tjob-exec/tjob-exec-manager/tjob-exec-manager.component';
import { SutExecManagerComponent } from './elastest-etm/sut-exec/sut-exec-manager/sut-exec-manager.component';
import { ElastestRabbitmqService } from './shared/services/elastest-rabbitmq.service';
import { PopupService } from './shared/services/popup.service';

import { EusService } from './elastest-eus/elastest-eus.service';
import { SafePipe } from './elastest-eus/safe-pipe';
import { CapitalizePipe } from './elastest-eus/capitalize-pipe';
import { ElastestEusDialog } from './elastest-eus/elastest-eus.dialog';
import { ElastestEusDialogService } from './elastest-eus/elastest-eus.dialog.service';
import { RunTJobModalComponent } from './elastest-etm/tjob/run-tjob-modal/run-tjob-modal.component';

import { InstancesManagerComponent } from './elastest-esm/support-services/instance-manager/instances-manager.component';
import { ServiceGuiComponent } from './elastest-esm/support-services/service-gui/service-gui.component';
import { ServiceDetailComponent } from './elastest-esm/support-services/service-detail/service-detail.component';
import { SafeUrlPipe } from './sanitizer.pipe';
import { ElastestTestEnginesComponent } from './elastest-test-engines/elastest-test-engines.component';
import { ElastestEreComponent } from './elastest-test-engines/elastest-ere/elastest-ere.component';
import { ElastestEceComponent } from './elastest-test-engines/elastest-ece/elastest-ece.component';
import { TestEngineViewComponent } from './elastest-test-engines/test-engine-view/test-engine-view.component';
import { FilesManagerComponent } from './elastest-etm/files-manager/files-manager.component';
import { ProjectManagerComponent } from './elastest-etm/project/project-manager/project-manager.component';
import { TJobExecsManagerComponent } from './elastest-etm/tjob-exec/tjob-execs-manager/tjob-execs-manager.component';
import { GetIndexModalComponent } from './elastest-log-analyzer/get-index-modal/get-index-modal.component';
import { ElastestLogAnalyzerComponent } from './elastest-log-analyzer/elastest-log-analyzer.component';
import { AgGridModule } from 'ag-grid-angular/main';
import { TreeModule } from 'angular-tree-component';

import { HelpComponent } from './elastest-etm/help/help.component';
import { ShowMessageModalComponent } from './elastest-log-analyzer/show-message-modal/show-message-modal.component';
import { MarkComponent } from './elastest-log-analyzer/mark-component/mark.component';
import { InputTrimModule } from 'ng2-trim-directive';
import { EtmTestlinkComponent } from './etm-testlink/etm-testlink.component';
import { TestProjectComponent } from './etm-testlink/test-project/test-project.component';
import { TestProjectFormComponent } from './etm-testlink/test-project/test-project-form/test-project-form.component';
import { TLTestSuiteComponent } from './etm-testlink/test-suite/test-suite.component';
import { TestSuiteComponent } from './elastest-etm/test-suite/test-suite.component';
import { TestPlanComponent } from './etm-testlink/test-plan/test-plan.component';
import { TestPlanFormComponent } from './etm-testlink/test-plan/test-plan-form/test-plan-form.component';
import { TestSuiteFormComponent } from './etm-testlink/test-suite/test-suite-form/test-suite-form.component';
import { TLTestCaseComponent } from './etm-testlink/test-case/test-case.component';
import { TestCaseComponent } from './elastest-etm/test-case/test-case.component';
import { TestCaseFormComponent } from './etm-testlink/test-case/test-case-form/test-case-form.component';
import { BuildComponent } from './etm-testlink/build/build.component';
import { BuildFormComponent } from './etm-testlink/build/build-form/build-form.component';
import { TestCaseStepComponent } from './etm-testlink/test-case-step/test-case-step.component';
import { TestCaseStepFormComponent } from './etm-testlink/test-case-step/test-case-step-form/test-case-step-form.component';
import { ExecuteCaseModalComponent } from './etm-testlink/build/execute-case-modal/execute-case-modal.component';
import { ExecutionComponent } from './etm-testlink/execution/execution.component';
import { TestCaseExecsComponent } from './etm-testlink/build/test-case-execs/test-case-execs.component';
import { Autosize } from 'angular2-autosize';
import { ExternalProjectComponent } from './elastest-etm/external/external-project/external-project.component';
import { ExternalTestCaseComponent } from './elastest-etm/external/external-test-case/external-test-case.component';
import { ExternalTestExecutionComponent } from './elastest-etm/external/external-test-execution/external-test-execution.component';
import { ExecutionFormComponent } from './etm-testlink/execution/execution-form/execution-form.component';
import { ExternalTestExecutionFormComponent } from './elastest-etm/external/external-test-execution/external-test-execution-form/external-test-execution-form.component';
import { ExecutionViewComponent } from './elastest-etm/external/external-test-execution/external-test-execution-form/execution-view/execution-view.component';
import { ExternalTjobComponent } from './elastest-etm/external/external-tjob/external-tjob.component';
import { ExternalTjobExecutionComponent } from './elastest-etm/external/external-tjob-execution/external-tjob-execution.component';
import { ETExternalModelsTransformService } from './elastest-etm/external/et-external-models-transform.service';
import { ExternalTjobExecutionNewComponent } from './elastest-etm/external/external-tjob-execution/external-tjob-execution-new/external-tjob-execution-new.component';
import { CaseExecutionViewComponent } from './elastest-etm/external/external-tjob-execution/external-tjob-execution-new/case-execution-view/case-execution-view.component';
import { ExternalTjobExecsViewComponent } from './elastest-etm/external/external-tjob-execution/external-tjob-execs-view/external-tjob-execs-view.component';
import { ExternalTjobFormComponent } from './elastest-etm/external/external-tjob/external-tjob-form/external-tjob-form.component';
import { TestSuitesViewComponent } from './elastest-etm/test-suite/test-suites-view/test-suites-view.component';
import { TestCasesViewComponent } from './elastest-etm/test-case/test-cases-view/test-cases-view.component';
import { SupportServiceConfigViewComponent } from './elastest-esm/support-service-config-view/support-service-config-view.component';
import { ExternalTestExecutionsViewComponent } from './elastest-etm/external/external-test-execution/external-test-executions-view/external-test-executions-view.component';
import { EdmContainerComponent } from './elastest-edm/edm-container/edm-container.component';
import { EmpContainerComponent } from './elastest-emp/emp-container/emp-container.component';
import { TestPlanExecutionComponent } from './etm-testlink/test-plan/test-plan-execution/test-plan-execution.component';
import { SelectBuildModalComponent } from './etm-testlink/test-plan/select-build-modal/select-build-modal.component';
import { TimeAgoPipe } from 'time-ago-pipe';
import { EtmJenkinsComponent } from './etm-jenkins/etm-jenkins.component';

const httpInterceptorProviders: Type<any>[] = [RequestInterceptor];

@NgModule({
  declarations: [
    AppComponent,
    routedComponents,
    TjobManagerComponent,
    SutManagerComponent,
    TjobExecManagerComponent,
    SutExecManagerComponent,
    SafePipe,
    CapitalizePipe,
    ElastestEusDialog,
    RunTJobModalComponent,
    EtmMonitoringViewComponent,
    InstancesManagerComponent,
    ServiceGuiComponent,
    ServiceDetailComponent,
    SafeUrlPipe,
    ElastestTestEnginesComponent,
    ElastestEreComponent,
    ElastestEceComponent,
    TestEngineViewComponent,
    FilesManagerComponent,
    ProjectManagerComponent,
    TJobExecsManagerComponent,
    GetIndexModalComponent,
    ElastestLogAnalyzerComponent,
    MonitoringConfigurationComponent,
    HelpComponent,
    ShowMessageModalComponent,
    MarkComponent,
    EtmTestlinkComponent,
    TestProjectComponent,
    TestProjectFormComponent,
    TLTestSuiteComponent,
    TestSuiteComponent,
    TestPlanComponent,
    TestPlanFormComponent,
    TestSuiteFormComponent,
    TLTestCaseComponent,
    TestCaseComponent,
    TestCaseFormComponent,
    BuildComponent,
    BuildFormComponent,
    TestCaseStepComponent,
    TestCaseStepFormComponent,
    ExecuteCaseModalComponent,
    ExecutionComponent,
    TestCaseExecsComponent,
    Autosize,
    CaseExecutionViewComponent,
    ExternalProjectComponent,
    ExternalTestCaseComponent,
    ExternalTestExecutionComponent,
    ExecutionFormComponent,
    ExternalTestExecutionFormComponent,
    ExecutionViewComponent,
    ExternalTjobComponent,
    ExternalTjobExecutionComponent,
    ExternalTjobExecutionNewComponent,
    EdmContainerComponent,
    EmpContainerComponent,
    EtmJenkinsComponent,
    ExternalTestExecutionsViewComponent,
    ExternalTjobExecsViewComponent,
    ExternalTjobFormComponent,
    SelectBuildModalComponent,
    SupportServiceConfigViewComponent,
    TestCasesViewComponent,
    // TestEnginesService,
    TestPlanExecutionComponent,
    TestSuitesViewComponent,
    TimeAgoPipe,
  ], // directives, components, and pipes owned by this NgModule
  imports: [
    appRoutes,
    AppRoutingModule,
    AgGridModule.withComponents([]),
    BrowserModule,
    BrowserAnimationsModule,
    CovalentExpansionPanelModule,
    CovalentMessageModule,
    CovalentHttpModule.forRoot({
      interceptors: [
        {
          interceptor: RequestInterceptor,
          paths: ['**'],
        },
      ],
    }),
    CovalentHighlightModule,
    CovalentMarkdownModule,
    FormsModule,
    InputTrimModule,
    MdDatepickerModule,
    MdButtonToggleModule,
    MdDialogModule,
    MdNativeDateModule,
    MdProgressSpinnerModule,
    MdRadioModule,
    MdSidenavModule,
    SharedModule,
    TreeModule,
  ], // modules needed to run this module
  providers: [
    httpInterceptorProviders,
    Title,
    TdLayoutManageListComponent,
    StompService,
    StompWSManager,
    SutService,
    SutExecService,
    TJobService,
    TestCaseService,
    TestSuiteService,
    TJobExecService,
    ProjectService,
    MonitoringService,
    ElastestRabbitmqService,
    PopupService,
    DatePipe,
    TitlesService,
    BreadcrumbService,
    FilesService,
    ETModelsTransformServices,
    ETTestlinkModelsTransformService,
    ETExternalModelsTransformService,
    TestLinkService,
    ExternalService,
    TransformService,
    ConfigurationService,
    {
      provide: APP_INITIALIZER,
      useFactory: configServiceFactory,
      deps: [ConfigurationService],
      multi: true,
    },
    EusService,
    ElastestEusDialogService,
    EsmService,
    TestEnginesService,
    LogAnalyzerService,
  ],
  entryComponents: [
    ElastestEusDialog,
    RunTJobModalComponent,
    GetIndexModalComponent,
    ShowMessageModalComponent,
    ExecuteCaseModalComponent,
    MonitoringConfigurationComponent,
    SelectBuildModalComponent,
    ElastestLogAnalyzerComponent,
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
