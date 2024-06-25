import { ComponentFixture, TestBed, tick, fakeAsync } from '@angular/core/testing';
import { KpiCardV2Component } from './kpi-card-v2.component';

import { RouterTestingModule } from '@angular/router/testing';
import { SharedService } from '../../services/shared.service';
import { HelperService } from 'src/app/services/helper.service';
import { GetAuthService } from '../../services/getauth.service';
import { HttpClientModule } from '@angular/common/http';
import { APP_CONFIG, AppConfig } from '../../services/app.config';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpService } from '../../services/http.service';
import { CommonModule, DatePipe } from '@angular/common';
import { DialogService, DynamicDialogRef } from 'primeng/dynamicdialog';
import { of } from 'rxjs';

describe('KpiCardV2Component', () => {
  let component: KpiCardV2Component;
  let fixture: ComponentFixture<KpiCardV2Component>;
  let getAuth: GetAuthService;
  let httpService: HttpService
  let sharedService: SharedService;
  let helperService: HelperService;
  let dialogService: DialogService;
  const fakeKpiFieldMappingList = require('../../../test/resource/fakeMappingFieldConfig.json');
  const dropDownMetaData = require('../../../test/resource/KPIConfig.json');
  const fakeSelectedFieldMapping = {
    "id": "63282cbaf5c740241aff32a1",
    "projectToolConfigId": "63282ca6487eff1e8b70b1bb",
    "basicProjectConfigId": "63282c82487eff1e8b70b1b9",
    "sprintName": "customfield_12700",
    "jiradefecttype": [
      "Defect"
    ],
    "defectPriority": [],
    "jiraIssueTypeNames": [
      "Story",
      "Enabler Story",
      "Change request",
      "Defect",
      "Epic"
    ],
    "jiraBugRaisedByQACustomField": "",
    "jiraBugRaisedByQAIdentification": "",
    "jiraBugRaisedByQAValue": [],
    "jiraDefectDroppedStatus": [],
    "epicCostOfDelay": "customfield_58102",
    "epicRiskReduction": "customfield_58101",

  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [KpiCardV2Component],
      imports: [RouterTestingModule, HttpClientModule, BrowserAnimationsModule],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],

      providers: [SharedService, GetAuthService, HttpService, HelperService, CommonModule, DatePipe, DialogService,
        { provide: APP_CONFIG, useValue: AppConfig }
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(KpiCardV2Component);
    component = fixture.componentInstance;
    getAuth = TestBed.get(GetAuthService);
    httpService = TestBed.inject(HttpService);
    sharedService = TestBed.inject(SharedService);
    helperService = TestBed.inject(HelperService);
    dialogService = TestBed.inject(DialogService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('checkIfDataPresent should return true when data is an array with length greater than 0', () => {
    const data = [1, 2, 3];

    const result = component.checkIfDataPresent(data);

    expect(result).toBe(true);
  });

  it('checkIfDataPresent should return true when data is an object with at least one key', () => {
    const data = { key1: 'value1', key2: 'value2' };

    const result = component.checkIfDataPresent(data);

    expect(result).toBe(true);
  });

  it('checkIfDataPresent should return false when data is an empty array', () => {
    const data = [];

    const result = component.checkIfDataPresent(data);

    expect(result).toBe(false);
  });

  it('checkIfDataPresent should return false when data is an empty object', () => {
    const data = {};

    const result = component.checkIfDataPresent(data);

    expect(result).toBe(false);
  });

  it('checkIfDataPresent should return false when data is undefined', () => {
    const data = undefined;

    const result = component.checkIfDataPresent(data);

    expect(result).toBe(false);
  });

  it('checkIfDataPresent should return false when data is a string', () => {
    const data = 'test';

    const result = component.checkIfDataPresent(data);

    expect(result).toBe(false);
  });

  it('checkIfDataPresent should return false when data is a number', () => {
    const data = 123;

    const result = component.checkIfDataPresent(data);

    expect(result).toBe(false);
  });

  it('checkIfDataPresent should return false when data is a boolean', () => {
    const data = true;

    const result = component.checkIfDataPresent(data);

    expect(result).toBe(false);
  });

  it('should prepare Data for display', () => {
    component.colors = {
      'API POD 1 - Core_6524a7677c8bb73cd0c3fe67': {
        "nodeName": "API POD 1 - Core",
        "color": "#6079C5",
        "nodeId": "API POD 1 - Core_6524a7677c8bb73cd0c3fe67"
      }
    }

    component.trendValueList = [{
      "data": "API POD 1 - Core",
      "maturity": "4",
      "value": [
        {
          "data": "29",
          "sSprintID": "55039_API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
          "sSprintName": "Sprint 16_API POD 1 - Core",
          "hoverValue": {
            "Defects": 5,
            "Stories": 17
          },
          "sprintIds": [
            "55039_API POD 1 - Core_6524a7677c8bb73cd0c3fe67"
          ],
          "sprintNames": [
            "Sprint 16_API POD 1 - Core"
          ],
          "value": 29.411764705882355,
          "sprojectName": "API POD 1 - Core",
          "sortSprint": "Sprint 16",
          "xName": 1
        },
        {
          "data": "40",
          "sSprintID": "55040_API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
          "sSprintName": "Sprint 17_API POD 1 - Core",
          "hoverValue": {
            "Defects": 4,
            "Stories": 10
          },
          "sprintIds": [
            "55040_API POD 1 - Core_6524a7677c8bb73cd0c3fe67"
          ],
          "sprintNames": [
            "Sprint 17_API POD 1 - Core"
          ],
          "value": 40,
          "sprojectName": "API POD 1 - Core",
          "sortSprint": "Sprint 17",
          "xName": 2
        },
        {
          "data": "109",
          "sSprintID": "55041_API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
          "sSprintName": "Sprint 18_API POD 1 - Core",
          "hoverValue": {
            "Defects": 12,
            "Stories": 11
          },
          "sprintIds": [
            "55041_API POD 1 - Core_6524a7677c8bb73cd0c3fe67"
          ],
          "sprintNames": [
            "Sprint 18_API POD 1 - Core"
          ],
          "value": 109.09090909090908,
          "sprojectName": "API POD 1 - Core",
          "sortSprint": "Sprint 18",
          "xName": 3
        },
        {
          "data": "80",
          "sSprintID": "55042_API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
          "sSprintName": "Sprint 19_API POD 1 - Core",
          "hoverValue": {
            "Defects": 8,
            "Stories": 10
          },
          "sprintIds": [
            "55042_API POD 1 - Core_6524a7677c8bb73cd0c3fe67"
          ],
          "sprintNames": [
            "Sprint 19_API POD 1 - Core"
          ],
          "value": 80,
          "sprojectName": "API POD 1 - Core",
          "sortSprint": "Sprint 19",
          "xName": 4
        },
        {
          "data": "67",
          "sSprintID": "55043_API POD 1 - Core_6524a7677c8bb73cd0c3fe67",
          "sSprintName": "Sprint 20_API POD 1 - Core",
          "hoverValue": {
            "Defects": 4,
            "Stories": 6
          },
          "sprintIds": [
            "55043_API POD 1 - Core_6524a7677c8bb73cd0c3fe67"
          ],
          "sprintNames": [
            "Sprint 20_API POD 1 - Core"
          ],
          "value": 66.66666666666666,
          "sprojectName": "API POD 1 - Core",
          "sortSprint": "Sprint 20",
          "xName": 5
        }
      ],
      "maturityValue": "65.03"
    }]

    component.kpiData = {
      kpiDetail: {
        "id": "64b4ed7acba3c12de16472ec",
        "kpiId": "kpi14",
        "kpiName": "Defect Injection Rate",
        "isDeleted": "False",
        "defaultOrder": 1,
        "kpiUnit": "%",
        "chartType": "line",
        "upperThresholdBG": "red",
        "lowerThresholdBG": "white",
        "showTrend": true,
        "isPositiveTrend": false,
        "calculateMaturity": true,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "200",
        "thresholdValue": 10,
        "kanban": false,
        "groupId": 3,
        "kpiInfo": {
          "definition": "Meausures the Percentage of Defect created and linked to stories in a sprint against the number of stories in the same sprint",
          "formula": [
            {
              "lhs": "DIR for a sprint",
              "operator": "division",
              "operands": [
                "No. of defects tagged to all stories closed in a sprint",
                "Total no. of stories closed in the sprint"
              ]
            }
          ],
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#Defect-Injection-Rate"
              }
            }
          ]
        },
        "aggregationCriteria": "average",
        "maturityRange": [
          "-175",
          "175-125",
          "125-75",
          "75-25",
          "25-"
        ],
        "trendCalculative": false,
        "xaxisLabel": "Sprints",
        "yaxisLabel": "Percentage",
        "isAdditionalFilterSupport": true
      }
    }

    component.prepareData();

    expect(component.sprintDetailsList).toEqual(
      [{
        "project": "API POD 1 - Core",
        "hoverList": [
          {
            "duration": "Sprint 16_API POD 1 - Core",
            "value": 29.41,
            "unit": " %",
            "params": "Defects : 5, Stories : 17"
          },
          {
            "duration": "Sprint 17_API POD 1 - Core",
            "value": 40,
            "unit": " %",
            "params": "Defects : 4, Stories : 10"
          },
          {
            "duration": "Sprint 18_API POD 1 - Core",
            "value": 109.09,
            "unit": " %",
            "params": "Defects : 12, Stories : 11"
          },
          {
            "duration": "Sprint 19_API POD 1 - Core",
            "value": 80,
            "unit": " %",
            "params": "Defects : 8, Stories : 10"
          },
          {
            "duration": "Sprint 20_API POD 1 - Core",
            "value": 66.67,
            "unit": " %",
            "params": "Defects : 4, Stories : 6"
          }
        ],
        "color": "#6079C5"
      }])
  });

  it('should get Mapping configuration', () => {
    component.kpiData = {
      kpiId: 'kpi3',
      kpiDetail: {
        kpiId: 'kpi3',
        kpiSource: 'Jira',
        combinedKpiSource: 'Jira/Azure'
      }
    };
    spyOn(sharedService, 'getSelectedTab').and.returnValue('My Dashboard');
    spyOn(sharedService, 'getSelectedType').and.returnValue('scrum');
    spyOn(sharedService, 'getSelectedTrends').and.returnValue([{ basicProjectConfigId: '123' }]);
    spyOn(httpService, 'getKPIFieldMappingConfig').and.returnValue(of(fakeKpiFieldMappingList));

    const fakeMetaDataList = [
      {
        projectID: '123',
        kpiSource: 'jira',
        metaData: dropDownMetaData.data
      }
    ]
    spyOn(sharedService, 'getFieldMappingMetaData').and.returnValue(fakeMetaDataList);
    component.getKPIFieldMappingConfig();
    expect(component.fieldMappingConfig.length).toEqual(fakeKpiFieldMappingList.data.fieldConfiguration.length);
  })

  it('should get FieldMapping', () => {
    component.selectedToolConfig = [{ id: '123' }];
    component.kpiData = {
      kpiId: 'pi123'
    }
    spyOn(httpService, 'getFieldMappingsWithHistory').and.returnValue(of({
      success: true, data: {
        fieldMappingResponses: fakeSelectedFieldMapping,
        metaTemplateCode: '10'
      }
    }));
    component.getFieldMapping();
    expect(Object.keys(component.selectedFieldMapping).length).toBeGreaterThan(0);
  })

  it('should get getFieldMappingMetaData', () => {
    component.selectedToolConfig = [{ id: '123' }];
    spyOn(httpService, 'getKPIConfigMetadata').and.returnValue(of(dropDownMetaData));
    component.getFieldMappingMetaData('jira');
    expect(component.fieldMappingMetaData).not.toBeNull();
  });

  it('should handle filter change for radio', () => {
    const spy = spyOn(component.optionSelected, 'emit');
    component.handleChange('radio', 'Story Points');
    expect(spy).toHaveBeenCalledWith('Story Points');
  });

  it('should handle filter change for single select', () => {
    const filterOptionsingle = {
      filter1: [{}]
    };
    component.filterOptions = {};
    const spy = spyOn(component.optionSelected, 'emit');
    component.handleChange('single', undefined);
    expect(spy).toHaveBeenCalled();
  });

  it('should handle filter change for multi select', () => {
    const filterOptionMulti = { filter1: ['P1', 'P2'] };
    component.filterOptions = filterOptionMulti;
    const spy = spyOn(component.optionSelected, 'emit');
    component.handleChange('multi', undefined);
    expect(spy).toHaveBeenCalledWith(filterOptionMulti);
  });

  it('should set filter default option', fakeAsync(() => {
    const response = {
      kpi113: [
        'Overall'
      ]
    };

    component.kpiData = {
      kpiId: 'kpi113',
      kpiName: 'Value delivered (Cost of Delay)',
      isEnabled: true,
      order: 28,
      kpiDetail: {
        id: '633ed17f2c2d5abef2451ff3',
        kpiId: 'kpi113',
      },
      shown: true
    };
    sharedService.setKpiSubFilterObj(response);
    component.ngOnInit();
    tick();
    expect(component.filterOption).toBe('Overall');
  }));

  it('should set default filter value for kpi having radiobutton filter', fakeAsync(() => {
    component.kpiData = {
      kpiId: 'kpi3',
      kpiName: 'Lead Time',
      isEnabled: true,
      order: 25,
      kpiDetail: {
        id: '633ed17f2c2d5abef2451ff0',
        kpiId: 'kpi3',
        kpiName: 'Lead Time',
        kpiSource: 'Jira',
        kanban: false,
        kpiFilter: 'radioButton',
      },
      shown: true
    };

    const response = { kpi3: ['default'], action: "update" };
    sharedService.setKpiSubFilterObj(response);
    component.ngOnInit();
    tick();
    expect(component.radioOption).toBe('default');
  }));

  it('should set menuItems correctly', () => {

    component.selectedTab = 'release';
    component.colors = {};
    component.ngOnChanges({});
    expect(component.menuItems).toEqual([
      {
        label: 'Settings',
        icon: 'fas fa-cog',
        command: jasmine.any(Function),
        disabled: false
      },
      {
        label: 'List View',
        icon: 'pi pi-align-justify',
        command: jasmine.any(Function),
        disabled: true
      },
      {
        label: 'Explore',
        icon: 'pi pi-table',
        command: jasmine.any(Function),
      },
      {
        label: 'Comments',
        icon: 'pi pi-comments',
        command: jasmine.any(Function),
      },
    ]);
  });

  it('should subscribe to selectedFilterOptionObs and set filterOptions and filterOption correctly', () => {
    const selectedFilterOptionObs = {
      kpi72: {
        filter1: ['Overall'],
        filter2: ['Option 1'],
      },
    };

    sharedService.setKpiSubFilterObj(selectedFilterOptionObs);

    // const selectedFilterOptionObsSpy = spyOn(sharedService,'setKpiSubFilterObj').and.callFake(()=>of(selectedFilterOptionObs));

    component.ngOnInit();

    // expect(selectedFilterOptionObsSpy).toHaveBeenCalled();
    // expect(component.filterOptions).toEqual({
    //   filter1: ['Overall'],
    //   filter2: ['Option 1'],
    // });
    expect(component.filterOption).toBe('Overall');
  });

  // it('should set radioOption correctly when kpiFilter is "radiobutton"', () => {
  //   const selectedFilterOptionObs = of({
  //     kpi72: {
  //       filter1: ['Option 1'],
  //     },
  //   });
  //   sharedService.selectedFilterOptionObs.and.returnValue(selectedFilterOptionObs);
  //   component.kpiData = {
  //     kpiDetail: {
  //       kpiFilter: 'radiobutton',
  //     },
  //   };

  //   component.ngOnInit();

  //   expect(component.radioOption).toBe('Option 1');
  // });

  // it('should set selectedTab correctly', () => {
  //   const selectedFilterOptionObs = of({});
  //   sharedService.selectedFilterOptionObs.and.returnValue(selectedFilterOptionObs);
  //   sharedService.getSelectedTab.and.returnValue('Tab 1');

  //   component.ngOnInit();

  //   expect(sharedService.getSelectedTab).toHaveBeenCalled();
  //   expect(component.selectedTab).toBe('tab 1');
  // });

  // it('should set radioOption correctly when dropdownArr is not empty', () => {
  //   const selectedFilterOptionObs = of({});
  //   sharedService.selectedFilterOptionObs.and.returnValue(selectedFilterOptionObs);
  //   component.kpiData = {
  //     kpiDetail: {
  //       kpiFilter: 'radiobutton',
  //     },
  //   };
  //   component.dropdownArr = [
  //     {
  //       options: ['Option 1', 'Option 2'],
  //     },
  //   ];

  //   component.ngOnInit();

  //   expect(component.radioOption).toBe('Option 1');
  // });

  it('should delete the matching key from filterOptions', () => {
    const event = 'Event 1';
    const filterOptions = {
      'Event 1': 'Option 1',
      'Event 2': 'Option 2',
      'Event 3': 'Option 3'
    };

    component.handleClearAll(event);

    expect(filterOptions[event]).toEqual('Option 1');
  });

  it('should emit the correct event', () => {
    const event = 'Event 1';
    const filterOptions = {
      'Event 1': 'Option 1',
      'Event 2': 'Option 2',
      'Event 3': 'Option 3'
    };

    const emitSpy = spyOn(component.optionSelected, 'emit');

    component.handleClearAll(event);

    expect(emitSpy).toHaveBeenCalledWith(['Overall']);
  });

  it('should not delete any key if there is no match', () => {
    const event = 'Event 4';
    const filterOptions = {
      'Event 1': 'Option 1',
      'Event 2': 'Option 2',
      'Event 3': 'Option 3'
    };

    component.handleClearAll(event);

    expect(filterOptions).toEqual({
      'Event 1': 'Option 1',
      'Event 2': 'Option 2',
      'Event 3': 'Option 3'
    });
  });
});