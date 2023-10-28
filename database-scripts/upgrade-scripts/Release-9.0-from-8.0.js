//------------------------------DSV S2 Changes
db.getCollection('field_mapping_structure').insertMany([
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
            "definition": "All issue types that identify with a Story.",

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
    }
])
// Initialize an array to store the bulk write operations
var metaDataOperations = [];

// Add the first update operation to the bulk operations array
metaDataOperations.push({
   updateMany: {
      filter: {
         $or: [
            { "templateCode": "8" },
            { "tool": "Azure" }
         ]
      },
      update: {
         $push: {
            "workflow": {
               "type": "firstDevstatus",
               "value": [
                  "In Analysis",
                  "IN ANALYSIS",
                  "In Development",
                  "In Progress"
               ]
            }
         }
      }
   }
});

metaDataOperations.push({
   updateMany: {
      filter: {
         "templateCode": "7"
      },
      update: {
         $push: {
            "workflow": {
               "type": "jiraStatusForInProgressKPI154",
               "value": [
                  "In Analysis",
                  "In Development",
                  "In Progress"
               ]
            }
         }
      }
   }
});

metaDataOperations.push({
   updateMany: {
      filter: {
         "templateCode": "7"
      },
      update: {
         $push: {
            "workflow": {
               "type": "jiraStatusStartDevelopmentKPI154",
               "value": [
                  "In Analysis",
                  "IN ANALYSIS",
                  "In Development",
                  "In Progress"
               ]
            }
         }
      }
   }
});

metaDataOperations.push({
   updateMany: {
      filter: {
         "templateCode": "7"
      },
      update: {
         $push: {
            "workflow":{
                      "type": "storyFirstStatusKPI154",
                      "value": [
                          "Open"
                      ]
                  }
         }
      }
   }
});

// Execute the bulk write operations
db.getCollection('metadata_identifier').bulkWrite(metaDataOperations);