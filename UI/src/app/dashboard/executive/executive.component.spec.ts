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

import { ComponentFixture, TestBed, inject, waitForAsync, fakeAsync, tick } from '@angular/core/testing';
import { ExecutiveComponent } from './executive.component';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpService } from '../../services/http.service';
import { SharedService } from '../../services/shared.service';
import { HelperService } from '../../services/helper.service';

import { APP_CONFIG, AppConfig } from '../../services/app.config';
import { ExcelService } from '../../services/excel.service';
import { DatePipe } from '../../../../node_modules/@angular/common';
import { RouterTestingModule } from '@angular/router/testing';
import { CommonModule } from '@angular/common';
import { InputSwitchModule } from 'primeng/inputswitch';
import { ReactiveFormsModule } from '@angular/forms';
import { Routes } from '@angular/router';
import { DashboardComponent } from '../../dashboard/dashboard.component';
declare let $: any;
import { CircularProgressComponent } from '../../component/circular-progress/circular-progress.component';
import { ProgressbarComponent } from '../../component/progressbar/progressbar.component';
import { CircularchartComponent } from '../../component/circularchart/circularchart.component';
import { NumberchartComponent } from '../../component/numberchart/numberchart.component';
import { BarchartComponent } from '../../component/barchart/barchart.component';
import { LineBarChartComponent } from '../../component/line-bar-chart/line-bar-chart.component';
import { GaugechartComponent } from '../../component/gaugechart/gaugechart.component';
import { MultilineComponent } from '../../component/multiline/multiline.component';
import { MaturityComponent } from '../../dashboard/maturity/maturity.component';
import { FilterComponent } from '../../dashboard/filter/filter.component';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { of } from 'rxjs/internal/observable/of';
import { DropdownModule } from 'primeng/dropdown';
import { ExportExcelComponent } from 'src/app/component/export-excel/export-excel.component';

const masterData = require('../../../test/resource/masterData.json');
const filterData = require('../../../test/resource/filterData.json');

