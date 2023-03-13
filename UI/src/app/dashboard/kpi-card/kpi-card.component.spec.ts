import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { of } from 'rxjs';

import { KpiCardComponent } from './kpi-card.component';
import { SharedService } from 'src/app/services/shared.service';
import { HttpService } from 'src/app/services/http.service';
import { AppConfig, APP_CONFIG } from 'src/app/services/app.config';

describe('KpiCardComponent', () => {
  let component: KpiCardComponent;
  let fixture: ComponentFixture<KpiCardComponent>;
  let sharedService: SharedService;
  // let httpMock;
  let http_service: HttpService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      declarations: [ KpiCardComponent ],
      providers: [SharedService, HttpService, { provide: APP_CONFIG, useValue: AppConfig }]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(KpiCardComponent);
    component = fixture.componentInstance;
    sharedService = TestBed.inject(SharedService);
    http_service = TestBed.inject(HttpService);
    // httpMock = TestBed.inject(HttpTestingController);
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

  it('should get comments', () => {
    const response = {
      message: "Found comments",
      success: true,
      data: {
        node: "DOTC_63b51633f33fd2360e9e72bd",
        sprintId: "",
        kpiId: "kpi118",
        CommentsInfo: [{
          commentId: "43514629-78a0-4a3c-bf87-82f89c036f04",
          commentBy: "SUPERADMIN",
          commentOn: "2023-03-13 13:23:34",
          comment: "test1"
        }]
      }
    }
    spyOn(http_service, 'getComment').and.returnValue(of(response));
    component.getComments();
    fixture.detectChanges();
    expect(component.commentsList).toEqual(response.data.CommentsInfo);
  });

  it('should submit comments', () => {
    const response = {
      message: "Your Comment has been submitted successfully ",
      success: true,
      data: {
        node: "DOTC_63b51633f33fd2360e9e72bd",
        level: "4",
        sprintId: "",
        commentKpiWise: [{
          kpiId: "kpi14",
          commentInfo: [{
            commentId: "6138f970-1243-4470-b8fb-781787e5713c",
            commentBy: "SUPERADMIN",
            commentOn: "2023-03-13 14:03:33",
            comment: "test 1"
          }]
        }]
      }
    }
    spyOn(http_service, 'submitComment').and.returnValue(of(response));
    component.submitComment({nodeId: '', level : ''});
    fixture.detectChanges();
    expect(component.commentText).toBe('');
  });

  it('should open comments', () => {
    const sharedObj = {
      "filterData": [
          {
              "nodeId": "DOTC_63b51633f33fd2360e9e72bd",
              "nodeName": "DOTC",
              "path": [
                  "D3_hierarchyLevelThree###D2_hierarchyLevelTwo###D1_hierarchyLevelOne"
              ],
              "labelName": "project",
              "parentId": [
                  "D3_hierarchyLevelThree"
              ],
              "level": 4,
              "basicProjectConfigId": "63b51633f33fd2360e9e72bd"
          }
      ],
      "filterApplyData": {
          "ids": [
              "DOTC_63b51633f33fd2360e9e72bd"
          ],
          "selectedMap": {
              "project": [
                  "DOTC_63b51633f33fd2360e9e72bd"
              ],
              "sprint": [],
              "afOne": []
          },
          "level": 4
      }
  }
  spyOn(sharedService, 'getFilterObject').and.returnValue(sharedObj);
  component.openComments();
  fixture.detectChanges();
  expect(component.selectedFilters).toEqual(sharedObj.filterData);
  });
});
