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

/*********************************************
File contains Developer dashboard 's
unit test cases.
@author rishabh
*******************************/

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { HttpService } from '../../services/http.service';
import { SharedService } from '../../services/shared.service';
import { HelperService } from '../../services/helper.service';
import { ExcelService } from '../../services/excel.service';
import { DatePipe } from '../../../../node_modules/@angular/common';
import { MessageService } from 'primeng/api';
import { DeveloperComponent } from './developer.component';
import { APP_CONFIG, AppConfig } from '../../services/app.config';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CommonModule } from '@angular/common';
import { DropdownModule } from 'primeng/dropdown';
import { RouterTestingModule } from '@angular/router/testing';
import { DashboardComponent } from '../dashboard.component';
import { ExportExcelComponent } from 'src/app/component/export-excel/export-excel.component';
import { Routes } from '@angular/router';
import { environment } from 'src/environments/environment';

const selectedTab = 'developer';
const masterData = require('../../../test/resource/masterData.json');
const filterData = require('../../../test/resource/filterData.json');
const fakeDeveloperTabData = require('../../../test/resource/fakeDeveloperTabData.json');
const dashConfigData = require('../../../test/resource/fakeShowHideApi.json').data;
const filterApplyDataWithScrum = {
  kpiList: [
    {
      "id": "64da0fc3fa8e85ada337fb42",
      "kpiId": "kpi84",
      "kpiName": "Mean Time To Merge",
      "isDeleted": "False",
      "defaultOrder": 23,
      "kpiCategory": "Developer",
      "kpiUnit": "Hours",
      "chartType": "",
      "showTrend": true,
      "isPositiveTrend": false,
      "calculateMaturity": true,
      "hideOverallFilter": true,
      "kpiSource": "BitBucket",
      "maxValue": "10",
      "thresholdValue": 55,
      "kanban": false,
      "groupId": 1,
      "kpiInfo": {
        "definition": "MEAN TIME TO MERGE measures the efficiency of the code review process in a team",
        "details": [
          {
            "type": "paragraph",
            "value": "It is calculated in ‘Hours’. Fewer the Hours better is the ‘Speed’"
          },
          {
            "type": "paragraph",
            "value": "A progress indicator shows trend of Mean time to merge in last 2 weeks. A downward trend is considered positive"
          },
          {
            "type": "paragraph",
            "value": "Maturity of the KPI is calculated based on the average of the last 5 weeks"
          }
        ],
        "maturityLevels": [
          {
            "level": "M5",
            "bgColor": "#6cab61",
            "range": "<4 Hours"
          },
          {
            "level": "M4",
            "bgColor": "#AEDB76",
            "range": "4-8 Hours"
          },
          {
            "level": "M3",
            "bgColor": "#eff173",
            "range": "8-16 Hours"
          },
          {
            "level": "M2",
            "bgColor": "#ffc35b",
            "range": "16-48 Hours"
          },
          {
            "level": "M1",
            "bgColor": "#F06667",
            "range": ">48 Hours"
          }
        ]
      },
      "kpiFilter": "dropDown",
      "aggregationCriteria": "average",
      "maturityRange": [
        "-16",
        "16-8",
        "8-4",
        "4-2",
        "2-"
      ],
      "isRepoToolKpi": false,
      "xaxisLabel": "Weeks",
      "yaxisLabel": "Count(Hours)",
      "additionalFilterSupport": false,
      "trendCalculative": false
    },
    {
      "id": "64da1294fa8e85ada337fb43",
      "kpiId": "kpi11",
      "kpiName": "Check-Ins & Merge Requests",
      "isDeleted": "False",
      "defaultOrder": 24,
      "kpiCategory": "Developer",
      "kpiUnit": "MRs",
      "chartType": "",
      "showTrend": true,
      "isPositiveTrend": true,
      "lineLegend": "Merge Requests",
      "barLegend": "Commits",
      "calculateMaturity": true,
      "hideOverallFilter": true,
      "kpiSource": "BitBucket",
      "maxValue": "10",
      "thresholdValue": 55,
      "kanban": false,
      "groupId": 1,
      "kpiInfo": {
        "definition": "NUMBER OF CHECK-INS helps in measuring the transparency as well the how well the tasks have been broken down. NUMBER OF MERGE REQUESTS when looked at along with commits highlights the efficiency of the review process",
        "details": [
          {
            "type": "paragraph",
            "value": "It is calculated as a Count. Higher the count better is the ‘Speed’"
          },
          {
            "type": "paragraph",
            "value": "A progress indicator shows trend of Number of Check-ins & Merge requests between last 2 days. An upward trend is considered positive"
          }
        ],
        "maturityLevels": [
          {
            "level": "M5",
            "bgColor": "#6cab61",
            "range": "> 16"
          },
          {
            "level": "M4",
            "bgColor": "#AEDB76",
            "range": "8-16"
          },
          {
            "level": "M3",
            "bgColor": "#eff173",
            "range": "4-8"
          },
          {
            "level": "M2",
            "bgColor": "#ffc35b",
            "range": "2-4"
          },
          {
            "level": "M1",
            "bgColor": "#F06667",
            "range": "0-2"
          }
        ]
      },
      "kpiFilter": "dropDown",
      "aggregationCriteria": "average",
      "maturityRange": [
        "-2",
        "2-4",
        "4-8",
        "8-16",
        "16-"
      ],
      "isRepoToolKpi": false,
      "xaxisLabel": "Days",
      "yaxisLabel": "Count",
      "additionalFilterSupport": false,
      "trendCalculative": false
    }
  ], ids: ['Speedy 2.0_62503_Speedy 2.0'], level: 3, selectedMap: { hierarchyLevelOne: ['ASDFG_hierarchyLevelOne'], Project: ['Speedy 2.0_62503_Speedy 2.0'], SubProject: [], Sprint: [], Build: [], Release: [], Squad: [], Individual: [] }
};

