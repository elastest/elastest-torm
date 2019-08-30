import { DatePipe } from '@angular/common';
import { MonitoringService } from './shared/services/monitoring.service';
import { FilesService } from './shared/services/files.service';
import { TransformService } from './elastest-etm/manage-elastest/manage-main-services/transform.service';
import { TestLinkService } from './etm-testlink/testlink.service';
import { ExternalService } from './elastest-etm/external/external.service';
import { LogAnalyzerService } from './elastest-log-analyzer/log-analyzer.service';
import { MonitoringConfigurationComponent } from './elastest-etm/etm-monitoring-view/monitoring-configuration/monitoring-configuration.component';
import { EtmMonitoringViewComponent } from './elastest-etm/etm-monitoring-view/etm-monitoring-view.component';
import { ETModelsTransformServices } from './shared/services/et-models-transform.service';
import { ETTestlinkModelsTransformService } from './shared/services/et-testlink-models-transform.service';
import { TitlesService } from './shared/services/titles.service';
import { EtPluginsService } from './elastest-test-engines/et-plugins.service';
import { EsmService } from './elastest-esm/esm-service.service';
import { TdLayoutManageListComponent, CovalentLoadingModule, CovalentFileModule, CovalentChipsModule } from '@covalent/core';
import { CovalentExpansionPanelModule, CovalentMessageModule } from '@covalent/core';
import { CovalentCodeEditorModule } from '@covalent/code-editor';

import { APP_INITIALIZER, NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BrowserModule, Title } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { AutosizeModule } from 'ngx-autosize';
import { CovalentHighlightModule } from '@covalent/highlight';
import { CovalentMarkdownModule } from '@covalent/markdown';

import { AppComponent } from './app.component';
import { DragDropModule } from '@angular/cdk/drag-drop';

import { routedComponents, AppRoutingModule, appRoutes } from './app-routing.module';
import { HttpClientModule } from '@angular/common/http';
import { BreadcrumbService } from './shared/breadcrumb/breadcrumb.service';
import { SharedModule } from './shared/shared.module';
import { StompRService } from '@stomp/ng2-stompjs';
import { StompWSManager } from './shared/services/stomp-ws-manager.service';
import { TJobService } from './elastest-etm/tjob/tjob.service';
import { TJobExecService } from './elastest-etm/tjob-exec/tjobExec.service';
import { SutService } from './elastest-etm/sut/sut.service';
import { SutExecService } from './elastest-etm/sut-exec/sutExec.service';
import { ProjectService } from './elastest-etm/project/project.service';
import { TestSuiteService } from './elastest-etm/test-suite/test-suite.service';
import { TestCaseService } from './elastest-etm/test-case/test-case.service';
import {
  MatDatepickerModule,
  MatNativeDateModule,
  MatRadioModule,
  MatButtonToggleModule,
  MatDialogModule,
  MatFormFieldModule,
  MatSidenavModule,
  MatProgressSpinnerModule,
} from '@angular/material';
import { ConfigurationService } from './config/configuration-service.service';
import { configServiceFactory } from './config/configServiceFactory';
import { TjobManagerComponent } from './elastest-etm/tjob/tjob-manager/tjob-manager.component';
import { SutManagerComponent } from './elastest-etm/sut/sut-manager/sut-manager.component';
import { FinishedTjobExecManagerComponent } from './elastest-etm/tjob-exec/finished-tjob-exec-manager/finished-tjob-exec-manager.component';
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
import { ElastestQaComponent } from './elastest-test-engines/elastest-qa/elastest-qa.component';
import { TestEngineViewComponent } from './elastest-test-engines/test-engine-view/test-engine-view.component';
import { FilesManagerComponent } from './elastest-etm/files-manager/files-manager.component';
import { ProjectManagerComponent } from './elastest-etm/project/project-manager/project-manager.component';
import { TJobExecsManagerComponent } from './elastest-etm/tjob-exec/tjob-execs-manager/tjob-execs-manager.component';
import { GetIndexModalComponent } from './elastest-log-analyzer/get-index-modal/get-index-modal.component';
import { ElastestLogAnalyzerComponent } from './elastest-log-analyzer/elastest-log-analyzer.component';
import { AgGridModule } from 'ag-grid-angular';
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
import { LiveTjobExecManagerComponent } from './elastest-etm/tjob-exec/live-tjob-exec-manager/live-tjob-exec-manager.component';
import { DashboardComponent } from './elastest-etm/dashboard/dashboard.component';
import { ChildTjobExecsViewComponent } from './elastest-etm/tjob-exec/child-tjob-execs-view/child-tjob-execs-view.component';
import { TjobExecViewComponent } from './elastest-etm/tjob-exec/tjob-exec-view/tjob-exec-view.component';
import { ParentTjobExecReportViewComponent } from './elastest-etm/tjob-exec/parent-tjob-exec-report-view/parent-tjob-exec-report-view.component';
import { CredentialsDialogComponent } from './shared/credentials-dialog/credentials-dialog.component';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { library } from '@fortawesome/fontawesome-svg-core';
import { faJenkins } from '@fortawesome/free-brands-svg-icons';
import { fas } from '@fortawesome/free-solid-svg-icons';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import { TjobExecsComparatorComponent } from './elastest-etm/tjob-exec/tjob-execs-comparator/tjob-execs-comparator.component';
import { TableService } from './elastest-log-comparator/service/table.service';
import { ManageElastestComponent } from './elastest-etm/manage-elastest/manage-elastest.component';
import { ManageMainServicesComponent } from './elastest-etm/manage-elastest/manage-main-services/manage-main-services.component';
import { ManageElasticsearchComponent } from './elastest-etm/manage-elastest/manage-elasticsearch/manage-elasticsearch.component';
import { ElasticsearchApiService } from './elastest-etm/manage-elastest/manage-elasticsearch/elasticsearch-api.service';
import { ManageClustersComponent } from './elastest-etm/manage-elastest/manage-clusters/manage-clusters.component';
import { ManageClustersService } from './elastest-etm/manage-elastest/manage-clusters/manage-clusters.service';
import { EtmRestClientService } from './shared/services/etm-rest-client.service';
import { ExternalMonitoringDbComponent } from './elastest-etm/sut/sut-form/external-monitoring-db/external-monitoring-db.component';
import { ExternalMonitoringDBService } from './elastest-etm/sut/sut-form/external-monitoring-db/external-monitoring-db.service';
import { ExternalElasticsearchConfigurationComponent } from './elastest-etm/external-monitoring-db/external-elasticsearch-configuration/external-elasticsearch-configuration.component';
import { ExternalPrometheusConfigurationComponent } from './elastest-etm/external-monitoring-db/external-prometheus-configuration/external-prometheus-configuration.component';
import { AngularSplitModule } from 'angular-split';
import { BrowserCardComponentComponent } from './elastest-eus/browser-card-component/browser-card-component.component';
import { CrossbrowserComponentComponent } from './elastest-eus/crossbrowser-component/crossbrowser-component.component';

