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
  let mockService: jasmine.SpyObj<SharedService>;
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
    mockService = jasmine.createSpyObj(SharedService, ['selectedFilterOptionObs', 'getSelectedTab']);

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

  describe('checkIfDataPresent', () => {
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

  describe('checkDataAtGranularLevel', () => {
    it('should return true if data is an array with non-empty data arrays', () => {
      const data = [
        { data: [1, 2, 3] },
        { data: [4, 5, 6] },
      ];

      const result = component.checkDataAtGranularLevel(data);

      expect(result).toBe(true);
    });

    it('should return true if data is an array with non-empty value arrays or non-empty objects', () => {
      const data = [
        { value: [1, 2, 3] },
        { value: { prop: 'value' } },
      ];

      const result = component.checkDataAtGranularLevel(data);

      expect(result).toBe(false);
    });

    it('should return true if data is an array with non-empty dataGroup arrays', () => {
      const data = [
        { dataGroup: [1, 2, 3] },
        { dataGroup: [4, 5, 6] },
      ];

      const result = component.checkDataAtGranularLevel(data);

      expect(result).toBe(true);
    });

    it('should return true if data is an object with non-zero number of keys', () => {
      const data = {
        key1: 'value1',
        key2: 'value2',
      };

      const result = component.checkDataAtGranularLevel(data);

      expect(result).toBe(false);
    });

    it('should return false if data is an empty array', () => {
      const data = [];

      const result = component.checkDataAtGranularLevel(data);

      expect(result).toBe(false);
    });

    it('should return false if data is an empty object', () => {
      const data = {};

      const result = component.checkDataAtGranularLevel(data);

      expect(result).toBe(false);
    });

    it('should return false if data is not an array or object', () => {
      const data = 'invalid data';

      const result = component.checkDataAtGranularLevel(data);

      expect(result).toBe(true);
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
  }));

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

  it('should handle Overall values correctly for kpi72', () => {
    const filterData = {
      kpi72: {
        filter2: ['Overall'],
        filter3: ['Other'],
        filter4: ['OtherFilters']
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
    // expect(component.filterOptions["filter1"]).toEqual('Overall');
    expect(component.filterOptions["filter2"]).toEqual('Overall');
  });


  it('should handle Overall values in filter1 correctly for kpi72', () => {
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

  it('should return true if data is present for kpiId kpi139 or kpi127 and trendValueList and trendValueList[0].value have length', () => {
    component.kpiData = { kpiId: 'kpi139' };
    component.trendValueList = [{ value: [{ value: [1, 2, 3] }] }];

    expect(component.checkIfDataPresent('200')).toBeTrue();
  });

  it('should return true if data is present for kpiId kpi168, kpi70 or kpi153 and trendValueList and trendValueList[0].value have length greater than 0', () => {
    component.kpiData = { kpiId: 'kpi168' };
    component.trendValueList = [{ value: [{ data: 1 }] }];

    expect(component.checkIfDataPresent('200')).toBeTrue();
  });

  it('should return true if data is present at granular level and selectedTab is "developer"', () => {
    component.selectedTab = 'developer';
    component.trendValueList = [{ data: 1 }];

    expect(component.checkDataAtGranularLevel(component.trendValueList)).toBeTrue();
  });

  it('should return false if data is not present at granular level and selectedTab is not "developer"', () => {
    component.selectedTab = 'other';
    component.trendValueList = [];

    expect(component.checkDataAtGranularLevel(component.trendValueList)).toBeFalse();
  });
});
