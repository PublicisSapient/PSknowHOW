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
//----------------7.6.0 Changes ---------------------------
//DTS-26121 Enchancement of Quality Status Overlay
db.kpi_column_configs.updateMany({"kpiId" : "kpi133"},{$set:{"kpiColumnDetails" : [
           		{
           			"columnName" : "Issue Id",
           			"order" : Double("0"),
           			"isShown" : true,
           			"isDefault" : true
           		},
           		{
           			"columnName" : "Issue Type",
           			"order" : Double("1"),
           			"isShown" : true,
           			"isDefault" : true
           		},
           		{
           			"columnName" : "Issue Description",
           			"order" : Double("2"),
           			"isShown" : true,
           			"isDefault" : true
           		},
           		{
           			"columnName" : "Issue Status",
           			"order" : Double("3"),
           			"isShown" : true,
           			"isDefault" : true
           		},
           		{
           			"columnName" : "Priority",
           			"order" : Double("4"),
           			"isShown" : true,
           			"isDefault" : true
           		},
           		{
           			"columnName" : "Linked Stories",
           			"order" : Double("5"),
           			"isShown" : true,
           			"isDefault" : false
           		},
           		{
           			"columnName" : "Linked Stories Size",
           			"order" : Double("6"),
           			"isShown" : true,
           			"isDefault" : false
           		},
           		{
           			"columnName" : "Assignee",
           			"order" : Double("7"),
           			"isShown" : true,
           			"isDefault" : false
           		}
           	] }});

//---- reverse KPI info for KPI 137 (Defect Reopen Rate)

db.getCollection('kpi_master').updateOne(
  { "kpiId": "kpi137" },
  { $set: { "It shows number of defects reopened in a given span of time in comparison to the total defects raised. For all the reopened defects, the average time to reopen is also available." } }
);

//reversing metadata_identifier back when we use to compare metadata_identifier with boardMetadata
db.getCollection('metadata_identifier').update({
        "templateCode": "7"
    }, // Match documents with templateCode equal to "7"
    {
        $set: {
            "tool": "Jira",
            "templateName": "Standard Template",
            "templateCode": "7",
            "isKanban": false,
            "disabled": false,
            "issues": [{
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
            "customfield": [{
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
                },
                {
                    "type": "epicLink",
                    "value": [
                        "Epic Link"
                    ]
                }
            ],
            "workflow": [{
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
            "valuestoidentify": [{
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
    }, {
        multi: false
    }
);

//removing epicLink from documents of metadata_identifier
db.getCollection('metadata_identifier').updateMany(
   { "templateCode": { $in: ["7", "8"] } },
   { $pull: {
      "customfield": {
         "type": "epicLink"
      }
   }}
);

db.action_policy_rule.updateOne(
{
    "name": "Fetch Sprint"
},
{
$set: {
        "name": "Fetch Sprint",
        "roleAllowed": "",
        "description": "super admin and project admin can run active sprint fetch",
        "roleActionCheck": "action == 'TRIGGER_SPRINT_FETCH'",
        "condition": "subject.authorities.contains('ROLE_SUPERADMIN') || subject.authorities.contains('ROLE_PROJECT_ADMIN')",
        "createdDate": new Date(),
        "lastModifiedDate": new Date(),
        "isDeleted": false
    }
});

// delete mapping for sprint velocity
db.field_mapping_structure.deleteMany({
    "fieldName": "jiraIterationIssuetypeKPI39"
});


//------ DTS-27515
db.getCollection('field_mapping_structure').insertOne(
{
    "fieldName": "jiraDevDueDateCustomField",
    "fieldLabel": "Dev Due Date",
    "fieldType": "text",
    "fieldCategory": "fields",
    "section": "Custom Fields Mapping",
    "tooltip": {
        "definition": "This field is to track dev due date of issues tagged in the iteration."
    }
});

db.getCollection('field_mapping_structure').deleteOne(
{
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
   }
);
// --- Reverse fieldType for KPI 138
var fieldNameToUpdate = "readyForDevelopmentStatusKPI138";
  db.getCollection('field_mapping_structure').update(
    { "fieldName": fieldNameToUpdate },
    { $set: {
    "fieldType": "text"
    } },
    { multi: false }
  );

// -- Reverse field by converting the array back to a string

db.field_mapping.find({ readyForDevelopmentStatusKPI138: { $type: 4}}).forEach(function(doc) {

    db.field_mapping.updateMany(
        { _id: doc._id },
        {
            $set: {
                readyForDevelopmentStatusKPI138: doc.readyForDevelopmentStatusKPI138[0]
            }
        }
    );
});

// --------------------- Release 7.7 -----------------------------------------------------------------
// delete mapping for Quality Status

db.field_mapping_structure.deleteMany({
    "fieldName": "jiraItrQSIssueTypeKPI133"
});