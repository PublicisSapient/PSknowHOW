//---------8.1.0 changes----------------------------------------------------------------------
db.field_mapping_structure.deleteMany({
    "fieldName": {
        $in: ["thresholdValueKPI14",
            "thresholdValueKPI82",
            "thresholdValueKPI111",
            "thresholdValueKPI35",
            "thresholdValueKPI34",
            "thresholdValueKPI37",
            "thresholdValueKPI28",
            "thresholdValueKPI36",
            "thresholdValueKPI16",
            "thresholdValueKPI17",
            "thresholdValueKPI38",
            "thresholdValueKPI27",
            "thresholdValueKPI72",
            "thresholdValueKPI84",
            "thresholdValueKPI11",
            "thresholdValueKPI62",
            "thresholdValueKPI64",
            "thresholdValueKPI67",
            "thresholdValueKPI65",
            "thresholdValueKPI157",
            "thresholdValueKPI158",
            "thresholdValueKPI159",
            "thresholdValueKPI160",
            "thresholdValueKPI164",
            "startDateCountKPI150"]
    }
});
// delete lead time for change
db.kpi_master.deleteOne({
      "kpiId": "kpi166"
    });

db.field_mapping_structure.deleteMany({
    "fieldName": { $in: [ "jiraStoryIdentificationKPI166", "jiraDodKPI166", "jiraProductionIncidentIdentification"]}
});


db.getCollection('metadata_identifier').updateMany(
   { "templateCode": { $in: ["7"] } },
   { $pull: {
      "workflow": {
         "type":"jiraDodKPI166"
      },
      "issues" : {
       "type": "jiraStoryIdentificationKPI166"
      }
   }}
);
//----DSV-2
db.field_mapping_structure.deleteMany({
    "fieldName": { $in: [ "jiraStatusStartDevelopmentKPI154", "jiraDevDoneStatusKPI154", "jiraQADoneStatusKPI154", "jiraIterationCompletionStatusKPI154", "jiraStatusForInProgressKPI154", "jiraSubTaskIdentification","storyFirstStatusKPI154","jiraOnHoldStatusKPI154"]}
});
// Update documents in a single operation
db.getCollection('metadata_identifier').updateMany(
   {
      $or: [
         { "templateCode": "8" },
         { "tool": "Azure" },
         { "templateCode": "7" }
      ]
   },
   {
      $pull: {
         "workflow": {
            $in: [
               { "type": "firstDevstatus" },
               { "type": "jiraStatusForInProgressKPI154" },
               { "type": "jiraStatusStartDevelopmentKPI154" },
               { "type": "storyFirstStatusKPI154" }
            ]
         }
      }
   }
);

// delete Sonar Code Quality Kpi
db.getCollection('kpi_master').deleteOne(
  { "kpiId": "kpi168" }
);

// delete kpi_category_mapping for Sonar Code Quality
db.kpi_category_mapping.deleteOne({
    "kpiId": "kpi168"
});

db.kpi_master.updateMany(
   { "kpiId" : { $in: ["kpi169"] } },
   { $unset: { kpiFilter: 1 } }
);

// DTS-27379: rollback field mapping structure
db.getCollection("field_mapping_structure").deleteMany({
    "fieldName": {
        $in: ["jiraIssueWaitStateKPI170", "jiraIssueClosedStateKPI170"]
    }
});
// DTS-27379: delete flow efficiency KPI
db.getCollection("kpi_master").deleteOne({
      "kpiId": "kpi170"
    });

db.kpi_master.updateOne({ "kpiId": "kpi138" }, { $set: { "defaultOrder": 8 } })
db.kpi_master.updateOne({ "kpiId": "kpi129" }, { $set: { "defaultOrder": 2 } })
db.kpi_master.updateOne({ "kpiId": "kpi137" }, { $set: { "defaultOrder": 3 } })
db.kpi_master.updateOne({ "kpiId": "kpi161" }, { $set: { "defaultOrder": 5 } })
db.kpi_master.updateOne({ "kpiId": "kpi127" }, { $set: { "defaultOrder": 4 } })
db.kpi_master.updateOne({ "kpiId": "kpi139" }, { $set: { "defaultOrder": 5 } })

// DTS-29397 rollback repo tools
db.getCollection("repo_tools_provider").bulkWrite([
  {
    updateOne: {
      filter: { "toolName": "bitbucket" },
      update: {
        $set: {
          "testServerApiUrl": "",
          "testApiUrl": "https://api.bitbucket.org/2.0/repositories/"
        }
      }
    }
  },
  // Update for gitlab tool
  {
    updateOne: {
      filter: { "toolName": "gitlab" },
      update: {
        $set: {
          "testApiUrl": "https://gitlab.com/api/v4/projects/"
        }
      }
    }
  }
], { ordered: false });

// Change PR size maturity
db.kpi_master.updateOne({ "kpiId": "kpi162" }, { $set: { "calculateMaturity" : true } })

