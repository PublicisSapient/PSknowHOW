//---------7.2.0-------


//7.3 changes

//-------------------- insert new kpi details -------

db.kpi_column_configs.insertMany([{
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
                                  }
                                 ]);

db.getCollection('kpi_master').insertMany(
[{
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
       "kpiFilter": "dropdown",
       "boxType": "chart",
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
          "groupId": 10,
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
          "kpiFilter": "dropdown",
          "boxType": "chart",
          "calculateMaturity": false
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
     }
 ]);

 db.getCollection('kpi_fieldmapping').insertMany(
     [{
         kpiId: 'kpi34',
         kpiName: 'Defect Removal Efficiency',
         kpiSource: 'Jira',
         type: ['Scrum'],
         fieldNames: {
             'Workflow Status Mapping': ['jiraDefectRemovalStatus', 'resolutionTypeForRejection', 'jiraDefectRejectionStatus'],
             'Issue Types Mapping': ['jiraDefectRemovalIssueType']
         }
     }
 ]);