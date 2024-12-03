import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';

import { MilestoneComponent } from './milestone.component';
import { HttpService } from 'src/app/services/http.service';
import { SharedService } from 'src/app/services/shared.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { HelperService } from 'src/app/services/helper.service';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { APP_CONFIG, AppConfig } from '../../services/app.config';
import { Routes } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { ExcelService } from 'src/app/services/excel.service';
import { DatePipe } from '@angular/common';
import { of } from 'rxjs';
import { ExportExcelComponent } from 'src/app/component/export-excel/export-excel.component';
import { MessageService } from 'primeng/api';



describe('MilestoneComponent', () => {
    let component: MilestoneComponent;
    let fixture: ComponentFixture<MilestoneComponent>;
    const routes: Routes = [
        { path: 'dashboard', component: MilestoneComponent },
        { path: 'authentication/login', component: MilestoneComponent }
    ];
    const filterData = {
        masterData: {
            kpiList: []
        },
        filterData: [
            {
                nodeId: '38998_DEMO_SONAR_63284960fdd20276d60e4df5',
                nodeName: 'Tools|PI_10|ITR_6|07 Sep_DEMO_SONAR',
                releaseStartDate: '2022-09-07T08:40:00.0000000',
                releaseEndDate: '2022-09-27T08:40:00.0000000',
                path: [
                    'DEMO_SONAR_63284960fdd20276d60e4df5###asc_subaccount###mn_account###asfd_business###TESTS_corporate'
                ],
                labelName: 'release',
                parentId: [
                    'DEMO_SONAR_63284960fdd20276d60e4df5'
                ],
                releaseState: 'ACTIVE',
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
                release: [
                    '38998_DEMO_SONAR_63284960fdd20276d60e4df5'
                ],
                sqd: []
            },
            level: 6
        },
        selectedTab: 'Release'
    };

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
    const fakeJiraPayload = require('../../../test/resource/fakeJiraPayload.json');
    const fakeMilestoneKpiResponse = require('../../../test/resource/milestoneKpiResponse.json');
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
    const userConfigData = require('../../../test/resource/fakeGlobalConfigData.json');
    const fakeJiraGroupId1 = require('../../../test/resource/fakeJiraGroupId1.json');
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
                kpiSubCategory : 'Speed',
                kpiSource: 'Jira',
                maxValue: '300',
                kanban: true,
                groupId: 4,
                kpiWidth : 100,
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
        },
        {
            kpiId: 'kpi75',
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
                kpiSubCategory : 'Value',
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
    let service: SharedService;
    let helperService: HelperService
    let httpService;
    let excelService;
    let exportExcelComponent;
    beforeEach(async () => {
        service = new SharedService();
        await TestBed.configureTestingModule({
            imports: [
                HttpClientTestingModule,
                RouterTestingModule.withRoutes(routes),
            ],
            declarations: [MilestoneComponent,ExportExcelComponent],
            providers: [
                HelperService,MessageService,
                { provide: APP_CONFIG, useValue: AppConfig },
                HttpService,
                { provide: SharedService, useValue: service }
                , ExcelService, DatePipe],
            schemas: [CUSTOM_ELEMENTS_SCHEMA]

        })
            .compileComponents();
        service = TestBed.inject(SharedService);
        httpService = TestBed.inject(HttpService);
        helperService = TestBed.inject(HelperService);
        excelService = TestBed.inject(ExcelService);
        exportExcelComponent = TestBed.createComponent(ExportExcelComponent).componentInstance;

        fixture = TestBed.createComponent(MilestoneComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
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
                    releaseStartDate: '2022-09-07T08:40:00.0000000',
                    releaseEndDate: '2022-09-27T08:40:00.0000000',
                    path: [
                        'DEMO_SONAR_63284960fdd20276d60e4df5###asc_subaccount###mn_account###asfd_business###TESTS_corporate'
                    ],
                    labelName: 'release',
                    parentId: [
                        'DEMO_SONAR_63284960fdd20276d60e4df5'
                    ],
                    releaseState: 'ACTIVE',
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
                    release: [
                        '38998_DEMO_SONAR_63284960fdd20276d60e4df5'
                    ],
                    sqd: []
                },
                level: 6
            },
            selectedTab: 'Release'
        };
        component.globalConfig = userConfigData;
        const spy = spyOn(component, 'receiveSharedData');
        service.passDataToDashboard.emit(sharedObject);
        fixture.detectChanges();
        expect(spy).toHaveBeenCalledWith(sharedObject);

    });

    it('should process config data on getting globalDashConfigData', () => {
        const spy = spyOn(component, 'processKpiConfigData');
        service.globalDashConfigData.emit(userConfigData['data']);
        fixture.detectChanges();
        expect(spy).toHaveBeenCalled();
    });

    it('should calculate business days', () => {
        const today = new Date("2023-05-01T00:00:00").toISOString().split('T')[0];
        const endDate = new Date('2023-06-01T00:00:00').toISOString().split('T')[0];
        const days = component.calcBusinessDays(today, endDate);
        expect(days).toBe(24);
    });

    it('should process kpi config Data', () => {
        component.configGlobalData = configGlobalData;
        component.navigationTabs = [
            {'label':'Speed', 'count': 0,kpis : [],width : 'half', fullWidthKpis : []},
            {'label':'Quality', 'count': 0,kpis : [],width :'half'},
            {'label':'Value', 'count': 0,kpis : [],width :'full'},
          ];
        component.processKpiConfigData();
        expect(component.noKpis).toBeFalse();
        component.configGlobalData[0]['isEnabled'] = false;
        component.configGlobalData[0]['shown'] = false;
        component.processKpiConfigData();
        expect(Object.keys(component.kpiConfigData).length).toBe(configGlobalData.length);
    });

    it('check whether scrum', (done) => {
        const type = 'Scrum';
        component.getSelectedType(type);
        component.selectedtype = 'Scrum';
        fixture.detectChanges();
        expect(component.selectedtype).toBe(type);
        done();
    });

    it('should call groupKpi methods on selecting filter', () => {
        component.selectedtype = 'Scrum';
        const spygroupJiraKpi = spyOn(component, 'groupJiraKpi');
        const spycalcBusinessDays = spyOn(component, 'calcBusinessDays');
        spyOn(service, 'getDashConfigData').and.returnValue(userConfigData['data']);
        component.receiveSharedData(filterData);
        expect(spygroupJiraKpi).toHaveBeenCalled();
    });

    it('should make post call when kpi available for Jira for Scrum', () => {
        const kpiListJira = [{
            id: '6332dd4b82451128f9939a29',
            kpiId: 'kpi17',
            kpiName: 'Unit Test Coverage'
        }];
        component.updatedConfigGlobalData = [{
            kpiId: 'kpi17',
            kpiName: 'Unit Test Coverage',
            isEnabled: true,
            order: 23,
            kpiDetail: {
                kanban: false,
                kpiSource: 'Jira',
                kpiCategory: 'Release',
                groupId: 1
            },
            shown: true
        }]
        const spy = spyOn(helperService, 'groupKpiFromMaster').and.returnValue({ kpiList: kpiListJira });
        const postJiraSpy = spyOn(component, 'postJiraKpi');
        component.groupJiraKpi(['kpi17']);
        expect(postJiraSpy).toHaveBeenCalled();
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

    it('should evalvate the aggregated expression', () => {
        let aggregatedArr = [
            {
                "label": "First Time Pass Stories",
                "value": "8.00",
                "value1": null,
                "modalValues": null
            },
            {
                "label": "Total Stories",
                "value": "9.00",
                "modalValues": [],
                "value1": null
            },
            {
                "label": "First Time Pass Rate %",
                "value": 88.89,
                "expressions": [
                    "First Time Pass Stories",
                    "Total Stories",
                    "percentage"
                ],
                "value1": null,
                "modalValues": null
            }
        ];
        component.evalvateExpression(aggregatedArr[2], aggregatedArr, []);
        expect(aggregatedArr[2].value).toEqual(88.89);
    })

    it('should get chart type', () => {
        component.updatedConfigGlobalData = [
            {
                kpiId: 'kpi125',
                kpiDetail: {
                    chartType: 'GroupBarChart'
                }
            }
        ];
        expect(component.getKpiChartType('kpi125')).toEqual('GroupBarChart');
    });

    it('should apply aggregation for groupBarchart', () => {
        const data = [
            {
                filter1: "Defect",
                value: [{
                    "data": "0",
                    "value": 10,
                    "hoverValue": {
                        "Defect": 5,
                    },
                    "subFilter": "Issues planned to be closed",
                    "date": "2023-02-22",
                    "kpiGroup": "Defect",
                    "groupBy": "date",
                    "sprojectName": "41411_AGHORI"
                }]
            },
            {
                filter1: "Change request",
                value: [{
                    "data": "0",
                    "value": 11,
                    "hoverValue": {
                        "Change request": 5
                    },
                    "subFilter": "Issues planned to be closed",
                    "date": "2023-02-22",
                    "kpiGroup": "Defect",
                    "groupBy": "date",
                    "sprojectName": "41411_AGHORI"
                }
                ]
            }
        ];
        const result = component.applyAggregationForChart(data);
        expect(result[0]?.value[0].value).toEqual(21);
    });

    it('should handle selected option', () => {
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
        component.filterApplyData = {
            ids : ['fakeRelease']
        }
        component.handleSelectedOption(event, kpi)
        component.kpiSelectedFilterObj['kpi123'] = {};
        component.kpiSelectedFilterObj['kpi123'] = event
        spyOn(component, 'getChartData');
        service.setKpiSubFilterObj(component.kpiSelectedFilterObj)
        expect(Object.keys(component.kpiSelectedFilterObj['kpi123']).length).toEqual(Object.keys(event).length);
    });

    it('postJiraKpi should call httpServicepost', fakeAsync(() => {
        const jiraKpiData = {
            kpi14: {
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

    it('postJiraKpi should call httpServicepost', fakeAsync(() => {
        const jiraKpiData = {
            kpi14: {
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

    it('should process config data on getting globalDashConfigData', () => {
        // spyOn(service,'globalDashConfigData').and.returnValue(userConfigData['data']);
        const spy = spyOn(component, 'processKpiConfigData');
        service.globalDashConfigData.emit(userConfigData['data']);
        fixture.detectChanges();
        expect(component.configGlobalData.length).toEqual(1);
        expect(spy).toHaveBeenCalled();
    });

    it('should perform the aggregation logic', () => {
        const data = component.applyAggregationLogic(arrToBeAggregated);
        // const spy = spyOn(component, 'applyAggregationLogic');
        fixture.detectChanges();
        expect(data).toEqual(aggregatedData);
    });

    it('should handle selected option', () => {
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
        component.filterApplyData = {
            ids : ['fakeRelease']
        }
        component.handleSelectedOption(event, kpi)
        component.kpiSelectedFilterObj['kpi123'] = {};
        component.kpiSelectedFilterObj['kpi123'] = event
        spyOn(component, 'getChartData');
        service.setKpiSubFilterObj(component.kpiSelectedFilterObj)
        expect(Object.keys(component.kpiSelectedFilterObj['kpi123']).length).toEqual(Object.keys(event).length);
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
                            "modalValues": [
                                {
                                    "Issue Id": "DTS-22685",
                                    "Issue URL": "http://testabc.com/jira/browse/DTS-22685",
                                    "Issue Description": "Iteration KPI | Popup window is not wide enough to read details  ",
                                    "Issue Status": "Open",
                                    "Due Date": "-"
                                }
                            ]
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
                    "value": 21,
                },
            ]
        }
        const combo = [{
            filter1: 'Overall',
            filter2: 'Overall',
        }]

        spyOn(helperService, 'createCombinations').and.returnValue(combo);
        component.getChartData('kpi124', 0)
        expect(component.kpiChartData['kpi124'][0].data.length).toEqual(res.data.length);
    })

    it('should getchartdata for kpi when trendValueList is an object with single filter', () => {
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
                            "modalValues": [
                                {
                                    "Issue Id": "DTS-22685",
                                    "Issue URL": "http://testabc.com/jira/browse/DTS-22685",
                                    "Issue Description": "Iteration KPI | Popup window is not wide enough to read details  ",
                                }
                            ]
                        }]
                    }
                ]

            }
        }];
        component.kpiSelectedFilterObj['kpi124'] = {
            'filter1': ['Overall']
        }
        const res = {
            "filter1": "Overall",
            "data": [
                {
                    "label": "Issue without estimates",
                    "value": 21,
                    "value1": 51,
                    "unit": "",
                    "modalValues": []
                },
            ]
        }
        const combo = [{
            filter1: 'Overall',
        }]

        spyOn(helperService, 'createCombinations').and.returnValue(combo);
        component.getChartData('kpi124', 0)
        expect(component.kpiChartData['kpi124'][0].data.length).toEqual(res.data.length);
    })

    it('should getchartdata for kpi when trendValueList is an object and KPI selected filter is blank', () => {
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
                            "modalValues": [
                                {
                                    "Issue Id": "DTS-22685",
                                    "Issue URL": "http://testabc.com/jira/browse/DTS-22685",
                                    "Issue Description": "Iteration KPI | Popup window is not wide enough to read details  ",
                                }
                            ]
                        }]
                    }
                ]

            }
        }];
        component.kpiSelectedFilterObj['kpi124'] = {}

        const combo = [{
            filter1: 'Overall',
        }]

        spyOn(helperService, 'createCombinations').and.returnValue(combo);
        component.getChartData('kpi124', 0)
        expect(component.kpiChartData['kpi124'][0].data.length).toBeGreaterThan(0)
    })

    it('should get chartdata for kpi when trendValueList is an object but there is no data', () => {
        component.allKpiArray = [{
            kpiId: 'kpi124',
            trendValueList: {
                value: []
            }
        }];
        component.kpiSelectedFilterObj['kpi124'] = {}
        const combo = [{ filter1: 'Overall' }]

        spyOn(helperService, 'createCombinations').and.returnValue(combo);
        component.getChartData('kpi124', 0)
        expect(component.kpiChartData['kpi124'].length).toBeGreaterThan(0)
    })

    it('should get chartdata for kpi when trendValueList is an Array of filters', () => {
        component.allKpiArray = [{
            kpiId: 'kpi124',
            trendValueList: [
                { filter1: 'hold', value: [{ count: 1 }] },
                { filter1: 'hold', value: [{ count: 1 }] },
                { filter1: 'in progress', value: [{ count: 2 }] },
                { filter1: 'in progress', value: [{ count: 2 }] }
            ]
        }];
        component.kpiSelectedFilterObj['kpi124'] = {
            filter1: ['hold', 'in progress']
        }

        const spyObj = spyOn(component, 'applyAggregationLogic');
        spyOn(component, 'getKpiChartType');
        component.getChartData('kpi124', 0)
        expect(spyObj).toHaveBeenCalled();
    })

    it('should get chartdata for kpi when trendValueList is an Array without filter ', () => {
        component.allKpiArray = [{
            kpiId: 'kpi124',
            trendValueList: [
                {
                    "value": [
                        {
                            "data": "2",
                            "value": [
                                {
                                    "value": 1,
                                    "drillDown": [
                                        {
                                            "value": 1,
                                            "subFilter": "Open",
                                            "size": 5
                                        }
                                    ],
                                    "subFilter": "To Do",
                                    "size": 5
                                },
                            ],
                            "kpiGroup": "KnowHOW | Developer Dashboard to show KPIs from Repos",
                            "size": "5.0"
                        },
                    ]
                }
            ]
        }];
        component.kpiSelectedFilterObj['kpi124'] = {
            filter1: ['hold', 'in progress']
        }

        // const spyObj = spyOn(component, 'applyAggregationLogic');
        spyOn(component, 'getKpiChartType');
        component.getChartData('kpi124', 0)
        expect(component.kpiChartData['kpi124'].length).toBeGreaterThan(0)
    })

    it("should create kpi wise list", () => {
        component.filterApplyData = {ids : ['fakeRelease']}
        spyOn(component,'getChartData');
        spyOn(helperService,'setFilterValueIfAlreadyHaveBackup');
        const fakeKPi = helperService.createKpiWiseId(fakeMilestoneKpiResponse.response);
        component.createAllKpiArray(fakeKPi)
        expect(component.allKpiArray.length).toBeGreaterThan(0);
    })

    it("should create kpi array when trendvalueList is object", () => {
        component.filterApplyData = {ids : ['fakeRelease']}
        spyOn(component,'getChartData');
        spyOn(helperService,'setFilterValueIfAlreadyHaveBackup');
        let kpi = [{
            kpiId: "kpi141",
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
                            "modalValues": [
                                {
                                    "Issue Id": "DTS-22685",
                                    "Issue URL": "http://testabc.com/jira/browse/DTS-22685",
                                    "Issue Description": "Iteration KPI | Popup window is not wide enough to read details  ",
                                }
                            ]
                        }]
                    }
                ]

            },
            filters: ['f1', "f2"]
        },]
        const fakeKPi = helperService.createKpiWiseId(kpi);
        component.createAllKpiArray(fakeKPi)
        expect(component.allKpiArray.length).toBeGreaterThan(0);
    })

    it('should get kpi comments count', fakeAsync(() => {
        component.filterData = filterData.filterData;
        component.filterApplyData = {
            'ids': ["38998_DEMO_SONAR_63284960fdd20276d60e4df5"],
            'selectedMap': {
                'release': ["38998_DEMO_SONAR_63284960fdd20276d60e4df5"]
            },
            'level': 6
        };
        const response = {
            "message": "Found Comments Count",
            "success": true,
            "data": {
                "kpi118": 1
            }
        };

        component.kpiCommentsCountObj = {
            'kpi118': 0
        };
        component.updatedConfigGlobalData = [
            {
                kpiId: 'kpi118',
                kpiName: 'Deployment Frequency',
                isEnabled: true,
                order: 23,
                kpiDetail: {

                },
                shown: true
            }
        ];
        spyOn(helperService, 'getKpiCommentsCount').and.returnValue(Promise.resolve({}))
        component.getKpiCommentsCount();
        tick();
        expect(component.kpiCommentsCountObj).toBeDefined();
    }));

    it('should work download excel functionality', () => {
        spyOn(component.exportExcelComponent, 'downloadExcel')
        component.downloadExcel('kpi122', 'name', true, true);
        expect(exportExcelComponent).toBeDefined();
    })

    it('should set the colorObj', () => {
        component.kpiChartData = {
            kpi121: {
                kpiId: 'kpi123'
            }
        }
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

    it('should get chartdata for kpi when trendValueList is an Array of two filters for card', () => {
        component.allKpiArray = [{
            kpiId: 'kpi124',
            trendValueList: [
                { filter1: 'hold', filter2: 'f2', value: [{ count: 1 }] },
                { filter1: 'f2', filter2: 'f2', value: [{ count: 2 }] },
                { filter1: 'in progress', filter2: 'f2', value: [{ count: 2 }] },
            ]
        }];
        component.kpiSelectedFilterObj['kpi124'] = {
            filter1: ['hold', 'in progress'],
            filter2: ['f2']
        }
        const spyObj = spyOn(component, 'applyAggregationLogic');
        spyOn(component, 'getKpiChartType').and.returnValue('abc')
        component.getChartData('kpi124', 0)
        expect(spyObj).toHaveBeenCalled();
    })

    it('should get chartdata for kpi when trendValueList is an Array of two filters for chart', () => {
        component.allKpiArray = [{
            kpiId: 'kpi124',
            trendValueList: [
                { filter1: 'hold', filter2: 'f2', value: [{ count: 1 }] },
                { filter1: 'f2', filter2: 'f2', value: [{ count: 2 }] },
                { filter1: 'in progress', filter2: 'f2', value: [{ count: 2 }] },
            ]
        }];
        component.kpiSelectedFilterObj['kpi124'] = {
            filter1: ['hold', 'in progress'],
            filter2: ['f2']
        }

        const spyObj = spyOn(component, 'applyAggregationForChart');
        spyOn(component, 'getKpiChartType').and.returnValue('GroupBarChart')
        component.getChartData('kpi124', 0)
        expect(spyObj).toHaveBeenCalled();
    })

    it('should get chartdata for kpi when trendValueList is an Array of two filters without aggregration', () => {
        component.allKpiArray = [{
            kpiId: 'kpi124',
            trendValueList: [
                { filter1: 'hold', filter2: 'f2', value: [{ count: 1 }] },
            ]
        }];
        component.kpiSelectedFilterObj['kpi124'] = {
            filter1: ['hold', 'in progress'],
            filter2: ['f2']
        }
        spyOn(component, 'getKpiChartType').and.returnValue('abc')
        component.getChartData('kpi124', 0)
    })

    it('should apply aggregation for chart when value is object', () => {
        const data = [
            {
                filter1: "Defect",
                value: [{
                    "data": "0",
                    "value": { data: 12 },
                    "hoverValue": {
                        "Defect": 5,
                    },
                }]
            },
            {
                filter1: "Change request",
                value: [{
                    "data": "0",
                    "value": { data: 1 },
                    "hoverValue": {
                        "Change request": 5
                    },
                }
                ]
            }
        ];
        const result = component.applyAggregationForChart(data);
        expect(result[0]?.value[0].value.data).toEqual(13);
    });

    it('should apply aggregation for chart when value is array', () => {
        const data = [
            {
                filter1: "Defect",
                value: [{
                    "data": "0",
                    "value": [{ data: 12 }],
                    "hoverValue": {
                        "Defect": 5,
                    },
                }]
            },
            {
                filter1: "Change request",
                value: [{
                    "data": "0",
                    "value": [{ data: 12 }],
                    "hoverValue": {
                        "Change request": 5
                    },
                }
                ]
            }
        ];
        const result = component.applyAggregationForChart(data);
        expect(result).toBeDefined();
    });

    it('should reload kpis', () => {
        const data = {
            kpiDetail: {
                kpiId: 'kpi123',
                groupId: 9
            }
        }
        const rValue = {
            kpiList: ['kpi123']
        }
        spyOn(helperService, 'groupKpiFromMaster').and.returnValue(rValue);
        const spyObj = spyOn(component, 'postJiraKpi');
        component.reloadKPI(data);
        expect(spyObj).toHaveBeenCalled();
    })

    it('should return true if data is present in the first format', () => {
        const result = component.checkIfDataPresent([{ data: 5 }]);
        expect(result).toBeTrue();
      });

      it('should return true if data is present in the second format', () => {
       const result = component.checkIfDataPresent([{ value: [{ data: 10 }] }]);
        expect(result).toBeTrue();
      });
    
      it('should return false if data is present but not a valid number in the second format', () => {
        const result = component.checkIfDataPresent([{ value: [{ data: 'xyz' }] }]);
        expect(result).toBeFalse();
      });

      it("should createapiarry for radiobutton",()=>{
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
                    kpiFilter : 'radiobutton'
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
        component.filterApplyData = {ids : ['release1']}
        spyOn(component,'ifKpiExist').and.returnValue(-1)
        component.createAllKpiArray(data);
        expect(component.kpiSelectedFilterObj).toBeDefined();
      })

      it("should createapiarry for dropdown",()=>{
        component.filterApplyData = {ids : ['release1']}
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
                    kpiFilter : 'dropdown'
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
        component.createAllKpiArray(data);
        expect(component.kpiSelectedFilterObj).toBeDefined();
      })

      it("should createapiarry for multi dropdown",()=>{
        component.filterApplyData = {ids : ['release1']}
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
                filters : {
                    filter1 : {
                        options : ['story']
                    }
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
                    kpiFilter : 'multiDropdown'
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
        component.createAllKpiArray(data);
        expect(component.kpiSelectedFilterObj).toBeDefined();
      })

});



