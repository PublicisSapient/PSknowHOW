import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';

import { KpiCardComponent } from './kpi-card.component';
import { SharedService } from 'src/app/services/shared.service';
import { HttpService } from 'src/app/services/http.service';
import { AppConfig, APP_CONFIG } from 'src/app/services/app.config';
import { of } from 'rxjs';


describe('KpiCardComponent', () => {
  let component: KpiCardComponent;
  let fixture: ComponentFixture<KpiCardComponent>;
  let sharedService: SharedService;
  let httpService: HttpService;
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
      providers: [SharedService,HttpService,
        { provide: APP_CONFIG, useValue: AppConfig }]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(KpiCardComponent);
    component = fixture.componentInstance;
    sharedService = TestBed.inject(SharedService);
    httpService = TestBed.inject(HttpService);
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
    spyOn(httpService,'getFieldMappings').and.returnValue(of({success: true, data: fakeSelectedFieldMapping}));
    component.getFieldMapping();
    expect(Object.keys(component.selectedFieldMapping).length).toBeGreaterThan(0);
  })

  it('should get getFieldMappingMetaData',()=>{
    component.selectedToolConfig = [{id : '123'}];
    spyOn(httpService,'getKPIConfigMetadata').and.returnValue(of(dropDownMetaData));
    component.getFieldMappingMetaData('jira');
    expect(component.fieldMappingMetaData).not.toBeNull();
  });

});
