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

import { IterationComponent } from './iteration.component';
import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpService } from '../../services/http.service';
import { ExcelService } from '../../services/excel.service';
import { SharedService } from '../../services/shared.service';
import { HelperService } from '../../services/helper.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { MultilineComponent } from '../../component/multiline/multiline.component';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { APP_CONFIG, AppConfig } from '../../services/app.config';
import { DatePipe } from '@angular/common';
import { of } from 'rxjs/internal/observable/of';
import { Routes } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { DashboardComponent } from '../dashboard.component';

describe('IterationComponent', () => {
  let component: IterationComponent;
  let fixture: ComponentFixture<IterationComponent>;
  let service: SharedService;
  let httpService: HttpService;
  let helperService: HelperService;
  let httpMock;
  let reqJira;
  const baseUrl = environment.baseUrl;
  const selectedTab = 'Iteration';
  const filterApplyDataWithNoFilter = {};
  const masterData = require('../../../test/resource/masterData.json');
  const filterData = require('../../../test/resource/filterData.json');
  const filterApplyDataWithScrum = { level: 2, label: 'Account', ids: ['CIM', 'FCA'], startDate: '', endDate: '', selectedMap: { Level1: [], Level2: ['CIM', 'FCA'], Level3: [], Project: [], Sprint: [], Build: [], Release: [], Squad: [], Individual: [] } };

  const fakejira = [
    {
        kpiId: 'kpi82',
        kpiName: 'First Time Pass Rate',
        unit: '%',
        maxValue: '100',
        chartType: '',
        kpiInfo: {
            definition: 'FIRST TIME PASS RATE measures the percentage of tickets that pass QA first time (without stimulating a return transition or defect tagged)',
            formula: [
                {
                    lhs: 'First time pass rate (FTPR) for a Sprint',
                    operator: 'division',
                    operands: [
                        'No. of issues closed in a sprint which do not have a return transition or any defects tagged',
                        'Total no. of issues closed in the sprint'
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
                    value: 'A progress indicator shows trend of first time pass rate between last 2 sprints. An upward trend is considered positive'
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
                    range: '>=90%'
                },
                {
                    level: 'M4',
                    bgColor: '#AEDB76',
                    range: '>=75-90%'
                },
                {
                    level: 'M3',
                    bgColor: '#eff173',
                    range: '>=50-75%'
                },
                {
                    level: 'M2',
                    bgColor: '#ffc35b',
                    range: '>=25-50%'
                },
                {
                    level: 'M1',
                    bgColor: '#F06667',
                    range: '< 25%'
                }
            ]
        },
        id: '633545fb9d3ee24be23d2865',
        isDeleted: 'False',
        kpiUnit: '%',
        kanban: false,
        kpiSource: 'Jira',
        trendValueList: [
            {
                data: 'Scrum Project',
                value: [
                    {
                        data: '100',
                        sSprintID: '40203_Scrum Project_6335363749794a18e8a4479b',
                        sSprintName: 'DTS |Test1 |ITR_1|OpenSource_Scrum Project',
                        value: 100,
                        hoverValue: {
                            'FTP Stories': 9,
                            'Closed Stories': 9
                        },
                        sprintIds: [
                            '40203_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'DTS |Test1 |ITR_1|OpenSource_Scrum Project'
                        ],
                        sprojectName: 'Scrum Project'
                    },
                    {
                        data: '67',
                        sSprintID: '38295_Scrum Project_6335363749794a18e8a4479b',
                        sSprintName: 'Test1|PI_10|ITR_5| 24th Aug_Scrum Project',
                        value: 66.67,
                        hoverValue: {
                            'FTP Stories': 2,
                            'Closed Stories': 3
                        },
                        sprintIds: [
                            '38295_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'Test1|PI_10|ITR_5| 24th Aug_Scrum Project'
                        ],
                        sprojectName: 'Scrum Project'
                    },
                    {
                        data: '0',
                        sSprintID: '38294_Scrum Project_6335363749794a18e8a4479b',
                        sSprintName: 'Test1|PI_10|ITR_4| 10th Aug_Scrum Project',
                        value: 0,
                        sprintIds: [
                            '38294_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'Test1|PI_10|ITR_4| 10th Aug_Scrum Project'
                        ],
                        sprojectName: 'Scrum Project'
                    },
                    {
                        data: '100',
                        sSprintID: '38296_Scrum Project_6335363749794a18e8a4479b',
                        sSprintName: 'Test1|PI_10|ITR_6| 07th Sep_Scrum Project',
                        value: 100,
                        hoverValue: {
                            'FTP Stories': 5,
                            'Closed Stories': 5
                        },
                        sprintIds: [
                            '38296_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'Test1|PI_10|ITR_6| 07th Sep_Scrum Project'
                        ],
                        sprojectName: 'Scrum Project'
                    },
                    {
                        data: '70',
                        sSprintID: '40345_Scrum Project_6335363749794a18e8a4479b',
                        sSprintName: 'DTS| Test1|PI_10|Opensource_Scrum Project',
                        value: 70,
                        hoverValue: {
                            'FTP Stories': 14,
                            'Closed Stories': 20
                        },
                        sprintIds: [
                            '40345_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'DTS| Test1|PI_10|Opensource_Scrum Project'
                        ],
                        sprojectName: 'Scrum Project'
                    }
                ],
                maturity: '3'
            }
        ],
        maturityRange: [
            '-25',
            '25-50',
            '50-75',
            '75-90',
            '90-'
        ],
        groupId: 1
    },
    {
        kpiId: 'kpi111',
        kpiName: 'Defect Density',
        unit: '%',
        maxValue: '500',
        chartType: '',
        kpiInfo: {
            definition: 'DEFECT DENSITY measures the total number of defects against the size of a story',
            formula: [
                {
                    lhs: 'Defect Density',
                    operator: 'division',
                    operands: [
                        'No. of defects tagged to all stories closed in a sprint',
                        'Total size of stories closed in the sprint'
                    ]
                }
            ],
            details: [
                {
                    type: 'paragraph',
                    value: 'The KPI is applicable only if the estimation is being done in \'STory Points\''
                },
                {
                    type: 'paragraph',
                    value: 'It is calculated as a ‘Percentage’. Lower the percentage, better is the ‘Quality’'
                },
                {
                    type: 'paragraph',
                    value: 'A progress indicator shows trend of defect density between last 2 sprints. A downward trend is considered positive'
                },
                {
                    type: 'paragraph',
                    value: 'Maturity of the KPI is calculated based on the average of the last 5 sprints'
                },
                {
                    type: 'paragraph',
                    value: 'If the KPI data is not available for last 5 sprints, the Maturity level will not be shown'
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
                    range: '10%-25%'
                },
                {
                    level: 'M3',
                    bgColor: '#eff173',
                    range: '25%-60%'
                },
                {
                    level: 'M2',
                    bgColor: '#ffc35b',
                    range: '60% -90%'
                },
                {
                    level: 'M1',
                    bgColor: '#F06667',
                    range: '>90%'
                }
            ]
        },
        id: '633545fb9d3ee24be23d2866',
        isDeleted: 'False',
        kpiUnit: '%',
        kanban: false,
        kpiSource: 'Jira',
        trendValueList: [
            {
                data: 'Scrum Project',
                value: [
                    {
                        data: '0',
                        sSprintID: '40203_Scrum Project_6335363749794a18e8a4479b',
                        sSprintName: 'DTS |Test1 |ITR_1|OpenSource_Scrum Project',
                        value: 0,
                        hoverValue: {
                            Defects: 0,
                            'Size of Closed Stories': 25
                        },
                        sprintIds: [
                            '40203_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'DTS |Test1 |ITR_1|OpenSource_Scrum Project'
                        ],
                        sprojectName: 'Scrum Project'
                    },
                    {
                        data: '74',
                        sSprintID: '38295_Scrum Project_6335363749794a18e8a4479b',
                        sSprintName: 'Test1|PI_10|ITR_5| 24th Aug_Scrum Project',
                        value: 74.07,
                        hoverValue: {
                            Defects: 11,
                            'Size of Closed Stories': 27
                        },
                        sprintIds: [
                            '38295_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'Test1|PI_10|ITR_5| 24th Aug_Scrum Project'
                        ],
                        sprojectName: 'Scrum Project'
                    },
                    {
                        data: '67',
                        sSprintID: '38294_Scrum Project_6335363749794a18e8a4479b',
                        sSprintName: 'Test1|PI_10|ITR_4| 10th Aug_Scrum Project',
                        value: 66.67,
                        hoverValue: {
                            Defects: 1,
                            'Size of Closed Stories': 3
                        },
                        sprintIds: [
                            '38294_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'Test1|PI_10|ITR_4| 10th Aug_Scrum Project'
                        ],
                        sprojectName: 'Scrum Project'
                    },
                    {
                        data: '13',
                        sSprintID: '38296_Scrum Project_6335363749794a18e8a4479b',
                        sSprintName: 'Test1|PI_10|ITR_6| 07th Sep_Scrum Project',
                        value: 13.33,
                        hoverValue: {
                            Defects: 1,
                            'Size of Closed Stories': 15
                        },
                        sprintIds: [
                            '38296_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'Test1|PI_10|ITR_6| 07th Sep_Scrum Project'
                        ],
                        sprojectName: 'Scrum Project'
                    },
                    {
                        data: '59',
                        sSprintID: '40345_Scrum Project_6335363749794a18e8a4479b',
                        sSprintName: 'DTS| Test1|PI_10|Opensource_Scrum Project',
                        value: 58.54,
                        hoverValue: {
                            Defects: 12,
                            'Size of Closed Stories': 41
                        },
                        sprintIds: [
                            '40345_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'DTS| Test1|PI_10|Opensource_Scrum Project'
                        ],
                        sprojectName: 'Scrum Project'
                    }
                ],
                maturity: '3'
            }
        ],
        maturityRange: [
            '-90',
            '90-60',
            '60-25',
            '25-10',
            '10-'
        ],
        groupId: 1
    },
    {
        kpiId: 'kpi35',
        kpiName: 'Defect Seepage Rate',
        unit: '%',
        maxValue: '100',
        chartType: '',
        kpiInfo: {
            definition: 'DEFECT SEEPAGE RATE measures the percentage of defects leaked from the current testing stage to the subsequent stage',
            formula: [
                {
                    lhs: 'DSR for a sprint',
                    operator: 'division',
                    operands: [
                        'No. of  valid defects reported at a stage (e.g. UAT)',
                        ' Total no. of defects reported in the current stage and previous stage (UAT & QA)'
                    ]
                }
            ],
            details: [
                {
                    type: 'paragraph',
                    value: 'It is calculated as a ‘Percentage’. Lesser the percentage, better is the ‘Quality’'
                },
                {
                    type: 'paragraph',
                    value: 'A progress indicator shows trend of defect seepage rate between last 2 sprints. A downward trend is considered positive'
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
                    range: '<25%'
                },
                {
                    level: 'M4',
                    bgColor: '#AEDB76',
                    range: '>=25-50%'
                },
                {
                    level: 'M3',
                    bgColor: '#eff173',
                    range: '>=50-75%'
                },
                {
                    level: 'M2',
                    bgColor: '#ffc35b',
                    range: '>=75-90%'
                },
                {
                    level: 'M1',
                    bgColor: '#F06667',
                    range: '>=90%'
                }
            ]
        },
        id: '633545fb9d3ee24be23d2867',
        isDeleted: 'False',
        kpiUnit: '%',
        kanban: false,
        kpiSource: 'Jira',
        thresholdValue: 10,
        trendValueList: [
            {
                data: 'Scrum Project',
                value: [
                    {
                        data: '0',
                        sSprintID: '40203_Scrum Project_6335363749794a18e8a4479b',
                        sSprintName: 'DTS |Test1 |ITR_1|OpenSource_Scrum Project',
                        value: 0,
                        hoverValue: {
                            'Escaped Defects': 0,
                            'Total Defects': 0
                        },
                        sprintIds: [
                            '40203_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'DTS |Test1 |ITR_1|OpenSource_Scrum Project'
                        ],
                        sprojectName: 'Scrum Project'
                    },
                    {
                        data: '0',
                        sSprintID: '38295_Scrum Project_6335363749794a18e8a4479b',
                        sSprintName: 'Test1|PI_10|ITR_5| 24th Aug_Scrum Project',
                        value: 0,
                        hoverValue: {
                            'Escaped Defects': 0,
                            'Total Defects': 11
                        },
                        sprintIds: [
                            '38295_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'Test1|PI_10|ITR_5| 24th Aug_Scrum Project'
                        ],
                        sprojectName: 'Scrum Project'
                    },
                    {
                        data: '0',
                        sSprintID: '38294_Scrum Project_6335363749794a18e8a4479b',
                        sSprintName: 'Test1|PI_10|ITR_4| 10th Aug_Scrum Project',
                        value: 0,
                        hoverValue: {
                            'Escaped Defects': 0,
                            'Total Defects': 1
                        },
                        sprintIds: [
                            '38294_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'Test1|PI_10|ITR_4| 10th Aug_Scrum Project'
                        ],
                        sprojectName: 'Scrum Project'
                    },
                    {
                        data: '0',
                        sSprintID: '38296_Scrum Project_6335363749794a18e8a4479b',
                        sSprintName: 'Test1|PI_10|ITR_6| 07th Sep_Scrum Project',
                        value: 0,
                        hoverValue: {
                            'Escaped Defects': 0,
                            'Total Defects': 1
                        },
                        sprintIds: [
                            '38296_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'Test1|PI_10|ITR_6| 07th Sep_Scrum Project'
                        ],
                        sprojectName: 'Scrum Project'
                    },
                    {
                        data: '0',
                        sSprintID: '40345_Scrum Project_6335363749794a18e8a4479b',
                        sSprintName: 'DTS| Test1|PI_10|Opensource_Scrum Project',
                        value: 0,
                        hoverValue: {
                            'Escaped Defects': 0,
                            'Total Defects': 12
                        },
                        sprintIds: [
                            '40345_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'DTS| Test1|PI_10|Opensource_Scrum Project'
                        ],
                        sprojectName: 'Scrum Project'
                    }
                ],
                maturity: '5'
            }
        ],
        maturityRange: [
            '-90',
            '90-75',
            '75-50',
            '50-25',
            '25-'
        ],
        groupId: 1
    },
    {
        kpiId: 'kpi28',
        kpiName: 'Defect Count By Priority',
        unit: 'Number',
        maxValue: '90',
        chartType: '',
        kpiInfo: {
            definition: 'DEFECT COUNT BY PRIORITY measures number of defects for each priority defined in a project',
            formula: [
                {
                    lhs: 'Defect Count By Priority'
                },
                {
                    rhs: 'No. of defects linked to stories grouped by priority'
                }
            ],
            details: [
                {
                    type: 'paragraph',
                    value: 'It is calculated as ‘Count’. Lower the count, better is the ‘Quality’'
                },
                {
                    type: 'paragraph',
                    value: 'A progress indicator shows trend of defect count by priority between last 2 sprints. A downward trend is considered positive'
                }
            ]
        },
        id: '633545fb9d3ee24be23d286a',
        isDeleted: 'False',
        kpiUnit: 'Number',
        kanban: false,
        kpiSource: 'Jira',
        thresholdValue: 55,
        trendValueList: [
            {
                filter: 'Overall',
                value: [
                    {
                        data: 'Scrum Project',
                        value: [
                            {
                                data: '0',
                                sSprintID: '40203_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'DTS |Test1 |ITR_1|OpenSource_Scrum Project',
                                value: 0,
                                hoverValue: {
                                    P2: 0,
                                    P3: 0,
                                    P4: 0
                                },
                                kpiGroup: 'Overall',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '11',
                                sSprintID: '38295_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'Test1|PI_10|ITR_5| 24th Aug_Scrum Project',
                                value: 11,
                                hoverValue: {
                                    P2: 3,
                                    P3: 5,
                                    P4: 3
                                },
                                kpiGroup: 'Overall',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '1',
                                sSprintID: '38294_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'Test1|PI_10|ITR_4| 10th Aug_Scrum Project',
                                value: 1,
                                hoverValue: {
                                    P2: 0,
                                    P3: 1,
                                    P4: 0
                                },
                                kpiGroup: 'Overall',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '1',
                                sSprintID: '38296_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'Test1|PI_10|ITR_6| 07th Sep_Scrum Project',
                                value: 1,
                                hoverValue: {
                                    P2: 0,
                                    P3: 1,
                                    P4: 0
                                },
                                kpiGroup: 'Overall',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '12',
                                sSprintID: '40345_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'DTS| Test1|PI_10|Opensource_Scrum Project',
                                value: 12,
                                hoverValue: {
                                    P2: 0,
                                    P3: 3,
                                    P4: 9
                                },
                                kpiGroup: 'Overall',
                                sprojectName: 'Scrum Project'
                            }
                        ]
                    }
                ]
            },
            {
                filter: 'P2',
                value: [
                    {
                        data: 'Scrum Project',
                        value: [
                            {
                                data: '0',
                                sSprintID: '40203_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'DTS |Test1 |ITR_1|OpenSource_Scrum Project',
                                value: 0,
                                hoverValue: {
                                    P2: 0
                                },
                                kpiGroup: 'P2',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '3',
                                sSprintID: '38295_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'Test1|PI_10|ITR_5| 24th Aug_Scrum Project',
                                value: 3,
                                hoverValue: {
                                    P2: 3
                                },
                                kpiGroup: 'P2',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '0',
                                sSprintID: '38294_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'Test1|PI_10|ITR_4| 10th Aug_Scrum Project',
                                value: 0,
                                hoverValue: {
                                    P2: 0
                                },
                                kpiGroup: 'P2',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '0',
                                sSprintID: '38296_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'Test1|PI_10|ITR_6| 07th Sep_Scrum Project',
                                value: 0,
                                hoverValue: {
                                    P2: 0
                                },
                                kpiGroup: 'P2',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '0',
                                sSprintID: '40345_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'DTS| Test1|PI_10|Opensource_Scrum Project',
                                value: 0,
                                hoverValue: {
                                    P2: 0
                                },
                                kpiGroup: 'P2',
                                sprojectName: 'Scrum Project'
                            }
                        ]
                    }
                ]
            },
            {
                filter: 'P3',
                value: [
                    {
                        data: 'Scrum Project',
                        value: [
                            {
                                data: '0',
                                sSprintID: '40203_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'DTS |Test1 |ITR_1|OpenSource_Scrum Project',
                                value: 0,
                                hoverValue: {
                                    P3: 0
                                },
                                kpiGroup: 'P3',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '5',
                                sSprintID: '38295_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'Test1|PI_10|ITR_5| 24th Aug_Scrum Project',
                                value: 5,
                                hoverValue: {
                                    P3: 5
                                },
                                kpiGroup: 'P3',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '1',
                                sSprintID: '38294_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'Test1|PI_10|ITR_4| 10th Aug_Scrum Project',
                                value: 1,
                                hoverValue: {
                                    P3: 1
                                },
                                kpiGroup: 'P3',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '1',
                                sSprintID: '38296_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'Test1|PI_10|ITR_6| 07th Sep_Scrum Project',
                                value: 1,
                                hoverValue: {
                                    P3: 1
                                },
                                kpiGroup: 'P3',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '3',
                                sSprintID: '40345_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'DTS| Test1|PI_10|Opensource_Scrum Project',
                                value: 3,
                                hoverValue: {
                                    P3: 3
                                },
                                kpiGroup: 'P3',
                                sprojectName: 'Scrum Project'
                            }
                        ]
                    }
                ]
            },
            {
                filter: 'P4',
                value: [
                    {
                        data: 'Scrum Project',
                        value: [
                            {
                                data: '0',
                                sSprintID: '40203_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'DTS |Test1 |ITR_1|OpenSource_Scrum Project',
                                value: 0,
                                hoverValue: {
                                    P4: 0
                                },
                                kpiGroup: 'P4',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '3',
                                sSprintID: '38295_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'Test1|PI_10|ITR_5| 24th Aug_Scrum Project',
                                value: 3,
                                hoverValue: {
                                    P4: 3
                                },
                                kpiGroup: 'P4',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '0',
                                sSprintID: '38294_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'Test1|PI_10|ITR_4| 10th Aug_Scrum Project',
                                value: 0,
                                hoverValue: {
                                    P4: 0
                                },
                                kpiGroup: 'P4',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '0',
                                sSprintID: '38296_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'Test1|PI_10|ITR_6| 07th Sep_Scrum Project',
                                value: 0,
                                hoverValue: {
                                    P4: 0
                                },
                                kpiGroup: 'P4',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '9',
                                sSprintID: '40345_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'DTS| Test1|PI_10|Opensource_Scrum Project',
                                value: 9,
                                hoverValue: {
                                    P4: 9
                                },
                                kpiGroup: 'P4',
                                sprojectName: 'Scrum Project'
                            }
                        ]
                    }
                ]
            }
        ],
        groupId: 1
    },
    {
        kpiId: 'kpi83',
        kpiName: 'Average Resolution Time',
        unit: 'Days',
        maxValue: '100',
        chartType: '',
        kpiInfo: {
            definition: 'AVERAGE RESOLUTION TIME measures average time taken to complete an issue that could be a story or bug etc.',
            formula: [
                {
                    lhs: 'Sum of resolution times of all issues completed in the Sprint/No. of issues completed within a sprint'
                }
            ],
            details: [
                {
                    type: 'paragraph',
                    value: 'It is calculated as a ‘Days’. Fewer the days better is the ‘Speed’'
                },
                {
                    type: 'paragraph',
                    value: 'A progress indicator shows trend of Average Resolution Time between last 2 sprints. A downward trend is considered positive'
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
                    range: '<= 3 days'
                },
                {
                    level: 'M4',
                    bgColor: '#AEDB76',
                    range: '3-5 days'
                },
                {
                    level: 'M3',
                    bgColor: '#eff173',
                    range: '5-8 days'
                },
                {
                    level: 'M2',
                    bgColor: '#ffc35b',
                    range: '8-10 days'
                },
                {
                    level: 'M1',
                    bgColor: '#F06667',
                    range: '=> 10 days'
                }
            ]
        },
        id: '633545fb9d3ee24be23d2878',
        isDeleted: 'False',
        kpiUnit: 'Days',
        kanban: false,
        kpiSource: 'Jira',
        trendValueList: [
            {
                filter: 'Overall',
                value: [
                    {
                        data: 'Scrum Project',
                        value: [
                            {
                                data: '4',
                                sSprintID: '40203_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'DTS |Test1 |ITR_1|OpenSource_Scrum Project',
                                value: 3.67,
                                hoverValue: {},
                                kpiGroup: 'Overall',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '2',
                                sSprintID: '38295_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'Test1|PI_10|ITR_5| 24th Aug_Scrum Project',
                                value: 1.76,
                                hoverValue: {},
                                kpiGroup: 'Overall',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '2',
                                sSprintID: '38294_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'Test1|PI_10|ITR_4| 10th Aug_Scrum Project',
                                value: 1.54,
                                hoverValue: {},
                                kpiGroup: 'Overall',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '3',
                                sSprintID: '38296_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'Test1|PI_10|ITR_6| 07th Sep_Scrum Project',
                                value: 2.62,
                                hoverValue: {},
                                kpiGroup: 'Overall',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '5',
                                sSprintID: '40345_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'DTS| Test1|PI_10|Opensource_Scrum Project',
                                value: 5.18,
                                hoverValue: {},
                                kpiGroup: 'Overall',
                                sprojectName: 'Scrum Project'
                            }
                        ],
                        maturity: '5'
                    }
                ]
            },
            {
                filter: 'Bug',
                value: [
                    {
                        data: 'Scrum Project',
                        value: [
                            {
                                data: '0',
                                sSprintID: '40203_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'DTS |Test1 |ITR_1|OpenSource_Scrum Project',
                                value: 0,
                                hoverValue: {},
                                kpiGroup: 'Bug',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '1',
                                sSprintID: '38295_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'Test1|PI_10|ITR_5| 24th Aug_Scrum Project',
                                value: 1.44,
                                hoverValue: {},
                                kpiGroup: 'Bug',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '2',
                                sSprintID: '38294_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'Test1|PI_10|ITR_4| 10th Aug_Scrum Project',
                                value: 1.54,
                                hoverValue: {},
                                kpiGroup: 'Bug',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '1',
                                sSprintID: '38296_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'Test1|PI_10|ITR_6| 07th Sep_Scrum Project',
                                value: 1.38,
                                hoverValue: {},
                                kpiGroup: 'Bug',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '1',
                                sSprintID: '40345_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'DTS| Test1|PI_10|Opensource_Scrum Project',
                                value: 1.43,
                                hoverValue: {},
                                kpiGroup: 'Bug',
                                sprojectName: 'Scrum Project'
                            }
                        ],
                        maturity: '5'
                    }
                ]
            },
            {
                filter: 'Change request',
                value: [
                    {
                        data: 'Scrum Project',
                        value: [
                            {
                                data: '0',
                                sSprintID: '40203_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'DTS |Test1 |ITR_1|OpenSource_Scrum Project',
                                value: 0,
                                hoverValue: {},
                                kpiGroup: 'Change request',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '0',
                                sSprintID: '38295_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'Test1|PI_10|ITR_5| 24th Aug_Scrum Project',
                                value: 0,
                                hoverValue: {},
                                kpiGroup: 'Change request',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '0',
                                sSprintID: '38294_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'Test1|PI_10|ITR_4| 10th Aug_Scrum Project',
                                value: 0,
                                hoverValue: {},
                                kpiGroup: 'Change request',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '0',
                                sSprintID: '38296_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'Test1|PI_10|ITR_6| 07th Sep_Scrum Project',
                                value: 0,
                                hoverValue: {},
                                kpiGroup: 'Change request',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '0',
                                sSprintID: '40345_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'DTS| Test1|PI_10|Opensource_Scrum Project',
                                value: 0,
                                hoverValue: {},
                                kpiGroup: 'Change request',
                                sprojectName: 'Scrum Project'
                            }
                        ],
                        maturity: '5'
                    }
                ]
            },
            {
                filter: 'Enabler Story',
                value: [
                    {
                        data: 'Scrum Project',
                        value: [
                            {
                                data: '0',
                                sSprintID: '40203_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'DTS |Test1 |ITR_1|OpenSource_Scrum Project',
                                value: 0,
                                hoverValue: {},
                                kpiGroup: 'Enabler Story',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '8',
                                sSprintID: '38295_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'Test1|PI_10|ITR_5| 24th Aug_Scrum Project',
                                value: 8,
                                hoverValue: {},
                                kpiGroup: 'Enabler Story',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '0',
                                sSprintID: '38294_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'Test1|PI_10|ITR_4| 10th Aug_Scrum Project',
                                value: 0,
                                hoverValue: {},
                                kpiGroup: 'Enabler Story',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '0',
                                sSprintID: '38296_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'Test1|PI_10|ITR_6| 07th Sep_Scrum Project',
                                value: 0,
                                hoverValue: {},
                                kpiGroup: 'Enabler Story',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '0',
                                sSprintID: '40345_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'DTS| Test1|PI_10|Opensource_Scrum Project',
                                value: 0,
                                hoverValue: {},
                                kpiGroup: 'Enabler Story',
                                sprojectName: 'Scrum Project'
                            }
                        ],
                        maturity: '5'
                    }
                ]
            },
            {
                filter: 'Epic',
                value: [
                    {
                        data: 'Scrum Project',
                        value: [
                            {
                                data: '0',
                                sSprintID: '40203_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'DTS |Test1 |ITR_1|OpenSource_Scrum Project',
                                value: 0,
                                hoverValue: {},
                                kpiGroup: 'Epic',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '0',
                                sSprintID: '38295_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'Test1|PI_10|ITR_5| 24th Aug_Scrum Project',
                                value: 0,
                                hoverValue: {},
                                kpiGroup: 'Epic',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '0',
                                sSprintID: '38294_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'Test1|PI_10|ITR_4| 10th Aug_Scrum Project',
                                value: 0,
                                hoverValue: {},
                                kpiGroup: 'Epic',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '0',
                                sSprintID: '38296_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'Test1|PI_10|ITR_6| 07th Sep_Scrum Project',
                                value: 0,
                                hoverValue: {},
                                kpiGroup: 'Epic',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '0',
                                sSprintID: '40345_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'DTS| Test1|PI_10|Opensource_Scrum Project',
                                value: 0,
                                hoverValue: {},
                                kpiGroup: 'Epic',
                                sprojectName: 'Scrum Project'
                            }
                        ],
                        maturity: '5'
                    }
                ]
            },
            {
                filter: 'Story',
                value: [
                    {
                        data: 'Scrum Project',
                        value: [
                            {
                                data: '4',
                                sSprintID: '40203_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'DTS |Test1 |ITR_1|OpenSource_Scrum Project',
                                value: 3.67,
                                hoverValue: {},
                                kpiGroup: 'Story',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '4',
                                sSprintID: '38295_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'Test1|PI_10|ITR_5| 24th Aug_Scrum Project',
                                value: 4,
                                hoverValue: {},
                                kpiGroup: 'Story',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '0',
                                sSprintID: '38294_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'Test1|PI_10|ITR_4| 10th Aug_Scrum Project',
                                value: 0,
                                hoverValue: {},
                                kpiGroup: 'Story',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '5',
                                sSprintID: '38296_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'Test1|PI_10|ITR_6| 07th Sep_Scrum Project',
                                value: 4.6,
                                hoverValue: {},
                                kpiGroup: 'Story',
                                sprojectName: 'Scrum Project'
                            },
                            {
                                data: '8',
                                sSprintID: '40345_Scrum Project_6335363749794a18e8a4479b',
                                sSprintName: 'DTS| Test1|PI_10|Opensource_Scrum Project',
                                value: 7.8,
                                hoverValue: {},
                                kpiGroup: 'Story',
                                sprojectName: 'Scrum Project'
                            }
                        ],
                        maturity: '4'
                    }
                ]
            }
        ],
        maturityRange: [
            '-10',
            '10-8',
            '8-5',
            '5-3',
            '3-'
        ],
        groupId: 1
    },
    {
        kpiId: 'kpi126',
        kpiName: 'Created vs Resolved defects',
        unit: 'Number',
        maxValue: '300',
        chartType: '',
        kpiInfo: {
            definition: 'Created vs Resolved defects gives a view of closed defects in an iteration vs planned + added defects in the iteration. The aim is to close all the defects that are in the iteration.',
            details: [
                {
                    type: 'paragraph',
                    value: 'If the No. of defects resolved are equal to the No. of defects created in the latest sprint, the KPI is considered having a positive trend.'
                }
            ]
        },
        id: '633545fb9d3ee24be23d286c',
        isDeleted: 'False',
        kpiUnit: 'Number',
        kanban: false,
        kpiSource: 'Jira',
        trendValueList: [
            {
                data: 'Scrum Project',
                value: [
                    {
                        data: '1',
                        sSprintID: '40203_Scrum Project_6335363749794a18e8a4479b',
                        sSprintName: 'DTS |Test1 |ITR_1|OpenSource_Scrum Project',
                        value: 1,
                        hoverValue: {
                            resolvedDefects: 0,
                            createdDefects: 1
                        },
                        sprintIds: [
                            '40203_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'DTS |Test1 |ITR_1|OpenSource_Scrum Project'
                        ],
                        lineValue: 0,
                        sprojectName: 'Scrum Project'
                    },
                    {
                        data: '31',
                        sSprintID: '38295_Scrum Project_6335363749794a18e8a4479b',
                        sSprintName: 'Test1|PI_10|ITR_5| 24th Aug_Scrum Project',
                        value: 31,
                        hoverValue: {
                            resolvedDefects: 29,
                            createdDefects: 31
                        },
                        sprintIds: [
                            '38295_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'Test1|PI_10|ITR_5| 24th Aug_Scrum Project'
                        ],
                        lineValue: 29,
                        sprojectName: 'Scrum Project'
                    },
                    {
                        data: '30',
                        sSprintID: '38294_Scrum Project_6335363749794a18e8a4479b',
                        sSprintName: 'Test1|PI_10|ITR_4| 10th Aug_Scrum Project',
                        value: 30,
                        hoverValue: {
                            resolvedDefects: 22,
                            createdDefects: 30
                        },
                        sprintIds: [
                            '38294_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'Test1|PI_10|ITR_4| 10th Aug_Scrum Project'
                        ],
                        lineValue: 22,
                        sprojectName: 'Scrum Project'
                    },
                    {
                        data: '10',
                        sSprintID: '38296_Scrum Project_6335363749794a18e8a4479b',
                        sSprintName: 'Test1|PI_10|ITR_6| 07th Sep_Scrum Project',
                        value: 10,
                        hoverValue: {
                            resolvedDefects: 7,
                            createdDefects: 10
                        },
                        sprintIds: [
                            '38296_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'Test1|PI_10|ITR_6| 07th Sep_Scrum Project'
                        ],
                        lineValue: 7,
                        sprojectName: 'Scrum Project'
                    },
                    {
                        data: '20',
                        sSprintID: '40345_Scrum Project_6335363749794a18e8a4479b',
                        sSprintName: 'DTS| Test1|PI_10|Opensource_Scrum Project',
                        value: 20,
                        hoverValue: {
                            resolvedDefects: 14,
                            createdDefects: 20
                        },
                        sprintIds: [
                            '40345_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'DTS| Test1|PI_10|Opensource_Scrum Project'
                        ],
                        lineValue: 14,
                        sprojectName: 'Scrum Project'
                    }
                ]
            }
        ],
        groupId: 1
    },
    {
        kpiId: 'kpi46',
        kpiName: 'Sprint Capacity Utilization',
        unit: 'Hours',
        maxValue: '500',
        chartType: '',
        kpiInfo: {
            definition: 'SPRINT CAPACITY UTILIZATION depicts the maximum amount of time a team can commit within sprint',
            details: [
                {
                    type: 'paragraph',
                    value: 'This KPI is calculated based on 2 parameters'
                },
                {
                    type: 'paragraph',
                    value: 'Estimated Hours: It explains the total hours required to complete Sprint backlog'
                },
                {
                    type: 'paragraph',
                    value: 'Logged Work: The amount of time team has logged within a Sprint'
                }
            ]
        },
        id: '633545fb9d3ee24be23d2877',
        isDeleted: 'False',
        kpiUnit: 'Hours',
        kanban: false,
        kpiSource: 'Jira',
        trendValueList: [
            {
                data: 'Scrum Project',
                value: [
                    {
                        data: '0.0',
                        sSprintID: '40203_Scrum Project_6335363749794a18e8a4479b',
                        sSprintName: 'DTS |Test1 |ITR_1|OpenSource_Scrum Project',
                        value: 0,
                        hoverValue: {
                            'Estimated Hours': 0,
                            'Logged Work': 68
                        },
                        sprintIds: [
                            '40203_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'DTS |Test1 |ITR_1|OpenSource_Scrum Project'
                        ],
                        lineValue: 68,
                        sprojectName: 'Scrum Project'
                    },
                    {
                        data: '20.0',
                        sSprintID: '38295_Scrum Project_6335363749794a18e8a4479b',
                        sSprintName: 'Test1|PI_10|ITR_5| 24th Aug_Scrum Project',
                        value: 20,
                        hoverValue: {
                            'Estimated Hours': 20,
                            'Logged Work': 189
                        },
                        sprintIds: [
                            '38295_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'Test1|PI_10|ITR_5| 24th Aug_Scrum Project'
                        ],
                        lineValue: 189,
                        sprojectName: 'Scrum Project'
                    },
                    {
                        data: '0.0',
                        sSprintID: '38294_Scrum Project_6335363749794a18e8a4479b',
                        sSprintName: 'Test1|PI_10|ITR_4| 10th Aug_Scrum Project',
                        value: 0,
                        hoverValue: {
                            'Estimated Hours': 0,
                            'Logged Work': 1
                        },
                        sprintIds: [
                            '38294_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'Test1|PI_10|ITR_4| 10th Aug_Scrum Project'
                        ],
                        lineValue: 1,
                        sprojectName: 'Scrum Project'
                    },
                    {
                        data: '40.0',
                        sSprintID: '38296_Scrum Project_6335363749794a18e8a4479b',
                        sSprintName: 'Test1|PI_10|ITR_6| 07th Sep_Scrum Project',
                        value: 40,
                        hoverValue: {
                            'Estimated Hours': 40,
                            'Logged Work': 57
                        },
                        sprintIds: [
                            '38296_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'Test1|PI_10|ITR_6| 07th Sep_Scrum Project'
                        ],
                        lineValue: 57,
                        sprojectName: 'Scrum Project'
                    },
                    {
                        data: '50.0',
                        sSprintID: '40345_Scrum Project_6335363749794a18e8a4479b',
                        sSprintName: 'DTS| Test1|PI_10|Opensource_Scrum Project',
                        value: 50,
                        hoverValue: {
                            'Estimated Hours': 50,
                            'Logged Work': 139
                        },
                        sprintIds: [
                            '40345_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'DTS| Test1|PI_10|Opensource_Scrum Project'
                        ],
                        lineValue: 139,
                        sprojectName: 'Scrum Project'
                    }
                ]
            }
        ],
        groupId: 1
    },
    {
        kpiId: 'kpi40',
        kpiName: 'Story Count',
        unit: 'Stories',
        maxValue: '',
        chartType: '',
        kpiInfo: {
            definition: 'STORY COUNT measures the overall work taken in a sprint',
            formula: [
                {
                    lhs: 'No. of stories tagged to a Sprint'
                }
            ]
        },
        id: '633545fb9d3ee24be23d2874',
        isDeleted: 'False',
        kpiUnit: 'Stories',
        kanban: false,
        kpiSource: 'Jira',
        trendValueList: [
            {
                data: 'Scrum Project',
                value: [
                    {
                        data: '22.0',
                        sSprintID: '40203_Scrum Project_6335363749794a18e8a4479b',
                        sSprintName: 'DTS |Test1 |ITR_1|OpenSource_Scrum Project',
                        value: 22,
                        hoverValue: {},
                        sprintIds: [
                            '40203_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'DTS |Test1 |ITR_1|OpenSource_Scrum Project'
                        ],
                        sprojectName: 'Scrum Project'
                    },
                    {
                        data: '16.0',
                        sSprintID: '38295_Scrum Project_6335363749794a18e8a4479b',
                        sSprintName: 'Test1|PI_10|ITR_5| 24th Aug_Scrum Project',
                        value: 16,
                        hoverValue: {},
                        sprintIds: [
                            '38295_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'Test1|PI_10|ITR_5| 24th Aug_Scrum Project'
                        ],
                        sprojectName: 'Scrum Project'
                    },
                    {
                        data: '11.0',
                        sSprintID: '38294_Scrum Project_6335363749794a18e8a4479b',
                        sSprintName: 'Test1|PI_10|ITR_4| 10th Aug_Scrum Project',
                        value: 11,
                        hoverValue: {},
                        sprintIds: [
                            '38294_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'Test1|PI_10|ITR_4| 10th Aug_Scrum Project'
                        ],
                        sprojectName: 'Scrum Project'
                    },
                    {
                        data: '14.0',
                        sSprintID: '38296_Scrum Project_6335363749794a18e8a4479b',
                        sSprintName: 'Test1|PI_10|ITR_6| 07th Sep_Scrum Project',
                        value: 14,
                        hoverValue: {},
                        sprintIds: [
                            '38296_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'Test1|PI_10|ITR_6| 07th Sep_Scrum Project'
                        ],
                        sprojectName: 'Scrum Project'
                    },
                    {
                        data: '33.0',
                        sSprintID: '40345_Scrum Project_6335363749794a18e8a4479b',
                        sSprintName: 'DTS| Test1|PI_10|Opensource_Scrum Project',
                        value: 33,
                        hoverValue: {},
                        sprintIds: [
                            '40345_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'DTS| Test1|PI_10|Opensource_Scrum Project'
                        ],
                        sprojectName: 'Scrum Project'
                    }
                ]
            }
        ],
        groupId: 1
    },
    {
        kpiId: 'kpi39',
        kpiName: 'Sprint Velocity',
        unit: 'SP',
        maxValue: '300',
        chartType: '',
        kpiInfo: {
            definition: 'SPRINT VELOCITY measures the rate at which a team can deliver every Sprint',
            formula: [
                {
                    lhs: 'Sum of story points of all stories completed within a Sprint'
                }
            ]
        },
        id: '633545fb9d3ee24be23d2876',
        isDeleted: 'False',
        kpiUnit: 'SP',
        kanban: false,
        kpiSource: 'Jira',
        trendValueList: [
            {
                data: 'Scrum Project',
                value: [
                    {
                        data: '61',
                        sSprintID: '40203_Scrum Project_6335363749794a18e8a4479b',
                        sSprintName: 'DTS |Test1 |ITR_1|OpenSource_Scrum Project',
                        value: 61,
                        hoverValue: {},
                        sprintIds: [
                            '40203_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'DTS |Test1 |ITR_1|OpenSource_Scrum Project'
                        ],
                        sprojectName: 'Scrum Project'
                    },
                    {
                        data: '45',
                        sSprintID: '38295_Scrum Project_6335363749794a18e8a4479b',
                        sSprintName: 'Test1|PI_10|ITR_5| 24th Aug_Scrum Project',
                        value: 45,
                        hoverValue: {},
                        sprintIds: [
                            '38295_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'Test1|PI_10|ITR_5| 24th Aug_Scrum Project'
                        ],
                        sprojectName: 'Scrum Project'
                    },
                    {
                        data: '23',
                        sSprintID: '38294_Scrum Project_6335363749794a18e8a4479b',
                        sSprintName: 'Test1|PI_10|ITR_4| 10th Aug_Scrum Project',
                        value: 23,
                        hoverValue: {},
                        sprintIds: [
                            '38294_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'Test1|PI_10|ITR_4| 10th Aug_Scrum Project'
                        ],
                        sprojectName: 'Scrum Project'
                    },
                    {
                        data: '22',
                        sSprintID: '38296_Scrum Project_6335363749794a18e8a4479b',
                        sSprintName: 'Test1|PI_10|ITR_6| 07th Sep_Scrum Project',
                        value: 22,
                        hoverValue: {},
                        sprintIds: [
                            '38296_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'Test1|PI_10|ITR_6| 07th Sep_Scrum Project'
                        ],
                        sprojectName: 'Scrum Project'
                    },
                    {
                        data: '116',
                        sSprintID: '40345_Scrum Project_6335363749794a18e8a4479b',
                        sSprintName: 'DTS| Test1|PI_10|Opensource_Scrum Project',
                        value: 116,
                        hoverValue: {},
                        sprintIds: [
                            '40345_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'DTS| Test1|PI_10|Opensource_Scrum Project'
                        ],
                        sprojectName: 'Scrum Project'
                    }
                ]
            }
        ],
        groupId: 1
    }
];
  const fakeJiraGroupId1 = require('../../../test/resource/fakeJiraGroupId1.json');

  const fakeJiraNoFilter = [{ kpiId: 'kpi81', kpiName: 'Stories Without Estimate', value: [{ data: 'Total Issues', count: 12 }, { data: 'Issues Without Estimate', count: 0 }], unit: '%', maxValue: '500', chartType: '', id: '605e281c3a5c2e37eedfef72', isDeleted: 'False', kpiCategory: 'Iteration', kpiUnit: '%', kanban: false, kpiSource: 'Jira', trendValueList: [{ data: 'Test1Project', value: [{ data: '0', sSprintID: '29752_Test1Project_605af167f0fb3000015e6358', sSprintName: 'PS HOW |PI_4|ITR_6|10_Mar_Test1Project', howerValue: { 'Total Issues': 12, 'Issues Without Estimate': 0 }, startDate: '10-Mar-2021', endDate: '24-Mar-2021', sprojectName: 'Test1Project' }] }], groupId: 8 }, { kpiId: 'kpi115', kpiName: 'Missing Work Logs', value: [{ data: 'Total Issues(excluding open and dropped)', count: 12 }, { data: 'Issues Without Worklog', count: 12 }, { data: 'Total Issues(including open and dropped)', count: 12 }], unit: '%', maxValue: '500', chartType: '', id: '605e281c3a5c2e37eedfef6a', isDeleted: 'False', kpiCategory: 'Iteration', kpiUnit: '%', kanban: false, kpiSource: 'Jira', trendValueList: [{ data: 'Test1Project', value: [{ data: '100', sSprintID: '29752_Test1Project_605af167f0fb3000015e6358', sSprintName: 'PS HOW |PI_4|ITR_6|10_Mar_Test1Project', value: 100, howerValue: { 'Total Issues(excluding open and dropped)': 12, 'Issues Without Worklog': 12, 'Total Issues(including open and dropped)': 12 }, startDate: '10-Mar-2021', endDate: '24-Mar-2021', sprojectName: 'Test1Project' }] }], groupId: 8 }, { kpiId: 'kpi75', kpiName: 'Estimate vs Actual', value: [{ data: 'Estimated Effort (in hours)', count: 288, value: '100.0%' }, { data: 'Actual Effort (in hours)', count: 0, value: '0.0%' }], unit: '%', maxValue: '', chartType: '', id: '605e281c3a5c2e37eedfef6c', isDeleted: 'False', kpiCategory: 'Iteration', kpiUnit: '%', kanban: false, kpiSource: 'Jira', trendValueList: [{ data: 'Test1Project', value: [{ data: '0', sSprintID: '29752_Test1Project_605af167f0fb3000015e6358', sSprintName: 'PS HOW |PI_4|ITR_6|10_Mar_Test1Project', howerMap: { 'Estimated Effort (in hours)': [288, 100], 'Actual Effort (in hours)': [0, 0] }, startDate: '10-Mar-2021', endDate: '24-Mar-2021', sprojectName: 'Test1Project' }] }], groupId: 8 }, { kpiId: 'kpi76', kpiName: 'Story Completion', value: [{ data: 'Completed Issues', count: 7 }, { data: 'Total Issues', count: 12 }], unit: '%', maxValue: '500', chartType: '', id: '605e281c3a5c2e37eedfef6d', isDeleted: 'False', kpiCategory: 'Iteration', kpiUnit: '%', kanban: false, kpiSource: 'Jira', trendValueList: [{ data: 'Test1Project', value: [{ data: '58', sSprintID: '29752_Test1Project_605af167f0fb3000015e6358', sSprintName: 'PS HOW |PI_4|ITR_6|10_Mar_Test1Project', value: 58.333333333333336, howerValue: { 'Completed Issues': 7, 'Total Issues': 12 }, startDate: '10-Mar-2021', endDate: '24-Mar-2021', sprojectName: 'Test1Project' }] }], groupId: 8 }, { kpiId: 'kpi77', kpiName: 'Sprint Velocity (LOE Based)', value: [{ data: 'Team Logged efforts (in hours)', count: 0 }, { data: 'Delivered Story Points', count: 24 }], unit: '', maxValue: '500', chartType: '', id: '605e281c3a5c2e37eedfef6e', isDeleted: 'False', kpiCategory: 'Iteration', kpiUnit: '', kanban: false, kpiSource: 'Jira', trendValueList: [{ data: 'Test1Project', value: [{ data: '0', sSprintID: '29752_Test1Project_605af167f0fb3000015e6358', sSprintName: 'PS HOW |PI_4|ITR_6|10_Mar_Test1Project', value: 0, howerValue: { 'Team Logged efforts (in hours)': 0, 'Delivered Story Points': 24 }, startDate: '10-Mar-2021', endDate: '24-Mar-2021', sprojectName: 'Test1Project' }] }], groupId: 8 }, { kpiId: 'kpi78', kpiName: 'Actual vs Remaining', value: [{ data: 'Actual Effort (in hours)', count: 0, value: '0.0%' }, { data: 'Remaining Effort (in hours)', count: 288, value: '100.0%' }], unit: '%', maxValue: '500', chartType: '', id: '605e281c3a5c2e37eedfef6f', isDeleted: 'False', kpiCategory: 'Iteration', kpiUnit: '%', kanban: false, kpiSource: 'Jira', trendValueList: [{ data: 'Test1Project', value: [{ data: '100', sSprintID: '29752_Test1Project_605af167f0fb3000015e6358', sSprintName: 'PS HOW |PI_4|ITR_6|10_Mar_Test1Project', howerMap: { 'Actual Effort (in hours)': [0, 0], 'Remaining Effort (in hours)': [288, 100] }, startDate: '10-Mar-2021', endDate: '24-Mar-2021', sprojectName: 'Test1Project' }] }], groupId: 8 }, { kpiId: 'kpi80', kpiName: 'Defect Count Without Story Link', value: 698, unit: '', maxValue: '500', chartType: '', id: '605e281c3a5c2e37eedfef71', isDeleted: 'False', kpiCategory: 'Iteration', kpiUnit: '', kanban: false, kpiSource: 'Jira', trendValueList: [{ data: 'Test1Project', value: [{ data: '698', value: 698, howerValue: { 'Total Defects': 1334, 'Defects Without Story Link': 698 }, sprojectName: 'Test1Project' }] }], groupId: 8 }];

  const dashConfigData = { message: 'Data found for the key', success: true, data: [{ kpiId: 'kpi999', kpiName: 'Total Defect Aging', isEnabled: true, kanban: false }, { kpiId: 'kpi998', kpiName: 'Regression Automation Coverage', isEnabled: true, kanban: true }, { kpiId: 'kpi997', kpiName: 'Total Ticket Aging', isEnabled: true, kanban: true }, { kpiId: 'kpi996', kpiName: 'Unit Testing', isEnabled: true, kanban: true }, { kpiId: 'kpi995', kpiName: 'Code Quality', isEnabled: true, kanban: true }, { kpiId: 'kpi994', kpiName: 'Sonar Violation', isEnabled: true, kanban: true }, { kpiId: 'kpi993', kpiName: 'Sonar Tech Debt', isEnabled: true, kanban: true }, { kpiId: 'kpi992', kpiName: 'Jira Tech Debt', isEnabled: true, kanban: true }, { kpiId: 'kpi991', kpiName: 'Jenkins Code Build Time', isEnabled: true, kanban: true }, { kpiId: 'kpi990', kpiName: 'Number of check-ins per day in master', isEnabled: true, kanban: true }, { kpiId: 'kpi989', kpiName: 'Kpi Maturity', isEnabled: true, kanban: true }, { kpiId: 'kpi988', kpiName: 'Engg Maturity', isEnabled: true, kanban: true }, { kpiId: 'kpi3', kpiName: 'DoR To DoD', isEnabled: true, kanban: false }, { kpiId: 'kpi5', kpiName: 'Sprint Predictability', isEnabled: true, kanban: false }, { kpiId: 'kpi8', kpiName: 'Code Build Time', isEnabled: true, kanban: false }, { kpiId: 'kpi11', kpiName: 'Number of check-ins per day in master', isEnabled: true, kanban: false }, { kpiId: 'kpi14', kpiName: 'Defects Injection Rate', isEnabled: true, kanban: false }, { kpiId: 'kpi15', kpiName: 'Code Quality', isEnabled: true, kanban: false }, { kpiId: 'kpi16', kpiName: 'In-Sprint Automation Coverage', isEnabled: true, kanban: false }, { kpiId: 'kpi17', kpiName: 'Unit Testing', isEnabled: true, kanban: false }, { kpiId: 'kpi26', kpiName: 'Jira Tech Debt', isEnabled: true, kanban: false }, { kpiId: 'kpi27', kpiName: 'Sonar Tech Debt', isEnabled: true, kanban: false }, { kpiId: 'kpi28', kpiName: 'Defect Count By Priority (tagged to Story)', isEnabled: true, kanban: false }, { kpiId: 'kpi34', kpiName: 'Defect Removal Efficiency', isEnabled: true, kanban: false }, { kpiId: 'kpi35', kpiName: 'Defect Seepage Rate', isEnabled: true, kanban: false }, { kpiId: 'kpi36', kpiName: 'Defect Count By RCA (tagged to Story)', isEnabled: true, kanban: false }, { kpiId: 'kpi37', kpiName: 'Defect Rejection Rate', isEnabled: true, kanban: false }, { kpiId: 'kpi38', kpiName: 'Sonar Violations', isEnabled: true, kanban: false }, { kpiId: 'kpi39', kpiName: 'Sprint Velocity', isEnabled: true, kanban: false }, { kpiId: 'kpi40', kpiName: 'Story Count', isEnabled: true, kanban: false }, { kpiId: 'kpi41', kpiName: 'Total Defect Count', isEnabled: true, kanban: false }, { kpiId: 'kpi42', kpiName: 'Regression Automation Coverage', isEnabled: true, kanban: false }, { kpiId: 'kpi43', kpiName: 'Crash Rate', isEnabled: true, kanban: false }, { kpiId: 'kpi46', kpiName: 'Sprint Capacity', isEnabled: true, kanban: false }, { kpiId: 'kpi47', kpiName: 'Throughput', isEnabled: true, kanban: false }, { kpiId: 'kpi48', kpiName: 'Total Ticket Count', isEnabled: true, kanban: true }, { kpiId: 'kpi49', kpiName: 'Ticket Velocity', isEnabled: true, kanban: true }, { kpiId: 'kpi50', kpiName: 'Ticket Count by Priority', isEnabled: true, kanban: true }, { kpiId: 'kpi51', kpiName: 'Ticket Count By RCA', isEnabled: true, kanban: true }, { kpiId: 'kpi52', kpiName: 'Throughput', isEnabled: true, kanban: true }, { kpiId: 'kpi53', kpiName: 'Cycle Time', isEnabled: true, kanban: true }, { kpiId: 'kpi54', kpiName: 'Ticket Open rate by Priority', isEnabled: true, kanban: true }, { kpiId: 'kpi55', kpiName: 'Ticket Re-open rate by Priority', isEnabled: true, kanban: true }, { kpiId: 'kpi56', kpiName: 'Work in Progress vs Closed', isEnabled: true, kanban: true }, { kpiId: 'kpi57', kpiName: 'Ticket Throughput', isEnabled: true, kanban: true }, { kpiId: 'kpi58', kpiName: 'Team Capacity', isEnabled: true, kanban: true }] };
  const routes: Routes = [
    { path: 'dashboard', component: DashboardComponent },
    { path: 'authentication/login', component: DashboardComponent }

  ];
  const userConfigData =require('../../../test/resource/fakeGlobalConfigData.json');
  const configGlobalData = [
    {
      kpiId: 'kpi74',
      kpiName: 'Release Frequency',
      isEnabled: true,
      order: 1,
      kpiDetail: {
        id: '63320976b7f239ac93c2686a',
        kpiId: 'kpi74',
        kpiName: 'Release Frequency',
        isDeleted: 'False',
        defaultOrder: 17,
        kpiUnit: '',
        chartType: 'line',
        showTrend: true,
        isPositiveTrend: true,
        calculateMaturity: false,
        kpiSource: 'Jira',
        maxValue: '300',
        kanban: true,
        groupId: 4,
        kpiInfo: {
          definition: 'Release Frequency highlights the number of releases done in a month',
          formula: [
            {
              lhs: 'Release Frequency for a month',
              rhs: 'Number of fix versions in JIRA for a project that have a release date falling in a particular month'
            }
          ],
          details: [
            {
              type: 'paragraph',
              value: 'It is calculated as a ‘Count’. Higher the Release Frequency, more valuable it is for the Business or a Project'
            },
            {
              type: 'paragraph',
              value: 'A progress indicator shows trend of Release Frequency between last 2 months. An upward trend is considered positive'
            }
          ]
        },
        aggregationCriteria: 'sum',
        trendCalculative: false,
        squadSupport: false,
        xaxisLabel: 'Months',
        yaxisLabel: 'Count'
      },
      shown: true
    }
  ];

  const fakeJiraPayload =require('../../../test/resource/fakeJiraPayload.json');

  beforeEach  (() => {
    service = new SharedService();
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        RouterTestingModule.withRoutes(routes),
      ],
      declarations: [IterationComponent,
        MultilineComponent, DashboardComponent],
      providers: [
        HelperService,
        { provide: APP_CONFIG, useValue: AppConfig },
        HttpService,
        { provide: SharedService, useValue: service }
        , ExcelService, DatePipe

      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]

    })
      .compileComponents();
    service = TestBed.inject(SharedService);
    httpService = TestBed.inject(HttpService);
    helperService = TestBed.inject(HelperService);

    spyOn(helperService, 'colorAccToMaturity').and.returnValue(('#44739f'));
    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(IterationComponent);
    component = fixture.componentInstance;
    // We set the expectations for the HttpClient mock
    reqJira = httpMock.match((request) => request.url);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('check whether scrum', (done) => {
    const type = 'Scrum';
    component.getSelectedType(type);
    component.selectedtype = 'Scrum';
    fixture.detectChanges();
    expect(component.selectedtype).toBe(type);
    done();

  });

  // it('download excel functionality for Overall Iteration Progress count', () => {
  //   spyOn(helperService, 'downloadExcel').and.returnValue(of(''));
  //   component.downloadExcel('kpi76', 'Overall Iteration Progress Count', false);
  // });

  xit('Scrum with filter applied', (done) => {
    const type = 'Scrum';
    service.setSelectedType(type);
    // component.selectedtype = 'Scrum';
    console.log('Selected Type ====> ', component.selectedtype);
    service.select(masterData, filterData, filterApplyDataWithScrum, selectedTab);
    fixture.detectChanges();
    httpMock.match(baseUrl + '/api/jira/kpi')[0].flush(fakejira);
    expect(component.selectedtype).toBe(type);
    done();
  });


  it('should process kpi config Data',()=>{
    component.configGlobalData =  configGlobalData;
    component.processKpiConfigData();
    expect(component.noKpis).toBeFalse();
    component.configGlobalData[0]['isEnabled']=false;
    component.configGlobalData[0]['shown']=false;
    component.processKpiConfigData();
    expect(component.noKpis).toBeTrue();
    expect(Object.keys(component.kpiConfigData).length).toBe(configGlobalData.length);
  });

  it('should call groupKpi methods on selecting filter',()=>{
    const filterData ={
      masterData: {
        kpiList:[]
      },
      filterData: [
        {
          nodeId: '38998_DEMO_SONAR_63284960fdd20276d60e4df5',
          nodeName: 'Tools|PI_10|ITR_6|07 Sep_DEMO_SONAR',
          sprintStartDate: '2022-09-07T08:40:00.0000000',
          sprintEndDate: '2022-09-27T08:40:00.0000000',
          path: [
              'DEMO_SONAR_63284960fdd20276d60e4df5###asc_subaccount###mn_account###asfd_business###TESTS_corporate'
          ],
          labelName: 'sprint',
          parentId: [
              'DEMO_SONAR_63284960fdd20276d60e4df5'
          ],
          sprintState: 'ACTIVE',
          level: 6
      }
      ],
      filterApplyData: {
          ids: [
              '38998_DEMO_SONAR_63284960fdd20276d60e4df5'
          ],
          sprintIncluded: [
              'CLOSED',
              'ACTIVE'
          ],
          selectedMap: {
              corporate: [],
              business: [],
              account: [],
              subaccount: [],
              project: [],
              sprint: [
                  '38998_DEMO_SONAR_63284960fdd20276d60e4df5'
              ],
              sqd: []
          },
          level: 6
      },
      selectedTab: 'Iteration'
  };
  component.selectedtype ='Scrum';
  const spygroupJiraKpi =spyOn(component,'groupJiraKpi');
  const spycalcBusinessDays =spyOn(component,'calcBusinessDays');
  component.receiveSharedData(filterData);
  expect(spycalcBusinessDays).toHaveBeenCalled();
  expect(spygroupJiraKpi).toHaveBeenCalled();
  filterData.filterData =[];
  component.receiveSharedData(filterData);
  expect(component.noTabAccess).toBeTrue();
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
      kpiCategory:'Iteration',
      groupId:1
    }]
  };
    const spy=spyOn(helperService,'groupKpiFromMaster').and.returnValue({kpiList : kpiListJira});
    const postJiraSpy=spyOn(component,'postJiraKpi');
    component.groupJiraKpi(['kpi17']);
    expect(postJiraSpy).toHaveBeenCalled();
  });

  it('postJiraKpi should call httpServicepost',fakeAsync(()=>{
    const jiraKpiData = {
      kpi14:    {
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
    };
    component.jiraKpiData ={};
    component.loaderJiraArray=['kpi14'];
    const spy=spyOn(httpService,'postKpi').and.returnValue(of(fakeJiraGroupId1));
    const spycreateKpiWiseId =spyOn(helperService,'createKpiWiseId').and.returnValue(jiraKpiData);
    const spycreateAllKpiArray =spyOn(component,'createAllKpiArray');
    component.postJiraKpi(fakeJiraPayload,'jira');
    tick();
    expect(spycreateKpiWiseId).toHaveBeenCalled();
    expect(spycreateAllKpiArray).toHaveBeenCalledWith(jiraKpiData);
  }));

  it('should call downloadExcel',()=>{
    component.filterApplyData=[];
    component.filterData=[];
    const spy = spyOn(helperService,'downloadExcel');
    component.downloadExcel('kpi14','Lead Time',false);
    expect(spy).toHaveBeenCalled();
  });

  it('should return video link for kpi',()=>{
    component.masterData ={
      kpiList:[
        {
          kpiId:'kpi14',
          videoLink:{
            disabled :false,
            videoUrl:'www.google.com'
          }
        }
      ]
    };
    const result =component.getVideoLink('kpi14');
    expect(result).toEqual('www.google.com');
  });

  it('should check if video link is available',()=>{
    component.masterData ={
      kpiList:[
        {
          kpiId:'kpi14',
          videoLink:{
            disabled :false,
            videoUrl:'www.google.com'
          }
        }
      ]
    };

    const result = component.isVideoLinkAvailable('kpi14');
    expect(result).toBeTrue();
  });

  it('should return the count of items selected',()=>{
    const obj={
      first :[1,2],
      second:[1]
    };
    const result =component.checkItemsSelected(obj);
    expect(result).toEqual(3);
  });

  it('should check if kpi exists',()=>{
    component.allKpiArray =[{
      kpiId:'kpi13'
    }];
    const result = component.ifKpiExist('kpi13');
    expect(result).toEqual(0);
  });

  it('should set no tabAcces to true when no data',()=>{

    service.passDataToDashboard.emit({});
    fixture.detectChanges();
    expect(component.noTabAccess).toBeTrue();
  });

  it('should call receive data on getting data from passDataToDashboard',()=>{
    const sharedObject= {
      masterData: {
        kpiList:[]
      },
      filterData: [
        {
          nodeId: '38998_DEMO_SONAR_63284960fdd20276d60e4df5',
          nodeName: 'Tools|PI_10|ITR_6|07 Sep_DEMO_SONAR',
          sprintStartDate: '2022-09-07T08:40:00.0000000',
          sprintEndDate: '2022-09-27T08:40:00.0000000',
          path: [
              'DEMO_SONAR_63284960fdd20276d60e4df5###asc_subaccount###mn_account###asfd_business###TESTS_corporate'
          ],
          labelName: 'sprint',
          parentId: [
              'DEMO_SONAR_63284960fdd20276d60e4df5'
          ],
          sprintState: 'ACTIVE',
          level: 6
      }
      ],
      filterApplyData: {
          ids: [
              '38998_DEMO_SONAR_63284960fdd20276d60e4df5'
          ],
          sprintIncluded: [
              'CLOSED',
              'ACTIVE'
          ],
          selectedMap: {
              corporate: [],
              business: [],
              account: [],
              subaccount: [],
              project: [],
              sprint: [
                  '38998_DEMO_SONAR_63284960fdd20276d60e4df5'
              ],
              sqd: []
          },
          level: 6
      },
      selectedTab: 'Iteration'
  };
  const spy =spyOn(component,'receiveSharedData');
  service.passDataToDashboard.emit(sharedObject);
  fixture.detectChanges();
  expect(spy).toHaveBeenCalledWith(sharedObject);
  expect(component.noTabAccess).toBeFalse();
  });

  it('should process kpi config data on onTypeRefresh',()=>{
    spyOn(service,'getSelectedType');
    spyOn(service,'getDashConfigData').and.returnValue(userConfigData['data']);
    const spy =spyOn(component,'processKpiConfigData');
    service.onTypeRefresh.emit('Scrum');
    fixture.detectChanges();
    expect(component.configGlobalData.length).toEqual(1);
    expect(spy).toHaveBeenCalled();
  });

  it('should set the colorObj',()=>{
    const x ={
      'Sample One_hierarchyLevelOne': {
          nodeName: 'Sample One',
          color: '#079FFF'
      }
  };
   service.setColorObj(x);
   fixture.detectChanges();
   component.ngOnInit();
   expect(component.colorObj).toBe(x);
  });

it('should process config data on getting globalDashConfigData',()=>{
  // spyOn(service,'globalDashConfigData').and.returnValue(userConfigData['data']);
  const spy =spyOn(component,'processKpiConfigData');
  service.globalDashConfigData.emit(userConfigData['data']);
  fixture.detectChanges();
  expect(component.configGlobalData.length).toEqual(1);
  expect(spy).toHaveBeenCalled();
});
});
