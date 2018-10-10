import { EtmLogsGroupComponent } from '../elastest-etm/etm-monitoring-view/etm-logs-group/etm-logs-group.component';
import { EtmChartGroupComponent } from '../elastest-etm/etm-monitoring-view/etm-chart-group/etm-chart-group.component';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { FlexLayoutModule } from '@angular/flex-layout';
import {
  CovalentDataTableModule,
  CovalentMediaModule,
  CovalentLoadingModule,
  CovalentNotificationsModule,
  CovalentLayoutModule,
  CovalentMenuModule,
  CovalentPagingModule,
  CovalentSearchModule,
  CovalentStepsModule,
  CovalentCommonModule,
  CovalentDialogsModule,
} from '@covalent/core';
import {
  MdButtonModule,
  MdCardModule,
  MdIconModule,
  MdListModule,
  MdMenuModule,
  MdTooltipModule,
  MdSlideToggleModule,
  MdInputModule,
  MdCheckboxModule,
  MdToolbarModule,
  MdSnackBarModule,
  MdSidenavModule,
  MdTabsModule,
  MdSelectModule,
} from '@angular/material';
import { NgxChartsModule } from '@swimlane/ngx-charts';
import { LogsViewComponent } from './logs-view/logs-view.component';
import { MetricsViewComponent } from './metrics-view/metrics-view.component';
import { LoadPreviousViewComponent } from './load-previous-view/load-previous-view.component';
import { ParametersViewComponent } from './parameters-view/parameters-view.component';
import { MetricsChartCardComponent } from './metrics-view/metrics-chart-card/metrics-chart-card.component';
import { ComboChartComponent } from './metrics-view/metrics-chart-card/combo-chart/combo-chart.component';
import { CovalentExpansionPanelModule } from '@covalent/core';
import { TooltipAreaComponent } from './metrics-view/metrics-chart-card/combo-chart/components/tooltip-area.component';
import { TimelineComponent } from './metrics-view/metrics-chart-card/combo-chart/components/timeline.component';
import { VncClientComponent } from './vnc-client/vnc-client.component';
import { TestVncComponent } from './vnc-client/test-vnc/test-vnc.component';
import { RefreshComponent } from './refresh/refresh.component';
import { BreadcrumbComponent } from './breadcrumb/breadcrumb.component';
import { LogsViewTextComponent } from './logs-view-text/logs-view-text.component';
import { StringListViewComponent } from './string-list-view/string-list-view.component';
import { MultiConfigViewComponent } from './multi-config-view/multi-config-view.component';

const FLEX_LAYOUT_MODULES: any[] = [FlexLayoutModule];

const ANGULAR_MODULES: any[] = [FormsModule, ReactiveFormsModule];

const MATERIAL_MODULES: any[] = [
  MdButtonModule,
  MdCardModule,
  MdIconModule,
  MdListModule,
  MdMenuModule,
  MdTooltipModule,
  MdSlideToggleModule,
  MdInputModule,
  MdCheckboxModule,
  MdToolbarModule,
  MdSnackBarModule,
  MdSidenavModule,
  MdTabsModule,
  MdSelectModule,
];

const COVALENT_MODULES: any[] = [
  CovalentDataTableModule,
  CovalentMediaModule,
  CovalentLoadingModule,
  CovalentNotificationsModule,
  CovalentLayoutModule,
  CovalentMenuModule,
  CovalentPagingModule,
  CovalentSearchModule,
  CovalentStepsModule,
  CovalentCommonModule,
  CovalentDialogsModule,
];

const CHART_MODULES: any[] = [NgxChartsModule];

@NgModule({
  imports: [
    CommonModule,
    ANGULAR_MODULES,
    MATERIAL_MODULES,
    COVALENT_MODULES,
    CHART_MODULES,
    FLEX_LAYOUT_MODULES,
    CovalentExpansionPanelModule,
  ],
  declarations: [
    LogsViewComponent,
    MetricsViewComponent,
    LoadPreviousViewComponent,
    ParametersViewComponent,
    MetricsChartCardComponent,
    ComboChartComponent,
    EtmChartGroupComponent,
    TooltipAreaComponent,
    TimelineComponent,
    EtmLogsGroupComponent,
    VncClientComponent,
    TestVncComponent,
    RefreshComponent,
    StringListViewComponent,
    BreadcrumbComponent,
    LogsViewTextComponent,
    MultiConfigViewComponent,
  ],
  exports: [
    ANGULAR_MODULES,
    MATERIAL_MODULES,
    COVALENT_MODULES,
    CHART_MODULES,
    FLEX_LAYOUT_MODULES,
    LogsViewComponent,
    MetricsViewComponent,
    MetricsChartCardComponent,
    LoadPreviousViewComponent,
    ParametersViewComponent,
    MultiConfigViewComponent,
    StringListViewComponent,
    ComboChartComponent,
    EtmChartGroupComponent,
    EtmLogsGroupComponent,
    TooltipAreaComponent,
    TimelineComponent,
    VncClientComponent,
    TestVncComponent,
    RefreshComponent,
    BreadcrumbComponent,
    LogsViewTextComponent,
  ],
})
export class SharedModule {}
