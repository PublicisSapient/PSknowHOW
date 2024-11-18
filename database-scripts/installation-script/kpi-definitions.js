
//The below three categories are the names used on three tabs and SCRUM/Kanban KPI are classified in these categories. you can change the names.
db.getCollection('kpi_category').insertMany(
[
  {
    "categoryId": "categoryOne",
    "categoryName": "Team Efficiency"
  },
  {
    "categoryId": "categoryTwo",
    "categoryName": "Team Quality"
  },
  {
    "categoryId": "categoryThree",
    "categoryName": "Team Delivery"
  }
]);

//Definition of all KPI in PSknowHOW, you can also edit Maturity range in applicable KPI
db.getCollection('kpi_master').insertMany(
[
  {
    "kpiId": "kpi14",
    "kpiName": "Defect Injection Rate",
    "maxValue": "200",
    "kpiUnit": "%",
    "isDeleted": "False",
    "defaultOrder": 1,
    "kpiSource": "Jira",
    "groupId": 2,
    "thresholdValue": "10",
    "kanban": false,
    "chartType": "line",
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
    "upperThresholdBG" : "red",
    "lowerThresholdBG" : "white",
    "xAxisLabel": "Sprints",
    "yAxisLabel": "Percentage",
    "isPositiveTrend": false,
    "showTrend": true,
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": true,
    "maturityRange": [
      "-175",
      "175-125",
      "125-75",
      "75-25",
      "25-"
    ]
  },
  {
    "kpiId": "kpi82",
    "kpiName": "First Time Pass Rate",
    "maxValue": "100",
    "kpiUnit": "%",
    "isDeleted": "False",
    "defaultOrder": 2,
    "kpiSource": "Jira",
    "groupId": 1,
    "thresholdValue": "75",
    "kanban": false,
    "chartType": "line",
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
    "upperThresholdBG" : "white",
    "lowerThresholdBG" : "red",
    "xAxisLabel": "Sprints",
    "yAxisLabel": "Percentage",
    "isPositiveTrend": true,
    "showTrend": true,
    "kpiFilter": "multiSelectDropDown",
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": true,
    "maturityRange": [
      "-25",
      "25-50",
      "50-75",
      "75-90",
      "90-"
    ]
  },
  {
    "kpiId": "kpi111",
    "kpiName": "Defect Density",
    "maxValue": "500",
    "kpiUnit": "%",
    "isDeleted": "False",
    "defaultOrder": 3,
    "kpiSource": "Jira",
    "groupId": 1,
    "thresholdValue": "25",
    "kanban": false,
    "chartType": "line",
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
    "upperThresholdBG" : "red",
    "lowerThresholdBG" : "white",
    "xAxisLabel": "Sprints",
    "yAxisLabel": "Percentage",
    "isPositiveTrend": false,
    "showTrend": true,
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": true,
    "maturityRange": [
      "-90",
      "90-60",
      "60-25",
      "25-10",
      "10-"
    ]
  },
  {
    "kpiId": "kpi35",
    "kpiName": "Defect Seepage Rate",
    "maxValue": "100",
    "kpiUnit": "%",
    "isDeleted": "False",
    "defaultOrder": 4,
    "kpiSource": "Jira",
    "groupId": 1,
    "thresholdValue": "10",
    "kanban": false,
    "chartType": "line",
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
    "upperThresholdBG" : "red",
    "lowerThresholdBG" : "white",
    "xAxisLabel": "Sprints",
    "yAxisLabel": "Percentage",
    "isPositiveTrend": false,
    "showTrend": true,
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": true,
    "maturityRange": [
      "-90",
      "90-75",
      "75-50",
      "50-25",
      "25-"
    ]
  },
  {
    "kpiId": "kpi34",
    "kpiName": "Defect Removal Efficiency",
    "maxValue": "100",
    "kpiUnit": "%",
    "isDeleted": "False",
    "defaultOrder": 5,
    "kpiSource": "Jira",
    "groupId": 3,
    "thresholdValue": "90",
    "kanban": false,
    "chartType": "line",
    "kpiInfo": {
      "definition": "Measure of percentage of story linked defects fixed against the total number of defects raised in  the sprint.",
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
            "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27197457/Scrum+QUALITY+KPIs#Defect-Removal-Efficiency"
          }
        }
      ]
    },
    "upperThresholdBG" : "white",
    "lowerThresholdBG" : "red",
    "xAxisLabel": "Sprints",
    "yAxisLabel": "Percentage",
    "isPositiveTrend": true,
    "showTrend": true,
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": true,
    "maturityRange": [
      "-25",
      "25-50",
      "50-75",
      "75-90",
      "90-"
    ]
  },
  {
    "kpiId": "kpi37",
    "kpiName": "Defect Rejection Rate",
    "maxValue": "100",
    "kpiUnit": "%",
    "isDeleted": "False",
    "defaultOrder": 6,
    "kpiSource": "Jira",
    "groupId": 3,
    "thresholdValue": "10",
    "kanban": false,
    "chartType": "line",
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
    "upperThresholdBG" : "red",
    "lowerThresholdBG" : "white",
    "xAxisLabel": "Sprints",
    "yAxisLabel": "Percentage",
    "isPositiveTrend": false,
    "showTrend": true,
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": true,
    "maturityRange": [
      "-75",
      "75-50",
      "50-30",
      "30-10",
      "10-"
    ]
  },
  {
    "kpiId": "kpi28",
    "kpiName": "Defect Count By Priority",
    "maxValue": "90",
    "kpiUnit": "Number",
    "isDeleted": "False",
    "defaultOrder": 7,
    "kpiSource": "Jira",
    "groupId": 1,
    "thresholdValue": "55",
    "kanban": false,
    "chartType": "line",
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
    "upperThresholdBG" : "red",
    "lowerThresholdBG" : "white",
    "xAxisLabel": "Sprints",
    "yAxisLabel": "Count",
    "isPositiveTrend": false,
    "showTrend": true,
    "kpiFilter": "multiSelectDropDown",
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi36",
    "kpiName": "Defect Count By RCA",
    "maxValue": "100",
    "kpiUnit": "Number",
    "isDeleted": "False",
    "defaultOrder": 8,
    "kpiSource": "Jira",
    "groupId": 3,
    "thresholdValue": "55",
    "kanban": false,
    "chartType": "line",
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
    "upperThresholdBG" : "red",
    "lowerThresholdBG" : "white",
    "xAxisLabel": "Sprints",
    "yAxisLabel": "Count",
    "isPositiveTrend": false,
    "showTrend": true,
    "kpiFilter": "multiSelectDropDown",
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi126",
    "kpiName": "Created vs Resolved defects",
    "kpiUnit": "Number",
    "isDeleted": "False",
    "defaultOrder": 9,
    "kpiSource": "Jira",
    "groupId": 1,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "grouped_column_plus_line",
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
    "upperThresholdBG" : "white",
    "lowerThresholdBG" : "red",
    "xAxisLabel": "Sprints",
    "yAxisLabel": "Count",
    "isPositiveTrend": true,
    "isTrendCalculative": true,
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
    "showTrend": true,
    "lineLegend": "Resolved Defects",
    "barLegend": "Created Defects",
    "kpiFilter": "radioButton",
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": true,
    "maxValue": "300",
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi42",
    "kpiName": "Regression Automation Coverage",
    "isDeleted": "False",
    "defaultOrder": 10,
    "kpiSource": "Zypher",
    "groupId": 1,
    "maxValue": "100",
    "kpiUnit": "%",
    "kanban": false,
    "chartType": "line",
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
    "upperThresholdBG" : "white",
    "lowerThresholdBG" : "red",
    "xAxisLabel": "Sprints",
    "yAxisLabel": "Percentage",
    "isPositiveTrend": true,
    "showTrend": true,
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "maturityRange": [
      "-20",
      "20-40",
      "40-60",
      "60-80",
      "80-"
    ]
  },
  {
    "kpiId": "kpi16",
    "kpiName": "In-Sprint Automation Coverage",
    "maxValue": "100",
    "kpiUnit": "%",
    "isDeleted": "False",
    "defaultOrder": 11,
    "kpiSource": "Zypher",
    "groupId": 1,
    "thresholdValue": "80",
    "kanban": false,
    "chartType": "line",
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
    "upperThresholdBG" : "white",
    "lowerThresholdBG" : "red",
    "xAxisLabel": "Sprints",
    "yAxisLabel": "Percentage",
    "isPositiveTrend": true,
    "showTrend": true,
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "maturityRange": [
      "-20",
      "20-40",
      "40-60",
      "60-80",
      "80-"
    ]
  },
  {
    "kpiId": "kpi17",
    "kpiName": "Unit Test Coverage",
    "maxValue": "100",
    "kpiUnit": "%",
    "isDeleted": "False",
    "defaultOrder": 12,
    "kpiSource": "Sonar",
    "groupId": 1,
    "thresholdValue": "55",
    "kanban": false,
    "chartType": "line",
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
    "upperThresholdBG" : "white",
    "lowerThresholdBG" : "red",
    "yAxisLabel": "Percentage",
    "xAxisLabel": "Weeks",
    "isPositiveTrend": true,
    "showTrend": true,
    "kpiFilter": "multiSelectDropDown",
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "hideOverallFilter": false,
    "maturityRange": [
      "-20",
      "20-40",
      "40-60",
      "60-80",
      "80-"
    ]
  },
  {
    "kpiId": "kpi38",
    "kpiName": "Sonar Violations",
    "maxValue": "",
    "kpiUnit": "Number",
    "isDeleted": "False",
    "defaultOrder": 13,
    "kpiSource": "Sonar",
    "groupId": 1,
    "thresholdValue": "55",
    "kanban": false,
    "chartType": "line",
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
    "upperThresholdBG" : "red",
    "lowerThresholdBG" : "white",
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Count",
    "isPositiveTrend": false,
    "showTrend": true,
    "kpiFilter": "multiSelectDropDown",
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi27",
    "kpiName": "Sonar Tech Debt",
    "maxValue": "90",
    "kpiUnit": "Days",
    "isDeleted": "False",
    "defaultOrder": 14,
    "kpiSource": "Sonar",
    "groupId": 1,
    "thresholdValue": "55",
    "kanban": false,
    "chartType": "line",
    "kpiInfo": {
      "definition": "Time Estimate required to fix all Issues/code smells reported in Sonar code analysis.",
      "formula": [
        {
          "lhs":"It is calculated as effort to fix all Code Smells. The effort is calculated in minutes and converted to days by assuming 1 Day =8 Hours."
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
    "upperThresholdBG" : "red",
    "lowerThresholdBG" : "white",
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Days",
    "isPositiveTrend": false,
    "showTrend": true,
    "kpiFilter": "dropDown",
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "hideOverallFilter": true,
    "maturityRange": [
      "-100",
      "100-50",
      "50-30",
      "30-10",
      "10-"
    ]
  },
  {
    "kpiId": "kpi116",
    "kpiName": "Change Failure Rate",
    "kpiCategory": "Dora",
    "maxValue": "100",
    "kpiUnit": "%",
    "isDeleted": "False",
    "defaultOrder": 15,
    "kpiSource": "Jenkins",
    "groupId": 14,
    "thresholdValue": 0,
    "kanban": false,
    "chartType": "line",
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
    "upperThresholdBG" : "red",
    "lowerThresholdBG" : "white",
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Percentage",
    "isPositiveTrend": false,
    "showTrend": true,
    "kpiFilter": "dropDown",
    "aggregationCriteria": "average",
    "aggregationCircleCriteria" : "average",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "hideOverallFilter": true,
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
    ]
  },
  {
    "kpiId": "kpi70",
    "kpiName": "Test Execution and pass percentage",
    "isDeleted": "False",
    "defaultOrder": 16,
    "kpiSource": "Zypher",
    "groupId": 1,
    "maxValue": "100",
    "kpiUnit": "%",
    "kanban": false,
    "chartType": "grouped_column_plus_line",
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
    "upperThresholdBG" : "white",
    "lowerThresholdBG" : "red",
    "xAxisLabel": "Sprints",
    "yAxisLabel": "Percentage",
    "lineLegend": "Passed",
    "barLegend": "Executed",
    "isPositiveTrend": true,
    "showTrend": true,
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "maturityRange": [
      "-20",
      "20-40",
      "40-60",
      "60-80",
      "80-"
    ]
  },
  {
    "kpiId": "kpi40",
    "kpiName": "Issue Count",
    "maxValue": "",
    "kpiUnit": "",
    "isDeleted": "False",
    "defaultOrder": 17,
    "kpiSource": "Jira",
    "groupId": 1,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "line",
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
    "xAxisLabel": "Sprints",
    "yAxisLabel": "Count",
    "isPositiveTrend": true,
    "showTrend": false,
    "aggregationCriteria": "sum",
    "kpiFilter": "radioButton",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi72",
    "kpiName": "Commitment Reliability",
    "maxValue": "200",
    "kpiUnit": "%",
    "isDeleted": "False",
    "defaultOrder": 18,
    "kpiSource": "Jira",
    "groupId": 2,
    "thresholdValue": "85",
    "kanban": false,
    "chartType": "line",
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
    "upperThresholdBG" : "white",
    "lowerThresholdBG" : "red",
    "xAxisLabel": "Sprints",
    "yAxisLabel": "Percentage",
    "isPositiveTrend": true,
    "showTrend": true,
    "aggregationCriteria": "average",
    "kpiFilter": "dropDown",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": true,
    "maturityRange": [
      "-40",
      "40-60",
      "60-75",
      "75-90",
      "90-"
    ]
  },
  {
    "kpiId": "kpi5",
    "kpiName": "Sprint Predictability",
    "isDeleted": "False",
    "kpiInAggregatedFeed": "True",
    "defaultOrder": 19,
    "kpiOnDashboard": [
      "Aggregated"
    ],
    "kpiSource": "Jira",
    "groupId": 2,
    "maxValue": "10",
    "kpiUnit": "%",
    "thresholdValue": "",
    "kanban": false,
    "chartType": "line",
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
    "xAxisLabel": "Sprints",
    "yAxisLabel": "Percentage",
    "isPositiveTrend": true,
    "showTrend": false,
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi39",
    "kpiName": "Sprint Velocity",
    "maxValue": "300",
    "kpiUnit": "SP",
    "isDeleted": "False",
    "defaultOrder": 20,
    "kpiSource": "Jira",
    "groupId": 2,
    "thresholdValue": "",
    "kanban": false,
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
    "xAxisLabel": "Sprints",
    "yAxisLabel": "Count",
    "isPositiveTrend": true,
    "showTrend": false,
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": false,
	"lineLegend": "Sprint Velocity",
    "barLegend": "Last 5 Sprints Average",
	"chartType":"grouped_column_plus_line",
	"isTrendCalculative": true,
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
    ]
  },
  {
    "kpiId": "kpi46",
    "kpiName": "Sprint Capacity Utilization",
    "maxValue": "500",
    "kpiUnit": "Hours",
    "isDeleted": "False",
    "defaultOrder": 21,
    "kpiSource": "Jira",
    "groupId": 1,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "grouped_column_plus_line",
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
    "xAxisLabel": "Sprints",
    "yAxisLabel": "Hours",
    "lineLegend": "Logged",
    "barLegend": "Estimated",
    "isPositiveTrend": true,
    "showTrend": false,
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi84",
    "kpiName": "Mean Time To Merge",
    "maxValue": "10",
    "kpiUnit": "Hours",
    "isDeleted": "False",
    "defaultOrder": 22,
    "groupId": 1,
    "kpiSource": "BitBucket",
    "thresholdValue": "55",
    "kanban": false,
    "chartType": "line",
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
    "upperThresholdBG" : "red",
    "lowerThresholdBG" : "white",
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Count(Hours)",
    "isPositiveTrend": false,
    "showTrend": true,
    "kpiFilter": "dropDown",
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "hideOverallFilter": true,
    "isRepoToolKpi": false,
    "kpiCategory": "Developer",
    "maturityRange": [
      "-48",
      "48-16",
      "16-8",
      "8-4",
      "4-"
    ]
  },
  {
    "kpiId": "kpi11",
    "kpiName": "Check-Ins & Merge Requests",
    "maxValue": "10",
    "kpiUnit": "MRs",
    "isDeleted": "False",
    "defaultOrder": 23,
    "kpiSource": "BitBucket",
    "groupId": 1,
    "thresholdValue": "55",
    "kanban": false,
    "chartType": "grouped_column_plus_line",
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
    "upperThresholdBG" : "white",
    "lowerThresholdBG" : "red",
    "xAxisLabel": "Days",
    "yAxisLabel": "Count",
    "lineLegend": "Merge Requests",
    "barLegend": "Commits",
    "isPositiveTrend": true,
    "showTrend": true,
    "kpiFilter": "dropDown",
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "hideOverallFilter": true,
    "isRepoToolKpi": false,
    "kpiCategory": "Developer",
    "maturityRange": [
      "-2",
      "2-4",
      "4-8",
      "8-16",
      "16-"
    ]
  },
  {
    "kpiId": "kpi8",
    "kpiName": "Code Build Time",
    "maxValue": "100",
    "kpiUnit": "min",
    "isDeleted": "False",
    "defaultOrder": 24,
    "kpiSource": "Jenkins",
    "groupId": 1,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "line",
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
    "upperThresholdBG" : "red",
    "lowerThresholdBG" : "white",
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Count(Mins)",
    "isPositiveTrend": false,
    "showTrend": true,
    "kpiFilter": "dropDown",
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "hideOverallFilter": true,
    "maturityRange": [
      "-45",
      "45-30",
      "30-15",
      "15-5",
      "5-"
    ]
  },
  {
    "kpiId": "kpi118",
    "kpiName": "Deployment Frequency",
    "kpiCategory": "Dora",
    "maxValue": "100",
    "kpiUnit": "Number",
    "isDeleted": "False",
    "defaultOrder": 25,
    "kpiSource": "Jenkins",
    "groupId": 14,
    "thresholdValue": 0,
    "kanban": false,
    "chartType": "line",
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
    "upperThresholdBG" : "white",
    "lowerThresholdBG" : "red",
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Count",
    "isPositiveTrend": true,
    "showTrend": true,
    "kpiFilter": "multiSelectDropDown",
    "aggregationCriteria": "sum",
    "aggregationCircleCriteria" : "average",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "maturityRange": [
      "0-2" ,
      "2-4" ,
      "4-6" ,
      "6-8" ,
      "8-"
    ],
    "maturityLevel": [
        {
          "level": "M5",
          "bgColor": "#167a26",
		  "label": ">= 2 per week",
		  "displayRange": "0,1"
        },
        {
          "level": "M4",
          "bgColor": "#4ebb1a",
		  "label": "Once per week",
		  "displayRange": "2,3"
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
          "displayRange": "6,7"
        },
        {
          "level": "M1",
           "bgColor": "#c91212",
          "label": "< Once in 8 weeks",
          "displayRange": "8 and Above"
        }
     ]
  },
  {
    "kpiId": "kpi73",
    "kpiName": "Release Frequency",
    "maxValue": "300",
    "kpiUnit": "",
    "isDeleted": "False",
    "defaultOrder": 26,
    "kpiSource": "Jira",
    "groupId": 4,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "line",
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
    "upperThresholdBG" : "white",
    "lowerThresholdBG" : "red",
    "xAxisLabel": "Months",
    "yAxisLabel": "Count",
    "isPositiveTrend": true,
    "showTrend": true,
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi113",
    "kpiName": "Value delivered (Cost of Delay)",
    "maxValue": "300",
    "kpiUnit": "",
    "isDeleted": "False",
    "defaultOrder": 27,
    "kpiSource": "Jira",
    "groupId": 4,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "line",
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
    "upperThresholdBG" : "white",
    "lowerThresholdBG" : "red",
    "xAxisLabel": "Months",
    "yAxisLabel": "Count(Days)",
    "isPositiveTrend": true,
    "showTrend": true,
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi55",
    "kpiName": "Ticket Open vs Closed rate by type",
    "kpiUnit": "Tickets",
    "defaultOrder": 1,
    "kpiSource": "Jira",
    "groupId": 1,
    "thresholdValue": "",
    "kanban": true,
    "chartType": "grouped_column_plus_line",
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
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Count",
    "isPositiveTrend": false,
    "isTrendCalculative": true,
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
    "showTrend": true,
    "lineLegend": "Closed Tickets",
    "barLegend": "Open Tickets",
    "kpiFilter": "multiSelectDropDown",
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi54",
    "kpiName": "Ticket Open vs Closed rate by Priority",
    "kpiUnit": "Tickets",
    "isDeleted": "False",
    "defaultOrder": 2,
    "kpiSource": "Jira",
    "groupId": 1,
    "thresholdValue": "",
    "kanban": true,
    "chartType": "grouped_column_plus_line",
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
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Count",
    "isPositiveTrend": false,
    "isTrendCalculative": true,
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
    "showTrend": true,
    "lineLegend": "Closed Tickets",
    "barLegend": "Open Tickets",
    "kpiFilter": "multiSelectDropDown",
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi50",
    "kpiName": "Net Open Ticket Count by Priority",
    "isDeleted": "False",
    "defaultOrder": 3,
    "kpiSource": "Jira",
    "groupId": 1,
    "thresholdValue": "",
    "kanban": true,
    "chartType": "line",
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
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Count",
    "maxValue": "",
    "kpiUnit": "Number",
    "isPositiveTrend": false,
    "showTrend": true,
    "kpiFilter": "multiSelectDropDown",
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi51",
    "kpiName": "Net Open Ticket Count By RCA",
    "isDeleted": "False",
    "defaultOrder": 4,
    "kpiSource": "Jira",
    "groupId": 1,
    "thresholdValue": "",
    "kanban": true,
    "chartType": "line",
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
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Count",
    "maxValue": "",
    "kpiUnit": "Number",
    "isPositiveTrend": false,
    "showTrend": true,
    "kpiFilter": "multiSelectDropDown",
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi48",
    "kpiName": "Net Open Ticket By Status",
    "kpiUnit": "",
    "isDeleted": "False",
    "defaultOrder": 5,
    "kpiSource": "Jira",
    "groupId": 2,
    "thresholdValue": "",
    "kanban": true,
    "chartType": "line",
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
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Count",
    "isPositiveTrend": false,
    "isTrendCalculative": false,
    "showTrend": true,
    "kpiFilter": "multiSelectDropDown",
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi997",
    "kpiName": "Open Ticket Ageing By Priority",
    "kpiUnit": "Number",
    "isDeleted": "False",
    "defaultOrder": 6,
    "kpiSource": "Jira",
    "groupId": 2,
    "thresholdValue": "",
    "kanban": true,
    "chartType": "line",
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
    "xAxisLabel": "Months",
    "yAxisLabel": "Count",
    "isPositiveTrend": false,
    "kpiFilter": "multiSelectDropDown",
    "showTrend": false,
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi63",
    "kpiName": "Regression Automation Coverage",
    "isDeleted": "False",
    "defaultOrder": 7,
    "kpiSource": "Zypher",
    "groupId": 1,
    "maxValue": "100",
    "kpiUnit": "%",
    "kanban": true,
    "chartType": "line",
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
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Percentage",
    "isPositiveTrend": true,
    "showTrend": true,
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "maturityRange": [
      "-20",
      "20-40",
      "40-60",
      "60-80",
      "80-"
    ]
  },
  {
    "kpiId": "kpi62",
    "kpiName": "Unit Test Coverage",
    "maxValue": "100",
    "kpiUnit": "%",
    "isDeleted": "False",
    "defaultOrder": 8,
    "kpiSource": "Sonar",
    "groupId": 1,
    "thresholdValue": "55",
    "kanban": true,
    "chartType": "line",
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
    "upperThresholdBG" : "white",
    "lowerThresholdBG" : "red",
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Percentage",
    "isPositiveTrend": true,
    "kpiFilter": "dropDown",
    "aggregationCriteria": "average",
    "showTrend": true,
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "hideOverallFilter": true,
    "maturityRange": [
      "-20",
      "20-40",
      "40-60",
      "60-80",
      "80-"
    ]
  },
  {
    "kpiId": "kpi64",
    "kpiName": "Sonar Violations",
    "maxValue": "",
    "kpiUnit": "Number",
    "isDeleted": "False",
    "defaultOrder": 9,
    "kpiSource": "Sonar",
    "groupId": 1,
    "thresholdValue": "55",
    "kanban": true,
    "chartType": "line",
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
    "upperThresholdBG" : "red",
    "lowerThresholdBG" : "white",
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Count",
    "isPositiveTrend": false,
    "showTrend": true,
    "kpiFilter": "multiSelectDropDown",
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi67",
    "kpiName": "Sonar Tech Debt",
    "maxValue": "90",
    "kpiUnit": "Days",
    "isDeleted": "False",
    "defaultOrder": 10,
    "kpiSource": "Sonar",
    "groupId": 1,
    "thresholdValue": "55",
    "kanban": true,
    "chartType": "line",
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
    "upperThresholdBG" : "red",
    "lowerThresholdBG" : "white",
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Days",
    "isPositiveTrend": false,
    "showTrend": true,
    "kpiFilter": "dropDown",
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "hideOverallFilter": true,
    "maturityRange": [
      "-100",
      "100-50",
      "50-30",
      "30-10",
      "10-"
    ]
  },
  {
    "kpiId": "kpi71",
    "kpiName": "Test Execution and pass percentage",
    "isDeleted": "False",
    "defaultOrder": 11,
    "kpiSource": "Zypher",
    "groupId": 1,
    "maxValue": "100",
    "kpiUnit": "%",
    "kanban": true,
    "chartType": "grouped_column_plus_line",
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
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Percentage",
    "lineLegend": "Passed",
    "barLegend": "Executed",
    "isPositiveTrend": true,
    "showTrend": true,
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "maturityRange": [
      "-20",
      "20-40",
      "40-60",
      "60-80",
      "80-"
    ]
  },
  {
    "kpiId": "kpi49",
    "kpiName": "Ticket Velocity",
    "isDeleted": "False",
    "defaultOrder": 12,
    "kpiSource": "Jira",
    "groupId": 1,
    "thresholdValue": "",
    "maxValue": "300",
    "kpiUnit": "SP",
    "kanban": true,
    "chartType": "line",
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
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Story Points",
    "isPositiveTrend": true,
    "showTrend": false,
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi58",
    "kpiName": "Team Capacity",
    "maxValue": "",
    "kpiUnit": "Hours",
    "isDeleted": "False",
    "defaultOrder": 13,
    "kpiSource": "Jira",
    "groupId": 1,
    "thresholdValue": "",
    "kanban": true,
    "chartType": "line",
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
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Hours",
    "isPositiveTrend": true,
    "showTrend": false,
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi66",
    "kpiName": "Code Build Time",
    "maxValue": "100",
    "kpiUnit": "min",
    "isDeleted": "False",
    "defaultOrder": 14,
    "kpiSource": "Jenkins",
    "groupId": 1,
    "thresholdValue": "",
    "kanban": true,
    "chartType": "line",
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
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Min",
    "isPositiveTrend": false,
    "showTrend": true,
    "kpiFilter": "dropDown",
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "hideOverallFilter": true,
    "maturityRange": [
      "-45",
      "45-30",
      "30-15",
      "15-5",
      "5-"
    ]
  },
  {
    "kpiId": "kpi65",
    "kpiName": "Number of Check-ins",
    "maxValue": "10",
    "kpiUnit": "check-ins",
    "isDeleted": "False",
    "defaultOrder": 15,
    "groupId": 1,
    "kpiSource": "BitBucket",
    "thresholdValue": "55",
    "kanban": true,
    "chartType": "line",
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
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Count",
    "isPositiveTrend": true,
    "showTrend": true,
    "kpiFilter": "dropDown",
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "hideOverallFilter": true,
    "isRepoToolKpi": false,
    "kpiCategory": "Developer",
    "maturityRange": [
      "-2",
      "2-4",
      "4-8",
      "8-16",
      "16-"
    ]
  },
  {
    "kpiId": "kpi53",
    "kpiName": "Lead Time",
    "isDeleted": "False",
    "kpiInAggregatedFeed": "True",
    "kpiOnDashboard": [
      "Aggregated"
    ],
    "kpiBaseLine": "0",
    "thresholdValue": "",
    "defaultOrder": 16,
    "kpiUnit": "Days",
    "kpiSource": "Jira",
    "groupId": 3,
    "kanban": true,
    "chartType": "table",
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
    "xAxisLabel": "",
    "yAxisLabel": "",
    "isPositiveTrend": false,
    "showTrend": false,
    "kpiFilter": "radioButton",
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
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
    ]
  },
  {
    "kpiId": "kpi74",
    "kpiName": "Release Frequency",
    "maxValue": "300",
    "kpiUnit": "",
    "isDeleted": "False",
    "defaultOrder": 17,
    "kpiSource": "Jira",
    "groupId": 4,
    "thresholdValue": "",
    "kanban": true,
    "chartType": "line",
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
    "xAxisLabel": "Months",
    "yAxisLabel": "Count",
    "isPositiveTrend": true,
    "showTrend": true,
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi114",
    "kpiName": "Value delivered (Cost of Delay)",
    "maxValue": "300",
    "kpiUnit": "",
    "isDeleted": "False",
    "defaultOrder": 18,
    "kpiSource": "Jira",
    "groupId": 4,
    "thresholdValue": "",
    "kanban": true,
    "chartType": "line",
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
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Days",
    "isPositiveTrend": true,
    "showTrend": true,
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi121",
    "kpiName": "Capacity",
    "maxValue": "",
    "kpiUnit": "",
    "isDeleted": "False",
    "defaultOrder": 0,
    "kpiCategory": "Iteration",
    "kpiSubCategory": "Iteration Review",
    "kpiSource": "Jira",
    "groupId": 8,
    "thresholdValue": "",
    "kanban": false,
    "chartType": null,
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
    "xAxisLabel": "",
    "yAxisLabel": "",
    "isPositiveTrend": true,
    "showTrend": false,
    "isAdditionalFilterSupport": false,
    "boxType": "1_column",
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi119",
    "kpiName": "Work Remaining",
    "maxValue": "",
    "kpiUnit": "Hours",
    "isDeleted": "False",
    "defaultOrder": 4,
    "kpiCategory": "Iteration",
    "kpiSubCategory": "Iteration Review",
    "kpiSource": "Jira",
    "groupId": 8,
    "thresholdValue": "",
    "kanban": false,
    "chartType": null,
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
    "xAxisLabel": "",
    "yAxisLabel": "",
    "isPositiveTrend": true,
    "showTrend": false,
    "isAdditionalFilterSupport": false,
    "kpiFilter": "multiSelectDropDown",
    "boxType": "3_column",
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi128",
    "kpiName": "Planned Work Status",
    "maxValue": "",
    "kpiUnit": "Count",
    "isDeleted": "False",
    "defaultOrder": 2,
    "kpiCategory": "Iteration",
    "kpiSubCategory": "Iteration Review",
    "kpiSource": "Jira",
    "groupId": 8,
    "thresholdValue": "",
    "kanban": false,
    "chartType": null,
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
    "xAxisLabel": "",
    "yAxisLabel": "",
    "isPositiveTrend": true,
    "showTrend": false,
    "isSquadSupport": false,
    "kpiFilter": "multiSelectDropDown",
    "boxType": "3_column",
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi75",
    "kpiName": "Estimate vs Actual",
    "maxValue": "",
    "kpiUnit": "Hours",
    "isDeleted": "False",
    "defaultOrder": 22,
    "kpiCategory": "Iteration",
    "kpiSubCategory": "Iteration Review",
    "kpiSource": "Jira",
    "groupId": 8,
    "thresholdValue": "",
    "kanban": false,
    "chartType": null,
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
    "xAxisLabel": "",
    "yAxisLabel": "",
    "isPositiveTrend": true,
    "showTrend": false,
    "isAdditionalFilterSupport": false,
    "kpiFilter": "multiSelectDropDown",
    "boxType": "2_column",
    "calculateMaturity": true
  },
  {
    "kpiId": "kpi123",
    "kpiName": "Issues likely to Spill",
    "maxValue": "",
    "kpiUnit": "Count",
    "isDeleted": "False",
    "defaultOrder": 7,
    "kpiCategory": "Iteration",
    "kpiSubCategory": "Iteration Review",
    "kpiSource": "Jira",
    "groupId": 8,
    "thresholdValue": "",
    "kanban": false,
    "chartType": null,
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
    "xAxisLabel": "",
    "yAxisLabel": "",
    "isPositiveTrend": true,
    "showTrend": false,
    "isAdditionalFilterSupport": false,
    "kpiFilter": "multiSelectDropDown",
    "boxType": "3_column",
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi122",
    "kpiName": "Closure Possible Today",
    "maxValue": "",
    "kpiUnit": "Story Point",
    "isDeleted": "False",
    "defaultOrder": 6,
    "kpiCategory": "Iteration",
    "kpiSubCategory": "Iteration Review",
    "kpiSource": "Jira",
    "groupId": 8,
    "thresholdValue": "",
    "kanban": false,
    "chartType": null,
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
    "xAxisLabel": "",
    "yAxisLabel": "",
    "isPositiveTrend": true,
    "showTrend": false,
    "isAdditionalFilterSupport": false,
    "kpiFilter": "multiSelectDropDown",
    "boxType": "2_column",
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi120",
    "kpiName": "Iteration Commitment",
    "maxValue": "",
    "kpiUnit": "Count",
    "isDeleted": "False",
    "defaultOrder": 1,
    "kpiCategory": "Iteration",
    "kpiSubCategory": "Iteration Review",
    "kpiSource": "Jira",
    "groupId": 8,
    "thresholdValue": "",
    "kanban": false,
    "chartType": null,
    "kpiInfo": {
      "definition": "Iteration commitment shows in terms of issue count and story points the Initial commitment (issues tagged when the iteration starts), Scope added and Scope removed.",
      "details": [
        {
          "type": "paragraph",
          "value": "Overall commitment= Initial Commitment + Scope added - Scope removed"
        }
      ]
    },
    "xAxisLabel": "",
    "yAxisLabel": "",
    "isPositiveTrend": true,
    "showTrend": false,
    "isAdditionalFilterSupport": false,
    "kpiWidth": 100,
    "kpiFilter": "multiSelectDropDown",
    "boxType": "3_column",
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi124",
    "kpiName": "Estimation Hygiene",
    "maxValue": "",
    "kpiUnit": "Count",
    "isDeleted": "False",
    "defaultOrder": 21,
    "kpiCategory": "Iteration",
    "kpiSubCategory": "Iteration Review",
    "kpiSource": "Jira",
    "groupId": 8,
    "thresholdValue": "",
    "kanban": false,
    "chartType": null,
    "kpiInfo": {
      "definition": "It shows the count of issues which do not have estimates and count of In progress issues without any work logs."
    },
    "xAxisLabel": "",
    "yAxisLabel": "",
    "isPositiveTrend": true,
    "showTrend": false,
    "isAdditionalFilterSupport": false,
    "kpiFilter": "multiSelectDropDown",
    "boxType": "2_column_big",
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi132",
    "kpiName": "Defect Count by RCA",
    "maxValue": "",
    "kpiUnit": "Count",
    "isDeleted": "False",
    "defaultOrder": 14,
    "kpiCategory": "Iteration",
    "kpiSubCategory": "Iteration Review",
    "kpiSource": "Jira",
    "groupId": 8,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "pieChart",
    "kpiInfo": {
      "definition": "It shows the breakup of all defects within an iteration by root cause identified."
    },
    "xAxisLabel": "",
    "yAxisLabel": "",
    "isPositiveTrend": true,
    "showTrend": false,
    "isAdditionalFilterSupport": false,
    "kpiFilter": "radioButton",
    "boxType": "chart",
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi133",
    "kpiName": "Quality Status",
    "maxValue": "",
    "kpiUnit": "",
    "isDeleted": "False",
    "defaultOrder": 12,
    "kpiCategory": "Iteration",
    "kpiSubCategory": "Iteration Review",
    "kpiSource": "Jira",
    "groupId": 8,
    "thresholdValue": "",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": false,
    "kanban": false,
    "chartType": null,
    "kpiInfo": {
      "definition": "It showcases the count of defect linked to stories and count that are not linked to any story. The defect injection rate and defect density are shown to give a wholistic view of quality of ongoing iteration",
      "details": [
        {
          "type": "paragraph",
          "value": "*Any defect created during the iteration duration but is not added to the iteration is not considered"
        }
      ]
    },
    "xAxisLabel": "",
    "yAxisLabel": "",
    "isPositiveTrend": true,
    "kpiFilter": "",
    "showTrend": false,
    "boxType": "3_column"
  },
  {
    "kpiId": "kpi134",
    "kpiName": "Unplanned Work Status",
    "maxValue": "",
    "kpiUnit": "Count",
    "isDeleted": "False",
    "defaultOrder": 5,
    "kpiCategory": "Iteration",
    "kpiSubCategory": "Iteration Review",
    "kpiSource": "Jira",
    "groupId": 8,
    "thresholdValue": "",
    "kanban": false,
    "chartType": null,
    "kpiInfo": {
      "definition": "It shows count of the issues which do not have a due date. It also shows the completed count amongst the unplanned issues."
    },
    "xAxisLabel": "",
    "yAxisLabel": "",
    "isPositiveTrend": true,
    "showTrend": false,
    "isSquadSupport": false,
    "kpiFilter": "multiSelectDropDown",
    "boxType": "2_column",
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi125",
    "kpiName": "Iteration Burnup",
    "maxValue": "",
    "kpiUnit": "Count",
    "isDeleted": "False",
    "defaultOrder": 8,
    "kpiCategory": "Iteration",
    "kpiSubCategory": "Iteration Progress",
    "kpiSource": "Jira",
    "groupId": 8,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "CumulativeMultilineChart",
    "kpiInfo": {
      "definition": "Iteration Burnup KPI shows the cumulative actual progress against the overall scope of the iteration on a daily basis. For teams putting due dates at the beginning of iteration, the graph additionally shows the actual progress in comparison to the planning done and also predicts the probable progress for the remaining days of the iteration."
    },
    "xAxisLabel": "Days",
    "yAxisLabel": "Count",
    "isPositiveTrend": true,
    "showTrend": false,
    "isAdditionalFilterSupport": false,
    "kpiFilter": "multiselectdropdown",
    "boxType": "chart",
    "kpiWidth": 100,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi131",
    "kpiName": "Wastage",
    "maxValue": "",
    "kpiUnit": "Hours",
    "isDeleted": "False",
    "defaultOrder": 9,
    "kpiCategory": "Iteration",
    "kpiSubCategory": "Iteration Review",
    "kpiSource": "Jira",
    "groupId": 8,
    "thresholdValue": "",
    "kanban": false,
    "chartType": null,
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
    "xAxisLabel": "",
    "yAxisLabel": "",
    "isPositiveTrend": true,
    "showTrend": false,
    "isAdditionalFilterSupport": false,
    "kpiFilter": "multiSelectDropDown",
    "boxType": "3_column",
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi135",
    "kpiName": "First Time Pass Rate",
    "maxValue": "",
    "kpiUnit": "Hours",
    "isDeleted": "False",
    "defaultOrder": 11,
    "kpiCategory": "Iteration",
    "kpiSubCategory": "Iteration Review",
    "kpiSource": "Jira",
    "groupId": 8,
    "thresholdValue": "",
    "kanban": false,
    "chartType": null,
    "kpiInfo": {
      "definition": "Percentage of tickets that passed QA with no return transition or any tagging to a specific configured status and no linkage of a defect."
    },
    "xAxisLabel": "",
    "yAxisLabel": "",
    "isPositiveTrend": true,
    "showTrend": false,
    "isAdditionalFilterSupport": false,
    "kpiFilter": "multiSelectDropDown",
    "boxType": "3_column",
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi129",
    "kpiName": "Issues Without Story Link",
    "maxValue": "",
    "kpiUnit": "Hours",
    "isDeleted": "False",
    "defaultOrder": 3,
    "kpiCategory": "Backlog",
    "kpiSource": "Jira",
    "groupId": 11,
    "thresholdValue": "",
    "kanban": false,
    "chartType": null,
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
    "xAxisLabel": "",
    "yAxisLabel": "",
    "isPositiveTrend": true,
    "showTrend": false,
    "isSquadSupport": false,
    "isAdditionalFilterSupport": false,
    "kpiFilter": "",
    "boxType": "3_column",
    "kpiSubCategory": "Backlog Health",
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi127",
    "kpiName": "Production Defects Ageing",
    "kpiUnit": "Number",
    "isDeleted": "False",
    "defaultOrder": 2,
    "kpiCategory": "Backlog",
    "kpiSource": "Jira",
    "groupId": 10,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "line",
    "kpiInfo": {
      "definition": "It groups all the open production defects based on their ageing in the backlog."
    },
    "xAxisLabel": "Months",
    "yAxisLabel": "Count",
    "isPositiveTrend": false,
    "kpiFilter": "multiSelectDropDown",
    "showTrend": false,
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": false,
    "kpiSubCategory": "Backlog Health",
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi139",
    "kpiName": "Refinement Rejection Rate",
    "kpiUnit": "%",
    "isDeleted": "False",
    "defaultOrder": 6,
    "kpiCategory": "Backlog",
    "kpiSource": "Jira",
    "groupId": 10,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "line",
    "kpiInfo": {
      "definition": "It measures the percentage of stories rejected during refinement as compared to the overall stories discussed in a week."
    },
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Count",
    "isPositiveTrend": false,
    "kpiFilter": "",
    "showTrend": false,
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": false,
    "kpiSubCategory": "Backlog Health",
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi136",
    "kpiName": "Defect Count by Status",
    "maxValue": "",
    "kpiUnit": "Count",
    "isDeleted": "False",
    "defaultOrder": 13,
    "kpiCategory": "Iteration",
    "kpiSubCategory": "Iteration Review",
    "kpiSource": "Jira",
    "groupId": 8,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "pieChart",
    "kpiInfo": {
      "definition": "It shows the breakup of all defects within an iteration by status. User can view the total defects in the iteration as well as the defects created after iteration start."
    },
    "xAxisLabel": "",
    "yAxisLabel": "",
    "isPositiveTrend": true,
    "showTrend": false,
    "isAdditionalFilterSupport": false,
    "kpiFilter": "radioButton",
    "boxType": "chart",
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi137",
    "kpiName": "Defect Reopen Rate",
    "kpiUnit": "Hours",
    "isDeleted": "False",
    "defaultOrder": 5,
    "kpiCategory": "Backlog",
    "kpiSource": "Jira",
    "groupId": 10,
    "thresholdValue": "",
    "kanban": false,
    "kpiInfo": {
      "definition": "It shows number of defects reopened in a given span of time in comparison to the total closed defects. For all the reopened defects, the average time to reopen is also available."
    },
    "isPositiveTrend": false,
    "kpiFilter": "dropdown",
    "showTrend": false,
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": true,
    "hideOverallFilter": true,
    "kpiSubCategory": "Backlog Health",
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi141",
    "kpiName": "Defect Count by Status",
    "maxValue": "",
    "kpiUnit": "Count",
    "isDeleted": "False",
    "defaultOrder": 1,
    "kpiCategory": "Release",
    "kpiSubCategory": "Quality",
    "kpiSource": "Jira",
    "groupId": 9,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "pieChart",
    "kpiInfo": {
      "definition": "It shows the breakup of all defects tagged to a release based on Status. The breakup is shown in terms of count & percentage."
    },
    "xAxisLabel": "",
    "yAxisLabel": "",
    "isPositiveTrend": true,
    "showTrend": false,
    "isAdditionalFilterSupport": false,
    "kpiFilter": "",
    "boxType": "chart",
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi142",
    "kpiName": "Defect Count by RCA",
    "maxValue": "",
    "kpiUnit": "Count",
    "isDeleted": "False",
    "defaultOrder": 2,
    "kpiCategory": "Release",
    "kpiSubCategory": "Quality",
    "kpiSource": "Jira",
    "groupId": 9,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "pieChart",
    "kpiInfo": {
      "definition": "It shows the breakup of all defects tagged to a release based on RCA. The breakup is shown in terms of count & percentage."
    },
    "xAxisLabel": "",
    "yAxisLabel": "",
    "isPositiveTrend": true,
    "showTrend": false,
    "isAdditionalFilterSupport": false,
    "kpiFilter" : "radioButton",
    "boxType": "chart",
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi143",
    "kpiName": "Defect Count by Assignee",
    "maxValue": "",
    "kpiUnit": "Count",
    "isDeleted": "False",
    "defaultOrder": 3,
    "kpiCategory": "Release",
    "kpiSubCategory": "Quality",
    "kpiSource": "Jira",
    "groupId": 9,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "pieChart",
    "kpiInfo": {
      "definition": "It shows the breakup of all defects tagged to a release based on Assignee. The breakup is shown in terms of count & percentage."
    },
    "xAxisLabel": "",
    "yAxisLabel": "",
    "isPositiveTrend": true,
    "showTrend": false,
    "isAdditionalFilterSupport": false,
    "kpiFilter" : "radioButton",
    "boxType": "chart",
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi144",
    "kpiName": "Defect Count by Priority",
    "maxValue": "",
    "kpiUnit": "Count",
    "isDeleted": "False",
    "defaultOrder": 4,
    "kpiCategory": "Release",
    "kpiSubCategory": "Quality",
    "kpiSource": "Jira",
    "groupId": 9,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "pieChart",
    "kpiInfo": {
      "definition": "It shows the breakup of all defects tagged to a release based on Priority. The breakup is shown in terms of count & percentage."
    },
    "xAxisLabel": "",
    "yAxisLabel": "",
    "isPositiveTrend": true,
    "showTrend": false,
    "isAdditionalFilterSupport": false,
    "kpiFilter" : "radioButton",
    "boxType": "chart",
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi989",
    "kpiName": "Kpi Maturity",
    "isDeleted": "False",
    "defaultOrder": 1,
    "kpiCategory": "Kpi Maturity",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": false,
    "kanban": false
  },
  {
    "kpiId": "kpi140",
    "kpiName": "Defect Count by Priority",
    "maxValue": "",
    "kpiUnit": "Count",
    "isDeleted": "False",
    "defaultOrder": 15,
    "kpiCategory": "Iteration",
    "kpiSubCategory": "Iteration Review",
    "kpiSource": "Jira",
    "groupId": 8,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "pieChart",
    "kpiInfo": {
      "definition": "It shows the breakup of all defects within an iteration by priority. User can view the total defects in the iteration as well as the defects created after iteration start."
    },
    "xAxisLabel": "",
    "yAxisLabel": "",
    "isPositiveTrend": true,
    "showTrend": false,
    "isAdditionalFilterSupport": false,
    "kpiFilter": "radioButton",
    "boxType": "chart",
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi147",
    "kpiName": "Release Progress",
    "maxValue": "",
    "kpiUnit": "Count",
    "isDeleted": "False",
    "defaultOrder": 5,
    "kpiCategory": "Release",
    "kpiSubCategory": "Speed",
    "kpiSource": "Jira",
    "groupId": 9,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "horizontalPercentBarChart",
    "kpiInfo": {
      "definition": "It shows the breakup by status of issues tagged to a release. The breakup is based on both issue count and story points"
    },
    "xAxisLabel": "",
    "yAxisLabel": "",
    "kpiWidth": 100,
    "isPositiveTrend": true,
    "showTrend": false,
    "isAdditionalFilterSupport": false,
    "kpiFilter": "dropDown",
    "boxType": "chart",
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi145",
    "kpiName": "Dev Completion Status",
    "maxValue": "",
    "kpiUnit": "Count",
    "isDeleted": "False",
    "defaultOrder": 3,
    "kpiCategory": "Iteration",
    "kpiSubCategory": "Iteration Review",
    "kpiSource": "Jira",
    "groupId": 8,
    "thresholdValue": "",
    "kanban": false,
    "chartType": null,
    "kpiInfo": {
      "definition": "It gives a comparative view between the planned completion and actual completion from a development point of view. In addition, user can see the delay (in days) in dev completed issues"
    },
    "xAxisLabel": "",
    "yAxisLabel": "",
    "isPositiveTrend": true,
    "showTrend": false,
    "isSquadSupport": false,
    "kpiFilter": "multiSelectDropDown",
    "boxType": "3_column",
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi138",
    "kpiName": "Backlog Readiness",
    "maxValue": "",
    "kpiUnit": "Count",
    "isDeleted": "False",
    "defaultOrder": 1,
    "kpiCategory": "Backlog",
    "kpiSource": "Jira",
    "groupId": 11,
    "thresholdValue": "",
    "kanban": false,
    "chartType": null,
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
    "xAxisLabel": "",
    "yAxisLabel": "",
    "isPositiveTrend": true,
    "showTrend": false,
    "isAdditionalFilterSupport": false,
    "kpiFilter": "multiSelectDropDown",
    "boxType": "3_column",
    "kpiSubCategory": "Backlog Health",
    "calculateMaturity": false
  },
  {
      "kpiId": "kpi3",
      "kpiName": "Lead Time",
      "isDeleted": "False",
      "kpiCategory": "Backlog",
      "boxType": null,
      "kpiBaseLine": "0",
      "thresholdValue": "20",
      "defaultOrder": "1",
      "kpiUnit": "Days",
      "kpiSource": "Jira",
      "groupId": 11,
      "kanban": false,
      "aggregationCriteria": "sum",
      "chartType": "line",
      "kpiInfo": {
        "definition": "Lead Time is the time from the moment when the request was made by a client and placed on a board to when all work on this item is completed and the request was delivered to the client",
        "formula": null,
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
      "xAxisLabel": "Range",
      "yAxisLabel": "Days",
      "isPositiveTrend": false,
      "showTrend": true,
      "kpiFilter": "dropdown",
      "isAdditionalFilterSupport": false,
      "calculateMaturity": false,
      "kpiSubCategory": "Flow KPIs",
      "lowerThresholdBG": "white",
      "upperThresholdBG": "red",
      "maturityRange": ["-60", "60-45", "45-30", "30-10", "10-"]
    },
  {
    "kpiId": "kpi148",
    "kpiName": "Flow Load",
    "kpiUnit": "",
    "isDeleted": "False",
    "defaultOrder": 7,
    "kpiCategory": "Backlog",
    "kpiSource": "Jira",
    "groupId": 11,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "stacked-area",
    "kpiInfo": {
      "definition": " Flow load indicates how many items are currently in the backlog. This KPI emphasizes on limiting work in progress to enabling a fast flow of issues"
    },
    "xAxisLabel": "Time",
    "yAxisLabel": "Count",
    "isPositiveTrend": false,
    "kpiFilter": "",
    "showTrend": false,
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": false,
    "kpiSubCategory": "Flow KPIs",
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi146",
    "kpiName": "Flow Distribution",
    "kpiUnit": "",
    "isDeleted": "False",
    "defaultOrder": 6,
    "kpiCategory": "Backlog",
    "kpiSource": "Jira",
    "groupId": 10,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "stacked-area",
    "kpiInfo": {
      "definition": "Flow Distribution evaluates the amount of each kind of work (issue types) which are open in the backlog over a period of time."
    },
    "xAxisLabel": "Time",
    "yAxisLabel": "Count",
    "isPositiveTrend": false,
    "kpiFilter": "",
    "showTrend": false,
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": false,
    "kpiSubCategory": "Flow KPIs",
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi149",
    "kpiName": "Happiness Index",
    "kpiSource": "Jira",
    "aggregationCriteria": "average",
    "boxType": "3_column",
    "calculateMaturity": false,
    "chartType": "line",
    "defaultOrder": 28,
    "groupId": 3,
    "isAdditionalFilterSupport": false,
    "isDeleted": "False",
    "isPositiveTrend": true,
    "kanban": false,
    "kpiFilter": "multiSelectDropDown",
    "kpiInfo": {
      "details": [
        {
          "type": "paragraph",
          "value": "KPI for tracking moral of team members"
        }
      ]
    },
    "kpiUnit": "",
    "maxValue": "5",
    "showTrend": false,
    "thresholdValue": "",
    "xAxisLabel": "Sprints",
    "yAxisLabel": "Rating"
  },
  {
    "kpiId": "kpi150",
    "kpiName": "Release Burnup",
    "maxValue": "",
    "kpiUnit": "Count",
    "isDeleted": "False",
    "defaultOrder": 1,
    "kpiCategory": "Release",
    "kpiSubCategory": "Speed",
    "kpiSource": "Jira",
    "groupId": 9,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "CumulativeMultilineChart",
    "kpiInfo": {
      "definition": "It shows the cumulative daily actual progress of the release against the overall scope. It also shows additionally the scope added or removed during the release w.r.t Dev/Qa completion date and Dev/Qa completion status for the Release tagged issues",
      "details" : [
        {
          "type" : "link",
          "kpiLinkDetail" : {
            "text" : "Detailed Information at",
            "link" : "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/41582601/RELEASE+Health#Release-Burnup"
          }
        }
      ]
    }
    "xAxisLabel": "",
    "yAxisLabel": "Count",
    "kpiWidth": 100,
    "isPositiveTrend": true,
    "showTrend": false,
    "isAdditionalFilterSupport": false,
    "kpiFilter": "radioButton",
    "boxType": "chart",
    "calculateMaturity": false
  },
  {
      "kpiId": "kpi151",
      "kpiName": "Backlog Count By Status",
      "kpiUnit": "Count",
      "isDeleted": "False",
      "defaultOrder": 9,
      "kpiCategory": "Backlog",
      "kpiSource": "Jira",
      "groupId": 10,
      "thresholdValue": "",
      "kanban": false,
      "chartType": "pieChart",
      "kpiInfo": {
          "definition": "Total count of issues in the Backlog with a breakup by Status."
      },
      "xAxisLabel": "",
      "yAxisLabel": "",
      "isPositiveTrend": true,
      "showTrend": false,
      "isAdditionalFilterSupport": false,
      "boxType": "chart",
      "kpiSubCategory": "Backlog Overview",
      "calculateMaturity": false
  },
  {
      "kpiId": "kpi152",
      "kpiName": "Backlog Count By Issue Type",
      "kpiUnit": "Count",
      "isDeleted": "False",
      "defaultOrder": 10,
      "kpiCategory": "Backlog",
      "kpiSource": "Jira",
      "groupId": 11,
      "thresholdValue": "",
      "kanban": false,
      "chartType": "pieChart",
      "kpiInfo": {
          "definition": "Total count of issues in the backlog with a breakup by issue type."
      },
      "xAxisLabel": "",
      "yAxisLabel": "",
      "isPositiveTrend": true,
      "showTrend": false,
      "isAdditionalFilterSupport": false,
      "boxType": "chart",
      "kpiSubCategory": "Backlog Overview",
      "calculateMaturity": false
  },
  {
      "kpiId": "kpi153",
      "kpiName": "PI Predictability",
      "maxValue": "200",
      "kpiUnit": "",
      "isDeleted": "False",
      "defaultOrder": 29,
      "kpiSource": "Jira",
      "groupId": 4,
      "thresholdValue": "",
      "kanban": false,
      "chartType": "multipleline",
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
      "xAxisLabel": "PIs",
      "yAxisLabel": "Business Value",
      "isPositiveTrend": true,
      "showTrend": true,
      "aggregationCriteria": "sum",
      "isAdditionalFilterSupport": false,
      "calculateMaturity": false
  },
  {
    "kpiId": "kpi154",
    "kpiName": "Daily Standup View",
    "maxValue": "",
    "isDeleted": "False",
    "defaultOrder": 8,
    "kpiCategory": "Iteration",
    "kpiSubCategory": "Daily Standup",
    "kpiSource": "Jira",
    "groupId": 13,
    "thresholdValue": "",
    "kanban": false,
    "isPositiveTrend": true,
    "showTrend": false,
    "isAdditionalFilterSupport": false,
    "kpiFilter": "multiselectdropdown",
    "kpiWidth": 100,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi155",
    "kpiName": "Defect Count By Type",
    "kpiUnit": "Count",
    "isDeleted": "False",
    "defaultOrder": 11,
    "kpiCategory": "Backlog",
    "kpiSource": "Jira",
    "groupId": 11,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "pieChart",
    "kpiInfo": {
      "definition": "Total count of issues in the backlog with a breakup by defect type."
    },
    "xAxisLabel": "",
    "yAxisLabel": "",
    "isPositiveTrend": true,
    "showTrend": false,
    "isAdditionalFilterSupport": false,
    "boxType": "chart",
    "kpiSubCategory": "Backlog Overview",
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi156",
    "kpiName": "Lead Time For Change",
    "maxValue": "100",
    "kpiUnit": "Days",
    "isDeleted": "False",
    "defaultOrder": 3,
    "kpiSource": "Jira",
    "kpiCategory": "Dora",
    "groupId": 15,
    "thresholdValue": 0,
    "kanban": false,
    "chartType": "line",
    "kpiInfo": {
      "definition": "LEAD TIME FOR CHANGE measures the velocity of software delivery.",
      "details": [
        {
          "type": "paragraph",
          "value": "LEAD TIME FOR CHANGE Captures the time between a code change to commit and deployed to production."
        }
      ]
    },
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Days",
    "isPositiveTrend": true,
    "showTrend": true,
    "kpiFilter": "",
    "aggregationCriteria": "sum",
    "aggregationCircleCriteria" : "average",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "maturityRange": [
      "90-",
      "30-90",
      "7-30",
      "1-7",
      "-1"
    ]
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
      ]
  },
  {
    "kpiId": "kpi157",
    "kpiName": "Check-Ins & Merge Requests",
    "maxValue": 10,
    "kpiUnit": "MRs",
    "isDeleted": "False",
    "defaultOrder": 1,
    "kpiSource": "BitBucket",
    "groupId": 1,
    "thresholdValue": 55,
    "kanban": false,
    "chartType": "grouped_column_plus_line",
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
    "xAxisLabel": "Days",
    "yAxisLabel": "Count",
    "lineLegend": "Merge Requests",
    "barLegend": "Commits",
    "isPositiveTrend": true,
    "showTrend": true,
    "kpiFilter": "dropDown",
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "hideOverallFilter": true,
    "maturityRange": [
      -2,
      "2-4",
      "4-8",
      "8-16",
      "16-"
    ],
    "isRepoToolKpi": true,
    "kpiCategory": "Developer"
  },
    {
      "kpiId": "kpi158",
      "kpiName": "Mean Time To Merge",
      "maxValue": "10",
      "kpiUnit": "Hours",
      "isDeleted": "False",
      "defaultOrder": 2,
      "groupId": 1,
      "kpiSource": "BitBucket",
      "thresholdValue": "55",
      "kanban": false,
      "chartType": "line",
      "kpiInfo": {
        "definition":
          "MEAN TIME TO MERGE measures the efficiency of the code review process in a team",
        "details": [
          {
            "type": "paragraph",
            "value":
              "It is calculated in ‘Hours’. Fewer the Hours better is the ‘Speed’",
          },
          {
            "type": "paragraph",
            "value":
              "A progress indicator shows trend of Mean time to merge in last 2 weeks. A downward trend is considered positive",
          },
          {
            "type": "paragraph",
            "value":
              "Maturity of the KPI is calculated based on the average of the last 5 weeks",
          },
        ],
        "maturityLevels": [
          {
            "level": "M5",
            "bgColor": "#6cab61",
            "range": "<4 Hours",
          },
          {
            "level": "M4",
            "bgColor": "#AEDB76",
            "range": "4-8 Hours",
          },
          {
            "level": "M3",
            "bgColor": "#eff173",
            "range": "8-16 Hours",
          },
          {
            "level": "M2",
            "bgColor": "#ffc35b",
            "range": "16-48 Hours",
          },
          {
            "level": "M1",
            "bgColor": "#F06667",
            "range": ">48 Hours",
          },
        ],
      },
      "xAxisLabel": "Weeks",
      "yAxisLabel": "Count(Hours)",
      "isPositiveTrend": false,
      "showTrend": true,
      "kpiFilter": "dropDown",
      "aggregationCriteria": "average",
      "isAdditionalFilterSupport": false,
      "calculateMaturity": true,
      "hideOverallFilter": true,
      "maturityRange": ["-16", "16-8", "8-4", "4-2", "2-"],
      "isRepoToolKpi": true,
      "kpiCategory": "Developer",
    },
    {
        "kpiId": "kpi159",
        "kpiName": "Number of Check-ins",
        "maxValue": 10,
        "kpiUnit": "check-ins",
        "isDeleted": false,
        "defaultOrder": 1,
        "groupId": 1,
        "kpiSource": "BitBucket",
        "thresholdValue": 55,
        "kanban": true,
        "chartType": "line",
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
        "xAxisLabel": "Weeks",
        "yAxisLabel": "Count",
        "isPositiveTrend": true,
        "showTrend": true,
        "kpiFilter": "dropDown",
        "aggregationCriteria": "sum",
        "isAdditionalFilterSupport": false,
        "calculateMaturity": true,
        "hideOverallFilter": true,
        "maturityRange": ["-2", "2-4", "4-8", "8-16", "16-"],
        "isRepoToolKpi": true,
        "kpiCategory": "Developer"
      },
      {
        "kpiId": "kpi160",
        "kpiName": "Pickup Time",
        "maxValue": 10,
        "kpiUnit": "Hours",
        "isDeleted": false,
        "defaultOrder": 3,
        "groupId": 1,
        "kpiSource": "BitBucket",
        "thresholdValue": 20,
        "kanban": false,
        "chartType": "line",
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
        "xAxisLabel": "Weeks",
        "yAxisLabel": "Count (Hours)",
        "isPositiveTrend": false,
        "showTrend": true,
        "kpiFilter": "dropDown",
        "aggregationCriteria": "average",
        "isAdditionalFilterSupport": false,
        "calculateMaturity": true,
        "hideOverallFilter": true,
        "maturityRange": ["-16", "16-8", "8-4", "4-2", "2-"],
        "isRepoToolKpi": true,
        "kpiCategory": "Developer"
      },
      {
        "kpiId": "kpi162",
        "kpiName": "PR Size",
        "maxValue": 10,
        "kpiUnit": "Lines",
        "isDeleted": false,
        "defaultOrder": 4,
        "groupId": 1,
        "kpiSource": "BitBucket",
        "kanban": false,
        "chartType": "line",
        "kpiInfo": {
          "definition": "Pull request size measures the number of code lines modified in a pull request. Smaller pull requests are easier to review, safer to merge, and correlate to a lower cycle time."
        },
        "xAxisLabel": "Weeks",
        "yAxisLabel": "Count (No. of Lines)",
        "isPositiveTrend": false,
        "showTrend": true,
        "kpiFilter": "dropDown",
        "aggregationCriteria": "average",
        "isAdditionalFilterSupport": false,
        "calculateMaturity": true,
        "hideOverallFilter": true,
        "maturityRange": ["-16", "16-8", "8-4", "4-2", "2-"],
        "isRepoToolKpi": true,
        "kpiCategory": "Developer"
  },
  {
	"kpiId": "kpi164",
	"kpiName": "Scope Churn",
	"maxValue": "200",
	"kpiUnit": "%",
	"isDeleted": "False",
	"defaultOrder": Double("30"),
	"kpiSource": "Jira",
	"groupId": Double("4"),
	"thresholdValue": "20",
	"kanban": false,
	"chartType": "line",
	"kpiInfo": {
		"definition": "Scope churn explain the change in the scope of sprint since the start of iteration",
		"formula": [{
			"lhs": "Scope Churn",
			"operator": "division",
			"operands": ["Count of Stories added + Count of Stories removed", " Count of Stories in Initial Commitment at the time of Sprint start"]
		}],
		"details": [{
			"type": "link",
			"kpiLinkDetail": {
				"text": "Detailed Information at",
				"link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/26935328/Scrum+SPEED+KPIs#Scope-Churn"
			}
		}]
	},
	"upperThresholdBG" : "red",
    "lowerThresholdBG" : "white",
	"xAxisLabel": "Sprints",
	"yAxisLabel": "Percentage",
	"isPositiveTrend": false,
	"showTrend": true,
	"aggregationCriteria": "average",
	"isAdditionalFilterSupport": true,
	"calculateMaturity": true,
    "maturityRange": ["-50", "50-30", "30-20", "20-10", "10-"]
 },
 {
     "kpiId":"kpi163",
     "kpiName":"Defect by Testing Phase",
     "maxValue":"",
     "kpiUnit":"Count",
     "isDeleted":"False",
     "defaultOrder":7,
     "kpiCategory":"Release",
     "kpiSource":"Jira",
     "groupId":9,
     "thresholdValue":"",
     "kanban":false,
     "chartType":"horizontalPercentBarChart",
     "kpiInfo":{
       "definition":" It gives a breakup of escaped defects by testing phase"
     },
     "xAxisLabel":"",
     "yAxisLabel":"",
     "isPositiveTrend":true,
     "showTrend":false,
     "isAdditionalFilterSupport":false,
     "kpiFilter":"radioButton",
     "boxType":"chart",
     "calculateMaturity":false,
     "kpiSubCategory": "Quality",
  	 "maturityRange": ["-40", "40-60", "60-75", "75-90", "90-"]
 },
 {
    "kpiId": "kpi165",
    "kpiName": "Epic Progress",
    "maxValue": "",
    "kpiUnit": "Count",
    "isDeleted": "False",
    "defaultOrder": 5,
    "kpiCategory": "Release",
    "kpiSubCategory": "Value",
    "kpiSource": "Jira",
    "groupId": 9,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "horizontalPercentBarChart",
    "kpiInfo": {
      "definition": "It depicts the progress of each epic in a release in terms of total count and %age completion."
    },
    "xAxisLabel": "",
    "yAxisLabel": "",
    "kpiWidth": 100,
    "isPositiveTrend": true,
    "showTrend": false,
    "isAdditionalFilterSupport": false,
    "kpiFilter": "multiSelectDropDown",
    "boxType": "chart",
    "calculateMaturity": false
  },
{
      "kpiId": "kpi169",
      "kpiName": "Epic Progress",
      "maxValue": "",
      "kpiUnit": "Count",
      "isDeleted": "False",
      "defaultOrder": 5,
      "kpiCategory": "Backlog",
      "kpiSource": "Jira",
      "groupId": 9,
      "thresholdValue": "",
      "kanban": false,
      "chartType": "horizontalPercentBarChart",
      "kpiInfo": {
        "definition": "It depicts the progress of each epic in terms of total count and %age completion."
      },
      "xAxisLabel": "",
      "yAxisLabel": "",
      "kpiWidth": 100,
      "isPositiveTrend": true,
      "showTrend": false,
      "isAdditionalFilterSupport": false,
      "kpiFilter": "radioButton",
      "boxType": "chart",
      "calculateMaturity": false,
      "kpiSubCategory": "Epic View"
    },
    {
        "kpiId": "kpi161",
        "kpiName": "Iteration Readiness",
        "maxValue": "",
        "kpiUnit": "Count",
        "isDeleted": "False",
        "defaultOrder": 4,
        "kpiCategory": "Backlog",
        "kpiSource": "Jira",
        "groupId": 11,
        "thresholdValue": "",
        "kanban": false,
        "chartType": "stackedColumn",
        "kpiInfo": {
          "definition": "Iteration readiness depicts the state of future iterations w.r.t the quality of refined Backlog"
        },
        "xAxisLabel": "Sprint",
        "yAxisLabel": "Count",
        "isPositiveTrend": true,
        "showTrend": false,
        "kpiSubCategory": "Backlog Health",
        "isAdditionalFilterSupport": false,
        "kpiFilter": "",
        "boxType": "chart",
        "calculateMaturity": false,
        "maturityRange": ["-40", "40-60", "60-75", "75-90", "90-"]
 },
 {
    "kpiId": "kpi168",
    "kpiName": "Sonar Code Quality",
    "kpiUnit": "unit",
    "maxValue": "90",
    "isDeleted": "False",
    "defaultOrder": 14,
    "kpiSource": "Sonar",
    "groupId": 1,
    "kanban": false,
    "chartType": "bar-with-y-axis-group",
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
    "xAxisLabel": "Months",
    "yAxisLabel": "Code Quality",
    "isPositiveTrend": true,
    "showTrend": true,
    "kpiFilter": "dropDown",
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "hideOverallFilter": true,
    "maturityRange": ["5", "4", "3", "2", "1"],
    "yaxisOrder" : {
            5 : 'E',
            4 : 'D',
            3 : 'C',
            2 : 'B',
            1 : 'A'
        }
  },
  {
    "kpiId": "kpi170",
    "kpiName": "Flow Efficiency",
    "kpiUnit": "%",
    "isDeleted": "False",
    "defaultOrder": 1,
    "kpiCategory": "Backlog",
    "kpiSource": "Jira",
    "groupId": 11,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "line",
    "kpiInfo": {
        "definition": "The percentage of time spent in work states vs wait states across the lifecycle of an issue"
    },
    "xAxisLabel": "Duration",
    "yAxisLabel": "Percentage",
    "isPositiveTrend": false,
    "kpiFilter": "dropDown",
    "showTrend": false,
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": false,
    "kpiSubCategory": "Flow KPIs"
  },
  {
      "kpiId": "kpi166",
      "kpiName": "Mean Time to Recover",
      "maxValue": "100",
      "kpiUnit": "Hours",
      "isDeleted": "False",
      "defaultOrder": 4,
      "kpiSource": "Jira",
      "kpiCategory": "Dora",
      "groupId": 15,
      "thresholdValue": 0,
      "kanban": false,
      "chartType": "line",
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
                   "type" : "link",
                    "kpiLinkDetail" : {
                    "text" : "Detailed Information at",
                    "link" : "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/59080705/DORA+KPIs#Mean-time-to-Recover-(MTTR)"
                    }
               }
          ]
      },
      "xAxisLabel": "Weeks",
      "yAxisLabel": "Hours",
      "isPositiveTrend": false,
      "showTrend": true,
      "kpiFilter": "",
      "aggregationCriteria": "sum",
      "aggregationCircleCriteria": "average",
      "isAdditionalFilterSupport": false,
      "calculateMaturity": true,
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
      ]
    },
  {
      "kpiId": "kpi171",
      "kpiName": "Cycle Time",
      "maxValue": "",
      "kpiUnit": "Count",
      "isDeleted": "False",
      "defaultOrder": 4,
      "kpiCategory": "Backlog",
      "kpiSource": "Jira",
      "groupId": 11,
      "thresholdValue": "",
      "kanban": false,
      "chartType": "stackedColumn",
      "isAggregationStacks" : false ,
      "xAxisLabel": "",
      "yAxisLabel": "Days",
      "isAdditionalFilterSupport": false,
      "kpiFilter": "dropDown",
      "boxType": "chart",
      "calculateMaturity": false,
      "kpiInfo" : {
      "definition": "Cycle time helps ascertain time spent on each step of the complete issue lifecycle. It is being depicted in the visualization as 3 core cycles - Intake to DOR, DOR to DOD, DOD to Live.",
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
      "kpiSubCategory": "Flow KPIs"
    }
]
);

