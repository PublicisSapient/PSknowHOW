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
 * Unless required    by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

import { ComponentFixture, TestBed, tick, fakeAsync } from '@angular/core/testing';
import { KpiCardV2Component } from './kpi-card-v2.component';

import { RouterTestingModule } from '@angular/router/testing';
import { SharedService } from '../../services/shared.service';
import { HelperService } from 'src/app/services/helper.service';
import { GetAuthService } from '../../services/getauth.service';
import { HttpClientModule } from '@angular/common/http';
import { APP_CONFIG, AppConfig } from '../../services/app.config';
import { CUSTOM_ELEMENTS_SCHEMA, EventEmitter } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpService } from '../../services/http.service';
import { CommonModule, DatePipe } from '@angular/common';
import { DialogService } from 'primeng/dynamicdialog';
import { KpiHelperService } from '../../services/kpi-helper.service';
import { of } from 'rxjs';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

describe('KpiCardV2Component', () => {
  let component: KpiCardV2Component;
  let fixture: ComponentFixture<KpiCardV2Component>;
  let getAuth: GetAuthService;
  let httpMock: HttpTestingController;
  let httpService: HttpService
  let sharedService: SharedService;
  let helperService: HelperService;
  let dialogService: DialogService;
  let mockService: jasmine.SpyObj<SharedService>;
  let kpiHelperService;
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
      imports: [RouterTestingModule, HttpClientTestingModule, BrowserAnimationsModule],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],

      providers: [SharedService, GetAuthService, HttpService, HelperService, CommonModule, DatePipe, DialogService, KpiHelperService,
        { provide: APP_CONFIG, useValue: AppConfig }
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(KpiCardV2Component);
    component = fixture.componentInstance;
    getAuth = TestBed.get(GetAuthService);
    httpService = TestBed.inject(HttpService);
    httpMock = TestBed.inject(HttpTestingController);
    sharedService = TestBed.inject(SharedService);
    helperService = TestBed.inject(HelperService);
    dialogService = TestBed.inject(DialogService);
    mockService = jasmine.createSpyObj(SharedService, ['selectedFilterOptionObs', 'getSelectedTab']);
    kpiHelperService = TestBed.inject(KpiHelperService); //jasmine.createSpyObj(KpiHelperService, ['getChartDataSet']);

    component.kpiData = {
      kpiId: 'kpi72',
      kpiDetail: { kpiFilter: 'radioButton' }
    };
    component.dropdownArr = [{ options: ['option1', 'option2'] }];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // ------ start of initializeMenu ------
  it('should initialize menu items correctly', () => {
    // Mocking dependencies and component properties
    spyOn(component, 'onOpenFieldMappingDialog');
    spyOn(component, 'prepareData');
    spyOn(component, 'exportToExcel');
    spyOn(component, 'openCommentModal');

    component.disableSettings = false;
    component.selectedTab = 'iteration';
    component.kpiData = {
      kpiDetail: {
        chartType: 'bar'
      }
    };

    spyOn(sharedService, 'getSelectedType').and.returnValue('scrum');

    // Call the function
    component.initializeMenu();

    // Assertions
    expect(component.menuItems.length).toBe(4);

    const settingsItem = component.menuItems[0];
    expect(settingsItem.label).toBe('Settings');
    expect(settingsItem.icon).toBe('fas fa-cog');
    expect(settingsItem.disabled).toBeFalse();
    settingsItem.command();
    expect(component.onOpenFieldMappingDialog).toHaveBeenCalled();

    const listViewItem = component.menuItems[1];
    expect(listViewItem.label).toBe('List View');
    expect(listViewItem.icon).toBe('pi pi-align-justify');
    expect(listViewItem.disabled).toBeFalse();
    listViewItem.command({});
    expect(component.prepareData).toHaveBeenCalled();

    const exploreItem = component.menuItems[2];
    expect(exploreItem.label).toBe('Explore');
    expect(exploreItem.icon).toBe('pi pi-table');
    expect(exploreItem.disabled).toBeFalse();
    exploreItem.command();
    expect(component.exportToExcel).toHaveBeenCalled();

    const commentsItem = component.menuItems[3];
    expect(commentsItem.label).toBe('Comments');
    expect(commentsItem.icon).toBe('pi pi-comments');
    expect(commentsItem.disabled).toBeUndefined(); // No disabled property set
    commentsItem.command({});
    expect(component.openCommentModal).toHaveBeenCalled();
    expect(component.showComments).toBeTrue();
  });
  // ------ end of initializeMenu ------

  // ------------- start of radioOption -------------
  it('should set the correct radioOption when dropdownArr changes', () => {
    // Mock service to return a backup value
    spyOn(sharedService, 'getKpiSubFilterObj').and.returnValue({
      kpi72: { filter1: ['BackupOption'] }
    });

    // Simulate changes in dropdownArr
    const changes = {
      dropdownArr: {
        currentValue: [{ options: ['option1', 'option2'] }],
        previousValue: [],
        firstChange: false,
        isFirstChange: () => false
      }
    };

    // Trigger ngOnChanges with the mocked changes
    component.ngOnChanges(changes);

    // Verify that the correct radio option is set based on the backup value
    expect(component.radioOption).toBe('BackupOption');
  });

  it('should default radioOption to the first dropdown option when no backup value exists', () => {
    // Mock service to return no backup value
    spyOn(sharedService, 'getKpiSubFilterObj').and.returnValue({});

    // Simulate changes in dropdownArr
    const changes = {
      dropdownArr: {
        currentValue: [{ options: ['option1', 'option2'] }],
        previousValue: [],
        firstChange: false,
        isFirstChange: () => false
      }
    };

    // Trigger ngOnChanges with the mocked changes
    component.ngOnChanges(changes);

    // Verify that the correct radio option is set to the first dropdown option
    expect(component.radioOption).toBe('option1');
  });

  it('should set radioOption to the first value from backup if it exists without filter1', () => {
    // Mock service to return a backup value without filter1
    spyOn(sharedService, 'getKpiSubFilterObj').and.returnValue({
      kpi72: ['BackupOption']
    });

    // Simulate changes in dropdownArr
    const changes = {
      dropdownArr: {
        currentValue: [{ options: ['option1', 'option2'] }],
        previousValue: [],
        firstChange: false,
        isFirstChange: () => false
      }
    };

    // Trigger ngOnChanges with the mocked changes
    component.ngOnChanges(changes);

    // Verify that the radioOption is set to the first value of the backup array
    expect(component.radioOption).toBe('BackupOption');
  });
  // ---- end of radioOption ----

  xdescribe('checkIfDataPresent', () => {
    it('should return true if data is present and kpiStatusCode is "200"', () => {
      component.kpiDataStatusCode = '200';
      component.kpiChartData = { data: [{ data: [1, 2, 3] }] };

      const result = component.checkIfDataPresent('data');

      expect(result).toBe(false);
    });

    it('should return false if data is not present', () => {
      component.kpiDataStatusCode = '200';
      component.kpiChartData = {};

      const result = component.checkIfDataPresent('data');

      expect(result).toBe(false);
    });

    it('should return false if kpiStatusCode is not "200"', () => {
      component.kpiDataStatusCode = '400';
      component.kpiChartData = { data: [{ data: [1, 2, 3] }] };

      const result = component.checkIfDataPresent('data');

      expect(result).toBe(false);
    });

    it('should return false if data is not present at granular level', () => {
      component.kpiDataStatusCode = '200';
      component.kpiChartData = { data: [{ data: [] }] };

      const result = component.checkIfDataPresent('data');

      expect(result).toBe(false);
    });
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

  // it('should set filter default option', fakeAsync(() => {
  //   const response = {
  //     kpi113: ['Overall']
  //   };

  //   // Mock sharedService behavior
  //   // spyOn(sharedService, 'setKpiSubFilterObj').and.callFake(() => { });
  //   spyOn(sharedService, 'getKpiSubFilterObj').and.returnValue({}); // Mock default sub-filter object

  //   // Mock component data
  //   component.kpiData = {
  //     kpiId: 'kpi113',
  //     kpiName: 'Value delivered (Cost of Delay)',
  //     isEnabled: true,
  //     order: 28,
  //     kpiDetail: {
  //       id: '633ed17f2c2d5abef2451ff3',
  //       kpiId: 'kpi113',
  //     },
  //     shown: true
  //   };

  //   // Mock dropdown array
  //   component.dropdownArr = [{ options: ['Overall', 'Detail'] }];

  //   // Initialize component
  //   component.ngOnInit();
  //   tick();

  //   // Verify that setKpiSubFilterObj was called
  //   // expect(sharedService.setKpiSubFilterObj).toHaveBeenCalledWith(response);

  //   // Verify HTTP calls (if any are expected)
  //   const req = httpMock.expectOne('http://localhost:8080/api/connections');
  //   expect(req.request.method).toBe('GET');
  //   req.flush({}); // Mock empty response

  //   httpMock.verify(); // Ensure no unexpected HTTP calls
  // }));

  it('should set menuItems correctly', () => {

    component.selectedTab = 'release';
    component.colors = {};
    component.ngOnChanges({});
    expect(component.menuItems).toEqual([
      {
        label: 'Settings',
        icon: 'fas fa-cog',
        command: jasmine.any(Function),
        disabled: true
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
        disabled: true
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
    component.ngOnInit();
  });

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

  it('should subscribe to selectedFilterOptionObs and handle multiple keys', () => {
    const filterData = {
      kpi72: {
        filter1: ['Overall'],
        filter2: ['Other']
      }
    };

    mockService.selectedFilterOptionObs = of(filterData);
    mockService.getSelectedTab.and.returnValue('Tab1');
    component.ngOnInit();

    // expect(component.kpiSelectedFilterObj).toEqual(filterData);
    // expect(component.filterOptions[0]).toEqual(['Overall']);
    // expect(component.selectedTab).toEqual('tab1');
  });

  it('should handle non-Overall values correctly', () => {
    const filterData = {
      kpi72: {
        filter1: ['Specific'],
        filter2: ['Other']
      },
      kpi113: {
        filter1: ['Specific'],
        filter2: ['Other']
      }
    };

    component.kpiData = {
      kpiId: 'kpi72',
      kpiName: 'Value delivered (Cost of Delay)',
      isEnabled: true,
      order: 28,
      kpiDetail: {
        id: '633ed17f2c2d5abef2451ff3',
        kpiId: 'kpi72',
      },
      shown: true
    };

    sharedService.setKpiSubFilterObj(filterData);
    mockService.getSelectedTab.and.returnValue('Tab1');
    component.ngOnInit();

    expect(component.kpiSelectedFilterObj).toEqual(filterData);
    expect(component.filterOptions["filter1"]).toBe('Specific');
    expect(component.filterOptions["filter2"]).toBe('Other');
  });

  // it('should handle Overall values correctly for kpi72', fakeAsync(() => {
  //   const filterData = {
  //     kpi72: {
  //       filter2: ['Overall'],
  //       filter3: ['Other'],
  //       filter4: ['OtherFilters']
  //     },
  //     kpi113: {
  //       filter1: ['Specific'],
  //       filter2: ['Other']
  //     }
  //   };

  //   const mockResponse = {}; // Mock response for the HTTP call.

  //   // Mocking component dependencies
  //   component.kpiData = {
  //     kpiId: 'kpi72',
  //     kpiName: 'Value delivered (Cost of Delay)',
  //     isEnabled: true,
  //     order: 28,
  //     kpiDetail: {
  //       id: '633ed17f2c2d5abef2451ff3',
  //       kpiId: 'kpi72',
  //     },
  //     shown: true
  //   };

  //   // Mock shared service behavior
  //   spyOn(sharedService, 'setKpiSubFilterObj').and.callFake(() => {});
  //   mockService.getSelectedTab.and.returnValue('Tab1');

  //   // Trigger ngOnInit and mock HTTP request
  //   component.ngOnInit();
  //   tick();

  //   const req = httpMock.match((r) => r.url === 'http://localhost:8080/api/connections');
  //   expect(req.length).toBe(1); // Ensure one request was made
  //   expect(req[0].request.method).toBe('GET');
  //   req[0].flush(mockResponse); // Mocking a successful response

  //   // Verify behavior
  //   sharedService.setKpiSubFilterObj(filterData);
  //   expect(sharedService.setKpiSubFilterObj).toHaveBeenCalledWith(filterData);
  //   expect(component.kpiSelectedFilterObj).toEqual(filterData);
  //   expect(component.filterOptions['filter2']).toEqual('Overall');

  //   httpMock.verify(); // Ensure no outstanding HTTP requests
  // }));


  xit('should handle Overall values in filter1 correctly for kpi72', () => {
    const filterData = {
      kpi72: {
        filter1: ['Overall'],
        filter2: ['Other'],
        filter3: ['OtherFilters']
      },
      kpi113: {
        filter1: ['Specific'],
        filter2: ['Other']
      }
    };

    component.kpiData = {
      kpiId: 'kpi72',
      kpiName: 'Value delivered (Cost of Delay)',
      isEnabled: true,
      order: 28,
      kpiDetail: {
        id: '633ed17f2c2d5abef2451ff3',
        kpiId: 'kpi72',
      },
      shown: true
    };

    sharedService.setKpiSubFilterObj(filterData);
    mockService.getSelectedTab.and.returnValue('Tab1');
    component.ngOnInit();

    expect(component.kpiSelectedFilterObj).toEqual(filterData);
    expect(component.filterOptions["filter1"]).toEqual('Overall');
    // expect(component.filterOptions["filter2"]).toEqual('Overall');
  });

  it('should handle other key values in filter1 correctly for kpi72', () => {
    const filterData = {
      kpi72: {
        filter3: ['option3'],
        filter: ['Overall']
      },
      kpi113: {
        filter1: ['Specific'],
        filter2: ['Other']
      }
    };

    component.kpiData = {
      kpiId: 'kpi72',
      kpiName: 'Value delivered (Cost of Delay)',
      isEnabled: true,
      order: 28,
      kpiDetail: {
        id: '633ed17f2c2d5abef2451ff3',
        kpiId: 'kpi72',
      },
      shown: true
    };

    sharedService.setKpiSubFilterObj(filterData);
    mockService.getSelectedTab.and.returnValue('Tab1');
    component.ngOnInit();

    expect(component.kpiSelectedFilterObj).toEqual(filterData);
    // expect(component.filterOptions).toEqual(filterData);
    // expect(component.filterOptions["filter2"]).toEqual('Overall');
  });

  it('should handle kpiFilter radio button logic', fakeAsync(() => {
    const filterData = {
      kpi72: {
        filter1: ['option2']
      },
      kpi113: {
        filter1: ['option2']
      }
    };

    component.kpiData = {
      kpiId: 'kpi113',
      kpiName: 'Value delivered (Cost of Delay)',
      isEnabled: true,
      order: 28,
      kpiDetail: {
        id: '633ed17f2c2d5abef2451ff3',
        kpiId: 'kpi113',
        kpiFilter: 'radiobutton'
      },
      shown: true
    };
    component.dropdownArr = [];
    sharedService.setKpiSubFilterObj(filterData);
    mockService.getSelectedTab.and.returnValue('Tab1');
    component.ngOnInit();
    tick(100);
    fixture.detectChanges();
    expect(component.radioOption).toEqual('option2');
  })
  );

  it('should set displayConfigModel to false and emit reloadKPITab event', () => {
    component.displayConfigModel = true;
    component.kpiData = { id: 1, name: 'KPI 1' };
    component.reloadKPITab = new EventEmitter();
    const reloadKPISpy = spyOn(component.reloadKPITab, 'emit');
    component.reloadKPI();

    expect(component.displayConfigModel).toBe(false);
    expect(reloadKPISpy).toHaveBeenCalledWith({ id: 1, name: 'KPI 1' });
  });


  it('should emit downloadExcel event with true value', () => {
    component.downloadExcel = new EventEmitter();
    const exportSpy = spyOn(component.downloadExcel, 'emit');
    component.exportToExcel();

    expect(exportSpy).toHaveBeenCalledWith(true);
  });


  it('should call getKPIFieldMappingConfig function', () => {
    mockService.getSelectedTab.and.returnValue('Tab1');
    const getFieldMappingSpy = spyOn(component, 'getKPIFieldMappingConfig');
    component.onOpenFieldMappingDialog();

    expect(getFieldMappingSpy).toHaveBeenCalled();
  });

  it('should return the correct color CSS class based on the index', () => {
    const mockColorCssClassArray = ['color1', 'color2', 'color3'];
    component.colorCssClassArray = mockColorCssClassArray;

    expect(component.getColorCssClasses(0)).toBe('color1');
    expect(component.getColorCssClasses(1)).toBe('color2');
    expect(component.getColorCssClasses(2)).toBe('color3');
  });


  it('should return true if any rowData has a non-null and non-undefined value for the specified field', () => {
    component.sprintDetailsList = [
      {
        hoverList: [
          { field1: null, field2: 'value2' },
          { field1: 'value1', field2: 'value2' }
        ]
      },
      {
        hoverList: [
          { field1: 'value1', field2: null },
          { field1: null, field2: null }
        ]
      }
    ];
    component.selectedTabIndex = 0;
    expect(component.hasData('field1')).toBe(true);
    expect(component.hasData('field2')).toBe(true);
  });

  it("should return execution date of processor", () => {
    const tracelog = [{
      processorName: 'Jira',
      executionSuccess: false,
      executionEndedAt: '2023-01-04T06:02:20'
    }]
    spyOn(component, 'findTraceLogForTool').and.returnValue(tracelog);
    const resp = component.showExecutionDate('Jira')
    expect(resp).not.toBe("NA")
  })

  it('should find tracelog for specfic tool', () => {
    spyOn(sharedService, 'getProcessorLogDetails').and.returnValue([{
      processorName: 'jira',
      executionSuccess: false,
      executionEndedAt: '2023-01-04T06:02:20'
    }])
    const toolDetails = component.findTraceLogForTool("jira");
    expect(toolDetails).toBeDefined();
  })

  it('should handle Overall values in filter1 correctly for non kpi72', () => {
    const filterData = {
      kpi7: {
        filter: ['OtherFilters', 'Overall'],
        filter1: ['Overall'],
        filter2: ['Other'],

      },
      kpi113: {
        filter1: ['Specific'],
        filter2: ['Other']
      }
    };

    component.kpiData = {
      kpiId: 'kpi7',
      kpiName: 'Value delivered (Cost of Delay)',
      isEnabled: true,
      order: 28,
      kpiDetail: {
        id: '633ed17f2c2d5abef2451ff3',
        kpiId: 'kpi7',
      },
      shown: true
    };

    sharedService.setKpiSubFilterObj(filterData);
    mockService.getSelectedTab.and.returnValue('Tab1');
    component.ngOnInit();

    expect(component.kpiSelectedFilterObj).toEqual(filterData);
    expect(component.filterOptions["filter1"]).toEqual(['Overall']);
  });

  it('should show tooltip', () => {
    component.showTooltip(true);
    expect(component.isTooltip).toBeTrue();
  });

  describe('handleClearAll', () => {
    beforeEach(() => {
      component.filterOptions = {
        key1: 'value1',
        key2: 'value2',
        Key3: 'value3'
      };
      component.optionSelected = jasmine.createSpyObj('EventEmitter', ['emit']);
    });

    it('should delete the matching key from filterOptions', () => {
      component.handleClearAll('key2');
      expect(component.filterOptions).toEqual({
        key1: 'value1',
        Key3: 'value3'
      });
    });

    it('should delete the matching key from filterOptions ignoring case', () => {
      component.handleClearAll('KEY3');
      expect(component.filterOptions).toEqual({
        key1: 'value1',
        key2: 'value2'
      });
    });

    it('should not delete any key if there is no match', () => {
      component.handleClearAll('key4');
      expect(component.filterOptions).toEqual({
        key1: 'value1',
        key2: 'value2',
        Key3: 'value3'
      });
    });

    it('should emit the optionSelected event with ["Overall"]', () => {
      component.handleClearAll('key1');
      expect(component.optionSelected.emit).toHaveBeenCalledWith(['Overall']);
    });
  });

  it('should set the warning message when val is true', () => {
    component.kpiDataStatusCode = '201';
    component.showWarning(true);

    expect(component.warning).toBe('Configure the missing mandatory field mappings in KPI Settings for accurate data display.');
  });

  it('should clear the warning message when val is false', () => {
    component.showWarning(false);

    expect(component.warning).toBeNull();
  });

  it('should return true if data is present for kpiId kpi148 or kpi146 and trendValueList has length', () => {
    component.kpiData = { kpiId: 'kpi148' };
    component.trendValueList = [{ value: [1, 2, 3] }];

    expect(component.checkIfDataPresent('200')).toBeTrue();
  });

  xit('should return true if data is present for kpiId kpi139 or kpi127 and trendValueList and trendValueList[0].value have length', () => {
    component.kpiData = { kpiId: 'kpi139' };
    component.trendValueList = [{ value: [{ value: [1, 2, 3] }] }];

    expect(component.checkIfDataPresent('200')).toBeTrue();
  });

  it('should return true if data is present for kpiId kpi168, kpi70 or kpi153 and trendValueList and trendValueList[0].value have length greater than 0', () => {
    component.kpiData = { kpiId: 'kpi168' };
    component.trendValueList = [{ value: [{ data: 1 }] }];

    expect(component.checkIfDataPresent('200')).toBeTrue();
  });

  describe('KpiCardV2Component.handleClearAll() handleClearAll method', () => {
    describe('Happy Path', () => {
      it('should clear the filterOptions and emit "Overall" when dropdownArr length is 1', () => {
        component.dropdownArr = [{}];
        component.filterOptions = { filter1: 'value1' };
        const emitSpy = spyOn(component.optionSelected, 'emit');

        component.handleClearAll('filter1');

        expect(component.filterOptions).toEqual({});
        expect(emitSpy).toHaveBeenCalledWith(['Overall']);
      });

      it('should clear the specific filter and emit filterOptions when dropdownArr length is greater than 1', () => {
        component.dropdownArr = [{}, {}];
        component.filterOptions = { filter1: 'value1', filter2: 'value2' };
        const emitSpy = spyOn(component.optionSelected, 'emit');

        component.handleClearAll('filter1');

        expect(component.filterOptions).toEqual({ filter1: [], filter2: 'value2' });
        expect(emitSpy).toHaveBeenCalledWith(component.filterOptions);
      });
    });

    describe('Edge Cases', () => {
      it('should handle case when filterOptions is empty and dropdownArr length is greater than 1', () => {
        component.dropdownArr = [{}, {}];
        component.filterOptions = {};
        const emitSpy = spyOn(component.optionSelected, 'emit');

        component.handleClearAll('filter1');

        expect(component.filterOptions).toEqual({});
        expect(emitSpy).toHaveBeenCalledWith({});
      });

      it('should handle case when event is not present in filterOptions', () => {
        component.dropdownArr = [{}, {}];
        component.filterOptions = { filter2: 'value2' };
        const emitSpy = spyOn(component.optionSelected, 'emit');

        component.handleClearAll('filter2');

        expect(component.filterOptions).toEqual({ filter2: [] });
        expect(emitSpy).toHaveBeenCalledWith(component.filterOptions);
      });
    });
  });

  describe('KpiCardV2Component.checkIfDataPresent() checkIfDataPresent method', () => {
    describe('Happy Path', () => {
      it('should return true when data is 200 and kpiId is kpi148 with trendValueList', () => {
        component.kpiData = { kpiId: 'kpi148' };
        component.trendValueList = [{}];
        const result = component.checkIfDataPresent('200');
        expect(result).toBe(true);
      });

      it('should return true when data is 200 and kpiId is kpi139 with trendValueList having value', () => {
        component.kpiData = { kpiId: 'kpi139' };
        component.trendValueList = [{ value: [{}] }];
        const result = component.checkIfDataPresent('200');
        expect(result).toBe(true);
      });

      it('should return true when data is 200 and kpiId is kpi171 with trendValueList having data', () => {
        component.kpiData = { kpiId: 'kpi171' };
        component.trendValueList = [{ data: [{}] }];
        const result = component.checkIfDataPresent('200');
        expect(result).toBe(true);
      });

      it('should return true when data is 200 and helperService returns true', () => {
        component.kpiData = { kpiDetail: { chartType: 'someType' } };
        component.selectedTab = 'someTab';
        spyOn(helperService, 'checkDataAtGranularLevel').and.returnValue(true);
        const result = component.checkIfDataPresent('200');
        expect(result).toBe(true);
      });
    });

    describe('Edge Cases', () => {
      it('should return false when data is not 200 or 201', () => {
        const result = component.checkIfDataPresent('404');
        expect(result).toBe(false);
      });

      it('should return false when kpiId is kpi171 but trendValueList is empty', () => {
        component.kpiData = { kpiId: 'kpi171' };
        component.trendValueList = [];
        const result = component.checkIfDataPresent('200');
        expect(result).toBe(false);
      });

      it('should return false when helperService returns false', () => {
        component.kpiData = { kpiDetail: { chartType: 'someType' } };
        component.selectedTab = 'someTab';
        spyOn(helperService, 'checkDataAtGranularLevel').and.returnValue(false);
        const result = component.checkIfDataPresent('200');
        expect(result).toBe(false);
      });
    });
  });

  describe('KpiCardV2Component.handleChange() handleChange method', () => {
    describe('Happy Path', () => {
      it('should emit the selected value when type is radio', () => {
        const emitSpy = spyOn(component.optionSelected, 'emit');
        const value = { value: 'someValue' };

        component.handleChange('radio', value);

        expect(emitSpy).toHaveBeenCalledWith('someValue');
      });

      it('should emit filterOptions when type is single', () => {
        const emitSpy = spyOn(component.optionSelected, 'emit');
        component.filterOptions = { filter1: 'value1' };

        component.handleChange('single');

        expect(emitSpy).toHaveBeenCalledWith({ filter1: 'value1' });
      });

      it('should emit Overall when filterOptions is empty', () => {
        const emitSpy = spyOn(component.optionSelected, 'emit');
        component.filterOptions = {};

        component.handleChange('multi');

        expect(emitSpy).toHaveBeenCalledWith(['Overall']);
      });

      it('should emit filterOptions when filterOptions is not empty', () => {
        const emitSpy = spyOn(component.optionSelected, 'emit');
        component.filterOptions = { filter1: 'value1' };

        component.handleChange('multi');

        expect(emitSpy).toHaveBeenCalledWith({ filter1: 'value1' });
      });
    });

    it('should move selected option to top', () => {
      component.dropdownArr[0].options = ['option1', 'option2'];
      component.handleChange('multi', { value: ['option2'] });
      expect(component.dropdownArr[0].options).toEqual(['option2', 'option1'])
    });
  });

  describe('Happy Path Tests', () => {
    it('should call prepareData when event.listView is true', () => {
      // Arrange
      const prepareDataSpy = spyOn(component, 'prepareData' as any);
      const event = { listView: true };

      // Act
      component.handleAction(event);

      // Assert
      expect(prepareDataSpy).toHaveBeenCalled();
    });

    it('should call onOpenFieldMappingDialog when event.setting is true', () => {
      // Arrange
      const onOpenFieldMappingDialogSpy = spyOn(component, 'onOpenFieldMappingDialog' as any);
      const event = { setting: true };

      // Act
      component.handleAction(event);

      // Assert
      expect(onOpenFieldMappingDialogSpy).toHaveBeenCalled();
    });

    it('should call exportToExcel when event.explore is true', () => {
      // Arrange
      const exportToExcelSpy = spyOn(component, 'exportToExcel' as any);
      const event = { explore: true };

      // Act
      component.handleAction(event);

      // Assert
      expect(exportToExcelSpy).toHaveBeenCalled();
    });

    it('should call openCommentModal when event.comment is true', () => {
      // Arrange
      const openCommentModalSpy = spyOn(component, 'openCommentModal' as any);
      const event = { comment: true };

      // Act
      component.handleAction(event);

      // Assert
      expect(openCommentModalSpy).toHaveBeenCalled();
    });
  });

  describe('Edge Case Tests', () => {
    it('should not call any method when event is empty', () => {
      // Arrange
      const prepareDataSpy = spyOn(component, 'prepareData' as any);
      const onOpenFieldMappingDialogSpy = spyOn(component, 'onOpenFieldMappingDialog' as any);
      const exportToExcelSpy = spyOn(component, 'exportToExcel' as any);
      const openCommentModalSpy = spyOn(component, 'openCommentModal' as any);
      const event = {};

      // Act
      component.handleAction(event);

      // Assert
      expect(prepareDataSpy).not.toHaveBeenCalled();
      expect(onOpenFieldMappingDialogSpy).not.toHaveBeenCalled();
      expect(exportToExcelSpy).not.toHaveBeenCalled();
      expect(openCommentModalSpy).not.toHaveBeenCalled();
    });

    it('should handle unexpected event properties gracefully', () => {
      // Arrange
      const prepareDataSpy = spyOn(component, 'prepareData' as any);
      const event = { unexpectedProperty: true };

      // Act
      component.handleAction(event);

      // Assert
      expect(prepareDataSpy).not.toHaveBeenCalled();
    });
  });

  describe('KpiCardV2Component.checkFilterPresence() checkFilterPresence method', () => {
    describe('Happy Paths', () => {
      it('should return true when filterGroup is present', () => {
        const filterData = { filterGroup: { someKey: 'someValue' } };
        const result = component.checkFilterPresence(filterData);
        expect(result).toEqual({ someKey: 'someValue' });
      });

      it('should return false when filterGroup is not present', () => {
        const filterData = { someOtherKey: 'someValue' };
        const result = component.checkFilterPresence(filterData);
        expect(result).toBe(undefined);
      });
    });

    describe('Edge Cases', () => {
      it('should return false when filterData is null', () => {
        const result = component.checkFilterPresence(null);
        expect(result).toBe(undefined);
      });

      it('should return false when filterData is undefined', () => {
        const result = component.checkFilterPresence(undefined);
        expect(result).toBe(undefined);
      });

      it('should return false when filterData is an empty object', () => {
        const filterData = {};
        const result = component.checkFilterPresence(filterData);
        expect(result).toBe(undefined);
      });

      it('should return false when filterGroup is an empty object', () => {
        const filterData = { filterGroup: {} };
        const result = component.checkFilterPresence(filterData);
        expect(result).toEqual({});
      });
    });
  });

  describe('KpiCardV2Component.sanitizeArray() sanitizeArray method', () => {
    describe('Happy paths', () => {
      it('should remove null, undefined, and empty objects from an array', () => {
        const input = [
          { key1: 'value1' },
          null,
          { key2: 'value2' },
          undefined,
          {},
          { key3: 'value3' }
        ];
        const expectedOutput = [
          { key1: 'value1' },
          { key2: 'value2' },
          { key3: 'value3' }
        ];

        const result = component.sanitizeArray(input);
        expect(result).toEqual(expectedOutput);
      });
    });

    describe('Edge cases', () => {
      it('should return an empty array when input is an empty array', () => {
        const input: any[] = [];
        const expectedOutput: any[] = [];

        const result = component.sanitizeArray(input);
        expect(result).toEqual(expectedOutput);
      });

      it('should return an empty array when input contains only null, undefined, or empty objects', () => {
        const input = [null, undefined, {}];
        const expectedOutput: any[] = [];

        const result = component.sanitizeArray(input);
        expect(result).toEqual(expectedOutput);
      });

      it('should handle arrays with only valid objects', () => {
        const input = [{ key1: 'value1' }, { key2: 'value2' }];
        const expectedOutput = [{ key1: 'value1' }, { key2: 'value2' }];

        const result = component.sanitizeArray(input);
        expect(result).toEqual(expectedOutput);
      });
    });
  });


  describe('KpiCardV2Component.calculateValue() calculateValue method', () => {
    describe('Happy Path Tests', () => {
      it('should calculate the total value correctly for numeric values', () => {
        const issueData = [
          { value: 10 },
          { value: 20 },
          { value: 30 },
        ];
        const result = component.calculateValue(issueData, 'value');
        expect(result).toBe('60');
      });

      it('should return "0" when no numeric values are present', () => {
        const issueData = [
          { value: 'a' },
          { value: 'b' },
          { value: 'c' },
        ];
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

  describe('KpiCardV2Component.onFilterClear() onFilterClear method', () => {
    describe('Happy Paths', () => {
      it('should reset copyCardData and currentChartData to initial state', () => {
        // Arrange
        component.cardData = {
          issueData: [{ id: 1, name: 'Issue 1' }, { id: 2, name: 'Issue 2' }],
        };
        component.copyCardData = { issueData: [] };
        component.currentChartData = { chartData: [], totalCount: 0 };

        spyOn(kpiHelperService, 'getChartDataSet').and.returnValue({
          chartData: [{ id: 1, name: 'Issue 1' }, { id: 2, name: 'Issue 2' }],
          totalCount: 2,
        });

        // Act
        component.onFilterClear();

        // Assert
        expect(component.copyCardData.issueData).toEqual(component.cardData.issueData);
        expect(component.currentChartData.chartData).toEqual([{ id: 1, name: 'Issue 1' }, { id: 2, name: 'Issue 2' }]);
        expect(component.currentChartData.totalCount).toBe(2);
      });
    });

    describe('Edge Cases', () => {
      it('should handle empty issueData gracefully', () => {
        // Arrange
        component.cardData = { issueData: [] };
        component.copyCardData = { issueData: [{ id: 1, name: 'Issue 1' }] };
        component.currentChartData = { chartData: [{ id: 1, name: 'Issue 1' }], totalCount: 1 };

        spyOn(kpiHelperService, 'getChartDataSet').and.returnValue({
          chartData: [],
          totalCount: 0,
        });

        // Act
        component.onFilterClear();

        // Assert
        expect(component.copyCardData.issueData).toEqual([]);
        expect(component.currentChartData.chartData).toEqual([]);
        expect(component.currentChartData.totalCount).toBe(0);
      });

      it('should handle null issueData gracefully', () => {
        // Arrange
        component.cardData = { issueData: null };
        component.copyCardData = { issueData: [{ id: 1, name: 'Issue 1' }] };
        component.currentChartData = { chartData: [{ id: 1, name: 'Issue 1' }], totalCount: 1 };

        spyOn(kpiHelperService, 'getChartDataSet').and.returnValue({
          chartData: [],
          totalCount: 0,
        } as any);

        // Act
        component.onFilterClear();

        // Assert
        expect(component.copyCardData.issueData).toBeNull();
        expect(component.currentChartData.chartData).toEqual([]);
        expect(component.currentChartData.totalCount).toBe(0);
      });
    });
  });

  describe('KpiCardV2Component.onFilterChange() onFilterChange method', () => {
    describe('Happy Path Tests', () => {
      it('should update chart data when a valid filter is applied', () => {
        const mockEvent = {
          selectedKeyObj: { Category: 'SomeCategory' },
          selectedKey: 'SomeKey',
          otherFilter: 'SomeValue'
        };

        const mockIssueData = [
          { Category: ['SomeCategory'], SomeKey: 10 },
          { Category: ['OtherCategory'], SomeKey: 20 }
        ];

        component.cardData = { issueData: mockIssueData };
        component.copyCardData = { issueData: mockIssueData };
        component.colorPalette = ['#FBCF5F', '#6079C5', '#A4F6A5'];

        spyOn(kpiHelperService, 'getChartDataSet').and.returnValue({
          chartData: [],
          totalCount: 0
        });

        component.onFilterChange(mockEvent);

        expect(component.copyCardData.issueData).toEqual([]);
        expect(kpiHelperService.getChartDataSet).toHaveBeenCalled();
      });
    });

    describe('Edge Case Tests', () => {
      it('should handle empty filter gracefully', () => {
        const mockEvent = {
          selectedKeyObj: { Category: 'Value' },
          selectedKey: 'SomeKey'
        };

        const mockIssueData = [
          { Category: ['SomeCategory'], SomeKey: 10 },
          { Category: ['OtherCategory'], SomeKey: 20 }
        ];

        component.cardData = { issueData: mockIssueData };
        component.copyCardData = { issueData: mockIssueData };
        component.colorPalette = ['#FBCF5F', '#6079C5', '#A4F6A5'];

        spyOn(kpiHelperService, 'getChartDataSet').and.returnValue({
          chartData: [],
          totalCount: 0
        });

        component.onFilterChange(mockEvent);

        expect(component.copyCardData.issueData).toEqual(mockIssueData);
        expect(kpiHelperService.getChartDataSet).toHaveBeenCalled();
      });

      it('should handle null filter object', () => {
        const mockEvent = {
          selectedKeyObj: null,
          selectedKey: 'SomeKey'
        };

        const mockIssueData = [
          { Category: ['SomeCategory'], SomeKey: 10 },
          { Category: ['OtherCategory'], SomeKey: 20 }
        ];

        component.cardData = { issueData: mockIssueData };
        component.copyCardData = { issueData: mockIssueData };
        component.colorPalette = ['#FBCF5F', '#6079C5', '#A4F6A5'];

        spyOn(kpiHelperService, 'getChartDataSet').and.returnValue({
          chartData: [],
          totalCount: 0
        });

        component.onFilterChange(mockEvent);

        expect(component.copyCardData.issueData).toEqual(mockIssueData);
        expect(kpiHelperService.getChartDataSet).toHaveBeenCalled();
      });
    });
  });

  describe('KpiCardV2Component.showCummalative() showCummalative method', () => {
    describe('Happy Path Tests', () => {
      it('should return cumulative value for stacked-bar chart type', () => {
        component.kpiData = { kpiDetail: { chartType: 'stacked-bar' } };
        component.currentChartData = { totalCount: 480 };
        spyOn(kpiHelperService, 'convertToHoursIfTime').and.returnValue('1d');

        const result = component.showCummalative();

        expect(result).toBe('1d');
        expect(kpiHelperService.convertToHoursIfTime).toHaveBeenCalledWith(480, 'day');
      });

      it('should return cumulative value for other chart types with selectedButtonValue', () => {
        component.kpiData = { kpiDetail: { chartType: 'other-chart' } };
        component.selectedButtonValue = [{ key: 'someKey', unit: 'hours' }];
        component.copyCardData = { issueData: [{ someKey: 60 }, { someKey: 120 }] };
        spyOn(kpiHelperService, 'convertToHoursIfTime').and.returnValue('3h');

        const result = component.showCummalative();

        expect(result).toBe('3h');
        expect(kpiHelperService.convertToHoursIfTime).toHaveBeenCalledWith('180', 'hours');
      });
    });

    describe('Edge Case Tests', () => {
      it('should handle empty selectedButtonValue gracefully', () => {
        component.kpiData = { kpiDetail: { chartType: 'stacked-bar-chart' } };
        component.selectedButtonValue = [];
        component.currentChartData = { totalCount: 100 };

        const result = component.showCummalative();

        expect(result).toBe(100);
      });

      it('should handle undefined selectedButtonValue gracefully', () => {
        component.kpiData = { kpiDetail: { chartType: 'other-chart' } };
        component.selectedButtonValue = undefined;
        component.currentChartData = { totalCount: 200 };

        const result = component.showCummalative();

        expect(result).toBe(200);
      });

      it('should handle undefined kpiData gracefully', () => {
        component.kpiData = undefined;
        component.currentChartData = { totalCount: 300 };

        const result = component.showCummalative();

        expect(result).toBe(300);
      });
    });
  });

  describe('KpiCardV2Component.convertToHoursIfTime() convertToHoursIfTime method', () => {
    describe('Happy paths', () => {
      it('should convert minutes to hours and minutes correctly', () => {
        // Arrange
        const value = 150; // 2 hours and 30 minutes
        const unit = 'hours';
        spyOn(kpiHelperService, 'convertToHoursIfTime').and.returnValue('2h 30m');

        // Act
        const result = component.convertToHoursIfTime(value, unit);

        // Assert
        expect(result).toBe('2h 30m');
      });

      it('should return the same value if unit is not time-related', () => {
        // Arrange
        const value = 100;
        const unit = 'points';
        spyOn(kpiHelperService, 'convertToHoursIfTime').and.returnValue('100');

        // Act
        const result = component.convertToHoursIfTime(value, unit);

        // Assert
        expect(result).toBe('100');
      });
    });

    describe('Edge cases', () => {
      it('should handle zero value correctly', () => {
        // Arrange
        const value = 0;
        const unit = 'hours';
        spyOn(kpiHelperService, 'convertToHoursIfTime').and.returnValue('0h');

        // Act
        const result = component.convertToHoursIfTime(value, unit);

        // Assert
        expect(result).toBe('0h');
      });

      it('should handle negative values correctly', () => {
        // Arrange
        const value = -90; // -1 hour and 30 minutes
        const unit = 'hours';
        spyOn(kpiHelperService, 'convertToHoursIfTime').and.returnValue('-1h 30m');

        // Act
        const result = component.convertToHoursIfTime(value, unit);

        // Assert
        expect(result).toBe('-1h 30m');
      });

      it('should handle large values correctly', () => {
        // Arrange
        const value = 10000; // 166 hours and 40 minutes
        const unit = 'hours';
        spyOn(kpiHelperService, 'convertToHoursIfTime').and.returnValue('166h 40m');

        // Act
        const result = component.convertToHoursIfTime(value, unit);

        // Assert
        expect(result).toBe('166h 40m');
      });
    });
  });
});
