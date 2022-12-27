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

import { ComponentFixture, fakeAsync, TestBed, tick, waitForAsync } from '@angular/core/testing';

import { MaturityComponent } from './maturity.component';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpService } from '../../services/http.service';
import { SharedService } from '../../services/shared.service';
import { HttpClientModule } from '@angular/common/http';
import { APP_CONFIG, AppConfig } from '../../services/app.config';
import { ExcelService } from '../../services/excel.service';
import { DatePipe } from '../../../../node_modules/@angular/common';
import { RouterTestingModule } from '@angular/router/testing';
import { HelperService } from '../../services/helper.service';
import { environment } from '../../../environments/environment';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { of } from 'rxjs';
const masterData = require('../../../test/resource/masterData.json');
const filterData = require('../../../test/resource/filterData.json');


describe('MaturityComponent', () => {
  let component: MaturityComponent;
  let fixture: ComponentFixture<MaturityComponent>;
  let service: SharedService;
  let httpService: HttpService;
  let helperService: HelperService;

  const baseUrl = environment.baseUrl;  // Servers Env

  const filterApplyDataWithNoFilter = {};
  const selectedTab = 'Maturity';

  const dashConfigData = { message: 'Data found for the key', success: true, data: [{ kpiId: 'kpi999', kpiName: 'Total Defect Aging', isEnabled: true, kanban: false }, { kpiId: 'kpi998', kpiName: 'Regression Automation Coverage', isEnabled: true, kanban: true }, { kpiId: 'kpi997', kpiName: 'Total Ticket Aging', isEnabled: true, kanban: true }, { kpiId: 'kpi996', kpiName: 'Unit Testing', isEnabled: true, kanban: true }, { kpiId: 'kpi995', kpiName: 'Code Quality', isEnabled: true, kanban: true }, { kpiId: 'kpi994', kpiName: 'Sonar Violation', isEnabled: true, kanban: true }, { kpiId: 'kpi993', kpiName: 'Sonar Tech Debt', isEnabled: true, kanban: true }, { kpiId: 'kpi992', kpiName: 'Jira Tech Debt', isEnabled: true, kanban: true }, { kpiId: 'kpi991', kpiName: 'Jenkins Code Build Time', isEnabled: true, kanban: true }, { kpiId: 'kpi990', kpiName: 'Number of check-ins per day in master', isEnabled: true, kanban: true }, { kpiId: 'kpi989', kpiName: 'Kpi Maturity', isEnabled: true, kanban: true }, { kpiId: 'kpi988', kpiName: 'Engg Maturity', isEnabled: true, kanban: true }, { kpiId: 'kpi3', kpiName: 'DoR To DoD', isEnabled: true, kanban: false }, { kpiId: 'kpi5', kpiName: 'Sprint Predictability', isEnabled: true, kanban: false }, { kpiId: 'kpi8', kpiName: 'Code Build Time', isEnabled: true, kanban: false }, { kpiId: 'kpi11', kpiName: 'Number of check-ins per day in master', isEnabled: true, kanban: false }, { kpiId: 'kpi14', kpiName: 'Defects Injection Rate', isEnabled: true, kanban: false }, { kpiId: 'kpi15', kpiName: 'Code Quality', isEnabled: true, kanban: false }, { kpiId: 'kpi16', kpiName: 'In-Sprint Automation Coverage', isEnabled: true, kanban: false }, { kpiId: 'kpi17', kpiName: 'Unit Testing', isEnabled: true, kanban: false }, { kpiId: 'kpi26', kpiName: 'Jira Tech Debt', isEnabled: true, kanban: false }, { kpiId: 'kpi27', kpiName: 'Sonar Tech Debt', isEnabled: true, kanban: false }, { kpiId: 'kpi28', kpiName: 'Defect Count By Priority (tagged to Story)', isEnabled: true, kanban: false }, { kpiId: 'kpi34', kpiName: 'Defect Removal Efficiency', isEnabled: true, kanban: false }, { kpiId: 'kpi35', kpiName: 'Defect Seepage Rate', isEnabled: true, kanban: false }, { kpiId: 'kpi36', kpiName: 'Defect Count By RCA (tagged to Story)', isEnabled: true, kanban: false }, { kpiId: 'kpi37', kpiName: 'Defect Rejection Rate', isEnabled: true, kanban: false }, { kpiId: 'kpi38', kpiName: 'Sonar Violations', isEnabled: true, kanban: false }, { kpiId: 'kpi39', kpiName: 'Sprint Velocity', isEnabled: true, kanban: false }, { kpiId: 'kpi40', kpiName: 'Story Count', isEnabled: true, kanban: false }, { kpiId: 'kpi41', kpiName: 'Total Defect Count', isEnabled: true, kanban: false }, { kpiId: 'kpi42', kpiName: 'Regression Automation Coverage', isEnabled: true, kanban: false }, { kpiId: 'kpi43', kpiName: 'Crash Rate', isEnabled: true, kanban: false }, { kpiId: 'kpi46', kpiName: 'Sprint Capacity', isEnabled: true, kanban: false }, { kpiId: 'kpi47', kpiName: 'Throughput', isEnabled: true, kanban: false }, { kpiId: 'kpi48', kpiName: 'Total Ticket Count', isEnabled: true, kanban: true }, { kpiId: 'kpi49', kpiName: 'Ticket Velocity', isEnabled: true, kanban: true }, { kpiId: 'kpi50', kpiName: 'Ticket Count by Priority', isEnabled: true, kanban: true }, { kpiId: 'kpi51', kpiName: 'Ticket Count By RCA', isEnabled: true, kanban: true }, { kpiId: 'kpi52', kpiName: 'Throughput', isEnabled: true, kanban: true }, { kpiId: 'kpi53', kpiName: 'Cycle Time', isEnabled: true, kanban: true }, { kpiId: 'kpi54', kpiName: 'Ticket Open rate by Priority', isEnabled: true, kanban: true }, { kpiId: 'kpi55', kpiName: 'Ticket Re-open rate by Priority', isEnabled: true, kanban: true }, { kpiId: 'kpi56', kpiName: 'Work in Progress vs Closed', isEnabled: true, kanban: true }, { kpiId: 'kpi57', kpiName: 'Ticket Throughput', isEnabled: true, kanban: true }, { kpiId: 'kpi58', kpiName: 'Team Capacity', isEnabled: true, kanban: true }] };

  let httpMock;
  let reqJira;

  const fakeJiraGroupId1 = require('../../../test/resource/fakeJiraGroupId1.json');
  const fakeSonarPayload = require('../../../test/resource/fakeSonarPayload.json');
  const fakeSonarResponse = require('../../../test/resource/fakeSonarResponse.json');
  const fakeJenkinsPayload =require('../../../test/resource/fakeJenkinsPayload.json');
  const fakeJenkinsResponse =require('../../../test/resource/fakeJenkinsResponse.json');
  const fakeZypherPayload =require('../../../test/resource/fakeZypherPayload.json');
  const fakeZypherResponse =require('../../../test/resource/fakeZypherResponse.json');
  const fakeJiraPayload =require('../../../test/resource/fakeJiraPayload.json');
  const fakeBitbucketPayload =require('../../../test/resource/fakeBitbucketPayload.json');
  const fakeBitbucketResponse =require('../../../test/resource/fakeBitBucketResponse.json');

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [MaturityComponent],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
      imports: [FormsModule,
        HttpClientModule,
        RouterTestingModule,
        HttpClientTestingModule],
      providers: [HelperService,
        HttpService,
        // { provide: SharedService, useValue: service },
        SharedService,
        ExcelService,
        DatePipe,
        { provide: APP_CONFIG, useValue: AppConfig }]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MaturityComponent);
    component = fixture.componentInstance;

    // const type = 'Scrum';
    // service.setSelectedType(type);
    // service.selectType(type);
    // service.select(masterData, filterData, filterApplyDataWithNoFilter, selectedTab);
    // service.setDashConfigData(dashConfigData.data);

    // fixture.detectChanges();
    service = TestBed.get(SharedService);
    httpService = TestBed.get(HttpService);
    helperService = TestBed.get(HelperService);
    httpMock = TestBed.get(HttpTestingController);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });



  it('should call kpi grouping methods on receiveing data',()=>{
    const event ={
      masterData :[],
      filterData:[],
      filterApplyData :[],
    };
    const masterData = {
      kpiList :[
        {
          calculateMaturity:true,
          kpiId:'kpi12'
        }
      ]
    };
    const spy = spyOn(service,'getSelectedTab').and.returnValue('Maturity');
    const spyongetMasterData = spyOn(service,'getMasterData').and.returnValue(masterData);
    component.selectedtype ='Scrum';
    // let spyDrawAreaChart =spyOn(component,'drawAreaChart');
    const groupingMethods =['groupJenkinsKpi','groupZypherKpi','groupBitBucketKpi','groupSonarKpi','groupJiraKpi'];
    const spyGroupingMthods =[];
    for(let i=0;i<groupingMethods.length;i++){
     spyGroupingMthods.push(spyOn(component,groupingMethods[i] as any));
    }

    component.receiveSharedData(event);
    // expect(spyDrawAreaChart).toHaveBeenCalled();
    for(let i=0;i<groupingMethods.length;i++){
     expect(spyGroupingMthods[i]).toHaveBeenCalled();
     }
  });

  it('should call receiveshared method on load',()=>{
    const sharedObject ={
      masterData :[],
      filterData:[],
      filterApplyData :[],
    };
    const spy = spyOn(service,'getSelectedType').and.returnValue('Scrum');
    const spygetFilterObject = spyOn(service,'getFilterObject').and.returnValue(sharedObject);
    const spyReceiveSharedData =spyOn(component,'receiveSharedData');
    component.ngOnInit();
    expect(spy).toHaveBeenCalled();
    expect(spygetFilterObject).toHaveBeenCalled();
    expect(spyReceiveSharedData).toHaveBeenCalledWith(sharedObject);
  });


  it('should make post call when kpi available for Sonar for Scrum',()=>{
    const kpiListSonar =[{
      id: '6332dd4b82451128f9939a29',
      kpiId: 'kpi17',
      kpiName: 'Unit Test Coverage'
  }];
    const spy=spyOn(helperService,'groupKpiFromMaster').and.returnValue({kpiList : kpiListSonar});
    const spyMasterData =spyOn(service,'getMasterData').and.returnValue(['kpi17']);
    const postSonarSpy=spyOn(component,'postSonarKpi');
    component.groupSonarKpi(['kpi17']);
    expect(postSonarSpy).toHaveBeenCalled();
  });

  it('should make post call when kpi available for Jenkins for Scrum',()=>{
    const kpiListJenkins =[{
      id: '6332dd4b82451128f9939a29',
      kpiId: 'kpi17',
      kpiName: 'Unit Test Coverage'
  }];
    const spy=spyOn(helperService,'groupKpiFromMaster').and.returnValue({kpiList : kpiListJenkins});
    const spyMasterData =spyOn(service,'getMasterData').and.returnValue(['kpi17']);
    const postJenkinsSpy=spyOn(component,'postJenkinsKpi');
    component.groupJenkinsKpi(['kpi17']);
    expect(postJenkinsSpy).toHaveBeenCalled();
  });

  it('should make post call when kpi available for Zypher for Scrum',()=>{
    const kpiListZypher =[{
      id: '6332dd4b82451128f9939a29',
      kpiId: 'kpi17',
      kpiName: 'Unit Test Coverage'
  }];
    const spy=spyOn(helperService,'groupKpiFromMaster').and.returnValue({kpiList : kpiListZypher});
    const spyMasterData =spyOn(service,'getMasterData').and.returnValue(['kpi17']);
    const postZypherSpy=spyOn(component,'postZypherKpi');
    component.groupZypherKpi(['kpi17']);
    expect(postZypherSpy).toHaveBeenCalled();
  });

  it('should make post call when kpi available for BitBucket for Scrum',()=>{
    const kpiListBitBucket =[{
      id: '6332dd4b82451128f9939a29',
      kpiId: 'kpi17',
      kpiName: 'Unit Test Coverage'
  }];
    const spy=spyOn(helperService,'groupKpiFromMaster').and.returnValue({kpiList : kpiListBitBucket});
    const spyMasterData =spyOn(service,'getMasterData').and.returnValue(['kpi17']);
    const postBitBucketSpy=spyOn(component,'postBitBucketKpi');
    component.groupBitBucketKpi(['kpi17']);
    expect(postBitBucketSpy).toHaveBeenCalled();
  });

  it('should make post call when kpi available for Jira for Scrum',()=>{
    const kpiListJira =[{
      id: '6332dd4b82451128f9939a29',
      kpiId: 'kpi17',
      kpiName: 'Unit Test Coverage'
  }];
  component.masterData ={
    kpiList :[{
      kpiId: 'kpi17',
      kanban:false,
      kpiSource:'Jira',
      groupId:1
    }]
  };
    const spy=spyOn(helperService,'groupKpiFromMaster').and.returnValue({kpiList : kpiListJira});
    const spyMasterData =spyOn(service,'getMasterData').and.returnValue(['kpi17']);
    const postJiraSpy=spyOn(component,'postJiraKpi');
    component.groupJiraKpi(['kpi17']);
    expect(postJiraSpy).toHaveBeenCalled();
  });

  it('should call postSonar',fakeAsync(()=>{
    const sonarKpiData =[
      {
          kpiId: 'kpi17',
          kpiName: 'Unit Test Coverage',
          unit: '%',
          maxValue: '100',
          chartType: '',
          kpiInfo: {
              definition: 'UNIT TEST COVERAGE is a measurement of the amount of code that is run by unit tests - either lines, branches, or methods.',
              formula: [
                  {
                      lhs: 'The calculation is done directly in Sonarqube'
                  }
              ],
              details: [
                  {
                      type: 'paragraph',
                      value: 'It is calculated in ‘Percentage’. Higher the percentage, better is the ‘Quality’'
                  },
                  {
                      type: 'paragraph',
                      value: 'Maturity of the KPI is calculated based on the latest value'
                  },
                  {
                      type: 'paragraph',
                      value: 'A progress indicator shows trend of Unit test coverage between last 2 weeks. An upward trend is considered positive'
                  }
              ],
              maturityLevels: [
                  {
                      level: 'M5',
                      bgColor: '#6cab61',
                      range: '>80%'
                  },
                  {
                      level: 'M4',
                      bgColor: '#AEDB76',
                      range: '60-80%'
                  },
                  {
                      level: 'M3',
                      bgColor: '#eff173',
                      range: '40-60%'
                  },
                  {
                      level: 'M2',
                      bgColor: '#ffc35b',
                      range: '20-40%'
                  },
                  {
                      level: 'M1',
                      bgColor: '#F06667',
                      range: '<20%'
                  }
              ]
          },
          id: '63353ea4087f75e5147b11d0',
          isDeleted: 'False',
          kpiUnit: '%',
          kanban: false,
          kpiSource: 'Sonar',
          thresholdValue: 55,
          trendValueList: [],
          maturityRange: [
              '>=20',
              '20-40',
              '40-60',
              '60-80',
              '80-0'
          ],
          groupId: 1
      }
    ];
    const jenkinsKpiData = [
      {
        kpiId: 'kpi116',
        kpiName: 'Change Failure Rate',
        unit: '%',
        maxValue: '100',
        chartType: '',
        kpiInfo: {
          definition: 'CHANGE FAILURE RATE measures the proportion of builds that have failed for whatever reason over a given period of time',
          formula: [
            {
              lhs: 'CHANGE FAILURE RATE',
              operator: 'division',
              operands: [
                'Total number of failed Builds',
                'Total number of Builds'
              ]
            }
          ],
          details: [
            {
              type: 'paragraph',
              value: 'It is calculated as a ‘Percentage’. Lower the percentage, better is the ‘Quality’'
            },
            {
              type: 'paragraph',
              value: 'A progress indicator shows trend of Change Failure Rate between last 2 weeks. A downward trend is considered positive'
            },
            {
              type: 'paragraph',
              value: 'Maturity of the KPI is calculated based on the average of the last 5 weeks'
            },
            {
              type: 'paragraph',
              value: '*If the KPI data is not available for last 5 weeks, the Maturity level will not be shown'
            }
          ],
          maturityLevels: [
            {
              level: 'M5',
              bgColor: '#6cab61',
              range: '<10%'
            },
            {
              level: 'M4',
              bgColor: '#AEDB76',
              range: '>=10-20%,'
            },
            {
              level: 'M3',
              bgColor: '#eff173',
              range: '>=20-30%'
            },
            {
              level: 'M2',
              bgColor: '#ffc35b',
              range: '>=30-50%'
            },
            {
              level: 'M1',
              bgColor: '#F06667',
              range: '>50%'
            }
          ]
        },
        id: '63355d7c41a0342c3790fb91',
        isDeleted: 'False',
        kpiUnit: '%',
        kanban: false,
        kpiSource: 'Jenkins',
        thresholdValue: 0,
        trendValueList: [],
        maturityRange: [
          '-50',
          '50-30',
          '30-20',
          '20-10',
          '10-'
        ],
        groupId: 1
      }
    ];
    const zypherKpiData = [
      {
        kpiId: 'kpi42',
        kpiName: 'Regression Automation Coverage',
        unit: '%',
        maxValue: '100',
        chartType: '',
        kpiInfo: {
          definition: 'REGRESSION AUTOMATION COVERAGE measures progress of automation of regression test cases',
          formula: [
            {
              lhs: 'Regression Automation Coverage ',
              operator: 'division',
              operands: [
                'No. of regression test cases automated',
                'Total no. of regression test cases'
              ]
            }
          ],
          details: [
            {
              type: 'paragraph',
              value: 'It is calculated as a ‘Percentage’. Higher the percentage, better is the ‘Quality’'
            },
            {
              type: 'paragraph',
              value: 'A progress indicator shows trend of regression automation coverage between last 2 sprints. An upward trend is considered positive'
            },
            {
              type: 'paragraph',
              value: 'Maturity of the KPI is calculated based on the latest value'
            }
          ],
          maturityLevels: [
            {
              level: 'M5',
              bgColor: '#6cab61',
              range: '>= 80%'
            },
            {
              level: 'M4',
              bgColor: '#AEDB76',
              range: '60-80%'
            },
            {
              level: 'M3',
              bgColor: '#eff173',
              range: '40-60%'
            },
            {
              level: 'M2',
              bgColor: '#ffc35b',
              range: '20-40%'
            },
            {
              level: 'M1',
              bgColor: '#F06667',
              range: '< 20%'
            }
          ]
        },
        id: '63355d7c41a0342c3790fb8c',
        isDeleted: 'False',
        kpiUnit: '%',
        kanban: false,
        kpiSource: 'Zypher',
        trendValueList: [],
        maturityRange: [
          '0 - 20',
          '20-40',
          '40-60',
          '60-80',
          '80>='
        ],
        groupId: 1
      }
    ];
    const jiraKpiData = [
      {
        kpiId: 'kpi14',
        kpiName: 'Defect Injection Rate',
        unit: '%',
        maxValue: '200',
        chartType: '',
        kpiInfo: {
          definition: 'DEFECT INJECTION RATE measures the total number of defects (bugs) detected for a story',
          formula: [
            {
              lhs: 'DIR for a sprint',
              operator: 'division',
              operands: [
                'No. of defects tagged to all stories closed in a sprint',
                'Total no. of stories closed in the sprint'
              ]
            }
          ],
          details: [
            {
              type: 'paragraph',
              value: 'It is calculated as a ‘Percentage’. Lower the percentage, better is the ‘Quality’'
            },
            {
              type: 'paragraph',
              value: 'A progress indicator shows trend of defect injection rate between last 2 sprints. A downward trend is considered positive'
            },
            {
              type: 'paragraph',
              value: 'Maturity of the KPI is calculated based on the average of the last 5 sprints'
            },
            {
              type: 'paragraph',
              value: '*If the KPI data is not available for last 5 sprints, the Maturity level will not be shown'
            }
          ],
          maturityLevels: [
            {
              level: 'M5',
              bgColor: '#6cab61',
              range: '25%'
            },
            {
              level: 'M4',
              bgColor: '#AEDB76',
              range: '75-25%'
            },
            {
              level: 'M3',
              bgColor: '#eff173',
              range: '125%-75%'
            },
            {
              level: 'M2',
              bgColor: '#ffc35b',
              range: '175% -125%'
            },
            {
              level: 'M1',
              bgColor: '#F06667',
              range: '> 175%'
            }
          ]
        },
        id: '63355d7c41a0342c3790fb83',
        isDeleted: 'False',
        kpiUnit: '%',
        kanban: false,
        kpiSource: 'Jira',
        thresholdValue: 10,
        trendValueList: [],
        maturityRange: [
          '>=175',
          '175-125',
          '125-75',
          '75-25',
          '25-0'
        ],
        groupId: 2
      }
    ];
    const bitBucketKpiData = [
      {
        kpiId: 'kpi84',
        kpiName: 'Mean Time To Merge',
        unit: 'Hours',
        maxValue: '10',
        chartType: '',
        kpiInfo: {
          definition: 'MEAN TIME TO MERGE measures the efficiency of the code review process in a team',
          details: [
            {
              type: 'paragraph',
              value: 'It is calculated in ‘Hours’. Fewer the Hours better is the ‘Speed’'
            },
            {
              type: 'paragraph',
              value: 'A progress indicator shows trend of Mean time to merge in last 2 weeks. A downward trend is considered positive'
            },
            {
              type: 'paragraph',
              value: 'Maturity of the KPI is calculated based on the average of the last 5 weeks'
            }
          ],
          maturityLevels: [
            {
              level: 'M5',
              bgColor: '#6cab61',
              range: '<4 Hours'
            },
            {
              level: 'M4',
              bgColor: '#AEDB76',
              range: '4-8 Hours'
            },
            {
              level: 'M3',
              bgColor: '#eff173',
              range: '8-16 Hours'
            },
            {
              level: 'M2',
              bgColor: '#ffc35b',
              range: '16-48 Hours'
            },
            {
              level: 'M1',
              bgColor: '#F06667',
              range: '>48 Hours'
            }
          ]
        },
        id: '63355d7c41a0342c3790fb98',
        isDeleted: 'False',
        kpiUnit: 'Hours',
        kanban: false,
        kpiSource: 'BitBucket',
        thresholdValue: 55,
        trendValueList: [
          {
            filter: 'Overall',
            value: [
              {
                data: 'bittest',
                value: [
                  {
                    data: '0',
                    value: 0,
                    hoverValue: {},
                    date: '2022-08-29 to 2022-09-04',
                    sprojectName: 'BITBUCKET_DEMO'
                  },
                  {
                    data: '0',
                    value: 0,
                    hoverValue: {},
                    date: '2022-09-05 to 2022-09-11',
                    sprojectName: 'BITBUCKET_DEMO'
                  },
                  {
                    data: '0',
                    value: 0,
                    hoverValue: {},
                    date: '2022-09-12 to 2022-09-18',
                    sprojectName: 'BITBUCKET_DEMO'
                  },
                  {
                    data: '0',
                    value: 0,
                    hoverValue: {},
                    date: '2022-09-19 to 2022-09-25',
                    sprojectName: 'BITBUCKET_DEMO'
                  },
                  {
                    data: '0',
                    value: 0,
                    hoverValue: {},
                    date: '2022-09-26 to 2022-10-02',
                    sprojectName: 'BITBUCKET_DEMO'
                  }
                ],
                maturity: '5'
              }
            ]
          }
        ],
        maturityRange: [
          '-16',
          '16-8',
          '8-4',
          '4-2',
          '2-'
        ],
        groupId: 1
      }
    ];

  component.sonarKpiRequest  ='';
  component.jenkinsKpiRequest ='';
  component.zypherKpiRequest='';
  component.sonarKpiRequest='';
  component.bitBucketKpiRequest='';
  component.selectedtype = 'Scrum';
  component.noOfJiraGroups =1;
  const spydrawAreaChart =spyOn(component,'drawAreaChart');
  const spyhandleTabChange = spyOn(component,'handleTabChange');
  const sources =['sonar','jenkins','zypher','jira','bitbucket'];
  const postMethods =['postSonarKpi','postJenkinsKpi','postZypherKpi','postJiraKpi','postBitBucketKpi'];
  const fakeResponses = [fakeSonarResponse,fakeJenkinsResponse,fakeZypherResponse,fakeJiraGroupId1,fakeBitbucketResponse];
  const fakePayloads =[fakeSonarPayload,fakeJenkinsPayload,fakeZypherPayload,fakeJiraPayload,fakeBitbucketPayload];
  for(let i=0; i<postMethods.length;i++ ){
   httpService.postKpi = jasmine.createSpy().and.returnValue(of(fakeResponses[i]));
    component[postMethods[i]](fakePayloads[i],sources[i]);
  }

  tick();
  expect(Object.keys(component.sonarKpiData).length).toEqual(sonarKpiData.length);
  expect(Object.keys(component.jenkinsKpiData).length).toEqual(jenkinsKpiData.length);
  expect(Object.keys(component.zypherKpiData).length).toEqual(zypherKpiData.length);
  expect(Object.keys(component.jiraKpiData).length).toEqual(jiraKpiData.length);
  expect(Object.keys(component.bitBucketKpiData).length).toEqual(bitBucketKpiData.length);
  expect(spyhandleTabChange).toHaveBeenCalled();
  }));

  it('should receive data on passDataToDashboard',()=>{
    const spy =spyOn(component,'receiveSharedData');
    service.passDataToDashboard.emit({});
    fixture.detectChanges();
    expect(spy).toHaveBeenCalled();
  });

  it('should set type on TypeRefresh',()=>{
    const spy =spyOn(service,'getSelectedType');
    service.onTypeRefresh.emit('Kanban');
    fixture.detectChanges();
    expect(spy).toHaveBeenCalled();
  });
});
