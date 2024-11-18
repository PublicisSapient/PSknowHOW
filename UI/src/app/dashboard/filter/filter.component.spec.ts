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

import { ComponentFixture, discardPeriodicTasks, fakeAsync, flushMicrotasks, inject, TestBed, tick, waitForAsync } from '@angular/core/testing';

import { FilterComponent } from './filter.component';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { UntypedFormControl, UntypedFormGroup, FormsModule, ReactiveFormsModule, FormControl } from '@angular/forms';
import { HttpService } from '../../services/http.service';
import { SharedService } from '../../services/shared.service';
import { APP_CONFIG, AppConfig } from '../../services/app.config';
import { GetAuthorizationService } from 'src/app/services/get-authorization.service';
import { ExcelService } from '../../services/excel.service';
import { DatePipe } from '../../../../node_modules/@angular/common';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { Router, Routes } from '@angular/router';
import { environment } from 'src/environments/environment';
import { MessageService } from 'primeng/api';
import { HelperService } from 'src/app/services/helper.service';
import { NgSelectModule } from '@ng-select/ng-select';
import { of, throwError } from 'rxjs';
import { GoogleAnalyticsService } from 'src/app/services/google-analytics.service';

class MockRouter {

  // Mock navigate method
  navigate(commands: any[], extras?: any): Promise<boolean> {
    // Implement navigate logic for testing if needed
    return Promise.resolve(true); // Mock resolve value
  }
  navigateByUrl(commands: any[], extras?: any): Promise<boolean> {
    // Implement navigate logic for testing if needed
    return Promise.resolve(true); // Mock resolve value
  }
  url: string = '/';
}

export { MockRouter };

