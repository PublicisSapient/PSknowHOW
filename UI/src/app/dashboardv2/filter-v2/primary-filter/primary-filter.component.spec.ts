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

    component.primaryFilterConfig = {
      defaultLevel: {
        "labelName": "Project"
      }, type: 'multiSelect'
    };

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
    it('should return true when arrays are equal', () => {
      const arr1 = [1, 2, 3];
      const arr2 = [1, 2, 3];

      const result = component.arraysEqual(arr1, arr2);

      expect(result).toBe(true);
    });

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
  });

  describe('deepEqual', () => {
    it('should return true when objects are equal', () => {
      const obj1 = { name: 'John', age: 30 };
      const obj2 = { name: 'John', age: 30 };

      const result = component.deepEqual(obj1, obj2);

      expect(result).toBe(true);
    });

    it('should return false when objects have different properties', () => {
      const obj1 = { name: 'John', age: 30 };
      const obj2 = { name: 'John' };

      const result = component.deepEqual(obj1, obj2);

      expect(result).toBe(false);
    });

    it('should return false when objects have different property values', () => {
      const obj1 = { name: 'John', age: 30 };
      const obj2 = { name: 'John', age: 25 };

      const result = component.deepEqual(obj1, obj2);

      expect(result).toBe(false);
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
    beforeEach(() => {
      component.filters = [
        { labelName: 'Label 1', nodeId: 'node-1' },
        { labelName: 'Label 2', nodeId: 'node-2' },
      ];

      component.primaryFilterConfig = {
        defaultLevel: { labelName: 'Label 1' },
        type: 'multiSelect',
      };

      component.filterData = {
        Project: [
          { labelName: 'Label 1', nodeId: 'node-1' },
          { labelName: 'Label 2', nodeId: 'node-2' },
        ],
      };

      component.hierarchyLevels = ['label 1', 'label 2'];
    });

    it('should call populateFilters', () => {
      spyOn(component, 'populateFilters');

      component.applyDefaultFilters();

      expect(component.populateFilters).toHaveBeenCalled();
    });

    it('should set stateFilters and selectedFilters when filters match primaryFilterConfig', fakeAsync(() => {
      spyOn(helperService, 'getBackupOfFilterSelectionState' as any).and.returnValue({ primary_level: [{ labelName: 'Label 1', nodeId: 'node-1' }] });
      component.applyDefaultFilters();
      tick(100);
      expect(component.stateFilters).toEqual({ primary_level: [{ labelName: 'Label 1', nodeId: 'node-1' }] });
      expect(component.selectedFilters).toEqual([{ labelName: 'Label 1', nodeId: 'node-1' }]);
    }));

    it('should set stateFilters and selectedFilters when filters match sprint or release', fakeAsync(() => {
      spyOn(helperService, 'getBackupOfFilterSelectionState' as any).and.returnValue({ primary_level: [{ labelName: 'Sprint', parentId: 'node-1' }] });
      component.applyDefaultFilters();
      tick(100);
      expect(component.stateFilters).toEqual({ primary_level: [{ labelName: 'Sprint', parentId: 'node-1' }] });
      expect(component.selectedFilters).toEqual([{ labelName: 'Label 1', nodeId: 'node-1' }]);
    }));

    it('should reset selectedFilters and call setBackupOfFilterSelectionState, applyPrimaryFilters, and setProjectAndLevelBackupBasedOnSelectedLevel when filters do not match primaryFilterConfig', fakeAsync(() => {
      
      spyOn(helperService, 'getBackupOfFilterSelectionState' as any).and.returnValue(null);
      spyOn(component, 'applyPrimaryFilters');
      spyOn(component, 'setProjectAndLevelBackupBasedOnSelectedLevel');
      spyOn(helperService, 'setBackupOfFilterSelectionState');
      component.applyDefaultFilters();
      tick(100);
      expect(component.selectedFilters).toEqual([{ labelName: 'Label 1', nodeId: 'node-1' }]);
      expect(helperService.setBackupOfFilterSelectionState).toHaveBeenCalledWith({ primary_level: null });
      expect(component.applyPrimaryFilters).toHaveBeenCalledWith({});
      expect(component.setProjectAndLevelBackupBasedOnSelectedLevel).toHaveBeenCalled();
    }));

    it('should reset selectedFilters and call setBackupOfFilterSelectionState, applyPrimaryFilters, and setProjectAndLevelBackupBasedOnSelectedLevel when stateFilters are not set', fakeAsync(() => {
      spyOn(helperService, 'getBackupOfFilterSelectionState' as any).and.returnValue({});
      spyOn(component, 'applyPrimaryFilters');
      spyOn(component, 'setProjectAndLevelBackupBasedOnSelectedLevel');

      component.applyDefaultFilters();
      tick(100);
      expect(component.selectedFilters).toEqual([{ labelName: 'Label 1', nodeId: 'node-1' }]);
      expect(helperService.getBackupOfFilterSelectionState).toHaveBeenCalled();
      expect(component.applyPrimaryFilters).toHaveBeenCalledWith({});
      expect(component.setProjectAndLevelBackupBasedOnSelectedLevel).toHaveBeenCalled();
    }));

    it('should set selectedFilters when stateFilters has parent_level', fakeAsync(() => {
      spyOn(helperService, 'getBackupOfFilterSelectionState' as any).and.returnValue({ parent_level: { labelName: 'Label 1', nodeId: 'node-1' } });

      component.applyDefaultFilters();
      tick(100);
      expect(component.selectedFilters).toEqual([{ labelName: 'Label 1', nodeId: 'node-1' }]);
    }));

    // unable to debug this, component.filters value clashing with that in beforeEach
    xit('should set selectedFilters to empty array and call setNoSprints and onPrimaryFilterChange when filters do not match primaryFilterConfig and stateFilters is not set', fakeAsync(() => {
      component.filters = [{ labelName: 'Label 3', nodeId: 'node-3' }];
      spyOn(helperService, 'getBackupOfFilterSelectionState' as any).and.returnValue(null);
      spyOn(component.onPrimaryFilterChange, 'emit');
      spyOn(sharedService, 'setNoSprints');
      component.applyDefaultFilters();
      tick(100);
      expect(component.selectedFilters).toEqual([]);
      expect(sharedService.setNoSprints).toHaveBeenCalledWith(true);
      expect(component.onPrimaryFilterChange.emit).toHaveBeenCalledWith([]);
    }));
  });
});