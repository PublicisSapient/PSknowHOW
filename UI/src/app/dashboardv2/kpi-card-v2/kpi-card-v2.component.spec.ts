import { ComponentFixture, TestBed } from '@angular/core/testing';

import { KpiCardV2Component } from './kpi-card-v2.component';

describe('KpiCardV2Component', () => {
  let component: KpiCardV2Component;
  let fixture: ComponentFixture<KpiCardV2Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ KpiCardV2Component ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(KpiCardV2Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
