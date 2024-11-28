import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TrendIndicatorComponent } from './trend-indicator.component';

describe('TrendIndicatorComponent', () => {
  let component: TrendIndicatorComponent;
  let fixture: ComponentFixture<TrendIndicatorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TrendIndicatorComponent],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TrendIndicatorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should return the correct tooltip content', () => {
    component.dataTrend = { isCumulative: true };
    expect(component.getTooltipContent()).toBe(
      '<div class="inner-content">Maturity based on latest trend on Cumulative data series</div>',
    );

    component.dataTrend = { isCumulative: false, maturityDenominator: 10 };
    expect(component.getTooltipContent()).toBe(
      '<div class="inner-content">Average maturity for 10 data points.</div>',
    );
  });
});
