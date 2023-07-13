//---------7.2.0-------
const bulkUpdateOps = [];

function splitString(inputValue, delimiter) {
    return inputValue.split(delimiter);
}

function addIntoSet(list, array, delimiter, projectName) {
    splitString(array, delimiter).forEach(element => {
        list.add(element + "_" + projectName);
    });
}

function deleteFromAccountHierarchy(project_release) {
    const projectId = project_release.projectId;
    const configId = project_release.configId;

    const projectName = splitString(projectId, "_").shift();

    const list = new Set();
    //issues which had a history of release are maintained in a list
    db.jira_issue_custom_history.find(
        { "basicProjectConfigId": configId.str },
        { 'fixVersionUpdationLog.changedFrom': 1, 'fixVersionUpdationLog.changedTo': 1, _id: 0 }
    ).forEach(doc => {
        const fixVersionUpdationLog = doc.fixVersionUpdationLog;
        if (fixVersionUpdationLog) {
            fixVersionUpdationLog.forEach(log => {
                addIntoSet(list, log.changedFrom + "," + log.changedTo, ',', projectName);
            });
        }
    });

    const nodeNames = Array.from(list);
    print("projectId "+ projectId+ " nodeNames "+nodeNames);

    bulkUpdateOps.push({
        updateMany: {
            filter: {
                nodeName: { $in: nodeNames },
                basicProjectConfigId: configId,
                labelName: "release"
            },
            update: {
                $set: { retain: true }
            }
        }
    });
}

//for each project_release, going to remove extra releases which are not-required
db.getCollection('project_release').find().forEach(project_release => {
    if (project_release.projectId !== undefined) {
        deleteFromAccountHierarchy(project_release);
    }
});

//bulk-updating account_hierarchy with column retain=true, if those are required on release filter
if (bulkUpdateOps.length > 0) {
    db.account_hierarchy.bulkWrite(bulkUpdateOps);
}

//deleting the columns which are not required i.e., retain column absent
db.account_hierarchy.deleteMany({ retain: { $eq: null }, labelName: "release" });

//finally deleting the column name by unsetting the retain column
db.account_hierarchy.update(
    { "labelName": "release" },
    { $unset: { retain: 1 } },
    { multi: true }
);

//------ comment feature collections generic field name for all board (iteration , release)//
db.kpi_comments.updateMany(
  {},
  [
    { $set: { "nodeChildId": "$sprintId" } },
    { $unset: "sprintId" },
  ]
)
db.kpi_comments_history.updateMany(
  {},
  [
    { $set: { "nodeChildId": "$sprintId" } },
    { $unset: "sprintId" },
  ]
)


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

db.getCollection('kpi_master').insert(
[{
       "kpiId": "kpi151",
       "kpiName": "Backlog Count By Status",
       "kpiUnit": "Count",
       "isDeleted": "False",
       "defaultOrder": 8,
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
          "defaultOrder": 8,
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