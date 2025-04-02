import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TabularKpiV2Component } from './tabular-kpi-v2.component';

describe('TabularKpiV2Component', () => {
  let component: TabularKpiV2Component;
  let fixture: ComponentFixture<TabularKpiV2Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TabularKpiV2Component],
    }).compileComponents();

    fixture = TestBed.createComponent(TabularKpiV2Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
