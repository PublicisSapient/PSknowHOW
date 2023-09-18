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

db.field_mapping_structure.deleteMany({
    "fieldName": "sprintName"
});

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
  { $set: { "kpiInfo.definition" : "It shows number of defects reopened in a given span of time in comparison to the total defects raised. For all the reopened defects, the average time to reopen is also available." } }
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

//updated action_policy "Fetch Sprint"
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

//----------------7.7.0 Changes ---------------------------
// delete mapping for Quality Status and notification enabler
// delete mapping for PI Predictability KPI
db.field_mapping_structure.deleteMany({
    "fieldName": { $in: [ "jiraItrQSIssueTypeKPI133", "notificationEnabler", "epicPlannedValue", "epicAchievedValue", "jiraIssueEpicTypeKPI153","epicLink"]}
});

// delete column config for PI Predictability KPI
db.kpi_column_configs.deleteOne({
    "kpiId": "kpi153"
});

// delete kpi_category_mapping for PI Predictability KPI
db.kpi_category_mapping.deleteOne({
    "kpiId": "kpi153"
});

db.kpi_master.bulkWrite([
  // Reverting Dora dashboard changes
  {
    updateMany: {
      filter: { kpiId: { $in: ["kpi116", "kpi118"] } },
      update: { $unset: { kpiCategory: "" } }
    }
  },
  // Reverse the deployment freq x-axis
  {
    updateOne: {
      filter: { kpiId: "kpi118" },
      update: { $set: { xAxisLabel: "Months" } }
    }
  },
  {
    updateMany: {
      filter: { kpiId: { $in: ["kpi116", "kpi118"] } },
      update: { $set: { groupId: 1 } }
    }
  },
// delete PI Predictability KPI (153)deleting dailyStandup kpi (154)
  {
    deleteMany: {
      filter: { kpiId: { $in: ["kpi153", "kpi154"] } }
    }
  }
]);

// Note : below code only For Opensource project
db.kpi_category_mapping.insertMany([
  {
  	"kpiId" : "kpi116",
  	"categoryId" : "categoryTwo",
  	"kpiOrder" : 15,
  	"kanban" : false
  },
  {
  	"kpiId" : "kpi118",
  	"categoryId" : "categoryThree",
  	"kpiOrder" : 1,
  	"kanban" : false
  },
]);


//------------------------- 7.8.0 changes----------------------------------------------------------------------------------
// delete FieldMapping Field which consider subtask defect  ---------------------------------------------------------------------------

db.field_mapping_structure.deleteMany({
    "fieldName": {
        $in: ["uploadDataKPI42", "uploadDataKPI16", "jiraSubTaskDefectType", "jiraDefectRejectionStatusKPI155", "jiraDodKPI155", "jiraLiveStatusKPI155"]
    }
});

//DTS-26123
db.getCollection('kpi_master').deleteOne(
  { "kpiId": "kpi155" }
);

db.getCollection('metadata_identifier').updateMany(
   { "templateCode": { $in: ["4", "5", "6", "7"] } },
   { $pull: {
      "workflow": {
         "type":"jiraDodKPI155"
      }
   }}
);

//------------------------- 7.9.0 changes----------------------------------------------------------------------------------
//remove search options from fieldmapping structure rollback
db.field_mapping_structure.updateMany(
    {
        "fieldName": {
            $in: ["resolutionTypeForRejectionKPI37",
                "resolutionTypeForRejectionKPI28",
                "resolutionTypeForRejectionDSR",
                "resolutionTypeForRejectionKPI82",
                "resolutionTypeForRejectionKPI135",
                "resolutionTypeForRejectionKPI133",
                "resolutionTypeForRejectionRCAKPI36",
                "resolutionTypeForRejectionKPI14",
                "resolutionTypeForRejectionQAKPI111",
                "resolutionTypeForRejectionKPI35"
            ]
        }
    },
    {
        $set: { "fieldCategory": "workflow" }
    }

)

// delete Scope Churn kpi
db.getCollection('kpi_master').deleteOne(
  { "kpiId": "kpi164" }
);

//delete fieldMapping for Scope Churn KPI
db.field_mapping_structure.deleteMany({
    "fieldName": { $in: ["jiraStoryIdentificationKPI164"]}
});

// delete column config for Scope Churn KPI
db.kpi_column_configs.deleteOne({
    "kpiId": "kpi164"
});

// delete kpi_category_mapping for Scope Churn KPI
db.kpi_category_mapping.deleteOne({
    "kpiId": "kpi164"
});

// Note : below code only For Opensource project
// deleting metadata_identifier for scope churn
db.getCollection('metadata_identifier').updateMany(
   { "templateCode": { $in: ["7"] } },
   { $pull: {
      "workflow": {
         "type":"jiraStoryIdentificationKPI164"
      }
   }}
);



//DTS-28198 remove radio button filter to release kpis
var kpiIdsToUpdate = ["kpi142", "kpi143", "kpi144"];
var originalKpiFilterField = {
  "kpiFilter" : "",
};
db.getCollection("kpi_master").updateMany(
  { kpiId: { $in: kpiIdsToUpdate } },
  { $set: originalKpiFilterField }
);



//Reversing DTS-27550 making release Progress filter to dropdown
db.kpi_master.updateOne(
  { "kpiId": "kpi147" },
  { $set: { "kpiFilter": "multiSelectDropDown" } }
);
