import { TestBed, ComponentFixture } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
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
import { AppModule } from './app.module';

describe('AppModule', () => {
  let module: AppModule;

  beforeEach(async () => {
    module = new AppModule();
  });

  it('should create the app', () => {
    expect(module).toBeTruthy();
  });
});
