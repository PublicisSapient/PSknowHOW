//---------7.5.0 changes------------------------------------------------------------------

// Reversing "Fetch Sprint" action policy
db.action_policy_rule.deleteMany({
    "name": "Fetch Sprint"
});

// Reversing  jiraDodKPI37 changes in field mapping
db.field_mapping_structure.deleteMany({
    "fieldName": "jiraDodKPI37"
});

// Reversing jiraDodKPI37 field mapping
const fieldMapToReverse = db.field_mapping.find({ "jiraDodKPI37": { $exists: true } });
fieldMapToReverse.forEach(function(fm) {
    const jiraDefectRejectionlIssueType = fm.jiraDefectRejectionlIssueType

    db.field_mapping.updateOne(
        { "_id": fm._id },
        {
            $set: {
                "jiraIssueTypeKPI37": jiraDefectRejectionlIssueType
            },
            $unset: {
                "jiraDodKPI37": ""
            }
        }
    );
});

// Reversion DRR definition change
db.kpi_master.updateOne(
  {
    "kpiId": "kpi37",
    "kpiInfo.formula.operands": "Total no. of defects Closed in a sprint"
  },
  {
    $set: {
      "kpiInfo.formula.$[formulaElem].operands.$[operandElem]": "Total no. of defects reported in a sprint"
    }
  },
  {
    arrayFilters: [
      { "formulaElem.operands": { $exists: true } },
      { "operandElem": "Total no. of defects Closed in a sprint" }
    ]
  }
);

//------7.6 changes-------

