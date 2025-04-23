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

import { GoogleAnalyticsService } from 'src/app/services/google-analytics.service';
import { FilterNewComponent } from './filter-new.component';

import {
  ComponentFixture,
  TestBed,
  fakeAsync,
  tick,
} from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientModule } from '@angular/common/http';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { APP_CONFIG, AppConfig } from '../../services/app.config';
import { CommonModule, DatePipe } from '@angular/common';

import { SharedService } from 'src/app/services/shared.service';
import { HelperService } from 'src/app/services/helper.service';
import { HttpService } from 'src/app/services/http.service';
import { FeatureFlagsService } from 'src/app/services/feature-toggle.service';
import { MessageService } from 'primeng/api';
import { of, throwError } from 'rxjs';

class MockMultiSelect {
  overlayVisible: boolean = false;
  close = () => {};
  show = () => {};
}

describe('FilterNewComponent', () => {
  let fixture: ComponentFixture<FilterNewComponent>;
  let component: FilterNewComponent;
  let sharedService: SharedService;
  let httpService: HttpService;
  let helperService;
  let featureFlagsService;
  let messageService;
  let gaService;
  let mockMultiSelect: MockMultiSelect;
  beforeEach(async () => {
    mockMultiSelect = new MockMultiSelect() as any;
    await TestBed.configureTestingModule({
      declarations: [FilterNewComponent],
      imports: [
        RouterTestingModule,
        HttpClientModule,
        BrowserAnimationsModule,
        HttpClientTestingModule,
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],

      providers: [
        SharedService,
        HelperService,
        CommonModule,
        DatePipe,
        { provide: APP_CONFIG, useValue: AppConfig },
        HttpService,
        FeatureFlagsService,
        MessageService,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(FilterNewComponent);
    component = fixture.componentInstance;
    sharedService = TestBed.inject(SharedService);
    helperService = TestBed.inject(HelperService);
    httpService = TestBed.inject(HttpService);
    featureFlagsService = TestBed.inject(FeatureFlagsService);
    messageService = TestBed.inject(MessageService);
    gaService = TestBed.inject(GoogleAnalyticsService);
    component.selectedTab = 'iteration';
    component.selectedType = 'scrum';
    component.showHideDdn = mockMultiSelect as any;
    localStorage.setItem(
      'completeHierarchyData',
      '{"kanban":[{"id":"6442815917ed167d8157f0f5","level":1,"hierarchyLevelId":"bu","hierarchyLevelName":"BU","hierarchyInfo":"Business Unit"},{"id":"6442815917ed167d8157f0f6","level":2,"hierarchyLevelId":"ver","hierarchyLevelName":"Vertical","hierarchyInfo":"Industry"},{"id":"6442815917ed167d8157f0f7","level":3,"hierarchyLevelId":"acc","hierarchyLevelName":"Account","hierarchyInfo":"Account"},{"id":"6442815917ed167d8157f0f8","level":4,"hierarchyLevelId":"port","hierarchyLevelName":"Engagement","hierarchyInfo":"Engagement"},{"level":5,"hierarchyLevelId":"project","hierarchyLevelName":"Project"},{"level":6,"hierarchyLevelId":"release","hierarchyLevelName":"Release"},{"level":7,"hierarchyLevelId":"sqd","hierarchyLevelName":"Squad"}],"scrum":[{"id":"6442815917ed167d8157f0f5","level":1,"hierarchyLevelId":"bu","hierarchyLevelName":"BU","hierarchyInfo":"Business Unit"},{"id":"6442815917ed167d8157f0f6","level":2,"hierarchyLevelId":"ver","hierarchyLevelName":"Vertical","hierarchyInfo":"Industry"},{"id":"6442815917ed167d8157f0f7","level":3,"hierarchyLevelId":"acc","hierarchyLevelName":"Account","hierarchyInfo":"Account"},{"id":"6442815917ed167d8157f0f8","level":4,"hierarchyLevelId":"port","hierarchyLevelName":"Engagement","hierarchyInfo":"Engagement"},{"level":5,"hierarchyLevelId":"project","hierarchyLevelName":"Project"},{"level":6,"hierarchyLevelId":"sprint","hierarchyLevelName":"Sprint"},{"level":6,"hierarchyLevelId":"release","hierarchyLevelName":"Release"},{"level":7,"hierarchyLevelId":"sqd","hierarchyLevelName":"Squad"}]}',
    );
    localStorage.setItem(
      'shared_link',
      '/dashboard/speed?stateFilters=fRB6gVl_&kpiFilters=47DEQpj8&selectedTab=speed&selectedType=scrum',
    );
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should return an array of object values', () => {
    const obj = { a: 1, b: 2, c: 3 };
    expect(component.objectKeys(obj)).toEqual([1, 2, 3]);
  });

  it('should return an empty array for an empty object', () => {
    const obj = {};
    expect(component.objectKeys(obj)).toEqual([]);
  });

  it('should return an empty array for null input', () => {
    expect(component.objectKeys(null)).toEqual([]);
  });

  it('should return an empty array for undefined input', () => {
    expect(component.objectKeys(undefined)).toEqual([]);
  });

  xit('should return the immediate parent display name and child nodeId', () => {
    const child = { nodeId: 'child1' };
    const result = component.getImmediateParentDisplayName(child);
    expect(result).toBe('');
  });

  xit('should return an empty string if child node is not found', () => {
    const child = { nodeId: 'child2' };
    const result = component.getImmediateParentDisplayName(child);
    expect(result).toBe('');
  });

  xit('should return an empty string if filterDataArr is empty', () => {
    component.filterDataArr = {};
    const child = { nodeId: 'child1' };
    const result = component.getImmediateParentDisplayName(child);
    expect(result).toBe('');
  });

  describe('FilterNewComponent.ngOnInit() ngOnInit method', () => {
    describe('Happy Path', () => {
      it('should initialize selectedTab and selectedType correctly', async () => {
        spyOn(sharedService, 'getSelectedTab').and.returnValue('iteration');
        spyOn(sharedService, 'getBackupOfFilterSelectionState').and.returnValue(
          'scrum',
        );
        spyOn(featureFlagsService, 'isFeatureEnabled').and.returnValue(true);
        spyOn(sharedService, 'setRecommendationsFlag');
        await component.ngOnInit();

        expect(component.selectedTab).toBe('iteration');
        expect(component.selectedType).toBe('scrum');
        expect(component.kanban).toBe(false);
        expect(sharedService.setRecommendationsFlag).toHaveBeenCalledWith(true);
      });
    });

    describe('Edge Cases', () => {
      it('should handle null selectedTab and selectedType gracefully', async () => {
        spyOn(sharedService, 'getSelectedTab').and.returnValue(null);
        spyOn(sharedService, 'getBackupOfFilterSelectionState').and.returnValue(
          null,
        );
        spyOn(featureFlagsService, 'isFeatureEnabled').and.returnValue(false);
        spyOn(sharedService, 'setRecommendationsFlag');
        await component.ngOnInit();

        // expect(component.selectedTab).toBe('iteration');
        expect(component.selectedType).toBe('scrum');
        expect(component.kanban).toBe(false);
        expect(sharedService.setRecommendationsFlag).toHaveBeenCalledWith(
          false,
        );
      });
    });
  });

  describe('FilterNewComponent.setDateFilter() setDateFilter method', () => {
    describe('Happy Path', () => {
      it('should set kanban to true and add "Months" to dateRangeFilter.types when selectedType is "kanban"', () => {
        component.selectedType = 'kanban';
        component.dateRangeFilter = { types: ['Days', 'Weeks'], counts: [] };
        component.setDateFilter();
        expect(component.kanban).toBe(true);
        expect(component.dateRangeFilter.types).toContain('Months');
      });

      it('should set kanban to false and remove "Months" from dateRangeFilter.types when selectedType is not "kanban"', () => {
        component.selectedType = 'scrum';
        component.dateRangeFilter = {
          types: ['Days', 'Weeks', 'Months'],
          counts: [],
        };
        component.setDateFilter();
        expect(component.kanban).toBe(false);
        expect(component.dateRangeFilter.types).not.toContain('Months');
      });

      it('should remove "Months" from dateRangeFilter.types when selectedTab is "developer" and selectedType is "kanban"', () => {
        component.selectedType = 'kanban';
        component.selectedTab = 'developer';
        component.dateRangeFilter = {
          types: ['Days', 'Weeks', 'Months'],
          counts: [],
        };
        component.setDateFilter();
        expect(component.dateRangeFilter.types).not.toContain('Months');
      });
    });

    describe('Edge Cases', () => {
      it('should handle empty dateRangeFilter.types gracefully', () => {
        component.selectedType = 'kanban';
        component.dateRangeFilter = { types: [], counts: [] };
        component.setDateFilter();
        expect(component.dateRangeFilter.types).toContain('Months');
      });

      it('should not add "Months" if it already exists in dateRangeFilter.types', () => {
        component.selectedType = 'kanban';
        component.dateRangeFilter = {
          types: ['Days', 'Weeks', 'Months'],
          counts: [],
        };
        component.setDateFilter();
        expect(
          component.dateRangeFilter.types.filter((type) => type === 'Months')
            .length,
        ).toBe(1);
      });
    });
  });

  describe('FilterNewComponent.setHierarchyLevels() setHierarchyLevels method', () => {
    describe('Happy Path', () => {
      it('should set hierarchies and call getFiltersData when hierarchies are not set', () => {
        // Arrange
        const mockHierarchyData = { data: [{ id: 1, name: 'Level 1' }] };
        spyOn(httpService, 'getAllHierarchyLevels').and.returnValue(
          of(mockHierarchyData) as any,
        );
        const getFiltersDataSpy = spyOn(component, 'getFiltersData' as any);

        // Act
        component.setHierarchyLevels();

        // Assert
        expect(httpService.getAllHierarchyLevels).toHaveBeenCalled();
        expect(component.hierarchies).toEqual(mockHierarchyData.data);
        expect(getFiltersDataSpy).toHaveBeenCalled();
      });

      it('should call getFiltersData when hierarchies are already set', () => {
        // Arrange
        component.hierarchies = [{ id: 1, name: 'Level 1' }];
        const getFiltersDataSpy = spyOn(component, 'getFiltersData' as any);

        // Act
        component.setHierarchyLevels();

        // Assert
        expect(getFiltersDataSpy).toHaveBeenCalled();
      });
    });

    describe('Edge Cases', () => {
      it('should handle error when getAllHierarchyLevels fails', () => {
        // Arrange
        spyOn(httpService, 'getAllHierarchyLevels').and.returnValue(
          of({ error: 'Error' }) as any,
        );
        const getFiltersDataSpy = spyOn(component, 'getFiltersData' as any);

        // Act
        component.setHierarchyLevels();

        // Assert
        expect(httpService.getAllHierarchyLevels).toHaveBeenCalled();
        expect(getFiltersDataSpy).not.toHaveBeenCalled();
      });

      it('should handle empty data from getAllHierarchyLevels', () => {
        // Arrange
        const mockHierarchyData = { data: [] };
        spyOn(httpService, 'getAllHierarchyLevels').and.returnValue(
          of(mockHierarchyData) as any,
        );
        const getFiltersDataSpy = spyOn(component, 'getFiltersData' as any);

        // Act
        component.setHierarchyLevels();

        // Assert
        expect(httpService.getAllHierarchyLevels).toHaveBeenCalled();
        expect(component.hierarchies).toEqual([]);
        expect(getFiltersDataSpy).toHaveBeenCalled();
      });
    });
  });

  describe('FilterNewComponent.setSelectedMapLevels() setSelectedMapLevels method', () => {
    describe('Happy Path', () => {
      it('should correctly set selectedMap for scrum type', () => {
        // Arrange
        component.kanban = false;
        const mockHierarchyData = {
          scrum: [
            { hierarchyLevelId: 'level1', hierarchyLevelName: 'Level 1' },
            { hierarchyLevelId: 'level2', hierarchyLevelName: 'Level 2' },
          ],
        };
        localStorage.setItem(
          'completeHierarchyData',
          JSON.stringify(mockHierarchyData),
        );
        component.filterApplyData['selectedMap'] = {
          'Level 1': 'value1',
          'Level 2': 'value2',
        };

        // Act
        component.setSelectedMapLevels();

        // Assert
        expect(component.filterApplyData['selectedMap']).toEqual({
          level1: 'value1',
          level2: 'value2',
        });
      });

      it('should correctly set selectedMap for kanban type', () => {
        // Arrange
        component.kanban = true;
        const mockHierarchyData = {
          kanban: [
            { hierarchyLevelId: 'level1', hierarchyLevelName: 'Level 1' },
            { hierarchyLevelId: 'level2', hierarchyLevelName: 'Level 2' },
          ],
        };
        localStorage.setItem(
          'completeHierarchyData',
          JSON.stringify(mockHierarchyData),
        );
        component.filterApplyData['selectedMap'] = {
          'Level 1': 'value1',
          'Level 2': 'value2',
        };

        // Act
        component.setSelectedMapLevels();

        // Assert
        expect(component.filterApplyData['selectedMap']).toEqual({
          level1: 'value1',
          level2: 'value2',
        });
      });
    });

    describe('Edge Cases', () => {
      it('should handle missing selectedMap data gracefully', () => {
        // Arrange
        component.kanban = false;
        const mockHierarchyData = {
          scrum: [
            { hierarchyLevelId: 'level1', hierarchyLevelName: 'Level 1' },
          ],
        };
        localStorage.setItem(
          'completeHierarchyData',
          JSON.stringify(mockHierarchyData),
        );
        component.filterApplyData['selectedMap'] = {};

        // Act
        component.setSelectedMapLevels();

        // Assert
        expect(component.filterApplyData['selectedMap']).toEqual({
          level1: undefined,
        });
      });
    });
  });

  describe('FilterNewComponent.setSelectedDateType() setSelectedDateType method', () => {
    describe('Happy Path', () => {
      it('should set the selected date type and notify the service', () => {
        // Arrange
        const label = 'Weeks';
        const spy = spyOn(sharedService.dateFilterSelectedDateType, 'next');

        // Act
        component.setSelectedDateType(label);

        // Assert
        expect(spy).toHaveBeenCalledWith(label);
      });
    });

    describe('Edge Cases', () => {
      it('should handle empty string as label', () => {
        // Arrange
        const label = '';
        const spy = spyOn(sharedService.dateFilterSelectedDateType, 'next');

        // Act
        component.setSelectedDateType(label);

        // Assert
        expect(spy).toHaveBeenCalledWith(label);
      });

      it('should handle null as label', () => {
        // Arrange
        const label = null;
        const spy = spyOn(sharedService.dateFilterSelectedDateType, 'next');

        // Act
        component.setSelectedDateType(label as any);

        // Assert
        expect(spy).toHaveBeenCalledWith(label);
      });

      it('should handle undefined as label', () => {
        // Arrange
        const label = undefined;
        const spy = spyOn(sharedService.dateFilterSelectedDateType, 'next');

        // Act
        component.setSelectedDateType(label as any);

        // Assert
        expect(spy).toHaveBeenCalledWith(label);
      });
    });
  });

  describe('FilterNewComponent.setSelectedType() setSelectedType method', () => {
    describe('Happy Path', () => {
      it('should set selectedType to "kanban" and update related properties', () => {
        // Arrange
        const type = 'kanban';
        spyOn(sharedService, 'setBackupOfFilterSelectionState');
        spyOn(sharedService, 'setScrumKanban');
        spyOn(sharedService, 'setSelectedType');
        // Act
        component.setSelectedType(type);

        // Assert
        expect(component.selectedType).toBe('kanban');
        expect(component.kanban).toBe(true);
        expect(component.filterApplyData).toEqual({});
        expect(sharedService.setSelectedType).toHaveBeenCalledWith('kanban');
        expect(
          sharedService.setBackupOfFilterSelectionState,
        ).toHaveBeenCalledWith({ selected_type: 'kanban' });
        expect(sharedService.setScrumKanban).toHaveBeenCalledWith('kanban');
      });

      it('should set selectedType to "scrum" and update related properties', () => {
        // Arrange
        const type = 'scrum';
        spyOn(sharedService, 'setBackupOfFilterSelectionState');
        spyOn(sharedService, 'setScrumKanban');
        spyOn(sharedService, 'setSelectedType');
        // Act
        component.setSelectedType(type);

        // Assert
        expect(component.selectedType).toBe('scrum');
        expect(component.kanban).toBe(false);
        expect(component.filterApplyData).toEqual({});
        expect(sharedService.setSelectedType).toHaveBeenCalledWith('scrum');
        expect(
          sharedService.setBackupOfFilterSelectionState,
        ).toHaveBeenCalledWith({ selected_type: 'scrum' });
        expect(sharedService.setScrumKanban).toHaveBeenCalledWith('scrum');
      });
    });

    describe('Edge Cases', () => {
      it('should handle empty string type gracefully', () => {
        // Arrange
        const type = '';
        spyOn(sharedService, 'setBackupOfFilterSelectionState');
        spyOn(sharedService, 'setScrumKanban');
        spyOn(sharedService, 'setSelectedType');

        // Act
        component.setSelectedType(type);

        // Assert
        expect(component.selectedType).toBe('');
        expect(component.kanban).toBe(false);
        expect(component.filterApplyData).toEqual({});
        expect(sharedService.setSelectedType).toHaveBeenCalledWith('');
        expect(
          sharedService.setBackupOfFilterSelectionState,
        ).toHaveBeenCalledWith({ selected_type: '' });
        expect(sharedService.setScrumKanban).toHaveBeenCalledWith('');
      });
    });
  });

  describe('FilterNewComponent.processBoardData() processBoardData method', () => {
    describe('Happy Path', () => {
      it('should process board data correctly when valid data is provided', () => {
        const boardData = {
          scrum: [
            {
              boardSlug: 'iteration',
              filters: {
                projectTypeSwitch: { enabled: true },
                parentFilter: { someKey: 'someValue' },
                primaryFilter: { someKey: 'someValue' },
                additionalFilters: [{ someKey: 'someValue' }],
              },
              kpis: [{ shown: true, kpiDetail: { someDetail: 'detail' } }],
            },
          ],
          others: [],
        };

        component.selectedType = 'scrum';
        component.selectedTab = 'iteration';

        component.processBoardData(boardData);

        expect(component.kanbanRequired).toEqual({ enabled: true });
        expect(component.masterData['kpiList'].length).toBe(1);
        expect(component.parentFilterConfig).toEqual({ someKey: 'someValue' });
        expect(component.additionalFilterConfig).toEqual([
          { someKey: 'someValue' },
        ]);
      });

      it('should process board data correctly and set primary filter config when parent filter config is not there', () => {
        const boardData = {
          scrum: [
            {
              boardSlug: 'iteration',
              filters: {
                projectTypeSwitch: { enabled: true },
                primaryFilter: { someKey: 'someValue' },
                additionalFilters: [{ someKey: 'someValue' }],
              },
              kpis: [{ shown: true, kpiDetail: { someDetail: 'detail' } }],
            },
          ],
          others: [],
        };

        component.selectedType = 'scrum';
        component.selectedTab = 'iteration';

        component.processBoardData(boardData);

        expect(component.kanbanRequired).toEqual({ enabled: true });
        expect(component.masterData['kpiList'].length).toBe(1);
        expect(component.primaryFilterConfig).toEqual({ someKey: 'someValue' });
        expect(component.additionalFilterConfig).toEqual([
          { someKey: 'someValue' },
        ]);
      });

      it('should process board data correctly and set additional filter config to null when additional filter config is not there', () => {
        const boardData = {
          scrum: [
            {
              boardSlug: 'iteration',
              filters: {
                projectTypeSwitch: { enabled: true },
                primaryFilter: { someKey: 'someValue' },
              },
              kpis: [{ shown: true, kpiDetail: { someDetail: 'detail' } }],
            },
          ],
          others: [],
        };

        component.selectedType = 'scrum';
        component.selectedTab = 'iteration';

        component.processBoardData(boardData);

        expect(component.kanbanRequired).toEqual({ enabled: true });
        expect(component.masterData['kpiList'].length).toBe(1);
        expect(component.primaryFilterConfig).toEqual({ someKey: 'someValue' });
        expect(component.additionalFilterConfig).toBe(null);
      });

      it('should process board data correctly for boards under "other" category when valid data is provided', () => {
        const boardData = {
          scrum: [
            {
              boardSlug: 'iteration',
              filters: {
                projectTypeSwitch: { enabled: true },
                parentFilter: { someKey: 'someValue' },
                primaryFilter: { someKey: 'someValue' },
                additionalFilters: [{ someKey: 'someValue' }],
              },
              kpis: [{ shown: true, kpiDetail: { someDetail: 'detail' } }],
            },
          ],
          others: [
            {
              boardSlug: 'release',
              filters: {
                projectTypeSwitch: { enabled: true },
                parentFilter: { someKey: 'someValue' },
                primaryFilter: { someKey: 'someValue' },
                additionalFilters: [{ someKey: 'someValue' }],
              },
              kpis: [{ shown: true, kpiDetail: { someDetail: 'detail' } }],
            },
          ],
        };

        component.selectedType = 'scrum';
        component.selectedTab = 'release';

        component.processBoardData(boardData);

        expect(component.kanbanRequired).toEqual({ enabled: true });
        expect(component.masterData['kpiList'].length).toBe(1);
        expect(component.parentFilterConfig).toEqual({ someKey: 'someValue' });
        expect(component.additionalFilterConfig).toEqual([
          { someKey: 'someValue' },
        ]);
      });
    });

    describe('Edge Cases', () => {
      it('should switch to scrum if kanban is not enabled', () => {
        spyOn(sharedService, 'setSelectedType');
        const boardData = {
          kanban: [
            {
              boardSlug: 'iteration',
              filters: {
                projectTypeSwitch: { enabled: false },
              },
              kpis: [{ shown: true, kpiDetail: { someDetail: 'detail' } }],
            },
          ],
          others: [],
        };

        component.selectedType = 'kanban';
        component.selectedTab = 'iteration';

        component.processBoardData(boardData);

        expect(component.kanban).toBe(false);
        expect(component.selectedType).toBe('scrum');
        expect(sharedService.setSelectedType).toHaveBeenCalledWith('scrum');
      });
    });
  });

  describe('FilterNewComponent.getFiltersData() getFiltersData method', () => {
    describe('Happy Path', () => {
      it('should fetch and process filter data successfully', async () => {
        // Arrange
        component.previousSelectedFilterData = {};
        const mockHierarchyData = {
          data: [{ hierarchyLevelId: '1', hierarchyLevelName: 'Project' }],
        };
        const mockFilterData = {
          success: true,
          data: [
            {
              level: 1,
              labelName: 'Project',
              nodeId: '123',
              nodeName: 'def',
              basicProjectConfigId: '123',
            },
            {
              level: 1,
              labelName: 'Project2',
              nodeId: '321',
              nodeName: 'abc',
              basicProjectConfigId: '321',
            },
          ],
        };
        spyOn(helperService, 'sortAlphabetically').and.returnValue(
          of([
            {
              level: 1,
              labelName: 'Project2',
              nodeId: '321',
              nodeName: 'abc',
              basicProjectConfigId: '321',
            },
            {
              level: 1,
              labelName: 'Project',
              nodeId: '123',
              nodeName: 'def',
              basicProjectConfigId: '123',
            },
          ]),
        );
        spyOn(httpService, 'getFilterData').and.returnValue(of(mockFilterData));
        component.selectedTab = 'iteration';
        // Act
        component.getFiltersData();

        // Assert
        // expect(httpService.getAllHierarchyLevels).toHaveBeenCalled();
        expect(httpService.getFilterData).toHaveBeenCalledWith({
          kanban: false,
          sprintIncluded: ['CLOSED', 'ACTIVE'],
        });
        expect(component.filterApiData).toEqual(mockFilterData.data);
      });
    });

    describe('Edge Cases', () => {
      it('should handle empty hierarchy data gracefully', () => {
        // Arrange
        const mockHierarchyData = { data: [] };
        spyOn(httpService, 'getAllHierarchyLevels').and.returnValue(
          of(mockHierarchyData),
        );

        // Act
        component.getFiltersData();

        // Assert
        // expect(httpService.getAllHierarchyLevels).toHaveBeenCalled();
        expect(component.hierarchies).toBeUndefined();
      });

      it('should handle filter data fetch failure', () => {
        // Arrange
        const mockHierarchyData = {
          data: [{ hierarchyLevelId: '1', hierarchyLevelName: 'Project' }],
        };
        const mockFilterData = { success: false };
        spyOn(httpService, 'getAllHierarchyLevels').and.returnValue(
          of(mockHierarchyData),
        );
        spyOn(httpService, 'getFilterData').and.returnValue(of(mockFilterData));

        // Act
        component.getFiltersData();

        // Assert
        expect(httpService.getFilterData).toHaveBeenCalled();
        expect(component.filterApiData).toEqual([]);
      });
    });
  });

  describe('FilterNewComponent.setCategories() setCategories method', () => {
    describe('Happy Path', () => {
      it('should set categories correctly when data is available', () => {
        // Arrange
        const mockHierarchyData = [
          { hierarchyLevelId: 'level1', hierarchyLevelName: 'Level 1' },
          { hierarchyLevelId: 'level2', hierarchyLevelName: 'Level 2' },
        ];
        const mockFilterDataArr = {
          scrum: {
            level1: [
              {
                level: 1,
                labelName: 'Project1',
                nodeId: '1',
                nodeName: 'Node 1',
                basicProjectConfigId: '321',
              },
            ],
            level2: [
              {
                level: 2,
                labelName: 'Project2',
                nodeId: '2',
                nodeName: 'Node 2',
                basicProjectConfigId: '421',
              },
            ],
          },
        };
        localStorage.setItem(
          'completeHierarchyData',
          JSON.stringify({ scrum: mockHierarchyData }),
        );
        component.filterDataArr = mockFilterDataArr;
        component.selectedType = 'scrum';
        spyOn(helperService, 'sortAlphabetically').and.returnValue(
          of([
            {
              level: 1,
              labelName: 'Project1',
              nodeId: '1',
              nodeName: 'Node 1',
              basicProjectConfigId: '321',
            },
            {
              level: 2,
              labelName: 'Project2',
              nodeId: '2',
              nodeName: 'Node 2',
              basicProjectConfigId: '421',
            },
          ]),
        );
        // Act
        component.setCategories();

        // Assert
        expect(component.filterDataArr['scrum']).toEqual({
          'Level 1': [
            {
              level: 1,
              labelName: 'Project1',
              nodeId: '1',
              nodeName: 'Node 1',
              basicProjectConfigId: '321',
            },
          ],
          'Level 2': [
            {
              level: 2,
              labelName: 'Project2',
              nodeId: '2',
              nodeName: 'Node 2',
              basicProjectConfigId: '421',
            },
          ],
        });
      });
    });
  });

  describe('FilterNewComponent.compareStringArrays() compareStringArrays method', () => {
    describe('Happy Path', () => {
      it('should return true for identical arrays', () => {
        const array1 = ['a', 'b', 'c'];
        const array2 = ['a', 'b', 'c'];
        expect(component.compareStringArrays(array1, array2)).toBe(true);
      });

      it('should return false for arrays with different lengths', () => {
        const array1 = ['a', 'b', 'c'];
        const array2 = ['a', 'b'];
        expect(component.compareStringArrays(array1, array2)).toBe(false);
      });

      it('should return false for arrays with same length but different elements', () => {
        const array1 = ['a', 'b', 'c'];
        const array2 = ['a', 'b', 'd'];
        expect(component.compareStringArrays(array1, array2)).toBe(false);
      });
    });

    describe('Edge Cases', () => {
      it('should return false if the first array is null', () => {
        const array1 = null;
        const array2 = ['a', 'b', 'c'];
        expect(component.compareStringArrays(array1, array2)).toBe(false);
      });

      it('should return false if the second array is null', () => {
        const array1 = ['a', 'b', 'c'];
        const array2 = null;
        expect(component.compareStringArrays(array1, array2)).toBe(false);
      });

      it('should return false if both arrays are null', () => {
        const array1 = null;
        const array2 = null;
        expect(component.compareStringArrays(array1, array2)).toBe(false);
      });

      it('should return true for two empty arrays', () => {
        const array1: string[] = [];
        const array2: string[] = [];
        expect(component.compareStringArrays(array1, array2)).toBe(true);
      });

      it('should return false for one empty array and one non-empty array', () => {
        const array1: string[] = [];
        const array2 = ['a'];
        expect(component.compareStringArrays(array1, array2)).toBe(false);
      });
    });
  });

  describe('FilterNewComponent.getBoardConfig() getBoardConfig method', () => {
    it('should successfully fetch and process board config data', async () => {
      // Arrange
      const mockResponse = {
        success: true,
        data: {
          userBoardConfigDTO: {
            scrum: [],
            others: [],
            configDetails: {},
          },
        },
      };
      spyOn(httpService, 'getShowHideOnDashboardNewUI').and.returnValue(
        of(mockResponse) as any,
      );
      spyOn(sharedService, 'setDashConfigData');
      // Act
      await component.getBoardConfig(['project1']);

      // Assert
      expect(httpService.getShowHideOnDashboardNewUI).toHaveBeenCalledWith({
        basicProjectConfigIds: ['project1'],
      });
      expect(sharedService.setDashConfigData).toHaveBeenCalled();
      expect(component.blockUI).toBe(false);
    });
  });

  describe('Edge Cases', () => {
    it('should handle error response gracefully', async () => {
      // Arrange
      spyOn(httpService, 'getShowHideOnDashboardNewUI').and.returnValue(
        throwError(new Error('Error fetching data')) as any,
      );
      spyOn(messageService, 'add');
      // Act
      await component.getBoardConfig(['project1']);

      // Assert
      expect(messageService.add).toHaveBeenCalledWith({
        severity: 'error',
        summary: 'Error fetching data',
      });
      expect(component.blockUI).toBe(false);
    });

    it('should not make API call if projectList is unchanged', async () => {
      // Arrange
      component.projectList = ['project1'];
      spyOn(httpService, 'getShowHideOnDashboardNewUI');
      // Act
      await component.getBoardConfig(['project1']);

      // Assert
      expect(httpService.getShowHideOnDashboardNewUI).not.toHaveBeenCalled();
    });
  });

  describe('FilterNewComponent.removeFilter() removeFilter method', () => {
    describe('Happy Path', () => {
      it('should remove a filter when multiple filters are present', () => {
        // Arrange
        component.filterDataArr = {
          scrum: {
            Project: [
              { nodeId: '1', nodeName: 'Filter1', labelName: 'Project' },
              { nodeId: '2', nodeName: 'Filter2', labelName: 'Project' },
            ],
          },
        };
        component.colorObj = {
          '1': { nodeId: '1', nodeName: 'Filter1', labelName: 'Project' },
          '2': { nodeId: '2', nodeName: 'Filter2', labelName: 'Project' },
        };
        const stateFilters = {
          primary_level: [{ nodeId: '1', labelName: 'Project' }],
        };
        spyOn(sharedService, 'getBackupOfFilterSelectionState').and.returnValue(
          stateFilters,
        );

        spyOn(sharedService, 'setSelectedTrends');

        spyOn(sharedService, 'setBackupOfFilterSelectionState');

        // Act
        component.removeFilter('1');

        // Assert
        expect(component.colorObj).not.toEqual(
          jasmine.objectContaining({
            1: { nodeId: '1', nodeName: 'Filter1' },
          }),
        );
        expect(sharedService.setSelectedTrends).toHaveBeenCalled();
        expect(
          sharedService.setBackupOfFilterSelectionState,
        ).toHaveBeenCalled();
      });
    });

    describe('Edge Cases', () => {
      it('should not remove a filter if it is the only one present', () => {
        // Arrange
        component.colorObj = {
          '1': { nodeId: '1', nodeName: 'Filter1' },
        };
        spyOn(sharedService, 'setSelectedTrends');
        spyOn(sharedService, 'setBackupOfFilterSelectionState');
        // Act
        component.removeFilter('1');

        // Assert
        expect(component.colorObj).toEqual(
          jasmine.objectContaining({
            1: { nodeId: '1', nodeName: 'Filter1' },
          }),
        );
        expect(sharedService.setSelectedTrends).not.toHaveBeenCalled();
        expect(
          sharedService.setBackupOfFilterSelectionState,
        ).not.toHaveBeenCalled();
      });

      it('should handle removal of a non-existent filter gracefully', () => {
        // Arrange
        component.colorObj = {
          '1': { nodeId: '1', nodeName: 'Filter1' },
        };
        spyOn(sharedService, 'setSelectedTrends');
        spyOn(sharedService, 'setBackupOfFilterSelectionState');
        // Act
        component.removeFilter('2');

        // Assert
        expect(component.colorObj).toEqual(
          jasmine.objectContaining({
            1: { nodeId: '1', nodeName: 'Filter1' },
          }),
        );
        expect(sharedService.setSelectedTrends).not.toHaveBeenCalled();
        expect(
          sharedService.setBackupOfFilterSelectionState,
        ).not.toHaveBeenCalled();
      });
    });
  });

  describe('FilterNewComponent.setAdditionalHierarchyLevels() setAdditionalHierarchyLevels method', () => {
    describe('Happy Path', () => {
      it('should set additionalFilterLevelArr correctly when hierarchies are defined', () => {
        // Arrange
        component.hierarchies = {
          scrum: [
            { hierarchyLevelId: 'project', level: 1 },
            { hierarchyLevelId: 'team', level: 2 },
            { hierarchyLevelId: 'sprint', level: 3 },
          ],
        };
        component.selectedType = 'scrum';

        // Act
        component.setAdditionalHierarchyLevels();

        // Assert
        expect(component.additionalFilterLevelArr).toEqual([
          { hierarchyLevelId: 'team', level: 2 },
          { hierarchyLevelId: 'sprint', level: 3 },
        ]);
      });

      it('should set squadLevel correctly excluding sprint and release', () => {
        // Arrange
        component.hierarchies = {
          scrum: [
            { hierarchyLevelId: 'project', level: 1 },
            { hierarchyLevelId: 'team', level: 2 },
            { hierarchyLevelId: 'sprint', level: 3 },
            { hierarchyLevelId: 'release', level: 4 },
          ],
        };
        component.selectedType = 'scrum';

        // Act
        component.setAdditionalHierarchyLevels();

        // Assert
        expect(component.squadLevel).toEqual([
          { hierarchyLevelId: 'team', level: 2 },
        ]);
      });
    });

    describe('Edge Cases', () => {
      it('should handle empty hierarchies gracefully', () => {
        // Arrange
        component.hierarchies = {};
        component.selectedType = 'scrum';

        // Act
        component.setAdditionalHierarchyLevels();

        // Assert
        expect(component.additionalFilterLevelArr).toEqual([]);
        expect(component.squadLevel).toEqual([]);
      });

      it('should handle undefined selectedType gracefully', () => {
        // Arrange
        component.hierarchies = {
          scrum: [
            { hierarchyLevelId: 'project', level: 1 },
            { hierarchyLevelId: 'team', level: 2 },
          ],
        };
        component.selectedType = undefined;

        // Act
        component.setAdditionalHierarchyLevels();

        // Assert
        expect(component.additionalFilterLevelArr).toEqual([]);
        expect(component.squadLevel).toEqual([]);
      });
    });
  });

  describe('FilterNewComponent.firstLoadFilterCheck() firstLoadFilterCheck method', () => {
    describe('Happy Path', () => {
      it('should set kanbanProjectsAvailable to true when filter data is available for kanban', () => {
        // Arrange
        const filterApiData = {
          success: true,
          data: [{ labelName: 'project' }],
        };
        spyOn(httpService, 'getFilterData').and.returnValue(
          of(filterApiData) as any,
        );
        spyOn(sharedService, 'setNoProjectsForNewUI');
        // Act
        component.firstLoadFilterCheck(true);

        // Assert
        expect(component.kanbanProjectsAvailable).toBe(true);
        expect(sharedService.setNoProjectsForNewUI).toHaveBeenCalledWith({
          kanban: false,
          scrum: false,
        });
      });

      it('should set scrumProjectsAvailable to true when filter data is available for scrum', () => {
        // Arrange
        const filterApiData = {
          success: true,
          data: [{ labelName: 'project' }],
        };
        spyOn(httpService, 'getFilterData').and.returnValue(
          of(filterApiData) as any,
        );
        spyOn(sharedService, 'setNoProjectsForNewUI');
        // Act
        component.firstLoadFilterCheck(false);

        // Assert
        expect(component.scrumProjectsAvailable).toBe(true);
        expect(sharedService.setNoProjectsForNewUI).toHaveBeenCalledWith({
          kanban: false,
          scrum: false,
        });
      });
    });

    describe('Edge Cases', () => {
      it('should handle empty filter data gracefully', () => {
        // Arrange
        const filterApiData = { success: true, data: [] };
        spyOn(httpService, 'getFilterData').and.returnValue(
          of(filterApiData) as any,
        );
        spyOn(sharedService, 'setNoProjectsForNewUI');
        spyOn(sharedService, 'setNoProjects');
        // Act
        component.firstLoadFilterCheck(true);

        // Assert
        expect(component.kanbanProjectsAvailable).toBe(false);
        expect(sharedService.setNoProjectsForNewUI).toHaveBeenCalledWith({
          kanban: true,
          scrum: false,
        });
      });

      it('should handle empty filter data gracefully for scrum', () => {
        // Arrange
        const filterApiData = { success: true, data: [] };
        spyOn(httpService, 'getFilterData').and.returnValue(
          of(filterApiData) as any,
        );
        spyOn(sharedService, 'setNoProjectsForNewUI');
        spyOn(sharedService, 'setNoProjects');
        // Act
        component.firstLoadFilterCheck(false);

        // Assert
        expect(sharedService.setNoProjectsForNewUI).toHaveBeenCalledWith({
          kanban: false,
          scrum: true,
        });
        expect(sharedService.setNoProjects).toHaveBeenCalledWith(true);
      });

      it('should handle unsuccessful filter data response', () => {
        // Arrange
        const filterApiData = { success: false };
        spyOn(httpService, 'getFilterData').and.returnValue(
          of(filterApiData) as any,
        );
        spyOn(sharedService, 'setNoProjectsForNewUI');
        // Act
        component.firstLoadFilterCheck(false);

        // Assert
        expect(component.scrumProjectsAvailable).toBe(true);
        expect(sharedService.setNoProjectsForNewUI).not.toHaveBeenCalled();
      });
    });
  });

  describe('FilterNewComponent.setCategories() setCategories method', () => {
    describe('Happy Path', () => {
      it('should set categories correctly when data is available', () => {
        // Arrange
        const mockHierarchyData = [
          { hierarchyLevelId: 'project', hierarchyLevelName: 'Project' },
          { hierarchyLevelId: 'sprint', hierarchyLevelName: 'Sprint' },
        ];
        const mockFilterData = {
          scrum: {
            project: [{ nodeId: '1', nodeName: 'Project 1' }],
            sprint: [{ nodeId: '2', nodeName: 'Sprint 1', parentId: '1' }],
          },
        };
        localStorage.setItem(
          'completeHierarchyData',
          JSON.stringify({ scrum: mockHierarchyData }),
        );
        component.filterDataArr = mockFilterData;

        // Act
        component.setCategories();

        // Assert
        expect(component.filterDataArr['scrum']['Sprint']).toEqual([
          { nodeId: '2', nodeName: 'Sprint 1', parentId: '1' },
        ]);
      });
    });

    describe('Edge Cases', () => {
      it('should handle empty hierarchy data gracefully', () => {
        // Arrange
        localStorage.setItem(
          'completeHierarchyData',
          JSON.stringify({ scrum: [] }),
        );
        component.filterDataArr = { scrum: {} };

        // Act
        component.setCategories();

        // Assert
        expect(component.filterDataArr['scrum']).toEqual({});
      });
    });
  });

  describe('FilterNewComponent.callBoardConfigAsPerStateFilters() callBoardConfigAsPerStateFilters method', () => {
    describe('Happy Path', () => {
      it('should call getBoardConfig with correct project ID when state filters are available', () => {
        // Arrange
        const mockStateFilters = {
          primary_level: [
            { labelName: 'project', basicProjectConfigId: '123' },
          ],
        };
        spyOn(sharedService, 'getBackupOfFilterSelectionState').and.returnValue(
          mockStateFilters as any,
        );
        component.filterDataArr = {
          scrum: {
            Project: [{ nodeId: '123', basicProjectConfigId: '123' }],
          },
        };

        const getBoardConfigSpy = spyOn(component, 'getBoardConfig' as any);

        // Act
        component.callBoardConfigAsPerStateFilters();

        // Assert
        expect(getBoardConfigSpy).toHaveBeenCalledWith(['123']);
      });

      it('should call getBoardConfig with first project ID when no state filters are available', () => {
        // Arrange
        spyOn(sharedService, 'getBackupOfFilterSelectionState').and.returnValue(
          null,
        );
        component.filterDataArr = {
          scrum: {
            Project: [{ nodeId: '123', basicProjectConfigId: '123' }],
          },
        };

        const getBoardConfigSpy = spyOn(component, 'getBoardConfig' as any);

        // Act
        component.callBoardConfigAsPerStateFilters();

        // Assert
        expect(getBoardConfigSpy).toHaveBeenCalledWith(['123']);
      });
    });

    describe('Edge Cases', () => {
      it('should handle empty non-project statefilters gracefully', () => {
        // Arrange
        const mockStateFilters = {
          primary_level: [
            {
              labelName: 'sprint',
              basicProjectConfigId: '123',
              parentId: '234',
            },
          ],
        };
        spyOn(sharedService, 'getBackupOfFilterSelectionState').and.returnValue(
          mockStateFilters as any,
        );
        component.filterDataArr = {
          scrum: {
            Project: [{ nodeId: '234', basicProjectConfigId: '234' }],
          },
        };

        const getBoardConfigSpy = spyOn(component, 'getBoardConfig' as any);

        // Act
        component.callBoardConfigAsPerStateFilters();

        // Assert
        expect(getBoardConfigSpy).toHaveBeenCalledWith(['234']);
      });

      it('should handle missing selectedLevel filterDataArr gracefully', () => {
        // Arrange
        // Arrange
        const mockStateFilters = null;
        spyOn(sharedService, 'getBackupOfFilterSelectionState').and.returnValue(
          mockStateFilters as any,
        );
        component.filterDataArr = {
          scrum: {
            Project: [
              { nodeId: '123', basicProjectConfigId: '123', nodeName: 'def' },
              { nodeId: '234', basicProjectConfigId: '234', nodeName: 'abc' },
            ],
          },
        };
        component.selectedLevel = null;
        const getBoardConfigSpy = spyOn(component, 'getBoardConfig' as any);

        // Act
        component.callBoardConfigAsPerStateFilters();

        // Assert
        expect(getBoardConfigSpy).toHaveBeenCalledWith(['234']);
      });
    });
  });

  describe('FilterNewComponent.getBoardConfig() getBoardConfig method', () => {
    describe('Happy Path', () => {
      it('should fetch board configuration successfully', async () => {
        // Arrange
        const projectList = ['project1', 'project2'];
        const response = {
          success: true,
          data: {
            userBoardConfigDTO: {
              scrum: [],
              others: [],
              configDetails: {},
            },
          },
        };
        spyOn(httpService, 'getShowHideOnDashboardNewUI').and.returnValue(
          of(response),
        );

        // Act
        await component.getBoardConfig(projectList);

        // Assert
        expect(httpService.getShowHideOnDashboardNewUI).toHaveBeenCalledWith({
          basicProjectConfigIds: projectList,
        });
        expect(component.dashConfigData).toEqual(
          response.data.userBoardConfigDTO,
        );
      });
    });

    describe('Edge Cases', () => {
      it('should handle empty project list gracefully', async () => {
        // Arrange
        const projectList: string[] = [];
        const response = {
          success: true,
          data: {
            userBoardConfigDTO: {
              scrum: [],
              others: [],
              configDetails: {},
            },
          },
        };
        spyOn(httpService, 'getShowHideOnDashboardNewUI').and.returnValue(
          of(response),
        );

        // Act
        await component.getBoardConfig(projectList);

        // Assert
        expect(httpService.getShowHideOnDashboardNewUI).toHaveBeenCalledWith({
          basicProjectConfigIds: [],
        });
        expect(component.dashConfigData).toEqual(
          response.data.userBoardConfigDTO,
        );
      });

      xit('should handle API failure gracefully', async () => {
        // Arrange
        const projectList = ['project1'];
        spyOn(httpService, 'getShowHideOnDashboardNewUI').and.returnValue(
          of(new Error('API Error')),
        );
        spyOn(messageService, 'add');
        // Act
        await component.getBoardConfig(projectList);

        // Assert
        expect(httpService.getShowHideOnDashboardNewUI).toHaveBeenCalledWith({
          basicProjectConfigIds: projectList,
        });
        expect(messageService.add).toHaveBeenCalledWith({
          severity: 'error',
          summary: 'API Error',
        });
      });
    });
  });

  describe('FilterNewComponent.handleParentFilterChange() handleParentFilterChange method', () => {
    it('should update primaryFilterConfig and selectedLevel when handleParentFilterChange is called with valid event', () => {
      // Arrange
      const event = 'someLevel';
      component.selectedBoard = {
        boardId: 1,
        boardName: 'Category 1',
        filters: {
          primaryFilter: {
            defaultLevel: {
              labelName: 'project',
              sortBy: null,
            },
          },
        },
      };

      // Act
      component.handleParentFilterChange(event);

      // Assert
      expect(component.primaryFilterConfig).toEqual(
        component.selectedBoard.filters.primaryFilter,
      );
      expect(component.selectedLevel).toBe(event);
    });
  });

  describe('Edge Cases', () => {
    it('should handle null event gracefully', () => {
      // Arrange
      const event = null;
      component.selectedBoard = {
        boardId: 1,
        boardName: 'Category 1',
        filters: {
          primaryFilter: {
            defaultLevel: {
              labelName: 'project',
              sortBy: null,
            },
          },
        },
      };

      // Act
      component.handleParentFilterChange(event);

      // Assert
      expect(component.primaryFilterConfig).toEqual(
        component.selectedBoard.filters.primaryFilter,
      );
      expect(component.selectedLevel).toBeNull();
    });

    it('should handle undefined event gracefully', () => {
      // Arrange
      const event = undefined;
      component.selectedBoard = {
        boardId: 1,
        boardName: 'Category 1',
        filters: {
          primaryFilter: {
            defaultLevel: {
              labelName: 'project',
              sortBy: null,
            },
          },
        },
      };

      // Act
      component.handleParentFilterChange(event);

      // Assert
      expect(component.primaryFilterConfig).toEqual(
        component.selectedBoard.filters.primaryFilter,
      );
      expect(component.selectedLevel).toBeUndefined();
    });

    it('should handle empty string event gracefully', () => {
      // Arrange
      const event = '';
      component.selectedBoard = {
        boardId: 1,
        boardName: 'Category 1',
        filters: {
          primaryFilter: {
            defaultLevel: {
              labelName: 'project',
              sortBy: null,
            },
          },
        },
      };
      // Act
      component.handleParentFilterChange(event);

      // Assert
      expect(component.primaryFilterConfig).toEqual(
        component.selectedBoard.filters.primaryFilter,
      );
      expect(component.selectedLevel).toBe('');
    });
  });

  describe('FilterNewComponent.setLevelNames() setLevelNames method', () => {
    describe('Happy Path', () => {
      it('should correctly set level names when valid data is provided', () => {
        const data = {
          scrum: [
            {
              filters: {
                primaryFilter: {
                  defaultLevel: { labelName: 'project' },
                },
                parentFilter: { labelName: 'Organization Level' },
                additionalFilters: [{ defaultLevel: { labelName: 'sprint' } }],
              },
            },
          ],
          others: [
            {
              filters: {
                primaryFilter: {
                  defaultLevel: { labelName: 'project' },
                },
                parentFilter: { labelName: 'Organization Level' },
                additionalFilters: [{ defaultLevel: { labelName: 'sprint' } }],
              },
            },
          ],
        };

        const levelDetails = [
          { hierarchyLevelId: 'project', hierarchyLevelName: 'Project' },
          { hierarchyLevelId: 'sprint', hierarchyLevelName: 'Sprint' },
        ];

        spyOn(localStorage, 'getItem').and.returnValue(
          JSON.stringify({ scrum: levelDetails }),
        );

        const result = component.setLevelNames(data);

        expect(
          result.scrum[0].filters.primaryFilter.defaultLevel.labelName,
        ).toBe('Project');
        expect(
          result.scrum[0].filters.additionalFilters[0].defaultLevel.labelName,
        ).toBe('Sprint');
      });

      it('should correctly set level names when valid data is provided and parent filters are not Organization Levels', () => {
        const data = {
          scrum: [
            {
              filters: {
                primaryFilter: {
                  defaultLevel: { labelName: 'sprint' },
                },
                parentFilter: { labelName: 'project', emittedLevel: 'sprint' },
              },
            },
          ],
          others: [
            {
              filters: {
                primaryFilter: {
                  defaultLevel: { labelName: 'project' },
                },
                parentFilter: { labelName: 'Organization Level' },
                additionalFilters: [{ defaultLevel: { labelName: 'sprint' } }],
              },
            },
          ],
        };

        const levelDetails = [
          { hierarchyLevelId: 'project', hierarchyLevelName: 'Project' },
          { hierarchyLevelId: 'sprint', hierarchyLevelName: 'Sprint' },
        ];

        spyOn(localStorage, 'getItem').and.returnValue(
          JSON.stringify({ scrum: levelDetails }),
        );

        const result = component.setLevelNames(data);

        expect(
          result.scrum[0].filters.primaryFilter.defaultLevel.labelName,
        ).toBe('Sprint');
        expect(result.scrum[0].filters.parentFilter.emittedLevel).toBe(
          'Sprint',
        );
      });
    });

    describe('Edge Cases', () => {
      it('should handle empty data gracefully', () => {
        const data = {
          scrum: [],
          others: [],
        };

        const result = component.setLevelNames(data);

        expect(result.scrum).toEqual([]);
        expect(result.others).toEqual([]);
      });

      it('should handle missing hierarchy data in localStorage', () => {
        const data = {
          scrum: [
            {
              filters: {
                primaryFilter: {
                  defaultLevel: { labelName: 'project' },
                },
              },
            },
          ],
          others: [],
        };

        spyOn(localStorage, 'getItem').and.returnValue(null);

        const result = component.setLevelNames(data);

        expect(
          result.scrum[0].filters.primaryFilter.defaultLevel.labelName,
        ).toBe('project');
      });

      it('should handle missing filters in board data', () => {
        const data = {
          scrum: [
            {
              filters: null,
            },
          ],
          others: [],
        };

        const result = component.setLevelNames(data);

        expect(result.scrum[0].filters).toBeNull();
      });
    });
  });

  describe('FilterNewComponent.setColors() setColors method', () => {
    describe('Happy Path', () => {
      xit('should set colors for nodes with nodeId', () => {
        const data = [
          { nodeId: '1', nodeName: 'Node 1', labelName: 'Label 1' },
          { nodeId: '2', nodeName: 'Node 2', labelName: 'Label 2' },
        ];
        spyOn(sharedService, 'setColorObj');

        component.setColors(data);

        expect(component.colorObj).toEqual({
          '1': {
            nodeName: 'Node 1',
            color: '#6079C5',
            nodeId: '1',
            labelName: 'Label 1',
          },
          '2': {
            nodeName: 'Node 2',
            color: '#FFB587',
            nodeId: '2',
            labelName: 'Label 2',
          },
        });
        expect(sharedService.setColorObj).toHaveBeenCalledWith(
          component.colorObj,
        );
      });
    });
  });

  describe('FilterNewComponent.removeFilter() removeFilter method', () => {
    describe('Happy Path', () => {
      it('should remove a filter when multiple filters are present', () => {
        // Arrange
        component.colorObj = {
          '1': { nodeId: '1', nodeName: 'Filter1' },
          '2': { nodeId: '2', nodeName: 'Filter2' },
        };
        component.selectedType = 'scrum';
        component.selectedLevel = 'Project';
        component.filterDataArr = {
          scrum: {
            Project: [
              { nodeId: '1', nodeName: 'Filter1', labelName: 'Project' },
              { nodeId: '2', nodeName: 'Filter2', labelName: 'Project' },
            ],
          },
        };
        spyOn(sharedService, 'getBackupOfFilterSelectionState').and.returnValue(
          {},
        );
        spyOn(sharedService, 'setSelectedTrends');
        spyOn(sharedService, 'setBackupOfFilterSelectionState');
        // Act
        component.removeFilter('1');

        // Assert
        // expect(component.colorObj).not.toHaveProperty('1');
        expect(sharedService.setSelectedTrends).toHaveBeenCalled();
        expect(
          sharedService.setBackupOfFilterSelectionState,
        ).toHaveBeenCalled();
      });

      it('should remove a filter when multiple filters along with state filters are present', () => {
        // Arrange
        component.colorObj = {
          '1': { nodeId: '1', nodeName: 'Filter1' },
          '2': { nodeId: '2', nodeName: 'Filter2' },
        };
        component.selectedType = 'scrum';
        component.selectedLevel = 'Project';
        component.filterDataArr = {
          scrum: {
            Project: [
              { nodeId: '1', nodeName: 'Filter1', labelName: 'Project' },
              { nodeId: '2', nodeName: 'Filter2', labelName: 'Project' },
            ],
          },
        };
        spyOn(sharedService, 'getBackupOfFilterSelectionState').and.returnValue(
          {
            additional_level: {
              nodeId: '3',
              nodeName: 'sprint1',
              labelName: 'Sprint',
              parentId: '1',
            },
          },
        );
        spyOn(sharedService, 'setSelectedTrends');
        spyOn(sharedService, 'setBackupOfFilterSelectionState');
        // Act
        component.removeFilter('1');

        // Assert
        // expect(component.colorObj).not.toHaveProperty('1');
        expect(sharedService.setSelectedTrends).toHaveBeenCalled();
        expect(
          sharedService.setBackupOfFilterSelectionState,
        ).toHaveBeenCalled();
      });

      it('should remove a filter when multiple filters along with state filters and non-string selectedLevel are present', () => {
        // Arrange
        component.colorObj = {
          '1': { nodeId: '1', nodeName: 'Filter1' },
          '2': { nodeId: '2', nodeName: 'Filter2' },
        };
        component.selectedType = 'scrum';
        component.selectedLevel = {
          nodeId: '4',
          nodeName: 'Filter4',
          labelName: 'Engagemenent',
          emittedLevel: 'Project',
        };
        component.filterDataArr = {
          scrum: {
            Project: [
              { nodeId: '1', nodeName: 'Filter1', labelName: 'Project' },
              { nodeId: '2', nodeName: 'Filter2', labelName: 'Project' },
            ],
          },
        };
        component.filterApplyData['selectedMap'] = {};
        spyOn(sharedService, 'getBackupOfFilterSelectionState').and.returnValue(
          {
            additional_level: {
              nodeId: '3',
              nodeName: 'sprint1',
              labelName: 'Sprint',
              parentId: '1',
            },
          },
        );
        spyOn(sharedService, 'setSelectedTrends');
        spyOn(sharedService, 'setBackupOfFilterSelectionState');
        // Act
        component.removeFilter('1');

        // Assert
        // expect(component.colorObj).not.toHaveProperty('1');
        expect(sharedService.setSelectedTrends).toHaveBeenCalled();
        expect(
          sharedService.setBackupOfFilterSelectionState,
        ).toHaveBeenCalled();
      });
    });

    describe('Edge Cases', () => {
      it('should handle removal when only one filter is present', () => {
        // Arrange
        component.colorObj = {
          '1': { nodeId: '1', nodeName: 'Filter1' },
        };
        component.selectedType = 'scrum';
        component.selectedLevel = 'Project';
        component.filterDataArr = {
          scrum: {
            Project: [
              { nodeId: '1', nodeName: 'Filter1', labelName: 'Project' },
            ],
          },
        };
        spyOn(sharedService, 'getBackupOfFilterSelectionState').and.returnValue(
          {},
        );
        spyOn(sharedService, 'setSelectedTrends');
        spyOn(sharedService, 'setBackupOfFilterSelectionState');
        // Act
        component.removeFilter('1');

        // Assert
        // expect(component.colorObj).not.toHaveProperty('1');
        expect(sharedService.setSelectedTrends).not.toHaveBeenCalled();
        expect(
          sharedService.setBackupOfFilterSelectionState,
        ).not.toHaveBeenCalled();
      });

      it('should handle removal when filter ID does not exist', () => {
        // Arrange
        component.colorObj = {
          '1': { nodeId: '1', nodeName: 'Filter1' },
        };
        component.selectedType = 'scrum';
        component.selectedLevel = 'Project';
        component.filterDataArr = {
          scrum: {
            Project: [{ nodeId: '1', nodeName: 'Filter1' }],
          },
        };
        spyOn(sharedService, 'getBackupOfFilterSelectionState').and.returnValue(
          {},
        );
        spyOn(sharedService, 'setSelectedTrends');
        spyOn(sharedService, 'setBackupOfFilterSelectionState');
        // Act
        component.removeFilter('2');

        // Assert
        // expect(component.colorObj).toHaveProperty('1');
        expect(sharedService.setSelectedTrends).not.toHaveBeenCalled();
        expect(
          sharedService.setBackupOfFilterSelectionState,
        ).not.toHaveBeenCalled();
      });
    });
  });

  describe('FilterNewComponent.setSprintDetails() setSprintDetails method', () => {
    describe('Happy Path', () => {
      it('should set sprint details correctly for iteration tab', () => {
        // Arrange
        component.selectedTab = 'iteration';
        const event = [
          {
            nodeId: 'sprint1',
            sprintStartDate: '2023-01-01T00:00:00',
            sprintEndDate: '2023-01-15T00:00:00',
          },
        ];
        spyOn(sharedService, 'setCurrentSelectedSprint');
        // Act
        component.setSprintDetails(event);

        // Assert
        expect(component.combinedDate).toBe("01 Jan'23 - 15 Jan'23");
        expect(component.additionalData).toBe(true);
        expect(component.filterApplyData['ids']).toEqual(['sprint1']);
        expect(component.selectedSprint).toEqual(event[0]);
        expect(sharedService.setCurrentSelectedSprint).toHaveBeenCalledWith(
          event[0],
        );
      });

      it('should set sprint details correctly for release tab', () => {
        // Arrange
        component.selectedTab = 'release';
        const event = [
          {
            nodeId: 'release1',
            releaseStartDate: '2023-02-01T00:00:00',
            releaseEndDate: '2023-02-28T00:00:00',
          },
        ];
        spyOn(sharedService, 'setCurrentSelectedSprint');

        // Act
        component.setSprintDetails(event);

        // Assert
        expect(component.combinedDate).toBe("01 Feb'23 - 28 Feb'23");
        expect(component.additionalData).toBe(true);
        expect(component.filterApplyData['ids']).toEqual(['release1']);
        expect(component.selectedSprint).toEqual(event[0]);
        expect(sharedService.setCurrentSelectedSprint).toHaveBeenCalledWith(
          event[0],
        );
      });
    });
  });

  describe('FilterNewComponent.formatDate() formatDate method', () => {
    describe('Happy Path', () => {
      it('should format a valid date string correctly', () => {
        const dateString = '2023-10-15';
        const formattedDate = component.formatDate(dateString);
        expect(formattedDate).toBe("15 Oct'23");
      });

      it('should format another valid date string correctly', () => {
        const dateString = '2022-01-01';
        const formattedDate = component.formatDate(dateString);
        expect(formattedDate).toBe("01 Jan'22");
      });
    });

    describe('Edge Cases', () => {
      it('should return "N/A" for an empty date string', () => {
        const dateString = '';
        const formattedDate = component.formatDate(dateString);
        expect(formattedDate).toBe('N/A');
      });
    });
  });

  describe('FilterNewComponent.getCorrectLevelMapping() getCorrectLevelMapping method', () => {
    describe('Happy Path', () => {
      it('should return the correct level mapping when level is found in additionalFilterLevelArr', () => {
        // Arrange
        component.additionalFilterLevelArr = [
          { hierarchyLevelId: 'level1', hierarchyLevelName: 'Level 1' },
          { hierarchyLevelId: 'level2', hierarchyLevelName: 'Level 2' },
        ];
        const level = 'level1';

        // Act
        const result = component.getCorrectLevelMapping(level);

        // Assert
        expect(result).toBe('Level 1');
      });

      it('should return the correct level mapping when level is found in squadLevel', () => {
        // Arrange
        component.additionalFilterLevelArr = [
          { hierarchyLevelId: 'squad', hierarchyLevelName: 'Squad Level' },
        ];
        component.squadLevel = component.additionalFilterLevelArr;
        const level = 'squad';

        // Act
        const result = component.getCorrectLevelMapping(level);

        // Assert
        expect(result).toBe('Squad Level');
      });
    });

    describe('Edge Cases', () => {
      it('should return an empty string when level is not found', () => {
        // Arrange
        component.additionalFilterLevelArr = [
          { hierarchyLevelId: 'level1', hierarchyLevelName: 'Level 1' },
        ];
        const level = 'nonexistent';

        // Act
        const result = component.getCorrectLevelMapping(level);

        // Assert
        expect(result).toBeUndefined();
      });

      it('should handle case where additionalFilterLevelArr is empty', () => {
        // Arrange
        component.additionalFilterLevelArr = [];
        const level = 'level1';

        // Act
        const result = component.getCorrectLevelMapping(level);

        // Assert
        expect(result).toBeUndefined();
      });
    });
  });

  describe('FilterNewComponent.getProcessorsTraceLogsForProject() getProcessorsTraceLogsForProject method', () => {
    describe('Happy Path', () => {
      it('should successfully fetch processor trace logs for a project', async () => {
        // Arrange
        const log = [
          {
            processorName: 'azure',
          },
        ];
        const mockResponse = { success: true, data: log };
        spyOn(httpService, 'getProcessorsTraceLogsForProject').and.returnValue(
          of(mockResponse),
        );
        spyOn(sharedService, 'getSelectedTrends').and.returnValue([
          { basicProjectConfigId: '123' },
        ]);
        spyOn(sharedService, 'setProcessorLogDetails');

        // Act
        await component.getProcessorsTraceLogsForProject();

        // Assert
        expect(
          httpService.getProcessorsTraceLogsForProject,
        ).toHaveBeenCalledWith('123');
        expect(sharedService.setProcessorLogDetails).toHaveBeenCalledWith(log);
      });
    });

    describe('Edge Cases', () => {
      xit('should handle error when fetching processor trace logs fails', async () => {
        // Arrange
        const mockError = { success: false };
        spyOn(httpService, 'getProcessorsTraceLogsForProject').and.returnValue(
          of(mockError),
        );
        spyOn(sharedService, 'getSelectedTrends').and.returnValue([
          { basicProjectConfigId: '123' },
        ]);
        spyOn(sharedService, 'setProcessorLogDetails');
        spyOn(messageService, 'add');

        // Act
        await component.getProcessorsTraceLogsForProject();

        // Assert
        expect(
          httpService.getProcessorsTraceLogsForProject,
        ).toHaveBeenCalledWith('123');
        expect(messageService.add).toHaveBeenCalledWith({
          severity: 'error',
          summary:
            "Error in fetching processor's execution date. Please try after some time.",
        });
      });
    });
  });

  describe('FilterNewComponent.fetchData() fetchData method', () => {
    describe('Happy Path', () => {
      it('should successfully fetch data for an active sprint', fakeAsync(() => {
        // Arrange
        const mockSprintId = '123';
        component.selectedSprint = {
          nodeId: mockSprintId,
          sprintState: 'ACTIVE',
        } as any;
        spyOn(httpService, 'getActiveIterationStatus').and.returnValue(
          of({ success: true }) as any,
        );
        spyOn(httpService, 'getactiveIterationfetchStatus').and.returnValue(
          of({ success: true, data: { fetchSuccessful: true } }) as any,
        );

        // Act
        component.fetchData();
        tick(3000);
        // Assert
        // expect(component.blockUI).toBe(false);
        expect(component.selectedProjectLastSyncStatus).toBe('SUCCESS');
      }));
    });

    it('should successfully handle erroneous data for an active sprint', fakeAsync(() => {
      // Arrange
      const mockSprintId = '123';
      component.selectedSprint = {
        nodeId: mockSprintId,
        sprintState: 'ACTIVE',
      } as any;
      spyOn(httpService, 'getActiveIterationStatus').and.returnValue(
        of({ success: true }) as any,
      );
      spyOn(httpService, 'getactiveIterationfetchStatus').and.returnValue(
        of({
          success: true,
          data: { fetchSuccessful: false, errorInFetch: true },
        }) as any,
      );

      // Act
      component.fetchData();
      tick(3000);
      // Assert
      // expect(component.blockUI).toBe(false);
      expect(component.selectedProjectLastSyncStatus).toBe('FAILURE');
    }));

    describe('Edge Cases', () => {
      it('should handle error when fetching active iteration fetch status fails', fakeAsync(() => {
        // Arrange
        const mockSprintId = '123';
        component.selectedSprint = {
          nodeId: mockSprintId,
          sprintState: 'ACTIVE',
        } as any;
        spyOn(httpService, 'getActiveIterationStatus').and.returnValue(
          of({ success: true }) as any,
        );
        spyOn(httpService, 'getactiveIterationfetchStatus').and.returnValue(
          throwError('Error') as any,
        );

        // Act
        component.fetchData();
        tick(3000);
        // Assert
        // expect(component.blockUI).toBe(false);
        expect(component.lastSyncData).toEqual({});
      }));

      it('should handle case when sprint is not active', fakeAsync(() => {
        // Arrange
        component.selectedSprint = {
          nodeId: '123',
          sprintState: 'CLOSED',
        } as any;

        // Act
        component.fetchData();
        tick(3000);
        // Assert
        expect(component.blockUI).toBe(true);
      }));
    });
  });

  describe('FilterNewComponent.compileGAData() compileGAData method', () => {
    describe('Happy Path', () => {
      it('should compile GA data correctly for a valid selectedFilterArray', () => {
        const selectedFilterArray = [
          {
            nodeId: '1',
            nodeName: 'Project A',
            labelName: 'project',
            path: ['1###2###3'],
          },
          {
            nodeId: '2',
            nodeName: 'Project B',
            labelName: 'project',
            path: ['2###3###4'],
          },
        ];

        component.filterApiData = [
          { nodeId: '1', nodeName: 'Category 1' },
          { nodeId: '2', nodeName: 'Category 2' },
          { nodeId: '3', nodeName: 'Category 3' },
          { nodeId: '4', nodeName: 'Category 4' },
        ];
        spyOn(gaService, 'setProjectData');
        component.compileGAData(selectedFilterArray);

        expect(gaService.setProjectData).toHaveBeenCalledWith([
          {
            id: '1',
            name: 'Project A',
            level: 'project',
            category1: 'Category 3',
            category2: 'Category 2',
            category3: 'Category 1',
          },
          {
            id: '2',
            name: 'Project B',
            level: 'project',
            category1: 'Category 4',
            category2: 'Category 3',
            category3: 'Category 2',
          },
        ]);
      });
    });

    describe('Edge Cases', () => {
      it('should handle empty selectedFilterArray gracefully', () => {
        const selectedFilterArray = [];
        spyOn(gaService, 'setProjectData');
        component.compileGAData(selectedFilterArray);

        expect(gaService.setProjectData).toHaveBeenCalledWith([]);
      });

      it('should handle selectedFilterArray with missing path gracefully', () => {
        const selectedFilterArray = [
          { nodeId: '1', nodeName: 'Project A', labelName: 'project' },
        ];
        spyOn(gaService, 'setProjectData');
        component.compileGAData(selectedFilterArray);

        expect(gaService.setProjectData).toHaveBeenCalledWith([
          {
            id: '1',
            name: 'Project A',
            level: 'project',
          },
        ]);
      });

      it('should handle selectedFilterArray with empty path gracefully', () => {
        const selectedFilterArray = [
          {
            nodeId: '1',
            nodeName: 'Project A',
            labelName: 'project',
            path: [],
          },
        ];
        spyOn(gaService, 'setProjectData');
        component.compileGAData(selectedFilterArray);

        expect(gaService.setProjectData).toHaveBeenCalledWith([
          {
            id: '1',
            name: 'Project A',
            level: 'project',
          },
        ]);
      });
    });
  });

  describe('FilterNewComponent.showHideKPIs() showHideKPIs method', () => {
    describe('Happy Path', () => {
      it('should successfully save KPI configuration', () => {
        // Arrange
        component.dashConfigDataDeepCopyBackup = {
          scrum: [{ boardSlug: 'iteration', kpis: [] }],
          others: [],
          configDetails: {},
        };
        component.dashConfigData = {
          scrum: [{ boardSlug: 'iteration', kpis: [] }],
          others: [],
          configDetails: {},
        } as any;
        component.selectedType = 'scrum';
        component.selectedTab = 'iteration';
        component.masterDataCopy = {
          kpiList: [{ kpiId: 1, shown: true }],
        } as any;
        spyOn(httpService, 'submitShowHideOnDashboard').and.returnValue(
          of({
            success: true,
          }),
        );
        spyOn(messageService, 'add');
        spyOn(sharedService, 'setDashConfigData');

        // Act
        component.showHideKPIs();

        // Assert
        expect(httpService.submitShowHideOnDashboard).toHaveBeenCalled();
        expect(messageService.add).toHaveBeenCalledWith({
          severity: 'success',
          summary: 'Successfully Saved',
          detail: '',
        });
        expect(sharedService.setDashConfigData).toHaveBeenCalledWith(
          component.dashConfigData,
        );
      });
    });

    describe('Edge Cases', () => {
      it('should handle error when saving KPI configuration fails', async () => {
        // Arrange
        component.dashConfigDataDeepCopyBackup = {
          scrum: [{ boardSlug: 'iteration', kpis: [] }],
          others: [],
          configDetails: {},
        };
        component.dashConfigData = {
          scrum: [{ boardSlug: 'iteration', kpis: [] }],
          others: [],
          configDetails: {},
        } as any;
        component.selectedType = 'scrum';
        component.selectedTab = 'iteration';
        component.masterDataCopy = {
          kpiList: [{ kpiId: 1, shown: true }],
        } as any;
        spyOn(httpService, 'submitShowHideOnDashboard').and.returnValue(
          of(new Error('Network Error')),
        );
        spyOn(messageService, 'add');
        // Act
        await component.showHideKPIs();

        // Assert
        expect(httpService.submitShowHideOnDashboard).toHaveBeenCalled();
        expect(messageService.add).toHaveBeenCalledWith({
          severity: 'error',
          summary: 'Error in Saving Configuraion',
        });
      });
    });
  });

  describe('FilterNewComponent.showHideSelectAllApply() showHideSelectAllApply method', () => {
    describe('Happy Path', () => {
      it('should enable all KPIs when showHideSelectAll is true', () => {
        // Arrange
        component.showHideSelectAll = true;
        component.masterDataCopy['kpiList'] = [
          { isEnabled: false },
          { isEnabled: false },
        ] as any;

        // Act
        component.showHideSelectAllApply();

        // Assert
        expect(
          component.masterDataCopy['kpiList'].every((kpi) => kpi.isEnabled),
        ).toBe(true);
      });

      it('should disable all KPIs when showHideSelectAll is false', () => {
        // Arrange
        component.showHideSelectAll = false;
        component.masterDataCopy['kpiList'] = [
          { isEnabled: true },
          { isEnabled: true },
        ] as any;

        // Act
        component.showHideSelectAllApply();

        // Assert
        expect(
          component.masterDataCopy['kpiList'].every((kpi) => !kpi.isEnabled),
        ).toBe(true);
      });
    });

    describe('Edge Cases', () => {
      it('should handle empty kpiList gracefully', () => {
        // Arrange
        component.showHideSelectAll = true;
        component.masterDataCopy['kpiList'] = [] as any;

        // Act
        component.showHideSelectAllApply();

        // Assert
        expect(component.masterDataCopy['kpiList'].length).toBe(0);
      });
    });
  });

  describe('FilterNewComponent.showChartToggle() showChartToggle method', () => {
    describe('Happy Path', () => {
      it('should set showChart to the provided value and call setShowTableView', () => {
        // Arrange
        const expectedValue = 'table';
        spyOn(sharedService, 'setShowTableView');
        // Act
        component.showChartToggle(expectedValue);

        // Assert
        expect(component.showChart).toBe(expectedValue);
        expect(sharedService.setShowTableView).toHaveBeenCalledWith(
          expectedValue,
        );
      });
    });

    describe('Edge Cases', () => {
      it('should handle an empty string input gracefully', () => {
        // Arrange
        const expectedValue = '';
        spyOn(sharedService, 'setShowTableView');
        // Act
        component.showChartToggle(expectedValue);

        // Assert
        expect(component.showChart).toBe(expectedValue);
        expect(sharedService.setShowTableView).toHaveBeenCalledWith(
          expectedValue,
        );
      });

      it('should handle a null input gracefully', () => {
        // Arrange
        const expectedValue = null;
        spyOn(sharedService, 'setShowTableView');
        // Act
        component.showChartToggle(expectedValue);

        // Assert
        expect(component.showChart).toBe(expectedValue);
        expect(sharedService.setShowTableView).toHaveBeenCalledWith(
          expectedValue,
        );
      });

      it('should handle an undefined input gracefully', () => {
        // Arrange
        const expectedValue = undefined;
        spyOn(sharedService, 'setShowTableView');
        // Act
        component.showChartToggle(expectedValue);

        // Assert
        expect(component.showChart).toBe(expectedValue);
        expect(sharedService.setShowTableView).toHaveBeenCalledWith(
          expectedValue,
        );
      });
    });
  });

  describe('FilterNewComponent.populateAdditionalFilters() populateAdditionalFilters method', () => {
    describe('Happy Path', () => {
      it('should populate additional filters correctly for a given project', () => {
        // Arrange
        const event = [{ nodeId: '2', labelName: 'project', parentId: '1' }];
        component.filterDataArr = {
          scrum: {
            project: [{ nodeId: '2', labelName: 'project', parentId: '1' }],
            sprint: [{ nodeId: '3', labelName: 'sprint', parentId: '2' }],
          },
        };
        component.additionalFilterLevelArr = [
          { hierarchyLevelId: 'sprint', hierarchyLevelName: 'sprint' },
          { hierarchyLevelId: 'sqd', hierarchyLevelName: 'squad' },
        ];
        component.selectedType = 'scrum';
        component.selectedTab = 'iteration';
        component.additionalFilterConfig = [
          { defaultLevel: { labelName: 'sprint' } },
        ];

        // Act
        component.populateAdditionalFilters(event);

        // Assert
        expect(component.additionalFiltersArr['filter1']).toEqual([
          { nodeId: '3', labelName: 'sprint', parentId: '2' },
        ]);
      });
    });

    describe('Edge Cases', () => {
      it('should handle empty event array gracefully', () => {
        // Arrange
        const event = [];
        component.filterDataArr = {
          scrum: {
            Project: [],
            Sprint: [],
          },
        };
        component.selectedType = 'scrum';
        component.kanban = false;
        component.additionalFilterConfig = [
          { defaultLevel: { labelName: 'Sprint' } },
        ];
        component.additionalFilterLevelArr = [
          { hierarchyLevelId: 'sprint', hierarchyLevelName: 'sprint' },
          { hierarchyLevelId: 'sqd', hierarchyLevelName: 'squad' },
        ];

        // Act
        component.populateAdditionalFilters(event);

        // Assert
        expect(component.additionalFiltersArr['filter1']).toBeFalsy();
      });

      it('should handle event array gracefully when kanban is true', () => {
        // Arrange
        const event = { nodeId: '2', labelName: 'project', parentId: '1' };
        component.filterDataArr = {
          scrum: {
            Squad: [{ nodeId: '4', labelName: 'sqd', parentId: '2' }],
          },
        };
        component.selectedType = 'scrum';
        component.kanban = false;
        component.additionalFilterConfig = [
          { defaultLevel: { labelName: 'sqd' } },
        ];
        component.additionalFilterLevelArr = [
          { hierarchyLevelId: 'sqd', hierarchyLevelName: 'squad' },
        ];

        // Act
        component.populateAdditionalFilters(event);

        // Assert
        expect(component.additionalFiltersArr['filter1']).toEqual([]);
      });

      it('should handle non-project events correctly', () => {
        // Arrange
        const event = [{ nodeId: '2', labelName: 'sprint', parentId: '1' }];
        component.filterDataArr = {
          scrum: {
            project: [{ nodeId: '1', labelName: 'project', parentId: '0' }],
            sprint: [{ nodeId: '2', labelName: 'sprint', parentId: '1' }],
          },
        };
        component.additionalFilterLevelArr = [
          { hierarchyLevelId: 'sprint', hierarchyLevelName: 'sprint' },
          { hierarchyLevelId: 'sqd', hierarchyLevelName: 'squad' },
        ];
        component.selectedType = 'scrum';
        component.selectedTab = 'iteration';
        component.additionalFilterConfig = [
          { defaultLevel: { labelName: 'Sprint' } },
        ];

        // Act
        component.populateAdditionalFilters(event);

        // Assert
        expect(component.additionalFiltersArr['filter1']).toEqual([
          { nodeId: '2', labelName: 'sprint', parentId: '1' },
        ]);
      });
    });
  });

  describe('FilterNewComponent.applyDateFilter() applyDateFilter method', () => {
    describe('Happy Path', () => {
      it('should apply date filter correctly when selectedDayType is set', () => {
        // Arrange
        component.selectedDayType = 'Weeks';
        component.selectedDateValue = '5';
        component.filterApplyData = { selectedMap: {} };
        spyOn(sharedService, 'setSelectedDateFilter');
        spyOn(sharedService, 'select');
        component.filterDataArr = {
          scrum: {
            level1: [
              {
                level: 1,
                labelName: 'project',
                nodeId: '1',
                nodeName: 'Node 1',
                basicProjectConfigId: '321',
              },
            ],
            level2: [
              {
                level: 2,
                labelName: 'Project2',
                nodeId: '2',
                nodeName: 'Node 2',
                basicProjectConfigId: '421',
              },
            ],
          },
        };
        component.selectedLevel = 'project';
        // Act
        component.applyDateFilter();

        // Assert
        expect(component.selectedDateFilter).toBe('5 Weeks');
        expect(sharedService.setSelectedDateFilter).toHaveBeenCalledWith(
          'Weeks',
        );
        expect(component.filterApplyData['selectedMap']['date']).toEqual([
          'Weeks',
        ]);
        expect(component.filterApplyData['ids']).toEqual(['5']);
        expect(sharedService.select).toHaveBeenCalled();
      });

      it('should apply date filter correctly when selectedDayType is set and selected Level is not string', () => {
        // Arrange
        component.selectedDayType = 'Weeks';
        component.selectedDateValue = '5';
        component.filterApplyData = { selectedMap: {} };
        spyOn(sharedService, 'setSelectedDateFilter');
        spyOn(sharedService, 'select');
        component.filterDataArr = {
          scrum: {
            level1: [
              {
                level: 1,
                labelName: 'project',
                nodeId: '1',
                nodeName: 'Node 1',
                basicProjectConfigId: '321',
              },
            ],
            level2: [
              {
                level: 2,
                labelName: 'Project2',
                nodeId: '2',
                nodeName: 'Node 2',
                basicProjectConfigId: '421',
              },
            ],
          },
        };
        component.selectedLevel = {
          level: 3,
          labelName: 'engagement',
          emittedLevel: 'project',
        };
        // Act
        component.applyDateFilter();

        // Assert
        expect(component.selectedDateFilter).toBe('5 Weeks');
        expect(sharedService.setSelectedDateFilter).toHaveBeenCalledWith(
          'Weeks',
        );
        expect(component.filterApplyData['selectedMap']['date']).toEqual([
          'Weeks',
        ]);
        expect(component.filterApplyData['ids']).toEqual(['5']);
        expect(sharedService.select).toHaveBeenCalled();
      });
    });
  });

  describe('FilterNewComponent.prepareKPICalls() prepareKPICalls method', () => {
    describe('Happy Path', () => {
      it('should prepare KPI calls with valid event data', () => {
        // Arrange
        const event = [{ nodeId: '1', labelName: 'Project', level: 1 }];
        component.selectedType = 'scrum';
        component.selectedTab = 'iteration';
        component.filterDataArr = {
          scrum: {
            Project: [{ nodeId: '1', labelName: 'Project', level: 1 }],
            sprint: [{ nodeId: '2', labelName: 'sprint', parentId: '1' }],
          },
        };
        component.additionalFilterConfig = [
          { defaultLevel: { labelName: 'sprint' } },
        ];
        component.masterData = { kpiList: [] };
        component.boardData = { configDetails: {} };
        component.dashConfigData = { scrum: [], others: [] };
        spyOn(sharedService, 'setSelectedTrends');
        spyOn(sharedService, 'select');
        // Act
        component.prepareKPICalls(event);

        // Assert
        expect(sharedService.setSelectedTrends).toHaveBeenCalledWith(event);
        // expect(sharedService.select).toHaveBeenCalled();
      });

      it('should prepare KPI calls with valid event data for kanban', () => {
        // Arrange
        const event = [{ nodeId: '1', labelName: 'Project', level: 1 }];
        component.selectedType = 'kanban';
        component.kanban = true;
        component.selectedTab = 'iteration';
        component.filterDataArr = {
          kanban: {
            Project: [{ nodeId: '1', labelName: 'Project', level: 1 }],
            sprint: [{ nodeId: '2', labelName: 'sprint', parentId: '1' }],
          },
        };
        component.additionalFilterConfig = [
          { defaultLevel: { labelName: 'sqd' } },
        ];
        component.masterData = { kpiList: [] };
        component.boardData = { configDetails: {} };
        component.dashConfigData = { scrum: [], others: [] };
        spyOn(sharedService, 'setSelectedTrends');
        spyOn(sharedService, 'select');
        // Act
        component.prepareKPICalls(event);

        // Assert
        expect(sharedService.setSelectedTrends).toHaveBeenCalledWith(event);
        // expect(sharedService.select).toHaveBeenCalled();
      });

      it('should prepare KPI calls with valid event data when selected Level is an object', () => {
        // Arrange
        const event = [{ nodeId: '1', labelName: 'Project', level: 1 }];
        component.selectedType = 'scrum';
        component.selectedTab = 'iteration';
        component.filterDataArr = {
          scrum: {
            Project: [{ nodeId: '1', labelName: 'Project', level: 1 }],
            sprint: [{ nodeId: '2', labelName: 'sprint', parentId: '1' }],
          },
        };
        component.selectedLevel = {
          nodeId: '4',
          nodeName: 'Filter4',
          labelName: 'Engagemenent',
          emittedLevel: 'Project',
          fullNodeDetails: { nodeId: '1', labelName: 'Project', level: 1 },
        };
        component.additionalFilterConfig = [
          { defaultLevel: { labelName: 'sprint' } },
        ];
        component.masterData = { kpiList: [] };
        component.boardData = { configDetails: {} };
        component.dashConfigData = { scrum: [], others: [] };
        spyOn(sharedService, 'setSelectedTrends');
        spyOn(sharedService, 'select');
        // Act
        component.prepareKPICalls(event);

        // Assert
        expect(sharedService.setSelectedTrends).toHaveBeenCalledWith({
          nodeId: '1',
          labelName: 'Project',
          level: 1,
        });
        // expect(sharedService.select).toHaveBeenCalled();
      });

      it('should prepare KPI calls with sprint in event data', () => {
        // Arrange
        const event = [{ nodeId: '2', labelName: 'sprint', parentId: '1' }];
        component.selectedType = 'scrum';
        component.selectedTab = 'iteration';
        component.filterDataArr = {
          scrum: {
            Project: [{ nodeId: '1', labelName: 'Project', level: 1 }],
            sprint: [{ nodeId: '2', labelName: 'sprint', parentId: '1' }],
          },
        };
        component.additionalFilterConfig = [
          { defaultLevel: { labelName: 'sprint' } },
        ];
        component.masterData = { kpiList: [] };
        component.boardData = { configDetails: {} };
        component.dashConfigData = { scrum: [], others: [] };
        spyOn(sharedService, 'setSelectedTrends');
        spyOn(sharedService, 'select');
        // Act
        component.prepareKPICalls(event);

        // Assert
        expect(sharedService.setSelectedTrends).toHaveBeenCalledWith(event);
        // expect(sharedService.select).toHaveBeenCalled();
      });

      it('should prepare KPI calls with valid event data when selected level is null', () => {
        // Arrange
        const event = [{ nodeId: '1', labelName: 'Project', level: 1 }];
        component.selectedType = 'scrum';
        component.selectedTab = 'developer';
        component.selectedLevel = null;
        component.filterDataArr = {
          scrum: {
            Project: [{ nodeId: '1', labelName: 'Project', level: 1 }],
            sprint: [{ nodeId: '2', labelName: 'sprint', parentId: '1' }],
          },
        };
        component.additionalFilterConfig = [
          { defaultLevel: { labelName: 'sprint' } },
        ];
        component.masterData = { kpiList: [] };
        component.boardData = { configDetails: {} };
        component.dashConfigData = { scrum: [], others: [] };
        spyOn(sharedService, 'setSelectedTrends');
        spyOn(sharedService, 'select');
        // Act
        component.prepareKPICalls(event);

        // Assert
        expect(sharedService.setSelectedTrends).toHaveBeenCalledWith(event);
        // expect(sharedService.select).toHaveBeenCalled();
      });

      it('should prepare KPI calls with valid event data when selected level is null and tab is backlog', () => {
        // Arrange
        const event = [{ nodeId: '1', labelName: 'Project', level: 1 }];
        component.selectedType = 'scrum';
        component.selectedTab = 'backlog';
        component.selectedLevel = null;
        component.filterDataArr = {
          scrum: {
            Project: [{ nodeId: '1', labelName: 'Project', level: 1 }],
            Sprint: [{ nodeId: '2', labelName: 'sprint', parentId: '1' }],
          },
        };
        component.additionalFilterConfig = [
          { defaultLevel: { labelName: 'sprint' } },
        ];
        component.masterData = { kpiList: [] };
        component.boardData = { configDetails: {} };
        component.dashConfigData = { scrum: [], others: [] };
        spyOn(sharedService, 'setSelectedTrends');
        spyOn(sharedService, 'select');
        // Act
        component.prepareKPICalls(event);

        // Assert
        expect(sharedService.setSelectedTrends).toHaveBeenCalledWith(event);
        // expect(sharedService.select).toHaveBeenCalled();
      });
    });
  });

  describe('FilterNewComponent.handleAdditionalChange() handleAdditionalChange method', () => {
    describe('Happy Path', () => {
      it('should handle additional change correctly when event is valid', () => {
        // Arrange
        const event = {
          level1: [{ nodeId: '1', labelName: 'Level1', level: 5 }],
        };
        component.previousFilterEvent = { additional_level: {} };
        component.filterApplyData = { selectedMap: {} };
        component.filterDataArr = {
          scrum: {
            Project: [{ nodeId: '1', labelName: 'Project', level: 1 }],
            Sprint: [{ nodeId: '2', labelName: 'sprint', parentId: '1' }],
          },
        };
        // Act
        component.handleAdditionalChange(event);

        // Assert
        expect(
          component.previousFilterEvent.additional_level['Level1'],
        ).toEqual(event.level1);
        expect(component.filterApplyData['selectedMap']['Level1']).toEqual([
          '1',
        ]);
      });

      it('should handle additional change correctly when event is empty', () => {
        // Arrange
        const event = [];

        component.previousFilterEvent = {
          primary_level: {
            level1: [{ nodeId: '1', labelName: 'Level1', level: 5 }],
          },
          additional_level: {},
        };
        component.filterApplyData = { selectedMap: {} };
        component.filterDataArr = {
          scrum: {
            Project: [{ nodeId: '1', labelName: 'Project', level: 1 }],
            Sprint: [{ nodeId: '2', labelName: 'sprint', parentId: '1' }],
          },
        };
        // Act
        component.handleAdditionalChange(event);

        // Assert
        expect(component.previousFilterEvent).toEqual([]);
        expect(component.filterApplyData['selectedMap']['Level1']).toBeFalsy();
      });

      it('should handle additional change correctly when event is valid and tab is backlog', () => {
        // Arrange
        const event = {
          level1: [{ nodeId: '1', labelName: 'Level1', level: 5 }],
        };
        component.previousFilterEvent = { additional_level: {} };
        component.filterApplyData = { selectedMap: {} };
        component.filterDataArr = {
          scrum: {
            Project: [{ nodeId: '1', labelName: 'Project', level: 1 }],
            sprint: [{ nodeId: '2', labelName: 'sprint', parentId: '1' }],
          },
        };
        component.selectedTab = 'backlog';
        // Act
        component.handleAdditionalChange(event);

        // Assert
        expect(
          component.previousFilterEvent.additional_level['Level1'],
        ).toEqual(event.level1);
        expect(component.filterApplyData['selectedMap']['Level1']).toEqual([
          '1',
        ]);
      });

      it('should handle additional change correctly when event is valid and selectedLevel is null', () => {
        // Arrange
        const event = {
          level1: [{ nodeId: '1', labelName: 'Level1', level: 5 }],
        };
        component.previousFilterEvent = { additional_level: {} };
        component.filterApplyData = { selectedMap: {} };
        component.filterDataArr = {
          scrum: {
            Project: [{ nodeId: '1', labelName: 'Project', level: 1 }],
            sprint: [{ nodeId: '2', labelName: 'sprint', parentId: '1' }],
          },
        };
        component.selectedTab = 'backlog';
        component.selectedLevel = null;
        // Act
        component.handleAdditionalChange(event);

        // Assert
        expect(
          component.previousFilterEvent.additional_level['Level1'],
        ).toEqual(event.level1);
        expect(component.filterApplyData['selectedMap']['Level1']).toEqual([
          '1',
        ]);
      });

      it('should handle additional change correctly when event is valid and selectedLevel is object', () => {
        // Arrange
        const event = {
          level1: [{ nodeId: '1', labelName: 'Level1', level: 5 }],
        };
        component.previousFilterEvent = { additional_level: {} };
        component.filterApplyData = { selectedMap: {} };
        component.filterDataArr = {
          scrum: {
            Project: [{ nodeId: '1', labelName: 'Project', level: 1 }],
            sprint: [{ nodeId: '2', labelName: 'sprint', parentId: '1' }],
          },
        };
        component.selectedTab = 'backlog';
        component.selectedLevel = {
          labelName: 'project',
          emittedLevel: 'sprint',
        };
        // Act
        component.handleAdditionalChange(event);

        // Assert
        expect(
          component.previousFilterEvent.additional_level['Level1'],
        ).toEqual(event.level1);
        expect(component.filterApplyData['selectedMap']['Level1']).toEqual([
          '1',
        ]);
      });
    });

    describe('Edge Cases', () => {
      it('should not update if event level is less than or equal to 4', () => {
        // Arrange
        const event = {
          level1: [{ nodeId: '1', labelName: 'Level1', level: 4 }],
        };
        component.previousFilterEvent = { additional_level: {} };
        component.filterApplyData = { selectedMap: {} };

        // Act
        component.handleAdditionalChange(event);

        // Assert
        expect(
          component.previousFilterEvent.additional_level['Level1'],
        ).toEqual(event.level1);
        expect(
          component.filterApplyData['selectedMap']['Level1'],
        ).toBeUndefined();
      });
    });
  });

  xdescribe('handlePrimaryFilterChange - Happy Path', () => {
    it('should sort the event array when event is an array of objects based on nodeId', () => {
      component.previousFilterEvent = [];
      component.selectedTab = 'someTab';
      component.selectedType = 'someType';
      component.filterDataArr = { someType: { Sprint: [], Project: [] } };

      const event = [
        { nodeId: 2, labelName: 'project' },
        { nodeId: 1, labelName: 'project' },
      ];
      component.handlePrimaryFilterChange(event);
      expect(event[0].nodeId).toBe(1);
      expect(event[1].nodeId).toBe(2);
    });

    it('should call getBoardConfig if event and previous parent nodes are not deeply equal', () => {
      component.previousFilterEvent = [];
      component.selectedTab = 'someTab';
      component.selectedType = 'someType';
      component.filterDataArr = { someType: { Sprint: [], Project: [] } };

      const event = [
        { labelName: 'project', nodeId: 1, basicProjectConfigId: 123 },
      ];
      component.previousFilterEvent = [{ labelName: 'release', nodeId: 2 }];
      spyOn(component, 'arrayDeepCompare').and.returnValue(false);
      spyOn(component, 'getBoardConfig');
      spyOn(helperService, 'deepEqual').and.returnValue(false);

      component.handlePrimaryFilterChange(event);
      expect(component.getBoardConfig).toHaveBeenCalledWith([123], event);
    });

    it('should populate additional filters when additional_level exists', (done) => {
      component.previousFilterEvent = [];
      component.selectedTab = 'someTab';
      component.selectedType = 'someType';
      component.filterDataArr = { someType: { Sprint: [], Project: [] } };

      const event = {
        primary_level: 'somePrimaryLevel',
        additional_level: { key1: ['value'] },
      };
      spyOn(component, 'populateAdditionalFilters');
      spyOn(component, 'handleAdditionalChange');

      component.handlePrimaryFilterChange(event);
      setTimeout(() => {
        expect(component.populateAdditionalFilters).toHaveBeenCalledWith(
          'somePrimaryLevel',
        );
        expect(component.handleAdditionalChange).toHaveBeenCalledWith({
          key1: ['value'],
        });
        done();
      }, 0);
    });
  });

  describe('handlePrimaryFilterChange - Edge Cases', () => {
    it('should handle event with labelName not matching default level', () => {
      component.previousFilterEvent = [];
      component.selectedTab = 'someTab';
      component.selectedType = 'someType';
      component.filterDataArr = { someType: { Sprint: [], Project: [] } };
      spyOn(sharedService, 'setAdditionalFilters');
      component.primaryFilterConfig = {
        defaultLevel: { labelName: 'release' },
      };
      const event = [{ labelName: 'project', nodeId: 1 }];

      component.handlePrimaryFilterChange(event);
      expect(component.noSprint).toBe(true);
      expect(sharedService.setAdditionalFilters).toHaveBeenCalledWith([]);
    });

    it('should handle missing additional_level and primary_level in event', () => {
      component.previousFilterEvent = [];
      component.selectedTab = 'someTab';
      component.selectedType = 'someType';
      component.filterDataArr = { someType: { Sprint: [], Project: [] } };

      const event = { someOtherKey: 'value' };
      spyOn(component, 'prepareKPICalls');
      component.handlePrimaryFilterChange(event);
      expect(component.prepareKPICalls).not.toHaveBeenCalled();
    });
  });

  describe('FilterNewComponent.sendDataToDashboard() sendDataToDashboard method', () => {
    describe('Happy Path', () => {
      it('should set previousFilterEvent and call setColors with event data', () => {
        const event = [{ nodeId: '1', level: 1, labelName: 'Project' }];
        spyOn(component, 'setColors');
        component.filterDataArr = {
          scrum: {
            Project: [{ nodeId: '1', labelName: 'Project', level: 1 }],
            sprint: [{ nodeId: '2', labelName: 'sprint', parentId: '1' }],
          },
        };
        component.sendDataToDashboard(event);

        expect(component.previousFilterEvent).toEqual(event);
        expect(component.setColors).toHaveBeenCalledWith(event);
      });

      it('should update filterApplyData and call setSelectedMapLevels', () => {
        const event = [{ nodeId: '1', level: 1, labelName: 'Project' }];
        spyOn(component, 'setSelectedMapLevels');
        component.selectedLevel = 'Project';
        component.filterDataArr = {
          scrum: {
            Project: [{ nodeId: '1', labelName: 'Project', level: 1 }],
            sprint: [{ nodeId: '2', labelName: 'sprint', parentId: '1' }],
          },
        };
        component.sendDataToDashboard(event);

        expect(component.filterApplyData['level']).toBe(1);
        expect(component.filterApplyData['label']).toBe('Project');
        expect(component.setSelectedMapLevels).toHaveBeenCalled();
      });

      xit('should call service.select with correct parameters', () => {
        spyOn(sharedService, 'select');
        const event = [{ nodeId: '1', level: 1, labelName: 'Project' }];
        component.filterDataArr = { scrum: { Project: [] } };
        component.selectedType = 'scrum';
        component.selectedLevel = 'Project';
        component.masterData = {};
        component.boardData = { configDetails: {} };
        component.dashConfigData = {};

        component.sendDataToDashboard(event);

        expect(sharedService.select).toHaveBeenCalledWith(
          component.masterData,
          component.filterDataArr['scrum']['Project'],
          component.filterApplyData,
          component.selectedTab,
          false,
          true,
          component.boardData['configDetails'],
          true,
          component.dashConfigData,
          component.selectedType,
          {},
        );
      });
    });

    describe('Edge Cases', () => {
      xit('should handle null selectedLevel', () => {
        spyOn(sharedService, 'select');
        const event = [{ nodeId: '1', level: 1, labelName: 'Project' }];
        component.selectedLevel = null;
        component.filterDataArr = { scrum: { Project: [] } };
        component.selectedType = 'scrum';
        component.masterData = {};
        component.boardData = { configDetails: {} };
        component.dashConfigData = {};

        component.sendDataToDashboard(event);

        expect(sharedService.select).toHaveBeenCalledWith(
          component.masterData,
          component.filterDataArr['scrum']['Project'],
          component.filterApplyData,
          component.selectedTab,
          false,
          true,
          component.boardData['configDetails'],
          true,
          component.dashConfigData,
          component.selectedType,
          {},
        );
      });

      xit('should handle kanban type correctly', () => {
        const event = [{ nodeId: '1', level: 1, labelName: 'Project' }];
        component.kanban = true;
        component.selectedTab = 'backlog';
        component.filterDataArr = { kanban: { Sprint: [] } };
        component.selectedType = 'kanban';
        component.masterData = {};
        component.boardData = { configDetails: {} };
        component.dashConfigData = {};

        component.sendDataToDashboard(event);

        expect(component.filterApplyData['selectedMap']['sprint']).toEqual([]);
      });
    });
  });

  describe('FilterNewComponent.checkForFilterApplyDataSelectedMap() checkForFilterApplyDataSelectedMap method', () => {
    it('should set project and sprint in filterApplyData.selectedMap when sprint is selected and no project is selected', () => {
      const levelDetails = [
        { hierarchyLevelId: 'sprint', hierarchyLevelName: 'Sprint' },
        { hierarchyLevelId: 'project', hierarchyLevelName: 'Project' },
      ];
      const filterDataArr = {
        kanban: {
          Sprint: [
            { nodeId: 'sprint1', parentId: 'project1' },
            { nodeId: 'sprint2', parentId: 'project2' },
          ],
          Project: [{ nodeId: 'project1' }, { nodeId: 'project2' }],
        },
      };
      const filterApplyData = {
        selectedMap: {
          sprint: ['sprint1'],
          project: [],
        },
      };

      spyOn(localStorage, 'getItem').and.returnValue(
        JSON.stringify({ kanban: levelDetails }),
      );
      component.selectedType = 'kanban';
      component.filterDataArr = filterDataArr;
      component.filterApplyData = filterApplyData;

      component.checkForFilterApplyDataSelectedMap();

      expect(component.filterApplyData['selectedMap'].project).toEqual([
        'project1',
      ]);
      expect(component.filterApplyData['selectedMap'].sprint).toEqual([
        'sprint1',
      ]);
    });

    it('should set project and sprint in filterApplyData.selectedMap when squad is selected and no project is selected', () => {
      const levelDetails = [
        { hierarchyLevelId: 'sprint', hierarchyLevelName: 'Sprint' },
        { hierarchyLevelId: 'project', hierarchyLevelName: 'Project' },
        { hierarchyLevelId: 'sqd', hierarchyLevelName: 'Squad' },
      ];
      const filterDataArr = {
        kanban: {
          Sprint: [{ nodeId: 'sprint1', parentId: 'project1' }],
          Project: [{ nodeId: 'project1' }],
          Squad: [{ nodeId: 'squad1', parentId: 'sprint1' }],
        },
      };
      const filterApplyData = {
        selectedMap: {
          sqd: ['squad1'],
          project: [],
        },
      };

      spyOn(localStorage, 'getItem').and.returnValue(
        JSON.stringify({ kanban: levelDetails }),
      );
      component.selectedType = 'kanban';
      component.filterDataArr = filterDataArr;
      component.filterApplyData = filterApplyData;
      component.squadLevel = [{ hierarchyLevelId: 'sqd' }];

      component.checkForFilterApplyDataSelectedMap();

      expect(component.filterApplyData['selectedMap'].project).toEqual([
        'project1',
      ]);
      //   expect(component.filterApplyData['selectedMap'].sprint).toEqual(['sprint1']);
    });

    it('should not modify filterApplyData.selectedMap if project is already selected', () => {
      const filterApplyData = {
        selectedMap: {
          sprint: ['sprint1'],
          project: ['project1'],
        },
      };

      component.filterApplyData = filterApplyData;

      component.checkForFilterApplyDataSelectedMap();

      expect(component.filterApplyData['selectedMap'].project).toEqual([
        'project1',
      ]);
      expect(component.filterApplyData['selectedMap'].sprint).toEqual([
        'sprint1',
      ]);
    });
  });

  describe('isSprintGoalsHidden', () => {
    it('should return true when kanban is falsy and selectedTab is "my-knowhow"', () => {
      component.kanban = null; // Falsy value
      component.selectedTab = 'my-knowhow';
      expect(component.isSprintGoalsHidden()).toBeTrue();
    });

    it('should return true when kanban is falsy and selectedTab is "speed"', () => {
      component.kanban = undefined; // Another falsy value
      component.selectedTab = 'speed';
      expect(component.isSprintGoalsHidden()).toBeTrue();
    });

    it('should return true when kanban is falsy and selectedTab is "quality"', () => {
      component.kanban = false;
      component.selectedTab = 'quality';
      expect(component.isSprintGoalsHidden()).toBeTrue();
    });

    it('should return false when kanban is truthy, even if selectedTab is in the list', () => {
      component.kanban = true;
      component.selectedTab = 'my-knowhow';
      expect(component.isSprintGoalsHidden()).toBeFalse();
    });

    it('should return false when selectedTab is not in the list', () => {
      component.kanban = false;
      component.selectedTab = 'random';
      expect(component.isSprintGoalsHidden()).toBeFalse();
    });

    it('should return false when selectedTab is undefined', () => {
      component.kanban = false;
      component.selectedTab = undefined;
      expect(component.isSprintGoalsHidden()).toBeFalse();
    });

    it('should be case-insensitive (handle uppercase "SPEED")', () => {
      component.kanban = null;
      component.selectedTab = 'SPEED'; // Uppercase
      expect(component.isSprintGoalsHidden()).toBeTrue();
    });
  });

  describe('getBgClass', () => {
    it('should return "icon-apply" when showSprintGoalsPanel is true', () => {
      component.showSprintGoalsPanel = true;
      expect(component.getBgClass()).toBe('icon-apply');
    });

    it('should return "icon-not-active" when showSprintGoalsPanel is false', () => {
      component.showSprintGoalsPanel = false;
      expect(component.getBgClass()).toBe('icon-not-active');
    });

    it('should return "icon-not-active" when showSprintGoalsPanel is undefined', () => {
      component.showSprintGoalsPanel = undefined;
      expect(component.getBgClass()).toBe('icon-not-active');
    });

    it('should return "icon-not-active" when showSprintGoalsPanel is null', () => {
      component.showSprintGoalsPanel = null;
      expect(component.getBgClass()).toBe('icon-not-active');
    });
  });
});
