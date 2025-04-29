/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgSelectModule } from '@ng-select/ng-select';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CalendarModule } from 'primeng/calendar';
import { MultiSelectModule } from 'primeng/multiselect';
import { SelectButtonModule } from 'primeng/selectbutton';
import { DropdownModule } from 'primeng/dropdown';
import { ProjectFilterComponent } from './project-filter/project-filter.component';
import { NamePipePipe } from './name-pipe.pipe';
import { PageLoaderComponent } from './page-loader/page-loader.component';
import { FooterComponent } from './footer/footer.component';
import { KpiFilterComponent } from './kpi-filter/kpi-filter.component';
import { FieldMappingFormComponent } from './field-mapping-form/field-mapping-form.component';
import { DialogModule } from 'primeng/dialog';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService } from 'primeng/api';
import { FieldMappingFieldComponent } from './field-mapping-field/field-mapping-field.component';
import { AccordionModule } from 'primeng/accordion';
import { TooltipModule } from 'primeng/tooltip';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { ChipsModule } from 'primeng/chips';
import { RadioButtonModule } from 'primeng/radiobutton';
import { AdditionalFilterFieldComponent } from './additional-filter-field/additional-filter-field.component';
import { InputSwitchModule } from 'primeng/inputswitch';
import { CarouselModule } from 'primeng/carousel';
import { FeatureFlagDirective } from './custom-directives/feature-flag.directive';
import { ConditionalInputComponent } from './conditional-input/conditional-input.component';
import { InputNumberModule } from 'primeng/inputnumber';
import { OverlayPanelModule } from 'primeng/overlaypanel';
import { IsoDateFormatPipe } from './pipes/iso-date-format.pipe';

import { StackedBarChartComponent } from 'src/app/component/stacked-bar-chart/stacked-bar-chart.component';
import { PsKpiCardHeaderComponent } from 'src/app/component/kpi-card-v3/ps-kpi-card-header/ps-kpi-card-header.component';
import { PsKpiCardFilterComponent } from 'src/app/component/kpi-card-v3/ps-kpi-card-filter/ps-kpi-card-filter.component';
import { PsKpiCardChartRendererComponent } from 'src/app/component/kpi-card-v3/ps-kpi-card-chart-renderer/ps-kpi-card-chart-renderer.component';
import { StackedBarComponent } from 'src/app/component/stacked-bar/stacked-bar.component';
import { SemiCircleDonutChartComponent } from 'src/app/component/semi-circle-donut-chart/semi-circle-donut-chart.component';
import { TabularKpiV2Component } from 'src/app/component/tabular-kpi-v2/tabular-kpi-v2.component';
import { GroupedBarChartComponent } from 'src/app/component/grouped-bar-chart/grouped-bar-chart.component';
import { TabularKpiWithDonutChartComponent } from 'src/app/component/tabular-kpi-with-donut-chart/tabular-kpi-with-donut-chart.component';
import { BarchartComponent } from 'src/app/component/barchart/barchart.component';
import { KpiHelperService } from '../services/kpi-helper.service';
import { TabMenuModule } from 'primeng/tabmenu';
import { MenuModule } from 'primeng/menu';
import { TableModule } from 'primeng/table';
import { ChartWithFiltersComponent } from '../component/chart-with-filters/chart-with-filters.component';
import { KpiAdditionalFilterComponent } from '../component/kpi-additional-filter/kpi-additional-filter.component';
import { MultilineComponent } from '../component/multiline/multiline.component';
import { GroupedColumnPlusLineChartV2Component } from '../component/grouped-column-plus-line-chart-v2/grouped-column-plus-line-chart-v2.component';
import { HorizontalPercentBarChartv2Component } from '../component/horizontal-percent-bar-chartv2/horizontal-percent-bar-chartv2.component';
import { MultilineStyleV2Component } from '../component/multiline-style-v2/multiline-style-v2.component';
import { MultilineV2Component } from '../component/multiline-v2/multiline-v2.component';
import { GroupstackchartComponentv2 } from '../component/groupedstackchart-v2/groupstackchart-v2.component';
import { PiechartComponent } from '../component/piechart/piechart.component';
import { StackedAreaChartComponent } from '../component/stacked-area-chart/stacked-area-chart.component';
import { TooltipV2Component } from '../component/tooltip-v2/tooltip-v2.component';
import { TrendIndicatorV2Component } from '../dashboardv2/trend-indicator-v2/trend-indicator-v2.component';
import { BarWithYAxisGroupComponent } from '../component/bar-with-y-axis-group/bar-with-y-axis-group.component';
import { GroupBarChartComponent } from '../component/group-bar-chart/group-bar-chart.component';
import { HeaderComponent } from '../dashboardv2/header-v2/header.component';

