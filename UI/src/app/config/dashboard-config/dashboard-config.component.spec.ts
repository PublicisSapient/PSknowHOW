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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

import { DashboardconfigComponent } from './dashboard-config.component';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { CommonModule } from '@angular/common';
import { InputSwitchModule } from 'primeng/inputswitch';
import { FormControl, FormsModule, ReactiveFormsModule, UntypedFormGroup } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { SharedService } from '../../services/shared.service';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpService } from '../../services/http.service';
import { environment } from 'src/environments/environment';
import { APP_CONFIG, AppConfig } from '../../services/app.config';
import { MessageService } from 'primeng/api';
import { DropdownModule } from 'primeng/dropdown';
import { GetAuthorizationService } from 'src/app/services/get-authorization.service';
import { of, throwError } from 'rxjs';


describe('DashboardconfigComponent', () => {
  let component: DashboardconfigComponent;
  let fixture: ComponentFixture<DashboardconfigComponent>;
  const baseUrl = environment.baseUrl;
  let service;
  let httpMock;
  let httpService;
  let messageService;
  let getAuthorizationService;

  const fakeGetDashData = require('../../../test/resource/fakeShowHideApi.json');
  let fakeGetDashDataOthers = fakeGetDashData.data['scrum'][0].kpis.concat(fakeGetDashData.data['kanban'][0].kpis).concat(fakeGetDashData.data['others'][0].kpis);
  const fakeProjects = require('../../../test/resource/fakeProjectsDashConfig.json');
  fakeGetDashDataOthers = fakeGetDashDataOthers.filter((kpi) => kpi.shown);
  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        FormsModule,
        InputSwitchModule,
        ReactiveFormsModule,
        CommonModule,
        RouterTestingModule.withRoutes([]),
        HttpClientTestingModule,
        DropdownModule
      ],
      declarations: [DashboardconfigComponent],
      providers: [SharedService, HttpService, MessageService
        , { provide: APP_CONFIG, useValue: AppConfig }

      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    service = TestBed.inject(SharedService);
    fixture = TestBed.createComponent(DashboardconfigComponent);
    component = fixture.componentInstance;
    httpService = TestBed.inject(HttpService);
    messageService = TestBed.inject(MessageService);
    httpMock = TestBed.inject(HttpTestingController);
   getAuthorizationService = TestBed.inject(GetAuthorizationService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should get dashboard data ', waitForAsync(() => {
    component.getKpisData('pid');
    const httpreq = httpMock.expectOne(baseUrl + '/api/user-board-config/pid');
    component.userProjects = [{name : "p1",id:'id1',type : 'common'}]
    httpreq.flush(fakeGetDashData);
    expect(Object.keys(component.kpiFormValue['kpis']['controls']).length + 4).toBe(fakeGetDashDataOthers.length);
  }));

  it('should get dashboard data with error  ', waitForAsync(() => {
    component.getKpisData('pid');
    const httpreq = httpMock.expectOne(baseUrl + '/api/user-board-config/pid');
    httpreq.flush(['error']);
  }));

  it('should update dashboard config ', waitForAsync(() => {
    component.userName = "dummyName"
    component.selectedProject = {
      name : "fakeName",
      id :'fakeid'
    }
    component.kpiData = fakeGetDashData['data']['scrum'];
    component.kpiToBeHidden = [{
        "kpiId": "kpi121",
        "kpiName": "Defect Count by Status",
        "isEnabled": true,
        "order": 1,
        "kpiDetail": {
          "kpiInfo": {
            "definition": "Defect count by Status shows the breakup of all defects within an iteration by Status. The pie chart representation gives the count of defects in each Status"
          },
          "kpiFilter": "",
          "trendCalculative": false,
          "xaxisLabel": "",
          "yaxisLabel": "",
          "additionalFilterSupport": false
        },
        "shown": true
    }]
    component.kpiListData = fakeGetDashData['data'];
    component.updateData();
    const httpreq1 = httpMock.expectOne(baseUrl + '/api/user-board-config/saveAdmin/fakeid');
    httpreq1.flush(fakeGetDashData);
  }));


  it('should fetch all user projects', () => {
    const getProjectsResponse = { message: 'Fetched successfully', success: true, data: [{ id: '601bca9569515b0001d68182', projectName: 'TestRIshabh', createdAt: '2021-02-04T10:21:09', isKanban: false }] };
    component.getProjects();
    fixture.detectChanges();
    httpMock.match(baseUrl + '/api/basicconfigs')[0]?.flush(getProjectsResponse);
  });

  it('should allow user to select project', () => {
    const selectedProjects = { originalEvent: { isTrusted: true }, value: {id: '601bca9569515b0001d68182', name: 'test'}, itemValue: '601bca9569515b0001d68182' };
    const processorName = 'Jira';
    component.updateProjectSelection(selectedProjects);
    fixture.detectChanges();
    expect(component.selectedProject).toEqual({id: '601bca9569515b0001d68182', name: 'test'});
  });

  it('should reset form when tab changes',()=>{
    
    component.tabHeaders = ['scrum','kanban'];
    const spy = spyOn(component,'setFormControlData');
    component.handleTabChange({
      event : {
        index : 1
      }
    });
    expect(spy).toHaveBeenCalled();
  })

  it('should update kpiData and kpiListData', () => {
    // create sample data
    const obj = {
        "scrum": [
            {
                "boardId": 2,
                "boardName": "Speed",
                "kpis": [{
                  "kpiId": "kpi5",
                  "kpiName": "Sprint Predictability",
                  "isEnabled": true,
                  "order": 4,
                  "shown": false
              }]
            },
        ],
    }
    component.kpiChangesObj = {
        "Speed": [
            {
                "kpiId": "kpi5",
                "kpiName": "Sprint Predictability",
                "isEnabled": true,
                "order": 4,
                "shown": false
            }
        ]
    }
    component.kpiData = [
        {
            "boardId": 2,
            "boardName": "Speed",
            "kpis": [
                
                {
                    "kpiId": "kpi5",
                    "kpiName": "Sprint Predictability",
                    "isEnabled": true,
                    "order": 4,
                    "shown": false
                },
                
            ]
        },
    ]
    component.selectedTab = 'scrum';
    component.kpiListData[component.selectedTab] = [...component.kpiData];
    spyOn(component, 'updateData')
    component.save();
    expect(component.kpiListData).toEqual(obj);
  });

  it('should handle kpi change when event is not checked', () => {
    const event = {
      "originalEvent": {
          "isTrusted": true
      },
      "checked": false
    }
    const kpi = {
      "kpiId": "kpi39",
      "kpiName": "Sprint Velocity",
      "isEnabled": true,
      "order": 2,
      "kpiDetail": {
          "id": "64b4ed7acba3c12de16472ff",
          "kpiId": "kpi39",
          "kpiName": "Sprint Velocity",
          "isDeleted": "False",
          "defaultOrder": 20,
          "kpiUnit": "SP",
          "chartType": "grouped_column_plus_line",
          "showTrend": false,
          "isPositiveTrend": true,
          "lineLegend": "Sprint Velocity",
          "barLegend": "Last 5 Sprints Average",
          "calculateMaturity": false,
          "hideOverallFilter": false,
          "kpiSource": "Jira",
          "maxValue": "300",
          "kanban": false,
          "groupId": 2,
          "kpiInfo": {
              "definition": "Measures the rate of delivery across Sprints. Average velocity is calculated for the latest 5 sprints",
              "details": [
                  {
                      "type": "link",
                      "kpiLinkDetail": {
                          "text": "Detailed Information at",
                          "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/26935328/Scrum+SPEED+KPIs#Sprint-Velocity"
                      }
                  }
              ]
          },
          "aggregationCriteria": "sum",
          "trendCalculation": [
              {
                  "type": "Upwards",
                  "lhs": "value",
                  "rhs": "lineValue",
                  "operator": "<"
              },
              {
                  "type": "Upwards",
                  "lhs": "value",
                  "rhs": "lineValue",
                  "operator": "="
              },
              {
                  "type": "Downwards",
                  "lhs": "value",
                  "rhs": "lineValue",
                  "operator": ">"
              }
          ],
          "trendCalculative": true,
          "xaxisLabel": "Sprints",
          "yaxisLabel": "Count",
          "videoLink": {
              "id": "6309b8767bee141bb505e73f",
              "kpiId": "kpi39",
              "videoUrl": "",
              "disabled": false,
              "source": "You Tube"
          },
          "isAdditionalFilterSupport": true
      },
      "shown": true
    }

    const boardName = 'Speed';

    const kpis = [
      {
          "kpiId": "kpi40",
          "kpiName": "Issue Count",
          "isEnabled": true,
          "order": 1,
          "shown": true
      },
      {
          "kpiId": "kpi39",
          "kpiName": "Sprint Velocity",
          "isEnabled": true,
          "order": 2,
          "shown": true
      },
      {
          "kpiId": "kpi72",
          "kpiName": "Commitment Reliability",
          "isEnabled": true,
          "order": 3,
          "shown": true
      },
      {
          "kpiId": "kpi5",
          "kpiName": "Sprint Predictability",
          "isEnabled": true,
          "order": 4,
          "shown": true
      },
      {
          "kpiId": "kpi46",
          "kpiName": "Sprint Capacity Utilization",
          "isEnabled": true,
          "order": 5,
          "shown": true
      },
      {
          "kpiId": "kpi8",
          "kpiName": "Code Build Time",
          "isEnabled": true,
          "order": 8,
          "shown": true
      },
      {
          "kpiId": "kpi164",
          "kpiName": "Scope Churn",
          "isEnabled": true,
          "order": 9,
          "shown": true
      }
    ]
    const kpiObj = {
      "kpiId": "kpi39",
      "kpiName": "Sprint Velocity",
      "isEnabled": true,
      "order": 2,
      "kpiDetail": {
          "id": "64b4ed7acba3c12de16472ff",
          "kpiId": "kpi39",
          "kpiName": "Sprint Velocity",
          "isDeleted": "False",
          "defaultOrder": 20,
          "kpiUnit": "SP",
          "chartType": "grouped_column_plus_line",
          "showTrend": false,
          "isPositiveTrend": true,
          "lineLegend": "Sprint Velocity",
          "barLegend": "Last 5 Sprints Average",
          "calculateMaturity": false,
          "hideOverallFilter": false,
          "kpiSource": "Jira",
          "maxValue": "300",
          "kanban": false,
          "groupId": 2,
          "kpiInfo": {
              "definition": "Measures the rate of delivery across Sprints. Average velocity is calculated for the latest 5 sprints",
              "details": [
                  {
                      "type": "link",
                      "kpiLinkDetail": {
                          "text": "Detailed Information at",
                          "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/26935328/Scrum+SPEED+KPIs#Sprint-Velocity"
                      }
                  }
              ]
          },
          "aggregationCriteria": "sum",
          "trendCalculation": [
              {
                  "type": "Upwards",
                  "lhs": "value",
                  "rhs": "lineValue",
                  "operator": "<"
              },
              {
                  "type": "Upwards",
                  "lhs": "value",
                  "rhs": "lineValue",
                  "operator": "="
              },
              {
                  "type": "Downwards",
                  "lhs": "value",
                  "rhs": "lineValue",
                  "operator": ">"
              }
          ],
          "trendCalculative": true,
          "xaxisLabel": "Sprints",
          "yaxisLabel": "Count",
          "videoLink": {
              "id": "6309b8767bee141bb505e73f",
              "kpiId": "kpi39",
              "videoUrl": "",
              "disabled": false,
              "source": "You Tube"
          },
          "isAdditionalFilterSupport": true
      },
      "shown": false
    }
    const boardNames = {
      'Speed': new FormControl(true)
    }

    const kpiFormObj = {
      "kpi39": new FormControl(true)
    }
    component.kpiForm = new UntypedFormGroup({
      kpiCategories: new UntypedFormGroup(boardNames),
      kpis: new UntypedFormGroup(kpiFormObj)
    });
    spyOn(component, 'setMainDashboardKpiShowHideStatus')
    component.kpiChangesObj[boardName] = [];
    component.handleKpiChange(event, kpi, boardName, kpis);
    expect(component.kpiChangesObj[boardName]).toEqual([kpiObj]);
  })

  it('should handle kpi change when event is not checked', () => {
    const event = {
      "originalEvent": {
          "isTrusted": true
      },
      "checked": true
    }
    const kpi = {
      "kpiId": "kpi39",
      "kpiName": "Sprint Velocity",
      "isEnabled": true,
      "order": 2,
      "kpiDetail": {
          "id": "64b4ed7acba3c12de16472ff",
          "kpiId": "kpi39",
          "kpiName": "Sprint Velocity",
          "isDeleted": "False",
          "defaultOrder": 20,
          "kpiUnit": "SP",
          "chartType": "grouped_column_plus_line",
          "showTrend": false,
          "isPositiveTrend": true,
          "lineLegend": "Sprint Velocity",
          "barLegend": "Last 5 Sprints Average",
          "calculateMaturity": false,
          "hideOverallFilter": false,
          "kpiSource": "Jira",
          "maxValue": "300",
          "kanban": false,
          "groupId": 2,
          "kpiInfo": {
              "definition": "Measures the rate of delivery across Sprints. Average velocity is calculated for the latest 5 sprints",
              "details": [
                  {
                      "type": "link",
                      "kpiLinkDetail": {
                          "text": "Detailed Information at",
                          "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/26935328/Scrum+SPEED+KPIs#Sprint-Velocity"
                      }
                  }
              ]
          },
          "aggregationCriteria": "sum",
          "trendCalculation": [
              {
                  "type": "Upwards",
                  "lhs": "value",
                  "rhs": "lineValue",
                  "operator": "<"
              },
              {
                  "type": "Upwards",
                  "lhs": "value",
                  "rhs": "lineValue",
                  "operator": "="
              },
              {
                  "type": "Downwards",
                  "lhs": "value",
                  "rhs": "lineValue",
                  "operator": ">"
              }
          ],
          "trendCalculative": true,
          "xaxisLabel": "Sprints",
          "yaxisLabel": "Count",
          "videoLink": {
              "id": "6309b8767bee141bb505e73f",
              "kpiId": "kpi39",
              "videoUrl": "",
              "disabled": false,
              "source": "You Tube"
          },
          "isAdditionalFilterSupport": true
      },
      "shown": true
    }

    const boardName = 'Speed';

    const kpis = [
      {
          "kpiId": "kpi40",
          "kpiName": "Issue Count",
          "isEnabled": true,
          "order": 1,
          "shown": true
      },
      {
          "kpiId": "kpi39",
          "kpiName": "Sprint Velocity",
          "isEnabled": true,
          "order": 2,
          "shown": true
      },
      {
          "kpiId": "kpi72",
          "kpiName": "Commitment Reliability",
          "isEnabled": true,
          "order": 3,
          "shown": true
      },
      {
          "kpiId": "kpi5",
          "kpiName": "Sprint Predictability",
          "isEnabled": true,
          "order": 4,
          "shown": true
      },
      {
          "kpiId": "kpi46",
          "kpiName": "Sprint Capacity Utilization",
          "isEnabled": true,
          "order": 5,
          "shown": true
      },
      {
          "kpiId": "kpi8",
          "kpiName": "Code Build Time",
          "isEnabled": true,
          "order": 8,
          "shown": true
      },
      {
          "kpiId": "kpi164",
          "kpiName": "Scope Churn",
          "isEnabled": true,
          "order": 9,
          "shown": true
      }
    ]
    const kpiObj = {
      "kpiId": "kpi39",
      "kpiName": "Sprint Velocity",
      "isEnabled": true,
      "order": 2,
      "kpiDetail": {
          "id": "64b4ed7acba3c12de16472ff",
          "kpiId": "kpi39",
          "kpiName": "Sprint Velocity",
          "isDeleted": "False",
          "defaultOrder": 20,
          "kpiUnit": "SP",
          "chartType": "grouped_column_plus_line",
          "showTrend": false,
          "isPositiveTrend": true,
          "lineLegend": "Sprint Velocity",
          "barLegend": "Last 5 Sprints Average",
          "calculateMaturity": false,
          "hideOverallFilter": false,
          "kpiSource": "Jira",
          "maxValue": "300",
          "kanban": false,
          "groupId": 2,
          "kpiInfo": {
              "definition": "Measures the rate of delivery across Sprints. Average velocity is calculated for the latest 5 sprints",
              "details": [
                  {
                      "type": "link",
                      "kpiLinkDetail": {
                          "text": "Detailed Information at",
                          "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/26935328/Scrum+SPEED+KPIs#Sprint-Velocity"
                      }
                  }
              ]
          },
          "aggregationCriteria": "sum",
          "trendCalculation": [
              {
                  "type": "Upwards",
                  "lhs": "value",
                  "rhs": "lineValue",
                  "operator": "<"
              },
              {
                  "type": "Upwards",
                  "lhs": "value",
                  "rhs": "lineValue",
                  "operator": "="
              },
              {
                  "type": "Downwards",
                  "lhs": "value",
                  "rhs": "lineValue",
                  "operator": ">"
              }
          ],
          "trendCalculative": true,
          "xaxisLabel": "Sprints",
          "yaxisLabel": "Count",
          "videoLink": {
              "id": "6309b8767bee141bb505e73f",
              "kpiId": "kpi39",
              "videoUrl": "",
              "disabled": false,
              "source": "You Tube"
          },
          "isAdditionalFilterSupport": true
      },
      "shown": true
    }
    const boardNames = {
      'Speed': new FormControl(true)
    }

    const kpiFormObj = {
      "kpi39": new FormControl(true)
    }
    component.kpiForm = new UntypedFormGroup({
      kpiCategories: new UntypedFormGroup(boardNames),
      kpis: new UntypedFormGroup(kpiFormObj)
    });
    spyOn(component, 'setMainDashboardKpiShowHideStatus')
    // component.kpiChangesObj[boardName] = [];
    component.handleKpiChange(event, kpi, boardName, kpis);
    expect(component.kpiChangesObj[boardName]).toEqual([kpiObj]);
  })

  it('should handle kpi category change when event checked is false', () => {
    const event = {
      "originalEvent": {
          "isTrusted": true
      },
      "checked": false
    }

    const boardData = {
      "boardId": 8,
      "boardName": "Speed",
      "kpis": [
          {
              "kpiId": "kpi39",
              "kpiName": "Ticket Velocity",
              "isEnabled": true,
              "order": 1,
              "kpiDetail": {
                  "id": "64b4ed7acba3c12de1647312",
                  "kpiId": "kpi49",
                  "kpiName": "Ticket Velocity",
                  "isDeleted": "False",
                  "defaultOrder": 12,
                  "kpiUnit": "SP",
                  "chartType": "line",
                  "showTrend": false,
                  "isPositiveTrend": true,
                  "calculateMaturity": false,
                  "hideOverallFilter": false,
                  "kpiSource": "Jira",
                  "maxValue": "300",
                  "kanban": true,
                  "groupId": 1,
                  "isAdditionalFilterSupport": true
              },
              "shown": false
          },
      ]
    }

    const kpiFormObj = {
      "kpi39": new FormControl(true)
    }
    component.kpiForm = new UntypedFormGroup({
      kpis: new UntypedFormGroup(kpiFormObj)
    });
    const spy = spyOn(component, 'setMainDashboardKpiShowHideStatus')
    component.handleKpiCategoryChange(event, boardData);
    expect(spy).toHaveBeenCalledWith('kpi39', false);
  })

  it('should handle kpi category change when event checked is false', () => {
    const event = {
      "originalEvent": {
          "isTrusted": true
      },
      "checked": true
    }

    const boardData = {
      "boardId": 8,
      "boardName": "Speed",
      "kpis": [
          {
              "kpiId": "kpi39",
              "kpiName": "Ticket Velocity",
              "isEnabled": true,
              "order": 1,
              "kpiDetail": {
                  "id": "64b4ed7acba3c12de1647312",
                  "kpiId": "kpi49",
                  "kpiName": "Ticket Velocity",
                  "isDeleted": "False",
                  "defaultOrder": 12,
                  "kpiUnit": "SP",
                  "chartType": "line",
                  "showTrend": false,
                  "isPositiveTrend": true,
                  "calculateMaturity": false,
                  "hideOverallFilter": false,
                  "kpiSource": "Jira",
                  "maxValue": "300",
                  "kanban": true,
                  "groupId": 1,
                  "isAdditionalFilterSupport": true
              },
              "shown": false
          },
      ]
    }

    const kpiFormObj = {
      "kpi39": new FormControl(true)
    }
    component.kpiForm = new UntypedFormGroup({
      kpis: new UntypedFormGroup(kpiFormObj)
    });
    const spy = spyOn(component, 'setMainDashboardKpiShowHideStatus')
    component.handleKpiCategoryChange(event, boardData);
    expect(spy).toHaveBeenCalledWith('kpi39', true);
  })

  it('should set main dashboard kpi show hide status', () => {
    const kpiId = 'kpi39';
    const shown = true;
    component.selectedTab = 'scrum';
    component.tabListContent = fakeGetDashData.data;
    component.setMainDashboardKpiShowHideStatus(kpiId, shown);
    expect(component.tabListContent[component.selectedTab][0].kpis.find(kpiDetail => kpiDetail.kpiId === kpiId)?.shown).toBe(true);
  })

  it('should get projects when superadmin', () => {
    const response = fakeProjects;
    spyOn(httpService, 'getUserProjects').and.returnValue(of(response));
    spyOn(getAuthorizationService, 'checkIfSuperUser').and.returnValue(true)
    component.userProjects = [];
    component.loader = false;
    component.tabHeaders = [];
    component.backupUserProjects = [];
    component.selectedProject = {};
    spyOn(component, 'getKpisData')
    component.getProjects();
    expect(component.getKpisData).toHaveBeenCalledWith(component.selectedProject['id']);
  })

  it('should get projects when not superadmin', () => {
    const response = fakeProjects;
    spyOn(httpService, 'getUserProjects').and.returnValue(of(response));
    spyOn(getAuthorizationService, 'checkIfProjectAdmin').and.returnValue(true)
    component.userProjects = [];
    component.loader = false;
    component.tabHeaders = [];
    component.backupUserProjects = [];
    component.selectedProject = {};
    spyOn(component, 'getKpisData')
    component.getProjects();
    expect(component.getKpisData).toHaveBeenCalledWith(component.selectedProject['id']);
  })

  xit('should not get projects', () => {
    const errResponse = {
      'error': "Something went wrong"
    };
    spyOn(httpService, 'getUserProjects').and.returnValue(throwError(errResponse));
    // spyOn(getAuthorizationService, 'checkIfProjectAdmin').and.returnValue(true)
    // component.userProjects = [];
    // component.loader = false;
    // component.tabHeaders = [];
    // component.backupUserProjects = [];
    // component.selectedProject = {};
    const spy = spyOn(messageService, 'add')
    component.getProjects();
    expect(spy).toHaveBeenCalled();
  })

  describe('isEmptyObject', () => {
  
    it('should return true for an empty object', () => {
      const value = {};
      const result = component.isEmptyObject(value);
      expect(result).toBe(true);
    });
  
    it('should return false for an object with properties', () => {
      const value = { key: 'value' };
      const result = component.isEmptyObject(value);
      expect(result).toBe(false);
    });
  
    it('should return false for an array', () => {
      const value = [];
      const result = component.isEmptyObject(value);
      expect(result).toBe(false);
    });
  
    it('should return false for a string', () => {
      const value = 'string';
      const result = component.isEmptyObject(value);
      expect(result).toBe(false);
    });
  
    it('should return false for a number', () => {
      const value = 123;
      const result = component.isEmptyObject(value);
      expect(result).toBe(false);
    });
  
    it('should return false for a boolean', () => {
      const value = true;
      const result = component.isEmptyObject(value);
      expect(result).toBe(false);
    });
  });

  it('should get user projects for kanban',()=>{
    component.backupUserProjects = [{type : 'kanban'},{type : 'common'}]
    component.tabHeaders = ['scrum','kanban'];
    const spy = spyOn(component,'setFormControlData');
    component.handleTabChange({
        index : 1
    });
    expect(spy).toHaveBeenCalled();
  })

  it('should get user projects for scrum',()=>{
    component.backupUserProjects = [{type : 'scrum'},{type : 'common'}]
    component.tabHeaders = ['scrum','kanban'];
    const spy = spyOn(component,'setFormControlData');
    component.handleTabChange({
        index : 0
    });
    expect(spy).toHaveBeenCalled();
  })
});



