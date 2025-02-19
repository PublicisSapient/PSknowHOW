/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TrendIndicatorV2Component } from './trend-indicator-v2.component';
import { SimpleChange, SimpleChanges } from '@angular/core';
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

describe('ngOnChanges', () => {
  let component: TrendIndicatorV2Component;
  let fixture: ComponentFixture<TrendIndicatorV2Component>;

  beforeEach(() => {
    fixture = TestBed.createComponent(TrendIndicatorV2Component);
    component = fixture.componentInstance;
  });
  afterEach(() => {
    TestBed.resetTestingModule(); // Reset the module after each test to prevent conflicts
  });

  it('should do nothing if trendData is empty', () => {
    const changes: SimpleChanges = {
      trendData: new SimpleChange([], [], false)
    };

    component.ngOnChanges(changes);

    expect(component.dataObj).toEqual([]);
    expect(component.headerObj).toEqual([]);
  });

  it('should update dataObj and headerObj when trendData is available', () => {
    // Mock color object
    component.colorObj = {
      '1': { nodeId: '1', color: 'red' },
      '2': { nodeId: '2', color: 'blue' }
    };

    // Mock trendData
    component.trendData = [
      { hierarchyName: 'Project A', hierarchyId: '1', value: '50', trend: 'up' },
      { hierarchyName: 'Project B', hierarchyId: '2', value: '30', trend: 'down' }
    ];

    // Spy on methods
    spyOn(component, 'getMaturityValue').and.returnValue('High');
    spyOn(component, 'generateFlatArray').and.callFake((data) => data);

    const changes: SimpleChanges = {
      trendData: new SimpleChange(null, component.trendData, true)
    };

    component.ngOnChanges(changes);

    expect(component.dataObj.length).toBe(2);
    expect(component.headerObj).toEqual(['Project', 'Latest Trend', 'KPI Maturity']);

    expect(component.dataObj[0]).toEqual({
      'Project': 'red',
      'Latest Trend': '50 (up)',
      'KPI Maturity': 'High'
    });

    expect(component.dataObj[1]).toEqual({
      'Project': 'blue',
      'Latest Trend': '30 (down)',
      'KPI Maturity': 'High'
    });

    expect(component.getMaturityValue).toHaveBeenCalledTimes(2);
    expect(component.generateFlatArray).toHaveBeenCalled();
  });

  xit('should handle empty hierarchyName gracefully', () => {
    component.colorObj = {
      '1': { nodeId: '1', color: 'red' }
    };

    component.trendData = [
      { hierarchyId: '1', value: '50', trend: 'up' },
      { hierarchyName: 'Project B', hierarchyId: '2', value: '30', trend: 'down' }
    ];

    spyOn(component, 'getMaturityValue').and.returnValue('Medium');
    spyOn(component, 'generateFlatArray').and.callFake((data) => data);

    const changes: SimpleChanges = {
      trendData: new SimpleChange(null, component.trendData, true)
    };

    component.ngOnChanges(changes);

    expect(component.dataObj.length).toBe(2);
    expect(component.dataObj[0]).toEqual({});
    expect(component.dataObj[1]).toEqual({
      'Project': undefined,
      'Latest Trend': '30 (down)',
      'KPI Maturity': 'Medium'
    });

    expect(component.generateFlatArray).toHaveBeenCalled();
  });

  it('should push an empty object to dataObj when hierarchyName is missing', () => {
    component.trendData = [
      { hierarchyId: '123', value: '50', trend: 'Up' }, // No hierarchyName
    ];
    component.colorObj = [{ nodeId: '123', color: 'red' }]; // Mocking colorObj to avoid errors

    spyOn(component, 'generateFlatArray').and.callFake((data) => data); // Mock generateFlatArray
    const changes: SimpleChanges = {
      trendData: new SimpleChange(null, component.trendData, true)
    };

    component.ngOnChanges(changes); // Simulate initialization

    expect(component.dataObj.length).toBe(1);
    expect(component.dataObj[0]).toEqual({}); // Ensuring empty object is pushed
  });
});

});
