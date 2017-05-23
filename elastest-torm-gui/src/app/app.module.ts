import {NgModule, Type} from "@angular/core";
import {BrowserModule, Title} from "@angular/platform-browser";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";

import {CovalentCoreModule} from "@covalent/core";
import {CovalentHttpModule} from "@covalent/http";
import {CovalentHighlightModule} from "@covalent/highlight";
import {CovalentMarkdownModule} from "@covalent/markdown";

import {AppComponent} from "./app.component";
import {MainComponent} from "./main/main.component";
import {DashboardComponent} from "./dashboard/dashboard.component";
import {UsersComponent} from "./users/users.component";
import {UsersFormComponent} from "./users/form/form.component";
import {LogsComponent} from "./logs/logs.component";
import {FormComponent} from "./form/form.component";
import {DetailComponent} from "./detail/detail.component";
import {LoginComponent} from "./login/login.component";
import {DashboardProductComponent} from "./dashboard-product/dashboard-product.component";
import {ProductOverviewComponent} from "./dashboard-product/overview/overview.component";
import {ProductStatsComponent} from "./dashboard-product/stats/stats.component";
import {ProductFeaturesComponent} from "./dashboard-product/features/features.component";
import {FeaturesFormComponent} from "./dashboard-product/features/form/form.component";
import {TemplatesComponent} from "./templates/templates.component";
import {DashboardTemplateComponent} from "./templates/dashboard/dashboard.component";
import {EmailTemplateComponent} from "./templates/email/email.component";
import {EditorTemplateComponent} from "./templates/editor/editor.component";
import {appRoutes, appRoutingProviders} from "./app.routes";

import {RequestInterceptor} from "../config/interceptors/request.interceptor";

import {NgxChartsModule} from "@swimlane/ngx-charts";

import {TestManagerComponent} from "./test-manager/test-manager.commponent";
import {TestManagerService} from "./test-manager/test-manager.service";


import {StompService} from "ng2-stomp-service";
import {StompWSManager} from "./test-manager/stomp-ws-manager.service";
import {SafeUrlPipe} from "./test-manager/sanitizer.pipe";

const httpInterceptorProviders: Type<any>[] = [
  RequestInterceptor,
];

@NgModule({
  declarations: [
    AppComponent,
    MainComponent,
    DashboardComponent,
    DashboardProductComponent,
    ProductOverviewComponent,
    ProductStatsComponent,
    ProductFeaturesComponent,
    FeaturesFormComponent,
    UsersComponent,
    UsersFormComponent,
    LogsComponent,
    FormComponent,
    DetailComponent,
    LoginComponent,
    TemplatesComponent,
    DashboardTemplateComponent,
    EmailTemplateComponent,
    EditorTemplateComponent,
    TestManagerComponent,
    SafeUrlPipe,
  ], // directives, components, and pipes owned by this NgModule
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    CovalentCoreModule,
    CovalentHttpModule.forRoot({
      interceptors: [{
        interceptor: RequestInterceptor, paths: ['**'],
      }],
    }),
    CovalentHighlightModule,
    CovalentMarkdownModule,
    appRoutes,
    NgxChartsModule,
  ], // modules needed to run this module
  providers: [
    appRoutingProviders,
    httpInterceptorProviders,
    Title,
    TestManagerService,
    StompService,
    StompWSManager,
  ], // additional providers needed for this module
  entryComponents: [ ],
  bootstrap: [ AppComponent ],
})
export class AppModule {}