const completeHierarchyData = { "kanban": [{ "id": "64c27a5d1d26a19187772c84", "level": 1, "hierarchyLevelId": "hierarchyLevelOne", "hierarchyLevelName": "Organization" }, { "id": "64c27a5d1d26a19187772c85", "level": 2, "hierarchyLevelId": "hierarchyLevelTwo", "hierarchyLevelName": "Business Unit" }, { "id": "64c27a5d1d26a19187772c86", "level": 3, "hierarchyLevelId": "hierarchyLevelThree", "hierarchyLevelName": "Portfolio" }, { "level": 4, "hierarchyLevelId": "project", "hierarchyLevelName": "Project" }, { "level": 5, "hierarchyLevelId": "release", "hierarchyLevelName": "Release" }, { "level": 6, "hierarchyLevelId": "afOne", "hierarchyLevelName": "Teams" }], "scrum": [{ "id": "64c27a5d1d26a19187772c84", "level": 1, "hierarchyLevelId": "hierarchyLevelOne", "hierarchyLevelName": "Organization" }, { "id": "64c27a5d1d26a19187772c85", "level": 2, "hierarchyLevelId": "hierarchyLevelTwo", "hierarchyLevelName": "Business Unit" }, { "id": "64c27a5d1d26a19187772c86", "level": 3, "hierarchyLevelId": "hierarchyLevelThree", "hierarchyLevelName": "Portfolio" }, { "level": 4, "hierarchyLevelId": "project", "hierarchyLevelName": "Project" }, { "level": 5, "hierarchyLevelId": "sprint", "hierarchyLevelName": "Sprint" }, { "level": 5, "hierarchyLevelId": "release", "hierarchyLevelName": "Release" }, { "level": 6, "hierarchyLevelId": "afOne", "hierarchyLevelName": "Teams" }] };

const filterApplyData = {
  "ids": [
    5
  ],
  "sprintIncluded": [
    "CLOSED"
  ],
  "selectedMap": {
    "hierarchyLevelOne": [],
    "hierarchyLevelTwo": [],
    "hierarchyLevelThree": [],
    "project": [
      "Azureproj_64c2a5c930e0c37cd01c2a68"
    ],
    "sprint": [],
    "release": [],
    "afOne": [],
    "date": [
      "DAYS"
    ]
  },
  "level": 4,
  "label": "project"
};

