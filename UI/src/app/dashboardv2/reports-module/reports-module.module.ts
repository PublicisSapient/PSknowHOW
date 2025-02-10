import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReportContainerComponent } from './report-container/report-container.component';
import { ReportsRoutingModule } from './reports.routes';
import { ReportKpiCardComponent } from './report-kpi-card/report-kpi-card.component';

import { KpiHelperService } from 'src/app/services/kpi-helper.service';

import { AppModule } from 'src/app/app.module';
import { SharedModuleModule } from 'src/app/shared-module/shared-module.module';



@NgModule({
  declarations: [
    ReportContainerComponent,
    ReportKpiCardComponent,
    // StackedBarChartComponent,
    // BarchartComponent,
    // StackedBarComponent,
    // SemiCircleDonutChartComponent,
    // TabularKpiV2Component,
    // GroupedBarChartComponent,
    // TabularKpiWithDonutChartComponent
  ],
  imports: [
    CommonModule, ReportsRoutingModule, SharedModuleModule],
  providers: [KpiHelperService],
})
export class ReportsModuleModule { }
