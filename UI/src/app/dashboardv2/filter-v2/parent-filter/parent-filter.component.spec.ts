import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ParentFilterComponent } from './parent-filter.component';

import { RouterTestingModule } from '@angular/router/testing';
import { SharedService } from '../../../services/shared.service';
import { HelperService } from 'src/app/services/helper.service';
import { GetAuthService } from '../../../services/getauth.service';
import { HttpClientModule } from '@angular/common/http';
import { APP_CONFIG, AppConfig } from '../../../services/app.config';
import { CUSTOM_ELEMENTS_SCHEMA, EventEmitter } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpService } from '../../../services/http.service';
import { CommonModule, DatePipe } from '@angular/common';

describe('ParentFilterComponent', () => {
  let component: ParentFilterComponent;
  let fixture: ComponentFixture<ParentFilterComponent>;
  let getAuth: GetAuthService;
  let httpService: HttpService
  let sharedService: SharedService;
  let helperService: HelperService;
  let mockEventEmitter: EventEmitter<any>;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ParentFilterComponent],
      imports: [RouterTestingModule, HttpClientModule, BrowserAnimationsModule],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],

      providers: [SharedService, GetAuthService, HttpService, HelperService, CommonModule, DatePipe,
        { provide: APP_CONFIG, useValue: AppConfig }
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(ParentFilterComponent);
    component = fixture.componentInstance;
    getAuth = TestBed.get(GetAuthService);
    httpService = TestBed.inject(HttpService);
    sharedService = TestBed.inject(SharedService);
    helperService = TestBed.inject(HelperService);

    component.parentFilterConfig = { labelName: "Organization Level" };
    component.filterData = {
      sprint: [{ nodeId: 1, nodeName: 'Node 1' }],
      Level2: [{ nodeId: 2, nodeName: 'Node 2' }]
    };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });


  it('should update filterLevels and selectedLevel when selectedTab or selectedType changes and parentFilterConfig labelName is Organization Level', () => {
    component.selectedTab = 'developer';
    component.selectedType = 'scrum';
    component.filterData = {
      Level1: [{ nodeId: 1, nodeName: 'Node 1' }],
      Level2: [{ nodeId: 2, nodeName: 'Node 2' }]
    };
    component.additionalFilterLevels = ['Level2'];
    component.parentFilterConfig = { labelName: 'Organization Level' };
    spyOn(sharedService, 'getBackupOfFilterSelectionState').and.returnValue('Level1');
    spyOn(sharedService, 'setBackupOfFilterSelectionState');
    spyOn(component.onSelectedLevelChange, 'emit');

    component.ngOnChanges({
      selectedTab: {
        currentValue: 'developer', previousValue: null,
        firstChange: false,
        isFirstChange: function (): boolean {
          return false;
        }
      },
      selectedType: {
        currentValue: 'scrum', previousValue: null,
        firstChange: false,
        isFirstChange: function (): boolean {
          return false;
        }
      },
      parentFilterConfig: {
        currentValue: { labelName: 'Organization Level' }, previousValue: null,
        firstChange: false,
        isFirstChange: function (): boolean {
          return false;
        }
      }
    });

    expect(component.filterLevels).toEqual([{ nodeId: 'Level1', nodeName: 'Level1' }]);
    // expect(component.selectedLevel).toEqual('LEVEL1');
    expect(sharedService.getBackupOfFilterSelectionState).toHaveBeenCalledWith('parent_level');
    // expect(helperService.setBackupOfFilterSelectionState).toHaveBeenCalledWith({ 'parent_level': 'LEVEL1' });
    // expect(component.onSelectedLevelChange.emit).toHaveBeenCalledWith('level1');
  });

  it('should update filterLevels and selectedLevel when selectedTab or selectedType changes and parentFilterConfig labelName is not Organization Level', () => {
    component.selectedTab = 'iteration';
    component.selectedType = 'Type';
    component.filterData = {
      level1: [{ nodeId: 1, nodeName: 'Node 1' }, { nodeId: 4, nodeName: 'Node 4' }],
      level2: [{ nodeId: 2, nodeName: 'Node 2' }],
      level3: [{ nodeId: 3, nodeName: 'Node 3' }],
    };
    component.parentFilterConfig = { labelName: 'level1' };
    component.selectedLevel = null;
    component.stateFilters = null;
    spyOn(sharedService, 'getBackupOfFilterSelectionState').and.returnValue('project');
    spyOn(sharedService, 'setBackupOfFilterSelectionState');
    spyOn(component.onSelectedLevelChange, 'emit');

    component.ngOnChanges({
      selectedTab: {
        currentValue: 'iteration', previousValue: 'speed',
        firstChange: false,
        isFirstChange: function (): boolean {
          return false;
        }
      },
      selectedType: {
        currentValue: 'scrum', previousValue: 'kanban',
        firstChange: false,
        isFirstChange: function (): boolean {
          return false;
        }
      },
      parentFilterConfig: {
        currentValue: { labelName: 'Level1' }, previousValue: {
          labelName: 'Organization Level'
        },
        firstChange: false,
        isFirstChange: function (): boolean {
          return false;
        }
      }
    });

    expect(sharedService.getBackupOfFilterSelectionState).toHaveBeenCalledWith('primary_level');
  });

  xit('should emit selectedLevel when parentFilterConfig labelName is Organization Level', () => {
    component.parentFilterConfig = { labelName: 'Organization Level' };
    spyOn(component.onSelectedLevelChange, 'emit');
    spyOn(sharedService, 'setBackupOfFilterSelectionState');

    component.handleSelectedLevelChange();

    expect(component.onSelectedLevelChange.emit).toHaveBeenCalledWith(component.selectedLevel.toLowerCase());
    expect(sharedService.setBackupOfFilterSelectionState).toHaveBeenCalledWith({ 'parent_level': component.selectedLevel.toLowerCase(), 'primary_level': null });
  });

  it('should emit selectedNode when parentFilterConfig labelName is not Organization Level', () => {
    component.parentFilterConfig = { labelName: 'Level1' };
    component.filterData = { Level1: [{ nodeId: 1, nodeName: 'Node 1' }] };
    component.selectedLevel = 'Node 1';
    spyOn(component.onSelectedLevelChange, 'emit');
    spyOn(sharedService, 'setBackupOfFilterSelectionState');

    component.handleSelectedLevelChange();
  });


  it('should fill additionalFilterLevels with keys that have a higher level than project', () => {
    component.filterData = {
      Project: [{ level: 2 }],
      department: [{ level: 3 }],
      team: [{ level: 4 }],
    };
    component.fillAdditionalFilterLevels();

    expect(component.additionalFilterLevels).toEqual(['department', 'team']);
  });

  it('should not fill additionalFilterLevels if project has no length', () => {
    component.filterData = {
      project: [{ level: 2 }],
      department: [{ level: 3 }],
      team: [{ level: 4 }],
    };

    component.filterData['project'] = [];
    component.fillAdditionalFilterLevels();

    expect(component.additionalFilterLevels).toEqual([]);
  });

  it('should not fill additionalFilterLevels if no keys have a higher level than project', () => {
    component.filterData = {
      project: [{ level: 2 }],
      department: [{ level: 1 }],
      team: [{ level: 1 }],
    };
    component.fillAdditionalFilterLevels();

    expect(component.additionalFilterLevels).toEqual([]);
  });

  describe('ngOnChanges', () => {
    it('should handle parentFilterConfig changes for Organization Level', () => {
      component.filterData = {
        Level1: [{ nodeId: 1, nodeName: 'Node 1' }],
        Level2: [{ nodeId: 2, nodeName: 'Node 2' }]
      };
      const changes = {
        parentFilterConfig: {
          currentValue: { labelName: 'Organization Level' },
          previousValue: null,
          firstChange: false,
          isFirstChange: () => true
        }
      };

      spyOn(component, 'fillAdditionalFilterLevels');
      spyOn(component.service, 'getBackupOfFilterSelectionState').and.returnValue('parent_level');
      spyOn(component, 'handleSelectedLevelChange');

      component.ngOnChanges(changes);

      expect(component.fillAdditionalFilterLevels).toHaveBeenCalled();
      expect(component.filterLevels).toEqual([
        { nodeId: 'Level1', nodeName: 'Level1' },
        { nodeId: 'Level2', nodeName: 'Level2' }
      ]);
    });

    it('should handle parentFilterConfig changes for other levels', () => {
      component.filterData = {
        Level1: [{ nodeId: 1, nodeName: 'Node 1' }],
        Level2: [{ nodeId: 2, nodeName: 'Node 2' }]
      };
      const changes = {
        parentFilterConfig: {
          currentValue: { labelName: 'Level1' },
          previousValue: null,
          firstChange: false,
          isFirstChange: () => true
        }
      };

      spyOn(component.helperService, 'sortAlphabetically').and.returnValue([
        'Level1', 'Level2'
      ]);
      spyOn(component.service, 'getBackupOfFilterSelectionState').and.returnValue('primary_level');
      spyOn(component, 'handleSelectedLevelChange');

      component.ngOnChanges(changes);

      expect(component.filterLevels).toEqual([
        'Level1', 'Level2'
      ]);
    });

    it('should not handle changes if parentFilterConfig is not changed', () => {
      component.filterData = {
        Level1: [{ nodeId: 1, nodeName: 'Node 1' }],
        Level2: [{ nodeId: 2, nodeName: 'Node 2' }]
      };
      const changes = {
        parentFilterConfig: null
      };

      spyOn(component, 'fillAdditionalFilterLevels');
      spyOn(component.service, 'getBackupOfFilterSelectionState');
      spyOn(component, 'handleSelectedLevelChange');

      component.ngOnChanges(changes);

      expect(component.fillAdditionalFilterLevels).not.toHaveBeenCalled();
      expect(component.service.getBackupOfFilterSelectionState).not.toHaveBeenCalled();
      expect(component.handleSelectedLevelChange).not.toHaveBeenCalled();
    });
  });


  it('should handle parentFilterConfig changes for Organization Level when statefilters are present', () => {
    component.service.setBackupOfFilterSelectionState({ 'parent_level': {nodeId: 'Level1', nodeName: 'Level1'}, 'primary_level': null });
    component.filterData = {
      Level1: [{ nodeId: 1, nodeName: 'Node 1' }],
      Level2: [{ nodeId: 2, nodeName: 'Node 2' }]
    };
    const changes = {
      parentFilterConfig: {
        currentValue: { labelName: 'Organization Level' },
        previousValue: null,
        firstChange: false,
        isFirstChange: () => true
      }
    };

    spyOn(component, 'fillAdditionalFilterLevels');
    spyOn(component, 'handleSelectedLevelChange');

    component.ngOnChanges(changes);

    expect(component.fillAdditionalFilterLevels).toHaveBeenCalled();
    expect(component.filterLevels).toEqual([
      { nodeId: 'Level1', nodeName: 'Level1' },
      { nodeId: 'Level2', nodeName: 'Level2' }
    ]);
  });

  it('should handle parentFilterConfig changes for other levels  when statefilters are present', () => {
    component.service.setBackupOfFilterSelectionState({ 'parent_level': {labelName: 'Level1'} });
    component.filterData = {
      Level1: [{ nodeId: 1, nodeName: 'Node 1' }],
      Level2: [{ nodeId: 2, nodeName: 'Node 2' }]
    };
    const changes = {
      parentFilterConfig: {
        currentValue: { labelName: 'Level1' },
        previousValue: null,
        firstChange: false,
        isFirstChange: () => true
      }
    };

    spyOn(component.helperService, 'sortAlphabetically').and.returnValue([
      'Level1', 'Level2'
    ]);
    spyOn(component, 'handleSelectedLevelChange');

    component.ngOnChanges(changes);

    expect(component.filterLevels).toEqual([
     'Level1', 'Level2'
    ]);
  });

  it('should handle parentFilterConfig changes for other levels  when statefilters with primary level are present', () => {
    component.service.setBackupOfFilterSelectionState({ 'primary_level': [{parentId: 1} ]});
    component.filterData = {
      sprint: [{ nodeId: 1, nodeName: 'Node 1' }],
      Level2: [{ nodeId: 2, nodeName: 'Node 2' }]
    };
    const changes = {
      parentFilterConfig: {
        currentValue: { labelName: 'sprint' },
        previousValue: null,
        firstChange: false,
        isFirstChange: () => true
      }
    };

    spyOn(component.helperService, 'sortAlphabetically').and.returnValue([
      'Level2', 'sprint'
    ]);
    spyOn(component, 'handleSelectedLevelChange');

    component.ngOnChanges(changes);

    expect(component.filterLevels).toEqual([
      'Level2', 'sprint'
    ]);
  });

  it('should not handle changes if parentFilterConfig is not changed  when statefilters are present', () => {
    component.filterData = {
      Level1: [{ nodeId: 1, nodeName: 'Node 1' }],
      Level2: [{ nodeId: 2, nodeName: 'Node 2' }]
    };
    const changes = {
      parentFilterConfig: null
    };

    spyOn(component, 'fillAdditionalFilterLevels');
    spyOn(component.service, 'getBackupOfFilterSelectionState');
    spyOn(component, 'handleSelectedLevelChange');

    component.ngOnChanges(changes);

    expect(component.fillAdditionalFilterLevels).not.toHaveBeenCalled();
    expect(component.service.getBackupOfFilterSelectionState).not.toHaveBeenCalled();
    expect(component.handleSelectedLevelChange).not.toHaveBeenCalled();
  });

  describe('ParentFilterComponent.onDropdownChange() onDropdownChange method', () => {
    describe('Happy Path', () => {
      it('should handle dropdown change when an element is selected', () => {
        // Arrange
        component.selectedLevel = { nodeName: 'someValue' };
        const eventMock = { value: 'someValue' };
        spyOn(helperService,'isDropdownElementSelected').and.returnValue(true);
        spyOn(sharedService, 'setBackupOfFilterSelectionState');
        // Act
        component.onDropdownChange(eventMock);
  
        // Assert
        expect(helperService.isDropdownElementSelected).toHaveBeenCalledWith(
          eventMock,
        );
        expect(
          sharedService.setBackupOfFilterSelectionState,
        ).toHaveBeenCalled();
      });
    });
  
    describe('Edge Cases', () => {
      it('should not handle dropdown change when no element is selected', () => {
        // Arrange
        const eventMock = { value: null };
        spyOn(helperService,'isDropdownElementSelected').and.returnValue(false);
        spyOn(sharedService, 'setBackupOfFilterSelectionState');
        // Act
        component.onDropdownChange(eventMock);
  
        // Assert
        expect(helperService.isDropdownElementSelected).toHaveBeenCalledWith(
          eventMock,
        );
        expect(
          sharedService.setBackupOfFilterSelectionState,
        ).not.toHaveBeenCalled();
      });
  
      it('should handle dropdown change with undefined event', () => {
        // Arrange
        const eventMock = undefined;
        spyOn(helperService,'isDropdownElementSelected').and.returnValue(false);
        spyOn(sharedService, 'setBackupOfFilterSelectionState');
        // Act
        component.onDropdownChange(eventMock);
  
        // Assert
        expect(helperService.isDropdownElementSelected).toHaveBeenCalledWith(
          eventMock,
        );
        expect(
          sharedService.setBackupOfFilterSelectionState,
        ).not.toHaveBeenCalled();
      });
    });
  });

  describe('ParentFilterComponent.handleSelectedLevelChange() handleSelectedLevelChange method', () => {
    beforeEach(() => {
      mockEventEmitter = new EventEmitter<any>();
      component.onSelectedLevelChange = mockEventEmitter;
      spyOn(sharedService, 'setBackupOfFilterSelectionState');
    });
    describe('Happy Path', () => {
      it('should emit selected level nodeName when labelName is Organization Level', () => {
        // Arrange
        component.parentFilterConfig = { labelName: 'Organization Level' } as any;
        component.selectedLevel = { nodeName: 'Level1' } as any;
        spyOn(mockEventEmitter, 'emit');
        // Act
        component.handleSelectedLevelChange();
  
        // Assert
        expect(mockEventEmitter.emit).toHaveBeenCalledWith('Level1');
        expect(
          sharedService.setBackupOfFilterSelectionState,
        ).toHaveBeenCalledWith({ parent_level: 'Level1' });
      });
  
      it('should emit selected node details when labelName is not Organization Level', () => {
        // Arrange
        component.parentFilterConfig = { labelName: 'Some Level' } as any;
        component.selectedLevel = { nodeId: 'node1' } as any;
        component.filterData = {
          'Some Level': [{ nodeId: 'node1', nodeName: 'Node1' }],
        } as any;
        spyOn(mockEventEmitter, 'emit');
        // Act
        component.handleSelectedLevelChange();
  
        // Assert
        expect(mockEventEmitter.emit).toHaveBeenCalledWith({
          nodeId: 'node1',
          nodeType: 'Some Level',
          emittedLevel: undefined,
          fullNodeDetails: [{ nodeId: 'node1', nodeName: 'Node1' }],
        });
        expect(
          sharedService.setBackupOfFilterSelectionState,
        ).toHaveBeenCalledWith({
          parent_level: { nodeId: 'node1', nodeName: 'Node1' },
        });
      });
    });
  
    describe('Edge Cases', () => {
      it('should handle parentLevelChanged flag correctly for Organization Level', () => {
        // Arrange
        component.parentFilterConfig = { labelName: 'Organization Level' } as any;
        component.selectedLevel = { nodeName: 'Level1' } as any;
  
        // Act
        component.handleSelectedLevelChange(true);
  
        // Assert
        expect(
          sharedService.setBackupOfFilterSelectionState,
        ).toHaveBeenCalledWith({ parent_level: 'Level1', primary_level: null });
      });
  
      it('should handle parentLevelChanged flag correctly for non-Organization Level', () => {
        // Arrange
        component.parentFilterConfig = { labelName: 'Some Level' } as any;
        component.selectedLevel = { nodeId: 'node1' } as any;
        component.filterData = {
          'Some Level': [{ nodeId: 'node1', nodeName: 'Node1' }],
        } as any;
  
        // Act
        component.handleSelectedLevelChange(true);
  
        // Assert
        expect(
          sharedService.setBackupOfFilterSelectionState,
        ).toHaveBeenCalledWith({
          parent_level: { nodeId: 'node1', nodeName: 'Node1' },
          primary_level: null,
        });
      });
  
      it('should not emit if selectedLevel is undefined', () => {
        // Arrange
        component.parentFilterConfig = { labelName: 'Some Level' } as any;
        component.selectedLevel = undefined;
        spyOn(mockEventEmitter, 'emit');
        // Act
        component.handleSelectedLevelChange();
  
        // Assert
        expect(mockEventEmitter.emit).not.toHaveBeenCalled();
      });
    });
  });
});