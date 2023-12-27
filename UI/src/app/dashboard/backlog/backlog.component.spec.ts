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
            shown: true

        });
        component.processKpiConfigData();
        expect(component.noKpis).toBeFalse();
        component.configGlobalData[0]['isEnabled'] = false;
        component.configGlobalData[0]['shown'] = false;
        component.processKpiConfigData();
        expect(component.noKpis).toBeTrue();
        expect(Object.keys(component.kpiConfigData).length).toBe(configGlobalData.length);
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
        component.masterData = {
            kpiList: [{
                kpiId: 'kpi17',
                kanban: false,
                kpiSource: 'Jira',
                kpiCategory: 'Backlog',
                groupId: 1
            }]
        };
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
            }
        };
        component.jiraKpiData = {};
        component.loaderJiraArray = ['kpi14'];
        const spy = spyOn(httpService, 'postKpi').and.returnValue(of(fakeJiraGroupId1));
        const spycreateKpiWiseId = spyOn(helperService, 'createKpiWiseId').and.returnValue(jiraKpiData);
        const spycreateAllKpiArray = spyOn(component, 'createAllKpiArray');
        component.postJiraKpi(fakeJiraPayload, 'jira');
        tick();
        expect(spycreateKpiWiseId).toHaveBeenCalled();
        expect(spycreateAllKpiArray).toHaveBeenCalledWith(jiraKpiData);
    }));

    it('should call downloadExcel', () => {
        component.filterApplyData = [];
        component.filterData = [];
        const spy = spyOn(component.exportExcelComponent, 'downloadExcel');
        component.downloadExcel('kpi14', 'Lead Time', false, false);
        expect(spy).toHaveBeenCalled();
    });

    it('should return video link for kpi', () => {
        component.masterData = {
            kpiList: [
                {
                    kpiId: 'kpi14',
                    videoLink: {
                        disabled: false,
                        videoUrl: 'www.google.com'
                    }
                }
            ]
        };
        const result = component.isVideoLinkAvailable('kpi14');
        expect(result).toBeTrue();
    });

    it('should not return video link for kpi', () => {
        component.masterData = {
            kpiList: [
                {
                    kpiId: 'kpi14',
                    videoLink: {
                        disabled: false,
                        videoUrl: ''
                    }
                }
            ]
        };
        const result = component.isVideoLinkAvailable('kpi14');
        expect(result).toBeFalse()
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
        const spy = spyOn(component, 'checkMaturity').and.returnValue(of('M4'))
        component.checkMaturity(item);
        expect(spy).toHaveBeenCalled()
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
            trend: 'NA',
            maturity: 'M1',
            maturityValue: 'Low',
            kpiUnit: '%'
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
        const trendValueList = {
            "value": [
                {
                    "filter1": "Task",
                    "data": [
                        {
                            "label": "Intake - DOR",
                            "value": 0,
                            "value1": 0,
                            "unit": "d",
                            "unit1": "issues",
                            "modalValues": []
                        },
                        {
                            "label": "DOR - DOD",
                            "value": 0,
                            "value1": 0,
                            "unit": "d",
                            "unit1": "issues",
                            "modalValues": []
                        },
                        {
                            "label": "DOD - Live",
                            "value": 0,
                            "value1": 0,
                            "unit": "d",
                            "unit1": "issues",
                            "modalValues": []
                        }
                    ]
                },
                {
                    "filter1": "Studio Task",
                    "data": [
                        {
                            "label": "Intake - DOR",
                            "value": 0,
                            "value1": 0,
                            "unit": "d",
                            "unit1": "issues",
                            "modalValues": []
                        },
                        {
                            "label": "DOR - DOD",
                            "value": 0,
                            "value1": 0,
                            "unit": "d",
                            "unit1": "issues",
                            "modalValues": []
                        },
                        {
                            "label": "DOD - Live",
                            "value": 0,
                            "value1": 0,
                            "unit": "d",
                            "unit1": "issues",
                            "modalValues": []
                        }
                    ]
                },
                {
                    "filter1": "Epic",
                    "data": [
                        {
                            "label": "Intake - DOR",
                            "value": 0,
                            "value1": 0,
                            "unit": "d",
                            "unit1": "issues",
                            "modalValues": []
                        },
                        {
                            "label": "DOR - DOD",
                            "value": 0,
                            "value1": 0,
                            "unit": "d",
                            "unit1": "issues",
                            "modalValues": []
                        },
                        {
                            "label": "DOD - Live",
                            "value": 0,
                            "value1": 0,
                            "unit": "d",
                            "unit1": "issues",
                            "modalValues": []
                        }
                    ]
                },
                {
                    "filter1": "Change request",
                    "data": [
                        {
                            "label": "Intake - DOR",
                            "value": 0,
                            "value1": 0,
                            "unit": "d",
                            "unit1": "issues",
                            "modalValues": []
                        },
                        {
                            "label": "DOR - DOD",
                            "value": 0,
                            "value1": 0,
                            "unit": "d",
                            "unit1": "issues",
                            "modalValues": []
                        },
                        {
                            "label": "DOD - Live",
                            "value": 0,
                            "value1": 0,
                            "unit": "d",
                            "unit1": "issues",
                            "modalValues": []
                        }
                    ]
                },
                {
                    "filter1": "Bug",
                    "data": [
                        {
                            "label": "Intake - DOR",
                            "value": 9,
                            "value1": 6,
                            "unit": "d",
                            "unit1": "issues",
                        },
                        {
                            "label": "DOR - DOD",
                            "value": 4,
                            "value1": 6,
                            "unit": "d",
                            "unit1": "issues",
                            "modalValues": []
                        },
                        {
                            "label": "DOD - Live",
                            "value": 7,
                            "value1": 1,
                            "unit": "d",
                            "unit1": "issues",
                            "modalValues": [
                            ]
                        }
                    ]
                },
                {
                    "filter1": "Dependency",
                    "data": [
                        {
                            "label": "Intake - DOR",
                            "value": 0,
                            "value1": 0,
                            "unit": "d",
                            "unit1": "issues",
                            "modalValues": []
                        },
                        {
                            "label": "DOR - DOD",
                            "value": 0,
                            "value1": 0,
                            "unit": "d",
                            "unit1": "issues",
                            "modalValues": []
                        },
                        {
                            "label": "DOD - Live",
                            "value": 0,
                            "value1": 0,
                            "unit": "d",
                            "unit1": "issues",
                            "modalValues": []
                        }
                    ]
                },
                {
                    "filter1": "Enabler Story",
                    "data": [
                        {
                            "label": "Intake - DOR",
                            "value": 0,
                            "value1": 0,
                            "unit": "d",
                            "unit1": "issues",
                            "modalValues": []
                        },
                        {
                            "label": "DOR - DOD",
                            "value": 0,
                            "value1": 0,
                            "unit": "d",
                            "unit1": "issues",
                            "modalValues": []
                        },
                        {
                            "label": "DOD - Live",
                            "value": 0,
                            "value1": 0,
                            "unit": "d",
                            "unit1": "issues",
                            "modalValues": []
                        }
                    ]
                },
                {
                    "filter1": "Story",
                    "data": [
                        {
                            "label": "Intake - DOR",
                            "value": 28,
                            "value1": 99,
                            "unit": "d",
                            "unit1": "issues",
                            "modalValues": [
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-30140",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-30140",
                                    "Issue Description": "Repo tool Configuration Documentation",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "2d ",
                                    "DOR to DOD": "10d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "05-Dec-2023",
                                    "DOD Date": "19-Dec-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-20615",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-20615",
                                    "Issue Description": "Zephyr Server: Testcase fetching mechanism Improvement ",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "44d 3h ",
                                    "DOR to DOD": "101d 1h ",
                                    "DOD to Live": "7d ",
                                    "DOR Date": "06-Feb-2023",
                                    "DOD Date": "27-Jun-2023",
                                    "Live Date": "06-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27490",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27490",
                                    "Issue Description": "ASO | Iteration Readiness KPI",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "15d ",
                                    "DOR to DOD": "25d ",
                                    "DOD to Live": "8d ",
                                    "DOR Date": "21-Aug-2023",
                                    "DOD Date": "25-Sep-2023",
                                    "Live Date": "05-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-20459",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-20459",
                                    "Issue Description": "Priority field filter for Defect Removal efficiency KPI",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "159d ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "07-Jul-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27807",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27807",
                                    "Issue Description": "Fetch sprint data for iteration changes in new processor",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "4d ",
                                    "DOR to DOD": "15d ",
                                    "DOD to Live": "4d ",
                                    "DOR Date": "25-Aug-2023",
                                    "DOD Date": "15-Sep-2023",
                                    "Live Date": "20-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-23728",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-23728",
                                    "Issue Description": "FE - Analyze how the micro-frontend base will work and deployed.",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "3d 2h ",
                                    "DOR to DOD": "24d 1h ",
                                    "DOD to Live": "49d ",
                                    "DOR Date": "27-Mar-2023",
                                    "DOD Date": "28-Apr-2023",
                                    "Live Date": "06-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26955",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26955",
                                    "Issue Description": "Show sprint in filter even when no issue is tagged in a Sprint",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "1d ",
                                    "DOR to DOD": "16d ",
                                    "DOD to Live": "3d 5h ",
                                    "DOR Date": "10-Jul-2023",
                                    "DOD Date": "01-Aug-2023",
                                    "Live Date": "04-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-25745",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-25745",
                                    "Issue Description": "GS: DRE | Definition & Logic mismatch",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "8d ",
                                    "DOR to DOD": "18d 3h ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "28-Jun-2023",
                                    "DOD Date": "24-Jul-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-22593",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-22593",
                                    "Issue Description": "Consume queue data",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "11d ",
                                    "DOR to DOD": "3d ",
                                    "DOD to Live": "104d ",
                                    "DOR Date": "09-Mar-2023",
                                    "DOD Date": "14-Mar-2023",
                                    "Live Date": "04-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-22071",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-22071",
                                    "Issue Description": "AA: Lead time & Average Resolution time KPI changes",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "66d ",
                                    "DOR to DOD": "41d 5h ",
                                    "DOD to Live": "3d ",
                                    "DOR Date": "05-May-2023",
                                    "DOD Date": "03-Jul-2023",
                                    "Live Date": "06-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29784",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29784",
                                    "Issue Description": "Github Action Connection Screen Enhancement",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "17d ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "05-Dec-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28970",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28970",
                                    "Issue Description": "R & R | Integration | Dev Support",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "38d 1h ",
                                    "DOR to DOD": "11d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "20-Nov-2023",
                                    "DOD Date": "05-Dec-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26276",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26276",
                                    "Issue Description": "Create framework Generic pipeline for Data ingestion",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "13d 3h ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "10-Jul-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-23560",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-23560",
                                    "Issue Description": "DEBBIE | Change the APIs to fetch and return multiple date range information",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "62d ",
                                    "DOR to DOD": "8d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "15-Jun-2023",
                                    "DOD Date": "27-Jun-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-23164",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-23164",
                                    "Issue Description": "BE | Field Mapping Segregation",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "7d ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "27-Mar-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26052",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26052",
                                    "Issue Description": "Backend approach for Enlarged view",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "5d 2h ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "27-Jun-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28626",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28626",
                                    "Issue Description": "QE|| Authentication - end to end testing",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "48d 3h ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "20-Nov-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26325",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26325",
                                    "Issue Description": "Jira Processor | Implement custom repo with mongodb and spring batch",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "13d 1h ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "10-Jul-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28624",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28624",
                                    "Issue Description": "FE | Consume auth cookie from Auth to knowhow",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "20d ",
                                    "DOR to DOD": "20d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "11-Oct-2023",
                                    "DOD Date": "07-Nov-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28625",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28625",
                                    "Issue Description": "CH-Knowhow || BE || Integrate central hierarchy service - cache account hierarchy changes",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "48d 3h ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "20-Nov-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26327",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26327",
                                    "Issue Description": "Jira Processor | Group spring batch reader based on jira urls",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "4d ",
                                    "DOR to DOD": "8d ",
                                    "DOD to Live": "44d ",
                                    "DOR Date": "27-Jun-2023",
                                    "DOD Date": "07-Jul-2023",
                                    "Live Date": "06-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28628",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28628",
                                    "Issue Description": "CH-Knowhow || BE || Backward Compatibility for Integrate central hierarchy service",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "48d 3h ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "20-Nov-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-23735",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-23735",
                                    "Issue Description": "Create solution for authentication ",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "13d 2h ",
                                    "DOR to DOD": "33d ",
                                    "DOD to Live": "30d 2h ",
                                    "DOR Date": "10-Apr-2023",
                                    "DOD Date": "25-May-2023",
                                    "Live Date": "06-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-23339",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-23339",
                                    "Issue Description": "UI  | Create Form Generator using the filed configuration json",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "4d 2h ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "27-Mar-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26284",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26284",
                                    "Issue Description": "Debbie | Create KPI PR Size",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "18d 1h ",
                                    "DOR to DOD": "8d ",
                                    "DOD to Live": "14d 5h ",
                                    "DOR Date": "17-Jul-2023",
                                    "DOD Date": "27-Jul-2023",
                                    "Live Date": "16-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-22767",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-22767",
                                    "Issue Description": "Save Jira data",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "26d 1h ",
                                    "DOR to DOD": "5d ",
                                    "DOD to Live": "68d ",
                                    "DOR Date": "10-Apr-2023",
                                    "DOD Date": "17-Apr-2023",
                                    "Live Date": "19-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29557",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29557",
                                    "Issue Description": "Hierarchy Service | BE | Reconstruct APIs",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "4d ",
                                    "DOR to DOD": "18d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "06-Nov-2023",
                                    "DOD Date": "30-Nov-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28623",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28623",
                                    "Issue Description": "CH-Knowhow || BE || Integrate central hierarchy service - Hierarchy Suggestion Changes",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "48d 3h ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "20-Nov-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-22763",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-22763",
                                    "Issue Description": "DEBBIE | For each provider, analyze the documentation to see the availability of retrieving the desired data and define a complete solution",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "27d ",
                                    "DOR to DOD": "17d ",
                                    "DOD to Live": "67d 7h ",
                                    "DOR Date": "10-Apr-2023",
                                    "DOD Date": "03-May-2023",
                                    "Live Date": "04-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29038",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29038",
                                    "Issue Description": "User should have an ability to define Thresholds at Project level for each KPI in Speed, Quality and Value",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "9d ",
                                    "DOR to DOD": "18d 1h ",
                                    "DOD to Live": "16d 5h ",
                                    "DOR Date": "11-Oct-2023",
                                    "DOD Date": "06-Nov-2023",
                                    "Live Date": "28-Nov-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26324",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26324",
                                    "Issue Description": "Jira processor | Implement custom retry with spring batch reader",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "13d 1h ",
                                    "DOR to DOD": "56d ",
                                    "DOD to Live": "26d ",
                                    "DOR Date": "10-Jul-2023",
                                    "DOD Date": "26-Sep-2023",
                                    "Live Date": "31-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-25752",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-25752",
                                    "Issue Description": "Sprint capacity Y-axis should be days instead of hours",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "7d ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "27-Jun-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28621",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28621",
                                    "Issue Description": "CH-Knowhow || BE || Update Current Implementation of Hierarchy to support-id based hierarchy service",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "48d 3h ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "20-Nov-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-23784",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-23784",
                                    "Issue Description": "Milestone Board Addition",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "3d 1h ",
                                    "DOR to DOD": "27d 2h ",
                                    "DOD to Live": "52d ",
                                    "DOR Date": "27-Mar-2023",
                                    "DOD Date": "03-May-2023",
                                    "Live Date": "14-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-25720",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-25720",
                                    "Issue Description": "EJ.com: Value Dashboard | PI Predictability KPI ",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "26d ",
                                    "DOR to DOD": "23d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "21-Jul-2023",
                                    "DOD Date": "23-Aug-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-23785",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-23785",
                                    "Issue Description": "FE - Specifications for Micro-frontend and rest api compliance",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "13d ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "10-Apr-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28673",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28673",
                                    "Issue Description": "Change Iteration KPIs according to Base Class",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "35d ",
                                    "DOR to DOD": "13d 5h ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "01-Nov-2023",
                                    "DOD Date": "20-Nov-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-23380",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-23380",
                                    "Issue Description": "BE | Json contract + collections to make the form generator and field mapping",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "4d 2h ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "27-Mar-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28674",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28674",
                                    "Issue Description": "QE || End to end Testing - all KPIs ",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "59d ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "05-Dec-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27186",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27186",
                                    "Issue Description": "Show Sprint Names and KPI Values on one project selection",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "7d ",
                                    "DOR to DOD": "10d ",
                                    "DOD to Live": "6d ",
                                    "DOR Date": "26-Jul-2023",
                                    "DOD Date": "08-Aug-2023",
                                    "Live Date": "16-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-24592",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-24592",
                                    "Issue Description": "Jira processor dev testing (Kanban)",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "10d 1h ",
                                    "DOR to DOD": "101d ",
                                    "DOD to Live": "26d ",
                                    "DOR Date": "08-May-2023",
                                    "DOD Date": "26-Sep-2023",
                                    "Live Date": "31-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28677",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28677",
                                    "Issue Description": " FE | Cookie flow Impl under same sub domain centralAuth/Knowhow/Map/Retro",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "38d ",
                                    "DOR to DOD": "28d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "06-Nov-2023",
                                    "DOD Date": "14-Dec-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-22335",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-22335",
                                    "Issue Description": "QE|| Jira processor end to end testing (Scrum)",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "94d ",
                                    "DOR to DOD": "77d 5h ",
                                    "DOD to Live": "14d 3h ",
                                    "DOR Date": "26-Jun-2023",
                                    "DOD Date": "11-Oct-2023",
                                    "Live Date": "31-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28678",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28678",
                                    "Issue Description": "3. Debbie | Optimize Debbie framework in order to receive a high load of requests - Repo Activity & MR Life Cycle",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "20d ",
                                    "DOR to DOD": "12d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "11-Oct-2023",
                                    "DOD Date": "27-Oct-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-22334",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-22334",
                                    "Issue Description": "Jira processor dev testing (Scrum)",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "59d 5h ",
                                    "DOR to DOD": "101d ",
                                    "DOD to Live": "26d ",
                                    "DOR Date": "08-May-2023",
                                    "DOD Date": "26-Sep-2023",
                                    "Live Date": "31-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26136",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26136",
                                    "Issue Description": "Release Dashboard | Sub task level bug to included in Fix version issue count",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "14d ",
                                    "DOR to DOD": "36d ",
                                    "DOD to Live": "6d 3h ",
                                    "DOR Date": "10-Jul-2023",
                                    "DOD Date": "29-Aug-2023",
                                    "Live Date": "06-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28967",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28967",
                                    "Issue Description": "KnowHOW | BE |  Cache policy rules and check users with policy rules. ",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "10d ",
                                    "DOR to DOD": "20d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "11-Oct-2023",
                                    "DOD Date": "07-Nov-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-25974",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-25974",
                                    "Issue Description": "User should see real time data on iteration dashboard",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "5d 3h ",
                                    "DOR to DOD": "22d 1h ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "27-Jun-2023",
                                    "DOD Date": "27-Jul-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28968",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28968",
                                    "Issue Description": "CA-Knowhow || BE || DB script for central Auth integrate",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "60d 6h ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "20-Dec-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-24006",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-24006",
                                    "Issue Description": "Integrate with PSST Tool& analyze profiling ",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "0d",
                                    "DOR to DOD": "8d ",
                                    "DOD to Live": "72d ",
                                    "DOR Date": "27-Mar-2023",
                                    "DOD Date": "06-Apr-2023",
                                    "Live Date": "14-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29818",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29818",
                                    "Issue Description": "Backlog | Iteration Readiness - Club statuses based on mappings",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "3d 6h ",
                                    "DOR to DOD": "9d ",
                                    "DOD to Live": "7d ",
                                    "DOR Date": "21-Nov-2023",
                                    "DOD Date": "04-Dec-2023",
                                    "Live Date": "13-Dec-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26268",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26268",
                                    "Issue Description": "Tech stack for generic Pipeline",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "4d 3h ",
                                    "DOR to DOD": "15d 2h ",
                                    "DOD to Live": "36d ",
                                    "DOR Date": "27-Jun-2023",
                                    "DOD Date": "18-Jul-2023",
                                    "Live Date": "06-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-30187",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-30187",
                                    "Issue Description": "DSV - In progress filter on chart should work as per configuration in settings (Status)",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "3h ",
                                    "DOR to DOD": "12d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "05-Dec-2023",
                                    "DOD Date": "21-Dec-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27967",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27967",
                                    "Issue Description": "EJ: Scope Churn KPI - ",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "13d ",
                                    "DOR to DOD": "2d ",
                                    "DOD to Live": "4d ",
                                    "DOR Date": "13-Sep-2023",
                                    "DOD Date": "15-Sep-2023",
                                    "Live Date": "20-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-25667",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-25667",
                                    "Issue Description": "User is able to expand the KPI to full screen width and height of the browser",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "8d 2h ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "27-Jun-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28658",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28658",
                                    "Issue Description": "Deploy using Azure DevOps CI/CD Pipeline",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "38d 2h ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "06-Nov-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29344",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29344",
                                    "Issue Description": "FE | AUTH & AUTH cookie Resource wise",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "13d 4h ",
                                    "DOR to DOD": "11d 3h ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "06-Nov-2023",
                                    "DOD Date": "21-Nov-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28651",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28651",
                                    "Issue Description": "Knowhow | FE | Raise request & grant access management flow ",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "38d 3h ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "06-Nov-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-23482",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-23482",
                                    "Issue Description": "DEBBIE Setup  | Implement defined solution to lower number of containers and resource consumption",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "14d ",
                                    "DOR to DOD": "9d ",
                                    "DOD to Live": "76d ",
                                    "DOR Date": "10-Apr-2023",
                                    "DOD Date": "21-Apr-2023",
                                    "Live Date": "04-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28897",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28897",
                                    "Issue Description": "Central Auth | Design Architecture Diagram (Interact with all Micro Services)",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "11d ",
                                    "DOR to DOD": "19d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "11-Oct-2023",
                                    "DOD Date": "07-Nov-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28655",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28655",
                                    "Issue Description": "CH-Knowhow | FE |  Update Current Implementation of Hierarchy to support-id based hierarchy service",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "48d 2h ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "20-Nov-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28653",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28653",
                                    "Issue Description": "KnowHOW | FE | Create project on central auth & auth",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "20d ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "11-Oct-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28896",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28896",
                                    "Issue Description": "KnowHOW | BE | Integrate Authentication service with login page",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "11d ",
                                    "DOR to DOD": "11d 7h ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "11-Oct-2023",
                                    "DOD Date": "26-Oct-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-21067",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-21067",
                                    "Issue Description": "QE || - Field Mapping Template on Jira Configuration screen - Scrum/Kanban",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "138d ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "26-Jun-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-30174",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-30174",
                                    "Issue Description": "R & R | Integration | Dev Support",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "5h ",
                                    "DOR to DOD": "7d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "05-Dec-2023",
                                    "DOD Date": "14-Dec-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28790",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28790",
                                    "Issue Description": "Analyse single spa and react vs angular for knowhow ",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "5d ",
                                    "DOR to DOD": "32d 5h ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "21-Sep-2023",
                                    "DOD Date": "06-Nov-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28791",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28791",
                                    "Issue Description": "QE|| Automation - Updating scripts and new script for UI enhancements",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "58d 2h ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "05-Dec-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-24192",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-24192",
                                    "Issue Description": "Backlog: Issue type configuration for Production defects ageing KPI",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "19d ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "26-Apr-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29914",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29914",
                                    "Issue Description": "UI: Release Burnup X Axis interval logic on Iteration Burnup",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "9d 6h ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "05-Dec-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-22440",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-22440",
                                    "Issue Description": "SBR: Defect Rejection Rate | Improvement to Logic / Mapping",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "94d 2h ",
                                    "DOR to DOD": "26d ",
                                    "DOD to Live": "3d ",
                                    "DOR Date": "27-Jun-2023",
                                    "DOD Date": "02-Aug-2023",
                                    "Live Date": "04-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28789",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28789",
                                    "Issue Description": "QE || Testing - Writing testcases and execution - UI enhancement",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "58d 2h ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "05-Dec-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29879",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29879",
                                    "Issue Description": "FE - Create additional filters and add show/hide, table, graph icons",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "11d ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "05-Dec-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26120",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26120",
                                    "Issue Description": "UI|Option to Extract Reports as image/pdf",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "5d ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "27-Jun-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28787",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28787",
                                    "Issue Description": "QE|| Automation - Developer tab and Repo KPIS",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "58d 3h ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "05-Dec-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29847",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29847",
                                    "Issue Description": "Release & Backlog Dashboard | Epic by Progress",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "1d ",
                                    "DOR to DOD": "8d ",
                                    "DOD to Live": "8d ",
                                    "DOR Date": "21-Nov-2023",
                                    "DOD Date": "01-Dec-2023",
                                    "Live Date": "13-Dec-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29848",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29848",
                                    "Issue Description": "Release Dashboard | Show legends by default on Defects by Testing Phase KPI",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "1d ",
                                    "DOR to DOD": "7d ",
                                    "DOD to Live": "9d 1h ",
                                    "DOR Date": "21-Nov-2023",
                                    "DOD Date": "30-Nov-2023",
                                    "Live Date": "13-Dec-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28638",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28638",
                                    "Issue Description": "KnowHOW | BE | Update existing spring security- token expiration",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "20d ",
                                    "DOR to DOD": "20d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "11-Oct-2023",
                                    "DOD Date": "07-Nov-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28635",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28635",
                                    "Issue Description": " BE | Cookie flow Impl under same sub domain Knowhow/Map/Retro",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "38d 3h ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "06-Nov-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-24677",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-24677",
                                    "Issue Description": "KPI level threshold should be configurable through UI",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "44d ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "27-Jun-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-24556",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-24556",
                                    "Issue Description": "Sunbelt Rentals | Regression Automation Coverage Enhancement",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "24d ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "23-May-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28636",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28636",
                                    "Issue Description": "BE | Same way of getting authorization flow on different platforms(Map/R&R) Support",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "38d 3h ",
                                    "DOR to DOD": "12d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "06-Nov-2023",
                                    "DOD Date": "22-Nov-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26054",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26054",
                                    "Issue Description": "UI Widget on updated Json",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "5d 2h ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "27-Jun-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28633",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28633",
                                    "Issue Description": "Central Auth | FE | forgot password screen",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "38d 3h ",
                                    "DOR to DOD": "11d 1h ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "06-Nov-2023",
                                    "DOD Date": "21-Nov-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28634",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28634",
                                    "Issue Description": "QE|| Integration Testing - Integrate other platforms with KnowHOW",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "59d 4h ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "05-Dec-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28632",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28632",
                                    "Issue Description": "CH- QE || Central Hierarchy - end to end testing",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "48d 3h ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "20-Nov-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26213",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26213",
                                    "Issue Description": "1. Signup functionality implementaion for standard user",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "5d ",
                                    "DOR to DOD": "18d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "27-Jun-2023",
                                    "DOD Date": "21-Jul-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26192",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26192",
                                    "Issue Description": "Backlog KPI - Count by issue type",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "14d ",
                                    "DOR to DOD": "6d ",
                                    "DOD to Live": "2d ",
                                    "DOR Date": "10-Jul-2023",
                                    "DOD Date": "18-Jul-2023",
                                    "Live Date": "19-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28923",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28923",
                                    "Issue Description": "CH-KnowHow | Central Hierarchy Integration Dev Testing end to end",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "38d 4h ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "20-Nov-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28921",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28921",
                                    "Issue Description": "Central Hierarchy | Integration Dev Testing end to end",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "38d 4h ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "20-Nov-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28647",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28647",
                                    "Issue Description": "Central Auth | BE | DB script to migrate authorization Permission to central auth & auth.",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "48d 3h ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "20-Nov-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-23479",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-23479",
                                    "Issue Description": "DEBBIE Setup | Analyse and define complete solution to combine multiple containers and lower resource consumption under 4GB",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "4d ",
                                    "DOR to DOD": "11d ",
                                    "DOD to Live": "84d ",
                                    "DOR Date": "27-Mar-2023",
                                    "DOD Date": "11-Apr-2023",
                                    "Live Date": "04-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28640",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28640",
                                    "Issue Description": "Central Auth | BE | DB script for central auth & auth",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "38d 3h ",
                                    "DOR to DOD": "30d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "06-Nov-2023",
                                    "DOD Date": "18-Dec-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-25131",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-25131",
                                    "Issue Description": "3. policy resolver engine",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "24d 7h ",
                                    "DOR to DOD": "19d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "27-Jun-2023",
                                    "DOD Date": "24-Jul-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26341",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26341",
                                    "Issue Description": "UI | Json finalization (Screen 1)",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "4d ",
                                    "DOR to DOD": "18d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "27-Jun-2023",
                                    "DOD Date": "21-Jul-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26220",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26220",
                                    "Issue Description": "Tech debt for each sprint (Iteration Dashboard)",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "4d 5h ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "27-Jun-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-25132",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-25132",
                                    "Issue Description": "Backend|CRUD for authorization (project permissions, hierarchy)",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "33d 7h ",
                                    "DOR to DOD": "56d ",
                                    "DOD to Live": "25d 4h ",
                                    "DOR Date": "10-Jul-2023",
                                    "DOD Date": "26-Sep-2023",
                                    "Live Date": "31-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28920",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28920",
                                    "Issue Description": "Central AUTH | Integration Dev Testing end to end",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "38d 4h ",
                                    "DOR to DOD": "20d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "20-Nov-2023",
                                    "DOD Date": "18-Dec-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29215",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29215",
                                    "Issue Description": "Define maturity ranges for DORA metrics",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "14d ",
                                    "DOR to DOD": "28d 3h ",
                                    "DOD to Live": "3d ",
                                    "DOR Date": "31-Oct-2023",
                                    "DOD Date": "08-Dec-2023",
                                    "Live Date": "13-Dec-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26345",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26345",
                                    "Issue Description": "QE||Automation - Daily stand view",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "119d 1h ",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "NA",
                                    "DOR Date": "05-Dec-2023",
                                    "DOD Date": "NA",
                                    "Live Date": "NA"
                                }
                            ]
                        },
                        {
                            "label": "DOR - DOD",
                            "value": 23,
                            "value1": 58,
                            "unit": "d",
                            "unit1": "issues",
                            "modalValues": [
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-30140",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-30140",
                                    "Issue Description": "Repo tool Configuration Documentation",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "2d ",
                                    "DOR to DOD": "10d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "05-Dec-2023",
                                    "DOD Date": "19-Dec-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-20615",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-20615",
                                    "Issue Description": "Zephyr Server: Testcase fetching mechanism Improvement ",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "44d 3h ",
                                    "DOR to DOD": "101d 1h ",
                                    "DOD to Live": "7d ",
                                    "DOR Date": "06-Feb-2023",
                                    "DOD Date": "27-Jun-2023",
                                    "Live Date": "06-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27490",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27490",
                                    "Issue Description": "ASO | Iteration Readiness KPI",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "15d ",
                                    "DOR to DOD": "25d ",
                                    "DOD to Live": "8d ",
                                    "DOR Date": "21-Aug-2023",
                                    "DOD Date": "25-Sep-2023",
                                    "Live Date": "05-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27807",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27807",
                                    "Issue Description": "Fetch sprint data for iteration changes in new processor",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "4d ",
                                    "DOR to DOD": "15d ",
                                    "DOD to Live": "4d ",
                                    "DOR Date": "25-Aug-2023",
                                    "DOD Date": "15-Sep-2023",
                                    "Live Date": "20-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-23728",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-23728",
                                    "Issue Description": "FE - Analyze how the micro-frontend base will work and deployed.",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "3d 2h ",
                                    "DOR to DOD": "24d 1h ",
                                    "DOD to Live": "49d ",
                                    "DOR Date": "27-Mar-2023",
                                    "DOD Date": "28-Apr-2023",
                                    "Live Date": "06-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-30187",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-30187",
                                    "Issue Description": "DSV - In progress filter on chart should work as per configuration in settings (Status)",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "3h ",
                                    "DOR to DOD": "12d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "05-Dec-2023",
                                    "DOD Date": "21-Dec-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27967",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27967",
                                    "Issue Description": "EJ: Scope Churn KPI - ",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "13d ",
                                    "DOR to DOD": "2d ",
                                    "DOD to Live": "4d ",
                                    "DOR Date": "13-Sep-2023",
                                    "DOD Date": "15-Sep-2023",
                                    "Live Date": "20-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26955",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26955",
                                    "Issue Description": "Show sprint in filter even when no issue is tagged in a Sprint",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "1d ",
                                    "DOR to DOD": "16d ",
                                    "DOD to Live": "3d 5h ",
                                    "DOR Date": "10-Jul-2023",
                                    "DOD Date": "01-Aug-2023",
                                    "Live Date": "04-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-25745",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-25745",
                                    "Issue Description": "GS: DRE | Definition & Logic mismatch",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "8d ",
                                    "DOR to DOD": "18d 3h ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "28-Jun-2023",
                                    "DOD Date": "24-Jul-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-22593",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-22593",
                                    "Issue Description": "Consume queue data",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "11d ",
                                    "DOR to DOD": "3d ",
                                    "DOD to Live": "104d ",
                                    "DOR Date": "09-Mar-2023",
                                    "DOD Date": "14-Mar-2023",
                                    "Live Date": "04-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-22071",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-22071",
                                    "Issue Description": "AA: Lead time & Average Resolution time KPI changes",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "66d ",
                                    "DOR to DOD": "41d 5h ",
                                    "DOD to Live": "3d ",
                                    "DOR Date": "05-May-2023",
                                    "DOD Date": "03-Jul-2023",
                                    "Live Date": "06-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29344",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29344",
                                    "Issue Description": "FE | AUTH & AUTH cookie Resource wise",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "13d 4h ",
                                    "DOR to DOD": "11d 3h ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "06-Nov-2023",
                                    "DOD Date": "21-Nov-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28970",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28970",
                                    "Issue Description": "R & R | Integration | Dev Support",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "38d 1h ",
                                    "DOR to DOD": "11d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "20-Nov-2023",
                                    "DOD Date": "05-Dec-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-23482",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-23482",
                                    "Issue Description": "DEBBIE Setup  | Implement defined solution to lower number of containers and resource consumption",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "14d ",
                                    "DOR to DOD": "9d ",
                                    "DOD to Live": "76d ",
                                    "DOR Date": "10-Apr-2023",
                                    "DOD Date": "21-Apr-2023",
                                    "Live Date": "04-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28897",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28897",
                                    "Issue Description": "Central Auth | Design Architecture Diagram (Interact with all Micro Services)",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "11d ",
                                    "DOR to DOD": "19d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "11-Oct-2023",
                                    "DOD Date": "07-Nov-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-23560",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-23560",
                                    "Issue Description": "DEBBIE | Change the APIs to fetch and return multiple date range information",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "62d ",
                                    "DOR to DOD": "8d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "15-Jun-2023",
                                    "DOD Date": "27-Jun-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28896",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28896",
                                    "Issue Description": "KnowHOW | BE | Integrate Authentication service with login page",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "11d ",
                                    "DOR to DOD": "11d 7h ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "11-Oct-2023",
                                    "DOD Date": "26-Oct-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-30174",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-30174",
                                    "Issue Description": "R & R | Integration | Dev Support",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "5h ",
                                    "DOR to DOD": "7d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "05-Dec-2023",
                                    "DOD Date": "14-Dec-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28790",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28790",
                                    "Issue Description": "Analyse single spa and react vs angular for knowhow ",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "5d ",
                                    "DOR to DOD": "32d 5h ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "21-Sep-2023",
                                    "DOD Date": "06-Nov-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-22440",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-22440",
                                    "Issue Description": "SBR: Defect Rejection Rate | Improvement to Logic / Mapping",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "94d 2h ",
                                    "DOR to DOD": "26d ",
                                    "DOD to Live": "3d ",
                                    "DOR Date": "27-Jun-2023",
                                    "DOD Date": "02-Aug-2023",
                                    "Live Date": "04-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28624",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28624",
                                    "Issue Description": "FE | Consume auth cookie from Auth to knowhow",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "20d ",
                                    "DOR to DOD": "20d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "11-Oct-2023",
                                    "DOD Date": "07-Nov-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26327",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26327",
                                    "Issue Description": "Jira Processor | Group spring batch reader based on jira urls",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "4d ",
                                    "DOR to DOD": "8d ",
                                    "DOD to Live": "44d ",
                                    "DOR Date": "27-Jun-2023",
                                    "DOD Date": "07-Jul-2023",
                                    "Live Date": "06-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-23735",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-23735",
                                    "Issue Description": "Create solution for authentication ",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "13d 2h ",
                                    "DOR to DOD": "33d ",
                                    "DOD to Live": "30d 2h ",
                                    "DOR Date": "10-Apr-2023",
                                    "DOD Date": "25-May-2023",
                                    "Live Date": "06-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26284",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26284",
                                    "Issue Description": "Debbie | Create KPI PR Size",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "18d 1h ",
                                    "DOR to DOD": "8d ",
                                    "DOD to Live": "14d 5h ",
                                    "DOR Date": "17-Jul-2023",
                                    "DOD Date": "27-Jul-2023",
                                    "Live Date": "16-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-22767",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-22767",
                                    "Issue Description": "Save Jira data",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "26d 1h ",
                                    "DOR to DOD": "5d ",
                                    "DOD to Live": "68d ",
                                    "DOR Date": "10-Apr-2023",
                                    "DOD Date": "17-Apr-2023",
                                    "Live Date": "19-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29557",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29557",
                                    "Issue Description": "Hierarchy Service | BE | Reconstruct APIs",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "4d ",
                                    "DOR to DOD": "18d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "06-Nov-2023",
                                    "DOD Date": "30-Nov-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-22763",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-22763",
                                    "Issue Description": "DEBBIE | For each provider, analyze the documentation to see the availability of retrieving the desired data and define a complete solution",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "27d ",
                                    "DOR to DOD": "17d ",
                                    "DOD to Live": "67d 7h ",
                                    "DOR Date": "10-Apr-2023",
                                    "DOD Date": "03-May-2023",
                                    "Live Date": "04-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29038",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29038",
                                    "Issue Description": "User should have an ability to define Thresholds at Project level for each KPI in Speed, Quality and Value",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "9d ",
                                    "DOR to DOD": "18d 1h ",
                                    "DOD to Live": "16d 5h ",
                                    "DOR Date": "11-Oct-2023",
                                    "DOD Date": "06-Nov-2023",
                                    "Live Date": "28-Nov-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26324",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26324",
                                    "Issue Description": "Jira processor | Implement custom retry with spring batch reader",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "13d 1h ",
                                    "DOR to DOD": "56d ",
                                    "DOD to Live": "26d ",
                                    "DOR Date": "10-Jul-2023",
                                    "DOD Date": "26-Sep-2023",
                                    "Live Date": "31-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29847",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29847",
                                    "Issue Description": "Release & Backlog Dashboard | Epic by Progress",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "1d ",
                                    "DOR to DOD": "8d ",
                                    "DOD to Live": "8d ",
                                    "DOR Date": "21-Nov-2023",
                                    "DOD Date": "01-Dec-2023",
                                    "Live Date": "13-Dec-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-23784",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-23784",
                                    "Issue Description": "Milestone Board Addition",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "3d 1h ",
                                    "DOR to DOD": "27d 2h ",
                                    "DOD to Live": "52d ",
                                    "DOR Date": "27-Mar-2023",
                                    "DOD Date": "03-May-2023",
                                    "Live Date": "14-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29848",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29848",
                                    "Issue Description": "Release Dashboard | Show legends by default on Defects by Testing Phase KPI",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "1d ",
                                    "DOR to DOD": "7d ",
                                    "DOD to Live": "9d 1h ",
                                    "DOR Date": "21-Nov-2023",
                                    "DOD Date": "30-Nov-2023",
                                    "Live Date": "13-Dec-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28638",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28638",
                                    "Issue Description": "KnowHOW | BE | Update existing spring security- token expiration",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "20d ",
                                    "DOR to DOD": "20d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "11-Oct-2023",
                                    "DOD Date": "07-Nov-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-25720",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-25720",
                                    "Issue Description": "EJ.com: Value Dashboard | PI Predictability KPI ",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "26d ",
                                    "DOR to DOD": "23d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "21-Jul-2023",
                                    "DOD Date": "23-Aug-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28636",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28636",
                                    "Issue Description": "BE | Same way of getting authorization flow on different platforms(Map/R&R) Support",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "38d 3h ",
                                    "DOR to DOD": "12d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "06-Nov-2023",
                                    "DOD Date": "22-Nov-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28673",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28673",
                                    "Issue Description": "Change Iteration KPIs according to Base Class",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "35d ",
                                    "DOR to DOD": "13d 5h ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "01-Nov-2023",
                                    "DOD Date": "20-Nov-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27186",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27186",
                                    "Issue Description": "Show Sprint Names and KPI Values on one project selection",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "7d ",
                                    "DOR to DOD": "10d ",
                                    "DOD to Live": "6d ",
                                    "DOR Date": "26-Jul-2023",
                                    "DOD Date": "08-Aug-2023",
                                    "Live Date": "16-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-24592",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-24592",
                                    "Issue Description": "Jira processor dev testing (Kanban)",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "10d 1h ",
                                    "DOR to DOD": "101d ",
                                    "DOD to Live": "26d ",
                                    "DOR Date": "08-May-2023",
                                    "DOD Date": "26-Sep-2023",
                                    "Live Date": "31-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28677",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28677",
                                    "Issue Description": " FE | Cookie flow Impl under same sub domain centralAuth/Knowhow/Map/Retro",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "38d ",
                                    "DOR to DOD": "28d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "06-Nov-2023",
                                    "DOD Date": "14-Dec-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28633",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28633",
                                    "Issue Description": "Central Auth | FE | forgot password screen",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "38d 3h ",
                                    "DOR to DOD": "11d 1h ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "06-Nov-2023",
                                    "DOD Date": "21-Nov-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-22335",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-22335",
                                    "Issue Description": "QE|| Jira processor end to end testing (Scrum)",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "94d ",
                                    "DOR to DOD": "77d 5h ",
                                    "DOD to Live": "14d 3h ",
                                    "DOR Date": "26-Jun-2023",
                                    "DOD Date": "11-Oct-2023",
                                    "Live Date": "31-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28678",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28678",
                                    "Issue Description": "3. Debbie | Optimize Debbie framework in order to receive a high load of requests - Repo Activity & MR Life Cycle",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "20d ",
                                    "DOR to DOD": "12d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "11-Oct-2023",
                                    "DOD Date": "27-Oct-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-22334",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-22334",
                                    "Issue Description": "Jira processor dev testing (Scrum)",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "59d 5h ",
                                    "DOR to DOD": "101d ",
                                    "DOD to Live": "26d ",
                                    "DOR Date": "08-May-2023",
                                    "DOD Date": "26-Sep-2023",
                                    "Live Date": "31-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26213",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26213",
                                    "Issue Description": "1. Signup functionality implementaion for standard user",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "5d ",
                                    "DOR to DOD": "18d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "27-Jun-2023",
                                    "DOD Date": "21-Jul-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26136",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26136",
                                    "Issue Description": "Release Dashboard | Sub task level bug to included in Fix version issue count",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "14d ",
                                    "DOR to DOD": "36d ",
                                    "DOD to Live": "6d 3h ",
                                    "DOR Date": "10-Jul-2023",
                                    "DOD Date": "29-Aug-2023",
                                    "Live Date": "06-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26192",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26192",
                                    "Issue Description": "Backlog KPI - Count by issue type",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "14d ",
                                    "DOR to DOD": "6d ",
                                    "DOD to Live": "2d ",
                                    "DOR Date": "10-Jul-2023",
                                    "DOD Date": "18-Jul-2023",
                                    "Live Date": "19-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28967",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28967",
                                    "Issue Description": "KnowHOW | BE |  Cache policy rules and check users with policy rules. ",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "10d ",
                                    "DOR to DOD": "20d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "11-Oct-2023",
                                    "DOD Date": "07-Nov-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-25974",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-25974",
                                    "Issue Description": "User should see real time data on iteration dashboard",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "5d 3h ",
                                    "DOR to DOD": "22d 1h ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "27-Jun-2023",
                                    "DOD Date": "27-Jul-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-24006",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-24006",
                                    "Issue Description": "Integrate with PSST Tool& analyze profiling ",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "0d",
                                    "DOR to DOD": "8d ",
                                    "DOD to Live": "72d ",
                                    "DOR Date": "27-Mar-2023",
                                    "DOD Date": "06-Apr-2023",
                                    "Live Date": "14-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-23479",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-23479",
                                    "Issue Description": "DEBBIE Setup | Analyse and define complete solution to combine multiple containers and lower resource consumption under 4GB",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "4d ",
                                    "DOR to DOD": "11d ",
                                    "DOD to Live": "84d ",
                                    "DOR Date": "27-Mar-2023",
                                    "DOD Date": "11-Apr-2023",
                                    "Live Date": "04-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29818",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29818",
                                    "Issue Description": "Backlog | Iteration Readiness - Club statuses based on mappings",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "3d 6h ",
                                    "DOR to DOD": "9d ",
                                    "DOD to Live": "7d ",
                                    "DOR Date": "21-Nov-2023",
                                    "DOD Date": "04-Dec-2023",
                                    "Live Date": "13-Dec-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28640",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28640",
                                    "Issue Description": "Central Auth | BE | DB script for central auth & auth",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "38d 3h ",
                                    "DOR to DOD": "30d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "06-Nov-2023",
                                    "DOD Date": "18-Dec-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-25131",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-25131",
                                    "Issue Description": "3. policy resolver engine",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "24d 7h ",
                                    "DOR to DOD": "19d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "27-Jun-2023",
                                    "DOD Date": "24-Jul-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26341",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26341",
                                    "Issue Description": "UI | Json finalization (Screen 1)",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "4d ",
                                    "DOR to DOD": "18d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "27-Jun-2023",
                                    "DOD Date": "21-Jul-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-25132",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-25132",
                                    "Issue Description": "Backend|CRUD for authorization (project permissions, hierarchy)",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "33d 7h ",
                                    "DOR to DOD": "56d ",
                                    "DOD to Live": "25d 4h ",
                                    "DOR Date": "10-Jul-2023",
                                    "DOD Date": "26-Sep-2023",
                                    "Live Date": "31-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28920",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28920",
                                    "Issue Description": "Central AUTH | Integration Dev Testing end to end",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "38d 4h ",
                                    "DOR to DOD": "20d ",
                                    "DOD to Live": "NA",
                                    "DOR Date": "20-Nov-2023",
                                    "DOD Date": "18-Dec-2023",
                                    "Live Date": "NA"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29215",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29215",
                                    "Issue Description": "Define maturity ranges for DORA metrics",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "14d ",
                                    "DOR to DOD": "28d 3h ",
                                    "DOD to Live": "3d ",
                                    "DOR Date": "31-Oct-2023",
                                    "DOD Date": "08-Dec-2023",
                                    "Live Date": "13-Dec-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26268",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26268",
                                    "Issue Description": "Tech stack for generic Pipeline",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "4d 3h ",
                                    "DOR to DOD": "15d 2h ",
                                    "DOD to Live": "36d ",
                                    "DOR Date": "27-Jun-2023",
                                    "DOD Date": "18-Jul-2023",
                                    "Live Date": "06-Sep-2023"
                                }
                            ]
                        },
                        {
                            "label": "DOD - Live",
                            "value": 19,
                            "value1": 187,
                            "unit": "d",
                            "unit1": "issues",
                            "modalValues": [
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29395",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29395",
                                    "Issue Description": "KH | Disable original repo processors containers when Debbie is deployed",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "0d",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "28-Nov-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27491",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27491",
                                    "Issue Description": "AA | Commitment Reliability - Change in logic to calculate Delivered for Initial Commitment case",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "3d 1h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "16-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-20615",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-20615",
                                    "Issue Description": "Zephyr Server: Testcase fetching mechanism Improvement ",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "44d 3h ",
                                    "DOR to DOD": "101d 1h ",
                                    "DOD to Live": "7d ",
                                    "DOR Date": "06-Feb-2023",
                                    "DOD Date": "27-Jun-2023",
                                    "Live Date": "06-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29396",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29396",
                                    "Issue Description": "Upgrade repo processors cron before upgrading server",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "0d",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "28-Nov-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27490",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27490",
                                    "Issue Description": "ASO | Iteration Readiness KPI",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "15d ",
                                    "DOR to DOD": "25d ",
                                    "DOD to Live": "8d ",
                                    "DOR Date": "21-Aug-2023",
                                    "DOD Date": "25-Sep-2023",
                                    "Live Date": "05-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26282",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26282",
                                    "Issue Description": "Debbie | Create KPI Review time",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "28d 6h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "16-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26161",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26161",
                                    "Issue Description": "UI|Comments on Release & Backlog tab",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "1d 7h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "14-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27808",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27808",
                                    "Issue Description": "Create job parameter and reader to call jira batch from customapi",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "8d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "20-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27807",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27807",
                                    "Issue Description": "Fetch sprint data for iteration changes in new processor",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "4d ",
                                    "DOR to DOD": "15d ",
                                    "DOD to Live": "4d ",
                                    "DOR Date": "25-Aug-2023",
                                    "DOD Date": "15-Sep-2023",
                                    "Live Date": "20-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-23728",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-23728",
                                    "Issue Description": "FE - Analyze how the micro-frontend base will work and deployed.",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "3d 2h ",
                                    "DOR to DOD": "24d 1h ",
                                    "DOD to Live": "49d ",
                                    "DOR Date": "27-Mar-2023",
                                    "DOD Date": "28-Apr-2023",
                                    "Live Date": "06-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27525",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27525",
                                    "Issue Description": "CRUD for roles",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "25d 4h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "31-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27129",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27129",
                                    "Issue Description": "New Metadata identifier template impl'",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "27d 7h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "06-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-24774",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-24774",
                                    "Issue Description": "Documentation: Hardware Specs with Performance details",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "3d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "06-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-23685",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-23685",
                                    "Issue Description": "QE || Testing - Azure Kpis on dashboards (speed/quality/value) - scrum/kanban",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "17d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "06-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27524",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27524",
                                    "Issue Description": "API to fetch hierarchy groups based on resource",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "45d 5h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "31-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27249",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27249",
                                    "Issue Description": "Move the Capacity Planning to the left nav",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "2d 3h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "16-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-24533",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-24533",
                                    "Issue Description": "Defect Count By Status on Iteration",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "76d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "04-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26955",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26955",
                                    "Issue Description": "Show sprint in filter even when no issue is tagged in a Sprint",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "1d ",
                                    "DOR to DOD": "16d ",
                                    "DOD to Live": "3d 5h ",
                                    "DOR Date": "10-Jul-2023",
                                    "DOD Date": "01-Aug-2023",
                                    "Live Date": "04-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-22593",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-22593",
                                    "Issue Description": "Consume queue data",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "11d ",
                                    "DOR to DOD": "3d ",
                                    "DOD to Live": "104d ",
                                    "DOR Date": "09-Mar-2023",
                                    "DOD Date": "14-Mar-2023",
                                    "Live Date": "04-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28856",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28856",
                                    "Issue Description": "Subtask level issues should not be displayed on the KPI",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "7d 4h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "05-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27526",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27526",
                                    "Issue Description": "Create a separate tab for Repo KPIs (non-jira KPIs)",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "5d 3h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "05-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-22592",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-22592",
                                    "Issue Description": "Post issue data on queue",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "109d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "04-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27806",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27806",
                                    "Issue Description": "Update jira processor for new changes",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "17d 6h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "20-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27805",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27805",
                                    "Issue Description": "Implement Job for kanban-JQL",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "14d 6h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "20-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-22071",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-22071",
                                    "Issue Description": "AA: Lead time & Average Resolution time KPI changes",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "66d ",
                                    "DOR to DOD": "41d 5h ",
                                    "DOD to Live": "3d ",
                                    "DOR Date": "05-May-2023",
                                    "DOD Date": "03-Jul-2023",
                                    "Live Date": "06-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28574",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28574",
                                    "Issue Description": "Teaching Strategies: Code Quality KPI SonarQube",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "12d 4h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "28-Nov-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26153",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26153",
                                    "Issue Description": "Release Dashboard | Filter enhancement to show releases based on KnowHOW project config",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "5d 5h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "14-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28056",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28056",
                                    "Issue Description": "Good Year|  DIR | Resolution type to be excluded fields are  not fetching from Search dropdown",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "9d 6h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "20-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29305",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29305",
                                    "Issue Description": "Clear Issues and Bugs in sonarqube",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "10d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "31-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27523",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27523",
                                    "Issue Description": "Get API for custom hierarchy based on resource",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "45d 5h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "31-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26279",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26279",
                                    "Issue Description": "Debbie | Create KPI Pickup time",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "26d 5h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "16-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28197",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28197",
                                    "Issue Description": "AA: Lead time Configurations to be multi-select",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "1d 7h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "20-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-30012",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-30012",
                                    "Issue Description": "Backlog | Ordering of sub-tabs",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "1d 4h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "28-Nov-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26447",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26447",
                                    "Issue Description": "Removing Backlog changes for new processor",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "7d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "19-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26205",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26205",
                                    "Issue Description": "3. Debbie | Update Debbie API response to include project, repositories and branch details",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "14d 5h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "16-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27535",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27535",
                                    "Issue Description": "AA | Backlog Readiness KPI configuration enhancement",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "4d 7h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "16-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29714",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29714",
                                    "Issue Description": "UI || Default selection of release on Release dashboard",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "6d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "28-Nov-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26327",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26327",
                                    "Issue Description": "Jira Processor | Group spring batch reader based on jira urls",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "4d ",
                                    "DOR to DOD": "8d ",
                                    "DOD to Live": "44d ",
                                    "DOR Date": "27-Jun-2023",
                                    "DOD Date": "07-Jul-2023",
                                    "Live Date": "06-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-23336",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-23336",
                                    "Issue Description": "Error Handling Framework",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "8d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "06-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-24789",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-24789",
                                    "Issue Description": "Azure  Sprint snapshot Dev Testing ",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "29d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "06-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-23735",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-23735",
                                    "Issue Description": "Create solution for authentication ",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "13d 2h ",
                                    "DOR to DOD": "33d ",
                                    "DOD to Live": "30d 2h ",
                                    "DOR Date": "10-Apr-2023",
                                    "DOD Date": "25-May-2023",
                                    "Live Date": "06-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29311",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29311",
                                    "Issue Description": "DevOps Support 8.0.0",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "0d",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "31-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26284",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26284",
                                    "Issue Description": "Debbie | Create KPI PR Size",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "18d 1h ",
                                    "DOR to DOD": "8d ",
                                    "DOD to Live": "14d 5h ",
                                    "DOR Date": "17-Jul-2023",
                                    "DOD Date": "27-Jul-2023",
                                    "Live Date": "16-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-22767",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-22767",
                                    "Issue Description": "Save Jira data",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "26d 1h ",
                                    "DOR to DOD": "5d ",
                                    "DOD to Live": "68d ",
                                    "DOR Date": "10-Apr-2023",
                                    "DOD Date": "17-Apr-2023",
                                    "Live Date": "19-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29397",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29397",
                                    "Issue Description": "KH | Hide original repo processors from connection and configuration when Debbie is available",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "12d 3h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "28-Nov-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29315",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29315",
                                    "Issue Description": "Value KPI - PI Predictability Enhancement",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "9d 2h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "31-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28864",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28864",
                                    "Issue Description": "Iteration commitment to be full width KPI",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "3h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "20-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-22763",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-22763",
                                    "Issue Description": "DEBBIE | For each provider, analyze the documentation to see the availability of retrieving the desired data and define a complete solution",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "27d ",
                                    "DOR to DOD": "17d ",
                                    "DOD to Live": "67d 7h ",
                                    "DOR Date": "10-Apr-2023",
                                    "DOD Date": "03-May-2023",
                                    "Live Date": "04-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29038",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29038",
                                    "Issue Description": "User should have an ability to define Thresholds at Project level for each KPI in Speed, Quality and Value",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "9d ",
                                    "DOR to DOD": "18d 1h ",
                                    "DOD to Live": "16d 5h ",
                                    "DOR Date": "11-Oct-2023",
                                    "DOD Date": "06-Nov-2023",
                                    "Live Date": "28-Nov-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26324",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26324",
                                    "Issue Description": "Jira processor | Implement custom retry with spring batch reader",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "13d 1h ",
                                    "DOR to DOD": "56d ",
                                    "DOD to Live": "26d ",
                                    "DOR Date": "10-Jul-2023",
                                    "DOD Date": "26-Sep-2023",
                                    "Live Date": "31-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26202",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26202",
                                    "Issue Description": "KH | Create KPI PR Size",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "31d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "31-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29098",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29098",
                                    "Issue Description": "Backlog Dashboard - Sub Tabs",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "10d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "31-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26140",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26140",
                                    "Issue Description": "Release Dashboard data is not getting updated regularly after every processor run similar to iteration data",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "4d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "14-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28439",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28439",
                                    "Issue Description": "DSV: Due Date and Dev due date is showing wrong data for some issues ",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "21d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "28-Nov-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27745",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27745",
                                    "Issue Description": "EJ.COM | DevCompletion KPI calculation should have option to include either due date or custom dev due date",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "7h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "16-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26898",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26898",
                                    "Issue Description": "QE || Debbie - Mean time to merge and no of check-in testing. ",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "3d 1h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "28-Nov-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-25721",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-25721",
                                    "Issue Description": "UI | Release Dashboard - Filter changes",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "3d 5h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "04-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-23784",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-23784",
                                    "Issue Description": "Milestone Board Addition",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "3d 1h ",
                                    "DOR to DOD": "27d 2h ",
                                    "DOD to Live": "52d ",
                                    "DOR Date": "27-Mar-2023",
                                    "DOD Date": "03-May-2023",
                                    "Live Date": "14-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-22331",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-22331",
                                    "Issue Description": "Step- Create account hierarchy",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "104d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "04-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-24632",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-24632",
                                    "Issue Description": "Enhancement on Defect Reopen Rate KPI - Backlog board",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "7d 6h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "16-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-22330",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-22330",
                                    "Issue Description": " Transform Jira data",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "107d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "04-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28437",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28437",
                                    "Issue Description": "DSV: Sub task is closed but story got spilled",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "22d 3h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "28-Nov-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-24512",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-24512",
                                    "Issue Description": "User should be able to select up to 6 BU/Vertical/Account/Portfolio/Project at one time",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "39d 1h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "06-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28438",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28438",
                                    "Issue Description": "DSV: Header part (assignees) should be constant - while scrolling",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "24d 2h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "28-Nov-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26932",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26932",
                                    "Issue Description": "AA - Lead time enhancement to have a full width view and time duration filter",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "2d 1h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "14-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26899",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26899",
                                    "Issue Description": "Debbie | Delete project and project data",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "21d 7h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "16-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-30129",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-30129",
                                    "Issue Description": "Sensitive info in API JSON to list connections",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "4d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "13-Dec-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26934",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26934",
                                    "Issue Description": "Backlog tab - Removing/Updating checks on Backlog KPIs",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "3d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "14-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26815",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26815",
                                    "Issue Description": "Endeavour: In Sprint and Regression automation coverage through Upload data (Option to user)",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "2d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "06-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28431",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28431",
                                    "Issue Description": "DSV: Issue type icon should be shown on line chart, Legends and right component",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "22d 4h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "28-Nov-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28156",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28156",
                                    "Issue Description": "Analyze existing cache and redefine caching strategy",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "25d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "31-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27186",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27186",
                                    "Issue Description": "Show Sprint Names and KPI Values on one project selection",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "7d ",
                                    "DOR to DOD": "10d ",
                                    "DOD to Live": "6d ",
                                    "DOR Date": "26-Jul-2023",
                                    "DOD Date": "08-Aug-2023",
                                    "Live Date": "16-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-23381",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-23381",
                                    "Issue Description": "KPI changes for backlog",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "31d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "06-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27860",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27860",
                                    "Issue Description": "Show higher hierarchy level while selecting filter on Speed, Quality, Value Dashboards",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "2d 4h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "20-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-24592",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-24592",
                                    "Issue Description": "Jira processor dev testing (Kanban)",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "10d 1h ",
                                    "DOR to DOD": "101d ",
                                    "DOD to Live": "26d ",
                                    "DOR Date": "08-May-2023",
                                    "DOD Date": "26-Sep-2023",
                                    "Live Date": "31-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27221",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27221",
                                    "Issue Description": "Debbie | Performance Report for each KPI for one/three/six projects",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "9d 6h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "16-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-22335",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-22335",
                                    "Issue Description": "QE|| Jira processor end to end testing (Scrum)",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "94d ",
                                    "DOR to DOD": "77d 5h ",
                                    "DOD to Live": "14d 3h ",
                                    "DOR Date": "26-Jun-2023",
                                    "DOD Date": "11-Oct-2023",
                                    "Live Date": "31-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27861",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27861",
                                    "Issue Description": "Drag and Drop in Columns (Iteration, Release, Backlog)",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "5d 4h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "20-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-22334",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-22334",
                                    "Issue Description": "Jira processor dev testing (Scrum)",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "59d 5h ",
                                    "DOR to DOD": "101d ",
                                    "DOD to Live": "26d ",
                                    "DOR Date": "08-May-2023",
                                    "DOD Date": "26-Sep-2023",
                                    "Live Date": "31-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29643",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29643",
                                    "Issue Description": "Backlog | New KPI: Cycle time",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "1d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "13-Dec-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28675",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28675",
                                    "Issue Description": "Change Backlog KPIs according to Base Class",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "2d 5h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "28-Nov-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28676",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28676",
                                    "Issue Description": "Change Developer KPIs according to Base Class",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "5d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "28-Nov-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28555",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28555",
                                    "Issue Description": "Enrich |Release filter not  working when release name has more Underscores",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "1d 5h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "20-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28434",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28434",
                                    "Issue Description": "DSV: User should be able to click on Story to see sub-tasks graphical view",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "20d 3h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "28-Nov-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26136",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26136",
                                    "Issue Description": "Release Dashboard | Sub task level bug to included in Fix version issue count",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "14d ",
                                    "DOR to DOD": "36d ",
                                    "DOD to Live": "6d 3h ",
                                    "DOR Date": "10-Jul-2023",
                                    "DOD Date": "29-Aug-2023",
                                    "Live Date": "06-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-22332",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-22332",
                                    "Issue Description": "Step- Create history data of issue",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "99d 4h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "04-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26393",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26393",
                                    "Issue Description": "Backlog Dashboard - Logic change for dataset",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "2d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "06-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26150",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26150",
                                    "Issue Description": "Release Tab | Defects grouped by Testing Phase",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "2d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "05-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27876",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27876",
                                    "Issue Description": "KPI Performance Improvement",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "15d 2h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "31-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-24006",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-24006",
                                    "Issue Description": "Integrate with PSST Tool& analyze profiling ",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "0d",
                                    "DOR to DOD": "8d ",
                                    "DOD to Live": "72d ",
                                    "DOR Date": "27-Mar-2023",
                                    "DOD Date": "06-Apr-2023",
                                    "Live Date": "14-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-22065",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-22065",
                                    "Issue Description": "Backlog Dashboard | Logic of Backlog needs to be changed",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "102d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "04-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28845",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28845",
                                    "Issue Description": "Write Dynamic code for all the Boards",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "1d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "05-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29818",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29818",
                                    "Issue Description": "Backlog | Iteration Readiness - Club statuses based on mappings",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "3d 6h ",
                                    "DOR to DOD": "9d ",
                                    "DOD to Live": "7d ",
                                    "DOR Date": "21-Nov-2023",
                                    "DOD Date": "04-Dec-2023",
                                    "Live Date": "13-Dec-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28608",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28608",
                                    "Issue Description": "Release Tab | Burnup chart to show predicted completion date",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "16d 3h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "28-Nov-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-30118",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-30118",
                                    "Issue Description": "Users should be able to see the threshold value for all KPIs through configuration",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "3d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "13-Dec-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29819",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29819",
                                    "Issue Description": "Backlog | Backlog Health | Ordering of KPIs",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "5d 2h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "28-Nov-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28609",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28609",
                                    "Issue Description": "Backlog | Flow KPI - Lead time Enhancement",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "4d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "13-Dec-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27519",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27519",
                                    "Issue Description": "Debbie | Remove the need of SSH URL Repository enrollment",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "19d 3h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "06-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26142",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26142",
                                    "Issue Description": "Release Dashboard - Release Progress Enhancement KPI Hover on graph ",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "4d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "14-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-20567",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-20567",
                                    "Issue Description": "QE: Iteration dashboard testing with Azure Boards",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "22d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "06-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28440",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28440",
                                    "Issue Description": "DSV: 'In Progress' Issues filter to be added on top",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "21d 4h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "28-Nov-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27631",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27631",
                                    "Issue Description": "Security Issues - 2023 Q3 Penetration Test ",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "36d 3h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "31-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29379",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29379",
                                    "Issue Description": "Backlog Dashboard: Flow Efficiency KPI",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "5d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "28-Nov-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27633",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27633",
                                    "Issue Description": "Zephyr Scale 9.10 setup",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "10d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "06-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27632",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27632",
                                    "Issue Description": "Azure Subscription Setup and SapeCloud Server Migration",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "40d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "31-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26268",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26268",
                                    "Issue Description": "Tech stack for generic Pipeline",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "4d 3h ",
                                    "DOR to DOD": "15d 2h ",
                                    "DOD to Live": "36d ",
                                    "DOR Date": "27-Jun-2023",
                                    "DOD Date": "18-Jul-2023",
                                    "Live Date": "06-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26918",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26918",
                                    "Issue Description": "Backlog KPI - Count by Status",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "2d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "19-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27843",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27843",
                                    "Issue Description": "Maturity Data Fetch From Knowhow For MAP ",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "23d 3h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "05-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27205",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27205",
                                    "Issue Description": "Debbie | KPI argument to support list of project and to return project wise data",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "6d 5h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "16-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27967",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27967",
                                    "Issue Description": "EJ: Scope Churn KPI - ",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "13d ",
                                    "DOR to DOD": "2d ",
                                    "DOD to Live": "4d ",
                                    "DOR Date": "13-Sep-2023",
                                    "DOD Date": "15-Sep-2023",
                                    "Live Date": "20-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29868",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29868",
                                    "Issue Description": "Backlog Readiness KPI - Remove \"<\" or \">\" symbol on Backlog Strength - It should be only <count> Sprint",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "5d 7h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "28-Nov-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29109",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29109",
                                    "Issue Description": "Release Dashboard - Naming changes and regrouping in sub tabs",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "11d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "31-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26910",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26910",
                                    "Issue Description": "AA: Backlog Readiness Efficiency",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "3d 2h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "14-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28817",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28817",
                                    "Issue Description": "QE|| Automation - Data validation on Explore window for all kpis and sprint filters",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "30d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "28-Nov-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26197",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26197",
                                    "Issue Description": "KH | Create KPI Pickup time",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "21d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "06-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26350",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26350",
                                    "Issue Description": "KH | Debbie deployment",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "11d 5h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "06-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26196",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26196",
                                    "Issue Description": "1. KH | Update existing repo KPIs to use data transformation service",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "17d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "16-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27441",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27441",
                                    "Issue Description": "Debbie | Analyze where ssh_url and http_url are used",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "8d 5h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "16-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-23482",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-23482",
                                    "Issue Description": "DEBBIE Setup  | Implement defined solution to lower number of containers and resource consumption",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "14d ",
                                    "DOR to DOD": "9d ",
                                    "DOD to Live": "76d ",
                                    "DOR Date": "10-Apr-2023",
                                    "DOD Date": "21-Apr-2023",
                                    "Live Date": "04-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-25144",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-25144",
                                    "Issue Description": "GS: Unit test coverage enhancement",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "5d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "14-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29349",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29349",
                                    "Issue Description": "DIR, FTPR - Configuration enhancement",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "14d 2h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "28-Nov-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29104",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29104",
                                    "Issue Description": "Backlog Tab | Epic progress",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "11d 5h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "31-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27204",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27204",
                                    "Issue Description": "'KnowHOW | Distinct configuration options for Backlog and Release KPI's",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "9d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "04-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29120",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29120",
                                    "Issue Description": "User should have the option to see the scope churn by Story Points or Issue Count",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "14d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "28-Nov-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-22329",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-22329",
                                    "Issue Description": "Step- create metadata based on Jira template",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "101d 1h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "04-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-25719",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-25719",
                                    "Issue Description": "Github Tool - Code optimization - Processor is taking more time if there are more than 4000 records ",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "10d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "06-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-24345",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-24345",
                                    "Issue Description": "Refactor scripts for Release 7.1.0 ",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "2d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "14-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-22440",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-22440",
                                    "Issue Description": "SBR: Defect Rejection Rate | Improvement to Logic / Mapping",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "94d 2h ",
                                    "DOR to DOD": "26d ",
                                    "DOD to Live": "3d ",
                                    "DOR Date": "27-Jun-2023",
                                    "DOD Date": "02-Aug-2023",
                                    "Live Date": "04-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29119",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29119",
                                    "Issue Description": "Nissan | Sprint Capacity Utilization KPI - Estimation as Original Estimation - If Original Estimate added in Subtasks - it is not considering in main story estimation ",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "10d 2h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "31-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29912",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29912",
                                    "Issue Description": "GS | Units of Estimate vs Actual KPI to be converted to Days similar to Work Remaining",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "9d 2h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "13-Dec-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26129",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26129",
                                    "Issue Description": "Comment section Enhancement",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "1d 7h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "14-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-23656",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-23656",
                                    "Issue Description": "Db Indexing analysis",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "53d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "19-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-23813",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-23813",
                                    "Issue Description": "QE||Automation - Milestone dashboard",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "9d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "06-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27330",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27330",
                                    "Issue Description": "Auth & Auth Documentation",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "37d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "31-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29114",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29114",
                                    "Issue Description": "X-Axis Date format and alignment correction for DORA KPIs",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "12d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "31-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26121",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26121",
                                    "Issue Description": "Story level DIR available in the overlay ",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "7d 5h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "16-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-22324",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-22324",
                                    "Issue Description": "Fetch Issues based on JQL",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "95d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "04-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26123",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26123",
                                    "Issue Description": "TCP | Backlog KPI: Defect by type (Sprint bug / ELO / Regression) ",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "3d 1h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "06-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26522",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26522",
                                    "Issue Description": "Floating icon - for help",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "1d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "19-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-24343",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-24343",
                                    "Issue Description": "QE|| Jira processor end to end testing (Kanban)",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "14d 3h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "31-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-22659",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-22659",
                                    "Issue Description": "DEBBIE | Create API for triggering the scan and fetching scan progress",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "77d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "04-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-25928",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-25928",
                                    "Issue Description": "As a Project admin, when a Jira processor fails for a notification should be sent to admin ",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "33d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "31-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-30044",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-30044",
                                    "Issue Description": "SBR: Defect count by RCA - Include Inclusion instead of Exclusion",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "3d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "13-Dec-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29847",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29847",
                                    "Issue Description": "Release & Backlog Dashboard | Epic by Progress",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "1d ",
                                    "DOR to DOD": "8d ",
                                    "DOD to Live": "8d ",
                                    "DOR Date": "21-Nov-2023",
                                    "DOD Date": "01-Dec-2023",
                                    "Live Date": "13-Dec-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26975",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26975",
                                    "Issue Description": "Identify and save original issue type of Jira in DB",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "6d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "16-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29848",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29848",
                                    "Issue Description": "Release Dashboard | Show legends by default on Defects by Testing Phase KPI",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "1d ",
                                    "DOR to DOD": "7d ",
                                    "DOD to Live": "9d 1h ",
                                    "DOR Date": "21-Nov-2023",
                                    "DOD Date": "30-Nov-2023",
                                    "Live Date": "13-Dec-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26336",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26336",
                                    "Issue Description": "JSON Finalization (Screen 2)",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "12d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "06-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29966",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29966",
                                    "Issue Description": "Backlog and Dora KPI's to show data points on nodes permanently like S/Q/V KPI's",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "6d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "13-Dec-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27549",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27549",
                                    "Issue Description": "Release Tab | Release Burnup changes",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "2d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "20-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29849",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29849",
                                    "Issue Description": "Release Dashboard | Release Burnup Enhancements",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "6d 2h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "13-Dec-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-22658",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-22658",
                                    "Issue Description": "2. KH | Integrate Debbie project enrollment with KnowHOW",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "21d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "06-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28198",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28198",
                                    "Issue Description": "Release Dashboard Defect KPIs - Should show open defects by default",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "3d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "20-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26331",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26331",
                                    "Issue Description": "QE || Testing - Repo tools and kpi",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "2h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "28-Nov-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28870",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28870",
                                    "Issue Description": "DSV - Side panel should change on click of issue",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "23d 1h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "28-Nov-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29689",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29689",
                                    "Issue Description": " FTPR - Configuration enhancement on Iteration board",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "8d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "13-Dec-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29568",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29568",
                                    "Issue Description": "Backlog | Epic Progress - Radio buttons to identify Open Epics/ All Epics",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "11d 5h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "28-Nov-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28237",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28237",
                                    "Issue Description": "Graph KPI values - Show KPI values on one project selection",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "5d 4h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "20-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27664",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27664",
                                    "Issue Description": "Debbie | Performance analysis for Debbie APIs in order to receive a high load of requests",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "13d 4h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "06-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26333",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26333",
                                    "Issue Description": "Tag user to meaningful when assignee filter is off for Jira & Azure & Non Jira Processors",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "16d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "06-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26332",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26332",
                                    "Issue Description": "UI | Implement Daily Standup View (Screen 2)",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "7d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "20-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-25365",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-25365",
                                    "Issue Description": "Use lombok library throughout project for logging",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "7d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "06-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-24034",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-24034",
                                    "Issue Description": "Create backlog data in existing jira processor",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "84d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "04-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29324",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29324",
                                    "Issue Description": "Project level KPI Show/Hide",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "8d 4h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "28-Nov-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29842",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29842",
                                    "Issue Description": "Jira Processor showing success notification despite incomplete data fetch.",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "5d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "28-Nov-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-28092",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-28092",
                                    "Issue Description": "Integrate Mongock",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "18d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "28-Nov-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29461",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29461",
                                    "Issue Description": "GS | Definition Change on UI for DRE",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "17d 3h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "28-Nov-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26192",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26192",
                                    "Issue Description": "Backlog KPI - Count by issue type",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "14d ",
                                    "DOR to DOD": "6d ",
                                    "DOD to Live": "2d ",
                                    "DOR Date": "10-Jul-2023",
                                    "DOD Date": "18-Jul-2023",
                                    "Live Date": "19-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29580",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29580",
                                    "Issue Description": "Jira Processor: Notifications enhancement",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "8d 3h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "28-Nov-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27679",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27679",
                                    "Issue Description": "Maturity View - Visualization enhancement to show data in a tabular form",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "7d 3h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "05-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26900",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26900",
                                    "Issue Description": "KH | Consume debbie api for project and project data deletion ",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "14d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "16-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-25931",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-25931",
                                    "Issue Description": "A switch to turn of notifications of failures (User)",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "11d 1h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "06-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-23479",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-23479",
                                    "Issue Description": "DEBBIE Setup | Analyse and define complete solution to combine multiple containers and lower resource consumption under 4GB",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "4d ",
                                    "DOR to DOD": "11d ",
                                    "DOD to Live": "84d ",
                                    "DOR Date": "27-Mar-2023",
                                    "DOD Date": "11-Apr-2023",
                                    "Live Date": "04-Aug-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26109",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26109",
                                    "Issue Description": "GA for event Tracking ",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "3h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "19-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26507",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26507",
                                    "Issue Description": "GS : \"Debugging KH master Login issue - Authentication Failed error page\".",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "3d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "06-Jul-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-25935",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-25935",
                                    "Issue Description": "Create generic exception framework ",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "22d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "06-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27550",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27550",
                                    "Issue Description": "Release Tab | Release progress changes",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "7d 3h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "05-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27275",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27275",
                                    "Issue Description": "DORA | Lead time for change",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "2d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "05-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26340",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26340",
                                    "Issue Description": "BE | Implement Daily Standup View (Screen 2)",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "7d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "20-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27274",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27274",
                                    "Issue Description": "DORA | Mean time to Recover",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "11d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "28-Nov-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-25132",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-25132",
                                    "Issue Description": "Backend|CRUD for authorization (project permissions, hierarchy)",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "33d 7h ",
                                    "DOR to DOD": "56d ",
                                    "DOD to Live": "25d 4h ",
                                    "DOR Date": "10-Jul-2023",
                                    "DOD Date": "26-Sep-2023",
                                    "Live Date": "31-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26344",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26344",
                                    "Issue Description": "QE || DSV |Testing - Writing testcases and execution",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "15d 3h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "28-Nov-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29338",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29338",
                                    "Issue Description": "Scope Churn Enhancement to make it similar to Iteration Commitment",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "8d 4h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "31-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27795",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27795",
                                    "Issue Description": "SBR | Enhance  Sprint Velocity or issue count calculation to consider story once across multiple sprints for Iteration dashboard kpis",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "8d 4h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "20-Sep-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-27553",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-27553",
                                    "Issue Description": "Release Tab | Epic progress",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "11d 4h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "31-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26346",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26346",
                                    "Issue Description": "QE || Testing - User mapping for jira/non-jira tools",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "17d 2h ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "31-Oct-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-29215",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-29215",
                                    "Issue Description": "Define maturity ranges for DORA metrics",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "14d ",
                                    "DOR to DOD": "28d 3h ",
                                    "DOD to Live": "3d ",
                                    "DOR Date": "31-Oct-2023",
                                    "DOD Date": "08-Dec-2023",
                                    "Live Date": "13-Dec-2023"
                                },
                                {
                                    "spill": false,
                                    "preClosed": false,
                                    "Issue Id": "DTS-26862",
                                    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-26862",
                                    "Issue Description": "new installation - First user registration will become SUPERADMIN",
                                    "Issue Type": "Story",
                                    "Intake to DOR": "NA",
                                    "DOR to DOD": "NA",
                                    "DOD to Live": "2d ",
                                    "DOR Date": "NA",
                                    "DOD Date": "NA",
                                    "Live Date": "14-Jul-2023"
                                }
                            ]
                        }
                    ]
                },
                {
                    "filter1": "Overall",
                    "filter2": "Overall",
                    "data": [
                        {
                            "label": "Intake - DOR",
                            "value": 27,
                            "value1": 105,
                            "unit": "d",
                            "unit1": "issues",
                            "modalValues": []
                        },
                        {
                            "label": "DOR - DOD",
                            "value": 22,
                            "value1": 64,
                            "unit": "d",
                            "unit1": "issues",
                            "modalValues": []
                        },
                        {
                            "label": "DOD - Live",
                            "value": 19,
                            "value1": 188,
                            "unit": "d",
                            "unit1": "issues",
                            "modalValues": []
                        }
                    ]
                }
            ]
        }
        const fakeJiraData = [{
            "kpiId": "kpi171",
            "kpiName": "Cycle Time",
            "unit": "%",
            "maxValue": "200",
            "chartType": "",
            "trendValueList": {...trendValueList}
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
        const spy = spyOn(httpService, 'postKpi').and.returnValue(of(fakeJiraData));
        component.getkpi171Data('kpi171', trendValueList)
        expect(spy).toHaveBeenCalled();
    })
});

