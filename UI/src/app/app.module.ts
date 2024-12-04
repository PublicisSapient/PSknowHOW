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


/******************* Modules   ***********************/
import { APP_INITIALIZER, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { InterceptorModule } from './module/interceptor.module';
import { AppRoutingModule } from './module/app-routing.module';
import { CommonModule, DatePipe } from '@angular/common';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MultiSelectModule } from 'primeng/multiselect';
import { SelectButtonModule } from 'primeng/selectbutton';
import { DropdownModule } from 'primeng/dropdown';
import { RippleModule } from 'primeng/ripple';
import { ToastModule } from 'primeng/toast';
import { RadioButtonModule } from 'primeng/radiobutton';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { AccordionModule } from 'primeng/accordion';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { MenuModule } from 'primeng/menu';
import { CheckboxModule } from 'primeng/checkbox';
import { ScrollTopModule } from 'primeng/scrolltop';
/******************************************************/

/******************* components   ***********************/
import { AppComponent } from './app.component';
import { DashboardV2Component } from './dashboardv2/dashboard-v2/dashboard-v2.component';
import { HeaderComponent } from './dashboardv2/header-v2/header.component';
import { NavComponent } from './dashboard/nav/nav.component';
import { CircularProgressComponent } from './component/circular-progress/circular-progress.component';
import { ProgressbarComponent } from './component/progressbar/progressbar.component';
import { CircularchartComponent } from './component/circularchart/circularchart.component';
import { NumberchartComponent } from './component/numberchart/numberchart.component';
import { BarchartComponent } from './component/barchart/barchart.component';
import { LineBarChartComponent } from './component/line-bar-chart/line-bar-chart.component';
import { LineBarChartWithHowerComponent } from './component/line-bar-chart-with-hover/line-bar-chart-with-hover.component';
import { GaugechartComponent } from './component/gaugechart/gaugechart.component';
import { MultilineComponent } from './component/multiline/multiline.component';
import { ExecutiveComponent } from './dashboard/executive/executive.component';
import { MaturityComponent } from './dashboard/maturity/maturity.component';
import { FilterComponent } from './dashboard/filter/filter.component';
import { GroupstackchartComponent } from './component/groupedstackchart/groupstackchart.component';
import { GroupstackchartComponentv2 } from './component/groupedstackchart-v2/groupstackchart-v2.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { PiechartComponent } from './component/piechart/piechart.component';
import { ErrorComponent } from './dashboard/error/error.component';
import { KpiComponent } from './component/kpi-line-gauge/kpi-line-gauge.component';
import { SharedModuleModule } from '../app/shared-module/shared-module.module';
import { InputSwitchModule } from 'primeng/inputswitch';
import { BadgeModule } from 'primeng/badge';
import { TabViewModule } from 'primeng/tabview';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TabMenuModule } from 'primeng/tabmenu';
import { OverlayPanelModule } from 'primeng/overlaypanel';
import { SkeletonModule } from 'primeng/skeleton';
import { BlockUIModule } from 'primeng/blockui';
/******************************************************/



/******************* Services   ***********************/
import { ExcelService } from './services/excel.service';
import { SharedService } from './services/shared.service';
import { GetAuthService } from './services/getauth.service';
import { APP_CONFIG, AppConfig } from './services/app.config';

