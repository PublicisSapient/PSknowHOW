import { GoogleAnalyticsService } from 'src/app/services/google-analytics.service';
import { FilterNewComponent } from './filter-new.component';

import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
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

describe('FilterNewComponent', () => {
    let fixture: ComponentFixture<FilterNewComponent>;
    let component: FilterNewComponent;
    let sharedService: SharedService;
    let httpService: HttpService;
    let helperService;
    let featureFlagsService;
    let messageService;

    beforeEach(async () => {

        await TestBed.configureTestingModule({
            declarations: [FilterNewComponent],
            imports: [RouterTestingModule, HttpClientModule, BrowserAnimationsModule, HttpClientTestingModule],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],

            providers: [SharedService, HelperService, CommonModule, DatePipe,
                { provide: APP_CONFIG, useValue: AppConfig }, HttpService,
                FeatureFlagsService, MessageService]
        }).compileComponents();

        fixture = TestBed.createComponent(FilterNewComponent);
        component = fixture.componentInstance;
        sharedService = TestBed.inject(SharedService);
        helperService = TestBed.inject(HelperService);
        httpService = TestBed.inject(HttpService);
        featureFlagsService = TestBed.inject(FeatureFlagsService);
        messageService = TestBed.inject(MessageService);
        localStorage.setItem('completeHierarchyData', '{"kanban":[{"id":"6442815917ed167d8157f0f5","level":1,"hierarchyLevelId":"bu","hierarchyLevelName":"BU","hierarchyInfo":"Business Unit"},{"id":"6442815917ed167d8157f0f6","level":2,"hierarchyLevelId":"ver","hierarchyLevelName":"Vertical","hierarchyInfo":"Industry"},{"id":"6442815917ed167d8157f0f7","level":3,"hierarchyLevelId":"acc","hierarchyLevelName":"Account","hierarchyInfo":"Account"},{"id":"6442815917ed167d8157f0f8","level":4,"hierarchyLevelId":"port","hierarchyLevelName":"Engagement","hierarchyInfo":"Engagement"},{"level":5,"hierarchyLevelId":"project","hierarchyLevelName":"Project"},{"level":6,"hierarchyLevelId":"release","hierarchyLevelName":"Release"},{"level":7,"hierarchyLevelId":"sqd","hierarchyLevelName":"Squad"}],"scrum":[{"id":"6442815917ed167d8157f0f5","level":1,"hierarchyLevelId":"bu","hierarchyLevelName":"BU","hierarchyInfo":"Business Unit"},{"id":"6442815917ed167d8157f0f6","level":2,"hierarchyLevelId":"ver","hierarchyLevelName":"Vertical","hierarchyInfo":"Industry"},{"id":"6442815917ed167d8157f0f7","level":3,"hierarchyLevelId":"acc","hierarchyLevelName":"Account","hierarchyInfo":"Account"},{"id":"6442815917ed167d8157f0f8","level":4,"hierarchyLevelId":"port","hierarchyLevelName":"Engagement","hierarchyInfo":"Engagement"},{"level":5,"hierarchyLevelId":"project","hierarchyLevelName":"Project"},{"level":6,"hierarchyLevelId":"sprint","hierarchyLevelName":"Sprint"},{"level":6,"hierarchyLevelId":"release","hierarchyLevelName":"Release"},{"level":7,"hierarchyLevelId":"sqd","hierarchyLevelName":"Squad"}]}')
        fixture.detectChanges();
    });


    it('should create', () => {
        expect(component).toBeTruthy();
    });

    describe('FilterNewComponent.ngOnInit() ngOnInit method', () => {
        describe('Happy Path', () => {
            it('should initialize selectedTab and selectedType correctly', async () => {
                spyOn(sharedService, 'getSelectedTab').and.returnValue('iteration');
                spyOn(helperService, 'getBackupOfFilterSelectionState').and.returnValue(
                    'scrum',
                );
                spyOn(featureFlagsService, 'isFeatureEnabled').and.returnValue(true);
                spyOn(sharedService, 'setRecommendationsFlag');
                await component.ngOnInit();

                expect(component.selectedTab).toBe('iteration');
                expect(component.selectedType).toBe('scrum');
                expect(component.kanban).toBe(false);
                expect(sharedService.setRecommendationsFlag).toHaveBeenCalledWith(
                    true,
                );
            });
        });

        describe('Edge Cases', () => {
            it('should handle null selectedTab and selectedType gracefully', async () => {
                spyOn(sharedService, 'getSelectedTab').and.returnValue(null);
                spyOn(helperService, 'getBackupOfFilterSelectionState').and.returnValue(null);
                spyOn(featureFlagsService, 'isFeatureEnabled').and.returnValue(false);
                spyOn(sharedService, 'setRecommendationsFlag');
                await component.ngOnInit();

                expect(component.selectedTab).toBe('iteration');
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
                    scrum: [{ hierarchyLevelId: 'level1', hierarchyLevelName: 'Level 1' }],
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
                const spy = spyOn(
                    sharedService.dateFilterSelectedDateType,
                    'next',
                );

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
                const spy = spyOn(
                    sharedService.dateFilterSelectedDateType,
                    'next',
                );

                // Act
                component.setSelectedDateType(label);

                // Assert
                expect(spy).toHaveBeenCalledWith(label);
            });

            it('should handle null as label', () => {
                // Arrange
                const label = null;
                const spy = spyOn(
                    sharedService.dateFilterSelectedDateType,
                    'next',
                );

                // Act
                component.setSelectedDateType(label as any);

                // Assert
                expect(spy).toHaveBeenCalledWith(label);
            });

            it('should handle undefined as label', () => {
                // Arrange
                const label = undefined;
                const spy = spyOn(
                    sharedService.dateFilterSelectedDateType,
                    'next',
                );

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
                spyOn(helperService, 'setBackupOfFilterSelectionState');
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
                    helperService.setBackupOfFilterSelectionState,
                ).toHaveBeenCalledWith({ selected_type: 'kanban' });
                expect(sharedService.setScrumKanban).toHaveBeenCalledWith('kanban');
            });

            it('should set selectedType to "scrum" and update related properties', () => {
                // Arrange
                const type = 'scrum';
                spyOn(helperService, 'setBackupOfFilterSelectionState');
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
                    helperService.setBackupOfFilterSelectionState,
                ).toHaveBeenCalledWith({ selected_type: 'scrum' });
                expect(sharedService.setScrumKanban).toHaveBeenCalledWith('scrum');
            });
        });

        describe('Edge Cases', () => {
            it('should handle empty string type gracefully', () => {
                // Arrange
                const type = '';
                spyOn(helperService, 'setBackupOfFilterSelectionState');
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
                    helperService.setBackupOfFilterSelectionState,
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
                    ]
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
                    data: [{ level: 1, labelName: 'Project', nodeId: '123', nodeName: 'def', basicProjectConfigId: '123' },
                    { level: 1, labelName: 'Project2', nodeId: '321', nodeName: 'abc', basicProjectConfigId: '321' }
                    ],
                };
                spyOn(helperService, 'sortAlphabetically').and.returnValue(of([
                    { level: 1, labelName: 'Project2', nodeId: '321', nodeName: 'abc', basicProjectConfigId: '321' },
                    { level: 1, labelName: 'Project', nodeId: '123', nodeName: 'def', basicProjectConfigId: '123' }
                ]));
                spyOn(httpService, 'getFilterData').and.returnValue(of(mockFilterData));

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
                spyOn(httpService, 'getAllHierarchyLevels').and.returnValue(of(mockHierarchyData));

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
                spyOn(httpService, 'getAllHierarchyLevels').and.returnValue(of(mockHierarchyData));
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
                        level1: [{ level: 1, labelName: 'Project1', nodeId: '1', nodeName: 'Node 1', basicProjectConfigId: '321' }],
                        level2: [{ level: 2, labelName: 'Project2', nodeId: '2', nodeName: 'Node 2', basicProjectConfigId: '421' }],
                    },
                };
                localStorage.setItem(
                    'completeHierarchyData',
                    JSON.stringify({ scrum: mockHierarchyData }),
                );
                component.filterDataArr = mockFilterDataArr;
                component.selectedType = 'scrum';
                spyOn(helperService, 'sortAlphabetically').and.returnValue(of([
                    { level: 1, labelName: 'Project1', nodeId: '1', nodeName: 'Node 1', basicProjectConfigId: '321' },
                    { level: 2, labelName: 'Project2', nodeId: '2', nodeName: 'Node 2', basicProjectConfigId: '421' }
                ]));
                // Act
                component.setCategories();

                // Assert
                expect(component.filterDataArr['scrum']).toEqual({
                    'Level 1': [{ level: 1, labelName: 'Project1', nodeId: '1', nodeName: 'Node 1', basicProjectConfigId: '321' }],
                    'Level 2': [{ level: 2, labelName: 'Project2', nodeId: '2', nodeName: 'Node 2', basicProjectConfigId: '421' }],
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
            expect(
                httpService.getShowHideOnDashboardNewUI,
            ).not.toHaveBeenCalled();
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
                            { nodeId: '2', nodeName: 'Filter2', labelName: 'Project' }
                        ]
                    }
                }
                component.colorObj = {
                    '1': { nodeId: '1', nodeName: 'Filter1', labelName: 'Project' },
                    '2': { nodeId: '2', nodeName: 'Filter2', labelName: 'Project' },
                };
                const stateFilters = {
                    primary_level: [{ nodeId: '1', labelName: 'Project' }],
                };
                spyOn(helperService, 'getBackupOfFilterSelectionState').and.returnValue(
                    stateFilters,
                );

                spyOn(sharedService, 'setSelectedTrends');

                spyOn(helperService, 'setBackupOfFilterSelectionState');

                // Act
                component.removeFilter('1');

                // Assert
                expect(component.colorObj).not.toEqual(jasmine.objectContaining({
                    1: { nodeId: '1', nodeName: 'Filter1' },
                }));
                expect(sharedService.setSelectedTrends).toHaveBeenCalled();
                expect(
                    helperService.setBackupOfFilterSelectionState,
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
                spyOn(helperService, 'setBackupOfFilterSelectionState');
                // Act
                component.removeFilter('1');

                // Assert
                expect(component.colorObj).toEqual(jasmine.objectContaining({
                    1: { nodeId: '1', nodeName: 'Filter1' },
                }));
                expect(sharedService.setSelectedTrends).not.toHaveBeenCalled();
                expect(
                    helperService.setBackupOfFilterSelectionState,
                ).not.toHaveBeenCalled();
            });

            it('should handle removal of a non-existent filter gracefully', () => {
                // Arrange
                component.colorObj = {
                    '1': { nodeId: '1', nodeName: 'Filter1' },
                };
                spyOn(sharedService, 'setSelectedTrends');
                spyOn(helperService, 'setBackupOfFilterSelectionState');
                // Act
                component.removeFilter('2');

                // Assert
                expect(component.colorObj).toEqual(jasmine.objectContaining({
                    1: { nodeId: '1', nodeName: 'Filter1' },
                }));
                expect(sharedService.setSelectedTrends).not.toHaveBeenCalled();
                expect(
                    helperService.setBackupOfFilterSelectionState,
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
                const filterApiData = { success: true, data: [{ labelName: 'project' }] };
                spyOn(httpService, 'getFilterData').and.returnValue(of(filterApiData) as any);
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
                const filterApiData = { success: true, data: [{ labelName: 'project' }] };
                spyOn(httpService, 'getFilterData').and.returnValue(of(filterApiData) as any);
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
                spyOn(httpService, 'getFilterData').and.returnValue(of(filterApiData) as any);
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
                expect(sharedService.setNoProjects).toHaveBeenCalledWith(true);
            });

            it('should handle unsuccessful filter data response', () => {
                // Arrange
                const filterApiData = { success: false };
                spyOn(httpService, 'getFilterData').and.returnValue(of(filterApiData) as any);
                spyOn(sharedService, 'setNoProjectsForNewUI');
                // Act
                component.firstLoadFilterCheck(false);

                // Assert
                expect(component.scrumProjectsAvailable).toBe(true);
                expect(sharedService.setNoProjectsForNewUI).not.toHaveBeenCalled();
            });
        });
    });
});