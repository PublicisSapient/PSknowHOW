import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { FilterNewComponent } from './filter-new.component';
import { RouterTestingModule } from '@angular/router/testing';
import { SharedService } from '../../services/shared.service';
import { HelperService } from 'src/app/services/helper.service';
import { GetAuthService } from '../../services/getauth.service';
import { HttpClientModule } from '@angular/common/http';
import { APP_CONFIG, AppConfig } from '../../services/app.config';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpService } from '../../services/http.service';
import { CommonModule, DatePipe } from '@angular/common';
import { MessageService } from 'primeng/api';
import { of, throwError } from 'rxjs';

const boardData = {
  "username": "SUPERADMIN",
  "basicProjectConfigId": "getConfig",
  "scrum": [
    {
      "boardId": 1,
      "boardName": "My KnowHOW",
      "boardSlug": "mydashboard",
      "kpis": [
        {
          "kpiId": "kpi14",
          "kpiName": "Defect Injection Rate",
          "isEnabled": true,
          "order": 1,
          "kpiDetail": {
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
            "isAdditionalFilterSupport": true
          },
          "shown": true
        },
        {
          "kpiId": "kpi82",
          "kpiName": "First Time Pass Rate",
          "isEnabled": true,
          "order": 2,
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi111",
          "kpiName": "Defect Density",
          "isEnabled": true,
          "order": 3,
          "kpiDetail": {
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
            "isAdditionalFilterSupport": true
          },
          "shown": true
        },
        {
          "kpiId": "kpi35",
          "kpiName": "Defect Seepage Rate",
          "isEnabled": true,
          "order": 4,
          "kpiDetail": {
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
            "isAdditionalFilterSupport": true
          },
          "shown": true
        },
        {
          "kpiId": "kpi34",
          "kpiName": "Defect Removal Efficiency",
          "isEnabled": true,
          "order": 5,
          "kpiDetail": {
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
            "isAdditionalFilterSupport": true
          },
          "shown": true
        },
        {
          "kpiId": "kpi37",
          "kpiName": "Defect Rejection Rate",
          "isEnabled": true,
          "order": 6,
          "kpiDetail": {
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
            "isAdditionalFilterSupport": true
          },
          "shown": true
        },
        {
          "kpiId": "kpi28",
          "kpiName": "Defect Count By Priority",
          "isEnabled": true,
          "order": 7,
          "kpiDetail": {
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
            "isAdditionalFilterSupport": true
          },
          "shown": true
        },
        {
          "kpiId": "kpi36",
          "kpiName": "Defect Count By RCA",
          "isEnabled": true,
          "order": 8,
          "kpiDetail": {
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
            "isAdditionalFilterSupport": true
          },
          "shown": true
        },
        {
          "kpiId": "kpi126",
          "kpiName": "Created vs Resolved defects",
          "isEnabled": true,
          "order": 9,
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi42",
          "kpiName": "Regression Automation Coverage",
          "isEnabled": true,
          "order": 10,
          "kpiDetail": {
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
            "isAdditionalFilterSupport": false
          },
          "shown": true
        },
        {
          "kpiId": "kpi16",
          "kpiName": "In-Sprint Automation Coverage",
          "isEnabled": true,
          "order": 11,
          "kpiDetail": {
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
            "isAdditionalFilterSupport": false
          },
          "shown": true
        },
        {
          "kpiId": "kpi17",
          "kpiName": "Unit Test Coverage",
          "isEnabled": true,
          "order": 12,
          "kpiDetail": {
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
            "isAdditionalFilterSupport": false
          },
          "shown": true
        },
        {
          "kpiId": "kpi38",
          "kpiName": "Sonar Violations",
          "isEnabled": true,
          "order": 13,
          "kpiDetail": {
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
            "isAdditionalFilterSupport": false
          },
          "shown": true
        },
        {
          "kpiId": "kpi27",
          "kpiName": "Sonar Tech Debt",
          "isEnabled": true,
          "order": 14,
          "kpiDetail": {
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
            "isAdditionalFilterSupport": false
          },
          "shown": true
        },
        {
          "kpiId": "kpi168",
          "kpiName": "Sonar Code Quality",
          "isEnabled": true,
          "order": 14,
          "kpiDetail": {
            "id": "656347659b6b2f1d4faa9ebe",
            "kpiId": "kpi168",
            "kpiName": "Sonar Code Quality",
            "isDeleted": "False",
            "defaultOrder": 14,
            "kpiUnit": "unit",
            "chartType": "bar-with-y-axis-group",
            "showTrend": true,
            "isPositiveTrend": true,
            "calculateMaturity": true,
            "hideOverallFilter": true,
            "kpiSource": "Sonar",
            "maxValue": "90",
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
          "shown": true
        },
        {
          "kpiId": "kpi70",
          "kpiName": "Test Execution and pass percentage",
          "isEnabled": true,
          "order": 16,
          "kpiDetail": {
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
            "isAdditionalFilterSupport": false
          },
          "shown": true
        },
        {
          "kpiId": "kpi40",
          "kpiName": "Issue Count",
          "isEnabled": true,
          "order": 17,
          "kpiDetail": {
            "id": "64b4ed7acba3c12de16472fc",
            "kpiId": "kpi40",
            "kpiName": "Issue Count",
            "isDeleted": "False",
            "defaultOrder": 17,
            "kpiUnit": "",
            "chartType": "line",
            "showTrend": false,
            "isPositiveTrend": true,
            "calculateMaturity": false,
            "hideOverallFilter": false,
            "kpiSource": "Jira",
            "maxValue": "",
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
            "isAdditionalFilterSupport": true
          },
          "shown": true
        },
        {
          "kpiId": "kpi72",
          "kpiName": "Commitment Reliability",
          "isEnabled": true,
          "order": 18,
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi5",
          "kpiName": "Sprint Predictability",
          "isEnabled": true,
          "order": 19,
          "kpiDetail": {
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
            "showTrend": false,
            "isPositiveTrend": true,
            "calculateMaturity": false,
            "hideOverallFilter": false,
            "kpiSource": "Jira",
            "maxValue": "10",
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
            "isAdditionalFilterSupport": true
          },
          "shown": true
        },
        {
          "kpiId": "kpi39",
          "kpiName": "Sprint Velocity",
          "isEnabled": true,
          "order": 20,
          "kpiDetail": {
            "id": "64b4ed7acba3c12de16472ff",
            "kpiId": "kpi39",
            "kpiName": "Sprint Velocity",
            "isDeleted": "False",
            "defaultOrder": 20,
            "kpiUnit": "SP",
            "chartType": "grouped_column_plus_line",
            "showTrend": false,
            "isPositiveTrend": true,
            "lineLegend": "Sprint Velocity",
            "barLegend": "Last 5 Sprints Average",
            "calculateMaturity": false,
            "hideOverallFilter": false,
            "kpiSource": "Jira",
            "maxValue": "300",
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
            "isAdditionalFilterSupport": true
          },
          "shown": true
        },
        {
          "kpiId": "kpi46",
          "kpiName": "Sprint Capacity Utilization",
          "isEnabled": true,
          "order": 21,
          "kpiDetail": {
            "id": "64b4ed7acba3c12de1647300",
            "kpiId": "kpi46",
            "kpiName": "Sprint Capacity Utilization",
            "isDeleted": "False",
            "defaultOrder": 21,
            "kpiUnit": "Hours",
            "chartType": "grouped_column_plus_line",
            "showTrend": false,
            "isPositiveTrend": true,
            "lineLegend": "Logged",
            "barLegend": "Estimated",
            "calculateMaturity": false,
            "hideOverallFilter": false,
            "kpiSource": "Jira",
            "maxValue": "500",
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
            "isAdditionalFilterSupport": false
          },
          "shown": true
        },
        {
          "kpiId": "kpi8",
          "kpiName": "Code Build Time",
          "isEnabled": true,
          "order": 24,
          "kpiDetail": {
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
            "isAdditionalFilterSupport": false
          },
          "shown": true
        },
        {
          "kpiId": "kpi73",
          "kpiName": "Release Frequency",
          "isEnabled": true,
          "order": 26,
          "kpiDetail": {
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
            "isAdditionalFilterSupport": false
          },
          "shown": true
        },
        {
          "kpiId": "kpi113",
          "kpiName": "Value delivered (Cost of Delay)",
          "isEnabled": true,
          "order": 27,
          "kpiDetail": {
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
            "isAdditionalFilterSupport": false
          },
          "shown": true
        },
        {
          "kpiId": "kpi149",
          "kpiName": "Happiness Index",
          "isEnabled": true,
          "order": 28,
          "kpiDetail": {
            "id": "64b4ed7acba3c12de1647338",
            "kpiId": "kpi149",
            "kpiName": "Happiness Index",
            "isDeleted": "False",
            "defaultOrder": 28,
            "kpiUnit": "",
            "chartType": "line",
            "showTrend": false,
            "isPositiveTrend": true,
            "boxType": "3_column",
            "calculateMaturity": false,
            "hideOverallFilter": false,
            "kpiSource": "Jira",
            "maxValue": "5",
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
          "shown": true
        },
        {
          "kpiId": "kpi153",
          "kpiName": "PI Predictability",
          "isEnabled": true,
          "order": 29,
          "kpiDetail": {
            "id": "64ec311d1ef9f8e4f46ea8d6",
            "kpiId": "kpi153",
            "kpiName": "PI Predictability",
            "isDeleted": "False",
            "defaultOrder": 29,
            "kpiUnit": "",
            "chartType": "multipleline",
            "showTrend": true,
            "isPositiveTrend": true,
            "calculateMaturity": false,
            "hideOverallFilter": false,
            "kpiSource": "Jira",
            "maxValue": "200",
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
          "shown": true
        },
        {
          "kpiId": "kpi164",
          "kpiName": "Scope Churn",
          "isEnabled": true,
          "order": 30,
          "kpiDetail": {
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
          "shown": true
        }
      ],
      "filters": {
        "projectTypeSwitch": {
          "enabled": true,
          "visible": true
        },
        "primaryFilter": {
          "type": "multiSelect",
          "defaultLevel": {
            "labelName": "project",
            "sortBy": null
          }
        },
        "parentFilter": {
          "labelName": "Organization Level"
        },
        "additionalFilters": [
          {
            "type": "multiSelect",
            "defaultLevel": {
              "labelName": "sprint",
              "sortBy": null
            }
          },
          {
            "type": "multiSelect",
            "defaultLevel": {
              "labelName": "sqd",
              "sortBy": null
            }
          }
        ]
      }
    },
    {
      "boardId": 2,
      "boardName": "Speed",
      "boardSlug": "speed",
      "kpis": [
        {
          "kpiId": "kpi40",
          "kpiName": "Issue Count",
          "isEnabled": true,
          "order": 1,
          "kpiDetail": {
            "id": "64b4ed7acba3c12de16472fc",
            "kpiId": "kpi40",
            "kpiName": "Issue Count",
            "isDeleted": "False",
            "defaultOrder": 17,
            "kpiUnit": "",
            "chartType": "line",
            "showTrend": false,
            "isPositiveTrend": true,
            "calculateMaturity": false,
            "hideOverallFilter": false,
            "kpiSource": "Jira",
            "maxValue": "",
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
          "shown": true
        },
        {
          "kpiId": "kpi39",
          "kpiName": "Sprint Velocity",
          "isEnabled": true,
          "order": 2,
          "kpiDetail": {
            "id": "64b4ed7acba3c12de16472ff",
            "kpiId": "kpi39",
            "kpiName": "Sprint Velocity",
            "isDeleted": "False",
            "defaultOrder": 20,
            "kpiUnit": "SP",
            "chartType": "grouped_column_plus_line",
            "showTrend": false,
            "isPositiveTrend": true,
            "lineLegend": "Sprint Velocity",
            "barLegend": "Last 5 Sprints Average",
            "calculateMaturity": false,
            "hideOverallFilter": false,
            "kpiSource": "Jira",
            "maxValue": "300",
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
          "shown": true
        },
        {
          "kpiId": "kpi72",
          "kpiName": "Commitment Reliability",
          "isEnabled": true,
          "order": 3,
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi5",
          "kpiName": "Sprint Predictability",
          "isEnabled": true,
          "order": 4,
          "kpiDetail": {
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
            "showTrend": false,
            "isPositiveTrend": true,
            "calculateMaturity": false,
            "hideOverallFilter": false,
            "kpiSource": "Jira",
            "maxValue": "10",
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
          "shown": true
        },
        {
          "kpiId": "kpi46",
          "kpiName": "Sprint Capacity Utilization",
          "isEnabled": true,
          "order": 5,
          "kpiDetail": {
            "id": "64b4ed7acba3c12de1647300",
            "kpiId": "kpi46",
            "kpiName": "Sprint Capacity Utilization",
            "isDeleted": "False",
            "defaultOrder": 21,
            "kpiUnit": "Hours",
            "chartType": "grouped_column_plus_line",
            "showTrend": false,
            "isPositiveTrend": true,
            "lineLegend": "Logged",
            "barLegend": "Estimated",
            "calculateMaturity": false,
            "hideOverallFilter": false,
            "kpiSource": "Jira",
            "maxValue": "500",
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
          "shown": true
        },
        {
          "kpiId": "kpi8",
          "kpiName": "Code Build Time",
          "isEnabled": true,
          "order": 8,
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi164",
          "kpiName": "Scope Churn",
          "isEnabled": true,
          "order": 9,
          "kpiDetail": {
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
          "shown": true
        }
      ],
      "filters": {
        "projectTypeSwitch": {
          "enabled": true,
          "visible": true
        },
        "primaryFilter": {
          "type": "multiSelect",
          "defaultLevel": {
            "labelName": "project",
            "sortBy": null
          }
        },
        "parentFilter": {
          "labelName": "Organization Level"
        },
        "additionalFilters": [
          {
            "type": "multiSelect",
            "defaultLevel": {
              "labelName": "sprint",
              "sortBy": null
            }
          },
          {
            "type": "multiSelect",
            "defaultLevel": {
              "labelName": "sqd",
              "sortBy": null
            }
          }
        ]
      }
    },
    {
      "boardId": 3,
      "boardName": "Quality",
      "boardSlug": "quality",
      "kpis": [
        {
          "kpiId": "kpi14",
          "kpiName": "Defect Injection Rate",
          "isEnabled": true,
          "order": 1,
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi82",
          "kpiName": "First Time Pass Rate",
          "isEnabled": true,
          "order": 2,
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi111",
          "kpiName": "Defect Density",
          "isEnabled": true,
          "order": 3,
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi35",
          "kpiName": "Defect Seepage Rate",
          "isEnabled": true,
          "order": 4,
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi34",
          "kpiName": "Defect Removal Efficiency",
          "isEnabled": true,
          "order": 5,
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi37",
          "kpiName": "Defect Rejection Rate",
          "isEnabled": true,
          "order": 6,
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi28",
          "kpiName": "Defect Count By Priority",
          "isEnabled": true,
          "order": 7,
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi36",
          "kpiName": "Defect Count By RCA",
          "isEnabled": true,
          "order": 8,
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi126",
          "kpiName": "Created vs Resolved defects",
          "isEnabled": true,
          "order": 9,
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi42",
          "kpiName": "Regression Automation Coverage",
          "isEnabled": true,
          "order": 10,
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi16",
          "kpiName": "In-Sprint Automation Coverage",
          "isEnabled": true,
          "order": 11,
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi17",
          "kpiName": "Unit Test Coverage",
          "isEnabled": true,
          "order": 12,
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi38",
          "kpiName": "Sonar Violations",
          "isEnabled": true,
          "order": 13,
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi27",
          "kpiName": "Sonar Tech Debt",
          "isEnabled": true,
          "order": 14,
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi168",
          "kpiName": "Sonar Code Quality",
          "isEnabled": true,
          "order": 15,
          "kpiDetail": {
            "id": "656347659b6b2f1d4faa9ebe",
            "kpiId": "kpi168",
            "kpiName": "Sonar Code Quality",
            "isDeleted": "False",
            "defaultOrder": 14,
            "kpiUnit": "unit",
            "chartType": "bar-with-y-axis-group",
            "showTrend": true,
            "isPositiveTrend": true,
            "calculateMaturity": true,
            "hideOverallFilter": true,
            "kpiSource": "Sonar",
            "maxValue": "90",
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
          "shown": true
        },
        {
          "kpiId": "kpi70",
          "kpiName": "Test Execution and pass percentage",
          "isEnabled": true,
          "order": 16,
          "kpiDetail": {
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
          "shown": true
        }
      ],
      "filters": {
        "projectTypeSwitch": {
          "enabled": true,
          "visible": true
        },
        "primaryFilter": {
          "type": "multiSelect",
          "defaultLevel": {
            "labelName": "project",
            "sortBy": null
          }
        },
        "parentFilter": {
          "labelName": "Organization Level"
        },
        "additionalFilters": [
          {
            "type": "multiSelect",
            "defaultLevel": {
              "labelName": "sprint",
              "sortBy": null
            }
          },
          {
            "type": "multiSelect",
            "defaultLevel": {
              "labelName": "sqd",
              "sortBy": null
            }
          }
        ]
      }
    },
    {
      "boardId": 4,
      "boardName": "Value",
      "boardSlug": "value",
      "kpis": [
        {
          "kpiId": "kpi73",
          "kpiName": "Release Frequency",
          "isEnabled": true,
          "order": 2,
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi113",
          "kpiName": "Value delivered (Cost of Delay)",
          "isEnabled": true,
          "order": 3,
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi153",
          "kpiName": "PI Predictability",
          "isEnabled": true,
          "order": 4,
          "kpiDetail": {
            "id": "64ec311d1ef9f8e4f46ea8d6",
            "kpiId": "kpi153",
            "kpiName": "PI Predictability",
            "isDeleted": "False",
            "defaultOrder": 29,
            "kpiUnit": "",
            "chartType": "multipleline",
            "showTrend": true,
            "isPositiveTrend": true,
            "calculateMaturity": false,
            "hideOverallFilter": false,
            "kpiSource": "Jira",
            "maxValue": "200",
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
          "shown": true
        }
      ],
      "filters": {
        "projectTypeSwitch": {
          "enabled": true,
          "visible": true
        },
        "primaryFilter": {
          "type": "multiSelect",
          "defaultLevel": {
            "labelName": "project",
            "sortBy": null
          }
        },
        "parentFilter": {
          "labelName": "Organization Level"
        }
      }
    },
    {
      "boardId": 5,
      "boardName": "Iteration",
      "boardSlug": "iteration",
      "kpis": [
        {
          "kpiId": "kpi121",
          "kpiName": "Capacity",
          "isEnabled": true,
          "order": 0,
          "subCategoryBoard": "Iteration Review",
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi120",
          "kpiName": "Iteration Commitment",
          "isEnabled": true,
          "order": 1,
          "subCategoryBoard": "Iteration Review",
          "kpiDetail": {
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
            "kpiHeight": 100,
            "trendCalculative": false,
            "xaxisLabel": "",
            "yaxisLabel": "",
            "isAdditionalFilterSupport": false
          },
          "shown": true
        },
        {
          "kpiId": "kpi128",
          "kpiName": "Planned Work Status",
          "isEnabled": true,
          "order": 2,
          "subCategoryBoard": "Iteration Review",
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi145",
          "kpiName": "Dev Completion Status",
          "isEnabled": true,
          "order": 3,
          "subCategoryBoard": "Iteration Review",
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi119",
          "kpiName": "Work Remaining",
          "isEnabled": true,
          "order": 4,
          "subCategoryBoard": "Iteration Review",
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi134",
          "kpiName": "Unplanned Work Status",
          "isEnabled": true,
          "order": 5,
          "subCategoryBoard": "Iteration Review",
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi122",
          "kpiName": "Closure Possible Today",
          "isEnabled": true,
          "order": 6,
          "subCategoryBoard": "Iteration Review",
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi123",
          "kpiName": "Issues likely to Spill",
          "isEnabled": true,
          "order": 7,
          "subCategoryBoard": "Iteration Review",
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi125",
          "kpiName": "Iteration Burnup",
          "isEnabled": true,
          "order": 8,
          "subCategoryBoard": "Iteration Progress",
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi154",
          "kpiName": "Daily Standup View",
          "isEnabled": true,
          "order": 8,
          "subCategoryBoard": "Daily Standup",
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi131",
          "kpiName": "Wastage",
          "isEnabled": true,
          "order": 9,
          "subCategoryBoard": "Iteration Review",
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi135",
          "kpiName": "First Time Pass Rate",
          "isEnabled": true,
          "order": 11,
          "subCategoryBoard": "Iteration Review",
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi133",
          "kpiName": "Quality Status",
          "isEnabled": true,
          "order": 12,
          "subCategoryBoard": "Iteration Review",
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi136",
          "kpiName": "Defect Count by Status",
          "isEnabled": true,
          "order": 13,
          "subCategoryBoard": "Iteration Review",
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi132",
          "kpiName": "Defect Count by RCA",
          "isEnabled": true,
          "order": 14,
          "subCategoryBoard": "Iteration Review",
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi140",
          "kpiName": "Defect Count by Priority",
          "isEnabled": true,
          "order": 15,
          "subCategoryBoard": "Iteration Review",
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi124",
          "kpiName": "Estimation Hygiene",
          "isEnabled": true,
          "order": 21,
          "subCategoryBoard": "Iteration Review",
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi75",
          "kpiName": "Estimate vs Actual",
          "isEnabled": true,
          "order": 22,
          "subCategoryBoard": "Iteration Review",
          "kpiDetail": {
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
            "isAdditionalFilterSupport": false
          },
          "shown": true
        }
      ],
      "filters": {
        "projectTypeSwitch": {
          "enabled": false,
          "visible": true
        },
        "primaryFilter": {
          "type": "singleSelect",
          "defaultLevel": {
            "labelName": "sprint",
            "sortBy": "sprintState"
          }
        },
        "parentFilter": {
          "labelName": "Project",
          "emittedLevel": "sprint"
        },
        "additionalFilters": [
          {
            "type": "multiSelect",
            "defaultLevel": {
              "labelName": "sqd",
              "sortBy": null
            }
          }
        ]
      }
    },
    {
      "boardId": 6,
      "boardName": "Developer",
      "boardSlug": "developer",
      "kpis": [
        {
          "kpiId": "kpi84",
          "kpiName": "Mean Time To Merge",
          "isEnabled": true,
          "order": 22,
          "kpiDetail": {
            "id": "65793ddb127be336160bc0d3",
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
            "combinedKpiSource": "Bitbucket/AzureRepository/GitHub/GitLab",
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
                    "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70713477/Developer+Mean+time+to+Merge"
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
          "shown": true
        },
        {
          "kpiId": "kpi11",
          "kpiName": "Check-Ins & Merge Requests",
          "isEnabled": true,
          "order": 23,
          "kpiDetail": {
            "id": "65793ddb127be336160bc0d4",
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
            "combinedKpiSource": "Bitbucket/AzureRepository/GitHub/GitLab",
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
                    "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70451310/Developer+No.+of+Check-ins+and+Merge+Requests"
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
            "isAdditionalFilterSupport": false
          },
          "shown": true
        }
      ],
      "filters": {
        "projectTypeSwitch": {
          "enabled": true,
          "visible": true
        },
        "primaryFilter": {
          "type": "singleSelect",
          "defaultLevel": {
            "labelName": "project",
            "sortBy": null
          }
        },
        "parentFilter": null,
        "additionalFilters": [
          {
            "type": "singleSelect",
            "defaultLevel": {
              "labelName": "branch",
              "sortBy": null
            }
          },
          {
            "type": "singleSelect",
            "defaultLevel": {
              "labelName": "developer",
              "sortBy": null
            }
          }
        ]
      }
    }
  ],
  "kanban": [
    {
      "boardId": 7,
      "boardName": "My KnowHOW",
      "boardSlug": "mydashboard",
      "kpis": [
        {
          "kpiId": "kpi55",
          "kpiName": "Ticket Open vs Closed rate by type",
          "isEnabled": true,
          "order": 1,
          "kpiDetail": {
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
            "isAdditionalFilterSupport": true
          },
          "shown": true
        },
        {
          "kpiId": "kpi54",
          "kpiName": "Ticket Open vs Closed rate by Priority",
          "isEnabled": true,
          "order": 2,
          "kpiDetail": {
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
            "isAdditionalFilterSupport": true
          },
          "shown": true
        },
        {
          "kpiId": "kpi50",
          "kpiName": "Net Open Ticket Count by Priority",
          "isEnabled": true,
          "order": 3,
          "kpiDetail": {
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
            "isAdditionalFilterSupport": true
          },
          "shown": true
        },
        {
          "kpiId": "kpi51",
          "kpiName": "Net Open Ticket Count By RCA",
          "isEnabled": true,
          "order": 4,
          "kpiDetail": {
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
            "isAdditionalFilterSupport": true
          },
          "shown": true
        },
        {
          "kpiId": "kpi48",
          "kpiName": "Net Open Ticket By Status",
          "isEnabled": true,
          "order": 5,
          "kpiDetail": {
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
            "isAdditionalFilterSupport": true
          },
          "shown": true
        },
        {
          "kpiId": "kpi997",
          "kpiName": "Open Ticket Ageing By Priority",
          "isEnabled": true,
          "order": 6,
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi63",
          "kpiName": "Regression Automation Coverage",
          "isEnabled": true,
          "order": 7,
          "kpiDetail": {
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
            "isAdditionalFilterSupport": false
          },
          "shown": true
        },
        {
          "kpiId": "kpi62",
          "kpiName": "Unit Test Coverage",
          "isEnabled": true,
          "order": 8,
          "kpiDetail": {
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
            "isAdditionalFilterSupport": false
          },
          "shown": true
        },
        {
          "kpiId": "kpi64",
          "kpiName": "Sonar Violations",
          "isEnabled": true,
          "order": 9,
          "kpiDetail": {
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
            "isAdditionalFilterSupport": false
          },
          "shown": true
        },
        {
          "kpiId": "kpi67",
          "kpiName": "Sonar Tech Debt",
          "isEnabled": true,
          "order": 10,
          "kpiDetail": {
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
            "isAdditionalFilterSupport": false
          },
          "shown": true
        },
        {
          "kpiId": "kpi71",
          "kpiName": "Test Execution and pass percentage",
          "isEnabled": true,
          "order": 11,
          "kpiDetail": {
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
            "isAdditionalFilterSupport": false
          },
          "shown": true
        },
        {
          "kpiId": "kpi49",
          "kpiName": "Ticket Velocity",
          "isEnabled": true,
          "order": 12,
          "kpiDetail": {
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
            "isAdditionalFilterSupport": true
          },
          "shown": true
        },
        {
          "kpiId": "kpi58",
          "kpiName": "Team Capacity",
          "isEnabled": true,
          "order": 13,
          "kpiDetail": {
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
            "isAdditionalFilterSupport": false
          },
          "shown": true
        },
        {
          "kpiId": "kpi66",
          "kpiName": "Code Build Time",
          "isEnabled": true,
          "order": 14,
          "kpiDetail": {
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
            "isAdditionalFilterSupport": false
          },
          "shown": true
        },
        {
          "kpiId": "kpi53",
          "kpiName": "Lead Time",
          "isEnabled": true,
          "order": 16,
          "kpiDetail": {
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
            "isAdditionalFilterSupport": false
          },
          "shown": true
        },
        {
          "kpiId": "kpi74",
          "kpiName": "Release Frequency",
          "isEnabled": true,
          "order": 17,
          "kpiDetail": {
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
            "isAdditionalFilterSupport": false
          },
          "shown": true
        },
        {
          "kpiId": "kpi114",
          "kpiName": "Value delivered (Cost of Delay)",
          "isEnabled": true,
          "order": 18,
          "kpiDetail": {
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
            "isAdditionalFilterSupport": false
          },
          "shown": true
        }
      ],
      "filters": {
        "projectTypeSwitch": {
          "enabled": true,
          "visible": true
        },
        "primaryFilter": {
          "type": "multiSelect",
          "defaultLevel": {
            "labelName": "project",
            "sortBy": null
          }
        },
        "parentFilter": {
          "labelName": "Organization Level"
        },
        "additionalFilters": [
          {
            "type": "multiSelect",
            "defaultLevel": {
              "labelName": "sqd",
              "sortBy": null
            }
          }
        ]
      }
    },
    {
      "boardId": 8,
      "boardName": "Speed",
      "boardSlug": "speed",
      "kpis": [
        {
          "kpiId": "kpi49",
          "kpiName": "Ticket Velocity",
          "isEnabled": true,
          "order": 1,
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi58",
          "kpiName": "Team Capacity",
          "isEnabled": true,
          "order": 2,
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi66",
          "kpiName": "Code Build Time",
          "isEnabled": true,
          "order": 3,
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi53",
          "kpiName": "Lead Time",
          "isEnabled": true,
          "order": 5,
          "kpiDetail": {
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
          "shown": true
        }
      ],
      "filters": {
        "projectTypeSwitch": {
          "enabled": true,
          "visible": true
        },
        "primaryFilter": {
          "type": "multiSelect",
          "defaultLevel": {
            "labelName": "project",
            "sortBy": null
          }
        },
        "parentFilter": {
          "labelName": "Organization Level"
        },
        "additionalFilters": [
          {
            "type": "multiSelect",
            "defaultLevel": {
              "labelName": "sqd",
              "sortBy": null
            }
          }
        ]
      }
    },
    {
      "boardId": 9,
      "boardName": "Quality",
      "boardSlug": "quality",
      "kpis": [
        {
          "kpiId": "kpi55",
          "kpiName": "Ticket Open vs Closed rate by type",
          "isEnabled": true,
          "order": 1,
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi54",
          "kpiName": "Ticket Open vs Closed rate by Priority",
          "isEnabled": true,
          "order": 2,
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi50",
          "kpiName": "Net Open Ticket Count by Priority",
          "isEnabled": true,
          "order": 3,
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi51",
          "kpiName": "Net Open Ticket Count By RCA",
          "isEnabled": true,
          "order": 4,
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi48",
          "kpiName": "Net Open Ticket By Status",
          "isEnabled": true,
          "order": 5,
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi997",
          "kpiName": "Open Ticket Ageing By Priority",
          "isEnabled": true,
          "order": 6,
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi63",
          "kpiName": "Regression Automation Coverage",
          "isEnabled": true,
          "order": 7,
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi62",
          "kpiName": "Unit Test Coverage",
          "isEnabled": true,
          "order": 8,
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi64",
          "kpiName": "Sonar Violations",
          "isEnabled": true,
          "order": 9,
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi67",
          "kpiName": "Sonar Tech Debt",
          "isEnabled": true,
          "order": 10,
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi71",
          "kpiName": "Test Execution and pass percentage",
          "isEnabled": true,
          "order": 11,
          "kpiDetail": {
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
          "shown": true
        }
      ],
      "filters": {
        "projectTypeSwitch": {
          "enabled": true,
          "visible": true
        },
        "primaryFilter": {
          "type": "multiSelect",
          "defaultLevel": {
            "labelName": "project",
            "sortBy": null
          }
        },
        "parentFilter": {
          "labelName": "Organization Level"
        },
        "additionalFilters": [
          {
            "type": "multiSelect",
            "defaultLevel": {
              "labelName": "sqd",
              "sortBy": null
            }
          }
        ]
      }
    },
    {
      "boardId": 10,
      "boardName": "Value",
      "boardSlug": "value",
      "kpis": [
        {
          "kpiId": "kpi74",
          "kpiName": "Release Frequency",
          "isEnabled": true,
          "order": 1,
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi114",
          "kpiName": "Value delivered (Cost of Delay)",
          "isEnabled": true,
          "order": 2,
          "kpiDetail": {
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
          "shown": true
        }
      ],
      "filters": {
        "projectTypeSwitch": {
          "enabled": true,
          "visible": true
        },
        "primaryFilter": {
          "type": "multiSelect",
          "defaultLevel": {
            "labelName": "project",
            "sortBy": null
          }
        },
        "parentFilter": {
          "labelName": "Organization Level"
        },
        "additionalFilters": [
          {
            "type": "multiSelect",
            "defaultLevel": {
              "labelName": "sqd",
              "sortBy": null
            }
          }
        ]
      }
    },
    {
      "boardId": 11,
      "boardName": "Iteration",
      "boardSlug": "iteration",
      "kpis": [],
      "filters": {
        "projectTypeSwitch": {
          "enabled": false,
          "visible": true
        },
        "primaryFilter": {
          "type": "singleSelect",
          "defaultLevel": {
            "labelName": "sprint",
            "sortBy": "sprintState"
          }
        },
        "parentFilter": {
          "labelName": "Project",
          "emittedLevel": "sprint"
        },
        "additionalFilters": [
          {
            "type": "multiSelect",
            "defaultLevel": {
              "labelName": "sqd",
              "sortBy": null
            }
          }
        ]
      }
    },
    {
      "boardId": 12,
      "boardName": "Developer",
      "boardSlug": "developer",
      "kpis": [
        {
          "kpiId": "kpi65",
          "kpiName": "Number of Check-ins",
          "isEnabled": true,
          "order": 15,
          "kpiDetail": {
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
            "isAdditionalFilterSupport": false
          },
          "shown": true
        }
      ],
      "filters": {
        "projectTypeSwitch": {
          "enabled": true,
          "visible": true
        },
        "primaryFilter": {
          "type": "singleSelect",
          "defaultLevel": {
            "labelName": "project",
            "sortBy": null
          }
        },
        "parentFilter": null,
        "additionalFilters": [
          {
            "type": "singleSelect",
            "defaultLevel": {
              "labelName": "branch",
              "sortBy": null
            }
          },
          {
            "type": "singleSelect",
            "defaultLevel": {
              "labelName": "developer",
              "sortBy": null
            }
          }
        ]
      }
    }
  ],
  "others": [
    {
      "boardId": 13,
      "boardName": "Release",
      "boardSlug": "release",
      "kpis": [
        {
          "kpiId": "kpi141",
          "kpiName": "Defect Count by Status",
          "isEnabled": true,
          "order": 1,
          "subCategoryBoard": "Quality",
          "kpiDetail": {
            "id": "65793ddb127be336160bc0fe",
            "kpiId": "kpi141",
            "kpiName": "Defect Count by Status",
            "isDeleted": "False",
            "defaultOrder": 1,
            "kpiCategory": "Release",
            "kpiSubCategory": "Defects",
            "kpiUnit": "Count",
            "chartType": "pieChart",
            "showTrend": false,
            "isPositiveTrend": true,
            "boxType": "chart",
            "calculateMaturity": false,
            "hideOverallFilter": false,
            "kpiSource": "Jira",
            "combinedKpiSource": "Jira/Azure",
            "maxValue": "",
            "kanban": false,
            "groupId": 9,
            "kpiInfo": {
              "definition": "It shows the breakup of all defects tagged to a release based on Status. The breakup is shown in terms of count & percentage.",
              "details": [
                {
                  "type": "link",
                  "kpiLinkDetail": {
                    "text": "Detailed Information at",
                    "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/79986689/Release+Defect+count+by+Status"
                  }
                }
              ]
            },
            "kpiFilter": "",
            "trendCalculative": false,
            "xaxisLabel": "",
            "yaxisLabel": "",
            "isAdditionalFilterSupport": false
          },
          "shown": true
        },
        {
          "kpiId": "kpi150",
          "kpiName": "Release Burnup",
          "isEnabled": true,
          "order": 1,
          "subCategoryBoard": "Speed",
          "kpiDetail": {
            "id": "65793ddb127be336160bc10b",
            "kpiId": "kpi150",
            "kpiName": "Release Burnup",
            "isDeleted": "False",
            "defaultOrder": 1,
            "kpiCategory": "Release",
            "kpiSubCategory": "Progress",
            "kpiUnit": "Count",
            "chartType": "CumulativeMultilineChart",
            "showTrend": false,
            "isPositiveTrend": true,
            "boxType": "chart",
            "calculateMaturity": false,
            "hideOverallFilter": false,
            "kpiSource": "Jira",
            "combinedKpiSource": "Jira/Azure",
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
                    "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70484023/Release+Release+Burnup"
                  }
                },
                {
                  "type": "link",
                  "kpiLinkDetail": {
                    "text": "Detailed Information at",
                    "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70484023/Release+Release+Burnup"
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
          "shown": true
        },
        {
          "kpiId": "kpi178",
          "kpiName": "Defect Count By",
          "isEnabled": true,
          "order": 1,
          "subCategoryBoard": "Quality",
          "kpiDetail": {
            "id": "665f0e93bc80f461490c646a",
            "kpiId": "kpi178",
            "kpiName": "Defect Count By",
            "isDeleted": "False",
            "defaultOrder": 1,
            "kpiCategory": "Release",
            "kpiSubCategory": "Defects",
            "chartType": "chartWithFilter",
            "showTrend": false,
            "boxType": "chart",
            "calculateMaturity": false,
            "hideOverallFilter": false,
            "kpiSource": "Jira",
            "combinedKpiSource": "Jira/Azure",
            "kanban": false,
            "groupId": 9,
            "kpiInfo": {
              "definition": "It shows the breakup of all defects tagged to a release grouped by Status, Priority, or RCA.",
              "details": [
                {
                  "type": "link",
                  "kpiLinkDetail": {
                    "text": "Detailed Information at",
                    "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/144146433/Release+Defect+count+by"
                  }
                }
              ]
            },
            "kpiFilter": "",
            "trendCalculative": false,
            "isAdditionalFilterSupport": false
          },
          "shown": true
        },
        {
          "kpiId": "kpi179",
          "kpiName": "Release Plan",
          "isEnabled": true,
          "order": 1,
          "subCategoryBoard": "Speed",
          "kpiDetail": {
            "id": "666309170d4ae02ee5cc4574",
            "kpiId": "kpi179",
            "kpiName": "Release Plan",
            "isDeleted": "False",
            "defaultOrder": 1,
            "kpiCategory": "Release",
            "kpiSubCategory": "Progress",
            "kpiUnit": "Count",
            "chartType": "CumulativeMultilineChart",
            "showTrend": false,
            "isPositiveTrend": true,
            "boxType": "chart",
            "calculateMaturity": false,
            "hideOverallFilter": false,
            "kpiSource": "Jira",
            "combinedKpiSource": "Jira/Azure",
            "maxValue": "",
            "kanban": false,
            "groupId": 9,
            "kpiInfo": {
              "definition": "Displays the cumulative daily planned dues of the release based on the due dates of work items within the release scope.\n\nAdditionally, it provides an overview of the entire release scope.",
              "details": [
                {
                  "type": "link",
                  "kpiLinkDetail": {
                    "text": "Detailed Information at",
                    "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/147652609/Release+Release+Plan"
                  }
                }
              ]
            },
            "kpiFilter": "",
            "kpiWidth": 100,
            "trendCalculative": false,
            "xaxisLabel": "",
            "yaxisLabel": "Count",
            "isAdditionalFilterSupport": false
          },
          "shown": true
        },
        {
          "kpiId": "kpi142",
          "kpiName": "Defect Count by RCA",
          "isEnabled": true,
          "order": 2,
          "subCategoryBoard": "Quality",
          "kpiDetail": {
            "id": "65793ddb127be336160bc0ff",
            "kpiId": "kpi142",
            "kpiName": "Defect Count by RCA",
            "isDeleted": "False",
            "defaultOrder": 2,
            "kpiCategory": "Release",
            "kpiSubCategory": "Defects",
            "kpiUnit": "Count",
            "chartType": "stackedColumn",
            "showTrend": false,
            "isPositiveTrend": true,
            "boxType": "chart",
            "calculateMaturity": false,
            "hideOverallFilter": false,
            "kpiSource": "Jira",
            "combinedKpiSource": "Jira/Azure",
            "maxValue": "",
            "kanban": false,
            "groupId": 9,
            "kpiInfo": {
              "definition": "It shows the breakup of all defects tagged to a release based on RCA. The breakup is shown in terms of count at different testing phases.",
              "details": [
                {
                  "type": "link",
                  "kpiLinkDetail": {
                    "text": "Detailed Information at",
                    "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/79953937/Release+Defect+count+by+RCA"
                  }
                }
              ]
            },
            "kpiFilter": "radioButton",
            "trendCalculative": false,
            "xaxisLabel": "Test Phase",
            "yaxisLabel": "Count",
            "isAdditionalFilterSupport": false
          },
          "shown": true
        },
        {
          "kpiId": "kpi143",
          "kpiName": "Defect Count by Assignee",
          "isEnabled": true,
          "order": 3,
          "subCategoryBoard": "Quality",
          "kpiDetail": {
            "id": "65793ddb127be336160bc100",
            "kpiId": "kpi143",
            "kpiName": "Defect Count by Assignee",
            "isDeleted": "False",
            "defaultOrder": 3,
            "kpiCategory": "Release",
            "kpiSubCategory": "Defects",
            "kpiUnit": "Count",
            "chartType": "pieChart",
            "showTrend": false,
            "isPositiveTrend": true,
            "boxType": "chart",
            "calculateMaturity": false,
            "hideOverallFilter": false,
            "kpiSource": "Jira",
            "combinedKpiSource": "Jira/Azure",
            "maxValue": "",
            "kanban": false,
            "groupId": 9,
            "kpiInfo": {
              "definition": "It shows the breakup of all defects tagged to a release based on Assignee. The breakup is shown in terms of count & percentage.",
              "details": [
                {
                  "type": "link",
                  "kpiLinkDetail": {
                    "text": "Detailed Information at",
                    "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/79691782/Release+Defect+count+by+Assignee"
                  }
                }
              ]
            },
            "kpiFilter": "radioButton",
            "trendCalculative": false,
            "xaxisLabel": "",
            "yaxisLabel": "",
            "isAdditionalFilterSupport": false
          },
          "shown": true
        },
        {
          "kpiId": "kpi144",
          "kpiName": "Defect Count by Priority",
          "isEnabled": true,
          "order": 4,
          "subCategoryBoard": "Quality",
          "kpiDetail": {
            "id": "65793ddb127be336160bc101",
            "kpiId": "kpi144",
            "kpiName": "Defect Count by Priority",
            "isDeleted": "False",
            "defaultOrder": 4,
            "kpiCategory": "Release",
            "kpiSubCategory": "Defects",
            "kpiUnit": "Count",
            "chartType": "pieChart",
            "showTrend": false,
            "isPositiveTrend": true,
            "boxType": "chart",
            "calculateMaturity": false,
            "hideOverallFilter": false,
            "kpiSource": "Jira",
            "combinedKpiSource": "Jira/Azure",
            "maxValue": "",
            "kanban": false,
            "groupId": 9,
            "kpiInfo": {
              "definition": "It shows the breakup of all defects tagged to a release based on Priority. The breakup is shown in terms of count & percentage.",
              "details": [
                {
                  "type": "link",
                  "kpiLinkDetail": {
                    "text": "Detailed Information at",
                    "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/79953921/Release+Defect+count+by+Priority"
                  }
                }
              ]
            },
            "kpiFilter": "radioButton",
            "trendCalculative": false,
            "xaxisLabel": "",
            "yaxisLabel": "",
            "isAdditionalFilterSupport": false
          },
          "shown": true
        },
        {
          "kpiId": "kpi147",
          "kpiName": "Release Progress",
          "isEnabled": true,
          "order": 5,
          "subCategoryBoard": "Speed",
          "kpiDetail": {
            "id": "65793ddb127be336160bc104",
            "kpiId": "kpi147",
            "kpiName": "Release Progress",
            "isDeleted": "False",
            "defaultOrder": 5,
            "kpiCategory": "Release",
            "kpiSubCategory": "Progress",
            "kpiUnit": "Count",
            "chartType": "horizontalPercentBarChart",
            "showTrend": false,
            "isPositiveTrend": true,
            "boxType": "chart",
            "calculateMaturity": false,
            "hideOverallFilter": false,
            "kpiSource": "Jira",
            "combinedKpiSource": "Jira/Azure",
            "maxValue": "",
            "kanban": false,
            "groupId": 9,
            "kpiInfo": {
              "definition": "It shows the breakup by status of issues tagged to a release. The breakup is based on both issue count and story points",
              "details": [
                {
                  "type": "link",
                  "kpiLinkDetail": {
                    "text": "Detailed Information at",
                    "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/79757314/Release+Release+Progress"
                  }
                }
              ]
            },
            "kpiFilter": "dropDown",
            "kpiWidth": 100,
            "trendCalculative": false,
            "xaxisLabel": "",
            "yaxisLabel": "",
            "isAdditionalFilterSupport": false
          },
          "shown": true
        },
        {
          "kpiId": "kpi165",
          "kpiName": "Epic Progress",
          "isEnabled": true,
          "order": 5,
          "subCategoryBoard": "Value",
          "kpiDetail": {
            "id": "65793ddc127be336160bc119",
            "kpiId": "kpi165",
            "kpiName": "Epic Progress",
            "isDeleted": "False",
            "defaultOrder": 5,
            "kpiCategory": "Release",
            "kpiSubCategory": "Epics",
            "kpiUnit": "Count",
            "chartType": "horizontalPercentBarChart",
            "showTrend": false,
            "isPositiveTrend": true,
            "boxType": "chart",
            "calculateMaturity": false,
            "hideOverallFilter": false,
            "kpiSource": "Jira",
            "combinedKpiSource": "Jira/Azure",
            "maxValue": "",
            "kanban": false,
            "groupId": 9,
            "kpiInfo": {
              "definition": "It depicts the progress of each epic in a release in terms of total count and %age completion.",
              "details": [
                {
                  "type": "link",
                  "kpiLinkDetail": {
                    "text": "Detailed Information at",
                    "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/79986705/Release+Epic+Progress"
                  }
                }
              ]
            },
            "kpiFilter": "multiSelectDropDown",
            "kpiWidth": 100,
            "trendCalculative": false,
            "xaxisLabel": "",
            "yaxisLabel": "",
            "isAdditionalFilterSupport": false,
            "kpiHeight": 100
          },
          "shown": true
        },
        {
          "kpiId": "kpi163",
          "kpiName": "Defect by Testing Phase",
          "isEnabled": true,
          "order": 7,
          "subCategoryBoard": "Quality",
          "kpiDetail": {
            "id": "65793ddc127be336160bc118",
            "kpiId": "kpi163",
            "kpiName": "Defect by Testing Phase",
            "isDeleted": "False",
            "defaultOrder": 7,
            "kpiCategory": "Release",
            "kpiSubCategory": "Defects",
            "kpiUnit": "Count",
            "chartType": "horizontalPercentBarChart",
            "showTrend": false,
            "isPositiveTrend": true,
            "boxType": "chart",
            "calculateMaturity": false,
            "hideOverallFilter": false,
            "kpiSource": "Jira",
            "combinedKpiSource": "Jira/Azure",
            "maxValue": "",
            "kanban": false,
            "groupId": 9,
            "kpiInfo": {
              "definition": " It gives a breakup of escaped defects by testing phase",
              "details": [
                {
                  "type": "link",
                  "kpiLinkDetail": {
                    "text": "Detailed Information at",
                    "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/98140473/Release+Defect+count+by+Testing+phase"
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
            "xaxisLabel": "",
            "yaxisLabel": "",
            "isAdditionalFilterSupport": false
          },
          "shown": true
        }
      ],
      "filters": {
        "projectTypeSwitch": {
          "enabled": false,
          "visible": true
        },
        "primaryFilter": {
          "type": "singleSelect",
          "defaultLevel": {
            "labelName": "release",
            "sortBy": "releaseState"
          }
        },
        "parentFilter": {
          "labelName": "Project",
          "emittedLevel": "release"
        }
      }
    },
    {
      "boardId": 14,
      "boardName": "DORA",
      "boardSlug": "dora",
      "kpis": [
        {
          "kpiId": "kpi156",
          "kpiName": "Lead Time For Change",
          "isEnabled": true,
          "order": 3,
          "kpiDetail": {
            "id": "651e8b40b3cd2c83443d7345",
            "kpiId": "kpi156",
            "kpiName": "Lead Time For Change",
            "isDeleted": "False",
            "defaultOrder": 3,
            "kpiCategory": "Dora",
            "kpiUnit": "Days",
            "chartType": "line",
            "showTrend": true,
            "isPositiveTrend": true,
            "calculateMaturity": true,
            "hideOverallFilter": false,
            "kpiSource": "Jira",
            "maxValue": "100",
            "thresholdValue": 0,
            "kanban": false,
            "groupId": 15,
            "kpiInfo": {
              "definition": "LEAD TIME FOR CHANGE measures the velocity of software delivery.",
              "details": [
                {
                  "type": "paragraph",
                  "value": "LEAD TIME FOR CHANGE Captures the time between a code change to commit and deployed to production."
                },
                {
                  "type": "link",
                  "kpiLinkDetail": {
                    "text": "Detailed Information at",
                    "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/59080705/DORA+KPIs#Lead-time-for-changes"
                  }
                }
              ],
              "maturityLevels": []
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
                "label": "< 1 Day"
              },
              {
                "level": "M4",
                "bgColor": "#4ebb1a",
                "label": "< 7 Days"
              },
              {
                "level": "M3",
                "bgColor": "#ef7643",
                "label": "< 30 Days"
              },
              {
                "level": "M2",
                "bgColor": "#f53535",
                "label": "< 90 Days"
              },
              {
                "level": "M1",
                "bgColor": "#c91212",
                "label": ">= 90 Days"
              }
            ],
            "trendCalculative": false,
            "xaxisLabel": "Weeks",
            "yaxisLabel": "Days",
            "isAdditionalFilterSupport": false
          },
          "shown": true
        },
        {
          "kpiId": "kpi166",
          "kpiName": "Mean Time to Recover",
          "isEnabled": true,
          "order": 4,
          "kpiDetail": {
            "id": "656347659b6b2f1d4faa9ec0",
            "kpiId": "kpi166",
            "kpiName": "Mean Time to Recover",
            "isDeleted": "False",
            "defaultOrder": 4,
            "kpiCategory": "Dora",
            "kpiUnit": "Hours",
            "chartType": "line",
            "showTrend": true,
            "isPositiveTrend": false,
            "calculateMaturity": true,
            "hideOverallFilter": false,
            "kpiSource": "Jira",
            "maxValue": "100",
            "thresholdValue": 0,
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
                "bgColor": "#167a26"
              },
              {
                "level": "M4",
                "bgColor": "#4ebb1a"
              },
              {
                "level": "M3",
                "bgColor": "#ef7643"
              },
              {
                "level": "M2",
                "bgColor": "#f53535"
              },
              {
                "level": "M1",
                "bgColor": "#c91212"
              }
            ],
            "trendCalculative": false,
            "xaxisLabel": "Weeks",
            "yaxisLabel": "Hours",
            "isAdditionalFilterSupport": false
          },
          "shown": true
        },
        {
          "kpiId": "kpi116",
          "kpiName": "Change Failure Rate",
          "isEnabled": true,
          "order": 15,
          "kpiDetail": {
            "id": "64b4ed7acba3c12de16472fa",
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
            "thresholdValue": 0,
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
                "bgColor": "#167a26"
              },
              {
                "level": "M4",
                "bgColor": "#4ebb1a"
              },
              {
                "level": "M3",
                "bgColor": "#ef7643"
              },
              {
                "level": "M2",
                "bgColor": "#f53535"
              },
              {
                "level": "M1",
                "bgColor": "#c91212"
              }
            ],
            "trendCalculative": false,
            "xaxisLabel": "Weeks",
            "yaxisLabel": "Percentage",
            "isAdditionalFilterSupport": false
          },
          "shown": true
        },
        {
          "kpiId": "kpi118",
          "kpiName": "Deployment Frequency",
          "isEnabled": true,
          "order": 25,
          "kpiDetail": {
            "id": "64b4ed7acba3c12de1647304",
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
            "thresholdValue": 0,
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
            "aggregationCircleCriteria": "sum",
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
                "label": ">= 2 per week"
              },
              {
                "level": "M4",
                "bgColor": "#4ebb1a",
                "label": "Once per week"
              },
              {
                "level": "M3",
                "bgColor": "#ef7643",
                "label": "Once in 2 weeks"
              },
              {
                "level": "M2",
                "bgColor": "#f53535",
                "label": "Once in 4 weeks"
              },
              {
                "level": "M1",
                "bgColor": "#c91212",
                "label": "< Once in 8 weeks"
              }
            ],
            "trendCalculative": false,
            "xaxisLabel": "Weeks",
            "yaxisLabel": "Count",
            "isAdditionalFilterSupport": false
          },
          "shown": true
        }
      ],
      "filters": {
        "projectTypeSwitch": {
          "enabled": false,
          "visible": true
        },
        "primaryFilter": {
          "type": "singleSelect",
          "defaultLevel": {
            "labelName": "project",
            "sortBy": null
          }
        },
        "parentFilter": {
          "labelName": "Organization Level"
        }
      }
    },
    {
      "boardId": 15,
      "boardName": "Backlog",
      "boardSlug": "backlog",
      "kpis": [
        {
          "kpiId": "kpi138",
          "kpiName": "Backlog Readiness",
          "isEnabled": true,
          "order": 1,
          "subCategoryBoard": "Backlog Health",
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi3",
          "kpiName": "Lead Time",
          "isEnabled": true,
          "order": 1,
          "subCategoryBoard": "Flow KPIs",
          "kpiDetail": {
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
            "isAdditionalFilterSupport": false
          },
          "shown": true
        },
        {
          "kpiId": "kpi170",
          "kpiName": "Flow Efficiency",
          "isEnabled": true,
          "order": 1,
          "subCategoryBoard": "Flow KPIs",
          "kpiDetail": {
            "id": "656347669b6b2f1d4faa9ec5",
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
          "shown": true
        },
        {
          "kpiId": "kpi127",
          "kpiName": "Production Defects Ageing",
          "isEnabled": true,
          "order": 2,
          "subCategoryBoard": "Backlog Health",
          "kpiDetail": {
            "id": "64b4ed7acba3c12de1647328",
            "kpiId": "kpi127",
            "kpiName": "Production Defects Ageing",
            "isDeleted": "False",
            "defaultOrder": 2,
            "kpiCategory": "Backlog",
            "kpiSubCategory": "Backlog Health",
            "kpiUnit": "Number",
            "chartType": "line",
            "showTrend": false,
            "isPositiveTrend": false,
            "calculateMaturity": false,
            "hideOverallFilter": false,
            "kpiSource": "Jira",
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
          "shown": true
        },
        {
          "kpiId": "kpi129",
          "kpiName": "Issues Without Story Link",
          "isEnabled": true,
          "order": 3,
          "subCategoryBoard": "Backlog Health",
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi161",
          "kpiName": "Iteration Readiness",
          "isEnabled": true,
          "order": 4,
          "subCategoryBoard": "Backlog Health",
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi137",
          "kpiName": "Defect Reopen Rate",
          "isEnabled": true,
          "order": 5,
          "subCategoryBoard": "Backlog Health",
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi169",
          "kpiName": "Epic Progress",
          "isEnabled": true,
          "order": 5,
          "subCategoryBoard": "Epic View",
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi139",
          "kpiName": "Refinement Rejection Rate",
          "isEnabled": true,
          "order": 6,
          "subCategoryBoard": "Backlog Health",
          "kpiDetail": {
            "id": "64b4ed7acba3c12de1647329",
            "kpiId": "kpi139",
            "kpiName": "Refinement Rejection Rate",
            "isDeleted": "False",
            "defaultOrder": 6,
            "kpiCategory": "Backlog",
            "kpiSubCategory": "Backlog Health",
            "kpiUnit": "%",
            "chartType": "line",
            "showTrend": false,
            "isPositiveTrend": false,
            "calculateMaturity": false,
            "hideOverallFilter": false,
            "kpiSource": "Jira",
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
          "shown": true
        },
        {
          "kpiId": "kpi146",
          "kpiName": "Flow Distribution",
          "isEnabled": true,
          "order": 6,
          "subCategoryBoard": "Flow KPIs",
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi148",
          "kpiName": "Flow Load",
          "isEnabled": true,
          "order": 7,
          "subCategoryBoard": "Flow KPIs",
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi151",
          "kpiName": "Backlog Count By Status",
          "isEnabled": true,
          "order": 9,
          "subCategoryBoard": "Backlog Overview",
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi152",
          "kpiName": "Backlog Count By Issue Type",
          "isEnabled": true,
          "order": 10,
          "subCategoryBoard": "Backlog Overview",
          "kpiDetail": {
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
          "shown": true
        },
        {
          "kpiId": "kpi155",
          "kpiName": "Defect Count By Type",
          "isEnabled": true,
          "order": 11,
          "subCategoryBoard": "Backlog Overview",
          "kpiDetail": {
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
          "shown": true
        }
      ],
      "filters": {
        "projectTypeSwitch": {
          "enabled": false,
          "visible": true
        },
        "primaryFilter": {
          "type": "singleSelect",
          "defaultLevel": {
            "labelName": "project",
            "sortBy": null
          }
        },
        "parentFilter": null
      }
    },
    {
      "boardId": 16,
      "boardName": "KPI Maturity",
      "boardSlug": "Maturity",
      "kpis": [
        {
          "id": "65793ddb127be336160bc0be",
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
          "combinedKpiSource": "Jira/Azure",
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
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83853321/Defect+Injection+Rate"
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
          "isAdditionalFilterSupport": true
        },
        {
          "id": "65793ddb127be336160bc0bf",
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
          "combinedKpiSource": "Jira/Azure",
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
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/84049922/First+time+pass+rate+FTPR"
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
          "id": "65793ddb127be336160bc0c0",
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
          "combinedKpiSource": "Jira/Azure",
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
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83886083/Defect+Density"
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
          "isAdditionalFilterSupport": true
        },
        {
          "id": "65793ddb127be336160bc0c1",
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
          "combinedKpiSource": "Jira/Azure",
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
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/84049938/Defect+Seepage+Rate"
                }
              }
            ]
          },
          "kpiFilter": "dropDown",
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
          "isAdditionalFilterSupport": true
        },
        {
          "id": "65793ddb127be336160bc0c2",
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
          "combinedKpiSource": "Jira/Azure",
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
                  "Total no. of defects in a iteration"
                ]
              }
            ],
            "details": [
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83886099/Defect+Removal+Efficiency"
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
          "isAdditionalFilterSupport": true
        },
        {
          "id": "65793ddb127be336160bc0c3",
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
          "combinedKpiSource": "Jira/Azure",
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
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83886115/Defect+Rejection+Rate"
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
          "isAdditionalFilterSupport": true
        },
        {
          "id": "65793ddb127be336160bc0c4",
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
          "combinedKpiSource": "Jira/Azure",
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
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83820546/Defect+Count+by+Priority+Quality"
                }
              }
            ]
          },
          "kpiFilter": "multiSelectDropDown",
          "aggregationCriteria": "sum",
          "trendCalculative": false,
          "xaxisLabel": "Sprints",
          "yaxisLabel": "Count",
          "isAdditionalFilterSupport": true
        },
        {
          "id": "65793ddb127be336160bc0c5",
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
          "combinedKpiSource": "Jira/Azure",
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
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83820562/Defect+Count+By+RCA+Quality"
                }
              }
            ]
          },
          "kpiFilter": "multiSelectDropDown",
          "aggregationCriteria": "sum",
          "trendCalculative": false,
          "xaxisLabel": "Sprints",
          "yaxisLabel": "Count",
          "isAdditionalFilterSupport": true
        },
        {
          "id": "65793ddb127be336160bc0c6",
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
          "combinedKpiSource": "Jira/Azure",
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
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83886196/Created+vs+Resolved"
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
          "trendCalculative": false,
          "xaxisLabel": "Sprints",
          "yaxisLabel": "Count",
          "isAdditionalFilterSupport": true
        },
        {
          "id": "65793ddb127be336160bc0c7",
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
          "combinedKpiSource": "Zephyr/Zypher/JiraTest",
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
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/84049954/Regression+Automation+Coverage"
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
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc0c8",
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
          "combinedKpiSource": "Zephyr/Zypher/JiraTest",
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
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83886131/In-Sprint+Automation+Coverage"
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
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc0c9",
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
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83886147/Unit+Test+Coverage"
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
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc0ca",
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
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/84049987/Sonar+Violations"
                }
              }
            ]
          },
          "kpiFilter": "multiSelectDropDown",
          "aggregationCriteria": "sum",
          "trendCalculative": false,
          "xaxisLabel": "Weeks",
          "yaxisLabel": "Count",
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc0cb",
          "kpiId": "kpi27",
          "kpiName": "Tech Debt - Sonar Maintainability",
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
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83886163/Sonar+Tech+Debt"
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
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc0cc",
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
          "combinedKpiSource": "Jenkins/Bamboo/GitHubAction/AzurePipeline/Teamcity",
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
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/71958608/DORA+Change+Failure+Rate"
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
          "xaxisLabel": "Weeks",
          "yaxisLabel": "Percentage",
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc0cd",
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
          "combinedKpiSource": "Zephyr/Zypher/JiraTest",
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
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/84049970/Test+Execution+and+pass+percentage"
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
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc0ce",
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
          "combinedKpiSource": "Jira/Azure",
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
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/84050003/Issue+Count"
                }
              }
            ]
          },
          "kpiFilter": "radioButton",
          "aggregationCriteria": "sum",
          "trendCalculative": false,
          "xaxisLabel": "Sprints",
          "yaxisLabel": "Count",
          "isAdditionalFilterSupport": true
        },
        {
          "id": "65793ddb127be336160bc0cf",
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
          "combinedKpiSource": "Jira/Azure",
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
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/84050019/Commitment+Reliability"
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
          "id": "65793ddb127be336160bc0d0",
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
          "combinedKpiSource": "Jira/Azure",
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
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83886228/Sprint+Predictability"
                }
              }
            ]
          },
          "aggregationCriteria": "average",
          "trendCalculative": false,
          "xaxisLabel": "Sprints",
          "yaxisLabel": "Percentage",
          "isAdditionalFilterSupport": true
        },
        {
          "id": "65793ddb127be336160bc0d1",
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
          "combinedKpiSource": "Jira/Azure",
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
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83886212/Sprint+Velocity"
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
          "trendCalculative": false,
          "xaxisLabel": "Sprints",
          "yaxisLabel": "Count",
          "isAdditionalFilterSupport": true
        },
        {
          "id": "65793ddb127be336160bc0d2",
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
          "combinedKpiSource": "Jira/Azure",
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
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83820594/Sprint+Capacity+Utilization"
                }
              }
            ]
          },
          "aggregationCriteria": "sum",
          "trendCalculative": false,
          "xaxisLabel": "Sprints",
          "yaxisLabel": "Hours",
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc0d3",
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
          "combinedKpiSource": "Bitbucket/AzureRepository/GitHub/GitLab",
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
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70713477/Developer+Mean+time+to+Merge"
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
          "id": "65793ddb127be336160bc0d4",
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
          "combinedKpiSource": "Bitbucket/AzureRepository/GitHub/GitLab",
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
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70451310/Developer+No.+of+Check-ins+and+Merge+Requests"
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
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc0d5",
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
          "combinedKpiSource": "Jenkins/Bamboo/GitHubAction/AzurePipeline/Teamcity",
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
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83886260/Code+Build+Time"
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
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc0d6",
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
          "combinedKpiSource": "Jenkins/Bamboo/GitHubAction/AzurePipeline/Teamcity",
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
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/71827544/DORA+Deployment+Frequency"
                }
              }
            ]
          },
          "kpiFilter": "multiSelectDropDown",
          "aggregationCriteria": "sum",
          "aggregationCircleCriteria": "sum",
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
          "id": "65793ddb127be336160bc0dc",
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
          "combinedKpiSource": "Jira/Azure",
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
          "isAdditionalFilterSupport": true
        },
        {
          "id": "65793ddb127be336160bc0d7",
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
          "combinedKpiSource": "Jira/Azure",
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
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/84050035/Release+Frequency"
                }
              }
            ]
          },
          "aggregationCriteria": "sum",
          "trendCalculative": false,
          "xaxisLabel": "Months",
          "yaxisLabel": "Count",
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc0d8",
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
          "combinedKpiSource": "Jira/Azure",
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
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/84050051/Value+delivered+Cost+of+Delay"
                }
              }
            ]
          },
          "aggregationCriteria": "sum",
          "trendCalculative": false,
          "xaxisLabel": "Months",
          "yaxisLabel": "Count(Days)",
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc0d9",
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
          "combinedKpiSource": "Jira/Azure",
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
          "trendCalculative": false,
          "xaxisLabel": "Weeks",
          "yaxisLabel": "Count",
          "isAdditionalFilterSupport": true
        },
        {
          "id": "65793ddb127be336160bc0da",
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
          "combinedKpiSource": "Jira/Azure",
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
          "trendCalculative": false,
          "xaxisLabel": "Weeks",
          "yaxisLabel": "Count",
          "isAdditionalFilterSupport": true
        },
        {
          "id": "65793ddb127be336160bc0db",
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
          "combinedKpiSource": "Jira/Azure",
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
          "isAdditionalFilterSupport": true
        },
        {
          "id": "65793ddb127be336160bc0dd",
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
          "combinedKpiSource": "Jira/Azure",
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
          "isAdditionalFilterSupport": true
        },
        {
          "id": "65793ddb127be336160bc0de",
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
          "combinedKpiSource": "Jira/Azure",
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
          "id": "65793ddb127be336160bc0df",
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
          "combinedKpiSource": "Zephyr/Zypher/JiraTest",
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
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc0e0",
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
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc0e1",
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
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc0ed",
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
          "combinedKpiSource": "Jira/Azure",
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
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70713345/Planned+Work+Status"
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
          "id": "65793ddb127be336160bc0e2",
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
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc0e3",
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
          "combinedKpiSource": "Zephyr/Zypher/JiraTest",
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
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc0e4",
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
          "combinedKpiSource": "Jira/Azure",
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
          "isAdditionalFilterSupport": true
        },
        {
          "id": "65793ddb127be336160bc0e5",
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
          "combinedKpiSource": "Jira/Azure",
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
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc0e6",
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
          "combinedKpiSource": "Jenkins/Bamboo/GitHubAction/AzurePipeline/Teamcity",
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
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc0e7",
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
          "combinedKpiSource": "Bitbucket/AzureRepository/GitHub/GitLab",
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
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc0e8",
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
          "combinedKpiSource": "Jira/Azure",
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
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc0e9",
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
          "combinedKpiSource": "Jira/Azure",
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
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc0ea",
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
          "combinedKpiSource": "Jira/Azure",
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
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc0eb",
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
          "combinedKpiSource": "Jira/Azure",
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
          "id": "65793ddb127be336160bc0ec",
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
          "combinedKpiSource": "Jira/Azure",
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
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70680609/Work+Remaining"
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
          "id": "65793ddb127be336160bc0fa",
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
          "combinedKpiSource": "Jira/Azure",
          "thresholdValue": 0,
          "kanban": false,
          "groupId": 10,
          "kpiInfo": {
            "definition": "It groups all the open production defects based on their ageing in the backlog.",
            "details": [
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83886319/Production+defects+Ageing"
                }
              }
            ]
          },
          "kpiFilter": "multiSelectDropDown",
          "aggregationCriteria": "sum",
          "trendCalculative": false,
          "xaxisLabel": "Months",
          "yaxisLabel": "Count",
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc0ee",
          "kpiId": "kpi75",
          "kpiName": "Estimate vs Actual",
          "isDeleted": "False",
          "defaultOrder": 23,
          "kpiCategory": "Iteration",
          "kpiSubCategory": "Iteration Review",
          "kpiUnit": "Hours",
          "showTrend": false,
          "isPositiveTrend": true,
          "boxType": "2_column",
          "calculateMaturity": true,
          "hideOverallFilter": false,
          "kpiSource": "Jira",
          "combinedKpiSource": "Jira/Azure",
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
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70680658/Estimate+vs+Actual"
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
          "id": "65793ddb127be336160bc0ef",
          "kpiId": "kpi123",
          "kpiName": "Issues likely to Spill",
          "isDeleted": "False",
          "defaultOrder": 8,
          "kpiCategory": "Iteration",
          "kpiSubCategory": "Iteration Review",
          "kpiUnit": "Count",
          "showTrend": false,
          "isPositiveTrend": true,
          "boxType": "3_column",
          "calculateMaturity": false,
          "hideOverallFilter": false,
          "kpiSource": "Jira",
          "combinedKpiSource": "Jira/Azure",
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
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70713361/Issues+likely+to+spill"
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
          "id": "65793ddb127be336160bc0f0",
          "kpiId": "kpi122",
          "kpiName": "Closure Possible Today",
          "isDeleted": "False",
          "defaultOrder": 7,
          "kpiCategory": "Iteration",
          "kpiSubCategory": "Iteration Review",
          "kpiUnit": "Story Point",
          "showTrend": false,
          "isPositiveTrend": true,
          "boxType": "2_column",
          "calculateMaturity": false,
          "hideOverallFilter": false,
          "kpiSource": "Jira",
          "combinedKpiSource": "Jira/Azure",
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
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70713377/Closures+possible+today"
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
          "id": "65793ddb127be336160bc0f1",
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
          "combinedKpiSource": "Jira/Azure",
          "maxValue": "",
          "kanban": false,
          "groupId": 8,
          "kpiInfo": {
            "definition": "Iteration commitment shows in terms of issue count and story points the Initial commitment (issues tagged when the iteration starts), Scope added and Scope removed.",
            "details": [
              {
                "type": "paragraph",
                "value": "Overall commitment= Initial Commitment + Scope added - Scope removed"
              },
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70418594/Iteration+Commitment"
                }
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
          "id": "65793ddb127be336160bc0f2",
          "kpiId": "kpi124",
          "kpiName": "Estimation Hygiene",
          "isDeleted": "False",
          "defaultOrder": 22,
          "kpiCategory": "Iteration",
          "kpiSubCategory": "Iteration Review",
          "kpiUnit": "Count",
          "showTrend": false,
          "isPositiveTrend": true,
          "boxType": "2_column_big",
          "calculateMaturity": false,
          "hideOverallFilter": false,
          "kpiSource": "Jira",
          "combinedKpiSource": "Jira/Azure",
          "maxValue": "",
          "kanban": false,
          "groupId": 8,
          "kpiInfo": {
            "definition": "It shows the count of issues which do not have estimates and count of In progress issues without any work logs.",
            "details": [
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70680674/Estimate+Hygiene"
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
          "id": "65793ddb127be336160bc0f3",
          "kpiId": "kpi132",
          "kpiName": "Defect Count by RCA",
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
          "combinedKpiSource": "Jira/Azure",
          "maxValue": "",
          "kanban": false,
          "groupId": 8,
          "kpiInfo": {
            "definition": "It shows the breakup of all defects within an iteration by root cause identified.",
            "details": [
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70713409/Defect+count+by+RCA"
                }
              }
            ]
          },
          "kpiFilter": "radioButton",
          "trendCalculative": false,
          "xaxisLabel": "",
          "yaxisLabel": "",
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc102",
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
          "id": "65793ddb127be336160bc0f4",
          "kpiId": "kpi133",
          "kpiName": "Quality Status",
          "isDeleted": "False",
          "defaultOrder": 13,
          "kpiCategory": "Iteration",
          "kpiSubCategory": "Iteration Review",
          "kpiUnit": "",
          "showTrend": false,
          "isPositiveTrend": true,
          "boxType": "3_column",
          "calculateMaturity": false,
          "hideOverallFilter": false,
          "kpiSource": "Jira",
          "combinedKpiSource": "Jira/Azure",
          "maxValue": "",
          "kanban": false,
          "groupId": 8,
          "kpiInfo": {
            "definition": "It showcases the count of defect linked to stories and count that are not linked to any story. The defect injection rate and defect density are shown to give a wholistic view of quality of ongoing iteration",
            "details": [
              {
                "type": "paragraph",
                "value": "*Any defect created during the iteration duration but is not added to the iteration is not considered"
              },
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70680625/Quality+Status"
                }
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
          "id": "65793ddb127be336160bc0f5",
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
          "combinedKpiSource": "Jira/Azure",
          "maxValue": "",
          "kanban": false,
          "groupId": 8,
          "kpiInfo": {
            "definition": "It shows count of the issues which do not have a due date. It also shows the completed count amongst the unplanned issues.",
            "details": [
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70680593/Unplanned+Work+Status"
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
          "id": "65793ddb127be336160bc0f6",
          "kpiId": "kpi125",
          "kpiName": "Iteration Burnup",
          "isDeleted": "False",
          "defaultOrder": 9,
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
          "combinedKpiSource": "Jira/Azure",
          "maxValue": "",
          "kanban": false,
          "groupId": 8,
          "kpiInfo": {
            "definition": "Iteration Burnup KPI shows the cumulative actual progress against the overall scope of the iteration on a daily basis. For teams putting due dates at the beginning of iteration, the graph additionally shows the actual progress in comparison to the planning done and also predicts the probable progress for the remaining days of the iteration.",
            "details": [
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70680577/Iteration+Burnup"
                }
              }
            ]
          },
          "kpiFilter": "multiselectdropdown",
          "kpiWidth": 100,
          "trendCalculative": false,
          "xaxisLabel": "Days",
          "yaxisLabel": "Count",
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc0f7",
          "kpiId": "kpi131",
          "kpiName": "Wastage",
          "isDeleted": "False",
          "defaultOrder": 10,
          "kpiCategory": "Iteration",
          "kpiSubCategory": "Iteration Review",
          "kpiUnit": "Hours",
          "showTrend": false,
          "isPositiveTrend": true,
          "boxType": "3_column",
          "calculateMaturity": false,
          "hideOverallFilter": false,
          "kpiSource": "Jira",
          "combinedKpiSource": "Jira/Azure",
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
              },
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70713393/Wastage"
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
          "id": "65793ddb127be336160bc0f8",
          "kpiId": "kpi135",
          "kpiName": "First Time Pass Rate",
          "isDeleted": "False",
          "defaultOrder": 12,
          "kpiCategory": "Iteration",
          "kpiSubCategory": "Iteration Review",
          "kpiUnit": "Hours",
          "showTrend": false,
          "isPositiveTrend": true,
          "boxType": "3_column",
          "calculateMaturity": false,
          "hideOverallFilter": false,
          "kpiSource": "Jira",
          "combinedKpiSource": "Jira/Azure",
          "maxValue": "",
          "kanban": false,
          "groupId": 8,
          "kpiInfo": {
            "definition": "Percentage of tickets that passed QA with no return transition or any tagging to a specific configured status and no linkage of a defect.",
            "details": [
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70680642/First+time+pass+rate"
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
          "id": "65793ddb127be336160bc0f9",
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
          "combinedKpiSource": "Jira/Azure",
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
            ],
            "details": [
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/84050083/Issues+without+story+link"
                }
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
          "id": "65793ddb127be336160bc0fb",
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
          "combinedKpiSource": "Jira/Azure",
          "thresholdValue": 40,
          "kanban": false,
          "groupId": 10,
          "kpiInfo": {
            "definition": "It measures the percentage of stories rejected during refinement as compared to the overall stories discussed in a week.",
            "details": [
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83886335/Refinement+Rejection+Rate"
                }
              }
            ]
          },
          "kpiFilter": "",
          "aggregationCriteria": "sum",
          "trendCalculative": false,
          "xaxisLabel": "Weeks",
          "yaxisLabel": "Count",
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc0fc",
          "kpiId": "kpi136",
          "kpiName": "Defect Count by Status",
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
          "combinedKpiSource": "Jira/Azure",
          "maxValue": "",
          "kanban": false,
          "groupId": 8,
          "kpiInfo": {
            "definition": "It shows the breakup of all defects within an iteration by status. User can view the total defects in the iteration as well as the defects created after iteration start.",
            "details": [
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70713425/Defect+count+by+Status"
                }
              }
            ]
          },
          "kpiFilter": "radioButton",
          "trendCalculative": false,
          "xaxisLabel": "",
          "yaxisLabel": "",
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc0fd",
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
          "combinedKpiSource": "Jira/Azure",
          "kanban": false,
          "groupId": 10,
          "kpiInfo": {
            "definition": "It shows number of defects reopened in a given span of time in comparison to the total closed defects. For all the reopened defects, the average time to reopen is also available.",
            "details": [
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83820630/Defects+Reopen+rate"
                }
              }
            ]
          },
          "kpiFilter": "dropdown",
          "aggregationCriteria": "average",
          "trendCalculative": false,
          "isAdditionalFilterSupport": true
        },
        {
          "id": "65793ddb127be336160bc0fe",
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
          "combinedKpiSource": "Jira/Azure",
          "maxValue": "",
          "kanban": false,
          "groupId": 9,
          "kpiInfo": {
            "definition": "It shows the breakup of all defects tagged to a release based on Status. The breakup is shown in terms of count & percentage.",
            "details": [
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/79986689/Release+Defect+count+by+Status"
                }
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
          "id": "65793ddb127be336160bc0ff",
          "kpiId": "kpi142",
          "kpiName": "Defect Count by RCA",
          "isDeleted": "False",
          "defaultOrder": 2,
          "kpiCategory": "Release",
          "kpiSubCategory": "Quality",
          "kpiUnit": "Count",
          "chartType": "stackedColumn",
          "showTrend": false,
          "isPositiveTrend": true,
          "boxType": "chart",
          "calculateMaturity": false,
          "hideOverallFilter": false,
          "kpiSource": "Jira",
          "combinedKpiSource": "Jira/Azure",
          "maxValue": "",
          "kanban": false,
          "groupId": 9,
          "kpiInfo": {
            "definition": "It shows the breakup of all defects tagged to a release based on RCA. The breakup is shown in terms of count at different testing phases.",
            "details": [
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/79953937/Release+Defect+count+by+RCA"
                }
              }
            ]
          },
          "kpiFilter": "radioButton",
          "trendCalculative": false,
          "xaxisLabel": "Test Phase",
          "yaxisLabel": "Count",
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc100",
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
          "combinedKpiSource": "Jira/Azure",
          "maxValue": "",
          "kanban": false,
          "groupId": 9,
          "kpiInfo": {
            "definition": "It shows the breakup of all defects tagged to a release based on Assignee. The breakup is shown in terms of count & percentage.",
            "details": [
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/79691782/Release+Defect+count+by+Assignee"
                }
              }
            ]
          },
          "kpiFilter": "radioButton",
          "trendCalculative": false,
          "xaxisLabel": "",
          "yaxisLabel": "",
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc101",
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
          "combinedKpiSource": "Jira/Azure",
          "maxValue": "",
          "kanban": false,
          "groupId": 9,
          "kpiInfo": {
            "definition": "It shows the breakup of all defects tagged to a release based on Priority. The breakup is shown in terms of count & percentage.",
            "details": [
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/79953921/Release+Defect+count+by+Priority"
                }
              }
            ]
          },
          "kpiFilter": "radioButton",
          "trendCalculative": false,
          "xaxisLabel": "",
          "yaxisLabel": "",
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc103",
          "kpiId": "kpi140",
          "kpiName": "Defect Count by Priority",
          "isDeleted": "False",
          "defaultOrder": 16,
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
          "combinedKpiSource": "Jira/Azure",
          "maxValue": "",
          "kanban": false,
          "groupId": 8,
          "kpiInfo": {
            "definition": "It shows the breakup of all defects within an iteration by priority. User can view the total defects in the iteration as well as the defects created after iteration start.",
            "details": [
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70713441/Defect+count+by+Priority"
                }
              }
            ]
          },
          "kpiFilter": "radioButton",
          "trendCalculative": false,
          "xaxisLabel": "",
          "yaxisLabel": "",
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc104",
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
          "combinedKpiSource": "Jira/Azure",
          "maxValue": "",
          "kanban": false,
          "groupId": 9,
          "kpiInfo": {
            "definition": "It shows the breakup by status of issues tagged to a release. The breakup is based on both issue count and story points",
            "details": [
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/79757314/Release+Release+Progress"
                }
              }
            ]
          },
          "kpiFilter": "dropDown",
          "kpiWidth": 100,
          "trendCalculative": false,
          "xaxisLabel": "",
          "yaxisLabel": "",
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc105",
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
          "combinedKpiSource": "Jira/Azure",
          "maxValue": "",
          "kanban": false,
          "groupId": 8,
          "kpiInfo": {
            "definition": "It gives a comparative view between the planned completion and actual completion from a development point of view. In addition, user can see the delay (in days) in dev completed issues",
            "details": [
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/99483649/Dev+Completion+Status"
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
          "id": "65793ddb127be336160bc106",
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
          "combinedKpiSource": "Jira/Azure",
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
              },
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83886286/Backlog+Readiness"
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
          "id": "65793ddb127be336160bc107",
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
          "calculateMaturity": true,
          "hideOverallFilter": false,
          "kpiSource": "Jira",
          "combinedKpiSource": "Jira/Azure",
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
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc108",
          "kpiId": "kpi148",
          "kpiName": "Flow Load",
          "isDeleted": "False",
          "defaultOrder": 13,
          "kpiCategory": "Backlog",
          "kpiSubCategory": "Flow KPIs",
          "kpiUnit": "",
          "chartType": "stacked-area",
          "showTrend": false,
          "isPositiveTrend": false,
          "calculateMaturity": false,
          "hideOverallFilter": false,
          "kpiSource": "Jira",
          "combinedKpiSource": "Jira/Azure",
          "kanban": false,
          "groupId": 11,
          "kpiInfo": {
            "definition": " Flow load indicates how many items are currently in the backlog. This KPI emphasizes on limiting work in progress to enabling a fast flow of issues",
            "details": [
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83820646/Flow+Load"
                }
              }
            ]
          },
          "kpiFilter": "",
          "aggregationCriteria": "sum",
          "trendCalculative": false,
          "xaxisLabel": "Time",
          "yaxisLabel": "Count",
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc109",
          "kpiId": "kpi146",
          "kpiName": "Flow Distribution",
          "isDeleted": "False",
          "defaultOrder": 11,
          "kpiCategory": "Backlog",
          "kpiSubCategory": "Flow KPIs",
          "kpiUnit": "",
          "chartType": "stacked-area",
          "showTrend": false,
          "isPositiveTrend": false,
          "calculateMaturity": false,
          "hideOverallFilter": false,
          "kpiSource": "Jira",
          "combinedKpiSource": "Jira/Azure",
          "kanban": false,
          "groupId": 10,
          "kpiInfo": {
            "definition": "Flow Distribution evaluates the amount of each kind of work (issue types) which are open in the backlog over a period of time.",
            "details": [
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/84050099/Flow+Distribution"
                }
              }
            ]
          },
          "kpiFilter": "",
          "aggregationCriteria": "sum",
          "trendCalculative": false,
          "xaxisLabel": "Time",
          "yaxisLabel": "Count",
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc10a",
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
          "combinedKpiSource": "Jira/Azure",
          "maxValue": "5",
          "thresholdValue": 4,
          "kanban": false,
          "groupId": 16,
          "kpiInfo": {
            "details": [
              {
                "type": "paragraph",
                "value": "KPI for tracking moral of team members"
              },
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/41582623/People"
                }
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
          "id": "65793ddb127be336160bc10b",
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
          "combinedKpiSource": "Jira/Azure",
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
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70484023/Release+Release+Burnup"
                }
              },
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70484023/Release+Release+Burnup"
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
          "id": "65793ddb127be336160bc10c",
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
          "combinedKpiSource": "Jira/Azure",
          "kanban": false,
          "groupId": 10,
          "kpiInfo": {
            "definition": "Total count of issues in the Backlog with a breakup by Status.",
            "details": [
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83820678/Backlog+Count+by+Status"
                }
              }
            ]
          },
          "trendCalculative": false,
          "xaxisLabel": "",
          "yaxisLabel": "",
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc10d",
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
          "combinedKpiSource": "Jira/Azure",
          "kanban": false,
          "groupId": 11,
          "kpiInfo": {
            "definition": "Total count of issues in the backlog with a breakup by issue type.",
            "details": [
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/84050115/Backlog+Count+by+Issue+type"
                }
              }
            ]
          },
          "trendCalculative": false,
          "xaxisLabel": "",
          "yaxisLabel": "",
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddb127be336160bc10e",
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
          "combinedKpiSource": "Jira/Azure",
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
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/84050067/PI+Predictability"
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
          "id": "65793ddc127be336160bc10f",
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
          "combinedKpiSource": "Jira/Azure",
          "maxValue": "",
          "kanban": false,
          "groupId": 13,
          "kpiFilter": "multiselectdropdown",
          "kpiWidth": 100,
          "trendCalculative": false,
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddc127be336160bc110",
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
          "combinedKpiSource": "Jira/Azure",
          "kanban": false,
          "groupId": 11,
          "kpiInfo": {
            "definition": "Total count of issues in the backlog with a breakup by defect type.",
            "details": [
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/98140489/Defect+count+by+Type"
                }
              }
            ]
          },
          "trendCalculative": false,
          "xaxisLabel": "",
          "yaxisLabel": "",
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddc127be336160bc111",
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
          "combinedKpiSource": "Jira/Azure",
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
              },
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/71663772/DORA+Lead+time+for+changes"
                }
              },
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/71663772/DORA+Lead+time+for+changes"
                }
              }
            ],
            "maturityLevels": []
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
        {
          "id": "65793ddc127be336160bc112",
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
          "combinedKpiSource": "RepoTool",
          "maxValue": 10,
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
              },
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70451310/Developer+No.+of+Check-ins+and+Merge+Requests"
                }
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
          "id": "65793ddc127be336160bc113",
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
          "combinedKpiSource": "RepoTool",
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
              },
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70713477/Developer+Mean+time+to+Merge"
                }
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
          "id": "65793ddc127be336160bc114",
          "kpiId": "kpi159",
          "kpiName": "Number of Check-ins",
          "isDeleted": "false",
          "defaultOrder": 1,
          "kpiCategory": "Developer",
          "kpiUnit": "check-ins",
          "chartType": "line",
          "showTrend": true,
          "isPositiveTrend": true,
          "calculateMaturity": true,
          "hideOverallFilter": true,
          "kpiSource": "BitBucket",
          "combinedKpiSource": "RepoTool",
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
          "id": "65793ddc127be336160bc115",
          "kpiId": "kpi160",
          "kpiName": "Pickup Time",
          "isDeleted": "false",
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
          "combinedKpiSource": "RepoTool",
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
                "value": "A progress indicator shows trend of Pickup Time in the last 2 weeks. A downward trend is considered positive"
              },
              {
                "type": "paragraph",
                "value": "Maturity of the KPI is calculated based on the average of the last 5 weeks"
              },
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70680716/Developer+Pickup+time"
                }
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
          "yaxisLabel": "Count (Hours)",
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddc127be336160bc116",
          "kpiId": "kpi162",
          "kpiName": "PR Size",
          "isDeleted": "false",
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
          "combinedKpiSource": "RepoTool",
          "maxValue": 10,
          "thresholdValue": 4,
          "kanban": false,
          "groupId": 1,
          "kpiInfo": {
            "definition": "Pull request size measures the number of code lines modified in a pull request. Smaller pull requests are easier to review, safer to merge, and correlate to a lower cycle time.",
            "details": [
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70713493/Developer+PR+Size"
                }
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
          "yaxisLabel": "Count (No. of Lines)",
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddc127be336160bc117",
          "kpiId": "kpi164",
          "kpiName": "Scope Churn",
          "isDeleted": "False",
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
          "combinedKpiSource": "Jira/Azure",
          "maxValue": "200",
          "thresholdValue": 20,
          "kanban": false,
          "groupId": 5,
          "kpiInfo": {
            "definition": "Scope churn explain the change in the scope of sprint since the start of iteration",
            "formula": [
              {
                "lhs": "Scope Churn",
                "operator": "division",
                "operands": [
                  "Count of Stories added + Count of Stories removed",
                  " Count of Stories in Initial Commitment at the time of Sprint start"
                ]
              }
            ],
            "details": [
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83886244/Scope+Churn"
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
          "id": "65793ddc127be336160bc118",
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
          "combinedKpiSource": "Jira/Azure",
          "maxValue": "",
          "kanban": false,
          "groupId": 9,
          "kpiInfo": {
            "definition": " It gives a breakup of escaped defects by testing phase",
            "details": [
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/98140473/Release+Defect+count+by+Testing+phase"
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
          "xaxisLabel": "",
          "yaxisLabel": "",
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddc127be336160bc119",
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
          "combinedKpiSource": "Jira/Azure",
          "maxValue": "",
          "kanban": false,
          "groupId": 9,
          "kpiInfo": {
            "definition": "It depicts the progress of each epic in a release in terms of total count and %age completion.",
            "details": [
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/79986705/Release+Epic+Progress"
                }
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
          "id": "65793ddc127be336160bc11a",
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
          "combinedKpiSource": "Jira/Azure",
          "maxValue": "",
          "kanban": false,
          "groupId": 9,
          "kpiInfo": {
            "definition": "It depicts the progress of each epic in terms of total count and %age completion.",
            "details": [
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83820662/Epic+Progress"
                }
              }
            ]
          },
          "kpiFilter": "radioButton",
          "kpiWidth": 100,
          "trendCalculative": false,
          "xaxisLabel": "",
          "yaxisLabel": "",
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddc127be336160bc11b",
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
          "combinedKpiSource": "Jira/Azure",
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
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83886303/Iteration+Readiness"
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
          "id": "65793ddc127be336160bc11c",
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
          "combinedKpiSource": "Jira/Azure",
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
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/71663785/DORA+Mean+time+to+Restore"
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
          "id": "65793ddc127be336160bc11d",
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
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83886180/Sonar+Code+Quality"
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
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65793ddc127be336160bc11e",
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
          "combinedKpiSource": "Jira/Azure",
          "thresholdValue": 40,
          "kanban": false,
          "groupId": 11,
          "kpiInfo": {
            "definition": "The percentage of time spent in work states vs wait states across the lifecycle of an issue",
            "details": [
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/71827496/Flow+Efficiency"
                }
              }
            ]
          },
          "kpiFilter": "dropDown",
          "aggregationCriteria": "average",
          "trendCalculative": false,
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65c460eb89d20c2ab381c360",
          "kpiId": "kpi171",
          "kpiName": "Cycle Time",
          "isDeleted": "False",
          "defaultOrder": 12,
          "kpiCategory": "Backlog",
          "kpiSubCategory": "Flow KPIs",
          "kpiUnit": "Days",
          "chartType": "",
          "showTrend": false,
          "boxType": "2_column",
          "calculateMaturity": false,
          "hideOverallFilter": false,
          "kpiSource": "Jira",
          "combinedKpiSource": "Jira/Azure",
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
          "kpiFilter": "multiSelectDropDown",
          "trendCalculative": false,
          "xaxisLabel": "",
          "yaxisLabel": "",
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65eeb08194c86b415f978935",
          "kpiId": "kpi172",
          "kpiName": "Build Frequency",
          "isDeleted": "False",
          "defaultOrder": 24,
          "kpiUnit": "",
          "chartType": "line",
          "upperThresholdBG": "white",
          "lowerThresholdBG": "red",
          "showTrend": true,
          "isPositiveTrend": true,
          "calculateMaturity": true,
          "hideOverallFilter": true,
          "kpiSource": "Jenkins",
          "combinedKpiSource": "Jenkins/Bamboo/GitHubAction/AzurePipeline/Teamcity",
          "maxValue": "",
          "thresholdValue": 8,
          "kanban": false,
          "groupId": 1,
          "kpiInfo": {
            "definition": "Build frequency refers the number of successful builds done in a specific time frame.",
            "details": [
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/92930049/Build+Frequency"
                }
              }
            ]
          },
          "kpiFilter": "dropDown",
          "aggregationCriteria": "average",
          "maturityRange": [
            "1-2",
            "2-4",
            "5-8",
            "8-10",
            "10-"
          ],
          "trendCalculative": false,
          "xaxisLabel": "Weeks",
          "yaxisLabel": "Builds Count",
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65eeb08194c86b415f97893a",
          "kpiId": "kpi173",
          "kpiName": "Rework Rate",
          "isDeleted": "false",
          "defaultOrder": 5,
          "kpiCategory": "Developer",
          "kpiUnit": "%",
          "chartType": "line",
          "showTrend": true,
          "isPositiveTrend": false,
          "calculateMaturity": false,
          "hideOverallFilter": true,
          "kpiSource": "BitBucket",
          "combinedKpiSource": "RepoTool",
          "maxValue": "",
          "kanban": false,
          "groupId": 2,
          "kpiInfo": {
            "definition": "Percentage of code changes in which an engineer rewrites code that they recently updated (within the past three weeks).",
            "details": [
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/106528769/Developer+Rework+Rate"
                }
              }
            ]
          },
          "kpiFilter": "dropDown",
          "aggregationCriteria": "average",
          "maturityRange": [
            "-80",
            "80-50",
            "50-20",
            "20-5",
            "5-"
          ],
          "isRepoToolKpi": true,
          "trendCalculative": false,
          "xaxisLabel": "Weeks",
          "yaxisLabel": "Percentage",
          "isAdditionalFilterSupport": false
        },
        {
          "id": "65f1299e81460113cec05244",
          "kpiId": "kpi176",
          "kpiName": "Risks And Dependencies",
          "isDeleted": "False",
          "defaultOrder": 6,
          "kpiCategory": "Iteration",
          "kpiSubCategory": "Iteration Review",
          "kpiUnit": "Count",
          "chartType": "",
          "showTrend": false,
          "calculateMaturity": false,
          "hideOverallFilter": false,
          "kpiSource": "Jira",
          "combinedKpiSource": "Jira/Azure",
          "maxValue": "",
          "kanban": false,
          "groupId": 8,
          "kpiInfo": {
            "definition": "It displayed all the risks and dependencies tagged in a sprint",
            "details": [
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/102891521/Risks+and+Dependencies"
                }
              }
            ]
          },
          "kpiFilter": "radioButton",
          "trendCalculative": false,
          "xaxisLabel": "",
          "yaxisLabel": "",
          "isAdditionalFilterSupport": false
        },
        {
          "id": "6656a39c143cbb0ff82981b8",
          "kpiId": "kpi178",
          "kpiName": "Defect Count By",
          "isDeleted": "False",
          "defaultOrder": 1,
          "kpiCategory": "Release",
          "kpiSubCategory": "Quality",
          "chartType": "chartWithFilter",
          "showTrend": false,
          "boxType": "chart",
          "calculateMaturity": false,
          "hideOverallFilter": false,
          "kpiSource": "Jira",
          "combinedKpiSource": "Jira/Azure",
          "kanban": false,
          "groupId": 9,
          "kpiInfo": {
            "definition": "It shows the breakup of all defects tagged to a release grouped by Status, Priority, or RCA.",
            "details": [
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/144146433/Release+Defect+count+by"
                }
              }
            ]
          },
          "kpiFilter": "",
          "trendCalculative": false,
          "isAdditionalFilterSupport": false
        },
        {
          "id": "66616fc0ff078f6bc1ecf38d",
          "kpiId": "kpi179",
          "kpiName": "Release Plan",
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
          "combinedKpiSource": "Jira/Azure",
          "maxValue": "",
          "kanban": false,
          "groupId": 9,
          "kpiInfo": {
            "definition": "Displays the cumulative daily planned dues of the release based on the due dates of work items within the release scope.\n\nAdditionally, it provides an overview of the entire release scope.",
            "details": [
              {
                "type": "link",
                "kpiLinkDetail": {
                  "text": "Detailed Information at",
                  "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/147652609/Release+Release+Plan"
                }
              }
            ]
          },
          "kpiFilter": "",
          "kpiWidth": 100,
          "trendCalculative": false,
          "xaxisLabel": "",
          "yaxisLabel": "Count",
          "isAdditionalFilterSupport": false
        }
      ],
      "filters": {
        "projectTypeSwitch": {
          "enabled": true,
          "visible": true
        },
        "primaryFilter": {
          "type": "singleSelect",
          "defaultLevel": {
            "labelName": "project",
            "sortBy": null
          }
        },
        "parentFilter": {
          "labelName": "Organization Level"
        }
      }
    }
  ],
  "configDetails": {
    "kpiWiseAggregationType": {
      "kpi159": "sum",
      "kpi158": "average",
      "kpi114": "sum",
      "kpi997": "sum",
      "kpi116": "average",
      "kpi118": "sum",
      "kpi82": "average",
      "kpi37": "average",
      "kpi38": "sum",
      "kpi39": "sum",
      "kpi73": "sum",
      "kpi74": "sum",
      "kpi153": "sum",
      "kpi111": "average",
      "kpi34": "average",
      "kpi157": "average",
      "kpi113": "sum",
      "kpi35": "average",
      "kpi36": "sum",
      "kpi148": "sum",
      "kpi149": "average",
      "kpi5": "average",
      "kpi8": "average",
      "kpi50": "sum",
      "kpi48": "sum",
      "kpi49": "sum",
      "kpi84": "average",
      "kpi40": "sum",
      "kpi42": "average",
      "kpi146": "sum",
      "kpi46": "sum",
      "kpi137": "average",
      "kpi139": "sum",
      "kpi16": "average",
      "kpi17": "average",
      "kpi51": "sum",
      "kpi53": "average",
      "kpi54": "sum",
      "kpi55": "sum",
      "kpi11": "average",
      "kpi58": "sum",
      "kpi14": "average",
      "kpi126": "sum",
      "kpi127": "sum",
      "kpi70": "average",
      "kpi71": "average",
      "kpi72": "average",
      "kpi27": "sum",
      "kpi28": "sum",
      "kpi160": "average",
      "kpi162": "average",
      "kpi62": "average",
      "kpi63": "average",
      "kpi64": "sum",
      "kpi65": "sum",
      "kpi166": "sum",
      "kpi66": "average",
      "kpi67": "sum"
    },
    "percentile": 90,
    "hierarchySelectionCount": 3,
    "dateRangeFilter": {
      "types": [
        "Days",
        "Weeks",
        "Months"
      ],
      "counts": [
        5,
        10,
        15
      ]
    },
    "repoToolFlag": false
  }
};

