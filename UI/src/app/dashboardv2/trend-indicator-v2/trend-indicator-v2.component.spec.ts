import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TrendIndicatorV2Component } from './trend-indicator-v2.component';

describe('TrendIndicatorV2Component', () => {
  let component: TrendIndicatorV2Component;
  let fixture: ComponentFixture<TrendIndicatorV2Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TrendIndicatorV2Component ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TrendIndicatorV2Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
