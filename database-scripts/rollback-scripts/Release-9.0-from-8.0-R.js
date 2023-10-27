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