describe('DeveloperComponent', () => {
  let component: DeveloperComponent;
  let fixture: ComponentFixture<DeveloperComponent>;
  let service: SharedService;
  let httpService: HttpService;
  let helperService: HelperService;
  let excelService: ExcelService;
  let messageService: MessageService;
  let httpMock;

  const baseUrl = environment.baseUrl;

  const routes: Routes = [
    { path: 'dashboard', component: DashboardComponent },
    { path: 'authentication/login', component: DashboardComponent }
  ];

  beforeEach(async () => {
    service = new SharedService();
    await TestBed.configureTestingModule({
      imports: [
        FormsModule,
        HttpClientTestingModule,
        ReactiveFormsModule,
        CommonModule,
        DropdownModule,
        RouterTestingModule.withRoutes(routes)
      ],
      declarations: [DeveloperComponent, DashboardComponent,
        ExportExcelComponent],
      providers: [
        HelperService,
        HttpService,
        { provide: APP_CONFIG, useValue: AppConfig },
        { provide: SharedService, useValue: service },
        ExcelService, DatePipe, MessageService
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
      .compileComponents();

    fixture = TestBed.createComponent(DeveloperComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const type = 'Scrum';
    service.selectedtype = type;
    localStorage?.setItem('completeHierarchyData', JSON.stringify(completeHierarchyData));
    fixture.detectChanges();

    httpService = TestBed.inject(HttpService);
    helperService = TestBed.inject(HelperService);

    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should open Scrum by default', (done) => {
    const type = 'Scrum';
    service.setSelectedTypeOrTabRefresh(selectedTab, type);
    expect(component.selectedtype).toBe(type);
    done();
  });

  it('should fetch global dashboard config', (done) => {
    service.setDashConfigData(dashConfigData);
    fixture.detectChanges();
    expect(component.globalConfig).toEqual(dashConfigData);
    done();
  });

  it('should map the correct color to project', (done) => {
    const x = {
      "nodeName": "KnowHOW",
      "color": "#079FFF"
    };
    service.setColorObj(x);
    fixture.detectChanges();
    expect(component.colorObj).toBe(x);
    done();
  });

  it('should pull data for Scrum on load', (done) => {
    const type = 'Scrum';
    service.setDashConfigData(dashConfigData);
    fixture.detectChanges();
    expect(component.globalConfig).toEqual(dashConfigData);
    service.setSelectedTypeOrTabRefresh(selectedTab, type);
    service.select(masterData, filterData, filterApplyData, selectedTab, false, true);
    fixture.detectChanges();
    setTimeout(() => {
      httpMock.match(baseUrl + '/api/bitbucket/kpi')[0].flush(fakeDeveloperTabData);
      expect(component.bitBucketKpiData).toEqual(helperService.createKpiWiseId(fakeDeveloperTabData));
      done();
    }, 500);

  });


  it('should pull data for Kanban on switch to Kanban', (done) => {
    const type = 'Kanban';
    service.setDashConfigData(dashConfigData);
    fixture.detectChanges();
    expect(component.globalConfig).toEqual(dashConfigData);
    service.setSelectedTypeOrTabRefresh(selectedTab, type);
    service.select(masterData, filterData, filterApplyData, selectedTab, false, true);
    fixture.detectChanges();

    setTimeout(() => {
      httpMock.match(baseUrl + '/api/bitbucketkanban/kpi')[0].flush(fakeDeveloperTabData);
      done();
    }, 500);
  });

  it('should handle KPI filter change', (done) => {
    const x = {
      "nodeName": "KnowHOW",
      "color": "#079FFF"
    };
    service.setColorObj(x);
    component.allKpiArray =
      [{
        "kpiId": "kpi84",
        "kpiName": "Mean Time To Merge",
        "unit": "Hours",
        "maxValue": "10",
        "chartType": "",
        "kpiInfo": {
          "definition": "MEAN TIME TO MERGE measures the efficiency of the code review process in a team",
          "details": [
            {
              "type": "paragraph",
              "value": "It is calculated in ‘Hours’. Fewer the Hours better is the ‘Speed’"
            },
            {
              "type": "paragraph",
              "value": "A progress indicator shows trend of Mean time to merge in last 2 weeks. A downward trend is considered positive"
            },
            {
              "type": "paragraph",
              "value": "Maturity of the KPI is calculated based on the average of the last 5 weeks"
            }
          ],
          "maturityLevels": [
            {
              "level": "M5",
              "bgColor": "#6cab61",
              "range": "<4 Hours"
            },
            {
              "level": "M4",
              "bgColor": "#AEDB76",
              "range": "4-8 Hours"
            },
            {
              "level": "M3",
              "bgColor": "#eff173",
              "range": "8-16 Hours"
            },
            {
              "level": "M2",
              "bgColor": "#ffc35b",
              "range": "16-48 Hours"
            },
            {
              "level": "M1",
              "bgColor": "#F06667",
              "range": ">48 Hours"
            }
          ]
        },
        "id": "64da0fc3fa8e85ada337fb42",
        "isDeleted": "False",
        "kpiCategory": "Developer",
        "kpiUnit": "Hours",
        "kanban": false,
        "kpiSource": "BitBucket",
        "thresholdValue": 55,
        "trendValueList": [
          {
            "filter": "Overall",
            "value": [
              {
                "data": "KnowHOW",
                "value": [
                  {
                    "data": "1",
                    "value": 1,
                    "hoverValue": {},
                    "date": "2023-08-25",
                    "sprojectName": "KnowHOW"
                  },
                  {
                    "data": "0",
                    "value": 0,
                    "hoverValue": {},
                    "date": "2023-08-26",
                    "sprojectName": "KnowHOW"
                  },
                  {
                    "data": "0",
                    "value": 0,
                    "hoverValue": {},
                    "date": "2023-08-27",
                    "sprojectName": "KnowHOW"
                  },
                  {
                    "data": "0",
                    "value": 0,
                    "hoverValue": {},
                    "date": "2023-08-28",
                    "sprojectName": "KnowHOW"
                  },
                  {
                    "data": "0",
                    "value": 0,
                    "hoverValue": {},
                    "date": "2023-08-29",
                    "sprojectName": "KnowHOW"
                  }
                ],
                "maturity": "5",
                "maturityValue": "0.2"
              }
            ]
          },
          {
            "filter": "master -> PSknowHOW -> KnowHOW",
            "value": [
              {
                "data": "KnowHOW",
                "value": [
                  {
                    "data": "1",
                    "value": 1,
                    "hoverValue": {},
                    "date": "2023-08-25",
                    "sprojectName": "KnowHOW"
                  },
                  {
                    "data": "0",
                    "value": 0,
                    "hoverValue": {},
                    "date": "2023-08-26",
                    "sprojectName": "KnowHOW"
                  },
                  {
                    "data": "0",
                    "value": 0,
                    "hoverValue": {},
                    "date": "2023-08-27",
                    "sprojectName": "KnowHOW"
                  },
                  {
                    "data": "0",
                    "value": 0,
                    "hoverValue": {},
                    "date": "2023-08-28",
                    "sprojectName": "KnowHOW"
                  },
                  {
                    "data": "0",
                    "value": 0,
                    "hoverValue": {},
                    "date": "2023-08-29",
                    "sprojectName": "KnowHOW"
                  }
                ],
                "maturity": "5",
                "maturityValue": "0.2"
              }
            ]
          }
        ],
        "maturityRange": [
          "-16",
          "16-8",
          "8-4",
          "4-2",
          "2-"
        ],
        "groupId": 1
      }, {
        "kpiId": "kpi11",
        "kpiName": "Check-Ins & Merge Requests",
        "unit": "MRs",
        "maxValue": "10",
        "chartType": "",
        "kpiInfo": {
          "definition": "NUMBER OF CHECK-INS helps in measuring the transparency as well the how well the tasks have been broken down. NUMBER OF MERGE REQUESTS when looked at along with commits highlights the efficiency of the review process",
          "details": [
            {
              "type": "paragraph",
              "value": "It is calculated as a Count. Higher the count better is the ‘Speed’"
            },
            {
              "type": "paragraph",
              "value": "A progress indicator shows trend of Number of Check-ins & Merge requests between last 2 days. An upward trend is considered positive"
            }
          ],
          "maturityLevels": [
            {
              "level": "M5",
              "bgColor": "#6cab61",
              "range": "> 16"
            },
            {
              "level": "M4",
              "bgColor": "#AEDB76",
              "range": "8-16"
            },
            {
              "level": "M3",
              "bgColor": "#eff173",
              "range": "4-8"
            },
            {
              "level": "M2",
              "bgColor": "#ffc35b",
              "range": "2-4"
            },
            {
              "level": "M1",
              "bgColor": "#F06667",
              "range": "0-2"
            }
          ]
        },
        "id": "64da1294fa8e85ada337fb43",
        "isDeleted": "False",
        "kpiCategory": "Developer",
        "kpiUnit": "MRs",
        "kanban": false,
        "kpiSource": "BitBucket",
        "thresholdValue": 55,
        "trendValueList": [
          {
            "filter": "Overall",
            "value": [
              {
                "data": "KnowHOW",
                "value": [
                  {
                    "value": 0,
                    "hoverValue": {
                      "No. of Check in": 0,
                      "No. of Merge Requests": 0
                    },
                    "date": "2023-08-29",
                    "lineValue": 0,
                    "sprojectName": "KnowHOW"
                  },
                  {
                    "value": 2,
                    "hoverValue": {
                      "No. of Check in": 2,
                      "No. of Merge Requests": 1
                    },
                    "date": "2023-08-28",
                    "lineValue": 1,
                    "sprojectName": "KnowHOW"
                  },
                  {
                    "value": 1,
                    "hoverValue": {
                      "No. of Check in": 1,
                      "No. of Merge Requests": 1
                    },
                    "date": "2023-08-27",
                    "lineValue": 1,
                    "sprojectName": "KnowHOW"
                  },
                  {
                    "value": 0,
                    "hoverValue": {
                      "No. of Check in": 0,
                      "No. of Merge Requests": 0
                    },
                    "date": "2023-08-26",
                    "lineValue": 0,
                    "sprojectName": "KnowHOW"
                  },
                  {
                    "value": 2,
                    "hoverValue": {
                      "No. of Check in": 2,
                      "No. of Merge Requests": 2
                    },
                    "date": "2023-08-25",
                    "lineValue": 2,
                    "sprojectName": "KnowHOW"
                  }
                ],
                "maturity": "1",
                "maturityValue": "1"
              }
            ]
          },
          {
            "filter": "debbie_integration -> PSKnowHOW -> KnowHOW",
            "value": [
              {
                "data": "KnowHOW",
                "value": [
                  {
                    "value": 0,
                    "hoverValue": {
                      "No. of Check in": 0,
                      "No. of Merge Requests": 0
                    },
                    "date": "2023-08-29",
                    "lineValue": 0,
                    "sprojectName": "KnowHOW"
                  },
                  {
                    "value": 2,
                    "hoverValue": {
                      "No. of Check in": 2,
                      "No. of Merge Requests": 0
                    },
                    "date": "2023-08-28",
                    "lineValue": 0,
                    "sprojectName": "KnowHOW"
                  },
                  {
                    "value": 1,
                    "hoverValue": {
                      "No. of Check in": 1,
                      "No. of Merge Requests": 0
                    },
                    "date": "2023-08-27",
                    "lineValue": 0,
                    "sprojectName": "KnowHOW"
                  },
                  {
                    "value": 0,
                    "hoverValue": {
                      "No. of Check in": 0,
                      "No. of Merge Requests": 0
                    },
                    "date": "2023-08-26",
                    "lineValue": 0,
                    "sprojectName": "KnowHOW"
                  },
                  {
                    "value": 2,
                    "hoverValue": {
                      "No. of Check in": 2,
                      "No. of Merge Requests": 0
                    },
                    "date": "2023-08-25",
                    "lineValue": 0,
                    "sprojectName": "KnowHOW"
                  }
                ],
                "maturity": "1",
                "maturityValue": "0"
              }
            ]
          },
          {
            "filter": "master -> PSknowHOW -> KnowHOW",
            "value": [
              {
                "data": "KnowHOW",
                "value": [
                  {
                    "value": 0,
                    "hoverValue": {
                      "No. of Check in": 0,
                      "No. of Merge Requests": 0
                    },
                    "date": "2023-08-29",
                    "lineValue": 0,
                    "sprojectName": "KnowHOW"
                  },
                  {
                    "value": 0,
                    "hoverValue": {
                      "No. of Check in": 0,
                      "No. of Merge Requests": 1
                    },
                    "date": "2023-08-28",
                    "lineValue": 1,
                    "sprojectName": "KnowHOW"
                  },
                  {
                    "value": 0,
                    "hoverValue": {
                      "No. of Check in": 0,
                      "No. of Merge Requests": 1
                    },
                    "date": "2023-08-27",
                    "lineValue": 1,
                    "sprojectName": "KnowHOW"
                  },
                  {
                    "value": 0,
                    "hoverValue": {
                      "No. of Check in": 0,
                      "No. of Merge Requests": 0
                    },
                    "date": "2023-08-26",
                    "lineValue": 0,
                    "sprojectName": "KnowHOW"
                  },
                  {
                    "value": 9,
                    "hoverValue": {
                      "No. of Check in": 9,
                      "No. of Merge Requests": 2
                    },
                    "date": "2023-08-25",
                    "lineValue": 2,
                    "sprojectName": "KnowHOW"
                  }
                ],
                "maturity": "1",
                "maturityValue": "1"
              }
            ]
          }
        ],
        "maturityRange": [
          "-2",
          "2-4",
          "4-8",
          "8-16",
          "16-"
        ],
        "groupId": 1
      }];
    fixture.detectChanges();
    expect(component.colorObj).toBe(x);
    const event = {
      "filter1": "debbie_integration -> PSKnowHOW -> KnowHOW"
    };
    const kpi = {
      "kpiId": "kpi11",
      "kpiName": "Check-Ins & Merge Requests",
      "isEnabled": true,
      "order": 24,
      "kpiDetail": {
        "id": "64da1294fa8e85ada337fb43",
        "kpiId": "kpi11",
        "kpiName": "Check-Ins & Merge Requests",
        "isDeleted": "False",
        "defaultOrder": 24,
        "kpiCategory": "Developer",
        "kpiUnit": "MRs",
        "chartType": "grouped_column_plus_line",
        "showTrend": true,
        "isPositiveTrend": true,
        "lineLegend": "Merge Requests",
        "barLegend": "Commits",
        "calculateMaturity": true,
        "hideOverallFilter": true,
        "kpiSource": "BitBucket",
        "maxValue": "10",
        "thresholdValue": 55,
        "kanban": false,
        "groupId": 1,
        "kpiInfo": {
          "definition": "NUMBER OF CHECK-INS helps in measuring the transparency as well the how well the tasks have been broken down. NUMBER OF MERGE REQUESTS when looked at along with commits highlights the efficiency of the review process",
          "details": [
            {
              "type": "paragraph",
              "value": "It is calculated as a Count. Higher the count better is the ‘Speed’"
            },
            {
              "type": "paragraph",
              "value": "A progress indicator shows trend of Number of Check-ins & Merge requests between last 2 days. An upward trend is considered positive"
            }
          ],
          "maturityLevels": [
            {
              "level": "M5",
              "bgColor": "#6cab61",
              "range": "> 16"
            },
            {
              "level": "M4",
              "bgColor": "#AEDB76",
              "range": "8-16"
            },
            {
              "level": "M3",
              "bgColor": "#eff173",
              "range": "4-8"
            },
            {
              "level": "M2",
              "bgColor": "#ffc35b",
              "range": "2-4"
            },
            {
              "level": "M1",
              "bgColor": "#F06667",
              "range": "0-2"
            }
          ]
        },
        "kpiFilter": "dropDown",
        "aggregationCriteria": "average",
        "maturityRange": [
          "-2",
          "2-4",
          "4-8",
          "8-16",
          "16-"
        ],
        "isRepoToolKpi": false,
        "trendCalculative": false,
        "additionalFilterSupport": false,
        "xaxisLabel": "Days",
        "yaxisLabel": "Count"
      },
      "shown": true
    };
    component.handleSelectedOption(event, kpi);
    fixture.detectChanges();
    expect(component.kpiSelectedFilterObj['kpi11']).toEqual(["debbie_integration -> PSKnowHOW -> KnowHOW"]);
    done();
  });

});
