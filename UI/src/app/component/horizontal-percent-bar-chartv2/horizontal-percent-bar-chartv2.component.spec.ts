import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HorizontalPercentBarChartv2Component } from './horizontal-percent-bar-chartv2.component';

describe('HorizontalPercentBarChartv2Component', () => {
  let component: HorizontalPercentBarChartv2Component;
  let fixture: ComponentFixture<HorizontalPercentBarChartv2Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ HorizontalPercentBarChartv2Component ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HorizontalPercentBarChartv2Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