import { HelperService } from './services/helper.service';
import { GetAuthorizationService } from './services/get-authorization.service';
import { JsonExportImportService } from './services/json-export-import.service';
import { IterationComponent } from './dashboard/iteration/iteration.component';
import { OverlappedProgressbarComponent } from './component/overlapped-progressbar/overlapped-progressbar.component';
import { HorizontalStackProgressbarComponent } from './component/horizontal-stack-progressbar/horizontal-stack-progressbar.component';
import { CircularProgressWithLegendsComponent } from './component/circular-progress-with-legends/circular-progress-with-legends.component';
import { MessageService } from 'primeng/api';
import { DialogModule } from 'primeng/dialog';
import { DialogService } from 'primeng/dynamicdialog';
import { KpiCardComponent } from './dashboard/kpi-card/kpi-card.component';
import { TrendIndicatorComponent } from './dashboard/trend-indicator/trend-indicator.component';
import { NoAccessComponent } from './component/no-access/no-access.component';
import { TooltipComponent } from './component/tooltip/tooltip.component';
import { GroupedColumnPlusLineChartComponent } from './component/grouped-column-plus-line-chart/grouped-column-plus-line-chart.component';
import { BacklogComponent } from './dashboard/backlog/backlog.component';
import { TableComponent } from './component/table/table.component';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { ExportExcelComponent } from './component/export-excel/export-excel.component';
import { SsoAuthFailureComponent } from './component/sso-auth-failure/sso-auth-failure.component';
import { UnauthorisedAccessComponent } from './dashboard/unauthorised-access/unauthorised-access.component';
import { GroupBarChartComponent } from './component/group-bar-chart/group-bar-chart.component';
import { CommentsComponent } from './component/comments/comments.component';
import { CommentsV2Component } from './component/comments-v2/comments-v2.component';
import { MilestoneComponent } from './dashboard/milestone/milestone.component';
import { HorizontalPercentBarChartComponent } from './component/horizontal-percent-bar-chart/horizontal-percent-bar-chart.component';
import { CumulativeLineChartComponent } from './component/cumulative-line-chart/cumulative-line-chart.component';
import { InputTextModule } from 'primeng/inputtext';
import { StackedAreaChartComponent } from './component/stacked-area-chart/stacked-area-chart.component';
import { FeedbackComponent } from './feedback/feedback.component';
import { KpiTableComponent } from './dashboard/kpi-table/kpi-table.component';
import { DailyScrumComponent } from './dashboard/daily-scrum/daily-scrum.component';
import { DailyScrumTabComponent } from './dashboard/daily-scrum-tab/daily-scrum-tab.component';
import { AssigneeBoardComponent } from './dashboard/assignee-board/assignee-board.component';
import { IssueCardComponent } from './dashboard/issue-card/issue-card.component';
import { IssueBodyComponent } from './dashboard/issue-body/issue-body.component';
import { DailyScrumGraphComponent } from './component/daily-scrum-graph/daily-scrum-graph.component';
import { MultilineStyleComponent } from './component/multiline-style/multiline-style.component';
import { DoraComponent } from './dashboard/dora/dora.component';
import { DeveloperComponent } from './dashboard/developer/developer.component';
import { BarWithYAxisGroupComponent } from './component/bar-with-y-axis-group/bar-with-y-axis-group.component';
import { FeatureFlagsService } from './services/feature-toggle.service';
import { PageNotFoundComponent } from './page-not-found/page-not-found.component';
import { AppInitializerService } from './services/app-initializer.service';
import { AuthGuard } from './services/auth.guard';
import { RecentCommentsComponent } from './component/recent-comments/recent-comments.component';
import { NavNewComponent } from './dashboardv2/nav-v2/nav-new.component';
import { FilterNewComponent } from './dashboardv2/filter-v2/filter-new.component';
import { ParentFilterComponent } from './dashboardv2/filter-v2/parent-filter/parent-filter.component';
import { PrimaryFilterComponent } from './dashboardv2/filter-v2/primary-filter/primary-filter.component';
import { AdditionalFilterComponent } from './dashboardv2/filter-v2/additional-filter/additional-filter.component';
import { ExecutiveV2Component } from './dashboardv2/executive-v2/executive-v2.component';
import { KpiCardV2Component } from './dashboardv2/kpi-card-v2/kpi-card-v2.component';
import { MultilineV2Component } from './component/multiline-v2/multiline-v2.component';
import { TrendIndicatorV2Component } from './dashboardv2/trend-indicator-v2/trend-indicator-v2.component';
import { GroupedColumnPlusLineChartV2Component } from './component/grouped-column-plus-line-chart-v2/grouped-column-plus-line-chart-v2.component';
import { MultilineStyleV2Component } from './component/multiline-style-v2/multiline-style-v2.component';
import { TooltipV2Component } from './component/tooltip-v2/tooltip-v2.component';
import { HorizontalPercentBarChartv2Component } from './component/horizontal-percent-bar-chartv2/horizontal-percent-bar-chartv2.component';
import { RecommendationsComponent } from './component/recommendations/recommendations.component';
import { ChartWithFiltersComponent } from './component/chart-with-filters/chart-with-filters.component';
import { KpiAdditionalFilterComponent } from './component/kpi-additional-filter/kpi-additional-filter.component';
import { StickyHeaderV2Component } from './dashboardv2/sticky-header-v2/sticky-header-v2.component';
import { KpiCardV3Component } from './component/kpi-card-v3/kpi-card-v3.component';
import { StackedBarChartComponent } from './component/stacked-bar-chart/stacked-bar-chart.component';
import { PsKpiCardHeaderComponent } from './component/kpi-card-v3/ps-kpi-card-header/ps-kpi-card-header.component';
import { PsKpiCardFilterComponent } from './component/kpi-card-v3/ps-kpi-card-filter/ps-kpi-card-filter.component';
import { PsKpiCardChartRendererComponent } from './component/kpi-card-v3/ps-kpi-card-chart-renderer/ps-kpi-card-chart-renderer.component';
import { KpiHelperService } from './services/kpi-helper.service';
import { StackedBarComponent } from './component/stacked-bar/stacked-bar.component';
import { SemiCircleDonutChartComponent } from './component/semi-circle-donut-chart/semi-circle-donut-chart.component';


