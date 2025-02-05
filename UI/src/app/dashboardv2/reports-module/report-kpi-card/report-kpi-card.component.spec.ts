import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReportKpiCardComponent } from './report-kpi-card.component';
import { KpiHelperService } from 'src/app/services/kpi-helper.service';

describe('ReportKpiCardComponent', () => {
  let component: ReportKpiCardComponent;
  let fixture: ComponentFixture<ReportKpiCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ReportKpiCardComponent ],
      providers: [KpiHelperService]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReportKpiCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