//reversing metadata_identifier back when we use to compare metadata_identifier with boardMetadata
db.getCollection('metadata_identifier').update(
      { "templateCode": "4" }, // Match documents with templateCode equal to "4"
      { $set:
      {
                                                "tool": "Jira",
                                                "templateName": "DOJO Agile Template",
                                                "templateCode": "4",
                                                "isKanban": false,
                                                "disabled": false,
                                                "issues": [
                                                  {
                                                    "type": "story",
                                                    "value": [
                                                      "Story",
                                                      "Enabler Story"
                                                    ]
                                                  },
                                                  {
                                                    "type": "bug",
                                                    "value": [
                                                      "Defect"
                                                    ]
                                                  },
                                                  {
                                                    "type": "epic",
                                                    "value": [
                                                      "Epic"
                                                    ]
                                                  },
                                                  {
                                                    "type": "issuetype",
                                                    "value": [
                                                      "Story",
                                                      "Enabler Story",
                                                      "Change request",
                                                      "Defect"
                                                    ]
                                                  }
                                                ],
                                                "customfield": [
                                                  {
                                                    "type": "storypoint",
                                                    "value": [
                                                      "Story Points"
                                                    ]
                                                  },
                                                  {
                                                    "type": "sprint",
                                                    "value": [
                                                      "Sprint"
                                                    ]
                                                  },
                                                  {
                                                    "type": "rootcause",
                                                    "value": [
                                                      "Root Cause"
                                                    ]
                                                  },
                                                  {
                                                    "type": "techdebt",
                                                    "value": [
                                                      "Tech Debt"
                                                    ]
                                                  },
                                                  {
                                                    "type": "uat",
                                                    "value": [
                                                      "UAT"
                                                    ]
                                                  },
                                                  {
                                                    "type": "timeCriticality",
                                                    "value": [
                                                      "Time Criticality"
                                                    ]
                                                  },
                                                  {
                                                    "type": "wsjf",
                                                    "value": [
                                                      "WSJF"
                                                    ]
                                                  },
                                                  {
                                                    "type": "costOfDelay",
                                                    "value": [
                                                      "Cost of Delay"
                                                    ]
                                                  },
                                                  {
                                                    "type": "businessValue",
                                                    "value": [
                                                      "User-Business Value"
                                                    ]
                                                  },
                                                  {
                                                    "type": "riskReduction",
                                                    "value": [
                                                      "Risk Reduction-Opportunity Enablement Value"
                                                    ]
                                                  },
                                                  {
                                                    "type": "jobSize",
                                                    "value": [
                                                      "Job Size"
                                                    ]
                                                  }
                                                ],
                                                "workflow": [
                                                  {
                                                    "type": "dor",
                                                    "value": [
                                                      "Ready for Sprint Planning",
                                                      "In Progress"
                                                    ]
                                                  },
                                                  {
                                                    "type": "dod",
                                                    "value": [
                                                      "Closed"
                                                    ]
                                                  },
                                                  {
                                                    "type": "rejectionResolution",
                                                    "value": [
                                                      "Dropped",
                                                      "Rejected"
                                                    ]
                                                  },
                                                  {
                                                    "type": "delivered",
                                                    "value": [
                                                      "Closed"
                                                    ]
                                                  },
                                                  {
                                                    "type": "development",
                                                    "value": [
                                                      "In Development"
                                                    ]
                                                  },
                                                  {
                                                    "type": "qa",
                                                    "value": [
                                                      "In Testing"
                                                    ]
                                                  }
                                                ]
                                              }
      },
      { multi: false }
);
db.getCollection('metadata_identifier').update(
      { "templateCode": "5" }, // Match documents with templateCode equal to "5"
      { $set:
      {
                                                "tool": "Jira",
                                                "templateName": "DOJO Safe Template",
                                                "templateCode": "5",
                                                "isKanban": false,
                                                "disabled": false,
                                                "issues": [
                                                  {
                                                    "type": "story",
                                                    "value": [
                                                      "Story",
                                                      "Enabler Story"
                                                    ]
                                                  },
                                                  {
                                                    "type": "bug",
                                                    "value": [
                                                      "Defect"
                                                    ]
                                                  },
                                                  {
                                                    "type": "epic",
                                                    "value": [
                                                      "Epic"
                                                    ]
                                                  },
                                                  {
                                                    "type": "issuetype",
                                                    "value": [
                                                      "Story",
                                                      "Enabler Story",
                                                      "Change request",
                                                      "Defect"
                                                    ]
                                                  }
                                                ],
                                                "customfield": [
                                                  {
                                                    "type": "storypoint",
                                                    "value": [
                                                      "Story Points"
                                                    ]
                                                  },
                                                  {
                                                    "type": "sprint",
                                                    "value": [
                                                      "Sprint"
                                                    ]
                                                  },
                                                  {
                                                    "type": "rootcause",
                                                    "value": [
                                                      "Root Cause"
                                                    ]
                                                  },
                                                  {
                                                    "type": "techdebt",
                                                    "value": [
                                                      "Tech Debt"
                                                    ]
                                                  },
                                                  {
                                                    "type": "uat",
                                                    "value": [
                                                      "UAT"
                                                    ]
                                                  },
                                                  {
                                                    "type": "timeCriticality",
                                                    "value": [
                                                      "Time Criticality"
                                                    ]
                                                  },
                                                  {
                                                    "type": "wsjf",
                                                    "value": [
                                                      "WSJF"
                                                    ]
                                                  },
                                                  {
                                                    "type": "costOfDelay",
                                                    "value": [
                                                      "Cost of Delay"
                                                    ]
                                                  },
                                                  {
                                                    "type": "businessValue",
                                                    "value": [
                                                      "User-Business Value"
                                                    ]
                                                  },
                                                  {
                                                    "type": "riskReduction",
                                                    "value": [
                                                      "Risk Reduction-Opportunity Enablement Value"
                                                    ]
                                                  },
                                                  {
                                                    "type": "jobSize",
                                                    "value": [
                                                      "Job Size"
                                                    ]
                                                  }
                                                ],
                                                "workflow": [
                                                  {
                                                    "type": "dor",
                                                    "value": [
                                                      "Ready for Sprint Planning",
                                                      "In Progress"
                                                    ]
                                                  },
                                                  {
                                                    "type": "dod",
                                                    "value": [
                                                      "Closed"
                                                    ]
                                                  },
                                                  {
                                                    "type": "rejectionResolution",
                                                    "value": [
                                                      "Dropped",
                                                      "Rejected"
                                                    ]
                                                  },
                                                  {
                                                    "type": "delivered",
                                                    "value": [
                                                      "Closed"
                                                    ]
                                                  },
                                                  {
                                                    "type": "development",
                                                    "value": [
                                                      "In Development"
                                                    ]
                                                  },
                                                  {
                                                    "type": "qa",
                                                    "value": [
                                                      "In Testing"
                                                    ]
                                                  }
                                                ]
                                              }
      },
      { multi: false }
);
db.getCollection('metadata_identifier').update(
      { "templateCode": "6" }, // Match documents with templateCode equal to "6"
      { $set:
      {
                                                "tool": "Jira",
                                                "templateName": "DOJO Studio Template",
                                                "templateCode": "6",
                                                "isKanban": false,
                                                "disabled": false,
                                                "issues": [
                                                  {
                                                    "type": "story",
                                                    "value": [
                                                      "Story",
                                                      "Enabler Story"
                                                    ]
                                                  },
                                                  {
                                                    "type": "bug",
                                                    "value": [
                                                      "Defect"
                                                    ]
                                                  },
                                                  {
                                                    "type": "epic",
                                                    "value": [
                                                      "Epic"
                                                    ]
                                                  },
                                                  {
                                                    "type": "issuetype",
                                                    "value": [
                                                      "Story",
                                                      "Enabler Story",
                                                      "Change request",
                                                      "Defect"
                                                    ]
                                                  }
                                                ],
                                                "customfield": [
                                                  {
                                                    "type": "storypoint",
                                                    "value": [
                                                      "Story Points"
                                                    ]
                                                  },
                                                  {
                                                    "type": "sprint",
                                                    "value": [
                                                      "Sprint"
                                                    ]
                                                  },
                                                  {
                                                    "type": "rootcause",
                                                    "value": [
                                                      "Root Cause"
                                                    ]
                                                  },
                                                  {
                                                    "type": "techdebt",
                                                    "value": [
                                                      "Tech Debt"
                                                    ]
                                                  },
                                                  {
                                                    "type": "uat",
                                                    "value": [
                                                      "UAT"
                                                    ]
                                                  },
                                                  {
                                                    "type": "timeCriticality",
                                                    "value": [
                                                      "Time Criticality"
                                                    ]
                                                  },
                                                  {
                                                    "type": "wsjf",
                                                    "value": [
                                                      "WSJF"
                                                    ]
                                                  },
                                                  {
                                                    "type": "costOfDelay",
                                                    "value": [
                                                      "Cost of Delay"
                                                    ]
                                                  },
                                                  {
                                                    "type": "businessValue",
                                                    "value": [
                                                      "User-Business Value"
                                                    ]
                                                  },
                                                  {
                                                    "type": "riskReduction",
                                                    "value": [
                                                      "Risk Reduction-Opportunity Enablement Value"
                                                    ]
                                                  },
                                                  {
                                                    "type": "jobSize",
                                                    "value": [
                                                      "Job Size"
                                                    ]
                                                  }
                                                ],
                                                "workflow": [
                                                  {
                                                    "type": "dor",
                                                    "value": [
                                                      "Open"
                                                    ]
                                                  },
                                                  {
                                                    "type": "dod",
                                                    "value": [
                                                      "Closed"
                                                    ]
                                                  },
                                                  {
                                                    "type": "rejectionResolution",
                                                    "value": [
                                                      "Dropped",
                                                      "Rejected"
                                                    ]
                                                  },
                                                  {
                                                    "type": "delivered",
                                                    "value": [
                                                      "Closed"
                                                    ]
                                                  },
                                                  {
                                                    "type": "development",
                                                    "value": [
                                                      "In Development"
                                                    ]
                                                  },
                                                  {
                                                    "type": "qa",
                                                    "value": [
                                                      "In Testing"
                                                    ]
                                                  }
                                                ]
                                              }
      },
      { multi: false }
);
db.getCollection('metadata_identifier').update(
      { "templateCode": "7" }, // Match documents with templateCode equal to "7"
      { $set:
      {
                                                "tool": "Jira",
                                                "templateName": "Standard Template",
                                                "templateCode": "7",
                                                "isKanban": false,
                                                "disabled": false,
                                                "issues": [
                                                  {
                                                    "type": "story",
                                                    "value": [
                                                      "Story",
                                                      "Enabler Story",
                                                      "Tech Story",
                                                      "Change request"
                                                    ]
                                                  },
                                                  {
                                                    "type": "bug",
                                                    "value": [
                                                      "Defect",
                                                      "Bug"
                                                    ]
                                                  },
                                                  {
                                                    "type": "epic",
                                                    "value": [
                                                      "Epic"
                                                    ]
                                                  },
                                                  {
                                                    "type": "issuetype",
                                                    "value": [
                                                      "Story",
                                                      "Enabler Story",
                                                      "Tech Story",
                                                      "Change request",
                                                      "Defect",
                                                      "Bug",
                                                      "Epic"
                                                    ]
                                                  },
                                                  {
                                                    "type": "uatdefect",
                                                    "value": [
                                                      "UAT Defect"
                                                    ]
                                                  }
                                                ],
                                                "customfield": [
                                                  {
                                                    "type": "storypoint",
                                                    "value": [
                                                      "Story Points"
                                                    ]
                                                  },
                                                  {
                                                    "type": "sprint",
                                                    "value": [
                                                      "Sprint"
                                                    ]
                                                  },
                                                  {
                                                    "type": "rootcause",
                                                    "value": [
                                                      "Root Cause"
                                                    ]
                                                  },
                                                  {
                                                    "type": "techdebt",
                                                    "value": [
                                                      "Tech Debt"
                                                    ]
                                                  },
                                                  {
                                                    "type": "uat",
                                                    "value": [
                                                      "UAT"
                                                    ]
                                                  },
                                                  {
                                                    "type": "timeCriticality",
                                                    "value": [
                                                      "Time Criticality"
                                                    ]
                                                  },
                                                  {
                                                    "type": "wsjf",
                                                    "value": [
                                                      "WSJF"
                                                    ]
                                                  },
                                                  {
                                                    "type": "costOfDelay",
                                                    "value": [
                                                      "Cost of Delay"
                                                    ]
                                                  },
                                                  {
                                                    "type": "businessValue",
                                                    "value": [
                                                      "User-Business Value"
                                                    ]
                                                  },
                                                  {
                                                    "type": "riskReduction",
                                                    "value": [
                                                      "Risk Reduction-Opportunity Enablement Value"
                                                    ]
                                                  },
                                                  {
                                                    "type": "jobSize",
                                                    "value": [
                                                      "Job Size"
                                                    ]
                                                  }
                                                ],
                                                "workflow": [
                                                  {
                                                    "type": "dor",
                                                    "value": [
                                                      "Ready for Sprint Planning",
                                                      "In Progress"
                                                    ]
                                                  },
                                                  {
                                                    "type": "dod",
                                                    "value": [
                                                      "Closed",
                                                      "Resolved",
                                                      "Ready for Delivery"
                                                    ]
                                                  },
                                                  {
                                                    "type": "development",
                                                    "value": [
                                                      "Implementing",
                                                      "In Development",
                                                      "In Analysis"
                                                    ]
                                                  },
                                                  {
                                                    "type": "qa",
                                                    "value": [
                                                      "In Testing"
                                                    ]
                                                  },
                                                  {
                                                    "type": "firststatus",
                                                    "value": [
                                                      "Open"
                                                    ]
                                                  },
                                                  {
                                                    "type": "rejection",
                                                    "value": [
                                                      "Closed",
                                                      "Rejected"
                                                    ]
                                                  },
                                                  {
                                                    "type": "delivered",
                                                    "value": [
                                                      "Closed",
                                                      "Resolved",
                                                      "Ready for Delivery",
                                                      "Ready for Release"
                                                    ]
                                                  },
                                                  {
                                                    "type": "jiraWaitStatus",
                                                    "value": [
                                                      "Ready for Testing"
                                                    ]
                                                  },
                                                  {
                                                    "type": "jiraBlockedStatus",
                                                    "value": [
                                                      "On Hold",
                                                      "Blocked"
                                                    ]
                                                  },
                                                  {
                                                    "type": "jiraStatusForInProgress",
                                                    "value": [
                                                      "In Analysis",
                                                      "In Development",
                                                      "In Progress"
                                                    ]
                                                  },
                                                  {
                                                    "type": "jiraStatusForClosed",
                                                    "value": [
                                                      "Closed",
                                                      "CLOSED"
                                                    ]
                                                  }
                                                ],
                                                "valuestoidentify": [
                                                  {
                                                    "type": "rootCauseValue",
                                                    "value": [
                                                      "Coding"
                                                    ]
                                                  },
                                                  {
                                                    "type": "rejectionResolution",
                                                    "value": [
                                                      "Invalid",
                                                      "Duplicate",
                                                      "Unrequired"
                                                    ]
                                                  },
                                                  {
                                                    "type": "qaRootCause",
                                                    "value": [
                                                      "Coding",
                                                      "Configuration",
                                                      "Regression",
                                                      "Data"
                                                    ]
                                                  }
                                                ]
                                              }
      },
      { multi: false }
);

