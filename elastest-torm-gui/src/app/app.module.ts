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
import { MdDatepickerModule, MdNativeDateModule, MdRadioModule, MdButtonToggleModule, MdDialogModule } from '@angular/material';
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
import { EtmLogsMetricsViewComponent } from './elastest-etm/etm-logs-metrics-view/etm-logs-metrics-view.component';
import { InstancesManagerComponent } from './elastest-esm/support-services/instance-manager/instances-manager.component';
import { ServiceGuiComponent } from './elastest-esm/support-services/service-gui/service-gui.component';
import { ServiceDetailComponent } from './elastest-esm/support-services/service-detail/service-detail.component';
import {SafeUrlPipe} from "./sanitizer.pipe";

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
    EtmLogsMetricsViewComponent,    
    InstancesManagerComponent, 
    ServiceGuiComponent, 
    ServiceDetailComponent,
    SafeUrlPipe,
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
    ConfigurationService, {
      provide: APP_INITIALIZER, useFactory: configServiceFactory,
      deps: [ConfigurationService], multi: true
    },
    EusService,
    ElastestEusDialogService,
    EsmService
  ],
  entryComponents: [
    ElastestEusDialog,
    RunTJobModalComponent
  ],
  bootstrap: [AppComponent],
})
export class AppModule { }
