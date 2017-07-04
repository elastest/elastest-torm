import { APP_INITIALIZER, NgModule, Type } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BrowserModule, Title } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { CovalentHttpModule, IHttpInterceptor } from '@covalent/http';
import { CovalentHighlightModule } from '@covalent/highlight';
import { CovalentMarkdownModule } from '@covalent/markdown';

import { AppComponent } from './app.component';
import { RequestInterceptor } from '../config/interceptors/request.interceptor';

import { routedComponents, AppRoutingModule } from './app-routing.module';

import { SharedModule } from './shared/shared.module';
import { StompService } from 'ng2-stomp-service';
import { StompWSManager } from './elastest-etm/stomp-ws-manager.service';
import { TJobService } from './elastest-etm/tjob/tjob.service';
import { TJobExecService } from './elastest-etm/tjob-exec/tjobExec.service';
import { SutService } from './elastest-etm/sut/sut.service';
import { SutExecService } from './elastest-etm/sut-exec/sutExec.service';
import { ProjectService } from './elastest-etm/project/project.service';
import { ElasticSearchService } from './elastest-log-manager/services/elasticsearch.service';
import { MdDatepickerModule, MdNativeDateModule, MdRadioModule } from '@angular/material';
import { ConfigurationService } from './config/configuration-service.service';
import { configServiceFactory } from './config/configServiceFactory';
import { TjobManagerComponent } from './elastest-etm/tjob/tjob-manager/tjob-manager.component';
import { SutManagerComponent } from './elastest-etm/sut/sut-manager/sut-manager.component';
import { TjobExecManagerComponent } from './elastest-etm/tjob-exec/tjob-exec-manager/tjob-exec-manager.component';
import { SutExecManagerComponent } from './elastest-etm/sut-exec/sut-exec-manager/sut-exec-manager.component';


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
  ], // directives, components, and pipes owned by this NgModule
  imports: [
    AppRoutingModule,
    BrowserModule,
    BrowserAnimationsModule,
    SharedModule,
    MdDatepickerModule,
    MdNativeDateModule,
    MdRadioModule,
    BrowserModule,
    FormsModule,
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
    StompService,
    StompWSManager,
    SutService,
    SutExecService,
    TJobService,
    TJobExecService,
    ProjectService,
    ElasticSearchService,
     ConfigurationService, { provide: APP_INITIALIZER, useFactory: configServiceFactory,
      deps: [ConfigurationService], multi: true }
  ],
  entryComponents: [],
  bootstrap: [AppComponent],
})
export class AppModule { }
