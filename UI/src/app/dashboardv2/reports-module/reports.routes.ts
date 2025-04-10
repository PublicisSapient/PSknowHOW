import { RouterModule, Routes } from '@angular/router';
import { ReportContainerComponent } from './report-container/report-container.component';
import { NgModule } from '@angular/core';

export const reportsRoutes: Routes = [
  {
    path: 'Report',
    component: ReportContainerComponent,
  },
  {
    path: 'default-report',
    component: ReportContainerComponent,
  },
];

@NgModule({
  imports: [RouterModule.forChild(reportsRoutes)],
  exports: [],
  providers: [],
})
export class ReportsRoutingModule {}
