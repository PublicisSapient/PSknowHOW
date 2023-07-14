import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';

import { KpiCardComponent } from './kpi-card.component';
import { SharedService } from 'src/app/services/shared.service';

describe('KpiCardComponent', () => {
  let component: KpiCardComponent;
  let fixture: ComponentFixture<KpiCardComponent>;
  let sharedService: SharedService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      declarations: [ KpiCardComponent ],
      providers: [SharedService]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(KpiCardComponent);
    component = fixture.componentInstance;
    sharedService = TestBed.inject(SharedService);
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

    const response = { kpi3: ['default'] };
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

});
