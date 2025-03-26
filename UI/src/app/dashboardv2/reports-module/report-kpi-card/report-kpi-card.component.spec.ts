import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReportKpiCardComponent } from './report-kpi-card.component';
import { KpiHelperService } from 'src/app/services/kpi-helper.service';

describe('ReportKpiCardComponent', () => {
  let component: ReportKpiCardComponent;
  let fixture: ComponentFixture<ReportKpiCardComponent>;
  let kpiHelperService;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ReportKpiCardComponent],
      providers: [KpiHelperService],
    }).compileComponents();

    fixture = TestBed.createComponent(ReportKpiCardComponent);
    component = fixture.componentInstance;
    kpiHelperService = TestBed.inject(KpiHelperService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('ReportKpiCardComponent.ngOnChanges() ngOnChanges method', () => {
    describe('Happy paths', () => {
      it('should set default chartType to "old-table" if chartType is not provided', () => {
        const changes = {
          chartType: {
            previousValue: 'bar',
            currentValue: null,
            firstChange: false,
          },
        };

        component.ngOnChanges(changes as any);

        expect(component.chartType).toBe('old-table');
      });

      it('should call sortColors and setKpiFilters on changes', () => {
        const sortColorsSpy = spyOn(component as any, 'sortColors');
        const setKpiFiltersSpy = spyOn(component as any, 'setKpiFilters');

        component.ngOnChanges({} as any);

        expect(sortColorsSpy).toHaveBeenCalled();
        expect(setKpiFiltersSpy).toHaveBeenCalled();
      });
    });

    describe('Edge cases', () => {
      it('should handle empty kpiTrendsObj gracefully in sortColors', () => {
        component.kpiTrendsObj = [];
        component.trendColors = {};

        component.sortColors();

        expect(component.colors).toEqual([]);
      });

      it('should handle kpiFilters as a string in setKpiFilters', () => {
        component.kpiFilters = 'filter1';

        component.setKpiFilters();

        expect(component.kpiFilters).toEqual(['filter1']);
      });

      it('should handle kpiFilters as an object in setKpiFilters', () => {
        component.kpiFilters = { key1: 'value1', key2: 'value2' };

        component.setKpiFilters();

        expect(component.kpiFilters).toEqual(['value1', 'value2']);
      });
    });
  });

  // The component to be tested
  describe('ReportKpiCardComponent.sortColors() sortColors method', () => {
    describe('Happy paths', () => {
      it('should sort colors correctly when kpiTrendsObj and trendColors are properly defined', () => {
        component.kpiTrendsObj = [{ hierarchyId: '1' }, { hierarchyId: '2' }];
        component.trendColors = {
          '1': { color: 'red' },
          '2': { color: 'blue' },
        };

        component.sortColors();

        expect(component.trendColors).toEqual({
          '1': { color: 'red' },
          '2': { color: 'blue' },
        });
        expect(component.colors).toEqual(['red', 'blue']);
      });
    });
  });

  describe('KpiCardV2Component.calculateValue() calculateValue method', () => {
    describe('Happy Path Tests', () => {
      it('should calculate the total value correctly for numeric values', () => {
        const issueData = [{ value: 10 }, { value: 20 }, { value: 30 }];
        const result = component.calculateValue(issueData, 'value');
        expect(result).toBe('60');
      });

      it('should return "0" when no numeric values are present', () => {
        const issueData = [{ value: 'a' }, { value: 'b' }, { value: 'c' }];
        const result = component.calculateValue(issueData, 'value');
        expect(result).toBe('0');
      });
    });

    describe('Edge Case Tests', () => {
      it('should handle an empty issueData array gracefully', () => {
        const issueData: any[] = [];
        const result = component.calculateValue(issueData, 'value');
        expect(result).toBe('0');
      });

      it('should handle mixed data types in issueData', () => {
        const issueData = [
          { value: 10 },
          { value: '20' },
          { value: null },
          { value: undefined },
          { value: 30 },
        ];
        const result = component.calculateValue(issueData, 'value');
        expect(result).toBe('40');
      });
    });
  });

  describe('ReportKpiCardComponent.checkSprint() checkSprint method', () => {
    describe('Happy paths', () => {
      it('should return formatted value with unit when no filters are applied', () => {
        component.kpiSelectedFilterObj = {};
        const result = component.checkSprint(10.5, 'points', 'kpi1');
        expect(result).toBe('11 points');
      });

      it('should return value with unit when value is an integer and no filters are applied', () => {
        component.kpiSelectedFilterObj = {};
        const result = component.checkSprint(10, 'points', 'kpi1');
        expect(result).toBe('10 points');
      });
    });

    describe('Edge cases', () => {
      it('should return "-" when filter1 is applied and not "overall"', () => {
        component.kpiSelectedFilterObj = { filter1: ['sprint1'] };
        const result = component.checkSprint(10.5, 'points', 'kpi1');
        expect(result).toBe('-');
      });

      it('should return "-" when filter2 is applied and not "overall"', () => {
        component.kpiSelectedFilterObj = { filter2: ['sprint2'] };
        const result = component.checkSprint(10.5, 'points', 'kpi1');
        expect(result).toBe('-');
      });

      it('should handle empty filter1 and filter2 gracefully', () => {
        component.kpiSelectedFilterObj = { filter1: [], filter2: [] };
        const result = component.checkSprint(10.5, 'points', 'kpi1');
        expect(result).toBe('11 points');
      });

      it('should handle undefined kpiSelectedFilterObj gracefully', () => {
        component.kpiSelectedFilterObj = undefined;
        const result = component.checkSprint(10.5, 'points', 'kpi1');
        expect(result).toBe('11 points');
      });
    });
  });

  describe('ReportKpiCardComponent.checkFilterPresence() checkFilterPresence method', () => {
    describe('Happy paths', () => {
      it('should return the filterGroup when it exists in filterData', () => {
        // Test to ensure the method returns the filterGroup when present
        const filterData = { filterGroup: 'someGroup' };
        const result = component.checkFilterPresence(filterData);
        expect(result).toBe('someGroup');
      });

      it('should return undefined when filterGroup does not exist in filterData', () => {
        // Test to ensure the method returns undefined when filterGroup is not present
        const filterData = { anotherProperty: 'value' };
        const result = component.checkFilterPresence(filterData);
        expect(result).toBeUndefined();
      });
    });

    describe('Edge cases', () => {
      it('should return undefined when filterData is null', () => {
        // Test to ensure the method handles null filterData gracefully
        const result = component.checkFilterPresence(null);
        expect(result).toBeUndefined();
      });

      it('should return undefined when filterData is undefined', () => {
        // Test to ensure the method handles undefined filterData gracefully
        const result = component.checkFilterPresence(undefined);
        expect(result).toBeUndefined();
      });

      it('should return undefined when filterData is an empty object', () => {
        // Test to ensure the method handles an empty object gracefully
        const filterData = {};
        const result = component.checkFilterPresence(filterData);
        expect(result).toBeUndefined();
      });

      it('should return undefined when filterData is a non-object type', () => {
        // Test to ensure the method handles non-object types gracefully
        const filterData = 'string' as any;
        const result = component.checkFilterPresence(filterData);
        expect(result).toBeUndefined();
      });
    });
  });

  describe('ReportKpiCardComponent.convertToHoursIfTime() convertToHoursIfTime method', () => {
    describe('Happy paths', () => {
      it('should convert value to hours when unit is "days"', () => {
        // Arrange
        const value = 2;
        const unit = 'days';
        const expectedHours = 48;
        spyOn(kpiHelperService, 'convertToHoursIfTime').and.returnValue(
          expectedHours as any,
        );

        // Act
        const result = component.convertToHoursIfTime(value, unit);

        // Assert
        expect(result).toBe(expectedHours);
        expect(kpiHelperService.convertToHoursIfTime).toHaveBeenCalledWith(
          value,
          unit,
        );
      });

      it('should return the same value when unit is not time-related', () => {
        // Arrange
        const value = 100;
        const unit = 'units';
        spyOn(kpiHelperService, 'convertToHoursIfTime').and.returnValue(
          value as any,
        );

        // Act
        const result = component.convertToHoursIfTime(value, unit);

        // Assert
        expect(result).toBe(value);
        expect(kpiHelperService.convertToHoursIfTime).toHaveBeenCalledWith(
          value,
          unit,
        );
      });
    });

    describe('Edge cases', () => {
      it('should handle zero value correctly', () => {
        // Arrange
        const value = 0;
        const unit = 'hours';
        spyOn(kpiHelperService, 'convertToHoursIfTime').and.returnValue(
          value as any,
        );

        // Act
        const result = component.convertToHoursIfTime(value, unit);

        // Assert
        expect(result).toBe(value);
        expect(kpiHelperService.convertToHoursIfTime).toHaveBeenCalledWith(
          value,
          unit,
        );
      });

      it('should handle negative values correctly', () => {
        // Arrange
        const value = -5;
        const unit = 'days';
        const expectedHours = -120;
        spyOn(kpiHelperService, 'convertToHoursIfTime').and.returnValue(
          expectedHours as any,
        );

        // Act
        const result = component.convertToHoursIfTime(value, unit);

        // Assert
        expect(result).toBe(expectedHours);
        expect(kpiHelperService.convertToHoursIfTime).toHaveBeenCalledWith(
          value,
          unit,
        );
      });

      it('should handle null value gracefully', () => {
        // Arrange
        const value = null;
        const unit = 'hours';
        spyOn(kpiHelperService, 'convertToHoursIfTime').and.returnValue(
          null as any,
        );

        // Act
        const result = component.convertToHoursIfTime(value, unit);

        // Assert
        expect(result).toBeNull();
        expect(kpiHelperService.convertToHoursIfTime).toHaveBeenCalledWith(
          value,
          unit,
        );
      });

      it('should handle undefined unit gracefully', () => {
        // Arrange
        const value = 10;
        const unit = undefined;
        spyOn(kpiHelperService, 'convertToHoursIfTime').and.returnValue(
          value as any,
        );

        // Act
        const result = component.convertToHoursIfTime(value, unit);

        // Assert
        expect(result).toBe(value);
        expect(kpiHelperService.convertToHoursIfTime).toHaveBeenCalledWith(
          value,
          unit,
        );
      });
    });
  });
});
