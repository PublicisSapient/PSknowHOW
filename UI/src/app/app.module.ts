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
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { InterceptorModule } from './module/interceptor.module';
import { AppRoutingModule } from './module/app-routing.module';
import { CommonModule } from '@angular/common';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MultiSelectModule } from 'primeng/multiselect';
import { DropdownModule } from 'primeng/dropdown';
import { RippleModule } from 'primeng/ripple';
import { ToastModule } from 'primeng/toast';
import {RadioButtonModule} from 'primeng/radiobutton';
import {InputTextareaModule} from 'primeng/inputtextarea';
import {AccordionModule} from 'primeng/accordion';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { DatePipe } from '@angular/common';
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


/******************************************************/



/******************* Services   ***********************/
import { ExcelService } from './services/excel.service';
import { SharedService } from './services/shared.service';
import { GetAuthService } from './services/getauth.service';
import { APP_CONFIG, AppConfig } from './services/app.config';

import { HelperService } from './services/helper.service';
import { GetAuthorizationService } from './services/get-authorization.service';
import { JsonExportImportService } from './services/json-export-import.service';
import { RsaEncryptionService } from './services/rsa.encryption.service';
import { TextEncryptionService } from './services/text.encryption.service';
import { IterationComponent } from './dashboard/iteration/iteration.component';
import { OverlappedProgressbarComponent } from './component/overlapped-progressbar/overlapped-progressbar.component';
import { HorizontalStackProgressbarComponent } from './component/horizontal-stack-progressbar/horizontal-stack-progressbar.component';
import { CircularProgressWithLegendsComponent } from './component/circular-progress-with-legends/circular-progress-with-legends.component';
import { ExternalUrlDirective } from './external-url.directive';
import { MessageService } from 'primeng/api';
import { LandingPageComponent } from './dashboard/landing-page/landing-page.component';
import { DialogModule } from 'primeng/dialog';
import { KpiCardComponent } from './dashboard/kpi-card/kpi-card.component';
import { TrendIndicatorComponent } from './dashboard/trend-indicator/trend-indicator.component';
import { NoAccessComponent } from './component/no-access/no-access.component';
import { TooltipComponent } from './component/tooltip/tooltip.component';
import { GroupedColumnPlusLineChartComponent } from './component/grouped-column-plus-line-chart/grouped-column-plus-line-chart.component';
import { BacklogComponent } from './dashboard/backlog/backlog.component';
import { TableComponent } from './component/table/table.component';
import {DragDropModule} from '@angular/cdk/drag-drop';

/******************************************************/


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
        LandingPageComponent,
        KpiCardComponent,
        TrendIndicatorComponent,
        TooltipComponent,
        NoAccessComponent,
        GroupedColumnPlusLineChartComponent,
        BacklogComponent,
        TableComponent
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
        SharedModuleModule,
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
        DragDropModule
    ],
    providers: [
        ExcelService,
        SharedService,
        GetAuthService,
        ExecutiveComponent,
        HelperService,
        GetAuthorizationService,
        JsonExportImportService,
        RsaEncryptionService,
        MessageService,
        TextEncryptionService,
        DatePipe,
        { provide: APP_CONFIG, useValue: AppConfig }
    ],
    bootstrap: [AppComponent]
})
export class AppModule { }




