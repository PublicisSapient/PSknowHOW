import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PsKpiCardChartRendererComponent } from './ps-kpi-card-chart-renderer.component';

describe('PsKpiCardChartRendererComponent', () => {
  let component: PsKpiCardChartRendererComponent;
  let fixture: ComponentFixture<PsKpiCardChartRendererComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PsKpiCardChartRendererComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PsKpiCardChartRendererComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
