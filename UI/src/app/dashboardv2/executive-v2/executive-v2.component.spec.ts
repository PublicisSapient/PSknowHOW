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
 * Unless required    by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

import {
  ComponentFixture,
  TestBed,
  inject,
  waitForAsync,
  fakeAsync,
  tick,
} from '@angular/core/testing';
import { ExecutiveV2Component } from './executive-v2.component';
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
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { of } from 'rxjs/internal/observable/of';
import { DropdownModule } from 'primeng/dropdown';
import { ExportExcelComponent } from 'src/app/component/export-excel/export-excel.component';

import * as Excel from 'exceljs';
import * as fs from 'file-saver';
import { MessageService } from 'primeng/api';
import { throwError } from 'rxjs';

const masterData = require('../../../test/resource/masterData.json');
const filterData = require('../../../test/resource/filterData.json');
const dashConfigData2 =
  require('../../../test/resource/boardConfigNew.json').data;

describe('ExecutiveV2Component', () => {
  let component: ExecutiveV2Component;
  let fixture: ComponentFixture<ExecutiveV2Component>;
  let service: SharedService;
  let httpService: HttpService;
  let helperService: HelperService;
  let excelService: ExcelService;
  let exportExcelComponent;
  const baseUrl = environment.baseUrl; // Servers Env
  const fakeDoraKpis = require('../../../test/resource/fakeDoraKpis.json');
  const fakeDoraKpiFilters = require('../../../test/resource/fakeDoraKpiFilters.json');
  const globalData = require('../../../test/resource/fakeGlobalConfigData.json');
  const fakeMasterData = require('../../../test/resource/fakeMasterData.json');

  const filterApplyDataWithNoFilter = {};
  const filterApplyDataWithScrum = {
    kpiList: [
      {
        id: '5d3013be4020938b42c23ba7',
        kpiId: 'kpi8',
        kpiName: 'Code Build Time',
        isDeleted: 'False',
        kpiCategory: 'Productivity',
        kpiUnit: 'min',
        kpiSource: 'Jenkins',
        maxValue: '100',
        kanban: false,
        chartType: 'gaugeChart',
      },
    ],
    ids: ['Speedy 2.0_62503_Speedy 2.0'],
    level: 3,
    selectedMap: {
      hierarchyLevelOne: ['ASDFG_hierarchyLevelOne'],
      Project: ['Speedy 2.0_62503_Speedy 2.0'],
      SubProject: [],
      Sprint: [],
      Build: [],
      Release: [],
      Squad: [],
      Individual: [],
    },
  };
  const filterApplyDataWithKanban = {
    kpiList: [
      {
        id: '5d3013be4020938b42c23bd0',
        kpiId: 'kpi66',
        kpiName: 'Code Build Time',
        isDeleted: 'False',
        kpiCategory: 'Productivity',
        kpiUnit: 'min',
        kpiSource: 'Jenkins',
        maxValue: '100',
        kanban: true,
        chartType: 'gaugeChart',
      },
    ],
    ids: ['Date Range'],
    level: 5,
    selectedMap: {
      hierarchyLevelOne: ['ASDFG_hierarchyLevelOne'],
      Project: [],
      SubProject: [],
      Date: ['Date Range'],
      Build: [],
      Release: [],
      Squad: [],
      Individual: [],
    },
    startDate: '2019-04-30T18:30:00.000Z',
    endDate: '2019-08-08T11:00:24.000Z',
  };
  const selectedTab = 'my-knowhow';

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
          code: 'C1',
        },
        {
          name: 'Corpate1',
          code: 'Corpate1',
        },
        {
          name: 'Leve1',
          code: 'Leve1',
        },
        {
          name: 'Org1',
          code: 'Org1',
        },
        {
          name: 'Orgc',
          code: 'Orgc',
        },
        {
          name: 'TESTS',
          code: 'TESTS',
        },
        {
          name: 'Test1',
          code: 'Test1',
        },
        {
          name: 'TestC',
          code: 'TestC',
        },
        {
          name: 'TestCorp',
          code: 'TestCorp',
        },
        {
          name: 'abcv',
          code: 'abcv',
        },
        {
          name: 'bittest',
          code: 'bittest',
        },
        {
          name: 'dfdsg',
          code: 'dfdsg',
        },
        {
          name: 'dgdfhfgjgh',
          code: 'dgdfhfgjgh',
        },
        {
          name: 'dgfdh',
          code: 'dgfdh',
        },
        {
          name: 'dgfg',
          code: 'dgfg',
        },
        {
          name: 'dghhjjh',
          code: 'dghhjjh',
        },
        {
          name: 'djfyyyyyyyyyyyyyyy',
          code: 'djfyyyyyyyyyyyyyyy',
        },
        {
          name: 'dsgfdj',
          code: 'dsgfdj',
        },
        {
          name: 'fghhhj',
          code: 'fghhhj',
        },
        {
          name: 'fhgkl',
          code: 'fhgkl',
        },
        {
          name: 'fhjjjjjj',
          code: 'fhjjjjjj',
        },
        {
          name: 'gfhygjhk',
          code: 'gfhygjhk',
        },
        {
          name: 'ghhjhkjl',
          code: 'ghhjhkjl',
        },
        {
          name: 'ghjk',
          code: 'ghjk',
        },
        {
          name: 'gjhfkjhkj',
          code: 'gjhfkjhkj',
        },
        {
          name: 'gjhjkk',
          code: 'gjhjkk',
        },
        {
          name: 'gjkjllf',
          code: 'gjkjllf',
        },
        {
          name: 'gjtykghk',
          code: 'gjtykghk',
        },
        {
          name: 'hgjhgjk',
          code: 'hgjhgjk',
        },
        {
          name: 'hjkk',
          code: 'hjkk',
        },
        {
          name: 'rduuuuuuuuu',
          code: 'rduuuuuuuuu',
        },
        {
          name: 'trrrrrrrrrrrrrrrrrrr',
          code: 'trrrrrrrrrrrrrrrrrrr',
        },
        {
          name: 'trt',
          code: 'trt',
        },
        {
          name: 'wdddddd',
          code: 'wdddddd',
        },
        {
          name: 'ytttttttttttt',
          code: 'ytttttttttttt',
        },
      ],
      value: '',
      required: true,
    },
    {
      level: 2,
      hierarchyLevelId: 'business',
      hierarchyLevelName: 'Business Name',
      suggestions: [
        {
          name: 'B1',
          code: 'B1',
        },
        {
          name: 'Bus1',
          code: 'Bus1',
        },
        {
          name: 'Leve2',
          code: 'Leve2',
        },
        {
          name: 'Org2',
          code: 'Org2',
        },
        {
          name: 'Orgb',
          code: 'Orgb',
        },
        {
          name: 'Test2',
          code: 'Test2',
        },
        {
          name: 'TestB',
          code: 'TestB',
        },
        {
          name: 'TestBus',
          code: 'TestBus',
        },
        {
          name: 'asfd',
          code: 'asfd',
        },
        {
          name: 'dcccccccccc',
          code: 'dcccccccccc',
        },
        {
          name: 'ddddddddddddd',
          code: 'ddddddddddddd',
        },
        {
          name: 'dfhhhhhhh',
          code: 'dfhhhhhhh',
        },
        {
          name: 'erhjjkkjkl',
          code: 'erhjjkkjkl',
        },
        {
          name: 'fbcncvn',
          code: 'fbcncvn',
        },
        {
          name: 'fdjfjk',
          code: 'fdjfjk',
        },
        {
          name: 'fgdsfgdh',
          code: 'fgdsfgdh',
        },
        {
          name: 'fhgjhkjk',
          code: 'fhgjhkjk',
        },
        {
          name: 'ggggggg',
          code: 'ggggggg',
        },
        {
          name: 'ghjjk',
          code: 'ghjjk',
        },
        {
          name: 'hjjhjk',
          code: 'hjjhjk',
        },
        {
          name: 'hjuy',
          code: 'hjuy',
        },
        {
          name: 'jhbjnk',
          code: 'jhbjnk',
        },
        {
          name: 'jhkjljkll',
          code: 'jhkjljkll',
        },
        {
          name: 'jhlkl',
          code: 'jhlkl',
        },
        {
          name: 'jnkmlkm',
          code: 'jnkmlkm',
        },
        {
          name: 'knj,n,m m,',
          code: 'knj,n,m m,',
        },
        {
          name: 'rrrrrrrrr',
          code: 'rrrrrrrrr',
        },
        {
          name: 'rrrrrrrrre',
          code: 'rrrrrrrrre',
        },
        {
          name: 'rytrujjjk',
          code: 'rytrujjjk',
        },
        {
          name: 'ryyhtfjghk',
          code: 'ryyhtfjghk',
        },
        {
          name: 'sdddddddddddd',
          code: 'sdddddddddddd',
        },
        {
          name: 'sgdgf',
          code: 'sgdgf',
        },
        {
          name: 't1',
          code: 't1',
        },
        {
          name: 'ttttttuiiiiiiii',
          code: 'ttttttuiiiiiiii',
        },
        {
          name: 'wAAAAAAAAAA',
          code: 'wAAAAAAAAAA',
        },
      ],
      value: '',
      required: true,
    },
    {
      level: 3,
      hierarchyLevelId: 'dummyaccount',
      hierarchyLevelName: 'dummyAccount Name',
      suggestions: [
        {
          name: 'A1',
          code: 'A1',
        },
        {
          name: 'Acc1',
          code: 'Acc1',
        },
        {
          name: 'Level3',
          code: 'Level3',
        },
        {
          name: 'Org3',
          code: 'Org3',
        },
        {
          name: 'Orga',
          code: 'Orga',
        },
        {
          name: 'Test3',
          code: 'Test3',
        },
        {
          name: 'TestAcc',
          code: 'TestAcc',
        },
        {
          name: 'TestC',
          code: 'TestC',
        },
        {
          name: 'WRRRRRRRRR',
          code: 'WRRRRRRRRR',
        },
        {
          name: 'bxccnbcvn',
          code: 'bxccnbcvn',
        },
        {
          name: 'ddddddddddddddddd',
          code: 'ddddddddddddddddd',
        },
        {
          name: 'dddst',
          code: 'dddst',
        },
        {
          name: 'dfdgfdh',
          code: 'dfdgfdh',
        },
        {
          name: 'dfsgdf',
          code: 'dfsgdf',
        },
        {
          name: 'eeeee',
          code: 'eeeee',
        },
        {
          name: 'erttyyuui',
          code: 'erttyyuui',
        },
        {
          name: 'fdddddddddddddddd',
          code: 'fdddddddddddddddd',
        },
        {
          name: 'gjhkjjl',
          code: 'gjhkjjl',
        },
        {
          name: 'gsdddddddddddg',
          code: 'gsdddddddddddg',
        },
        {
          name: 'hjl',
          code: 'hjl',
        },
        {
          name: 'hkjkjlkl',
          code: 'hkjkjlkl',
        },
        {
          name: 'hyjykjl',
          code: 'hyjykjl',
        },
        {
          name: 'jhjkhkk',
          code: 'jhjkhkk',
        },
        {
          name: 'jj,ddddw',
          code: 'jj,ddddw',
        },
        {
          name: 'jjkjkjhk',
          code: 'jjkjkjhk',
        },
        {
          name: 'kmmmk',
          code: 'kmmmk',
        },
        {
          name: 'mn',
          code: 'mn',
        },
        {
          name: 'shhhhhhhhh',
          code: 'shhhhhhhhh',
        },
        {
          name: 'sss',
          code: 'sss',
        },
        {
          name: 'ssssssssssss',
          code: 'ssssssssssss',
        },
        {
          name: 't2',
          code: 't2',
        },
        {
          name: 'tyui',
          code: 'tyui',
        },
        {
          name: 'wwgt',
          code: 'wwgt',
        },
        {
          name: 'xfnnnnnnnnn',
          code: 'xfnnnnnnnnn',
        },
        {
          name: 'yutruityi',
          code: 'yutruityi',
        },
      ],
      value: '',
      required: true,
    },
    {
      level: 4,
      hierarchyLevelId: 'dummysubaccount',
      hierarchyLevelName: 'dummySubaccount',
      suggestions: [
        {
          name: 'Level4',
          code: 'Level4',
        },
        {
          name: 'Org4',
          code: 'Org4',
        },
        {
          name: 'Orgs',
          code: 'Orgs',
        },
        {
          name: 'S1',
          code: 'S1',
        },
        {
          name: 'Sub1',
          code: 'Sub1',
        },
        {
          name: 'Test4',
          code: 'Test4',
        },
        {
          name: 'TestS',
          code: 'TestS',
        },
        {
          name: 'Testsub',
          code: 'Testsub',
        },
        {
          name: 'aaaaaaaaaaaaaaaaa',
          code: 'aaaaaaaaaaaaaaaaa',
        },
        {
          name: 'asc',
          code: 'asc',
        },
        {
          name: 'cbvcxcncvn',
          code: 'cbvcxcncvn',
        },
        {
          name: 'eeeeeeeeee',
          code: 'eeeeeeeeee',
        },
        {
          name: 'eeeeeeeeeeee',
          code: 'eeeeeeeeeeee',
        },
        {
          name: 'erweteryu',
          code: 'erweteryu',
        },
        {
          name: 'ffff',
          code: 'ffff',
        },
        {
          name: 'fhfd',
          code: 'fhfd',
        },
        {
          name: 'fhgjhk',
          code: 'fhgjhk',
        },
        {
          name: 'fhjkk',
          code: 'fhjkk',
        },
        {
          name: 'ghthhhhhhhhhht',
          code: 'ghthhhhhhhhhht',
        },
        {
          name: 'hjkhkjk',
          code: 'hjkhkjk',
        },
        {
          name: 'hkkkkkk',
          code: 'hkkkkkk',
        },
        {
          name: 'jhhjkjhkj',
          code: 'jhhjkjhkj',
        },
        {
          name: 'jhhvgvggv',
          code: 'jhhvgvggv',
        },
        {
          name: 'jkjkllk;k;',
          code: 'jkjkllk;k;',
        },
        {
          name: 'kmkkkk',
          code: 'kmkkkk',
        },
        {
          name: 'saaaaaaaaaaaaa',
          code: 'saaaaaaaaaaaaa',
        },
        {
          name: 'sasdfdgfgf',
          code: 'sasdfdgfgf',
        },
        {
          name: 'sdf',
          code: 'sdf',
        },
        {
          name: 'sdgggggggg',
          code: 'sdgggggggg',
        },
        {
          name: 'seeeeee',
          code: 'seeeeee',
        },
        {
          name: 'sytttu',
          code: 'sytttu',
        },
        {
          name: 't3',
          code: 't3',
        },
        {
          name: 'xgggggggggg',
          code: 'xgggggggggg',
        },
        {
          name: 'zfghg',
          code: 'zfghg',
        },
        {
          name: 'zzzzzzzzzzzzzzzzzzf',
          code: 'zzzzzzzzzzzzzzzzzzf',
        },
      ],
      value: '',
      required: true,
    },
  ];

  const fakeJenkins = [
    {
      kpiId: 'kpi8',
      kpiName: 'Code Build Time',
      unit: 'min',
      maxValue: '100',
      chartType: 'gaugeChart',
      id: '5d3013be4020938b42c23ba7',
      isDeleted: 'False',
      kpiCategory: 'Productivity',
      kpiUnit: 'min',
      kanban: false,
      kpiSource: 'Jenkins',
      trendValueList: [],
      maturityValue: '0',
      maturityRange: ['-360', '360-240', '240-120', '120-10', '10-'],
    },
  ];

  const fakeZypher = [
    {
      kpiId: 'kpi16',
      kpiName: 'In-Sprint Automation Coverage',
      value: 0,
      unit: '',
      maxValue: '100',
      chartType: 'gaugeChart',
      id: '5d3013be4020938b42c23bac',
      isDeleted: 'False',
      kpiCategory: 'Quality',
      kpiUnit: '',
      kanban: false,
      kpiSource: 'Zypher',
      thresholdValue: 80.0,
      trendValueList: [
        {
          data: 'Speedy 2.0',
          value: [
            {
              data: '0',
              sProjectName: 'Speedy 2.0',
              sSprintID: 'KPI dashboard Sprint 5_62503_Speedy 2.0',
              sSprintName: 'KPI dashboard Sprint 5_Speedy 2.0',
              value: 0.0,
              howerValue: { 'Automated Tests': 0, 'Total Tests': 0 },
              preCalculatedDataModel: { data: {} },
            },
            {
              data: '0',
              sProjectName: 'Speedy 2.0',
              sSprintID: 'KPI dashboard Sprint 18_62503_Speedy 2.0',
              sSprintName: 'KPI dashboard Sprint 18_Speedy 2.0',
              value: 0.0,
              howerValue: { 'Automated Tests': 0, 'Total Tests': 0 },
              preCalculatedDataModel: { data: {} },
            },
            {
              data: '0',
              sProjectName: 'Speedy 2.0',
              sSprintID: 'KPI dashboard Sprint 23_62503_Speedy 2.0',
              sSprintName: 'KPI dashboard Sprint 23_Speedy 2.0',
              value: 0.0,
              howerValue: { 'Automated Tests': 0, 'Total Tests': 0 },
              preCalculatedDataModel: { data: {} },
            },
            {
              data: '0',
              sProjectName: 'Speedy 2.0',
              sSprintID: 'KPI dashboard Sprint 32_62503_Speedy 2.0',
              sSprintName: 'KPI dashboard Sprint 32_Speedy 2.0',
              value: 0.0,
              howerValue: { 'Automated Tests': 0, 'Total Tests': 0 },
              preCalculatedDataModel: { data: {} },
            },
            {
              data: '0',
              sProjectName: 'Speedy 2.0',
              sSprintID: 'KPI dashboard Sprint 33_62503_Speedy 2.0',
              sSprintName: 'KPI dashboard Sprint 33_Speedy 2.0',
              value: 0.0,
              howerValue: { 'Automated Tests': 0, 'Total Tests': 0 },
              preCalculatedDataModel: { data: {} },
            },
            {
              data: '0',
              sProjectName: 'Speedy 2.0',
              sSprintID: 'KPI dashboard Sprint 34_62503_Speedy 2.0',
              sSprintName: 'KPI dashboard Sprint 34_Speedy 2.0',
              value: 0.0,
              howerValue: { 'Automated Tests': 0, 'Total Tests': 0 },
              preCalculatedDataModel: { data: {} },
            },
            {
              data: '0',
              sProjectName: 'Speedy 2.0',
              sSprintID: 'KPI dashboard Sprint 35_62503_Speedy 2.0',
              sSprintName: 'KPI dashboard Sprint 35_Speedy 2.0',
              value: 0.0,
              howerValue: { 'Automated Tests': 0, 'Total Tests': 0 },
              preCalculatedDataModel: { data: {} },
            },
            {
              data: '0',
              sProjectName: 'Speedy 2.0',
              sSprintID: 'DTI | 22 Jul - 04 Aug_62503_Speedy 2.0',
              sSprintName: 'DTI | 22 Jul - 04 Aug_Speedy 2.0',
              value: 0.0,
              howerValue: { 'Automated Tests': 0, 'Total Tests': 0 },
              preCalculatedDataModel: { data: {} },
            },
          ],
          preCalculatedDataModel: { data: {} },
        },
      ],
      maturityValue: '1',
      maturityRange: ['-20', '20-40', '40-60', '60-80', '80-'],
    },
    {
      kpiId: 'kpi42',
      kpiName: 'Regression Automation Coverage',
      value: 0,
      unit: '%',
      maxValue: '100',
      chartType: 'gaugeChart',
      id: '5d3013be4020938b42c23bb9',
      isDeleted: 'False',
      kpiCategory: 'Quality',
      kpiUnit: '%',
      kanban: false,
      kpiSource: 'Zypher',
      trendValueList: [
        {
          data: 'Speedy 2.0',
          value: [
            {
              data: '0.0',
              sProjectName: 'Speedy 2.0',
              sSprintID: 'KPI dashboard Sprint 5_62503_Speedy 2.0',
              sSprintName: 'KPI dashboard Sprint 5_Speedy 2.0',
              value: 0.0,
              howerValue: {},
              preCalculatedDataModel: { data: {} },
            },
            {
              data: '0.0',
              sProjectName: 'Speedy 2.0',
              sSprintID: 'KPI dashboard Sprint 18_62503_Speedy 2.0',
              sSprintName: 'KPI dashboard Sprint 18_Speedy 2.0',
              value: 0.0,
              howerValue: {},
              preCalculatedDataModel: { data: {} },
            },
            {
              data: '0.0',
              sProjectName: 'Speedy 2.0',
              sSprintID: 'KPI dashboard Sprint 23_62503_Speedy 2.0',
              sSprintName: 'KPI dashboard Sprint 23_Speedy 2.0',
              value: 0.0,
              howerValue: {},
              preCalculatedDataModel: { data: {} },
            },
            {
              data: '0.0',
              sProjectName: 'Speedy 2.0',
              sSprintID: 'KPI dashboard Sprint 32_62503_Speedy 2.0',
              sSprintName: 'KPI dashboard Sprint 32_Speedy 2.0',
              value: 0.0,
              howerValue: {},
              preCalculatedDataModel: { data: {} },
            },
            {
              data: '0.0',
              sProjectName: 'Speedy 2.0',
              sSprintID: 'KPI dashboard Sprint 33_62503_Speedy 2.0',
              sSprintName: 'KPI dashboard Sprint 33_Speedy 2.0',
              value: 0.0,
              howerValue: {},
              preCalculatedDataModel: { data: {} },
            },
            {
              data: '0.0',
              sProjectName: 'Speedy 2.0',
              sSprintID: 'KPI dashboard Sprint 34_62503_Speedy 2.0',
              sSprintName: 'KPI dashboard Sprint 34_Speedy 2.0',
              value: 0.0,
              howerValue: {},
              preCalculatedDataModel: { data: {} },
            },
            {
              data: '0.0',
              sProjectName: 'Speedy 2.0',
              sSprintID: 'KPI dashboard Sprint 35_62503_Speedy 2.0',
              sSprintName: 'KPI dashboard Sprint 35_Speedy 2.0',
              value: 0.0,
              howerValue: {},
              preCalculatedDataModel: { data: {} },
            },
            {
              data: '0.0',
              sProjectName: 'Speedy 2.0',
              sSprintID: 'DTI | 22 Jul - 04 Aug_62503_Speedy 2.0',
              sSprintName: 'DTI | 22 Jul - 04 Aug_Speedy 2.0',
              value: 0.0,
              howerValue: {},
              preCalculatedDataModel: { data: {} },
            },
          ],
          preCalculatedDataModel: { data: {} },
        },
      ],
      maturityValue: '1',
      maturityRange: ['-20', '20-40', '40-60', '60-80', '80-'],
    },
  ];

  const fakeBitBucket = [
    {
      kpiId: 'kpi11',
      kpiName: 'Number of check-ins per day in master',
      value: [],
      unit: 'check-ins',
      maxValue: '10',
      chartType: 'gaugeChart',
      id: '5d3013be4020938b42c23ba9',
      isDeleted: 'False',
      kpiCategory: 'Productivity',
      kpiUnit: 'check-ins',
      kanban: false,
      kpiSource: 'BitBucket',
      thresholdValue: 55.0,
      maturityRange: ['0', '2', '4', '8', '32'],
    },
  ];

  const fakeSonar = [
    {
      kpiId: 'kpi15',
      kpiName: 'Code Quality',
      value: {},
      chartType: 'gaugeChart',
      id: '5d3013be4020938b42c23bab',
      isDeleted: 'False',
      kpiCategory: 'Quality',
      kanban: false,
      kpiSource: 'Sonar',
      thresholdValue: 55.0,
      trendValueList: [],
      maturityValue: '0',
      maturityRange: ['E', 'D', 'C', 'B', 'A'],
    },
    {
      kpiId: 'kpi17',
      kpiName: 'Unit Testing',
      value: { aggregatedValue: '0.00' },
      unit: '%',
      maxValue: '100',
      chartType: 'gaugeChart',
      id: '5d3013be4020938b42c23bad',
      isDeleted: 'False',
      kpiCategory: 'Quality',
      kpiUnit: '%',
      kanban: false,
      kpiSource: 'Sonar',
      thresholdValue: 55.0,
      trendValueList: [],
      maturityValue: '5',
      maturityRange: ['-80', '80-60', '60-40', '40-20', '20-'],
    },
    {
      kpiId: 'kpi27',
      kpiName: 'Sonar Tech Debt',
      value: { aggregatedValue: 0 },
      unit: 'Days',
      maxValue: '90',
      chartType: 'gaugeChart',
      id: '5d3013be4020938b42c23baf',
      isDeleted: 'False',
      kpiCategory: 'Productivity',
      kpiUnit: 'Days',
      kanban: false,
      kpiSource: 'Sonar',
      thresholdValue: 55.0,
      trendValueList: [],
      maturityValue: '0',
      maturityRange: ['-80', '80-60', '60-40', '40-20', '20-'],
    },
    {
      kpiId: 'kpi38',
      kpiName: 'Sonar Violations',
      value: { aggregatedValue: [] },
      unit: 'Number',
      maxValue: '',
      chartType: 'gaugeChart',
      id: '5d3013be4020938b42c23bb5',
      isDeleted: 'False',
      kpiCategory: 'Quality',
      kpiUnit: 'Number',
      kanban: false,
      kpiSource: 'Sonar',
      thresholdValue: 55.0,
      trendValueList: [],
    },
  ];

  const fakeSonarKanban = [
    {
      kpiId: 'kpi61',
      kpiName: 'Code Quality',
      value: {},
      chartType: 'gaugeChart',
      id: '5d3013be4020938b42c23bcb',
      isDeleted: 'False',
      kpiCategory: 'Quality',
      kanban: true,
      kpiSource: 'Sonar',
      thresholdValue: 55.0,
      trendValueList: [],
      maturityValue: '0',
      maturityRange: ['E', 'D', 'C', 'B', 'A'],
    },
    {
      kpiId: 'kpi62',
      kpiName: 'Unit Testing',
      value: { aggregatedValue: '0.0' },
      unit: '%',
      maxValue: '100',
      chartType: 'gaugeChart',
      id: '5d3013be4020938b42c23bcc',
      isDeleted: 'False',
      kpiCategory: 'Quality',
      kpiUnit: '%',
      kanban: true,
      kpiSource: 'Sonar',
      thresholdValue: 55.0,
      trendValueList: [],
      maturityValue: '5',
      maturityRange: ['-20', '20-40', '40-60', '60-80', '80-'],
    },
    {
      kpiId: 'kpi64',
      kpiName: 'Sonar Violations',
      value: { aggregatedValue: [] },
      unit: 'Number',
      maxValue: '',
      chartType: 'gaugeChart',
      id: '5d3013be4020938b42c23bce',
      isDeleted: 'False',
      kpiCategory: 'Quality',
      kpiUnit: 'Number',
      kanban: true,
      kpiSource: 'Sonar',
      thresholdValue: 55.0,
      trendValueList: [],
    },
    {
      kpiId: 'kpi67',
      kpiName: 'Sonar Tech Debt',
      value: {},
      unit: 'Days',
      maxValue: '90',
      chartType: 'gaugeChart',
      id: '5d3013be4020938b42c23bd1',
      isDeleted: 'False',
      kpiCategory: 'Productivity',
      kpiUnit: 'Days',
      kanban: true,
      kpiSource: 'Sonar',
      thresholdValue: 55.0,
      trendValueList: [],
      maturityValue: '0',
      maturityRange: ['-80', '80-60', '60-40', '40-20', '20-'],
    },
  ];

  const fakeJenkinsKanban = [
    {
      kpiId: 'kpi66',
      kpiName: 'Code Build Time',
      unit: 'min',
      maxValue: '100',
      chartType: 'gaugeChart',
      id: '5d3013be4020938b42c23bd0',
      isDeleted: 'False',
      kpiCategory: 'Productivity',
      kpiUnit: 'min',
      kanban: true,
      kpiSource: 'Jenkins',
      trendValueList: [],
      maturityValue: '0',
      maturityRange: ['-360', '360-240', '240-120', '120-10', '10-'],
    },
  ];

  const fakeZypherKanban = [
    {
      kpiId: 'kpi63',
      kpiName: 'Regression Automation Coverage',
      value: 74,
      unit: '%',
      maxValue: '100',
      chartType: 'gaugeChart',
      id: '5d3013be4020938b42c23bcd',
      isDeleted: 'False',
      kpiCategory: 'Quality',
      kpiUnit: '%',
      kanban: true,
      kpiSource: 'Zypher',
      trendValueList: [
        {
          data: 'TESTNIS Pace',
          value: [
            {
              data: '0.0',
              sProjectName: 'TESTNIS Pace',
              value: 74.0,
              kanbanDate: '2019-08-04',
              howerValue: { 'Automated Tests': 0, 'Total Tests': 587 },
              preCalculatedDataModel: { data: {} },
            },
            {
              data: '0.0',
              sProjectName: 'TESTNIS Pace',
              value: 74.0,
              kanbanDate: '2019-08-05',
              howerValue: { 'Automated Tests': 0, 'Total Tests': 587 },
              preCalculatedDataModel: { data: {} },
            },
            {
              data: '0.0',
              sProjectName: 'TESTNIS Pace',
              value: 74.0,
              kanbanDate: '2019-08-06',
              howerValue: { 'Automated Tests': 0, 'Total Tests': 587 },
              preCalculatedDataModel: { data: {} },
            },
            {
              data: '0.0',
              sProjectName: 'TESTNIS Pace',
              value: 74.0,
              kanbanDate: '2019-08-07',
              howerValue: { 'Automated Tests': 0, 'Total Tests': 587 },
              preCalculatedDataModel: { data: {} },
            },
            {
              data: '0.0',
              sProjectName: 'TESTNIS Pace',
              value: 74.0,
              kanbanDate: '2019-08-08',
              howerValue: { 'Automated Tests': 0, 'Total Tests': 587 },
              preCalculatedDataModel: { data: {} },
            },
            {
              data: '0.0',
              sProjectName: 'TESTNIS Pace',
              value: 74.0,
              kanbanDate: '2019-08-09',
              howerValue: { 'Automated Tests': 0, 'Total Tests': 587 },
              preCalculatedDataModel: { data: {} },
            },
            {
              data: '0.0',
              sProjectName: 'TESTNIS Pace',
              value: 74.0,
              kanbanDate: '2019-08-10',
              howerValue: { 'Automated Tests': 0, 'Total Tests': 587 },
              preCalculatedDataModel: { data: {} },
            },
          ],
          preCalculatedDataModel: { data: {} },
        },
        {
          data: 'PACE Support Project',
          value: [
            {
              data: '0.0',
              sProjectName: 'PACE Support Project',
              value: 74.0,
              kanbanDate: '2019-08-04',
              howerValue: { 'Automated Tests': 0, 'Total Tests': 587 },
              preCalculatedDataModel: { data: {} },
            },
            {
              data: '0.0',
              sProjectName: 'PACE Support Project',
              value: 74.0,
              kanbanDate: '2019-08-05',
              howerValue: { 'Automated Tests': 0, 'Total Tests': 587 },
              preCalculatedDataModel: { data: {} },
            },
            {
              data: '0.0',
              sProjectName: 'PACE Support Project',
              value: 74.0,
              kanbanDate: '2019-08-06',
              howerValue: { 'Automated Tests': 0, 'Total Tests': 587 },
              preCalculatedDataModel: { data: {} },
            },
            {
              data: '0.0',
              sProjectName: 'PACE Support Project',
              value: 74.0,
              kanbanDate: '2019-08-07',
              howerValue: { 'Automated Tests': 0, 'Total Tests': 587 },
              preCalculatedDataModel: { data: {} },
            },
            {
              data: '0.0',
              sProjectName: 'PACE Support Project',
              value: 74.0,
              kanbanDate: '2019-08-08',
              howerValue: { 'Automated Tests': 0, 'Total Tests': 587 },
              preCalculatedDataModel: { data: {} },
            },
            {
              data: '0.0',
              sProjectName: 'PACE Support Project',
              value: 74.0,
              kanbanDate: '2019-08-09',
              howerValue: { 'Automated Tests': 0, 'Total Tests': 587 },
              preCalculatedDataModel: { data: {} },
            },
            {
              data: '0.0',
              sProjectName: 'PACE Support Project',
              value: 74.0,
              kanbanDate: '2019-08-10',
              howerValue: { 'Automated Tests': 0, 'Total Tests': 587 },
              preCalculatedDataModel: { data: {} },
            },
          ],
          preCalculatedDataModel: { data: {} },
        },
        {
          data: 'Helios DevOps Support',
          value: [
            {
              data: '0.0',
              sProjectName: 'Helios DevOps Support',
              value: 74.0,
              kanbanDate: '2019-08-04',
              howerValue: { 'Automated Tests': 0, 'Total Tests': 587 },
              preCalculatedDataModel: { data: {} },
            },
            {
              data: '0.0',
              sProjectName: 'Helios DevOps Support',
              value: 74.0,
              kanbanDate: '2019-08-05',
              howerValue: { 'Automated Tests': 0, 'Total Tests': 587 },
              preCalculatedDataModel: { data: {} },
            },
            {
              data: '0.0',
              sProjectName: 'Helios DevOps Support',
              value: 74.0,
              kanbanDate: '2019-08-06',
              howerValue: { 'Automated Tests': 0, 'Total Tests': 587 },
              preCalculatedDataModel: { data: {} },
            },
            {
              data: '0.0',
              sProjectName: 'Helios DevOps Support',
              value: 74.0,
              kanbanDate: '2019-08-07',
              howerValue: { 'Automated Tests': 0, 'Total Tests': 587 },
              preCalculatedDataModel: { data: {} },
            },
            {
              data: '0.0',
              sProjectName: 'Helios DevOps Support',
              value: 74.0,
              kanbanDate: '2019-08-08',
              howerValue: { 'Automated Tests': 0, 'Total Tests': 587 },
              preCalculatedDataModel: { data: {} },
            },
            {
              data: '0.0',
              sProjectName: 'Helios DevOps Support',
              value: 74.0,
              kanbanDate: '2019-08-09',
              howerValue: { 'Automated Tests': 0, 'Total Tests': 587 },
              preCalculatedDataModel: { data: {} },
            },
            {
              data: '0.0',
              sProjectName: 'Helios DevOps Support',
              value: 74.0,
              kanbanDate: '2019-08-10',
              howerValue: { 'Automated Tests': 0, 'Total Tests': 587 },
              preCalculatedDataModel: { data: {} },
            },
          ],
          preCalculatedDataModel: { data: {} },
        },
      ],
      maturityValue: '4',
      maturityRange: ['-20', '20-40', '40-60', '60-80', '80-'],
    },
  ];

  const fakejiraKanban = [
    {
      kpiId: 'kpi82',
      kpiName: 'First Time Pass Rate',
      unit: '%',
      maxValue: '100',
      chartType: '',
      kpiInfo: {
        definition:
          'FIRST TIME PASS RATE measures the percentage of tickets that pass QA first time (without stimulating a return transition or defect tagged)',
        formula: [
          {
            lhs: 'First time pass rate (FTPR) for a Sprint',
            operator: 'division',
            operands: [
              'No. of issues closed in a sprint which do not have a return transition or any defects tagged',
              'Total no. of issues closed in the sprint',
            ],
          },
        ],
        details: [
          {
            type: 'paragraph',
            value:
              'It is calculated as a ‘Percentage’. Higher the percentage, better is the ‘Quality’',
          },
          {
            type: 'paragraph',
            value:
              'A progress indicator shows trend of first time pass rate between last 2 sprints. An upward trend is considered positive',
          },
          {
            type: 'paragraph',
            value:
              'Maturity of the KPI is calculated based on the average of the last 5 sprints',
          },
          {
            type: 'paragraph',
            value:
              '*If the KPI data is not available for last 5 sprints, the Maturity level will not be shown',
          },
        ],
        maturityLevels: [
          {
            level: 'M5',
            bgColor: '#6cab61',
            range: '>=90%',
          },
          {
            level: 'M4',
            bgColor: '#AEDB76',
            range: '>=75-90%',
          },
          {
            level: 'M3',
            bgColor: '#eff173',
            range: '>=50-75%',
          },
          {
            level: 'M2',
            bgColor: '#ffc35b',
            range: '>=25-50%',
          },
          {
            level: 'M1',
            bgColor: '#F06667',
            range: '< 25%',
          },
        ],
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
                'Closed Stories': 9,
              },
              sprintIds: ['40203_Scrum Project_6335363749794a18e8a4479b'],
              sprintNames: ['TEST |Test1 |ITR_1|OpenSource_Scrum Project'],
              sprojectName: 'Scrum Project',
            },
            {
              data: '67',
              sSprintID: '38295_Scrum Project_6335363749794a18e8a4479b',
              sSprintName: 'Test1|PI_10|ITR_5| 24th Aug_Scrum Project',
              value: 66.67,
              hoverValue: {
                'FTP Stories': 2,
                'Closed Stories': 3,
              },
              sprintIds: ['38295_Scrum Project_6335363749794a18e8a4479b'],
              sprintNames: ['Test1|PI_10|ITR_5| 24th Aug_Scrum Project'],
              sprojectName: 'Scrum Project',
            },
            {
              data: '0',
              sSprintID: '38294_Scrum Project_6335363749794a18e8a4479b',
              sSprintName: 'Test1|PI_10|ITR_4| 10th Aug_Scrum Project',
              value: 0,
              sprintIds: ['38294_Scrum Project_6335363749794a18e8a4479b'],
              sprintNames: ['Test1|PI_10|ITR_4| 10th Aug_Scrum Project'],
              sprojectName: 'Scrum Project',
            },
            {
              data: '100',
              sSprintID: '38296_Scrum Project_6335363749794a18e8a4479b',
              sSprintName: 'Test1|PI_10|ITR_6| 07th Sep_Scrum Project',
              value: 100,
              hoverValue: {
                'FTP Stories': 5,
                'Closed Stories': 5,
              },
              sprintIds: ['38296_Scrum Project_6335363749794a18e8a4479b'],
              sprintNames: ['Test1|PI_10|ITR_6| 07th Sep_Scrum Project'],
              sprojectName: 'Scrum Project',
            },
            {
              data: '70',
              sSprintID: '40345_Scrum Project_6335363749794a18e8a4479b',
              sSprintName: 'TEST| Test1|PI_10|Opensource_Scrum Project',
              value: 70,
              hoverValue: {
                'FTP Stories': 14,
                'Closed Stories': 20,
              },
              sprintIds: ['40345_Scrum Project_6335363749794a18e8a4479b'],
              sprintNames: ['TEST| Test1|PI_10|Opensource_Scrum Project'],
              sprojectName: 'Scrum Project',
            },
          ],
          maturity: '3',
        },
      ],
      maturityRange: ['-25', '25-50', '50-75', '75-90', '90-'],
      groupId: 1,
    },
    {
      kpiId: 'kpi111',
      kpiName: 'Defect Density',
      unit: '%',
      maxValue: '500',
      chartType: '',
      kpiInfo: {
        definition:
          'DEFECT DENSITY measures the total number of defects against the size of a story',
        formula: [
          {
            lhs: 'Defect Density',
            operator: 'division',
            operands: [
              'No. of defects tagged to all stories closed in a sprint',
              'Total size of stories closed in the sprint',
            ],
          },
        ],
        details: [
          {
            type: 'paragraph',
            value:
              "The KPI is applicable only if the estimation is being done in 'STory Points'",
          },
          {
            type: 'paragraph',
            value:
              'It is calculated as a ‘Percentage’. Lower the percentage, better is the ‘Quality’',
          },
          {
            type: 'paragraph',
            value:
              'A progress indicator shows trend of defect density between last 2 sprints. A downward trend is considered positive',
          },
          {
            type: 'paragraph',
            value:
              'Maturity of the KPI is calculated based on the average of the last 5 sprints',
          },
          {
            type: 'paragraph',
            value:
              'If the KPI data is not available for last 5 sprints, the Maturity level will not be shown',
          },
        ],
        maturityLevels: [
          {
            level: 'M5',
            bgColor: '#6cab61',
            range: '<10%',
          },
          {
            level: 'M4',
            bgColor: '#AEDB76',
            range: '10%-25%',
          },
          {
            level: 'M3',
            bgColor: '#eff173',
            range: '25%-60%',
          },
          {
            level: 'M2',
            bgColor: '#ffc35b',
            range: '60% -90%',
          },
          {
            level: 'M1',
            bgColor: '#F06667',
            range: '>90%',
          },
        ],
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
                'Size of Closed Stories': 25,
              },
              sprintIds: ['40203_Scrum Project_6335363749794a18e8a4479b'],
              sprintNames: ['TEST |Test1 |ITR_1|OpenSource_Scrum Project'],
              sprojectName: 'Scrum Project',
            },
            {
              data: '74',
              sSprintID: '38295_Scrum Project_6335363749794a18e8a4479b',
              sSprintName: 'Test1|PI_10|ITR_5| 24th Aug_Scrum Project',
              value: 74.07,
              hoverValue: {
                Defects: 11,
                'Size of Closed Stories': 27,
              },
              sprintIds: ['38295_Scrum Project_6335363749794a18e8a4479b'],
              sprintNames: ['Test1|PI_10|ITR_5| 24th Aug_Scrum Project'],
              sprojectName: 'Scrum Project',
            },
            {
              data: '67',
              sSprintID: '38294_Scrum Project_6335363749794a18e8a4479b',
              sSprintName: 'Test1|PI_10|ITR_4| 10th Aug_Scrum Project',
              value: 66.67,
              hoverValue: {
                Defects: 1,
                'Size of Closed Stories': 3,
              },
              sprintIds: ['38294_Scrum Project_6335363749794a18e8a4479b'],
              sprintNames: ['Test1|PI_10|ITR_4| 10th Aug_Scrum Project'],
              sprojectName: 'Scrum Project',
            },
            {
              data: '13',
              sSprintID: '38296_Scrum Project_6335363749794a18e8a4479b',
              sSprintName: 'Test1|PI_10|ITR_6| 07th Sep_Scrum Project',
              value: 13.33,
              hoverValue: {
                Defects: 1,
                'Size of Closed Stories': 15,
              },
              sprintIds: ['38296_Scrum Project_6335363749794a18e8a4479b'],
              sprintNames: ['Test1|PI_10|ITR_6| 07th Sep_Scrum Project'],
              sprojectName: 'Scrum Project',
            },
            {
              data: '59',
              sSprintID: '40345_Scrum Project_6335363749794a18e8a4479b',
              sSprintName: 'TEST| Test1|PI_10|Opensource_Scrum Project',
              value: 58.54,
              hoverValue: {
                Defects: 12,
                'Size of Closed Stories': 41,
              },
              sprintIds: ['40345_Scrum Project_6335363749794a18e8a4479b'],
              sprintNames: ['TEST| Test1|PI_10|Opensource_Scrum Project'],
              sprojectName: 'Scrum Project',
            },
          ],
          maturity: '3',
        },
      ],
      maturityRange: ['-90', '90-60', '60-25', '25-10', '10-'],
      groupId: 1,
    },
    {
      kpiId: 'kpi35',
      kpiName: 'Defect Seepage Rate',
      unit: '%',
      maxValue: '100',
      chartType: '',
      kpiInfo: {
        definition:
          'DEFECT SEEPAGE RATE measures the percentage of defects leaked from the current testing stage to the subsequent stage',
        formula: [
          {
            lhs: 'DSR for a sprint',
            operator: 'division',
            operands: [
              'No. of  valid defects reported at a stage (e.g. UAT)',
              ' Total no. of defects reported in the current stage and previous stage (UAT & QA)',
            ],
          },
        ],
        details: [
          {
            type: 'paragraph',
            value:
              'It is calculated as a ‘Percentage’. Lesser the percentage, better is the ‘Quality’',
          },
          {
            type: 'paragraph',
            value:
              'A progress indicator shows trend of defect seepage rate between last 2 sprints. A downward trend is considered positive',
          },
          {
            type: 'paragraph',
            value:
              'Maturity of the KPI is calculated based on the average of the last 5 sprints',
          },
          {
            type: 'paragraph',
            value:
              '*If the KPI data is not available for last 5 sprints, the Maturity level will not be shown',
          },
        ],
        maturityLevels: [
          {
            level: 'M5',
            bgColor: '#6cab61',
            range: '<25%',
          },
          {
            level: 'M4',
            bgColor: '#AEDB76',
            range: '>=25-50%',
          },
          {
            level: 'M3',
            bgColor: '#eff173',
            range: '>=50-75%',
          },
          {
            level: 'M2',
            bgColor: '#ffc35b',
            range: '>=75-90%',
          },
          {
            level: 'M1',
            bgColor: '#F06667',
            range: '>=90%',
          },
        ],
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
                'Total Defects': 0,
              },
              sprintIds: ['40203_Scrum Project_6335363749794a18e8a4479b'],
              sprintNames: ['TEST |Test1 |ITR_1|OpenSource_Scrum Project'],
              sprojectName: 'Scrum Project',
            },
            {
              data: '0',
              sSprintID: '38295_Scrum Project_6335363749794a18e8a4479b',
              sSprintName: 'Test1|PI_10|ITR_5| 24th Aug_Scrum Project',
              value: 0,
              hoverValue: {
                'Escaped Defects': 0,
                'Total Defects': 11,
              },
              sprintIds: ['38295_Scrum Project_6335363749794a18e8a4479b'],
              sprintNames: ['Test1|PI_10|ITR_5| 24th Aug_Scrum Project'],
              sprojectName: 'Scrum Project',
            },
            {
              data: '0',
              sSprintID: '38294_Scrum Project_6335363749794a18e8a4479b',
              sSprintName: 'Test1|PI_10|ITR_4| 10th Aug_Scrum Project',
              value: 0,
              hoverValue: {
                'Escaped Defects': 0,
                'Total Defects': 1,
              },
              sprintIds: ['38294_Scrum Project_6335363749794a18e8a4479b'],
              sprintNames: ['Test1|PI_10|ITR_4| 10th Aug_Scrum Project'],
              sprojectName: 'Scrum Project',
            },
            {
              data: '0',
              sSprintID: '38296_Scrum Project_6335363749794a18e8a4479b',
              sSprintName: 'Test1|PI_10|ITR_6| 07th Sep_Scrum Project',
              value: 0,
              hoverValue: {
                'Escaped Defects': 0,
                'Total Defects': 1,
              },
              sprintIds: ['38296_Scrum Project_6335363749794a18e8a4479b'],
              sprintNames: ['Test1|PI_10|ITR_6| 07th Sep_Scrum Project'],
              sprojectName: 'Scrum Project',
            },
            {
              data: '0',
              sSprintID: '40345_Scrum Project_6335363749794a18e8a4479b',
              sSprintName: 'TEST| Test1|PI_10|Opensource_Scrum Project',
              value: 0,
              hoverValue: {
                'Escaped Defects': 0,
                'Total Defects': 12,
              },
              sprintIds: ['40345_Scrum Project_6335363749794a18e8a4479b'],
              sprintNames: ['TEST| Test1|PI_10|Opensource_Scrum Project'],
              sprojectName: 'Scrum Project',
            },
          ],
          maturity: '5',
        },
      ],
      maturityRange: ['-90', '90-75', '75-50', '50-25', '25-'],
      groupId: 1,
    },
    {
      kpiId: 'kpi28',
      kpiName: 'Defect Count By Priority',
      unit: 'Number',
      maxValue: '90',
      chartType: '',
      kpiInfo: {
        definition:
          'DEFECT COUNT BY PRIORITY measures number of defects for each priority defined in a project',
        formula: [
          {
            lhs: 'Defect Count By Priority',
          },
          {
            rhs: 'No. of defects linked to stories grouped by priority',
          },
        ],
        details: [
          {
            type: 'paragraph',
            value:
              'It is calculated as ‘Count’. Lower the count, better is the ‘Quality’',
          },
          {
            type: 'paragraph',
            value:
              'A progress indicator shows trend of defect count by priority between last 2 sprints. A downward trend is considered positive',
          },
        ],
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
                    P4: 0,
                  },
                  kpiGroup: 'Overall',
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '11',
                  sSprintID: '38295_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'Test1|PI_10|ITR_5| 24th Aug_Scrum Project',
                  value: 11,
                  hoverValue: {
                    P2: 3,
                    P3: 5,
                    P4: 3,
                  },
                  kpiGroup: 'Overall',
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '1',
                  sSprintID: '38294_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'Test1|PI_10|ITR_4| 10th Aug_Scrum Project',
                  value: 1,
                  hoverValue: {
                    P2: 0,
                    P3: 1,
                    P4: 0,
                  },
                  kpiGroup: 'Overall',
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '1',
                  sSprintID: '38296_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'Test1|PI_10|ITR_6| 07th Sep_Scrum Project',
                  value: 1,
                  hoverValue: {
                    P2: 0,
                    P3: 1,
                    P4: 0,
                  },
                  kpiGroup: 'Overall',
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '12',
                  sSprintID: '40345_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'TEST| Test1|PI_10|Opensource_Scrum Project',
                  value: 12,
                  hoverValue: {
                    P2: 0,
                    P3: 3,
                    P4: 9,
                  },
                  kpiGroup: 'Overall',
                  sprojectName: 'Scrum Project',
                },
              ],
            },
          ],
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
                    P2: 0,
                  },
                  kpiGroup: 'P2',
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '3',
                  sSprintID: '38295_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'Test1|PI_10|ITR_5| 24th Aug_Scrum Project',
                  value: 3,
                  hoverValue: {
                    P2: 3,
                  },
                  kpiGroup: 'P2',
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '0',
                  sSprintID: '38294_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'Test1|PI_10|ITR_4| 10th Aug_Scrum Project',
                  value: 0,
                  hoverValue: {
                    P2: 0,
                  },
                  kpiGroup: 'P2',
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '0',
                  sSprintID: '38296_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'Test1|PI_10|ITR_6| 07th Sep_Scrum Project',
                  value: 0,
                  hoverValue: {
                    P2: 0,
                  },
                  kpiGroup: 'P2',
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '0',
                  sSprintID: '40345_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'TEST| Test1|PI_10|Opensource_Scrum Project',
                  value: 0,
                  hoverValue: {
                    P2: 0,
                  },
                  kpiGroup: 'P2',
                  sprojectName: 'Scrum Project',
                },
              ],
            },
          ],
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
                    P3: 0,
                  },
                  kpiGroup: 'P3',
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '5',
                  sSprintID: '38295_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'Test1|PI_10|ITR_5| 24th Aug_Scrum Project',
                  value: 5,
                  hoverValue: {
                    P3: 5,
                  },
                  kpiGroup: 'P3',
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '1',
                  sSprintID: '38294_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'Test1|PI_10|ITR_4| 10th Aug_Scrum Project',
                  value: 1,
                  hoverValue: {
                    P3: 1,
                  },
                  kpiGroup: 'P3',
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '1',
                  sSprintID: '38296_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'Test1|PI_10|ITR_6| 07th Sep_Scrum Project',
                  value: 1,
                  hoverValue: {
                    P3: 1,
                  },
                  kpiGroup: 'P3',
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '3',
                  sSprintID: '40345_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'TEST| Test1|PI_10|Opensource_Scrum Project',
                  value: 3,
                  hoverValue: {
                    P3: 3,
                  },
                  kpiGroup: 'P3',
                  sprojectName: 'Scrum Project',
                },
              ],
            },
          ],
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
                    P4: 0,
                  },
                  kpiGroup: 'P4',
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '3',
                  sSprintID: '38295_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'Test1|PI_10|ITR_5| 24th Aug_Scrum Project',
                  value: 3,
                  hoverValue: {
                    P4: 3,
                  },
                  kpiGroup: 'P4',
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '0',
                  sSprintID: '38294_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'Test1|PI_10|ITR_4| 10th Aug_Scrum Project',
                  value: 0,
                  hoverValue: {
                    P4: 0,
                  },
                  kpiGroup: 'P4',
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '0',
                  sSprintID: '38296_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'Test1|PI_10|ITR_6| 07th Sep_Scrum Project',
                  value: 0,
                  hoverValue: {
                    P4: 0,
                  },
                  kpiGroup: 'P4',
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '9',
                  sSprintID: '40345_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'TEST| Test1|PI_10|Opensource_Scrum Project',
                  value: 9,
                  hoverValue: {
                    P4: 9,
                  },
                  kpiGroup: 'P4',
                  sprojectName: 'Scrum Project',
                },
              ],
            },
          ],
        },
      ],
      groupId: 1,
    },
    {
      kpiId: 'kpi83',
      kpiName: 'Average Resolution Time',
      unit: 'Days',
      maxValue: '100',
      chartType: '',
      kpiInfo: {
        definition:
          'AVERAGE RESOLUTION TIME measures average time taken to complete an issue that could be a story or bug etc.',
        formula: [
          {
            lhs: 'Sum of resolution times of all issues completed in the Sprint/No. of issues completed within a sprint',
          },
        ],
        details: [
          {
            type: 'paragraph',
            value:
              'It is calculated as a ‘Days’. Fewer the days better is the ‘Speed’',
          },
          {
            type: 'paragraph',
            value:
              'A progress indicator shows trend of Average Resolution Time between last 2 sprints. A downward trend is considered positive',
          },
          {
            type: 'paragraph',
            value:
              'Maturity of the KPI is calculated based on the average of the last 5 sprints',
          },
          {
            type: 'paragraph',
            value:
              '*If the KPI data is not available for last 5 sprints, the Maturity level will not be shown',
          },
        ],
        maturityLevels: [
          {
            level: 'M5',
            bgColor: '#6cab61',
            range: '<= 3 days',
          },
          {
            level: 'M4',
            bgColor: '#AEDB76',
            range: '3-5 days',
          },
          {
            level: 'M3',
            bgColor: '#eff173',
            range: '5-8 days',
          },
          {
            level: 'M2',
            bgColor: '#ffc35b',
            range: '8-10 days',
          },
          {
            level: 'M1',
            bgColor: '#F06667',
            range: '=> 10 days',
          },
        ],
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
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '2',
                  sSprintID: '38295_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'Test1|PI_10|ITR_5| 24th Aug_Scrum Project',
                  value: 1.76,
                  hoverValue: {},
                  kpiGroup: 'Overall',
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '2',
                  sSprintID: '38294_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'Test1|PI_10|ITR_4| 10th Aug_Scrum Project',
                  value: 1.54,
                  hoverValue: {},
                  kpiGroup: 'Overall',
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '3',
                  sSprintID: '38296_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'Test1|PI_10|ITR_6| 07th Sep_Scrum Project',
                  value: 2.62,
                  hoverValue: {},
                  kpiGroup: 'Overall',
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '5',
                  sSprintID: '40345_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'TEST| Test1|PI_10|Opensource_Scrum Project',
                  value: 5.18,
                  hoverValue: {},
                  kpiGroup: 'Overall',
                  sprojectName: 'Scrum Project',
                },
              ],
              maturity: '5',
            },
          ],
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
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '1',
                  sSprintID: '38295_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'Test1|PI_10|ITR_5| 24th Aug_Scrum Project',
                  value: 1.44,
                  hoverValue: {},
                  kpiGroup: 'Bug',
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '2',
                  sSprintID: '38294_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'Test1|PI_10|ITR_4| 10th Aug_Scrum Project',
                  value: 1.54,
                  hoverValue: {},
                  kpiGroup: 'Bug',
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '1',
                  sSprintID: '38296_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'Test1|PI_10|ITR_6| 07th Sep_Scrum Project',
                  value: 1.38,
                  hoverValue: {},
                  kpiGroup: 'Bug',
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '1',
                  sSprintID: '40345_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'TEST| Test1|PI_10|Opensource_Scrum Project',
                  value: 1.43,
                  hoverValue: {},
                  kpiGroup: 'Bug',
                  sprojectName: 'Scrum Project',
                },
              ],
              maturity: '5',
            },
          ],
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
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '0',
                  sSprintID: '38295_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'Test1|PI_10|ITR_5| 24th Aug_Scrum Project',
                  value: 0,
                  hoverValue: {},
                  kpiGroup: 'Change request',
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '0',
                  sSprintID: '38294_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'Test1|PI_10|ITR_4| 10th Aug_Scrum Project',
                  value: 0,
                  hoverValue: {},
                  kpiGroup: 'Change request',
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '0',
                  sSprintID: '38296_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'Test1|PI_10|ITR_6| 07th Sep_Scrum Project',
                  value: 0,
                  hoverValue: {},
                  kpiGroup: 'Change request',
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '0',
                  sSprintID: '40345_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'TEST| Test1|PI_10|Opensource_Scrum Project',
                  value: 0,
                  hoverValue: {},
                  kpiGroup: 'Change request',
                  sprojectName: 'Scrum Project',
                },
              ],
              maturity: '5',
            },
          ],
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
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '8',
                  sSprintID: '38295_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'Test1|PI_10|ITR_5| 24th Aug_Scrum Project',
                  value: 8,
                  hoverValue: {},
                  kpiGroup: 'Enabler Story',
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '0',
                  sSprintID: '38294_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'Test1|PI_10|ITR_4| 10th Aug_Scrum Project',
                  value: 0,
                  hoverValue: {},
                  kpiGroup: 'Enabler Story',
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '0',
                  sSprintID: '38296_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'Test1|PI_10|ITR_6| 07th Sep_Scrum Project',
                  value: 0,
                  hoverValue: {},
                  kpiGroup: 'Enabler Story',
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '0',
                  sSprintID: '40345_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'TEST| Test1|PI_10|Opensource_Scrum Project',
                  value: 0,
                  hoverValue: {},
                  kpiGroup: 'Enabler Story',
                  sprojectName: 'Scrum Project',
                },
              ],
              maturity: '5',
            },
          ],
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
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '0',
                  sSprintID: '38295_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'Test1|PI_10|ITR_5| 24th Aug_Scrum Project',
                  value: 0,
                  hoverValue: {},
                  kpiGroup: 'Epic',
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '0',
                  sSprintID: '38294_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'Test1|PI_10|ITR_4| 10th Aug_Scrum Project',
                  value: 0,
                  hoverValue: {},
                  kpiGroup: 'Epic',
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '0',
                  sSprintID: '38296_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'Test1|PI_10|ITR_6| 07th Sep_Scrum Project',
                  value: 0,
                  hoverValue: {},
                  kpiGroup: 'Epic',
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '0',
                  sSprintID: '40345_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'TEST| Test1|PI_10|Opensource_Scrum Project',
                  value: 0,
                  hoverValue: {},
                  kpiGroup: 'Epic',
                  sprojectName: 'Scrum Project',
                },
              ],
              maturity: '5',
            },
          ],
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
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '4',
                  sSprintID: '38295_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'Test1|PI_10|ITR_5| 24th Aug_Scrum Project',
                  value: 4,
                  hoverValue: {},
                  kpiGroup: 'Story',
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '0',
                  sSprintID: '38294_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'Test1|PI_10|ITR_4| 10th Aug_Scrum Project',
                  value: 0,
                  hoverValue: {},
                  kpiGroup: 'Story',
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '5',
                  sSprintID: '38296_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'Test1|PI_10|ITR_6| 07th Sep_Scrum Project',
                  value: 4.6,
                  hoverValue: {},
                  kpiGroup: 'Story',
                  sprojectName: 'Scrum Project',
                },
                {
                  data: '8',
                  sSprintID: '40345_Scrum Project_6335363749794a18e8a4479b',
                  sSprintName: 'TEST| Test1|PI_10|Opensource_Scrum Project',
                  value: 7.8,
                  hoverValue: {},
                  kpiGroup: 'Story',
                  sprojectName: 'Scrum Project',
                },
              ],
              maturity: '4',
            },
          ],
        },
      ],
      maturityRange: ['-10', '10-8', '8-5', '5-3', '3-'],
      groupId: 1,
    },
    {
      kpiId: 'kpi126',
      kpiName: 'Created vs Resolved defects',
      unit: 'Number',
      maxValue: '300',
      chartType: '',
      kpiInfo: {
        definition:
          'Created vs Resolved defects gives a view of closed defects in an iteration vs planned + added defects in the iteration. The aim is to close all the defects that are in the iteration.',
        details: [
          {
            type: 'paragraph',
            value:
              'If the No. of defects resolved are equal to the No. of defects created in the latest sprint, the KPI is considered having a positive trend.',
          },
        ],
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
                createdDefects: 1,
              },
              sprintIds: ['40203_Scrum Project_6335363749794a18e8a4479b'],
              sprintNames: ['TEST |Test1 |ITR_1|OpenSource_Scrum Project'],
              lineValue: 0,
              sprojectName: 'Scrum Project',
            },
            {
              data: '31',
              sSprintID: '38295_Scrum Project_6335363749794a18e8a4479b',
              sSprintName: 'Test1|PI_10|ITR_5| 24th Aug_Scrum Project',
              value: 31,
              hoverValue: {
                resolvedDefects: 29,
                createdDefects: 31,
              },
              sprintIds: ['38295_Scrum Project_6335363749794a18e8a4479b'],
              sprintNames: ['Test1|PI_10|ITR_5| 24th Aug_Scrum Project'],
              lineValue: 29,
              sprojectName: 'Scrum Project',
            },
            {
              data: '30',
              sSprintID: '38294_Scrum Project_6335363749794a18e8a4479b',
              sSprintName: 'Test1|PI_10|ITR_4| 10th Aug_Scrum Project',
              value: 30,
              hoverValue: {
                resolvedDefects: 22,
                createdDefects: 30,
              },
              sprintIds: ['38294_Scrum Project_6335363749794a18e8a4479b'],
              sprintNames: ['Test1|PI_10|ITR_4| 10th Aug_Scrum Project'],
              lineValue: 22,
              sprojectName: 'Scrum Project',
            },
            {
              data: '10',
              sSprintID: '38296_Scrum Project_6335363749794a18e8a4479b',
              sSprintName: 'Test1|PI_10|ITR_6| 07th Sep_Scrum Project',
              value: 10,
              hoverValue: {
                resolvedDefects: 7,
                createdDefects: 10,
              },
              sprintIds: ['38296_Scrum Project_6335363749794a18e8a4479b'],
              sprintNames: ['Test1|PI_10|ITR_6| 07th Sep_Scrum Project'],
              lineValue: 7,
              sprojectName: 'Scrum Project',
            },
            {
              data: '20',
              sSprintID: '40345_Scrum Project_6335363749794a18e8a4479b',
              sSprintName: 'TEST| Test1|PI_10|Opensource_Scrum Project',
              value: 20,
              hoverValue: {
                resolvedDefects: 14,
                createdDefects: 20,
              },
              sprintIds: ['40345_Scrum Project_6335363749794a18e8a4479b'],
              sprintNames: ['TEST| Test1|PI_10|Opensource_Scrum Project'],
              lineValue: 14,
              sprojectName: 'Scrum Project',
            },
          ],
        },
      ],
      groupId: 1,
    },
    {
      kpiId: 'kpi46',
      kpiName: 'Sprint Capacity Utilization',
      unit: 'Hours',
      maxValue: '500',
      chartType: '',
      kpiInfo: {
        definition:
          'SPRINT CAPACITY UTILIZATION depicts the maximum amount of time a team can commit within sprint',
        details: [
          {
            type: 'paragraph',
            value: 'This KPI is calculated based on 2 parameters',
          },
          {
            type: 'paragraph',
            value:
              'Estimated Hours: It explains the total hours required to complete Sprint backlog',
          },
          {
            type: 'paragraph',
            value:
              'Logged Work: The amount of time team has logged within a Sprint',
          },
        ],
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
                'Logged Work': 68,
              },
              sprintIds: ['40203_Scrum Project_6335363749794a18e8a4479b'],
              sprintNames: ['TEST |Test1 |ITR_1|OpenSource_Scrum Project'],
              lineValue: 68,
              sprojectName: 'Scrum Project',
            },
            {
              data: '20.0',
              sSprintID: '38295_Scrum Project_6335363749794a18e8a4479b',
              sSprintName: 'Test1|PI_10|ITR_5| 24th Aug_Scrum Project',
              value: 20,
              hoverValue: {
                'Estimated Hours': 20,
                'Logged Work': 189,
              },
              sprintIds: ['38295_Scrum Project_6335363749794a18e8a4479b'],
              sprintNames: ['Test1|PI_10|ITR_5| 24th Aug_Scrum Project'],
              lineValue: 189,
              sprojectName: 'Scrum Project',
            },
            {
              data: '0.0',
              sSprintID: '38294_Scrum Project_6335363749794a18e8a4479b',
              sSprintName: 'Test1|PI_10|ITR_4| 10th Aug_Scrum Project',
              value: 0,
              hoverValue: {
                'Estimated Hours': 0,
                'Logged Work': 1,
              },
              sprintIds: ['38294_Scrum Project_6335363749794a18e8a4479b'],
              sprintNames: ['Test1|PI_10|ITR_4| 10th Aug_Scrum Project'],
              lineValue: 1,
              sprojectName: 'Scrum Project',
            },
            {
              data: '40.0',
              sSprintID: '38296_Scrum Project_6335363749794a18e8a4479b',
              sSprintName: 'Test1|PI_10|ITR_6| 07th Sep_Scrum Project',
              value: 40,
              hoverValue: {
                'Estimated Hours': 40,
                'Logged Work': 57,
              },
              sprintIds: ['38296_Scrum Project_6335363749794a18e8a4479b'],
              sprintNames: ['Test1|PI_10|ITR_6| 07th Sep_Scrum Project'],
              lineValue: 57,
              sprojectName: 'Scrum Project',
            },
            {
              data: '50.0',
              sSprintID: '40345_Scrum Project_6335363749794a18e8a4479b',
              sSprintName: 'TEST| Test1|PI_10|Opensource_Scrum Project',
              value: 50,
              hoverValue: {
                'Estimated Hours': 50,
                'Logged Work': 139,
              },
              sprintIds: ['40345_Scrum Project_6335363749794a18e8a4479b'],
              sprintNames: ['TEST| Test1|PI_10|Opensource_Scrum Project'],
              lineValue: 139,
              sprojectName: 'Scrum Project',
            },
          ],
        },
      ],
      groupId: 1,
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
            lhs: 'No. of stories tagged to a Sprint',
          },
        ],
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
              sprintIds: ['40203_Scrum Project_6335363749794a18e8a4479b'],
              sprintNames: ['TEST |Test1 |ITR_1|OpenSource_Scrum Project'],
              sprojectName: 'Scrum Project',
            },
            {
              data: '16.0',
              sSprintID: '38295_Scrum Project_6335363749794a18e8a4479b',
              sSprintName: 'Test1|PI_10|ITR_5| 24th Aug_Scrum Project',
              value: 16,
              hoverValue: {},
              sprintIds: ['38295_Scrum Project_6335363749794a18e8a4479b'],
              sprintNames: ['Test1|PI_10|ITR_5| 24th Aug_Scrum Project'],
              sprojectName: 'Scrum Project',
            },
            {
              data: '11.0',
              sSprintID: '38294_Scrum Project_6335363749794a18e8a4479b',
              sSprintName: 'Test1|PI_10|ITR_4| 10th Aug_Scrum Project',
              value: 11,
              hoverValue: {},
              sprintIds: ['38294_Scrum Project_6335363749794a18e8a4479b'],
              sprintNames: ['Test1|PI_10|ITR_4| 10th Aug_Scrum Project'],
              sprojectName: 'Scrum Project',
            },
            {
              data: '14.0',
              sSprintID: '38296_Scrum Project_6335363749794a18e8a4479b',
              sSprintName: 'Test1|PI_10|ITR_6| 07th Sep_Scrum Project',
              value: 14,
              hoverValue: {},
              sprintIds: ['38296_Scrum Project_6335363749794a18e8a4479b'],
              sprintNames: ['Test1|PI_10|ITR_6| 07th Sep_Scrum Project'],
              sprojectName: 'Scrum Project',
            },
            {
              data: '33.0',
              sSprintID: '40345_Scrum Project_6335363749794a18e8a4479b',
              sSprintName: 'TEST| Test1|PI_10|Opensource_Scrum Project',
              value: 33,
              hoverValue: {},
              sprintIds: ['40345_Scrum Project_6335363749794a18e8a4479b'],
              sprintNames: ['TEST| Test1|PI_10|Opensource_Scrum Project'],
              sprojectName: 'Scrum Project',
            },
          ],
        },
      ],
      groupId: 1,
    },
    {
      kpiId: 'kpi39',
      kpiName: 'Sprint Velocity',
      unit: 'SP',
      maxValue: '300',
      chartType: '',
      kpiInfo: {
        definition:
          'SPRINT VELOCITY measures the rate at which a team can deliver every Sprint',
        formula: [
          {
            lhs: 'Sum of story points of all stories completed within a Sprint',
          },
        ],
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
              sprintIds: ['40203_Scrum Project_6335363749794a18e8a4479b'],
              sprintNames: ['TEST |Test1 |ITR_1|OpenSource_Scrum Project'],
              sprojectName: 'Scrum Project',
            },
            {
              data: '45',
              sSprintID: '38295_Scrum Project_6335363749794a18e8a4479b',
              sSprintName: 'Test1|PI_10|ITR_5| 24th Aug_Scrum Project',
              value: 45,
              hoverValue: {},
              sprintIds: ['38295_Scrum Project_6335363749794a18e8a4479b'],
              sprintNames: ['Test1|PI_10|ITR_5| 24th Aug_Scrum Project'],
              sprojectName: 'Scrum Project',
            },
            {
              data: '23',
              sSprintID: '38294_Scrum Project_6335363749794a18e8a4479b',
              sSprintName: 'Test1|PI_10|ITR_4| 10th Aug_Scrum Project',
              value: 23,
              hoverValue: {},
              sprintIds: ['38294_Scrum Project_6335363749794a18e8a4479b'],
              sprintNames: ['Test1|PI_10|ITR_4| 10th Aug_Scrum Project'],
              sprojectName: 'Scrum Project',
            },
            {
              data: '22',
              sSprintID: '38296_Scrum Project_6335363749794a18e8a4479b',
              sSprintName: 'Test1|PI_10|ITR_6| 07th Sep_Scrum Project',
              value: 22,
              hoverValue: {},
              sprintIds: ['38296_Scrum Project_6335363749794a18e8a4479b'],
              sprintNames: ['Test1|PI_10|ITR_6| 07th Sep_Scrum Project'],
              sprojectName: 'Scrum Project',
            },
            {
              data: '116',
              sSprintID: '40345_Scrum Project_6335363749794a18e8a4479b',
              sSprintName: 'TEST| Test1|PI_10|Opensource_Scrum Project',
              value: 116,
              hoverValue: {},
              sprintIds: ['40345_Scrum Project_6335363749794a18e8a4479b'],
              sprintNames: ['TEST| Test1|PI_10|Opensource_Scrum Project'],
              sprojectName: 'Scrum Project',
            },
          ],
        },
      ],
      groupId: 1,
    },
  ];

  const fakeJiraGroupId1 = require('../../../test/resource/fakeJiraGroupId1.json');
  const fakeKpiTableHeadingArray = [
    {
      field: 'kpiName',
      header: 'Kpi Name',
    },
    {
      field: 'frequency',
      header: 'Frequency',
    },
    {
      field: 1,
      header: 1,
    },
    {
      field: 2,
      header: 2,
    },
    {
      field: 3,
      header: 3,
    },
    {
      field: 4,
      header: 4,
    },
    {
      field: 5,
      header: 5,
    },
    {
      field: 'trend',
      header: 'Trend',
    },
    {
      field: 'maturity',
      header: 'Maturity',
    },
  ];

  const fakeKpi171Data = require('../../../test/resource/fakeKpi171Data.json');

  beforeEach(async () => {
    const routes: Routes = [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'authentication/login', component: DashboardComponent },
    ];

    await TestBed.configureTestingModule({
      imports: [
        FormsModule,
        HttpClientTestingModule,
        InputSwitchModule,
        ReactiveFormsModule,
        CommonModule,
        DropdownModule,
        RouterTestingModule.withRoutes(routes),
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
        ExecutiveV2Component,
        MaturityComponent,
        DashboardComponent,
        ExportExcelComponent,
      ],
      providers: [
        HelperService,
        MessageService,
        { provide: APP_CONFIG, useValue: AppConfig },
        HttpService,
        SharedService,
        ExcelService,
        DatePipe,
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ExecutiveV2Component);
    component = fixture.componentInstance;
    service = TestBed.inject(SharedService);
    httpService = TestBed.inject(HttpService);
    helperService = TestBed.inject(HelperService);
    excelService = TestBed.inject(ExcelService);
    httpMock = TestBed.inject(HttpTestingController);

    // const type = 'scrum';
    // service.selectedtype = type;
    // service.select(masterData, filterData, filterApplyDataWithNoFilter, selectedTab, false, true, null, true, null, 'scrum');
    // service.setDashConfigData(dashConfigData?.data);
    // component.selectedTab = 'developer';
    // fixture.detectChanges();

    // We set the expectations for the HttpClient mock
    reqJira = httpMock.match((request) => request.url);
    exportExcelComponent =
      TestBed.createComponent(ExportExcelComponent).componentInstance;
    spyOn(helperService, 'colorAccToMaturity').and.returnValue('#44739f');
    helperService.deepEqual = jasmine
      .createSpy('deepEqual')
      .and.returnValue(true);
    spyOn(service, 'setScrumKanban');
    spyOn(service, 'setSelectedBoard');
    component.globalConfig = {
      kanban: [],
      scrum: [
        {
          boardSlug: 'test-board',
          boardName: 'test-board',
          kpis: ['kpi1', 'kpi118'],
        },
        { boardSlug: 'other-board', boardName: 'other-board', kpis: ['kpi3'] },
      ],
      others: [],
    };

    component.updatedConfigGlobalData = [
      {
        kpiId: 'kpi118',
        kpiName: 'Deployment Frequency',
        isEnabled: true,
        order: 23,
        kpiDetail: {},
        shown: true,
      },
    ];
    component.service.setSelectedType('Scrum');
    component.ngOnInit();
    fixture.detectChanges();
  });

  afterEach(() => {
    // httpMock.verify();
  });

  it('check whether scrum', (done) => {
    const type = 'Scrum';
    component.selectedtype = 'Scrum';
    spyOn(httpService, 'postKpi').and.returnValue(of(fakeJiraGroupId1));
    fixture.detectChanges();
    expect(component.selectedtype).toBe(type);
    done();
  });

  it('download excel functionality', fakeAsync(() => {
    const excelData = {
      kpiName: 'Defect Injection Rate',
      kpiId: 'kpi14',
      columns: ['sprintName', 'storyID', 'issueDescription', 'linkedDefects'],
      excelData: [
        {
          // eslint-disable-next-line @typescript-eslint/naming-convention
          sprintName: 'AP|PI_10|ITR_5| 24 Aug',
          storyID: {
            // eslint-disable-next-line @typescript-eslint/naming-convention
            'TEST-17970': 'http://testabc.com/jira/browse/TEST-17970',
          },
          issueDescription: 'This is second Story',
          linkedDefects: {
            // eslint-disable-next-line @typescript-eslint/naming-convention
            'TEST-18675': 'http://testabc.com/jira/browse/TEST-18675',
          },
        },
      ],
    };
    const spy = spyOn(component.exportExcelComponent, 'downloadExcel');
    component.iSAdditionalFilterSelected = false;
    component.downloadExcel(
      'kpi35',
      'Defect Seepage Rate',
      false,
      false,
      'multiline',
    );
    expect(spy).toHaveBeenCalled();
  }));

  xit('Scrum with filter applied', (done) => {
    const type = 'Scrum';
    service.selectedtype = type;

    service.select(
      masterData,
      filterData,
      filterApplyDataWithNoFilter,
      selectedTab,
      false,
      true,
      null,
      true,
      null,
      'scrum',
    );
    fixture.detectChanges();
    httpMock.match(baseUrl + '/api/jira/kpi')[0].flush(fakeJiraGroupId1);
    httpMock.match(baseUrl + '/api/jenkins/kpi')[0].flush(fakeJenkins);
    httpMock.match(baseUrl + '/api/zypher/kpi')[0].flush(fakeZypher);
    httpMock.match(baseUrl + '/api/bitbucket/kpi')[0].flush(fakeBitBucket);
    httpMock.match(baseUrl + '/api/sonar/kpi')[0].flush(fakeSonar);
    expect(component.selectedtype).toBe(type);
    done();
  });

  xit('kanban without filter applied', (done) => {
    const type = 'Kanban';
    service.selectedtype = type;
    service.select(
      masterData,
      filterData,
      filterApplyDataWithNoFilter,
      selectedTab,
      false,
      true,
      null,
      true,
      null,
      'scrum',
    );
    httpMock.match(baseUrl + '/api/jirakanban/kpi')[0].flush(fakejiraKanban);
    httpMock
      .match(baseUrl + '/api/jenkinskanban/kpi')[0]
      .flush(fakeJenkinsKanban);
    httpMock
      .match(baseUrl + '/api/zypherkanban/kpi')[0]
      .flush(fakeZypherKanban);
    httpMock
      .match(baseUrl + '/api/bitbucketkanban/kpi')[0]
      .flush(fakeBitBucket);
    httpMock.match(baseUrl + '/api/sonarkanban/kpi')[0].flush(fakeSonarKanban);
    expect(component.selectedtype).toBe(type);
    // fixture.detectChanges();
    done();
  });

  xit('kanban with filter applied only Date', (done) => {
    const type = 'kanban';
    service.setScrumKanban('kanban');
    service.select(
      masterData,
      filterData,
      filterApplyDataWithNoFilter,
      selectedTab,
      false,
      true,
      null,
      true,
      null,
      'scrum',
    );
    fixture.detectChanges();
    spyOn(httpService, 'postKpiKanban').and.returnValue(of(fakejiraKanban));
    // httpMock.match(baseUrl + '/api/jirakanban/kpi')[0].flush(fakejiraKanban);
    // httpMock.match(baseUrl + '/api/jenkinskanban/kpi')[0].flush(fakeJenkinsKanban);
    // httpMock.match(baseUrl + '/api/zypherkanban/kpi')[0].flush(fakeZypherKanban);
    // httpMock.match(baseUrl + '/api/bitbucketkanban/kpi')[0].flush(fakeBitBucket);
    // httpMock.match(baseUrl + '/api/sonarkanban/kpi')[0].flush(fakeSonarKanban);
    expect(component.selectedtype).toBe(type);
    done();
  });

  // --------- checking for backup data, if not use default values --------------
  it('should handle selectedTrendsEventSubject subscription', () => {
    const trend = [{ id: 1 }];
    const selectedTrendFromLS = [{ id: 2 }];
    spyOn(localStorage, 'getItem').and.returnValue(
      JSON.stringify(selectedTrendFromLS),
    );
    spyOn(localStorage, 'setItem');
    spyOn(service, 'setKpiSubFilterObj');
    helperService.deepEqual = jasmine
      .createSpy('deepEqual')
      .and.returnValue(false);

    service.selectedTrendsEventSubject.next(trend);

    expect(component.selectedTrend).toEqual(trend);
    expect(localStorage.setItem).toHaveBeenCalledWith(
      'selectedTrend',
      JSON.stringify(trend),
    );
    expect(component.kpiSelectedFilterObj).toEqual({});
    expect(service.setKpiSubFilterObj).toHaveBeenCalledWith(null);
  });

  it('should handle selectedTrendsEventSubject subscription with equal trends', () => {
    const trend = [{ id: 1 }];
    spyOn(localStorage, 'getItem').and.returnValue(JSON.stringify(trend));
    spyOn(service, 'setKpiSubFilterObj');
    // spyOn(helperService, 'deepEqual').and.returnValue(true);
    helperService.deepEqual = jasmine
      .createSpy('deepEqual')
      .and.returnValue(true);
    service.selectedTrendsEventSubject.next(trend);
    expect(component.selectedTrend).toEqual([]);
    // expect(localStorage.setItem).toHaveBeenCalledWith('selectedTrend', JSON.stringify(trend));
    expect(component.kpiSelectedFilterObj).toEqual({});
    expect(service.setKpiSubFilterObj).toHaveBeenCalledWith({});

    // expect(component.selectedTrend).toEqual([]);
    expect(service.setKpiSubFilterObj).toHaveBeenCalledWith(
      service.getKpiSubFilterObj(),
    );
  });

  // --------- end of checking for backup data, if not use default values -------

  // --------- array deep compare --------------
  it('should return true for identical arrays', () => {
    const a1 = [1, 2, 3];
    const a2 = [1, 2, 3];
    expect(component.arrayDeepCompare(a1, a2)).toBe(true);
  });

  it('should return false for arrays with different elements', () => {
    const a1 = [1, 2, 3];
    const a2 = [1, 2, 4];
    expect(component.arrayDeepCompare(a1, a2)).toBe(true);
  });

  it('should return true for arrays with nested objects', () => {
    const a1 = [
      { a: 1, b: 2 },
      { c: 3, d: 4 },
    ];
    const a2 = [
      { a: 1, b: 2 },
      { c: 3, d: 4 },
    ];
    expect(component.arrayDeepCompare(a1, a2)).toBe(true);
  });

  it('should return false for arrays with nested objects with different values', () => {
    const a1 = [
      { a: 1, b: 2 },
      { c: 3, d: 4 },
    ];
    const a2 = [
      { a: 1, b: 3 },
      { c: 3, d: 4 },
    ];
    expect(component.arrayDeepCompare(a1, a2)).toBe(true);
  });

  it('should return true for arrays with nested arrays', () => {
    const a1 = [
      [1, 2],
      [3, 4],
    ];
    const a2 = [
      [1, 2],
      [3, 4],
    ];
    expect(component.arrayDeepCompare(a1, a2)).toBe(true);
  });

  it('should return false for arrays with nested arrays with different values', () => {
    const a1 = [
      [1, 2],
      [3, 4],
    ];
    const a2 = [
      [1, 3],
      [3, 4],
    ];
    expect(component.arrayDeepCompare(a1, a2)).toBe(true);
  });
  // --------- end of array deep compare -------

  // ---------- getBackupKPIFilters --------------
  it('should update kpiSelectedFilterObj when kpiId is found in service.getKpiSubFilterObj()', () => {
    const kpiId = 'kpi123';
    component.updatedConfigGlobalData = [
      {
        kpiId: 'kpi123',
        kpiDetail: {
          chartType: 'GroupBarChart',
        },
      },
    ];
    const filterPropArr = ['filter'];
    const serviceKpiSubFilterObj = { [kpiId]: 'some value' };
    spyOn(service, 'getKpiSubFilterObj').and.returnValue(
      serviceKpiSubFilterObj,
    );
    component.getBackupKPIFilters(kpiId, filterPropArr);
    expect(component.kpiSelectedFilterObj[kpiId]).toBe('some value');
  });

  it('should update kpiSelectedFilterObj when filterPropArr includes filter and filterType is not multiselectdropdown', () => {
    const kpiId = 'kpi123';
    const filterPropArr = ['filter'];
    const filterType = 'dropdown';
    const kpiDropdowns = { [kpiId]: [{ options: ['option1'] }] };
    component.updatedConfigGlobalData = [
      { kpiId, kpiDetail: { kpiFilter: filterType } },
    ];
    component.kpiDropdowns = kpiDropdowns;
    component.getBackupKPIFilters(kpiId, filterPropArr);
    expect(component.kpiSelectedFilterObj[kpiId]).toEqual(['option1']);
  });

  it('should update kpiSelectedFilterObj when filterPropArr includes filter and filterType is multiselectdropdown', () => {
    const kpiId = 'kpi123';
    const filterPropArr = ['filter'];
    const filterType = 'multiselectdropdown';
    const kpiDropdowns = { [kpiId]: [{ options: ['option1'] }] };
    component.updatedConfigGlobalData = [
      { kpiId, kpiDetail: { kpiFilter: filterType } },
    ];
    component.kpiDropdowns = kpiDropdowns;
    component.getBackupKPIFilters(kpiId, filterPropArr);
    expect(component.kpiSelectedFilterObj[kpiId]).toEqual([]);
  });

  it('should update kpiSelectedFilterObj when filterPropArr includes filter1 and filter2', () => {
    const kpiId = 'kpi123';
    component.updatedConfigGlobalData = [
      {
        kpiId: 'kpi123',
        kpiDetail: {
          chartType: 'GroupBarChart',
        },
      },
    ];
    const filterPropArr = ['filter1', 'filter2'];
    const kpiDropdowns = {
      [kpiId]: [{ options: ['option1'] }, { options: ['option2'] }],
    };
    component.kpiDropdowns = kpiDropdowns;
    component.getBackupKPIFilters(kpiId, filterPropArr);
    expect(component.kpiSelectedFilterObj[kpiId]).toEqual({
      filter1: ['option1'],
      filter2: ['option2'],
    });
  });

  it('should not update kpiSelectedFilterObj when kpiDropdowns is empty', () => {
    const kpiId = 'kpi123';
    component.updatedConfigGlobalData = [
      {
        kpiId: 'kpi123',
        kpiDetail: {
          chartType: 'GroupBarChart',
        },
      },
    ];
    const filterPropArr = ['filter'];
    const kpiDropdowns = {};
    component.kpiDropdowns = kpiDropdowns;
    component.getBackupKPIFilters(kpiId, filterPropArr);
    expect(component.kpiSelectedFilterObj[kpiId]).toBeUndefined();
  });

  it('should update kpiSelectedFilterObj when filterPropArr includes filter and filterType is undefined', () => {
    const kpiId = 'kpi123';
    component.updatedConfigGlobalData = [
      {
        kpiId: 'kpi123',
        kpiDetail: {
          chartType: 'GroupBarChart',
        },
      },
    ];
    const filterPropArr = ['filter'];
    const kpiDropdowns = { [kpiId]: [{ options: ['option1'] }] };
    component.updatedConfigGlobalData = [{ kpiId, kpiDetail: {} }]; // filterType is undefined
    component.kpiDropdowns = kpiDropdowns;
    component.getBackupKPIFilters(kpiId, filterPropArr);
    expect(component.kpiSelectedFilterObj[kpiId]).toEqual(['option1']);
  });
  // ---------- end of getBackupKPIFilters -------

  // ----------- getBackupKPIFiltersForBacklog -------
  it('should set kpiSelectedFilterObj when kpiId exists in getKpiSubFilterObj', () => {
    const kpiId = 'kpi1';
    component.updatedConfigGlobalData = [
      {
        kpiId: 'kpi123',
        kpiDetail: {
          chartType: 'GroupBarChart',
        },
      },
    ];
    const filterObj = { [kpiId]: 'filterValue' };
    service.setKpiSubFilterObj(filterObj);
    component.getBackupKPIFiltersForBacklog(kpiId);
    expect(component.kpiSelectedFilterObj[kpiId]).toBe('filterValue');
  });

  it('should set kpiSelectedFilterObj when filters is not empty and filterType is not multiselectdropdown', () => {
    const kpiId = 'kpi2';
    component.updatedConfigGlobalData = [
      {
        kpiId: 'kpi2',
        kpiDetail: {
          chartType: 'GroupBarChart',
        },
      },
    ];
    const filters = { filter1: 'value1' };
    const filterType = 'dropdown';
    component.allKpiArray = [{ kpiId, filters, trendValueList: [] }];
    component.kpiDropdowns = { [kpiId]: [{ options: ['option1'] }] };
    component.getBackupKPIFiltersForBacklog(kpiId);
    expect(component.kpiSelectedFilterObj[kpiId]).toEqual(['option1']);
  });

  it('should set kpiSelectedFilterObj when filters is not empty and filterType is multiselectdropdown', () => {
    const kpiId = 'kpi3';
    const filters = { filter1: 'value1' };
    const filterType = 'multiselectdropdown';
    component.allKpiArray = [{ kpiId, filters, trendValueList: [] }];
    component.kpiDropdowns = { [kpiId]: [{ options: ['option1'] }] };
    component.updatedConfigGlobalData = [
      { kpiId, kpiDetail: { kpiFilter: filterType } },
    ];
    component.getBackupKPIFiltersForBacklog(kpiId);
    expect(component.kpiSelectedFilterObj[kpiId]).toBeFalsy();
  });

  it('should set kpiSelectedFilterObj when filterType is not empty and not multiselectdropdown', () => {
    const kpiId = 'kpi3';

    const filters = { filter1: 'value1' };
    const filterType = 'dropdown';
    component.allKpiArray = [{ kpiId, filters, trendValueList: [] }];
    component.kpiDropdowns = { [kpiId]: [{ options: ['option1'] }] };
    component.updatedConfigGlobalData = [
      { kpiId, kpiDetail: { kpiFilter: filterType } },
    ];
    component.getBackupKPIFiltersForBacklog(kpiId);
    expect(component.kpiSelectedFilterObj[kpiId]).toEqual(['option1']);
  });

  it('should set kpiSelectedFilterObj when filters is empty and trendValueList has filter property', () => {
    const kpiId = 'kpi4';
    component.updatedConfigGlobalData = [
      {
        kpiId: 'kpi4',
        kpiDetail: {
          chartType: 'GroupBarChart',
        },
      },
    ];
    const trendValueList = [{ filter: 'value1' }];
    component.allKpiArray = [{ kpiId, trendValueList }];
    component.getBackupKPIFiltersForBacklog(kpiId);
    expect(component.kpiSelectedFilterObj[kpiId]).toEqual({
      filter1: ['Overall'],
    });
  });

  it('should set kpiSelectedFilterObj when filters is empty and trendValueList has filter1 property', () => {
    const kpiId = 'kpi5';
    component.updatedConfigGlobalData = [
      {
        kpiId: 'kpi5',
        kpiDetail: {
          chartType: 'GroupBarChart',
        },
      },
    ];
    const trendValueList = [{ filter1: 'value1' }];
    component.allKpiArray = [{ kpiId, trendValueList }];
    component.kpiDropdowns = { [kpiId]: [{ options: ['option1'] }] };
    component.getBackupKPIFiltersForBacklog(kpiId);
    expect(component.kpiSelectedFilterObj[kpiId]).toEqual({
      filter1: ['option1'],
    });
  });

  it('should not set kpiSelectedFilterObj when kpiId does not exist in allKpiArray', () => {
    const kpiId = 'kpi6';
    component.updatedConfigGlobalData = [
      {
        kpiId: 'kpi6',
        kpiDetail: {
          chartType: 'GroupBarChart',
        },
      },
    ];
    component.getBackupKPIFiltersForBacklog(kpiId);
    expect(component.kpiSelectedFilterObj[kpiId]).toBeUndefined();
  });
  // ----------- end of getBackupKPIFiltersForBacklog -------

  // xit('cycle time priority Sum in kanban', ((done) => {
  //   const type = 'Kanban';
  //   service.selectedtype = type;
  //   service.select(masterData, filterData, filterApplyDataWithNoFilter, selectedTab);
  //   httpMock.match(baseUrl + '/api/jirakanban/kpi')[0].flush(fakejiraKanban);
  //   httpMock.match(baseUrl + '/api/jenkinskanban/kpi')[0].flush(fakeJenkinsKanban);
  //   httpMock.match(baseUrl + '/api/zypherkanban/kpi')[0].flush(fakeZypherKanban);
  //   httpMock.match(baseUrl + '/api/bitbucketkanban/kpi')[0].flush(fakeBitBucket);
  //   httpMock.match(baseUrl + '/api/sonarkanban/kpi')[0].flush(fakeSonarKanban);
  //   component.getPriorityColor(0);

  //   done();

  // }));

  it('color acc to maturity check ', waitForAsync(() => {
    const returnBlue = component.returnColorAccToMaturity(0);
    spyOn(component, 'receiveSharedData');
    expect(returnBlue).toBe('#44739f');
    // done();
  }));

  it('color acc to maturity check array', (done) => {
    component.colorAccToMaturity('1-2-3');
    spyOn(component, 'receiveSharedData');
    expect(component.maturityColorCycleTime[0]).toBe('#44739f');
    done();
  });

  it('should create', (done) => {
    spyOn(component, 'receiveSharedData');
    expect(component).toBeTruthy();
    done();
  });

  // it('should process kpi config Data', () => {
  //   component.configGlobalData = configGlobalData;
  //   component.processKpiConfigData();
  //   expect(component.noKpis).toBeFalse();
  //   component.configGlobalData[0]['isEnabled'] = false;
  //   component.configGlobalData[0]['shown'] = false;
  //   component.processKpiConfigData();
  //   expect(component.noKpis).toBeTrue();
  // });

  it('should make post call when kpi available for Sonar for Scrum', () => {
    const kpiListSonar = [
      {
        id: '6332dd4b82451128f9939a29',
        kpiId: 'kpi17',
        kpiName: 'Unit Test Coverage',
      },
    ];
    component.updatedConfigGlobalData = [
      {
        kpiId: 'kpi17',
        kpiName: 'Unit Test Coverage',
        isEnabled: true,
        order: 23,
        kpiDetail: {
          kanban: false,
          kpiSource: 'Sonar',
          kpiCategory: 'Speed',
          groupId: 1,
        },
        shown: true,
      },
    ];
    const spy = spyOn(helperService, 'groupKpiFromMaster').and.returnValue({
      kpiList: kpiListSonar,
    });
    const postSonarSpy = spyOn(component, 'postSonarKpi');
    component.groupSonarKpi(['kpi17']);
    expect(postSonarSpy).toHaveBeenCalled();
  });

  it('should make post call when kpi available for Jenkins for Scrum', () => {
    const kpiListJenkins = [
      {
        id: '6332dd4b82451128f9939a29',
        kpiId: 'kpi17',
        kpiName: 'Unit Test Coverage',
      },
    ];
    component.updatedConfigGlobalData = [
      {
        kpiId: 'kpi17',
        kpiName: 'Unit Test Coverage',
        isEnabled: true,
        order: 23,
        kpiDetail: {
          kanban: false,
          kpiSource: 'Jenkins',
          kpiCategory: 'Speed',
          groupId: 1,
        },
        shown: true,
      },
    ];
    spyOn(helperService, 'groupKpiFromMaster').and.returnValue({
      kpiList: kpiListJenkins,
    });
    const postJenkinsSpy = spyOn(component, 'postJenkinsKpi');
    component.groupJenkinsKpi(['kpi17']);
    expect(postJenkinsSpy).toHaveBeenCalled();
  });

  it('should make post call when kpi available for Zypher for Scrum', () => {
    const kpiListZypher = [
      {
        id: '6332dd4b82451128f9939a29',
        kpiId: 'kpi17',
        kpiName: 'Unit Test Coverage',
        kpiSource: 'Zypher',
        kanban: false,
        groupId: 1,
        kpiDetail: {
          kpiId: 'kpi17',
          kpiName: 'Unit Test Coverage',
          kpiSource: 'Zypher',
          groupId: 1,
        },
      },
    ];
    component.updatedConfigGlobalData = [
      {
        id: '6332dd4b82451128f9939a29',
        kpiId: 'kpi17',
        kpiName: 'Unit Test Coverage',
        kpiSource: 'Zypher',
        kanban: false,
        kpiDetail: {
          kpiId: 'kpi17',
          kpiName: 'Unit Test Coverage',
          kpiSource: 'Zypher',
          groupId: 1,
        },
      },
    ];
    component.selectedtype = 'Scrum';
    spyOn(helperService, 'groupKpiFromMaster').and.returnValue({
      kpiList: kpiListZypher,
    });
    const postZypherSpy = spyOn(component, 'postZypherKpi');
    component.groupZypherKpi(['kpi17']);
    expect(postZypherSpy).toHaveBeenCalled();
  });

  it('should make post call when kpi available for Jira for Scrum', () => {
    const kpiListJira = [
      {
        id: '6332dd4b82451128f9939a29',
        kpiId: 'kpi17',
        kpiName: 'Unit Test Coverage',
        kpiDetail: {
          kpiId: 'kpi17',
          kpiName: 'Unit Test Coverage',
          kpiSource: 'Jira',
          groupId: 1,
        },
      },
    ];
    component.updatedConfigGlobalData = [
      {
        id: '6332dd4b82451128f9939a29',
        kpiId: 'kpi17',
        kpiName: 'Unit Test Coverage',
        kpiDetail: {
          kpiId: 'kpi17',
          kpiName: 'Unit Test Coverage',
          kpiSource: 'Jira',
          groupId: 1,
        },
      },
    ];
    component.selectedtype = 'Scrum';
    spyOn(helperService, 'groupKpiFromMaster').and.returnValue({
      kpiList: kpiListJira,
    });
    const postJiraSpy = spyOn(component, 'postJiraKpi');
    component.groupJiraKpi(['kpi17']);
    expect(postJiraSpy).toHaveBeenCalled();
  });

  it('should make post call when kpi available for Jira for Scrum on release page', () => {
    const kpiListJira = [
      {
        id: '6332dd4b82451128f9939a29',
        kpiId: 'kpi17',
        kpiName: 'Unit Test Coverage',
        kpiDetail: {
          kpiId: 'kpi17',
          kpiName: 'Unit Test Coverage',
          kpiSource: 'Jira',
          groupId: 1,
        },
      },
    ];
    component.updatedConfigGlobalData = [
      {
        id: '6332dd4b82451128f9939a29',
        kpiId: 'kpi17',
        kpiName: 'Unit Test Coverage',
        kpiDetail: {
          kpiId: 'kpi17',
          kpiName: 'Unit Test Coverage',
          kpiSource: 'Jira',
          groupId: 1,
        },
      },
    ];
    component.selectedtype = 'Scrum';
    component.selectedTab = 'release';
    spyOn(helperService, 'groupKpiFromMaster').and.returnValue({
      kpiList: kpiListJira,
    });
    const postJiraSpy = spyOn(component, 'postJiraKpi');
    component.groupJiraKpi(['kpi17']);
    expect(postJiraSpy).toHaveBeenCalled();
  });

  it('should make post call when kpi available for BitBucket for Scrum', () => {
    const kpiListBitBucket = [
      {
        id: '6332dd4b82451128f9939a29',
        kpiId: 'kpi17',
        kpiName: 'Unit Test Coverage',
      },
    ];
    const spy = spyOn(helperService, 'groupKpiFromMaster').and.returnValue({
      kpiList: kpiListBitBucket,
    });
    const postBitBucketSpy = spyOn(component, 'postBitBucketKpi');
    component.groupBitBucketKpi(['kpi17']);
    expect(postBitBucketSpy).toHaveBeenCalled();
  });

  it('should check if kpi exists', () => {
    component.allKpiArray = [
      {
        kpiId: 'kpi13',
      },
    ];
    const result = component.ifKpiExist('kpi13');
    expect(result).toEqual(0);
  });

  it('should refresh values onTypeRefresh', () => {
    spyOn(service, 'getSelectedType');
    spyOn(service, 'getDashConfigData').and.returnValue(globalData['data']);
    const spy = spyOn(component, 'processKpiConfigData');
    service.onTypeOrTabRefresh.next({
      selectedTab: 'Caterory One',
      selectedType: 'Scrum',
    });
    fixture.detectChanges();
    expect(component.selectedBranchFilter).toBe('Select');
  });

  it('should set noTabAccess to true when no filterData', () => {
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
            yaxisLabel: 'Percentage',
          },
        ],
      },
      filterData: [],
      filterApplyData: {
        ids: ['bittest_corporate'],
        sprintIncluded: ['CLOSED'],
        selectedMap: {
          corporate: ['bittest_corporate'],
          business: [],
          account: [],
          subaccount: [],
          project: [],
          sprint: [],
          sqd: [],
        },
        level: 1,
      },
      selectedTab: 'My Test1',
      isAdditionalFilters: false,
    };
    component.receiveSharedData(event);
    expect(component.noTabAccess).toBe(false);
  });

  xit('should call grouping kpi functions when filterdata is available', () => {
    spyOn(service, 'getDashConfigData').and.returnValue(globalData['data']);
    component.filterApplyData = {};
    const event = {
      masterData: {
        kpiList: [
          {
            id: '633ed17f2c2d5abef2451fd8',
            kpiId: 'kpi17',
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
            yaxisLabel: 'Percentage',
          },
        ],
      },
      filterData: [
        {
          nodeId: 'BITBUCKET_DEMO_632c46c6728e93266f5d5631',
          nodeName: 'BITBUCKET_DEMO',
          path: 't3_subaccount###t2_account###t1_business###bittest_corporate',
          labelName: 'project',
          parentId: 't3_subaccount',
          level: 5,
          basicProjectConfigId: '632c46c6728e93266f5d5631',
        },
      ],
      filterApplyData: {
        ids: ['bittest_corporate'],
        sprintIncluded: ['CLOSED'],
        selectedMap: {
          corporate: ['bittest_corporate'],
          business: [],
          account: [],
          subaccount: [],
          project: [],
          sprint: [],
          sqd: [],
          release: ['release1'],
        },
        level: 1,
      },
      selectedTab: 'Quality',
      isAdditionalFilters: false,
      makeAPICall: true,
    };
    component.updatedConfigGlobalData = [
      {
        kpiId: 'kpi17',
        kpiName: 'Unit Test Coverage',
        isEnabled: true,
        order: 23,
        kpiDetail: {
          kanban: false,
          kpiSource: 'Jira',
          kpiCategory: 'Speed',
          groupId: 1,
        },
        shown: true,
      },
    ];
    component.configGlobalData = component.updatedConfigGlobalData;
    component.selectedtype = 'Scrum';
    component.timeRemaining = 0;
    const spy = spyOn(component, 'calcBusinessDays').and.callThrough();
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

  it('should set release end date', () => {
    const filterData = [
      {
        nodeId: 'release1',
        labelName: 'release',
        releaseEndDate: '2023-01-01',
      },
      {
        nodeId: 'release2',
        labelName: 'release',
        releaseEndDate: '2023-01-02',
      },
    ];
    const filterApplyData = {
      selectedMap: {
        release: ['release1'],
      },
    };
    const selectedRelease = filterData.find(
      (x) =>
        x.nodeId === filterApplyData.selectedMap.release[0] &&
        x.labelName.toLowerCase() === 'release',
    );
    const endDate = new Date(selectedRelease?.releaseEndDate)
      .toISOString()
      .split('T')[0];
    component.releaseEndDate = endDate;
    expect(component.releaseEndDate).toEqual('2023-01-01');
  });

  it('should set release end date to undefined if selectedRelease is undefined', () => {
    const filterData = [
      {
        nodeId: 'release1',
        labelName: 'release',
        releaseEndDate: '2023-01-01',
      },
      {
        nodeId: 'release2',
        labelName: 'release',
        releaseEndDate: '2023-01-02',
      },
    ];
    const filterApplyData = {
      selectedMap: {
        release: [undefined],
      },
    };
    component.filterData = filterData;
    component.filterApplyData = filterApplyData;
    const selectedRelease = filterData.find(
      (x) =>
        x.nodeId === filterApplyData.selectedMap.release[0] &&
        x.labelName.toLowerCase() === 'release',
    );
    const endDate =
      selectedRelease !== undefined
        ? new Date(selectedRelease?.releaseEndDate).toISOString().split('T')[0]
        : undefined;
    component.releaseEndDate = endDate;
    expect(component.releaseEndDate).toBeUndefined();
  });

  it('should make post Sonar call', fakeAsync(() => {
    const postData = {
      kpiList: [
        {
          id: '633ed17f2c2d5abef2451fe3',
          kpiId: 'kpi17',
          kpiName: 'Unit Test Coverage',
        },
      ],
    };
    const successRes = [
      {
        kpiId: 'kpi17',
        kpiName: 'Unit Test Coverage',
        id: '633ed17f2c2d5abef2451fe3',
      },
    ];
    const mockSubscription = {
      unsubscribe: jasmine.createSpy('unsubscribe'),
    };
    component.sonarKpiRequest = mockSubscription;
    spyOn(httpService, 'postKpi').and.returnValue(of(postData.kpiList));
    const spy = spyOn(component, 'afterSonarKpiResponseReceived');
    component.postSonarKpi(postData, 'Sonar');
    tick();
    expect(spy).toHaveBeenCalledWith(successRes, postData);
  }));

  // it('should make post Sonar Kanban Kpi call', fakeAsync(() => {
  //   const postData = {
  //     kpiList: [
  //       {
  //         id: '633ed17f2c2d5abef2451fe3',
  //         kpiId: 'kpi17',
  //         kpiName: 'Unit Test Coverage',
  //       },
  //       {
  //         id: '633ed17f2c2d5abef2451fe4',
  //         kpiId: 'kpi38',
  //         kpiName: 'Sonar Violations'
  //       },
  //       {
  //         id: '633ed17f2c2d5abef2451fe5',
  //         kpiId: 'kpi27',
  //         kpiName: 'Sonar Tech Debt',
  //       }
  //     ]
  //   };

  //   const mockSubscription = {
  //     unsubscribe: jasmine.createSpy('unsubscribe'),
  //   };
  //   component.sonarKpiRequest = mockSubscription;
  //   spyOn(httpService, 'postKpiKanban').and.returnValue(of(postData.kpiList));
  //   const spy = spyOn(component, 'afterSonarKpiResponseReceived');
  //   component.postSonarKanbanKpi(postData, 'sonar');
  //   tick();
  //   expect(spy).toHaveBeenCalledWith(postData.kpiList);
  // }));

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
          kpiName: 'Sonar Violations',
        },
        {
          id: '633ed17f2c2d5abef2451fe5',
          kpiId: 'kpi27',
          kpiName: 'Sonar Tech Debt',
        },
      ],
    };
    const mockSubscription = {
      unsubscribe: jasmine.createSpy('unsubscribe'),
    };
    component.jenkinsKpiRequest = mockSubscription;
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
          kpiId: 'kpi17',
          kpiName: 'Unit Test Coverage',
        },
        {
          id: '633ed17f2c2d5abef2451fe4',
          kpiId: 'kpi38',
          kpiName: 'Sonar Violations',
        },
        {
          id: '633ed17f2c2d5abef2451fe5',
          kpiId: 'kpi27',
          kpiName: 'Sonar Tech Debt',
        },
      ],
    };
    const mockSubscription = {
      unsubscribe: jasmine.createSpy('unsubscribe'),
    };
    component.jenkinsKpiRequest = mockSubscription;
    spyOn(httpService, 'postKpiKanban').and.returnValue(of(postData.kpiList));
    const spy = spyOn(component, 'createAllKpiArray');
    component.postJenkinsKanbanKpi(postData, 'Jenkins');
    tick();
    expect(spy).toHaveBeenCalledWith(postData.kpiList);
  }));

  it('should make post Jira call', fakeAsync(() => {
    component.tooltip = {
      sprintCountForKpiCalculation: 2,
    };
    component.selectedTab = 'my-knowhow';
    component.filterApplyData = {
      label: 'project',
      selectedMap: {
        sprint: [],
      },
    };
    const postData = {
      kpiList: [
        {
          id: '633ed17f2c2d5abef2451fe3',
          kpiId: 'kpi17',
          kpiName: 'Unit Test Coverage',
        },
        {
          id: '633ed17f2c2d5abef2451fe3',
          kpiId: 'kpi73',
          kpiName: 'Unit Test Coverage',
          trendValueList: [
            {
              value: [
                {
                  name: 'n1',
                },
                {
                  name: 'n1',
                },
                {
                  name: 'n1',
                },
              ],
            },
          ],
        },
        {
          id: '633ed17f2c2d5abef2451fe4',
          kpiId: 'kpi38',
          kpiName: 'Sonar Violations',
        },
        {
          id: '633ed17f2c2d5abef2451fe5',
          kpiId: 'kpi27',
          kpiName: 'Sonar Tech Debt',
        },
      ],
    };

    const kpiWiseData = {
      kpi27: {
        id: '633ed17f2c2d5abef2451fe5',
        kpiName: 'Sonar Tech Debt',
      },
      kpi38: {
        id: '633ed17f2c2d5abef2451fe4',
        kpiName: 'Sonar Violations',
      },
      kpi17: {
        id: '633ed17f2c2d5abef2451fe3',
        kpiName: 'Unit Test Coverage',
      },
    };
    component.jiraKpiRequest = '';
    spyOn(helperService, 'createKpiWiseId').and.returnValue(kpiWiseData);
    spyOn(httpService, 'postKpi').and.returnValue(of(postData.kpiList));
    const spy = spyOn(component, 'createAllKpiArray');
    component.postJiraKpi(postData, 'Jira');
    component.jiraKpiData = {};
    tick();
    expect(spy).toHaveBeenCalled();
  }));

  it('should make post Jira Kanban call', fakeAsync(() => {
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
          kpiName: 'Sonar Violations',
        },
        {
          id: '633ed17f2c2d5abef2451fe5',
          kpiId: 'kpi27',
          kpiName: 'Sonar Tech Debt',
        },
      ],
    };

    const kpiWiseData = {
      kpi27: {
        id: '633ed17f2c2d5abef2451fe5',
        kpiName: 'Sonar Tech Debt',
      },
      kpi38: {
        id: '633ed17f2c2d5abef2451fe4',
        kpiName: 'Sonar Violations',
      },
      kpi17: {
        id: '633ed17f2c2d5abef2451fe3',
        kpiName: 'Unit Test Coverage',
      },
      kpi997: {
        kpiId: 'kpi997',
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
            filter: 'Overall',
            value: [
              {
                data: 'PSknowHOW ',
                value: [
                  {
                    data: '2',
                    sSprintID: '0-1',
                  },
                  {
                    data: '1',
                    sSprintID: '1-3',
                  },
                  {
                    data: '0',
                    sSprintID: '3-6',
                  },
                  {
                    data: '0',
                    sSprintID: '6-12',
                  },
                  {
                    data: '1',
                    sSprintID: '>12',
                  },
                ],
              },
            ],
          },
        ],
        groupId: 2,
        xAxisValues: ['0-1', '1-3', '3-6', '6-12', '>12'],
      },
    };
    component.jiraKpiRequest = '';
    spyOn(helperService, 'createKpiWiseId').and.returnValue(kpiWiseData);
    spyOn(httpService, 'postKpiKanban').and.returnValue(of(postData.kpiList));
    const spy = spyOn(component, 'createAllKpiArray');
    component.postJiraKanbanKpi(postData, 'Jira');
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
          kpiName: 'Sonar Violations',
        },
        {
          id: '633ed17f2c2d5abef2451fe5',
          kpiId: 'kpi27',
          kpiName: 'Sonar Tech Debt',
        },
      ],
    };

    const kpiWiseData = {
      kpi27: {
        id: '633ed17f2c2d5abef2451fe5',
        kpiName: 'Sonar Tech Debt',
      },
      kpi38: {
        id: '633ed17f2c2d5abef2451fe4',
        kpiName: 'Sonar Violations',
      },
      kpi17: {
        id: '633ed17f2c2d5abef2451fe3',
        kpiName: 'Unit Test Coverage',
      },
    };
    const mockSubscription = {
      unsubscribe: jasmine.createSpy('unsubscribe'),
    };
    component.bitBucketKpiRequest = mockSubscription;
    spyOn(helperService, 'createKpiWiseId').and.returnValue(kpiWiseData);
    spyOn(httpService, 'postKpi').and.returnValue(of(postData.kpiList));
    const spy = spyOn(component, 'createAllKpiArray');
    component.postBitBucketKpi(postData, 'Bitbucket');
    tick();
    expect(spy).toHaveBeenCalled();
  }));

  // it('should return color', () => {
  //   expect(component.getPriorityColor(1)).toBe('#FE7F0C');
  // });

  it('should change View', () => {
    component.changeView('list');
    expect(component.isChartView).toBeFalse();

    component.changeView('chart');
    expect(component.isChartView).toBeTrue();
  });

  xit('should generate colorObj', () => {
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
            xName: 1,
          },
          {
            data: '0',
            value: 0,
            hoverValue: {},
            date: '2022-09-12 to 2022-09-18',
            sprojectName: 'bittest',
            xName: 2,
          },
          {
            data: '0',
            value: 0,
            hoverValue: {},
            date: '2022-09-19 to 2022-09-25',
            sprojectName: 'bittest',
            xName: 3,
          },
          {
            data: '0',
            value: 0,
            hoverValue: {},
            date: '2022-09-26 to 2022-10-02',
            sprojectName: 'bittest',
            xName: 4,
          },
          {
            data: '0',
            value: 0,
            hoverValue: {},
            date: '2022-10-03 to 2022-10-09',
            sprojectName: 'bittest',
            xName: 5,
          },
        ],
        maturity: '5',
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
            projectNames: ['Bus1', 'TestB'],
            sprojectName: 'Corpate1',
            xName: 1,
          },
          {
            data: '0.0',
            value: 0,
            hoverValue: {},
            date: '2022-09-12 to 2022-09-18',
            sprintIds: [],
            sprintNames: [],
            projectNames: ['Bus1', 'TestB'],
            sprojectName: 'Corpate1',
            xName: 2,
          },
          {
            data: '0.0',
            value: 0,
            hoverValue: {},
            date: '2022-09-19 to 2022-09-25',
            sprintIds: [],
            sprintNames: [],
            projectNames: ['Bus1', 'TestB'],
            sprojectName: 'Corpate1',
            xName: 3,
          },
          {
            data: '0.0',
            value: 0,
            hoverValue: {},
            date: '2022-09-26 to 2022-10-02',
            sprintIds: [],
            sprintNames: [],
            projectNames: ['Bus1', 'TestB'],
            sprojectName: 'Corpate1',
            xName: 4,
          },
          {
            data: '0.0',
            value: 0,
            hoverValue: {},
            date: '2022-10-03 to 2022-10-09',
            sprintIds: [],
            sprintNames: [],
            projectNames: ['Bus1', 'TestB'],
            sprojectName: 'Corpate1',
            xName: 5,
          },
        ],
        maturity: '4',
      },
    ];
    component.colorObj = {
      bittest_corporate: {
        nodeName: 'bittest',
        color: '#079FFF',
      },
      Corpate1_corporate: {
        nodeName: 'Corpate1',
        color: '#cdba38',
      },
    };
    component.chartColorList = {};
    component.generateColorObj('kpi84', arr);
    expect(component.chartColorList['kpi84'].length).toEqual(2);
  });

  it('should get dropdown data', () => {
    component.allKpiArray = [
      {
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
                      'No. of Merge Requests': 0,
                    },
                    date: '03/10/2022',
                    lineValue: 0,
                    sprojectName: 'bittest',
                  },
                  {
                    value: 0,
                    hoverValue: {
                      'No. of Check in': 0,
                      'No. of Merge Requests': 0,
                    },
                    date: '04/10/2022',
                    lineValue: 0,
                    sprojectName: 'bittest',
                  },
                  {
                    value: 0,
                    hoverValue: {
                      'No. of Check in': 0,
                      'No. of Merge Requests': 0,
                    },
                    date: '05/10/2022',
                    lineValue: 0,
                    sprojectName: 'bittest',
                  },
                  {
                    value: 0,
                    hoverValue: {
                      'No. of Check in': 0,
                      'No. of Merge Requests': 0,
                    },
                    date: '06/10/2022',
                    lineValue: 0,
                    sprojectName: 'bittest',
                  },
                  {
                    value: 0,
                    hoverValue: {
                      'No. of Check in': 0,
                      'No. of Merge Requests': 0,
                    },
                    date: '07/10/2022',
                    lineValue: 0,
                    sprojectName: 'bittest',
                  },
                ],
              },
              {
                data: 'Corpate1',
                value: [
                  {
                    data: '0',
                    value: 0,
                    hoverValue: {
                      'No. of Check in': 0,
                      'No. of Merge Requests': 0,
                    },
                    date: '03/10/2022',
                    sprintIds: [],
                    sprintNames: [],
                    projectNames: ['Bus1', 'TestB'],
                    lineValue: 0,
                    sprojectName: 'Corpate1',
                  },
                  {
                    data: '0',
                    value: 0,
                    hoverValue: {
                      'No. of Check in': 0,
                      'No. of Merge Requests': 0,
                    },
                    date: '04/10/2022',
                    sprintIds: [],
                    sprintNames: [],
                    projectNames: ['Bus1', 'TestB'],
                    lineValue: 0,
                    sprojectName: 'Corpate1',
                  },
                  {
                    data: '0',
                    value: 0,
                    hoverValue: {
                      'No. of Check in': 0,
                      'No. of Merge Requests': 0,
                    },
                    date: '05/10/2022',
                    sprintIds: [],
                    sprintNames: [],
                    projectNames: ['Bus1', 'TestB'],
                    lineValue: 0,
                    sprojectName: 'Corpate1',
                  },
                  {
                    data: '0',
                    value: 0,
                    hoverValue: {
                      'No. of Check in': 0,
                      'No. of Merge Requests': 0,
                    },
                    date: '06/10/2022',
                    sprintIds: [],
                    sprintNames: [],
                    projectNames: ['Bus1', 'TestB'],
                    lineValue: 0,
                    sprojectName: 'Corpate1',
                  },
                  {
                    data: '0',
                    value: 0,
                    hoverValue: {
                      'No. of Check in': 0,
                      'No. of Merge Requests': 0,
                    },
                    date: '07/10/2022',
                    sprintIds: [],
                    sprintNames: [],
                    projectNames: ['Bus1', 'TestB'],
                    lineValue: 0,
                    sprojectName: 'Corpate1',
                  },
                ],
              },
            ],
          },
        ],
        groupId: 1,
      },
    ];
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
          trendCalculative: false,
        },
        shown: true,
      },
    ];
    component.getDropdownArray('kpi11');
    expect(component.kpiDropdowns['kpi11'].length).toBeGreaterThan(0);
  });

  it('should check maturity', () => {
    const item = [
      {
        data: 'EU',
        value: [
          {
            data: '27.33',
            value: 27.33,

            sprojectName: 'EU',
          },
          {
            data: '0.15',
            value: 0.15,

            sprojectName: 'EU',
          },
          {
            data: '8.66',
            value: 8.66,

            sprojectName: 'EU',
          },
          {
            data: '93.86',
            value: 93.86,

            sprojectName: 'EU',
          },
          {
            data: '15.1',
            value: 15.1,

            sprojectName: 'EU',
          },
        ],
        maturity: '4',
      },
    ];
    const spy = spyOn(component, 'checkMaturity').and.returnValue(of('M4'));
    component.checkMaturity(item);
    expect(spy).toHaveBeenCalled();
  });

  it('should check latest trend and maturity', () => {
    const item = [
      {
        data: 'EU',
        value: [
          {
            data: '27.33',
            value: 27.33,

            sprojectName: 'EU',
          },
          {
            data: '0.15',
            value: 0.15,

            sprojectName: 'EU',
          },
          {
            data: '8.66',
            value: 8.66,

            sprojectName: 'EU',
          },
          {
            data: '93.86',
            value: 93.86,

            sprojectName: 'EU',
          },
          {
            data: '15.1',
            value: 15.1,

            sprojectName: 'EU',
          },
        ],
        maturity: '4',
      },
    ];
    const kpiData = {
      kpiId: 'kpi121',
      kpiName: 'Capacity',
      isEnabled: true,
      order: 2,
      kpiDetail: {
        id: '6407068ba59c6c0bdeb427ae',
        kpiId: 'kpi121',
        kpiName: 'Capacity',
        isDeleted: 'False',
        defaultOrder: 2,
        kpiCategory: 'Iteration',
        kpiUnit: '',
        showTrend: false,
        isPositiveTrend: true,
        boxType: '1_column',
        calculateMaturity: false,
        hideOverallFilter: false,
        kpiSource: 'Jira',
        maxValue: '',
        kanban: false,
        groupId: 8,
        kpiInfo: {
          details: [
            {
              type: 'paragraph',
              value:
                "Planned capacity is the development team's available time.",
            },
            {
              type: 'paragraph',
              value: 'Source of this is KnowHOW',
            },
          ],
        },
        trendCalculative: false,
        additionalFilterSupport: false,
        xaxisLabel: '',
        yaxisLabel: '',
      },
      shown: true,
    };
    const spy = spyOn(component, 'checkLatestAndTrendValue');
    component.checkLatestAndTrendValue(kpiData, item);
    expect(spy).toHaveBeenCalled();
  });

  it('should call set kpi values after SonarKpiResponseReceive', () => {
    const response = [
      {
        kpiId: 'kpi17',
        kpiName: 'Unit Test Coverage',
        unit: '%',
        chartType: '',
        id: '64b4ed7acba3c12de16472f7',
        isDeleted: 'False',
        kpiUnit: '%',
        kanban: false,
        kpiSource: 'Sonar',
        thresholdValue: 55,
        maturityRange: ['-20', '20-40', '40-60', '60-80', '80-'],
        groupId: 1,
        trendValueList: [],
        maxValue: '100',
        kpiInfo: {
          definition:
            'Measure  of the amount of code that is covered by unit tests.',
          formula: [
            {
              lhs: 'The calculation is done directly in Sonarqube',
            },
          ],
          details: [
            {
              type: 'link',
              kpiLinkDetail: {
                text: 'Detailed Information at',
                link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#Unit-Test-Coverage',
              },
            },
          ],
        },
      },
    ];
    spyOn(helperService, 'createKpiWiseId').and.returnValue({
      kpi17: {
        kpiId: 'kpi17',
        kpiName: 'Unit Test Coverage',
        unit: '%',
        maxValue: '100',
        chartType: '',
        kpiInfo: {
          definition:
            'Measure  of the amount of code that is covered by unit tests.',
          formula: [
            {
              lhs: 'The calculation is done directly in Sonarqube',
            },
          ],
          details: [
            {
              type: 'link',
              kpiLinkDetail: {
                text: 'Detailed Information at',
                link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#Unit-Test-Coverage',
              },
            },
          ],
        },
        id: '64ad2860dadebadcf40c7038',
        isDeleted: 'False',
        kpiUnit: '%',
        kanban: false,
        kpiSource: 'Sonar',
        thresholdValue: 55,
        trendValueList: [
          {
            value: [{ filter: 'average sum', value: 5 }],
          },
        ],
        maturityRange: ['-20', '20-40', '40-60', '60-80', '80-'],
        groupId: 1,
      },
    });
    spyOn(helperService, 'createSonarFilter');
    const createAllKpiArraySpy = spyOn(component, 'createAllKpiArray');
    component.afterSonarKpiResponseReceived(response, {
      ids: ['API POD 1 - Core_6524a7677c8bb73cd0c3fe67'],
      kpiList: [
        {
          id: '64b4ed7acba3c12de16472f7',
          kpiId: 'kpi17',
          kpiName: 'Unit Test Coverage',
          isDeleted: 'False',
          defaultOrder: 12,
          kpiUnit: '%',
          chartType: '',
          upperThresholdBG: 'white',
          lowerThresholdBG: 'red',
          showTrend: true,
          isPositiveTrend: true,
          calculateMaturity: true,
          hideOverallFilter: false,
          kpiSource: 'Sonar',
          maxValue: '100',
          thresholdValue: 55,
          kanban: false,
          groupId: 1,
          kpiInfo: {
            definition:
              'Measure  of the amount of code that is covered by unit tests.',
            formula: [
              {
                lhs: 'The calculation is done directly in Sonarqube',
              },
            ],
            details: [
              {
                type: 'link',
                kpiLinkDetail: {
                  text: 'Detailed Information at',
                  link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#Unit-Test-Coverage',
                },
              },
            ],
          },
          kpiFilter: 'multiSelectDropDown',
          aggregationCriteria: 'average',
          maturityRange: ['-20', '20-40', '40-60', '60-80', '80-'],
          trendCalculative: false,
          xaxisLabel: 'Weeks',
          yaxisLabel: 'Percentage',
          isAdditionalFilterSupport: false,
        },
        {
          id: '64b4ed7acba3c12de16472f8',
          kpiId: 'kpi38',
          kpiName: 'Sonar Violations',
          isDeleted: 'False',
          defaultOrder: 13,
          kpiUnit: 'Number',
          chartType: '',
          upperThresholdBG: 'red',
          lowerThresholdBG: 'white',
          showTrend: true,
          isPositiveTrend: false,
          calculateMaturity: false,
          hideOverallFilter: false,
          kpiSource: 'Sonar',
          maxValue: '',
          thresholdValue: 55,
          kanban: false,
          groupId: 1,
          kpiInfo: {
            definition:
              'Measures the count of issues that voilates the set of coding rules, defined through the associated Quality profile for each programming language in the project.',
            formula: [
              {
                lhs: 'Issues are categorized in 3 types: Bug, Vulnerability and Code Smells',
              },
            ],
            details: [
              {
                type: 'link',
                kpiLinkDetail: {
                  text: 'Detailed Information at',
                  link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#Sonar-Violations',
                },
              },
            ],
          },
          kpiFilter: 'multiSelectDropDown',
          aggregationCriteria: 'sum',
          trendCalculative: false,
          xaxisLabel: 'Weeks',
          yaxisLabel: 'Count',
          isAdditionalFilterSupport: false,
        },
        {
          id: '64b4ed7acba3c12de16472f9',
          kpiId: 'kpi27',
          kpiName: 'Sonar Tech Debt',
          isDeleted: 'False',
          defaultOrder: 14,
          kpiUnit: 'Days',
          chartType: '',
          upperThresholdBG: 'red',
          lowerThresholdBG: 'white',
          showTrend: true,
          isPositiveTrend: false,
          calculateMaturity: true,
          hideOverallFilter: true,
          kpiSource: 'Sonar',
          maxValue: '90',
          thresholdValue: 55,
          kanban: false,
          groupId: 1,
          kpiInfo: {
            definition:
              'Time Estimate required to fix all Issues/code smells reported in Sonar code analysis.',
            formula: [
              {
                lhs: 'It is calculated as effort to fix all Code Smells. The effort is calculated in minutes and converted to days by assuming 1 Day =8 Hours.',
              },
            ],
            details: [
              {
                type: 'link',
                kpiLinkDetail: {
                  text: 'Detailed Information at',
                  link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#Sonar-Tech-Debt',
                },
              },
            ],
          },
          kpiFilter: 'dropDown',
          aggregationCriteria: 'sum',
          maturityRange: ['-100', '100-50', '50-30', '30-10', '10-'],
          trendCalculative: false,
          xaxisLabel: 'Weeks',
          yaxisLabel: 'Days',
          isAdditionalFilterSupport: false,
        },
        {
          id: '656347659b6b2f1d4faa9ebe',
          kpiId: 'kpi168',
          kpiName: 'Sonar Code Quality',
          isDeleted: 'False',
          defaultOrder: 14,
          kpiUnit: 'unit',
          chartType: '',
          showTrend: true,
          isPositiveTrend: true,
          calculateMaturity: true,
          hideOverallFilter: true,
          kpiSource: 'Sonar',
          maxValue: '90',
          kanban: false,
          groupId: 1,
          kpiInfo: {
            definition:
              'Sonar Code Quality is graded based on the static and dynamic code analysis procedure built in Sonarqube that analyses code from multiple perspectives.',
            details: [
              {
                type: 'paragraph',
                value: 'Code Quality in Sonarqube is shown as Grades (A to E).',
              },
              {
                type: 'paragraph',
                value: 'A is the highest (best) and,',
              },
              {
                type: 'paragraph',
                value: 'E is the least',
              },
              {
                type: 'link',
                kpiLinkDetail: {
                  text: 'Detailed Information at',
                  link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#Sonar-Code-Quality',
                },
              },
            ],
          },
          kpiFilter: 'dropDown',
          aggregationCriteria: 'average',
          maturityRange: ['5', '4', '3', '2', '1'],
          yaxisOrder: {
            '1': 'A',
            '2': 'B',
            '3': 'C',
            '4': 'D',
            '5': 'E',
          },
          trendCalculative: false,
          xaxisLabel: 'Months',
          yaxisLabel: 'Code Quality',
          isAdditionalFilterSupport: false,
        },
      ],
      selectedMap: {
        bu: [],
        ver: [],
        acc: [],
        port: [],
        project: ['API POD 1 - Core_6524a7677c8bb73cd0c3fe67'],
        release: [],
        sprint: [],
        sqd: [],
      },
      label: 'project',
      level: 5,
      sprintIncluded: ['CLOSED'],
    });
    expect(createAllKpiArraySpy).toHaveBeenCalled();
  });

  it('should call set kpi values after Zypher KpiResponseReceive', () => {
    component.filterApplyData = {
      label: 'project',
    };
    // spyOn(component, 'getLastConfigurableTrendingListData')
    const response = [
      {
        kpiId: 'kpi42',
        kpiName: 'Regression Automation Coverage',
        unit: '%',
        maxValue: '100',
        chartType: '',
        kpiInfo: {
          definition:
            'Measures the progress of automation of regression test cases (the test cases which are marked as part of regression suite.',
          formula: [
            {
              lhs: 'Regression Automation Coverage ',
              operator: 'division',
              operands: [
                'No. of regression test cases automated',
                'Total no. of regression test cases',
              ],
            },
          ],
          details: [
            {
              type: 'link',
              kpiLinkDetail: {
                text: 'Detailed Information at',
                link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#Regression-Automation-Coverage',
              },
            },
          ],
        },
        id: '64ad2860dadebadcf40c7036',
        isDeleted: 'False',
        kpiUnit: '%',
        kanban: false,
        kpiSource: 'Zypher',
        trendValueList: [
          {
            data: 'AddingIterationProject',
            value: [
              {
                data: '0.0',
                sSprintID:
                  '43307_AddingIterationProject_64a4ff265b5fdd437756f904',
                sSprintName: 'KnowHOW | PI_13| ITR_3_AddingIterationProject',
                value: 0,
                hoverValue: {},
                sprintIds: [
                  '43307_AddingIterationProject_64a4ff265b5fdd437756f904',
                ],
                sprintNames: ['KnowHOW | PI_13| ITR_3_AddingIterationProject'],
                sprojectName: 'AddingIterationProject',
                xName: 1,
              },
              {
                data: '0.0',
                sSprintID:
                  '43308_AddingIterationProject_64a4ff265b5fdd437756f904',
                sSprintName: 'KnowHOW | PI_13| ITR_4_AddingIterationProject',
                value: 0,
                hoverValue: {},
                sprintIds: [
                  '43308_AddingIterationProject_64a4ff265b5fdd437756f904',
                ],
                sprintNames: ['KnowHOW | PI_13| ITR_4_AddingIterationProject'],
                sprojectName: 'AddingIterationProject',
                xName: 2,
              },
              {
                data: '0.0',
                sSprintID:
                  '43309_AddingIterationProject_64a4ff265b5fdd437756f904',
                sSprintName: 'KnowHOW | PI_13| ITR_5_AddingIterationProject',
                value: 0,
                hoverValue: {},
                sprintIds: [
                  '43309_AddingIterationProject_64a4ff265b5fdd437756f904',
                ],
                sprintNames: ['KnowHOW | PI_13| ITR_5_AddingIterationProject'],
                sprojectName: 'AddingIterationProject',
                xName: 3,
              },
              {
                data: '0.0',
                sSprintID:
                  '43310_AddingIterationProject_64a4ff265b5fdd437756f904',
                sSprintName: 'KnowHOW | PI_13| ITR_6_AddingIterationProject',
                value: 0,
                hoverValue: {},
                sprintIds: [
                  '43310_AddingIterationProject_64a4ff265b5fdd437756f904',
                ],
                sprintNames: ['KnowHOW | PI_13| ITR_6_AddingIterationProject'],
                sprojectName: 'AddingIterationProject',
                xName: 4,
              },
              {
                data: '0.0',
                sSprintID:
                  '45160_AddingIterationProject_64a4ff265b5fdd437756f904',
                sSprintName: 'KnowHOW | PI_14| ITR_1_AddingIterationProject',
                value: 0,
                hoverValue: {},
                sprintIds: [
                  '45160_AddingIterationProject_64a4ff265b5fdd437756f904',
                ],
                sprintNames: ['KnowHOW | PI_14| ITR_1_AddingIterationProject'],
                sprojectName: 'AddingIterationProject',
                xName: 5,
              },
            ],
            maturity: '1',
            maturityValue: '0.0',
          },
        ],
        maturityRange: ['-20', '20-40', '40-60', '60-80', '80-'],
        groupId: 1,
      },
    ];
    component.selectedtype = 'Scrum';
    spyOn(helperService, 'createKpiWiseId').and.returnValue({
      kpi42: {
        kpiId: 'kpi42',
        kpiName: 'Regression Automation Coverage',
        unit: '%',
        maxValue: '100',
        chartType: '',
        kpiInfo: {
          definition:
            'Measures the progress of automation of regression test cases (the test cases which are marked as part of regression suite.',
          formula: [
            {
              lhs: 'Regression Automation Coverage ',
              operator: 'division',
              operands: [
                'No. of regression test cases automated',
                'Total no. of regression test cases',
              ],
            },
          ],
          details: [
            {
              type: 'link',
              kpiLinkDetail: {
                text: 'Detailed Information at',
                link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#Regression-Automation-Coverage',
              },
            },
          ],
        },
        id: '64ad2860dadebadcf40c7036',
        isDeleted: 'False',
        kpiUnit: '%',
        kanban: false,
        kpiSource: 'Zypher',
        trendValueList: [
          {
            data: 'AddingIterationProject',
            value: [
              {
                data: '0.0',
                sSprintID:
                  '43307_AddingIterationProject_64a4ff265b5fdd437756f904',
                sSprintName: 'KnowHOW | PI_13| ITR_3_AddingIterationProject',
                value: 0,
                hoverValue: {},
                sprintIds: [
                  '43307_AddingIterationProject_64a4ff265b5fdd437756f904',
                ],
                sprintNames: ['KnowHOW | PI_13| ITR_3_AddingIterationProject'],
                sprojectName: 'AddingIterationProject',
                xName: 1,
              },
              {
                data: '0.0',
                sSprintID:
                  '43308_AddingIterationProject_64a4ff265b5fdd437756f904',
                sSprintName: 'KnowHOW | PI_13| ITR_4_AddingIterationProject',
                value: 0,
                hoverValue: {},
                sprintIds: [
                  '43308_AddingIterationProject_64a4ff265b5fdd437756f904',
                ],
                sprintNames: ['KnowHOW | PI_13| ITR_4_AddingIterationProject'],
                sprojectName: 'AddingIterationProject',
                xName: 2,
              },
              {
                data: '0.0',
                sSprintID:
                  '43309_AddingIterationProject_64a4ff265b5fdd437756f904',
                sSprintName: 'KnowHOW | PI_13| ITR_5_AddingIterationProject',
                value: 0,
                hoverValue: {},
                sprintIds: [
                  '43309_AddingIterationProject_64a4ff265b5fdd437756f904',
                ],
                sprintNames: ['KnowHOW | PI_13| ITR_5_AddingIterationProject'],
                sprojectName: 'AddingIterationProject',
                xName: 3,
              },
              {
                data: '0.0',
                sSprintID:
                  '43310_AddingIterationProject_64a4ff265b5fdd437756f904',
                sSprintName: 'KnowHOW | PI_13| ITR_6_AddingIterationProject',
                value: 0,
                hoverValue: {},
                sprintIds: [
                  '43310_AddingIterationProject_64a4ff265b5fdd437756f904',
                ],
                sprintNames: ['KnowHOW | PI_13| ITR_6_AddingIterationProject'],
                sprojectName: 'AddingIterationProject',
                xName: 4,
              },
              {
                data: '0.0',
                sSprintID:
                  '45160_AddingIterationProject_64a4ff265b5fdd437756f904',
                sSprintName: 'KnowHOW | PI_14| ITR_1_AddingIterationProject',
                value: 0,
                hoverValue: {},
                sprintIds: [
                  '45160_AddingIterationProject_64a4ff265b5fdd437756f904',
                ],
                sprintNames: ['KnowHOW | PI_14| ITR_1_AddingIterationProject'],
                sprojectName: 'AddingIterationProject',
                xName: 5,
              },
            ],
            maturity: '1',
            maturityValue: '0.0',
          },
        ],
        maturityRange: ['-20', '20-40', '40-60', '60-80', '80-'],
        groupId: 1,
      },
    });
    spyOn(helperService, 'calculateTestExecutionData').and.returnValue({
      selectedTestExecutionFilterData: [],
      testExecutionFilterData: [],
    });
    const createAllKpiArraySpy = spyOn(component, 'createAllKpiArray');
    component.afterZypherKpiResponseReceived(response, {
      kpiList: [
        {
          id: '64b4ed7acba3c12de16472f5',
          kpiId: 'kpi42',
          kpiName: 'Regression Automation Coverage',
          isDeleted: 'False',
          defaultOrder: 10,
          kpiUnit: '%',
          chartType: '',
          upperThresholdBG: 'white',
          lowerThresholdBG: 'red',
          showTrend: true,
          isPositiveTrend: true,
          calculateMaturity: true,
          hideOverallFilter: false,
          kpiSource: 'Zypher',
          maxValue: '100',
          kanban: false,
          groupId: 1,
          kpiInfo: {
            definition:
              'Measures the progress of automation of regression test cases (the test cases which are marked as part of regression suite.',
            formula: [
              {
                lhs: 'Regression Automation Coverage ',
                operator: 'division',
                operands: [
                  'No. of regression test cases automated',
                  'Total no. of regression test cases',
                ],
              },
            ],
            details: [
              {
                type: 'link',
                kpiLinkDetail: {
                  text: 'Detailed Information at',
                  link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#Regression-Automation-Coverage',
                },
              },
            ],
          },
          aggregationCriteria: 'average',
          maturityRange: ['-20', '20-40', '40-60', '60-80', '80-'],
          trendCalculative: false,
          xaxisLabel: 'Sprints',
          yaxisLabel: 'Percentage',
          isAdditionalFilterSupport: false,
        },
        {
          id: '64b4ed7acba3c12de16472f6',
          kpiId: 'kpi16',
          kpiName: 'In-Sprint Automation Coverage',
          isDeleted: 'False',
          defaultOrder: 11,
          kpiUnit: '%',
          chartType: '',
          upperThresholdBG: 'white',
          lowerThresholdBG: 'red',
          showTrend: true,
          isPositiveTrend: true,
          calculateMaturity: true,
          hideOverallFilter: false,
          kpiSource: 'Zypher',
          maxValue: '100',
          thresholdValue: 80,
          kanban: false,
          groupId: 1,
          kpiInfo: {
            definition:
              'Measures the progress of automation of test cases created within the Sprint',
            formula: [
              {
                lhs: 'In-Sprint Automation Coverage ',
                operator: 'division',
                operands: [
                  'No. of in-sprint test cases automated',
                  'Total no. of in-sprint test cases created',
                ],
              },
            ],
            details: [
              {
                type: 'link',
                kpiLinkDetail: {
                  text: 'Detailed Information at',
                  link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#In-Sprint-Automation-Coverage',
                },
              },
            ],
          },
          aggregationCriteria: 'average',
          maturityRange: ['-20', '20-40', '40-60', '60-80', '80-'],
          trendCalculative: false,
          xaxisLabel: 'Sprints',
          yaxisLabel: 'Percentage',
          isAdditionalFilterSupport: false,
        },
        {
          id: '64b4ed7acba3c12de16472fb',
          kpiId: 'kpi70',
          kpiName: 'Test Execution and pass percentage',
          isDeleted: 'False',
          defaultOrder: 16,
          kpiUnit: '%',
          chartType: '',
          upperThresholdBG: 'white',
          lowerThresholdBG: 'red',
          showTrend: true,
          isPositiveTrend: true,
          lineLegend: 'Passed',
          barLegend: 'Executed',
          calculateMaturity: true,
          hideOverallFilter: false,
          kpiSource: 'Zypher',
          maxValue: '100',
          kanban: false,
          groupId: 1,
          kpiInfo: {
            definition:
              'Measures the percentage of test cases that have been executed & and the test that have passed.',
            details: [
              {
                type: 'link',
                kpiLinkDetail: {
                  text: 'Detailed Information at',
                  link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#Test-Execution-and-pass-percentage',
                },
              },
            ],
          },
          aggregationCriteria: 'average',
          maturityRange: ['-20', '20-40', '40-60', '60-80', '80-'],
          trendCalculative: false,
          xaxisLabel: 'Sprints',
          yaxisLabel: 'Percentage',
          isAdditionalFilterSupport: false,
        },
      ],
    });

    expect(createAllKpiArraySpy).toHaveBeenCalled();
  });

  it('should call zypher kpi api', () => {
    spyOn(httpService, 'postKpi').and.returnValue(of({}));
    const spyafterZypherKpiResponseReceived = spyOn(
      component,
      'afterZypherKpiResponseReceived',
    );
    component.postZypherKpi({}, 'Zypher');
    fixture.detectChanges();
    expect(spyafterZypherKpiResponseReceived).toHaveBeenCalled();
  });

  it('should call createCombinations', () => {
    const t1 = ['Initial Commitment (Story Points)'];
    const t2 = ['Overall'];
    const response = component.createCombinations(t1, t2);
    const t3 = [
      {
        filter1: 'Initial Commitment (Story Points)',
        filter2: 'Overall',
      },
    ];
    expect(response).toEqual(t3);
  });

  it('should call zypher kanban kpi api', () => {
    const mockSubscription = {
      unsubscribe: jasmine.createSpy('unsubscribe'),
    };
    component.zypherKpiRequest = mockSubscription;
    spyOn(httpService, 'postKpiKanban').and.returnValue(of({}));
    const spyafterZypherKpiResponseReceived = spyOn(
      component,
      'afterZypherKpiResponseReceived',
    );
    component.postZypherKanbanKpi({}, 'Zypher');
    fixture.detectChanges();
    expect(spyafterZypherKpiResponseReceived).toHaveBeenCalled();
  });

  it('should call post bitbucket kanban kpi', () => {
    const mockSubscription = {
      unsubscribe: jasmine.createSpy('unsubscribe'),
    };
    component.bitBucketKpiRequest = mockSubscription;
    spyOn(httpService, 'postKpiKanban').and.returnValue(of({}));
    const spycreateAllKpiArray = spyOn(component, 'createAllKpiArray');
    component.postBitBucketKanbanKpi({}, 'Bitbucket');
    fixture.detectChanges();
    expect(spycreateAllKpiArray).toHaveBeenCalled();
  });

  it('should call checkLatestAndTrendValue for trendCalculation false', () => {
    const kpiData = {
      kpiId: 'kpi72',
      kpiName: 'Commitment Reliability',
      isEnabled: true,
      order: 18,
      kpiDetail: {
        id: '64b70909097ae57dfd51c080',
        kpiId: 'kpi72',
        kpiName: 'Commitment Reliability',
        isDeleted: 'False',
        defaultOrder: 18,
        kpiUnit: '%',
        chartType: 'line',
        showTrend: true,
        isPositiveTrend: true,
        calculateMaturity: true,
        hideOverallFilter: false,
        kpiSource: 'Jira',
        maxValue: '200',
        thresholdValue: 85,
        kanban: false,
        groupId: 2,
        kpiInfo: {
          definition:
            'Measures the percentage of work completed at the end of a iteration in comparison to the initial scope and the final scope',
          formula: [
            {
              lhs: 'Commitment reliability',
              operator: 'division',
              operands: [
                'No. of issues or Size of issues completed',
                'No. of issues or Size of issues committed',
              ],
            },
          ],
          details: [
            {
              type: 'link',
              kpiLinkDetail: {
                text: 'Detailed Information at',
                link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/26935328/Scrum+SPEED+KPIs#Commitment-Reliability',
              },
            },
          ],
        },
        kpiFilter: 'dropDown',
        aggregationCriteria: 'average',
        maturityRange: ['-40', '40-60', '60-75', '75-90', '90-'],
        xaxisLabel: 'Sprints',
        yaxisLabel: 'Percentage',
        trendCalculative: false,
        additionalFilterSupport: true,
      },
      shown: true,
    };
    const item = {
      data: 'AddingIterationProject',
      value: [
        {
          data: '43',
          sSprintID: '43306_AddingIterationProject_64a4ff265b5fdd437756f904',
          sSprintName: 'KnowHOW | PI_13| ITR_2_AddingIterationProject',
          value: 43,
          hoverValue: {
            Delivered: 78.5,
            'Initially Commited': 181.5,
          },
          kpiGroup: 'Initial Commitment (Story Points)#overAll',
          sprojectName: 'AddingIterationProject',
          xName: 1,
        },
        {
          data: '37',
          sSprintID: '43307_AddingIterationProject_64a4ff265b5fdd437756f904',
          sSprintName: 'KnowHOW | PI_13| ITR_3_AddingIterationProject',
          value: 37,
          hoverValue: {
            Delivered: 87,
            'Initially Commited': 229,
          },
          kpiGroup: 'Initial Commitment (Story Points)#overAll',
          sprojectName: 'AddingIterationProject',
          xName: 2,
        },
        {
          data: '39',
          sSprintID: '43308_AddingIterationProject_64a4ff265b5fdd437756f904',
          sSprintName: 'KnowHOW | PI_13| ITR_4_AddingIterationProject',
          value: 39,
          hoverValue: {
            Delivered: 78,
            'Initially Commited': 200,
          },
          kpiGroup: 'Initial Commitment (Story Points)#overAll',
          sprojectName: 'AddingIterationProject',
          xName: 3,
        },
        {
          data: '38',
          sSprintID: '43309_AddingIterationProject_64a4ff265b5fdd437756f904',
          sSprintName: 'KnowHOW | PI_13| ITR_5_AddingIterationProject',
          value: 38,
          hoverValue: {
            Delivered: 83.5,
            'Initially Commited': 217.5,
          },
          kpiGroup: 'Initial Commitment (Story Points)#overAll',
          sprojectName: 'AddingIterationProject',
          xName: 4,
        },
        {
          data: '66',
          sSprintID: '43310_AddingIterationProject_64a4ff265b5fdd437756f904',
          sSprintName: 'KnowHOW | PI_13| ITR_6_AddingIterationProject',
          value: 66,
          hoverValue: {
            Delivered: 125,
            'Initially Commited': 189,
          },
          kpiGroup: 'Initial Commitment (Story Points)#overAll',
          sprojectName: 'AddingIterationProject',
          xName: 5,
        },
      ],
      maturity: '2',
      maturityValue: '45',
    };
    const spyData = component.checkLatestAndTrendValue(kpiData, item);
    const result = ['66 %', '+ve', '%'];
    expect(spyData).toEqual(result);
  });

  it('should call checkLatestAndTrendValue for trendCalculation true', () => {
    const kpiData = {
      kpiId: 'kpi72',
      kpiName: 'Commitment Reliability',
      isEnabled: true,
      order: 18,
      kpiDetail: {
        id: '64b70909097ae57dfd51c080',
        kpiId: 'kpi72',
        kpiName: 'Commitment Reliability',
        isDeleted: 'False',
        defaultOrder: 18,
        kpiUnit: '%',
        chartType: 'line',
        showTrend: true,
        isPositiveTrend: true,
        calculateMaturity: true,
        hideOverallFilter: false,
        kpiSource: 'Jira',
        maxValue: '200',
        thresholdValue: 85,
        kanban: false,
        groupId: 2,
        kpiInfo: {
          definition:
            'Measures the percentage of work completed at the end of a iteration in comparison to the initial scope and the final scope',
          formula: [
            {
              lhs: 'Commitment reliability',
              operator: 'division',
              operands: [
                'No. of issues or Size of issues completed',
                'No. of issues or Size of issues committed',
              ],
            },
          ],
          details: [
            {
              type: 'link',
              kpiLinkDetail: {
                text: 'Detailed Information at',
                link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/26935328/Scrum+SPEED+KPIs#Commitment-Reliability',
              },
            },
          ],
        },
        kpiFilter: 'dropDown',
        aggregationCriteria: 'average',
        maturityRange: ['-40', '40-60', '60-75', '75-90', '90-'],
        xaxisLabel: 'Sprints',
        yaxisLabel: 'Percentage',
        trendCalculative: true,
        additionalFilterSupport: true,
      },
      shown: true,
    };
    const item = {
      data: 'AddingIterationProject',
      value: [
        {
          data: '43',
          sSprintID: '43306_AddingIterationProject_64a4ff265b5fdd437756f904',
          sSprintName: 'KnowHOW | PI_13| ITR_2_AddingIterationProject',
          value: 43,
          hoverValue: {
            Delivered: 78.5,
            'Initially Commited': 181.5,
          },
          kpiGroup: 'Initial Commitment (Story Points)#overAll',
          sprojectName: 'AddingIterationProject',
          xName: 1,
        },
        {
          data: '37',
          sSprintID: '43307_AddingIterationProject_64a4ff265b5fdd437756f904',
          sSprintName: 'KnowHOW | PI_13| ITR_3_AddingIterationProject',
          value: 37,
          hoverValue: {
            Delivered: 87,
            'Initially Commited': 229,
          },
          kpiGroup: 'Initial Commitment (Story Points)#overAll',
          sprojectName: 'AddingIterationProject',
          xName: 2,
        },
        {
          data: '39',
          sSprintID: '43308_AddingIterationProject_64a4ff265b5fdd437756f904',
          sSprintName: 'KnowHOW | PI_13| ITR_4_AddingIterationProject',
          value: 39,
          hoverValue: {
            Delivered: 78,
            'Initially Commited': 200,
          },
          kpiGroup: 'Initial Commitment (Story Points)#overAll',
          sprojectName: 'AddingIterationProject',
          xName: 3,
        },
        {
          data: '38',
          sSprintID: '43309_AddingIterationProject_64a4ff265b5fdd437756f904',
          sSprintName: 'KnowHOW | PI_13| ITR_5_AddingIterationProject',
          value: 38,
          hoverValue: {
            Delivered: 83.5,
            'Initially Commited': 217.5,
          },
          kpiGroup: 'Initial Commitment (Story Points)#overAll',
          sprojectName: 'AddingIterationProject',
          xName: 4,
        },
        {
          data: '66',
          sSprintID: '43310_AddingIterationProject_64a4ff265b5fdd437756f904',
          sSprintName: 'KnowHOW | PI_13| ITR_6_AddingIterationProject',
          value: 66,
          hoverValue: {
            Delivered: 125,
            'Initially Commited': 189,
          },
          kpiGroup: 'Initial Commitment (Story Points)#overAll',
          sprojectName: 'AddingIterationProject',
          xName: 5,
        },
      ],
      maturity: '2',
      maturityValue: '45',
    };
    const spyData = component.checkLatestAndTrendValue(kpiData, item);
    const result = ['66 %', 'NA', '%'];
    expect(spyData).toEqual(result);
  });

  it('should call checkMaturity', () => {
    const item = {
      data: 'AddingIterationProject',
      value: [
        {
          data: '43',
          sSprintID: '43306_AddingIterationProject_64a4ff265b5fdd437756f904',
          sSprintName: 'KnowHOW | PI_13| ITR_2_AddingIterationProject',
          value: 43,
          hoverValue: {
            Delivered: 78.5,
            'Initially Commited': 181.5,
          },
          kpiGroup: 'Initial Commitment (Story Points)#overAll',
          sprojectName: 'AddingIterationProject',
          xName: 1,
        },
        {
          data: '37',
          sSprintID: '43307_AddingIterationProject_64a4ff265b5fdd437756f904',
          sSprintName: 'KnowHOW | PI_13| ITR_3_AddingIterationProject',
          value: 37,
          hoverValue: {
            Delivered: 87,
            'Initially Commited': 229,
          },
          kpiGroup: 'Initial Commitment (Story Points)#overAll',
          sprojectName: 'AddingIterationProject',
          xName: 2,
        },
        {
          data: '39',
          sSprintID: '43308_AddingIterationProject_64a4ff265b5fdd437756f904',
          sSprintName: 'KnowHOW | PI_13| ITR_4_AddingIterationProject',
          value: 39,
          hoverValue: {
            Delivered: 78,
            'Initially Commited': 200,
          },
          kpiGroup: 'Initial Commitment (Story Points)#overAll',
          sprojectName: 'AddingIterationProject',
          xName: 3,
        },
        {
          data: '38',
          sSprintID: '43309_AddingIterationProject_64a4ff265b5fdd437756f904',
          sSprintName: 'KnowHOW | PI_13| ITR_5_AddingIterationProject',
          value: 38,
          hoverValue: {
            Delivered: 83.5,
            'Initially Commited': 217.5,
          },
          kpiGroup: 'Initial Commitment (Story Points)#overAll',
          sprojectName: 'AddingIterationProject',
          xName: 4,
        },
        {
          data: '66',
          sSprintID: '43310_AddingIterationProject_64a4ff265b5fdd437756f904',
          sSprintName: 'KnowHOW | PI_13| ITR_6_AddingIterationProject',
          value: 66,
          hoverValue: {
            Delivered: 125,
            'Initially Commited': 189,
          },
          kpiGroup: 'Initial Commitment (Story Points)#overAll',
          sprojectName: 'AddingIterationProject',
          xName: 5,
        },
      ],
      maturity: '2',
      maturityValue: '45',
    };
    const response = 'M2';
    const spyData = component.checkMaturity(item);
    expect(spyData).toEqual(response);
  });

  it('should call handleSelectedOption for kpi72', () => {
    const event = {
      filter1: 'Initial Commitment (Count)',
      filter2: 'Enabler Story',
    };
    const kpi = {
      kpiId: 'kpi72',
      kpiName: 'Commitment Reliability',
      isEnabled: true,
      order: 18,
      kpiDetail: {
        id: '64b70909097ae57dfd51c080',
        kpiId: 'kpi72',
        kpiName: 'Commitment Reliability',
        isDeleted: 'False',
        defaultOrder: 18,
        kpiUnit: '%',
        chartType: 'line',
        showTrend: true,
        isPositiveTrend: true,
        calculateMaturity: true,
        hideOverallFilter: false,
        kpiSource: 'Jira',
        maxValue: '200',
        thresholdValue: 85,
        kanban: false,
        groupId: 2,
        kpiInfo: {
          definition:
            'Measures the percentage of work completed at the end of a iteration in comparison to the initial scope and the final scope',
          formula: [
            {
              lhs: 'Commitment reliability',
              operator: 'division',
              operands: [
                'No. of issues or Size of issues completed',
                'No. of issues or Size of issues committed',
              ],
            },
          ],
          details: [
            {
              type: 'link',
              kpiLinkDetail: {
                text: 'Detailed Information at',
                link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/26935328/Scrum+SPEED+KPIs#Commitment-Reliability',
              },
            },
          ],
        },
        kpiFilter: 'dropDown',
        aggregationCriteria: 'average',
        maturityRange: ['-40', '40-60', '60-75', '75-90', '90-'],
        xaxisLabel: 'Sprints',
        yaxisLabel: 'Percentage',
        trendCalculative: false,
        additionalFilterSupport: true,
      },
      shown: true,
    };
    const response = {
      filter1: ['Initial Commitment (Count)'],
      filter2: ['Enabler Story'],
    };

    component.globalConfig = {
      kanban: [],
      scrum: [
        {
          boardSlug: 'test-board',
          boardName: 'test-board',
          kpis: ['kpi1', 'kpi72'],
        },
      ],
      others: [],
    };
    component.handleSelectedOption(event, kpi);
    expect(component.kpiSelectedFilterObj['kpi72']).toEqual(response);
  });

  xit('should call handleSelectedOption for non kpi72', () => {
    const event = {
      filter1: ['P3'],
      filter2: [],
    };
    const kpi = {
      kpiId: 'kpi28',
      kpiName: 'Defect Count By Priority',
      isEnabled: true,
      order: 7,
      kpiDetail: {
        id: '64b70909097ae57dfd51c075',
        kpiId: 'kpi28',
        kpiName: 'Defect Count By Priority',
        isDeleted: 'False',
        defaultOrder: 7,
        kpiUnit: 'Number',
        chartType: 'line',
        showTrend: true,
        isPositiveTrend: false,
        calculateMaturity: false,
        hideOverallFilter: false,
        kpiSource: 'Jira',
        maxValue: '90',
        thresholdValue: 55,
        kanban: false,
        groupId: 1,
        kpiInfo: {
          definition:
            'Measures the number of defects grouped by priority in an iteration',
          formula: [
            {
              lhs: 'Defect Count By Priority=No. of defects linked to stories grouped by priority',
            },
          ],
          details: [
            {
              type: 'link',
              kpiLinkDetail: {
                text: 'Detailed Information at',
                link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#Defect-Count-by-Priority',
              },
            },
          ],
        },
        kpiFilter: 'multiSelectDropDown',
        aggregationCriteria: 'sum',
        xaxisLabel: 'Sprints',
        yaxisLabel: 'Count',
        trendCalculative: false,
        additionalFilterSupport: true,
      },
      shown: true,
    };
    component.globalConfig = {
      kanban: [],
      scrum: [
        {
          boardSlug: 'test-board',
          boardName: 'test-board',
          kpis: ['kpi1', 'kpi28'],
        },
      ],
      others: [],
    };
    component.handleSelectedOption(event, kpi);
    expect(component.kpiSelectedFilterObj).toBeDefined();
  });

  xit('should call handleSelectedOption for non kpi72', () => {
    const event = {
      filter1: [],
    };
    const kpi = {
      kpiId: 'kpi28',
      kpiName: 'Defect Count By Priority',
      isEnabled: true,
      order: 7,
      kpiDetail: {
        id: '64b70909097ae57dfd51c075',
        kpiId: 'kpi28',
        kpiName: 'Defect Count By Priority',
        isDeleted: 'False',
        defaultOrder: 7,
        kpiUnit: 'Number',
        chartType: 'line',
        showTrend: true,
        isPositiveTrend: false,
        calculateMaturity: false,
        hideOverallFilter: false,
        kpiSource: 'Jira',
        maxValue: '90',
        thresholdValue: 55,
        kanban: false,
        groupId: 1,
        kpiInfo: {
          definition:
            'Measures the number of defects grouped by priority in an iteration',
          formula: [
            {
              lhs: 'Defect Count By Priority=No. of defects linked to stories grouped by priority',
            },
          ],
          details: [
            {
              type: 'link',
              kpiLinkDetail: {
                text: 'Detailed Information at',
                link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#Defect-Count-by-Priority',
              },
            },
          ],
        },
        kpiFilter: 'multiSelectDropDown',
        aggregationCriteria: 'sum',
        xaxisLabel: 'Sprints',
        yaxisLabel: 'Count',
        trendCalculative: false,
        additionalFilterSupport: true,
      },
      shown: true,
    };
    const response = ['P3'];
    const spyData = component.handleSelectedOption(event, kpi);
    // expect(component.kpiSelectedFilterObj["kpi28"]).toEqual(response);
  });

  xit('should call handleSelectedOption for non kpi72', () => {
    const event = 'test1';
    const kpi = {
      kpiId: 'kpi28',
      kpiName: 'Defect Count By Priority',
      isEnabled: true,
      order: 7,
      kpiDetail: {
        id: '64b70909097ae57dfd51c075',
        kpiId: 'kpi28',
        kpiName: 'Defect Count By Priority',
        isDeleted: 'False',
        defaultOrder: 7,
        kpiUnit: 'Number',
        chartType: 'line',
        showTrend: true,
        isPositiveTrend: false,
        calculateMaturity: false,
        hideOverallFilter: false,
        kpiSource: 'Jira',
        maxValue: '90',
        thresholdValue: 55,
        kanban: false,
        groupId: 1,
        kpiInfo: {
          definition:
            'Measures the number of defects grouped by priority in an iteration',
          formula: [
            {
              lhs: 'Defect Count By Priority=No. of defects linked to stories grouped by priority',
            },
          ],
          details: [
            {
              type: 'link',
              kpiLinkDetail: {
                text: 'Detailed Information at',
                link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#Defect-Count-by-Priority',
              },
            },
          ],
        },
        kpiFilter: 'multiSelectDropDown',
        aggregationCriteria: 'sum',
        xaxisLabel: 'Sprints',
        yaxisLabel: 'Count',
        trendCalculative: false,
        additionalFilterSupport: true,
      },
      shown: true,
    };
    const response = ['P3'];
    const spyData = component.handleSelectedOption(event, kpi);
    // expect(component.kpiSelectedFilterObj["kpi28"]).toEqual(response);
  });

  it('should call getDropdownArray', () => {
    const kpiId = 'kpi72';
    component.allKpiArray = [
      {
        kpiId: 'kpi72',
        kpiName: 'Commitment Reliability',
        unit: '%',
        maxValue: '200',
        chartType: '',
        kpiInfo: {
          definition:
            'Measures the percentage of work completed at the end of a iteration in comparison to the initial scope and the final scope',
          formula: [
            {
              lhs: 'Commitment reliability',
              operator: 'division',
              operands: [
                'No. of issues or Size of issues completed',
                'No. of issues or Size of issues committed',
              ],
            },
          ],
          details: [
            {
              type: 'link',
              kpiLinkDetail: {
                text: 'Detailed Information at',
                link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/26935328/Scrum+SPEED+KPIs#Commitment-Reliability',
              },
            },
          ],
        },
        id: '64a58c54600c3151b16a1196',
        isDeleted: 'False',
        kpiUnit: '%',
        kanban: false,
        kpiSource: 'Jira',
        thresholdValue: 85,
        trendValueList: [
          {
            filter1: 'Initial Commitment (Story Points)',
            filter2: 'Overall',
            value: [
              {
                data: 'AddingIterationProject',
                value: [
                  {
                    data: '43',
                    sSprintID:
                      '43306_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_2_AddingIterationProject',
                    value: 43,
                    hoverValue: {
                      Delivered: 78.5,
                      'Initially Commited': 181.5,
                    },
                    kpiGroup: 'Initial Commitment (Story Points)#overAll',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '37',
                    sSprintID:
                      '43307_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_3_AddingIterationProject',
                    value: 37,
                    hoverValue: {
                      Delivered: 87,
                      'Initially Commited': 229,
                    },
                    kpiGroup: 'Initial Commitment (Story Points)#overAll',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '39',
                    sSprintID:
                      '43308_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_4_AddingIterationProject',
                    value: 39,
                    hoverValue: {
                      Delivered: 78,
                      'Initially Commited': 200,
                    },
                    kpiGroup: 'Initial Commitment (Story Points)#overAll',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '38',
                    sSprintID:
                      '43309_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_5_AddingIterationProject',
                    value: 38,
                    hoverValue: {
                      Delivered: 83.5,
                      'Initially Commited': 217.5,
                    },
                    kpiGroup: 'Initial Commitment (Story Points)#overAll',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '66',
                    sSprintID:
                      '43310_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_6_AddingIterationProject',
                    value: 66,
                    hoverValue: {
                      Delivered: 125,
                      'Initially Commited': 189,
                    },
                    kpiGroup: 'Initial Commitment (Story Points)#overAll',
                    sprojectName: 'AddingIterationProject',
                  },
                ],
                maturity: '2',
                maturityValue: '45',
              },
            ],
          },
          {
            filter1: 'Initial Commitment (Story Points)',
            filter2: 'Story',
            value: [
              {
                data: 'AddingIterationProject',
                value: [
                  {
                    data: '42',
                    sSprintID:
                      '43306_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_2_AddingIterationProject',
                    value: 42,
                    hoverValue: {
                      Delivered: 77.5,
                      'Initially Commited': 180.5,
                    },
                    kpiGroup: 'Initial Commitment (Story Points)#Story',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '37',
                    sSprintID:
                      '43307_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_3_AddingIterationProject',
                    value: 37,
                    hoverValue: {
                      Delivered: 83,
                      'Initially Commited': 222,
                    },
                    kpiGroup: 'Initial Commitment (Story Points)#Story',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '39',
                    sSprintID:
                      '43308_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_4_AddingIterationProject',
                    value: 39,
                    hoverValue: {
                      Delivered: 78,
                      'Initially Commited': 198,
                    },
                    kpiGroup: 'Initial Commitment (Story Points)#Story',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '37',
                    sSprintID:
                      '43309_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_5_AddingIterationProject',
                    value: 37,
                    hoverValue: {
                      Delivered: 79.5,
                      'Initially Commited': 211.5,
                    },
                    kpiGroup: 'Initial Commitment (Story Points)#Story',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '65',
                    sSprintID:
                      '43310_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_6_AddingIterationProject',
                    value: 65,
                    hoverValue: {
                      Delivered: 123,
                      'Initially Commited': 187,
                    },
                    kpiGroup: 'Initial Commitment (Story Points)#Story',
                    sprojectName: 'AddingIterationProject',
                  },
                ],
                maturity: '2',
                maturityValue: '44',
              },
            ],
          },
          {
            filter1: 'Initial Commitment (Story Points)',
            filter2: 'Enabler Story',
            value: [
              {
                data: 'AddingIterationProject',
                value: [
                  {
                    data: '0',
                    sSprintID:
                      '43310_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_6_AddingIterationProject',
                    value: 0,
                    hoverValue: {
                      Delivered: 0,
                      'Initially Commited': 0,
                    },
                    kpiGroup: 'Initial Commitment (Story Points)#Enabler Story',
                    sprojectName: 'AddingIterationProject',
                  },
                ],
                maturity: '1',
                maturityValue: '0',
              },
            ],
          },
          {
            filter1: 'Initial Commitment (Story Points)',
            filter2: 'Defect',
            value: [
              {
                data: 'AddingIterationProject',
                value: [
                  {
                    data: '100',
                    sSprintID:
                      '43306_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_2_AddingIterationProject',
                    value: 100,
                    hoverValue: {
                      Delivered: 1,
                      'Initially Commited': 1,
                    },
                    kpiGroup: 'Initial Commitment (Story Points)#Defect',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '57',
                    sSprintID:
                      '43307_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_3_AddingIterationProject',
                    value: 57,
                    hoverValue: {
                      Delivered: 4,
                      'Initially Commited': 7,
                    },
                    kpiGroup: 'Initial Commitment (Story Points)#Defect',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '0',
                    sSprintID:
                      '43308_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_4_AddingIterationProject',
                    value: 0,
                    hoverValue: {
                      Delivered: 0,
                      'Initially Commited': 2,
                    },
                    kpiGroup: 'Initial Commitment (Story Points)#Defect',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '66',
                    sSprintID:
                      '43309_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_5_AddingIterationProject',
                    value: 66,
                    hoverValue: {
                      Delivered: 4,
                      'Initially Commited': 6,
                    },
                    kpiGroup: 'Initial Commitment (Story Points)#Defect',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '100',
                    sSprintID:
                      '43310_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_6_AddingIterationProject',
                    value: 100,
                    hoverValue: {
                      Delivered: 2,
                      'Initially Commited': 2,
                    },
                    kpiGroup: 'Initial Commitment (Story Points)#Defect',
                    sprojectName: 'AddingIterationProject',
                  },
                ],
                maturity: '3',
                maturityValue: '65',
              },
            ],
          },
          {
            filter1: 'Initial Commitment (Count)',
            filter2: 'Overall',
            value: [
              {
                data: 'AddingIterationProject',
                value: [
                  {
                    data: '53',
                    sSprintID:
                      '43306_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_2_AddingIterationProject',
                    value: 53,
                    hoverValue: {
                      Delivered: 26,
                      'Initially Commited': 49,
                    },
                    kpiGroup: 'Initial Commitment (Count)#overAll',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '43',
                    sSprintID:
                      '43307_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_3_AddingIterationProject',
                    value: 43,
                    hoverValue: {
                      Delivered: 24,
                      'Initially Commited': 55,
                    },
                    kpiGroup: 'Initial Commitment (Count)#overAll',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '46',
                    sSprintID:
                      '43308_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_4_AddingIterationProject',
                    value: 46,
                    hoverValue: {
                      Delivered: 29,
                      'Initially Commited': 62,
                    },
                    kpiGroup: 'Initial Commitment (Count)#overAll',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '42',
                    sSprintID:
                      '43309_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_5_AddingIterationProject',
                    value: 42,
                    hoverValue: {
                      Delivered: 27,
                      'Initially Commited': 63,
                    },
                    kpiGroup: 'Initial Commitment (Count)#overAll',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '64',
                    sSprintID:
                      '43310_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_6_AddingIterationProject',
                    value: 64,
                    hoverValue: {
                      Delivered: 37,
                      'Initially Commited': 57,
                    },
                    kpiGroup: 'Initial Commitment (Count)#overAll',
                    sprojectName: 'AddingIterationProject',
                  },
                ],
                maturity: '2',
                maturityValue: '50',
              },
            ],
          },
          {
            filter1: 'Initial Commitment (Count)',
            filter2: 'Story',
            value: [
              {
                data: 'AddingIterationProject',
                value: [
                  {
                    data: '48',
                    sSprintID:
                      '43306_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_2_AddingIterationProject',
                    value: 48,
                    hoverValue: {
                      Delivered: 22,
                      'Initially Commited': 45,
                    },
                    kpiGroup: 'Initial Commitment (Count)#Story',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '40',
                    sSprintID:
                      '43307_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_3_AddingIterationProject',
                    value: 40,
                    hoverValue: {
                      Delivered: 20,
                      'Initially Commited': 49,
                    },
                    kpiGroup: 'Initial Commitment (Count)#Story',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '45',
                    sSprintID:
                      '43308_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_4_AddingIterationProject',
                    value: 45,
                    hoverValue: {
                      Delivered: 26,
                      'Initially Commited': 57,
                    },
                    kpiGroup: 'Initial Commitment (Count)#Story',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '36',
                    sSprintID:
                      '43309_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_5_AddingIterationProject',
                    value: 36,
                    hoverValue: {
                      Delivered: 20,
                      'Initially Commited': 55,
                    },
                    kpiGroup: 'Initial Commitment (Count)#Story',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '65',
                    sSprintID:
                      '43310_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_6_AddingIterationProject',
                    value: 65,
                    hoverValue: {
                      Delivered: 34,
                      'Initially Commited': 52,
                    },
                    kpiGroup: 'Initial Commitment (Count)#Story',
                    sprojectName: 'AddingIterationProject',
                  },
                ],
                maturity: '2',
                maturityValue: '47',
              },
            ],
          },
          {
            filter1: 'Initial Commitment (Count)',
            filter2: 'Enabler Story',
            value: [
              {
                data: 'AddingIterationProject',
                value: [
                  {
                    data: '0',
                    sSprintID:
                      '43310_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_6_AddingIterationProject',
                    value: 0,
                    hoverValue: {
                      Delivered: 0,
                      'Initially Commited': 0,
                    },
                    kpiGroup: 'Initial Commitment (Count)#Enabler Story',
                    sprojectName: 'AddingIterationProject',
                  },
                ],
                maturity: '1',
                maturityValue: '0',
              },
            ],
          },
          {
            filter1: 'Initial Commitment (Count)',
            filter2: 'Defect',
            value: [
              {
                data: 'AddingIterationProject',
                value: [
                  {
                    data: '100',
                    sSprintID:
                      '43306_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_2_AddingIterationProject',
                    value: 100,
                    hoverValue: {
                      Delivered: 4,
                      'Initially Commited': 4,
                    },
                    kpiGroup: 'Initial Commitment (Count)#Defect',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '66',
                    sSprintID:
                      '43307_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_3_AddingIterationProject',
                    value: 66,
                    hoverValue: {
                      Delivered: 4,
                      'Initially Commited': 6,
                    },
                    kpiGroup: 'Initial Commitment (Count)#Defect',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '60',
                    sSprintID:
                      '43308_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_4_AddingIterationProject',
                    value: 60,
                    hoverValue: {
                      Delivered: 3,
                      'Initially Commited': 5,
                    },
                    kpiGroup: 'Initial Commitment (Count)#Defect',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '87',
                    sSprintID:
                      '43309_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_5_AddingIterationProject',
                    value: 87,
                    hoverValue: {
                      Delivered: 7,
                      'Initially Commited': 8,
                    },
                    kpiGroup: 'Initial Commitment (Count)#Defect',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '60',
                    sSprintID:
                      '43310_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_6_AddingIterationProject',
                    value: 60,
                    hoverValue: {
                      Delivered: 3,
                      'Initially Commited': 5,
                    },
                    kpiGroup: 'Initial Commitment (Count)#Defect',
                    sprojectName: 'AddingIterationProject',
                  },
                ],
                maturity: '4',
                maturityValue: '75',
              },
            ],
          },
          {
            filter1: 'Final Scope (Story Points)',
            filter2: 'Overall',
            value: [
              {
                data: 'AddingIterationProject',
                value: [
                  {
                    data: '47',
                    sSprintID:
                      '43306_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_2_AddingIterationProject',
                    value: 47,
                    hoverValue: {
                      Delivered: 91.5,
                      'Final Scope': 193.5,
                    },
                    kpiGroup: 'Final Scope (Story Points)#overAll',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '38',
                    sSprintID:
                      '43307_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_3_AddingIterationProject',
                    value: 38,
                    hoverValue: {
                      Delivered: 92.5,
                      'Final Scope': 241.5,
                    },
                    kpiGroup: 'Final Scope (Story Points)#overAll',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '43',
                    sSprintID:
                      '43308_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_4_AddingIterationProject',
                    value: 43,
                    hoverValue: {
                      Delivered: 101,
                      'Final Scope': 231.2,
                    },
                    kpiGroup: 'Final Scope (Story Points)#overAll',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '37',
                    sSprintID:
                      '43309_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_5_AddingIterationProject',
                    value: 37,
                    hoverValue: {
                      Delivered: 85.5,
                      'Final Scope': 228.5,
                    },
                    kpiGroup: 'Final Scope (Story Points)#overAll',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '59',
                    sSprintID:
                      '43310_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_6_AddingIterationProject',
                    value: 59,
                    hoverValue: {
                      Delivered: 131,
                      'Final Scope': 222,
                    },
                    kpiGroup: 'Final Scope (Story Points)#overAll',
                    sprojectName: 'AddingIterationProject',
                  },
                ],
                maturity: '2',
                maturityValue: '45',
              },
            ],
          },
          {
            filter1: 'Final Scope (Story Points)',
            filter2: 'Story',
            value: [
              {
                data: 'AddingIterationProject',
                value: [
                  {
                    data: '46',
                    sSprintID:
                      '43306_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_2_AddingIterationProject',
                    value: 46,
                    hoverValue: {
                      Delivered: 88.5,
                      'Final Scope': 189.5,
                    },
                    kpiGroup: 'Final Scope (Story Points)#Story',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '37',
                    sSprintID:
                      '43307_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_3_AddingIterationProject',
                    value: 37,
                    hoverValue: {
                      Delivered: 88.5,
                      'Final Scope': 234.5,
                    },
                    kpiGroup: 'Final Scope (Story Points)#Story',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '44',
                    sSprintID:
                      '43308_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_4_AddingIterationProject',
                    value: 44,
                    hoverValue: {
                      Delivered: 101,
                      'Final Scope': 229.2,
                    },
                    kpiGroup: 'Final Scope (Story Points)#Story',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '36',
                    sSprintID:
                      '43309_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_5_AddingIterationProject',
                    value: 36,
                    hoverValue: {
                      Delivered: 81.5,
                      'Final Scope': 222.5,
                    },
                    kpiGroup: 'Final Scope (Story Points)#Story',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '58',
                    sSprintID:
                      '43310_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_6_AddingIterationProject',
                    value: 58,
                    hoverValue: {
                      Delivered: 129,
                      'Final Scope': 219,
                    },
                    kpiGroup: 'Final Scope (Story Points)#Story',
                    sprojectName: 'AddingIterationProject',
                  },
                ],
                maturity: '2',
                maturityValue: '44',
              },
            ],
          },
          {
            filter1: 'Final Scope (Story Points)',
            filter2: 'Enabler Story',
            value: [
              {
                data: 'AddingIterationProject',
                value: [
                  {
                    data: '0',
                    sSprintID:
                      '43310_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_6_AddingIterationProject',
                    value: 0,
                    hoverValue: {
                      Delivered: 0,
                      'Final Scope': 0,
                    },
                    kpiGroup: 'Final Scope (Story Points)#Enabler Story',
                    sprojectName: 'AddingIterationProject',
                  },
                ],
                maturity: '1',
                maturityValue: '0',
              },
            ],
          },
          {
            filter1: 'Final Scope (Story Points)',
            filter2: 'Defect',
            value: [
              {
                data: 'AddingIterationProject',
                value: [
                  {
                    data: '75',
                    sSprintID:
                      '43306_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_2_AddingIterationProject',
                    value: 75,
                    hoverValue: {
                      Delivered: 3,
                      'Final Scope': 4,
                    },
                    kpiGroup: 'Final Scope (Story Points)#Defect',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '57',
                    sSprintID:
                      '43307_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_3_AddingIterationProject',
                    value: 57,
                    hoverValue: {
                      Delivered: 4,
                      'Final Scope': 7,
                    },
                    kpiGroup: 'Final Scope (Story Points)#Defect',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '0',
                    sSprintID:
                      '43308_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_4_AddingIterationProject',
                    value: 0,
                    hoverValue: {
                      Delivered: 0,
                      'Final Scope': 2,
                    },
                    kpiGroup: 'Final Scope (Story Points)#Defect',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '66',
                    sSprintID:
                      '43309_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_5_AddingIterationProject',
                    value: 66,
                    hoverValue: {
                      Delivered: 4,
                      'Final Scope': 6,
                    },
                    kpiGroup: 'Final Scope (Story Points)#Defect',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '66',
                    sSprintID:
                      '43310_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_6_AddingIterationProject',
                    value: 66,
                    hoverValue: {
                      Delivered: 2,
                      'Final Scope': 3,
                    },
                    kpiGroup: 'Final Scope (Story Points)#Defect',
                    sprojectName: 'AddingIterationProject',
                  },
                ],
                maturity: '2',
                maturityValue: '53',
              },
            ],
          },
          {
            filter1: 'Final Scope (Count)',
            filter2: 'Overall',
            value: [
              {
                data: 'AddingIterationProject',
                value: [
                  {
                    data: '63',
                    sSprintID:
                      '43306_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_2_AddingIterationProject',
                    value: 63,
                    hoverValue: {
                      Delivered: 41,
                      'Final Scope': 65,
                    },
                    kpiGroup: 'Final Scope (Count)#overAll',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '42',
                    sSprintID:
                      '43307_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_3_AddingIterationProject',
                    value: 42,
                    hoverValue: {
                      Delivered: 30,
                      'Final Scope': 71,
                    },
                    kpiGroup: 'Final Scope (Count)#overAll',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '52',
                    sSprintID:
                      '43308_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_4_AddingIterationProject',
                    value: 52,
                    hoverValue: {
                      Delivered: 47,
                      'Final Scope': 89,
                    },
                    kpiGroup: 'Final Scope (Count)#overAll',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '45',
                    sSprintID:
                      '43309_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_5_AddingIterationProject',
                    value: 45,
                    hoverValue: {
                      Delivered: 38,
                      'Final Scope': 83,
                    },
                    kpiGroup: 'Final Scope (Count)#overAll',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '64',
                    sSprintID:
                      '43310_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_6_AddingIterationProject',
                    value: 64,
                    hoverValue: {
                      Delivered: 53,
                      'Final Scope': 82,
                    },
                    kpiGroup: 'Final Scope (Count)#overAll',
                    sprojectName: 'AddingIterationProject',
                  },
                ],
                maturity: '2',
                maturityValue: '53',
              },
            ],
          },
          {
            filter1: 'Final Scope (Count)',
            filter2: 'Story',
            value: [
              {
                data: 'AddingIterationProject',
                value: [
                  {
                    data: '58',
                    sSprintID:
                      '43306_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_2_AddingIterationProject',
                    value: 58,
                    hoverValue: {
                      Delivered: 32,
                      'Final Scope': 55,
                    },
                    kpiGroup: 'Final Scope (Count)#Story',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '40',
                    sSprintID:
                      '43307_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_3_AddingIterationProject',
                    value: 40,
                    hoverValue: {
                      Delivered: 24,
                      'Final Scope': 60,
                    },
                    kpiGroup: 'Final Scope (Count)#Story',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '46',
                    sSprintID:
                      '43308_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_4_AddingIterationProject',
                    value: 46,
                    hoverValue: {
                      Delivered: 32,
                      'Final Scope': 69,
                    },
                    kpiGroup: 'Final Scope (Count)#Story',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '35',
                    sSprintID:
                      '43309_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_5_AddingIterationProject',
                    value: 35,
                    hoverValue: {
                      Delivered: 23,
                      'Final Scope': 65,
                    },
                    kpiGroup: 'Final Scope (Count)#Story',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '62',
                    sSprintID:
                      '43310_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_6_AddingIterationProject',
                    value: 62,
                    hoverValue: {
                      Delivered: 37,
                      'Final Scope': 59,
                    },
                    kpiGroup: 'Final Scope (Count)#Story',
                    sprojectName: 'AddingIterationProject',
                  },
                ],
                maturity: '2',
                maturityValue: '48',
              },
            ],
          },
          {
            filter1: 'Final Scope (Count)',
            filter2: 'Enabler Story',
            value: [
              {
                data: 'AddingIterationProject',
                value: [
                  {
                    data: '0',
                    sSprintID:
                      '43310_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_6_AddingIterationProject',
                    value: 0,
                    hoverValue: {
                      Delivered: 0,
                      'Final Scope': 1,
                    },
                    kpiGroup: 'Final Scope (Count)#Enabler Story',
                    sprojectName: 'AddingIterationProject',
                  },
                ],
                maturity: '1',
                maturityValue: '0',
              },
            ],
          },
          {
            filter1: 'Final Scope (Count)',
            filter2: 'Defect',
            value: [
              {
                data: 'AddingIterationProject',
                value: [
                  {
                    data: '90',
                    sSprintID:
                      '43306_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_2_AddingIterationProject',
                    value: 90,
                    hoverValue: {
                      Delivered: 9,
                      'Final Scope': 10,
                    },
                    kpiGroup: 'Final Scope (Count)#Defect',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '54',
                    sSprintID:
                      '43307_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_3_AddingIterationProject',
                    value: 54,
                    hoverValue: {
                      Delivered: 6,
                      'Final Scope': 11,
                    },
                    kpiGroup: 'Final Scope (Count)#Defect',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '75',
                    sSprintID:
                      '43308_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_4_AddingIterationProject',
                    value: 75,
                    hoverValue: {
                      Delivered: 15,
                      'Final Scope': 20,
                    },
                    kpiGroup: 'Final Scope (Count)#Defect',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '83',
                    sSprintID:
                      '43309_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_5_AddingIterationProject',
                    value: 83,
                    hoverValue: {
                      Delivered: 15,
                      'Final Scope': 18,
                    },
                    kpiGroup: 'Final Scope (Count)#Defect',
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '72',
                    sSprintID:
                      '43310_AddingIterationProject_64a4ff265b5fdd437756f904',
                    sSprintName:
                      'KnowHOW | PI_13| ITR_6_AddingIterationProject',
                    value: 72,
                    hoverValue: {
                      Delivered: 16,
                      'Final Scope': 22,
                    },
                    kpiGroup: 'Final Scope (Count)#Defect',
                    sprojectName: 'AddingIterationProject',
                  },
                ],
                maturity: '4',
                maturityValue: '75',
              },
            ],
          },
        ],
        maturityRange: ['-40', '40-60', '60-75', '75-90', '90-'],
        groupId: 2,
      },
    ];
    component.colorObj = {
      AddingIterationProject_64a4ff265b5fdd437756f904: {
        nodeName: 'AddingIterationProject',
        color: '#079FFF',
      },
    };

    const response = [
      {
        filterType: 'Select a filter',
        options: [
          'Initial Commitment (Story Points)',
          'Initial Commitment (Count)',
          'Final Scope (Story Points)',
          'Final Scope (Count)',
        ],
      },
      {
        filterType: 'Select a filter',
        options: ['Overall', 'Story', 'Enabler Story', 'Defect'],
      },
    ];
    const spyData = component.getDropdownArray(kpiId);
    expect(component.kpiDropdowns['kpi72']).toEqual(response);
  });

  it('should reload KPI once jira mapping saved ', () => {
    const KPiList = [
      {
        id: 'kpi1',
      },
    ];
    const fakeKPiDetails = {
      kpiDetails: {
        kpiSource: 'jira',
        kanban: true,
        groupId: 1,
      },
    };
    spyOn(service, 'getSelectedType').and.returnValue('kanban');
    spyOn(helperService, 'groupKpiFromMaster').and.returnValue({
      kpiList: KPiList,
    });
    const spy = spyOn(component, 'postJiraKanbanKpi');
    component.reloadKPI(fakeKPiDetails);
    expect(spy).toBeDefined();
  });

  it('should get kpi comments count', fakeAsync(() => {
    component.filterApplyData = {
      selectedMap: {
        project: ['KnowHOW_6360fefc3fa9e175755f0728'],
      },
      level: 5,
    };
    const response = {
      message: 'Found Comments Count',
      success: true,
      data: {
        kpi118: 1,
      },
    };

    component.kpiCommentsCountObj = {
      kpi118: 0,
    };
    component.updatedConfigGlobalData = [
      {
        kpiId: 'kpi118',
        kpiName: 'Deployment Frequency',
        isEnabled: true,
        order: 23,
        kpiDetail: {},
        shown: true,
      },
    ];
    spyOn(helperService, 'getKpiCommentsCount').and.returnValue(
      Promise.resolve({}),
    );
    component.getKpiCommentsCount();
    tick();
    expect(component.kpiCommentsCountObj).toBeDefined();
  }));

  it('should get kpi comments count when have kpi id', fakeAsync(() => {
    component.filterApplyData = {
      selectedMap: {
        project: ['KnowHOW_6360fefc3fa9e175755f0728'],
      },
      level: 5,
    };
    const response = {
      message: 'Found Comments Count',
      success: true,
      data: {
        kpi118: 1,
      },
    };

    component.kpiCommentsCountObj = {
      kpi118: 0,
    };
    component.updatedConfigGlobalData = [
      {
        kpiId: 'kpi118',
        kpiName: 'Deployment Frequency',
        isEnabled: true,
        order: 23,
        kpiDetail: {},
        shown: true,
      },
    ];
    spyOn(helperService, 'getKpiCommentsCount').and.resolveTo(response);
    component.getKpiCommentsCount('kpi118');
    tick();
    expect(component.kpiCommentsCountObj).toBeDefined();
  }));

  xit('should getchartdata for kpi when trendValueList is an object and with single filter', () => {
    component.allKpiArray = fakeDoraKpis;
    component.kpiSelectedFilterObj['kpi118'] = ['Overall'];
    const res = fakeDoraKpis[0].trendValueList.filter(
      (x) => x['filter'] == 'Overall',
    )[0];
    component.globalConfig = {
      kanban: [],
      scrum: [
        {
          boardSlug: 'test-board',
          boardName: 'test-board',
          kpis: ['kpi1', 'kpi118'],
        },
        { boardSlug: 'other-board', boardName: 'other-board', kpis: ['kpi3'] },
      ],
      others: [],
    };
    component.updatedConfigGlobalData = [
      {
        kpiId: 'kpi118',
        kpiName: 'Deployment Frequency',
        isEnabled: true,
        order: 23,
        kpiDetail: {},
        shown: true,
      },
    ];
    component.getChartData('kpi118', 0, 'sum');
    expect(component.kpiChartData['kpi118'][0]?.value.length).toEqual(
      res?.value[0]?.value?.length,
    );
  });

  xit('should getchartdata for kpi when trendValueList is an object and with multiple filter', () => {
    component.allKpiArray = fakeDoraKpis;
    component.kpiSelectedFilterObj['kpi118'] = [
      '81.200.188.111->KnowHOW',
      '81.200.188.112->KnowHOW',
    ];
    const res = fakeDoraKpiFilters;
    component.tooltip = {
      percentile: 90,
    };
    spyOn(helperService, 'applyAggregationLogic').and.callThrough();
    component.getChartData('kpi118', 0, 'sum');
    expect(component.kpiChartData['kpi118'][0]?.value?.length).toEqual(
      res?.value?.length,
    );
  });

  it('should create all kpi Table Heads when scrum is selected', () => {
    const tableHeadsArr = [
      {
        field: 'kpiName',
        header: 'Kpi Name',
      },
      {
        field: 'frequency',
        header: 'Frequency',
      },
      {
        field: 1,
        header: 1,
      },
      {
        field: 2,
        header: 2,
      },
      {
        field: 3,
        header: 3,
      },
      {
        field: 4,
        header: 4,
      },
      {
        field: 5,
        header: 5,
      },
      {
        field: 'trend',
        header: 'Trend',
      },
      {
        field: 'maturity',
        header: 'Maturity',
      },
    ];
    component.selectedtype = 'Scrum';
    component.noOfDataPoints = 5;
    component.kpiTableHeadingArr = [];
    component.createKpiTableHeads(component.selectedtype?.toLowerCase());
    expect(component.kpiTableHeadingArr?.length).toEqual(tableHeadsArr?.length);
  });

  it('should create all kpi Table Heads when kanban is selected', () => {
    const tableHeadsArr = [
      {
        field: 'kpiName',
        header: 'Kpi Name',
      },
      {
        field: 'frequency',
        header: 'Frequency',
      },
      {
        field: 1,
        header: 1,
      },
      {
        field: 2,
        header: 2,
      },
      {
        field: 3,
        header: 3,
      },
      {
        field: 4,
        header: 4,
      },
      {
        field: 5,
        header: 5,
      },
      {
        field: 'trend',
        header: 'Trend',
      },
      {
        field: 'maturity',
        header: 'Maturity',
      },
    ];
    component.selectedtype = 'Kanban';
    component.filterApplyData = {
      ids: [5],
    };
    component.noOfDataPoints = 5;
    component.kpiTableHeadingArr = [];
    component.createKpiTableHeads(component.selectedtype?.toLowerCase());
    expect(component.kpiTableHeadingArr?.length).toEqual(tableHeadsArr?.length);
  });

  it('should create all kpi array when trendValueList does not have filter', () => {
    const data = {
      kpi14: {
        kpiId: 'kpi14',
        kpiName: 'Defect Injection Rate',
        unit: '%',
        maxValue: '200',
        chartType: '',
        id: '64e72b51cab22644f44242f5',
        isDeleted: 'False',
        kpiUnit: '%',
        kanban: false,
        kpiSource: 'Jira',
      },
    };
    component.updatedConfigGlobalData = [
      {
        kpiId: 'kpi14',
        kpiName: 'Defect Injection Rate',
        isEnabled: true,
        order: 1,
        kpiDetail: {
          aggregationCriteria: 'average',
        },
        shown: true,
      },
    ];
    spyOn(component, 'ifKpiExist');
    const spy = spyOn(component, 'getChartData').and.callThrough();
    component.createAllKpiArray(data);
    expect(spy).toHaveBeenCalled();
  });

  xit('should create all kpi array when trendValueList has dropdown filter', () => {
    const data = {
      kpi28: {
        kpiId: 'kpi28',
        kpiName: 'Defect Count By Priority',
        unit: 'Number',
        maxValue: '90',
        chartType: '',
        id: '64e72b51cab22644f44242fb',
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
                data: 'KnowHOW',
                value: [
                  {
                    data: '4',
                    sSprintID: '43310_KnowHOW_6360fefc3fa9e175755f0728',
                    sSprintName: 'KnowHOW | PI_13| ITR_6_KnowHOW',
                    value: 4,
                    hoverValue: {
                      P1: 0,
                    },
                    kpiGroup: 'Overall',
                    sprojectName: 'KnowHOW',
                  },
                  {
                    data: '1',
                    sSprintID: '45160_KnowHOW_6360fefc3fa9e175755f0728',
                    sSprintName: 'KnowHOW | PI_14| ITR_1_KnowHOW',
                    value: 1,
                    hoverValue: {
                      P1: 0,
                    },
                    kpiGroup: 'Overall',
                    sprojectName: 'KnowHOW',
                  },
                  {
                    data: '6',
                    sSprintID: '45161_KnowHOW_6360fefc3fa9e175755f0728',
                    sSprintName: 'KnowHOW | PI_14| ITR_2_KnowHOW',
                    value: 6,
                    hoverValue: {
                      P1: 0,
                    },
                    kpiGroup: 'Overall',
                    sprojectName: 'KnowHOW',
                  },
                  {
                    data: '19',
                    sSprintID: '45162_KnowHOW_6360fefc3fa9e175755f0728',
                    sSprintName: 'KnowHOW | PI_14| ITR_3_KnowHOW',
                    value: 19,
                    hoverValue: {
                      P1: 1,
                    },
                    kpiGroup: 'Overall',
                    sprojectName: 'KnowHOW',
                  },
                  {
                    data: '3',
                    sSprintID: '45163_KnowHOW_6360fefc3fa9e175755f0728',
                    sSprintName: 'KnowHOW | PI_14| ITR_4_KnowHOW',
                    value: 3,
                    hoverValue: {
                      P1: 0,
                    },
                    kpiGroup: 'Overall',
                    sprojectName: 'KnowHOW',
                  },
                ],
              },
            ],
          },
          {
            filter: 'P1',
            value: [
              {
                data: 'KnowHOW',
                value: [
                  {
                    data: '4',
                    sSprintID: '43310_KnowHOW_6360fefc3fa9e175755f0728',
                    sSprintName: 'KnowHOW | PI_13| ITR_6_KnowHOW',
                    value: 4,
                    hoverValue: {
                      P1: 0,
                    },
                    kpiGroup: 'Overall',
                    sprojectName: 'KnowHOW',
                  },
                  {
                    data: '1',
                    sSprintID: '45160_KnowHOW_6360fefc3fa9e175755f0728',
                    sSprintName: 'KnowHOW | PI_14| ITR_1_KnowHOW',
                    value: 1,
                    hoverValue: {
                      P1: 0,
                    },
                    kpiGroup: 'Overall',
                    sprojectName: 'KnowHOW',
                  },
                  {
                    data: '6',
                    sSprintID: '45161_KnowHOW_6360fefc3fa9e175755f0728',
                    sSprintName: 'KnowHOW | PI_14| ITR_2_KnowHOW',
                    value: 6,
                    hoverValue: {
                      P1: 0,
                    },
                    kpiGroup: 'Overall',
                    sprojectName: 'KnowHOW',
                  },
                  {
                    data: '19',
                    sSprintID: '45162_KnowHOW_6360fefc3fa9e175755f0728',
                    sSprintName: 'KnowHOW | PI_14| ITR_3_KnowHOW',
                    value: 19,
                    hoverValue: {
                      P1: 1,
                    },
                    kpiGroup: 'Overall',
                    sprojectName: 'KnowHOW',
                  },
                  {
                    data: '3',
                    sSprintID: '45163_KnowHOW_6360fefc3fa9e175755f0728',
                    sSprintName: 'KnowHOW | PI_14| ITR_4_KnowHOW',
                    value: 3,
                    hoverValue: {
                      P1: 0,
                    },
                    kpiGroup: 'Overall',
                    sprojectName: 'KnowHOW',
                  },
                ],
              },
            ],
          },
        ],
      },
    };
    component.updatedConfigGlobalData = [
      {
        kpiId: 'kpi28',
        kpiName: 'Defect Injection Rate',
        isEnabled: true,
        order: 1,
        kpiDetail: {
          aggregationCriteria: 'average',
          kpiFilter: 'dropdown',
        },
        shown: true,
      },
    ];
    spyOn(component, 'ifKpiExist');
    component.kpiSelectedFilterObj['kpi28'] = {};
    spyOn(component, 'getDropdownArray');
    component.kpiSelectedFilterObj['action'] = 'new';
    spyOn(service, 'setKpiSubFilterObj');
    const spy = spyOn(component, 'getChartData').and.callThrough();
    component.createAllKpiArray(data);
    expect(spy).toHaveBeenCalled();
  });

  xit('should create all kpi array when trendValueList has radiobutton filter', () => {
    const data = {
      kpi126: {
        kpiId: 'kpi126',
        kpiName: 'Created vs Resolved defects',
        unit: 'Number',
        maxValue: '300',
        chartType: '',
        id: '64e72b51cab22644f44242fd',
        isDeleted: 'False',
        kpiUnit: 'Number',
        kanban: false,
        kpiSource: 'Jira',
        trendValueList: [
          {
            filter: 'Total Defects',
            value: [
              {
                data: 'AddingIterationProject',
                value: [
                  {
                    data: '38.0',
                    sSprintID:
                      '974_AddingIterationProject_64e739541426ba469c39c102',
                    sSprintName: 'DRP Sprint 76_AddingIterationProject',
                    value: 38,
                    hoverValue: {
                      createdDefects: 38,
                      resolvedDefects: 24,
                    },
                    kpiGroup: 'Total Defects',
                    sprintIds: [
                      '974_AddingIterationProject_64e739541426ba469c39c102',
                    ],
                    sprintNames: ['DRP Sprint 76_AddingIterationProject'],
                    lineValue: 24,
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '33.0',
                    sSprintID:
                      '975_AddingIterationProject_64e739541426ba469c39c102',
                    sSprintName: 'DRP Sprint 77_AddingIterationProject',
                    value: 33,
                    hoverValue: {
                      createdDefects: 33,
                      resolvedDefects: 28,
                    },
                    kpiGroup: 'Total Defects',
                    sprintIds: [
                      '975_AddingIterationProject_64e739541426ba469c39c102',
                    ],
                    sprintNames: ['DRP Sprint 77_AddingIterationProject'],
                    lineValue: 28,
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '23.0',
                    sSprintID:
                      '976_AddingIterationProject_64e739541426ba469c39c102',
                    sSprintName: 'DRP Sprint 78_AddingIterationProject',
                    value: 23,
                    hoverValue: {
                      createdDefects: 23,
                      resolvedDefects: 21,
                    },
                    kpiGroup: 'Total Defects',
                    sprintIds: [
                      '976_AddingIterationProject_64e739541426ba469c39c102',
                    ],
                    sprintNames: ['DRP Sprint 78_AddingIterationProject'],
                    lineValue: 21,
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '37.0',
                    sSprintID:
                      '977_AddingIterationProject_64e739541426ba469c39c102',
                    sSprintName: 'DRP Sprint 79_AddingIterationProject',
                    value: 37,
                    hoverValue: {
                      createdDefects: 37,
                      resolvedDefects: 28,
                    },
                    kpiGroup: 'Total Defects',
                    sprintIds: [
                      '977_AddingIterationProject_64e739541426ba469c39c102',
                    ],
                    sprintNames: ['DRP Sprint 79_AddingIterationProject'],
                    lineValue: 28,
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '32.0',
                    sSprintID:
                      '978_AddingIterationProject_64e739541426ba469c39c102',
                    sSprintName: 'DRP Sprint 80_AddingIterationProject',
                    value: 32,
                    hoverValue: {
                      createdDefects: 32,
                      resolvedDefects: 29,
                    },
                    kpiGroup: 'Total Defects',
                    sprintIds: [
                      '978_AddingIterationProject_64e739541426ba469c39c102',
                    ],
                    sprintNames: ['DRP Sprint 80_AddingIterationProject'],
                    lineValue: 29,
                    sprojectName: 'AddingIterationProject',
                  },
                ],
              },
            ],
          },
          {
            filter: 'Added Defects',
            value: [
              {
                data: 'AddingIterationProject',
                value: [
                  {
                    data: '30.0',
                    sSprintID:
                      '974_AddingIterationProject_64e739541426ba469c39c102',
                    sSprintName: 'DRP Sprint 76_AddingIterationProject',
                    value: 30,
                    hoverValue: {
                      createdDefects: 30,
                      resolvedDefects: 18,
                    },
                    kpiGroup: 'Added Defects',
                    sprintIds: [
                      '974_AddingIterationProject_64e739541426ba469c39c102',
                    ],
                    sprintNames: ['DRP Sprint 76_AddingIterationProject'],
                    lineValue: 18,
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '16.0',
                    sSprintID:
                      '975_AddingIterationProject_64e739541426ba469c39c102',
                    sSprintName: 'DRP Sprint 77_AddingIterationProject',
                    value: 16,
                    hoverValue: {
                      createdDefects: 16,
                      resolvedDefects: 14,
                    },
                    kpiGroup: 'Added Defects',
                    sprintIds: [
                      '975_AddingIterationProject_64e739541426ba469c39c102',
                    ],
                    sprintNames: ['DRP Sprint 77_AddingIterationProject'],
                    lineValue: 14,
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '21.0',
                    sSprintID:
                      '976_AddingIterationProject_64e739541426ba469c39c102',
                    sSprintName: 'DRP Sprint 78_AddingIterationProject',
                    value: 21,
                    hoverValue: {
                      createdDefects: 21,
                      resolvedDefects: 19,
                    },
                    kpiGroup: 'Added Defects',
                    sprintIds: [
                      '976_AddingIterationProject_64e739541426ba469c39c102',
                    ],
                    sprintNames: ['DRP Sprint 78_AddingIterationProject'],
                    lineValue: 19,
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '26.0',
                    sSprintID:
                      '977_AddingIterationProject_64e739541426ba469c39c102',
                    sSprintName: 'DRP Sprint 79_AddingIterationProject',
                    value: 26,
                    hoverValue: {
                      createdDefects: 26,
                      resolvedDefects: 20,
                    },
                    kpiGroup: 'Added Defects',
                    sprintIds: [
                      '977_AddingIterationProject_64e739541426ba469c39c102',
                    ],
                    sprintNames: ['DRP Sprint 79_AddingIterationProject'],
                    lineValue: 20,
                    sprojectName: 'AddingIterationProject',
                  },
                  {
                    data: '27.0',
                    sSprintID:
                      '978_AddingIterationProject_64e739541426ba469c39c102',
                    sSprintName: 'DRP Sprint 80_AddingIterationProject',
                    value: 27,
                    hoverValue: {
                      createdDefects: 27,
                      resolvedDefects: 24,
                    },
                    kpiGroup: 'Added Defects',
                    sprintIds: [
                      '978_AddingIterationProject_64e739541426ba469c39c102',
                    ],
                    sprintNames: ['DRP Sprint 80_AddingIterationProject'],
                    lineValue: 24,
                    sprojectName: 'AddingIterationProject',
                  },
                ],
              },
            ],
          },
        ],
      },
    };
    component.updatedConfigGlobalData = [
      {
        kpiId: 'kpi126',
        kpiName: 'Defect Injection Rate',
        isEnabled: true,
        order: 1,
        kpiDetail: {
          aggregationCriteria: 'average',
          kpiFilter: 'radiobutton',
          kpiUnit: 'Number',
        },
        shown: true,
      },
    ];
    spyOn(component, 'ifKpiExist');
    component.kpiSelectedFilterObj['kpi126'] = {};
    spyOn(component, 'getDropdownArray');
    component.kpiDropdowns['kpi126'] = [
      {
        filterType: 'Select a filter',
        options: ['Total Defects', 'Added Defects'],
      },
    ];
    component.kpiSelectedFilterObj['action'] = 'new';
    spyOn(service, 'setKpiSubFilterObj');
    const spy = spyOn(component, 'getChartData').and.callThrough();
    component.createAllKpiArray(data);
    expect(spy).toHaveBeenCalled();
  });

  it('should take care of loader untill full table data is loading', () => {
    component.kpiTableDataObj = {
      knowhow: [
        {
          kpiId: 'kpi123',
        },
        {
          kpiId: 'kpi1234',
        },
      ],
    };
    component.maturityTableKpiList = ['kpi123', 'kpi1234'];
    spyOn(component, 'ifKpiExist').and.returnValue(1);
    const spy = spyOn(service, 'setMaturiyTableLoader');
    component.handleMaturityTableLoader();
    expect(spy).toBeDefined();
  });

  describe('reloadKPI', () => {
    beforeEach(() => {
      spyOn(component, 'ifKpiExist').and.returnValue(1);
      spyOn(component.allKpiArray, 'splice');
      spyOn(helperService, 'groupKpiFromMaster').and.returnValue({
        kpiList: [{ kpiId: 1 }],
      });
    });

    it('should remove the kpi from allKpiArray if it exists', () => {
      const event = {
        kpiDetail: {
          kpiId: 1,
        },
      };

      component.reloadKPI(event);

      expect(component.ifKpiExist).toHaveBeenCalledWith(1);
      expect(component.allKpiArray.splice).toHaveBeenCalledWith(1, 1);
    });

    it('should group the kpi from master and call the appropriate post method for kanban view', () => {
      const event = {
        kpiDetail: {
          kpiId: 2,
          kpiSource: 'sonar',
          kanban: true,
          groupId: 'group1',
        },
      };
      spyOn(service, 'getSelectedType').and.returnValue('Kanban');
      const spyobj = spyOn(component, 'postSonarKanbanKpi');
      component.reloadKPI(event);

      expect(spyobj).toHaveBeenCalled();
    });

    it('should reload jenkins kanban KPI', () => {
      const event = {
        kpiDetail: {
          kpiId: 2,
          kpiSource: 'jenkins',
          kanban: true,
          groupId: 'group1',
        },
      };
      spyOn(service, 'getSelectedType').and.returnValue('Kanban');
      const spyobj = spyOn(component, 'postJenkinsKanbanKpi');
      component.reloadKPI(event);
      expect(spyobj).toHaveBeenCalled();
    });

    it('should reload zypher kanban KPI', () => {
      const event = {
        kpiDetail: {
          kpiId: 2,
          kpiSource: 'zypher',
          kanban: true,
          groupId: 'group1',
        },
      };
      spyOn(service, 'getSelectedType').and.returnValue('Kanban');
      const spyobj = spyOn(component, 'postZypherKanbanKpi');
      component.reloadKPI(event);
      expect(spyobj).toHaveBeenCalled();
    });

    it('should reload bitbucket kanban KPI', () => {
      const event = {
        kpiDetail: {
          kpiId: 2,
          kpiSource: 'bitbucket',
          kanban: true,
          groupId: 'group1',
        },
      };
      spyOn(service, 'getSelectedType').and.returnValue('Kanban');
      const spyobj = spyOn(component, 'postBitBucketKanbanKpi');
      component.reloadKPI(event);
      expect(spyobj).toHaveBeenCalled();
    });

    it('should reload sonar scrum KPI', () => {
      const event = {
        kpiDetail: {
          kpiId: 2,
          kpiSource: 'sonar',
          kanban: true,
          groupId: 'group1',
        },
      };
      spyOn(service, 'getSelectedType').and.returnValue('scrum');
      const spyobj = spyOn(component, 'postSonarKpi');
      component.reloadKPI(event);
      expect(spyobj).toHaveBeenCalled();
    });

    it('should reload jenkins scrum KPI', () => {
      const event = {
        kpiDetail: {
          kpiId: 2,
          kpiSource: 'jenkins',
          kanban: true,
          groupId: 'group1',
        },
      };
      spyOn(service, 'getSelectedType').and.returnValue('scrum');
      const spyobj = spyOn(component, 'postJenkinsKpi');
      component.reloadKPI(event);
      expect(spyobj).toHaveBeenCalled();
    });

    it('should reload zypher scrum KPI', () => {
      const event = {
        kpiDetail: {
          kpiId: 'kpi2',
          kpiSource: 'Zypher',
          kanban: true,
          groupId: 1,
        },
      };
      component.globalConfig = {
        kanban: [
          { boardSlug: 'test-board', boardName: 'test-board', kpis: ['kpi2'] },
          {
            boardSlug: 'other-board',
            boardName: 'other-board',
            kpis: ['kpi3'],
          },
        ],
        scrum: [],
        others: [],
      };
      component.selectedTab = 'test-board';
      component.updatedConfigGlobalData = [
        {
          kpiId: 'kpi2',
          kpiName: 'Deployment Frequency',
          isEnabled: true,
          order: 23,
          kpiDetail: {
            kpiId: 'kpi2',
            kpiSource: 'Zypher',
            kanban: true,
            groupId: 1,
          },
          shown: true,
        },
      ];
      spyOn(service, 'getSelectedType').and.returnValue('kanban');
      const spyobj = spyOn(component, 'postZypherKanbanKpi');
      component.reloadKPI(event);
      expect(spyobj).toHaveBeenCalled();
    });

    it('should reload bitbucket scrum KPI', () => {
      const event = {
        kpiDetail: {
          kpiId: 2,
          kpiSource: 'bitbucket',
          kanban: true,
          groupId: 'group1',
        },
      };
      spyOn(service, 'getSelectedType').and.returnValue('scrum');
      const spyobj = spyOn(component, 'postBitBucketKpi');
      component.reloadKPI(event);
      expect(spyobj).toHaveBeenCalled();
    });
  });

  describe('downloadGlobalExcel', () => {
    let workbook: Excel.Workbook;
    let worksheet: Excel.Worksheet;
    let saveAsSpy: jasmine.Spy;

    beforeEach(() => {
      workbook = new Excel.Workbook();
      worksheet = workbook.addWorksheet('Kpi Data');
      saveAsSpy = spyOn(fs, 'saveAs');
      spyOn(workbook, 'addWorksheet').and.returnValue(worksheet);

      spyOn(worksheet, 'addRow');
      spyOn(worksheet, 'eachRow');
      spyOn(worksheet, 'mergeCells');
    });

    it('should work download excel functionality', () => {
      spyOn(component.exportExcelComponent, 'downloadExcel');
      component.downloadExcel('kpi70', 'name', true, true, 'multiline');
      expect(exportExcelComponent).toBeDefined();
    });
  });

  it('should set the colorObj', () => {
    component.kpiChartData = {
      kpi121: {
        kpiId: 'kpi123',
      },
    };
    const x = {
      'Sample One_hierarchyLevelOne': {
        nodeName: 'Sample One',
        color: '#079FFF',
      },
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
  });

  it('should noTabAccess true when emp details available', () => {
    service.setEmptyData('test');
    fixture.detectChanges();
    component.ngOnInit();
    expect(component.noTabAccess).toBeTruthy();
  });

  it('postJiraKpi should call httpServicepost', fakeAsync(() => {
    component.filterApplyData = {
      label: 'project',
    };
    component.selectedTab = 'my-knowhow';
    // spyOn(component, 'getLastConfigurableTrendingListData');
    const jiraKpiData = {
      kpi14: {
        kpiId: 'kpi14',
        kpiName: 'Defect Injection Rate',
        unit: '%',
        maxValue: '200',
        chartType: '',
        id: '63355d7c41a0342c3790fb83',
        isDeleted: 'False',
        kpiUnit: '%',
        kanban: false,
        kpiSource: 'Jira',
        thresholdValue: 10,
        trendValueList: [],
        maturityRange: ['>=175', '175-125', '125-75', '75-25', '25-0'],
        groupId: 2,
      },
    };
    component.jiraKpiData = {};
    const spy = spyOn(httpService, 'postKpi').and.returnValue(of(null));
    spyOn(helperService, 'createKpiWiseId').and.returnValue(jiraKpiData);
    component.postJiraKpi(
      {
        kpiList: [{ kpiId: 'kpi141' }],
      },
      'jira',
    );
    tick();
    expect(spy).toHaveBeenCalled();
  }));

  it('should hadle of postJiraKanbanKpi', fakeAsync(() => {
    const jiraKpiData = {
      kpi14: {
        kpiId: 'kpi14',
        kpiName: 'Defect Injection Rate',
        unit: '%',
        maxValue: '200',
        chartType: '',
        id: '63355d7c41a0342c3790fb83',
        isDeleted: 'False',
        kpiUnit: '%',
        kanban: false,
        kpiSource: 'Jira',
        thresholdValue: 10,
        trendValueList: [],
        maturityRange: ['>=175', '175-125', '125-75', '75-25', '25-0'],
        groupId: 2,
      },
    };
    component.jiraKpiData = {};
    const spy = spyOn(httpService, 'postKpiKanban').and.returnValue(of(null));
    spyOn(helperService, 'createKpiWiseId').and.returnValue(jiraKpiData);
    component.postJiraKanbanKpi(
      {
        kpiList: [{ kpiId: 'kpi141' }],
      },
      'jira',
    );
    tick();
    expect(spy).toHaveBeenCalled();
  }));

  it('should getchartdata for kpi when trendValueList is arry with two filter', () => {
    component.allKpiArray = [
      {
        kpiId: 'kpi118',
        trendValueList: [
          {
            filter1: 'f1',
            filter2: 'f2',
          },
        ],
      },
    ];
    component.kpiSelectedFilterObj['kpi118'] = {
      filter1: 'f1',
      filter2: 'f2',
    };
    const res = fakeDoraKpiFilters;
    component.tooltip = {
      percentile: 90,
    };
    spyOn(helperService, 'applyAggregationLogic').and.callThrough();
    spyOn(component, 'createCombinations').and.returnValue([
      { filter1: 'f1', filter2: 'filter2' },
    ]);
    component.getChartData('kpi118', 0, 'sum');
    expect(component.kpiChartData).toBeDefined();
  });

  it('should getchartdata for kpi when trendValueList is arry with any one i.e filter1', () => {
    component.allKpiArray = [
      {
        kpiId: 'kpi118',
        trendValueList: [
          {
            filter1: 'f1',
          },
        ],
      },
    ];
    component.kpiSelectedFilterObj['kpi118'] = {
      filter1: 'f1',
    };
    const res = fakeDoraKpiFilters;
    component.tooltip = {
      percentile: 90,
    };
    spyOn(helperService, 'applyAggregationLogic').and.callThrough();
    spyOn(component, 'createCombinations').and.returnValue([
      { filter1: 'f1', filter2: 'filter2' },
    ]);
    component.getChartData('kpi118', 0, 'sum');
    expect(component.kpiChartData).toBeDefined();
  });

  it('should getchartdata for kpi when kpiSelectedFilterObj do not have filter1 and filter2', () => {
    component.allKpiArray = [
      {
        kpiId: 'kpi118',
        trendValueList: [
          {
            filter1: 'f1',
          },
        ],
      },
    ];
    component.kpiSelectedFilterObj['kpi118'] = {
      filter: 'f1',
    };
    const res = fakeDoraKpiFilters;
    component.tooltip = {
      percentile: 90,
    };
    spyOn(helperService, 'applyAggregationLogic').and.callThrough();
    spyOn(component, 'createCombinations').and.returnValue([
      { filter1: 'f1', filter2: 'filter2' },
    ]);
    component.getChartData('kpi118', 0, 'sum');
    expect(component.kpiChartData).toBeDefined();
  });

  it('should getchartdata for kpi17', () => {
    component.allKpiArray = [
      {
        kpiId: 'kpi17',
        trendValueList: [
          {
            filter: 'f1',
            value: [{ value: 5 }],
          },
          {
            filter: 'f2',
            value: [{ value: 10 }],
          },
        ],
      },
    ];
    component.kpiSelectedFilterObj['kpi17'] = ['f1', 'f2'];
    const res = fakeDoraKpiFilters;
    component.tooltip = {
      percentile: 90,
    };
    spyOn(helperService, 'applyAggregationLogic').and.callThrough();
    spyOn(component, 'createCombinations').and.returnValue([
      { filter1: 'f1', filter2: 'filter2' },
    ]);
    component.getChartData('kpi17', 0, 'sum');
    expect(component.kpiChartData).toBeDefined();
  });

  it('should getchartdata for kpi17 and filter is average coverage', () => {
    component.allKpiArray = [
      {
        kpiId: 'kpi17',
        trendValueList: [
          {
            filter: 'average coverage',
            value: [{ value: 5 }],
          },
          {
            filter: 'f2',
            value: [{ value: 10 }],
          },
        ],
      },
    ];
    component.kpiSelectedFilterObj['kpi17'] = ['average coverage'];
    const res = fakeDoraKpiFilters;
    component.tooltip = {
      percentile: 90,
    };
    spyOn(helperService, 'applyAggregationLogic').and.callThrough();
    spyOn(component, 'createCombinations').and.returnValue([
      { filter1: 'f1', filter2: 'filter2' },
    ]);
    component.getChartData('kpi17', 0, 'sum');
    expect(component.kpiChartData).toBeDefined();
  });

  it('should preapare column of kpi3', () => {
    component.allKpiArray = [
      {
        kpiId: 'kpi3',
        trendValueList: [
          {
            filter: 'average coverage',
            value: [
              {
                value: [{ data: 0 }],
              },
            ],
          },
          {
            filter: 'f2',
            value: [{ value: 10 }],
          },
        ],
      },
    ];
    component.hierarchyLevel = [
      {
        hierarchyLevelName: 'h1',
      },
      {
        hierarchyLevelName: 'h2',
      },
    ];
    (component.filterApplyData = {
      ids: ['bittest_corporate'],
      sprintIncluded: ['CLOSED'],
      selectedMap: {
        business: [],
        account: [],
        subaccount: [],
        project: [],
        sprint: [],
        sqd: [],
      },
      level: 1,
    }),
      (component.kpiSelectedFilterObj['kpi3'] = ['average coverage']);
    component.tooltip = {
      percentile: 90,
    };
    spyOn(helperService, 'applyAggregationLogic').and.callThrough();
    spyOn(component, 'createCombinations').and.returnValue([
      { filter1: 'f1', filter2: 'filter2' },
    ]);
    component.getChartData('kpi3', 0, 'sum');
    expect(component.kpiChartData).toBeDefined();
  });

  xit('should generate colorObj for kpi17', () => {
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
            xName: 1,
          },
          {
            data: '0',
            value: 0,
            hoverValue: {},
            date: '2022-09-12 to 2022-09-18',
            sprojectName: 'bittest',
            xName: 2,
          },
          {
            data: '0',
            value: 0,
            hoverValue: {},
            date: '2022-09-19 to 2022-09-25',
            sprojectName: 'bittest',
            xName: 3,
          },
          {
            data: '0',
            value: 0,
            hoverValue: {},
            date: '2022-09-26 to 2022-10-02',
            sprojectName: 'bittest',
            xName: 4,
          },
          {
            data: '0',
            value: 0,
            hoverValue: {},
            date: '2022-10-03 to 2022-10-09',
            sprojectName: 'bittest',
            xName: 5,
          },
        ],
        maturity: '5',
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
            projectNames: ['Bus1', 'TestB'],
            sprojectName: 'Corpate1',
            xName: 1,
          },
          {
            data: '0.0',
            value: 0,
            hoverValue: {},
            date: '2022-09-12 to 2022-09-18',
            sprintIds: [],
            sprintNames: [],
            projectNames: ['Bus1', 'TestB'],
            sprojectName: 'Corpate1',
            xName: 2,
          },
          {
            data: '0.0',
            value: 0,
            hoverValue: {},
            date: '2022-09-19 to 2022-09-25',
            sprintIds: [],
            sprintNames: [],
            projectNames: ['Bus1', 'TestB'],
            sprojectName: 'Corpate1',
            xName: 3,
          },
          {
            data: '0.0',
            value: 0,
            hoverValue: {},
            date: '2022-09-26 to 2022-10-02',
            sprintIds: [],
            sprintNames: [],
            projectNames: ['Bus1', 'TestB'],
            sprojectName: 'Corpate1',
            xName: 4,
          },
          {
            data: '0.0',
            value: 0,
            hoverValue: {},
            date: '2022-10-03 to 2022-10-09',
            sprintIds: [],
            sprintNames: [],
            projectNames: ['Bus1', 'TestB'],
            sprojectName: 'Corpate1',
            xName: 5,
          },
        ],
        maturity: '4',
      },
    ];
    component.colorObj = {
      bittest_corporate: {
        nodeName: 'bittest',
        color: '#079FFF',
      },
      Corpate1_corporate: {
        nodeName: 'Corpate1',
        color: '#cdba38',
      },
    };
    component.chartColorList = {};
    component.generateColorObj('kpi17', arr);
    expect(component.chartColorList['kpi17'].length).toEqual(2);
  });

  it('should set the hierarchyLevel to the value of the selected type in the completeHierarchyData from localStorage', () => {
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
            yaxisLabel: 'Percentage',
          },
        ],
      },
      filterData: [],
      filterApplyData: {
        ids: ['bittest_corporate'],
        sprintIncluded: ['CLOSED'],
        selectedMap: {
          corporate: ['bittest_corporate'],
          business: [],
          account: [],
          subaccount: [],
          project: [],
          sprint: [],
          sqd: [],
        },
        level: 1,
      },
      selectedTab: 'my knowhow',
      isAdditionalFilters: false,
    };
    component.serviceObject = {
      makeAPICall: true,
    };
    component.colorObj = {
      node1_color: 'red',
      node2_color: 'green',
      node3_color: 'blue',
    };
    component.kpiTableDataObj = {
      node1: ['data1'],
      node2: ['data2'],
      node3: ['data3'],
    };
    const localDate = {
      scrum: 'test1',
      kanban: 'test2',
    };
    component.selectedtype = 'scrum';
    localStorage.setItem('completeHierarchyData', JSON.stringify(localDate));
    component.receiveSharedData(event);
    expect(component.noTabAccess).toBe(false);
  });

  it('should return -1 if a.key is "Select"', () => {
    const a = { key: 'Select' };
    const b = { key: 'SomeKey' };
    const result = component.originalOrder(a, b);
    expect(result).toBe(-1);
  });

  it('should return 0 if a.key and b.key are the same', () => {
    const a = { key: 'SomeKey' };
    const b = { key: 'SomeKey' };
    const result = component.originalOrder(a, b);
    expect(result).toBeDefined();
  });

  it('should get table data for kpi when trendValueList dont have filter when kpi name is availiable', () => {
    component.allKpiArray = [
      {
        kpiName: 'abc',
      },
    ];
    component.kpiTableHeadingArr = fakeKpiTableHeadingArray;
    component.noOfDataPoints = 5;
    component.colorObj = {
      AddingIterationProject_64e739541426ba469c39c102: {
        nodeName: 'AddingIterationProject',
        color: '#079FFF',
      },
    };
    component.kpiTableDataObj['AddingIterationProject'] = [];
    const enabledKpi = {
      kpiDetail: {
        xaxisLabel: 'Sprints',
      },
      isEnabled: true,
      shown: true,
      order: '1',
    };
    const returnedObj = {
      AddingIterationProject: [
        {
          '1': '122.6',
          '2': '126.9',
          '3': '176.5',
          '4': '83.3',
          '5': '57.7',
          kpiId: 'kpi14',
          kpiName: 'Defect Injection Rate',
          frequency: 'Sprints',
          show: true,
          hoverText: [
            '1 - DRP Sprint 71_AddingIterationProject',
            '2 - DRP Sprint 72_AddingIterationProject',
            '3 - DRP Sprint 73_AddingIterationProject',
            '4 - DRP Sprint 74_AddingIterationProject',
            '5 - DRP Sprint 75_AddingIterationProject',
          ],
          latest: '85 %',
          trend: '-ve',
          maturity: 'M3',
          order: '1',
        },
      ],
    };

    component.getTableData('kpi14', 0, enabledKpi);
    expect(component.kpiTableDataObj['AddingIterationProject']?.length).toEqual(
      returnedObj['AddingIterationProject']?.length,
    );
  });

  it('should sort the rows in the kpfableDataObj for the provided hierarchyName by order', () => {
    component.kpiTableDataObj = {
      hierarchy1: [{ order: 3 }, { order: 1 }, { order: 2 }],
      hierarchy2: [{ order: 2 }, { order: 1 }, { order: 3 }],
    };
    const hierarchyName = 'hierarchy1';
    component.sortingRowsInTable(hierarchyName);
    expect(component.kpiTableDataObj[hierarchyName]).toEqual([
      { order: 1 },
      { order: 2 },
      { order: 3 },
    ]);
  });

  it('should not sort the rows in the kpiTableDataObj if the hierarchyName does not exist', () => {
    component.kpiTableDataObj = {
      hierarchy1: [{ order: 3 }, { order: 1 }, { order: 2 }],
      hierarchy2: [{ order: 2 }, { order: 1 }, { order: 3 }],
    };
    const hierarchyName = 'hierarchy3';
    component.sortingRowsInTable(hierarchyName);
    expect(component.kpiTableDataObj[hierarchyName]).toBeUndefined();
  });

  xit('should create trend data for kpi kpi17', () => {
    component.updatedConfigGlobalData = [
      {
        kpiId: 'kpi17',
      },
    ];
    component.kpiChartData = {
      kpi17: [
        {
          filter: 'average coverage',
        },
      ],
    };
    spyOn(component, 'checkLatestAndTrendValue').and.returnValue([]);
    component.createTrendsData('kpi17');
    expect(component.kpiChartData).toBeDefined();
  });

  xit('should handle selected option when triggerAdditionalFilters is triggered and selectedTab is developer', () => {
    spyOn(component, 'handleSelectedOption');
    const data = { key: 'value' };
    component.service.triggerAdditionalFilters.next(data);
    component.selectedTab = 'developer';
    fixture.detectChanges();
    expect(component.handleSelectedOption).toHaveBeenCalled();
  });

  xit('should not handle selected option when triggerAdditionalFilters is triggered and selectedTab is not developer', () => {
    spyOn(component, 'handleSelectedOption');
    component.selectedTab = 'admin';
    const data = { key: 'value' };
    component.service.triggerAdditionalFilters.next(data);
    expect(component.handleSelectedOption).not.toHaveBeenCalled();
  });

  it('should not handle selected option when triggerAdditionalFilters is triggered and data is empty', () => {
    spyOn(component, 'handleSelectedOption');
    component.selectedTab = 'developer';
    const data = {};
    component.service.triggerAdditionalFilters.next(data);
    expect(component.handleSelectedOption).not.toHaveBeenCalled();
  });

  xit('should handle selected option for each kpi in updatedConfigGlobalData', () => {
    spyOn(component, 'handleSelectedOption');
    component.selectedTab = 'developer';
    const data = { key: 'value' };
    component.updatedConfigGlobalData = [
      { kpi1: 'value1' },
      { kpi2: 'value2' },
    ];
    component.service.triggerAdditionalFilters.next(data);
    expect(component.handleSelectedOption).toHaveBeenCalledTimes(2);
  });

  it('should return the correct latest value, trend value, and unit when item.value is not empty and trendCalculative is true', () => {
    const kpiData = {
      kpiDetail: {
        kpiUnit: 'Number',
        showTrend: true,
        trendCalculative: true,
        trendCalculation: [
          { lhs: 'lhsKey', rhs: 'rhsKey', operator: '>' },
          { lhs: 'lhsKey', rhs: 'rhsKey', operator: '<' },
        ],
      },
    };
    const item = {
      value: [
        {
          dataValue: [
            { lineType: 'solid', value: 10 },
            { lineType: 'dotted', value: 20 },
          ],
          lhsKey: 5,
          rhsKey: 3,
        },
      ],
    };
    const expectedLatest = '10';
    const expectedTrend = '--';
    const expectedUnit = '';

    const [actualLatest, actualTrend, actualUnit] =
      component.checkLatestAndTrendValue(kpiData, item);

    expect(actualLatest).toEqual(expectedLatest);
    expect(actualTrend).toEqual(expectedTrend);
    expect(actualUnit).toEqual(expectedUnit);
  });

  it('should return the correct latest value, trend value, and unit when item.value is not empty and trendCalculative is false', () => {
    const kpiData = {
      kpiDetail: {
        kpiUnit: 'Number',
        showTrend: true,
        trendCalculative: false,
        isPositiveTrend: true,
      },
    };
    const item = {
      value: [
        {
          dataValue: [
            { lineType: 'solid', value: 10 },
            { lineType: 'dotted', value: 20 },
          ],
        },
        {
          dataValue: [
            { lineType: 'solid', value: 5 },
            { lineType: 'dotted', value: 15 },
          ],
        },
      ],
    };
    const expectedLatest = '5';
    const expectedTrend = '-ve';
    const expectedUnit = '';

    const [actualLatest, actualTrend, actualUnit] =
      component.checkLatestAndTrendValue(kpiData, item);

    expect(actualLatest).toEqual(expectedLatest);
    expect(actualTrend).toEqual(expectedTrend);
    expect(actualUnit).toEqual(expectedUnit);
  });

  it('should return the correct latest value, trend value, and unit when item.value is empty', () => {
    const kpiData = {
      kpiDetail: {
        kpiUnit: 'Number',
        showTrend: true,
        trendCalculative: true,
        trendCalculation: [
          { lhs: 'lhsKey', rhs: 'rhsKey', operator: '>' },
          { lhs: 'lhsKey', rhs: 'rhsKey', operator: '<' },
        ],
      },
    };
    const item = {
      value: [],
    };
    const expectedLatest = '';
    const expectedTrend = 'NA';
    const expectedUnit = '';

    const [actualLatest, actualTrend, actualUnit] =
      component.checkLatestAndTrendValue(kpiData, item);

    expect(actualLatest).toEqual(expectedLatest);
    expect(actualTrend).toEqual(expectedTrend);
    // expect(actualUnit).toEqual(expectedUnit);
  });

  it('should return the correct latest value, trend value, and unit when item.value is not empty and kpiDetail.showTrend is false', () => {
    const kpiData = {
      kpiDetail: {
        kpiUnit: 'Number',
        showTrend: false,
      },
    };
    const item = {
      value: [
        {
          dataValue: [
            { lineType: 'solid', value: 10 },
            { lineType: 'dotted', value: 20 },
          ],
        },
      ],
    };
    const expectedLatest = '10';
    const expectedTrend = 'NA';
    const expectedUnit = '';

    const [actualLatest, actualTrend, actualUnit] =
      component.checkLatestAndTrendValue(kpiData, item);

    expect(actualLatest).toEqual(expectedLatest);
    expect(actualTrend).toEqual(expectedTrend);
    // expect(actualUnit).toEqual(expectedUnit);
  });

  it('should return the correct latest value, trend value, and unit when item.value is empty and kpiDetail.showTrend is false', () => {
    const kpiData = {
      kpiDetail: {
        kpiUnit: 'Number',
        showTrend: false,
      },
    };
    const item = {
      value: [],
    };
    const expectedLatest = '';
    const expectedTrend = 'NA';
    const expectedUnit = '';

    const [actualLatest, actualTrend, actualUnit] =
      component.checkLatestAndTrendValue(kpiData, item);

    expect(actualLatest).toEqual(expectedLatest);
    expect(actualTrend).toEqual(expectedTrend);
    // expect(actualUnit).toEqual(expectedUnit);
  });

  it('should sort the array alphabetically when the array has more than one element', () => {
    const objArray = [
      { data: 'Carrot' },
      { data: 'Apple' },
      { data: 'Banana' },
    ];
    const expectedArray = [
      { data: 'Apple' },
      { data: 'Banana' },
      { data: 'Carrot' },
    ];

    const sortedArray = component.sortAlphabetically(objArray);

    expect(sortedArray).toEqual(expectedArray);
  });

  it('should not modify the array when the array has only one element', () => {
    const objArray = [{ data: 'Apple' }];
    const expectedArray = [{ data: 'Apple' }];

    const sortedArray = component.sortAlphabetically(objArray);

    expect(sortedArray).toEqual(expectedArray);
  });

  it('should return an empty array when the input array is empty', () => {
    const objArray = [];
    const expectedArray = [];

    const sortedArray = component.sortAlphabetically(objArray);

    expect(sortedArray).toEqual(expectedArray);
  });

  it('should return null when the input array is null', () => {
    const objArray = null;
    const expectedArray = null;

    const sortedArray = component.sortAlphabetically(objArray);

    expect(sortedArray).toEqual(expectedArray);
  });

  it('getChartDataforRelease should set the kpiChartData array correctly when kpiId is not kpi178 and trendValueList is an object', () => {
    const kpiId = 'kpi123';
    const idx = 0;
    const aggregationType = 'sum';
    const kpiFilterChange = false;
    component.allKpiArray = [{}];
    component.allKpiArray[idx].trendValueList = {
      value: [
        { filter1: 'Overall', value: 50 },
        { filter1: 'Filter1', value: 20 },
        { filter1: 'Filter2', value: 30 },
      ],
    };
    const expectedChartData = [{ filter1: 'Overall', value: 50 }];

    component.getChartDataforRelease(
      kpiId,
      idx,
      aggregationType,
      kpiFilterChange,
    );
    const actualChartData = component.kpiChartData[kpiId];

    expect(actualChartData).toEqual(expectedChartData);
  });

  it('getChartDataforRelease should set the kpiChartData array correctly when kpiId is not kpi178 and trendValueList is an array', () => {
    const kpiId = 'kpi123';
    const idx = 0;
    const aggregationType = 'sum';
    const kpiFilterChange = false;
    component.allKpiArray = [{}];
    component.allKpiArray[idx].trendValueList = {
      value: [
        { filter1: 'Overall', value: 50 },
        { filter1: 'Filter1', value: 20 },
        { filter1: 'Filter2', value: 30 },
      ],
    };
    const expectedChartData = [{ filter1: 'Overall', value: 50 }];

    component.getChartDataforRelease(
      kpiId,
      idx,
      aggregationType,
      kpiFilterChange,
    );
    const actualChartData = component.kpiChartData[kpiId];

    expect(actualChartData).toEqual(expectedChartData);
  });

  it('getChartDataforRelease should set the kpiChartData array correctly when kpiId is kpi178', () => {
    const kpiId = 'kpi178';
    const idx = 0;
    const aggregationType = 'sum';
    const kpiFilterChange = false;
    component.allKpiArray = [{}];
    component.allKpiArray[idx] = {
      kpiId: 'kpi178',
      kpiName: 'Defect Count By',
      chartType: '',
      id: '665f0e93bc80f461490c646a',
      isDeleted: 'False',
      kpiCategory: 'Release',
      kanban: false,
      kpiSource: 'Jira',
      groupId: 9,
      sprint: 'Phase 1_API POD 2 - Account Management',
      modalHeads: [
        'Issue Id',
        'Issue Description',
        'Sprint Name',
        'Issue Type',
        'Issue Status',
        'Root Cause List',
        'Priority',
        'Testing Phase',
        'Assignee',
      ],
      issueData: [
        {
          'Issue Id': 'MAPIECP-1759',
          'Issue URL':
            'https://tools.publicis.sapient.com/jira/browse/MAPIECP-1759',
          'Issue Description':
            'Dev Storefront | Unable to login into the application and encountering CORS issue',
          'Issue Status': 'Closed',
          'Issue Type': 'Bug',
          'Size(story point/hours)': '.0',
          'Logged Work': '0d',
          'Original Estimate': '0d',
          Priority: 'P1 - Blocker',
          'Due Date': '-',
          'Remaining Estimate': '0d',
          'Remaining Days': '0d',
          'Dev Due Date': '-',
          Assignee: 'Nidhi Goyal',
          'Change Date': '2024-01-26',
          Labels: [],
          'Created Date': '2023-11-13',
          'Root Cause List': ['None'],
          'Owner Full Name': ['Nidhi Goyal'],
          'Sprint Name': 'Sprint 10',
          Resolution: 'Fixed or Completed',
          'Release Name': 'Phase 1',
          'Updated Date': '2024-01-26',
          'Testing Phase': ['Undefined'],
        },
      ],
      filterGroup: {
        filterGroup1: [
          {
            filterKey: 'Issue Status',
            filterName: 'Status',
            filterType: 'Single',
            order: 1,
          },
          {
            filterKey: 'Priority',
            filterName: 'Priority',
            filterType: 'Single',
            order: 2,
          },
          {
            filterKey: 'Root Cause List',
            filterName: 'RCA',
            filterType: 'Multi',
            order: 3,
          },
        ],
        filterGroup2: [
          {
            filterKey: 'Assignee',
            filterName: 'Assignee',
            filterType: 'Single',
            order: 1,
          },
          {
            filterKey: 'Testing Phase',
            filterName: 'Testing Phase',
            filterType: 'Multi',
            order: 2,
          },
        ],
      },
      kpiInfo: {
        definition:
          'It shows the breakup of all defects tagged to a release grouped by Status, Priority, or RCA.',
        details: [
          {
            type: 'link',
            kpiLinkDetail: {
              text: 'Detailed Information at',
              link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/144146433/Release+Defect+count+by',
            },
          },
        ],
      },
    };
    const expectedChartData = [
      {
        data: [
          {
            'Issue Id': 'MAPIECP-1759',
            'Issue URL':
              'https://tools.publicis.sapient.com/jira/browse/MAPIECP-1759',
            'Issue Description':
              'Dev Storefront | Unable to login into the application and encountering CORS issue',
            'Issue Status': 'Closed',
            'Issue Type': 'Bug',
            'Size(story point/hours)': '.0',
            'Logged Work': '0d',
            'Original Estimate': '0d',
            Priority: 'P1 - Blocker',
            'Due Date': '-',
            'Remaining Estimate': '0d',
            'Remaining Days': '0d',
            'Dev Due Date': '-',
            Assignee: 'Nidhi Goyal',
            'Change Date': '2024-01-26',
            Labels: [],
            'Created Date': '2023-11-13',
            'Root Cause List': ['None'],
            'Owner Full Name': ['Nidhi Goyal'],
            'Sprint Name': 'Sprint 10',
            Resolution: 'Fixed or Completed',
            'Release Name': 'Phase 1',
            'Updated Date': '2024-01-26',
            'Testing Phase': ['Undefined'],
          },
        ],
        filters: {
          filterGroup1: [
            {
              filterKey: 'Issue Status',
              filterName: 'Status',
              filterType: 'Single',
              order: 1,
            },
            {
              filterKey: 'Priority',
              filterName: 'Priority',
              filterType: 'Single',
              order: 2,
            },
            {
              filterKey: 'Root Cause List',
              filterName: 'RCA',
              filterType: 'Multi',
              order: 3,
            },
          ],
          filterGroup2: [
            {
              filterKey: 'Assignee',
              filterName: 'Assignee',
              filterType: 'Single',
              order: 1,
            },
            {
              filterKey: 'Testing Phase',
              filterName: 'Testing Phase',
              filterType: 'Multi',
              order: 2,
            },
          ],
        },
        modalHeads: [
          'Issue Id',
          'Issue Description',
          'Sprint Name',
          'Issue Type',
          'Issue Status',
          'Root Cause List',
          'Priority',
          'Testing Phase',
          'Assignee',
        ],
      },
    ];

    component.getChartDataforRelease(
      kpiId,
      idx,
      aggregationType,
      kpiFilterChange,
    );
    const actualChartData = component.kpiChartData[kpiId];

    expect(actualChartData).toEqual(expectedChartData);
  });

  it('getChartDataforRelease should set the kpiChartData array correctly when kpiId is not kpi178 and trendValueList is null', () => {
    const kpiId = 'kpi123';
    const idx = 0;
    const aggregationType = 'sum';
    const kpiFilterChange = false;
    component.allKpiArray = [{}];
    component.allKpiArray[idx].trendValueList = null;
    const expectedChartData = [{}];

    component.getChartDataforRelease(
      kpiId,
      idx,
      aggregationType,
      kpiFilterChange,
    );
    const actualChartData = component.kpiChartData[kpiId];

    expect(actualChartData).toEqual(expectedChartData);
  });

  it('getChartDataforRelease should set the kpiChartData array correctly when kpiId is not kpi178 and trendValueList is an object with no value property', () => {
    const kpiId = 'kpi123';
    const idx = 0;
    const aggregationType = 'sum';
    const kpiFilterChange = false;
    component.allKpiArray = [{}];
    component.allKpiArray[idx].trendValueList = {
      filter1: 'Overall',
      filter2: 'All',
    };
    const expectedChartData = [{ filter1: 'Overall', filter2: 'All' }];

    component.getChartDataforRelease(
      kpiId,
      idx,
      aggregationType,
      kpiFilterChange,
    );
    const actualChartData = component.kpiChartData[kpiId];

    expect(actualChartData).toEqual(expectedChartData);
  });

  xit('getChartData should set additional filters on developer tab', () => {
    component.selectedTab = 'developer';
    component.allKpiArray = [
      {
        kpiId: 'kpi84',
        kpiName: 'Mean Time To Merge',
        unit: 'Hours',
        chartType: '',
        id: '65793ddb127be336160bc0d3',
        isDeleted: 'False',
        kpiCategory: 'Developer',
        kpiUnit: 'Hours',
        kanban: false,
        kpiSource: 'BitBucket',
        thresholdValue: 55,
        maturityRange: ['-48', '48-16', '16-8', '8-4', '4-'],
        groupId: 1,
        responseCode: '200',
        trendValueList: [
          {
            filter: 'Overall',
            value: [
              {
                data: 'PSknowHOW',
                maturity: '4',
                value: [
                  {
                    data: '0',
                    hoverValue: {},
                    date: '20-May-2024 to 31-May-2024',
                    value: 0,
                    sprojectName: 'PSknowHOW',
                  },
                  {
                    data: '11',
                    hoverValue: {},
                    date: '27-May-2024 to 07-Jun-2024',
                    value: 11,
                    sprojectName: 'PSknowHOW',
                  },
                  {
                    data: '11',
                    hoverValue: {},
                    date: '03-Jun-2024 to 14-Jun-2024',
                    value: 11,
                    sprojectName: 'PSknowHOW',
                  },
                  {
                    data: '0',
                    hoverValue: {},
                    date: '10-Jun-2024 to 21-Jun-2024',
                    value: 0,
                    sprojectName: 'PSknowHOW',
                  },
                  {
                    data: '0',
                    hoverValue: {},
                    date: '17-Jun-2024 to 28-Jun-2024',
                    value: 0,
                    sprojectName: 'PSknowHOW',
                  },
                ],
                maturityValue: '4.4',
              },
            ],
          },
          {
            filter: 'master -> PSknowHOW -> PSknowHOW',
            value: [
              {
                data: 'PSknowHOW',
                maturity: '4',
                value: [
                  {
                    data: '0',
                    hoverValue: {},
                    date: '20-May-2024 to 31-May-2024',
                    value: 0,
                    sprojectName: 'PSknowHOW',
                  },
                  {
                    data: '11',
                    hoverValue: {},
                    date: '27-May-2024 to 07-Jun-2024',
                    value: 11,
                    sprojectName: 'PSknowHOW',
                  },
                  {
                    data: '11',
                    hoverValue: {},
                    date: '03-Jun-2024 to 14-Jun-2024',
                    value: 11,
                    sprojectName: 'PSknowHOW',
                  },
                  {
                    data: '0',
                    hoverValue: {},
                    date: '10-Jun-2024 to 21-Jun-2024',
                    value: 0,
                    sprojectName: 'PSknowHOW',
                  },
                  {
                    data: '0',
                    hoverValue: {},
                    date: '17-Jun-2024 to 28-Jun-2024',
                    value: 0,
                    sprojectName: 'PSknowHOW',
                  },
                ],
                maturityValue: '4.4',
              },
            ],
          },
        ],
        maxValue: '10',
        kpiInfo: {
          definition:
            'Measures the efficiency of the code review process in a team',
          details: [
            {
              type: 'link',
              kpiLinkDetail: {
                text: 'Detailed Information at',
                link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70713477/Developer+Mean+time+to+Merge',
              },
            },
          ],
        },
      },
    ];
    component.kpiSelectedFilterObj = {
      kpi84: {
        filter1: ['Overall'],
      },
    };
    component.globalConfig = {
      username: 'SUPERADMIN',
      scrum: [
        {
          boardId: 6,
          boardName: 'Developer',
          boardSlug: 'developer',
          kpis: [
            {
              kpiId: 'kpi84',
              kpiName: 'Check-Ins & Merge Requests',
              isEnabled: true,
              order: 1,
              kpiDetail: {
                id: '65793ddc127be336160bc112',
                kpiId: 'kpi84',
                kpiName: 'Check-Ins & Merge Requests',
                isDeleted: 'False',
                defaultOrder: 1,
                kpiCategory: 'Developer',
                kpiUnit: 'MRs',
                chartType: 'grouped_column_plus_line',
                showTrend: true,
                isPositiveTrend: true,
                lineLegend: 'Merge Requests',
                barLegend: 'Commits',
                calculateMaturity: true,
                hideOverallFilter: true,
                kpiSource: 'BitBucket',
                maxValue: 10,
                thresholdValue: 55,
                kanban: false,
                groupId: 1,
                kpiInfo: {
                  definition:
                    'NUMBER OF CHECK-INS helps in measuring the transparency as well the how well the tasks have been broken down. NUMBER OF MERGE REQUESTS when looked at along with commits highlights the efficiency of the review process',
                  details: [
                    {
                      type: 'paragraph',
                      value:
                        'It is calculated as a Count. Higher the count better is the ‘Speed’',
                    },
                    {
                      type: 'paragraph',
                      value:
                        'A progress indicator shows trend of Number of Check-ins & Merge requests between last 2 days. An upward trend is considered positive',
                    },
                  ],
                  maturityLevels: [
                    {
                      level: 'M5',
                      bgColor: '#6cab61',
                      range: '> 16',
                    },
                    {
                      level: 'M4',
                      bgColor: '#AEDB76',
                      range: '8-16',
                    },
                    {
                      level: 'M3',
                      bgColor: '#eff173',
                      range: '4-8',
                    },
                    {
                      level: 'M2',
                      bgColor: '#ffc35b',
                      range: '2-4',
                    },
                    {
                      level: 'M1',
                      bgColor: '#F06667',
                      range: '0-2',
                    },
                  ],
                },
                kpiFilter: 'dropDown',
                aggregationCriteria: 'average',
                maturityRange: ['-2', '2-4', '4-8', '8-16', '16-'],
                isRepoToolKpi: true,
                trendCalculative: false,
                xaxisLabel: 'Days',
                yaxisLabel: 'Count',
                isAdditionalFilterSupport: false,
              },
              shown: true,
            },
          ],
          filters: {
            projectTypeSwitch: {
              enabled: true,
              visible: true,
            },
            primaryFilter: {
              type: 'singleSelect',
              defaultLevel: {
                labelName: 'Project',
              },
            },
            additionalFilters: [
              {
                type: 'singleSelect',
                defaultLevel: {
                  labelName: 'branch',
                },
              },
              {
                type: 'singleSelect',
                defaultLevel: {
                  labelName: 'developer',
                },
              },
            ],
          },
        },
      ],
      kanban: [
        {
          boardId: 12,
          boardName: 'Developer',
          boardSlug: 'developer',
          kpis: [
            {
              kpiId: 'kpi159',
              kpiName: 'Number of Check-ins',
              isEnabled: true,
              order: 1,
              kpiDetail: {
                id: '65793ddc127be336160bc114',
                kpiId: 'kpi159',
                kpiName: 'Number of Check-ins',
                isDeleted: 'false',
                defaultOrder: 1,
                kpiCategory: 'Developer',
                kpiUnit: 'check-ins',
                chartType: 'line',
                showTrend: true,
                isPositiveTrend: true,
                calculateMaturity: true,
                hideOverallFilter: true,
                kpiSource: 'BitBucket',
                combinedKpiSource: 'Bitbucket/AzureRepository/GitHub/GitLab',
                maxValue: 10,
                thresholdValue: 55,
                kanban: true,
                groupId: 1,
                kpiInfo: {
                  definition:
                    'NUMBER OF CHECK-INS helps in measuring the transparency as well the how well the tasks have been broken down.',
                  details: [
                    {
                      type: 'paragraph',
                      value:
                        'It is calculated as a Count. Higher the count better is the ‘Speed’',
                    },
                    {
                      type: 'paragraph',
                      value:
                        'A progress indicator shows trend of Number of Check-ins & Merge requests between last 2 days. An upward trend is considered positive.',
                    },
                    {
                      type: 'paragraph',
                      value:
                        'Maturity of the KPI is calculated based on the latest value',
                    },
                  ],
                  maturityLevels: [
                    {
                      level: 'M5',
                      bgColor: '#6cab61',
                      range: '>16',
                    },
                    {
                      level: 'M4',
                      bgColor: '#AEDB76',
                      range: '8-16',
                    },
                    {
                      level: 'M3',
                      bgColor: '#eff173',
                      range: '4-8',
                    },
                    {
                      level: 'M2',
                      bgColor: '#ffc35b',
                      range: '2-4',
                    },
                    {
                      level: 'M1',
                      bgColor: '#F06667',
                      range: '0-2',
                    },
                  ],
                },
                kpiFilter: 'dropDown',
                aggregationCriteria: 'sum',
                maturityRange: ['-2', '2-4', '4-8', '8-16', '16-'],
                isRepoToolKpi: true,
                trendCalculative: false,
                xaxisLabel: 'Weeks',
                yaxisLabel: 'Count',
                isAdditionalFilterSupport: false,
              },
              shown: true,
            },
          ],
          filters: {
            projectTypeSwitch: {
              enabled: true,
              visible: true,
            },
            primaryFilter: {
              type: 'singleSelect',
              defaultLevel: {
                labelName: 'project',
              },
            },
            additionalFilters: [
              {
                type: 'singleSelect',
                defaultLevel: {
                  labelName: 'branch',
                },
              },
              {
                type: 'singleSelect',
                defaultLevel: {
                  labelName: 'developer',
                },
              },
            ],
          },
        },
      ],
    };
    const kpiId = 'kpi84';
    const idx = 0;
    const aggregationType = 'average';
    const kpiFilterChange = false;

    const additionalFiltersArr = {
      filter: [
        {
          nodeId: 'Overall',
          nodeName: 'Overall',
          labelName: 'branch',
        },
        {
          nodeId: 'master -> PSknowHOW -> PSknowHOW',
          nodeName: 'master -> PSknowHOW -> PSknowHOW',
          labelName: 'branch',
        },
      ],
    };

    component.getChartData(kpiId, idx, aggregationType, kpiFilterChange);
    fixture.detectChanges();
    expect(component.additionalFiltersArr).toEqual(additionalFiltersArr);
  });

  it('should make post Sonar Kanban Kpi call', fakeAsync(() => {
    const postData = {
      kpiList: [
        {
          id: '633ed17f2c2d5abef2451fe3',
          kpiId: 'kpi17',
          kpiName: 'Unit Test Coverage',
        },
      ],
    };
    const successRes = [
      {
        kpiId: 'kpi17',
        kpiName: 'Unit Test Coverage',
        id: '633ed17f2c2d5abef2451fe3',
      },
    ];
    const mockSubscription = {
      unsubscribe: jasmine.createSpy('unsubscribe'),
    };
    component.sonarKpiRequest = mockSubscription;
    spyOn(httpService, 'postKpiKanban').and.returnValue(of(postData.kpiList));
    const spy = spyOn(component, 'afterSonarKpiResponseReceived');
    component.postSonarKanbanKpi(postData, 'sonar');
    tick();
    expect(spy).toHaveBeenCalledWith(successRes, postData);
  }));

  it('should get chartdataforRelease for kpi when trendValueList is an Array of two filters for card', () => {
    component.allKpiArray = [
      {
        kpiId: 'kpi124',
        trendValueList: [
          { filter1: 'hold', filter2: 'f2', value: [{ count: 1 }] },
          { filter1: 'f2', filter2: 'f2', value: [{ count: 2 }] },
          { filter1: 'in progress', filter2: 'f2', value: [{ count: 2 }] },
        ],
      },
    ];
    component.kpiSelectedFilterObj['kpi124'] = {
      filter1: ['hold', 'in progress'],
      filter2: ['f2'],
    };
    const spyObj = spyOn(component, 'applyAggregationLogic');
    spyOn(component, 'getKpiChartType').and.returnValue('abc');
    component.getChartDataforRelease('kpi124', 0);
    expect(spyObj).toHaveBeenCalled();
  });

  it('should get chartdataforRelease for kpi when trendValueList is an Array of two filters for card and kpiSelectedFilters have only 1 filter', () => {
    component.allKpiArray = [
      {
        kpiId: 'kpi124',
        trendValueList: [
          { filter1: 'hold', filter2: 'f2', value: [{ count: 1 }] },
          { filter1: 'f2', filter2: 'f2', value: [{ count: 2 }] },
          { filter1: 'in progress', filter2: 'f2', value: [{ count: 2 }] },
        ],
      },
    ];
    component.kpiSelectedFilterObj['kpi124'] = {
      filter1: ['hold', 'in progress'],
    };
    const spyObj = spyOn(component, 'applyAggregationLogic');
    spyOn(component, 'getKpiChartType').and.returnValue('abc');
    component.getChartDataforRelease('kpi124', 0);
    expect(spyObj).toHaveBeenCalled();
  });

  it('should get chartdataforRelease for kpi when trendValueList is an Array of two filters without aggregration', () => {
    component.allKpiArray = [
      {
        kpiId: 'kpi124',
        trendValueList: [
          { filter1: 'hold', filter2: 'f2', value: [{ count: 1 }] },
        ],
      },
    ];
    component.kpiSelectedFilterObj['kpi124'] = {
      filter1: ['hold', 'in progress'],
      filter2: ['f2'],
    };
    spyOn(component, 'getKpiChartType').and.returnValue('abc');
    component.getChartDataforRelease('kpi124', 0);
  });

  it('should getChartDataforRelease for kpi when trendValueList is an object', () => {
    component.allKpiArray = [
      {
        kpiId: 'kpi124',
        trendValueList: {
          value: [
            {
              filter1: 'Overall',
              filter2: 'Overall',
              data: [
                {
                  label: 'Scope added',
                  value: 1,
                  value1: 0,
                  labelInfo: '(Issue Count/Original Estimate)',
                  unit: '',
                  modalValues: [
                    {
                      'Issue Id': 'DTS-22685',
                      'Issue URL': 'http://testabc.com/jira/browse/DTS-22685',
                      'Issue Description':
                        'Iteration KPI | Popup window is not wide enough to read details  ',
                      'Issue Status': 'Open',
                      'Due Date': '-',
                    },
                  ],
                },
              ],
            },
          ],
        },
      },
    ];
    component.kpiSelectedFilterObj['kpi124'] = {
      filter1: ['Overall'],
      filter2: ['Overall'],
    };
    const res = {
      filter1: 'Overall',
      filter2: 'Overall',
      data: [
        {
          label: 'Issue without estimates',
          value: 21,
        },
      ],
    };
    const combo = [
      {
        filter1: 'Overall',
        filter2: 'Overall',
      },
    ];

    spyOn(helperService, 'createCombinations').and.returnValue(combo);
    component.getChartDataforRelease('kpi124', 0);
    expect(component.kpiChartData['kpi124'][0].data.length).toEqual(
      res.data.length,
    );
  });

  it('should getChartDataforRelease for kpi when trendValueList is an object with single filter', () => {
    component.allKpiArray = [
      {
        kpiId: 'kpi124',
        trendValueList: {
          value: [
            {
              filter1: 'Overall',
              data: [
                {
                  label: 'Scope added',
                  value: 1,
                  value1: 0,
                  labelInfo: '(Issue Count/Original Estimate)',
                  unit: '',
                  modalValues: [
                    {
                      'Issue Id': 'DTS-22685',
                      'Issue URL': 'http://testabc.com/jira/browse/DTS-22685',
                      'Issue Description':
                        'Iteration KPI | Popup window is not wide enough to read details  ',
                    },
                  ],
                },
              ],
            },
          ],
        },
      },
    ];
    component.kpiSelectedFilterObj['kpi124'] = {
      filter1: ['Overall'],
    };
    const res = {
      filter1: 'Overall',
      data: [
        {
          label: 'Issue without estimates',
          value: 21,
          value1: 51,
          unit: '',
          modalValues: [],
        },
      ],
    };
    const combo = [
      {
        filter1: 'Overall',
      },
    ];

    spyOn(helperService, 'createCombinations').and.returnValue(combo);
    component.getChartDataforRelease('kpi124', 0);
    expect(component.kpiChartData['kpi124'][0].data.length).toEqual(
      res.data.length,
    );
  });

  it('should getChartDataforRelease for kpi when trendValueList is an object and KPI selected filter is blank', () => {
    component.allKpiArray = [
      {
        kpiId: 'kpi124',
        trendValueList: {
          value: [
            {
              filter1: 'Overall',
              data: [
                {
                  label: 'Scope added',
                  value: 1,
                  value1: 0,
                  labelInfo: '(Issue Count/Original Estimate)',
                  unit: '',
                  modalValues: [
                    {
                      'Issue Id': 'DTS-22685',
                      'Issue URL': 'http://testabc.com/jira/browse/DTS-22685',
                      'Issue Description':
                        'Iteration KPI | Popup window is not wide enough to read details  ',
                    },
                  ],
                },
              ],
            },
          ],
        },
      },
    ];
    component.kpiSelectedFilterObj['kpi124'] = {};

    const combo = [
      {
        filter1: 'Overall',
      },
    ];

    spyOn(helperService, 'createCombinations').and.returnValue(combo);
    component.getChartDataforRelease('kpi124', 0);
    expect(component.kpiChartData['kpi124'][0].data.length).toBeGreaterThan(0);
  });

  it('should getChartDataforRelease for kpi when trendValueList is an object but there is no data', () => {
    component.allKpiArray = [
      {
        kpiId: 'kpi124',
        trendValueList: {
          value: [],
        },
      },
    ];
    component.kpiSelectedFilterObj['kpi124'] = {};
    const combo = [{ filter1: 'Overall' }];

    spyOn(helperService, 'createCombinations').and.returnValue(combo);
    component.getChartDataforRelease('kpi124', 0);
    expect(component.kpiChartData['kpi124'].length).toBeGreaterThan(0);
  });

  it('should apply aggregation logic correctly for a single data array', () => {
    const mockData = [
      {
        data: [
          {
            label: 'Label 1',
            value: 10,
            value1: 5,
            modalValues: ['value1', 'value2'],
          },
          {
            label: 'Label 2',
            value: 20,
            value1: 10,
            modalValues: ['value3', 'value4'],
          },
        ],
      },
    ];

    const expectedAggregatedData = [
      {
        data: [
          {
            label: 'Label 1',
            value: 10,
            value1: 5,
            modalValues: ['value1', 'value2'],
          },
          {
            label: 'Label 2',
            value: 20,
            value1: 10,
            modalValues: ['value3', 'value4'],
          },
        ],
      },
    ];

    const result = component.applyAggregationLogic(mockData);

    expect(result).toEqual(expectedAggregatedData);
  });

  it('should apply aggregation logic correctly for multiple data arrays', () => {
    const mockData = [
      {
        data: [
          {
            label: 'Label 1',
            value: 10,
            value1: 5,
            modalValues: ['value1', 'value2'],
          },
          {
            label: 'Label 2',
            value: 20,
            value1: 10,
            modalValues: ['value3', 'value4'],
          },
        ],
      },
      {
        data: [
          {
            label: 'Label 2',
            value: 30,
            value1: 15,
            modalValues: ['value5', 'value6'],
          },
          {
            label: 'Label 3',
            value: 40,
            value1: 20,
            modalValues: ['value7', 'value8'],
          },
        ],
      },
    ];

    const expectedAggregatedData = [
      {
        data: [
          {
            label: 'Label 1',
            value: 10,
            value1: 5,
            modalValues: ['value1', 'value2'],
          },
          {
            label: 'Label 2',
            value: 50,
            value1: 25,
            modalValues: ['value3', 'value4', 'value5', 'value6'],
          },
          {
            label: 'Label 3',
            value: 40,
            value1: 20,
            modalValues: ['value7', 'value8'],
          },
        ],
      },
    ];

    const result = component.applyAggregationLogic(mockData);

    expect(result).toEqual(expectedAggregatedData);
  });

  it('should get table data for kpi when trendValueList dont have filter', () => {
    component.allKpiArray = [
      {
        kpiId: 'kpi172',
        kpiName: 'Build Frequency',
        unit: '',
        chartType: '',
        id: '65eeb08194c86b415f978935',
        isDeleted: 'False',
        kpiUnit: '',
        kanban: false,
        kpiSource: 'Jenkins',
        thresholdValue: 8,
        maturityRange: ['1-2', '2-4', '5-8', '8-10', '10-'],
        groupId: 1,
        trendValueList: [],
        maxValue: '',
        kpiInfo: {
          definition:
            'Build frequency refers the number of successful builds done in a specific time frame.',
          details: [
            {
              type: 'link',
              kpiLinkDetail: {
                text: 'Detailed Information at',
                link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/92930049/Build+Frequency',
              },
            },
          ],
        },
      },
    ];
    component.kpiTableHeadingArr = fakeKpiTableHeadingArray;
    component.noOfDataPoints = 5;
    component.colorObj = {
      AddingIterationProject_64e739541426ba469c39c102: {
        nodeName: 'AddingIterationProject',
        color: '#079FFF',
      },
    };
    component.kpiTableDataObj['AddingIterationProject'] = [];
    const enabledKpi = {
      kpiDetail: {
        xaxisLabel: 'Sprints',
      },
      isEnabled: true,
      shown: true,
      order: '1',
      kpiId: 'kpi172',
    };

    component.kpiTrendsObj = {
      kpi172: [
        {
          hierarchyName: 'AddingIterationProject',
          value: '66.7 %',
          trend: '+ve',
          maturity: 'M4',
          maturityValue: '65.03',
          kpiUnit: '%',
        },
      ],
    };
    const returnedObj = {
      AddingIterationProject: [
        {
          '1': '122.6',
          '2': '126.9',
          '3': '176.5',
          '4': '83.3',
          '5': '57.7',
          kpiId: 'kpi172',
          kpiName: 'Defect Injection Rate',
          frequency: 'Sprints',
          show: true,
          hoverText: [
            '1 - DRP Sprint 71_AddingIterationProject',
            '2 - DRP Sprint 72_AddingIterationProject',
            '3 - DRP Sprint 73_AddingIterationProject',
            '4 - DRP Sprint 74_AddingIterationProject',
            '5 - DRP Sprint 75_AddingIterationProject',
          ],
          latest: '85 %',
          trend: '-ve',
          maturity: 'M3',
          order: '1',
        },
      ],
    };

    component.getTableData('kpi172', 0, enabledKpi);
    expect(component.kpiTableDataObj['AddingIterationProject']?.length).toEqual(
      returnedObj['AddingIterationProject']?.length,
    );
  });

  it('should group Jira kanban kpi', () => {
    const kpiListJiraKanban = [
      {
        id: '64c27a3b1d26a19187772b2e',
        kpiId: 'kpi54',
        kpiName: 'Ticket Open vs Closed rate by Priority',
      },
      {
        id: '64c27a3b1d26a19187772b2d',
        kpiId: 'kpi55',
        kpiName: 'Ticket Open vs Closed rate by type',
      },
    ];
    component.updatedConfigGlobalData = [
      {
        kpiId: 'kpi54',
        kpiName: 'Ticket Open vs Closed rate by Priority',
        isEnabled: true,
        order: 23,
        kpiDetail: {
          kanban: true,
          kpiSource: 'Jira',
          kpiCategory: 'Quality',
          groupId: 1,
        },
        shown: true,
      },
      {
        kpiId: 'kpi55',
        kpiName: 'Ticket Open vs Closed rate by type',
        isEnabled: true,
        order: 23,
        kpiDetail: {
          kanban: true,
          kpiSource: 'Jira',
          kpiCategory: 'Quality',
          groupId: 1,
        },
        shown: true,
      },
    ];
    component.configGlobalData = component.updatedConfigGlobalData;
    component.jiraKpiData = {};
    component.kpiJira = {};
    spyOn(helperService, 'groupKpiFromMaster').and.returnValue({
      kpiList: kpiListJiraKanban,
    });
    const spy = spyOn(component, 'postJiraKanbanKpi');
    component.groupJiraKanbanKpi(['kpi54', 'kpi55']);
    expect(spy).toHaveBeenCalled();
  });

  it('should group Sonar kanban kpi', () => {
    const kpiListSonarKanban = [
      {
        id: '64c27a3b1d26a19187772b2e',
        kpiId: 'kpi62',
        kpiName: 'Unit Test Coverage',
      },
    ];
    component.kpiListSonar = {};
    spyOn(helperService, 'groupKpiFromMaster').and.returnValue({
      kpiList: kpiListSonarKanban,
    });
    const spy = spyOn(component, 'postSonarKanbanKpi');
    component.groupSonarKanbanKpi(['kpi62']);
    expect(spy).toHaveBeenCalled();
  });

  it('should group Jenkins kanban kpi', () => {
    const kpiListJenkinsKanban = [
      {
        id: '64c27a3b1d26a19187772b3a',
        kpiId: 'kpi66',
        kpiName: 'Code Build Time',
      },
    ];
    spyOn(helperService, 'groupKpiFromMaster').and.returnValue({
      kpiList: kpiListJenkinsKanban,
    });
    const spy = spyOn(component, 'postJenkinsKanbanKpi');
    component.groupJenkinsKanbanKpi(['kpi66']);
    expect(spy).toHaveBeenCalled();
  });

  it('should group Zypher kanban kpi', () => {
    const kpiListZypherKanban = [
      {
        id: '64c27a3b1d26a19187772b33',
        kpiId: 'kpi63',
        kpiName: 'Regression Automation Coverage',
      },
    ];
    spyOn(helperService, 'groupKpiFromMaster').and.returnValue({
      kpiList: kpiListZypherKanban,
    });
    const spy = spyOn(component, 'postZypherKanbanKpi');
    component.groupZypherKanbanKpi(['kpi63']);
    expect(spy).toHaveBeenCalled();
  });

  it('should group Bitbucket kanban kpi', () => {
    const kpiListBitbucketKanban = [
      {
        id: '64c27a3b1d26a19187772b3b',
        kpiId: 'kpi65',
        kpiName: 'Number of Check-ins',
      },
    ];
    spyOn(helperService, 'groupKpiFromMaster').and.returnValue({
      kpiList: kpiListBitbucketKanban,
    });
    const spy = spyOn(component, 'postBitBucketKanbanKpi');
    component.groupBitBucketKanbanKpi(['kpi65']);
    expect(spy).toHaveBeenCalled();
  });

  it('should post jira kpis when Release board is selected', fakeAsync(() => {
    component.tooltip = {
      sprintCountForKpiCalculation: 2,
    };
    component.filterApplyData = {
      label: 'project',
      selectedMap: {
        sprint: [],
      },
    };
    component.selectedTab = 'release';
    const postData = {
      kpiList: [
        {
          id: '64c27a3b1d26a19187772b52',
          kpiId: 'kpi141',
          kpiName: 'Defect Count by Status',
        },
        {
          id: '64c27a3b1d26a19187772b53',
          kpiId: 'kpi142',
          kpiName: 'Defect Count by RCA',
        },
        {
          id: '64c27a3b1d26a19187772b54',
          kpiId: 'kpi143',
          kpiName: 'Defect Count by Assignee',
        },
      ],
    };

    const kpiWiseData = {
      kpi141: {
        id: '64c27a3b1d26a19187772b52',
        kpiName: 'Defect Count by Status',
      },
      kpi142: {
        id: '64c27a3b1d26a19187772b53',
        kpiName: 'Defect Count by RCA',
      },
      kpi143: {
        id: '64c27a3b1d26a19187772b54',
        kpiName: 'Defect Count by Assignee',
      },
    };
    component.jiraKpiRequest = '';
    spyOn(helperService, 'createKpiWiseId').and.returnValue(kpiWiseData);
    spyOn(component, 'removeLoaderFromKPIs');
    spyOn(httpService, 'postKpiNonTrend').and.returnValue(of(postData.kpiList));
    const spy = spyOn(component, 'createAllKpiArray');
    component.postJiraKpi(postData, 'Jira');
    component.jiraKpiData = {};
    tick();
    expect(spy).toHaveBeenCalled();
  }));
  it('should handle successful post request and update jiraKpiData', () => {
    const mockPostData = {
      kpiList: [
        {
          id: '65793ddb127be336160bc0fe',
          kpiId: 'kpi141',
          kpiName: 'Defect Count by Status',
          isDeleted: 'False',
          defaultOrder: 1,
          kpiCategory: 'Release',
          kpiSubCategory: 'Quality',
          kpiUnit: 'Count',
          chartType: '',
          showTrend: false,
          isPositiveTrend: true,
          boxType: 'chart',
          calculateMaturity: false,
          hideOverallFilter: false,
          kpiSource: 'Jira',
          combinedKpiSource: 'Jira/Azure',
          maxValue: '',
          kanban: false,
          groupId: 9,
          kpiInfo: {
            definition:
              'It shows the breakup of all defects tagged to a release based on Status. The breakup is shown in terms of count & percentage.',
            details: [
              {
                type: 'link',
                kpiLinkDetail: {
                  text: 'Detailed Information at',
                  link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/79986689/Release+Defect+count+by+Status',
                },
              },
            ],
          },
          kpiFilter: '',
          trendCalculative: false,
          xaxisLabel: '',
          yaxisLabel: '',
          isAdditionalFilterSupport: false,
        },
      ],
      ids: ['148419_API POD 2 - Account Management_6524a7de7c8bb73cd0c3fe6d'],
      level: 6,
      selectedMap: {
        bu: [],
        ver: [],
        acc: [],
        port: [],
        project: [],
        release: [
          '148419_API POD 2 - Account Management_6524a7de7c8bb73cd0c3fe6d',
        ],
        sprint: [],
        sqd: [],
      },
      sprintIncluded: ['CLOSED'],
      label: 'release',
    };
    const mockGetData = [
      {
        kpiId: 'kpi141',
        kpiName: 'Defect Count by Status',
        unit: 'Count',
        chartType: '',
        id: '65793ddb127be336160bc0fe',
        isDeleted: 'False',
        kpiCategory: 'Release',
        kpiUnit: 'Count',
        kanban: false,
        kpiSource: 'Jira',
        groupId: 9,
        sprint: 'Phase 1_API POD 2 - Account Management',
        modalHeads: [
          'Issue ID',
          'Issue Description',
          'Sprint Name',
          'Issue Type',
          'Issue Status',
          'Root Cause',
          'Priority',
          'Assignee',
        ],
        trendValueList: [
          {
            filter1: 'Overall',
            value: [
              {
                data: 'API POD 2 - Account Management',
                value: [
                  {
                    data: '1',
                    kpiGroup: 'Overall',
                    value: {
                      Closed: 1,
                    },
                    sprojectName: 'API POD 2 - Account Management',
                  },
                ],
              },
            ],
          },
        ],
        maxValue: '',
        kpiInfo: {
          definition:
            'It shows the breakup of all defects tagged to a release based on Status. The breakup is shown in terms of count & percentage.',
          details: [
            {
              type: 'link',
              kpiLinkDetail: {
                text: 'Detailed Information at',
                link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/79986689/Release+Defect+count+by+Status',
              },
            },
          ],
        },
      },
    ];

    spyOn(httpService, 'postKpiNonTrend').and.returnValue(of(mockGetData));
    spyOn(helperService, 'createKpiWiseId').and.returnValue(mockGetData);

    component.postJiraKPIForRelease(mockPostData, 'jira');

    expect(httpService.postKpiNonTrend).toHaveBeenCalledWith(
      mockPostData,
      'jira',
    );
    expect(helperService.createKpiWiseId).toHaveBeenCalledWith(mockGetData);
    // expect(component.jiraKpiData).toEqual(mockGetData);
  });

  it('should handle successful post request and update jiraKpiData when api resturns no data', () => {
    const mockPostData = {
      kpiList: [
        {
          id: '65793ddb127be336160bc0fe',
          kpiId: 'kpi141',
          kpiName: 'Defect Count by Status',
          isDeleted: 'False',
          defaultOrder: 1,
          kpiCategory: 'Release',
          kpiSubCategory: 'Quality',
          kpiUnit: 'Count',
          chartType: '',
          showTrend: false,
          isPositiveTrend: true,
          boxType: 'chart',
          calculateMaturity: false,
          hideOverallFilter: false,
          kpiSource: 'Jira',
          combinedKpiSource: 'Jira/Azure',
          maxValue: '',
          kanban: false,
          groupId: 9,
          kpiInfo: {
            definition:
              'It shows the breakup of all defects tagged to a release based on Status. The breakup is shown in terms of count & percentage.',
            details: [
              {
                type: 'link',
                kpiLinkDetail: {
                  text: 'Detailed Information at',
                  link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/79986689/Release+Defect+count+by+Status',
                },
              },
            ],
          },
          kpiFilter: '',
          trendCalculative: false,
          xaxisLabel: '',
          yaxisLabel: '',
          isAdditionalFilterSupport: false,
        },
      ],
      ids: ['148419_API POD 2 - Account Management_6524a7de7c8bb73cd0c3fe6d'],
      level: 6,
      selectedMap: {
        bu: [],
        ver: [],
        acc: [],
        port: [],
        project: [],
        release: [
          '148419_API POD 2 - Account Management_6524a7de7c8bb73cd0c3fe6d',
        ],
        sprint: [],
        sqd: [],
      },
      sprintIncluded: ['CLOSED'],
      label: 'release',
    };
    const mockGetData = {
      error: 'API call failed',
    };

    spyOn(httpService, 'postKpiNonTrend').and.returnValue(of(mockGetData));
    spyOn(helperService, 'createKpiWiseId').and.returnValue(mockGetData);

    component.postJiraKPIForRelease(mockPostData, 'jira');

    expect(httpService.postKpiNonTrend).toHaveBeenCalledWith(
      mockPostData,
      'jira',
    );
    expect(helperService.createKpiWiseId).not.toHaveBeenCalled();
    // expect(component.jiraKpiData).toEqual(mockGetData);
  });

  it('should handle successful post request and update bitbucketKpiData when api resturns no data', () => {
    const mockPostData = {
      kpiList: [
        {
          id: '65793ddb127be336160bc0fe',
          kpiId: 'kpi141',
          kpiName: 'Defect Count by Status',
          isDeleted: 'False',
          defaultOrder: 1,
          kpiCategory: 'Release',
          kpiSubCategory: 'Quality',
          kpiUnit: 'Count',
          chartType: '',
          showTrend: false,
          isPositiveTrend: true,
          boxType: 'chart',
          calculateMaturity: false,
          hideOverallFilter: false,
          kpiSource: 'bitbucket',
          combinedKpiSource: 'bitbucket',
          maxValue: '',
          kanban: false,
          groupId: 9,
          kpiInfo: {
            definition:
              'It shows the breakup of all defects tagged to a release based on Status. The breakup is shown in terms of count & percentage.',
            details: [
              {
                type: 'link',
                kpiLinkDetail: {
                  text: 'Detailed Information at',
                  link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/79986689/Release+Defect+count+by+Status',
                },
              },
            ],
          },
          kpiFilter: '',
          trendCalculative: false,
          xaxisLabel: '',
          yaxisLabel: '',
          isAdditionalFilterSupport: false,
        },
      ],
      ids: ['148419_API POD 2 - Account Management_6524a7de7c8bb73cd0c3fe6d'],
      level: 6,
      selectedMap: {
        bu: [],
        ver: [],
        acc: [],
        port: [],
        project: [],
        release: [
          '148419_API POD 2 - Account Management_6524a7de7c8bb73cd0c3fe6d',
        ],
        sprint: [],
        sqd: [],
      },
      sprintIncluded: ['CLOSED'],
      label: 'release',
    };
    const mockGetData = {
      error: 'API call failed',
    };

    spyOn(httpService, 'postKpi').and.returnValue(of(mockGetData));
    spyOn(helperService, 'createKpiWiseId').and.returnValue(mockGetData);

    component.postBitBucketKpi(mockPostData, 'bitbucket');

    expect(httpService.postKpi).toHaveBeenCalledWith(mockPostData, 'bitbucket');
    expect(helperService.createKpiWiseId).not.toHaveBeenCalled();
    // expect(component.bitBucketKpiData).toEqual(mockGetData);
  });

  it('should handle successful post request and update jenkinsKpiData when api resturns no data', () => {
    const mockPostData = {
      kpiList: [
        {
          id: '65793ddb127be336160bc0fe',
          kpiId: 'kpi141',
          kpiName: 'Defect Count by Status',
          isDeleted: 'False',
          defaultOrder: 1,
          kpiCategory: 'Release',
          kpiSubCategory: 'Quality',
          kpiUnit: 'Count',
          chartType: '',
          showTrend: false,
          isPositiveTrend: true,
          boxType: 'chart',
          calculateMaturity: false,
          hideOverallFilter: false,
          kpiSource: 'jenkins',
          combinedKpiSource: 'jenkins',
          maxValue: '',
          kanban: false,
          groupId: 9,
          kpiInfo: {
            definition:
              'It shows the breakup of all defects tagged to a release based on Status. The breakup is shown in terms of count & percentage.',
            details: [
              {
                type: 'link',
                kpiLinkDetail: {
                  text: 'Detailed Information at',
                  link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/79986689/Release+Defect+count+by+Status',
                },
              },
            ],
          },
          kpiFilter: '',
          trendCalculative: false,
          xaxisLabel: '',
          yaxisLabel: '',
          isAdditionalFilterSupport: false,
        },
      ],
      ids: ['148419_API POD 2 - Account Management_6524a7de7c8bb73cd0c3fe6d'],
      level: 6,
      selectedMap: {
        bu: [],
        ver: [],
        acc: [],
        port: [],
        project: [],
        release: [
          '148419_API POD 2 - Account Management_6524a7de7c8bb73cd0c3fe6d',
        ],
        sprint: [],
        sqd: [],
      },
      sprintIncluded: ['CLOSED'],
      label: 'release',
    };
    const mockGetData = null;

    spyOn(httpService, 'postKpi').and.returnValue(of(mockGetData));
    spyOn(helperService, 'createKpiWiseId').and.returnValue(mockGetData);

    component.postJenkinsKpi(mockPostData, 'jenkins');

    expect(httpService.postKpi).toHaveBeenCalledWith(mockPostData, 'jenkins');
    expect(helperService.createKpiWiseId).not.toHaveBeenCalled();
    // expect(component.jenkinsKpiData).toEqual(mockGetData);
  });

  it('should handle error response and update jiraKpiData', () => {
    const mockPostData = {
      kpiList: [
        {
          id: '65793ddb127be336160bc0fe',
          kpiId: 'kpi141',
          kpiName: 'Defect Count by Status',
          isDeleted: 'False',
          defaultOrder: 1,
          kpiCategory: 'Release',
          kpiSubCategory: 'Quality',
          kpiUnit: 'Count',
          chartType: '',
          showTrend: false,
          isPositiveTrend: true,
          boxType: 'chart',
          calculateMaturity: false,
          hideOverallFilter: false,
          kpiSource: 'Jira',
          combinedKpiSource: 'Jira/Azure',
          maxValue: '',
          kanban: false,
          groupId: 9,
          kpiInfo: {
            definition:
              'It shows the breakup of all defects tagged to a release based on Status. The breakup is shown in terms of count & percentage.',
            details: [
              {
                type: 'link',
                kpiLinkDetail: {
                  text: 'Detailed Information at',
                  link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/79986689/Release+Defect+count+by+Status',
                },
              },
            ],
          },
          kpiFilter: '',
          trendCalculative: false,
          xaxisLabel: '',
          yaxisLabel: '',
          isAdditionalFilterSupport: false,
        },
        {
          id: '65793ddb127be336160bc10b',
          kpiId: 'kpi150',
          kpiName: 'Release Burnup',
          isDeleted: 'False',
          defaultOrder: 1,
          kpiCategory: 'Release',
          kpiSubCategory: 'Speed',
          kpiUnit: 'Count',
          chartType: '',
          showTrend: false,
          isPositiveTrend: true,
          boxType: 'chart',
          calculateMaturity: false,
          hideOverallFilter: false,
          kpiSource: 'Jira',
          combinedKpiSource: 'Jira/Azure',
          maxValue: '',
          kanban: false,
          groupId: 9,
          kpiInfo: {
            definition:
              'It shows the cumulative daily actual progress of the release against the overall scope. It also shows additionally the scope added or removed during the release w.r.t Dev/Qa completion date and Dev/Qa completion status for the Release tagged issues',
            details: [
              {
                type: 'link',
                kpiLinkDetail: {
                  text: 'Detailed Information at',
                  link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70484023/Release+Release+Burnup',
                },
              },
              {
                type: 'link',
                kpiLinkDetail: {
                  text: 'Detailed Information at',
                  link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70484023/Release+Release+Burnup',
                },
              },
            ],
          },
          kpiFilter: 'radioButton',
          kpiWidth: 100,
          trendCalculative: false,
          xaxisLabel: '',
          yaxisLabel: 'Count',
          isAdditionalFilterSupport: false,
        },
        {
          id: '6656a39c143cbb0ff82981b8',
          kpiId: 'kpi178',
          kpiName: 'Defect Count By',
          isDeleted: 'False',
          defaultOrder: 1,
          kpiCategory: 'Release',
          kpiSubCategory: 'Quality',
          chartType: '',
          showTrend: false,
          boxType: 'chart',
          calculateMaturity: false,
          hideOverallFilter: false,
          kpiSource: 'Jira',
          combinedKpiSource: 'Jira/Azure',
          kanban: false,
          groupId: 9,
          kpiInfo: {
            definition:
              'It shows the breakup of all defects tagged to a release grouped by Status, Priority, or RCA.',
            details: [
              {
                type: 'link',
                kpiLinkDetail: {
                  text: 'Detailed Information at',
                  link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/144146433/Release+Defect+count+by',
                },
              },
            ],
          },
          kpiFilter: '',
          trendCalculative: false,
          isAdditionalFilterSupport: false,
        },
        {
          id: '66616fc0ff078f6bc1ecf38d',
          kpiId: 'kpi179',
          kpiName: 'Release Plan',
          isDeleted: 'False',
          defaultOrder: 1,
          kpiCategory: 'Release',
          kpiSubCategory: 'Speed',
          kpiUnit: 'Count',
          chartType: '',
          showTrend: false,
          isPositiveTrend: true,
          boxType: 'chart',
          calculateMaturity: false,
          hideOverallFilter: false,
          kpiSource: 'Jira',
          combinedKpiSource: 'Jira/Azure',
          maxValue: '',
          kanban: false,
          groupId: 9,
          kpiInfo: {
            definition:
              'Displays the cumulative daily planned dues of the release based on the due dates of work items within the release scope.\n\nAdditionally, it provides an overview of the entire release scope.',
            details: [
              {
                type: 'link',
                kpiLinkDetail: {
                  text: 'Detailed Information at',
                  link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/147652609/Release+Release+Plan',
                },
              },
            ],
          },
          kpiFilter: '',
          kpiWidth: 100,
          trendCalculative: false,
          xaxisLabel: '',
          yaxisLabel: 'Count',
          isAdditionalFilterSupport: false,
        },
        {
          id: '65793ddb127be336160bc0ff',
          kpiId: 'kpi142',
          kpiName: 'Defect Count by RCA',
          isDeleted: 'False',
          defaultOrder: 2,
          kpiCategory: 'Release',
          kpiSubCategory: 'Quality',
          kpiUnit: 'Count',
          chartType: '',
          showTrend: false,
          isPositiveTrend: true,
          boxType: 'chart',
          calculateMaturity: false,
          hideOverallFilter: false,
          kpiSource: 'Jira',
          combinedKpiSource: 'Jira/Azure',
          maxValue: '',
          kanban: false,
          groupId: 9,
          kpiInfo: {
            definition:
              'It shows the breakup of all defects tagged to a release based on RCA. The breakup is shown in terms of count at different testing phases.',
            details: [
              {
                type: 'link',
                kpiLinkDetail: {
                  text: 'Detailed Information at',
                  link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/79953937/Release+Defect+count+by+RCA',
                },
              },
            ],
          },
          kpiFilter: 'radioButton',
          trendCalculative: false,
          xaxisLabel: 'Test Phase',
          yaxisLabel: 'Count',
          isAdditionalFilterSupport: false,
        },
        {
          id: '65793ddb127be336160bc100',
          kpiId: 'kpi143',
          kpiName: 'Defect Count by Assignee',
          isDeleted: 'False',
          defaultOrder: 3,
          kpiCategory: 'Release',
          kpiSubCategory: 'Quality',
          kpiUnit: 'Count',
          chartType: '',
          showTrend: false,
          isPositiveTrend: true,
          boxType: 'chart',
          calculateMaturity: false,
          hideOverallFilter: false,
          kpiSource: 'Jira',
          combinedKpiSource: 'Jira/Azure',
          maxValue: '',
          kanban: false,
          groupId: 9,
          kpiInfo: {
            definition:
              'It shows the breakup of all defects tagged to a release based on Assignee. The breakup is shown in terms of count & percentage.',
            details: [
              {
                type: 'link',
                kpiLinkDetail: {
                  text: 'Detailed Information at',
                  link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/79691782/Release+Defect+count+by+Assignee',
                },
              },
            ],
          },
          kpiFilter: 'radioButton',
          trendCalculative: false,
          xaxisLabel: '',
          yaxisLabel: '',
          isAdditionalFilterSupport: false,
        },
        {
          id: '65793ddb127be336160bc101',
          kpiId: 'kpi144',
          kpiName: 'Defect Count by Priority',
          isDeleted: 'False',
          defaultOrder: 4,
          kpiCategory: 'Release',
          kpiSubCategory: 'Quality',
          kpiUnit: 'Count',
          chartType: '',
          showTrend: false,
          isPositiveTrend: true,
          boxType: 'chart',
          calculateMaturity: false,
          hideOverallFilter: false,
          kpiSource: 'Jira',
          combinedKpiSource: 'Jira/Azure',
          maxValue: '',
          kanban: false,
          groupId: 9,
          kpiInfo: {
            definition:
              'It shows the breakup of all defects tagged to a release based on Priority. The breakup is shown in terms of count & percentage.',
            details: [
              {
                type: 'link',
                kpiLinkDetail: {
                  text: 'Detailed Information at',
                  link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/79953921/Release+Defect+count+by+Priority',
                },
              },
            ],
          },
          kpiFilter: 'radioButton',
          trendCalculative: false,
          xaxisLabel: '',
          yaxisLabel: '',
          isAdditionalFilterSupport: false,
        },
        {
          id: '65793ddb127be336160bc104',
          kpiId: 'kpi147',
          kpiName: 'Release Progress',
          isDeleted: 'False',
          defaultOrder: 5,
          kpiCategory: 'Release',
          kpiSubCategory: 'Speed',
          kpiUnit: 'Count',
          chartType: '',
          showTrend: false,
          isPositiveTrend: true,
          boxType: 'chart',
          calculateMaturity: false,
          hideOverallFilter: false,
          kpiSource: 'Jira',
          combinedKpiSource: 'Jira/Azure',
          maxValue: '',
          kanban: false,
          groupId: 9,
          kpiInfo: {
            definition:
              'It shows the breakup by status of issues tagged to a release. The breakup is based on both issue count and story points',
            details: [
              {
                type: 'link',
                kpiLinkDetail: {
                  text: 'Detailed Information at',
                  link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/79757314/Release+Release+Progress',
                },
              },
            ],
          },
          kpiFilter: 'dropDown',
          kpiWidth: 100,
          trendCalculative: false,
          xaxisLabel: '',
          yaxisLabel: '',
          isAdditionalFilterSupport: false,
        },
        {
          id: '65793ddc127be336160bc119',
          kpiId: 'kpi165',
          kpiName: 'Epic Progress',
          isDeleted: 'False',
          defaultOrder: 5,
          kpiCategory: 'Release',
          kpiSubCategory: 'Value',
          kpiUnit: 'Count',
          chartType: '',
          showTrend: false,
          isPositiveTrend: true,
          boxType: 'chart',
          calculateMaturity: false,
          hideOverallFilter: false,
          kpiSource: 'Jira',
          combinedKpiSource: 'Jira/Azure',
          maxValue: '',
          kanban: false,
          groupId: 9,
          kpiInfo: {
            definition:
              'It depicts the progress of each epic in a release in terms of total count and %age completion.',
            details: [
              {
                type: 'link',
                kpiLinkDetail: {
                  text: 'Detailed Information at',
                  link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/79986705/Release+Epic+Progress',
                },
              },
            ],
          },
          kpiFilter: 'multiSelectDropDown',
          kpiWidth: 100,
          trendCalculative: false,
          xaxisLabel: '',
          yaxisLabel: '',
          isAdditionalFilterSupport: false,
        },
        {
          id: '65793ddc127be336160bc118',
          kpiId: 'kpi163',
          kpiName: 'Defect by Testing Phase',
          isDeleted: 'False',
          defaultOrder: 7,
          kpiCategory: 'Release',
          kpiSubCategory: 'Quality',
          kpiUnit: 'Count',
          chartType: '',
          showTrend: false,
          isPositiveTrend: true,
          boxType: 'chart',
          calculateMaturity: false,
          hideOverallFilter: false,
          kpiSource: 'Jira',
          combinedKpiSource: 'Jira/Azure',
          maxValue: '',
          kanban: false,
          groupId: 9,
          kpiInfo: {
            definition:
              ' It gives a breakup of escaped defects by testing phase',
            details: [
              {
                type: 'link',
                kpiLinkDetail: {
                  text: 'Detailed Information at',
                  link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/98140473/Release+Defect+count+by+Testing+phase',
                },
              },
            ],
          },
          kpiFilter: 'radioButton',
          maturityRange: ['-40', '40-60', '60-75', '75-90', '90-'],
          trendCalculative: false,
          xaxisLabel: '',
          yaxisLabel: '',
          isAdditionalFilterSupport: false,
        },
      ],
      ids: ['148419_API POD 2 - Account Management_6524a7de7c8bb73cd0c3fe6d'],
      level: 6,
      selectedMap: {
        bu: [],
        ver: [],
        acc: [],
        port: [],
        project: [],
        release: [
          '148419_API POD 2 - Account Management_6524a7de7c8bb73cd0c3fe6d',
        ],
        sprint: [],
        sqd: [],
      },
      sprintIncluded: ['CLOSED'],
      label: 'release',
    };
    const mockErrorData = {};

    spyOn(httpService, 'postKpiNonTrend').and.returnValue(of(mockErrorData));

    component.postJiraKPIForRelease(mockPostData, 'jira');

    expect(httpService.postKpiNonTrend).toHaveBeenCalledWith(
      mockPostData,
      'jira',
    );
    expect(component.jiraKpiData).toEqual(mockErrorData);
  });

  it('should handle selected option on release when event is an object', () => {
    const mockEvent = {
      filter1: ['value1', 'value2'],
      filter2: ['value3'],
    };
    const mockKpi = { kpiId: 'kpi1' };

    component.handleSelectedOptionOnRelease(mockEvent, mockKpi);

    expect(component.kpiSelectedFilterObj[mockKpi.kpiId]).toEqual(mockEvent);
  });

  it('should handle selected option on release when event is not an object', () => {
    const mockEvent = 'value1';
    const mockKpi = { kpiId: 'kpi1' };

    component.handleSelectedOptionOnRelease(mockEvent, mockKpi);

    expect(component.kpiSelectedFilterObj[mockKpi.kpiId]).toEqual({
      filter1: [mockEvent],
    });
  });

  it('should delete empty values from event object', () => {
    const mockEvent = {
      filter1: [],
      filter2: ['value1'],
    };
    const mockKpi = { kpiId: 'kpi1' };

    component.handleSelectedOptionOnRelease(mockEvent, mockKpi);

    expect(component.kpiSelectedFilterObj[mockKpi.kpiId]).toEqual({
      filter2: ['value1'],
    });
  });

  it('should get table data for kpi when trendValueList dont have filter when kpi name is availiable', () => {
    component.allKpiArray = [
      {
        kpiName: 'abc',
      },
    ];
    component.kpiTableHeadingArr = fakeKpiTableHeadingArray;
    component.noOfDataPoints = 5;
    component.colorObj = {
      AddingIterationProject_64e739541426ba469c39c102: {
        nodeName: 'AddingIterationProject',
        color: '#079FFF',
      },
    };
    component.kpiTableDataObj['AddingIterationProject'] = [];
    const enabledKpi = {
      kpiDetail: {
        xaxisLabel: 'Sprints',
      },
      isEnabled: true,
      shown: true,
      order: '1',
    };
    const returnedObj = {
      AddingIterationProject: [
        {
          '1': '122.6',
          '2': '126.9',
          '3': '176.5',
          '4': '83.3',
          '5': '57.7',
          kpiId: 'kpi14',
          kpiName: 'Defect Injection Rate',
          frequency: 'Sprints',
          show: true,
          hoverText: [
            '1 - DRP Sprint 71_AddingIterationProject',
            '2 - DRP Sprint 72_AddingIterationProject',
            '3 - DRP Sprint 73_AddingIterationProject',
            '4 - DRP Sprint 74_AddingIterationProject',
            '5 - DRP Sprint 75_AddingIterationProject',
          ],
          latest: '85 %',
          trend: '-ve',
          maturity: 'M3',
          order: '1',
        },
      ],
    };

    component.getTableData('kpi14', 0, enabledKpi);
    expect(component.kpiTableDataObj['AddingIterationProject']?.length).toEqual(
      returnedObj['AddingIterationProject']?.length,
    );
  });

  it('should return the correct chart type when kpiId exists in updatedConfigGlobalData', () => {
    const mockKpiId = 'kpi1';
    component.updatedConfigGlobalData = [
      {
        kpiId: 'kpi1',
        kpiDetail: {
          kpiId: 'kpi1',
          chartType: 'line',
        },
      },
    ];
    const result = component.getKpiChartType(mockKpiId);

    expect(result).toEqual('line');
  });

  it('should return undefined when kpiId does not exist in updatedConfigGlobalData', () => {
    const mockKpiId = 'kpi4';
    component.updatedConfigGlobalData = [];
    const result = component.getKpiChartType(mockKpiId);

    expect(result).toBeUndefined();
  });

  it('should update kpiTableDataObj with correct data when idx is greater than or equal to 0 and filter is overall', () => {
    component.allKpiArray = [
      {
        kpiId: 'kpi1',
        kpiName: 'KPI 1',
        trendValueList: [{ data: 'value1' }, { data: 'value2' }],
      },
      {
        kpiId: 'kpi2',
        kpiName: 'KPI 2',
        trendValueList: [{ data: 'value3' }, { data: 'value4' }],
      },
    ];
    component.colorObj = {
      branch1: { nodeName: 'branch1' },
      branch2: { nodeName: 'branch2' },
    };
    component.kpiTrendsObj = {
      kpi1: [
        {
          hierarchyName: 'branch1',
          value: 'value1',
          trend: 'trend1',
          maturity: 'maturity1',
          kpiUnit: 'unit1',
        },
      ],
      kpi2: [
        {
          hierarchyName: 'branch2',
          value: 'value2',
          trend: 'trend2',
          maturity: 'maturity2',
          kpiUnit: 'unit2',
        },
      ],
    };
    component.kpiTableDataObj = {
      branch1: [
        {
          kpiId: 'kpi1',
          kpiName: 'KPI 1',
          frequency: 'Monthly',
          show: true,
          hoverText: [],
          order: 1,
        },
      ],
      branch2: [
        {
          kpiId: 'kpi2',
          kpiName: 'KPI 2',
          frequency: 'Weekly',
          show: true,
          hoverText: [],
          order: 2,
        },
      ],
    };
    component.maturityTableKpiList = ['kpi1', 'kpi2'];
    component.noOfDataPoints = 2;

    const mockKpiId = 'kpi1';
    const mockIdx = 0;
    const mockEnabledKpi = {
      kpiDetail: { xaxisLabel: 'Monthly' },
      isEnabled: true,
      shown: true,
      order: 1,
    };
    component.colorObj = { branch1: { nodeName: 'branch1' } };
    spyOn(component, 'sortingRowsInTable');

    component.getTableData(mockKpiId, mockIdx, mockEnabledKpi);
    expect(component.sortingRowsInTable).toHaveBeenCalled();
  });

  it('should update kpiTableDataObj with correct data when idx is greater than or equal to 0 and filter is not overall', () => {
    component.allKpiArray = [
      {
        kpiId: 'kpi1',
        kpiName: 'KPI 1',
        trendValueList: [{ data: 'value1' }, { data: 'value2' }],
      },
      {
        kpiId: 'kpi2',
        kpiName: 'KPI 2',
        trendValueList: [{ data: 'value3' }, { data: 'value4' }],
      },
    ];
    component.colorObj = {
      color1: { nodeName: 'branch1' },
      color2: { nodeName: 'branch2' },
    };
    component.kpiTrendsObj = {
      kpi1: [
        {
          hierarchyName: 'branch1',
          value: 'value1',
          trend: 'trend1',
          maturity: 'maturity1',
          kpiUnit: 'unit1',
        },
      ],
      kpi2: [
        {
          hierarchyName: 'branch2',
          value: 'value2',
          trend: 'trend2',
          maturity: 'maturity2',
          kpiUnit: 'unit2',
        },
      ],
    };
    component.kpiTableDataObj = {
      branch1: [
        {
          kpiId: 'kpi1',
          kpiName: 'KPI 1',
          frequency: 'Monthly',
          show: true,
          hoverText: [],
          order: 1,
        },
      ],
      branch2: [
        {
          kpiId: 'kpi2',
          kpiName: 'KPI 2',
          frequency: 'Weekly',
          show: true,
          hoverText: [],
          order: 2,
        },
      ],
    };

    const mockKpiId = 'kpi17';
    const mockIdx = 0;
    const mockEnabledKpi = {
      kpiDetail: { xaxisLabel: 'Monthly' },
      isEnabled: true,
      shown: true,
      order: 1,
    };
    component.colorObj = { branch1: { nodeName: 'branch1' } };
    spyOn(component, 'sortingRowsInTable');

    component.getTableData(mockKpiId, mockIdx, mockEnabledKpi);
    expect(component.sortingRowsInTable).toHaveBeenCalled();
  });

  it('should update kpiTableDataObj with correct data when idx is less than 0 and filter is overall', () => {
    component.allKpiArray = [
      {
        kpiId: 'kpi1',
        kpiName: 'KPI 1',
        trendValueList: [{ data: 'value1' }, { data: 'value2' }],
      },
      {
        kpiId: 'kpi2',
        kpiName: 'KPI 2',
        trendValueList: [{ data: 'value3' }, { data: 'value4' }],
      },
    ];
    component.colorObj = {
      color1: { nodeName: 'branch1' },
      color2: { nodeName: 'branch2' },
    };
    component.kpiTrendsObj = {
      kpi1: [
        {
          hierarchyName: 'branch1',
          value: 'value1',
          trend: 'trend1',
          maturity: 'maturity1',
          kpiUnit: 'unit1',
        },
      ],
      kpi2: [
        {
          hierarchyName: 'branch2',
          value: 'value2',
          trend: 'trend2',
          maturity: 'maturity2',
          kpiUnit: 'unit2',
        },
      ],
    };
    component.kpiTableDataObj = {
      branch1: [
        {
          kpiId: 'kpi1',
          kpiName: 'KPI 1',
          frequency: 'Monthly',
          show: true,
          hoverText: [],
          order: 1,
        },
      ],
      branch2: [
        {
          kpiId: 'kpi2',
          kpiName: 'KPI 2',
          frequency: 'Weekly',
          show: true,
          hoverText: [],
          order: 2,
        },
      ],
    };

    const mockKpiId = 'kpi2';
    const mockIdx = -1;
    const mockEnabledKpi = {
      kpiDetail: { xaxisLabel: 'Weekly' },
      isEnabled: true,
      shown: true,
      order: 2,
    };
    component.colorObj = { branch2: { nodeName: 'branch2' } };
    spyOn(component, 'sortingRowsInTable');

    component.getTableData(mockKpiId, mockIdx, mockEnabledKpi);
    expect(component.sortingRowsInTable).not.toHaveBeenCalled();
  });

  it('should update kpiTableDataObj with correct data when idx is less than 0 and filter is not overall', () => {
    component.allKpiArray = [
      {
        kpiId: 'kpi1',
        kpiName: 'KPI 1',
        trendValueList: [{ data: 'value1' }, { data: 'value2' }],
      },
      {
        kpiId: 'kpi2',
        kpiName: 'KPI 2',
        trendValueList: [{ data: 'value3' }, { data: 'value4' }],
      },
    ];
    // component.colorObj = { color1: { nodeName: 'branch1' }, color2: { nodeName: 'branch2' } };
    component.kpiTrendsObj = {
      kpi1: [
        {
          hierarchyName: 'branch1',
          value: 'value1',
          trend: 'trend1',
          maturity: 'maturity1',
          kpiUnit: 'unit1',
        },
      ],
      kpi2: [
        {
          hierarchyName: 'branch2',
          value: 'value2',
          trend: 'trend2',
          maturity: 'maturity2',
          kpiUnit: 'unit2',
        },
      ],
    };
    component.kpiTableDataObj = {
      branch1: [
        {
          kpiId: 'kpi1',
          kpiName: 'KPI 1',
          frequency: 'Monthly',
          show: true,
          hoverText: [],
          order: 1,
        },
      ],
      branch2: [
        {
          kpiId: 'kpi2',
          kpiName: 'KPI 2',
          frequency: 'Weekly',
          show: true,
          hoverText: [],
          order: 2,
        },
      ],
    };

    const mockKpiId = 'kpi72';
    const mockIdx = -1;
    const mockEnabledKpi = {
      kpiDetail: { xaxisLabel: 'Weekly' },
      isEnabled: true,
      shown: true,
      order: 2,
    };
    component.colorObj = { branch2: { nodeName: 'branch2', color: 'red' } };
    spyOn(component, 'sortingRowsInTable');

    component.getTableData(mockKpiId, mockIdx, mockEnabledKpi);
    expect(component.sortingRowsInTable).not.toHaveBeenCalled();
  });

  it('should add kpiId to maturityTableKpiList if not already present', () => {
    component.allKpiArray = [
      {
        kpiId: 'kpi1',
        kpiName: 'KPI 1',
        trendValueList: [{ data: 'value1' }, { data: 'value2' }],
      },
      {
        kpiId: 'kpi2',
        kpiName: 'KPI 2',
        trendValueList: [{ data: 'value3' }, { data: 'value4' }],
      },
    ];
    component.colorObj = {
      branch1: { nodeName: 'branch1' },
      branch2: { nodeName: 'branch2' },
    };
    component.kpiTrendsObj = {
      kpi1: [
        {
          hierarchyName: 'branch1',
          value: 'value1',
          trend: 'trend1',
          maturity: 'maturity1',
          kpiUnit: 'unit1',
        },
      ],
      kpi2: [
        {
          hierarchyName: 'branch2',
          value: 'value2',
          trend: 'trend2',
          maturity: 'maturity2',
          kpiUnit: 'unit2',
        },
      ],
    };
    component.kpiTableDataObj = {
      branch1: [
        {
          kpiId: 'kpi1',
          kpiName: 'KPI 1',
          frequency: 'Monthly',
          show: true,
          hoverText: [],
          order: 1,
        },
      ],
      branch2: [
        {
          kpiId: 'kpi2',
          kpiName: 'KPI 2',
          frequency: 'Weekly',
          show: true,
          hoverText: [],
          order: 2,
        },
      ],
    };

    const mockKpiId = 'kpi5';
    const mockIdx = 0;
    const mockEnabledKpi = {
      kpiDetail: { xaxisLabel: 'Monthly' },
      isEnabled: true,
      shown: true,
      order: 1,
    };
    spyOn(component, 'sortingRowsInTable');

    component.getTableData(mockKpiId, mockIdx, mockEnabledKpi);
    expect(component.sortingRowsInTable).toHaveBeenCalled();
  });

  it('should set correct kpiChartData on release dashboard', () => {
    component.allKpiArray = [
      {
        kpiId: 'kpi142',
        kpiName: 'Defect Count by RCA',
        unit: 'Count',
        chartType: '',
        id: '65793ddb127be336160bc0ff',
        isDeleted: 'False',
        kpiCategory: 'Release',
        kpiUnit: 'Count',
        kanban: false,
        kpiSource: 'Jira',
        groupId: 9,
        sprint: 'Phase 1_API POD 2 - Account Management',
        modalHeads: [
          'Issue ID',
          'Issue Description',
          'Sprint Name',
          'Issue Type',
          'Issue Status',
          'Root Cause',
          'Priority',
          'Assignee',
          'Testing Phase',
        ],
        trendValueList: [
          {
            filter1: 'Open Defects',
          },
          {
            filter1: 'Total Defects',
            value: [
              {
                data: '1',
                sSprintName: 'Undefined',
                value: [
                  {
                    subFilter: 'None',
                    value: 1,
                    size: 0,
                  },
                ],
              },
            ],
          },
        ],
        maxValue: '',
        kpiInfo: {
          definition:
            'It shows the breakup of all defects tagged to a release based on RCA. The breakup is shown in terms of count at different testing phases.',
          details: [
            {
              type: 'link',
              kpiLinkDetail: {
                text: 'Detailed Information at',
                link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/79953937/Release+Defect+count+by+RCA',
              },
            },
          ],
        },
      },
    ];
    component.kpiSelectedFilterObj = {
      kpi142: {
        filter1: ['Open Defects'],
      },
    };
    component.getChartDataforRelease('kpi142', 0, undefined, false);
    expect(component.kpiChartData['kpi142']).toEqual([
      {
        filter1: 'Open Defects',
      },
    ]);
  });

  it('should set correct kpiChartData on release dashboard when trendvaluelist has value attribute', () => {
    component.allKpiArray = [
      {
        kpiId: 'kpi142',
        kpiName: 'Defect Count by RCA',
        unit: 'Count',
        chartType: '',
        id: '65793ddb127be336160bc0ff',
        isDeleted: 'False',
        kpiCategory: 'Release',
        kpiUnit: 'Count',
        kanban: false,
        kpiSource: 'Jira',
        groupId: 9,
        sprint: 'Phase 1_API POD 2 - Account Management',
        modalHeads: [
          'Issue ID',
          'Issue Description',
          'Sprint Name',
          'Issue Type',
          'Issue Status',
          'Root Cause',
          'Priority',
          'Assignee',
          'Testing Phase',
        ],
        trendValueList: [
          {
            filter1: 'Open Defects',
          },
          {
            filter1: 'Total Defects',
            value: [
              {
                data: '1',
                sSprintName: 'Undefined',
                value: [
                  {
                    subFilter: 'None',
                    value: 1,
                    size: 0,
                  },
                ],
              },
            ],
          },
        ],
        maxValue: '',
        kpiInfo: {
          definition:
            'It shows the breakup of all defects tagged to a release based on RCA. The breakup is shown in terms of count at different testing phases.',
          details: [
            {
              type: 'link',
              kpiLinkDetail: {
                text: 'Detailed Information at',
                link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/79953937/Release+Defect+count+by+RCA',
              },
            },
          ],
        },
      },
      {
        kpiId: 'kpi143',
        kpiName: 'Defect Count by Assignee',
        unit: 'Count',
        chartType: '',
        id: '65793ddb127be336160bc100',
        isDeleted: 'False',
        kpiCategory: 'Release',
        kpiUnit: 'Count',
        kanban: false,
        kpiSource: 'Jira',
        groupId: 9,
        sprint: 'Phase 1_API POD 2 - Account Management',
        modalHeads: [
          'Issue ID',
          'Issue Description',
          'Sprint Name',
          'Issue Type',
          'Issue Status',
          'Root Cause',
          'Priority',
          'Assignee',
        ],
        trendValueList: [
          {
            filter1: 'Open Defects',
            value: [
              {
                data: 'API POD 2 - Account Management',
                value: [
                  {
                    data: '0',
                    value: {},
                  },
                ],
              },
            ],
          },
          {
            filter1: 'Total Defects',
            value: [
              {
                data: 'API POD 2 - Account Management',
                value: [
                  {
                    data: '1',
                    value: {
                      'Nidhi Goyal': 1,
                    },
                  },
                ],
              },
            ],
          },
        ],
        maxValue: '',
        kpiInfo: {
          definition:
            'It shows the breakup of all defects tagged to a release based on Assignee. The breakup is shown in terms of count & percentage.',
          details: [
            {
              type: 'link',
              kpiLinkDetail: {
                text: 'Detailed Information at',
                link: 'https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/79691782/Release+Defect+count+by+Assignee',
              },
            },
          ],
        },
      },
    ];
    component.kpiSelectedFilterObj = {
      kpi142: {
        filter1: ['Open Defects'],
      },
      kpi143: {
        filter1: ['Open Defects'],
      },
    };
    component.getChartDataforRelease('kpi143', 1, undefined, false);
    expect(component.kpiChartData['kpi143']).toEqual([
      {
        data: 'API POD 2 - Account Management',
        value: [
          {
            data: '0',
            value: {},
          },
        ],
      },
    ]);
  });

  it('should set selectedKPITab to the provided tab', () => {
    const mockTab = 'tab1';

    component.selectKPITab(mockTab);

    expect(component.selectedKPITab).toEqual(mockTab);
  });

  // it('should set filter value to first option if formType is radiobutton', () => {
  //   component.allKpiArray = [
  //     {
  //       filters: {
  //         filter1: ['value1'],
  //         filter2: ['value2'],
  //       },
  //     },
  //     {
  //       kpiId: 'kpi1',
  //       filters: {},
  //     },
  //   ];
  //   component.updatedConfigGlobalData = [
  //     {
  //       kpiId: 'kpi1',
  //       kpiDetail: {
  //         kpiFilter: 'RadioButton',
  //       },
  //     },
  //     {
  //       kpiId: 'kpi2',
  //       kpiDetail: {
  //         kpiFilter: 'Dropdown',
  //       },
  //     },
  //   ];
  //   component.kpiDropdowns = {
  //     kpi1: [
  //       {
  //         options: ['option1', 'option2'],
  //       },
  //     ],
  //   };
  //   const mockData = {
  //     kpi1: {
  //       kpiId: 'kpi1',
  //     },
  //   };
  //   const mockKey = 'kpi1';
  //   const setBackupSpy = spyOn(component, 'setFilterValueIfAlreadyHaveBackup');
  //   component.populateKPIFilters(mockData, mockKey);

  //   expect(setBackupSpy).toHaveBeenCalledWith('kpi1', {}, [undefined]);
  // });

  // it('should set filter value to Overall if formType is dropdown', () => {
  //   component.allKpiArray = [
  //     {
  //       filters: {
  //         filter1: ['value1'],
  //         filter2: ['value2'],
  //       },
  //     },
  //     {
  //       kpiId: 'kpi1',
  //       filters: {},
  //     },
  //   ];
  //   component.updatedConfigGlobalData = [
  //     {
  //       kpiId: 'kpi1',
  //       kpiDetail: {
  //         kpiFilter: 'RadioButton',
  //       },
  //     },
  //     {
  //       kpiId: 'kpi2',
  //       kpiDetail: {
  //         kpiFilter: 'Dropdown',
  //       },
  //     },
  //   ];
  //   component.kpiDropdowns = {
  //     kpi1: [
  //       {
  //         options: ['option1', 'option2'],
  //       },
  //     ],
  //   };
  //   const mockData = {
  //     kpi2: {
  //       kpiId: 'kpi2',
  //     },
  //   };
  //   const mockKey = 'kpi2';
  //   const setBackupSpy = spyOn(component, 'setFilterValueIfAlreadyHaveBackup');
  //   component.populateKPIFilters(mockData, mockKey);

  //   expect(setBackupSpy).toHaveBeenCalledWith('kpi2', {}, ['Overall']);
  // });

  // it('should set filter value to Overall if filters exist', () => {
  //   component.allKpiArray = [
  //     {
  //       filters: {
  //         filter1: ['value1'],
  //         filter2: ['value2'],
  //       },
  //     },
  //     {
  //       kpiId: 'kpi1',
  //       filters: {},
  //     },
  //   ];
  //   component.updatedConfigGlobalData = [
  //     {
  //       kpiId: 'kpi1',
  //       kpiDetail: {
  //         kpiFilter: 'RadioButton',
  //       },
  //     },
  //     {
  //       kpiId: 'kpi2',
  //       kpiDetail: {
  //         kpiFilter: 'Dropdown',
  //       },
  //     },
  //   ];
  //   component.kpiDropdowns = {
  //     kpi1: [
  //       {
  //         options: ['option1', 'option2'],
  //       },
  //     ],
  //   };
  //   const mockData = {
  //     kpi3: {
  //       kpiId: 'kpi3',
  //     },
  //   };
  //   const mockKey = 'kpi3';
  //   const setBackupSpy = spyOn(component, 'setFilterValueIfAlreadyHaveBackup');
  //   component.populateKPIFilters(mockData, mockKey);

  //   expect(setBackupSpy).toHaveBeenCalledWith('kpi3', {}, ['Overall']);
  // });

  // it('should set filter value to Overall if no matching filter is found', () => {
  //   component.allKpiArray = [
  //     {
  //       filters: {
  //         filter1: ['value1'],
  //         filter2: ['value2'],
  //       },
  //     },
  //     {
  //       kpiId: 'kpi1',
  //       filters: {},
  //     },
  //   ];
  //   component.updatedConfigGlobalData = [
  //     {
  //       kpiId: 'kpi1',
  //       kpiDetail: {
  //         kpiFilter: 'RadioButton',
  //       },
  //     },
  //     {
  //       kpiId: 'kpi2',
  //       kpiDetail: {
  //         kpiFilter: 'Dropdown',
  //       },
  //     },
  //   ];
  //   component.kpiDropdowns = {
  //     kpi1: [
  //       {
  //         options: ['option1', 'option2'],
  //       },
  //     ],
  //   };
  //   const mockData = {
  //     kpi4: {
  //       kpiId: 'kpi4',
  //     },
  //   };
  //   const mockKey = 'kpi4';
  //   const setBackupSpy = spyOn(component, 'setFilterValueIfAlreadyHaveBackup');

  //   component.populateKPIFilters(mockData, mockKey);

  //   expect(setBackupSpy).toHaveBeenCalledWith('kpi4', {}, ['Overall']);
  // });

  it('should return the maximum number of sprints for any project', () => {
    const eventMock = {
      filterApplyData: {
        selectedMap: {
          sprint: [
            'sprint1_project1',
            'sprint2_project1',
            'n1_sprint2_project1',
            'sprint1_project2',
            'sprint2_project2',
            'n1_sprint2_project2',
          ],
        },
      },
    };

    const result = component.coundMaxNoOfSprintSelectedForProject(eventMock);
    expect(result).toBe(3); // Project 1 has 3 sprints, and so does project 2
  });

  it('should return sprint count from config if no sprints are selected', () => {
    const eventMock = {
      configDetails: {
        sprintCountForKpiCalculation: 5,
      },
    };

    const result = component.coundMaxNoOfSprintSelectedForProject(eventMock);
    expect(result).toBe(5);
  });

  it('should handle empty selectedMap gracefully', () => {
    const eventMock = {
      filterApplyData: {
        selectedMap: {
          sprint: [],
        },
      },
      configDetails: {
        sprintCountForKpiCalculation: 3,
      },
    };

    const result = component.coundMaxNoOfSprintSelectedForProject(eventMock);
    expect(result).toBe(3);
  });

  it('should handle selected option on release when event is an object when event key equals to 0', () => {
    const mockEvent = {
      filter1: ['test1', 'test2'],
      filter2: [],
    };
    const mockKpi = { kpiId: 'kpi1' };

    component.selectedTab = 'value';
    component.handleSelectedOption(mockEvent, mockKpi);

    expect(component.kpiSelectedFilterObj[mockKpi.kpiId]).toEqual(mockEvent);
  });

  it('should handle selected option on release when event is an object when event key equals to 0 and dor single dropdown', () => {
    const mockEvent = {
      filter1: 'test1',
    };
    const mockKpi = { kpiId: 'kpi1', kpiDetail: { kpiFilter: 'dropDown' } };

    component.selectedTab = 'value';
    component.handleSelectedOption(mockEvent, mockKpi);

    expect(component.kpiSelectedFilterObj[mockKpi.kpiId]).toBeDefined();
  });

  it('should handle selected option on release when event is an object when event key equals to 0', () => {
    const mockEvent = {
      filter1: 'test1',
    };
    const mockKpi = { kpiId: 'kpi1', kpiDetail: { kpiFilter: 'nondropDown' } };

    component.selectedTab = 'value';
    component.handleSelectedOption(mockEvent, mockKpi);

    expect(component.kpiSelectedFilterObj[mockKpi.kpiId]).toBeDefined();
  });

  describe('checkIfDataPresent', () => {
    it('should return true if data is present and kpiStatusCode is "200"', () => {
      component.kpiStatusCodeArr = { data: '200' };
      component.kpiChartData = { data: [{ data: [1, 2, 3] }] };

      const result = component.checkIfDataPresent('data');

      expect(result).toBe(false);
    });

    it('should return false if data is not present', () => {
      component.kpiStatusCodeArr = { data: '200' };
      component.kpiChartData = {};

      const result = component.checkIfDataPresent('data');

      expect(result).toBe(false);
    });

    it('should return false if kpiStatusCode is not "200"', () => {
      component.kpiStatusCodeArr = { data: '400' };
      component.kpiChartData = { data: [{ data: [1, 2, 3] }] };

      const result = component.checkIfDataPresent('data');

      expect(result).toBe(false);
    });

    it('should return false if data is not present at granular level', () => {
      component.kpiStatusCodeArr = { data: '200' };
      component.kpiChartData = { data: [{ data: [] }] };

      const result = component.checkIfDataPresent('data');

      expect(result).toBe(false);
    });
  });

  it('should getchartdata on backlog board for kpi when trendValueList is an object', () => {
    component.allKpiArray = [
      {
        kpiId: 'kpi124',
        trendValueList: {
          value: [
            {
              filter1: 'Overall',
              filter2: 'Overall',
              data: [
                {
                  label: 'Scope added',
                  value: 1,
                  value1: 0,
                  labelInfo: '(Issue Count/Original Estimate)',
                  unit: '',
                },
              ],
            },
          ],
        },
      },
    ];
    component.kpiSelectedFilterObj['kpi124'] = {
      filter1: ['Overall'],
      filter2: ['Overall'],
    };
    const res = {
      filter1: 'Overall',
      filter2: 'Overall',
      data: [
        {
          label: 'Issue without estimates',
        },
      ],
    };
    component.updatedConfigGlobalData = [
      {
        kpiId: 'kpi125',
        kpiDetail: {
          chartType: 'GroupBarChart',
        },
      },
    ];
    spyOn(component, 'createTrendsData');
    component.getChartDataForBacklog('kpi124', 0, 'sum');
    expect(component.kpiChartData).toBeDefined();
  });

  it('should get getChartDataForBacklog for kpi when trendValueList is an object with single filter', () => {
    component.allKpiArray = [
      {
        kpiId: 'kpi124',
        trendValueList: {
          value: [
            {
              filter1: 'Overall',
              data: [
                {
                  label: 'Scope added',
                },
              ],
            },
          ],
        },
      },
    ];
    component.kpiSelectedFilterObj['kpi124'] = {
      filter1: ['Overall'],
    };
    component.updatedConfigGlobalData = [
      {
        kpiId: 'kpi125',
        kpiDetail: {
          chartType: 'GroupBarChart',
        },
      },
    ];

    spyOn(component, 'createTrendsData');
    component.getChartDataForBacklog('kpi124', 0, 'sum');
    expect(component.kpiChartData).toBeDefined();
  });

  it('should get getChartDataForBacklog for kpi when trendValueList is an object and KPI selected filter is blank', () => {
    component.allKpiArray = [
      {
        kpiId: 'kpi124',
        trendValueList: {
          value: [
            {
              filter1: 'Overall',
              data: [
                {
                  label: 'Scope added',
                  value: 1,
                },
              ],
            },
          ],
        },
      },
    ];
    component.updatedConfigGlobalData = [
      {
        kpiId: 'kpi125',
        kpiDetail: {
          chartType: 'GroupBarChart',
        },
      },
    ];
    component.kpiSelectedFilterObj['kpi124'] = {};

    spyOn(component, 'createTrendsData');
    component.getChartDataForBacklog('kpi124', 0, 'sum');
    expect(component.kpiChartData['kpi124'][0].data.length).toBeGreaterThan(0);
  });

  it('should create trend data for the given kpiId when the data exists', () => {
    component.configGlobalData = [
      { kpiId: 1, name: 'KPI 1' },
      { kpiId: 2, name: 'KPI 2' },
    ];
    component.kpiChartData = {
      1: [
        { data: 'Data 1', value: [1, 2, 3], maturity: 1, maturityValue: 'Low' },
        {
          data: 'Data 2',
          value: [4, 5, 6],
          maturity: 2,
          maturityValue: 'Medium',
        },
      ],
      2: [
        {
          data: 'Data 3',
          value: [7, 8, 9],
          maturity: 3,
          maturityValue: 'High',
        },
      ],
    };
    component.kpiTrendObject = {};
    spyOn(component, 'checkLatestAndTrendValue').and.returnValue([
      '3',
      'NA',
      '%',
    ]);
    // call the method
    component.createTrendsData(1);

    // check if the kpiTrendObject was updated correctly
    // expect(component.kpiTrendObject[1]).toEqual([
    //   {
    //     hierarchyName: 'Data 1',
    //     trend: 'NA',
    //     maturity: 'M1',
    //     maturityValue: 'Low',
    //     maturityDenominator: 3,
    //     kpiUnit: '%'
    //   }
    // ]);
  });

  it('should not create trend data for the given kpiId when the data does not exist', () => {
    component.configGlobalData = [
      { kpiId: 1, name: 'KPI 1' },
      { kpiId: 2, name: 'KPI 2' },
    ];
    // call the method
    component.createTrendsData(3);

    // check if the kpiTrendObject remains empty
    expect(component.kpiTrendObject[3]).toBeUndefined();
  });

  it('should get kpi171 data', () => {
    const fakeJiraData = [
      {
        kpiId: 'kpi171',
        kpiName: 'Cycle Time',
        unit: '%',
        maxValue: '200',
        chartType: '',
        trendValueList: { ...fakeKpi171Data },
      },
    ];
    component.kpiSelectedFilterObj['kpi171'] = { filter1: 'Task' };
    spyOn(component, 'ifKpiExist');
    component.allKpiArray = [];
    component.updatedConfigGlobalData = [
      {
        id: '655e0d435769c2002ad81574',
        kpiId: 'kpi171',
        kpiName: 'Flow Efficiency',
        isDeleted: 'False',
        defaultOrder: 1,
        kpiCategory: 'Backlog',
        kpiSubCategory: 'Flow KPIs',
        kpiUnit: '%',
        chartType: '',
        showTrend: false,
        isPositiveTrend: false,
        calculateMaturity: false,
        hideOverallFilter: false,
        kpiSource: 'Jira',
        kanban: false,
        groupId: 11,
        kpiInfo: {
          definition:
            'The percentage of time spent in work states vs wait states across the lifecycle of an issue',
        },
        kpiFilter: 'dropDown',
        aggregationCriteria: 'average',
        trendCalculative: false,
        xaxisLabel: 'Duration',
        yaxisLabel: 'Percentage',
        isAdditionalFilterSupport: false,
        isEnabled: true,
        shown: true,
        kpiDetail: {
          kanban: false,
          kpiSource: 'Jira',
          groupId: 11,
          isEnabled: true,
          shown: true,
          kpiId: 'kpi171',
        },
      },
    ];

    component.filterApplyData = { level: 'level1', label: 'level1' };
    spyOn(component, 'getChartDataForCardWithCombinationFilter');
    const spy = spyOn(httpService, 'postKpiNonTrend').and.returnValue(
      of([
        {
          kpiId: 'kpi171',
          trendValueList: fakeKpi171Data,
        },
      ]),
    );
    component.getkpi171Data('kpi171', fakeKpi171Data);
    expect(spy).toHaveBeenCalled();
  });

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
              value: 0,
            },
          ],
        },
      ],
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
            value: 5,
          },
        ],
      },
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
              value: 0,
            },
          ],
        },
      ],
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
            value: 0,
          },
        ],
      },
    ]);
  });

  it('should handle selected option when have single dropdown', () => {
    const event = {
      filter1: ['Tech Story'],
    };
    const kpi = {
      kpiId: 'kpi123',
    };
    component.kpiSelectedFilterObj['kpi123'] = {};
    component.kpiSelectedFilterObj['kpi123'] = event;
    spyOn(component, 'getChartData');
    service.setKpiSubFilterObj(component.kpiSelectedFilterObj);
    component.handleSelectedOption(event, kpi);
    expect(
      Object.keys(component.kpiSelectedFilterObj['kpi123']).length,
    ).toEqual(Object.keys(event).length);
  });

  it('should convert to hours if time', () => {
    const time = '14880';
    const unit = 'hours';
    const convertedTime = component.convertToHoursIfTime(time, unit);
    expect(convertedTime).toEqual('248h');
  });

  it('should issue details view shown on arrow click', () => {
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
    component.service.setSelectedTrends([{ basicProjectConfigId: 'testid' }]);

    const tableValues = [
      {
        ['Issue Description']:
          'Playground server is failing with OutOfMemoryError',
        ['Issue Id']: 'DTS-20225',
      },
    ];
    component.handleArrowClick(kpi, 'Issue Count', tableValues);
    expect(component.displayModal).toBeFalse();
  });

  it('should convert to hours', () => {
    let result = component.convertToHoursIfTime(25, 'hours');
    expect(result).toEqual('25m');

    result = component.convertToHoursIfTime(65, 'hours');
    expect(result).toEqual('1h 5m');

    result = component.convertToHoursIfTime(60, 'hours');
    expect(result).toEqual('1h');
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
      1: { filter1: 'value1', filter2: 'value2' },
    });
    expect(component.getChartDataForCard).toHaveBeenCalledWith(1, -1);
    expect(service.setKpiSubFilterObj).toHaveBeenCalledWith(
      component.kpiSelectedFilterObj,
    );
  });

  it('should update the kpiSelectedFilterObj correctly when the event is not empty for kpi138', () => {
    // create sample data
    const event = { filter1: 'value1', filter2: 'value2' };
    const kpi = { kpiId: 'kpi138' };
    // call the method
    spyOn(component, 'getChartDataForCard').and.callThrough();
    spyOn(service, 'setKpiSubFilterObj');
    component.handleSelectedOptionForCard(event, kpi);

    // check if the kpiSelectedFilterObj was updated correctly
    expect(component.kpiSelectedFilterObj).toEqual({
      kpi138: { filter1: 'value1', filter2: 'value2' },
    });
    expect(component.getChartDataForCard).toHaveBeenCalledWith('kpi138', -1);
    expect(service.setKpiSubFilterObj).toHaveBeenCalledWith(
      component.kpiSelectedFilterObj,
    );
  });

  it('should apply aggregation logic for kpi138', () => {
    const arr = [
      {
        filter1: 'Tech Debt',
        filter2: 'Medium',
        data: [
          {
            label: 'Ready Backlog',
            value: 2,
            value1: 4,
            unit1: 'SP',
            modalValues: [],
          },
          {
            label: 'Backlog Strength',
            value: 0,
            unit: 'Sprint',
          },
          {
            label: 'Readiness Cycle time',
            value: 8,
            unit: 'days',
          },
        ],
      },
      {
        filter1: 'Story',
        filter2: 'Medium',
        data: [
          {
            label: 'Ready Backlog',
            value: 6,
            value1: 12,
            unit1: 'SP',
            modalValues: [],
          },
          {
            label: 'Backlog Strength',
            value: 0,
            unit: 'Sprint',
          },
          {
            label: 'Readiness Cycle time',
            value: 17,
            unit: 'days',
          },
        ],
      },
    ];
    const kpi138Obj = [
      {
        filter1: 'Tech Debt',
        filter2: 'Medium',
        data: [
          {
            label: 'Ready Backlog',
            value: 8,
            value1: 16,
            unit1: 'SP',
            modalValues: [],
          },
          {
            label: 'Backlog Strength',
            value: 0,
            unit: 'Sprint',
            value1: null,
            modalValues: null,
          },
          {
            label: 'Readiness Cycle time',
            value: 15,
            unit: 'days',
            value1: null,
            modalValues: null,
          },
        ],
      },
    ];
    spyOn(component, 'applyAggregationLogic').and.callThrough();
    expect(component.applyAggregationLogicForkpi138(arr)).toEqual(kpi138Obj);
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
        groupId: 2,
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
            filter: 'Overall',
            value: [
              {
                data: 'PSknowHOW ',
                value: [
                  {
                    data: '2',
                    sSprintID: '0-1',
                  },
                  {
                    data: '1',
                    sSprintID: '1-3',
                  },
                  {
                    data: '0',
                    sSprintID: '3-6',
                  },
                  {
                    data: '0',
                    sSprintID: '6-12',
                  },
                  {
                    data: '1',
                    sSprintID: '>12',
                  },
                ],
              },
            ],
          },
        ],
        groupId: 2,
        xAxisValues: ['0-1', '1-3', '3-6', '6-12', '>12'],
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
            filter: 'Overall',
            value: [
              {
                data: 'PSknowHOW ',
                value: [
                  {
                    data: '2',
                    sSprintID: '0-1',
                  },
                  {
                    data: '1',
                    sSprintID: '1-3',
                  },
                  {
                    data: '0',
                    sSprintID: '3-6',
                  },
                  {
                    data: '0',
                    sSprintID: '6-12',
                  },
                  {
                    data: '1',
                    sSprintID: '>12',
                  },
                ],
              },
            ],
          },
        ],
        groupId: 2,
        xAxisValues: ['0-1', '1-3', '3-6', '6-12', '>12'],
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
            filter: 'Overall',
            value: [
              {
                data: 'PSknowHOW ',
                value: [
                  {
                    data: '2',
                    sSprintID: '0-1',
                  },
                  {
                    data: '1',
                    sSprintID: '1-3',
                  },
                  {
                    data: '0',
                    sSprintID: '3-6',
                  },
                  {
                    data: '0',
                    sSprintID: '6-12',
                  },
                  {
                    data: '1',
                    sSprintID: '>12',
                  },
                ],
              },
            ],
          },
        ],
        groupId: 2,
        xAxisValues: ['0-1', '1-3', '3-6', '6-12', '>12'],
      },
    };
    const fakeJiraPayload = require('../../../test/resource/fakeJiraPayload.json');
    component.jiraKpiData = {};
    component.selectedTab = 'backlog';
    const spy = spyOn(httpService, 'postKpiNonTrend').and.returnValue(
      of(fakeJiraGroupId1),
    );
    const spycreateKpiWiseId = spyOn(
      helperService,
      'createKpiWiseId',
    ).and.returnValue(jiraKpiData);
    const spycreateAllKpiArray = spyOn(
      component,
      'createAllKpiArrayForBacklog',
    );
    component.postJiraKpi(fakeJiraPayload, 'jira');
    tick();
    // expect(spycreateKpiWiseId).toHaveBeenCalled();
    expect(spycreateAllKpiArray).toHaveBeenCalledWith(jiraKpiData);
  }));

  xit('should handle selected option when have multi dropdown', () => {
    const event = {
      filter1: ['Tech Story'],
      filter2: ['Medium'],
    };
    component.kpiSelectedFilterObj['kpi123'] = {
      filter1: ['Tech Story'],
    };
    component.allKpiArray = [
      {
        kpiId: 'kpi123',
        trendValueList: [
          {
            filter: 'Overall',
            value: [
              {
                data: 'PSknowHOW ',
                value: [
                  {
                    data: '2',
                    sSprintID: '0-1',
                  },
                  {
                    data: '1',
                    sSprintID: '1-3',
                  },
                  {
                    data: '0',
                    sSprintID: '3-6',
                  },
                  {
                    data: '0',
                    sSprintID: '6-12',
                  },
                  {
                    data: '1',
                    sSprintID: '>12',
                  },
                ],
              },
            ],
          },
        ],
      },
    ];
    const kpi = {
      kpiId: 'kpi123',
      kpiDetail: { chartType: 'line', aggregationCriteria: null },
    };
    component.kpiSelectedFilterObj['kpi123'] = {};
    component.kpiSelectedFilterObj['kpi123'] = event;
    spyOn(component, 'getChartData');
    component.selectedTab = 'backlog';
    service.setKpiSubFilterObj(component.kpiSelectedFilterObj);
    component.handleSelectedOption(event, kpi);
    expect(
      Object.keys(component.kpiSelectedFilterObj['kpi123']).length,
    ).toEqual(Object.keys(event).length);
  });

  it('should update the kpiSelectedFilterObj correctly when the event is not empty', () => {
    // create sample data
    const event = { filter1: 'value1', filter2: 'value2' };
    const kpi = { kpiId: 1, kpiDetail: { chartType: 'line' } };
    // call the method
    spyOn(component, 'getChartDataForCard').and.callThrough();
    spyOn(service, 'setKpiSubFilterObj');
    component.handleSelectedOptionForCard(event, kpi);

    // check if the kpiSelectedFilterObj was updated correctly
    expect(component.kpiSelectedFilterObj).toEqual({
      1: { filter1: 'value1', filter2: 'value2' },
    });
    expect(component.getChartDataForCard).toHaveBeenCalledWith(1, -1);
    expect(service.setKpiSubFilterObj).toHaveBeenCalledWith(
      component.kpiSelectedFilterObj,
    );
  });

  it('should get dropdown array for kpi', () => {
    const kpiDropdowns = {
      kpi75: [
        {
          filterType: 'Filter by issue type',
          options: ['Tech Story', 'Technical Debt', 'Bug', 'Story'],
        },
      ],
    };
    spyOn(component, 'ifKpiExist').and.returnValue('0');
    component.allKpiArray = [
      {
        kpiId: 'kpi75',
        filters: {
          filter1: {
            filterType: 'Filter by issue type',
            options: ['Tech Story', 'Technical Debt', 'Bug', 'Story'],
          },
        },
      },
    ];
    component.getDropdownArrayForBacklog('kpi75');
    expect(component.kpiDropdowns['kpi75'].length).toEqual(
      kpiDropdowns['kpi75'].length,
    );
  });

  it('should get dropdown array for kpi with filter in trending list', () => {
    const kpiDropdowns = {
      kpi75: [
        {
          filterType: 'Filter by issue type',
          options: ['Tech Story', 'Technical Debt', 'Bug', 'Story'],
        },
      ],
    };
    spyOn(component, 'ifKpiExist').and.returnValue('0');
    component.allKpiArray = [
      {
        kpiId: 'kpi75',
        trendValueList: [
          {
            filter: 'Overall',
            data: [
              {
                label: 'Scope added',
                value: 1,
                value1: 0,
                labelInfo: '(Issue Count/Original Estimate)',
                unit: '',
              },
            ],
          },
        ],
      },
    ];
    component.getDropdownArrayForBacklog('kpi75');
    expect(component.kpiDropdowns).toBeDefined();
  });

  it('should get dropdown array for kpi with filter1 in trending list', () => {
    const kpiDropdowns = {
      kpi75: [
        {
          filterType: 'Filter by issue type',
          options: ['Tech Story', 'Technical Debt', 'Bug', 'Story'],
        },
      ],
    };
    spyOn(component, 'ifKpiExist').and.returnValue('0');
    component.allKpiArray = [
      {
        kpiId: 'kpi75',
        trendValueList: [
          {
            filter1: 'Overall',
            data: [
              {
                label: 'Scope added',
                value: 1,
                value1: 0,
                labelInfo: '(Issue Count/Original Estimate)',
                unit: '',
              },
            ],
          },
        ],
      },
    ];
    component.getDropdownArrayForBacklog('kpi75');
    expect(component.kpiDropdowns).toBeDefined();
  });

  it('should handle kpi171 without filter2', () => {
    const kpiId = 'kpi171';
    const trendValueList = {
      value: [
        {
          filter1: 'Enabler Story',
          data: [
            {
              label: 'Intake - DOR',
              value: 28,
              value1: 1,
              unit: 'd',
              unit1: 'issues',
              modalValues: [
                {
                  spill: false,
                  preClosed: false,
                  'Issue Id': 'DTS-30246',
                },
              ],
            },
            {
              label: 'DOD - Live',
              value: 0,
              value1: 0,
              unit: 'd',
              unit1: 'issues',
              modalValues: [],
            },
          ],
        },
        {
          filter1: 'bug',
          data: [
            {
              label: 'Intake - DOR',
              value: 28,
              value1: 1,
              unit: 'd',
              unit1: 'issues',
              modalValues: [
                {
                  spill: false,
                  preClosed: false,
                  'Issue Id': 'DTS-30246',
                },
              ],
            },
          ],
        },
      ],
    };

    component.kpiSelectedFilterObj = {
      [kpiId]: {
        filter1: 'Past Month',
        filter2: ['Enabler Story', 'bug'],
      },
    };

    component.getChartDataForCardWithCombinationFilter(kpiId, trendValueList);

    expect(component.kpiChartData).toBeDefined();
  });

  xit('should create allkpiarray when we have filter property in trendValueList list', () => {
    localStorage.setItem(
      'completeHierarchyData',
      JSON.stringify({
        kanban: [
          {
            id: '6442815917ed167d8157f0f5',
            level: 1,
            hierarchyLevelId: 'bu',
            hierarchyLevelName: 'BU',
            hierarchyInfo: 'Business Unit',
          },
          {
            id: '6442815917ed167d8157f0f6',
            level: 2,
            hierarchyLevelId: 'ver',
            hierarchyLevelName: 'Vertical',
            hierarchyInfo: 'Industry',
          },
          {
            id: '6442815917ed167d8157f0f7',
            level: 3,
            hierarchyLevelId: 'acc',
            hierarchyLevelName: 'Account',
            hierarchyInfo: 'Account',
          },
          {
            id: '6442815917ed167d8157f0f8',
            level: 4,
            hierarchyLevelId: 'port',
            hierarchyLevelName: 'Engagement',
            hierarchyInfo: 'Engagement',
          },
          {
            level: 5,
            hierarchyLevelId: 'project',
            hierarchyLevelName: 'Project',
          },
          {
            level: 6,
            hierarchyLevelId: 'release',
            hierarchyLevelName: 'Release',
          },
          { level: 7, hierarchyLevelId: 'sqd', hierarchyLevelName: 'Squad' },
        ],
        scrum: [
          {
            id: '6442815917ed167d8157f0f5',
            level: 1,
            hierarchyLevelId: 'bu',
            hierarchyLevelName: 'BU',
            hierarchyInfo: 'Business Unit',
          },
          {
            id: '6442815917ed167d8157f0f6',
            level: 2,
            hierarchyLevelId: 'ver',
            hierarchyLevelName: 'Vertical',
            hierarchyInfo: 'Industry',
          },
          {
            id: '6442815917ed167d8157f0f7',
            level: 3,
            hierarchyLevelId: 'acc',
            hierarchyLevelName: 'Account',
            hierarchyInfo: 'Account',
          },
          {
            id: '6442815917ed167d8157f0f8',
            level: 4,
            hierarchyLevelId: 'port',
            hierarchyLevelName: 'Engagement',
            hierarchyInfo: 'Engagement',
          },
          {
            level: 5,
            hierarchyLevelId: 'project',
            hierarchyLevelName: 'Project',
          },
          {
            level: 6,
            hierarchyLevelId: 'sprint',
            hierarchyLevelName: 'Sprint',
          },
          {
            level: 6,
            hierarchyLevelId: 'release',
            hierarchyLevelName: 'Release',
          },
          { level: 7, hierarchyLevelId: 'sqd', hierarchyLevelName: 'Squad' },
        ],
      }),
    );
    const data = {
      kpi141: {
        kpiId: 'kpi141',
        kpiName: 'Defect Count by Status',
        chartType: '',
        kpiInfo: {
          definition:
            'It shows the breakup of all defects tagged to a release based on Status. The breakup is shown in terms of count & percentage.',
        },
        id: '64b4ed7acba3c12de164732c',
        isDeleted: false,
        kpiCategory: 'Release',
        kpiUnit: 'Count',
        kanban: false,
        kpiSource: 'Jira',
        trendValueList: [
          {
            filter: 'story',
            value: [
              {
                data: '1',
                value: [
                  {
                    value: 0,
                  },
                ],
                kpiGroup: 'Issue Count',
              },
            ],
          },
        ],
        groupId: 9,
      },
    };

    component.updatedConfigGlobalData = [
      {
        kpiId: 'kpi141',
        kpiName: 'Deployment Frequency',
        isEnabled: true,
        order: 23,
        kpiDetail: {
          kpiFilter: 'multiDropdown',
          chartType: 'graph',
        },
        shown: true,
      },
    ];

    component.kpiSelectedFilterObj['kpi124'] = {
      filter1: ['story'],
    };
    component.kpiDropdowns = {
      kpi141: {
        options: ['story'],
      },
    };
    spyOn(component, 'ifKpiExist').and.returnValue(-1);
    spyOn(component, 'createTrendsData');
    component.createAllKpiArray(data);
    expect(component.kpiSelectedFilterObj).toBeDefined();
  });

  it('should prepare data from trending value list when there is no kpi filter and value is blank', () => {
    component.allKpiArray = [
      {
        kpiId: 'kpi138',
        trendValueList: {
          value: [],
        },
      },
    ];
    component.getChartDataForCard('kpi138', 0);
    expect(component.kpiChartData).toBeDefined();
  });

  it('should prepare data from trending value list when there is no kpi filter and value is not blank', () => {
    component.allKpiArray = [
      {
        kpiId: 'kpi124',
        trendValueList: {
          value: [
            {
              data: [
                {
                  label: 'Scope added',
                  value: 1,
                  value1: 0,
                  labelInfo: '(Issue Count/Original Estimate)',
                  unit: '',
                },
              ],
            },
          ],
        },
      },
    ];
    component.getChartDataForCard('kpi124', 0);
    expect(component.kpiChartData).toBeDefined();
  });

  it('should prepare data from trending value list when there is no kpi filter and trendinglist is array ', () => {
    component.allKpiArray = [
      {
        kpiId: 'kpi124',
        trendValueList: [
          {
            value: [
              {
                data: [
                  {
                    label: 'Scope added',
                    value: 1,
                    value1: 0,
                    labelInfo: '(Issue Count/Original Estimate)',
                    unit: '',
                  },
                ],
              },
            ],
          },
        ],
      },
    ];
    component.getChartDataForCard('kpi124', 0);
    expect(component.kpiChartData).toBeDefined();
  });

  it('should prepare data from trending value list when have multi dropdown filter', () => {
    component.allKpiArray = [
      {
        kpiId: 'kpi124',
        trendValueList: {
          value: [
            {
              filter1: 'Overall',
              filter2: 'Overall',
              data: [
                {
                  label: 'Scope added',
                  value: 1,
                  value1: 0,
                  labelInfo: '(Issue Count/Original Estimate)',
                  unit: '',
                },
              ],
            },
          ],
        },
      },
    ];
    component.kpiSelectedFilterObj['kpi124'] = {
      filter1: ['story'],
      filter2: ['bug'],
    };
    component.getChartDataForCard('kpi124', 0);
    expect(component.kpiChartData).toBeDefined();
  });

  it('should prepare data from trending value list when have single dropdown filter', () => {
    component.allKpiArray = [
      {
        kpiId: 'kpi124',
        trendValueList: {
          value: [
            {
              filter1: 'Overall',
              data: [
                {
                  label: 'Scope added',
                  value: 1,
                  value1: 0,
                  labelInfo: '(Issue Count/Original Estimate)',
                  unit: '',
                },
              ],
            },
          ],
        },
      },
    ];
    component.kpiSelectedFilterObj['kpi124'] = {
      filter1: ['story'],
    };
    component.getChartDataForCard('kpi124', 0);
    expect(component.kpiChartData).toBeDefined();
  });

  it('should prepare data from trending value list when have radio button', () => {
    component.allKpiArray = [
      {
        kpiId: 'kpi124',
        trendValueList: {
          value: [
            {
              filter1: 'Overall',
              data: [
                {
                  label: 'Scope added',
                  value: 1,
                  value1: 0,
                  labelInfo: '(Issue Count/Original Estimate)',
                  unit: '',
                },
              ],
            },
          ],
        },
      },
    ];
    component.kpiSelectedFilterObj['kpi124'] = {
      filter1: 'story',
    };
    component.getChartDataForCard('kpi124', 0);
    expect(component.kpiChartData).toBeDefined();
  });

  it('should apply aggrefaration logic for non progress-bar chart ', () => {
    component.allKpiArray = [
      {
        kpiId: 'kpi124',
        trendValueList: [
          {
            filter: 'f1',
            value: [
              {
                filter1: 'Overall',
                data: [
                  {
                    label: 'Scope added',
                    value: 1,
                  },
                ],
              },
            ],
          },
        ],
      },
    ];
    component.updatedConfigGlobalData = [
      {
        kpiId: 'kpi125',
        kpiDetail: {
          chartType: 'GroupBarChart',
        },
      },
    ];
    component.kpiSelectedFilterObj['kpi124'] = {
      f1: ['value1'],
      f2: ['value2'],
    };

    spyOn(component, 'createTrendsData');
    spyOn(helperService, 'applyAggregationLogic');
    component.getChartDataForBacklog('kpi124', 0, 'sum');
    expect(component.kpiChartData['kpi124']).toBeUndefined();
  });

  it('should get chart data when have one filter', () => {
    component.allKpiArray = [
      {
        kpiId: 'kpi124',
        trendValueList: [
          {
            filter: 'f1',
            value: [
              {
                filter1: 'Overall',
                data: [
                  {
                    label: 'Scope added',
                    value: 1,
                  },
                ],
              },
            ],
          },
        ],
      },
    ];
    component.updatedConfigGlobalData = [
      {
        kpiId: 'kpi125',
        kpiDetail: {
          chartType: 'GroupBarChart',
        },
      },
    ];
    spyOn(component, 'getChartType').and.returnValue('progress-bar');
    component.kpiSelectedFilterObj['kpi124'] = { f1: ['f1'] };

    spyOn(component, 'createTrendsData');
    spyOn(component, 'applyAggregationLogicForProgressBar');
    component.getChartDataForBacklog('kpi124', 0, 'sum');
    expect(component.kpiChartData).toBeDefined();
  });

  it('should create kpi array when trendvalueList is object', () => {
    let kpi = [
      {
        kpiId: 'kpi141',
        trendValueList: {
          value: [
            {
              filter1: 'Overall',
              data: [
                {
                  label: 'Scope added',
                },
              ],
            },
          ],
        },
        filters: ['f1', 'f2'],
      },
    ];
    component.updatedConfigGlobalData = [
      {
        kpiId: 'kpi125',
        kpiDetail: {
          chartType: 'GroupBarChart',
        },
      },
    ];
    const fakeKPi = helperService.createKpiWiseId(kpi);
    spyOn(component, 'ifKpiExist').and.returnValue(1);
    component.createAllKpiArrayForBacklog(fakeKPi);
    expect(component.allKpiArray.length).toBeGreaterThan(0);
  });

  it('should createapiarry for radiobutton', () => {
    const data = {
      kpi141: {
        kpiId: 'kpi141',
        kpiName: 'Defect Count by Status',
        unit: 'Count',
        maxValue: '',
        chartType: 'graph',
        kpiInfo: {
          definition:
            'It shows the breakup of all defects tagged to a release based on Status. The breakup is shown in terms of count & percentage.',
        },
        id: '64b4ed7acba3c12de164732c',
        isDeleted: false,
        kpiCategory: 'Release',
        kpiUnit: 'Count',
        kanban: false,
        kpiSource: 'Jira',
        trendValueList: [
          {
            filter1: 'story',
            value: [
              {
                data: '1',
                value: [
                  {
                    value: 0,
                    drillDown: [],
                    subFilter: 'To Do',
                  },
                ],
                kpiGroup: 'Issue Count',
              },
            ],
          },
        ],
        groupId: 9,
      },
    };

    component.updatedConfigGlobalData = [
      {
        kpiId: 'kpi141',
        kpiName: 'Deployment Frequency',
        isEnabled: true,
        order: 23,
        kpiDetail: {
          kpiFilter: 'radiobutton',
          chartType: 'graph',
        },
        shown: true,
      },
    ];

    component.kpiSelectedFilterObj['kpi124'] = {
      filter1: ['story'],
    };
    component.kpiDropdowns = {
      kpi141: {
        options: ['story'],
      },
    };
    spyOn(component, 'ifKpiExist').and.returnValue(-1);
    spyOn(component, 'createTrendsData');
    component.createAllKpiArrayForBacklog(data);
    expect(component.kpiSelectedFilterObj).toBeDefined();
  });

  it('should createapiarry for dropdown', () => {
    const data = {
      kpi141: {
        kpiId: 'kpi141',
        kpiName: 'Defect Count by Status',
        unit: 'Count',
        maxValue: '',
        chartType: '',
        kpiInfo: {
          definition:
            'It shows the breakup of all defects tagged to a release based on Status. The breakup is shown in terms of count & percentage.',
        },
        id: '64b4ed7acba3c12de164732c',
        isDeleted: false,
        kpiCategory: 'Release',
        kpiUnit: 'Count',
        kanban: false,
        kpiSource: 'Jira',
        trendValueList: [
          {
            filter1: 'story',
            value: [
              {
                data: '1',
                value: [
                  {
                    value: 0,
                    drillDown: [],
                    subFilter: 'To Do',
                  },
                ],
                kpiGroup: 'Issue Count',
              },
            ],
          },
        ],
        groupId: 9,
      },
    };

    component.updatedConfigGlobalData = [
      {
        kpiId: 'kpi141',
        kpiName: 'Deployment Frequency',
        isEnabled: true,
        order: 23,
        kpiDetail: {
          kpiFilter: 'dropdown',
          chartType: 'graph',
        },
        shown: true,
      },
    ];

    component.kpiSelectedFilterObj['kpi124'] = {
      filter1: ['story'],
    };
    component.kpiDropdowns = {
      kpi141: {
        options: ['story'],
      },
    };
    spyOn(component, 'ifKpiExist').and.returnValue(-1);
    spyOn(component, 'createTrendsData');
    component.createAllKpiArrayForBacklog(data);
    expect(component.kpiSelectedFilterObj).toBeDefined();
  });

  it('should createapiarry for multi dropdown', () => {
    const data = {
      kpi141: {
        kpiId: 'kpi141',
        chartType: '',
        kpiInfo: {
          definition:
            'It shows the breakup of all defects tagged to a release based on Status. The breakup is shown in terms of count & percentage.',
        },
        filters: {
          filter1: {
            options: ['story'],
          },
        },
        id: '64b4ed7acba3c12de164732c',
        isDeleted: false,
        kpiCategory: 'Release',
        trendValueList: [
          {
            filter1: 'story',
            value: [
              {
                data: '1',
                kpiGroup: 'Issue Count',
              },
            ],
          },
        ],
        groupId: 9,
      },
    };

    component.updatedConfigGlobalData = [
      {
        kpiId: 'kpi141',
        kpiName: 'Deployment Frequency',
        isEnabled: true,
        order: 23,
        kpiDetail: {
          kpiFilter: 'multiDropdown',
          chartType: 'graph',
        },
        shown: true,
      },
    ];

    component.kpiSelectedFilterObj['kpi124'] = {
      filter1: ['story'],
    };
    component.kpiDropdowns = {
      kpi141: {
        options: ['story'],
      },
    };
    spyOn(component, 'ifKpiExist').and.returnValue(-1);
    spyOn(component, 'createTrendsData');
    component.createAllKpiArrayForBacklog(data);
    expect(component.kpiSelectedFilterObj).toBeDefined();
  });

  describe('ExecutiveV2 handleSelectedOptionOnBacklog', () => {
    let component;
    let helperServiceMock;
    let serviceMock;

    beforeEach(() => {
      fixture = TestBed.createComponent(ExecutiveV2Component);

      helperServiceMock = {
        createBackupOfFiltersSelection: jasmine.createSpy(
          'createBackupOfFiltersSelection',
        ),
      };

      serviceMock = {
        setKpiSubFilterObj: jasmine.createSpy('setKpiSubFilterObj'),
      };

      component = fixture.componentInstance;
    });

    describe('handleSelectedOptionOnBacklog', () => {
      xit('should update kpiSelectedFilterObj with single dropdown event', () => {
        const kpi = {
          kpiId: 'kpi-1',
          kpiDetail: {
            aggregationCriteria: 'sum',
          },
        };
        const event = { filter1: 'option1' };

        component.kpiSelectedFilterObj = { 'kpi-1': {} };

        component.handleSelectedOptionOnBacklog(event, kpi);

        expect(component.kpiSelectedFilterObj).toEqual({
          'kpi-1': { filter1: ['option1'] },
        });
      });

      xit('should update kpiSelectedFilterObj with multi dropdown event', () => {
        const kpi = {
          kpiId: 'kpi-1',
          kpiDetail: {
            aggregationCriteria: 'sum',
          },
        };
        component.kpiSelectedFilterObj['kpi-1'] = {
          filter1: 'option1',
        };

        const event = {
          filter1: ['option1', 'option2'],
          filter2: ['option3', 'option4'],
        };
        const selectedFilterBackup = {
          filter1: ['option5'],
          filter2: ['option6'],
        };

        component.kpiSelectedFilterObj = {};
        component.kpiSelectedFilterObj[kpi.kpiId] = selectedFilterBackup;

        component.handleSelectedOptionOnBacklog(event, kpi);

        expect(component.kpiSelectedFilterObj).toEqual({
          'kpi-1': {
            filter1: ['option1', 'option2'],
            filter2: ['option3', 'option4'],
          },
        });
      });

      it('should update kpiSelectedFilterObj with single dropdown event when selectedFilterBackup does not have filter2', () => {
        const kpi = {
          kpiId: 'kpi-1',
          kpiDetail: {
            aggregationCriteria: 'sum',
          },
          trendValueList: {
            value: [
              {
                filter1: 'Overall',
                data: [
                  {
                    label: 'Scope added',
                  },
                ],
              },
            ],
          },
        };

        component.allKpiArray = [kpi];
        component.configGlobalData = [kpi];
        component.updatedConfigGlobalData = [kpi];
        component.kpiSelectedFilterObj['kpi-1'] = {
          filter1: 'option1',
        };

        const event = { filter1: 'option1' };
        const selectedFilterBackup = { filter1: ['option2'] };
        component.selectedTab = 'backlog';
        component.kpiSelectedFilterObj = {};
        component.kpiSelectedFilterObj[kpi.kpiId] = selectedFilterBackup;

        component.handleSelectedOptionOnBacklog(event, kpi);

        expect(component.kpiSelectedFilterObj).toEqual({
          'kpi-1': { filter1: 'option1' },
        });
      });

      // it('should call getChartDataForBacklog, createBackupOfFiltersSelection, and setKpiSubFilterObj', () => {
      //   const kpi = {
      //     kpiId: 'kpi-1',
      //     kpiDetail: {
      //       aggregationCriteria: 'sum',
      //     },
      //   };

      //   component.kpiSelectedFilterObj['kpi-1'] = {
      //     filter1: 'option1',
      //   }

      //   const event = { filter1: 'option1' };

      //   spyOn(component, 'getChartDataForBacklog');

      //   component.handleSelectedOptionOnBacklog(event, kpi);

      //   expect(component.getChartDataForBacklog).toHaveBeenCalledWith('kpi-1', -1, 'sum');
      // });
    });
  });

  describe('checkSprint', () => {
    beforeEach(() => {
      component.kpiSelectedFilterObj = {
        kpi1: {
          filter1: ['filter1Value'],
          filter2: ['filter2Value'],
        },
        kpi2: {
          filter1: ['overall'],
          filter2: ['filter2Value'],
        },
        kpi3: {
          filter1: ['filter1Value'],
          filter2: ['overall'],
        },
        kpi4: {
          filter1: ['overall'],
          filter2: ['overall'],
        },
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

  describe('onTabSwitch', () => {
    it('should update the component properties', () => {
      const data = { selectedBoard: 'Speed' };
      service.onTabSwitch.next(data);

      expect(component.noFilterApplyData).toBe(false);
      expect(component.kpiLoader).toEqual(new Set());
      expect(component.kpiStatusCodeArr).toEqual({});
      expect(component.immediateLoader).toBe(true);
      expect(component.processedKPI11Value).toEqual({});
      expect(component.selectedBranchFilter).toBe('Select');
      expect(component.serviceObject).toEqual({});
      expect(component.kpiTrendObject).toEqual({});
    });
  });

  describe('onScrumKanbanSwitch', () => {
    it('should update the component properties', () => {
      const data = { selectedType: 'scrum' };
      service.onScrumKanbanSwitch.next(data);

      expect(component.noFilterApplyData).toBe(false);
      expect(component.kpiLoader).toEqual(new Set());
      expect(component.kpiStatusCodeArr).toEqual({});
      expect(component.immediateLoader).toBe(true);
      expect(component.processedKPI11Value).toEqual({});
      expect(component.selectedBranchFilter).toBe('Select');
      expect(component.serviceObject).toEqual({});
      expect(component.kpiTrendObject).toEqual({});
    });
  });

  describe('resetToDefaults', () => {
    it('should update the component properties', () => {
      component.resetToDefaults();

      expect(component.noFilterApplyData).toBe(false);
      expect(component.kpiLoader).toEqual(new Set());
      expect(component.kpiStatusCodeArr).toEqual({});
      expect(component.immediateLoader).toBe(true);
      expect(component.processedKPI11Value).toEqual({});
      expect(component.selectedBranchFilter).toBe('Select');
      expect(component.serviceObject).toEqual({});
      expect(component.kpiTrendObject).toEqual({});
    });
  });

  describe('setUpTabs', () => {
    it('should set up tabs for selectedTab "release"', () => {
      component.selectedTab = 'release';
      component.configGlobalData = [
        { kpiDetail: { kpiSubCategory: 'Tab1' }, shown: true, isEnabled: true },
        { kpiDetail: { kpiSubCategory: 'Tab2' }, shown: true, isEnabled: true },
        { kpiDetail: { kpiSubCategory: 'Tab3' }, shown: true, isEnabled: true },
      ];
      spyOn(component.service, 'getDashConfigData').and.returnValue({
        scrum: [{ boardName: 'Tab1' }, { boardName: 'Tab2' }],
        others: [{ boardName: 'Tab3' }],
      });

      component.setUpTabs();

      expect(component.tabsArr).toEqual(new Set(['Tab1', 'Tab2', 'Tab3']));
      expect(component.selectedKPITab).toBe('Tab1');
    });

    it('should set up tabs for selectedTab other than "release"', () => {
      component.selectedTab = 'other';
      component.configGlobalData = [
        { kpiDetail: { kpiSubCategory: 'Tab1' }, shown: true, isEnabled: true },
        { kpiDetail: { kpiSubCategory: 'Tab2' }, shown: true, isEnabled: true },
        { kpiDetail: { kpiSubCategory: 'Tab3' }, shown: true, isEnabled: true },
      ];
      spyOn(component.service, 'getDashConfigData').and.returnValue({});

      component.setUpTabs();

      expect(component.tabsArr).toEqual(new Set(['Tab1', 'Tab2', 'Tab3']));
      expect(component.selectedKPITab).toBe('Tab1');
    });
  });

  /* describe('setFilterValueIfAlreadyHaveBackup', () => {
    it('should set the filter value and call getDropdownArray for selectedTab other than "backlog"', () => {
      component.selectedTab = 'tab1';
      component.kpiSelectedFilterObj = {};
      const kpiId = 'kpi1';
      const refreshValue = 'refresh1';
      const initialValue = 'initial1';
      const filters = {};

      // spyOn(component.helperService, 'setFilterValueIfAlreadyHaveBackup').and.returnValue({});

      spyOn(component, 'getDropdownArray');

      // component.setFilterValueIfAlreadyHaveBackup(kpiId, refreshValue, initialValue, filters);

      expect(component.kpiSelectedFilterObj).toEqual({});
      expect(component.getDropdownArray).toHaveBeenCalledWith(kpiId);
    });

    it('should set the filter value and call getDropdownArrayForBacklog for selectedTab "backlog" with chartType', () => {
      component.selectedTab = 'backlog';
      component.kpiSelectedFilterObj = {};
      component.updatedConfigGlobalData = [
        { kpiId: 'kpi1', kpiDetail: { chartType: 'chart1' } }
      ];
      const kpiId = 'kpi1';
      const refreshValue = 'refresh1';
      const initialValue = 'initial1';
      const filters = {};

      // spyOn(component.helperService, 'setFilterValueIfAlreadyHaveBackup').and.returnValue({});

      spyOn(component, 'getDropdownArrayForBacklog');

      // component.setFilterValueIfAlreadyHaveBackup(kpiId, refreshValue, initialValue, filters);

      expect(component.kpiSelectedFilterObj).toEqual({});
      expect(component.getDropdownArrayForBacklog).toHaveBeenCalledWith(kpiId);
    });

    it('should set the filter value and call getDropdownArrayForCard for selectedTab "backlog" without chartType', () => {
      component.selectedTab = 'backlog';
      component.kpiSelectedFilterObj = {};
      component.updatedConfigGlobalData = [
        { kpiId: 'kpi1', kpiDetail: {} }
      ];
      const kpiId = 'kpi1';
      const refreshValue = 'refresh1';
      const initialValue = 'initial1';
      const filters = {};

      // spyOn(component.helperService, 'setFilterValueIfAlreadyHaveBackup').and.returnValue({});

      spyOn(component, 'getDropdownArrayForCard');

      // component.setFilterValueIfAlreadyHaveBackup(kpiId, refreshValue, initialValue, filters);

      expect(component.kpiSelectedFilterObj).toEqual({});
      expect(component.getDropdownArrayForCard).toHaveBeenCalledWith(kpiId);
    });
  }); */

  /* describe('handleSelectedOptionOnBacklog', () => {
    it('should handle selected option for single dropdown', () => {
      const event = { filter1: 'value1' };
      const kpi = { kpiId: 'kpi1', kpiDetail: {} };

      component.kpiSelectedFilterObj = {};
      spyOn(component, 'getChartDataForBacklog');
      spyOn(component.helperService, 'createBackupOfFiltersSelection');
      spyOn(component.service, 'setKpiSubFilterObj');

      component.handleSelectedOptionOnBacklog(event, kpi);

      expect(component.kpiSelectedFilterObj).toEqual({ kpi1: { filter1: 'value1' } });
      expect(component.getChartDataForBacklog).toHaveBeenCalledWith('kpi1', -1, undefined);
      expect(component.helperService.createBackupOfFiltersSelection).toHaveBeenCalledWith(component.kpiSelectedFilterObj, 'backlog', '');
      expect(component.service.setKpiSubFilterObj).toHaveBeenCalledWith(component.kpiSelectedFilterObj);
    });

    it('should handle selected option for multi dropdown', () => {
      const event = { filter1: ['value1', 'value2'], filter2: ['value3', 'value4'] };
      const kpi = { kpiId: 'kpi1', kpiDetail: {} };

      component.kpiSelectedFilterObj = { kpi1: { filter1: ['previousValue'], filter2: ['previousValue'] } };
      spyOn(component, 'getChartDataForBacklog');
      spyOn(component.helperService, 'createBackupOfFiltersSelection');
      spyOn(component.service, 'setKpiSubFilterObj');

      component.handleSelectedOptionOnBacklog(event, kpi);

      expect(component.kpiSelectedFilterObj).toEqual({
        kpi1: { filter1: ['value1', 'value2'], filter2: ['value3', 'value4'] }
      });
      expect(component.getChartDataForBacklog).toHaveBeenCalledWith('kpi1', -1, undefined);
      expect(component.helperService.createBackupOfFiltersSelection).toHaveBeenCalledWith(component.kpiSelectedFilterObj, 'backlog', '');
      expect(component.service.setKpiSubFilterObj).toHaveBeenCalledWith(component.kpiSelectedFilterObj);
    });

    it('should handle selected option for single dropdown with existing backup', () => {
      const event = { filter1: 'value1' };
      const kpi = { kpiId: 'kpi1', kpiDetail: {} };

      component.kpiSelectedFilterObj = { kpi1: { filter2: ['previousValue'] } };
      spyOn(component, 'getChartDataForBacklog');
      spyOn(component.helperService, 'createBackupOfFiltersSelection');
      spyOn(component.service, 'setKpiSubFilterObj');

      component.handleSelectedOptionOnBacklog(event, kpi);

      expect(component.kpiSelectedFilterObj).toEqual({ kpi1: { filter1: ['value1'], filter2: ['previousValue'] } });
      expect(component.getChartDataForBacklog).toHaveBeenCalledWith('kpi1', -1, undefined);
      expect(component.helperService.createBackupOfFiltersSelection).toHaveBeenCalledWith(component.kpiSelectedFilterObj, 'backlog', '');
      expect(component.service.setKpiSubFilterObj).toHaveBeenCalledWith(component.kpiSelectedFilterObj);
    });
  }); */

  describe('receiveSharedData', () => {
    xit('should set the hierarchyLevel and filterData when completeHierarchyData and dashConfigData are present', () => {
      component.service.setSelectedType('scrum');
      component.selectedtype = 'scrum';
      component.selectedtype = 'Type1';
      component.filterApplyData = {};
      component.globalConfig = {};
      component.configGlobalData = [];
      component.updatedConfigGlobalData = [];
      component.tooltip = {};
      component.additionalFiltersArr = {};
      component.allKpiArray = [];
      component.kpiChartData = {};
      component.chartColorList = {};
      component.kpiSelectedFilterObj = {};
      component.kpiDropdowns = {};
      component.kpiTrendsObj = {};
      component.kpiTableDataObj = {};
      component.kpiLoader = new Set();
      component.kpiStatusCodeArr = {};
      component.immediateLoader = false;
      component.noFilterApplyData = false;
      component.noOfFilterSelected = 0;
      component.noTabAccess = false;
      component.showCommentIcon = false;

      spyOn(localStorage, 'getItem').and.returnValue(
        '{"Type1": {"hierarchyLevelId": "level1"}}',
      );
      spyOn(JSON, 'parse').and.callThrough();

      component.receiveSharedData({
        dashConfigData: { Type1: { scrum: [{ boardName: 'Tab1', kpis: [] }] } },
        filterData: [{ labelName: 'label1', level: 'level1' }],
        filterApplyData: { level: 'level1' },
        configDetails: {},
        loading: false,
        makeAPICall: true,
        selectedTab: 'Tab1',
      });

      expect(component.hierarchyLevel).toEqual({ hierarchyLevelId: 'level1' });
      expect(localStorage.getItem).toHaveBeenCalledWith(
        'completeHierarchyData',
      );
      expect(JSON.parse).toHaveBeenCalledWith(
        '{"Type1": {"hierarchyLevelId": "level1"}}',
      );
      expect(component.filterData).toEqual([
        { labelName: 'label1', level: 'level1' },
      ]);
      expect(component.filterApplyData).toEqual({ level: 'level1' });
      expect(component.globalConfig).toEqual({
        Type1: { scrum: [{ boardName: 'Tab1', kpis: [] }] },
      });
      expect(component.configGlobalData).toEqual([]);
      expect(component.updatedConfigGlobalData).toEqual([]);
      expect(component.tooltip).toEqual({});
      expect(component.additionalFiltersArr).toEqual({});
      expect(component.allKpiArray).toEqual([]);
      expect(component.kpiChartData).toEqual({});
      expect(component.chartColorList).toEqual({});
      expect(component.kpiSelectedFilterObj).toEqual({});
      expect(component.kpiDropdowns).toEqual({});
      expect(component.kpiTrendsObj).toEqual({});
      expect(component.kpiTableDataObj).toEqual({});
      expect(component.kpiLoader).toEqual(new Set());
      expect(component.kpiStatusCodeArr).toEqual({});
      expect(component.immediateLoader).toBe(false);
      expect(component.noFilterApplyData).toBe(false);
      expect(component.noOfFilterSelected).toBe(1);
      expect(component.noTabAccess).toBe(false);
      expect(component.showCommentIcon).toBe(false);
    });

    it('should call the necessary group functions and set showCommentIcon to true', () => {
      component.service.setSelectedType('scrum');
      component.selectedtype = 'scrum';
      component.selectedTab = 'my-knowhow';
      component.filterData = [];
      component.filterApplyData = { selectedMap: { sprint: 'level1' } };
      component.configGlobalData = [{ boardName: 'Tab1', kpis: [] }];
      component.selectedtype = 'Type1';
      component.hierarchyLevel = [{ hierarchyLevelId: 'level1' }];

      spyOn(component, 'groupJiraKpi');
      spyOn(component, 'groupSonarKpi');
      spyOn(component, 'groupJenkinsKpi');
      spyOn(component, 'groupZypherKpi');
      spyOn(component, 'groupBitBucketKpi');
      spyOn(component, 'createKpiTableHeads');
      spyOn(component, 'getKpiCommentsCount');

      component.receiveSharedData({
        dashConfigData: {
          scrum: [
            {
              boardName: 'Tab1',
              boardSlug: 'Tab1',
              kpis: [{ kpiId: 'kpi1', shown: true }],
            },
          ],
        },
        filterData: [{ level: 'level1' }, { level: 'level2' }],
        filterApplyData: { level: 'level1', ids: ['Proj1'] },
        configDetails: {},
        loading: false,
        makeAPICall: true,
        selectedTab: 'Tab1',
        selectedType: 'scrum',
      });

      expect(component.groupJiraKpi).toHaveBeenCalled();
      expect(component.groupSonarKpi).toHaveBeenCalled();
      expect(component.groupJenkinsKpi).toHaveBeenCalled();
      expect(component.groupZypherKpi).toHaveBeenCalled();
      expect(component.groupBitBucketKpi).toHaveBeenCalled();
      expect(component.createKpiTableHeads).toHaveBeenCalled();
      // expect(component.getKpiCommentsCount).toHaveBeenCalled();
      expect(component.showCommentIcon).toBe(false);
    });
  });

  it('should call the necessary group functions for Kanban and set showCommentIcon to true', () => {
    component.service.setSelectedType('kanban');
    component.filterData = [];
    component.filterApplyData = { level: 'level1' };
    component.configGlobalData = [{ boardName: 'Tab1', kpis: [] }];
    component.selectedtype = 'kanban';
    component.selectedTab = 'my-knowhow';
    component.hierarchyLevel = [{ hierarchyLevelId: 'level1' }];

    spyOn(component, 'groupJiraKanbanKpi');
    spyOn(component, 'groupSonarKanbanKpi');
    spyOn(component, 'groupJenkinsKanbanKpi');
    spyOn(component, 'groupZypherKanbanKpi');
    spyOn(component, 'groupBitBucketKanbanKpi');
    spyOn(component, 'createKpiTableHeads');
    spyOn(component, 'getKpiCommentsCount');

    component.receiveSharedData({
      dashConfigData: {
        kanban: [
          {
            boardName: 'Tab1',
            boardSlug: 'Tab1',
            kpis: [{ kpiId: 'kpi1', shown: true }],
          },
        ],
      },
      filterData: [{ level: 'level1' }, { level: 'level2' }],
      filterApplyData: { level: 'level1', ids: ['Proj1'] },
      configDetails: {},
      loading: false,
      makeAPICall: true,
      selectedTab: 'Tab1',
      selectedType: 'kanban',
    });

    expect(component.groupJiraKanbanKpi).toHaveBeenCalled();
    expect(component.groupSonarKanbanKpi).toHaveBeenCalled();
    expect(component.groupJenkinsKanbanKpi).toHaveBeenCalled();
    expect(component.groupZypherKanbanKpi).toHaveBeenCalled();
    expect(component.groupBitBucketKanbanKpi).toHaveBeenCalled();
    // expect(component.createKpiTableHeads).toHaveBeenCalledWith('type1');
    // expect(component.getKpiCommentsCount).toHaveBeenCalled();
    expect(component.showCommentIcon).toBe(false);
  });

  it('should return true if data is present for kpiId kpi148 or kpi146 and kpiChartData has length', () => {
    component.kpiStatusCodeArr = { kpi148: '200' };
    component.kpiChartData = { kpi148: [{ value: [1, 2, 3] }] };

    expect(
      component.checkIfDataPresent({
        kpiId: 'kpi148',
        kpiDetail: { chartType: 'lineChart' },
      }),
    ).toBeTrue();
  });

  it('should return true if data is present for kpiId kpi139 or kpi127 and kpiChartData and kpiChartData[0].value have length', () => {
    component.kpiStatusCodeArr = { kpi139: '200' };
    component.kpiChartData = { kpi139: [{ value: [{ value: [1, 2, 3] }] }] };

    expect(
      component.checkIfDataPresent({
        kpiId: 'kpi139',
        kpiDetail: { chartType: 'lineChart' },
      }),
    ).toBeTrue();
  });

  it('should return true if data is present for kpiId kpi168, kpi70 or kpi153 and kpiChartData and kpiChartData[0].value have length greater than 0', () => {
    component.kpiStatusCodeArr = { kpi168: '200' };
    component.kpiChartData = { kpi168: [{ value: [{ data: 1 }] }] };

    expect(
      component.checkIfDataPresent({
        kpiId: 'kpi168',
        kpiDetail: { chartType: 'lineChart' },
      }),
    ).toBeTrue();
  });

  it('should return true if data is present for random KPI where kpiChartData[0].value have length greater than 0', () => {
    component.selectedTab = 'value';
    component.kpiStatusCodeArr = { kpi123: '200' };
    component.kpiChartData = { kpi123: [{ value: [{ data: 1 }] }] };

    expect(
      component.checkIfDataPresent({
        kpiId: 'kpi123',
        kpiDetail: { chartType: 'lineChart' },
      }),
    ).toBeTrue();
  });

  it('should return true if data is present for kpiId kpi148 or kpi146 and kpiChartData has length', () => {
    component.kpiStatusCodeArr = { kpi148: '200' };
    component.kpiChartData = { kpi148: [{ value: [1, 2, 3] }] };

    expect(
      component.checkIfDataPresent({
        kpiId: 'kpi148',
        kpiDetail: { chartType: 'lineChart' },
      }),
    ).toBeTrue();
  });

  it('should return true if data is present for kpiId kpi139 or kpi127 and kpiChartData and kpiChartData[0].value have length', () => {
    component.kpiStatusCodeArr = { kpi139: '200' };
    component.kpiChartData = { kpi139: [{ value: [{ value: [1, 2, 3] }] }] };

    expect(
      component.checkIfDataPresent({
        kpiId: 'kpi139',
        kpiDetail: { chartType: 'lineChart' },
      }),
    ).toBeTrue();
  });

  it('should return true if data is present for kpiId kpi168, kpi70 or kpi153 and kpiChartData and kpiChartData[0].value have length greater than 0', () => {
    component.kpiStatusCodeArr = { kpi168: '200' };
    component.kpiChartData = { kpi168: [{ value: [{ data: 1 }] }] };

    expect(
      component.checkIfDataPresent({
        kpiId: 'kpi168',
        kpiDetail: { chartType: 'lineChart' },
      }),
    ).toBeTrue();
  });

  it('should return true if data is present for kpiId kpi171 and kpiChartData and kpiChartData.value[0].data have length greater than 0', () => {
    component.kpiStatusCodeArr = { kpi171: '200' };
    component.kpiChartData = { kpi171: [{ data: [1, 2, 3] }] };

    expect(
      component.checkIfDataPresent({
        kpiId: 'kpi171',
        kpiDetail: { chartType: 'lineChart' },
      }),
    ).toBeTrue();
  });

  it('should return true if partial data is present for kpiId kpi139 and kpiData has length and filters length is 2', () => {
    component.allKpiArray = [
      {
        kpiId: 'kpi123',
        trendValueList: [
          { filter1: 'filter1', value: ['v1', 'v2', 'v3'] },
          { filter1: 'filter2', value: [] },
        ],
      },
    ];

    expect(
      component.checkIfPartialDataPresent({
        kpiId: 'kpi123',
        kpiDetail: { chartType: 'lineChart' },
      }),
    ).toBeTrue();
  });

  it('should return true if data is present for kpiId kpi148', () => {
    component.kpiStatusCodeArr = { kpi148: '200' };
    component.kpiChartData = { kpi148: [{ value: [1, 2, 3] }] };

    expect(
      component.checkIfDataPresent({
        kpiId: 'kpi148',
        kpiDetail: { chartType: 'lineChart' },
      }),
    ).toBeTrue();
  });

  it('should return true if data is present for kpiId kpi139', () => {
    component.kpiStatusCodeArr = { kpi139: '200' };
    component.kpiChartData = { kpi139: [{ value: [{ value: [1, 2, 3] }] }] };

    expect(
      component.checkIfDataPresent({
        kpiId: 'kpi139',
        kpiDetail: { chartType: 'lineChart' },
      }),
    ).toBeTrue();
  });

  it('should return true if data is present for kpiId kpi168', () => {
    component.kpiStatusCodeArr = { kpi168: '200' };
    component.kpiChartData = { kpi168: [{ value: [{ data: 1 }] }] };

    expect(
      component.checkIfDataPresent({
        kpiId: 'kpi168',
        kpiDetail: { chartType: 'lineChart' },
      }),
    ).toBeTrue();
  });

  it('should return true if data is present for kpiId kpi171', () => {
    component.kpiStatusCodeArr = { kpi171: '200' };
    component.kpiChartData = { kpi171: { value: [{ data: [1, 2, 3] }] } };

    expect(
      component.checkIfDataPresent({
        kpiId: 'kpi171',
        kpiDetail: { chartType: 'lineChart' },
      }),
    ).toBeFalse();
  });

  it('should return true if partial data is present for kpiId kpi171', () => {
    const kpiData = {
      value: [
        { filter1: 'filter1', data: [1, 2, 3] },
        { filter1: 'filter2', data: [] },
      ],
    };
    const filters = ['filter1', 'filter2'];

    expect(component.checkIfPartialDataForKpi171(kpiData)).toEqual(true);
  });

  it('should return false if partial data is not present for kpiId kpi171', () => {
    const kpiData = {
      value: [
        { filter1: 'filter1', data: [] },
        { filter1: 'filter2', data: [] },
      ],
    };
    const filters = ['filter1', 'filter2'];

    expect(component.checkIfPartialDataForKpi171(kpiData)).toBeFalsy();
  });

  describe('ExecutiveV2Component.setGlobalConfigData() setGlobalConfigData method', () => {
    describe('Happy Path', () => {
      xit('should set configGlobalData correctly when kanban is activated', () => {
        // Test description: Ensure that configGlobalData is set correctly when kanban is activated.
        component.selectedtype = 'kanban';
        component.selectedTab = 'test-board';

        const globalConfig = {
          kanban: [
            {
              boardSlug: 'test-board',
              boardName: 'test-board',
              kpis: ['kpi1', 'kpi2'],
            },
            {
              boardSlug: 'other-board',
              boardName: 'other-board',
              kpis: ['kpi3'],
            },
          ],
          scrum: [],
          others: [],
        };

        component.setGlobalConfigData(globalConfig as any);

        expect(component.configGlobalData).toEqual(['kpi1', 'kpi2']);
      });

      xit('should set configGlobalData correctly when scrum is activated', () => {
        // Test description: Ensure that configGlobalData is set correctly when scrum is activated.
        component.selectedTab = 'test-board';

        const globalConfig = {
          kanban: [],
          scrum: [
            {
              boardSlug: 'test-board',
              boardName: 'test-board',
              kpis: ['kpi1', 'kpi2'],
            },
            {
              boardSlug: 'other-board',
              boardName: 'other-board',
              kpis: ['kpi3'],
            },
          ],
          others: [],
        };

        component.setGlobalConfigData(globalConfig as any);

        expect(component.configGlobalData).toEqual(['kpi1', 'kpi2']);
      });
    });

    describe('Edge Cases', () => {
      it('should handle case where no matching board is found', () => {
        // Test description: Ensure that configGlobalData is set to undefined when no matching board is found.
        component.selectedTab = 'non-existent-board';

        const globalConfig = {
          kanban: [
            {
              boardSlug: 'test-board',
              boardName: 'test-board',
              kpis: ['kpi1', 'kpi2'],
            },
          ],
          scrum: [],
          others: [],
        };

        component.setGlobalConfigData(globalConfig as any);

        expect(component.configGlobalData).toBeUndefined();
      });

      it('should fallback to "others" when no kanban or scrum match is found', () => {
        // Test description: Ensure that configGlobalData falls back to "others" when no kanban or scrum match is found.
        component.selectedTab = 'other-board';

        const globalConfig = {
          kanban: [],
          scrum: [],
          others: [
            {
              boardSlug: 'other-board',
              boardName: 'other-board',
              kpis: ['kpi1', 'kpi2'],
            },
          ],
        };

        component.setGlobalConfigData(globalConfig as any);

        expect(component.configGlobalData).toEqual(['kpi1', 'kpi2']);
      });

      xit('should filter updatedConfigGlobalData to only shown items', () => {
        // Test description: Ensure that updatedConfigGlobalData only includes items that are shown.
        component.selectedtype = 'kanban';
        component.selectedTab = 'test-board';

        const globalConfig = {
          kanban: [
            {
              boardSlug: 'test-board',
              boardName: 'test-board',
              kpis: [
                { shown: true, name: 'kpi1' },
                { shown: false, name: 'kpi2' },
              ],
            },
          ],
          scrum: [],
          others: [],
        };

        component.setGlobalConfigData(globalConfig as any);

        expect(component.updatedConfigGlobalData).toEqual([
          { shown: true, name: 'kpi1' },
        ]);
      });
    });
  });

  it('should generate excel on click of export button', () => {
    component.modalDetails = {
      header: 'Work Remaining / Issue Count/Original Estimate',
      tableHeadings: ['Issue Id', 'Issue Description', 'Issue Status'],
      tableValues: [
        {
          'Issue Id': 'DTS-22685',
          'Issue URL': 'http://testabc.com/jira/browse/DTS-22685',
          'Issue Description':
            'Iteration KPI | Popup window is not wide enough to read details  ',
          'Issue Status': 'Open',
        },
      ],
    };

    const spyGenerateExcel = spyOn(excelService, 'generateExcel');
    component.generateExcel();
    expect(spyGenerateExcel).toHaveBeenCalled();
  });

  it('should return the yaxisLabel from trendData when conditions are met', () => {
    component.allKpiArray = [
      {
        kpiId: 1,
        trendValueList: [
          {
            filter: 'test-filter',
            yaxisLabel: 'Expected Label',
          },
        ],
      },
    ];
    component.kpiSelectedFilterObj = {
      1: ['test-filter'],
    };

    const result = component.checkYAxis({ kpiId: 1 });
    expect(result).toBe('Expected Label');
  });

  it('should return the kpiDetail.yaxisLabel when trendData is not found', () => {
    component.allKpiArray = [
      {
        kpiId: 2,
        trendValueList: [],
      },
    ];
    component.kpiSelectedFilterObj = {
      2: ['different-filter'],
    };

    const result = component.checkYAxis({
      kpiId: 2,
      kpiDetail: { yaxisLabel: 'Fallback Label' },
    });
    expect(result).toBe('Fallback Label');
  });

  it('should return undefined if kpiDataResponce is not found', () => {
    component.allKpiArray = [];
    component.kpiSelectedFilterObj = {};

    const result = component.checkYAxis({ kpiId: 3 });
    expect(result).toBeUndefined();
  });

  it('should return the yaxisLabel when selectedFilterVal.filter1 is used', () => {
    component.allKpiArray = [
      {
        kpiId: 4,
        trendValueList: [
          {
            filter1: 'filter1-value',
            yaxisLabel: 'Label from Filter1',
          },
        ],
      },
    ];
    component.kpiSelectedFilterObj = {
      4: { filter1: ['filter1-value'] },
    };

    const result = component.checkYAxis({ kpiId: 4 });
    expect(result).toBe('Label from Filter1');
  });

  describe('ExecutiveV2Component.checkYAxis() checkYAxis method', () => {
    describe('Happy Path', () => {
      it('should return the yaxisLabel from trendData when all conditions are met', () => {
        // Arrange
        const kpi = { kpiId: 1, kpiDetail: { yaxisLabel: 'Default Label' } };
        component.allKpiArray = [
          {
            kpiId: 1,
            trendValueList: [{ filter: 'filter1', yaxisLabel: 'Trend Label' }],
          },
        ];
        component.kpiSelectedFilterObj = { 1: { filter1: ['filter1'] } };

        // Act
        const result = component.checkYAxis(kpi);

        // Assert
        expect(result).toBe('Trend Label');
      });
    });

    describe('Edge Cases', () => {
      it('should return the default yaxisLabel when no trendData is found', () => {
        // Arrange
        const kpi = { kpiId: 1, kpiDetail: { yaxisLabel: 'Default Label' } };
        component.allKpiArray = [
          {
            kpiId: 1,
            trendValueList: [],
          },
        ];
        component.kpiSelectedFilterObj = { 1: { filter1: ['filter1'] } };

        // Act
        const result = component.checkYAxis(kpi);

        // Assert
        expect(result).toBe('Default Label');
      });

      it('should return the default yaxisLabel when kpiDataResponce is undefined', () => {
        // Arrange
        const kpi = { kpiId: 2, kpiDetail: { yaxisLabel: 'Default Label' } };
        component.allKpiArray = [
          {
            kpiId: 1,
            trendValueList: [{ filter: 'filter1', yaxisLabel: 'Trend Label' }],
          },
        ];
        component.kpiSelectedFilterObj = { 2: { filter1: ['filter1'] } };

        // Act
        const result = component.checkYAxis(kpi);

        // Assert
        expect(result).toBe('Default Label');
      });

      it('should handle missing filter1 gracefully', () => {
        // Arrange
        const kpi = { kpiId: 1, kpiDetail: { yaxisLabel: 'Default Label' } };
        component.allKpiArray = [
          {
            kpiId: 1,
            trendValueList: [{ filter: 'filter1', yaxisLabel: 'Trend Label' }],
          },
        ];
        component.kpiSelectedFilterObj = { 1: ['filter1'] };

        // Act
        const result = component.checkYAxis(kpi);

        // Assert
        expect(result).toBe('Trend Label');
      });
    });
  });

  /*describe('ExecutiveV2Component.showExecutionDate() showExecutionDate method', () => {
    // Happy path tests
    it('should return true when executionSuccess is true and executionEndedAt is not 0', () => {
      spyOn(component as any, 'findTraceLogForTool').and.returnValue({
        executionEndedAt: 1,
        executionSuccess: true,
      } as any);

      const result = component.showExecutionDate('processorName');
      expect(result).toBe(true);
    });

    // Edge case tests
    it('should return false when traceLog is undefined', () => {
      spyOn(component as any, 'findTraceLogForTool')
        .and.returnValue(undefined as any);

      const result = component.showExecutionDate('processorName');
      expect(result).toBe(false);
    });

    it('should return false when traceLog is null', () => {
      spyOn(component as any, 'findTraceLogForTool')
        .and.returnValue(null as any);

      const result = component.showExecutionDate('processorName');
      expect(result).toBe(false);
    });

    it('should return false when executionEndedAt is 0', () => {
      spyOn(component as any, 'findTraceLogForTool').and.returnValue({
        executionEndedAt: 0,
        executionSuccess: true,
      } as any);

      const result = component.showExecutionDate('processorName');
      expect(result).toBe(false);
    });

    it('should return false when executionSuccess is false', () => {
      spyOn(component as any, 'findTraceLogForTool').and.returnValue({
        executionEndedAt: 1,
        executionSuccess: false,
      } as any);

      const result = component.showExecutionDate('processorName');
      expect(result).toBe(false);
    });
  });*/

  describe('ExecutiveV2Component.findTraceLogForTool() findTraceLogForTool method', () => {
    describe('Happy Path', () => {
      it('should return the correct processor log when processorName is found', () => {
        // Arrange
        const processorName = 'processor1';
        const mockLogDetails = [
          { processorName: 'processor1' },
          { processorName: 'processor2' },
        ];
        spyOn(service, 'getProcessorLogDetails').and.returnValue(
          mockLogDetails as any,
        );

        // Act
        const result = component.findTraceLogForTool(processorName);

        // Assert
        expect(result).toEqual({ processorName: 'processor1' });
      });

      it('should handle processorName with slashes correctly', () => {
        // Arrange
        const processorName = 'processor1/processor2';
        const mockLogDetails = [
          { processorName: 'processor1' },
          { processorName: 'processor2' },
        ];
        spyOn(service, 'getProcessorLogDetails').and.returnValue(
          mockLogDetails as any,
        );

        // Act
        const result = component.findTraceLogForTool(processorName);

        // Assert
        expect(result).toEqual({ processorName: 'processor1' });
      });
    });

    describe('Edge Cases', () => {
      it('should return undefined when processorName is not found', () => {
        // Arrange
        const processorName = 'nonexistentProcessor';
        const mockLogDetails = [
          { processorName: 'processor1' },
          { processorName: 'processor2' },
        ];
        spyOn(service, 'getProcessorLogDetails').and.returnValue(
          mockLogDetails as any,
        );

        // Act
        const result = component.findTraceLogForTool(processorName);

        // Assert
        expect(result).toBeUndefined();
      });

      it('should handle empty processorName gracefully', () => {
        // Arrange
        const processorName = '';
        const mockLogDetails = [
          { processorName: 'processor1' },
          { processorName: 'processor2' },
        ];
        spyOn(service, 'getProcessorLogDetails').and.returnValue(
          mockLogDetails as any,
        );

        // Act
        const result = component.findTraceLogForTool(processorName);

        // Assert
        expect(result).toBeUndefined();
      });
    });
  });

  describe('ExecutiveV2Component.checkIfZeroData() checkIfZeroData method', () => {
    describe('Happy Path', () => {
      it('should return true when data is present and trends length is 1', () => {
        // Arrange
        const kpi = {
          kpiId: 'kpi148',
          kpiDetail: { combinedKpiSource: '', kpiSource: '' },
        };
        component.kpiChartData = { kpi148: [{ value: [{ data: '10' }] }] };
        spyOn(component, 'checkIfDataPresent' as any).and.returnValue(true);
        spyOn(service, 'getSelectedTrends').and.returnValue([{}]);

        // Act
        const result = component.checkIfZeroData(kpi as any);

        // Assert
        expect(result).toBe(true);
      });

      it('should return true when dataValue is greater than 0', () => {
        // Arrange
        const kpi = {
          kpiId: 'kpi139',
          kpiDetail: { combinedKpiSource: '', kpiSource: '' },
        };
        component.kpiChartData = { kpi139: [{ value: [{ data: '10' }] }] };
        spyOn(component, 'checkIfDataPresent' as any).and.returnValue(true);
        spyOn(service, 'getSelectedTrends').and.returnValue([{}]);

        // Act
        const result = component.checkIfZeroData(kpi as any);

        // Assert
        expect(result).toBe(true);
      });
    });

    describe('Edge Cases', () => {
      it('should return false when no data is present', () => {
        // Arrange
        const kpi = {
          kpiId: 'kpi171',
          kpiDetail: { combinedKpiSource: '', kpiSource: '' },
        };
        component.kpiChartData = { kpi171: [] };
        spyOn(component, 'checkIfDataPresent' as any).and.returnValue(false);

        // Act
        const result = component.checkIfZeroData(kpi as any);

        // Assert
        expect(result).toBeFalsy();
      });

      xit('should set kpiStatusCodeArr to "202" when processorLastRunSuccess is false', () => {
        // Arrange
        const kpi = {
          kpiId: 'kpi139',
          kpiDetail: { combinedKpiSource: '', kpiSource: '' },
        };
        component.kpiChartData = { kpi139: [{ value: [{ data: '0' }] }] };
        spyOn(component, 'checkIfDataPresent' as any).and.returnValue(true);
        spyOn(component, 'showExecutionDate' as any).and.returnValue(false);
        spyOn(service, 'getSelectedTrends').and.returnValue([
          { nodeId: '123', labelName: 'project' },
        ]);

        // Act
        component.checkIfZeroData(kpi as any);

        // Assert
        expect(component.kpiStatusCodeArr['kpi139']).toBe('202');
      });
    });
  });

  describe('ExecutiveV2Component.calcBusinessDays() calcBusinessDays method', () => {
    describe('Happy paths', () => {
      it('should return the correct number of business days between two weekdays', () => {
        const startDate = new Date('2023-10-02'); // Monday
        const endDate = new Date('2023-10-06'); // Friday
        const result = component.calcBusinessDays(startDate, endDate);
        expect(result).toBe(5);
      });

      it('should return 1 when the start and end dates are the same weekday', () => {
        const date = new Date('2023-10-04'); // Wednesday
        const result = component.calcBusinessDays(date, date);
        expect(result).toBe(1);
      });

      it('should correctly calculate business days spanning a weekend', () => {
        const startDate = new Date('2023-10-06'); // Friday
        const endDate = new Date('2023-10-10'); // Tuesday
        const result = component.calcBusinessDays(startDate, endDate);
        expect(result).toBe(3);
      });
    });

    describe('Edge cases', () => {
      it('should return 0 if the end date is before the start date', () => {
        const startDate = new Date('2023-10-10'); // Tuesday
        const endDate = new Date('2023-10-06'); // Friday
        const result = component.calcBusinessDays(startDate, endDate);
        expect(result).toBe(0);
      });

      it('should handle dates that fall on a weekend', () => {
        const startDate = new Date('2023-10-07'); // Saturday
        const endDate = new Date('2023-10-08'); // Sunday
        const result = component.calcBusinessDays(startDate, endDate);
        expect(result).toBe(0);
      });

      it('should handle a start date on a weekday and an end date on a weekend', () => {
        const startDate = new Date('2023-10-06'); // Friday
        const endDate = new Date('2023-10-08'); // Sunday
        const result = component.calcBusinessDays(startDate, endDate);
        expect(result).toBe(1);
      });
    });
  });

  describe('ExecutiveV2Component.getkpiwidth() getkpiwidth method', () => {
    describe('Happy Paths', () => {
      it('should return "p-col-12" for kpiwidth 100', () => {
        const result = component.getkpiwidth(100);
        expect(result).toBe('p-col-12');
      });

      it('should return "p-col-6" for kpiwidth 50', () => {
        const result = component.getkpiwidth(50);
        expect(result).toBe('p-col-6');
      });

      it('should return "p-col-8" for kpiwidth 66', () => {
        const result = component.getkpiwidth(66);
        expect(result).toBe('p-col-8');
      });

      it('should return "p-col-4" for kpiwidth 33', () => {
        const result = component.getkpiwidth(33);
        expect(result).toBe('p-col-4');
      });
    });

    // Edge case tests
    describe('Edge Cases', () => {
      it('should return "p-col-6" for kpiwidth 0', () => {
        const result = component.getkpiwidth(0);
        expect(result).toBe('p-col-6');
      });

      it('should return "p-col-6" for negative kpiwidth', () => {
        const result = component.getkpiwidth(-10);
        expect(result).toBe('p-col-6');
      });

      it('should return "p-col-6" for kpiwidth greater than 100', () => {
        const result = component.getkpiwidth(150);
        expect(result).toBe('p-col-6');
      });

      it('should return "p-col-6" for non-matching kpiwidth', () => {
        const result = component.getkpiwidth(75);
        expect(result).toBe('p-col-6');
      });
    });
  });

  it('should set the default filter for a single option when filterPropArr includes "filter" and filterType is not "multiselectdropdown"', () => {
    component.kpiDropdowns = {
      kpi1: [{ options: ['option1', 'option2'] }],
      kpi2: [{ options: ['option1'] }, { options: ['option2'] }],
    };
    component.kpiSelectedFilterObj = {};
    component.getDefaultKPIFiltersForRelease(
      'kpi1',
      ['filter'],
      'singleselect',
    );
    expect(component.kpiSelectedFilterObj['kpi1']).toEqual(['option1']);
  });

  it('should set the default filter for a single option when filterPropArr includes "filter" and filterType is undefined', () => {
    component.kpiDropdowns = {
      kpi1: [{ options: ['option1', 'option2'] }],
      kpi2: [{ options: ['option1'] }, { options: ['option2'] }],
    };
    component.kpiSelectedFilterObj = {};
    component.getDefaultKPIFiltersForRelease('kpi1', ['filter'], undefined);
    expect(component.kpiSelectedFilterObj['kpi1']).toEqual(['option1']);
  });

  it('should set the default filter for multiple options when filterPropArr includes "filter1" and "filter2"', () => {
    component.kpiDropdowns = {
      kpi1: [{ options: ['option1', 'option2'] }],
      kpi2: [{ options: ['option1'] }, { options: ['option2'] }],
    };
    component.kpiSelectedFilterObj = {};
    component.getDefaultKPIFiltersForRelease(
      'kpi2',
      ['filter1', 'filter2'],
      undefined,
    );
    expect(component.kpiSelectedFilterObj['kpi2']).toEqual({
      filter1: ['option1'],
      filter2: ['option2'],
    });
  });

  // Edge Case Tests
  it('should set an empty array when filterPropArr includes "filter" and filterType is "multiselectdropdown"', () => {
    component.kpiDropdowns = {
      kpi1: [{ options: ['option1', 'option2'] }],
      kpi2: [{ options: ['option1'] }, { options: ['option2'] }],
    };
    component.kpiSelectedFilterObj = {};
    component.getDefaultKPIFiltersForRelease(
      'kpi1',
      ['filter'],
      'multiselectdropdown',
    );
    expect(component.kpiSelectedFilterObj['kpi1']).toEqual([]);
  });

  it('should handle empty kpiDropdowns gracefully', () => {
    component.kpiDropdowns = {};
    component.getDefaultKPIFiltersForRelease('kpi1', ['filter'], undefined);
    expect(component.kpiSelectedFilterObj['kpi1']).toBeUndefined();
  });

  it('should handle missing options in kpiDropdowns gracefully', () => {
    component.kpiDropdowns = { kpi1: [{}] };
    component.getDefaultKPIFiltersForRelease('kpi1', ['filter'], undefined);
    expect(component.kpiSelectedFilterObj['kpi1']).toBeUndefined();
  });

  describe('ExecutiveV2Component.ngOnInit() ngOnInit method', () => {
    describe('Happy paths', () => {
      it('should subscribe to globalDashConfigData and process KPI config data', (done) => {
        // Arrange
        component.selectedtype = 'scrum';
        const globalConfig = {
          scrum: [{ boardName: 'Tab1', boardSlug: 'Tab1', kpis: [] }],
          kanban: [],
          others: [],
          enabledKPIs: ['kpi1', 'kpi2'],
        };
        spyOn(component, 'processKpiConfigData' as any);
        spyOn(component, 'setUpTabs' as any);
        spyOn(component, 'reloadKPI' as any);

        // Act
        component.ngOnInit();
        service.globalDashConfigData.next(globalConfig);

        // Assert
        setTimeout(() => {
          expect(component.processKpiConfigData).toHaveBeenCalled();
          expect(component.setUpTabs).toHaveBeenCalled();
          expect(component.reloadKPI).toHaveBeenCalledWith('kpi1');
          expect(component.reloadKPI).toHaveBeenCalledWith('kpi2');
          done();
        }, 500);
      });
    });

    describe('Edge cases', () => {
      it('should handle empty selectedTrends from localStorage', () => {
        // Arrange
        spyOn(localStorage, 'getItem').and.returnValue(null);

        // Act
        component.ngOnInit();

        // Assert
        expect(component.selectedTrend).toEqual([]);
      });
    });
  });

  describe('ExecutiveV2Component.postJiraKPIForIteration() postJiraKPIForIteration method', () => {
    describe('Happy Paths', () => {
      it('should process data correctly when valid data is returned', () => {
        const postData = {
          kpiList: [
            {
              id: '65793ddb127be336160bc0fe',
              kpiId: 'kpi121',
              kpiName: 'Defect Count by Status',
            },
          ],
        };
        const source = 'jira';
        const getData = [{ kpi121: { trendValueList: { value: 10 } } }];
        spyOn(httpService, 'postKpiNonTrend').and.returnValue(of(getData));
        spyOn(helperService, 'createKpiWiseId').and.returnValue({
          kpi121: getData[0],
        } as any);

        component.postJiraKPIForIteration(postData, source);

        expect(httpService.postKpiNonTrend).toHaveBeenCalledWith(
          postData,
          source,
        );
        expect(component.iterationKPIData).toEqual({ kpi121: getData[0] });
      });

      it('should update iterationConfigData when kpi121 is present', () => {
        const postData = { some: 'data' };
        const source = 'source';
        const getData = [{ kpi121: { trendValueList: { value: 10 } } }];
        spyOn(httpService, 'postKpiNonTrend').and.returnValue(
          of(getData) as any,
        );
        spyOn(helperService, 'createKpiWiseId').and.returnValue({
          kpi121: getData[0],
        } as any);
        spyOn(component.service.iterationConfigData, 'next');
        component.postJiraKPIForIteration(postData, source);

        expect(component.service.iterationConfigData.next).toHaveBeenCalledWith(
          {
            daysLeft: component.timeRemaining,
            capacity: { value: { value: 0 } },
          },
        );
      });
    });

    describe('Edge Cases', () => {
      it('should handle null data gracefully', () => {
        const postData = { some: 'data' };
        const source = 'source';
        spyOn(httpService, 'postKpiNonTrend').and.returnValue(of(null) as any);
        spyOn(component, 'handleKPIError');
        component.postJiraKPIForIteration(postData, source);

        expect(component.handleKPIError).toHaveBeenCalledWith(postData);
      });

      xit('should handle error response correctly', () => {
        const postData = { some: 'data' };
        const source = 'source';
        const getData = [{ error: true }];
        spyOn(httpService, 'postKpiNonTrend').and.returnValue(
          of(getData) as any,
        );
        spyOn(component, 'handleKPIError');
        component.postJiraKPIForIteration(postData, source);

        expect(component.handleKPIError).toHaveBeenCalledWith(postData);
      });

      it('should handle HTTP error correctly', () => {
        const postData = { some: 'data' };
        const source = 'source';
        spyOn(httpService, 'postKpiNonTrend').and.returnValue(
          throwError(() => new Error('HTTP error')) as any,
        );
        spyOn(component, 'handleKPIError');
        component.postJiraKPIForIteration(postData, source);

        expect(component.handleKPIError).toHaveBeenCalledWith(postData);
      });
    });
  });
});