library.add(faJenkins);
library.add(fas);

@NgModule({
  declarations: [
    AppComponent,
    routedComponents,
    TjobManagerComponent,
    SutManagerComponent,
    FinishedTjobExecManagerComponent,
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
    ElastestQaComponent,
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
    TestPlanExecutionComponent,
    TestSuitesViewComponent,
    TimeAgoPipe,
    LiveTjobExecManagerComponent,
    DashboardComponent,
    ChildTjobExecsViewComponent,
    TjobExecViewComponent,
    ParentTjobExecReportViewComponent,
    CredentialsDialogComponent,
    TjobExecsComparatorComponent,
    ManageElastestComponent,
    ManageMainServicesComponent,
    ManageElasticsearchComponent,
    ManageClustersComponent,
    ExternalMonitoringDbComponent,
    ExternalElasticsearchConfigurationComponent,
    ExternalPrometheusConfigurationComponent,
    BrowserCardComponentComponent,
    CrossbrowserComponentComponent,
  ], // directives, components, and pipes owned by this NgModule
  imports: [
    appRoutes,
    AppRoutingModule,
    AgGridModule.withComponents([]),
    AngularSplitModule.forRoot(),
    AutosizeModule,
    BrowserModule,
    BrowserAnimationsModule,
    CovalentChipsModule,
    CovalentCodeEditorModule,
    CovalentExpansionPanelModule,
    CovalentLoadingModule,
    CovalentMessageModule,
    CovalentHighlightModule,
    CovalentMarkdownModule,
    CovalentFileModule,
    DragDropModule,
    FontAwesomeModule,
    FormsModule,
    HttpClientModule,
    InputTrimModule,
    MatButtonToggleModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatDialogModule,
    MatNativeDateModule,
    MatProgressSpinnerModule,
    MatRadioModule,
    MatSidenavModule,
    SharedModule,
    TreeModule,
  ], // modules needed to run this module
  providers: [
    Title,
    TdLayoutManageListComponent,
    StompWSManager,
    SutService,
    ExternalMonitoringDBService,
    SutExecService,
    TJobService,
    TestCaseService,
    TestSuiteService,
    TJobExecService,
    ProjectService,
    ElasticsearchApiService,
    ManageClustersService,
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
    EtmRestClientService,
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
    EtPluginsService,
    LogAnalyzerService,
    StompRService,
    TableService,
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
    CredentialsDialogComponent,
  ],
  bootstrap: [AppComponent],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class AppModule {
  constructor(private matIconRegistry: MatIconRegistry, private domSanitizer: DomSanitizer) {
    matIconRegistry.addSvgIconSet(domSanitizer.bypassSecurityTrustResourceUrl('/assets/symbol-defs.svg'));
  }
}
