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
import { CommonModule, DatePipe } from '@angular/common';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
/******************************************************/

/******************* components   ***********************/
import { AppComponent } from './app.component';
import { DashboardV2Component } from './features/dashboardv2/dashboard-v2/dashboard-v2.component';
import { HeaderComponent } from './features/dashboardv2/header-v2/header.component';
import { NavComponent } from './features/dashboard/nav/nav.component';
import { ExecutiveComponent } from './features/dashboard/executive/executive.component';
import { MaturityComponent } from './features/dashboard/maturity/maturity.component';
import { FilterComponent } from './features/dashboard/filter/filter.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { ErrorComponent } from './features/dashboard/error/error.component';
/******************************************************/



/******************* Services   ***********************/
import { APP_CONFIG, AppConfig } from './core/configs/app.config';

import { IterationComponent } from './features/dashboard/iteration/iteration.component';
import { ExternalUrlDirective } from './shared/directives/external-url.directive';
import { KpiCardComponent } from './features/dashboard/kpi-card/kpi-card.component';
import { TrendIndicatorComponent } from './features/dashboard/trend-indicator/trend-indicator.component';
import { BacklogComponent } from './features/dashboard/backlog/backlog.component';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { UnauthorisedAccessComponent } from './features/dashboard/unauthorised-access/unauthorised-access.component';
import { MilestoneComponent } from './features/dashboard/milestone/milestone.component';
import { FeedbackComponent } from './pages/feedback/feedback.component';
import { KpiTableComponent } from './features/dashboard/kpi-table/kpi-table.component';
import { DailyScrumComponent } from './features/dashboard/daily-scrum/daily-scrum.component';
import { DailyScrumTabComponent } from './features/dashboard/daily-scrum-tab/daily-scrum-tab.component';
import { AssigneeBoardComponent } from './features/dashboard/assignee-board/assignee-board.component';
import { IssueCardComponent } from './features/dashboard/issue-card/issue-card.component';
import { IssueBodyComponent } from './features/dashboard/issue-body/issue-body.component';
import { DoraComponent } from './features/dashboard/dora/dora.component';
import { DeveloperComponent } from './features/dashboard/developer/developer.component';
import { PageNotFoundComponent } from './pages/page-not-found/page-not-found.component';
import { AuthGuard } from './core/guards/auth.guard';
// import { RecentCommentsComponent } from './shared/component/recent-comments/recent-comments.component';
import { NavNewComponent } from './features/dashboardv2/nav-v2/nav-new.component';
import { FilterNewComponent } from './features/dashboardv2/filter-v2/filter-new.component';
import { ParentFilterComponent } from './features/dashboardv2/filter-v2/parent-filter/parent-filter.component';
import { PrimaryFilterComponent } from './features/dashboardv2/filter-v2/primary-filter/primary-filter.component';
import { AdditionalFilterComponent } from './features/dashboardv2/filter-v2/additional-filter/additional-filter.component';
import { ExecutiveV2Component } from './features/dashboardv2/executive-v2/executive-v2.component';
import { KpiCardV2Component } from './features/dashboardv2/kpi-card-v2/kpi-card-v2.component';
import { TrendIndicatorV2Component } from './features/dashboardv2/trend-indicator-v2/trend-indicator-v2.component';
import { StickyHeaderV2Component } from './features/dashboardv2/sticky-header-v2/sticky-header-v2.component';
import { AppInitializerService } from './core/services/app-initializer.service';
import { ExcelService } from './core/services/excel.service';
import { SharedService } from './core/services/shared.service';
import { GetAuthService } from './core/services/getauth.service';
import { HelperService } from './core/services/helper.service';
import { GetAuthorizationService } from './core/services/get-authorization.service';
import { JsonExportImportService } from './core/services/json-export-import.service';
import { FeatureFlagsService } from './core/services/feature-toggle.service';
import { SharedModuleModule } from './shared/shared-module.module';
import { AppRoutingModule } from './shared/module/app-routing.module';
import { InterceptorModule } from './core/interceptors/interceptor.module';
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
        ExecutiveComponent,
        MaturityComponent,
        FilterComponent,
        DashboardComponent,
        ErrorComponent,
        IterationComponent,
        ExternalUrlDirective,
        KpiCardComponent,
        TrendIndicatorComponent,
        BacklogComponent,
        UnauthorisedAccessComponent,
        MilestoneComponent,
        FeedbackComponent,
        KpiTableComponent,
        DailyScrumComponent,
        DailyScrumTabComponent,
        AssigneeBoardComponent,
        IssueCardComponent,
        IssueBodyComponent,
        DoraComponent,
        FeedbackComponent,
        DeveloperComponent,
        DeveloperComponent,
        PageNotFoundComponent,
        HeaderComponent,
        FilterNewComponent,
        ParentFilterComponent,
        PrimaryFilterComponent,
        AdditionalFilterComponent,
        NavNewComponent,
        ExecutiveV2Component,
        DashboardV2Component,
        KpiCardV2Component,
        TrendIndicatorV2Component,
        PageNotFoundComponent,
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
        BrowserAnimationsModule,
        FontAwesomeModule,
        DragDropModule,
        SharedModuleModule,
        NgPrimeModuleModule
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
