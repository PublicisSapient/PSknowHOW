import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TrendIndicatorV2Component } from './trend-indicator-v2.component';
describe('TrendIndicatorV2Component', () => {
  let component: TrendIndicatorV2Component;
  let fixture: ComponentFixture<TrendIndicatorV2Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TrendIndicatorV2Component]
    }).compileComponents();

    fixture = TestBed.createComponent(TrendIndicatorV2Component);
    component = fixture.componentInstance;
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should clear data on destroy', () => {
    component.ngOnDestroy();
    expect(component.trendData).toEqual([]);
    expect(component.colorObj).toBe('');
    expect(component.dataObj).toEqual([]);
    expect(component.headerObj).toEqual([]);
  });

  it('should generate flat array', () => {
    const dataSet = [
      { Project: 'red', 'Latest Trend': '10 (up)', 'KPI Maturity': '5 P (high)' }
    ];
    const result = component.generateFlatArray(dataSet);
    expect(result.length).toBe(3);
  });

  it('should get maturity value', () => {
    const trend = { maturityValue: 5, maturity: 'high', kpiUnit: 'points' };
    const result = component.getMaturityValue(trend);
    expect(result).toBe('5 P (high)');
  });

  it('should return maturity if no maturityValue', () => {
    const trend = { maturity: 'NA' };
    const result = component.getMaturityValue(trend);
    expect(result).toBe('NA');
  });
});
