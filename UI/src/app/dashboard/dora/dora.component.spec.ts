import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';

import { DoraComponent } from './dora.component';
import { SharedService } from '../../services/shared.service';
import { HttpService } from 'src/app/services/http.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { APP_CONFIG, AppConfig } from 'src/app/services/app.config';
import { HelperService } from 'src/app/services/helper.service';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { DatePipe } from '@angular/common';
import { of } from 'rxjs';
import { ExportExcelComponent } from 'src/app/component/export-excel/export-excel.component';
import { MessageService } from 'primeng/api';

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
  const updatedConfigGlobalData = [
    {
      "kpiId": "kpi118",
      "kpiName": "Deployment Frequency",
      "isEnabled": true,
      "order": 1,
      "kpiDetail": {
        "id": "64b4ed7acba3c12de1647304",
        "kpiId": "kpi118",
        "kpiName": "Deployment Frequency",
        "isDeleted": "False",
        "defaultOrder": 25,
        "kpiCategory": "Dora",
        "kpiUnit": "Number",
        "chartType": "line",
        "showTrend": true,
        "isPositiveTrend": true,
        "calculateMaturity": true,
        "hideOverallFilter": false,
        "kpiSource": "Jenkins",
        "maxValue": "100",
        "thresholdValue": 0,
        "kanban": false,
        "groupId": 1,
        "kpiInfo": {
          "definition": "Measures how often code is deployed to production in a period",
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27131959/Scrum+VALUE+KPIs#Deployment-Frequency"
              }
            }
          ]
        },
        "kpiFilter": "multiSelectDropDown",
        "aggregationCriteria": "sum",
        "maturityRange": [
          "-1",
          "1-2",
          "2-5",
          "5-10",
          "10-"
        ],
        "trendCalculative": false,
        "xaxisLabel": "Months",
        "yaxisLabel": "Count",
        "additionalFilterSupport": false
      },
      "shown": true
    },
    {
      "kpiId": "kpi116",
      "kpiName": "Change Failure Rate",
      "isEnabled": true,
      "order": 2,
      "kpiDetail": {
        "id": "64b4ed7acba3c12de16472fa",
        "kpiId": "kpi116",
        "kpiName": "Change Failure Rate",
        "isDeleted": "False",
        "defaultOrder": 15,
        "kpiCategory": "Dora",
        "kpiUnit": "%",
        "chartType": "line",
        "showTrend": true,
        "isPositiveTrend": false,
        "calculateMaturity": true,
        "hideOverallFilter": true,
        "kpiSource": "Jenkins",
        "maxValue": "100",
        "thresholdValue": 0,
        "kanban": false,
        "groupId": 1,
        "kpiInfo": {
          "definition": "Measures the proportion of builds that have failed over a given period of time",
          "formula": [
            {
              "lhs": "Change Failure Rate",
              "operator": "division",
              "operands": [
                "Total number of failed Builds",
                "Total number of Builds"
              ]
            }
          ],
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#Change-Failure-Rate"
              }
            }
          ]
        },
        "kpiFilter": "dropDown",
        "aggregationCriteria": "average",
        "maturityRange": [
          "-50",
          "50-30",
          "30-20",
          "20-10",
          "10-"
        ],
        "trendCalculative": false,
        "xaxisLabel": "Weeks",
        "yaxisLabel": "Percentage",
        "additionalFilterSupport": false
      },
      "shown": true
    }
  ]
  const globalData = require('../../../test/resource/fakeGlobalConfigData.json');
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
  const kpi116ChartData = [
    {
      "kpiId": "kpi116",
      "kpiName": "Change Failure Rate",
      "unit": "%",
      "maxValue": "100",
      "chartType": "",
      "kpiInfo": {
        "definition": "Measures the proportion of builds that have failed over a given period of time",
        "formula": [
          {
            "lhs": "Change Failure Rate",
            "operator": "division",
            "operands": [
              "Total number of failed Builds",
              "Total number of Builds"
            ]
          }
        ],
        "details": [
          {
            "type": "link",
            "kpiLinkDetail": {
              "text": "Detailed Information at",
              "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#Change-Failure-Rate"
            }
          }
        ]
      },
      "id": "6570635518989f8fe1280eaf",
      "isDeleted": "False",
      "kpiCategory": "Dora",
      "kpiUnit": "%",
      "kanban": false,
      "kpiSource": "Jenkins",
      "thresholdValue": 50,
      "trendValueList": [
        {
          "filter": "Overall",
          "value": [
            {
              "data": "PSknowHOW ",
              "value": [
                {
                  "data": "71.43",
                  "sSprintID": "30/10 - 05/11",
                  "sSprintName": "30/10 - 05/11",
                  "value": 71.43,
                  "hoverValue": {
                    "Total number of Changes": 7,
                    "Failed Changes": 5
                  },
                  "date": "30/10 - 05/11",
                  "sprintIds": [
                    "30/10 - 05/11"
                  ],
                  "sprintNames": [
                    "30/10 - 05/11"
                  ],
                  "projectNames": [
                    "PSknowHOW "
                  ],
                  "sprojectName": "PSknowHOW "
                },
                {
                  "data": "0.0",
                  "sSprintID": "06/11 - 12/11",
                  "sSprintName": "06/11 - 12/11",
                  "value": 0,
                  "hoverValue": {
                    "Total number of Changes": 1,
                    "Failed Changes": 0
                  },
                  "date": "06/11 - 12/11",
                  "sprintIds": [
                    "06/11 - 12/11"
                  ],
                  "sprintNames": [
                    "06/11 - 12/11"
                  ],
                  "projectNames": [
                    "PSknowHOW "
                  ],
                  "sprojectName": "PSknowHOW "
                },
                {
                  "data": "0.0",
                  "sSprintID": "13/11 - 19/11",
                  "sSprintName": "13/11 - 19/11",
                  "value": 0,
                  "hoverValue": {
                    "Total number of Changes": 0,
                    "Failed Changes": 0
                  },
                  "date": "13/11 - 19/11",
                  "sprintIds": [
                    "13/11 - 19/11"
                  ],
                  "sprintNames": [
                    "13/11 - 19/11"
                  ],
                  "projectNames": [
                    "PSknowHOW "
                  ],
                  "sprojectName": "PSknowHOW "
                },
                {
                  "data": "0.0",
                  "sSprintID": "20/11 - 26/11",
                  "sSprintName": "20/11 - 26/11",
                  "value": 0,
                  "hoverValue": {
                    "Total number of Changes": 0,
                    "Failed Changes": 0
                  },
                  "date": "20/11 - 26/11",
                  "sprintIds": [
                    "20/11 - 26/11"
                  ],
                  "sprintNames": [
                    "20/11 - 26/11"
                  ],
                  "projectNames": [
                    "PSknowHOW "
                  ],
                  "sprojectName": "PSknowHOW "
                },
                {
                  "data": "50.0",
                  "sSprintID": "27/11 - 03/12",
                  "sSprintName": "27/11 - 03/12",
                  "value": 50,
                  "hoverValue": {
                    "Total number of Changes": 8,
                    "Failed Changes": 4
                  },
                  "date": "27/11 - 03/12",
                  "sprintIds": [
                    "27/11 - 03/12"
                  ],
                  "sprintNames": [
                    "27/11 - 03/12"
                  ],
                  "projectNames": [
                    "PSknowHOW "
                  ],
                  "sprojectName": "PSknowHOW "
                },
                {
                  "data": "0.0",
                  "sSprintID": "04/12 - 10/12",
                  "sSprintName": "04/12 - 10/12",
                  "value": 0,
                  "hoverValue": {
                    "Total number of Changes": 0,
                    "Failed Changes": 0
                  },
                  "date": "04/12 - 10/12",
                  "sprintIds": [
                    "04/12 - 10/12"
                  ],
                  "sprintNames": [
                    "04/12 - 10/12"
                  ],
                  "projectNames": [
                    "PSknowHOW "
                  ],
                  "sprojectName": "PSknowHOW "
                },
                {
                  "data": "0.0",
                  "sSprintID": "11/12 - 17/12",
                  "sSprintName": "11/12 - 17/12",
                  "value": 0,
                  "hoverValue": {
                    "Total number of Changes": 0,
                    "Failed Changes": 0
                  },
                  "date": "11/12 - 17/12",
                  "sprintIds": [
                    "11/12 - 17/12"
                  ],
                  "sprintNames": [
                    "11/12 - 17/12"
                  ],
                  "projectNames": [
                    "PSknowHOW "
                  ],
                  "sprojectName": "PSknowHOW "
                },
                {
                  "data": "0.0",
                  "sSprintID": "18/12 - 24/12",
                  "sSprintName": "18/12 - 24/12",
                  "value": 0,
                  "hoverValue": {
                    "Total number of Changes": 0,
                    "Failed Changes": 0
                  },
                  "date": "18/12 - 24/12",
                  "sprintIds": [
                    "18/12 - 24/12"
                  ],
                  "sprintNames": [
                    "18/12 - 24/12"
                  ],
                  "projectNames": [
                    "PSknowHOW "
                  ],
                  "sprojectName": "PSknowHOW "
                }
              ],
              "maturity": "4",
              "maturityValue": "15.18",
              "aggregationValue": 15.18
            }
          ]
        },
        {
          "filter": "invoke_via_api->PSknowHOW ",
          "value": [
            {
              "data": "PSknowHOW ",
              "value": [
                {
                  "data": "71.43",
                  "count": 7,
                  "sSprintID": "30/10 - 05/11",
                  "sSprintName": "30/10 - 05/11",
                  "value": 71.43,
                  "hoverValue": {
                    "Total number of Changes": 7,
                    "Failed Changes": 5
                  },
                  "date": "30/10 - 05/11",
                  "kpiGroup": "invoke_via_api",
                  "sprintIds": [
                    "30/10 - 05/11"
                  ],
                  "sprintNames": [
                    "30/10 - 05/11"
                  ],
                  "sprojectName": "PSknowHOW "
                },
                {
                  "data": "0.0",
                  "count": 1,
                  "sSprintID": "06/11 - 12/11",
                  "sSprintName": "06/11 - 12/11",
                  "value": 0,
                  "hoverValue": {
                    "Total number of Changes": 1,
                    "Failed Changes": 0
                  },
                  "date": "06/11 - 12/11",
                  "kpiGroup": "invoke_via_api",
                  "sprintIds": [
                    "06/11 - 12/11"
                  ],
                  "sprintNames": [
                    "06/11 - 12/11"
                  ],
                  "sprojectName": "PSknowHOW "
                },
                {
                  "data": "0.0",
                  "count": 0,
                  "sSprintID": "13/11 - 19/11",
                  "sSprintName": "13/11 - 19/11",
                  "value": 0,
                  "hoverValue": {
                    "Total number of Changes": 0,
                    "Failed Changes": 0
                  },
                  "date": "13/11 - 19/11",
                  "kpiGroup": "invoke_via_api",
                  "sprintIds": [
                    "13/11 - 19/11"
                  ],
                  "sprintNames": [
                    "13/11 - 19/11"
                  ],
                  "sprojectName": "PSknowHOW "
                },
                {
                  "data": "0.0",
                  "count": 0,
                  "sSprintID": "20/11 - 26/11",
                  "sSprintName": "20/11 - 26/11",
                  "value": 0,
                  "hoverValue": {
                    "Total number of Changes": 0,
                    "Failed Changes": 0
                  },
                  "date": "20/11 - 26/11",
                  "kpiGroup": "invoke_via_api",
                  "sprintIds": [
                    "20/11 - 26/11"
                  ],
                  "sprintNames": [
                    "20/11 - 26/11"
                  ],
                  "sprojectName": "PSknowHOW "
                },
                {
                  "data": "50.0",
                  "count": 8,
                  "sSprintID": "27/11 - 03/12",
                  "sSprintName": "27/11 - 03/12",
                  "value": 50,
                  "hoverValue": {
                    "Total number of Changes": 8,
                    "Failed Changes": 4
                  },
                  "date": "27/11 - 03/12",
                  "kpiGroup": "invoke_via_api",
                  "sprintIds": [
                    "27/11 - 03/12"
                  ],
                  "sprintNames": [
                    "27/11 - 03/12"
                  ],
                  "sprojectName": "PSknowHOW "
                },
                {
                  "data": "0.0",
                  "count": 0,
                  "sSprintID": "04/12 - 10/12",
                  "sSprintName": "04/12 - 10/12",
                  "value": 0,
                  "hoverValue": {
                    "Total number of Changes": 0,
                    "Failed Changes": 0
                  },
                  "date": "04/12 - 10/12",
                  "kpiGroup": "invoke_via_api",
                  "sprintIds": [
                    "04/12 - 10/12"
                  ],
                  "sprintNames": [
                    "04/12 - 10/12"
                  ],
                  "sprojectName": "PSknowHOW "
                },
                {
                  "data": "0.0",
                  "count": 0,
                  "sSprintID": "11/12 - 17/12",
                  "sSprintName": "11/12 - 17/12",
                  "value": 0,
                  "hoverValue": {
                    "Total number of Changes": 0,
                    "Failed Changes": 0
                  },
                  "date": "11/12 - 17/12",
                  "kpiGroup": "invoke_via_api",
                  "sprintIds": [
                    "11/12 - 17/12"
                  ],
                  "sprintNames": [
                    "11/12 - 17/12"
                  ],
                  "sprojectName": "PSknowHOW "
                },
                {
                  "data": "0.0",
                  "count": 0,
                  "sSprintID": "18/12 - 24/12",
                  "sSprintName": "18/12 - 24/12",
                  "value": 0,
                  "hoverValue": {
                    "Total number of Changes": 0,
                    "Failed Changes": 0
                  },
                  "date": "18/12 - 24/12",
                  "kpiGroup": "invoke_via_api",
                  "sprintIds": [
                    "18/12 - 24/12"
                  ],
                  "sprintNames": [
                    "18/12 - 24/12"
                  ],
                  "sprojectName": "PSknowHOW "
                }
              ],
              "maturity": "4",
              "maturityValue": "15.18",
              "aggregationValue": 15.18
            }
          ]
        }
      ],
      "maturityRange": [
        "-60",
        "60-45",
        "45-30",
        "30-15",
        "15-"
      ],
      "maturityLevel": [
        {
          "level": "M5",
          "bgColor": "#167a26",
          "displayRange": "0-15 %"
        },
        {
          "level": "M4",
          "bgColor": "#4ebb1a",
          "displayRange": "15-30 %"
        },
        {
          "level": "M3",
          "bgColor": "#ef7643",
          "displayRange": "30-45 %"
        },
        {
          "level": "M2",
          "bgColor": "#f53535",
          "displayRange": "45-60 %"
        },
        {
          "level": "M1",
          "bgColor": "#c91212",
          "displayRange": "60 % and Above"
        }
      ],
      "groupId": 14,
      "filterDuration": {
        "duration": "WEEKS",
        "value": 8
      }
    }
  ];
  const kpi118ChartData = [{
    "data": "PSknowHOW ",
    "value": [
      {
        "data": "2",
        "sSprintID": "30/10 - 05/11",
        "sSprintName": "30/10 - 05/11",
        "value": 2,
        "hoverValue": {
          "81.200.188.111": 1,
          "81.200.188.112": 1,
          "81.200.188.113": 0
        },
        "date": "30/10 - 05/11",
        "sprintIds": [
          "30/10 - 05/11"
        ],
        "sprintNames": [
          "30/10 - 05/11"
        ],
        "projectNames": [
          "PSknowHOW "
        ],
        "sprojectName": "PSknowHOW ",
        "sortSprint": "30/10 - 05/11",
        "xName": 1
      },
      {
        "data": "1",
        "sSprintID": "06/11 - 12/11",
        "sSprintName": "06/11 - 12/11",
        "value": 1,
        "hoverValue": {
          "81.200.188.111": 0,
          "81.200.188.112": 0,
          "81.200.188.113": 1
        },
        "date": "06/11 - 12/11",
        "sprintIds": [
          "06/11 - 12/11"
        ],
        "sprintNames": [
          "06/11 - 12/11"
        ],
        "projectNames": [
          "PSknowHOW "
        ],
        "sprojectName": "PSknowHOW ",
        "sortSprint": "06/11 - 12/11",
        "xName": 2
      },
      {
        "data": "0",
        "sSprintID": "13/11 - 19/11",
        "sSprintName": "13/11 - 19/11",
        "value": 0,
        "hoverValue": {
          "81.200.188.111": 0,
          "81.200.188.112": 0,
          "81.200.188.113": 0
        },
        "date": "13/11 - 19/11",
        "sprintIds": [
          "13/11 - 19/11"
        ],
        "sprintNames": [
          "13/11 - 19/11"
        ],
        "projectNames": [
          "PSknowHOW "
        ],
        "sprojectName": "PSknowHOW ",
        "sortSprint": "13/11 - 19/11",
        "xName": 3
      },
      {
        "data": "0",
        "sSprintID": "20/11 - 26/11",
        "sSprintName": "20/11 - 26/11",
        "value": 0,
        "hoverValue": {
          "81.200.188.111": 0,
          "81.200.188.112": 0,
          "81.200.188.113": 0
        },
        "date": "20/11 - 26/11",
        "sprintIds": [
          "20/11 - 26/11"
        ],
        "sprintNames": [
          "20/11 - 26/11"
        ],
        "projectNames": [
          "PSknowHOW "
        ],
        "sprojectName": "PSknowHOW ",
        "sortSprint": "20/11 - 26/11",
        "xName": 4
      },
      {
        "data": "4",
        "sSprintID": "27/11 - 03/12",
        "sSprintName": "27/11 - 03/12",
        "value": 4,
        "hoverValue": {
          "81.200.188.111": 1,
          "81.200.188.112": 2,
          "81.200.188.113": 1
        },
        "date": "27/11 - 03/12",
        "sprintIds": [
          "27/11 - 03/12"
        ],
        "sprintNames": [
          "27/11 - 03/12"
        ],
        "projectNames": [
          "PSknowHOW "
        ],
        "sprojectName": "PSknowHOW ",
        "sortSprint": "27/11 - 03/12",
        "xName": 5
      },
      {
        "data": "0",
        "sSprintID": "04/12 - 10/12",
        "sSprintName": "04/12 - 10/12",
        "value": 0,
        "hoverValue": {
          "81.200.188.111": 0,
          "81.200.188.112": 0,
          "81.200.188.113": 0
        },
        "date": "04/12 - 10/12",
        "sprintIds": [
          "04/12 - 10/12"
        ],
        "sprintNames": [
          "04/12 - 10/12"
        ],
        "projectNames": [
          "PSknowHOW "
        ],
        "sprojectName": "PSknowHOW ",
        "sortSprint": "04/12 - 10/12",
        "xName": 6
      },
      {
        "data": "0",
        "sSprintID": "11/12 - 17/12",
        "sSprintName": "11/12 - 17/12",
        "value": 0,
        "hoverValue": {
          "81.200.188.111": 0,
          "81.200.188.112": 0,
          "81.200.188.113": 0
        },
        "date": "11/12 - 17/12",
        "sprintIds": [
          "11/12 - 17/12"
        ],
        "sprintNames": [
          "11/12 - 17/12"
        ],
        "projectNames": [
          "PSknowHOW "
        ],
        "sprojectName": "PSknowHOW ",
        "sortSprint": "11/12 - 17/12",
        "xName": 7
      },
      {
        "data": "0",
        "sSprintID": "18/12 - 24/12",
        "sSprintName": "18/12 - 24/12",
        "value": 0,
        "hoverValue": {
          "81.200.188.111": 0,
          "81.200.188.112": 0,
          "81.200.188.113": 0
        },
        "date": "18/12 - 24/12",
        "sprintIds": [
          "18/12 - 24/12"
        ],
        "sprintNames": [
          "18/12 - 24/12"
        ],
        "projectNames": [
          "PSknowHOW "
        ],
        "sprojectName": "PSknowHOW ",
        "sortSprint": "18/12 - 24/12",
        "xName": 8
      }
    ],
    "maturity": "1",
    "maturityValue": "1",
    "aggregationValue": 1,
    "maturityColor": "#c91212"
  }];
  const filterData = [
    {
      "nodeId": "11642_Sonepar INT_6542b4bb08f31814845119b2",
      "nodeName": "[USA-SPRFLD] Spark SINT Available_Sonepar INT",
      "path": [
        "Sonepar INT_6542b4bb08f31814845119b2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar INT_6542b4bb08f31814845119b2"
      ],
      "level": 6,
      "releaseEndDate": "2023-04-12T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "3342_Sonepar Cloud_6542b82208f31814845119bb",
      "nodeName": "Cloud - S 14.4 / 76_Sonepar Cloud",
      "sprintStartDate": "2023-10-26T09:43:56.514Z",
      "sprintEndDate": "2023-11-09T09:23:48.000Z",
      "path": [
        "Sonepar Cloud_6542b82208f31814845119bb###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar Cloud_6542b82208f31814845119bb"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "41002_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.15.xxx iOS_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-16T00:00:00.000Z",
      "releaseStartDate": "2023-11-09T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "13556_Ecom Post-Purchase Squad_64be67caeb7015715615c4c5",
      "nodeName": "SBRAPP 3.4_Ecom Post-Purchase Squad",
      "path": [
        "Ecom Post-Purchase Squad_64be67caeb7015715615c4c5###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Ecom Post-Purchase Squad_64be67caeb7015715615c4c5"
      ],
      "level": 6,
      "releaseEndDate": "2023-12-05T00:00:00.000Z",
      "releaseStartDate": "2023-06-20T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "11723_Sonepar MAP_6542b43f08f31814845119ab",
      "nodeName": "DFS-PI15_Sonepar MAP",
      "path": [
        "Sonepar MAP_6542b43f08f31814845119ab###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar MAP_6542b43f08f31814845119ab"
      ],
      "level": 6,
      "releaseEndDate": "2024-03-16T00:00:00.000Z",
      "releaseStartDate": "2023-12-06T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "11819_Sonepar Cloud_6542b82208f31814845119bb",
      "nodeName": "FE Next RC_Sonepar Cloud",
      "path": [
        "Sonepar Cloud_6542b82208f31814845119bb###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar Cloud_6542b82208f31814845119bb"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-08T00:00:00.000Z",
      "releaseStartDate": "2023-11-06T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2284_CMS_644103e772a7c53c78f70582",
      "nodeName": "CMS_Sep_CMS",
      "sprintStartDate": "2023-08-07T14:41:44.342Z",
      "sprintEndDate": "2023-09-04T14:32:00.000Z",
      "path": [
        "CMS_644103e772a7c53c78f70582###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "CMS_644103e772a7c53c78f70582"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "13565_GearBox Squad 2_64770ec45286e83998a56141",
      "nodeName": "GB Dashboard 23.04_GearBox Squad 2",
      "path": [
        "GearBox Squad 2_64770ec45286e83998a56141###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "GearBox Squad 2_64770ec45286e83998a56141"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-29T00:00:00.000Z",
      "releaseStartDate": "2023-08-07T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "53770_pod16 2_657219fd78247c3cd726d630",
      "nodeName": "POD 16 | Sprint 23_15_pod16 2",
      "sprintStartDate": "2023-10-12T05:00:00.000Z",
      "sprintEndDate": "2023-10-25T13:00:00.000Z",
      "path": [
        "pod16 2_657219fd78247c3cd726d630###2021 WLP Brand Retainer_port###AAA Auto Club Group_acc###Automative1_ver###A_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "pod16 2_657219fd78247c3cd726d630"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2274_Data Visualization_64425a0a30d86a7f539c7fdc",
      "nodeName": "DEA Sprint 23.21_Data Visualization",
      "sprintStartDate": "2023-10-11T04:03:41.941Z",
      "sprintEndDate": "2023-10-25T03:30:00.000Z",
      "path": [
        "Data Visualization_64425a0a30d86a7f539c7fdc###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Data Visualization_64425a0a30d86a7f539c7fdc"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "2206_My Account_6441081a72a7c53c78f70595",
      "nodeName": "Cart&Checkout July07 2023 Rel7_My Account",
      "sprintStartDate": "2023-06-12T14:10:22.861Z",
      "sprintEndDate": "2023-07-09T14:43:00.000Z",
      "path": [
        "My Account_6441081a72a7c53c78f70595###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "My Account_6441081a72a7c53c78f70595"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "Commerce_sqd_64be65cceb7015715615c4ba",
      "nodeName": "Commerce",
      "path": [
        "2309_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2268_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2266_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2265_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2218_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2267_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sqd",
      "parentId": [
        "2309_Dotcom + Mobile App_64be65cceb7015715615c4ba",
        "2268_Dotcom + Mobile App_64be65cceb7015715615c4ba",
        "2266_Dotcom + Mobile App_64be65cceb7015715615c4ba",
        "2265_Dotcom + Mobile App_64be65cceb7015715615c4ba",
        "2218_Dotcom + Mobile App_64be65cceb7015715615c4ba",
        "2267_Dotcom + Mobile App_64be65cceb7015715615c4ba"
      ],
      "level": 7
    },
    {
      "nodeId": "2362_Website TOF_6441052372a7c53c78f70588",
      "nodeName": "WebsiteTOF_Dec_22_2023_Rel13_Website TOF",
      "sprintStartDate": "2023-11-27T18:05:30.337Z",
      "sprintEndDate": "2023-12-24T10:12:00.000Z",
      "path": [
        "Website TOF_6441052372a7c53c78f70588###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Website TOF_6441052372a7c53c78f70588"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "3264_Sonepar BUY_6542b42308f31814845119a8",
      "nodeName": "B&D - 14.1_Sonepar BUY",
      "sprintStartDate": "2023-09-15T15:52:32.013Z",
      "sprintEndDate": "2023-09-27T20:23:00.000Z",
      "path": [
        "Sonepar BUY_6542b42308f31814845119a8###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar BUY_6542b42308f31814845119a8"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "40759_SAME_63a4810c09378702f4eab210",
      "nodeName": "1.3.38_SAME",
      "path": [
        "SAME_63a4810c09378702f4eab210###ACE20001_port###Abu Dhabi Investment Authority_acc###PS Internal_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "SAME_63a4810c09378702f4eab210"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-31T00:00:00.000Z",
      "releaseStartDate": "2023-10-02T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "151221_Do it Best_657049b505ce0569d5612ec7",
      "nodeName": "FEAEM_RV1.6_Do it Best",
      "path": [
        "Do it Best_657049b505ce0569d5612ec7###3PP CRM_port###ADEO_acc###Automotive_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Do it Best_657049b505ce0569d5612ec7"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-28T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Released"
    },
    {
      "nodeId": "13589_Ecom Purchase Squad_64be677aeb7015715615c4c1",
      "nodeName": "SBRAPP 3.3.1_Ecom Purchase Squad",
      "path": [
        "Ecom Purchase Squad_64be677aeb7015715615c4c1###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Ecom Purchase Squad_64be677aeb7015715615c4c1"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-05T00:00:00.000Z",
      "releaseStartDate": "2023-09-11T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2242_Website BOF_644108c072a7c53c78f7059a",
      "nodeName": "Cart&Checkout Sept01 2023 Rel9_Website BOF",
      "sprintStartDate": "2023-08-07T14:02:03.600Z",
      "sprintEndDate": "2023-09-03T16:15:00.000Z",
      "path": [
        "Website BOF_644108c072a7c53c78f7059a###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Website BOF_644108c072a7c53c78f7059a"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "RABO Scrum (New)_64a5dac31734471c30844068",
      "nodeName": "RABO Scrum (New)",
      "path": "AA_port###ADEO_acc###Financial Services_ver###EU_bu",
      "labelName": "project",
      "parentId": "AA_port",
      "level": 5,
      "basicProjectConfigId": "64a5dac31734471c30844068"
    },
    {
      "nodeId": "Website BOF_644108c072a7c53c78f7059a",
      "nodeName": "Website BOF",
      "path": "API_port###Academy Sports_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "API_port",
      "level": 5,
      "basicProjectConfigId": "644108c072a7c53c78f7059a"
    },
    {
      "nodeId": "2231_Website BOF_644108c072a7c53c78f7059a",
      "nodeName": "OMS_Oct27_2023_Rel11_Website BOF",
      "sprintStartDate": "2023-10-02T01:10:38.456Z",
      "sprintEndDate": "2023-10-31T14:49:00.000Z",
      "path": [
        "Website BOF_644108c072a7c53c78f7059a###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Website BOF_644108c072a7c53c78f7059a"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "39909_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.12.2162 Android_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-07-05T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "AAA Auto Club Group_acc",
      "nodeName": "AAA Auto Club Group",
      "path": "Automative1_ver###EU_bu",
      "labelName": "acc",
      "parentId": "Automative1_ver",
      "level": 3
    },
    {
      "nodeId": "2258_SEO_64a4e0591734471c30843fc0",
      "nodeName": "Search_Sep29_2023.Rel10_SEO",
      "sprintStartDate": "2023-09-04T20:50:34.462Z",
      "sprintEndDate": "2023-10-01T21:39:00.000Z",
      "path": [
        "SEO_64a4e0591734471c30843fc0###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "SEO_64a4e0591734471c30843fc0"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "15307_DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "nodeName": "BABCOCK_DRP - Discovery POD",
      "path": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1"
      ],
      "level": 6,
      "releaseEndDate": "2021-01-27T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Team 2_sqd_63a02b61bbc09e116d744d9d",
      "nodeName": "Team 2",
      "path": [
        "2369_GearBox_63a02b61bbc09e116d744d9d###GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2326_GearBox_63a02b61bbc09e116d744d9d###GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2229_GearBox_63a02b61bbc09e116d744d9d###GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2318_GearBox_63a02b61bbc09e116d744d9d###GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2370_GearBox_63a02b61bbc09e116d744d9d###GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2317_GearBox_63a02b61bbc09e116d744d9d###GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sqd",
      "parentId": [
        "2369_GearBox_63a02b61bbc09e116d744d9d",
        "2326_GearBox_63a02b61bbc09e116d744d9d",
        "2229_GearBox_63a02b61bbc09e116d744d9d",
        "2318_GearBox_63a02b61bbc09e116d744d9d",
        "2370_GearBox_63a02b61bbc09e116d744d9d",
        "2317_GearBox_63a02b61bbc09e116d744d9d"
      ],
      "level": 7
    },
    {
      "nodeId": "2318_GearBox Squad 3_64770ef45286e83998a56143",
      "nodeName": "GB_Sprint 23.22_GearBox Squad 3",
      "sprintStartDate": "2023-10-25T11:33:17.758Z",
      "sprintEndDate": "2023-11-08T03:30:00.000Z",
      "path": [
        "GearBox Squad 3_64770ef45286e83998a56143###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "GearBox Squad 3_64770ef45286e83998a56143"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "11800_Sonepar MAP_6542b43f08f31814845119ab",
      "nodeName": "v1.399 BE AFS_Sonepar MAP",
      "path": [
        "Sonepar MAP_6542b43f08f31814845119ab###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar MAP_6542b43f08f31814845119ab"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-02T00:00:00.000Z",
      "releaseStartDate": "2023-10-31T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "40083_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.13.2181 Android_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-08-07T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "eCommerce_sqd_6377306a175a953a0a49d322",
      "nodeName": "eCommerce",
      "path": [
        "2287_Integration Services_6377306a175a953a0a49d322###Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2356_Integration Services_6377306a175a953a0a49d322###Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2320_Integration Services_6377306a175a953a0a49d322###Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2377_Integration Services_6377306a175a953a0a49d322###Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sqd",
      "parentId": [
        "2287_Integration Services_6377306a175a953a0a49d322",
        "2356_Integration Services_6377306a175a953a0a49d322",
        "2320_Integration Services_6377306a175a953a0a49d322",
        "2377_Integration Services_6377306a175a953a0a49d322"
      ],
      "level": 7
    },
    {
      "nodeId": "PORIO_656ee0e5053eaf4d3a2b06eb",
      "nodeName": "PORIO",
      "path": "3PP Social - Chrysler_port###AAA Auto Club Group_acc###Consumer Products_ver###EU_bu",
      "labelName": "project",
      "parentId": "3PP Social - Chrysler_port",
      "level": 5,
      "basicProjectConfigId": "656ee0e5053eaf4d3a2b06eb"
    },
    {
      "nodeId": "45164_KN Server_656969922d6d5f774de2e686",
      "nodeName": "KnowHOW | PI_14| ITR_5_KN Server",
      "sprintStartDate": "2023-08-23T05:41:00.000Z",
      "sprintEndDate": "2023-09-05T05:41:00.000Z",
      "path": [
        "KN Server_656969922d6d5f774de2e686###2021 WLP Brand Retainer_port###ADNOC Global Trading Limited_acc###Consumer Products_ver###Government Services_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "KN Server_656969922d6d5f774de2e686"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "54403_DHL Logistics Scrumban_6549029708f3181484511bbb",
      "nodeName": "FT2 Sprint83 17.10.23-31.10.23_DHL Logistics Scrumban",
      "sprintStartDate": "2023-10-17T09:40:00.000Z",
      "sprintEndDate": "2023-10-31T09:40:00.000Z",
      "path": [
        "DHL Logistics Scrumban_6549029708f3181484511bbb###DPDHL - CSI DCI - Logistics and CJ_port###Deutsche Post AG_acc###Automotive_ver###EU_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "DHL Logistics Scrumban_6549029708f3181484511bbb"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "274_PSknowHOW _6527af981704342160f43748",
      "nodeName": "KnowHOW | PI_15| ITR_2_PSknowHOW ",
      "sprintStartDate": "2023-10-11T08:05:40.935Z",
      "sprintEndDate": "2023-10-24T08:20:00.000Z",
      "path": [
        "PSknowHOW _6527af981704342160f43748###DTS_port###Methods and Tools_acc###PS Internal_ver###Internal_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "PSknowHOW _6527af981704342160f43748"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "40041_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.13.1501 iOS_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-07-31T00:00:00.000Z",
      "releaseStartDate": "2023-06-16T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Automative1_ver",
      "nodeName": "Automative1",
      "path": "EU_bu",
      "labelName": "ver",
      "parentId": "EU_bu",
      "level": 2
    },
    {
      "nodeId": "287_PSknowHOW _6527af981704342160f43748",
      "nodeName": "KnowHOW | PI_15| ITR_5_PSknowHOW ",
      "sprintStartDate": "2023-11-22T07:22:02.359Z",
      "sprintEndDate": "2023-12-05T08:28:00.000Z",
      "path": [
        "PSknowHOW _6527af981704342160f43748###DTS_port###Methods and Tools_acc###PS Internal_ver###Internal_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "PSknowHOW _6527af981704342160f43748"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Team 3_sqd_63a02b61bbc09e116d744d9d",
      "nodeName": "Team 3",
      "path": [
        "2318_GearBox_63a02b61bbc09e116d744d9d###GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2317_GearBox_63a02b61bbc09e116d744d9d###GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2370_GearBox_63a02b61bbc09e116d744d9d###GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "1086_GearBox_63a02b61bbc09e116d744d9d###GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2229_GearBox_63a02b61bbc09e116d744d9d###GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2326_GearBox_63a02b61bbc09e116d744d9d###GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2369_GearBox_63a02b61bbc09e116d744d9d###GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sqd",
      "parentId": [
        "2318_GearBox_63a02b61bbc09e116d744d9d",
        "2317_GearBox_63a02b61bbc09e116d744d9d",
        "2370_GearBox_63a02b61bbc09e116d744d9d",
        "1086_GearBox_63a02b61bbc09e116d744d9d",
        "2229_GearBox_63a02b61bbc09e116d744d9d",
        "2326_GearBox_63a02b61bbc09e116d744d9d",
        "2369_GearBox_63a02b61bbc09e116d744d9d"
      ],
      "level": 7
    },
    {
      "nodeId": "2286_Canada ELD_64f1d426d8d45b13a8de56cb",
      "nodeName": "CELD_Sprint 23.20_Canada ELD",
      "sprintStartDate": "2023-09-27T04:00:00.000Z",
      "sprintEndDate": "2023-10-10T16:00:00.000Z",
      "path": [
        "Canada ELD_64f1d426d8d45b13a8de56cb###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Canada ELD_64f1d426d8d45b13a8de56cb"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "10029_Test rc_657056c668a1225c0126c843",
      "nodeName": "KnowHOW PI-14_Test rc",
      "path": [
        "Test rc_657056c668a1225c0126c843###2021 WLP Brand Retainer_port###AB Tetra Pak_acc###Consumer Products_ver###Europe_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Test rc_657056c668a1225c0126c843"
      ],
      "level": 6,
      "releaseEndDate": "2023-09-26T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2374_Data Visualization_64425a0a30d86a7f539c7fdc",
      "nodeName": "DV Sprint 23.24_Data Visualization",
      "sprintStartDate": "2023-11-22T06:27:26.194Z",
      "sprintEndDate": "2023-12-06T04:30:00.000Z",
      "path": [
        "Data Visualization_64425a0a30d86a7f539c7fdc###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Data Visualization_64425a0a30d86a7f539c7fdc"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "2380_My Account_6441081a72a7c53c78f70595",
      "nodeName": "MyAccount_Nov24_2023.Rel12_My Account",
      "sprintStartDate": "2023-10-30T05:00:28.241Z",
      "sprintEndDate": "2023-11-24T05:00:00.000Z",
      "path": [
        "My Account_6441081a72a7c53c78f70595###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "My Account_6441081a72a7c53c78f70595"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "11711_Website BOF_644108c072a7c53c78f7059a",
      "nodeName": "Website Jan 2024 Release_Website BOF",
      "path": [
        "Website BOF_644108c072a7c53c78f7059a###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Website BOF_644108c072a7c53c78f7059a"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-27T00:00:00.000Z",
      "releaseStartDate": "2023-10-02T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "11825_Design System_64ad9e667d51263c17602c67",
      "nodeName": "v1.406 FE RC_Design System",
      "path": [
        "Design System_64ad9e667d51263c17602c67###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Design System_64ad9e667d51263c17602c67"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-22T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Released"
    },
    {
      "nodeId": "10599_Sonepar EAC_6542b3b008f31814845119a0",
      "nodeName": "v1.189 FE RC_Sonepar EAC",
      "path": [
        "Sonepar EAC_6542b3b008f31814845119a0###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar EAC_6542b3b008f31814845119a0"
      ],
      "level": 6,
      "releaseEndDate": "2022-09-05T00:00:00.000Z",
      "releaseStartDate": "2022-08-29T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "15560_DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "nodeName": "Daredevil_DRP - Discovery POD",
      "path": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1"
      ],
      "level": 6,
      "releaseEndDate": "2022-04-20T00:00:00.000Z",
      "releaseStartDate": "2022-04-20T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "11750_Design System_64ad9e667d51263c17602c67",
      "nodeName": "FE next RC_Design System",
      "path": [
        "Design System_64ad9e667d51263c17602c67###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Design System_64ad9e667d51263c17602c67"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-08T00:00:00.000Z",
      "releaseStartDate": "2023-10-05T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Endeavour Group Limited_acc",
      "nodeName": "Endeavour Group Limited",
      "path": "Retail_ver###International_bu",
      "labelName": "acc",
      "parentId": "Retail_ver",
      "level": 3
    },
    {
      "nodeId": "151529_Do it Best_657049b505ce0569d5612ec7",
      "nodeName": "MAG-HOTFIX-1.4-P5_Do it Best",
      "path": [
        "Do it Best_657049b505ce0569d5612ec7###3PP CRM_port###ADEO_acc###Automotive_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Do it Best_657049b505ce0569d5612ec7"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-09T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Released"
    },
    {
      "nodeId": "FDFH_656f0b8d275aa91d24a6e568",
      "nodeName": "FDFH",
      "path": "3PP CRM_port###AAA Auto Club Group_acc###Automotive_ver###A_bu",
      "labelName": "project",
      "parentId": "3PP CRM_port",
      "level": 5,
      "basicProjectConfigId": "656f0b8d275aa91d24a6e568"
    },
    {
      "nodeId": "Team 2_sqd_64770ec45286e83998a56141",
      "nodeName": "Team 2",
      "path": [
        "2326_GearBox Squad 2_64770ec45286e83998a56141###GearBox Squad 2_64770ec45286e83998a56141###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2369_GearBox Squad 2_64770ec45286e83998a56141###GearBox Squad 2_64770ec45286e83998a56141###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2229_GearBox Squad 2_64770ec45286e83998a56141###GearBox Squad 2_64770ec45286e83998a56141###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2317_GearBox Squad 2_64770ec45286e83998a56141###GearBox Squad 2_64770ec45286e83998a56141###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2318_GearBox Squad 2_64770ec45286e83998a56141###GearBox Squad 2_64770ec45286e83998a56141###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2228_GearBox Squad 2_64770ec45286e83998a56141###GearBox Squad 2_64770ec45286e83998a56141###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sqd",
      "parentId": [
        "2326_GearBox Squad 2_64770ec45286e83998a56141",
        "2369_GearBox Squad 2_64770ec45286e83998a56141",
        "2229_GearBox Squad 2_64770ec45286e83998a56141",
        "2317_GearBox Squad 2_64770ec45286e83998a56141",
        "2318_GearBox Squad 2_64770ec45286e83998a56141",
        "2228_GearBox Squad 2_64770ec45286e83998a56141"
      ],
      "level": 7
    },
    {
      "nodeId": "a87e019f-88ab-4e9e-b5fd-a07b90d6608b_Azure Project_656d830b9f546b19742cb55b",
      "nodeName": "Iteration 3_Azure Project",
      "sprintStartDate": "2023-06-01T00:00:00.000Z",
      "sprintEndDate": "2023-06-02T00:00:00.000Z",
      "path": [
        "Azure Project_656d830b9f546b19742cb55b###3PP CRM_port###ADEO_acc###B_ver###Europe_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Azure Project_656d830b9f546b19742cb55b"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Internal_bu",
      "nodeName": "Internal",
      "path": "",
      "labelName": "bu",
      "level": 1
    },
    {
      "nodeId": "North America_bu",
      "nodeName": "North America",
      "path": "",
      "labelName": "bu",
      "level": 1
    },
    {
      "nodeId": "280_Test rc_657056c668a1225c0126c843",
      "nodeName": "KnowHOW | PI_15| ITR_1_Test rc",
      "sprintStartDate": "2023-09-27T10:23:08.827Z",
      "sprintEndDate": "2023-10-10T08:18:00.000Z",
      "path": [
        "Test rc_657056c668a1225c0126c843###2021 WLP Brand Retainer_port###AB Tetra Pak_acc###Consumer Products_ver###Europe_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Test rc_657056c668a1225c0126c843"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "3266_Sonepar SAF_6542b3da08f31814845119a2",
      "nodeName": "B&D - 14.3_Sonepar SAF",
      "sprintStartDate": "2023-10-12T09:06:24.233Z",
      "sprintEndDate": "2023-10-25T09:51:00.000Z",
      "path": [
        "Sonepar SAF_6542b3da08f31814845119a2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar SAF_6542b3da08f31814845119a2"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "13589_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "nodeName": "SBRAPP 3.3.1_Dotcom + Mobile App",
      "path": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-05T00:00:00.000Z",
      "releaseStartDate": "2023-09-11T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "11829_Sonepar SAF_6542b3da08f31814845119a2",
      "nodeName": "BE Next RC B&D_Sonepar SAF",
      "path": [
        "Sonepar SAF_6542b3da08f31814845119a2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar SAF_6542b3da08f31814845119a2"
      ],
      "level": 6,
      "releaseEndDate": "2023-12-29T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "1004_Promotion Engine TOF_644105f972a7c53c78f7058c",
      "nodeName": "Promo_Engine_TOF_Aug_4_Promotion Engine TOF",
      "sprintStartDate": "2023-07-10T18:24:04.896Z",
      "sprintEndDate": "2023-08-04T20:15:00.000Z",
      "path": [
        "Promotion Engine TOF_644105f972a7c53c78f7058c###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Promotion Engine TOF_644105f972a7c53c78f7058c"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "new test_65714b7752bfa01f6cff043d",
      "nodeName": "new test",
      "path": "2021 WLP Brand Retainer_port###AAA Auto Club Group_acc###Consumer Products_ver###Europe_bu",
      "labelName": "project",
      "parentId": "2021 WLP Brand Retainer_port",
      "level": 5,
      "basicProjectConfigId": "65714b7752bfa01f6cff043d"
    },
    {
      "nodeId": "1017_DRP - HomePage POD_64b3f315c4e72b57c94035e2",
      "nodeName": "DRP Sprint 85_DRP - HomePage POD",
      "sprintStartDate": "2023-11-02T14:18:44.928Z",
      "sprintEndDate": "2023-11-25T04:00:00.000Z",
      "path": [
        "DRP - HomePage POD_64b3f315c4e72b57c94035e2###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "DRP - HomePage POD_64b3f315c4e72b57c94035e2"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "10389_Mobile App_637b17b9175a953a0a49d3c2",
      "nodeName": "Release 3.3.0_Mobile App",
      "path": [
        "Mobile App_637b17b9175a953a0a49d3c2###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Mobile App_637b17b9175a953a0a49d3c2"
      ],
      "level": 6,
      "releaseEndDate": "2023-07-06T00:00:00.000Z",
      "releaseStartDate": "2023-04-12T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2241_Website TOF_6441052372a7c53c78f70588",
      "nodeName": "Cart&Checkout Aug05 2023 Rel8_Website TOF",
      "sprintStartDate": "2023-07-10T16:15:00.000Z",
      "sprintEndDate": "2023-08-06T16:15:00.000Z",
      "path": [
        "Website TOF_6441052372a7c53c78f70588###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Website TOF_6441052372a7c53c78f70588"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "11785_Sonepar EAC_6542b3b008f31814845119a0",
      "nodeName": "v1.394 FE_Sonepar EAC",
      "path": [
        "Sonepar EAC_6542b3b008f31814845119a0###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar EAC_6542b3b008f31814845119a0"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-31T00:00:00.000Z",
      "releaseStartDate": "2023-10-23T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "Team 2_sqd_648ab6186803f300a9fd2e0e",
      "nodeName": "Team 2",
      "path": [
        "2238_R1+ Logistics_648ab6186803f300a9fd2e0e###R1+ Logistics_648ab6186803f300a9fd2e0e###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2177_R1+ Logistics_648ab6186803f300a9fd2e0e###R1+ Logistics_648ab6186803f300a9fd2e0e###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2203_R1+ Logistics_648ab6186803f300a9fd2e0e###R1+ Logistics_648ab6186803f300a9fd2e0e###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2202_R1+ Logistics_648ab6186803f300a9fd2e0e###R1+ Logistics_648ab6186803f300a9fd2e0e###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sqd",
      "parentId": [
        "2238_R1+ Logistics_648ab6186803f300a9fd2e0e",
        "2177_R1+ Logistics_648ab6186803f300a9fd2e0e",
        "2203_R1+ Logistics_648ab6186803f300a9fd2e0e",
        "2202_R1+ Logistics_648ab6186803f300a9fd2e0e"
      ],
      "level": 7
    },
    {
      "nodeId": "54861_pod16 2_657219fd78247c3cd726d630",
      "nodeName": "POD 16 | Sprint 23_19_pod16 2",
      "sprintStartDate": "2023-12-07T05:00:00.000Z",
      "sprintEndDate": "2023-12-20T13:00:00.000Z",
      "path": [
        "pod16 2_657219fd78247c3cd726d630###2021 WLP Brand Retainer_port###AAA Auto Club Group_acc###Automative1_ver###A_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "pod16 2_657219fd78247c3cd726d630"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "2777_SAME_63a4810c09378702f4eab210",
      "nodeName": "Sprint 100 - Initial Plan_SAME",
      "sprintStartDate": "2023-10-10T14:24:05.541Z",
      "sprintEndDate": "2023-10-24T12:23:00.000Z",
      "path": [
        "SAME_63a4810c09378702f4eab210###ACE20001_port###Abu Dhabi Investment Authority_acc###PS Internal_ver###EU_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "SAME_63a4810c09378702f4eab210"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2229_GearBox_63a02b61bbc09e116d744d9d",
      "nodeName": "GB_Sprint 23.20_GearBox",
      "sprintStartDate": "2023-09-27T05:00:43.565Z",
      "sprintEndDate": "2023-10-11T03:30:00.000Z",
      "path": [
        "GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "GearBox_63a02b61bbc09e116d744d9d"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "3336_Design System_64ad9e667d51263c17602c67",
      "nodeName": "WATTS 14.2_Design System",
      "sprintStartDate": "2023-09-29T09:55:18.106Z",
      "sprintEndDate": "2023-10-04T22:00:00.000Z",
      "path": [
        "Design System_64ad9e667d51263c17602c67###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Design System_64ad9e667d51263c17602c67"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2296_P2P_64ca0b8f5fec906dbc18f3c5",
      "nodeName": "P2P_Sprint 23.20_P2P",
      "sprintStartDate": "2023-09-27T15:26:02.000Z",
      "sprintEndDate": "2023-10-10T21:00:00.000Z",
      "path": [
        "P2P_64ca0b8f5fec906dbc18f3c5###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "P2P_64ca0b8f5fec906dbc18f3c5"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Automotive_ver",
      "nodeName": "Automotive",
      "path": "International_bu",
      "labelName": "ver",
      "parentId": "International_bu",
      "level": 2
    },
    {
      "nodeId": "Team 2_sqd_63a02b61bbc09e116d744d9d",
      "nodeName": "Team 2",
      "path": "2326_GearBox_63a02b61bbc09e116d744d9d###GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2326_GearBox_63a02b61bbc09e116d744d9d",
      "level": 7
    },
    {
      "nodeId": "15511_DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "nodeName": "Jan2022_R1_DRP - Discovery POD",
      "path": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1"
      ],
      "level": 6,
      "releaseEndDate": "2022-01-20T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Commerce_sqd_64be65cceb7015715615c4ba",
      "nodeName": "Commerce",
      "path": "2268_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2268_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "level": 7
    },
    {
      "nodeId": "GearBox_63a02b61bbc09e116d744d9d",
      "nodeName": "GearBox",
      "path": "Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Sunbelt Rentals_port",
      "level": 5,
      "basicProjectConfigId": "63a02b61bbc09e116d744d9d"
    },
    {
      "nodeId": "54236_Do it Best_657049b505ce0569d5612ec7",
      "nodeName": "Do it Best - Sprint V1.4_Do it Best",
      "sprintStartDate": "2023-10-11T11:00:00.000Z",
      "sprintEndDate": "2023-10-31T01:00:00.000Z",
      "path": [
        "Do it Best_657049b505ce0569d5612ec7###3PP CRM_port###ADEO_acc###Automotive_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Do it Best_657049b505ce0569d5612ec7"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "3199_Sonepar AFS_654135db88c4b8114af77dba",
      "nodeName": "AFS - S14.3_Sonepar AFS",
      "sprintStartDate": "2023-10-13T22:00:18.148Z",
      "sprintEndDate": "2023-10-25T21:30:00.000Z",
      "path": [
        "Sonepar AFS_654135db88c4b8114af77dba###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar AFS_654135db88c4b8114af77dba"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2232_Service & Assets_6494298bca84920b10dddd3b",
      "nodeName": "SERV_Sprint 23.19_Service & Assets",
      "sprintStartDate": "2023-09-13T16:56:28.763Z",
      "sprintEndDate": "2023-09-26T16:56:00.000Z",
      "path": [
        "Service & Assets_6494298bca84920b10dddd3b###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Service & Assets_6494298bca84920b10dddd3b"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "54753_DHL Logistics Scrumban_6549029708f3181484511bbb",
      "nodeName": "FT1 Sprint 85_DHL Logistics Scrumban",
      "sprintStartDate": "2023-11-14T07:05:00.000Z",
      "sprintEndDate": "2023-11-27T07:05:00.000Z",
      "path": [
        "DHL Logistics Scrumban_6549029708f3181484511bbb###DPDHL - CSI DCI - Logistics and CJ_port###Deutsche Post AG_acc###Automotive_ver###EU_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "DHL Logistics Scrumban_6549029708f3181484511bbb"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "Team 2_sqd_64770ec45286e83998a56141",
      "nodeName": "Team 2",
      "path": "2369_GearBox Squad 2_64770ec45286e83998a56141###GearBox Squad 2_64770ec45286e83998a56141###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2369_GearBox Squad 2_64770ec45286e83998a56141",
      "level": 7
    },
    {
      "nodeId": "39822_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.12.1489 iOS_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-06-04T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "1037_Promotion Engine TOF_644105f972a7c53c78f7058c",
      "nodeName": "Promo_Engine_BOF_Sep_1_Promotion Engine TOF",
      "sprintStartDate": "2023-08-07T21:58:09.148Z",
      "sprintEndDate": "2023-09-01T19:29:00.000Z",
      "path": [
        "Promotion Engine TOF_644105f972a7c53c78f7058c###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Promotion Engine TOF_644105f972a7c53c78f7058c"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "10142_PSknowHOW _6527af981704342160f43748",
      "nodeName": "KnowHOW v8.3.0_PSknowHOW ",
      "path": [
        "PSknowHOW _6527af981704342160f43748###DTS_port###Methods and Tools_acc###PS Internal_ver###Internal_bu"
      ],
      "labelName": "release",
      "parentId": [
        "PSknowHOW _6527af981704342160f43748"
      ],
      "level": 6,
      "releaseEndDate": "2023-12-28T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "52911_POD 16_657065700615235d92401735",
      "nodeName": "POD 16 | Sprint 23_11_POD 16",
      "sprintStartDate": "2023-08-17T10:30:00.000Z",
      "sprintEndDate": "2023-08-30T18:30:00.000Z",
      "path": [
        "POD 16_657065700615235d92401735###Nissan Core Team - PS_port###Nissan Motor Co. Ltd._acc###Automotive_ver###International_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "POD 16_657065700615235d92401735"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Canada ELD_64f1d426d8d45b13a8de56cb",
      "nodeName": "Canada ELD",
      "path": "Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Sunbelt Rentals_port",
      "level": 5,
      "basicProjectConfigId": "64f1d426d8d45b13a8de56cb"
    },
    {
      "nodeId": "1037_Promotion Engine BOF_64c178d7eb7015715615c5a6",
      "nodeName": "Promo_Engine_BOF_Sep_1_Promotion Engine BOF",
      "sprintStartDate": "2023-08-07T21:58:09.148Z",
      "sprintEndDate": "2023-09-01T19:29:00.000Z",
      "path": [
        "Promotion Engine BOF_64c178d7eb7015715615c5a6###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Promotion Engine BOF_64c178d7eb7015715615c5a6"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "10034_Cart & checkout_6441078b72a7c53c78f70590",
      "nodeName": "Backlog_Cart & checkout",
      "path": [
        "Cart & checkout_6441078b72a7c53c78f70590###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Cart & checkout_6441078b72a7c53c78f70590"
      ],
      "level": 6,
      "releaseEndDate": "2019-09-03T00:00:00.000Z",
      "releaseStartDate": "2019-06-24T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2178_R1+ Logistics_648ab6186803f300a9fd2e0e",
      "nodeName": "LOG_Sprint 23.18_R1+ Logistics",
      "sprintStartDate": "2023-08-30T14:26:46.786Z",
      "sprintEndDate": "2023-09-13T03:50:00.000Z",
      "path": [
        "R1+ Logistics_648ab6186803f300a9fd2e0e###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "R1+ Logistics_648ab6186803f300a9fd2e0e"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "3213_Sonepar MAP_6542b43f08f31814845119ab",
      "nodeName": "M&P - S14.5_Sonepar MAP",
      "sprintStartDate": "2023-11-09T11:16:39.739Z",
      "sprintEndDate": "2023-11-22T10:44:00.000Z",
      "path": [
        "Sonepar MAP_6542b43f08f31814845119ab###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar MAP_6542b43f08f31814845119ab"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "10176_PSknowHOW _6527af981704342160f43748",
      "nodeName": "KnowHOW v8.3.0_PSknowHOW ",
      "path": [
        "PSknowHOW _6527af981704342160f43748###DTS_port###Methods and Tools_acc###PS Internal_ver###Internal_bu"
      ],
      "labelName": "release",
      "parentId": [
        "PSknowHOW _6527af981704342160f43748"
      ],
      "level": 6,
      "releaseEndDate": "2024-01-05T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2229_GearBox Squad 1_6449103b3be37902a3f1ba70",
      "nodeName": "GB_Sprint 23.20_GearBox Squad 1",
      "sprintStartDate": "2023-09-27T05:00:43.565Z",
      "sprintEndDate": "2023-10-11T03:30:00.000Z",
      "path": [
        "GearBox Squad 1_6449103b3be37902a3f1ba70###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "GearBox Squad 1_6449103b3be37902a3f1ba70"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "ENI S.p.A_acc",
      "nodeName": "ENI S.p.A",
      "path": "Energy & Commodities_ver###EU_bu",
      "labelName": "acc",
      "parentId": "Energy & Commodities_ver",
      "level": 3
    },
    {
      "nodeId": "1038_Promotion Engine TOF_644105f972a7c53c78f7058c",
      "nodeName": "Promo_Engine_BOF_Jul_7_Promotion Engine TOF",
      "sprintStartDate": "2023-06-12T23:25:08.459Z",
      "sprintEndDate": "2023-07-07T18:50:00.000Z",
      "path": [
        "Promotion Engine TOF_644105f972a7c53c78f7058c###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Promotion Engine TOF_644105f972a7c53c78f7058c"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2355_VDOS Outside Hauler_647702b25286e83998a56138",
      "nodeName": "OMS_Sprint 23.22_VDOS Outside Hauler",
      "sprintStartDate": "2023-10-25T15:31:14.456Z",
      "sprintEndDate": "2023-11-08T15:31:03.000Z",
      "path": [
        "VDOS Outside Hauler_647702b25286e83998a56138###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "VDOS Outside Hauler_647702b25286e83998a56138"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "506b485a-82b2-4271-b839-2a94e17666e7_KYC _63e5fea5ae1aeb593f3395aa",
      "nodeName": "Sprint 21_KYC ",
      "sprintStartDate": "2023-08-07T00:00:00.000Z",
      "sprintEndDate": "2023-08-25T00:00:00.000Z",
      "path": [
        "KYC _63e5fea5ae1aeb593f3395aa###ENI-Evolutions_port###ENI S.p.A_acc###Energy & Commodities_ver###EU_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "KYC _63e5fea5ae1aeb593f3395aa"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "1007_DRP - HomePage POD_64b3f315c4e72b57c94035e2",
      "nodeName": "DRP Sprint 83_DRP - HomePage POD",
      "sprintStartDate": "2023-09-21T17:14:44.383Z",
      "sprintEndDate": "2023-10-11T16:37:00.000Z",
      "path": [
        "DRP - HomePage POD_64b3f315c4e72b57c94035e2###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "DRP - HomePage POD_64b3f315c4e72b57c94035e2"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "3218_Unified Commerce - Dan's MVP_64ab97327d51263c17602b58",
      "nodeName": "UC Sprint 15_Unified Commerce - Dan's MVP",
      "sprintStartDate": "2023-11-15T03:30:00.000Z",
      "sprintEndDate": "2023-11-29T03:30:00.000Z",
      "path": [
        "Unified Commerce - Dan's MVP_64ab97327d51263c17602b58###Endeavour Group Pty Ltd_port###Endeavour Group Limited_acc###Retail_ver###International_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Unified Commerce - Dan's MVP_64ab97327d51263c17602b58"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Team 4_sqd_648ab6186803f300a9fd2e0e",
      "nodeName": "Team 4",
      "path": [
        "2177_R1+ Logistics_648ab6186803f300a9fd2e0e###R1+ Logistics_648ab6186803f300a9fd2e0e###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2204_R1+ Logistics_648ab6186803f300a9fd2e0e###R1+ Logistics_648ab6186803f300a9fd2e0e###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2203_R1+ Logistics_648ab6186803f300a9fd2e0e###R1+ Logistics_648ab6186803f300a9fd2e0e###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sqd",
      "parentId": [
        "2177_R1+ Logistics_648ab6186803f300a9fd2e0e",
        "2204_R1+ Logistics_648ab6186803f300a9fd2e0e",
        "2203_R1+ Logistics_648ab6186803f300a9fd2e0e"
      ],
      "level": 7
    },
    {
      "nodeId": "11774_Sonepar EAC_6542b3b008f31814845119a0",
      "nodeName": "v1.392 FE_Sonepar EAC",
      "path": [
        "Sonepar EAC_6542b3b008f31814845119a0###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar EAC_6542b3b008f31814845119a0"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-26T00:00:00.000Z",
      "releaseStartDate": "2023-10-23T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "15210_DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "nodeName": "Westeros_DRP - Discovery POD",
      "path": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1"
      ],
      "level": 6,
      "releaseEndDate": "2020-10-28T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "11785_Sonepar BUY_6542b42308f31814845119a8",
      "nodeName": "v1.394 FE_Sonepar BUY",
      "path": [
        "Sonepar BUY_6542b42308f31814845119a8###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar BUY_6542b42308f31814845119a8"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-31T00:00:00.000Z",
      "releaseStartDate": "2023-10-23T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "2237_Service & Assets_6494298bca84920b10dddd3b",
      "nodeName": "SERV_Sprint 23.20_Service & Assets",
      "sprintStartDate": "2023-09-27T04:30:48.548Z",
      "sprintEndDate": "2023-10-11T03:00:00.000Z",
      "path": [
        "Service & Assets_6494298bca84920b10dddd3b###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Service & Assets_6494298bca84920b10dddd3b"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2914_Sonepar Global_6448a8213be37902a3f1ba45",
      "nodeName": "D-AX League S13.3_Sonepar Global",
      "sprintStartDate": "2023-07-20T19:55:38.112Z",
      "sprintEndDate": "2023-08-03T09:13:00.000Z",
      "path": [
        "Sonepar Global_6448a8213be37902a3f1ba45###Sonepar Client Cost - MC_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar Global_6448a8213be37902a3f1ba45"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "41022_SAME_63a4810c09378702f4eab210",
      "nodeName": "1.3.37.6_SAME",
      "path": [
        "SAME_63a4810c09378702f4eab210###ACE20001_port###Abu Dhabi Investment Authority_acc###PS Internal_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "SAME_63a4810c09378702f4eab210"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-16T00:00:00.000Z",
      "releaseStartDate": "2023-11-15T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "2204_R1+ Logistics_648ab6186803f300a9fd2e0e",
      "nodeName": "LOG_Sprint 23.22_R1+ Logistics",
      "sprintStartDate": "2023-10-25T18:14:54.637Z",
      "sprintEndDate": "2023-11-08T05:00:00.000Z",
      "path": [
        "R1+ Logistics_648ab6186803f300a9fd2e0e###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "R1+ Logistics_648ab6186803f300a9fd2e0e"
      ],
      "sprintState": "closed",
      "level": 6
    },
    {
      "nodeId": "13599_GearBox Squad 1_6449103b3be37902a3f1ba70",
      "nodeName": "23.05_GearBox Squad 1",
      "path": [
        "GearBox Squad 1_6449103b3be37902a3f1ba70###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "GearBox Squad 1_6449103b3be37902a3f1ba70"
      ],
      "level": 6,
      "releaseEndDate": "2024-01-31T00:00:00.000Z",
      "releaseStartDate": "2023-11-22T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "39540_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.13.X Android_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-08-04T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "15627_DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "nodeName": "Kingpin_DRP - Discovery POD",
      "path": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1"
      ],
      "level": 6,
      "releaseEndDate": "2022-07-27T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "11782_Sonepar SAF_6542b3da08f31814845119a2",
      "nodeName": "v1.391 BE S&P - Hotfix_Sonepar SAF",
      "path": [
        "Sonepar SAF_6542b3da08f31814845119a2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar SAF_6542b3da08f31814845119a2"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-23T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Released"
    },
    {
      "nodeId": "156_PSknowHOW _6527af981704342160f43748",
      "nodeName": "PS HOW |PI_15|ITR_3|25_Oct_PSknowHOW ",
      "sprintStartDate": "2023-10-25T16:30:20.220Z",
      "sprintEndDate": "2023-11-08T16:30:00.000Z",
      "path": [
        "PSknowHOW _6527af981704342160f43748###DTS_port###Methods and Tools_acc###PS Internal_ver###Internal_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "PSknowHOW _6527af981704342160f43748"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "HomepageEcom_sqd_64b3f315c4e72b57c94035e2",
      "nodeName": "HomepageEcom",
      "path": [
        "990_DRP - HomePage POD_64b3f315c4e72b57c94035e2###DRP - HomePage POD_64b3f315c4e72b57c94035e2###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu",
        "1006_DRP - HomePage POD_64b3f315c4e72b57c94035e2###DRP - HomePage POD_64b3f315c4e72b57c94035e2###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu",
        "1008_DRP - HomePage POD_64b3f315c4e72b57c94035e2###DRP - HomePage POD_64b3f315c4e72b57c94035e2###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu",
        "1017_DRP - HomePage POD_64b3f315c4e72b57c94035e2###DRP - HomePage POD_64b3f315c4e72b57c94035e2###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu",
        "1007_DRP - HomePage POD_64b3f315c4e72b57c94035e2###DRP - HomePage POD_64b3f315c4e72b57c94035e2###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu",
        "1016_DRP - HomePage POD_64b3f315c4e72b57c94035e2###DRP - HomePage POD_64b3f315c4e72b57c94035e2###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "sqd",
      "parentId": [
        "990_DRP - HomePage POD_64b3f315c4e72b57c94035e2",
        "1006_DRP - HomePage POD_64b3f315c4e72b57c94035e2",
        "1008_DRP - HomePage POD_64b3f315c4e72b57c94035e2",
        "1017_DRP - HomePage POD_64b3f315c4e72b57c94035e2",
        "1007_DRP - HomePage POD_64b3f315c4e72b57c94035e2",
        "1016_DRP - HomePage POD_64b3f315c4e72b57c94035e2"
      ],
      "level": 7
    },
    {
      "nodeId": "Team 2_sqd_647588bc5286e83998a5609c",
      "nodeName": "Team 2",
      "path": [
        "2175_R1+ Frontline_647588bc5286e83998a5609c###R1+ Frontline_647588bc5286e83998a5609c###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2174_R1+ Frontline_647588bc5286e83998a5609c###R1+ Frontline_647588bc5286e83998a5609c###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2214_R1+ Frontline_647588bc5286e83998a5609c###R1+ Frontline_647588bc5286e83998a5609c###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2215_R1+ Frontline_647588bc5286e83998a5609c###R1+ Frontline_647588bc5286e83998a5609c###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2213_R1+ Frontline_647588bc5286e83998a5609c###R1+ Frontline_647588bc5286e83998a5609c###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2212_R1+ Frontline_647588bc5286e83998a5609c###R1+ Frontline_647588bc5286e83998a5609c###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sqd",
      "parentId": [
        "2175_R1+ Frontline_647588bc5286e83998a5609c",
        "2174_R1+ Frontline_647588bc5286e83998a5609c",
        "2214_R1+ Frontline_647588bc5286e83998a5609c",
        "2215_R1+ Frontline_647588bc5286e83998a5609c",
        "2213_R1+ Frontline_647588bc5286e83998a5609c",
        "2212_R1+ Frontline_647588bc5286e83998a5609c"
      ],
      "level": 7
    },
    {
      "nodeId": "Sunbelt Rentals_port",
      "nodeName": "Sunbelt Rentals",
      "path": "Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "port",
      "parentId": "Sunbelt Rentals_acc",
      "level": 4
    },
    {
      "nodeId": "2404_PIM_64a4e09e1734471c30843fc2",
      "nodeName": "PIM_Dec22_23_Sprint_46_PIM",
      "sprintStartDate": "2023-11-27T16:53:15.397Z",
      "sprintEndDate": "2023-12-25T15:56:00.000Z",
      "path": [
        "PIM_64a4e09e1734471c30843fc2###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "PIM_64a4e09e1734471c30843fc2"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "11647_Sonepar INT_6542b4bb08f31814845119b2",
      "nodeName": "[USA-SPRFLD] Login Ready_Sonepar INT",
      "path": [
        "Sonepar INT_6542b4bb08f31814845119b2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar INT_6542b4bb08f31814845119b2"
      ],
      "level": 6,
      "releaseEndDate": "2023-06-07T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2317_GearBox Squad 2_64770ec45286e83998a56141",
      "nodeName": "GB_Sprint 23.21_GearBox Squad 2",
      "sprintStartDate": "2023-10-11T06:00:29.059Z",
      "sprintEndDate": "2023-10-25T03:30:00.000Z",
      "path": [
        "GearBox Squad 2_64770ec45286e83998a56141###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "GearBox Squad 2_64770ec45286e83998a56141"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2294_P2P_64ca0b8f5fec906dbc18f3c5",
      "nodeName": "P2P_Sprint 23.24_P2P",
      "sprintStartDate": "2023-11-22T16:44:20.000Z",
      "sprintEndDate": "2023-12-06T04:00:00.000Z",
      "path": [
        "P2P_64ca0b8f5fec906dbc18f3c5###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "P2P_64ca0b8f5fec906dbc18f3c5"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "40955_SAME_63a4810c09378702f4eab210",
      "nodeName": "1.3.40_SAME",
      "path": [
        "SAME_63a4810c09378702f4eab210###ACE20001_port###Abu Dhabi Investment Authority_acc###PS Internal_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "SAME_63a4810c09378702f4eab210"
      ],
      "level": 6,
      "releaseEndDate": "2023-12-15T00:00:00.000Z",
      "releaseStartDate": "2023-11-08T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "3514_Sonepar Mobile App_6448a96c3be37902a3f1ba48",
      "nodeName": "DFA QA Sprint 14.6_Sonepar Mobile App",
      "sprintStartDate": "2023-11-23T12:29:26.665Z",
      "sprintEndDate": "2023-12-07T11:29:30.000Z",
      "path": [
        "Sonepar Mobile App_6448a96c3be37902a3f1ba48###Sonepar Client Cost - MC_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar Mobile App_6448a96c3be37902a3f1ba48"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "Commerce_sqd_64be65cceb7015715615c4ba",
      "nodeName": "Commerce",
      "path": "2266_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2266_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "level": 7
    },
    {
      "nodeId": "2139_Data Engineering_644258b830d86a7f539c7fd7",
      "nodeName": "DV Sprint 23.16_Data Engineering",
      "sprintStartDate": "2023-08-02T04:25:15.336Z",
      "sprintEndDate": "2023-08-16T03:30:00.000Z",
      "path": [
        "Data Engineering_644258b830d86a7f539c7fd7###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Data Engineering_644258b830d86a7f539c7fd7"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "2238_Service & Assets_6494298bca84920b10dddd3b",
      "nodeName": "SERV_Sprint 23.21_Service & Assets",
      "sprintStartDate": "2023-10-11T17:43:28.866Z",
      "sprintEndDate": "2023-10-24T04:00:00.000Z",
      "path": [
        "Service & Assets_6494298bca84920b10dddd3b###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Service & Assets_6494298bca84920b10dddd3b"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2354_VDOS Translations_651fe6bb1704342160f43511",
      "nodeName": "OMS_Sprint 23.21_VDOS Translations",
      "sprintStartDate": "2023-10-11T09:16:16.993Z",
      "sprintEndDate": "2023-10-25T09:16:08.000Z",
      "path": [
        "VDOS Translations_651fe6bb1704342160f43511###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "VDOS Translations_651fe6bb1704342160f43511"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "AA Data and Reporting_649c00cd1734471c30843d2d",
      "nodeName": "AA Data and Reporting",
      "path": "Anglo American Marketing Limited_port###Anglo American Marketing Limited_acc###Energy & Commodities_ver###EU_bu",
      "labelName": "project",
      "parentId": "Anglo American Marketing Limited_port",
      "level": 5,
      "basicProjectConfigId": "649c00cd1734471c30843d2d"
    },
    {
      "nodeId": "11723_Sonepar EAC_6542b3b008f31814845119a0",
      "nodeName": "DFS-PI15_Sonepar EAC",
      "path": [
        "Sonepar EAC_6542b3b008f31814845119a0###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar EAC_6542b3b008f31814845119a0"
      ],
      "level": 6,
      "releaseEndDate": "2024-03-16T00:00:00.000Z",
      "releaseStartDate": "2023-12-06T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "15306_DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "nodeName": "ACE_DRP - Discovery POD",
      "path": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1"
      ],
      "level": 6,
      "releaseEndDate": "2021-01-13T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2354_VDOS Outside Hauler_647702b25286e83998a56138",
      "nodeName": "OMS_Sprint 23.21_VDOS Outside Hauler",
      "sprintStartDate": "2023-10-11T09:16:16.993Z",
      "sprintEndDate": "2023-10-25T09:16:08.000Z",
      "path": [
        "VDOS Outside Hauler_647702b25286e83998a56138###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "VDOS Outside Hauler_647702b25286e83998a56138"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "39914_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.12.2163 Android_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-07-06T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "54891_Do it Best_657049b505ce0569d5612ec7",
      "nodeName": "Do it Best - EO Sprint V3_Do it Best",
      "sprintStartDate": "2023-11-30T10:09:00.000Z",
      "sprintEndDate": "2023-12-13T10:09:00.000Z",
      "path": [
        "Do it Best_657049b505ce0569d5612ec7###3PP CRM_port###ADEO_acc###Automotive_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Do it Best_657049b505ce0569d5612ec7"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "Team 3_sqd_64770ef45286e83998a56143",
      "nodeName": "Team 3",
      "path": [
        "2228_GearBox Squad 3_64770ef45286e83998a56143###GearBox Squad 3_64770ef45286e83998a56143###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2369_GearBox Squad 3_64770ef45286e83998a56143###GearBox Squad 3_64770ef45286e83998a56143###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2318_GearBox Squad 3_64770ef45286e83998a56143###GearBox Squad 3_64770ef45286e83998a56143###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2229_GearBox Squad 3_64770ef45286e83998a56143###GearBox Squad 3_64770ef45286e83998a56143###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2326_GearBox Squad 3_64770ef45286e83998a56143###GearBox Squad 3_64770ef45286e83998a56143###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2317_GearBox Squad 3_64770ef45286e83998a56143###GearBox Squad 3_64770ef45286e83998a56143###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sqd",
      "parentId": [
        "2228_GearBox Squad 3_64770ef45286e83998a56143",
        "2369_GearBox Squad 3_64770ef45286e83998a56143",
        "2318_GearBox Squad 3_64770ef45286e83998a56143",
        "2229_GearBox Squad 3_64770ef45286e83998a56143",
        "2326_GearBox Squad 3_64770ef45286e83998a56143",
        "2317_GearBox Squad 3_64770ef45286e83998a56143"
      ],
      "level": 7
    },
    {
      "nodeId": "10150_PSknowHOW _6527af981704342160f43748",
      "nodeName": "KnowHOW v8.2.0_PSknowHOW ",
      "path": [
        "PSknowHOW _6527af981704342160f43748###DTS_port###Methods and Tools_acc###PS Internal_ver###Internal_bu"
      ],
      "labelName": "release",
      "parentId": [
        "PSknowHOW _6527af981704342160f43748"
      ],
      "level": 6,
      "releaseEndDate": "2023-12-07T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Team 3_sqd_63a02b61bbc09e116d744d9d",
      "nodeName": "Team 3",
      "path": "2317_GearBox_63a02b61bbc09e116d744d9d###GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2317_GearBox_63a02b61bbc09e116d744d9d",
      "level": 7
    },
    {
      "nodeId": "3348_Sonepar SRE_6542b86f08f31814845119be",
      "nodeName": "OPS-S 14.4 / 76_Sonepar SRE",
      "sprintStartDate": "2023-10-26T14:30:36.674Z",
      "sprintEndDate": "2023-11-09T14:29:19.000Z",
      "path": [
        "Sonepar SRE_6542b86f08f31814845119be###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar SRE_6542b86f08f31814845119be"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "11713_Onsite search_644131b98b61fa2477214bf3",
      "nodeName": "Website Oct 2023 - HF2 Release_Onsite search",
      "path": [
        "Onsite search_644131b98b61fa2477214bf3###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Onsite search_644131b98b61fa2477214bf3"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-23T00:00:00.000Z",
      "releaseStartDate": "2023-10-16T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "40319_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.13.2188 Android_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-09-04T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "11732_Sonepar SAF_6542b3da08f31814845119a2",
      "nodeName": "v1.385 BE S&P_Sonepar SAF",
      "path": [
        "Sonepar SAF_6542b3da08f31814845119a2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar SAF_6542b3da08f31814845119a2"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-23T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Released"
    },
    {
      "nodeId": "R1+ Sales_648ab46f6803f300a9fd2e09",
      "nodeName": "R1+ Sales",
      "path": "Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Sunbelt Rentals_port",
      "level": 5,
      "basicProjectConfigId": "648ab46f6803f300a9fd2e09"
    },
    {
      "nodeId": "2244_Cart & checkout_6441078b72a7c53c78f70590",
      "nodeName": "Cart&Checkout Oct27 2023 Rel11_Cart & checkout",
      "sprintStartDate": "2023-10-02T22:20:36.136Z",
      "sprintEndDate": "2023-10-29T16:16:00.000Z",
      "path": [
        "Cart & checkout_6441078b72a7c53c78f70590###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Cart & checkout_6441078b72a7c53c78f70590"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2151_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "nodeName": "eComm_Sprint 23.12_Dotcom + Mobile App",
      "sprintStartDate": "2023-06-12T14:04:57.839Z",
      "sprintEndDate": "2024-06-20T16:15:00.000Z",
      "path": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "3340_Design System_64ad9e667d51263c17602c67",
      "nodeName": "WATTS 14.6_Design System",
      "sprintStartDate": "2023-11-23T09:09:41.579Z",
      "sprintEndDate": "2023-12-04T22:00:00.000Z",
      "path": [
        "Design System_64ad9e667d51263c17602c67###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Design System_64ad9e667d51263c17602c67"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "54547_DHL Logistics Scrumban_6549029708f3181484511bbb",
      "nodeName": "FT2 Sprint84 31.10.23-14.11.23_DHL Logistics Scrumban",
      "sprintStartDate": "2023-10-31T10:37:00.000Z",
      "sprintEndDate": "2023-11-14T10:37:00.000Z",
      "path": [
        "DHL Logistics Scrumban_6549029708f3181484511bbb###DPDHL - CSI DCI - Logistics and CJ_port###Deutsche Post AG_acc###Automotive_ver###EU_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "DHL Logistics Scrumban_6549029708f3181484511bbb"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "3214_Unified Commerce - Dan's MVP_64ab97327d51263c17602b58",
      "nodeName": "UC Sprint 11_Unified Commerce - Dan's MVP",
      "sprintStartDate": "2023-09-20T04:48:30.268Z",
      "sprintEndDate": "2023-10-04T02:00:00.000Z",
      "path": [
        "Unified Commerce - Dan's MVP_64ab97327d51263c17602b58###Endeavour Group Pty Ltd_port###Endeavour Group Limited_acc###Retail_ver###International_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Unified Commerce - Dan's MVP_64ab97327d51263c17602b58"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "15559_DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "nodeName": "SFCC 2.1_DRP - Discovery POD",
      "path": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1"
      ],
      "level": 6,
      "releaseEndDate": "2022-03-23T00:00:00.000Z",
      "releaseStartDate": "2022-03-23T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Sonepar MAP_6542b43f08f31814845119ab",
      "nodeName": "Sonepar MAP",
      "path": "Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Sonepar SAS_port",
      "level": 5,
      "basicProjectConfigId": "6542b43f08f31814845119ab"
    },
    {
      "nodeId": "Team 2_sqd_647588bc5286e83998a5609c",
      "nodeName": "Team 2",
      "path": "2174_R1+ Frontline_647588bc5286e83998a5609c###R1+ Frontline_647588bc5286e83998a5609c###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2174_R1+ Frontline_647588bc5286e83998a5609c",
      "level": 7
    },
    {
      "nodeId": "2268_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "nodeName": "ECOM_Sprint 23.24_Dotcom + Mobile App",
      "sprintStartDate": "2023-11-22T16:47:00.079Z",
      "sprintEndDate": "2023-12-05T22:00:00.000Z",
      "path": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "11646_Sonepar INT_6542b4bb08f31814845119b2",
      "nodeName": "[USA-SPRFLD] Place Order Ready_Sonepar INT",
      "path": [
        "Sonepar INT_6542b4bb08f31814845119b2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar INT_6542b4bb08f31814845119b2"
      ],
      "level": 6,
      "releaseEndDate": "2023-08-16T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2285_Onsite search_644131b98b61fa2477214bf3",
      "nodeName": "CMS_Oct2_2023Rel.10_Onsite search",
      "sprintStartDate": "2023-09-05T14:03:22.074Z",
      "sprintEndDate": "2023-10-02T15:53:00.000Z",
      "path": [
        "Onsite search_644131b98b61fa2477214bf3###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Onsite search_644131b98b61fa2477214bf3"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "13557_Ecom Pre-Purchase Squad_64be66e3eb7015715615c4bd",
      "nodeName": "SBRAPP 3.5_Ecom Pre-Purchase Squad",
      "path": [
        "Ecom Pre-Purchase Squad_64be66e3eb7015715615c4bd###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Ecom Pre-Purchase Squad_64be66e3eb7015715615c4bd"
      ],
      "level": 6,
      "releaseEndDate": "2024-01-31T00:00:00.000Z",
      "releaseStartDate": "2023-10-25T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "11657_Sonepar INT_6542b4bb08f31814845119b2",
      "nodeName": "[USA-SPRFLD] F&F Migrated_Sonepar INT",
      "path": [
        "Sonepar INT_6542b4bb08f31814845119b2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar INT_6542b4bb08f31814845119b2"
      ],
      "level": 6,
      "releaseEndDate": "2023-09-27T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "40655_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.14.Hotfix Android_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-09-19T00:00:00.000Z",
      "releaseStartDate": "2023-09-15T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "10226_VDOS_63777558175a953a0a49d363",
      "nodeName": "3.0_Rel 1 (BY)_VDOS",
      "path": [
        "VDOS_63777558175a953a0a49d363###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "VDOS_63777558175a953a0a49d363"
      ],
      "level": 6,
      "releaseEndDate": "2022-05-31T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "3211_Sonepar Cloud_6542b82208f31814845119bb",
      "nodeName": "M&P - S14.3_Sonepar Cloud",
      "sprintStartDate": "2023-10-12T08:36:49.124Z",
      "sprintEndDate": "2023-10-25T12:00:00.000Z",
      "path": [
        "Sonepar Cloud_6542b82208f31814845119bb###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar Cloud_6542b82208f31814845119bb"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Sonepar AFS_654135db88c4b8114af77dba",
      "nodeName": "Sonepar AFS",
      "path": "Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Sonepar SAS_port",
      "level": 5,
      "basicProjectConfigId": "654135db88c4b8114af77dba"
    },
    {
      "nodeId": "1178_ASO Mobile App_64a4fab01734471c30843fda",
      "nodeName": "MobileApp_29_Sep_2023_Rel10_ASO Mobile App",
      "sprintStartDate": "2023-09-05T20:05:34.964Z",
      "sprintEndDate": "2023-09-29T20:05:00.000Z",
      "path": [
        "ASO Mobile App_64a4fab01734471c30843fda###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "ASO Mobile App_64a4fab01734471c30843fda"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "40423_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.14.1517 iOS_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-09-13T00:00:00.000Z",
      "releaseStartDate": "2023-08-08T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "3PP Social - Chrysler_port",
      "nodeName": "3PP Social - Chrysler",
      "path": "ADNOC Global Trading Limited_acc###Consumer Products_ver###Internal_bu",
      "labelName": "port",
      "parentId": "ADNOC Global Trading Limited_acc",
      "level": 4
    },
    {
      "nodeId": "2323_VDOS Outside Hauler_647702b25286e83998a56138",
      "nodeName": "OMS_Sprint 23.20_VDOS Outside Hauler",
      "sprintStartDate": "2023-09-28T12:04:38.169Z",
      "sprintEndDate": "2023-10-11T04:00:00.000Z",
      "path": [
        "VDOS Outside Hauler_647702b25286e83998a56138###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "VDOS Outside Hauler_647702b25286e83998a56138"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2177_R1+ Logistics_648ab6186803f300a9fd2e0e",
      "nodeName": "LOG_Sprint 23.17_R1+ Logistics",
      "sprintStartDate": "2023-08-16T16:15:35.298Z",
      "sprintEndDate": "2023-08-30T03:26:00.000Z",
      "path": [
        "R1+ Logistics_648ab6186803f300a9fd2e0e###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "R1+ Logistics_648ab6186803f300a9fd2e0e"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2760_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "Sprint 121_RMMO",
      "sprintStartDate": "2023-09-01T06:47:34.082Z",
      "sprintEndDate": "2023-09-18T09:55:00.000Z",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "R1+ Logistics_648ab6186803f300a9fd2e0e",
      "nodeName": "R1+ Logistics",
      "path": "Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Sunbelt Rentals_port",
      "level": 5,
      "basicProjectConfigId": "648ab6186803f300a9fd2e0e"
    },
    {
      "nodeId": "Content_sqd_64be65cceb7015715615c4ba",
      "nodeName": "Content",
      "path": [
        "2218_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2265_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2309_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2266_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2267_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2268_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sqd",
      "parentId": [
        "2218_Dotcom + Mobile App_64be65cceb7015715615c4ba",
        "2265_Dotcom + Mobile App_64be65cceb7015715615c4ba",
        "2309_Dotcom + Mobile App_64be65cceb7015715615c4ba",
        "2266_Dotcom + Mobile App_64be65cceb7015715615c4ba",
        "2267_Dotcom + Mobile App_64be65cceb7015715615c4ba",
        "2268_Dotcom + Mobile App_64be65cceb7015715615c4ba"
      ],
      "level": 7
    },
    {
      "nodeId": "Legacy CC_sqd_64be65cceb7015715615c4ba",
      "nodeName": "Legacy CC",
      "path": [
        "2265_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2267_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2268_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2266_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2218_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2309_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sqd",
      "parentId": [
        "2265_Dotcom + Mobile App_64be65cceb7015715615c4ba",
        "2267_Dotcom + Mobile App_64be65cceb7015715615c4ba",
        "2268_Dotcom + Mobile App_64be65cceb7015715615c4ba",
        "2266_Dotcom + Mobile App_64be65cceb7015715615c4ba",
        "2218_Dotcom + Mobile App_64be65cceb7015715615c4ba",
        "2309_Dotcom + Mobile App_64be65cceb7015715615c4ba"
      ],
      "level": 7
    },
    {
      "nodeId": "ADEO_acc",
      "nodeName": "ADEO",
      "path": "B_ver###Europe_bu",
      "labelName": "acc",
      "parentId": "B_ver",
      "level": 3
    },
    {
      "nodeId": "274_Test rc_657056c668a1225c0126c843",
      "nodeName": "KnowHOW | PI_15| ITR_2_Test rc",
      "sprintStartDate": "2023-10-11T08:05:40.935Z",
      "sprintEndDate": "2023-10-24T08:20:00.000Z",
      "path": [
        "Test rc_657056c668a1225c0126c843###2021 WLP Brand Retainer_port###AB Tetra Pak_acc###Consumer Products_ver###Europe_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Test rc_657056c668a1225c0126c843"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "1095_Website TOF_6441052372a7c53c78f70588",
      "nodeName": "Search_Jul7_2023.Rel7_Website TOF",
      "sprintStartDate": "2023-06-12T20:10:00.419Z",
      "sprintEndDate": "2023-07-09T20:59:00.000Z",
      "path": [
        "Website TOF_6441052372a7c53c78f70588###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Website TOF_6441052372a7c53c78f70588"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "15209_DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "nodeName": "Unsullied_DRP - Discovery POD",
      "path": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1"
      ],
      "level": 6,
      "releaseEndDate": "2020-10-14T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2293_Cart & checkout_6441078b72a7c53c78f70590",
      "nodeName": "MyAccount_Sep01_2023.Rel9_Cart & checkout",
      "sprintStartDate": "2023-08-07T13:06:10.926Z",
      "sprintEndDate": "2023-09-01T16:15:00.000Z",
      "path": [
        "Cart & checkout_6441078b72a7c53c78f70590###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Cart & checkout_6441078b72a7c53c78f70590"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "2291_P2P_64ca0b8f5fec906dbc18f3c5",
      "nodeName": "P2P_Sprint 23.19_P2P",
      "sprintStartDate": "2023-09-13T16:00:06.065Z",
      "sprintEndDate": "2023-09-27T03:59:00.000Z",
      "path": [
        "P2P_64ca0b8f5fec906dbc18f3c5###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "P2P_64ca0b8f5fec906dbc18f3c5"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "3273_Sonepar Global_6448a8213be37902a3f1ba45",
      "nodeName": "Data Team 14.5_Sonepar Global",
      "sprintStartDate": "2023-11-09T08:32:14.881Z",
      "sprintEndDate": "2023-11-22T18:13:00.000Z",
      "path": [
        "Sonepar Global_6448a8213be37902a3f1ba45###Sonepar Client Cost - MC_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar Global_6448a8213be37902a3f1ba45"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "53637_POD 16_657065700615235d92401735",
      "nodeName": "POD 16 | Sprint 23_12_POD 16",
      "sprintStartDate": "2023-08-31T12:49:00.000Z",
      "sprintEndDate": "2023-09-13T12:49:00.000Z",
      "path": [
        "POD 16_657065700615235d92401735###Nissan Core Team - PS_port###Nissan Motor Co. Ltd._acc###Automotive_ver###International_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "POD 16_657065700615235d92401735"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "45279_KN Server_656969922d6d5f774de2e686",
      "nodeName": "MAP|PI_14|ITR_2_KN Server",
      "sprintStartDate": "2023-07-12T07:03:00.000Z",
      "sprintEndDate": "2023-07-25T18:03:00.000Z",
      "path": [
        "KN Server_656969922d6d5f774de2e686###2021 WLP Brand Retainer_port###ADNOC Global Trading Limited_acc###Consumer Products_ver###Government Services_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "KN Server_656969922d6d5f774de2e686"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Browse & Search_sqd_63dc01e47228be4c30553ce1",
      "nodeName": "Browse & Search",
      "path": [
        "1008_DRP - Discovery POD_63dc01e47228be4c30553ce1###DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu",
        "1007_DRP - Discovery POD_63dc01e47228be4c30553ce1###DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu",
        "1006_DRP - Discovery POD_63dc01e47228be4c30553ce1###DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu",
        "990_DRP - Discovery POD_63dc01e47228be4c30553ce1###DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu",
        "1017_DRP - Discovery POD_63dc01e47228be4c30553ce1###DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu",
        "1016_DRP - Discovery POD_63dc01e47228be4c30553ce1###DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "sqd",
      "parentId": [
        "1008_DRP - Discovery POD_63dc01e47228be4c30553ce1",
        "1007_DRP - Discovery POD_63dc01e47228be4c30553ce1",
        "1006_DRP - Discovery POD_63dc01e47228be4c30553ce1",
        "990_DRP - Discovery POD_63dc01e47228be4c30553ce1",
        "1017_DRP - Discovery POD_63dc01e47228be4c30553ce1",
        "1016_DRP - Discovery POD_63dc01e47228be4c30553ce1"
      ],
      "level": 7
    },
    {
      "nodeId": "1065_Mobile App_637b17b9175a953a0a49d3c2",
      "nodeName": "SBR.COM_Sprint 23.14_Mobile App",
      "sprintStartDate": "2023-07-05T15:42:52.589Z",
      "sprintEndDate": "2023-07-18T21:00:00.000Z",
      "path": [
        "Mobile App_637b17b9175a953a0a49d3c2###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Mobile App_637b17b9175a953a0a49d3c2"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "GearBox Squad 1_6449103b3be37902a3f1ba70",
      "nodeName": "GearBox Squad 1",
      "path": "Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Sunbelt Rentals_port",
      "level": 5,
      "basicProjectConfigId": "6449103b3be37902a3f1ba70"
    },
    {
      "nodeId": "Retail_port",
      "nodeName": "Retail",
      "path": "The Childrens Place, Inc._acc###Retail_ver###North America_bu",
      "labelName": "port",
      "parentId": "The Childrens Place, Inc._acc",
      "level": 4
    },
    {
      "nodeId": "API_port",
      "nodeName": "API",
      "path": "Academy Sports_acc###Retail_ver###North America_bu",
      "labelName": "port",
      "parentId": "Academy Sports_acc",
      "level": 4
    },
    {
      "nodeId": "15591_DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "nodeName": "Hulk_Hotfix1_DRP - Discovery POD",
      "path": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1"
      ],
      "level": 6,
      "releaseEndDate": "2022-06-24T00:00:00.000Z",
      "releaseStartDate": "2022-06-13T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2257_Promotion Engine BOF_64c178d7eb7015715615c5a6",
      "nodeName": "Search_Sep01_2023.Rel9_Promotion Engine BOF",
      "sprintStartDate": "2023-08-07T21:41:19.769Z",
      "sprintEndDate": "2023-09-03T22:30:00.000Z",
      "path": [
        "Promotion Engine BOF_64c178d7eb7015715615c5a6###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Promotion Engine BOF_64c178d7eb7015715615c5a6"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "Supply Chain_sqd_6377306a175a953a0a49d322",
      "nodeName": "Supply Chain",
      "path": [
        "2287_Integration Services_6377306a175a953a0a49d322###Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2356_Integration Services_6377306a175a953a0a49d322###Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2406_Integration Services_6377306a175a953a0a49d322###Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2320_Integration Services_6377306a175a953a0a49d322###Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2384_Integration Services_6377306a175a953a0a49d322###Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2377_Integration Services_6377306a175a953a0a49d322###Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sqd",
      "parentId": [
        "2287_Integration Services_6377306a175a953a0a49d322",
        "2356_Integration Services_6377306a175a953a0a49d322",
        "2406_Integration Services_6377306a175a953a0a49d322",
        "2320_Integration Services_6377306a175a953a0a49d322",
        "2384_Integration Services_6377306a175a953a0a49d322",
        "2377_Integration Services_6377306a175a953a0a49d322"
      ],
      "level": 7
    },
    {
      "nodeId": "2202_Website BOF_644108c072a7c53c78f7059a",
      "nodeName": "Web_Payment_Jun09_2023_Rel6_Website BOF",
      "sprintStartDate": "2023-05-16T13:46:25.840Z",
      "sprintEndDate": "2023-06-09T13:50:00.000Z",
      "path": [
        "Website BOF_644108c072a7c53c78f7059a###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Website BOF_644108c072a7c53c78f7059a"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "4342_RABO Scrum (New)_64a5dac31734471c30844068",
      "nodeName": "Sentry FY24 Sprint 22_RABO Scrum (New)",
      "sprintStartDate": "2023-11-23T09:36:51.333Z",
      "sprintEndDate": "2023-12-06T23:30:00.000Z",
      "path": [
        "RABO Scrum (New)_64a5dac31734471c30844068###AA_port###ADEO_acc###Financial Services_ver###EU_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "RABO Scrum (New)_64a5dac31734471c30844068"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "39773_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.12.2153 Android_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-06-13T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "10030_PSknowHOW _6527af981704342160f43748",
      "nodeName": "KnowHOW PI-13_PSknowHOW ",
      "path": [
        "PSknowHOW _6527af981704342160f43748###DTS_port###Methods and Tools_acc###PS Internal_ver###Internal_bu"
      ],
      "labelName": "release",
      "parentId": [
        "PSknowHOW _6527af981704342160f43748"
      ],
      "level": 6,
      "releaseEndDate": "2023-06-27T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Team 1_sqd_63a02b61bbc09e116d744d9d",
      "nodeName": "Team 1",
      "path": [
        "2370_GearBox_63a02b61bbc09e116d744d9d###GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2317_GearBox_63a02b61bbc09e116d744d9d###GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2318_GearBox_63a02b61bbc09e116d744d9d###GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2369_GearBox_63a02b61bbc09e116d744d9d###GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2229_GearBox_63a02b61bbc09e116d744d9d###GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2326_GearBox_63a02b61bbc09e116d744d9d###GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sqd",
      "parentId": [
        "2370_GearBox_63a02b61bbc09e116d744d9d",
        "2317_GearBox_63a02b61bbc09e116d744d9d",
        "2318_GearBox_63a02b61bbc09e116d744d9d",
        "2369_GearBox_63a02b61bbc09e116d744d9d",
        "2229_GearBox_63a02b61bbc09e116d744d9d",
        "2326_GearBox_63a02b61bbc09e116d744d9d"
      ],
      "level": 7
    },
    {
      "nodeId": "2220_Onsite search_644131b98b61fa2477214bf3",
      "nodeName": "CMS_Jul07_2023.Rel07_Onsite search",
      "sprintStartDate": "2023-06-12T14:37:55.686Z",
      "sprintEndDate": "2023-07-10T14:28:00.000Z",
      "path": [
        "Onsite search_644131b98b61fa2477214bf3###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Onsite search_644131b98b61fa2477214bf3"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "Sonepar SAS_port",
      "nodeName": "Sonepar SAS",
      "path": "Sonepar SAS_acc###Retail_ver###North America_bu",
      "labelName": "port",
      "parentId": "Sonepar SAS_acc",
      "level": 4
    },
    {
      "nodeId": "10599_Sonepar AFS_654135db88c4b8114af77dba",
      "nodeName": "v1.189 FE RC_Sonepar AFS",
      "path": [
        "Sonepar AFS_654135db88c4b8114af77dba###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar AFS_654135db88c4b8114af77dba"
      ],
      "level": 6,
      "releaseEndDate": "2022-09-05T00:00:00.000Z",
      "releaseStartDate": "2022-08-29T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "38787_RABO Scrum (New)_64a5dac31734471c30844068",
      "nodeName": "dotcom-2023-12-07_RABO Scrum (New)",
      "path": [
        "RABO Scrum (New)_64a5dac31734471c30844068###AA_port###ADEO_acc###Financial Services_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RABO Scrum (New)_64a5dac31734471c30844068"
      ],
      "level": 6,
      "releaseEndDate": "2023-12-07T00:00:00.000Z",
      "releaseStartDate": "2023-11-23T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "11711_Cart & checkout_6441078b72a7c53c78f70590",
      "nodeName": "Website Jan 2024 Release_Cart & checkout",
      "path": [
        "Cart & checkout_6441078b72a7c53c78f70590###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Cart & checkout_6441078b72a7c53c78f70590"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-27T00:00:00.000Z",
      "releaseStartDate": "2023-10-02T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "04863281-aa7c-4e54-b482-eb942a898afd_KYC _63e5fea5ae1aeb593f3395aa",
      "nodeName": "Sprint 26_KYC ",
      "sprintStartDate": "2023-11-20T00:00:00.000Z",
      "sprintEndDate": "2023-12-08T00:00:00.000Z",
      "path": [
        "KYC _63e5fea5ae1aeb593f3395aa###ENI-Evolutions_port###ENI S.p.A_acc###Energy & Commodities_ver###EU_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "KYC _63e5fea5ae1aeb593f3395aa"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "3365_Sonepar SAF_6542b3da08f31814845119a2",
      "nodeName": "Search & Products 14.5_Sonepar SAF",
      "sprintStartDate": "2023-11-09T14:56:54.046Z",
      "sprintEndDate": "2023-11-23T14:55:07.000Z",
      "path": [
        "Sonepar SAF_6542b3da08f31814845119a2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar SAF_6542b3da08f31814845119a2"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "15588_DRP - HomePage POD_64b3f315c4e72b57c94035e2",
      "nodeName": "Ironman_DRP - HomePage POD",
      "path": [
        "DRP - HomePage POD_64b3f315c4e72b57c94035e2###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "DRP - HomePage POD_64b3f315c4e72b57c94035e2"
      ],
      "level": 6,
      "releaseEndDate": "2022-06-29T00:00:00.000Z",
      "releaseStartDate": "2022-06-09T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "4542527e-93c7-45d2-acd2-e913a220e9b0_KYC _63e5fea5ae1aeb593f3395aa",
      "nodeName": "Sprint 24_KYC ",
      "sprintStartDate": "2023-10-09T00:00:00.000Z",
      "sprintEndDate": "2023-10-27T00:00:00.000Z",
      "path": [
        "KYC _63e5fea5ae1aeb593f3395aa###ENI-Evolutions_port###ENI S.p.A_acc###Energy & Commodities_ver###EU_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "KYC _63e5fea5ae1aeb593f3395aa"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "13572_Ecom Post-Purchase Squad_64be67caeb7015715615c4c5",
      "nodeName": "SBRWEB 1.7_Ecom Post-Purchase Squad",
      "path": [
        "Ecom Post-Purchase Squad_64be67caeb7015715615c4c5###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Ecom Post-Purchase Squad_64be67caeb7015715615c4c5"
      ],
      "level": 6,
      "releaseEndDate": "2024-02-07T00:00:00.000Z",
      "releaseStartDate": "2023-08-16T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "285_PSknowHOW _6527af981704342160f43748",
      "nodeName": "KnowHOW | PI_15| ITR_3_PSknowHOW ",
      "sprintStartDate": "2023-10-25T08:39:36.159Z",
      "sprintEndDate": "2023-11-07T08:20:00.000Z",
      "path": [
        "PSknowHOW _6527af981704342160f43748###DTS_port###Methods and Tools_acc###PS Internal_ver###Internal_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "PSknowHOW _6527af981704342160f43748"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "133810_pod16 2_657219fd78247c3cd726d630",
      "nodeName": "Release 23.03.4_pod16 2",
      "path": [
        "pod16 2_657219fd78247c3cd726d630###2021 WLP Brand Retainer_port###AAA Auto Club Group_acc###Automative1_ver###A_bu"
      ],
      "labelName": "release",
      "parentId": [
        "pod16 2_657219fd78247c3cd726d630"
      ],
      "level": 6,
      "releaseEndDate": "2023-03-22T00:00:00.000Z",
      "releaseStartDate": "2023-03-13T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2734_SAME_63a4810c09378702f4eab210",
      "nodeName": "Sprint 98_SAME",
      "sprintStartDate": "2023-09-12T14:31:34.243Z",
      "sprintEndDate": "2023-09-26T12:22:00.000Z",
      "path": [
        "SAME_63a4810c09378702f4eab210###ACE20001_port###Abu Dhabi Investment Authority_acc###PS Internal_ver###EU_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "SAME_63a4810c09378702f4eab210"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2278_Data Visualization_64425a0a30d86a7f539c7fdc",
      "nodeName": "DV Sprint 23.22_Data Visualization",
      "sprintStartDate": "2023-10-25T10:59:07.190Z",
      "sprintEndDate": "2023-11-08T03:30:00.000Z",
      "path": [
        "Data Visualization_64425a0a30d86a7f539c7fdc###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Data Visualization_64425a0a30d86a7f539c7fdc"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2291_PIM_64a4e09e1734471c30843fc2",
      "nodeName": "PIM_Oct_23_Sprint_42_PIM",
      "sprintStartDate": "2023-09-04T14:04:45.501Z",
      "sprintEndDate": "2023-10-02T14:04:00.000Z",
      "path": [
        "PIM_64a4e09e1734471c30843fc2###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "PIM_64a4e09e1734471c30843fc2"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "HomepageEcom_sqd_64b3f315c4e72b57c94035e2",
      "nodeName": "HomepageEcom",
      "path": "1006_DRP - HomePage POD_64b3f315c4e72b57c94035e2###DRP - HomePage POD_64b3f315c4e72b57c94035e2###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "1006_DRP - HomePage POD_64b3f315c4e72b57c94035e2",
      "level": 7
    },
    {
      "nodeId": "10032_PSknowHOW _6527af981704342160f43748",
      "nodeName": "KnowHOW PI-12_PSknowHOW ",
      "path": [
        "PSknowHOW _6527af981704342160f43748###DTS_port###Methods and Tools_acc###PS Internal_ver###Internal_bu"
      ],
      "labelName": "release",
      "parentId": [
        "PSknowHOW _6527af981704342160f43748"
      ],
      "level": 6,
      "releaseEndDate": "2023-03-28T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2323_VDOS_63777558175a953a0a49d363",
      "nodeName": "OMS_Sprint 23.20_VDOS",
      "sprintStartDate": "2023-09-28T12:04:38.169Z",
      "sprintEndDate": "2023-10-11T04:00:00.000Z",
      "path": [
        "VDOS_63777558175a953a0a49d363###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "VDOS_63777558175a953a0a49d363"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2366_R1+ Sales_648ab46f6803f300a9fd2e09",
      "nodeName": "SALES_Sprint 23.24_R1+ Sales",
      "sprintStartDate": "2023-11-22T18:17:58.305Z",
      "sprintEndDate": "2023-12-06T04:00:00.000Z",
      "path": [
        "R1+ Sales_648ab46f6803f300a9fd2e09###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "R1+ Sales_648ab46f6803f300a9fd2e09"
      ],
      "sprintState": "active",
      "level": 6
    },
    {
      "nodeId": "13606_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "nodeName": "SBRWEB 2.2 _Dotcom + Mobile App",
      "path": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba"
      ],
      "level": 6,
      "releaseEndDate": "2024-03-20T00:00:00.000Z",
      "releaseStartDate": "2023-11-08T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "1063_Data Visualization_64425a0a30d86a7f539c7fdc",
      "nodeName": "SBR.COM_Sprint 23.12_Data Visualization",
      "sprintStartDate": "2023-06-07T16:25:18.142Z",
      "sprintEndDate": "2023-06-20T21:00:00.000Z",
      "path": [
        "Data Visualization_64425a0a30d86a7f539c7fdc###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Data Visualization_64425a0a30d86a7f539c7fdc"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "150500_pod16 2_657219fd78247c3cd726d630",
      "nodeName": "Release_Nov2023_pod16 2",
      "path": [
        "pod16 2_657219fd78247c3cd726d630###2021 WLP Brand Retainer_port###AAA Auto Club Group_acc###Automative1_ver###A_bu"
      ],
      "labelName": "release",
      "parentId": [
        "pod16 2_657219fd78247c3cd726d630"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-30T00:00:00.000Z",
      "releaseStartDate": "2023-11-01T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "10425_Mobile App_637b17b9175a953a0a49d3c2",
      "nodeName": "Release 3.4.0_Mobile App",
      "path": [
        "Mobile App_637b17b9175a953a0a49d3c2###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Mobile App_637b17b9175a953a0a49d3c2"
      ],
      "level": 6,
      "releaseEndDate": "2023-08-16T00:00:00.000Z",
      "releaseStartDate": "2023-06-20T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "1114_Mobile App_637b17b9175a953a0a49d3c2",
      "nodeName": "MA_Sprint 23.12_Mobile App",
      "sprintStartDate": "2023-06-07T16:27:55.524Z",
      "sprintEndDate": "2023-06-20T22:00:00.000Z",
      "path": [
        "Mobile App_637b17b9175a953a0a49d3c2###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Mobile App_637b17b9175a953a0a49d3c2"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2206_Onsite search_644131b98b61fa2477214bf3",
      "nodeName": "Cart&Checkout July07 2023 Rel7_Onsite search",
      "sprintStartDate": "2023-06-12T14:10:22.861Z",
      "sprintEndDate": "2023-07-09T14:43:00.000Z",
      "path": [
        "Onsite search_644131b98b61fa2477214bf3###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Onsite search_644131b98b61fa2477214bf3"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "Automotive_ver",
      "nodeName": "Automotive",
      "path": "North America_bu",
      "labelName": "ver",
      "parentId": "North America_bu",
      "level": 2
    },
    {
      "nodeId": "Sonepar BUY_6542b42308f31814845119a8",
      "nodeName": "Sonepar BUY",
      "path": "Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Sonepar SAS_port",
      "level": 5,
      "basicProjectConfigId": "6542b42308f31814845119a8"
    },
    {
      "nodeId": "45164_MAP_63a304a909378702f4eab1d0",
      "nodeName": "KnowHOW | PI_14| ITR_5_MAP",
      "sprintStartDate": "2023-08-23T05:41:00.000Z",
      "sprintEndDate": "2023-09-05T05:41:00.000Z",
      "path": [
        "MAP_63a304a909378702f4eab1d0###DTS_port###Methods and Tools_acc###PS Internal_ver###Internal_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "MAP_63a304a909378702f4eab1d0"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "1989_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "Q2 2022_RMMO",
      "sprintStartDate": "2022-04-01T06:12:41.885Z",
      "sprintEndDate": "2022-06-30T06:12:00.000Z",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "11594_Sonepar AFS_654135db88c4b8114af77dba",
      "nodeName": "BE next RC AFS_Sonepar AFS",
      "path": [
        "Sonepar AFS_654135db88c4b8114af77dba###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar AFS_654135db88c4b8114af77dba"
      ],
      "level": 6,
      "releaseEndDate": "2023-08-15T00:00:00.000Z",
      "releaseStartDate": "2023-07-24T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "11773_Sonepar BUY_6542b42308f31814845119a8",
      "nodeName": "V1.389 BE B&D_Sonepar BUY",
      "path": [
        "Sonepar BUY_6542b42308f31814845119a8###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar BUY_6542b42308f31814845119a8"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-23T00:00:00.000Z",
      "releaseStartDate": "2023-10-18T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "2309_Website TOF_6441052372a7c53c78f70588",
      "nodeName": "Search_Oct27_2023.Rel11_Website TOF",
      "sprintStartDate": "2023-10-02T21:54:41.265Z",
      "sprintEndDate": "2023-10-29T22:43:00.000Z",
      "path": [
        "Website TOF_6441052372a7c53c78f70588###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Website TOF_6441052372a7c53c78f70588"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "11723_Sonepar BUY_6542b42308f31814845119a8",
      "nodeName": "DFS-PI15_Sonepar BUY",
      "path": [
        "Sonepar BUY_6542b42308f31814845119a8###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar BUY_6542b42308f31814845119a8"
      ],
      "level": 6,
      "releaseEndDate": "2024-03-16T00:00:00.000Z",
      "releaseStartDate": "2023-12-06T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "10176_Test rc_657056c668a1225c0126c843",
      "nodeName": "KnowHOW v8.3.0_Test rc",
      "path": [
        "Test rc_657056c668a1225c0126c843###2021 WLP Brand Retainer_port###AB Tetra Pak_acc###Consumer Products_ver###Europe_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Test rc_657056c668a1225c0126c843"
      ],
      "level": 6,
      "releaseEndDate": "2024-01-05T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "3242_Sonepar Cloud_6542b82208f31814845119bb",
      "nodeName": "Cloud - S 14.2 / 74_Sonepar Cloud",
      "sprintStartDate": "2023-09-28T09:50:05.064Z",
      "sprintEndDate": "2023-10-12T09:34:22.000Z",
      "path": [
        "Sonepar Cloud_6542b82208f31814845119bb###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar Cloud_6542b82208f31814845119bb"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "1116_Website BOF_644108c072a7c53c78f7059a",
      "nodeName": "Web_Payment_Apr14_2023_Rel04_Website BOF",
      "sprintStartDate": "2023-03-21T12:54:57.346Z",
      "sprintEndDate": "2023-04-18T00:41:00.000Z",
      "path": [
        "Website BOF_644108c072a7c53c78f7059a###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Website BOF_644108c072a7c53c78f7059a"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "3193_Sonepar EAC_6542b3b008f31814845119a0",
      "nodeName": "Engage 14.3_Sonepar EAC",
      "sprintStartDate": "2023-10-12T08:29:52.476Z",
      "sprintEndDate": "2023-10-26T14:17:00.000Z",
      "path": [
        "Sonepar EAC_6542b3b008f31814845119a0###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar EAC_6542b3b008f31814845119a0"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "12491_Unified Commerce - Dan's MVP_64ab97327d51263c17602b58",
      "nodeName": "DanMVP1.0_UAT_Unified Commerce - Dan's MVP",
      "path": [
        "Unified Commerce - Dan's MVP_64ab97327d51263c17602b58###Endeavour Group Pty Ltd_port###Endeavour Group Limited_acc###Retail_ver###International_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Unified Commerce - Dan's MVP_64ab97327d51263c17602b58"
      ],
      "level": 6,
      "releaseEndDate": "2024-02-02T00:00:00.000Z",
      "releaseStartDate": "2023-04-05T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "40654_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.14.Hotfix iOS_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-09-29T00:00:00.000Z",
      "releaseStartDate": "2023-09-18T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "11643_Sonepar INT_6542b4bb08f31814845119b2",
      "nodeName": "[USA-SPRFLD] Spark PREPROD Available_Sonepar INT",
      "path": [
        "Sonepar INT_6542b4bb08f31814845119b2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar INT_6542b4bb08f31814845119b2"
      ],
      "level": 6,
      "releaseEndDate": "2023-04-26T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "40075_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.13.2180 Android_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-08-03T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2297_Website TOF_6441052372a7c53c78f70588",
      "nodeName": "Promo_Engine_TOF_Sep_1_Website TOF",
      "sprintStartDate": "2023-08-07T21:54:42.268Z",
      "sprintEndDate": "2023-09-01T17:01:00.000Z",
      "path": [
        "Website TOF_6441052372a7c53c78f70588###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Website TOF_6441052372a7c53c78f70588"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Salesforce_sqd_6377306a175a953a0a49d322",
      "nodeName": "Salesforce",
      "path": [
        "2384_Integration Services_6377306a175a953a0a49d322###Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2320_Integration Services_6377306a175a953a0a49d322###Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2287_Integration Services_6377306a175a953a0a49d322###Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2377_Integration Services_6377306a175a953a0a49d322###Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2356_Integration Services_6377306a175a953a0a49d322###Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2406_Integration Services_6377306a175a953a0a49d322###Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sqd",
      "parentId": [
        "2384_Integration Services_6377306a175a953a0a49d322",
        "2320_Integration Services_6377306a175a953a0a49d322",
        "2287_Integration Services_6377306a175a953a0a49d322",
        "2377_Integration Services_6377306a175a953a0a49d322",
        "2356_Integration Services_6377306a175a953a0a49d322",
        "2406_Integration Services_6377306a175a953a0a49d322"
      ],
      "level": 7
    },
    {
      "nodeId": "2267_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "nodeName": "ECOM_Sprint 23.23_Dotcom + Mobile App",
      "sprintStartDate": "2023-11-08T21:44:08.000Z",
      "sprintEndDate": "2023-11-21T21:00:00.000Z",
      "path": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "54354_Do it Best_657049b505ce0569d5612ec7",
      "nodeName": "Do it Best - EO Sprint V2_Do it Best",
      "sprintStartDate": "2023-11-15T22:14:00.000Z",
      "sprintEndDate": "2023-11-28T22:14:00.000Z",
      "path": [
        "Do it Best_657049b505ce0569d5612ec7###3PP CRM_port###ADEO_acc###Automotive_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Do it Best_657049b505ce0569d5612ec7"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2228_GearBox Squad 3_64770ef45286e83998a56143",
      "nodeName": "GB_Sprint 23.19_GearBox Squad 3",
      "sprintStartDate": "2023-09-13T05:00:25.796Z",
      "sprintEndDate": "2023-09-27T03:30:00.000Z",
      "path": [
        "GearBox Squad 3_64770ef45286e83998a56143###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "GearBox Squad 3_64770ef45286e83998a56143"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2229_GearBox Squad 3_64770ef45286e83998a56143",
      "nodeName": "GB_Sprint 23.20_GearBox Squad 3",
      "sprintStartDate": "2023-09-27T05:00:43.565Z",
      "sprintEndDate": "2023-10-11T03:30:00.000Z",
      "path": [
        "GearBox Squad 3_64770ef45286e83998a56143###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "GearBox Squad 3_64770ef45286e83998a56143"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2349_CMS_644103e772a7c53c78f70582",
      "nodeName": "CMS_Dec 22_2023_RelXX_CMS",
      "sprintStartDate": "2023-11-27T17:06:25.000Z",
      "sprintEndDate": "2023-12-25T17:54:00.000Z",
      "path": [
        "CMS_644103e772a7c53c78f70582###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "CMS_644103e772a7c53c78f70582"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "Team 3_sqd_64770ef45286e83998a56143",
      "nodeName": "Team 3",
      "path": "2369_GearBox Squad 3_64770ef45286e83998a56143###GearBox Squad 3_64770ef45286e83998a56143###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2369_GearBox Squad 3_64770ef45286e83998a56143",
      "level": 7
    },
    {
      "nodeId": "13557_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "nodeName": "SBRAPP 3.5_Dotcom + Mobile App",
      "path": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba"
      ],
      "level": 6,
      "releaseEndDate": "2024-01-31T00:00:00.000Z",
      "releaseStartDate": "2023-10-25T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "1096bad3-020e-44f9-b21c-92fa8cec037a_KYC _63e5fea5ae1aeb593f3395aa",
      "nodeName": "Sprint 25_KYC ",
      "sprintStartDate": "2023-10-30T00:00:00.000Z",
      "sprintEndDate": "2023-11-17T00:00:00.000Z",
      "path": [
        "KYC _63e5fea5ae1aeb593f3395aa###ENI-Evolutions_port###ENI S.p.A_acc###Energy & Commodities_ver###EU_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "KYC _63e5fea5ae1aeb593f3395aa"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "13526_VDOS_63777558175a953a0a49d363",
      "nodeName": "LOGISTICS_PILOT_VERSION 2_VDOS",
      "path": [
        "VDOS_63777558175a953a0a49d363###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "VDOS_63777558175a953a0a49d363"
      ],
      "level": 6,
      "releaseEndDate": "2024-04-05T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "TestConn_6571528eeed1d93352754ba4",
      "nodeName": "TestConn",
      "path": "3PP CRM_port###AAA Auto Club Group_acc###Automative1_ver###EU_bu",
      "labelName": "project",
      "parentId": "3PP CRM_port",
      "level": 5,
      "basicProjectConfigId": "6571528eeed1d93352754ba4"
    },
    {
      "nodeId": "53771_pod16 2_657219fd78247c3cd726d630",
      "nodeName": "POD 16 | Sprint 23_16_pod16 2",
      "sprintStartDate": "2023-10-26T05:00:00.000Z",
      "sprintEndDate": "2023-11-08T13:00:00.000Z",
      "path": [
        "pod16 2_657219fd78247c3cd726d630###2021 WLP Brand Retainer_port###AAA Auto Club Group_acc###Automative1_ver###A_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "pod16 2_657219fd78247c3cd726d630"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2393_VDOS Translations_651fe6bb1704342160f43511",
      "nodeName": "OMS_Sprint 23.23_VDOS Translations",
      "sprintStartDate": "2023-11-09T13:38:16.954Z",
      "sprintEndDate": "2023-11-21T05:00:00.000Z",
      "path": [
        "VDOS Translations_651fe6bb1704342160f43511###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "VDOS Translations_651fe6bb1704342160f43511"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "3343_Sonepar Cloud_6542b82208f31814845119bb",
      "nodeName": "Cloud - S 14.5 / 77_Sonepar Cloud",
      "sprintStartDate": "2023-11-09T10:28:35.085Z",
      "sprintEndDate": "2023-11-23T10:05:34.000Z",
      "path": [
        "Sonepar Cloud_6542b82208f31814845119bb###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar Cloud_6542b82208f31814845119bb"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "11830_Sonepar SAF_6542b3da08f31814845119a2",
      "nodeName": "v1.408 FE_Sonepar SAF",
      "path": [
        "Sonepar SAF_6542b3da08f31814845119a2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar SAF_6542b3da08f31814845119a2"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-24T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Released"
    },
    {
      "nodeId": "45282_MAP_63a304a909378702f4eab1d0",
      "nodeName": "MAP|PI_14|ITR_5_MAP",
      "sprintStartDate": "2023-08-23T07:03:00.000Z",
      "sprintEndDate": "2023-09-05T18:03:00.000Z",
      "path": [
        "MAP_63a304a909378702f4eab1d0###DTS_port###Methods and Tools_acc###PS Internal_ver###Internal_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "MAP_63a304a909378702f4eab1d0"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2285_Website BOF_644108c072a7c53c78f7059a",
      "nodeName": "CMS_Sep29_2023Rel.10_Website BOF",
      "sprintStartDate": "2023-09-05T14:03:22.074Z",
      "sprintEndDate": "2023-10-02T15:53:00.000Z",
      "path": [
        "Website BOF_644108c072a7c53c78f7059a###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Website BOF_644108c072a7c53c78f7059a"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "3268_Sonepar BUY_6542b42308f31814845119a8",
      "nodeName": "B&D - 14.5_Sonepar BUY",
      "sprintStartDate": "2023-11-09T16:17:32.406Z",
      "sprintEndDate": "2023-11-22T17:02:00.000Z",
      "path": [
        "Sonepar BUY_6542b42308f31814845119a8###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar BUY_6542b42308f31814845119a8"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "pod16 2_657219fd78247c3cd726d630",
      "nodeName": "pod16 2",
      "path": "2021 WLP Brand Retainer_port###AAA Auto Club Group_acc###Automative1_ver###A_bu",
      "labelName": "project",
      "parentId": "2021 WLP Brand Retainer_port",
      "level": 5,
      "basicProjectConfigId": "657219fd78247c3cd726d630"
    },
    {
      "nodeId": "13527_VDOS_63777558175a953a0a49d363",
      "nodeName": "LOGISTICS_PILOT 2_VDOS",
      "path": [
        "VDOS_63777558175a953a0a49d363###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "VDOS_63777558175a953a0a49d363"
      ],
      "level": 6,
      "releaseEndDate": "2024-03-15T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "151508_Do it Best_657049b505ce0569d5612ec7",
      "nodeName": "feaem_hotfix_rv1.4_p1_Do it Best",
      "path": [
        "Do it Best_657049b505ce0569d5612ec7###3PP CRM_port###ADEO_acc###Automotive_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Do it Best_657049b505ce0569d5612ec7"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-02T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Released"
    },
    {
      "nodeId": "13603_GearBox Squad 2_64770ec45286e83998a56141",
      "nodeName": "GB GearboxMobileAPI V3_GearBox Squad 2",
      "path": [
        "GearBox Squad 2_64770ec45286e83998a56141###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "GearBox Squad 2_64770ec45286e83998a56141"
      ],
      "level": 6,
      "releaseEndDate": "2023-12-05T00:00:00.000Z",
      "releaseStartDate": "2023-09-11T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "40124_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.13.2183 Android_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-08-09T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "1115_Website BOF_644108c072a7c53c78f7059a",
      "nodeName": "Web_Payment_Mar17_2023_Rel03_Website BOF",
      "sprintStartDate": "2023-02-19T18:13:00.000Z",
      "sprintEndDate": "2023-03-20T06:00:00.000Z",
      "path": [
        "Website BOF_644108c072a7c53c78f7059a###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Website BOF_644108c072a7c53c78f7059a"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "45279_MAP_63a304a909378702f4eab1d0",
      "nodeName": "MAP|PI_14|ITR_2_MAP",
      "sprintStartDate": "2023-07-12T07:03:00.000Z",
      "sprintEndDate": "2023-07-25T18:03:00.000Z",
      "path": [
        "MAP_63a304a909378702f4eab1d0###DTS_port###Methods and Tools_acc###PS Internal_ver###Internal_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "MAP_63a304a909378702f4eab1d0"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "10160_Test rc_657056c668a1225c0126c843",
      "nodeName": "KnowHOW PI-15_Test rc",
      "path": [
        "Test rc_657056c668a1225c0126c843###2021 WLP Brand Retainer_port###AB Tetra Pak_acc###Consumer Products_ver###Europe_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Test rc_657056c668a1225c0126c843"
      ],
      "level": 6,
      "releaseEndDate": "2024-01-02T00:00:00.000Z",
      "releaseStartDate": "2023-09-27T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "3194_Sonepar EAC_6542b3b008f31814845119a0",
      "nodeName": "Engage 14.4_Sonepar EAC",
      "sprintStartDate": "2023-10-26T08:27:38.196Z",
      "sprintEndDate": "2023-11-09T15:18:00.000Z",
      "path": [
        "Sonepar EAC_6542b3b008f31814845119a0###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar EAC_6542b3b008f31814845119a0"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "302_PSknowHOW _6527af981704342160f43748",
      "nodeName": "KnowHOW | PI_15| ITR_6_PSknowHOW ",
      "sprintStartDate": "2023-12-06T06:20:52.058Z",
      "sprintEndDate": "2023-12-26T08:28:00.000Z",
      "path": [
        "PSknowHOW _6527af981704342160f43748###DTS_port###Methods and Tools_acc###PS Internal_ver###Internal_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "PSknowHOW _6527af981704342160f43748"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "13604_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "nodeName": "SBRWEB 2.0_Dotcom + Mobile App",
      "path": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba"
      ],
      "level": 6,
      "releaseEndDate": "2024-02-15T00:00:00.000Z",
      "releaseStartDate": "2023-08-02T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "13604_Ecom Pre-Purchase Squad_64be66e3eb7015715615c4bd",
      "nodeName": "SBRWEB 2.0_Ecom Pre-Purchase Squad",
      "path": [
        "Ecom Pre-Purchase Squad_64be66e3eb7015715615c4bd###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Ecom Pre-Purchase Squad_64be66e3eb7015715615c4bd"
      ],
      "level": 6,
      "releaseEndDate": "2024-02-15T00:00:00.000Z",
      "releaseStartDate": "2023-08-02T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "11770_Sonepar MAP_6542b43f08f31814845119ab",
      "nodeName": "v1.395 BE M&P_Sonepar MAP",
      "path": [
        "Sonepar MAP_6542b43f08f31814845119ab###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar MAP_6542b43f08f31814845119ab"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-31T00:00:00.000Z",
      "releaseStartDate": "2023-10-18T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "2315_Promotion Engine TOF_644105f972a7c53c78f7058c",
      "nodeName": "Promo_Engine_TOF_Sep_29_Promotion Engine TOF",
      "sprintStartDate": "2023-09-05T00:54:33.679Z",
      "sprintEndDate": "2023-09-29T17:01:00.000Z",
      "path": [
        "Promotion Engine TOF_644105f972a7c53c78f7058c###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Promotion Engine TOF_644105f972a7c53c78f7058c"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Test rc_657056c668a1225c0126c843",
      "nodeName": "Test rc",
      "path": "2021 WLP Brand Retainer_port###AB Tetra Pak_acc###Consumer Products_ver###Europe_bu",
      "labelName": "project",
      "parentId": "2021 WLP Brand Retainer_port",
      "level": 5,
      "basicProjectConfigId": "657056c668a1225c0126c843"
    },
    {
      "nodeId": "54879_Do it Best_657049b505ce0569d5612ec7",
      "nodeName": "Do it Best - Sprint V1.7_Do it Best",
      "sprintStartDate": "2023-11-29T09:02:00.000Z",
      "sprintEndDate": "2023-12-19T09:02:00.000Z",
      "path": [
        "Do it Best_657049b505ce0569d5612ec7###3PP CRM_port###ADEO_acc###Automotive_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Do it Best_657049b505ce0569d5612ec7"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "3184_Sonepar Mobile App_6448a96c3be37902a3f1ba48",
      "nodeName": "Mobile - S14.5_Sonepar Mobile App",
      "sprintStartDate": "2023-11-09T14:59:44.420Z",
      "sprintEndDate": "2023-11-23T14:59:41.000Z",
      "path": [
        "Sonepar Mobile App_6448a96c3be37902a3f1ba48###Sonepar Client Cost - MC_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar Mobile App_6448a96c3be37902a3f1ba48"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "10226_VDOS Outside Hauler_647702b25286e83998a56138",
      "nodeName": "3.0_Rel 1 (BY)_VDOS Outside Hauler",
      "path": [
        "VDOS Outside Hauler_647702b25286e83998a56138###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "VDOS Outside Hauler_647702b25286e83998a56138"
      ],
      "level": 6,
      "releaseEndDate": "2022-05-31T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "3337_Design System_64ad9e667d51263c17602c67",
      "nodeName": "WATTS 14.3_Design System",
      "sprintStartDate": "2023-10-13T07:34:52.436Z",
      "sprintEndDate": "2023-10-24T22:00:00.000Z",
      "path": [
        "Design System_64ad9e667d51263c17602c67###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Design System_64ad9e667d51263c17602c67"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "3203_Sonepar eProcurement_6542947f08f3181484511988",
      "nodeName": "Eproc 14.1_Sonepar eProcurement",
      "sprintStartDate": "2023-09-15T14:54:41.604Z",
      "sprintEndDate": "2023-09-28T15:41:53.000Z",
      "path": [
        "Sonepar eProcurement_6542947f08f3181484511988###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar eProcurement_6542947f08f3181484511988"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "GearBox Squad 3_64770ef45286e83998a56143",
      "nodeName": "GearBox Squad 3",
      "path": "Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Sunbelt Rentals_port",
      "level": 5,
      "basicProjectConfigId": "64770ef45286e83998a56143"
    },
    {
      "nodeId": "Nissan Motor Co. Ltd._acc",
      "nodeName": "Nissan Motor Co. Ltd.",
      "path": "Automotive_ver###International_bu",
      "labelName": "acc",
      "parentId": "Automotive_ver",
      "level": 3
    },
    {
      "nodeId": "2377_Integration Services_6377306a175a953a0a49d322",
      "nodeName": "IS_Sprint 23.22_Integration Services",
      "sprintStartDate": "2023-10-25T12:00:40.159Z",
      "sprintEndDate": "2023-11-08T12:00:00.000Z",
      "path": [
        "Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Integration Services_6377306a175a953a0a49d322"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "1006_DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "nodeName": "DRP Sprint 82_DRP - Discovery POD",
      "sprintStartDate": "2023-08-31T12:31:00.986Z",
      "sprintEndDate": "2023-09-21T16:37:00.000Z",
      "path": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "3347_Sonepar SRE_6542b86f08f31814845119be",
      "nodeName": "OPS-S 14.3 / 75_Sonepar SRE",
      "sprintStartDate": "2023-10-12T13:16:53.083Z",
      "sprintEndDate": "2023-10-26T13:15:00.000Z",
      "path": [
        "Sonepar SRE_6542b86f08f31814845119be###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar SRE_6542b86f08f31814845119be"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2273_Data Engineering_644258b830d86a7f539c7fd7",
      "nodeName": "DEA Sprint 23.20_Data Engineering",
      "sprintStartDate": "2023-09-27T04:44:51.667Z",
      "sprintEndDate": "2023-10-11T03:30:00.000Z",
      "path": [
        "Data Engineering_644258b830d86a7f539c7fd7###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Data Engineering_644258b830d86a7f539c7fd7"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Salesforce_sqd_6377306a175a953a0a49d322",
      "nodeName": "Salesforce",
      "path": "2320_Integration Services_6377306a175a953a0a49d322###Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2320_Integration Services_6377306a175a953a0a49d322",
      "level": 7
    },
    {
      "nodeId": "11854_Sonepar MAP_6542b43f08f31814845119ab",
      "nodeName": "v1.399.1 BE Hotfix AFS_Sonepar MAP",
      "path": [
        "Sonepar MAP_6542b43f08f31814845119ab###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar MAP_6542b43f08f31814845119ab"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-30T00:00:00.000Z",
      "releaseStartDate": "2023-11-30T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "38786_RABO Scrum (New)_64a5dac31734471c30844068",
      "nodeName": "dotcom-2023-11-23_RABO Scrum (New)",
      "path": [
        "RABO Scrum (New)_64a5dac31734471c30844068###AA_port###ADEO_acc###Financial Services_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RABO Scrum (New)_64a5dac31734471c30844068"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-23T00:00:00.000Z",
      "releaseStartDate": "2023-11-09T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "AAA Auto Club Group_acc",
      "nodeName": "AAA Auto Club Group",
      "path": "Consumer Products_ver###EU_bu",
      "labelName": "acc",
      "parentId": "Consumer Products_ver",
      "level": 3
    },
    {
      "nodeId": "2317_Promotion Engine TOF_644105f972a7c53c78f7058c",
      "nodeName": "Promo_Engine_BOF_Sep_29_Promotion Engine TOF",
      "sprintStartDate": "2023-09-05T00:51:14.636Z",
      "sprintEndDate": "2023-09-29T13:48:00.000Z",
      "path": [
        "Promotion Engine TOF_644105f972a7c53c78f7058c###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Promotion Engine TOF_644105f972a7c53c78f7058c"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "13598_GearBox Squad 1_6449103b3be37902a3f1ba70",
      "nodeName": "GB Dashboard Hotfix 23.03.02_GearBox Squad 1",
      "path": [
        "GearBox Squad 1_6449103b3be37902a3f1ba70###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "GearBox Squad 1_6449103b3be37902a3f1ba70"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-07T00:00:00.000Z",
      "releaseStartDate": "2023-10-25T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "My Account_6441081a72a7c53c78f70595",
      "nodeName": "My Account",
      "path": "API_port###Academy Sports_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "API_port",
      "level": 5,
      "basicProjectConfigId": "6441081a72a7c53c78f70595"
    },
    {
      "nodeId": "2315_Website TOF_6441052372a7c53c78f70588",
      "nodeName": "Promo_Engine_TOF_Sep_29_Website TOF",
      "sprintStartDate": "2023-09-05T00:54:33.679Z",
      "sprintEndDate": "2023-09-29T17:01:00.000Z",
      "path": [
        "Website TOF_6441052372a7c53c78f70588###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Website TOF_6441052372a7c53c78f70588"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Team 1_sqd_63a02b61bbc09e116d744d9d",
      "nodeName": "Team 1",
      "path": "2317_GearBox_63a02b61bbc09e116d744d9d###GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2317_GearBox_63a02b61bbc09e116d744d9d",
      "level": 7
    },
    {
      "nodeId": "3183_Sonepar Mobile App_6448a96c3be37902a3f1ba48",
      "nodeName": "Mobile - S14.4_Sonepar Mobile App",
      "sprintStartDate": "2023-10-26T14:17:34.168Z",
      "sprintEndDate": "2023-11-09T14:17:28.000Z",
      "path": [
        "Sonepar Mobile App_6448a96c3be37902a3f1ba48###Sonepar Client Cost - MC_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar Mobile App_6448a96c3be37902a3f1ba48"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "11652_Sonepar INT_6542b4bb08f31814845119b2",
      "nodeName": "[USA-SPRFLD] Stocks Ready_Sonepar INT",
      "path": [
        "Sonepar INT_6542b4bb08f31814845119b2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar INT_6542b4bb08f31814845119b2"
      ],
      "level": 6,
      "releaseEndDate": "2023-06-21T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Automotive_ver",
      "nodeName": "Automotive",
      "path": "EU_bu",
      "labelName": "ver",
      "parentId": "EU_bu",
      "level": 2
    },
    {
      "nodeId": "11711_Website TOF_6441052372a7c53c78f70588",
      "nodeName": "Website Jan 2024 Release_Website TOF",
      "path": [
        "Website TOF_6441052372a7c53c78f70588###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Website TOF_6441052372a7c53c78f70588"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-27T00:00:00.000Z",
      "releaseStartDate": "2023-10-02T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "KN Server_656969922d6d5f774de2e686",
      "nodeName": "KN Server",
      "path": "2021 WLP Brand Retainer_port###ADNOC Global Trading Limited_acc###Consumer Products_ver###Government Services_bu",
      "labelName": "project",
      "parentId": "2021 WLP Brand Retainer_port",
      "level": 5,
      "basicProjectConfigId": "656969922d6d5f774de2e686"
    },
    {
      "nodeId": "2243_Data Engineering_644258b830d86a7f539c7fd7",
      "nodeName": "DA Sprint 23.19_Data Engineering",
      "sprintStartDate": "2023-09-13T04:19:34.495Z",
      "sprintEndDate": "2023-09-26T21:54:31.000Z",
      "path": [
        "Data Engineering_644258b830d86a7f539c7fd7###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Data Engineering_644258b830d86a7f539c7fd7"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "11594_Sonepar MAP_6542b43f08f31814845119ab",
      "nodeName": "BE next RC AFS_Sonepar MAP",
      "path": [
        "Sonepar MAP_6542b43f08f31814845119ab###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar MAP_6542b43f08f31814845119ab"
      ],
      "level": 6,
      "releaseEndDate": "2023-08-15T00:00:00.000Z",
      "releaseStartDate": "2023-07-24T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "3201_Sonepar MAP_6542b43f08f31814845119ab",
      "nodeName": "AFS - S14.5_Sonepar MAP",
      "sprintStartDate": "2023-11-09T16:30:42.172Z",
      "sprintEndDate": "2023-11-22T22:00:00.000Z",
      "path": [
        "Sonepar MAP_6542b43f08f31814845119ab###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar MAP_6542b43f08f31814845119ab"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "Sonepar Cloud_6542b82208f31814845119bb",
      "nodeName": "Sonepar Cloud",
      "path": "Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Sonepar SAS_port",
      "level": 5,
      "basicProjectConfigId": "6542b82208f31814845119bb"
    },
    {
      "nodeId": "Financial Services_ver",
      "nodeName": "Financial Services",
      "path": "EU_bu",
      "labelName": "ver",
      "parentId": "EU_bu",
      "level": 2
    },
    {
      "nodeId": "54035_Do it Best_657049b505ce0569d5612ec7",
      "nodeName": "Do it Best - Sprint V1.3_Do it Best",
      "sprintStartDate": "2023-09-21T13:02:00.000Z",
      "sprintEndDate": "2023-10-04T13:02:00.000Z",
      "path": [
        "Do it Best_657049b505ce0569d5612ec7###3PP CRM_port###ADEO_acc###Automotive_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Do it Best_657049b505ce0569d5612ec7"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "45280_MAP_63a304a909378702f4eab1d0",
      "nodeName": "MAP|PI_14|ITR_3_MAP",
      "sprintStartDate": "2023-07-26T07:03:00.000Z",
      "sprintEndDate": "2023-08-08T18:03:00.000Z",
      "path": [
        "MAP_63a304a909378702f4eab1d0###DTS_port###Methods and Tools_acc###PS Internal_ver###Internal_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "MAP_63a304a909378702f4eab1d0"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "3212_Sonepar MAP_6542b43f08f31814845119ab",
      "nodeName": "M&P - S14.4_Sonepar MAP",
      "sprintStartDate": "2023-10-26T08:34:15.000Z",
      "sprintEndDate": "2023-11-08T10:44:00.000Z",
      "path": [
        "Sonepar MAP_6542b43f08f31814845119ab###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar MAP_6542b43f08f31814845119ab"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "13565_GearBox_63a02b61bbc09e116d744d9d",
      "nodeName": "GB Dashboard 23.04_GearBox",
      "path": [
        "GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "GearBox_63a02b61bbc09e116d744d9d"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-29T00:00:00.000Z",
      "releaseStartDate": "2023-08-07T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "Sonepar SAF_6542b3da08f31814845119a2",
      "nodeName": "Sonepar SAF",
      "path": "Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Sonepar SAS_port",
      "level": 5,
      "basicProjectConfigId": "6542b3da08f31814845119a2"
    },
    {
      "nodeId": "Retail_ver",
      "nodeName": "Retail",
      "path": "North America_bu",
      "labelName": "ver",
      "parentId": "North America_bu",
      "level": 2
    },
    {
      "nodeId": "HomepageEcom_sqd_64b3f315c4e72b57c94035e2",
      "nodeName": "HomepageEcom",
      "path": "1008_DRP - HomePage POD_64b3f315c4e72b57c94035e2###DRP - HomePage POD_64b3f315c4e72b57c94035e2###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "1008_DRP - HomePage POD_64b3f315c4e72b57c94035e2",
      "level": 7
    },
    {
      "nodeId": "11819_Sonepar MAP_6542b43f08f31814845119ab",
      "nodeName": "v1.403 FE RC_Sonepar MAP",
      "path": [
        "Sonepar MAP_6542b43f08f31814845119ab###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar MAP_6542b43f08f31814845119ab"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-13T00:00:00.000Z",
      "releaseStartDate": "2023-11-06T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "3263_Sonepar SAF_6542b3da08f31814845119a2",
      "nodeName": "Search & Products 14.6_Sonepar SAF",
      "sprintStartDate": "2023-11-23T16:10:11.808Z",
      "sprintEndDate": "2023-12-07T16:10:06.000Z",
      "path": [
        "Sonepar SAF_6542b3da08f31814845119a2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar SAF_6542b3da08f31814845119a2"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "3182_Sonepar Mobile App_6448a96c3be37902a3f1ba48",
      "nodeName": "Mobile - S14.3_Sonepar Mobile App",
      "sprintStartDate": "2023-10-12T13:44:05.715Z",
      "sprintEndDate": "2023-10-26T13:43:59.000Z",
      "path": [
        "Sonepar Mobile App_6448a96c3be37902a3f1ba48###Sonepar Client Cost - MC_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar Mobile App_6448a96c3be37902a3f1ba48"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2304_Service & Assets_6494298bca84920b10dddd3b",
      "nodeName": "SERV_Sprint 23.23_Service & Assets",
      "sprintStartDate": "2023-11-08T05:00:32.856Z",
      "sprintEndDate": "2023-11-22T04:30:00.000Z",
      "path": [
        "Service & Assets_6494298bca84920b10dddd3b###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Service & Assets_6494298bca84920b10dddd3b"
      ],
      "sprintState": "closed",
      "level": 6
    },
    {
      "nodeId": "145850_pod16 2_657219fd78247c3cd726d630",
      "nodeName": "Release_Aug2023_pod16 2",
      "path": [
        "pod16 2_657219fd78247c3cd726d630###2021 WLP Brand Retainer_port###AAA Auto Club Group_acc###Automative1_ver###A_bu"
      ],
      "labelName": "release",
      "parentId": [
        "pod16 2_657219fd78247c3cd726d630"
      ],
      "level": 6,
      "releaseEndDate": "2023-08-31T00:00:00.000Z",
      "releaseStartDate": "2023-08-01T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "1160_CMS_644103e772a7c53c78f70582",
      "nodeName": "SEO_Jun9_2023_Rel6_CMS",
      "sprintStartDate": "2023-05-15T19:57:09.298Z",
      "sprintEndDate": "2023-06-11T19:53:00.000Z",
      "path": [
        "CMS_644103e772a7c53c78f70582###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "CMS_644103e772a7c53c78f70582"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "40031_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.13.2177 Android_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-08-04T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "DTS_port",
      "nodeName": "DTS",
      "path": "Methods and Tools_acc###PS Internal_ver###Internal_bu",
      "labelName": "port",
      "parentId": "Methods and Tools_acc",
      "level": 4
    },
    {
      "nodeId": "3PP - Cross Regional_port",
      "nodeName": "3PP - Cross Regional",
      "path": "AB Tetra Pak_acc###Consumer Products_ver###A_bu",
      "labelName": "port",
      "parentId": "AB Tetra Pak_acc",
      "level": 4
    },
    {
      "nodeId": "39849_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.12.1490 iOS_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-06-09T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Team 2_sqd_648ab6186803f300a9fd2e0e",
      "nodeName": "Team 2",
      "path": "2177_R1+ Logistics_648ab6186803f300a9fd2e0e###R1+ Logistics_648ab6186803f300a9fd2e0e###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2177_R1+ Logistics_648ab6186803f300a9fd2e0e",
      "level": 7
    },
    {
      "nodeId": "2265_My Account_6441081a72a7c53c78f70595",
      "nodeName": "SRE- July_My Account",
      "sprintStartDate": "2023-06-27T08:41:13.842Z",
      "sprintEndDate": "2023-07-31T06:58:00.000Z",
      "path": [
        "My Account_6441081a72a7c53c78f70595###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "My Account_6441081a72a7c53c78f70595"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "13553_Ecom Pre-Purchase Squad_64be66e3eb7015715615c4bd",
      "nodeName": "SBRWEB 1.6_Ecom Pre-Purchase Squad",
      "path": [
        "Ecom Pre-Purchase Squad_64be66e3eb7015715615c4bd###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Ecom Pre-Purchase Squad_64be66e3eb7015715615c4bd"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-29T00:00:00.000Z",
      "releaseStartDate": "2023-08-16T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "3207_Sonepar eProcurement_6542947f08f3181484511988",
      "nodeName": "Eproc 14.5_Sonepar eProcurement",
      "sprintStartDate": "2023-11-09T16:15:15.528Z",
      "sprintEndDate": "2023-11-22T16:59:55.000Z",
      "path": [
        "Sonepar eProcurement_6542947f08f3181484511988###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar eProcurement_6542947f08f3181484511988"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Post-Purchase Team_sqd_64be65cceb7015715615c4ba",
      "nodeName": "Post-Purchase Team",
      "path": [
        "2265_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2309_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2218_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sqd",
      "parentId": [
        "2265_Dotcom + Mobile App_64be65cceb7015715615c4ba",
        "2309_Dotcom + Mobile App_64be65cceb7015715615c4ba",
        "2218_Dotcom + Mobile App_64be65cceb7015715615c4ba"
      ],
      "level": 7
    },
    {
      "nodeId": "Data Engineering_644258b830d86a7f539c7fd7",
      "nodeName": "Data Engineering",
      "path": "Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Sunbelt Rentals_port",
      "level": 5,
      "basicProjectConfigId": "644258b830d86a7f539c7fd7"
    },
    {
      "nodeId": "11826_Sonepar Cloud_6542b82208f31814845119bb",
      "nodeName": "v1.404 BE LPS_Sonepar Cloud",
      "path": [
        "Sonepar Cloud_6542b82208f31814845119bb###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar Cloud_6542b82208f31814845119bb"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-13T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Released"
    },
    {
      "nodeId": "International_bu",
      "nodeName": "International",
      "path": "",
      "labelName": "bu",
      "level": 1
    },
    {
      "nodeId": "1177_ASO Mobile App_64a4fab01734471c30843fda",
      "nodeName": "MobileAppChk_01 Sep_ASO Mobile App",
      "sprintStartDate": "2023-08-07T13:35:49.477Z",
      "sprintEndDate": "2023-09-01T13:35:00.000Z",
      "path": [
        "ASO Mobile App_64a4fab01734471c30843fda###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "ASO Mobile App_64a4fab01734471c30843fda"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "40685_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.14.2215 Android_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-22T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Released"
    },
    {
      "nodeId": "2662_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "Sprint 116_RMMO",
      "sprintStartDate": "2023-06-13T08:47:48.503Z",
      "sprintEndDate": "2023-06-26T12:03:00.000Z",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Content_sqd_64be65cceb7015715615c4ba",
      "nodeName": "Content",
      "path": "2265_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2265_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "level": 7
    },
    {
      "nodeId": "3125_Sonepar INT_6542b4bb08f31814845119b2",
      "nodeName": "USA-SPRFLD S14.6_Sonepar INT",
      "sprintStartDate": "2023-11-23T19:31:31.529Z",
      "sprintEndDate": "2023-12-06T22:00:00.000Z",
      "path": [
        "Sonepar INT_6542b4bb08f31814845119b2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar INT_6542b4bb08f31814845119b2"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "eCommerce_sqd_6377306a175a953a0a49d322",
      "nodeName": "eCommerce",
      "path": "2356_Integration Services_6377306a175a953a0a49d322###Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2356_Integration Services_6377306a175a953a0a49d322",
      "level": 7
    },
    {
      "nodeId": "11830_Sonepar MAP_6542b43f08f31814845119ab",
      "nodeName": "v1.408 FE_Sonepar MAP",
      "path": [
        "Sonepar MAP_6542b43f08f31814845119ab###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar MAP_6542b43f08f31814845119ab"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-24T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Released"
    },
    {
      "nodeId": "2355_Onsite search_644131b98b61fa2477214bf3",
      "nodeName": "Search_Dec22_2023.Rel13_Onsite search",
      "sprintStartDate": "2023-11-27T22:27:13.996Z",
      "sprintEndDate": "2023-12-24T23:16:00.000Z",
      "path": [
        "Onsite search_644131b98b61fa2477214bf3###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Onsite search_644131b98b61fa2477214bf3"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "4341_RABO Scrum (New)_64a5dac31734471c30844068",
      "nodeName": "Sentry FY24 Sprint 21_RABO Scrum (New)",
      "sprintStartDate": "2023-11-09T09:54:43.381Z",
      "sprintEndDate": "2023-11-22T23:30:00.000Z",
      "path": [
        "RABO Scrum (New)_64a5dac31734471c30844068###AA_port###ADEO_acc###Financial Services_ver###EU_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "RABO Scrum (New)_64a5dac31734471c30844068"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "3201_Sonepar SAF_6542b3da08f31814845119a2",
      "nodeName": "AFS - S14.5_Sonepar SAF",
      "sprintStartDate": "2023-11-09T16:30:42.172Z",
      "sprintEndDate": "2023-11-22T22:00:00.000Z",
      "path": [
        "Sonepar SAF_6542b3da08f31814845119a2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar SAF_6542b3da08f31814845119a2"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "Supply Chain_sqd_6377306a175a953a0a49d322",
      "nodeName": "Supply Chain",
      "path": "2356_Integration Services_6377306a175a953a0a49d322###Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2356_Integration Services_6377306a175a953a0a49d322",
      "level": 7
    },
    {
      "nodeId": "AB Tetra Pak_acc",
      "nodeName": "AB Tetra Pak",
      "path": "Consumer Products_ver###A_bu",
      "labelName": "acc",
      "parentId": "Consumer Products_ver",
      "level": 3
    },
    {
      "nodeId": "2317_GearBox Squad 3_64770ef45286e83998a56143",
      "nodeName": "GB_Sprint 23.21_GearBox Squad 3",
      "sprintStartDate": "2023-10-11T06:00:29.059Z",
      "sprintEndDate": "2023-10-25T03:30:00.000Z",
      "path": [
        "GearBox Squad 3_64770ef45286e83998a56143###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "GearBox Squad 3_64770ef45286e83998a56143"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "13572_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "nodeName": "SBRWEB 1.7_Dotcom + Mobile App",
      "path": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba"
      ],
      "level": 6,
      "releaseEndDate": "2024-02-07T00:00:00.000Z",
      "releaseStartDate": "2023-08-16T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "City and County of San Francisco_acc",
      "nodeName": "City and County of San Francisco",
      "path": "Financial Services_ver###North America_bu",
      "labelName": "acc",
      "parentId": "Financial Services_ver",
      "level": 3
    },
    {
      "nodeId": "3349_Sonepar SRE_6542b86f08f31814845119be",
      "nodeName": "OPS-S 14.5 / 77_Sonepar SRE",
      "sprintStartDate": "2023-11-09T15:34:53.965Z",
      "sprintEndDate": "2023-11-23T15:31:49.000Z",
      "path": [
        "Sonepar SRE_6542b86f08f31814845119be###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar SRE_6542b86f08f31814845119be"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "2313_Website TOF_6441052372a7c53c78f70588",
      "nodeName": "WebsiteTOF_Oct_27_2023_Rel11_Website TOF",
      "sprintStartDate": "2023-10-02T22:38:28.528Z",
      "sprintEndDate": "2023-10-29T14:45:00.000Z",
      "path": [
        "Website TOF_6441052372a7c53c78f70588###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Website TOF_6441052372a7c53c78f70588"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2c22724f-2e1e-4703-87ae-e05018709453_Azure Project_656d830b9f546b19742cb55b",
      "nodeName": "Iteration 5_Azure Project",
      "sprintStartDate": "2023-06-06T00:00:00.000Z",
      "sprintEndDate": "2023-06-07T00:00:00.000Z",
      "path": [
        "Azure Project_656d830b9f546b19742cb55b###3PP CRM_port###ADEO_acc###B_ver###Europe_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Azure Project_656d830b9f546b19742cb55b"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "15660_DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "nodeName": "NickFury_DRP - Discovery POD",
      "path": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1"
      ],
      "level": 6,
      "releaseEndDate": "2022-09-26T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "10160_PSknowHOW _6527af981704342160f43748",
      "nodeName": "KnowHOW PI-15_PSknowHOW ",
      "path": [
        "PSknowHOW _6527af981704342160f43748###DTS_port###Methods and Tools_acc###PS Internal_ver###Internal_bu"
      ],
      "labelName": "release",
      "parentId": [
        "PSknowHOW _6527af981704342160f43748"
      ],
      "level": 6,
      "releaseEndDate": "2024-01-02T00:00:00.000Z",
      "releaseStartDate": "2023-09-27T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Automative1_ver",
      "nodeName": "Automative1",
      "path": "A_bu",
      "labelName": "ver",
      "parentId": "A_bu",
      "level": 2
    },
    {
      "nodeId": "13563_Ecom Pre-Purchase Squad_64be66e3eb7015715615c4bd",
      "nodeName": "SBRWEB CAP_Ecom Pre-Purchase Squad",
      "path": [
        "Ecom Pre-Purchase Squad_64be66e3eb7015715615c4bd###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Ecom Pre-Purchase Squad_64be66e3eb7015715615c4bd"
      ],
      "level": 6,
      "releaseEndDate": "2024-02-07T00:00:00.000Z",
      "releaseStartDate": "2023-08-02T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2241_Data Engineering_644258b830d86a7f539c7fd7",
      "nodeName": "DA Sprint 23.17_Data Engineering",
      "sprintStartDate": "2023-08-16T04:36:42.978Z",
      "sprintEndDate": "2023-08-30T03:59:00.000Z",
      "path": [
        "Data Engineering_644258b830d86a7f539c7fd7###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Data Engineering_644258b830d86a7f539c7fd7"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "Content_sqd_64be65cceb7015715615c4ba",
      "nodeName": "Content",
      "path": "2309_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2309_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "level": 7
    },
    {
      "nodeId": "15207_DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "nodeName": "Valeryan_DRP - Discovery POD",
      "path": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1"
      ],
      "level": 6,
      "releaseEndDate": "2020-09-16T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "11713_Website TOF_6441052372a7c53c78f70588",
      "nodeName": "Website Oct 2023 - HF2 Release_Website TOF",
      "path": [
        "Website TOF_6441052372a7c53c78f70588###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Website TOF_6441052372a7c53c78f70588"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-23T00:00:00.000Z",
      "releaseStartDate": "2023-10-16T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "Regina Maria_acc",
      "nodeName": "Regina Maria",
      "path": "Health_ver###EU_bu",
      "labelName": "acc",
      "parentId": "Health_ver",
      "level": 3
    },
    {
      "nodeId": "40172_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.13.2185 Android_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-08-14T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2309_Onsite search_644131b98b61fa2477214bf3",
      "nodeName": "Search_Oct27_2023.Rel11_Onsite search",
      "sprintStartDate": "2023-10-02T21:54:41.265Z",
      "sprintEndDate": "2023-10-29T22:43:00.000Z",
      "path": [
        "Onsite search_644131b98b61fa2477214bf3###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Onsite search_644131b98b61fa2477214bf3"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "3205_Sonepar eProcurement_6542947f08f3181484511988",
      "nodeName": "Eproc 14.3_Sonepar eProcurement",
      "sprintStartDate": "2023-10-15T20:07:49.841Z",
      "sprintEndDate": "2023-10-25T20:51:00.000Z",
      "path": [
        "Sonepar eProcurement_6542947f08f3181484511988###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar eProcurement_6542947f08f3181484511988"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2318_GearBox Squad 2_64770ec45286e83998a56141",
      "nodeName": "GB_Sprint 23.22_GearBox Squad 2",
      "sprintStartDate": "2023-10-25T11:33:17.758Z",
      "sprintEndDate": "2023-11-08T03:30:00.000Z",
      "path": [
        "GearBox Squad 2_64770ec45286e83998a56141###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "GearBox Squad 2_64770ec45286e83998a56141"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "13557_Ecom Purchase Squad_64be677aeb7015715615c4c1",
      "nodeName": "SBRAPP 3.5_Ecom Purchase Squad",
      "path": [
        "Ecom Purchase Squad_64be677aeb7015715615c4c1###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Ecom Purchase Squad_64be677aeb7015715615c4c1"
      ],
      "level": 6,
      "releaseEndDate": "2024-01-31T00:00:00.000Z",
      "releaseStartDate": "2023-10-25T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Legacy CC_sqd_64be65cceb7015715615c4ba",
      "nodeName": "Legacy CC",
      "path": "2267_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2267_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "level": 7
    },
    {
      "nodeId": "Team 1_sqd_6449103b3be37902a3f1ba70",
      "nodeName": "Team 1",
      "path": [
        "2229_GearBox Squad 1_6449103b3be37902a3f1ba70###GearBox Squad 1_6449103b3be37902a3f1ba70###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2326_GearBox Squad 1_6449103b3be37902a3f1ba70###GearBox Squad 1_6449103b3be37902a3f1ba70###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2317_GearBox Squad 1_6449103b3be37902a3f1ba70###GearBox Squad 1_6449103b3be37902a3f1ba70###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2318_GearBox Squad 1_6449103b3be37902a3f1ba70###GearBox Squad 1_6449103b3be37902a3f1ba70###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2369_GearBox Squad 1_6449103b3be37902a3f1ba70###GearBox Squad 1_6449103b3be37902a3f1ba70###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2228_GearBox Squad 1_6449103b3be37902a3f1ba70###GearBox Squad 1_6449103b3be37902a3f1ba70###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sqd",
      "parentId": [
        "2229_GearBox Squad 1_6449103b3be37902a3f1ba70",
        "2326_GearBox Squad 1_6449103b3be37902a3f1ba70",
        "2317_GearBox Squad 1_6449103b3be37902a3f1ba70",
        "2318_GearBox Squad 1_6449103b3be37902a3f1ba70",
        "2369_GearBox Squad 1_6449103b3be37902a3f1ba70",
        "2228_GearBox Squad 1_6449103b3be37902a3f1ba70"
      ],
      "level": 7
    },
    {
      "nodeId": "B_ver",
      "nodeName": "B",
      "path": "Government Services_bu",
      "labelName": "ver",
      "parentId": "Government Services_bu",
      "level": 2
    },
    {
      "nodeId": "GearBox Squad 2_64770ec45286e83998a56141",
      "nodeName": "GearBox Squad 2",
      "path": "Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Sunbelt Rentals_port",
      "level": 5,
      "basicProjectConfigId": "64770ec45286e83998a56141"
    },
    {
      "nodeId": "2241_Promotion Engine BOF_64c178d7eb7015715615c5a6",
      "nodeName": "Cart&Checkout Aug04 2023 Rel8_Promotion Engine BOF",
      "sprintStartDate": "2023-07-10T16:15:00.000Z",
      "sprintEndDate": "2023-08-06T16:15:00.000Z",
      "path": [
        "Promotion Engine BOF_64c178d7eb7015715615c5a6###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Promotion Engine BOF_64c178d7eb7015715615c5a6"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "2263_CMS_644103e772a7c53c78f70582",
      "nodeName": "CMS_Aug4_2023.Rel08_CMS",
      "sprintStartDate": "2023-07-10T16:36:40.516Z",
      "sprintEndDate": "2023-08-07T19:18:00.000Z",
      "path": [
        "CMS_644103e772a7c53c78f70582###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "CMS_644103e772a7c53c78f70582"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "1017_DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "nodeName": "DRP Sprint 85_DRP - Discovery POD",
      "sprintStartDate": "2023-11-02T14:18:44.928Z",
      "sprintEndDate": "2023-11-25T04:00:00.000Z",
      "path": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "13599_GearBox_63a02b61bbc09e116d744d9d",
      "nodeName": "23.05_GearBox",
      "path": [
        "GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "GearBox_63a02b61bbc09e116d744d9d"
      ],
      "level": 6,
      "releaseEndDate": "2024-01-31T00:00:00.000Z",
      "releaseStartDate": "2023-11-22T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2210_Website BOF_644108c072a7c53c78f7059a",
      "nodeName": "Promo_Engine_BOF_Aug_4_Website BOF",
      "sprintStartDate": "2023-07-10T21:26:00.000Z",
      "sprintEndDate": "2023-08-04T21:26:00.000Z",
      "path": [
        "Website BOF_644108c072a7c53c78f7059a###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Website BOF_644108c072a7c53c78f7059a"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "11650_Sonepar INT_6542b4bb08f31814845119b2",
      "nodeName": "[USA-SPRFLD] Prices Ready_Sonepar INT",
      "path": [
        "Sonepar INT_6542b4bb08f31814845119b2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar INT_6542b4bb08f31814845119b2"
      ],
      "level": 6,
      "releaseEndDate": "2023-06-21T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "3214_Sonepar MAP_6542b43f08f31814845119ab",
      "nodeName": "M&P - S14.6_Sonepar MAP",
      "sprintStartDate": "2023-11-23T10:23:47.247Z",
      "sprintEndDate": "2023-12-06T10:44:00.000Z",
      "path": [
        "Sonepar MAP_6542b43f08f31814845119ab###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar MAP_6542b43f08f31814845119ab"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "3261_Sonepar SAF_6542b3da08f31814845119a2",
      "nodeName": "Search & Products 14.4_Sonepar SAF",
      "sprintStartDate": "2023-10-27T08:53:31.529Z",
      "sprintEndDate": "2023-11-10T08:53:23.000Z",
      "path": [
        "Sonepar SAF_6542b3da08f31814845119a2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar SAF_6542b3da08f31814845119a2"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "11711_PIM_64a4e09e1734471c30843fc2",
      "nodeName": "Website Jan 2024 Release_PIM",
      "path": [
        "PIM_64a4e09e1734471c30843fc2###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "PIM_64a4e09e1734471c30843fc2"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-27T00:00:00.000Z",
      "releaseStartDate": "2023-10-02T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2241_Website BOF_644108c072a7c53c78f7059a",
      "nodeName": "Cart&Checkout Aug05 2023 Rel8_Website BOF",
      "sprintStartDate": "2023-07-10T16:15:00.000Z",
      "sprintEndDate": "2023-08-06T16:15:00.000Z",
      "path": [
        "Website BOF_644108c072a7c53c78f7059a###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Website BOF_644108c072a7c53c78f7059a"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2310_Onsite search_644131b98b61fa2477214bf3",
      "nodeName": "Search_Nov24_2023.Rel12_Onsite search",
      "sprintStartDate": "2023-10-30T21:41:45.135Z",
      "sprintEndDate": "2023-11-26T22:30:26.000Z",
      "path": [
        "Onsite search_644131b98b61fa2477214bf3###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Onsite search_644131b98b61fa2477214bf3"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2365_R1+ Sales_648ab46f6803f300a9fd2e09",
      "nodeName": "SALES_Sprint 23.23_R1+ Sales",
      "sprintStartDate": "2023-11-08T12:59:02.484Z",
      "sprintEndDate": "2023-11-22T04:00:00.000Z",
      "path": [
        "R1+ Sales_648ab46f6803f300a9fd2e09###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "R1+ Sales_648ab46f6803f300a9fd2e09"
      ],
      "sprintState": "closed",
      "level": 6
    },
    {
      "nodeId": "SBR Mulesoft Tribe_645e15429c05c375596bf94b",
      "nodeName": "SBR Mulesoft Tribe",
      "path": "Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Sunbelt Rentals_port",
      "level": 5,
      "basicProjectConfigId": "645e15429c05c375596bf94b"
    },
    {
      "nodeId": "Team 1_sqd_6494298bca84920b10dddd3b",
      "nodeName": "Team 1",
      "path": [
        "2238_Service & Assets_6494298bca84920b10dddd3b###Service & Assets_6494298bca84920b10dddd3b###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2304_Service & Assets_6494298bca84920b10dddd3b###Service & Assets_6494298bca84920b10dddd3b###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2232_Service & Assets_6494298bca84920b10dddd3b###Service & Assets_6494298bca84920b10dddd3b###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2237_Service & Assets_6494298bca84920b10dddd3b###Service & Assets_6494298bca84920b10dddd3b###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2305_Service & Assets_6494298bca84920b10dddd3b###Service & Assets_6494298bca84920b10dddd3b###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2303_Service & Assets_6494298bca84920b10dddd3b###Service & Assets_6494298bca84920b10dddd3b###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sqd",
      "parentId": [
        "2238_Service & Assets_6494298bca84920b10dddd3b",
        "2304_Service & Assets_6494298bca84920b10dddd3b",
        "2232_Service & Assets_6494298bca84920b10dddd3b",
        "2237_Service & Assets_6494298bca84920b10dddd3b",
        "2305_Service & Assets_6494298bca84920b10dddd3b",
        "2303_Service & Assets_6494298bca84920b10dddd3b"
      ],
      "level": 7
    },
    {
      "nodeId": "Consumer Products_ver",
      "nodeName": "Consumer Products",
      "path": "Europe_bu",
      "labelName": "ver",
      "parentId": "Europe_bu",
      "level": 2
    },
    {
      "nodeId": "2214_R1+ Frontline_647588bc5286e83998a5609c",
      "nodeName": "FRONT_Sprint 23.23_R1+ Frontline",
      "sprintStartDate": "2023-11-08T17:06:59.000Z",
      "sprintEndDate": "2023-11-22T19:38:54.000Z",
      "path": [
        "R1+ Frontline_647588bc5286e83998a5609c###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "R1+ Frontline_647588bc5286e83998a5609c"
      ],
      "sprintState": "closed",
      "level": 6
    },
    {
      "nodeId": "2211_Promotion Engine TOF_644105f972a7c53c78f7058c",
      "nodeName": "Promo_Engine_TOF_July_7_Promotion Engine TOF",
      "sprintStartDate": "2023-06-12T20:18:21.882Z",
      "sprintEndDate": "2023-07-07T20:25:00.000Z",
      "path": [
        "Promotion Engine TOF_644105f972a7c53c78f7058c###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Promotion Engine TOF_644105f972a7c53c78f7058c"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "145851_pod16 2_657219fd78247c3cd726d630",
      "nodeName": "Release_Sep2023_pod16 2",
      "path": [
        "pod16 2_657219fd78247c3cd726d630###2021 WLP Brand Retainer_port###AAA Auto Club Group_acc###Automative1_ver###A_bu"
      ],
      "labelName": "release",
      "parentId": [
        "pod16 2_657219fd78247c3cd726d630"
      ],
      "level": 6,
      "releaseEndDate": "2023-09-29T00:00:00.000Z",
      "releaseStartDate": "2023-09-01T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2245_Cart & checkout_6441078b72a7c53c78f70590",
      "nodeName": "Cart&Checkout Nov24 2023 Rel12_Cart & checkout",
      "sprintStartDate": "2023-10-30T14:11:17.486Z",
      "sprintEndDate": "2023-11-26T16:16:00.000Z",
      "path": [
        "Cart & checkout_6441078b72a7c53c78f70590###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Cart & checkout_6441078b72a7c53c78f70590"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "B_ver",
      "nodeName": "B",
      "path": "Europe_bu",
      "labelName": "ver",
      "parentId": "Europe_bu",
      "level": 2
    },
    {
      "nodeId": "2314_PIM_64a4e09e1734471c30843fc2",
      "nodeName": "PIM_Nov_23_Sprint_43_PIM",
      "sprintStartDate": "2023-10-02T14:28:36.485Z",
      "sprintEndDate": "2023-10-30T16:30:00.000Z",
      "path": [
        "PIM_64a4e09e1734471c30843fc2###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "PIM_64a4e09e1734471c30843fc2"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2243_Cart & checkout_6441078b72a7c53c78f70590",
      "nodeName": "Cart&Checkout Sep29 2023 Rel10_Cart & checkout",
      "sprintStartDate": "2023-09-05T02:19:58.310Z",
      "sprintEndDate": "2023-10-01T16:16:00.000Z",
      "path": [
        "Cart & checkout_6441078b72a7c53c78f70590###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Cart & checkout_6441078b72a7c53c78f70590"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Team 2_sqd_648ab46f6803f300a9fd2e09",
      "nodeName": "Team 2",
      "path": [
        "2364_R1+ Sales_648ab46f6803f300a9fd2e09###R1+ Sales_648ab46f6803f300a9fd2e09###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2361_R1+ Sales_648ab46f6803f300a9fd2e09###R1+ Sales_648ab46f6803f300a9fd2e09###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2288_R1+ Sales_648ab46f6803f300a9fd2e09###R1+ Sales_648ab46f6803f300a9fd2e09###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sqd",
      "parentId": [
        "2364_R1+ Sales_648ab46f6803f300a9fd2e09",
        "2361_R1+ Sales_648ab46f6803f300a9fd2e09",
        "2288_R1+ Sales_648ab46f6803f300a9fd2e09"
      ],
      "level": 7
    },
    {
      "nodeId": "11723_Design System_64ad9e667d51263c17602c67",
      "nodeName": "DFS-PI15_Design System",
      "path": [
        "Design System_64ad9e667d51263c17602c67###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Design System_64ad9e667d51263c17602c67"
      ],
      "level": 6,
      "releaseEndDate": "2024-03-16T00:00:00.000Z",
      "releaseStartDate": "2023-12-06T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Import_656edecc053eaf4d3a2b06da",
      "nodeName": "Import",
      "path": "3PP Social - Chrysler_port###ADNOC Global Trading Limited_acc###Consumer Products_ver###Internal_bu",
      "labelName": "project",
      "parentId": "3PP Social - Chrysler_port",
      "level": 5,
      "basicProjectConfigId": "656edecc053eaf4d3a2b06da"
    },
    {
      "nodeId": "13572_Ecom Pre-Purchase Squad_64be66e3eb7015715615c4bd",
      "nodeName": "SBRWEB 1.7_Ecom Pre-Purchase Squad",
      "path": [
        "Ecom Pre-Purchase Squad_64be66e3eb7015715615c4bd###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Ecom Pre-Purchase Squad_64be66e3eb7015715615c4bd"
      ],
      "level": 6,
      "releaseEndDate": "2024-02-07T00:00:00.000Z",
      "releaseStartDate": "2023-08-16T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Anglo American Marketing Limited_port",
      "nodeName": "Anglo American Marketing Limited",
      "path": "Anglo American Marketing Limited_acc###Energy & Commodities_ver###EU_bu",
      "labelName": "port",
      "parentId": "Anglo American Marketing Limited_acc",
      "level": 4
    },
    {
      "nodeId": "15596_DRP - HomePage POD_64b3f315c4e72b57c94035e2",
      "nodeName": "Juggernaut_DRP - HomePage POD",
      "path": [
        "DRP - HomePage POD_64b3f315c4e72b57c94035e2###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "DRP - HomePage POD_64b3f315c4e72b57c94035e2"
      ],
      "level": 6,
      "releaseEndDate": "2022-07-13T00:00:00.000Z",
      "releaseStartDate": "2022-06-23T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "ACE20001_port",
      "nodeName": "ACE20001",
      "path": "Abu Dhabi Investment Authority_acc###PS Internal_ver###EU_bu",
      "labelName": "port",
      "parentId": "Abu Dhabi Investment Authority_acc",
      "level": 4
    },
    {
      "nodeId": "Team 2_sqd_64770ec45286e83998a56141",
      "nodeName": "Team 2",
      "path": "2229_GearBox Squad 2_64770ec45286e83998a56141###GearBox Squad 2_64770ec45286e83998a56141###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2229_GearBox Squad 2_64770ec45286e83998a56141",
      "level": 7
    },
    {
      "nodeId": "13589_Ecom Pre-Purchase Squad_64be66e3eb7015715615c4bd",
      "nodeName": "SBRAPP 3.3.1_Ecom Pre-Purchase Squad",
      "path": [
        "Ecom Pre-Purchase Squad_64be66e3eb7015715615c4bd###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Ecom Pre-Purchase Squad_64be66e3eb7015715615c4bd"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-05T00:00:00.000Z",
      "releaseStartDate": "2023-09-11T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "45283_KN Server_656969922d6d5f774de2e686",
      "nodeName": "MAP|PI_14|ITR_6_KN Server",
      "sprintStartDate": "2023-09-06T07:03:00.000Z",
      "sprintEndDate": "2023-09-26T07:03:00.000Z",
      "path": [
        "KN Server_656969922d6d5f774de2e686###2021 WLP Brand Retainer_port###ADNOC Global Trading Limited_acc###Consumer Products_ver###Government Services_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "KN Server_656969922d6d5f774de2e686"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "A_bu",
      "nodeName": "A",
      "path": "",
      "labelName": "bu",
      "level": 1
    },
    {
      "nodeId": "Consumer Products_ver",
      "nodeName": "Consumer Products",
      "path": "Government Services_bu",
      "labelName": "ver",
      "parentId": "Government Services_bu",
      "level": 2
    },
    {
      "nodeId": "2203_R1+ Logistics_648ab6186803f300a9fd2e0e",
      "nodeName": "LOG_Sprint 23.21_R1+ Logistics",
      "sprintStartDate": "2023-10-11T14:54:26.928Z",
      "sprintEndDate": "2023-10-25T03:30:00.000Z",
      "path": [
        "R1+ Logistics_648ab6186803f300a9fd2e0e###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "R1+ Logistics_648ab6186803f300a9fd2e0e"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "1113_Mobile App_637b17b9175a953a0a49d3c2",
      "nodeName": "MA_Sprint 23.11_Mobile App",
      "sprintStartDate": "2023-05-24T15:35:26.133Z",
      "sprintEndDate": "2023-06-06T21:00:00.000Z",
      "path": [
        "Mobile App_637b17b9175a953a0a49d3c2###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Mobile App_637b17b9175a953a0a49d3c2"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "40080_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.13.1503 iOS_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-08-04T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "39834_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.12.2155 Android_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-06-19T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "3123_Sonepar INT_6542b4bb08f31814845119b2",
      "nodeName": "USA-SPRFLD S14.4_Sonepar INT",
      "sprintStartDate": "2023-10-25T22:41:28.000Z",
      "sprintEndDate": "2023-11-08T22:00:00.000Z",
      "path": [
        "Sonepar INT_6542b4bb08f31814845119b2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar INT_6542b4bb08f31814845119b2"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2317_GearBox Squad 1_6449103b3be37902a3f1ba70",
      "nodeName": "GB_Sprint 23.21_GearBox Squad 1",
      "sprintStartDate": "2023-10-11T06:00:29.059Z",
      "sprintEndDate": "2023-10-25T03:30:00.000Z",
      "path": [
        "GearBox Squad 1_6449103b3be37902a3f1ba70###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "GearBox Squad 1_6449103b3be37902a3f1ba70"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "13598_GearBox_63a02b61bbc09e116d744d9d",
      "nodeName": "GB Dashboard Hotfix 23.03.02_GearBox",
      "path": [
        "GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "GearBox_63a02b61bbc09e116d744d9d"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-07T00:00:00.000Z",
      "releaseStartDate": "2023-10-25T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "Unified Commerce - Dan's MVP_64ab97327d51263c17602b58",
      "nodeName": "Unified Commerce - Dan's MVP",
      "path": "Endeavour Group Pty Ltd_port###Endeavour Group Limited_acc###Retail_ver###International_bu",
      "labelName": "project",
      "parentId": "Endeavour Group Pty Ltd_port",
      "level": 5,
      "basicProjectConfigId": "64ab97327d51263c17602b58"
    },
    {
      "nodeId": "3260_Sonepar SAF_6542b3da08f31814845119a2",
      "nodeName": "Search & Products 14.3_Sonepar SAF",
      "sprintStartDate": "2023-10-13T15:00:00.000Z",
      "sprintEndDate": "2023-10-26T15:00:00.000Z",
      "path": [
        "Sonepar SAF_6542b3da08f31814845119a2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar SAF_6542b3da08f31814845119a2"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "EU_bu",
      "nodeName": "EU",
      "path": "",
      "labelName": "bu",
      "level": 1
    },
    {
      "nodeId": "40131_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.13.1504 iOS_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-08-04T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "3060_Sonepar Global_6448a8213be37902a3f1ba45",
      "nodeName": "Data Team 14.1_Sonepar Global",
      "sprintStartDate": "2023-09-14T16:17:15.206Z",
      "sprintEndDate": "2023-09-27T18:13:00.000Z",
      "path": [
        "Sonepar Global_6448a8213be37902a3f1ba45###Sonepar Client Cost - MC_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar Global_6448a8213be37902a3f1ba45"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "15467_DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "nodeName": "PHOENIX_HotFix_Web_DRP - Discovery POD",
      "path": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1"
      ],
      "level": 6,
      "releaseEndDate": "2021-08-18T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "40362_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.13.2190 Android_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-09-11T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "41047_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.15.1528 iOS_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-28T00:00:00.000Z",
      "releaseStartDate": "2023-11-21T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "150902_DHL Logistics Scrumban_6549029708f3181484511bbb",
      "nodeName": "Logistics 2023.9.1_DHL Logistics Scrumban",
      "path": [
        "DHL Logistics Scrumban_6549029708f3181484511bbb###DPDHL - CSI DCI - Logistics and CJ_port###Deutsche Post AG_acc###Automotive_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "DHL Logistics Scrumban_6549029708f3181484511bbb"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-30T00:00:00.000Z",
      "releaseStartDate": "2023-10-26T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "40419_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.14.2201 Android_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-09-18T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "1085_VDOS_63777558175a953a0a49d363",
      "nodeName": "OMS_Sprint 23.9_VDOS",
      "sprintStartDate": "2023-05-10T13:23:31.611Z",
      "sprintEndDate": "2023-05-23T23:55:00.000Z",
      "path": [
        "VDOS_63777558175a953a0a49d363###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "VDOS_63777558175a953a0a49d363"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "2317_Promotion Engine BOF_64c178d7eb7015715615c5a6",
      "nodeName": "Promo_Engine_BOF_Sep_29_Promotion Engine BOF",
      "sprintStartDate": "2023-09-05T00:51:14.636Z",
      "sprintEndDate": "2023-09-29T13:48:00.000Z",
      "path": [
        "Promotion Engine BOF_64c178d7eb7015715615c5a6###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Promotion Engine BOF_64c178d7eb7015715615c5a6"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "11819_Sonepar EAC_6542b3b008f31814845119a0",
      "nodeName": "FE Next RC_Sonepar EAC",
      "path": [
        "Sonepar EAC_6542b3b008f31814845119a0###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar EAC_6542b3b008f31814845119a0"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-08T00:00:00.000Z",
      "releaseStartDate": "2023-11-06T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2228_GearBox Squad 2_64770ec45286e83998a56141",
      "nodeName": "GB_Sprint 23.19_GearBox Squad 2",
      "sprintStartDate": "2023-09-13T05:00:25.796Z",
      "sprintEndDate": "2023-09-27T03:30:00.000Z",
      "path": [
        "GearBox Squad 2_64770ec45286e83998a56141###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "GearBox Squad 2_64770ec45286e83998a56141"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "13600_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "nodeName": "SBRWEB 1.6.1_Dotcom + Mobile App",
      "path": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba"
      ],
      "level": 6,
      "releaseEndDate": "2023-12-13T00:00:00.000Z",
      "releaseStartDate": "2023-09-27T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "1006_DRP - HomePage POD_64b3f315c4e72b57c94035e2",
      "nodeName": "DRP Sprint 82_DRP - HomePage POD",
      "sprintStartDate": "2023-08-31T12:31:00.986Z",
      "sprintEndDate": "2023-09-21T16:37:00.000Z",
      "path": [
        "DRP - HomePage POD_64b3f315c4e72b57c94035e2###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "DRP - HomePage POD_64b3f315c4e72b57c94035e2"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "3271_Sonepar Global_6448a8213be37902a3f1ba45",
      "nodeName": "Data Team 14.3_Sonepar Global",
      "sprintStartDate": "2023-10-12T09:59:11.799Z",
      "sprintEndDate": "2023-10-25T18:13:00.000Z",
      "path": [
        "Sonepar Global_6448a8213be37902a3f1ba45###Sonepar Client Cost - MC_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar Global_6448a8213be37902a3f1ba45"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "38784_RABO Scrum (New)_64a5dac31734471c30844068",
      "nodeName": "dotcom-2023-10-26_RABO Scrum (New)",
      "path": [
        "RABO Scrum (New)_64a5dac31734471c30844068###AA_port###ADEO_acc###Financial Services_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RABO Scrum (New)_64a5dac31734471c30844068"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-26T00:00:00.000Z",
      "releaseStartDate": "2023-10-12T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "Academy Sports_acc",
      "nodeName": "Academy Sports",
      "path": "Retail_ver###North America_bu",
      "labelName": "acc",
      "parentId": "Retail_ver",
      "level": 3
    },
    {
      "nodeId": "13622_Integration Services_6377306a175a953a0a49d322",
      "nodeName": "3.0_R1_Sales_Rel1 HF1 (IS)_Integration Services",
      "path": [
        "Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Integration Services_6377306a175a953a0a49d322"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-22T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Released"
    },
    {
      "nodeId": "13601_GearBox_63a02b61bbc09e116d744d9d",
      "nodeName": "GB GoMobileAndroid 23.01_GearBox",
      "path": [
        "GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "GearBox_63a02b61bbc09e116d744d9d"
      ],
      "level": 6,
      "releaseEndDate": "2023-12-15T00:00:00.000Z",
      "releaseStartDate": "2023-08-07T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "ASO Mobile App_64a4fab01734471c30843fda",
      "nodeName": "ASO Mobile App",
      "path": "ASO_port###Academy Sports_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "ASO_port",
      "level": 5,
      "basicProjectConfigId": "64a4fab01734471c30843fda"
    },
    {
      "nodeId": "11715_Website TOF_6441052372a7c53c78f70588",
      "nodeName": "Website Oct 2023 - HF3 Fanatics Release_Website TOF",
      "path": [
        "Website TOF_6441052372a7c53c78f70588###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Website TOF_6441052372a7c53c78f70588"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-10T00:00:00.000Z",
      "releaseStartDate": "2023-11-01T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "2960_Sonepar Global_6448a8213be37902a3f1ba45",
      "nodeName": "Data Insight - 13.4_Sonepar Global",
      "sprintStartDate": "2023-08-03T10:53:20.982Z",
      "sprintEndDate": "2023-08-16T21:59:00.000Z",
      "path": [
        "Sonepar Global_6448a8213be37902a3f1ba45###Sonepar Client Cost - MC_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar Global_6448a8213be37902a3f1ba45"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "2275_Data Engineering_644258b830d86a7f539c7fd7",
      "nodeName": "DEA Sprint 23.22_Data Engineering",
      "sprintStartDate": "2023-10-25T06:34:30.591Z",
      "sprintEndDate": "2023-11-08T03:30:00.000Z",
      "path": [
        "Data Engineering_644258b830d86a7f539c7fd7###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Data Engineering_644258b830d86a7f539c7fd7"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "ENI-Evolutions_port",
      "nodeName": "ENI-Evolutions",
      "path": "ENI S.p.A_acc###Energy & Commodities_ver###EU_bu",
      "labelName": "port",
      "parentId": "ENI S.p.A_acc",
      "level": 4
    },
    {
      "nodeId": "VDOS Translations_651fe6bb1704342160f43511",
      "nodeName": "VDOS Translations",
      "path": "Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Sunbelt Rentals_port",
      "level": 5,
      "basicProjectConfigId": "651fe6bb1704342160f43511"
    },
    {
      "nodeId": "4294_RABO Scrum (New)_64a5dac31734471c30844068",
      "nodeName": "Sentry FY24 Sprint 20_RABO Scrum (New)",
      "sprintStartDate": "2023-10-26T09:10:58.674Z",
      "sprintEndDate": "2023-11-08T22:30:00.000Z",
      "path": [
        "RABO Scrum (New)_64a5dac31734471c30844068###AA_port###ADEO_acc###Financial Services_ver###EU_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "RABO Scrum (New)_64a5dac31734471c30844068"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2406_Integration Services_6377306a175a953a0a49d322",
      "nodeName": "IS_Sprint 23.24_Integration Services",
      "sprintStartDate": "2023-11-22T18:55:04.986Z",
      "sprintEndDate": "2023-12-06T18:55:00.000Z",
      "path": [
        "Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Integration Services_6377306a175a953a0a49d322"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "3201_Sonepar SRE_6542b86f08f31814845119be",
      "nodeName": "AFS - S14.5_Sonepar SRE",
      "sprintStartDate": "2023-11-09T16:30:42.172Z",
      "sprintEndDate": "2023-11-22T22:00:00.000Z",
      "path": [
        "Sonepar SRE_6542b86f08f31814845119be###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar SRE_6542b86f08f31814845119be"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2215_R1+ Frontline_647588bc5286e83998a5609c",
      "nodeName": "FRONT_Sprint 23.24_R1+ Frontline",
      "sprintStartDate": "2023-11-22T16:50:51.000Z",
      "sprintEndDate": "2023-12-06T19:22:16.000Z",
      "path": [
        "R1+ Frontline_647588bc5286e83998a5609c###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "R1+ Frontline_647588bc5286e83998a5609c"
      ],
      "sprintState": "active",
      "level": 6
    },
    {
      "nodeId": "2391_Website BOF_644108c072a7c53c78f7059a",
      "nodeName": "Cart&Checkout Dec22 2023 Rel13_Website BOF",
      "sprintStartDate": "2023-11-27T14:14:53.159Z",
      "sprintEndDate": "2023-12-25T05:00:00.000Z",
      "path": [
        "Website BOF_644108c072a7c53c78f7059a###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Website BOF_644108c072a7c53c78f7059a"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "2713_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "Sprint 119_RMMO",
      "sprintStartDate": "2023-07-25T07:39:51.327Z",
      "sprintEndDate": "2023-08-07T12:03:00.000Z",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "3124_Sonepar INT_6542b4bb08f31814845119b2",
      "nodeName": "USA-SPRFLD S14.5_Sonepar INT",
      "sprintStartDate": "2023-11-08T15:47:02.118Z",
      "sprintEndDate": "2023-11-22T22:00:00.000Z",
      "path": [
        "Sonepar INT_6542b4bb08f31814845119b2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar INT_6542b4bb08f31814845119b2"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "40130_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.13.2184 Android_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-08-04T00:00:00.000Z",
      "releaseStartDate": "2023-06-29T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "40968_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.15.1526 iOS_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-28T00:00:00.000Z",
      "releaseStartDate": "2023-11-03T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Service & Assets_6494298bca84920b10dddd3b",
      "nodeName": "Service & Assets",
      "path": "Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Sunbelt Rentals_port",
      "level": 5,
      "basicProjectConfigId": "6494298bca84920b10dddd3b"
    },
    {
      "nodeId": "40456_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.14.1518 iOS_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-09-06T00:00:00.000Z",
      "releaseStartDate": "2023-08-18T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "148924_Do it Best_657049b505ce0569d5612ec7",
      "nodeName": "FEAEM_RV1.4_Do it Best",
      "path": [
        "Do it Best_657049b505ce0569d5612ec7###3PP CRM_port###ADEO_acc###Automotive_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Do it Best_657049b505ce0569d5612ec7"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-31T00:00:00.000Z",
      "releaseStartDate": "2023-10-11T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "2356_Integration Services_6377306a175a953a0a49d322",
      "nodeName": "IS_Sprint 23.21_Integration Services",
      "sprintStartDate": "2023-10-11T04:00:22.534Z",
      "sprintEndDate": "2023-10-25T04:00:00.000Z",
      "path": [
        "Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Integration Services_6377306a175a953a0a49d322"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "10143_Test rc_657056c668a1225c0126c843",
      "nodeName": "KnowHOW v9.1.0_Test rc",
      "path": [
        "Test rc_657056c668a1225c0126c843###2021 WLP Brand Retainer_port###AB Tetra Pak_acc###Consumer Products_ver###Europe_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Test rc_657056c668a1225c0126c843"
      ],
      "level": 6,
      "releaseEndDate": "2024-01-18T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "5bf63499-277c-4928-94d6-eb1004fc548f_KYC _63e5fea5ae1aeb593f3395aa",
      "nodeName": "Sprint 22_KYC ",
      "sprintStartDate": "2023-08-28T00:00:00.000Z",
      "sprintEndDate": "2023-09-15T00:00:00.000Z",
      "path": [
        "KYC _63e5fea5ae1aeb593f3395aa###ENI-Evolutions_port###ENI S.p.A_acc###Energy & Commodities_ver###EU_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "KYC _63e5fea5ae1aeb593f3395aa"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "11819_Sonepar BUY_6542b42308f31814845119a8",
      "nodeName": "v1.403 FE RC_Sonepar BUY",
      "path": [
        "Sonepar BUY_6542b42308f31814845119a8###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar BUY_6542b42308f31814845119a8"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-13T00:00:00.000Z",
      "releaseStartDate": "2023-11-06T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "2202_R1+ Logistics_648ab6186803f300a9fd2e0e",
      "nodeName": "LOG_Sprint 23.20_R1+ Logistics",
      "sprintStartDate": "2023-09-27T17:34:59.385Z",
      "sprintEndDate": "2023-10-11T17:34:00.000Z",
      "path": [
        "R1+ Logistics_648ab6186803f300a9fd2e0e###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "R1+ Logistics_648ab6186803f300a9fd2e0e"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "11829_Sonepar BUY_6542b42308f31814845119a8",
      "nodeName": "BE Next RC B&D_Sonepar BUY",
      "path": [
        "Sonepar BUY_6542b42308f31814845119a8###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar BUY_6542b42308f31814845119a8"
      ],
      "level": 6,
      "releaseEndDate": "2023-12-29T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Supply Chain_sqd_6377306a175a953a0a49d322",
      "nodeName": "Supply Chain",
      "path": "2406_Integration Services_6377306a175a953a0a49d322###Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2406_Integration Services_6377306a175a953a0a49d322",
      "level": 7
    },
    {
      "nodeId": "10143_PSknowHOW _6527af981704342160f43748",
      "nodeName": "KnowHOW v9.1.0_PSknowHOW ",
      "path": [
        "PSknowHOW _6527af981704342160f43748###DTS_port###Methods and Tools_acc###PS Internal_ver###Internal_bu"
      ],
      "labelName": "release",
      "parentId": [
        "PSknowHOW _6527af981704342160f43748"
      ],
      "level": 6,
      "releaseEndDate": "2024-01-18T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "13621_Ecom Post-Purchase Squad_64be67caeb7015715615c4c5",
      "nodeName": "SBRAPP 3.6 _Ecom Post-Purchase Squad",
      "path": [
        "Ecom Post-Purchase Squad_64be67caeb7015715615c4c5###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Ecom Post-Purchase Squad_64be67caeb7015715615c4c5"
      ],
      "level": 6,
      "releaseEndDate": "2024-04-24T00:00:00.000Z",
      "releaseStartDate": "2024-01-17T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2257_SEO_64a4e0591734471c30843fc0",
      "nodeName": "Search_Sep01_2023.Rel9_SEO",
      "sprintStartDate": "2023-08-07T21:41:19.769Z",
      "sprintEndDate": "2023-09-03T22:30:00.000Z",
      "path": [
        "SEO_64a4e0591734471c30843fc0###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "SEO_64a4e0591734471c30843fc0"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "Browse & Search_sqd_63dc01e47228be4c30553ce1",
      "nodeName": "Browse & Search",
      "path": "1007_DRP - Discovery POD_63dc01e47228be4c30553ce1###DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "1007_DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "level": 7
    },
    {
      "nodeId": "Sonepar INT_6542b4bb08f31814845119b2",
      "nodeName": "Sonepar INT",
      "path": "Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Sonepar SAS_port",
      "level": 5,
      "basicProjectConfigId": "6542b4bb08f31814845119b2"
    },
    {
      "nodeId": "13627_GearBox_63a02b61bbc09e116d744d9d",
      "nodeName": "GB Dashboard 3.0 24.01_GearBox",
      "path": [
        "GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "GearBox_63a02b61bbc09e116d744d9d"
      ],
      "level": 6,
      "releaseEndDate": "2024-02-20T00:00:00.000Z",
      "releaseStartDate": "2023-12-06T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "11714_ASO Mobile App_64a4fab01734471c30843fda",
      "nodeName": "Mobile-App-Jan-2024_ASO Mobile App",
      "path": [
        "ASO Mobile App_64a4fab01734471c30843fda###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "ASO Mobile App_64a4fab01734471c30843fda"
      ],
      "level": 6,
      "releaseEndDate": "2024-01-29T00:00:00.000Z",
      "releaseStartDate": "2023-10-02T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Browse & Search_sqd_63dc01e47228be4c30553ce1",
      "nodeName": "Browse & Search",
      "path": "1006_DRP - Discovery POD_63dc01e47228be4c30553ce1###DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "1006_DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "level": 7
    },
    {
      "nodeId": "2347_CMS_644103e772a7c53c78f70582",
      "nodeName": "CMS_Oct27_2023_RelXX_CMS",
      "sprintStartDate": "2023-10-02T21:07:20.753Z",
      "sprintEndDate": "2023-10-30T16:51:00.000Z",
      "path": [
        "CMS_644103e772a7c53c78f70582###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "CMS_644103e772a7c53c78f70582"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Deutsche Post AG_acc",
      "nodeName": "Deutsche Post AG",
      "path": "Automotive_ver###EU_bu",
      "labelName": "acc",
      "parentId": "Automotive_ver",
      "level": 3
    },
    {
      "nodeId": "Test Serv Asset_647582005286e83998a56096",
      "nodeName": "Test Serv Asset",
      "path": "Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Sunbelt Rentals_port",
      "level": 5,
      "basicProjectConfigId": "647582005286e83998a56096"
    },
    {
      "nodeId": "Team 4_sqd_648ab6186803f300a9fd2e0e",
      "nodeName": "Team 4",
      "path": "2204_R1+ Logistics_648ab6186803f300a9fd2e0e###R1+ Logistics_648ab6186803f300a9fd2e0e###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2204_R1+ Logistics_648ab6186803f300a9fd2e0e",
      "level": 7
    },
    {
      "nodeId": "2288_R1+ Sales_648ab46f6803f300a9fd2e09",
      "nodeName": "SALES_Sprint 23.20_R1+ Sales",
      "sprintStartDate": "2023-09-27T15:47:04.556Z",
      "sprintEndDate": "2023-10-11T02:51:02.000Z",
      "path": [
        "R1+ Sales_648ab46f6803f300a9fd2e09###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "R1+ Sales_648ab46f6803f300a9fd2e09"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2180_CMS_644103e772a7c53c78f70582",
      "nodeName": "PIM_May_23_Sprint_37_CMS",
      "sprintStartDate": "2023-04-17T13:40:06.258Z",
      "sprintEndDate": "2023-05-15T15:49:00.000Z",
      "path": [
        "CMS_644103e772a7c53c78f70582###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "CMS_644103e772a7c53c78f70582"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "40925_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.15.1525 iOS_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-07T00:00:00.000Z",
      "releaseStartDate": "2023-10-26T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Team 2_sqd_64770ec45286e83998a56141",
      "nodeName": "Team 2",
      "path": "2317_GearBox Squad 2_64770ec45286e83998a56141###GearBox Squad 2_64770ec45286e83998a56141###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2317_GearBox Squad 2_64770ec45286e83998a56141",
      "level": 7
    },
    {
      "nodeId": "POD 16_657065700615235d92401735",
      "nodeName": "POD 16",
      "path": "Nissan Core Team - PS_port###Nissan Motor Co. Ltd._acc###Automotive_ver###International_bu",
      "labelName": "project",
      "parentId": "Nissan Core Team - PS_port",
      "level": 5,
      "basicProjectConfigId": "657065700615235d92401735"
    },
    {
      "nodeId": "3408_Sonepar Mobile App_6448a96c3be37902a3f1ba48",
      "nodeName": "DFA QA Sprint 14.4_Sonepar Mobile App",
      "sprintStartDate": "2023-10-26T11:29:35.895Z",
      "sprintEndDate": "2023-11-09T11:29:30.000Z",
      "path": [
        "Sonepar Mobile App_6448a96c3be37902a3f1ba48###Sonepar Client Cost - MC_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar Mobile App_6448a96c3be37902a3f1ba48"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2284_SEO_64a4e0591734471c30843fc0",
      "nodeName": "CMS_Sep1_2023Rel.09_SEO",
      "sprintStartDate": "2023-08-07T14:41:44.342Z",
      "sprintEndDate": "2023-09-04T14:32:00.000Z",
      "path": [
        "SEO_64a4e0591734471c30843fc0###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "SEO_64a4e0591734471c30843fc0"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2175_R1+ Frontline_647588bc5286e83998a5609c",
      "nodeName": "FRONT_Sprint 23.20_R1+ Frontline",
      "sprintStartDate": "2023-09-27T17:06:36.303Z",
      "sprintEndDate": "2023-10-11T19:39:29.000Z",
      "path": [
        "R1+ Frontline_647588bc5286e83998a5609c###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "R1+ Frontline_647588bc5286e83998a5609c"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "39692_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.12.1487 iOS_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-06-04T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Team 2_sqd_647588bc5286e83998a5609c",
      "nodeName": "Team 2",
      "path": "2214_R1+ Frontline_647588bc5286e83998a5609c###R1+ Frontline_647588bc5286e83998a5609c###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2214_R1+ Frontline_647588bc5286e83998a5609c",
      "level": 7
    },
    {
      "nodeId": "3366_Sonepar INT_6542b4bb08f31814845119b2",
      "nodeName": "USA-SPRFLD Sprint PI 14.2_Sonepar INT",
      "sprintStartDate": "2023-09-28T18:16:06.168Z",
      "sprintEndDate": "2023-10-10T22:00:00.000Z",
      "path": [
        "Sonepar INT_6542b4bb08f31814845119b2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar INT_6542b4bb08f31814845119b2"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Team 1_sqd_6449103b3be37902a3f1ba70",
      "nodeName": "Team 1",
      "path": "2326_GearBox Squad 1_6449103b3be37902a3f1ba70###GearBox Squad 1_6449103b3be37902a3f1ba70###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2326_GearBox Squad 1_6449103b3be37902a3f1ba70",
      "level": 7
    },
    {
      "nodeId": "2315_Data Engineering_644258b830d86a7f539c7fd7",
      "nodeName": "DA Sprint 23.22_Data Engineering",
      "sprintStartDate": "2023-10-25T11:02:40.231Z",
      "sprintEndDate": "2023-11-08T03:30:00.000Z",
      "path": [
        "Data Engineering_644258b830d86a7f539c7fd7###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Data Engineering_644258b830d86a7f539c7fd7"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "3202_Sonepar SRE_6542b86f08f31814845119be",
      "nodeName": "AFS - S14.6_Sonepar SRE",
      "sprintStartDate": "2023-11-23T20:55:23.671Z",
      "sprintEndDate": "2023-12-06T22:00:00.000Z",
      "path": [
        "Sonepar SRE_6542b86f08f31814845119be###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar SRE_6542b86f08f31814845119be"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "Team 3_sqd_63a02b61bbc09e116d744d9d",
      "nodeName": "Team 3",
      "path": "2370_GearBox_63a02b61bbc09e116d744d9d###GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2370_GearBox_63a02b61bbc09e116d744d9d",
      "level": 7
    },
    {
      "nodeId": "11783_Sonepar MAP_6542b43f08f31814845119ab",
      "nodeName": "BE next RC S&P_Sonepar MAP",
      "path": [
        "Sonepar MAP_6542b43f08f31814845119ab###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar MAP_6542b43f08f31814845119ab"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-27T00:00:00.000Z",
      "releaseStartDate": "2023-10-23T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2393_VDOS Outside Hauler_647702b25286e83998a56138",
      "nodeName": "OMS_Sprint 23.23_VDOS Outside Hauler",
      "sprintStartDate": "2023-11-09T13:38:16.954Z",
      "sprintEndDate": "2023-11-21T05:00:00.000Z",
      "path": [
        "VDOS Outside Hauler_647702b25286e83998a56138###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "VDOS Outside Hauler_647702b25286e83998a56138"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Team 2_sqd_63a02b61bbc09e116d744d9d",
      "nodeName": "Team 2",
      "path": "2229_GearBox_63a02b61bbc09e116d744d9d###GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2229_GearBox_63a02b61bbc09e116d744d9d",
      "level": 7
    },
    {
      "nodeId": "Energy & Commodities_ver",
      "nodeName": "Energy & Commodities",
      "path": "EU_bu",
      "labelName": "ver",
      "parentId": "EU_bu",
      "level": 2
    },
    {
      "nodeId": "Salesforce_sqd_6377306a175a953a0a49d322",
      "nodeName": "Salesforce",
      "path": "2287_Integration Services_6377306a175a953a0a49d322###Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2287_Integration Services_6377306a175a953a0a49d322",
      "level": 7
    },
    {
      "nodeId": "VDOS/TMS_sqd_6377306a175a953a0a49d322",
      "nodeName": "VDOS/TMS",
      "path": [
        "2287_Integration Services_6377306a175a953a0a49d322###Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2377_Integration Services_6377306a175a953a0a49d322###Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sqd",
      "parentId": [
        "2287_Integration Services_6377306a175a953a0a49d322",
        "2377_Integration Services_6377306a175a953a0a49d322"
      ],
      "level": 7
    },
    {
      "nodeId": "39839_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.12.2156 Android_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-06-19T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "1160_SEO_64a4e0591734471c30843fc0",
      "nodeName": "SEO_Jun9_2023_Rel6_SEO",
      "sprintStartDate": "2023-05-15T19:57:09.298Z",
      "sprintEndDate": "2023-06-11T19:53:00.000Z",
      "path": [
        "SEO_64a4e0591734471c30843fc0###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "SEO_64a4e0591734471c30843fc0"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "3345_Sonepar SRE_6542b86f08f31814845119be",
      "nodeName": "OPS-S 14.1 / 73_Sonepar SRE",
      "sprintStartDate": "2023-09-22T16:23:46.060Z",
      "sprintEndDate": "2023-09-28T16:19:00.000Z",
      "path": [
        "Sonepar SRE_6542b86f08f31814845119be###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar SRE_6542b86f08f31814845119be"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "15309_DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "nodeName": "CADILLAC_DRP - Discovery POD",
      "path": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1"
      ],
      "level": 6,
      "releaseEndDate": "2021-02-10T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "11789_Sonepar BUY_6542b42308f31814845119a8",
      "nodeName": "v1.405 BE B&D_Sonepar BUY",
      "path": [
        "Sonepar BUY_6542b42308f31814845119a8###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar BUY_6542b42308f31814845119a8"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-21T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Released"
    },
    {
      "nodeId": "2240_Website TOF_6441052372a7c53c78f70588",
      "nodeName": "Monthly Sprint (Aug Release)_Website TOF",
      "sprintStartDate": "2023-07-11T05:57:33.593Z",
      "sprintEndDate": "2023-07-30T05:00:00.000Z",
      "path": [
        "Website TOF_6441052372a7c53c78f70588###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Website TOF_6441052372a7c53c78f70588"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "11711_Onsite search_644131b98b61fa2477214bf3",
      "nodeName": "Website Jan 2024 Release_Onsite search",
      "path": [
        "Onsite search_644131b98b61fa2477214bf3###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Onsite search_644131b98b61fa2477214bf3"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-27T00:00:00.000Z",
      "releaseStartDate": "2023-10-02T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2206_CMS_644103e772a7c53c78f70582",
      "nodeName": "Cart&Checkout July07 2023 Rel7_CMS",
      "sprintStartDate": "2023-06-12T14:10:22.861Z",
      "sprintEndDate": "2023-07-09T14:43:00.000Z",
      "path": [
        "CMS_644103e772a7c53c78f70582###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "CMS_644103e772a7c53c78f70582"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "2293_P2P_64ca0b8f5fec906dbc18f3c5",
      "nodeName": "P2P_Sprint 23.23_P2P",
      "sprintStartDate": "2023-11-08T16:03:27.000Z",
      "sprintEndDate": "2023-11-21T23:00:00.000Z",
      "path": [
        "P2P_64ca0b8f5fec906dbc18f3c5###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "P2P_64ca0b8f5fec906dbc18f3c5"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "11807_Sonepar EAC_6542b3b008f31814845119a0",
      "nodeName": "v1.402 FE_Sonepar EAC",
      "path": [
        "Sonepar EAC_6542b3b008f31814845119a0###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar EAC_6542b3b008f31814845119a0"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-08T00:00:00.000Z",
      "releaseStartDate": "2023-11-02T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "Sonepar Global_6448a8213be37902a3f1ba45",
      "nodeName": "Sonepar Global",
      "path": "Sonepar Client Cost - MC_port###Sonepar SAS_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Sonepar Client Cost - MC_port",
      "level": 5,
      "basicProjectConfigId": "6448a8213be37902a3f1ba45"
    },
    {
      "nodeId": "1096_Promotion Engine BOF_64c178d7eb7015715615c5a6",
      "nodeName": "Search_Aug4_2023.Rel8_Promotion Engine BOF",
      "sprintStartDate": "2023-07-10T21:57:54.526Z",
      "sprintEndDate": "2023-08-06T22:46:00.000Z",
      "path": [
        "Promotion Engine BOF_64c178d7eb7015715615c5a6###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Promotion Engine BOF_64c178d7eb7015715615c5a6"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "4253_RABO Scrum (New)_64a5dac31734471c30844068",
      "nodeName": "Sentry FY24 Sprint 18_RABO Scrum (New)",
      "sprintStartDate": "2023-09-28T09:32:10.385Z",
      "sprintEndDate": "2023-10-10T23:07:00.000Z",
      "path": [
        "RABO Scrum (New)_64a5dac31734471c30844068###AA_port###ADEO_acc###Financial Services_ver###EU_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "RABO Scrum (New)_64a5dac31734471c30844068"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2355_VDOS Translations_651fe6bb1704342160f43511",
      "nodeName": "OMS_Sprint 23.22_VDOS Translations",
      "sprintStartDate": "2023-10-25T15:31:14.456Z",
      "sprintEndDate": "2023-11-08T15:31:03.000Z",
      "path": [
        "VDOS Translations_651fe6bb1704342160f43511###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "VDOS Translations_651fe6bb1704342160f43511"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "40412_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.14.2199 Android_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-09-18T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Team 1_sqd_63a02b61bbc09e116d744d9d",
      "nodeName": "Team 1",
      "path": "2318_GearBox_63a02b61bbc09e116d744d9d###GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2318_GearBox_63a02b61bbc09e116d744d9d",
      "level": 7
    },
    {
      "nodeId": "11717_Website TOF_6441052372a7c53c78f70588",
      "nodeName": "Website Oct 2023 - Feeds HF4 Release_Website TOF",
      "path": [
        "Website TOF_6441052372a7c53c78f70588###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Website TOF_6441052372a7c53c78f70588"
      ],
      "level": 6,
      "releaseEndDate": "2023-12-08T00:00:00.000Z",
      "releaseStartDate": "2023-11-27T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2189_My Account_6441081a72a7c53c78f70595",
      "nodeName": "CDP July 2023.Rel7_My Account",
      "sprintStartDate": "2023-06-12T15:19:27.641Z",
      "sprintEndDate": "2023-07-07T15:35:00.000Z",
      "path": [
        "My Account_6441081a72a7c53c78f70595###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "My Account_6441081a72a7c53c78f70595"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2364_R1+ Sales_648ab46f6803f300a9fd2e09",
      "nodeName": "SALES_Sprint 23.22_R1+ Sales",
      "sprintStartDate": "2023-10-25T20:54:45.436Z",
      "sprintEndDate": "2023-11-08T04:00:00.000Z",
      "path": [
        "R1+ Sales_648ab46f6803f300a9fd2e09###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "R1+ Sales_648ab46f6803f300a9fd2e09"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "15627_DRP - HomePage POD_64b3f315c4e72b57c94035e2",
      "nodeName": "Kingpin_DRP - HomePage POD",
      "path": [
        "DRP - HomePage POD_64b3f315c4e72b57c94035e2###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "DRP - HomePage POD_64b3f315c4e72b57c94035e2"
      ],
      "level": 6,
      "releaseEndDate": "2022-07-27T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Supply Chain_sqd_6377306a175a953a0a49d322",
      "nodeName": "Supply Chain",
      "path": "2320_Integration Services_6377306a175a953a0a49d322###Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2320_Integration Services_6377306a175a953a0a49d322",
      "level": 7
    },
    {
      "nodeId": "2287_Integration Services_6377306a175a953a0a49d322",
      "nodeName": "IS_Sprint 23.19_Integration Services",
      "sprintStartDate": "2023-09-13T12:43:35.712Z",
      "sprintEndDate": "2023-09-27T00:43:00.000Z",
      "path": [
        "Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Integration Services_6377306a175a953a0a49d322"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Team 2_sqd_647588bc5286e83998a5609c",
      "nodeName": "Team 2",
      "path": "2215_R1+ Frontline_647588bc5286e83998a5609c###R1+ Frontline_647588bc5286e83998a5609c###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2215_R1+ Frontline_647588bc5286e83998a5609c",
      "level": 7
    },
    {
      "nodeId": "2281_VDOS_63777558175a953a0a49d363",
      "nodeName": "OMS_Sprint 23.19_VDOS",
      "sprintStartDate": "2023-09-13T15:05:51.587Z",
      "sprintEndDate": "2023-09-27T15:05:00.000Z",
      "path": [
        "VDOS_63777558175a953a0a49d363###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "VDOS_63777558175a953a0a49d363"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Team 1_sqd_6449103b3be37902a3f1ba70",
      "nodeName": "Team 1",
      "path": "2317_GearBox Squad 1_6449103b3be37902a3f1ba70###GearBox Squad 1_6449103b3be37902a3f1ba70###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2317_GearBox Squad 1_6449103b3be37902a3f1ba70",
      "level": 7
    },
    {
      "nodeId": "Team 2_sqd_647588bc5286e83998a5609c",
      "nodeName": "Team 2",
      "path": "2213_R1+ Frontline_647588bc5286e83998a5609c###R1+ Frontline_647588bc5286e83998a5609c###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2213_R1+ Frontline_647588bc5286e83998a5609c",
      "level": 7
    },
    {
      "nodeId": "2242_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "nodeName": "DA Sprint 23.18_Dotcom + Mobile App",
      "sprintStartDate": "2023-08-30T09:55:02.326Z",
      "sprintEndDate": "2023-09-13T03:30:00.000Z",
      "path": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "2405_VDOS Outside Hauler_647702b25286e83998a56138",
      "nodeName": "OMS_Sprint 23.24_VDOS Outside Hauler",
      "sprintStartDate": "2023-11-21T16:22:25.465Z",
      "sprintEndDate": "2023-12-05T05:00:00.000Z",
      "path": [
        "VDOS Outside Hauler_647702b25286e83998a56138###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "VDOS Outside Hauler_647702b25286e83998a56138"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "Europe_bu",
      "nodeName": "Europe",
      "path": "",
      "labelName": "bu",
      "level": 1
    },
    {
      "nodeId": "286_PSknowHOW _6527af981704342160f43748",
      "nodeName": "KnowHOW | PI_15| ITR_4_PSknowHOW ",
      "sprintStartDate": "2023-11-08T09:30:00.000Z",
      "sprintEndDate": "2023-11-21T08:28:00.000Z",
      "path": [
        "PSknowHOW _6527af981704342160f43748###DTS_port###Methods and Tools_acc###PS Internal_ver###Internal_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "PSknowHOW _6527af981704342160f43748"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "3122_Sonepar INT_6542b4bb08f31814845119b2",
      "nodeName": "USA-SPRFLD S14.3_Sonepar INT",
      "sprintStartDate": "2023-10-12T03:02:18.000Z",
      "sprintEndDate": "2023-10-25T03:02:00.000Z",
      "path": [
        "Sonepar INT_6542b4bb08f31814845119b2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar INT_6542b4bb08f31814845119b2"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2369_GearBox_63a02b61bbc09e116d744d9d",
      "nodeName": "GB_Sprint 23.24_GearBox",
      "sprintStartDate": "2023-11-22T07:00:44.987Z",
      "sprintEndDate": "2023-12-06T03:30:00.000Z",
      "path": [
        "GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "GearBox_63a02b61bbc09e116d744d9d"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "11819_Sonepar eProcurement_6542947f08f3181484511988",
      "nodeName": "FE Next RC_Sonepar eProcurement",
      "path": [
        "Sonepar eProcurement_6542947f08f3181484511988###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar eProcurement_6542947f08f3181484511988"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-08T00:00:00.000Z",
      "releaseStartDate": "2023-11-06T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "11713_My Account_6441081a72a7c53c78f70595",
      "nodeName": "Website Oct 2023 - HF2 Release_My Account",
      "path": [
        "My Account_6441081a72a7c53c78f70595###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "My Account_6441081a72a7c53c78f70595"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-23T00:00:00.000Z",
      "releaseStartDate": "2023-10-16T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "12555_Unified Commerce - Dan's MVP_64ab97327d51263c17602b58",
      "nodeName": "DanMVP1.0_Prod_Unified Commerce - Dan's MVP",
      "path": [
        "Unified Commerce - Dan's MVP_64ab97327d51263c17602b58###Endeavour Group Pty Ltd_port###Endeavour Group Limited_acc###Retail_ver###International_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Unified Commerce - Dan's MVP_64ab97327d51263c17602b58"
      ],
      "level": 6,
      "releaseEndDate": "2024-05-17T00:00:00.000Z",
      "releaseStartDate": "2023-04-05T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Team 2_sqd_647588bc5286e83998a5609c",
      "nodeName": "Team 2",
      "path": "2212_R1+ Frontline_647588bc5286e83998a5609c###R1+ Frontline_647588bc5286e83998a5609c###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2212_R1+ Frontline_647588bc5286e83998a5609c",
      "level": 7
    },
    {
      "nodeId": "2174_R1+ Frontline_647588bc5286e83998a5609c",
      "nodeName": "FRONT_Sprint 23.19_R1+ Frontline",
      "sprintStartDate": "2023-09-13T18:10:12.884Z",
      "sprintEndDate": "2023-09-27T20:43:00.000Z",
      "path": [
        "R1+ Frontline_647588bc5286e83998a5609c###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "R1+ Frontline_647588bc5286e83998a5609c"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2310_Website TOF_6441052372a7c53c78f70588",
      "nodeName": "Search_Nov24_2023.Rel12_Website TOF",
      "sprintStartDate": "2023-10-30T21:41:45.135Z",
      "sprintEndDate": "2023-11-26T22:30:26.000Z",
      "path": [
        "Website TOF_6441052372a7c53c78f70588###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Website TOF_6441052372a7c53c78f70588"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "Team 4_sqd_648ab6186803f300a9fd2e0e",
      "nodeName": "Team 4",
      "path": "2203_R1+ Logistics_648ab6186803f300a9fd2e0e###R1+ Logistics_648ab6186803f300a9fd2e0e###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2203_R1+ Logistics_648ab6186803f300a9fd2e0e",
      "level": 7
    },
    {
      "nodeId": "Legacy CC_sqd_64be65cceb7015715615c4ba",
      "nodeName": "Legacy CC",
      "path": "2268_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2268_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "level": 7
    },
    {
      "nodeId": "10385_Cart & checkout_6441078b72a7c53c78f70590",
      "nodeName": "Promotions Jan 2024 Release_Cart & checkout",
      "path": [
        "Cart & checkout_6441078b72a7c53c78f70590###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Cart & checkout_6441078b72a7c53c78f70590"
      ],
      "level": 6,
      "releaseEndDate": "2024-01-25T00:00:00.000Z",
      "releaseStartDate": "2023-07-01T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2369_GearBox Squad 2_64770ec45286e83998a56141",
      "nodeName": "GB_Sprint 23.24_GearBox Squad 2",
      "sprintStartDate": "2023-11-22T07:00:44.987Z",
      "sprintEndDate": "2023-12-06T03:30:00.000Z",
      "path": [
        "GearBox Squad 2_64770ec45286e83998a56141###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "GearBox Squad 2_64770ec45286e83998a56141"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "Team 3_sqd_64770ef45286e83998a56143",
      "nodeName": "Team 3",
      "path": "2318_GearBox Squad 3_64770ef45286e83998a56143###GearBox Squad 3_64770ef45286e83998a56143###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2318_GearBox Squad 3_64770ef45286e83998a56143",
      "level": 7
    },
    {
      "nodeId": "Commerce_sqd_64be65cceb7015715615c4ba",
      "nodeName": "Commerce",
      "path": "2265_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2265_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "level": 7
    },
    {
      "nodeId": "Salesforce_sqd_6377306a175a953a0a49d322",
      "nodeName": "Salesforce",
      "path": "2377_Integration Services_6377306a175a953a0a49d322###Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2377_Integration Services_6377306a175a953a0a49d322",
      "level": 7
    },
    {
      "nodeId": "53769_pod16 2_657219fd78247c3cd726d630",
      "nodeName": "POD 16 | Sprint 23_14_pod16 2",
      "sprintStartDate": "2023-09-28T05:00:00.000Z",
      "sprintEndDate": "2023-10-11T13:00:00.000Z",
      "path": [
        "pod16 2_657219fd78247c3cd726d630###2021 WLP Brand Retainer_port###AAA Auto Club Group_acc###Automative1_ver###A_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "pod16 2_657219fd78247c3cd726d630"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Team 1_sqd_6494298bca84920b10dddd3b",
      "nodeName": "Team 1",
      "path": "2304_Service & Assets_6494298bca84920b10dddd3b###Service & Assets_6494298bca84920b10dddd3b###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2304_Service & Assets_6494298bca84920b10dddd3b",
      "level": 7
    },
    {
      "nodeId": "1016_DRP - HomePage POD_64b3f315c4e72b57c94035e2",
      "nodeName": "DRP Sprint 86_DRP - HomePage POD",
      "sprintStartDate": "2023-11-24T14:39:59.215Z",
      "sprintEndDate": "2023-12-14T04:00:00.000Z",
      "path": [
        "DRP - HomePage POD_64b3f315c4e72b57c94035e2###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "DRP - HomePage POD_64b3f315c4e72b57c94035e2"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "13600_Ecom Post-Purchase Squad_64be67caeb7015715615c4c5",
      "nodeName": "SBRWEB 1.6.1_Ecom Post-Purchase Squad",
      "path": [
        "Ecom Post-Purchase Squad_64be67caeb7015715615c4c5###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Ecom Post-Purchase Squad_64be67caeb7015715615c4c5"
      ],
      "level": 6,
      "releaseEndDate": "2023-12-13T00:00:00.000Z",
      "releaseStartDate": "2023-09-27T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Team 3_sqd_63a02b61bbc09e116d744d9d",
      "nodeName": "Team 3",
      "path": "1086_GearBox_63a02b61bbc09e116d744d9d###GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "1086_GearBox_63a02b61bbc09e116d744d9d",
      "level": 7
    },
    {
      "nodeId": "2213_R1+ Frontline_647588bc5286e83998a5609c",
      "nodeName": "FRONT_Sprint 23.22_R1+ Frontline",
      "sprintStartDate": "2023-10-25T15:31:09.148Z",
      "sprintEndDate": "2023-11-08T18:03:02.000Z",
      "path": [
        "R1+ Frontline_647588bc5286e83998a5609c###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "R1+ Frontline_647588bc5286e83998a5609c"
      ],
      "sprintState": "closed",
      "level": 6
    },
    {
      "nodeId": "11713_PIM_64a4e09e1734471c30843fc2",
      "nodeName": "Website Oct 2023 - HF2 Release_PIM",
      "path": [
        "PIM_64a4e09e1734471c30843fc2###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "PIM_64a4e09e1734471c30843fc2"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-23T00:00:00.000Z",
      "releaseStartDate": "2023-10-16T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "3211_Sonepar MAP_6542b43f08f31814845119ab",
      "nodeName": "M&P - S14.3_Sonepar MAP",
      "sprintStartDate": "2023-10-12T08:36:49.124Z",
      "sprintEndDate": "2023-10-25T12:00:00.000Z",
      "path": [
        "Sonepar MAP_6542b43f08f31814845119ab###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar MAP_6542b43f08f31814845119ab"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Team 2_sqd_648ab6186803f300a9fd2e0e",
      "nodeName": "Team 2",
      "path": "2203_R1+ Logistics_648ab6186803f300a9fd2e0e###R1+ Logistics_648ab6186803f300a9fd2e0e###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2203_R1+ Logistics_648ab6186803f300a9fd2e0e",
      "level": 7
    },
    {
      "nodeId": "2700_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "Sprint 118_RMMO",
      "sprintStartDate": "2023-07-11T10:22:14.707Z",
      "sprintEndDate": "2023-07-24T12:03:00.000Z",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "10150_Test rc_657056c668a1225c0126c843",
      "nodeName": "KnowHOW v8.2.0_Test rc",
      "path": [
        "Test rc_657056c668a1225c0126c843###2021 WLP Brand Retainer_port###AB Tetra Pak_acc###Consumer Products_ver###Europe_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Test rc_657056c668a1225c0126c843"
      ],
      "level": 6,
      "releaseEndDate": "2023-12-07T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "38803_RABO Scrum (New)_64a5dac31734471c30844068",
      "nodeName": "FY24 Q4_RABO Scrum (New)",
      "path": [
        "RABO Scrum (New)_64a5dac31734471c30844068###AA_port###ADEO_acc###Financial Services_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RABO Scrum (New)_64a5dac31734471c30844068"
      ],
      "level": 6,
      "releaseEndDate": "2024-01-31T00:00:00.000Z",
      "releaseStartDate": "2023-09-28T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2373_Data Visualization_64425a0a30d86a7f539c7fdc",
      "nodeName": "DV Sprint 23.23_Data Visualization",
      "sprintStartDate": "2023-11-08T05:09:08.191Z",
      "sprintEndDate": "2023-11-22T04:30:00.000Z",
      "path": [
        "Data Visualization_64425a0a30d86a7f539c7fdc###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Data Visualization_64425a0a30d86a7f539c7fdc"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "13621_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "nodeName": "SBRAPP 3.6 _Dotcom + Mobile App",
      "path": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba"
      ],
      "level": 6,
      "releaseEndDate": "2024-04-24T00:00:00.000Z",
      "releaseStartDate": "2024-01-17T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "15205_DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "nodeName": "Tyrion _DRP - Discovery POD",
      "path": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1"
      ],
      "level": 6,
      "releaseEndDate": "2020-08-19T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2275_Data Visualization_64425a0a30d86a7f539c7fdc",
      "nodeName": "DEA Sprint 23.22_Data Visualization",
      "sprintStartDate": "2023-10-25T06:34:30.591Z",
      "sprintEndDate": "2023-11-08T03:30:00.000Z",
      "path": [
        "Data Visualization_64425a0a30d86a7f539c7fdc###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Data Visualization_64425a0a30d86a7f539c7fdc"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "2370_GearBox_63a02b61bbc09e116d744d9d",
      "nodeName": "GB_Sprint 23.25_GearBox",
      "sprintStartDate": "2023-12-06T11:14:00.668Z",
      "sprintEndDate": "2023-12-20T03:30:00.000Z",
      "path": [
        "GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "GearBox_63a02b61bbc09e116d744d9d"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "2293_ASO Mobile App_64a4fab01734471c30843fda",
      "nodeName": "MyAccount_Sep01_2023.Rel9_ASO Mobile App",
      "sprintStartDate": "2023-08-07T13:06:10.926Z",
      "sprintEndDate": "2023-09-01T16:15:00.000Z",
      "path": [
        "ASO Mobile App_64a4fab01734471c30843fda###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "ASO Mobile App_64a4fab01734471c30843fda"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "151117_Do it Best_657049b505ce0569d5612ec7",
      "nodeName": "FEAEM_HOTFIX_RV1.5_01_Do it Best",
      "path": [
        "Do it Best_657049b505ce0569d5612ec7###3PP CRM_port###ADEO_acc###Automotive_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Do it Best_657049b505ce0569d5612ec7"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-17T00:00:00.000Z",
      "releaseStartDate": "2023-11-16T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "40969_SAME_63a4810c09378702f4eab210",
      "nodeName": "1.3.37.4_SAME",
      "path": [
        "SAME_63a4810c09378702f4eab210###ACE20001_port###Abu Dhabi Investment Authority_acc###PS Internal_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "SAME_63a4810c09378702f4eab210"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-05T00:00:00.000Z",
      "releaseStartDate": "2023-11-03T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "2137_Data Engineering_644258b830d86a7f539c7fd7",
      "nodeName": "DV Sprint 23.14_Data Engineering",
      "sprintStartDate": "2023-07-05T04:26:43.065Z",
      "sprintEndDate": "2023-07-19T03:30:00.000Z",
      "path": [
        "Data Engineering_644258b830d86a7f539c7fd7###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Data Engineering_644258b830d86a7f539c7fd7"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "3201_Sonepar AFS_654135db88c4b8114af77dba",
      "nodeName": "AFS - S14.5_Sonepar AFS",
      "sprintStartDate": "2023-11-09T16:30:42.172Z",
      "sprintEndDate": "2023-11-22T22:00:00.000Z",
      "path": [
        "Sonepar AFS_654135db88c4b8114af77dba###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar AFS_654135db88c4b8114af77dba"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "nodeName": "DRP - Discovery POD",
      "path": "Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Retail_port",
      "level": 5,
      "basicProjectConfigId": "63dc01e47228be4c30553ce1"
    },
    {
      "nodeId": "R1+ Frontline_647588bc5286e83998a5609c",
      "nodeName": "R1+ Frontline",
      "path": "Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Sunbelt Rentals_port",
      "level": 5,
      "basicProjectConfigId": "647588bc5286e83998a5609c"
    },
    {
      "nodeId": "40303_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.13.1507 iOS_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-08-23T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "54554_DHL Logistics Scrumban_6549029708f3181484511bbb",
      "nodeName": "FT1 Sprint 84_DHL Logistics Scrumban",
      "sprintStartDate": "2023-10-31T06:20:00.000Z",
      "sprintEndDate": "2023-11-13T06:20:00.000Z",
      "path": [
        "DHL Logistics Scrumban_6549029708f3181484511bbb###DPDHL - CSI DCI - Logistics and CJ_port###Deutsche Post AG_acc###Automotive_ver###EU_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "DHL Logistics Scrumban_6549029708f3181484511bbb"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "3191_Sonepar EAC_6542b3b008f31814845119a0",
      "nodeName": "Engage 14.1_Sonepar EAC",
      "sprintStartDate": "2023-09-15T08:15:37.712Z",
      "sprintEndDate": "2023-09-28T08:15:00.000Z",
      "path": [
        "Sonepar EAC_6542b3b008f31814845119a0###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar EAC_6542b3b008f31814845119a0"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "54237_Do it Best_657049b505ce0569d5612ec7",
      "nodeName": "Do it Best - Sprint V1.5_Do it Best",
      "sprintStartDate": "2023-11-01T11:00:00.000Z",
      "sprintEndDate": "2023-11-15T02:00:00.000Z",
      "path": [
        "Do it Best_657049b505ce0569d5612ec7###3PP CRM_port###ADEO_acc###Automotive_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Do it Best_657049b505ce0569d5612ec7"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "11819_Design System_64ad9e667d51263c17602c67",
      "nodeName": "FE Next RC_Design System",
      "path": [
        "Design System_64ad9e667d51263c17602c67###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Design System_64ad9e667d51263c17602c67"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-08T00:00:00.000Z",
      "releaseStartDate": "2023-11-06T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "11649_Sonepar INT_6542b4bb08f31814845119b2",
      "nodeName": "[USA-SPRFLD] Checkout Ready_Sonepar INT",
      "path": [
        "Sonepar INT_6542b4bb08f31814845119b2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar INT_6542b4bb08f31814845119b2"
      ],
      "level": 6,
      "releaseEndDate": "2023-08-02T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "15560_DRP - HomePage POD_64b3f315c4e72b57c94035e2",
      "nodeName": "Daredevil_DRP - HomePage POD",
      "path": [
        "DRP - HomePage POD_64b3f315c4e72b57c94035e2###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "DRP - HomePage POD_64b3f315c4e72b57c94035e2"
      ],
      "level": 6,
      "releaseEndDate": "2022-04-20T00:00:00.000Z",
      "releaseStartDate": "2022-04-20T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "1176_ASO Mobile App_64a4fab01734471c30843fda",
      "nodeName": "MobileAppChk_04 Aug_ASO Mobile App",
      "sprintStartDate": "2023-07-10T13:35:54.903Z",
      "sprintEndDate": "2023-08-04T13:35:00.000Z",
      "path": [
        "ASO Mobile App_64a4fab01734471c30843fda###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "ASO Mobile App_64a4fab01734471c30843fda"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2405_VDOS Translations_651fe6bb1704342160f43511",
      "nodeName": "OMS_Sprint 23.24_VDOS Translations",
      "sprintStartDate": "2023-11-21T16:22:25.465Z",
      "sprintEndDate": "2023-12-05T05:00:00.000Z",
      "path": [
        "VDOS Translations_651fe6bb1704342160f43511###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "VDOS Translations_651fe6bb1704342160f43511"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "Regina Maria Portofolio_port",
      "nodeName": "Regina Maria Portofolio",
      "path": "Regina Maria_acc###Health_ver###EU_bu",
      "labelName": "port",
      "parentId": "Regina Maria_acc",
      "level": 4
    },
    {
      "nodeId": "2288_Onsite search_644131b98b61fa2477214bf3",
      "nodeName": "WebsiteTOF_Sep 29_2023_Rel10_Onsite search",
      "sprintStartDate": "2023-09-04T14:41:58.069Z",
      "sprintEndDate": "2023-10-02T14:41:00.000Z",
      "path": [
        "Onsite search_644131b98b61fa2477214bf3###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Onsite search_644131b98b61fa2477214bf3"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "13621_Ecom Purchase Squad_64be677aeb7015715615c4c1",
      "nodeName": "SBRAPP 3.6 _Ecom Purchase Squad",
      "path": [
        "Ecom Purchase Squad_64be677aeb7015715615c4c1###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Ecom Purchase Squad_64be677aeb7015715615c4c1"
      ],
      "level": 6,
      "releaseEndDate": "2024-04-24T00:00:00.000Z",
      "releaseStartDate": "2024-01-17T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "11854_Sonepar AFS_654135db88c4b8114af77dba",
      "nodeName": "v1.399.1 BE Hotfix AFS_Sonepar AFS",
      "path": [
        "Sonepar AFS_654135db88c4b8114af77dba###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar AFS_654135db88c4b8114af77dba"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-30T00:00:00.000Z",
      "releaseStartDate": "2023-11-30T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "4365_RABO Scrum (New)_64a5dac31734471c30844068",
      "nodeName": "YA/UA FY24 Sprint 21_RABO Scrum (New)",
      "sprintStartDate": "2023-11-08T14:10:00.863Z",
      "sprintEndDate": "2023-11-23T09:30:00.000Z",
      "path": [
        "RABO Scrum (New)_64a5dac31734471c30844068###AA_port###ADEO_acc###Financial Services_ver###EU_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "RABO Scrum (New)_64a5dac31734471c30844068"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "2259_Website TOF_6441052372a7c53c78f70588",
      "nodeName": "Week 25 Sprint_Website TOF",
      "sprintStartDate": "2023-06-19T03:40:28.464Z",
      "sprintEndDate": "2023-06-25T05:00:00.000Z",
      "path": [
        "Website TOF_6441052372a7c53c78f70588###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Website TOF_6441052372a7c53c78f70588"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "10385_Website TOF_6441052372a7c53c78f70588",
      "nodeName": "Promotions Aug 2023 Release_Website TOF",
      "path": [
        "Website TOF_6441052372a7c53c78f70588###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Website TOF_6441052372a7c53c78f70588"
      ],
      "level": 6,
      "releaseEndDate": "2024-01-25T00:00:00.000Z",
      "releaseStartDate": "2023-07-01T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2737_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "Sprint 120_RMMO",
      "sprintStartDate": "2023-08-08T08:55:58.061Z",
      "sprintEndDate": "2023-08-28T12:03:00.000Z",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "10170_Test rc_657056c668a1225c0126c843",
      "nodeName": "KnowHOW v9.2.0_Test rc",
      "path": [
        "Test rc_657056c668a1225c0126c843###2021 WLP Brand Retainer_port###AB Tetra Pak_acc###Consumer Products_ver###Europe_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Test rc_657056c668a1225c0126c843"
      ],
      "level": 6,
      "releaseEndDate": "2024-02-08T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "ASO_port",
      "nodeName": "ASO",
      "path": "Academy Sports_acc###Retail_ver###North America_bu",
      "labelName": "port",
      "parentId": "Academy Sports_acc",
      "level": 4
    },
    {
      "nodeId": "Automotive_ver",
      "nodeName": "Automotive",
      "path": "A_bu",
      "labelName": "ver",
      "parentId": "A_bu",
      "level": 2
    },
    {
      "nodeId": "PS Internal_ver",
      "nodeName": "PS Internal",
      "path": "EU_bu",
      "labelName": "ver",
      "parentId": "EU_bu",
      "level": 2
    },
    {
      "nodeId": "2391_Cart & checkout_6441078b72a7c53c78f70590",
      "nodeName": "Cart&Checkout Dec22 2023 Rel13_Cart & checkout",
      "sprintStartDate": "2023-11-27T14:14:53.159Z",
      "sprintEndDate": "2023-12-25T05:00:00.000Z",
      "path": [
        "Cart & checkout_6441078b72a7c53c78f70590###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Cart & checkout_6441078b72a7c53c78f70590"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "3209_Sonepar MAP_6542b43f08f31814845119ab",
      "nodeName": "M&P - S14.1_Sonepar MAP",
      "sprintStartDate": "2023-09-15T07:11:17.815Z",
      "sprintEndDate": "2023-09-27T10:43:00.000Z",
      "path": [
        "Sonepar MAP_6542b43f08f31814845119ab###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar MAP_6542b43f08f31814845119ab"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "10139_Test rc_657056c668a1225c0126c843",
      "nodeName": "KnowHOW v8.1.0_Test rc",
      "path": [
        "Test rc_657056c668a1225c0126c843###2021 WLP Brand Retainer_port###AB Tetra Pak_acc###Consumer Products_ver###Europe_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Test rc_657056c668a1225c0126c843"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-28T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Released"
    },
    {
      "nodeId": "39787_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.12.2154 Android_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-06-13T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2326_GearBox Squad 1_6449103b3be37902a3f1ba70",
      "nodeName": "GB_Sprint 23.23_GearBox Squad 1",
      "sprintStartDate": "2023-11-08T11:28:16.595Z",
      "sprintEndDate": "2023-11-22T03:30:00.000Z",
      "path": [
        "GearBox Squad 1_6449103b3be37902a3f1ba70###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "GearBox Squad 1_6449103b3be37902a3f1ba70"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "10170_PSknowHOW _6527af981704342160f43748",
      "nodeName": "KnowHOW v9.2.0_PSknowHOW ",
      "path": [
        "PSknowHOW _6527af981704342160f43748###DTS_port###Methods and Tools_acc###PS Internal_ver###Internal_bu"
      ],
      "labelName": "release",
      "parentId": [
        "PSknowHOW _6527af981704342160f43748"
      ],
      "level": 6,
      "releaseEndDate": "2024-02-08T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Team 3_sqd_63a02b61bbc09e116d744d9d",
      "nodeName": "Team 3",
      "path": "2229_GearBox_63a02b61bbc09e116d744d9d###GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2229_GearBox_63a02b61bbc09e116d744d9d",
      "level": 7
    },
    {
      "nodeId": "285_Test rc_657056c668a1225c0126c843",
      "nodeName": "KnowHOW | PI_15| ITR_3_Test rc",
      "sprintStartDate": "2023-10-25T08:39:36.159Z",
      "sprintEndDate": "2023-11-07T08:20:00.000Z",
      "path": [
        "Test rc_657056c668a1225c0126c843###2021 WLP Brand Retainer_port###AB Tetra Pak_acc###Consumer Products_ver###Europe_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Test rc_657056c668a1225c0126c843"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "53769_POD 16_657065700615235d92401735",
      "nodeName": "POD 16 | Sprint 23_14_POD 16",
      "sprintStartDate": "2023-09-28T10:30:00.000Z",
      "sprintEndDate": "2023-10-11T18:30:00.000Z",
      "path": [
        "POD 16_657065700615235d92401735###Nissan Core Team - PS_port###Nissan Motor Co. Ltd._acc###Automotive_ver###International_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "POD 16_657065700615235d92401735"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "3346_Sonepar SRE_6542b86f08f31814845119be",
      "nodeName": "OPS-S 14.2 / 74_Sonepar SRE",
      "sprintStartDate": "2023-09-29T13:53:08.603Z",
      "sprintEndDate": "2023-10-12T13:49:00.000Z",
      "path": [
        "Sonepar SRE_6542b86f08f31814845119be###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar SRE_6542b86f08f31814845119be"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "1096_Onsite search_644131b98b61fa2477214bf3",
      "nodeName": "Search_Aug4_2023.Rel8_Onsite search",
      "sprintStartDate": "2023-07-10T21:57:54.526Z",
      "sprintEndDate": "2023-08-06T22:46:00.000Z",
      "path": [
        "Onsite search_644131b98b61fa2477214bf3###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Onsite search_644131b98b61fa2477214bf3"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2297_Onsite search_644131b98b61fa2477214bf3",
      "nodeName": "Promo_Engine_TOF_Sep_1_Onsite search",
      "sprintStartDate": "2023-08-07T21:54:42.268Z",
      "sprintEndDate": "2023-09-01T17:01:00.000Z",
      "path": [
        "Onsite search_644131b98b61fa2477214bf3###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Onsite search_644131b98b61fa2477214bf3"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "2365_My Account_6441081a72a7c53c78f70595",
      "nodeName": "More_Options_22_Dec_2023_Rel13_My Account",
      "sprintStartDate": "2023-11-28T05:36:40.485Z",
      "sprintEndDate": "2023-12-23T05:30:00.000Z",
      "path": [
        "My Account_6441081a72a7c53c78f70595###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "My Account_6441081a72a7c53c78f70595"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "151413_Do it Best_657049b505ce0569d5612ec7",
      "nodeName": "MAG-RV1.5_Do it Best",
      "path": [
        "Do it Best_657049b505ce0569d5612ec7###3PP CRM_port###ADEO_acc###Automotive_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Do it Best_657049b505ce0569d5612ec7"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-14T00:00:00.000Z",
      "releaseStartDate": "2023-11-01T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "ADEO_acc",
      "nodeName": "ADEO",
      "path": "Financial Services_ver###EU_bu",
      "labelName": "acc",
      "parentId": "Financial Services_ver",
      "level": 3
    },
    {
      "nodeId": "2323_VDOS Translations_651fe6bb1704342160f43511",
      "nodeName": "OMS_Sprint 23.20_VDOS Translations",
      "sprintStartDate": "2023-09-28T12:04:38.169Z",
      "sprintEndDate": "2023-10-11T04:00:00.000Z",
      "path": [
        "VDOS Translations_651fe6bb1704342160f43511###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "VDOS Translations_651fe6bb1704342160f43511"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "3PP - Cross Regional_port",
      "nodeName": "3PP - Cross Regional",
      "path": "ADNOC Global Trading Limited_acc###Consumer Products_ver###Government Services_bu",
      "labelName": "port",
      "parentId": "ADNOC Global Trading Limited_acc",
      "level": 4
    },
    {
      "nodeId": "AAA Auto Club Group_acc",
      "nodeName": "AAA Auto Club Group",
      "path": "Automotive_ver###A_bu",
      "labelName": "acc",
      "parentId": "Automotive_ver",
      "level": 3
    },
    {
      "nodeId": "11208_Sonepar BUY_6542b42308f31814845119a8",
      "nodeName": "V1.393 BE B&D_Sonepar BUY",
      "path": [
        "Sonepar BUY_6542b42308f31814845119a8###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar BUY_6542b42308f31814845119a8"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-26T00:00:00.000Z",
      "releaseStartDate": "2023-10-25T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "39885_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.12.1496 iOS_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-06-18T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Consumer Products_ver",
      "nodeName": "Consumer Products",
      "path": "EU_bu",
      "labelName": "ver",
      "parentId": "EU_bu",
      "level": 2
    },
    {
      "nodeId": "11774_Sonepar SAF_6542b3da08f31814845119a2",
      "nodeName": "v1.392 FE_Sonepar SAF",
      "path": [
        "Sonepar SAF_6542b3da08f31814845119a2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar SAF_6542b3da08f31814845119a2"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-26T00:00:00.000Z",
      "releaseStartDate": "2023-10-23T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "2797_SAME_63a4810c09378702f4eab210",
      "nodeName": "Sprint 103 - Initial Plan_SAME",
      "sprintStartDate": "2023-11-22T08:02:24.906Z",
      "sprintEndDate": "2023-12-04T22:00:00.000Z",
      "path": [
        "SAME_63a4810c09378702f4eab210###ACE20001_port###Abu Dhabi Investment Authority_acc###PS Internal_ver###EU_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "SAME_63a4810c09378702f4eab210"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "2021 WLP Brand Retainer_port",
      "nodeName": "2021 WLP Brand Retainer",
      "path": "AB Tetra Pak_acc###B_ver###Government Services_bu",
      "labelName": "port",
      "parentId": "AB Tetra Pak_acc",
      "level": 4
    },
    {
      "nodeId": "11825_Sonepar EAC_6542b3b008f31814845119a0",
      "nodeName": "v1.406 FE RC_Sonepar EAC",
      "path": [
        "Sonepar EAC_6542b3b008f31814845119a0###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar EAC_6542b3b008f31814845119a0"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-22T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Released"
    },
    {
      "nodeId": "Team 1_sqd_6494298bca84920b10dddd3b",
      "nodeName": "Team 1",
      "path": "2232_Service & Assets_6494298bca84920b10dddd3b###Service & Assets_6494298bca84920b10dddd3b###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2232_Service & Assets_6494298bca84920b10dddd3b",
      "level": 7
    },
    {
      "nodeId": "133809_pod16 2_657219fd78247c3cd726d630",
      "nodeName": "Release 23.03_pod16 2",
      "path": [
        "pod16 2_657219fd78247c3cd726d630###2021 WLP Brand Retainer_port###AAA Auto Club Group_acc###Automative1_ver###A_bu"
      ],
      "labelName": "release",
      "parentId": [
        "pod16 2_657219fd78247c3cd726d630"
      ],
      "level": 6,
      "releaseEndDate": "2023-03-08T00:00:00.000Z",
      "releaseStartDate": "2023-02-21T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "124101_pod16 2_657219fd78247c3cd726d630",
      "nodeName": "Release 22.05_pod16 2",
      "path": [
        "pod16 2_657219fd78247c3cd726d630###2021 WLP Brand Retainer_port###AAA Auto Club Group_acc###Automative1_ver###A_bu"
      ],
      "labelName": "release",
      "parentId": [
        "pod16 2_657219fd78247c3cd726d630"
      ],
      "level": 6,
      "releaseEndDate": "2022-05-04T00:00:00.000Z",
      "releaseStartDate": "2022-04-18T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2277_Data Visualization_64425a0a30d86a7f539c7fdc",
      "nodeName": "DV Sprint 23.21_Data Visualization",
      "sprintStartDate": "2023-10-11T04:02:22.561Z",
      "sprintEndDate": "2023-10-25T03:30:00.000Z",
      "path": [
        "Data Visualization_64425a0a30d86a7f539c7fdc###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Data Visualization_64425a0a30d86a7f539c7fdc"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "1056_Promotion Engine TOF_644105f972a7c53c78f7058c",
      "nodeName": "Promo_Engine_BOF_Jun_9_Promotion Engine TOF",
      "sprintStartDate": "2023-05-16T03:16:27.794Z",
      "sprintEndDate": "2023-06-10T14:16:00.000Z",
      "path": [
        "Promotion Engine TOF_644105f972a7c53c78f7058c###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Promotion Engine TOF_644105f972a7c53c78f7058c"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "2384_Integration Services_6377306a175a953a0a49d322",
      "nodeName": "IS_Sprint 23.23_Integration Services",
      "sprintStartDate": "2023-11-08T05:00:26.981Z",
      "sprintEndDate": "2023-11-22T05:00:00.000Z",
      "path": [
        "Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Integration Services_6377306a175a953a0a49d322"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "11651_Sonepar INT_6542b4bb08f31814845119b2",
      "nodeName": "[USA-SPRFLD] Order History Ready_Sonepar INT",
      "path": [
        "Sonepar INT_6542b4bb08f31814845119b2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar INT_6542b4bb08f31814845119b2"
      ],
      "level": 6,
      "releaseEndDate": "2023-07-19T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Team 1_sqd_6449103b3be37902a3f1ba70",
      "nodeName": "Team 1",
      "path": "2318_GearBox Squad 1_6449103b3be37902a3f1ba70###GearBox Squad 1_6449103b3be37902a3f1ba70###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2318_GearBox Squad 1_6449103b3be37902a3f1ba70",
      "level": 7
    },
    {
      "nodeId": "C&C San Francisco - PAS_port",
      "nodeName": "C&C San Francisco - PAS",
      "path": "City and County of San Francisco_acc###Financial Services_ver###North America_bu",
      "labelName": "port",
      "parentId": "City and County of San Francisco_acc",
      "level": 4
    },
    {
      "nodeId": "a193027c-65c7-410a-ac62-5c4d08af2e3d_Azure Project_656d830b9f546b19742cb55b",
      "nodeName": "Iteration 1_Azure Project",
      "sprintStartDate": "2023-05-25T00:00:00.000Z",
      "sprintEndDate": "2023-05-26T00:00:00.000Z",
      "path": [
        "Azure Project_656d830b9f546b19742cb55b###3PP CRM_port###ADEO_acc###B_ver###Europe_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Azure Project_656d830b9f546b19742cb55b"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "DRP - HomePage POD_64b3f315c4e72b57c94035e2",
      "nodeName": "DRP - HomePage POD",
      "path": "Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Retail_port",
      "level": 5,
      "basicProjectConfigId": "64b3f315c4e72b57c94035e2"
    },
    {
      "nodeId": "1008_DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "nodeName": "DRP Sprint 84_DRP - Discovery POD",
      "sprintStartDate": "2023-10-12T10:17:13.296Z",
      "sprintEndDate": "2023-11-01T09:40:00.000Z",
      "path": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "0ed77670-e2f8-4145-8379-0ed14674f059_Azure Project_656d830b9f546b19742cb55b",
      "nodeName": "Iteration4_Azure Project",
      "sprintStartDate": "2023-06-02T00:00:00.000Z",
      "sprintEndDate": "2023-06-05T00:00:00.000Z",
      "path": [
        "Azure Project_656d830b9f546b19742cb55b###3PP CRM_port###ADEO_acc###B_ver###Europe_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Azure Project_656d830b9f546b19742cb55b"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2303_Service & Assets_6494298bca84920b10dddd3b",
      "nodeName": "SERV_Sprint 23.22_Service & Assets",
      "sprintStartDate": "2023-10-25T04:30:50.391Z",
      "sprintEndDate": "2023-11-08T03:30:00.000Z",
      "path": [
        "Service & Assets_6494298bca84920b10dddd3b###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Service & Assets_6494298bca84920b10dddd3b"
      ],
      "sprintState": "closed",
      "level": 6
    },
    {
      "nodeId": "54737_Do it Best_657049b505ce0569d5612ec7",
      "nodeName": "Do it Best - Sprint V1.6_Do it Best",
      "sprintStartDate": "2023-11-15T13:54:00.000Z",
      "sprintEndDate": "2023-11-28T13:54:00.000Z",
      "path": [
        "Do it Best_657049b505ce0569d5612ec7###3PP CRM_port###ADEO_acc###Automotive_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Do it Best_657049b505ce0569d5612ec7"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Retail_ver",
      "nodeName": "Retail",
      "path": "International_bu",
      "labelName": "ver",
      "parentId": "International_bu",
      "level": 2
    },
    {
      "nodeId": "13557_Ecom Post-Purchase Squad_64be67caeb7015715615c4c5",
      "nodeName": "SBRAPP 3.5_Ecom Post-Purchase Squad",
      "path": [
        "Ecom Post-Purchase Squad_64be67caeb7015715615c4c5###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Ecom Post-Purchase Squad_64be67caeb7015715615c4c5"
      ],
      "level": 6,
      "releaseEndDate": "2024-01-31T00:00:00.000Z",
      "releaseStartDate": "2023-10-25T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2279_Canada ELD_64f1d426d8d45b13a8de56cb",
      "nodeName": "CELD_Sprint 23.19_Canada ELD",
      "sprintStartDate": "2023-08-30T06:28:23.102Z",
      "sprintEndDate": "2023-09-12T15:43:00.000Z",
      "path": [
        "Canada ELD_64f1d426d8d45b13a8de56cb###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Canada ELD_64f1d426d8d45b13a8de56cb"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Sonepar Int Test_6557c0f708f3181484511ee1",
      "nodeName": "Sonepar Int Test",
      "path": "Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Sonepar SAS_port",
      "level": 5,
      "basicProjectConfigId": "6557c0f708f3181484511ee1"
    },
    {
      "nodeId": "Do it Best_657049b505ce0569d5612ec7",
      "nodeName": "Do it Best",
      "path": "3PP CRM_port###ADEO_acc###Automotive_ver###North America_bu",
      "labelName": "project",
      "parentId": "3PP CRM_port",
      "level": 5,
      "basicProjectConfigId": "657049b505ce0569d5612ec7"
    },
    {
      "nodeId": "13512_VDOS Outside Hauler_647702b25286e83998a56138",
      "nodeName": "OH_20230613_VDOS Outside Hauler",
      "path": [
        "VDOS Outside Hauler_647702b25286e83998a56138###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "VDOS Outside Hauler_647702b25286e83998a56138"
      ],
      "level": 6,
      "releaseEndDate": "2023-06-13T00:00:00.000Z",
      "releaseStartDate": "2023-06-13T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "41064_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.15.1531 iOS_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-24T00:00:00.000Z",
      "releaseStartDate": "2023-11-23T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "eCommerce_sqd_6377306a175a953a0a49d322",
      "nodeName": "eCommerce",
      "path": "2320_Integration Services_6377306a175a953a0a49d322###Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2320_Integration Services_6377306a175a953a0a49d322",
      "level": 7
    },
    {
      "nodeId": "3338_Design System_64ad9e667d51263c17602c67",
      "nodeName": "WATTS 14.4_Design System",
      "sprintStartDate": "2023-10-25T22:00:00.000Z",
      "sprintEndDate": "2023-11-07T22:00:00.000Z",
      "path": [
        "Design System_64ad9e667d51263c17602c67###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Design System_64ad9e667d51263c17602c67"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "11723_Sonepar SAF_6542b3da08f31814845119a2",
      "nodeName": "DFS-PI15_Sonepar SAF",
      "path": [
        "Sonepar SAF_6542b3da08f31814845119a2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar SAF_6542b3da08f31814845119a2"
      ],
      "level": 6,
      "releaseEndDate": "2024-03-16T00:00:00.000Z",
      "releaseStartDate": "2023-12-06T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "10131_Test rc_657056c668a1225c0126c843",
      "nodeName": "KnowHOW v8.0.0_Test rc",
      "path": [
        "Test rc_657056c668a1225c0126c843###2021 WLP Brand Retainer_port###AB Tetra Pak_acc###Consumer Products_ver###Europe_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Test rc_657056c668a1225c0126c843"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-31T00:00:00.000Z",
      "releaseStartDate": "2023-08-31T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "2274_Data Engineering_644258b830d86a7f539c7fd7",
      "nodeName": "DEA Sprint 23.21_Data Engineering",
      "sprintStartDate": "2023-10-11T04:03:41.941Z",
      "sprintEndDate": "2023-10-25T03:30:00.000Z",
      "path": [
        "Data Engineering_644258b830d86a7f539c7fd7###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Data Engineering_644258b830d86a7f539c7fd7"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "45283_MAP_63a304a909378702f4eab1d0",
      "nodeName": "MAP|PI_14|ITR_6_MAP",
      "sprintStartDate": "2023-09-06T07:03:00.000Z",
      "sprintEndDate": "2023-09-26T07:03:00.000Z",
      "path": [
        "MAP_63a304a909378702f4eab1d0###DTS_port###Methods and Tools_acc###PS Internal_ver###Internal_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "MAP_63a304a909378702f4eab1d0"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "151220_Do it Best_657049b505ce0569d5612ec7",
      "nodeName": "FEAEM_RV1.5_Do it Best",
      "path": [
        "Do it Best_657049b505ce0569d5612ec7###3PP CRM_port###ADEO_acc###Automotive_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Do it Best_657049b505ce0569d5612ec7"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-14T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Released"
    },
    {
      "nodeId": "2281_VDOS Outside Hauler_647702b25286e83998a56138",
      "nodeName": "OMS_Sprint 23.19_VDOS Outside Hauler",
      "sprintStartDate": "2023-09-13T15:05:51.587Z",
      "sprintEndDate": "2023-09-27T15:05:00.000Z",
      "path": [
        "VDOS Outside Hauler_647702b25286e83998a56138###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "VDOS Outside Hauler_647702b25286e83998a56138"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "38785_RABO Scrum (New)_64a5dac31734471c30844068",
      "nodeName": "dotcom-2023-11-09_RABO Scrum (New)",
      "path": [
        "RABO Scrum (New)_64a5dac31734471c30844068###AA_port###ADEO_acc###Financial Services_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RABO Scrum (New)_64a5dac31734471c30844068"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-09T00:00:00.000Z",
      "releaseStartDate": "2023-10-26T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "10599_Sonepar SAF_6542b3da08f31814845119a2",
      "nodeName": "v1.189 FE RC_Sonepar SAF",
      "path": [
        "Sonepar SAF_6542b3da08f31814845119a2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar SAF_6542b3da08f31814845119a2"
      ],
      "level": 6,
      "releaseEndDate": "2022-09-05T00:00:00.000Z",
      "releaseStartDate": "2022-08-29T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "10175_PSknowHOW _6527af981704342160f43748",
      "nodeName": "KnowHOW PI-16_PSknowHOW ",
      "path": [
        "PSknowHOW _6527af981704342160f43748###DTS_port###Methods and Tools_acc###PS Internal_ver###Internal_bu"
      ],
      "labelName": "release",
      "parentId": [
        "PSknowHOW _6527af981704342160f43748"
      ],
      "level": 6,
      "releaseEndDate": "2024-03-26T00:00:00.000Z",
      "releaseStartDate": "2023-12-27T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "3216_Unified Commerce - Dan's MVP_64ab97327d51263c17602b58",
      "nodeName": "UC Sprint 13_Unified Commerce - Dan's MVP",
      "sprintStartDate": "2023-10-18T05:03:34.235Z",
      "sprintEndDate": "2023-11-01T04:00:00.000Z",
      "path": [
        "Unified Commerce - Dan's MVP_64ab97327d51263c17602b58###Endeavour Group Pty Ltd_port###Endeavour Group Limited_acc###Retail_ver###International_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Unified Commerce - Dan's MVP_64ab97327d51263c17602b58"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "15660_DRP - HomePage POD_64b3f315c4e72b57c94035e2",
      "nodeName": "NickFury_DRP - HomePage POD",
      "path": [
        "DRP - HomePage POD_64b3f315c4e72b57c94035e2###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "DRP - HomePage POD_64b3f315c4e72b57c94035e2"
      ],
      "level": 6,
      "releaseEndDate": "2022-09-26T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "1135_Promotion Engine BOF_64c178d7eb7015715615c5a6",
      "nodeName": "OMS_Sep01_2023_Rel9_Promotion Engine BOF",
      "sprintStartDate": "2023-08-07T14:58:28.766Z",
      "sprintEndDate": "2023-09-05T17:28:00.000Z",
      "path": [
        "Promotion Engine BOF_64c178d7eb7015715615c5a6###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Promotion Engine BOF_64c178d7eb7015715615c5a6"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "13563_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "nodeName": "SBRWEB CAP_Dotcom + Mobile App",
      "path": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba"
      ],
      "level": 6,
      "releaseEndDate": "2024-02-07T00:00:00.000Z",
      "releaseStartDate": "2023-08-02T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "DHL Logistics Scrumban_6549029708f3181484511bbb",
      "nodeName": "DHL Logistics Scrumban",
      "path": "DPDHL - CSI DCI - Logistics and CJ_port###Deutsche Post AG_acc###Automotive_ver###EU_bu",
      "labelName": "project",
      "parentId": "DPDHL - CSI DCI - Logistics and CJ_port",
      "level": 5,
      "basicProjectConfigId": "6549029708f3181484511bbb"
    },
    {
      "nodeId": "2796_SAME_63a4810c09378702f4eab210",
      "nodeName": "Sprint 102 - Initial Plan_SAME",
      "sprintStartDate": "2023-11-07T15:29:49.542Z",
      "sprintEndDate": "2023-11-21T12:25:00.000Z",
      "path": [
        "SAME_63a4810c09378702f4eab210###ACE20001_port###Abu Dhabi Investment Authority_acc###PS Internal_ver###EU_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "SAME_63a4810c09378702f4eab210"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2350_ASO Mobile App_64a4fab01734471c30843fda",
      "nodeName": "MyAccount_Oct27_2023.Rel11_ASO Mobile App",
      "sprintStartDate": "2023-10-04T05:00:04.184Z",
      "sprintEndDate": "2023-10-28T04:30:00.000Z",
      "path": [
        "ASO Mobile App_64a4fab01734471c30843fda###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "ASO Mobile App_64a4fab01734471c30843fda"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "nodeName": "Dotcom + Mobile App",
      "path": "Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Sunbelt Rentals_port",
      "level": 5,
      "basicProjectConfigId": "64be65cceb7015715615c4ba"
    },
    {
      "nodeId": "Team 2_sqd_63a02b61bbc09e116d744d9d",
      "nodeName": "Team 2",
      "path": "2318_GearBox_63a02b61bbc09e116d744d9d###GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2318_GearBox_63a02b61bbc09e116d744d9d",
      "level": 7
    },
    {
      "nodeId": "2281_VDOS Translations_651fe6bb1704342160f43511",
      "nodeName": "OMS_Sprint 23.19_VDOS Translations",
      "sprintStartDate": "2023-09-13T15:05:51.587Z",
      "sprintEndDate": "2023-09-27T15:05:00.000Z",
      "path": [
        "VDOS Translations_651fe6bb1704342160f43511###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "VDOS Translations_651fe6bb1704342160f43511"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2328_ASO Mobile App_64a4fab01734471c30843fda",
      "nodeName": "MobileApp_24_Nov_2023_Rel12_ASO Mobile App",
      "sprintStartDate": "2023-10-31T02:06:31.617Z",
      "sprintEndDate": "2023-11-25T02:10:00.000Z",
      "path": [
        "ASO Mobile App_64a4fab01734471c30843fda###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "ASO Mobile App_64a4fab01734471c30843fda"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Team 1_sqd_6494298bca84920b10dddd3b",
      "nodeName": "Team 1",
      "path": "2237_Service & Assets_6494298bca84920b10dddd3b###Service & Assets_6494298bca84920b10dddd3b###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2237_Service & Assets_6494298bca84920b10dddd3b",
      "level": 7
    },
    {
      "nodeId": "157_PSknowHOW _6527af981704342160f43748",
      "nodeName": "PS HOW |PI_15|ITR_4|08_Nov_PSknowHOW ",
      "sprintStartDate": "2023-11-08T17:03:48.030Z",
      "sprintEndDate": "2023-11-22T16:00:00.000Z",
      "path": [
        "PSknowHOW _6527af981704342160f43748###DTS_port###Methods and Tools_acc###PS Internal_ver###Internal_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "PSknowHOW _6527af981704342160f43748"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "2326_GearBox Squad 3_64770ef45286e83998a56143",
      "nodeName": "GB_Sprint 23.23_GearBox Squad 3",
      "sprintStartDate": "2023-11-08T11:28:16.595Z",
      "sprintEndDate": "2023-11-22T03:30:00.000Z",
      "path": [
        "GearBox Squad 3_64770ef45286e83998a56143###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "GearBox Squad 3_64770ef45286e83998a56143"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "11807_Sonepar BUY_6542b42308f31814845119a8",
      "nodeName": "v1.402 FE_Sonepar BUY",
      "path": [
        "Sonepar BUY_6542b42308f31814845119a8###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar BUY_6542b42308f31814845119a8"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-08T00:00:00.000Z",
      "releaseStartDate": "2023-11-02T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "39878_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.12.1495 iOS_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-06-17T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "VDOS Outside Hauler_647702b25286e83998a56138",
      "nodeName": "VDOS Outside Hauler",
      "path": "Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Sunbelt Rentals_port",
      "level": 5,
      "basicProjectConfigId": "647702b25286e83998a56138"
    },
    {
      "nodeId": "2290_PIM_64a4e09e1734471c30843fc2",
      "nodeName": "PIM_Sep_23_Sprint_41_PIM",
      "sprintStartDate": "2023-08-07T17:25:14.025Z",
      "sprintEndDate": "2023-09-04T16:30:00.000Z",
      "path": [
        "PIM_64a4e09e1734471c30843fc2###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "PIM_64a4e09e1734471c30843fc2"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2277_PIM_64a4e09e1734471c30843fc2",
      "nodeName": "PIM_Aug_23_Sprint_40_PIM",
      "sprintStartDate": "2023-07-10T16:56:40.712Z",
      "sprintEndDate": "2023-08-07T16:30:00.000Z",
      "path": [
        "PIM_64a4e09e1734471c30843fc2###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "PIM_64a4e09e1734471c30843fc2"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "10030_Test rc_657056c668a1225c0126c843",
      "nodeName": "KnowHOW PI-13_Test rc",
      "path": [
        "Test rc_657056c668a1225c0126c843###2021 WLP Brand Retainer_port###AB Tetra Pak_acc###Consumer Products_ver###Europe_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Test rc_657056c668a1225c0126c843"
      ],
      "level": 6,
      "releaseEndDate": "2023-06-27T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "1161_SEO_64a4e0591734471c30843fc0",
      "nodeName": "SEO_Jul7_2023_Rel7_SEO",
      "sprintStartDate": "2023-06-12T20:12:16.494Z",
      "sprintEndDate": "2023-07-09T20:08:00.000Z",
      "path": [
        "SEO_64a4e0591734471c30843fc0###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "SEO_64a4e0591734471c30843fc0"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "ADEO_acc",
      "nodeName": "ADEO",
      "path": "Automotive_ver###North America_bu",
      "labelName": "acc",
      "parentId": "Automotive_ver",
      "level": 3
    },
    {
      "nodeId": "38678_RABO Scrum (New)_64a5dac31734471c30844068",
      "nodeName": "dotcom-2023-09-28_RABO Scrum (New)",
      "path": [
        "RABO Scrum (New)_64a5dac31734471c30844068###AA_port###ADEO_acc###Financial Services_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RABO Scrum (New)_64a5dac31734471c30844068"
      ],
      "level": 6,
      "releaseEndDate": "2023-09-28T00:00:00.000Z",
      "releaseStartDate": "2023-09-14T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "VDOS/TMS_sqd_6377306a175a953a0a49d322",
      "nodeName": "VDOS/TMS",
      "path": "2377_Integration Services_6377306a175a953a0a49d322###Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2377_Integration Services_6377306a175a953a0a49d322",
      "level": 7
    },
    {
      "nodeId": "2257_Cart & checkout_6441078b72a7c53c78f70590",
      "nodeName": "Search_Sep01_2023.Rel9_Cart & checkout",
      "sprintStartDate": "2023-08-07T21:41:19.769Z",
      "sprintEndDate": "2023-09-03T22:30:00.000Z",
      "path": [
        "Cart & checkout_6441078b72a7c53c78f70590###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Cart & checkout_6441078b72a7c53c78f70590"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "11848_Sonepar AFS_654135db88c4b8114af77dba",
      "nodeName": "1.366.1 BE Hotfix AFS_Sonepar AFS",
      "path": [
        "Sonepar AFS_654135db88c4b8114af77dba###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar AFS_654135db88c4b8114af77dba"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-28T00:00:00.000Z",
      "releaseStartDate": "2023-11-28T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2326_GearBox_63a02b61bbc09e116d744d9d",
      "nodeName": "GB_Sprint 23.23_GearBox",
      "sprintStartDate": "2023-11-08T11:28:16.595Z",
      "sprintEndDate": "2023-11-22T03:30:00.000Z",
      "path": [
        "GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "GearBox_63a02b61bbc09e116d744d9d"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2401_My Account_6441081a72a7c53c78f70595",
      "nodeName": "MyAccount_Dec22_2023.Rel13_My Account",
      "sprintStartDate": "2023-11-27T06:00:12.547Z",
      "sprintEndDate": "2023-12-22T06:00:00.000Z",
      "path": [
        "My Account_6441081a72a7c53c78f70595###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "My Account_6441081a72a7c53c78f70595"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "Sonepar Mobile App_6448a96c3be37902a3f1ba48",
      "nodeName": "Sonepar Mobile App",
      "path": "Sonepar Client Cost - MC_port###Sonepar SAS_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Sonepar Client Cost - MC_port",
      "level": 5,
      "basicProjectConfigId": "6448a96c3be37902a3f1ba48"
    },
    {
      "nodeId": "2268_Cart & checkout_6441078b72a7c53c78f70590",
      "nodeName": "Web_Payment_Sep01_2023_Rel9_Cart & checkout",
      "sprintStartDate": "2023-08-07T13:30:59.611Z",
      "sprintEndDate": "2023-09-01T13:34:00.000Z",
      "path": [
        "Cart & checkout_6441078b72a7c53c78f70590###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Cart & checkout_6441078b72a7c53c78f70590"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "155_PSknowHOW _6527af981704342160f43748",
      "nodeName": "PS HOW |PI_15|ITR_2|11_Oct_PSknowHOW ",
      "sprintStartDate": "2023-10-11T15:19:19.877Z",
      "sprintEndDate": "2023-10-25T16:00:00.000Z",
      "path": [
        "PSknowHOW _6527af981704342160f43748###DTS_port###Methods and Tools_acc###PS Internal_ver###Internal_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "PSknowHOW _6527af981704342160f43748"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "Endeavour Group Pty Ltd_port",
      "nodeName": "Endeavour Group Pty Ltd",
      "path": "Endeavour Group Limited_acc###Retail_ver###International_bu",
      "labelName": "port",
      "parentId": "Endeavour Group Limited_acc",
      "level": 4
    },
    {
      "nodeId": "54246_DHL Logistics Scrumban_6549029708f3181484511bbb",
      "nodeName": "FT2 Sprint 82 3.10.23-17.10.23_DHL Logistics Scrumban",
      "sprintStartDate": "2023-10-04T09:06:00.000Z",
      "sprintEndDate": "2023-10-18T09:06:00.000Z",
      "path": [
        "DHL Logistics Scrumban_6549029708f3181484511bbb###DPDHL - CSI DCI - Logistics and CJ_port###Deutsche Post AG_acc###Automotive_ver###EU_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "DHL Logistics Scrumban_6549029708f3181484511bbb"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "15596_DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "nodeName": "Juggernaut_DRP - Discovery POD",
      "path": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1"
      ],
      "level": 6,
      "releaseEndDate": "2022-07-13T00:00:00.000Z",
      "releaseStartDate": "2022-06-23T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "1037_Website BOF_644108c072a7c53c78f7059a",
      "nodeName": "Promo_Engine_BOF_Sep_1_Website BOF",
      "sprintStartDate": "2023-08-07T21:58:09.148Z",
      "sprintEndDate": "2023-09-01T19:29:00.000Z",
      "path": [
        "Website BOF_644108c072a7c53c78f7059a###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Website BOF_644108c072a7c53c78f7059a"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "2282_My Account_6441081a72a7c53c78f70595",
      "nodeName": "Fulfillment_Sept01_2023_Rel9_My Account",
      "sprintStartDate": "2023-08-07T22:22:56.353Z",
      "sprintEndDate": "2024-09-04T14:44:00.000Z",
      "path": [
        "My Account_6441081a72a7c53c78f70595###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "My Account_6441081a72a7c53c78f70595"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "13553_Ecom Purchase Squad_64be677aeb7015715615c4c1",
      "nodeName": "SBRWEB 1.6_Ecom Purchase Squad",
      "path": [
        "Ecom Purchase Squad_64be677aeb7015715615c4c1###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Ecom Purchase Squad_64be677aeb7015715615c4c1"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-29T00:00:00.000Z",
      "releaseStartDate": "2023-08-16T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "3PP CRM_port",
      "nodeName": "3PP CRM",
      "path": "ADEO_acc###Automotive_ver###North America_bu",
      "labelName": "port",
      "parentId": "ADEO_acc",
      "level": 4
    },
    {
      "nodeId": "Legacy CC_sqd_64be65cceb7015715615c4ba",
      "nodeName": "Legacy CC",
      "path": "2266_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2266_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "level": 7
    },
    {
      "nodeId": "Sonepar SRE_6542b86f08f31814845119be",
      "nodeName": "Sonepar SRE",
      "path": "Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Sonepar SAS_port",
      "level": 5,
      "basicProjectConfigId": "6542b86f08f31814845119be"
    },
    {
      "nodeId": "1179_ASO Mobile App_64a4fab01734471c30843fda",
      "nodeName": "MobileApp_27_Oct_2023_Rel11_ASO Mobile App",
      "sprintStartDate": "2023-10-03T19:39:11.646Z",
      "sprintEndDate": "2023-10-27T19:43:00.000Z",
      "path": [
        "ASO Mobile App_64a4fab01734471c30843fda###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "ASO Mobile App_64a4fab01734471c30843fda"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "39891_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.12.2158 Android_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-07-03T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "testkkk_65684655ae2c8767903e75c2",
      "nodeName": "testkkk",
      "path": "3PP - Cross Regional_port###AB Tetra Pak_acc###Consumer Products_ver###A_bu",
      "labelName": "project",
      "parentId": "3PP - Cross Regional_port",
      "level": 5,
      "basicProjectConfigId": "65684655ae2c8767903e75c2"
    },
    {
      "nodeId": "Salesforce_sqd_6377306a175a953a0a49d322",
      "nodeName": "Salesforce",
      "path": "2356_Integration Services_6377306a175a953a0a49d322###Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2356_Integration Services_6377306a175a953a0a49d322",
      "level": 7
    },
    {
      "nodeId": "13600_Ecom Purchase Squad_64be677aeb7015715615c4c1",
      "nodeName": "SBRWEB 1.6.1_Ecom Purchase Squad",
      "path": [
        "Ecom Purchase Squad_64be677aeb7015715615c4c1###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Ecom Purchase Squad_64be677aeb7015715615c4c1"
      ],
      "level": 6,
      "releaseEndDate": "2023-12-13T00:00:00.000Z",
      "releaseStartDate": "2023-09-27T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "3267_Sonepar BUY_6542b42308f31814845119a8",
      "nodeName": "B&D - 14.4_Sonepar BUY",
      "sprintStartDate": "2023-10-26T08:46:56.325Z",
      "sprintEndDate": "2023-11-08T09:31:44.000Z",
      "path": [
        "Sonepar BUY_6542b42308f31814845119a8###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar BUY_6542b42308f31814845119a8"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2371_Data Engineering_644258b830d86a7f539c7fd7",
      "nodeName": "DEA Sprint 23.23_Data Engineering",
      "sprintStartDate": "2023-11-08T05:09:48.245Z",
      "sprintEndDate": "2023-11-22T04:30:00.000Z",
      "path": [
        "Data Engineering_644258b830d86a7f539c7fd7###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Data Engineering_644258b830d86a7f539c7fd7"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2319_Website TOF_6441052372a7c53c78f70588",
      "nodeName": "WebsiteTOF_Nov_24_2023_Rel12_Website TOF",
      "sprintStartDate": "2023-10-30T21:42:24.648Z",
      "sprintEndDate": "2023-11-26T13:49:00.000Z",
      "path": [
        "Website TOF_6441052372a7c53c78f70588###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Website TOF_6441052372a7c53c78f70588"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "1116_Mobile App_637b17b9175a953a0a49d3c2",
      "nodeName": "MA_Sprint 23.14_Mobile App",
      "sprintStartDate": "2023-07-05T16:15:17.740Z",
      "sprintEndDate": "2023-07-18T22:00:00.000Z",
      "path": [
        "Mobile App_637b17b9175a953a0a49d3c2###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Mobile App_637b17b9175a953a0a49d3c2"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "3195_Sonepar EAC_6542b3b008f31814845119a0",
      "nodeName": "Engage 14.5_Sonepar EAC",
      "sprintStartDate": "2023-11-09T14:39:57.059Z",
      "sprintEndDate": "2023-11-23T15:18:00.000Z",
      "path": [
        "Sonepar EAC_6542b3b008f31814845119a0###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar EAC_6542b3b008f31814845119a0"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "TAP_65696f2b2d6d5f774de2e68a",
      "nodeName": "TAP",
      "path": "2021 WLP Brand Retainer_port###ADNOC Global Trading Limited_acc###Consumer Products_ver###Government Services_bu",
      "labelName": "project",
      "parentId": "2021 WLP Brand Retainer_port",
      "level": 5,
      "basicProjectConfigId": "65696f2b2d6d5f774de2e68a"
    },
    {
      "nodeId": "13600_Ecom Pre-Purchase Squad_64be66e3eb7015715615c4bd",
      "nodeName": "SBRWEB 1.6.1_Ecom Pre-Purchase Squad",
      "path": [
        "Ecom Pre-Purchase Squad_64be66e3eb7015715615c4bd###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Ecom Pre-Purchase Squad_64be66e3eb7015715615c4bd"
      ],
      "level": 6,
      "releaseEndDate": "2023-12-13T00:00:00.000Z",
      "releaseStartDate": "2023-09-27T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "11695_Cart & checkout_6441078b72a7c53c78f70590",
      "nodeName": "Payments - Website Jan 2024 Release_Cart & checkout",
      "path": [
        "Cart & checkout_6441078b72a7c53c78f70590###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Cart & checkout_6441078b72a7c53c78f70590"
      ],
      "level": 6,
      "releaseEndDate": "2024-01-25T00:00:00.000Z",
      "releaseStartDate": "2023-06-01T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2285_Canada ELD_64f1d426d8d45b13a8de56cb",
      "nodeName": "CELD_Sprint 23.20_Canada ELD",
      "sprintStartDate": "2023-09-13T05:20:03.244Z",
      "sprintEndDate": "2023-09-26T16:22:00.000Z",
      "path": [
        "Canada ELD_64f1d426d8d45b13a8de56cb###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Canada ELD_64f1d426d8d45b13a8de56cb"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "10146_PSknowHOW _6527af981704342160f43748",
      "nodeName": "KnowHOW v9.0.0_PSknowHOW ",
      "path": [
        "PSknowHOW _6527af981704342160f43748###DTS_port###Methods and Tools_acc###PS Internal_ver###Internal_bu"
      ],
      "labelName": "release",
      "parentId": [
        "PSknowHOW _6527af981704342160f43748"
      ],
      "level": 6,
      "releaseEndDate": "2024-01-11T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "45281_KN Server_656969922d6d5f774de2e686",
      "nodeName": "MAP|PI_14|ITR_4_KN Server",
      "sprintStartDate": "2023-08-09T07:03:00.000Z",
      "sprintEndDate": "2023-08-23T07:03:00.000Z",
      "path": [
        "KN Server_656969922d6d5f774de2e686###2021 WLP Brand Retainer_port###ADNOC Global Trading Limited_acc###Consumer Products_ver###Government Services_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "KN Server_656969922d6d5f774de2e686"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "3344_Sonepar Cloud_6542b82208f31814845119bb",
      "nodeName": "Cloud - S 14.6 / 78_Sonepar Cloud",
      "sprintStartDate": "2023-11-23T10:08:10.005Z",
      "sprintEndDate": "2023-12-07T09:45:20.000Z",
      "path": [
        "Sonepar Cloud_6542b82208f31814845119bb###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar Cloud_6542b82208f31814845119bb"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "11656_Sonepar INT_6542b4bb08f31814845119b2",
      "nodeName": "[USA-SPRFLD] Ready for UAT_Sonepar INT",
      "path": [
        "Sonepar INT_6542b4bb08f31814845119b2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar INT_6542b4bb08f31814845119b2"
      ],
      "level": 6,
      "releaseEndDate": "2023-08-16T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2288_Website TOF_6441052372a7c53c78f70588",
      "nodeName": "WebsiteTOF_Sep 29_2023_Rel10_Website TOF",
      "sprintStartDate": "2023-09-04T14:41:58.069Z",
      "sprintEndDate": "2023-10-02T14:41:00.000Z",
      "path": [
        "Website TOF_6441052372a7c53c78f70588###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Website TOF_6441052372a7c53c78f70588"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2205_Onsite search_644131b98b61fa2477214bf3",
      "nodeName": "MyAccount_Jul07_2023.Rel7_Onsite search",
      "sprintStartDate": "2023-06-12T13:07:15.151Z",
      "sprintEndDate": "2023-07-07T09:11:00.000Z",
      "path": [
        "Onsite search_644131b98b61fa2477214bf3###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Onsite search_644131b98b61fa2477214bf3"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "2210_Promotion Engine TOF_644105f972a7c53c78f7058c",
      "nodeName": "Promo_Engine_BOF_Aug_4_Promotion Engine TOF",
      "sprintStartDate": "2023-07-10T21:26:00.000Z",
      "sprintEndDate": "2023-08-04T21:26:00.000Z",
      "path": [
        "Promotion Engine TOF_644105f972a7c53c78f7058c###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Promotion Engine TOF_644105f972a7c53c78f7058c"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "3339_Design System_64ad9e667d51263c17602c67",
      "nodeName": "WATTS 14.5_Design System",
      "sprintStartDate": "2023-11-08T16:52:29.784Z",
      "sprintEndDate": "2023-11-21T22:00:00.000Z",
      "path": [
        "Design System_64ad9e667d51263c17602c67###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Design System_64ad9e667d51263c17602c67"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Team 3_sqd_63a02b61bbc09e116d744d9d",
      "nodeName": "Team 3",
      "path": "2326_GearBox_63a02b61bbc09e116d744d9d###GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2326_GearBox_63a02b61bbc09e116d744d9d",
      "level": 7
    },
    {
      "nodeId": "2265_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "nodeName": "ECOM_Sprint 23.21_Dotcom + Mobile App",
      "sprintStartDate": "2023-10-11T16:40:42.000Z",
      "sprintEndDate": "2023-10-24T20:00:00.000Z",
      "path": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "151910_Do it Best_657049b505ce0569d5612ec7",
      "nodeName": "FEAEM_HOTFIX_RV1.5_02_Do it Best",
      "path": [
        "Do it Best_657049b505ce0569d5612ec7###3PP CRM_port###ADEO_acc###Automotive_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Do it Best_657049b505ce0569d5612ec7"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-28T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Released"
    },
    {
      "nodeId": "11723_Sonepar Mobile App_6448a96c3be37902a3f1ba48",
      "nodeName": "DFS-PI15_Sonepar Mobile App",
      "path": [
        "Sonepar Mobile App_6448a96c3be37902a3f1ba48###Sonepar Client Cost - MC_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar Mobile App_6448a96c3be37902a3f1ba48"
      ],
      "level": 6,
      "releaseEndDate": "2024-03-16T00:00:00.000Z",
      "releaseStartDate": "2023-12-06T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "40491_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.14.2203 Android_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-09-09T00:00:00.000Z",
      "releaseStartDate": "2023-08-25T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "AA_port",
      "nodeName": "AA",
      "path": "ADEO_acc###Financial Services_ver###EU_bu",
      "labelName": "port",
      "parentId": "ADEO_acc",
      "level": 4
    },
    {
      "nodeId": "2240_Data Engineering_644258b830d86a7f539c7fd7",
      "nodeName": "DA Sprint 23.16_Data Engineering",
      "sprintStartDate": "2023-08-02T04:58:13.492Z",
      "sprintEndDate": "2023-08-15T03:30:00.000Z",
      "path": [
        "Data Engineering_644258b830d86a7f539c7fd7###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Data Engineering_644258b830d86a7f539c7fd7"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "3266_Sonepar BUY_6542b42308f31814845119a8",
      "nodeName": "B&D - 14.3_Sonepar BUY",
      "sprintStartDate": "2023-10-12T09:06:24.233Z",
      "sprintEndDate": "2023-10-25T09:51:00.000Z",
      "path": [
        "Sonepar BUY_6542b42308f31814845119a8###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar BUY_6542b42308f31814845119a8"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "PS Internal_ver",
      "nodeName": "PS Internal",
      "path": "Internal_bu",
      "labelName": "ver",
      "parentId": "Internal_bu",
      "level": 2
    },
    {
      "nodeId": "2318_GearBox_63a02b61bbc09e116d744d9d",
      "nodeName": "GB_Sprint 23.22_GearBox",
      "sprintStartDate": "2023-10-25T11:33:17.758Z",
      "sprintEndDate": "2023-11-08T03:30:00.000Z",
      "path": [
        "GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "GearBox_63a02b61bbc09e116d744d9d"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Design System_64ad9e667d51263c17602c67",
      "nodeName": "Design System",
      "path": "Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Sonepar SAS_port",
      "level": 5,
      "basicProjectConfigId": "64ad9e667d51263c17602c67"
    },
    {
      "nodeId": "13556_Ecom Purchase Squad_64be677aeb7015715615c4c1",
      "nodeName": "SBRAPP 3.4_Ecom Purchase Squad",
      "path": [
        "Ecom Purchase Squad_64be677aeb7015715615c4c1###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Ecom Purchase Squad_64be677aeb7015715615c4c1"
      ],
      "level": 6,
      "releaseEndDate": "2023-12-05T00:00:00.000Z",
      "releaseStartDate": "2023-06-20T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "1007_DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "nodeName": "DRP Sprint 83_DRP - Discovery POD",
      "sprintStartDate": "2023-09-21T17:14:44.383Z",
      "sprintEndDate": "2023-10-11T16:37:00.000Z",
      "path": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Team 3_sqd_64770ef45286e83998a56143",
      "nodeName": "Team 3",
      "path": "2229_GearBox Squad 3_64770ef45286e83998a56143###GearBox Squad 3_64770ef45286e83998a56143###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2229_GearBox Squad 3_64770ef45286e83998a56143",
      "level": 7
    },
    {
      "nodeId": "2350_My Account_6441081a72a7c53c78f70595",
      "nodeName": "MyAccount_Oct27_2023.Rel11_My Account",
      "sprintStartDate": "2023-10-04T05:00:04.184Z",
      "sprintEndDate": "2023-10-28T04:30:00.000Z",
      "path": [
        "My Account_6441081a72a7c53c78f70595###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "My Account_6441081a72a7c53c78f70595"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "3196_Sonepar EAC_6542b3b008f31814845119a0",
      "nodeName": "Engage 14.6_Sonepar EAC",
      "sprintStartDate": "2023-11-23T09:34:09.774Z",
      "sprintEndDate": "2023-12-07T15:19:00.000Z",
      "path": [
        "Sonepar EAC_6542b3b008f31814845119a0###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar EAC_6542b3b008f31814845119a0"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "45282_KN Server_656969922d6d5f774de2e686",
      "nodeName": "MAP|PI_14|ITR_5_KN Server",
      "sprintStartDate": "2023-08-23T07:03:00.000Z",
      "sprintEndDate": "2023-09-05T18:03:00.000Z",
      "path": [
        "KN Server_656969922d6d5f774de2e686###2021 WLP Brand Retainer_port###ADNOC Global Trading Limited_acc###Consumer Products_ver###Government Services_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "KN Server_656969922d6d5f774de2e686"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2372_Data Engineering_644258b830d86a7f539c7fd7",
      "nodeName": "DEA Sprint 23.24_Data Engineering",
      "sprintStartDate": "2023-11-22T05:04:59.972Z",
      "sprintEndDate": "2023-12-06T04:25:42.000Z",
      "path": [
        "Data Engineering_644258b830d86a7f539c7fd7###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Data Engineering_644258b830d86a7f539c7fd7"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "13556_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "nodeName": "SBRAPP 3.4_Dotcom + Mobile App",
      "path": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba"
      ],
      "level": 6,
      "releaseEndDate": "2023-12-05T00:00:00.000Z",
      "releaseStartDate": "2023-06-20T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "11644_Sonepar INT_6542b4bb08f31814845119b2",
      "nodeName": "[USA-SPRFLD] Spark PROD Available_Sonepar INT",
      "path": [
        "Sonepar INT_6542b4bb08f31814845119b2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar INT_6542b4bb08f31814845119b2"
      ],
      "level": 6,
      "releaseEndDate": "2023-05-24T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "11825_Sonepar BUY_6542b42308f31814845119a8",
      "nodeName": "v1.406 FE RC_Sonepar BUY",
      "path": [
        "Sonepar BUY_6542b42308f31814845119a8###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar BUY_6542b42308f31814845119a8"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-22T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Released"
    },
    {
      "nodeId": "15292_DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "nodeName": "Westeros_Hotfix_04Nov_DRP - Discovery POD",
      "path": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1"
      ],
      "level": 6,
      "releaseEndDate": "2020-11-04T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "11711_CMS_644103e772a7c53c78f70582",
      "nodeName": "Website Jan 2024 Release_CMS",
      "path": [
        "CMS_644103e772a7c53c78f70582###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "CMS_644103e772a7c53c78f70582"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-27T00:00:00.000Z",
      "releaseStartDate": "2023-10-02T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "11758_Sonepar MAP_6542b43f08f31814845119ab",
      "nodeName": "V1.366 BE AFS_Sonepar MAP",
      "path": [
        "Sonepar MAP_6542b43f08f31814845119ab###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar MAP_6542b43f08f31814845119ab"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-06T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "1008_DRP - HomePage POD_64b3f315c4e72b57c94035e2",
      "nodeName": "DRP Sprint 84_DRP - HomePage POD",
      "sprintStartDate": "2023-10-12T10:17:13.296Z",
      "sprintEndDate": "2023-11-01T09:40:00.000Z",
      "path": [
        "DRP - HomePage POD_64b3f315c4e72b57c94035e2###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "DRP - HomePage POD_64b3f315c4e72b57c94035e2"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "3274_Sonepar Global_6448a8213be37902a3f1ba45",
      "nodeName": "Data Team 14.6_Sonepar Global",
      "sprintStartDate": "2023-11-23T10:08:09.426Z",
      "sprintEndDate": "2023-12-06T18:13:00.000Z",
      "path": [
        "Sonepar Global_6448a8213be37902a3f1ba45###Sonepar Client Cost - MC_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar Global_6448a8213be37902a3f1ba45"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "15311_DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "nodeName": "DAEWOO_DRP - Discovery POD",
      "path": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1"
      ],
      "level": 6,
      "releaseEndDate": "2021-02-24T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Ecom Pre-Purchase Squad_64be66e3eb7015715615c4bd",
      "nodeName": "Ecom Pre-Purchase Squad",
      "path": "Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Sunbelt Rentals_port",
      "level": 5,
      "basicProjectConfigId": "64be66e3eb7015715615c4bd"
    },
    {
      "nodeId": "VDOS_63777558175a953a0a49d363",
      "nodeName": "VDOS",
      "path": "Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Sunbelt Rentals_port",
      "level": 5,
      "basicProjectConfigId": "63777558175a953a0a49d363"
    },
    {
      "nodeId": "SAME_63a4810c09378702f4eab210",
      "nodeName": "SAME",
      "path": "ACE20001_port###Abu Dhabi Investment Authority_acc###PS Internal_ver###EU_bu",
      "labelName": "project",
      "parentId": "ACE20001_port",
      "level": 5,
      "basicProjectConfigId": "63a4810c09378702f4eab210"
    },
    {
      "nodeId": "2266_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "nodeName": "ECOM_Sprint 23.22_Dotcom + Mobile App",
      "sprintStartDate": "2023-10-25T20:59:02.000Z",
      "sprintEndDate": "2023-11-07T21:00:00.000Z",
      "path": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "13589_Ecom Post-Purchase Squad_64be67caeb7015715615c4c5",
      "nodeName": "SBRAPP 3.3.1_Ecom Post-Purchase Squad",
      "path": [
        "Ecom Post-Purchase Squad_64be67caeb7015715615c4c5###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Ecom Post-Purchase Squad_64be67caeb7015715615c4c5"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-05T00:00:00.000Z",
      "releaseStartDate": "2023-09-11T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2244_Website BOF_644108c072a7c53c78f7059a",
      "nodeName": "Cart&Checkout Oct27 2023 Rel11_Website BOF",
      "sprintStartDate": "2023-10-02T22:20:36.136Z",
      "sprintEndDate": "2023-10-29T16:16:00.000Z",
      "path": [
        "Website BOF_644108c072a7c53c78f7059a###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Website BOF_644108c072a7c53c78f7059a"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "13572_Ecom Purchase Squad_64be677aeb7015715615c4c1",
      "nodeName": "SBRWEB 1.7_Ecom Purchase Squad",
      "path": [
        "Ecom Purchase Squad_64be677aeb7015715615c4c1###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Ecom Purchase Squad_64be677aeb7015715615c4c1"
      ],
      "level": 6,
      "releaseEndDate": "2024-02-07T00:00:00.000Z",
      "releaseStartDate": "2023-08-16T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Nissan Core Team - PS_port",
      "nodeName": "Nissan Core Team - PS",
      "path": "Nissan Motor Co. Ltd._acc###Automotive_ver###International_bu",
      "labelName": "port",
      "parentId": "Nissan Motor Co. Ltd._acc",
      "level": 4
    },
    {
      "nodeId": "Commerce_sqd_64be65cceb7015715615c4ba",
      "nodeName": "Commerce",
      "path": "2218_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2218_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "level": 7
    },
    {
      "nodeId": "1096_SEO_64a4e0591734471c30843fc0",
      "nodeName": "Search_Aug4_2023.Rel8_SEO",
      "sprintStartDate": "2023-07-10T21:57:54.526Z",
      "sprintEndDate": "2023-08-06T22:46:00.000Z",
      "path": [
        "SEO_64a4e0591734471c30843fc0###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "SEO_64a4e0591734471c30843fc0"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "HomepageEcom_sqd_64b3f315c4e72b57c94035e2",
      "nodeName": "HomepageEcom",
      "path": "1017_DRP - HomePage POD_64b3f315c4e72b57c94035e2###DRP - HomePage POD_64b3f315c4e72b57c94035e2###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "1017_DRP - HomePage POD_64b3f315c4e72b57c94035e2",
      "level": 7
    },
    {
      "nodeId": "2249_Data Visualization_64425a0a30d86a7f539c7fdc",
      "nodeName": "DV Sprint 23.19_Data Visualization",
      "sprintStartDate": "2023-09-13T04:18:58.741Z",
      "sprintEndDate": "2023-09-27T03:30:00.000Z",
      "path": [
        "Data Visualization_64425a0a30d86a7f539c7fdc###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Data Visualization_64425a0a30d86a7f539c7fdc"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "eCommerce_sqd_6377306a175a953a0a49d322",
      "nodeName": "eCommerce",
      "path": "2377_Integration Services_6377306a175a953a0a49d322###Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2377_Integration Services_6377306a175a953a0a49d322",
      "level": 7
    },
    {
      "nodeId": "11784_Design System_64ad9e667d51263c17602c67",
      "nodeName": "v1.390.1 FE_Design System",
      "path": [
        "Design System_64ad9e667d51263c17602c67###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Design System_64ad9e667d51263c17602c67"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-23T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Released"
    },
    {
      "nodeId": "13560_P2P_64ca0b8f5fec906dbc18f3c5",
      "nodeName": "P2P R1.0_P2P",
      "path": [
        "P2P_64ca0b8f5fec906dbc18f3c5###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "P2P_64ca0b8f5fec906dbc18f3c5"
      ],
      "level": 6,
      "releaseEndDate": "2024-04-30T00:00:00.000Z",
      "releaseStartDate": "2023-06-21T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2212_R1+ Frontline_647588bc5286e83998a5609c",
      "nodeName": "FRONT_Sprint 23.21_R1+ Frontline",
      "sprintStartDate": "2023-10-11T15:47:00.220Z",
      "sprintEndDate": "2023-10-25T18:19:55.000Z",
      "path": [
        "R1+ Frontline_647588bc5286e83998a5609c###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "R1+ Frontline_647588bc5286e83998a5609c"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2343_Promotion Engine BOF_64c178d7eb7015715615c5a6",
      "nodeName": "Promo_Engine_BOF_Oct_27_Promotion Engine BOF",
      "sprintStartDate": "2023-10-02T22:42:11.125Z",
      "sprintEndDate": "2023-10-27T14:52:00.000Z",
      "path": [
        "Promotion Engine BOF_64c178d7eb7015715615c5a6###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Promotion Engine BOF_64c178d7eb7015715615c5a6"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Content_sqd_64be65cceb7015715615c4ba",
      "nodeName": "Content",
      "path": "2266_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2266_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "level": 7
    },
    {
      "nodeId": "PSknowHOW _6527af981704342160f43748",
      "nodeName": "PSknowHOW ",
      "path": "DTS_port###Methods and Tools_acc###PS Internal_ver###Internal_bu",
      "labelName": "project",
      "parentId": "DTS_port",
      "level": 5,
      "basicProjectConfigId": "6527af981704342160f43748"
    },
    {
      "nodeId": "2218_Mobile App_637b17b9175a953a0a49d3c2",
      "nodeName": "ECOM_Sprint 23.19_Mobile App",
      "sprintStartDate": "2023-09-13T17:19:57.893Z",
      "sprintEndDate": "2023-09-26T20:00:00.000Z",
      "path": [
        "Mobile App_637b17b9175a953a0a49d3c2###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Mobile App_637b17b9175a953a0a49d3c2"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2310_Canada ELD_64f1d426d8d45b13a8de56cb",
      "nodeName": "CELD_Sprint 23.21_Canada ELD",
      "sprintStartDate": "2023-10-11T12:48:26.945Z",
      "sprintEndDate": "2023-10-24T15:34:00.000Z",
      "path": [
        "Canada ELD_64f1d426d8d45b13a8de56cb###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Canada ELD_64f1d426d8d45b13a8de56cb"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "10385_Promotion Engine TOF_644105f972a7c53c78f7058c",
      "nodeName": "Promotions Aug 2023 Release_Promotion Engine TOF",
      "path": [
        "Promotion Engine TOF_644105f972a7c53c78f7058c###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Promotion Engine TOF_644105f972a7c53c78f7058c"
      ],
      "level": 6,
      "releaseEndDate": "2024-01-25T00:00:00.000Z",
      "releaseStartDate": "2023-07-01T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Team 1_sqd_63a02b61bbc09e116d744d9d",
      "nodeName": "Team 1",
      "path": "2369_GearBox_63a02b61bbc09e116d744d9d###GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2369_GearBox_63a02b61bbc09e116d744d9d",
      "level": 7
    },
    {
      "nodeId": "3450_Sonepar Mobile App_6448a96c3be37902a3f1ba48",
      "nodeName": "DFA QA Sprint 14.5_Sonepar Mobile App",
      "sprintStartDate": "2023-11-09T13:00:28.322Z",
      "sprintEndDate": "2023-11-23T11:29:30.000Z",
      "path": [
        "Sonepar Mobile App_6448a96c3be37902a3f1ba48###Sonepar Client Cost - MC_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar Mobile App_6448a96c3be37902a3f1ba48"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "10032_Test rc_657056c668a1225c0126c843",
      "nodeName": "KnowHOW PI-12_Test rc",
      "path": [
        "Test rc_657056c668a1225c0126c843###2021 WLP Brand Retainer_port###AB Tetra Pak_acc###Consumer Products_ver###Europe_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Test rc_657056c668a1225c0126c843"
      ],
      "level": 6,
      "releaseEndDate": "2023-03-28T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "CCSF Project_64e90e10dd4f0b7e8ed0a5fe",
      "nodeName": "CCSF Project",
      "path": "C&C San Francisco - PAS_port###City and County of San Francisco_acc###Financial Services_ver###North America_bu",
      "labelName": "project",
      "parentId": "C&C San Francisco - PAS_port",
      "level": 5,
      "basicProjectConfigId": "64e90e10dd4f0b7e8ed0a5fe"
    },
    {
      "nodeId": "13563_Ecom Purchase Squad_64be677aeb7015715615c4c1",
      "nodeName": "SBRWEB CAP_Ecom Purchase Squad",
      "path": [
        "Ecom Purchase Squad_64be677aeb7015715615c4c1###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Ecom Purchase Squad_64be677aeb7015715615c4c1"
      ],
      "level": 6,
      "releaseEndDate": "2024-02-07T00:00:00.000Z",
      "releaseStartDate": "2023-08-02T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "11790_Sonepar EAC_6542b3b008f31814845119a0",
      "nodeName": "v1.398 FE_Sonepar EAC",
      "path": [
        "Sonepar EAC_6542b3b008f31814845119a0###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar EAC_6542b3b008f31814845119a0"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-03T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Released"
    },
    {
      "nodeId": "3210_Sonepar MAP_6542b43f08f31814845119ab",
      "nodeName": "M&P - S14.2_Sonepar MAP",
      "sprintStartDate": "2023-09-28T14:01:06.267Z",
      "sprintEndDate": "2023-10-11T12:00:00.000Z",
      "path": [
        "Sonepar MAP_6542b43f08f31814845119ab###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar MAP_6542b43f08f31814845119ab"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2245_Website BOF_644108c072a7c53c78f7059a",
      "nodeName": "Cart&Checkout Nov24 2023 Rel12_Website BOF",
      "sprintStartDate": "2023-10-30T14:11:17.486Z",
      "sprintEndDate": "2023-11-26T16:16:00.000Z",
      "path": [
        "Website BOF_644108c072a7c53c78f7059a###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Website BOF_644108c072a7c53c78f7059a"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "Salesforce_sqd_6377306a175a953a0a49d322",
      "nodeName": "Salesforce",
      "path": "2406_Integration Services_6377306a175a953a0a49d322###Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2406_Integration Services_6377306a175a953a0a49d322",
      "level": 7
    },
    {
      "nodeId": "1086_GearBox_63a02b61bbc09e116d744d9d",
      "nodeName": "GB Sprint 23.11_GearBox",
      "sprintStartDate": "2023-05-10T10:49:36.598Z",
      "sprintEndDate": "2023-05-24T02:00:00.000Z",
      "path": [
        "GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "GearBox_63a02b61bbc09e116d744d9d"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "Data Visualization_64425a0a30d86a7f539c7fdc",
      "nodeName": "Data Visualization",
      "path": "Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Sunbelt Rentals_port",
      "level": 5,
      "basicProjectConfigId": "64425a0a30d86a7f539c7fdc"
    },
    {
      "nodeId": "53987_DHL Logistics Scrumban_6549029708f3181484511bbb",
      "nodeName": "DPDHL Go-live_DHL Logistics Scrumban",
      "sprintStartDate": "2023-09-18T06:46:00.000Z",
      "sprintEndDate": "2023-10-28T06:46:00.000Z",
      "path": [
        "DHL Logistics Scrumban_6549029708f3181484511bbb###DPDHL - CSI DCI - Logistics and CJ_port###Deutsche Post AG_acc###Automotive_ver###EU_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "DHL Logistics Scrumban_6549029708f3181484511bbb"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "67_PSknowHOW _6527af981704342160f43748",
      "nodeName": "MAP|PI_15|ITR_4_PSknowHOW ",
      "sprintStartDate": "2023-11-09T06:14:57.395Z",
      "sprintEndDate": "2023-11-22T18:00:00.000Z",
      "path": [
        "PSknowHOW _6527af981704342160f43748###DTS_port###Methods and Tools_acc###PS Internal_ver###Internal_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "PSknowHOW _6527af981704342160f43748"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "Commerce_sqd_64be65cceb7015715615c4ba",
      "nodeName": "Commerce",
      "path": "2267_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2267_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "level": 7
    },
    {
      "nodeId": "DPDHL - CSI DCI - Logistics and CJ_port",
      "nodeName": "DPDHL - CSI DCI - Logistics and CJ",
      "path": "Deutsche Post AG_acc###Automotive_ver###EU_bu",
      "labelName": "port",
      "parentId": "Deutsche Post AG_acc",
      "level": 4
    },
    {
      "nodeId": "2187_Onsite search_644131b98b61fa2477214bf3",
      "nodeName": "WebsiteTOF_June_9_2023_Rel06_Onsite search",
      "sprintStartDate": "2023-05-15T05:10:51.124Z",
      "sprintEndDate": "2023-06-09T22:26:00.000Z",
      "path": [
        "Onsite search_644131b98b61fa2477214bf3###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Onsite search_644131b98b61fa2477214bf3"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "10385_My Account_6441081a72a7c53c78f70595",
      "nodeName": "Promotions Aug 2023 Release_My Account",
      "path": [
        "My Account_6441081a72a7c53c78f70595###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "My Account_6441081a72a7c53c78f70595"
      ],
      "level": 6,
      "releaseEndDate": "2024-01-25T00:00:00.000Z",
      "releaseStartDate": "2023-07-01T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2308_Website TOF_6441052372a7c53c78f70588",
      "nodeName": "Fanatics Oct Sprint - Rel10_Website TOF",
      "sprintStartDate": "2023-09-04T14:01:14.770Z",
      "sprintEndDate": "2023-10-02T23:22:00.000Z",
      "path": [
        "Website TOF_6441052372a7c53c78f70588###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Website TOF_6441052372a7c53c78f70588"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "11829_Sonepar AFS_654135db88c4b8114af77dba",
      "nodeName": "BE Next RC B&D_Sonepar AFS",
      "path": [
        "Sonepar AFS_654135db88c4b8114af77dba###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar AFS_654135db88c4b8114af77dba"
      ],
      "level": 6,
      "releaseEndDate": "2023-12-29T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "65_PSknowHOW _6527af981704342160f43748",
      "nodeName": "MAP|PI_15|ITR_2_PSknowHOW ",
      "sprintStartDate": "2023-10-12T06:36:03.000Z",
      "sprintEndDate": "2023-10-24T17:30:00.000Z",
      "path": [
        "PSknowHOW _6527af981704342160f43748###DTS_port###Methods and Tools_acc###PS Internal_ver###Internal_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "PSknowHOW _6527af981704342160f43748"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "11819_Sonepar AFS_654135db88c4b8114af77dba",
      "nodeName": "FE Next RC_Sonepar AFS",
      "path": [
        "Sonepar AFS_654135db88c4b8114af77dba###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar AFS_654135db88c4b8114af77dba"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-08T00:00:00.000Z",
      "releaseStartDate": "2023-11-06T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "13601_GearBox Squad 2_64770ec45286e83998a56141",
      "nodeName": "GB GoMobileAndroid 23.01_GearBox Squad 2",
      "path": [
        "GearBox Squad 2_64770ec45286e83998a56141###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "GearBox Squad 2_64770ec45286e83998a56141"
      ],
      "level": 6,
      "releaseEndDate": "2023-12-05T00:00:00.000Z",
      "releaseStartDate": "2023-08-07T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "CMS_644103e772a7c53c78f70582",
      "nodeName": "CMS",
      "path": "ASO_port###Academy Sports_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "ASO_port",
      "level": 5,
      "basicProjectConfigId": "644103e772a7c53c78f70582"
    },
    {
      "nodeId": "11712_My Account_6441081a72a7c53c78f70595",
      "nodeName": "OMS Jan 2024 Release_My Account",
      "path": [
        "My Account_6441081a72a7c53c78f70595###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "My Account_6441081a72a7c53c78f70595"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-27T00:00:00.000Z",
      "releaseStartDate": "2023-10-02T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "1096_Website TOF_6441052372a7c53c78f70588",
      "nodeName": "Search_Aug4_2023.Rel8_Website TOF",
      "sprintStartDate": "2023-07-10T21:57:54.526Z",
      "sprintEndDate": "2023-08-06T22:46:00.000Z",
      "path": [
        "Website TOF_6441052372a7c53c78f70588###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Website TOF_6441052372a7c53c78f70588"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "1125_Website BOF_644108c072a7c53c78f7059a",
      "nodeName": "Web_Payment_May12_2023_Rel5_Website BOF",
      "sprintStartDate": "2023-04-17T18:28:22.234Z",
      "sprintEndDate": "2023-05-15T06:15:00.000Z",
      "path": [
        "Website BOF_644108c072a7c53c78f7059a###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Website BOF_644108c072a7c53c78f7059a"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "10131_PSknowHOW _6527af981704342160f43748",
      "nodeName": "KnowHOW v8.0.0_PSknowHOW ",
      "path": [
        "PSknowHOW _6527af981704342160f43748###DTS_port###Methods and Tools_acc###PS Internal_ver###Internal_bu"
      ],
      "labelName": "release",
      "parentId": [
        "PSknowHOW _6527af981704342160f43748"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-31T00:00:00.000Z",
      "releaseStartDate": "2023-08-31T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "c7a4b243-6453-4134-acc4-6e56c0c0ead0_Azure Project_656d830b9f546b19742cb55b",
      "nodeName": "Iteration 6_Azure Project",
      "sprintStartDate": "2023-11-07T00:00:00.000Z",
      "sprintEndDate": "2023-11-09T00:00:00.000Z",
      "path": [
        "Azure Project_656d830b9f546b19742cb55b###3PP CRM_port###ADEO_acc###B_ver###Europe_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Azure Project_656d830b9f546b19742cb55b"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "11790_Sonepar SAF_6542b3da08f31814845119a2",
      "nodeName": "v1.398 FE_Sonepar SAF",
      "path": [
        "Sonepar SAF_6542b3da08f31814845119a2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar SAF_6542b3da08f31814845119a2"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-03T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Released"
    },
    {
      "nodeId": "3217_Unified Commerce - Dan's MVP_64ab97327d51263c17602b58",
      "nodeName": "UC Sprint 14_Unified Commerce - Dan's MVP",
      "sprintStartDate": "2023-11-01T03:43:34.679Z",
      "sprintEndDate": "2023-11-15T03:30:00.000Z",
      "path": [
        "Unified Commerce - Dan's MVP_64ab97327d51263c17602b58###Endeavour Group Pty Ltd_port###Endeavour Group Limited_acc###Retail_ver###International_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Unified Commerce - Dan's MVP_64ab97327d51263c17602b58"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "142810_DHL Logistics Scrumban_6549029708f3181484511bbb",
      "nodeName": "Logistics 2023.10.0_DHL Logistics Scrumban",
      "path": [
        "DHL Logistics Scrumban_6549029708f3181484511bbb###DPDHL - CSI DCI - Logistics and CJ_port###Deutsche Post AG_acc###Automotive_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "DHL Logistics Scrumban_6549029708f3181484511bbb"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-25T00:00:00.000Z",
      "releaseStartDate": "2023-09-18T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "2238_R1+ Logistics_648ab6186803f300a9fd2e0e",
      "nodeName": "SERV_Sprint 23.21_R1+ Logistics",
      "sprintStartDate": "2023-10-11T17:43:28.866Z",
      "sprintEndDate": "2023-10-24T04:00:00.000Z",
      "path": [
        "R1+ Logistics_648ab6186803f300a9fd2e0e###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "R1+ Logistics_648ab6186803f300a9fd2e0e"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "Command Center_sqd_64be65cceb7015715615c4ba",
      "nodeName": "Command Center",
      "path": [
        "2266_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2267_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2265_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2268_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sqd",
      "parentId": [
        "2266_Dotcom + Mobile App_64be65cceb7015715615c4ba",
        "2267_Dotcom + Mobile App_64be65cceb7015715615c4ba",
        "2265_Dotcom + Mobile App_64be65cceb7015715615c4ba",
        "2268_Dotcom + Mobile App_64be65cceb7015715615c4ba"
      ],
      "level": 7
    },
    {
      "nodeId": "11800_Sonepar AFS_654135db88c4b8114af77dba",
      "nodeName": "v1.399 BE AFS_Sonepar AFS",
      "path": [
        "Sonepar AFS_654135db88c4b8114af77dba###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar AFS_654135db88c4b8114af77dba"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-02T00:00:00.000Z",
      "releaseStartDate": "2023-10-31T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "PIM_64a4e09e1734471c30843fc2",
      "nodeName": "PIM",
      "path": "ASO_port###Academy Sports_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "ASO_port",
      "level": 5,
      "basicProjectConfigId": "64a4e09e1734471c30843fc2"
    },
    {
      "nodeId": "287_Test rc_657056c668a1225c0126c843",
      "nodeName": "KnowHOW | PI_15| ITR_5_Test rc",
      "sprintStartDate": "2023-11-22T07:22:02.359Z",
      "sprintEndDate": "2023-12-05T08:28:00.000Z",
      "path": [
        "Test rc_657056c668a1225c0126c843###2021 WLP Brand Retainer_port###AB Tetra Pak_acc###Consumer Products_ver###Europe_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Test rc_657056c668a1225c0126c843"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2316_Website BOF_644108c072a7c53c78f7059a",
      "nodeName": "Web_Payment_Oct27_2023_Rel11_Website BOF",
      "sprintStartDate": "2023-10-03T12:55:14.923Z",
      "sprintEndDate": "2023-10-27T16:59:00.000Z",
      "path": [
        "Website BOF_644108c072a7c53c78f7059a###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Website BOF_644108c072a7c53c78f7059a"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "13528_VDOS_63777558175a953a0a49d363",
      "nodeName": "LOGISTICS_PILOT 3_VDOS",
      "path": [
        "VDOS_63777558175a953a0a49d363###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "VDOS_63777558175a953a0a49d363"
      ],
      "level": 6,
      "releaseEndDate": "2024-03-08T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "3472_Sonepar BUY_6542b42308f31814845119a8",
      "nodeName": "B&D - 14.6_Sonepar BUY",
      "sprintStartDate": "2023-11-23T11:49:18.289Z",
      "sprintEndDate": "2023-12-06T12:33:10.000Z",
      "path": [
        "Sonepar BUY_6542b42308f31814845119a8###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar BUY_6542b42308f31814845119a8"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "11830_Sonepar EAC_6542b3b008f31814845119a0",
      "nodeName": "v1.408 FE_Sonepar EAC",
      "path": [
        "Sonepar EAC_6542b3b008f31814845119a0###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar EAC_6542b3b008f31814845119a0"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-24T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Released"
    },
    {
      "nodeId": "11830_Sonepar BUY_6542b42308f31814845119a8",
      "nodeName": "v1.408 FE_Sonepar BUY",
      "path": [
        "Sonepar BUY_6542b42308f31814845119a8###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar BUY_6542b42308f31814845119a8"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-24T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Released"
    },
    {
      "nodeId": "2369_GearBox Squad 3_64770ef45286e83998a56143",
      "nodeName": "GB_Sprint 23.24_GearBox Squad 3",
      "sprintStartDate": "2023-11-22T07:00:44.987Z",
      "sprintEndDate": "2023-12-06T03:30:00.000Z",
      "path": [
        "GearBox Squad 3_64770ef45286e83998a56143###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "GearBox Squad 3_64770ef45286e83998a56143"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "Command Center_sqd_64be65cceb7015715615c4ba",
      "nodeName": "Command Center",
      "path": "2267_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2267_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "level": 7
    },
    {
      "nodeId": "Consumer Products_ver",
      "nodeName": "Consumer Products",
      "path": "A_bu",
      "labelName": "ver",
      "parentId": "A_bu",
      "level": 2
    },
    {
      "nodeId": "Mobile App_637b17b9175a953a0a49d3c2",
      "nodeName": "Mobile App",
      "path": "Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Sunbelt Rentals_port",
      "level": 5,
      "basicProjectConfigId": "637b17b9175a953a0a49d3c2"
    },
    {
      "nodeId": "11841_Sonepar SAF_6542b3da08f31814845119a2",
      "nodeName": "V1.410 BE S&P HOTFIX Product MS BE_Sonepar SAF",
      "path": [
        "Sonepar SAF_6542b3da08f31814845119a2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar SAF_6542b3da08f31814845119a2"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-24T00:00:00.000Z",
      "releaseStartDate": "2023-11-23T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "13565_GearBox Squad 1_6449103b3be37902a3f1ba70",
      "nodeName": "GB Dashboard 23.04_GearBox Squad 1",
      "path": [
        "GearBox Squad 1_6449103b3be37902a3f1ba70###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "GearBox Squad 1_6449103b3be37902a3f1ba70"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-29T00:00:00.000Z",
      "releaseStartDate": "2023-08-07T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "11723_Sonepar AFS_654135db88c4b8114af77dba",
      "nodeName": "DFS-PI15_Sonepar AFS",
      "path": [
        "Sonepar AFS_654135db88c4b8114af77dba###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar AFS_654135db88c4b8114af77dba"
      ],
      "level": 6,
      "releaseEndDate": "2024-03-16T00:00:00.000Z",
      "releaseStartDate": "2023-12-06T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "54408_DHL Logistics Scrumban_6549029708f3181484511bbb",
      "nodeName": "FT1 Sprint 83_DHL Logistics Scrumban",
      "sprintStartDate": "2023-10-17T06:15:00.000Z",
      "sprintEndDate": "2023-10-30T06:15:00.000Z",
      "path": [
        "DHL Logistics Scrumban_6549029708f3181484511bbb###DPDHL - CSI DCI - Logistics and CJ_port###Deutsche Post AG_acc###Automotive_ver###EU_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "DHL Logistics Scrumban_6549029708f3181484511bbb"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2369_GearBox Squad 1_6449103b3be37902a3f1ba70",
      "nodeName": "GB_Sprint 23.24_GearBox Squad 1",
      "sprintStartDate": "2023-11-22T07:00:44.987Z",
      "sprintEndDate": "2023-12-06T03:30:00.000Z",
      "path": [
        "GearBox Squad 1_6449103b3be37902a3f1ba70###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "GearBox Squad 1_6449103b3be37902a3f1ba70"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "2361_R1+ Sales_648ab46f6803f300a9fd2e09",
      "nodeName": "SALES_Sprint 23.21_R1+ Sales",
      "sprintStartDate": "2023-10-12T12:59:44.559Z",
      "sprintEndDate": "2023-10-26T00:03:37.000Z",
      "path": [
        "R1+ Sales_648ab46f6803f300a9fd2e09###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "R1+ Sales_648ab46f6803f300a9fd2e09"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Financial Services_ver",
      "nodeName": "Financial Services",
      "path": "North America_bu",
      "labelName": "ver",
      "parentId": "North America_bu",
      "level": 2
    },
    {
      "nodeId": "13604_Ecom Post-Purchase Squad_64be67caeb7015715615c4c5",
      "nodeName": "SBRWEB 2.0_Ecom Post-Purchase Squad",
      "path": [
        "Ecom Post-Purchase Squad_64be67caeb7015715615c4c5###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Ecom Post-Purchase Squad_64be67caeb7015715615c4c5"
      ],
      "level": 6,
      "releaseEndDate": "2024-02-15T00:00:00.000Z",
      "releaseStartDate": "2023-08-02T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Team 2_sqd_63a02b61bbc09e116d744d9d",
      "nodeName": "Team 2",
      "path": "2370_GearBox_63a02b61bbc09e116d744d9d###GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2370_GearBox_63a02b61bbc09e116d744d9d",
      "level": 7
    },
    {
      "nodeId": "3185_Sonepar Mobile App_6448a96c3be37902a3f1ba48",
      "nodeName": "Mobile - S14.6_Sonepar Mobile App",
      "sprintStartDate": "2023-11-23T14:30:33.328Z",
      "sprintEndDate": "2023-12-07T12:59:22.000Z",
      "path": [
        "Sonepar Mobile App_6448a96c3be37902a3f1ba48###Sonepar Client Cost - MC_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar Mobile App_6448a96c3be37902a3f1ba48"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "Team 3_sqd_64770ef45286e83998a56143",
      "nodeName": "Team 3",
      "path": "2326_GearBox Squad 3_64770ef45286e83998a56143###GearBox Squad 3_64770ef45286e83998a56143###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2326_GearBox Squad 3_64770ef45286e83998a56143",
      "level": 7
    },
    {
      "nodeId": "11818_Sonepar AFS_654135db88c4b8114af77dba",
      "nodeName": "v1.401 BE AX Bridge_Sonepar AFS",
      "path": [
        "Sonepar AFS_654135db88c4b8114af77dba###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar AFS_654135db88c4b8114af77dba"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-09T00:00:00.000Z",
      "releaseStartDate": "2023-11-08T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "13604_Ecom Purchase Squad_64be677aeb7015715615c4c1",
      "nodeName": "SBRWEB 2.0_Ecom Purchase Squad",
      "path": [
        "Ecom Purchase Squad_64be677aeb7015715615c4c1###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Ecom Purchase Squad_64be677aeb7015715615c4c1"
      ],
      "level": 6,
      "releaseEndDate": "2024-02-15T00:00:00.000Z",
      "releaseStartDate": "2023-08-02T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "AB Tetra Pak_acc",
      "nodeName": "AB Tetra Pak",
      "path": "B_ver###Government Services_bu",
      "labelName": "acc",
      "parentId": "B_ver",
      "level": 3
    },
    {
      "nodeId": "Government Services_bu",
      "nodeName": "Government Services",
      "path": "",
      "labelName": "bu",
      "level": 1
    },
    {
      "nodeId": "38781_RABO Scrum (New)_64a5dac31734471c30844068",
      "nodeName": "sitecore-4.56.0_RABO Scrum (New)",
      "path": [
        "RABO Scrum (New)_64a5dac31734471c30844068###AA_port###ADEO_acc###Financial Services_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RABO Scrum (New)_64a5dac31734471c30844068"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-23T00:00:00.000Z",
      "releaseStartDate": "2023-11-09T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "11799_Sonepar SAF_6542b3da08f31814845119a2",
      "nodeName": "v1.400 FE_Sonepar SAF",
      "path": [
        "Sonepar SAF_6542b3da08f31814845119a2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar SAF_6542b3da08f31814845119a2"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-06T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Released"
    },
    {
      "nodeId": "Team 2_sqd_648ab6186803f300a9fd2e0e",
      "nodeName": "Team 2",
      "path": "2202_R1+ Logistics_648ab6186803f300a9fd2e0e###R1+ Logistics_648ab6186803f300a9fd2e0e###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2202_R1+ Logistics_648ab6186803f300a9fd2e0e",
      "level": 7
    },
    {
      "nodeId": "39729_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.12.2143 Android_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-05-31T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Team 2_sqd_64770ec45286e83998a56141",
      "nodeName": "Team 2",
      "path": "2318_GearBox Squad 2_64770ec45286e83998a56141###GearBox Squad 2_64770ec45286e83998a56141###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2318_GearBox Squad 2_64770ec45286e83998a56141",
      "level": 7
    },
    {
      "nodeId": "2393_VDOS_63777558175a953a0a49d363",
      "nodeName": "OMS_Sprint 23.23_VDOS",
      "sprintStartDate": "2023-11-09T13:38:16.954Z",
      "sprintEndDate": "2023-11-21T05:00:00.000Z",
      "path": [
        "VDOS_63777558175a953a0a49d363###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "VDOS_63777558175a953a0a49d363"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Onsite search_644131b98b61fa2477214bf3",
      "nodeName": "Onsite search",
      "path": "ASO_port###Academy Sports_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "ASO_port",
      "level": 5,
      "basicProjectConfigId": "644131b98b61fa2477214bf3"
    },
    {
      "nodeId": "2294_My Account_6441081a72a7c53c78f70595",
      "nodeName": "MyAccount_Sep29_2023.Rel10_My Account",
      "sprintStartDate": "2023-09-04T11:12:20.179Z",
      "sprintEndDate": "2023-09-29T16:17:00.000Z",
      "path": [
        "My Account_6441081a72a7c53c78f70595###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "My Account_6441081a72a7c53c78f70595"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "11790_Sonepar BUY_6542b42308f31814845119a8",
      "nodeName": "v1.398 FE_Sonepar BUY",
      "path": [
        "Sonepar BUY_6542b42308f31814845119a8###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar BUY_6542b42308f31814845119a8"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-03T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Released"
    },
    {
      "nodeId": "Command Center_sqd_64be65cceb7015715615c4ba",
      "nodeName": "Command Center",
      "path": "2265_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2265_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "level": 7
    },
    {
      "nodeId": "2313_ASO Mobile App_64a4fab01734471c30843fda",
      "nodeName": "WebsiteTOF_Oct_27_2023_Rel11_ASO Mobile App",
      "sprintStartDate": "2023-10-02T22:38:28.528Z",
      "sprintEndDate": "2023-10-29T14:45:00.000Z",
      "path": [
        "ASO Mobile App_64a4fab01734471c30843fda###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "ASO Mobile App_64a4fab01734471c30843fda"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "4191_RABO Scrum (New)_64a5dac31734471c30844068",
      "nodeName": "Sentry FY24 Sprint 17_RABO Scrum (New)",
      "sprintStartDate": "2023-09-15T09:24:56.180Z",
      "sprintEndDate": "2023-09-27T22:59:00.000Z",
      "path": [
        "RABO Scrum (New)_64a5dac31734471c30844068###AA_port###ADEO_acc###Financial Services_ver###EU_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "RABO Scrum (New)_64a5dac31734471c30844068"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2676_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "Sprint 117_RMMO",
      "sprintStartDate": "2023-06-27T10:43:57.669Z",
      "sprintEndDate": "2023-07-10T12:03:00.000Z",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "40217_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.14.1511 iOS_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-08-10T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "151526_Do it Best_657049b505ce0569d5612ec7",
      "nodeName": "MAG-HOTFIX-1.4-P4_Do it Best",
      "path": [
        "Do it Best_657049b505ce0569d5612ec7###3PP CRM_port###ADEO_acc###Automotive_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Do it Best_657049b505ce0569d5612ec7"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-08T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Released"
    },
    {
      "nodeId": "2326_GearBox Squad 2_64770ec45286e83998a56141",
      "nodeName": "GB_Sprint 23.23_GearBox Squad 2",
      "sprintStartDate": "2023-11-08T11:28:16.595Z",
      "sprintEndDate": "2023-11-22T03:30:00.000Z",
      "path": [
        "GearBox Squad 2_64770ec45286e83998a56141###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "GearBox Squad 2_64770ec45286e83998a56141"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "151416_Do it Best_657049b505ce0569d5612ec7",
      "nodeName": "MAG-HOTFIX-1.5-P1_Do it Best",
      "path": [
        "Do it Best_657049b505ce0569d5612ec7###3PP CRM_port###ADEO_acc###Automotive_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Do it Best_657049b505ce0569d5612ec7"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-16T00:00:00.000Z",
      "releaseStartDate": "2023-11-15T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "The Childrens Place, Inc._acc",
      "nodeName": "The Childrens Place, Inc.",
      "path": "Retail_ver###North America_bu",
      "labelName": "acc",
      "parentId": "Retail_ver",
      "level": 3
    },
    {
      "nodeId": "Ecom Purchase Squad_64be677aeb7015715615c4c1",
      "nodeName": "Ecom Purchase Squad",
      "path": "Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Sunbelt Rentals_port",
      "level": 5,
      "basicProjectConfigId": "64be677aeb7015715615c4c1"
    },
    {
      "nodeId": "1162_SEO_64a4e0591734471c30843fc0",
      "nodeName": "SEO_Aug4_2023_Rel8_SEO",
      "sprintStartDate": "2023-07-10T21:40:08.240Z",
      "sprintEndDate": "2023-08-06T21:36:00.000Z",
      "path": [
        "SEO_64a4e0591734471c30843fc0###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "SEO_64a4e0591734471c30843fc0"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "53772_pod16 2_657219fd78247c3cd726d630",
      "nodeName": "POD 16 | Sprint 23_17_pod16 2",
      "sprintStartDate": "2023-11-09T05:00:00.000Z",
      "sprintEndDate": "2023-11-22T13:00:00.000Z",
      "path": [
        "pod16 2_657219fd78247c3cd726d630###2021 WLP Brand Retainer_port###AAA Auto Club Group_acc###Automative1_ver###A_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "pod16 2_657219fd78247c3cd726d630"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "10435_Mobile App_637b17b9175a953a0a49d3c2",
      "nodeName": "Release 3.5.0_Mobile App",
      "path": [
        "Mobile App_637b17b9175a953a0a49d3c2###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Mobile App_637b17b9175a953a0a49d3c2"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-17T00:00:00.000Z",
      "releaseStartDate": "2023-08-02T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2292_P2P_64ca0b8f5fec906dbc18f3c5",
      "nodeName": "P2P_Sprint 23.22_P2P",
      "sprintStartDate": "2023-10-25T16:21:21.081Z",
      "sprintEndDate": "2023-11-08T01:00:00.000Z",
      "path": [
        "P2P_64ca0b8f5fec906dbc18f3c5###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "P2P_64ca0b8f5fec906dbc18f3c5"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2308_CMS_644103e772a7c53c78f70582",
      "nodeName": "Fanatics Oct Sprint - Rel10_CMS",
      "sprintStartDate": "2023-09-04T14:01:14.770Z",
      "sprintEndDate": "2023-10-02T23:22:00.000Z",
      "path": [
        "CMS_644103e772a7c53c78f70582###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "CMS_644103e772a7c53c78f70582"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "1016_DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "nodeName": "DRP Sprint 86_DRP - Discovery POD",
      "sprintStartDate": "2023-11-24T14:39:59.215Z",
      "sprintEndDate": "2023-12-14T04:00:00.000Z",
      "path": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "15636_DRP - HomePage POD_64b3f315c4e72b57c94035e2",
      "nodeName": "Magneto_DRP - HomePage POD",
      "path": [
        "DRP - HomePage POD_64b3f315c4e72b57c94035e2###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "DRP - HomePage POD_64b3f315c4e72b57c94035e2"
      ],
      "level": 6,
      "releaseEndDate": "2022-09-05T00:00:00.000Z",
      "releaseStartDate": "2022-08-18T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2206_Website BOF_644108c072a7c53c78f7059a",
      "nodeName": "Cart&Checkout July07 2023 Rel7_Website BOF",
      "sprintStartDate": "2023-06-12T14:10:22.861Z",
      "sprintEndDate": "2023-07-09T14:43:00.000Z",
      "path": [
        "Website BOF_644108c072a7c53c78f7059a###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Website BOF_644108c072a7c53c78f7059a"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "39894_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.12.2159 Android_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-07-04T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "3200_Sonepar AFS_654135db88c4b8114af77dba",
      "nodeName": "AFS - S14.4_Sonepar AFS",
      "sprintStartDate": "2023-10-26T19:43:55.545Z",
      "sprintEndDate": "2023-11-08T21:30:00.000Z",
      "path": [
        "Sonepar AFS_654135db88c4b8114af77dba###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar AFS_654135db88c4b8114af77dba"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "40615_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.15.1520 iOS_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-11T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2329_ASO Mobile App_64a4fab01734471c30843fda",
      "nodeName": "MobileApp_22_Dec_2023_Rel13_ASO Mobile App",
      "sprintStartDate": "2023-11-28T05:30:26.115Z",
      "sprintEndDate": "2023-12-23T05:34:42.000Z",
      "path": [
        "ASO Mobile App_64a4fab01734471c30843fda###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "ASO Mobile App_64a4fab01734471c30843fda"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "3204_Sonepar eProcurement_6542947f08f3181484511988",
      "nodeName": "Eproc 14.2_Sonepar eProcurement",
      "sprintStartDate": "2023-09-28T16:17:35.923Z",
      "sprintEndDate": "2023-10-11T17:01:00.000Z",
      "path": [
        "Sonepar eProcurement_6542947f08f3181484511988###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar eProcurement_6542947f08f3181484511988"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "10257_Integration Services_6377306a175a953a0a49d322",
      "nodeName": "3.0_Rel 1 (IS)_Integration Services",
      "path": [
        "Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Integration Services_6377306a175a953a0a49d322"
      ],
      "level": 6,
      "releaseEndDate": "2022-05-31T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "11807_Sonepar AFS_654135db88c4b8114af77dba",
      "nodeName": "v1.402 FE_Sonepar AFS",
      "path": [
        "Sonepar AFS_654135db88c4b8114af77dba###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar AFS_654135db88c4b8114af77dba"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-08T00:00:00.000Z",
      "releaseStartDate": "2023-11-02T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "Cart & checkout_6441078b72a7c53c78f70590",
      "nodeName": "Cart & checkout",
      "path": "API_port###Academy Sports_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "API_port",
      "level": 5,
      "basicProjectConfigId": "6441078b72a7c53c78f70590"
    },
    {
      "nodeId": "R1F_65253d241704342160f4364a",
      "nodeName": "R1F",
      "path": "3PP - Cross Regional_port###ADNOC Global Trading Limited_acc###Consumer Products_ver###Government Services_bu",
      "labelName": "project",
      "parentId": "3PP - Cross Regional_port",
      "level": 5,
      "basicProjectConfigId": "65253d241704342160f4364a"
    },
    {
      "nodeId": "3192_Sonepar EAC_6542b3b008f31814845119a0",
      "nodeName": "Engage 14.2_Sonepar EAC",
      "sprintStartDate": "2023-09-28T08:28:06.583Z",
      "sprintEndDate": "2023-10-12T14:17:00.000Z",
      "path": [
        "Sonepar EAC_6542b3b008f31814845119a0###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar EAC_6542b3b008f31814845119a0"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2297_Promotion Engine BOF_64c178d7eb7015715615c5a6",
      "nodeName": "Promo_Engine_TOF_Sep_1_Promotion Engine BOF",
      "sprintStartDate": "2023-08-07T21:54:42.268Z",
      "sprintEndDate": "2023-09-01T17:01:00.000Z",
      "path": [
        "Promotion Engine BOF_64c178d7eb7015715615c5a6###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Promotion Engine BOF_64c178d7eb7015715615c5a6"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "Sonepar Client Cost - MC_port",
      "nodeName": "Sonepar Client Cost - MC",
      "path": "Sonepar SAS_acc###Retail_ver###North America_bu",
      "labelName": "port",
      "parentId": "Sonepar SAS_acc",
      "level": 4
    },
    {
      "nodeId": "Website TOF_6441052372a7c53c78f70588",
      "nodeName": "Website TOF",
      "path": "ASO_port###Academy Sports_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "ASO_port",
      "level": 5,
      "basicProjectConfigId": "6441052372a7c53c78f70588"
    },
    {
      "nodeId": "40849_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.15.1522 iOS_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-16T00:00:00.000Z",
      "releaseStartDate": "2023-10-09T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "11648_Sonepar INT_6542b4bb08f31814845119b2",
      "nodeName": "[USA-SPRFLD] Search Ready_Sonepar INT",
      "path": [
        "Sonepar INT_6542b4bb08f31814845119b2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar INT_6542b4bb08f31814845119b2"
      ],
      "level": 6,
      "releaseEndDate": "2023-06-21T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "15588_DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "nodeName": "Ironman_DRP - Discovery POD",
      "path": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1"
      ],
      "level": 6,
      "releaseEndDate": "2022-06-29T00:00:00.000Z",
      "releaseStartDate": "2022-06-09T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2176_Cart & checkout_6441078b72a7c53c78f70590",
      "nodeName": "SRE_Analysis_2023_10AprTo 21st_Cart & checkout",
      "sprintStartDate": "2023-04-11T14:14:27.985Z",
      "sprintEndDate": "2023-04-21T14:14:00.000Z",
      "path": [
        "Cart & checkout_6441078b72a7c53c78f70590###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Cart & checkout_6441078b72a7c53c78f70590"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "37978_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.12.X Android_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-05-31T00:00:00.000Z",
      "releaseStartDate": "2023-03-01T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "ADNOC Global Trading Limited_acc",
      "nodeName": "ADNOC Global Trading Limited",
      "path": "Consumer Products_ver###Internal_bu",
      "labelName": "acc",
      "parentId": "Consumer Products_ver",
      "level": 3
    },
    {
      "nodeId": "Team 1_sqd_6494298bca84920b10dddd3b",
      "nodeName": "Team 1",
      "path": "2305_Service & Assets_6494298bca84920b10dddd3b###Service & Assets_6494298bca84920b10dddd3b###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2305_Service & Assets_6494298bca84920b10dddd3b",
      "level": 7
    },
    {
      "nodeId": "Post-Purchase Team_sqd_64be65cceb7015715615c4ba",
      "nodeName": "Post-Purchase Team",
      "path": "2309_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2309_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "level": 7
    },
    {
      "nodeId": "15455_DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "nodeName": "RACEAIR_DRP - Discovery POD",
      "path": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1"
      ],
      "level": 6,
      "releaseEndDate": "2021-09-08T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Team 1_sqd_6494298bca84920b10dddd3b",
      "nodeName": "Team 1",
      "path": "2303_Service & Assets_6494298bca84920b10dddd3b###Service & Assets_6494298bca84920b10dddd3b###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2303_Service & Assets_6494298bca84920b10dddd3b",
      "level": 7
    },
    {
      "nodeId": "15669_DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "nodeName": "Oracle_DRP - Discovery POD",
      "path": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1"
      ],
      "level": 6,
      "releaseEndDate": "2022-10-17T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Browse & Search_sqd_63dc01e47228be4c30553ce1",
      "nodeName": "Browse & Search",
      "path": "990_DRP - Discovery POD_63dc01e47228be4c30553ce1###DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "990_DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "level": 7
    },
    {
      "nodeId": "15848_DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "nodeName": "R23.11.1_DRP - Discovery POD",
      "path": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-08T00:00:00.000Z",
      "releaseStartDate": "2023-11-02T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2187_Cart & checkout_6441078b72a7c53c78f70590",
      "nodeName": "WebsiteTOF_June_9_2023_Rel4_Cart & checkout",
      "sprintStartDate": "2023-05-15T05:10:51.124Z",
      "sprintEndDate": "2023-06-09T22:26:00.000Z",
      "path": [
        "Cart & checkout_6441078b72a7c53c78f70590###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Cart & checkout_6441078b72a7c53c78f70590"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "3341_Sonepar Cloud_6542b82208f31814845119bb",
      "nodeName": "Cloud - S 14.3 / 75_Sonepar Cloud",
      "sprintStartDate": "2023-10-12T09:30:37.719Z",
      "sprintEndDate": "2023-10-26T09:12:25.000Z",
      "path": [
        "Sonepar Cloud_6542b82208f31814845119bb###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar Cloud_6542b82208f31814845119bb"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2214_ASO Mobile App_64a4fab01734471c30843fda",
      "nodeName": "MyAccount_Aug04_2023.Rel8_ASO Mobile App",
      "sprintStartDate": "2023-07-10T17:31:25.024Z",
      "sprintEndDate": "2023-08-05T04:23:00.000Z",
      "path": [
        "ASO Mobile App_64a4fab01734471c30843fda###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "ASO Mobile App_64a4fab01734471c30843fda"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "151525_Do it Best_657049b505ce0569d5612ec7",
      "nodeName": "MAG-HOTFIX-1.4-P3_Do it Best",
      "path": [
        "Do it Best_657049b505ce0569d5612ec7###3PP CRM_port###ADEO_acc###Automotive_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Do it Best_657049b505ce0569d5612ec7"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-07T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Released"
    },
    {
      "nodeId": "2257_My Account_6441081a72a7c53c78f70595",
      "nodeName": "Search_Sep01_2023.Rel9_My Account",
      "sprintStartDate": "2023-08-07T21:41:19.769Z",
      "sprintEndDate": "2023-09-03T22:30:00.000Z",
      "path": [
        "My Account_6441081a72a7c53c78f70595###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "My Account_6441081a72a7c53c78f70595"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "Legacy CC_sqd_64be65cceb7015715615c4ba",
      "nodeName": "Legacy CC",
      "path": "2218_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2218_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "level": 7
    },
    {
      "nodeId": "3206_Sonepar eProcurement_6542947f08f3181484511988",
      "nodeName": "Eproc 14.4_Sonepar eProcurement",
      "sprintStartDate": "2023-10-26T13:22:36.904Z",
      "sprintEndDate": "2023-11-08T14:06:00.000Z",
      "path": [
        "Sonepar eProcurement_6542947f08f3181484511988###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar eProcurement_6542947f08f3181484511988"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Team 2_sqd_648ab46f6803f300a9fd2e09",
      "nodeName": "Team 2",
      "path": "2361_R1+ Sales_648ab46f6803f300a9fd2e09###R1+ Sales_648ab46f6803f300a9fd2e09###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2361_R1+ Sales_648ab46f6803f300a9fd2e09",
      "level": 7
    },
    {
      "nodeId": "3267_Sonepar SAF_6542b3da08f31814845119a2",
      "nodeName": "B&D - 14.4_Sonepar SAF",
      "sprintStartDate": "2023-10-26T08:46:56.325Z",
      "sprintEndDate": "2023-11-08T09:31:44.000Z",
      "path": [
        "Sonepar SAF_6542b3da08f31814845119a2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar SAF_6542b3da08f31814845119a2"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "990_DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "nodeName": "DRP Sprint 81_DRP - Discovery POD",
      "sprintStartDate": "2023-08-11T11:05:47.715Z",
      "sprintEndDate": "2023-08-31T14:53:00.000Z",
      "path": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "3265_Sonepar BUY_6542b42308f31814845119a8",
      "nodeName": "B & D - 14.2_Sonepar BUY",
      "sprintStartDate": "2023-09-28T21:14:00.000Z",
      "sprintEndDate": "2023-10-10T22:00:00.000Z",
      "path": [
        "Sonepar BUY_6542b42308f31814845119a8###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar BUY_6542b42308f31814845119a8"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Team 1_sqd_6449103b3be37902a3f1ba70",
      "nodeName": "Team 1",
      "path": "2369_GearBox Squad 1_6449103b3be37902a3f1ba70###GearBox Squad 1_6449103b3be37902a3f1ba70###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2369_GearBox Squad 1_6449103b3be37902a3f1ba70",
      "level": 7
    },
    {
      "nodeId": "10444_VDOS Outside Hauler_647702b25286e83998a56138",
      "nodeName": "OH_FS1_VDOS Outside Hauler",
      "path": [
        "VDOS Outside Hauler_647702b25286e83998a56138###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "VDOS Outside Hauler_647702b25286e83998a56138"
      ],
      "level": 6,
      "releaseEndDate": "2023-05-26T00:00:00.000Z",
      "releaseStartDate": "2023-03-01T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "45280_KN Server_656969922d6d5f774de2e686",
      "nodeName": "MAP|PI_14|ITR_3_KN Server",
      "sprintStartDate": "2023-07-26T07:03:00.000Z",
      "sprintEndDate": "2023-08-08T18:03:00.000Z",
      "path": [
        "KN Server_656969922d6d5f774de2e686###2021 WLP Brand Retainer_port###ADNOC Global Trading Limited_acc###Consumer Products_ver###Government Services_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "KN Server_656969922d6d5f774de2e686"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "13523_Mobile App_637b17b9175a953a0a49d3c2",
      "nodeName": "Release 3.2.3 (hotfix)_Mobile App",
      "path": [
        "Mobile App_637b17b9175a953a0a49d3c2###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Mobile App_637b17b9175a953a0a49d3c2"
      ],
      "level": 6,
      "releaseEndDate": "2023-06-29T00:00:00.000Z",
      "releaseStartDate": "2023-06-23T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2248_Data Engineering_644258b830d86a7f539c7fd7",
      "nodeName": "DV Sprint 23.18_Data Engineering",
      "sprintStartDate": "2023-08-30T07:50:39.832Z",
      "sprintEndDate": "2023-09-13T03:30:00.000Z",
      "path": [
        "Data Engineering_644258b830d86a7f539c7fd7###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Data Engineering_644258b830d86a7f539c7fd7"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "10331_Mobile App_637b17b9175a953a0a49d3c2",
      "nodeName": "Release 3.2.0_Mobile App",
      "path": [
        "Mobile App_637b17b9175a953a0a49d3c2###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Mobile App_637b17b9175a953a0a49d3c2"
      ],
      "level": 6,
      "releaseEndDate": "2023-05-18T00:00:00.000Z",
      "releaseStartDate": "2023-02-15T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "HomepageEcom_sqd_64b3f315c4e72b57c94035e2",
      "nodeName": "HomepageEcom",
      "path": "1007_DRP - HomePage POD_64b3f315c4e72b57c94035e2###DRP - HomePage POD_64b3f315c4e72b57c94035e2###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "1007_DRP - HomePage POD_64b3f315c4e72b57c94035e2",
      "level": 7
    },
    {
      "nodeId": "Content_sqd_64be65cceb7015715615c4ba",
      "nodeName": "Content",
      "path": "2267_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2267_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "level": 7
    },
    {
      "nodeId": "41005_SAME_63a4810c09378702f4eab210",
      "nodeName": "1.3.37.5_SAME",
      "path": [
        "SAME_63a4810c09378702f4eab210###ACE20001_port###Abu Dhabi Investment Authority_acc###PS Internal_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "SAME_63a4810c09378702f4eab210"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-10T00:00:00.000Z",
      "releaseStartDate": "2023-11-10T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "53773_pod16 2_657219fd78247c3cd726d630",
      "nodeName": "POD 16 | Sprint 23_18_pod16 2",
      "sprintStartDate": "2023-11-23T13:00:00.000Z",
      "sprintEndDate": "2023-12-06T13:00:00.000Z",
      "path": [
        "pod16 2_657219fd78247c3cd726d630###2021 WLP Brand Retainer_port###AAA Auto Club Group_acc###Automative1_ver###A_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "pod16 2_657219fd78247c3cd726d630"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Browse & Search_sqd_63dc01e47228be4c30553ce1",
      "nodeName": "Browse & Search",
      "path": "1017_DRP - Discovery POD_63dc01e47228be4c30553ce1###DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "1017_DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "level": 7
    },
    {
      "nodeId": "2218_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "nodeName": "ECOM_Sprint 23.19_Dotcom + Mobile App",
      "sprintStartDate": "2023-09-13T17:19:57.893Z",
      "sprintEndDate": "2023-09-26T20:00:00.000Z",
      "path": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2309_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "nodeName": "ECOM_Sprint 23.20_Dotcom + Mobile App",
      "sprintStartDate": "2023-09-27T18:05:39.000Z",
      "sprintEndDate": "2023-10-10T20:00:00.000Z",
      "path": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "15572_DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "nodeName": "Firelord_DRP - Discovery POD",
      "path": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1"
      ],
      "level": 6,
      "releaseEndDate": "2022-05-18T00:00:00.000Z",
      "releaseStartDate": "2022-05-18T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "40242_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.13.2186 Android_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-08-24T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Abu Dhabi Investment Authority_acc",
      "nodeName": "Abu Dhabi Investment Authority",
      "path": "PS Internal_ver###EU_bu",
      "labelName": "acc",
      "parentId": "PS Internal_ver",
      "level": 3
    },
    {
      "nodeId": "13563_Ecom Post-Purchase Squad_64be67caeb7015715615c4c5",
      "nodeName": "SBRWEB CAP_Ecom Post-Purchase Squad",
      "path": [
        "Ecom Post-Purchase Squad_64be67caeb7015715615c4c5###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Ecom Post-Purchase Squad_64be67caeb7015715615c4c5"
      ],
      "level": 6,
      "releaseEndDate": "2024-02-07T00:00:00.000Z",
      "releaseStartDate": "2023-08-02T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Sonepar eProcurement_6542947f08f3181484511988",
      "nodeName": "Sonepar eProcurement",
      "path": "Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Sonepar SAS_port",
      "level": 5,
      "basicProjectConfigId": "6542947f08f3181484511988"
    },
    {
      "nodeId": "2221_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "nodeName": "P2P_Sprint 23.18_Dotcom + Mobile App",
      "sprintStartDate": "2023-08-30T18:03:26.540Z",
      "sprintEndDate": "2023-09-13T00:24:00.000Z",
      "path": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "Team 3_sqd_64770ef45286e83998a56143",
      "nodeName": "Team 3",
      "path": "2317_GearBox Squad 3_64770ef45286e83998a56143###GearBox Squad 3_64770ef45286e83998a56143###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2317_GearBox Squad 3_64770ef45286e83998a56143",
      "level": 7
    },
    {
      "nodeId": "Command Center_sqd_64be65cceb7015715615c4ba",
      "nodeName": "Command Center",
      "path": "2268_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2268_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "level": 7
    },
    {
      "nodeId": "Supply Chain_sqd_6377306a175a953a0a49d322",
      "nodeName": "Supply Chain",
      "path": "2384_Integration Services_6377306a175a953a0a49d322###Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2384_Integration Services_6377306a175a953a0a49d322",
      "level": 7
    },
    {
      "nodeId": "39854_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.12.2157 Android_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-06-23T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2021 WLP Brand Retainer_port",
      "nodeName": "2021 WLP Brand Retainer",
      "path": "AAA Auto Club Group_acc###Automative1_ver###A_bu",
      "labelName": "port",
      "parentId": "AAA Auto Club Group_acc",
      "level": 4
    },
    {
      "nodeId": "Consumer Products_ver",
      "nodeName": "Consumer Products",
      "path": "Internal_bu",
      "labelName": "ver",
      "parentId": "Internal_bu",
      "level": 2
    },
    {
      "nodeId": "HomepageEcom_sqd_64b3f315c4e72b57c94035e2",
      "nodeName": "HomepageEcom",
      "path": "1016_DRP - HomePage POD_64b3f315c4e72b57c94035e2###DRP - HomePage POD_64b3f315c4e72b57c94035e2###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "1016_DRP - HomePage POD_64b3f315c4e72b57c94035e2",
      "level": 7
    },
    {
      "nodeId": "Sonepar SAS_acc",
      "nodeName": "Sonepar SAS",
      "path": "Retail_ver###North America_bu",
      "labelName": "acc",
      "parentId": "Retail_ver",
      "level": 3
    },
    {
      "nodeId": "15669_DRP - HomePage POD_64b3f315c4e72b57c94035e2",
      "nodeName": "Oracle_DRP - HomePage POD",
      "path": [
        "DRP - HomePage POD_64b3f315c4e72b57c94035e2###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "DRP - HomePage POD_64b3f315c4e72b57c94035e2"
      ],
      "level": 6,
      "releaseEndDate": "2022-10-17T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "40026_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.13.2176 Android_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-08-04T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "1095_Cart & checkout_6441078b72a7c53c78f70590",
      "nodeName": "Search_Jul7_2023.Relx_Cart & checkout",
      "sprintStartDate": "2023-06-12T20:10:00.419Z",
      "sprintEndDate": "2023-07-09T20:59:00.000Z",
      "path": [
        "Cart & checkout_6441078b72a7c53c78f70590###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Cart & checkout_6441078b72a7c53c78f70590"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "3PP Social - Chrysler_port",
      "nodeName": "3PP Social - Chrysler",
      "path": "AAA Auto Club Group_acc###Consumer Products_ver###EU_bu",
      "labelName": "port",
      "parentId": "AAA Auto Club Group_acc",
      "level": 4
    },
    {
      "nodeId": "10382_ASO Mobile App_64a4fab01734471c30843fda",
      "nodeName": "Mobile-App-Oct-2023_ASO Mobile App",
      "path": [
        "ASO Mobile App_64a4fab01734471c30843fda###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "ASO Mobile App_64a4fab01734471c30843fda"
      ],
      "level": 6,
      "releaseEndDate": "2023-09-29T00:00:00.000Z",
      "releaseStartDate": "2023-09-04T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2232_My Account_6441081a72a7c53c78f70595",
      "nodeName": "OMS_Nov24_2023_Rel12_My Account",
      "sprintStartDate": "2023-10-30T17:26:00.000Z",
      "sprintEndDate": "2023-11-27T15:52:00.000Z",
      "path": [
        "My Account_6441081a72a7c53c78f70595###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "My Account_6441081a72a7c53c78f70595"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "Anglo American Marketing Limited_acc",
      "nodeName": "Anglo American Marketing Limited",
      "path": "Energy & Commodities_ver###EU_bu",
      "labelName": "acc",
      "parentId": "Energy & Commodities_ver",
      "level": 3
    },
    {
      "nodeId": "1154_SEO_64a4e0591734471c30843fc0",
      "nodeName": "SEO_May12_2023_Rel5_SEO",
      "sprintStartDate": "2023-04-17T19:51:16.637Z",
      "sprintEndDate": "2023-05-14T19:47:00.000Z",
      "path": [
        "SEO_64a4e0591734471c30843fc0###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "SEO_64a4e0591734471c30843fc0"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "ChangeDateIssue_655e019308f31814845120b3",
      "nodeName": "ChangeDateIssue",
      "path": "DTS_port###Methods and Tools_acc###PS Internal_ver###Internal_bu",
      "labelName": "project",
      "parentId": "DTS_port",
      "level": 5,
      "basicProjectConfigId": "655e019308f31814845120b3"
    },
    {
      "nodeId": "3215_Unified Commerce - Dan's MVP_64ab97327d51263c17602b58",
      "nodeName": "UC Sprint 12_Unified Commerce - Dan's MVP",
      "sprintStartDate": "2023-10-04T03:30:00.000Z",
      "sprintEndDate": "2023-10-18T03:29:00.000Z",
      "path": [
        "Unified Commerce - Dan's MVP_64ab97327d51263c17602b58###Endeavour Group Pty Ltd_port###Endeavour Group Limited_acc###Retail_ver###International_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Unified Commerce - Dan's MVP_64ab97327d51263c17602b58"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2284_Onsite search_644131b98b61fa2477214bf3",
      "nodeName": "CMS_Sep1_2023Rel.09_Onsite search",
      "sprintStartDate": "2023-08-07T14:41:44.342Z",
      "sprintEndDate": "2023-09-04T14:32:00.000Z",
      "path": [
        "Onsite search_644131b98b61fa2477214bf3###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Onsite search_644131b98b61fa2477214bf3"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "1090_My Account_6441081a72a7c53c78f70595",
      "nodeName": "OMS_May12_2023_Rel5_My Account",
      "sprintStartDate": "2023-04-17T13:17:15.083Z",
      "sprintEndDate": "2023-05-15T19:12:00.000Z",
      "path": [
        "My Account_6441081a72a7c53c78f70595###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "My Account_6441081a72a7c53c78f70595"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "Sonepar EAC_6542b3b008f31814845119a0",
      "nodeName": "Sonepar EAC",
      "path": "Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Sonepar SAS_port",
      "level": 5,
      "basicProjectConfigId": "6542b3b008f31814845119a0"
    },
    {
      "nodeId": "Promotion Engine BOF_64c178d7eb7015715615c5a6",
      "nodeName": "Promotion Engine BOF",
      "path": "ASO_port###Academy Sports_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "ASO_port",
      "level": 5,
      "basicProjectConfigId": "64c178d7eb7015715615c5a6"
    },
    {
      "nodeId": "KYC _63e5fea5ae1aeb593f3395aa",
      "nodeName": "KYC ",
      "path": "ENI-Evolutions_port###ENI S.p.A_acc###Energy & Commodities_ver###EU_bu",
      "labelName": "project",
      "parentId": "ENI-Evolutions_port",
      "level": 5,
      "basicProjectConfigId": "63e5fea5ae1aeb593f3395aa"
    },
    {
      "nodeId": "2795_SAME_63a4810c09378702f4eab210",
      "nodeName": "Sprint 101 - Initial Plan_SAME",
      "sprintStartDate": "2023-10-24T15:12:01.255Z",
      "sprintEndDate": "2023-11-07T12:25:00.000Z",
      "path": [
        "SAME_63a4810c09378702f4eab210###ACE20001_port###Abu Dhabi Investment Authority_acc###PS Internal_ver###EU_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "SAME_63a4810c09378702f4eab210"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "15742_DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "nodeName": "R23.01.2.0.2_DRP - Discovery POD",
      "path": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1"
      ],
      "level": 6,
      "releaseEndDate": "2023-02-06T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2320_Integration Services_6377306a175a953a0a49d322",
      "nodeName": "IS_Sprint 23.20_Integration Services",
      "sprintStartDate": "2023-09-27T14:12:18.774Z",
      "sprintEndDate": "2023-10-11T04:00:00.000Z",
      "path": [
        "Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Integration Services_6377306a175a953a0a49d322"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "3202_Sonepar AFS_654135db88c4b8114af77dba",
      "nodeName": "AFS - S14.6_Sonepar AFS",
      "sprintStartDate": "2023-11-23T20:55:23.671Z",
      "sprintEndDate": "2023-12-06T22:00:00.000Z",
      "path": [
        "Sonepar AFS_654135db88c4b8114af77dba###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar AFS_654135db88c4b8114af77dba"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "37977_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.12.X iOS_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-05-31T00:00:00.000Z",
      "releaseStartDate": "2023-03-01T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2242_Cart & checkout_6441078b72a7c53c78f70590",
      "nodeName": "Cart&Checkout Sept01 2023 Rel9_Cart & checkout",
      "sprintStartDate": "2023-08-07T14:02:03.600Z",
      "sprintEndDate": "2023-09-03T16:15:00.000Z",
      "path": [
        "Cart & checkout_6441078b72a7c53c78f70590###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Cart & checkout_6441078b72a7c53c78f70590"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2021 WLP Brand Retainer_port",
      "nodeName": "2021 WLP Brand Retainer",
      "path": "ADNOC Global Trading Limited_acc###Consumer Products_ver###Government Services_bu",
      "labelName": "port",
      "parentId": "ADNOC Global Trading Limited_acc",
      "level": 4
    },
    {
      "nodeId": "15208_DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "nodeName": "Varys_DRP - Discovery POD",
      "path": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1"
      ],
      "level": 6,
      "releaseEndDate": "2020-09-30T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "15636_DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "nodeName": "Magneto_DRP - Discovery POD",
      "path": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1"
      ],
      "level": 6,
      "releaseEndDate": "2022-09-05T00:00:00.000Z",
      "releaseStartDate": "2022-08-18T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2258_Onsite search_644131b98b61fa2477214bf3",
      "nodeName": "Search_Sep29_2023.Rel10_Onsite search",
      "sprintStartDate": "2023-09-04T20:50:34.462Z",
      "sprintEndDate": "2023-10-01T21:39:00.000Z",
      "path": [
        "Onsite search_644131b98b61fa2477214bf3###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Onsite search_644131b98b61fa2477214bf3"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Legacy CC_sqd_64be65cceb7015715615c4ba",
      "nodeName": "Legacy CC",
      "path": "2309_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2309_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "level": 7
    },
    {
      "nodeId": "Purchase Team_sqd_64be65cceb7015715615c4ba",
      "nodeName": "Purchase Team",
      "path": [
        "2218_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
        "2309_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sqd",
      "parentId": [
        "2218_Dotcom + Mobile App_64be65cceb7015715615c4ba",
        "2309_Dotcom + Mobile App_64be65cceb7015715615c4ba"
      ],
      "level": 7
    },
    {
      "nodeId": "2135_Data Engineering_644258b830d86a7f539c7fd7",
      "nodeName": "DV Sprint 23.12_Data Engineering",
      "sprintStartDate": "2023-06-07T04:01:18.891Z",
      "sprintEndDate": "2023-06-21T03:30:00.000Z",
      "path": [
        "Data Engineering_644258b830d86a7f539c7fd7###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Data Engineering_644258b830d86a7f539c7fd7"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "3272_Sonepar Global_6448a8213be37902a3f1ba45",
      "nodeName": "Data Team 14.4_Sonepar Global",
      "sprintStartDate": "2023-10-26T09:40:42.234Z",
      "sprintEndDate": "2023-11-08T18:13:00.000Z",
      "path": [
        "Sonepar Global_6448a8213be37902a3f1ba45###Sonepar Client Cost - MC_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar Global_6448a8213be37902a3f1ba45"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "11783_Sonepar SAF_6542b3da08f31814845119a2",
      "nodeName": "v1.407 BE S&P_Sonepar SAF",
      "path": [
        "Sonepar SAF_6542b3da08f31814845119a2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar SAF_6542b3da08f31814845119a2"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-27T00:00:00.000Z",
      "releaseStartDate": "2023-10-23T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "280_PSknowHOW _6527af981704342160f43748",
      "nodeName": "KnowHOW | PI_15| ITR_1_PSknowHOW ",
      "sprintStartDate": "2023-09-27T10:23:08.827Z",
      "sprintEndDate": "2023-10-10T08:18:00.000Z",
      "path": [
        "PSknowHOW _6527af981704342160f43748###DTS_port###Methods and Tools_acc###PS Internal_ver###Internal_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "PSknowHOW _6527af981704342160f43748"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "40897_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.15.1523 iOS_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-31T00:00:00.000Z",
      "releaseStartDate": "2023-10-18T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "286_Test rc_657056c668a1225c0126c843",
      "nodeName": "KnowHOW | PI_15| ITR_4_Test rc",
      "sprintStartDate": "2023-11-08T09:30:00.000Z",
      "sprintEndDate": "2023-11-21T08:28:00.000Z",
      "path": [
        "Test rc_657056c668a1225c0126c843###2021 WLP Brand Retainer_port###AB Tetra Pak_acc###Consumer Products_ver###Europe_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Test rc_657056c668a1225c0126c843"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Team 1_sqd_63a02b61bbc09e116d744d9d",
      "nodeName": "Team 1",
      "path": "2229_GearBox_63a02b61bbc09e116d744d9d###GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2229_GearBox_63a02b61bbc09e116d744d9d",
      "level": 7
    },
    {
      "nodeId": "13603_GearBox_63a02b61bbc09e116d744d9d",
      "nodeName": "GB GearboxMobileAPI V3_GearBox",
      "path": [
        "GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "GearBox_63a02b61bbc09e116d744d9d"
      ],
      "level": 6,
      "releaseEndDate": "2023-12-15T00:00:00.000Z",
      "releaseStartDate": "2023-09-11T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2228_GearBox Squad 1_6449103b3be37902a3f1ba70",
      "nodeName": "GB_Sprint 23.19_GearBox Squad 1",
      "sprintStartDate": "2023-09-13T05:00:25.796Z",
      "sprintEndDate": "2023-09-27T03:30:00.000Z",
      "path": [
        "GearBox Squad 1_6449103b3be37902a3f1ba70###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "GearBox Squad 1_6449103b3be37902a3f1ba70"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "54745_DHL Logistics Scrumban_6549029708f3181484511bbb",
      "nodeName": "FT2 Sprint85 14.11.23-28.11.23_DHL Logistics Scrumban",
      "sprintStartDate": "2023-11-14T09:01:00.000Z",
      "sprintEndDate": "2023-11-28T09:01:00.000Z",
      "path": [
        "DHL Logistics Scrumban_6549029708f3181484511bbb###DPDHL - CSI DCI - Logistics and CJ_port###Deutsche Post AG_acc###Automotive_ver###EU_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "DHL Logistics Scrumban_6549029708f3181484511bbb"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "Sunbelt Rentals_acc",
      "nodeName": "Sunbelt Rentals",
      "path": "Retail_ver###North America_bu",
      "labelName": "acc",
      "parentId": "Retail_ver",
      "level": 3
    },
    {
      "nodeId": "2348_CMS_644103e772a7c53c78f70582",
      "nodeName": "CMS_Nov 24_2023_RelXX_CMS",
      "sprintStartDate": "2023-10-30T14:29:04.000Z",
      "sprintEndDate": "2023-11-24T17:53:00.000Z",
      "path": [
        "CMS_644103e772a7c53c78f70582###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "CMS_644103e772a7c53c78f70582"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2237_R1+ Sales_648ab46f6803f300a9fd2e09",
      "nodeName": "SERV_Sprint 23.20_R1+ Sales",
      "sprintStartDate": "2023-09-27T04:30:48.548Z",
      "sprintEndDate": "2023-10-11T03:00:00.000Z",
      "path": [
        "R1+ Sales_648ab46f6803f300a9fd2e09###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "R1+ Sales_648ab46f6803f300a9fd2e09"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "990_DRP - HomePage POD_64b3f315c4e72b57c94035e2",
      "nodeName": "DRP Sprint 81_DRP - HomePage POD",
      "sprintStartDate": "2023-08-11T11:05:47.715Z",
      "sprintEndDate": "2023-08-31T14:53:00.000Z",
      "path": [
        "DRP - HomePage POD_64b3f315c4e72b57c94035e2###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "DRP - HomePage POD_64b3f315c4e72b57c94035e2"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "13553_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "nodeName": "SBRWEB 1.6_Dotcom + Mobile App",
      "path": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Dotcom + Mobile App_64be65cceb7015715615c4ba"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-29T00:00:00.000Z",
      "releaseStartDate": "2023-08-16T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "11713_CMS_644103e772a7c53c78f70582",
      "nodeName": "Website Oct 2023 - HF2 Release_CMS",
      "path": [
        "CMS_644103e772a7c53c78f70582###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "CMS_644103e772a7c53c78f70582"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-23T00:00:00.000Z",
      "releaseStartDate": "2023-10-16T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "40762_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.15.1521 iOS_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-13T00:00:00.000Z",
      "releaseStartDate": "2023-09-29T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "45281_MAP_63a304a909378702f4eab1d0",
      "nodeName": "MAP|PI_14|ITR_4_MAP",
      "sprintStartDate": "2023-08-09T07:03:00.000Z",
      "sprintEndDate": "2023-08-23T07:03:00.000Z",
      "path": [
        "MAP_63a304a909378702f4eab1d0###DTS_port###Methods and Tools_acc###PS Internal_ver###Internal_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "MAP_63a304a909378702f4eab1d0"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "3198_Sonepar AFS_654135db88c4b8114af77dba",
      "nodeName": "AFS - S14.2_Sonepar AFS",
      "sprintStartDate": "2023-09-28T17:04:39.186Z",
      "sprintEndDate": "2023-10-11T21:30:00.000Z",
      "path": [
        "Sonepar AFS_654135db88c4b8114af77dba###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar AFS_654135db88c4b8114af77dba"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "P2P_64ca0b8f5fec906dbc18f3c5",
      "nodeName": "P2P",
      "path": "Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Sunbelt Rentals_port",
      "level": 5,
      "basicProjectConfigId": "64ca0b8f5fec906dbc18f3c5"
    },
    {
      "nodeId": "40666_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.14.2213 Android_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-22T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Released"
    },
    {
      "nodeId": "MAP_63a304a909378702f4eab1d0",
      "nodeName": "MAP",
      "path": "DTS_port###Methods and Tools_acc###PS Internal_ver###Internal_bu",
      "labelName": "project",
      "parentId": "DTS_port",
      "level": 5,
      "basicProjectConfigId": "63a304a909378702f4eab1d0"
    },
    {
      "nodeId": "2348_Onsite search_644131b98b61fa2477214bf3",
      "nodeName": "CMS_Nov 24_2023_RelXX_Onsite search",
      "sprintStartDate": "2023-10-30T14:29:04.000Z",
      "sprintEndDate": "2023-11-24T17:53:00.000Z",
      "path": [
        "Onsite search_644131b98b61fa2477214bf3###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Onsite search_644131b98b61fa2477214bf3"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "2616_Sonepar Mobile App_6448a96c3be37902a3f1ba48",
      "nodeName": "DFA QA Sprint 12.3_Sonepar Mobile App",
      "sprintStartDate": "2023-05-11T10:12:01.549Z",
      "sprintEndDate": "2023-05-26T10:11:00.000Z",
      "path": [
        "Sonepar Mobile App_6448a96c3be37902a3f1ba48###Sonepar Client Cost - MC_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar Mobile App_6448a96c3be37902a3f1ba48"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "Team 2_sqd_64770ec45286e83998a56141",
      "nodeName": "Team 2",
      "path": "2228_GearBox Squad 2_64770ec45286e83998a56141###GearBox Squad 2_64770ec45286e83998a56141###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2228_GearBox Squad 2_64770ec45286e83998a56141",
      "level": 7
    },
    {
      "nodeId": "31551af7-a7c0-4424-9251-238d04805bd4_Azure Project_656d830b9f546b19742cb55b",
      "nodeName": "Iteration 2_Azure Project",
      "sprintStartDate": "2023-05-28T00:00:00.000Z",
      "sprintEndDate": "2023-05-29T00:00:00.000Z",
      "path": [
        "Azure Project_656d830b9f546b19742cb55b###3PP CRM_port###ADEO_acc###B_ver###Europe_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Azure Project_656d830b9f546b19742cb55b"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "151225_Do it Best_657049b505ce0569d5612ec7",
      "nodeName": "FEAEM_HOTFIX_RV1.4_02_Do it Best",
      "path": [
        "Do it Best_657049b505ce0569d5612ec7###3PP CRM_port###ADEO_acc###Automotive_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Do it Best_657049b505ce0569d5612ec7"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-10T00:00:00.000Z",
      "releaseStartDate": "2023-11-09T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": " Buy & Deliver_651af337d18501286c28a464",
      "nodeName": " Buy & Deliver",
      "path": "Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Sonepar SAS_port",
      "level": 5,
      "basicProjectConfigId": "651af337d18501286c28a464"
    },
    {
      "nodeId": "3219_Unified Commerce - Dan's MVP_64ab97327d51263c17602b58",
      "nodeName": "UC Sprint 16_Unified Commerce - Dan's MVP",
      "sprintStartDate": "2023-11-29T04:40:40.470Z",
      "sprintEndDate": "2023-12-13T03:30:00.000Z",
      "path": [
        "Unified Commerce - Dan's MVP_64ab97327d51263c17602b58###Endeavour Group Pty Ltd_port###Endeavour Group Limited_acc###Retail_ver###International_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Unified Commerce - Dan's MVP_64ab97327d51263c17602b58"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "15572_DRP - HomePage POD_64b3f315c4e72b57c94035e2",
      "nodeName": "Firelord_DRP - HomePage POD",
      "path": [
        "DRP - HomePage POD_64b3f315c4e72b57c94035e2###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "DRP - HomePage POD_64b3f315c4e72b57c94035e2"
      ],
      "level": 6,
      "releaseEndDate": "2022-05-18T00:00:00.000Z",
      "releaseStartDate": "2022-05-18T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "3201_Sonepar Cloud_6542b82208f31814845119bb",
      "nodeName": "AFS - S14.5_Sonepar Cloud",
      "sprintStartDate": "2023-11-09T16:30:42.172Z",
      "sprintEndDate": "2023-11-22T22:00:00.000Z",
      "path": [
        "Sonepar Cloud_6542b82208f31814845119bb###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar Cloud_6542b82208f31814845119bb"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "153001_Do it Best_657049b505ce0569d5612ec7",
      "nodeName": "MAG-HOTFIX-1.6-P1_Do it Best",
      "path": [
        "Do it Best_657049b505ce0569d5612ec7###3PP CRM_port###ADEO_acc###Automotive_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Do it Best_657049b505ce0569d5612ec7"
      ],
      "level": 6,
      "releaseEndDate": "2023-12-06T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Released"
    },
    {
      "nodeId": "2245_CMS_644103e772a7c53c78f70582",
      "nodeName": "Cart&Checkout Nov24 2023 Rel12_CMS",
      "sprintStartDate": "2023-10-30T14:11:17.486Z",
      "sprintEndDate": "2023-11-26T16:16:00.000Z",
      "path": [
        "CMS_644103e772a7c53c78f70582###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "CMS_644103e772a7c53c78f70582"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "2318_PIM_64a4e09e1734471c30843fc2",
      "nodeName": "Fanatics Nov Sprint - Rel11_PIM",
      "sprintStartDate": "2023-10-02T21:08:44.423Z",
      "sprintEndDate": "2023-10-30T18:03:00.000Z",
      "path": [
        "PIM_64a4e09e1734471c30843fc2###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "PIM_64a4e09e1734471c30843fc2"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "13556_Ecom Pre-Purchase Squad_64be66e3eb7015715615c4bd",
      "nodeName": "SBRAPP 3.4_Ecom Pre-Purchase Squad",
      "path": [
        "Ecom Pre-Purchase Squad_64be66e3eb7015715615c4bd###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Ecom Pre-Purchase Squad_64be66e3eb7015715615c4bd"
      ],
      "level": 6,
      "releaseEndDate": "2023-12-05T00:00:00.000Z",
      "releaseStartDate": "2023-06-20T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Content_sqd_64be65cceb7015715615c4ba",
      "nodeName": "Content",
      "path": "2268_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2268_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "level": 7
    },
    {
      "nodeId": "11825_Sonepar SAF_6542b3da08f31814845119a2",
      "nodeName": "v1.406 FE RC_Sonepar SAF",
      "path": [
        "Sonepar SAF_6542b3da08f31814845119a2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar SAF_6542b3da08f31814845119a2"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-22T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Released"
    },
    {
      "nodeId": "11830_Sonepar AFS_654135db88c4b8114af77dba",
      "nodeName": "v1.408 FE_Sonepar AFS",
      "path": [
        "Sonepar AFS_654135db88c4b8114af77dba###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar AFS_654135db88c4b8114af77dba"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-24T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Released"
    },
    {
      "nodeId": "10146_Test rc_657056c668a1225c0126c843",
      "nodeName": "KnowHOW v9.0.0_Test rc",
      "path": [
        "Test rc_657056c668a1225c0126c843###2021 WLP Brand Retainer_port###AB Tetra Pak_acc###Consumer Products_ver###Europe_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Test rc_657056c668a1225c0126c843"
      ],
      "level": 6,
      "releaseEndDate": "2024-01-11T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2220_Cart & checkout_6441078b72a7c53c78f70590",
      "nodeName": "CMS_Jul07_2023.Rel07_Cart & checkout",
      "sprintStartDate": "2023-06-12T14:37:55.686Z",
      "sprintEndDate": "2023-07-10T14:28:00.000Z",
      "path": [
        "Cart & checkout_6441078b72a7c53c78f70590###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Cart & checkout_6441078b72a7c53c78f70590"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "133807_pod16 2_657219fd78247c3cd726d630",
      "nodeName": "Release 23.02_pod16 2",
      "path": [
        "pod16 2_657219fd78247c3cd726d630###2021 WLP Brand Retainer_port###AAA Auto Club Group_acc###Automative1_ver###A_bu"
      ],
      "labelName": "release",
      "parentId": [
        "pod16 2_657219fd78247c3cd726d630"
      ],
      "level": 6,
      "releaseEndDate": "2023-02-08T00:00:00.000Z",
      "releaseStartDate": "2023-01-23T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "10175_Test rc_657056c668a1225c0126c843",
      "nodeName": "KnowHOW PI-16_Test rc",
      "path": [
        "Test rc_657056c668a1225c0126c843###2021 WLP Brand Retainer_port###AB Tetra Pak_acc###Consumer Products_ver###Europe_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Test rc_657056c668a1225c0126c843"
      ],
      "level": 6,
      "releaseEndDate": "2024-03-26T00:00:00.000Z",
      "releaseStartDate": "2023-12-27T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "1089_Data Visualization_64425a0a30d86a7f539c7fdc",
      "nodeName": "SAL_Sprint 23.12_Data Visualization",
      "sprintStartDate": "2023-06-07T21:44:56.965Z",
      "sprintEndDate": "2023-06-20T10:27:00.000Z",
      "path": [
        "Data Visualization_64425a0a30d86a7f539c7fdc###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Data Visualization_64425a0a30d86a7f539c7fdc"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "2285_CMS_644103e772a7c53c78f70582",
      "nodeName": "CMS_Oct2_2023Rel.10_CMS",
      "sprintStartDate": "2023-09-05T14:03:22.074Z",
      "sprintEndDate": "2023-10-02T15:53:00.000Z",
      "path": [
        "CMS_644103e772a7c53c78f70582###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "CMS_644103e772a7c53c78f70582"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2257_Onsite search_644131b98b61fa2477214bf3",
      "nodeName": "Search_Sep01_2023.Rel9_Onsite search",
      "sprintStartDate": "2023-08-07T21:41:19.769Z",
      "sprintEndDate": "2023-09-03T22:30:00.000Z",
      "path": [
        "Onsite search_644131b98b61fa2477214bf3###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Onsite search_644131b98b61fa2477214bf3"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "39762_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.12.1488 iOS_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-05-27T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "3282_Design System_64ad9e667d51263c17602c67",
      "nodeName": "WATTS 14.1_Design System",
      "sprintStartDate": "2023-09-15T07:46:35.208Z",
      "sprintEndDate": "2023-09-26T22:00:00.000Z",
      "path": [
        "Design System_64ad9e667d51263c17602c67###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Design System_64ad9e667d51263c17602c67"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "11819_Sonepar SAF_6542b3da08f31814845119a2",
      "nodeName": "v1.403 FE RC_Sonepar SAF",
      "path": [
        "Sonepar SAF_6542b3da08f31814845119a2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar SAF_6542b3da08f31814845119a2"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-13T00:00:00.000Z",
      "releaseStartDate": "2023-11-06T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "Post-Purchase Team_sqd_64be65cceb7015715615c4ba",
      "nodeName": "Post-Purchase Team",
      "path": "2218_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2218_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "level": 7
    },
    {
      "nodeId": "42334_DHL Logistics Scrumban_6549029708f3181484511bbb",
      "nodeName": "Joint Infra BAU 2023_DHL Logistics Scrumban",
      "sprintStartDate": "2023-01-02T08:11:00.000Z",
      "sprintEndDate": "2023-12-31T08:11:00.000Z",
      "path": [
        "DHL Logistics Scrumban_6549029708f3181484511bbb###DPDHL - CSI DCI - Logistics and CJ_port###Deutsche Post AG_acc###Automotive_ver###EU_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "DHL Logistics Scrumban_6549029708f3181484511bbb"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "3PP CRM_port",
      "nodeName": "3PP CRM",
      "path": "AAA Auto Club Group_acc###Automative1_ver###EU_bu",
      "labelName": "port",
      "parentId": "AAA Auto Club Group_acc",
      "level": 4
    },
    {
      "nodeId": "52302_POD 16_657065700615235d92401735",
      "nodeName": "POD 16 | Sprint 23_10_POD 16",
      "sprintStartDate": "2023-08-03T14:58:00.000Z",
      "sprintEndDate": "2023-08-16T14:58:00.000Z",
      "path": [
        "POD 16_657065700615235d92401735###Nissan Core Team - PS_port###Nissan Motor Co. Ltd._acc###Automotive_ver###International_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "POD 16_657065700615235d92401735"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Team 3_sqd_63a02b61bbc09e116d744d9d",
      "nodeName": "Team 3",
      "path": "2369_GearBox_63a02b61bbc09e116d744d9d###GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2369_GearBox_63a02b61bbc09e116d744d9d",
      "level": 7
    },
    {
      "nodeId": "3197_Sonepar AFS_654135db88c4b8114af77dba",
      "nodeName": "AFS - S14.1_Sonepar AFS",
      "sprintStartDate": "2023-09-14T18:50:18.352Z",
      "sprintEndDate": "2023-09-27T06:16:00.000Z",
      "path": [
        "Sonepar AFS_654135db88c4b8114af77dba###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar AFS_654135db88c4b8114af77dba"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2179_Website BOF_644108c072a7c53c78f7059a",
      "nodeName": "Cart&Checkout June09 2023 Rel6_Website BOF",
      "sprintStartDate": "2023-05-15T14:09:55.158Z",
      "sprintEndDate": "2023-06-11T13:52:00.000Z",
      "path": [
        "Website BOF_644108c072a7c53c78f7059a###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Website BOF_644108c072a7c53c78f7059a"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "2224_P2P_64ca0b8f5fec906dbc18f3c5",
      "nodeName": "P2P_Sprint 23.21_P2P",
      "sprintStartDate": "2023-10-11T15:21:04.000Z",
      "sprintEndDate": "2023-10-24T22:00:00.000Z",
      "path": [
        "P2P_64ca0b8f5fec906dbc18f3c5###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "P2P_64ca0b8f5fec906dbc18f3c5"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Integration Services_6377306a175a953a0a49d322",
      "nodeName": "Integration Services",
      "path": "Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Sunbelt Rentals_port",
      "level": 5,
      "basicProjectConfigId": "6377306a175a953a0a49d322"
    },
    {
      "nodeId": "2246_Data Engineering_644258b830d86a7f539c7fd7",
      "nodeName": "DEA Sprint 23.19_Data Engineering",
      "sprintStartDate": "2023-09-13T04:19:23.134Z",
      "sprintEndDate": "2023-09-27T03:30:00.000Z",
      "path": [
        "Data Engineering_644258b830d86a7f539c7fd7###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Data Engineering_644258b830d86a7f539c7fd7"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Team 1_sqd_6449103b3be37902a3f1ba70",
      "nodeName": "Team 1",
      "path": "2228_GearBox Squad 1_6449103b3be37902a3f1ba70###GearBox Squad 1_6449103b3be37902a3f1ba70###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2228_GearBox Squad 1_6449103b3be37902a3f1ba70",
      "level": 7
    },
    {
      "nodeId": "3270_Sonepar Global_6448a8213be37902a3f1ba45",
      "nodeName": "Data Team 14.2_Sonepar Global",
      "sprintStartDate": "2023-09-28T08:43:34.748Z",
      "sprintEndDate": "2023-10-11T18:13:00.000Z",
      "path": [
        "Sonepar Global_6448a8213be37902a3f1ba45###Sonepar Client Cost - MC_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Sonepar Global_6448a8213be37902a3f1ba45"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "40892_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "1.17.0 API_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-27T00:00:00.000Z",
      "releaseStartDate": "2023-06-01T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "11785_Sonepar SAF_6542b3da08f31814845119a2",
      "nodeName": "v1.394 FE_Sonepar SAF",
      "path": [
        "Sonepar SAF_6542b3da08f31814845119a2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar SAF_6542b3da08f31814845119a2"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-31T00:00:00.000Z",
      "releaseStartDate": "2023-10-23T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "11706_CMS_644103e772a7c53c78f70582",
      "nodeName": "More Options - Mar 2024 Release _CMS",
      "path": [
        "CMS_644103e772a7c53c78f70582###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "CMS_644103e772a7c53c78f70582"
      ],
      "level": 6,
      "releaseEndDate": "2024-03-29T00:00:00.000Z",
      "releaseStartDate": "2023-08-07T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "11790_Sonepar AFS_654135db88c4b8114af77dba",
      "nodeName": "v1.398 FE_Sonepar AFS",
      "path": [
        "Sonepar AFS_654135db88c4b8114af77dba###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar AFS_654135db88c4b8114af77dba"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-03T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Released"
    },
    {
      "nodeId": "40129_SAME_63a4810c09378702f4eab210",
      "nodeName": "1.3.33.1_SAME",
      "path": [
        "SAME_63a4810c09378702f4eab210###ACE20001_port###Abu Dhabi Investment Authority_acc###PS Internal_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "SAME_63a4810c09378702f4eab210"
      ],
      "level": 6,
      "releaseEndDate": "2023-07-05T00:00:00.000Z",
      "releaseStartDate": "2023-06-26T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "13553_Ecom Post-Purchase Squad_64be67caeb7015715615c4c5",
      "nodeName": "SBRWEB 1.6_Ecom Post-Purchase Squad",
      "path": [
        "Ecom Post-Purchase Squad_64be67caeb7015715615c4c5###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Ecom Post-Purchase Squad_64be67caeb7015715615c4c5"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-29T00:00:00.000Z",
      "releaseStartDate": "2023-08-16T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "Supply Chain_sqd_6377306a175a953a0a49d322",
      "nodeName": "Supply Chain",
      "path": "2377_Integration Services_6377306a175a953a0a49d322###Integration Services_6377306a175a953a0a49d322###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2377_Integration Services_6377306a175a953a0a49d322",
      "level": 7
    },
    {
      "nodeId": "2293_My Account_6441081a72a7c53c78f70595",
      "nodeName": "MyAccount_Sep01_2023.Rel9_My Account",
      "sprintStartDate": "2023-08-07T13:06:10.926Z",
      "sprintEndDate": "2023-09-01T16:15:00.000Z",
      "path": [
        "My Account_6441081a72a7c53c78f70595###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "My Account_6441081a72a7c53c78f70595"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "41028_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": " 3.15.1527 iOS_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-12-07T00:00:00.000Z",
      "releaseStartDate": "2023-11-16T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2318_GearBox Squad 1_6449103b3be37902a3f1ba70",
      "nodeName": "GB_Sprint 23.22_GearBox Squad 1",
      "sprintStartDate": "2023-10-25T11:33:17.758Z",
      "sprintEndDate": "2023-11-08T03:30:00.000Z",
      "path": [
        "GearBox Squad 1_6449103b3be37902a3f1ba70###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "GearBox Squad 1_6449103b3be37902a3f1ba70"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "10385_Promotion Engine BOF_64c178d7eb7015715615c5a6",
      "nodeName": "Promotions Aug 2023 Release_Promotion Engine BOF",
      "path": [
        "Promotion Engine BOF_64c178d7eb7015715615c5a6###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Promotion Engine BOF_64c178d7eb7015715615c5a6"
      ],
      "level": 6,
      "releaseEndDate": "2024-01-25T00:00:00.000Z",
      "releaseStartDate": "2023-07-01T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Team 2_sqd_648ab46f6803f300a9fd2e09",
      "nodeName": "Team 2",
      "path": "2288_R1+ Sales_648ab46f6803f300a9fd2e09###R1+ Sales_648ab46f6803f300a9fd2e09###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2288_R1+ Sales_648ab46f6803f300a9fd2e09",
      "level": 7
    },
    {
      "nodeId": "SEO_64a4e0591734471c30843fc0",
      "nodeName": "SEO",
      "path": "ASO_port###Academy Sports_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "ASO_port",
      "level": 5,
      "basicProjectConfigId": "64a4e0591734471c30843fc0"
    },
    {
      "nodeId": "Team 2_sqd_63a02b61bbc09e116d744d9d",
      "nodeName": "Team 2",
      "path": "2317_GearBox_63a02b61bbc09e116d744d9d###GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2317_GearBox_63a02b61bbc09e116d744d9d",
      "level": 7
    },
    {
      "nodeId": "10029_PSknowHOW _6527af981704342160f43748",
      "nodeName": "KnowHOW PI-14_PSknowHOW ",
      "path": [
        "PSknowHOW _6527af981704342160f43748###DTS_port###Methods and Tools_acc###PS Internal_ver###Internal_bu"
      ],
      "labelName": "release",
      "parentId": [
        "PSknowHOW _6527af981704342160f43748"
      ],
      "level": 6,
      "releaseEndDate": "2023-09-26T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Azure Project_656d830b9f546b19742cb55b",
      "nodeName": "Azure Project",
      "path": "3PP CRM_port###ADEO_acc###B_ver###Europe_bu",
      "labelName": "project",
      "parentId": "3PP CRM_port",
      "level": 5,
      "basicProjectConfigId": "656d830b9f546b19742cb55b"
    },
    {
      "nodeId": "1115_Mobile App_637b17b9175a953a0a49d3c2",
      "nodeName": "MA_Sprint 23.13_Mobile App",
      "sprintStartDate": "2023-06-21T15:15:57.809Z",
      "sprintEndDate": "2023-07-04T21:00:00.000Z",
      "path": [
        "Mobile App_637b17b9175a953a0a49d3c2###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Mobile App_637b17b9175a953a0a49d3c2"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "53768_POD 16_657065700615235d92401735",
      "nodeName": "POD 16 | Sprint 23_13_POD 16",
      "sprintStartDate": "2023-09-14T10:30:00.000Z",
      "sprintEndDate": "2023-09-27T18:46:00.000Z",
      "path": [
        "POD 16_657065700615235d92401735###Nissan Core Team - PS_port###Nissan Motor Co. Ltd._acc###Automotive_ver###International_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "POD 16_657065700615235d92401735"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "Purchase Team_sqd_64be65cceb7015715615c4ba",
      "nodeName": "Purchase Team",
      "path": "2309_Dotcom + Mobile App_64be65cceb7015715615c4ba###Dotcom + Mobile App_64be65cceb7015715615c4ba###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2309_Dotcom + Mobile App_64be65cceb7015715615c4ba",
      "level": 7
    },
    {
      "nodeId": "2229_GearBox Squad 2_64770ec45286e83998a56141",
      "nodeName": "GB_Sprint 23.20_GearBox Squad 2",
      "sprintStartDate": "2023-09-27T05:00:43.565Z",
      "sprintEndDate": "2023-10-11T03:30:00.000Z",
      "path": [
        "GearBox Squad 2_64770ec45286e83998a56141###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "GearBox Squad 2_64770ec45286e83998a56141"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2276_Data Visualization_64425a0a30d86a7f539c7fdc",
      "nodeName": "DV Sprint 23.20_Data Visualization",
      "sprintStartDate": "2023-09-27T05:19:37.103Z",
      "sprintEndDate": "2023-10-11T03:30:00.000Z",
      "path": [
        "Data Visualization_64425a0a30d86a7f539c7fdc###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Data Visualization_64425a0a30d86a7f539c7fdc"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "1057_Promotion Engine TOF_644105f972a7c53c78f7058c",
      "nodeName": "Promo_Engine_TOF_Jun_9_Promotion Engine TOF",
      "sprintStartDate": "2023-05-15T20:19:11.035Z",
      "sprintEndDate": "2023-06-10T08:19:00.000Z",
      "path": [
        "Promotion Engine TOF_644105f972a7c53c78f7058c###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Promotion Engine TOF_644105f972a7c53c78f7058c"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2317_GearBox_63a02b61bbc09e116d744d9d",
      "nodeName": "GB_Sprint 23.21_GearBox",
      "sprintStartDate": "2023-10-11T06:00:29.059Z",
      "sprintEndDate": "2023-10-25T03:30:00.000Z",
      "path": [
        "GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "GearBox_63a02b61bbc09e116d744d9d"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2378_PIM_64a4e09e1734471c30843fc2",
      "nodeName": "PIM_Nov24_23_Sprint_44_PIM",
      "sprintStartDate": "2023-10-30T15:56:47.670Z",
      "sprintEndDate": "2023-11-27T15:56:00.000Z",
      "path": [
        "PIM_64a4e09e1734471c30843fc2###ASO_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "PIM_64a4e09e1734471c30843fc2"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "151205_Do it Best_657049b505ce0569d5612ec7",
      "nodeName": "FEAEM_HOTFIX_RV1.4_01_Do it Best",
      "path": [
        "Do it Best_657049b505ce0569d5612ec7###3PP CRM_port###ADEO_acc###Automotive_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Do it Best_657049b505ce0569d5612ec7"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-06T00:00:00.000Z",
      "releaseStartDate": "2023-11-02T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "11711_My Account_6441081a72a7c53c78f70595",
      "nodeName": "Website Jan 2024 Release_My Account",
      "path": [
        "My Account_6441081a72a7c53c78f70595###API_port###Academy Sports_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "My Account_6441081a72a7c53c78f70595"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-27T00:00:00.000Z",
      "releaseStartDate": "2023-10-02T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Health_ver",
      "nodeName": "Health",
      "path": "EU_bu",
      "labelName": "ver",
      "parentId": "EU_bu",
      "level": 2
    },
    {
      "nodeId": "Methods and Tools_acc",
      "nodeName": "Methods and Tools",
      "path": "PS Internal_ver###Internal_bu",
      "labelName": "acc",
      "parentId": "PS Internal_ver",
      "level": 3
    },
    {
      "nodeId": "40867_SAME_63a4810c09378702f4eab210",
      "nodeName": "1.3.39_SAME",
      "path": [
        "SAME_63a4810c09378702f4eab210###ACE20001_port###Abu Dhabi Investment Authority_acc###PS Internal_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "SAME_63a4810c09378702f4eab210"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-17T00:00:00.000Z",
      "releaseStartDate": "2023-10-11T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "2305_Service & Assets_6494298bca84920b10dddd3b",
      "nodeName": "SERV_Sprint 23.24_Service & Assets",
      "sprintStartDate": "2023-11-22T05:00:41.347Z",
      "sprintEndDate": "2023-12-06T04:30:00.000Z",
      "path": [
        "Service & Assets_6494298bca84920b10dddd3b###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Service & Assets_6494298bca84920b10dddd3b"
      ],
      "sprintState": "active",
      "level": 6
    },
    {
      "nodeId": "13606_Ecom Post-Purchase Squad_64be67caeb7015715615c4c5",
      "nodeName": "SBRWEB 2.2 _Ecom Post-Purchase Squad",
      "path": [
        "Ecom Post-Purchase Squad_64be67caeb7015715615c4c5###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Ecom Post-Purchase Squad_64be67caeb7015715615c4c5"
      ],
      "level": 6,
      "releaseEndDate": "2024-03-20T00:00:00.000Z",
      "releaseStartDate": "2023-11-08T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Promotion Engine TOF_644105f972a7c53c78f7058c",
      "nodeName": "Promotion Engine TOF",
      "path": "ASO_port###Academy Sports_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "ASO_port",
      "level": 5,
      "basicProjectConfigId": "644105f972a7c53c78f7058c"
    },
    {
      "nodeId": "4254_RABO Scrum (New)_64a5dac31734471c30844068",
      "nodeName": "Sentry FY24 Sprint 19_RABO Scrum (New)",
      "sprintStartDate": "2023-10-12T09:22:30.229Z",
      "sprintEndDate": "2023-10-25T22:30:00.000Z",
      "path": [
        "RABO Scrum (New)_64a5dac31734471c30844068###AA_port###ADEO_acc###Financial Services_ver###EU_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "RABO Scrum (New)_64a5dac31734471c30844068"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "151405_Do it Best_657049b505ce0569d5612ec7",
      "nodeName": "MAG-HOTFIX-1.4-P2_Do it Best",
      "path": [
        "Do it Best_657049b505ce0569d5612ec7###3PP CRM_port###ADEO_acc###Automotive_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Do it Best_657049b505ce0569d5612ec7"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-06T00:00:00.000Z",
      "releaseStartDate": "2023-11-01T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "Browse & Search_sqd_63dc01e47228be4c30553ce1",
      "nodeName": "Browse & Search",
      "path": "1016_DRP - Discovery POD_63dc01e47228be4c30553ce1###DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "1016_DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "level": 7
    },
    {
      "nodeId": "2207_P2P_64ca0b8f5fec906dbc18f3c5",
      "nodeName": "ECOM_Sprint 23.17_P2P",
      "sprintStartDate": "2023-08-16T16:39:13.848Z",
      "sprintEndDate": "2023-08-29T20:00:00.000Z",
      "path": [
        "P2P_64ca0b8f5fec906dbc18f3c5###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "P2P_64ca0b8f5fec906dbc18f3c5"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "302_Test rc_657056c668a1225c0126c843",
      "nodeName": "KnowHOW | PI_15| ITR_6_Test rc",
      "sprintStartDate": "2023-12-06T06:20:52.058Z",
      "sprintEndDate": "2023-12-26T08:28:00.000Z",
      "path": [
        "Test rc_657056c668a1225c0126c843###2021 WLP Brand Retainer_port###AB Tetra Pak_acc###Consumer Products_ver###Europe_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "Test rc_657056c668a1225c0126c843"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "Retrol_657152fe52bfa01f6cff044f",
      "nodeName": "Retrol",
      "path": "2021 WLP Brand Retainer_port###AB Tetra Pak_acc###B_ver###Government Services_bu",
      "labelName": "project",
      "parentId": "2021 WLP Brand Retainer_port",
      "level": 5,
      "basicProjectConfigId": "657152fe52bfa01f6cff044f"
    },
    {
      "nodeId": "40340_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.13.1510 iOS_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-08-26T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "133520_DHL Logistics Scrumban_6549029708f3181484511bbb",
      "nodeName": "Logistics 2023.11.0_DHL Logistics Scrumban",
      "path": [
        "DHL Logistics Scrumban_6549029708f3181484511bbb###DPDHL - CSI DCI - Logistics and CJ_port###Deutsche Post AG_acc###Automotive_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "DHL Logistics Scrumban_6549029708f3181484511bbb"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-09T00:00:00.000Z",
      "releaseStartDate": "2023-10-03T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "15826_DRP - Discovery POD_63dc01e47228be4c30553ce1",
      "nodeName": "R23.10.3_DRP - Discovery POD",
      "path": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1###Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "DRP - Discovery POD_63dc01e47228be4c30553ce1"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-08T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Released"
    },
    {
      "nodeId": "Ecom Post-Purchase Squad_64be67caeb7015715615c4c5",
      "nodeName": "Ecom Post-Purchase Squad",
      "path": "Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "project",
      "parentId": "Sunbelt Rentals_port",
      "level": 5,
      "basicProjectConfigId": "64be67caeb7015715615c4c5"
    },
    {
      "nodeId": "2749_SAME_63a4810c09378702f4eab210",
      "nodeName": "Sprint 99 - Initial Plan_SAME",
      "sprintStartDate": "2023-09-26T14:44:39.960Z",
      "sprintEndDate": "2023-10-10T12:22:00.000Z",
      "path": [
        "SAME_63a4810c09378702f4eab210###ACE20001_port###Abu Dhabi Investment Authority_acc###PS Internal_ver###EU_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "SAME_63a4810c09378702f4eab210"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "10139_PSknowHOW _6527af981704342160f43748",
      "nodeName": "KnowHOW v8.1.0_PSknowHOW ",
      "path": [
        "PSknowHOW _6527af981704342160f43748###DTS_port###Methods and Tools_acc###PS Internal_ver###Internal_bu"
      ],
      "labelName": "release",
      "parentId": [
        "PSknowHOW _6527af981704342160f43748"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-28T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Released"
    },
    {
      "nodeId": "11807_Sonepar SAF_6542b3da08f31814845119a2",
      "nodeName": "v1.402 FE_Sonepar SAF",
      "path": [
        "Sonepar SAF_6542b3da08f31814845119a2###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar SAF_6542b3da08f31814845119a2"
      ],
      "level": 6,
      "releaseEndDate": "2023-11-08T00:00:00.000Z",
      "releaseStartDate": "2023-11-02T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "39539_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.13.X iOS_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-08-04T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "2405_VDOS_63777558175a953a0a49d363",
      "nodeName": "OMS_Sprint 23.24_VDOS",
      "sprintStartDate": "2023-11-21T16:22:25.465Z",
      "sprintEndDate": "2023-12-05T05:00:00.000Z",
      "path": [
        "VDOS_63777558175a953a0a49d363###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "VDOS_63777558175a953a0a49d363"
      ],
      "sprintState": "ACTIVE",
      "level": 6
    },
    {
      "nodeId": "2355_VDOS_63777558175a953a0a49d363",
      "nodeName": "OMS_Sprint 23.22_VDOS",
      "sprintStartDate": "2023-10-25T15:31:14.456Z",
      "sprintEndDate": "2023-11-08T15:31:03.000Z",
      "path": [
        "VDOS_63777558175a953a0a49d363###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "VDOS_63777558175a953a0a49d363"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "11770_Sonepar AFS_654135db88c4b8114af77dba",
      "nodeName": "v1.395 BE M&P_Sonepar AFS",
      "path": [
        "Sonepar AFS_654135db88c4b8114af77dba###Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Sonepar AFS_654135db88c4b8114af77dba"
      ],
      "level": 6,
      "releaseEndDate": "2023-10-31T00:00:00.000Z",
      "releaseStartDate": "2023-10-18T00:00:00.000Z",
      "releaseState": "Released"
    },
    {
      "nodeId": "13621_Ecom Pre-Purchase Squad_64be66e3eb7015715615c4bd",
      "nodeName": "SBRAPP 3.6 _Ecom Pre-Purchase Squad",
      "path": [
        "Ecom Pre-Purchase Squad_64be66e3eb7015715615c4bd###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "release",
      "parentId": [
        "Ecom Pre-Purchase Squad_64be66e3eb7015715615c4bd"
      ],
      "level": 6,
      "releaseEndDate": "2024-04-24T00:00:00.000Z",
      "releaseStartDate": "2024-01-17T00:00:00.000Z",
      "releaseState": "Unreleased"
    },
    {
      "nodeId": "Team 1_sqd_63a02b61bbc09e116d744d9d",
      "nodeName": "Team 1",
      "path": "2326_GearBox_63a02b61bbc09e116d744d9d###GearBox_63a02b61bbc09e116d744d9d###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu",
      "labelName": "sqd",
      "parentId": "2326_GearBox_63a02b61bbc09e116d744d9d",
      "level": 7
    },
    {
      "nodeId": "717d8091-8e4e-4a14-a897-b2502accf34d_KYC _63e5fea5ae1aeb593f3395aa",
      "nodeName": "Sprint 23_KYC ",
      "sprintStartDate": "2023-09-18T00:00:00.000Z",
      "sprintEndDate": "2023-10-06T00:00:00.000Z",
      "path": [
        "KYC _63e5fea5ae1aeb593f3395aa###ENI-Evolutions_port###ENI S.p.A_acc###Energy & Commodities_ver###EU_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "KYC _63e5fea5ae1aeb593f3395aa"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "2354_VDOS_63777558175a953a0a49d363",
      "nodeName": "OMS_Sprint 23.21_VDOS",
      "sprintStartDate": "2023-10-11T09:16:16.993Z",
      "sprintEndDate": "2023-10-25T09:16:08.000Z",
      "path": [
        "VDOS_63777558175a953a0a49d363###Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu"
      ],
      "labelName": "sprint",
      "parentId": [
        "VDOS_63777558175a953a0a49d363"
      ],
      "sprintState": "CLOSED",
      "level": 6
    },
    {
      "nodeId": "RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "RMMO",
      "path": "Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu",
      "labelName": "project",
      "parentId": "Regina Maria Portofolio_port",
      "level": 5,
      "basicProjectConfigId": "6392c9225a7c6d3e49b53f19"
    },
    {
      "nodeId": "39999_RMMO_6392c9225a7c6d3e49b53f19",
      "nodeName": "3.13.1500 iOS_RMMO",
      "path": [
        "RMMO_6392c9225a7c6d3e49b53f19###Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu"
      ],
      "labelName": "release",
      "parentId": [
        "RMMO_6392c9225a7c6d3e49b53f19"
      ],
      "level": 6,
      "releaseEndDate": "2023-07-31T00:00:00.000Z",
      "releaseStartDate": "",
      "releaseState": "Unreleased"
    }
  ];
  const masterData = {
    "kpiList": [
      {
        "id": "64b4ed7acba3c12de16472ec",
        "kpiId": "kpi14",
        "kpiName": "Defect Injection Rate",
        "isDeleted": "False",
        "defaultOrder": 1,
        "kpiUnit": "%",
        "chartType": "line",
        "upperThresholdBG": "red",
        "lowerThresholdBG": "white",
        "showTrend": true,
        "isPositiveTrend": false,
        "calculateMaturity": true,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "200",
        "thresholdValue": 10,
        "kanban": false,
        "groupId": 3,
        "kpiInfo": {
          "definition": "Meausures the Percentage of Defect created and linked to stories in a sprint against the number of stories in the same sprint",
          "formula": [
            {
              "lhs": "DIR for a sprint",
              "operator": "division",
              "operands": [
                "No. of defects tagged to all stories closed in a sprint",
                "Total no. of stories closed in the sprint"
              ]
            }
          ],
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#Defect-Injection-Rate"
              }
            }
          ]
        },
        "aggregationCriteria": "average",
        "maturityRange": [
          "-175",
          "175-125",
          "125-75",
          "75-25",
          "25-"
        ],
        "trendCalculative": false,
        "xaxisLabel": "Sprints",
        "yaxisLabel": "Percentage",
        "videoLink": {
          "id": "6309b8767bee141bb505e733",
          "kpiId": "kpi14",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": true
      },
      {
        "id": "64b4ed7acba3c12de16472ed",
        "kpiId": "kpi82",
        "kpiName": "First Time Pass Rate",
        "isDeleted": "False",
        "defaultOrder": 2,
        "kpiUnit": "%",
        "chartType": "line",
        "upperThresholdBG": "white",
        "lowerThresholdBG": "red",
        "showTrend": true,
        "isPositiveTrend": true,
        "calculateMaturity": true,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "100",
        "thresholdValue": 75,
        "kanban": false,
        "groupId": 1,
        "kpiInfo": {
          "definition": "Measures the percentage of tickets that passed QA with no return transition or any tagging to a specific configured status and no linkage of a defect",
          "formula": [
            {
              "lhs": "FTPR",
              "operator": "division",
              "operands": [
                "No. of issues closed in a sprint with no return transition or any defects tagged",
                "Total no. of issues closed in the sprint"
              ]
            }
          ],
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#First-time-pass-rate"
              }
            }
          ]
        },
        "kpiFilter": "multiSelectDropDown",
        "aggregationCriteria": "average",
        "maturityRange": [
          "-25",
          "25-50",
          "50-75",
          "75-90",
          "90-"
        ],
        "trendCalculative": false,
        "xaxisLabel": "Sprints",
        "yaxisLabel": "Percentage",
        "isAdditionalFilterSupport": true
      },
      {
        "id": "64b4ed7acba3c12de16472ee",
        "kpiId": "kpi111",
        "kpiName": "Defect Density",
        "isDeleted": "False",
        "defaultOrder": 3,
        "kpiUnit": "%",
        "chartType": "line",
        "upperThresholdBG": "red",
        "lowerThresholdBG": "white",
        "showTrend": true,
        "isPositiveTrend": false,
        "calculateMaturity": true,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "500",
        "thresholdValue": 25,
        "kanban": false,
        "groupId": 1,
        "kpiInfo": {
          "definition": "Measures the total number of defect created and linked to stories in a sprint against the size of stories in the same sprint",
          "formula": [
            {
              "lhs": "Defect Density",
              "operator": "division",
              "operands": [
                "No. of defects tagged to all stories closed in a sprint",
                "Total size of stories closed in the sprint"
              ]
            }
          ],
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#Defect-Density"
              }
            }
          ]
        },
        "aggregationCriteria": "average",
        "maturityRange": [
          "-90",
          "90-60",
          "60-25",
          "25-10",
          "10-"
        ],
        "trendCalculative": false,
        "xaxisLabel": "Sprints",
        "yaxisLabel": "Percentage",
        "videoLink": {
          "id": "6309b8767bee141bb505e760",
          "kpiId": "kpi111",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": true
      },
      {
        "id": "64b4ed7acba3c12de16472ef",
        "kpiId": "kpi35",
        "kpiName": "Defect Seepage Rate",
        "isDeleted": "False",
        "defaultOrder": 4,
        "kpiUnit": "%",
        "chartType": "line",
        "upperThresholdBG": "red",
        "lowerThresholdBG": "white",
        "showTrend": true,
        "isPositiveTrend": false,
        "calculateMaturity": true,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "100",
        "thresholdValue": 10,
        "kanban": false,
        "groupId": 1,
        "kpiInfo": {
          "definition": "Measures the percentage of defects leaked from the QA (sprint) testing stage to the UAT/Production stage",
          "formula": [
            {
              "lhs": "DSR for a sprint",
              "operator": "division",
              "operands": [
                "No. of  valid defects reported at a stage (e.g. UAT)",
                " Total no. of defects reported in the current stage and previous stage (UAT & QA)"
              ]
            }
          ],
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#Defect-Seepage-Rate"
              }
            }
          ]
        },
        "aggregationCriteria": "average",
        "maturityRange": [
          "-90",
          "90-75",
          "75-50",
          "50-25",
          "25-"
        ],
        "trendCalculative": false,
        "xaxisLabel": "Sprints",
        "yaxisLabel": "Percentage",
        "videoLink": {
          "id": "6309b8767bee141bb505e73b",
          "kpiId": "kpi35",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": true
      },
      {
        "id": "64b4ed7acba3c12de16472f0",
        "kpiId": "kpi34",
        "kpiName": "Defect Removal Efficiency",
        "isDeleted": "False",
        "defaultOrder": 5,
        "kpiUnit": "%",
        "chartType": "line",
        "upperThresholdBG": "white",
        "lowerThresholdBG": "red",
        "showTrend": true,
        "isPositiveTrend": true,
        "calculateMaturity": true,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "100",
        "thresholdValue": 90,
        "kanban": false,
        "groupId": 3,
        "kpiInfo": {
          "definition": "Measure of percentage of defects closed against the total count tagged to the iteration",
          "formula": [
            {
              "lhs": "DRE for a sprint",
              "operator": "division",
              "operands": [
                "No. of defects in the iteration that are fixed",
                "Total no. of defects in an iteration"
              ]
            }
          ],
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#Defect-Removal-Efficiency"
              }
            }
          ]
        },
        "aggregationCriteria": "average",
        "maturityRange": [
          "-25",
          "25-50",
          "50-75",
          "75-90",
          "90-"
        ],
        "trendCalculative": false,
        "xaxisLabel": "Sprints",
        "yaxisLabel": "Percentage",
        "videoLink": {
          "id": "6309b8767bee141bb505e73a",
          "kpiId": "kpi34",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": true
      },
      {
        "id": "64b4ed7acba3c12de16472f1",
        "kpiId": "kpi37",
        "kpiName": "Defect Rejection Rate",
        "isDeleted": "False",
        "defaultOrder": 6,
        "kpiUnit": "%",
        "chartType": "line",
        "upperThresholdBG": "red",
        "lowerThresholdBG": "white",
        "showTrend": true,
        "isPositiveTrend": false,
        "calculateMaturity": true,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "100",
        "thresholdValue": 10,
        "kanban": false,
        "groupId": 3,
        "kpiInfo": {
          "definition": "Measures the percentage of defect rejection  based on status or resolution of the defect",
          "formula": [
            {
              "lhs": "DRR for a sprint",
              "operator": "division",
              "operands": [
                "No. of defects rejected in a sprint",
                "Total no. of defects Closed in a sprint"
              ]
            }
          ],
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#Defect-Rejection-Rate"
              }
            }
          ]
        },
        "aggregationCriteria": "average",
        "maturityRange": [
          "-75",
          "75-50",
          "50-30",
          "30-10",
          "10-"
        ],
        "trendCalculative": false,
        "xaxisLabel": "Sprints",
        "yaxisLabel": "Percentage",
        "videoLink": {
          "id": "6309b8767bee141bb505e73d",
          "kpiId": "kpi37",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": true
      },
      {
        "id": "64b4ed7acba3c12de16472f2",
        "kpiId": "kpi28",
        "kpiName": "Defect Count By Priority",
        "isDeleted": "False",
        "defaultOrder": 7,
        "kpiUnit": "Number",
        "chartType": "line",
        "upperThresholdBG": "red",
        "lowerThresholdBG": "white",
        "showTrend": true,
        "isPositiveTrend": false,
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "90",
        "thresholdValue": 55,
        "kanban": false,
        "groupId": 1,
        "kpiInfo": {
          "definition": "Measures the number of defects grouped by priority in an iteration",
          "formula": [
            {
              "lhs": "Defect Count By Priority=No. of defects linked to stories grouped by priority"
            }
          ],
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#Defect-Count-by-Priority"
              }
            }
          ]
        },
        "kpiFilter": "multiSelectDropDown",
        "aggregationCriteria": "sum",
        "trendCalculative": false,
        "xaxisLabel": "Sprints",
        "yaxisLabel": "Count",
        "videoLink": {
          "id": "6309b8767bee141bb505e739",
          "kpiId": "kpi28",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": true
      },
      {
        "id": "64b4ed7acba3c12de16472f3",
        "kpiId": "kpi36",
        "kpiName": "Defect Count By RCA",
        "isDeleted": "False",
        "defaultOrder": 8,
        "kpiUnit": "Number",
        "chartType": "line",
        "upperThresholdBG": "red",
        "lowerThresholdBG": "white",
        "showTrend": true,
        "isPositiveTrend": false,
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "100",
        "thresholdValue": 55,
        "kanban": false,
        "groupId": 3,
        "kpiInfo": {
          "definition": "Measures the number of defects grouped by root cause in an iteration",
          "formula": [
            {
              "lhs": "Defect Count By RCA = No. of defects linked to stories grouped by Root Cause"
            }
          ],
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#Defect-Count-By-RCA"
              }
            }
          ]
        },
        "kpiFilter": "multiSelectDropDown",
        "aggregationCriteria": "sum",
        "trendCalculative": false,
        "xaxisLabel": "Sprints",
        "yaxisLabel": "Count",
        "videoLink": {
          "id": "6309b8767bee141bb505e73c",
          "kpiId": "kpi36",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": true
      },
      {
        "id": "64b4ed7acba3c12de16472f4",
        "kpiId": "kpi126",
        "kpiName": "Created vs Resolved defects",
        "isDeleted": "False",
        "defaultOrder": 9,
        "kpiUnit": "Number",
        "chartType": "grouped_column_plus_line",
        "upperThresholdBG": "white",
        "lowerThresholdBG": "red",
        "showTrend": true,
        "isPositiveTrend": true,
        "lineLegend": "Resolved Defects",
        "barLegend": "Created Defects",
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "300",
        "thresholdValue": 0,
        "kanban": false,
        "groupId": 1,
        "kpiInfo": {
          "definition": "Comparative view of number of defects created and number of defects closed in an iteration.",
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#Created-vs-Resolved"
              }
            }
          ]
        },
        "kpiFilter": "radioButton",
        "aggregationCriteria": "sum",
        "trendCalculation": [
          {
            "type": "Upwards",
            "lhs": "value",
            "rhs": "lineValue",
            "operator": "<"
          },
          {
            "type": "Upwards",
            "lhs": "value",
            "rhs": "lineValue",
            "operator": "="
          },
          {
            "type": "Downwards",
            "lhs": "value",
            "rhs": "lineValue",
            "operator": ">"
          }
        ],
        "trendCalculative": true,
        "xaxisLabel": "Sprints",
        "yaxisLabel": "Count",
        "isAdditionalFilterSupport": true
      },
      {
        "id": "64b4ed7acba3c12de16472f5",
        "kpiId": "kpi42",
        "kpiName": "Regression Automation Coverage",
        "isDeleted": "False",
        "defaultOrder": 10,
        "kpiUnit": "%",
        "chartType": "line",
        "upperThresholdBG": "white",
        "lowerThresholdBG": "red",
        "showTrend": true,
        "isPositiveTrend": true,
        "calculateMaturity": true,
        "hideOverallFilter": false,
        "kpiSource": "Zypher",
        "maxValue": "100",
        "thresholdValue": 60,
        "kanban": false,
        "groupId": 1,
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
        "aggregationCriteria": "average",
        "maturityRange": [
          "-20",
          "20-40",
          "40-60",
          "60-80",
          "80-"
        ],
        "trendCalculative": false,
        "xaxisLabel": "Sprints",
        "yaxisLabel": "Percentage",
        "videoLink": {
          "id": "6309b8767bee141bb505e741",
          "kpiId": "kpi42",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de16472f6",
        "kpiId": "kpi16",
        "kpiName": "In-Sprint Automation Coverage",
        "isDeleted": "False",
        "defaultOrder": 11,
        "kpiUnit": "%",
        "chartType": "line",
        "upperThresholdBG": "white",
        "lowerThresholdBG": "red",
        "showTrend": true,
        "isPositiveTrend": true,
        "calculateMaturity": true,
        "hideOverallFilter": false,
        "kpiSource": "Zypher",
        "maxValue": "100",
        "thresholdValue": 80,
        "kanban": false,
        "groupId": 1,
        "kpiInfo": {
          "definition": "Measures the progress of automation of test cases created within the Sprint",
          "formula": [
            {
              "lhs": "In-Sprint Automation Coverage ",
              "operator": "division",
              "operands": [
                "No. of in-sprint test cases automated",
                "Total no. of in-sprint test cases created"
              ]
            }
          ],
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#In-Sprint-Automation-Coverage"
              }
            }
          ]
        },
        "aggregationCriteria": "average",
        "maturityRange": [
          "-20",
          "20-40",
          "40-60",
          "60-80",
          "80-"
        ],
        "trendCalculative": false,
        "xaxisLabel": "Sprints",
        "yaxisLabel": "Percentage",
        "videoLink": {
          "id": "6309b8767bee141bb505e735",
          "kpiId": "kpi16",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de16472f7",
        "kpiId": "kpi17",
        "kpiName": "Unit Test Coverage",
        "isDeleted": "False",
        "defaultOrder": 12,
        "kpiUnit": "%",
        "chartType": "line",
        "upperThresholdBG": "white",
        "lowerThresholdBG": "red",
        "showTrend": true,
        "isPositiveTrend": true,
        "calculateMaturity": true,
        "hideOverallFilter": false,
        "kpiSource": "Sonar",
        "maxValue": "100",
        "thresholdValue": 55,
        "kanban": false,
        "groupId": 1,
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
        "kpiFilter": "multiSelectDropDown",
        "aggregationCriteria": "average",
        "maturityRange": [
          "-20",
          "20-40",
          "40-60",
          "60-80",
          "80-"
        ],
        "trendCalculative": false,
        "xaxisLabel": "Weeks",
        "yaxisLabel": "Percentage",
        "videoLink": {
          "id": "6309b8767bee141bb505e736",
          "kpiId": "kpi17",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de16472f8",
        "kpiId": "kpi38",
        "kpiName": "Sonar Violations",
        "isDeleted": "False",
        "defaultOrder": 13,
        "kpiUnit": "Number",
        "chartType": "line",
        "upperThresholdBG": "red",
        "lowerThresholdBG": "white",
        "showTrend": true,
        "isPositiveTrend": false,
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Sonar",
        "maxValue": "",
        "thresholdValue": 55,
        "kanban": false,
        "groupId": 1,
        "kpiInfo": {
          "definition": "Measures the count of issues that voilates the set of coding rules, defined through the associated Quality profile for each programming language in the project.",
          "formula": [
            {
              "lhs": "Issues are categorized in 3 types: Bug, Vulnerability and Code Smells"
            }
          ],
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#Sonar-Violations"
              }
            }
          ]
        },
        "kpiFilter": "multiSelectDropDown",
        "aggregationCriteria": "sum",
        "trendCalculative": false,
        "xaxisLabel": "Weeks",
        "yaxisLabel": "Count",
        "videoLink": {
          "id": "6309b8767bee141bb505e73e",
          "kpiId": "kpi38",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de16472f9",
        "kpiId": "kpi27",
        "kpiName": "Sonar Tech Debt",
        "isDeleted": "False",
        "defaultOrder": 14,
        "kpiUnit": "Days",
        "chartType": "line",
        "upperThresholdBG": "red",
        "lowerThresholdBG": "white",
        "showTrend": true,
        "isPositiveTrend": false,
        "calculateMaturity": true,
        "hideOverallFilter": true,
        "kpiSource": "Sonar",
        "maxValue": "90",
        "thresholdValue": 55,
        "kanban": false,
        "groupId": 1,
        "kpiInfo": {
          "definition": "Time Estimate required to fix all Issues/code smells reported in Sonar code analysis.",
          "formula": [
            {
              "lhs": "It is calculated as effort to fix all Code Smells. The effort is calculated in minutes and converted to days by assuming 1 Day =8 Hours."
            }
          ],
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#Sonar-Tech-Debt"
              }
            }
          ]
        },
        "kpiFilter": "dropDown",
        "aggregationCriteria": "sum",
        "maturityRange": [
          "-100",
          "100-50",
          "50-30",
          "30-10",
          "10-"
        ],
        "trendCalculative": false,
        "xaxisLabel": "Weeks",
        "yaxisLabel": "Days",
        "videoLink": {
          "id": "6309b8767bee141bb505e738",
          "kpiId": "kpi27",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de16472fb",
        "kpiId": "kpi70",
        "kpiName": "Test Execution and pass percentage",
        "isDeleted": "False",
        "defaultOrder": 16,
        "kpiUnit": "%",
        "chartType": "grouped_column_plus_line",
        "upperThresholdBG": "white",
        "lowerThresholdBG": "red",
        "showTrend": true,
        "isPositiveTrend": true,
        "lineLegend": "Passed",
        "barLegend": "Executed",
        "calculateMaturity": true,
        "hideOverallFilter": false,
        "kpiSource": "Zypher",
        "maxValue": "100",
        "thresholdValue": 80,
        "kanban": false,
        "groupId": 1,
        "kpiInfo": {
          "definition": "Measures the percentage of test cases that have been executed & and the test that have passed.",
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#Test-Execution-and-pass-percentage"
              }
            }
          ]
        },
        "aggregationCriteria": "average",
        "maturityRange": [
          "-20",
          "20-40",
          "40-60",
          "60-80",
          "80-"
        ],
        "trendCalculative": false,
        "xaxisLabel": "Sprints",
        "yaxisLabel": "Percentage",
        "videoLink": {
          "id": "6309b8767bee141bb505e759",
          "kpiId": "kpi70",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de16472fc",
        "kpiId": "kpi40",
        "kpiName": "Issue Count",
        "isDeleted": "False",
        "defaultOrder": 17,
        "kpiUnit": "",
        "chartType": "line",
        "upperThresholdBG": "white",
        "lowerThresholdBG": "red",
        "showTrend": false,
        "isPositiveTrend": true,
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "",
        "thresholdValue": 20,
        "kanban": false,
        "groupId": 5,
        "kpiInfo": {
          "definition": "Number of Issues assigned in a sprint.",
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/26935328/Scrum+SPEED+KPIs#Issue-Count"
              }
            }
          ]
        },
        "kpiFilter": "radioButton",
        "aggregationCriteria": "sum",
        "trendCalculative": false,
        "xaxisLabel": "Sprints",
        "yaxisLabel": "Count",
        "videoLink": {
          "id": "6309b8767bee141bb505e740",
          "kpiId": "kpi40",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": true
      },
      {
        "id": "64b4ed7acba3c12de16472fd",
        "kpiId": "kpi72",
        "kpiName": "Commitment Reliability",
        "isDeleted": "False",
        "defaultOrder": 18,
        "kpiUnit": "%",
        "chartType": "line",
        "upperThresholdBG": "white",
        "lowerThresholdBG": "red",
        "showTrend": true,
        "isPositiveTrend": true,
        "calculateMaturity": true,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "200",
        "thresholdValue": 85,
        "kanban": false,
        "groupId": 2,
        "kpiInfo": {
          "definition": "Measures the percentage of work completed at the end of a iteration in comparison to the initial scope and the final scope",
          "formula": [
            {
              "lhs": "Commitment reliability",
              "operator": "division",
              "operands": [
                "No. of issues or Size of issues completed",
                "No. of issues or Size of issues committed"
              ]
            }
          ],
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/26935328/Scrum+SPEED+KPIs#Commitment-Reliability"
              }
            }
          ]
        },
        "kpiFilter": "dropDown",
        "aggregationCriteria": "average",
        "maturityRange": [
          "-40",
          "40-60",
          "60-75",
          "75-90",
          "90-"
        ],
        "trendCalculative": false,
        "xaxisLabel": "Sprints",
        "yaxisLabel": "Percentage",
        "isAdditionalFilterSupport": true
      },
      {
        "id": "64b4ed7acba3c12de16472fe",
        "kpiId": "kpi5",
        "kpiName": "Sprint Predictability",
        "isDeleted": "False",
        "defaultOrder": 19,
        "kpiInAggregatedFeed": "True",
        "kpiOnDashboard": [
          "Aggregated"
        ],
        "kpiUnit": "%",
        "chartType": "line",
        "upperThresholdBG": "white",
        "lowerThresholdBG": "red",
        "showTrend": false,
        "isPositiveTrend": true,
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "10",
        "thresholdValue": 0,
        "kanban": false,
        "groupId": 2,
        "kpiInfo": {
          "definition": "Measures the percentage the iteration velocity against the average velocity of last 3 iteration.",
          "formula": [
            {
              "lhs": "Sprint Predictability for a sprint",
              "operator": "division",
              "operands": [
                "sprint velocity of the targeted sprint.",
                "average sprint velocity of previous 3 sprints"
              ]
            }
          ],
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/26935328/Scrum+SPEED+KPIs#Sprint-Predictability"
              }
            }
          ]
        },
        "aggregationCriteria": "average",
        "trendCalculative": false,
        "xaxisLabel": "Sprints",
        "yaxisLabel": "Percentage",
        "videoLink": {
          "id": "6309b8767bee141bb505e730",
          "kpiId": "kpi5",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": true
      },
      {
        "id": "64b4ed7acba3c12de16472ff",
        "kpiId": "kpi39",
        "kpiName": "Sprint Velocity",
        "isDeleted": "False",
        "defaultOrder": 20,
        "kpiUnit": "SP",
        "chartType": "grouped_column_plus_line",
        "upperThresholdBG": "white",
        "lowerThresholdBG": "red",
        "showTrend": false,
        "isPositiveTrend": true,
        "lineLegend": "Sprint Velocity",
        "barLegend": "Last 5 Sprints Average",
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "300",
        "thresholdValue": 40,
        "kanban": false,
        "groupId": 2,
        "kpiInfo": {
          "definition": "Measures the rate of delivery across Sprints. Average velocity is calculated for the latest 5 sprints",
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/26935328/Scrum+SPEED+KPIs#Sprint-Velocity"
              }
            }
          ]
        },
        "aggregationCriteria": "sum",
        "trendCalculation": [
          {
            "type": "Upwards",
            "lhs": "value",
            "rhs": "lineValue",
            "operator": "<"
          },
          {
            "type": "Upwards",
            "lhs": "value",
            "rhs": "lineValue",
            "operator": "="
          },
          {
            "type": "Downwards",
            "lhs": "value",
            "rhs": "lineValue",
            "operator": ">"
          }
        ],
        "trendCalculative": true,
        "xaxisLabel": "Sprints",
        "yaxisLabel": "Count",
        "videoLink": {
          "id": "6309b8767bee141bb505e73f",
          "kpiId": "kpi39",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": true
      },
      {
        "id": "64b4ed7acba3c12de1647300",
        "kpiId": "kpi46",
        "kpiName": "Sprint Capacity Utilization",
        "isDeleted": "False",
        "defaultOrder": 21,
        "kpiUnit": "Hours",
        "chartType": "grouped_column_plus_line",
        "upperThresholdBG": "white",
        "lowerThresholdBG": "red",
        "showTrend": false,
        "isPositiveTrend": true,
        "lineLegend": "Logged",
        "barLegend": "Estimated",
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "500",
        "thresholdValue": 0,
        "kanban": false,
        "groupId": 5,
        "kpiInfo": {
          "definition": "Measure the outcome of sprint as planned estimate vs actual estimate",
          "details": [
            {
              "type": "paragraph",
              "value": "Estimated Hours: It explains the total hours required to complete Sprint backlog. The capacity is defined in KnowHOW"
            },
            {
              "type": "paragraph",
              "value": "Logged Work: The amount of time team has logged within a Sprint. It is derived as sum of all logged work against issues tagged to a Sprint in Jira"
            },
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/26935328/Scrum+SPEED+KPIs#Sprint-Capacity-Utilization"
              }
            }
          ]
        },
        "aggregationCriteria": "sum",
        "trendCalculative": false,
        "xaxisLabel": "Sprints",
        "yaxisLabel": "Hours",
        "videoLink": {
          "id": "6309b8767bee141bb505e745",
          "kpiId": "kpi46",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de1647301",
        "kpiId": "kpi84",
        "kpiName": "Mean Time To Merge",
        "isDeleted": "False",
        "defaultOrder": 22,
        "kpiCategory": "Developer",
        "kpiUnit": "Hours",
        "chartType": "line",
        "upperThresholdBG": "red",
        "lowerThresholdBG": "white",
        "showTrend": true,
        "isPositiveTrend": false,
        "calculateMaturity": true,
        "hideOverallFilter": true,
        "kpiSource": "BitBucket",
        "maxValue": "10",
        "thresholdValue": 55,
        "kanban": false,
        "groupId": 1,
        "kpiInfo": {
          "definition": "Measures the efficiency of the code review process in a team",
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/26935328/Scrum+SPEED+KPIs#Mean-time-to-merge"
              }
            }
          ]
        },
        "kpiFilter": "dropDown",
        "aggregationCriteria": "average",
        "maturityRange": [
          "-48",
          "48-16",
          "16-8",
          "8-4",
          "4-"
        ],
        "isRepoToolKpi": false,
        "trendCalculative": false,
        "xaxisLabel": "Weeks",
        "yaxisLabel": "Count(Hours)",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de1647302",
        "kpiId": "kpi11",
        "kpiName": "Check-Ins & Merge Requests",
        "isDeleted": "False",
        "defaultOrder": 23,
        "kpiCategory": "Developer",
        "kpiUnit": "MRs",
        "chartType": "grouped_column_plus_line",
        "upperThresholdBG": "white",
        "lowerThresholdBG": "red",
        "showTrend": true,
        "isPositiveTrend": true,
        "lineLegend": "Merge Requests",
        "barLegend": "Commits",
        "calculateMaturity": true,
        "hideOverallFilter": true,
        "kpiSource": "BitBucket",
        "maxValue": "10",
        "thresholdValue": 55,
        "kanban": false,
        "groupId": 1,
        "kpiInfo": {
          "definition": "Comparative view of number of check-ins and number of merge request raised for a period.",
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/26935328/Scrum+SPEED+KPIs#Number-of-Check-ins-&-Merge-requests"
              }
            }
          ]
        },
        "kpiFilter": "dropDown",
        "aggregationCriteria": "average",
        "maturityRange": [
          "-2",
          "2-4",
          "4-8",
          "8-16",
          "16-"
        ],
        "isRepoToolKpi": false,
        "trendCalculative": false,
        "xaxisLabel": "Days",
        "yaxisLabel": "Count",
        "videoLink": {
          "id": "6309b8767bee141bb505e732",
          "kpiId": "kpi11",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de1647303",
        "kpiId": "kpi8",
        "kpiName": "Code Build Time",
        "isDeleted": "False",
        "defaultOrder": 24,
        "kpiUnit": "min",
        "chartType": "line",
        "upperThresholdBG": "red",
        "lowerThresholdBG": "white",
        "showTrend": true,
        "isPositiveTrend": false,
        "calculateMaturity": true,
        "hideOverallFilter": true,
        "kpiSource": "Jenkins",
        "maxValue": "100",
        "thresholdValue": 6,
        "kanban": false,
        "groupId": 1,
        "kpiInfo": {
          "definition": "Measures the time taken for a builds of a given Job.",
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/26935328/Scrum+SPEED+KPIs#Code-Build-Time"
              }
            }
          ]
        },
        "kpiFilter": "dropDown",
        "aggregationCriteria": "average",
        "maturityRange": [
          "-45",
          "45-30",
          "30-15",
          "15-5",
          "5-"
        ],
        "trendCalculative": false,
        "xaxisLabel": "Weeks",
        "yaxisLabel": "Count(Mins)",
        "videoLink": {
          "id": "6309b8767bee141bb505e731",
          "kpiId": "kpi8",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de1647305",
        "kpiId": "kpi73",
        "kpiName": "Release Frequency",
        "isDeleted": "False",
        "defaultOrder": 26,
        "kpiUnit": "",
        "chartType": "line",
        "upperThresholdBG": "white",
        "lowerThresholdBG": "red",
        "showTrend": true,
        "isPositiveTrend": true,
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "300",
        "thresholdValue": 2,
        "kanban": false,
        "groupId": 4,
        "kpiInfo": {
          "definition": "Measures the number of releases done in a month",
          "formula": [
            {
              "lhs": "Release Frequency for a month = Number of fix versions in JIRA for a project that have a release date falling in a particular month"
            }
          ],
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27131959/Scrum+VALUE+KPIs#Release-Frequency"
              }
            }
          ]
        },
        "aggregationCriteria": "sum",
        "trendCalculative": false,
        "xaxisLabel": "Months",
        "yaxisLabel": "Count",
        "videoLink": {
          "id": "6309b8767bee141bb505e75b",
          "kpiId": "kpi73",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de1647306",
        "kpiId": "kpi113",
        "kpiName": "Value delivered (Cost of Delay)",
        "isDeleted": "False",
        "defaultOrder": 27,
        "kpiUnit": "",
        "chartType": "line",
        "upperThresholdBG": "white",
        "lowerThresholdBG": "red",
        "showTrend": true,
        "isPositiveTrend": true,
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "300",
        "thresholdValue": 0,
        "kanban": false,
        "groupId": 4,
        "kpiInfo": {
          "definition": "Cost of delay (CoD) is a indicator of the economic value of completing a feature sooner as opposed to later.",
          "formula": [
            {
              "lhs": "COD for a Epic or a Feature  =  User-Business Value + Time Criticality + Risk Reduction and/or Opportunity Enablement."
            }
          ],
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27131959/Scrum+VALUE+KPIs#Value-delivered-(Cost-of-Delay)"
              }
            }
          ]
        },
        "aggregationCriteria": "sum",
        "trendCalculative": false,
        "xaxisLabel": "Months",
        "yaxisLabel": "Count(Days)",
        "videoLink": {
          "id": "6309b8767bee141bb505e75d",
          "kpiId": "kpi113",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de1647307",
        "kpiId": "kpi55",
        "kpiName": "Ticket Open vs Closed rate by type",
        "defaultOrder": 1,
        "kpiUnit": "Tickets",
        "chartType": "grouped_column_plus_line",
        "showTrend": true,
        "isPositiveTrend": false,
        "lineLegend": "Closed Tickets",
        "barLegend": "Open Tickets",
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "kanban": true,
        "groupId": 1,
        "kpiInfo": {
          "definition": "Ticket open vs closed rate by type gives a comparison of new tickets getting raised vs number of tickets getting closed grouped by issue type during a defined period.",
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/35651585/Kanban+QUALITY+KPIs#Ticket-open-vs-closed-rate-by-type"
              }
            }
          ]
        },
        "kpiFilter": "multiSelectDropDown",
        "aggregationCriteria": "sum",
        "trendCalculation": [
          {
            "type": "Upwards",
            "lhs": "value",
            "rhs": "lineValue",
            "operator": "<"
          },
          {
            "type": "Neutral",
            "lhs": "value",
            "rhs": "lineValue",
            "operator": "="
          },
          {
            "type": "Downwards",
            "lhs": "value",
            "rhs": "lineValue",
            "operator": ">"
          }
        ],
        "trendCalculative": true,
        "xaxisLabel": "Weeks",
        "yaxisLabel": "Count",
        "videoLink": {
          "id": "6309b8767bee141bb505e74d",
          "kpiId": "kpi55",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": true
      },
      {
        "id": "64b4ed7acba3c12de1647308",
        "kpiId": "kpi54",
        "kpiName": "Ticket Open vs Closed rate by Priority",
        "isDeleted": "False",
        "defaultOrder": 2,
        "kpiUnit": "Tickets",
        "chartType": "grouped_column_plus_line",
        "showTrend": true,
        "isPositiveTrend": false,
        "lineLegend": "Closed Tickets",
        "barLegend": "Open Tickets",
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "kanban": true,
        "groupId": 1,
        "kpiInfo": {
          "definition": "Ticket open vs closed rate by priority gives a comparison of new tickets getting raised vs number of tickets getting closed grouped by priority during a defined period.",
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/35651585/Kanban+QUALITY+KPIs#Ticket-Open-vs-Closed-rate-by-Priority"
              }
            }
          ]
        },
        "kpiFilter": "multiSelectDropDown",
        "aggregationCriteria": "sum",
        "trendCalculation": [
          {
            "type": "Upwards",
            "lhs": "value",
            "rhs": "lineValue",
            "operator": "<"
          },
          {
            "type": "Neutral",
            "lhs": "value",
            "rhs": "lineValue",
            "operator": "="
          },
          {
            "type": "Downwards",
            "lhs": "value",
            "rhs": "lineValue",
            "operator": ">"
          }
        ],
        "trendCalculative": true,
        "xaxisLabel": "Weeks",
        "yaxisLabel": "Count",
        "videoLink": {
          "id": "6309b8767bee141bb505e74c",
          "kpiId": "kpi54",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": true
      },
      {
        "id": "64b4ed7acba3c12de1647309",
        "kpiId": "kpi50",
        "kpiName": "Net Open Ticket Count by Priority",
        "isDeleted": "False",
        "defaultOrder": 3,
        "kpiUnit": "Number",
        "chartType": "line",
        "showTrend": true,
        "isPositiveTrend": false,
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "",
        "kanban": true,
        "groupId": 1,
        "kpiInfo": {
          "definition": "Measures of  overall open tickets during a defined period grouped by priority. It considers the gross open and closed count during the period.",
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/35651585/Kanban+QUALITY+KPIs#Net-Open-Ticket-Count-By-Priority"
              }
            }
          ]
        },
        "kpiFilter": "multiSelectDropDown",
        "aggregationCriteria": "sum",
        "trendCalculative": false,
        "xaxisLabel": "Weeks",
        "yaxisLabel": "Count",
        "videoLink": {
          "id": "6309b8767bee141bb505e749",
          "kpiId": "kpi50",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": true
      },
      {
        "id": "64b4ed7acba3c12de164730a",
        "kpiId": "kpi51",
        "kpiName": "Net Open Ticket Count By RCA",
        "isDeleted": "False",
        "defaultOrder": 4,
        "kpiUnit": "Number",
        "chartType": "line",
        "showTrend": true,
        "isPositiveTrend": false,
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "",
        "kanban": true,
        "groupId": 1,
        "kpiInfo": {
          "definition": "Measures of  overall open tickets during a defined period grouped by RCA. It considers the gross open and closed count during the period.",
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/35651585/Kanban+QUALITY+KPIs#Net-Open-Ticket-Count-By-RCA-(Ticket-Count-By-RCA)"
              }
            }
          ]
        },
        "kpiFilter": "multiSelectDropDown",
        "aggregationCriteria": "sum",
        "trendCalculative": false,
        "xaxisLabel": "Weeks",
        "yaxisLabel": "Count",
        "videoLink": {
          "id": "6309b8767bee141bb505e74a",
          "kpiId": "kpi51",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": true
      },
      {
        "id": "64b4ed7acba3c12de164730b",
        "kpiId": "kpi48",
        "kpiName": "Net Open Ticket By Status",
        "isDeleted": "False",
        "defaultOrder": 5,
        "kpiUnit": "",
        "chartType": "line",
        "showTrend": true,
        "isPositiveTrend": false,
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "kanban": true,
        "groupId": 2,
        "kpiInfo": {
          "definition": "Measures the overall open tickets during a defined period grouped by Status. It considers the gross open and closed count during the period.",
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/35651585/Kanban+QUALITY+KPIs#Net-Open-Ticket-count-by-Status-(Total-Ticket-Count)"
              }
            }
          ]
        },
        "kpiFilter": "multiSelectDropDown",
        "aggregationCriteria": "sum",
        "trendCalculative": false,
        "xaxisLabel": "Weeks",
        "yaxisLabel": "Count",
        "videoLink": {
          "id": "6309b8767bee141bb505e747",
          "kpiId": "kpi48",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": true
      },
      {
        "id": "64b4ed7acba3c12de164730c",
        "kpiId": "kpi997",
        "kpiName": "Open Ticket Ageing By Priority",
        "isDeleted": "False",
        "defaultOrder": 6,
        "kpiUnit": "Number",
        "chartType": "line",
        "showTrend": false,
        "isPositiveTrend": false,
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "kanban": true,
        "groupId": 2,
        "kpiInfo": {
          "definition": "Measure of all the open tickets based on their ageing, grouped by priority",
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/35651585/Kanban+QUALITY+KPIs#Open-Tickets-Ageing-by-Priority-(Total-Tickets-Aging)"
              }
            }
          ]
        },
        "kpiFilter": "multiSelectDropDown",
        "aggregationCriteria": "sum",
        "trendCalculative": false,
        "xaxisLabel": "Months",
        "yaxisLabel": "Count",
        "isAdditionalFilterSupport": true
      },
      {
        "id": "64b4ed7acba3c12de164730d",
        "kpiId": "kpi63",
        "kpiName": "Regression Automation Coverage",
        "isDeleted": "False",
        "defaultOrder": 7,
        "kpiUnit": "%",
        "chartType": "line",
        "showTrend": true,
        "isPositiveTrend": true,
        "calculateMaturity": true,
        "hideOverallFilter": false,
        "kpiSource": "Zypher",
        "maxValue": "100",
        "kanban": true,
        "groupId": 1,
        "kpiInfo": {
          "definition": "Measures progress of automation of regression test cases",
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
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/35651585/Kanban+QUALITY+KPIs#Regression-automation-Coverage"
              }
            }
          ]
        },
        "aggregationCriteria": "average",
        "maturityRange": [
          "-20",
          "20-40",
          "40-60",
          "60-80",
          "80-"
        ],
        "trendCalculative": false,
        "xaxisLabel": "Weeks",
        "yaxisLabel": "Percentage",
        "videoLink": {
          "id": "6309b8767bee141bb505e754",
          "kpiId": "kpi63",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de164730e",
        "kpiId": "kpi62",
        "kpiName": "Unit Test Coverage",
        "isDeleted": "False",
        "defaultOrder": 8,
        "kpiUnit": "%",
        "chartType": "line",
        "upperThresholdBG": "white",
        "lowerThresholdBG": "red",
        "showTrend": true,
        "isPositiveTrend": true,
        "calculateMaturity": true,
        "hideOverallFilter": true,
        "kpiSource": "Sonar",
        "maxValue": "100",
        "thresholdValue": 55,
        "kanban": true,
        "groupId": 1,
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
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/35651585/Kanban+QUALITY+KPIs#Unit-Test-Coverage"
              }
            }
          ]
        },
        "kpiFilter": "dropDown",
        "aggregationCriteria": "average",
        "maturityRange": [
          "-20",
          "20-40",
          "40-60",
          "60-80",
          "80-"
        ],
        "trendCalculative": false,
        "xaxisLabel": "Weeks",
        "yaxisLabel": "Percentage",
        "videoLink": {
          "id": "6309b8767bee141bb505e753",
          "kpiId": "kpi62",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de164730f",
        "kpiId": "kpi64",
        "kpiName": "Sonar Violations",
        "isDeleted": "False",
        "defaultOrder": 9,
        "kpiUnit": "Number",
        "chartType": "line",
        "upperThresholdBG": "red",
        "lowerThresholdBG": "white",
        "showTrend": true,
        "isPositiveTrend": false,
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Sonar",
        "maxValue": "",
        "thresholdValue": 55,
        "kanban": true,
        "groupId": 1,
        "kpiInfo": {
          "definition": "Measures the count of issues that voilates the set of coding rules, defined through the associated Quality profile for each programming language in the project.",
          "formula": [
            {
              "lhs": "The calculation is done directly in Sonarqube."
            }
          ],
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/35651585/Kanban+QUALITY+KPIs#Sonar-Violations"
              }
            }
          ]
        },
        "kpiFilter": "multiSelectDropDown",
        "aggregationCriteria": "sum",
        "trendCalculative": false,
        "xaxisLabel": "Weeks",
        "yaxisLabel": "Count",
        "videoLink": {
          "id": "6309b8767bee141bb505e755",
          "kpiId": "kpi64",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de1647310",
        "kpiId": "kpi67",
        "kpiName": "Sonar Tech Debt",
        "isDeleted": "False",
        "defaultOrder": 10,
        "kpiUnit": "Days",
        "chartType": "line",
        "upperThresholdBG": "red",
        "lowerThresholdBG": "white",
        "showTrend": true,
        "isPositiveTrend": false,
        "calculateMaturity": true,
        "hideOverallFilter": true,
        "kpiSource": "Sonar",
        "maxValue": "90",
        "thresholdValue": 55,
        "kanban": true,
        "groupId": 1,
        "kpiInfo": {
          "definition": "Time Estimate required to fix all Issues/code smells reported in Sonar code analysis",
          "formula": [
            {
              "lhs": "It is calculated as effort to fix all Code Smells. The effort is calculated in minutes and converted to days by assuming 1 Day =8 Hours"
            }
          ],
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/35651585/Kanban+QUALITY+KPIs#Sonar-Tech-Debt"
              }
            }
          ]
        },
        "kpiFilter": "dropDown",
        "aggregationCriteria": "sum",
        "maturityRange": [
          "-100",
          "100-50",
          "50-30",
          "30-10",
          "10-"
        ],
        "trendCalculative": false,
        "xaxisLabel": "Weeks",
        "yaxisLabel": "Days",
        "videoLink": {
          "id": "6309b8767bee141bb505e758",
          "kpiId": "kpi67",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de1647311",
        "kpiId": "kpi71",
        "kpiName": "Test Execution and pass percentage",
        "isDeleted": "False",
        "defaultOrder": 11,
        "kpiUnit": "%",
        "chartType": "grouped_column_plus_line",
        "showTrend": true,
        "isPositiveTrend": true,
        "lineLegend": "Passed",
        "barLegend": "Executed",
        "calculateMaturity": true,
        "hideOverallFilter": false,
        "kpiSource": "Zypher",
        "maxValue": "100",
        "kanban": true,
        "groupId": 1,
        "kpiInfo": {
          "definition": "Measures the percentage of test cases that have been executed & the percentage that have passed in a defined duration.",
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/35651585/Kanban+QUALITY+KPIs#Test-Execution-and-pass-percentage"
              }
            }
          ]
        },
        "aggregationCriteria": "average",
        "maturityRange": [
          "-20",
          "20-40",
          "40-60",
          "60-80",
          "80-"
        ],
        "trendCalculative": false,
        "xaxisLabel": "Weeks",
        "yaxisLabel": "Percentage",
        "videoLink": {
          "id": "6309b8767bee141bb505e75a",
          "kpiId": "kpi71",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de1647312",
        "kpiId": "kpi49",
        "kpiName": "Ticket Velocity",
        "isDeleted": "False",
        "defaultOrder": 12,
        "kpiUnit": "SP",
        "chartType": "line",
        "showTrend": false,
        "isPositiveTrend": true,
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "300",
        "kanban": true,
        "groupId": 1,
        "kpiInfo": {
          "definition": "Ticket velocity measures the size of tickets (in story points) completed in a defined duration",
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/35717121/Kanban+SPEED+KPIs#Ticket-Velocity"
              }
            }
          ]
        },
        "aggregationCriteria": "sum",
        "trendCalculative": false,
        "xaxisLabel": "Weeks",
        "yaxisLabel": "Story Points",
        "videoLink": {
          "id": "6309b8767bee141bb505e748",
          "kpiId": "kpi49",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": true
      },
      {
        "id": "64b4ed7acba3c12de1647313",
        "kpiId": "kpi58",
        "kpiName": "Team Capacity",
        "isDeleted": "False",
        "defaultOrder": 13,
        "kpiUnit": "Hours",
        "chartType": "line",
        "showTrend": false,
        "isPositiveTrend": true,
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "",
        "kanban": true,
        "groupId": 1,
        "kpiInfo": {
          "definition": "Team Capacity is sum of capacity of all team member measured in hours during a defined period. This is defined/managed by project administration section",
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/35717121/Kanban+SPEED+KPIs#Ticket-Velocity"
              }
            },
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Refer the capacity management guide at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/32473095/Capacity+Management"
              }
            }
          ]
        },
        "aggregationCriteria": "sum",
        "trendCalculative": false,
        "xaxisLabel": "Weeks",
        "yaxisLabel": "Hours",
        "videoLink": {
          "id": "6309b8767bee141bb505e750",
          "kpiId": "kpi58",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de1647314",
        "kpiId": "kpi66",
        "kpiName": "Code Build Time",
        "isDeleted": "False",
        "defaultOrder": 14,
        "kpiUnit": "min",
        "chartType": "line",
        "showTrend": true,
        "isPositiveTrend": false,
        "calculateMaturity": true,
        "hideOverallFilter": true,
        "kpiSource": "Jenkins",
        "maxValue": "100",
        "kanban": true,
        "groupId": 1,
        "kpiInfo": {
          "definition": "Measures the time taken for a build of a given Job.",
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/35717121/Kanban+SPEED+KPIs#Code-Build-Time"
              }
            }
          ]
        },
        "kpiFilter": "dropDown",
        "aggregationCriteria": "average",
        "maturityRange": [
          "-45",
          "45-30",
          "30-15",
          "15-5",
          "5-"
        ],
        "trendCalculative": false,
        "xaxisLabel": "Weeks",
        "yaxisLabel": "Min",
        "videoLink": {
          "id": "6309b8767bee141bb505e757",
          "kpiId": "kpi66",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de1647315",
        "kpiId": "kpi65",
        "kpiName": "Number of Check-ins",
        "isDeleted": "False",
        "defaultOrder": 15,
        "kpiCategory": "Developer",
        "kpiUnit": "check-ins",
        "chartType": "line",
        "showTrend": true,
        "isPositiveTrend": true,
        "calculateMaturity": true,
        "hideOverallFilter": true,
        "kpiSource": "BitBucket",
        "maxValue": "10",
        "thresholdValue": 55,
        "kanban": true,
        "groupId": 1,
        "kpiInfo": {
          "definition": "Measures of the the count of check in in repo for the defined period.",
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/35717121/Kanban+SPEED+KPIs#Number-of-Check-ins"
              }
            }
          ]
        },
        "kpiFilter": "dropDown",
        "aggregationCriteria": "sum",
        "maturityRange": [
          "-2",
          "2-4",
          "4-8",
          "8-16",
          "16-"
        ],
        "isRepoToolKpi": false,
        "trendCalculative": false,
        "xaxisLabel": "Weeks",
        "yaxisLabel": "Count",
        "videoLink": {
          "id": "6309b8767bee141bb505e756",
          "kpiId": "kpi65",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de1647316",
        "kpiId": "kpi53",
        "kpiName": "Lead Time",
        "isDeleted": "False",
        "defaultOrder": 16,
        "kpiInAggregatedFeed": "True",
        "kpiOnDashboard": [
          "Aggregated"
        ],
        "kpiBaseLine": "0",
        "kpiUnit": "Days",
        "chartType": "table",
        "showTrend": false,
        "isPositiveTrend": false,
        "calculateMaturity": true,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "kanban": true,
        "groupId": 3,
        "kpiInfo": {
          "definition": "Measures  Total time between a request was made and  all work on this item is completed and the request was delivered .",
          "formula": [
            {
              "lhs": "It is calculated as the sum following"
            }
          ],
          "details": [
            {
              "type": "paragraph",
              "value": "Open to Triage: Time taken from ticket creation to it being refined & prioritized for development."
            },
            {
              "type": "paragraph",
              "value": "Triage to Complete: Time taken from start of work on a ticket to it being completed by team."
            },
            {
              "type": "paragraph",
              "value": "Complete to Live: Time taken between ticket completion to it going live."
            },
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/35717121/Kanban+SPEED+KPIs#Lead-Time"
              }
            }
          ]
        },
        "kpiFilter": "radioButton",
        "aggregationCriteria": "average",
        "maturityRange": [
          "-60",
          "60-45",
          "45-30",
          "30-10",
          "10-"
        ],
        "maturityLevel": [
          {
            "level": "LeadTime",
            "range": [
              "-60",
              "60-45",
              "45-30",
              "30-10",
              "10-"
            ]
          },
          {
            "level": "Open-Triage",
            "range": [
              "-30",
              "30-20",
              "20-10",
              "10-5",
              "5-"
            ]
          },
          {
            "level": "Triage-Complete",
            "range": [
              "-20",
              "20-10",
              "10-7",
              "7-3",
              "3-"
            ]
          },
          {
            "level": "Complete-Live",
            "range": [
              "-30",
              "30-15",
              "15-5",
              "5-2",
              "2-"
            ]
          }
        ],
        "trendCalculative": false,
        "xaxisLabel": "",
        "yaxisLabel": "",
        "videoLink": {
          "id": "6309b8767bee141bb505e74b",
          "kpiId": "kpi53",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de1647317",
        "kpiId": "kpi74",
        "kpiName": "Release Frequency",
        "isDeleted": "False",
        "defaultOrder": 17,
        "kpiUnit": "",
        "chartType": "line",
        "showTrend": true,
        "isPositiveTrend": true,
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "300",
        "kanban": true,
        "groupId": 4,
        "kpiInfo": {
          "definition": "Measures the number of releases done in a month",
          "formula": [
            {
              "lhs": "Release Frequency for a month",
              "rhs": "Number of fix versions in JIRA for a project that have a release date falling in a particular month"
            }
          ],
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/35651597/Kanban+VALUE+KPIs#Release-Frequency"
              }
            }
          ]
        },
        "aggregationCriteria": "sum",
        "trendCalculative": false,
        "xaxisLabel": "Months",
        "yaxisLabel": "Count",
        "videoLink": {
          "id": "6309b8767bee141bb505e75c",
          "kpiId": "kpi74",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de1647318",
        "kpiId": "kpi114",
        "kpiName": "Value delivered (Cost of Delay)",
        "isDeleted": "False",
        "defaultOrder": 18,
        "kpiUnit": "",
        "chartType": "line",
        "showTrend": true,
        "isPositiveTrend": true,
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "300",
        "kanban": true,
        "groupId": 4,
        "kpiInfo": {
          "definition": "Cost of delay (CoD) is a indicator of the economic value of completing a feature sooner as opposed to later.",
          "formula": [
            {
              "lhs": "COD for a Epic or a Feature  =  User-Business Value + Time Criticality + Risk Reduction and/or Opportunity Enablement."
            }
          ],
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/35651597/Kanban+VALUE+KPIs#Value-delivered-(Cost-of-Delay)"
              }
            }
          ]
        },
        "aggregationCriteria": "sum",
        "trendCalculative": false,
        "xaxisLabel": "Weeks",
        "yaxisLabel": "Days",
        "videoLink": {
          "id": "6309b8767bee141bb505e75e",
          "kpiId": "kpi114",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de1647319",
        "kpiId": "kpi121",
        "kpiName": "Capacity",
        "isDeleted": "False",
        "defaultOrder": 0,
        "kpiCategory": "Iteration",
        "kpiSubCategory": "Iteration Review",
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
        "xaxisLabel": "",
        "yaxisLabel": "",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de164731a",
        "kpiId": "kpi119",
        "kpiName": "Work Remaining",
        "isDeleted": "False",
        "defaultOrder": 4,
        "kpiCategory": "Iteration",
        "kpiSubCategory": "Iteration Review",
        "kpiUnit": "Hours",
        "showTrend": false,
        "isPositiveTrend": true,
        "boxType": "3_column",
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "",
        "kanban": false,
        "groupId": 8,
        "kpiInfo": {
          "definition": "Remaining work in the iteration in terms count of issues & sum of story estimates. Sum of remaining hours required to complete pending work.",
          "details": [
            {
              "type": "paragraph",
              "value": "In the list of Issues you can see potential delay & completion date for each issues."
            },
            {
              "type": "paragraph",
              "value": "In addition, it also shows the potential delay because of all pending stories. Potential delay and predicted completion date can be seen for each issue as well."
            },
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/2883631/Iteration+Dashboard#Work-Remaining"
              }
            }
          ]
        },
        "kpiFilter": "multiSelectDropDown",
        "trendCalculative": false,
        "xaxisLabel": "",
        "yaxisLabel": "",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de164731b",
        "kpiId": "kpi128",
        "kpiName": "Planned Work Status",
        "isDeleted": "False",
        "defaultOrder": 2,
        "kpiCategory": "Iteration",
        "kpiSubCategory": "Iteration Review",
        "kpiUnit": "Count",
        "showTrend": false,
        "isPositiveTrend": true,
        "boxType": "3_column",
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "",
        "kanban": false,
        "groupId": 8,
        "kpiInfo": {
          "definition": "It shows count of the issues having a due date which are planned to be completed until today and how many of these issues have actually been completed. It also depicts the delay in completing the planned issues in terms of days.",
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/2883631/Iteration+Dashboard#Planned-Work-Status"
              }
            }
          ]
        },
        "kpiFilter": "multiSelectDropDown",
        "trendCalculative": false,
        "xaxisLabel": "",
        "yaxisLabel": "",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de164731c",
        "kpiId": "kpi75",
        "kpiName": "Estimate vs Actual",
        "isDeleted": "False",
        "defaultOrder": 22,
        "kpiCategory": "Iteration",
        "kpiSubCategory": "Iteration Review",
        "kpiUnit": "Hours",
        "showTrend": false,
        "isPositiveTrend": true,
        "boxType": "2_column",
        "calculateMaturity": true,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "",
        "kanban": false,
        "groupId": 8,
        "kpiInfo": {
          "definition": "Estimate vs Actual gives a comparative view of the sum of estimated hours of all issues in an iteration as against the total time spent on these issues.",
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/2883631/Iteration+Dashboard#Estimate-vs-Actual"
              }
            }
          ]
        },
        "kpiFilter": "multiSelectDropDown",
        "trendCalculative": false,
        "xaxisLabel": "",
        "yaxisLabel": "",
        "videoLink": {
          "id": "6309b8767bee141bb505e761",
          "kpiId": "kpi75",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de164731d",
        "kpiId": "kpi123",
        "kpiName": "Issues likely to Spill",
        "isDeleted": "False",
        "defaultOrder": 7,
        "kpiCategory": "Iteration",
        "kpiSubCategory": "Iteration Review",
        "kpiUnit": "Count",
        "showTrend": false,
        "isPositiveTrend": true,
        "boxType": "3_column",
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "",
        "kanban": false,
        "groupId": 8,
        "kpiInfo": {
          "definition": "It gives intelligence to the team about number of issues that could potentially not get completed during the iteration. Issues which have a Predicted Completion date > Sprint end date are considered.",
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/2883631/Iteration+Dashboard#Issues-likely-to-spill"
              }
            }
          ]
        },
        "kpiFilter": "multiSelectDropDown",
        "trendCalculative": false,
        "xaxisLabel": "",
        "yaxisLabel": "",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de164731e",
        "kpiId": "kpi122",
        "kpiName": "Closure Possible Today",
        "isDeleted": "False",
        "defaultOrder": 6,
        "kpiCategory": "Iteration",
        "kpiSubCategory": "Iteration Review",
        "kpiUnit": "Story Point",
        "showTrend": false,
        "isPositiveTrend": true,
        "boxType": "2_column",
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "",
        "kanban": false,
        "groupId": 8,
        "kpiInfo": {
          "definition": "It gives intelligence to users about how many issues can be completed on a particular day of an iteration. An issue is included as a possible closure based on the calculation of Predicted completion date.",
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/2883631/Iteration+Dashboard#Closures-possible-today"
              }
            }
          ]
        },
        "kpiFilter": "multiSelectDropDown",
        "trendCalculative": false,
        "xaxisLabel": "",
        "yaxisLabel": "",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de164731f",
        "kpiId": "kpi120",
        "kpiName": "Iteration Commitment",
        "isDeleted": "False",
        "defaultOrder": 1,
        "kpiCategory": "Iteration",
        "kpiSubCategory": "Iteration Review",
        "kpiUnit": "Count",
        "showTrend": false,
        "isPositiveTrend": true,
        "boxType": "3_column",
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "",
        "kanban": false,
        "groupId": 8,
        "kpiInfo": {
          "definition": "Iteration commitment shows in terms of issue count and story points the Initial commitment (issues tagged when the iteration starts), Scope added and Scope removed.",
          "details": [
            {
              "type": "paragraph",
              "value": "Overall commitment= Initial Commitment + Scope added - Scope removed"
            }
          ]
        },
        "kpiFilter": "multiSelectDropDown",
        "kpiWidth": 100,
        "trendCalculative": false,
        "xaxisLabel": "",
        "yaxisLabel": "",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de1647320",
        "kpiId": "kpi124",
        "kpiName": "Estimation Hygiene",
        "isDeleted": "False",
        "defaultOrder": 21,
        "kpiCategory": "Iteration",
        "kpiSubCategory": "Iteration Review",
        "kpiUnit": "Count",
        "showTrend": false,
        "isPositiveTrend": true,
        "boxType": "2_column_big",
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "",
        "kanban": false,
        "groupId": 8,
        "kpiInfo": {
          "definition": "It shows the count of issues which do not have estimates and count of In progress issues without any work logs."
        },
        "kpiFilter": "multiSelectDropDown",
        "trendCalculative": false,
        "xaxisLabel": "",
        "yaxisLabel": "",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de1647321",
        "kpiId": "kpi132",
        "kpiName": "Defect Count by RCA",
        "isDeleted": "False",
        "defaultOrder": 14,
        "kpiCategory": "Iteration",
        "kpiSubCategory": "Iteration Review",
        "kpiUnit": "Count",
        "chartType": "pieChart",
        "showTrend": false,
        "isPositiveTrend": true,
        "boxType": "chart",
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "",
        "kanban": false,
        "groupId": 8,
        "kpiInfo": {
          "definition": "It shows the breakup of all defects within an iteration by root cause identified."
        },
        "kpiFilter": "radioButton",
        "trendCalculative": false,
        "xaxisLabel": "",
        "yaxisLabel": "",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de1647322",
        "kpiId": "kpi133",
        "kpiName": "Quality Status",
        "isDeleted": "False",
        "defaultOrder": 12,
        "kpiCategory": "Iteration",
        "kpiSubCategory": "Iteration Review",
        "kpiUnit": "",
        "showTrend": false,
        "isPositiveTrend": true,
        "boxType": "3_column",
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "",
        "kanban": false,
        "groupId": 8,
        "kpiInfo": {
          "definition": "It showcases the count of defect linked to stories and count that are not linked to any story. The defect injection rate and defect density are shown to give a wholistic view of quality of ongoing iteration",
          "details": [
            {
              "type": "paragraph",
              "value": "*Any defect created during the iteration duration but is not added to the iteration is not considered"
            }
          ]
        },
        "kpiFilter": "",
        "trendCalculative": false,
        "xaxisLabel": "",
        "yaxisLabel": "",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de1647323",
        "kpiId": "kpi134",
        "kpiName": "Unplanned Work Status",
        "isDeleted": "False",
        "defaultOrder": 5,
        "kpiCategory": "Iteration",
        "kpiSubCategory": "Iteration Review",
        "kpiUnit": "Count",
        "showTrend": false,
        "isPositiveTrend": true,
        "boxType": "2_column",
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "",
        "kanban": false,
        "groupId": 8,
        "kpiInfo": {
          "definition": "It shows count of the issues which do not have a due date. It also shows the completed count amongst the unplanned issues."
        },
        "kpiFilter": "multiSelectDropDown",
        "trendCalculative": false,
        "xaxisLabel": "",
        "yaxisLabel": "",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de1647324",
        "kpiId": "kpi125",
        "kpiName": "Iteration Burnup",
        "isDeleted": "False",
        "defaultOrder": 8,
        "kpiCategory": "Iteration",
        "kpiSubCategory": "Iteration Progress",
        "kpiUnit": "Count",
        "chartType": "CumulativeMultilineChart",
        "showTrend": false,
        "isPositiveTrend": true,
        "boxType": "chart",
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "",
        "kanban": false,
        "groupId": 8,
        "kpiInfo": {
          "definition": "Iteration Burnup KPI shows the cumulative actual progress against the overall scope of the iteration on a daily basis. For teams putting due dates at the beginning of iteration, the graph additionally shows the actual progress in comparison to the planning done and also predicts the probable progress for the remaining days of the iteration."
        },
        "kpiFilter": "multiselectdropdown",
        "kpiWidth": 100,
        "trendCalculative": false,
        "xaxisLabel": "Days",
        "yaxisLabel": "Count",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de1647325",
        "kpiId": "kpi131",
        "kpiName": "Wastage",
        "isDeleted": "False",
        "defaultOrder": 9,
        "kpiCategory": "Iteration",
        "kpiSubCategory": "Iteration Review",
        "kpiUnit": "Hours",
        "showTrend": false,
        "isPositiveTrend": true,
        "boxType": "3_column",
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
              "value": "Wastage = Blocked time + Wait time"
            },
            {
              "type": "paragraph",
              "value": "Blocked time - Total time when any issue is in a status like Blocked as defined in the configuration or if any issue is flagged."
            },
            {
              "type": "paragraph",
              "value": "Wait time : Total time when any issue is in status similar to Ready for testing, ready for deployment as defined in the configuration etc."
            }
          ]
        },
        "kpiFilter": "multiSelectDropDown",
        "trendCalculative": false,
        "xaxisLabel": "",
        "yaxisLabel": "",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de1647326",
        "kpiId": "kpi135",
        "kpiName": "First Time Pass Rate",
        "isDeleted": "False",
        "defaultOrder": 11,
        "kpiCategory": "Iteration",
        "kpiSubCategory": "Iteration Review",
        "kpiUnit": "Hours",
        "showTrend": false,
        "isPositiveTrend": true,
        "boxType": "3_column",
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "",
        "kanban": false,
        "groupId": 8,
        "kpiInfo": {
          "definition": "Percentage of tickets that passed QA with no return transition or any tagging to a specific configured status and no linkage of a defect."
        },
        "kpiFilter": "multiSelectDropDown",
        "trendCalculative": false,
        "xaxisLabel": "",
        "yaxisLabel": "",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de1647327",
        "kpiId": "kpi129",
        "kpiName": "Issues Without Story Link",
        "isDeleted": "False",
        "defaultOrder": 3,
        "kpiCategory": "Backlog",
        "kpiSubCategory": "Backlog Health",
        "kpiUnit": "Hours",
        "showTrend": false,
        "isPositiveTrend": true,
        "boxType": "3_column",
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "",
        "kanban": false,
        "groupId": 11,
        "kpiInfo": {
          "formula": [
            {
              "lhs": "Testcases without story link = Total non-regression test cases without story link"
            },
            {
              "lhs": "Defect Count Without Story Link= Total defects without Story link"
            }
          ]
        },
        "kpiFilter": "",
        "trendCalculative": false,
        "xaxisLabel": "",
        "yaxisLabel": "",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de1647328",
        "kpiId": "kpi127",
        "kpiName": "Production Defects Ageing",
        "isDeleted": "False",
        "defaultOrder": 2,
        "kpiCategory": "Backlog",
        "kpiSubCategory": "Backlog Health",
        "kpiUnit": "Number",
        "chartType": "line",
        "upperThresholdBG": "red",
        "lowerThresholdBG": "white",
        "showTrend": false,
        "isPositiveTrend": false,
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "thresholdValue": 0,
        "kanban": false,
        "groupId": 10,
        "kpiInfo": {
          "definition": "It groups all the open production defects based on their ageing in the backlog."
        },
        "kpiFilter": "multiSelectDropDown",
        "aggregationCriteria": "sum",
        "trendCalculative": false,
        "xaxisLabel": "Months",
        "yaxisLabel": "Count",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de1647329",
        "kpiId": "kpi139",
        "kpiName": "Refinement Rejection Rate",
        "isDeleted": "False",
        "defaultOrder": 6,
        "kpiCategory": "Backlog",
        "kpiSubCategory": "Backlog Health",
        "kpiUnit": "%",
        "chartType": "line",
        "upperThresholdBG": "red",
        "lowerThresholdBG": "white",
        "showTrend": false,
        "isPositiveTrend": false,
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "thresholdValue": 40,
        "kanban": false,
        "groupId": 10,
        "kpiInfo": {
          "definition": "It measures the percentage of stories rejected during refinement as compared to the overall stories discussed in a week."
        },
        "kpiFilter": "",
        "aggregationCriteria": "sum",
        "trendCalculative": false,
        "xaxisLabel": "Weeks",
        "yaxisLabel": "Count",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de164732a",
        "kpiId": "kpi136",
        "kpiName": "Defect Count by Status",
        "isDeleted": "False",
        "defaultOrder": 13,
        "kpiCategory": "Iteration",
        "kpiSubCategory": "Iteration Review",
        "kpiUnit": "Count",
        "chartType": "pieChart",
        "showTrend": false,
        "isPositiveTrend": true,
        "boxType": "chart",
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "",
        "kanban": false,
        "groupId": 8,
        "kpiInfo": {
          "definition": "It shows the breakup of all defects within an iteration by status. User can view the total defects in the iteration as well as the defects created after iteration start."
        },
        "kpiFilter": "radioButton",
        "trendCalculative": false,
        "xaxisLabel": "",
        "yaxisLabel": "",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de164732b",
        "kpiId": "kpi137",
        "kpiName": "Defect Reopen Rate",
        "isDeleted": "False",
        "defaultOrder": 5,
        "kpiCategory": "Backlog",
        "kpiSubCategory": "Backlog Health",
        "kpiUnit": "Hours",
        "showTrend": false,
        "isPositiveTrend": false,
        "calculateMaturity": false,
        "hideOverallFilter": true,
        "kpiSource": "Jira",
        "kanban": false,
        "groupId": 10,
        "kpiInfo": {
          "definition": "It shows number of defects reopened in a given span of time in comparison to the total closed defects. For all the reopened defects, the average time to reopen is also available."
        },
        "kpiFilter": "dropdown",
        "aggregationCriteria": "average",
        "trendCalculative": false,
        "isAdditionalFilterSupport": true
      },
      {
        "id": "64b4ed7acba3c12de164732c",
        "kpiId": "kpi141",
        "kpiName": "Defect Count by Status",
        "isDeleted": "False",
        "defaultOrder": 1,
        "kpiCategory": "Release",
        "kpiSubCategory": "Quality",
        "kpiUnit": "Count",
        "chartType": "pieChart",
        "showTrend": false,
        "isPositiveTrend": true,
        "boxType": "chart",
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "",
        "kanban": false,
        "groupId": 9,
        "kpiInfo": {
          "definition": "It shows the breakup of all defects tagged to a release based on Status. The breakup is shown in terms of count & percentage."
        },
        "kpiFilter": "",
        "trendCalculative": false,
        "xaxisLabel": "",
        "yaxisLabel": "",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de164732d",
        "kpiId": "kpi142",
        "kpiName": "Defect Count by RCA",
        "isDeleted": "False",
        "defaultOrder": 2,
        "kpiCategory": "Release",
        "kpiSubCategory": "Quality",
        "kpiUnit": "Count",
        "chartType": "pieChart",
        "showTrend": false,
        "isPositiveTrend": true,
        "boxType": "chart",
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "",
        "kanban": false,
        "groupId": 9,
        "kpiInfo": {
          "definition": "It shows the breakup of all defects tagged to a release based on RCA. The breakup is shown in terms of count & percentage."
        },
        "kpiFilter": "radioButton",
        "trendCalculative": false,
        "xaxisLabel": "",
        "yaxisLabel": "",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de164732e",
        "kpiId": "kpi143",
        "kpiName": "Defect Count by Assignee",
        "isDeleted": "False",
        "defaultOrder": 3,
        "kpiCategory": "Release",
        "kpiSubCategory": "Quality",
        "kpiUnit": "Count",
        "chartType": "pieChart",
        "showTrend": false,
        "isPositiveTrend": true,
        "boxType": "chart",
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "",
        "kanban": false,
        "groupId": 9,
        "kpiInfo": {
          "definition": "It shows the breakup of all defects tagged to a release based on Assignee. The breakup is shown in terms of count & percentage."
        },
        "kpiFilter": "radioButton",
        "trendCalculative": false,
        "xaxisLabel": "",
        "yaxisLabel": "",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de164732f",
        "kpiId": "kpi144",
        "kpiName": "Defect Count by Priority",
        "isDeleted": "False",
        "defaultOrder": 4,
        "kpiCategory": "Release",
        "kpiSubCategory": "Quality",
        "kpiUnit": "Count",
        "chartType": "pieChart",
        "showTrend": false,
        "isPositiveTrend": true,
        "boxType": "chart",
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "",
        "kanban": false,
        "groupId": 9,
        "kpiInfo": {
          "definition": "It shows the breakup of all defects tagged to a release based on Priority. The breakup is shown in terms of count & percentage."
        },
        "kpiFilter": "radioButton",
        "trendCalculative": false,
        "xaxisLabel": "",
        "yaxisLabel": "",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de1647330",
        "kpiId": "kpi989",
        "kpiName": "Kpi Maturity",
        "isDeleted": "False",
        "defaultOrder": 1,
        "kpiCategory": "Kpi Maturity",
        "showTrend": false,
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kanban": false,
        "trendCalculative": false,
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de1647331",
        "kpiId": "kpi140",
        "kpiName": "Defect Count by Priority",
        "isDeleted": "False",
        "defaultOrder": 15,
        "kpiCategory": "Iteration",
        "kpiSubCategory": "Iteration Review",
        "kpiUnit": "Count",
        "chartType": "pieChart",
        "showTrend": false,
        "isPositiveTrend": true,
        "boxType": "chart",
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "",
        "kanban": false,
        "groupId": 8,
        "kpiInfo": {
          "definition": "It shows the breakup of all defects within an iteration by priority. User can view the total defects in the iteration as well as the defects created after iteration start."
        },
        "kpiFilter": "radioButton",
        "trendCalculative": false,
        "xaxisLabel": "",
        "yaxisLabel": "",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de1647332",
        "kpiId": "kpi147",
        "kpiName": "Release Progress",
        "isDeleted": "False",
        "defaultOrder": 5,
        "kpiCategory": "Release",
        "kpiSubCategory": "Speed",
        "kpiUnit": "Count",
        "chartType": "horizontalPercentBarChart",
        "showTrend": false,
        "isPositiveTrend": true,
        "boxType": "chart",
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "",
        "kanban": false,
        "groupId": 9,
        "kpiInfo": {
          "definition": "It shows the breakup by status of issues tagged to a release. The breakup is based on both issue count and story points"
        },
        "kpiFilter": "dropDown",
        "kpiWidth": 100,
        "trendCalculative": false,
        "xaxisLabel": "",
        "yaxisLabel": "",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de1647333",
        "kpiId": "kpi145",
        "kpiName": "Dev Completion Status",
        "isDeleted": "False",
        "defaultOrder": 3,
        "kpiCategory": "Iteration",
        "kpiSubCategory": "Iteration Review",
        "kpiUnit": "Count",
        "showTrend": false,
        "isPositiveTrend": true,
        "boxType": "3_column",
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "",
        "kanban": false,
        "groupId": 8,
        "kpiInfo": {
          "definition": "It gives a comparative view between the planned completion and actual completion from a development point of view. In addition, user can see the delay (in days) in dev completed issues"
        },
        "kpiFilter": "multiSelectDropDown",
        "trendCalculative": false,
        "xaxisLabel": "",
        "yaxisLabel": "",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de1647334",
        "kpiId": "kpi138",
        "kpiName": "Backlog Readiness",
        "isDeleted": "False",
        "defaultOrder": 1,
        "kpiCategory": "Backlog",
        "kpiSubCategory": "Backlog Health",
        "kpiUnit": "Count",
        "showTrend": false,
        "isPositiveTrend": true,
        "boxType": "3_column",
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "",
        "kanban": false,
        "groupId": 11,
        "kpiInfo": {
          "details": [
            {
              "type": "paragraph",
              "value": "Ready Backlog: No. of issues which are refined in the backlog. This is identified through a status configured in KnowHOW."
            },
            {
              "type": "paragraph",
              "value": "Backlog Strength: Total size of 'Refined' issues in the backlog / Average velocity of last 5 sprints. It is calculated in terms of no. of sprints. Recommended strength is 2 sprints."
            },
            {
              "type": "paragraph",
              "value": "Readiness cycle time: Average time taken for Product Backlog items (PBIs) to be refined."
            }
          ]
        },
        "kpiFilter": "multiSelectDropDown",
        "trendCalculative": false,
        "xaxisLabel": "",
        "yaxisLabel": "",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de1647335",
        "kpiId": "kpi3",
        "kpiName": "Lead Time",
        "isDeleted": "False",
        "defaultOrder": 1,
        "kpiCategory": "Backlog",
        "kpiSubCategory": "Flow KPIs",
        "kpiBaseLine": "0",
        "kpiUnit": "Days",
        "chartType": "line",
        "upperThresholdBG": "red",
        "lowerThresholdBG": "white",
        "showTrend": true,
        "isPositiveTrend": false,
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "thresholdValue": 20,
        "kanban": false,
        "groupId": 11,
        "kpiInfo": {
          "definition": "Lead Time is the time from the moment when the request was made by a client and placed on a board to when all work on this item is completed and the request was delivered to the client",
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70811702/Lead+time"
              }
            }
          ]
        },
        "kpiFilter": "dropdown",
        "aggregationCriteria": "sum",
        "maturityRange": [
          "-60",
          "60-45",
          "45-30",
          "30-10",
          "10-"
        ],
        "trendCalculative": false,
        "xaxisLabel": "Range",
        "yaxisLabel": "Days",
        "videoLink": {
          "id": "6309b8767bee141bb505e72f",
          "kpiId": "kpi3",
          "videoUrl": "",
          "disabled": false,
          "source": "You Tube"
        },
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de1647336",
        "kpiId": "kpi148",
        "kpiName": "Flow Load",
        "isDeleted": "False",
        "defaultOrder": 7,
        "kpiCategory": "Backlog",
        "kpiSubCategory": "Flow KPIs",
        "kpiUnit": "",
        "chartType": "stacked-area",
        "showTrend": false,
        "isPositiveTrend": false,
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "kanban": false,
        "groupId": 11,
        "kpiInfo": {
          "definition": " Flow load indicates how many items are currently in the backlog. This KPI emphasizes on limiting work in progress to enabling a fast flow of issues"
        },
        "kpiFilter": "",
        "aggregationCriteria": "sum",
        "trendCalculative": false,
        "xaxisLabel": "Time",
        "yaxisLabel": "Count",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de1647337",
        "kpiId": "kpi146",
        "kpiName": "Flow Distribution",
        "isDeleted": "False",
        "defaultOrder": 6,
        "kpiCategory": "Backlog",
        "kpiSubCategory": "Flow KPIs",
        "kpiUnit": "",
        "chartType": "stacked-area",
        "showTrend": false,
        "isPositiveTrend": false,
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "kanban": false,
        "groupId": 10,
        "kpiInfo": {
          "definition": "Flow Distribution evaluates the amount of each kind of work (issue types) which are open in the backlog over a period of time."
        },
        "kpiFilter": "",
        "aggregationCriteria": "sum",
        "trendCalculative": false,
        "xaxisLabel": "Time",
        "yaxisLabel": "Count",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de1647338",
        "kpiId": "kpi149",
        "kpiName": "Happiness Index",
        "isDeleted": "False",
        "defaultOrder": 28,
        "kpiUnit": "",
        "chartType": "line",
        "upperThresholdBG": "white",
        "lowerThresholdBG": "red",
        "showTrend": false,
        "isPositiveTrend": true,
        "boxType": "3_column",
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "5",
        "thresholdValue": 4,
        "kanban": false,
        "groupId": 16,
        "kpiInfo": {
          "details": [
            {
              "type": "paragraph",
              "value": "KPI for tracking moral of team members"
            }
          ]
        },
        "kpiFilter": "multiSelectDropDown",
        "aggregationCriteria": "average",
        "trendCalculative": false,
        "xaxisLabel": "Sprints",
        "yaxisLabel": "Rating",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b4ed7acba3c12de1647339",
        "kpiId": "kpi150",
        "kpiName": "Release Burnup",
        "isDeleted": "False",
        "defaultOrder": 1,
        "kpiCategory": "Release",
        "kpiSubCategory": "Speed",
        "kpiUnit": "Count",
        "chartType": "CumulativeMultilineChart",
        "showTrend": false,
        "isPositiveTrend": true,
        "boxType": "chart",
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "",
        "kanban": false,
        "groupId": 9,
        "kpiInfo": {
          "definition": "It shows the cumulative daily actual progress of the release against the overall scope. It also shows additionally the scope added or removed during the release w.r.t Dev/Qa completion date and Dev/Qa completion status for the Release tagged issues",
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/41582601/RELEASE+Health#Release-Burnup"
              }
            }
          ]
        },
        "kpiFilter": "radioButton",
        "kpiWidth": 100,
        "trendCalculative": false,
        "xaxisLabel": "",
        "yaxisLabel": "Count",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b8bc29c1c8b81824a36a5e",
        "kpiId": "kpi151",
        "kpiName": "Backlog Count By Status",
        "isDeleted": "False",
        "defaultOrder": 9,
        "kpiCategory": "Backlog",
        "kpiSubCategory": "Backlog Overview",
        "kpiUnit": "Count",
        "chartType": "pieChart",
        "showTrend": false,
        "isPositiveTrend": true,
        "boxType": "chart",
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "kanban": false,
        "groupId": 10,
        "kpiInfo": {
          "definition": "Total count of issues in the Backlog with a breakup by Status."
        },
        "trendCalculative": false,
        "xaxisLabel": "",
        "yaxisLabel": "",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64b8bc29c1c8b81824a36a5f",
        "kpiId": "kpi152",
        "kpiName": "Backlog Count By Issue Type",
        "isDeleted": "False",
        "defaultOrder": 10,
        "kpiCategory": "Backlog",
        "kpiSubCategory": "Backlog Overview",
        "kpiUnit": "Count",
        "chartType": "pieChart",
        "showTrend": false,
        "isPositiveTrend": true,
        "boxType": "chart",
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "kanban": false,
        "groupId": 11,
        "kpiInfo": {
          "definition": "Total count of issues in the backlog with a breakup by issue type."
        },
        "trendCalculative": false,
        "xaxisLabel": "",
        "yaxisLabel": "",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64ec311d1ef9f8e4f46ea8d6",
        "kpiId": "kpi153",
        "kpiName": "PI Predictability",
        "isDeleted": "False",
        "defaultOrder": 29,
        "kpiUnit": "",
        "chartType": "multipleline",
        "upperThresholdBG": "white",
        "lowerThresholdBG": "red",
        "showTrend": true,
        "isPositiveTrend": true,
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "200",
        "thresholdValue": 0,
        "kanban": false,
        "groupId": 4,
        "kpiInfo": {
          "definition": "PI predictability is calculated by the sum of the actual value achieved against the planned value at the beginning of the PI",
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27131959/Scrum+VALUE+KPIs#PI-Predictability"
              }
            }
          ]
        },
        "aggregationCriteria": "sum",
        "trendCalculative": false,
        "xaxisLabel": "PIs",
        "yaxisLabel": "Business Value",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64ec311d1ef9f8e4f46ea8d7",
        "kpiId": "kpi154",
        "kpiName": "Daily Standup View",
        "isDeleted": "False",
        "defaultOrder": 8,
        "kpiCategory": "Iteration",
        "kpiSubCategory": "Daily Standup",
        "showTrend": false,
        "isPositiveTrend": true,
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "",
        "kanban": false,
        "groupId": 13,
        "kpiFilter": "multiselectdropdown",
        "kpiWidth": 100,
        "trendCalculative": false,
        "isAdditionalFilterSupport": false
      },
      {
        "id": "64f88591335bf55dfe842cdc",
        "kpiId": "kpi155",
        "kpiName": "Defect Count By Type",
        "isDeleted": "False",
        "defaultOrder": 11,
        "kpiCategory": "Backlog",
        "kpiSubCategory": "Backlog Overview",
        "kpiUnit": "Count",
        "chartType": "pieChart",
        "showTrend": false,
        "isPositiveTrend": true,
        "boxType": "chart",
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "kanban": false,
        "groupId": 11,
        "kpiInfo": {
          "definition": "Total count of issues in the backlog with a breakup by defect type."
        },
        "trendCalculative": false,
        "xaxisLabel": "",
        "yaxisLabel": "",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "650bc420797db1ee82d622bf",
        "kpiId": "kpi164",
        "kpiName": "Scope Churn",
        "isDeleted": "false",
        "defaultOrder": 30,
        "kpiUnit": "%",
        "chartType": "line",
        "upperThresholdBG": "red",
        "lowerThresholdBG": "white",
        "showTrend": true,
        "isPositiveTrend": false,
        "calculateMaturity": true,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": 200,
        "thresholdValue": 20,
        "kanban": false,
        "groupId": 5,
        "kpiInfo": {
          "definition": "Scope churn explains the change in the scope of the sprint since the start of the iteration",
          "formula": [
            {
              "lhs": "Scope Churn",
              "operator": "division",
              "operands": [
                "Count of Stories added + Count of Stories removed",
                "Count of Stories in Initial Commitment at the time of Sprint start"
              ]
            }
          ],
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/26935328/Scrum+SPEED+KPIs#Scope-Churn"
              }
            }
          ]
        },
        "kpiFilter": "radioButton",
        "aggregationCriteria": "average",
        "maturityRange": [
          "-50",
          "50-30",
          "30-20",
          "20-10",
          "10-"
        ],
        "trendCalculative": false,
        "xaxisLabel": "Sprints",
        "yaxisLabel": "Percentage",
        "isAdditionalFilterSupport": true
      },
      {
        "id": "651e8b1eb3cd2c83443d733c",
        "kpiId": "kpi161",
        "kpiName": "Iteration Readiness",
        "isDeleted": "False",
        "defaultOrder": 4,
        "kpiCategory": "Backlog",
        "kpiSubCategory": "Backlog Health",
        "kpiUnit": "Count",
        "chartType": "stackedColumn",
        "showTrend": false,
        "isPositiveTrend": true,
        "boxType": "chart",
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "",
        "kanban": false,
        "groupId": 11,
        "kpiInfo": {
          "definition": "Iteration readiness depicts the state of future iterations w.r.t the quality of refined Backlog",
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/2916400/BACKLOG+Governance#Iteration-Readiness"
              }
            }
          ]
        },
        "kpiFilter": "radioButton",
        "maturityRange": [
          "-40",
          "40-60",
          "60-75",
          "75-90",
          "90-"
        ],
        "trendCalculative": false,
        "xaxisLabel": "Sprint",
        "yaxisLabel": "Count",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "651e8b1eb3cd2c83443d733d",
        "kpiId": "kpi163",
        "kpiName": "Defect by Testing Phase",
        "isDeleted": "False",
        "defaultOrder": 7,
        "kpiCategory": "Release",
        "kpiSubCategory": "Quality",
        "kpiUnit": "Count",
        "chartType": "horizontalPercentBarChart",
        "showTrend": false,
        "isPositiveTrend": true,
        "boxType": "chart",
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "",
        "kanban": false,
        "groupId": 9,
        "kpiInfo": {
          "definition": " It gives a breakup of escaped defects by testing phase"
        },
        "kpiFilter": "radioButton",
        "trendCalculative": false,
        "xaxisLabel": "",
        "yaxisLabel": "",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "651e8b42b3cd2c83443d7347",
        "kpiId": "kpi157",
        "kpiName": "Check-Ins & Merge Requests",
        "isDeleted": "False",
        "defaultOrder": 1,
        "kpiCategory": "Developer",
        "kpiUnit": "MRs",
        "chartType": "grouped_column_plus_line",
        "showTrend": true,
        "isPositiveTrend": true,
        "lineLegend": "Merge Requests",
        "barLegend": "Commits",
        "calculateMaturity": true,
        "hideOverallFilter": true,
        "kpiSource": "BitBucket",
        "maxValue": "10",
        "thresholdValue": 55,
        "kanban": false,
        "groupId": 1,
        "kpiInfo": {
          "definition": "NUMBER OF CHECK-INS helps in measuring the transparency as well the how well the tasks have been broken down. NUMBER OF MERGE REQUESTS when looked at along with commits highlights the efficiency of the review process",
          "details": [
            {
              "type": "paragraph",
              "value": "It is calculated as a Count. Higher the count better is the ‘Speed’"
            },
            {
              "type": "paragraph",
              "value": "A progress indicator shows trend of Number of Check-ins & Merge requests between last 2 days. An upward trend is considered positive"
            }
          ],
          "maturityLevels": [
            {
              "level": "M5",
              "bgColor": "#6cab61",
              "range": "> 16"
            },
            {
              "level": "M4",
              "bgColor": "#AEDB76",
              "range": "8-16"
            },
            {
              "level": "M3",
              "bgColor": "#eff173",
              "range": "4-8"
            },
            {
              "level": "M2",
              "bgColor": "#ffc35b",
              "range": "2-4"
            },
            {
              "level": "M1",
              "bgColor": "#F06667",
              "range": "0-2"
            }
          ]
        },
        "kpiFilter": "dropDown",
        "aggregationCriteria": "average",
        "maturityRange": [
          "-2",
          "2-4",
          "4-8",
          "8-16",
          "16-"
        ],
        "isRepoToolKpi": true,
        "trendCalculative": false,
        "xaxisLabel": "Days",
        "yaxisLabel": "Count",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "651e8b42b3cd2c83443d7348",
        "kpiId": "kpi158",
        "kpiName": "Mean Time To Merge",
        "isDeleted": "False",
        "defaultOrder": 2,
        "kpiCategory": "Developer",
        "kpiUnit": "Hours",
        "chartType": "line",
        "upperThresholdBG": "red",
        "lowerThresholdBG": "white",
        "showTrend": true,
        "isPositiveTrend": false,
        "calculateMaturity": true,
        "hideOverallFilter": true,
        "kpiSource": "BitBucket",
        "maxValue": "10",
        "thresholdValue": 55,
        "kanban": false,
        "groupId": 1,
        "kpiInfo": {
          "definition": "MEAN TIME TO MERGE measures the efficiency of the code review process in a team",
          "details": [
            {
              "type": "paragraph",
              "value": "It is calculated in ‘Hours’. Fewer the Hours better is the ‘Speed’"
            },
            {
              "type": "paragraph",
              "value": "A progress indicator shows trend of Mean time to merge in last 2 weeks. A downward trend is considered positive"
            },
            {
              "type": "paragraph",
              "value": "Maturity of the KPI is calculated based on the average of the last 5 weeks"
            }
          ],
          "maturityLevels": [
            {
              "level": "M5",
              "bgColor": "#6cab61",
              "range": "<4 Hours"
            },
            {
              "level": "M4",
              "bgColor": "#AEDB76",
              "range": "4-8 Hours"
            },
            {
              "level": "M3",
              "bgColor": "#eff173",
              "range": "8-16 Hours"
            },
            {
              "level": "M2",
              "bgColor": "#ffc35b",
              "range": "16-48 Hours"
            },
            {
              "level": "M1",
              "bgColor": "#F06667",
              "range": ">48 Hours"
            }
          ]
        },
        "kpiFilter": "dropDown",
        "aggregationCriteria": "average",
        "maturityRange": [
          "-16",
          "16-8",
          "8-4",
          "4-2",
          "2-"
        ],
        "isRepoToolKpi": true,
        "trendCalculative": false,
        "xaxisLabel": "Weeks",
        "yaxisLabel": "Count(Hours)",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "651e8b42b3cd2c83443d7349",
        "kpiId": "kpi159",
        "kpiName": "Number of Check-ins",
        "isDeleted": "False",
        "defaultOrder": 1,
        "kpiCategory": "Developer",
        "kpiUnit": "check-ins",
        "chartType": "line",
        "showTrend": true,
        "isPositiveTrend": true,
        "calculateMaturity": true,
        "hideOverallFilter": true,
        "kpiSource": "BitBucket",
        "maxValue": 10,
        "thresholdValue": 55,
        "kanban": true,
        "groupId": 1,
        "kpiInfo": {
          "definition": "NUMBER OF CHECK-INS helps in measuring the transparency as well the how well the tasks have been broken down.",
          "details": [
            {
              "type": "paragraph",
              "value": "It is calculated as a Count. Higher the count better is the ‘Speed’"
            },
            {
              "type": "paragraph",
              "value": "A progress indicator shows trend of Number of Check-ins & Merge requests between last 2 days. An upward trend is considered positive."
            },
            {
              "type": "paragraph",
              "value": "Maturity of the KPI is calculated based on the latest value"
            }
          ],
          "maturityLevels": [
            {
              "level": "M5",
              "bgColor": "#6cab61",
              "range": ">16"
            },
            {
              "level": "M4",
              "bgColor": "#AEDB76",
              "range": "8-16"
            },
            {
              "level": "M3",
              "bgColor": "#eff173",
              "range": "4-8"
            },
            {
              "level": "M2",
              "bgColor": "#ffc35b",
              "range": "2-4"
            },
            {
              "level": "M1",
              "bgColor": "#F06667",
              "range": "0-2"
            }
          ]
        },
        "kpiFilter": "dropDown",
        "aggregationCriteria": "sum",
        "maturityRange": [
          "-2",
          "2-4",
          "4-8",
          "8-16",
          "16-"
        ],
        "isRepoToolKpi": true,
        "trendCalculative": false,
        "xaxisLabel": "Weeks",
        "yaxisLabel": "Count",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "651e8b42b3cd2c83443d734a",
        "kpiId": "kpi160",
        "kpiName": "Pickup Time",
        "isDeleted": "False",
        "defaultOrder": 3,
        "kpiCategory": "Developer",
        "kpiUnit": "Hours",
        "chartType": "line",
        "upperThresholdBG": "red",
        "lowerThresholdBG": "white",
        "showTrend": true,
        "isPositiveTrend": false,
        "calculateMaturity": true,
        "hideOverallFilter": true,
        "kpiSource": "BitBucket",
        "maxValue": 10,
        "thresholdValue": 20,
        "kanban": false,
        "groupId": 1,
        "kpiInfo": {
          "definition": "Pickup time measures the time a pull request waits for someone to start reviewing it. Low pickup time represents strong teamwork and a healthy review",
          "details": [
            {
              "type": "paragraph",
              "value": "It is calculated in ‘Hours’. Fewer the Hours better is the ‘Speed’"
            },
            {
              "type": "paragraph",
              "value": "A progress indicator shows trend of Pickup Time in last 2 weeks. A downward trend is considered positive"
            },
            {
              "type": "paragraph",
              "value": "Maturity of the KPI is calculated based on the average of the last 5 weeks"
            }
          ],
          "maturityLevels": [
            {
              "level": "M5",
              "bgColor": "#6cab61",
              "range": "<4 Hours"
            },
            {
              "level": "M4",
              "bgColor": "#AEDB76",
              "range": "4-8 Hours"
            },
            {
              "level": "M3",
              "bgColor": "#eff173",
              "range": "8-16 Hours"
            },
            {
              "level": "M2",
              "bgColor": "#ffc35b",
              "range": "16-48 Hours"
            },
            {
              "level": "M1",
              "bgColor": "#F06667",
              "range": ">48 Hours"
            }
          ]
        },
        "kpiFilter": "dropDown",
        "aggregationCriteria": "average",
        "maturityRange": [
          "-16",
          "16-8",
          "8-4",
          "4-2",
          "2-"
        ],
        "isRepoToolKpi": true,
        "trendCalculative": false,
        "xaxisLabel": "Weeks",
        "yaxisLabel": "Count(Hours)",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "651e8b42b3cd2c83443d734b",
        "kpiId": "kpi162",
        "kpiName": "PR Size",
        "isDeleted": "False",
        "defaultOrder": 4,
        "kpiCategory": "Developer",
        "kpiUnit": "Lines",
        "chartType": "line",
        "upperThresholdBG": "red",
        "lowerThresholdBG": "white",
        "showTrend": false,
        "isPositiveTrend": false,
        "calculateMaturity": false,
        "hideOverallFilter": true,
        "kpiSource": "BitBucket",
        "maxValue": 10,
        "thresholdValue": 4,
        "kanban": false,
        "groupId": 1,
        "kpiInfo": {
          "definition": "Pull request size measures the number of code lines modified in a pull request. Smaller pull requests are easier to review, safer to merge, and correlate to a lower cycle time."
        },
        "kpiFilter": "dropDown",
        "aggregationCriteria": "average",
        "maturityRange": [
          "-16",
          "16-8",
          "8-4",
          "4-2",
          "2-"
        ],
        "isRepoToolKpi": true,
        "trendCalculative": false,
        "xaxisLabel": "Weeks",
        "yaxisLabel": "Count(No. of Lines)",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "6541e98bb1cd5889350cae45",
        "kpiId": "kpi165",
        "kpiName": "Epic Progress",
        "isDeleted": "False",
        "defaultOrder": 5,
        "kpiCategory": "Release",
        "kpiSubCategory": "Value",
        "kpiUnit": "Count",
        "chartType": "horizontalPercentBarChart",
        "showTrend": false,
        "isPositiveTrend": true,
        "boxType": "chart",
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "",
        "kanban": false,
        "groupId": 9,
        "kpiInfo": {
          "definition": "It depicts the progress of each epic in a release in terms of total count and %age completion."
        },
        "kpiFilter": "multiSelectDropDown",
        "kpiWidth": 100,
        "trendCalculative": false,
        "xaxisLabel": "",
        "yaxisLabel": "",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "6541e98bb1cd5889350cae46",
        "kpiId": "kpi169",
        "kpiName": "Epic Progress",
        "isDeleted": "False",
        "defaultOrder": 5,
        "kpiCategory": "Backlog",
        "kpiSubCategory": "Epic View",
        "kpiUnit": "Count",
        "chartType": "horizontalPercentBarChart",
        "showTrend": false,
        "isPositiveTrend": true,
        "boxType": "chart",
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "",
        "kanban": false,
        "groupId": 9,
        "kpiInfo": {
          "definition": "It depicts the progress of each epic in terms of total count and %age completion."
        },
        "kpiFilter": "radioButton",
        "kpiWidth": 100,
        "trendCalculative": false,
        "xaxisLabel": "",
        "yaxisLabel": "",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "656347659b6b2f1d4faa9ebe",
        "kpiId": "kpi168",
        "kpiName": "Sonar Code Quality",
        "isDeleted": "False",
        "defaultOrder": 14,
        "kpiUnit": "unit",
        "chartType": "bar-with-y-axis-group",
        "upperThresholdBG": "white",
        "lowerThresholdBG": "red",
        "showTrend": true,
        "isPositiveTrend": true,
        "calculateMaturity": true,
        "hideOverallFilter": true,
        "kpiSource": "Sonar",
        "maxValue": "90",
        "thresholdValue": 2,
        "kanban": false,
        "groupId": 1,
        "kpiInfo": {
          "definition": "Sonar Code Quality is graded based on the static and dynamic code analysis procedure built in Sonarqube that analyses code from multiple perspectives.",
          "details": [
            {
              "type": "paragraph",
              "value": "Code Quality in Sonarqube is shown as Grades (A to E)."
            },
            {
              "type": "paragraph",
              "value": "A is the highest (best) and,"
            },
            {
              "type": "paragraph",
              "value": "E is the least"
            },
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#Sonar-Code-Quality"
              }
            }
          ]
        },
        "kpiFilter": "dropDown",
        "aggregationCriteria": "average",
        "maturityRange": [
          "5",
          "4",
          "3",
          "2",
          "1"
        ],
        "yaxisOrder": {
          "1": "A",
          "2": "B",
          "3": "C",
          "4": "D",
          "5": "E"
        },
        "trendCalculative": false,
        "xaxisLabel": "Months",
        "yaxisLabel": "Code Quality",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "656347669b6b2f1d4faa9ec5",
        "kpiId": "kpi170",
        "kpiName": "Flow Efficiency",
        "isDeleted": "False",
        "defaultOrder": 1,
        "kpiCategory": "Backlog",
        "kpiSubCategory": "Flow KPIs",
        "kpiUnit": "%",
        "chartType": "line",
        "upperThresholdBG": "red",
        "lowerThresholdBG": "white",
        "showTrend": false,
        "isPositiveTrend": false,
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "thresholdValue": 40,
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
      },
      {
        "id": "656f3692f2ca9d0920f07394",
        "kpiId": "kpi171",
        "kpiName": "Cycle Time",
        "isDeleted": "False",
        "defaultOrder": 4,
        "kpiCategory": "Backlog",
        "kpiSubCategory": "Flow KPIs",
        "kpiUnit": "Days",
        "chartType": "stackedColumn",
        "showTrend": false,
        "boxType": "chart",
        "calculateMaturity": false,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "",
        "kanban": false,
        "groupId": 11,
        "kpiInfo": {
          "definition": "Cycle time helps ascertain time spent on each step of the complete issue lifecycle. It is being depicted in the visualization as 3 core cycles - Intake to DOR, DOR to DOD, DOD to Live",
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70418714/Cycle+time"
              }
            }
          ]
        },
        "kpiFilter": "dropDown",
        "isAggregationStacks": false,
        "trendCalculative": false,
        "xaxisLabel": "Range",
        "yaxisLabel": "Days",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "6570635518989f8fe1280eae",
        "kpiId": "kpi166",
        "kpiName": "Mean Time to Recover",
        "isDeleted": "False",
        "defaultOrder": 4,
        "kpiCategory": "Dora",
        "kpiUnit": "Hours",
        "chartType": "line",
        "upperThresholdBG": "red",
        "lowerThresholdBG": "white",
        "showTrend": true,
        "isPositiveTrend": false,
        "calculateMaturity": true,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "100",
        "thresholdValue": 24,
        "kanban": false,
        "groupId": 15,
        "kpiInfo": {
          "definition": "Mean time to recover will be based on the Production incident tickets raised during a certain period of time.",
          "details": [
            {
              "type": "paragraph",
              "value": "For all the production incident tickets raised during a time period, the time between created date and closed date of the incident ticket will be calculated."
            },
            {
              "type": "paragraph",
              "value": "The average of all such tickets will be shown."
            },
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/59080705/DORA+KPIs#Mean-time-to-Recover-(MTTR)"
              }
            }
          ],
          "maturityLevels": []
        },
        "kpiFilter": "",
        "aggregationCriteria": "sum",
        "aggregationCircleCriteria": "average",
        "maturityRange": [
          "48-",
          "24-48",
          "12-24",
          "1-12",
          "-1"
        ],
        "maturityLevel": [
          {
            "level": "M5",
            "bgColor": "#167a26",
            "displayRange": "0-1 Hour"
          },
          {
            "level": "M4",
            "bgColor": "#4ebb1a",
            "displayRange": "1-12 Hours"
          },
          {
            "level": "M3",
            "bgColor": "#ef7643",
            "displayRange": "12-24 Hours"
          },
          {
            "level": "M2",
            "bgColor": "#f53535",
            "displayRange": "24-48 Hours"
          },
          {
            "level": "M1",
            "bgColor": "#c91212",
            "displayRange": "48 Hours and Above"
          }
        ],
        "trendCalculative": false,
        "xaxisLabel": "Weeks",
        "yaxisLabel": "Hours",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "6570635518989f8fe1280eaf",
        "kpiId": "kpi116",
        "kpiName": "Change Failure Rate",
        "isDeleted": "False",
        "defaultOrder": 15,
        "kpiCategory": "Dora",
        "kpiUnit": "%",
        "chartType": "line",
        "upperThresholdBG": "red",
        "lowerThresholdBG": "white",
        "showTrend": true,
        "isPositiveTrend": false,
        "calculateMaturity": true,
        "hideOverallFilter": true,
        "kpiSource": "Jenkins",
        "maxValue": "100",
        "thresholdValue": 30,
        "kanban": false,
        "groupId": 14,
        "kpiInfo": {
          "definition": "Measures the proportion of builds that have failed over a given period of time",
          "formula": [
            {
              "lhs": "Change Failure Rate",
              "operator": "division",
              "operands": [
                "Total number of failed Builds",
                "Total number of Builds"
              ]
            }
          ],
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#Change-Failure-Rate"
              }
            }
          ]
        },
        "kpiFilter": "dropDown",
        "aggregationCriteria": "average",
        "aggregationCircleCriteria": "average",
        "maturityRange": [
          "-60",
          "60-45",
          "45-30",
          "30-15",
          "15-"
        ],
        "maturityLevel": [
          {
            "level": "M5",
            "bgColor": "#167a26",
            "displayRange": "0-15 %"
          },
          {
            "level": "M4",
            "bgColor": "#4ebb1a",
            "displayRange": "15-30 %"
          },
          {
            "level": "M3",
            "bgColor": "#ef7643",
            "displayRange": "30-45 %"
          },
          {
            "level": "M2",
            "bgColor": "#f53535",
            "displayRange": "45-60 %"
          },
          {
            "level": "M1",
            "bgColor": "#c91212",
            "displayRange": "60 % and Above"
          }
        ],
        "trendCalculative": false,
        "isAdditionalFilterSupport": false
      },
      {
        "id": "6570635518989f8fe1280eb0",
        "kpiId": "kpi118",
        "kpiName": "Deployment Frequency",
        "isDeleted": "False",
        "defaultOrder": 25,
        "kpiCategory": "Dora",
        "kpiUnit": "Number",
        "chartType": "line",
        "upperThresholdBG": "white",
        "lowerThresholdBG": "red",
        "showTrend": true,
        "isPositiveTrend": true,
        "calculateMaturity": true,
        "hideOverallFilter": false,
        "kpiSource": "Jenkins",
        "maxValue": "100",
        "thresholdValue": 6,
        "kanban": false,
        "groupId": 14,
        "kpiInfo": {
          "definition": "Measures how often code is deployed to production in a period",
          "details": [
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27131959/Scrum+VALUE+KPIs#Deployment-Frequency"
              }
            }
          ]
        },
        "kpiFilter": "multiSelectDropDown",
        "aggregationCriteria": "sum",
        "aggregationCircleCriteria": "average",
        "maturityRange": [
          "0-2",
          "2-4",
          "4-6",
          "6-8",
          "8-"
        ],
        "maturityLevel": [
          {
            "level": "M5",
            "bgColor": "#167a26",
            "label": ">= 2 per week",
            "displayRange": "8 and Above"
          },
          {
            "level": "M4",
            "bgColor": "#4ebb1a",
            "label": "Once per week",
            "displayRange": "6,7"
          },
          {
            "level": "M3",
            "bgColor": "#ef7643",
            "label": "Once in 2 weeks",
            "displayRange": "4,5"
          },
          {
            "level": "M2",
            "bgColor": "#f53535",
            "label": "Once in 4 weeks",
            "displayRange": "2,3"
          },
          {
            "level": "M1",
            "bgColor": "#c91212",
            "label": "< Once in 8 weeks",
            "displayRange": "0,1"
          }
        ],
        "trendCalculative": false,
        "xaxisLabel": "Weeks",
        "yaxisLabel": "Count",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "6570635518989f8fe1280eb1",
        "kpiId": "kpi156",
        "kpiName": "Lead Time For Change",
        "isDeleted": "False",
        "defaultOrder": 3,
        "kpiCategory": "Dora",
        "kpiUnit": "Days",
        "chartType": "line",
        "upperThresholdBG": "white",
        "lowerThresholdBG": "red",
        "showTrend": true,
        "isPositiveTrend": true,
        "calculateMaturity": true,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "100",
        "thresholdValue": 7,
        "kanban": false,
        "groupId": 15,
        "kpiInfo": {
          "definition": "LEAD TIME FOR CHANGE measures the velocity of software delivery.",
          "details": [
            {
              "type": "paragraph",
              "value": "LEAD TIME FOR CHANGE Captures the time between a code change to commit and deployed to production."
            }
          ]
        },
        "kpiFilter": "",
        "aggregationCriteria": "sum",
        "aggregationCircleCriteria": "average",
        "maturityRange": [
          "90-",
          "30-90",
          "7-30",
          "1-7",
          "-1"
        ],
        "maturityLevel": [
          {
            "level": "M5",
            "bgColor": "#167a26",
            "label": "< 1 Day",
            "displayRange": "0-1 Day"
          },
          {
            "level": "M4",
            "bgColor": "#4ebb1a",
            "label": "< 7 Days",
            "displayRange": "1-7 Days"
          },
          {
            "level": "M3",
            "bgColor": "#ef7643",
            "label": "< 30 Days",
            "displayRange": "7-30 Days"
          },
          {
            "level": "M2",
            "bgColor": "#f53535",
            "label": "< 90 Days",
            "displayRange": "30-90 Days"
          },
          {
            "level": "M1",
            "bgColor": "#c91212",
            "label": ">= 90 Days",
            "displayRange": "90 Days and Above"
          }
        ],
        "trendCalculative": false,
        "xaxisLabel": "Weeks",
        "yaxisLabel": "Days",
        "isAdditionalFilterSupport": false
      }
    ]
  };
  const postData = {
    "kpiList": [
      {
        "id": "6570635518989f8fe1280eae",
        "kpiId": "kpi166",
        "kpiName": "Mean Time to Recover",
        "isDeleted": "False",
        "defaultOrder": 4,
        "kpiCategory": "Dora",
        "kpiUnit": "Hours",
        "chartType": "",
        "upperThresholdBG": "red",
        "lowerThresholdBG": "white",
        "showTrend": true,
        "isPositiveTrend": false,
        "calculateMaturity": true,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "100",
        "thresholdValue": 24,
        "kanban": false,
        "groupId": 15,
        "kpiInfo": {
          "definition": "Mean time to recover will be based on the Production incident tickets raised during a certain period of time.",
          "details": [
            {
              "type": "paragraph",
              "value": "For all the production incident tickets raised during a time period, the time between created date and closed date of the incident ticket will be calculated."
            },
            {
              "type": "paragraph",
              "value": "The average of all such tickets will be shown."
            },
            {
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/59080705/DORA+KPIs#Mean-time-to-Recover-(MTTR)"
              }
            }
          ],
          "maturityLevels": []
        },
        "kpiFilter": "",
        "aggregationCriteria": "sum",
        "aggregationCircleCriteria": "average",
        "maturityRange": [
          "48-",
          "24-48",
          "12-24",
          "1-12",
          "-1"
        ],
        "maturityLevel": [
          {
            "level": "M5",
            "bgColor": "#167a26",
            "displayRange": "0-1 Hour"
          },
          {
            "level": "M4",
            "bgColor": "#4ebb1a",
            "displayRange": "1-12 Hours"
          },
          {
            "level": "M3",
            "bgColor": "#ef7643",
            "displayRange": "12-24 Hours"
          },
          {
            "level": "M2",
            "bgColor": "#f53535",
            "displayRange": "24-48 Hours"
          },
          {
            "level": "M1",
            "bgColor": "#c91212",
            "displayRange": "48 Hours and Above"
          }
        ],
        "trendCalculative": false,
        "xaxisLabel": "Weeks",
        "yaxisLabel": "Hours",
        "isAdditionalFilterSupport": false
      },
      {
        "id": "6570635518989f8fe1280eb1",
        "kpiId": "kpi156",
        "kpiName": "Lead Time For Change",
        "isDeleted": "False",
        "defaultOrder": 3,
        "kpiCategory": "Dora",
        "kpiUnit": "Days",
        "chartType": "",
        "upperThresholdBG": "white",
        "lowerThresholdBG": "red",
        "showTrend": true,
        "isPositiveTrend": true,
        "calculateMaturity": true,
        "hideOverallFilter": false,
        "kpiSource": "Jira",
        "maxValue": "100",
        "thresholdValue": 7,
        "kanban": false,
        "groupId": 15,
        "kpiInfo": {
          "definition": "LEAD TIME FOR CHANGE measures the velocity of software delivery.",
          "details": [
            {
              "type": "paragraph",
              "value": "LEAD TIME FOR CHANGE Captures the time between a code change to commit and deployed to production."
            }
          ]
        },
        "kpiFilter": "",
        "aggregationCriteria": "sum",
        "aggregationCircleCriteria": "average",
        "maturityRange": [
          "90-",
          "30-90",
          "7-30",
          "1-7",
          "-1"
        ],
        "maturityLevel": [
          {
            "level": "M5",
            "bgColor": "#167a26",
            "label": "< 1 Day",
            "displayRange": "0-1 Day"
          },
          {
            "level": "M4",
            "bgColor": "#4ebb1a",
            "label": "< 7 Days",
            "displayRange": "1-7 Days"
          },
          {
            "level": "M3",
            "bgColor": "#ef7643",
            "label": "< 30 Days",
            "displayRange": "7-30 Days"
          },
          {
            "level": "M2",
            "bgColor": "#f53535",
            "label": "< 90 Days",
            "displayRange": "30-90 Days"
          },
          {
            "level": "M1",
            "bgColor": "#c91212",
            "label": ">= 90 Days",
            "displayRange": "90 Days and Above"
          }
        ],
        "trendCalculative": false,
        "xaxisLabel": "Weeks",
        "yaxisLabel": "Days",
        "isAdditionalFilterSupport": false
      }
    ],
    "ids": [
      "PSknowHOW _6527af981704342160f43748"
    ],
    "level": 5,
    "selectedMap": {
      "bu": [],
      "ver": [],
      "acc": [],
      "port": [],
      "project": [
        "PSknowHOW _6527af981704342160f43748"
      ],
      "sprint": [],
      "release": [],
      "sqd": []
    },
    "sprintIncluded": [
      "CLOSED"
    ],
    "label": "project"
  };

  const fakeDoraKpis = require('../../../test/resource/fakeDoraKpis.json');
  const fakeDoraKpiFilters = require('../../../test/resource/fakeDoraKpiFilters.json');
  beforeEach(async () => {
    service = new SharedService();
    await TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule
      ],
      declarations: [DoraComponent,ExportExcelComponent],
      providers: [
        HelperService,MessageService,
        HttpService,
        { provide: APP_CONFIG, useValue: AppConfig },
        { provide: SharedService, useValue: service },
        DatePipe
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
      .compileComponents();

    fixture = TestBed.createComponent(DoraComponent);
    httpService = TestBed.inject(HttpService);
    helperService = TestBed.inject(HelperService);
    component = fixture.componentInstance;
    component.globalConfig = {}
    service.select(masterData, filterData, {}, 'dora');
    component.filterApplyData = {
      "ids": [
        "PSknowHOW _6527af981704342160f43748"
      ],
      "sprintIncluded": [
        "CLOSED"
      ],
      "selectedMap": {
        "bu": [],
        "ver": [],
        "acc": [],
        "port": [],
        "project": [
          "PSknowHOW _6527af981704342160f43748"
        ],
        "sprint": [],
        "release": [],
        "sqd": []
      },
      "level": 5,
      "label": "project"
    };
    component.filterData = filterData;
    fixture.detectChanges();
  });

  it('should create', (done) => {
    spyOn(component, 'receiveSharedData');
    expect(component).toBeTruthy();
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
  });

  it('should make post call when kpi available for Jenkins for Scrum', () => {
    const kpiListJenkins = {
      "kpiList": [{
        id: '6332dd4b82451128f9939a29',
        kpiId: 'kpi118',
        kpiName: 'Deployment Frequency'
      }]
    };
    const postJenkinsSpy = spyOn(component, 'postJenkinsKpi');
    component.groupJenkinsKpi(['kpi118']);
    expect(postJenkinsSpy).toBeDefined();
  });

  it('should check if kpi exists', () => {
    component.allKpiArray = [{
      kpiId: 'kpi118'
    }];
    const result = component.ifKpiExist('kpi118');
    expect(result).toEqual(0);
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
    component.selectedtype = 'scrum'
    const lstotage = {'scrum' : []}
    spyOn(component, 'processKpiConfigData');
    localStorage.setItem('completeHierarchyData',JSON.stringify(lstotage))
    component.receiveSharedData(event);
    expect(component.noTabAccess).toBe(true);
  });

  it('should call grouping kpi functions when filterdata is available', () => {
    const data = globalData['data'];
    spyOn(service, 'getDashConfigData').and.returnValue(data);
    component.configGlobalData = [{
      kpiId: 'kpi14',
      kpiName: 'Defect Injection Rate',
      isEnabled: true,
      order: 1,
      kpiDetail: {
        id: '633ed17f2c2d5abef2451fd8',
        kpiId: 'kpi14',
        kpiName: 'Defect Injection Rate',
        kanban: true,
        groupId: 2,
      },
      shown: true
    }]
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
    spyOn(component, 'processKpiConfigData');
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

  it('should get dropdown data', () => {
    component.allKpiArray = [{
      kpiId: 'kpi118',
      kpiName: 'Deployment Frequency',
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
    component.updatedConfigGlobalData = updatedConfigGlobalData;
    component.getDropdownArray('kpi118');
    expect(component.kpiDropdowns['kpi118'].length).toBeGreaterThan(0);
  });

  it('should call getDropdownArray', () => {
    const kpiId = "kpi118";
    component.allKpiArray = fakeDoraKpis;
    component.colorObj = {
      "KnowHOW": {
        "nodeName": "KnowHOW",
        "color": "#079FFF"
      }
    };

    const response = [
      {
        "filterType": "Select a filter",
        "options": [
          "81.200.188.111->KnowHOW",
          "81.200.188.112->KnowHOW",
          "81.200.188.113->KnowHOW"
        ]
      },
    ];
    component.updatedConfigGlobalData = updatedConfigGlobalData;
    component.getDropdownArray(kpiId);
    expect(component.kpiDropdowns["kpi118"][0]?.options).toEqual(response[0]?.options);
  });

  it('should sort Alphabetically', () => {
    const objArray = [
      {
        "data": "AddingIterationProject",
        "value": [
        ],
        "maturity": "1",
        "maturityValue": "0.0"
      }
    ];

    const value = [
      {
        "data": "AddingIterationProject",
        "value": [
        ],
        "maturity": "1",
        "maturityValue": "0.0"
      }
    ]
    const result = component.sortAlphabetically(objArray);
    expect(result).toEqual(value);

  });

  it('should call handleSelectedOption for kpi118', () => {
    const event = {
      "filter": ["81.200.188.111->KnowHOW"],
    };
    const kpi = {
      "kpiId": "kpi118",
      "kpiName": "Deployment Frequency",
      "isEnabled": true,
      "order": 18,
      "shown": true
    };
    const response = ['81.200.188.111->KnowHOW'];
    const spyData = component.handleSelectedOption(event, kpi);
    expect(component.kpiSelectedFilterObj["kpi118"]).toEqual(response);
  });

  it('should get kpi comments count', fakeAsync(() => {
    component.filterApplyData = {
      'selectedMap': {
        'project': ["KnowHOW_6360fefc3fa9e175755f0728"]
      },
      'level': 5
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
    spyOn(helperService,'getKpiCommentsCount').and.returnValue(Promise.resolve({}))
    component.getKpiCommentsCount();
    tick();
    expect(component.kpiCommentsCountObj).toBeDefined();
  }));

  it('should getchartdata for kpi when trendValueList is an object and with single filter', () => {
    component.allKpiArray = fakeDoraKpis;
    component.kpiSelectedFilterObj['kpi118'] = ['Overall'];
    const res = fakeDoraKpis[0].trendValueList.filter(x => x['filter'] == 'Overall')[0];
    component.getChartData('kpi118', 0, 'sum')
    expect(component.kpiChartData['kpi118'][0]?.value.length).toEqual(res?.value[0]?.value?.length);
  });

  it('should getchartdata for kpi when trendValueList is an object and with multiple filter', () => {
    component.allKpiArray = fakeDoraKpis;
    component.kpiSelectedFilterObj['kpi118'] = ['81.200.188.111->KnowHOW', '81.200.188.112->KnowHOW'];
    const res = fakeDoraKpiFilters;
    component.tooltip = {
      'percentile': 90
    };
    spyOn(helperService, 'applyAggregationLogic').and.callThrough();
    component.getChartData('kpi118', 0, 'sum')
    expect(component.kpiChartData['kpi118'][0]?.value?.length).toEqual(res?.value?.length);
  });

  it('should generate colorObj', () => {
    let kpiId = 'kpi118';
    let kpiData = kpi118ChartData;
    component.colorObj = {
      "PSknowHOW _6527af981704342160f43748": {
        "nodeName": "PSknowHOW ",
        "color": "#079FFF"
      }
    };
    fixture.detectChanges();
    component.generateColorObj(kpiId, kpiData);
    fixture.detectChanges();
    expect(component.chartColorList[kpiId]).toEqual(['#079FFF']);

  });



  describe('createAllKpiArray', () => {
    it('should add data to allKpiArray', () => {
      const data = { 1: { kpiId: 1, trendValueList: [] } };
      component.createAllKpiArray(data);
      expect(component.allKpiArray).toEqual([{ kpiId: 1, trendValueList: [] }]);
    });

    it('should remove existing data from allKpiArray if the kpiId already exists', () => {
      component.allKpiArray = [{ kpiId: 1, trendValueList: [] }];
      const data = { 1: { kpiId: 1, trendValueList: [] } };
      component.createAllKpiArray(data);
      expect(component.allKpiArray).toEqual([{ kpiId: 1, trendValueList: [] }]);
    });

    it('should call getDropdownArray and setKpiSubFilterObj if the trendValueList has filter or filter1 properties', () => {
      spyOn(component, 'getDropdownArray');
      spyOn(component.service, 'setKpiSubFilterObj');
      const data = kpi116ChartData;
      component.createAllKpiArray(data);
      expect(component.getDropdownArray).toHaveBeenCalledWith('kpi116');
      expect(component.service.setKpiSubFilterObj).toHaveBeenCalledWith({
        kpi116: ['Overall'], action: 'new'
      });
    });

    it('should call getChartData if inputIsChartData is false', () => {
      spyOn(component, 'getChartData');
      const data = { 1: { kpiId: 1, trendValueList: [] } };
      component.createAllKpiArray(data);
      expect(component.getChartData).toHaveBeenCalledWith(1, 0, undefined);
    });

    it('should not call getChartData if inputIsChartData is true', () => {
      spyOn(component, 'getChartData');
      const data = { 1: { kpiId: 1, trendValueList: [] } };
      component.createAllKpiArray(data, true);
      expect(component.getChartData).not.toHaveBeenCalled();
    });
  });

  describe('showTooltip', () => {
    it('should set toolTipTop if event is provided', () => {
      const event = { target: { getBoundingClientRect: () => ({ top: 100, left: 0, width: 0, height: 0 }) } };
      component.showTooltip(event, true, 'kpi116');
      expect(component.toolTipTop).toBe(100);
    });

    it('should set isTooltip to kpiId if val is true', () => {
      component.showTooltip(null, true, 'kpi116');
      expect(component.isTooltip).toBe('kpi116');
    });

    it('should set isTooltip to an empty string if val is false', () => {
      component.showTooltip(null, false, 1);
      expect(component.isTooltip).toBe('');
    });
  });

  describe('reloadKPI', () => {
    it('should remove existing kpi from allKpiArray if it exists', () => {
      component.allKpiArray = [{ kpiId: 1 }];
      const event = { kpiDetail: { kpiId: 1 } };
      component.reloadKPI(event);
      expect(component.allKpiArray).toEqual([]);
    });

    it('should call postJenkinsKpi if kpiSource is "jenkins"', () => {
      const kpiListJenkins = [{
        id: '6332dd4b82451128f9939a29',
        kpiId: 'kpi118',
        kpiName: 'Deployment Frequency'
      }];
      const event = {
        "kpiId": "kpi118",
        "kpiName": "Deployment Frequency",
        "isEnabled": true,
        "order": 25,
        "kpiDetail": {
          "id": "6570635518989f8fe1280eb0",
          "kpiId": "kpi118",
          "kpiName": "Deployment Frequency",
          "isDeleted": "False",
          "defaultOrder": 25,
          "kpiCategory": "Dora",
          "kpiUnit": "Number",
          "chartType": "line",
          "upperThresholdBG": "white",
          "lowerThresholdBG": "red",
          "showTrend": true,
          "isPositiveTrend": true,
          "calculateMaturity": true,
          "hideOverallFilter": false,
          "kpiSource": "Jenkins",
          "maxValue": "100",
          "thresholdValue": 6,
          "kanban": false,
          "groupId": 14,
          "kpiInfo": {
            "definition": "Measures how often code is deployed to production in a period",
            "details": [
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27131959/Scrum+VALUE+KPIs#Deployment-Frequency"
                }
              }
            ]
          },
          "kpiFilter": "multiSelectDropDown",
          "aggregationCriteria": "sum",
          "aggregationCircleCriteria": "average",
          "maturityRange": [
            "0-2",
            "2-4",
            "4-6",
            "6-8",
            "8-"
          ],
          "maturityLevel": [
            {
              "level": "M5",
              "bgColor": "#167a26",
              "label": ">= 2 per week",
              "displayRange": "8 and Above"
            },
            {
              "level": "M4",
              "bgColor": "#4ebb1a",
              "label": "Once per week",
              "displayRange": "6,7"
            },
            {
              "level": "M3",
              "bgColor": "#ef7643",
              "label": "Once in 2 weeks",
              "displayRange": "4,5"
            },
            {
              "level": "M2",
              "bgColor": "#f53535",
              "label": "Once in 4 weeks",
              "displayRange": "2,3"
            },
            {
              "level": "M1",
              "bgColor": "#c91212",
              "label": "< Once in 8 weeks",
              "displayRange": "0,1"
            }
          ],
          "trendCalculative": false,
          "xaxisLabel": "Weeks",
          "yaxisLabel": "Count",
          "isAdditionalFilterSupport": false
        },
        "shown": true
      };
      spyOn(helperService, 'groupKpiFromMaster').and.returnValue({ kpiList: kpiListJenkins });
      const spy = spyOn(component, 'postJenkinsKpi');
      component.reloadKPI(event);
      expect(spy).toHaveBeenCalled();
    });

    it('should call postJiraKpi if kpiSource is not "jenkins"', () => {
      const kpiListJira = [{
        id: '6332dd4b82451128f9939a29',
        kpiId: 'kpi166',
        kpiName: 'Mean Time to Recover'
      }, {
        id: '6332dd4b82451128f9939b29',
        kpiId: 'kpi156',
        kpiName: 'Lead Time For Change'
      }];
      const event = {
        "kpiId": "kpi156",
        "kpiName": "Lead Time For Change",
        "isEnabled": true,
        "order": 3,
        "kpiDetail": {
          "id": "6570635518989f8fe1280eb1",
          "kpiId": "kpi156",
          "kpiName": "Lead Time For Change",
          "isDeleted": "False",
          "defaultOrder": 3,
          "kpiCategory": "Dora",
          "kpiUnit": "Days",
          "chartType": "line",
          "upperThresholdBG": "white",
          "lowerThresholdBG": "red",
          "showTrend": true,
          "isPositiveTrend": true,
          "calculateMaturity": true,
          "hideOverallFilter": false,
          "kpiSource": "Jira",
          "maxValue": "100",
          "thresholdValue": 7,
          "kanban": false,
          "groupId": 15,
          "kpiInfo": {
            "definition": "LEAD TIME FOR CHANGE measures the velocity of software delivery.",
            "details": [
              {
                "type": "paragraph",
                "value": "LEAD TIME FOR CHANGE Captures the time between a code change to commit and deployed to production."
              }
            ]
          },
          "kpiFilter": "",
          "aggregationCriteria": "sum",
          "aggregationCircleCriteria": "average",
          "maturityRange": [
            "90-",
            "30-90",
            "7-30",
            "1-7",
            "-1"
          ],
          "maturityLevel": [
            {
              "level": "M5",
              "bgColor": "#167a26",
              "label": "< 1 Day",
              "displayRange": "0-1 Day"
            },
            {
              "level": "M4",
              "bgColor": "#4ebb1a",
              "label": "< 7 Days",
              "displayRange": "1-7 Days"
            },
            {
              "level": "M3",
              "bgColor": "#ef7643",
              "label": "< 30 Days",
              "displayRange": "7-30 Days"
            },
            {
              "level": "M2",
              "bgColor": "#f53535",
              "label": "< 90 Days",
              "displayRange": "30-90 Days"
            },
            {
              "level": "M1",
              "bgColor": "#c91212",
              "label": ">= 90 Days",
              "displayRange": "90 Days and Above"
            }
          ],
          "trendCalculative": false,
          "xaxisLabel": "Weeks",
          "yaxisLabel": "Days",
          "isAdditionalFilterSupport": false
        },
        "shown": true
      };
      spyOn(helperService, 'groupKpiFromMaster').and.returnValue({ kpiList: kpiListJira });
      const spy = spyOn(component, 'postJiraKpi');
      component.reloadKPI(event);
      expect(spy).toHaveBeenCalled();
    });

    it('should not call postJenkinsKpi or postJiraKpi if kpiList is empty', () => {
      spyOn(component, 'postJenkinsKpi');
      spyOn(component, 'postJiraKpi');
      const event = { kpiDetail: { kpiSource: 'jenkins', groupId: 'group1' } };
      component.reloadKPI(event);
      expect(component.postJenkinsKpi).not.toHaveBeenCalled();
      expect(component.postJiraKpi).not.toHaveBeenCalled();
    });
  });

  describe('postJiraKpi', () => {
    it('should call httpService.postKpi with the correct arguments', () => {
      spyOn(httpService, 'postKpi').and.returnValue(of({}));
      const postData = { kpiList: [{ kpiId: 1 }] };
      component.postJiraKpi(postData, 'jira');
      expect(httpService.postKpi).toHaveBeenCalledWith(postData, 'jira');
    });

    xit('should update loaderJiraArray and jiraKpiData if the response is successful', () => {
      spyOn(httpService, 'postKpi').and.returnValue(of({ 1: { kpiId: 1 } }));
      const postData = { kpiList: [{ kpiId: 1 }] };
      component.loaderJiraArray = [1];
      component.jiraKpiData = {};
      component.postJiraKpi(postData, 'jira');
      expect(component.loaderJiraArray).toEqual([]);
      expect(component.jiraKpiData).toEqual({ 1: { kpiId: 1 } });
    });

    it('should call createAllKpiArray if the response is successful', () => {
      spyOn(httpService, 'postKpi').and.returnValue(of({ 1: { kpiId: 1 } }));
      spyOn(component, 'createAllKpiArray');
      component.postJiraKpi(postData, 'jira');
      expect(component.createAllKpiArray).toHaveBeenCalledWith({ 1: { kpiId: 1 } });
    });

    xit('should update loaderJiraArray if the response is unsuccessful', () => {
      spyOn(httpService, 'postKpi').and.returnValue(of({ error: true }));
      const postData = { kpiList: [{ kpiId: 1 }] };
      component.loaderJiraArray = [1];
      component.postJiraKpi(postData, 'jira');
      expect(component.loaderJiraArray).toEqual([]);
    });

    it('should update kpiLoader to false', () => {
      spyOn(httpService, 'postKpi').and.returnValue(of({}));
      const postData = { kpiList: [{ kpiId: 1 }] };
      component.kpiLoader = true;
      component.postJiraKpi(postData, 'jira');
      expect(component.kpiLoader).toBeFalse();
    });
  });

  describe('ngOnInit', () => {
    // it('should subscribe to mapColorToProjectObs and set colorObj and trendBoxColorObj if x is not empty', () => {
    //   const resp = {
    //     "PSknowHOW _6527af981704342160f43748": {
    //       "nodeName": "PSknowHOW ",
    //       "color": "#079FFF"
    //     }
    //   };
    //   spyOn(service.mapColorToProjectObs, 'subscribe').and.returnValue(of(resp));
    //   component.ngOnInit();
    //   expect(component.colorObj).toEqual({ 1: 'red' });
    //   expect(component.trendBoxColorObj).toEqual({ 1: 'red' });
    // });

    // it('should not set trendBoxColorObj if kpiChartData is empty', () => {
    //   const resp = {
    //     "PSknowHOW _6527af981704342160f43748": {
    //       "nodeName": "PSknowHOW ",
    //       "color": "#079FFF"
    //     }
    //   };
    //   spyOn(service.mapColorToProjectObs, 'subscribe').and.returnValue(of(resp));
    //   component.kpiChartData = {};
    //   component.ngOnInit();
    //   expect(component.trendBoxColorObj).toEqual({});
    // });

    it('should set tooltip and call setGlobalConfigData if getConfigDetails returns a valid response', () => {
      spyOn(httpService, 'getConfigDetails').and.returnValue(of(['test']));
      spyOn(service, 'setGlobalConfigData');
      component.ngOnInit();
      expect(component.tooltip).toEqual(['test']);
      expect(service.setGlobalConfigData).toHaveBeenCalledWith(['test']);
    });

    // it('should subscribe to noProjectsObs and set noProjects and kanbanActivated', () => {
    //   spyOn(service.noProjectsObs, 'subscribe').and.returnValue(of(true));
    //   spyOn(service, 'getSelectedType').and.returnValue('Kanban');
    //   component.ngOnInit();
    //   expect(component.noProjects).toBeTrue();
    //   expect(component.kanbanActivated).toBeTrue();
    // });

    it('should subscribe to getEmptyData and set noTabAccess', () => {
      spyOn(service, 'getEmptyData').and.returnValue(of(true));
      component.ngOnInit();
      expect(component.noTabAccess).toBeTrue();
    });

    it('should subscribe to getEmptyData and set noTabAccess', () => {
      spyOn(service, 'getEmptyData').and.returnValue(of(false));
      component.ngOnInit();
      expect(component.noTabAccess).toBeFalse();
    });
  });

  it('should set dashboard config data',()=>{
    service.setDashConfigData({"others": [
      {
        boardName : 'dora',
        kpis : [{
          kpiId : 'kpi123'
        }]
      }
    ]})
    expect(component.configGlobalData.length).toBeGreaterThan(0);
  })

  it('should set dashboard config data when dora is undefined',()=>{
    service.setDashConfigData({"others": [
      {
        boardName : 'nondora',
        kpis : undefined
      }
    ]})
    expect(component.configGlobalData).toBe(undefined);
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
    component.ngOnInit();
    service.setColorObj(x);
    expect(component.colorObj).toBe(x);
});

it('should work download excel functionality', () => {
  const exportExcelComponent = TestBed.createComponent(ExportExcelComponent).componentInstance;
  spyOn(component.exportExcelComponent, 'downloadExcel')
  component.downloadExcel('kpi122', 'name', true, true);
  expect(exportExcelComponent).toBeDefined();
})

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
        kpiCategory: 'dora',
        groupId: 1
    },
    shown: true
  }];
  const spy = spyOn(helperService, 'groupKpiFromMaster').and.returnValue({ kpiList: kpiListJira });
  const postJiraSpy = spyOn(component, 'postJiraKpi');
  component.groupJiraKpi(['kpi17']);
  expect(postJiraSpy).toHaveBeenCalled();
});

  it('should get kpi comments count', fakeAsync(() => {
    component.filterApplyData = {
      'selectedMap': {
        'project': ["KnowHOW_6360fefc3fa9e175755f0728"]
      },
      'level': 5
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
    spyOn(helperService,'getKpiCommentsHttp').and.returnValue(of(response.data).toPromise());
    component.getKpiCommentsCount('kpi118');
    tick();
    expect(component.kpiCommentsCountObj['kpi118']).toBe(1);
  }));

  it('should give error on post jira kpi ', fakeAsync(() => {
    component.loaderJiraArray = [];
    const errorResponse = {
      success: false,
      error: "Error"
    }
    component.jiraKpiData = {};
    spyOn(httpService, 'postKpi').and.returnValue(of(errorResponse));
    component.postJiraKpi(postData, 'jira');
    expect(component.jiraKpiData['success']).toBe(false);
  }));

  it('should update the kpiSelectedFilterObj and call getChartData method', () => {
    // Arrange
    const event = { key1: ['value1', 'value2'], key2: 'value3' };
    const kpi = {
      kpiId: 'kpi74',
      kpiDetail: {
        aggregationCriteria: 'sum'
      }
    };
    component.kpiSelectedFilterObj['action'] = 'new';
    spyOn(component, 'getChartData');
    spyOn(service, 'setKpiSubFilterObj');
    // Act
    component.handleSelectedOption(event, kpi);

    // Assert
    expect(component.kpiSelectedFilterObj[kpi.kpiId]).toEqual('value3');
    expect(component.getChartData).toHaveBeenCalledWith(kpi.kpiId, component.ifKpiExist(kpi.kpiId), kpi.kpiDetail.aggregationCriteria);
    expect(component.kpiSelectedFilterObj['action']).toBe('update');
    expect(service.setKpiSubFilterObj).toHaveBeenCalledWith(component.kpiSelectedFilterObj);
  });

  it('should push the event value to kpiSelectedFilterObj if event is not an object', () => {
    // Arrange
    const event = 'value';
    const kpi = {
      kpiId: 'kpi118',
      kpiDetail: {
        aggregationCriteria: 'sum'
      }
    };
    component.kpiSelectedFilterObj['kpi118'] = [];
    component.kpiSelectedFilterObj['action'] = 'new';
    spyOn(component, 'getChartData');
    spyOn(service, 'setKpiSubFilterObj');
    // Act
    component.handleSelectedOption(event, kpi);

    // Assert
    expect(component.kpiSelectedFilterObj[kpi.kpiId]).toEqual(['value']);
    expect(component.getChartData).toHaveBeenCalledWith(kpi.kpiId, component.ifKpiExist(kpi.kpiId), kpi.kpiDetail.aggregationCriteria);
    expect(component.kpiSelectedFilterObj['action']).toBe('update');
    expect(service.setKpiSubFilterObj).toHaveBeenCalledWith(component.kpiSelectedFilterObj);
  });

  it('should delete the event key from kpiSelectedFilterObj if event[key] is an empty array', () => {
    // Arrange
    const event = { key1: [] };
    const kpi = {
      kpiId: 'kpi74',
      kpiDetail: {
        aggregationCriteria: 'sum'
      }
    };

    component.kpiSelectedFilterObj['action'] = 'new';
    spyOn(component, 'getChartData');
    spyOn(service, 'setKpiSubFilterObj');

    // Act
    component.handleSelectedOption(event, kpi);

    // Assert
    expect(component.kpiSelectedFilterObj[kpi.kpiId]).toEqual(event);
    expect(component.getChartData).toHaveBeenCalledWith(kpi.kpiId, component.ifKpiExist(kpi.kpiId), kpi.kpiDetail.aggregationCriteria);
    expect(component.kpiSelectedFilterObj['action']).toBe('update');
    expect(service.setKpiSubFilterObj).toHaveBeenCalledWith(component.kpiSelectedFilterObj);
  });

});