describe('FilterNewComponent', () => {
  let component: FilterNewComponent;
  let fixture: ComponentFixture<FilterNewComponent>;
  let getAuth: GetAuthService;
  let httpService: HttpService
  let sharedService: SharedService;
  let helperService: HelperService;
  let messageService: MessageService;

  afterEach(() => {
    fixture.destroy();
  });

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [FilterNewComponent],
      imports: [RouterTestingModule, HttpClientModule, BrowserAnimationsModule],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],

      providers: [SharedService, GetAuthService, HttpService, HelperService, CommonModule, DatePipe, MessageService,
        { provide: APP_CONFIG, useValue: AppConfig }
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(FilterNewComponent);
    component = fixture.componentInstance;
    getAuth = TestBed.get(GetAuthService);
    httpService = TestBed.inject(HttpService);
    sharedService = TestBed.inject(SharedService);
    helperService = TestBed.inject(HelperService);
    messageService = TestBed.inject(MessageService);
    component.subscriptions = [];
    fixture.detectChanges();
    component.boardData = boardData;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });





  it('should set selectedTab and selectedType in ngOnInit', () => {
    spyOn(sharedService, 'getSelectedTab').and.returnValue('iteration');
    spyOn(helperService, 'getBackupOfFilterSelectionState').and.returnValue(null);

    component.ngOnInit();

    expect(component.selectedTab).toBe('iteration');
    expect(component.selectedType).toBe('scrum');
  });

  it('should set kanban to true if selectedType is "kanban"', () => {
    spyOn(sharedService, 'getSelectedTab').and.returnValue(null);
    spyOn(helperService, 'getBackupOfFilterSelectionState').and.returnValue('kanban');

    component.ngOnInit();

    expect(component.kanban).toBe(true);
  });

  it('should subscribe to globalDashConfigData and process boardData', () => {
    component.selectedTab = 'release';
    component.selectedType = 'kanban';
    spyOn(sharedService.globalDashConfigData, 'subscribe').and.callFake(callback => {
      callback(boardData);
    });
    spyOn(component, 'processBoardData');

    component.ngOnInit();
    expect(component.selectedType).toEqual('scrum');
    expect(component.processBoardData).toHaveBeenCalledWith(boardData);
  });


  it('should set selectedTab and selectedType in ngOnInit', () => {
    spyOn(sharedService, 'getSelectedTab').and.returnValue('iteration');
    spyOn(helperService, 'getBackupOfFilterSelectionState').and.returnValue(null);

    component.ngOnInit();

    expect(component.selectedTab).toBe('iteration');
    expect(component.selectedType).toBe('scrum');
  });

  it('should set kanban to true if selectedType is "kanban"', () => {
    spyOn(sharedService, 'getSelectedTab').and.returnValue(null);
    spyOn(helperService, 'getBackupOfFilterSelectionState').and.returnValue('kanban');

    component.ngOnInit();

    expect(component.kanban).toBe(true);
  });

  it('should set selectedDayType correctly', () => {
    const label = 'Weeks';
    component.setSelectedDateType(label);
    expect(component.selectedDayType).toBe(label);
  });

  it('should set selectedType and kanban correctly for scrum', () => {
    const type = 'Scrum';

    component.setSelectedType(type);

    expect(component.selectedType).toBe('scrum');
    expect(component.kanban).toBe(false);
  });

  it('should call service and helper methods correctly', () => {
    const type = 'scrum';
    component.boardData = boardData;
    component.selectedType = 'scrum';
    spyOn(sharedService, 'setSelectedTypeOrTabRefresh');
    spyOn(sharedService, 'setSelectedType');
    spyOn(helperService, 'setBackupOfFilterSelectionState');

    component.setSelectedType(type);

    expect(sharedService.setSelectedTypeOrTabRefresh).toHaveBeenCalledWith(component.selectedTab, component.selectedType);
    expect(sharedService.setSelectedType).toHaveBeenCalledWith(component.selectedType);
    expect(helperService.setBackupOfFilterSelectionState).toHaveBeenCalledWith({ 'selected_type': component.selectedType });
  });

  it('should call httpService.getFilterData() with selectedFilterData', () => {
    spyOn(httpService, 'getFilterData').and.returnValue(of({ success: true, data: { /* mock filter data */ } }));
    spyOn(component.cdr, 'detectChanges');

    component.getFiltersData();

    expect(httpService.getFilterData).toHaveBeenCalledWith(component.selectedFilterData);
    expect(component.cdr.detectChanges).toHaveBeenCalled();
  });

  it('should not call httpService.getFilterData() if filterDataArr has data for selectedType', () => {
    component.filterDataArr = { scrum: { /* mock filter data */ } };
    spyOn(httpService, 'getFilterData');
    spyOn(component.cdr, 'detectChanges');

    component.getFiltersData();

    expect(httpService.getFilterData).not.toHaveBeenCalled();
    expect(component.cdr.detectChanges).not.toHaveBeenCalled();
  });

  it('should process and store filter data correctly', () => {
    const data = [
      { labelName: 'Category 1', level: 2 },
      { labelName: 'Category 2', level: 1 },
      { labelName: 'Category 1', level: 3 },
      { labelName: 'Category 2', level: 4 }
    ];

    component.processFilterData(data);

    expect(component.filterDataArr).toEqual({
      [component.selectedType]: {
        'Category 1': [
          { labelName: 'Category 1', level: 2 },
          { labelName: 'Category 1', level: 3 }
        ],
        'Category 2': [
          { labelName: 'Category 2', level: 1 },
          { labelName: 'Category 2', level: 4 }
        ]
      }
    });
  });


  it('should set selectedLevel correctly for string input', () => {
    const event = 'LEVEL 1';

    component.handleParentFilterChange(event);

    expect(component.selectedLevel).toBe('level 1');
  });

  it('should set selectedLevel correctly for non-string input', () => {
    const event = { level: 2 };

    component.handleParentFilterChange(event);

    expect(component.selectedLevel).toEqual({ level: 2 });
  });

  it('should set colors correctly for valid data', () => {
    const data = [
      { nodeId: 1, nodeName: 'Node 1' },
      { nodeId: 2, nodeName: 'Node 2' },
      { nodeId: 3, nodeName: 'Node 3' }
    ];

    component.setColors(data);

    expect(component.colorObj).toEqual({
      1: { nodeName: 'Node 1', color: '#6079C5', nodeId: 1 },
      2: { nodeName: 'Node 2', color: '#FFB587', nodeId: 2 },
      3: { nodeName: 'Node 3', color: '#D48DEF', nodeId: 3 }
    });
  });

  it('should not set colors if data is empty', () => {
    const data = [];

    component.setColors(data);

    expect(component.colorObj).toEqual({});
  });

  it('should call service.setColorObj() with colorObj after a timeout', (done) => {
    const data = [
      { nodeId: 1, nodeName: 'Node 1' }
    ];
    spyOn(sharedService, 'setColorObj');

    component.setColors(data);
    expect(sharedService.setColorObj).toHaveBeenCalledWith(component.colorObj);
    done();

  });

  it('should return keys of object if object is not null and has keys', () => {
    const obj = { key1: 'value1', key2: 'value2' };

    const result = component.getObjectKeys(obj);

    expect(result).toEqual(['key1', 'key2']);
  });

  it('should return an empty array if object is null', () => {
    const obj = null;

    const result = component.getObjectKeys(obj);

    expect(result).toEqual([]);
  });

  it('should return an empty array if object has no keys', () => {
    const obj = {};

    const result = component.getObjectKeys(obj);

    expect(result).toEqual([]);
  });

  it('should remove the filter with the given id if there are more than one filters', () => {
    component.colorObj = {
      1: { nodeName: 'Node 1', color: '#6079C5', nodeId: 1 },
      2: { nodeName: 'Node 2', color: '#FFB587', nodeId: 2 }
    };
    component.filterDataArr = {
      scrum: {
        project: [
          { nodeId: 1, labelName: 'Category 1' },
          { nodeId: 2, labelName: 'Category 2' }
        ]
      }
    };
    component.selectedLevel = 'project';
    component.selectedType = 'scrum';
    spyOn(sharedService, 'setColorObj');
    spyOn(component, 'handlePrimaryFilterChange');
    spyOn(helperService, 'setBackupOfFilterSelectionState');

    component.removeFilter(1);

    expect(component.colorObj).toEqual({
      2: { nodeName: 'Node 2', color: '#FFB587', nodeId: 2 }
    });
    // expect(sharedService.setColorObj).toHaveBeenCalledWith(component.colorObj);
    expect(component.handlePrimaryFilterChange).toHaveBeenCalled();
    expect(helperService.setBackupOfFilterSelectionState).toHaveBeenCalled();
  });

  it('should not remove the filter if there is only one filter', () => {
    component.colorObj = {
      1: { nodeName: 'Node 1', color: '#6079C5', nodeId: 1 }
    };
    component.filterDataArr = {
      selectedType: {
        selectedLevel: [
          { nodeId: 1, labelName: 'Category 1' }
        ]
      }
    };
    spyOn(sharedService, 'setColorObj');
    spyOn(component, 'handlePrimaryFilterChange');
    spyOn(helperService, 'setBackupOfFilterSelectionState');

    component.removeFilter(1);

    expect(component.colorObj).toEqual({
      1: { nodeName: 'Node 1', color: '#6079C5', nodeId: 1 }
    });
    expect(sharedService.setColorObj).not.toHaveBeenCalled();
    expect(component.handlePrimaryFilterChange).not.toHaveBeenCalled();
    expect(helperService.setBackupOfFilterSelectionState).not.toHaveBeenCalled();
  });

  it('should return true if two arrays are equal', () => {
    const arr1 = [1, 2, 3];
    const arr2 = [1, 2, 3];

    const result = component.arraysEqual(arr1, arr2);

    expect(result).toBe(true);
  });

  it('should return false if two arrays have different lengths', () => {
    const arr1 = [1, 2, 3];
    const arr2 = [1, 2];

    const result = component.arraysEqual(arr1, arr2);

    expect(result).toBe(false);
  });

  it('should return false if two arrays have different elements', () => {
    const arr1 = [1, 2, 3];
    const arr2 = [1, 4, 3];

    const result = component.arraysEqual(arr1, arr2);

    expect(result).toBe(false);
  });

  it('should return true if two objects are deeply equal', () => {
    const obj1 = { name: 'John', age: 30 };
    const obj2 = { name: 'John', age: 30 };

    const result = component.deepEqual(obj1, obj2);

    expect(result).toBe(true);
  });

  it('should return false if two objects have different keys', () => {
    const obj1 = { name: 'John', age: 30 };
    const obj2 = { name: 'John', gender: 'male' };

    const result = component.deepEqual(obj1, obj2);

    expect(result).toBe(false);
  });

  it('should return false if two objects have different values', () => {
    const obj1 = { name: 'John', age: 30 };
    const obj2 = { name: 'John', age: 40 };

    const result = component.deepEqual(obj1, obj2);

    expect(result).toBe(false);
  });


  it('should set selected trends if event is not null and has length', () => {
    const event = [{ level: 'Level 1', labelName: 'Label 1', nodeId: 1 }];
    component.filterDataArr = {
      scrum: {
        project: [
          { nodeId: 1, labelName: 'Category 1' },
          { nodeId: 2, labelName: 'Category 2' }
        ],
        sprint: [
          { nodeId: 3, labelName: 'Category 3', parentId: 'ABCD_1234' },
          { nodeId: 4, labelName: 'Category 4', parentId: 'ABCDE_1234' }
        ],
        sqd: [
          { nodeId: 5, labelName: 'Category 5', parentId: 'ABCDEF_1234' },
          { nodeId: 6, labelName: 'Category 6', parentId: 'ABCDEFGH_1234' }
        ]
      }
    };
    component.additionalFilterConfig = [
      {
        "type": "multiSelect",
        "defaultLevel": {
          "labelName": "sprint",
          "sortBy": null
        }
      },
      {
        "type": "multiSelect",
        "defaultLevel": {
          "labelName": "sqd",
          "sortBy": null
        }
      }
    ];
    component.selectedLevel = 'project';
    component.selectedType = 'scrum';
    component.kanban = false;
    spyOn(sharedService, 'setSelectedTrends');

    component.handlePrimaryFilterChange(event);

    expect(sharedService.setSelectedTrends).toHaveBeenCalledWith(event);
  });


  it('should not set selected trends if event is null or has no length', () => {
    const event = null;
    spyOn(sharedService, 'setSelectedTrends');

    component.handlePrimaryFilterChange(event);

    expect(sharedService.setSelectedTrends).not.toHaveBeenCalled();
  });


  it('should set selected date filter and call setSelectedDateFilter()', () => {
    const selectedDateValue = 1;
    const selectedDayType = 'Weeks';
    component.selectedLevel = 'project';
    spyOn(sharedService, 'setSelectedDateFilter');
    component.filterApplyData = {
      selectedMap: {
        date: ''
      }
    };
    component.filterDataArr = {
      scrum: {
        project: [
          { nodeId: 1, labelName: 'Category 1' },
          { nodeId: 2, labelName: 'Category 2' }
        ],
        sprint: [
          { nodeId: 3, labelName: 'Category 3', parentId: 'ABCD_1234' },
          { nodeId: 4, labelName: 'Category 4', parentId: 'ABCDE_1234' }
        ],
        sqd: [
          { nodeId: 5, labelName: 'Category 5', parentId: 'ABCDEF_1234' },
          { nodeId: 6, labelName: 'Category 6', parentId: 'ABCDEFGH_1234' }
        ]
      }
    };
    component.applyDateFilter();

    // expect(component.selectedDateFilter).toBe(`${selectedDateValue} ${selectedDayType}`);
    // expect(sharedService.setSelectedDateFilter).toHaveBeenCalledWith(selectedDayType);
  });

  it('should set filterApplyData and call service.select()', () => {
    const selectedDateValue = 1;
    const selectedDayType = 'Weeks';
    component.selectedLevel = 'project';
    component.selectedTab = 'Tab 1';
    component.filterApplyData = {
      selectedMap: {
        date: ''
      }
    };
    component.filterDataArr = {
      scrum: {
        project: [
          { nodeId: 1, labelName: 'Category 1' },
          { nodeId: 2, labelName: 'Category 2' }
        ],
        sprint: [
          { nodeId: 3, labelName: 'Category 3', parentId: 'ABCD_1234' },
          { nodeId: 4, labelName: 'Category 4', parentId: 'ABCDE_1234' }
        ],
        sqd: [
          { nodeId: 5, labelName: 'Category 5', parentId: 'ABCDEF_1234' },
          { nodeId: 6, labelName: 'Category 6', parentId: 'ABCDEFGH_1234' }
        ]
      }
    };
    component.masterData = {};
    component.boardData = { configDetails: {} };
    component.filterApplyData = { selectedMap: {} };
    spyOn(sharedService, 'select');

    component.applyDateFilter();

    // expect(component.filterApplyData['selectedMap']['date']).toEqual([selectedDayType]);
    // expect(component.filterApplyData['ids']).toEqual([selectedDateValue]);
    expect(sharedService.select).toHaveBeenCalledWith(component.masterData, component.filterDataArr['scrum']['project'], component.filterApplyData, component.selectedTab, false, true, component.boardData['configDetails'], true);
  });

  it('should update selectedProjectLastSyncDate on fetch data success ', fakeAsync(() => {

    component.selectedSprint = {
      "nodeId": "43310_ABFZyDaLnk_64942ed8eb73c425e4d7ba8d",
      "nodeName": "KnowHOW | PI_13| ITR_6_ABFZyDaLnk",
      "sprintStartDate": "2023-06-07T11:52:00.0000000",
      "sprintEndDate": "2023-06-27T11:52:00.0000000",
      "sprintState": "ACTIVE",
      "level": 6
    };
    const getActiveIterationStatusSpy = spyOn(httpService, 'getActiveIterationStatus').and.returnValue(of({
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

  it('should not update selectedProjectLastSyncDate on fetch data failure ', fakeAsync(() => {

    component.selectedSprint = {
      "nodeId": "43310_ABFZyDaLnk_64942ed8eb73c425e4d7ba8d",
      "nodeName": "KnowHOW | PI_13| ITR_6_ABFZyDaLnk",
      "sprintStartDate": "2023-06-07T11:52:00.0000000",
      "sprintEndDate": "2023-06-27T11:52:00.0000000",
      "sprintState": "ACTIVE",
      "level": 6
    };
    const getActiveIterationStatusSpy = spyOn(httpService, 'getActiveIterationStatus').and.returnValue(of({
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

  it('should get error while fetching active iteration data', fakeAsync(() => {

    component.selectedSprint = {
      "nodeId": "43310_ABFZyDaLnk_64942ed8eb73c425e4d7ba8d",
      "nodeName": "KnowHOW | PI_13| ITR_6_ABFZyDaLnk",
      "sprintStartDate": "2023-06-07T11:52:00.0000000",
      "sprintEndDate": "2023-06-27T11:52:00.0000000",
      "sprintState": "ACTIVE",
      "level": 6
    };
    const getActiveIterationStatusSpy = spyOn(httpService, 'getActiveIterationStatus').and.returnValue(of({
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

  it('should get error while fetching active iteration data and getactiveIterationfetchStatus is false itself', fakeAsync(() => {
    component.selectedSprint = {
      "nodeId": "43310_ABFZyDaLnk_64942ed8eb73c425e4d7ba8d",
      "nodeName": "KnowHOW | PI_13| ITR_6_ABFZyDaLnk",
      "sprintStartDate": "2023-06-07T11:52:00.0000000",
      "sprintEndDate": "2023-06-27T11:52:00.0000000",
      "sprintState": "ACTIVE",
      "level": 6
    };
    const getActiveIterationStatusSpy = spyOn(httpService, 'getActiveIterationStatus').and.returnValue(of({
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

  it('should get error while fetching active iteration data and getactiveIterationfetchStatus is throw error', fakeAsync(() => {
    component.selectedSprint = {
      "nodeId": "43310_ABFZyDaLnk_64942ed8eb73c425e4d7ba8d",
      "nodeName": "KnowHOW | PI_13| ITR_6_ABFZyDaLnk",
      "sprintStartDate": "2023-06-07T11:52:00.0000000",
      "sprintEndDate": "2023-06-27T11:52:00.0000000",
      "sprintState": "ACTIVE",
      "level": 6
    };
    const getActiveIterationStatusSpy = spyOn(httpService, 'getActiveIterationStatus').and.returnValue(of({
      "message": "Got HTTP response: 200 on url: http://localhost:50008/activeIteration/fetch",
      "success": true
    }));

    spyOn(httpService, 'getactiveIterationfetchStatus').and.returnValue(throwError('Error'));

    component.fetchData();
    tick(10000);
    expect(getActiveIterationStatusSpy).toHaveBeenCalled();
    expect(component.selectedProjectLastSyncStatus).toEqual('');
  }));


  it('should set filterApplyData and call service.select() if event is not null and has length', () => {
    const event = [{ level: 'Level 1', labelName: 'Label 1', nodeId: 1 }];
    component.filterApplyData = {
      level: '',
      label: '',
      ids: [],
      selectedMap: {
        project: [],
        sprint: []
      }

    };
    component.selectedLevel = 'Level 1';
    component.selectedTab = 'Backlog';
    component.selectedType = 'Type 1';
    component.filterDataArr = {
      'Type 1': {
        'Level 1': {},
        project: {},
        sprint: [{ nodeId: 1, parentId: [1], sprintState: 'Closed' }]
      }
    };
    component.masterData = {};
    component.boardData = { configDetails: {} };
    spyOn(sharedService, 'select');

    component.handleAdditionalChange(event);

    expect(component.filterApplyData['level']).toBe(event[0].level);
    expect(component.filterApplyData['label']).toBe(event[0].labelName);
    // expect(component.filterApplyData['ids']).toEqual([event[0].nodeId]);
    // expect(component.filterApplyData['selectedMap'][event[0].labelName]).toEqual([event[0].nodeId]);
    // expect(component.filterApplyData['selectedMap']['sprint']).toEqual([1]);
    // expect(sharedService.select).toHaveBeenCalledWith(component.masterData, component.filterDataArr['Type 1']['Level 1'], component.filterApplyData, component.selectedTab, false, true, component.boardData['configDetails'], true);
  });

  it('should call handlePrimaryFilterChange() if event is null or has no length', () => {
    const event = null;
    component.previousFilterEvent = [{ level: 'Level 1', labelName: 'Label 1', nodeId: 1 }];
    spyOn(component, 'handlePrimaryFilterChange');

    component.handleAdditionalChange(event);

    expect(component.handlePrimaryFilterChange).toHaveBeenCalledWith(component.previousFilterEvent);
  });

  it('should set processor log details when response is successful', () => {
    const mockBasicProjectConfigId = '123';
    const mockResponse = { success: true, data: { /* mock data */ } };

    spyOn(httpService, 'getProcessorsTraceLogsForProject').and.returnValue(of(mockResponse));
    spyOn(component.service, 'setProcessorLogDetails');

    component.previousFilterEvent = [{ basicProjectConfigId: mockBasicProjectConfigId }];
    component.getProcessorsTraceLogsForProject();

    expect(httpService.getProcessorsTraceLogsForProject).toHaveBeenCalledWith(mockBasicProjectConfigId);
    expect(component.service.setProcessorLogDetails).toHaveBeenCalledWith(mockResponse.data);
  });

  it('should show error message when response is not successful', () => {
    const mockBasicProjectConfigId = '123';
    const mockResponse = { success: false };

    spyOn(httpService, 'getProcessorsTraceLogsForProject').and.returnValue(of(mockResponse));
    spyOn(messageService, 'add');

    component.previousFilterEvent = [{ basicProjectConfigId: mockBasicProjectConfigId }];
    component.getProcessorsTraceLogsForProject();

    expect(httpService.getProcessorsTraceLogsForProject).toHaveBeenCalledWith(mockBasicProjectConfigId);
    expect(messageService.add).toHaveBeenCalledOnceWith({
      severity: 'error',
      summary: "Error in fetching processor's execution date. Please try after some time."
    });
  });

  it('should update filterApplyData and selectedSprint when selectedTab is iteration', () => {
    component.additionalFilterConfig = [
      {
        "type": "multiSelect",
        "defaultLevel": {
          "labelName": "sprint",
          "sortBy": null
        }
      },
      {
        "type": "multiSelect",
        "defaultLevel": {
          "labelName": "sqd",
          "sortBy": null
        }
      }
    ];
    component.selectedType = 'Type 1';
    component.filterDataArr = {
      'Type 1': {
        'Level 1': {},
        project: {},
        sprint: [{ nodeId: 'sprint1', parentId: [1], sprintState: 'Closed' }]
      }
    };
    const mockEvent = [
      {
        nodeId: 'sprint1',
        sprintEndDate: '2022-06-30',
        sprintStartDate: '2022-06-01'
      }
    ];

    component.selectedTab = 'iteration';
    component.filterApplyData = {};
    component.selectedSprint = null;
    component.additionalData = false;

    component.handlePrimaryFilterChange(mockEvent);

    expect(component.filterApplyData['ids']).toEqual(['sprint1']);
    expect(component.selectedSprint).toEqual(mockEvent[0]);
    expect(component.additionalData).toBe(true);
  });

  it('should set additionalData to false when selectedTab is not iteration', () => {
    component.additionalFilterConfig = [
      {
        "type": "multiSelect",
        "defaultLevel": {
          "labelName": "sprint",
          "sortBy": null
        }
      },
      {
        "type": "multiSelect",
        "defaultLevel": {
          "labelName": "sqd",
          "sortBy": null
        }
      }
    ];
    component.selectedType = 'Type 1';
    component.filterDataArr = {
      'Type 1': {
        'Level 1': {},
        project: {},
        sprint: [{ nodeId: 'sprint1', parentId: [1], sprintState: 'Closed' }]
      }
    };
    const mockEvent = [
      {
        nodeId: 'sprint1',
        sprintEndDate: '2022-06-30',
        sprintStartDate: '2022-06-01'
      }
    ];

    component.selectedTab = 'backlog';
    component.additionalData = true;

    component.handlePrimaryFilterChange(mockEvent);

    expect(component.additionalData).toBe(false);
  });

  it('should call service.select with correct parameters when selectedLevel is a string', () => {
    component.additionalFilterConfig = [
      {
        "type": "multiSelect",
        "defaultLevel": {
          "labelName": "sprint",
          "sortBy": null
        }
      },
      {
        "type": "multiSelect",
        "defaultLevel": {
          "labelName": "sqd",
          "sortBy": null
        }
      }
    ];
    component.selectedType = 'type1';
    const mockEvent = [
      {
        nodeId: 'sprint1',
        sprintEndDate: '2022-06-30',
        sprintStartDate: '2022-06-01'
      }
    ];
    component.filterDataArr = {
      'type1': {
        'Level1': {},
        project: {},
        sprint: [{ nodeId: 'sprint1', parentId: [1], sprintState: 'Closed' }]
      }
    };
    component.selectedLevel = 'level 1';
    component.masterData = {};
    component.filterApplyData = {};
    component.selectedTab = 'iteration';
    component.boardData = {
      configDetails: {}
    };

    spyOn(component.service, 'select');

    component.handlePrimaryFilterChange(mockEvent);

    expect(component.service.select).toHaveBeenCalledOnceWith(
      component.masterData,
      component.filterDataArr['type1'].level1,
      component.filterApplyData,
      component.selectedTab,
      false,
      true,
      component.boardData['configDetails'],
      true
    );
  });

  it('should call service.select with correct parameters when selectedLevel is an object', () => {
    component.additionalFilterConfig = [
      {
        "type": "multiSelect",
        "defaultLevel": {
          "labelName": "sprint",
          "sortBy": null
        }
      },
      {
        "type": "multiSelect",
        "defaultLevel": {
          "labelName": "sqd",
          "sortBy": null
        }
      }
    ];
    component.selectedType = 'type1';
    component.filterDataArr = {
      'type1': {
        'Level1': [{ nodeId: 'level1' }],
        project: {},
        sprint: [{ nodeId: 'sprint1', parentId: [1], sprintState: 'Closed' }]
      }
    };
    const mockEvent = [
      {
        nodeId: 'sprint1',
        sprintEndDate: '2022-06-30',
        sprintStartDate: '2022-06-01'
      }
    ];

    component.selectedLevel = { emittedLevel: 'level1' };
    component.masterData = {};
    component.filterApplyData = {};
    component.selectedTab = 'iteration';
    component.boardData = {
      configDetails: {}
    };

    spyOn(component.service, 'select');

    component.handlePrimaryFilterChange(mockEvent);

    expect(component.service.select).toHaveBeenCalledOnceWith(
      component.masterData,
      component.filterDataArr['type1'].level1,
      component.filterApplyData,
      component.selectedTab,
      false,
      true,
      component.boardData['configDetails'],
      true
    );
  });

  it('should call service.select with correct parameters when selectedLevel is not defined', () => {
    component.additionalFilterConfig = [
      {
        "type": "multiSelect",
        "defaultLevel": {
          "labelName": "sprint",
          "sortBy": null
        }
      },
      {
        "type": "multiSelect",
        "defaultLevel": {
          "labelName": "sqd",
          "sortBy": null
        }
      }
    ];
    component.selectedType = 'type1';
    component.filterDataArr = {
      'type1': {
        project: [{ nodeId: 'project1' }, { nodeId: 'project2' }],
        sprint: [{ nodeId: 'sprint1', parentId: [1], sprintState: 'Closed' }]
      }
    };
    const mockEvent = [
      {
        nodeId: 'sprint1',
        sprintEndDate: '2022-06-30',
        sprintStartDate: '2022-06-01'
      }
    ];

    component.selectedLevel = null;
    component.masterData = {};
    component.filterApplyData = {};
    component.selectedTab = 'iteration';
    component.kanban = true;
    component.boardData = {
      configDetails: {}
    };

    spyOn(component.service, 'select');

    component.handlePrimaryFilterChange(mockEvent);

    expect(component.service.select).toHaveBeenCalledOnceWith(
      component.masterData,
      component.filterDataArr['type1'].project,
      component.filterApplyData,
      component.selectedTab,
      false,
      true,
      component.boardData['configDetails'],
      true
    );
  });
});
