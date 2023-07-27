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
      }
 ]);

 //7.4 changes
 db.kpi_column_configs.updateOne(
   { "kpiId": "kpi72" },
   {
     $set: {
       "kpiColumnDetails": [
         {
           "columnName": "Sprint Name",
           "order": 0,
           "isShown": true,
           "isDefault": false
         },
         {
           "columnName": "Story ID",
           "order": 1,
           "isShown": true,
           "isDefault": false
         },
         {
           "columnName": "Issue Status",
           "order": 2,
           "isShown": true,
           "isDefault": false
         },
         {
           "columnName": "Issue Type",
           "order": 3,
           "isShown": true,
           "isDefault": true
         },
         {
           "columnName": "Initial Commitment",
           "order": 4,
           "isShown": true,
           "isDefault": true
         },
         {
           "columnName": "Size(story point/hours)",
           "order": 5,
           "isShown": true,
           "isDefault": true
         }
       ]
     }
   }
 );