/******************************************************/
export function initializeApp(appInitializerService: AppInitializerService) {
    return (): Promise<any> => {
        return appInitializerService.checkFeatureFlag();
    }
}

@NgModule({
    declarations: [
        AppComponent,
        NavComponent,
        CircularProgressComponent,
        ProgressbarComponent,
        CircularchartComponent,
        NumberchartComponent,
        BarchartComponent,
        LineBarChartComponent,
        LineBarChartWithHowerComponent,
        GaugechartComponent,
        MultilineComponent,
        ExecutiveComponent,
        MaturityComponent,
        FilterComponent,
        DashboardComponent,
        GroupstackchartComponent,
        GroupstackchartComponentv2,
        PiechartComponent,
        KpiComponent,
        ErrorComponent,
        // FooterComponent,
        IterationComponent,
        OverlappedProgressbarComponent,
        HorizontalStackProgressbarComponent,
        CircularProgressWithLegendsComponent,
        KpiCardComponent,
        TrendIndicatorComponent,
        TooltipComponent,
        NoAccessComponent,
        GroupedColumnPlusLineChartComponent,
        BacklogComponent,
        TableComponent,
        ExportExcelComponent,
        CommentsComponent,
        CommentsV2Component,
        SsoAuthFailureComponent,
        UnauthorisedAccessComponent,
        GroupBarChartComponent,
        MilestoneComponent,
        HorizontalPercentBarChartComponent,
        CumulativeLineChartComponent,
        StackedAreaChartComponent,
        FeedbackComponent,
        KpiTableComponent,
        DailyScrumComponent,
        DailyScrumTabComponent,
        AssigneeBoardComponent,
        IssueCardComponent,
        IssueBodyComponent,
        DailyScrumGraphComponent,
        MultilineStyleComponent,
        DoraComponent,
        FeedbackComponent,
        DeveloperComponent,
        BarWithYAxisGroupComponent,
        DeveloperComponent,
        PageNotFoundComponent,
        HeaderComponent,
        FilterNewComponent,
        ParentFilterComponent,
        PrimaryFilterComponent,
        AdditionalFilterComponent,
        NavNewComponent,
        RecentCommentsComponent,
        ExecutiveV2Component,
        DashboardV2Component,
        KpiCardV2Component,
        MultilineV2Component,
        TrendIndicatorV2Component,
        GroupedColumnPlusLineChartV2Component,
        MultilineStyleV2Component,
        TooltipV2Component,
        HorizontalPercentBarChartv2Component,
        PageNotFoundComponent,
        RecommendationsComponent,
        ChartWithFiltersComponent,
        KpiAdditionalFilterComponent,
        StickyHeaderV2Component,
        KpiCardV3Component,
        StackedBarChartComponent,
        PsKpiCardHeaderComponent,
        PsKpiCardFilterComponent,
        PsKpiCardChartRendererComponent,
        StackedBarComponent,
        SemiCircleDonutChartComponent
    ],
    imports: [
        DropdownModule,
        BrowserModule,
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        HttpClientModule,
        InterceptorModule,
        AppRoutingModule,
        // NgSelectModule,
        MultiSelectModule,
        SelectButtonModule,
        BrowserAnimationsModule,
        InputSwitchModule,
        RippleModule,
        BadgeModule,
        TabViewModule,
        TableModule,
        ButtonModule,
        TabMenuModule,
        ToastModule,
        DialogModule,
        RadioButtonModule,
        InputTextareaModule,
        AccordionModule,
        DialogModule,
        FontAwesomeModule,
        DragDropModule,
        OverlayPanelModule,
        MenuModule,
        CheckboxModule,
        SkeletonModule,
        BlockUIModule,
        SharedModuleModule,
        InputTextModule,
        ScrollTopModule
    ],
    providers: [
        ExcelService,
        SharedService,
        GetAuthService,
        ExecutiveComponent,
        HelperService,
        GetAuthorizationService,
        JsonExportImportService,
        MessageService,
        DatePipe,
        FeatureFlagsService,
        AuthGuard,
        DialogService,
        KpiHelperService,
        { provide: APP_CONFIG, useValue: AppConfig },
        {
            provide: APP_INITIALIZER,
            useFactory: initializeApp,
            deps: [AppInitializerService],
            multi: true
        }
    ],
    bootstrap: [AppComponent]
})
export class AppModule { }
