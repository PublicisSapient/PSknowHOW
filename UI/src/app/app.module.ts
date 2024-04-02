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
/******************************************************/

/******************* components   ***********************/
import { AppComponent } from './app.component';
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
import { ExternalUrlDirective } from './external-url.directive';
import { MessageService } from 'primeng/api';
import { DialogModule } from 'primeng/dialog';
import { KpiCardComponent } from './dashboard/kpi-card/kpi-card.component';
import { TrendIndicatorComponent } from './dashboard/trend-indicator/trend-indicator.component';
import { NoAccessComponent } from './component/no-access/no-access.component';
import { TooltipComponent } from './component/tooltip/tooltip.component';
import { GroupedColumnPlusLineChartComponent } from './component/grouped-column-plus-line-chart/grouped-column-plus-line-chart.component';
import { BacklogComponent } from './dashboard/backlog/backlog.component';
import { TableComponent } from './component/table/table.component';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { ExportExcelComponent } from './component/export-excel/export-excel.component';
import { environment } from 'src/environments/environment';
import { tap } from 'rxjs/operators';
import { SsoAuthFailureComponent } from './component/sso-auth-failure/sso-auth-failure.component';
import { UnauthorisedAccessComponent } from './dashboard/unauthorised-access/unauthorised-access.component';
import { GroupBarChartComponent } from './component/group-bar-chart/group-bar-chart.component';
import { CommentsComponent } from './component/comments/comments.component';
import { MilestoneComponent } from './dashboard/milestone/milestone.component';
import { HorizontalPercentBarChartComponent } from './component/horizontal-percent-bar-chart/horizontal-percent-bar-chart.component';
import { CumulativeLineChartComponent } from './component/cumulative-line-chart/cumulative-line-chart.component';

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
import { Router, Routes } from '@angular/router';
import { FeatureGuard } from './services/feature.guard';
import { AccessGuard } from './services/access.guard';
import { HttpService } from './services/http.service';
import { GoogleAnalyticsService } from './services/google-analytics.service';
import { AuthGuard } from './services/auth.guard';
import { Logged } from './services/logged.guard';
import { SSOGuard } from './services/sso.guard';

/******************************************************/

