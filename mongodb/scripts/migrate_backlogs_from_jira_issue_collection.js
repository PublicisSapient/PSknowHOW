print("Start : script for creating and migrating data in backlog collection");

let backlogs = db.jira_issue.find({"sprintAssetState" : {"$in":["","FUTURE"]}, "typeName": { '$ne': "Epic" }});
print("Backlog Fetched : "+backlogs.size());

backlogs.forEach(function(obj){
 var res1 = db.issue_backlog.save(obj);
 var res2 = db.jira_issue.deleteOne({"_id":obj._id})
 let historyObj = db.jira_issue_custom_history.findOne({"storyID" : obj.number, "basicProjectConfigId" : obj.basicProjectConfigId});
 var res3 = db.issue_backlog_custom_history.save(historyObj);
 var res4 = db.jira_issue_custom_history.deleteOne({"_id":historyObj._id});
});

print("End : script for creating and migrating data in backlog collection");