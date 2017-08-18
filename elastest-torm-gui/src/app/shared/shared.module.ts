import { NgModule, } from '@angular/core';
import { CommonModule, } from '@angular/common';
import { FormsModule, ReactiveFormsModule, } from '@angular/forms';
import { FlexLayoutModule, } from '@angular/flex-layout';
import {
  CovalentDataTableModule, CovalentMediaModule, CovalentLoadingModule,
  CovalentNotificationsModule, CovalentLayoutModule, CovalentMenuModule,
  CovalentPagingModule, CovalentSearchModule, CovalentStepsModule,
  CovalentCommonModule, CovalentDialogsModule,
} from '@covalent/core';
import {
  MdButtonModule, MdCardModule, MdIconModule,
  MdListModule, MdMenuModule, MdTooltipModule,
  MdSlideToggleModule, MdInputModule, MdCheckboxModule,
  MdToolbarModule, MdSnackBarModule, MdSidenavModule,
  MdTabsModule, MdSelectModule,
} from '@angular/material';
import { NgxChartsModule, } from '@swimlane/ngx-charts';
import { LogsViewComponent } from './logs-view/logs-view.component';
import { MetricsViewComponent } from './metrics-view/metrics-view.component';
import { LoadPreviousViewComponent } from './load-previous-view/load-previous-view.component';
import { ParametersViewComponent } from './parameters-view/parameters-view.component';
import { ComplexMetricsViewComponent } from './metrics-view/complex-metrics-view/complex-metrics-view.component';
import { ComboChartComponent } from './metrics-view/complex-metrics-view/combo-chart/combo-chart.component';
import { CovalentExpansionPanelModule } from '@covalent/core';
import { EtmComplexMetricsGroupComponent } from './metrics-view/complex-metrics-view/etm-complex-metrics-group/etm-complex-metrics-group.component';
import { TooltipAreaComponent } from './metrics-view/complex-metrics-view/combo-chart/tooltip-area.component';
import { EtmLogsGroupComponent } from './logs-view/etm-logs-group/etm-logs-group.component';

const FLEX_LAYOUT_MODULES: any[] = [
  FlexLayoutModule,
];

const ANGULAR_MODULES: any[] = [
  FormsModule, ReactiveFormsModule,
];

const MATERIAL_MODULES: any[] = [
  MdButtonModule, MdCardModule, MdIconModule,
  MdListModule, MdMenuModule, MdTooltipModule,
  MdSlideToggleModule, MdInputModule, MdCheckboxModule,
  MdToolbarModule, MdSnackBarModule, MdSidenavModule,
  MdTabsModule, MdSelectModule,
];

const COVALENT_MODULES: any[] = [
  CovalentDataTableModule, CovalentMediaModule, CovalentLoadingModule,
  CovalentNotificationsModule, CovalentLayoutModule, CovalentMenuModule,
  CovalentPagingModule, CovalentSearchModule, CovalentStepsModule,
  CovalentCommonModule, CovalentDialogsModule,
];

const CHART_MODULES: any[] = [
  NgxChartsModule,
];

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
    ComplexMetricsViewComponent,
    ComboChartComponent,
    EtmComplexMetricsGroupComponent,
    TooltipAreaComponent,
    EtmLogsGroupComponent,    
  ],
  exports: [
    ANGULAR_MODULES,
    MATERIAL_MODULES,
    COVALENT_MODULES,
    CHART_MODULES,
    FLEX_LAYOUT_MODULES,
    LogsViewComponent,
    MetricsViewComponent,
    ComplexMetricsViewComponent,
    LoadPreviousViewComponent,
    ParametersViewComponent,
    ComboChartComponent,
    EtmComplexMetricsGroupComponent,
    EtmLogsGroupComponent,
    TooltipAreaComponent,
  ]
})
export class SharedModule { }
