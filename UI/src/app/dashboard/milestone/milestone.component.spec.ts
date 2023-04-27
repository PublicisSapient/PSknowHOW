import { ComponentFixture, TestBed } from '@angular/core/testing';

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
    selectedTab: 'Milestone'
};
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
  let service: SharedService;
  let helperService : HelperService
  let httpService;
  let excelService;
  beforeEach(async () => {
    service = new SharedService();
    await TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        RouterTestingModule.withRoutes(routes),
    ],
    declarations: [MilestoneComponent],
    providers: [
      HelperService,
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
        selectedTab: 'Milestone'
    };
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
  const today = new Date().toISOString().split('T')[0];
  const endDate = new Date('2023-02-27T13:36:00.0000000').toISOString().split('T')[0];
  const spy = spyOn(component, 'calcBusinessDays').and.returnValue(of(0))
  component.calcBusinessDays(today, endDate);
  expect(spy).toHaveBeenCalled();
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
  component.masterData = {
      kpiList: [{
          kpiId: 'kpi17',
          kanban: false,
          kpiSource: 'Jira',
          kpiCategory: 'Milestone',
          groupId: 1
      }]
  };
  const spy = spyOn(helperService, 'groupKpiFromMaster').and.returnValue({ kpiList: kpiListJira });
  const postJiraSpy = spyOn(component, 'postJiraKpi');
  component.groupJiraKpi(['kpi17']);
  expect(postJiraSpy).toHaveBeenCalled();
});

});