describe('FilterComponent', () => {
  let component: FilterComponent;
  let fixture: ComponentFixture<FilterComponent>;
  let httpService: HttpService;
  let messageService: MessageService;
  let httpMock;
  let sharedService: SharedService;
  let getAuthorizationService: GetAuthorizationService;
  let helperService: HelperService;
  let excelService: ExcelService;
  let ga: GoogleAnalyticsService;
  let router: Router;
  const baseUrl = environment.baseUrl;  // Servers Env

  const fakeFilterData = require('../../../test/resource/fakeFilterData.json');
  const fakeMasterData = require('../../../test/resource/masterData.json');
  const configGlobalData = require('../../../test/resource/fakeGlobalConfigData.json');
  const hierarchyLevels = [
    {
      hierarchyLevelId: 'hierarchyLevelOne',
      hierarchyLevelName: 'Level One',
      id: '63b3f4ea6770d3a031b92492',
      level: 1,
    },
    {
      hierarchyLevelId: 'hierarchyLevelTwo',
      hierarchyLevelName: 'Level Two',
      id: '63b3f4ea6770d3a031b92492',
      level: 2,
    },
    {
      hierarchyLevelId: 'hierarchyLevelThree',
      hierarchyLevelName: 'Level Three',
      id: '63b3f4ea6770d3a031b92492',
      level: 3,
    },
    {
      hierarchyLevelId: 'project',
      hierarchyLevelName: 'Project',
      id: '63b3f4ea6770d3a031b92493',
      level: 3,
    },
  ];
  const fakeCommentList = require('../../../test/resource/fakeCommentList.json');

  const additionalFiltersDdn =  {
    selectedLevel: [{
      labelName: 'sprint',
      level: 5,
      nodeId: '40201_HvyVrzlpld_63b81ef5224e7b4d03186dab',
      nodeName: 'DTS | KnowHOW | PI_11| ITR_4_HvyVrzlpld',
      parentId: ['HvyVrzlpld_63b81ef5224e7b4d03186dab'],
      path: [
        'HvyVrzlpld_63b81ef5224e7b4d03186dab###Level3_hiera…vel2_hierarchyLevelTwo###Level1_hierarchyLevelOne',
      ],
      sprintEndDate: '2022-11-23T10:20:00.0000000',
      sprintStartDate: '2022-11-09T10:20:00.0000000',
      sprintState: 'CLOSED',
    }],
  }

  const releaseList = [{
    labelName: "release",
    level: 5,
    nodeId: "844_DOTC_63b51633f33fd2360e9e72bd",
    nodeName: "KnowHow 6.7.0",
    parentId: ["DOTC_63b51633f33fd2360e9e72bd"],
    path: "DOTC_63b51633f33fd2360e9e72bd###D3_hierarchyLevelThree###D2_hierarchyLevelTwo###D1_hierarchyLevelOne",
    releaseEndDate: "2023-05-14T19:11:00.0000000",
    releaseStartDate: "2023-04-17T19:11:00.0000000",
    releaseState: "RELEASED",
  },
  {
    labelName: "release",
    level: 5,
    nodeId: "934_DOTC_63b51633f33fd2360e9e72bd",
    nodeName: "KnowHow 6.8.0",
    parentId: ["DOTC_63b51633f33fd2360e9e72bd"],
    path: "DOTC_63b51633f33fd2360e9e72bd###D3_hierarchyLevelThree###D2_hierarchyLevelTwo###D1_hierarchyLevelOne",
    releaseEndDate: "2023-04-11T11:52:00.0000000",
    releaseStartDate: "2023-01-04T16:04:03.6900000",
    releaseState: "UNRELEASED",
  }
]

  const selectedFilterArray = [
    {
      additionalFilters: {
        grossMaturity: 'Maturity Score : NA',
        labelName: 'sprint',
        level: 5,
        nodeId: '844_DOTC_63b51633f33fd2360e9e72bd',
        nodeName: 'MA_Sprint 23.01_DOTC',
        parentId: ['DOTC_63b51633f33fd2360e9e72bd'],
        path: [
          'DOTC_63b51633f33fd2360e9e72bd###D3_hierarchyLevelThree###D2_hierarchyLevelTwo###D1_hierarchyLevelOne',
        ],
        sprintEndDate: '2023-01-17T22:00:00.0000000',
        sprintStartDate: '2023-01-04T16:04:03.6900000',
        sprintState: 'ACTIVE',
      },
    },
  ];

  const selectedFilterArrayNestedArray =  [
    {
      additionalFilters:[ {
        grossMaturity: 'Maturity Score : NA',
        labelName: 'sprint',
        level: 5,
        nodeId: '844_DOTC_63b51633f33fd2360e9e72bd',
        nodeName: 'MA_Sprint 23.01_DOTC',
        parentId: ['DOTC_63b51633f33fd2360e9e72bd'],
        path: [
          'DOTC_63b51633f33fd2360e9e72bd###D3_hierarchyLevelThree###D2_hierarchyLevelTwo###D1_hierarchyLevelOne',
        ],
        sprintEndDate: '2023-01-17T22:00:00.0000000',
        sprintStartDate: '2023-01-04T16:04:03.6900000',
        sprintState: 'ACTIVE',
      },]
    },
  ];

  const additionalFiltersArr = [
    { hierarchyLevelId: 'sprint', hierarchyLevelName: 'Sprint', level: 5 },
    {
      hierarchyLevelId: 'afOne',
      hierarchyLevelName: 'Additional Filter One',
      level: 6,
    },
  ];

  const trendLineValueList = [{
    labelName: 'hierarchyLevelOne',
    level: 1,
    nodeId: 'AutoTest1_hierarchyLevelOne',
    nodeName: 'AutoTest1',
    parentId: [undefined],
    path: [''],
  }];

  const filterApplyData = {
    ids: [
      'bittest_corporate'
    ],
    sprintIncluded: [
      'CLOSED'
    ],
    selectedMap: {
      corporate: [
        'bittest_corporate'
      ],
      business: [],
      account: [],
      subaccount: [],
      project: [],
      sprint: [],
      sqd: []
    },
    level: 1
  };

  const fakeSelectedFilterArray =  [
    {
        "nodeId": "45160_ADDD_649a920fdf3e6c21e3968e30",
        "nodeName": "KnowHOW | PI_14| ITR_1_ADDD",
        "sprintStartDate": "2023-06-28T05:41:00.0000000",
        "sprintEndDate": "2023-07-11T05:41:00.0000000",
        "path": [
            "ADDD_649a920fdf3e6c21e3968e30###2021 WLP Brand Retainer_port###ADEO_acc###Education_ver###A_bu"
        ],
        "labelName": "sprint",
        "parentId": [
            "ADDD_649a920fdf3e6c21e3968e30"
        ],
        "sprintState": "ACTIVE",
        "level": 6
    }
]

const completeHierarchyData = {
  kanban: [
   {
    level: 4,
    hierarchyLevelId: "port",
    hierarchyLevelName: "Portfolio"
   },
    {
      level: 6,
      hierarchyLevelId: 'sqd',
      hierarchyLevelName: 'Squad'
    }
  ],
  scrum: [

    {
      level: 4,
      hierarchyLevelId: "port",
      hierarchyLevelName: "Portfolio"
     }
  ]
};

  beforeEach(() => {
    const routes: Routes = [
      { path: 'dashboard', component: FilterComponent }

    ];


    TestBed.configureTestingModule({
      declarations: [FilterComponent],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
      imports: [FormsModule, HttpClientTestingModule, ReactiveFormsModule, NgSelectModule, FormsModule, RouterTestingModule
      ],
      providers: [HttpService, SharedService, ExcelService, DatePipe, GetAuthorizationService, MessageService, HelperService,
        { provide: APP_CONFIG, useValue: AppConfig }]
        // { provide: Router, useClass: MockRouter }]
    })
      .compileComponents();

      jasmine.DEFAULT_TIMEOUT_INTERVAL = 10000;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FilterComponent);
    component = fixture.componentInstance;
    sharedService = TestBed.inject(SharedService);
    httpService = TestBed.inject(HttpService);
    getAuthorizationService = TestBed.inject(GetAuthorizationService);
    helperService = TestBed.inject(HelperService);
    messageService = TestBed.inject(MessageService);
    excelService = TestBed.inject(ExcelService);
    httpMock = TestBed.inject(HttpTestingController);
    ga = TestBed.inject(GoogleAnalyticsService)
    router = TestBed.inject(Router);
    spyOn(sharedService.passDataToDashboard, 'emit');
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('when  tab is clicked and  scrum is selected', (done) => {
    const selectedTab = 'my-knowhow';
    const boardId = 1;
    fixture.detectChanges();
    expect(component.kanban).toBeFalsy();
    done();
  });

  it('when  tab is clicked and  kanban is selected', (done) => {
    fixture.detectChanges();
    const selectedTab = 'my-knowhow';
    const boardId = 7;
    component.selectedType('Kanban');
    fixture.detectChanges();
    expect(component.kanban).toBeTruthy();
    done();
  });

  it('when Kanban is clicked', (done) => {
    component.selectedType('Kanban');
    expect(component.kanban).toBeTruthy();
    done();
  });
  it('when Scrum is clicked', (done) => {
    component.selectedType('Scrum');
    expect(component.kanban).toBeFalsy();
    done();
  });


  xit('checking master data on load', () => {
    fixture.detectChanges();
    httpService.getMasterData().subscribe(masterData => {
      expect(component.masterData.kpiList.length).toBe(fakeMasterData.kpiList.length);
    });
    httpMock.expectOne(baseUrl + '/api/masterData').flush(fakeMasterData);
  });


  it('should get Hierarchy levels when hierarchy  is null', fakeAsync(() => {
    const spy = spyOn(httpService, 'getAllHierarchyLevels').and.returnValue(of(fakeFilterData));
    const spygetFilterDataOnLoad = spyOn(component, 'getFilterDataOnLoad');
    const spySetLevel = spyOn(component, 'setLevels');
    component.setHierarchyLevels();
    tick();
    expect(spygetFilterDataOnLoad).toHaveBeenCalled();
    expect(spySetLevel).toHaveBeenCalled();
  }));

  it('should not get Hierarchy levels when hierarchy is not null', fakeAsync(() => {
    component.hierarchies = {
      id : 'Project',
      lavelName : 'Project',
      lavel : 4
    };
    const spy = spyOn(httpService, 'getAllHierarchyLevels').and.returnValue(of(fakeFilterData));
    const spygetFilterDataOnLoad = spyOn(component, 'getFilterDataOnLoad');
    const spySetLevel = spyOn(component, 'setLevels');
    component.setHierarchyLevels();
    tick();
    expect(spygetFilterDataOnLoad).toHaveBeenCalled();
    expect(spySetLevel).toHaveBeenCalled();
    expect(spy).not.toHaveBeenCalled();
  }));


  // it('should render downloaded Excel', () => {
  //   let response = JSON.parse(JSON.stringify(fakeFilterData));
  //   response.data = [];
  //   component.renderDownloadExcel(response);
  //   expect(component.enginneringMaturityErrorMessage).toEqual('No Data Available');

  //   response.success = false;
  //   component.renderDownloadExcel(response);
  //   expect(component.enginneringMaturityErrorMessage).toEqual('No Access!');

  //   response = undefined;
  //   component.renderDownloadExcel(response);
  //   expect(component.enginneringMaturityErrorMessage).toEqual('Some error occurred!');

  // });

  // it('should make array unique', () => {
  //   const input = [
  //     {
  //       nodeId: 'UI_sqd_63281fda79d8f0130811b6d5',
  //       nodeName: 'UI',
  //       path: [
  //         '38295_SonarTest_63281fda79d8f0130811b6d5###SonarTest_63281fda79d8f0130811b6d5###TestP_port###GDD_acc###Consumer Products_ver###FCG_bu',
  //         '38294_SonarTest_63281fda79d8f0130811b6d5###SonarTest_63281fda79d8f0130811b6d5###TestP_port###GDD_acc###Consumer Products_ver###FCG_bu'
  //       ],
  //       labelName: 'sqd',
  //       parentId: [
  //         '38295_SonarTest_63281fda79d8f0130811b6d5',
  //         '38294_SonarTest_63281fda79d8f0130811b6d5'
  //       ],
  //       level: 7
  //     },
  //     {
  //       nodeId: 'UI_sqd_63281fda79d8f0130811b6d5',
  //       nodeName: 'UI',
  //       path: '38294_SonarTest_63281fda79d8f0130811b6d5###SonarTest_63281fda79d8f0130811b6d5###TestP_port###GDD_acc###Consumer Products_ver###FCG_bu',
  //       labelName: 'sqd',
  //       parentId: '38294_SonarTest_63281fda79d8f0130811b6d5',
  //       level: 7
  //     }
  //   ];
  //   const output = [
  //     {
  //       nodeId: 'UI_sqd_63281fda79d8f0130811b6d5',
  //       nodeName: 'UI',
  //       path: [
  //         '38295_SonarTest_63281fda79d8f0130811b6d5###SonarTest_63281fda79d8f0130811b6d5###TestP_port###GDD_acc###Consumer Products_ver###FCG_bu',
  //         '38294_SonarTest_63281fda79d8f0130811b6d5###SonarTest_63281fda79d8f0130811b6d5###TestP_port###GDD_acc###Consumer Products_ver###FCG_bu'
  //       ],
  //       labelName: 'sqd',
  //       parentId: [
  //         '38295_SonarTest_63281fda79d8f0130811b6d5',
  //         '38294_SonarTest_63281fda79d8f0130811b6d5'
  //       ],
  //       level: 7
  //     }
  //   ];

  //   const result = component.makeUniqueArrayList(input);
  //   expect(result.length).toBe(1);
  // });

  it('should set hierarchy levels', () => {
    component.kanban = false;
    component.hierarchies = {
      kanban: [
      ],
      scrum: [
        {
            id: '634d4013832e582e0d6b3885',
            level: 1,
            hierarchyLevelId: 'hierarchyLevelOne',
            hierarchyLevelName: 'Level One'
        },
        {
            id: '634d4013832e582e0d6b3886',
            level: 2,
            hierarchyLevelId: 'hierarchyLevelTwo',
            hierarchyLevelName: 'Level Two'
        },
        {
            id: '634d4013832e582e0d6b3887',
            level: 3,
            hierarchyLevelId: 'hierarchyLevelThree',
            hierarchyLevelName: 'Level Three'
        },
        {
            level: 4,
            hierarchyLevelId: 'project',
            hierarchyLevelName: 'Project'
        },
        {
            level: 5,
            hierarchyLevelId: 'sprint',
            hierarchyLevelName: 'Sprint'
        }
    ]
    };
    component.setLevels();
    expect(component.hierarchyLevels.length).toBe(4);
    expect(component.additionalFiltersArr.length).toBe(1);
  });

  it('should get  masterData', fakeAsync(() => {
    const spy = spyOn(sharedService, 'getMasterData').and.returnValue([]);
    const spyHttpgetMasterData = spyOn(httpService, 'getMasterData').and.returnValue(of(fakeMasterData));
    const spyprocessMasterData = spyOn(component, 'processMasterData');
    component.getMasterData();
    tick();
    expect(spy).toHaveBeenCalled();
    expect(spyHttpgetMasterData).toHaveBeenCalled();
    expect(spyprocessMasterData).toHaveBeenCalledWith(fakeMasterData);
  }));

  it('process masterData if already data is available', () => {
    const spy = spyOn(sharedService, 'getMasterData').and.returnValue(fakeMasterData);
    const spyprocessMasterData = spyOn(component, 'processMasterData');
    component.getMasterData();
    expect(spyprocessMasterData).toHaveBeenCalledWith(fakeMasterData);
  });

  it('should process master Data', () => {
    component.selectedTab = 'kpi-maturity';
    const spyhandleIteration = spyOn(component, 'handleIterationFilters');
    const spyapplyChanges = spyOn(component, 'applyChanges');
    component.processMasterData(fakeMasterData);
    expect(spyapplyChanges).toHaveBeenCalled();

    component.selectedTab = 'Iteration';
    component.processMasterData(fakeMasterData);
    expect(spyhandleIteration).toHaveBeenCalled();

    spyOn(component,'handleMilestoneFilter')
    component.selectedTab = 'release';
    component.processMasterData(fakeMasterData);
    expect(spyhandleIteration).toHaveBeenCalled();

    // spyOn(component,'applyChanges')
    component.selectedTab = 'developer';
    component.processMasterData(fakeMasterData);
    expect(spyhandleIteration).toHaveBeenCalled();

  });

  it('should set filters empty when selected tab is iteraiton', waitForAsync ( async() => {
    component.toggleDropdown['commentSummary'] = true;
    await component.ngOnInit();
    const spy = spyOn(sharedService, 'setEmptyFilter');
    component.selectedTab = 'iteration';
    const fake = { selectedTab : 'Iteration', selectedType : 'scrum' };
    sharedService.onTypeOrTabRefresh.next(fake);
    expect(spy).toHaveBeenCalled();
    }));

  it('should set showChart as chart when selected tab is kpi-maturity', () => {
    component.toggleDropdown['commentSummary'] = true;
    component.ngOnInit();
    component.selectedTab = 'kpi-maturity';
    const fake = { selectedTab : 'kpi-maturity', selectedType : 'scrum' };
    sharedService.onTypeOrTabRefresh.next(fake);
    expect(component.showChart).toBe('chart');
    });

    it('should set selectedDayType as days when selected tab is developer', waitForAsync ( async () => {
      component.toggleDropdown['commentSummary'] = true;
      await component.ngOnInit();
      component.selectedTab = 'developer';
      const fake = { selectedTab : 'developer', selectedType : 'scrum' };
      sharedService.onTypeOrTabRefresh.next(fake);
      expect(component.selectedDayType).toBe('Days');
      }));

      it('should call getNotifiocation functions',waitForAsync ( async ()=>{
        await component.ngOnInit();
        const spyObj = spyOn(component,'getNotification');
        sharedService.notificationUpdate()
        expect(spyObj).toHaveBeenCalled();
      }))

  it('should set the colorObj', waitForAsync ( async () => {
    await component.ngOnInit();
    const x = {
      'Sample One_hierarchyLevelOne': {
        nodeName: 'Sample One',
        color: '#079FFF'
      }
    };
    sharedService.setColorObj(x);
    
    expect(component.colorObj).toBe(x);
  }));

  it('should set isSuperAdmin flag', () => {
    spyOn(getAuthorizationService, 'checkIfSuperUser').and.returnValue(true);
    component.ngOnInit();
    expect(component.isSuperAdmin).toBeTrue();
  });

  it('should set the Maturity Score', () => {
    const maturityObj = {
      'Scrum Project': 3.1578947368421053
    };
    const selecteFilterArray = [
      {
        nodeId: 'Scrum Project_6335363749794a18e8a4479b',
        nodeName: 'Scrum Project',
        path: [
          'Sample Three_hierarchyLevelThree###Sample Two_hierarchyLevelTwo###Sample One_hierarchyLevelOne'
        ],
        labelName: 'project',
        parentId: [
          'Sample Three_hierarchyLevelThree'
        ],
        level: 4,
        basicProjectConfigId: '6335363749794a18e8a4479b',
        additionalFilters: [],
        grossMaturity: 'Maturity Score : 3.16'
      }
    ];

    const result = 'Maturity Score : 3.16';
    component.selectedFilterArray = selecteFilterArray;
    // helperService.passMaturityToFilter.emit(maturityObj);
    fixture.detectChanges();
    expect(component.selectedFilterArray[0].grossMaturity).toEqual(result);
  });



  it('should get filter data on load', () => {
    spyOn(sharedService, 'getFilterData').and.returnValue(fakeFilterData);
    spyOn(httpService,'getFilterData').and.returnValue(of(fakeFilterData));
    component.previousType = false;
    component.kanban = false;
    component.selectedTab = '';
    component.initFlag = false;
    const spy = spyOn(component, 'processFilterData');
    component.getFilterDataOnLoad();
    fixture.detectChanges();
    expect(spy).toHaveBeenCalled();
  });

  it('should process filter data when filter data is comming', () => {
    spyOn(sharedService, 'getFilterData').and.returnValue(fakeFilterData);
    component.previousType = true;
    component.kanban = false;
    component.selectedTab = '';
    component.initFlag = true;
    const spy = spyOn(component, 'processFilterData');
    const spygetFilterData = spyOn(httpService, 'getFilterData').and.returnValue(of(fakeFilterData));
    component.getFilterDataOnLoad();
    expect(spy).toHaveBeenCalled();
  });

  it('should set Empty Data when filter data is not available', () => {
    const filterData = ['error'];
    const spy = spyOn(sharedService, 'setEmptyData');
    component.processFilterData(filterData);
    expect(spy).toHaveBeenCalledWith(true);
  });

  it('should createForm when filterData is available', () => {
    const additionalFiltersArr = [
      {
          level: 6,
          hierarchyLevelId: 'sprint',
          hierarchyLevelName: 'Sprint'
      },
      {
          level: 7,
          hierarchyLevelId: 'sqd',
          hierarchyLevelName: 'Squad'
      }
  ];
  component.initializeFilterForm();

  spyOn(component,'checkIfFilterAlreadySelected');
    component.additionalFiltersArr = additionalFiltersArr;
    const spy = spyOn(sharedService, 'setFilterData');
    const spycreateFormGroup = spyOn(component, 'createFormGroup');
    component.processFilterData(fakeFilterData);
    expect(spycreateFormGroup).toHaveBeenCalled();
  });

  it('should set no project if filter data length is 0', () => {
    const additionalFiltersArr = [
      {
          level: 6,
          hierarchyLevelId: 'sprint',
          hierarchyLevelName: 'Sprint'
      },
      {
          level: 7,
          hierarchyLevelId: 'sqd',
          hierarchyLevelName: 'Squad'
      }
  ];

  spyOn(component,'checkIfFilterAlreadySelected');
    component.additionalFiltersArr = additionalFiltersArr;
    const spyObj = spyOn(sharedService,'setNoProjects')
    const fakedata = {data : []}
    component.processFilterData(fakedata);
    expect(spyObj).toHaveBeenCalled();
  });

  it('should create form group based on level for blank array', () => {
    component.filterForm = new UntypedFormGroup({
      sprint: new UntypedFormControl()
    });
    component.createFormGroup('sprint', []);
    expect(component.filterForm.controls['sprint'].value).toBeFalsy();
  });

  it('should create form group based on level when array is not blank', () => {
    const arr = [{nodeId : 'n1'},{nodeId : 'n2'}]
    component.filterForm = new UntypedFormGroup({
      sprint: new UntypedFormControl()
    });
    component.createFormGroup('sprint', arr);
    expect(component.filterForm).toBeDefined()
  });


  it('should set Selected Day Type', () => {
    component.setSelectedDateType('Day');
    expect(component.selectedDayType).toEqual('Day');
  });

  it('should  assign UserName For KpiData', () => {
    spyOn(sharedService,'getCurrentUserDetails').and.returnValue('dummy user')
    component.kpiListData = {
      username: undefined,
      id: 1
    };
    component.assignUserNameForKpiData();
    expect(component.kpiListData.username).toBe('dummy user');
  });

  it('should navigate To Selected Tab', inject([Router], (router: Router) => {
    component.selectedTab = 'Speed';
    component.kanban = false;
    component.kpiListData = configGlobalData['data'];
    const spy = spyOn(router, 'navigate');
    component.navigateToSelectedTab();
    expect(spy).toHaveBeenCalledWith(['/dashboard/speed'], {queryParamsHandling: 'merge'});
  }));

  it('should get kpiorder list', fakeAsync(() => {
    component.kpiListData = {};
    const spy = spyOn(httpService, 'getShowHideOnDashboard').and.returnValue(of(configGlobalData));
    const spyprocessKpiList = spyOn(sharedService, 'setDashConfigData');
    const spynavigateToSelectedTab = spyOn(component, 'navigateToSelectedTab');
    component.getKpiOrderedList();
    tick();
    expect(spyprocessKpiList).toHaveBeenCalled();
  }));

  it('should show error message on kpiList', fakeAsync(() => {
    component.kpiListData = {};
    const spy = spyOn(httpService, 'getShowHideOnDashboard').and.returnValue(throwError('Something went wrong'));
    const spyMessageService = spyOn(messageService, 'add');
    component.getKpiOrderedList();
    tick();
    expect(spyMessageService).toHaveBeenCalled();
  }));

  it('should call processKpiList when kpiList is available', () => {
    spyOn(component,'isEmptyObject').and.returnValue(false)
    spyOn(helperService,'makeSyncShownProjectLevelAndUserLevelKpis');
    const spyprocessKpiList = spyOn(sharedService, 'setDashConfigData')
    component.getKpiOrderedList();
    expect(spyprocessKpiList).toHaveBeenCalled();
  });

  it('should kpiList not blank for other than backlog and iteration', () => {
    component.selectedTab = '';
    component.kanban = false;
    component.kpiListData = configGlobalData['data'];
    component.processKpiList();
    expect(component.kpiList).not.toBeNull();
  });

  it('should kpiList not blank for backlog', () => {
    component.selectedTab = 'Backlog';
    component.kanban = false;
    component.kpiListData = configGlobalData['data'];
    component.processKpiList();
    expect(component.kpiList).not.toBeNull();
  });

  it('should kpiList not blank for iteration', () => {
    component.selectedTab = 'Iteration';
    component.kanban = false;
    component.kpiListData = configGlobalData['data'];
    component.processKpiList();
    expect(component.kpiList).not.toBeNull();
  });

  it('should kpiList not blank for release', () => {
    component.selectedTab = 'release';
    component.kanban = false;
    component.kpiListData = configGlobalData['data'];
    component.processKpiList();
    expect(component.kpiList).not.toBeNull();
  });

  it('should kpiList not blank for dora', () => {
    component.selectedTab = 'dora';
    component.kanban = false;
    component.kpiListData = configGlobalData['data'];
    component.processKpiList();
    expect(component.kpiList).not.toBeNull();
  });

  it('should handle all kpi change', () => {
    component.kpiForm = new UntypedFormGroup({
      kpis: new UntypedFormControl()
    });
    component.showKpisList = [
      {
        kpiId: 'kpi14',
        kpiName: 'Defect Injection Rate',
        isEnabled: true,
      }
    ];
    const event = {
      checked: false
    };
    component.handleAllKpiChange(event);
    expect(component.kpiFormValue['kpis'].valid).toBeTruthy();
  });

  it('should set enableAllKpis to false', () => {
    const event = {
      checked: false
    };
    component.kpiForm = new UntypedFormGroup({
      enableAllKpis: new UntypedFormControl()
    });
    component.handleKpiChange(event);
    expect(component.kpiFormValue['enableAllKpis'].value).toBeFalsy();
  });

  it('should set marker for selected options', () => {

    const selecteFilterArray = [
      {
        nodeId: 'Scrum Project_6335363749794a18e8a4479b',
        nodeName: 'Scrum Project',
        path: [
          'Sample Three_hierarchyLevelThree###Sample Two_hierarchyLevelTwo###Sample One_hierarchyLevelOne'
        ],
        labelName: 'project',
        parentId: [
          'Sample Three_hierarchyLevelThree'
        ],
        level: 4,
        basicProjectConfigId: '6335363749794a18e8a4479b',
        additionalFilters: [],
        grossMaturity: 'Maturity Score : 3.16'
      }
    ];
    const spy = spyOn(sharedService, 'setColorObj');
    component.setMarker();
    expect(spy).toHaveBeenCalled();
  });

  it('should handle select', () => {
    component.filterData = fakeFilterData['data'];
    const spy = spyOn(helperService, 'makeUniqueArrayList');
    component.handleSelect('project');
    expect(spy).toHaveBeenCalled();
  });


  it('should check for default filter selection for iteration tab and no projects available', () => {
    const filterData = [
      {
        nodeId: 'BITBUCKET_DEMO_632c46c6728e93266f5d5631',
        nodeName: 'BITBUCKET_DEMO',
        path: 't3_subaccount###t2_account###t1_business###bittest_corporate',
        labelName: 'project1',
        parentId: 't3',
        level: 5,
        basicProjectConfigId: '632c46c6728e93266f5d5631'
      }];
    component.selectedTab = 'iteration';
    component.filterData = filterData;
    const spy = spyOn(component, 'getProcessorsTraceLogsForProject');
    component.trendLineValueList = [];
    component.checkDefaultFilterSelection();
    expect(spy).not.toHaveBeenCalled();
  });

  it('should check for default filter selection', () => {
    const filterData = [
      {
        nodeId: 'BITBUCKET_DEMO_632c46c6728e93266f5d5631',
        nodeName: 'BITBUCKET_DEMO',
        path: 't3_subaccount###t2_account###t1_business###bittest_corporate',
        labelName: 'project',
        parentId: 't3',
        level: 5,
        basicProjectConfigId: '632c46c6728e93266f5d5631'
      }];

    const hierarchyLevels = [
      {
        id: '63244d35d1d9f4caf85056f7',
        level: 1,
        hierarchyLevelId: 'corporate',
        hierarchyLevelName: 'Corporate Name'
      },
      {
        id: '63244d35d1d9f4caf85056f8',
        level: 2,
        hierarchyLevelId: 'business',
        hierarchyLevelName: 'Business Name'
      },
      {
        id: '63244d35d1d9f4caf85056f9',
        level: 3,
        hierarchyLevelId: 'dummyaccount',
        hierarchyLevelName: 'dummyAccount Name'
      },
      {
        id: '63244d35d1d9f4caf85056fa',
        level: 4,
        hierarchyLevelId: 'dummysubaccount',
        hierarchyLevelName: 'dummySubaccount'
      },
      {
        level: 5,
        hierarchyLevelId: 'dummyproject',
        hierarchyLevelName: 'dummyProject'
      }
    ];
    component.selectedTab = '';
    component.filterData = filterData;
    component.trendLineValueList = [];
    component.hierarchyLevels = hierarchyLevels;
    spyOn(component, 'setTrendValueFilter');
    component.checkDefaultFilterSelection();
    expect(component.trendLineValueList.length).toBeGreaterThan(0);
  });

  it('should check if Add Filter Disabled', () => {

    component.filterForm = new UntypedFormGroup({
      selectedLevel: new UntypedFormControl('project')
    });
    component.kanban = false;
    component.filterApplyData = filterApplyData;
    component.filteredAddFilters = [];
    const result = component.isAddFilterDisabled('sprint');
    expect(result).toBeTrue();
  });

  it('should handle iteration filter for active sprints', () => {
    component.filterForm = new UntypedFormGroup({
      selectedTrendValue: new UntypedFormControl('DOTC_63b51633f33fd2360e9e72bd'),
      selectedSprintValue: new UntypedFormControl('40201_HvyVrzlpld_63b81ef5224e7b4d03186dab')
    });
    component.selectedFilterArray = [];
    component.trendLineValueList = [{nodeId:'DOTC_63b51633f33fd2360e9e72bd', basicProjectConfigId: '63284960fdd20276d60e4df5'}];
    component.additionalFiltersDdn = {
      sprint : [{
        labelName: 'sprint',
        level: 5,
        nodeId: '40201_HvyVrzlpld_63b81ef5224e7b4d03186dab',
        nodeName: 'DTS | KnowHOW | PI_11| ITR_4_HvyVrzlpld',
        parentId: ['DOTC_63b51633f33fd2360e9e72bd'],
        path: [
          'HvyVrzlpld_63b81ef5224e7b4d03186dab###Level3_hiera…vel2_hierarchyLevelTwo###Level1_hierarchyLevelOne',
        ],
        sprintEndDate: '2022-11-23T10:20:00.0000000',
        sprintStartDate: '2022-11-09T10:20:00.0000000',
        sprintState: 'active',
      }]
    };
    const spy = spyOn(component, 'getProcessorsTraceLogsForProject');
    spyOn(sharedService, 'setNoSprints');
    spyOn(component, 'createFilterApplyData');
    component.handleIterationFilters('project',true);
    expect(spy).toHaveBeenCalled();
  });

  it('should handle iteration filter for sqd', () => {
    const selectedSqd = {
      'sq1' : new UntypedFormControl(true)
    }
    component.filterForm = new UntypedFormGroup({
      selectedTrendValue: new UntypedFormControl('DOTC_63b51633f33fd2360e9e72bd'),
      selectedSprintValue: new UntypedFormControl('40201_HvyVrzlpld_63b81ef5224e7b4d03186dab'),
      'sqd' : new UntypedFormControl(true)
    });
    component.selectedSprint = [{nodeName : 'sprint'}]
    component.selectedFilterArray = [];
    component.trendLineValueList = [{nodeId:'DOTC_63b51633f33fd2360e9e72bd', basicProjectConfigId: '63284960fdd20276d60e4df5'}];
    component.additionalFiltersDdn = {
      sqd : [{
        labelName: 'sqd',
        level: 5,
        nodeId: 'sq1',
        nodeName: 'DTS | KnowHOW | PI_11| ITR_4_HvyVrzlpld',
        parentId: ['DOTC_63b51633f33fd2360e9e72bd'],
        path: [
          'HvyVrzlpld_63b81ef5224e7b4d03186dab###Level3_hiera…vel2_hierarchyLevelTwo###Level1_hierarchyLevelOne',
        ],
        sprintEndDate: '2022-11-23T10:20:00.0000000',
        sprintStartDate: '2022-11-09T10:20:00.0000000',
        sprintState: 'active',
      }]
    };
    const spy = spyOn(component, 'getProcessorsTraceLogsForProject');
    spyOn(sharedService, 'setNoSprints');
    spyOn(component, 'createFilterApplyData');
    component.handleIterationFilters('sqd',true);
    expect(spy).toHaveBeenCalled();
  });


  it('should handle iteration filter for close sprints', () => {
    component.filterForm = new UntypedFormGroup({
      selectedTrendValue: new UntypedFormControl('DOTC_63b51633f33fd2360e9e72bd'),
      selectedSprintValue: new UntypedFormControl('40201_HvyVrzlpld_63b81ef5224e7b4d03186dab')
    });
    component.selectedFilterArray = [];
    component.trendLineValueList = [{nodeId:'DOTC_63b51633f33fd2360e9e72bd', basicProjectConfigId: '63284960fdd20276d60e4df5'}];
    component.additionalFiltersDdn = {
      sprint : [{
        labelName: 'sprint',
        level: 5,
        nodeId: '40201_HvyVrzlpld_63b81ef5224e7b4d03186dab',
        nodeName: 'DTS | KnowHOW | PI_11| ITR_4_HvyVrzlpld',
        parentId: ['DOTC_63b51633f33fd2360e9e72bd'],
        path: [
          'HvyVrzlpld_63b81ef5224e7b4d03186dab###Level3_hiera…vel2_hierarchyLevelTwo###Level1_hierarchyLevelOne',
        ],
        sprintEndDate: '2022-11-23T10:20:00.0000000',
        sprintStartDate: '2022-11-09T10:20:00.0000000',
        sprintState: 'closed',
      }]
    };
    const spy = spyOn(component, 'getProcessorsTraceLogsForProject');
    spyOn(sharedService, 'setNoSprints');
    spyOn(component, 'createFilterApplyData');
    component.handleIterationFilters('project');
    expect(spy).toHaveBeenCalled();
  });

  it('should get format date based on iteration/milestone', () => {
    component.selectedTab = 'iteration';
    component.filteredAddFilters = {
      sprint: [
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
        }]
    };
    component.filterForm = new UntypedFormGroup({
      selectedSprintValue: new UntypedFormControl('38998_DEMO_SONAR_63284960fdd20276d60e4df5')
    });

    let result = component.getDate('start');
    expect(result).toBe('07/Sep/2022');

    let result2 = component.getDate('end');
    expect(result2).toBe('27/Sep/2022');
  });

  it("should call formatted date for iteration",()=>{
    component.selectedTab = 'iteration';
    const spy = spyOn(component,"getFormatDateBasedOnIterationAndMilestone");
    component.getDate('start');
    expect(spy).toHaveBeenCalled();
  })

  it("should call formatted date for milestone",()=>{
    component.selectedTab = 'release';
    const spy = spyOn(component,"getFormatDateBasedOnIterationAndMilestone");
    component.getDate('start');
    expect(spy).toHaveBeenCalled();
  })


  it('should remove sprint', () => {
    component.selectedTab = 'knowhow'
    component.filterForm = new UntypedFormGroup({
      sprint: new UntypedFormControl({})
    });
    component.selectedTab = "value";

    const spy = spyOn(component, 'applyChanges');
    component.removeItem('sprint', '38994_DEMO_SONAR_63284960fdd20276d60e4df5');
    expect(component.filterForm.get('sprint').value).toBeFalsy();
  });

  it('should remove sprint for sqd and iteration tab', () => {
    component.selectedTab = 'iteration'
    component.filterForm = new UntypedFormGroup({
      sqd: new UntypedFormControl({})
    });

    const spy = spyOn(component, 'handleIterationFilters');
    component.removeItem('sqd', '38994_DEMO_SONAR_63284960fdd20276d60e4df5');
    expect(component.filterForm.get('sqd').value).toBeFalsy();
  });

  it('should remove node', () => {
    const selectedFilterArray = [
      {
        nodeId: 'bittest_corporate',
        nodeName: 'bittest',
        path: [
          ''
        ],
        labelName: 'corporate',
        level: 1,
        parentId: [
          null
        ],
        additionalFilters: [],
        grossMaturity: 'Maturity Score : 0.26'
      },
      {
        nodeId: 'Corpate1_corporate',
        nodeName: 'Corpate1',
        path: [
          ''
        ],
        labelName: 'corporate',
        level: 1,
        parentId: [
          null
        ],
        additionalFilters: [],
        grossMaturity: 'Maturity Score : 1.37'
      },
      {
        nodeId: 'Leve1_corporate',
        nodeName: 'Leve1',
        path: [
          ''
        ],
        labelName: 'corporate',
        level: 1,
        parentId: [
          null
        ],
        additionalFilters: [],
        grossMaturity: 'Maturity Score : 1.37'
      }
    ];

    component.filterForm = new UntypedFormGroup({
      selectedTrendValue: new UntypedFormControl('')
    });
    const spy = spyOn(component, 'applyChanges');
    component.removeNode('Corpate1_corporate');
    expect(spy).toHaveBeenCalled();
  });

  it('should submit kpi config chane', fakeAsync(() => {
    component.kpiList = [
      {
        kpiId: 'kpi14',
        kpiName: 'Defect Injection Rate',
        isEnabled: false,
        order: 1,
        kpiDetail: {},
        shown: true
      }];
    component.kpiListData = configGlobalData['data'];
    component.kanban = false;
    component.selectedTab = 'My KnowHOW';
    component.kpiForm = new UntypedFormGroup({
      kpis: new UntypedFormControl({
        kpi14: true
      })
    });

    spyOn(component, 'assignUserNameForKpiData');
    spyOn(httpService, 'submitShowHideOnDashboard').and.returnValue(of({ success: true }));
    spyOn(sharedService, 'setDashConfigData');
    component.submitKpiConfigChange();
    tick();
    expect(component.toggleDropdown['showHide']).toBeFalse();
  }));

  it("should get processor trace log details",()=>{
    const fakeResponce = {
      message : "Successfully",
      success : true
    }
    spyOn(httpService,"getProcessorsTraceLogsForProject").and.returnValue(of(fakeResponce));
    spyOn(component,"findTraceLogForTool");
    spyOn(component,'showExecutionDate');
    component.getProcessorsTraceLogsForProject("63284960fdd20276d60e4df5");
    expect(httpService.getProcessorsTraceLogsForProject).toHaveBeenCalled();
  })

  it("should get processor trace log details and selectedProjectData should be there",()=>{
    const fakeResponce = {
      message : "Successfully",
      success : true
    }
    spyOn(httpService,"getProcessorsTraceLogsForProject").and.returnValue(of(fakeResponce));
    spyOn(component,"findTraceLogForTool");
    spyOn(component,'showExecutionDate');
    component.selectedProjectData['basicProjectConfigId'] = "63284960fdd20276d60e4df5";
    component.getProcessorsTraceLogsForProject("63284960fdd20276d60e4df5");
    expect(httpService.getProcessorsTraceLogsForProject).toHaveBeenCalled();
  })

  it("should give messsge if request is failing",()=>{
    messageService = TestBed.inject(MessageService);
    const fakeResponce = {
      message : "Error occured while getting data",
      success : false
    }
    spyOn(messageService,"add");
    spyOn(component,"findTraceLogForTool");
    spyOn(component,"showExecutionDate");
    spyOn(httpService,"getProcessorsTraceLogsForProject").and.returnValue(of(fakeResponce));
    component.getProcessorsTraceLogsForProject("63284960fdd20276d60e4df5");
    expect(component.showExecutionDate).not.toHaveBeenCalled();
  })

  it("should apply filters based on node selection",()=>{

    component.hierarchyLevels = hierarchyLevels;
    component.trendLineValueList = trendLineValueList;
    component.additionalFiltersDdn =  additionalFiltersDdn;
    component.additionalFiltersArr = additionalFiltersArr;
    component.selectedFilterArray = selectedFilterArray;
    component.ngOnInit();
    spyOn(component,"sortAlphabetically");
    spyOn(sharedService,"setSelectedLevel");
    spyOn(sharedService,"setSelectedTrends");
    spyOn(component,"setSelectedSprintOnServiceLayer");
    component.filterForm.get('selectedLevel').setValue("hierarchyLevelOne");
    component.filterForm.get('selectedTrendValue').setValue("AutoTest1_hierarchyLevelOne");
    spyOn(component,"compileGAData");
    component.applyChanges('sprint',true);
    expect(sharedService.setSelectedLevel).toHaveBeenCalled();
    expect(sharedService.setSelectedTrends).toHaveBeenCalled();
  })

  it("should disabled taggle dropdown",()=>{
    component.hierarchyLevels = hierarchyLevels;
    component.trendLineValueList = trendLineValueList;
    component.additionalFiltersDdn =  additionalFiltersDdn;
    component.additionalFiltersArr = additionalFiltersArr;
    component.selectedFilterArray = selectedFilterArray;
    component.kanban = true;
    spyOn(component,"sortAlphabetically");
    spyOn(sharedService,"setSelectedLevel");
    spyOn(sharedService,"setSelectedTrends");
    spyOn(component,"setSelectedSprintOnServiceLayer");
    component.ngOnInit();
    component.filterForm?.get('selectedLevel')?.setValue("hierarchyLevelOne");
    component.filterForm?.get('selectedTrendValue')?.setValue("AutoTest1_hierarchyLevelOne");
    spyOn(component,"compileGAData");
    component.applyChanges("date",true);
    expect(component.toggleDateDropdown).toBeFalsy();
  })

  it("should filter apply data when apply source is undefined",()=>{
    component.hierarchyLevels = hierarchyLevels;
    component.trendLineValueList = trendLineValueList;
    component.additionalFiltersDdn =  additionalFiltersDdn;
    component.additionalFiltersArr = additionalFiltersArr;
    component.selectedFilterArray = selectedFilterArray;
    component.kanban = true;
    spyOn(component,"sortAlphabetically");
    spyOn(sharedService,"setSelectedLevel");
    spyOn(sharedService,"setSelectedTrends");
    component.ngOnInit();
    component.filterForm?.get('selectedLevel')?.setValue("hierarchyLevelOne");
    component.filterForm?.get('selectedTrendValue')?.setValue(["AutoTest1_hierarchyLevelOne"]);
    spyOn(component,"compileGAData");
    component.applyChanges(undefined,true);
    expect(component.toggleDateDropdown).toBeFalsy();
  })

  it("should get data when sprint selected for trending (speed/quality/value) tabs",()=>{
      component.hierarchyLevels = hierarchyLevels;
    component.trendLineValueList = trendLineValueList;
    component.additionalFiltersDdn =  {
      sprint : [{
        labelName: 'sprint',
        level: 5,
        nodeId: 'AutoTest1_hierarchyLevelOne',
        nodeName: 'DTS | KnowHOW | PI_11| ITR_4_HvyVrzlpld',
        parentId: ['AutoTest1_hierarchyLevelOne'],
        path: [
          'HvyVrzlpld_63b81ef5224e7b4d03186dab###Level3_hiera…vel2_hierarchyLevelTwo###Level1_hierarchyLevelOne',
        ],
        sprintEndDate: '2022-11-23T10:20:00.0000000',
        sprintStartDate: '2022-11-09T10:20:00.0000000',
        sprintState: 'CLOSED',
      }]
    };
    component.additionalFiltersArr = additionalFiltersArr;
    component.selectedFilterArray = [{
      nideId : "AutoTest1_hierarchyLevelOne"
    }];
    component.kanban = true;
    spyOn(component,"sortAlphabetically");
    spyOn(sharedService,"setSelectedLevel");
    spyOn(sharedService,"setSelectedTrends");
    component.ngOnInit();
    component.filterForm = new UntypedFormGroup({
      selectedTrendValue: new UntypedFormControl(),
      date: new UntypedFormControl(''),
      selectedLevel: new UntypedFormControl(),
      selectedSprintValue: new UntypedFormControl(),
      selectedRelease: new UntypedFormControl(),
      sprint: new UntypedFormControl(),
    });
    component.filterForm?.get('selectedLevel')?.setValue("hierarchyLevelOne");
    component.filterForm?.get('selectedTrendValue')?.setValue(["AutoTest1_hierarchyLevelOne"]);
    component.filterForm?.get('sprint')?.setValue({
      AutoTest1_hierarchyLevelOne : true
    });
    spyOn(component,"compileGAData");
    component.applyChanges("sprint",true);
    expect(component.toggleDateDropdown).toBeFalsy();
  })

  it("should apply filter on addtional filters",()=>{
    component.filteredAddFilters = {};
    component.additionalFiltersDdn = additionalFiltersDdn;
    component.ngOnInit();
    component.filterForm?.get('selectedLevel')?.setValue("project");
    component.filterForm?.get('selectedTrendValue')?.setValue("AutoTest1_hierarchyLevelOne");
    component.filterAdditionalFilters();
    expect(component.filteredAddFilters['selectedLevel']).not.toBeNull()
  })

  it("should apply filter on addtional filters for sprint",()=>{
    component.filteredAddFilters = {};
    component.additionalFiltersDdn = {
      sprint : [{
        labelName: 'sprint',
        level: 5,
        nodeId: '40201_HvyVrzlpld_63b81ef5224e7b4d03186dab',
        nodeName: 'DTS | KnowHOW | PI_11| ITR_4_HvyVrzlpld',
        parentId: ['HvyVrzlpld_63b81ef5224e7b4d03186dab'],
        path: [
          'HvyVrzlpld_63b81ef5224e7b4d03186dab###Level3_hiera…vel2_hierarchyLevelTwo###Level1_hierarchyLevelOne',
        ],
        sprintEndDate: '2022-11-23T10:20:00.0000000',
        sprintStartDate: '2022-11-09T10:20:00.0000000',
        sprintState: 'CLOSED',
      }],
    };
    component.ngOnInit();
    component.filterForm?.get('selectedLevel')?.setValue("project");
    component.filterForm?.get('selectedTrendValue')?.setValue("AutoTest1_hierarchyLevelOne");
    component.filterAdditionalFilters();
    expect(component.filteredAddFilters['selectedLevel']).not.toBeNull()
  })

  it("should exists lable name for sprint filter",()=>{
    component.selectedFilterArray = [
      {
          grossMaturity: 'Maturity Score : NA',
          labelName: 'sprint',
          level: 5,
          nodeId: '844_DOTC_63b51633f33fd2360e9e72bd',
          nodeName: 'MA_Sprint 23.01_DOTC',
          parentId: ['DOTC_63b51633f33fd2360e9e72bd'],
          path: [
            'DOTC_63b51633f33fd2360e9e72bd###D3_hierarchyLevelThree###D2_hierarchyLevelTwo###D1_hierarchyLevelOne',
          ],
          sprintEndDate: '2023-01-17T22:00:00.0000000',
          sprintStartDate: '2023-01-04T16:04:03.6900000',
          sprintState: 'ACTIVE',
      },
    ];
     component.filterApplyData = filterApplyData;
    spyOn(component,"resetFilterApplyObj");
    spyOn(component,"compileGAData");
    component.createFilterApplyData();
    expect(component.filterApplyData['selectedMap']['sprint'].length).toBeGreaterThan(0)
  })

  it("should labels come when addtional filter are applied for sprint",()=>{

    component.selectedFilterArray = selectedFilterArrayNestedArray;

    component.filterApplyData = {
      ids: [
        'bittest_corporate'
      ],
      sprintIncluded: [
        'CLOSED'
      ],
      selectedMap: {
        corporate: [
          'bittest_corporate'
        ],
        business: [],
        account: [],
        subaccount: [],
        project: [],
        sprint: [],
        sqd: []
      },
      level: 1
    };
    spyOn(component,"resetFilterApplyObj");
    spyOn(component,"compileGAData");
    component.createFilterApplyData();
    expect(component.filterApplyData['level']).not.toBeNull();
  })

  it("should labels come when selected filter level and filterdata apply level is same for sprint",()=>{

  component.selectedFilterArray = selectedFilterArrayNestedArray;
   component.filterApplyData = {
      ids: [
        'bittest_corporate'
      ],
      sprintIncluded: [
        'CLOSED'
      ],
      selectedMap: {
        corporate: [
          'bittest_corporate'
        ],
        business: [],
        account: [],
        subaccount: [],
        project: [],
        sprint: [],
        sqd: []
      },
      level: 5
    };
    spyOn(component,"resetFilterApplyObj");
    spyOn(component,"compileGAData");
    component.createFilterApplyData();
    expect(component.filterApplyData['level']).not.toBeNull();
  })

  it("should labels come for kanban when date is not null",()=>{
   component.ngOnInit();
   component.filterApplyData = {
    ids: [
      'bittest_corporate'
    ],
    sprintIncluded: [
      'CLOSED'
    ],
    selectedMap: {
      corporate: [
        'bittest_corporate'
      ]
    },
    level: 5
  };
   component.kanban = true;
   component.filterForm.get('date').setValue('07/09/2022');
    spyOn(component,"resetFilterApplyObj");
    component.createFilterApplyData();
    expect(component.filterApplyData['ids']).not.toBeNull();
  })

  it("should success alert come while submitting show/hide kpi data successfully",()=>{
    component.selectedTab = 'My KnowHOW';
    component.kpiListData = configGlobalData['data'];
    component.kanban = false;
    const fakeResponce = {
      success : true
    }
    spyOn(messageService,'add');
    spyOn(httpService,'submitShowHideOnDashboard').and.returnValue(of(fakeResponce))
     component.setKPIOrder();
     expect(messageService.add).toHaveBeenCalled();
   })

   it("should  alert while submitting show/hide kpi data when response is fail",()=>{
    component.selectedTab = 'My KnowHOW';
    component.kpiListData = configGlobalData['data'];
    component.kanban = false;
    const fakeResponce = {
      success : false
    }
    spyOn(messageService,'add');
    spyOn(httpService,'submitShowHideOnDashboard').and.returnValue(of(fakeResponce))
     component.setKPIOrder();
     expect(messageService.add).toHaveBeenCalled();
   })

   it("should enable show chart toggle ",()=>{
    component.showChartToggle('chart');
    expect(component.showChart).toBe('chart')
   })

   it("should enable if type is spring",()=>{
    component.ngOnInit();
    component.filterForm?.get('sprint')?.setValue("hierarchyLevelOne");
    const result =component.checkIfBtnDisabled("sprint");
    expect(result).toBe(true);
   })

   it("should enable if type is scrum",()=>{
    component.ngOnInit();
    component.filterForm?.get('scrum')?.setValue("hierarchyLevelOne");
    const result =component.checkIfBtnDisabled("scrum");
    expect(result).toBe(true);
   })

   it("should enable tooltip",()=>{
    component.showTooltip(true);
    expect(component.isTooltip).toBe(true);
   })

    xit('should redirect on login page',inject([Router], (router: Router) => {
      const navigateSpy = spyOn(router, 'navigate');
      component.logout();
      httpMock.expectOne(baseUrl + '/api/userlogout').flush(null);
      expect(navigateSpy).toHaveBeenCalledWith(['./authentication/login']);
    }));

    it("should redirect from notification",inject([Router], (router: Router) =>{
      const navigateSpy = spyOn(router, 'navigate');
      component.routeForAccess("Project Access Request");
      expect(navigateSpy).toHaveBeenCalledWith(['/dashboard/Config/Profile/RequestStatus']);
    }))

    it("should redirect on project access from notification for Superadmin and admin",inject([Router], (router: Router) =>{
      const navigateSpy = spyOn(router, 'navigate');
      spyOn(getAuthorizationService,"checkIfSuperUser").and.returnValue(true);
      spyOn(getAuthorizationService,"checkIfProjectAdmin").and.returnValue(true);
      component.routeForAccess("Project Access Request");
      expect(navigateSpy).toHaveBeenCalledWith(['/dashboard/Config/Profile/GrantRequests']);
    }))

    it("should redirect on User access from notification for Superadmin and admin",inject([Router], (router: Router) =>{
      const navigateSpy = spyOn(router, 'navigate');
      spyOn(getAuthorizationService,"checkIfSuperUser").and.returnValue(true);
      spyOn(getAuthorizationService,"checkIfProjectAdmin").and.returnValue(true);
      component.routeForAccess("User Access Request");
      expect(navigateSpy).toHaveBeenCalledWith(['/dashboard/Config/Profile/GrantNewUserAuthRequests']);
    }))

    it("should not redirect on User access/project access from notification for Superadmin and admin",inject([Router], (router: Router) =>{
      const navigateSpy = spyOn(router, 'navigate');
      spyOn(getAuthorizationService,"checkIfSuperUser").and.returnValue(true);
      spyOn(getAuthorizationService,"checkIfProjectAdmin").and.returnValue(true);
      component.routeForAccess("Default case");
      expect(navigateSpy).not.toHaveBeenCalledWith(['/dashboard/Config/Profile/GrantNewUserAuthRequests']);
    }))

    it("should notification list not null if response is comming",()=>{
      const fakeResponce = {
        message: 'Data came successfully',
        success: true,
        data: [{ count: 2, type: 'User Access Request' }],
      };
      spyOn(httpService,'getAccessRequestsNotifications').and.returnValue(of(fakeResponce));
      component.getNotification();
      expect(component.notificationList).not.toBe(null);
    })

    it("should call message service if notification api is facing any issue",()=>{
      const fakeResponce = {
        message: 'some error occured',
        success: false,
        data: [],
      };
      spyOn(httpService,'getAccessRequestsNotifications').and.returnValue(of(fakeResponce));
      const spy = spyOn(messageService,'add');
      component.getNotification();
      expect(spy).toHaveBeenCalled();
    });

    it('should check if maturity tab is hidden',()=>{
      component.kpiListData = configGlobalData['data'];
      expect(component.checkIfMaturityTabHidden()).toBeFalse();
    });

    it('navigate to dashboard should call navigateToSelectedTab',()=>{
      spyOn(httpService,'getShowHideOnDashboard').and.returnValue(of(configGlobalData));
      spyOn(component,'getNotification');
      spyOn(component,'processKpiList');
      component.kanban=false;
      const navigateToSelectedTabSpy = spyOn(component,'navigateToSelectedTab');
      spyOn(httpService,'getFilterData').and.returnValue(of(fakeFilterData));
      spyOn(sharedService,'getSelectedLevel').and.returnValue({
        "level": 5,
        "hierarchyLevelId": "project",
        "hierarchyLevelName": "Project"
    });
      spyOn(component,'checkIfFilterAlreadySelected');
      spyOn(helperService,'makeSyncShownProjectLevelAndUserLevelKpis')
      spyOn(sharedService,'setDashConfigData')
      component.navigateToDashboard();
      fixture.detectChanges();
      expect(navigateToSelectedTabSpy).toHaveBeenCalled();
    });

    it('should get tooltip data on component load',()=>{
      const fakeResponce = {
        dateRangeFilter: {
          counts: [5, 10, 15],
          types: ['Days', 'Weeks', 'Months']
        },
        hierarchySelectionCount: 3,
        kpiWiseAggregationType: { kpi114: 'sum', kpi997: 'sum', kpi116: 'average', kpi118: 'sum', kpi82: 'average' },
        percentile: 90
      };
      component.selectedTab = "developer";
      spyOn(httpService,"getConfigDetails").and.returnValue(of(fakeResponce));
      component.ngOnInit();
      expect(component.dateRangeFilter).not.toBeNull();
     });

  it('should not make an api call if hierchies alresy available', () => {
    component.hierarchies = [{
      "level": 4,
      "hierarchyLevelId": "project",
      "hierarchyLevelName": "Project"
    }];
    const spy = spyOn(httpService, 'getAllHierarchyLevels');
    component.setHierarchyLevels();
    expect(spy).not.toHaveBeenCalled();
  });

  it('should call applyChagnes on selection of trendValue', () => {
    const spy = spyOn(component, 'applyChanges');
    component.initializeFilterForm();
    component.filterForm?.get('selectedTrendValue').setValue('DOTC_63b51633f33fd2360e9e72bd')
    component.additionalFiltersArr = [{
      "level": 5,
      "hierarchyLevelId": "sprint",
      "hierarchyLevelName": "Sprint"
    }];
    component.onSelectedTrendValueChange(null);
    expect(spy).toHaveBeenCalled();
  });


  it('should check if need to call  default filter for iteration', () => {
    spyOn(sharedService, 'getSelectedLevel').and.returnValue({
      "level": 4,
      "hierarchyLevelId": "project",
      "hierarchyLevelName": "Project"
    });

    spyOn(sharedService, 'getSelectedTrends').and.returnValue([
      {
        "nodeId": "aCjCgoFkxh_64218f1f7b8332581c81169d",
        "nodeName": "aCjCgoFkxh",
        "path": [
          "Level3_hierarchyLevelThree###Level2_hierarchyLevelTwo###Level1_hierarchyLevelOne"
        ],
        "labelName": "project",
        "parentId": [
          "Level3_hierarchyLevelThree"
        ],
        "level": 4,
        "basicProjectConfigId": "64218f1f7b8332581c81169d",
        "additionalFilters": []
      }
    ]);

    component.previousType = true;
    component.selectedTab = 'Iteration';
    let spyDefaultFilter = spyOn(component, 'findProjectWhichHasData');
    component.checkIfFilterAlreadySelected();
    expect(spyDefaultFilter).toHaveBeenCalled();
  });

  it('should check if need to call  default filter for other boards', () => {
    spyOn(sharedService, 'getSelectedLevel').and.returnValue({
      "level": 4,
      "hierarchyLevelId": "project",
      "hierarchyLevelName": "Project"
    });

    spyOn(sharedService, 'getSelectedTrends').and.returnValue([
      {
        "nodeId": "aCjCgoFkxh_64218f1f7b8332581c81169d",
        "nodeName": "aCjCgoFkxh",
        "path": [
          "Level3_hierarchyLevelThree###Level2_hierarchyLevelTwo###Level1_hierarchyLevelOne"
        ],
        "labelName": "project",
        "parentId": [
          "Level3_hierarchyLevelThree"
        ],
        "level": 4,
        "basicProjectConfigId": "64218f1f7b8332581c81169d",
        "additionalFilters": []
      }
    ]);

    component.previousType = true;
    component.kanban= false;
    component.selectedTab = 'my-knowhow';
    let spyDefaultFilter = spyOn(component, 'checkDefaultFilterSelection');
    component.checkIfFilterAlreadySelected();
    expect(spyDefaultFilter).toHaveBeenCalled();
  });

  it('should check if filter already selected for iteration', () => {
    spyOn(sharedService, 'getSelectedLevel').and.returnValue({
      "level": 4,
      "hierarchyLevelId": "project",
      "hierarchyLevelName": "Project"
    });

    spyOn(sharedService, 'getSelectedTrends').and.returnValue([
      {
        "nodeId": "Level1_hierarchyLevelOne",
        "nodeName": "Level1",
        "path": [
          ""
        ],
        "labelName": "hierarchyLevelOne",
        "level": 1,
        "parentId": [
          null
        ],
        "additionalFilters": []
      }
    ]);

    component.previousType = false;
    component.selectedTab = 'Iteration';
    component.initializeFilterForm();
    component.checkIfFilterAlreadySelected();
    expect(component.defaultFilterSelection).toBeFalse();
  });

  it('should check if filter already selected for other board', () => {
    spyOn(sharedService, 'getSelectedLevel').and.returnValue({
      "level": 4,
      "hierarchyLevelId": "project",
      "hierarchyLevelName": "Project"
    });

    spyOn(sharedService, 'getSelectedTrends').and.returnValue([
      {
        "nodeId": "Level1_hierarchyLevelOne",
        "nodeName": "Level1",
        "path": [
          ""
        ],
        "labelName": "hierarchyLevelOne",
        "level": 1,
        "parentId": [
          null
        ],
        "additionalFilters": []
      }
    ]);

    component.previousType = false;
    component.kanban=false;
    component.selectedTab = 'my-knowhow';
    component.initializeFilterForm();
    component.checkIfFilterAlreadySelected();
    expect(component.filterForm.get('selectedTrendValue').value[0]).toEqual('Level1_hierarchyLevelOne');
  });

  it('should navigate To Maturity tab', inject([Router], (router: Router) => {
    component.selectedTab = 'kpi-maturity';
    component.kanban = false;
    component.kpiListData = configGlobalData['data'];
    const spy = spyOn(router, 'navigateByUrl');
    const spyMaturity = spyOn(component,'checkIfMaturityTabHidden').and.returnValue(false);
    component.navigateToSelectedTab();
    expect(spy).toHaveBeenCalledWith('/dashboard/kpi-maturity');
  }));

  it('should not navigate To Maturity tab', inject([Router], (router: Router) => {
    component.selectedTab = 'kpi-maturity';
    component.kanban = false;
    component.kpiListData = configGlobalData['data'];
    const spy = spyOn(router, 'navigateByUrl');
    const spyMaturity = spyOn(component,'checkIfMaturityTabHidden').and.returnValue(true);
    component.navigateToSelectedTab();
    expect(spy).not.toHaveBeenCalledWith('/dashboard/kpi-maturity');
  }));

  it("should get project which have atleast one release",()=>{
    component.additionalFiltersDdn['release'] = releaseList;
    component.selectedProjectData['nodeId'] = "844_DOTC_63b51633f33fd2360e9e72bd";
    component.initializeFilterForm();
    component.checkIfProjectHasRelease();
    expect(component.selectedRelease).not.toBeNull();
  })

  it("should have atlease release for sort",()=>{
    component.additionalFiltersDdn['release'] = [
      {
        parentId : ['844_DOTC_63b51633f33fd2360e9e72bd']
      }
    ];
    component.selectedProjectData['nodeId'] = "844_DOTC_63b51633f33fd2360e9e72bd";
    component.initializeFilterForm();
    component.checkIfProjectHasRelease();
  })

  it("should sort release",()=>{
    component.additionalFiltersDdn['release'] = [{
      labelName: "release",
      level: 5,
      nodeId: "844_844_DOTC_63b51633f33fd2360e9e72bd",
      nodeName: "KnowHow 6.7.0",
      parentId: ["844_DOTC_63b51633f33fd2360e9e72bd"],
      path: "844_DOTC_63b51633f33fd2360e9e72bd###D3_hierarchyLevelThree###D2_hierarchyLevelTwo###D1_hierarchyLevelOne",
      releaseEndDate: "2023-05-14T19:11:00.0000000",
      releaseStartDate: "2023-04-17T19:11:00.0000000",
      releaseState: "unreleased",
    },
    {
      labelName: "release",
      level: 5,
      nodeId: "844_DOTC_63b51633f33fd2360e9e72bd",
      nodeName: "KnowHow 6.8.0",
      parentId: ["844_DOTC_63b51633f33fd2360e9e72bd"],
      path: "844_DOTC_63b51633f33fd2360e9e72bd###D3_hierarchyLevelThree###D2_hierarchyLevelTwo###D1_hierarchyLevelOne",
      releaseEndDate: "2023-04-11T11:52:00.0000000",
      releaseStartDate: "2023-01-04T16:04:03.6900000",
      releaseState: "unreleased",
    }
  ]
    component.selectedProjectData['nodeId'] = "844_DOTC_63b51633f33fd2360e9e72bd";
    component.initializeFilterForm();
    component.checkIfProjectHasRelease();
  })

  it('should filter release when project dropdown value change',()=>{
    const spyFunct = spyOn(component,'checkIfProjectHasData');
    spyOn(component,'createFilterApplyData')
    component.additionalFiltersDdn['release'] = releaseList;
    component.trendLineValueList = [
      {
        nodeId: "DOTC_63b51633f33fd2360e9e72bd",
        nodeName: "KnowHOW|PI_10|ITR_6| 07th Sep_Scrum Project",
        sprintStartDate: "2022-09-07T19:38:00.0000000",
        sprintEndDate: "2022-09-27T22:30:00.0000000",
        path: "Scrum Project_6335363749794a18e8a4479b###Sample Three_hierarchyLevelThree###Sample Two_hierarchyLevelTwo###Sample One_hierarchyLevelOne",
        labelName: "sprint",
        parentId: ["DOTC_63b51633f33fd2360e9e72bd"],
        sprintState: "CLOSED",
        level: 5
    },
    ]
    component.initializeFilterForm();
    component.filterForm?.get('selectedTrendValue').setValue('DOTC_63b51633f33fd2360e9e72bd')
    component.handleIterationFilters('project',true);
    expect(spyFunct).toHaveBeenCalled();
  })

  it('should get date and status of jira processor when status is success',()=>{
    let fakeTraceLog = {
           "id": "6458fcffc5ef9b0a35606e16",
            "processorName": "Jira",
            "basicProjectConfigId": "6458c6685decd920d5eb2edd",
            "executionStartedAt": 1683552962413,
            "executionEndedAt": 1683590549223,
            "executionSuccess": true,
            "lastSuccessfulRun": "2023-05-09 00:02",
    }
    spyOn(component,'findTraceLogForTool').and.returnValue(fakeTraceLog);
    const spyOnFetchData = spyOn(component,'fetchActiveIterationStatus');
    component.showExecutionDate();
    expect(component.selectedProjectLastSyncStatus).toBe('SUCCESS');
    expect(spyOnFetchData).toHaveBeenCalled();
  })

  it('should get date and status of jira processor when status is false',()=>{
    let fakeTraceLog = {
           "id": "6458fcffc5ef9b0a35606e16",
            "processorName": "Jira",
            "basicProjectConfigId": "6458c6685decd920d5eb2edd",
            "executionStartedAt": 1683552962413,
            "executionEndedAt": 1683590549223,
            "executionSuccess": false,
            "lastSuccessfulRun": "2023-05-09 00:02",
    };
    const spyOnFetchData = spyOn(component,'fetchActiveIterationStatus');
    spyOn(component,'findTraceLogForTool').and.returnValue(fakeTraceLog)
    component.showExecutionDate();
    expect(component.selectedProjectLastSyncStatus).toBe("FAILURE");
    expect(spyOnFetchData).toHaveBeenCalled();
  });

  it('should get date and status of jira processor when executionEndedAt is 0 ',()=>{
    let fakeTraceLog = {
           "id": "6458fcffc5ef9b0a35606e16",
            "processorName": "Jira",
            "basicProjectConfigId": "6458c6685decd920d5eb2edd",
            "executionStartedAt": 1683552962413,
            "executionEndedAt": 0,
            "executionSuccess": false,
            "lastSuccessfulRun": "2023-05-09 00:02",
    };
    const spyOnFetchData = spyOn(component,'fetchActiveIterationStatus');
    spyOn(component,'findTraceLogForTool').and.returnValue(fakeTraceLog)
    component.showExecutionDate();
    expect(spyOnFetchData).toHaveBeenCalled();
  });

  it('should get date and status of jira processor when tracklog is null',()=>{
    let fakeTraceLog = null
    const spyOnFetchData = spyOn(component,'fetchActiveIterationStatus');
    spyOn(component,'findTraceLogForTool').and.returnValue(fakeTraceLog)
    component.showExecutionDate();
    expect(spyOnFetchData).toHaveBeenCalled();
  });


  it('should get comment summary', fakeAsync(() => {
    component.showKpisList = [
      {
        kpiId : 'kpi123',
        kpiName : 'name'
      },
      {
        kpiId : 'kpi1234',
        kpiName : 'name2'
      }
    ]
    component.kpiObj = {
      kpi123 : {},
      kpi1234 : {}
    }
    component.showSpinner = true;
    const reqObj = {
        "level": 5,
        "kpiIds": [
            "kpi14",
            "kpi82",
            "kpi111",
            "kpi35",
            "kpi34",
            "kpi37",
            "kpi28",
            "kpi36",
            "kpi126",
            "kpi42",
            "kpi16",
            "kpi17",
            "kpi38",
            "kpi27",
            "kpi116",
            "kpi70",
            "kpi40",
            "kpi72",
            "kpi5",
            "kpi39",
            "kpi46",
            "kpi84",
            "kpi11",
            "kpi8",
            "kpi118",
            "kpi73",
            "kpi113",
            "kpi149"
        ],
        "nodes": [
            "ADDD_649a920fdf3e6c21e3968e30"
        ],
        "nodeChildId": ''
    }
    component.filterApplyData['selectedMap'] = {
      'sprint': [],
      'release': [],
      'project': ['ADDD_649a920fdf3e6c21e3968e30'],
      'level' :3,
      'ids' : ['id1'],
      'selectedMap' : {
        'sprint' : ['s1'],
        'release' : ['r1'],
      }

    }
    component.selectedTab = 'my-knowhow';
    const spy = spyOn(httpService, 'getCommentSummary').and.returnValue(of({success : true,data : 'working'}));

    component.getRecentComments();
    tick();
    expect(spy).toHaveBeenCalled();

  }));

  it('should get comment summary for iterartion', fakeAsync(() => {
    component.showSpinner = true;
    const reqObj = {
        "level": 5,
        "kpiIds": [
            "kpi14",
            "kpi82",
            "kpi111",
            "kpi35",
            "kpi34",
            "kpi37",
            "kpi28",
        ],
        "nodes": [
            "ADDD_649a920fdf3e6c21e3968e30"
        ],
        "nodeChildId": ''
    }
    component.filterApplyData = {
      ids : ['id1'],
      selectedMap : {
        'sprint': [],
        'release': [],
        'project': ['ADDD_649a920fdf3e6c21e3968e30'],
        'level' :3,
        'ids' : ['id1'],
      }
    }
    component.filterData= [{
      nodeId : 'id1'
    }]
    component.selectedTab = 'iteration';
    const spy = spyOn(httpService, 'getCommentSummary').and.returnValue(throwError('Error'));

    component.getRecentComments();
    tick();
    expect(spy).toHaveBeenCalled();

  }));

  it('should handle comment summary button click', () => {
    component.toggleDropdown['commentSummary'] = false;
    const spy = spyOn(component, 'getRecentComments');
    component.handleBtnClick();
    expect(spy).toHaveBeenCalled();
  });

  it('should compile GA data', () => {
    component.selectedFilterArray = fakeSelectedFilterArray;
    const gaArray = [
      {
          "id": "45160_ADDD_649a920fdf3e6c21e3968e30",
          "name": "KnowHOW | PI_14| ITR_1_ADDD",
          "level": "sprint",
          "category1": "A",
          "category2": "Education",
          "category3": "ADEO",
          "category4": "2021 WLP Brand Retainer",
          "category5": "ADDD"
      }
  ]
    expect(component.selectedFilterArray.length).toEqual(gaArray.length);
  });

  it('should update kpi on click of update',()=>{
   const spySelect=  spyOn(sharedService,'select');
    component.onUpdateKPI();
    expect(spySelect).toHaveBeenCalled();
  });

  it('should update selectedProjectLastSyncDate on fetch data success ', fakeAsync(() => {
    component.initializeFilterForm();
    component.filterForm?.get('selectedSprintValue').setValue('43310_ABFZyDaLnk_64942ed8eb73c425e4d7ba8d')
    component.selectedSprint = {
      "nodeId": "43310_ABFZyDaLnk_64942ed8eb73c425e4d7ba8d",
      "nodeName": "KnowHOW | PI_13| ITR_6_ABFZyDaLnk",
      "sprintStartDate": "2023-06-07T11:52:00.0000000",
      "sprintEndDate": "2023-06-27T11:52:00.0000000",
      "sprintState": "ACTIVE",
      "level": 6
    };
    const getActiveIterationStatusSpy= spyOn(httpService, 'getActiveIterationStatus').and.returnValue(of({
      "message": "Got HTTP response: 200 on url: http://localhost:50008/activeIteration/fetch",
      "success": true
    }));

    spyOn(httpService, 'getactiveIterationfetchStatus').and.returnValue(of({
      "message": "Successfully fetched last sync details from db",
      "success": true,
      "data": {
        "id": "64ba0f5f56af7e18da9da925",
        "sprintId": "42842_KnowHOW_6360fefc3fa9e175755f0728",
        "fetchSuccessful": true,
        "errorInFetch": false,
        "lastSyncDateTime": "2023-07-21T10:23:51.845"
      }
    }));

    component.fetchData();
    tick(10000);
    expect(getActiveIterationStatusSpy).toHaveBeenCalled();
    expect(component.selectedProjectLastSyncStatus).toEqual('SUCCESS');
    expect(component.selectedProjectLastSyncDate).toEqual('2023-07-21T10:23:51.845');
  }));

  it('should not update selectedProjectLastSyncDate on fetch data failure ',fakeAsync(()=>{
    component.initializeFilterForm();
    component.filterForm?.get('selectedSprintValue').setValue('43310_ABFZyDaLnk_64942ed8eb73c425e4d7ba8d')
    component.selectedSprint = {
      "nodeId": "43310_ABFZyDaLnk_64942ed8eb73c425e4d7ba8d",
      "nodeName": "KnowHOW | PI_13| ITR_6_ABFZyDaLnk",
      "sprintStartDate": "2023-06-07T11:52:00.0000000",
      "sprintEndDate": "2023-06-27T11:52:00.0000000",
      "sprintState": "ACTIVE",
      "level": 6
    };
    const getActiveIterationStatusSpy= spyOn(httpService, 'getActiveIterationStatus').and.returnValue(of({
      "message": "Got HTTP response: 200 on url: http://localhost:50008/activeIteration/fetch",
      "success": false
    }));

    const getactiveIterationfetchStatusSpy = spyOn(httpService, 'getactiveIterationfetchStatus').and.returnValue(of({
      "message": "Successfully fetched last sync details from db",
      "success": true,
      "data": {
        "id": "64ba0f5f56af7e18da9da925",
        "sprintId": "42842_KnowHOW_6360fefc3fa9e175755f0728",
        "fetchSuccessful": false,
        "errorInFetch": true,
        "lastSyncDateTime": "2023-07-21T10:23:51.845"
      }
    }));

    component.fetchData();
    tick(10000);
    expect(getActiveIterationStatusSpy).toHaveBeenCalled();
    expect(getactiveIterationfetchStatusSpy).not.toHaveBeenCalled();
    expect(Object.keys(component.lastSyncData).length).toEqual(0);
  }));

  it('should get error while fetching active iteration data',fakeAsync(()=>{
    component.initializeFilterForm();
    component.filterForm?.get('selectedSprintValue').setValue('43310_ABFZyDaLnk_64942ed8eb73c425e4d7ba8d')
    component.selectedSprint = {
      "nodeId": "43310_ABFZyDaLnk_64942ed8eb73c425e4d7ba8d",
      "nodeName": "KnowHOW | PI_13| ITR_6_ABFZyDaLnk",
      "sprintStartDate": "2023-06-07T11:52:00.0000000",
      "sprintEndDate": "2023-06-27T11:52:00.0000000",
      "sprintState": "ACTIVE",
      "level": 6
    };
    const getActiveIterationStatusSpy= spyOn(httpService, 'getActiveIterationStatus').and.returnValue(of({
      "message": "Got HTTP response: 200 on url: http://localhost:50008/activeIteration/fetch",
      "success": true
    }));

    spyOn(httpService, 'getactiveIterationfetchStatus').and.returnValue(of({
      "message": "Successfully fetched last sync details from db",
      "success": true,
      "data": {
        "id": "64ba0f5f56af7e18da9da925",
        "sprintId": "42842_KnowHOW_6360fefc3fa9e175755f0728",
        "fetchSuccessful": false,
        "errorInFetch": true,
        "lastSyncDateTime": "2023-07-21T10:23:51.845"
      }
    }));

    component.fetchData();
    tick(10000);
    expect(getActiveIterationStatusSpy).toHaveBeenCalled();
    expect(component.selectedProjectLastSyncStatus).toEqual('FAILURE');
  }));

  it('should get error while fetching active iteration data and getactiveIterationfetchStatus is false itself',fakeAsync(()=>{
    component.initializeFilterForm();
    component.filterForm?.get('selectedSprintValue').setValue('43310_ABFZyDaLnk_64942ed8eb73c425e4d7ba8d')
    component.selectedSprint = {
      "nodeId": "43310_ABFZyDaLnk_64942ed8eb73c425e4d7ba8d",
      "nodeName": "KnowHOW | PI_13| ITR_6_ABFZyDaLnk",
      "sprintStartDate": "2023-06-07T11:52:00.0000000",
      "sprintEndDate": "2023-06-27T11:52:00.0000000",
      "sprintState": "ACTIVE",
      "level": 6
    };
    const getActiveIterationStatusSpy= spyOn(httpService, 'getActiveIterationStatus').and.returnValue(of({
      "message": "Got HTTP response: 200 on url: http://localhost:50008/activeIteration/fetch",
      "success": true
    }));

    spyOn(httpService, 'getactiveIterationfetchStatus').and.returnValue(of({
      "message": "Successfully fetched last sync details from db",
      "success": false,
      "data": {
        "id": "64ba0f5f56af7e18da9da925",
        "sprintId": "42842_KnowHOW_6360fefc3fa9e175755f0728",
        "fetchSuccessful": false,
        "errorInFetch": true,
        "lastSyncDateTime": "2023-07-21T10:23:51.845"
      }
    }));

    component.fetchData();
    tick(10000);
    expect(getActiveIterationStatusSpy).toHaveBeenCalled();
    expect(component.selectedProjectLastSyncStatus).toEqual('');
  }));

  it('should get error while fetching active iteration data and getactiveIterationfetchStatus is throw error',fakeAsync(()=>{
    component.initializeFilterForm();
    component.filterForm?.get('selectedSprintValue').setValue('43310_ABFZyDaLnk_64942ed8eb73c425e4d7ba8d')
    component.selectedSprint = {
      "nodeId": "43310_ABFZyDaLnk_64942ed8eb73c425e4d7ba8d",
      "nodeName": "KnowHOW | PI_13| ITR_6_ABFZyDaLnk",
      "sprintStartDate": "2023-06-07T11:52:00.0000000",
      "sprintEndDate": "2023-06-27T11:52:00.0000000",
      "sprintState": "ACTIVE",
      "level": 6
    };
    const getActiveIterationStatusSpy= spyOn(httpService, 'getActiveIterationStatus').and.returnValue(of({
      "message": "Got HTTP response: 200 on url: http://localhost:50008/activeIteration/fetch",
      "success": true
    }));

    spyOn(httpService, 'getactiveIterationfetchStatus').and.returnValue(throwError('Error'));

    component.fetchData();
    tick(10000);
    expect(getActiveIterationStatusSpy).toHaveBeenCalled();
    expect(component.selectedProjectLastSyncStatus).toEqual('');
  }));

  it('should fetch ActiveIterationStatus success case',()=>{
    component.initializeFilterForm();
    component.filterForm?.get('selectedSprintValue').setValue('43310_ABFZyDaLnk_64942ed8eb73c425e4d7ba8d')
    component.selectedSprint = {
      "nodeId": "43310_ABFZyDaLnk_64942ed8eb73c425e4d7ba8d",
      "nodeName": "KnowHOW | PI_13| ITR_6_ABFZyDaLnk",
      "sprintStartDate": "2023-06-07T11:52:00.0000000",
      "sprintEndDate": "2023-06-27T11:52:00.0000000",
      "sprintState": "ACTIVE",
      "level": 6
    };
    component.selectedProjectLastSyncDate = '2023-06-21T10:23:51.845';
    const getactiveIterationfetchStatusSpy = spyOn(httpService, 'getactiveIterationfetchStatus').and.returnValue(of({
      "message": "Successfully fetched last sync details from db",
      "success": true,
      "data": {
        "id": "64ba0f5f56af7e18da9da925",
        "sprintId": "42842_KnowHOW_6360fefc3fa9e175755f0728",
        "fetchSuccessful": true,
        "errorInFetch": false,
        "lastSyncDateTime": "2023-07-21T10:23:51.845"
      }
    }));
    component.fetchActiveIterationStatus();
    fixture.detectChanges();
    expect(component.selectedProjectLastSyncStatus).toEqual('SUCCESS');

  });


  it('should fetch ActiveIterationStatus fail case',()=>{
    component.initializeFilterForm();
    component.filterForm?.get('selectedSprintValue').setValue('43310_ABFZyDaLnk_64942ed8eb73c425e4d7ba8d')
    component.selectedSprint = {
      "nodeId": "43310_ABFZyDaLnk_64942ed8eb73c425e4d7ba8d",
      "nodeName": "KnowHOW | PI_13| ITR_6_ABFZyDaLnk",
      "sprintStartDate": "2023-06-07T11:52:00.0000000",
      "sprintEndDate": "2023-06-27T11:52:00.0000000",
      "sprintState": "ACTIVE",
      "level": 6
    };

    const getactiveIterationfetchStatusSpy = spyOn(httpService, 'getactiveIterationfetchStatus').and.returnValue(of({
      "message": "Successfully fetched last sync details from db",
      "success": true,
      "data": {
        "id": "64ba0f5f56af7e18da9da925",
        "sprintId": "42842_KnowHOW_6360fefc3fa9e175755f0728",
        "fetchSuccessful": false,
        "errorInFetch": true,
        "lastSyncDateTime": "2023-07-21T10:23:51.845"
      }
    }));
    component.selectedProjectLastSyncDate = '2023-06-21T10:23:51.845';
    component.fetchActiveIterationStatus();
    fixture.detectChanges();
    expect(component.selectedProjectLastSyncStatus).toEqual('FAILURE');

  });

  it('should remove identifier from parent id',()=>{
    component.kanban = false;
    localStorage.setItem('completeHierarchyData', JSON.stringify(completeHierarchyData));
     spyOn(sharedService,'getSelectedLevel').and.returnValue({
        "level": 5,
        "hierarchyLevelId": "project",
        "hierarchyLevelName": "Project"
    });
    const value1 = component.parentIDClean("Demo_port");
    expect(value1).toBe("Demo Portfolio");
  })

  it('should toggle filter', () => {
    const mockTargetElement = document.createElement('div');
    component.toggleDropdown = {
      'showHide': false,
      'commentSummary':  false
    };
    component.toggleDropdownObj = {
      "sprint": false,
      "release": false,
      "sqd": false
    }
    const spy = spyOn(sharedService, 'getClickedItem').and.returnValue(of(mockTargetElement));
    component.toggleFilter();
    expect(spy).toHaveBeenCalled();
  });

  xit('should navigate to home page', () => {
    (router as any).url = '/somepath/Config';
    component.kanban = true;
    component.selectedTab = 'kpi-maturity';
    component.projectIndex = 1;
    spyOn(sharedService, 'setEmptyFilter');
    spyOn(sharedService, 'setSelectedType');
    const spy = spyOn(router, 'navigateByUrl');
    component.navigateToHomePage();
    expect(spy).toHaveBeenCalledWith(`/dashboard/iteration`);
  });

  it('should get kpi order list on project level', () => {
    component.kpiListDataProjectLevel = [];
    component.kpiListData = [];
    component.masterData = fakeMasterData;
    component.filterData = fakeFilterData;
    component.filterApplyData = filterApplyData;
    component.selectedTab = 'backlog';
    const obj = [{
      "nodeId": "Scrum Project_6335363749794a18e8a4479b",
      "nodeName": "Scrum Project",
      "path": "Sample Three_hierarchyLevelThree###Sample Two_hierarchyLevelTwo###Sample One_hierarchyLevelOne",
      "labelName": "project",
      "parentId": "Sample Three_hierarchyLevelThree",
      "level": 4,
      "basicProjectConfigId": "6335363749794a18e8a4479b"
    }]
    const mockData = configGlobalData;
    spyOn(helperService, 'makeSyncShownProjectLevelAndUserLevelKpis');
    spyOn(sharedService, 'getSelectedLevel').and.returnValue({ hierarchyLevelId: 'project' });
    spyOn(sharedService, 'getSelectedTrends').and.returnValue(obj);
    spyOn(sharedService, 'setDashConfigData');
    const spy = spyOn(sharedService, 'select');
    spyOn(sharedService, 'setUpdatedBoardList');
    spyOn(httpService, 'getShowHideOnDashboard').and.returnValue(of(mockData));
    spyOn(component, 'processKpiList');
    spyOn(component, 'navigateToSelectedTab');
    component.getKpiOrderListProjectLevel();
    // expect(spy).toHaveBeenCalledWith(fakeMasterData, fakeFilterData, filterApplyData, component.selectedTab, component.isAdditionalFilter, true, null, true, mockData);
  })

  it('should get kpi order list on project level and return error', () => {
    component.selectedTab = 'backlog';
    const obj = [{
      "nodeId": "Scrum Project_6335363749794a18e8a4479b",
      "nodeName": "Scrum Project",
      "path": "Sample Three_hierarchyLevelThree###Sample Two_hierarchyLevelTwo###Sample One_hierarchyLevelOne",
      "labelName": "project",
      "parentId": "Sample Three_hierarchyLevelThree",
      "level": 4,
      "basicProjectConfigId": "6335363749794a18e8a4479b"
    }]
    spyOn(helperService, 'makeSyncShownProjectLevelAndUserLevelKpis');
    spyOn(sharedService, 'getSelectedLevel').and.returnValue({ hierarchyLevelId: 'project' });
    spyOn(sharedService, 'getSelectedTrends').and.returnValue(obj);
    spyOn(httpService, 'getShowHideOnDashboard').and.returnValue(throwError('Something went wrong'));
    const spy = spyOn(messageService, 'add');
    component.getKpiOrderListProjectLevel();
    expect(spy).toHaveBeenCalled()
  })

  it('should handle kpi change if all kpis are enabled', () => {
    const event = {
      "originalEvent": {
          "isTrusted": true
      },
      "checked": true
    }
    const kpiObj = {
      kpi75: new UntypedFormControl(true)
    }
    component.kpiForm = new UntypedFormGroup({
      enableAllKpis: new UntypedFormControl(false),
      kpis: new UntypedFormGroup(kpiObj),
    });
    component.handleKpiChange(event);
    expect(component.kpiFormValue['enableAllKpis'].value).toBeTruthy;
  });

  it('should handle kpi change- if atleast one kpi is disabled', () => {
    const event = {
      "originalEvent": {
          "isTrusted": true
      },
      "checked": true
    }
    const kpiObj = {
      kpi75: new UntypedFormControl(true),
      kpi39: new UntypedFormControl(false)
    }
    component.kpiForm = new UntypedFormGroup({
      enableAllKpis: new UntypedFormControl(false),
      kpis: new UntypedFormGroup(kpiObj),
    });
    component.handleKpiChange(event);
    expect(component.kpiFormValue['enableAllKpis'].value).toBeFalsy;
  });

  it('should set trend value filter when allowMultipleSelection is true', () => {
    component.allowMultipleSelection = true;
    component.filterForm = new UntypedFormGroup({
      selectedTrendValue: new UntypedFormControl(''),
      date: new UntypedFormControl(''),
      selectedLevel: new UntypedFormControl(),
      selectedSprintValue: new UntypedFormControl(),
      selectedRelease: new UntypedFormControl(),
    });
    const nodeId = 'AAA_8327462874264dsd34';
    component.trendLineValueList = [
      {
        'nodeId': nodeId
      }
    ]
    component.setTrendValueFilter();
    expect(component.filterForm['controls']['selectedTrendValue'].value).toEqual([nodeId])
  })

  it('should set trend value filter when allowMultipleSelection is false', () => {
    component.allowMultipleSelection = false;
    component.filterForm = new UntypedFormGroup({
      selectedTrendValue: new UntypedFormControl(''),
      date: new UntypedFormControl(''),
      selectedLevel: new UntypedFormControl(),
      selectedSprintValue: new UntypedFormControl(),
      selectedRelease: new UntypedFormControl(),
    });
    const nodeId = 'AAA_8327462874264dsd34';
    component.trendLineValueList = [
      {
        'nodeId': nodeId
      }
    ]
    component.setTrendValueFilter();
    expect(component.filterForm['controls']['selectedTrendValue'].value).toEqual(nodeId)
  })

  it('should find project which has data when selectedTab is release', () => {
    component.defaultFilterSelection = true;
    component.trendLineValueList = [];
    component.filterForm = new UntypedFormGroup({
      selectedTrendValue: new UntypedFormControl(''),
      date: new UntypedFormControl(''),
      selectedLevel: new UntypedFormControl('project'),
      selectedSprintValue: new UntypedFormControl(),
      selectedRelease: new UntypedFormControl(),
    });
    component.filterData = fakeFilterData.data;
    component.selectedTab = 'release';
    component.hierarchyLevels = hierarchyLevels;
    spyOn(component, 'sortAlphabetically').and.callThrough();
    spyOn(helperService, 'makeUniqueArrayList').and.callThrough();
    spyOn(component, 'checkIfProjectHasRelease');
    const len = component.trendLineValueList.length;
    spyOn(sharedService, 'setSelectedLevel');
    spyOn(sharedService, 'setSelectedTrends')
    component.findProjectWhichHasData();
    expect(component.filterForm['controls']['selectedTrendValue'].value).toEqual(component.trendLineValueList[len]?.nodeId);
  });

  it('should find project which has data when selectedTab is not release', () => {
    component.defaultFilterSelection = true;
    component.trendLineValueList = [];
    component.filterForm = new UntypedFormGroup({
      selectedTrendValue: new UntypedFormControl(''),
      date: new UntypedFormControl(''),
      selectedLevel: new UntypedFormControl('project'),
      selectedSprintValue: new UntypedFormControl(),
      selectedRelease: new UntypedFormControl(),
    });
    component.filterData = fakeFilterData.data;
    component.hierarchyLevels = hierarchyLevels;
    component.selectedTab = 'backlog';
    spyOn(component, 'sortAlphabetically').and.callThrough();
    spyOn(helperService, 'makeUniqueArrayList').and.callThrough();
    spyOn(component, 'checkIfProjectHasData');
    const len = component.trendLineValueList.length;
    spyOn(sharedService, 'setSelectedLevel');
    spyOn(sharedService, 'setSelectedTrends')
    component.findProjectWhichHasData();
    expect(component.filterForm['controls']['selectedTrendValue'].value).toEqual(component.trendLineValueList[len]?.nodeId);
  });

  it('should get logo image', () => {
    const logoImage = {
      image: 'logo.png'
    }
    spyOn(httpService, 'getUploadedImage').and.returnValue(of(logoImage));
    component.getLogoImage();
    expect(component.logoImage).toBe("data:image/png;base64," + logoImage.image);
  })

  it('should not get logo image', () => {
    spyOn(httpService, 'getUploadedImage').and.returnValue(of({}));
    component.getLogoImage();
    expect(component.logoImage).toBe(undefined);
  })

  it('should navigate to Capacity Planning page', () => {
    const spy = spyOn(router, 'navigateByUrl');

    // Act
    component.redirectToCapacityPlanning();

    // Assert
    expect(spy).toBeDefined();
  });

  it('should get user name form current user details',()=>{
    component.ngOnInit();
    sharedService.setCurrentUserDetails({user_name : "test user"})
     expect(component.username).toBe("test user");
  })

  it('should initialitze info',()=>{
    spyOn(sharedService,'getCurrentUserDetails').and.returnValue(['ROLE_GUEST'])
    component.initializeUserInfo();
    expect(component.isGuest).toBeTruthy();
  })

  it('should unsubscribe filterKpiRequest', () => {
    spyOn(sharedService, 'getFilterData').and.returnValue(fakeFilterData);
    spyOn(httpService,'getFilterData').and.returnValue(of(fakeFilterData));
    component.previousType = false;
    component.kanban = false;
    component.selectedTab = '';
    const filterKpiRequest = jasmine.createSpyObj('Subscription', ['unsubscribe']);
    component.filterKpiRequest = filterKpiRequest
    component.initFlag = false;
    const spy = spyOn(component, 'processFilterData');
    component.getFilterDataOnLoad();
    fixture.detectChanges();
    expect(spy).toHaveBeenCalled();
  });

  it('should return the latest passed release', () => {
    const releaseList = [
      { releaseEndDate: '2021-08-01T00:00:00.000Z' },
      { releaseEndDate: '2021-07-01T00:00:00.000Z' },
      { releaseEndDate: '2021-09-01T00:00:00.000Z' },
    ];
    const result = component.findLatestPassedRelease(releaseList);
    expect(result.length).toEqual(3);
  });

  it('should return null if releaseList is empty', () => {
    const releaseList = [];
    const result = component.findLatestPassedRelease(releaseList);
    expect(result).toBeNull();
  });

  describe('YourComponent', () => {
    beforeEach(() => {
      component.trendLineValueList = [
        { nodeId: 1, nodeName: 'Node1' },
        { nodeId: 2, nodeName: 'Node2' },
        { nodeId: 3, nodeName: 'Node3' },
      ];
    });

    it('should return the node name for the given nodeId', () => {
      const nodeId = 2;
      const result = component.getNodeName(nodeId);
      expect(result).toEqual('Node2');
    });

    it('should return undefined if the nodeId is not found', () => {
      const nodeId = 4;
      const result = component.getNodeName(nodeId);
      expect(result).toBeUndefined();
    });
  });

  describe('YourComponent', () => {

    beforeEach(() => {
      component.selectedFilterArray = [
        {
          additionalFilters: [
            {
              path: ['1###2###3'],
              nodeName: 'Node3',
              nodeId: '3',
              labelName: 'Label3'
            }
          ],
          path: [],
          nodeId: '1',
          nodeName: 'Node1',
          labelName: 'Label1'
        },
        {
          additionalFilters: [],
          path: ['4###5###6'],
          nodeId: '4',
          nodeName: 'Node4',
          labelName: 'Label4'
        }
      ];
      component.filterData = [
        { nodeId: '1', nodeName: 'Node1' },
        { nodeId: '2', nodeName: 'Node2' },
        { nodeId: '3', nodeName: 'Node3' },
        { nodeId: '4', nodeName: 'Node4' },
        { nodeId: '5', nodeName: 'Node5' },
        { nodeId: '6', nodeName: 'Node6' }
      ];
    });

    it('should compile GA data with additional filters', () => {
      const spyob = spyOn(ga, 'setProjectData');
      component.compileGAData();
      expect(spyob).toHaveBeenCalled();
    });

    it('should compile GA data without additional filters', () => {
      const spyob = spyOn(ga, 'setProjectData');
      component.compileGAData();
      expect(spyob).toHaveBeenCalled();
    });
  });

  describe('YourComponent', () => {

    beforeEach(() => {
      component.processorName = ['tool1'];
      const processorsTracelogs = [
        { processorName: 'tool1', traceLog: 'Trace log for tool1' },
        { processorName: 'tool2', traceLog: 'Trace log for tool2' },
        { processorName: 'tool3', traceLog: 'Trace log for tool3' }
      ];
      component.service.setProcessorLogDetails(processorsTracelogs)
    });

    it('should return the trace log for the tool if it exists in processorsTracelogs', () => {
      const result = component.findTraceLogForTool();
      expect(result).toEqual({ processorName: 'tool1', traceLog: 'Trace log for tool1' });
    });
  });

  describe('YourComponent', () => {

    beforeEach(() => {
      component.selectedFilterArray = [
        {
          additionalFilters: [
            {
              path: ['1###2###3'],
              nodeName: 'Node3',
              nodeId: '3',
              labelName: 'Label3'
            }
          ],
          path: [],
          nodeId: '1',
          nodeName: 'Node1',
          labelName: 'Label1'
        },
        {
          additionalFilters: [],
          path: ['4###5###6'],
          nodeId: '4',
          nodeName: 'Node4',
          labelName: 'Label4'
        }
      ];
      component.filterData = [
        { nodeId: '1', nodeName: 'Node1' },
        { nodeId: '2', nodeName: 'Node2' },
        { nodeId: '3', nodeName: 'Node3' },
        { nodeId: '4', nodeName: 'Node4' },
        { nodeId: '5', nodeName: 'Node5' },
        { nodeId: '6', nodeName: 'Node6' }
      ];
    });

    it('should compile GA data with additional filters', () => {
      const spyob = spyOn(ga, 'setProjectData');
      component.compileGAData();
      expect(spyob).toHaveBeenCalled();
    });

    it('should compile GA data without additional filters', () => {
      const spyob = spyOn(ga, 'setProjectData');
      component.compileGAData();
      expect(spyob).toHaveBeenCalled();
    });
  });

  it('should set sprint list in service layer',()=>{
    component.selectedTab = 'value'
      component.selectedFilterArray = [{
        nodeId : 'a123',
        additionalFilters : [{
          nodeiId : 'sp123'
        }]
      }]
      const spyObj = spyOn(sharedService,'setAddtionalFilterBackup');
      component.setSelectedSprintOnServiceLayer('sqd');
      expect(spyObj).toHaveBeenCalled();
    })

    it('should get backup sprints',()=>{
      component.selectedTab = 'speed';
      sharedService.setAddtionalFilterBackup({
        sprint : {
          'p1' : [
            {nodeId : 'sp1',}
          ]
        }
      })
      spyOn(sharedService,'getSelectedTrends').and.returnValue([{nodeId : 'p1'}])
      const rValue = component.getSprintsWhichWasAlreadySelected('sprint');
      expect(rValue).not.toBeNull();
    })

    it('should set not blank kpi filter for release and sprint when changing',()=>{
      component.selectedTab = 'iteration';
      const mockObj ={
        kpiFilters : {
          iteration : {},
          release : {},
        }
      }
      spyOn(sharedService,'getAddtionalFilterBackup').and.returnValue(mockObj);
      spyOn(sharedService, 'setAddtionalFilterBackup')
      const spyobj =  spyOn(sharedService, 'setKpiSubFilterObj')
        component.refreshKpiLevelFiltersBackup('sprint',true);
        expect(spyobj).toHaveBeenCalled();
      })

      it('should set blank kpi filter for backlog component',()=>{
        component.selectedTab = 'backlog';
        const mockObj ={
          kpiFilters : {
            backlog : {}
          }
        }
        spyOn(sharedService,'getAddtionalFilterBackup').and.returnValue(mockObj);
        const spyobj = spyOn(sharedService, 'setAddtionalFilterBackup')
        spyOn(sharedService, 'setKpiSubFilterObj')

          component.refreshKpiLevelFiltersBackup('project',true);
          expect(spyobj).toHaveBeenCalled();
        })

        it('should close dropdown',()=>{
          component.toggleDropdownObj = {
            abc : true,
            bac : false
          } 
          component.closeAllDropdowns();
        })
    
        xit('should set selectedTab to the first boardName if selectedTab does not exist in kanban or scrum', () => {
          // Arrange
          component.kpiListData = {
            kanban: [{ boardName: 'Board 1', kpis: [] }],
            scrum: [],
            others: []
          };
          component.kanban = true;
          component.selectedTab = 'Non-existent Board';
          spyOn(sharedService, 'setSelectedTab');
          spyOn(component.router, 'navigate');
          // Act
          component.changeSelectedTab();
    
          // Assert
          expect(component.selectedTab).toBe('Board 1');
        });

        it('should filter sprint addtional filter',()=>{
          component.filterForm = new UntypedFormGroup({
            sprintSearch : new UntypedFormControl('abc')
          });
          component.filteredAddFilters = {
            sprint : [
              {nodeName : "sp2"},
              {nodeName : "sp1"}
            ]
          }
          component.applySearchFilter('sprint')
        })


        it('should handle milestone filters for project', () => {
          component.filterForm = new UntypedFormGroup({
            selectedTrendValue: new UntypedFormControl('DOTC_63b51633f33fd2360e9e72bd'),
            selectedRelease: new UntypedFormControl('40201_HvyVrzlpld_63b81ef5224e7b4d03186dab')
          });
          component.selectedFilterArray = [];
          component.trendLineValueList = [{nodeId:'DOTC_63b51633f33fd2360e9e72bd', basicProjectConfigId: '63284960fdd20276d60e4df5'}];
          component.additionalFiltersDdn = {
            release : [{
              labelName: 'release',
              level: 6,
              nodeId: '40201_HvyVrzlpld_63b81ef5224e7b4d03186dab',
              nodeName: 'DTS | KnowHOW | PI_11| ITR_4_HvyVrzlpld',
              parentId: ['DOTC_63b51633f33fd2360e9e72bd'],
              path: [
                'HvyVrzlpld_63b81ef5224e7b4d03186dab###Level3_hiera…vel2_hierarchyLevelTwo###Level1_hierarchyLevelOne',
              ],
              sprintEndDate: '2022-11-23T10:20:00.0000000',
              sprintStartDate: '2022-11-09T10:20:00.0000000',
              sprintState: 'active',
            }]
          };
          const spy = spyOn(component, 'getProcessorsTraceLogsForProject');
          spyOn(component, 'emptyIdsFromQueryParam');
          spyOn(component, 'refreshKpiLevelFiltersBackup');
          spyOn(sharedService, 'setNoSprints');
          spyOn(component, 'createFilterApplyData');
          component.handleMilestoneFilter('project',true);
          expect(spy).toHaveBeenCalled();
        });

        it("should set project for sqd addtion filters",()=>{
          component.selectedTab = 'iteration';
          component.selectedFilterArray = [
            {
                grossMaturity: 'Maturity Score : NA',
                labelName: 'sprint',
                level: 5,
                nodeId: '844_DOTC_63b51633f33fd2360e9e72bd',
                nodeName: 'MA_Sprint 23.01_DOTC',
                parentId: ['DOTC_63b51633f33fd2360e9e72bd'],
                path: [
                  'DOTC_63b51633f33fd2360e9e72bd###D3_hierarchyLevelThree###D2_hierarchyLevelTwo###D1_hierarchyLevelOne',
                ],
                sprintEndDate: '2023-01-17T22:00:00.0000000',
                sprintStartDate: '2023-01-04T16:04:03.6900000',
                sprintState: 'ACTIVE',
                additionalFilters : [{
                labelName: 'sqd',
                level: 6,
                nodeId: '844_DOTC_63b51633f33fd2360e9e72bd',
                nodeName: 'MA_Sprint 23.01_DOTC',
                parentId: ['DOTC_63b51633f33fd2360e9e72bd'],
                path: [
                  'DOTC_63b51633f33fd2360e9e72bd###D3_hierarchyLevelThree###D2_hierarchyLevelTwo###D1_hierarchyLevelOne',
                ],
                }]
            },
          ];
           component.filterApplyData = filterApplyData;
          spyOn(component,"resetFilterApplyObj");
          spyOn(component,"compileGAData");
          component.createFilterApplyData();
          expect(component.filterApplyData['selectedMap']['sprint'].length).toBeGreaterThan(0)
        })

      it("should navigate to home",()=>{
        spyOnProperty(router, 'url', 'get').and.returnValue('/Help/Config');
        spyOn(router,'navigate')
        spyOn(sharedService,'setEmptyFilter')
        spyOn(sharedService,'setSelectedType')
        const spyob = spyOn(component,'changeSelectedTab')
        component.navigateToHomePage()
        expect(spyob).toHaveBeenCalled();
      })

      it('should redirect on login page',inject([Router], fakeAsync((router: Router) => {
        component.loader = false;
        const res = {
          success: true,
          message: 'Logged out successfully'
        }
        spyOn(httpService, 'logout').and.returnValue(of(res));
        environment['AUTHENTICATION_SERVICE'] = false;
        component.service['isKanban'] = false;
        spyOn(sharedService, 'setSelectedProject');
        spyOn(sharedService, 'setCurrentUserDetails');
        spyOn(sharedService, 'setVisibleSideBar');
        spyOn(sharedService, 'setAddtionalFilterBackup');
        spyOn(sharedService, 'setKpiSubFilterObj');
        const navigateSpy = spyOn(router, 'navigate');
        component.logout();
        tick();
        expect(navigateSpy).toHaveBeenCalledWith(['./authentication/login']);
      })));

});