describe('ExecutiveComponent', () => {
  let component: ExecutiveComponent;
  let fixture: ComponentFixture<ExecutiveComponent>;
  let service: SharedService;
  let httpService: HttpService;
  let helperService: HelperService;

  const baseUrl = environment.baseUrl;  // Servers Env

  const globalData =require('../../../test/resource/fakeGlobalConfigData.json');
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
  const filterApplyDataWithNoFilter = {};
  const filterApplyDataWithScrum = { kpiList: [{ id: '5d3013be4020938b42c23ba7', kpiId: 'kpi8', kpiName: 'Code Build Time', isDeleted: 'False', kpiCategory: 'Productivity', kpiUnit: 'min', kpiSource: 'Jenkins', maxValue: '100', kanban: false, chartType: 'gaugeChart' }], ids: ['Speedy 2.0_62503_Speedy 2.0'], level: 3, selectedMap: { hierarchyLevelOne :['ASDFG_hierarchyLevelOne'], Project: ['Speedy 2.0_62503_Speedy 2.0'], SubProject: [], Sprint: [], Build: [], Release: [], Squad: [], Individual: [] } };
  const filterApplyDataWithKanban = { kpiList: [{ id: '5d3013be4020938b42c23bd0', kpiId: 'kpi66', kpiName: 'Code Build Time', isDeleted: 'False', kpiCategory: 'Productivity', kpiUnit: 'min', kpiSource: 'Jenkins', maxValue: '100', kanban: true, chartType: 'gaugeChart' }], ids: ['Date Range'], level: 5, selectedMap: { hierarchyLevelOne :['ASDFG_hierarchyLevelOne'], Project: [], SubProject: [], Date: ['Date Range'], Build: [], Release: [], Squad: [], Individual: [] }, startDate: '2019-04-30T18:30:00.000Z', endDate: '2019-08-08T11:00:24.000Z' };
  const selectedTab = 'mydashboard';

  const dashConfigData = require('../../../test/resource/fakeShowHideApi.json');

  let httpMock;
  let reqJira;

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

  const fakeJenkins = [{ kpiId: 'kpi8', kpiName: 'Code Build Time', unit: 'min', maxValue: '100', chartType: 'gaugeChart', id: '5d3013be4020938b42c23ba7', isDeleted: 'False', kpiCategory: 'Productivity', kpiUnit: 'min', kanban: false, kpiSource: 'Jenkins', trendValueList: [], maturityValue: '0', maturityRange: ['-360', '360-240', '240-120', '120-10', '10-'] }];

  const fakeZypher = [{ kpiId: 'kpi16', kpiName: 'In-Sprint Automation Coverage', value: 0, unit: '', maxValue: '100', chartType: 'gaugeChart', id: '5d3013be4020938b42c23bac', isDeleted: 'False', kpiCategory: 'Quality', kpiUnit: '', kanban: false, kpiSource: 'Zypher', thresholdValue: 80.0, trendValueList: [{ data: 'Speedy 2.0', value: [{ data: '0', sProjectName: 'Speedy 2.0', sSprintID: 'KPI dashboard Sprint 5_62503_Speedy 2.0', sSprintName: 'KPI dashboard Sprint 5_Speedy 2.0', value: 0.0, howerValue: { 'Automated Tests': 0, 'Total Tests': 0 }, preCalculatedDataModel: { data: {} } }, { data: '0', sProjectName: 'Speedy 2.0', sSprintID: 'KPI dashboard Sprint 18_62503_Speedy 2.0', sSprintName: 'KPI dashboard Sprint 18_Speedy 2.0', value: 0.0, howerValue: { 'Automated Tests': 0, 'Total Tests': 0 }, preCalculatedDataModel: { data: {} } }, { data: '0', sProjectName: 'Speedy 2.0', sSprintID: 'KPI dashboard Sprint 23_62503_Speedy 2.0', sSprintName: 'KPI dashboard Sprint 23_Speedy 2.0', value: 0.0, howerValue: { 'Automated Tests': 0, 'Total Tests': 0 }, preCalculatedDataModel: { data: {} } }, { data: '0', sProjectName: 'Speedy 2.0', sSprintID: 'KPI dashboard Sprint 32_62503_Speedy 2.0', sSprintName: 'KPI dashboard Sprint 32_Speedy 2.0', value: 0.0, howerValue: { 'Automated Tests': 0, 'Total Tests': 0 }, preCalculatedDataModel: { data: {} } }, { data: '0', sProjectName: 'Speedy 2.0', sSprintID: 'KPI dashboard Sprint 33_62503_Speedy 2.0', sSprintName: 'KPI dashboard Sprint 33_Speedy 2.0', value: 0.0, howerValue: { 'Automated Tests': 0, 'Total Tests': 0 }, preCalculatedDataModel: { data: {} } }, { data: '0', sProjectName: 'Speedy 2.0', sSprintID: 'KPI dashboard Sprint 34_62503_Speedy 2.0', sSprintName: 'KPI dashboard Sprint 34_Speedy 2.0', value: 0.0, howerValue: { 'Automated Tests': 0, 'Total Tests': 0 }, preCalculatedDataModel: { data: {} } }, { data: '0', sProjectName: 'Speedy 2.0', sSprintID: 'KPI dashboard Sprint 35_62503_Speedy 2.0', sSprintName: 'KPI dashboard Sprint 35_Speedy 2.0', value: 0.0, howerValue: { 'Automated Tests': 0, 'Total Tests': 0 }, preCalculatedDataModel: { data: {} } }, { data: '0', sProjectName: 'Speedy 2.0', sSprintID: 'DTI | 22 Jul - 04 Aug_62503_Speedy 2.0', sSprintName: 'DTI | 22 Jul - 04 Aug_Speedy 2.0', value: 0.0, howerValue: { 'Automated Tests': 0, 'Total Tests': 0 }, preCalculatedDataModel: { data: {} } }], preCalculatedDataModel: { data: {} } }], maturityValue: '1', maturityRange: ['-20', '20-40', '40-60', '60-80', '80-'] }, { kpiId: 'kpi42', kpiName: 'Regression Automation Coverage', value: 0, unit: '%', maxValue: '100', chartType: 'gaugeChart', id: '5d3013be4020938b42c23bb9', isDeleted: 'False', kpiCategory: 'Quality', kpiUnit: '%', kanban: false, kpiSource: 'Zypher', trendValueList: [{ data: 'Speedy 2.0', value: [{ data: '0.0', sProjectName: 'Speedy 2.0', sSprintID: 'KPI dashboard Sprint 5_62503_Speedy 2.0', sSprintName: 'KPI dashboard Sprint 5_Speedy 2.0', value: 0.0, howerValue: {}, preCalculatedDataModel: { data: {} } }, { data: '0.0', sProjectName: 'Speedy 2.0', sSprintID: 'KPI dashboard Sprint 18_62503_Speedy 2.0', sSprintName: 'KPI dashboard Sprint 18_Speedy 2.0', value: 0.0, howerValue: {}, preCalculatedDataModel: { data: {} } }, { data: '0.0', sProjectName: 'Speedy 2.0', sSprintID: 'KPI dashboard Sprint 23_62503_Speedy 2.0', sSprintName: 'KPI dashboard Sprint 23_Speedy 2.0', value: 0.0, howerValue: {}, preCalculatedDataModel: { data: {} } }, { data: '0.0', sProjectName: 'Speedy 2.0', sSprintID: 'KPI dashboard Sprint 32_62503_Speedy 2.0', sSprintName: 'KPI dashboard Sprint 32_Speedy 2.0', value: 0.0, howerValue: {}, preCalculatedDataModel: { data: {} } }, { data: '0.0', sProjectName: 'Speedy 2.0', sSprintID: 'KPI dashboard Sprint 33_62503_Speedy 2.0', sSprintName: 'KPI dashboard Sprint 33_Speedy 2.0', value: 0.0, howerValue: {}, preCalculatedDataModel: { data: {} } }, { data: '0.0', sProjectName: 'Speedy 2.0', sSprintID: 'KPI dashboard Sprint 34_62503_Speedy 2.0', sSprintName: 'KPI dashboard Sprint 34_Speedy 2.0', value: 0.0, howerValue: {}, preCalculatedDataModel: { data: {} } }, { data: '0.0', sProjectName: 'Speedy 2.0', sSprintID: 'KPI dashboard Sprint 35_62503_Speedy 2.0', sSprintName: 'KPI dashboard Sprint 35_Speedy 2.0', value: 0.0, howerValue: {}, preCalculatedDataModel: { data: {} } }, { data: '0.0', sProjectName: 'Speedy 2.0', sSprintID: 'DTI | 22 Jul - 04 Aug_62503_Speedy 2.0', sSprintName: 'DTI | 22 Jul - 04 Aug_Speedy 2.0', value: 0.0, howerValue: {}, preCalculatedDataModel: { data: {} } }], preCalculatedDataModel: { data: {} } }], maturityValue: '1', maturityRange: ['-20', '20-40', '40-60', '60-80', '80-'] }];

  const fakeBitBucket = [{ kpiId: 'kpi11', kpiName: 'Number of check-ins per day in master', value: [], unit: 'check-ins', maxValue: '10', chartType: 'gaugeChart', id: '5d3013be4020938b42c23ba9', isDeleted: 'False', kpiCategory: 'Productivity', kpiUnit: 'check-ins', kanban: false, kpiSource: 'BitBucket', thresholdValue: 55.0, maturityRange: ['0', '2', '4', '8', '32'] }];

  const fakeSonar = [{ kpiId: 'kpi15', kpiName: 'Code Quality', value: {}, chartType: 'gaugeChart', id: '5d3013be4020938b42c23bab', isDeleted: 'False', kpiCategory: 'Quality', kanban: false, kpiSource: 'Sonar', thresholdValue: 55.0, trendValueList: [], maturityValue: '0', maturityRange: ['E', 'D', 'C', 'B', 'A'] }, { kpiId: 'kpi17', kpiName: 'Unit Testing', value: { aggregatedValue: '0.00' }, unit: '%', maxValue: '100', chartType: 'gaugeChart', id: '5d3013be4020938b42c23bad', isDeleted: 'False', kpiCategory: 'Quality', kpiUnit: '%', kanban: false, kpiSource: 'Sonar', thresholdValue: 55.0, trendValueList: [], maturityValue: '5', maturityRange: ['-80', '80-60', '60-40', '40-20', '20-'] }, { kpiId: 'kpi27', kpiName: 'Sonar Tech Debt', value: { aggregatedValue: 0 }, unit: 'Days', maxValue: '90', chartType: 'gaugeChart', id: '5d3013be4020938b42c23baf', isDeleted: 'False', kpiCategory: 'Productivity', kpiUnit: 'Days', kanban: false, kpiSource: 'Sonar', thresholdValue: 55.0, trendValueList: [], maturityValue: '0', maturityRange: ['-80', '80-60', '60-40', '40-20', '20-'] }, { kpiId: 'kpi38', kpiName: 'Sonar Violations', value: { aggregatedValue: [] }, unit: 'Number', maxValue: '', chartType: 'gaugeChart', id: '5d3013be4020938b42c23bb5', isDeleted: 'False', kpiCategory: 'Quality', kpiUnit: 'Number', kanban: false, kpiSource: 'Sonar', thresholdValue: 55.0, trendValueList: [] }];

  const fakeSonarKanban = [{ kpiId: 'kpi61', kpiName: 'Code Quality', value: {}, chartType: 'gaugeChart', id: '5d3013be4020938b42c23bcb', isDeleted: 'False', kpiCategory: 'Quality', kanban: true, kpiSource: 'Sonar', thresholdValue: 55.0, trendValueList: [], maturityValue: '0', maturityRange: ['E', 'D', 'C', 'B', 'A'] }, { kpiId: 'kpi62', kpiName: 'Unit Testing', value: { aggregatedValue: '0.0' }, unit: '%', maxValue: '100', chartType: 'gaugeChart', id: '5d3013be4020938b42c23bcc', isDeleted: 'False', kpiCategory: 'Quality', kpiUnit: '%', kanban: true, kpiSource: 'Sonar', thresholdValue: 55.0, trendValueList: [], maturityValue: '5', maturityRange: ['-20', '20-40', '40-60', '60-80', '80-'] }, { kpiId: 'kpi64', kpiName: 'Sonar Violations', value: { aggregatedValue: [] }, unit: 'Number', maxValue: '', chartType: 'gaugeChart', id: '5d3013be4020938b42c23bce', isDeleted: 'False', kpiCategory: 'Quality', kpiUnit: 'Number', kanban: true, kpiSource: 'Sonar', thresholdValue: 55.0, trendValueList: [] }, { kpiId: 'kpi67', kpiName: 'Sonar Tech Debt', value: {}, unit: 'Days', maxValue: '90', chartType: 'gaugeChart', id: '5d3013be4020938b42c23bd1', isDeleted: 'False', kpiCategory: 'Productivity', kpiUnit: 'Days', kanban: true, kpiSource: 'Sonar', thresholdValue: 55.0, trendValueList: [], maturityValue: '0', maturityRange: ['-80', '80-60', '60-40', '40-20', '20-'] }];

  const fakeJenkinsKanban = [{ kpiId: 'kpi66', kpiName: 'Code Build Time', unit: 'min', maxValue: '100', chartType: 'gaugeChart', id: '5d3013be4020938b42c23bd0', isDeleted: 'False', kpiCategory: 'Productivity', kpiUnit: 'min', kanban: true, kpiSource: 'Jenkins', trendValueList: [], maturityValue: '0', maturityRange: ['-360', '360-240', '240-120', '120-10', '10-'] }];

  const fakeZypherKanban = [{ kpiId: 'kpi63', kpiName: 'Regression Automation Coverage', value: 74, unit: '%', maxValue: '100', chartType: 'gaugeChart', id: '5d3013be4020938b42c23bcd', isDeleted: 'False', kpiCategory: 'Quality', kpiUnit: '%', kanban: true, kpiSource: 'Zypher', trendValueList: [{ data: 'TESTNIS Pace', value: [{ data: '0.0', sProjectName: 'TESTNIS Pace', value: 74.0, kanbanDate: '2019-08-04', howerValue: { 'Automated Tests': 0, 'Total Tests': 587 }, preCalculatedDataModel: { data: {} } }, { data: '0.0', sProjectName: 'TESTNIS Pace', value: 74.0, kanbanDate: '2019-08-05', howerValue: { 'Automated Tests': 0, 'Total Tests': 587 }, preCalculatedDataModel: { data: {} } }, { data: '0.0', sProjectName: 'TESTNIS Pace', value: 74.0, kanbanDate: '2019-08-06', howerValue: { 'Automated Tests': 0, 'Total Tests': 587 }, preCalculatedDataModel: { data: {} } }, { data: '0.0', sProjectName: 'TESTNIS Pace', value: 74.0, kanbanDate: '2019-08-07', howerValue: { 'Automated Tests': 0, 'Total Tests': 587 }, preCalculatedDataModel: { data: {} } }, { data: '0.0', sProjectName: 'TESTNIS Pace', value: 74.0, kanbanDate: '2019-08-08', howerValue: { 'Automated Tests': 0, 'Total Tests': 587 }, preCalculatedDataModel: { data: {} } }, { data: '0.0', sProjectName: 'TESTNIS Pace', value: 74.0, kanbanDate: '2019-08-09', howerValue: { 'Automated Tests': 0, 'Total Tests': 587 }, preCalculatedDataModel: { data: {} } }, { data: '0.0', sProjectName: 'TESTNIS Pace', value: 74.0, kanbanDate: '2019-08-10', howerValue: { 'Automated Tests': 0, 'Total Tests': 587 }, preCalculatedDataModel: { data: {} } }], preCalculatedDataModel: { data: {} } }, { data: 'PACE Support Project', value: [{ data: '0.0', sProjectName: 'PACE Support Project', value: 74.0, kanbanDate: '2019-08-04', howerValue: { 'Automated Tests': 0, 'Total Tests': 587 }, preCalculatedDataModel: { data: {} } }, { data: '0.0', sProjectName: 'PACE Support Project', value: 74.0, kanbanDate: '2019-08-05', howerValue: { 'Automated Tests': 0, 'Total Tests': 587 }, preCalculatedDataModel: { data: {} } }, { data: '0.0', sProjectName: 'PACE Support Project', value: 74.0, kanbanDate: '2019-08-06', howerValue: { 'Automated Tests': 0, 'Total Tests': 587 }, preCalculatedDataModel: { data: {} } }, { data: '0.0', sProjectName: 'PACE Support Project', value: 74.0, kanbanDate: '2019-08-07', howerValue: { 'Automated Tests': 0, 'Total Tests': 587 }, preCalculatedDataModel: { data: {} } }, { data: '0.0', sProjectName: 'PACE Support Project', value: 74.0, kanbanDate: '2019-08-08', howerValue: { 'Automated Tests': 0, 'Total Tests': 587 }, preCalculatedDataModel: { data: {} } }, { data: '0.0', sProjectName: 'PACE Support Project', value: 74.0, kanbanDate: '2019-08-09', howerValue: { 'Automated Tests': 0, 'Total Tests': 587 }, preCalculatedDataModel: { data: {} } }, { data: '0.0', sProjectName: 'PACE Support Project', value: 74.0, kanbanDate: '2019-08-10', howerValue: { 'Automated Tests': 0, 'Total Tests': 587 }, preCalculatedDataModel: { data: {} } }], preCalculatedDataModel: { data: {} } }, { data: 'Helios DevOps Support', value: [{ data: '0.0', sProjectName: 'Helios DevOps Support', value: 74.0, kanbanDate: '2019-08-04', howerValue: { 'Automated Tests': 0, 'Total Tests': 587 }, preCalculatedDataModel: { data: {} } }, { data: '0.0', sProjectName: 'Helios DevOps Support', value: 74.0, kanbanDate: '2019-08-05', howerValue: { 'Automated Tests': 0, 'Total Tests': 587 }, preCalculatedDataModel: { data: {} } }, { data: '0.0', sProjectName: 'Helios DevOps Support', value: 74.0, kanbanDate: '2019-08-06', howerValue: { 'Automated Tests': 0, 'Total Tests': 587 }, preCalculatedDataModel: { data: {} } }, { data: '0.0', sProjectName: 'Helios DevOps Support', value: 74.0, kanbanDate: '2019-08-07', howerValue: { 'Automated Tests': 0, 'Total Tests': 587 }, preCalculatedDataModel: { data: {} } }, { data: '0.0', sProjectName: 'Helios DevOps Support', value: 74.0, kanbanDate: '2019-08-08', howerValue: { 'Automated Tests': 0, 'Total Tests': 587 }, preCalculatedDataModel: { data: {} } }, { data: '0.0', sProjectName: 'Helios DevOps Support', value: 74.0, kanbanDate: '2019-08-09', howerValue: { 'Automated Tests': 0, 'Total Tests': 587 }, preCalculatedDataModel: { data: {} } }, { data: '0.0', sProjectName: 'Helios DevOps Support', value: 74.0, kanbanDate: '2019-08-10', howerValue: { 'Automated Tests': 0, 'Total Tests': 587 }, preCalculatedDataModel: { data: {} } }], preCalculatedDataModel: { data: {} } }], maturityValue: '4', maturityRange: ['-20', '20-40', '40-60', '60-80', '80-'] }];

  const fakejiraKanban = [
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
                        sSprintName: 'TEST |Test1 |ITR_1|OpenSource_Scrum Project',
                        value: 100,
                        hoverValue: {
                            'FTP Stories': 9,
                            'Closed Stories': 9
                        },
                        sprintIds: [
                            '40203_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'TEST |Test1 |ITR_1|OpenSource_Scrum Project'
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
                        sSprintName: 'TEST| Test1|PI_10|Opensource_Scrum Project',
                        value: 70,
                        hoverValue: {
                            'FTP Stories': 14,
                            'Closed Stories': 20
                        },
                        sprintIds: [
                            '40345_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'TEST| Test1|PI_10|Opensource_Scrum Project'
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
                        sSprintName: 'TEST |Test1 |ITR_1|OpenSource_Scrum Project',
                        value: 0,
                        hoverValue: {
                            Defects: 0,
                            'Size of Closed Stories': 25
                        },
                        sprintIds: [
                            '40203_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'TEST |Test1 |ITR_1|OpenSource_Scrum Project'
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
                        sSprintName: 'TEST| Test1|PI_10|Opensource_Scrum Project',
                        value: 58.54,
                        hoverValue: {
                            Defects: 12,
                            'Size of Closed Stories': 41
                        },
                        sprintIds: [
                            '40345_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'TEST| Test1|PI_10|Opensource_Scrum Project'
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
                        sSprintName: 'TEST |Test1 |ITR_1|OpenSource_Scrum Project',
                        value: 0,
                        hoverValue: {
                            'Escaped Defects': 0,
                            'Total Defects': 0
                        },
                        sprintIds: [
                            '40203_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'TEST |Test1 |ITR_1|OpenSource_Scrum Project'
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
                        sSprintName: 'TEST| Test1|PI_10|Opensource_Scrum Project',
                        value: 0,
                        hoverValue: {
                            'Escaped Defects': 0,
                            'Total Defects': 12
                        },
                        sprintIds: [
                            '40345_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'TEST| Test1|PI_10|Opensource_Scrum Project'
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
                                sSprintName: 'TEST |Test1 |ITR_1|OpenSource_Scrum Project',
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
                                sSprintName: 'TEST| Test1|PI_10|Opensource_Scrum Project',
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
                                sSprintName: 'TEST |Test1 |ITR_1|OpenSource_Scrum Project',
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
                                sSprintName: 'TEST| Test1|PI_10|Opensource_Scrum Project',
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
                                sSprintName: 'TEST |Test1 |ITR_1|OpenSource_Scrum Project',
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
                                sSprintName: 'TEST| Test1|PI_10|Opensource_Scrum Project',
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
                                sSprintName: 'TEST |Test1 |ITR_1|OpenSource_Scrum Project',
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
                                sSprintName: 'TEST| Test1|PI_10|Opensource_Scrum Project',
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
                                sSprintName: 'TEST |Test1 |ITR_1|OpenSource_Scrum Project',
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
                                sSprintName: 'TEST| Test1|PI_10|Opensource_Scrum Project',
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
                                sSprintName: 'TEST |Test1 |ITR_1|OpenSource_Scrum Project',
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
                                sSprintName: 'TEST| Test1|PI_10|Opensource_Scrum Project',
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
                                sSprintName: 'TEST |Test1 |ITR_1|OpenSource_Scrum Project',
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
                                sSprintName: 'TEST| Test1|PI_10|Opensource_Scrum Project',
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
                                sSprintName: 'TEST |Test1 |ITR_1|OpenSource_Scrum Project',
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
                                sSprintName: 'TEST| Test1|PI_10|Opensource_Scrum Project',
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
                                sSprintName: 'TEST |Test1 |ITR_1|OpenSource_Scrum Project',
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
                                sSprintName: 'TEST| Test1|PI_10|Opensource_Scrum Project',
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
                                sSprintName: 'TEST |Test1 |ITR_1|OpenSource_Scrum Project',
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
                                sSprintName: 'TEST| Test1|PI_10|Opensource_Scrum Project',
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
                        sSprintName: 'TEST |Test1 |ITR_1|OpenSource_Scrum Project',
                        value: 1,
                        hoverValue: {
                            resolvedDefects: 0,
                            createdDefects: 1
                        },
                        sprintIds: [
                            '40203_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'TEST |Test1 |ITR_1|OpenSource_Scrum Project'
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
                        sSprintName: 'TEST| Test1|PI_10|Opensource_Scrum Project',
                        value: 20,
                        hoverValue: {
                            resolvedDefects: 14,
                            createdDefects: 20
                        },
                        sprintIds: [
                            '40345_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'TEST| Test1|PI_10|Opensource_Scrum Project'
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
                        sSprintName: 'TEST |Test1 |ITR_1|OpenSource_Scrum Project',
                        value: 0,
                        hoverValue: {
                            'Estimated Hours': 0,
                            'Logged Work': 68
                        },
                        sprintIds: [
                            '40203_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'TEST |Test1 |ITR_1|OpenSource_Scrum Project'
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
                        sSprintName: 'TEST| Test1|PI_10|Opensource_Scrum Project',
                        value: 50,
                        hoverValue: {
                            'Estimated Hours': 50,
                            'Logged Work': 139
                        },
                        sprintIds: [
                            '40345_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'TEST| Test1|PI_10|Opensource_Scrum Project'
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
                        sSprintName: 'TEST |Test1 |ITR_1|OpenSource_Scrum Project',
                        value: 22,
                        hoverValue: {},
                        sprintIds: [
                            '40203_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'TEST |Test1 |ITR_1|OpenSource_Scrum Project'
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
                        sSprintName: 'TEST| Test1|PI_10|Opensource_Scrum Project',
                        value: 33,
                        hoverValue: {},
                        sprintIds: [
                            '40345_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'TEST| Test1|PI_10|Opensource_Scrum Project'
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
                        sSprintName: 'TEST |Test1 |ITR_1|OpenSource_Scrum Project',
                        value: 61,
                        hoverValue: {},
                        sprintIds: [
                            '40203_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'TEST |Test1 |ITR_1|OpenSource_Scrum Project'
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
                        sSprintName: 'TEST| Test1|PI_10|Opensource_Scrum Project',
                        value: 116,
                        hoverValue: {},
                        sprintIds: [
                            '40345_Scrum Project_6335363749794a18e8a4479b'
                        ],
                        sprintNames: [
                            'TEST| Test1|PI_10|Opensource_Scrum Project'
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


  beforeEach(() => {

    service = new SharedService();

    const routes: Routes = [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'authentication/login', component: DashboardComponent }

    ];

    TestBed.configureTestingModule({
      imports: [
        FormsModule,
        HttpClientTestingModule,
        InputSwitchModule,
        ReactiveFormsModule,
        CommonModule,
        DropdownModule,
        RouterTestingModule.withRoutes(routes)
      ],
      declarations: [
        CircularProgressComponent,
        ProgressbarComponent,
        CircularchartComponent,
        NumberchartComponent,
        BarchartComponent,
        LineBarChartComponent,
        GaugechartComponent,
        MultilineComponent,
        ExecutiveComponent,
        MaturityComponent,
        FilterComponent,
        DashboardComponent,
        ExportExcelComponent
      ],
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

  });




  beforeEach(() => {


    fixture = TestBed.createComponent(ExecutiveComponent);
    component = fixture.componentInstance;

    const type = 'Scrum';
    service.selectedtype=type;
    service.select(masterData, filterData, filterApplyDataWithNoFilter, selectedTab);
    service.setDashConfigData(dashConfigData.data);

    fixture.detectChanges();

    httpService = TestBed.get(HttpService);
    helperService = TestBed.get(HelperService);

    httpMock = TestBed.get(HttpTestingController);

    // We set the expectations for the HttpClient mock
    reqJira = httpMock.match((request) => request.url);

    spyOn(helperService, 'colorAccToMaturity').and.returnValue(('#44739f'));



  });

  afterEach(() => {
    httpMock.verify();
  });

  it('check whether scrum', (done) => {
    const type = 'Scrum';
    component.selectedtype = 'Scrum';
    spyOn(httpService,'postKpi').and.returnValue(of(fakeJiraGroupId1));
    fixture.detectChanges();
    expect(component.selectedtype).toBe(type);
    done();

  });

    it('download excel functionality', fakeAsync(() => {
        const excelData = {
            kpiName: 'Defect Injection Rate',
            kpiId: 'kpi14',
            columns: ['sprintName','storyID','issueDescription','linkedDefects'],
            excelData: [
                {
                    // eslint-disable-next-line @typescript-eslint/naming-convention
                    sprintName: 'AP|PI_10|ITR_5| 24 Aug',
                    storyID: {
                        // eslint-disable-next-line @typescript-eslint/naming-convention
                        'TEST-17970': 'http://testabc.com/jira/browse/TEST-17970'
                    },
                    issueDescription: 'This is second Story',
                    linkedDefects: {
                        // eslint-disable-next-line @typescript-eslint/naming-convention
                        'TEST-18675': 'http://testabc.com/jira/browse/TEST-18675',
                    }
                }]
        };
        const spy = spyOn(component.exportExcelComponent, 'downloadExcel');
        component.iSAdditionalFilterSelected =false;
        component.downloadExcel('kpi35', 'Defect Seepage Rate', false,false);
        expect(spy).toHaveBeenCalled();
    }));

  xit('Scrum with filter applied', (done) => {
    const type = 'Scrum';
    service.selectedtype =type;

    service.select(masterData, filterData, filterApplyDataWithScrum, selectedTab);
    fixture.detectChanges();
    httpMock.match(baseUrl + '/api/jira/kpi')[0].flush(fakeJiraGroupId1);
    httpMock.match(baseUrl + '/api/jenkins/kpi')[0].flush(fakeJenkins);
    httpMock.match(baseUrl + '/api/zypher/kpi')[0].flush(fakeZypher);
    httpMock.match(baseUrl + '/api/bitbucket/kpi')[0].flush(fakeBitBucket);
    httpMock.match(baseUrl + '/api/sonar/kpi')[0].flush(fakeSonar);
    expect(component.selectedtype).toBe(type);
    done();

  });

  xit('kanban without filter applied', ((done) => {
    const type = 'Kanban';
    service.selectedtype=type;
    service.select(masterData, filterData, filterApplyDataWithNoFilter, selectedTab);
    httpMock.match(baseUrl + '/api/jirakanban/kpi')[0].flush(fakejiraKanban);
    httpMock.match(baseUrl + '/api/jenkinskanban/kpi')[0].flush(fakeJenkinsKanban);
    httpMock.match(baseUrl + '/api/zypherkanban/kpi')[0].flush(fakeZypherKanban);
    httpMock.match(baseUrl + '/api/bitbucketkanban/kpi')[0].flush(fakeBitBucket);
    httpMock.match(baseUrl + '/api/sonarkanban/kpi')[0].flush(fakeSonarKanban);
    expect(component.selectedtype).toBe(type);
    // fixture.detectChanges();
    done();
  }));

  it('kanban with filter applied only Date', (done) => {
    const type = 'Kanban';
    service.setSelectedTypeOrTabRefresh('Category One','Kanban');
    service.select(masterData, filterData, filterApplyDataWithKanban, selectedTab);
    fixture.detectChanges();
    spyOn(httpService,'postKpiKanban').and.returnValue(of(fakejiraKanban));
    // httpMock.match(baseUrl + '/api/jirakanban/kpi')[0].flush(fakejiraKanban);
    // httpMock.match(baseUrl + '/api/jenkinskanban/kpi')[0].flush(fakeJenkinsKanban);
    // httpMock.match(baseUrl + '/api/zypherkanban/kpi')[0].flush(fakeZypherKanban);
    // httpMock.match(baseUrl + '/api/bitbucketkanban/kpi')[0].flush(fakeBitBucket);
    // httpMock.match(baseUrl + '/api/sonarkanban/kpi')[0].flush(fakeSonarKanban);
    expect(component.selectedtype).toBe(type);
    done();


  });




  xit('cycle time priority Sum in kanban', ((done) => {
    const type = 'Kanban';
    service.selectedtype =type;
    service.select(masterData, filterData, filterApplyDataWithNoFilter, selectedTab);
    httpMock.match(baseUrl + '/api/jirakanban/kpi')[0].flush(fakejiraKanban);
    httpMock.match(baseUrl + '/api/jenkinskanban/kpi')[0].flush(fakeJenkinsKanban);
    httpMock.match(baseUrl + '/api/zypherkanban/kpi')[0].flush(fakeZypherKanban);
    httpMock.match(baseUrl + '/api/bitbucketkanban/kpi')[0].flush(fakeBitBucket);
    httpMock.match(baseUrl + '/api/sonarkanban/kpi')[0].flush(fakeSonarKanban);
 component.getPriorityColor(0);

   done();

  }));



  it('color acc to maturity check ', waitForAsync(() => {
    const returnBlue = component.returnColorAccToMaturity(0);
    spyOn(component,'receiveSharedData');
    expect(returnBlue).toBe('#44739f');
    // done();
  }));

  it('color acc to maturity check array', (done) => {
    component.colorAccToMaturity('1-2-3');
    spyOn(component,'receiveSharedData');
    expect(component.maturityColorCycleTime[0]).toBe('#44739f');
    done();
  });

  it('should create', (done) => {
    spyOn(component,'receiveSharedData');
    expect(component).toBeTruthy();
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
  });

  it('should make post call when kpi available for Sonar for Scrum',()=>{
    const kpiListSonar =[{
      id: '6332dd4b82451128f9939a29',
      kpiId: 'kpi17',
      kpiName: 'Unit Test Coverage'
  }];
    const spy=spyOn(helperService,'groupKpiFromMaster').and.returnValue({kpiList : kpiListSonar});
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
  component.masterData ={
    kpiList :[{
      kpiId: 'kpi17',
      kanban:false,
      kpiSource:'Zypher',
      groupId:1
    }]
  };
    const spy=spyOn(helperService,'groupKpiFromMaster').and.returnValue({kpiList : kpiListZypher});
    const postZypherSpy=spyOn(component,'postZypherKpi');
    component.groupZypherKpi(['kpi17']);
    expect(postZypherSpy).toHaveBeenCalled();
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
    const postJiraSpy=spyOn(component,'postJiraKpi');
    component.groupJiraKpi(['kpi17']);
    expect(postJiraSpy).toHaveBeenCalled();
  });


  it('should make post call when kpi available for BitBucket for Scrum',()=>{
    const kpiListBitBucket =[{
      id: '6332dd4b82451128f9939a29',
      kpiId: 'kpi17',
      kpiName: 'Unit Test Coverage'
  }];
    const spy=spyOn(helperService,'groupKpiFromMaster').and.returnValue({kpiList : kpiListBitBucket});
    const postBitBucketSpy=spyOn(component,'postBitBucketKpi');
    component.groupBitBucketKpi(['kpi17']);
    expect(postBitBucketSpy).toHaveBeenCalled();
  });

  it('should check if kpi exists',()=>{
    component.allKpiArray =[{
      kpiId:'kpi13'
    }];
    const result = component.ifKpiExist('kpi13');
    expect(result).toEqual(0);
  });


  it('should refresh values onTypeRefresh',()=>{
    spyOn(service,'getSelectedType');
    spyOn(service,'getDashConfigData').and.returnValue(globalData['data']);
    const spy =spyOn(component,'processKpiConfigData');
    service.onTypeOrTabRefresh.next({selectedTab:'Caterory One',selectedType:'Scrum'});
    component.kanbanActivated =false;
    fixture.detectChanges();
   expect(component.selectedBranchFilter).toBe('Select');
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
    const spyZypher = spyOn(component, 'groupZypherKpi');
    const spyJira = spyOn(component, 'groupJiraKpi');
    const spyBitBucket = spyOn(component, 'groupBitBucketKpi');
    const spySonar = spyOn(component, 'groupSonarKpi');
    localStorage.setItem('hierarchyData', JSON.stringify(hierarchyData));
    spyOn(component, 'getKpiCommentsCount');
    component.receiveSharedData(event);

    expect(spyJenkins).toHaveBeenCalled();
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
    const result = component.getVideoLink('kpi14');
    expect(result).toEqual('www.google.com');
  });

  it('should check if video link is available', () => {
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
    expect(component.isVideoLinkAvailable('kpi15')).toBeFalse();
  });

  it('should return kpiName', () => {
    component.masterData = {
      kpiList: [
        {
          kpiId: 'kpi11',
          kpiName: 'Defect Injection Rate',
        }
      ]
    };
    expect(component.getKPIName('kpi14')).toBe('Defect Injection Rate');
  });

  it('should make post Sonar call', fakeAsync(() => {
    const postData = {
      kpiList: [
        {
          id: '633ed17f2c2d5abef2451fe3',
          kpiId: 'kpi17',
          kpiName: 'Unit Test Coverage',
        },
        {
          id: '633ed17f2c2d5abef2451fe4',
          kpiId: 'kpi38',
          kpiName: 'Sonar Violations'
        },
        {
          id: '633ed17f2c2d5abef2451fe5',
          kpiId: 'kpi27',
          kpiName: 'Sonar Tech Debt',
        }
      ]
    };

    component.sonarKpiRequest = '';
    spyOn(httpService, 'postKpi').and.returnValue(of(postData.kpiList));
    const spy = spyOn(component, 'afterSonarKpiResponseReceived');
    component.postSonarKpi(postData, 'Sonar');
    tick();
    expect(spy).toHaveBeenCalledWith(postData.kpiList);
  }));

  it('should make post Jenkins call', fakeAsync(() => {
    const postData = {
      kpiList: [
        {
          id: '633ed17f2c2d5abef2451fe3',
          kpiId: 'kpi17',
          kpiName: 'Unit Test Coverage',
        },
        {
          id: '633ed17f2c2d5abef2451fe4',
          kpiId: 'kpi38',
          kpiName: 'Sonar Violations'
        },
        {
          id: '633ed17f2c2d5abef2451fe5',
          kpiId: 'kpi27',
          kpiName: 'Sonar Tech Debt',
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

  it('should make post Jira call', fakeAsync(() => {
    const postData = {
      kpiList: [
        {
          id: '633ed17f2c2d5abef2451fe3',
          kpiId: 'kpi17',
          kpiName: 'Unit Test Coverage',
        },
        {
          id: '633ed17f2c2d5abef2451fe4',
          kpiId: 'kpi38',
          kpiName: 'Sonar Violations'
        },
        {
          id: '633ed17f2c2d5abef2451fe5',
          kpiId: 'kpi27',
          kpiName: 'Sonar Tech Debt',
        }
      ]
    };

    const kpiWiseData = {
      kpi27: {
        id: '633ed17f2c2d5abef2451fe5',
        kpiName: 'Sonar Tech Debt',
      },
      kpi38: {
        id: '633ed17f2c2d5abef2451fe4',
        kpiName: 'Sonar Violations'
      },
      kpi17: {
        id: '633ed17f2c2d5abef2451fe3',
        kpiName: 'Unit Test Coverage',
      }
    };
    component.jiraKpiRequest = '';
    component.loaderJiraArray = [];
    spyOn(helperService, 'createKpiWiseId').and.returnValue(kpiWiseData);
    spyOn(httpService, 'postKpi').and.returnValue(of(postData.kpiList));
    const spy = spyOn(component, 'createAllKpiArray');
    component.postJiraKpi(postData, 'Jira');
    component.jiraKpiData = {};
    tick();
    expect(spy).toHaveBeenCalled();
  }));

  it('should make post BitBucket call', fakeAsync(() => {
    const postData = {
      kpiList: [
        {
          id: '633ed17f2c2d5abef2451fe3',
          kpiId: 'kpi17',
          kpiName: 'Unit Test Coverage',
        },
        {
          id: '633ed17f2c2d5abef2451fe4',
          kpiId: 'kpi38',
          kpiName: 'Sonar Violations'
        },
        {
          id: '633ed17f2c2d5abef2451fe5',
          kpiId: 'kpi27',
          kpiName: 'Sonar Tech Debt',
        }
      ]
    };

    const kpiWiseData = {
      kpi27: {
        id: '633ed17f2c2d5abef2451fe5',
        kpiName: 'Sonar Tech Debt',
      },
      kpi38: {
        id: '633ed17f2c2d5abef2451fe4',
        kpiName: 'Sonar Violations'
      },
      kpi17: {
        id: '633ed17f2c2d5abef2451fe3',
        kpiName: 'Unit Test Coverage',
      }
    };
    component.bitBucketKpiRequest = '';
    component.loaderJiraArray = [];
    spyOn(helperService, 'createKpiWiseId').and.returnValue(kpiWiseData);
    spyOn(httpService, 'postKpi').and.returnValue(of(postData.kpiList));
    const spy = spyOn(component, 'createAllKpiArray');
    component.postBitBucketKpi(postData, 'Bitbucket');
    tick();
    expect(spy).toHaveBeenCalled();
  }));

  it('should return color', () => {
    expect(component.getPriorityColor(1)).toBe('#FE7F0C');
  });

  it('should change View', () => {
    component.changeView('list');
    expect(component.isChartView).toBeFalse();

    component.changeView('chart');
    expect(component.isChartView).toBeTrue();

  });

  it('should generate colorObj', () => {
    const arr = [
      {
        data: 'bittest',
        value: [
          {
            data: '0',
            value: 0,
            hoverValue: {},
            date: '2022-09-05 to 2022-09-11',
            sprojectName: 'bittest',
            xName: 1
          },
          {
            data: '0',
            value: 0,
            hoverValue: {},
            date: '2022-09-12 to 2022-09-18',
            sprojectName: 'bittest',
            xName: 2
          },
          {
            data: '0',
            value: 0,
            hoverValue: {},
            date: '2022-09-19 to 2022-09-25',
            sprojectName: 'bittest',
            xName: 3
          },
          {
            data: '0',
            value: 0,
            hoverValue: {},
            date: '2022-09-26 to 2022-10-02',
            sprojectName: 'bittest',
            xName: 4
          },
          {
            data: '0',
            value: 0,
            hoverValue: {},
            date: '2022-10-03 to 2022-10-09',
            sprojectName: 'bittest',
            xName: 5
          }
        ],
        maturity: '5'
      },
      {
        data: 'Corpate1',
        value: [
          {
            data: '19.0',
            value: 19,
            hoverValue: {},
            date: '2022-09-05 to 2022-09-11',
            sprintIds: [],
            sprintNames: [],
            projectNames: [
              'Bus1',
              'TestB'
            ],
            sprojectName: 'Corpate1',
            xName: 1
          },
          {
            data: '0.0',
            value: 0,
            hoverValue: {},
            date: '2022-09-12 to 2022-09-18',
            sprintIds: [],
            sprintNames: [],
            projectNames: [
              'Bus1',
              'TestB'
            ],
            sprojectName: 'Corpate1',
            xName: 2
          },
          {
            data: '0.0',
            value: 0,
            hoverValue: {},
            date: '2022-09-19 to 2022-09-25',
            sprintIds: [],
            sprintNames: [],
            projectNames: [
              'Bus1',
              'TestB'
            ],
            sprojectName: 'Corpate1',
            xName: 3
          },
          {
            data: '0.0',
            value: 0,
            hoverValue: {},
            date: '2022-09-26 to 2022-10-02',
            sprintIds: [],
            sprintNames: [],
            projectNames: [
              'Bus1',
              'TestB'
            ],
            sprojectName: 'Corpate1',
            xName: 4
          },
          {
            data: '0.0',
            value: 0,
            hoverValue: {},
            date: '2022-10-03 to 2022-10-09',
            sprintIds: [],
            sprintNames: [],
            projectNames: [
              'Bus1',
              'TestB'
            ],
            sprojectName: 'Corpate1',
            xName: 5
          }
        ],
        maturity: '4'
      }
    ];
    component.colorObj = {
      bittest_corporate: {
        nodeName: 'bittest',
        color: '#079FFF'
      },
      Corpate1_corporate: {
        nodeName: 'Corpate1',
        color: '#cdba38'
      }
    };
    component.chartColorList = {};
    component.generateColorObj('kpi84', arr);
    expect(component.chartColorList['kpi84'].length).toEqual(2);
  });

  it('should get dropdown data', () => {
    component.allKpiArray = [{
      kpiId: 'kpi11',
      kpiName: 'Check-Ins & Merge Requests',
      unit: 'MRs',
      maxValue: '10',
      chartType: '',
      id: '633fbb9cef4cf185c987ad5c',
      isDeleted: 'False',
      kpiUnit: 'MRs',
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
                  value: 0,
                  hoverValue: {
                    'No. of Check in': 0,
                    'No. of Merge Requests': 0
                  },
                  date: '03/10/2022',
                  lineValue: 0,
                  sprojectName: 'bittest'
                },
                {
                  value: 0,
                  hoverValue: {
                    'No. of Check in': 0,
                    'No. of Merge Requests': 0
                  },
                  date: '04/10/2022',
                  lineValue: 0,
                  sprojectName: 'bittest'
                },
                {
                  value: 0,
                  hoverValue: {
                    'No. of Check in': 0,
                    'No. of Merge Requests': 0
                  },
                  date: '05/10/2022',
                  lineValue: 0,
                  sprojectName: 'bittest'
                },
                {
                  value: 0,
                  hoverValue: {
                    'No. of Check in': 0,
                    'No. of Merge Requests': 0
                  },
                  date: '06/10/2022',
                  lineValue: 0,
                  sprojectName: 'bittest'
                },
                {
                  value: 0,
                  hoverValue: {
                    'No. of Check in': 0,
                    'No. of Merge Requests': 0
                  },
                  date: '07/10/2022',
                  lineValue: 0,
                  sprojectName: 'bittest'
                }
              ]
            },
            {
              data: 'Corpate1',
              value: [
                {
                  data: '0',
                  value: 0,
                  hoverValue: {
                    'No. of Check in': 0,
                    'No. of Merge Requests': 0
                  },
                  date: '03/10/2022',
                  sprintIds: [],
                  sprintNames: [],
                  projectNames: [
                    'Bus1',
                    'TestB'
                  ],
                  lineValue: 0,
                  sprojectName: 'Corpate1'
                },
                {
                  data: '0',
                  value: 0,
                  hoverValue: {
                    'No. of Check in': 0,
                    'No. of Merge Requests': 0
                  },
                  date: '04/10/2022',
                  sprintIds: [],
                  sprintNames: [],
                  projectNames: [
                    'Bus1',
                    'TestB'
                  ],
                  lineValue: 0,
                  sprojectName: 'Corpate1'
                },
                {
                  data: '0',
                  value: 0,
                  hoverValue: {
                    'No. of Check in': 0,
                    'No. of Merge Requests': 0
                  },
                  date: '05/10/2022',
                  sprintIds: [],
                  sprintNames: [],
                  projectNames: [
                    'Bus1',
                    'TestB'
                  ],
                  lineValue: 0,
                  sprojectName: 'Corpate1'
                },
                {
                  data: '0',
                  value: 0,
                  hoverValue: {
                    'No. of Check in': 0,
                    'No. of Merge Requests': 0
                  },
                  date: '06/10/2022',
                  sprintIds: [],
                  sprintNames: [],
                  projectNames: [
                    'Bus1',
                    'TestB'
                  ],
                  lineValue: 0,
                  sprojectName: 'Corpate1'
                },
                {
                  data: '0',
                  value: 0,
                  hoverValue: {
                    'No. of Check in': 0,
                    'No. of Merge Requests': 0
                  },
                  date: '07/10/2022',
                  sprintIds: [],
                  sprintNames: [],
                  projectNames: [
                    'Bus1',
                    'TestB'
                  ],
                  lineValue: 0,
                  sprojectName: 'Corpate1'
                }
              ]
            }
          ]
        }
      ],
      groupId: 1
    }];
    component.updatedConfigGlobalData = [
      {
        kpiId: 'kpi11',
        kpiName: 'Check-Ins & Merge Requests',
        isEnabled: true,
        order: 23,
        kpiDetail: {
          id: '633fbb9cef4cf185c987ad5c',
          kpiId: 'kpi11',
          kpiName: 'Check-Ins & Merge Requests',
          isDeleted: 'False',
          defaultOrder: 23,
          kpiUnit: 'MRs',
          chartType: 'grouped_column_plus_line',
          showTrend: true,
          isPositiveTrend: true,
          lineLegend: 'Merge Requests',
          barLegend: 'Commits',
          calculateMaturity: true,
          kpiSource: 'BitBucket',
          maxValue: '10',
          thresholdValue: 55,
          kanban: false,
          groupId: 1,
          kpiInfo: {},
          kpiFilter: 'dropDown',
          aggregationCriteria: 'average',
          yaxisLabel: 'Count',
          additionalFilterSupport: false,
          xaxisLabel: 'Days',
          trendCalculative: false
        },
        shown: true
      }
    ];
    component.getDropdownArray('kpi11');
    expect(component.kpiDropdowns['kpi11'].length).toBeGreaterThan(0);
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
            "maxValue": "",
            "kanban": false,
            "groupId": 8,
            "kpiInfo": {
                "details": [
                    {
                        "type": "paragraph",
                        "value": "Planned capacity is the development team's available time."
                    },
                    {
                        "type": "paragraph",
                        "value": "Source of this is KnowHOW"
                    }
                ]
            },
            "trendCalculative": false,
            "additionalFilterSupport": false,
            "xaxisLabel": "",
            "yaxisLabel": ""
        },
        "shown": true
    }
    const spy = spyOn(component, 'checkLatestAndTrendValue');
    component.checkLatestAndTrendValue(kpiData, item);
    expect(spy).toHaveBeenCalled();
  });

  it('should call set kpi values after SonarKpiResponseReceive', () => {
    const response = [
        {
            "kpiId": "kpi17",
            "kpiName": "Unit Test Coverage",
            "unit": "%",
            "maxValue": "100",
            "chartType": "",
            "kpiInfo": {
                "definition": "Measure  of the amount of code that is covered by unit tests.",
                "formula": [
                    {
                        "lhs": "The calculation is done directly in Sonarqube"
                    }
                ],
                "details": [
                    {
                        "type": "link",
                        "kpiLinkDetail": {
                            "text": "Detailed Information at",
                            "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#Unit-Test-Coverage"
                        }
                    }
                ]
            },
            "id": "64ad2860dadebadcf40c7038",
            "isDeleted": "False",
            "kpiUnit": "%",
            "kanban": false,
            "kpiSource": "Sonar",
            "thresholdValue": 55,
            "trendValueList": [],
            "maturityRange": [
                "-20",
                "20-40",
                "40-60",
                "60-80",
                "80-"
            ],
            "groupId": 1
        }
    ];
    spyOn(helperService, 'createKpiWiseId').and.returnValue({
        "kpi17": {
            "kpiId": "kpi17",
            "kpiName": "Unit Test Coverage",
            "unit": "%",
            "maxValue": "100",
            "chartType": "",
            "kpiInfo": {
                "definition": "Measure  of the amount of code that is covered by unit tests.",
                "formula": [
                    {
                        "lhs": "The calculation is done directly in Sonarqube"
                    }
                ],
                "details": [
                    {
                        "type": "link",
                        "kpiLinkDetail": {
                            "text": "Detailed Information at",
                            "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#Unit-Test-Coverage"
                        }
                    }
                ]
            },
            "id": "64ad2860dadebadcf40c7038",
            "isDeleted": "False",
            "kpiUnit": "%",
            "kanban": false,
            "kpiSource": "Sonar",
            "thresholdValue": 55,
            "trendValueList": [],
            "maturityRange": [
                "-20",
                "20-40",
                "40-60",
                "60-80",
                "80-"
            ],
            "groupId": 1
        }
    });
    spyOn(helperService, 'createSonarFilter');
    const createAllKpiArraySpy = spyOn(component, 'createAllKpiArray');
    component.afterSonarKpiResponseReceived(response);
    expect(component.kpiLoader).toBeFalse();
    expect(createAllKpiArraySpy).toHaveBeenCalled();

});



it('should call set kpi values after Zypher KpiResponseReceive', () => {
    const response = [
        {
            "kpiId": "kpi42",
            "kpiName": "Regression Automation Coverage",
            "unit": "%",
            "maxValue": "100",
            "chartType": "",
            "kpiInfo": {
                "definition": "Measures the progress of automation of regression test cases (the test cases which are marked as part of regression suite.",
                "formula": [
                    {
                        "lhs": "Regression Automation Coverage ",
                        "operator": "division",
                        "operands": [
                            "No. of regression test cases automated",
                            "Total no. of regression test cases"
                        ]
                    }
                ],
                "details": [
                    {
                        "type": "link",
                        "kpiLinkDetail": {
                            "text": "Detailed Information at",
                            "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#Regression-Automation-Coverage"
                        }
                    }
                ]
            },
            "id": "64ad2860dadebadcf40c7036",
            "isDeleted": "False",
            "kpiUnit": "%",
            "kanban": false,
            "kpiSource": "Zypher",
            "trendValueList": [
                {
                    "data": "AddingIterationProject",
                    "value": [
                        {
                            "data": "0.0",
                            "sSprintID": "43307_AddingIterationProject_64a4ff265b5fdd437756f904",
                            "sSprintName": "KnowHOW | PI_13| ITR_3_AddingIterationProject",
                            "value": 0,
                            "hoverValue": {},
                            "sprintIds": [
                                "43307_AddingIterationProject_64a4ff265b5fdd437756f904"
                            ],
                            "sprintNames": [
                                "KnowHOW | PI_13| ITR_3_AddingIterationProject"
                            ],
                            "sprojectName": "AddingIterationProject",
                            "xName": 1
                        },
                        {
                            "data": "0.0",
                            "sSprintID": "43308_AddingIterationProject_64a4ff265b5fdd437756f904",
                            "sSprintName": "KnowHOW | PI_13| ITR_4_AddingIterationProject",
                            "value": 0,
                            "hoverValue": {},
                            "sprintIds": [
                                "43308_AddingIterationProject_64a4ff265b5fdd437756f904"
                            ],
                            "sprintNames": [
                                "KnowHOW | PI_13| ITR_4_AddingIterationProject"
                            ],
                            "sprojectName": "AddingIterationProject",
                            "xName": 2
                        },
                        {
                            "data": "0.0",
                            "sSprintID": "43309_AddingIterationProject_64a4ff265b5fdd437756f904",
                            "sSprintName": "KnowHOW | PI_13| ITR_5_AddingIterationProject",
                            "value": 0,
                            "hoverValue": {},
                            "sprintIds": [
                                "43309_AddingIterationProject_64a4ff265b5fdd437756f904"
                            ],
                            "sprintNames": [
                                "KnowHOW | PI_13| ITR_5_AddingIterationProject"
                            ],
                            "sprojectName": "AddingIterationProject",
                            "xName": 3
                        },
                        {
                            "data": "0.0",
                            "sSprintID": "43310_AddingIterationProject_64a4ff265b5fdd437756f904",
                            "sSprintName": "KnowHOW | PI_13| ITR_6_AddingIterationProject",
                            "value": 0,
                            "hoverValue": {},
                            "sprintIds": [
                                "43310_AddingIterationProject_64a4ff265b5fdd437756f904"
                            ],
                            "sprintNames": [
                                "KnowHOW | PI_13| ITR_6_AddingIterationProject"
                            ],
                            "sprojectName": "AddingIterationProject",
                            "xName": 4
                        },
                        {
                            "data": "0.0",
                            "sSprintID": "45160_AddingIterationProject_64a4ff265b5fdd437756f904",
                            "sSprintName": "KnowHOW | PI_14| ITR_1_AddingIterationProject",
                            "value": 0,
                            "hoverValue": {},
                            "sprintIds": [
                                "45160_AddingIterationProject_64a4ff265b5fdd437756f904"
                            ],
                            "sprintNames": [
                                "KnowHOW | PI_14| ITR_1_AddingIterationProject"
                            ],
                            "sprojectName": "AddingIterationProject",
                            "xName": 5
                        }
                    ],
                    "maturity": "1",
                    "maturityValue": "0.0"
                }
            ],
            "maturityRange": [
                "-20",
                "20-40",
                "40-60",
                "60-80",
                "80-"
            ],
            "groupId": 1
        }
    ];
    component.selectedtype = 'Scrum';
    spyOn(helperService, 'createKpiWiseId').and.returnValue({
        kpi42: {
            "kpiId": "kpi42",
            "kpiName": "Regression Automation Coverage",
            "unit": "%",
            "maxValue": "100",
            "chartType": "",
            "kpiInfo": {
                "definition": "Measures the progress of automation of regression test cases (the test cases which are marked as part of regression suite.",
                "formula": [
                    {
                        "lhs": "Regression Automation Coverage ",
                        "operator": "division",
                        "operands": [
                            "No. of regression test cases automated",
                            "Total no. of regression test cases"
                        ]
                    }
                ],
                "details": [
                    {
                        "type": "link",
                        "kpiLinkDetail": {
                            "text": "Detailed Information at",
                            "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#Regression-Automation-Coverage"
                        }
                    }
                ]
            },
            "id": "64ad2860dadebadcf40c7036",
            "isDeleted": "False",
            "kpiUnit": "%",
            "kanban": false,
            "kpiSource": "Zypher",
            "trendValueList": [
                {
                    "data": "AddingIterationProject",
                    "value": [
                        {
                            "data": "0.0",
                            "sSprintID": "43307_AddingIterationProject_64a4ff265b5fdd437756f904",
                            "sSprintName": "KnowHOW | PI_13| ITR_3_AddingIterationProject",
                            "value": 0,
                            "hoverValue": {},
                            "sprintIds": [
                                "43307_AddingIterationProject_64a4ff265b5fdd437756f904"
                            ],
                            "sprintNames": [
                                "KnowHOW | PI_13| ITR_3_AddingIterationProject"
                            ],
                            "sprojectName": "AddingIterationProject",
                            "xName": 1
                        },
                        {
                            "data": "0.0",
                            "sSprintID": "43308_AddingIterationProject_64a4ff265b5fdd437756f904",
                            "sSprintName": "KnowHOW | PI_13| ITR_4_AddingIterationProject",
                            "value": 0,
                            "hoverValue": {},
                            "sprintIds": [
                                "43308_AddingIterationProject_64a4ff265b5fdd437756f904"
                            ],
                            "sprintNames": [
                                "KnowHOW | PI_13| ITR_4_AddingIterationProject"
                            ],
                            "sprojectName": "AddingIterationProject",
                            "xName": 2
                        },
                        {
                            "data": "0.0",
                            "sSprintID": "43309_AddingIterationProject_64a4ff265b5fdd437756f904",
                            "sSprintName": "KnowHOW | PI_13| ITR_5_AddingIterationProject",
                            "value": 0,
                            "hoverValue": {},
                            "sprintIds": [
                                "43309_AddingIterationProject_64a4ff265b5fdd437756f904"
                            ],
                            "sprintNames": [
                                "KnowHOW | PI_13| ITR_5_AddingIterationProject"
                            ],
                            "sprojectName": "AddingIterationProject",
                            "xName": 3
                        },
                        {
                            "data": "0.0",
                            "sSprintID": "43310_AddingIterationProject_64a4ff265b5fdd437756f904",
                            "sSprintName": "KnowHOW | PI_13| ITR_6_AddingIterationProject",
                            "value": 0,
                            "hoverValue": {},
                            "sprintIds": [
                                "43310_AddingIterationProject_64a4ff265b5fdd437756f904"
                            ],
                            "sprintNames": [
                                "KnowHOW | PI_13| ITR_6_AddingIterationProject"
                            ],
                            "sprojectName": "AddingIterationProject",
                            "xName": 4
                        },
                        {
                            "data": "0.0",
                            "sSprintID": "45160_AddingIterationProject_64a4ff265b5fdd437756f904",
                            "sSprintName": "KnowHOW | PI_14| ITR_1_AddingIterationProject",
                            "value": 0,
                            "hoverValue": {},
                            "sprintIds": [
                                "45160_AddingIterationProject_64a4ff265b5fdd437756f904"
                            ],
                            "sprintNames": [
                                "KnowHOW | PI_14| ITR_1_AddingIterationProject"
                            ],
                            "sprojectName": "AddingIterationProject",
                            "xName": 5
                        }
                    ],
                    "maturity": "1",
                    "maturityValue": "0.0"
                }
            ],
            "maturityRange": [
                "-20",
                "20-40",
                "40-60",
                "60-80",
                "80-"
            ],
            "groupId": 1
        }
    });
    spyOn(helperService, 'calculateTestExecutionData').and.returnValue({
        selectedTestExecutionFilterData: [],
        testExecutionFilterData: []
    });
    const createAllKpiArraySpy = spyOn(component, 'createAllKpiArray');
    component.afterZypherKpiResponseReceived(response);
    expect(component.kpiLoader).toBeFalse();
    expect(createAllKpiArraySpy).toHaveBeenCalled();

});

it('should call zypher kpi api',()=>{
  spyOn(httpService,'postKpi').and.returnValue(of({}));
  const spyafterZypherKpiResponseReceived = spyOn(component,'afterZypherKpiResponseReceived');
  component.postZypherKpi({},'Zypher');
  fixture.detectChanges();
  expect(spyafterZypherKpiResponseReceived).toHaveBeenCalled();
});

it('should call zypher kanban kpi api',()=>{
  component.zypherKpiRequest  = '';
  spyOn(httpService,'postKpiKanban').and.returnValue(of({}));
  const spyafterZypherKpiResponseReceived = spyOn(component,'afterZypherKpiResponseReceived');
  component.postZypherKanbanKpi({},'Zypher');
  fixture.detectChanges();
  expect(spyafterZypherKpiResponseReceived).toHaveBeenCalled();
});

it('should call post bitbucket kanban kpi',()=>{
  component.bitBucketKpiRequest  = '';
  spyOn(httpService,'postKpiKanban').and.returnValue(of({}));
  const spycreateAllKpiArray = spyOn(component,'createAllKpiArray');
  component.postBitBucketKanbanKpi({},'Bitbucket');
  fixture.detectChanges();
  expect(spycreateAllKpiArray).toHaveBeenCalled();
})

});


