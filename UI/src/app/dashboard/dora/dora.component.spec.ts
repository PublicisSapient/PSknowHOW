import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';

import { DoraComponent } from './dora.component';
import { SharedService } from '../../services/shared.service';
import { HttpService } from 'src/app/services/http.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { APP_CONFIG, AppConfig } from 'src/app/services/app.config';
import { HelperService } from 'src/app/services/helper.service';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { DatePipe } from '@angular/common';
import { of } from 'rxjs';

describe('DoraComponent', () => {
  let component: DoraComponent;
  let fixture: ComponentFixture<DoraComponent>;
  let service: SharedService;
  let httpService: HttpService;
  let helperService: HelperService;
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
  const globalData =require('../../../test/resource/fakeGlobalConfigData.json');
  const hierarchyData = [
    {
      level: 1,
      hierarchyLevelId: 'corporate',
      hierarchyLevelName: 'Corporate Name',
      suggestions: [
        {
          name: 'C1',
          code: 'C1'
        },
        {
          name: 'Corpate1',
          code: 'Corpate1'
        },
        {
          name: 'Leve1',
          code: 'Leve1'
        },
        {
          name: 'Org1',
          code: 'Org1'
        },
        {
          name: 'Orgc',
          code: 'Orgc'
        },
        {
          name: 'TESTS',
          code: 'TESTS'
        },
        {
          name: 'Test1',
          code: 'Test1'
        },
        {
          name: 'TestC',
          code: 'TestC'
        },
        {
          name: 'TestCorp',
          code: 'TestCorp'
        },
        {
          name: 'abcv',
          code: 'abcv'
        },
        {
          name: 'bittest',
          code: 'bittest'
        },
        {
          name: 'dfdsg',
          code: 'dfdsg'
        },
        {
          name: 'dgdfhfgjgh',
          code: 'dgdfhfgjgh'
        },
        {
          name: 'dgfdh',
          code: 'dgfdh'
        },
        {
          name: 'dgfg',
          code: 'dgfg'
        },
        {
          name: 'dghhjjh',
          code: 'dghhjjh'
        },
        {
          name: 'djfyyyyyyyyyyyyyyy',
          code: 'djfyyyyyyyyyyyyyyy'
        },
        {
          name: 'dsgfdj',
          code: 'dsgfdj'
        },
        {
          name: 'fghhhj',
          code: 'fghhhj'
        },
        {
          name: 'fhgkl',
          code: 'fhgkl'
        },
        {
          name: 'fhjjjjjj',
          code: 'fhjjjjjj'
        },
        {
          name: 'gfhygjhk',
          code: 'gfhygjhk'
        },
        {
          name: 'ghhjhkjl',
          code: 'ghhjhkjl'
        },
        {
          name: 'ghjk',
          code: 'ghjk'
        },
        {
          name: 'gjhfkjhkj',
          code: 'gjhfkjhkj'
        },
        {
          name: 'gjhjkk',
          code: 'gjhjkk'
        },
        {
          name: 'gjkjllf',
          code: 'gjkjllf'
        },
        {
          name: 'gjtykghk',
          code: 'gjtykghk'
        },
        {
          name: 'hgjhgjk',
          code: 'hgjhgjk'
        },
        {
          name: 'hjkk',
          code: 'hjkk'
        },
        {
          name: 'rduuuuuuuuu',
          code: 'rduuuuuuuuu'
        },
        {
          name: 'trrrrrrrrrrrrrrrrrrr',
          code: 'trrrrrrrrrrrrrrrrrrr'
        },
        {
          name: 'trt',
          code: 'trt'
        },
        {
          name: 'wdddddd',
          code: 'wdddddd'
        },
        {
          name: 'ytttttttttttt',
          code: 'ytttttttttttt'
        }
      ],
      value: '',
      required: true
    },
    {
      level: 2,
      hierarchyLevelId: 'business',
      hierarchyLevelName: 'Business Name',
      suggestions: [
        {
          name: 'B1',
          code: 'B1'
        },
        {
          name: 'Bus1',
          code: 'Bus1'
        },
        {
          name: 'Leve2',
          code: 'Leve2'
        },
        {
          name: 'Org2',
          code: 'Org2'
        },
        {
          name: 'Orgb',
          code: 'Orgb'
        },
        {
          name: 'Test2',
          code: 'Test2'
        },
        {
          name: 'TestB',
          code: 'TestB'
        },
        {
          name: 'TestBus',
          code: 'TestBus'
        },
        {
          name: 'asfd',
          code: 'asfd'
        },
        {
          name: 'dcccccccccc',
          code: 'dcccccccccc'
        },
        {
          name: 'ddddddddddddd',
          code: 'ddddddddddddd'
        },
        {
          name: 'dfhhhhhhh',
          code: 'dfhhhhhhh'
        },
        {
          name: 'erhjjkkjkl',
          code: 'erhjjkkjkl'
        },
        {
          name: 'fbcncvn',
          code: 'fbcncvn'
        },
        {
          name: 'fdjfjk',
          code: 'fdjfjk'
        },
        {
          name: 'fgdsfgdh',
          code: 'fgdsfgdh'
        },
        {
          name: 'fhgjhkjk',
          code: 'fhgjhkjk'
        },
        {
          name: 'ggggggg',
          code: 'ggggggg'
        },
        {
          name: 'ghjjk',
          code: 'ghjjk'
        },
        {
          name: 'hjjhjk',
          code: 'hjjhjk'
        },
        {
          name: 'hjuy',
          code: 'hjuy'
        },
        {
          name: 'jhbjnk',
          code: 'jhbjnk'
        },
        {
          name: 'jhkjljkll',
          code: 'jhkjljkll'
        },
        {
          name: 'jhlkl',
          code: 'jhlkl'
        },
        {
          name: 'jnkmlkm',
          code: 'jnkmlkm'
        },
        {
          name: 'knj,n,m m,',
          code: 'knj,n,m m,'
        },
        {
          name: 'rrrrrrrrr',
          code: 'rrrrrrrrr'
        },
        {
          name: 'rrrrrrrrre',
          code: 'rrrrrrrrre'
        },
        {
          name: 'rytrujjjk',
          code: 'rytrujjjk'
        },
        {
          name: 'ryyhtfjghk',
          code: 'ryyhtfjghk'
        },
        {
          name: 'sdddddddddddd',
          code: 'sdddddddddddd'
        },
        {
          name: 'sgdgf',
          code: 'sgdgf'
        },
        {
          name: 't1',
          code: 't1'
        },
        {
          name: 'ttttttuiiiiiiii',
          code: 'ttttttuiiiiiiii'
        },
        {
          name: 'wAAAAAAAAAA',
          code: 'wAAAAAAAAAA'
        }
      ],
      value: '',
      required: true
    },
    {
      level: 3,
      hierarchyLevelId: 'dummyaccount',
      hierarchyLevelName: 'dummyAccount Name',
      suggestions: [
        {
          name: 'A1',
          code: 'A1'
        },
        {
          name: 'Acc1',
          code: 'Acc1'
        },
        {
          name: 'Level3',
          code: 'Level3'
        },
        {
          name: 'Org3',
          code: 'Org3'
        },
        {
          name: 'Orga',
          code: 'Orga'
        },
        {
          name: 'Test3',
          code: 'Test3'
        },
        {
          name: 'TestAcc',
          code: 'TestAcc'
        },
        {
          name: 'TestC',
          code: 'TestC'
        },
        {
          name: 'WRRRRRRRRR',
          code: 'WRRRRRRRRR'
        },
        {
          name: 'bxccnbcvn',
          code: 'bxccnbcvn'
        },
        {
          name: 'ddddddddddddddddd',
          code: 'ddddddddddddddddd'
        },
        {
          name: 'dddst',
          code: 'dddst'
        },
        {
          name: 'dfdgfdh',
          code: 'dfdgfdh'
        },
        {
          name: 'dfsgdf',
          code: 'dfsgdf'
        },
        {
          name: 'eeeee',
          code: 'eeeee'
        },
        {
          name: 'erttyyuui',
          code: 'erttyyuui'
        },
        {
          name: 'fdddddddddddddddd',
          code: 'fdddddddddddddddd'
        },
        {
          name: 'gjhkjjl',
          code: 'gjhkjjl'
        },
        {
          name: 'gsdddddddddddg',
          code: 'gsdddddddddddg'
        },
        {
          name: 'hjl',
          code: 'hjl'
        },
        {
          name: 'hkjkjlkl',
          code: 'hkjkjlkl'
        },
        {
          name: 'hyjykjl',
          code: 'hyjykjl'
        },
        {
          name: 'jhjkhkk',
          code: 'jhjkhkk'
        },
        {
          name: 'jj,ddddw',
          code: 'jj,ddddw'
        },
        {
          name: 'jjkjkjhk',
          code: 'jjkjkjhk'
        },
        {
          name: 'kmmmk',
          code: 'kmmmk'
        },
        {
          name: 'mn',
          code: 'mn'
        },
        {
          name: 'shhhhhhhhh',
          code: 'shhhhhhhhh'
        },
        {
          name: 'sss',
          code: 'sss'
        },
        {
          name: 'ssssssssssss',
          code: 'ssssssssssss'
        },
        {
          name: 't2',
          code: 't2'
        },
        {
          name: 'tyui',
          code: 'tyui'
        },
        {
          name: 'wwgt',
          code: 'wwgt'
        },
        {
          name: 'xfnnnnnnnnn',
          code: 'xfnnnnnnnnn'
        },
        {
          name: 'yutruityi',
          code: 'yutruityi'
        }
      ],
      value: '',
      required: true
    },
    {
      level: 4,
      hierarchyLevelId: 'dummysubaccount',
      hierarchyLevelName: 'dummySubaccount',
      suggestions: [
        {
          name: 'Level4',
          code: 'Level4'
        },
        {
          name: 'Org4',
          code: 'Org4'
        },
        {
          name: 'Orgs',
          code: 'Orgs'
        },
        {
          name: 'S1',
          code: 'S1'
        },
        {
          name: 'Sub1',
          code: 'Sub1'
        },
        {
          name: 'Test4',
          code: 'Test4'
        },
        {
          name: 'TestS',
          code: 'TestS'
        },
        {
          name: 'Testsub',
          code: 'Testsub'
        },
        {
          name: 'aaaaaaaaaaaaaaaaa',
          code: 'aaaaaaaaaaaaaaaaa'
        },
        {
          name: 'asc',
          code: 'asc'
        },
        {
          name: 'cbvcxcncvn',
          code: 'cbvcxcncvn'
        },
        {
          name: 'eeeeeeeeee',
          code: 'eeeeeeeeee'
        },
        {
          name: 'eeeeeeeeeeee',
          code: 'eeeeeeeeeeee'
        },
        {
          name: 'erweteryu',
          code: 'erweteryu'
        },
        {
          name: 'ffff',
          code: 'ffff'
        },
        {
          name: 'fhfd',
          code: 'fhfd'
        },
        {
          name: 'fhgjhk',
          code: 'fhgjhk'
        },
        {
          name: 'fhjkk',
          code: 'fhjkk'
        },
        {
          name: 'ghthhhhhhhhhht',
          code: 'ghthhhhhhhhhht'
        },
        {
          name: 'hjkhkjk',
          code: 'hjkhkjk'
        },
        {
          name: 'hkkkkkk',
          code: 'hkkkkkk'
        },
        {
          name: 'jhhjkjhkj',
          code: 'jhhjkjhkj'
        },
        {
          name: 'jhhvgvggv',
          code: 'jhhvgvggv'
        },
        {
          name: 'jkjkllk;k;',
          code: 'jkjkllk;k;'
        },
        {
          name: 'kmkkkk',
          code: 'kmkkkk'
        },
        {
          name: 'saaaaaaaaaaaaa',
          code: 'saaaaaaaaaaaaa'
        },
        {
          name: 'sasdfdgfgf',
          code: 'sasdfdgfgf'
        },
        {
          name: 'sdf',
          code: 'sdf'
        },
        {
          name: 'sdgggggggg',
          code: 'sdgggggggg'
        },
        {
          name: 'seeeeee',
          code: 'seeeeee'
        },
        {
          name: 'sytttu',
          code: 'sytttu'
        },
        {
          name: 't3',
          code: 't3'
        },
        {
          name: 'xgggggggggg',
          code: 'xgggggggggg'
        },
        {
          name: 'zfghg',
          code: 'zfghg'
        },
        {
          name: 'zzzzzzzzzzzzzzzzzzf',
          code: 'zzzzzzzzzzzzzzzzzzf'
        }
      ],
      value: '',
      required: true
    }
  ];

  beforeEach(async () => {
    service = new SharedService();
    await TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule
      ],
      declarations: [ DoraComponent ],
      providers: [
        HelperService,
        HttpService,
        { provide: APP_CONFIG, useValue: AppConfig },
        { provide: SharedService, useValue: service },
        DatePipe
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DoraComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should process kpi config Data', () => {
    component.configGlobalData = configGlobalData;
    component.processKpiConfigData();
    expect(component.noKpis).toBeFalse();
    component.configGlobalData[0]['isEnabled'] = false;
    component.configGlobalData[0]['shown'] = false;
    component.processKpiConfigData();
    expect(component.noKpis).toBeTrue();
  });

  it('should make post call when kpi available for Jenkins for Scrum', () => {
    const kpiListJenkins = [{
      id: '6332dd4b82451128f9939a29',
      kpiId: 'kpi17',
      kpiName: 'Unit Test Coverage'
    }];
    const spy = spyOn(helperService, 'groupKpiFromMaster').and.returnValue({ kpiList: kpiListJenkins });
    const postJenkinsSpy = spyOn(component, 'postJenkinsKpi');
    component.groupJenkinsKpi(['kpi17']);
    expect(postJenkinsSpy).toHaveBeenCalled();
  });

  it('should check if kpi exists', () => {
    component.allKpiArray = [{
      kpiId: 'kpi118'
    }];
    const result = component.ifKpiExist('kpi118');
    expect(result).toEqual(0);
  });

  it('should refresh values onTypeRefresh', () => {
    spyOn(service, 'getSelectedType');
    spyOn(service, 'getDashConfigData').and.returnValue(globalData['data']);
    const spy = spyOn(component, 'processKpiConfigData');
    service.onTypeOrTabRefresh.next({ selectedTab: 'Dora', selectedType: 'Scrum' });
    component.kanbanActivated = false;
    fixture.detectChanges();
    expect(component.selectedtype).toBe('Dora');
  });

  it('should set noTabAccess to true when no filterData', () => {
    spyOn(service, 'getDashConfigData').and.returnValue(globalData['data']);
    component.kanbanActivated = false;
    component.filterApplyData = {};
    const event = {
      masterData: {
        kpiList: [{
          id: '633ed17f2c2d5abef2451fd8',
          kpiId: 'kpi14',
          kpiName: 'Defect Injection Rate',
          isDeleted: 'False',
          defaultOrder: 1,
          kpiUnit: '%',
          chartType: 'line',
          showTrend: true,
          isPositiveTrend: false,
          calculateMaturity: true,
          kpiSource: 'Jira',
          maxValue: '200',
          thresholdValue: 10,
          kanban: false,
          groupId: 2,
          kpiInfo: {},
          aggregationCriteria: 'average',
          trendCalculative: false,
          additionalFilterSupport: true,
          xaxisLabel: 'Sprints',
          yaxisLabel: 'Percentage'
        }]
      },
      filterData: [],
      filterApplyData: {
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
      },
      selectedTab: 'My Test1',
      isAdditionalFilters: false
    };
    component.receiveSharedData(event);
    expect(component.noTabAccess).toBe(true);

  });

  it('should call grouping kpi functions when filterdata is available', () => {
    spyOn(service, 'getDashConfigData').and.returnValue(globalData['data']);
    component.filterApplyData = {};
    const event = {
      masterData: {
        kpiList: [
          {
            id: '633ed17f2c2d5abef2451fd8',
            kpiId: 'kpi14',
            kpiName: 'Defect Injection Rate',
            isDeleted: 'False',
            defaultOrder: 1,
            kpiUnit: '%',
            chartType: 'line',
            showTrend: true,
            isPositiveTrend: false,
            calculateMaturity: true,
            kpiSource: 'Jira',
            maxValue: '200',
            thresholdValue: 10,
            kanban: false,
            groupId: 2,
            kpiInfo: {},
            aggregationCriteria: 'average',
            trendCalculative: false,
            additionalFilterSupport: true,
            xaxisLabel: 'Sprints',
            yaxisLabel: 'Percentage'
          }]
      },
      filterData: [
        {
          nodeId: 'BITBUCKET_DEMO_632c46c6728e93266f5d5631',
          nodeName: 'BITBUCKET_DEMO',
          path: 't3_subaccount###t2_account###t1_business###bittest_corporate',
          labelName: 'project',
          parentId: 't3_subaccount',
          level: 5,
          basicProjectConfigId: '632c46c6728e93266f5d5631'
        }
      ],
      filterApplyData: {
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
      },
      selectedTab: 'My Test1',
      isAdditionalFilters: false,
      makeAPICall: true
    };
    component.kanbanActivated = false;
    component.selectedtype = 'Scrum';

    const spyJenkins = spyOn(component, 'groupJenkinsKpi');
    localStorage.setItem('hierarchyData', JSON.stringify(hierarchyData));
    spyOn(component, 'getKpiCommentsCount');
    component.receiveSharedData(event);

    expect(spyJenkins).toHaveBeenCalled();
  });

  it('should make post Jenkins call', fakeAsync(() => {
    const postData = {
      kpiList: [
        {
          id: '633ed17f2c2d5abef2451fe3',
          kpiId: 'kpi118',
          kpiName: 'Deployment Frequency',
        },
        {
          id: '633ed17f2c2d5abef2451fe4',
          kpiId: 'kpi116',
          kpiName: 'Change Failure Rate'
        }
      ]
    };

    component.jenkinsKpiRequest = '';
    spyOn(httpService, 'postKpi').and.returnValue(of(postData.kpiList));
    const spy = spyOn(component, 'createAllKpiArray');
    component.postJenkinsKpi(postData, 'Jenkins');
    tick();
    expect(spy).toHaveBeenCalledWith(postData.kpiList);
  }));

  it('should make post Jenkins Kanban call', fakeAsync(() => {
    const postData = {
      kpiList: [
        {
          id: '633ed17f2c2d5abef2451fe3',
          kpiId: 'kpi118',
          kpiName: 'Deployment Frequency',
        },
        {
          id: '633ed17f2c2d5abef2451fe4',
          kpiId: 'kpi116',
          kpiName: 'Change Failure Rate'
        }
      ]
    };

    component.jenkinsKpiRequest = '';
    spyOn(httpService, 'postKpiKanban').and.returnValue(of(postData.kpiList));
    const spy = spyOn(component, 'createAllKpiArray');
    component.postJenkinsKanbanKpi(postData, 'Jenkins');
    tick();
    expect(spy).toHaveBeenCalledWith(postData.kpiList);
  }));
});
