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
            "thresholdValueKPI164"]
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
          "testApiUrl": "https://gitlab.com/api/v4/projects/""
        }
      }
    }
  }
], { ordered: false });

// Change PR size maturity
db.kpi_master.updateOne({ "kpiId": "kpi162" }, { $set: { "calculateMaturity" : true } })