import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';

import { KpiCardComponent } from './kpi-card.component';
import { SharedService } from 'src/app/services/shared.service';
import { HttpService } from 'src/app/services/http.service';
import { AppConfig, APP_CONFIG } from 'src/app/services/app.config';
import { of } from 'rxjs';
import { SimpleChanges } from '@angular/core';
import { GetAuthorizationService } from 'src/app/services/get-authorization.service';


describe('KpiCardComponent', () => {
  let component: KpiCardComponent;
  let fixture: ComponentFixture<KpiCardComponent>;
  let sharedService: SharedService;
  let httpService: HttpService;
  let authService: GetAuthorizationService
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
      imports: [HttpClientTestingModule],
      declarations: [ KpiCardComponent ],
      providers: [SharedService,HttpService,GetAuthorizationService,
        { provide: APP_CONFIG, useValue: AppConfig }]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(KpiCardComponent);
    component = fixture.componentInstance;
    sharedService = TestBed.inject(SharedService);
    httpService = TestBed.inject(HttpService);
    authService = TestBed.inject(GetAuthorizationService);
    mockService = jasmine.createSpyObj(SharedService, ['selectedFilterOptionObs', 'getSelectedTab']);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should showFilterTooltip',()=>{
    component.filterOptions ={
      1:['Overall','P1']
    };
    component.showFilterTooltip(true,1);
    expect(component.filterMultiSelectOptionsData['details'][1].length).toEqual( component.filterOptions[1].length);
    component.showFilterTooltip(false);
    expect(Object.keys(component.filterMultiSelectOptionsData).length).toEqual(0);
  });

  it('should handle clear all filter',()=>{
    component.filterOptions={
      filter1:[],
      filter2:[]
    };
    component.handleClearAll('filter1');
    expect(component.filterOptions.hasOwnProperty('filter1')).toBeFalsy();
  });

  it('should return color based on nodename',()=>{
    component.trendBoxColorObj ={
      C1_corporate: {
          nodeName: 'C1',
          color: '#079FFF'
      },
      C1: {
          nodeName: 'C1',
          color: '#079FFF'
      }
  };
  expect(component.getColor('C1')).toBe('#079FFF');
  });

  it('should handle filter change for radio',()=>{
    const spy = spyOn(component.optionSelected,'emit');
    component.handleChange('radio','Story Points');
    expect(spy).toHaveBeenCalledWith('Story Points');
  });

  it('should handle filter change for single select',()=>{
    const filterOptionsingle = {
      filter1: [{}]
    };
    component.filterOptions ={};
    const spy = spyOn(component.optionSelected,'emit');
    component.handleChange('single',undefined);
    expect(spy).toHaveBeenCalled();
  });

  it('should handle filter change for multi select',()=>{
    const filterOptionMulti ={filter1 : ['P1','P2']};
    component.filterOptions = filterOptionMulti;
    const spy = spyOn(component.optionSelected,'emit');
    component.handleChange('multi',undefined);
    expect(spy).toHaveBeenCalledWith(filterOptionMulti);
  });

  it('should show tooltip',()=>{
    component.showTooltip(true);
    expect(component.isTooltip).toBeTrue();
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

    const response = { kpi3: ['default'],action : "update" };
    sharedService.setKpiSubFilterObj(response);
    component.ngOnInit();
    tick();
    expect(component.radioOption).toBe('default');
  }));

  it('should prepare hover sprint data',()=>{
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
    component.colors = [
      {
        nodeName : "p1",
        color : 'red'
      },
      {
        nodeName : "p2",
        color : 're2'
      }
    ];
     component.trendValueList = [
      {
        data : 'p1',
        value : [
          {
            data : "53.55",
            sSprintName : "SBR.COM_Sprint 23.05_DOTC",
            value : "53.55",
            hoverValue : {
              Defects : 8,
              Stories : 15
            }
          },
          {
            data : "55.55",
            sSprintName : "SBR.COM_Sprint 23.05_DOTC",
            value : "53.55",
            hoverValue : {
              Defects : 8,
              Stories : 15
            }
          }
        ]
      }
     ]
     component.prepareData()
     expect(component.projectList.length).toBeGreaterThan(0);
  })

  it("should get css class",()=>{
    component.colorCssClassArray = ['sprint-hover-project1','sprint-hover-project2','sprint-hover-project3','sprint-hover-project4','sprint-hover-project5','sprint-hover-project6'];
    const rValue = component.getColorCssClasses(0);
    expect(rValue).toBe('sprint-hover-project1');
  })

  it('should check if hoverValues are not available',()=>{
    component.sprintDetailsList = [
      {
        project : 'p1',
        value : "20",
        hoverList : []
      }];
      const rValue = component.hasData('params')
      expect(rValue).toBeFalse();
    
  })

  it('should get Mapping configuration',()=>{
    component.kpiData = {
      kpiId: 'kpi3',
      kpiDetail: {
        kpiId: 'kpi3',
        kpiSource: 'Jira',
        combinedKpiSource :'Jira/Azure'
      }
    };
    spyOn(sharedService,'getSelectedTab').and.returnValue('My Dashboard');
    spyOn(sharedService,'getSelectedType').and.returnValue('scrum');
    spyOn(sharedService,'getSelectedTrends').and.returnValue([{basicProjectConfigId : '123'}]);
    spyOn(httpService,'getKPIFieldMappingConfig').and.returnValue(of(fakeKpiFieldMappingList));
    
    const fakeMetaDataList = [
      {
        projectID: '123',
        kpiSource: 'jira',
        metaData: dropDownMetaData.data
      }
    ]
    spyOn(sharedService,'getFieldMappingMetaData').and.returnValue(fakeMetaDataList);
    component.getKPIFieldMappingConfig();
    expect(component.fieldMappingConfig.length).toEqual(fakeKpiFieldMappingList.data.fieldConfiguration.length);
  })

  it('should get FieldMapping',()=>{
    component.selectedToolConfig = [{id : '123'}];
    component.kpiData = {
      kpiId : 'pi123'
    }
    spyOn(httpService,'getFieldMappingsWithHistory').and.returnValue(of({success: true, data: {
      fieldMappingResponses : fakeSelectedFieldMapping,
      metaTemplateCode : '10'
    }}));
    component.getFieldMapping();
    expect(Object.keys(component.selectedFieldMapping).length).toBeGreaterThan(0);
  })

  it('should get getFieldMappingMetaData',()=>{
    component.selectedToolConfig = [{id : '123'}];
    spyOn(httpService,'getKPIConfigMetadata').and.returnValue(of(dropDownMetaData));
    component.getFieldMappingMetaData('jira');
    expect(component.fieldMappingMetaData).not.toBeNull();
  });

  it('should prepare data when project is selected', () => {
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
    component.colors = [
      {
        nodeName : "p1",
        color : 'red'
      },
      {
        nodeName : "p2",
        color : 're2'
      }
    ];
    component.trendValueList = [
      {
        "data": "p1",
        "value": [
          {
              "sSprintID": "P1 PI-12",
              "sSprintName": "P1 PI-12",
              "dataValue": [
                  {
                      "name": "Achieved Value",
                      "lineType": "solid",
                      "data": "116.0",
                      "value": 64.67,
                      "hoverValue": {}
                  },
                  {
                      "name": "Planned Value",
                      "lineType": "dotted",
                      "data": "116.0",
                      "value": 116,
                      "hoverValue": {}
                  }
              ],
              "sprojectName": "p1"
          },
        ]
      },{
        "data": "p2",
        "value": [
          {
              "sSprintID": "P2 PI-12",
              "sSprintName": "P2 PI-12",
              "dataValue": [
                  {
                      "name": "Achieved Value",
                      "lineType": "solid",
                      "data": "116.0",
                      "value": 64.67,
                      "hoverValue": {}
                  },
                  {
                      "name": "Planned Value",
                      "lineType": "dotted",
                      "data": "116.0",
                      "value": 116,
                      "hoverValue": {}
                  }
              ],
              "sprojectName": "p2"
          },
        ]
      }
    ];
    const len = component.trendValueList[0]?.value[0]?.dataValue?.length + 1;
    component.columnList = [];
    component.prepareData();
    expect(component.columnList.length).toBeGreaterThan(1);
  })

  it('should reload kpi', () => {
    component.displayConfigModel = true;
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
    const emitSpy = spyOn(component.reloadKPITab, 'emit');
    component.reloadKPI();
    expect(component.displayConfigModel).toBeFalse();
    expect(emitSpy).toHaveBeenCalledWith(component.kpiData);
  })

  it('should handle get count', () => {
    const event = 'kpi120';
    const emitSpy = spyOn(component.getCommentCountByKpi, 'emit');
    component.handleGetCount(event);
    expect(emitSpy).toHaveBeenCalledWith(event);
  })

  it('should handle kpi click', () => {
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
    const spy = spyOn(component, 'triggerGaEvent');
    component.handleKpiClick();
    expect(spy).toHaveBeenCalled();
  })

  it('should emit downloadExcel event when exportToExcel is called', () => {
    spyOn(component.downloadExcel, 'emit');
    component.exportToExcel();
    expect(component.downloadExcel.emit).toHaveBeenCalledWith(true);
  });
  it('should call getKPIFieldMappingConfig when onOpenFieldMappingDialog is called', () => {
    spyOn(component, 'getKPIFieldMappingConfig');
    component.onOpenFieldMappingDialog();
    expect(component.getKPIFieldMappingConfig).toHaveBeenCalled();
  });

  it('should set loading to false and noData to true when fieldMappingConfig length is 0', () => {
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
        combinedKpiSource : 'Jira/Azure'
      },
      shown: true
    };
    // Arrange
    const selectedTab = 'release';
    const selectedType = 'scrum';
    const selectedTrend = [{ basicProjectConfigId: '123' }];
    const fieldMappingConfig = [];
    spyOn(sharedService, 'getSelectedTab').and.returnValue(selectedTab);
    spyOn(sharedService, 'getSelectedType').and.returnValue(selectedType);
    spyOn(sharedService, 'getSelectedTrends').and.returnValue(selectedTrend);
    spyOn(httpService, 'getKPIFieldMappingConfig').and.returnValue(of({ success: true, data: { fieldConfiguration: fieldMappingConfig } }));

    // Act
    component.getKPIFieldMappingConfig();

    // Assert
    expect(component.loading).toBeFalse();
    expect(component.noData).toBeTrue();
  });

  it('should handle no data for KPI field mapping config', fakeAsync(() => {
    // Arrange
    component.kpiData = {
     kpiId: 'kpi3',
      kpiDetail: {
        id: '633ed17f2c2d5abef2451ff0',
        kpiId: 'kpi3',
        kpiName: 'Lead Time',
        kpiSource: 'Jira',
        combinedKpiSource : 'Jira/Azure'
      },
      shown: true
    };
    const selectedTab = 'my dashboard';
    const selectedType = 'scrum';
    const selectedTrend = [{ basicProjectConfigId: '123' }];
    spyOn(sharedService, 'getSelectedTab').and.returnValue(selectedTab);
    spyOn(sharedService, 'getSelectedType').and.returnValue(selectedType);
    spyOn(sharedService, 'getSelectedTrends').and.returnValue(selectedTrend);
    spyOn(httpService, 'getKPIFieldMappingConfig').and.returnValue(of({ success: true, data: { fieldConfiguration: [], kpiSource: 'jira', projectToolConfigId: '456' } }));
  
    // Act
    component.getKPIFieldMappingConfig();
    tick();
  
    // Assert
    expect(httpService.getKPIFieldMappingConfig).toHaveBeenCalledWith(`${selectedTrend[0]?.basicProjectConfigId}/${component.kpiData?.kpiId}`);
    expect(component.loading).toBeFalse();
    expect(component.noData).toBeTrue();
    expect(component.displayConfigModel).toBeTrue();
    expect(component.fieldMappingConfig).toEqual([]);
    expect(component.selectedToolConfig).toEqual([{ id: '456', toolName: 'jira' }]);
  }));

  it('should handle no field mapping config data', () => {
    const selectedTab = 'my dashboard';
    const selectedType = 'scrum';
    const selectedTrend = [{ basicProjectConfigId: '123' }];
    const fakeData = {
      success: true,
      data: {
        fieldConfiguration: [],
        kpiSource: 'jira',
        projectToolConfigId: '456'
      }
    };
    component.kpiData = {
      kpiId: 'kpiId',
      kpiDetail : {
        combinedKpiSource : 'Jira/Azure',
        kpiSource : 'Jira'
      }
      
    }
    component.selectedToolConfig = [];
    component.selectedConfig = {};
    spyOn(sharedService, 'getSelectedTab').and.returnValue(selectedTab);
    spyOn(sharedService, 'getSelectedType').and.returnValue(selectedType);
    spyOn(sharedService, 'getSelectedTrends').and.returnValue(selectedTrend);
    spyOn(httpService, 'getKPIFieldMappingConfig').and.returnValue(of(fakeData));
  
    component.getKPIFieldMappingConfig();
  
    expect(sharedService.getSelectedTab).toHaveBeenCalled();
    expect(sharedService.getSelectedType).toHaveBeenCalled();
    expect(sharedService.getSelectedTrends).toHaveBeenCalled();
    expect(httpService.getKPIFieldMappingConfig).toHaveBeenCalledWith('123/kpiId');
    expect(component.loading).toBeFalse();
    expect(component.noData).toBeTrue();
    expect(component.displayConfigModel).toBeTrue();
    expect(component.fieldMappingConfig).toEqual([]);
    expect(component.selectedToolConfig).toEqual([{ id: '456', toolName: 'jira' }]);
    // expect(component.selectedConfig).toBeUndefined();
    expect(component.fieldMappingMetaData).toEqual([]);
  });

  it('should handle error on getting field mapping', () => {
    component.selectedToolConfig = [
      {
        id: 'xxxxxxxxxxxxxxx'
      }
    ];
    const errResponse = {
      success: false,
      error: 'Something went wrong'
    }
    component.kpiData = {
      kpiId : 'kpi123'
    }
    component.loading = true
    spyOn(httpService, 'getFieldMappingsWithHistory').and.returnValue(of(errResponse));
    component.getFieldMapping();
    expect(component.loading).toBeFalse();
  })

  it('should handle error on getting field mapping meta data', () => {
    component.selectedToolConfig = [
      {
        id: 'xxxxxxxxxxxxxxx'
      }
    ];
    const errResponse = {
      error: "Something went wrong",
      success: false
    }
    component.kpiData = {
      kpiId : 'kpi123'
    }
    component.fieldMappingMetaData = [];
    spyOn(httpService, 'getKPIConfigMetadata').and.returnValue(of(errResponse))
    component.getFieldMappingMetaData('jira');
    expect(component.fieldMappingMetaData).toEqual([]);
  })
  
  it('should update userRole and checkIfViewer on ngOnChanges', () => {
    // Arrange
    const changes: SimpleChanges = {
      dropdownArr: {
        currentValue: ['option1', 'option2'],
        previousValue: ['option1'],
        firstChange: false,
        isFirstChange: () => false
      }
    };
    spyOn(authService, 'getRole').and.returnValue('projectAdmin');
    spyOn(authService, 'checkIfViewer').and.returnValue(true);
    spyOn(sharedService, 'getSelectedTrends').and.returnValue([{ basicProjectConfigId: '123' }]);
  
    // Act
    component.ngOnChanges(changes);
  
    // Assert
    expect(authService.getRole).toHaveBeenCalled();
    expect(authService.checkIfViewer).toHaveBeenCalledWith({ id: '123' });
    expect(component.userRole).toBe('projectAdmin');
    expect(component.checkIfViewer).toBe(true);
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
  });

  it('should handle other key values in filter1 correctly for kpi72', () => {
    const filterData = {
      kpi72: {
        filter3: ['option3'],
        filter2: ['Overall']
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
  });

  it('should handle Overall values in filter1 correctly for kpi72', () => {
    const filterData = {
      kpi72: {
        filter: ['OtherFilters','Overall'],
        filter1: ['Overall'],
        filter2: ['Other'],
        
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
  });

  it('should handle Overall values in filter1 correctly for non kpi72', () => {
    const filterData = {
      kpi7: {
        filter: ['OtherFilters','Overall'],
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

  it('should get getColorCssClasses',()=>{
    const rValue = component.getColorCssClasses(0);
    expect(rValue).toBeDefined();
  })


describe('hasData', () => {
  beforeEach(() => {
    component.sprintDetailsList = [
      { hoverList: [{ field1: null, field2: 'data' }] },
      { hoverList: [{ field1: undefined, field2: 'data' }] },
      { hoverList: [{ field1: 'data', field2: null }] },
    ];
  });

  it('should return true if the field is not null or undefined in the hoverList of the selected tab', () => {
    component.selectedTabIndex = 0;
    expect(component.hasData('field2')).toBe(true);
  });

  it('should return false if the field is null in the hoverList of the selected tab', () => {
    component.selectedTabIndex = 0;
    expect(component.hasData('field1')).toBe(false);
  });

  it('should return false if the field is undefined in the hoverList of the selected tab', () => {
    component.selectedTabIndex = 1;
    expect(component.hasData('field1')).toBe(false);
  });

  it('should return true if the field is not null or undefined in the hoverList of the selected tab', () => {
    component.selectedTabIndex = 2;
    expect(component.hasData('field1')).toBe(true);
  });

  it('should handle out of bounds selectedTabIndex gracefully', () => {
    component.selectedTabIndex = 3;  // Out of bounds
    expect(() => component.hasData('field1')).toThrow();
  });

});

it("should return execution date of processor",()=>{
  const tracelog = [{
    processorName : 'Jira',
    executionSuccess : false,
    executionEndedAt : '2023-01-04T06:02:20'
  }]
  spyOn(component,'findTraceLogForTool').and.returnValue(tracelog);
  const resp = component.showExecutionDate('Jira')
  expect(resp).not.toBe("NA")
})

it('should find tracelog for specfic tool',()=>{
  spyOn(sharedService,'getProcessorLogDetails').and.returnValue([{
    processorName : 'jira',
    executionSuccess : false,
    executionEndedAt : '2023-01-04T06:02:20'
  }])
  const toolDetails = component.findTraceLogForTool("jira");
  expect(toolDetails).toBeDefined();
})

});