//mapping of KPI into categories, change the category if you want to swap the tabs of KPI
db.getCollection('kpi_category_mapping').insertMany(
[{
		"kpiId": "kpi40",
		"categoryId": "categoryOne",
		"kpiOrder": 1,
		"kanban": false
	},
	{
		"kpiId": "kpi39",
		"categoryId": "categoryOne",
		"kpiOrder": 2,
		"kanban": false
	},
	{
		"kpiId": "kpi72",
		"categoryId": "categoryOne",
		"kpiOrder": 3,
		"kanban": false
	},
    {
    	"kpiId": "kpi5",
    	"categoryId": "categoryOne",
    	"kpiOrder": 4,
    	"kanban": false
    },
	{
		"kpiId": "kpi46",
		"categoryId": "categoryOne",
		"kpiOrder": 5,
		"kanban": false
	},
	{
		"kpiId": "kpi8",
		"categoryId": "categoryOne",
		"kpiOrder": 8,
		"kanban": false
	},
	{
		"kpiId": "kpi14",
		"categoryId": "categoryTwo",
		"kpiOrder": 1,
		"kanban": false
	},
	{
		"kpiId": "kpi82",
		"categoryId": "categoryTwo",
		"kpiOrder": 2,
		"kanban": false
	},
	{
		"kpiId": "kpi111",
		"categoryId": "categoryTwo",
		"kpiOrder": 3,
		"kanban": false
	},
	{
		"kpiId": "kpi35",
		"categoryId": "categoryTwo",
		"kpiOrder": 4,
		"kanban": false
	},
	{
		"kpiId": "kpi34",
		"categoryId": "categoryTwo",
		"kpiOrder": 5,
		"kanban": false
	},
	{
		"kpiId": "kpi37",
		"categoryId": "categoryTwo",
		"kpiOrder": 6,
		"kanban": false
	},
	{
		"kpiId": "kpi28",
		"categoryId": "categoryTwo",
		"kpiOrder": 7,
		"kanban": false
	},
	{
		"kpiId": "kpi36",
		"categoryId": "categoryTwo",
		"kpiOrder": 8,
		"kanban": false
	},
	{
		"kpiId": "kpi126",
		"categoryId": "categoryTwo",
		"kpiOrder": 9,
		"kanban": false
	},
	{
		"kpiId": "kpi42",
		"categoryId": "categoryTwo",
		"kpiOrder": 10,
		"kanban": false
	},
	{
		"kpiId": "kpi16",
		"categoryId": "categoryTwo",
		"kpiOrder": 11,
		"kanban": false
	},
	{
		"kpiId": "kpi17",
		"categoryId": "categoryTwo",
		"kpiOrder": 12,
		"kanban": false
	},
	{
		"kpiId": "kpi38",
		"categoryId": "categoryTwo",
		"kpiOrder": 13,
		"kanban": false
	},
	{
		"kpiId": "kpi27",
		"categoryId": "categoryTwo",
		"kpiOrder": 14,
		"kanban": false
	},
    {
		"kpiId": "kpi168",
		"categoryId": "categoryTwo",
		"kpiOrder": 15,
		"kanban": false
	},
	{
		"kpiId": "kpi70",
		"categoryId": "categoryTwo",
		"kpiOrder": 16,
		"kanban": false
	},
	{
		"kpiId": "kpi73",
		"categoryId": "categoryThree",
		"kpiOrder": 2,
		"kanban": false
	},
	{
		"kpiId": "kpi113",
		"categoryId": "categoryThree",
		"kpiOrder": 3,
		"kanban": false
	},
    {
		"kpiId": "kpi153",
		"categoryId": "categoryThree",
		"kpiOrder": 4,
		"kanban": false
	},
	{
		"kpiId": "kpi49",
		"categoryId": "categoryOne",
		"kpiOrder": 1,
		"kanban": true
	},
	{
		"kpiId": "kpi58",
		"categoryId": "categoryOne",
		"kpiOrder": 2,
		"kanban": true
	},
	{
		"kpiId": "kpi66",
		"categoryId": "categoryOne",
		"kpiOrder": 3,
		"kanban": true
	},
	{
		"kpiId": "kpi53",
		"categoryId": "categoryOne",
		"kpiOrder": 5,
		"kanban": true
	},
	{
		"kpiId": "kpi55",
		"categoryId": "categoryTwo",
		"kpiOrder": 1,
		"kanban": true
	},
	{
		"kpiId": "kpi54",
		"categoryId": "categoryTwo",
		"kpiOrder": 2,
		"kanban": true
	},
	{
		"kpiId": "kpi50",
		"categoryId": "categoryTwo",
		"kpiOrder": 3,
		"kanban": true
	},
	{
		"kpiId": "kpi51",
		"categoryId": "categoryTwo",
		"kpiOrder": 4,
		"kanban": true
	},
	{
		"kpiId": "kpi48",
		"categoryId": "categoryTwo",
		"kpiOrder": 5,
		"kanban": true
	},
	{
		"kpiId": "kpi997",
		"categoryId": "categoryTwo",
		"kpiOrder": 6,
		"kanban": true
	},
	{
		"kpiId": "kpi63",
		"categoryId": "categoryTwo",
		"kpiOrder": 7,
		"kanban": true
	},
	{
		"kpiId": "kpi62",
		"categoryId": "categoryTwo",
		"kpiOrder": 8,
		"kanban": true
	},
	{
		"kpiId": "kpi64",
		"categoryId": "categoryTwo",
		"kpiOrder": 9,
		"kanban": true
	},
	{
		"kpiId": "kpi67",
		"categoryId": "categoryTwo",
		"kpiOrder": 10,
		"kanban": true
	},
	{
		"kpiId": "kpi71",
		"categoryId": "categoryTwo",
		"kpiOrder": 11,
		"kanban": true
	},
	{
		"kpiId": "kpi74",
		"categoryId": "categoryThree",
		"kpiOrder": 1,
		"kanban": true
	},
	{
		"kpiId": "kpi114",
		"categoryId": "categoryThree",
		"kpiOrder": 2,
		"kanban": true
	},
    {
    	"kpiId": "kpi164",
    	"categoryId": "categoryOne",
    	"kpiOrder": 9,
    	"kanban": false
    }
]);


