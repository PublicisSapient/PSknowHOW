import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TabularKpiWithDonutChartComponent } from './tabular-kpi-with-donut-chart.component';

describe('TabularKpiWithDonutChartComponent', () => {
  let component: TabularKpiWithDonutChartComponent;
  let fixture: ComponentFixture<TabularKpiWithDonutChartComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TabularKpiWithDonutChartComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(TabularKpiWithDonutChartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
