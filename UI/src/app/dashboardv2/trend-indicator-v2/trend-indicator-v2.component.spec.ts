import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TrendIndicatorV2Component } from './trend-indicator-v2.component';
import { SimpleChanges } from '@angular/core';
describe('TrendIndicatorV2Component', () => {
  let component: TrendIndicatorV2Component;
  let fixture: ComponentFixture<TrendIndicatorV2Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TrendIndicatorV2Component],
    }).compileComponents();

    fixture = TestBed.createComponent(TrendIndicatorV2Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should generate the dataObj and headerObj correctly when trendData is not empty', () => {
    const trendData = [
      {
        hierarchyName: 'API POD 1 - Core',
        value: '66.7 %',
        trend: '+ve',
        maturity: 'M4',
        maturityValue: '65.03',
        kpiUnit: '%',
      },
    ];

    component.trendData = trendData;
    component.colorObj = {
      'API POD 1 - Core': {
        nodeName: 'API POD 1 - Core',
        color: '#6079C5',
        nodeId: 'API POD 1 - Core_6524a7677c8bb73cd0c3fe67',
      },
    };

    const changes: SimpleChanges = {
      trendData: {
        currentValue: trendData,
        previousValue: null,
        firstChange: true,
        isFirstChange: () => true,
      },
    };

    component.ngOnChanges(changes);

    expect(component.dataObj).toEqual([
      ['#6079C5'],
      ['66.7 % (+ve)'],
      ['65.03 % (M4)'],
    ]);
    expect(component.headerObj).toEqual([
      'Project',
      'Latest Trend',
      'KPI Maturity',
    ]);
  });

  it('should not generate the dataObj and headerObj when trendData is empty', () => {
    const trendData = [];

    component.trendData = trendData;
    component.colorObj = {
      'Project A': { nodeName: 'Project A', color: '#FF0000' },
      'Project B': { nodeName: 'Project B', color: '#00FF00' },
    };

    const changes: SimpleChanges = {
      trendData: {
        currentValue: trendData,
        previousValue: null,
        firstChange: true,
        isFirstChange: () => true,
      },
    };

    component.ngOnChanges(changes);

    expect(component.dataObj).toEqual([]);
    expect(component.headerObj).toEqual([]);
  });
});
