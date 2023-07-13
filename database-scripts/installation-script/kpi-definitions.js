
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
            "No. of defects tagged to stories in the iteration that are fixed",
            "Total no. of defects tagged to stories in a iteration"
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
            "Total no. of defects reported in a sprint"
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
    "maxValue": "100",
    "kpiUnit": "%",
    "isDeleted": "False",
    "defaultOrder": 15,
    "kpiSource": "Jenkins",
    "groupId": 1,
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
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Percentage",
    "isPositiveTrend": false,
    "showTrend": true,
    "kpiFilter": "dropDown",
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "hideOverallFilter": true,
    "maturityRange": [
      "-50",
      "50-30",
      "30-20",
      "20-10",
      "10-"
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
    "chartType": "grouped_column_plus_line",
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
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Count(Hours)",
    "isPositiveTrend": false,
    "showTrend": true,
    "kpiFilter": "dropDown",
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "hideOverallFilter": true,
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
    "maxValue": "100",
    "kpiUnit": "Number",
    "isDeleted": "False",
    "defaultOrder": 25,
    "kpiSource": "Jenkins",
    "groupId": 1,
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
    "xAxisLabel": "Months",
    "yAxisLabel": "Count",
    "isPositiveTrend": true,
    "showTrend": true,
    "kpiFilter": "multiSelectDropDown",
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "maturityRange": [
      "-1",
      "1-2",
      "2-5",
      "5-10",
      "10-"
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
    "defaultOrder": 1,
    "kpiCategory": "Backlog",
    "kpiSource": "Jira",
    "groupId": 10,
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
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi127",
    "kpiName": "Production Defects Ageing",
    "kpiUnit": "Number",
    "isDeleted": "False",
    "defaultOrder": 3,
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
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi139",
    "kpiName": "Refinement Rejection Rate",
    "kpiUnit": "%",
    "isDeleted": "False",
    "defaultOrder": 4,
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
    "defaultOrder": 2,
    "kpiCategory": "Backlog",
    "kpiSource": "Jira",
    "groupId": 10,
    "thresholdValue": "",
    "kanban": false,
    "kpiInfo": {
      "definition": "It shows number of defects reopened in a given span of time in comparison to the total defects raised. For all the reopened defects, the average time to reopen is also available."
    },
    "isPositiveTrend": false,
    "kpiFilter": "dropdown",
    "showTrend": false,
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": true,
    "hideOverallFilter": true,
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
    "kpiFilter": "",
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
    "kpiFilter": "",
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
    "kpiFilter": "",
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
    "isPositiveTrend": true,
    "showTrend": false,
    "isAdditionalFilterSupport": false,
    "kpiFilter": "multiSelectDropDown",
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
    "defaultOrder": 5,
    "kpiCategory": "Backlog",
    "kpiSource": "Jira",
    "groupId": 10,
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
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi3",
    "kpiName": "Lead Time",
    "isDeleted": "False",
    "kpiCategory": "Backlog",
    "boxType": "2_column",
    "kpiBaseLine": "0",
    "thresholdValue": "",
    "defaultOrder": 6,
    "kpiUnit": "Count",
    "kpiSource": "Jira",
    "groupId": 10,
    "kanban": false,
    "chartType": "",
    "kpiInfo": {
      "definition": "Measures Total time between a request was made and  all work on this item is completed and the request was delivered .",
      "formula": [
        {
          "lhs": "It is calculated as the sum Ideation time, Development time & Release time"
        }
      ],
      "details": [
        {
          "type": "paragraph",
          "value": "Ideation time (Intake to DOR): Time taken from issue creation to it being ready for Sprint."
        },
        {
          "type": "paragraph",
          "value": "Development time (DOR to DOD): Time taken from start of work on an issue to it being completed in the Sprint as per DOD."
        },
        {
          "type": "paragraph",
          "value": "Release time (DOD to Live): Time taken between story completion to it going live."
        },
        {
          "type": "link",
          "kpiLinkDetail": {
            "text": "Detailed Information at",
            "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/2916400/BACKLOG+Governance#Lead-time"
          }
        }
      ]
    },
    "xAxisLabel": "",
    "yAxisLabel": "",
    "isPositiveTrend": false,
    "showTrend": false,
    "kpiFilter": "multiSelectDropDown",
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
        "level": "Intake-DoR",
        "range": [
          "-30",
          "30-20",
          "20-10",
          "10-5",
          "5-"
        ]
      },
      {
        "level": "DoR-DoD",
        "range": [
          "-20",
          "20-10",
          "10-7",
          "7-3",
          "3-"
        ]
      },
      {
        "level": "DoD-Live",
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
    "kpiId": "kpi148",
    "kpiName": "Flow Load",
    "kpiUnit": "",
    "isDeleted": "False",
    "defaultOrder": 4,
    "kpiCategory": "Backlog",
    "kpiSource": "Jira",
    "groupId": 10,
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
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi146",
    "kpiName": "Flow Distribution",
    "kpiUnit": "",
    "isDeleted": "False",
    "defaultOrder": 4,
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
    "defaultOrder": 6,
    "kpiCategory": "Release",
    "kpiSource": "Jira",
    "groupId": 9,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "CumulativeMultilineChart",
    "kpiInfo": {
      "definition": "It shows the cumulative daily actual progress of the release against the overall scope. It also shows additionally the scope added or removed during the release."
    },
    "xAxisLabel": "",
    "yAxisLabel": "Count",
    "isPositiveTrend": true,
    "showTrend": false,
    "isAdditionalFilterSupport": false,
    "kpiFilter": "radioButton",
    "boxType": "chart",
    "calculateMaturity": false
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
		"kpiId": "kpi84",
		"categoryId": "categoryOne",
		"kpiOrder": 6,
		"kanban": false
	},
	{
		"kpiId": "kpi11",
		"categoryId": "categoryOne",
		"kpiOrder": 7,
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
		"kpiId": "kpi116",
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
		"kpiId": "kpi118",
		"categoryId": "categoryThree",
		"kpiOrder": 1,
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
		"kpiId": "kpi65",
		"categoryId": "categoryOne",
		"kpiOrder": 4,
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
	}
]);