db.kpi_master.updateOne(
{
    "kpiId": "kpi162"
},
{ $set: {
        "calculateMaturity": true,
        "showTrend": true
    }
}
)
db.kpi_master.updateMany(
{
    "kpiId": { $in: [
            "kpi160",
            "kpi158"
        ]
    }
},
{ $set: {
        "upperThresholdBG": "",
        "lowerThresholdBG": ""
    }
}
);

db.field_mapping_structure.updateOne(
    { "fieldName": "jiraDodKPI14" },
    {
        $set: {
            "fieldLabel": "Status considered for defect closure",
            "tooltip": {
                "definition": "Status considered for defect closure (Mention completed status of all types of defects)"
            }
        }
    }
)

db.kpi_master.updateMany(
    {
        "kpiId": { $in: ["kpi152", "kpi155", "kpi151"] }
    },
    {
        $set: { "kpiSubCategory": "Summary" }
    }
);


//------------------------- 8.2.0 changes----------------------------------------------------------------------------------
db.getCollection("field_mapping_structure").deleteMany({
    "fieldName": {
        $in: ["populateByDevDoneKPI150", "jiraDevDoneStatusKPI150"]
    }
});

db.getCollection("kpi_master").updateOne(
    { "kpiId": "kpi150" },
    {
        $set: {
            "kpiInfo.definition": "It shows the cumulative daily actual progress of the release against the overall scope. It also shows additionally the scope added or removed during the release.",
        }
    }
);

db.field_mapping_structure.find(
    { "fieldName" : "uploadDataKPI42" },
    { $rename: { "toggleLabelRight": "toggleLabel" } }
);

db.field_mapping_structure.find(
    { "fieldName" : "uploadDataKPI16" },
    { $rename: { "toggleLabelRight": "toggleLabel" } }
);


//DTS-29689 FTPR Iteration kpi labels
db.field_mapping_structure.deleteOne({
    "fieldName" : "jiraLabelsKPI135"
})


db.kpi_master.updateOne(
  { "kpiId": "kpi3" },
  {
    $set: {
      "thresholdValue": "20",
      "kpiUnit": null,
      "chartType": null,
      "kpiInfo.definition": "Measures Total time between a request was made and  all work on this item is completed and the request was delivered .",
      "kpiInfo.formula":[{"lhs": "It is calculated as the sum Ideation time, Development time & Release time"}],
      "boxType":null,
      "kpiWidth":null,
      "showTrend":true,
      "kpiInfo.details": [
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
      ],
      "kpiFilter": "multiSelectDropDown",
      "yAxisLabel": null,
      "xAxisLabel": null,
      "kpiWidth": 100,
      "showTrend": false,
      "boxType": "2_column",
      "maturityRange": null,
      "aggregationCriteria": null,
      "lowerThresholdBG": null,
      "upperThresholdBG": null,
    }
  }
)
db.getCollection("field_mapping_structure").deleteMany({
    "fieldName": {
        $in: ["thresholdValueKPI3"]
    }
});

db.kpi_master.deleteOne({ "kpiId": "kpi171" });
// Update document with "fieldName" equal to "jiraDorKPI3"
db.field_mapping_structure.updateOne({ "fieldName": "jiraDorKPI171" }, { $set: { "fieldName": "jiraDorKPI3" } });

// Update document with "fieldName" equal to "jiraDodKPI3"
db.field_mapping_structure.updateOne({ "fieldName": "jiraDodKPI171" }, { $set: { "fieldName": "jiraDodKPI3" } });

// Update document with "fieldName" equal to "storyFirstStatusKPI3"
db.field_mapping_structure.updateOne({ "fieldName": "storyFirstStatusKPI171" }, { $set: { "fieldName": "storyFirstStatusKPI3" } });

// Delete documents where "fieldName" is in the specified list
db.field_mapping_structure.deleteMany({
  "fieldName": {
    "$in": ["jiraLiveStatusKPI171", "jiraIssueTypeKPI171"]
  }
});


db.kpi_master.updateOne(
  { "kpiId": "kpi116" },
  {
    $set: {
      "maturityRange": [
        "-50",
        "50-30",
        "30-20",
        "20-10",
        "10-"
      ],
      "maturityLevel": null
    }
  }
);

db.kpi_master.updateOne(
  { "kpiId": "kpi118" },
  {
    $set: {
      "maturityRange": [
        "-1",
        "1-2",
        "2-5",
        "5-10",
        "10-"
      ],
      "aggregationCircleCriteria" : "average",
      "maturityLevel": null
    }
  }
);

db.kpi_master.updateOne(
  { "kpiId": "kpi156" },
  {
    $set: {
      "calculateMaturity": false,
      "maturityRange": [],
      "maturityLevel": [],
      "kpiInfo.maturityLevels": []
    }
  }
);

db.kpi_master.updateOne(
  { "kpiId": "kpi166" },
  {
    $set: {
      "calculateMaturity": false,
      "maturityRange": [],
      "maturityLevel": [],
      "kpiInfo.maturityLevels": []
    }
  }
);