const   routes = [
    { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    {
      path: 'authentication',
      loadChildren: () => import('../app/authentication/authentication.module').then(m => m.AuthenticationModule),
      resolve: [Logged],
      canActivate: [SSOGuard]
    },
    {
      path: 'dashboard', component: DashboardComponent,
      canActivateChild: [FeatureGuard],
      children: [
        { path: '', redirectTo: 'iteration', pathMatch: 'full' },
        {
          path: 'mydashboard', component: IterationComponent, pathMatch: 'full', canActivate: [AccessGuard],
          data: {
            feature: "My Dashboard"
          }
        },
        {
          path: 'iteration', component: IterationComponent, pathMatch: 'full', canActivate: [AccessGuard],
          data: {
            feature: "Iteration"
          }
        },
        {
          path: 'developer', component: DeveloperComponent, pathMatch: 'full', canActivate: [AccessGuard],
          data: {
            feature: "Developer"
          }
        },
        {
          path: 'Maturity', component: MaturityComponent, pathMatch: 'full', canActivate: [AccessGuard],
          data: {
            feature: "Maturity"
          }
        },
        {
          path: 'backlog', component: BacklogComponent, pathMatch: 'full', canActivate: [AccessGuard],
          data: {
            feature: "Backlog"
          }
        },
        {
          path: 'release', component: MilestoneComponent, pathMatch: 'full', canActivate: [AccessGuard],
          data: {
            feature: "Release"
          }
        },
        {
          path: 'dora', component: DoraComponent, pathMatch: 'full', canActivate: [AccessGuard],
          data: {
            feature: "Dora"
          }
        },
        { path: 'Error', component: ErrorComponent, pathMatch: 'full' },
        { path: 'unauthorized-access', component: UnauthorisedAccessComponent, pathMatch: 'full' },
        {
          path: 'Config',
          loadChildren: () => import('../app/config/config.module').then(m => m.ConfigModule), canLoad: [FeatureGuard],
          data: {
            feature: "Config"
          }
        },
        { path: ':boardName', component: ExecutiveComponent, pathMatch: 'full' },

      ], canActivate: [AuthGuard]
    },
    { path: 'authentication-fail', component: SsoAuthFailureComponent },
    { path: '**', redirectTo: 'authentication' }
  ];



export function initializeApp(http: HttpService, featureToggleService: FeatureFlagsService,
    ga: GoogleAnalyticsService, sharedService: SharedService) {
    return (): Promise<any> => {
        return checkFeatureFlag(http, featureToggleService, ga, sharedService);
    }
}

export function checkFeatureFlag(http, featureToggleService, ga, sharedService) {
    console.log('Inside CheckFeatureFlag');
    return new Promise<void>((resolve, reject) => {
        if (!environment['production']) {
            // new FeatureFlagsService.config = this.featureToggleService.loadConfig().then((res) => res);
            validateToken(http, featureToggleService, ga, sharedService);
        } else {
            const env$ = http.http.get('assets/env.json').pipe(
                tap(env => {
                    console.log("env inside app initializer", env['AUTHENTICATION_SERVICE']);

                    environment['baseUrl'] = env['baseUrl'] || '';
                    environment['SSO_LOGIN'] = env['SSO_LOGIN'] || false;
                    environment['AUTHENTICATION_SERVICE'] = env['AUTHENTICATION_SERVICE'];
                    environment['CENTRAL_LOGIN_URL'] = env['CENTRAL_LOGIN_URL'] || '';
                    environment['MAP_URL'] = env['MAP_URL'] || '';
                    environment['RETROS_URL'] = env['RETROS_URL'] || '';
                    if (environment['AUTHENTICATION_SERVICE'] != true) {
                        this.router.resetConfig([...routes]);
                        validateToken(http, featureToggleService, ga, sharedService);
                    }
                }));
            env$.toPromise().then(async res => {
                featureToggleService.config = featureToggleService.loadConfig().then((res) => res);
            });
        }



        // load google Analytics script on all instances except local and if customAPI property is true
        let addGAScript = featureToggleService.isFeatureEnabled('GOOGLE_ANALYTICS');
        if (addGAScript) {
            if (window.location.origin.indexOf('localhost') === -1) {
                ga.load('gaTagManager').then(data => {
                    console.log('script loaded ', data);
                })
            }
        }
        resolve();
    })
}

export function validateToken(http, featureToggleService, ga, sharedService) {
    return new Promise<void>((resolve, reject) => {
        if (environment['AUTHENTICATION_SERVICE'] == true) {

            let url = window.location.href;
            let authToken = url.split("authToken=")?.[1]?.split("&")?.[0];
            if (authToken) {
                sharedService.setAuthToken(authToken);
            }
            let obj = {
                'resource': environment.RESOURCE,
                'authToken': authToken
            };

            http.router.navigateByUrl('dashboard');
            // Make API call or initialization logic here...
            http.getUserValidation(obj).subscribe((response) => {
                if (response?.['success']) {
                    sharedService.setCurrentUserDetails(response?.['data']);
                    localStorage.setItem("user_name", response?.['data']?.user_name);
                    localStorage.setItem("user_email", response?.['data']?.user_email);
                    if (authToken) {
                        ga.setLoginMethod(response?.['data'], response?.['data']?.authType);
                    }
                }
            }, error => {
                console.log(error);
            })
        } else {
            http.router.navigateByUrl('dashboard');
        }
        resolve();

    })

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
        PageNotFoundComponent
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
        BrowserAnimationsModule,
        InputSwitchModule,
        RippleModule,
        BadgeModule,
        TabViewModule,
        TableModule,
        ButtonModule,
        TabMenuModule,
        ToastModule,
        RadioButtonModule,
        InputTextareaModule,
        AccordionModule,
        DialogModule,
        FontAwesomeModule,
        DragDropModule,
        OverlayPanelModule,
        MenuModule,
        SkeletonModule,
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
        MessageService,
        DatePipe,
        FeatureFlagsService,
        { provide: APP_CONFIG, useValue: AppConfig },
        {
            provide: APP_INITIALIZER,
            useFactory: initializeApp,
            deps: [HttpService, FeatureFlagsService,
                GoogleAnalyticsService, SharedService],
            multi: true
        }
    ],
    bootstrap: [AppComponent]
})
export class AppModule { }
