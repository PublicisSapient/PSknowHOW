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
import { ConditionalInputComponent } from './component/conditional-input/conditional-input.component';
import { FeatureFlagDirective } from './directives/custom-directives/feature-flag.directive';
import { FieldMappingFormComponent } from './component/field-mapping-form/field-mapping-form.component';
import { KpiFilterComponent } from './component/kpi-filter/kpi-filter.component';
import { FooterComponent } from './component/footer/footer.component';
import { PageLoaderComponent } from './component/page-loader/page-loader.component';
import { NamePipePipe } from './pipes/name-pipe.pipe';
import { ProjectFilterComponent } from './component/project-filter/project-filter.component';
import { AdditionalFilterFieldComponent } from './component/additional-filter-field/additional-filter-field.component';
import { FieldMappingFieldComponent } from './component/field-mapping-field/field-mapping-field.component';
import { NgPrimeModuleModule } from './module/ng-Prime-module.module';
import { CompareStartEndWithCurrentDatePipe } from './pipes/compareStartEndWithCurrentDate';
import { TypeofPipe } from './pipes/type-of.pipe';
import { ScrumKanbanPipe } from './pipes/scrumKanbanPipe';
import { AutoCompleteComponent } from './component/auto-complete/auto-complete.component';
import { BarWithYAxisGroupComponent } from './component/bar-with-y-axis-group/bar-with-y-axis-group.component';
import { BarchartComponent } from './component/barchart/barchart.component';
import { ChartWithFiltersComponent } from './component/chart-with-filters/chart-with-filters.component';
import { CircularProgressComponent } from './component/circular-progress/circular-progress.component';
import { CircularProgressWithLegendsComponent } from './component/circular-progress-with-legends/circular-progress-with-legends.component';
import { CircularchartComponent } from './component/circularchart/circularchart.component';
import { CommentsComponent } from './component/comments/comments.component';
import { CommentsV2Component } from './component/comments-v2/comments-v2.component';
import { CumulativeLineChartComponent } from './component/cumulative-line-chart/cumulative-line-chart.component';
import { DailyScrumGraphComponent } from './component/daily-scrum-graph/daily-scrum-graph.component';
import { GaugechartComponent } from './component/gaugechart/gaugechart.component';
import { ExportExcelComponent } from './component/export-excel/export-excel.component';
import { GroupBarChartComponent } from './component/group-bar-chart/group-bar-chart.component';
import { GroupedColumnPlusLineChartComponent } from './component/grouped-column-plus-line-chart/grouped-column-plus-line-chart.component';
import { GroupedColumnPlusLineChartV2Component } from './component/grouped-column-plus-line-chart-v2/grouped-column-plus-line-chart-v2.component';
import { GroupstackchartComponent } from './component/groupedstackchart/groupstackchart.component';
import { HorizontalPercentBarChartComponent } from './component/horizontal-percent-bar-chart/horizontal-percent-bar-chart.component';
import { HorizontalPercentBarChartv2Component } from './component/horizontal-percent-bar-chartv2/horizontal-percent-bar-chartv2.component';
import { HorizontalStackProgressbarComponent } from './component/horizontal-stack-progressbar/horizontal-stack-progressbar.component';
import { KpiAdditionalFilterComponent } from './component/kpi-additional-filter/kpi-additional-filter.component';
import { LineBarChartComponent } from './component/line-bar-chart/line-bar-chart.component';
import { LineBarChartWithHowerComponent } from './component/line-bar-chart-with-hover/line-bar-chart-with-hover.component';
import { MultilineComponent } from './component/multiline/multiline.component';
import { MultilineStyleComponent } from './component/multiline-style/multiline-style.component';
import { MultilineStyleV2Component } from './component/multiline-style-v2/multiline-style-v2.component';
import { MultilineV2Component } from './component/multiline-v2/multiline-v2.component';
import { NoAccessComponent } from './component/no-access/no-access.component';
import { NumberchartComponent } from './component/numberchart/numberchart.component';
import { OverlappedProgressbarComponent } from './component/overlapped-progressbar/overlapped-progressbar.component';
import { PiechartComponent } from './component/piechart/piechart.component';
import { ProgressbarComponent } from './component/progressbar/progressbar.component';
import { RecentCommentsComponent } from './component/recent-comments/recent-comments.component';
import { RecommendationsComponent } from './component/recommendations/recommendations.component';
import { SsoAuthFailureComponent } from './component/sso-auth-failure/sso-auth-failure.component';
import { StackedAreaChartComponent } from './component/stacked-area-chart/stacked-area-chart.component';
import { TableComponent } from './component/table/table.component';
import { TooltipComponent } from './component/tooltip/tooltip.component';
import { TooltipV2Component } from './component/tooltip-v2/tooltip-v2.component';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    NgSelectModule,
    ReactiveFormsModule,
    NgPrimeModuleModule
  ],
  exports: [
    NgSelectModule,

    ProjectFilterComponent,
    ChartWithFiltersComponent,
    PageLoaderComponent,
    FooterComponent,
    KpiFilterComponent,
    FieldMappingFormComponent,
    FieldMappingFieldComponent,
    AdditionalFilterFieldComponent,
    FeatureFlagDirective,
    ConditionalInputComponent,
    AutoCompleteComponent,
    BarWithYAxisGroupComponent,
    BarchartComponent,
    CircularProgressComponent,
    CircularchartComponent,
    CommentsComponent,
    CommentsV2Component,
    CumulativeLineChartComponent,
    DailyScrumGraphComponent,
    ExportExcelComponent,
    GaugechartComponent,
    GroupBarChartComponent,
    GroupedColumnPlusLineChartComponent,
    GroupedColumnPlusLineChartV2Component,
    GroupstackchartComponent,
    HorizontalPercentBarChartComponent,
    HorizontalPercentBarChartv2Component,
    HorizontalStackProgressbarComponent,
    KpiAdditionalFilterComponent,
    LineBarChartComponent,
    LineBarChartWithHowerComponent,
    MultilineComponent,
    MultilineStyleComponent,
    MultilineStyleV2Component,
    MultilineV2Component,
    NoAccessComponent,
    NumberchartComponent,
    OverlappedProgressbarComponent,
    PiechartComponent,
    ProgressbarComponent,
    RecentCommentsComponent,
    RecommendationsComponent,
    SsoAuthFailureComponent,
    StackedAreaChartComponent,
    TableComponent,
    TooltipComponent,
    TooltipV2Component,
    CircularProgressWithLegendsComponent,

    TypeofPipe,
    CompareStartEndWithCurrentDatePipe,
    NamePipePipe,
    ScrumKanbanPipe
  ],
  declarations: [
    ProjectFilterComponent,
    ChartWithFiltersComponent,
    PageLoaderComponent,
    FooterComponent,
    KpiFilterComponent,
    FieldMappingFormComponent,
    FieldMappingFieldComponent,
    AdditionalFilterFieldComponent,
    FeatureFlagDirective,
    ConditionalInputComponent,
    AutoCompleteComponent,
    BarWithYAxisGroupComponent,
    BarchartComponent,
    CircularProgressComponent,
    CircularchartComponent,
    CommentsComponent,
    CommentsV2Component,
    CumulativeLineChartComponent,
    DailyScrumGraphComponent,
    ExportExcelComponent,
    GaugechartComponent,
    GroupBarChartComponent,
    GroupedColumnPlusLineChartComponent,
    GroupedColumnPlusLineChartV2Component,
    GroupstackchartComponent,
    HorizontalPercentBarChartComponent,
    HorizontalPercentBarChartv2Component,
    HorizontalStackProgressbarComponent,
    KpiAdditionalFilterComponent,
    LineBarChartComponent,
    LineBarChartWithHowerComponent,
    MultilineComponent,
    MultilineStyleComponent,
    MultilineStyleV2Component,
    MultilineV2Component,
    NoAccessComponent,
    NumberchartComponent,
    OverlappedProgressbarComponent,
    PiechartComponent,
    ProgressbarComponent,
    RecentCommentsComponent,
    RecommendationsComponent,
    SsoAuthFailureComponent,
    StackedAreaChartComponent,
    TableComponent,
    TooltipComponent,
    TooltipV2Component,
    CircularProgressWithLegendsComponent,

    TypeofPipe,
    CompareStartEndWithCurrentDatePipe,
    NamePipePipe,
    ScrumKanbanPipe
  ],
  providers: []
})
export class SharedModuleModule { }