import { RecentCommentsComponent } from '../component/recent-comments/recent-comments.component';
import { CumulativeLineChartComponent } from '../component/cumulative-line-chart/cumulative-line-chart.component';
import { ReportKpiCardComponent } from '../dashboardv2/reports-module/report-kpi-card/report-kpi-card.component';
import { CollapsiblePanelComponent } from '../component/collapsible-panel/collapsible-panel.component';
import { TableComponent } from '../component/table/table.component';
import { UtcToUserLocalPipe } from './pipes/utc-to-user-local.pipe';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    CalendarModule,
    NgSelectModule,
    MultiSelectModule,
    DropdownModule,
    DialogModule,
    ConfirmDialogModule,
    AccordionModule,
    ReactiveFormsModule,
    ToastModule,
    TooltipModule,
    InputTextModule,
    ButtonModule,
    ChipsModule,
    RadioButtonModule,
    InputSwitchModule,
    CarouselModule,
    InputNumberModule,
    OverlayPanelModule,
    SelectButtonModule,
    MenuModule,
    TabMenuModule,
    TableModule,
    ToastModule,
  ],
  exports: [
    NgSelectModule,
    ProjectFilterComponent,
    NamePipePipe,
    PageLoaderComponent,
    CalendarModule,
    FooterComponent,
    KpiFilterComponent,
    FieldMappingFormComponent,
    FeatureFlagDirective,
    CarouselModule,
    ConditionalInputComponent,
    IsoDateFormatPipe,
    StackedBarChartComponent,
    PsKpiCardHeaderComponent,
    PsKpiCardFilterComponent,
    PsKpiCardChartRendererComponent,
    StackedBarComponent,
    SemiCircleDonutChartComponent,
    TabularKpiV2Component,
    GroupedBarChartComponent,
    TabularKpiWithDonutChartComponent,
    BarchartComponent,
    MultilineComponent,
    MultilineV2Component,
    GroupstackchartComponentv2,
    GroupBarChartComponent,
    ToastModule,
    StackedAreaChartComponent,
    PiechartComponent,
    TrendIndicatorV2Component,
    GroupedColumnPlusLineChartV2Component,
    MultilineStyleV2Component,
    TooltipV2Component,
    HorizontalPercentBarChartv2Component,
    ChartWithFiltersComponent,
    HeaderComponent,
    RecentCommentsComponent,
    CumulativeLineChartComponent,
    ReportKpiCardComponent,
    BarWithYAxisGroupComponent,
    CollapsiblePanelComponent,
    TableComponent,
    UtcToUserLocalPipe
  ],
  declarations: [
    ProjectFilterComponent,
    NamePipePipe,
    PageLoaderComponent,
    FooterComponent,
    KpiFilterComponent,
    FieldMappingFormComponent,
    FieldMappingFieldComponent,
    AdditionalFilterFieldComponent,
    FeatureFlagDirective,
    ConditionalInputComponent,
    IsoDateFormatPipe,
    StackedBarChartComponent,
    PsKpiCardHeaderComponent,
    PsKpiCardFilterComponent,
    PsKpiCardChartRendererComponent,
    StackedBarComponent,
    SemiCircleDonutChartComponent,
    TabularKpiV2Component,
    GroupedBarChartComponent,
    TabularKpiWithDonutChartComponent,
    BarchartComponent,
    ChartWithFiltersComponent,
    KpiAdditionalFilterComponent,
    MultilineComponent,
    MultilineV2Component,
    StackedAreaChartComponent,
    GroupstackchartComponentv2,
    GroupBarChartComponent,
    PiechartComponent,
    TrendIndicatorV2Component,
    GroupedColumnPlusLineChartV2Component,
    MultilineStyleV2Component,
    TooltipV2Component,
    HorizontalPercentBarChartv2Component,
    HeaderComponent,
    RecentCommentsComponent,
    CumulativeLineChartComponent,
    ReportKpiCardComponent,
    BarWithYAxisGroupComponent,
    CollapsiblePanelComponent,
    TableComponent,
    UtcToUserLocalPipe,
  ],
  providers: [ConfirmationService, KpiHelperService],
})
export class SharedModuleModule {}
