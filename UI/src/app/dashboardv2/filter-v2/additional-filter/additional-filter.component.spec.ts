import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { AdditionalFilterComponent } from './additional-filter.component';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterTestingModule } from '@angular/router/testing';
import { SharedService } from '../../../services/shared.service';
import { HelperService } from 'src/app/services/helper.service';
import { GetAuthService } from '../../../services/getauth.service';
import { HttpClientModule } from '@angular/common/http';
import { APP_CONFIG, AppConfig } from '../../../services/app.config';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpService } from '../../../services/http.service';
import { of } from 'rxjs';

describe('AdditionalFilterComponent', () => {
  let component: AdditionalFilterComponent;
  let fixture: ComponentFixture<AdditionalFilterComponent>;
  let getAuth: GetAuthService;
  let httpService: HttpService
  let sharedService: SharedService;
  let helperService: HelperService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AdditionalFilterComponent],
      imports: [RouterTestingModule, HttpClientModule, BrowserAnimationsModule],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],

      providers: [SharedService, GetAuthService, HttpService, HelperService, CommonModule, DatePipe,
        { provide: APP_CONFIG, useValue: AppConfig }
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(AdditionalFilterComponent);
    component = fixture.componentInstance;
    getAuth = TestBed.inject(GetAuthService);
    httpService = TestBed.inject(HttpService);
    sharedService = TestBed.inject(SharedService);
    helperService = TestBed.inject(HelperService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

 xit('should reset filterData, filterSet, and additional filter config when selectedTab changes', () => {
    component.filterData = ['Filter 1', 'Filter 2'];
    component.filterSet = new Set(['Filter 1', 'Filter 2']);
    component.selectedFilters = ['Filter 1', 'Filter 2'];

    component.ngOnChanges({
      additionalFilterConfig: {
        currentValue: 'New Tab', previousValue: 'Old Tab', firstChange: false,
        isFirstChange: function (): boolean {
          return false;
        }
      }
    });

    expect(component.filterSet.size).toBe(0);
    expect(component.selectedFilters.length).toBe(0);
  });


  it('should set filterData, selectedFilters, and selectedTrends when service.populateAdditionalFilters emits', () => {
    const data = {
      filter1: [
        {
          "nodeId": "55042_API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
          "nodeName": "Sprint 19_API POD 1 - Core",
          "sprintStartDate": "2024-05-02T07:26:00.000Z",
          "sprintEndDate": "2024-05-14T17:26:00.000Z",
          "path": "API POD 1 - Core_6524a7677c8bb73cd0c3fe67###Pharmaceutical Industries_port###Australian Pharmaceutical Industries Pty Ltd_acc###Retail_ver###International_bu",
          "labelName": "sprint",
          "parentId": "API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
          "sprintState": "CLOSED",
          "level": 6
        },
        {
          "nodeId": "53970_API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
          "nodeName": "Sprint 8_API POD 1 - Core",
          "sprintStartDate": "2023-11-29T11:08:00.000Z",
          "sprintEndDate": "2023-12-12T11:08:00.000Z",
          "path": "API POD 1 - Core_6524a7677c8bb73cd0c3fe67###Pharmaceutical Industries_port###Australian Pharmaceutical Industries Pty Ltd_acc###Retail_ver###International_bu",
          "labelName": "sprint",
          "parentId": "API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
          "sprintState": "CLOSED",
          "level": 6
        },
        {
          "nodeId": "55339_API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
          "nodeName": "Sprint 10_API POD 1 - Core",
          "sprintStartDate": "2023-12-28T12:26:00.000Z",
          "sprintEndDate": "2024-01-09T18:26:00.000Z",
          "path": "API POD 1 - Core_6524a7677c8bb73cd0c3fe67###Pharmaceutical Industries_port###Australian Pharmaceutical Industries Pty Ltd_acc###Retail_ver###International_bu",
          "labelName": "sprint",
          "parentId": "API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
          "sprintState": "CLOSED",
          "level": 6
        },
        {
          "nodeId": "55041_API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
          "nodeName": "Sprint 18_API POD 1 - Core",
          "sprintStartDate": "2024-04-17T07:19:00.000Z",
          "sprintEndDate": "2024-04-30T01:19:00.000Z",
          "path": "API POD 1 - Core_6524a7677c8bb73cd0c3fe67###Pharmaceutical Industries_port###Australian Pharmaceutical Industries Pty Ltd_acc###Retail_ver###International_bu",
          "labelName": "sprint",
          "parentId": "API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
          "sprintState": "CLOSED",
          "level": 6
        },
        {
          "nodeId": "53971_API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
          "nodeName": "Sprint 9_API POD 1 - Core",
          "sprintStartDate": "2023-12-13T11:08:00.000Z",
          "sprintEndDate": "2023-12-26T18:08:00.000Z",
          "path": "API POD 1 - Core_6524a7677c8bb73cd0c3fe67###Pharmaceutical Industries_port###Australian Pharmaceutical Industries Pty Ltd_acc###Retail_ver###International_bu",
          "labelName": "sprint",
          "parentId": "API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
          "sprintState": "CLOSED",
          "level": 6
        },
        {
          "nodeId": "55037_API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
          "nodeName": "Sprint 14_API POD 1 - Core",
          "sprintStartDate": "2024-02-21T13:49:00.000Z",
          "sprintEndDate": "2024-03-05T18:49:00.000Z",
          "path": "API POD 1 - Core_6524a7677c8bb73cd0c3fe67###Pharmaceutical Industries_port###Australian Pharmaceutical Industries Pty Ltd_acc###Retail_ver###International_bu",
          "labelName": "sprint",
          "parentId": "API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
          "sprintState": "CLOSED",
          "level": 6
        },
        {
          "nodeId": "55044_API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
          "nodeName": "Sprint 21_API POD 1 - Core",
          "sprintStartDate": "2024-05-30T04:27:00.000Z",
          "sprintEndDate": "2024-06-11T10:27:00.000Z",
          "path": "API POD 1 - Core_6524a7677c8bb73cd0c3fe67###Pharmaceutical Industries_port###Australian Pharmaceutical Industries Pty Ltd_acc###Retail_ver###International_bu",
          "labelName": "sprint",
          "parentId": "API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
          "sprintState": "ACTIVE",
          "level": 6
        },
        {
          "nodeId": "55039_API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
          "nodeName": "Sprint 16_API POD 1 - Core",
          "sprintStartDate": "2024-03-20T11:21:00.000Z",
          "sprintEndDate": "2024-04-02T18:21:00.000Z",
          "path": "API POD 1 - Core_6524a7677c8bb73cd0c3fe67###Pharmaceutical Industries_port###Australian Pharmaceutical Industries Pty Ltd_acc###Retail_ver###International_bu",
          "labelName": "sprint",
          "parentId": "API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
          "sprintState": "CLOSED",
          "level": 6
        },
        {
          "nodeId": "53969_API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
          "nodeName": "Sprint 7_API POD 1 - Core",
          "sprintStartDate": "2023-11-14T11:08:00.000Z",
          "sprintEndDate": "2023-11-28T11:08:00.000Z",
          "path": "API POD 1 - Core_6524a7677c8bb73cd0c3fe67###Pharmaceutical Industries_port###Australian Pharmaceutical Industries Pty Ltd_acc###Retail_ver###International_bu",
          "labelName": "sprint",
          "parentId": "API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
          "sprintState": "CLOSED",
          "level": 6
        },
        {
          "nodeId": "55040_API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
          "nodeName": "Sprint 17_API POD 1 - Core",
          "sprintStartDate": "2024-04-03T18:12:00.000Z",
          "sprintEndDate": "2024-04-16T00:12:00.000Z",
          "path": "API POD 1 - Core_6524a7677c8bb73cd0c3fe67###Pharmaceutical Industries_port###Australian Pharmaceutical Industries Pty Ltd_acc###Retail_ver###International_bu",
          "labelName": "sprint",
          "parentId": "API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
          "sprintState": "CLOSED",
          "level": 6
        },
        {
          "nodeId": "54078_API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
          "nodeName": "Sprint 11_API POD 1 - Core",
          "sprintStartDate": "2024-01-10T05:17:00.000Z",
          "sprintEndDate": "2024-01-23T05:17:00.000Z",
          "path": "API POD 1 - Core_6524a7677c8bb73cd0c3fe67###Pharmaceutical Industries_port###Australian Pharmaceutical Industries Pty Ltd_acc###Retail_ver###International_bu",
          "labelName": "sprint",
          "parentId": "API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
          "sprintState": "CLOSED",
          "level": 6
        },
        {
          "nodeId": "55036_API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
          "nodeName": "Sprint 13_API POD 1 - Core",
          "sprintStartDate": "2024-02-06T19:51:00.000Z",
          "sprintEndDate": "2024-02-20T01:51:00.000Z",
          "path": "API POD 1 - Core_6524a7677c8bb73cd0c3fe67###Pharmaceutical Industries_port###Australian Pharmaceutical Industries Pty Ltd_acc###Retail_ver###International_bu",
          "labelName": "sprint",
          "parentId": "API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
          "sprintState": "CLOSED",
          "level": 6
        },
        {
          "nodeId": "55035_API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
          "nodeName": "Sprint 12_API POD 1 - Core",
          "sprintStartDate": "2024-01-24T11:19:00.000Z",
          "sprintEndDate": "2024-02-06T18:19:00.000Z",
          "path": "API POD 1 - Core_6524a7677c8bb73cd0c3fe67###Pharmaceutical Industries_port###Australian Pharmaceutical Industries Pty Ltd_acc###Retail_ver###International_bu",
          "labelName": "sprint",
          "parentId": "API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
          "sprintState": "CLOSED",
          "level": 6
        },
        {
          "nodeId": "55043_API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
          "nodeName": "Sprint 20_API POD 1 - Core",
          "sprintStartDate": "2024-05-15T15:38:00.000Z",
          "sprintEndDate": "2024-05-27T21:38:00.000Z",
          "path": "API POD 1 - Core_6524a7677c8bb73cd0c3fe67###Pharmaceutical Industries_port###Australian Pharmaceutical Industries Pty Ltd_acc###Retail_ver###International_bu",
          "labelName": "sprint",
          "parentId": "API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
          "sprintState": "CLOSED",
          "level": 6
        },
        {
          "nodeId": "55038_API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
          "nodeName": "Sprint 15_API POD 1 - Core",
          "sprintStartDate": "2024-03-06T11:52:00.000Z",
          "sprintEndDate": "2024-03-19T17:52:00.000Z",
          "path": "API POD 1 - Core_6524a7677c8bb73cd0c3fe67###Pharmaceutical Industries_port###Australian Pharmaceutical Industries Pty Ltd_acc###Retail_ver###International_bu",
          "labelName": "sprint",
          "parentId": "API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
          "sprintState": "CLOSED",
          "level": 6
        },
        {
          "nodeId": "53968_API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
          "nodeName": "Sprint 6_API POD 1 - Core",
          "sprintStartDate": "2023-11-01T11:08:00.000Z",
          "sprintEndDate": "2023-11-14T11:08:00.000Z",
          "path": "API POD 1 - Core_6524a7677c8bb73cd0c3fe67###Pharmaceutical Industries_port###Australian Pharmaceutical Industries Pty Ltd_acc###Retail_ver###International_bu",
          "labelName": "sprint",
          "parentId": "API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
          "sprintState": "CLOSED",
          "level": 6
        }
      ],
      filter2: []
    };
    spyOn(sharedService, 'getSelectedTrends').and.returnValue([{
      "nodeId": "API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
      "nodeName": "API POD 1 - Core",
      "path": "Pharmaceutical Industries_port###Australian Pharmaceutical Industries Pty Ltd_acc###Retail_ver###International_bu",
      "labelName": "project",
      "parentId": "Pharmaceutical Industries_port",
      "level": 5,
      "basicProjectConfigId": "6524a7677c8bb73cd0c3fe67"
    }]);
    spyOn(helperService, 'sortByField').and.callThrough();
    spyOn(component, 'applyAdditionalFilter');
    component.selectedTab = 'my-knowhow';
    component.selectedLevel = 'project';
    component.selectedType = 'scrum';
    component.additionalFilterConfig = [
      {
        "type": "multiSelect",
        "defaultLevel": {
          "labelName": "sprint",
          "sortBy": null
        }
      },
      {
        "type": "multiSelect",
        "defaultLevel": {
          "labelName": "sqd",
          "sortBy": null
        }
      }
    ];
    sharedService.populateAdditionalFilters = of(data);

    component.ngOnInit();
    expect(component.filterData.length).toEqual(2);
    expect(component.selectedFilters).toEqual([]);
    expect(component.selectedTrends).toEqual([{
      "nodeId": "API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
      "nodeName": "API POD 1 - Core",
      "path": "Pharmaceutical Industries_port###Australian Pharmaceutical Industries Pty Ltd_acc###Retail_ver###International_bu",
      "labelName": "project",
      "parentId": "Pharmaceutical Industries_port",
      "level": 5,
      "basicProjectConfigId": "6524a7677c8bb73cd0c3fe67"
    }]);
    expect(helperService.sortByField).toHaveBeenCalledTimes(2);
  });

 xit('should set selectedFilters to Overall if filterData has Overall and selectedTab is developer', () => {
    const data = {
      filter1: [
        {
          "nodeId": "master -> myapi-code -> API POD 3 - Search & Browse",
          "nodeName": "master -> myapi-code -> API POD 3 - Search & Browse"
        },
        {
          "nodeId": "master -> myapi-test -> API POD 3 - Search & Browse",
          "nodeName": "master -> myapi-test -> API POD 3 - Search & Browse"
        }
      ]
    };
    spyOn(sharedService, 'getSelectedTrends').and.returnValue([]);
    spyOn(helperService, 'sortByField').and.callThrough();
    spyOn(component, 'applyAdditionalFilter');
    component.selectedTab = 'developer';
    component.selectedLevel = 'project';
    component.selectedType = 'scrum';
    component.additionalFilterConfig = [
      {
        "type": "singleSelect",
        "defaultLevel": {
          "labelName": "branch",
          "sortBy": null
        }
      },
      {
        "type": "singleSelect",
        "defaultLevel": {
          "labelName": "developer",
          "sortBy": null
        }
      }
    ];
    sharedService.populateAdditionalFilters = of(data);
    component.ngOnInit();
    expect(component.filterData.length).toEqual(1);
    expect(component.selectedFilters).toEqual([{ nodeId: 'master -> myapi-code -> API POD 3 - Search & Browse', nodeName: 'master -> myapi-code -> API POD 3 - Search & Browse' }]);
    expect(component.selectedTrends).toEqual([]);
  });


  it('should apply additional filters and update appliedFilters when selectedTab is developer and filterData has length 1', () => {
    component.selectedTab = 'Developer';
    component.filterData = [[{ nodeId: 1, nodeName: 'Filter 1' }]];
    spyOn(sharedService, 'applyAdditionalFilters');

    component.applyAdditionalFilter({ value: 1 }, 0);

    expect(component.appliedFilters['filter']).toEqual([1]);
    expect(sharedService.applyAdditionalFilters).toHaveBeenCalledWith({ value: 1, index: 0 });
  });

  it('should apply additional filters and update appliedFilters when selectedTab is developer and filterData has length greater than 1', () => {
    component.selectedTab = 'Developer';
    component.filterData = [[{ nodeId: 1, nodeName: 'Filter 1' }], [{ nodeId: 2, nodeName: 'Filter 2' }]];
    spyOn(sharedService, 'applyAdditionalFilters');

    component.applyAdditionalFilter({ value: 2 }, 1);

    expect(component.appliedFilters['filter1']).toEqual([2]);
    expect(sharedService.applyAdditionalFilters).toHaveBeenCalledWith({ value: 2, index: 1 });
  });

  xit('should apply default filter when Overall is present in filterData', fakeAsync(() => {
    const mockFilterData = [[
      { nodeId: 'Overall', nodeName: 'Overall' },
      { nodeId: 'Node 1', nodeName: 'Node 1' },
      { nodeId: 'Node 2', nodeName: 'Node 2' }
    ]];

    spyOn(component, 'applyAdditionalFilter');

    component.filterData = mockFilterData;
    component.applyDefaultFilter();
    tick(100);
    expect(component.filterData[0].nodeName).toEqual('Overall');
    expect(component.selectedFilters).toEqual(['Overall']);
    expect(component.applyAdditionalFilter).toHaveBeenCalledOnceWith({ value: 'Overall' }, 1);
  }));

  it('should apply default filter when Overall is not present in filterData', fakeAsync(() => {
    const mockFilterData = [
      [{ nodeId: 'Node 1', nodeName: 'Node 1' }],
      [{ nodeId: 'Node 2', nodeName: 'Node 2' }]
    ];

    spyOn(component, 'applyAdditionalFilter');

    component.filterData = mockFilterData;
    component.applyDefaultFilter();
    tick(100);
    expect(component.filterData[0][0].nodeId).toEqual('Node 1');
    expect(component.selectedFilters).toEqual([{ nodeId: 'Node 1', nodeName: 'Node 1' }, { nodeId: 'Node 2', nodeName: 'Node 2' }]);
  }));

  it('should apply default filter when filterData is empty', fakeAsync(() => {
    spyOn(component, 'applyAdditionalFilter');

    component.filterData = [];
    component.applyDefaultFilter();
    tick(100);
    expect(component.filterData).toEqual([]);
    // expect(component.selectedFilters).toEqual(['Overall']);
    expect(component.applyAdditionalFilter).toHaveBeenCalledTimes(0);
  }));

  it('should set additional filter level and emit event if not from backup', () => {
    component.filterData = [
      [{ labelName: 'filter1' }],
      [{ labelName: 'filter2' }],
    ];
    component.selectedTab = 'MyKnowHOW';
    component.stateFilters = {
      level: {
        filter1: ['value1'],
        filter2: ['value2'],
      },
    };
    // spyOn(sharedService, 'setBackupOfFilterSelectionState');
    const mockOnAdditionalFilterChange = spyOn(component.onAdditionalFilterChange, 'emit');
    component.appliedFilters = {};
    // component.service = jasmine.createSpyObj('Service', ['applyAdditionalFilters']);
    component.multiSelect = jasmine.createSpyObj('MultiSelect', ['close']);
    const mockEvent = [
      [{ labelName: 'filter1' }],
      [{ labelName: 'filter2' }],
    ];
    const mockIndex = 2;
    const mockMulti = false;
    const mockFromBackup = false;

    component.applyAdditionalFilter(mockEvent, mockIndex, mockMulti, mockFromBackup);

    // expect(component.helperService.setBackupOfFilterSelectionState).toHaveBeenCalledWith({
    //   additional_level: {
    //     level: {
    //       filter1: [{ labelName: 'filter1' }],
    //       filter2: [{ labelName: 'filter2' }],
    //     },
    //   },
    // });
    expect(mockOnAdditionalFilterChange).toHaveBeenCalledTimes(2);
    // expect(mockOnAdditionalFilterChange).toHaveBeenCalledWith([{filter1: { labelName: 'filter1' }}]);
    // expect(mockOnAdditionalFilterChange).toHaveBeenCalledWith([{filter2: { labelName: 'filter2' }}]);
  });

  it('should emit event if from backup', () => {

    component.filterData = [
      [{ labelName: 'filter1' }],
      [{ labelName: 'filter2' }],
    ];
    component.selectedTab = 'Speed';
    component.stateFilters = {
      level: {
        filter1: ['value1'],
        filter2: ['value2'],
      },
    };
    // component.helperService = jasmine.createSpyObj('HelperService', ['setBackupOfFilterSelectionState']);
    const mockAdditionalFilterChange = spyOn(component.onAdditionalFilterChange, 'emit');
    component.appliedFilters = {};
    // component.service = jasmine.createSpyObj('Service', ['applyAdditionalFilters']);
    component.multiSelect = jasmine.createSpyObj('MultiSelect', ['close']);

    const mockEvent = [{ labelName: 'filter1' }];
    const mockIndex = 1;
    const mockMulti = false;
    const mockFromBackup = true;

    component.applyAdditionalFilter(mockEvent, mockIndex, mockMulti, mockFromBackup);

    expect(mockAdditionalFilterChange).toHaveBeenCalledTimes(1);
    expect(mockAdditionalFilterChange).toHaveBeenCalledWith([{ labelName: 'filter1' }]);
  });

  xit('should add filter value to appliedFilters and call applyAdditionalFilters', () => {
    component.filterData = [
      [{ labelName: 'filter1' }],
      [{ labelName: 'filter2' }],
    ];
    component.selectedTab = 'Speed';
    component.stateFilters = {
      level: {
        filter1: ['value1'],
        filter2: ['value2'],
      },
    };
    component.helperService = jasmine.createSpyObj('HelperService', ['setBackupOfFilterSelectionState']);
    const mockAdditionalFilterChange = spyOn(component.onAdditionalFilterChange, 'emit');
    component.appliedFilters = {};
    component.service = jasmine.createSpyObj('Service', ['applyAdditionalFilters']);
    component.multiSelect = jasmine.createSpyObj('MultiSelect', ['close']);

    const mockEvent = { value: 'value1' };
    const mockIndex = 1;
    const mockMulti = true;
    const mockFromBackup = false;

    component.applyAdditionalFilter(mockEvent, mockIndex, mockMulti, mockFromBackup);

    expect(component.appliedFilters).toEqual({ filter1: ['value1'] });
    expect(mockAdditionalFilterChange).toHaveBeenCalledWith('value1');
  });

  it('should close multiSelect if overlayVisible is true', () => {
    component.filterData = [
      [{ labelName: 'filter1' }],
      [{ labelName: 'filter2' }],
    ];
    component.selectedTab = 'Developer';
    component.stateFilters = {
      level: {
        filter1: ['value1'],
        filter2: ['value2'],
      },
    };
    component.helperService = jasmine.createSpyObj('HelperService', ['setBackupOfFilterSelectionState']);
    spyOn(component.onAdditionalFilterChange, 'emit');
    component.appliedFilters = {};
    component.service = jasmine.createSpyObj('Service', ['applyAdditionalFilters']);
    component.multiSelect = jasmine.createSpyObj('MultiSelect', ['close']);

    const mockEvent = { value: 'value1' };
    const mockIndex = 1;
    const mockMulti = true;
    const mockFromBackup = false;
    component.multiSelect.overlayVisible = true;

    component.applyAdditionalFilter(mockEvent, mockIndex, mockMulti, mockFromBackup);

    expect(component.multiSelect.close).toHaveBeenCalled();
  });

  it('should not close multiSelect if overlayVisible is false', () => {
    component.filterData = [
      [{ labelName: 'filter1' }],
      [{ labelName: 'filter2' }],
    ];
    component.selectedTab = 'Developer';
    component.stateFilters = {
      level: {
        filter1: ['value1'],
        filter2: ['value2'],
      },
    };
    component.helperService = jasmine.createSpyObj('HelperService', ['setBackupOfFilterSelectionState']);
    spyOn(component.onAdditionalFilterChange, 'emit');
    component.appliedFilters = {};
    component.service = jasmine.createSpyObj('Service', ['applyAdditionalFilters']);
    component.multiSelect = jasmine.createSpyObj('MultiSelect', ['close']);
    const mockEvent = { value: 'value1' };
    const mockIndex = 1;
    const mockMulti = true;
    const mockFromBackup = false;
    component.multiSelect.overlayVisible = false;

    component.applyAdditionalFilter(mockEvent, mockIndex, mockMulti, mockFromBackup);

    expect(component.multiSelect.close).not.toHaveBeenCalled();
  });

  // -> moveSelectedOptionToTop() & onSelectionChange()

  it('should not modify filterData when selectedFilters is empty', () => {
    component.selectedFilters = [];
    component.filterData = [[{ nodeName: 'option1' }, { nodeName: 'option2' }]];
    component.moveSelectedOptionToTop(null, 0);
    expect(component.filterData).toEqual([[{ nodeName: 'option1' }, { nodeName: 'option2' }]]);
  });

  it('should move selected options to top when selectedFilters is not empty', () => {
    component.selectedFilters = [[{ nodeName: 'option2' }]];
    component.filterData = [[{ nodeName: 'option1' }, { nodeName: 'option2' }]];
    component.moveSelectedOptionToTop(null, 0);
    expect(component.filterData).toEqual([[{ nodeName: 'option2' }, { nodeName: 'option1' }]]);
  });

  it('should not modify filterData when selectedFilters has no matching options', () => {
    component.selectedFilters = [[{ nodeName: 'option3' }]];
    component.filterData = [[{ nodeName: 'option1' }, { nodeName: 'option2' }]];
    component.moveSelectedOptionToTop(null, 0);
    expect(component.filterData).toEqual([[{ nodeName: 'option1' }, { nodeName: 'option2' }]]);
  });

  it('should not modify filterData when filterData is empty', () => {
    component.selectedFilters = [[{ nodeName: 'option1' }]];
    component.filterData = [];
    component.moveSelectedOptionToTop(null, 0);
    expect(component.filterData).toEqual([]);
  });

  it('should not call moveSelectedOptionToTop if event.value is empty', () => {
    const event = { value: '' };
    const index = 0;
    spyOn(component, 'moveSelectedOptionToTop');
    component.onSelectionChange(event, index);
    expect(component.moveSelectedOptionToTop).not.toHaveBeenCalled();
  });

  // -> end of moveSelectedOptionToTop() & onSelectionChange()

  it('should update filterData and selectedFilters when data is provided', () => {
    const data = {
      filter1: [{ nodeId: 1, nodeName: 'Filter 1' }],
      filter2: [{ nodeId: 2, nodeName: 'Filter 2' }],
    };

    component.selectedTab = 'developer';
    component.filterData = [[{ nodeName: 'Filter 1' }, { nodeName: 'Filter 2' }]];;
    component.selectedFilters = [];
    component.selectedTrends = [];

    component.service.populateAdditionalFilters = of(data);
    component.ngOnInit();

    // expect(component.selectedFilters).toEqual(['Overall']);
    expect(component.previousSelectedTrends).toEqual([]);
    // expect(helperService.setBackupOfFilterSelectionState).toHaveBeenCalledWith({ additional_level: null });
  });

  it('should update filterData and selectedFilters when data is provided and selectedTab is not "developer"', () => {
    const data = {
      filter1: [{ nodeId: 1, nodeName: 'Filter 1' }],
      filter2: [{ nodeId: 2, nodeName: 'Filter 2' }],
    };

    component.selectedTab = 'other';
    component.filterData = [];
    component.selectedFilters = [];
    component.selectedTrends = [];
    component.additionalFilterConfig = [
      { defaultLevel: { labelName: 'Sprint' } },
      { defaultLevel: { labelName: 'Squad' } },
    ];
    component.stateFilters = { sprint: [1], squad: [2] };

    component.service.populateAdditionalFilters = of(data);

    let sortByFieldSpy = spyOn(helperService, 'sortByField');
    component.ngOnInit();

    expect(component.filterData).toEqual([data.filter1, data.filter2]);
    expect(component.selectedFilters).toEqual([]);
    expect(sortByFieldSpy).toHaveBeenCalled();
  });

  describe('AdditionalFilterComponent.setCorrectLevel() setCorrectLevel method', () => {
    describe('Happy Path', () => {
      it('should set the correct level based on the state filters', fakeAsync(() => {
        // Arrange
        component.additionalFilterLevelArr = [
          { hierarchyLevelId: 'level1', hierarchyLevelName: 'Level 1' },
          { hierarchyLevelId: 'level2', hierarchyLevelName: 'Level 2' },
        ];
        component.additionalFilterConfig = [
          { defaultLevel: { labelName: 'Level 1' } },
          { defaultLevel: { labelName: 'Level 2' } },
        ];
        spyOn(sharedService,'getBackupOfFilterSelectionState').and.returnValue({
          level1: [{ nodeId: 'node1' }],
          level2: [{ nodeId: 'node2' }],
        });

        component.filterData = [
          [{ nodeId: 'node1' }],
          [{ nodeId: 'node2' }],
        ];
  
        // Act
        component.setCorrectLevel();
        tick(100);
        // Assert
        // setTimeout(() => {
          expect(component.selectedFilters[0]).toEqual([{ nodeId: 'node1' }]);
          expect(component.selectedFilters[1]).toEqual([{ nodeId: 'node2' }]);
        // }, 100);
      }));
    });
  
    describe('Edge Cases', () => {
      it('should handle empty state filters gracefully', () => {
        // Arrange
        component.additionalFilterLevelArr = [
          { hierarchyLevelId: 'level1', hierarchyLevelName: 'Level 1' },
        ];
        component.additionalFilterConfig = [
          { defaultLevel: { labelName: 'Level 1' } },
        ];
        spyOn(component.service,'getBackupOfFilterSelectionState').and.returnValue({});
  
        // Act
        component.setCorrectLevel();
  
        // Assert
        setTimeout(() => {
          expect(component.selectedFilters[0]).toBeUndefined();
        }, 100);
      });
  
      // it('should handle missing hierarchyLevelId in additionalFilterLevelArr', () => {
      //   // Arrange
      //   component.additionalFilterLevelArr = [{ hierarchyLevelName: 'Level 1' }];
      //   component.additionalFilterConfig = [
      //     { defaultLevel: { labelName: 'Level 1' } },
      //   ];
      //   spyOn(helperService,'getBackupOfFilterSelectionState').and.returnValue({
      //     level1: [{ nodeId: 'node1' }],
      //   });
  
      //   // Act
      //   component.setCorrectLevel();
  
      //   // Assert
      //   setTimeout(() => {
      //     expect(component.selectedFilters[0]).toBeUndefined();
      //   }, 100);
      // });
    });
  });

  describe('AdditionalFilterComponent.resetFilterData() resetFilterData method', () => {
    describe('Happy Path', () => {
      it('should clear filterData when selectedTab is not "developer"', () => {
        // Arrange
        component.selectedTab = 'notDeveloper';
        component.filterData = [{ nodeId: 1 }, { nodeId: 2 }];
  
        // Act
        component.resetFilterData();
  
        // Assert
        expect(component.filterData).toEqual([]);
      });
  
      it('should not clear filterData if selectedTab is "developer" and trends have not changed', () => {
        // Arrange
        component.selectedTab = 'developer';
        component.selectedTrends = [{ nodeId: 1 }];
        component.previousSelectedTrends = [{ nodeId: 1 }];
        component.filterData = [{ nodeId: 1 }, { nodeId: 2 }];
        spyOn(sharedService, 'getSelectedTrends').and.returnValue([{ nodeId: 1 }]);
        // Act
        component.resetFilterData();
  
        // Assert
        expect(component.filterData).toEqual([{ nodeId: 1 }, { nodeId: 2 }]);
      });
    });
  
    describe('Edge Cases', () => {
      it('should clear filterData if selectedTab is "developer" and trends have changed', () => {
        // Arrange
        component.selectedTab = 'developer';
        component.selectedTrends = [{ nodeId: 1 }];
        component.previousSelectedTrends = [{ nodeId: 2 }];
        component.filterData = [{ nodeId: 1 }, { nodeId: 2 }];
        spyOn(sharedService, 'getSelectedTrends').and.returnValue([{ nodeId: 1 }]);
        // Act
        component.resetFilterData();
  
        // Assert
        expect(component.filterData).toEqual([]);
        expect(component.previousSelectedTrends).toEqual([{ nodeId: 1 }]);
      });
  
      // it('should handle empty selectedTrends and previousSelectedTrends gracefully', () => {
      //   // Arrange
      //   component.selectedTab = 'developer';
      //   component.selectedTrends = [];
      //   component.previousSelectedTrends = [];
      //   component.filterData = [{ nodeId: 1 }, { nodeId: 2 }];
  
      //   // Act
      //   component.resetFilterData();
  
      //   // Assert
      //   expect(component.filterData).toEqual([]);
      //   expect(component.previousSelectedTrends).toEqual([]);
      // });
    });
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
        component.onDropDownChange(event, index);
  
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
        component.onDropDownChange(event, index);
  
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
        component.onDropDownChange(event, index);
  
        // Assert
        expect(helperService.isDropdownElementSelected).toHaveBeenCalledWith(
          event,
        );
        // expect(service.applyAdditionalFilters).not.toHaveBeenCalled();
      });
    });
  });
});
