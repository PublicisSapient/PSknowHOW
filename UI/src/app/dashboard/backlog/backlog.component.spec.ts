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

import { TestBed, ComponentFixture } from '@angular/core/testing';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { APP_CONFIG, AppConfig } from '../../services/app.config';
import { HttpClientModule } from '@angular/common/http';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpService } from '../../services/http.service';
import { BacklogComponent } from './backlog.component';
import { SharedService } from '../../services/shared.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ExcelService } from 'src/app/services/excel.service';
import { DatePipe } from '@angular/common';
import { HelperService } from 'src/app/services/helper.service';

describe('BacklogComponent', () => {
    let component: BacklogComponent;
    let fixture: ComponentFixture<BacklogComponent>;
    let service: SharedService;
    let httpService: HttpService;
    let httpMock;
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
    const user_name = 'xyz';
    beforeEach(() => {
        service = new SharedService();
        TestBed.configureTestingModule({
            declarations: [
                BacklogComponent
            ],
            imports: [
                FormsModule,
                HttpClientModule,
                HttpClientTestingModule,
                RouterTestingModule,
            ],
            providers: [
                HelperService,
                HttpService,
                {provide: SharedService, useValue: service},
                { provide: APP_CONFIG, useValue: AppConfig }
                , ExcelService, DatePipe
            ],
            schemas: [CUSTOM_ELEMENTS_SCHEMA]

        }).compileComponents();
        service = TestBed.inject(SharedService);
        httpService = TestBed.inject(HttpService);
        httpMock = TestBed.inject(HttpTestingController);
        fixture = TestBed.createComponent(BacklogComponent);
        component = fixture.componentInstance;
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
            selectedTab: 'Backlog'
        };
        component.selectedtype = 'Scrum';
        const spygroupJiraKpi = spyOn(component, 'groupJiraKpi');
        component.receiveSharedData(filterData);
        expect(spygroupJiraKpi).toHaveBeenCalled();
        filterData.filterData = [];
        component.receiveSharedData(filterData);
        expect(component.noTabAccess).toBeTrue();
    });

});

