import { ComponentFixture, TestBed } from '@angular/core/testing';
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
    getAuth = TestBed.get(GetAuthService);
    httpService = TestBed.inject(HttpService);
    sharedService = TestBed.inject(SharedService);
    helperService = TestBed.inject(HelperService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should reset filterData, filterSet, and selectedFilters when selectedTab changes', () => {
    component.filterData = ['Filter 1', 'Filter 2'];
    component.filterSet = new Set(['Filter 1', 'Filter 2']);
    component.selectedFilters = ['Filter 1', 'Filter 2'];

    component.ngOnChanges({
      selectedTab: {
        currentValue: 'New Tab', previousValue: 'Old Tab', firstChange: false,
        isFirstChange: function (): boolean {
          return false;
        }
      }
    });

    expect(component.filterData).toEqual([]);
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
    component.selectedTab = 'mydashboard';
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

  it('should set selectedFilters to Overall if filterData has Overall and selectedTab is developer', () => {
    const data = {
      filter: [
        {
          "nodeId": "Overall",
          "nodeName": "Overall"
        },
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
    expect(component.selectedFilters).toEqual([{ nodeId: 'Overall', nodeName: 'Overall' }]);
    expect(component.selectedTrends).toEqual([]);
  });


  it('should apply additional filters and update appliedFilters when selectedTab is developer and filterData has length 1', () => {
    component.selectedTab = 'Developer';
    component.filterData = [[{ nodeId: 1, nodeName: 'Filter 1' }]];
    spyOn(sharedService, 'applyAdditionalFilters');

    component.applyAdditionalFilter({ value: 1 }, 0);

    expect(component.appliedFilters['filter']).toEqual([1]);
    expect(sharedService.applyAdditionalFilters).toHaveBeenCalledWith(1);
  });

  it('should apply additional filters and update appliedFilters when selectedTab is developer and filterData has length greater than 1', () => {
    component.selectedTab = 'Developer';
    component.filterData = [[{ nodeId: 1, nodeName: 'Filter 1' }], [{ nodeId: 2, nodeName: 'Filter 2' }]];
    spyOn(sharedService, 'applyAdditionalFilters');

    component.applyAdditionalFilter({ value: 2 }, 1);

    expect(component.appliedFilters['filter1']).toEqual([2]);
    expect(sharedService.applyAdditionalFilters).toHaveBeenCalledWith(2);
  });

});