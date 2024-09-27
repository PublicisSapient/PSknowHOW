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
import { InterceptorModule } from './shared/module/interceptor.module';
import { AppRoutingModule } from './shared/module/app-routing.module';
import { CommonModule, DatePipe } from '@angular/common';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

/******************************************************/

/******************* components   ***********************/
import { AppComponent } from './app.component';
import { SharedModuleModule } from './shared/module/shared-module.module';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';

/******************************************************/



/******************* Services   ***********************/
import { ExcelService } from './core/services/excel.service';
import { SharedService } from './core/services/shared.service';
import { GetAuthService } from './core/services/getauth.service';
import { APP_CONFIG, AppConfig } from './core/configs/app.config';

import { HelperService } from './core/services/helper.service';
import { GetAuthorizationService } from './core/services/get-authorization.service';
import { JsonExportImportService } from './core/services/json-export-import.service';

import { ExternalUrlDirective } from './shared/directives/external-url.directive';
import { FeatureFlagsService } from './core/services/feature-toggle.service';
import { PageNotFoundComponent } from './pages/page-not-found/page-not-found.component';
import { AppInitializerService } from './core/services/app-initializer.service';
import { AuthGuard } from './core/guards/auth.guard';
/************** New added */
import { CircularProgressComponent } from './shared/component/circular-progress/circular-progress.component';
import { NavComponent } from './features/dashboard/nav/nav.component';
import { ProgressbarComponent } from './shared/component/progressbar/progressbar.component';
import { CircularchartComponent } from './shared/component/circularchart/circularchart.component';
import { NumberchartComponent } from './shared/component/numberchart/numberchart.component';
import { BarchartComponent } from './shared/component/barchart/barchart.component';
import { LineBarChartComponent } from './shared/component/line-bar-chart/line-bar-chart.component';
import { LineBarChartWithHowerComponent } from './shared/component/line-bar-chart-with-hover/line-bar-chart-with-hover.component';
import { GaugechartComponent } from './shared/component/gaugechart/gaugechart.component';
import { MultilineComponent } from './shared/component/multiline/multiline.component';
import { ExecutiveComponent } from './features/dashboard/executive/executive.component';
import { MaturityComponent } from './features/dashboard/maturity/maturity.component';
import { FilterComponent } from './features/dashboard/filter/filter.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { GroupstackchartComponent } from './shared/component/groupedstackchart/groupstackchart.component';
import { PiechartComponent } from './shared/component/piechart/piechart.component';
import { KpiComponent } from './shared/component/kpi-line-gauge/kpi-line-gauge.component';
import { ErrorComponent } from './features/dashboard/error/error.component';
import { IterationComponent } from './features/dashboard/iteration/iteration.component';
import { OverlappedProgressbarComponent } from './shared/component/overlapped-progressbar/overlapped-progressbar.component';
import { HorizontalStackProgressbarComponent } from './shared/component/horizontal-stack-progressbar/horizontal-stack-progressbar.component';
import { CircularProgressWithLegendsComponent } from './shared/component/circular-progress-with-legends/circular-progress-with-legends.component';
import { KpiCardComponent } from './features/dashboard/kpi-card/kpi-card.component';
import { TrendIndicatorComponent } from './features/dashboard/trend-indicator/trend-indicator.component';
import { TooltipComponent } from './shared/component/tooltip/tooltip.component';
import { NoAccessComponent } from './shared/component/no-access/no-access.component';
import { StickyHeaderV2Component } from './features/dashboardv2/sticky-header-v2/sticky-header-v2.component';
import { KpiAdditionalFilterComponent } from './shared/component/kpi-additional-filter/kpi-additional-filter.component';
import { ChartWithFiltersComponent } from './shared/component/chart-with-filters/chart-with-filters.component';
import { RecommendationsComponent } from './shared/component/recommendations/recommendations.component';
import { HorizontalPercentBarChartv2Component } from './shared/component/horizontal-percent-bar-chartv2/horizontal-percent-bar-chartv2.component';
import { TooltipV2Component } from './shared/component/tooltip-v2/tooltip-v2.component';
import { MultilineStyleV2Component } from './shared/component/multiline-style-v2/multiline-style-v2.component';
import { GroupedColumnPlusLineChartV2Component } from './shared/component/grouped-column-plus-line-chart-v2/grouped-column-plus-line-chart-v2.component';
import { TrendIndicatorV2Component } from './features/dashboardv2/trend-indicator-v2/trend-indicator-v2.component';
import { MultilineV2Component } from './shared/component/multiline-v2/multiline-v2.component';
import { KpiCardV2Component } from './features/dashboardv2/kpi-card-v2/kpi-card-v2.component';
import { DashboardV2Component } from './features/dashboardv2/dashboard-v2/dashboard-v2.component';
import { ExecutiveV2Component } from './features/dashboardv2/executive-v2/executive-v2.component';
import { RecentCommentsComponent } from './shared/component/recent-comments/recent-comments.component';
import { NavNewComponent } from './features/dashboardv2/nav-v2/nav-new.component';
import { AdditionalFilterComponent } from './features/dashboardv2/filter-v2/additional-filter/additional-filter.component';
import { PrimaryFilterComponent } from './features/dashboardv2/filter-v2/primary-filter/primary-filter.component';
import { ParentFilterComponent } from './features/dashboardv2/filter-v2/parent-filter/parent-filter.component';
import { FilterNewComponent } from './features/dashboardv2/filter-v2/filter-new.component';
import { HeaderComponent } from './features/dashboardv2/header-v2/header.component';
import { DeveloperComponent } from './features/dashboard/developer/developer.component';
import { BarWithYAxisGroupComponent } from './shared/component/bar-with-y-axis-group/bar-with-y-axis-group.component';
import { FeedbackComponent } from './pages/feedback/feedback.component';
import { DoraComponent } from './features/dashboard/dora/dora.component';
import { MultilineStyleComponent } from './shared/component/multiline-style/multiline-style.component';
import { DailyScrumGraphComponent } from './shared/component/daily-scrum-graph/daily-scrum-graph.component';
import { IssueBodyComponent } from './features/dashboard/issue-body/issue-body.component';
import { IssueCardComponent } from './features/dashboard/issue-card/issue-card.component';
import { AssigneeBoardComponent } from './features/dashboard/assignee-board/assignee-board.component';
import { DailyScrumTabComponent } from './features/dashboard/daily-scrum-tab/daily-scrum-tab.component';
import { DailyScrumComponent } from './features/dashboard/daily-scrum/daily-scrum.component';
import { KpiTableComponent } from './features/dashboard/kpi-table/kpi-table.component';
import { StackedAreaChartComponent } from './shared/component/stacked-area-chart/stacked-area-chart.component';
import { CumulativeLineChartComponent } from './shared/component/cumulative-line-chart/cumulative-line-chart.component';
import { HorizontalPercentBarChartComponent } from './shared/component/horizontal-percent-bar-chart/horizontal-percent-bar-chart.component';
import { MilestoneComponent } from './features/dashboard/milestone/milestone.component';
import { GroupBarChartComponent } from './shared/component/group-bar-chart/group-bar-chart.component';
import { SsoAuthFailureComponent } from './shared/component/sso-auth-failure/sso-auth-failure.component';
import { UnauthorisedAccessComponent } from './features/dashboard/unauthorised-access/unauthorised-access.component';
import { CommentsV2Component } from './shared/component/comments-v2/comments-v2.component';
import { CommentsComponent } from './shared/component/comments/comments.component';
import { ExportExcelComponent } from './shared/component/export-excel/export-excel.component';
import { TableComponent } from './shared/component/table/table.component';
import { BacklogComponent } from './features/dashboard/backlog/backlog.component';
import { GroupedColumnPlusLineChartComponent } from './shared/component/grouped-column-plus-line-chart/grouped-column-plus-line-chart.component';
import { NgPrimeModuleModule } from './shared/module/ng-Prime-module.module';


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
        PiechartComponent,
        KpiComponent,
        ErrorComponent,
        // FooterComponent,
        IterationComponent,
        OverlappedProgressbarComponent,
        HorizontalStackProgressbarComponent,
        CircularProgressWithLegendsComponent,
        ExternalUrlDirective,
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
        StickyHeaderV2Component
    ],
    imports: [
        BrowserModule,
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        HttpClientModule,
        InterceptorModule,
        AppRoutingModule,
        // NgSelectModule,
        BrowserAnimationsModule,
        FontAwesomeModule,
        NgPrimeModuleModule,
        SharedModuleModule
    ],
    providers: [
        ExcelService,
        SharedService,
        GetAuthService,
        ExecutiveComponent,
        HelperService,
        GetAuthorizationService,
        JsonExportImportService,
        DatePipe,
        FeatureFlagsService,
        AuthGuard,
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