//Fields, used on issue details for KPI issue lists
db.kpi_column_configs.insertMany([{
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
                                 		}, {
                                 			columnName: 'Initial Commitment',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: true
                                 		}, {
                                 			columnName: 'Size(story point/hours)',
                                 			order: 4,
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
                                 			columnName: 'Linked Stories',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Linked Stories Size',
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
                                  }
                                 ]);

//default fields mapping for each KPI, these fields are used to populate the config JIRA for any 
//project. these can be changed/updated in project config under setting in the KnowHOW

db.getCollection('kpi_fieldmapping').insertMany(
[
{
        kpiId: 'kpi14',
        kpiName: 'Defect Injection Rate',
		kpiSource:'Jira',
        type: ['Scrum'],
        fieldNames :  {'Workflow Status Mapping' : ['jiraDod', 'jiraDefectCreatedStatus', 'jiraDefectDroppedStatus','resolutionTypeForRejection','jiraDefectRejectionStatus'], 'Issue Types Mapping' : ['jiraDefectInjectionIssueType'], 'Defects Mapping' : ['defectPriority', 'excludeRCAFromFTPR'] }
      },
      {
        kpiId: 'kpi82',
        kpiName: 'First Time Pass Rate',
		kpiSource:'Jira',
        type: ['Scrum'],
        fieldNames : {'Workflow Status Mapping' : ['resolutionTypeForRejection','jiraIssueDeliverdStatus','jiraDefectRejectionStatus','jiraIterationCompletionStatusCustomField','jiraStatusForQa','jiraStatusForDevelopment','jiraFtprRejectStatus'], 'Defects Mapping' : ['defectPriority', 'excludeRCAFromFTPR'] ,'Issue Types Mapping' : ['jiraIterationCompletionTypeCustomField','jiraFTPRStoryIdentification']}
        },

      {
        kpiId: 'kpi111',
        kpiName: 'Defect Density',
		kpiSource:'Jira',
        type: ['Scrum'],
        fieldNames : {'Issue Types Mapping' : ['jiraQADefectDensityIssueType'], 'Workflow Status Mapping' : ['jiraDod','resolutionTypeForRejection','jiraDefectRejectionStatus'], 'Defects Mapping' : ['jiraBugRaisedByQAIdentification','defectPriority','excludeRCAFromFTPR'], 'Custom Fields Mapping' : ['estimationCriteria', 'storyPointToHourMapping', 'jiraStoryPointsCustomField'] }
      },
      {
        kpiId: 'kpi35',
        kpiName: 'Defect Seepage Rate',
		kpiSource:'Jira',
        type: ['Scrum'],
        fieldNames : {'Issue Types Mapping' :['jiraDefectSeepageIssueType'], 'Workflow Status Mapping' : ['jiraDefectDroppedStatus'], 'Defects Mapping' : ['jiraBugRaisedByIdentification'] }
      },
      {
        kpiId: 'kpi34',
        kpiName: 'Defect Removal Efficiency',
		kpiSource:'Jira',
        type: ['Scrum'],
        fieldNames : {'Workflow Status Mapping' :  ['jiraDefectRemovalStatus'], 'Issue Types Mapping' : ['jiraDefectRemovalIssueType'] }
      },
      {
        kpiId: 'kpi37',
        kpiName: 'Defect Rejection Rate',
		kpiSource:'Jira',
        type: ['Scrum'],
        fieldNames : { 'Workflow Status Mapping' : ['jiraDefectRejectionStatus','resolutionTypeForRejection'], 'Issue Types Mapping' :  ['jiraDefectRejectionlIssueType'] }
      },
      {
        kpiId: 'kpi28',
        kpiName: 'Defect Count By Priority',
		kpiSource:'Jira',
        type: ['Scrum'],
        fieldNames : {'Issue Types Mapping' :  ['jiraDefectCountlIssueType'] , 'Workflow Status Mapping' : ['jiraDefectDroppedStatus'] }
      },
      {
        kpiId: 'kpi36',
        kpiName: 'Defect Count By RCA',
		kpiSource:'Jira',
        type: ['Scrum'],
        fieldNames : {'Issue Types Mapping' :  ['jiraDefectCountlIssueType'], 'Workflow Status Mapping' : ['jiraDefectDroppedStatus'] }
      },
      {
        kpiId: 'kpi126',
        type: ['Scrum'],
        kpiName: 'Created vs Resolved defects',
		kpiSource:'Jira',
        fieldNames : {'Workflow Status Mapping' : ['jiraIssueDeliverdStatus'] }
      },
      {
        kpiId: 'kpi42',
        kpiName: 'Regression Automation Coverage',
		kpiSource: 'Zypher',
        type: ['Scrum'],
        fieldNames : {'Test Cases Mapping' : ['jiraRegressionTestValue', 'testRegressionValue', 'regressionAutomationFolderPath'] }
      },
      {
        kpiId: 'kpi16',
        kpiName: 'In-Sprint Automation Coverage',
		kpiSource: 'Zypher',
        type: ['Scrum'],
        fieldNames : {'Test Cases Mapping' : ['jiraRegressionTestValue', 'testRegressionValue', 'regressionAutomationFolderPath'] }
      },
      {
        kpiId: 'kpi17',
        type: ['Scrum'],
        kpiName: 'Unit Test Coverage',
		kpiSource: 'Sonar',
        fieldNames : { }
      },
      {
        kpiId: 'kpi38',
        kpiName: 'Sonar Violations',
		kpiSource: 'Sonar',
        type: ['Scrum'],
        fieldNames : { }
      },
      {
        kpiId: 'kpi27',
        kpiName: 'Sonar Tech Debt',
		kpiSource: 'Sonar',
        type: ['Scrum'],
        fieldNames : { }
      },
      {
        kpiId: 'kpi116',
        kpiName: 'Change Failure Rate',
		kpiSource: 'Jenkins',
        type: ['Scrum'],
        fieldNames : { }
      },
      {
        kpiId: 'kpi70',
        kpiName: 'Test Execution and pass percentage',
		kpiSource: 'Zypher',
        type: ['Scrum'],
        fieldNames : { }
      },
      {
        kpiId: 'kpi40',
        kpiName: 'Issue Count',
		kpiSource: 'Jira',
        type: ['Scrum'],
        fieldNames : {'Issue Types Mapping' : ['jiraStoryIdentification','jiraIterationCompletionTypeCustomField'] ,'Workflow Status Mapping' : ['jiraIterationCompletionStatusCustomField']}
      },
      {
        kpiId: 'kpi72',
        kpiName: 'Commitment Reliability',
		kpiSource: 'Jira',
        type: ['Scrum'],
        fieldNames : { 'Issue Types Mapping' : ['jiraIterationCompletionTypeCustomField'], 'Workflow Status Mapping' : ['jiraIterationCompletionStatusCustomField'], 'Custom Fields Mapping' : ['estimationCriteria', 'storyPointToHourMapping', 'jiraStoryPointsCustomField'] }
      },
      {
        kpiId: 'kpi39',
        kpiName: 'Sprint Velocity',
		kpiSource: 'Jira',
        type: ['Scrum'],
        fieldNames : {'Issue Types Mapping' : ['jiraIterationCompletionTypeCustomField'], 'Custom Fields Mapping' : ['estimationCriteria', 'storyPointToHourMapping', 'jiraStoryPointsCustomField'] ,'Workflow Status Mapping' : ['jiraIterationCompletionStatusCustomField']}
      },
      {
        kpiId: 'kpi46',
        kpiName: 'Sprint Capacity Utilization',
		kpiSource: 'Jira',
        type: ['Scrum'],
        fieldNames : {'Issue Types Mapping' : ['jiraSprintCapacityIssueType'] }
      },
      {
        kpiId: 'kpi84',
        type: ['Scrum'],
        kpiName: 'Mean Time To Merge',
		kpiSource: 'BitBucket',
        fieldNames : { }
      },
      {
        kpiId: 'kpi11',
        type: ['Scrum'],
        kpiName: 'Check-Ins & Merge Requests',
		kpiSource: 'BitBucket',
        fieldNames : { }
      },
      {
        kpiId: 'kpi8',
        kpiName: 'Code Build Time',
		kpiSource: 'Jenkins',
        type: ['Scrum'],
        fieldNames : { }
      },
      {
        kpiId: 'kpi3',
        kpiName: 'Lead Time',
		kpiSource: 'Jira',
        type: ['Scrum'],
        fieldNames : {'Issue Types Mapping' : ['jiraIntakeToDorIssueType'], 'Workflow Status Mapping' : ['jiraDor', 'jiraDod', 'jiraLiveStatus','storyFirstStatus'] }
      },
      {
        kpiId: 'kpi118',
        kpiName: 'Deployment Frequency',
		kpiSource: 'Jenkins',
        type: ['Scrum'],
        fieldNames : { }
      },
      {
        kpiId: 'kpi73',
        kpiName: 'Release Frequency',
		kpiSource: 'Jira',
        type: ['Scrum'],
        fieldNames : { }
      },
      {
        kpiId: 'kpi113',
        kpiName: 'Value delivered (Cost of Delay)',
		kpiSource: 'Jira',
        type: ['Scrum'],
        fieldNames : {'Custom Fields Mapping' : ['epicCostOfDelay'] }
      },
	  {
        kpiId: 'kpi5',
        kpiName: 'Sprint Predictability',
		kpiSource: 'Jira',
        type: ['Scrum'],
        fieldNames : {'Issue Types Mapping' : ['jiraSprintVelocityIssueType','jiraIterationCompletionTypeCustomField'], 'Workflow Status Mapping' : ['jiraIssueDeliverdStatus','jiraIterationCompletionStatusCustomField'], 'Custom Fields Mapping' : ['estimationCriteria', 'storyPointToHourMapping', 'jiraStoryPointsCustomField'] }
      },
      {
        kpiId: 'kpi55',
        kpiName: 'Ticket Open vs Closed rate by type',
		kpiSource: 'Jira',
        type: ['Kanban'],
        fieldNames : {'Issue Types Mapping' : ['ticketCountIssueType'], 'Workflow Status Mapping' : ['jiraTicketClosedStatus'] }
      },
      {
        kpiId: 'kpi54',
        kpiName: 'Ticket Open vs Closed rate by Priority',
		kpiSource: 'Jira',
        type: ['Kanban'],
        fieldNames : {'Issue Types Mapping' : ['ticketCountIssueType'],'Workflow Status Mapping' : ['jiraTicketClosedStatus'] }
      },
      {
        kpiId: 'kpi50',
        kpiName: 'Net Open Ticket Count by Priority',
		kpiSource: 'Jira',
        type: ['Kanban'],
        fieldNames : { 'Workflow Status Mapping' : ['storyFirstStatus', 'jiraTicketClosedStatus', 'jiraLiveStatus', 'jiraTicketRejectedStatus'], 'Issue Types Mapping' : ['kanbanRCACountIssueType', 'ticketCountIssueType'] }
      },
      {
        kpiId: 'kpi51',
        kpiName: 'Net Open Ticket Count By RCA',
		kpiSource: 'Jira',
        type: ['Kanban'],
        fieldNames : { 'Workflow Status Mapping' : ['storyFirstStatus', 'jiraTicketClosedStatus', 'jiraLiveStatus', 'jiraTicketRejectedStatus'], 'Issue Types Mapping' : ['kanbanRCACountIssueType', 'ticketCountIssueType']  }
      },
      {
        kpiId: 'kpi48',
        kpiName: 'Net Open Ticket By Status',
		kpiSource: 'Jira',
        type: ['Kanban'],
        fieldNames : { 'Workflow Status Mapping' : ['storyFirstStatus', 'jiraTicketClosedStatus', 'jiraLiveStatus', 'jiraTicketRejectedStatus'], 'Issue Types Mapping' : ['kanbanRCACountIssueType', 'ticketCountIssueType']  }
      },
      {
        kpiId: 'kpi997',
        kpiName: 'Open Ticket Ageing By Priority',
		kpiSource: 'Jira',
        type: ['Kanban'],
        fieldNames : { 'Workflow Status Mapping' : ['jiraTicketClosedStatus', 'jiraLiveStatus', 'jiraTicketRejectedStatus'], 'Issue Types Mapping' : ['ticketCountIssueType'] }
      },
      {
        kpiId: 'kpi63',
        kpiName: 'Regression Automation Coverage',
		kpiSource: 'Zypher',
        type: ['Kanban'],
        fieldNames : { 'Test Cases Mapping' : ['jiraRegressionTestValue', 'testRegressionValue', 'regressionAutomationFolderPath'] }
      },
      {
        kpiId: 'kpi49',
        kpiName: 'Ticket Velocity',
		kpiSource: 'Jira',
        type: ['Kanban'],
        fieldNames : { 'Issue Types Mapping' : ['jiraTicketVelocityIssueType'], 'Workflow Status Mapping' : ['ticketDeliverdStatus'] }
      },
      {
        kpiId: 'kpi53',
        kpiName: 'Lead Time',
        kpiSource:'Jira',
        type: ['Kanban'],
        fieldNames : {'Workflow Status Mapping' : ['jiraTicketTriagedStatus', 'jiraTicketClosedStatus', 'jiraLiveStatus'] }
      },
      {
        kpiId: 'kpi114',
        type: ['Kanban'],
		kpiSource: 'Jira',
        kpiName: 'Value delivered (Cost of Delay)',
        fieldNames : {'Custom Fields Mapping' : ['epicCostOfDelay'] }
      },
      {
        kpiId: 'kpi74',
        kpiName: 'Release Frequency',
		kpiSource: 'Jira',
        type: ['Kanban'],
        fieldNames : { }
      },
      {
        kpiId: 'kpi62',
        kpiName: 'Unit Test Coverage',
		kpiSource: 'Sonar',
        type: ['Kanban'],
        fieldNames : { }
      },
      {
        kpiId: 'kpi64',
        kpiName: 'Sonar Violations',
		kpiSource: 'Sonar',
        type: ['Kanban'],
        fieldNames : { }
      },
      {
        kpiId: 'kpi67',
        kpiName: 'Sonar Tech Debt',
		kpiSource: 'Sonar',
        type: ['Kanban'],
        fieldNames : { }
      },
      {
        kpiId: 'kpi71',
        kpiName: 'Test Execution and pass percentage',
		kpiSource: 'Zypher',
        type: ['Kanban'],
        fieldNames : { }
      },
      {
        kpiId: 'kpi58',
        kpiName: 'Team Capacity',
		kpiSource: 'Jira',
        type: ['Kanban'],
        fieldNames : { }
      },
      {
        kpiId: 'kpi66',
        kpiName: 'Code Build Time',
		kpiSource: 'Jenkins',
        type: ['Kanban'],
        fieldNames : { }
      },
      {
        kpiId: 'kpi65',
        kpiName: 'Number of Check-ins',
		kpiSource: 'BitBucket',
        type: ['Kanban'],
        fieldNames : { }
      },
      {
        kpiId: 'kpi121',
        kpiName: 'Capacity',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : {'Issue Types Mapping' : ['jiraSprintCapacityIssueType'] }
      },
      {
        kpiId: 'kpi119',
        kpiName: 'Work Remaining',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : { 'Workflow Status Mapping' : ['jiraStatusForInProgress', 'jiraDevDoneStatus','jiraIterationCompletionStatusCustomField'], 'Issue Types Mapping' : ['jiraIterationCompletionTypeCustomField'],'Custom Fields Mapping' : ['jiraDueDateField']}
      },
      {
        kpiId: 'kpi75',
        kpiName: 'Estimate vs Actual',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : { 'Workflow Status Mapping' : ['jiraIterationCompletionStatusCustomField'], 'Issue Types Mapping' : ['jiraIterationCompletionTypeCustomField']}
      },
      {
        kpiId: 'kpi123',
        kpiName: 'Issues likely to Spill',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : { 'Workflow Status Mapping' : ['jiraStatusForInProgress','jiraIterationCompletionStatusCustomField'], 'Custom Fields Mapping' : ['jiraDueDateField'],'Issue Types Mapping' : ['jiraIterationCompletionTypeCustomField']}
      },
      {
        kpiId: 'kpi122',
        kpiName: 'Closure Possible Today',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : { 'Workflow Status Mapping' : ['jiraStatusForInProgress','jiraIterationCompletionStatusCustomField'], 'Custom Fields Mapping' : ['jiraDueDateField'],'Issue Types Mapping' : ['jiraIterationCompletionTypeCustomField']}
      },
      {
        kpiId: 'kpi120',
        kpiName: 'Iteration Commitment',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : {'Workflow Status Mapping' : ['jiraIterationCompletionStatusCustomField'],'Issue Types Mapping' : ['jiraIterationCompletionTypeCustomField'] }
      },
      {
        kpiId: 'kpi124',
        kpiName: 'Estimation Hygiene',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : { 'Workflow Status Mapping' : ['issueStatusExcluMissingWork','jiraIterationCompletionStatusCustomField'],'Issue Types Mapping' : ['jiraIterationCompletionTypeCustomField'] }
      },
      {
        kpiId: 'kpi125',
        kpiName: 'Iteration Burnup',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : {'Workflow Status Mapping' : ['jiraIterationCompletionStatusCustomField'],'Issue Types Mapping' : ['jiraIterationCompletionTypeCustomField'] }
      },
	  {
        kpiId: 'kpi128',
        kpiName: 'Planned Work Status',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : {'Workflow Status Mapping' : ['jiraStatusForInProgress', 'jiraDevDoneStatus','jiraIterationCompletionStatusCustomField'],'Issue Types Mapping' : ['jiraIterationCompletionTypeCustomField']}
      },
      {
         kpiId: 'kpi145',
         kpiName: 'Dev Completion Status',
      	 kpiSource: 'Jira',
         type: ['Other'],
         fieldNames : {'Workflow Status Mapping' : ['jiraStatusForInProgress', 'jiraDevDoneStatus']}
      },
      {
        kpiId: 'kpi79',
        kpiName: 'Test Cases Without Story Link',
		kpiSource: 'Zypher',
        type: ['Other'],
        fieldNames : { 'Issue Types Mapping' : ['jiraStoryIdentification'], 'Test Cases Mapping' : ['JiraRegressionTestValue', 'testRegressionValue', 'regressionAutomationFolderPath'] }
      },
      {
        kpiId: 'kpi80',
        kpiName: 'Defects Without Story Link',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : { 'Issue Types Mapping' : ['jiraStoryIdentification'], 'Workflow Status Mapping' : ['excludeStatusKpi129'] }
      },
      {
        kpiId: 'kpi127',
        kpiName: 'Production Defects Ageing',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : { 'Workflow Status Mapping' : ['jiraDod', 'jiraLiveStatus', 'jiraDefectDroppedStatus'], 'Defects Mapping' : ['productionDefectIdentifier'] }
      },
      {
        kpiId: 'kpi131',
        kpiName: 'Wastage',
        kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : { 'Workflow Status Mapping' : ['jiraBlockedStatus', 'jiraWaitStatus','jiraIterationCompletionStatusCustomField'],'Issue Types Mapping' : ['jiraIterationCompletionTypeCustomField'] }
      },
      {
        kpiId: 'kpi133',
        kpiName: 'Quality Status',
        kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : {'Workflow Status Mapping' : ['resolutionTypeForRejection','jiraDefectRejectionStatus','jiraIterationCompletionStatusCustomField'], 'Defects Mapping' : ['defectPriority', 'excludeRCAFromFTPR','jiradefecttype'], 'Issue Types Mapping' : ['jiraIterationCompletionTypeCustomField'] }
      },
	  {
        kpiId: 'kpi134',
        kpiName: 'Unplanned Work Status',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : {'Workflow Status Mapping' : ['jiraIterationCompletionStatusCustomField'],'Issue Types Mapping' : ['jiraIterationCompletionTypeCustomField'] }
      },
      {
        kpiId: 'kpi136',
        kpiName: 'Defect Count by Status',
        kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : {'Defects Mapping' : ['jiradefecttype'],'Workflow Status Mapping' : ['jiraIterationCompletionStatusCustomField'],'Issue Types Mapping' : ['jiraIterationCompletionTypeCustomField'] }
      },
      {
        kpiId: 'kpi137',
        kpiName: 'Defect Reopen Rate',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : { 'Workflow Status Mapping' : ['jiraDefectClosedStatus'] }
      },
      {
        kpiId: 'kpi141',
        kpiName: 'Defect Count by Status (Release)',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : {'Defects Mapping' : ['jiradefecttype'] }
      },
      {
        kpiId: 'kpi142',
        kpiName: 'Defect Count by RCA (Release)',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : {'Defects Mapping' : ['jiradefecttype'] }
      },
      {
        kpiId: 'kpi143',
        kpiName: 'Defect Count by Assignee (Release)',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : {'Defects Mapping' : ['jiradefecttype'] }
      },
      {
        kpiId: 'kpi144',
        kpiName: 'Defect Count by Priority (Release)',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : {'Defects Mapping' : ['jiradefecttype'] }
      },
      {
        kpiId: 'kpi989',
        kpiName: 'Kpi Maturity',
        type: ['Other'],
        fieldNames : { }
      },
      {
        kpiId: 'kpi139',
        kpiName: 'Refinement Rejection Rate',
        kpiSource: 'Jira',
        type: ['Other'],
        fieldNames: {
          'Workflow Status Mapping': [
            'jiraReadyForRefinement',
            'jiraAcceptedInRefinement',
            'jiraRejectedInRefinement'
          ]
        }
      },
      {
              kpiId: 'kpi138',
              kpiName: 'Backlog Readiness Efficiency',
      		kpiSource: 'Jira',
              type: ['Other'],
              fieldNames : {'Workflow Status Mapping' : ['readyForDevelopmentStatus'] }
            },
            {
             kpiId: 'Kpi148',
             kpiName: 'Flow Load',
             kpiSource: 'Jira',
             type: ['Other'],
             fieldNames: {
                 'Workflow Status Mapping': [
                    'storyFirstStatus',
                    'jiraStatusForInProgress',
                    'jiraStatusForQa'
                ]
             }
            },
      {
        kpiId: 'Kpi146',
        kpiName: 'Flow Distribution',
        type: ['Other'],
        fieldNames : { }
      }
	  ]);





