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
import { CircularProgressComponent } from './component/circular-progress/circular-progress.component';
import { ProgressbarComponent } from './component/progressbar/progressbar.component';
import { CircularchartComponent } from './component/circularchart/circularchart.component';
import { NumberchartComponent } from './component/numberchart/numberchart.component';

import { LineBarChartComponent } from './component/line-bar-chart/line-bar-chart.component';
import { LineBarChartWithHowerComponent } from './component/line-bar-chart-with-hover/line-bar-chart-with-hover.component';
import { GaugechartComponent } from './component/gaugechart/gaugechart.component';

import { MaturityComponent } from './dashboard/maturity/maturity.component';
import { GroupstackchartComponent } from './component/groupedstackchart/groupstackchart.component';


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
import { OverlappedProgressbarComponent } from './component/overlapped-progressbar/overlapped-progressbar.component';
import { HorizontalStackProgressbarComponent } from './component/horizontal-stack-progressbar/horizontal-stack-progressbar.component';
import { CircularProgressWithLegendsComponent } from './component/circular-progress-with-legends/circular-progress-with-legends.component';
import { MessageService } from 'primeng/api';
import { DialogModule } from 'primeng/dialog';
import { DialogService } from 'primeng/dynamicdialog';
import { TrendIndicatorComponent } from './dashboard/trend-indicator/trend-indicator.component';
import { NoAccessComponent } from './component/no-access/no-access.component';
import { TooltipComponent } from './component/tooltip/tooltip.component';
import { GroupedColumnPlusLineChartComponent } from './component/grouped-column-plus-line-chart/grouped-column-plus-line-chart.component';
import { TableComponent } from './component/table/table.component';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { ExportExcelComponent } from './component/export-excel/export-excel.component';
import { SsoAuthFailureComponent } from './component/sso-auth-failure/sso-auth-failure.component';
import { UnauthorisedAccessComponent } from './dashboard/unauthorised-access/unauthorised-access.component';

import { CommentsComponent } from './component/comments/comments.component';
import { CommentsV2Component } from './component/comments-v2/comments-v2.component';
import { HorizontalPercentBarChartComponent } from './component/horizontal-percent-bar-chart/horizontal-percent-bar-chart.component';
import { InputTextModule } from 'primeng/inputtext';

import { FeedbackComponent } from './feedback/feedback.component';
import { KpiTableComponent } from './dashboard/kpi-table/kpi-table.component';
import { DailyScrumComponent } from './dashboard/daily-scrum/daily-scrum.component';
import { DailyScrumTabComponent } from './dashboard/daily-scrum-tab/daily-scrum-tab.component';
import { AssigneeBoardComponent } from './dashboard/assignee-board/assignee-board.component';
import { IssueCardComponent } from './dashboard/issue-card/issue-card.component';
import { IssueBodyComponent } from './dashboard/issue-body/issue-body.component';
import { DailyScrumGraphComponent } from './component/daily-scrum-graph/daily-scrum-graph.component';
import { MultilineStyleComponent } from './component/multiline-style/multiline-style.component';

import { FeatureFlagsService } from './services/feature-toggle.service';
import { PageNotFoundComponent } from './page-not-found/page-not-found.component';
import { AppInitializerService } from './services/app-initializer.service';
import { AuthGuard } from './services/auth.guard';
import { NavNewComponent } from './dashboardv2/nav-v2/nav-new.component';
import { FilterNewComponent } from './dashboardv2/filter-v2/filter-new.component';
import { ParentFilterComponent } from './dashboardv2/filter-v2/parent-filter/parent-filter.component';
import { PrimaryFilterComponent } from './dashboardv2/filter-v2/primary-filter/primary-filter.component';
import { AdditionalFilterComponent } from './dashboardv2/filter-v2/additional-filter/additional-filter.component';
import { ExecutiveV2Component } from './dashboardv2/executive-v2/executive-v2.component';
import { KpiCardV2Component } from './dashboardv2/kpi-card-v2/kpi-card-v2.component';

import { RecommendationsComponent } from './component/recommendations/recommendations.component';

import { StickyHeaderV2Component } from './dashboardv2/sticky-header-v2/sticky-header-v2.component';
import { KpiHelperService } from './services/kpi-helper.service';
import { SelectButtonModule } from 'primeng/selectbutton';
import { AddToReportPopUpComponent } from './component/add-to-report-pop-up/add-to-report-pop-up.component';
import { PanelModule } from 'primeng/panel';


/******************************************************/
export function initializeApp(appInitializerService: AppInitializerService) {
    return (): Promise<any> => {
        return appInitializerService.checkFeatureFlag();
    }
}

@NgModule({
    declarations: [
        AppComponent,
        CircularProgressComponent,
        ProgressbarComponent,
        CircularchartComponent,
        NumberchartComponent,
        LineBarChartComponent,
        LineBarChartWithHowerComponent,
        GaugechartComponent,
        
        MaturityComponent,
        GroupstackchartComponent,
        KpiComponent,
        ErrorComponent,
        // FooterComponent,
        OverlappedProgressbarComponent,
        HorizontalStackProgressbarComponent,
        CircularProgressWithLegendsComponent,
        TrendIndicatorComponent,
        TooltipComponent,
        NoAccessComponent,
        GroupedColumnPlusLineChartComponent,
        TableComponent,
        ExportExcelComponent,
        CommentsComponent,
        CommentsV2Component,
        SsoAuthFailureComponent,
        UnauthorisedAccessComponent,
        HorizontalPercentBarChartComponent,
        FeedbackComponent,
        KpiTableComponent,
        DailyScrumComponent,
        DailyScrumTabComponent,
        AssigneeBoardComponent,
        IssueCardComponent,
        IssueBodyComponent,
        DailyScrumGraphComponent,
        MultilineStyleComponent,
        FeedbackComponent,
        PageNotFoundComponent,
        FilterNewComponent,
        ParentFilterComponent,
        PrimaryFilterComponent,
        AdditionalFilterComponent,
        NavNewComponent,
        ExecutiveV2Component,
        DashboardV2Component,
        KpiCardV2Component,
        PageNotFoundComponent,
        RecommendationsComponent,
        StickyHeaderV2Component,
        AddToReportPopUpComponent,
    ],
    imports: [
        SharedModuleModule,
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
        BrowserAnimationsModule,
        InputSwitchModule,
        RippleModule,
        BadgeModule,
        TabViewModule,
        TableModule,
        ButtonModule,
        TabMenuModule,
        MenuModule,
        ToastModule,
        DialogModule,
        RadioButtonModule,
        InputTextareaModule,
        AccordionModule,
        DialogModule,
        FontAwesomeModule,
        DragDropModule,
        OverlayPanelModule,
        PanelModule,
        CheckboxModule,
        SkeletonModule,
        BlockUIModule,
        InputTextModule,
        ScrollTopModule,
        SelectButtonModule
    ],
    providers: [
        ExcelService,
        SharedService,
        GetAuthService,
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
