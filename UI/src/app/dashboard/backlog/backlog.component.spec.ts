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

import { BacklogComponent } from './backlog.component';
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
import { ExportExcelComponent } from 'src/app/component/export-excel/export-excel.component';
import { MessageService } from 'primeng/api';

describe('BacklogComponent', () => {
    let component: BacklogComponent;
    let fixture: ComponentFixture<BacklogComponent>;
    let service: SharedService;
    let httpService: HttpService;
    let helperService: HelperService;
    let excelService: ExcelService;
    let messageService: MessageService;
    let httpMock;
    let reqJira;
    const baseUrl = environment.baseUrl;
    const selectedTab = 'Iteration';
    const filterApplyDataWithNoFilter = {};
    const masterData = require('../../../test/resource/masterData.json');
    const filterData = require('../../../test/resource/filterData.json');
    const filterApplyDataWithScrum = { level: 2, label: 'Account', ids: ['CIM', 'FCA'], startDate: '', endDate: '', selectedMap: { Level1: [], Level2: ['CIM', 'FCA'], Level3: [], Project: [], Sprint: [], Build: [], Release: [], Squad: [], Individual: [] } };
    const fakeKpiResponse = require('../../../test/resource/milestoneKpiResponse.json');
    const fakeKpi171Data = require('../../../test/resource/fakeKpi171Data.json');
    const arrToBeAggregated = [
        {
            "filter1": "Defect",
            "filter2": "P2 - Critical",
            "data": [
                {
                    "label": "Issues at Risk",
                    "value": 0,
                    "value1": 3,
                    "unit": "",
                    "modalValues": []
                },
                {
                    "label": "Story Point",
                    "value": 0,
                    "unit": "SP"
                }
            ]
        },
        {
            "filter1": "Story",
            "filter2": "P2 - Critical",
            "data": [
                {
                    "label": "Issues at Risk",
                    "value": 6,
                    "value1": 11,
                    "unit": "",
                    "modalValues": [
                        {
                            "col1": {
                                "number": "DTS-20547",
                                "url": "http://testabc.com/jira/browse/DTS-20547"
                            },
                            "description": "GS | UI| My dashboard page will be the landing page in case of ping federate  (all data API call to be initiated post successful login)",
                            "issueStatus": "In Analysis",
                            "issueType": "Story"
                        },
                        {
                            "col1": {
                                "number": "DTS-20544",
                                "url": "http://testabc.com/jira/browse/DTS-20544"
                            },
                            "description": "GS | Backend| By pass knowHOW security flow in case of ping federate (conditional bean loading (Auth to work after disabling springboot security",
                            "issueStatus": "Ready for Testing",
                            "issueType": "Story"
                        },
                        {
                            "col1": {
                                "number": "DTS-20554",
                                "url": "http://testabc.com/jira/browse/DTS-20554"
                            },
                            "description": "GS | Backend| Super Admin to add permission to project/ by giving a SSO ID (pre-authorize)",
                            "issueStatus": "Code Review",
                            "issueType": "Story"
                        },
                        {
                            "col1": {
                                "number": "DTS-20551",
                                "url": "http://testabc.com/jira/browse/DTS-20551"
                            },
                            "description": "GS | Backend| All notification alert should bypass kafka flow in case of GS",
                            "issueStatus": "In Analysis",
                            "issueType": "Story"
                        },
                        {
                            "col1": {
                                "number": "DTS-20540",
                                "url": "http://testabc.com/jira/browse/DTS-20540"
                            },
                            "description": "GS |- Devops | Pass authentication type and apigateway  URL (kong ) as environment variable",
                            "issueStatus": "Ready for Testing",
                            "issueType": "Story"
                        },
                        {
                            "col1": {
                                "number": "DTS-20542",
                                "url": "http://testabc.com/jira/browse/DTS-20542"
                            },
                            "description": "GS | UI| Bypass login flow in casee of KONG API",
                            "issueStatus": "Ready for Testing",
                            "issueType": "Story"
                        }
                    ]
                },
                {
                    "label": "Story Point",
                    "value": 19,
                    "unit": "SP"
                }
            ]
        }
    ]
    const aggregatedData = [
        {
            "filter1": "Defect",
            "filter2": "P2 - Critical",
            "data": [
                {
                    "label": "Issues at Risk",
                    "value": 6,
                    "value1": 14,
                    "unit": "",
                    "modalValues": [
                        {
                            "col1": {
                                "number": "DTS-20547",
                                "url": "http://testabc.com/jira/browse/DTS-20547"
                            },
                            "description": "GS | UI| My dashboard page will be the landing page in case of ping federate  (all data API call to be initiated post successful login)",
                            "issueStatus": "In Analysis",
                            "issueType": "Story"
                        },
                        {
                            "col1": {
                                "number": "DTS-20544",
                                "url": "http://testabc.com/jira/browse/DTS-20544"
                            },
                            "description": "GS | Backend| By pass knowHOW security flow in case of ping federate (conditional bean loading (Auth to work after disabling springboot security",
                            "issueStatus": "Ready for Testing",
                            "issueType": "Story"
                        },
                        {
                            "col1": {
                                "number": "DTS-20554",
                                "url": "http://testabc.com/jira/browse/DTS-20554"
                            },
                            "description": "GS | Backend| Super Admin to add permission to project/ by giving a SSO ID (pre-authorize)",
                            "issueStatus": "Code Review",
                            "issueType": "Story"
                        },
                        {
                            "col1": {
                                "number": "DTS-20551",
                                "url": "http://testabc.com/jira/browse/DTS-20551"
                            },
                            "description": "GS | Backend| All notification alert should bypass kafka flow in case of GS",
                            "issueStatus": "In Analysis",
                            "issueType": "Story"
                        },
                        {
                            "col1": {
                                "number": "DTS-20540",
                                "url": "http://testabc.com/jira/browse/DTS-20540"
                            },
                            "description": "GS |- Devops | Pass authentication type and apigateway  URL (kong ) as environment variable",
                            "issueStatus": "Ready for Testing",
                            "issueType": "Story"
                        },
                        {
                            "col1": {
                                "number": "DTS-20542",
                                "url": "http://testabc.com/jira/browse/DTS-20542"
                            },
                            "description": "GS | UI| Bypass login flow in casee of KONG API",
                            "issueStatus": "Ready for Testing",
                            "issueType": "Story"
                        }
                    ]
                },
                {
                    "label": "Story Point",
                    "value": 19,
                    "unit": "SP",
                    "value1": null,
                    "modalValues": null
                }
            ]
        }
    ]
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
    const userConfigData = require('../../../test/resource/fakeGlobalConfigData.json');
    const configGlobalData = [
        {
            kpiId: 'kpi74',
            kpiName: 'Release Frequency',
            isEnabled: true,
            order: 1,
            kpiDetail: {
                kpiSubCategory : 'Flow KPIs',
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
                aggregationCriteria: 'sum',
                trendCalculative: false,
                squadSupport: false,
                xaxisLabel: 'Months',
                yaxisLabel: 'Count'
            },
            shown: true
        },
        {
            kpiId: 'kpi3',
            kpiName: 'Release Frequency',
            isEnabled: true,
            order: 1,
            kpiDetail: {
                kpiWidth : 100,
                kpiSubCategory : 'Flow KPIs',
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
                aggregationCriteria: 'sum',
                trendCalculative: false,
                squadSupport: false,
                xaxisLabel: 'Months',
                yaxisLabel: 'Count'
            },
            shown: true
        },
        {
            kpiId: 'kpi89',
            kpiName: 'Release Frequency',
            isEnabled: true,
            order: 1,
            kpiDetail: {
                kpiWidth : 100,
                kpiSubCategory : 'Flow KPIs',
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
                aggregationCriteria: 'sum',
                trendCalculative: false,
                squadSupport: false,
                xaxisLabel: 'Months',
                yaxisLabel: 'Count'
            },
            shown: false
        },
        {
            kpiId: 'kpi741',
            kpiName: 'Release Frequency',
            isEnabled: true,
            order: 1,
            kpiDetail: {
                kpiSubCategory : 'Epic View',
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
                aggregationCriteria: 'sum',
                trendCalculative: false,
                squadSupport: false,
                xaxisLabel: 'Months',
                yaxisLabel: 'Count'
            },
            shown: true
        }

    ];

    const fakeJiraPayload = require('../../../test/resource/fakeJiraPayload.json');
    const kpiDropdowns = {
        "kpi75": [
            {
                "filterType": "Filter by issue type",
                "options": [
                    "Tech Story",
                    "Technical Debt",
                    "Bug",
                    "Story"
                ]
            }
        ]
    }
    beforeEach(() => {
        service = new SharedService();
        TestBed.configureTestingModule({
            imports: [
                HttpClientTestingModule,
                RouterTestingModule.withRoutes(routes),
            ],
            declarations: [BacklogComponent,
                MultilineComponent, DashboardComponent, ExportExcelComponent],
            providers: [
                HelperService,
                { provide: APP_CONFIG, useValue: AppConfig },
                HttpService,
                { provide: SharedService, useValue: service }
                , ExcelService, DatePipe, MessageService

            ],
            schemas: [CUSTOM_ELEMENTS_SCHEMA]

        })
            .compileComponents();
        service = TestBed.inject(SharedService);
        httpService = TestBed.inject(HttpService);
        helperService = TestBed.inject(HelperService);
        excelService = TestBed.inject(ExcelService);

        spyOn(helperService, 'colorAccToMaturity').and.returnValue(('#44739f'));
        service.setNoProjects(true);
        httpMock = TestBed.inject(HttpTestingController);
        fixture = TestBed.createComponent(BacklogComponent);
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


    it('should process kpi config Data', () => {
        component.configGlobalData = configGlobalData;
        component.configGlobalData.push({
            kpiId: 'kpi120',
            isEnabled: false,
            shown: true,
            kpiName: 'Iteration Commitment',
            order: 1,
            subCategoryBoard: 'Iteration Review',
            kpiDetail: {
                subCategoryBoard: 'Iteration Review',
                id: '63320976b7f239ac93c2686a',
                kpiId: 'kpi120',
                kpiName: 'Iteration Commitment',
                isDeleted: 'False',
                defaultOrder: 17,
                kpiUnit: '',
                chartType: 'line',
                kanban: true,
                groupId: 4,
                aggregationCriteria: 'sum',
                trendCalculative: false,
                kpiSubCategory: 'Iteration Review'
            },
        });
        component.configGlobalData[0]['isEnabled'] = false;
        component.configGlobalData[0]['shown'] = false;
        component.processKpiConfigData();
        expect(Object.keys(component.kpiConfigData).length).toBe(configGlobalData.length);
        expect(component.noKpis).toBeFalse();
    });

    it('should call groupKpi methods on selecting filter', () => {
        const filterData = {
            masterData: {
                kpiList: []
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
        component.selectedtype = 'Scrum';
        const spygroupJiraKpi = spyOn(component, 'groupJiraKpi');
        spyOn(service, 'getDashConfigData').and.returnValue(userConfigData['data']);
        spyOn(component, 'processKpiConfigData');
        component.receiveSharedData(filterData);
        expect(spygroupJiraKpi).toHaveBeenCalled();
        filterData.filterData = [];
        component.receiveSharedData(filterData);
        expect(component.noTabAccess).toBeTrue();
    });

    it('should make post call when kpi available for Jira for Scrum', () => {
        const kpiListJira = [{
            id: '6332dd4b82451128f9939a29',
            kpiId: 'kpi17',
            kpiName: 'Unit Test Coverage'
        }];
        component.updatedConfigGlobalData = [
            {
                kpiId: 'kpi17',
                kpiName: 'Unit Test Coverage',
                isEnabled: true,
                order: 23,
                kpiDetail: {
                    kanban: false,
                    kpiSource: 'Jira',
                    kpiCategory: 'Backlog',
                    groupId: 1
                },
                shown: true
            }
        ];
        const spy = spyOn(helperService, 'groupKpiFromMaster').and.returnValue({ kpiList: kpiListJira });;
        const postJiraSpy = spyOn(component, 'postJiraKpi');
        component.groupJiraKpi(['kpi17']);
        expect(postJiraSpy).toHaveBeenCalled();
    });

    it('should make post call when kpi available for Jira for Scrum when we have kpi list', () => {
        const kpiListJira = [{
            id: '6332dd4b82451128f9939a29',
            kpiId: 'kpi17',
            kpiName: 'Unit Test Coverage'
        }];
        component.updatedConfigGlobalData = [
            {
                kpiId: 'kpi17',
                kpiName: 'Unit Test Coverage',
                isEnabled: true,
                order: 23,
                kpiDetail: {
                    kanban: false,
                    kpiSource: 'Jira',
                    kpiCategory: 'Backlog',
                    groupId: 1
                },
                shown: true
            }
        ];
        component.kpiJira = {
            kpiList : []
        }
        const spy = spyOn(helperService, 'groupKpiFromMaster').and.returnValue({ kpiList: kpiListJira });
        const postJiraSpy = spyOn(component, 'postJiraKpi');
        component.groupJiraKpi(['kpi17']);
        expect(postJiraSpy).toHaveBeenCalled();
    });


    it('postJiraKpi should call httpServicepost', fakeAsync(() => {
        const jiraKpiData = {
            kpi14: {
                kpiId: 'kpi14',
                kpiName: 'Defect Injection Rate',
                unit: '%',
                maxValue: '200',
                chartType: '',
                id: '63355d7c41a0342c3790fb83',
                kpiUnit: '%',
                kanban: false,
                kpiSource: 'Jira',
                thresholdValue: 10,
                trendValueList: [],
                groupId: 2
            },
            kpi127: {
                kpiId: 'kpi14',
                kpiName: 'Defect Injection Rate',
                unit: '%',
                maxValue: '200',
                chartType: '',
                id: '63355d7c41a0342c3790fb83',
                kpiUnit: '%',
                kanban: false,
                kpiSource: 'Jira',
                thresholdValue: 10,
                trendValueList: [
                    {
                        "filter": "Overall",
                        "value": [
                            {
                                "data": "PSknowHOW ",
                                "value": [
                                    {
                                        "data": "2",
                                        "sSprintID": "0-1",
                                    },
                                    {
                                        "data": "1",
                                        "sSprintID": "1-3",
                                    },
                                    {
                                        "data": "0",
                                        "sSprintID": "3-6",
                                    },
                                    {
                                        "data": "0",
                                        "sSprintID": "6-12",
                                    },
                                    {
                                        "data": "1",
                                        "sSprintID": ">12",
                                    }
                                ]
                            }
                        ]
                    }
                ],
                groupId: 2,
                xAxisValues :[
                    "0-1",
                    "1-3",
                    "3-6",
                    "6-12",
                    ">12"
                ],
            },
            kpi170: {
                kpiId: 'kpi14',
                kpiName: 'Defect Injection Rate',
                unit: '%',
                maxValue: '200',
                chartType: '',
                id: '63355d7c41a0342c3790fb83',
                kpiUnit: '%',
                kanban: false,
                kpiSource: 'Jira',
                thresholdValue: 10,
                trendValueList: [
                    {
                        "filter": "Overall",
                        "value": [
                            {
                                "data": "PSknowHOW ",
                                "value": [
                                    {
                                        "data": "2",
                                        "sSprintID": "0-1",
                                    },
                                    {
                                        "data": "1",
                                        "sSprintID": "1-3",
                                    },
                                    {
                                        "data": "0",
                                        "sSprintID": "3-6",
                                    },
                                    {
                                        "data": "0",
                                        "sSprintID": "6-12",
                                    },
                                    {
                                        "data": "1",
                                        "sSprintID": ">12",
                                    }
                                ]
                            }
                        ]
                    }
                ],
                groupId: 2,
                xAxisValues :[
                    "0-1",
                    "1-3",
                    "3-6",
                    "6-12",
                    ">12"
                ],
            },
            kpi3: {
                kpiId: 'kpi14',
                kpiName: 'Defect Injection Rate',
                unit: '%',
                maxValue: '200',
                chartType: '',
                id: '63355d7c41a0342c3790fb83',
                kpiUnit: '%',
                kanban: false,
                kpiSource: 'Jira',
                thresholdValue: 10,
                trendValueList: [
                    {
                        "filter": "Overall",
                        "value": [
                            {
                                "data": "PSknowHOW ",
                                "value": [
                                    {
                                        "data": "2",
                                        "sSprintID": "0-1",
                                    },
                                    {
                                        "data": "1",
                                        "sSprintID": "1-3",
                                    },
                                    {
                                        "data": "0",
                                        "sSprintID": "3-6",
                                    },
                                    {
                                        "data": "0",
                                        "sSprintID": "6-12",
                                    },
                                    {
                                        "data": "1",
                                        "sSprintID": ">12",
                                    }
                                ]
                            }
                        ]
                    }
                ],
                groupId: 2,
                xAxisValues :[
                    "0-1",
                    "1-3",
                    "3-6",
                    "6-12",
                    ">12"
                ],
            },
        };
        component.jiraKpiData = {};
        component.loaderJiraArray = ['kpi14'];
        const spy = spyOn(httpService, 'postKpiNonTrend').and.returnValue(of(fakeJiraGroupId1));
        const spycreateKpiWiseId = spyOn(helperService, 'createKpiWiseId').and.returnValue(jiraKpiData);
        const spycreateAllKpiArray = spyOn(component, 'createAllKpiArray');
        component.postJiraKpi(fakeJiraPayload, 'jira');
        tick();
        expect(spycreateKpiWiseId).toHaveBeenCalled();
        expect(spycreateAllKpiArray).toHaveBeenCalledWith(jiraKpiData);
    }));

    it('postJiraKpi should call httpServicepost when reponse will give error', fakeAsync(() => {
        
        component.jiraKpiData = {};
        component.loaderJiraArray = ['kpi14'];
        const spy = spyOn(httpService, 'postKpiNonTrend').and.returnValue(of(null));
        component.postJiraKpi(fakeJiraPayload, 'jira');
        tick();
    }));

    it('should call downloadExcel', () => {
        component.filterApplyData = [];
        component.filterData = [];
        const spy = spyOn(component.exportExcelComponent, 'downloadExcel');
        component.downloadExcel('kpi14', 'Lead Time', false, false);
        expect(spy).toHaveBeenCalled();
    });

    it('should check if kpi exists', () => {
        component.allKpiArray = [{
            kpiId: 'kpi13'
        }];
        const result = component.ifKpiExist('kpi13');
        expect(result).toEqual(0);
    });

    it('should set no tabAcces to true when no data', () => {
        service.passDataToDashboard.emit({});
        fixture.detectChanges();
        expect(component.noTabAccess).toBeTrue();
    });

    it('should call receive data on getting data from passDataToDashboard', () => {
        const sharedObject = {
            masterData: {
                kpiList: []
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
            selectedTab: 'Backlog'
        };
        component.globalConfig = userConfigData['data'];
        const spy = spyOn(component, 'receiveSharedData');
        service.passDataToDashboard.emit(sharedObject);
        fixture.detectChanges();
        expect(spy).toHaveBeenCalledWith(sharedObject);
        expect(component.noTabAccess).toBeFalse();
    });

    it('should set the colorObj', () => {
        const x = {
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

    it('should process config data on getting globalDashConfigData', () => {
        component.sharedObject = {};
        service.globalDashConfigData.emit(userConfigData['data']);
        fixture.detectChanges();
        expect(component.configGlobalData.length).toEqual(3);
    });

    it('should perform the aggregation logic', () => {
        const data = component.applyAggregationLogic(arrToBeAggregated);
        fixture.detectChanges();
        expect(data).toEqual(aggregatedData);
    });

    it('should get dropdown array for kpi', () => {
        spyOn(component, 'ifKpiExist').and.returnValue('0');
        component.allKpiArray = [{
            'kpiId': 'kpi75',
            filters: {
                "filter1": {
                    "filterType": "Filter by issue type",
                    "options": [
                        "Tech Story",
                        "Technical Debt",
                        "Bug",
                        "Story"
                    ]
                }
            }
        }]
        component.getDropdownArray('kpi75');
        expect(component.kpiDropdowns['kpi75'].length).toEqual(kpiDropdowns['kpi75'].length);
    });

    it('should get dropdown array for kpi with filter in trending list', () => {
        spyOn(component, 'ifKpiExist').and.returnValue('0');
        component.allKpiArray = [{
            'kpiId': 'kpi75',
            trendValueList: [
                {
                    filter: "Overall",
                        data: [{
                            "label": "Scope added",
                            "value": 1,
                            "value1": 0,
                            "labelInfo": "(Issue Count/Original Estimate)",
                            "unit": "",
                        }]
                }

            ]
        }]
        component.getDropdownArray('kpi75');
        expect(component.kpiDropdowns).toBeDefined();
    });

    it('should get dropdown array for kpi with filter1 in trending list', () => {
        spyOn(component, 'ifKpiExist').and.returnValue('0');
        component.allKpiArray = [{
            'kpiId': 'kpi75',
            trendValueList: [
                {
                    filter1: "Overall",
                        data: [{
                            "label": "Scope added",
                            "value": 1,
                            "value1": 0,
                            "labelInfo": "(Issue Count/Original Estimate)",
                            "unit": "",
                        }]
                }

            ]
        }]
        component.getDropdownArray('kpi75');
        expect(component.kpiDropdowns).toBeDefined();
    });


    it('should handle selected option when have multi dropdown', () => {
        const event = {
            "filter1": [
                "Tech Story"
            ],
            "filter2": [
                "Medium"
            ]
        };
        const kpi = {
            'kpiId': 'kpi123'
        }
        component.kpiSelectedFilterObj['kpi123'] = {};
        component.kpiSelectedFilterObj['kpi123'] = event
        spyOn(component, 'getChartData');
        service.setKpiSubFilterObj(component.kpiSelectedFilterObj)
        component.handleSelectedOption(event, kpi)
        expect(Object.keys(component.kpiSelectedFilterObj['kpi123']).length).toEqual(Object.keys(event).length);
    });

    it('should handle selected option when have single dropdown', () => {
        const event = {
            "filter1": [
                "Tech Story"
            ],
        };
        const kpi = {
            'kpiId': 'kpi123'
        }
        component.kpiSelectedFilterObj['kpi123'] = {};
        component.kpiSelectedFilterObj['kpi123'] = event
        spyOn(component, 'getChartData');
        service.setKpiSubFilterObj(component.kpiSelectedFilterObj)
        component.handleSelectedOption(event, kpi)
        expect(Object.keys(component.kpiSelectedFilterObj['kpi123']).length).toEqual(Object.keys(event).length);
    });

    it('should convert to hours if time', () => {
        const time = '14880';
        const unit = 'hours';
        const convertedTime = component.convertToHoursIfTime(time, unit);
        expect(convertedTime).toEqual('248h');
    });

    it("should issue details view shown on arrow click", () => {
        const kpi = {
            isEnabled: true,
            kpiDetail: {
                id: '63c85780f1cc727f444c6f0d',
                kpiId: 'kpi119',
                defaultOrder: 3,
            },
            kpiId: 'kpi119',
            kpiName: 'Work Remaining',
            order: 3,
            shown: true,
        };
        const tableValues = [{
            ['Issue Description']:
                'Playground server is failing with OutOfMemoryError',
            ['Issue Id']: 'DTS-20225',
        }];
        component.handleArrowClick(kpi, "Issue Count", tableValues);
        expect(component.displayModal).toBeTruthy();
    });

    it('should convert to hours', () => {
        let result = component.convertToHoursIfTime(25, 'hours');
        expect(result).toEqual('25m');

        result = component.convertToHoursIfTime(65, 'hours');
        expect(result).toEqual('1h 5m');

        result = component.convertToHoursIfTime(60, 'hours');
        expect(result).toEqual('1h');
    });


    it('should generate excel on click of export button', () => {
        component.modalDetails = {
            header: 'Work Remaining / Issue Count/Original Estimate',
            tableHeadings: [
                "Issue Id",
                "Issue Description",
                "Issue Status",
            ],
            tableValues: [{
                'Issue Id': 'DTS-22685',
                'Issue URL': 'http://testabc.com/jira/browse/DTS-22685',
                'Issue Description': 'Iteration KPI | Popup window is not wide enough to read details  ',
                'Issue Status': 'Open',
            }],
        };

        const spyGenerateExcel = spyOn(excelService, 'generateExcel');
        component.generateExcel();
        expect(spyGenerateExcel).toHaveBeenCalled();
    });

    it('should getchartdata for kpi when trendValueList is an object', () => {
        component.allKpiArray = [{
            kpiId: 'kpi124',
            trendValueList: {
                value: [
                    {
                        filter1: "Overall",
                        filter2: "Overall",
                        data: [{
                            "label": "Scope added",
                            "value": 1,
                            "value1": 0,
                            "labelInfo": "(Issue Count/Original Estimate)",
                            "unit": "",
                        }]
                    }
                ]

            }
        }];
        component.kpiSelectedFilterObj['kpi124'] = {
            'filter1': ['Overall'],
            'filter2': ['Overall']
        }
        const res = {
            "filter1": "Overall",
            "filter2": "Overall",
            "data": [
                {
                    "label": "Issue without estimates",
                },
            ]
        }
        component.updatedConfigGlobalData = [
            {
                kpiId: 'kpi125',
                kpiDetail: {
                    chartType: 'GroupBarChart'
                }
            }
        ];
        spyOn(component, 'createTrendData')
        component.getChartData('kpi124', 0, 'sum')
        expect(component.kpiChartData).toBeDefined()
    })



    it('should get chartdata for kpi when trendValueList is an object with single filter', () => {
        component.allKpiArray = [{
            kpiId: 'kpi124',
            trendValueList: {
                value: [
                    {
                        filter1: "Overall",
                        data: [{
                            "label": "Scope added",
                        }]
                    }
                ]

            }
        }];
        component.kpiSelectedFilterObj['kpi124'] = {
            'filter1': ['Overall']
        }
        component.updatedConfigGlobalData = [
            {
                kpiId: 'kpi125',
                kpiDetail: {
                    chartType: 'GroupBarChart'
                }
            }
        ];

        spyOn(component, 'createTrendData')
        component.getChartData('kpi124', 0, 'sum')
        expect(component.kpiChartData).toBeDefined();
    })

    it('should get chartdata for kpi when trendValueList is an object and KPI selected filter is blank', () => {
        component.allKpiArray = [{
            kpiId: 'kpi124',
            trendValueList: {
                value: [
                    {
                        filter1: "Overall",
                        data: [{
                            "label": "Scope added",
                            "value": 1,
                        }]
                    }
                ]

            }
        }];
        component.updatedConfigGlobalData = [
            {
                kpiId: 'kpi125',
                kpiDetail: {
                    chartType: 'GroupBarChart'
                }
            }
        ];
        component.kpiSelectedFilterObj['kpi124'] = {}

        spyOn(component, 'createTrendData')
        component.getChartData('kpi124', 0, 'sum')
        expect(component.kpiChartData['kpi124'][0].data.length).toBeGreaterThan(0)
    })

    it("should create kpi array when trendvalueList is object", () => {
        let kpi = [{
            kpiId: "kpi141",
            trendValueList: {
                value: [
                    {
                        filter1: "Overall",
                        data: [{
                            "label": "Scope added",
                        }]
                    }
                ]

            },
            filters: ['f1', "f2"]
        },]
        component.updatedConfigGlobalData = [
            {
                kpiId: 'kpi125',
                kpiDetail: {
                    chartType: 'GroupBarChart'
                }
            }
        ];
        const fakeKPi = helperService.createKpiWiseId(kpi);
        spyOn(component, 'ifKpiExist').and.returnValue(1);
        component.createAllKpiArray(fakeKPi)
        expect(component.allKpiArray.length).toBeGreaterThan(0);
    })

    it('should reload KPI once mapping saved ', () => {
        const KPiList = [{
            id: "kpi1"
        }];
        const fakeKPiDetails = {
            kpiDetails: {
                kpiSource: 'jira',
                kanban: true,
                groupId: 1
            }
        }
        spyOn(service, 'getSelectedType').and.returnValue('kanban');
        spyOn(helperService, 'groupKpiFromMaster').and.returnValue({ kpiList: KPiList })
        const spy = spyOn(component, 'postJiraKpi');
        component.reloadKPI(fakeKPiDetails);
        expect(spy).toBeDefined();
    })

    it('should check latest trend and maturity', () => {
        const item = [
          {
            "data": "EU",
            "value": [
              {
                "data": "27.33",
                "value": 27.33,
    
                "sprojectName": "EU"
              },
              {
                "data": "0.15",
                "value": 0.15,
    
                "sprojectName": "EU"
              },
              {
                "data": "8.66",
                "value": 8.66,
    
                "sprojectName": "EU"
              },
              {
                "data": "93.86",
                "value": 93.86,
    
                "sprojectName": "EU"
              },
              {
                "data": "15.1",
                "value": 15.1,
    
                "sprojectName": "EU"
              }
            ],
            "maturity": "4"
          }
        ];
        const kpiData = {
          "kpiId": "kpi121",
          "kpiName": "Capacity",
          "isEnabled": true,
          "order": 2,
          "kpiDetail": {
            "id": "6407068ba59c6c0bdeb427ae",
            "kpiId": "kpi121",
            "kpiName": "Capacity",
            "isDeleted": "False",
            "defaultOrder": 2,
            "kpiCategory": "Iteration",
            "kpiUnit": "",
            "showTrend": false,
            "isPositiveTrend": true,
            "boxType": "1_column",
            "calculateMaturity": false,
            "hideOverallFilter": false,
            "kpiSource": "Jira",
            "trendCalculative": false,
          },
          "shown": true
        }
        const spy = spyOn(component, 'checkLatestAndTrendValue');
        component.checkLatestAndTrendValue(kpiData, item);
        expect(spy).toHaveBeenCalled();
    });

    it('should check maturity', () => {
        const item = {
            "data": "EU",
            "value": [
              {
                "data": "27.33",
                "value": 27.33,
    
                "sprojectName": "EU"
              },
              {
                "data": "0.15",
                "value": 0.15,
    
                "sprojectName": "EU"
              },
              {
                "data": "8.66",
                "value": 8.66,
    
                "sprojectName": "EU"
              },
              {
                "data": "93.86",
                "value": 93.86,
    
                "sprojectName": "EU"
              },
              {
                "data": "15.1",
                "value": 15.1,
    
                "sprojectName": "EU"
              }
            ],
            "maturity": "4"
        };
        const val = component.checkMaturity(item);
        expect(val).toEqual('M4')
    })

    it('should check maturity when maturity is undefined', () => {
        const item = {
            "data": "EU"
        };
        const val = component.checkMaturity(item);
        expect(val).toEqual('NA')
    })

    it('should check maturity when all data is 0', () => {
        const item = {
            "data": "EU",
            "value": [
              {
                "data": "0",
                "value": 0,
    
                "sprojectName": "EU"
              },
              {
                "data": "0",
                "value": 0,
    
                "sprojectName": "EU"
              },
              {
                "data": "0",
                "value": 0,
    
                "sprojectName": "EU"
              },
              {
                "data": "0",
                "value": 0,
    
                "sprojectName": "EU"
              },
              {
                "data": "0",
                "value": 0,
    
                "sprojectName": "EU"
              }
            ],
            "maturity": "4"
        };
        const val = component.checkMaturity(item);
        expect(val).toEqual('M4')
    })

    it('should check maturity when all value array length is less than 5', () => {
        const item = {
            "data": "EU",
            "value": [
              {
                "data": "0",
                "value": 0,
    
                "sprojectName": "EU"
              },
              {
                "data": "0",
                "value": 0,
    
                "sprojectName": "EU"
              },
              {
                "data": "0",
                "value": 0,
    
                "sprojectName": "EU"
              },
            ],
            "maturity": "4"
        };
        const val = component.checkMaturity(item);
        expect(val).toEqual('M4')
    })

    it('should create trend data for the given kpiId when the data exists', () => {
        component.configGlobalData = [
            { kpiId: 1, name: 'KPI 1' },
            { kpiId: 2, name: 'KPI 2' }
          ];
        component.kpiChartData = {
        1: [
            { data: 'Data 1', value: [1, 2, 3], maturity: 1, maturityValue: 'Low' },
            { data: 'Data 2', value: [4, 5, 6], maturity: 2, maturityValue: 'Medium' }
        ],
        2: [
            { data: 'Data 3', value: [7, 8, 9], maturity: 3, maturityValue: 'High' }
        ]
        };
        component.kpiTrendObject = {};
        spyOn(component, 'checkLatestAndTrendValue').and.returnValue(['3', 'NA', '%']);
        // call the method
        component.createTrendData(1);
    
        // check if the kpiTrendObject was updated correctly
        expect(component.kpiTrendObject[1]).toEqual([
          {
            hierarchyName: 'Data 1',
            trend: '3',
            maturity: 'M1',
            maturityValue: 'Low',
            maturityDenominator: 3,
            kpiUnit: 'NA'
          }
        ]);
      });
    
      it('should not create trend data for the given kpiId when the data does not exist', () => {
        component.configGlobalData = [
            { kpiId: 1, name: 'KPI 1' },
            { kpiId: 2, name: 'KPI 2' }
        ];
        // call the method
        component.createTrendData(3);
    
        // check if the kpiTrendObject remains empty
        expect(component.kpiTrendObject[3]).toBeUndefined();
    });


    it('should get kpi171 data', () => {
        const fakeJiraData = [{
            "kpiId": "kpi171",
            "kpiName": "Cycle Time",
            "unit": "%",
            "maxValue": "200",
            "chartType": "",
            "trendValueList": {...fakeKpi171Data}
        }];
        component.kpiSelectedFilterObj['kpi171'] = {"filter1": "Task"}
        spyOn(component, 'ifKpiExist');
        component.allKpiArray = [];
        component.kpiJira = {
            "kpiList": [
                {
                    "id": "655e0d435769c2002ad81574",
                    "kpiId": "kpi171",
                    "kpiName": "Flow Efficiency",
                    "isDeleted": "False",
                    "defaultOrder": 1,
                    "kpiCategory": "Backlog",
                    "kpiSubCategory": "Flow KPIs",
                    "kpiUnit": "%",
                    "chartType": "",
                    "showTrend": false,
                    "isPositiveTrend": false,
                    "calculateMaturity": false,
                    "hideOverallFilter": false,
                    "kpiSource": "Jira",
                    "kanban": false,
                    "groupId": 11,
                    "kpiInfo": {
                        "definition": "The percentage of time spent in work states vs wait states across the lifecycle of an issue"
                    },
                    "kpiFilter": "dropDown",
                    "aggregationCriteria": "average",
                    "trendCalculative": false,
                    "xaxisLabel": "Duration",
                    "yaxisLabel": "Percentage",
                    "isAdditionalFilterSupport": false
                }
            ],
            "ids": [
                "AAAA_655f0ebed08ea076bfb2c9db"
            ],
            "level": 5,
            "selectedMap": {
                "bu": [],
                "ver": [],
                "acc": [],
                "port": [],
                "project": [
                    "AAAA_655f0ebed08ea076bfb2c9db"
                ],
                "sprint": [],
                "release": [],
                "sqd": []
            },
            "sprintIncluded": [
                "CLOSED"
            ],
            "label": "project"
        }
        component.kpiSpecificLoader = [];
        spyOn(component, 'getChartDataForCardWithCombinationFilter');
        const spy = spyOn(httpService, 'postKpiNonTrend').and.returnValue(of(fakeJiraData));
        component.getkpi171Data('kpi171', fakeKpi171Data)
        expect(spy).toHaveBeenCalled();
    })

   

    it('should apply the aggregation logic correctly when the data is valid', () => {
        // create sample data
        const obj = {
          'Category 1': [
            {
              data: 'Data 1',
              value: [
                {
                  hoverValue: { 'Total Value': 10, 'Other Value': 5 },
                  maxValue: 0,
                  value: 0
                }
              ]
            }
          ]
        };
    
        // call the method
        const result = component.applyAggregationLogicForProgressBar(obj);
    
        // check if the result is correct
        expect(result).toEqual([
          {
            data: 'Data 1',
            value: [
              {
                hoverValue: { 'Total Value': 10, 'Other Value': 5 },
                maxValue: 10,
                value: 5
              }
            ]
          }
        ]);
      });
    
      it('should not apply the aggregation logic when the data is invalid', () => {
        // create sample data
        const obj = {
          'Category 1': [
            {
              data: 'Data 1',
              value: [
                {
                  maxValue: 0,
                  value: 0
                }
              ]
            }
          ]
        };
    
        // call the method
        const result = component.applyAggregationLogicForProgressBar(obj);
    
        // check if the result is correct
        expect(result).toEqual([
          {
            data: 'Data 1',
            value: [
              {
                maxValue: 0,
                value: 0
              }
            ]
          }
        ]);
    });

    it('should update the kpiSelectedFilterObj correctly when the event is not empty', () => {
         // create sample data
        const event = { filter1: 'value1', filter2: 'value2' };
        const kpi = { kpiId: 1 };
        // call the method
        spyOn(component, 'getChartDataForCard').and.callThrough();
        spyOn(service, 'setKpiSubFilterObj');
        component.handleSelectedOptionForCard(event, kpi);
    
        // check if the kpiSelectedFilterObj was updated correctly
        expect(component.kpiSelectedFilterObj).toEqual({
          1: { filter1: 'value1', filter2: 'value2' }
        });
        expect(component.getChartDataForCard).toHaveBeenCalledWith(1, -1);
        expect(service.setKpiSubFilterObj).toHaveBeenCalledWith(component.kpiSelectedFilterObj);
      });

      it('should apply aggregation logic for kpi138', () => {
        const arr = [
            {
                "filter1": "Tech Debt",
                "filter2": "Medium",
                "data": [
                    {
                        "label": "Ready Backlog",
                        "value": 2,
                        "value1": 4,
                        "unit1": "SP",
                        "modalValues": []
                    },
                    {
                        "label": "Backlog Strength",
                        "value": 0,
                        "unit": "Sprint"
                    },
                    {
                        "label": "Readiness Cycle time",
                        "value": 8,
                        "unit": "days"
                    }
                ]
            },
            {
                "filter1": "Story",
                "filter2": "Medium",
                "data": [
                    {
                        "label": "Ready Backlog",
                        "value": 6,
                        "value1": 12,
                        "unit1": "SP",
                        "modalValues": []
                    },
                    {
                        "label": "Backlog Strength",
                        "value": 0,
                        "unit": "Sprint"
                    },
                    {
                        "label": "Readiness Cycle time",
                        "value": 17,
                        "unit": "days"
                    }
                ]
            }
        ];
        const kpi138Obj = [
            {
                "filter1": "Tech Debt",
                "filter2": "Medium",
                "data": [
                    {
                        "label": "Ready Backlog",
                        "value": 8,
                        "value1": 16,
                        "unit1": "SP",
                        "modalValues": []
                    },
                    {
                        "label": "Backlog Strength",
                        "value": 0,
                        "unit": "Sprint",
                        "value1": null,
                        "modalValues": null
                    },
                    {
                        "label": "Readiness Cycle time",
                        "value": 15,
                        "unit": "days",
                        "value1": null,
                        "modalValues": null
                    }
                ]
            }
        ]
        spyOn(component, 'applyAggregationLogic').and.callThrough();
        expect(component.applyAggregationLogicForkpi138(arr)).toEqual(kpi138Obj)
    })

    it('should check latest trend and maturity', () => {
        const item = {
            "data": "AAAA",
            "value": [
                {
                    "data": "0.0",
                    "sSprintID": "< 6 Months",
                    "sSprintName": "< 6 Months",
                    "value": 0,
                    "hoverValue": {
                        "Issue Count": 0
                    },
                    "date": "< 6 Months",
                    "kpiGroup": "Overall",
                    "sprojectName": "AAAA",
                    "xAxisTick": "< 6 Months",
                    "sortSprint": "< 6 Months",
                    "xName": "< 6 Months"
                },
                {
                    "data": "0.0",
                    "sSprintID": "< 3 Months",
                    "sSprintName": "< 3 Months",
                    "value": 0,
                    "hoverValue": {
                        "Issue Count": 0
                    },
                    "date": "< 3 Months",
                    "kpiGroup": "Overall",
                    "sprojectName": "AAAA",
                    "xAxisTick": "< 3 Months",
                    "sortSprint": "< 3 Months",
                    "xName": "< 3 Months"
                },
                {
                    "data": "0.0",
                    "sSprintID": "< 1 Months",
                    "sSprintName": "< 1 Months",
                    "value": 0,
                    "hoverValue": {
                        "Issue Count": 0
                    },
                    "date": "< 1 Months",
                    "kpiGroup": "Overall",
                    "sprojectName": "AAAA",
                    "xAxisTick": "< 1 Months",
                    "sortSprint": "< 1 Months",
                    "xName": "< 1 Months"
                },
                {
                    "data": "0.0",
                    "sSprintID": "< 2 Weeks",
                    "sSprintName": "< 2 Weeks",
                    "value": 0,
                    "hoverValue": {
                        "Issue Count": 0
                    },
                    "date": "< 2 Weeks",
                    "kpiGroup": "Overall",
                    "sprojectName": "AAAA",
                    "xAxisTick": "< 2 Weeks",
                    "sortSprint": "< 2 Weeks",
                    "xName": "< 2 Weeks"
                },
                {
                    "data": "0.0",
                    "sSprintID": "< 1 Week",
                    "sSprintName": "< 1 Week",
                    "value": 0,
                    "hoverValue": {
                        "Issue Count": 0
                    },
                    "date": "< 1 Week",
                    "kpiGroup": "Overall",
                    "sprojectName": "AAAA",
                    "xAxisTick": "< 1 Week",
                    "sortSprint": "< 1 Week",
                    "xName": "< 1 Week"
                }
            ]
        }
        const kpiData = {
            "kpiId": "kpi170",
            "kpiName": "Flow Efficiency",
            "isEnabled": true,
            "order": 1,
            "subCategoryBoard": "Flow KPIs",
            "kpiDetail": {
                "id": "655e0d435769c2002ad81574",
                "kpiId": "kpi170",
                "kpiName": "Flow Efficiency",
                "isDeleted": "False",
                "defaultOrder": 1,
                "kpiCategory": "Backlog",
                "kpiSubCategory": "Flow KPIs",
                "kpiUnit": "%",
                "chartType": "line",
                "showTrend": false,
                "isPositiveTrend": false,
                "calculateMaturity": false,
                "hideOverallFilter": false,
                "kpiSource": "Jira",
                "kanban": false,
                "groupId": 11,
                "kpiFilter": "dropDown",
                "aggregationCriteria": "average",
                "trendCalculative": false,
                "xaxisLabel": "Duration",
                "yaxisLabel": "Percentage",
                "isAdditionalFilterSupport": false
            },
            "shown": true
        }
        const res = [
            "0 %",
            "NA",
            "%"
        ]
        // spyOn(component, 'checkLatestAndTrendValue');
        component.checkLatestAndTrendValue(kpiData, item);
        expect(component.checkLatestAndTrendValue(kpiData, item)).toEqual(res);
    });

    it('should noTabAccess false when emp details not available', () => {
        service.setEmptyData('');
        fixture.detectChanges();
        component.ngOnInit();
        expect(component.noTabAccess).toBeFalsy();
    })

    it('should noTabAccess true when emp details available', () => {
        service.setEmptyData('test');
        fixture.detectChanges();
        component.ngOnInit();
        expect(component.noTabAccess).toBeTruthy();
    })

    it('should set tooltip and call setGlobalConfigData on successful getConfigDetails', () => {
        const mockFilterData = { filter: 'data' };
        spyOn(httpService, 'getConfigDetails').and.returnValue(of(mockFilterData));
        component.ngOnInit();
        expect(component.tooltip).toBe(mockFilterData);
      });

      it("should createapiarry for radiobutton",()=>{
        const data = {
            kpi141 : {
                kpiId: "kpi141",
                kpiName: "Defect Count by Status",
                unit: "Count",
                maxValue: "",
                chartType : "graph",
                kpiInfo: {
                    definition: "It shows the breakup of all defects tagged to a release based on Status. The breakup is shown in terms of count & percentage."
                },
                id: "64b4ed7acba3c12de164732c",
                isDeleted: false,
                kpiCategory: "Release",
                kpiUnit: "Count",
                kanban: false,
                kpiSource: "Jira",
                trendValueList: [
                    {
                        filter1 : 'story',
                        value : [
                            {
                                data: "1",
                                value: [
                                    {
                                        value: 0,
                                        drillDown: [],
                                        subFilter: "To Do"
                                    },
                                ],
                                kpiGroup: "Issue Count"
                            }
                        ]
                    }
                ],
                groupId: 9
            }
        };

        component.updatedConfigGlobalData = [
            {
                kpiId: 'kpi141',
                kpiName: 'Deployment Frequency',
                isEnabled: true,
                order: 23,
                kpiDetail: {
                    kpiFilter : 'radiobutton',
                    chartType : "graph"
                },
                shown: true
            }
        ];

        component.kpiSelectedFilterObj['kpi124'] = {
            filter1: ['story']
        }
        component.kpiDropdowns = {
            kpi141 : {
                options : ['story']
            }
        }
        spyOn(component,'ifKpiExist').and.returnValue(-1);
        spyOn(component,'createTrendData');
        component.createAllKpiArray(data);
        expect(component.kpiSelectedFilterObj).toBeDefined();
      })

      it("should createapiarry for dropdown",()=>{
        const data = {
            kpi141 : {
                kpiId: "kpi141",
                kpiName: "Defect Count by Status",
                unit: "Count",
                maxValue: "",
                chartType: "",
                kpiInfo: {
                    definition: "It shows the breakup of all defects tagged to a release based on Status. The breakup is shown in terms of count & percentage."
                },
                id: "64b4ed7acba3c12de164732c",
                isDeleted: false,
                kpiCategory: "Release",
                kpiUnit: "Count",
                kanban: false,
                kpiSource: "Jira",
                trendValueList: [
                    {
                        filter1 : 'story',
                        value : [
                            {
                                data: "1",
                                value: [
                                    {
                                        value: 0,
                                        drillDown: [],
                                        subFilter: "To Do"
                                    },
                                ],
                                kpiGroup: "Issue Count"
                            }
                        ]
                    }
                ],
                groupId: 9
            }
        };

        component.updatedConfigGlobalData = [
            {
                kpiId: 'kpi141',
                kpiName: 'Deployment Frequency',
                isEnabled: true,
                order: 23,
                kpiDetail: {
                    kpiFilter : 'dropdown',
                    chartType : "graph"
                },
                shown: true
            }
        ];

        component.kpiSelectedFilterObj['kpi124'] = {
            filter1: ['story']
        }
        component.kpiDropdowns = {
            kpi141 : {
                options : ['story']
            }
        }
        spyOn(component,'ifKpiExist').and.returnValue(-1)
        spyOn(component,'createTrendData');
        component.createAllKpiArray(data);
        expect(component.kpiSelectedFilterObj).toBeDefined();
      })

      it("should createapiarry for multi dropdown",()=>{
        const data = {
            kpi141 : {
                kpiId: "kpi141",
                chartType: "",
                kpiInfo: {
                    definition: "It shows the breakup of all defects tagged to a release based on Status. The breakup is shown in terms of count & percentage."
                },
                filters : {
                    filter1 : {
                        options : ['story']
                    }
                },
                id: "64b4ed7acba3c12de164732c",
                isDeleted: false,
                kpiCategory: "Release",
                trendValueList: [
                    {
                        filter1 : 'story',
                        value : [
                            {
                                data: "1",
                                kpiGroup: "Issue Count"
                            }
                        ]
                    }
                ],
                groupId: 9
            }
        };

        component.updatedConfigGlobalData = [
            {
                kpiId: 'kpi141',
                kpiName: 'Deployment Frequency',
                isEnabled: true,
                order: 23,
                kpiDetail: {
                    kpiFilter : 'multiDropdown',
                    chartType : "graph"
                },
                shown: true
            }
        ];

        component.kpiSelectedFilterObj['kpi124'] = {
            filter1: ['story']
        }
        component.kpiDropdowns = {
            kpi141 : {
                options : ['story']
            }
        }
        spyOn(component,'ifKpiExist').and.returnValue(-1)
        spyOn(component,'createTrendData');
        component.createAllKpiArray(data);
        expect(component.kpiSelectedFilterObj).toBeDefined();
      })

      it("should createapiarry when we have filter property in trending list",()=>{
        const data = {
            kpi141 : {
                kpiId: "kpi141",
                kpiName: "Defect Count by Status",
                chartType: "",
                kpiInfo: {
                    definition: "It shows the breakup of all defects tagged to a release based on Status. The breakup is shown in terms of count & percentage."
                },
                id: "64b4ed7acba3c12de164732c",
                isDeleted: false,
                kpiCategory: "Release",
                kpiUnit: "Count",
                kanban: false,
                kpiSource: "Jira",
                trendValueList: [
                    {
                        filter : 'story',
                        value : [
                            {
                                data: "1",
                                value: [
                                    {
                                        value: 0,
                                    },
                                ],
                                kpiGroup: "Issue Count"
                            }
                        ]
                    }
                ],
                groupId: 9
            }
        };

        component.updatedConfigGlobalData = [
            {
                kpiId: 'kpi141',
                kpiName: 'Deployment Frequency',
                isEnabled: true,
                order: 23,
                kpiDetail: {
                    kpiFilter : 'multiDropdown',
                    chartType : "graph"
                },
                shown: true
            }
        ];

        component.kpiSelectedFilterObj['kpi124'] = {
            filter1: ['story']
        }
        component.kpiDropdowns = {
            kpi141 : {
                options : ['story']
            }
        }
        spyOn(component,'ifKpiExist').and.returnValue(-1)
        spyOn(component,'createTrendData');
        component.createAllKpiArray(data);
        expect(component.kpiSelectedFilterObj).toBeDefined();
      })

      it('should prepare data from trending value list when there is no kpi filter and value is blank',()=>{
        component.allKpiArray = [{
            kpiId: 'kpi124',
            trendValueList: {
                value: []

            }
        }];
        component.getChartDataForCard('kpi124',0);
        expect(component.kpiChartData).toBeDefined();
      })

      it('should prepare data from trending value list when there is no kpi filter and value is not blank',()=>{
        component.allKpiArray = [{
            kpiId: 'kpi124',
            trendValueList: {
                value: [
                    {
                        data: [{
                            "label": "Scope added",
                            "value": 1,
                            "value1": 0,
                            "labelInfo": "(Issue Count/Original Estimate)",
                            "unit": "",
                        }]
                    }
                ]

            }
        }];
        component.getChartDataForCard('kpi124',0);
        expect(component.kpiChartData).toBeDefined();
      })

      it('should prepare data from trending value list when there is no kpi filter and trendinglist is array ',()=>{
        component.allKpiArray = [{
            kpiId: 'kpi124',
            trendValueList: [
                {value: [
                    {
                        data: [{
                            "label": "Scope added",
                            "value": 1,
                            "value1": 0,
                            "labelInfo": "(Issue Count/Original Estimate)",
                            "unit": "",
                        }]
                    }
                ]}
            ]
        }];
        component.getChartDataForCard('kpi124',0);
        expect(component.kpiChartData).toBeDefined();
      })
      

      it('should prepare data from trending value list when have multi dropdown filter',()=>{
        component.allKpiArray = [{
            kpiId: 'kpi124',
            trendValueList: {
                value: [
                    {
                        filter1: "Overall",
                        filter2: "Overall",
                        data: [{
                            "label": "Scope added",
                            "value": 1,
                            "value1": 0,
                            "labelInfo": "(Issue Count/Original Estimate)",
                            "unit": "",
                        }]
                    }
                ]

            }
        }];
        component.kpiSelectedFilterObj['kpi124'] = {
            filter1: ['story'],
            filter2: ['bug']
        }
        component.getChartDataForCard('kpi124',0);
        expect(component.kpiChartData).toBeDefined();
      })

      it('should prepare data from trending value list when have single dropdown filter',()=>{
        component.allKpiArray = [{
            kpiId: 'kpi124',
            trendValueList: {
                value: [
                    {
                        filter1: "Overall",
                        data: [{
                            "label": "Scope added",
                            "value": 1,
                            "value1": 0,
                            "labelInfo": "(Issue Count/Original Estimate)",
                            "unit": "",
                        }]
                    }
                ]

            }
        }];
        component.kpiSelectedFilterObj['kpi124'] = {
            filter1: ['story'],
        }
        component.getChartDataForCard('kpi124',0);
        expect(component.kpiChartData).toBeDefined();
      })

      it('should prepare data from trending value list when have radio button',()=>{
        component.allKpiArray = [{
            kpiId: 'kpi124',
            trendValueList: {
                value: [
                    {
                        filter1: "Overall",
                        data: [{
                            "label": "Scope added",
                            "value": 1,
                            "value1": 0,
                            "labelInfo": "(Issue Count/Original Estimate)",
                            "unit": "",
                        }]
                    }
                ]

            }
        }];
        component.kpiSelectedFilterObj['kpi124'] = {
            filter1: 'story',
        }
        component.getChartDataForCard('kpi124',0);
        expect(component.kpiChartData).toBeDefined();
      })

      describe('getChartType', () => {
      
        beforeEach(() => {
          component.updatedConfigGlobalData = [
            {
              kpiId: 'kpi1',
              kpiDetail: {
                chartType: 'line'
              }
            },
            {
              kpiId: 'kpi2',
              kpiDetail: {
                chartType: 'bar'
              }
            },
            {
              kpiId: 'kpi3',
              kpiDetail: {
                chartType: 'pie'
              }
            }
          ];
        });
      
        it('should return the chartType of the specified kpiId', () => {
          // Arrange
          const kpiId = 'kpi2';
      
          // Act
          const result = component.getChartType(kpiId);
      
          // Assert
          expect(result).toBe('bar');
        });
      
        it('should return undefined if the specified kpiId is not found', () => {
          // Arrange
          const kpiId = 'kpi4';
      
          // Act
          const result = component.getChartType(kpiId);
      
          // Assert
          expect(result).toBeUndefined();
        });
      
        it('should return undefined if the kpiDetail property is undefined', () => {
          // Arrange
          const kpiId = 'kpi1';
          component.updatedConfigGlobalData[0].kpiDetail = undefined;
      
          // Act
          const result = component.getChartType(kpiId);
      
          // Assert
          expect(result).toBeUndefined();
        });

        it('should return undefined if the kpiDetail property is undefined', () => {
            // Arrange
            const kpiId = 'kpi1';
            component.updatedConfigGlobalData[0] = undefined;
        
            // Act
            const result = component.getChartType(kpiId);
        
            // Assert
            expect(result).toBeUndefined();
          });
      });

      describe('checkSprint', () => {
      
        beforeEach(() => {
          component.kpiSelectedFilterObj = {
            kpi1: {
              filter1: ['filter1Value'],
              filter2: ['filter2Value']
            },
            kpi2: {
              filter1: ['overall'],
              filter2: ['filter2Value']
            },
            kpi3: {
              filter1: ['filter1Value'],
              filter2: ['overall']
            },
            kpi4: {
              filter1: ['overall'],
              filter2: ['overall']
            }
          };
        });
      
        it('should return "-" if filter1 is not "overall" and has values or filter2 is not "overall" and has values', () => {
          // Arrange
          const value = 10;
          const unit = 'units';
          const kpiId = 'kpi1';
      
          // Act
          const result = component.checkSprint(value, unit, kpiId);
      
          // Assert
          expect(result).toBe('-');
        });
      
        it('should return formatted value with unit if filter1 is "overall" and filter2 is not "overall"', () => {
          // Arrange
          const value = 10.5;
          const unit = 'units';
          const kpiId = 'kpi2';
      
          // Act
          const result = component.checkSprint(value, unit, kpiId);
      
          // Assert
          expect(result).toBe('-');
        });
      
        it('should return formatted value with unit if filter1 is not "overall" and filter2 is "overall"', () => {
          // Arrange
          const value = 10.2;
          const unit = 'units';
          const kpiId = 'kpi3';
      
          // Act
          const result = component.checkSprint(value, unit, kpiId);
      
          // Assert
          expect(result).toBe('-');
        });
      
        it('should return formatted value with unit if both filter1 and filter2 are "overall"', () => {
          // Arrange
          const value = 10.8;
          const unit = 'units';
          const kpiId = 'kpi4';
      
          // Act
          const result = component.checkSprint(value, unit, kpiId);
      
          // Assert
          expect(result).toBe('11 units');
        });
      });


describe('typeOf', () => {  
    it('should return true if the value is an object and not null', () => {

      const value = { prop: 'value' };
  
      // Act
      const result = component.typeOf(value);
  
      // Assert
      expect(result).toBe(true);
    });
  
    it('should return false if the value is not an object', () => {

      const value = 'string';
  
      // Act
      const result = component.typeOf(value);
  
      // Assert
      expect(result).toBe(false);
    });
  
    it('should return false if the value is null', () => {

      const value = null;
  
      // Act
      const result = component.typeOf(value);
  
      // Assert
      expect(result).toBe(false);
    });
  });

  it('should update the activeIndex property with the index of the selected tab', () => {
    const event = { index: 1 };

    // Act
    component.handleTabChange(event);

    // Assert
    expect(component.activeIndex).toBe(1);
  });

  describe('checkLatestAndTrendValue', () => {
  
    it('should return latest value and unit for an item with no trend data', () => {
      const kpiData = {
        kpiDetail: {
          kpiUnit: 'Units',
          showTrend: false,
          isPositiveTrend: true
        }
      };
      const item = {
        value: [
          { value: 10 },
          { value: 20 },
          { value: 30 }
        ]
      };
      const result = component.checkLatestAndTrendValue(kpiData, item);
      expect(result[0]).toBe('30 Units');
      expect(result[1]).toBe('NA');
      expect(result[2]).toBe('Units');
    });

    it('should return latest line value and unit for an item with no trend data', () => {
        const kpiData = {
          kpiDetail: {
            kpiUnit: 'Units',
            showTrend: false,
            isPositiveTrend: true
          }
        };
        const item = {
          value: [
            { lineValue: 10 },
            { lineValue: 50 },
            { lineValue: 30 }
          ]
        };
        const result = component.checkLatestAndTrendValue(kpiData, item);
        expect(result[0]).toBe('30 Units');
        expect(result[1]).toBe('NA');
        expect(result[2]).toBe('Units');
      });
  
    it('should return latest value, trend value, and unit for a valid item with positive trend', () => {
      const kpiData = {
        kpiDetail: {
          kpiUnit: 'Units',
          showTrend: true,
          isPositiveTrend: true
        }
      };
      const item = {
        value: [
          { value: 10 },
          { value: 20 },
          { value: 30 }
        ]
      };
      const result = component.checkLatestAndTrendValue(kpiData, item);
      expect(result[0]).toBe('30 Units');
      expect(result[1]).toBe('+ve');
      expect(result[2]).toBe('Units');
    });
  
    it('should return latest value, trend value, and unit for a valid item with negative trend', () => {
      const kpiData = {
        kpiDetail: {
          kpiUnit: 'Units',
          showTrend: true,
          isPositiveTrend: false
        }
      };
      const item = {
        value: [
          { value: 30 },
          { value: 20 },
          { value: 10 }
        ]
      };
      const result = component.checkLatestAndTrendValue(kpiData, item);
      expect(result[0]).toBe('10 Units');
      expect(result[1]).toBe('+ve');
    });

    it('should return latest value, trend value, and unit for a valid item with negative trend', () => {
        const kpiData = {
          kpiDetail: {
            kpiUnit: 'Units',
            showTrend: true,
            isPositiveTrend: false
          }
        };
        const item = {
          value: [
            { value: 30 },
            { value: 20 },
            { value: 40 }
          ]
        };
        const result = component.checkLatestAndTrendValue(kpiData, item);
        expect(result[0]).toBe('40 Units');
        expect(result[1]).toBe('-ve');
      });

      it('should return latest value, trend value, and unit for a valid item with negative trend', () => {
        const kpiData = {
          kpiDetail: {
            kpiUnit: 'Units',
            showTrend: true,
            isPositiveTrend: true
          }
        };
        const item = {
          value: [
            { value: 30 },
            { value: 50 },
            { value: 40 }
          ]
        };
        const result = component.checkLatestAndTrendValue(kpiData, item);
        expect(result[0]).toBe('40 Units');
        expect(result[1]).toBe('-ve');
      });

      it('should not return when item is undefined', () => {
        const kpiData = {
          kpiDetail: {
            kpiUnit: 'Units',
            showTrend: true,
            isPositiveTrend: true
          }
        };
        const item = undefined
        const result = component.checkLatestAndTrendValue(kpiData, item);
        expect(result[0]).toBe('');
        expect(result[1]).toBe('NA');
      });


  
    it('should return latest value, trend value, and unit for a valid item with no trend', () => {
      const kpiData = {
        kpiDetail: {
          kpiUnit: 'Units',
          showTrend: true,
          isPositiveTrend: true
        }
      };
      const item = {
        value: [
          { value: 10 }
        ]
      };
      const result = component.checkLatestAndTrendValue(kpiData, item);
      expect(result[0]).toBe('10 Units');
  
    });
    it('should return latest value, trend value, and unit for a valid item with custom unit', () => {
      const kpiData = {
        kpiDetail: {
          kpiUnit: 'Custom',
          showTrend: true,
          isPositiveTrend: true
        }
      };
      const item = {
        value: [
          { value: 10 },
          { value: 20 },
          { value: 30 }
        ]
      };
      const result = component.checkLatestAndTrendValue(kpiData, item);
      expect(result[0]).toBe('30 Custom');
      expect(result[1]).toBe('+ve');
      expect(result[2]).toBe('Custom');
    });
  });

  describe('getDropdownArrayForCard', () => {
  
    it('should set dropdown array for an existing kpi with filters', () => {
      const kpiId = 'kpi-1';
      component.allKpiArray = [
        { kpiId: 'kpi-1', filters: { filter1: ['value1', 'value2'], filter2: ['value3'] } }
      ];
      component.getDropdownArrayForCard(kpiId);
      expect(component.kpiDropdowns[kpiId]).toEqual([['value1', 'value2'], ['value3']]);
    });
  
    it('should set empty dropdown array for an existing kpi with empty filters object', () => {
      const kpiId = 'kpi-1';
      component.allKpiArray = [
        { kpiId: 'kpi-1', filters: {} }
      ];
      component.getDropdownArrayForCard(kpiId);
      expect(component.kpiDropdowns[kpiId]).toEqual([]);
    });
  
    it('should set empty dropdown array for an existing kpi with undefined filters', () => {
      const kpiId = 'kpi-1';
      component.allKpiArray = [
        { kpiId: 'kpi-1' }
      ];
      component.getDropdownArrayForCard(kpiId);
      expect(component.kpiDropdowns[kpiId]).toEqual([]);
    });
  
    it('should set dropdown array for an existing kpi with filters and undefined values', () => {
      // Arrange
      const kpiId = 'kpi-1';
      component.allKpiArray = [
        { kpiId: 'kpi-1', filters: { filter1: undefined, filter2: ['value3'] } }
      ];
      component.getDropdownArrayForCard(kpiId);
      expect(component.kpiDropdowns).toBeDefined();
    });
  });

  it('should apply aggrefaration logic for non progress-bar chart ', () => {
    component.allKpiArray = [{
        kpiId: 'kpi124',
        trendValueList: [

           { filter : "f1",
            value: [
                {
                    filter1: "Overall",
                    data: [{
                        "label": "Scope added",
                        "value": 1,
                    }]
                }
            ]
        }

        ]
    }];
    component.updatedConfigGlobalData = [
        {
            kpiId: 'kpi125',
            kpiDetail: {
                chartType: 'GroupBarChart'
            }
        }
    ];
    component.kpiSelectedFilterObj['kpi124'] = {f1 : ["value1"],f2 : ["value2"]}

    spyOn(component, 'createTrendData')
    spyOn(helperService, 'applyAggregationLogic')
    component.getChartData('kpi124', 0, 'sum')
    expect(component.kpiChartData['kpi124']).toBeUndefined();
})

it('should apply aggrefaration logic for progress-bar chart ', () => {
    component.allKpiArray = [{
        kpiId: 'kpi124',
        trendValueList: [

           { filter : "f1",
            value: [
                {
                    filter1: "Overall",
                    data: [{
                        "label": "Scope added",
                        "value": 1,
                    }]
                }
            ]
        }

        ]
    }];
    component.updatedConfigGlobalData = [
        {
            kpiId: 'kpi125',
            kpiDetail: {
                chartType: 'GroupBarChart'
            }
        }
    ];
    spyOn(component,'getChartType').and.returnValue('progress-bar');
    component.kpiSelectedFilterObj['kpi124'] = {f1 : ["value1"],f2 : ["value2"]}

    spyOn(component, 'createTrendData')
    spyOn(component, 'applyAggregationLogicForProgressBar')
    component.getChartData('kpi124', 0, 'sum')
    expect(component.kpiChartData).toBeDefined();
})

it('should get chart data when have one filter', () => {
    component.allKpiArray = [{
        kpiId: 'kpi124',
        trendValueList: [

           { filter : "f1",
            value: [
                {
                    filter1: "Overall",
                    data: [{
                        "label": "Scope added",
                        "value": 1,
                    }]
                }
            ]
        }

        ]
    }];
    component.updatedConfigGlobalData = [
        {
            kpiId: 'kpi125',
            kpiDetail: {
                chartType: 'GroupBarChart'
            }
        }
    ];
    spyOn(component,'getChartType').and.returnValue('progress-bar');
    component.kpiSelectedFilterObj['kpi124'] = {f1 : ["f1"]}

    spyOn(component, 'createTrendData')
    spyOn(component, 'applyAggregationLogicForProgressBar')
    component.getChartData('kpi124', 0, 'sum')
    expect(component.kpiChartData).toBeDefined();
})

it('should get chart data when have no filter', () => {
    component.allKpiArray = [{
        kpiId: 'kpi124',
        trendValueList: [

           { filter : "Overall",
            value: [
                {
                    filter1: "Overall",
                    data: [{
                        "label": "Scope added",
                        "value": 1,
                    }]
                }
            ]
        }

        ]
    }];
    component.updatedConfigGlobalData = [
        {
            kpiId: 'kpi125',
            kpiDetail: {
                chartType: 'GroupBarChart'
            }
        }
    ];
    spyOn(component,'getChartType').and.returnValue('progress-bar');
    component.kpiSelectedFilterObj['kpi124'] = {}

    spyOn(component, 'createTrendData')
    spyOn(component, 'applyAggregationLogicForProgressBar')
    component.getChartData('kpi124', 0, 'sum')
    expect(component.kpiChartData).toBeDefined();
})
  
    it('should handle kpi171 without filter2', () => {
      const kpiId = 'kpi171';
      const trendValueList = { value: [
        {filter1 : 'Enabler Story', data :[
            {
                "label": "Intake - DOR",
                "value": 28,
                "value1": 1,
                "unit": "d",
                "unit1": "issues",
                "modalValues": [
                    {
                        "spill": false,
                        "preClosed": false,
                        "Issue Id": "DTS-30246",
                    }
                ]
            },
            {
                "label": "DOD - Live",
                "value": 0,
                "value1": 0,
                "unit": "d",
                "unit1": "issues",
                "modalValues": []
            }
        ]},
        {filter1 : 'bug',data :[
            {
                "label": "Intake - DOR",
                "value": 28,
                "value1": 1,
                "unit": "d",
                "unit1": "issues",
                "modalValues": [
                    {
                        "spill": false,
                        "preClosed": false,
                        "Issue Id": "DTS-30246",
                    }
                ]
            },
        ]},
      ] };
  
        component.kpiSelectedFilterObj = {
            [kpiId]: {
                filter1 : "Past Month",
                filter2 : ['Enabler Story','bug']
            }
        };
  
      component.getChartDataForCardWithCombinationFilter(kpiId, trendValueList);
  
      expect(component.kpiChartData).toBeDefined();
    });


});




