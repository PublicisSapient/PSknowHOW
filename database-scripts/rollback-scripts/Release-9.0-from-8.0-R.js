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