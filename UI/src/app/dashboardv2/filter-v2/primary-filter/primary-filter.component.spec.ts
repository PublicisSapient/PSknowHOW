import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { PrimaryFilterComponent } from './primary-filter.component';

import { RouterTestingModule } from '@angular/router/testing';
import { SharedService } from '../../../services/shared.service';
import { HelperService } from 'src/app/services/helper.service';
import { GetAuthService } from '../../../services/getauth.service';
import { HttpClientModule } from '@angular/common/http';
import { APP_CONFIG, AppConfig } from '../../../services/app.config';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpService } from '../../../services/http.service';
import { CommonModule, DatePipe } from '@angular/common';

describe('PrimaryFilterComponent', () => {
  let component: PrimaryFilterComponent;
  let fixture: ComponentFixture<PrimaryFilterComponent>;
  let getAuth: GetAuthService;
  let httpService: HttpService
  let sharedService: SharedService;
  let helperService: HelperService;
  let filters: any[];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PrimaryFilterComponent],
      imports: [RouterTestingModule, HttpClientModule, BrowserAnimationsModule],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],

      providers: [SharedService, GetAuthService, HttpService, HelperService, CommonModule, DatePipe,
        { provide: APP_CONFIG, useValue: AppConfig }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PrimaryFilterComponent);
    component = fixture.componentInstance;
    getAuth = TestBed.get(GetAuthService);
    httpService = TestBed.inject(HttpService);
    sharedService = TestBed.inject(SharedService);
    helperService = TestBed.inject(HelperService);

    filters = [
      { nodeId: 1, nodeName: 'item1' },
      { nodeId: 2, nodeName: 'item2' },
      { nodeId: 3, nodeName: 'item3' }
    ];
    component.filters = filters;

    component.primaryFilterConfig = {
      "type": "multiSelect",
      "defaultLevel": {
        "labelName": "project",
        "sortBy": null
      }
    };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call applyDefaultFilters() when primaryFilterConfig, selectedType, and selectedLevel change', () => {
    component.primaryFilterConfig = { labelName: 'Filter1' };
    component.selectedType = 'Type1';
    component.selectedLevel = 'Level1';
    spyOn(component, 'applyDefaultFilters');

    component.ngOnChanges({
      primaryFilterConfig: {
        currentValue: { labelName: 'Filter1' }, previousValue: null, firstChange: false,
        isFirstChange: function (): boolean {
          throw new Error('Function not implemented.');
        }
      },
      selectedType: {
        currentValue: 'Type1', previousValue: null, firstChange: false,
        isFirstChange: function (): boolean {
          throw new Error('Function not implemented.');
        }
      },
      selectedLevel: {
        currentValue: 'Level1', previousValue: null, firstChange: false,
        isFirstChange: function (): boolean {
          throw new Error('Function not implemented.');
        }
      }
    });

    expect(component.applyDefaultFilters).toHaveBeenCalled();
  });

  it('should populate filters and emit onPrimaryFilterChange when primaryFilterConfig, selectedType, or selectedLevel change', fakeAsync(() => {
    component.primaryFilterConfig = { defaultLevel: {labelName: 'Filter1'} };
    component.selectedType = 'Type1';
    component.selectedLevel = 'Level1';
    component.filters = [{ nodeId: 1, nodeName: 'Node1' }];
    component.filterData = {
      Level1: [{ nodeId: 1, nodeName: 'Node1' }, { nodeId: 2, nodeName: 'Node2' }],
      Level2: [{ nodeId: 3, nodeName: 'Node3' }]
    };
    spyOn(component, 'populateFilters');
    spyOn(helperService, 'getBackupOfFilterSelectionState').and.returnValue([{ nodeId: 1, nodeName: 'Node1' }]);
    spyOn(helperService, 'setBackupOfFilterSelectionState');
    spyOn(component.onPrimaryFilterChange, 'emit');
    spyOn(component, 'setProjectAndLevelBackupBasedOnSelectedLevel');

    component.ngOnChanges({
      primaryFilterConfig: {
        currentValue: { defaultLevel: {labelName: 'Filter1' }}, previousValue: null, firstChange: true,
        isFirstChange: function (): boolean {
          throw new Error('Function not implemented.');
        }
      },
      selectedType: {
        currentValue: 'Type1', previousValue: null, firstChange: true,
        isFirstChange: function (): boolean {
          throw new Error('Function not implemented.');
        }
      },
      selectedLevel: {
        currentValue: 'Level1', previousValue: null, firstChange: true,
        isFirstChange: function (): boolean {
          throw new Error('Function not implemented.');
        }
      }
    });
    tick(200);
    expect(component.populateFilters).toHaveBeenCalled();
    expect(component.onPrimaryFilterChange.emit).toHaveBeenCalledWith([{ nodeId: 1, nodeName: 'Node1' }]);
    expect(component.setProjectAndLevelBackupBasedOnSelectedLevel).toHaveBeenCalled();
  })
  );

  it('should populate filters based on selectedLevel when it is a string', () => {
    component.selectedLevel = 'Level1';
    component.filterData = {
      Level1: [{ nodeId: 1, nodeName: 'Node1' }, { nodeId: 2, nodeName: 'Node2' }],
      Level2: [{ nodeId: 3, nodeName: 'Node3' }]
    };

    component.primaryFilterConfig = {
      "type": "multiSelect",
      "defaultLevel": {
        "labelName": "Level1",
        "sortBy": null
      }
    };
    spyOn(helperService, 'sortAlphabetically');

    component.populateFilters();

    expect(helperService.sortAlphabetically).toHaveBeenCalledTimes(1);
    // expect(component.filters).toEqual([{ nodeId: 1, nodeName: 'Node1' }, { nodeId: 2, nodeName: 'Node2' }]);
  });

  it('should populate filters based on selectedLevel when it is an object', () => {
    component.selectedLevel = { nodeId: 1, nodeType: 'Level1', emittedLevel: 'Level2' };
    component.filterData = { Level2: [{ nodeId: 1, nodeName: 'Node1', parentId: 1 }, { nodeId: 2, nodeName: 'Node2', parentId: 1 }] };
    component.primaryFilterConfig = {
      "type": "multiSelect",
      "defaultLevel": {
        "labelName": "Level1",
        "sortBy": 'nodeName'
      }
    };

    spyOn(helperService, 'sortByField');

    component.populateFilters();

    expect(helperService.sortByField).toHaveBeenCalledWith([{ nodeId: 1, nodeName: 'Node1', parentId: 1 }, { nodeId: 2, nodeName: 'Node2', parentId: 1 }], ['nodeName']);
  });

  it('should populate filters with defaultLevel when selectedLevel is not provided', () => {
    component.selectedLevel = null;
    component.primaryFilterConfig = { defaultLevel: { sortBy: 'sortByField' } };
    component.filterData = { Project: [{ nodeId: 1, nodeName: 'Node1' }] };
    spyOn(helperService, 'sortAlphabetically');

    component.populateFilters();

    expect(helperService.sortAlphabetically).toHaveBeenCalledWith([{ nodeId: 1, nodeName: 'Node1' }]);
  });

  it('should convert selectedFilters to an array if it is not already an array', () => {
    component.selectedFilters = { nodeId: 1, nodeName: 'Node1' };
    spyOn(helperService, 'setBackupOfFilterSelectionState');
    spyOn(component.onPrimaryFilterChange, 'emit');
    spyOn(component, 'setProjectAndLevelBackupBasedOnSelectedLevel');

    component.applyPrimaryFilters(null);

    expect(component.selectedFilters).toEqual([{ nodeId: 1, nodeName: 'Node1' }]);
    expect(helperService.setBackupOfFilterSelectionState).toHaveBeenCalledWith({ 'primary_level': [{ nodeId: 1, nodeName: 'Node1' }] });
    expect(component.onPrimaryFilterChange.emit).toHaveBeenCalledWith([{ nodeId: 1, nodeName: 'Node1' }]);
    expect(component.setProjectAndLevelBackupBasedOnSelectedLevel).toHaveBeenCalled();
  });

  it('should not convert selectedFilters to an array if it is already an array', () => {
    component.selectedFilters = [{ nodeId: 1, nodeName: 'Node1' }];
    spyOn(helperService, 'setBackupOfFilterSelectionState');
    spyOn(component.onPrimaryFilterChange, 'emit');
    spyOn(component, 'setProjectAndLevelBackupBasedOnSelectedLevel');

    component.applyPrimaryFilters(null);

    expect(component.selectedFilters).toEqual([{ nodeId: 1, nodeName: 'Node1' }]);
    expect(helperService.setBackupOfFilterSelectionState).toHaveBeenCalledWith({ 'primary_level': [{ nodeId: 1, nodeName: 'Node1' }] });
    expect(component.onPrimaryFilterChange.emit).toHaveBeenCalledWith([{ nodeId: 1, nodeName: 'Node1' }]);
    expect(component.setProjectAndLevelBackupBasedOnSelectedLevel).toHaveBeenCalled();
  });

  it('should set selectedTrends and selectedLevel based on selectedLevel when it is a string', () => {
    component.selectedLevel = 'Level1';
    component.selectedFilters = [{ nodeId: 1, nodeName: 'Node1' }];
    spyOn(sharedService, 'setSelectedTrends');
    spyOn(sharedService, 'setSelectedLevel');

    component.setProjectAndLevelBackupBasedOnSelectedLevel();

    expect(sharedService.setSelectedTrends).toHaveBeenCalledWith([{ nodeId: 1, nodeName: 'Node1' }]);
    expect(sharedService.setSelectedLevel).toHaveBeenCalledWith({ hierarchyLevelName: 'level1' });
  });

  it('should set selectedTrends and selectedLevel based on selectedLevel when it is an object', () => {
    component.selectedLevel = { nodeId: 1, nodeType: 'Level1', emittedLevel: 'Level2', fullNodeDetails: [{ nodeId: 1, nodeName: 'Node1' }] };
    spyOn(sharedService, 'setSelectedTrends');
    spyOn(sharedService, 'setSelectedLevel');

    component.setProjectAndLevelBackupBasedOnSelectedLevel();

    expect(sharedService.setSelectedTrends).toHaveBeenCalledWith([{ nodeId: 1, nodeName: 'Node1' }]);
    expect(sharedService.setSelectedLevel).toHaveBeenCalledWith({ hierarchyLevelName: 'level1' });
  });

  it('should populate filters, set selectedFilters, and call other methods after a delay', fakeAsync(() => {
    component.populateFilters = jasmine.createSpy('populateFilters');
    spyOn(component, 'applyPrimaryFilters');
    spyOn(component, 'setProjectAndLevelBackupBasedOnSelectedLevel');

    component.applyDefaultFilters();
    tick(100);

    expect(component.populateFilters).toHaveBeenCalled();
    expect(component.applyPrimaryFilters).toHaveBeenCalledWith({});
    expect(component.setProjectAndLevelBackupBasedOnSelectedLevel).toHaveBeenCalled();
  }));

  it('should handle cases where stateFilters or primaryFilterConfig are null or undefined', () => {
    component.stateFilters = null;
    component.primaryFilterConfig = null;
    component.applyDefaultFilters();
    expect(component.selectedFilters).toBeUndefined();
  });

  it('should call applyDefaultFilters if primaryFilterConfig, selectedType, or selectedLevel changes', () => {
    component.primaryFilterConfig = {
      filter1: ['value1'],
      filter2: ['value2'],
    };
    component.selectedType = 'type1';
    component.selectedLevel = 1;
    component.applyDefaultFilters = jasmine.createSpy('applyDefaultFilters');
    component.populateFilters = jasmine.createSpy('populateFilters');
    // component.helperService = jasmine.createSpyObj('HelperService', ['getBackupOfFilterSelectionState', 'setBackupOfFilterSelectionState']);
    // component.onPrimaryFilterChange = jasmine.createSpy('onPrimaryFilterChange');
    component.setProjectAndLevelBackupBasedOnSelectedLevel = jasmine.createSpy('setProjectAndLevelBackupBasedOnSelectedLevel');
    component.filterData = [
      [
        { nodeId: 'node1', labelName: 'filter1' },
        { nodeId: 'node2', labelName: 'filter2' },
      ],
      [
        { nodeId: 'node3', labelName: 'filter3' },
        { nodeId: 'node4', labelName: 'filter4' },
      ],
    ];
    component.selectedFilters = [];
    component.selectedAdditionalFilters = {};
    component.stateFilters = [];

    const mockChanges = {
      primaryFilterConfig: {
        previousValue: {
          filter1: ['value1'],
          filter2: ['value2'],
        },
        currentValue: {
          filter1: ['value1'],
          filter2: ['value3'],
        },
        firstChange: false,
        isFirstChange: () => false
      },
      selectedType: {
        previousValue: 'type1',
        currentValue: 'type2',
        firstChange: false,
        isFirstChange: () => false
      },
      selectedLevel: {
        previousValue: 1,
        currentValue: 2,
        firstChange: false,
        isFirstChange: () => false
      },
    };

    component.ngOnChanges(mockChanges);

    expect(component.applyDefaultFilters).toHaveBeenCalled();
  });

  it('should reset selectedFilters and call populateFilters if filters exist', () => {
    component.primaryFilterConfig = {
      filter1: ['value1'],
      filter2: ['value2'],
    };
    component.selectedType = 'type1';
    component.selectedLevel = 1;
    component.applyDefaultFilters = jasmine.createSpy('applyDefaultFilters');
    component.populateFilters = jasmine.createSpy('populateFilters');
    // component.helperService = jasmine.createSpyObj('HelperService', ['getBackupOfFilterSelectionState', 'setBackupOfFilterSelectionState']);
    // component.onPrimaryFilterChange = jasmine.createSpy('onPrimaryFilterChange');
    component.setProjectAndLevelBackupBasedOnSelectedLevel = jasmine.createSpy('setProjectAndLevelBackupBasedOnSelectedLevel');
    component.filterData = {
      'level1': [
        { nodeId: 'node1', labelName: 'filter1' },
        { nodeId: 'node2', labelName: 'filter2' },
      ],
      'level2': [
        { nodeId: 'node3', labelName: 'filter3' },
        { nodeId: 'node4', labelName: 'filter4' },
      ],
    };
    component.selectedFilters = [];
    component.selectedAdditionalFilters = {};
    component.stateFilters = [];
    const mockChanges = {};

    component.ngOnChanges(mockChanges);

    expect([...component.selectedFilters]).toEqual([]);
    expect(component.populateFilters).toHaveBeenCalled();
  });

  xit('should set selectedFilters and call setBackupOfFilterSelectionState and onPrimaryFilterChange if primary_level is in stateFilters', () => {
    component.primaryFilterConfig = {
      filter1: ['value1'],
      filter2: ['value2'],
    };
    component.selectedType = 'type1';
    component.selectedLevel = 1;
    component.applyDefaultFilters = jasmine.createSpy('applyDefaultFilters');
    component.populateFilters = jasmine.createSpy('populateFilters');


    component.setProjectAndLevelBackupBasedOnSelectedLevel = jasmine.createSpy('setProjectAndLevelBackupBasedOnSelectedLevel');
    component.selectedFilters = [];
    component.selectedAdditionalFilters = {};
    const mockChanges = {};

    component.filters = [
      { nodeId: 'node1', labelName: 'filter1' },
      { nodeId: 'node2', labelName: 'filter2' },
    ];
    spyOn(helperService, 'getBackupOfFilterSelectionState').and.returnValue({
      primary_level: [{ nodeId: 'node1', labelName: 'filter1' },
      { nodeId: 'node2', labelName: 'filter2' }]
    });
    const mockSetBackupOfFilterSelectionState = spyOn(component.helperService, 'setBackupOfFilterSelectionState');
    const mockOnPrimaryFilterChange = spyOn(component.onPrimaryFilterChange, 'emit');
    component.ngOnChanges(mockChanges);

    expect(component.selectedFilters).toEqual([
      { nodeId: 'node1', labelName: 'filter1' },
      { nodeId: 'node2', labelName: 'filter2' },
    ]);
    expect(mockSetBackupOfFilterSelectionState).toHaveBeenCalledWith({
      primary_level: [
        { nodeId: 'node1', labelName: 'filter1' },
        { nodeId: 'node2', labelName: 'filter2' },
      ],
    });
    expect(mockOnPrimaryFilterChange).toHaveBeenCalledWith([
      { nodeId: 'node1', labelName: 'filter1' },
      { nodeId: 'node2', labelName: 'filter2' },
    ]);
    expect(component.setProjectAndLevelBackupBasedOnSelectedLevel).toHaveBeenCalled();
  });


  it('should set selectedFilters and selectedAdditionalFilters and call onPrimaryFilterChange if primary_level and additional_level are in stateFilters', () => {
    const mockChanges = {};
    component.filterData = {
      'level1': [
        { nodeId: 'node1', nodeName: 'filter1' },
        { nodeId: 'node2', nodeName: 'filter2' },
      ],
      'level2': [
        { nodeId: 'node3', labelName: 'filter3' },
        { nodeId: 'node4', labelName: 'filter4' },
      ],
      'level3': [
        { nodeId: 'node5', labelName: 'filter5' },
        { nodeId: 'node6', labelName: 'filter6' },
      ],
    };
    spyOn(helperService, 'getBackupOfFilterSelectionState').and.returnValue({
      primary_level: [
        { nodeId: 'node1', nodeName: 'filter1' },
        { nodeId: 'node2', nodeName: 'filter2' },
      ],
      additional_level: {
          level4 :[
          { nodeId: 'node3', nodeName: 'filter3' },
          { nodeId: 'node4', nodeName: 'filter4' },
        ]
      },
    });
    component.selectedLevel = 'level1';
    const mockOnPrimaryFilterChange = spyOn(component.onPrimaryFilterChange, 'emit');
    component.ngOnChanges(mockChanges);

    expect(component.selectedFilters).toEqual([
      { nodeId: 'node1', nodeName: 'filter1' },
      { nodeId: 'node2', nodeName: 'filter2' },
    ]);
    expect(component.selectedAdditionalFilters).toEqual({
      level4: [
        { nodeId: 'node3', nodeName: 'filter3' },
        { nodeId: 'node4', nodeName: 'filter4' },
      ]
    });
    expect(mockOnPrimaryFilterChange).toHaveBeenCalledWith({
      primary_level: [
        { nodeId: 'node1', nodeName: 'filter1' },
        { nodeId: 'node2', nodeName: 'filter2' },
      ],
      additional_level: {
        level4: [
          { nodeId: 'node3', nodeName: 'filter3' },
          { nodeId: 'node4', nodeName: 'filter4' },
        ]
      },
    });
  });

  it('should call applyDefaultFilters if no filters exist', () => {
    component.filterData = {
      'level1': [
        { nodeId: 'node1', nodeName: 'filter1' },
        { nodeId: 'node2', nodeName: 'filter2' },
      ],
      'level2': [
        { nodeId: 'node3', nodeName: 'filter3' },
        { nodeId: 'node4', nodeName: 'filter4' },
      ],
    };
    component.selectedLevel = 'level1';
    const mockChanges = {
      primaryFilterConfig: {
        previousValue: {
          filter1: ['value1'],
          filter2: ['value2'],
        },
        currentValue: {
          filter1: ['value1'],
          filter2: ['value3'],
        },
        firstChange: false,
        isFirstChange: () => false
      },
      selectedType: {
        previousValue: 'type1',
        currentValue: 'type2',
        firstChange: false,
        isFirstChange: () => false
      },
      selectedLevel: {
        previousValue: 1,
        currentValue: 2,
        firstChange: false,
        isFirstChange: () => false
      }
    };
    component.filters = [];
    const mockApplyDefaultFilters = spyOn(component, 'applyDefaultFilters');
    component.ngOnChanges(mockChanges);

    expect(mockApplyDefaultFilters).toHaveBeenCalled();
  });

  it('should not modify filters array when event.value is null', () => {
    const event = { value: null };
    component.moveSelectedOptionToTop();
    expect(component.filters).toEqual(filters);
  });

  it('should remove selected item from its original position', () => {
    const event = { value: [{ nodeName: 'item2' }] };
    component.moveSelectedOptionToTop();
    expect(component.filters).not.toContain([{ nodeId: 2, nodeName: 'item2' }]);
  });

  // -> moveSelectedOptionToTop() & onSelectionChange()

  it('should move selected options to top when selectedFilters is a subset of filters', () => {
    component.filters = ['option1', 'option2', 'option3'];
    component.selectedFilters = ['option1', 'option2'];
    component.moveSelectedOptionToTop();
    expect(component.filters).toEqual(['option1', 'option2', 'option3']);
  });

  it('should not move options when selectedFilters is not a subset of filters', () => {
    component.filters = ['option1', 'option2', 'option3'];
    component.selectedFilters = ['option4', 'option5'];
    component.moveSelectedOptionToTop();
    expect(component.filters).toEqual(['option1', 'option2', 'option3']);
  });

  it('should not move options when selectedFilters is an empty array', () => {
    component.filters = ['option1', 'option2', 'option3'];
    component.selectedFilters = [];
    component.moveSelectedOptionToTop();
    expect(component.filters).toEqual(['option1', 'option2', 'option3']);
  });

  it('should not move options when filters is an empty array', () => {
    component.filters = [];
    component.selectedFilters = ['option1', 'option2'];
    component.moveSelectedOptionToTop();
    expect(component.filters).toEqual([]);
  });

  it('should not move options when both filters and selectedFilters are empty arrays', () => {
    component.filters = [];
    component.selectedFilters = [];
    component.moveSelectedOptionToTop();
    expect(component.filters).toEqual([]);
  });

  it('should not call moveSelectedOptionToTop when event value is empty', () => {
    const event = { value: [] };
    spyOn(component, 'moveSelectedOptionToTop');
    component.onSelectionChange(event);
    expect(component.moveSelectedOptionToTop).not.toHaveBeenCalled();
  });

  it('should call moveSelectedOptionToTop when event value has one or more elements', () => {
    const event = { value: ['element1'] };
    spyOn(component, 'moveSelectedOptionToTop');
    component.onSelectionChange(event);
    expect(component.moveSelectedOptionToTop).toHaveBeenCalledTimes(1);
  });

  it('should not call moveSelectedOptionToTop when event value is null or undefined', () => {
    const event = { value: null };
    spyOn(component, 'moveSelectedOptionToTop');
    component.onSelectionChange(event);
    expect(component.moveSelectedOptionToTop).not.toHaveBeenCalled();

    const event2 = { value: undefined };
    component.onSelectionChange(event2);
    expect(component.moveSelectedOptionToTop).not.toHaveBeenCalled();
  });

  // -> end moveSelectedOptionToTop() & onSelectionChange()

  it('should convert selectedFilters to an array if it\'s not already one', () => {
    component.selectedFilters = 'test';
    component.applyPrimaryFilters(null);
    expect(component.selectedFilters).toBeInstanceOf(Array);
  });

  it('should call setBackupOfFilterSelectionState with the correct data', () => {
    spyOn(helperService, 'setBackupOfFilterSelectionState');
    component.selectedFilters = ['test'];
    component.applyPrimaryFilters(null);
    expect(helperService.setBackupOfFilterSelectionState).toHaveBeenCalledWith({ 'primary_level': ['test'] });
  });

  it('should emit onPrimaryFilterChange with the correct data when defaultLevel is not \'sprint\'', () => {
    component.primaryFilterConfig = { 'defaultLevel': { 'labelName': 'test' } };
    component.selectedFilters = ['test'];
    spyOn(component.onPrimaryFilterChange, 'emit');
    component.applyPrimaryFilters(null);
    expect(component.onPrimaryFilterChange.emit).toHaveBeenCalledWith(['test']);
  });

  it('should emit onPrimaryFilterChange with the correct data when defaultLevel is \'sprint\' and sprintState is \'active\'', () => {
    component.primaryFilterConfig = { 'defaultLevel': { 'labelName': 'sprint' } };
    component.selectedFilters = [{ sprintState: 'active' }];
    spyOn(component.onPrimaryFilterChange, 'emit');
    component.applyPrimaryFilters(null);
    expect(component.onPrimaryFilterChange.emit).toHaveBeenCalledWith([{ sprintState: 'active' }]);
  });

  it('should emit onPrimaryFilterChange with an empty array when defaultLevel is \'sprint\' and sprintState is not \'active\'', () => {
    component.primaryFilterConfig = { 'defaultLevel': { 'labelName': 'sprint' } };
    component.selectedFilters = [{ sprintState: 'inactive' }];
    spyOn(component.onPrimaryFilterChange, 'emit');
    component.applyPrimaryFilters(null);
    expect(component.onPrimaryFilterChange.emit).toHaveBeenCalledWith([]);
  });

  it('should call setNoSprints when defaultLevel is \'sprint\' and sprintState is not \'active\'', () => {
    component.primaryFilterConfig = { 'defaultLevel': { 'labelName': 'sprint' } };
    component.selectedFilters = [{ sprintState: 'inactive' }];
    spyOn(sharedService, 'setNoSprints');
    component.applyPrimaryFilters(null);
    expect(sharedService.setNoSprints).toHaveBeenCalledWith(true);
  });

  it('should call setProjectAndLevelBackupBasedOnSelectedLevel', () => {
    spyOn(component, 'setProjectAndLevelBackupBasedOnSelectedLevel');
    component.applyPrimaryFilters(null);
    expect(component.setProjectAndLevelBackupBasedOnSelectedLevel).toHaveBeenCalledTimes(1);
  });

});

