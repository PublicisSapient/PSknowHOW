import { RouterModule, Routes } from '@angular/router';
import { DefaultReportComponent } from './default-report/default-report.component';
import { NgModule } from '@angular/core';

export const reportsRoutes: Routes = [
    {
        path: 'Report',
        component: DefaultReportComponent
    },
    {
        path: 'default-report',
        component: DefaultReportComponent
    }
];

@NgModule({
    imports: [RouterModule.forChild(reportsRoutes)],
    exports: [],
    providers: [
    ]
})

export class ReportsRoutingModule { }