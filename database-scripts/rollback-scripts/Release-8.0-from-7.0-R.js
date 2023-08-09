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

//removing epicLink from documents of metadata_identifier
db.getCollection('metadata_identifier').updateMany(
   { "templateCode": { $in: ["7", "8"] } },
   { $pull: {
      "customfield": {
         "type": "epicLink"
      }
   }}
);