//Fields, used on issue details for KPI issue lists
db.kpi_column_configs.insertMany([
{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi8',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Job Name',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Start Time',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'End Time',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Duration',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Build Status',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Build Url',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Weeks',
                                 			order: 7,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi40',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Sprint Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Story ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi11',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Repository Url',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Branch',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Day',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'No. Of Commit',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'No. of Merge',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi84',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Repository Url',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Branch',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Weeks',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Mean Time To Merge (In Hours)',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi3',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Story ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Intake to DOR(In Days)',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'DOR to DOD (In Days)',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'DOD TO Live (In Days)',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Lead Time (In Days)',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi53',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Story ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Open to Triage(In Days)',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Triage to Complete (In Days)',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Complete TO Live (In Days)',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Lead Time (In Days)',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi39',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Sprint Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Story ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Size(story point/hours)',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi5',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Sprint Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Story ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Size(story point/hours)',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi46',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Sprint Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Story ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Original Time Estimate (in hours)',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Total Time Spent (in hours)',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi72',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Sprint Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Story ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Status',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		},
                                 		 {
                                            columnName: 'Issue Type',
                                            order: 3,
                                            isShown: true,
                                            isDefault: true
                                           },
                                           {
                                 			columnName: 'Initial Commitment',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                 		},
                                 		{
                                 			columnName: 'Size(story point/hours)',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: true
                                 		}
                                 		]
                                 	},
                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi14',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Sprint Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Story ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Linked Defects',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi82',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Sprint Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Story ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'First Time Pass',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi111',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Sprint Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Story ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Linked Defects to Story',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Size(story point/hours)',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi35',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Sprint Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Defect ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Escaped Defect',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi34',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Sprint Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Defect ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Defect Removed',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi37',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Sprint Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Defect ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Defect Rejected',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi28',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Sprint Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Defect ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Priority',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi36',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Sprint Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Defect ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Root Cause',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi132',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Defect ID',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Status',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Size(story point/hours)',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Root Cause',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Priority',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Assignee',
                                 			order: 7,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi136',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Defect ID',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Status',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Size(story point/hours)',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Root Cause',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Priority',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Assignee',
                                 			order: 7,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi126',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Sprint Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Created Defect ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Resolved',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi42',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Sprint Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Test Case ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Automated',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi16',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Sprint Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Test Case ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Linked Story ID',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Automated',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi17',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Job Name',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Unit Coverage',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Weeks',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi38',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Job Name',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Sonar Violations',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Weeks',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi27',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Job Name',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Tech Debt (in days)',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Weeks',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi116',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Job Name',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Total Build Count',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Total Build Failure Count',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Build Failure Percentage',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Weeks',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi70',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Sprint Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Total Test',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Executed Test',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Execution %',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Passed Test',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Passed %',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi113',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Cost of Delay',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Epic ID',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Epic Name',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Epic End Date',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Month',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi125',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue ID',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Status',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Size(story point/hours)',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Planned Completion Date (Due Date)',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Actual Completion Date',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Remaining Estimate',
                                 			order: 7,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Potential Delay(in days)',
                                 			order: 8,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Predicted Completion Date',
                                 			order: 9,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Assignee',
                                 			order: 10,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi73',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Release Name',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Release Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Release End Date',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Month',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi118',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Date',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Job Name',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Month',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Environment',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi80',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Priority',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Defects Without Story Link',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi79',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Test Case ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Linked to Story',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi129',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue Id',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi127',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Defect ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Priority',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Created Date',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Status',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi62',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Job Name',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Unit Coverage',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Day/Week/Month',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi64',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Job Name',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Sonar Violations',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Day/Week/Month',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi67',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Job Name',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Tech Debt (in days)',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Day/Week/Month',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi71',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Execution Date',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Total Test',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Executed Test',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Execution %',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Passed Test',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Passed %',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi63',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Day/Week/Month',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Test Case ID',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Automated',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi997',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Ticket Issue ID',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Priority',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Created Date',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Status',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi48',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Day/Week/Month',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Ticket Issue ID',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Status',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Created Date',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi51',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Day/Week/Month',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Ticket Issue ID',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Root Cause',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Created Date',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi50',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Day/Week/Month',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Ticket Issue ID',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Priority',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Created Date',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi55',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Day/Week/Month',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Ticket Issue ID',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Status',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi54',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Day/Week/Month',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Ticket Issue ID',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Priority',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Status',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi49',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Day/Week/Month',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Ticket Issue ID',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Size (In Story Points)',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi66',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Job Name',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Start Time',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'End Time',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Duration',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Build Status',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Build Url',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi65',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Repository Url',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Branch',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Day',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'No. Of Commit',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi58',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Start Date',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'End Date',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Estimated Capacity (in hours)',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi123',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue Id',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Priority',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Size(story point/hours)',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Status',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Due Date',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Remaining Estimate',
                                 			order: 7,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Predicted Completion Date',
                                 			order: 8,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Assignee',
                                 			order: 9,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi120',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue Id',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Status',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Size(story point/hours)',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Priority',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Due Date',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Original Estimate',
                                 			order: 7,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Remaining Estimate',
                                 			order: 8,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Assignee',
                                 			order: 9,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi124',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue Id',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Status',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Assignee',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi130',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue Id',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Priority',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Status',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Due Date',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Remaining Hours',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Delay',
                                 			order: 7,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi75',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue Id',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Status',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Original Estimate',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Logged Work',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Assignee',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi119',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue Id',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Status',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Size(story point/hours)',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Original Estimate',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Remaining Estimate',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Dev Due Date',
                                 			order: 7,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Dev Completion Date',
                                 			order: 8,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Due Date',
                                 			order: 9,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Predicted Completion Date',
                                 			order: 10,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Potential Delay(in days)',
                                 			order: 11,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Assignee',
                                 			order: 12,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi131',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue Id',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Priority',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Size(story point/hours)',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Blocked Time',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Wait Time',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Total Wastage',
                                 			order: 7,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Assignee',
                                 			order: 8,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi133',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue Id',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Status',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Priority',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Linked Defect',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		},{
                                            columnName: 'Size(story point/hours)',
                                            order: 6,
                                            isShown: true,
                                            isDefault: true
                                        },{
                                            columnName : "DIR",
                                            order : Double("7"),
                                            isShown : true,
                                            isDefault : false
                                        },
                                        {
                                            columnName : "Defect Density",
                                            order : Double("8"),
                                            isShown : true,
                                            isDefault : false
                                        },
                                        {
                                            columnName : "Assignee",
                                            order : Double("9"),
                                            isShown : true,
                                            isDefault : false
                                        }]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi134',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue Id',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Priority',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Status',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Size(story point/hours)',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Remaining Estimate',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Assignee',
                                 			order: 7,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi122',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue Id',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Size(story point/hours)',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Status',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Due Date',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Remaining Estimate',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Assignee',
                                 			order: 7,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi135',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue Id',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'First Time Pass',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Linked Defect',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Defect Priority',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},

                                 	{
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi128',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue Id',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Status',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Size(story point/hours)',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Original Estimate',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Remaining Estimate',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Due Date',
                                 			order: 7,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Actual Start Date',
                                 			order: 8,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Dev Completion Date',
                                 			order: 9,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Actual Completion Date',
                                 			order: 10,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Delay(in days)',
                                 			order: 11,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Predicted Completion Date',
                                 			order: 12,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Potential Delay(in days)',
                                 			order: 13,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Assignee',
                                 			order: 14,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},
                                    {
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi139',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue Id',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Priority',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Status',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Change Date',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Weeks',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Issue Status',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: false
                                 		}]
                                 	},
                                 	{
                                        basicProjectConfigId: null,
                                        kpiId: 'kpi140',
                                        kpiColumnDetails: [{
                                            columnName: 'Defect ID',
                                            order: 0,
                                            isShown: true,
                                            isDefault: false
                                        }, {
                                            columnName: 'Issue Description',
                                            order: 1,
                                            isShown: true,
                                            isDefault: true
                                        }, {
                                            columnName: 'Issue Status',
                                            order: 2,
                                            isShown: true,
                                            isDefault: true
                                        }, {
                                            columnName: 'Issue Type',
                                            order: 3,
                                            isShown: true,
                                            isDefault: true
                                        }, {
                                            columnName: 'Size(story point/hours)',
                                            order: 4,
                                            isShown: true,
                                            isDefault: true
                                        }, {
                                            columnName: 'Root Cause',
                                            order: 5,
                                            isShown: true,
                                            isDefault: false
                                        }, {
                                            columnName: 'Priority',
                                            order: 6,
                                            isShown: true,
                                            isDefault: true
                                        }, {
                                            columnName: 'Assignee',
                                            order: 7,
                                            isShown: true,
                                            isDefault: false
                                        }, {
                                            columnName: 'Created during Iteration',
                                            order: 8,
                                            isShown: true,
                                            isDefault: false
                                        }]
                                 	},
                                    {
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi141',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue ID',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                        }, {
                                 			columnName: 'Sprint Name',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                        }, {
                                 			columnName: 'Issue Status',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                        }, {
                                 			columnName: 'Root Cause',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: true
                                        }, {
                                 			columnName: 'Priority',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Assignee',
                                 			order: 7,
                                 			isShown: true,
                                 			isDefault: true
                                 		}]
                                 	},
                                    {
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi142',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue ID',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                        }, {
                                 			columnName: 'Sprint Name',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                        }, {
                                 			columnName: 'Issue Status',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                        }, {
                                 			columnName: 'Root Cause',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: true
                                        }, {
                                 			columnName: 'Priority',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Assignee',
                                 			order: 7,
                                 			isShown: true,
                                 			isDefault: true
                                 		}]
                                 	},
                                    {
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi143',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue ID',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                        }, {
                                 			columnName: 'Sprint Name',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                        }, {
                                 			columnName: 'Issue Status',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                        }, {
                                 			columnName: 'Root Cause',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: true
                                        }, {
                                 			columnName: 'Priority',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Assignee',
                                 			order: 7,
                                 			isShown: true,
                                 			isDefault: true
                                 		}]
                                 	},
                                    {
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi144',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Issue ID',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Description',
                                 			order: 1,
                                 			isShown: true,
                                 			isDefault: true
                                        }, {
                                 			columnName: 'Sprint Name',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Issue Type',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                        }, {
                                 			columnName: 'Issue Status',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: true
                                        }, {
                                 			columnName: 'Root Cause',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: true
                                        }, {
                                 			columnName: 'Priority',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Assignee',
                                 			order: 7,
                                 			isShown: true,
                                 			isDefault: true
                                 		}]
                                 	},
                                 	{
                                    basicProjectConfigId: null,
                                    kpiId: 'kpi147',
                                    kpiColumnDetails: [{
                                      columnName: 'Issue ID',
                                      order: 0,
                                      isShown: true,
                                      isDefault: true
                                    },
                                    {
                                      columnName: 'Issue Type',
                                      order: 3,
                                      isShown: true,
                                      isDefault: true
                                    },
                                    {
                                      columnName: 'Issue Description',
                                      order: 1,
                                      isShown: true,
                                      isDefault: true
                                    }, {
                                      columnName: 'Priority',
                                      order: 6,
                                      isShown: true,
                                      isDefault: true
                                    }, {
                                      columnName: 'Assignee',
                                      order: 7,
                                      isShown: true,
                                      isDefault: true
                                    }, {
                                      columnName: 'Issue Status',
                                      order: 4,
                                      isShown: true,
                                      isDefault: true
                                    }]
                                  },
                                  {
                                     basicProjectConfigId: null,
                                     kpiId: 'kpi145',
                                     kpiColumnDetails: [{
                                       columnName: 'Issue Id',
                                       order: 0,
                                       isShown: true,
                                       isDefault: true
                                    },{
                                       columnName: 'Issue Description',
                                       order: 1,
                                       isShown: true,
                                       isDefault: true
                                    }, {
                                       columnName: 'Issue Status',
                                       order: 2,
                                       isShown: true,
                                       isDefault: true
                                    }, {
                                       columnName: 'Issue Type',
                                       order: 3,
                                       isShown: true,
                                       isDefault: true
                                    }, {
                                       columnName: 'Size(story point/hours)',
                                       order: 4,
                                       isShown: true,
                                       isDefault: true
                                    }, {
                                       columnName: 'Remaining Estimate',
                                       order: 5,
                                       isShown: true,
                                       isDefault: false
                                    }, {
                                       columnName: 'Dev Due Date',
                                       order: 6,
                                       isShown: true,
                                       isDefault: false
                                    }, {
                                       columnName: 'Dev Completion Date',
                                       order: 7,
                                       isShown: true,
                                       isDefault: false
                                    }]
                                  },
                                  {
                                    basicProjectConfigId: null,
                                    kpiId: 'kpi138',
                                    kpiColumnDetails: [{
                                        columnName: 'Issue Id',
                                        order: 0,
                                        isShown: true,
                                        isDefault: true
                                    }, {
                                        columnName: 'Issue Type',
                                        order: 1,
                                        isShown: true,
                                        isDefault: true
                                    },{
                                        columnName: 'Issue Description',
                                        order: 2,
                                        isShown: true,
                                        isDefault: true
                                    },{
                                        columnName: 'Priority',
                                        order: 3,
                                        isShown: true,
                                        isDefault: false
                                    }, {
                                        columnName: 'Size(story point/hours)',
                                        order: 4,
                                        isShown: true,
                                        isDefault: false
                                    }]
                                  },
                                  {
                                    basicProjectConfigId: null,
                                    kpiId: 'Kpi146',
                                    kpiColumnDetails: [{
                                        columnName: 'Date',
                                        order: 0,
                                        isShown: true,
                                        isDefault: true
                                    }]
                                   },
                                  {
                                    basicProjectConfigId: null,
                                    kpiId: 'Kpi148',
                                    kpiColumnDetails: [{
                                        columnName: 'Date',
                                        order: 0,
                                        isShown: true,
                                        isDefault: true
                                        }]
                                  },
                                  {
                                    basicProjectConfigId: null,
                                    kpiId: 'kpi149',
                                    kpiColumnDetails: [{
                                        columnName: 'Sprint Name',
                                        order: 0,
                                        isShown: true,
                                        isDefault: true
                                    }, {
                                        columnName: 'User Name',
                                        order: 1,
                                        isShown: true,
                                        isDefault: true
                                    },{
                                        columnName: 'Sprint Rating',
                                        order: 2,
                                        isShown: true,
                                        isDefault: true
                                    }]
                                  },
                                  {
                                    basicProjectConfigId: null,
                                    kpiId: 'kpi150',
                                    kpiColumnDetails: [{
                                      columnName: 'Issue ID',
                                      order: 0,
                                      isShown: true,
                                      isDefault: true
                                    },
                                    {
                                      columnName: 'Issue Type',
                                      order: 3,
                                      isShown: true,
                                      isDefault: true
                                    },
                                    {
                                      columnName: 'Issue Description',
                                      order: 2,
                                      isShown: true,
                                      isDefault: true
                                    }, {
                                      columnName: 'Story Size(In story point)',
                                      order: 3,
                                      isShown: true,
                                      isDefault: true
                                    }, {
                                      columnName: 'Priority',
                                      order: 4,
                                      isShown: true,
                                      isDefault: true
                                    }, {
                                      columnName: 'Assignee',
                                      order: 5,
                                      isShown: true,
                                      isDefault: true
                                    },
                                    {
                                      columnName: 'Issue Status',
                                      order: 6,
                                      isShown: true,
                                      isDefault: true
                                    }]
                                  },
                                  {
                                    basicProjectConfigId: null,
                                    kpiId: 'kpi151',
                                    kpiColumnDetails: [{
                                      columnName: 'Issue ID',
                                      order: 0,
                                      isShown: true,
                                      isDefault: true
                                    },
                                    {
                                      columnName: 'Issue Description',
                                      order: 1,
                                      isShown: true,
                                      isDefault: true
                                    },
                                    {
                                      columnName: 'Issue Type',
                                      order: 2,
                                      isShown: true,
                                      isDefault: true
                                    }, {
                                      columnName: 'Issue Status',
                                      order: 3,
                                      isShown: true,
                                      isDefault: true
                                    }, {
                                      columnName: 'Priority',
                                      order: 4,
                                      isShown: true,
                                      isDefault: true
                                    }, {
                                      columnName: 'Created Date',
                                      order: 5,
                                      isShown: true,
                                      isDefault: true
                                    }, {
                                      columnName: 'Updated Date',
                                      order: 6,
                                      isShown: true,
                                      isDefault: true
                                    },
                                    {
                                      columnName: 'Assignee',
                                      order: 7,
                                      isShown: true,
                                      isDefault: true
                                    }]
                                  },
                                  {
                                    basicProjectConfigId: null,
                                    kpiId: 'kpi152',
                                    kpiColumnDetails: [{
                                      columnName: 'Issue ID',
                                      order: 0,
                                      isShown: true,
                                      isDefault: true
                                    },
                                    {
                                      columnName: 'Issue Description',
                                      order: 1,
                                      isShown: true,
                                      isDefault: true
                                    },
                                    {
                                      columnName: 'Issue Type',
                                      order: 2,
                                      isShown: true,
                                      isDefault: true
                                    }, {
                                      columnName: 'Issue Status',
                                      order: 3,
                                      isShown: true,
                                      isDefault: true
                                    }, {
                                      columnName: 'Priority',
                                      order: 4,
                                      isShown: true,
                                      isDefault: true
                                    }, {
                                      columnName: 'Created Date',
                                      order: 5,
                                      isShown: true,
                                      isDefault: true
                                    }, {
                                      columnName: 'Updated Date',
                                      order: 6,
                                      isShown: true,
                                      isDefault: true
                                    },
                                    {
                                      columnName: 'Assignee',
                                      order: 7,
                                      isShown: true,
                                      isDefault: true
                                    }]
                                  },
                                   {
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi153',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		},  {
                                 			columnName: 'Epic ID',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Epic Name',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Status',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'PI Name',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Planned Value',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Achieved Value',
                                 			order: 7,
                                 			isShown: true,
                                 			isDefault: false
                                 		 }]
                                 	},
                                 	{
                                          basicProjectConfigId: null,
                                          kpiId: "kpi157",
                                          kpiColumnDetails: [
                                            {
                                              columnName: "Project Name",
                                              order: 0,
                                              isShown: true,
                                              isDefault: true,
                                            },
                                            {
                                              columnName: "Repository Url",
                                              order: 1,
                                              isShown: true,
                                              isDefault: true,
                                            },
                                            {
                                              columnName: "Branch",
                                              order: 2,
                                              isShown: true,
                                              isDefault: true,
                                            },
                                            {
                                              columnName: "Day",
                                              order: 3,
                                              isShown: true,
                                              isDefault: true,
                                            },
                                            {
                                              columnName: "No. Of Commit",
                                              order: 4,
                                              isShown: true,
                                              isDefault: true,
                                            },
                                            {
                                              columnName: "No. of Merge",
                                              order: 5,
                                              isShown: true,
                                              isDefault: true,
                                            },
                                          ],
                                        },
                                    {
                                      basicProjectConfigId: null,
                                      kpiId: "kpi158",
                                      kpiColumnDetails: [
                                        {
                                          columnName: "Project Name",
                                          order: 0,
                                          isShown: true,
                                          isDefault: true,
                                        },
                                        {
                                          columnName: "Repository Url	",
                                          order: 1,
                                          isShown: true,
                                          isDefault: true,
                                        },
                                        {
                                          columnName: "Branch",
                                          order: 2,
                                          isShown: true,
                                          isDefault: true,
                                        },
                                        {
                                          columnName: "Weeks",
                                          order: 3,
                                          isShown: true,
                                          isDefault: true,
                                        },
                                        {
                                          columnName: "Mean Time To Merge (In Hours)",
                                          order: 4,
                                          isShown: true,
                                          isDefault: true,
                                        },
                                      ],
                                    },
                                    {
                                      basicProjectConfigId: null,
                                      kpiId: "kpi159",
                                      kpiColumnDetails: [
                                        {
                                          columnName: "Project Name",
                                          order: 0,
                                          isShown: true,
                                          isDefault: true,
                                        },
                                        {
                                          columnName: "Repository Url	",
                                          order: 1,
                                          isShown: true,
                                          isDefault: true,
                                        },
                                        {
                                          columnName: "Branch",
                                          order: 2,
                                          isShown: true,
                                          isDefault: true,
                                        },
                                        {
                                          columnName: "Days",
                                          order: 3,
                                          isShown: true,
                                          isDefault: true,
                                        },
                                        {
                                          columnName: "No. Of Commit",
                                          order: 4,
                                          isShown: true,
                                          isDefault: true,
                                        },
                                      ],
                                    },
                                    {
                                      basicProjectConfigId: null,
                                      kpiId: "kpi160",
                                      kpiColumnDetails: [
                                        {
                                          columnName: "Project Name",
                                          order: 0,
                                          isShown: true,
                                          isDefault: true,
                                        },
                                        {
                                          columnName: "Repository Url	",
                                          order: 1,
                                          isShown: true,
                                          isDefault: true,
                                        },
                                        {
                                          columnName: "Branch",
                                          order: 2,
                                          isShown: true,
                                          isDefault: true,
                                        },
                                        {
                                          columnName: "Weeks",
                                          order: 3,
                                          isShown: true,
                                          isDefault: true,
                                        },
                                        {
                                          columnName: "Pickup Time (In Hours)",
                                          order: 4,
                                          isShown: true,
                                          isDefault: true,
                                        },
                                      ],
                                    },
                                    {
                                      basicProjectConfigId: null,
                                      kpiId: "kpi162",
                                      kpiColumnDetails: [
                                        {
                                          columnName: "Project Name",
                                          order: 0,
                                          isShown: true,
                                          isDefault: true,
                                        },
                                        {
                                          columnName: "Repository Url	",
                                          order: 1,
                                          isShown: true,
                                          isDefault: true,
                                        },
                                        {
                                          columnName: "Branch",
                                          order: 2,
                                          isShown: true,
                                          isDefault: true,
                                        },
                                        {
                                          columnName: "Weeks",
                                          order: 3,
                                          isShown: true,
                                          isDefault: true,
                                        },
                                        {
                                          columnName: "PR Size",
                                          order: 4,
                                          isShown: true,
                                          isDefault: true,
                                        },
                                      ],
                                    },
                                 	{
                                    	basicProjectConfigId: null,
                                    	kpiId: 'kpi164',
                                    	kpiColumnDetails: [{
                                    		columnName: 'Sprint Name',
                                    		order: 0,
                                    		isShown: true,
                                    		isDefault: false
                                    	}, {
                                    		columnName: 'Issue ID',
                                    		order: 2,
                                    		isShown: true,
                                    		isDefault: false
                                    	}, {
                                    		columnName: 'Issue Type',
                                    		order: 3,
                                    		isShown: true,
                                    		isDefault: false
                                    	}, {
                                    		columnName: 'Issue Description',
                                    		order: 4,
                                    		isShown: true,
                                    		isDefault: false
                                    	}, {
                                    		columnName: 'Size(story point/hours)',
                                    		order: 5,
                                    		isShown: true,
                                    		isDefault: false
                                    	}, {
                                    		columnName: 'Scope Change Date',
                                    		order: 6,
                                    		isShown: true,
                                    		isDefault: false
                                    	}, {
                                    		columnName: 'Scope Change (Added/Removed)',
                                    		order: 7,
                                    		isShown: true,
                                    		isDefault: false
                                    	}, {
                                    		columnName: 'Issue Status',
                                    		order: 8,
                                    		isShown: true,
                                    		isDefault: false
                                    	}]
                                    }
                                   ]);

