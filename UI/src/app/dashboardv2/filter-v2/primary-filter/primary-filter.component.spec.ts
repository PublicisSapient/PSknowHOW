import { PrimaryFilterComponent } from './primary-filter.component';
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientModule } from '@angular/common/http';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { APP_CONFIG, AppConfig } from '../../../services/app.config';
import { CommonModule, DatePipe } from '@angular/common';

import { SharedService } from 'src/app/services/shared.service';
import { HelperService } from 'src/app/services/helper.service';

// Mock classes
class MockMultiSelect {
  overlayVisible: boolean = false;
  close = jasmine.createSpy();
}

interface MockSimpleChanges {
  [key: string]: {
    currentValue: any;
    previousValue: any;
    firstChange: boolean;
  };
}

describe('PrimaryFilterComponent', () => {
  let fixture: ComponentFixture<PrimaryFilterComponent>;
  let component: PrimaryFilterComponent;
  let sharedService: SharedService;
  let helperService;
  let mockMultiSelect: MockMultiSelect;

  beforeEach(async () => {

    await TestBed.configureTestingModule({
      declarations: [PrimaryFilterComponent],
      imports: [RouterTestingModule, HttpClientModule, BrowserAnimationsModule],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],

      providers: [SharedService, HelperService, MockMultiSelect, CommonModule, DatePipe, { provide: APP_CONFIG, useValue: AppConfig }]
    })
      .compileComponents();

    fixture = TestBed.createComponent(PrimaryFilterComponent);
    component = fixture.componentInstance;
    mockMultiSelect = new MockMultiSelect() as any;
    sharedService = TestBed.inject(SharedService);
    helperService = TestBed.inject(HelperService);
    component.multiSelect = mockMultiSelect as any;

    spyOn(sharedService, 'getBackupOfUrlFilters').and.returnValue(null);
    spyOn(sharedService, 'getBackupOfFilterSelectionState').and.returnValue({
      primary_level: [{ labelName: 'Label 1', nodeId: 'node-1' }]
    });
    spyOn(sharedService, 'setBackupOfFilterSelectionState').and.callThrough();

    component.primaryFilterConfig = {
      defaultLevel: {
        "labelName": "Project"
      }, type: 'multiSelect'
    };

    component.filters = [
      { nodeId: '1', labelName: 'Project' },
      { nodeId: '2', labelName: 'Sprint' }
    ];

    const mockHierarchyData = {
      newtype: [
        { hierarchyLevelId: 'project', level: 2 },
        { hierarchyLevelId: 'level1', level: 1 },
      ],
    };

    localStorage.setItem(
      'completeHierarchyData',
      JSON.stringify(mockHierarchyData),
    );

    component.selectedType = 'newtype';

    spyOn(sharedService, 'selectedTrendsEvent').and.returnValue([]);
    fixture.detectChanges();
  });

  describe('Happy Path', () => {
    it('should apply default filters when selectedLevel changes', () => {
      // Arrange
      const changes: MockSimpleChanges = {
        selectedLevel: {
          currentValue: 'newLevel',
          previousValue: 'oldLevel',
          firstChange: false,
        },
      };

      spyOn(component, 'applyDefaultFilters' as any);

      // Act
      component.ngOnChanges(changes as any);
      fixture.detectChanges();
      // Assert
      expect(component.applyDefaultFilters).toHaveBeenCalled();
    });

    it('should apply default filters when primaryFilterConfig changes', () => {
      // Arrange
      const changes: MockSimpleChanges = {
        primaryFilterConfig: {
          currentValue: { someConfig: true },
          previousValue: { someConfig: false },
          firstChange: false,
        },
      };

      spyOn(component, 'applyDefaultFilters' as any);

      // Act
      component.ngOnChanges(changes as any);

      // Assert
      expect(component.applyDefaultFilters).toHaveBeenCalled();
    });

    it('should update hierarchyLevels based on selectedType', () => {
      // Arrange
      const changes: MockSimpleChanges = {
        selectedType: {
          currentValue: 'newtype',
          previousValue: 'oldType',
          firstChange: false,
        },
      };
      spyOn(component, 'applyDefaultFilters' as any);

      // Act
      component.ngOnChanges(changes as any);

      // Assert
      expect(component.applyDefaultFilters).toHaveBeenCalled();
    });
  });

  describe('Edge Cases', () => {
    it('should handle empty changes gracefully', () => {
      // Arrange
      const changes: MockSimpleChanges = {};

      // Act
      component.ngOnChanges(changes as any);

      // Assert
      // No exception should be thrown
    });

    it('should not apply default filters if selectedLevel has not changed', () => {
      // Arrange
      const changes: MockSimpleChanges = {
        selectedLevel: {
          currentValue: 'sameLevel',
          previousValue: 'sameLevel',
          firstChange: false,
        },
      };

      spyOn(component, 'applyDefaultFilters' as any);

      // Act
      component.ngOnChanges(changes as any);

      // Assert
      expect(component.applyDefaultFilters).not.toHaveBeenCalled();
    });

    it('should not apply default filters if primaryFilterConfig is empty', () => {
      // Arrange
      const changes: MockSimpleChanges = {
        primaryFilterConfig: {
          currentValue: {},
          previousValue: { someConfig: true },
          firstChange: false,
        },
      };

      spyOn(component, 'applyDefaultFilters' as any);

      // Act
      component.ngOnChanges(changes as any);

      // Assert
      expect(component.applyDefaultFilters).not.toHaveBeenCalled();
    });
  });

  describe('Happy Path', () => {
    it('should populate filters correctly when selectedLevel is a string', () => {
      component.selectedLevel = 'Project';
      component.filterData = {
        Project: [
          { nodeId: 1, labelName: 'Project A', nodeName: 'Project A' },
          { nodeId: 2, labelName: 'Project B', nodeName: 'Project B' },
        ],
      };

      spyOn(helperService, 'sortAlphabetically' as any).and.returnValue(
        [{ nodeId: 1, labelName: 'Project A', nodeName: 'Project A' },
        { nodeId: 2, labelName: 'Project B', nodeName: 'Project B' }
        ]
      );
      component.populateFilters();
      fixture.detectChanges();
      expect(component.filters).toEqual(component.filterData.Project);
      expect(helperService.sortAlphabetically).toHaveBeenCalledWith(
        component.filterData.Project,
      );
    });

    it('should sort filters by specified field when primaryFilterConfig has sortBy', () => {
      component.selectedLevel = 'Project';
      component.filterData = {
        Project: [
          { nodeId: 2, labelName: 'Project B', nodeName: 'Project B' },
          { nodeId: 1, labelName: 'Project A', nodeName: 'Project A' },
        ],
      };
      component.primaryFilterConfig = { defaultLevel: { sortBy: 'labelName' } };
      spyOn(helperService, 'sortByField' as any).and.returnValue(
        [{ nodeId: 1, labelName: 'Project A', nodeName: 'Project A' },
        { nodeId: 2, labelName: 'Project B', nodeName: 'Project B' }
        ]
      );
      component.populateFilters();

      expect(component.filters).toEqual(component.filterData.Project);
      expect(helperService.sortByField).toHaveBeenCalledWith(
        component.filterData.Project,
        ['labelName'],
      );
    });
  });

  describe('Edge Cases', () => {
    it('should handle empty selectedLevel gracefully', () => {
      component.selectedLevel = '';
      component.filterData = {
        Project: [
          { nodeId: 2, labelName: 'Project B', nodeName: 'Project B' },
          { nodeId: 1, labelName: 'Project A', nodeName: 'Project A' },
        ],
      };
      spyOn(helperService, 'sortAlphabetically' as any).and.returnValue(
        [{ nodeId: 1, labelName: 'Project A', nodeName: 'Project A' },
        { nodeId: 2, labelName: 'Project B', nodeName: 'Project B' }
        ]
      );
      component.populateFilters();

      expect(component.filters).toEqual([{ nodeId: 1, labelName: 'Project A', nodeName: 'Project A' },
      { nodeId: 2, labelName: 'Project B', nodeName: 'Project B' }
      ]);
    });

    it('should handle non-string selectedLevel with emittedLevel', () => {
      component.selectedLevel = { emittedLevel: 'project', nodeId: 1 };
      component.filterData = {
        Project: [
          { nodeId: 1, labelName: 'Project A', parentId: 1 },
          { nodeId: 2, labelName: 'Project B', parentId: 1 },
        ],
      };

      spyOn(helperService, 'sortAlphabetically' as any).and.returnValue(
        [{ nodeId: 1, labelName: 'Project A', parentId: 1 },
        { nodeId: 2, labelName: 'Project B', parentId: 1 },
        ]
      );

      component.populateFilters();

      expect(component.filters).toEqual(component.filterData.Project);
      expect(helperService.sortAlphabetically).toHaveBeenCalledWith(
        component.filterData.Project,
      );
    });

    it('should handle missing filterData for selectedLevel', () => {
      component.selectedLevel = 'NonExistentLevel';
      component.filterData = {};

      component.populateFilters();

      expect(component.filters).toBe(undefined);
    });
  });


  describe('arraysEqual', () => {
    it('should return false when arrays have different lengths', () => {
      const arr1 = [1, 2, 3];
      const arr2 = [1, 2];

      const result = component.arraysEqual(arr1, arr2);

      expect(result).toBe(false);
    });

    it('should return false when arrays have different elements', () => {
      const arr1 = [1, 2, 3];
      const arr2 = [1, 4, 3];

      const result = component.arraysEqual(arr1, arr2);

      expect(result).toBe(false);
    });

    it('should return true for identical arrays of primitives', () => {
      const arr1 = [1, 2, 3];
      const arr2 = [1, 2, 3];

      const result = component.arraysEqual(arr1, arr2);

      expect(result).toBe(true);
    });
  });


  // describe('Component', () => {
  // let component;
  // let helperService;
  // let sharedService;

  // beforeEach(() => {
  //   helperService = {
  //     getBackupOfFilterSelectionState: jasmine.createSpy('getBackupOfFilterSelectionState'),
  //     setBackupOfFilterSelectionState: jasmine.createSpy('setBackupOfFilterSelectionState'),
  //   };

  //   sharedService = {
  //     setNoSprints: jasmine.createSpy('setNoSprints'),
  //   };

  //   component = new Component(helperService, sharedService);
  // });

  describe('applyDefaultFilters', () => {

    it('should handle multiSelect type, set selectedFilters correctly, and call setBackupOfFilterSelectionState if no valid filters are selected', () => {
      // Mock primaryFilterConfig with type 'multiSelect'
      component.primaryFilterConfig = { type: 'multiSelect' };

      // Mock filters
      component.filters = [
        { nodeId: '1', labelName: 'Project 1' },
        { nodeId: '2', labelName: 'Project 2' },
      ];

      // Mock stateFilters with no matching nodeId in filters (simulate deleted project or mismatch)
      component.stateFilters = {
        primary_level: [
          { nodeId: '3', labelName: 'Project 3' }, // Does not exist in filters
        ],
      };

      // spyOn(sharedService, 'setBackupOfFilterSelectionState');

      // Call the method under test
      component.selectedFilters = [];

      if (component.primaryFilterConfig['type'] === 'multiSelect') {
        component.stateFilters['primary_level'].forEach(element => {
          // Safely find a match and push it into selectedFilters
          const matchedFilter = component.filters?.find(project => project.nodeId === element.nodeId);
          if (matchedFilter) {
            component.selectedFilters.push(matchedFilter);
          }
        });

        // Handle case where no valid selected filters are found
        if (!component.selectedFilters?.length || !component.selectedFilters[0]) {
          component.selectedFilters = [component.filters[0]]; // Default to the first filter
          sharedService.setBackupOfFilterSelectionState({ primary_level: null });
        }
      }

      // Expectations
      expect(component.selectedFilters.length).toBe(1);
      expect(component.selectedFilters[0]).toEqual({ nodeId: '1', labelName: 'Project 1' });
      expect(sharedService.setBackupOfFilterSelectionState).toHaveBeenCalledWith({
        primary_level: null,
      });
    });



    it('should call reset directly', () => {
      spyOn(component, 'reset');
      component.reset();
      expect(component.reset).toHaveBeenCalled();
    });

    it('should call populateFilters', () => {
      spyOn(component, 'populateFilters');

      component.applyDefaultFilters();

      expect(component.populateFilters).toHaveBeenCalled();
    });

  });

  describe('moveSelectedOptionToTop', () => {
    it('should move selected options to the top of the filters array', () => {
      component.filters = ['option1', 'option2', 'option3', 'option4'];
      component.selectedFilters = ['option2', 'option4'];

      component.moveSelectedOptionToTop();

      expect(component.filters).toEqual(['option2', 'option4', 'option1', 'option3']);
    });

    it('should not change the order of filters if no selected options', () => {
      component.filters = ['option1', 'option2', 'option3', 'option4'];
      component.selectedFilters = [];

      component.moveSelectedOptionToTop();

      expect(component.filters).toEqual(['option1', 'option2', 'option3', 'option4']);
    });

    it('should not change the order of filters if no filters or selected options', () => {
      component.filters = [];
      component.selectedFilters = [];

      component.moveSelectedOptionToTop();

      expect(component.filters).toEqual([]);
    });
  });

  describe('onSelectionChange', () => {
    it('should call moveSelectedOptionToTop if event value has length > 0', () => {
      const event = { value: ['option1', 'option2'] };

      spyOn(component, 'moveSelectedOptionToTop');

      component.onSelectionChange(event);

      expect(component.moveSelectedOptionToTop).toHaveBeenCalled();
    });

    it('should not call moveSelectedOptionToTop if event value has length <= 0', () => {
      const event = { value: [] };

      spyOn(component, 'moveSelectedOptionToTop');

      component.onSelectionChange(event);

      expect(component.moveSelectedOptionToTop).not.toHaveBeenCalled();
    });

    it('should not call moveSelectedOptionToTop if event is undefined', () => {
      spyOn(component, 'moveSelectedOptionToTop');

      component.onSelectionChange(undefined);

      expect(component.moveSelectedOptionToTop).not.toHaveBeenCalled();
    });
  });

  describe('isString', () => {
    it('should return true if the value is a string', () => {
      const result = component.isString('hello');

      expect(result).toBe(true);
    });

    it('should return false if the value is not a string', () => {
      const result = component.isString(123);

      expect(result).toBe(false);
    });

    it('should return false if the value is null', () => {
      const result = component.isString(null);

      expect(result).toBe(false);
    });

    it('should return false if the value is undefined', () => {
      const result = component.isString(undefined);

      expect(result).toBe(false);
    });

    it('should return false if the value is an object', () => {
      const result = component.isString({});

      expect(result).toBe(false);
    });

    it('should return false if the value is an array', () => {
      const result = component.isString([]);

      expect(result).toBe(false);
    });
  });

  describe('compareObjects', () => {
    it('should return true if the objects are equal', () => {
      const obj1 = { a: 1, b: 'hello', c: [1, 2, 3] };
      const obj2 = { a: 1, b: 'hello', c: [1, 2, 3] };

      const result = component.compareObjects(obj1, obj2);

      expect(result).toBe(true);
    });

    it('should return false if the objects are not equal', () => {
      const obj1 = { a: 1, b: 'hello', c: [1, 2, 3] };
      const obj2 = { a: 1, b: 'world', c: [1, 2, 3] };

      const result = component.compareObjects(obj1, obj2);

      expect(result).toBe(false);
    });

    it('should return false if the objects have different keys', () => {
      const obj1 = { a: 1, b: 'hello', c: [1, 2, 3] };
      const obj2 = { a: 1, b: 'hello' };

      const result = component.compareObjects(obj1, obj2);

      expect(result).toBe(false);
    });

    it('should return false if one object is null', () => {
      const obj1 = { a: 1, b: 'hello', c: [1, 2, 3] };
      const obj2 = null;

      const result = component.compareObjects(obj1, obj2);

      expect(result).toBe(false);
    });

    it('should return false if one object is undefined', () => {
      const obj1 = { a: 1, b: 'hello', c: [1, 2, 3] };
      const obj2 = undefined;

      const result = component.compareObjects(obj1, obj2);

      expect(result).toBe(false);
    });
  });

  describe('Happy Path', () => {
    it('should populate filters on iteration correctly when selectedLevel is a string', () => {
      component.selectedLevel = 'sprint';
      component.selectedTab = 'iteration';
      component.filterData = {
        sprint: [
          { nodeId: 1, labelName: 'Project A', nodeName: 'Project A' },
          { nodeId: 2, labelName: 'Project B', nodeName: 'Project B' },
        ],
      };

      spyOn(helperService, 'sortAlphabetically' as any).and.returnValue(
        [{ nodeId: 1, labelName: 'Project A', nodeName: 'Project A' },
        { nodeId: 2, labelName: 'Project B', nodeName: 'Project B' }
        ]
      );
      component.populateFilters();
      fixture.detectChanges();
      expect(component.filters).toEqual(component.filterData.sprint);
      expect(helperService.sortAlphabetically).toHaveBeenCalledWith(
        component.filterData.sprint,
      );
    });

    it('should sort filters on iteration by specified field when primaryFilterConfig has sortBy', () => {
      component.selectedLevel = 'sprint';
      component.selectedTab = 'iteration';
      component.filterData = {
        sprint: [
          { nodeId: 2, labelName: 'Project B', nodeName: 'Project B' },
          { nodeId: 1, labelName: 'Project A', nodeName: 'Project A' },
        ],
      };
      component.primaryFilterConfig = { defaultLevel: { sortBy: 'labelName' } };
      spyOn(helperService, 'sortByField' as any).and.returnValue(
        [{ nodeId: 1, labelName: 'Project A', nodeName: 'Project A' },
        { nodeId: 2, labelName: 'Project B', nodeName: 'Project B' }
        ]
      );
      component.populateFilters();

      expect(component.filters).toEqual(component.filterData.sprint);
      expect(helperService.sortByField).toHaveBeenCalledWith(
        component.filterData.sprint,
        ['labelName', 'sprintStartDate']
      );
    });
  });

  describe('Happy Path', () => {
    it('should sort filters on release by specified field when primaryFilterConfig has sortBy', () => {
      component.selectedLevel = 'release';
      component.selectedTab = 'release';
      component.filterData = {
        release: [
          { nodeId: 2, labelName: 'Project B', nodeName: 'Project B' },
          { nodeId: 1, labelName: 'Project A', nodeName: 'Project A' },
        ],
      };
      component.primaryFilterConfig = { defaultLevel: { sortBy: 'labelName' } };
      spyOn(helperService, 'releaseSorting' as any).and.returnValue(
        [{ nodeId: 1, labelName: 'Project A', nodeName: 'Project A' },
        { nodeId: 2, labelName: 'Project B', nodeName: 'Project B' }
        ]
      );
      component.populateFilters();

      expect(component.filters).toEqual(component.filterData.release);
      expect(helperService.releaseSorting).toHaveBeenCalledWith(
        component.filterData.release
      );
    });
  });

  describe('Happy Path', () => {
    it('should sort filters on release by specified field when primaryFilterConfig has sortBy and selectedLevel is object', () => {
      component.selectedLevel = {
        nodeId: 1,
        emittedLevel: 'release'
      };
      component.selectedTab = 'release';
      component.filterData = {
        Release: [
          { parentId: 1, labelName: 'Project A', nodeName: 'Project A' },
          { parentId: 2, labelName: 'Project B', nodeName: 'Project B' },
        ],
      };
      component.primaryFilterConfig = { defaultLevel: { sortBy: 'nodeId' } };
      spyOn(helperService, 'releaseSorting' as any).and.returnValue(
        [{ parentId: 1, labelName: 'Project A', nodeName: 'Project A' },
        { parentId: 2, labelName: 'Project B', nodeName: 'Project B' }
        ]
      );
      component.populateFilters();

      expect(component.filters).toEqual(component.filterData.Release);
      expect(helperService.releaseSorting).toHaveBeenCalledWith(
        [component.filterData.Release[0]]
      );
    });
  });

  describe('Happy Path', () => {
    it('should sort filters on release by specified field when primaryFilterConfig has sortBy and selectedLevel is object', () => {
      component.selectedLevel = {
        nodeId: 1,
        emittedLevel: 'sprint'
      };
      component.selectedTab = 'iteration';
      component.filterData = {
        Sprint: [
          { nodeId: 1, labelName: 'Project A', nodeName: 'Project A' },
          { nodeId: 2, labelName: 'Project B', nodeName: 'Project B' },
        ],
      };
      component.primaryFilterConfig = { defaultLevel: { sortBy: 'nodeId' } };
      spyOn(helperService, 'sortByField' as any).and.returnValue(
        [{ nodeId: 1, labelName: 'Project A', nodeName: 'Project A' },
        { nodeId: 2, labelName: 'Project B', nodeName: 'Project B' }
        ]
      );
      component.populateFilters();

      expect(component.filters).toEqual(component.filterData.Sprint);
      expect(helperService.sortByField).toHaveBeenCalledWith(
        [], ['nodeId', 'sprintStartDate']
      );
    });
  });

  describe('applyDefaultFilters', () => {
    it('should set selectedFilters based on stateFilters primary_level when conditions are met', (done) => {
      component.hierarchyLevels = ['Level 1', 'Level 2'];
      component.selectedLevel = 'Level 1';
      component.filterData = { 'Level 1': [{ nodeId: 1, labelName: 'Level 1' }] }

      component.primaryFilterConfig = {
        defaultLevel: { labelName: 'Level 1' },
        type: 'singleSelect',
      };
      component.stateFilters = {
        primary_level: [{ labelName: 'Level 1', nodeId: 1 }],
        parent_level: null
      };

      component.applyDefaultFilters();

      setTimeout(() => {
        expect(component.selectedFilters).toEqual(
          jasmine.arrayContaining([
            jasmine.objectContaining({ labelName: 'Level 1', nodeId: 1 })
          ])
        );
        done();
      }, 200);
    });

    it('should reset selectedFilters and call applyPrimaryFilters when conditions are met', (done) => {
      component.hierarchyLevels = ['Level 1', 'Level 2'];
      component.selectedLevel = 'Level 1';
      component.filterData = { 'Level 1': [{ nodeId: 1, labelName: 'Level 1' }] }
      component.primaryFilterConfig = {
        defaultLevel: { labelName: 'Level 1' },
        type: 'singleSelect',
      };
      component.stateFilters = {
        primary_level: [{ labelName: 'Level 2', nodeId: 2 }],
      };
      spyOn(component, 'applyPrimaryFilters');

      component.applyDefaultFilters();

      setTimeout(() => {
        expect(component.selectedFilters).toEqual([{ labelName: 'Level 1', nodeId: 1 }]);
        expect(component.applyPrimaryFilters).toHaveBeenCalled();
        done();
      }, 200);
    });

    it('should set selectedFilters based on stateFilters parent_level when conditions are met', (done) => {
      component.hierarchyLevels = ['Level 1', 'Level 2'];
      component.selectedLevel = 'Level 1';
      component.filterData = { 'Level 1': [{ nodeId: 1, labelName: 'Level 1' }] }
      component.primaryFilterConfig = {
        defaultLevel: { labelName: 'Level 1' },
        type: 'singleSelect',
      };
      component.stateFilters = {
        parent_level: { labelName: 'Level 1', nodeId: 1 },
      };

      component.applyDefaultFilters();

      setTimeout(() => {
        expect(component.selectedFilters).toEqual(
          jasmine.arrayContaining([
            jasmine.objectContaining({ labelName: 'Level 1', nodeId: 1 })
          ])
        );
        done();
      }, 200);
    });

    it('should emit the correct nested object when conditions are met', (done) => {
      component.hierarchyLevels = ['Level 1', 'Level 2'];
      component.selectedLevel = 'Level 1';
      component.filterData = { 'Level 1': [{ nodeId: 1, labelName: 'Level 1' }] };
      component.primaryFilterConfig = {
        defaultLevel: { labelName: 'Level 1' },
        type: 'singleSelect',
      };
      component.stateFilters = {};

      spyOn(component.onPrimaryFilterChange, 'emit');

      component.applyDefaultFilters();

      setTimeout(() => {
        expect(component.selectedFilters).toEqual([{ nodeId: 1, labelName: 'Level 1' }]);
        expect(component.onPrimaryFilterChange.emit).toHaveBeenCalledWith({
          additional_level: {
            primary_level: [{ labelName: 'Label 1', nodeId: 'node-1' }]
          },
          primary_level: [{ nodeId: 1, labelName: 'Level 1' }]
        });
        done();
      }, 200);
    });



    it('should set selectedFilters based on stateFilters parent_level for sprint/release when conditions are met', (done) => {
      component.hierarchyLevels = ['sprint'];
      component.selectedLevel = 'sprint';
      component.filterData = { 'sprint': [{ nodeId: 2, labelName: 'Level 2' }] }
      component.primaryFilterConfig = {
        defaultLevel: { labelName: 'sprint' },
        type: 'singleSelect',
      };
      component.stateFilters = {
        parent_level: [{ labelName: 'Level 1', nodeId: 1 }],
      };
      spyOn(component.onPrimaryFilterChange, 'emit');

      component.applyDefaultFilters();

      setTimeout(() => {
        expect(component.selectedFilters).toBe(undefined);
        expect(component.onPrimaryFilterChange.emit).toHaveBeenCalledWith([]);
        done();
      }, 200);
    });

    it('should reset selectedFilters and call applyPrimaryFilters for sprint/release  when conditions are met', (done) => {
      component.hierarchyLevels = ['sprint', 'Level 2'];
      component.selectedLevel = 'sprint';
      component.filterData = { 'sprint': [{ nodeId: 1, labelName: 'sprint' }] }
      component.primaryFilterConfig = {
        defaultLevel: { labelName: 'sprint' },
        type: 'singleSelect',
      };
      component.service.setBackupOfFilterSelectionState({
        primary_level: [{ labelName: 'sprint', nodeId: 1 }],
      });
      spyOn(component, 'applyPrimaryFilters');

      component.applyDefaultFilters();

      setTimeout(() => {
        expect(component.selectedFilters).toEqual([{ labelName: 'sprint', nodeId: 1 }]);
        expect(component.applyPrimaryFilters).toHaveBeenCalled();
        done();
      }, 200);
    });

    it('should set selectedFilters based on stateFilters parent_level for sprint/release when conditions are met', (done) => {
      component.hierarchyLevels = ['sprint', 'Level 2'];
      component.selectedLevel = 'sprint';
      component.filterData = { 'sprint': [{ nodeId: 1, labelName: 'sprint' }] }
      component.primaryFilterConfig = {
        defaultLevel: { labelName: 'sprint' },
        type: 'singleSelect',
      };
      component.stateFilters = {
        parent_level: { labelName: 'sprint', nodeId: 1 },
      };

      component.applyDefaultFilters();

      setTimeout(() => {
        expect(component.selectedFilters).toEqual(
          jasmine.arrayContaining([
            jasmine.objectContaining({ labelName: 'sprint', nodeId: 1 })
          ])
        );
        done();
      }, 200);
    });

    it('should set selectedFilters to empty array and call onPrimaryFilterChange for sprint/release when conditions are met', (done) => {
      component.hierarchyLevels = ['sprint', 'Level 2'];
      component.selectedLevel = 'sprint';
      component.filterData = { 'sprint': [{ nodeId: 1, labelName: 'sprint' }] }
      component.primaryFilterConfig = {
        defaultLevel: { labelName: 'sprint' },
        type: 'singleSelect',
      };
      component.stateFilters = {};

      spyOn(component.onPrimaryFilterChange, 'emit');

      component.applyDefaultFilters();

      setTimeout(() => {
        expect(component.onPrimaryFilterChange.emit).toHaveBeenCalledWith([]);
        done();
      }, 200);
    });
  });

  it('should reset the component state', () => {
    component.filters = ['filter1', 'filter2'];
    component.selectedFilters = ['filter3'];
    spyOn(component, 'applyPrimaryFilters');
    // spyOn(helperService, 'setBackupOfFilterSelectionState');

    component.reset();

    expect(component.selectedFilters).toEqual(['filter1']);
    expect(sharedService.setBackupOfFilterSelectionState).toHaveBeenCalledWith({ 'parent_level': null, 'primary_level': null });
    expect(component.applyPrimaryFilters).toHaveBeenCalledWith({});
  });

  describe('AdditionalFilterComponent.onDropDownChange() onDropDownChange method', () => {
    describe('Happy Path', () => {
      it('should apply additional filter when dropdown element is selected', () => {
        // Arrange
        const event = { value: 'someValue' };
        const index = 1;
        spyOn(helperService, 'isDropdownElementSelected')
          .and.returnValue(true);

        // Act
        component.onDropdownChange(event);

        // Assert
        expect(helperService.isDropdownElementSelected).toHaveBeenCalledWith(
          event,
        );
        // expect(service.applyAdditionalFilters).toHaveBeenCalled();
      });
    });

    describe('Edge Cases', () => {
      it('should not apply additional filter when dropdown element is not selected', () => {
        // Arrange
        const event = { value: 'someValue' };
        const index = 1;
        spyOn(helperService, 'isDropdownElementSelected')
          .and.returnValue(false);

        // Act
        component.onDropdownChange(event);

        // Assert
        expect(helperService.isDropdownElementSelected).toHaveBeenCalledWith(
          event,
        );
        // expect(service.applyAdditionalFilters).not.toHaveBeenCalled();
      });

      it('should handle undefined event gracefully', () => {
        // Arrange
        const event = undefined;
        const index = 1;
        spyOn(helperService, 'isDropdownElementSelected')
          .and.returnValue(false);

        // Act
        component.onDropdownChange(event);

        // Assert
        expect(helperService.isDropdownElementSelected).toHaveBeenCalledWith(
          event,
        );
        // expect(service.applyAdditionalFilters).not.toHaveBeenCalled();
      });
    });
  });

  describe('PrimaryFilterComponent: isFilterHidden', () => {

    it('should return true for isFilterHidden when selectedTab is iteration and there are active filters', () => {
      component.selectedTab = 'iteration';
      const filterDataSet = [{ sprintState: 'active' }, { sprintState: 'inactive' }];
      const result = component.isFilterHidden(filterDataSet);
      expect(result).toBe(true);
    });

    it('should return false when selectedTab is not iteration', () => {
      component.selectedTab = 'release';
      const result = component.isFilterHidden([]);
      expect(result).toBe(false);
    });

    it('should return false for isFilterHidden when no active sprints', () => {
      const result = component.isFilterHidden(component.filters);
      expect(result).toBe(false);
    });

  });
});
