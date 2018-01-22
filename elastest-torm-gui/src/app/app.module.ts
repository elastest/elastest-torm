import { TestLinkService } from './etm-testlink/testlink.service';
import { LogAnalyzerService } from './elastest-log-analyzer/log-analyzer.service';
import {
  MonitoringConfigurationComponent,
} from './elastest-etm/etm-monitoring-view/monitoring-configuration/monitoring-configuration.component';
import { EtmMonitoringViewComponent } from './elastest-etm/etm-monitoring-view/etm-monitoring-view.component';
import { ETModelsTransformServices } from './shared/services/et-models-transform.service';
import { ETTestlinkModelsTransformService } from './shared/services/et-testlink-models-transform.service';
import { TitlesService } from './shared/services/titles.service';
import { TestEnginesService } from './elastest-test-engines/test-engines.service';
import { EsmService } from './elastest-esm/esm-service.service';
import { RouterModule } from '@angular/router';
import { TdLayoutManageListComponent } from '@covalent/core/layout/layout-manage-list/layout-manage-list.component';
import { CovalentExpansionPanelModule } from '@covalent/core';
import { APP_INITIALIZER, NgModule, Type } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BrowserModule, Title } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { CovalentHttpModule, IHttpInterceptor } from '@covalent/http';
import { CovalentHighlightModule } from '@covalent/highlight';
import { CovalentMarkdownModule } from '@covalent/markdown';

import { AppComponent } from './app.component';
import { RequestInterceptor } from '../config/interceptors/request.interceptor';

import { routedComponents, AppRoutingModule, appRoutes } from './app-routing.module';

import { SharedModule } from './shared/shared.module';
import { StompService } from './shared/services/stomp.service';
import { StompWSManager } from './shared/services/stomp-ws-manager.service';
import { TJobService } from './elastest-etm/tjob/tjob.service';
import { TJobExecService } from './elastest-etm/tjob-exec/tjobExec.service';
import { SutService } from './elastest-etm/sut/sut.service';
import { SutExecService } from './elastest-etm/sut-exec/sutExec.service';
import { ProjectService } from './elastest-etm/project/project.service';
import { ElasticSearchService } from './shared/services/elasticsearch.service';
import { MdDatepickerModule, MdNativeDateModule, MdRadioModule, MdButtonToggleModule, MdDialogModule, MdSidenavModule, MdProgressSpinnerModule }
  from '@angular/material';
import { ConfigurationService } from './config/configuration-service.service';
import { configServiceFactory } from './config/configServiceFactory';
import { TjobManagerComponent } from './elastest-etm/tjob/tjob-manager/tjob-manager.component';
import { SutManagerComponent } from './elastest-etm/sut/sut-manager/sut-manager.component';
import { TjobExecManagerComponent } from './elastest-etm/tjob-exec/tjob-exec-manager/tjob-exec-manager.component';
import { SutExecManagerComponent } from './elastest-etm/sut-exec/sut-exec-manager/sut-exec-manager.component';
import { ElastestRabbitmqService } from './shared/services/elastest-rabbitmq.service';
import { ElastestESService } from './shared/services/elastest-es.service';
import { PopupService } from './shared/services/popup.service';

import { EusService } from './elastest-eus/elastest-eus.service';
import { SafePipe } from './elastest-eus/safe-pipe';
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
import { ExecutionModalComponent } from './elastest-log-manager/execution-modal/execution-modal.component';

import { HelpComponent } from './elastest-etm/help/help.component';
import { ShowMessageModalComponent } from './elastest-log-analyzer/show-message-modal/show-message-modal.component';
import { MarkComponent } from './elastest-log-analyzer/mark-component/mark.component';
import { InputTrimModule } from 'ng2-trim-directive';
import { EtmTestlinkComponent } from './etm-testlink/etm-testlink.component';
import { TestProjectComponent } from './etm-testlink/test-project/test-project.component';
import { TestProjectFormComponent } from './etm-testlink/test-project/test-project-form/test-project-form.component';
import { TestSuiteComponent } from './etm-testlink/test-suite/test-suite.component';
import { TestPlanComponent } from './etm-testlink/test-plan/test-plan.component';
import { TestPlanFormComponent } from './etm-testlink/test-plan/test-plan-form/test-plan-form.component';
import { TestSuiteFormComponent } from './etm-testlink/test-suite/test-suite-form/test-suite-form.component';
import { TestCaseComponent } from './etm-testlink/test-case/test-case.component';
import { TestCaseFormComponent } from './etm-testlink/test-case/test-case-form/test-case-form.component';
import { BuildComponent } from './etm-testlink/build/build.component';
import { BuildFormComponent } from './etm-testlink/build/build-form/build-form.component';
import { TestCaseStepComponent } from './etm-testlink/test-case-step/test-case-step.component';
import { TestCaseStepFormComponent } from './etm-testlink/test-case-step/test-case-step-form/test-case-step-form.component';
import { ExecuteCaseModalComponent } from './etm-testlink/build/execute-case-modal/execute-case-modal.component';


const httpInterceptorProviders: Type<any>[] = [
  RequestInterceptor,
];

@NgModule({
  declarations: [
    AppComponent,
    routedComponents,
    TjobManagerComponent,
    SutManagerComponent,
    TjobExecManagerComponent,
    SutExecManagerComponent,
    SafePipe,
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
    ExecutionModalComponent,
    MonitoringConfigurationComponent,
    HelpComponent,
    ShowMessageModalComponent,
    MarkComponent,
    EtmTestlinkComponent,
    TestProjectComponent,
    TestProjectFormComponent,
    TestSuiteComponent,
    TestPlanComponent,
    TestPlanFormComponent,
    TestSuiteFormComponent,
    TestCaseComponent,
    TestCaseFormComponent,
    BuildComponent,
    BuildFormComponent,
    TestCaseStepComponent,
    TestCaseStepFormComponent,
    ExecuteCaseModalComponent,
  ], // directives, components, and pipes owned by this NgModule
  imports: [
    appRoutes,
    AppRoutingModule,
    BrowserModule,
    BrowserAnimationsModule,
    SharedModule,
    MdDatepickerModule,
    MdNativeDateModule,
    MdRadioModule,
    BrowserModule,
    FormsModule,
    MdButtonToggleModule,
    MdDialogModule,
    MdSidenavModule,
    MdProgressSpinnerModule,
    AgGridModule.withComponents([]),
    TreeModule,
    InputTrimModule,
    CovalentExpansionPanelModule,
    CovalentHttpModule.forRoot({
      interceptors: [{
        interceptor: RequestInterceptor, paths: ['**'],
      }],
    }),
    CovalentHighlightModule,
    CovalentMarkdownModule,
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
    TJobExecService,
    ProjectService,
    ElasticSearchService,
    ElastestRabbitmqService,
    ElastestESService,
    PopupService,
    TitlesService,
    ETModelsTransformServices,
    ETTestlinkModelsTransformService,
    TestLinkService,
    ConfigurationService, {
      provide: APP_INITIALIZER, useFactory: configServiceFactory,
      deps: [ConfigurationService], multi: true
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
    ExecutionModalComponent,
    ExecuteCaseModalComponent,
    MonitoringConfigurationComponent
  ],
  bootstrap: [AppComponent],
})
export class AppModule { }