//default fields mapping structure for KPI, these fields are used to populate the config JIRA for any
//project. these can be changed/updated in project config under setting in the KnowHOW

//field_mapping_structure
db.getCollection('field_mapping_structure').insertMany(
[
{
        "fieldName": "jiraStoryIdentificationKpi40",
        "fieldLabel": "Issue type to identify Story",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "All issue types that are used as/equivalent to Story.",

        }
    }, {
        "fieldName": "jiraStoryIdentificationKPI129",
        "fieldLabel": "Issue type to identify Story",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "All issue types that are used as/equivalent to Story.",

        }
    },
    {
        "fieldName": "jiraSprintCapacityIssueTypeKpi46",
        "fieldLabel": "Issue type to be included",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "All issues types against work is logged and should be considered for Utilization"
        }
    },
    {
        "fieldName": "jiraIterationIssuetypeKPI122",
        "fieldLabel": "Issue type to be included",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "All issues types added will only be included in showing closures (Note: If nothing is added then all issue types by default will be considered)"
        }
    },
    {
        "fieldName": "jiraIterationIssuetypeKPI124",
        "fieldLabel": "Issue type to be included",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "All issues types added will only be included in showing closures (Note: If nothing is added then all issue types by default will be considered)"
        }
    },
    {
        "fieldName": "jiraIterationIssuetypeKPI138",
        "fieldLabel": "Iteration Board Issue types",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "Issue Types to be considered Completed"
        }
    },
    {
        "fieldName": "jiraIterationIssuetypeKPI131",
        "fieldLabel": "Issue type to be included",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "All issues types added will only be included in showing closures (Note: If nothing is added then all issue types by default will be considered)"
        }
    },
    {
        "fieldName": "jiraIterationIssuetypeKPI128",
        "fieldLabel": "Issue type to be included",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "All issues types added will only be included in showing closures (Note: If nothing is added then all issue types by default will be considered)"
        }
    },
    {
        "fieldName": "jiraIterationIssuetypeKPI134",
        "fieldLabel": "Issue type to be included",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "All issues types added will only be included in showing closures (Note: If nothing is added then all issue types by default will be considered)"
        }
    },
    {
        "fieldName": "jiraIterationIssuetypeKPI145",
        "fieldLabel": "Issue type to be included",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "All issues types added will only be included in showing closures (Note: If nothing is added then all issue types by default will be considered)"
        }
    },
    {
        "fieldName": "jiraIterationIssuetypeKpi72",
        "fieldLabel": "Issue type to be included",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "All issue types that are considered in sprint commitment"
        }
    }, {
        "fieldName": "jiraIterationIssuetypeKpi5",
        "fieldLabel": "Issue type to be included",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "All issue types that should be included in Sprint Predictability calculation"
        }
    },
    {
        "fieldName": "jiraIterationIssuetypeKPI119",
        "fieldLabel": "Issue type to be included",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "All issues types added will only be included in showing closures (Note: If nothing is added then all issue types by default will be considered)"
        }
    },
    {
        "fieldName": "jiraIterationIssuetypeKPI75",
        "fieldLabel": "Issue type to be included",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "All issues types added will only be included in showing closures (Note: If nothing is added then all issue types by default will be considered)"
        }
    },
    {
        "fieldName": "jiraIterationIssuetypeKPI123",
        "fieldLabel": "Issue type to be included",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "All issues types added will only be included in showing closures (Note: If nothing is added then all issue types by default will be considered)"
        }
    },
    {
        "fieldName": "jiraIterationIssuetypeKPI125",
        "fieldLabel": "Issue type to be included",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "All issues types added will only be included in showing closures (Note: If nothing is added then all issue types by default will be considered)"
        }
    },
    {
        "fieldName": "jiraIterationIssuetypeKPI120",
        "fieldLabel": "Issue type to be included",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "All issues types added will only be included in showing closures (Note: If nothing is added then all issue types by default will be considered)"
        }
    },
    {
        "fieldName": "jiraIterationIssuetypeKPI39",
        "fieldLabel": "Issue type to be included",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "All issues types added will only be included in showing closures (Note: If nothing is added then all issue types by default will be considered)"
        }
    },
    {
        "fieldName": "jiraSprintVelocityIssueTypeBR",
        "fieldLabel": "Sprint Velocity - Issue Types with Linked Defect",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "All issue types with which defect is linked. <br>  Example: Story, Change Request .<hr>"
        }
    },
    {
        "fieldName": "jiraSprintVelocityIssueTypeEH",
        "fieldLabel": "Sprint Velocity - Issue Types with Linked Defect",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "All issue types with which defect is linked. <br>  Example: Story, Change Request .<hr>"
        }
    },
    {
        "fieldName": "jiraIssueDeliverdStatusKPI138",
        "fieldLabel": "Issue Delivered Status",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Status from workflow on which issue is delivered. <br> Example: Closed<hr>"
        }
    },
    {
        "fieldName": "jiraIssueDeliverdStatusKPI126",
        "fieldLabel": "Status to identify completed issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Status considered for defect closure (Mention completed status of all types of defects)"
        }
    },
    {
        "fieldName": "jiraIssueDeliverdStatusKPI82",
        "fieldLabel": "Status to identify completed issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Completion status for all issue types mentioned for calculation of FTPR"
        }
    },
    {
        "fieldName": "resolutionTypeForRejectionKPI28",
        "fieldLabel": "Resolution type to be excluded",
        "fieldType": "chips",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Resolutions for defects which are to be excluded from 'Defect count by Priority' calculation"
        }
    },
    {
        "fieldName": "resolutionTypeForRejectionKPI37",
        "fieldLabel": "Resolution type to be included",
        "fieldType": "chips",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Resolutions for defects which are to be excluded from 'Defect Rejection Rate' calculation."
        }
    },
    {
        "fieldName": "resolutionTypeForRejectionDSR",
        "fieldLabel": "Resolution Type for Rejection",
        "fieldType": "chips",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Resolution type to identify rejected defects. <br>"
        }
    },
    {
        "fieldName": "resolutionTypeForRejectionKPI82",
        "fieldLabel": "Resolution type to be excluded",
        "fieldType": "chips",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Resolutions for defects which are to be excluded from 'FTPR' calculation"
        }
    },
    {
        "fieldName": "resolutionTypeForRejectionKPI135",
        "fieldLabel": "Resolution type to be excluded",
        "fieldType": "chips",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Resolutions for defects which are to be excluded from 'FTPR' calculation"
        }
    },
    {
        "fieldName": "resolutionTypeForRejectionKPI133",
        "fieldLabel": "Resolution type to be excluded",
        "fieldType": "chips",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Resolutions for defects which are to be excluded from 'Quality Status' calculation"
        }
    },
    {
        "fieldName": "resolutionTypeForRejectionRCAKPI36",
        "fieldLabel": "Resolution type to be excluded",
        "fieldType": "chips",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Resolutions for defects which are to be excluded from 'Defect count by RCA' calculation."
        }
    },
    {
        "fieldName": "resolutionTypeForRejectionKPI14",
        "fieldLabel": "Resolution type to be excluded",
        "fieldType": "chips",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Resolutions for defects which are to be excluded from 'Defect Injection rate' calculation <br>"
        }
    },
    {
        "fieldName": "resolutionTypeForRejectionQAKPI111",
        "fieldLabel": "Resolution type to be excluded",
        "fieldType": "chips",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Resolutions for defects which are to be excluded from 'Defect Density' calculation."
        }
    },

    {
        "fieldName": "jiraDefectRejectionStatusKPI28",
        "fieldLabel": "Status to be excluded",
        "fieldType": "text",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses which are considered for Rejecting defects."
        }
    },
    {
        "fieldName": "jiraDefectRejectionStatusKPI37",
        "fieldLabel": "Status to identify Rejected defects",
        "fieldType": "text",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses which are considered for Rejecting defects."
        }
    },
    {
        "fieldName": "jiraDefectRejectionStatusDSR",
        "fieldLabel": "Defect Rejection Status",
        "fieldType": "text",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Status from workflow on which defect is considered as rejected. <br>Example: Cancelled<hr>"
        }
    },
    {
        "fieldName": "jiraDefectRejectionStatusKPI82",
        "fieldLabel": "Status to be excluded",
        "fieldType": "text",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses which are considered for Rejecting defects"
        }
    },
    {
        "fieldName": "jiraDefectRejectionStatusKPI135",
        "fieldLabel": "Defect Rejection Status",
        "fieldType": "text",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Status from workflow on which defect is considered as rejected. <br>Example: Cancelled<hr>"
        }
    },
    {
        "fieldName": "jiraDefectRejectionStatusKPI133",
        "fieldLabel": "Status to be excluded",
        "fieldType": "text",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses which are considered for Rejecting defects."
        }
    },
    {
        "fieldName": "jiraDefectRejectionStatusRCAKPI36",
        "fieldLabel": "Status to be excluded",
        "fieldType": "text",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses which are considered for Rejecting defects."
        }
    },
    {
        "fieldName": "jiraDefectRejectionStatusKPI14",
        "fieldLabel": "Status to be excluded",
        "fieldType": "text",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses which are considered for Rejecting defects"
        }
    },
    {
        "fieldName": "jiraDefectRejectionStatusQAKPI111",
        "fieldLabel": "Status to be excluded",
        "fieldType": "text",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses which are considered for Rejecting defects."
        }
    },
    {
        "fieldName": "jiraDefectRejectionStatusKPI151",
        "fieldLabel": "Status to be excluded",
        "fieldType": "text",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses which are considered for Rejecting defects."
        }
    },
    {
        "fieldName": "jiraDefectRejectionStatusKPI152",
        "fieldLabel": "Status to be excluded",
        "fieldType": "text",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses which are considered for Rejecting defects."
        }
    },
    {
        "fieldName": "jiraStatusForDevelopmentKPI82",
        "fieldLabel": "Status for 'In Development' issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses that relate to In development status of a Story"
        }
    },
    {
        "fieldName": "jiraStatusForDevelopmentKPI135",
        "fieldLabel": "Status for 'In Development' issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses that relate to In development status of a Story"
        }
    },
    {
        "fieldName": "jiraDorKPI3",
        "fieldLabel": "DOR status",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Status/es that identify that an issue is ready to be taken in the sprint."
        }
    },
    {
        "fieldName": "jiraIssueTypeKPI3",
        "fieldLabel": "Issue type to be included",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "All issue types that should be included in Lead time calculation."
        }
    },
    {
        "fieldName": "jiraKPI82StoryIdentification",
        "fieldLabel": "Issue type to be included",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "All issue types for which FTPR should be calculated"
        }
    },
    {
        "fieldName": "jiraKPI135StoryIdentification",
        "fieldLabel": "Issue type to be included",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "All issue types for which FTPR should be calculated"
        }
    },
    {
        "fieldName": "defectPriorityKPI135",
        "fieldLabel": "Priority to be excluded",
        "fieldType": "multiselect",
        "section": "Defects Mapping",
        "tooltip": {
            "definition": "Priority values of defects which are to be excluded in 'FTPR' calculation"
        },
        "options": [{
                "label": "p1",
                "value": "p1"
            },
            {
                "label": "p2",
                "value": "p2"
            },
            {
                "label": "p3",
                "value": "p3"
            },
            {
                "label": "p4",
                "value": "p4"
            },
            {
                "label": "p5",
                "value": "p5"
            }
        ]
    },
    {
        "fieldName": "defectPriorityKPI14",
        "fieldLabel": "Priority to be excluded",
        "fieldType": "multiselect",
        "section": "Defects Mapping",
        "tooltip": {
            "definition": "Priority values of defects which are to be excluded in 'Defect Injection rate' calculation"
        },
        "options": [{
                "label": "p1",
                "value": "p1"
            },
            {
                "label": "p2",
                "value": "p2"
            },
            {
                "label": "p3",
                "value": "p3"
            },
            {
                "label": "p4",
                "value": "p4"
            },
            {
                "label": "p5",
                "value": "p5"
            }
        ]
    },
    {
        "fieldName": "defectPriorityQAKPI111",
        "fieldLabel": "Priority to be excluded",
        "fieldType": "multiselect",
        "section": "Defects Mapping",
        "tooltip": {
            "definition": "Priority values of defects which are to be excluded in 'Defect Density' calculation"
        },
        "options": [{
                "label": "p1",
                "value": "p1"
            },
            {
                "label": "p2",
                "value": "p2"
            },
            {
                "label": "p3",
                "value": "p3"
            },
            {
                "label": "p4",
                "value": "p4"
            },
            {
                "label": "p5",
                "value": "p5"
            }
        ]
    },
    {
        "fieldName": "defectPriorityKPI82",
        "fieldLabel": "Priority to be excluded",
        "fieldType": "multiselect",
        "section": "Defects Mapping",
        "tooltip": {
            "definition": "Priority values of defects which are to be excluded in 'FTPR' calculation"
        },
        "options": [{
                "label": "p1",
                "value": "p1"
            },
            {
                "label": "p2",
                "value": "p2"
            },
            {
                "label": "p3",
                "value": "p3"
            },
            {
                "label": "p4",
                "value": "p4"
            },
            {
                "label": "p5",
                "value": "p5"
            }
        ]
    },
    {
        "fieldName": "defectPriorityKPI133",
        "fieldLabel": "Priority to be excluded",
        "fieldType": "multiselect",
        "section": "Defects Mapping",
        "tooltip": {
            "definition": "Priority values of defects which are to be excluded in 'Quality Status' calculation"
        },
        "options": [{
                "label": "p1",
                "value": "p1"
            },
            {
                "label": "p2",
                "value": "p2"
            },
            {
                "label": "p3",
                "value": "p3"
            },
            {
                "label": "p4",
                "value": "p4"
            },
            {
                "label": "p5",
                "value": "p5"
            }
        ]
    },
    {
        "fieldName": "excludeRCAFromKPI82",
        "fieldLabel": "Root cause values to be excluded",
        "fieldType": "chips",
        "section": "Defects Mapping",
        "tooltip": {
            "definition": "Root cause reasons for defects which are to be excluded from 'FTPR' calculation"
        }
    },
    {
        "fieldName": "excludeRCAFromKPI135",
        "fieldLabel": "Defect RCA exclusion from Quality KPIs",
        "fieldType": "chips",
        "section": "Defects Mapping",
        "tooltip": {
            "definition": "The defects tagged to priority values selected in this field on Mappings screen will be excluded"
        }
    },
    {
        "fieldName": "excludeRCAFromKPI14",
        "fieldLabel": "Root cause values to be excluded",
        "fieldType": "chips",
        "section": "Defects Mapping",
        "tooltip": {
            "definition": "Root cause reasons for defects which are to be excluded from 'Defect Injection rate' calculation"
        }
    },
    {
        "fieldName": "excludeRCAFromQAKPI111",
        "fieldLabel": "Root cause values to be excluded",
        "fieldType": "chips",
        "section": "Defects Mapping",
        "tooltip": {
            "definition": "Root cause reasons for defects which are to be excluded from 'Defect Density' calculation"
        }
    },
    {
        "fieldName": "excludeRCAFromKPI133",
        "fieldLabel": "Root cause values to be excluded",
        "fieldType": "chips",
        "section": "Defects Mapping",
        "tooltip": {
            "definition": "Root cause reasons for defects which are to be excluded from 'Quality Status' calculation"
        }
    },
    {
        "fieldName": "jiraDefectInjectionIssueTypeKPI14",
        "fieldLabel": "Issue types which will have linked defects",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "Issue type that will have defects linked to them."
        }
    },
    {
        "fieldName": "jiraDefectCreatedStatusKPI14",
        "fieldLabel": "Default status when defect is created",
        "fieldType": "text",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Default status when upon creation of Defect (Mention default status of all types of defects)"
        }
    },
    {
        "fieldName": "jiraDodKPI14",
        "fieldLabel": "Status considered for Issue closure",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Status considered for issue closure (Mention completed status of all types of issues)"
        }
    },
    {
        "fieldName": "jiraDodQAKPI111",
        "fieldLabel": "Status considered for defect closure",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Status considered for defect closure (Mention completed status of all types of defects)"
        }
    },
    {
        "fieldName": "jiraDodKPI171",
        "fieldLabel": "DOD Status",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Status/es that identify that an issue is completed based on Definition of Done (DoD)"
        }
    },
    {
        "fieldName": "jiraDodKPI127",
        "fieldLabel": "Status to identify completed issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Status/es that identify that an issue is completed based on Definition of Done (DoD)"
        }
    },
    {
        "fieldName": "jiraDodKPI152",
        "fieldLabel": "DOD Status",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Status/es that identify that an issue is completed based on Definition of Done (DoD)"
        }
    },
    {
        "fieldName": "jiraDodKPI151",
        "fieldLabel": "DOD Status",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Status/es that identify that an issue is completed based on Definition of Done (DoD)"
        }
    },
    {
        "fieldName": "jiraQAKPI111IssueType",
        "fieldLabel": "Issue types which will have linked defects",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "Issue type that will have defects linked to them."
        }
    },
    {
        "fieldName": "jiraIssueTypeKPI35",
        "fieldLabel": "Issue types which will have linked defects",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "Issue type that will have defects linked to them."
        }
    },
    {
        "fieldName": "jiraIssueTypeNames",
        "fieldLabel": "Issue types to be included",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "processorCommon": true,
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "All the issue types used by a project in Jira."
        }
    },
    {
        "fieldName": "jiraDefectRemovalStatusKPI34",
        "fieldLabel": "Status to identify closed defects",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses which are used when defect is fixed & closed."
        }
    },
    {
        "fieldName": "jiraIssueDeliverdStatusCVR",
        "fieldLabel": "Issue Delivered Status",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Status from workflow on which issue is delivered. <br> Example: Closed<hr>"
        }
    },
    {
        "fieldName": "resolutionTypeForRejectionKPI35",
        "fieldLabel": "Resolution type to be excluded",
        "fieldType": "chips",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Resolutions for defects which are to be excluded from 'Defect Seepage rate' calculation."
        }
    },
    {
        "fieldName": "jiraDefectRejectionStatusKPI35",
        "fieldLabel": "Status to be excluded",
        "fieldType": "text",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses which are considered for Rejecting defects"
        }
    },
    {
        "fieldName": "jiraIterationCompletionStatusKPI135",
        "fieldLabel": "Status to identify completed issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
        }
    },
    {
        "fieldName": "jiraIterationCompletionStatusCustomField",
        "fieldLabel": "Status to identify completed issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Status to identify as closed"
        }
    },
    {
        "fieldName": "jiraIterationCompletionStatusKPI122",
        "fieldLabel": "Status to identify completed issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
        }
    },
    {
        "fieldName": "jiraIterationCompletionStatusKPI75",
        "fieldLabel": "Status to identify completed issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
        }
    },
    {
        "fieldName": "jiraIterationCompletionStatusKPI145",
        "fieldLabel": "Status to identify completed issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
        }
    },
    {
        "fieldName": "jiraIterationCompletionStatusKPI140",
        "fieldLabel": "Status to identify completed issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
        }
    },
    {
        "fieldName": "jiraIterationCompletionStatusKPI132",
        "fieldLabel": "Status to identify completed issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
        }
    },
    {
        "fieldName": "jiraIterationCompletionStatusKPI136",
        "fieldLabel": "Status to identify completed issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
        }
    },
    {
        "fieldName": "jiraIterationCompletionStatusKpi72",
        "fieldLabel": "Status to identify completed issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
        }
    },
    {
        "fieldName": "jiraIterationCompletionStatusKpi39",
        "fieldLabel": "Status to identify completed issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
        }
    },
    {
        "fieldName": "jiraIterationCompletionStatusKpi5",
        "fieldLabel": "Status to identify completed issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
        }
    },
    {
        "fieldName": "jiraIterationCompletionStatusKPI124",
        "fieldLabel": "Status to identify completed issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
        }
    },
    {
        "fieldName": "jiraIterationCompletionStatusKPI123",
        "fieldLabel": "Status to identify completed issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
        }
    },
    {
        "fieldName": "jiraIterationCompletionStatusKPI125",
        "fieldLabel": "Status to identify completed issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
        }
    },
    {
        "fieldName": "jiraIterationCompletionStatusKPI120",
        "fieldLabel": "Status to identify completed issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
        }
    },
    {
        "fieldName": "jiraIterationCompletionStatusKPI128",
        "fieldLabel": "Status to identify completed issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
        }
    },
    {
        "fieldName": "jiraIterationCompletionStatusKPI134",
        "fieldLabel": "Status to identify completed issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
        }
    },
    {
        "fieldName": "jiraIterationCompletionStatusKPI133",
        "fieldLabel": "Status to identify completed issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
        }
    },
    {
        "fieldName": "jiraIterationCompletionStatusKPI119",
        "fieldLabel": "Status to identify completed issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
        }
    },
    {
        "fieldName": "jiraIterationCompletionStatusKPI131",
        "fieldLabel": "Status to identify completed issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
        }
    },
    {
        "fieldName": "jiraWaitStatusKPI131",
        "fieldLabel": "Status that signify queue",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "The statuses wherein no activity takes place and signifies that issue is queued and need to move for work to resume on the issue."
        }
    },
    {
        "fieldName": "jiraIterationCompletionStatusKPI138",
        "fieldLabel": "Status to identify completed issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Status to identify as closed"
        }
    },

    {
        "fieldName": "jiraLiveStatus",
        "fieldLabel": "Status to identify Live status",
        "fieldType": "text",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Provide any status from workflow on which Live is considered."
        }
    }, {
        "fieldName": "jiraLiveStatusKPI152",
        "fieldLabel": "Status to identify Live status",
        "fieldType": "text",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Provide any status from workflow on which Live is considered."
        }
    }, {
        "fieldName": "jiraLiveStatusKPI151",
        "fieldLabel": "Status to identify Live status",
        "fieldType": "text",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Provide any status from workflow on which Live is considered."
        }
    },
    {
        "fieldName": "jiraLiveStatusKPI3",
        "fieldLabel": "Live Status - Lead Time",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Status/es that identify that an issue is LIVE in Production."
        }
    },
    {
        "fieldName": "jiraLiveStatusLTK",
        "fieldLabel": "Live Status - Lead Time",
        "fieldType": "text",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Provide any status from workflow on which Live is considered."
        }
    },
    {
        "fieldName": "jiraLiveStatusNOPK",
        "fieldLabel": "Live Status - Net Open Ticket Count by Priority",
        "fieldType": "text",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Provide any status from workflow on which Live is considered."
        }
    },
    {
        "fieldName": "jiraLiveStatusNOSK",
        "fieldLabel": "Live Status - Net Open Ticket by Status",
        "fieldType": "text",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Provide any status from workflow on which Live is considered."
        }
    },
    {
        "fieldName": "jiraLiveStatusNORK",
        "fieldLabel": "Live Status - Net Open Ticket Count By RCA",
        "fieldType": "text",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Provide any status from workflow on which Live is considered."
        }
    },
    {
        "fieldName": "jiraLiveStatusOTA",
        "fieldLabel": "Live Status - Open Ticket Ageing",
        "fieldType": "text",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Provide any status from workflow on which Live is considered."
        }
    },
    {
        "fieldName": "jiraLiveStatusKPI127",
        "fieldLabel": "Status to identify live issues",
        "fieldType": "text",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Status/es that identify that an issue is LIVE in Production."
        }
    },
    {
        "fieldName": "jiradefecttype",
        "fieldLabel": "Issue Type to identify defects",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "section": "Defects Mapping",
        "processorCommon": true,
        "tooltip": {
            "definition": "All the issue types that signify a defect in Jira/Azure"
        }
    },
    {
        "fieldName": "jiraStoryPointsCustomField",
        "fieldLabel": "Story Points Custom Field",
        "fieldType": "text",
        "fieldCategory": "fields",
        "section": "Custom Fields Mapping",
        "tooltip": {
            "definition": "Field used in Jira for Story points"
        }
    },
    {
        "fieldName": "workingHoursDayCPT",
        "fieldLabel": "Working Hours in a Day",
        "fieldType": "text",
        "fieldCategory": "fields",
        "section": "Custom Fields Mapping",
        "tooltip": {
            "definition": "Working hours in a day"
        }
    },
    {
        "fieldName": "epicCostOfDelay",
        "fieldLabel": "Custom field for COD",
        "fieldType": "text",
        "fieldCategory": "fields",
        "section": "Custom Fields Mapping",
        "tooltip": {
            "definition": "JIRA/AZURE applications let you add custom fields in addition to the built-in fields. Provide value of Cost Of delay field for Epics that need to show on Trend line. <br> Example:customfield_11111 <hr>",

        }
    },
    {
        "fieldName": "epicRiskReduction",
        "fieldLabel": "Custom field for Risk Reduction",
        "fieldType": "text",
        "fieldCategory": "fields",
        "section": "Custom Fields Mapping",
        "tooltip": {
            "definition": "JIRA/AZURE applications let you add custom fields in addition to the built-in fields.Provide value of Risk reduction/ Enablement value for Epic that is required to calculated Cost of delay <br> Example: customfield_11111<hr>",

        }
    },
    {
        "fieldName": "epicUserBusinessValue",
        "fieldLabel": "Custom field for BV",
        "fieldType": "text",
        "fieldCategory": "fields",
        "section": "Custom Fields Mapping",
        "tooltip": {
            "definition": "JIRA/AZURE applications let you add custom fields in addition to the built-in fields.Provide value of User-Business Value for Epic that is required to calculated Cost of delay. <br>Example:customfield_11111<hr>",

        }
    },
    {
        "fieldName": "epicWsjf",
        "fieldLabel": "Custom field for WSJF",
        "fieldType": "text",
        "fieldCategory": "fields",
        "section": "Custom Fields Mapping",
        "tooltip": {
            "definition": "JIRA/AZURE applications let you add custom fields in addition to the built-in fields.Provide value of WSJF value that is required to calculated Cost of delay <br />Example:customfield_11111<hr>",

        }
    },
    {
        "fieldName": "epicTimeCriticality",
        "fieldLabel": "Custom field for Time Criticality",
        "fieldType": "text",
        "fieldCategory": "fields",
        "section": "Custom Fields Mapping",
        "tooltip": {
            "definition": "JIRA/AZURE applications let you add custom fields in addition to the built-in fields.Provide value of Time Criticality value on Epic that is required to calculated Cost of delay .<br />Example:customfield_11111<hr>",

        }
    },
    {
        "fieldName": "epicJobSize",
        "fieldLabel": "Custom field for Job Size",
        "fieldType": "text",
        "fieldCategory": "fields",
        "section": "Custom Fields Mapping",
        "tooltip": {
            "definition": "JIRA/AZURE applications let you add custom fields in addition to the built-in fields.Provide value of Job size on EPIC that is required to calculated WSJF. <br>Example:customfield_11111<hr>",

        }
    },
    {
        "fieldName": "estimationCriteria",
        "fieldLabel": "Estimation Criteria",
        "fieldType": "radiobutton",
        "section": "Custom Fields Mapping",
        "tooltip": {
            "definition": "Estimation criteria for stories. <br> Example: Buffered Estimation."
        },
        "options": [{
                "label": "Story Point",
                "value": "Story Point"
            },
            {
                "label": "Actual (Original Estimation)",
                "value": "Actual Estimation"
            }
        ],
        "nestedFields": [{
            "fieldName": "storyPointToHourMapping",
            "fieldLabel": "Story Point to Hour Conversion",
            "fieldType": "text",
            "filterGroup": ["Story Point"],
            "tooltip": {
                "definition": "Estimation technique used by teams for e.g. story points, Hours etc."
            }
        }]
    },
    {
        "fieldName": "jiraIncludeBlockedStatusKPI131",
        "fieldLabel": "Status to identify Blocked issues",
        "fieldType": "radiobutton",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "The statuses that signify that team is unable to proceed on an issue due to internal or external dependency like On Hold, Waiting for user response, dependent work etc."
        },
        "options": [{
                "label": "Blocked Status",
                "value": "Blocked Status"
            },
            {
                "label": "Include Flagged Issue",
                "value": "Include Flagged Issue"
            }
        ],
        "nestedFields": [{
            "fieldName": "jiraBlockedStatusKPI131",
            "fieldLabel": "Status to Identify 'Blocked' status ",
            "fieldType": "chips",
            "filterGroup": ["Blocked Status"],
            "tooltip": {
                "definition": "Provide Status to Identify Blocked Issues."
            }
        }]
    },
    {
        "fieldName": "jiraBugRaisedByQAIdentification",
        "fieldLabel": "QA Defect Identification",
        "fieldType": "radiobutton",
        "section": "Defects Mapping",
        "tooltip": {
            "definition": "This field is used to identify if a defect is raised by QA<br>1. CustomField : If a separate custom field is used.<br>2. Labels : If a label is used to identify. Example: QA Defect <hr>"
        },
        "options": [{
                "label": "CustomField",
                "value": "CustomField"
            },
            {
                "label": "Labels",
                "value": "Labels"
            }
        ],
        "nestedFields": [{
                "fieldName": "jiraBugRaisedByQAValue",
                "fieldLabel": "QA Defect Values",
                "fieldType": "chips",
                "filterGroup": ["CustomField", "Labels"],
                "tooltip": {
                    "definition": "Provide label name to identify QA raised defects."
                }
            },
            {
                "fieldName": "jiraBugRaisedByQACustomField",
                "fieldLabel": "QA Defect Custom Field",
                "fieldType": "text",
                "fieldCategory": "fields",
                "filterGroup": ["CustomField"],
                "tooltip": {
                    "definition": "Provide customfield name to identify QA raised defects. <br>Example: customfield_13907"
                }
            }
        ]
    },
    {
        "fieldName": "jiraBugRaisedByIdentification",
        "fieldLabel": "UAT Defect Identification",
        "fieldType": "radiobutton",
        "section": "Defects Mapping",
        "tooltip": {
            "definition": "This field is used to identify if a defect is raised by third party or client:<br>1. CustomField : If a separate custom field is used<br>2. Labels : If a label is used to identify. Example: TECH_DEBT (This has to be one value).<hr>"
        },
        "options": [{
                "label": "CustomField",
                "value": "CustomField"
            },
            {
                "label": "Labels",
                "value": "Labels"
            }
        ],
        "nestedFields": [

            {
                "fieldName": "jiraBugRaisedByCustomField",
                "fieldLabel": "UAT Defect Custom Field",
                "fieldType": "text",
                "fieldCategory": "fields",
                "filterGroup": ["CustomField"],
                "tooltip": {
                    "definition": "Provide customfield name to identify UAT or client raised defects. <br> Example: customfield_13907<hr>"
                }
            },
            {
                "fieldName": "jiraBugRaisedByValue",
                "fieldLabel": "UAT Defect Values",
                "fieldType": "chips",
                "filterGroup": ["CustomField", "Labels"],
                "tooltip": {
                    "definition": "Provide label name to identify UAT or client raised defects.<br /> Example: Clone_by_QA <hr>"
                }
            }
        ]
    },
    {
        "fieldName": "additionalFilterConfig",
        "fieldLabel": "Filter that can be applied on a Project",
        "section": "Additional Filter Identifier",
        "fieldType": "dropdown",
        "tooltip": {
            "definition": "This field is used to identify Additional Filters. <br> Example: SQUAD<br>",

        }
    },
    {
        "fieldName": "issueStatusExcluMissingWorkKPI124",
        "fieldLabel": "Status to be excluded",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses of an issue that should be ignored for checking the logged work",
        }
    }, {
        "fieldName": "jiraStatusForInProgressKPI145",
        "fieldLabel": "Status to identify In Progress issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses that issues have moved from the Created status and also has not been completed",
        }
    },
    {
        "fieldName": "jiraStatusForInProgressKPI122",
        "fieldLabel": "Status to identify In Progress issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses that issues have moved from the Created status and also has not been completed",
        }
    }, {
        "fieldName": "jiraStatusForInProgressKPI125",
        "fieldLabel": "Status to identify In Progress issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses that issues have moved from the Created status and also has not been completed",
        }
    }, {
        "fieldName": "jiraStatusForInProgressKPI123",
        "fieldLabel": "Status to identify In Progress issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses that issues have moved from the Created status and also has not been completed",
        }
    }, {
        "fieldName": "jiraStatusForInProgressKPI119",
        "fieldLabel": "Status to identify In Progress issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses that issues have moved from the Created status and also has not been completed",
        }
    }, {
        "fieldName": "jiraStatusForInProgressKPI128",
        "fieldLabel": "Status to identify In Progress issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses that issues have moved from the Created status and also has not been completed",
        }
    }, {
        "fieldName": "jiraStatusForInProgressKPI148",
        "fieldLabel": "Status to identify In Progress issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses that issues have moved from the Created status and also has not been completed",
        }
    }, {
        "fieldName": "jiraDevDoneStatusKPI119",
        "fieldLabel": "Status to identify Dev completed issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Status that confirms that the development work is completed and an issue can be passed on for testing",
        }
    }, {
        "fieldName": "jiraDevDoneStatusKPI145",
        "fieldLabel": "Status to identify Dev completion",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Status that confirms that the development work is completed and an issue can be passed on for testing",
        }
    }, {
        "fieldName": "jiraDevDoneStatusKPI128",
        "fieldLabel": "Status to identify Dev completed issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Status that confirms that the development work is completed and an issue can be passed on for testing",
        }
    }, {
        "fieldName": "jiraDefectCountlIssueTypeKPI28",
        "fieldLabel": "Issue types which will have linked defects",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "Issue types that are considered as defects in Jira.",
        }
    }, {
        "fieldName": "jiraDefectCountlIssueTypeKPI36",
        "fieldLabel": "Issue types which will have linked defects",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "Issue types that are considered as defects in Jira.",
        }
    }, {
        "fieldName": "jiraStatusForQaKPI135",
        "fieldLabel": "Status to Identify In Testing Status",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "The status of Defect Issue Type which identifies the 'In-Testing' status in JIRA. <br> Example: Ready For Testing<hr>",

        }
    }, {
        "fieldName": "jiraStatusForQaKPI82",
        "fieldLabel": "Status to Identify In Testing Status",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "The status of Defect Issue Type which identifies the 'In-Testing' status in JIRA. <br> Example: Ready For Testing<hr>",

        }
    }, {
        "fieldName": "jiraStatusForQaKPI148",
        "fieldLabel": "Status to Identify In Testing Status",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "The status of Defect Issue Type which identifies the 'In-Testing' status in JIRA. <br> Example: Ready For Testing<hr>",

        }
    },
    {
        "fieldName": "storyFirstStatusKPI3",
        "fieldLabel": "Status when 'Story' issue type is created",
        "fieldType": "text",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All issue types that identify with a Story.",

        }
    },
    {
        "fieldName": "storyFirstStatusKPI148",
        "fieldLabel": "Status when 'Story' issue type is created",
        "fieldType": "text",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All issue types that identify with a Story.",

        }
    },
    {
        "fieldName": "jiraFtprRejectStatusKPI135",
        "fieldLabel": "FTPR Rejection Status ",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "This status depicts the stories which have not passed QA. FTP stories can also be identified by a return transition but if status is mentioned that will be considered."
        }
    },
    {
        "fieldName": "jiraFtprRejectStatusKPI82",
        "fieldLabel": "FTPR Rejection Status ",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "This status depicts the stories which have not passed QA. FTP stories can also be identified by a return transition but if status is mentioned that will be considered."
        }
    },
    {
        "fieldName": "jiraDefectClosedStatusKPI137",
        "fieldLabel": "Status to identify Closed Bugs",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "This field should consider all status that are considered Closed in Jira for e.g. Closed, Done etc."
        }
    },
    {
        "fieldName": "jiraAcceptedInRefinementKPI139",
        "fieldLabel": "Accepted in Refinement",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": " Status that Defines Jira Issue is that is Accepted in Refinement."
        }
    },
    {
        "fieldName": "jiraReadyForRefinementKPI139",
        "fieldLabel": "Ready For Refinement",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Status that Defines Jira Issue is Ready for Refinement."
        }
    },
    {
        "fieldName": "jiraRejectedInRefinementKPI139",
        "fieldLabel": "Rejected in Refinement",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Status that Defines Jira Issue is Rejected In Refinement."
        }
    },
    {
        "fieldName": "readyForDevelopmentStatusKPI138",
        "fieldLabel": "Status to identify issues Ready for Development ",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Status to identify Ready for development from the backlog.",
        }
    },
    {
    "fieldName": "jiraDueDateField",
    "fieldLabel": "Due Date",
    "fieldType": "radiobutton",
    "section": "Custom Fields Mapping",
    "tooltip": {
        "definition": "This field is to track due date of issues tagged in the iteration"
    },
    "options": [{
            "label": "Custom Field",
            "value": "CustomField"
        },
        {
            "label": "Due Date",
            "value": "Due Date"
        }
    ],
    "nestedFields": [

        {
            "fieldName": "jiraDueDateCustomField",
            "fieldLabel": "Due Date Custom Field",
            "fieldType": "text",
            "fieldCategory": "fields",
            "filterGroup": ["CustomField"],
            "tooltip": {
                "definition": "This field is to track due date of issues tagged in the iteration."
            }
        }
    ]
    }, {
     "fieldName": "jiraDevDueDateField",
     "fieldLabel": "Dev Due Date",
     "fieldType": "radiobutton",
     "section": "Custom Fields Mapping",
     "tooltip": {
       "definition": "This field is to track dev due date of issues tagged in the iteration."
     },
     "options": [
       {
         "label": "Custom Field",
         "value": "CustomField"
       },
       {
         "label": "Due Date",
         "value": "Due Date"
       }
     ],
     "nestedFields": [
       {
         "fieldName": "jiraDevDueDateCustomField",
         "fieldLabel": "Dev Due Date Custom Field",
         "fieldType": "text",
         "fieldCategory": "fields",
         "filterGroup": [
           "CustomField"
         ],
         "tooltip": {
           "definition": "This field is to track dev due date of issues tagged in the iteration."
         }
       }
     ]
   }, {
    "fieldName": "jiraIssueEpicType",
    "fieldLabel": "Epic Issue Type",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Issue Types Mapping",
    "tooltip": {
        "definition": "This field is used to identify Epic Issue type.",
    }
}, {
    "fieldName": "rootCause",
    "fieldLabel": "Root Cause",
    "fieldType": "text",
    "fieldCategory": "fields",
    "section": "Custom Fields Mapping",
    "tooltip": {
        "definition": "JIRA/AZURE applications let you add custom fields in addition to the built-in fields. Root Cause is a custom field in JIRA. So User need to provide that custom field which is associated with Root Cause in Users JIRA Installation.",
    }
}, {
    "fieldName": "storyFirstStatus",
    "fieldLabel": "Story First Status",
    "fieldType": "text",
    "fieldCategory": "workflow",
    "section": "WorkFlow Status Mapping",
    "tooltip": {
        "definition": "Default status when a Story is opened.",
    }
}, {
    "fieldName": "jiraTestAutomationIssueType",
    "fieldLabel": "In Sprint Automation - Issue Types with Linked Defect ",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Issue Types Mapping",
    "tooltip": {
        "definition": "",
    }
}, {
    "fieldName": "jiraDefectDroppedStatusKPI127",
    "fieldLabel": "Defect Dropped Status",
    "fieldType": "chips",
    "fieldCategory": "workflow",
    "section": "WorkFlow Status Mapping",
    "tooltip": {
        "definition": "All statuses with which defect is linked.",
    }
}, {
    "fieldName": "productionDefectIdentifier",
    "fieldLabel": "Production defects identification",
    "fieldType": "radiobutton",
    "section": "Defects Mapping",
    "tooltip": {
        "definition": "This field is used to identify if a defect is raised by Production. 1. CustomField : If a separate custom field is used, 2. Labels : If a label is used to identify, 3. Component : If a Component is used to identify"
    },
    "options": [{
            "label": "CustomField",
            "value": "CustomField"
        },
        {
            "label": "Labels",
            "value": "Labels"
        },
        {
            "label": "Component",
            "value": "Component"
        }
    ],
    "nestedFields": [

        {
            "fieldName": "productionDefectCustomField",
            "fieldLabel": "Production Defect CustomField",
            "fieldType": "text",
            "fieldCategory": "fields",
            "filterGroup": ["CustomField"],
            "tooltip": {
                "definition": " Provide customfield name to identify Production raised defects."
            }
        },
        {
            "fieldName": "productionDefectValue",
            "fieldLabel": "Production Defect Values",
            "fieldType": "chips",
            "filterGroup": ["CustomField", "Labels"],
            "tooltip": {
                "definition": "Provide label name to identify Production raised defects."
            }
        },
        {
            "fieldName": "productionDefectComponentValue",
            "fieldLabel": "Component",
            "fieldType": "text",
            "filterGroup": ["Component"],
            "tooltip": {
                "definition": "Provide label name to identify Production raised defects."
            }
        }
    ]
}, {
    "fieldName": "ticketCountIssueType",
    "fieldLabel": "Ticket Count Issue Type",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Issue Types Mapping",
    "tooltip": {
        "definition": "",

    }
}, {
    "fieldName": "kanbanRCACountIssueType",
    "fieldLabel": "Ticket RCA Count Issue Type",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Issue Types Mapping",
    "tooltip": {
        "definition": "",


    }
}, {
    "fieldName": "jiraTicketVelocityIssueType",
    "fieldLabel": "Ticket Velocity Issue Type",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Issue Types Mapping",
    "tooltip": {
        "definition": "",


    }
}, {
    "fieldName": "kanbanCycleTimeIssueType",
    "fieldLabel": "Kanban Lead Time Issue Type",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Issue Types Mapping",
    "tooltip": {
        "definition": "",


    }
}, {
    "fieldName": "ticketDeliverdStatus",
    "fieldLabel": "Ticket Delivered Status",
    "fieldType": "chips",
    "fieldCategory": "workflow",
    "section": "WorkFlow Status Mapping",
    "tooltip": {
        "definition": "Status from workflow on which ticket is considered as delivered."
    }
}, {
    "fieldName": "jiraTicketClosedStatus",
    "fieldLabel": "Ticket Closed Status",
    "fieldType": "chips",
    "fieldCategory": "workflow",
    "section": "WorkFlow Status Mapping",
    "tooltip": {
        "definition": "Status from workflow on which ticket is considered as Resolved."
    }
}, {
    "fieldName": "jiraTicketTriagedStatus",
    "fieldLabel": "Ticket Triaged Status",
    "fieldType": "chips",
    "fieldCategory": "workflow",
    "section": "WorkFlow Status Mapping",
    "tooltip": {
        "definition": "Status from workflow on which ticket is considered as Triaged."
    }
}, {
    "fieldName": "jiraTicketRejectedStatus",
    "fieldLabel": "Ticket Rejected/Dropped Status",
    "fieldType": "chips",
    "fieldCategory": "workflow",
    "section": "WorkFlow Status Mapping",
    "tooltip": {
        "definition": "Status from workflow on which ticket is considered as Rejected/Dropped."
    }
  },
  {
    "fieldName": "jiraDodKPI37",
    "fieldLabel": "Status to identify completed issues",
    "fieldType": "chips",
    "fieldCategory": "workflow",
    "section": "WorkFlow Status Mapping",
    "tooltip": {
        "definition": "Status/es that identify that an issue is completed based on Definition of Done (DoD)"
    }
   },
   {
    "fieldName": "sprintName",
    "fieldLabel": "Sprint Name",
    "fieldType": "text",
    "fieldCategory": "fields",
    "section": "Custom Fields Mapping",
    "tooltip": {
        "definition": "JIRA applications let you add custom fields in addition to the built-in fields. Sprint name is a custom field in JIRA. So User need to provide that custom field which is associated with Sprint in Users JIRA Installation."
    }
},{
        "fieldName": "notificationEnabler",
        "fieldLabel": "Processor Failure Notification",
        "fieldType": "radiobutton",
        "section": "Custom Fields Mapping",
        "tooltip": {
             "definition": "On/Off notification in case processor failure."
        },
        "options": [{
             "label": "On",
             "value": true
        },
        {
             "label": "Off",
             "value": false
        }
        ]
    },
    {
        "fieldName": "epicPlannedValue",
        "fieldLabel": "Custom field for Epic Planned Value",
        "fieldType": "text",
        "fieldCategory": "fields",
        "section": "Custom Fields Mapping",
        "tooltip": {
            "definition": "JIRA applications let you add custom fields in addition to the built-in fields. Provide value of Planned Value for Epics that need to show on Trend line. <br> Example:customfield_11111 <hr>",
    }
    },
    {
        "fieldName": "epicAchievedValue",
        "fieldLabel": "Custom field for Epic Achieved Value",
        "fieldType": "text",
        "fieldCategory": "fields",
        "section": "Custom Fields Mapping",
        "tooltip": {
            "definition": "JIRA applications let you add custom fields in addition to the built-in fields. Provide value of Achieved Value for Epics that need to show on Trend line. <br> Example:customfield_11111 <hr>",
    }
    },
    {
    "fieldName": "jiraIssueEpicTypeKPI153",
    "fieldLabel": "Epic Issue Type",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Issue Types Mapping",
    "tooltip": {
        "definition": "This field is used to identify Epic Issue type.",
    }
    },
    {
        "fieldName": "jiraItrQSIssueTypeKPI133",
        "fieldLabel": "Issue types which will have linked defects",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "Consider issue types which have defects tagged to them"
        }
    },
    {
        "fieldName": "epicLink",
        "fieldLabel": "Custom field for Epic Link",
        "fieldType": "text",
        "fieldCategory": "fields",
        "section": "Custom Fields Mapping",
        "tooltip": {
            "definition": "JIRA applications let you add custom fields in addition to the built-in fields.Provide value of Epic Linkage to the story/defect<br />Example:customfield_11111<hr>"
        }
    },
    {
      "fieldName": "jiraSubTaskDefectType",
      "fieldLabel": "Issue type for sub-task defect",
      "fieldType": "chips",
      "fieldCategory": "Issue_Type",
      "section": "Issue Types Mapping",
      "tooltip": {
           "definition": "Any issue type mentioned will be considered as sub-task bug on Release dashboard"
      }
    },
    {
        "fieldName": "jiraStatusStartDevelopmentKPI154",
        "fieldLabel": "Status to identify start of development",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Status from workflow on which issue is started development. <br> Example: In Analysis<hr>"
        }
    },
    {
        "fieldName": "jiraDevDoneStatusKPI154",
        "fieldLabel": "Status to identify Dev completed issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Status that confirms that the development work is completed and an issue can be passed on for testing",
        }
    },
    {
        "fieldName": "jiraQADoneStatusKPI154",
        "fieldLabel": "Status to identify QA completed issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Status that confirms that the QA work is completed and an issue can be ready for signoff/close",
        }
    },
    {
        "fieldName": "jiraIterationCompletionStatusKPI154",
        "fieldLabel": "Status to identify completed issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
        }
    },
    {
        "fieldName": "jiraStatusForInProgressKPI154",
        "fieldLabel": "Status to identify In Progress issues",
        "section": "WorkFlow Status Mapping",
        "fieldType": "chips",
        "readOnly": true,
        "tooltip": {
            "definition": "All statuses that issues have moved from the Created status and also has not been completed. <br> This field is same as the configuration field of Work Remaining KPI",
        }
    },
    {
       "fieldName": "jiraSubTaskIdentification",
       "fieldLabel": "Sub-Task Issue Types",
       "fieldType": "chips",
       "fieldCategory": "Issue_Type",
       "section": "Issue Types Mapping",
       "tooltip": {
       "definition": "Any issue type mentioned will be considered as sub-task linked with story"
       }
    },
    {
        "fieldName": "storyFirstStatusKPI154",
        "fieldLabel": "Status when 'Story' issue type is created",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All status that identify open statuses.",

        }
    },
    {
        "fieldName": "jiraOnHoldStatusKPI154",
        "fieldLabel": "Status when issue type is put on Hold",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "All status that identify hold/blocked statuses.",

        }
    },
    {
       "fieldName":"jiraDefectRejectionStatusKPI155",
       "fieldLabel":"Ticket Rejected/Dropped Status",
       "fieldType":"text",
       "fieldCategory":"workflow",
       "section":"WorkFlow Status Mapping",
       "tooltip":{
          "definition":"Status from workflow on which ticket is considered as Rejected/Dropped."
       }
    },
    {
       "fieldName":"jiraDodKPI155",
       "fieldLabel":"DOD Status",
       "fieldType":"chips",
       "fieldCategory":"workflow",
       "section":"WorkFlow Status Mapping",
       "tooltip":{
          "definition":"Status/es that identify that an issue is completed based on Definition of Done (DoD)."
       }
    },
    {
       "fieldName":"jiraLiveStatusKPI155",
       "fieldLabel":"Status to identify Live status",
       "fieldType":"text",
       "fieldCategory":"workflow",
       "section":"WorkFlow Status Mapping",
       "tooltip":{
          "definition":"Provide any status from workflow on which Live is considered."
       }
    },
    {
    	"fieldName": "uploadDataKPI42",
    	"fieldLabel": "KPI calculation logic",
    	"fieldType": "toggle",
    	"toggleLabelRight": "Upload Data",
    	"section": "WorkFlow Status Mapping",
    	"processorCommon": false,
    	"tooltip": {
    		"definition": "Enabled State (Kpi from data on Upload data screen)"
    	}
    },
    {
    	"fieldName": "uploadDataKPI16",
    	"fieldLabel": "KPI calculation logic",
    	"fieldType": "toggle",
    	"toggleLabelRight": "Upload Data",
    	"section": "WorkFlow Status Mapping",
    	"processorCommon": false,
    	"tooltip": {
    		"definition": "Enabled State (Kpi from data on Upload data screen)"
    	}
    },
    {
		"fieldName": "jiraStoryIdentificationKPI164",
		"fieldLabel": "Issue type to identify Story",
		"fieldType": "chips",
		"fieldCategory": "Issue_Type",
		"section": "Issue Types Mapping",
		"tooltip": {
			"definition": "All issue types that are used as/equivalent to Story.",

		}
	},
{
    "fieldName": "jiraIssueTypeNamesKPI161",
    "fieldLabel": "Issue types to be included",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "processorCommon": false,
    "section": "Issue Types Mapping",
    "tooltip": {
        "definition": "All the issue types used by a project in Jira."
    }
},
{
    "fieldName": "jiraIssueTypeNamesKPI146",
    "fieldLabel": "Issue types to be included",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "processorCommon": false,
    "section": "Issue Types Mapping",
    "tooltip": {
        "definition": "All the issue types used by a project in Jira."
    }
},
{
    "fieldName": "jiraIssueTypeNamesKPI148",
    "fieldLabel": "Issue types to be included",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "processorCommon": false,
    "section": "Issue Types Mapping",
    "tooltip": {
        "definition": "All the issue types used by a project in Jira."
    }
},
{
    "fieldName": "jiraIssueTypeNamesKPI151",
    "fieldLabel": "Issue types to be included",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "processorCommon": false,
    "section": "Issue Types Mapping",
    "tooltip": {
        "definition": "All the issue types used by a project in Jira."
    }
},
{
    "fieldName": "jiraIssueTypeNamesKPI152",
    "fieldLabel": "Issue types to be included",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "processorCommon": false,
    "section": "Issue Types Mapping",
    "tooltip": {
        "definition": "All the issue types used by a project in Jira."
    }
},
{
            "fieldName": "leadTimeConfigRepoTool",
            "fieldLabel": "Lead Time KPI calculation logic",
            "fieldType": "radiobutton",
            "section": "Custom Fields Mapping",
            "tooltip": {
                "definition": "By Default State Calculation is based on Jira Issues and Releases. <br>  1. Jira : Calculation is based on Jira Issues and Releases, <br>  2. Repo : Calculation is based on Repo Data and Releases. <br> Branch Name Must have Jira Issue Key in it.",
            },
            "options": [{
                    "label": "Jira",
                    "value": "Jira"
                },
                {
                    "label": "Repo",
                    "value": "Repo"
                }
            ],
            "nestedFields": [
{
	"fieldName": "jiraIssueTypeKPI156",
	"fieldLabel": "Issue type to be included",
	"fieldType": "chips",
	"fieldCategory": "Issue_Type",
	 "filterGroup": ["Jira"],
	"tooltip": {
		"definition": "Only these Issue Types will be considered for Lead Time Calculation. If this Configuration is not provided, all the Issue Types will be considered. <br> Example: Story, Enabler Story, Tech Story, Change request <hr>."
	}
}, {
	"fieldName": "jiraDodKPI156",
	"fieldLabel": "Status to identify DOD",
	"fieldType": "chips",
	"fieldCategory": "workflow",
	"filterGroup": ["Jira"],
	"tooltip": {
		"definition": " Definition of Doneness. Provide any status from workflow on which DOD is considered. Difference between the latest date of theses statuses and release end date will be considered as the Lead Time. <br> <br> <b>Note:</b> This configuration will be ignored if Lead Time KPI calculation logic is set to Repo Data. <br> <br> <b>Note:</b> This configuration will be ignored if Issue Type is not provided.br> Example: Closed,Done. <hr> "
	}
},{
	"fieldName": "toBranchForMRKPI156",
	"fieldLabel": "Production Branch Name",
	"fieldType": "text",
	"filterGroup": ["Repo"],
	"tooltip": {
		"definition": "Production Branch in Which all the Child Branches are Merged <br> eg. master <hr>"
	}
}]
},
{
  "fieldName":"testingPhaseDefectsIdentifier",
  "fieldLabel":"Testing phase defects identification",
  "fieldType":"radiobutton",
  "section":"Defects Mapping",
  "tooltip":{
    "definition":"This field is used to identify a defect in which phase it is raised. 1. CustomField : If a separate custom field is used, 2. Labels : If a label is used to identify, 3. Component : If a Component is used to identify"
  },
  "options":[
    {
      "label":"CustomField",
      "value":"CustomField"
    },
    {
      "label":"Labels",
      "value":"Labels"
    },
    {
      "label":"Component",
      "value":"Component"
    }
  ],
  "nestedFields":[
    {
      "fieldName":"testingPhaseDefectCustomField",
      "fieldLabel":"Testing Phase Defect CustomField",
      "fieldType":"text",
      "fieldCategory":"fields",
      "filterGroup":[
        "CustomField"
      ],
      "tooltip":{
        "definition":" Provide customfield name to identify testing phase defects."
      }
    },
    {
      "fieldName":"testingPhaseDefectValue",
      "fieldLabel":"Testing Phase Defect Values",
      "fieldType":"chips",
      "filterGroup":[
        "CustomField",
        "Labels"
      ],
      "tooltip":{
        "definition":"Provide label name to identify testing phase defects."
      }
    },
    {
      "fieldName":"testingPhaseDefectComponentValue",
      "fieldLabel":"Component",
      "fieldType":"text",
      "filterGroup":[
        "Component"
      ],
      "tooltip":{
        "definition":"Provide label name to identify testing phase defects."
      }
    }
  ]
},
{
  "fieldName":"jiraDodKPI163",
  "fieldLabel":"DOD Status",
  "fieldType":"chips",
  "fieldCategory":"workflow",
  "section":"WorkFlow Status Mapping",
  "tooltip":{
    "definition":"Status/es that identify that an issue is completed based on Definition of Done (DoD)."
  }
},
{
  "fieldName": "startDateCountKPI150",
  "fieldLabel": "Count of days from the release start date to calculate closure rate for prediction",
  "fieldType": "number",
  "section": "Issue Types Mapping",
  "tooltip": {
    "definition": "If this field is kept blank, then daily closure rate of issues is calculated based on the number of working days between today and the release start date or date when first issue was added. This configuration allows you to decide from which date the closure rate should be calculated."
  }
},
    {
        "fieldName": "thresholdValueKPI14",
        "fieldLabel": "Target KPI Value",
        "fieldType": "number",
        "section": "Custom Fields Mapping",
        "tooltip": {
            "definition": "Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"
        }
    },
    {
        "fieldName": "thresholdValueKPI82",
        "fieldLabel": "Target KPI Value",
        "fieldType": "number",
        "section": "Custom Fields Mapping",
        "tooltip": {
            "definition": "Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"
        }
    },
    {
        "fieldName": "thresholdValueKPI111",
        "fieldLabel": "Target KPI Value",
        "fieldType": "number",
        "section": "Custom Fields Mapping",
        "tooltip": {
            "definition": "Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"
        }
    },
    {
        "fieldName": "thresholdValueKPI35",
        "fieldLabel": "Target KPI Value",
        "fieldType": "number",
        "section": "Custom Fields Mapping",
        "tooltip": {
            "definition": "Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"
        }
    },
    {
        "fieldName": "thresholdValueKPI34",
        "fieldLabel": "Target KPI Value",
        "fieldType": "number",
        "section": "Custom Fields Mapping",
        "tooltip": {
            "definition": "Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"
        }
    },
    {
        "fieldName": "thresholdValueKPI37",
        "fieldLabel": "Target KPI Value",
        "fieldType": "number",
        "section": "Custom Fields Mapping",
        "tooltip": {
            "definition": "Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"
        }
    },
    {
        "fieldName": "thresholdValueKPI28",
        "fieldLabel": "Target KPI Value",
        "fieldType": "number",
        "section": "Custom Fields Mapping",
        "tooltip": {
            "definition": "Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"
        }
    },
    {
        "fieldName": "thresholdValueKPI36",
        "fieldLabel": "Target KPI Value",
        "fieldType": "number",
        "section": "Custom Fields Mapping",
        "tooltip": {
           "definition": "Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"
        }
    },
    {
        "fieldName": "thresholdValueKPI16",
        "fieldLabel": "Target KPI Value",
        "fieldType": "number",
        "section": "Custom Fields Mapping",
        "tooltip": {
            "definition": "Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"
        }
    },
    {
        "fieldName": "thresholdValueKPI17",
        "fieldLabel": "Target KPI Value",
        "fieldType": "number",
        "section": "Custom Fields Mapping",
        "tooltip": {
            "definition": "Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"
        }
    },
    {
        "fieldName": "thresholdValueKPI38",
        "fieldLabel": "Target KPI Value",
        "fieldType": "number",
        "section": "Custom Fields Mapping",
        "tooltip": {
            "definition": "Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"
        }
    },
    {
        "fieldName": "thresholdValueKPI27",
        "fieldLabel": "Target KPI Value",
        "fieldType": "number",
        "section": "Custom Fields Mapping",
        "tooltip": {
           "definition": "Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"
        }
    },
    {
        "fieldName": "thresholdValueKPI72",
        "fieldLabel": "Target KPI Value",
        "fieldType": "number",
        "section": "Custom Fields Mapping",
        "tooltip": {
            "definition": "Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"
        }
    },
    {
        "fieldName": "thresholdValueKPI84",
        "fieldLabel": "Target KPI Value",
        "fieldType": "number",
        "section": "Custom Fields Mapping",
        "tooltip": {
            "definition": "Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"
        }
    },
    {
        "fieldName": "thresholdValueKPI11",
        "fieldLabel": "Target KPI Value",
        "fieldType": "number",
        "section": "Custom Fields Mapping",
        "tooltip": {
            "definition": "Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"
        }
    },
    {
        "fieldName": "thresholdValueKPI62",
        "fieldLabel": "Target KPI Value",
        "fieldType": "number",
        "section": "Custom Fields Mapping",
        "tooltip": {
            "definition": "Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"
        }
    },
    {
        "fieldName": "thresholdValueKPI64",
        "fieldLabel": "Target KPI Value",
        "fieldType": "number",
        "section": "Custom Fields Mapping",
        "tooltip": {
            "definition": "Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"
        }
    },
    {
        "fieldName": "thresholdValueKPI67",
        "fieldLabel": "Target KPI Value",
        "fieldType": "number",
        "section": "Custom Fields Mapping",
        "tooltip": {
            "definition": "Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"
        }
    },
    {
        "fieldName": "thresholdValueKPI65",
        "fieldLabel": "Target KPI Value",
        "fieldType": "number",
        "section": "Custom Fields Mapping",
        "tooltip": {
            "definition": "Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"
        }
    },
    {
        "fieldName": "thresholdValueKPI157",
        "fieldLabel": "Target KPI Value",
        "fieldType": "number",
        "section": "Custom Fields Mapping",
        "tooltip": {
            "definition": "Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"
        }
    },
    {
        "fieldName": "thresholdValueKPI158",
        "fieldLabel": "Target KPI Value",
        "fieldType": "number",
        "section": "Custom Fields Mapping",
        "tooltip": {
            "definition": "Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"
        }
    },
    {
        "fieldName": "thresholdValueKPI159",
        "fieldLabel": "Target KPI Value",
        "fieldType": "number",
        "section": "Custom Fields Mapping",
        "tooltip": {
            "definition": "Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"
        }
    },
    {
        "fieldName": "thresholdValueKPI160",
        "fieldLabel": "Target KPI Value",
        "fieldType": "number",
        "section": "Custom Fields Mapping",
        "tooltip": {
            "definition": "Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"
        }
    },
    {
        "fieldName": "thresholdValueKPI164",
        "fieldLabel": "Target KPI Value",
        "fieldType": "number",
        "section": "Custom Fields Mapping",
        "tooltip": {
            "definition": "Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"
        }
    },
    {
        "fieldName": "thresholdValueKPI3",
        "fieldLabel": "Target KPI Value",
        "fieldType": "number",
        "section": "Custom Fields Mapping",
        "tooltip": {
            "definition": "Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"
        }
    },
{
    "fieldName": "jiraStoryIdentificationKPI166",
    "fieldLabel": "Issue type to identify Production incidents",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Issue Types Mapping",
    "tooltip": {
        "definition": "All issue types that are used as/equivalent to Production incidents.",

    }
},
{
    "fieldName": "jiraProductionIncidentIdentification",
    "fieldLabel": "Production incidents identification",
    "fieldType": "radiobutton",
    "section": "Defects Mapping",
    "tooltip": {
        "definition": "This field is used to identify if a production incident is raised by third party or client:<br>1. CustomField : If a separate custom field is used<br>2. Labels : If a label is used to identify. Example: PROD_DEFECT (This has to be one value).<hr>"
    },
    "options": [{
        "label": "CustomField",
        "value": "CustomField"
    },
    {
        "label": "Labels",
        "value": "Labels"
    }
    ],
    "nestedFields": [

        {
            "fieldName": "jiraProdIncidentRaisedByCustomField",
            "fieldLabel": "Production Incident Custom Field",
            "fieldType": "text",
            "fieldCategory": "fields",
            "filterGroup": ["CustomField"],
            "tooltip": {
                "definition": "Provide customfield name to identify Production Incident. <br> Example: customfield_13907<hr>"
            }
        },
        {
            "fieldName": "jiraProdIncidentRaisedByValue",
            "fieldLabel": "Production Incident Values",
            "fieldType": "chips",
            "filterGroup": ["CustomField", "Labels"],
            "tooltip": {
                "definition": "Provide label name to identify Production IncidentProduction IncideProd_Incidentxample: Clone_by_QA <hr>"
            }
        }
    ]
},
{
    "fieldName": "jiraDodKPI166",
    "fieldLabel": "DOD Status",
    "fieldType": "chips",
    "fieldCategory": "workflow",
    "section": "WorkFlow Status Mapping",
    "tooltip": {
        "definition": "Status/es that identify that an issue is completed based on Definition of Done (DoD)."
    }
},
{
    "fieldName": "jiraIssueClosedStateKPI170",
    "fieldLabel": "Status to identify Close Statuses",
    "fieldCategory": "workflow",
    "fieldType": "chips",
    "section": "WorkFlow Status Mapping",
    "tooltip": {
        "definition": "All statuses that signify an issue is 'DONE' based on 'Definition Of Done'"
    }
    },
{
    "fieldName": "jiraIssueWaitStateKPI170",
    "fieldLabel": "Status to identify Wait Statuses",
    "fieldCategory": "workflow",
    "fieldType": "chips",
    "section": "WorkFlow Status Mapping",
    "tooltip": {
        "definition": "The statuses wherein no activity takes place and signifies that the issue is in the queue"
    }
},
{
 "fieldName": "populateByDevDoneKPI150",
 "fieldLabel": "Prediction logic",
 "fieldType": "toggle",
 "toggleLabelLeft" : "Overall completion",
 "toggleLabelRight": "Dev Completion",
 "section": "WorkFlow Status Mapping",
 "processorCommon": false,
 "tooltip": {
   "definition": "Enabled State (Kpi will populate w.r.t Dev complete date)"
 }
},
{
 "fieldName": "jiraDevDoneStatusKPI150",
 "fieldLabel": "Status to identify Dev completed issues",
 "fieldType": "chips",
 "fieldCategory": "workflow",
 "section": "WorkFlow Status Mapping",
 "tooltip": {
   "definition": "Status that confirms that the development work is completed and an issue can be passed on for testing",
 }
},
{
    "fieldName" : "jiraLabelsKPI135",
    "fieldLabel" : "Labels to identify issues to be included",
    "fieldType" : "chips",
    "section" : "WorkFlow Status Mapping",
    "tooltip" : {
      "definition" : "Calculation should only those issues which have defined labels tagged."
    }
},
{
     "fieldName": "jiraLiveStatusKPI171",
     "fieldLabel": "Live Status - Cycle Time",
     "fieldCategory": "workflow",
     "fieldType": "chips",
     "section": "WorkFlow Status Mapping",
     "tooltip": {
       "definition": "Status/es that identify that an issue is LIVE in Production"
     }
   },
   {
     "fieldName": "jiraIssueTypeKPI171",
     "fieldLabel": "Issue type to be included",
     "fieldCategory": "Issue_Type",
     "fieldType": "chips",
     "section": "Issue Types Mapping",
     "tooltip": {
       "definition": "All issue types that should be included in Lead time calculation."
     }
   }

]);
